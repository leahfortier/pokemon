package util.string;

import main.Global;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringUtils {
    private static final String[] PROPER_CASE_DELIMITERS = { " ", "-", "_" };

    // Utility class -- should not be instantiated
    private StringUtils() {
        Global.error(this.getClass().getSimpleName() + " class cannot be instantiated.");
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

    public static String trimPrefix(String s, String prefix) {
        if (s.startsWith(prefix)) {
            return s.substring(prefix.length(), s.length());
        } else {
            return s;
        }
    }

    public static String trimSuffix(String s, String suffix) {
        if (s.endsWith(suffix)) {
            return s.substring(0, s.length() - suffix.length());
        } else {
            return s;
        }
    }

    public static String trimChars(String quoted, String prefix, String suffix) {
        while (quoted.startsWith(prefix) && quoted.endsWith(suffix)) {
            quoted = quoted.substring(1, quoted.length() - 1);
        }
        return quoted;
    }

    public static String trimQuotes(String quoted) {
        return trimChars(quoted, "\"", "\"");
    }

    public static String trimSingleQuotes(String quoted) {
        return trimChars(quoted, "'", "'");
    }

    public static boolean isAlphaOnly(String s) {
        return s.matches("[a-zA-Z]+");
    }

    public static String articleString(final String s) {
        if (isNullOrEmpty(s)) {
            return "";
        }

        boolean vowelStart = Character.toString(s.charAt(0)).toUpperCase().matches("[AEIOU]");
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
        if (isNullOrWhiteSpace(string)) {
            return "";
        }

        string = string.trim();

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
            return "";
        }

        // Convert special characters to their safer version (poke e -> e)
        // Replace delimiters with underscores
        // Remove special characters
        name = SpecialCharacter.replaceSpecialCharacters(name.trim())
                               .replaceAll("[\\s-]", "_")
                               .replaceAll("['.:]", "");

        // Insert an underscore whenever a capital letter comes after a lower-case one
        StringAppender appender = new StringAppender(name);
        for (int i = name.length() - 1; i > 1; i--) {
            if (Character.isUpperCase(name.charAt(i)) && Character.isLowerCase(name.charAt(i - 1))) {
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
        return SpecialCharacter.replaceSpecialCharacters(properCase(name))
                               .replaceAll("[^0-9a-zA-Z]", "");
    }

    // Takes a string in the form methodName(parameter1, parameter2, ...) and returns methodName
    public static String getMethodName(String methodHeader) {
        Pattern headerPattern = Pattern.compile("^([a-zA-Z]+)\\(.*\\)$");
        Matcher matcher = headerPattern.matcher(methodHeader);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
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

    // Yes I realize this is not totally comprehensive but it honestly seems fine for now and can be added on if necessary
    public static String toString(Object object) {
        if (object instanceof Object[]) {
            return Arrays.toString((Object[])object);
        } else if (object instanceof int[]) {
            return Arrays.toString((int[])object);
        } else {
            return Objects.toString(object);
        }
    }
}
