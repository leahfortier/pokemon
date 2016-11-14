package pattern;

import map.Direction;
import map.entity.EntityAction;
import map.entity.npc.NPCInteraction;
import mapMaker.model.TriggerModel.TriggerModelType;
import util.Point;
import util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NPCMatcher extends MapMakerEntityMatcher {
    public String name;
    private int startX;
    private int startY;
    private String path;
    public int spriteIndex;
    public Direction direction;
    public NPCInteractionMatcher[] interactions;

    private transient List<Point> location;
    private transient Map<String, NPCInteraction> interactionMap;
    private transient String startKey;

    public NPCMatcher(String name,
                      String condition,
                      String path,
                      int spriteIndex,
                      Direction direction,
                      List<NPCInteractionMatcher> interactions) {
        this.setName(name);
        this.path = StringUtils.nullWhiteSpace(path);
        this.spriteIndex = spriteIndex;
        this.direction = direction;
        this.interactions = interactions.toArray(new NPCInteractionMatcher[0]);

        super.setCondition(condition);
    }

    public void setName(String name) {
        this.name = StringUtils.nullWhiteSpace(name);
    }

    public Map<String, NPCInteraction> getInteractionMap() {
        if (interactionMap != null) {
            return interactionMap;
        }

        interactionMap = new HashMap<>();
        for (NPCInteractionMatcher interaction : interactions) {
            interactionMap.put(interaction.name, new NPCInteraction(interaction.walkToPlayer, interaction.getActions()));
        }

        if (interactions.length == 0) {
            startKey = "no_interactions";
            interactionMap.put(startKey, new NPCInteraction(false, new ArrayList<>()));

        } else {
            startKey = interactions[0].name;

        }
        return interactionMap;
    }

    public int getX() {
        return this.startX;
    }

    public int getY() {
        return this.startY;
    }

    public String getStartKey() {
        if (interactionMap == null) {
            getInteractionMap();
        }

        return startKey;
    }

    public String getPath() {
        if (StringUtils.isNullOrEmpty(this.path)) {
            this.path = Direction.WAIT_CHARACTER + "";
        }

        return this.path;
    }

    public int getSpriteIndex() {
        return this.spriteIndex;
    }

    public Direction getDirection() {
        return this.direction;
    }

    @Override
    public List<Point> getLocation() {
        if (this.location != null) {
            return this.location;
        }

        this.location = new ArrayList<>();
        this.location.add(new Point(startX, startY));
        return this.location;
    }

    @Override
    public void addPoint(Point location) {
        this.startX = location.x;
        this.startY = location.y;
    }

    @Override
    public TriggerModelType getTriggerModelType() {
        return TriggerModelType.NPC;
    }

    @Override
    public String getBasicName() {
        return this.name;
    }

    public static class NPCInteractionMatcher {
        public String name;
        public boolean walkToPlayer;
        public ActionMatcher[] npcActions;

        public NPCInteractionMatcher(String name, boolean walkToPlayer, ActionMatcher[] npcActions) {
            this.name = StringUtils.nullWhiteSpace(name);
            this.walkToPlayer = walkToPlayer;
            this.npcActions = npcActions;
        }

        public List<EntityAction> getActions() {
            List<EntityAction> actions = new ArrayList<>();
            for (ActionMatcher action : this.npcActions) {
                actions.add(action.getAction(null));
            }

            return actions;
        }
    }
}
