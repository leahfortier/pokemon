package pattern.map;

import map.entity.Entity;
import map.entity.EntityAction;
import map.entity.MiscEntity;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.generic.EntityMatcher;
import pattern.generic.SinglePointTriggerMatcher;
import pattern.action.ActionMatcher;

import java.util.ArrayList;
import java.util.List;

public class MiscEntityMatcher extends SinglePointTriggerMatcher implements EntityMatcher {
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

    @Override
    public Entity createEntity() {
        return new MiscEntity(
                this.getTriggerName(),
                this.getLocation(),
                this.getCondition(),
                this.getActions()
        );
    }
}
