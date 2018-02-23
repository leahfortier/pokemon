package map.triggers;

import message.MessageUpdate;
import message.Messages;
import pattern.action.ActionMatcher.ChoiceActionMatcher;
import pattern.action.ChoiceMatcher;

public class ChoiceTrigger extends Trigger {
    private final String question;
    private final ChoiceMatcher[] choices;

    public ChoiceTrigger(ChoiceActionMatcher matcher) {
        this.question = matcher.getQuestion();
        this.choices = matcher.getChoices();
    }

    @Override
    public void execute() {
        Messages.addToFront(new MessageUpdate(this.question).withChoices(this.choices));
    }
}
