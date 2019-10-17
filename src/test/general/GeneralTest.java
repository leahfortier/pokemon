package test.general;

import battle.attack.AttackNamesies;
import generator.format.SplitScanner;
import item.ItemNamesies;
import item.berry.farm.PlantedBerry;
import map.Direction;
import map.PathDirection;
import org.junit.Assert;
import org.junit.Test;
import pokemon.ability.AbilityNamesies;
import pokemon.species.PokemonNamesies;
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

    // Test to confirm that StringAppender is the only class that should be using a StringBuilder
    // Otherwise they should be using StringAppender instead!!
    @Test
    public void noStringBuilderTest() {
        // GeneralTest is here too since this is just checking text not like actual Java usage stuff
        // This is mapping class object to simple file end name (Ex: 'StringAppender.java')
        String[] exceptions = List.of(GeneralTest.class, StringAppender.class)
                                  .stream()
                                  .map(classy -> classy.getSimpleName() + ".java")
                                  .toArray(String[]::new);

        // Go through all the source files and make sure StringBuilder appears if and only if in the above exceptions
        for (File file : FileIO.listFiles("src")) {
            String fileName = file.getPath();
            String contents = FileIO.readEntireFile(fileName);
            Assert.assertEquals(fileName, StringUtils.endsWithAny(fileName, exceptions), contents.contains("StringBuilder"));
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

    @Test
    public void directionTest() {
        assertOpposites(Direction.UP, Direction.DOWN);
        assertOpposites(Direction.LEFT, Direction.RIGHT);

        // PathDirection has every direction plus WAIT
        Assert.assertEquals(4, Direction.values().length);
        Assert.assertEquals(Direction.values().length + 1, PathDirection.values().length);

        for (Direction direction : Direction.values()) {
            assertPathDirection(direction);
        }

        Assert.assertNull(PathDirection.WAIT.getDirection());
    }

    private void assertPathDirection(Direction direction) {
        PathDirection pathDirection = PathDirection.valueOf(direction.name());
        Assert.assertEquals(pathDirection, direction.getPathDirection());
        Assert.assertEquals(direction, pathDirection.getDirection());
    }

    // Makes sure direction and opposite and distinct
    // Makes sure direction and opposite are both the opposite of each other
    private void assertOpposites(Direction direction, Direction opposite) {
        Assert.assertNotEquals(direction, opposite);
        Assert.assertEquals(opposite, direction.getOpposite());
        Assert.assertEquals(direction, opposite.getOpposite());
    }
}
