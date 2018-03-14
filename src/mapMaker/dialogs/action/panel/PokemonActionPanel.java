package mapMaker.dialogs.action.panel;

import item.ItemNamesies;
import mapMaker.dialogs.action.ActionPanel;
import pattern.PokemonMatcher;
import pattern.action.ActionMatcher.GivePokemonActionMatcher;
import pokemon.active.PartyPokemon;
import pokemon.species.PokemonNamesies;
import util.ColorDocumentListener.ColorCondition;
import util.GuiUtils;

import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class PokemonActionPanel extends ActionPanel<GivePokemonActionMatcher> {
    private final JTextField pokemonNameField;
    private final JFormattedTextField levelField;
    private final JCheckBox isEggCheckBox;
    private final JCheckBox shinyCheckBox;
    private final JTextField itemNameField;

    public PokemonActionPanel() {
        this.pokemonNameField = GuiUtils.createColorConditionTextField(new ColorCondition() {
            @Override
            public boolean greenCondition() {
                return PokemonNamesies.tryValueOf(pokemonNameField.getText().trim()) != null;
            }
        });

        this.levelField = GuiUtils.createIntegerTextField(1, 1, PartyPokemon.MAX_LEVEL);
        this.isEggCheckBox = GuiUtils.createCheckBox("Is Egg", action -> setEnabled());
        this.shinyCheckBox = GuiUtils.createCheckBox("Shiny");

        this.itemNameField = GuiUtils.createColorConditionTextField(new ColorCondition() {
            @Override
            public boolean greenCondition() {
                return ItemNamesies.tryValueOf(itemNameField.getText().trim()) != null;
            }
        });

        JPanel checkBoxComponent = GuiUtils.createHorizontalLayoutComponent(
                this.isEggCheckBox,
                this.shinyCheckBox
        );

        GuiUtils.setVerticalLayout(
                this,
                GuiUtils.createTextFieldComponent("Pokemon", this.pokemonNameField),
                GuiUtils.createTextFieldComponent("Level", this.levelField),
                checkBoxComponent,
                GuiUtils.createTextFieldComponent("Hold Item", this.itemNameField)
        );
    }

    private void setEnabled() {
        boolean isEggy = isEggCheckBox.isSelected();
        levelField.setEnabled(!isEggy);
        shinyCheckBox.setEnabled(!isEggy);
        itemNameField.setEnabled(!isEggy);
    }

    @Override
    public GivePokemonActionMatcher getActionMatcher() {
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
    protected void load(GivePokemonActionMatcher matcher) {
        PokemonMatcher pokemonMatcher = matcher.getPokemonMatcher();

        this.pokemonNameField.setText(pokemonMatcher.getNamesies().getName());
        this.isEggCheckBox.setSelected(pokemonMatcher.isEgg());

        if (!pokemonMatcher.isEgg()) {
            if (pokemonMatcher.hasHoldItem()) {
                this.itemNameField.setText(pokemonMatcher.getHoldItem().getName());
            }

            this.levelField.setValue(Integer.parseInt(pokemonMatcher.getLevel() + ""));
            this.shinyCheckBox.setSelected(pokemonMatcher.isShiny());
        }

        setEnabled();
    }
}
