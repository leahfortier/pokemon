package map.triggers;

import gui.view.TradeView;
import gui.view.ViewMode;
import main.Game;
import pattern.TradePokemonMatcher;
import util.SerializationUtils;

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
