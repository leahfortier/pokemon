package mapMaker.dialogs.action.panel;

import mapMaker.dialogs.action.ActionPanel;
import pattern.action.EmptyActionMatcher;

import java.util.function.Supplier;

public class EmptyActionPanel extends ActionPanel<EmptyActionMatcher> {
    private final Supplier<EmptyActionMatcher> actionMatcherGetter;

    public EmptyActionPanel(Supplier<EmptyActionMatcher> actionMatcherGetter) {
        this.actionMatcherGetter = actionMatcherGetter;
    }

    @Override
    public EmptyActionMatcher getActionMatcher() {
        return this.actionMatcherGetter.get();
    }

    @Override
    protected void load(EmptyActionMatcher matcher) {
        // Nothing to load since it be empty...
    }
}
