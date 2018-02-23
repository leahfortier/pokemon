package map.triggers;

import gui.view.TradeView;
import gui.view.ViewMode;
import main.Game;
import pokemon.PokemonNamesies;

public class TradePokemonTrigger extends Trigger {
    private final PokemonNamesies tradePokemon;
    private final PokemonNamesies requested;

    public TradePokemonTrigger(PokemonNamesies tradePokemon, PokemonNamesies requestedPokemon) {
        this.tradePokemon = tradePokemon;
        this.requested = requestedPokemon;
    }

    @Override
    public void execute() {
        TradeView tradeView = Game.instance().getTradeView();
        tradeView.setTrade(this.tradePokemon, this.requested);

        ChangeViewTrigger.addChangeViewTriggerMessage(ViewMode.TRADE_VIEW);
    }
}
