package map.triggers;

import main.Game;
import pattern.PokemonMatcher;
import pokemon.active.PartyPokemon;

public class GivePokemonTrigger extends Trigger {
    private final PokemonMatcher pokemonMatcher;

    public GivePokemonTrigger(PokemonMatcher matcher) {
        this.pokemonMatcher = matcher;
    }

    @Override
    public void execute() {
        Game.getPlayer().addPokemon(PartyPokemon.createActivePokemon(this.pokemonMatcher, true));
    }
}
