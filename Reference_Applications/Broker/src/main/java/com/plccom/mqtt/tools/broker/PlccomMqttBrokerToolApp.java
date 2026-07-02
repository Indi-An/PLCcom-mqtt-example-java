package com.plccom.mqtt.tools.broker;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.JTextPane;
import javax.swing.text.JTextComponent;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.plccom.mqtt.OperationResult;
import com.plccom.mqtt.broker.MqttBrokerAdminSnapshot;
import com.plccom.mqtt.broker.MqttBrokerStatistics;
import com.plccom.mqtt.broker.PlccomMqttBroker;
import com.plccom.mqtt.broker.PlccomMqttBrokerAccessDecision;
import com.plccom.mqtt.broker.PlccomMqttBrokerAccessDeniedReason;
import com.plccom.mqtt.broker.PlccomMqttBrokerAccessValidator;
import com.plccom.mqtt.broker.PlccomMqttBrokerBuilder;
import com.plccom.mqtt.broker.PlccomMqttBrokerClientCertificateMode;
import com.plccom.mqtt.broker.PlccomMqttBrokerConnectContext;
import com.plccom.mqtt.broker.PlccomMqttBrokerFactory;
import com.plccom.mqtt.broker.PlccomMqttBrokerListenerOptions;
import com.plccom.mqtt.broker.PlccomMqttBrokerPublishContext;
import com.plccom.mqtt.broker.PlccomMqttBrokerSubscriptionContext;
import com.plccom.mqtt.broker.PlccomMqttBrokerTlsOptions;
import com.plccom.mqtt.client.PlccomMqttException;
import com.plccom.mqtt.diagnostics.PlccomMqttDiagnosticEvent;
import com.plccom.mqtt.diagnostics.PlccomMqttDiagnosticLevel;
import com.plccom.mqtt.pki.PlccomMqttOwnCertificate;
import com.plccom.mqtt.pki.PlccomMqttPkiOptions;
import com.plccom.mqtt.pki.PlccomMqttPkiStore;
import com.plccom.mqtt.pki.PlccomMqttPkiStoreEvent;
import com.plccom.mqtt.pki.PlccomMqttPkiStoreKind;

/**
 * Standalone Swing test application for the PLCcom MQTT broker API.
 *
 * The tool starts an embedded broker and exposes the same operations an
 * operator commonly needs for manual acceptance testing: network listeners, TLS
 * and WebSocket endpoints, access validation, administration snapshots and SDK
 * diagnostics.
 *
 * The application is also useful as a companion to the numbered Broker
 * workshops: the workshops explain the broker API in small source-code steps,
 * while this tool lets a user try the same broker behavior interactively and
 * watch listener, access and diagnostic results in one window.
 */
public final class PlccomMqttBrokerToolApp {
    private static final String MESSAGE_BUNDLE =
            "com.plccom.mqtt.tools.broker.messages";
    private static final String APPLICATION_NAME = "PLCcom.Mqtt.Tools.Broker";
    private static final String DEFAULT_PKI_FOLDER = defaultPkiDirectory().getAbsolutePath();
    private static final String DEFAULT_OWN_CERTIFICATE_ALIAS = APPLICATION_NAME;
    private static final SimpleDateFormat TIME = new SimpleDateFormat("HH:mm:ss.SSS");
    private static final int FORM_LABEL_WIDTH = 220;
    private static final int CONTROL_HEIGHT = 28;
    private static final int BUTTON_HEIGHT = 30;
    private static final int BUTTON_MIN_WIDTH = 96;
    private static final int ICON_BUTTON_SIZE = 30;
    private static final int NUMERIC_CONTROL_WIDTH = 128;
    private static final int COMBO_CONTROL_WIDTH = 180;
    private static final int LISTENER_FIELD_WIDTH = 230;
    private static final int SECURITY_LABEL_WIDTH = 190;
    private static final int SECURITY_FIELD_WIDTH = 230;
    private static final int LISTENER_NAME_COLUMN = 0;
    private static final int LISTENER_TRANSPORT_COLUMN = 1;
    private static final int LISTENER_BIND_COLUMN = 2;
    private static final int LISTENER_PORT_COLUMN = 3;
    private static final int LISTENER_ENDPOINT_COLUMN = 4;
    private static final int LISTENER_CERTIFICATE_COLUMN = 5;
    private static final int LISTENER_REMOVE_COLUMN = 6;
    private static final int LISTENER_ROWS_VISIBLE = 4;
    private static final int LISTENER_COLUMN_WIDTH = 650;
    private static final int LOGO_HEIGHT = 30;
    private static final int STATUS_BADGE_WIDTH = 150;
    private static final String DEFAULT_LISTENER_CERTIFICATE = "";
    private static final String NO_LISTENER_CERTIFICATE = "-";
    private static final String[] OWN_CERTIFICATE_EXTENSIONS = {
            ".der", ".cer", ".cert", ".crt", ".pem"
    };
    private static final String[] OWN_PRIVATE_EXTENSIONS = {
            ".pem", ".key", ".der", ".p12", ".pfx"
    };
    private static final String SETTINGS_FILE_NAME = "broker-tool.properties";
    private static final String ENCRYPTED_PREFIX = "enc:v1:";
    private static final byte[] SETTINGS_SALT = new byte[] {
            80, 76, 67, 99, 111, 109, 77, 81, 84, 84, 84, 111, 111, 108, 115, 49
    };
    private static final SecureRandom SETTINGS_RANDOM = new SecureRandom();

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "PLCcom-MQTT-Broker-Tool");
        thread.setDaemon(true);
        return thread;
    });
    private final Map<JLabel, String> labelKeys = new LinkedHashMap<JLabel, String>();
    private final Map<AbstractButton, String> buttonKeys = new LinkedHashMap<AbstractButton, String>();
    private final Map<JComponent, String> tooltipKeys = new LinkedHashMap<JComponent, String>();
    private final Set<AbstractButton> primaryButtons =
            java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<AbstractButton, Boolean>());
    private final List<String> tabKeys = new ArrayList<String>();
    private final Set<String> promptedRejectedCertificates = ConcurrentHashMap.newKeySet();

    private Locale locale = Locale.GERMAN;
    private ResourceBundle texts = ResourceBundle.getBundle(MESSAGE_BUNDLE, locale);
    private Palette palette = Palette.dark();
    private Status currentStatus = Status.NEUTRAL;
    private PlccomMqttBroker broker;
    private boolean settingsPersistenceEnabled = true;
    private boolean settingsSaved;

    private JFrame frame;
    private JPanel root;
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JComponent logoPanel;
    private JLabel statusPill;
    private JButton themeButton;
    private JButton languageButton;
    private JButton exitButton;
    private JButton certificateButton;
    private JButton helpButton;
    private JButton startButton;
    private JButton stopButton;
    private LogPanel logPanel;
    private DefaultTableModel statisticsModel;
    private JTabbedPane tabs;
    private final AtomicBoolean operationActive = new AtomicBoolean();

    private JTextField licenseUserField;
    private JTextField licenseSerialField;
    private DefaultTableModel listenerModel;
    private JTable listenerTable;
    private JPasswordField certificatePasswordField;
    private JTextField pkiStoreField;
    private JTextField pkiOwnAliasField;
    private JComboBox<String> clientCertificateModeCombo;
    private JCheckBox demoAccessValidatorCheck;
    private JCheckBox requireUsernameCheck;
    private JCheckBox requireClientCertificateCheck;
    private JTextField blockedTopicPrefixField;
    private JSpinner maxConnectionsSpinner;
    private JSpinner socketReadTimeoutSpinner;
    private JComboBox<String> diagnosticLevelCombo;
    private JSpinner diagnosticThrottleSpinner;


    /**
     * Starts the standalone broker test application.
     *
     * @param args optional command line arguments. Passing
     *        {@code --screenshot <file>} renders the main window into a PNG file
     *        and exits; {@code --light}, {@code --english} and
     *        {@code --tab <index>} adjust that screenshot.
     * @throws Exception if screenshot rendering fails
     */
    public static void main(String[] args) throws Exception {
        final StartupArguments startup = StartupArguments.parse(args);
        if (startup.screenshotFile != null) {
            SwingUtilities.invokeAndWait(() -> {
                PlccomMqttBrokerToolApp app = new PlccomMqttBrokerToolApp();
                app.settingsPersistenceEnabled = false;
                app.show();
                if (startup.light) {
                    app.setLightTheme();
                }
                if (startup.english) {
                    app.setLanguage(Locale.ENGLISH);
                }
                if (startup.tabIndex >= 0 && startup.tabIndex < app.tabs.getTabCount()) {
                    app.tabs.setSelectedIndex(startup.tabIndex);
                }
                app.frame.setSize(1180, 900);
                app.frame.setLocationRelativeTo(null);
                try {
                    app.writeScreenshot(startup.screenshotFile);
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                } finally {
                    app.shutdown();
                    app.frame.dispose();
                }
            });
            System.exit(0);
            return;
        }
        SwingUtilities.invokeLater(() -> new PlccomMqttBrokerToolApp().show());
    }

    private void show() {
        buildUi();
        loadSettings();
        ensureConfiguredPkiDirectories();
        translate();
        applyTheme();
        updateActionStates();
        frame.setVisible(true);
        logInfo("log.ready");
    }

    private void buildUi() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                shutdown();
            }
        });

        root = new JPanel(new BorderLayout(0, 0));
        frame.setContentPane(root);
        root.add(createHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(10, 10));
        body.setBorder(new EmptyBorder(10, 12, 12, 12));
        body.add(createLicenseBar(), BorderLayout.NORTH);

        body.add(createTabs(), BorderLayout.CENTER);
        body.add(createLogPanel(), BorderLayout.SOUTH);

        root.add(body, BorderLayout.CENTER);
        root.add(createFooterActions(), BorderLayout.SOUTH);
        frame.setMinimumSize(new Dimension(1080, 820));
        frame.setSize(1180, 900);
        frame.setLocationRelativeTo(null);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(14, 0));
        header.setName("header");
        header.setBorder(new EmptyBorder(10, 16, 10, 16));

        logoPanel = new LogoCard(loadLogo());
        logoPanel.setName("logoPanel");
        header.add(logoPanel, BorderLayout.WEST);

        JPanel titlePanel = new JPanel(new BorderLayout(0, 3));
        titlePanel.setOpaque(false);
        titleLabel = new JLabel();
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        subtitleLabel = new JLabel();
        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.PLAIN, 12f));
        subtitleLabel.setName("subtitle");
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        header.add(titlePanel, BorderLayout.CENTER);

        statusPill = new JLabel();
        statusPill.setHorizontalAlignment(SwingConstants.CENTER);
        statusPill.setIconTextGap(8);
        setFixedSize(statusPill, STATUS_BADGE_WIDTH, BUTTON_HEIGHT);
        statusPill.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(palette.border),
                new EmptyBorder(6, 12, 6, 12)));
        header.add(statusPill, BorderLayout.EAST);
        return header;
    }

    private JPanel createFooterActions() {
        JPanel footer = new JPanel(new BorderLayout(8, 0));
        footer.setName("footer");
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, palette.border),
                new EmptyBorder(8, 16, 10, 16)));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        themeButton = button("button.theme", ToolIcon.THEME, this::toggleTheme);
        languageButton = button("button.language", ToolIcon.LANGUAGE, this::toggleLanguage);
        certificateButton = button("button.certificates", ToolIcon.CERTIFICATE, this::showCertificateManager);
        helpButton = button("button.help", ToolIcon.REQUEST, this::openHelp);
        exitButton = button("button.exit", ToolIcon.EXIT, this::exitApplication);
        actions.add(themeButton);
        actions.add(languageButton);
        actions.add(certificateButton);
        actions.add(helpButton);
        actions.add(exitButton);
        footer.add(actions, BorderLayout.EAST);
        return footer;
    }

    private JPanel createLicenseBar() {
        JPanel panel = cardPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel title = label("license.title");
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(title, gbc);

        JLabel hint = label("license.hint");
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        panel.add(hint, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(label("license.user"), gbc);
        licenseUserField = textField(12, "");
        setFixedSize(licenseUserField, 112, CONTROL_HEIGHT);
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.18;
        panel.add(licenseUserField, gbc);

        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(label("license.serial"), gbc);
        licenseSerialField = textField(24, "");
        setFixedSize(licenseSerialField, 190, CONTROL_HEIGHT);
        gbc.gridx = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.25;
        panel.add(licenseSerialField, gbc);
        return panel;
    }

    private JTabbedPane createTabs() {
        tabs = new JTabbedPane();
        tabs.addChangeListener(event -> refreshTabTitles());
        addToolTab("tab.listeners", createListenersPanel());
        addToolTab("tab.statistics", createStatisticsPanel());
        refreshTabTitles();
        return tabs;
    }

    private void addToolTab(String key, Component content) {
        tabKeys.add(key);
        tabs.addTab(t(key), content);
    }

    private void refreshTabTitles() {
        if (tabs == null) {
            return;
        }
        for (int index = 0; index < tabKeys.size(); index++) {
            tabs.setTitleAt(index, t(tabKeys.get(index)));
            JLabel label = new JLabel(t(tabKeys.get(index)));
            label.setOpaque(true);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            boolean selected = tabs.getSelectedIndex() == index;
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0,
                            selected ? palette.accent : palette.border),
                    new EmptyBorder(7, 18, 7, 18)));
            label.setBackground(selected ? palette.surface : palette.surfaceAlt);
            label.setForeground(selected ? palette.accent : palette.muted);
            label.setFont(label.getFont().deriveFont(selected ? Font.BOLD : Font.PLAIN, 13f));
            tabs.setTabComponentAt(index, label);
        }
    }

    private Component createListenersPanel() {
        listenerModel = createListenerModel();
        listenerTable = new JTable(listenerModel);
        listenerTable.setFillsViewportHeight(true);
        listenerTable.setRowHeight(24);
        listenerTable.setAutoCreateRowSorter(false);
        listenerTable.getTableHeader().setReorderingAllowed(false);
        listenerTable.setPreferredScrollableViewportSize(
                new Dimension(520, LISTENER_ROWS_VISIBLE * listenerTable.getRowHeight()));
        listenerTable.setDefaultRenderer(Object.class, new ThemedTableRenderer());
        listenerModel.addTableModelListener(event -> {
            int column = event.getColumn();
            if (column == LISTENER_ENDPOINT_COLUMN
                    || column == LISTENER_CERTIFICATE_COLUMN
                    || column == LISTENER_REMOVE_COLUMN) {
                return;
            }
            refreshListenerDerivedColumns();
        });
        configureListenerCertificateColumn();
        configureRemoveColumn();
        addListenerRow("Main TCP", "TCP", "127.0.0.1", 1883);
        certificatePasswordField = new JPasswordField(24);
        normalizeControlHeight(certificatePasswordField);
        pkiStoreField = textField(22, DEFAULT_PKI_FOLDER);
        setFixedSize(pkiStoreField, 220, CONTROL_HEIGHT);
        pkiOwnAliasField = textField(14, DEFAULT_OWN_CERTIFICATE_ALIAS);
        setFixedSize(pkiOwnAliasField, 220, CONTROL_HEIGHT);
        clientCertificateModeCombo = combo("NONE", "OPTIONAL", "REQUIRED");
        demoAccessValidatorCheck = checkBox("access.demoValidator", false);
        requireUsernameCheck = checkBox("access.requireUsername", false);
        requireClientCertificateCheck = checkBox("access.requireClientCertificate", false);
        blockedTopicPrefixField = textField(24, "blocked/");
        setFixedSize(blockedTopicPrefixField, SECURITY_FIELD_WIDTH, CONTROL_HEIGHT);
        maxConnectionsSpinner = plainSpinner(1000, 1, 200000, 10);
        socketReadTimeoutSpinner = spinner(1000, 0, 600000, 250);
        diagnosticLevelCombo = combo("Debug / all", "Info", "Warnings", "Errors", "Fatal only", "Off");
        diagnosticLevelCombo.setSelectedIndex(1);
        diagnosticThrottleSpinner = spinner(1000, 0, 60000, 250);
        applyListenerFieldWidth(maxConnectionsSpinner, socketReadTimeoutSpinner,
                pkiStoreField, pkiOwnAliasField);
        applySecurityFieldWidth(certificatePasswordField, clientCertificateModeCombo,
                blockedTopicPrefixField, diagnosticThrottleSpinner);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel columns = new JPanel(new GridBagLayout());
        columns.setBorder(BorderFactory.createEmptyBorder());
        GridBagConstraints columnGbc = new GridBagConstraints();
        columnGbc.gridy = 0;
        columnGbc.anchor = GridBagConstraints.NORTHWEST;
        columnGbc.fill = GridBagConstraints.BOTH;
        columnGbc.weighty = 1;

        JPanel networkPanel = formPanel();
        GridBagConstraints networkGbc = formGbc();
        int networkRow = 0;
        addWide(networkPanel, networkGbc, networkRow++, sectionTitle("section.listeners"));
        JScrollPane listenerScroll = new JScrollPane(listenerTable);
        listenerScroll.setPreferredSize(new Dimension(LISTENER_COLUMN_WIDTH, 122));
        listenerScroll.setMinimumSize(new Dimension(520, 122));
        registerTooltip(listenerScroll, "listener.table.tip");
        networkGbc.gridx = 0;
        networkGbc.gridy = networkRow++;
        networkGbc.gridwidth = 2;
        networkGbc.weightx = 1;
        networkGbc.fill = GridBagConstraints.BOTH;
        networkPanel.add(listenerScroll, networkGbc);
        networkGbc.gridwidth = 1;
        addWide(networkPanel, networkGbc, networkRow++, listenerAddButtons());
        addRow(networkPanel, networkGbc, networkRow++, "listener.maxConnections",
                listenerInput(maxConnectionsSpinner));
        addRow(networkPanel, networkGbc, networkRow++, "listener.socketReadTimeout",
                listenerInput(socketReadTimeoutSpinner));
        addFormBottomFiller(networkPanel, networkGbc, networkRow);

        JPanel securityPanel = formPanel();
        GridBagConstraints securityGbc = formGbc();
        int securityRow = 0;
        addWide(securityPanel, securityGbc, securityRow++, sectionTitle("section.security"));
        addCompactRow(securityPanel, securityGbc, securityRow++, "listener.certificatePassword",
                compactInput(certificatePasswordField), SECURITY_LABEL_WIDTH);
        addCompactRow(securityPanel, securityGbc, securityRow++, "listener.clientCertMode",
                compactInput(clientCertificateModeCombo), SECURITY_LABEL_WIDTH);

        // Stacked vertically so all demo access options stay fully visible regardless
        // of the translated label lengths.
        JPanel access = new JPanel(new GridLayout(0, 1, 0, 4));
        access.setOpaque(false);
        access.setBorder(BorderFactory.createEmptyBorder());
        access.add(demoAccessValidatorCheck);
        access.add(requireUsernameCheck);
        access.add(requireClientCertificateCheck);
        addWide(securityPanel, securityGbc, securityRow++, sectionTitle("access.row"));
        addWide(securityPanel, securityGbc, securityRow++, access);
        addCompactRow(securityPanel, securityGbc, securityRow++, "access.blockedPrefix",
                compactInput(blockedTopicPrefixField), SECURITY_LABEL_WIDTH);
        addCompactRow(securityPanel, securityGbc, securityRow++, "listener.diagnosticLevel",
                compactInput(diagnosticLevelCombo), SECURITY_LABEL_WIDTH);
        addCompactRow(securityPanel, securityGbc, securityRow++, "listener.diagnosticThrottle",
                compactInput(diagnosticThrottleSpinner), SECURITY_LABEL_WIDTH);
        addFormBottomFiller(securityPanel, securityGbc, securityRow);

        // The security form column hugs its content (weightx 0) so its fixed-width
        // fields are never stranded in dead space; the listener column takes the
        // extra width (weightx 1) because the multi-column table benefits from it.
        // Both field widths and labels are fixed, so the tab is identical in DE/EN
        // and stays tidy at any window width.
        columnGbc.gridx = 0;
        columnGbc.weightx = 1;
        columnGbc.insets = new Insets(0, 0, 0, 5);
        columns.add(networkPanel, columnGbc);

        columnGbc.gridx = 1;
        columnGbc.weightx = 0;
        columnGbc.insets = new Insets(0, 5, 0, 0);
        columns.add(securityPanel, columnGbc);
        panel.add(columns, BorderLayout.CENTER);

        startButton = primaryButton("button.start", this::startBroker);
        stopButton = button("button.stop", this::stopBroker);
        panel.add(actionBar(startButton, stopButton), BorderLayout.SOUTH);
        return panel;
    }


    private DefaultTableModel createListenerModel() {
        return new DefaultTableModel(new Object[] {
                t("listener.column.name"),
                t("listener.column.transport"),
                t("listener.column.bind"),
                t("listener.column.port"),
                t("listener.column.endpoint"),
                t("listener.column.certificate"),
                "" }, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                if (column == LISTENER_ENDPOINT_COLUMN || column == LISTENER_REMOVE_COLUMN) {
                    return false;
                }
                if (column == LISTENER_CERTIFICATE_COLUMN) {
                    return isSecureTransport(String.valueOf(getValueAt(row, LISTENER_TRANSPORT_COLUMN)));
                }
                return true;
            }
        };
    }

    private Component listenerAddButtons() {
        JButton addTcp = button("button.addTcpListener", () -> showListenerEditor(-1, "TCP"));
        JButton addTls = button("button.addTlsListener", () -> showListenerEditor(-1, "TLS"));
        JButton addWs = button("button.addWebSocketListener", () -> showListenerEditor(-1, "WS"));
        JButton addWss = button("button.addSecureWebSocketListener", () -> showListenerEditor(-1, "WSS"));
        JButton edit = button("button.editListener", this::editSelectedListener);
        // GridLayout keeps all listener command buttons visually aligned.
        JPanel row = new JPanel(new GridLayout(1, 5, 8, 0));
        row.setOpaque(false);
        row.add(addTcp);
        row.add(addTls);
        row.add(addWs);
        row.add(addWss);
        row.add(edit);
        return row;
    }

    /**
     * Configures the last listener column as a per-row remove affordance: a small
     * trash glyph that deletes that listener on click. This keeps removal directly
     * on each row instead of needing a separate button plus selection.
     */
    private void configureRemoveColumn() {
        javax.swing.table.TableColumn column =
                listenerTable.getColumnModel().getColumn(LISTENER_REMOVE_COLUMN);
        column.setMinWidth(34);
        column.setMaxWidth(34);
        column.setResizable(false);
        column.setCellRenderer(new ListenerRemoveRenderer());
        listenerTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                int viewRow = listenerTable.rowAtPoint(event.getPoint());
                int viewColumn = listenerTable.columnAtPoint(event.getPoint());
                if (viewRow < 0 || viewColumn < 0) {
                    return;
                }
                int modelColumn = listenerTable.convertColumnIndexToModel(viewColumn);
                int modelRow = listenerTable.convertRowIndexToModel(viewRow);
                if (modelColumn == LISTENER_REMOVE_COLUMN && listenerTable.getRowCount() > 1) {
                    removeListenerRow(modelRow);
                    return;
                }
                if (event.getClickCount() == 2) {
                    showListenerEditor(modelRow, cellTextNoThrow(modelRow, LISTENER_TRANSPORT_COLUMN));
                }
            }
        });
    }

    private void configureListenerCertificateColumn() {
        TableColumn endpointColumn = listenerTable.getColumnModel().getColumn(LISTENER_ENDPOINT_COLUMN);
        endpointColumn.setPreferredWidth(190);
        endpointColumn.setMinWidth(130);

        TableColumn certificateColumn = listenerTable.getColumnModel().getColumn(LISTENER_CERTIFICATE_COLUMN);
        certificateColumn.setPreferredWidth(160);
        certificateColumn.setMinWidth(120);
        certificateColumn.setCellEditor(listenerCertificateEditor());
    }

    private void configureCompactListenerTable() {
        hideListenerColumn(LISTENER_PORT_COLUMN);
        hideListenerColumn(LISTENER_BIND_COLUMN);
        hideListenerColumn(LISTENER_TRANSPORT_COLUMN);
        setListenerColumnWidth(LISTENER_NAME_COLUMN, 130, 110, 180);
        setListenerColumnWidth(LISTENER_ENDPOINT_COLUMN, 285, 220, 420);
        setListenerColumnWidth(LISTENER_CERTIFICATE_COLUMN, 170, 120, 240);
        listenerTable.setToolTipText(t("listener.table.tip"));
    }

    private void hideListenerColumn(int modelColumn) {
        TableColumn column = listenerColumn(modelColumn);
        if (column != null) {
            listenerTable.removeColumn(column);
        }
    }

    private void setListenerColumnWidth(int modelColumn, int preferred, int minimum, int maximum) {
        TableColumn column = listenerColumn(modelColumn);
        if (column == null) {
            return;
        }
        column.setPreferredWidth(preferred);
        column.setMinWidth(minimum);
        column.setMaxWidth(maximum);
    }

    private TableColumn listenerColumn(int modelColumn) {
        if (listenerTable == null) {
            return null;
        }
        for (int index = 0; index < listenerTable.getColumnModel().getColumnCount(); index++) {
            TableColumn column = listenerTable.getColumnModel().getColumn(index);
            if (column.getModelIndex() == modelColumn) {
                return column;
            }
        }
        return null;
    }
    private DefaultCellEditor listenerCertificateEditor() {
        JComboBox<String> combo = listenerCertificateCombo();
        return new DefaultCellEditor(combo) {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value,
                    boolean selected, int row, int column) {
                Component component = super.getTableCellEditorComponent(table, value, selected, row, column);
                combo.setSelectedItem(normalizeListenerCertificate(
                        value == null ? null : String.valueOf(value)));
                return component;
            }

            @Override
            public Object getCellEditorValue() {
                return normalizeListenerCertificate(String.valueOf(combo.getSelectedItem()));
            }
        };
    }

    private JComboBox<String> listenerCertificateCombo() {
        JComboBox<String> combo = new JComboBox<String>(
                new DefaultComboBoxModel<String>(listenerCertificateOptions().toArray(new String[0])));
        combo.putClientProperty("plccom.listenerCertificateCombo", Boolean.TRUE);
        combo.setRenderer(new ListenerCertificateListRenderer());
        return combo;
    }

    private List<String> listenerCertificateOptions() {
        TreeSet<String> aliases = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        aliases.add(DEFAULT_OWN_CERTIFICATE_ALIAS);
        try {
            PlccomMqttPkiStore pkiStore = createPkiStore();
            addOwnAliases(aliases, pkiStore.ownCertsDirectory(), OWN_CERTIFICATE_EXTENSIONS);
            addOwnAliases(aliases, pkiStore.ownPrivateDirectory(), OWN_PRIVATE_EXTENSIONS);
        } catch (IOException | RuntimeException ignored) {
            // The combo remains usable with the default broker identity even if
            // the PKI folder cannot be scanned during UI setup.
        }
        List<String> options = new ArrayList<String>();
        options.add(DEFAULT_LISTENER_CERTIFICATE);
        for (String alias : aliases) {
            String normalized = normalizeListenerCertificate(alias);
            if (normalized.length() > 0) {
                options.add(normalized);
            }
        }
        return options;
    }

    private String displayListenerCertificate(Object value) {
        String alias = normalizeListenerCertificate(value == null ? null : String.valueOf(value));
        return alias.length() == 0 ? t("listener.certificate.default") : alias;
    }

    private void refreshListenerCertificateEditor() {
        TableColumn certificateColumn = listenerColumn(LISTENER_CERTIFICATE_COLUMN);
        if (certificateColumn == null) {
            return;
        }
        certificateColumn.setCellEditor(listenerCertificateEditor());
        refreshListenerDerivedColumns();
    }

    private void addOwnAliases(Set<String> aliases, Path directory, String[] extensions) throws IOException {
        if (!Files.isDirectory(directory)) {
            return;
        }
        Set<String> extensionSet = new HashSet<String>(Arrays.asList(extensions));
        try (Stream<Path> stream = Files.list(directory)) {
            stream.filter(Files::isRegularFile)
                    .map(path -> aliasFromFile(path, extensionSet))
                    .filter(alias -> alias != null && alias.trim().length() > 0)
                    .forEach(aliases::add);
        }
    }

    private static String aliasFromFile(Path path, Set<String> extensions) {
        String name = path.getFileName() == null ? "" : path.getFileName().toString();
        String lower = name.toLowerCase(Locale.ROOT);
        for (String extension : extensions) {
            if (lower.endsWith(extension)) {
                return name.substring(0, name.length() - extension.length());
            }
        }
        return null;
    }

    private void editSelectedListener() {
        if (listenerTable == null || listenerTable.getRowCount() == 0) {
            return;
        }
        int viewRow = listenerTable.getSelectedRow();
        if (viewRow < 0) {
            viewRow = 0;
            listenerTable.getSelectionModel().setSelectionInterval(0, 0);
        }
        int modelRow = listenerTable.convertRowIndexToModel(viewRow);
        showListenerEditor(modelRow, cellTextNoThrow(modelRow, LISTENER_TRANSPORT_COLUMN));
    }

    private void showListenerEditor(int modelRow, String initialTransport) {
        String transport = normalizeTransport(initialTransport);
        boolean edit = modelRow >= 0 && modelRow < listenerModel.getRowCount();
        JTextField nameField = textField(20, edit
                ? cellTextNoThrow(modelRow, LISTENER_NAME_COLUMN)
                : defaultListenerName(transport));
        JComboBox<String> transportCombo = combo("TCP", "TLS", "WS", "WSS");
        transportCombo.setSelectedItem(edit ? normalizeTransport(cellTextNoThrow(modelRow, LISTENER_TRANSPORT_COLUMN)) : transport);
        JTextField bindField = textField(18, edit ? cellTextNoThrow(modelRow, LISTENER_BIND_COLUMN) : "127.0.0.1");
        int port = edit ? portCellNoThrow(modelRow, LISTENER_PORT_COLUMN) : nextListenerPort(defaultPortForTransport(transport));
        JSpinner portSpinner = plainSpinner(Math.max(0, port), 0, 65535, 1);
        JComboBox<String> certificateCombo = listenerCertificateCombo();
        certificateCombo.setSelectedItem(edit
                ? displayListenerCertificate(cellTextNoThrow(modelRow, LISTENER_CERTIFICATE_COLUMN))
                : t("listener.certificate.default"));
        JLabel endpointPreview = new JLabel();

        JPanel form = formPanel();
        GridBagConstraints gbc = formGbc();
        int row = 0;
        addDialogRow(form, gbc, row++, "listener.editor.name", nameField);
        addDialogRow(form, gbc, row++, "listener.editor.transport", transportCombo);
        addDialogRow(form, gbc, row++, "listener.editor.bind", bindField);
        addDialogRow(form, gbc, row++, "listener.editor.port", portSpinner);
        addDialogRow(form, gbc, row++, "listener.editor.certificate", certificateCombo);
        addDialogRow(form, gbc, row++, "listener.editor.endpoint", endpointPreview);
        theme(form);

        Runnable refresh = () -> {
            String selectedTransport = normalizeTransport(String.valueOf(transportCombo.getSelectedItem()));
            boolean secure = isSecureTransport(selectedTransport);
            certificateCombo.setEnabled(secure);
            endpointPreview.setText(endpointFor(selectedTransport, bindField.getText(), ((Number) portSpinner.getValue()).intValue()));
        };
        transportCombo.addActionListener(event -> refresh.run());
        bindField.getDocument().addDocumentListener(new SimpleDocumentListener(refresh));
        portSpinner.addChangeListener(event -> refresh.run());
        refresh.run();

        while (true) {
            int result = JOptionPane.showConfirmDialog(frame, form,
                    t(edit ? "listener.editor.title.edit" : "listener.editor.title.add"),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) {
                return;
            }
            String name = trimText(nameField.getText());
            String selectedTransport = normalizeTransport(String.valueOf(transportCombo.getSelectedItem()));
            String bindAddress = trimText(bindField.getText());
            int selectedPort = ((Number) portSpinner.getValue()).intValue();
            String certificate = isSecureTransport(selectedTransport)
                    ? normalizeListenerCertificate(String.valueOf(certificateCombo.getSelectedItem()))
                    : NO_LISTENER_CERTIFICATE;
            if (name.length() == 0) {
                JOptionPane.showMessageDialog(frame, t("error.listenerName"), t("status.error"), JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (containsListenerName(name, edit ? modelRow : -1)) {
                JOptionPane.showMessageDialog(frame, t("error.listenerNameDuplicate"), t("status.error"), JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (edit) {
                listenerModel.setValueAt(name, modelRow, LISTENER_NAME_COLUMN);
                listenerModel.setValueAt(selectedTransport, modelRow, LISTENER_TRANSPORT_COLUMN);
                listenerModel.setValueAt(bindAddress, modelRow, LISTENER_BIND_COLUMN);
                listenerModel.setValueAt(Integer.valueOf(selectedPort), modelRow, LISTENER_PORT_COLUMN);
                listenerModel.setValueAt(certificate, modelRow, LISTENER_CERTIFICATE_COLUMN);
                refreshListenerDerivedColumns();
                listenerTable.getSelectionModel().setSelectionInterval(modelRow, modelRow);
            } else {
                addListenerRow(name, selectedTransport, bindAddress, selectedPort, certificate);
            }
            return;
        }
    }

    private void addDialogRow(JPanel panel, GridBagConstraints gbc, int row, String labelKey, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel rowLabel = new JLabel(t(labelKey));
        panel.add(rowLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
    }

    private static String normalizeTransport(String transport) {
        String normalized = transport == null ? "TCP" : transport.trim().toUpperCase(Locale.ROOT);
        if ("WEBSOCKET".equals(normalized)) {
            return "WS";
        }
        if ("TLS".equals(normalized) || "WS".equals(normalized) || "WSS".equals(normalized)) {
            return normalized;
        }
        return "TCP";
    }

    private int defaultPortForTransport(String transport) {
        String normalized = normalizeTransport(transport);
        if ("TLS".equals(normalized)) {
            return 8883;
        }
        if ("WS".equals(normalized)) {
            return 8080;
        }
        if ("WSS".equals(normalized)) {
            return 8443;
        }
        return 1883;
    }
    private void addListenerRow(String name, String transport, String bindAddress, int port) {
        addListenerRow(name, transport, bindAddress, port, DEFAULT_LISTENER_CERTIFICATE);
    }

    private void addListenerRow(String name, String transport, String bindAddress, int port,
            String certificateAlias) {
        String alias = isSecureTransport(transport) ? normalizeListenerCertificate(certificateAlias)
                : NO_LISTENER_CERTIFICATE;
        listenerModel.addRow(new Object[] {
                name,
                transport,
                bindAddress,
                Integer.valueOf(port),
                endpointFor(transport, bindAddress, port),
                alias,
                "" });
        if (listenerTable != null) {
            int row = listenerModel.getRowCount() - 1;
            listenerTable.getSelectionModel().setSelectionInterval(row, row);
        }
        refreshListenerDerivedColumns();
        updateActionStates();
    }

    private void removeListenerRow(int modelRow) {
        // At least one listener must always remain (the remove icon is disabled then).
        if (listenerModel.getRowCount() <= 1 || modelRow < 0
                || modelRow >= listenerModel.getRowCount()) {
            return;
        }
        listenerModel.removeRow(modelRow);
        if (listenerModel.getRowCount() > 0) {
            int next = Math.min(modelRow, listenerModel.getRowCount() - 1);
            listenerTable.getSelectionModel().setSelectionInterval(next, next);
        }
        updateActionStates();
    }

    private String defaultListenerName(String transport) {
        String prefix = transport == null || transport.trim().length() == 0
                ? "Listener"
                : transport.trim();
        int suffix = 1;
        while (containsListenerName(prefix + " " + suffix)) {
            suffix++;
        }
        return prefix + " " + suffix;
    }

    private boolean containsListenerName(String name) {
        return containsListenerName(name, -1);
    }

    private boolean containsListenerName(String name, int ignoredRow) {
        for (int row = 0; row < listenerModel.getRowCount(); row++) {
            if (row == ignoredRow) {
                continue;
            }
            Object value = listenerModel.getValueAt(row, 0);
            if (name.equalsIgnoreCase(String.valueOf(value).trim())) {
                return true;
            }
        }
        return false;
    }

    private int nextListenerPort(int preferred) {
        int port = preferred;
        while (containsListenerPort(port) && port < 65535) {
            port++;
        }
        return port;
    }

    private boolean containsListenerPort(int port) {
        for (int row = 0; row < listenerModel.getRowCount(); row++) {
            Object value = listenerModel.getValueAt(row, LISTENER_PORT_COLUMN);
            if (value instanceof Number && ((Number) value).intValue() == port) {
                return true;
            }
            if (String.valueOf(value).trim().equals(String.valueOf(port))) {
                return true;
            }
        }
        return false;
    }

    private void refreshListenerDerivedColumns() {
        if (listenerModel == null) {
            return;
        }
        for (int row = 0; row < listenerModel.getRowCount(); row++) {
            String transport = cellTextNoThrow(row, LISTENER_TRANSPORT_COLUMN);
            String bindAddress = cellTextNoThrow(row, LISTENER_BIND_COLUMN);
            int port = portCellNoThrow(row, LISTENER_PORT_COLUMN);
            String endpoint = endpointFor(transport, bindAddress, port);
            if (!endpoint.equals(String.valueOf(listenerModel.getValueAt(row, LISTENER_ENDPOINT_COLUMN)))) {
                listenerModel.setValueAt(endpoint, row, LISTENER_ENDPOINT_COLUMN);
            }

            String currentCertificate = cellTextNoThrow(row, LISTENER_CERTIFICATE_COLUMN);
            String normalized = isSecureTransport(transport)
                    ? normalizeListenerCertificate(currentCertificate)
                    : NO_LISTENER_CERTIFICATE;
            if (!normalized.equals(currentCertificate)) {
                listenerModel.setValueAt(normalized, row, LISTENER_CERTIFICATE_COLUMN);
            }
        }
    }

    private String cellTextNoThrow(int row, int column) {
        Object value = listenerModel.getValueAt(row, column);
        return value == null ? "" : String.valueOf(value).trim();
    }

    private int portCellNoThrow(int row, int column) {
        Object value = listenerModel.getValueAt(row, column);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (RuntimeException ex) {
            return -1;
        }
    }

    private static boolean isSecureTransport(String transport) {
        String normalized = transport == null ? "" : transport.trim().toUpperCase(Locale.ROOT);
        return "TLS".equals(normalized) || "WSS".equals(normalized);
    }

    private String endpointFor(String transport, String bindAddress, int port) {
        String normalizedTransport = transport == null ? "" : transport.trim().toUpperCase(Locale.ROOT);
        String host = bindAddress == null || bindAddress.trim().length() == 0
                ? "127.0.0.1"
                : bindAddress.trim();
        String displayPort = port >= 0 ? String.valueOf(port) : "?";
        if ("TLS".equals(normalizedTransport)) {
            return "mqtts://" + host + ":" + displayPort;
        }
        if ("WS".equals(normalizedTransport) || "WEBSOCKET".equals(normalizedTransport)) {
            return "ws://" + host + ":" + displayPort + "/mqtt";
        }
        if ("WSS".equals(normalizedTransport)) {
            return "wss://" + host + ":" + displayPort + "/mqtt";
        }
        return "mqtt://" + host + ":" + displayPort;
    }

    private String normalizeListenerCertificate(String certificateAlias) {
        String alias = trimOrNull(certificateAlias);
        if (alias == null || NO_LISTENER_CERTIFICATE.equals(alias) || isDefaultCertificateText(alias)) {
            return DEFAULT_LISTENER_CERTIFICATE;
        }
        return alias;
    }

    private boolean isDefaultCertificateText(String value) {
        if (value == null) {
            return true;
        }
        String normalized = value.trim();
        return normalized.length() == 0
                || "Default".equalsIgnoreCase(normalized)
                || "Standard".equalsIgnoreCase(normalized)
                || t("listener.certificate.default").equalsIgnoreCase(normalized);
    }

    private BrokerListenerConfiguration configuredListenerOptions() throws Exception {
        List<PlccomMqttBrokerListenerOptions> listeners = new ArrayList<PlccomMqttBrokerListenerOptions>();
        PlccomMqttBrokerTlsOptions tlsOptions = null;
        for (int row = 0; row < listenerModel.getRowCount(); row++) {
            String name = stringCell(row, LISTENER_NAME_COLUMN);
            String transport = stringCell(row, LISTENER_TRANSPORT_COLUMN).toUpperCase(Locale.ROOT);
            String bindAddress = stringCell(row, LISTENER_BIND_COLUMN);
            int port = portCell(row, LISTENER_PORT_COLUMN);
            if ("TCP".equals(transport)) {
                listeners.add(PlccomMqttBrokerListenerOptions.tcp(name, bindAddress, port));
            } else if ("TLS".equals(transport)) {
                if (tlsOptions == null) {
                    tlsOptions = createTlsOptions();
                }
                listeners.add(PlccomMqttBrokerListenerOptions.tls(name, bindAddress, port, tlsOptions));
            } else if ("WS".equals(transport) || "WEBSOCKET".equals(transport)) {
                listeners.add(PlccomMqttBrokerListenerOptions.webSocket(name, bindAddress, port));
            } else if ("WSS".equals(transport)) {
                if (tlsOptions == null) {
                    tlsOptions = createTlsOptions();
                }
                listeners.add(PlccomMqttBrokerListenerOptions.secureWebSocket(name, bindAddress, port, tlsOptions));
            } else {
                throw new IllegalArgumentException(t("error.listenerTransport") + " " + transport);
            }
        }
        if (listeners.isEmpty()) {
            throw new IllegalArgumentException(t("error.listenerRequired"));
        }
        return new BrokerListenerConfiguration(listeners, tlsOptions);
    }

    private static final class BrokerListenerConfiguration {
        final List<PlccomMqttBrokerListenerOptions> listeners;
        final PlccomMqttBrokerTlsOptions tlsOptions;

        BrokerListenerConfiguration(List<PlccomMqttBrokerListenerOptions> listeners,
                PlccomMqttBrokerTlsOptions tlsOptions) {
            this.listeners = listeners;
            this.tlsOptions = tlsOptions;
        }
    }

    private String stringCell(int row, int column) {
        Object value = listenerModel.getValueAt(row, column);
        String text = value == null ? "" : String.valueOf(value).trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException(t("error.listenerCell") + " " + (row + 1));
        }
        return text;
    }

    private int portCell(int row, int column) {
        Object value = listenerModel.getValueAt(row, column);
        int port;
        if (value instanceof Number) {
            port = ((Number) value).intValue();
        } else {
            try {
                port = Integer.parseInt(String.valueOf(value).trim());
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException(t("error.listenerPort") + " " + (row + 1), ex);
            }
        }
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException(t("error.listenerPort") + " " + (row + 1));
        }
        return port;
    }

    private Component createStatisticsPanel() {
        statisticsModel = new DefaultTableModel(new Object[] { "Name", "Value" }, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(statisticsModel);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new ThemedTableRenderer());

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(actionBar(
                primaryButton("button.refreshStatistics", this::refreshStatistics),
                button("button.resetCounters", this::resetCounters)), BorderLayout.SOUTH);
        return panel;
    }

    private Component createLogPanel() {
        logPanel = new LogPanel();
        JPanel outer = new JPanel(new BorderLayout(0, 0));
        outer.setPreferredSize(new Dimension(300, 210));
        outer.add(logPanel, BorderLayout.CENTER);
        return outer;
    }

    private static PlccomMqttDiagnosticLevel selectedDiagnosticLevel(JComboBox<String> combo) {
        if (combo == null) {
            return PlccomMqttDiagnosticLevel.INFO;
        }
        switch (combo.getSelectedIndex()) {
            case 0:
                return PlccomMqttDiagnosticLevel.DEBUG;
            case 2:
                return PlccomMqttDiagnosticLevel.WARNING;
            case 3:
                return PlccomMqttDiagnosticLevel.ERROR;
            case 4:
                return PlccomMqttDiagnosticLevel.FATAL;
            case 5:
                return PlccomMqttDiagnosticLevel.OFF;
            case 1:
            default:
                return PlccomMqttDiagnosticLevel.INFO;
        }
    }
    private void startBroker() {
        PlccomMqttBroker current = broker;
        if (current != null && current.isRunning()) {
            logInfo("log.already.running");
            updateActionStates();
            return;
        }
        runAsync("startBroker", () -> {
            // Starting again should replace the previous broker cleanly. This
            // avoids accidental port conflicts and keeps every test run tied to
            // the listener settings currently visible in the UI.
            closeBrokerQuietly();

            // License validation deliberately happens at the real product
            // boundary: creating/starting the broker. If the user or serial number
            // is wrong, the exception is shown in the tool log.
            BrokerListenerConfiguration listenerConfiguration = configuredListenerOptions();
            PlccomMqttBrokerBuilder builder = PlccomMqttBrokerFactory
                    .builder(trim(licenseUserField), trim(licenseSerialField))
                    .listeners(listenerConfiguration.listeners)
                    .maxConnections(number(maxConnectionsSpinner))
                    .socketReadTimeoutMillis(number(socketReadTimeoutSpinner))
                    .diagnosticLevel(selectedDiagnosticLevel(diagnosticLevelCombo))
                    .diagnosticThrottleMillis(number(diagnosticThrottleSpinner))
                    .onDiagnostic(this::diagnostic);
            if (listenerConfiguration.tlsOptions != null) {
                // Port-specific certificate assignments replace only the server
                // identity. The default TLS options carry the shared policy,
                // especially client-certificate mode and PKI validation.
                builder.defaultTlsOptions(listenerConfiguration.tlsOptions);
            }
            applyListenerCertificateAssignments(builder);
            if (demoAccessValidatorCheck.isSelected()) {
                // With a validator configured, the Java application owns the
                // access decision. Without one, the broker uses the documented
                // default open policy.
                builder.accessValidator(new DemoAccessValidator());
            } else {
                builder.allowAnonymous();
            }
            PlccomMqttBroker created = builder.start();
            broker = created;
            setStatus("status.running", Status.SUCCESS);
            logSuccess("log.started", created.getBoundPorts());
            logListenerEndpoints();
            refreshStatisticsDirect();
        });
    }

    private void stopBroker() {
        PlccomMqttBroker current = broker;
        if (current == null || !current.isRunning()) {
            logInfo("log.already.stopped");
            updateActionStates();
            return;
        }
        runAsync("stopBroker", () -> {
            closeBrokerQuietly();
            setStatus("status.ready", Status.NEUTRAL);
            logInfo("log.stopped");
        });
    }

    private void refreshStatistics() {
        runAsync("refreshStatistics", () -> {
            MqttBrokerAdminSnapshot snapshot = refreshStatisticsDirect();
            if (snapshot != null) {
                MqttBrokerStatistics statistics = snapshot.getStatistics();
                logSuccess("log.statistics.refreshed", snapshot.getBoundPorts(),
                        statistics.getActiveConnectionCount(), statistics.getApplicationListenerCount(),
                        statistics.getRetainedMessageCount());
            }
        });
    }

    private void resetCounters() {
        runAsync("resetCounters", () -> {
            requireBroker().admin().resetCounters();
            logInfo("log.counters.reset");
            refreshStatisticsDirect();
        });
    }

    private MqttBrokerAdminSnapshot refreshStatisticsDirect() {
        PlccomMqttBroker current = broker;
        if (current == null) {
            SwingUtilities.invokeLater(() -> statisticsModel.setRowCount(0));
            return null;
        }
        MqttBrokerAdminSnapshot snapshot = current.admin().snapshot();
        MqttBrokerStatistics statistics = snapshot.getStatistics();
        SwingUtilities.invokeLater(() -> {
            statisticsModel.setRowCount(0);
            addStatistic("Running", statistics.isRunning());
            addStatistic("Bound ports", snapshot.getBoundPorts());
            addStatistic("Active connections", statistics.getActiveConnectionCount());
            addStatistic("Sessions", statistics.getSessionCount());
            addStatistic("Active sessions", statistics.getActiveSessionCount());
            addStatistic("Subscriptions", statistics.getSubscriptionCount());
            addStatistic("Queued messages", statistics.getQueuedMessageCount());
            addStatistic("Retained messages", statistics.getRetainedMessageCount());
            addStatistic("Application listeners", statistics.getApplicationListenerCount());
            addStatistic("Accepted connections", snapshot.getCounters().getAcceptedConnections());
            addStatistic("Received publishes", snapshot.getCounters().getReceivedPublishes());
            addStatistic("Application publishes", snapshot.getCounters().getApplicationPublishes());
            addStatistic("Retained snapshot entries", snapshot.getRetainedMessages().size());
            addStatistic("Session snapshot entries", snapshot.getSessions().size());
        });
        return snapshot;
    }

    private void addStatistic(String name, Object value) {
        statisticsModel.addRow(new Object[] { name, String.valueOf(value) });
    }

    private PlccomMqttBroker requireBroker() throws PlccomMqttException {
        PlccomMqttBroker current = broker;
        if (current == null || !current.isRunning()) {
            throw new PlccomMqttException(t("error.notRunning"));
        }
        return current;
    }

    private <T extends OperationResult> T requireGood(T result) throws PlccomMqttException {
        if (result == null) {
            throw new PlccomMqttException("MQTT broker operation returned no result");
        }
        if (result.isGood()) {
            return result;
        }
        StringBuilder message = new StringBuilder(result.getQuality().toString());
        String resultMessage = result.getMessage(locale);
        if (resultMessage != null && !resultMessage.trim().isEmpty()) {
            message.append(": ");
            message.append(resultMessage);
        }
        throw new PlccomMqttException(message.toString(), result.getInnerException());
    }

    private void closeBrokerQuietly() {
        PlccomMqttBroker current = broker;
        broker = null;
        if (current != null) {
            try {
                current.close();
            } catch (RuntimeException ex) {
                logWarningText(t("log.stop.cleanup") + " " + shortCause(ex));
            }
        }
    }

    private void shutdown() {
        if (settingsPersistenceEnabled && !settingsSaved) {
            settingsSaved = true;
            saveSettings();
        }
        closeBrokerQuietly();
        executor.shutdownNow();
    }

    private void loadSettings() {
        if (!settingsPersistenceEnabled) {
            return;
        }
        Properties settings = new Properties();
        File file = settingsFile();
        if (!file.isFile()) {
            return;
        }
        try (FileInputStream input = new FileInputStream(file)) {
            settings.load(input);
        } catch (IOException ignored) {
            return;
        }

        String language = settings.getProperty("ui.language");
        if ("en".equalsIgnoreCase(language)) {
            locale = Locale.ENGLISH;
            texts = ResourceBundle.getBundle(MESSAGE_BUNDLE, locale);
        } else if ("de".equalsIgnoreCase(language)) {
            locale = Locale.GERMAN;
            texts = ResourceBundle.getBundle(MESSAGE_BUNDLE, locale);
        }
        String theme = settings.getProperty("ui.theme");
        if ("light".equalsIgnoreCase(theme)) {
            palette = Palette.light();
        } else if ("dark".equalsIgnoreCase(theme)) {
            palette = Palette.dark();
        }
        loadToolSettings(settings);
    }

    private void saveSettings() {
        Properties settings = new Properties();
        settings.setProperty("ui.language", locale.getLanguage());
        settings.setProperty("ui.theme", palette.dark ? "dark" : "light");
        saveToolSettings(settings);

        File file = settingsFile();
        File directory = file.getParentFile();
        if (directory != null && !directory.isDirectory()) {
            directory.mkdirs();
        }
        try (FileOutputStream output = new FileOutputStream(file)) {
            settings.store(output,
                    "PLCcom MQTT Broker Tool user settings. Sensitive values use AES/GCM encryption.");
        } catch (IOException ignored) {
            // Settings persistence is a convenience feature. A failure here must
            // never prevent the user application from closing cleanly.
        }
    }

    private void loadToolSettings(Properties settings) {
        setProtectedText(settings, "license.user", licenseUserField);
        setProtectedText(settings, "license.serial", licenseSerialField);
        loadListenerSettings(settings);
        setProtectedText(settings, "listener.certificatePassword", certificatePasswordField);
        pkiStoreField.setText(DEFAULT_PKI_FOLDER);
        pkiOwnAliasField.setText(DEFAULT_OWN_CERTIFICATE_ALIAS);
        setCombo(settings, "listener.clientCertificateMode", clientCertificateModeCombo);
        setCheck(settings, "access.demoValidator", demoAccessValidatorCheck);
        setCheck(settings, "access.requireUsername", requireUsernameCheck);
        setCheck(settings, "access.requireClientCertificate", requireClientCertificateCheck);
        setText(settings, "access.blockedTopicPrefix", blockedTopicPrefixField);
        setSpinner(settings, "runtime.maxConnections", maxConnectionsSpinner);
        setSpinner(settings, "runtime.socketReadTimeout", socketReadTimeoutSpinner);
        setCombo(settings, "runtime.diagnosticLevel", diagnosticLevelCombo);
        setSpinner(settings, "runtime.diagnosticThrottle", diagnosticThrottleSpinner);
    }

    private void saveToolSettings(Properties settings) {
        putProtectedText(settings, "license.user", licenseUserField);
        putProtectedText(settings, "license.serial", licenseSerialField);
        saveListenerSettings(settings);
        putProtectedText(settings, "listener.certificatePassword", certificatePasswordField);
        putCombo(settings, "listener.clientCertificateMode", clientCertificateModeCombo);
        putCheck(settings, "access.demoValidator", demoAccessValidatorCheck);
        putCheck(settings, "access.requireUsername", requireUsernameCheck);
        putCheck(settings, "access.requireClientCertificate", requireClientCertificateCheck);
        putText(settings, "access.blockedTopicPrefix", blockedTopicPrefixField);
        putSpinner(settings, "runtime.maxConnections", maxConnectionsSpinner);
        putSpinner(settings, "runtime.socketReadTimeout", socketReadTimeoutSpinner);
        putCombo(settings, "runtime.diagnosticLevel", diagnosticLevelCombo);
        putSpinner(settings, "runtime.diagnosticThrottle", diagnosticThrottleSpinner);
    }


    private void loadListenerSettings(Properties settings) {
        listenerModel.setRowCount(0);
        int count = integerSetting(settings, "listener.count", -1);
        if (count > 0) {
            for (int index = 0; index < count; index++) {
                String prefix = "listener." + index + ".";
                addListenerRow(settings.getProperty(prefix + "name", defaultListenerName("TCP")),
                        settings.getProperty(prefix + "transport", "TCP"),
                        settings.getProperty(prefix + "bind", "127.0.0.1"),
                        integerSetting(settings, prefix + "port", 1883),
                        settings.getProperty(prefix + "certificate", DEFAULT_LISTENER_CERTIFICATE));
            }
            return;
        }
        String bind = settings.getProperty("listener.bindAddress", "127.0.0.1");
        addListenerRow("Main TCP", "TCP", bind, integerSetting(settings, "listener.tcpPort", 1883));
        if (booleanSetting(settings, "listener.secondTcp", false)) {
            addListenerRow("Second TCP", "TCP", bind, integerSetting(settings, "listener.secondTcpPort", 1884));
        }
        if (booleanSetting(settings, "listener.webSocket", false)) {
            addListenerRow("WebSocket", "WS", bind, integerSetting(settings, "listener.webSocketPort", 8080));
        }
        if (booleanSetting(settings, "listener.tls", false)) {
            addListenerRow("TLS", "TLS", bind, integerSetting(settings, "listener.tlsPort", 8883));
        }
        if (booleanSetting(settings, "listener.secureWebSocket", false)) {
            addListenerRow("WSS", "WSS", bind, integerSetting(settings, "listener.secureWebSocketPort", 8443));
        }
    }

    private void saveListenerSettings(Properties settings) {
        settings.setProperty("listener.count", String.valueOf(listenerModel.getRowCount()));
        for (int row = 0; row < listenerModel.getRowCount(); row++) {
            String prefix = "listener." + row + ".";
            settings.setProperty(prefix + "name", String.valueOf(listenerModel.getValueAt(row, 0)));
            settings.setProperty(prefix + "transport", String.valueOf(listenerModel.getValueAt(row, 1)));
            settings.setProperty(prefix + "bind", String.valueOf(listenerModel.getValueAt(row, 2)));
            settings.setProperty(prefix + "port", String.valueOf(listenerModel.getValueAt(row, 3)));
            settings.setProperty(prefix + "certificate",
                    normalizeListenerCertificate(cellTextNoThrow(row, LISTENER_CERTIFICATE_COLUMN)));
        }
    }

    private int integerSetting(Properties settings, String key, int fallback) {
        try {
            return Integer.parseInt(settings.getProperty(key, String.valueOf(fallback)).trim());
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    private boolean booleanSetting(Properties settings, String key, boolean fallback) {
        String value = settings.getProperty(key);
        return value == null ? fallback : Boolean.parseBoolean(value);
    }

    private void runAsync(String operation, ThrowingRunnable task) {
        if (!operationActive.compareAndSet(false, true)) {
            logWarning("log.operation.busy");
            return;
        }
        setStatus("status.busy", Status.WARNING);
        updateActionStates();
        executor.submit(() -> {
            try {
                task.run();
                if (broker == null || !broker.isRunning()) {
                    setStatus("status.ready", Status.NEUTRAL);
                } else {
                    setStatus("status.running", Status.SUCCESS);
                }
            } catch (Throwable ex) {
                // All button actions end here on failure, including invalid
                // license data, occupied ports, invalid certificates, access
                // validator failures and MQTT runtime errors. The tool stays
                // usable and the operator gets the cause in the log.
                setStatus("status.error", Status.ERROR);
                logError("log.operation.failed", operation, shortCause(ex));
                logErrorText(t("log.failure.details") + " " + detailedCause(ex));
            } finally {
                operationActive.set(false);
                updateActionStates();
            }
        });
    }

    private void applyListenerCertificateAssignments(PlccomMqttBrokerBuilder builder) throws Exception {
        Map<Integer, String> aliasesByPort = new LinkedHashMap<Integer, String>();
        for (int row = 0; row < listenerModel.getRowCount(); row++) {
            String transport = stringCell(row, LISTENER_TRANSPORT_COLUMN);
            if (!isSecureTransport(transport)) {
                continue;
            }
            int port = portCell(row, LISTENER_PORT_COLUMN);
            String alias = normalizeListenerCertificate(cellTextNoThrow(row, LISTENER_CERTIFICATE_COLUMN));
            if (alias.length() == 0) {
                continue;
            }
            if (port == 0) {
                throw new IllegalArgumentException(t("error.listenerCertificatePortZero") + " " + (row + 1));
            }
            String previous = aliasesByPort.put(Integer.valueOf(port), alias);
            if (previous != null && !previous.equals(alias)) {
                throw new IllegalArgumentException(t("error.listenerCertificatePortConflict") + " " + port);
            }
        }
        if (aliasesByPort.isEmpty()) {
            return;
        }

        char[] password = certificatePasswordField.getPassword();
        try {
            PlccomMqttPkiStore pkiStore = createPkiStore();
            for (Map.Entry<Integer, String> entry : aliasesByPort.entrySet()) {
                builder.assignTlsCertificateToPort(entry.getKey().intValue(),
                        createOwnCertificate(pkiStore, entry.getValue(), password));
            }
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private PlccomMqttBrokerTlsOptions createTlsOptions() throws Exception {
        String pkiOwnAlias = trimOrNull(pkiOwnAliasField);
        if (pkiOwnAlias == null) {
            throw new IllegalStateException(t("error.ownAliasRequired"));
        }
        char[] password = certificatePasswordField.getPassword();
        try {
            // The broker test app follows the PLCcom PKI workflow from the
            // library: load the existing own identity or create it on demand.
            PlccomMqttPkiStore pkiStore = createPkiStore();
            PlccomMqttOwnCertificate ownCertificate = createOwnCertificate(pkiStore, pkiOwnAlias, password);
            KeyStore keyStore = ownCertificate.loadOrCreate();
            return PlccomMqttBrokerTlsOptions.builder()
                    .keyStore(keyStore, password)
                    .enabledProtocols("TLSv1.3", "TLSv1.2")
                    .clientCertificateMode(clientCertificateMode())
                    .pkiOptions(PlccomMqttPkiOptions.builder()
                            .pkiStore(pkiStore)
                            .build())
                    .build();
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private PlccomMqttOwnCertificate createOwnCertificate(PlccomMqttPkiStore pkiStore,
            String alias, char[] password) throws Exception {
        return new PlccomMqttOwnCertificate(
                pkiStore, alias, password, 3650, "Indi.An GmbH",
                Arrays.asList("localhost"),
                Arrays.asList(InetAddress.getByName("127.0.0.1"),
                        InetAddress.getByName("::1")));
    }

    private void logListenerEndpoints() {
        for (int row = 0; row < listenerModel.getRowCount(); row++) {
            String transport = cellTextNoThrow(row, LISTENER_TRANSPORT_COLUMN);
            String certificate = isSecureTransport(transport)
                    ? normalizeListenerCertificate(cellTextNoThrow(row, LISTENER_CERTIFICATE_COLUMN))
                    : NO_LISTENER_CERTIFICATE;
            String certificateText = certificate.length() == 0
                    ? t("listener.certificate.default")
                    : certificate;
            logInfo("log.listener.endpoint",
                    cellTextNoThrow(row, LISTENER_NAME_COLUMN),
                    cellTextNoThrow(row, LISTENER_ENDPOINT_COLUMN),
                    certificateText);
        }
    }


    private void openHelp() {
        try {
            File index = prepareHelpWebsite();
            openInBrowser(index);
            logInfo("log.help.opened", index.getAbsolutePath());
        } catch (Exception ex) {
            logError("log.help.failed", shortCause(ex));
        }
    }

    private File prepareHelpWebsite() throws IOException {
        String language = Locale.GERMAN.getLanguage().equals(locale.getLanguage()) ? "de" : "en";
        File targetDirectory = new File(new File(new File(settingsDirectory(), "Help"), "Broker"), version());
        targetDirectory = new File(targetDirectory, language);
        if (!targetDirectory.exists() && !targetDirectory.mkdirs()) {
            throw new IOException("Cannot create help directory: " + targetDirectory.getAbsolutePath());
        }
        copyHelpResource("/com/plccom/mqtt/tools/broker/help/common/styles.css",
                new File(targetDirectory, "styles.css"));
        File index = new File(targetDirectory, "index.html");
        copyHelpResource("/com/plccom/mqtt/tools/broker/help/" + language + "/index.html", index);
        return index;
    }

    private void copyHelpResource(String resourceName, File target) throws IOException {
        try (InputStream input = PlccomMqttBrokerToolApp.class.getResourceAsStream(resourceName)) {
            if (input == null) {
                throw new IOException("Missing help resource: " + resourceName);
            }
            try (FileOutputStream output = new FileOutputStream(target)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) >= 0) {
                    output.write(buffer, 0, read);
                }
            }
        }
    }

    private void openInBrowser(File file) throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(file.toURI());
                return;
            }
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                desktop.open(file);
                return;
            }
        }
        openInBrowserFallback(file);
    }

    private static void openInBrowserFallback(File file) throws IOException {
        String uri = file.toURI().toString();
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (osName.contains("win")) {
            Runtime.getRuntime().exec(new String[] { "rundll32", "url.dll,FileProtocolHandler", uri });
        } else if (osName.contains("mac")) {
            Runtime.getRuntime().exec(new String[] { "open", uri });
        } else {
            Runtime.getRuntime().exec(new String[] { "xdg-open", uri });
        }
    }
    private void showCertificateManager() {
        char[] password = certificatePasswordField == null
                ? new char[0]
                : certificatePasswordField.getPassword();
        try {
            PlccomMqttCertificateManagerDialog.show(frame, texts, locale,
                    createPkiStore(), DEFAULT_OWN_CERTIFICATE_ALIAS, password,
                    component -> theme(component));
        } catch (Throwable ex) {
            logError("log.operation.failed", "certificates", shortCause(ex));
            logErrorText(t("log.failure.details") + " " + detailedCause(ex));
        } finally {
            Arrays.fill(password, '\0');
            refreshListenerCertificateEditor();
        }
    }

    private void ensureConfiguredPkiDirectories() {
        try {
            createPkiStore();
        } catch (IOException | RuntimeException ex) {
            logWarning("log.pki.ensure.failed", shortCause(ex));
        }
    }

    private PlccomMqttPkiStore createPkiStore() throws IOException {
        final PlccomMqttPkiStore[] storeRef = new PlccomMqttPkiStore[1];
        PlccomMqttPkiStore pkiStore = new PlccomMqttPkiStore(Paths.get(DEFAULT_PKI_FOLDER),
                event -> handlePkiStoreEvent(storeRef[0], event));
        storeRef[0] = pkiStore;
        pkiStore.ensureDirectories();
        return pkiStore;
    }

    private void handlePkiStoreEvent(PlccomMqttPkiStore pkiStore,
            PlccomMqttPkiStoreEvent event) {
        if (pkiStore == null || event == null
                || event.getStoreKind() != PlccomMqttPkiStoreKind.REJECTED) {
            return;
        }
        String fingerprint = certificateFingerprint(event.getCertificate());
        if (event.isNewFile()) {
            // A newly written rejected certificate means the user intentionally
            // removed or reset the previous store entry. Treat the next connect
            // like a fresh onboarding attempt, even within the same tool run.
            promptedRejectedCertificates.add(fingerprint);
        } else if (!promptedRejectedCertificates.add(fingerprint)) {
            return;
        }
        SwingUtilities.invokeLater(() -> showRejectedCertificateDialog(pkiStore, event, fingerprint));
    }

    private void showRejectedCertificateDialog(PlccomMqttPkiStore pkiStore,
            PlccomMqttPkiStoreEvent event, String fingerprint) {
        X509Certificate certificate = event.getCertificate();
        logWarning("log.pki.rejected", fingerprint, event.getPath());

        JTextArea details = new JTextArea(format("dialog.certificate.message",
                certificate.getSubjectX500Principal().getName(),
                certificate.getIssuerX500Principal().getName(),
                certificate.getSerialNumber().toString(16).toUpperCase(Locale.ROOT),
                certificate.getNotBefore(),
                certificate.getNotAfter(),
                fingerprint,
                event.getPath()));
        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        details.setColumns(72);
        details.setRows(13);
        theme(details);

        Object[] options = {
                t("dialog.certificate.trust"),
                t("dialog.certificate.keepRejected")
        };
        int choice = JOptionPane.showOptionDialog(frame, new JScrollPane(details),
                t("dialog.certificate.title"), JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE, null, options, options[0]);
        if (choice != JOptionPane.YES_OPTION) {
            logWarning("log.pki.rejectedKept", fingerprint, event.getPath());
            return;
        }
        try {
            logSuccess("log.pki.trusted",
                    fingerprint,
                    pkiStore.moveRejectedToTrusted(certificate));
            JOptionPane.showMessageDialog(frame,
                    t("dialog.certificate.trustedMessage"),
                    t("dialog.certificate.title"),
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            logError("log.pki.trust.failed", shortCause(ex));
            JOptionPane.showMessageDialog(frame,
                    format("dialog.certificate.trustFailed", shortCause(ex)),
                    t("dialog.certificate.title"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String certificateFingerprint(X509Certificate certificate) {
        try {
            return PlccomMqttPkiStore.sha1Thumbprint(certificate);
        } catch (Exception ex) {
            return "unknown";
        }
    }

    private PlccomMqttBrokerClientCertificateMode clientCertificateMode() {
        if (requireClientCertificateCheck != null && requireClientCertificateCheck.isSelected()) {
            // A CONNECT validator can only authenticate a TLS client certificate
            // if the TLS listener actually requests one during the handshake.
            // The reference tool therefore makes the TLS requirement explicit
            // whenever its demo validator requires a user certificate.
            return PlccomMqttBrokerClientCertificateMode.REQUIRED;
        }
        int index = clientCertificateModeCombo.getSelectedIndex();
        if (index == 1) {
            return PlccomMqttBrokerClientCertificateMode.OPTIONAL;
        }
        if (index == 2) {
            return PlccomMqttBrokerClientCertificateMode.REQUIRED;
        }
        return PlccomMqttBrokerClientCertificateMode.NONE;
    }

    private void diagnostic(PlccomMqttDiagnosticEvent event) {
        logPanel.append(LogLevel.WARNING, formatDiagnostic(event));
    }

    private String formatDiagnostic(PlccomMqttDiagnosticEvent event) {
        StringBuilder builder = new StringBuilder();
        // Diagnostics are structured log events. Severity/category/code are stable
        // enough for operators to filter, while optional fields explain which
        // listener, topic, client or remote endpoint triggered the event.
        builder.append("diagnostic ")
                .append(event.getSeverity())
                .append(" ")
                .append(event.getCategory())
                .append(" ")
                .append(event.getCode())
                .append(": ")
                .append(event.getMessage());
        if (event.getOperation() != null) {
            builder.append(" operation=").append(event.getOperation());
        }
        if (event.getTopicName() != null) {
            builder.append(" topic=").append(event.getTopicName());
        }
        if (event.getClientId() != null) {
            builder.append(" clientId=").append(event.getClientId());
        }
        if (event.getRemoteAddress() != null) {
            builder.append(" remote=").append(event.getRemoteAddress());
        }
        if (event.getSuppressedCount() > 0) {
            builder.append(" suppressed=").append(event.getSuppressedCount());
        }
        if (event.getCause() != null) {
            builder.append(" cause=").append(shortCause(event.getCause()));
        }
        return builder.toString();
    }

    private void toggleTheme() {
        palette = palette.dark ? Palette.light() : Palette.dark();
        applyTheme();
    }

    private void setLightTheme() {
        palette = Palette.light();
        applyTheme();
    }

    private void toggleLanguage() {
        setLanguage(Locale.GERMAN.getLanguage().equals(locale.getLanguage()) ? Locale.ENGLISH : Locale.GERMAN);
    }

    private void setLanguage(Locale value) {
        locale = value;
        texts = ResourceBundle.getBundle(MESSAGE_BUNDLE, locale);
        translate();
    }

    private void exitApplication() {
        frame.dispose();
    }

    private void translate() {
        if (frame == null) {
            return;
        }
        frame.setTitle(t("app.title") + " " + version());
        titleLabel.setText(t("app.title"));
        subtitleLabel.setText(t("app.subtitle"));
        for (Map.Entry<JLabel, String> entry : labelKeys.entrySet()) {
            entry.getKey().setText(t(entry.getValue()));
        }
        for (Map.Entry<AbstractButton, String> entry : buttonKeys.entrySet()) {
            entry.getKey().setText(t(entry.getValue()));
        }
        for (Map.Entry<JComponent, String> entry : tooltipKeys.entrySet()) {
            entry.getKey().setToolTipText(t(entry.getValue()));
        }
        refreshTabTitles();
        updateListenerTableHeaders();
        refreshListenerCertificateEditor();
        languageButton.setText(Locale.GERMAN.getLanguage().equals(locale.getLanguage()) ? "EN" : "DE");
        setStatusTextOnly(broker != null && broker.isRunning() ? "status.running" : "status.ready");
        if (logPanel != null) {
            logPanel.translate();
        }
        updateActionStates();
        frame.revalidate();
    }


    private void updateListenerTableHeaders() {
        if (listenerTable == null) {
            return;
        }
        setListenerHeader(LISTENER_NAME_COLUMN, "listener.column.name");
        setListenerHeader(LISTENER_TRANSPORT_COLUMN, "listener.column.transport");
        setListenerHeader(LISTENER_BIND_COLUMN, "listener.column.bind");
        setListenerHeader(LISTENER_PORT_COLUMN, "listener.column.port");
        setListenerHeader(LISTENER_ENDPOINT_COLUMN, "listener.column.endpoint");
        setListenerHeader(LISTENER_CERTIFICATE_COLUMN, "listener.column.certificate");
        listenerTable.getTableHeader().repaint();
    }

    private void setListenerHeader(int modelColumn, String key) {
        TableColumn column = listenerColumn(modelColumn);
        if (column != null) {
            column.setHeaderValue(t(key));
        }
    }

    private void applyTheme() {
        if (root == null) {
            return;
        }
        theme(root);
        if (logPanel != null) {
            logPanel.translate();
            logPanel.recolor();
        }
        themeButton.setText(palette.dark ? t("button.theme.light") : t("button.theme.dark"));
        refreshTabTitles();
        styleStatusBadge();
        updateActionStates();
        frame.revalidate();
        frame.repaint();
    }

    private void updateActionStates() {
        if (SwingUtilities.isEventDispatchThread()) {
            applyActionStates();
        } else {
            SwingUtilities.invokeLater(this::applyActionStates);
        }
    }

    private void applyActionStates() {
        boolean busy = operationActive.get();
        boolean running = broker != null && broker.isRunning();
        if (startButton != null) {
            startButton.setEnabled(!busy && !running);
            theme(startButton);
        }
        if (stopButton != null) {
            stopButton.setEnabled(!busy && running);
            theme(stopButton);
        }
        if (helpButton != null) {
            helpButton.setEnabled(true);
            theme(helpButton);
        }
        if (exitButton != null) {
            exitButton.setEnabled(true);
            theme(exitButton);
        }
    }

    private void theme(Component component) {
        String name = component.getName();
        if ("logoPanel".equals(component.getName())) {
            // The logo card paints its own static white surface so the navy
            // wordmark stays legible in both themes; nothing to recolour here.
            return;
        }
        if (component instanceof JComponent) {
            ((JComponent) component).setOpaque(true);
        }
        component.setBackground(palette.windowBg);
        component.setForeground(palette.text);
        if ("header".equals(name)) {
            ((JComponent) component).setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, palette.border),
                    new EmptyBorder(10, 16, 10, 16)));
        } else if ("footer".equals(name)) {
            ((JComponent) component).setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, palette.border),
                    new EmptyBorder(8, 16, 10, 16)));
        } else if ("actionBar".equals(name)) {
            ((JComponent) component).setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, palette.border),
                    new EmptyBorder(10, 0, 0, 0)));
        } else if ("subtitle".equals(name)) {
            component.setForeground(palette.muted);
        }
        if (component instanceof JTextComponent) {
            JTextComponent text = (JTextComponent) component;
            text.setBackground(palette.surfaceAlt);
            text.setForeground(palette.text);
            text.setCaretColor(palette.text);
            text.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(palette.border),
                    new EmptyBorder(4, 6, 4, 6)));
        } else if (component instanceof JButton) {
            themeButton((JButton) component);
        } else if (component instanceof JTabbedPane) {
            JTabbedPane tabbedPane = (JTabbedPane) component;
            tabbedPane.setBackground(palette.surface);
            tabbedPane.setForeground(palette.text);
            tabbedPane.setBorder(BorderFactory.createLineBorder(palette.border));
        } else if (component instanceof JCheckBox) {
            component.setBackground(palette.windowBg);
        } else if (component instanceof JComboBox<?>) {
            JComboBox<?> combo = (JComboBox<?>) component;
            combo.setBackground(palette.surfaceAlt);
            combo.setForeground(palette.text);
            if (!Boolean.TRUE.equals(((JComponent) combo).getClientProperty("plccom.listenerCertificateCombo"))) {
                combo.setRenderer(new ThemedListRenderer());
            }
        } else if (component instanceof JSpinner) {
            component.setBackground(palette.surfaceAlt);
            component.setForeground(palette.text);
        } else if (component instanceof JTable) {
            JTable table = (JTable) component;
            table.setBackground(palette.surface);
            table.setForeground(palette.text);
            table.setGridColor(palette.gridLine);
            table.setSelectionBackground(palette.accentPressed);
            table.setSelectionForeground(Color.WHITE);
            table.getTableHeader().setBackground(palette.gridHeader);
            table.getTableHeader().setForeground(palette.text);
        } else if (component instanceof JScrollPane) {
            ((JScrollPane) component).getViewport().setBackground(palette.surface);
        }
        if (component instanceof java.awt.Container) {
            for (Component child : ((java.awt.Container) component).getComponents()) {
                theme(child);
            }
        }
    }

    private void themeButton(JButton button) {
        boolean iconOnly = button.getText() == null || button.getText().isEmpty();
        EmptyBorder padding = iconOnly
                ? new EmptyBorder(4, 4, 4, 4)
                : new EmptyBorder(6, 14, 6, 14);
        Color background;
        Color foreground;
        Color line;
        if (!button.isEnabled()) {
            background = palette.surfaceAlt;
            foreground = palette.muted;
            line = palette.border;
        } else if (primaryButtons.contains(button)) {
            background = palette.accent;
            foreground = Color.WHITE;
            line = palette.accentPressed;
        } else {
            background = palette.secondaryBg;
            foreground = palette.text;
            line = palette.secondaryBorder;
        }
        button.setBackground(background);
        button.setForeground(foreground);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(line), padding));
        button.setCursor(button.isEnabled()
                ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                : Cursor.getDefaultCursor());
    }

    private JPanel formPanel() {
        JPanel panel = cardPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));
        return panel;
    }

    private JPanel cardPanel(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(palette.border),
                new EmptyBorder(8, 8, 8, 8)));
        return panel;
    }

    private GridBagConstraints formGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String key, Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(rowLabel(key), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = shouldStretch(field) ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
        registerTooltipTree(field, key + ".tip");
        panel.add(field, gbc);
    }

    private void addCompactRow(JPanel panel, GridBagConstraints gbc, int row, String key,
            Component field, int labelWidth) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(rowLabel(key, labelWidth), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = shouldStretch(field) ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
        registerTooltipTree(field, key + ".tip");
        panel.add(field, gbc);
    }

    private void addWide(JPanel panel, GridBagConstraints gbc, int row, Component component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, gbc);
        gbc.gridwidth = 1;
    }

    private void addFormBottomFiller(JPanel panel, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(javax.swing.Box.createVerticalGlue(), gbc);
        gbc.gridwidth = 1;
        gbc.weighty = 0;
    }

    private JPanel listenerInput(JComponent field, Component... trailingComponents) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setBorder(BorderFactory.createEmptyBorder());
        panel.add(trailingComponents.length == 0 ? fixedListenerField(field) : field);
        for (Component trailingComponent : trailingComponents) {
            panel.add(javax.swing.Box.createHorizontalStrut(8));
            panel.add(trailingComponent);
        }
        return panel;
    }

    private JPanel compactInput(JComponent field) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setBorder(BorderFactory.createEmptyBorder());
        // Wrap in a fixed-width holder (BorderLayout CENTER forces the child to the
        // holder size) so every security field - text field, combo and spinner -
        // has the exact same width and aligned right edges, regardless of the
        // component type's own preferred size.
        panel.add(fixedSecurityField(field));
        return panel;
    }

    private JComponent fixedSecurityField(JComponent field) {
        JPanel holder = new JPanel(new BorderLayout());
        holder.setBorder(BorderFactory.createEmptyBorder());
        holder.add(field, BorderLayout.CENTER);
        setFixedSize(holder, SECURITY_FIELD_WIDTH, CONTROL_HEIGHT);
        return holder;
    }

    private JComponent fixedListenerField(JComponent field) {
        JPanel holder = new JPanel(new BorderLayout());
        holder.setBorder(BorderFactory.createEmptyBorder());
        holder.add(field, BorderLayout.CENTER);
        setFixedSize(holder, LISTENER_FIELD_WIDTH, CONTROL_HEIGHT);
        return holder;
    }


    private void applySecurityFieldWidth(JComponent... components) {
        for (JComponent component : components) {
            setFixedSize(component, SECURITY_FIELD_WIDTH, CONTROL_HEIGHT);
        }
    }

    private void applyListenerFieldWidth(JComponent... components) {
        for (JComponent component : components) {
            setFixedSize(component, LISTENER_FIELD_WIDTH, CONTROL_HEIGHT);
        }
    }


    private JLabel label(String key) {
        JLabel label = new JLabel(t(key));
        labelKeys.put(label, key);
        return label;
    }

    private JButton primaryButton(String key, Runnable action) {
        JButton button = button(key, iconFor(key), action);
        primaryButtons.add(button);
        return button;
    }

    private JButton button(String key, Runnable action) {
        return button(key, iconFor(key), action);
    }

    private JButton button(String key, ToolIcon icon, Runnable action) {
        ToolButton button = new ToolButton();
        button.setText(t(key));
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setBorderPainted(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setOpaque(true);
        buttonKeys.put(button, key);
        if (icon != null) {
            button.setIcon(new ToolButtonIcon(icon));
            button.setIconTextGap(8);
            button.setHorizontalTextPosition(SwingConstants.RIGHT);
        }
        registerTooltip(button, key + ".tip");
        button.addActionListener((ActionEvent e) -> action.run());
        return button;
    }

    private JButton iconButton(ToolIcon icon, String tooltipKey, Runnable action) {
        JButton button = new JButton(new ToolButtonIcon(icon));
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setBorderPainted(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        setFixedSize(button, ICON_BUTTON_SIZE, ICON_BUTTON_SIZE);
        registerTooltip(button, tooltipKey);
        registerTooltip(button, tooltipKey + ".tip");
        button.addActionListener((ActionEvent e) -> action.run());
        return button;
    }

    private JPanel actionBar(JComponent... buttons) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setName("actionBar");
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, palette.border),
                new EmptyBorder(10, 0, 0, 0)));
        for (JComponent button : buttons) {
            bar.add(button);
        }
        return bar;
    }

    private ToolIcon iconFor(String key) {
        if ("button.theme".equals(key)) {
            return ToolIcon.THEME;
        }
        if ("button.language".equals(key)) {
            return ToolIcon.LANGUAGE;
        }
        if ("button.certificates".equals(key)) {
            return ToolIcon.CERTIFICATE;
        }
        if ("button.exit".equals(key)) {
            return ToolIcon.EXIT;
        }
        if ("button.editListener".equals(key)) {
            return ToolIcon.EDIT;
        }
        if ("button.connect".equals(key) || "button.start".equals(key)) {
            return ToolIcon.START;
        }
        if ("button.disconnect".equals(key) || "button.stop".equals(key)) {
            return ToolIcon.STOP;
        }
        if (key != null && key.indexOf("publish") >= 0) {
            return ToolIcon.PUBLISH;
        }
        if (key != null && (key.indexOf("subscribe") >= 0 || key.indexOf("listen") >= 0)) {
            return ToolIcon.SUBSCRIBE;
        }
        if (key != null && key.indexOf("request") >= 0) {
            return ToolIcon.REQUEST;
        }
        if (key != null && (key.indexOf("copy") >= 0 || key.indexOf("save") >= 0)) {
            return ToolIcon.FILE;
        }
        if (key != null && (key.indexOf("clear") >= 0 || key.indexOf("delete") >= 0 || key.indexOf("remove") >= 0)) {
            return ToolIcon.REMOVE;
        }
        if (key != null && key.indexOf("refresh") >= 0) {
            return ToolIcon.REFRESH;
        }
        if (key != null && key.indexOf("reset") >= 0) {
            return ToolIcon.RESET;
        }
        if (key != null && key.indexOf("add") >= 0) {
            return ToolIcon.ADD;
        }
        return null;
    }

    private JCheckBox checkBox(String key, boolean selected) {
        JCheckBox box = new JCheckBox(t(key), selected);
        buttonKeys.put(box, key);
        registerTooltip(box, key + ".tip");
        return box;
    }

    private void registerTooltip(JComponent component, String key) {
        if (texts.containsKey(key)) {
            tooltipKeys.put(component, key);
            component.setToolTipText(t(key));
        }
    }

    private void registerTooltipTree(Component component, String key) {
        if (!texts.containsKey(key)) {
            return;
        }
        if (component instanceof JComponent && !tooltipKeys.containsKey(component)) {
            registerTooltip((JComponent) component, key);
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                registerTooltipTree(child, key);
            }
        }
    }

    private JTextField textField(int columns, String text) {
        JTextField field = new JTextField(text, columns);
        normalizeControlHeight(field);
        return field;
    }

    private JSpinner spinner(int value, int min, int max, int step) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, step));
        configureSpinner(spinner, true);
        return spinner;
    }

    private JSpinner plainSpinner(int value, int min, int max, int step) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, step));
        configureSpinner(spinner, false);
        return spinner;
    }

    private void configureSpinner(JSpinner spinner, boolean groupingUsed) {
        setFixedSize(spinner, NUMERIC_CONTROL_WIDTH, CONTROL_HEIGHT);
        if (!groupingUsed) {
            spinner.setEditor(new JSpinner.NumberEditor(spinner, "0"));
        }
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JSpinner.DefaultEditor defaultEditor = (JSpinner.DefaultEditor) editor;
            defaultEditor.getTextField().setHorizontalAlignment(SwingConstants.RIGHT);
            if (defaultEditor instanceof JSpinner.NumberEditor) {
                DecimalFormat format = ((JSpinner.NumberEditor) defaultEditor).getFormat();
                format.setGroupingUsed(groupingUsed);
            }
        }
    }

    private JComboBox<String> combo(String... values) {
        JComboBox<String> combo = new JComboBox<String>(new DefaultComboBoxModel<String>(values));
        Dimension preferred = combo.getPreferredSize();
        setFixedSize(combo, Math.max(preferred.width, COMBO_CONTROL_WIDTH), CONTROL_HEIGHT);
        return combo;
    }

    private JPanel inlinePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setBorder(BorderFactory.createEmptyBorder());
        return panel;
    }

    private JComponent sectionTitle(String key) {
        JPanel panel = new JPanel(new BorderLayout(4, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
        JLabel title = label(key);
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        panel.add(title, BorderLayout.CENTER);
        String tooltipKey = key + ".tip";
        if (texts.containsKey(tooltipKey)) {
            panel.add(helpIcon(tooltipKey), BorderLayout.EAST);
        }
        return panel;
    }

    private JComponent rowLabel(String key) {
        JLabel rowLabel = label(key);
        String tooltipKey = key + ".tip";
        if (!texts.containsKey(tooltipKey)) {
            rowLabel.setPreferredSize(new Dimension(FORM_LABEL_WIDTH, CONTROL_HEIGHT));
            rowLabel.setMinimumSize(new Dimension(FORM_LABEL_WIDTH, CONTROL_HEIGHT));
            return rowLabel;
        }

        JPanel panel = new JPanel(new BorderLayout(6, 0));
        panel.setBorder(BorderFactory.createEmptyBorder());
        panel.setPreferredSize(new Dimension(FORM_LABEL_WIDTH, CONTROL_HEIGHT));
        panel.setMinimumSize(new Dimension(FORM_LABEL_WIDTH, CONTROL_HEIGHT));
        panel.add(rowLabel, BorderLayout.CENTER);
        panel.add(helpIcon(tooltipKey), BorderLayout.EAST);
        registerTooltip(rowLabel, tooltipKey);
        registerTooltip(panel, tooltipKey);
        return panel;
    }

    private JComponent rowLabel(String key, int width) {
        JComponent component = rowLabel(key);
        component.setPreferredSize(new Dimension(width, CONTROL_HEIGHT));
        component.setMinimumSize(new Dimension(width, CONTROL_HEIGHT));
        return component;
    }

    private JComponent helpIcon(String tooltipKey) {
        HelpBubble help = new HelpBubble();
        tooltipKeys.put(help, tooltipKey);
        help.setToolTipText(t(tooltipKey));
        return help;
    }

    private boolean shouldStretch(Component component) {
        return component instanceof JTextComponent
                || component instanceof JScrollPane
                || component instanceof JPanel;
    }

    private void normalizeControlHeight(JComponent component) {
        Dimension preferred = component.getPreferredSize();
        component.setPreferredSize(new Dimension(preferred.width, CONTROL_HEIGHT));
        component.setMinimumSize(new Dimension(Math.min(preferred.width, 80), CONTROL_HEIGHT));
    }

    private void setFixedSize(JComponent component, int width, int height) {
        Dimension size = new Dimension(width, height);
        component.setPreferredSize(size);
        component.setMinimumSize(size);
        component.setMaximumSize(size);
    }

    private Icon loadLogo() {
        try {
            BufferedImage source = ImageIO.read(
                    PlccomMqttBrokerToolApp.class.getResource("/assets/PLCcom_720.png"));
            return new ImageIcon(scaledLogo(source, LOGO_HEIGHT));
        } catch (Exception ex) {
            return new ImageIcon();
        }
    }

    /**
     * Scales the high-resolution logo to the requested display height while
     * preserving its aspect ratio. A second variant at twice the size is bundled
     * so the wordmark also stays crisp on HiDPI (Retina) displays instead of
     * looking pixelated.
     */
    private static Image scaledLogo(BufferedImage source, int targetHeight) {
        int targetWidth = Math.max(1,
                Math.round(targetHeight * (float) source.getWidth() / source.getHeight()));
        Image standard = highQualityScale(source, targetWidth, targetHeight);
        Image hiDpi = highQualityScale(source, targetWidth * 2, targetHeight * 2);
        return new BaseMultiResolutionImage(standard, hiDpi);
    }

    private static BufferedImage highQualityScale(BufferedImage source, int targetWidth, int targetHeight) {
        BufferedImage current = source;
        int width = source.getWidth();
        int height = source.getHeight();
        // Progressive halving gives a noticeably cleaner result than a single
        // large down-scale step, which is what made the logo look pixelated.
        while (width > targetWidth * 2 && height > targetHeight * 2) {
            width = Math.max(targetWidth, width / 2);
            height = Math.max(targetHeight, height / 2);
            current = scaleStep(current, width, height);
        }
        return scaleStep(current, targetWidth, targetHeight);
    }

    private static BufferedImage scaleStep(BufferedImage source, int width, int height) {
        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = target.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(source, 0, 0, width, height, null);
        } finally {
            g2.dispose();
        }
        return target;
    }

    private void setStatus(String key, Status status) {
        SwingUtilities.invokeLater(() -> {
            currentStatus = status;
            setStatusTextOnly(key);
            styleStatusBadge();
        });
    }

    private void setStatusTextOnly(String key) {
        if (statusPill != null) {
            statusPill.setText(t(key));
            styleStatusBadge();
        }
    }

    private void styleStatusBadge() {
        if (statusPill == null) {
            return;
        }
        statusPill.setOpaque(true);
        statusPill.setBackground(palette.surfaceAlt);
        statusPill.setForeground(palette.text);
        statusPill.setIcon(new StatusDotIcon(currentStatus.color(palette)));
        statusPill.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(currentStatus.color(palette)),
                new EmptyBorder(6, 12, 6, 12)));
    }

    private void logInfo(String key, Object... args) {
        logPanel.append(LogLevel.INFO, format(key, args));
    }

    private void logSuccess(String key, Object... args) {
        logPanel.append(LogLevel.SUCCESS, format(key, args));
    }

    private void logWarning(String key, Object... args) {
        logPanel.append(LogLevel.WARNING, format(key, args));
    }

    private void logError(String key, Object... args) {
        logPanel.append(LogLevel.ERROR, format(key, args));
    }

    private void logInfoText(String text) {
        logPanel.append(LogLevel.INFO, text);
    }

    private void logWarningText(String text) {
        logPanel.append(LogLevel.WARNING, text);
    }

    private void logErrorText(String text) {
        logPanel.append(LogLevel.ERROR, text);
    }

    private String format(String key, Object... args) {
        return java.text.MessageFormat.format(t(key), args);
    }

    private String t(String key) {
        return texts.containsKey(key) ? texts.getString(key) : "!" + key + "!";
    }

    private static String trimText(String value) {
        return value == null ? "" : value.trim();
    }
    private static String trim(JTextComponent field) {
        return field.getText().trim();
    }

    private static String trimOrNull(JTextComponent field) {
        String value = trim(field);
        return value.length() == 0 ? null : value;
    }

    private static String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() == 0 ? null : trimmed;
    }

    private static int number(JSpinner spinner) {
        return ((Number) spinner.getValue()).intValue();
    }

    private static String shortCause(Throwable ex) {
        if (ex == null) {
            return "none";
        }
        return ex.getClass().getSimpleName() + ": " + ex.getMessage();
    }

    private static String detailedCause(Throwable ex) {
        if (ex == null) {
            return "none";
        }
        StringBuilder builder = new StringBuilder();
        Throwable current = ex;
        int depth = 0;
        while (current != null && depth < 8) {
            if (depth > 0) {
                builder.append(" -> ");
            }
            builder.append(current.getClass().getSimpleName());
            String message = current.getMessage();
            if (message != null && message.trim().length() > 0) {
                builder.append(": ").append(message.trim());
            }
            current = current.getCause();
            depth++;
        }
        return builder.toString();
    }

    private static File settingsFile() {
        return new File(settingsDirectory(), SETTINGS_FILE_NAME);
    }

    private static File defaultPkiDirectory() {
        return new File(new File(userDataDirectory(), "Broker"),
                PlccomMqttPkiStore.DEFAULT_BASE_DIRECTORY_NAME);
    }

    private static File settingsDirectory() {
        return new File(userDataDirectory(), "Tools");
    }

    private static File userDataDirectory() {
        String home = System.getProperty("user.home", ".");
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        File base;
        if (osName.contains("win")) {
            String appData = System.getenv("APPDATA");
            base = appData == null || appData.trim().length() == 0
                    ? new File(new File(home, "AppData"), "Roaming")
                    : new File(appData);
        } else if (osName.contains("mac")) {
            base = new File(new File(home, "Library"), "Application Support");
        } else {
            String xdgDataHome = System.getenv("XDG_DATA_HOME");
            base = xdgDataHome == null || xdgDataHome.trim().length() == 0
                    ? new File(new File(home, ".local"), "share")
                    : new File(xdgDataHome);
        }
        return new File(new File(base, "PLCcom"), "Mqtt");
    }

    private static void putText(Properties settings, String key, JTextComponent component) {
        settings.setProperty(key, component.getText());
    }

    private static void setText(Properties settings, String key, JTextComponent component) {
        String value = settings.getProperty(key);
        if (value != null) {
            component.setText(value);
        }
    }

    private static void putProtectedText(Properties settings, String key, JTextComponent component) {
        settings.setProperty(key, encryptSetting(component.getText()));
    }

    private static void setProtectedText(Properties settings, String key, JTextComponent component) {
        String value = settings.getProperty(key);
        if (value != null) {
            component.setText(decryptSetting(value));
        }
    }

    private static void putCheck(Properties settings, String key, JCheckBox component) {
        settings.setProperty(key, Boolean.toString(component.isSelected()));
    }

    private static void setCheck(Properties settings, String key, JCheckBox component) {
        String value = settings.getProperty(key);
        if (value != null) {
            component.setSelected(Boolean.parseBoolean(value));
        }
    }

    private static void putCombo(Properties settings, String key, JComboBox<String> component) {
        settings.setProperty(key, Integer.toString(component.getSelectedIndex()));
    }

    private static void setCombo(Properties settings, String key, JComboBox<String> component) {
        String value = settings.getProperty(key);
        if (value == null) {
            return;
        }
        try {
            int index = Integer.parseInt(value);
            if (index >= 0 && index < component.getItemCount()) {
                component.setSelectedIndex(index);
            }
        } catch (NumberFormatException ignored) {
            // Ignore obsolete or manually edited settings values.
        }
    }

    private static void putSpinner(Properties settings, String key, JSpinner component) {
        settings.setProperty(key, String.valueOf(component.getValue()));
    }

    private static void setSpinner(Properties settings, String key, JSpinner component) {
        String value = settings.getProperty(key);
        if (value == null) {
            return;
        }
        try {
            component.setValue(Integer.valueOf(value));
        } catch (RuntimeException ignored) {
            // Ignore obsolete or manually edited settings values.
        }
    }

    private static String encryptSetting(String value) {
        if (value == null || value.length() == 0) {
            return "";
        }
        try {
            byte[] iv = new byte[12];
            SETTINGS_RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, settingsKey(), new GCMParameterSpec(128, iv));
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            return ENCRYPTED_PREFIX + Base64.getEncoder().encodeToString(combined);
        } catch (GeneralSecurityException ex) {
            return "";
        }
    }

    private static String decryptSetting(String value) {
        if (value == null || !value.startsWith(ENCRYPTED_PREFIX)) {
            return value;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(value.substring(ENCRYPTED_PREFIX.length()));
            if (combined.length < 13) {
                return "";
            }
            byte[] iv = Arrays.copyOfRange(combined, 0, 12);
            byte[] encrypted = Arrays.copyOfRange(combined, 12, combined.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, settingsKey(), new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (RuntimeException | GeneralSecurityException ex) {
            return "";
        }
    }

    private static SecretKeySpec settingsKey() throws GeneralSecurityException {
        char[] material = (System.getProperty("user.name", "")
                + "\n" + System.getProperty("user.home", "")
                + "\n" + System.getProperty("os.name", "")).toCharArray();
        try {
            PBEKeySpec spec = new PBEKeySpec(material, SETTINGS_SALT, 65536, 256);
            byte[] key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();
            spec.clearPassword();
            return new SecretKeySpec(key, "AES");
        } finally {
            Arrays.fill(material, '\0');
        }
    }

    private static String version() {
        String version = PlccomMqttBrokerToolApp.class.getPackage().getImplementationVersion();
        return version == null ? "2.1.1" : version;
    }

    private void writeScreenshot(File file) throws IOException {
        BufferedImage image = new BufferedImage(frame.getWidth(), frame.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        frame.paint(image.getGraphics());
        ImageIO.write(image, "png", file);
    }

    private final class DemoAccessValidator implements PlccomMqttBrokerAccessValidator {
        @Override
        public PlccomMqttBrokerAccessDecision validateConnect(PlccomMqttBrokerConnectContext context) {
            String certificateIdentity = clientCertificateIdentity(context);
            logInfo("log.access.connect", context.getClientId(), context.getUserName(),
                    certificateIdentity);
            // This demo rule is intentionally simple: it shows where applications
            // can require credentials, but it does not implement a real password
            // database. Production validators should delegate to the application's
            // own secure user store and must return quickly.
            if (requireUsernameCheck.isSelected()
                    && (context.getUserName() == null || context.getUserName().trim().length() == 0)) {
                return PlccomMqttBrokerAccessDecision.deny(
                        PlccomMqttBrokerAccessDeniedReason.BAD_USER_NAME_OR_PASSWORD,
                        "The tool demo validator requires a user name");
            }
            if (requireClientCertificateCheck.isSelected() && !context.hasClientCertificate()) {
                return PlccomMqttBrokerAccessDecision.deny(
                        PlccomMqttBrokerAccessDeniedReason.NOT_AUTHORIZED,
                        "The tool demo validator requires a trusted client certificate");
            }
            return PlccomMqttBrokerAccessDecision.allow(authenticatedPrincipal(context, certificateIdentity));
        }

        @Override
        public PlccomMqttBrokerAccessDecision validatePublish(PlccomMqttBrokerPublishContext context) {
            // Topic authorization is checked per operation. Returning deny maps to
            // protocol-level negative acknowledgements where the MQTT version
            // supports them.
            if (isBlocked(context.getTopicName())) {
                logWarning("log.access.denied", "PUBLISH", context.getClientId(), context.getTopicName());
                return PlccomMqttBrokerAccessDecision.deny(
                        PlccomMqttBrokerAccessDeniedReason.NOT_AUTHORIZED,
                        "The tool demo validator blocked this publish topic");
            }
            return PlccomMqttBrokerAccessDecision.allow(context.getPrincipal());
        }

        @Override
        public PlccomMqttBrokerAccessDecision validateSubscribe(PlccomMqttBrokerSubscriptionContext context) {
            // Subscription authorization uses the Topic Filter requested by the
            // client. This may contain wildcards, so production policies often
            // compare it against ACL rules instead of a simple string prefix.
            if (isBlocked(context.getTopicFilter())) {
                logWarning("log.access.denied", "SUBSCRIBE", context.getClientId(), context.getTopicFilter());
                return PlccomMqttBrokerAccessDecision.deny(
                        PlccomMqttBrokerAccessDeniedReason.NOT_AUTHORIZED,
                        "The tool demo validator blocked this subscription");
            }
            return PlccomMqttBrokerAccessDecision.allow(context.getPrincipal());
        }

        @Override
        public PlccomMqttBrokerAccessDecision validateUnsubscribe(PlccomMqttBrokerSubscriptionContext context) {
            return PlccomMqttBrokerAccessDecision.allow(context.getPrincipal());
        }

        private boolean isBlocked(String topic) {
            String prefix = trimOrNull(blockedTopicPrefixField);
            return prefix != null && topic != null && topic.startsWith(prefix);
        }

        private String authenticatedPrincipal(PlccomMqttBrokerConnectContext context,
                String certificateIdentity) {
            String userName = trimOrNull(context.getUserName());
            boolean hasCertificate = context.hasClientCertificate();
            if (userName != null && hasCertificate) {
                return userName + " [" + certificateIdentity + "]";
            }
            if (userName != null) {
                return userName;
            }
            if (hasCertificate) {
                return certificateIdentity;
            }
            return null;
        }

        private String clientCertificateIdentity(PlccomMqttBrokerConnectContext context) {
            X509Certificate certificate = context.getClientCertificate();
            if (certificate == null) {
                return t("access.certificate.none");
            }
            return "Subject=" + certificate.getSubjectX500Principal().getName()
                    + ", Issuer=" + certificate.getIssuerX500Principal().getName()
                    + ", Serial=" + certificate.getSerialNumber().toString(16).toUpperCase(Locale.ROOT)
                    + ", SHA1=" + certificateFingerprint(certificate);
        }
    }

    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private enum Status {
        NEUTRAL, SUCCESS, WARNING, ERROR;

        Color color(Palette palette) {
            switch (this) {
                case SUCCESS:
                    return palette.success;
                case WARNING:
                    return palette.warning;
                case ERROR:
                    return palette.danger;
                default:
                    return palette.accent;
            }
        }
    }

    private enum LogLevel {
        INFO, SUCCESS, WARNING, ERROR
    }

    private final class LogPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private final JTextPane textPane = new JTextPane();
        private final JScrollPane scrollPane = new JScrollPane();
        private final java.util.List<LogLine> entries = new java.util.ArrayList<>();
        private final JButton copyButton = iconButton(ToolIcon.COPY, "button.copyLog", this::copy);
        private final JButton saveButton = iconButton(ToolIcon.SAVE, "button.saveLog", this::save);
        private final JButton clearButton = iconButton(ToolIcon.TRASH, "button.clearLog", this::clear);

        LogPanel() {
            super(new BorderLayout(10, 0));
            textPane.setEditable(false);
            textPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            textPane.setBorder(new EmptyBorder(6, 8, 6, 8));
            scrollPane.setViewportView(textPane);
            scrollPane.setBorder(BorderFactory.createLineBorder(palette.border));

            JPanel toolbar = new JPanel();
            toolbar.setName("logToolbar");
            toolbar.setOpaque(false);
            toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));
            toolbar.add(copyButton);
            toolbar.add(javax.swing.Box.createVerticalStrut(6));
            toolbar.add(saveButton);
            toolbar.add(javax.swing.Box.createVerticalStrut(6));
            toolbar.add(clearButton);
            toolbar.add(javax.swing.Box.createVerticalGlue());

            add(toolbar, BorderLayout.WEST);
            add(scrollPane, BorderLayout.CENTER);
            translate();
        }

        void translate() {
            setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(palette.border),
                    t("log.title"), TitledBorder.LEFT, TitledBorder.TOP,
                    getFont().deriveFont(Font.BOLD, 12f), palette.muted));
            scrollPane.setBorder(BorderFactory.createLineBorder(palette.border));
            repaint();
        }

        void append(LogLevel level, String message) {
            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater(() -> append(level, message));
                return;
            }
            String line = TIME.format(new Date()) + "  " + message + System.lineSeparator();
            entries.add(new LogLine(level, line));
            insertLine(level, line);
            textPane.setCaretPosition(textPane.getStyledDocument().getLength());
        }

        private void insertLine(LogLevel level, String line) {
            StyledDocument doc = textPane.getStyledDocument();
            Style style = textPane.addStyle(level.name(), null);
            StyleConstants.setForeground(style, color(level));
            try {
                doc.insertString(doc.getLength(), line, style);
            } catch (BadLocationException ex) {
                textPane.setText(textPane.getText() + line);
            }
        }

        /**
         * Re-renders every stored log line in the current palette colours. Log
         * foregrounds are baked into the styled document at insert time, so without
         * this a theme switch would leave earlier lines in the previous theme's
         * colours (e.g. dark-theme white text on the light background). Called from
         * {@code applyTheme()} on every theme change.
         */
        void recolor() {
            textPane.setText("");
            for (LogLine entry : entries) {
                insertLine(entry.level, entry.text);
            }
            textPane.setCaretPosition(textPane.getStyledDocument().getLength());
        }

        private void clear() {
            entries.clear();
            textPane.setText("");
        }

        private Color color(LogLevel level) {
            switch (level) {
                case SUCCESS:
                    return palette.success;
                case WARNING:
                    return palette.warning;
                case ERROR:
                    return palette.danger;
                default:
                    return palette.text;
            }
        }

        private void save() {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("plccom-mqtt-broker-tool.log"));
            if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                try (FileWriter writer = new FileWriter(chooser.getSelectedFile())) {
                    writer.write(textPane.getText());
                    logInfo("log.saved", chooser.getSelectedFile().getAbsolutePath());
                } catch (IOException ex) {
                    logError("log.save.failed", shortCause(ex));
                }
            }
        }

        private void copy() {
            String selectedText = textPane.getSelectedText();
            String value = selectedText == null || selectedText.length() == 0
                    ? textPane.getText()
                    : selectedText;
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(value), null);
            logInfo("log.copied", selectedText == null || selectedText.length() == 0
                    ? t("log.copy.all")
                    : t("log.copy.selection"));
        }

        private final class LogLine {
            private final LogLevel level;
            private final String text;

            LogLine(LogLevel level, String text) {
                this.level = level;
                this.text = text;
            }
        }
    }

    private static final class SimpleDocumentListener implements javax.swing.event.DocumentListener {
        private final Runnable action;

        SimpleDocumentListener(Runnable action) {
            this.action = action;
        }

        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent event) {
            action.run();
        }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent event) {
            action.run();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent event) {
            action.run();
        }
    }
    private final class ThemedTableRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, selected, hasFocus,
                    row, column);
            int modelColumn = table.convertColumnIndexToModel(column);
            if (modelColumn == LISTENER_CERTIFICATE_COLUMN) {
                setText(displayListenerCertificate(value));
            }
            if (selected) {
                component.setBackground(palette.accentPressed);
                component.setForeground(Color.WHITE);
            } else {
                component.setBackground(row % 2 == 0 ? palette.surface : palette.altRow);
                component.setForeground(palette.text);
            }
            return component;
        }
    }

    private final class ListenerCertificateListRenderer extends ThemedListRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean selected, boolean cellHasFocus) {
            return super.getListCellRendererComponent(list, displayListenerCertificate(value),
                    index, selected, cellHasFocus);
        }
    }

    /** Renders the per-row remove affordance (a small trash glyph) in the last listener column. */
    private final class ListenerRemoveRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        ListenerRemoveRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, "", selected, hasFocus, row, column);
            // The last remaining listener cannot be removed, so its icon is disabled.
            boolean canRemove = table.getRowCount() > 1;
            setIcon(new ToolButtonIcon(ToolIcon.TRASH));
            setForeground(canRemove ? palette.danger : palette.muted);
            setBackground(selected ? palette.accentPressed
                    : (row % 2 == 0 ? palette.surface : palette.altRow));
            setToolTipText(t("button.removeListener.tip"));
            setCursor(canRemove
                    ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    : Cursor.getDefaultCursor());
            return this;
        }
    }

    private class ThemedListRenderer extends DefaultTableCellRenderer
            implements javax.swing.ListCellRenderer<Object> {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
                boolean selected, boolean focus) {
            setText(String.valueOf(value));
            setOpaque(true);
            setBackground(selected ? palette.accentPressed : palette.surfaceAlt);
            setForeground(selected ? Color.WHITE : palette.text);
            setBorder(new EmptyBorder(4, 8, 4, 8));
            return this;
        }
    }

    private final class HelpBubble extends JComponent {
        private static final long serialVersionUID = 1L;
        private static final int SIZE = 16;

        HelpBubble() {
            setPreferredSize(new Dimension(SIZE, CONTROL_HEIGHT));
            setMinimumSize(new Dimension(SIZE, CONTROL_HEIGHT));
            setMaximumSize(new Dimension(SIZE, CONTROL_HEIGHT));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int y = (getHeight() - SIZE) / 2;
                g2.setColor(palette.surfaceAlt);
                g2.fillOval(0, y, SIZE - 1, SIZE - 1);
                g2.setColor(palette.accent);
                g2.drawOval(0, y, SIZE - 1, SIZE - 1);
                g2.setFont(getFont().deriveFont(Font.BOLD, 11f));
                g2.setColor(palette.text);
                int textWidth = g2.getFontMetrics().stringWidth("?");
                int textAscent = g2.getFontMetrics().getAscent();
                int textX = (SIZE - textWidth) / 2;
                int textY = y + (SIZE + textAscent) / 2 - 2;
                g2.drawString("?", textX, textY);
            } finally {
                g2.dispose();
            }
        }
    }

    /**
     * Action button that sizes itself to its content: a fixed, compact height and
     * a width that follows the label (clamped to a sensible minimum). This keeps
     * related buttons visually aligned without hard-coding wide fixed sizes.
     */
    private final class ToolButton extends JButton {
        private static final long serialVersionUID = 1L;

        @Override
        public Dimension getPreferredSize() {
            Dimension preferred = super.getPreferredSize();
            return new Dimension(Math.max(BUTTON_MIN_WIDTH, preferred.width), BUTTON_HEIGHT);
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }
    }

    /** White rounded card that hosts the PLCcom logo crisply in both themes. */
    private static final class LogoCard extends JComponent {
        private static final long serialVersionUID = 1L;
        private final Icon logo;

        LogoCard(Icon logo) {
            this.logo = logo;
            setOpaque(false);
            Dimension size = new Dimension(logo.getIconWidth() + 28, logo.getIconHeight() + 18);
            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int width = getWidth();
                int height = getHeight();
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, width - 1, height - 1, 12, 12);
                g2.setColor(new Color(0xD3DBE3));
                g2.drawRoundRect(0, 0, width - 1, height - 1, 12, 12);
                int x = (width - logo.getIconWidth()) / 2;
                int y = (height - logo.getIconHeight()) / 2;
                logo.paintIcon(this, g2, x, y);
            } finally {
                g2.dispose();
            }
        }
    }

    private enum ToolIcon {
        THEME, LANGUAGE, CERTIFICATE, EXIT, START, STOP, PUBLISH, SUBSCRIBE, REQUEST,
        FILE, ADD, EDIT, REMOVE, COPY, SAVE, TRASH, REFRESH, RESET
    }

    private static final class StatusDotIcon implements Icon {
        private static final int SIZE = 10;
        private final Color color;

        StatusDotIcon(Color color) {
            this.color = color;
        }

        public int getIconWidth() {
            return SIZE;
        }

        public int getIconHeight() {
            return SIZE;
        }

        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(x, y, SIZE, SIZE);
            } finally {
                g2.dispose();
            }
        }
    }

    private static final class ToolButtonIcon implements Icon {
        private static final int SIZE = 16;
        private final ToolIcon icon;

        ToolButtonIcon(ToolIcon icon) {
            this.icon = icon;
        }

        public int getIconWidth() {
            return SIZE;
        }

        public int getIconHeight() {
            return SIZE;
        }

        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(component.isEnabled() ? component.getForeground() : Color.GRAY);
                int cx = x + SIZE / 2;
                int cy = y + SIZE / 2;
                switch (icon) {
                    case THEME:
                        g2.drawOval(x + 3, y + 3, 10, 10);
                        g2.drawLine(cx, y + 1, cx, y + 3);
                        g2.drawLine(cx, y + 13, cx, y + 15);
                        g2.drawLine(x + 1, cy, x + 3, cy);
                        g2.drawLine(x + 13, cy, x + 15, cy);
                        break;
                    case LANGUAGE:
                        g2.drawString("A", x + 1, y + 12);
                        g2.drawString("B", x + 8, y + 12);
                        break;
                    case CERTIFICATE:
                        g2.drawRoundRect(x + 3, y + 2, 10, 12, 3, 3);
                        g2.drawLine(x + 5, y + 6, x + 11, y + 6);
                        g2.drawLine(x + 5, y + 9, x + 10, y + 9);
                        break;
                    case EXIT:
                        g2.drawLine(x + 3, y + 3, x + 3, y + 13);
                        g2.drawLine(x + 3, y + 3, x + 9, y + 3);
                        g2.drawLine(x + 3, y + 13, x + 9, y + 13);
                        g2.drawLine(x + 7, cy, x + 14, cy);
                        g2.drawLine(x + 11, y + 5, x + 14, cy);
                        g2.drawLine(x + 11, y + 11, x + 14, cy);
                        break;
                    case START:
                        g2.fillPolygon(new int[] { x + 4, x + 12, x + 4 }, new int[] { y + 3, cy, y + 13 }, 3);
                        break;
                    case STOP:
                        g2.fillRect(x + 4, y + 4, 9, 9);
                        break;
                    case PUBLISH:
                        g2.drawLine(cx, y + 13, cx, y + 3);
                        g2.drawLine(cx, y + 3, x + 4, y + 8);
                        g2.drawLine(cx, y + 3, x + 12, y + 8);
                        break;
                    case SUBSCRIBE:
                        g2.drawLine(cx, y + 3, cx, y + 13);
                        g2.drawLine(cx, y + 13, x + 4, y + 8);
                        g2.drawLine(cx, y + 13, x + 12, y + 8);
                        break;
                    case REQUEST:
                        g2.drawOval(x + 3, y + 3, 10, 10);
                        g2.drawString("?", x + 6, y + 12);
                        break;
                    case FILE:
                        g2.drawRect(x + 4, y + 3, 8, 10);
                        g2.drawLine(x + 6, y + 6, x + 11, y + 6);
                        g2.drawLine(x + 6, y + 9, x + 11, y + 9);
                        break;
                    case COPY:
                        g2.drawRect(x + 5, y + 5, 7, 9);
                        g2.drawLine(x + 3, y + 3, x + 9, y + 3);
                        g2.drawLine(x + 3, y + 3, x + 3, y + 11);
                        break;
                    case SAVE:
                        g2.drawLine(cx, y + 2, cx, y + 10);
                        g2.drawLine(cx, y + 10, x + 5, y + 7);
                        g2.drawLine(cx, y + 10, x + 11, y + 7);
                        g2.drawLine(x + 3, y + 13, x + 13, y + 13);
                        break;
                    case TRASH:
                        g2.drawLine(x + 3, y + 4, x + 13, y + 4);
                        g2.drawLine(x + 6, y + 4, x + 6, y + 2);
                        g2.drawLine(x + 6, y + 2, x + 10, y + 2);
                        g2.drawLine(x + 10, y + 4, x + 10, y + 2);
                        g2.drawLine(x + 4, y + 4, x + 5, y + 14);
                        g2.drawLine(x + 12, y + 4, x + 11, y + 14);
                        g2.drawLine(x + 5, y + 14, x + 11, y + 14);
                        break;
                    case ADD:
                        g2.drawLine(cx, y + 4, cx, y + 12);
                        g2.drawLine(x + 4, cy, x + 12, cy);
                        break;
                    case EDIT:
                        g2.drawLine(x + 5, y + 12, x + 12, y + 5);
                        g2.drawLine(x + 10, y + 3, x + 13, y + 6);
                        g2.drawLine(x + 4, y + 13, x + 6, y + 12);
                        break;
                    case REMOVE:
                        g2.drawLine(x + 4, cy, x + 12, cy);
                        break;
                    case REFRESH:
                        // Clockwise reload arrow: open circle with a head at the top gap.
                        g2.drawArc(x + 3, y + 3, 10, 10, 95, 250);
                        g2.fillPolygon(new int[] { x + 8, x + 13, x + 12 },
                                new int[] { y + 2, y + 1, y + 6 }, 3);
                        break;
                    case RESET:
                        // Counter-clockwise arrow: mirror of REFRESH, head on the left.
                        g2.drawArc(x + 3, y + 3, 10, 10, 85, -250);
                        g2.fillPolygon(new int[] { x + 8, x + 3, x + 4 },
                                new int[] { y + 2, y + 1, y + 6 }, 3);
                        break;
                    default:
                        break;
                }
            } finally {
                g2.dispose();
            }
        }
    }

    private static final class Palette {
        final boolean dark;
        final Color windowBg;
        final Color surface;
        final Color surfaceAlt;
        final Color border;
        final Color text;
        final Color muted;
        final Color accent;
        final Color accentPressed;
        final Color secondaryBg;
        final Color secondaryBorder;
        final Color success;
        final Color warning;
        final Color danger;
        final Color gridHeader;
        final Color altRow;
        final Color gridLine;

        private Palette(boolean dark, int windowBg, int surface, int surfaceAlt, int border,
                int text, int muted, int accent, int accentPressed, int secondaryBg,
                int secondaryBorder, int success, int warning, int danger,
                int gridHeader, int altRow, int gridLine) {
            this.dark = dark;
            this.windowBg = new Color(windowBg);
            this.surface = new Color(surface);
            this.surfaceAlt = new Color(surfaceAlt);
            this.border = new Color(border);
            this.text = new Color(text);
            this.muted = new Color(muted);
            this.accent = new Color(accent);
            this.accentPressed = new Color(accentPressed);
            this.secondaryBg = new Color(secondaryBg);
            this.secondaryBorder = new Color(secondaryBorder);
            this.success = new Color(success);
            this.warning = new Color(warning);
            this.danger = new Color(danger);
            this.gridHeader = new Color(gridHeader);
            this.altRow = new Color(altRow);
            this.gridLine = new Color(gridLine);
        }

        // One calm, brand-aligned blue accent; neutral greys carry everything else.
        static Palette dark() {
            return new Palette(true, 0x191E26, 0x222A35, 0x2B3440, 0x3A4452,
                    0xE8ECF2, 0x97A3B2, 0x3D7FC4, 0x2F6196, 0x2E3742, 0x434E5C,
                    0x4FB07C, 0xD8A441, 0xD86B5E, 0x2B3440, 0x252D38, 0x333D49);
        }

        static Palette light() {
            return new Palette(false, 0xEDF1F5, 0xFFFFFF, 0xF4F7FA, 0xD4DCE4,
                    0x1B2733, 0x66727F, 0x1F61A8, 0x174B85, 0xEAEFF4, 0xC3CDD7,
                    0x2E8B57, 0xB7791F, 0xC44A3E, 0xEAEFF4, 0xF6F9FB, 0xE2E8EE);
        }
    }

    private static final class StartupArguments {
        final File screenshotFile;
        final boolean light;
        final boolean english;
        final int tabIndex;

        StartupArguments(File screenshotFile, boolean light, boolean english, int tabIndex) {
            this.screenshotFile = screenshotFile;
            this.light = light;
            this.english = english;
            this.tabIndex = tabIndex;
        }

        static StartupArguments parse(String[] args) {
            File screenshot = null;
            boolean light = false;
            boolean english = false;
            int tabIndex = -1;
            for (int i = 0; i < args.length; i++) {
                if ("--screenshot".equals(args[i]) && i + 1 < args.length) {
                    screenshot = new File(args[++i]);
                } else if ("--light".equals(args[i])) {
                    light = true;
                } else if ("--english".equals(args[i])) {
                    english = true;
                } else if ("--tab".equals(args[i]) && i + 1 < args.length) {
                    tabIndex = parseTabIndex(args[++i]);
                }
            }
            return new StartupArguments(screenshot, light, english, tabIndex);
        }

        private static int parseTabIndex(String value) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                return -1;
            }
        }
    }
}

