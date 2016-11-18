package mapMaker.dialogs;

import mapMaker.dialogs.action.ActionListPanel;
import pattern.map.EventMatcher;
import util.GUIUtils;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class EventTriggerDialog extends TriggerDialog<EventMatcher> {
	private static final long serialVersionUID = -1493772382824925408L;
	
	private JTextField nameTextField;
	private JTextArea conditionTextArea;
	private ActionListPanel actionListPanel;

	public EventTriggerDialog() {
		this.nameTextField = new JTextField();
		this.conditionTextArea = new JTextArea();
		this.actionListPanel = new ActionListPanel();

		JPanel nameComponent = GUIUtils.createTextFieldComponent("Name", nameTextField);
		JPanel conditionComponent = GUIUtils.createTextAreaComponent("Condition", conditionTextArea);

		GUIUtils.setVerticalLayout(this, nameComponent, conditionComponent, actionListPanel);
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
