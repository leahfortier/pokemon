package mapMaker.dialogs.action.trigger;

import pattern.MoveNPCTriggerMatcher;
import util.GUIUtils;
import util.SerializationUtils;

import javax.swing.JCheckBox;
import javax.swing.JTextField;

class MoveNPCTriggerPanel extends TriggerContentsPanel {
    private final JTextField entityNameField; // TODO: Change to combo box with NPCs
    private final JCheckBox endPlayerCheckbox;
    private final JTextField endEntranceField; // TODO: Change to combo box with entrances
    
    MoveNPCTriggerPanel() {
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
    protected void load(String triggerContents) {
        MoveNPCTriggerMatcher matcher = SerializationUtils.deserializeJson(triggerContents, MoveNPCTriggerMatcher.class);
        
        this.entityNameField.setText(matcher.getNpcEntityName());
        this.endEntranceField.setText(matcher.getEndEntranceName());
        this.endPlayerCheckbox.setSelected(matcher.endLocationIsPlayer());
        
        setEnabled();
    }
    
    @Override
    protected String getTriggerContents() {
        MoveNPCTriggerMatcher matcher = new MoveNPCTriggerMatcher(
                this.entityNameField.getText().trim(),
                this.endEntranceField.getText().trim(),
                this.endPlayerCheckbox.isSelected()
        );
        
        return SerializationUtils.getJson(matcher);
    }
}
