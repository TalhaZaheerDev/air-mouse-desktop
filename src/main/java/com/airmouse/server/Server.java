package com.airmouse.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * TCP server that listens on port 5000.
 * Accepts one client at a time, handles it, then waits for the next.
 */
public class Server {

    private static final int PORT = 5000;

    private final MouseController mouseController;

    public Server(MouseController mouseController) {
        this.mouseController = mouseController;
    }

    /**
     * Starts the server loop. Blocks indefinitely.
     */
    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            printBanner();

            while (true) {
                System.out.println("Waiting for connection...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connected");

                ClientHandler handler = new ClientHandler(clientSocket, mouseController);
                handler.handle();

                // After client disconnects, loop back and wait for next client
            }
        }
    }

    /**
     * Prints the startup banner with detected local IPv4 address and port.
     */
    private void printBanner() {
        String localIp = detectLocalIpv4();

        System.out.println();
        System.out.println("=================================");
        System.out.println("   AirMouse Server Started");
        System.out.println("=================================");
        System.out.println();
        System.out.println("Local IP : " + localIp);
        System.out.println("Port     : " + PORT);
        System.out.println();
        System.out.println("Enter these values into the Flutter app.");
        System.out.println();
    }

    /**
     * Detects the local IPv4 address from a real network adapter (Wi-Fi/Ethernet),
     * ignoring loopback, link-local, and virtual adapters (VMware, Hyper-V, Docker, etc.).
     */
    private String detectLocalIpv4() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();

                // Skip down, loopback, or virtual adapters
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
                    continue;
                }

                // Skip known virtual adapter names
                String name = ni.getDisplayName().toLowerCase();
                if (name.contains("vmware") || name.contains("virtualbox")
                        || name.contains("hyper-v") || name.contains("docker")
                        || name.contains("vethernet") || name.contains("loopback")) {
                    continue;
                }

                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    // Only IPv4, skip loopback and link-local (169.254.x.x)
                    if (addr instanceof java.net.Inet4Address
                            && !addr.isLoopbackAddress()
                            && !addr.isLinkLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            System.out.println("Warning: Could not detect local IP (" + e.getMessage() + ")");
        }

        return "Unknown (check ipconfig manually)";
    }
}
