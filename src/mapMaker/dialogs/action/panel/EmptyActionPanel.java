package mapMaker.dialogs.action.panel;

import main.Global;
import mapMaker.dialogs.action.ActionPanel;
import mapMaker.dialogs.action.ActionType;
import pattern.action.ActionMatcher;
import pattern.action.ActionMatcher.DayCareActionMatcher;
import pattern.action.ActionMatcher.HealPartyActionMatcher;
import pattern.action.ActionMatcher.ReloadMapActionMatcher;

public class EmptyActionPanel extends ActionPanel {
    @Override
    public ActionMatcher getActionMatcher(ActionType actionType) {
        switch (actionType) {
            case DAY_CARE:
                return new DayCareActionMatcher();
            case HEAL_PARTY:
                return new HealPartyActionMatcher();
            case RELOAD_MAP:
                return new ReloadMapActionMatcher();
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
