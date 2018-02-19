package pattern.action;

import java.util.Arrays;
import java.util.List;

public class ChoiceMatcher {
    private String text;
    public ActionMatcher[] actions;

    public ChoiceMatcher(String text, ActionMatcher[] actions) {
        this.text = text;
        this.actions = actions;
    }

    public String getText() {
        return this.text;
    }

    public List<ActionMatcher> getActions() {
        return Arrays.asList(this.actions);
    }
}
