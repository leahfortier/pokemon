package pattern.map;

import map.condition.ConditionSet;
import map.entity.Entity;
import map.entity.MiscEntity;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.action.ActionList;
import pattern.generic.EntityMatcher.MultiEntityMatcher;
import pattern.generic.MultiPointTriggerMatcher;
import pattern.interaction.InteractionMatcher;
import util.Point;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Should always have at least one interaction
public class MiscEntityMatcher extends MultiPointTriggerMatcher implements MultiEntityMatcher {
    private String name;
    private InteractionMatcher[] interactions;

    public MiscEntityMatcher(String name, String conditionName, ConditionSet conditionSet, List<InteractionMatcher> interactions) {
        this.name = name;
        this.interactions = interactions.toArray(new InteractionMatcher[0]);

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

    public List<InteractionMatcher> getInteractionMatcherList() {
        return Arrays.asList(this.interactions);
    }

    private String getStartKey() {
        return interactions[0].getName();
    }

    private Map<String, ActionList> getInteractionMap() {
        Map<String, ActionList> interactionMap = new HashMap<>();
        for (InteractionMatcher interaction : interactions) {
            interactionMap.put(interaction.getName(), interaction.getActions());
        }

        return interactionMap;
    }

    @Override
    public Entity createEntity(Point location) {
        return new MiscEntity(
                this.getTriggerName(),
                location,
                this.getCondition(),
                this.getStartKey(),
                this.getInteractionMap()
        );
    }
}
