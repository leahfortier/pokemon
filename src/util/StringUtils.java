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

    // TODO: Look at this again and rewrite it
    public static String properCase(String string) {
        if (isNullOrEmpty(string)) {
            return empty();
        }

        StringBuilder s = new StringBuilder();
        string = string.trim();

        while (!string.isEmpty()) {
            s.append(string.substring(0, 1).toUpperCase());
            string = string.substring(1, string.length());

            if (string.isEmpty()) {
                break;
            }

            char c = ' ';
            int index = string.indexOf(c);
            int indexOther = string.indexOf('-');
            if (indexOther != -1 && (indexOther < index || index == -1)) {
                c = '-';
                index = indexOther;
            }

            if (index == -1) {
                s.append(string.substring(0,string.length()));
                string = "";
            }
            else {
                s.append(string.substring(0,index) + c);
                string = string.substring(index + 1, string.length());
            }
        }

        return s.toString();
    }
}
