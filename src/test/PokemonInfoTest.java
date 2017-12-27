package test;

import battle.attack.AttackNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.PokemonInfo;
import pokemon.PokemonNamesies;

public class PokemonInfoTest {
    @Test
    public void totalPokemonTest() {
        // Add one to account for the empty pokemon at the beginning
        Assert.assertTrue(PokemonNamesies.values().length == PokemonInfo.NUM_POKEMON + 1);
        Assert.assertTrue(PokemonNamesies.values()[0] == PokemonNamesies.NONE);
    }

    @Test
    public void numberTest() {
        for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++) {
            PokemonInfo pokemonInfo = PokemonInfo.getPokemonInfo(i);
            PokemonNamesies pokemonNamesies = PokemonNamesies.values()[i];

            Assert.assertEquals(pokemonInfo.namesies(), pokemonNamesies);
            Assert.assertEquals(pokemonInfo.getName(), pokemonNamesies.getName());
            Assert.assertEquals(pokemonInfo.getNumber(), pokemonNamesies.ordinal());
            Assert.assertEquals(pokemonInfo.getNumber(), i);
        }
    }

    // TODO: Test case for this but include -1 for evolution level
    // TODO: Test that these are always in numerical order
//			if (level < 0 || level > ActivePokemon.MAX_LEVEL) {
//				Global.error("Invalid level " + level + " (Move: " + attackName + ")");
//			}
    @Test
    public void stockpileTest() {
        for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++) {
            PokemonInfo pokemonInfo = PokemonInfo.getPokemonInfo(i);

            Integer stockpileLevel = pokemonInfo.levelLearned(AttackNamesies.STOCKPILE);
            Integer spitUpLevel = pokemonInfo.levelLearned(AttackNamesies.SPIT_UP);
            Integer swallowLevel = pokemonInfo.levelLearned(AttackNamesies.SWALLOW);

            if (stockpileLevel != null) {
                Assert.assertFalse(
                        "Stockpile learned without/after Spit Up or Swallow for " + pokemonInfo.getName() + ". " +
                                "Levels: " + stockpileLevel + ", " + spitUpLevel + ", " + swallowLevel,
                        (spitUpLevel == null && swallowLevel == null) ||
                                (spitUpLevel != null && spitUpLevel < stockpileLevel) ||
                                (swallowLevel != null && swallowLevel < stockpileLevel)
                );
            } else {
                Assert.assertFalse(
                        "Spit Up/Swallow learned without Stockpile for " + pokemonInfo.getName() + ". " +
                                "Levels: " + spitUpLevel + ", " + swallowLevel,
                        spitUpLevel != null || swallowLevel != null
                );
            }
        }
    }
}
