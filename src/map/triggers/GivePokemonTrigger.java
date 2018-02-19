package map.triggers;

import main.Game;
import map.condition.Condition;
import pattern.PokemonMatcher;
import pokemon.PartyPokemon;
import util.SerializationUtils;

class GivePokemonTrigger extends Trigger {
    private final PokemonMatcher pokemonMatcher;

    GivePokemonTrigger(String pokemonDescription, Condition condition) {
        this(SerializationUtils.deserializeJson(pokemonDescription, PokemonMatcher.class), condition);
    }

    public GivePokemonTrigger(PokemonMatcher matcher, Condition condition) {
        super(matcher.getJson(), condition);
        this.pokemonMatcher = matcher;
    }

    @Override
    protected void executeTrigger() {
        Game.getPlayer().addPokemon(PartyPokemon.createActivePokemon(this.pokemonMatcher, true));
    }
}
