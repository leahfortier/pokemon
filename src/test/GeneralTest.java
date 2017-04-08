package test;

import org.junit.Assert;
import org.junit.Test;
import pokemon.Gender;
import pokemon.PokemonNamesies;
import util.StringUtils;
import util.TimeUtils;

public class GeneralTest {
    private static final double DELTA = 1e-15;

    public static void assertEquals(String message, double expected, double actual) {
        Assert.assertEquals(message, expected, actual, DELTA);
    }

    public static void assertEquals(double expected, double actual) {
        Assert.assertEquals(expected, actual, DELTA);
    }

    public static boolean healthRatioMatch(TestPokemon pokemon, double fraction) {
        return (int)(Math.ceil(fraction*pokemon.getMaxHP())) == pokemon.getHP();
    }

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

    @Test
    public void timeTest() {
        assertEquals(1000*60*60*24, TimeUtils.MILLSECONDS_IN_DAY);
        assertEquals(1, TimeUtils.numDaysPassed(TimeUtils.getCurrentTimestamp() - TimeUtils.MILLSECONDS_IN_DAY));
        assertEquals(1.5, TimeUtils.numDaysPassed(TimeUtils.getCurrentTimestamp() - (long)(1.5*TimeUtils.MILLSECONDS_IN_DAY)));
        assertEquals(15, TimeUtils.numDaysPassed(TimeUtils.getCurrentTimestamp() - 15*TimeUtils.MILLSECONDS_IN_DAY));
    }
}
