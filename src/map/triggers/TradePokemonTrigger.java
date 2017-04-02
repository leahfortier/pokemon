package map.triggers;

import gui.view.TradeView;
import gui.view.ViewMode;
import main.Game;
import mapMaker.dialogs.action.trigger.TriggerActionType;
import message.MessageUpdate;
import message.Messages;
import pattern.TradePokemonMatcher;
import pattern.action.ActionMatcher;
import pattern.action.ChoiceActionMatcher;
import pattern.action.ChoiceActionMatcher.ChoiceMatcher;
import pattern.action.TriggerActionMatcher;
import pokemon.ActivePokemon;
import trainer.player.Player;
import util.PokeString;
import util.SerializationUtils;
import util.StringUtils;

import java.util.List;

public class TradePokemonTrigger extends Trigger {
    private final TradePokemonMatcher tradePokemonMatcher;

    TradePokemonTrigger(String contents, String condition) {
        super(TriggerType.TRADE_POKEMON, contents, condition);

        tradePokemonMatcher = SerializationUtils.deserializeJson(contents, TradePokemonMatcher.class);
    }

    @Override
    protected void executeTrigger() {
        TradeView tradeView = Game.instance().getTradeView();
        tradeView.setTrade(this.tradePokemonMatcher);

        ChangeViewTrigger.addChangeViewTriggerMessage(ViewMode.TRADE_VIEW);
    }
}
