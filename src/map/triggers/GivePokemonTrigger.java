package map.triggers;

import main.Game;
import pattern.PokemonMatcher;
import pokemon.ActivePokemon;
import util.SerializationUtils;

class GivePokemonTrigger extends Trigger {
    private final PokemonMatcher pokemonMatcher;

    GivePokemonTrigger(String pokemonDescription, String condition) {
        super(TriggerType.GIVE_POKEMON, pokemonDescription, condition);

        this.pokemonMatcher = SerializationUtils.deserializeJson(pokemonDescription, PokemonMatcher.class);
    }

    protected void executeTrigger() {
        Game.getPlayer().addPokemon(ActivePokemon.createActivePokemon(this.pokemonMatcher, true));
    }
}
