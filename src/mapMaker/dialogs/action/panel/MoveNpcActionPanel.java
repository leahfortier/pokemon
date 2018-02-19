package mapMaker.dialogs.action.panel;

import mapMaker.dialogs.action.ActionPanel;
import mapMaker.dialogs.action.ActionType;
import pattern.action.ActionMatcher;
import pattern.action.ActionMatcher.MoveNpcActionMatcher;
import util.GUIUtils;

import javax.swing.JCheckBox;
import javax.swing.JTextField;

public class MoveNpcActionPanel extends ActionPanel {
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
    public ActionMatcher getActionMatcher(ActionType actionType) {
        return new MoveNpcActionMatcher(
                this.entityNameField.getText().trim(),
                this.endEntranceField.getText().trim(),
                this.endPlayerCheckbox.isSelected()
        );
    }

    @Override
    protected void load(ActionMatcher matcher) {
        MoveNpcActionMatcher npcMatcher = (MoveNpcActionMatcher)matcher;

        this.entityNameField.setText(npcMatcher.getNpcEntityName());
        this.endEntranceField.setText(npcMatcher.getEndEntranceName());
        this.endPlayerCheckbox.setSelected(npcMatcher.endLocationIsPlayer());

        setEnabled();
    }
}
