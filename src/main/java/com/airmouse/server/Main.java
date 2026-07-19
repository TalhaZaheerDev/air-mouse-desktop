package com.airmouse.server;

import javax.swing.*;

/**
 * Entry point for AirMouse Server RC1.
 * Initializes Swing UI, Tray, and starts the TCP server on a background thread.
 */
public class Main {

    public static void main(String[] args) {
        // Run UI creation on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                // Set native look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                // Create Core Components First
                MouseController mouseController = new MouseController();
                Server server = new Server(mouseController);
                
                // Create Window (implements ServerListener)
                AirMouseWindow window = new AirMouseWindow(mouseController, server);
                server.setListener(window);
                
                // Setup System Tray
                TrayManager trayManager = new TrayManager(window, server);
                trayManager.setupTray();
                window.setTrayManager(trayManager);
                
                // Show Window
                window.setVisible(true);
                
                // Start Server Async
                server.startAsync();
                
            } catch (Exception e) {
                System.err.println("Failed to start server: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
}
