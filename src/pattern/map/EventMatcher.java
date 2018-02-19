package pattern.map;

import map.condition.ConditionSet;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.action.ActionMatcher;
import pattern.generic.MultiPointTriggerMatcher;

import java.util.Arrays;
import java.util.List;

public class EventMatcher extends MultiPointTriggerMatcher {
    private String name;
    public ActionMatcher[] actions;

    public EventMatcher(String name, String conditionName, ConditionSet conditionSet, ActionMatcher[] actions) {
        this.name = name;
        this.actions = actions;

        super.setCondition(conditionName, conditionSet);
    }

    @Override
    public TriggerModelType getTriggerModelType() {
        return TriggerModelType.EVENT;
    }

    @Override
    public String getBasicName() {
        return name;
    }

    public List<ActionMatcher> getActions() {
        return Arrays.asList(this.actions);
    }
}
