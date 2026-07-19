package com.airmouse.server;

/**
 * Entry point for AirMouse Server.
 * Creates the MouseController and starts the TCP server on port 5000.
 */
public class Main {

    public static void main(String[] args) {
        try {
            MouseController mouseController = new MouseController();
            Server server = new Server(mouseController);
            server.start();
        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
