package pattern;

class MatchConstants {

    enum MatchType {
        WORD("\\w+"),
        MULTI_WORD("[\\w ]+"),
        INTEGER("\\d+"),
        DIALOGUE(".*"),
        POKEMON_NAME(MULTI_WORD.regex),
        ATTACK_NAME(MULTI_WORD.regex),
        ITEM_NAME("[\\w \\-'.]+"),
        ENTITY_NAME("[\\w\\_]+");

        private final String regex;

        MatchType(final String regex) {
            this.regex = regex;
        }
    }

    static String group(final MatchType matchType) {
         return "(" + matchType.regex + ")";
    }

    static String getSimpleParameterRegex(final String parameterName, final MatchType matchType) {
        return "(?:" +
                    "(" + parameterName + ":) " +           // Group 1: parameterName:
                    "(" + matchType.regex + ")" +     // Group 2: regex
                ")";
    }

    static String getSimpleParameterRegexList(final String parameterName, final MatchType matchType, final int listLength) {
        String regex = "(?:(" + parameterName + ":) ";
        for (int i = 0; i < listLength; i++) {
            regex += group(matchType);
            if (i < listLength - 1) {
                regex += ", ";
            }
        }

        regex += ")";
        return regex;
    }

    static String getNestedParameterRegex(MatchType matchType) {
        return getNestedParameterRegex(matchType.regex);
    }

    static String getNestedParameterRegex(final String parameterName) {
        return "(?:(" + parameterName + ") " +
                "\\{" +
                "[\\s]*" +
                "(.*)" +
                "[\\s]*" +
                "\\}) +" +
                "[\\s]*";
    }
}
