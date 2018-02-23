package mapMaker.dialogs.action;

import pattern.action.ActionMatcher;

import javax.swing.JPanel;

public abstract class ActionPanel<T extends ActionMatcher> extends JPanel {
    public abstract T getActionMatcher();
    protected abstract void load(T matcher);

    public void render() {}
}
