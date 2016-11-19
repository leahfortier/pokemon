package mapMaker.dialogs;

import mapMaker.dialogs.action.ActionListPanel;
import pattern.map.EventMatcher;
import util.GUIUtils;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class EventTriggerDialog extends TriggerDialog<EventMatcher> {
	private static final long serialVersionUID = -1493772382824925408L;

	private final JPanel nameComponent;
	private final JPanel conditionComponent;

	private final JTextField nameTextField;
	private final JTextArea conditionTextArea;
	private final ActionListPanel actionListPanel;

	public EventTriggerDialog() {
		this.nameTextField = new JTextField();
		this.conditionTextArea = new JTextArea();
		this.actionListPanel = new ActionListPanel(this);

		nameComponent = GUIUtils.createTextFieldComponent("Name", nameTextField);
		conditionComponent = GUIUtils.createTextAreaComponent("Condition", conditionTextArea);
	}

	@Override
	public void renderDialog() {
		removeAll();
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
