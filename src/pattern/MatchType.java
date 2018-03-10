package pattern;

import main.Global;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum MatchType {
    WORD("\\w+"),
    MULTI_WORD("[\\w ]+"),
    INTEGER("\\d+"),
    DIALOGUE(".*"),
    POKEMON_NAME(MULTI_WORD.regex),
    ATTACK_NAME(MULTI_WORD.regex),
    ITEM_NAME("[\\w \\-'.]+"),
    ENTITY_NAME("[\\w\\_]+"),
    VARIABLE_TYPE("[\\w\\[\\]\\<\\>\\d\\? ]+");

    private static final Pattern VARIABLE_DECLARATION = Pattern.compile(VARIABLE_TYPE.group() + " " + WORD.group());

    public final String regex;

    MatchType(final String regex) {
        this.regex = regex;
    }

    public String group() {
        return "(" + this.regex + ")";
    }

    public static Entry<String, String> getVariableDeclaration(String variableDeclaration) {
        Matcher matcher = VARIABLE_DECLARATION.matcher(variableDeclaration);
        if (!matcher.matches()) {
            Global.error("Variable declaration not properly formatted: " + variableDeclaration);
        }

        return new SimpleEntry<>(matcher.group(1), matcher.group(2));
    }
}
