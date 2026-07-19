package com.airmouse.server;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Premium Dark-mode Swing UI for AirMouse Server.
 */
public class AirMouseWindow extends JFrame implements ServerListener {

    // Colors
    private static final Color BG_COLOR = new Color(30, 30, 30);
    private static final Color CARD_COLOR = new Color(45, 45, 45);
    private static final Color ACCENT_COLOR = new Color(0, 120, 215);
    private static final Color TEXT_PRIMARY = new Color(240, 240, 240);
    private static final Color TEXT_MUTED = new Color(170, 170, 170);
    private static final Color STATUS_GREEN = new Color(0, 230, 118);
    private static final Color STATUS_YELLOW = new Color(255, 193, 7);
    private static final Color STATUS_RED = new Color(244, 67, 54);

    // Core Components
    private final MouseController mouseController;
    private final Server server;
    private TrayManager trayManager;

    // UI Components
    private JLabel statusLabel;
    private JLabel statusDot;
    private JLabel ipLabel;
    private JLabel portLabel;
    private JLabel connectionLabel;
    private JLabel deviceLabel;
    private JTextArea logArea;
    private JSlider sensitivitySlider;
    private JLabel sensitivityLabel;
    
    private String currentIp = "Unknown";
    private int logCount = 0;
    private static final int MAX_LOGS = 100;

    public AirMouseWindow(MouseController mouseController, Server server) {
        super("AirMouse Server");
        this.mouseController = mouseController;
        this.server = server;
        setupUI();
    }

    public void setTrayManager(TrayManager trayManager) {
        this.trayManager = trayManager;
    }

    private void setupUI() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (trayManager != null) {
                    setVisible(false);
                } else {
                    System.exit(0);
                }
            }
        });

        setSize(560, 680); // Adjusted height for logging area
        setResizable(false);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_COLOR);

        try {
            java.net.URL imgUrl = getClass().getResource("/icons/AirMouse.png");
            if (imgUrl != null) {
                setIconImage(new ImageIcon(imgUrl).getImage());
            }
        } catch (Exception e) {
            // ignore
        }

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(25, 30, 20, 30));

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(BG_COLOR);
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel titleLabel = new JLabel("AirMouse Server");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subTitleLabel = new JLabel("Wireless Mouse Receiver");
        subTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subTitleLabel.setForeground(TEXT_MUTED);
        subTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(subTitleLabel);
        
        mainPanel.add(headerPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        // Status Card
        JPanel statusCard = createCardPanel(45);
        statusCard.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        statusDot = new JLabel("●");
        statusDot.setFont(new Font("Segoe UI", Font.BOLD, 18));
        statusDot.setForeground(STATUS_RED);
        statusLabel = new JLabel("Disconnected");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        statusLabel.setForeground(TEXT_PRIMARY);
        statusCard.add(statusDot);
        statusCard.add(statusLabel);
        mainPanel.add(statusCard);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Info Grid
        JPanel infoGrid = new JPanel(new GridLayout(2, 2, 15, 15));
        infoGrid.setBackground(BG_COLOR);
        infoGrid.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoGrid.setMaximumSize(new Dimension(500, 120));

        ipLabel = new JLabel("Detecting...");
        portLabel = new JLabel("5000");
        connectionLabel = new JLabel("Detecting...");
        deviceLabel = new JLabel("None");

        infoGrid.add(createLabeledCard("Desktop IP", ipLabel));
        infoGrid.add(createLabeledCard("Port", portLabel));
        infoGrid.add(createLabeledCard("Connection", connectionLabel));
        infoGrid.add(createLabeledCard("Connected Device", deviceLabel));
        mainPanel.add(infoGrid);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Sensitivity Card
        JPanel sensCardWrapper = new JPanel();
        sensCardWrapper.setLayout(new BoxLayout(sensCardWrapper, BoxLayout.Y_AXIS));
        sensCardWrapper.setBackground(BG_COLOR);
        sensCardWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        sensCardWrapper.setMaximumSize(new Dimension(500, 80));

        JLabel sensTitle = new JLabel("Cursor Sensitivity");
        sensTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sensTitle.setForeground(TEXT_MUTED);
        sensTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel sensCard = createCardPanel(60);
        sensCard.setLayout(new BorderLayout(15, 0));
        sensCard.setBorder(new EmptyBorder(10, 20, 10, 20));

        double initialSens = SettingsManager.getSensitivity();
        sensitivityLabel = new JLabel(String.format("%.1fx", initialSens));
        sensitivityLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sensitivityLabel.setForeground(TEXT_PRIMARY);
        
        // Slider 5 to 30 => 0.5x to 3.0x
        sensitivitySlider = new JSlider(5, 30, (int)(initialSens * 10));
        sensitivitySlider.setBackground(CARD_COLOR);
        sensitivitySlider.addChangeListener(e -> {
            double val = sensitivitySlider.getValue() / 10.0;
            sensitivityLabel.setText(String.format("%.1fx", val));
            mouseController.setSensitivity(val);
            SettingsManager.setSensitivity(val);
        });

        sensCard.add(sensitivitySlider, BorderLayout.CENTER);
        sensCard.add(sensitivityLabel, BorderLayout.EAST);
        
        sensCardWrapper.add(sensTitle);
        sensCardWrapper.add(Box.createRigidArea(new Dimension(0, 5)));
        sensCardWrapper.add(sensCard);
        
        mainPanel.add(sensCardWrapper);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Auto Start Toggle (placed compactly above buttons)
        JCheckBox startupCheck = new JCheckBox("Start with Windows");
        startupCheck.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        startupCheck.setForeground(TEXT_PRIMARY);
        startupCheck.setBackground(BG_COLOR);
        startupCheck.setFocusPainted(false);
        startupCheck.setAlignmentX(Component.CENTER_ALIGNMENT);
        startupCheck.setSelected(SettingsManager.isAutoStart());
        startupCheck.addActionListener(e -> toggleStartup(startupCheck.isSelected()));
        // Ensure it's correctly applied on boot if expected
        if(SettingsManager.isAutoStart() != getStartupShortcutFile().exists()) {
             toggleStartup(SettingsManager.isAutoStart());
        }

        mainPanel.add(startupCheck);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnPanel.setBackground(BG_COLOR);
        
        JButton copyBtn = createStyledButton("Copy IP", ACCENT_COLOR);
        copyBtn.addActionListener(e -> {
            StringSelection selection = new StringSelection(currentIp + ":" + portLabel.getText());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
            copyBtn.setText("Copied!");
            Timer timer = new Timer(2000, evt -> copyBtn.setText("Copy IP"));
            timer.setRepeats(false);
            timer.start();
        });

        JButton restartBtn = createStyledButton("Restart Server", new Color(70, 70, 70));
        restartBtn.addActionListener(e -> server.restartAsync());

        JButton hideBtn = createStyledButton("Hide to Tray", new Color(70, 70, 70));
        hideBtn.addActionListener(e -> setVisible(false));

        btnPanel.add(copyBtn);
        btnPanel.add(restartBtn);
        btnPanel.add(hideBtn);
        btnPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(btnPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Log Area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(20, 20, 20));
        logArea.setForeground(new Color(150, 200, 150));
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 50)));
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        scrollPane.setMaximumSize(new Dimension(500, 100));
        scrollPane.setPreferredSize(new Dimension(500, 100));
        
        // Smart scrolling to bottom
        scrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
            e.getAdjustable().setValue(e.getAdjustable().getMaximum());
        });

        mainPanel.add(scrollPane);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Footer
        JLabel footerLabel = new JLabel("AirMouse v1.0");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerLabel.setForeground(new Color(100, 100, 100));
        footerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(footerLabel);

        add(mainPanel);
    }

    private JPanel createLabeledCard(String title, JLabel valueLabel) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(BG_COLOR);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(TEXT_MUTED);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel card = createCardPanel(45);
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        valueLabel.setForeground(TEXT_PRIMARY);
        card.add(valueLabel);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        wrapper.add(titleLbl);
        wrapper.add(Box.createRigidArea(new Dimension(0, 4)));
        wrapper.add(card);

        return wrapper;
    }

    private JPanel createCardPanel(int height) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_COLOR);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(220, height));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        return panel;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 35));
        return btn;
    }

    private File getStartupShortcutFile() {
        String startupDir = System.getenv("APPDATA") + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
        return new File(startupDir, "AirMouseServer.vbs");
    }

    private void toggleStartup(boolean enable) {
        SettingsManager.setAutoStart(enable);
        File shortcutFile = getStartupShortcutFile();
        if (enable) {
            try {
                String currentDir = System.getProperty("user.dir");
                String targetPath = new File(currentDir, "AirMouse Server.exe").getAbsolutePath();
                if (!new File(targetPath).exists()) {
                    targetPath = "java -jar " + new File(currentDir, "target\\airmouse-server-1.0.0-RC1.jar").getAbsolutePath();
                }

                try (FileWriter fw = new FileWriter(shortcutFile)) {
                    fw.write("Set WshShell = CreateObject(\"WScript.Shell\")\n");
                    if (targetPath.startsWith("java ")) {
                        fw.write("WshShell.Run \"cmd /c " + targetPath + "\", 0\n");
                    } else {
                        fw.write("WshShell.Run \"\"\"" + targetPath + "\"\"\", 1\n");
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            if (shortcutFile.exists()) {
                shortcutFile.delete();
            }
        }
    }

    // --- ServerListener Implementation ---

    @Override
    public void onServerStarted(String ip, int port) {
        SwingUtilities.invokeLater(() -> {
            this.currentIp = ip;
            ipLabel.setText(ip);
            portLabel.setText(String.valueOf(port));
            statusLabel.setText("Waiting for Connection");
            statusDot.setForeground(STATUS_YELLOW);
            
            NetworkUtils.NetworkInfo info = NetworkUtils.detect();
            connectionLabel.setText(info.connectionType + " (TCP)");
        });
    }

    @Override
    public void onClientConnected(String clientAddress) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Connected");
            statusDot.setForeground(STATUS_GREEN);
            deviceLabel.setText(clientAddress);
        });
    }

    @Override
    public void onClientDisconnected() {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Waiting for Connection");
            statusDot.setForeground(STATUS_YELLOW);
            deviceLabel.setText("None");
        });
    }

    @Override
    public void onLogMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logCount++;
            if (logCount > MAX_LOGS) {
                try {
                    int endOfFirstLine = logArea.getLineEndOffset(0);
                    logArea.replaceRange("", 0, endOfFirstLine);
                    logCount--;
                } catch (Exception e) {
                    // ignore text area offset errors
                }
            }
        });
    }
}
