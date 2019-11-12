package pattern.map;

import map.condition.ConditionSet;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.action.ActionList;
import pattern.action.ActionMatcher;
import pattern.location.MultiPointTriggerMatcher;

public class EventMatcher extends MultiPointTriggerMatcher {
    private String name;
    private ActionMatcher[] actions;

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

    public ActionList getActions() {
        return new ActionList(actions);
    }
}
