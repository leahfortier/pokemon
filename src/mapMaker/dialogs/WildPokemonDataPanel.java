package mapMaker.dialogs;

import map.WildEncounter;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;
import util.ColorDocumentListener;
import util.GUIUtils;
import util.StringUtils;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;

class WildPokemonDataPanel extends JPanel {
	
	private static final long serialVersionUID = -7408589859784929623L;

	private final JTextField pokemonTextField;
	private final JFormattedTextField probabilityFormattedTextField;
	private final JFormattedTextField lowLevelFormattedTextField;
	private final JFormattedTextField highLevelFormattedTextField;
	private final JCheckBox selectedCheckBox;
	
	WildPokemonDataPanel(WildEncounter wildEncounter) {
		
		selectedCheckBox = new JCheckBox();
		pokemonTextField = new JTextField();

		pokemonTextField.getDocument().addDocumentListener(new ColorDocumentListener() {
			@Override
			protected boolean greenCondition() {
				return PokemonNamesies.tryValueOf(pokemonTextField.getText().trim()) != null;
			}

			@Override
			protected JComponent colorComponent() {
				return pokemonTextField;
			}
		});

		pokemonTextField.setText(StringUtils.empty());
		
		probabilityFormattedTextField = GUIUtils.createIntegerTextField(100, 1, 100);
		lowLevelFormattedTextField = GUIUtils.createIntegerTextField(1, 1, ActivePokemon.MAX_LEVEL);
		highLevelFormattedTextField = GUIUtils.createIntegerTextField(ActivePokemon.MAX_LEVEL, 1, ActivePokemon.MAX_LEVEL);

		GUIUtils.setHorizontalLayout(
				this,
				selectedCheckBox,
				pokemonTextField,
				probabilityFormattedTextField,
				lowLevelFormattedTextField,
				highLevelFormattedTextField
		);

		this.load(wildEncounter);
	}

	public boolean isSelected() {
		return this.selectedCheckBox.isSelected();
	}

	WildEncounter getWildEncounter() {
		String pokemon = pokemonTextField.getText();
		String minLevel = lowLevelFormattedTextField.getText();
		String maxLevel = highLevelFormattedTextField.getText();
		String probability = probabilityFormattedTextField.getText();

		return new WildEncounter(pokemon, minLevel, maxLevel, probability);
	}

	private void load(WildEncounter wildEncounter) {
		if (wildEncounter == null) {
			return;
		}

		pokemonTextField.setText(wildEncounter.getPokemonName());
		probabilityFormattedTextField.setValue(wildEncounter.getProbability());
		lowLevelFormattedTextField.setValue(wildEncounter.getMinLevel());
		highLevelFormattedTextField.setValue(wildEncounter.getMaxLevel());
	}
}
