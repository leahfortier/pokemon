package mapMaker.dialogs.action.panel;

import mapMaker.dialogs.action.ActionPanel;
import pattern.action.ActionMatcher.MoveNpcActionMatcher;
import util.GUIUtils;

import javax.swing.JCheckBox;
import javax.swing.JTextField;

public class MoveNpcActionPanel extends ActionPanel<MoveNpcActionMatcher> {
    private final JTextField entityNameField; // TODO: Change to combo box with NPCs
    private final JCheckBox endPlayerCheckbox;
    private final JTextField endEntranceField; // TODO: Change to combo box with entrances

    public MoveNpcActionPanel() {
        this.entityNameField = GUIUtils.createTextField();
        this.endPlayerCheckbox = GUIUtils.createCheckBox("End at Player", action -> setEnabled());
        this.endEntranceField = GUIUtils.createTextField();

        GUIUtils.setVerticalLayout(
                this,
                GUIUtils.createTextFieldComponent("NPC Entity Name", entityNameField),
                endPlayerCheckbox,
                GUIUtils.createTextFieldComponent("End Entrance Name", endEntranceField)
        );
    }

    private void setEnabled() {
        endEntranceField.setEnabled(!endPlayerCheckbox.isSelected());
    }

    @Override
    public MoveNpcActionMatcher getActionMatcher() {
        return new MoveNpcActionMatcher(
                this.entityNameField.getText().trim(),
                this.endEntranceField.getText().trim(),
                this.endPlayerCheckbox.isSelected()
        );
    }

    @Override
    protected void load(MoveNpcActionMatcher matcher) {
        this.entityNameField.setText(matcher.getNpcEntityName());
        this.endEntranceField.setText(matcher.getEndEntranceName());
        this.endPlayerCheckbox.setSelected(matcher.endLocationIsPlayer());

        setEnabled();
    }
}
