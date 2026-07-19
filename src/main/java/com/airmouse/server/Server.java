package com.airmouse.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * TCP server that listens on port 5000.
 * Accepts one client at a time, handles it, then waits for the next.
 * Runs the accept loop on a background thread so the UI is not blocked.
 */
public class Server {

    private static final int PORT = 5000;

    private final MouseController mouseController;
    private ServerListener listener;
    private ServerSocket serverSocket;
    private volatile boolean running = false;

    public Server(MouseController mouseController) {
        this.mouseController = mouseController;
    }

    public void setListener(ServerListener listener) {
        this.listener = listener;
    }

    /**
     * Starts the server loop in a new background thread.
     */
    public void startAsync() {
        if (running) return;
        running = true;

        Thread serverThread = new Thread(this::runServerLoop, "AirMouse-Server-Thread");
        serverThread.setDaemon(true);
        serverThread.start();
    }

    private void runServerLoop() {
        try {
            serverSocket = new ServerSocket(PORT);
            
            NetworkUtils.NetworkInfo netInfo = NetworkUtils.detect();
            if (listener != null) {
                listener.onServerStarted(netInfo.ipAddress, PORT);
            }

            if (listener != null) {
                listener.onServerStarted(netInfo.ipAddress, PORT);
                listener.onLogMessage("Server Started on " + netInfo.ipAddress + ":" + PORT);
            }

            while (running && !serverSocket.isClosed()) {
                if (listener != null) listener.onLogMessage("Waiting for connection...");
                Socket clientSocket = serverSocket.accept();
                
                String clientAddress = clientSocket.getInetAddress().getHostAddress();
                if (listener != null) listener.onLogMessage("Connected: " + clientAddress);

                if (listener != null) {
                    listener.onClientConnected(clientAddress);
                }

                ClientHandler handler = new ClientHandler(clientSocket, mouseController, listener);
                handler.handle();

                // After client disconnects, loop back and wait for next client
            }
        } catch (IOException e) {
            if (running) {
                if (listener != null) listener.onLogMessage("Server error: " + e.getMessage());
            }
        } finally {
            stop();
        }
    }

    public void restartAsync() {
        stop();
        startAsync();
    }

    public void stop() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
