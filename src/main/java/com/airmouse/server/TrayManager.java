package com.airmouse.server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Manages the System Tray icon and its interactions.
 */
public class TrayManager {

    private final AirMouseWindow window;
    private final Server server;

    public TrayManager(AirMouseWindow window, Server server) {
        this.window = window;
        this.server = server;
    }

    public void setupTray() {
        if (!SystemTray.isSupported()) {
            System.err.println("System tray not supported!");
            return;
        }

        SystemTray tray = SystemTray.getSystemTray();
        
        Image image = null;
        try {
            java.net.URL imgUrl = getClass().getResource("/icons/AirMouse.png");
            if (imgUrl != null) {
                // Scale icon for tray (usually 16x16)
                ImageIcon icon = new ImageIcon(imgUrl);
                image = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            }
        } catch (Exception e) {
            // fallback to creating a simple colored circle
            image = createFallbackTrayIcon();
        }

        if (image == null) {
            image = createFallbackTrayIcon();
        }

        PopupMenu popup = new PopupMenu();
        
        MenuItem openItem = new MenuItem("Open");
        openItem.addActionListener(e -> restoreWindow());
        
        MenuItem restartItem = new MenuItem("Restart Server");
        restartItem.addActionListener(e -> server.restartAsync());

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> exitApp());
        
        popup.add(openItem);
        popup.add(restartItem);
        popup.addSeparator();
        popup.add(exitItem);

        TrayIcon trayIcon = new TrayIcon(image, "AirMouse Server", popup);
        trayIcon.setImageAutoSize(true);
        
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    restoreWindow();
                }
            }
        });

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.err.println("TrayIcon could not be added.");
        }
    }

    private Image createFallbackTrayIcon() {
        int w = 16;
        int h = 16;
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0, 230, 118)); // STATUS_GREEN
        g2.fillOval(0, 0, w, h);
        g2.dispose();
        return img;
    }

    private void restoreWindow() {
        SwingUtilities.invokeLater(() -> {
            window.setVisible(true);
            window.toFront();
        });
    }

    private void exitApp() {
        server.stop();
        System.exit(0);
    }
}
