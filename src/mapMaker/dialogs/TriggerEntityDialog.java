package mapMaker.dialogs;

import pattern.AreaDataMatcher.TriggerMatcher;

public class TriggerEntityDialog extends EventTriggerDialog {
	
	private static final long serialVersionUID = -8044906676343275320L;
	
	public void setTriggerEntity (TriggerMatcher triggerEntity, String name){
//		nameTextField.setText(name);
//		triggerTextField.setText(triggerEntity.trigger);
//		conditionTextField.setText(triggerEntity.condition.replace("&"," & ").replace("|"," | "));
	}
	
	public TriggerMatcher getTriggerEntity() {
//		if (triggerTextField.getText().isEmpty())
			return null;
//		return new TriggerEntityData(
//				nameTextField.getText(),
//				"condition: " + conditionTextField.getText().trim().replace(" ", ""),
//				triggerTextField.getText(),
//				-1,
//				-1);
	}
}
