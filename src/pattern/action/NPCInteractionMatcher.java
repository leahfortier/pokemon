package pattern.action;

import pattern.JsonMatcher;
import util.StringUtils;

import java.util.Arrays;
import java.util.List;

public class NPCInteractionMatcher implements JsonMatcher {
    private String name;
    private boolean walkToPlayer;
    private ActionMatcher[] npcActions;

    public NPCInteractionMatcher(String name, boolean walkToPlayer, ActionMatcher[] npcActions) {
        this.name = StringUtils.nullWhiteSpace(name);
        this.walkToPlayer = walkToPlayer;
        this.npcActions = npcActions;
    }

    public String getName() {
        return this.name;
    }

    public boolean shouldWalkToPlayer() {
        return this.walkToPlayer;
    }

    public List<ActionMatcher> getActions() {
        return Arrays.asList(this.npcActions);
    }
}
