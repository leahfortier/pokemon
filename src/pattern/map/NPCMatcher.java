package pattern.map;

import main.Global;
import map.Direction;
import map.PathDirection;
import map.condition.ConditionSet;
import map.entity.Entity;
import map.entity.movable.MoveAxis;
import map.entity.movable.NPCEntity;
import map.entity.movable.NPCInteraction;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.action.ActionList;
import pattern.action.NPCInteractionMatcher;
import pattern.generic.EntityMatcher;
import pattern.generic.SinglePointTriggerMatcher;
import util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NPCMatcher extends SinglePointTriggerMatcher implements EntityMatcher {
    private static final String NO_INTERACTIONS_KEY = "no_interactions";

    private String name;
    private String path;
    private int spriteIndex;
    private Direction direction;
    private MoveAxis moveAxis;
    private NPCInteractionMatcher[] interactions;

    public NPCMatcher(String name,
                      String conditionName,
                      ConditionSet conditionSet,
                      String path,
                      int spriteIndex,
                      Direction direction,
                      List<NPCInteractionMatcher> interactions) {
        this.name = StringUtils.nullWhiteSpace(name);
        this.path = StringUtils.nullWhiteSpace(path);
        this.spriteIndex = spriteIndex;
        this.direction = direction;
        this.interactions = interactions.toArray(new NPCInteractionMatcher[0]);

        super.setCondition(conditionName, conditionSet);
    }

    public List<NPCInteractionMatcher> getInteractionMatcherList() {
        return Arrays.asList(this.interactions);
    }

    public Map<String, NPCInteraction> getInteractionMap() {
        if (interactions.length == 0) {
            return Collections.singletonMap(NO_INTERACTIONS_KEY, new NPCInteraction(false, new ActionList()));
        }

        Map<String, NPCInteraction> interactionMap = new HashMap<>();
        for (NPCInteractionMatcher interaction : interactions) {
            if (interactionMap.containsKey(interaction.getName())) {
                Global.error("Duplicate interaction name for " + this.getTriggerName() + ": " + interaction.getName());
            }

            NPCInteraction npcInteraction = new NPCInteraction(interaction.shouldWalkToPlayer(), interaction.getActions());
            interactionMap.put(interaction.getName(), npcInteraction);
        }

        return interactionMap;
    }

    private String getStartKey() {
        if (interactions.length == 0) {
            return NO_INTERACTIONS_KEY;
        } else {
            return interactions[0].getName();
        }
    }

    public String getPath() {
        if (StringUtils.isNullOrEmpty(this.path)) {
            this.path = PathDirection.defaultPath();
        }

        return this.path;
    }

    public int getSpriteIndex() {
        return this.spriteIndex;
    }

    public Direction getDirection() {
        return this.direction;
    }

    private MoveAxis getMoveAxis() {
        if (this.moveAxis != null) {
            return this.moveAxis;
        }

        return MoveAxis.FACING;
    }

    @Override
    public TriggerModelType getTriggerModelType() {
        return TriggerModelType.NPC;
    }

    @Override
    public String getBasicName() {
        return this.name;
    }

    @Override
    public Entity createEntity() {
        return new NPCEntity(
                this.getTriggerName(),
                this.getLocation(),
                this.getCondition(),
                this.getPath(),
                this.getDirection(),
                this.getMoveAxis(),
                this.getSpriteIndex(),
                this.getStartKey(),
                this.getInteractionMap()
        );
    }
}
