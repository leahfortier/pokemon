package mapMaker.dialogs.wildbattle;

import map.Condition;
import map.overworld.EncounterRate;
import map.overworld.WildEncounter;
import mapMaker.dialogs.TimeOfDayPanel;
import mapMaker.dialogs.TriggerDialog;
import pattern.map.WildBattleMatcher;
import util.GUIUtils;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WildBattleTriggerEditDialog extends TriggerDialog<WildBattleMatcher> {
	private final JPanel topComponent;
	private final JPanel bottomComponent;

	private final JTextField nameTextField;
	private final JComboBox<EncounterRate> encounterRateComboBox;

	private final TimeOfDayPanel timeOfDayPanel;
	private final JTextField conditionTextField;

	private final List<WildPokemonDataPanel> wildPokemonPanels;

	private final int index;

	public WildBattleTriggerEditDialog(WildBattleMatcher wildBattleMatcher, int index) {
		super("Wild Battle Trigger Editor");

		this.index = index;

		wildPokemonPanels = new ArrayList<>();
		
		nameTextField = GUIUtils.createTextField(this.getDefaultName());
		encounterRateComboBox = GUIUtils.createComboBox(EncounterRate.values());

		timeOfDayPanel = new TimeOfDayPanel();
		conditionTextField = GUIUtils.createTextField();

		JButton addPokemonButton = GUIUtils.createButton("Add Pokemon", event -> addPokemonPanel(null));
		JButton removeSelectedButton = GUIUtils.createButton(
				"Remove Selected",
				event -> {
					wildPokemonPanels.removeIf(WildPokemonDataPanel::isSelected);
					render();
				}
		);

		this.topComponent = GUIUtils.createVerticalLayoutComponent(
				GUIUtils.createHorizontalLayoutComponent(
						GUIUtils.createTextFieldComponent("Name", nameTextField),
						GUIUtils.createComboBoxComponent("Encounter Rate", encounterRateComboBox)
				),
				GUIUtils.createHorizontalLayoutComponent(
						timeOfDayPanel,
						GUIUtils.createTextFieldComponent("Condition", conditionTextField)
				)
		);


		this.bottomComponent = GUIUtils.createHorizontalLayoutComponent(
				addPokemonButton,
				removeSelectedButton
		);

		this.load(wildBattleMatcher);
	}

	private void addPokemonPanel(WildEncounter wildEncounter) {
		wildPokemonPanels.add(new WildPokemonDataPanel(wildEncounter));
		render();
	}

	@Override
	protected void renderDialog() {
		removeAll();

		List<JComponent> components = new ArrayList<>();
		components.add(topComponent);
		if (!wildPokemonPanels.isEmpty()) {
			components.add(GUIUtils.createLabel("     Pokemon Name     Probability       Min Level        Max Level"));
		}
		components.addAll(wildPokemonPanels);
		components.add(bottomComponent);

		GUIUtils.setVerticalLayout(this, components.toArray(new JComponent[0]));
	}

	private String getDefaultName() {
		return "Wild Battle " + index;
	}

	@Override
	protected WildBattleMatcher getMatcher() {
		if (wildPokemonPanels.isEmpty()) {
			return null;
		}

		String name = this.getNameField(nameTextField, this.getDefaultName());
		EncounterRate encounterRate = (EncounterRate)encounterRateComboBox.getSelectedItem();
		List<WildEncounter> wildEncounters = wildPokemonPanels
				.stream()
				.map(WildPokemonDataPanel::getWildEncounter)
				.collect(Collectors.toList());
		String condition = Condition.and(timeOfDayPanel.getCondition(), conditionTextField.getText());

		WildBattleMatcher matcher = new WildBattleMatcher(name, encounterRate, wildEncounters);;
		matcher.setCondition(condition);

		return matcher;
	}

	private void load(WildBattleMatcher matcher) {
		if (matcher == null) {
			return;
		}

		nameTextField.setText(matcher.getName());


		for (WildEncounter wildEncounter : matcher.getWildEncounters()) {
			addPokemonPanel(wildEncounter);
		}
	}
}
