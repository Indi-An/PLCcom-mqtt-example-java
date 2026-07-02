package com.plccom.mqtt.tools.client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

import com.plccom.mqtt.pki.PlccomMqttOwnCertificate;
import com.plccom.mqtt.pki.PlccomMqttPkiStore;

/**
 * Package-private Swing dialog for PLCcom MQTT PKI store maintenance.
 *
 * The dialog deliberately lives in the tool modules, not in Core. Core owns the
 * certificate store and validation rules; the tools provide the operator UI for
 * viewing own, trusted, rejected and issuer certificates.
 */
final class PlccomMqttCertificateManagerDialog {
    private static final String CERTIFICATE_TYPE = "X.509";
    private static final int COL_SUBJECT = 0;
    private static final int COL_ISSUER = 1;
    private static final int COL_VALID_UNTIL = 2;
    private static final int COL_FINGERPRINT = 3;
    private static final int COL_FILE = 4;

    private final JFrame owner;
    private final ResourceBundle texts;
    private final Locale locale;
    private final PlccomMqttPkiStore pkiStore;
    private final String ownAlias;
    private final char[] password;
    private final Consumer<Component> theme;
    private final DateFormat dateFormat;

    private JDialog dialog;

    static void show(JFrame owner, ResourceBundle texts, Locale locale,
            PlccomMqttPkiStore pkiStore, String ownAlias, char[] password,
            Consumer<Component> theme) {
        new PlccomMqttCertificateManagerDialog(owner, texts, locale, pkiStore,
                ownAlias, password, theme).showDialog();
    }

    private PlccomMqttCertificateManagerDialog(JFrame owner, ResourceBundle texts,
            Locale locale, PlccomMqttPkiStore pkiStore, String ownAlias,
            char[] password, Consumer<Component> theme) {
        this.owner = owner;
        this.texts = texts;
        this.locale = locale == null ? Locale.ENGLISH : locale;
        this.pkiStore = pkiStore;
        this.ownAlias = ownAlias;
        this.password = password == null ? new char[0] : Arrays.copyOf(password, password.length);
        this.theme = theme;
        this.dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                DateFormat.SHORT, this.locale);
    }

    private void showDialog() {
        try {
            pkiStore.ensureDirectories();
        } catch (IOException ex) {
            showError(t("cert.error.ensure"), ex);
            return;
        }
        dialog = new JDialog(owner, t("cert.manager.title"), true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(8, 8));
        dialog.add(createContent(), BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        actions.add(button("cert.close", () -> dialog.dispose()));
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.setMinimumSize(new Dimension(920, 600));
        dialog.setSize(980, 640);
        dialog.setLocationRelativeTo(owner);
        if (theme != null) {
            theme.accept(dialog.getContentPane());
        }
        dialog.setVisible(true);
        Arrays.fill(password, '\0');
    }

    private JTabbedPane createContent() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab(t("cert.tab.own"), createOwnPanel());
        tabs.addTab(t("cert.tab.trusted"), createStorePanel(pkiStore.trustedCertsDirectory(), StoreAction.TRUSTED));
        tabs.addTab(t("cert.tab.rejected"), createStorePanel(pkiStore.rejectedCertsDirectory(), StoreAction.REJECTED));
        tabs.addTab(t("cert.tab.issuers"), createStorePanel(pkiStore.issuerCertsDirectory(), StoreAction.ISSUER));
        return tabs;
    }

    private JPanel createOwnPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTextArea details = new JTextArea();
        details.setEditable(false);
        details.setLineWrap(false);
        refreshOwnDetails(details);
        panel.add(new JScrollPane(details), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.add(button("cert.refresh", () -> refreshOwnDetails(details)));
        actions.add(button("cert.createRenew", () -> createOrRenewOwnCertificate(details)));
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createStorePanel(Path directory, StoreAction action) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTable table = new JTable(new CertificateTableModel());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.getColumnModel().getColumn(COL_SUBJECT).setPreferredWidth(250);
        table.getColumnModel().getColumn(COL_ISSUER).setPreferredWidth(250);
        table.getColumnModel().getColumn(COL_VALID_UNTIL).setPreferredWidth(130);
        table.getColumnModel().getColumn(COL_FINGERPRINT).setPreferredWidth(240);
        table.getColumnModel().getColumn(COL_FILE).setPreferredWidth(360);
        refreshTable(table, directory);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.add(button("cert.refresh", () -> refreshTable(table, directory)));
        actions.add(button("cert.details", () -> showSelectedDetails(table)));
        if (action == StoreAction.TRUSTED || action == StoreAction.ISSUER) {
            actions.add(button("cert.import", () -> importCertificate(table, action, directory)));
        }
        if (action == StoreAction.REJECTED) {
            actions.add(button("cert.trust", () -> trustSelectedRejected(table)));
        }
        actions.add(button("cert.delete", () -> deleteSelected(table, directory)));
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshOwnDetails(JTextArea details) {
        StringBuilder builder = new StringBuilder();
        appendLine(builder, "cert.path", pkiStore.getBaseDirectory().toString());
        appendLine(builder, "cert.alias", ownAlias);
        appendLine(builder, "cert.certificatePath", pkiStore.ownCertificatePath(ownAlias).toString());
        appendLine(builder, "cert.privateKeyPath", pkiStore.ownPrivateKeyPath(ownAlias).toString());
        appendLine(builder, "cert.containerPath", pkiStore.ownContainerPath(ownAlias).toString());
        builder.append(System.lineSeparator());
        try {
            Path certificatePath = pkiStore.ownCertificatePath(ownAlias);
            if (Files.exists(certificatePath)) {
                builder.append(formatCertificate(loadCertificate(certificatePath), certificatePath));
            } else {
                builder.append(t("cert.notFound"));
            }
        } catch (Exception ex) {
            builder.append(t("cert.load.failed")).append(' ').append(ex.getMessage());
        }
        details.setText(builder.toString());
        details.setCaretPosition(0);
    }

    private void createOrRenewOwnCertificate(JTextArea details) {
        try {
            PlccomMqttOwnCertificate ownCertificate = new PlccomMqttOwnCertificate(
                    pkiStore, ownAlias, password, 3650, "Indi.An GmbH",
                    Arrays.asList("localhost"),
                    Arrays.asList(InetAddress.getByName("127.0.0.1"),
                            InetAddress.getByName("::1")));
            KeyStore keyStore = ownCertificate.build(true);
            if (keyStore == null) {
                throw new IllegalStateException("Own certificate generation returned no key store.");
            }
            refreshOwnDetails(details);
            JOptionPane.showMessageDialog(dialog, t("cert.own.generated"),
                    t("cert.manager.title"), JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showError(t("cert.own.failed"), ex);
        }
    }

    private void refreshTable(JTable table, Path directory) {
        CertificateTableModel model = (CertificateTableModel) table.getModel();
        model.setRowCount(0);
        try {
            Files.createDirectories(directory);
            try (java.util.stream.Stream<Path> stream = Files.list(directory)) {
                stream.filter(PlccomMqttCertificateManagerDialog::isCertificateFile)
                        .sorted()
                        .forEach(path -> addCertificateRow(model, path));
            }
        } catch (IOException ex) {
            showError(t("cert.load.failed"), ex);
        }
    }

    private void addCertificateRow(CertificateTableModel model, Path path) {
        try {
            X509Certificate certificate = loadCertificate(path);
            model.addRow(new Object[] {
                    certificate.getSubjectX500Principal().getName(),
                    certificate.getIssuerX500Principal().getName(),
                    dateFormat.format(certificate.getNotAfter()),
                    PlccomMqttPkiStore.sha1Thumbprint(certificate),
                    path.toAbsolutePath().normalize().toString()
            });
        } catch (Exception ignored) {
            model.addRow(new Object[] {
                    t("cert.unreadable"),
                    "",
                    "",
                    "",
                    path.toAbsolutePath().normalize().toString()
            });
        }
    }

    private void showSelectedDetails(JTable table) {
        Path path = selectedPath(table);
        if (path == null) {
            return;
        }
        try {
            JTextArea details = new JTextArea(formatCertificate(loadCertificate(path), path));
            details.setEditable(false);
            details.setCaretPosition(0);
            JScrollPane pane = new JScrollPane(details);
            pane.setPreferredSize(new Dimension(760, 360));
            if (theme != null) {
                theme.accept(pane);
            }
            JOptionPane.showMessageDialog(dialog, pane, t("cert.details"),
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showError(t("cert.load.failed"), ex);
        }
    }

    private void importCertificate(JTable table, StoreAction action, Path directory) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(t("cert.import"));
        if (chooser.showOpenDialog(dialog) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            X509Certificate certificate = loadCertificate(chooser.getSelectedFile().toPath());
            if (action == StoreAction.TRUSTED) {
                pkiStore.storeTrustedCertificate(certificate);
            } else if (action == StoreAction.ISSUER) {
                pkiStore.storeIssuerCertificate(certificate);
            }
            refreshTable(table, directory);
            JOptionPane.showMessageDialog(dialog, t("cert.import.success"),
                    t("cert.manager.title"), JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showError(t("cert.import.failed"), ex);
        }
    }

    private void trustSelectedRejected(JTable table) {
        Path path = selectedPath(table);
        if (path == null) {
            return;
        }
        try {
            X509Certificate certificate = loadCertificate(path);
            pkiStore.moveRejectedToTrusted(certificate);
            refreshTable(table, pkiStore.rejectedCertsDirectory());
            JOptionPane.showMessageDialog(dialog, t("cert.trust.success"),
                    t("cert.manager.title"), JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showError(t("cert.trust.failed"), ex);
        }
    }

    private void deleteSelected(JTable table, Path directory) {
        Path path = selectedPath(table);
        if (path == null) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(dialog,
                t("cert.delete.confirm") + System.lineSeparator() + path,
                t("cert.manager.title"), JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            Files.deleteIfExists(path);
            refreshTable(table, directory);
            JOptionPane.showMessageDialog(dialog, t("cert.delete.success"),
                    t("cert.manager.title"), JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            showError(t("cert.delete.failed"), ex);
        }
    }

    private Path selectedPath(JTable table) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(dialog, t("cert.selectFirst"),
                    t("cert.manager.title"), JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Object value = table.getModel().getValueAt(modelRow, COL_FILE);
        return Paths.get(String.valueOf(value));
    }

    private String formatCertificate(X509Certificate certificate, Path path) throws Exception {
        StringBuilder builder = new StringBuilder();
        appendLine(builder, "cert.subject", certificate.getSubjectX500Principal().getName());
        appendLine(builder, "cert.issuer", certificate.getIssuerX500Principal().getName());
        appendLine(builder, "cert.serial", certificate.getSerialNumber().toString(16).toUpperCase(Locale.ROOT));
        appendLine(builder, "cert.validFrom", dateFormat.format(certificate.getNotBefore()));
        appendLine(builder, "cert.validUntil", dateFormat.format(certificate.getNotAfter()));
        appendLine(builder, "cert.fingerprint", PlccomMqttPkiStore.sha1Thumbprint(certificate));
        appendLine(builder, "cert.file", path.toAbsolutePath().normalize().toString());
        return builder.toString();
    }

    private void appendLine(StringBuilder builder, String key, String value) {
        builder.append(t(key)).append(": ").append(value).append(System.lineSeparator());
    }

    private JButton button(String key, Runnable action) {
        JButton button = new JButton(t(key));
        button.addActionListener(event -> action.run());
        return button;
    }

    private String t(String key) {
        return texts.containsKey(key) ? texts.getString(key) : "!" + key + "!";
    }

    private void showError(String message, Exception ex) {
        JOptionPane.showMessageDialog(dialog == null ? owner : dialog,
                message + System.lineSeparator() + ex.getClass().getSimpleName()
                        + ": " + ex.getMessage(),
                t("cert.manager.title"), JOptionPane.ERROR_MESSAGE);
    }

    private static boolean isCertificateFile(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return Files.isRegularFile(path)
                && (name.endsWith(".der") || name.endsWith(".cer")
                        || name.endsWith(".cert") || name.endsWith(".crt")
                        || name.endsWith(".pem"));
    }

    private static X509Certificate loadCertificate(Path path)
            throws IOException, CertificateException {
        CertificateFactory factory = CertificateFactory.getInstance(CERTIFICATE_TYPE);
        try (InputStream input = Files.newInputStream(path)) {
            return (X509Certificate) factory.generateCertificate(input);
        }
    }

    private enum StoreAction {
        TRUSTED, REJECTED, ISSUER
    }

    private final class CertificateTableModel extends DefaultTableModel {
        private static final long serialVersionUID = 1L;

        CertificateTableModel() {
            super(new Object[] {
                    t("cert.column.subject"),
                    t("cert.column.issuer"),
                    t("cert.column.validUntil"),
                    t("cert.column.fingerprint"),
                    t("cert.column.file")
            }, 0);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }
}
