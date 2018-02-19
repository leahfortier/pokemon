package map.triggers;

import gui.view.TradeView;
import gui.view.ViewMode;
import main.Game;
import map.condition.Condition;
import pattern.TradePokemonMatcher;
import pokemon.PokemonNamesies;
import util.SerializationUtils;

public class TradePokemonTrigger extends Trigger {
    private final PokemonNamesies tradePokemon;
    private final PokemonNamesies requested;

    TradePokemonTrigger(String contents, Condition condition) {
        this(SerializationUtils.deserializeJson(contents, TradePokemonMatcher.class), condition);
    }

    public TradePokemonTrigger(TradePokemonMatcher matcher, Condition condition) {
        this(matcher.getTradePokemon(), matcher.getRequested(), condition);
    }

    public TradePokemonTrigger(PokemonNamesies tradePokemon, PokemonNamesies requestedPokemon, Condition condition) {
        super(tradePokemon.name() + "/" + requestedPokemon.name(), condition);
        this.tradePokemon = tradePokemon;
        this.requested = requestedPokemon;
    }

    @Override
    protected void executeTrigger() {
        TradeView tradeView = Game.instance().getTradeView();
        tradeView.setTrade(this.tradePokemon, this.requested);

        ChangeViewTrigger.addChangeViewTriggerMessage(ViewMode.TRADE_VIEW);
    }
}
