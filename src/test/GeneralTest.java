package test;

import item.ItemNamesies;
import item.berry.farm.PlantedBerry;
import org.junit.Assert;
import org.junit.Test;
import pokemon.Gender;
import pokemon.PokemonNamesies;
import util.StringUtils;
import util.TimeUtils;

import java.util.concurrent.TimeUnit;

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

        TestPokemon magnemite = genderTestPokemon(PokemonNamesies.MAGNEMITE, Gender.GENDERLESS);
        TestPokemon voltorb = genderTestPokemon(PokemonNamesies.VOLTORB, Gender.GENDERLESS);
        TestPokemon jynx = genderTestPokemon(PokemonNamesies.JYNX, Gender.FEMALE);
        TestPokemon miltank = genderTestPokemon(PokemonNamesies.MILTANK, Gender.FEMALE);
        TestPokemon hitmonlee = genderTestPokemon(PokemonNamesies.HITMONLEE, Gender.MALE);
        TestPokemon hitmonchan = genderTestPokemon(PokemonNamesies.HITMONCHAN, Gender.MALE);

        // Genderless vs genderless
        Assert.assertFalse(Gender.oppositeGenders(magnemite, voltorb));

        // Male vs female
        Assert.assertTrue(Gender.oppositeGenders(hitmonchan, jynx));

        // Female vs male
        Assert.assertTrue(Gender.oppositeGenders(miltank, hitmonlee));

        // Male vs male
        Assert.assertFalse(Gender.oppositeGenders(hitmonchan, hitmonlee));

        // Female vs female
        Assert.assertFalse(Gender.oppositeGenders(jynx, miltank));

        // Male vs genderless
        Assert.assertFalse(Gender.oppositeGenders(hitmonchan, magnemite));

        // Genderless vs male
        Assert.assertFalse(Gender.oppositeGenders(voltorb, hitmonlee));

        // Female vs genderless
        Assert.assertFalse(Gender.oppositeGenders(jynx, magnemite));

        // Genderless vs female
        Assert.assertFalse(Gender.oppositeGenders(voltorb, miltank));
    }

    private TestPokemon genderTestPokemon(PokemonNamesies name, Gender gender) {
        TestPokemon pokemon = new TestPokemon(name);
        Assert.assertEquals(gender, pokemon.getGender());
        return pokemon;
    }

    @Test
    public void timeTest() {
        long millsecondsInHour = TimeUnit.HOURS.toMillis(1);
        assertEquals(1000*60*60, millsecondsInHour);
        assertEquals(1, TimeUtils.hoursSince(TimeUtils.getCurrentTimestamp() - millsecondsInHour));
        assertEquals(1, TimeUtils.hoursSince(TimeUtils.getCurrentTimestamp() - (long)(1.5*millsecondsInHour)));
        assertEquals(15, TimeUtils.hoursSince(TimeUtils.getCurrentTimestamp() - 15*millsecondsInHour));

        long millsecondsInMinute = TimeUnit.MINUTES.toMillis(1);
        assertEquals(1000*60, millsecondsInMinute);
        assertEquals(1, TimeUtils.minutesSince(TimeUtils.getCurrentTimestamp() - millsecondsInMinute));
        assertEquals(1, TimeUtils.minutesSince(TimeUtils.getCurrentTimestamp() - (long)(1.5*millsecondsInMinute)));
        assertEquals(15, TimeUtils.minutesSince(TimeUtils.getCurrentTimestamp() - 15*millsecondsInMinute));

        long millsecondsInSecond = TimeUnit.SECONDS.toMillis(1);
        assertEquals(1000, millsecondsInSecond);
        assertEquals(1, TimeUtils.secondsSince(TimeUtils.getCurrentTimestamp() - millsecondsInSecond));
        assertEquals(1, TimeUtils.secondsSince(TimeUtils.getCurrentTimestamp() - (long)(1.5*millsecondsInSecond)));
        assertEquals(15, TimeUtils.secondsSince(TimeUtils.getCurrentTimestamp() - 15*millsecondsInSecond));

        Assert.assertEquals("0:59", TimeUtils.formatMinutes(59));
        Assert.assertEquals("1:00", TimeUtils.formatMinutes(60));
        Assert.assertEquals("1:01", TimeUtils.formatMinutes(61));
        Assert.assertEquals("1:39", TimeUtils.formatMinutes(99));
        Assert.assertEquals("0:01", TimeUtils.formatMinutes(1));
        Assert.assertEquals("0:01", TimeUtils.formatSeconds(60));
        Assert.assertEquals("0:01", TimeUtils.formatSeconds(80));
        Assert.assertEquals("0:00", TimeUtils.formatSeconds(59));
        Assert.assertEquals("1234:56", TimeUtils.formatMinutes(TimeUnit.HOURS.toMinutes(1234) + 56));

        // TODO: This should be moved if there are more berry tests later
        Assert.assertEquals("24:00", new PlantedBerry(ItemNamesies.ORAN_BERRY).getTimeLeftString());
    }
}
