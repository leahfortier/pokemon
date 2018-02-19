package map.triggers;

import main.Game;
import map.condition.Condition;
import pattern.PokemonMatcher;
import pokemon.PartyPokemon;

public class GivePokemonTrigger extends Trigger {
    private final PokemonMatcher pokemonMatcher;

    public GivePokemonTrigger(PokemonMatcher matcher, Condition condition) {
        super(matcher.getJson(), condition);
        this.pokemonMatcher = matcher;
    }

    @Override
    protected void executeTrigger() {
        Game.getPlayer().addPokemon(PartyPokemon.createActivePokemon(this.pokemonMatcher, true));
    }
}
