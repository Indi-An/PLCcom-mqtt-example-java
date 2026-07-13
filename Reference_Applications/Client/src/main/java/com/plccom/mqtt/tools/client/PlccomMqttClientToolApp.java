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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
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
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
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

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
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
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import com.plccom.mqtt.client.PlccomMqttEndpoint;
import com.plccom.mqtt.client.PlccomMqttException;
import com.plccom.mqtt.client.PlccomMqttProtocolVersion;
import com.plccom.mqtt.client.PlccomMqttPublishMessage;
import com.plccom.mqtt.client.PlccomMqttRetainHandling;
import com.plccom.mqtt.client.PlccomMqttTlsOptions;
import com.plccom.mqtt.core.VersionInfo;
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
    private static final String PAYLOAD_FORMAT_PLAINTEXT = "Plaintext";
    private static final String PAYLOAD_FORMAT_JSON = "JSON";
    private static final String PAYLOAD_FORMAT_BASE64 = "Base64";
    private static final String PAYLOAD_FORMAT_HEX = "Hex";
    private static final String PAYLOAD_JSON_TEMPLATE =
            "{\n  \"msg\": \"Hello from PLCcom Client\"\n}";
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
    // Fixed application salt (the ASCII bytes of "PLCcomMQTTTools1") for the PBKDF2
    // settings-key derivation. A constant salt is acceptable here because the derived
    // key only obfuscates locally stored secrets on this account; it is not a password
    // hash meant to resist offline cracking.
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

    // Shared infrastructure. Every network operation runs on this single-thread
    // executor so the Swing event dispatch thread never blocks; the maps remember
    // which resource key each widget shows so the whole UI can be re-translated
    // and re-themed live when the language or theme is switched.
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "PLCcom-MQTT-Client-Tool");
        thread.setDaemon(true);
        return thread;
    });
    private final List<MqttSubscribeResult> subscriptions = new ArrayList<MqttSubscribeResult>();
    private final Map<JLabel, String> labelKeys = new LinkedHashMap<JLabel, String>();
    private final Map<AbstractButton, String> buttonKeys = new LinkedHashMap<AbstractButton, String>();
    private final Map<JComponent, String> tooltipKeys = new LinkedHashMap<JComponent, String>();
    // Buttons that should be painted in the accent colour (the primary action of a tab).
    private final Set<AbstractButton> primaryButtons =
            java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<AbstractButton, Boolean>());
    private final List<String> tabKeys = new ArrayList<String>();
    // Fingerprints already offered for trust, so we prompt once per rejected certificate.
    private final Set<String> promptedRejectedCertificates = ConcurrentHashMap.newKeySet();

    // Mutable application state: current language, theme, status and the live client.
    private Locale locale = Locale.GERMAN;
    private ResourceBundle texts = ResourceBundle.getBundle(MESSAGE_BUNDLE, locale);
    private Palette palette = Palette.dark();
    private Status currentStatus = Status.NEUTRAL;
    // Written on the single background worker thread and read from the Swing
    // event dispatch thread (button states, status text), so it is volatile for
    // the reading thread to observe the latest value reliably.
    private volatile PlccomMqttClient client;
    private boolean settingsPersistenceEnabled = true;
    private boolean settingsSaved;
    private boolean settingsAutoSaveInstalled;
    private Timer settingsAutoSaveTimer;

    // Top-level window plus the header and footer widgets and the log panel.
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

    // Connection tab: license, endpoint, protocol, credentials, timeouts,
    // diagnostics, TLS/PKI and the optional Last Will message.
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
    // Mirror of the diagnostic-level combo for the tool's own log filter. Kept in
    // a volatile field because log helpers run on SDK worker threads, which must
    // not touch the Swing combo directly.
    private volatile PlccomMqttDiagnosticLevel uiLogLevel = PlccomMqttDiagnosticLevel.INFO;
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

    // Publish tab: topic, payload and QoS plus the MQTT 5 message properties.
    private JTextField publishTopicField;
    private JTextArea publishPayloadArea;
    private JComboBox<String> publishQosCombo;
    private JComboBox<String> publishFormatCombo;
    private String currentPayloadFormat = PAYLOAD_FORMAT_PLAINTEXT;
    private JCheckBox retainCheck;
    private JTextField contentTypeField;
    private JSpinner messageExpirySpinner;
    private JTextField responseTopicPropertyField;
    private JTextField correlationDataField;
    private JTextField userPropertyNameField;
    private JTextField userPropertyValueField;

    // Request/response tab: publishes to a request topic and waits for the reply.
    private JTextField requestTopicField;
    private JTextField requestResponseTopicField;
    private JTextArea requestPayloadArea;

    // Subscribe tab: topic filter, QoS and the MQTT 5 subscription options.
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
                if (startup.english) {
                    // Apply the language before show() so the initial log line and
                    // all texts render in the selected language.
                    app.locale = Locale.ENGLISH;
                    app.texts = ResourceBundle.getBundle(MESSAGE_BUNDLE, Locale.ENGLISH);
                }
                app.show();
                if (startup.light) {
                    app.setLightTheme();
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

    /**
     * Builds the window, restores the saved settings and shows the application.
     * Runs on the Swing event dispatch thread.
     */
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

    /**
     * Assembles the whole window: the header on top, the license bar, the tabbed
     * configuration area and the log panel in the centre, and the action footer at
     * the bottom. This only lays out the widgets; their texts and colours are
     * applied afterwards by {@link #translate()} and {@link #applyTheme()}.
     */
    private void buildUi() {
        frame = new JFrame();
        frame.setIconImages(loadAppIcons());
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

    /**
     * Builds the top header: the PLCcom logo on the left, the application title and
     * subtitle in the middle, and the status pill (Ready/Connected/...) on the right.
     */
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

    /**
     * Builds the bottom footer: the diagnostic-level selector on the left and the
     * global action buttons (theme, language, certificates, help, exit) on the right.
     */
    private JPanel createFooterActions() {
        JPanel footer = new JPanel(new BorderLayout(8, 0));
        footer.setName("footer");
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, palette.border),
                new EmptyBorder(8, 16, 10, 16)));

        // Diagnostic level lives in the footer (left side), mirroring the Broker
        // tool, so the Connection tab stays focused on connection settings. It
        // gets its own docked area so its label never crowds the action buttons.
        JPanel diagnostics = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        diagnostics.setOpaque(false);
        JLabel diagnosticLevelLabel = label("connect.diagnosticLevel");
        diagnosticLevelLabel.setFont(diagnosticLevelLabel.getFont().deriveFont(Font.PLAIN, 12f));
        // A few px of trailing slack so the last glyph is never clipped when the
        // label is painted off-screen for screenshots.
        diagnosticLevelLabel.setBorder(new EmptyBorder(0, 0, 0, 6));
        diagnostics.add(diagnosticLevelLabel);
        diagnostics.add(diagnosticLevelCombo);
        footer.add(diagnostics, BorderLayout.WEST);

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

    /**
     * Builds the license bar shown above the tabs: the user and serial fields plus a
     * hint linking to the download page where a free trial license can be created.
     */
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

        JPanel hintRow = new JPanel();
        hintRow.setLayout(new BoxLayout(hintRow, BoxLayout.X_AXIS));
        hintRow.setOpaque(false);
        hintRow.add(label("license.hint"));
        hintRow.add(javax.swing.Box.createHorizontalStrut(6));
        hintRow.add(createDownloadLink());
        hintRow.add(javax.swing.Box.createHorizontalGlue());
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        panel.add(hintRow, gbc);
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

    /**
     * Creates the five configuration tabs (Connection, Publish, Subscribe,
     * Request/response, Messages) and keeps their titles translatable.
     */
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

    /** Adds a tab and remembers its resource key so the title can be re-translated. */
    private void addToolTab(String key, Component content) {
        tabKeys.add(key);
        tabs.addTab(t(key), content);
    }

    /**
     * Re-applies the (possibly translated) tab titles and paints a custom header
     * label for each tab, drawing an accent underline under the selected one. Also
     * runs on every selection change so the highlight follows the user.
     */
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

    /**
     * Builds the Connection tab. Every widget is created first, then arranged in two
     * side-by-side forms: the left column holds the connection basics (endpoint,
     * client id, protocol, options, client certificate and Last Will), the right
     * column holds credentials and timeouts. Connect and Disconnect sit in the
     * action bar along the bottom.
     */
    private Component createConnectionPanel() {
        // 1) Create every widget on this tab first; the layout is assembled afterwards.
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
        // Push level changes to a live client immediately, so switching the
        // level takes effect at once instead of only on the next connect.
        diagnosticLevelCombo.addActionListener(event -> applyRuntimeDiagnosticLevel());
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

        // 2) Assemble the layout: two form columns inside a GridBag, action bar below.
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel columns = new JPanel(new GridBagLayout());
        columns.setBorder(BorderFactory.createEmptyBorder());
        GridBagConstraints columnGbc = new GridBagConstraints();
        columnGbc.gridy = 0;
        columnGbc.fill = GridBagConstraints.BOTH;
        columnGbc.weighty = 1;

        // Left column: connection basics (endpoint, client id, protocol, options,
        // client certificate and the Last Will toggle).
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

        // Right column: credentials and timeouts.
        JPanel optionsPanel = formPanel();
        GridBagConstraints optionsGbc = formGbc();
        int optionsRow = 0;
        addRow(optionsPanel, optionsGbc, optionsRow++, "connect.username", toolInput(usernameField));
        addRow(optionsPanel, optionsGbc, optionsRow++, "connect.password", toolInput(passwordField));
        addRow(optionsPanel, optionsGbc, optionsRow++, "connect.keepAlive", toolInput(keepAliveSpinner));
        addRow(optionsPanel, optionsGbc, optionsRow++, "connect.topicAlias", toolInput(topicAliasSpinner));
        addRow(optionsPanel, optionsGbc, optionsRow++, "connect.connectTimeout", toolInput(connectTimeoutSpinner));
        addRow(optionsPanel, optionsGbc, optionsRow++, "connect.operationTimeout", toolInput(operationTimeoutSpinner));
        addFormBottomFiller(optionsPanel, optionsGbc, optionsRow);

        // Place the two columns with a ~56/44 width split (basics slightly wider).
        columnGbc.gridx = 0;
        columnGbc.weightx = 0.56;
        columnGbc.insets = new Insets(0, 0, 0, 5);
        columns.add(connectionPanel, columnGbc);

        columnGbc.gridx = 1;
        columnGbc.weightx = 0.44;
        columnGbc.insets = new Insets(0, 5, 0, 0);
        columns.add(optionsPanel, columnGbc);
        panel.add(columns, BorderLayout.CENTER);

        // 3) Action bar: Connect is the primary (accent) action, Disconnect next to it.
        connectButton = primaryButton("button.connect", this::connect);
        disconnectButton = button("button.disconnect", this::disconnect);
        panel.add(actionBar(connectButton, disconnectButton), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Builds the Last Will editor used inside {@link #showLastWillDialog()}: the
     * message basics (topic, payload, QoS, retain) on the left and the MQTT 5 will
     * properties (delay, message expiry, content type) on the right.
     */
    private JPanel createLastWillPanel() {
        // A bordered card with a bold title on top and two side-by-side forms below.
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

        // Left column: the Last Will message basics (topic, payload, QoS, retain).
        JPanel messagePanel = formPanel();
        messagePanel.setBorder(BorderFactory.createEmptyBorder());
        GridBagConstraints messageGbc = formGbc();
        int messageRow = 0;
        addRow(messagePanel, messageGbc, messageRow++, "will.topic", toolInput(lastWillTopicField));
        addRow(messagePanel, messageGbc, messageRow++, "will.payload",
                fixedToolArea(lastWillPayloadArea, 74));
        addRow(messagePanel, messageGbc, messageRow++, "will.qos", toolInput(lastWillQosCombo));
        addRow(messagePanel, messageGbc, messageRow++, "will.flags", toolInput(lastWillRetainCheck));

        // Right column: the MQTT 5 will properties (publish delay, message expiry,
        // content type). MQTT 3.1.1 brokers simply ignore these.
        JPanel mqtt5Panel = formPanel();
        mqtt5Panel.setBorder(BorderFactory.createEmptyBorder());
        GridBagConstraints mqtt5Gbc = formGbc();
        int mqtt5Row = 0;
        addRow(mqtt5Panel, mqtt5Gbc, mqtt5Row++, "will.delay", toolInput(lastWillDelaySpinner));
        addRow(mqtt5Panel, mqtt5Gbc, mqtt5Row++, "will.expiry", toolInput(lastWillExpirySpinner));
        addRow(mqtt5Panel, mqtt5Gbc, mqtt5Row++, "will.contentType", toolInput(lastWillContentTypeField));

        // Give the message column a bit more width than the properties column (~58/42).
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

    /** Opens the Last Will editor in a modal dialog anchored to the main window. */
    private void showLastWillDialog() {
        // Wrap the shared Last Will panel in a modal dialog with a single Close button.
        JDialog dialog = new JDialog(frame, t("will.dialog.title"), true);
        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBorder(new EmptyBorder(12, 12, 12, 12));
        content.add(createLastWillPanel(), BorderLayout.CENTER);

        JButton closeButton = button("button.close", () -> dialog.dispose());
        JPanel footer = actionBar(closeButton);
        content.add(footer, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        theme(content); // match the current theme
        // The Last Will fields are only edited here, so they are always enabled while
        // the dialog is open regardless of the main window's connection state.
        setLastWillFieldsEnabled(true);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(760, 360));
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    /**
     * Builds the Publish tab: the message itself (topic, format, payload, QoS,
     * retain) on the left and the MQTT 5 publish properties on the right, with
     * Publish and "Delete retained" in the action bar.
     */
    private Component createPublishPanel() {
        // Create the Publish-tab widgets first; the layout follows below.
        publishTopicField = textField(32, "plccom/tool/client/demo");
        publishPayloadArea = new JTextArea("Hello from PLCcom MQTT Client Tool", 5, 48);
        publishPayloadArea.setLineWrap(true);
        publishPayloadArea.setWrapStyleWord(true);
        publishQosCombo = combo("QoS 0", "QoS 1", "QoS 2");
        publishFormatCombo = combo(PAYLOAD_FORMAT_PLAINTEXT, PAYLOAD_FORMAT_JSON,
                PAYLOAD_FORMAT_BASE64, PAYLOAD_FORMAT_HEX);
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

        // Left column: the message itself (topic, format, payload, QoS, retain).
        JPanel messagePanel = formPanel();
        GridBagConstraints messageGbc = formGbc();
        int messageRow = 0;
        addRow(messagePanel, messageGbc, messageRow++, "publish.topic", toolInput(publishTopicField));
        addRow(messagePanel, messageGbc, messageRow++, "publish.format", toolInput(publishFormatCombo));
        addRow(messagePanel, messageGbc, messageRow++, "publish.payload",
                fixedToolArea(publishPayloadArea, PAYLOAD_AREA_HEIGHT));
        addRow(messagePanel, messageGbc, messageRow++, "publish.qos", toolInput(publishQosCombo));
        addRow(messagePanel, messageGbc, messageRow++, "publish.flags", toolInput(retainCheck));
        addFormBottomFiller(messagePanel, messageGbc, messageRow);

        // Right column: the MQTT 5 publish properties (content type, message expiry,
        // response topic, correlation data and one user property).
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

        // Action bar: Publish (primary) plus "Delete retained" (an empty retained publish).
        panel.add(actionBar(
                primaryButton("button.publish", this::publish),
                button("button.deleteRetained", this::deleteRetained)), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Builds the Request/response tab, which publishes a request and waits for a
     * reply on the response topic. The left form holds topic, response topic and
     * payload; the right card explains the round-trip.
     */
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

        // Left column: the request itself (request topic, response topic, payload).
        JPanel requestForm = formPanel();
        GridBagConstraints requestGbc = formGbc();
        int requestRow = 0;
        addRow(requestForm, requestGbc, requestRow++, "request.topic", toolInput(requestTopicField));
        addRow(requestForm, requestGbc, requestRow++, "request.responseTopic",
                toolInput(requestResponseTopicField));
        addRow(requestForm, requestGbc, requestRow++, "request.payload",
                fixedToolArea(requestPayloadArea, PAYLOAD_AREA_HEIGHT));
        addFormBottomFiller(requestForm, requestGbc, requestRow);

        // Right column: a short explanation card describing the request/response round-trip.
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

    /**
     * Builds the Subscribe tab: the subscription basics (topic filter, QoS, shared
     * group, receive timeout) on the left and the MQTT 5 subscription options
     * (no-local, retain-as-published, retain handling, subscription identifier) on
     * the right. Subscribe, "Receive once" and "Unsubscribe all" sit at the bottom.
     */
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

        // Left column: subscription basics (topic filter, QoS, shared group, receive timeout).
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

        // Right column: the MQTT 5 subscription options (no-local, retain-as-published,
        // retain handling, subscription identifier).
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

        // Action bar: Subscribe (primary), plus one-shot receive and unsubscribe-all.
        panel.add(actionBar(
                primaryButton("button.subscribe", this::subscribe),
                button("button.receiveOnce", this::receiveOnce),
                button("button.unsubscribeAll", this::unsubscribeAll)), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Builds the Messages tab: a read-only table listing every received message
     * (time, topic, QoS, retain, payload, content type and subscription ids) with a
     * button to clear it.
     */
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

    /** Wraps the shared {@link LogPanel} in a fixed-height container below the tabs. */
    private Component createLogPanel() {
        logPanel = new LogPanel();
        JPanel outer = new JPanel(new BorderLayout(0, 0));
        outer.setPreferredSize(new Dimension(300, 210));
        outer.add(logPanel, BorderLayout.CENTER);
        return outer;
    }

    /**
     * Connects to the broker using the settings currently shown in the UI. Runs on
     * the background worker thread; any failure (including an invalid license) is
     * reported in the tool log.
     */
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

    /** Closes the active client and returns the tool to its idle state. */
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

    /**
     * Publishes the message from the Publish tab: encodes the payload for the chosen
     * format, applies QoS, retain and the MQTT 5 properties, then sends it. Runs on
     * the worker thread.
     */
    private void publish() {
        runAsync("publish", () -> {
            PlccomMqttClient current = requireConnectedClient();
            String payloadFormat = (String) publishFormatCombo.getSelectedItem();
            byte[] payload = encodePublishPayloadOrThrow(payloadFormat, publishPayloadArea.getText());
            MqttClientPublishBuilder builder = current.publish(trim(publishTopicField), payload);
            applyQos(builder, publishQosCombo);

            // Retain stores the last value for a topic on the broker. Message
            // Expiry and the response/correlation fields are MQTT 5 metadata; MQTT
            // 3.1.1 brokers simply do not negotiate those features.
            builder.retain(retainCheck.isSelected());
            if (isTextPayloadFormat(payloadFormat)) {
                // Plaintext and JSON are UTF-8 text; Base64/Hex carry raw bytes.
                builder.payloadFormatUtf8();
            }
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

    /**
     * Encodes the payload for the chosen format, translating any decoding error into
     * a {@link PlccomMqttException} with a readable message for the log.
     */
    private byte[] encodePublishPayloadOrThrow(String payloadFormat, String text)
            throws PlccomMqttException {
        try {
            return encodePublishPayload(payloadFormat, text);
        } catch (RuntimeException ex) {
            throw new PlccomMqttException(format("error.payload.format", payloadFormat), ex);
        }
    }

    /**
     * Reacts to a change of the payload-format selector by converting the visible
     * text into the newly chosen format, so the editor always shows exactly the
     * bytes that will be published.
     */
    private void onPublishFormatChanged() {
        String newFormat = (String) publishFormatCombo.getSelectedItem();
        if (newFormat == null || newFormat.equals(currentPayloadFormat)) {
            return;
        }
        // Convert the visible payload into the newly selected format, so the editor
        // always shows exactly what will be sent. The current text is decoded to raw
        // bytes using the previous format, then re-rendered in the new format.
        String text = publishPayloadArea.getText();
        if (text.trim().isEmpty()) {
            publishPayloadArea.setText(
                    PAYLOAD_FORMAT_JSON.equals(newFormat) ? PAYLOAD_JSON_TEMPLATE : "");
        } else {
            byte[] bytes = decodePayloadBytesLenient(currentPayloadFormat, text);
            publishPayloadArea.setText(encodePayloadText(newFormat, bytes));
        }
        currentPayloadFormat = newFormat;
    }

    /**
     * Decodes editor text into raw bytes using the given format, never throwing:
     * invalid Base64/Hex falls back to the UTF-8 bytes of the raw text so switching
     * formats can never lose what the user typed. Used only for the on-screen
     * conversion; the actual publish uses the strict {@link #encodePublishPayload}.
     */
    private static byte[] decodePayloadBytesLenient(String format, String text) {
        try {
            if (PAYLOAD_FORMAT_BASE64.equals(format)) {
                return Base64.getDecoder().decode(collapseWhitespace(text));
            }
            if (PAYLOAD_FORMAT_HEX.equals(format)) {
                return decodeHex(text);
            }
        } catch (RuntimeException ex) {
            // Malformed Base64/Hex: keep the raw text so nothing is lost.
        }
        return text.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Renders raw bytes as editor text in the given format. JSON keeps an existing
     * JSON object/array unchanged and otherwise wraps the text into a small
     * {@code {"msg": ...}} object. Package-private for {@code ClientToolPayloadFormatTest}.
     */
    static String encodePayloadText(String format, byte[] bytes) {
        if (PAYLOAD_FORMAT_BASE64.equals(format)) {
            return Base64.getEncoder().encodeToString(bytes);
        }
        if (PAYLOAD_FORMAT_HEX.equals(format)) {
            return toHex(bytes);
        }
        if (PAYLOAD_FORMAT_JSON.equals(format)) {
            String text = new String(bytes, StandardCharsets.UTF_8);
            if (looksLikeJsonDocument(text)) {
                return text;
            }
            JsonObject wrapper = new JsonObject();
            wrapper.addProperty("msg", text);
            return new GsonBuilder().setPrettyPrinting().create().toJson(wrapper);
        }
        return new String(bytes, StandardCharsets.UTF_8); // Plaintext
    }

    /** Quick check whether the text is a JSON object or array, so JSON stays verbatim. */
    private static boolean looksLikeJsonDocument(String text) {
        String trimmed = text.trim();
        if (trimmed.isEmpty() || !(trimmed.charAt(0) == '{' || trimmed.charAt(0) == '[')) {
            return false;
        }
        try {
            JsonElement element = JsonParser.parseString(trimmed);
            return element.isJsonObject() || element.isJsonArray();
        } catch (RuntimeException ex) {
            return false;
        }
    }

    /** Uppercase hex string, two digits per byte, with no separators. */
    private static String toHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            hex.append(Character.forDigit((value >> 4) & 0xF, 16));
            hex.append(Character.forDigit(value & 0xF, 16));
        }
        return hex.toString().toUpperCase(Locale.ROOT);
    }

    /**
     * Converts the payload editor text into the bytes to publish, according to the
     * selected payload format. Plaintext and JSON are sent verbatim as UTF-8 (JSON is
     * validated first); Base64 and Hex are decoded into raw bytes. Package-private for
     * {@code ClientToolPayloadFormatTest}.
     */
    static byte[] encodePublishPayload(String payloadFormat, String text) {
        if (PAYLOAD_FORMAT_BASE64.equals(payloadFormat)) {
            return Base64.getDecoder().decode(collapseWhitespace(text));
        }
        if (PAYLOAD_FORMAT_HEX.equals(payloadFormat)) {
            return decodeHex(text);
        }
        if (PAYLOAD_FORMAT_JSON.equals(payloadFormat)) {
            JsonParser.parseString(text); // throws if the text is not well-formed JSON
            return text.getBytes(StandardCharsets.UTF_8);
        }
        return text.getBytes(StandardCharsets.UTF_8); // Plaintext
    }

    /** True for the UTF-8 text formats (Plaintext, JSON), false for Base64/Hex. */
    static boolean isTextPayloadFormat(String payloadFormat) {
        return PAYLOAD_FORMAT_PLAINTEXT.equals(payloadFormat)
                || PAYLOAD_FORMAT_JSON.equals(payloadFormat);
    }

    /** Removes all whitespace so Base64/Hex pasted with line breaks still decodes. */
    private static String collapseWhitespace(String text) {
        return text == null ? "" : text.replaceAll("\\s", "");
    }

    /** Parses a hex string into bytes, rejecting odd length or non-hex characters. */
    private static byte[] decodeHex(String text) {
        String hex = collapseWhitespace(text);
        if ((hex.length() & 1) == 1) {
            throw new IllegalArgumentException("Hex payload must have an even number of digits");
        }
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            int high = Character.digit(hex.charAt(i * 2), 16);
            int low = Character.digit(hex.charAt(i * 2 + 1), 16);
            if (high < 0 || low < 0) {
                throw new IllegalArgumentException("Hex payload contains a non-hex character");
            }
            bytes[i] = (byte) ((high << 4) | low);
        }
        return bytes;
    }

    /** Clears the retained message on the publish topic via an empty retained publish. */
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

    /**
     * Runs an MQTT 5 request/response round-trip from the Request tab and shows the
     * reply in the log and the Messages tab.
     */
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

    /**
     * Subscribes to the topic filter from the Subscribe tab with the chosen QoS and
     * MQTT 5 options, and remembers the subscription so it can be undone later.
     */
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

    /**
     * Waits once (up to the configured timeout) for the next message on the active
     * subscriptions, without installing a permanent callback.
     */
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

    /** Unsubscribes from every active subscription created on the Subscribe tab. */
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

    /** Returns the live client, or throws with a readable message if not connected. */
    private PlccomMqttClient requireConnectedClient() throws PlccomMqttException {
        PlccomMqttClient current = client;
        if (current == null || !current.isConnected()) {
            throw new PlccomMqttException(t("error.notConnected"));
        }
        return current;
    }

    /**
     * Returns the result when the operation succeeded, otherwise throws a
     * {@link PlccomMqttException} built from its quality and localized message.
     */
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

    /**
     * Cleans up the current client without surfacing errors: undoes subscriptions,
     * disconnects if needed and closes it. Used on reconnect, disconnect and shutdown.
     */
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

    /** Best-effort unsubscribe of all tracked subscriptions, logging any failure only. */
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

    /**
     * Releases everything on window close: stops the auto-save timer, persists the
     * settings once, closes the client and shuts the worker thread down.
     */
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

    /** Restores language, theme and all field values from the per-user settings file. */
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

    /** Persists language, theme and all field values to the per-user settings file. */
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

    /** Applies every saved field value from the given settings back into the UI widgets. */
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
        String maxLinesValue = settings.getProperty("log.maxLines");
        if (maxLinesValue != null && logPanel != null) {
            try {
                logPanel.setMaxLines(Integer.parseInt(maxLinesValue.trim()));
            } catch (NumberFormatException ignored) {
                // Keep the default maximum on an obsolete or edited value.
            }
        }
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
        setCombo(settings, "publish.format", publishFormatCombo);
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

    /** Writes every field value into the given settings; sensitive fields are encrypted. */
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
        if (logPanel != null) {
            settings.setProperty("log.maxLines", String.valueOf(logPanel.getMaxLines()));
        }
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
        putCombo(settings, "publish.format", publishFormatCombo);
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

    /**
     * Runs a button action on the background worker thread, guarding against
     * overlapping operations and funnelling every failure into the log while keeping
     * the window responsive. Also drives the status pill (busy/ready/connected/error).
     */
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

    /** Pushes the currently selected diagnostic level to the live client, if any. */
    private void applyRuntimeDiagnosticLevel() {
        // The selected level drives both consumers: the tool's own log filter
        // (always, even without a connection) and the live SDK client.
        uiLogLevel = selectedDiagnosticLevel(diagnosticLevelCombo);
        PlccomMqttClient current = client;
        if (current != null) {
            current.setDiagnosticLevel(uiLogLevel);
        }
    }

    /** Maps the diagnostic-level combo selection to a {@link PlccomMqttDiagnosticLevel}. */
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

    /**
     * Configures TLS on the builder when the endpoint is secure: sets the PKI store
     * and host-name validation, and adds the client certificate when mutual TLS is
     * enabled. The certificate password is wiped from memory afterwards.
     */
    private void applyClientTls(PlccomMqttClientBuilder builder) throws Exception {
        String endpointText = trim(endpointField);
        if (!isSecureEndpoint(endpointText)) {
            return;
        }
        PlccomMqttEndpoint endpoint = PlccomMqttEndpoint.parse(endpointText);
        char[] password = clientCertificatePasswordField.getPassword();
        try {
            PlccomMqttPkiStore pkiStore = createPkiStore();
            PlccomMqttTlsOptions.Builder tls = PlccomMqttTlsOptions.builder()
                    .enabledProtocols("TLSv1.3", "TLSv1.2")
                    .pkiOptions(PlccomMqttPkiOptions.builder()
                            .pkiStore(pkiStore)
                            .validateRemoteCertificateHostName(true)
                            .targetHostName(endpoint.getHost())
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

    /** Human-readable list of keystore aliases (with key/certificate tag) for the log. */
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

    /** Copies the bundled help website to a per-user folder and opens it in the browser. */
    private void openHelp() {
        try {
            File index = prepareHelpWebsite();
            openInBrowser(index);
            logInfo("log.help.opened", index.getAbsolutePath());
        } catch (Exception ex) {
            logError("log.help.failed", shortCause(ex));
        }
    }

    /**
     * Installs the debounced auto-save: a short one-shot timer writes the settings a
     * moment after the last change, and every editable field is wired to restart it.
     * The payload-format listener is attached last so restoring a saved format does
     * not convert the just-loaded payload.
     */
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
                publishFormatCombo, subscribeQosCombo, retainHandlingCombo);
        // Sync the tracked payload format with the loaded value, then attach the
        // conversion listener after settings load, so restoring a saved format does
        // not convert (or overwrite) the saved payload.
        currentPayloadFormat = (String) publishFormatCombo.getSelectedItem();
        publishFormatCombo.addActionListener(event -> onPublishFormatChanged());
        bindButtonAutoSave(cleanStartCheck, reconnectCheck, clientCertificateCheck,
                lastWillCheck, lastWillRetainCheck, retainCheck, sharedSubscriptionCheck,
                noLocalCheck, retainAsPublishedCheck, subscriptionIdentifierCheck);
        bindSpinnerAutoSave(keepAliveSpinner, topicAliasSpinner, connectTimeoutSpinner,
                operationTimeoutSpinner, lastWillDelaySpinner,
                lastWillExpirySpinner, messageExpirySpinner, subscriptionIdentifierSpinner,
                receiveTimeoutSpinner);
    }

    /** Restarts the auto-save timer whenever any of the given text components change. */
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

    /** Restarts the auto-save timer whenever any of the given combo boxes change. */
    private void bindComboAutoSave(JComboBox<?>... components) {
        for (JComboBox<?> component : components) {
            if (component != null) {
                component.addActionListener(event -> scheduleSettingsAutoSave());
            }
        }
    }

    /** Restarts the auto-save timer whenever any of the given checkboxes toggle. */
    private void bindButtonAutoSave(AbstractButton... components) {
        for (AbstractButton component : components) {
            if (component != null) {
                component.addActionListener(event -> scheduleSettingsAutoSave());
            }
        }
    }

    /** Restarts the auto-save timer whenever any of the given spinners change. */
    private void bindSpinnerAutoSave(JSpinner... components) {
        for (JSpinner component : components) {
            if (component != null) {
                component.addChangeListener(event -> scheduleSettingsAutoSave());
            }
        }
    }

    /** (Re)starts the debounce timer so a burst of edits is saved once, shortly after. */
    private void scheduleSettingsAutoSave() {
        if (!settingsPersistenceEnabled || !settingsAutoSaveInstalled || settingsAutoSaveTimer == null) {
            return;
        }
        settingsAutoSaveTimer.restart();
    }

    /**
     * Extracts the localized help page and stylesheet from the jar into a per-version
     * folder under the user profile and returns the index file to open.
     */
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

    /** Copies a classpath resource to a file, failing loudly if the resource is missing. */
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

    /**
     * Builds the clickable download-page link shown in the license bar. Its text
     * is the localized URL (from {@code license.downloadUrl}), it is underlined
     * and coloured like a hyperlink, and clicking it opens the page in the
     * default browser.
     */
    private JLabel createDownloadLink() {
        JLabel link = label("license.downloadUrl");
        link.setName("licenseLink");
        java.util.Map<java.awt.font.TextAttribute, Object> attributes =
                new java.util.HashMap<java.awt.font.TextAttribute, Object>(link.getFont().getAttributes());
        attributes.put(java.awt.font.TextAttribute.UNDERLINE, java.awt.font.TextAttribute.UNDERLINE_ON);
        link.setFont(link.getFont().deriveFont(attributes));
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                browseUrl(t("license.downloadUrl"));
            }
        });
        return link;
    }

    /** Opens a URL in the default browser, falling back to printing it in the log. */
    private void browseUrl(String url) {
        try {
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new java.net.URI(url));
                return;
            }
        } catch (Exception ex) {
            // Fall through: show the URL in the (selectable) log so it can be copied.
        }
        logInfoText(url);
    }

    /** Opens a local file in the browser, falling back to an OS-specific launcher. */
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

    /** Last-resort browser launch via the platform's file/URL handler. */
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

    /** Opens the shared PLCcom certificate manager for this tool's per-user PKI store. */
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

    /** Creates the PKI folder structure on startup so TLS has somewhere to store keys. */
    private void ensureConfiguredPkiDirectories() {
        try {
            createPkiStore();
        } catch (IOException | RuntimeException ex) {
            logWarning("log.pki.ensure.failed", shortCause(ex));
        }
    }

    /**
     * Creates the per-user PKI store and wires its event callback so a newly rejected
     * remote certificate triggers the trust prompt.
     */
    private PlccomMqttPkiStore createPkiStore() throws IOException {
        final PlccomMqttPkiStore[] storeRef = new PlccomMqttPkiStore[1];
        PlccomMqttPkiStore pkiStore = new PlccomMqttPkiStore(Paths.get(DEFAULT_PKI_FOLDER),
                event -> handlePkiStoreEvent(storeRef[0], event));
        storeRef[0] = pkiStore;
        pkiStore.ensureDirectories();
        return pkiStore;
    }

    /**
     * Reacts to PKI store events: when a remote certificate is stored as rejected,
     * prompts the user once per fingerprint to trust it. A freshly written rejected
     * file is treated as a new onboarding attempt even within the same run.
     */
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

    /**
     * Shows the details of an untrusted remote certificate and, on confirmation,
     * moves it from the rejected to the trusted store.
     */
    private void showRejectedCertificateDialog(PlccomMqttPkiStore pkiStore,
            PlccomMqttPkiStoreEvent event, String fingerprint) {
        X509Certificate certificate = event.getCertificate();
        logWarning("log.pki.rejected", fingerprint, event.getPath());

        // Show the full certificate identity (subject, issuer, serial, validity,
        // fingerprint, file) as a read-only, word-wrapped text block so the user can
        // decide whether to trust it.
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

        // Two explicit choices: trust the certificate, or keep it rejected. The
        // warning icon signals that trusting is a security decision.
        Object[] options = {
                t("dialog.certificate.trust"),
                t("dialog.certificate.keepRejected")
        };
        int choice = JOptionPane.showOptionDialog(frame, new JScrollPane(details),
                t("dialog.certificate.title"), JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE, null, options, options[0]);
        if (choice != JOptionPane.YES_OPTION) {
            // Kept rejected: just note it in the log; the next connect will prompt again.
            logWarning("log.pki.rejectedKept", fingerprint, event.getPath());
            return;
        }
        // Trusted: move the file from the rejected to the trusted store and tell the
        // user to retry the connection. Any failure to move it is reported too.
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

    /**
     * Opens the endpoint assistant: a small dialog that composes a valid broker URL
     * from transport, host, port and (for WebSocket) path, and writes it back into
     * the endpoint field once it validates.
     */
    private void showEndpointAssistant() {
        // Pre-fill the four editors from whatever is currently in the endpoint field,
        // so the assistant opens on the user's existing value instead of a blank form.
        EndpointParts initial = parseEndpointParts(trim(endpointField));
        JComboBox<String> transportCombo = combo(ENDPOINT_TRANSPORT_TCP, ENDPOINT_TRANSPORT_TLS,
                ENDPOINT_TRANSPORT_WS, ENDPOINT_TRANSPORT_WSS);
        JTextField hostField = textField(22, initial.host);
        JSpinner portSpinner = plainSpinner(initial.port, 1, 65535, 1);
        JTextField pathField = textField(22, initial.path);

        transportCombo.setSelectedItem(initial.transport);
        // The path field only makes sense for WebSocket transports; keep it enabled
        // only for those.
        Runnable refreshPathState = () -> pathField.setEnabled(isWebSocketTransport(transportCombo.getSelectedItem()));
        transportCombo.addActionListener(e -> {
            // When the user switches transport and the port is still one of the well
            // known defaults, follow it to the new transport's default port. A custom
            // port is left untouched so we never overwrite a deliberate choice.
            int currentPort = number(portSpinner);
            if (currentPort == 1883 || currentPort == 8883 || currentPort == 8080 || currentPort == 8443) {
                portSpinner.setValue(Integer.valueOf(defaultEndpointPort(transportCombo.getSelectedItem())));
            }
            refreshPathState.run();
        });
        refreshPathState.run();

        // Lay out the four rows (transport, host, port, path) in a simple grid.
        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(new EmptyBorder(8, 8, 8, 8));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        addEndpointAssistantRow(content, gbc, "dialog.endpoint.transport", transportCombo);
        addEndpointAssistantRow(content, gbc, "dialog.endpoint.host", hostField);
        addEndpointAssistantRow(content, gbc, "dialog.endpoint.port", portSpinner);
        addEndpointAssistantRow(content, gbc, "dialog.endpoint.path", pathField);
        theme(content);

        // Loop so an invalid combination keeps the dialog open with the user's input,
        // instead of discarding it. Cancel (or the window close) leaves the field as is.
        Object[] options = { t("dialog.endpoint.apply"), t("dialog.endpoint.cancel") };
        while (true) {
            int result = JOptionPane.showOptionDialog(frame, content, t("dialog.endpoint.title"),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (result != 0) {
                return; // anything other than "Apply" (index 0) cancels
            }
            // Compose the URL from the four parts and validate it. On success write
            // it back to the endpoint field and close; on failure warn and loop.
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

    /** Adds one label + field row to the endpoint assistant's grid. */
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

    /** Returns the validated endpoint from the field, throwing if it is malformed. */
    private String endpointForConnect() throws PlccomMqttException {
        String endpoint = trim(endpointField);
        validateEndpoint(endpoint);
        return endpoint;
    }

    /** Composes a broker URL from the assistant's transport, host, port and path. */
    private String buildEndpointFromParts(Object transport, String host, int port, String path) {
        String scheme = endpointScheme(transport);
        StringBuilder builder = new StringBuilder();
        builder.append(scheme).append("://").append(hostForUri(host)).append(':').append(port);
        if (isWebSocketTransport(transport)) {
            builder.append(normalizedWebSocketPath(path));
        }
        return builder.toString();
    }

    /**
     * Validates a broker endpoint URL: a known scheme, no embedded credentials, a
     * host and a 1..65535 port, no query/fragment, and a path only for WebSocket
     * schemes. Throws a localized {@link PlccomMqttException} for the first problem.
     */
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
        String rawAuthority = uri.getRawAuthority();
        boolean hasUserInfo = (uri.getUserInfo() != null && uri.getUserInfo().length() > 0)
                || (rawAuthority != null && rawAuthority.indexOf('@') >= 0);
        if (hasUserInfo) {
            throw new PlccomMqttException(t("error.endpoint.userInfo"));
        }
        String host = endpointHost(uri);
        if (host == null || host.trim().length() == 0) {
            throw new PlccomMqttException(t("error.endpoint.host"));
        }
        int port = endpointPort(uri);
        if (port < 1 || port > 65535) {
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

    /** Splits an endpoint URL back into assistant parts, falling back to sane defaults. */
    private EndpointParts parseEndpointParts(String endpoint) {
        EndpointParts defaults = new EndpointParts(ENDPOINT_TRANSPORT_TCP, "127.0.0.1", 1883, "/mqtt");
        if (endpoint == null || endpoint.trim().length() == 0) {
            return defaults;
        }
        try {
            URI uri = new URI(endpoint.trim());
            String host = endpointHost(uri);
            if (!isSupportedEndpointScheme(uri.getScheme()) || host == null) {
                return defaults;
            }
            String transport = endpointTransport(uri.getScheme());
            int uriPort = endpointPort(uri);
            int port = uriPort > 0 ? uriPort : defaultEndpointPort(transport);
            String path = uri.getPath();
            return new EndpointParts(transport, host, port,
                    path == null || path.length() == 0 ? "/mqtt" : path);
        } catch (RuntimeException | URISyntaxException ex) {
            return defaults;
        }
    }

    /** True for the four accepted schemes: mqtt, mqtts, ws, wss. */
    private static boolean isSupportedEndpointScheme(String scheme) {
        return "mqtt".equalsIgnoreCase(scheme)
                || "mqtts".equalsIgnoreCase(scheme)
                || "ws".equalsIgnoreCase(scheme)
                || "wss".equalsIgnoreCase(scheme);
    }

    // java.net.URI.getHost()/getPort() return null/-1 for host names that are not
    // strictly RFC 2396 compliant, most notably Windows/NetBIOS names containing
    // an underscore (for example MY_BROKER_HOST). These helpers recover the host
    // and port from the raw authority so the tool accepts such endpoints, matching
    // the underscore-tolerant parsing in the client library (PlccomMqttEndpoint).
    // Package-private so ClientToolEndpointTest can verify the recovery directly.
    static String endpointHost(URI uri) {
        String host = uri.getHost();
        if (host != null) {
            return host;
        }
        String authority = authorityWithoutUserInfo(uri);
        if (authority == null) {
            return null;
        }
        if (authority.startsWith("[")) {
            int close = authority.indexOf(']');
            return close > 0 ? authority.substring(0, close + 1) : null;
        }
        int colon = authority.lastIndexOf(':');
        String hostPart = colon >= 0 ? authority.substring(0, colon) : authority;
        return hostPart.length() == 0 ? null : hostPart;
    }

    static int endpointPort(URI uri) {
        int port = uri.getPort();
        if (port >= 0) {
            return port;
        }
        String authority = authorityWithoutUserInfo(uri);
        if (authority == null) {
            return -1;
        }
        int colon;
        if (authority.startsWith("[")) {
            int close = authority.indexOf(']');
            colon = close < 0 ? -1 : authority.indexOf(':', close);
        } else {
            colon = authority.lastIndexOf(':');
        }
        if (colon < 0 || colon + 1 >= authority.length()) {
            return -1;
        }
        try {
            return Integer.parseInt(authority.substring(colon + 1));
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    static String authorityWithoutUserInfo(URI uri) {
        String authority = uri.getRawAuthority();
        if (authority == null) {
            return null;
        }
        int at = authority.indexOf('@');
        return at >= 0 ? authority.substring(at + 1) : authority;
    }

    /** URI scheme for an assistant transport (mqtt/mqtts/ws/wss). */
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

    /** Inverse of {@link #endpointScheme}: the transport label for a URI scheme. */
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

    /** Conventional default port per transport (1883/8883/8080/8443). */
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

    /** True for the WebSocket transports (WS/WSS), which alone carry a path. */
    private static boolean isWebSocketTransport(Object transport) {
        return ENDPOINT_TRANSPORT_WS.equals(transport) || ENDPOINT_TRANSPORT_WSS.equals(transport);
    }

    /** Ensures a WebSocket path is non-empty and starts with a slash. */
    private static String normalizedWebSocketPath(String path) {
        if (path == null || path.trim().length() == 0) {
            return "/mqtt";
        }
        String value = path.trim();
        return value.startsWith("/") ? value : "/" + value;
    }

    /** Wraps a bare IPv6 address in [brackets] so it is valid in a URI authority. */
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

    /** SHA-1 thumbprint of a certificate, or "unknown" if it cannot be computed. */
    private static String certificateFingerprint(X509Certificate certificate) {
        try {
            return PlccomMqttPkiStore.sha1Thumbprint(certificate);
        } catch (Exception ex) {
            return "unknown";
        }
    }

    /** True when the endpoint uses a TLS transport (mqtts:// or wss://). */
    private static boolean isSecureEndpoint(String endpoint) {
        String lower = endpoint.toLowerCase(Locale.ROOT);
        return lower.startsWith("mqtts://") || lower.startsWith("wss://");
    }

    /** Selects the MQTT protocol version on the builder from the protocol combo. */
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

    /** Configures the Last Will on the builder from the Last Will fields, if enabled. */
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

    /** Sets username/password on the builder and wipes the password bytes afterwards. */
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

    /** Applies the selected QoS (0/1/2) to a publish builder. */
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

    /** Applies the selected QoS (0/1/2) to a Last Will builder. */
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

    /** Applies the selected maximum QoS (0/1/2) to a subscribe builder. */
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

    /** Maps the retain-handling combo to the MQTT 5 retain-handling option. */
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

    /** Adds a received message to the Messages table and the log, on the Swing thread. */
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

    /**
     * Diagnostic callback from the client: renders the event as a log line. The
     * SDK dispatcher has already applied the selected diagnostic level, so the
     * event goes straight to the panel - coloured by its real severity instead
     * of a fixed warning colour.
     */
    private void diagnostic(PlccomMqttDiagnosticEvent event) {
        logPanel.append(diagnosticLogLevel(event), formatDiagnostic(event));
    }

    /** Maps a diagnostic severity to the log panel colour level. */
    private static LogLevel diagnosticLogLevel(PlccomMqttDiagnosticEvent event) {
        switch (event.getSeverity()) {
            case ERROR:
            case FATAL:
                return LogLevel.ERROR;
            case WARNING:
                return LogLevel.WARNING;
            default:
                return LogLevel.INFO;
        }
    }

    /** Formats a diagnostic event into a single structured, filterable log line. */
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
        if (event.getCause() != null) {
            builder.append(" cause=").append(shortCause(event.getCause()));
        }
        return builder.toString();
    }

    /** Switches between the dark and light palettes and repaints. */
    private void toggleTheme() {
        palette = palette.dark ? Palette.light() : Palette.dark();
        applyTheme();
        scheduleSettingsAutoSave();
    }

    /** Forces the light palette (used by the screenshot mode). */
    private void setLightTheme() {
        palette = Palette.light();
        applyTheme();
    }

    /** Switches the UI language between German and English. */
    private void toggleLanguage() {
        setLanguage(Locale.GERMAN.getLanguage().equals(locale.getLanguage()) ? Locale.ENGLISH : Locale.GERMAN);
    }

    /** Applies the given UI language, reloads the texts and re-translates the window. */
    private void setLanguage(Locale value) {
        locale = value;
        texts = ResourceBundle.getBundle(MESSAGE_BUNDLE, locale);
        translate();
        scheduleSettingsAutoSave();
    }

    /** Closes the window, which triggers the shutdown and settings-save handler. */
    private void exitApplication() {
        frame.dispose();
    }

    /** Re-applies all texts for the current language across the whole window. */
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
        // The theme button label depends on the current theme, not on a fixed
        // resource key, so restore it after the generic button translation.
        updateThemeButtonLabel();
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

    /** Re-applies the current palette to every component and repaints the window. */
    private void applyTheme() {
        if (root == null) {
            return;
        }
        theme(root);
        if (logPanel != null) {
            logPanel.translate();
            logPanel.recolor();
        }
        updateThemeButtonLabel();
        refreshTabTitles();
        styleStatusBadge();
        updateActionStates();
        frame.revalidate();
        frame.repaint();
    }

    /** Keeps the theme button labelled with the theme it would switch to. */
    private void updateThemeButtonLabel() {
        if (themeButton != null) {
            themeButton.setText(palette.dark ? t("button.theme.light") : t("button.theme.dark"));
        }
    }

    /** Refreshes button and field enablement, hopping onto the Swing thread if needed. */
    private void updateActionStates() {
        if (SwingUtilities.isEventDispatchThread()) {
            applyActionStates();
        } else {
            SwingUtilities.invokeLater(this::applyActionStates);
        }
    }

    /**
     * Enables or disables and re-themes the action buttons and the certificate/Last
     * Will fields to match the current connection and busy state.
     */
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

    /**
     * Recursively applies the current palette to a component and its children. A few
     * components are special-cased by name (header, footer, action bar, subtitle,
     * license link) or by type (text fields, buttons, tables, combos and so on).
     */
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
        } else if ("licenseLink".equals(name)) {
            component.setForeground(palette.accent);
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

    /**
     * Colours a button for the current palette and state: primary (accent), secondary
     * or disabled, with icon-only buttons getting tighter padding.
     */
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

    /** A bordered card panel with extra inner padding, used as a form column. */
    private JPanel formPanel() {
        JPanel panel = cardPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));
        return panel;
    }

    /** A panel with the standard themed border and padding used for cards. */
    private JPanel cardPanel(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(palette.border),
                new EmptyBorder(8, 8, 8, 8)));
        return panel;
    }

    /** Fresh {@link GridBagConstraints} with the standard form insets and left anchor. */
    private GridBagConstraints formGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }

    /** Adds one "label + field" row to a form and registers the field's tooltip. */
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

    /** Adds a stretching glue row so a form's rows stay top-aligned. */
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

    /** Lays out the endpoint field with the assistant button on its right. */
    private JPanel endpointInput(JButton assistantButton) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBorder(BorderFactory.createEmptyBorder());
        panel.add(fixedToolField(244, endpointField), BorderLayout.CENTER);
        panel.add(assistantButton, BorderLayout.EAST);
        setFixedSize(panel, TOOL_FIELD_WIDTH, CONTROL_HEIGHT);
        return panel;
    }

    /** Wraps a field (plus optional trailing controls) in the standard field cell. */
    private JPanel toolInput(JComponent field, Component... trailingComponents) {
        return toolInput(TOOL_FIELD_WIDTH, field, trailingComponents);
    }

    /** Wraps a field of the given width, followed by optional trailing controls. */
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

    /** Lays out several controls in a row with even spacing (no fixed width). */
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

    /**
     * Lays out a leading checkbox plus a trailing control as one fixed-width field
     * cell ({@link #TOOL_FIELD_WIDTH}). The checkbox keeps a fixed width so the
     * trailing control lands at the same position in every language.
     */
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

    /** Wraps a component in a fixed-size holder so form columns stay aligned. */
    private JComponent fixedToolField(int width, JComponent field) {
        JPanel holder = new JPanel(new BorderLayout());
        holder.setBorder(BorderFactory.createEmptyBorder());
        holder.add(field, BorderLayout.CENTER);
        setFixedSize(holder, width, CONTROL_HEIGHT);
        return holder;
    }

    /** A fixed-size, vertically scrolling holder for a multi-line text area. */
    private JScrollPane fixedToolArea(Component component, int height) {
        JScrollPane pane = scroll(component);
        pane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        setFixedSize(pane, TOOL_FIELD_WIDTH, height);
        return pane;
    }

    /** A label whose text is registered for live re-translation. */
    private JLabel label(String key) {
        JLabel label = new JLabel(t(key));
        labelKeys.put(label, key);
        return label;
    }

    /** A button styled as the primary (accent) action of its tab. */
    private JButton primaryButton(String key, Runnable action) {
        JButton button = button(key, iconFor(key), action);
        primaryButtons.add(button);
        return button;
    }

    /** A standard button with an icon auto-chosen from its resource key. */
    private JButton button(String key, Runnable action) {
        return button(key, iconFor(key), action);
    }

    /** Builds a themed button with the given icon and wires its action and tooltip. */
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

    /** A square, icon-only button (e.g. copy/save/clear in the log toolbar). */
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

    /** A left-aligned bar of buttons with a top divider, used at the foot of each tab. */
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

    /** Picks a fitting icon for a button based on keywords in its resource key. */
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

    /** A checkbox whose label is registered for live re-translation. */
    private JCheckBox checkBox(String key, boolean selected) {
        JCheckBox box = new JCheckBox(t(key), selected);
        buttonKeys.put(box, key);
        registerTooltip(box, key + ".tip");
        return box;
    }

    /** Binds a tooltip (when the key exists) and remembers it for re-translation. */
    private void registerTooltip(JComponent component, String key) {
        if (texts.containsKey(key)) {
            tooltipKeys.put(component, key);
            component.setToolTipText(t(key));
        }
    }

    /** Binds the same tooltip to a component and all of its children (for wrapped fields). */
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

    /** A text field normalized to the standard control height. */
    private JTextField textField(int columns, String text) {
        JTextField field = new JTextField(text, columns);
        normalizeControlHeight(field);
        return field;
    }

    /** A numeric spinner with thousands grouping (e.g. timeouts in milliseconds). */
    private JSpinner spinner(int value, int min, int max, int step) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, step));
        configureSpinner(spinner, true);
        return spinner;
    }

    /** A numeric spinner without grouping (e.g. identifiers and small counts). */
    private JSpinner plainSpinner(int value, int min, int max, int step) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, step));
        configureSpinner(spinner, false);
        return spinner;
    }

    /** Sizes and right-aligns a spinner and toggles its thousands grouping. */
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

    /** A combo box sized to at least the standard control width. */
    private JComboBox<String> combo(String... values) {
        JComboBox<String> combo = new JComboBox<String>(new DefaultComboBoxModel<String>(values));
        Dimension preferred = combo.getPreferredSize();
        setFixedSize(combo, Math.max(preferred.width, COMBO_CONTROL_WIDTH), CONTROL_HEIGHT);
        return combo;
    }

    /** A plain scroll pane around the given component. */
    private JScrollPane scroll(Component component) {
        return new JScrollPane(component);
    }

    /** A left-aligned flow panel with no border, for inline groupings. */
    private JPanel inlinePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setBorder(BorderFactory.createEmptyBorder());
        return panel;
    }

    /** A fixed-width form label, given a help bubble when the field has a tooltip. */
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

    /** The small "?" help bubble that reveals a field's tooltip on hover. */
    private JComponent helpIcon(String tooltipKey) {
        HelpBubble help = new HelpBubble();
        tooltipKeys.put(help, tooltipKey);
        help.setToolTipText(t(tooltipKey));
        return help;
    }

    /** Whether a field should stretch to fill the remaining width of its form column. */
    private boolean shouldStretch(Component component) {
        return component instanceof JTextComponent
                || component instanceof JScrollPane
                || component instanceof JPanel;
    }

    /** Forces a component to the standard control height while keeping its width. */
    private void normalizeControlHeight(JComponent component) {
        Dimension preferred = component.getPreferredSize();
        component.setPreferredSize(new Dimension(preferred.width, CONTROL_HEIGHT));
        component.setMinimumSize(new Dimension(Math.min(preferred.width, 80), CONTROL_HEIGHT));
    }

    /** Pins a component's preferred/min/max size so the layout never resizes it. */
    private void setFixedSize(JComponent component, int width, int height) {
        Dimension size = new Dimension(width, height);
        component.setPreferredSize(size);
        component.setMinimumSize(size);
        component.setMaximumSize(size);
    }

    /** Loads the PLCcom wordmark, trims its margins and scales it for the header. */
    private Icon loadLogo() {
        try {
            BufferedImage source = ImageIO.read(
                    PlccomMqttClientToolApp.class.getResource("/assets/PLCcom.png"));
            return new ImageIcon(scaledLogo(trimLogoMargins(source), LOGO_HEIGHT));
        } catch (Exception ex) {
            return new ImageIcon();
        }
    }

    /**
     * Loads the PLCcom application icon in several sizes so the operating system
     * can pick the sharpest one for the title bar, task bar and Alt-Tab switcher.
     */
    private List<Image> loadAppIcons() {
        List<Image> icons = new ArrayList<Image>();
        for (int size : new int[] { 16, 32, 48, 64, 128, 256 }) {
            try {
                java.net.URL url = getClass().getResource("/assets/icon/PLCcom_" + size + ".png");
                if (url != null) {
                    icons.add(ImageIO.read(url));
                }
            } catch (IOException ignored) {
                // A missing or unreadable size is skipped; the others still apply.
            }
        }
        return icons;
    }

    /**
     * Removes the uniform transparent or near-white margin baked into the logo
     * file so the wordmark fills the header card instead of floating in a wide
     * white border. Returns the original image when there is nothing to trim.
     */
    private static BufferedImage trimLogoMargins(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int top = 0;
        int bottom = height - 1;
        int left = 0;
        int right = width - 1;
        while (top < bottom && logoRowIsMargin(source, top)) {
            top++;
        }
        while (bottom > top && logoRowIsMargin(source, bottom)) {
            bottom--;
        }
        while (left < right && logoColumnIsMargin(source, left)) {
            left++;
        }
        while (right > left && logoColumnIsMargin(source, right)) {
            right--;
        }
        if (left == 0 && top == 0 && right == width - 1 && bottom == height - 1) {
            return source;
        }
        return source.getSubimage(left, top, right - left + 1, bottom - top + 1);
    }

    /** True when the whole row {@code y} of the logo is margin, so it can be trimmed. */
    private static boolean logoRowIsMargin(BufferedImage image, int y) {
        for (int x = 0; x < image.getWidth(); x++) {
            if (!isLogoMarginPixel(image.getRGB(x, y))) {
                return false;
            }
        }
        return true;
    }

    /** True when the whole column {@code x} of the logo is margin, so it can be trimmed. */
    private static boolean logoColumnIsMargin(BufferedImage image, int x) {
        for (int y = 0; y < image.getHeight(); y++) {
            if (!isLogoMarginPixel(image.getRGB(x, y))) {
                return false;
            }
        }
        return true;
    }

    /** A pixel counts as margin when it is (almost) transparent or near-white. */
    private static boolean isLogoMarginPixel(int argb) {
        int alpha = (argb >>> 24) & 0xFF;
        if (alpha < 16) {
            return true;
        }
        int red = (argb >> 16) & 0xFF;
        int green = (argb >> 8) & 0xFF;
        int blue = argb & 0xFF;
        return red > 244 && green > 244 && blue > 244;
    }

    /**
     * Builds a multi-resolution icon for the requested display height. The base
     * variant is a bicubically scaled bitmap for pixel-perfect rendering at 100%
     * display scale; the high-resolution variant is the full-resolution source
     * itself, so on any HiDPI scale (125%, 150%, 200%, ...) Swing has enough
     * pixels to downscale smoothly instead of upsizing a small bitmap.
     */
    private static Image scaledLogo(BufferedImage source, int targetHeight) {
        int targetWidth = Math.max(1,
                Math.round(targetHeight * (float) source.getWidth() / source.getHeight()));
        Image standard = highQualityScale(source, targetWidth, targetHeight);
        return new BaseMultiResolutionImage(standard, source);
    }

    /** Down-scales an image to the target size, halving progressively for a clean result. */
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

    /** One scaling step: draws the source into a new image with high-quality hints. */
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

    /** Sets the status pill's text and colour for the given status, on the Swing thread. */
    private void setStatus(String key, Status status) {
        SwingUtilities.invokeLater(() -> {
            currentStatus = status;
            setStatusTextOnly(key);
            styleStatusBadge();
        });
    }

    /** Updates only the status pill's text, keeping the current status colour. */
    private void setStatusTextOnly(String key) {
        if (statusPill != null) {
            statusPill.setText(t(key));
            styleStatusBadge();
        }
    }

    /** Colours the status pill and its dot to match the current status and theme. */
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

    // Logging helpers: translate a message key (optionally with arguments), or take
    // ready-made text, and append it to the log panel at the matching level. Every
    // helper runs through appendLog so the selected diagnostic level filters the
    // WHOLE log, exactly like the C# reference tool.
    private void logInfo(String key, Object... args) {
        appendLog(LogLevel.INFO, format(key, args));
    }

    private void logSuccess(String key, Object... args) {
        appendLog(LogLevel.SUCCESS, format(key, args));
    }

    private void logWarning(String key, Object... args) {
        appendLog(LogLevel.WARNING, format(key, args));
    }

    private void logError(String key, Object... args) {
        appendLog(LogLevel.ERROR, format(key, args));
    }

    private void logInfoText(String text) {
        appendLog(LogLevel.INFO, text);
    }

    private void logWarningText(String text) {
        appendLog(LogLevel.WARNING, text);
    }

    private void logErrorText(String text) {
        appendLog(LogLevel.ERROR, text);
    }

    /**
     * The single funnel for the tool's own log lines: applies the diagnostic-level
     * filter before the line reaches the panel. SDK diagnostic events do not pass
     * through here - they are already filtered inside the SDK dispatcher with the
     * same selected level.
     */
    private void appendLog(LogLevel level, String text) {
        if (shouldLog(level)) {
            logPanel.append(level, text);
        }
    }

    /**
     * Decides whether a tool log line passes the selected diagnostic level. This
     * mirrors the C# reference tool, where the level filters the whole log and
     * not only SDK diagnostics: Off silences everything, Debug and Info show all
     * tool lines (the tool itself has no DEBUG lines), Warnings keeps warnings
     * and errors, Errors keeps errors only, and Fatal leaves only the SDK's
     * fatal diagnostics visible.
     */
    private boolean shouldLog(LogLevel level) {
        switch (uiLogLevel) {
            case OFF:
            case FATAL:
                return false;
            case WARNING:
                return level == LogLevel.WARNING || level == LogLevel.ERROR;
            case ERROR:
                return level == LogLevel.ERROR;
            case DEBUG:
            case INFO:
            default:
                return true;
        }
    }

    /** Formats a resource string with {@link java.text.MessageFormat} arguments. */
    private String format(String key, Object... args) {
        return java.text.MessageFormat.format(t(key), args);
    }

    /** Localized text for a key, or a visible {@code !key!} marker when it is missing. */
    private String t(String key) {
        return texts.containsKey(key) ? texts.getString(key) : "!" + key + "!";
    }

    /** The trimmed text of a field. */
    private static String trim(JTextComponent field) {
        return field.getText().trim();
    }

    /** The trimmed text of a field, or {@code null} when it is empty. */
    private static String trimOrNull(JTextComponent field) {
        String value = trim(field);
        return value.length() == 0 ? null : value;
    }

    /** The int value currently held by a spinner. */
    private static int number(JSpinner spinner) {
        return ((Number) spinner.getValue()).intValue();
    }

    /** One-line "Type: message" summary of a throwable for the log. */
    private static String shortCause(Throwable ex) {
        if (ex == null) {
            return "none";
        }
        return ex.getClass().getSimpleName() + ": " + ex.getMessage();
    }

    /** Full "Type: message -&gt; cause -&gt; ..." chain (up to 8 levels) for the details line. */
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

    // Per-user file locations. Settings and PKI material live under the platform's
    // standard application-data directory (APPDATA on Windows, ~/Library on macOS,
    // XDG data home on Linux), all beneath a shared PLCcom/Mqtt root.
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

    // Typed settings accessors: each put* writes one widget's value into the
    // Properties, and each set* restores it. The "protected" text variants
    // transparently encrypt/decrypt sensitive values (license, credentials, passwords).
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

    /**
     * Encrypts a settings value with AES/GCM and returns a prefixed Base64 string, the
     * random 12-byte IV prepended to the ciphertext. Returns "" on any failure.
     */
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

    /** Reverses {@link #encryptSetting}; passes plain values through, returns "" on failure. */
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

    /**
     * Derives the AES key with PBKDF2 from stable, machine-local properties (user
     * name, home and OS name), so the encrypted settings are readable on this account
     * but do not travel to another machine or user.
     */
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

    /**
     * The tool version for the window title. The built JAR reports the real
     * Maven version through the manifest implementation version; without a
     * manifest (IDE or test runs) the SDK's central {@link VersionInfo} is
     * used, so no per-tool version constant has to be maintained.
     */
    private static String version() {
        String version = PlccomMqttClientToolApp.class.getPackage().getImplementationVersion();
        return version == null ? VersionInfo.getVersion() : version;
    }

    /** Paints the current window into a PNG file (used by the {@code --screenshot} mode). */
    private void writeScreenshot(File file) throws IOException {
        BufferedImage image = new BufferedImage(frame.getWidth(), frame.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        frame.paint(image.getGraphics());
        ImageIO.write(image, "png", file);
    }

    /** A {@link Runnable} that may throw, used for the background tool operations. */
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    /** Header status-pill states, each mapped to a palette colour. */
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

    /** Severity of a log line, which selects its colour. */
    private enum LogLevel {
        INFO, SUCCESS, WARNING, ERROR
    }

    /**
     * The log panel below the tabs: a coloured, monospaced, size-capped transcript
     * with a copy/save/clear/detach toolbar and a "max lines" selector. It can also
     * mirror its content live into a large detached {@link LogWindow}.
     */
    private final class LogPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private static final int DEFAULT_MAX_LINES = 5000;
        private final JTextPane textPane = new JTextPane();
        private final JScrollPane scrollPane = new JScrollPane();
        private final java.util.List<LogLine> entries = new java.util.ArrayList<>();
        private final JButton copyButton = iconButton(ToolIcon.COPY, "button.copyLog", this::copy);
        private final JButton saveButton = iconButton(ToolIcon.SAVE, "button.saveLog", this::save);
        private final JButton clearButton = iconButton(ToolIcon.TRASH, "button.clearLog", this::clear);
        private final JButton windowButton = iconButton(ToolIcon.WINDOW, "button.logWindow", this::openWindow);
        private final JComboBox<Integer> maxLinesCombo =
                new JComboBox<>(new Integer[] { 500, 1000, 5000, 10000 });
        private final JLabel maxLinesLabel = label("log.maxLines");
        private int maxLines = DEFAULT_MAX_LINES;
        private LogWindow logWindow;

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
            toolbar.add(javax.swing.Box.createVerticalStrut(6));
            toolbar.add(windowButton);
            toolbar.add(javax.swing.Box.createVerticalGlue());

            maxLinesCombo.setSelectedItem(Integer.valueOf(DEFAULT_MAX_LINES));
            maxLinesCombo.addActionListener(event -> {
                Integer selected = (Integer) maxLinesCombo.getSelectedItem();
                if (selected != null) {
                    maxLines = selected.intValue();
                    trimToMax();
                    scheduleSettingsAutoSave();
                }
            });
            JPanel header = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            header.setName("logHeader");
            header.setOpaque(false);
            header.add(maxLinesLabel);
            header.add(maxLinesCombo);

            add(header, BorderLayout.NORTH);
            add(toolbar, BorderLayout.WEST);
            add(scrollPane, BorderLayout.CENTER);
            translate();
        }

        /** Re-applies the titled border and colours for the current language and theme. */
        void translate() {
            setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(palette.border),
                    t("log.title"), TitledBorder.LEFT, TitledBorder.TOP,
                    getFont().deriveFont(Font.BOLD, 12f), palette.muted));
            scrollPane.setBorder(BorderFactory.createLineBorder(palette.border));
            if (logWindow != null) {
                logWindow.translate();
            }
            repaint();
        }

        /**
         * Appends one timestamped line at the given level (hopping onto the Swing
         * thread if needed), mirrors it into the detached window if open, and trims
         * back to the line cap.
         */
        void append(LogLevel level, String message) {
            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater(() -> append(level, message));
                return;
            }
            String line = TIME.format(new Date()) + "  " + message + System.lineSeparator();
            entries.add(new LogLine(level, line));
            insertLine(level, line);
            if (logWindow != null) {
                logWindow.appendLine(level, line);
            }
            trimToMax();
            textPane.setCaretPosition(textPane.getStyledDocument().getLength());
        }

        /** Inserts one styled line into the embedded pane. */
        private void insertLine(LogLevel level, String line) {
            insertInto(textPane, level, line);
        }

        /** Inserts a line into a pane in the colour for its level, rebuilding on error. */
        private void insertInto(JTextPane target, LogLevel level, String line) {
            StyledDocument doc = target.getStyledDocument();
            Style style = target.addStyle(level.name(), null);
            StyleConstants.setForeground(style, color(level));
            try {
                doc.insertString(doc.getLength(), line, style);
            } catch (BadLocationException ex) {
                target.setText(target.getText() + line);
            }
        }

        /** Re-renders every retained line into the given pane in current colours. */
        private void rebuildPane(JTextPane target) {
            target.setText("");
            for (LogLine entry : entries) {
                insertInto(target, entry.level, entry.text);
            }
        }

        /** Removes leading characters from a pane, rebuilding it on any mismatch. */
        private void removeLeadingFrom(JTextPane target, int chars) {
            StyledDocument doc = target.getStyledDocument();
            try {
                if (chars <= doc.getLength()) {
                    doc.remove(0, chars);
                } else {
                    rebuildPane(target);
                }
            } catch (BadLocationException ex) {
                rebuildPane(target);
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
            rebuildPane(textPane);
            textPane.setCaretPosition(textPane.getStyledDocument().getLength());
            if (logWindow != null) {
                logWindow.refreshTheme();
            }
        }

        /** Clears the transcript in the panel and in the detached window. */
        private void clear() {
            entries.clear();
            textPane.setText("");
            if (logWindow != null) {
                logWindow.clearText();
            }
        }

        /** Opens, or re-focuses, the large detached live log window. */
        private void openWindow() {
            if (logWindow != null) {
                logWindow.toFront();
                logWindow.requestFocus();
                return;
            }
            logWindow = new LogWindow();
            logWindow.setVisible(true);
        }

        /**
         * Drops the oldest log lines once the configured maximum is exceeded, from
         * both the retained list and the document, so the log stays bounded. The
         * excess is removed in one pass (amortised); on any offset mismatch the
         * document is rebuilt from the retained entries.
         */
        private void trimToMax() {
            int excess = entries.size() - maxLines;
            if (excess <= 0) {
                return;
            }
            int chars = 0;
            for (int i = 0; i < excess; i++) {
                chars += entries.get(i).text.length();
            }
            entries.subList(0, excess).clear();
            removeLeadingFrom(textPane, chars);
            if (logWindow != null) {
                logWindow.removeLeading(chars);
            }
        }

        /** The current maximum number of retained log lines. */
        int getMaxLines() {
            return maxLines;
        }

        void setMaxLines(int value) {
            // Selecting a valid option fires the listener, which updates maxLines
            // and trims immediately; an unknown value keeps the current default.
            maxLinesCombo.setSelectedItem(Integer.valueOf(value));
        }

        /** Palette colour for a log level. */
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

        /** Saves the visible log to a user-chosen file. */
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

        /** Copies the selection, or the whole log when nothing is selected, to the clipboard. */
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

        /**
         * Detached large log viewer. It mirrors the docked log live (append,
         * trim, clear, theme and language changes) and adds an auto-scroll
         * toggle, so an operator can either follow the newest lines or hold a
         * scroll position while analysing older output.
         */
        private final class LogWindow extends JDialog {
            private static final long serialVersionUID = 1L;
            private final JTextPane pane = new JTextPane();
            private final JScrollPane paneScroll = new JScrollPane();
            private final JCheckBox autoScrollCheck = new JCheckBox("", true);

            LogWindow() {
                super(frame);
                setIconImages(loadAppIcons());
                setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                pane.setEditable(false);
                pane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
                pane.setBorder(new EmptyBorder(6, 8, 6, 8));
                // The caret must never follow document changes on its own: with
                // auto-scroll off the view has to stay exactly where the user
                // scrolled to, even while new lines keep arriving.
                ((DefaultCaret) pane.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
                paneScroll.setViewportView(pane);

                JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
                bar.add(autoScrollCheck);

                JPanel content = new JPanel(new BorderLayout(0, 4));
                content.setBorder(new EmptyBorder(8, 10, 10, 10));
                content.add(bar, BorderLayout.NORTH);
                content.add(paneScroll, BorderLayout.CENTER);
                setContentPane(content);

                setSize(1000, 700);
                setMinimumSize(new Dimension(640, 400));
                setLocationRelativeTo(frame);
                addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent event) {
                        logWindow = null;
                    }
                });
                translate();
                refreshTheme();
            }

            /** Re-applies the window title and auto-scroll label for the current language. */
            void translate() {
                setTitle(t("log.window.title"));
                autoScrollCheck.setText(t("log.autoScroll"));
            }

            /** Appends one already-formatted line and follows the tail when auto-scroll is on. */
            void appendLine(LogLevel level, String line) {
                insertInto(pane, level, line);
                followTail();
            }

            /** Drops the given number of leading characters to mirror the docked log's trim. */
            void removeLeading(int chars) {
                removeLeadingFrom(pane, chars);
                followTail();
            }

            /** Empties the detached view. */
            void clearText() {
                pane.setText("");
            }

            /** Re-applies the current palette and re-renders every retained line. */
            void refreshTheme() {
                theme(getContentPane());
                paneScroll.setBorder(BorderFactory.createLineBorder(palette.border));
                rebuildPane(pane);
                followTail();
            }

            /** Scrolls to the newest line, but only while auto-scroll is enabled. */
            private void followTail() {
                if (autoScrollCheck.isSelected()) {
                    pane.setCaretPosition(pane.getStyledDocument().getLength());
                }
            }
        }

        /** One retained log entry: its level (which drives the colour) and rendered text. */
        private final class LogLine {
            private final LogLevel level;
            private final String text;

            LogLine(LogLevel level, String text) {
                this.level = level;
                this.text = text;
            }
        }
    }

    /** Table cell renderer with theme-aware zebra striping and selection colours. */
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

    /** Combo-box popup renderer that matches the current theme. */
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

    /** The small circular "?" glyph that reveals a field's tooltip on hover. */
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
            Dimension size = new Dimension(logo.getIconWidth() + 20, logo.getIconHeight() + 12);
            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Resample the logo smoothly when the display runs at a fractional
                // scale (125%, 150%, ...). Without this hint Java2D falls back to
                // nearest-neighbour, which makes the wordmark look pixelated.
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
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

    /** The set of vector glyphs the tool draws on its buttons. */
    private enum ToolIcon {
        THEME, LANGUAGE, CERTIFICATE, EXIT, START, STOP, PUBLISH, SUBSCRIBE, REQUEST,
        FILE, ADD, REMOVE, COPY, SAVE, TRASH, REFRESH, RESET, WINDOW
    }

    /** A small filled dot used as the status pill's colour indicator. */
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

    /** Draws the crisp vector glyph for a {@link ToolIcon} at 16x16, theme-coloured. */
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
                    case WINDOW:
                        // Detached-window glyph: a frame with an arrow leaving top-right.
                        g2.drawLine(x + 3, y + 5, x + 3, y + 13);
                        g2.drawLine(x + 3, y + 13, x + 11, y + 13);
                        g2.drawLine(x + 11, y + 13, x + 11, y + 9);
                        g2.drawLine(x + 3, y + 5, x + 7, y + 5);
                        g2.drawLine(x + 8, y + 8, x + 13, y + 3);
                        g2.drawLine(x + 9, y + 3, x + 13, y + 3);
                        g2.drawLine(x + 13, y + 3, x + 13, y + 7);
                        break;
                    default:
                        break;
                }
            } finally {
                g2.dispose();
            }
        }
    }

    /** The parts of a broker endpoint URL, as edited in the endpoint assistant. */
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

    /** An immutable colour palette; {@link #dark()} and {@link #light()} are the two themes. */
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

    /** Parsed command-line options for the headless screenshot mode. */
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

        /** Parses {@code --screenshot}, {@code --light}, {@code --english} and {@code --tab}. */
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
