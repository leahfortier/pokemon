package pattern.action;

import map.condition.Condition;
import map.triggers.DialogueTrigger;
import map.triggers.GlobalTrigger;
import map.triggers.GroupTrigger;
import map.triggers.Trigger;
import map.triggers.UpdateTrigger;
import mapMaker.dialogs.action.ActionType;
import pattern.GroupTriggerMatcher;

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
        protected Trigger getTrigger(String entityName, Condition condition) {
            return new UpdateTrigger(new UpdateMatcher(entityName, update), null);
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
        protected Trigger getTrigger(String entityName, Condition condition) {
            return new GroupTrigger(new GroupTriggerMatcher(this.groupTrigger), null);
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
        protected Trigger getTrigger(String entityName, Condition condition) {
            return new GlobalTrigger(global, null);
        }
    }

    public static class DialogueActionMatcher extends StringActionMatcher {
        private String dialogue;

        public DialogueActionMatcher(String dialogue) {
            this.dialogue = dialogue;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.DIALOGUE;
        }

        @Override
        protected Trigger getTrigger(String entityName, Condition condition) {
            return new DialogueTrigger(this.dialogue, condition);
        }

        @Override
        public String getActionString() {
            return dialogue;
        }
    }
}
