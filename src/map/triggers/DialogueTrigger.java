package map.triggers;

import message.Messages;

public class DialogueTrigger extends Trigger {
    // TODO: This should likely be an array of dialogue and contents should be json of an array of Strings
    private final String dialogue;

    public DialogueTrigger(String dialogue) {
        super(dialogue);
        this.dialogue = dialogue;
    }

    @Override
    public void execute() {
        Messages.addToFront(this.dialogue);
    }
}
