package test;

import org.junit.Assert;
import org.junit.Test;
import pokemon.PokemonInfo;
import pokemon.PokemonNamesies;

public class PokemonInfoTest {
    @Test
    public void numberTest() {
        for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++) {
            PokemonInfo pokemonInfo = PokemonInfo.getPokemonInfo(i);
            PokemonNamesies pokemonNamesies = PokemonNamesies.values()[i];

            Assert.assertTrue(pokemonInfo.namesies() == pokemonNamesies);
            Assert.assertTrue(pokemonInfo.getNumber() == i);
        }
    }
}
