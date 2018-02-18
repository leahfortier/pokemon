package pattern.action;

import map.entity.EntityAction;
import util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class NPCInteractionMatcher {
    private String name;
    private boolean walkToPlayer;
    public ActionMatcher2[] npcActions;

    public NPCInteractionMatcher(String name, boolean walkToPlayer, ActionMatcher[] npcActions) {
        this.name = StringUtils.nullWhiteSpace(name);
        this.walkToPlayer = walkToPlayer;
//        this.npcActions = npcActions;
    }

    public String getName() {
        return this.name;
    }

    public boolean shouldWalkToPlayer() {
        return this.walkToPlayer;
    }

    public List<ActionMatcher> getActionMatcherList() {
        return new ArrayList<>();
//        return Arrays.asList(this.npcActions);
    }

    public List<EntityAction> getActions() {
        List<EntityAction> actions = new ArrayList<>();
//        for (ActionMatcher action : this.npcActions) {
//            actions.add(action.getAction(null));
//        }

        return actions;
    }
}
