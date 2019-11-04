package pattern.map;

import map.condition.ConditionSet;
import map.entity.Entity;
import map.entity.MiscEntity;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.action.ActionList;
import pattern.generic.EntityMatcher.MultiEntityMatcher;
import pattern.generic.MultiPointTriggerMatcher;
import pattern.interaction.MiscEntityInteractionMatcher;
import util.Point;

import java.util.Arrays;
import java.util.List;

public class MiscEntityMatcher extends MultiPointTriggerMatcher implements MultiEntityMatcher {
    private String name;
    private MiscEntityInteractionMatcher[] interactions;

    public MiscEntityMatcher(String name, String conditionName, ConditionSet conditionSet, List<MiscEntityInteractionMatcher> interactions) {
        this.name = name;
        this.interactions = interactions.toArray(new MiscEntityInteractionMatcher[0]);

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

    // TODO: Deprecate
    public ActionList getActions() {
        return interactions[0].getActions();
    }

    public List<MiscEntityInteractionMatcher> getInteractionMatcherList() {
        return Arrays.asList(this.interactions);
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
