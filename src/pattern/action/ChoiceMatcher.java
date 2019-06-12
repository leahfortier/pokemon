package pattern.action;

public class ChoiceMatcher {
    private String text;
    private ActionMatcher[] actions;

    public ChoiceMatcher(String text, ActionMatcher... actions) {
        this.text = text;
        this.actions = actions;
    }

    public String getText() {
        return this.text;
    }

    public ActionList getActions() {
        return new ActionList(actions);
    }
}
