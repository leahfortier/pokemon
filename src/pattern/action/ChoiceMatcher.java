package pattern.action;

import map.entity.EntityAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChoiceMatcher {
    private String text;
    public ActionMatcher2[] actions;

    public ChoiceMatcher(String text, ActionMatcher2[] actions) {
        this.text = text;
        this.actions = actions;
    }

    public String getText() {
        return this.text;
    }

    public List<ActionMatcher2> getActionMatchers() {
        return Arrays.asList(this.actions);
    }

    public List<EntityAction> getActions() {
        List<EntityAction> actions = new ArrayList<>();
        for (ActionMatcher2 action : this.actions) {
            actions.add(action.getAction(null));
        }

        return actions;
    }
}
