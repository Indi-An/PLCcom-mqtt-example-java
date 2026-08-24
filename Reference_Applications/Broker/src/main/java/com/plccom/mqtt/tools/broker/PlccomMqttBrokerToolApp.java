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

package com.plccom.mqtt.tools.broker;

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
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;
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
import javax.swing.JList;
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
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.plccom.mqtt.OperationResult;
import com.plccom.mqtt.broker.MqttBrokerAdminSnapshot;
import com.plccom.mqtt.broker.MqttBrokerStatistics;
import com.plccom.mqtt.broker.PlccomMqttBroker;
import com.plccom.mqtt.broker.PlccomMqttBrokerBuilder;
import com.plccom.mqtt.broker.PlccomMqttBrokerClientCertificateMode;
import com.plccom.mqtt.broker.PlccomMqttBrokerFactory;
import com.plccom.mqtt.broker.PlccomMqttBrokerListenerOptions;
import com.plccom.mqtt.broker.PlccomMqttBrokerTlsOptions;
import com.plccom.mqtt.client.PlccomMqttException;
import com.plccom.mqtt.core.VersionInfo;
import com.plccom.mqtt.diagnostics.PlccomMqttDiagnosticEvent;
import com.plccom.mqtt.diagnostics.PlccomMqttDiagnosticLevel;
import com.plccom.mqtt.diagnostics.PlccomMqttDiagnosticSeverity;
import com.plccom.mqtt.pki.PlccomMqttCertificateNames;
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
    private static final int SECURITY_FIELD_WIDTH = 230;
    private static final int LISTENER_NAME_COLUMN = 0;
    private static final int LISTENER_TRANSPORT_COLUMN = 1;
    private static final int LISTENER_BIND_COLUMN = 2;
    private static final int LISTENER_PORT_COLUMN = 3;
    private static final int LISTENER_ENDPOINT_COLUMN = 4;
    private static final int LISTENER_CERTIFICATE_COLUMN = 5;
    private static final int LISTENER_CLIENT_CERTS_COLUMN = 6;
    private static final int LISTENER_REMOVE_COLUMN = 7;
    private static final int LISTENER_ROWS_VISIBLE = 4;
    private static final int LISTENER_COLUMN_WIDTH = 650;
    private static final int LOGO_HEIGHT = 30;
    private static final int STATUS_BADGE_WIDTH = 150;
    private static final String DEFAULT_LISTENER_CERTIFICATE = "";
    private static final String DEFAULT_WEBSOCKET_PATH = "/mqtt";
    private static final String CLIENT_CERT_MODE_NONE = "NONE";
    private static final String CLIENT_CERT_MODE_OPTIONAL = "OPTIONAL";
    private static final String CLIENT_CERT_MODE_REQUIRED = "REQUIRED";
    private static final String[] OWN_CERTIFICATE_EXTENSIONS = {
            ".der", ".cer", ".cert", ".crt", ".pem"
    };
    private static final String[] OWN_PRIVATE_EXTENSIONS = {
            ".pem", ".key", ".der", ".p12", ".pfx"
    };
    private static final String SETTINGS_FILE_NAME = "broker-tool.properties";
    private static final String ENCRYPTED_PREFIX = "enc:v1:";
    // Fixed application salt (the ASCII bytes of "PLCcomMQTTTools1") for the PBKDF2
    // settings-key derivation. A constant salt is acceptable here because the derived
    // key only obfuscates locally stored secrets on this account; it is not a password
    // hash meant to resist offline cracking.
    private static final byte[] SETTINGS_SALT = new byte[] {
            80, 76, 67, 99, 111, 109, 77, 81, 84, 84, 84, 111, 111, 108, 115, 49
    };
    private static final SecureRandom SETTINGS_RANDOM = new SecureRandom();

    // Shared infrastructure. Every broker operation runs on this single-thread
    // executor so the Swing event dispatch thread never blocks; the maps remember
    // which resource key each widget shows so the whole UI can be re-translated
    // and re-themed live when the language or theme is switched.
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "PLCcom-MQTT-Broker-Tool");
        thread.setDaemon(true);
        return thread;
    });
    private final Map<JLabel, String> labelKeys = new LinkedHashMap<JLabel, String>();
    private final Map<AbstractButton, String> buttonKeys = new LinkedHashMap<AbstractButton, String>();
    private final Map<JComponent, String> tooltipKeys = new LinkedHashMap<JComponent, String>();
    // Buttons that should be painted in the accent colour (the primary action of a tab).
    private final Set<AbstractButton> primaryButtons =
            java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<AbstractButton, Boolean>());
    private final List<String> tabKeys = new ArrayList<String>();
    // Fingerprints already offered for trust, so we prompt once per rejected certificate.
    private final Set<String> promptedRejectedCertificates = ConcurrentHashMap.newKeySet();

    // Mutable application state: current language, theme, status and the live broker.
    private Locale locale = Locale.GERMAN;
    private ResourceBundle texts = ResourceBundle.getBundle(MESSAGE_BUNDLE, locale);
    private Palette palette = Palette.dark();
    private Status currentStatus = Status.NEUTRAL;
    // Written on the single background worker thread and read from the Swing
    // event dispatch thread (button states, status text), so it is volatile for
    // the reading thread to observe the latest value reliably.
    // The library reports the end of a time-limited license state with this
    // diagnostic event code. Kept in ONE place so a later family-wide rename
    // touches exactly one line in this tool.
    private static final String LICENSE_END_EVENT_CODE = "BROKER_LICENSE_EXPIRED";

    private volatile PlccomMqttBroker broker;
    private boolean settingsPersistenceEnabled = true;
    private boolean settingsSaved;

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
    private JButton startButton;
    private JButton stopButton;
    private LogPanel logPanel;
    private DefaultTableModel statisticsModel;
    private JTabbedPane tabs;
    private final AtomicBoolean operationActive = new AtomicBoolean();

    // Configuration widgets: license, the network listeners, the access rules and
    // the diagnostic level.
    private JTextField licenseUserField;
    private JTextField licenseSerialField;
    private final List<ListenerConfig> listenerConfigs = new ArrayList<ListenerConfig>();
    private DefaultTableModel listenerModel;
    private JTable listenerTable;
    private JCheckBox requireUsernameCheck;
    private JCheckBox requireClientCertificateCheck;
    private JTextField blockedTopicPrefixField;
    private JTextArea accessUsersArea;
    private JSpinner maxConnectionsSpinner;
    private JSpinner socketReadTimeoutSpinner;
    private JComboBox<String> diagnosticLevelCombo;
    // Mirror of the diagnostic-level combo for the tool's own log filter. Kept in
    // a volatile field because log helpers run on SDK worker threads, which must
    // not touch the Swing combo directly.
    private volatile PlccomMqttDiagnosticLevel uiLogLevel = PlccomMqttDiagnosticLevel.INFO;

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
        SwingUtilities.invokeLater(() -> new PlccomMqttBrokerToolApp().show());
    }

    /**
     * Builds the window, restores the saved settings and shows the application.
     * Runs on the Swing event dispatch thread.
     */
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
     * subtitle in the middle, and the status pill (Ready/Running/...) on the right.
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

        // Diagnostic level lives in the footer (left side), mirroring the C#
        // reference tool, so the listener tab stays focused on listeners and
        // access rules. It gets its own docked area so its label never crowds
        // the action buttons.
        JPanel diagnostics = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        diagnostics.setOpaque(false);
        JLabel diagnosticLevelLabel = label("listener.diagnosticLevel");
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

    /** Creates the two tabs (Listeners and Admin/Statistics) with translatable titles. */
    private JTabbedPane createTabs() {
        tabs = new JTabbedPane();
        tabs.addChangeListener(event -> refreshTabTitles());
        addToolTab("tab.listeners", createListenersPanel());
        addToolTab("tab.statistics", createStatisticsPanel());
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
     * Builds the Listeners tab: the network-listener table with its add/edit/remove
     * controls on top and the access-validation rules below, with Start and Stop in
     * the action bar.
     */
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
        configureListenerColumns();
        configureRemoveColumn();
        listenerConfigs.add(defaultTcpListenerConfig());
        rebuildListenerRows();
        requireUsernameCheck = checkBox("access.requireUsername", false);
        requireClientCertificateCheck = checkBox("access.requireClientCertificate", false);
        blockedTopicPrefixField = textField(24, "");
        // The users list is always editable, exactly like the C# reference tool.
        // Whether it participates in the access decision depends on "Require user
        // name" at broker start, not on the control being enabled.
        accessUsersArea = new JTextArea(4, 24);
        accessUsersArea.setLineWrap(false);
        accessUsersArea.setWrapStyleWord(false);
        accessUsersArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane accessUsersScroll = new JScrollPane(accessUsersArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        // Firm height so the list never collapses to a one-line strip when the
        // GridBag column is shorter than the sum of its preferred heights.
        accessUsersScroll.setPreferredSize(new Dimension(SECURITY_FIELD_WIDTH, 96));
        accessUsersScroll.setMinimumSize(new Dimension(120, 96));
        accessUsersScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));
        accessUsersScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerTooltipTree(accessUsersScroll, "access.users.tip");
        maxConnectionsSpinner = plainSpinner(1000, 1, 200000, 10);
        socketReadTimeoutSpinner = spinner(1000, 0, 600000, 250);
        diagnosticLevelCombo = combo("Debug / all", "Info", "Warnings", "Errors", "Fatal only", "Off");
        diagnosticLevelCombo.setSelectedIndex(1);
        // Push level changes to a running broker immediately, so switching the
        // level takes effect at once instead of only on the next start.
        diagnosticLevelCombo.addActionListener(event -> applyRuntimeDiagnosticLevel());
        applyListenerFieldWidth(maxConnectionsSpinner, socketReadTimeoutSpinner);

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

        // Right column: matches the C# reference layout. A group header, a visually
        // set-apart "Access validator" card (checkboxes + labelled users list),
        // then blocked topic prefix and diagnostics - all with labels above their
        // full-width fields.
        JPanel securityPanel = formPanel();
        GridBagConstraints securityGbc = formGbc();
        securityGbc.gridx = 0;
        securityGbc.gridwidth = 2;
        securityGbc.weightx = 1;
        securityGbc.fill = GridBagConstraints.HORIZONTAL;
        int securityRow = 0;

        securityGbc.gridy = securityRow++;
        securityGbc.insets = new Insets(0, 5, 4, 5);
        securityPanel.add(sectionTitle("section.security"), securityGbc);

        securityGbc.gridy = securityRow++;
        securityGbc.insets = new Insets(0, 5, 6, 5);
        securityPanel.add(buildAccessValidatorCard(accessUsersScroll), securityGbc);

        addStackedRow(securityPanel, securityGbc, securityRow++, "access.blockedPrefix",
                stretchField(blockedTopicPrefixField));

        securityGbc.gridy = securityRow;
        securityGbc.weighty = 1;
        securityGbc.fill = GridBagConstraints.BOTH;
        securityGbc.insets = new Insets(0, 5, 0, 5);
        securityPanel.add(javax.swing.Box.createVerticalGlue(), securityGbc);

        // Both columns share the width like the C# fork (roughly 1.08 : 0.92) so
        // the access card and its full-width fields get real room instead of being
        // stranded in dead space.
        columnGbc.gridx = 0;
        columnGbc.weightx = 1.08;
        columnGbc.insets = new Insets(0, 0, 0, 5);
        columns.add(networkPanel, columnGbc);

        columnGbc.gridx = 1;
        columnGbc.weightx = 0.92;
        columnGbc.insets = new Insets(0, 5, 0, 0);
        columns.add(securityPanel, columnGbc);
        panel.add(columns, BorderLayout.CENTER);

        startButton = primaryButton("button.start", this::startBroker);
        stopButton = button("button.stop", this::stopBroker);
        panel.add(actionBar(startButton, stopButton), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Builds the inner "Access validator" card of the right column: a set-apart
     * surface holding the two access-rule checkboxes and the labelled, always
     * editable users list. Mirrors the C# reference tool's inner access box.
     */
    private JPanel buildAccessValidatorCard(JScrollPane accessUsersScroll) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.putClientProperty("plccom.accessCard", Boolean.TRUE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(palette.border),
                new EmptyBorder(10, 12, 12, 12)));

        JLabel title = label("access.row");
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        requireUsernameCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        requireClientCertificateCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel usersLabel = label("access.users");
        usersLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(title);
        card.add(javax.swing.Box.createVerticalStrut(6));
        card.add(requireUsernameCheck);
        card.add(requireClientCertificateCheck);
        card.add(javax.swing.Box.createVerticalStrut(8));
        card.add(usersLabel);
        card.add(javax.swing.Box.createVerticalStrut(4));
        card.add(accessUsersScroll);
        return card;
    }

    /**
     * Adds a "label above, field below" pair to a GridBag form, both spanning the
     * full column width. This matches the C# reference tool where every field
     * carries its caption above it.
     */
    private void addStackedRow(JPanel panel, GridBagConstraints gbc, int labelRow,
            String key, JComponent field) {
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = labelRow * 2;
        gbc.insets = new Insets(6, 5, 1, 5);
        JLabel rowLabel = label(key);
        rowLabel.setFont(rowLabel.getFont().deriveFont(Font.PLAIN, 12f));
        panel.add(rowLabel, gbc);
        gbc.gridy = labelRow * 2 + 1;
        gbc.insets = new Insets(0, 5, 4, 5);
        registerTooltipTree(field, key + ".tip");
        panel.add(field, gbc);
    }

    /**
     * Relaxes a fixed-size control so a GridBag {@code HORIZONTAL} fill can
     * stretch it to the full column width while keeping its preferred height.
     */
    private JComponent stretchField(JComponent field) {
        int height = field.getPreferredSize().height;
        if (height <= 0) {
            height = CONTROL_HEIGHT;
        }
        field.setMinimumSize(new Dimension(80, height));
        field.setPreferredSize(new Dimension(SECURITY_FIELD_WIDTH, height));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        return field;
    }

    /** Builds the read-only table model that projects the listener configurations. */
    private DefaultTableModel createListenerModel() {
        // The table is a read-only projection of the listener configurations.
        // All changes go through the listener editor dialog so per-listener
        // TLS fields (certificate, password, client-certificate mode) can
        // never get out of sync with the visible row.
        return new DefaultTableModel(new Object[] {
                t("listener.column.name"),
                t("listener.column.transport"),
                t("listener.column.bind"),
                t("listener.column.port"),
                t("listener.column.endpoint"),
                t("listener.column.certificate"),
                t("listener.column.clientCerts"),
                "" }, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    /** The row of "add TCP/TLS/WS/WSS" buttons plus Edit shown above the listener table. */
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
                    showListenerEditor(modelRow, null);
                }
            }
        });
    }

    /** Sets sensible widths for the endpoint, certificate and client-certificate columns. */
    private void configureListenerColumns() {
        TableColumn endpointColumn = listenerTable.getColumnModel().getColumn(LISTENER_ENDPOINT_COLUMN);
        endpointColumn.setPreferredWidth(180);
        endpointColumn.setMinWidth(130);

        TableColumn certificateColumn = listenerTable.getColumnModel().getColumn(LISTENER_CERTIFICATE_COLUMN);
        certificateColumn.setPreferredWidth(130);
        certificateColumn.setMinWidth(100);

        TableColumn clientCertsColumn = listenerTable.getColumnModel().getColumn(LISTENER_CLIENT_CERTS_COLUMN);
        clientCertsColumn.setPreferredWidth(90);
        clientCertsColumn.setMinWidth(70);
    }

    /** The table column currently displaying the given model column, or null. */
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

    /** A combo listing the default identity plus the certificate aliases in the PKI store. */
    private JComboBox<String> listenerCertificateCombo() {
        JComboBox<String> combo = new JComboBox<String>(
                new DefaultComboBoxModel<String>(listenerCertificateOptions().toArray(new String[0])));
        combo.putClientProperty("plccom.listenerCertificateCombo", Boolean.TRUE);
        combo.setRenderer(new ListenerCertificateListRenderer());
        return combo;
    }

    /** The selectable listener certificates: the default plus any aliases found in the PKI store. */
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

    /** The label shown for a certificate value ("Default" for the empty/default identity). */
    private String displayListenerCertificate(Object value) {
        String alias = normalizeListenerCertificate(value == null ? null : String.valueOf(value));
        return alias.length() == 0 ? t("listener.certificate.default") : alias;
    }

    /** Adds the file-name aliases of certificates in a PKI subfolder to the set. */
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

    /** The alias part of a certificate file name (its name without a known extension). */
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

    /** Opens the listener editor for the selected row, or the first row when none is selected. */
    private void editSelectedListener() {
        if (listenerTable == null || listenerTable.getRowCount() == 0) {
            return;
        }
        int viewRow = listenerTable.getSelectedRow();
        if (viewRow < 0) {
            viewRow = 0;
            listenerTable.getSelectionModel().setSelectionInterval(0, 0);
        }
        showListenerEditor(listenerTable.convertRowIndexToModel(viewRow), null);
    }

    /**
     * Opens the modal listener editor to add or edit one listener. The TLS-only
     * fields (certificate, certificate password, client-certificate mode) and the
     * WebSocket path enable themselves for the chosen transport, and a live endpoint
     * preview shows the resulting address. The name is validated before saving.
     */
    private void showListenerEditor(int modelRow, String initialTransport) {
        // A valid modelRow means we edit that existing listener; a negative row means
        // we add a new one, with initialTransport pre-selecting TCP/TLS/WS/WSS.
        boolean edit = modelRow >= 0 && modelRow < listenerConfigs.size();
        ListenerConfig existing = edit ? listenerConfigs.get(modelRow) : null;
        String transport = edit ? existing.transport : normalizeTransport(initialTransport);
        // Pre-fill every editor from the existing listener, or from sensible defaults
        // for a new one: a unique name and the next free port for the transport.
        JTextField nameField = textField(20, edit ? existing.name : defaultListenerName(transport));
        JComboBox<String> transportCombo = combo("TCP", "TLS", "WS", "WSS");
        transportCombo.setSelectedItem(transport);
        JTextField bindField = textField(18, edit ? existing.bindAddress : "127.0.0.1");
        int port = edit ? existing.port : nextListenerPort(defaultPortForTransport(transport));
        JSpinner portSpinner = plainSpinner(Math.max(0, port), 0, 65535, 1);
        JTextField webSocketPathField = textField(18, edit ? existing.webSocketPath : DEFAULT_WEBSOCKET_PATH);
        JComboBox<String> certificateCombo = listenerCertificateCombo();
        certificateCombo.setSelectedItem(edit
                ? displayListenerCertificate(existing.certificateAlias)
                : t("listener.certificate.default"));
        JPasswordField certificatePasswordField = new JPasswordField(18);
        normalizeControlHeight(certificatePasswordField);
        // Dialog-local components set their tooltip directly; the persistent
        // tooltip registry is reserved for long-lived main-window components.
        certificatePasswordField.setToolTipText(t("listener.certificatePassword.tip"));
        if (edit) {
            certificatePasswordField.setText(existing.certificatePassword);
        }
        JComboBox<String> clientCertModeCombo = combo(CLIENT_CERT_MODE_NONE,
                CLIENT_CERT_MODE_OPTIONAL, CLIENT_CERT_MODE_REQUIRED);
        clientCertModeCombo.setToolTipText(t("listener.clientCertMode.tip"));
        if (edit) {
            clientCertModeCombo.setSelectedItem(normalizeClientCertificateMode(existing.clientCertificateMode));
        }
        JLabel endpointPreview = new JLabel(); // read-only, kept in sync by refresh below

        // Lay out the fields as label+field rows; the endpoint preview sits at the bottom.
        JPanel form = formPanel();
        GridBagConstraints gbc = formGbc();
        int row = 0;
        addDialogRow(form, gbc, row++, "listener.editor.name", nameField);
        addDialogRow(form, gbc, row++, "listener.editor.transport", transportCombo);
        addDialogRow(form, gbc, row++, "listener.editor.bind", bindField);
        addDialogRow(form, gbc, row++, "listener.editor.port", portSpinner);
        addDialogRow(form, gbc, row++, "listener.editor.wsPath", webSocketPathField);
        addDialogRow(form, gbc, row++, "listener.editor.certificate", certificateCombo);
        addDialogRow(form, gbc, row++, "listener.certificatePassword", certificatePasswordField);
        addDialogRow(form, gbc, row++, "listener.clientCertMode", clientCertModeCombo);
        addDialogRow(form, gbc, row++, "listener.editor.endpoint", endpointPreview);
        theme(form);

        // Keeps the dialog consistent with the chosen transport: the TLS-only fields
        // and the WebSocket path enable only where they apply, and the endpoint
        // preview always reflects the current transport, host, port and path.
        Runnable refresh = () -> {
            String selectedTransport = normalizeTransport(String.valueOf(transportCombo.getSelectedItem()));
            boolean secure = isSecureTransport(selectedTransport);
            boolean webSocket = isWebSocketTransport(selectedTransport);
            // Certificate, certificate password and client-certificate mode are
            // per-listener TLS settings; they stay disabled for plain TCP/WS.
            certificateCombo.setEnabled(secure);
            certificatePasswordField.setEnabled(secure);
            clientCertModeCombo.setEnabled(secure);
            webSocketPathField.setEnabled(webSocket);
            endpointPreview.setText(endpointFor(selectedTransport, bindField.getText(),
                    ((Number) portSpinner.getValue()).intValue(), webSocketPathField.getText()));
        };
        // Re-run refresh on every change that affects the preview or the enabled state,
        // and once now to initialise both.
        transportCombo.addActionListener(event -> refresh.run());
        bindField.getDocument().addDocumentListener(new SimpleDocumentListener(refresh));
        webSocketPathField.getDocument().addDocumentListener(new SimpleDocumentListener(refresh));
        portSpinner.addChangeListener(event -> refresh.run());
        refresh.run();

        // Loop so a validation error keeps the dialog open with the user's input
        // instead of discarding it.
        while (true) {
            int result = JOptionPane.showConfirmDialog(frame, form,
                    t(edit ? "listener.editor.title.edit" : "listener.editor.title.add"),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) {
                return; // Cancel or window close: leave the listener list unchanged
            }
            String name = trimText(nameField.getText());
            String selectedTransport = normalizeTransport(String.valueOf(transportCombo.getSelectedItem()));
            boolean secure = isSecureTransport(selectedTransport);
            boolean webSocket = isWebSocketTransport(selectedTransport);
            // A name is required...
            if (name.length() == 0) {
                JOptionPane.showMessageDialog(frame, t("error.listenerName"), t("status.error"), JOptionPane.ERROR_MESSAGE);
                continue;
            }
            // ...and must be unique among the other listeners.
            if (containsListenerName(name, edit ? modelRow : -1)) {
                JOptionPane.showMessageDialog(frame, t("error.listenerNameDuplicate"), t("status.error"), JOptionPane.ERROR_MESSAGE);
                continue;
            }
            // Validation passed: write every field into the target config, reusing the
            // existing one when editing or a fresh one when adding.
            ListenerConfig target = edit ? existing : new ListenerConfig();
            target.name = name;
            target.transport = selectedTransport;
            target.bindAddress = trimText(bindField.getText());
            target.port = ((Number) portSpinner.getValue()).intValue();
            // The WebSocket path only matters for WS/WSS; store the default otherwise.
            target.webSocketPath = webSocket
                    ? normalizeWebSocketPath(webSocketPathField.getText())
                    : DEFAULT_WEBSOCKET_PATH;
            // TLS-only settings are stored only for secure transports and cleared for TCP/WS.
            target.certificateAlias = secure
                    ? normalizeListenerCertificate(String.valueOf(certificateCombo.getSelectedItem()))
                    : DEFAULT_LISTENER_CERTIFICATE;
            char[] password = certificatePasswordField.getPassword();
            target.certificatePassword = secure ? new String(password) : "";
            Arrays.fill(password, '\0'); // wipe the transient password array
            target.clientCertificateMode = secure
                    ? normalizeClientCertificateMode(String.valueOf(clientCertModeCombo.getSelectedItem()))
                    : CLIENT_CERT_MODE_NONE;
            if (!edit) {
                listenerConfigs.add(target); // add-mode: append the new listener
            }
            // Refresh the table, reselect the edited/added row, and update the buttons.
            rebuildListenerRows();
            selectListenerRow(edit ? modelRow : listenerConfigs.size() - 1);
            updateActionStates();
            return;
        }
    }

    /** Adds one "label + field" row to a dialog form. */
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

    /** Normalizes a transport string to one of TCP/TLS/WS/WSS (WEBSOCKET maps to WS). */
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

    /** Conventional default port per transport (1883/8883/8080/8443). */
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

    /** The single plain-TCP listener the tool starts with. */
    private static ListenerConfig defaultTcpListenerConfig() {
        ListenerConfig config = new ListenerConfig();
        config.name = "Main TCP";
        return config;
    }

    /**
     * Re-populates the read-only listener table from the listener
     * configurations. The table shows the certificate and client-certificate
     * columns only for TLS/WSS listeners; plain TCP/WS rows stay empty there.
     */
    private void rebuildListenerRows() {
        if (listenerModel == null) {
            return;
        }
        listenerModel.setRowCount(0);
        for (ListenerConfig config : listenerConfigs) {
            boolean secure = isSecureTransport(config.transport);
            listenerModel.addRow(new Object[] {
                    config.name,
                    config.transport,
                    config.bindAddress,
                    Integer.valueOf(config.port),
                    endpointFor(config),
                    secure ? displayListenerCertificate(config.certificateAlias) : "",
                    secure ? normalizeClientCertificateMode(config.clientCertificateMode) : "",
                    "" });
        }
    }

    /** Selects the table row that shows the given listener config. */
    private void selectListenerRow(int modelRow) {
        if (listenerTable == null || modelRow < 0 || modelRow >= listenerModel.getRowCount()) {
            return;
        }
        int viewRow = listenerTable.convertRowIndexToView(modelRow);
        if (viewRow >= 0) {
            listenerTable.getSelectionModel().setSelectionInterval(viewRow, viewRow);
        }
    }

    /** Removes a listener (unless it is the last one) and reselects a neighbour. */
    private void removeListenerRow(int modelRow) {
        // At least one listener must always remain (the remove icon is disabled then).
        if (listenerConfigs.size() <= 1 || modelRow < 0 || modelRow >= listenerConfigs.size()) {
            return;
        }
        listenerConfigs.remove(modelRow);
        rebuildListenerRows();
        selectListenerRow(Math.min(modelRow, listenerConfigs.size() - 1));
        updateActionStates();
    }

    /** A unique default listener name for the transport, e.g. "TCP 1". */
    private String defaultListenerName(String transport) {
        String prefix = transport == null || transport.trim().length() == 0
                ? "Listener"
                : transport.trim();
        int suffix = 1;
        while (containsListenerName(prefix + " " + suffix, -1)) {
            suffix++;
        }
        return prefix + " " + suffix;
    }

    /** Whether another listener already uses this name (case-insensitive). */
    private boolean containsListenerName(String name, int ignoredIndex) {
        for (int index = 0; index < listenerConfigs.size(); index++) {
            if (index == ignoredIndex) {
                continue;
            }
            if (name.equalsIgnoreCase(listenerConfigs.get(index).name.trim())) {
                return true;
            }
        }
        return false;
    }

    /** The preferred port, advanced past any port already used by another listener. */
    private int nextListenerPort(int preferred) {
        int port = preferred;
        while (containsListenerPort(port) && port < 65535) {
            port++;
        }
        return port;
    }

    /** Whether any listener already binds this port. */
    private boolean containsListenerPort(int port) {
        for (ListenerConfig config : listenerConfigs) {
            if (config.port == port) {
                return true;
            }
        }
        return false;
    }

    /** True for the TLS transports (TLS/WSS). */
    private static boolean isSecureTransport(String transport) {
        String normalized = transport == null ? "" : transport.trim().toUpperCase(Locale.ROOT);
        return "TLS".equals(normalized) || "WSS".equals(normalized);
    }

    /** True for the WebSocket transports (WS/WSS). */
    private static boolean isWebSocketTransport(String transport) {
        String normalized = transport == null ? "" : transport.trim().toUpperCase(Locale.ROOT);
        return "WS".equals(normalized) || "WSS".equals(normalized);
    }

    /** Ensures a WebSocket path is non-empty and starts with a slash. */
    private static String normalizeWebSocketPath(String path) {
        String trimmed = path == null ? "" : path.trim();
        if (trimmed.length() == 0) {
            return DEFAULT_WEBSOCKET_PATH;
        }
        return trimmed.startsWith("/") ? trimmed : "/" + trimmed;
    }

    /** Normalizes a client-certificate mode to one of NONE/OPTIONAL/REQUIRED. */
    private static String normalizeClientCertificateMode(String mode) {
        String normalized = mode == null ? "" : mode.trim().toUpperCase(Locale.ROOT);
        if (CLIENT_CERT_MODE_OPTIONAL.equals(normalized) || CLIENT_CERT_MODE_REQUIRED.equals(normalized)) {
            return normalized;
        }
        return CLIENT_CERT_MODE_NONE;
    }

    /** The display endpoint URL for a listener config. */
    private String endpointFor(ListenerConfig config) {
        return endpointFor(config.transport, config.bindAddress, config.port, config.webSocketPath);
    }

    /** Composes an endpoint URL from transport, bind address, port and WebSocket path. */
    private String endpointFor(String transport, String bindAddress, int port, String webSocketPath) {
        String normalizedTransport = transport == null ? "" : transport.trim().toUpperCase(Locale.ROOT);
        String host = bindAddress == null || bindAddress.trim().length() == 0
                ? "127.0.0.1"
                : bindAddress.trim();
        String displayPort = port >= 0 ? String.valueOf(port) : "?";
        String path = normalizeWebSocketPath(webSocketPath);
        if ("TLS".equals(normalizedTransport)) {
            return "mqtts://" + host + ":" + displayPort;
        }
        if ("WS".equals(normalizedTransport) || "WEBSOCKET".equals(normalizedTransport)) {
            return "ws://" + host + ":" + displayPort + path;
        }
        if ("WSS".equals(normalizedTransport)) {
            return "wss://" + host + ":" + displayPort + path;
        }
        return "mqtt://" + host + ":" + displayPort;
    }

    /** Normalizes a certificate selection to an alias, or "" for the default identity. */
    private String normalizeListenerCertificate(String certificateAlias) {
        String alias = trimOrNull(certificateAlias);
        if (alias == null || isDefaultCertificateText(alias)) {
            return DEFAULT_LISTENER_CERTIFICATE;
        }
        return alias;
    }

    /** Whether a certificate label means "use the default broker identity". */
    private boolean isDefaultCertificateText(String value) {
        if (value == null) {
            return true;
        }
        String normalized = value.trim();
        return normalized.length() == 0
                || "-".equals(normalized)
                || "Default".equalsIgnoreCase(normalized)
                || "Standard".equalsIgnoreCase(normalized)
                || t("listener.certificate.default").equalsIgnoreCase(normalized);
    }

    /**
     * Translates the listener configurations into SDK
     * {@link PlccomMqttBrokerListenerOptions}, building TLS options (and the default
     * broker certificate) where needed. Throws when a required field is empty or a
     * port is invalid.
     */
    private List<PlccomMqttBrokerListenerOptions> configuredListenerOptions(
            List<ListenerConfig> configs, boolean requireUserCertificate) throws Exception {
        if (configs.isEmpty()) {
            throw new IllegalArgumentException(t("error.listenerRequired"));
        }
        List<PlccomMqttBrokerListenerOptions> listeners = new ArrayList<PlccomMqttBrokerListenerOptions>();
        List<String> defaultCertificateHosts = defaultCertificateHosts(configs);
        PlccomMqttPkiStore pkiStore = null;
        for (int index = 0; index < configs.size(); index++) {
            ListenerConfig config = configs.get(index);
            String name = requireListenerText(config.name, index);
            String transport = normalizeTransport(config.transport);
            String bindAddress = requireListenerText(config.bindAddress, index);
            int port = requireListenerPort(config.port, index);
            if ("TCP".equals(transport)) {
                listeners.add(PlccomMqttBrokerListenerOptions.tcp(name, bindAddress, port));
            } else if ("WS".equals(transport)) {
                listeners.add(PlccomMqttBrokerListenerOptions.webSocket(name, bindAddress, port));
            } else {
                if (pkiStore == null) {
                    pkiStore = createPkiStore();
                }
                PlccomMqttBrokerTlsOptions tlsOptions =
                        createListenerTlsOptions(pkiStore, config, requireUserCertificate, defaultCertificateHosts);
                listeners.add("TLS".equals(transport)
                        ? PlccomMqttBrokerListenerOptions.tls(name, bindAddress, port, tlsOptions)
                        : PlccomMqttBrokerListenerOptions.secureWebSocket(name, bindAddress, port, tlsOptions));
            }
        }
        return listeners;
    }

    /** The distinct bind hosts of the TLS listeners that use the default certificate. */
    private List<String> defaultCertificateHosts(List<ListenerConfig> configs) {
        ArrayList<String> hosts = new ArrayList<String>();
        for (ListenerConfig config : configs) {
            String transport = normalizeTransport(config.transport);
            if (!isSecureTransport(transport)
                    || normalizeListenerCertificate(config.certificateAlias).length() != 0) {
                continue;
            }
            String host = PlccomMqttCertificateNames.normalizeHost(config.bindAddress);
            if (host.length() == 0) {
                continue;
            }
            boolean exists = false;
            for (String current : hosts) {
                if (current.equalsIgnoreCase(host)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                hosts.add(host);
            }
        }
        return hosts;
    }

    /**
     * Builds the per-listener TLS options: server certificate, certificate
     * password and client-certificate mode all belong to the individual
     * listener, so two TLS ports can use different certificates and different
     * mTLS policies at the same time.
     */
    private PlccomMqttBrokerTlsOptions createListenerTlsOptions(PlccomMqttPkiStore pkiStore,
            ListenerConfig config, boolean requireUserCertificate,
            List<String> defaultCertificateHosts) throws Exception {
        String configuredAlias = normalizeListenerCertificate(config.certificateAlias);
        boolean usesDefaultCertificate = configuredAlias.length() == 0;
        String alias = usesDefaultCertificate ? DEFAULT_OWN_CERTIFICATE_ALIAS : configuredAlias;
        char[] password = config.certificatePassword == null
                ? new char[0]
                : config.certificatePassword.toCharArray();
        try {
            PlccomMqttOwnCertificate ownCertificate = createOwnCertificate(pkiStore, alias, password);
            KeyStore keyStore = usesDefaultCertificate
                    ? ownCertificate.loadOrCreateForServerHosts(defaultCertificateHosts)
                    : ownCertificate.loadOrCreate();
            if (!usesDefaultCertificate) {
                warnIfExplicitListenerCertificateDoesNotMatch(config, keyStore);
            }
            return PlccomMqttBrokerTlsOptions.builder()
                    .keyStore(keyStore, password)
                    .enabledProtocols("TLSv1.3", "TLSv1.2")
                    .clientCertificateMode(effectiveClientCertificateMode(config, requireUserCertificate))
                    .pkiOptions(PlccomMqttPkiOptions.builder()
                            .pkiStore(pkiStore)
                            .build())
                    .build();
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    /** Logs a warning when an explicitly chosen certificate does not match the listener host. */
    private void warnIfExplicitListenerCertificateDoesNotMatch(ListenerConfig config, KeyStore keyStore) {
        String host = PlccomMqttCertificateNames.normalizeHost(config.bindAddress);
        if (host.length() == 0 || "0.0.0.0".equals(host) || "::".equals(host)
                || "0:0:0:0:0:0:0:0".equals(host)) {
            return;
        }
        try {
            X509Certificate certificate = firstCertificate(keyStore);
            if (certificate != null && !PlccomMqttCertificateNames.matchesHost(certificate, host)) {
                logWarningText("Configured certificate for listener " + config.name
                        + " does not match endpoint host " + host
                        + ". The explicitly assigned certificate is used anyway.");
            }
        } catch (Exception ex) {
            logWarningText("Configured certificate for listener " + config.name
                    + " could not be checked against endpoint host " + host
                    + ". The explicitly assigned certificate is used anyway. Cause: " + shortCause(ex));
        }
    }

    /** The first X.509 certificate in a keystore, or null. */
    private static X509Certificate firstCertificate(KeyStore keyStore) throws GeneralSecurityException {
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            java.security.cert.Certificate certificate = keyStore.getCertificate(alias);
            if (certificate instanceof X509Certificate) {
                return (X509Certificate) certificate;
            }
        }
        return null;
    }

    /**
     * Maps a listener's client-certificate mode to the SDK value, upgrading NONE to
     * OPTIONAL when "Require user certificate" is active so the access validator can
     * deny CONNECT cleanly instead of failing the TLS handshake.
     */
    private PlccomMqttBrokerClientCertificateMode effectiveClientCertificateMode(
            ListenerConfig config, boolean requireUserCertificate) {
        String mode = normalizeClientCertificateMode(config.clientCertificateMode);
        if (CLIENT_CERT_MODE_REQUIRED.equals(mode)) {
            return PlccomMqttBrokerClientCertificateMode.REQUIRED;
        }
        if (CLIENT_CERT_MODE_OPTIONAL.equals(mode)) {
            return PlccomMqttBrokerClientCertificateMode.OPTIONAL;
        }
        if (requireUserCertificate) {
            // "Require user certificate" is an access rule, not a TLS rule. With
            // listener mode NONE the handshake would never transport a client
            // certificate, so the certificate is requested optionally. The access
            // validator can then deny CONNECT cleanly with MQTT reason code 0x87
            // instead of a hard TLS failure.
            logInfoText("Listener '" + config.name + "': client certificate mode NONE is used as"
                    + " OPTIONAL because 'Require user certificate' is active.");
            return PlccomMqttBrokerClientCertificateMode.OPTIONAL;
        }
        return PlccomMqttBrokerClientCertificateMode.NONE;
    }

    /** Returns the trimmed value, or throws a localized error naming the listener row. */
    private String requireListenerText(String value, int index) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException(t("error.listenerCell") + " " + (index + 1));
        }
        return text;
    }

    /** Returns the port when it is in range, or throws a localized error naming the row. */
    private int requireListenerPort(int port, int index) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException(t("error.listenerPort") + " " + (index + 1));
        }
        return port;
    }

    /**
     * Per-listener configuration edited in the listener editor dialog. TLS
     * fields (certificate alias, certificate password, client-certificate
     * mode) only apply to TLS/WSS listeners; the WebSocket path only applies
     * to WS/WSS listeners.
     */
    private static final class ListenerConfig {
        String name = "";
        String transport = "TCP";
        String bindAddress = "127.0.0.1";
        int port = 1883;
        String webSocketPath = DEFAULT_WEBSOCKET_PATH;
        String certificateAlias = DEFAULT_LISTENER_CERTIFICATE;
        String certificatePassword = "";
        String clientCertificateMode = CLIENT_CERT_MODE_NONE;
    }

    /** Builds the Admin/Statistics tab: a read-only key/value table with refresh and reset. */
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

    /** Wraps the shared {@link LogPanel} in a fixed-height container below the tabs. */
    private Component createLogPanel() {
        logPanel = new LogPanel();
        JPanel outer = new JPanel(new BorderLayout(0, 0));
        outer.setPreferredSize(new Dimension(300, 210));
        outer.add(logPanel, BorderLayout.CENTER);
        return outer;
    }

    /** Pushes the currently selected diagnostic level to the running broker, if any. */
    private void applyRuntimeDiagnosticLevel() {
        // The selected level drives both consumers: the tool's own log filter
        // (always, even without a running broker) and the live SDK broker.
        uiLogLevel = selectedDiagnosticLevel(diagnosticLevelCombo);
        PlccomMqttBroker current = broker;
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
     * Starts the broker with the settings currently shown in the UI. Every widget
     * value is snapshotted on the Swing thread first so the worker thread never
     * touches Swing; a previous broker is replaced cleanly and, when any access rule
     * is set, an access validator is installed. Runs on the worker thread.
     */
    private void startBroker() {
        PlccomMqttBroker current = broker;
        if (current != null && current.isRunning()) {
            logInfo("log.already.running");
            updateActionStates();
            return;
        }
        // Snapshot everything on the EDT so the worker thread never reads
        // Swing components. The access validator itself also works on this
        // snapshot: changing a checkbox later has no effect until restart.
        final List<ListenerConfig> configs = new ArrayList<ListenerConfig>(listenerConfigs);
        final boolean requireUserName = requireUsernameCheck.isSelected();
        final boolean requireUserCertificate = requireClientCertificateCheck.isSelected();
        final String blockedTopicPrefix = trimOrNull(blockedTopicPrefixField);
        final String accessUsersText = accessUsersArea.getText();
        final String licenseUser = trim(licenseUserField);
        final String licenseSerial = trim(licenseSerialField);
        final int maxConnections = number(maxConnectionsSpinner);
        final int socketReadTimeout = number(socketReadTimeoutSpinner);
        final PlccomMqttDiagnosticLevel diagnosticLevel = selectedDiagnosticLevel(diagnosticLevelCombo);
        runAsync("startBroker", () -> {
            // Starting again should replace the previous broker cleanly. This
            // avoids accidental port conflicts and keeps every test run tied to
            // the listener settings currently visible in the UI.
            closeBrokerQuietly();

            // The users list only takes part in the access decision while
            // "Require user name" is active. An unused list must never block a
            // connection.
            Map<String, byte[]> configuredAccessUsers = requireUserName
                    ? BrokerToolAccessValidator.parseAccessUsers(accessUsersText)
                    : new LinkedHashMap<String, byte[]>();
            boolean accessValidatorEnabled = requireUserName || requireUserCertificate
                    || blockedTopicPrefix != null;

            // License validation deliberately happens at the real product
            // boundary: creating/starting the broker. If the user or serial number
            // is wrong, the exception is shown in the tool log.
            PlccomMqttBrokerBuilder builder = PlccomMqttBrokerFactory
                    .builder(licenseUser, licenseSerial)
                    .listeners(configuredListenerOptions(configs, requireUserCertificate))
                    .maxConnections(maxConnections)
                    .socketReadTimeoutMillis(socketReadTimeout)
                    .diagnosticLevel(diagnosticLevel)
                    .onDiagnostic(this::diagnostic);
            if (accessValidatorEnabled) {
                logInfoText("Access validator enabled. requireUserName=" + requireUserName
                        + ", configuredUsers=" + configuredAccessUsers.size()
                        + ", requireClientCertificate=" + requireUserCertificate
                        + ", blockedTopicPrefix="
                        + (blockedTopicPrefix == null ? "none" : blockedTopicPrefix));
                builder.accessValidator(new BrokerToolAccessValidator(requireUserName,
                        configuredAccessUsers, requireUserCertificate, blockedTopicPrefix,
                        new BrokerToolAccessValidator.LogSink() {
                            @Override
                            public void info(String message) {
                                logInfoText(message);
                            }

                            @Override
                            public void warning(String message) {
                                logWarningText(message);
                            }
                        }));
            } else {
                logInfoText("Access validator disabled. All clients are allowed.");
                builder.allowAnonymous();
            }
            PlccomMqttBroker created = builder.start();
            broker = created;
            setStatus("status.running", Status.SUCCESS);
            logSuccess("log.started", created.getBoundPorts());
            logListenerEndpoints(configs);
            refreshStatisticsDirect();
        });
    }

    /** Stops the running broker and returns the tool to its idle state. */
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

    /** Refreshes the admin snapshot and logs a short summary. */
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

    /** Resets the broker's diagnostic counters and refreshes the statistics. */
    private void resetCounters() {
        runAsync("resetCounters", () -> {
            requireBroker().admin().resetCounters();
            logInfo("log.counters.reset");
            refreshStatisticsDirect();
        });
    }

    /**
     * Reads the current admin snapshot and repopulates the statistics table on the
     * Swing thread. Returns the snapshot, or null when the broker is not running.
     */
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

    /** Adds one name/value row to the statistics table. */
    private void addStatistic(String name, Object value) {
        statisticsModel.addRow(new Object[] { name, String.valueOf(value) });
    }

    /** Returns the running broker, or throws with a readable message if it is stopped. */
    private PlccomMqttBroker requireBroker() throws PlccomMqttException {
        PlccomMqttBroker current = broker;
        if (current == null || !current.isRunning()) {
            throw new PlccomMqttException(t("error.notRunning"));
        }
        return current;
    }

    /**
     * Returns the result when the operation succeeded, otherwise throws a
     * {@link PlccomMqttException} built from its quality and localized message.
     */
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

    /** Closes the current broker without surfacing errors. Used on restart, stop and shutdown. */
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

    /**
     * Releases everything on window close: persists the settings once, closes the
     * broker and shuts the worker thread down.
     */
    private void shutdown() {
        if (settingsPersistenceEnabled && !settingsSaved) {
            settingsSaved = true;
            saveSettings();
        }
        closeBrokerQuietly();
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
                    "PLCcom MQTT Broker Tool user settings. Sensitive values use AES/GCM encryption.");
        } catch (IOException ignored) {
            // Settings persistence is a convenience feature. A failure here must
            // never prevent the user application from closing cleanly.
        }
    }

    /** Applies every saved field value from the given settings back into the UI widgets. */
    private void loadToolSettings(Properties settings) {
        setProtectedText(settings, "license.user", licenseUserField);
        setProtectedText(settings, "license.serial", licenseSerialField);
        loadListenerSettings(settings);
        setCheck(settings, "access.requireUsername", requireUsernameCheck);
        setCheck(settings, "access.requireClientCertificate", requireClientCertificateCheck);
        setText(settings, "access.blockedTopicPrefix", blockedTopicPrefixField);
        setProtectedText(settings, "access.users", accessUsersArea);
        migrateLegacyAccessSettings(settings);
        setSpinner(settings, "runtime.maxConnections", maxConnectionsSpinner);
        setSpinner(settings, "runtime.socketReadTimeout", socketReadTimeoutSpinner);
        setCombo(settings, "runtime.diagnosticLevel", diagnosticLevelCombo);
        String maxLinesValue = settings.getProperty("log.maxLines");
        if (maxLinesValue != null && logPanel != null) {
            try {
                logPanel.setMaxLines(Integer.parseInt(maxLinesValue.trim()));
            } catch (NumberFormatException ignored) {
                // Keep the default maximum on an obsolete or edited value.
            }
        }
    }

    /**
     * Maps settings written by earlier tool versions onto the current access
     * model without silently activating rules the user never opted into.
     *
     * <ul>
     * <li>"Validate configured users" implied a user-name requirement, so it
     * maps to "Require user name".</li>
     * <li>The removed "Enable demo validator" flag is dropped. Old versions
     * always stored a blocked-prefix text (default {@code blocked/}) even when
     * no access rule was active; in that case the prefix is cleared so
     * upgrading never suddenly starts blocking topics.</li>
     * </ul>
     */
    private void migrateLegacyAccessSettings(Properties settings) {
        boolean legacy = settings.containsKey("access.demoValidator")
                || settings.containsKey("access.validateUsers");
        if (!legacy) {
            return;
        }
        if (booleanSetting(settings, "access.validateUsers", false)) {
            requireUsernameCheck.setSelected(true);
        }
        boolean anyLegacyRuleActive = booleanSetting(settings, "access.demoValidator", false)
                || booleanSetting(settings, "access.requireUsername", false)
                || booleanSetting(settings, "access.validateUsers", false)
                || booleanSetting(settings, "access.requireClientCertificate", false);
        if (!anyLegacyRuleActive) {
            blockedTopicPrefixField.setText("");
        }
    }

    /** Writes every field value into the given settings; sensitive fields are encrypted. */
    private void saveToolSettings(Properties settings) {
        putProtectedText(settings, "license.user", licenseUserField);
        putProtectedText(settings, "license.serial", licenseSerialField);
        saveListenerSettings(settings);
        putCheck(settings, "access.requireUsername", requireUsernameCheck);
        putCheck(settings, "access.requireClientCertificate", requireClientCertificateCheck);
        putText(settings, "access.blockedTopicPrefix", blockedTopicPrefixField);
        putProtectedText(settings, "access.users", accessUsersArea);
        putSpinner(settings, "runtime.maxConnections", maxConnectionsSpinner);
        putSpinner(settings, "runtime.socketReadTimeout", socketReadTimeoutSpinner);
        putCombo(settings, "runtime.diagnosticLevel", diagnosticLevelCombo);
        if (logPanel != null) {
            settings.setProperty("log.maxLines", String.valueOf(logPanel.getMaxLines()));
        }
    }

    /**
     * Restores the listener list from settings, understanding both the current
     * per-listener format and the older single-certificate/single-mode layout, which
     * it carries over to each TLS/WSS listener once.
     */
    private void loadListenerSettings(Properties settings) {
        listenerConfigs.clear();
        int count = integerSetting(settings, "listener.count", -1);
        if (count > 0) {
            // Earlier versions stored one global certificate password and one
            // global client-certificate mode. Both moved to the listener level;
            // existing values are carried over to every TLS/WSS listener once.
            String legacyPassword = settings.containsKey("listener.certificatePassword")
                    ? decryptSetting(settings.getProperty("listener.certificatePassword"))
                    : null;
            String legacyMode = legacyGlobalClientCertificateMode(settings);
            for (int index = 0; index < count; index++) {
                String prefix = "listener." + index + ".";
                ListenerConfig config = new ListenerConfig();
                config.transport = normalizeTransport(settings.getProperty(prefix + "transport", "TCP"));
                config.name = settings.getProperty(prefix + "name", defaultListenerName(config.transport));
                config.bindAddress = settings.getProperty(prefix + "bind", "127.0.0.1");
                config.port = integerSetting(settings, prefix + "port", 1883);
                config.webSocketPath = normalizeWebSocketPath(
                        settings.getProperty(prefix + "wsPath", DEFAULT_WEBSOCKET_PATH));
                boolean secure = isSecureTransport(config.transport);
                if (secure) {
                    config.certificateAlias = normalizeListenerCertificate(
                            settings.getProperty(prefix + "certificate", DEFAULT_LISTENER_CERTIFICATE));
                    String password = settings.getProperty(prefix + "certificatePassword");
                    if (password != null) {
                        config.certificatePassword = decryptSetting(password);
                    } else if (legacyPassword != null) {
                        config.certificatePassword = legacyPassword;
                    }
                    String mode = settings.getProperty(prefix + "clientCertificateMode");
                    if (mode != null) {
                        config.clientCertificateMode = normalizeClientCertificateMode(mode);
                    } else if (legacyMode != null) {
                        config.clientCertificateMode = legacyMode;
                    }
                }
                listenerConfigs.add(config);
            }
            rebuildListenerRows();
            return;
        }
        String bind = settings.getProperty("listener.bindAddress", "127.0.0.1");
        listenerConfigs.add(legacyListenerConfig("Main TCP", "TCP", bind,
                integerSetting(settings, "listener.tcpPort", 1883)));
        if (booleanSetting(settings, "listener.secondTcp", false)) {
            listenerConfigs.add(legacyListenerConfig("Second TCP", "TCP", bind,
                    integerSetting(settings, "listener.secondTcpPort", 1884)));
        }
        if (booleanSetting(settings, "listener.webSocket", false)) {
            listenerConfigs.add(legacyListenerConfig("WebSocket", "WS", bind,
                    integerSetting(settings, "listener.webSocketPort", 8080)));
        }
        if (booleanSetting(settings, "listener.tls", false)) {
            listenerConfigs.add(legacyListenerConfig("TLS", "TLS", bind,
                    integerSetting(settings, "listener.tlsPort", 8883)));
        }
        if (booleanSetting(settings, "listener.secureWebSocket", false)) {
            listenerConfigs.add(legacyListenerConfig("WSS", "WSS", bind,
                    integerSetting(settings, "listener.secureWebSocketPort", 8443)));
        }
        rebuildListenerRows();
    }

    /** Builds a listener config from the older flat settings layout. */
    private static ListenerConfig legacyListenerConfig(String name, String transport,
            String bindAddress, int port) {
        ListenerConfig config = new ListenerConfig();
        config.name = name;
        config.transport = transport;
        config.bindAddress = bindAddress;
        config.port = port;
        return config;
    }

    /** Reads the removed global client-certificate mode (0/1/2) from old settings. */
    private static String legacyGlobalClientCertificateMode(Properties settings) {
        String value = settings.getProperty("listener.clientCertificateMode");
        if (value == null) {
            return null;
        }
        if ("1".equals(value.trim())) {
            return CLIENT_CERT_MODE_OPTIONAL;
        }
        if ("2".equals(value.trim())) {
            return CLIENT_CERT_MODE_REQUIRED;
        }
        return CLIENT_CERT_MODE_NONE;
    }

    /** Writes the listener list to settings, one indexed block per listener. */
    private void saveListenerSettings(Properties settings) {
        settings.setProperty("listener.count", String.valueOf(listenerConfigs.size()));
        for (int index = 0; index < listenerConfigs.size(); index++) {
            ListenerConfig config = listenerConfigs.get(index);
            String prefix = "listener." + index + ".";
            settings.setProperty(prefix + "name", config.name);
            settings.setProperty(prefix + "transport", config.transport);
            settings.setProperty(prefix + "bind", config.bindAddress);
            settings.setProperty(prefix + "port", String.valueOf(config.port));
            settings.setProperty(prefix + "wsPath", config.webSocketPath);
            settings.setProperty(prefix + "certificate",
                    normalizeListenerCertificate(config.certificateAlias));
            settings.setProperty(prefix + "certificatePassword",
                    encryptSetting(config.certificatePassword));
            settings.setProperty(prefix + "clientCertificateMode",
                    normalizeClientCertificateMode(config.clientCertificateMode));
        }
    }

    /** Reads an int setting, falling back on a missing or malformed value. */
    private int integerSetting(Properties settings, String key, int fallback) {
        try {
            return Integer.parseInt(settings.getProperty(key, String.valueOf(fallback)).trim());
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    /** Reads a boolean setting, falling back on a missing value. */
    private boolean booleanSetting(Properties settings, String key, boolean fallback) {
        String value = settings.getProperty(key);
        return value == null ? fallback : Boolean.parseBoolean(value);
    }

    /**
     * Runs a button action on the background worker thread, guarding against
     * overlapping operations and funnelling every failure into the log while keeping
     * the window responsive. Also drives the status pill (busy/ready/running/error).
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

    /** Builds the tool's own certificate for the given alias (localhost + loopback SANs). */
    private PlccomMqttOwnCertificate createOwnCertificate(PlccomMqttPkiStore pkiStore,
            String alias, char[] password) throws Exception {
        return new PlccomMqttOwnCertificate(
                pkiStore, alias, password, 3650, "Indi.An GmbH",
                Arrays.asList("localhost"),
                Arrays.asList(InetAddress.getByName("127.0.0.1"),
                        InetAddress.getByName("::1")));
    }

    /** Logs each configured listener's endpoint, plus certificate/mode for TLS listeners. */
    private void logListenerEndpoints(List<ListenerConfig> configs) {
        for (ListenerConfig config : configs) {
            StringBuilder line = new StringBuilder("Listener ")
                    .append(config.name)
                    .append(": ")
                    .append(endpointFor(config));
            if (isSecureTransport(config.transport)) {
                String alias = normalizeListenerCertificate(config.certificateAlias);
                line.append(", certificate=")
                        .append(alias.length() == 0 ? DEFAULT_OWN_CERTIFICATE_ALIAS : alias)
                        .append(", clientCertificateMode=")
                        .append(normalizeClientCertificateMode(config.clientCertificateMode));
            }
            logInfoText(line.toString());
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
     * Extracts the localized help page and stylesheet from the jar into a per-version
     * folder under the user profile and returns the index file to open.
     */
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

    /** Copies a classpath resource to a file, failing loudly if the resource is missing. */
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
        // The certificate manager maintains the default tool identity, which is
        // created without a password. Passwords for listener certificates are
        // configured per listener in the listener editor.
        try {
            PlccomMqttCertificateManagerDialog.show(frame, texts, locale,
                    createPkiStore(), DEFAULT_OWN_CERTIFICATE_ALIAS, new char[0],
                    component -> theme(component));
        } catch (Throwable ex) {
            logError("log.operation.failed", "certificates", shortCause(ex));
            logErrorText(t("log.failure.details") + " " + detailedCause(ex));
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

    /** SHA-1 thumbprint of a certificate, or "unknown" if it cannot be computed. */
    private static String certificateFingerprint(X509Certificate certificate) {
        try {
            return PlccomMqttPkiStore.sha1Thumbprint(certificate);
        } catch (Exception ex) {
            return "unknown";
        }
    }

    /** Diagnostic callback from the broker: renders the event at a matching log level. */
    private void diagnostic(PlccomMqttDiagnosticEvent event) {
        logPanel.append(logLevelFor(event.getSeverity()), formatDiagnostic(event));
        if (LICENSE_END_EVENT_CODE.equals(event.getCode())) {
            handleLicenseEnded();
        }
    }

    /**
     * The library has halted the broker on its own; mirror that in the header.
     * Without this the status pill keeps showing Running while the log already
     * explains why no client can connect any more - the same state as if the
     * user had pressed Stop.
     *
     * <p>Idempotent on purpose: a user-initiated stop around the same time
     * must not produce a second state change. setStatus and updateActionStates
     * already hop onto the Swing thread themselves; the broker handle is
     * closed on a worker thread because close() joins library threads and the
     * event arrives on one of them.</p>
     */
    private void handleLicenseEnded() {
        final PlccomMqttBroker ended = broker;
        broker = null;
        if (ended != null) {
            Thread cleanup = new Thread(ended::close, "broker-tool-license-end-cleanup");
            cleanup.setDaemon(true);
            cleanup.start();
        }
        setStatus("status.stopped", Status.ERROR);
        updateActionStates();
    }

    /** Maps a diagnostic severity to the log colour level (error/warning/info). */
    private static LogLevel logLevelFor(PlccomMqttDiagnosticSeverity severity) {
        if (severity == PlccomMqttDiagnosticSeverity.ERROR
                || severity == PlccomMqttDiagnosticSeverity.FATAL) {
            return LogLevel.ERROR;
        }
        if (severity == PlccomMqttDiagnosticSeverity.WARNING) {
            return LogLevel.WARNING;
        }
        return LogLevel.INFO;
    }

    /** Formats a diagnostic event into a single structured, filterable log line. */
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
        if (event.getCause() != null) {
            builder.append(" cause=").append(shortCause(event.getCause()));
        }
        return builder.toString();
    }

    /** Switches between the dark and light palettes and repaints. */
    private void toggleTheme() {
        palette = palette.dark ? Palette.light() : Palette.dark();
        applyTheme();
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
        updateListenerTableHeaders();
        rebuildListenerRows();
        languageButton.setText(Locale.GERMAN.getLanguage().equals(locale.getLanguage()) ? "EN" : "DE");
        setStatusTextOnly(broker != null && broker.isRunning() ? "status.running" : "status.ready");
        if (logPanel != null) {
            logPanel.translate();
        }
        updateActionStates();
        frame.revalidate();
    }

    /** Re-applies the translated column headers of the listener table. */
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
        setListenerHeader(LISTENER_CLIENT_CERTS_COLUMN, "listener.column.clientCerts");
        listenerTable.getTableHeader().repaint();
    }

    /** Sets one listener-table column header from a resource key. */
    private void setListenerHeader(int modelColumn, String key) {
        TableColumn column = listenerColumn(modelColumn);
        if (column != null) {
            column.setHeaderValue(t(key));
        }
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

    /** Refreshes button enablement, hopping onto the Swing thread if needed. */
    private void updateActionStates() {
        if (SwingUtilities.isEventDispatchThread()) {
            applyActionStates();
        } else {
            SwingUtilities.invokeLater(this::applyActionStates);
        }
    }

    /** Enables or disables and re-themes the action buttons for the running/busy state. */
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

    /** Whether a component sits inside the set-apart "Access validator" card. */
    private static boolean isInAccessCard(Component component) {
        for (Component current = component; current != null; current = current.getParent()) {
            if (current instanceof JComponent
                    && Boolean.TRUE.equals(((JComponent) current)
                            .getClientProperty("plccom.accessCard"))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Recursively applies the current palette to a component and its children.
     * Components inside the "Access validator" card get the set-apart surface tone,
     * and a few components are special-cased by name (header, footer, ...) or type.
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
        // The inner "Access validator" card sits on a set-apart surface, so every
        // component below it (labels, checkboxes, its own panel) shares that tone.
        boolean inAccessCard = isInAccessCard(component);
        component.setBackground(inAccessCard ? palette.surfaceAlt : palette.windowBg);
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
            component.setBackground(inAccessCard ? palette.surfaceAlt : palette.windowBg);
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
        gbc.insets = new Insets(3, 5, 3, 5);
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

    /** Adds a component spanning both form columns (a header or full-width control). */
    private void addWide(JPanel panel, GridBagConstraints gbc, int row, Component component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, gbc);
        gbc.gridwidth = 1;
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

    /** Wraps a listener field (plus optional trailing controls) in the standard cell. */
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

    /** Wraps a component in a fixed-size holder so listener form columns stay aligned. */
    private JComponent fixedListenerField(JComponent field) {
        JPanel holder = new JPanel(new BorderLayout());
        holder.setBorder(BorderFactory.createEmptyBorder());
        holder.add(field, BorderLayout.CENTER);
        setFixedSize(holder, LISTENER_FIELD_WIDTH, CONTROL_HEIGHT);
        return holder;
    }

    /** Pins the given controls to the standard listener field width. */
    private void applyListenerFieldWidth(JComponent... components) {
        for (JComponent component : components) {
            setFixedSize(component, LISTENER_FIELD_WIDTH, CONTROL_HEIGHT);
        }
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

    /** A numeric spinner without grouping (e.g. connection counts). */
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

    /** A bold section header with an optional help bubble, spanning a form column. */
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
                    PlccomMqttBrokerToolApp.class.getResource("/assets/PLCcom.png"));
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

    /** The trimmed value, or "" when null. */
    private static String trimText(String value) {
        return value == null ? "" : value.trim();
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

    /** The trimmed value, or {@code null} when it is empty. */
    private static String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() == 0 ? null : trimmed;
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
        String version = PlccomMqttBrokerToolApp.class.getPackage().getImplementationVersion();
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

    /** A document listener that runs the same action on any text change. */
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

    /** Listener-certificate combo renderer that shows "Default" for the default identity. */
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

    /** Combo-box popup renderer that matches the current theme. */
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
        FILE, ADD, EDIT, REMOVE, COPY, SAVE, TRASH, REFRESH, RESET, WINDOW
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

