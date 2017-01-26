package test;

import pokemon.ActivePokemon;
import pokemon.Gender;
import pokemon.PokemonNamesies;
import pokemon.ability.AbilityNamesies;

class TestPokemon extends ActivePokemon {
    TestPokemon(final PokemonNamesies pokemon) {
        super(pokemon, 100, false, false);
    }

    TestPokemon withGender(Gender gender) {
        super.setGender(gender);
        return this;
    }

    TestPokemon withAbility(AbilityNamesies ability) {
        super.setAbility(ability);
        return this;
    }
}
