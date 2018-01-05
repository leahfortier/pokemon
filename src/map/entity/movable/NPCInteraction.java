package map.entity.movable;

import map.entity.EntityAction;

import java.util.List;

public class NPCInteraction {
    private boolean walkToPlayer;
    private List<EntityAction> actions;
    
    public NPCInteraction(boolean walkToPlayer, List<EntityAction> actions) {
        this.walkToPlayer = walkToPlayer;
        this.actions = actions;
    }
    
    public boolean shouldWalkToPlayer() {
        return this.walkToPlayer;
    }
    
    public List<EntityAction> getActions() {
        return this.actions;
    }
}
