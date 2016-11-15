package mapMaker.dialogs;

import mapMaker.dialogs.action.ActionListPanel;
import pattern.map.EventMatcher;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class EventTriggerDialog extends TriggerDialog<EventMatcher> {
	private static final long serialVersionUID = -1493772382824925408L;
	
	private JTextField nameTextField;
	private JTextArea conditionTextArea;
	private ActionListPanel actionListPanel;

	public EventTriggerDialog() {
		JLabel nameLabel = new JLabel("Name");
		JLabel conditionLabel = new JLabel("Condition");
		
		nameTextField = new JTextField();
		nameTextField.setColumns(10);

		conditionTextArea = new JTextArea();
		conditionTextArea.setColumns(20);
		conditionTextArea.setLineWrap(true);

		this.actionListPanel = new ActionListPanel();

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
						.addComponent(this.actionListPanel)
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
					.addComponent(this.actionListPanel)
				));
		setLayout(groupLayout);
		this.setPanelSize();
	}

	@Override
	public EventMatcher getMatcher() {
		return new EventMatcher(
				nameTextField.getText(),
				conditionTextArea.getText(),
				actionListPanel.getActions()
		);
	}

	@Override
	public void load(EventMatcher matcher) {
		nameTextField.setText(matcher.getBasicName());
		conditionTextArea.setText(matcher.getCondition());
		actionListPanel.load(matcher.getActionMatcherList());
	}
}
