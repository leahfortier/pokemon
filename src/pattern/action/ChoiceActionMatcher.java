package pattern.action;

import map.entity.EntityAction;

import java.util.ArrayList;
import java.util.List;

public class ChoiceActionMatcher {
    private String question;
    private ChoiceMatcher[] choices;

    public static class ChoiceMatcher {
        public String text;
        private ActionMatcher[] actions;

        public List<EntityAction> getActions() {
            List<EntityAction> actions = new ArrayList<>();
            for (ActionMatcher action : this.actions) {
                actions.add(action.getAction(null));
            }

            return actions;
        }
    }

    public String getQuestion() {
        return this.question;
    }

    public ChoiceMatcher[] getChoices() {
        return this.choices;
    }
}
