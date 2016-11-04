package mapMaker.dialogs;

import map.triggers.DialogueTrigger;
import map.triggers.TriggerData;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class DialogueTriggerDialog extends JPanel {
	private static final long serialVersionUID = -1493772382824925408L;
	
	private JTextField nameTextField;
	private JTextField dialogueNameTextField;
	private JTextArea dialogueTextArea;
	private JCheckBox createCheckBox;
	private JTextArea globalTextArea;
	private JTextArea conditionTextArea;

	public DialogueTriggerDialog() {
		JLabel nameLabel = new JLabel("Name");
		JLabel dialogueLabel = new JLabel("Dialogue Name");
		JLabel createDialogueLabel = new JLabel("Create Dialogue");
		JLabel dialogueTextLabel = new JLabel("Dialogue Text");
		JLabel conditionLabel = new JLabel("Condition");
		JLabel globalsLabel = new JLabel("Globals");
		
		nameTextField = new JTextField();
		nameTextField.setColumns(10);
		
		dialogueNameTextField = new JTextField();
		dialogueNameTextField.setColumns(10);
		
		createCheckBox = new JCheckBox("");
		createCheckBox.addActionListener(event -> dialogueTextArea.setEnabled(createCheckBox.isSelected()));
		
		JScrollPane scrollPane = new JScrollPane();
		
		dialogueTextArea = new JTextArea();
		//dialogueTextArea.setBounds(286, 120, 268, 100);
		dialogueTextArea.setEnabled(false);
		
		scrollPane.setViewportView(dialogueTextArea);

		JScrollPane scrollPane_2 = new JScrollPane();
		
		globalTextArea = new JTextArea();
		//globalTextArea.setBounds(6, 120, 268, 100);
		scrollPane_2.setViewportView(globalTextArea);
		//add(globalTextArea);
		//add(dialogueTextArea);
		
		conditionTextArea = new JTextArea();
		conditionTextArea.setColumns(20);
		conditionTextArea.setLineWrap(true);
//		conditionTextArea.setBounds(0, 0, 264, 35);
//		add(conditionTextArea);
//		conditionTextArea.setLineWrap(true);
//		
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
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(dialogueLabel, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE)
								.addComponent(createDialogueLabel, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE))
							.addGap(12)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(dialogueNameTextField, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
								.addComponent(createCheckBox, GroupLayout.PREFERRED_SIZE, 128, GroupLayout.PREFERRED_SIZE)))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(globalsLabel, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE)
							.addGap(174)
							.addComponent(dialogueTextLabel, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(scrollPane_2, GroupLayout.PREFERRED_SIZE, 268, GroupLayout.PREFERRED_SIZE)
							.addGap(12)
							.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 268, GroupLayout.PREFERRED_SIZE))))
		);

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
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(6)
							.addComponent(dialogueLabel)
							.addGap(16)
							.addComponent(createDialogueLabel))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(dialogueNameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addGap(6)
							.addComponent(createCheckBox)))
					.addGap(6)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(globalsLabel)
						.addComponent(dialogueTextLabel))
					.addGap(4)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(scrollPane_2, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
						.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)))
		);
		setLayout(groupLayout);
	}
	
	public void setDialogueTrigger(DialogueTrigger trigger, String name) {
		nameTextField.setText(name);
		conditionTextArea.setText(trigger.getCondition().getOriginalConditionString().replace("&"," & ").replace("|"," | "));
		
		for (String g: trigger.getGlobals()) {
			globalTextArea.append(g + "\n");
		}

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
		String dialogueName = dialogueNameTextField.getText().trim().replace(' ', '_');
		String dialogueString = dialogueName.length() > 0 ? "dialogue: " + dialogueName:"";
		
		String condition = conditionTextArea.getText().trim().replace(" ","");
		String conditionString = condition.length() > 0 ? "condition: " + condition : "";
		
		String[] globals = globalTextArea.getText().trim().length() > 0 ? globalTextArea.getText().trim().split("\n") : null;
		String globalsString = "";
		
		if (globals != null) {
			for (String global: globals) {
				globalsString += "global: " + global.trim() + "\n";
			}
		}

		if (createCheckBox.isSelected()) {
			dialogueString = "createDialogue: " + dialogueName +"\n";
			
			String[] dialogueLines = dialogueTextArea.getText().trim().split("\n");
			for (int currDialogue = 0; currDialogue < dialogueLines.length; ++currDialogue) {
				dialogueString += "text[" + currDialogue + "]: \"" + dialogueLines[currDialogue].trim() + "\"\n";
			}
		}

		String contents = conditionString + "\n" + globalsString + "\n" + dialogueString;
		return null;
//		return new DialogueTrigger(name, contents);
	}
	
	public TriggerData getTriggerData(String name) {
		DialogueTrigger dialogueTrigger = getDialogueTrigger(name);
		return null;
//		return new TriggerData(name, "Dialogue\n" + dialogueTrigger.triggerDataAsString());
	}
}
