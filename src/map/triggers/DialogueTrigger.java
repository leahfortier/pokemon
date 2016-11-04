package map.triggers;

import message.Messages;

public class DialogueTrigger extends Trigger {
	// TODO: This should likely be an array of dialogue and contents should be json of an array of Strings
	private String dialogue;

	DialogueTrigger(String dialogue) {
		super(TriggerType.DIALOGUE, dialogue);

		this.dialogue = dialogue;
	}

	protected void executeTrigger() {
		Messages.addMessageToFront(this.dialogue);
	}

	public String getDialogue() {
		return this.dialogue;
	}
}
