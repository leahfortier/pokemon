package pattern.action;

import map.triggers.DayCareTrigger;
import map.triggers.HealPartyTrigger;
import map.triggers.Trigger;
import map.triggers.map.ReloadMapTrigger;
import mapMaker.dialogs.action.ActionType;

public interface EmptyActionMatcher extends ActionMatcher {
    class HealPartyActionMatcher implements EmptyActionMatcher {
        @Override
        public ActionType getActionType() {
            return ActionType.HEAL_PARTY;
        }

        @Override
        public Trigger createNewTrigger(String entityName) {
            return new HealPartyTrigger();
        }
    }

    class DayCareActionMatcher implements EmptyActionMatcher {
        @Override
        public ActionType getActionType() {
            return ActionType.DAY_CARE;
        }

        @Override
        public Trigger createNewTrigger(String entityName) {
            return new DayCareTrigger();
        }
    }

    class ReloadMapActionMatcher implements EmptyActionMatcher {
        @Override
        public ActionType getActionType() {
            return ActionType.RELOAD_MAP;
        }

        @Override
        public Trigger createNewTrigger(String entityName) {
            return new ReloadMapTrigger();
        }
    }
}
