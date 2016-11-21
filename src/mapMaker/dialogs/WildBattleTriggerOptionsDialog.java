package mapMaker.dialogs;

import main.Global;
import pattern.map.WildBattleMatcher;
import util.GUIUtils;

import javax.swing.JButton;
import javax.swing.JComboBox;
import java.util.ArrayList;
import java.util.List;

public class WildBattleTriggerOptionsDialog extends TriggerDialog<WildBattleMatcher> {
	private static final long serialVersionUID = -7378035463487486331L;
	
	private JComboBox<String> comboBox;

	private JButton createButton;
	private JButton editButton;
	private List<WildBattleMatcher> wildBattleTriggers;
	
	public WildBattleTriggerOptionsDialog(List<WildBattleMatcher> wildBattleMatchers) {
		super("Wild Battle Trigger Options");

		this.wildBattleTriggers = new ArrayList<>();

		comboBox = GUIUtils.createComboBox(new String[0], null);

		createButton = GUIUtils.createButton(
				"Create New",
				event -> {
					WildBattleMatcher matcher = editWildBattleTrigger(null);
					if (matcher == null) {
						return;
					}

					this.addWildBattleTrigger(matcher);
					comboBox.setSelectedItem(matcher.getBasicName());
				}
		);

		editButton = GUIUtils.createButton(
				"Edit",
				event -> {
					WildBattleMatcher oldMatcher = this.getSelectedTriggerMatcher();
					WildBattleMatcher newMatcher = editWildBattleTrigger(oldMatcher);
					if (newMatcher == null) {
						return;
					}

					if (oldMatcher != null) {
						wildBattleTriggers.remove(oldMatcher);
					}

					addWildBattleTrigger(newMatcher);
				}
		);
		editButton.setEnabled(false);

		this.load(wildBattleMatchers);
	}

	private WildBattleMatcher getSelectedTriggerMatcher() {
		String wildBattleName = (String)comboBox.getSelectedItem();
		for (WildBattleMatcher matcher : wildBattleTriggers) {
			if (wildBattleName.equals(matcher.getBasicName())) {
				return matcher;
			}
		}

		Global.error("No wild battle trigger found with name " + wildBattleName);
		return null;
	}

	private WildBattleMatcher editWildBattleTrigger(WildBattleMatcher wildBattleMatcher) {
		return new WildBattleTriggerEditDialog(wildBattleMatcher, wildBattleTriggers.size()).getMatcher(this);
	}

	@Override
	protected void renderDialog() {
		comboBox.removeAllItems();
		this.wildBattleTriggers.forEach(matcher -> comboBox.addItem(matcher.getBasicName()));

		editButton.setEnabled(!wildBattleTriggers.isEmpty());

		GUIUtils.setVerticalLayout(
				this,
				comboBox,
				GUIUtils.createHorizontalLayoutComponent(createButton, editButton)
		);
	}


	private void addWildBattleTrigger(WildBattleMatcher newMatcher) {
		wildBattleTriggers.add(newMatcher);
		render();
	}

	@Override
	protected WildBattleMatcher getMatcher() {
		if (this.wildBattleTriggers.isEmpty()) {
			return null;
		}

		return wildBattleTriggers.get(this.comboBox.getSelectedIndex());
	}

	private void load(List<WildBattleMatcher> matchers) {
		if (matchers == null) {
			return;
		}

		matchers.forEach(this::addWildBattleTrigger);
	}
}
