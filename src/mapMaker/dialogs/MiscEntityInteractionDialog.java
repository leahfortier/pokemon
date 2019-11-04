package mapMaker.dialogs;

import mapMaker.dialogs.action.ActionListPanel;
import pattern.action.ActionMatcher;
import pattern.action.MiscEntityInteractionMatcher;
import util.GuiUtils;
import util.string.StringUtils;

import javax.swing.JPanel;
import javax.swing.JTextField;

public class MiscEntityInteractionDialog extends TriggerDialog<MiscEntityInteractionMatcher> {
    private final JPanel topComponent;

    private final JTextField interactionNameTextField;
    private final ActionListPanel actionListPanel;

    private final int interactionIndex;

    MiscEntityInteractionDialog(MiscEntityInteractionMatcher entityInteractionMatcher, int index) {
        super("New Misc Entity Interaction Dialog");

        this.interactionIndex = index;

        interactionNameTextField = GuiUtils.createTextField(this.getDefaultName());
        this.actionListPanel = new ActionListPanel(this);

        this.topComponent = GuiUtils.createTextFieldComponent("Interaction Name", interactionNameTextField);

        this.load(entityInteractionMatcher);
    }

    private String getDefaultName() {
        if (interactionIndex == 0) {
            return "default";
        }

        return "Interaction" + interactionIndex;
    }

    private void load(MiscEntityInteractionMatcher matcher) {
        if (matcher == null) {
            return;
        }

        interactionNameTextField.setText(matcher.getName());
        actionListPanel.load(matcher.getActions());
    }

    @Override
    protected MiscEntityInteractionMatcher getMatcher() {
        String interactionName = interactionNameTextField.getText();
        ActionMatcher[] actions = actionListPanel.getActions();

        if (StringUtils.isNullOrEmpty(interactionName)) {
            interactionName = this.getDefaultName();
        }

        return new MiscEntityInteractionMatcher(interactionName, actions);
    }

    @Override
    protected void renderDialog() {
        removeAll();
        GuiUtils.setVerticalLayout(this, topComponent, actionListPanel);
    }
}
