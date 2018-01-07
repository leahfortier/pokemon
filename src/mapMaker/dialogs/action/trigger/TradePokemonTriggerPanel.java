package mapMaker.dialogs.action.trigger;

import pattern.TradePokemonMatcher;
import pokemon.PokemonNamesies;
import util.ColorDocumentListener.ColorCondition;
import util.GUIUtils;
import util.SerializationUtils;

import javax.swing.JTextField;

public class TradePokemonTriggerPanel extends TriggerContentsPanel {
    private final JTextField requestedNameField;
    private final JTextField tradeNameField;

    public TradePokemonTriggerPanel() {
        this.requestedNameField = GUIUtils.createColorConditionTextField(new ColorCondition() {
            @Override
            public boolean greenCondition() {
                return PokemonNamesies.tryValueOf(requestedNameField.getText().trim()) != null;
            }
        });

        this.tradeNameField = GUIUtils.createColorConditionTextField(new ColorCondition() {
            @Override
            public boolean greenCondition() {
                return PokemonNamesies.tryValueOf(tradeNameField.getText().trim()) != null;
            }
        });

        GUIUtils.setVerticalLayout(
                this,
                GUIUtils.createTextFieldComponent("Requested Pokemon", this.requestedNameField),
                GUIUtils.createTextFieldComponent("Trade Pokemon", this.tradeNameField)
        );
    }

    @Override
    protected void load(String triggerContents) {
        TradePokemonMatcher matcher = SerializationUtils.deserializeJson(triggerContents, TradePokemonMatcher.class);
        requestedNameField.setText(matcher.getRequested().getName());
        tradeNameField.setText(matcher.getTradePokemon().getName());
    }

    @Override
    protected String getTriggerContents() {
        PokemonNamesies requested = PokemonNamesies.tryValueOf(requestedNameField.getText());
        PokemonNamesies tradePokemon = PokemonNamesies.tryValueOf(tradeNameField.getText());

        return SerializationUtils.getJson(new TradePokemonMatcher(requested, tradePokemon));
    }
}
