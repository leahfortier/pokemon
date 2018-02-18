package pattern.action;

import map.condition.Condition;
import map.entity.EntityAction;
import map.entity.EntityAction.GlobalAction;
import map.entity.EntityAction.GroupTriggerAction;
import map.entity.EntityAction.UpdateAction;
import mapMaker.dialogs.action.ActionType;

public abstract class StringActionMatcher extends ActionMatcher {
    public abstract String getActionString();

    public static class UpdateActionMatcher extends StringActionMatcher {
        public String update;

        public UpdateActionMatcher(String update) {
            this.update = update;
        }

        @Override
        public String getActionString() {
            return this.update;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.UPDATE;
        }

        @Override
        public EntityAction getAction(Condition condition) {
            return new UpdateAction(update);
        }
    }

    public static class GroupTriggerActionMatcher extends StringActionMatcher {
        public String groupTrigger;

        public GroupTriggerActionMatcher(String groupTrigger) {
            this.groupTrigger = groupTrigger;
        }

        @Override
        public String getActionString() {
            return this.groupTrigger;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.GROUP_TRIGGER;
        }

        @Override
        public EntityAction getAction(Condition condition) {
            return new GroupTriggerAction(groupTrigger);
        }
    }

    public static class GlobalActionMatcher extends StringActionMatcher {
        public String global;

        public GlobalActionMatcher(String global) {
            this.global = global;
        }

        @Override
        public String getActionString() {
            return this.global;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.GLOBAL;
        }

        @Override
        public EntityAction getAction(Condition condition) {
            return new GlobalAction(global);
        }
    }
}
