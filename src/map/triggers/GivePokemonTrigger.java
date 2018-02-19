package map.triggers;

import main.Game;
import pattern.PokemonMatcher;
import pokemon.PartyPokemon;

public class GivePokemonTrigger extends Trigger {
    private final PokemonMatcher pokemonMatcher;

    public GivePokemonTrigger(PokemonMatcher matcher) {
        super(matcher.getJson());
        this.pokemonMatcher = matcher;
    }

    @Override
    protected void executeTrigger() {
        Game.getPlayer().addPokemon(PartyPokemon.createActivePokemon(this.pokemonMatcher, true));
    }
}
