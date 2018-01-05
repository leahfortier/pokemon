package map.entity.movable;

import main.Game;
import map.Direction;
import map.PathDirection;
import map.entity.EntityAction;
import map.entity.EntityAction.BattleAction;
import trainer.player.Player;
import util.Point;
import util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

    private boolean dataCreated;

    public NPCEntity(
            String name,
            Point location,
            String condition,
            String path,
            Direction direction,
            MoveAxis moveAxis,
            int spriteIndex,
            Map<String, NPCInteraction> interactions,
            String startKey) {
        super(location, name, condition);

        this.path = path;

        this.defaultLocation =  location;
        this.defaultDirection = direction;
        this.moveAxis = moveAxis;
        this.spriteIndex = spriteIndex;

        this.interactions = interactions;
        this.startKey = startKey;

        this.reset();
        this.addData();
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
    public String getTriggerSuffix() {
        return this.getTriggerSuffix(this.getCurrentInteractionKey());
    }

    private String getTriggerSuffix(final String interactionName) {
        return super.getTriggerSuffix() + "_" + interactionName;
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
                && Game.getData().getTrigger(this.getWalkTrigger()).isTriggered();
    }

    public boolean isWalkToPlayer() {
        final String interaction = this.getCurrentInteractionKey();
        return this.interactions.get(interaction).shouldWalkToPlayer();
    }

    private String getWalkTrigger() {
        return isWalkToPlayer() ? this.getTriggerName() : StringUtils.empty();
    }

    public boolean isTrainer() {
        NPCInteraction interaction = interactions.get(this.getCurrentInteractionKey());
        List<EntityAction> actions = interaction.getActions();
        for (EntityAction action : actions) {
            if (action instanceof BattleAction) {
                return true;
            }
        }

        return false;
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
    public void addData() {
        if (dataCreated) {
            return;
        }

        for (Entry<String, NPCInteraction> interaction : this.interactions.entrySet()) {
            final String interactionName = interaction.getKey();
            final List<EntityAction> actions = interaction.getValue().getActions();

            EntityAction.addActionGroupTrigger(this.getEntityName(), this.getTriggerSuffix(interactionName), this.getConditionString(), actions);
        }
        
        dataCreated = true;
    }

    @Override
    public void setTempPath(String path, EndPathListener listener) {
        super.setTempPath(path, listener);
        hasAttention = false;
    }
}
