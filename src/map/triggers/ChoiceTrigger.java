package map.triggers;

import message.MessageUpdate;
import message.Messages;
import pattern.action.ChoiceActionMatcher;
import pattern.action.ChoiceActionMatcher.ChoiceMatcher;
import util.SerializationUtils;

class ChoiceTrigger extends Trigger {
    private final String question;
    private final ChoiceMatcher[] choices;

    ChoiceTrigger(String choices, String condition) {
        super(TriggerType.CHOICE, choices, condition);

        ChoiceActionMatcher matcher = SerializationUtils.deserializeJson(choices, ChoiceActionMatcher.class);
        this.question = matcher.getQuestion();
        this.choices = matcher.getChoices();
    }

    protected void executeTrigger() {
        Messages.addToFront(new MessageUpdate(this.question).withChoices(this.choices));
    }
}
