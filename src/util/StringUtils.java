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

    public static void appendLine(final StringBuilder builder, final String message) {
        builder.append(message).append("\n");
    }

    public static String empty() {
        return "";
    }

    public static String firstCaps(final String word) {
        if (isNullOrEmpty(word)) {
            return empty();
        }

        return Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
    }
}
