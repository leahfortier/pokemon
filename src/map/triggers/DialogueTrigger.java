package map.triggers;

import message.Messages;

class DialogueTrigger extends Trigger {
	// TODO: This should likely be an array of dialogue and contents should be json of an array of Strings
	private final String dialogue;

	DialogueTrigger(String dialogue, String condition) {
		super(TriggerType.DIALOGUE, dialogue, condition);

		this.dialogue = dialogue;
	}

	protected void executeTrigger() {
		Messages.addMessageToFront(this.dialogue);
	}
}
