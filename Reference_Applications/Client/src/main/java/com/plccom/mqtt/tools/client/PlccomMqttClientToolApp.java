package com.plccom.mqtt.tools.client;

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
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JLabel;
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
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
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
import com.plccom.mqtt.client.MqttClientPublishBuilder;
import com.plccom.mqtt.client.MqttClientSubscribeBuilder;
import com.plccom.mqtt.client.MqttClientWillBuilder;
import com.plccom.mqtt.client.MqttMessage;
import com.plccom.mqtt.client.MqttPublishResult;
import com.plccom.mqtt.client.MqttRequestResponseResult;
import com.plccom.mqtt.client.MqttSubscribeResult;
import com.plccom.mqtt.client.PlccomMqttClient;
import com.plccom.mqtt.client.PlccomMqttClientBuilder;
import com.plccom.mqtt.client.PlccomMqttClientFactory;
import com.plccom.mqtt.client.PlccomMqttException;
import com.plccom.mqtt.client.PlccomMqttProtocolVersion;
import com.plccom.mqtt.client.PlccomMqttPublishMessage;
import com.plccom.mqtt.client.PlccomMqttRetainHandling;
import com.plccom.mqtt.client.PlccomMqttTlsOptions;
import com.plccom.mqtt.diagnostics.PlccomMqttDiagnosticEvent;
import com.plccom.mqtt.diagnostics.PlccomMqttDiagnosticLevel;
import com.plccom.mqtt.pki.PlccomMqttOwnCertificate;
import com.plccom.mqtt.pki.PlccomMqttPkiOptions;
import com.plccom.mqtt.pki.PlccomMqttPkiStore;
import com.plccom.mqtt.pki.PlccomMqttPkiStoreEvent;
import com.plccom.mqtt.pki.PlccomMqttPkiStoreKind;

/**
 * Standalone Swing test application for the high-level PLCcom MQTT client API.
 *
 * The tool is intentionally built with JDK/Swing only so it can run on a plain
 * Java 11 installation. It exposes common MQTT client workflows in one place:
 * connect, publish, subscribe, retained messages, QoS 2, WebSocket endpoints,
 * MQTT 5 request/response, Subscription Identifiers and SDK diagnostics.
 *
 * The application is also useful as a companion to the numbered Client
 * workshops: the workshops explain the source code step by step, while this
 * tool lets a user try the same concepts interactively and immediately see the
 * resulting messages, errors and diagnostic events.
 */
public final class PlccomMqttClientToolApp {
    private static final String MESSAGE_BUNDLE =
            "com.plccom.mqtt.tools.client.messages";
    private static final String DEFAULT_ENDPOINT = "mqtt://127.0.0.1:1883";
    private static final String ENDPOINT_TRANSPORT_TCP = "TCP";
    private static final String ENDPOINT_TRANSPORT_TLS = "TLS";
    private static final String ENDPOINT_TRANSPORT_WS = "WebSocket";
    private static final String ENDPOINT_TRANSPORT_WSS = "Secure WebSocket";
    private static final String DEFAULT_CLIENT_ID = "plccom-mqtt-client-tool";
    private static final String APPLICATION_NAME = "PLCcom.Mqtt.Tools.Client";
    private static final String DEFAULT_PKI_FOLDER = defaultPkiDirectory().getAbsolutePath();
    private static final String DEFAULT_OWN_CERTIFICATE_ALIAS = APPLICATION_NAME;
    private static final String SETTINGS_FILE_NAME = "client-tool.properties";
    private static final String ENCRYPTED_PREFIX = "enc:v1:";
    private static final byte[] SETTINGS_SALT = new byte[] {
            80, 76, 67, 99, 111, 109, 77, 81, 84, 84, 84, 111, 111, 108, 115, 49
    };
    private static final SecureRandom SETTINGS_RANDOM = new SecureRandom();
    private static final SimpleDateFormat TIME = new SimpleDateFormat("HH:mm:ss.SSS");
    private static final int FORM_LABEL_WIDTH = 200;
    private static final int CONTROL_HEIGHT = 28;
    private static final int BUTTON_HEIGHT = 30;
    private static final int BUTTON_MIN_WIDTH = 96;
    private static final int ICON_BUTTON_SIZE = 30;
    private static final int NUMERIC_CONTROL_WIDTH = 128;
    private static final int COMBO_CONTROL_WIDTH = 180;
    private static final int TOOL_FIELD_WIDTH = 300;
    // Fixed width for inline checkbox/button rows (e.g. the connection Options and
    // Last Will rows). Sized for the longer language so the two checkboxes fit on
    // one line and the neighbouring column does not shift between DE and EN.
    private static final int INLINE_ROW_WIDTH = 330;
    // Fixed widths for the leading checkbox in option rows, sized for the longer
    // language so the trailing control aligns identically in DE and EN.
    private static final int OPTION_CHECK_NARROW = 88;
    private static final int OPTION_CHECK_WIDE = 162;
    private static final int PAYLOAD_AREA_HEIGHT = 132;
    private static final long REQUEST_TIMEOUT_MILLIS = 5000L;
    private static final int LOGO_HEIGHT = 30;
    private static final int STATUS_BADGE_WIDTH = 150;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "PLCcom-MQTT-Client-Tool");
        thread.setDaemon(true);
        return thread;
    });
    private final List<MqttSubscribeResult> subscriptions = new ArrayList<MqttSubscribeResult>();
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
    private PlccomMqttClient client;
    private boolean settingsPersistenceEnabled = true;
    private boolean settingsSaved;
    private boolean settingsAutoSaveInstalled;
    private Timer settingsAutoSaveTimer;

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
    private JButton connectButton;
    private JButton disconnectButton;
    private LogPanel logPanel;
    private DefaultTableModel messageModel;
    private JTabbedPane tabs;
    private final AtomicBoolean operationActive = new AtomicBoolean();

    private JTextField licenseUserField;
    private JTextField licenseSerialField;
    private JTextField endpointField;
    private JTextField clientIdField;
    private JComboBox<String> protocolCombo;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox cleanStartCheck;
    private JCheckBox reconnectCheck;
    private JCheckBox clientCertificateCheck;
    private JSpinner keepAliveSpinner;
    private JSpinner topicAliasSpinner;
    private JSpinner connectTimeoutSpinner;
    private JSpinner operationTimeoutSpinner;
    private JComboBox<String> diagnosticLevelCombo;
    private JSpinner diagnosticThrottleSpinner;
    private JPasswordField clientCertificatePasswordField;
    private JTextField clientPkiStoreField;
    private JTextField clientPkiOwnAliasField;
    private JCheckBox lastWillCheck;
    private JButton lastWillSettingsButton;
    private JTextField lastWillTopicField;
    private JTextArea lastWillPayloadArea;
    private JComboBox<String> lastWillQosCombo;
    private JCheckBox lastWillRetainCheck;
    private JSpinner lastWillDelaySpinner;
    private JSpinner lastWillExpirySpinner;
    private JTextField lastWillContentTypeField;

    private JTextField publishTopicField;
    private JTextArea publishPayloadArea;
    private JComboBox<String> publishQosCombo;
    private JCheckBox retainCheck;
    private JTextField contentTypeField;
    private JSpinner messageExpirySpinner;
    private JTextField responseTopicPropertyField;
    private JTextField correlationDataField;
    private JTextField userPropertyNameField;
    private JTextField userPropertyValueField;

    private JTextField requestTopicField;
    private JTextField requestResponseTopicField;
    private JTextArea requestPayloadArea;

    private JTextField subscribeTopicField;
    private JComboBox<String> subscribeQosCombo;
    private JCheckBox sharedSubscriptionCheck;
    private JTextField sharedGroupField;
    private JCheckBox noLocalCheck;
    private JCheckBox retainAsPublishedCheck;
    private JComboBox<String> retainHandlingCombo;
    private JCheckBox subscriptionIdentifierCheck;
    private JSpinner subscriptionIdentifierSpinner;
    private JSpinner receiveTimeoutSpinner;


    /**
     * Starts the standalone client test application.
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
                PlccomMqttClientToolApp app = new PlccomMqttClientToolApp();
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
        SwingUtilities.invokeLater(() -> new PlccomMqttClientToolApp().show());
    }

    private void show() {
        buildUi();
        loadSettings();
        installSettingsAutoSave();
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
        addToolTab("tab.connection", createConnectionPanel());
        addToolTab("tab.publish", createPublishPanel());
        addToolTab("tab.subscribe", createSubscribePanel());
        addToolTab("tab.request", createRequestPanel());
        addToolTab("tab.messages", createMessagesPanel());
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

    private Component createConnectionPanel() {
        endpointField = textField(34, DEFAULT_ENDPOINT);
        JButton endpointAssistantButton = button("button.endpointAssistant", this::showEndpointAssistant);
        primaryButtons.add(endpointAssistantButton);
        setFixedSize(endpointAssistantButton, 48, CONTROL_HEIGHT);
        clientIdField = textField(24, DEFAULT_CLIENT_ID);
        protocolCombo = combo("AUTO", "MQTT 5.0", "MQTT 3.1.1");
        usernameField = textField(16, "");
        passwordField = new JPasswordField(16);
        normalizeControlHeight(passwordField);
        cleanStartCheck = checkBox("connect.cleanStart", true);
        reconnectCheck = checkBox("connect.reconnect", true);
        clientCertificateCheck = checkBox("connect.clientCertificate", false);
        clientCertificateCheck.addActionListener(e -> updateActionStates());
        keepAliveSpinner = spinner(60, 0, 65535, 5);
        topicAliasSpinner = plainSpinner(8, 0, 65535, 1);
        connectTimeoutSpinner = spinner(10000, 0, 600000, 1000);
        operationTimeoutSpinner = spinner(10000, 0, 600000, 1000);
        diagnosticLevelCombo = combo("Debug / all", "Info", "Warnings", "Errors", "Fatal only", "Off");
        diagnosticLevelCombo.setSelectedIndex(1);
        diagnosticThrottleSpinner = spinner(1000, 0, 60000, 250);
        clientCertificatePasswordField = new JPasswordField(16);
        normalizeControlHeight(clientCertificatePasswordField);
        clientPkiStoreField = textField(24, DEFAULT_PKI_FOLDER);
        clientPkiOwnAliasField = textField(16, DEFAULT_OWN_CERTIFICATE_ALIAS);
        lastWillCheck = checkBox("will.enabled", false);
        lastWillCheck.addActionListener(e -> updateActionStates());
        lastWillSettingsButton = button("button.lastWillSettings", this::showLastWillDialog);
        lastWillTopicField = textField(30, "plccom/tool/client/status");
        lastWillPayloadArea = new JTextArea("Client disconnected unexpectedly", 3, 48);
        lastWillPayloadArea.setLineWrap(true);
        lastWillPayloadArea.setWrapStyleWord(true);
        lastWillQosCombo = combo("QoS 0", "QoS 1", "QoS 2");
        lastWillRetainCheck = checkBox("will.retain", false);
        lastWillDelaySpinner = plainSpinner(0, 0, Integer.MAX_VALUE, 1);
        lastWillExpirySpinner = plainSpinner(0, 0, Integer.MAX_VALUE, 10);
        lastWillContentTypeField = textField(22, "text/plain");

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel columns = new JPanel(new GridBagLayout());
        columns.setBorder(BorderFactory.createEmptyBorder());
        GridBagConstraints columnGbc = new GridBagConstraints();
        columnGbc.gridy = 0;
        columnGbc.fill = GridBagConstraints.BOTH;
        columnGbc.weighty = 1;

        JPanel connectionPanel = formPanel();
        GridBagConstraints connectionGbc = formGbc();
        int connectionRow = 0;
        addRow(connectionPanel, connectionGbc, connectionRow++, "connect.endpoint",
                endpointInput(endpointAssistantButton));
        addRow(connectionPanel, connectionGbc, connectionRow++, "connect.clientId", toolInput(clientIdField));
        addRow(connectionPanel, connectionGbc, connectionRow++, "connect.protocol", toolInput(protocolCombo));

        addRow(connectionPanel, connectionGbc, connectionRow++, "connect.options",
                fixedInlineRow(cleanStartCheck, reconnectCheck));
        addRow(connectionPanel, connectionGbc, connectionRow++, "connect.clientCertificate",
                clientCertificateCheck);
        addRow(connectionPanel, connectionGbc, connectionRow++, "connect.certificatePassword",
                toolInput(clientCertificatePasswordField));
        addRow(connectionPanel, connectionGbc, connectionRow++, "will.title",
                fixedInlineRow(lastWillCheck, lastWillSettingsButton));
        addFormBottomFiller(connectionPanel, connectionGbc, connectionRow);

        JPanel optionsPanel = formPanel();
        GridBagConstraints optionsGbc = formGbc();
        int optionsRow = 0;
        addRow(optionsPanel, optionsGbc, optionsRow++, "connect.username", toolInput(usernameField));
        addRow(optionsPanel, optionsGbc, optionsRow++, "connect.password", toolInput(passwordField));
        addRow(optionsPanel, optionsGbc, optionsRow++, "connect.keepAlive", toolInput(keepAliveSpinner));
        addRow(optionsPanel, optionsGbc, optionsRow++, "connect.topicAlias", toolInput(topicAliasSpinner));
        addRow(optionsPanel, optionsGbc, optionsRow++, "connect.connectTimeout", toolInput(connectTimeoutSpinner));
        addRow(optionsPanel, optionsGbc, optionsRow++, "connect.operationTimeout", toolInput(operationTimeoutSpinner));
        addRow(optionsPanel, optionsGbc, optionsRow++, "connect.diagnosticLevel", toolInput(diagnosticLevelCombo));
        addRow(optionsPanel, optionsGbc, optionsRow++, "connect.diagnosticThrottle", toolInput(diagnosticThrottleSpinner));
        addFormBottomFiller(optionsPanel, optionsGbc, optionsRow);

        columnGbc.gridx = 0;
        columnGbc.weightx = 0.56;
        columnGbc.insets = new Insets(0, 0, 0, 5);
        columns.add(connectionPanel, columnGbc);

        columnGbc.gridx = 1;
        columnGbc.weightx = 0.44;
        columnGbc.insets = new Insets(0, 5, 0, 0);
        columns.add(optionsPanel, columnGbc);
        panel.add(columns, BorderLayout.CENTER);

        connectButton = primaryButton("button.connect", this::connect);
        disconnectButton = button("button.disconnect", this::disconnect);
        panel.add(actionBar(connectButton, disconnectButton), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createLastWillPanel() {
        JPanel outer = cardPanel(new BorderLayout(0, 8));
        outer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(palette.border),
                new EmptyBorder(10, 14, 12, 14)));

        JPanel titleRow = inlinePanel();
        JLabel title = label("will.title");
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        titleRow.add(title);
        outer.add(titleRow, BorderLayout.NORTH);

        JPanel columns = new JPanel(new GridBagLayout());
        GridBagConstraints columnGbc = new GridBagConstraints();
        columnGbc.gridy = 0;
        columnGbc.fill = GridBagConstraints.BOTH;
        columnGbc.weighty = 1;

        JPanel messagePanel = formPanel();
        messagePanel.setBorder(BorderFactory.createEmptyBorder());
        GridBagConstraints messageGbc = formGbc();
        int messageRow = 0;
        addRow(messagePanel, messageGbc, messageRow++, "will.topic", toolInput(lastWillTopicField));
        addRow(messagePanel, messageGbc, messageRow++, "will.payload",
                fixedToolArea(lastWillPayloadArea, 74));
        addRow(messagePanel, messageGbc, messageRow++, "will.qos", toolInput(lastWillQosCombo));
        addRow(messagePanel, messageGbc, messageRow++, "will.flags", toolInput(lastWillRetainCheck));

        JPanel mqtt5Panel = formPanel();
        mqtt5Panel.setBorder(BorderFactory.createEmptyBorder());
        GridBagConstraints mqtt5Gbc = formGbc();
        int mqtt5Row = 0;
        addRow(mqtt5Panel, mqtt5Gbc, mqtt5Row++, "will.delay", toolInput(lastWillDelaySpinner));
        addRow(mqtt5Panel, mqtt5Gbc, mqtt5Row++, "will.expiry", toolInput(lastWillExpirySpinner));
        addRow(mqtt5Panel, mqtt5Gbc, mqtt5Row++, "will.contentType", toolInput(lastWillContentTypeField));

        columnGbc.gridx = 0;
        columnGbc.weightx = 0.58;
        columnGbc.insets = new Insets(0, 0, 0, 5);
        columns.add(messagePanel, columnGbc);
        columnGbc.gridx = 1;
        columnGbc.weightx = 0.42;
        columnGbc.insets = new Insets(0, 5, 0, 0);
        columns.add(mqtt5Panel, columnGbc);
        outer.add(columns, BorderLayout.CENTER);
        return outer;
    }

    private void showLastWillDialog() {
        JDialog dialog = new JDialog(frame, t("will.dialog.title"), true);
        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBorder(new EmptyBorder(12, 12, 12, 12));
        content.add(createLastWillPanel(), BorderLayout.CENTER);

        JButton closeButton = button("button.close", () -> dialog.dispose());
        JPanel footer = actionBar(closeButton);
        content.add(footer, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        theme(content);
        setLastWillFieldsEnabled(true);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(760, 360));
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private Component createPublishPanel() {
        publishTopicField = textField(32, "plccom/tool/client/demo");
        publishPayloadArea = new JTextArea("Hello from PLCcom MQTT Client Tool", 5, 48);
        publishPayloadArea.setLineWrap(true);
        publishPayloadArea.setWrapStyleWord(true);
        publishQosCombo = combo("QoS 0", "QoS 1", "QoS 2");
        retainCheck = checkBox("publish.retain", false);
        contentTypeField = textField(24, "text/plain");
        messageExpirySpinner = plainSpinner(0, 0, Integer.MAX_VALUE, 10);
        responseTopicPropertyField = textField(30, "");
        correlationDataField = textField(30, "");
        userPropertyNameField = textField(18, "");
        userPropertyValueField = textField(24, "");

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel columns = new JPanel(new GridBagLayout());
        columns.setBorder(BorderFactory.createEmptyBorder());
        GridBagConstraints columnGbc = new GridBagConstraints();
        columnGbc.gridy = 0;
        columnGbc.fill = GridBagConstraints.BOTH;
        columnGbc.weighty = 1;

        JPanel messagePanel = formPanel();
        GridBagConstraints messageGbc = formGbc();
        int messageRow = 0;
        addRow(messagePanel, messageGbc, messageRow++, "publish.topic", toolInput(publishTopicField));
        addRow(messagePanel, messageGbc, messageRow++, "publish.payload",
                fixedToolArea(publishPayloadArea, PAYLOAD_AREA_HEIGHT));
        addRow(messagePanel, messageGbc, messageRow++, "publish.qos", toolInput(publishQosCombo));
        addRow(messagePanel, messageGbc, messageRow++, "publish.flags", toolInput(retainCheck));
        addFormBottomFiller(messagePanel, messageGbc, messageRow);

        JPanel propertiesPanel = formPanel();
        GridBagConstraints propertiesGbc = formGbc();
        int propertiesRow = 0;
        addRow(propertiesPanel, propertiesGbc, propertiesRow++, "publish.contentType",
                toolInput(contentTypeField));
        addRow(propertiesPanel, propertiesGbc, propertiesRow++, "publish.expiry",
                toolInput(messageExpirySpinner));
        addRow(propertiesPanel, propertiesGbc, propertiesRow++, "publish.responseTopic",
                toolInput(responseTopicPropertyField));
        addRow(propertiesPanel, propertiesGbc, propertiesRow++, "publish.correlationData",
                toolInput(correlationDataField));
        addRow(propertiesPanel, propertiesGbc, propertiesRow++, "publish.userPropertyName",
                toolInput(userPropertyNameField));
        addRow(propertiesPanel, propertiesGbc, propertiesRow++, "publish.userPropertyValue",
                toolInput(userPropertyValueField));
        addFormBottomFiller(propertiesPanel, propertiesGbc, propertiesRow);

        columnGbc.gridx = 0;
        columnGbc.weightx = 0.5;
        columnGbc.insets = new Insets(0, 0, 0, 5);
        columns.add(messagePanel, columnGbc);

        columnGbc.gridx = 1;
        columnGbc.weightx = 0.5;
        columnGbc.insets = new Insets(0, 5, 0, 0);
        columns.add(propertiesPanel, columnGbc);
        panel.add(columns, BorderLayout.CENTER);

        panel.add(actionBar(
                primaryButton("button.publish", this::publish),
                button("button.publishJson", this::publishJson),
                button("button.deleteRetained", this::deleteRetained)), BorderLayout.SOUTH);
        return panel;
    }

    private Component createRequestPanel() {
        requestTopicField = textField(32, "request/plccom");
        requestResponseTopicField = textField(32, "response/plccom/client");
        requestPayloadArea = new JTextArea("read", 5, 48);
        requestPayloadArea.setLineWrap(true);
        requestPayloadArea.setWrapStyleWord(true);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel columns = new JPanel(new GridBagLayout());
        columns.setBorder(BorderFactory.createEmptyBorder());
        GridBagConstraints columnGbc = new GridBagConstraints();
        columnGbc.gridy = 0;
        columnGbc.fill = GridBagConstraints.BOTH;
        columnGbc.weighty = 1;

        JPanel requestForm = formPanel();
        GridBagConstraints requestGbc = formGbc();
        int requestRow = 0;
        addRow(requestForm, requestGbc, requestRow++, "request.topic", toolInput(requestTopicField));
        addRow(requestForm, requestGbc, requestRow++, "request.responseTopic",
                toolInput(requestResponseTopicField));
        addRow(requestForm, requestGbc, requestRow++, "request.payload",
                fixedToolArea(requestPayloadArea, PAYLOAD_AREA_HEIGHT));
        addFormBottomFiller(requestForm, requestGbc, requestRow);

        JPanel infoCard = formPanel();
        GridBagConstraints infoGbc = formGbc();
        infoGbc.gridx = 0;
        infoGbc.gridy = 0;
        infoGbc.gridwidth = 2;
        infoGbc.weightx = 1;
        infoGbc.fill = GridBagConstraints.HORIZONTAL;
        infoCard.add(label("request.info"), infoGbc);
        infoGbc.gridwidth = 1;
        addFormBottomFiller(infoCard, infoGbc, 1);

        columnGbc.gridx = 0;
        columnGbc.weightx = 0.6;
        columnGbc.insets = new Insets(0, 0, 0, 5);
        columns.add(requestForm, columnGbc);

        columnGbc.gridx = 1;
        columnGbc.weightx = 0.4;
        columnGbc.insets = new Insets(0, 5, 0, 0);
        columns.add(infoCard, columnGbc);
        panel.add(columns, BorderLayout.CENTER);

        panel.add(actionBar(primaryButton("button.request", this::sendRequest)), BorderLayout.SOUTH);
        return panel;
    }

    private Component createSubscribePanel() {
        subscribeTopicField = textField(34, "plccom/tool/client/#");
        subscribeQosCombo = combo("QoS 0", "QoS 1", "QoS 2");
        sharedSubscriptionCheck = checkBox("subscribe.shared", false);
        sharedGroupField = textField(18, "tool-workers");
        noLocalCheck = checkBox("subscribe.noLocal", false);
        retainAsPublishedCheck = checkBox("subscribe.retainAsPublished", false);
        retainHandlingCombo = combo("Send retained now", "Only if new", "Do not send retained");
        subscriptionIdentifierCheck = checkBox("subscribe.identifier.enabled", false);
        subscriptionIdentifierSpinner = plainSpinner(1, 1, 268435455, 1);
        receiveTimeoutSpinner = spinner(1000, 0, 60000, 250);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel columns = new JPanel(new GridBagLayout());
        columns.setBorder(BorderFactory.createEmptyBorder());
        GridBagConstraints columnGbc = new GridBagConstraints();
        columnGbc.gridy = 0;
        columnGbc.fill = GridBagConstraints.BOTH;
        columnGbc.weighty = 1;

        JPanel subscriptionPanel = formPanel();
        GridBagConstraints subscriptionGbc = formGbc();
        int subscriptionRow = 0;
        addRow(subscriptionPanel, subscriptionGbc, subscriptionRow++, "subscribe.topic",
                toolInput(subscribeTopicField));
        addRow(subscriptionPanel, subscriptionGbc, subscriptionRow++, "subscribe.qos",
                toolInput(subscribeQosCombo));
        addRow(subscriptionPanel, subscriptionGbc, subscriptionRow++, "subscribe.sharedRow",
                optionFieldRow(OPTION_CHECK_NARROW, sharedSubscriptionCheck, sharedGroupField));
        addRow(subscriptionPanel, subscriptionGbc, subscriptionRow++, "subscribe.receiveTimeout",
                toolInput(receiveTimeoutSpinner));
        addFormBottomFiller(subscriptionPanel, subscriptionGbc, subscriptionRow);

        JPanel mqtt5Panel = formPanel();
        GridBagConstraints mqtt5Gbc = formGbc();
        int mqtt5Row = 0;
        addRow(mqtt5Panel, mqtt5Gbc, mqtt5Row++, "subscribe.mqtt5",
                optionRow(noLocalCheck, retainAsPublishedCheck));
        addRow(mqtt5Panel, mqtt5Gbc, mqtt5Row++, "subscribe.retainHandling",
                toolInput(retainHandlingCombo));
        addRow(mqtt5Panel, mqtt5Gbc, mqtt5Row++, "subscribe.identifier",
                optionFieldRow(OPTION_CHECK_WIDE, subscriptionIdentifierCheck, subscriptionIdentifierSpinner));
        addFormBottomFiller(mqtt5Panel, mqtt5Gbc, mqtt5Row);

        columnGbc.gridx = 0;
        columnGbc.weightx = 0.5;
        columnGbc.insets = new Insets(0, 0, 0, 5);
        columns.add(subscriptionPanel, columnGbc);

        columnGbc.gridx = 1;
        columnGbc.weightx = 0.5;
        columnGbc.insets = new Insets(0, 5, 0, 0);
        columns.add(mqtt5Panel, columnGbc);
        panel.add(columns, BorderLayout.CENTER);

        panel.add(actionBar(
                primaryButton("button.subscribe", this::subscribe),
                button("button.receiveOnce", this::receiveOnce),
                button("button.unsubscribeAll", this::unsubscribeAll)), BorderLayout.SOUTH);
        return panel;
    }

    private Component createMessagesPanel() {
        messageModel = new DefaultTableModel(new Object[] {
                "Time", "Topic", "QoS", "Retain", "Payload", "Content Type", "Subscription IDs" }, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(messageModel);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new ThemedTableRenderer());

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(actionBar(button("button.clearMessages", () -> messageModel.setRowCount(0))),
                BorderLayout.SOUTH);
        return panel;
    }

    private Component createLogPanel() {
        logPanel = new LogPanel();
        JPanel outer = new JPanel(new BorderLayout(0, 0));
        outer.setPreferredSize(new Dimension(300, 210));
        outer.add(logPanel, BorderLayout.CENTER);
        return outer;
    }

    private void connect() {
        PlccomMqttClient current = client;
        if (current != null && current.isConnected()) {
            logInfo("log.already.connected");
            updateActionStates();
            return;
        }
        runAsync("connect", () -> {
            // A new connection test should start from a known state. Closing the
            // previous client also removes old subscriptions so later messages are
            // caused only by the settings currently visible in the UI.
            closeClientQuietly();

            // License validation deliberately happens at the real product
            // boundary: building/connecting the high-level client. If the user or
            // serial number is wrong, the exception is shown in the tool log.
            String endpoint = endpointForConnect();
            PlccomMqttClientBuilder builder = PlccomMqttClientFactory
                    .builder(trim(licenseUserField), trim(licenseSerialField))
                    .endpoint(endpoint)
                    .identifier(trimOrNull(clientIdField))
                    .cleanStart(cleanStartCheck.isSelected())
                    .keepAliveSeconds(number(keepAliveSpinner))
                    .topicAliasMaximum(number(topicAliasSpinner))
                    .connectTimeoutMillis(number(connectTimeoutSpinner))
                    .operationTimeoutMillis(number(operationTimeoutSpinner))
                    .diagnosticLevel(selectedDiagnosticLevel(diagnosticLevelCombo))
                    .diagnosticThrottleMillis(number(diagnosticThrottleSpinner))
                    .onDiagnostic(this::diagnostic)
                    .onConnected(ctx -> logSuccess("log.connected.details",
                            ctx.clientId(), ctx.negotiatedProtocolVersion(), ctx.sessionPresent()))
                    .onDisconnected(ctx -> logWarning("log.disconnected.details",
                            ctx.clientId(), ctx.graceful(), shortCause(ctx.cause())))
                    .onReconnectFailed(ctx -> logError("log.reconnect.failed",
                            ctx.clientId(), shortCause(ctx.cause())));

            applyProtocol(builder);
            applyCredentials(builder);
            applyLastWill(builder);
            applyClientTls(builder);
            if (reconnectCheck.isSelected()) {
                // Automatic reconnect is useful for real networks where a cable,
                // VPN or broker restart can interrupt the TCP connection.
                builder.automaticReconnect();
            }
            PlccomMqttClient created = builder.build();
            created.connect();
            client = created;
            setStatus("status.connected", Status.SUCCESS);
            logSuccess("log.connected", created.getNegotiatedProtocolVersion());
        });
    }

    private void disconnect() {
        PlccomMqttClient current = client;
        if (current == null || !current.isConnected()) {
            logInfo("log.already.disconnected");
            updateActionStates();
            return;
        }
        runAsync("disconnect", () -> {
            closeClientQuietly();
            setStatus("status.ready", Status.NEUTRAL);
            logInfo("log.disconnected");
        });
    }

    private void publish() {
        runAsync("publish", () -> {
            PlccomMqttClient current = requireConnectedClient();
            MqttClientPublishBuilder builder = current.publish(trim(publishTopicField), publishPayloadArea.getText());
            applyQos(builder, publishQosCombo);

            // Retain stores the last value for a topic on the broker. Message
            // Expiry and the response/correlation fields are MQTT 5 metadata; MQTT
            // 3.1.1 brokers simply do not negotiate those features.
            builder.retain(retainCheck.isSelected()).payloadFormatUtf8();
            String contentType = trimOrNull(contentTypeField);
            if (contentType != null) {
                builder.contentType(contentType);
            }
            int expiry = number(messageExpirySpinner);
            if (expiry > 0) {
                builder.messageExpirySeconds((long) expiry);
            }
            String responseTopic = trimOrNull(responseTopicPropertyField);
            if (responseTopic != null) {
                builder.responseTopic(responseTopic);
            }
            String correlationText = trimOrNull(correlationDataField);
            if (correlationText != null) {
                builder.correlationData(correlationText.getBytes(StandardCharsets.UTF_8));
            }
            String userPropertyName = trimOrNull(userPropertyNameField);
            String userPropertyValue = trimOrNull(userPropertyValueField);
            if (userPropertyName != null && userPropertyValue != null) {
                builder.userProperty(userPropertyName, userPropertyValue);
            }
            MqttPublishResult result = requireGood(builder.send());
            logSuccess("log.published", result.getTopic(), result.getQualityOfService(), result.isRetain());
        });
    }

    private void publishJson() {
        runAsync("publishJson", () -> {
            PlccomMqttClient current = requireConnectedClient();
            // The high-level API accepts normal Java maps for JSON payloads. This
            // keeps workshop code focused on MQTT instead of manual JSON string
            // escaping.
            Map<String, Object> payload = new LinkedHashMap<String, Object>();
            payload.put("source", "PLCcom MQTT Client Tool");
            payload.put("text", publishPayloadArea.getText());
            payload.put("time", System.currentTimeMillis());
            MqttClientPublishBuilder builder = current.publishJson(trim(publishTopicField), payload);
            applyQos(builder, publishQosCombo);
            builder.retain(retainCheck.isSelected());
            MqttPublishResult result = requireGood(builder.send());
            logSuccess("log.publishedJson", result.getTopic(), result.getQualityOfService());
        });
    }

    private void deleteRetained() {
        runAsync("deleteRetained", () -> {
            PlccomMqttClient current = requireConnectedClient();
            // MQTT deletes retained state by publishing a retained message with an
            // empty payload to the same Topic Name.
            requireGood(current.publish(trim(publishTopicField), new byte[0])
                    .qos1()
                    .retain(true)
                    .send());
            logSuccess("log.retained.deleted", trim(publishTopicField));
        });
    }

    private void sendRequest() {
        runAsync("request", () -> {
            PlccomMqttClient current = requireConnectedClient();
            String requestTopic = trim(requestTopicField);
            String responseTopic = trim(requestResponseTopicField);
            logInfo("log.request.sending", requestTopic, responseTopic);

            // MQTT 5 request/response is normal publish + subscribe under the hood:
            // the builder subscribes to the response topic, publishes the request
            // with Response Topic and random Correlation Data, then waits for the
            // matching reply before cleaning up the temporary subscription.
            MqttRequestResponseResult result = current.request(requestTopic, requestPayloadArea.getText())
                    .responseTopic(responseTopic)
                    .responseQos1()
                    .randomCorrelationData()
                    .timeoutMillis(REQUEST_TIMEOUT_MILLIS)
                    .send();
            requireGood(result);
            // Reuse the standard message handler so the answer appears both in the
            // log and in the Messages tab, exactly like a subscribed message.
            messageReceived(result.getResponse());
        });
    }

    private void subscribe() {
        runAsync("subscribe", () -> {
            PlccomMqttClient current = requireConnectedClient();
            MqttClientSubscribeBuilder builder = current.subscribe(trim(subscribeTopicField));
            applyQos(builder, subscribeQosCombo);
            if (sharedSubscriptionCheck.isSelected()) {
                // A Shared Subscription lets several clients divide work from one
                // Topic Filter. The broker chooses which group member receives each
                // matching publication.
                builder.sharedSubscription(trim(sharedGroupField));
            }

            // These options are MQTT 5 subscription controls: no-local avoids
            // receiving our own publishes, retain-as-published preserves the retain
            // flag, and retain handling controls replay of stored retained state.
            builder.noLocal(noLocalCheck.isSelected())
                    .retainAsPublished(retainAsPublishedCheck.isSelected())
                    .retainHandling(retainHandling());
            if (subscriptionIdentifierCheck.isSelected()) {
                // Subscription Identifiers are MQTT 5 numbers that help an
                // application map incoming messages back to the subscription that
                // matched them.
                builder.subscriptionIdentifier(number(subscriptionIdentifierSpinner));
            }
            builder.onMessage(this::messageReceived);
            MqttSubscribeResult result = requireGood(builder.send());
            synchronized (subscriptions) {
                subscriptions.add(result);
            }
            logSuccess("log.subscribed", result.getTopicFilter(), result.getMaximumQualityOfService());
        });
    }

    private void receiveOnce() {
        runAsync("receiveOnce", () -> {
            PlccomMqttClient current = requireConnectedClient();
            PlccomMqttPublishMessage message = current.receive(number(receiveTimeoutSpinner));
            if (message == null) {
                logWarning("log.receive.timeout");
            } else {
                messageReceived(new MqttMessage(message));
            }
        });
    }

    private void unsubscribeAll() {
        runAsync("unsubscribeAll", () -> {
            List<MqttSubscribeResult> copy;
            synchronized (subscriptions) {
                copy = new ArrayList<MqttSubscribeResult>(subscriptions);
                subscriptions.clear();
            }
            for (MqttSubscribeResult result : copy) {
                if (result.isActive()) {
                    requireGood(result.unsubscribe());
                    logInfo("log.unsubscribed", result.getTopicFilter());
                }
            }
        });
    }

    private PlccomMqttClient requireConnectedClient() throws PlccomMqttException {
        PlccomMqttClient current = client;
        if (current == null || !current.isConnected()) {
            throw new PlccomMqttException(t("error.notConnected"));
        }
        return current;
    }

    private <T extends OperationResult> T requireGood(T result) throws PlccomMqttException {
        if (result == null) {
            throw new PlccomMqttException("MQTT operation returned no result");
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

    private void closeClientQuietly() {
        unsubscribeAllQuietly();
        PlccomMqttClient current = client;
        client = null;
        if (current != null) {
            try {
                if (current.isConnected()) {
                    current.disconnect();
                }
            } catch (RuntimeException | PlccomMqttException ex) {
                logWarningText(t("log.disconnect.cleanup") + " " + shortCause(ex));
            } finally {
                current.close();
            }
        }
    }

    private void unsubscribeAllQuietly() {
        List<MqttSubscribeResult> copy;
        synchronized (subscriptions) {
            copy = new ArrayList<MqttSubscribeResult>(subscriptions);
            subscriptions.clear();
        }
        for (MqttSubscribeResult result : copy) {
            try {
                if (result.isActive()) {
                    requireGood(result.unsubscribe());
                }
            } catch (RuntimeException | PlccomMqttException ex) {
                logWarningText(t("log.unsubscribe.cleanup") + " " + shortCause(ex));
            }
        }
    }

    private void shutdown() {
        if (settingsAutoSaveTimer != null) {
            settingsAutoSaveTimer.stop();
        }
        if (settingsPersistenceEnabled && !settingsSaved) {
            settingsSaved = true;
            saveSettings();
        }
        closeClientQuietly();
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
                    "PLCcom MQTT Client Tool user settings. Sensitive values use AES/GCM encryption.");
        } catch (IOException ignored) {
            // Settings persistence is a convenience feature. A failure here must
            // never prevent the user application from closing cleanly.
        }
    }

    private void loadToolSettings(Properties settings) {
        setProtectedText(settings, "license.user", licenseUserField);
        setProtectedText(settings, "license.serial", licenseSerialField);
        setText(settings, "connection.endpoint", endpointField);
        setText(settings, "connection.clientId", clientIdField);
        setCombo(settings, "connection.protocol", protocolCombo);
        setProtectedText(settings, "connection.username", usernameField);
        setProtectedText(settings, "connection.password", passwordField);
        setCheck(settings, "connection.cleanStart", cleanStartCheck);
        setCheck(settings, "connection.reconnect", reconnectCheck);
        setCheck(settings, "connection.clientCertificate", clientCertificateCheck);
        setSpinner(settings, "connection.keepAlive", keepAliveSpinner);
        setSpinner(settings, "connection.topicAlias", topicAliasSpinner);
        setSpinner(settings, "connection.connectTimeout", connectTimeoutSpinner);
        setSpinner(settings, "connection.operationTimeout", operationTimeoutSpinner);
        setCombo(settings, "connection.diagnosticLevel", diagnosticLevelCombo);
        setSpinner(settings, "connection.diagnosticThrottle", diagnosticThrottleSpinner);
        setProtectedText(settings, "connection.clientCertificatePassword", clientCertificatePasswordField);
        setCheck(settings, "connection.lastWill", lastWillCheck);
        setText(settings, "connection.lastWillTopic", lastWillTopicField);
        setText(settings, "connection.lastWillPayload", lastWillPayloadArea);
        setCombo(settings, "connection.lastWillQos", lastWillQosCombo);
        setCheck(settings, "connection.lastWillRetain", lastWillRetainCheck);
        setSpinner(settings, "connection.lastWillDelay", lastWillDelaySpinner);
        setSpinner(settings, "connection.lastWillExpiry", lastWillExpirySpinner);
        setText(settings, "connection.lastWillContentType", lastWillContentTypeField);
        clientPkiStoreField.setText(DEFAULT_PKI_FOLDER);
        clientPkiOwnAliasField.setText(DEFAULT_OWN_CERTIFICATE_ALIAS);

        setText(settings, "publish.topic", publishTopicField);
        setText(settings, "publish.payload", publishPayloadArea);
        setCombo(settings, "publish.qos", publishQosCombo);
        setCheck(settings, "publish.retain", retainCheck);
        setText(settings, "publish.contentType", contentTypeField);
        setSpinner(settings, "publish.expiry", messageExpirySpinner);
        setText(settings, "publish.responseTopic", responseTopicPropertyField);
        setText(settings, "publish.correlationData", correlationDataField);
        setText(settings, "publish.userPropertyName", userPropertyNameField);
        setText(settings, "publish.userPropertyValue", userPropertyValueField);

        setText(settings, "request.topic", requestTopicField);
        setText(settings, "request.responseTopic", requestResponseTopicField);
        setText(settings, "request.payload", requestPayloadArea);

        setText(settings, "subscribe.topic", subscribeTopicField);
        setCombo(settings, "subscribe.qos", subscribeQosCombo);
        setCheck(settings, "subscribe.shared", sharedSubscriptionCheck);
        setText(settings, "subscribe.sharedGroup", sharedGroupField);
        setCheck(settings, "subscribe.noLocal", noLocalCheck);
        setCheck(settings, "subscribe.retainAsPublished", retainAsPublishedCheck);
        setCombo(settings, "subscribe.retainHandling", retainHandlingCombo);
        setCheck(settings, "subscribe.identifierEnabled", subscriptionIdentifierCheck);
        setSpinner(settings, "subscribe.identifier", subscriptionIdentifierSpinner);
        setSpinner(settings, "subscribe.receiveTimeout", receiveTimeoutSpinner);
    }

    private void saveToolSettings(Properties settings) {
        putProtectedText(settings, "license.user", licenseUserField);
        putProtectedText(settings, "license.serial", licenseSerialField);
        putText(settings, "connection.endpoint", endpointField);
        putText(settings, "connection.clientId", clientIdField);
        putCombo(settings, "connection.protocol", protocolCombo);
        putProtectedText(settings, "connection.username", usernameField);
        putProtectedText(settings, "connection.password", passwordField);
        putCheck(settings, "connection.cleanStart", cleanStartCheck);
        putCheck(settings, "connection.reconnect", reconnectCheck);
        putCheck(settings, "connection.clientCertificate", clientCertificateCheck);
        putSpinner(settings, "connection.keepAlive", keepAliveSpinner);
        putSpinner(settings, "connection.topicAlias", topicAliasSpinner);
        putSpinner(settings, "connection.connectTimeout", connectTimeoutSpinner);
        putSpinner(settings, "connection.operationTimeout", operationTimeoutSpinner);
        putCombo(settings, "connection.diagnosticLevel", diagnosticLevelCombo);
        putSpinner(settings, "connection.diagnosticThrottle", diagnosticThrottleSpinner);
        putProtectedText(settings, "connection.clientCertificatePassword", clientCertificatePasswordField);
        putCheck(settings, "connection.lastWill", lastWillCheck);
        putText(settings, "connection.lastWillTopic", lastWillTopicField);
        putText(settings, "connection.lastWillPayload", lastWillPayloadArea);
        putCombo(settings, "connection.lastWillQos", lastWillQosCombo);
        putCheck(settings, "connection.lastWillRetain", lastWillRetainCheck);
        putSpinner(settings, "connection.lastWillDelay", lastWillDelaySpinner);
        putSpinner(settings, "connection.lastWillExpiry", lastWillExpirySpinner);
        putText(settings, "connection.lastWillContentType", lastWillContentTypeField);

        putText(settings, "publish.topic", publishTopicField);
        putText(settings, "publish.payload", publishPayloadArea);
        putCombo(settings, "publish.qos", publishQosCombo);
        putCheck(settings, "publish.retain", retainCheck);
        putText(settings, "publish.contentType", contentTypeField);
        putSpinner(settings, "publish.expiry", messageExpirySpinner);
        putText(settings, "publish.responseTopic", responseTopicPropertyField);
        putText(settings, "publish.correlationData", correlationDataField);
        putText(settings, "publish.userPropertyName", userPropertyNameField);
        putText(settings, "publish.userPropertyValue", userPropertyValueField);

        putText(settings, "request.topic", requestTopicField);
        putText(settings, "request.responseTopic", requestResponseTopicField);
        putText(settings, "request.payload", requestPayloadArea);

        putText(settings, "subscribe.topic", subscribeTopicField);
        putCombo(settings, "subscribe.qos", subscribeQosCombo);
        putCheck(settings, "subscribe.shared", sharedSubscriptionCheck);
        putText(settings, "subscribe.sharedGroup", sharedGroupField);
        putCheck(settings, "subscribe.noLocal", noLocalCheck);
        putCheck(settings, "subscribe.retainAsPublished", retainAsPublishedCheck);
        putCombo(settings, "subscribe.retainHandling", retainHandlingCombo);
        putCheck(settings, "subscribe.identifierEnabled", subscriptionIdentifierCheck);
        putSpinner(settings, "subscribe.identifier", subscriptionIdentifierSpinner);
        putSpinner(settings, "subscribe.receiveTimeout", receiveTimeoutSpinner);
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
                if (client == null || !client.isConnected()) {
                    setStatus("status.ready", Status.NEUTRAL);
                } else {
                    setStatus("status.connected", Status.SUCCESS);
                }
            } catch (Throwable ex) {
                // All button actions end here on failure, including invalid
                // license data, broker connection problems, TLS errors and MQTT
                // protocol errors. The UI stays alive and the user sees the cause
                // in the log instead of losing the whole test application.
                setStatus("status.error", Status.ERROR);
                logError("log.operation.failed", operation, shortCause(ex));
                logErrorText(t("log.failure.details") + " " + detailedCause(ex));
            } finally {
                operationActive.set(false);
                updateActionStates();
            }
        });
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
    private void applyClientTls(PlccomMqttClientBuilder builder) throws Exception {
        if (!isSecureEndpoint(trim(endpointField))) {
            return;
        }
        char[] password = clientCertificatePasswordField.getPassword();
        try {
            PlccomMqttPkiStore pkiStore = createPkiStore();
            PlccomMqttTlsOptions.Builder tls = PlccomMqttTlsOptions.builder()
                    .enabledProtocols("TLSv1.3", "TLSv1.2")
                    .pkiOptions(PlccomMqttPkiOptions.builder()
                            .pkiStore(pkiStore)
                            .build());
            if (clientCertificateCheck.isSelected()) {
                String pkiOwnAlias = trimOrNull(clientPkiOwnAliasField);
                if (pkiOwnAlias == null) {
                    throw new IllegalStateException(t("error.clientOwnAliasRequired"));
                }
                PlccomMqttOwnCertificate ownCertificate = new PlccomMqttOwnCertificate(
                        pkiStore, pkiOwnAlias, password, 3650, "Indi.An GmbH",
                        Arrays.asList("localhost"),
                        Arrays.asList(InetAddress.getByName("127.0.0.1"),
                                InetAddress.getByName("::1")));
                KeyStore clientKeyStore = ownCertificate.loadOrCreate();
                logInfo("log.tls.clientCertificate.loaded", pkiOwnAlias,
                        describeKeyStoreAliases(clientKeyStore));
                tls.keyStore(clientKeyStore, password);
            } else {
                logInfo("log.tls.clientCertificate.disabled");
            }
            builder.tls(tls.build());
        } finally {
            Arrays.fill(password, '\0');
        }
    }


    private String describeKeyStoreAliases(KeyStore keyStore) {
        try {
            StringBuilder builder = new StringBuilder();
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                builder.append(alias);
                builder.append(keyStore.isKeyEntry(alias) ? ":key" : ":certificate");
            }
            return builder.length() == 0 ? "empty" : builder.toString();
        } catch (Exception ex) {
            return "unavailable (" + shortCause(ex) + ")";
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

    private void installSettingsAutoSave() {
        if (settingsAutoSaveInstalled) {
            return;
        }
        settingsAutoSaveInstalled = true;
        settingsAutoSaveTimer = new Timer(600, event -> {
            if (settingsPersistenceEnabled) {
                saveSettings();
            }
        });
        settingsAutoSaveTimer.setRepeats(false);

        bindTextAutoSave(licenseUserField, licenseSerialField, endpointField, clientIdField,
                usernameField, passwordField, clientCertificatePasswordField,
                lastWillTopicField, lastWillPayloadArea, lastWillContentTypeField,
                publishTopicField, publishPayloadArea, contentTypeField,
                responseTopicPropertyField, correlationDataField, userPropertyNameField,
                userPropertyValueField, requestTopicField, requestResponseTopicField,
                requestPayloadArea, subscribeTopicField, sharedGroupField);
        bindComboAutoSave(protocolCombo, diagnosticLevelCombo, lastWillQosCombo, publishQosCombo,
                subscribeQosCombo, retainHandlingCombo);
        bindButtonAutoSave(cleanStartCheck, reconnectCheck, clientCertificateCheck,
                lastWillCheck, lastWillRetainCheck, retainCheck, sharedSubscriptionCheck,
                noLocalCheck, retainAsPublishedCheck, subscriptionIdentifierCheck);
        bindSpinnerAutoSave(keepAliveSpinner, topicAliasSpinner, connectTimeoutSpinner,
                operationTimeoutSpinner, diagnosticThrottleSpinner, lastWillDelaySpinner,
                lastWillExpirySpinner, messageExpirySpinner, subscriptionIdentifierSpinner,
                receiveTimeoutSpinner);
    }

    private void bindTextAutoSave(JTextComponent... components) {
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                scheduleSettingsAutoSave();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                scheduleSettingsAutoSave();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                scheduleSettingsAutoSave();
            }
        };
        for (JTextComponent component : components) {
            if (component != null) {
                component.getDocument().addDocumentListener(listener);
            }
        }
    }

    private void bindComboAutoSave(JComboBox<?>... components) {
        for (JComboBox<?> component : components) {
            if (component != null) {
                component.addActionListener(event -> scheduleSettingsAutoSave());
            }
        }
    }

    private void bindButtonAutoSave(AbstractButton... components) {
        for (AbstractButton component : components) {
            if (component != null) {
                component.addActionListener(event -> scheduleSettingsAutoSave());
            }
        }
    }

    private void bindSpinnerAutoSave(JSpinner... components) {
        for (JSpinner component : components) {
            if (component != null) {
                component.addChangeListener(event -> scheduleSettingsAutoSave());
            }
        }
    }

    private void scheduleSettingsAutoSave() {
        if (!settingsPersistenceEnabled || !settingsAutoSaveInstalled || settingsAutoSaveTimer == null) {
            return;
        }
        settingsAutoSaveTimer.restart();
    }

    private File prepareHelpWebsite() throws IOException {
        String language = Locale.GERMAN.getLanguage().equals(locale.getLanguage()) ? "de" : "en";
        File targetDirectory = new File(new File(new File(settingsDirectory(), "Help"), "Client"), version());
        targetDirectory = new File(targetDirectory, language);
        if (!targetDirectory.exists() && !targetDirectory.mkdirs()) {
            throw new IOException("Cannot create help directory: " + targetDirectory.getAbsolutePath());
        }
        copyHelpResource("/com/plccom/mqtt/tools/client/help/common/styles.css",
                new File(targetDirectory, "styles.css"));
        File index = new File(targetDirectory, "index.html");
        copyHelpResource("/com/plccom/mqtt/tools/client/help/" + language + "/index.html", index);
        return index;
    }

    private void copyHelpResource(String resourceName, File target) throws IOException {
        try (InputStream input = PlccomMqttClientToolApp.class.getResourceAsStream(resourceName)) {
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
        char[] password = clientCertificatePasswordField == null
                ? new char[0]
                : clientCertificatePasswordField.getPassword();
        try {
            PlccomMqttCertificateManagerDialog.show(frame, texts, locale,
                    createPkiStore(), DEFAULT_OWN_CERTIFICATE_ALIAS, password,
                    component -> theme(component));
        } catch (Throwable ex) {
            logError("log.operation.failed", "certificates", shortCause(ex));
            logErrorText(t("log.failure.details") + " " + detailedCause(ex));
        } finally {
            Arrays.fill(password, '\0');
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

    private void showEndpointAssistant() {
        EndpointParts initial = parseEndpointParts(trim(endpointField));
        JComboBox<String> transportCombo = combo(ENDPOINT_TRANSPORT_TCP, ENDPOINT_TRANSPORT_TLS,
                ENDPOINT_TRANSPORT_WS, ENDPOINT_TRANSPORT_WSS);
        JTextField hostField = textField(22, initial.host);
        JSpinner portSpinner = plainSpinner(initial.port, 1, 65535, 1);
        JTextField pathField = textField(22, initial.path);

        transportCombo.setSelectedItem(initial.transport);
        Runnable refreshPathState = () -> pathField.setEnabled(isWebSocketTransport(transportCombo.getSelectedItem()));
        transportCombo.addActionListener(e -> {
            int currentPort = number(portSpinner);
            if (currentPort == 1883 || currentPort == 8883 || currentPort == 8080 || currentPort == 8443) {
                portSpinner.setValue(Integer.valueOf(defaultEndpointPort(transportCombo.getSelectedItem())));
            }
            refreshPathState.run();
        });
        refreshPathState.run();

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(new EmptyBorder(8, 8, 8, 8));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        addEndpointAssistantRow(content, gbc, "dialog.endpoint.transport", transportCombo);
        addEndpointAssistantRow(content, gbc, "dialog.endpoint.host", hostField);
        addEndpointAssistantRow(content, gbc, "dialog.endpoint.port", portSpinner);
        addEndpointAssistantRow(content, gbc, "dialog.endpoint.path", pathField);
        theme(content);

        Object[] options = { t("dialog.endpoint.apply"), t("dialog.endpoint.cancel") };
        while (true) {
            int result = JOptionPane.showOptionDialog(frame, content, t("dialog.endpoint.title"),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (result != 0) {
                return;
            }
            String endpoint = buildEndpointFromParts(transportCombo.getSelectedItem(),
                    trim(hostField), number(portSpinner), trim(pathField));
            try {
                validateEndpoint(endpoint);
                endpointField.setText(endpoint);
                return;
            } catch (PlccomMqttException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), t("dialog.endpoint.invalidTitle"),
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void addEndpointAssistantRow(JPanel panel, GridBagConstraints gbc, String key, JComponent field) {
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 8, 10);
        JLabel label = new JLabel(t(key));
        label.setPreferredSize(new Dimension(150, CONTROL_HEIGHT));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 8, 0);
        panel.add(fixedToolField(300, field), gbc);
        gbc.gridy++;
    }

    private String endpointForConnect() throws PlccomMqttException {
        String endpoint = trim(endpointField);
        validateEndpoint(endpoint);
        return endpoint;
    }

    private String buildEndpointFromParts(Object transport, String host, int port, String path) {
        String scheme = endpointScheme(transport);
        StringBuilder builder = new StringBuilder();
        builder.append(scheme).append("://").append(hostForUri(host)).append(':').append(port);
        if (isWebSocketTransport(transport)) {
            builder.append(normalizedWebSocketPath(path));
        }
        return builder.toString();
    }

    private void validateEndpoint(String endpoint) throws PlccomMqttException {
        if (endpoint == null || endpoint.trim().length() == 0) {
            throw new PlccomMqttException(t("error.endpoint.required"));
        }
        URI uri;
        try {
            uri = new URI(endpoint.trim());
        } catch (URISyntaxException ex) {
            throw new PlccomMqttException(t("error.endpoint.syntax"), ex);
        }
        String scheme = uri.getScheme();
        if (!isSupportedEndpointScheme(scheme)) {
            throw new PlccomMqttException(format("error.endpoint.scheme", "mqtt, mqtts, ws, wss"));
        }
        if (uri.getUserInfo() != null && uri.getUserInfo().length() > 0) {
            throw new PlccomMqttException(t("error.endpoint.userInfo"));
        }
        if (uri.getHost() == null || uri.getHost().trim().length() == 0) {
            throw new PlccomMqttException(t("error.endpoint.host"));
        }
        if (uri.getPort() < 1 || uri.getPort() > 65535) {
            throw new PlccomMqttException(t("error.endpoint.port"));
        }
        if (uri.getRawQuery() != null || uri.getRawFragment() != null) {
            throw new PlccomMqttException(t("error.endpoint.query"));
        }
        String path = uri.getRawPath();
        boolean websocket = "ws".equalsIgnoreCase(scheme) || "wss".equalsIgnoreCase(scheme);
        if (!websocket && path != null && path.length() > 0 && !"/".equals(path)) {
            throw new PlccomMqttException(t("error.endpoint.tcpPath"));
        }
        if (websocket && path != null && path.indexOf(' ') >= 0) {
            throw new PlccomMqttException(t("error.endpoint.websocketPath"));
        }
    }

    private EndpointParts parseEndpointParts(String endpoint) {
        EndpointParts defaults = new EndpointParts(ENDPOINT_TRANSPORT_TCP, "127.0.0.1", 1883, "/mqtt");
        if (endpoint == null || endpoint.trim().length() == 0) {
            return defaults;
        }
        try {
            URI uri = new URI(endpoint.trim());
            if (!isSupportedEndpointScheme(uri.getScheme()) || uri.getHost() == null) {
                return defaults;
            }
            String transport = endpointTransport(uri.getScheme());
            int port = uri.getPort() > 0 ? uri.getPort() : defaultEndpointPort(transport);
            String path = uri.getPath();
            return new EndpointParts(transport, uri.getHost(), port,
                    path == null || path.length() == 0 ? "/mqtt" : path);
        } catch (RuntimeException | URISyntaxException ex) {
            return defaults;
        }
    }

    private static boolean isSupportedEndpointScheme(String scheme) {
        return "mqtt".equalsIgnoreCase(scheme)
                || "mqtts".equalsIgnoreCase(scheme)
                || "ws".equalsIgnoreCase(scheme)
                || "wss".equalsIgnoreCase(scheme);
    }

    private static String endpointScheme(Object transport) {
        if (ENDPOINT_TRANSPORT_TLS.equals(transport)) {
            return "mqtts";
        }
        if (ENDPOINT_TRANSPORT_WS.equals(transport)) {
            return "ws";
        }
        if (ENDPOINT_TRANSPORT_WSS.equals(transport)) {
            return "wss";
        }
        return "mqtt";
    }

    private static String endpointTransport(String scheme) {
        if ("mqtts".equalsIgnoreCase(scheme)) {
            return ENDPOINT_TRANSPORT_TLS;
        }
        if ("ws".equalsIgnoreCase(scheme)) {
            return ENDPOINT_TRANSPORT_WS;
        }
        if ("wss".equalsIgnoreCase(scheme)) {
            return ENDPOINT_TRANSPORT_WSS;
        }
        return ENDPOINT_TRANSPORT_TCP;
    }

    private static int defaultEndpointPort(Object transport) {
        if (ENDPOINT_TRANSPORT_TLS.equals(transport)) {
            return 8883;
        }
        if (ENDPOINT_TRANSPORT_WS.equals(transport)) {
            return 8080;
        }
        if (ENDPOINT_TRANSPORT_WSS.equals(transport)) {
            return 8443;
        }
        return 1883;
    }

    private static boolean isWebSocketTransport(Object transport) {
        return ENDPOINT_TRANSPORT_WS.equals(transport) || ENDPOINT_TRANSPORT_WSS.equals(transport);
    }

    private static String normalizedWebSocketPath(String path) {
        if (path == null || path.trim().length() == 0) {
            return "/mqtt";
        }
        String value = path.trim();
        return value.startsWith("/") ? value : "/" + value;
    }

    private static String hostForUri(String host) {
        if (host == null) {
            return "";
        }
        String value = host.trim();
        if (value.indexOf(':') >= 0 && !value.startsWith("[") && !value.endsWith("]")) {
            return "[" + value + "]";
        }
        return value;
    }

    private static String certificateFingerprint(X509Certificate certificate) {
        try {
            return PlccomMqttPkiStore.sha1Thumbprint(certificate);
        } catch (Exception ex) {
            return "unknown";
        }
    }

    private static boolean isSecureEndpoint(String endpoint) {
        String lower = endpoint.toLowerCase(Locale.ROOT);
        return lower.startsWith("mqtts://") || lower.startsWith("wss://");
    }

    private void applyProtocol(PlccomMqttClientBuilder builder) {
        int index = protocolCombo.getSelectedIndex();
        if (index == 1) {
            builder.mqtt5();
        } else if (index == 2) {
            builder.mqtt311();
        } else {
            builder.autoProtocol();
        }
    }

    private void applyLastWill(PlccomMqttClientBuilder builder) throws PlccomMqttException {
        if (!lastWillCheck.isSelected()) {
            return;
        }
        String topic = trimOrNull(lastWillTopicField);
        if (topic == null) {
            throw new PlccomMqttException(t("error.will.topic.required"));
        }
        MqttClientWillBuilder will = builder.will(topic, lastWillPayloadArea.getText())
                .payloadFormatUtf8()
                .retain(lastWillRetainCheck.isSelected());
        applyQos(will, lastWillQosCombo);
        int willDelay = number(lastWillDelaySpinner);
        if (willDelay > 0) {
            will.willDelaySeconds((long) willDelay);
        }
        int expiry = number(lastWillExpirySpinner);
        if (expiry > 0) {
            will.messageExpirySeconds((long) expiry);
        }
        String contentType = trimOrNull(lastWillContentTypeField);
        if (contentType != null) {
            will.contentType(contentType);
        }
        will.done();
    }

    private void applyCredentials(PlccomMqttClientBuilder builder) {
        String user = trimOrNull(usernameField);
        char[] passwordChars = passwordField.getPassword();
        try {
            if (user != null || passwordChars.length > 0) {
                // Passwords are converted only for the builder call and cleared
                // immediately afterwards. The UI field still owns the visible user
                // input, but no extra byte array is kept around longer than needed.
                byte[] passwordBytes = new String(passwordChars).getBytes(StandardCharsets.UTF_8);
                builder.usernamePassword(user, passwordChars.length == 0 ? null : passwordBytes);
                Arrays.fill(passwordBytes, (byte) 0);
            }
        } finally {
            Arrays.fill(passwordChars, '\0');
        }
    }

    private void applyQos(MqttClientPublishBuilder builder, JComboBox<String> combo) {
        int index = combo.getSelectedIndex();
        if (index == 1) {
            builder.qos1();
        } else if (index == 2) {
            builder.qos2();
        } else {
            builder.qos0();
        }
    }

    private void applyQos(MqttClientWillBuilder builder, JComboBox<String> combo) {
        int index = combo.getSelectedIndex();
        if (index == 1) {
            builder.qos1();
        } else if (index == 2) {
            builder.qos2();
        } else {
            builder.qos0();
        }
    }

    private void applyQos(MqttClientSubscribeBuilder builder, JComboBox<String> combo) {
        int index = combo.getSelectedIndex();
        if (index == 1) {
            builder.qos1();
        } else if (index == 2) {
            builder.qos2();
        } else {
            builder.qos0();
        }
    }

    private PlccomMqttRetainHandling retainHandling() {
        int index = retainHandlingCombo.getSelectedIndex();
        if (index == 1) {
            return PlccomMqttRetainHandling.SEND_AT_SUBSCRIBE_IF_NEW;
        }
        if (index == 2) {
            return PlccomMqttRetainHandling.DO_NOT_SEND;
        }
        return PlccomMqttRetainHandling.SEND_AT_SUBSCRIBE;
    }

    private void messageReceived(MqttMessage message) {
        SwingUtilities.invokeLater(() -> {
            messageModel.addRow(new Object[] {
                    TIME.format(new Date()),
                    message.topic(),
                    message.qos(),
                    Boolean.valueOf(message.retain()),
                    message.payloadAsString(),
                    message.contentType(),
                    message.subscriptionIdentifiers()
            });
            logSuccess("log.message", message.topic(), message.qos(), message.retain(),
                    message.payloadAsString());
        });
    }

    private void diagnostic(PlccomMqttDiagnosticEvent event) {
        logPanel.append(LogLevel.WARNING, formatDiagnostic(event));
    }

    private String formatDiagnostic(PlccomMqttDiagnosticEvent event) {
        StringBuilder builder = new StringBuilder();
        // Diagnostics are structured log events. Severity/category/code are stable
        // enough for operators to filter, while the optional fields explain which
        // MQTT operation, topic or client triggered the event.
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
        scheduleSettingsAutoSave();
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
        scheduleSettingsAutoSave();
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
        languageButton.setText(Locale.GERMAN.getLanguage().equals(locale.getLanguage()) ? "EN" : "DE");
        setStatusTextOnly(client != null && client.isConnected() ? "status.connected" : "status.ready");
        if (logPanel != null) {
            logPanel.translate();
        }
        updateActionStates();
        frame.revalidate();
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
        boolean connected = client != null && client.isConnected();
        if (connectButton != null) {
            connectButton.setEnabled(!busy && !connected);
            theme(connectButton);
        }
        if (disconnectButton != null) {
            disconnectButton.setEnabled(!busy && connected);
            theme(disconnectButton);
        }
        if (helpButton != null) {
            helpButton.setEnabled(true);
            theme(helpButton);
        }
        if (exitButton != null) {
            exitButton.setEnabled(true);
            theme(exitButton);
        }
        boolean useClientCertificate = clientCertificateCheck != null && clientCertificateCheck.isSelected();
        if (clientPkiOwnAliasField != null) {
            clientPkiOwnAliasField.setEnabled(useClientCertificate);
            theme(clientPkiOwnAliasField);
        }
        if (clientCertificatePasswordField != null) {
            clientCertificatePasswordField.setEnabled(useClientCertificate);
            theme(clientCertificatePasswordField);
        }
        setLastWillFieldsEnabled(true);
    }

    private void setLastWillFieldsEnabled(boolean enabled) {
        Component[] components = new Component[] {
                lastWillTopicField,
                lastWillPayloadArea,
                lastWillQosCombo,
                lastWillRetainCheck,
                lastWillDelaySpinner,
                lastWillExpirySpinner,
                lastWillContentTypeField
        };
        for (Component component : components) {
            if (component != null) {
                component.setEnabled(enabled);
                theme(component);
            }
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
        if (component instanceof JPanel) {
            JPanel panel = (JPanel) component;
            if (panel.getBorder() != null) {
                panel.setBorder(panel.getBorder());
            }
        } else if (component instanceof JTextComponent) {
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
            combo.setRenderer(new ThemedListRenderer());
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
        gbc.insets = new Insets(5, 5, 5, 5);
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

    private JPanel endpointInput(JButton assistantButton) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBorder(BorderFactory.createEmptyBorder());
        panel.add(fixedToolField(244, endpointField), BorderLayout.CENTER);
        panel.add(assistantButton, BorderLayout.EAST);
        setFixedSize(panel, TOOL_FIELD_WIDTH, CONTROL_HEIGHT);
        return panel;
    }
    private JPanel toolInput(JComponent field, Component... trailingComponents) {
        return toolInput(TOOL_FIELD_WIDTH, field, trailingComponents);
    }

    private JPanel toolInput(int width, JComponent field, Component... trailingComponents) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setBorder(BorderFactory.createEmptyBorder());
        panel.add(fixedToolField(width, field));
        for (Component trailingComponent : trailingComponents) {
            panel.add(javax.swing.Box.createHorizontalStrut(8));
            panel.add(trailingComponent);
        }
        return panel;
    }

    private JPanel optionRow(Component... components) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setBorder(BorderFactory.createEmptyBorder());
        for (int i = 0; i < components.length; i++) {
            if (i > 0) {
                panel.add(javax.swing.Box.createHorizontalStrut(12));
            }
            panel.add(components[i]);
        }
        return panel;
    }

    /**
     * Lays out a leading checkbox plus a trailing control as one fixed-width field
     * cell ({@link #TOOL_FIELD_WIDTH}). The checkbox keeps a fixed width so the
     * trailing control lands at the same position in every language, and the
     * trailing control fills the rest so its right edge aligns with all other
     * fields. Without the fixed checkbox width the localized checkbox label (e.g.
     * "aktiv" vs "active") would shift the trailing control between DE and EN.
     */
    /**
     * Lays out inline controls (e.g. two checkboxes, or a checkbox plus a button)
     * inside a fixed-width cell ({@link #TOOL_FIELD_WIDTH}). Fixing the width keeps
     * the form column's preferred width independent of the translated control
     * labels, so the neighbouring column does not shift between DE and EN.
     */
    private JPanel fixedInlineRow(Component... components) {
        JPanel inner = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        inner.setBorder(BorderFactory.createEmptyBorder());
        for (Component component : components) {
            inner.add(component);
        }
        setFixedSize(inner, INLINE_ROW_WIDTH, CONTROL_HEIGHT);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setBorder(BorderFactory.createEmptyBorder());
        panel.add(inner);
        return panel;
    }

    private JPanel optionFieldRow(int checkWidth, JCheckBox check, JComponent trailing) {
        JPanel inner = new JPanel(new BorderLayout(8, 0));
        inner.setBorder(BorderFactory.createEmptyBorder());
        inner.add(fixedToolField(checkWidth, check), BorderLayout.WEST);
        inner.add(trailing, BorderLayout.CENTER);
        setFixedSize(inner, TOOL_FIELD_WIDTH, CONTROL_HEIGHT);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setBorder(BorderFactory.createEmptyBorder());
        panel.add(inner);
        return panel;
    }

    private JComponent fixedToolField(int width, JComponent field) {
        JPanel holder = new JPanel(new BorderLayout());
        holder.setBorder(BorderFactory.createEmptyBorder());
        holder.add(field, BorderLayout.CENTER);
        setFixedSize(holder, width, CONTROL_HEIGHT);
        return holder;
    }

    private JScrollPane fixedToolArea(Component component, int height) {
        JScrollPane pane = scroll(component);
        pane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        setFixedSize(pane, TOOL_FIELD_WIDTH, height);
        return pane;
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

    private JScrollPane scroll(Component component) {
        return new JScrollPane(component);
    }

    private JPanel inlinePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setBorder(BorderFactory.createEmptyBorder());
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
                    PlccomMqttClientToolApp.class.getResource("/assets/PLCcom_720.png"));
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

    private static String trim(JTextComponent field) {
        return field.getText().trim();
    }

    private static String trimOrNull(JTextComponent field) {
        String value = trim(field);
        return value.length() == 0 ? null : value;
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
        return new File(new File(userDataDirectory(), "Client"),
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
        String version = PlccomMqttClientToolApp.class.getPackage().getImplementationVersion();
        return version == null ? "2.1.1" : version;
    }

    private void writeScreenshot(File file) throws IOException {
        BufferedImage image = new BufferedImage(frame.getWidth(), frame.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        frame.paint(image.getGraphics());
        ImageIO.write(image, "png", file);
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
            chooser.setSelectedFile(new File("plccom-mqtt-client-tool.log"));
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

    private final class ThemedTableRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, selected, hasFocus,
                    row, column);
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

    private final class ThemedListRenderer extends DefaultTableCellRenderer
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
        FILE, ADD, REMOVE, COPY, SAVE, TRASH, REFRESH, RESET
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

    private static final class EndpointParts {
        final String transport;
        final String host;
        final int port;
        final String path;

        EndpointParts(String transport, String host, int port, String path) {
            this.transport = transport;
            this.host = host;
            this.port = port;
            this.path = path;
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
