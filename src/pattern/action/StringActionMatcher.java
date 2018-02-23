package pattern.action;

import map.triggers.DialogueTrigger;
import map.triggers.GlobalTrigger;
import map.triggers.Trigger;
import map.triggers.map.MovePlayerTrigger;
import mapMaker.dialogs.action.ActionType;

public interface StringActionMatcher extends ActionMatcher {
    String getStringValue();

    class GlobalActionMatcher implements StringActionMatcher {
        private String global;

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
        public Trigger createNewTrigger() {
            return new GlobalTrigger(global);
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
        public Trigger createNewTrigger() {
            return new DialogueTrigger(this.dialogue);
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
        public Trigger createNewTrigger() {
            return new MovePlayerTrigger(path);
        }

        @Override
        public String getStringValue() {
            return path;
        }
    }
}
