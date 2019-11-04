package map.triggers;

import gui.view.TradeView;
import gui.view.ViewMode;
import main.Game;
import message.Messages;
import pokemon.species.PokemonNamesies;

public class TradePokemonTrigger extends Trigger {
    private final PokemonNamesies tradePokemon;
    private final PokemonNamesies requested;

    public TradePokemonTrigger(PokemonNamesies tradePokemon, PokemonNamesies requestedPokemon) {
        this.tradePokemon = tradePokemon;
        this.requested = requestedPokemon;
    }

    @Override
    public void execute() {
        // Note: So like this is set up poorly and things will break if there is no dialogue before the view change...
        Messages.add("Trade????");

        TradeView tradeView = Game.instance().getTradeView();
        tradeView.setTrade(this.tradePokemon, this.requested);

        ChangeViewTrigger.addChangeViewTriggerMessage(ViewMode.TRADE_VIEW);
    }
}
