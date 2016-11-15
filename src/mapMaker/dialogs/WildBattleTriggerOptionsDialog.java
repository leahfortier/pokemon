package mapMaker.dialogs;

import main.Global;
import pattern.map.WildBattleMatcher;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import java.util.ArrayList;
import java.util.List;

public class WildBattleTriggerOptionsDialog extends TriggerDialog<List<WildBattleMatcher>> {
	private static final long serialVersionUID = -7378035463487486331L;
	
	private JComboBox<String> comboBox;

	private JButton editButton;
	private List<WildBattleMatcher> wildBattleTriggers;
	
	public WildBattleTriggerOptionsDialog() {
		JButton createButton = new JButton("Create New");
		createButton.addActionListener(event -> {
			WildBattleMatcher matcher = editWildBattleTrigger(null);
            if (matcher == null) {
				return;
			}

			this.addWildBattleTrigger(matcher);
        });

		comboBox = new JComboBox<>();

		this.wildBattleTriggers = new ArrayList<>();

		editButton = new JButton("Edit");
		editButton.setEnabled(false);
		editButton.addActionListener(event -> {
			WildBattleMatcher oldMatcher = this.getSelectedTriggerMatcher();
			WildBattleMatcher newMatcher = editWildBattleTrigger(oldMatcher);
            if (newMatcher == null) {
				return;
			}

			if (oldMatcher != null) {
				wildBattleTriggers.remove(oldMatcher);
			}

			addWildBattleTrigger(newMatcher);
        });
		
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, 246, GroupLayout.PREFERRED_SIZE)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(createButton, GroupLayout.PREFERRED_SIZE, 117, GroupLayout.PREFERRED_SIZE)
							.addGap(12)
							.addComponent(editButton, GroupLayout.PREFERRED_SIZE, 117, GroupLayout.PREFERRED_SIZE))))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(7)
					.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(createButton)
						.addComponent(editButton)))
		);

		setLayout(groupLayout);
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

	private WildBattleMatcher editWildBattleTrigger(WildBattleMatcher eventMatcher) {
		WildBattleTriggerEditDialog dialog = new WildBattleTriggerEditDialog();
		dialog.loadMatcher(eventMatcher);

		if (!dialog.giveOption("Wild Battle Trigger Editor", this)) {
			return null;
		}

		return dialog.getMatcher();
	}

	@Override
	protected void renderDialog() {
		comboBox.removeAllItems();
		this.wildBattleTriggers
				.forEach(matcher ->
						comboBox.addItem(matcher.getBasicName()));

		editButton.setEnabled(!wildBattleTriggers.isEmpty());
	}


	private void addWildBattleTrigger(WildBattleMatcher newMatcher) {
		wildBattleTriggers.add(newMatcher);
		render();
	}

	@Override
	public List<WildBattleMatcher> getMatcher() {
		return this.wildBattleTriggers;
	}

	@Override
	protected void load(List<WildBattleMatcher> matchers) {
		matchers.forEach(this::addWildBattleTrigger);
	}
}
