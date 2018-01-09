package test;

import generator.format.SplitScanner;
import item.ItemNamesies;
import item.berry.farm.PlantedBerry;
import org.junit.Assert;
import org.junit.Test;
import pokemon.Gender;
import pokemon.PokemonNamesies;
import util.FileIO;
import util.StringAppender;
import util.StringUtils;
import util.TimeUtils;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class GeneralTest extends BaseTest {

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
        TestPokemon pokemon = TestPokemon.newPlayerPokemon(name);
        Assert.assertEquals(gender, pokemon.getGender());
        return pokemon;
    }

    @Test
    public void timeTest() {
        long millsecondsInHour = TimeUnit.HOURS.toMillis(1);
        TestUtils.assertEquals(1000*60*60, millsecondsInHour);
        TestUtils.assertEquals(1, TimeUtils.hoursSince(TimeUtils.getCurrentTimestamp() - millsecondsInHour));
        TestUtils.assertEquals(1, TimeUtils.hoursSince(TimeUtils.getCurrentTimestamp() - (long)(1.5*millsecondsInHour)));
        TestUtils.assertEquals(15, TimeUtils.hoursSince(TimeUtils.getCurrentTimestamp() - 15*millsecondsInHour));

        long millsecondsInMinute = TimeUnit.MINUTES.toMillis(1);
        TestUtils.assertEquals(1000*60, millsecondsInMinute);
        TestUtils.assertEquals(1, TimeUtils.minutesSince(TimeUtils.getCurrentTimestamp() - millsecondsInMinute));
        TestUtils.assertEquals(1, TimeUtils.minutesSince(TimeUtils.getCurrentTimestamp() - (long)(1.5*millsecondsInMinute)));
        TestUtils.assertEquals(15, TimeUtils.minutesSince(TimeUtils.getCurrentTimestamp() - 15*millsecondsInMinute));

        long millsecondsInSecond = TimeUnit.SECONDS.toMillis(1);
        TestUtils.assertEquals(1000, millsecondsInSecond);
        TestUtils.assertEquals(1, TimeUtils.secondsSince(TimeUtils.getCurrentTimestamp() - millsecondsInSecond));
        TestUtils.assertEquals(1, TimeUtils.secondsSince(TimeUtils.getCurrentTimestamp() - (long)(1.5*millsecondsInSecond)));
        TestUtils.assertEquals(15, TimeUtils.secondsSince(TimeUtils.getCurrentTimestamp() - 15*millsecondsInSecond));

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

    @Test
    public void splitScannerTest() {
        String testString = "This is my test string";
        SplitScanner split = new SplitScanner(testString);
        Assert.assertTrue(split.hasNext());
        Assert.assertEquals("This", split.next());
        Assert.assertEquals("is my test string", split.getRemaining());

        Assert.assertTrue(split.hasNext());
        Assert.assertEquals("is", split.next());
        Assert.assertEquals("my test string", split.getRemaining());

        Assert.assertTrue(split.hasNext());
        Assert.assertEquals("my", split.next());
        Assert.assertEquals("test string", split.getRemaining());

        Assert.assertTrue(split.hasNext());
        Assert.assertEquals("test", split.next());
        Assert.assertEquals("string", split.getRemaining());

        Assert.assertTrue(split.hasNext());
        Assert.assertEquals("string", split.next());
        Assert.assertEquals("", split.getRemaining());

        Assert.assertFalse(split.hasNext());
    }

    @Test
    public void stringAppenderTest() {
        StringAppender stringAppender = new StringAppender();
        Assert.assertTrue(stringAppender.isEmpty());
        Assert.assertEquals(StringUtils.empty(), stringAppender.toString());
        Assert.assertEquals(0, stringAppender.length());
        checkStringAppender("", stringAppender);
        checkStringAppender("a", stringAppender.append("a"));
        checkStringAppender("ab", stringAppender.append("b"));
        checkStringAppender("", stringAppender.clear());
        checkStringAppender("a", stringAppender.appendDelimiter(", ", "a"));
        checkStringAppender("a, b", stringAppender.appendDelimiter(", ", "b"));
        checkStringAppender("a, b1 2 3", stringAppender.appendJoin(" ", 3, i -> (i + 1) + ""));
        checkStringAppender("a, b1 2 37.:.v.:.true.:.4.3", stringAppender.appendJoin(".:.", Arrays.asList(7, "v", true, 4.3)));
        checkStringAppender("", stringAppender.clear());
        checkStringAppender("3-5; 7-9; 2-4", stringAppender.appendJoin("; ", Arrays.asList(4, 8, 3), index -> (index - 1) + "-" + (index + 1)));
        Assert.assertFalse(stringAppender.isEmpty());
        stringAppender.clear();
        Assert.assertTrue(stringAppender.isEmpty());
        checkStringAppender("003 + 2.20", stringAppender.appendFormat("%03d + %.2f", 3, 2.2));
        checkStringAppender("003", stringAppender.setLength(3));
        checkStringAppender("-003", stringAppender.appendPrefix("-"));
        checkStringAppender("-003", stringAppender.appendIf(false, "x"));
        checkStringAppender("-003x", stringAppender.appendIf(true, "x"));
        Assert.assertEquals(5, stringAppender.length());
        checkStringAppender("-003x55555", stringAppender.appendRepeat("5", 5));
        checkStringAppender("-1003x55555", stringAppender.insert(1, "1"));
        Assert.assertEquals('1', stringAppender.charAt(1));
        checkStringAppender("", stringAppender.clear());
        checkStringAppender("a", stringAppender.append('a'));
        checkStringAppender("a", stringAppender.append(null));
        checkStringAppender("a", stringAppender.appendLine(null));
        checkStringAppender("a", stringAppender.appendLineIf(true, null));
        checkStringAppender("a\n", stringAppender.appendLine());
        checkStringAppender("a\nb\n", stringAppender.appendLine("b"));
        checkStringAppender("a\nb\n", stringAppender.appendDelimiter(";", ""));
        checkStringAppender("a\nb\n; ", stringAppender.appendDelimiter(";", " "));
        checkStringAppender("", stringAppender.clear());
        checkStringAppender(" ", stringAppender.append(" "));
        checkStringAppender(" ", stringAppender.appendPostDelimiter(" ", ""));
        checkStringAppender(" a ", stringAppender.appendPostDelimiter(" ", "a"));

        checkStringAppender("abc;123", new StringAppender("abc;123"));
        Assert.assertEquals("111", StringUtils.repeat("1", 3));
        Assert.assertEquals("1 a 1.3", StringUtils.spaceSeparated(1, "a", 1.3));
    }

    private void checkStringAppender(String equals, StringAppender appender) {
        Assert.assertEquals(equals, appender.toString());
        Assert.assertEquals(equals.length(), appender.length());
        Assert.assertEquals(StringUtils.isNullOrEmpty(equals), appender.isEmpty());
        for (int i = 0; i < equals.length(); i++) {
            Assert.assertEquals(equals.charAt(i), appender.charAt(i));
        }
    }

    @Test
    public void tabsTest() {
        for (File file : FileIO.listFiles("src")) {
            // Overwrite file should handle tab replacement
            FileIO.overwriteFile(file, FileIO.readEntireFile(file));
            Assert.assertFalse(FileIO.readEntireFile(file).contains("\t"));
        }
    }
}
