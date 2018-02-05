package pattern.map;

import main.Game;
import main.Global;
import map.condition.Condition;
import map.condition.ConditionSet;
import util.StringUtils;

public class ConditionMatcher {
    private String name;
    private String description;
    private ConditionSet condition;

    public ConditionMatcher(String name, String description, ConditionSet condition) {
        this.name = name;
        this.description = description;
        this.condition = condition;
    }

    public String getName() {
        return this.name;
    }

    public ConditionSet getCondition() {
        return this.condition;
    }

    public static Condition getCondition(String conditionName, ConditionSet condition) {
        if (!StringUtils.isNullOrEmpty(conditionName)) {
            if (condition != null) {
                Global.error("Cannot specify both condition name and contents.");
            }

            return Game.getData().getCondition(conditionName).getCondition();
        }

        if (condition == null) {
            return null;
        }

        return condition.getCondition();
    }
}
