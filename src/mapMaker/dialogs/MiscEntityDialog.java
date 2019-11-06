package mapMaker.dialogs;

import main.Global;
import mapMaker.dialogs.interaction.BasicInteractionDialog;
import mapMaker.dialogs.interaction.InteractionDialog;
import mapMaker.dialogs.interaction.InteractionListPanel;
import pattern.interaction.InteractionMatcher;
import pattern.map.MiscEntityMatcher;
import util.GuiUtils;

import javax.swing.JPanel;
import javax.swing.JTextField;
import java.util.List;

public class MiscEntityDialog extends TriggerDialog<MiscEntityMatcher> {

    private final JPanel topComponent;

    private final JTextField nameTextField;
    private final ConditionPanel conditionPanel;

    private final InteractionListPanel<InteractionMatcher> interactionsPanel;

    public MiscEntityDialog(MiscEntityMatcher matcher) {
        super("Misc Trigger Editor");

        this.nameTextField = GuiUtils.createTextField();
        this.conditionPanel = new ConditionPanel();

        this.interactionsPanel = new InteractionListPanel<>(this) {
            @Override
            protected InteractionDialog<InteractionMatcher> getInteractionDialog(InteractionMatcher matcher, int index) {
                return new BasicInteractionDialog(matcher, index);
            }
        };

        this.topComponent = GuiUtils.createVerticalLayoutComponent(
                GuiUtils.createTextFieldComponent("Name", nameTextField),
                conditionPanel
        );

        this.load(matcher);
    }

    @Override
    public void renderDialog() {
        removeAll();
        GuiUtils.setVerticalLayout(this, topComponent, interactionsPanel);
    }

    @Override
    protected MiscEntityMatcher getMatcher() {
        List<InteractionMatcher> interactions = this.interactionsPanel.getInteractions();
        if (interactions.size() == 0) {
            Global.info("Need at least one interaction for a valid misc entity.");
            return null;
        }

        return new MiscEntityMatcher(
                this.getNameField(nameTextField),
                conditionPanel.getConditionName(),
                conditionPanel.getConditionSet(),
                interactions
        );
    }

    private void load(MiscEntityMatcher matcher) {
        if (matcher == null) {
            return;
        }

        nameTextField.setText(matcher.getBasicName());
        conditionPanel.load(matcher);
        interactionsPanel.load(matcher.getInteractionMatcherList());
    }
}
