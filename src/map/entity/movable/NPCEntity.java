package map.entity.movable;

import main.Game;
import map.Direction;
import map.PathDirection;
import map.condition.Condition;
import map.entity.interaction.InteractionMap;
import map.entity.interaction.NPCInteraction;
import map.triggers.Trigger;
import util.Point;

import java.util.Map;

public class NPCEntity extends MovableEntity {
    static final int NPC_SIGHT_DISTANCE = 5;

    private final String path;
    private final Point defaultLocation;
    private final Direction defaultDirection;
    private final MoveAxis moveAxis;
    private final int spriteIndex;

    private final InteractionMap<NPCInteraction> interactions;

    private Direction transitionDirection;
    private boolean hasAttention;
    private boolean walkingToPlayer;

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
        this.interactions = new InteractionMap<>(this.getEntityName(), startKey, interactions);

        this.reset();
    }

    void walkTowards(int steps, PathDirection direction) {
        super.setTempPath(direction.getTempPath(steps), null);
        walkingToPlayer = true;
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
                && this.getTrigger().canTrigger();
    }

    private boolean isWalkToPlayer() {
        return this.interactions.getCurrentInteraction().shouldWalkToPlayer();
    }

    public boolean isTrainer() {
        return interactions.getCurrentInteraction().isBattleInteraction();
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
        return this.interactions.getTrigger(this.getCondition());
    }

    @Override
    public void setTempPath(String path, EndPathListener listener) {
        super.setTempPath(path, listener);
        hasAttention = false;
    }
}
