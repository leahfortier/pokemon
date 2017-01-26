package test;

import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;

class TestPokemon extends ActivePokemon {
    TestPokemon(final PokemonNamesies pokemon) {
        super(pokemon, 100, false, false);
    }
}
