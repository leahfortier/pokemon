package map.triggers;

import map.condition.Condition;
import message.MessageUpdate;
import message.Messages;
import pattern.action.ActionMatcher.ChoiceActionMatcher;
import pattern.action.ChoiceMatcher;
import util.SerializationUtils;

class ChoiceTrigger extends Trigger {
    private final String question;
    private final ChoiceMatcher[] choices;

    ChoiceTrigger(String choices, Condition condition) {
        super(TriggerType.CHOICE, choices, condition);

        ChoiceActionMatcher matcher = SerializationUtils.deserializeJson(choices, ChoiceActionMatcher.class);
        this.question = matcher.getQuestion();
        this.choices = matcher.getChoices();
    }

    @Override
    protected void executeTrigger() {
        Messages.addToFront(new MessageUpdate(this.question).withChoices(this.choices));
    }
}
