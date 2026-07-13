// MIT License
// Copyright (c) Indi.An GmbH
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

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
    // Column order of the certificate tables. The last column keeps the absolute
    // file path so a selected row can be turned back into a file on disk for the
    // details, trust and delete actions.
    private static final int COL_SUBJECT = 0;
    private static final int COL_ISSUER = 1;
    private static final int COL_VALID_UNTIL = 2;
    private static final int COL_FINGERPRINT = 3;
    private static final int COL_FILE = 4;

    private final JFrame owner;                 // parent window the dialog is centred on
    private final ResourceBundle texts;         // localized UI strings
    private final Locale locale;                // language used for date formatting
    private final PlccomMqttPkiStore pkiStore;  // the on-disk PKI store this dialog edits
    private final String ownAlias;              // alias of this tool's own identity
    private final char[] password;              // private-key password (own copy, wiped on close)
    private final Consumer<Component> theme;    // callback that recolours a component tree
    private final DateFormat dateFormat;        // locale-aware date/time formatter

    private JDialog dialog;                      // the modal window, created lazily in showDialog()

    /**
     * Opens the certificate manager modally and returns once the user closes it.
     * A one-shot instance is created, shown, and then discarded together with its
     * private-key password copy.
     */
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
        // Fall back to English if the caller did not pass a language, so the date
        // formatter and text lookups always have a valid locale.
        this.locale = locale == null ? Locale.ENGLISH : locale;
        this.pkiStore = pkiStore;
        this.ownAlias = ownAlias;
        // Keep our own copy of the password: the caller wipes its array after the
        // call returns, and we wipe ours when the dialog closes (see showDialog()).
        this.password = password == null ? new char[0] : Arrays.copyOf(password, password.length);
        this.theme = theme;
        this.dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                DateFormat.SHORT, this.locale);
    }

    /**
     * Builds the modal window, shows it, and blocks until it is closed. The whole
     * PKI folder layout is created first so the four store tabs always have a
     * directory to list, even on a brand-new profile.
     */
    private void showDialog() {
        // Make sure the own/trusted/rejected/issuer folders exist before we try to
        // list them. If that fails (e.g. no write permission) there is nothing to
        // show, so report it and abort.
        try {
            pkiStore.ensureDirectories();
        } catch (IOException ex) {
            showError(t("cert.error.ensure"), ex);
            return;
        }
        // Modal dialog: the tool's main window stays untouched while it is open.
        dialog = new JDialog(owner, t("cert.manager.title"), true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(8, 8));
        // The tabbed certificate view fills the centre; a single Close button sits
        // in a right-aligned footer.
        dialog.add(createContent(), BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        actions.add(button("cert.close", () -> dialog.dispose()));
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.setMinimumSize(new Dimension(920, 600));
        dialog.setSize(980, 640);
        dialog.setLocationRelativeTo(owner);
        // Apply the host tool's current theme so the dialog matches its dark/light look.
        if (theme != null) {
            theme.accept(dialog.getContentPane());
        }
        dialog.setVisible(true); // blocks here until the dialog is disposed
        // The dialog is closed: wipe our password copy so it does not linger in memory.
        Arrays.fill(password, '\0');
    }

    /** The four store tabs: own identity, trusted, rejected and issuer certificates. */
    private JTabbedPane createContent() {
        JTabbedPane tabs = new JTabbedPane();
        // "Own" is a details/generate view; the other three are file tables backed
        // by their respective PKI subfolders, distinguished by the StoreAction.
        tabs.addTab(t("cert.tab.own"), createOwnPanel());
        tabs.addTab(t("cert.tab.trusted"), createStorePanel(pkiStore.trustedCertsDirectory(), StoreAction.TRUSTED));
        tabs.addTab(t("cert.tab.rejected"), createStorePanel(pkiStore.rejectedCertsDirectory(), StoreAction.REJECTED));
        tabs.addTab(t("cert.tab.issuers"), createStorePanel(pkiStore.issuerCertsDirectory(), StoreAction.ISSUER));
        return tabs;
    }

    /**
     * Builds the "Own certificate" tab: a read-only text area with the paths and
     * details of this tool's identity, plus buttons to refresh the view and to
     * create or renew the certificate.
     */
    private JPanel createOwnPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        // A non-wrapping, read-only text area shows the certificate details as plain text.
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

    /**
     * Builds a certificate table tab for one PKI store folder. The available
     * buttons depend on the store kind: trusted/issuer can import from a file,
     * rejected can promote a certificate to trusted, and all stores can show
     * details and delete a file.
     */
    private JPanel createStorePanel(Path directory, StoreAction action) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        // Single-selection table; one row equals one certificate file on disk.
        JTable table = new JTable(new CertificateTableModel());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        // Give each column a sensible starting width (subject/issuer are long,
        // the file path is the widest).
        table.getColumnModel().getColumn(COL_SUBJECT).setPreferredWidth(250);
        table.getColumnModel().getColumn(COL_ISSUER).setPreferredWidth(250);
        table.getColumnModel().getColumn(COL_VALID_UNTIL).setPreferredWidth(130);
        table.getColumnModel().getColumn(COL_FINGERPRINT).setPreferredWidth(240);
        table.getColumnModel().getColumn(COL_FILE).setPreferredWidth(360);
        refreshTable(table, directory);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Build the action row. Refresh and Details apply to every store; the
        // store-specific actions are added conditionally below.
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.add(button("cert.refresh", () -> refreshTable(table, directory)));
        actions.add(button("cert.details", () -> showSelectedDetails(table)));
        // Only trusted and issuer stores accept certificates imported from a file.
        if (action == StoreAction.TRUSTED || action == StoreAction.ISSUER) {
            actions.add(button("cert.import", () -> importCertificate(table, action, directory)));
        }
        // A rejected certificate can be promoted to trusted after the user inspects it.
        if (action == StoreAction.REJECTED) {
            actions.add(button("cert.trust", () -> trustSelectedRejected(table)));
        }
        actions.add(button("cert.delete", () -> deleteSelected(table, directory)));
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Fills the "Own certificate" text area: first the store paths (base folder,
     * alias, certificate/key/container files), then a blank line, then the parsed
     * certificate details if the file already exists, or a "not created yet" hint.
     */
    private void refreshOwnDetails(JTextArea details) {
        StringBuilder builder = new StringBuilder();
        // Where the identity lives on disk. Showing the exact paths helps an
        // operator find or back up the files outside the tool.
        appendLine(builder, "cert.path", pkiStore.getBaseDirectory().toString());
        appendLine(builder, "cert.alias", ownAlias);
        appendLine(builder, "cert.certificatePath", pkiStore.ownCertificatePath(ownAlias).toString());
        appendLine(builder, "cert.privateKeyPath", pkiStore.ownPrivateKeyPath(ownAlias).toString());
        appendLine(builder, "cert.containerPath", pkiStore.ownContainerPath(ownAlias).toString());
        builder.append(System.lineSeparator());
        // Then the actual certificate content, if it has been created already.
        try {
            Path certificatePath = pkiStore.ownCertificatePath(ownAlias);
            if (Files.exists(certificatePath)) {
                builder.append(formatCertificate(loadCertificate(certificatePath), certificatePath));
            } else {
                builder.append(t("cert.notFound"));
            }
        } catch (Exception ex) {
            // A present but unreadable/broken file: show the reason instead of the details.
            builder.append(t("cert.load.failed")).append(' ').append(ex.getMessage());
        }
        details.setText(builder.toString());
        details.setCaretPosition(0); // scroll back to the top after replacing the text
    }

    /**
     * Creates or renews this tool's own certificate and refreshes the details view.
     * The certificate is a self-signed identity valid for ~10 years, with localhost
     * and the loopback addresses as subject alternative names, which is enough for
     * local test connections. {@code build(true)} forces a fresh certificate.
     */
    private void createOrRenewOwnCertificate(JTextArea details) {
        try {
            PlccomMqttOwnCertificate ownCertificate = new PlccomMqttOwnCertificate(
                    pkiStore, ownAlias, password, 3650, "Indi.An GmbH",
                    Arrays.asList("localhost"),
                    Arrays.asList(InetAddress.getByName("127.0.0.1"),
                            InetAddress.getByName("::1")));
            KeyStore keyStore = ownCertificate.build(true); // true = create/renew now
            if (keyStore == null) {
                throw new IllegalStateException("Own certificate generation returned no key store.");
            }
            refreshOwnDetails(details); // show the newly created certificate
            JOptionPane.showMessageDialog(dialog, t("cert.own.generated"),
                    t("cert.manager.title"), JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showError(t("cert.own.failed"), ex);
        }
    }

    /**
     * Reloads a store table from disk: clears the current rows and lists every
     * certificate file in the folder (sorted by path), adding one row per file.
     */
    private void refreshTable(JTable table, Path directory) {
        CertificateTableModel model = (CertificateTableModel) table.getModel();
        model.setRowCount(0);
        try {
            Files.createDirectories(directory); // tolerate a not-yet-created folder
            try (java.util.stream.Stream<Path> stream = Files.list(directory)) {
                stream.filter(PlccomMqttCertificateManagerDialog::isCertificateFile)
                        .sorted()
                        .forEach(path -> addCertificateRow(model, path));
            }
        } catch (IOException ex) {
            showError(t("cert.load.failed"), ex);
        }
    }

    /**
     * Parses one certificate file and appends it as a table row. A file that cannot
     * be parsed still gets a row (marked "unreadable") so it stays visible and can
     * be deleted, rather than silently disappearing from the list.
     */
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
            // Keep the file listed even if it is broken, so the user can remove it.
            model.addRow(new Object[] {
                    t("cert.unreadable"),
                    "",
                    "",
                    "",
                    path.toAbsolutePath().normalize().toString()
            });
        }
    }

    /** Shows the full details of the selected certificate in a scrollable popup. */
    private void showSelectedDetails(JTable table) {
        Path path = selectedPath(table);
        if (path == null) {
            return; // nothing selected; selectedPath() already warned the user
        }
        try {
            JTextArea details = new JTextArea(formatCertificate(loadCertificate(path), path));
            details.setEditable(false);
            details.setCaretPosition(0);
            JScrollPane pane = new JScrollPane(details);
            pane.setPreferredSize(new Dimension(760, 360));
            if (theme != null) {
                theme.accept(pane); // match the dialog theme
            }
            JOptionPane.showMessageDialog(dialog, pane, t("cert.details"),
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showError(t("cert.load.failed"), ex);
        }
    }

    /**
     * Imports a certificate file the user picks and stores it in the trusted or
     * issuer store (only those two support import), then refreshes the table.
     */
    private void importCertificate(JTable table, StoreAction action, Path directory) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(t("cert.import"));
        if (chooser.showOpenDialog(dialog) != JFileChooser.APPROVE_OPTION) {
            return; // user cancelled the file chooser
        }
        try {
            X509Certificate certificate = loadCertificate(chooser.getSelectedFile().toPath());
            // The store method normalizes the file name/format inside the PKI store.
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

    /**
     * Promotes the selected rejected certificate to the trusted store, so the next
     * connection to that peer is accepted. The rejected table is then reloaded so
     * the moved entry disappears from it.
     */
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

    /** Deletes the selected certificate file after a confirmation prompt. */
    private void deleteSelected(JTable table, Path directory) {
        Path path = selectedPath(table);
        if (path == null) {
            return;
        }
        // Deleting a certificate is irreversible from here, so confirm first and
        // show the exact file path being removed.
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

    /**
     * Returns the file path of the selected table row, or {@code null} (after a
     * warning) when nothing is selected. The path is read from the hidden file
     * column, so it survives table sorting via the model-row conversion.
     */
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

    /**
     * Renders one certificate as a multi-line, localized "label: value" block:
     * subject, issuer, hex serial, validity window, SHA-1 fingerprint and the file.
     */
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

    /** Appends one "localized-label: value" line to the builder. */
    private void appendLine(StringBuilder builder, String key, String value) {
        builder.append(t(key)).append(": ").append(value).append(System.lineSeparator());
    }

    /** A localized button that runs the given action when clicked. */
    private JButton button(String key, Runnable action) {
        JButton button = new JButton(t(key));
        button.addActionListener(event -> action.run());
        return button;
    }

    /** Localized text for a key, or a visible {@code !key!} marker when it is missing. */
    private String t(String key) {
        return texts.containsKey(key) ? texts.getString(key) : "!" + key + "!";
    }

    /** Shows a localized error popup with the exception type and message appended. */
    private void showError(String message, Exception ex) {
        JOptionPane.showMessageDialog(dialog == null ? owner : dialog,
                message + System.lineSeparator() + ex.getClass().getSimpleName()
                        + ": " + ex.getMessage(),
                t("cert.manager.title"), JOptionPane.ERROR_MESSAGE);
    }

    /** True for regular files with a common certificate extension (der/cer/cert/crt/pem). */
    private static boolean isCertificateFile(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return Files.isRegularFile(path)
                && (name.endsWith(".der") || name.endsWith(".cer")
                        || name.endsWith(".cert") || name.endsWith(".crt")
                        || name.endsWith(".pem"));
    }

    /** Reads a DER/PEM certificate file into an {@link X509Certificate}. */
    private static X509Certificate loadCertificate(Path path)
            throws IOException, CertificateException {
        CertificateFactory factory = CertificateFactory.getInstance(CERTIFICATE_TYPE);
        try (InputStream input = Files.newInputStream(path)) {
            return (X509Certificate) factory.generateCertificate(input);
        }
    }

    /** Which PKI store a table tab represents, controlling its available actions. */
    private enum StoreAction {
        TRUSTED, REJECTED, ISSUER
    }

    /** Read-only table model with the localized certificate columns. */
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
            return false; // the table is a viewer; edits happen through the buttons
        }
    }
}
