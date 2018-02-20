package map.entity.movable;

import pattern.action.ActionMatcher;
import pattern.action.ActionMatcher.BattleActionMatcher;

import java.util.List;

public class NPCInteraction {
    private boolean walkToPlayer;
    private List<ActionMatcher> actions;

    public NPCInteraction(boolean walkToPlayer, List<ActionMatcher> actions) {
        this.walkToPlayer = walkToPlayer;
        this.actions = actions;
    }

    public boolean shouldWalkToPlayer() {
        return this.walkToPlayer;
    }

    public List<ActionMatcher> getActions() {
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
