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

    public static void semiAssertTrue(String message, boolean fullAssert, boolean assertion) {
        if (!assertion) {
            if (fullAssert) {
                Assert.fail(message);
            } else {
                System.err.println(message);
            }
        }
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
        Assert.assertEquals(Gender.FEMALE, Gender.MALE.getOppositeGender());
        Assert.assertEquals(Gender.MALE, Gender.FEMALE.getOppositeGender());
        Assert.assertEquals(Gender.GENDERLESS, Gender.GENDERLESS.getOppositeGender());

        TestPokemon first = new TestPokemon(PokemonNamesies.MAGNEMITE);
        TestPokemon second = new TestPokemon(PokemonNamesies.VOLTORB);

        Assert.assertEquals(Gender.GENDERLESS, first.getGender());
        Assert.assertEquals(Gender.GENDERLESS, second.getGender());
        Assert.assertFalse(Gender.oppositeGenders(first, second));

        first = new TestPokemon(PokemonNamesies.HITMONCHAN);
        second = new TestPokemon(PokemonNamesies.JYNX);

        Assert.assertEquals(Gender.MALE, first.getGender());
        Assert.assertEquals(Gender.FEMALE, second.getGender());
        Assert.assertTrue(Gender.oppositeGenders(first, second));

        first = new TestPokemon(PokemonNamesies.HITMONCHAN);
        second = new TestPokemon(PokemonNamesies.HITMONLEE);

        Assert.assertEquals(Gender.MALE, first.getGender());
        Assert.assertEquals(Gender.MALE, second.getGender());
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
