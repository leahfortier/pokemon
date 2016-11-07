package map.triggers;

import message.MessageUpdate;
import message.Messages;
import pattern.AreaDataMatcher;
import pattern.AreaDataMatcher.ChoiceActionMatcher;
import pattern.AreaDataMatcher.ChoiceMatcher;

public class ChoiceTrigger extends Trigger {
    private String question;
    private ChoiceMatcher[] choices;

    ChoiceTrigger(String choices, String condition) {
        super(TriggerType.CHOICE, choices, condition);

        ChoiceActionMatcher matcher = AreaDataMatcher.deserialize(choices, ChoiceActionMatcher.class);
        this.question = matcher.question;
        this.choices = matcher.choices;
    }

    protected void executeTrigger() {
        Messages.addMessageToFront(new MessageUpdate(this.question, this.choices));
    }
}
