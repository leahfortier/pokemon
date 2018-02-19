package mapMaker.dialogs.action;

import main.Global;
import pattern.action.ActionMatcher;
import pattern.action.ActionMatcher.HealPartyActionMatcher;

public class EmptyActionPanel extends ActionPanel {
    @Override
    public ActionMatcher getActionMatcher(ActionType actionType) {
        switch (actionType) {
            case HEAL_PARTY:
                return new HealPartyActionMatcher();
            default:
                Global.info("Invalid action type for empty action " + actionType + " :(");
                return null;
        }
    }

    @Override
    protected void load(ActionMatcher matcher) {
        // Nothing to load since it be empty...
    }
}
