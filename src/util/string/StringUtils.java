package util.string;

import main.Global;

import java.util.Arrays;

public class StringUtils {
    private static final String[] PROPER_CASE_DELIMITERS = { " ", "-", "_" };

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

    public static String trimSuffix(String s, String suffix) {
        if (s.endsWith(suffix)) {
            return s.substring(0, s.length() - suffix.length());
        }

        return s;
    }

    public static String articleString(final String s) {
        if (isNullOrEmpty(s)) {
            return empty();
        }

        boolean vowelStart = (s.charAt(0) + "").matches("[AEIOU]");
        return "a" + (vowelStart ? "n" : "") + " " + s;
    }

    public static String repeat(String repeat, int numTimes) {
        return new StringAppender()
                .appendRepeat(repeat, numTimes)
                .toString();
    }

    // Convert to list of strings and join by a space
    public static String spaceSeparated(Object... values) {
        return new StringAppender()
                .appendJoin(" ", Arrays.asList(values))
                .toString();
    }

    // Examples:
    //   red -> Red
    //   water stone -> Water Stone
    //   x-scissor -> X-Scissor
    // For all upper-case words first do toLowercase
    public static String properCase(String string) {
        string = string.trim();
        if (isNullOrEmpty(string)) {
            return empty();
        }

        // Split by each delimiter and rejoin in proper case
        // "water stone" -> ["water", "stone"] -> ["Water", "Stone"] -> "Water Stone"
        for (String delimiter : PROPER_CASE_DELIMITERS) {
            string = new StringAppender()
                    .appendJoin(
                            delimiter,
                            Arrays.asList(string.split(delimiter)),
                            s -> s.substring(0, 1).toUpperCase() + s.substring(1)
                    )
                    .toString();
        }

        return string;
    }

    // Gets an enum-like name from the input name
    // Ex: King's Rock -> KINGS_ROCK
    public static String getNamesiesString(String name) {
        if (isNullOrWhiteSpace(name)) {
            return empty();
        }

        // Convert special characters to their safer version (poke e -> e)
        // Replace delimiters with underscores
        // Remove special characters
        name = SpecialCharacter.removeSpecialCharacters(name)
                               .replaceAll("[\\s-]", "_")
                               .replaceAll("['.:]", "");

        // Insert an underscore whenever a capital letter comes after a lower-case one
        StringAppender appender = new StringAppender(name);
        for (int i = name.length() - 1; i > 1; i--) {
            if (isUpper(name.charAt(i)) && isLower(name.charAt(i - 1))) {
                appender.insert(i, "_");
            }
        }

        // Convert to upper case
        return appender.toString().toUpperCase();
    }

    // Creates the className from the name
    // Ex: "King's Rock" -> "KingsRock"
    public static String getClassName(String name) {
        // Convert to proper case (just in CASE)
        // Convert special characters to their safer version (poke e -> e)
        // Remove all non-alphanumeric characters
        return SpecialCharacter.removeSpecialCharacters(properCase(name))
                               .replaceAll("[^0-9a-zA-Z]", "");
    }

    public static <T extends Enum<T>> T enumValueOf(Class<T> enumClass, String name) {
        T namesies = enumTryValueOf(enumClass, name);
        if (namesies == null) {
            Global.error(name + " does not have a valid " + enumClass.getSimpleName() + " value");
        }

        return namesies;
    }

    public static <T extends Enum<T>> T enumTryValueOf(Class<T> enumClass, String name) {
        try {
            return Enum.valueOf(enumClass, getNamesiesString(name));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
