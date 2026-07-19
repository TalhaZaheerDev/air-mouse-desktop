package com.airmouse.server;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Network utility for detecting local IPv4 address and connection type.
 * Filters out loopback, virtual, and link-local adapters.
 */
public final class NetworkUtils {

    private NetworkUtils() {}

    /** Virtual adapter keywords to ignore. */
    private static final String[] VIRTUAL_KEYWORDS = {
        "vmware", "virtualbox", "hyper-v", "docker",
        "vethernet", "loopback", "virtual", "vbox"
    };

    /**
     * Result holder for IP detection.
     */
    public static class NetworkInfo {
        public final String ipAddress;
        public final String connectionType;

        public NetworkInfo(String ipAddress, String connectionType) {
            this.ipAddress = ipAddress;
            this.connectionType = connectionType;
        }
    }

    /**
     * Detects the local IPv4 address and connection type from real adapters.
     */
    public static NetworkInfo detect() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();

                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
                    continue;
                }

                String displayName = ni.getDisplayName().toLowerCase();
                if (isVirtualAdapter(displayName)) {
                    continue;
                }

                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    if (addr instanceof java.net.Inet4Address
                            && !addr.isLoopbackAddress()
                            && !addr.isLinkLocalAddress()) {

                        String connType = detectConnectionType(displayName, ni.getName());
                        return new NetworkInfo(addr.getHostAddress(), connType);
                    }
                }
            }
        } catch (SocketException e) {
            System.err.println("Warning: Could not detect network (" + e.getMessage() + ")");
        }

        return new NetworkInfo("Unknown", "Unknown");
    }

    private static boolean isVirtualAdapter(String displayName) {
        for (String keyword : VIRTUAL_KEYWORDS) {
            if (displayName.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines connection type from adapter name.
     */
    private static String detectConnectionType(String displayName, String name) {
        String lower = displayName + " " + name.toLowerCase();
        if (lower.contains("wi-fi") || lower.contains("wifi")
                || lower.contains("wireless") || lower.contains("wlan")) {
            return "Wi-Fi";
        }
        if (lower.contains("ethernet") || lower.contains("eth")) {
            return "Ethernet";
        }
        return "Network";
    }
}
