package map.triggers;

import map.condition.Condition;
import message.MessageUpdate;
import message.Messages;
import pattern.action.ActionMatcher.ChoiceActionMatcher;
import pattern.action.ChoiceMatcher;
import util.SerializationUtils;

public class ChoiceTrigger extends Trigger {
    private final String question;
    private final ChoiceMatcher[] choices;

    ChoiceTrigger(String choices, Condition condition) {
        this(SerializationUtils.deserializeJson(choices, ChoiceActionMatcher.class), condition);
    }

    public ChoiceTrigger(ChoiceActionMatcher matcher, Condition condition) {
        super(matcher.getJson(), condition);
        this.question = matcher.getQuestion();
        this.choices = matcher.getChoices();
    }

    @Override
    protected void executeTrigger() {
        Messages.addToFront(new MessageUpdate(this.question).withChoices(this.choices));
    }
}
