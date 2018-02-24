package map.entity.movable;

import main.Game;
import map.Direction;
import map.PathDirection;
import map.condition.Condition;
import map.triggers.Trigger;
import trainer.player.Player;
import util.Point;

import java.util.HashMap;
import java.util.Map;

public class NPCEntity extends MovableEntity {
    static final int NPC_SIGHT_DISTANCE = 5;

    private final String path;
    private final Point defaultLocation;
    private final Direction defaultDirection;
    private final MoveAxis moveAxis;
    private final int spriteIndex;

    private final Map<String, NPCInteraction> interactions;
    private final String startKey;

    private Direction transitionDirection;
    private boolean hasAttention;
    private boolean walkingToPlayer;

    private Map<String, Trigger> triggerInteractionMap;

    public NPCEntity(
            String name,
            Point location,
            Condition condition,
            String path,
            Direction direction,
            MoveAxis moveAxis,
            int spriteIndex,
            String startKey,
            Map<String, NPCInteraction> interactions) {
        super(location, name, condition);

        this.path = path;
        this.defaultLocation = location;
        this.defaultDirection = direction;
        this.moveAxis = moveAxis;
        this.spriteIndex = spriteIndex;
        this.startKey = startKey;
        this.interactions = interactions;

        this.triggerInteractionMap = new HashMap<>();

        this.reset();
    }

    void walkTowards(int steps, PathDirection direction) {
        super.setTempPath(direction.getTempPath(steps), null);
        walkingToPlayer = true;
    }

    private String getCurrentInteractionKey() {
        Player player = Game.getPlayer();
        if (player.hasNpcInteraction(this.getEntityName())) {
            return player.getNpcInteractionName(this.getEntityName());
        }

        return this.startKey;
    }

    @Override
    protected int getSpriteIndex() {
        return this.spriteIndex;
    }

    @Override
    public int getTransitionTime() {
        return this.getTimeBetweenTiles()*2;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    protected void endPath() {
        this.walkingToPlayer = false;
    }

    @Override
    public boolean hasAttention() {
        return this.hasAttention;
    }

    @Override
    public Direction getDirection() {
        return this.transitionDirection;
    }

    @Override
    protected void setDirection(Direction direction) {
        this.transitionDirection = direction;
    }

    @Override
    public void getAttention(Direction direction) {
        this.setDirection(direction);
        if (this.moveAxis == MoveAxis.FACING && !this.hasTempPath()) {
            hasAttention = true;
        }
    }

    boolean canWalkToPlayer() {
        return this.isWalkToPlayer()
                && !this.walkingToPlayer
                && this.moveAxis.checkAxis(this.getLocation(), this.getDirection(), Game.getPlayer().getLocation())
                && this.getTrigger().isTriggered();
    }

    public boolean isWalkToPlayer() {
        final String interaction = this.getCurrentInteractionKey();
        return this.interactions.get(interaction).shouldWalkToPlayer();
    }

    public boolean isTrainer() {
        return interactions.get(this.getCurrentInteractionKey()).isBattleInteraction();
    }

    @Override
    public void reset() {
        super.reset();

        this.setLocation(defaultLocation);
        this.setDirection(defaultDirection);

        hasAttention = false;
        walkingToPlayer = false;
    }

    @Override
    public Trigger getTrigger() {
        String currentInteraction = this.getCurrentInteractionKey();

        if (!this.triggerInteractionMap.containsKey(currentInteraction)) {
            NPCInteraction interaction = this.interactions.get(currentInteraction);

            Trigger trigger = interaction.getActions().getGroupTrigger(
                    this.getEntityName(),
                    this.getCondition()
            );

            this.triggerInteractionMap.put(currentInteraction, trigger);
        }

        return this.triggerInteractionMap.get(currentInteraction);
    }

    @Override
    public void setTempPath(String path, EndPathListener listener) {
        super.setTempPath(path, listener);
        hasAttention = false;
    }
}
