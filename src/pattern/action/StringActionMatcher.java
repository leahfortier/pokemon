package pattern.action;

import main.Game;
import map.condition.Condition;
import map.triggers.DialogueTrigger;
import map.triggers.GlobalTrigger;
import map.triggers.GroupTrigger;
import map.triggers.Trigger;
import map.triggers.UpdateTrigger;
import map.triggers.map.MovePlayerTrigger;
import mapMaker.dialogs.action.ActionType;

public interface StringActionMatcher extends ActionMatcher {
    String getStringValue();

    class UpdateActionMatcher implements StringActionMatcher {
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
        public Trigger createNewTrigger(String entityName, Condition condition) {
            return new UpdateTrigger(new UpdateMatcher(entityName, update), condition);
        }
    }

    class GroupTriggerActionMatcher implements StringActionMatcher {
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
        public Trigger createNewTrigger(String entityName, Condition condition) {
            String triggerName = Trigger.createName(GroupTrigger.class, this.groupTrigger);
            return Game.getData().getTrigger(triggerName);
        }
    }

    class GlobalActionMatcher implements StringActionMatcher {
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
        public Trigger createNewTrigger(String entityName, Condition condition) {
            return new GlobalTrigger(global, condition);
        }
    }

    class DialogueActionMatcher implements StringActionMatcher {
        private String dialogue;

        public DialogueActionMatcher(String dialogue) {
            this.dialogue = dialogue;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.DIALOGUE;
        }

        @Override
        public Trigger createNewTrigger(String entityName, Condition condition) {
            return new DialogueTrigger(this.dialogue, condition);
        }

        @Override
        public String getStringValue() {
            return dialogue;
        }
    }

    class MovePlayerActionMatcher implements StringActionMatcher {
        private String path;

        public MovePlayerActionMatcher(String path) {
            this.path = path;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.MOVE_PLAYER;
        }

        @Override
        public Trigger createNewTrigger(String entityName, Condition condition) {
            return new MovePlayerTrigger(path, condition);
        }

        @Override
        public String getStringValue() {
            return path;
        }
    }
}
