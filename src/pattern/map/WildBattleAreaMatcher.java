package pattern.map;

import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.generic.MultiPointTriggerMatcher;

import java.util.List;

public class WildBattleAreaMatcher extends MultiPointTriggerMatcher {

    private String name;
    public List<WildBattleMatcher> wildBattles;

    public WildBattleAreaMatcher(String name, List<WildBattleMatcher> wildBattles) {
        this.name = name;
        this.wildBattles = wildBattles;
    }

    public List<WildBattleMatcher> getWildBattles() {
        return this.wildBattles;
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
