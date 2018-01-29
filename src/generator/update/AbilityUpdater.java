package generator.update;

import generator.GeneratorType;
import pokemon.ability.AbilityNamesies;
import util.FileIO;
import util.Folder;
import util.StringAppender;
import util.StringUtils;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class AbilityUpdater extends GeneratorUpdater {
    private static final String SCRIPTS_INPUT_FILE_NAME = Folder.SCRIPTS + "abilities.in";
    private static final String SCRIPTS_OUTPUT_FILE_NAME = Folder.SCRIPTS + "abilities.out";

    private final Map<AbilityNamesies, String> parsedDescriptionsMap;

    public AbilityUpdater() {
        super(GeneratorType.ABILITY_GEN);

        parsedDescriptionsMap = new EnumMap<>(AbilityNamesies.class);

        Scanner in = FileIO.openFile(SCRIPTS_OUTPUT_FILE_NAME);
        while (in.hasNext()) {
            AbilityNamesies abilityNamesies = AbilityNamesies.getValueOf(in.nextLine().trim());
            parsedDescriptionsMap.put(abilityNamesies, in.nextLine().trim());
        }
    }

    @Override
    public void writeScriptInputList() {
        Set<AbilityNamesies> toParse = EnumSet.allOf(AbilityNamesies.class);
        toParse.remove(AbilityNamesies.NO_ABILITY);

        String out = new StringAppender()
                .appendJoin("\n", toParse, AbilityNamesies::getName)
                .toString();
        FileIO.overwriteFile(SCRIPTS_INPUT_FILE_NAME, out);
    }

    @Override
    public String getNewDescription(String name) {
        AbilityNamesies abilityNamesies = AbilityNamesies.getValueOf(name);
        String description = parsedDescriptionsMap.get(abilityNamesies);
        if (!StringUtils.isNullOrEmpty(description)) {
            return description;
        } else {
            return abilityNamesies.getNewAbility().getDescription();
        }
    }
}
