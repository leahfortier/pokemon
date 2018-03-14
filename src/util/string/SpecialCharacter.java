package util.string;

public enum SpecialCharacter {
    POKE_E("\\\\u00e9", "\u00e9", "e"),
    FEMALE("\\\\u2640", "\u2640", "F"),
    MALE("\\\\u2642", "\u2642", "M");

    private final String unicodeLiteral;
    private final String specialCharacter;
    private final String replaceCharacter;

    SpecialCharacter(String unicodeLiteral, String specialCharacter, String replaceCharacter) {
        this.unicodeLiteral = unicodeLiteral;
        this.specialCharacter = specialCharacter;
        this.replaceCharacter = replaceCharacter;
    }

    public String getSpecialCharacter() {
        return this.specialCharacter;
    }

    // Converts special characters to a simple version (example: poke e -> e)
    public static String replaceSpecialCharacters(String input) {
        for (SpecialCharacter specialCharacter : values()) {
            input = input.replaceAll(specialCharacter.unicodeLiteral, specialCharacter.replaceCharacter);
            input = input.replaceAll(specialCharacter.specialCharacter, specialCharacter.replaceCharacter);
        }

        return input;
    }

    // Takes the special characters and replaces them with their literal unicode
    public static String convertSpecialToUnicode(String input) {
        for (SpecialCharacter specialCharacter : values()) {
            input = input.replaceAll(specialCharacter.specialCharacter, specialCharacter.unicodeLiteral);
        }

        return input;
    }

    // Removes the special characters as well as some symbols
    public static String removeSpecialSymbols(String input) {
        return replaceSpecialCharacters(input).replaceAll("[.'-]", "");
    }

    // Takes the unicode literals and replaces them with their special character value
    public static String restoreSpecialFromUnicode(String input) {
        for (SpecialCharacter specialCharacter : values()) {
            input = input.replaceAll(specialCharacter.unicodeLiteral, specialCharacter.specialCharacter);
        }

        return input;
    }
}
