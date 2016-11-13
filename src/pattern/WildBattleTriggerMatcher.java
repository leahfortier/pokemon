package pattern;

import main.Global;
import map.EncounterRate;
import map.WildEncounter;
import map.triggers.TriggerType;
import pattern.ActionMatcher.TriggerActionMatcher;
import pattern.MatchConstants.MatchType;
import util.JsonUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WildBattleTriggerMatcher {
    private static final Pattern wildEncounterPattern = Pattern.compile(
            MatchConstants.group(MatchType.POKEMON_NAME) + " " +
                    MatchConstants.group(MatchType.INTEGER) + "-" + MatchConstants.group(MatchType.INTEGER) + " " +
                    MatchConstants.group(MatchType.INTEGER) + "%"
    );

    private EncounterRate encounterRate;
    private String[] pokemon;

    private transient WildEncounter[] wildEncounters;

    public static TriggerMatcher createWildBattleMatcher(String name, EncounterRate encounterRate, WildEncounter[] wildEncounters) {
        WildBattleTriggerMatcher wildBattleTriggerMatcher = new WildBattleTriggerMatcher(encounterRate, wildEncounters);
        ActionMatcher action = new ActionMatcher();
        action.trigger = new TriggerActionMatcher(TriggerType.WILD_BATTLE, JsonUtils.getJson(wildBattleTriggerMatcher));

        return new TriggerMatcher(name, null, new ActionMatcher[] { action });
    }

    private WildBattleTriggerMatcher(EncounterRate encounterRate, WildEncounter[] wildEncounters) {
        this.encounterRate = encounterRate;
        this.wildEncounters = wildEncounters;
        this.pokemon = new String[wildEncounters.length];
        for (int i = 0; i < pokemon.length; i++) {
            WildEncounter wildEncounter = wildEncounters[i];
            this.pokemon[i] = wildEncounter.getPokemonName() + " " + wildEncounter.getMinLevel() + "-" + wildEncounter.getMaxLevel() + " " + wildEncounter.getProbability() + "%";
        }
    }

    public EncounterRate getEncounterRate() {
        return this.encounterRate;
    }

    public WildEncounter[] getWildEncounters() {
        if (this.wildEncounters != null) {
            return wildEncounters;
        }

        this.wildEncounters = new WildEncounter[pokemon.length];
        for (int i = 0; i < pokemon.length; i++) {
            Matcher matcher = wildEncounterPattern.matcher(pokemon[i]);
            if (!matcher.matches()) {
                Global.error("Invalid wild pokemon encounter description " + pokemon[i]);
            }

            wildEncounters[i] = new WildEncounter(
                    matcher.group(1),   // Pokemon name
                    matcher.group(2),   // Min level
                    matcher.group(3),   // Max level
                    matcher.group(4)    // Percentage probability
            );
        }

        return this.wildEncounters;
    }
}
