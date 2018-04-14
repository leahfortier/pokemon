package test.script;

import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.MoveCategory;
import battle.attack.MoveType;
import battle.effect.EffectInterfaces.SwapOpponentEffect;
import battle.effect.InvokeInterfaces.AlwaysCritEffect;
import battle.effect.InvokeInterfaces.CrashDamageMove;
import battle.effect.InvokeInterfaces.CritStageEffect;
import battle.effect.InvokeInterfaces.OpponentIgnoreStageEffect;
import battle.effect.attack.OhkoMove;
import generator.ClassFields;
import generator.GeneratorType;
import generator.format.InputFormatter;
import generator.update.ItemUpdater;
import generator.update.ItemUpdater.ItemParser;
import generator.update.MoveUpdater;
import generator.update.MoveUpdater.MoveParser;
import generator.update.UpdateGen;
import item.Item;
import item.ItemNamesies;
import item.bag.BagCategory;
import item.berry.Berry;
import item.hold.HoldItem;
import item.use.BallItem;
import item.use.BattleUseItem;
import item.use.EvolutionItem;
import item.use.TechnicalMachine;
import org.junit.Assert;
import org.junit.Test;
import test.BaseTest;
import type.Type;
import util.file.FileIO;
import util.file.Folder;
import util.string.StringUtils;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class ScriptTest extends BaseTest {
    @Test
    public void moveParserTest() {
        Set<AttackNamesies> toParse = EnumSet.allOf(AttackNamesies.class);
        toParse.remove(AttackNamesies.CONFUSION_DAMAGE);

        for (MoveParser moveParser : new MoveUpdater().getParseMoves()) {
            AttackNamesies attackNamesies = moveParser.attackNamesies;
            Type type = moveParser.type;
            MoveCategory category = moveParser.category;

            int pp = moveParser.pp;
            int power = moveParser.power;
            int accuracy = moveParser.accuracy;

            String chance = moveParser.chance;
            String crit = moveParser.crit;
            int priority = moveParser.priority;

            boolean physicalContact = moveParser.physicalContact;
            boolean soundMove = moveParser.soundMove;
            boolean punchMove = moveParser.punchMove;
            boolean snatchable = moveParser.snatchable;
            boolean defrosty = moveParser.defrosty;
            boolean magicBouncy = moveParser.magicBouncy;
            boolean protecty = moveParser.protecty;
            boolean mirrorMovey = moveParser.mirrorMovey;

            // A few special cases
            switch (attackNamesies) {
                case STRUGGLE:
                    // Because I'm right and this is wrong
                    Assert.assertEquals(type, Type.NORMAL);
                    type = Type.NO_TYPE;
                    break;
                case QUICK_GUARD:
                    // Doesn't make sense to snatch with only single battles, plus they're in the same priority bracket
                    Assert.assertEquals(true, snatchable);
                    snatchable = false;
                    // Fall through
                case CRAFTY_SHIELD:
                    // Why are Quick Guard/Crafty Shield different than Protect/Spiky Shield/etc. it's weird and I don't care
                    Assert.assertEquals(3, priority);
                    priority = 4;
                    break;
                case POISON_JAB:
                case PLASMA_FISTS:
                    // Because they should be
                    Assert.assertEquals(false, punchMove);
                    punchMove = true;
                    break;
                case ELECTRIFY:
                    // I merged this move with Ion Deluge and that includes priority
                    Assert.assertEquals(0, priority);
                    priority = 1;
                    break;
                case IMPRISON:
                    // I don't know why this is like this but it makes no sense
                    Assert.assertEquals(true, snatchable);
                    Assert.assertEquals(false, magicBouncy);
                    Assert.assertEquals(false, mirrorMovey);
                    snatchable = false;
                    magicBouncy = true;
                    mirrorMovey = true;
                    break;
                case ACUPRESSURE:
                case AROMATIC_MIST:
                case FLOWER_SHIELD:
                case ROTOTILLER:
                    // These were changed to only affect the user
                    Assert.assertEquals(false, snatchable);
                    snatchable = true;
                    break;
                case TEETER_DANCE:
                    // This too
                    Assert.assertEquals(false, magicBouncy);
                    magicBouncy = true;
                    break;
                case FLORAL_HEALING:
                case PURIFY:
                    // Also these moves
                    Assert.assertEquals(false, snatchable);
                    Assert.assertEquals(true, magicBouncy);
                    Assert.assertEquals(true, protecty);
                    snatchable = true;
                    magicBouncy = false;
                    protecty = false;
                    break;
                case MIND_READER:
                case LOCK_ON:
                    // Because I don't care enough
                    Assert.assertEquals(true, protecty);
                    Assert.assertEquals(true, mirrorMovey);
                    protecty = false;
                    mirrorMovey = false;
                    break;
                case MIMIC:
                case REFLECT_TYPE:
                case POWER_SPLIT:
                case GUARD_SPLIT:
                    // DGAF
                    Assert.assertEquals(true, protecty);
                    protecty = false;
                    break;
                case MEAN_LOOK:
                case BLOCK:
                case BESTOW:
                    // I feel like I should be right here
                    Assert.assertEquals(false, protecty);
                    protecty = true;
                    break;
                case WONDER_ROOM:
                case TRICK_ROOM:
                case MAGIC_ROOM:
                case FAIRY_LOCK:
                    // This makes no sense I reign supreme
                    Assert.assertEquals(true, mirrorMovey);
                    mirrorMovey = false;
                    break;
                case PSYCH_UP:
                case HEAL_PULSE:
                    // Again, I make the rules in this game
                    Assert.assertEquals(false, mirrorMovey);
                    mirrorMovey = true;
                    break;
                case TRIPLE_KICK:
                    // TODO: This is temporary and should be fixed
                    Assert.assertEquals(10, power);
                    power = 20;
                    break;
                case FOUL_PLAY:
                    // TODO: This should be fixed as well
                    Assert.assertEquals(95, power);
                    power = 1;
                    break;
            }

            Attack attack = attackNamesies.getNewAttack();
            Assert.assertEquals(attack.getName(), type, attack.getActualType());
            Assert.assertEquals(attack.getName(), category, attack.getCategory());
            Assert.assertEquals(attack.getName(), pp, attack.getPP());
            Assert.assertEquals(attack.getName(), priority, attack.getActualPriority());
            Assert.assertEquals(attack.getName(), physicalContact, attack.isMoveType(MoveType.PHYSICAL_CONTACT));
            Assert.assertEquals(attack.getName(), soundMove, attack.isMoveType(MoveType.SOUND_BASED));
            Assert.assertEquals(attack.getName(), punchMove, attack.isMoveType(MoveType.PUNCHING));
            Assert.assertEquals(attack.getName(), defrosty, attack.isMoveType(MoveType.DEFROST));

            String powerString = power == 0 || power == 1 ? "--" : Integer.toString(power);
            Assert.assertEquals(attack.getName(), powerString, attack.getPowerString());

            String accuracyString = accuracy == 0 ? "--" : Integer.toString(accuracy);
            Assert.assertEquals(attack.getName(), accuracyString, attack.getAccuracyString());

            int effectChance = chance.equals("--") ? 100 : Integer.parseInt(chance);
            Assert.assertEquals(attack.getName(), effectChance, attack.getEffectChance());

            Assert.assertEquals(attack.getName(), crit.equals("None"), attack.isStatusMove());
            switch (crit) {
                case "None":  // Status moves
                case "4.17%": // Regular attacking moves
                    Assert.assertFalse(attack.getName(), attack instanceof CritStageEffect);
                    Assert.assertFalse(attack.getName(), attack instanceof AlwaysCritEffect);
                    break;
                case "12.5%": // Crit-stage increased moves
                    Assert.assertTrue(attack.getName(), attack instanceof CritStageEffect);
                    Assert.assertFalse(attack.getName(), attack instanceof AlwaysCritEffect);
                    break;
                case "100%": // Always crit moves
                    Assert.assertFalse(attack.getName(), attack instanceof CritStageEffect);
                    Assert.assertTrue(attack.getName(), attack instanceof AlwaysCritEffect);
                    break;
                default:
                    Assert.fail("Invalid crit ratio " + crit + " for " + attack.getName());
                    break;
            }

            Assert.assertEquals(attack.getName(), snatchable, attack.isSelfTargetStatusMove() && !attack.isMoveType(MoveType.NON_SNATCHABLE));
            Assert.assertEquals(attack.getName(), magicBouncy, !attack.isSelfTarget() && attack.isStatusMove() && !attack.isMoveType(MoveType.NO_MAGIC_COAT));
            Assert.assertEquals(attack.getName(), protecty, !attack.isSelfTargetStatusMove() && !attack.isMoveType(MoveType.FIELD) && !attack.isMoveType(MoveType.PROTECT_PIERCING));
            Assert.assertEquals(attack.getName(), mirrorMovey, !attack.isSelfTargetStatusMove() && !attack.isMoveType(MoveType.FIELD) && !attack.isMoveType(MoveType.MIRRORLESS));

            toParse.remove(attackNamesies);
        }

        Assert.assertTrue(toParse.isEmpty());
    }

    @Test
    public void itemParserTest() {
        Set<ItemNamesies> toParse = EnumSet.allOf(ItemNamesies.class);
        toParse.remove(ItemNamesies.NO_ITEM);
        toParse.remove(ItemNamesies.SYRUP);
        toParse.remove(ItemNamesies.SURFBOARD);
        toParse.remove(ItemNamesies.RUBY);
        toParse.removeIf(itemNamesies -> itemNamesies.getItem() instanceof TechnicalMachine);

        for (ItemParser itemParser : new ItemUpdater().getParseItems()) {
            ItemNamesies itemNamesies = itemParser.itemNamesies;
            String itemType = itemParser.itemType;

            int fling = itemParser.fling;
            int price = itemParser.price;

            Type naturalGiftType = itemParser.naturalGiftType;
            int naturalGiftPower = itemParser.naturalGiftPower;

            Item item = itemNamesies.getItem();
            if (item.isHoldable() && fling != 0) {
                HoldItem holdItem = (HoldItem)item;
                Assert.assertEquals(item.getName(), fling, holdItem.flingDamage());
            } else if (!(item instanceof BallItem)) {
                // Ball items are not holdable in this game
                Assert.assertEquals(item.getName(), 0, fling);
            }

            if (item.getBagCategory() != BagCategory.KEY_ITEM) {
                // Serebii has the wrong values for these, and I manually looked up in Bulbapedia instead (which is more accurate but way harder to parse)
                // Just gonna leave this commented out since it's annoying and who cares
//                TestUtils.semiAssertTrue(StringUtils.spaceSeparated(item.getName(), price, item.getPrice()), price == item.getPrice());

                if (itemNamesies == ItemNamesies.MASTER_BALL || itemNamesies == ItemNamesies.SAFARI_BALL) {
                    Assert.assertEquals(item.getName(), 0, item.getPrice());
                } else {
                    Assert.assertTrue(item.getName(), item.getPrice() > 0);
                }
            } else {
                Assert.assertEquals(item.getName(), 0, price);
                Assert.assertEquals(item.getName(), -1, item.getPrice());
            }

            if (item instanceof Berry) {
                Berry berry = (Berry)item;
                Assert.assertEquals(item.getName(), naturalGiftType, berry.naturalGiftType());
                Assert.assertEquals(item.getName(), naturalGiftPower, berry.naturalGiftPower());
                Assert.assertNotEquals(item.getName(), Type.NO_TYPE, naturalGiftType);
                Assert.assertNotEquals(item.getName(), 0, naturalGiftPower);
            } else {
                Assert.assertEquals(item.getName(), Type.NO_TYPE, naturalGiftType);
                Assert.assertEquals(item.getName(), 0, naturalGiftPower);
            }

            switch (itemType) {
                case "Battle Effect":
                    Assert.assertTrue(item.getName(), item instanceof BattleUseItem);
                    break;
                case "Miscellaneous":
                    Assert.assertTrue(item.getName(), item.getBagCategory() == BagCategory.MISC);
                    break;
                case "Evolutionary":
                    Assert.assertTrue(item.getName(), item instanceof EvolutionItem);
                    break;
                case "Berry":
                    Assert.assertTrue(item.getName(), item.getBagCategory() == BagCategory.BERRY);
                    break;
                case "Key Item":
                    Assert.assertTrue(item.getName(), item.getBagCategory() == BagCategory.KEY_ITEM);
                    break;
                case "Hold Item":
                    Assert.assertTrue(item.getName(), item instanceof HoldItem);
                    break;
                case "Recovery":
                    Assert.assertTrue(item.getName(), item.getBagCategory() == BagCategory.MEDICINE);
                    break;
                case "Pokeball":
                    Assert.assertTrue(item.getName(), item.getBagCategory() == BagCategory.BALL);
                    break;
                case "Vitamins":
                    Assert.assertTrue(item.getName(), item.getBagCategory() == BagCategory.STAT);
                    break;
                default:
                    Assert.fail(item.getName() + ": " + itemType);
                    break;
            }

            toParse.remove(itemNamesies);
        }

        Assert.assertTrue(toParse.isEmpty());
    }

    @Test
    public void showdownMoveParserTest() {
        Scanner in = FileIO.openFile(Folder.SCRIPTS + "ps-moves.txt");
        in.useDelimiter("[\\s:]+");

        Assert.assertEquals("{", in.nextLine());

        Set<String> unimplementedIds = UpdateGen.unimplementedMoves
                .stream()
                .map(this::getId)
                .collect(Collectors.toSet());

        Map<AttackNamesies, ShowdownMoveParser> moveMap = new EnumMap<>(AttackNamesies.class);
        while (in.hasNext()) {
            String attackKey = StringUtils.trimQuotes(in.next());
            if (attackKey.equals("}")) {
                break;
            }

            Assert.assertTrue(attackKey, attackKey.matches("[a-z0-9]+"));
            Assert.assertEquals(attackKey, "{", in.next());
            Assert.assertEquals(attackKey, "", in.nextLine());

            ShowdownMoveParser moveParser = new ShowdownMoveParser(in, attackKey);
            AttackNamesies attackNamesies = moveParser.getAttack();
            String isZ = moveParser.isZ;
            Boolean isUnreleased = moveParser.is("isUnreleased");
            Boolean isNonstandard = moveParser.is("isNonstandard");
            if (attackNamesies == null) {
                if (attackKey.startsWith("hiddenpower")) {
                    Assert.assertNotNull(attackKey, StringUtils.enumTryValueOf(Type.class, attackKey.substring("hiddenpower".length())));
                } else if (unimplementedIds.contains(attackKey)) {
                    Assert.assertNull(attackKey, isZ);
                    Assert.assertNull(attackKey, isUnreleased);
                    Assert.assertNull(attackKey, isNonstandard);
                } else if (isZ != null) {
                    Assert.assertNull(attackKey, isUnreleased);
                    Assert.assertNull(attackKey, isNonstandard);
                } else if (isUnreleased != null && isUnreleased) {
                    Assert.assertNull(attackKey, isNonstandard);
                } else {
                    Assert.assertNotNull(attackKey, isNonstandard);
                    Assert.assertTrue(attackKey, isNonstandard);
                }
            } else {
                Assert.assertFalse(attackKey, unimplementedIds.contains(attackKey));
                Assert.assertNull(attackKey, isZ);
                Assert.assertNull(attackKey, isNonstandard);
                moveMap.put(attackNamesies, moveParser);
            }
        }
        in.close();

        Set<AttackNamesies> allAttacks = EnumSet.complementOf(EnumSet.of(AttackNamesies.CONFUSION_DAMAGE));
        for (AttackNamesies attackNamesies : allAttacks) {
            Assert.assertTrue(attackNamesies.getName(), moveMap.containsKey(attackNamesies));
        }
        Assert.assertEquals(allAttacks.size(), moveMap.size());

        Map<AttackNamesies, ClassFields> genFieldsMap = readGen();
        Assert.assertEquals(AttackNamesies.values().length, genFieldsMap.size());

        for (AttackNamesies attackNamesies : moveMap.keySet()) {
            ShowdownMoveParser moveParser = moveMap.get(attackNamesies);
            Attack attack = attackNamesies.getNewAttack();
            ClassFields genFields = genFieldsMap.get(attackNamesies);

            Boolean ignoreImmunity = moveParser.is("ignoreImmunity");
            if (attackNamesies == AttackNamesies.THUNDER_WAVE) {
                Assert.assertNotNull(attackNamesies.getName(), ignoreImmunity);
                Assert.assertFalse(attackNamesies.getName(), ignoreImmunity);
            } else {
                // TODO: Add tests for these
                checkBoolean(attackNamesies, ignoreImmunity, AttackNamesies.FUTURE_SIGHT, AttackNamesies.BIDE);
            }

            // TODO: I don't think this is actually enforced for trump card/sketch
            checkBoolean(attackNamesies, moveParser.is("noPPBoosts"), AttackNamesies.STRUGGLE, AttackNamesies.TRUMP_CARD, AttackNamesies.SKETCH);

            // TODO: I don't think this is enforced -- only Fire-type moves
            checkBoolean(attackNamesies, moveParser.is("thawsTarget"), AttackNamesies.SCALD, AttackNamesies.STEAM_ERUPTION);

            // Note: Chatter can be sketched in this game
            checkBoolean(attackNamesies, moveParser.is("noSketch"), AttackNamesies.STRUGGLE, AttackNamesies.CHATTER);

            checkBoolean(attackNamesies, moveParser.is("struggleRecoil"), AttackNamesies.STRUGGLE);
            checkBoolean(attackNamesies, moveParser.is("mindBlownRecoil"), AttackNamesies.MIND_BLOWN);
            checkBoolean(attackNamesies, moveParser.is("noFaint"), AttackNamesies.FALSE_SWIPE);
            checkBoolean(attackNamesies, moveParser.is("sleepUsable"), AttackNamesies.SLEEP_TALK, AttackNamesies.SNORE);
            checkBoolean(attackNamesies, moveParser.is("stealsBoosts"), AttackNamesies.SPECTRAL_THIEF);

            // TODO: Neither of these actually work as expected right now
            checkBoolean(attackNamesies, moveParser.is("multiaccuracy"), AttackNamesies.TRIPLE_KICK);
            checkBoolean(attackNamesies, moveParser.is("useTargetOffensive"), AttackNamesies.FOUL_PLAY);

            Boolean isFutureMove = moveParser.is("isFutureMove");
            checkBoolean(attackNamesies, isFutureMove, AttackNamesies.FUTURE_SIGHT, AttackNamesies.DOOM_DESIRE);
            checkBoolean(attackNamesies, moveParser.is("breaksProtect"), !attack.isStatusMove() && attack.isMoveType(MoveType.PROTECT_PIERCING) && isFutureMove == null);

            checkBoolean(attackNamesies, moveParser.is("ignoreEvasion"), attack instanceof OpponentIgnoreStageEffect);
            checkBoolean(attackNamesies, moveParser.is("ignoreDefensive"), attack instanceof OpponentIgnoreStageEffect);
            checkBoolean(attackNamesies, moveParser.is("ohko"), attack instanceof OhkoMove);
            checkBoolean(attackNamesies, moveParser.is("hasCustomRecoil"), attack instanceof CrashDamageMove);
            checkBoolean(attackNamesies, moveParser.is("willCrit"), attack instanceof AlwaysCritEffect);
            checkBoolean(attackNamesies, moveParser.is("forceSwitch"), attack instanceof SwapOpponentEffect);

            checkBoolean(attackNamesies, moveParser.is("ignoreAbility"), genFields.contains("IgnoreAbilityMove"));
            checkBoolean(attackNamesies, moveParser.is("selfSwitch"), genFields.contains("SelfSwitching") || attackNamesies == AttackNamesies.BATON_PASS);

            moveParser.assertEmpty();
        }
    }

    private Map<AttackNamesies, ClassFields> readGen() {
        Scanner in = FileIO.openFile(GeneratorType.ATTACK_GEN.getInputPath());
        new InputFormatter().readFileFormat(in);

        Map<AttackNamesies, ClassFields> fieldsMap = new EnumMap<>(AttackNamesies.class);
        while (in.hasNext()) {
            String line = in.nextLine().trim();

            // Ignore comments and white space
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String name = line.replace(":", "");
            ClassFields fields = new ClassFields(in, name);

            fieldsMap.put(AttackNamesies.getValueOf(name), fields);
        }

        return fieldsMap;
    }

    private void checkBoolean(AttackNamesies attackNamesies, Boolean value, AttackNamesies... trueAttacks) {
        boolean inList = false;
        for (AttackNamesies trueAttack : trueAttacks) {
            if (trueAttack == attackNamesies) {
                inList = true;
                break;
            }
        }
        checkBoolean(attackNamesies, value, inList);
    }

    private void checkBoolean(AttackNamesies attackNamesies, Boolean value, boolean attackTrue) {
        if (attackTrue) {
            Assert.assertNotNull(attackNamesies.getName(), value);
            Assert.assertTrue(attackNamesies.getName(), value);
        } else {
            Assert.assertNull(attackNamesies.getName(), value);
        }
    }

    // Id is all lowercase no special characters (except numbers if applicable)
    private String getId(String attackName) {
        return StringUtils.getNamesiesString(attackName).replaceAll("_", "").toLowerCase();
    }
}
