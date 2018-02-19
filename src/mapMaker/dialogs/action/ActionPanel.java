package mapMaker.dialogs.action;

import pattern.action.ActionMatcher;

import javax.swing.JPanel;

// TODO: Make this ActionPanel<T implements ActionMatcher2>
public abstract class ActionPanel extends JPanel {
    public abstract ActionMatcher getActionMatcher(ActionType actionType);
    protected abstract void load(ActionMatcher matcher);

    public void render() {}
}
