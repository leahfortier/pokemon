package pattern;

import map.entity.EntityAction;
import mapMaker.model.TriggerModel.TriggerModelType;

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
