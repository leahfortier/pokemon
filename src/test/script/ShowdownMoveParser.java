package test.script;

import battle.attack.AttackNamesies;
import battle.attack.MoveCategory;
import battle.effect.status.StatusNamesies;
import org.junit.Assert;
import pokemon.stat.Stat;
import test.general.TestUtils;
import type.Type;
import util.GeneralUtils;
import util.string.StringAppender;
import util.string.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShowdownMoveParser {
    private String attackKey;
    public String attackName;
    public String accuracy;
    public Integer basePower;
    public Integer fixedDamage;
    public Integer pp;
    public Integer priority;
    public Integer critRatio;
    public MoveCategory category;
    public MoveCategory defensiveCategory;
    public Set<String> flags;
    public String target;
    public String selfDestruct;
    public int[] drain;
    public int[] heal;
    public int[] recoil;
    public int[] multiHit;
    public StatusNamesies status;
    public String volatileStatus;
    public String isZ;
    public Type type;
    public int[] boosts;
    private Map<String, Boolean> booleanMap;
    public Set<String> functionKeys;
    public SecondaryEffect secondary;
    public Self self;
    public String[] noMetronome;

    public ShowdownMoveParser(Scanner in, String attackKey) {
        this.attackKey = attackKey;
        this.functionKeys = new HashSet<>();
        this.booleanMap = new HashMap<>();

        Set<String> seenKeys = new HashSet<>();
        while (true) {
            String key = readKey(in);
            if (key.startsWith("}")) {
                break;
            } else if (key.startsWith("//")) {
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
                case "contestType":
                case "nonGhostTarget":
                case "pressureTarget":
                    readString(message, in);
                    break;
                case "isMax":
                    if (attackKey.startsWith("gmax")) {
                        readString(message, in);
                    } else {
                        Assert.assertTrue(message, attackKey.startsWith("max"));
                        Assert.assertTrue(message, readBoolean(message, in));
                    }
                    this.booleanMap.put(key, true);
                    break;
                case "realMove":
                    Assert.assertEquals("Hidden Power", readString(message, in));
                    Assert.assertTrue(attackKey.startsWith("hiddenpower"));
                    Assert.assertNotNull(attackKey, StringUtils.enumTryValueOf(Type.class, attackKey.substring("hiddenpower".length())));
                    break;
                case "zMove":
                case "maxMove":
                    readCurly(message, in);
                    break;
                case "noMetronome":
                    Assert.assertEquals(message, "metronome", attackKey);
                    this.noMetronome = readStringBraces(message, in);
                    break;
                case "condition":
                    readCondition(message, in);
                    break;
                case "accuracy":
                    this.accuracy = readValue(message, in);
                    if (accuracy.equals("true")) {
                        accuracy = "--";
                    } else {
                        readInt(message, accuracy);
                    }
                    break;
                case "basePower":
                    this.basePower = readInt(message, in);
                    break;
                case "damage":
                    switch (attackKey) {
                        case "nightshade":
                        case "seismictoss":
                            Assert.assertEquals(message, "level", readSingleQuotedString(message, in));
                            this.functionKeys.add("damageCallback");
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
                    this.flags = readFlags(message, in);
                    this.flags.remove("distance"); // distance: Can target a Pokemon positioned anywhere in a Triple Battle.
                    this.flags.remove("mystery");  // mystery: Unknown effect.
                    this.flags.remove("nonsky");   // nonsky: Prevented from being executed or selected in a Sky Battle.
                    break;
                case "drain":
                    this.drain = readBraces(message, in);
                    break;
                case "heal":
                    this.heal = readBraces(message, in);
                    break;
                case "recoil":
                    this.recoil = readBraces(message, in);
                    break;
                case "multihit":
                    this.multiHit = readMultiHit(message, in);
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
                case "sideCondition":
                case "slotCondition":
                case "weather":
                case "terrain":
                case "pseudoWeather":
                    Assert.assertNull(message, this.volatileStatus);
                    this.volatileStatus = readSingleQuotedString(message, in).toLowerCase();
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
                        Assert.assertEquals(message, "'Ground': true", readCurly(message, in));
                        ignoreImmunity = true;
                    } else {
                        ignoreImmunity = readBoolean(message, in);
                    }
                    this.booleanMap.put(key, ignoreImmunity);
                    break;
                case "isNonstandard":
                    String value = readString(message, in);
                    Assert.assertTrue(message, Set.of("Past", "LGPE", "Gigantamax", "CAP").contains(value));
                    if (!value.equals("Past")) {
                        this.booleanMap.put(key, true);
                    }
                    break;
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
                case "hasCrashDamage":
                case "breaksProtect":
                case "thawsTarget":
                case "sleepUsable":
                case "useSourceDefensiveAsOffensive":
                case "smartTarget":
                case "tracksTarget":
                    this.booleanMap.put(key, readBoolean(message, in));
                    break;
                case "onTryHit":
                    if (attackKey.equals("teleport")) {
                        Assert.assertTrue(message, readBoolean(message, in));
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
                case "onModifyType":
                case "onTryImmunity":
                case "onModifyPriority":
                    readFunction(message, in);
                    this.functionKeys.add(key);
                    break;
                default:
                    Assert.fail(message + " " + readLine(message, in) + "\nUnknown key " + key);
                    break;
            }
        }
    }

    private String readKey(Scanner in) {
        String key = in.next().trim();
        if (key.startsWith("}")) {
            Assert.assertEquals("},", key);
            return "}";
        } else if (key.startsWith("//")) {
            // Comment -- consume the rest of the line
            return "// " + in.nextLine();
        } else if (key.contains("(")) {
            // Method name -- read the rest of the header and then remove the parameters/parentheses
            StringAppender methodName = new StringAppender(key);
            while (!methodName.toString().contains(")")) {
                methodName.append(" ").append(in.next());
            }
            key = StringUtils.getMethodName(methodName.toString());
        }
        return key;
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
        value = value.substring(1, value.length() - 1);
        return value;
    }

    private Set<String> readFlags(String message, Scanner in) {
        Set<String> flags = new HashSet<>();

        String value = readCurly(message, in);
        if (value.isEmpty()) {
            return flags;
        }

        Pattern flagPattern = Pattern.compile("([a-z]+): ([0-9]+)");
        String[] split = value.split(", ");

        for (String flagString : split) {
            Matcher matcher = flagPattern.matcher(flagString);
            Assert.assertTrue(message, matcher.matches());
            Assert.assertEquals(message, 1, Integer.parseInt(matcher.group(2)));

            String flag = matcher.group(1);
            Assert.assertFalse(message, flags.contains(flag));
            flags.add(flag);
        }

        return flags;
    }

    private String[] readStringBraces(String message, Scanner in) {
        String brace = readLine(message, in);
        Assert.assertEquals(message, "[", brace);

        String value = in.nextLine().trim();
        String move = "\"([A-Z][A-Za-z-' ]+)\"";
        Pattern pattern = Pattern.compile(String.format("^(%s, )+%s,$", move, move));
        Assert.assertTrue(value, pattern.matcher(value).matches());

        String[] values = value.split("[,]+");
        for (int i = 0; i < values.length; i++) {
            values[i] = StringUtils.trimQuotes(values[i].trim());
        }

        brace = in.nextLine().trim();
        Assert.assertEquals(message, "],", brace);

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

    private double readDouble(String message, Scanner in) {
        String value = readValue(message, in);
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            Assert.fail(message + ": " + value + " is not a double.");
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

    private void readBooleanOrFunction(String message, Scanner in) {
        if (in.hasNext("false,") || in.hasNext("true,")) {
            readBoolean(message, in);
        } else {
            readFunction(message, in);
        }
    }

    private void readFunction(String message, Scanner in) {
        String firstLine = in.nextLine().trim();
        Assert.assertEquals(message, "{", firstLine);

        int numBraces = 1;
        do {
            String nextLine = in.nextLine();
            numBraces += (int)nextLine.chars().filter(c -> c == '{').count();
            numBraces -= (int)nextLine.chars().filter(c -> c == '}').count();
        } while (numBraces != 0);
    }

    private void readCondition(String message, Scanner in) {
        Assert.assertEquals(message, "{", readLine(message, in));
        while (true) {
            String key = readKey(in);
            if (key.startsWith("}")) {
                break;
            } else if (key.startsWith("//")) {
                continue;
            }

            message += " " + key;
            Assert.assertTrue(message, StringUtils.isAlphaOnly(key));

            switch (key) {
                case "onResidualSubOrder":
                    readDouble(message, in);
                    break;
                case "onResidualOrder":
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
                case "onTypePriority":
                case "onModifyTypePriority":
                case "onSourceInvulnerabilityPriority":
                case "onEffectivenessPriority":
                    readInt(message, in);
                    break;
                case "noCopy":
                case "onCriticalHit":
                    readBoolean(message, in);
                    break;
                case "onLockMove":
                    readSingleQuotedString(message, in);
                    break;
                case "onStart":
                case "onResidual":
                case "onFoeAfterDamage":
                case "onEffectiveness":
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
                case "onTryImmunity":
                case "onAfterDamage":
                case "onTryHeal":
                case "onType":
                case "onAnyTryImmunity":
                case "onDamagingHit":
                case "onModifyType":
                case "onSwap":
                case "onAnyInvulnerability":
                case "onSourceInvulnerability":
                    readFunction(message, in);
                    break;
                case "onInvulnerability":
                    readBooleanOrFunction(message, in);
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
                        Assert.assertNull(message, secondary.boosts);
                        secondary.boosts = readStatChanges(message, in);
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
        if (firstLine.equals("null,")) {
            return null;
        }

        Assert.assertEquals(message, "{", firstLine);
        while (true) {
            String key = readKey(in);
            if (key.startsWith("}")) {
                break;
            } else if (key.startsWith("//")) {
                continue;
            }

            message += " " + key;
            Assert.assertTrue(message, StringUtils.isAlphaOnly(key));

            switch (key) {
                case "dustproof":
                    checkBoolean(message, in, attackKey, "sparklingaria");
                    break;
                case "boosts":
                    secondary.boosts = readStatChanges(message, in);
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
                    secondary.functionKeys.add(key);
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
            String key = readKey(in);
            if (key.startsWith("}")) {
                break;
            } else if (key.startsWith("//")) {
                continue;
            }

            switch (key) {
                case "boosts":
                    self.boosts = readStatChanges(message, in);
                    break;
                case "volatileStatus":
                case "sideCondition":
                case "pseudoWeather":
                    self.volatileStatus = readSingleQuotedString(message, in);
                    break;
                case "onHit":
                    readFunction(message, in);
                    self.onHit = true;
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

    public void addNoMetronome() {
        this.flags.add("noMetronome");
    }

    public int[] getBoosts() {
        int[] boosts = this.boosts;
        int[] secondaryBoosts = this.secondary == null ? null : this.secondary.boosts;
        int[] secondarySelfBoosts = this.secondary == null ? null : (this.secondary.self == null ? null : this.secondary.self.boosts);
        int[] selfBoosts = this.self == null ? null : this.self.boosts;

        // At most one of these can be non-empty
        int numNonNull = GeneralUtils.numNonNull(boosts, secondaryBoosts, secondarySelfBoosts, selfBoosts);
        TestUtils.assertEqualsAny(attackName, numNonNull, 0, 1);

        if (boosts != null) {
            this.boosts = null;
            return boosts;
        } else if (secondaryBoosts != null) {
            this.secondary.boosts = null;
            return secondaryBoosts;
        } else if (secondarySelfBoosts != null) {
            this.secondary.self.boosts = null;
            return secondarySelfBoosts;
        } else if (selfBoosts != null) {
            this.self.boosts = null;
            return selfBoosts;
        } else {
            return null;
        }
    }

    public void assertEmpty() {
        Assert.assertTrue(attackKey + " " + this.flags, this.flags.isEmpty());
        Assert.assertTrue(attackKey + " " + this.booleanMap, this.booleanMap.isEmpty());
    }

    public static class Self {
        int[] boosts;
        String volatileStatus;
        boolean onHit;

        @Override
        public String toString() {
            StringAppender s = new StringAppender();
            if (boosts != null) {
                s.appendDelimiter(" ", "Boosts: " + Arrays.toString(boosts));
            }
            if (!StringUtils.isNullOrEmpty(volatileStatus)) {
                s.appendDelimiter(" ", "Volatile: " + volatileStatus);
            }
            if (onHit) {
                s.appendDelimiter(" ", "OnHit");
            }
            return s.toString();
        }
    }

    public static class SecondaryEffect {
        int[] boosts;
        Integer chance;
        String volatileStatus;
        Self self;
        StatusNamesies status;
        Set<String> functionKeys;

        public SecondaryEffect() {
            functionKeys = new HashSet<>();
        }

        @Override
        public String toString() {
            StringAppender s = new StringAppender();
            if (boosts != null) {
                s.appendDelimiter(" ", "Boosts: " + Arrays.toString(boosts));
            }
            if (chance != null) {
                s.appendDelimiter(" ", "Chance: " + chance);
            }
            if (!StringUtils.isNullOrEmpty(volatileStatus)) {
                s.appendDelimiter(" ", "Volatile: " + volatileStatus);
            }
            if (self != null) {
                s.appendDelimiter(" ", "Self: <" + self + ">");
            }
            if (status != null) {
                s.appendDelimiter(" ", "Status: " + status);
            }
            if (!functionKeys.isEmpty()) {
                s.appendDelimiter(" ", "Functions: <" + functionKeys + ">");
            }
            return s.toString();
        }
    }
}
