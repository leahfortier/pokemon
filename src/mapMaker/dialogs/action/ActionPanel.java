package mapMaker.dialogs.action;

import pattern.action.ActionMatcher2;

import javax.swing.JPanel;

// TODO: Make this ActionPanel<T extends ActionMatcher2>
public abstract class ActionPanel extends JPanel {
    public abstract ActionMatcher2 getActionMatcher(ActionType actionType);
    protected abstract void load(ActionMatcher2 matcher);

    public void render() {}
}
