package map.triggers;

import message.Messages;

public class DialogueTrigger extends Trigger {
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
