package map.triggers;

import main.Game;
import pattern.PokemonMatcher;
import pokemon.ActivePokemon;

public class GivePokemonTrigger extends Trigger {
    private final PokemonMatcher pokemonMatcher;

    GivePokemonTrigger(String pokemonDescription) {
        super(TriggerType.GIVE_POKEMON, pokemonDescription);

        this.pokemonMatcher = PokemonMatcher.matchPokemonDescription(pokemonDescription);
    }

    protected void executeTrigger() {
        Game.getPlayer().addPokemon(null, ActivePokemon.createActivePokemon(this.pokemonMatcher, true));
    }
}
