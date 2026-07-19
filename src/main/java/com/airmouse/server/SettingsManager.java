package com.airmouse.server;

import java.util.prefs.Preferences;

/**
 * Manages local persistent settings.
 */
public class SettingsManager {
    
    private static final Preferences prefs = Preferences.userNodeForPackage(SettingsManager.class);
    
    private static final String KEY_SENSITIVITY = "cursor_sensitivity";
    private static final String KEY_AUTO_START = "auto_start";

    public static double getSensitivity() {
        return prefs.getDouble(KEY_SENSITIVITY, 1.5);
    }

    public static void setSensitivity(double value) {
        prefs.putDouble(KEY_SENSITIVITY, value);
    }

    public static boolean isAutoStart() {
        return prefs.getBoolean(KEY_AUTO_START, false);
    }

    public static void setAutoStart(boolean value) {
        prefs.putBoolean(KEY_AUTO_START, value);
    }
}
