package mapMaker.dialogs.wildbattle;

import map.condition.Condition;
import map.overworld.EncounterRate;
import map.overworld.WildEncounter;
import mapMaker.dialogs.TimeOfDayPanel;
import mapMaker.dialogs.TriggerDialog;
import pattern.map.WildBattleMatcher;
import pokemon.ActivePokemon;
import util.GUIUtils;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
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

	private final JFormattedTextField lowLevelFormattedTextField;
	private final JFormattedTextField highLevelFormattedTextField;

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

		lowLevelFormattedTextField = GUIUtils.createIntegerTextField(1, 1, ActivePokemon.MAX_LEVEL);
		highLevelFormattedTextField = GUIUtils.createIntegerTextField(ActivePokemon.MAX_LEVEL, 1, ActivePokemon.MAX_LEVEL);

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
						GUIUtils.createComboBoxComponent("Encounter Rate", encounterRateComboBox),
						lowLevelFormattedTextField,
						highLevelFormattedTextField
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
			components.add(GUIUtils.createLabel("     Pokemon Name                 	  Probability"));
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
		int minLevel = Integer.parseInt(lowLevelFormattedTextField.getText());
		int maxLevel = Integer.parseInt(highLevelFormattedTextField.getText());
		this.updatePokemonPanelsWithLevels(minLevel, maxLevel);
		List<WildEncounter> wildEncounters = wildPokemonPanels
				.stream()
				.map(WildPokemonDataPanel::getWildEncounter)
				.collect(Collectors.toList());
		String condition = Condition.and(timeOfDayPanel.getCondition(), conditionTextField.getText());

		WildBattleMatcher matcher = new WildBattleMatcher(
				name,
				encounterRate,
				minLevel,
				maxLevel,
				wildEncounters
		);
		matcher.setCondition(condition);

		return matcher;
	}

	private void load(WildBattleMatcher matcher) {
		if (matcher == null) {
			return;
		}

		nameTextField.setText(matcher.getName());
		encounterRateComboBox.setSelectedItem(matcher.getEncounterRate());
		conditionTextField.setText(matcher.getCondition());
		lowLevelFormattedTextField.setValue(matcher.getMinLevel());
		highLevelFormattedTextField.setValue(matcher.getMaxLevel());

		for (WildEncounter wildEncounter : matcher.getWildEncounters()) {
			addPokemonPanel(wildEncounter);
		}
	}

	private void updatePokemonPanelsWithLevels(int minLevel, int maxLevel) {
		for (WildPokemonDataPanel wildPokemonDataPanel : this.wildPokemonPanels) {
			wildPokemonDataPanel.setMinAndMaxLevel(minLevel, maxLevel);
		}
	}
}
