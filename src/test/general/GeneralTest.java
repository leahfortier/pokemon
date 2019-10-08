package test.general;

import battle.attack.AttackNamesies;
import generator.format.SplitScanner;
import item.ItemNamesies;
import item.berry.farm.PlantedBerry;
import org.junit.Assert;
import org.junit.Test;
import pokemon.ability.AbilityNamesies;
import pokemon.active.Gender;
import pokemon.species.PokemonInfo;
import pokemon.species.PokemonNamesies;
import test.pokemon.TestPokemon;
import util.TimeUtils;
import util.file.FileIO;
import util.string.StringAppender;
import util.string.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class GeneralTest extends BaseTest {

    @Test
    public void properCaseTest() {
        Assert.assertEquals("Red", StringUtils.properCase("red"));
        Assert.assertEquals("Water Stone", StringUtils.properCase("water stone"));
        Assert.assertEquals("X-Scissor", StringUtils.properCase("x-scissor"));
        Assert.assertEquals("DFS Town", StringUtils.properCase("DFS town"));
        Assert.assertEquals("King's Rock", StringUtils.properCase("king's rock"));
        Assert.assertEquals("DFS Town X-Scissor", StringUtils.properCase("DFS town x-scissor"));
        Assert.assertEquals("LeechSeed", StringUtils.properCase("LeechSeed"));
        Assert.assertEquals("Leech_Seed", StringUtils.properCase("leech_seed"));
        Assert.assertEquals("LEECH_SEED", StringUtils.properCase("LEECH_SEED"));
        Assert.assertEquals("Pok\u00e9 Ball", StringUtils.properCase("pok\u00e9 ball"));
    }

    @Test
    public void namesiesStringTest() {
        Assert.assertEquals("RED", StringUtils.getNamesiesString("red"));
        Assert.assertEquals("WATER_STONE", StringUtils.getNamesiesString("water stone"));
        Assert.assertEquals("X_SCISSOR", StringUtils.getNamesiesString("x-scissor"));
        Assert.assertEquals("DFS_TOWN", StringUtils.getNamesiesString("DFS town"));
        Assert.assertEquals("KINGS_ROCK", StringUtils.getNamesiesString("king's rock"));
        Assert.assertEquals("LEECH_SEED", StringUtils.getNamesiesString("LeechSeed"));
        Assert.assertEquals("POKE_BALL", StringUtils.getNamesiesString("Pok\u00e9 Ball"));

        // Make sure the namesies string of each name returns the enum name
        // FARFETCHD -> getName() -> Farfetch'd -> namesiesString() -> FARFETCHD
        namesiesStringTest(PokemonNamesies.values(), PokemonNamesies::getName);
        namesiesStringTest(AttackNamesies.values(), AttackNamesies::getName);
        namesiesStringTest(ItemNamesies.values(), ItemNamesies::getName);
        namesiesStringTest(AbilityNamesies.values(), AbilityNamesies::getName);
    }

    private <T extends Enum> void namesiesStringTest(T[] values, Function<T, String> nameMapper) {
        for (T namesies : values) {
            if (namesies == PokemonNamesies.NONE) {
                continue;
            }

            Assert.assertEquals(namesies.name(), StringUtils.getNamesiesString(nameMapper.apply(namesies)));
        }
    }

    @Test
    public void genderStringTest()  {
        genderStringTest(PokemonNamesies.MAGNEMITE, Gender.GENDERLESS_VALUE, "Genderless");
        genderStringTest(PokemonNamesies.HITMONLEE, 0, "100% Male");
        genderStringTest(PokemonNamesies.BULBASAUR, 1, "87.5% Male, 12.5% Female");
        genderStringTest(PokemonNamesies.ALAKAZAM, 2, "75% Male, 25% Female");
        genderStringTest(PokemonNamesies.MILOTIC, 4, "50% Male, 50% Female");
        genderStringTest(PokemonNamesies.GOTHITA, 6, "25% Male, 75% Female");
        genderStringTest(PokemonNamesies.LITLEO, 7, "12.5% Male, 87.5% Female");
        genderStringTest(PokemonNamesies.JYNX, 8, "100% Female");
    }

    private void genderStringTest(PokemonNamesies pokemonNamesies, int femaleRatio, String expected) {
        PokemonInfo pokemonInfo = pokemonNamesies.getInfo();
        Assert.assertEquals(femaleRatio, pokemonInfo.getFemaleRatio());
        Assert.assertEquals(expected, Gender.getGenderString(pokemonInfo));
    }

    @Test
    public void genderAppliesTest()  {
        // Magnemite is Genderless
        genderAppliesTest(PokemonNamesies.MAGNEMITE, Gender.MALE, false);
        genderAppliesTest(PokemonNamesies.MAGNEMITE, Gender.FEMALE, false);
        genderAppliesTest(PokemonNamesies.MAGNEMITE, Gender.GENDERLESS, true);

        // Jynx is always Female
        genderAppliesTest(PokemonNamesies.JYNX, Gender.MALE, false);
        genderAppliesTest(PokemonNamesies.JYNX, Gender.FEMALE, true);
        genderAppliesTest(PokemonNamesies.JYNX, Gender.GENDERLESS, false);

        // Hitmonlee is always Male
        genderAppliesTest(PokemonNamesies.HITMONLEE, Gender.MALE, true);
        genderAppliesTest(PokemonNamesies.HITMONLEE, Gender.FEMALE, false);
        genderAppliesTest(PokemonNamesies.HITMONLEE, Gender.GENDERLESS, false);

        // Pokemon that can be male or female (of different ratios)
        genderAppliesTest(PokemonNamesies.BULBASAUR);
        genderAppliesTest(PokemonNamesies.ALAKAZAM);
        genderAppliesTest(PokemonNamesies.MILOTIC);
        genderAppliesTest(PokemonNamesies.GOTHITA);
        genderAppliesTest(PokemonNamesies.LITLEO);
    }

    // Used for Pokemon that can be either male or female
    private void genderAppliesTest(PokemonNamesies pokemonNamesies) {
        genderAppliesTest(pokemonNamesies, Gender.MALE, true);
        genderAppliesTest(pokemonNamesies, Gender.FEMALE, true);
        genderAppliesTest(pokemonNamesies, Gender.GENDERLESS, false);
    }

    private void genderAppliesTest(PokemonNamesies pokemonNamesies, Gender gender, boolean applies) {
        Assert.assertEquals(applies, gender.genderApplies(pokemonNamesies.getInfo()));
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
        Assert.assertEquals("", stringAppender.toString());
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
            String fileName = file.getPath();
            FileIO.overwriteFile(fileName, FileIO.readEntireFile(fileName));
            Assert.assertFalse(FileIO.readEntireFile(fileName).contains("\t"));
        }
    }

    @Test
    public void methodNameTest() {
        Assert.assertEquals("methodName", StringUtils.getMethodName("methodName()"));
        Assert.assertEquals("methodName", StringUtils.getMethodName("methodName(a)"));
        Assert.assertEquals("methodName", StringUtils.getMethodName("methodName(a, b)"));
        Assert.assertEquals("", StringUtils.getMethodName("methodName"));
        Assert.assertEquals("", StringUtils.getMethodName("methodName("));
        Assert.assertEquals("", StringUtils.getMethodName("methodName() {"));
    }

    @Test
    public void stringSeparatedTest() {
        Assert.assertEquals("1 2 3", StringUtils.spaceSeparated(1, 2, 3));
        Assert.assertEquals("1 2 3", StringUtils.spaceSeparated(1, "2", 3));
        Assert.assertEquals("1 a 2.3", StringUtils.spaceSeparated(1, "a", 2.3));
        Assert.assertEquals("1 [2, 3] c", StringUtils.spaceSeparated(1, new int[] { 2, 3 }, "c"));
        Assert.assertEquals("1 BULBASAUR", StringUtils.spaceSeparated(1, PokemonNamesies.BULBASAUR));
        Assert.assertEquals("1 [OVERGROW]", StringUtils.spaceSeparated(1, new AbilityNamesies[] { AbilityNamesies.OVERGROW }));
        Assert.assertEquals("1 [OVERGROW]", StringUtils.spaceSeparated(1, List.of(AbilityNamesies.OVERGROW)));
    }
}