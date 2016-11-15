package pattern.map;

import map.entity.EntityAction;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.generic.SinglePointEntityMatcher;
import pattern.action.ActionMatcher;

import java.util.ArrayList;
import java.util.List;

public class MiscEntityMatcher extends SinglePointEntityMatcher {
    private String name;
    private ActionMatcher[] actions;

    @Override
    public TriggerModelType getTriggerModelType() {
        return TriggerModelType.TRIGGER_ENTITY;
    }

    @Override
    public String getBasicName() {
        return this.name;
    }

    public List<EntityAction> getActions() {
        List<EntityAction> entityActions = new ArrayList<>();
        for (ActionMatcher matcher : this.actions) {
            entityActions.add(matcher.getAction(this.getCondition()));
        }

        return entityActions;
    }
}
