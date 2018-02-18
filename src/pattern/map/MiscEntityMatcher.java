package pattern.map;

import map.condition.ConditionSet;
import map.entity.EntityAction;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.action.ActionMatcher2;
import pattern.generic.MultiPointTriggerMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MiscEntityMatcher extends MultiPointTriggerMatcher {
    private String name;
    public ActionMatcher2[] actions;

    public MiscEntityMatcher(String name, String conditionName, ConditionSet conditionSet, ActionMatcher2[] actions) {
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

    public List<ActionMatcher2> getActionMatcherList() {
        return Arrays.asList(this.actions);
    }

    public List<EntityAction> getActions() {
        List<EntityAction> entityActions = new ArrayList<>();
        for (ActionMatcher2 matcher : this.actions) {
            entityActions.add(matcher.getAction(this.getCondition()));
        }

        return entityActions;
    }
}
