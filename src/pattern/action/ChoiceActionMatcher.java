package pattern.action;

import map.entity.EntityAction;

import java.util.ArrayList;
import java.util.List;

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

    public static class ChoiceMatcher {
        private String text;
        public ActionMatcher2[] actions;

        public ChoiceMatcher(String text, ActionMatcher[] actions) {
            this.text = text;
//            this.actions = actions;
        }

        public String getText() {
            return this.text;
        }

        public List<ActionMatcher> getActionMatchers() {
            return new ArrayList<>();
//            return Arrays.asList(this.actions);
        }

        public List<EntityAction> getActions() {
            List<EntityAction> actions = new ArrayList<>();
//            for (ActionMatcher action : this.actions) {
//                actions.add(action.getAction(null));
//            }

            return actions;
        }
    }
}
