package pattern.map;

import main.Global;
import map.EncounterRate;
import map.WildEncounter;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.MatchConstants;
import pattern.MatchConstants.MatchType;
import pattern.generic.MultiPointTriggerMatcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WildBattleMatcher extends MultiPointTriggerMatcher {
    private static final Pattern wildEncounterPattern = Pattern.compile(
            MatchConstants.group(MatchType.POKEMON_NAME) + " " +
                    MatchConstants.group(MatchType.INTEGER) + "-" + MatchConstants.group(MatchType.INTEGER) + " " +
                    MatchConstants.group(MatchType.INTEGER) + "%"
    );

    public String name;
    private EncounterRate encounterRate;
    private String[] pokemon;

    public WildBattleMatcher(String name, EncounterRate encounterRate, WildEncounter[] wildEncounters) {
        this.name = name;
        this.encounterRate = encounterRate;
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
        WildEncounter[] wildEncounters = new WildEncounter[pokemon.length];
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

        return wildEncounters;
    }

    @Override
    public TriggerModelType getTriggerModelType() {
        return TriggerModelType.WILD_BATTLE;
    }

    @Override
    public String getBasicName() {
        return this.name;
    }
}
