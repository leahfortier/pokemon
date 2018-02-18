package mapMaker.dialogs.action;

import pattern.action.ActionMatcher2;
import pattern.action.ActionMatcher2.ChoiceActionMatcher2;
import pattern.action.ChoiceMatcher;
import util.GUIUtils;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;
import java.util.ArrayList;
import java.util.List;

class ChoiceActionPanel extends ActionPanel {

    private final ActionDialog parent;

    private final JTextField questionField;

    private final List<Choice> choices;
    private final JButton newChoiceButton;

    ChoiceActionPanel(ActionDialog parent) {
        this.parent = parent;

        questionField = GUIUtils.createTextField();
        choices = new ArrayList<>();
        newChoiceButton = GUIUtils.createButton(
                "New Choice",
                event -> {
                    choices.add(new Choice());
                    render();
                }
        );

        render();
    }

    @Override
    public ActionMatcher2 getActionMatcher(ActionType actionType) {
        ChoiceMatcher[] choiceMatchers = new ChoiceMatcher[this.choices.size()];
        for (int i = 0; i < choiceMatchers.length; i++) {
            choiceMatchers[i] = new ChoiceMatcher(
                    this.choices.get(i).textField.getText(),
                    this.choices.get(i).actions.toArray(new ActionMatcher2[0])
            );
        }

        return new ChoiceActionMatcher2(
                this.questionField.getText(),
                choiceMatchers
        );
    }

    @Override
    protected void load(ActionMatcher2 matcher) {
        if (matcher == null) {
            return;
        }

        ChoiceActionMatcher2 choiceActionMatcher = (ChoiceActionMatcher2)matcher;
        this.questionField.setText(choiceActionMatcher.getQuestion());

        ChoiceMatcher[] choiceMatchers = choiceActionMatcher.getChoices();
        for (ChoiceMatcher choiceMatcher : choiceMatchers) {
            Choice choice = new Choice();
            choice.textField.setText(choiceMatcher.getText());
            choice.actions.addAll(choiceMatcher.getActionMatchers());

            choices.add(choice);
        }

        render();
    }

    @Override
    public void render() {
        removeAll();

        List<JComponent> components = new ArrayList<>();
        components.add(questionField);

        for (int i = 0; i < choices.size(); i++) {
            int choiceIndex = i;
            final Choice choice = choices.get(i);
            final JComponent[] actionButtons = new JComponent[choice.actions.size()];

            for (int j = 0; j < actionButtons.length; j++) {
                final int actionIndex = j;
                ActionMatcher2 actionMatcher = choice.actions.get(j);
                actionButtons[j] = GUIUtils.createButton(
                        actionMatcher == null ? "EMPTY" : actionMatcher.getActionType().name(),
                        event -> {
                            ActionMatcher2 newActionMatcher = new ActionDialog(actionMatcher).getMatcher(parent);
                            if (newActionMatcher != null) {
                                choices.get(choiceIndex).actions.set(actionIndex, newActionMatcher);
                                render();
                            }
                        }
                );
            }

            components.add(GUIUtils.createHorizontalLayoutComponent(
                    choice.textField,
                    GUIUtils.createHorizontalLayoutComponent(actionButtons),
                    choice.newActionButton
            ));
        }

        components.add(newChoiceButton);

        GUIUtils.setVerticalLayout(this, components.toArray(new JComponent[0]));

        parent.render();
    }

    private class Choice {
        private final JTextField textField;
        private final List<ActionMatcher2> actions;
        private final JButton newActionButton;

        public Choice() {
            this.textField = GUIUtils.createTextField();
            this.actions = new ArrayList<>();
            this.newActionButton = GUIUtils.createButton(
                    "New Action",
                    event -> {
                        this.actions.add(null);
                        render();
                    }
            );
        }
    }
}
