package generator.update;

import pokemon.ability.AbilityNamesies;
import util.FileIO;
import util.Folder;
import util.StringAppender;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class AbilityParser {
    private static final String SCRIPTS_INPUT_FILE_NAME = Folder.SCRIPTS + "abilities.in";
    private static final String SCRIPTS_OUTPUT_FILE_NAME = Folder.SCRIPTS + "abilities.out";

    private static Map<AbilityNamesies, AbilityParser> parseAbilities;

    public final AbilityNamesies abilityNamesies;
    public final String description;

    private AbilityParser(Scanner in) {
        this.abilityNamesies = AbilityNamesies.getValueOf(in.nextLine().trim());
        this.description = in.nextLine().trim();
    }

    public static void writeAbilitiesList() {
        Set<AbilityNamesies> toParse = EnumSet.allOf(AbilityNamesies.class);
        toParse.remove(AbilityNamesies.NO_ABILITY);

        String out = new StringAppender()
                .appendJoin("\n", toParse, AbilityNamesies::getName)
                .toString();
        FileIO.overwriteFile(SCRIPTS_INPUT_FILE_NAME, out);
    }

    public static AbilityParser getParseAbility(AbilityNamesies abilityNamesies) {
        if (parseAbilities == null) {
            readAbilities();
        }

        return parseAbilities.get(abilityNamesies);
    }

    private static void readAbilities() {
        if (parseAbilities != null) {
            return;
        }

        parseAbilities = new EnumMap<>(AbilityNamesies.class);

        Scanner in = FileIO.openFile(SCRIPTS_OUTPUT_FILE_NAME);
        while (in.hasNext()) {
            AbilityParser abilityParser = new AbilityParser(in);
            parseAbilities.put(abilityParser.abilityNamesies, abilityParser);
        }
    }
}
