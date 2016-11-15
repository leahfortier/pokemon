package map.triggers;

import message.MessageUpdate;
import message.Messages;
import pattern.action.ChoiceActionMatcher;
import pattern.action.ChoiceActionMatcher.ChoiceMatcher;
import util.JsonUtils;

public class ChoiceTrigger extends Trigger {
    private String question;
    private ChoiceMatcher[] choices;

    ChoiceTrigger(String choices, String condition) {
        super(TriggerType.CHOICE, choices, condition);

        ChoiceActionMatcher matcher = JsonUtils.deserialize(choices, ChoiceActionMatcher.class);
        this.question = matcher.question;
        this.choices = matcher.choices;
    }

    protected void executeTrigger() {
        Messages.addMessageToFront(new MessageUpdate(this.question, this.choices));
    }
}
