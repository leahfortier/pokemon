package pattern.action;

import map.entity.EntityAction;

import java.util.ArrayList;
import java.util.List;

public class ChoiceActionMatcher {
    public String question;
    public ChoiceMatcher[] choices;

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
}
