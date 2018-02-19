package map.triggers;

import gui.view.TradeView;
import gui.view.ViewMode;
import main.Game;
import map.condition.Condition;
import pattern.TradePokemonMatcher;
import util.SerializationUtils;

public class TradePokemonTrigger extends Trigger {
    private final TradePokemonMatcher tradePokemonMatcher;

    TradePokemonTrigger(String contents, Condition condition) {
        this(SerializationUtils.deserializeJson(contents, TradePokemonMatcher.class), condition);
    }

    public TradePokemonTrigger(TradePokemonMatcher matcher, Condition condition) {
        super(matcher.getJson(), condition);
        this.tradePokemonMatcher = matcher;
    }

    @Override
    protected void executeTrigger() {
        TradeView tradeView = Game.instance().getTradeView();
        tradeView.setTrade(this.tradePokemonMatcher);

        ChangeViewTrigger.addChangeViewTriggerMessage(ViewMode.TRADE_VIEW);
    }
}
