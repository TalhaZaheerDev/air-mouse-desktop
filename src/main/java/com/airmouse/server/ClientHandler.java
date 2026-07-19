package com.airmouse.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Handles a single connected client.
 * Reads lines from the socket, parses commands, and delegates to MouseController.
 */
public class ClientHandler {

    private final Socket socket;
    private final MouseController mouseController;

    public ClientHandler(Socket socket, MouseController mouseController) {
        this.socket = socket;
        this.mouseController = mouseController;
    }

    /**
     * Reads commands from the client until disconnect.
     */
    public void handle() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                processCommand(line.trim());
            }

        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        } finally {
            closeSocket();
            System.out.println("Client Disconnected");
        }
    }

    /**
     * Parses and executes a single command line.
     */
    private void processCommand(String line) {
        if (line.isEmpty()) {
            return;
        }

        String[] parts = line.split(",");
        String command = parts[0];

        try {
            switch (command) {
                case "M" -> handleMove(parts);
                case "C" -> handleClick(parts);
                default -> System.out.println("Unknown command: " + line);
            }
        } catch (Exception e) {
            System.out.println("Invalid command: " + line + " (" + e.getMessage() + ")");
        }
    }

    private void handleMove(String[] parts) {
        if (parts.length != 3) {
            System.out.println("Invalid move command, expected M,dx,dy");
            return;
        }

        double dx = Double.parseDouble(parts[1].trim());
        double dy = Double.parseDouble(parts[2].trim());
        mouseController.moveMouse(dx, dy);
        System.out.println("MOVE dx=" + dx + " dy=" + dy);
    }

    private void handleClick(String[] parts) {
        if (parts.length != 2) {
            System.out.println("Invalid click command, expected C,L or C,R");
            return;
        }

        String button = parts[1].trim();
        switch (button) {
            case "L" -> {
                mouseController.leftClick();
                System.out.println("LEFT CLICK");
            }
            case "R" -> {
                mouseController.rightClick();
                System.out.println("RIGHT CLICK");
            }
            default -> System.out.println("Unknown click button: " + button);
        }
    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            // ignore
        }
    }
}
