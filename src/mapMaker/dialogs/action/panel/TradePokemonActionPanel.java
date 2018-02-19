package mapMaker.dialogs.action.panel;

import mapMaker.dialogs.action.ActionPanel;
import mapMaker.dialogs.action.ActionType;
import pattern.action.ActionMatcher;
import pattern.action.ActionMatcher.TradePokemonActionMatcher;
import pokemon.PokemonNamesies;
import util.ColorDocumentListener.ColorCondition;
import util.GUIUtils;

import javax.swing.JTextField;

public class TradePokemonActionPanel extends ActionPanel {
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
    public ActionMatcher getActionMatcher(ActionType actionType) {
        PokemonNamesies requested = PokemonNamesies.tryValueOf(requestedNameField.getText());
        PokemonNamesies tradePokemon = PokemonNamesies.tryValueOf(tradeNameField.getText());

        return new TradePokemonActionMatcher(requested, tradePokemon);
    }

    @Override
    protected void load(ActionMatcher matcher) {
        TradePokemonActionMatcher tradeMatcher = (TradePokemonActionMatcher)matcher;
        requestedNameField.setText(tradeMatcher.getRequested().getName());
        tradeNameField.setText(tradeMatcher.getTradePokemon().getName());
    }
}
