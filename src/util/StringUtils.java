package util;


import main.Global;

import java.util.Arrays;
import java.util.stream.Collectors;

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

    public static void appendSpaceSeparatedWord(final StringBuilder s, String word) {
        if (s.length() > 0) {
            s.append(" ");
        }

        s.append(word);
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
        // Convert to list of strings and join by a space
        return String.join(" ", Arrays.stream(values).map(Object::toString).collect(Collectors.toList()));
    }

    // Examples:
    //   red -> Red
    //   water stone -> Water Stone
    //   x-scissor -> X-Scissor
    // For all upper-case words first do toLowercase
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
                s.append(string.substring(0, string.length()));
                string = "";
            }
            else {
                s.append(string.substring(0, index)).append(c);
                string = string.substring(index + 1, string.length());
            }
        }

        return s.toString();
    }

    public static String getNamesiesString(String name) {
        if (isNullOrWhiteSpace(name)) {
            return empty();
        }

        // Remove special characters and spaces
        name = SpecialCharacter.removeSpecialCharacters(name).replace(" ", "");

        char[] nameChar = name.toCharArray();
        StringBuilder enumName = new StringBuilder(nameChar[0] + "");

        for (int i = 1; i < nameChar.length; i++) {
            if (((isUpper(nameChar[i]) &&
                    !isUpper(nameChar[i - 1])) || nameChar[i] == '-') &&
                    nameChar[i - 1] != '_' &&
                    enumName.charAt(enumName.length() - 1) != '_') {
                enumName.append("_");
            }

            if (isSpecial(nameChar[i])) {
                continue;
            }

            enumName.append(nameChar[i]);
        }

        return enumName.toString().toUpperCase();
    }

    // Creates the className from the name
    public static String getClassName(String name) {
        name = SpecialCharacter.removeSpecialCharacters(name);

        StringBuilder className = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            if (name.charAt(i) == '-') {
                if (isLower(name.charAt(i + 1))) {
                    char c = (char)(name.charAt(i + 1) - 'a' + 'A');
                    className.append(c);
                    i++;
                    continue;
                }

                continue;
            }

            if (isSpecial(name.charAt(i))) {
                continue;
            }

            className.append(name.charAt(i));
        }

        return className.toString();
    }
}
