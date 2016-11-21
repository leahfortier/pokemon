package mapMaker.dialogs;

import mapMaker.dialogs.action.ActionListPanel;
import pattern.action.ActionMatcher;
import pattern.map.EventMatcher;
import util.GUIUtils;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class EventTriggerDialog extends TriggerDialog<EventMatcher> {
	private static final long serialVersionUID = -1493772382824925408L;

	private final JPanel topComponent;

	private final JTextField nameTextField;
	private final JTextArea conditionTextArea;
	private final ActionListPanel actionListPanel;

	public EventTriggerDialog(EventMatcher eventMatcher) {
		super("Event Trigger Editor");

		this.nameTextField = GUIUtils.createTextField();
		this.conditionTextArea = GUIUtils.createTextArea();
		this.actionListPanel = new ActionListPanel(this);

		JPanel nameComponent = GUIUtils.createTextFieldComponent("Name", nameTextField);
		JPanel conditionComponent = GUIUtils.createTextAreaComponent("Condition", conditionTextArea);

		this.topComponent = GUIUtils.createVerticalLayoutComponent(nameComponent, conditionComponent);

		this.load(eventMatcher);
	}

	@Override
	public void renderDialog() {
		removeAll();
		GUIUtils.setVerticalLayout(this, topComponent, actionListPanel);
	}

	@Override
	protected EventMatcher getMatcher() {
		ActionMatcher[] actions = actionListPanel.getActions();
		if (actions == null || actions.length == 0) {
			System.err.println("Need at least one action for a valid event trigger.");
			return null;
		}

		return new EventMatcher(
				nameTextField.getText(),
				conditionTextArea.getText(),
				actions
		);
	}

	private void load(EventMatcher matcher) {
		if (matcher == null) {
			return;
		}

		nameTextField.setText(matcher.getBasicName());
		conditionTextArea.setText(matcher.getCondition());
		actionListPanel.load(matcher.getActionMatcherList());
	}
}
