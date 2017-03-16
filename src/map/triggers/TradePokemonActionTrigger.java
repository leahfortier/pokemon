package map.triggers;

import main.Game;
import message.Messages;
import pattern.TradePokemonMatcher;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;
import trainer.player.Player;
import util.SerializationUtils;

public class TradePokemonActionTrigger extends Trigger {
    private final TradePokemonMatcher tradePokemonMatcher;

    TradePokemonActionTrigger(String contents, String condition) {
        super(TriggerType.TRADE_POKEMON_ACTION, contents, condition);

        tradePokemonMatcher = SerializationUtils.deserializeJson(contents, TradePokemonMatcher.class);
    }

    @Override
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
        Messages.add("Please take good care of my " + npcTradePokemon.getName() + "...");
    }
}
