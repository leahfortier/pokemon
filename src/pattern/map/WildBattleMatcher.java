package pattern.map;

import map.condition.ConditionSet;
import map.overworld.EncounterRate;
import map.overworld.WildEncounterInfo;
import pattern.generic.TriggerMatcher;

import java.util.List;

public class WildBattleMatcher extends TriggerMatcher {
    private String name;
    private EncounterRate encounterRate;
    private WildEncounterInfo[] wildPokemon;

    public WildBattleMatcher(
            String name,
            String conditionName,
            ConditionSet conditionSet,
            EncounterRate encounterRate,
            List<WildEncounterInfo> wildEncounters) {
        this.name = name;
        this.encounterRate = encounterRate;
        this.wildPokemon = wildEncounters.toArray(new WildEncounterInfo[0]);

        super.setCondition(conditionName, conditionSet);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EncounterRate getEncounterRate() {
        return this.encounterRate;
    }

    public WildEncounterInfo[] getWildEncounters() {
        return this.wildPokemon;
    }
}
