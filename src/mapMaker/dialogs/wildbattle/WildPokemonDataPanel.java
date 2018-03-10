package mapMaker.dialogs.wildbattle;

import map.overworld.WildEncounterInfo;
import pokemon.PokemonNamesies;
import util.ColorDocumentListener.ColorCondition;
import util.GuiUtils;

import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

class WildPokemonDataPanel extends JPanel {
    private final JTextField pokemonTextField;
    private final JFormattedTextField probabilityFormattedTextField;
    private final JCheckBox selectedCheckBox;

    private PokemonProbabilityListener probabilityListener;

    private int probability;

    WildPokemonDataPanel(WildEncounterInfo wildEncounter) {
        selectedCheckBox = GuiUtils.createCheckBox();
        pokemonTextField = GuiUtils.createColorConditionTextField(new ColorCondition() {
            @Override
            public boolean greenCondition() {
                return PokemonNamesies.tryValueOf(pokemonTextField.getText().trim()) != null;
            }
        });

        probabilityFormattedTextField = GuiUtils.createIntegerTextField(
                wildEncounter == null ? 0 : wildEncounter.getProbability(),
                1,
                100
        );
        probabilityFormattedTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateProbability();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateProbability();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateProbability();
            }
        });

        GuiUtils.setHorizontalLayout(
                this,
                selectedCheckBox,
                pokemonTextField,
                probabilityFormattedTextField
        );

        this.load(wildEncounter);
    }

    private void updateProbability() {
        int oldProbability = probability;
        probability = (int)probabilityFormattedTextField.getValue();
        if (probabilityListener != null) {
            probabilityListener.updatePokemonProbability(oldProbability, probability);
        }
    }

    public void setProbabilityListener(PokemonProbabilityListener listener) {
        this.probabilityListener = listener;
    }

    public boolean isSelected() {
        return this.selectedCheckBox.isSelected();
    }

    WildEncounterInfo getWildEncounter(int minLevel, int maxLevel) {
        String pokemon = pokemonTextField.getText();
        String probability = probabilityFormattedTextField.getText();

        return new WildEncounterInfo(pokemon, minLevel, maxLevel, probability);
    }

    private void load(WildEncounterInfo wildEncounter) {
        if (wildEncounter == null) {
            return;
        }

        pokemonTextField.setText(wildEncounter.getPokemonName().getName());
        probabilityFormattedTextField.setValue(wildEncounter.getProbability());
    }
}
