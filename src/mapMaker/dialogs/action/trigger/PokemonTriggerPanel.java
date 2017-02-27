package mapMaker.dialogs.action.trigger;

import battle.attack.AttackNamesies;
import item.ItemNamesies;
import pattern.PokemonMatcher;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;
import util.ColorDocumentListener.ColorCondition;
import util.GUIUtils;
import util.SerializationUtils;

import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;

class PokemonTriggerPanel extends TriggerContentsPanel {
    private final JTextField pokemonNameField;
    private final JFormattedTextField levelField;
    private final JCheckBox randomEggCheckBox;
    private final JCheckBox isEggCheckBox;
    private final JCheckBox shinyCheckBox;
    private final JTextField itemNameField;

    PokemonTriggerPanel() {
        this.pokemonNameField = GUIUtils.createColorConditionTextField(new ColorCondition() {
            @Override
            public boolean greenCondition() {
                return PokemonNamesies.tryValueOf(pokemonNameField.getText().trim()) != null;
            }
        });

        this.levelField = GUIUtils.createIntegerTextField(1, 1, ActivePokemon.MAX_LEVEL);
        this.randomEggCheckBox = GUIUtils.createCheckBox("Random Egg", action -> setEnabled());
        this.isEggCheckBox = GUIUtils.createCheckBox("Is Egg", action -> setEnabled());
        this.shinyCheckBox = GUIUtils.createCheckBox("Shiny");

        this.itemNameField = GUIUtils.createColorConditionTextField(new ColorCondition() {
            @Override
            public boolean greenCondition() {
                return ItemNamesies.tryValueOf(itemNameField.getText().trim()) != null;
            }
        });

        JPanel checkBoxComponent = GUIUtils.createHorizontalLayoutComponent(
                this.randomEggCheckBox,
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
        boolean isEggy = randomEggCheckBox.isSelected() || isEggCheckBox.isSelected();
        pokemonNameField.setEnabled(!randomEggCheckBox.isSelected());
        levelField.setEnabled(!isEggy);
        shinyCheckBox.setEnabled(!isEggy);
        itemNameField.setEnabled(!isEggy);
    }

    @Override
    protected void load(String triggerContents) {
        PokemonMatcher matcher = SerializationUtils.deserializeJson(triggerContents, PokemonMatcher.class);

        if (!matcher.isRandomEgg()) {
            this.pokemonNameField.setText(matcher.getNamesies().getName());

            if (!matcher.isEgg()) {
                if (matcher.hasHoldItem()) {
                    this.itemNameField.setText(matcher.getHoldItem().getName());
                }

                this.levelField.setValue(Integer.parseInt(matcher.getLevel() + ""));
                this.shinyCheckBox.setSelected(matcher.isShiny());
            }
        }

        this.randomEggCheckBox.setSelected(matcher.isRandomEgg());
        this.isEggCheckBox.setSelected(matcher.isEgg());

        setEnabled();
    }

    @Override
    protected String getTriggerContents() {
        PokemonMatcher matcher;
        if (randomEggCheckBox.isSelected()) {
            matcher = PokemonMatcher.createRandomEggMatcher();
        }
        else if (isEggCheckBox.isSelected()) {
            matcher = PokemonMatcher.createEggMatcher(PokemonNamesies.getValueOf(pokemonNameField.getText()));
        }
        else {
            matcher = new PokemonMatcher(
                    PokemonNamesies.getValueOf(pokemonNameField.getText()),
                    null,
                    Integer.parseInt(levelField.getText().trim()),
                    shinyCheckBox.isSelected(),
                    new AttackNamesies[0],
                    itemNameField.getText().trim().isEmpty() ? null : ItemNamesies.getValueOf(itemNameField.getText())
            );
        }

        return SerializationUtils.getJson(matcher);
    }
}
