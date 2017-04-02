package map.triggers;

import gui.view.map.MapView;
import gui.view.map.VisualState;
import main.Game;
import message.Messages;
import pattern.TradePokemonMatcher;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;
import trainer.player.Player;

public class TradePokemonActionTrigger {
    private TradePokemonMatcher tradePokemonMatcher;

    protected void executeTrigger() {
        if (tradePokemonMatcher.isCancelled()) {
            return;
        }

        PokemonNamesies requested = tradePokemonMatcher.getRequested();
        int teamIndex = tradePokemonMatcher.getTeamIndex();

        Player player = Game.getPlayer();
        ActivePokemon playerTradePokemon = player.getTeam().get(teamIndex);

        if (requested != playerTradePokemon.getPokemonInfo().namesies()) {
            Messages.add("Hmm... Not exactly what I was looking for, but thanks anyway?");
            return;
        }

        ActivePokemon npcTradePokemon = new ActivePokemon(
                tradePokemonMatcher.getTradePokemon(),
                playerTradePokemon.getLevel(),
                false,
                true
        );

        player.getTeam().set(teamIndex, npcTradePokemon);
//        Messages.add("Please take good care of my " + npcTradePokemon.getName() + "...");

        MapView mapView = Game.instance().getMapView();
        mapView.setTrade(playerTradePokemon, npcTradePokemon);
        mapView.setState(VisualState.TRADE);
    }
}
