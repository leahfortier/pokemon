package mapMaker.dialogs.action;

import pattern.action.ActionMatcher;

import javax.swing.JPanel;

abstract class ActionPanel extends JPanel {
    public abstract ActionMatcher getActionMatcher(ActionType actionType);
    protected abstract void load(ActionMatcher matcher);

    public void render() {}
}
