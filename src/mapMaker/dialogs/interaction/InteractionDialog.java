package mapMaker.dialogs.interaction;

import mapMaker.dialogs.TriggerDialog;
import mapMaker.dialogs.action.ActionListPanel;
import pattern.interaction.InteractionMatcher;
import util.GuiUtils;
import util.string.StringUtils;

import javax.swing.JPanel;
import javax.swing.JTextField;

public abstract class InteractionDialog<InteractionType extends InteractionMatcher> extends TriggerDialog<InteractionType> {
    protected JPanel topComponent;

    private final JTextField interactionNameTextField;
    protected final ActionListPanel actionListPanel;

    private final int interactionIndex;

    public InteractionDialog(InteractionType interactionMatcher, int index) {
        super("New Entity Interaction Dialog");

        this.interactionIndex = index;

        interactionNameTextField = GuiUtils.createTextField(this.getDefaultName());
        this.actionListPanel = new ActionListPanel(this);

        this.topComponent = GuiUtils.createTextFieldComponent("Interaction Name", interactionNameTextField);

        this.load(interactionMatcher);
    }

    private String getDefaultName() {
        if (interactionIndex == 0) {
            return "default";
        }

        return "Interaction" + interactionIndex;
    }

    protected String getInteractionName() {
        String interactionName = interactionNameTextField.getText();
        if (!StringUtils.isNullOrEmpty(interactionName)) {
            return interactionName;
        }

        return this.getDefaultName();
    }

    protected void load(InteractionType matcher) {
        if (matcher == null) {
            return;
        }

        interactionNameTextField.setText(matcher.getName());
        actionListPanel.load(matcher.getActions());
    }

    @Override
    protected void renderDialog() {
        removeAll();
        GuiUtils.setVerticalLayout(this, topComponent, actionListPanel);
    }
}
