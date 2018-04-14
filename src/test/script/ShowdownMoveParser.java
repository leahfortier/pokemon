package test.script;

import battle.attack.AttackNamesies;
import battle.attack.MoveCategory;
import battle.effect.status.StatusNamesies;
import org.junit.Assert;
import pokemon.Stat;
import test.TestUtils;
import type.Type;
import util.string.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShowdownMoveParser {
    public String attackKey;
    public String attackName;
    public String accuracy;
    public Integer basePower;
    public Integer fixedDamage;
    public Integer pp;
    public Integer priority;
    public Integer critRatio;
    public MoveCategory category;
    public MoveCategory defensiveCategory;
    public String flags;
    public String target;
    public String selfDestruct;
    public int[] drain;
    public int[] heal;
    public int[] recoil;
    public int[] multiHit;
    public StatusNamesies status;
    public String volatileStatus;
    public String weather;
    public String terrain;
    public String sideCondition;
    public String isZ;
    public String[] noMetronome;
    public Type type;
    public int[] boosts;
    private Map<String, Boolean> booleanMap;
    public Set<String> functionKeys;
    public SecondaryEffect secondary;
    public Self self;

    public ShowdownMoveParser(Scanner in, String attackKey) {
        this.attackKey = attackKey;
        this.functionKeys = new HashSet<>();
        this.booleanMap = new HashMap<>();

        Set<String> seenKeys = new HashSet<>();
        while (true) {
            String key = in.next().trim();
            if (key.startsWith("}")) {
                break;
            } else if (key.startsWith("//")) {
                in.nextLine();
                continue;
            }

            String message = attackKey + " " + key;
            Assert.assertTrue(message, StringUtils.isAlphaOnly(key));
            Assert.assertFalse(message, seenKeys.contains(key));
            seenKeys.add(key);

            switch (key) {
                case "num":
                case "zMovePower":
                case "onBasePowerPriority":
                    readInt(message, in);
                    break;
                case "isViable":
                case "stallingMove":
                    readBoolean(message, in);
                    break;
                case "desc":
                case "shortDesc":
                case "contestType":
                case "nonGhostTarget":
                case "pressureTarget":
                    readString(message, in);
                    break;
                case "zMoveEffect":
                case "pseudoWeather":
                    readSingleQuotedString(message, in);
                    break;
                case "zMoveBoost":
                    readCurly(message, in);
                    break;
                case "effect":
                    readEffect(message, in);
                    break;
                case "id":
                    String id = readString(message, in);
                    if (attackKey.startsWith("hiddenpower")) {
                        Assert.assertEquals(message, "hiddenpower", id);
                    } else {
                        Assert.assertEquals(message, attackKey, id);
                    }
                    break;
                case "accuracy":
                    this.accuracy = readIntString(message, in, "true", "--");
                    break;
                case "basePower":
                    this.basePower = readInt(message, in);
                    break;
                case "damage":
                    switch (attackKey) {
                        case "nightshade":
                        case "seismictoss":
                            Assert.assertEquals(message, "level", readSingleQuotedString(message, in));
                            this.functionKeys.add("onDamageCallback");
                            break;
                        default:
                            this.fixedDamage = readInt(message, in);
                            break;
                    }
                    break;
                case "pp":
                    this.pp = readInt(message, in);
                    break;
                case "priority":
                    this.priority = readInt(message, in);
                    break;
                case "critRatio":
                    this.critRatio = readInt(message, in);
                    break;
                case "category":
                    this.category = readEnumValue(message, in, MoveCategory.class);
                    break;
                case "defensiveCategory":
                    this.defensiveCategory = readEnumValue(message, in, MoveCategory.class);
                    break;
                case "flags":
                    this.flags = readCurly(message, in);
                    break;
                case "drain":
                    this.drain = readBraces(message, in);
                    break;
                case "heal":
                    this.drain = readBraces(message, in);
                    break;
                case "recoil":
                    this.recoil = readBraces(message, in);
                    break;
                case "multihit":
                    this.multiHit = readMultiHit(message, in);
                    break;
                case "noMetronome":
                    Assert.assertEquals(message, "metronome", attackKey);
                    this.noMetronome = readStringBraces(message, in);
                    break;
                case "secondary":
                    Assert.assertNull(message, secondary);
                    this.secondary = readSecondary(message, in);
                    break;
                case "secondaries":
                    Assert.assertNull(message, secondary);
                    this.secondary = readSecondaries(message, in);
                    break;
                case "self":
                case "selfBoost":
                    Assert.assertNull(message, self);
                    this.self = readSelf(message, in);
                    break;
                case "name":
                    this.attackName = readString(message, in);
                    break;
                case "target":
                    this.target = readString(message, in);
                    break;
                case "selfdestruct":
                    this.selfDestruct = readString(message, in);
                    break;
                case "type":
                    this.type = readEnumValue(message, in, Type.class);
                    break;
                case "boosts":
                    this.boosts = readStatChanges(message, in);
                    break;
                case "status":
                    this.status = readStatus(message, in);
                    break;
                case "volatileStatus":
                    this.volatileStatus = readSingleQuotedString(message, in);
                    break;
                case "weather":
                    this.weather = readSingleQuotedString(message, in);
                    break;
                case "terrain":
                    this.terrain = readSingleQuotedString(message, in);
                    break;
                case "sideCondition":
                    this.sideCondition = readSingleQuotedString(message, in);
                    break;
                case "isZ":
                    this.isZ = readString(message, in);
                    break;
                case "selfSwitch":
                    if (attackKey.equals("batonpass")) {
                        Assert.assertEquals(message, "copyvolatile", readSingleQuotedString(message, in));
                    } else {
                        Assert.assertTrue(message, readBoolean(message, in));
                    }
                    this.booleanMap.put(key, true);
                    break;
                case "ohko":
                    if (attackKey.equals("sheercold")) {
                        Assert.assertEquals(message, "Ice", readSingleQuotedString(message, in));
                    } else {
                        Assert.assertTrue(message, readBoolean(message, in));
                    }
                    this.booleanMap.put(key, true);
                    break;
                case "ignoreImmunity":
                    boolean ignoreImmunity;
                    if (attackKey.equals("thousandarrows")) {
                        Assert.assertEquals(message, "{'Ground': true}", readCurly(message, in));
                        ignoreImmunity = true;
                    } else {
                        ignoreImmunity = readBoolean(message, in);
                    }
                    this.booleanMap.put(key, ignoreImmunity);
                    break;
                case "isUnreleased":
                case "isNonstandard":
                case "isFutureMove":
                case "noFaint":
                case "useTargetOffensive":
                case "mindBlownRecoil":
                case "noPPBoosts":
                case "stealsBoosts":
                case "struggleRecoil":
                case "multiaccuracy":
                case "noSketch":
                case "ignoreAbility":
                case "ignoreDefensive":
                case "ignoreEvasion":
                case "forceSwitch":
                case "willCrit":
                case "hasCustomRecoil":
                case "breaksProtect":
                case "thawsTarget":
                case "sleepUsable":
                    this.booleanMap.put(key, readBoolean(message, in));
                    break;
                case "onTryHit":
                    if (attackKey.equals("teleport")) {
                        Assert.assertFalse(message, readBoolean(message, in));
                        break;
                    }
                    // fallthrough
                case "basePowerCallback":
                case "onHit":
                case "onTryHitSide":
                case "beforeTurnCallback":
                case "onMoveAborted":
                case "onAfterMove":
                case "onModifyMove":
                case "beforeMoveCallback":
                case "onTry":
                case "onBasePower":
                case "onTryMove":
                case "onAfterSubDamage":
                case "damageCallback":
                case "onPrepareHit":
                case "onAfterMoveSecondarySelf":
                case "onHitField":
                case "onEffectiveness":
                case "onHitSide":
                case "onMoveFail":
                case "onAfterHit":
                case "onUseMoveMessage":
                    readFunction(message, in);
                    this.functionKeys.add(key);
                    break;
                default:
                    Assert.fail(message + " " + readLine(message, in) + "\nUnknown key " + key);
                    break;
            }
        }
    }

    private String readLine(String message, Scanner in) {
        String value = in.nextLine();
        Assert.assertTrue(message, value.startsWith(":"));

        int commentIndex = value.indexOf("//");
        if (commentIndex >= 0) {
            value = value.substring(0, commentIndex);
        }

        return StringUtils.trimPrefix(value, ":").trim();
    }

    private String readValue(String message, Scanner in) {
        String value = readLine(message, in);
        Assert.assertTrue(message, value.endsWith(","));
        return StringUtils.trimSuffix(value, ",").trim();
    }

    private String readString(String message, Scanner in) {
        String value = readValue(message, in);
        String unquoted = StringUtils.trimQuotes(value);
        Assert.assertEquals(message + " " + value, value.length() - 2, unquoted.length());
        return unquoted;
    }

    private String readSingleQuotedString(String message, Scanner in) {
        String value = readValue(message, in);
        Assert.assertTrue(message, value.startsWith("'") && value.endsWith("'"));
        value = value.substring(1, value.length() - 1);
        Assert.assertFalse(message, value.startsWith("'") || value.endsWith("'"));
        return value;
    }

    private String readCurly(String message, Scanner in) {
        String value = readValue(message, in);
        Assert.assertTrue(message, value.startsWith("{") && value.endsWith("}"));
        return value;
    }

    private String[] readStringBraces(String message, Scanner in) {
        String value = readValue(message, in);
        Pattern pattern = Pattern.compile("^\\['([a-z]+)'(?:, '([a-z]+)')+]$");
        Assert.assertTrue(value, pattern.matcher(value).matches());
        value = StringUtils.trimChars(value, "[", "]");
        String[] values = value.split("[, ]+");
        for (int i = 0; i < values.length; i++) {
            values[i] = StringUtils.trimSingleQuotes(values[i]);
        }
        return values;
    }

    private int[] readBraces(String message, Scanner in) {
        String value = readValue(message, in);
        return readBraces(message, value);
    }

    private int[] readBraces(String message, String value) {
        Pattern twoArrayPattern = Pattern.compile("^\\[(\\d+), (\\d+)]$");
        Matcher matcher = twoArrayPattern.matcher(value);
        Assert.assertTrue(message + " " + value, matcher.matches());
        return new int[] { readInt(message, matcher.group(1)), readInt(message, matcher.group(2)) };
    }

    private int[] readMultiHit(String message, Scanner in) {
        String value = readValue(message, in);
        try {
            return readBraces(message, value);
        } catch (AssertionError error) {
            int numHits = readInt(message, value);
            return new int[] { numHits, numHits };
        }
    }

    private <T extends Enum<T>> T readEnumValue(String message, Scanner in, Class<T> enumClass) {
        String value = readString(message, in);
        return StringUtils.enumValueOf(enumClass, value);
    }

    private int readInt(String message, Scanner in) {
        String value = readValue(message, in);
        return readInt(message, value);
    }

    private int readInt(String message, String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            Assert.fail(message + ": " + value + " is not an integer.");
            return 0;
        }
    }

    private boolean readBoolean(String message, Scanner in) {
        String value = readValue(message, in);
        switch (value) {
            case "true":
                return true;
            case "false":
                return false;
            default:
                Assert.fail(message + ": " + value + " is not a boolean.");
                return false;
        }
    }

    private void checkBoolean(String message, Scanner in, String attackKey, String... expected) {
        Assert.assertTrue(message, readBoolean(message, in));
        TestUtils.assertEqualsAny(message, attackKey, expected);
    }

    private String readIntString(String message, Scanner in, String validNonInt, String replacement) {
        String value = readValue(message, in);
        if (value.equals(validNonInt)) {
            return replacement;
        }

        readInt(message, value);
        return value;
    }

    private void readFunction(String message, Scanner in) {
        int numBraces = 1;

        String firstLine = readLine(message, in);
        Assert.assertTrue(message + " " + firstLine, firstLine.startsWith("function ("));
        while (true) {
            String nextLine = in.nextLine();
            numBraces += (int)nextLine.chars().filter(c -> c == '{').count();
            numBraces -= (int)nextLine.chars().filter(c -> c == '}').count();
            if (numBraces == 0) {
                break;
            }
        }
    }

    private void readEffect(String message, Scanner in) {
        Assert.assertEquals(message, "{", readLine(message, in));
        while (true) {
            String key = in.next();
            if (key.trim().startsWith("}")) {
                break;
            } else if (key.startsWith("//")) {
                in.nextLine();
                continue;
            }

            message += " " + key;
            Assert.assertTrue(message, StringUtils.isAlphaOnly(key));

            switch (key) {
                case "onResidualOrder":
                case "onResidualSubOrder":
                case "duration":
                case "onModifyWeightPriority":
                case "onTryHitPriority":
                case "onDamagePriority":
                case "onBasePowerPriority":
                case "onRedirectTargetPriority":
                case "onBeforeSwitchOutPriority":
                case "onBeforeMovePriority":
                case "onModifyMovePriority":
                case "onFoeRedirectTargetPriority":
                case "onSwitchInPriority":
                case "onFoeBeforeMovePriority":
                case "onTryMovePriority":
                case "onFoeTrapPokemonPriority":
                case "onTryPrimaryHitPriority":
                case "onAccuracyPriority":
                    readInt(message, in);
                    break;
                case "noCopy":
                case "onTryHeal":
                case "onCriticalHit":
                    readBoolean(message, in);
                    break;
                case "onLockMove":
                    readSingleQuotedString(message, in);
                    break;
                case "onStart":
                case "onResidual":
                case "onFoeAfterDamage":
                case "onFoeSwitchOut":
                case "onUpdate":
                case "onBeforeMove":
                case "onEnd":
                case "durationCallback":
                case "onAnyModifyDamage":
                case "onRestart":
                case "onModifyWeight":
                case "onTryHit":
                case "onHit":
                case "onDamage":
                case "onMoveAborted":
                case "onAccuracy":
                case "onSourceBasePower":
                case "onBasePower":
                case "onRedirectTarget":
                case "onFaint":
                case "onBeforeSwitchOut":
                case "onImmunity":
                case "onSourceModifyDamage":
                case "onDisableMove":
                case "onSetStatus":
                case "onTryAddVolatile":
                case "onModifyMove":
                case "onOverrideAction":
                case "onTrapPokemon":
                case "onModifyCritRatio":
                case "onFoeRedirectTarget":
                case "onNegateImmunity":
                case "onModifyBoost":
                case "onModifySpe":
                case "onTerrain":
                case "onModifyAccuracy":
                case "onSwitchIn":
                case "onFoeDisableMove":
                case "onFoeBeforeMove":
                case "onDragOut":
                case "onSourceAccuracy":
                case "onAllyTryHitSide":
                case "onBoost":
                case "onTryMove":
                case "onCopy":
                case "onAnyDragOut":
                case "onFoeTrapPokemon":
                case "onAnyAccuracy":
                case "onAnyBasePower":
                case "onAnyTryMove":
                case "onTryPrimaryHit":
                case "onAnySetStatus":
                    readFunction(message, in);
                    break;
                default:
                    Assert.fail(message + "\nUnknown effect key " + key + "; line: " + in.nextLine());
                    break;
            }
        }
    }

    private int[] readStatChanges(String message, Scanner in) {
        int[] statChanges = new int[Stat.NUM_BATTLE_STATS];

        Assert.assertEquals(message, "{", readLine(message, in));
        while (true) {
            String key = in.next();
            if (key.trim().startsWith("}")) {
                break;
            }

            message += " " + key;
            Assert.assertTrue(message, StringUtils.isAlphaOnly(key));

            Stat stat = getStat(message, key);
            int value = readInt(message, in);

            Assert.assertEquals(message, 0, statChanges[stat.index()]);
            statChanges[stat.index()] = value;
        }

        return statChanges;
    }

    private Stat getStat(String message, String key) {
        switch (key) {
            case "atk":
                return Stat.ATTACK;
            case "def":
                return Stat.DEFENSE;
            case "spa":
                return Stat.SP_ATTACK;
            case "spd":
                return Stat.SP_DEFENSE;
            case "spe":
                return Stat.SPEED;
            case "accuracy":
                return Stat.ACCURACY;
            case "evasion":
                return Stat.EVASION;
            default:
                Assert.fail(message + "\nUnknown stat name " + key);
                return Stat.HP;
        }
    }

    private SecondaryEffect readSecondaries(String message, Scanner in) {
        SecondaryEffect secondary = new SecondaryEffect();

        Assert.assertEquals(message, "[", readLine(message, in));

        while (true) {
            String firstLine = in.nextLine().trim();
            if (firstLine.equals("],")) {
                break;
            }

            Assert.assertTrue(message + " " + firstLine, firstLine.endsWith("{"));
            while (true) {
                String key = in.next().trim();
                if (key.startsWith("},")) {
                    String nextLine = in.nextLine().trim();
                    if (nextLine.endsWith("{")) {
                        continue;
                    } else {
                        break;
                    }
                }

                switch (key) {
                    case "boosts":
                        Assert.assertNull(message, secondary.statChanges);
                        secondary.statChanges = readStatChanges(message, in);
                        break;
                    case "chance":
                        if (secondary.chance == null) {
                            secondary.chance = 0;
                        }
                        secondary.chance += readInt(message, in);
                        break;
                    case "volatileStatus":
                        secondary.volatileStatus = readSingleQuotedString(message, in);
                        break;
                    case "status":
                        Assert.assertNull(message, secondary.status);
                        secondary.status = readStatus(message, in);
                        break;
                    default:
                        Assert.fail(message + "\nUnknown key " + key);
                        break;
                }
            }
        }

        return secondary;
    }

    private SecondaryEffect readSecondary(String message, Scanner in) {
        SecondaryEffect secondary = new SecondaryEffect();

        String firstLine = readLine(message, in);
        if (firstLine.equals("false,")) {
            return null;
        }

        Assert.assertEquals(message, "{", firstLine);
        while (true) {
            String key = in.next();
            if (key.trim().startsWith("}")) {
                break;
            }

            message += " " + key;
            Assert.assertTrue(message, StringUtils.isAlphaOnly(key));

            switch (key) {
                case "dustproof":
                    checkBoolean(message, in, attackKey, "sparklingaria");
                    break;
                case "boosts":
                    secondary.statChanges = readStatChanges(message, in);
                    break;
                case "chance":
                    secondary.chance = readInt(message, in);
                    break;
                case "volatileStatus":
                    secondary.volatileStatus = readSingleQuotedString(message, in);
                    break;
                case "status":
                    secondary.status = readStatus(message, in);
                    break;
                case "self":
                    secondary.self = readSelf(message, in);
                    break;
                case "onHit":
                    readFunction(message, in);
                    this.functionKeys.add(key);
                    break;
                default:
                    Assert.fail(message + "\nUnknown key " + key);
                    break;
            }
        }

        return secondary;
    }

    private StatusNamesies readStatus(String message, Scanner in) {
        String value = readSingleQuotedString(message, in);
        switch (value) {
            case "par":
                return StatusNamesies.PARALYZED;
            case "brn":
                return StatusNamesies.BURNED;
            case "frz":
                return StatusNamesies.FROZEN;
            case "psn":
                return StatusNamesies.POISONED;
            case "tox":
                return StatusNamesies.BADLY_POISONED;
            case "slp":
                return StatusNamesies.ASLEEP;
            default:
                Assert.fail(message + "\nUnknown status " + value);
                return null;
        }
    }

    private Self readSelf(String message, Scanner in) {
        Self self = new Self();

        String firstLine = readLine(message, in);
        Assert.assertEquals(message, "{", firstLine);

        while (true) {
            String key = in.next();
            if (key.trim().startsWith("}")) {
                break;
            }

            switch (key) {
                case "boosts":
                    self.boosts = readStatChanges(message, in);
                    break;
                case "volatileStatus":
                    self.volatileStatus = readSingleQuotedString(message, in);
                    break;
                case "onHit":
                    readFunction(message, in);
                    break;
                default:
                    Assert.fail(message + "\nUnknown key " + key);
                    break;
            }
        }

        return self;
    }

    public AttackNamesies getAttack() {
        if (attackName.equals("Judgment")) {
            return AttackNamesies.JUDGEMENT;
        }

        return AttackNamesies.tryValueOf(attackName);
    }

    // Note: Removes the value from the map in the process for the empty check
    public Boolean is(String booleanName) {
        return this.booleanMap.remove(booleanName);
    }

    public void assertEmpty() {
        Assert.assertTrue(attackKey + " " + this.booleanMap, this.booleanMap.isEmpty());
    }

    public class Self {
        int[] boosts;
        String volatileStatus;
    }

    public class SecondaryEffect {
        int[] statChanges;
        Integer chance;
        String volatileStatus;
        Self self;
        StatusNamesies status;
        Set<String> functionKeys;

        public SecondaryEffect() {
            functionKeys = new HashSet<>();
        }
    }
}
