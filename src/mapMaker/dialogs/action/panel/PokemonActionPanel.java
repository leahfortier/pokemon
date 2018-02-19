package mapMaker.dialogs.action.panel;

import item.ItemNamesies;
import mapMaker.dialogs.action.ActionPanel;
import mapMaker.dialogs.action.ActionType;
import pattern.PokemonMatcher;
import pattern.action.ActionMatcher;
import pattern.action.ActionMatcher.GivePokemonActionMatcher;
import pokemon.PartyPokemon;
import pokemon.PokemonNamesies;
import util.ColorDocumentListener.ColorCondition;
import util.GUIUtils;

import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class PokemonActionPanel extends ActionPanel {
    private final JTextField pokemonNameField;
    private final JFormattedTextField levelField;
    private final JCheckBox isEggCheckBox;
    private final JCheckBox shinyCheckBox;
    private final JTextField itemNameField;

    public PokemonActionPanel() {
        this.pokemonNameField = GUIUtils.createColorConditionTextField(new ColorCondition() {
            @Override
            public boolean greenCondition() {
                return PokemonNamesies.tryValueOf(pokemonNameField.getText().trim()) != null;
            }
        });

        this.levelField = GUIUtils.createIntegerTextField(1, 1, PartyPokemon.MAX_LEVEL);
        this.isEggCheckBox = GUIUtils.createCheckBox("Is Egg", action -> setEnabled());
        this.shinyCheckBox = GUIUtils.createCheckBox("Shiny");

        this.itemNameField = GUIUtils.createColorConditionTextField(new ColorCondition() {
            @Override
            public boolean greenCondition() {
                return ItemNamesies.tryValueOf(itemNameField.getText().trim()) != null;
            }
        });

        JPanel checkBoxComponent = GUIUtils.createHorizontalLayoutComponent(
                this.isEggCheckBox,
                this.shinyCheckBox
        );

        GUIUtils.setVerticalLayout(
                this,
                GUIUtils.createTextFieldComponent("Pokemon", this.pokemonNameField),
                GUIUtils.createTextFieldComponent("Level", this.levelField),
                checkBoxComponent,
                GUIUtils.createTextFieldComponent("Hold Item", this.itemNameField)
        );
    }

    private void setEnabled() {
        boolean isEggy = isEggCheckBox.isSelected();
        levelField.setEnabled(!isEggy);
        shinyCheckBox.setEnabled(!isEggy);
        itemNameField.setEnabled(!isEggy);
    }

    @Override
    public ActionMatcher getActionMatcher(ActionType actionType) {
        final PokemonMatcher matcher;
        if (isEggCheckBox.isSelected()) {
            matcher = PokemonMatcher.createEggMatcher(PokemonNamesies.getValueOf(pokemonNameField.getText()));
        } else {
            matcher = new PokemonMatcher(
                    PokemonNamesies.getValueOf(pokemonNameField.getText()),
                    null,
                    Integer.parseInt(levelField.getText().trim()),
                    shinyCheckBox.isSelected(),
                    null,
                    itemNameField.getText().trim().isEmpty() ? null : ItemNamesies.getValueOf(itemNameField.getText())
            );
        }

        return new GivePokemonActionMatcher(matcher);
    }

    @Override
    protected void load(ActionMatcher matcher) {
        GivePokemonActionMatcher givePokemonActionMatcher = (GivePokemonActionMatcher)matcher;

        PokemonMatcher pokemonMatcher = givePokemonActionMatcher.getPokemonMatcher();
        if (!pokemonMatcher.isStarterEgg()) {
            this.pokemonNameField.setText(pokemonMatcher.getNamesies().getName());

            if (!pokemonMatcher.isEgg()) {
                if (pokemonMatcher.hasHoldItem()) {
                    this.itemNameField.setText(pokemonMatcher.getHoldItem().getName());
                }

                this.levelField.setValue(Integer.parseInt(pokemonMatcher.getLevel() + ""));
                this.shinyCheckBox.setSelected(pokemonMatcher.isShiny());
            }
        }

        this.isEggCheckBox.setSelected(pokemonMatcher.isEgg());

        setEnabled();
    }
}
