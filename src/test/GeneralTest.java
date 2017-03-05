package test;

import org.junit.Assert;
import org.junit.Test;
import pokemon.Gender;
import pokemon.PokemonNamesies;
import util.StringUtils;

public class GeneralTest {
    @Test
    public void properCaseTest() {
        Assert.assertEquals(StringUtils.properCase("red"), "Red");
        Assert.assertEquals(StringUtils.properCase("water stone"), "Water Stone");
        Assert.assertEquals(StringUtils.properCase("x-scissor"), "X-Scissor");
        Assert.assertEquals(StringUtils.properCase("DFS town"), "DFS Town");
    }

    @Test
    public void oppositeGenderTest() {
        Assert.assertTrue(Gender.MALE.getOppositeGender() == Gender.FEMALE);
        Assert.assertTrue(Gender.FEMALE.getOppositeGender() == Gender.MALE);
        Assert.assertTrue(Gender.GENDERLESS.getOppositeGender() == Gender.GENDERLESS);

        TestPokemon first = new TestPokemon(PokemonNamesies.MAGNEMITE);
        TestPokemon second = new TestPokemon(PokemonNamesies.VOLTORB);

        Assert.assertTrue(first.getGender() == Gender.GENDERLESS);
        Assert.assertTrue(second.getGender() == Gender.GENDERLESS);
        Assert.assertFalse(Gender.oppositeGenders(first, second));

        first = new TestPokemon(PokemonNamesies.HITMONCHAN);
        second = new TestPokemon(PokemonNamesies.JYNX);

        Assert.assertTrue(first.getGender() == Gender.MALE);
        Assert.assertTrue(second.getGender() == Gender.FEMALE);
        Assert.assertTrue(Gender.oppositeGenders(first, second));

        first = new TestPokemon(PokemonNamesies.HITMONCHAN);
        second = new TestPokemon(PokemonNamesies.HITMONLEE);

        Assert.assertTrue(first.getGender() == Gender.MALE);
        Assert.assertTrue(second.getGender() == Gender.MALE);
        Assert.assertFalse(Gender.oppositeGenders(first, second));
    }
}
