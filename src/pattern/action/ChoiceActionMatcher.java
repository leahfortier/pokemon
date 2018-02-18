package pattern.action;

public class ChoiceActionMatcher {
    public String question;
    public ChoiceMatcher[] choices;

    public ChoiceActionMatcher(String question, ChoiceMatcher[] choices) {
        this.question = question;
        this.choices = choices;
    }

    public String getQuestion() {
        return this.question;
    }

    public ChoiceMatcher[] getChoices() {
        return this.choices;
    }
}
