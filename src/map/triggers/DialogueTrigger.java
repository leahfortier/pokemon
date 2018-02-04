package map.triggers;

import map.condition.Condition;
import message.Messages;

class DialogueTrigger extends Trigger {
    // TODO: This should likely be an array of dialogue and contents should be json of an array of Strings
    private final String dialogue;

    DialogueTrigger(String dialogue, Condition condition) {
        super(TriggerType.DIALOGUE, dialogue, condition);

        this.dialogue = dialogue;
    }

    @Override
    protected void executeTrigger() {
        Messages.addToFront(this.dialogue);
    }
}
