package pattern.action;

import util.serialization.JsonMatcher;
import util.string.StringUtils;

public class MiscEntityInteractionMatcher implements JsonMatcher {
    private String name;
    private ActionMatcher[] actions;

    public MiscEntityInteractionMatcher(String name, ActionMatcher[] actions) {
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
