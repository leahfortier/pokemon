package util;

import main.Global;

public class StringUtils {

    // Utility class -- should not be instantiated
    private StringUtils() {
        Global.error("Save class cannot be instantiated.");
    }

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    // Adds a space to a non-empty string
    public static String addSpace(final String s) {
        return isNullOrEmpty(s) ? "" : s + " ";
    }
}
