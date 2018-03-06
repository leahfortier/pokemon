package map.entity.movable;

import pattern.action.ActionList;
import pattern.action.ActionMatcher;
import pattern.action.EntityActionMatcher.BattleActionMatcher;

public class NPCInteraction {
    private final boolean walkToPlayer;
    private final ActionList actions;

    public NPCInteraction(boolean walkToPlayer, ActionList actions) {
        this.walkToPlayer = walkToPlayer;
        this.actions = actions;
    }

    public boolean shouldWalkToPlayer() {
        return this.walkToPlayer;
    }

    public ActionList getActions() {
        return this.actions;
    }

    public boolean isBattleInteraction() {
        for (ActionMatcher action : actions) {
            if (action instanceof BattleActionMatcher) {
                return true;
            }
        }

        return false;
    }
}
