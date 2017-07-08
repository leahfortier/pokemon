package mapMaker.dialogs.wildbattle;

import map.overworld.WildEncounter;
import pokemon.PokemonNamesies;
import util.ColorDocumentListener.ColorCondition;
import util.GUIUtils;

import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;

class WildPokemonDataPanel extends JPanel {
	
	private static final long serialVersionUID = -7408589859784929623L;

	private final JTextField pokemonTextField;
	private final JFormattedTextField probabilityFormattedTextField;
	private final JCheckBox selectedCheckBox;

	private int minLevel;
	private int maxLevel;
	
	WildPokemonDataPanel(WildEncounter wildEncounter) {
		selectedCheckBox = GUIUtils.createCheckBox();
		pokemonTextField = GUIUtils.createColorConditionTextField(new ColorCondition() {
			@Override
			public boolean greenCondition() {
				return PokemonNamesies.tryValueOf(pokemonTextField.getText().trim()) != null;
			}
		});
		
		probabilityFormattedTextField = GUIUtils.createIntegerTextField(100, 1, 100);

		GUIUtils.setHorizontalLayout(
				this,
				selectedCheckBox,
				pokemonTextField,
				probabilityFormattedTextField
		);

		this.load(wildEncounter);
	}

	public void setMinAndMaxLevel(int minLevel, int maxLevel) {
		this.minLevel = minLevel;
		this.maxLevel = maxLevel;
	}

	public boolean isSelected() {
		return this.selectedCheckBox.isSelected();
	}

	WildEncounter getWildEncounter() {
		String pokemon = pokemonTextField.getText();
		String probability = probabilityFormattedTextField.getText();

		return new WildEncounter(pokemon, minLevel, maxLevel, probability);
	}

	private void load(WildEncounter wildEncounter) {
		if (wildEncounter == null) {
			return;
		}

		pokemonTextField.setText(wildEncounter.getPokemonName().getName());
		probabilityFormattedTextField.setValue(wildEncounter.getProbability());
	}
}
