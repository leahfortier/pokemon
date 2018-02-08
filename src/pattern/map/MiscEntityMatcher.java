package pattern.map;

import map.condition.ConditionSet;
import map.entity.EntityAction;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.action.ActionMatcher;
import pattern.generic.MultiPointTriggerMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MiscEntityMatcher extends MultiPointTriggerMatcher {
    private String name;
    private ActionMatcher[] actions;

    public MiscEntityMatcher(String name, String conditionName, ConditionSet conditionSet, ActionMatcher[] actions) {
        this.name = name;
        this.actions = actions;

        super.setCondition(conditionName, conditionSet);
    }

    @Override
    public TriggerModelType getTriggerModelType() {
        return TriggerModelType.MISC_ENTITY;
    }

    @Override
    public String getBasicName() {
        return this.name;
    }

    public List<ActionMatcher> getActionMatcherList() {
        return Arrays.asList(this.actions);
    }

    public List<EntityAction> getActions() {
        List<EntityAction> entityActions = new ArrayList<>();
        for (ActionMatcher matcher : this.actions) {
            entityActions.add(matcher.getAction(this.getCondition()));
        }

        return entityActions;
    }
}
