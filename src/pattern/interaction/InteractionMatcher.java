package pattern.interaction;

import pattern.action.ActionList;
import pattern.action.ActionMatcher;
import util.serialization.JsonMatcher;
import util.string.StringUtils;

public class InteractionMatcher implements JsonMatcher {
    private String name;
    private ActionMatcher[] actions;

    public InteractionMatcher(String name, ActionMatcher[] actions) {
        this.name = StringUtils.nullWhiteSpace(name);
        this.actions = actions;
    }

    public String getName() {
        return this.name;
    }

    public ActionList getActions() {
        return new ActionList(actions);
    }
}
