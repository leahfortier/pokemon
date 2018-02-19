package pattern.action;

import gui.GameData;
import main.Game;
import map.condition.Condition;
import map.triggers.DialogueTrigger;
import map.triggers.GlobalTrigger;
import map.triggers.GroupTrigger;
import map.triggers.Trigger;
import map.triggers.UpdateTrigger;
import map.triggers.map.MovePlayerTrigger;
import mapMaker.dialogs.action.ActionType;
import pattern.GroupTriggerMatcher;

public abstract class StringActionMatcher extends ActionMatcher {
    public abstract String getStringValue();

    public static class UpdateActionMatcher extends StringActionMatcher {
        private String update;

        public UpdateActionMatcher(String update) {
            this.update = update;
        }

        @Override
        public String getStringValue() {
            return this.update;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.UPDATE;
        }

        @Override
        protected Trigger getTrigger(String entityName, Condition condition) {
            return new UpdateTrigger(new UpdateMatcher(entityName, update), condition);
        }
    }

    public static class GroupTriggerActionMatcher extends StringActionMatcher {
        public String groupTrigger;

        public GroupTriggerActionMatcher(String groupTrigger) {
            this.groupTrigger = groupTrigger;
        }

        @Override
        public String getStringValue() {
            return this.groupTrigger;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.GROUP_TRIGGER;
        }

        @Override
        protected Trigger getTrigger(String entityName, Condition condition) {
            GameData data = Game.getData();

            String triggerName = Trigger.createName(GroupTrigger.class, this.groupTrigger);
            if (data.hasTrigger(triggerName)) {
                return data.getTrigger(triggerName);
            }

            return new GroupTrigger(new GroupTriggerMatcher(this.groupTrigger), condition);
        }
    }

    public static class GlobalActionMatcher extends StringActionMatcher {
        public String global;

        public GlobalActionMatcher(String global) {
            this.global = global;
        }

        @Override
        public String getStringValue() {
            return this.global;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.GLOBAL;
        }

        @Override
        protected Trigger getTrigger(String entityName, Condition condition) {
            return new GlobalTrigger(global, condition);
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
        public String getStringValue() {
            return dialogue;
        }
    }

    public static class MovePlayerActionMatcher extends StringActionMatcher {
        private String path;

        public MovePlayerActionMatcher(String path) {
            this.path = path;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.MOVE_PLAYER;
        }

        @Override
        protected Trigger getTrigger(String entityName, Condition condition) {
            return new MovePlayerTrigger(path, condition);
        }

        @Override
        public String getStringValue() {
            return path;
        }
    }
}
