package pattern.map;

import map.condition.ConditionSet;
import map.entity.Entity;
import map.entity.MiscEntity;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.action.ActionList;
import pattern.action.ActionMatcher;
import pattern.generic.EntityMatcher.MultiEntityMatcher;
import pattern.generic.MultiPointTriggerMatcher;
import util.Point;

public class MiscEntityMatcher extends MultiPointTriggerMatcher implements MultiEntityMatcher {
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

    public ActionList getActions() {
        return new ActionList(actions);
    }

    @Override
    public Entity createEntity(Point location) {
        return new MiscEntity(
                this.getTriggerName(),
                location,
                this.getCondition(),
                this.getActions()
        );
    }
}
