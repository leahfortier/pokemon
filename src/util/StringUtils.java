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

    public static boolean isNullOrWhiteSpace(String s) {
        return isNullOrEmpty(s) || s.trim().isEmpty();
    }

    public static String nullWhiteSpace(String s) {
        return isNullOrWhiteSpace(s) ? null : s;
    }

    // Adds a new line to the beginning of a non-empty string
    public static String preNewLine(final String s) {
        return isNullOrEmpty(s) ? empty() : "\n" + s;
    }

    // Adds a new line to a non-empty string
    public static String addNewLine(final String s) {
        return isNullOrEmpty(s) ? empty() : s + "\n";
    }

    // Adds a space to a non-empty string
    public static String addSpace(final String s) {
        return isNullOrEmpty(s) ? empty() : s + " ";
    }

    public static void addCommaSeparatedValue(final StringBuilder builder, String newString) {
        if (!isNullOrEmpty(newString)) {
            if (builder.length() > 0) {
                builder.append(", ");
            }

            builder.append(newString);
        }
    }

    public static void appendLine(final StringBuilder builder, final String message) {
        builder.append(message).append("\n");
    }

    public static String empty() {
        return "";
    }

    public static boolean isSpecial(char c) {
        return !isLower(c) && !isUpper(c) && !isNumber(c) && c != '_';
    }

    public static boolean isUpper(char c) {
        return c >= 'A' && c <= 'Z';
    }

    public static boolean isLower(char c) {
        return c >= 'a' && c <= 'z';
    }

    public static boolean isNumber(char c) {
        return c >= '0' && c <= '9';
    }

    public static String articleString(final String s) {
        if (isNullOrEmpty(s)) {
            return empty();
        }

        boolean vowelStart = (s.charAt(0) + "").matches("[AEIOU]");
        return "a" + (vowelStart ? "n" : "") + " " + s;
    }

    public static void appendRepeat(StringBuilder builder, String repeat, int numTimes) {
        while (numTimes --> 0) {
            builder.append(repeat);
        }
    }

    public static String repeat(String repeat, int numTimes) {
        StringBuilder builder = new StringBuilder();
        appendRepeat(builder, repeat, numTimes);
        return builder.toString();
    }

    public static String spaceSeparated(Object... values) {
        StringBuilder builder = new StringBuilder();
        for (Object value : values) {
            builder.append(value).append(" ");
        }

        return builder.toString();
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
                s.append(string.substring(0, index)).append(c);
                string = string.substring(index + 1, string.length());
            }
        }

        return s.toString();
    }
}
