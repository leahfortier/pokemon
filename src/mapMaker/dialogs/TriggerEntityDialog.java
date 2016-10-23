package mapMaker.dialogs;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;

import map.entity.TriggerEntityData;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

public class TriggerEntityDialog extends JPanel {
	
	private static final long serialVersionUID = -8044906676343275320L;
	
	private JTextField conditionTextField;
	private JTextField nameTextField;
	private JTextField triggerTextField;
	
	public TriggerEntityDialog() {
		
		JLabel lblName = new JLabel("Name");
		
		JLabel lblTrigger = new JLabel("Trigger");
		
		JLabel lblCondition = new JLabel("Condition");
		
		conditionTextField = new JTextField();
		conditionTextField.setColumns(10);
		
		nameTextField = new JTextField();
		nameTextField.setColumns(10);
		
		triggerTextField = new JTextField();
		triggerTextField.setColumns(10);
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblName, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
							.addGap(17)
							.addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, 210, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblTrigger, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
							.addGap(17)
							.addComponent(triggerTextField, GroupLayout.PREFERRED_SIZE, 210, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblCondition, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE)
							.addGap(6)
							.addComponent(conditionTextField, GroupLayout.PREFERRED_SIZE, 210, GroupLayout.PREFERRED_SIZE))))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(lblName)
						.addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
					.addGap(6)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(lblTrigger)
						.addComponent(triggerTextField, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
					.addGap(6)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(lblCondition)
						.addComponent(conditionTextField, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)))
		);
		setLayout(groupLayout);
	}
	
	public void setTriggerEntity (TriggerEntityData triggerEntity, String name){
		nameTextField.setText(name);
		triggerTextField.setText(triggerEntity.trigger);
		conditionTextField.setText(triggerEntity.condition.getOriginalConditionString().replace("&"," & ").replace("|"," | "));
	}
	
	public TriggerEntityData getTriggerEntity() {
		if (triggerTextField.getText().isEmpty())
			return null;
		return new TriggerEntityData(
				nameTextField.getText(),
				"condition: " + conditionTextField.getText().trim().replace(" ", ""),
				triggerTextField.getText(),
				-1,
				-1);
	}
}
