package com.airmouse.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * Handles a single connected client.
 * Reads TCP stream byte-by-byte for minimal latency, parses commands inline,
 * and delegates to MouseController.
 */
public class ClientHandler {

    private final Socket socket;
    private final MouseController mouseController;
    private final ServerListener listener;

    /** Reusable buffer for line assembly — avoids per-line allocation. */
    private final byte[] lineBuffer = new byte[256];

    public ClientHandler(Socket socket, MouseController mouseController, ServerListener listener) {
        this.socket = socket;
        this.mouseController = mouseController;
        this.listener = listener;
    }

    /**
     * Reads commands from the client until disconnect.
     * Uses raw InputStream with TCP_NODELAY for lowest possible latency.
     */
    public void handle() {
        try {
            // TCP tuning: disable Nagle's algorithm to eliminate coalescing delay
            socket.setTcpNoDelay(true);
            socket.setReceiveBufferSize(8192);

            InputStream in = socket.getInputStream();
            int linePos = 0;

            int b;
            while ((b = in.read()) != -1) {
                if (b == '\n') {
                    if (linePos > 0) {
                        processLine(lineBuffer, linePos);
                    }
                    linePos = 0;
                } else if (b != '\r') {
                    if (linePos < lineBuffer.length) {
                        lineBuffer[linePos++] = (byte) b;
                    }
                    // If line exceeds buffer, excess bytes are silently dropped
                }
            }

        } catch (IOException e) {
            log("Connection error: " + e.getMessage());
        } finally {
            mouseController.reset();
            closeSocket();
            if (listener != null) {
                listener.onClientDisconnected();
            }
            log("Client Disconnected");
        }
    }

    private void log(String message) {
        if (listener != null) {
            listener.onLogMessage(message);
        }
    }

    /**
     * Parses and executes a command from the raw byte buffer.
     * Avoids String allocation for the hot path (move commands).
     */
    private void processLine(byte[] buf, int len) {
        if (len < 3) return; // Minimum valid command: "C,L"

        byte cmd = buf[0];
        if (buf[1] != ',') {
            log("Malformed command (no comma at pos 1)");
            return;
        }

        try {
            switch (cmd) {
                case 'M' -> handleMove(buf, len);
                case 'C' -> handleClick(buf, len);
                case 'S' -> handleScroll(buf, len);
                default -> log("Unknown command: " + new String(buf, 0, len));
            }
        } catch (Exception e) {
            log("Invalid command: " + new String(buf, 0, len)
                    + " (" + e.getMessage() + ")");
        }
    }

    /**
     * Parses M,dx,dy directly from byte buffer without String.split().
     */
    private void handleMove(byte[] buf, int len) {
        // Find the second comma separating dx and dy
        int commaPos = -1;
        for (int i = 2; i < len; i++) {
            if (buf[i] == ',') {
                commaPos = i;
                break;
            }
        }

        if (commaPos == -1 || commaPos == 2 || commaPos == len - 1) {
            log("Invalid move command, expected M,dx,dy");
            return;
        }

        double dx = parseDouble(buf, 2, commaPos);
        double dy = parseDouble(buf, commaPos + 1, len);

        mouseController.moveMouse(dx, dy);
        log("MOVE dx=" + dx + " dy=" + dy);
    }

    /**
     * Parses a double from a byte buffer range without creating a String.
     * Handles negative numbers and decimal points.
     */
    private static double parseDouble(byte[] buf, int start, int end) {
        // Trim leading/trailing spaces
        while (start < end && buf[start] == ' ') start++;
        while (end > start && buf[end - 1] == ' ') end--;

        boolean negative = false;
        if (start < end && buf[start] == '-') {
            negative = true;
            start++;
        }

        long intPart = 0;
        long fracPart = 0;
        long fracDiv = 1;
        boolean hasDot = false;

        for (int i = start; i < end; i++) {
            byte c = buf[i];
            if (c == '.') {
                hasDot = true;
            } else if (c >= '0' && c <= '9') {
                if (hasDot) {
                    fracPart = fracPart * 10 + (c - '0');
                    fracDiv *= 10;
                } else {
                    intPart = intPart * 10 + (c - '0');
                }
            } else {
                throw new NumberFormatException("Unexpected char: " + (char) c);
            }
        }

        double result = intPart + (fracDiv > 1 ? (double) fracPart / fracDiv : 0.0);
        return negative ? -result : result;
    }

    private void handleClick(byte[] buf, int len) {
        if (len != 3) {
            log("Invalid click command, expected C,L or C,R");
            return;
        }

        byte button = buf[2];
        switch (button) {
            case 'L' -> {
                mouseController.leftClick();
                log("LEFT CLICK");
            }
            case 'R' -> {
                mouseController.rightClick();
                log("RIGHT CLICK");
            }
            default -> log("Unknown click button: " + (char) button);
        }
    }

    private void handleScroll(byte[] buf, int len) {
        if (len < 4) {
            log("Invalid scroll command, expected S,delta");
            return;
        }

        double delta = parseDouble(buf, 2, len);
        mouseController.scroll(delta);
        log("SCROLL delta=" + delta);
    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            // ignore
        }
    }
}