package mapMaker.dialogs;

import map.triggers.DialogueTrigger;
import pattern.AreaDataMatcher.TriggerMatcher;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class EventTriggerDialog extends TriggerDialog {
	private static final long serialVersionUID = -1493772382824925408L;
	
	private JTextField nameTextField;
	private JTextArea conditionTextArea;
	private ActionListPanel ninininini;

	public EventTriggerDialog() {
		JLabel nameLabel = new JLabel("Name");
		JLabel conditionLabel = new JLabel("Condition");
		
		nameTextField = new JTextField();
		nameTextField.setColumns(10);

		conditionTextArea = new JTextArea();
		conditionTextArea.setColumns(20);
		conditionTextArea.setLineWrap(true);

		this.ninininini = new ActionListPanel();

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		//JTextArea textArea = new JTextArea();
		scrollPane_1.setViewportView(conditionTextArea);
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(nameLabel, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
							.addGap(12)
							.addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, 211, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(conditionLabel, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE)
								.addComponent(scrollPane_1, GroupLayout.PREFERRED_SIZE, 268, GroupLayout.PREFERRED_SIZE))
							.addGap(12)
						)
						.addComponent(this.ninininini)
					)));

		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(6)
							.addComponent(nameLabel))
						.addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(6)
							.addComponent(conditionLabel)
							.addGap(5)
							.addComponent(scrollPane_1, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE))
					)
					.addComponent(this.ninininini)
				));
		setLayout(groupLayout);
		this.setPanelSize();
	}

	public void setDialogueTrigger(DialogueTrigger trigger, String name) {
//		nameTextField.setText(name);
//		conditionTextArea.setText(trigger.getCondition().getOriginalConditionString().replace("&"," & ").replace("|"," | "));
//
//		for (String g: trigger.getGlobals()) {
//			globalTextArea.append(g + "\n");
//		}

//		// TODO: Once I figure out what is actually happening here figure out if this makes sense to be a method
//		dialogueNameTextField.setText(trigger.createDialogue ? trigger.dialogue : trigger.dialogueName);
//
//		if (trigger.createDialogue) {
//			createCheckBox.setSelected(true);
//			for (String d: trigger.dialogueLines) {
//				dialogueTextArea.append(d + "\n");
//			}
//		}
//
//		dialogueTextArea.setEnabled(trigger.createDialogue);
	}

	public String getDialogueTriggerName() {
		return nameTextField.getText().trim().replace(' ', '_');
	}
	
	private DialogueTrigger getDialogueTrigger(String name) {
//		String dialogueName = dialogueNameTextField.getText().trim().replace(' ', '_');
//		String dialogueString = dialogueName.length() > 0 ? "dialogue: " + dialogueName:"";
//
//		String condition = conditionTextArea.getText().trim().replace(" ","");
//		String conditionString = condition.length() > 0 ? "condition: " + condition : "";
//
//		String[] globals = globalTextArea.getText().trim().length() > 0 ? globalTextArea.getText().trim().split("\n") : null;
//		String globalsString = "";
//
//		if (globals != null) {
//			for (String global: globals) {
//				globalsString += "global: " + global.trim() + "\n";
//			}
//		}
//
//		if (createCheckBox.isSelected()) {
//			dialogueString = "createDialogue: " + dialogueName +"\n";
//
//			String[] dialogueLines = dialogueTextArea.getText().trim().split("\n");
//			for (int currDialogue = 0; currDialogue < dialogueLines.length; ++currDialogue) {
//				dialogueString += "text[" + currDialogue + "]: \"" + dialogueLines[currDialogue].trim() + "\"\n";
//			}
//		}
//
//		String contents = conditionString + "\n" + globalsString + "\n" + dialogueString;
		return null;
//		return new DialogueTrigger(name, contents);
	}
	
	public TriggerMatcher getTriggerData(String name) {
		DialogueTrigger dialogueTrigger = getDialogueTrigger(name);
		return null;
//		return new TriggerData(name, "Dialogue\n" + dialogueTrigger.triggerDataAsString());
	}
}
