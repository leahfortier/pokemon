package generator.update;

import generator.GeneratorType;
import generator.update.AbilityUpdater.AbilityParser;
import pokemon.ability.AbilityNamesies;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class AbilityUpdater extends GeneratorUpdater<AbilityNamesies, AbilityParser> {
    public AbilityUpdater() {
        super(GeneratorType.ABILITY_GEN, "abilities");
    }

    @Override
    protected Map<AbilityNamesies, AbilityParser> createEmpty() {
        return new EnumMap<>(AbilityNamesies.class);
    }

    @Override
    protected Set<AbilityNamesies> getToParse() {
        Set<AbilityNamesies> toParse = EnumSet.allOf(AbilityNamesies.class);
        toParse.remove(AbilityNamesies.NO_ABILITY);
        return toParse;
    }

    @Override
    protected AbilityNamesies getNamesies(String name) {
        return AbilityNamesies.getValueOf(name);
    }

    @Override
    protected String getName(AbilityNamesies namesies) {
        return namesies.getName();
    }

    @Override
    protected String getDescription(AbilityNamesies namesies) {
        return namesies.getNewAbility().getDescription();
    }

    @Override
    protected AbilityParser createParser(Scanner in) {
        return new AbilityParser(in);
    }

    public class AbilityParser extends BaseParser {
        public final AbilityNamesies abilityNamesies;

        private AbilityParser(Scanner in) {
            name = in.nextLine().trim();
            description = in.nextLine().trim();

            abilityNamesies = getNamesies(name);
        }
    }
}
