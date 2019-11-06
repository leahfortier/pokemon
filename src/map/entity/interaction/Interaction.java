package map.entity.interaction;

import pattern.action.ActionList;

public class Interaction {
    private final ActionList actions;

    public Interaction(ActionList actions) {
        this.actions = actions;
    }

    public ActionList getActions() {
        return this.actions;
    }
}
