package mapMaker.dialogs.action.panel;

import mapMaker.dialogs.action.ActionPanel;
import pattern.action.ActionMatcher.TradePokemonActionMatcher;
import pokemon.PokemonNamesies;
import util.ColorDocumentListener.ColorCondition;
import util.GUIUtils;

import javax.swing.JTextField;

public class TradePokemonActionPanel extends ActionPanel<TradePokemonActionMatcher> {
    private final JTextField requestedNameField;
    private final JTextField tradeNameField;

    public TradePokemonActionPanel() {
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
    public TradePokemonActionMatcher getActionMatcher() {
        PokemonNamesies requested = PokemonNamesies.tryValueOf(requestedNameField.getText());
        PokemonNamesies tradePokemon = PokemonNamesies.tryValueOf(tradeNameField.getText());

        return new TradePokemonActionMatcher(requested, tradePokemon);
    }

    @Override
    protected void load(TradePokemonActionMatcher matcher) {
        requestedNameField.setText(matcher.getRequested().getName());
        tradeNameField.setText(matcher.getTradePokemon().getName());
    }
}
