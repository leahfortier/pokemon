package mapMaker.dialogs.action;

import pattern.AreaDataMatcher.ActionMatcher;

import javax.swing.JPanel;

abstract class ActionPanel extends JPanel {
    public abstract ActionMatcher getActionMatcher(ActionType actionType);
    protected abstract void load(ActionMatcher matcher);
}
