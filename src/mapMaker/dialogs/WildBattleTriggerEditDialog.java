package mapMaker.dialogs;

import map.EncounterRate;
import map.WildEncounter;
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
	private static final long serialVersionUID = -3454589908432207758L;

	private final JPanel topComponent;
	private final JPanel bottomComponent;

	private final JTextField nameTextField;
	private final JComboBox<EncounterRate> rateComboBox;

	private final List<WildPokemonDataPanel> wildPokemonPanels;

	private final int index;
	
	public WildBattleTriggerEditDialog(WildBattleMatcher wildBattleMatcher, int index) {
		super("Wild Battle Trigger Editor");

		this.index = index;

		wildPokemonPanels = new ArrayList<>();
		
		nameTextField = GUIUtils.createTextField(this.getDefaultName());
		rateComboBox = GUIUtils.createComboBox(EncounterRate.values(), null);

		JButton addPokemonButton = GUIUtils.createButton("Add Pokemon", event -> addPokemonPanel(null));
		JButton removeSelectedButton = GUIUtils.createButton(
				"Remove Selected",
				event -> {
					wildPokemonPanels.removeIf(WildPokemonDataPanel::isSelected);
					render();
				}
		);

		this.topComponent = GUIUtils.createHorizontalLayoutComponent(
				GUIUtils.createTextFieldComponent("Name", nameTextField),
				GUIUtils.createComboBoxComponent("Encounter Rate", rateComboBox)
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
		EncounterRate encounterRate = (EncounterRate)rateComboBox.getSelectedItem();
		List<WildEncounter> wildEncounters = wildPokemonPanels
				.stream()
				.map(WildPokemonDataPanel::getWildEncounter)
				.collect(Collectors.toList());

		return new WildBattleMatcher(name, encounterRate, wildEncounters);
	}

	private void load(WildBattleMatcher matcher) {
		if (matcher == null) {
			return;
		}

		nameTextField.setText(matcher.getBasicName());
		rateComboBox.setSelectedItem(matcher.getEncounterRate());

		for (WildEncounter wildEncounter : matcher.getWildEncounters()) {
			addPokemonPanel(wildEncounter);
		}
	}
}
