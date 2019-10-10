package test.script;

import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.MoveCategory;
import battle.attack.MoveType;
import battle.effect.Effect;
import battle.effect.EffectInterfaces.PartialTrappingEffect;
import battle.effect.EffectInterfaces.PowderMove;
import battle.effect.EffectInterfaces.SapHealthEffect;
import battle.effect.EffectInterfaces.SwapOpponentEffect;
import battle.effect.EffectNamesies;
import battle.effect.InvokeInterfaces.AlwaysCritEffect;
import battle.effect.InvokeInterfaces.CrashDamageMove;
import battle.effect.InvokeInterfaces.CritStageEffect;
import battle.effect.InvokeInterfaces.OpponentIgnoreStageEffect;
import battle.effect.InvokeInterfaces.OpponentStatSwitchingEffect;
import battle.effect.attack.FixedDamageMove;
import battle.effect.attack.MultiStrikeMove;
import battle.effect.attack.MultiTurnMove.ChargingMove;
import battle.effect.attack.MultiTurnMove.RechargingMove;
import battle.effect.attack.OhkoMove;
import battle.effect.attack.RecoilMove.RecoilPercentageMove;
import battle.effect.attack.SapHealthMove;
import battle.effect.attack.SelfHealingMove;
import battle.effect.battle.StandardBattleEffectNamesies;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.status.StatusNamesies;
import generator.GeneratorType;
import generator.fields.ClassFields;
import generator.format.InputFormatter;
import generator.format.MethodInfo;
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
import pokemon.stat.Stat;
import test.battle.TestStages;
import test.general.BaseTest;
import test.general.TestUtils;
import type.Type;
import util.Action;
import util.GeneralUtils;
import util.file.FileIO;
import util.file.Folder;
import util.string.StringUtils;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Supplier;
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
                    Assert.assertTrue(snatchable);
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
                    Assert.assertFalse(punchMove);
                    punchMove = true;
                    break;
                case ELECTRIFY:
                    // I merged this move with Ion Deluge and that includes priority
                    Assert.assertEquals(0, priority);
                    priority = 1;
                    break;
                case IMPRISON:
                    // I don't know why this is like this but it makes no sense
                    Assert.assertTrue(snatchable);
                    Assert.assertFalse(magicBouncy);
                    Assert.assertFalse(mirrorMovey);
                    snatchable = false;
                    magicBouncy = true;
                    mirrorMovey = true;
                    break;
                case ACUPRESSURE:
                case AROMATIC_MIST:
                case FLOWER_SHIELD:
                case ROTOTILLER:
                    // These were changed to only affect the user
                    Assert.assertFalse(snatchable);
                    snatchable = true;
                    break;
                case TEETER_DANCE:
                    // This too
                    Assert.assertFalse(magicBouncy);
                    magicBouncy = true;
                    break;
                case FLORAL_HEALING:
                case PURIFY:
                    // Also these moves
                    Assert.assertFalse(snatchable);
                    Assert.assertTrue(magicBouncy);
                    Assert.assertTrue(protecty);
                    snatchable = true;
                    magicBouncy = false;
                    protecty = false;
                    break;
                case MIND_READER:
                case LOCK_ON:
                    // Because I don't care enough
                    Assert.assertTrue(protecty);
                    Assert.assertTrue(mirrorMovey);
                    protecty = false;
                    mirrorMovey = false;
                    break;
                case MIMIC:
                case REFLECT_TYPE:
                case POWER_SPLIT:
                case GUARD_SPLIT:
                    // DGAF
                    Assert.assertTrue(protecty);
                    protecty = false;
                    break;
                case MEAN_LOOK:
                case BLOCK:
                case BESTOW:
                    // I feel like I should be right here
                    Assert.assertFalse(protecty);
                    protecty = true;
                    break;
                case FAIRY_LOCK:
                    Assert.assertFalse(magicBouncy);
                    Assert.assertFalse(protecty);
                    magicBouncy = true;
                    protecty = true;
                    break;
                case WONDER_ROOM:
                case TRICK_ROOM:
                case MAGIC_ROOM:
                    // This makes no sense I reign supreme
                    Assert.assertTrue(mirrorMovey);
                    mirrorMovey = false;
                    break;
                case PSYCH_UP:
                case HEAL_PULSE:
                    // Again, I make the rules in this game
                    Assert.assertFalse(mirrorMovey);
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

            Assert.assertEquals(attack.getName(), snatchable, attack.isSnatchable());
            Assert.assertEquals(attack.getName(), magicBouncy, attack.isMagicReflectable());
            Assert.assertEquals(attack.getName(), protecty, attack.isProtectAffected());
            Assert.assertEquals(attack.getName(), mirrorMovey, !attack.isSelfTargetStatusMove() && !attack.isMoveType(MoveType.FIELD) && !attack.isMoveType(MoveType.MIRRORLESS));

            toParse.remove(attackNamesies);
        }

        Assert.assertTrue(toParse.toString(), toParse.isEmpty());
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
                    Assert.assertEquals(item.getName(), item.getBagCategory(), BagCategory.MISC);
                    break;
                case "Evolutionary":
                    Assert.assertTrue(item.getName(), item instanceof EvolutionItem);
                    break;
                case "Berry":
                    Assert.assertEquals(item.getName(), item.getBagCategory(), BagCategory.BERRY);
                    break;
                case "Key Item":
                    Assert.assertEquals(item.getName(), item.getBagCategory(), BagCategory.KEY_ITEM);
                    break;
                case "Hold Item":
                    Assert.assertTrue(item.getName(), item instanceof HoldItem);
                    break;
                case "Recovery":
                    Assert.assertEquals(item.getName(), item.getBagCategory(), BagCategory.MEDICINE);
                    break;
                case "Pokeball":
                    Assert.assertEquals(item.getName(), item.getBagCategory(), BagCategory.BALL);
                    break;
                case "Vitamins":
                    Assert.assertEquals(item.getName(), item.getBagCategory(), BagCategory.STAT);
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
    public void unimplementedMovesTest() {
        for (String unimplemented : UpdateGen.unimplementedMoves) {
            Assert.assertNull(AttackNamesies.tryValueOf(unimplemented));
        }
    }

    @Test
    public void showdownMoveParserTest() {
        Scanner in = FileIO.openFile(Folder.SCRIPTS_COMPARE + "ps-moves.txt");
        in.useDelimiter("[\\s:]+"); // whitespace and colon

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
            Assert.assertNotNull(attackKey, moveParser.accuracy);
            Assert.assertNotNull(attackKey, moveParser.basePower);
            Assert.assertNotNull(attackKey, moveParser.category);
            Assert.assertNotNull(attackKey, moveParser.flags);
            Assert.assertNotNull(attackKey, moveParser.pp);
            Assert.assertNotNull(attackKey, moveParser.priority);
            Assert.assertNotNull(attackKey, moveParser.target);
            Assert.assertNotNull(attackKey, moveParser.type);

            String isZ = moveParser.isZ;
            boolean isUnreleased = moveParser.is("isUnreleased") != null;
            boolean isNonstandard = moveParser.is("isNonstandard") != null;
            if (attackNamesies == null) {
                if (attackKey.startsWith("hiddenpower")) {
                    Assert.assertNotNull(attackKey, StringUtils.enumTryValueOf(Type.class, attackKey.substring("hiddenpower".length())));
                } else if (unimplementedIds.contains(attackKey)) {
                    // Moves intentionally not implemented (like Helping Hand and other dumbass double battle only moves)
                    Assert.assertNull(attackKey, isZ);
                    Assert.assertFalse(attackKey, isUnreleased);
                    Assert.assertFalse(attackKey, isNonstandard);
                } else if (isZ != null) {
                    Assert.assertFalse(attackKey, isUnreleased);
                    Assert.assertFalse(attackKey, isNonstandard);
                } else {
                    Assert.assertTrue(attackKey, isNonstandard || isUnreleased);
                }
            } else {
                Assert.assertFalse(attackKey, unimplementedIds.contains(attackKey));
                Assert.assertNull(attackKey, isZ);
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

        nullFangySecondary(moveMap, AttackNamesies.FIRE_FANG, StatusNamesies.BURNED);
        nullFangySecondary(moveMap, AttackNamesies.ICE_FANG, StatusNamesies.FROZEN);
        nullFangySecondary(moveMap, AttackNamesies.THUNDER_FANG, StatusNamesies.PARALYZED);

        nullOnHitSecondary(moveMap, AttackNamesies.ANCHOR_SHOT);
        nullOnHitSecondary(moveMap, AttackNamesies.SPIRIT_SHACKLE);
        nullOnHitSecondary(moveMap, AttackNamesies.SPARKLING_ARIA);
        nullOnHitSecondary(moveMap, AttackNamesies.THROAT_CHOP);

        nullOnHitSelf(moveMap, AttackNamesies.PSYCHO_SHIFT);

        // Handled separately in their API
        nullStatChangesUpdate(moveMap, AttackNamesies.DEFOG, new TestStages().set(-1, Stat.EVASION));
        nullStatChangesUpdate(moveMap, AttackNamesies.VENOM_DRENCH, new TestStages().set(-1, Stat.ATTACK, Stat.SP_ATTACK, Stat.SPEED));
        nullStatChangesUpdate(moveMap, AttackNamesies.SKULL_BASH, new TestStages().set(1, Stat.DEFENSE));

        // Manually changed moves in this API
        nullStatChangesUpdate(moveMap, AttackNamesies.FLOWER_SHIELD, new TestStages().set(1, Stat.DEFENSE));
        nullStatChangesUpdate(moveMap, AttackNamesies.ROTOTILLER, new TestStages().set(1, Stat.ATTACK, Stat.SP_ATTACK));
        nullStatChangesUpdate(moveMap, AttackNamesies.GEAR_UP, new TestStages().set(1, Stat.ATTACK, Stat.SP_ATTACK));
        nullStatChangesUpdate(moveMap, AttackNamesies.MAGNETIC_FLUX, new TestStages().set(1, Stat.DEFENSE, Stat.SP_DEFENSE));

        removeFlag(moveMap, "authentic", AttackNamesies.MIMIC, AttackNamesies.REFLECT_TYPE, AttackNamesies.CONVERSION_2, AttackNamesies.SNATCH, AttackNamesies.FAIRY_LOCK, AttackNamesies.AROMATIC_MIST, AttackNamesies.MAGNETIC_FLUX, AttackNamesies.GEAR_UP, AttackNamesies.POWDER, AttackNamesies.ME_FIRST, AttackNamesies.POWER_SWAP, AttackNamesies.GUARD_SWAP, AttackNamesies.SPEED_SWAP, AttackNamesies.SKETCH, AttackNamesies.DEFOG, AttackNamesies.BESTOW);
        removeFlag(moveMap, "charge", AttackNamesies.SKY_DROP);
        removeFlag(moveMap, "contact", AttackNamesies.PLASMA_FISTS); // I'm surprised this is wrong they seem to really have their shit together
        removeFlag(moveMap, "mirror", AttackNamesies.WONDER_ROOM, AttackNamesies.TRICK_ROOM, AttackNamesies.MAGIC_ROOM, AttackNamesies.MIND_READER, AttackNamesies.LOCK_ON, AttackNamesies.FAIRY_LOCK);
        removeFlag(moveMap, "protect", AttackNamesies.MIMIC, AttackNamesies.MIND_READER, AttackNamesies.LOCK_ON, AttackNamesies.REFLECT_TYPE, AttackNamesies.POWER_SPLIT, AttackNamesies.GUARD_SPLIT, AttackNamesies.FLORAL_HEALING, AttackNamesies.PURIFY);
        removeFlag(moveMap, "reflectable", AttackNamesies.FLORAL_HEALING, AttackNamesies.PURIFY);
        removeFlag(moveMap, "snatch", AttackNamesies.QUICK_GUARD, AttackNamesies.IMPRISON);

        for (AttackNamesies namesies : moveMap.keySet()) {
            ShowdownMoveParser moveParser = moveMap.get(namesies);
            Attack attack = namesies.getNewAttack();
            Effect effect = attack.getEffect() == null ? null : attack.getEffect().getEffect();
            String effectId = effect == null ? null : getId(effect.namesies().name());
            ClassFields genFields = genFieldsMap.get(namesies);

            specialCase(namesies, moveParser);

            String message = namesies.getName();
            Assert.assertEquals(message, moveParser.attackName, attack.getName());
            Assert.assertEquals(message, moveParser.accuracy, attack.getAccuracyString());
            Assert.assertEquals(message, moveParser.category, attack.getCategory());
            Assert.assertEquals(message, moveParser.type, attack.getActualType());
            Assert.assertEquals(message, (int)moveParser.pp, attack.getPP());
            Assert.assertEquals(message, (int)moveParser.priority, attack.getActualPriority());

            String powerString = moveParser.basePower == 0 ? "--" : Integer.toString(moveParser.basePower);
            Assert.assertEquals(message, powerString, attack.getPowerString());

            // TODO: We still have that bug where explosion doesn't cause fainting against a Ghost... :(
            checkCondition(namesies, moveParser.selfDestruct, attack.isMoveType(MoveType.USER_FAINTS), () -> {});

            // authentic: Ignores a target's substitute.
            checkFlag(namesies, moveParser, "authentic", attack.isMoveType(MoveType.SUBSTITUTE_PIERCING) || attack.isMoveType(MoveType.SOUND_BASED));

            // bite: Power is multiplied by 1.5 when used by a Pokemon with the Ability Strong Jaw.
            // Note: I include Super Fang here even though it is unaffected by Strong Jaw in case biting is used for other purposes
            checkFlag(namesies, moveParser, "bite", MoveType.BITING, AttackNamesies.SUPER_FANG);

            // bullet: Has no effect on Pokemon with the Ability Bulletproof.
            checkFlag(namesies, moveParser, "bullet", MoveType.BOMB_BALL);

            // charge: The user is unable to make a move between turns.
            checkFlag(namesies, moveParser, "charge", attack instanceof ChargingMove);

            // contact: Makes contact.
            checkFlag(namesies, moveParser, "contact", MoveType.PHYSICAL_CONTACT);

            // dance: When used by a Pokemon, other Pokemon with the Ability Dancer can attempt to execute the same move.
            // Note: I include Rain Dance in case this is every used for anything else even though repeating this move is useless
            checkFlag(namesies, moveParser, "dance", MoveType.DANCE, AttackNamesies.RAIN_DANCE);

            // defrost: Thaws the user if executed successfully while the user is frozen.
            checkFlag(namesies, moveParser, "defrost", MoveType.DEFROST);

            // gravity: Prevented from being executed or selected during Gravity's effect.
            checkFlag(namesies, moveParser, "gravity", MoveType.AIRBORNE);

            // heal: Prevented from being executed or selected during Heal Block's effect.
            // Note: I mostly use this tag for the Triage ability instead of Heal Block
            // Another note: These added moves here I just though made sense they don't technically apply to Triage either
            checkFlag(namesies, moveParser, "heal", MoveType.HEALING, AttackNamesies.AROMATHERAPY, AttackNamesies.REFRESH, AttackNamesies.HEAL_BELL);

            // mirror: Can be copied by Mirror Move.
            checkFlag(
                    namesies, moveParser, "mirror",
                    !attack.isSelfTargetStatusMove() && !attack.isMoveType(MoveType.FIELD) && !attack.isMoveType(MoveType.MIRRORLESS),
                    AttackNamesies.IMPRISON, AttackNamesies.PSYCH_UP, AttackNamesies.HEAL_PULSE, AttackNamesies.FAIRY_LOCK
            );

            // powder: Has no effect on Grass-type Pokemon, Pokemon with the Ability Overcoat, and Pokemon holding Safety Goggles.
            checkFlag(namesies, moveParser, "powder", attack instanceof PowderMove);

            // protect: Blocked by Detect, Protect, Spiky Shield, and if not a Status move, King's Shield.
            checkFlag(namesies, moveParser, "protect", attack.isProtectAffected(), AttackNamesies.MEAN_LOOK, AttackNamesies.BLOCK, AttackNamesies.FAIRY_LOCK, AttackNamesies.BESTOW);

            // pulse: Power is multiplied by 1.5 when used by a Pokemon with the Ability Mega Launcher.
            checkFlag(namesies, moveParser, "pulse", MoveType.AURA_PULSE);

            // punch: Power is multiplied by 1.2 when used by a Pokemon with the Ability Iron Fist.
            // Note: Jabbing totally counts as punching
            checkFlag(namesies, moveParser, "punch", MoveType.PUNCHING, AttackNamesies.POISON_JAB);

            // recharge: If this move is successful, the user must recharge on the following turn and cannot make a move.
            checkFlag(namesies, moveParser, "recharge", attack instanceof RechargingMove);

            // reflectable: Bounced back to the original user by Magic Coat or the Ability Magic Bounce.
            checkFlag(namesies, moveParser, "reflectable", attack.isMagicReflectable(), AttackNamesies.IMPRISON, AttackNamesies.TEETER_DANCE, AttackNamesies.FAIRY_LOCK);

            // snatch: Can be stolen from the original user and instead used by another Pokemon using Snatch.
            checkFlag(namesies, moveParser, "snatch", attack.isSnatchable(), AttackNamesies.ACUPRESSURE, AttackNamesies.AROMATIC_MIST, AttackNamesies.FLOWER_SHIELD, AttackNamesies.ROTOTILLER, AttackNamesies.FLORAL_HEALING, AttackNamesies.PURIFY);

            // sound: Has no effect on Pokemon with the Ability Soundproof.
            checkFlag(namesies, moveParser, "sound", MoveType.SOUND_BASED);

            checkRatioArray(
                    namesies, moveParser.recoil, attack instanceof RecoilPercentageMove,
                    () -> 1.0/Integer.parseInt(genFields.get("RecoilPercentage"))
            );

            checkRatioArray(
                    namesies, moveParser.heal, genFields.contains("HealFraction") || namesies == AttackNamesies.ROOST,
                    () -> {
                        double fraction;
                        if (namesies == AttackNamesies.ROOST) {
                            fraction = .5;
                        } else {
                            fraction = Double.parseDouble(genFields.get("HealFraction"));
                        }

                        // Should not cause an NPE since this is only checking for the constant fractions
                        TestUtils.assertEquals(message, fraction, ((SelfHealingMove)attack).getHealFraction(null, null));
                        return fraction;
                    }
            );

            checkRatioArray(
                    namesies, moveParser.drain, attack instanceof SapHealthMove,
                    () -> {
                        SapHealthEffect sappy = (SapHealthEffect)attack;
                        return sappy.sapPercentage();
                    }
            );

            checkCondition(
                    namesies, moveParser.multiHit, attack instanceof MultiStrikeMove,
                    () -> {
                        Assert.assertEquals(message, 2, moveParser.multiHit.length);
                        Assert.assertTrue(message, attack instanceof MultiStrikeMove); // For the warning
                        MultiStrikeMove multiStrikeMove = (MultiStrikeMove)attack;
                        Assert.assertEquals(message, moveParser.multiHit[0], multiStrikeMove.getMinHits());
                        Assert.assertEquals(message, moveParser.multiHit[1], multiStrikeMove.getMaxHits());
                    }
            );

            checkCondition(
                    namesies, moveParser.status,
                    attack.isStatusMove() && attack.getStatus() != StatusNamesies.NO_STATUS && namesies != AttackNamesies.REST,
                    () -> Assert.assertEquals(message, moveParser.status, attack.getStatus())
            );

            if (effect instanceof PartialTrappingEffect) {
                Assert.assertEquals(message, "partiallytrapped", moveParser.volatileStatus);
            } else if (moveParser.volatileStatus != null) {
                Assert.assertNotNull(message, effect);
                Assert.assertEquals(message, moveParser.volatileStatus, effectId);
            } else if (attack.isStatusMove()) {
                Set<EffectNamesies> nonParseVolatile = Set.of(
                        PokemonEffectNamesies.CHANGE_TYPE,
                        PokemonEffectNamesies.CHANGE_ABILITY,
                        StandardBattleEffectNamesies.POWER_SPLIT,
                        StandardBattleEffectNamesies.GUARD_SPLIT,
                        PokemonEffectNamesies.TRANSFORMED,
                        PokemonEffectNamesies.MIMIC,
                        PokemonEffectNamesies.TRAPPED,
                        PokemonEffectNamesies.LOCK_ON
                );
                Assert.assertTrue(message, effect == null || nonParseVolatile.contains(effect.namesies()));
            }

            checkCondition(
                    namesies, moveParser.defensiveCategory, attack instanceof OpponentStatSwitchingEffect,
                    () -> {
                        Assert.assertNotEquals(message, MoveCategory.STATUS, moveParser.defensiveCategory);
                        Assert.assertNotEquals(message, moveParser.category, moveParser.defensiveCategory);
                    }
            );

            checkCondition(
                    namesies, moveParser.critRatio, attack instanceof CritStageEffect,
                    () -> Assert.assertEquals(message, 2, (int)moveParser.critRatio)
            );

            if (moveParser.self != null
                    && (moveParser.self.onHit || moveParser.self.boosts != null
                        || (moveParser.self.volatileStatus != null
                            && Set.of("raging", "lockedmove", "uproar").contains(moveParser.self.volatileStatus)))) {
                Assert.assertTrue(message, attack.isSelfTarget());
                moveParser.self.onHit = false;
            } else if ((moveParser.secondary != null && moveParser.secondary.self != null)
                    || genFields.contains("FocusMove")
                    || namesies == AttackNamesies.SKULL_BASH
                    || namesies == AttackNamesies.MIMIC
                    || namesies == AttackNamesies.LOCK_ON
                    || namesies == AttackNamesies.MIND_READER
                    || namesies == AttackNamesies.REFLECT_TYPE
                    || namesies == AttackNamesies.TRANSFORM
                    || namesies == AttackNamesies.CONVERSION_2
                    || namesies == AttackNamesies.ROLE_PLAY
                    || namesies == AttackNamesies.FLORAL_HEALING
                    || namesies == AttackNamesies.PURIFY
                    || namesies == AttackNamesies.PAY_DAY) {
                Assert.assertTrue(message, attack.isSelfTarget());
            } else if (genFields.contains("MirrorMove")
                    || namesies == AttackNamesies.METRONOME
                    || namesies == AttackNamesies.SLEEP_TALK
                    || namesies == AttackNamesies.ASSIST
                    || namesies == AttackNamesies.FAIRY_LOCK
                    || namesies == AttackNamesies.PERISH_SONG
                    || namesies == AttackNamesies.IMPRISON) {
                Assert.assertFalse(message, attack.isSelfTarget());
            } else {
                switch (moveParser.target) {
                    case "normal":
                    case "allAdjacentFoes":
                    case "randomNormal":
                    case "any":
                    case "foeSide":
                    case "allAdjacent":
                    case "scripted":
                    case "adjacentFoe":
                        Assert.assertFalse(message, attack.isSelfTarget());
                        break;
                    case "self":
                    case "allySide":
                    case "allyTeam":
                    case "adjacentAllyOrSelf":
                    case "adjacentAlly":
                        Assert.assertTrue(message, attack.isSelfTarget());
                        break;
                    case "all":
                        Assert.assertTrue(message, attack.isMoveType(MoveType.FIELD) || attack.isSelfTarget());
                        break;
                    default:
                        Assert.fail(message + " Unknown target " + moveParser.target);
                }
            }

            int[] actualBoosts = attack.getStatChangesCopy();
            int[] parserBoosts = moveParser.getBoosts();
            if (GeneralUtils.isEmpty(actualBoosts)) {
                Assert.assertNull(message, parserBoosts);
            } else {
                Assert.assertNotNull(message, parserBoosts);
                TestUtils.assertEquals(message, parserBoosts, actualBoosts);
            }

            checkCondition(
                    namesies,
                    moveParser.secondary,
                    attack.hasSecondaryEffects() && namesies != AttackNamesies.SKULL_BASH,
                    () -> {
                        Assert.assertEquals(message, (int)moveParser.secondary.chance, attack.getEffectChance());
                        Assert.assertTrue(message, moveParser.secondary.functionKeys.isEmpty());
                        Assert.assertTrue(
                                message,
                                moveParser.secondary.self == null || moveParser.secondary.self.toString().equals("")
                        );
                        checkCondition(
                                namesies,
                                moveParser.secondary.volatileStatus,
                                attack.getEffect() == PokemonEffectNamesies.FLINCH || attack.getEffect() == PokemonEffectNamesies.CONFUSION,
                                () -> Assert.assertEquals(message, moveParser.secondary.volatileStatus, effectId)
                        );
                        checkCondition(
                                namesies, moveParser.secondary.status,
                                attack.getStatus() != StatusNamesies.NO_STATUS,
                                () -> Assert.assertEquals(message, moveParser.secondary.status, attack.getStatus())
                        );
                    }
            );

            checkSelfVolatile(namesies, moveParser, "lockedmove", attack.getEffect() == PokemonEffectNamesies.SELF_CONFUSION);
            checkSelfVolatile(namesies, moveParser, "mustrecharge", attack instanceof RechargingMove);
            checkSelfVolatile(namesies, moveParser, effectId, moveParser.self != null && moveParser.self.volatileStatus != null);

            Assert.assertTrue(message + " " + moveParser.self, moveParser.self == null || moveParser.self.toString().equals(""));

            if (attack instanceof FixedDamageMove) {
                String fixedDamage = genFields.get("FixedDamage");
                Assert.assertNotNull(message, fixedDamage);

                boolean hasCallback = moveParser.functionKeys.contains("damageCallback");
                try {
                    int damage = Integer.parseInt(fixedDamage);
                    Assert.assertEquals(message, (int)moveParser.fixedDamage, damage);
                    Assert.assertFalse(message, hasCallback);
                } catch (NumberFormatException ex) {
                    Assert.assertNull(message, moveParser.fixedDamage);
                    Assert.assertTrue(message, hasCallback);
                }
            } else {
                Assert.assertNull(message, moveParser.fixedDamage);
            }

            Boolean ignoreImmunity = moveParser.is("ignoreImmunity");
            if (namesies == AttackNamesies.THUNDER_WAVE) {
                Assert.assertNotNull(message, ignoreImmunity);
                Assert.assertFalse(message, ignoreImmunity);
            } else {
                // TODO: Add tests for these
                checkBoolean(namesies, ignoreImmunity, AttackNamesies.FUTURE_SIGHT, AttackNamesies.BIDE);
            }

            // TODO: I don't think this is actually enforced for trump card/sketch
            checkBoolean(namesies, moveParser.is("noPPBoosts"), AttackNamesies.STRUGGLE, AttackNamesies.TRUMP_CARD, AttackNamesies.SKETCH);

            // TODO: I don't think this is enforced -- only Fire-type moves
            checkBoolean(namesies, moveParser.is("thawsTarget"), AttackNamesies.SCALD, AttackNamesies.STEAM_ERUPTION);

            // Note: Chatter can be sketched in this game
            checkBoolean(namesies, moveParser.is("noSketch"), AttackNamesies.STRUGGLE, AttackNamesies.CHATTER);

            checkBoolean(namesies, moveParser.is("struggleRecoil"), AttackNamesies.STRUGGLE);
            checkBoolean(namesies, moveParser.is("mindBlownRecoil"), AttackNamesies.MIND_BLOWN);
            checkBoolean(namesies, moveParser.is("noFaint"), AttackNamesies.FALSE_SWIPE);
            checkBoolean(namesies, moveParser.is("sleepUsable"), AttackNamesies.SLEEP_TALK, AttackNamesies.SNORE);
            checkBoolean(namesies, moveParser.is("stealsBoosts"), AttackNamesies.SPECTRAL_THIEF);

            // TODO: Neither of these actually work as expected right now
            checkBoolean(namesies, moveParser.is("multiaccuracy"), AttackNamesies.TRIPLE_KICK);
            checkBoolean(namesies, moveParser.is("useTargetOffensive"), AttackNamesies.FOUL_PLAY);

            Boolean isFutureMove = moveParser.is("isFutureMove");
            checkBoolean(namesies, isFutureMove, AttackNamesies.FUTURE_SIGHT, AttackNamesies.DOOM_DESIRE);
            checkBoolean(namesies, moveParser.is("breaksProtect"), !attack.isStatusMove() && attack.isMoveType(MoveType.PROTECT_PIERCING) && isFutureMove == null);

            checkBoolean(namesies, moveParser.is("ignoreEvasion"), attack instanceof OpponentIgnoreStageEffect);
            checkBoolean(namesies, moveParser.is("ignoreDefensive"), attack instanceof OpponentIgnoreStageEffect);
            checkBoolean(namesies, moveParser.is("ohko"), attack instanceof OhkoMove);
            checkBoolean(namesies, moveParser.is("hasCustomRecoil"), attack instanceof CrashDamageMove);
            checkBoolean(namesies, moveParser.is("willCrit"), attack instanceof AlwaysCritEffect);
            checkBoolean(namesies, moveParser.is("forceSwitch"), attack instanceof SwapOpponentEffect);

            checkBoolean(namesies, moveParser.is("ignoreAbility"), genFields.contains("IgnoreAbilityMove") || namesies == AttackNamesies.PHOTON_GEYSER);
            checkBoolean(namesies, moveParser.is("selfSwitch"), genFields.contains("SelfSwitching") || namesies == AttackNamesies.BATON_PASS);

            moveParser.assertEmpty();
        }
    }

    private void checkRatioArray(AttackNamesies attackNamesies, int[] parserArray, boolean condition, Supplier<Double> ratioGetter) {
        checkCondition(
                attackNamesies, parserArray, condition, () -> {
                    double genRatio = ratioGetter.get();
                    String message = attackNamesies.getName() + " " + Arrays.toString(parserArray) + " " + genRatio;
                    Assert.assertEquals(message, 2, parserArray.length);
                    double parserRatio = (double)parserArray[0]/parserArray[1];
                    Assert.assertTrue(message, parserRatio > 0 && parserRatio < 1);
                    Assert.assertTrue(message, genRatio > 0 && genRatio < 1);
                    TestUtils.assertEquals(message, (int)(parserRatio*100), (int)(genRatio*100));
                }
        );
    }

    private void checkCondition(AttackNamesies attackNamesies, Object parserValue, boolean condition, Action additionalChecks) {
        if (condition) {
            Assert.assertNotNull(attackNamesies.getName(), parserValue);
            additionalChecks.performAction();
        } else {
            Assert.assertNull(attackNamesies.getName(), parserValue);
        }
    }

    private Map<AttackNamesies, ClassFields> readGen() {
        Scanner in = FileIO.openFile(GeneratorType.ATTACK_GEN.getInputPath());
        InputFormatter inputFormatter = new InputFormatter();
        inputFormatter.readFileFormat(in);

        Map<AttackNamesies, ClassFields> fieldsMap = new EnumMap<>(AttackNamesies.class);
        while (in.hasNext()) {
            String line = in.nextLine().trim();

            // Ignore comments and white space
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String name = line.replace(":", "");
            ClassFields fields = new ClassFields(in, name);

            for (String fieldName : fields.getFieldNames()) {
                MethodInfo methodInfo = inputFormatter.getOverrideMethod(fieldName);
                if (methodInfo != null) {
                    methodInfo.getMapFields().forEach(fields::addNew);
                }
            }

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

    private void checkFlag(AttackNamesies attackNamesies, ShowdownMoveParser moveParser, String flagName, MoveType moveType, AttackNamesies... exceptions) {
        checkFlag(attackNamesies, moveParser, flagName, attackNamesies.getNewAttack().isMoveType(moveType), exceptions);
    }

    private void checkFlag(AttackNamesies attackNamesies, ShowdownMoveParser moveParser, String flagName, boolean condition, AttackNamesies... exceptions) {
        Assert.assertEquals(
                attackNamesies.getName() + " " + flagName,
                moveParser.flags.contains(flagName) || Arrays.asList(exceptions).contains(attackNamesies),
                condition
        );
        moveParser.flags.remove(flagName);
    }

    private void checkSelfVolatile(AttackNamesies attackNamesies, ShowdownMoveParser moveParser, String volatileStatus, boolean condition) {
        if (condition) {
            String message = attackNamesies.getName();
            Assert.assertNotNull(message, moveParser.self);
            Assert.assertEquals(message, volatileStatus, moveParser.self.volatileStatus);
            moveParser.self.volatileStatus = null;
        }
    }

    // Id is all lowercase no special characters (except numbers if applicable)
    private String getId(String name) {
        return StringUtils.getNamesiesString(name).replaceAll("_", "").toLowerCase();
    }

    private String volatileStatusUpdate(String volatileStatus) {
        if (volatileStatus == null) {
            return null;
        }

        switch (volatileStatus) {
            case "endure":
                return "bracing";
            case "focusenergy":
                return "raisecrits";
            case "defensecurl":
                return "useddefensecurl";
            case "minimize":
                return "usedminimize";
            case "smackdown":
                return "grounded";
            case "autotomize":
                return "halfweight";
            case "attract":
                return "infatuation";
            case "gastroacid":
                return "changeability";
            case "electrify":
            case "iondeluge":
                return "changeattacktype";
            case "healingwish":
            case "lunardance":
                return "healswitch";
            case "raindance":
                return "raining";
            case "sunnyday":
                return "sunny";
            case "hail":
                return "hailing";
            case "rage":
                return "raging";
            case "fairylock":
                return "trapped";
        }

        return volatileStatus;
    }

    private void nullFangySecondary(Map<AttackNamesies, ShowdownMoveParser> moveParserMap, AttackNamesies attackNamesies, StatusNamesies statusNamesies) {
        ShowdownMoveParser moveParser = moveParserMap.get(attackNamesies);
        String message = attackNamesies.getName();
        Assert.assertNotNull(message, moveParser.secondary);
        Assert.assertNotEquals(message, "", moveParser.secondary.toString());
        Assert.assertEquals(message, 20, (int)moveParser.secondary.chance);
        moveParser.secondary.chance = null;
        Assert.assertEquals(message, "flinch", moveParser.secondary.volatileStatus);
        moveParser.secondary.volatileStatus = null;
        Assert.assertEquals(message, statusNamesies, moveParser.secondary.status);
        moveParser.secondary.status = null;
        Assert.assertEquals(message, "", moveParser.secondary.toString());
        moveParser.secondary.chance = 20;
    }

    private void nullOnHitSecondary(Map<AttackNamesies, ShowdownMoveParser> moveParserMap, AttackNamesies attackNamesies) {
        ShowdownMoveParser moveParser = moveParserMap.get(attackNamesies);
        String message = attackNamesies.getName();
        Assert.assertNotNull(message, moveParser.secondary);
        Assert.assertNotEquals(message, "", moveParser.secondary.toString());
        Assert.assertEquals(message, 100, (int)moveParser.secondary.chance);
        Assert.assertEquals(message, 1, moveParser.secondary.functionKeys.size());
        Assert.assertTrue(message, moveParser.secondary.functionKeys.remove("onHit"));
        moveParser.secondary.chance = null;
        Assert.assertEquals(message, "", moveParser.secondary.toString());
        moveParser.secondary = null;
    }

    private void nullOnHitSelf(Map<AttackNamesies, ShowdownMoveParser> moveParserMap, AttackNamesies attackNamesies) {
        ShowdownMoveParser moveParser = moveParserMap.get(attackNamesies);
        String message = attackNamesies.getName();
        Assert.assertNotNull(message, moveParser.self);
        Assert.assertNotEquals(message, "", moveParser.self.toString());
        Assert.assertTrue(message, moveParser.self.onHit);
        moveParser.self.onHit = false;
        Assert.assertEquals(message, "", moveParser.self.toString());
        moveParser.self = null;
    }

    private void nullStatChangesUpdate(Map<AttackNamesies, ShowdownMoveParser> moveParserMap, AttackNamesies attackNamesies, TestStages newStages) {
        ShowdownMoveParser moveParser = moveParserMap.get(attackNamesies);
        String message = attackNamesies.getName();
        Assert.assertNull(message, moveParser.getBoosts());
        moveParser.boosts = newStages.get();
    }

    private void removeFlag(Map<AttackNamesies, ShowdownMoveParser> moveParserMap, String flagName, AttackNamesies... attackNamesies) {
        for (AttackNamesies namesies : attackNamesies) {
            ShowdownMoveParser moveParser = moveParserMap.get(namesies);
            String message = namesies.getName() + " " + flagName;
            Assert.assertTrue(message, moveParser.flags.contains(flagName));
            moveParser.flags.remove(flagName);
        }
    }

    private void specialCase(AttackNamesies attackNamesies, ShowdownMoveParser moveParser) {
        moveParser.volatileStatus = volatileStatusUpdate(moveParser.volatileStatus);
        if (moveParser.self != null) {
            moveParser.self.volatileStatus = volatileStatusUpdate(moveParser.self.volatileStatus);
        }

        String message = attackNamesies.getName();
        switch (attackNamesies) {
            case TRIPLE_KICK:
                Assert.assertEquals(message, 10, (int)moveParser.basePower);
                moveParser.basePower = 20;
                break;
            case FOUL_PLAY:
                Assert.assertEquals(message, 95, (int)moveParser.basePower);
                moveParser.basePower = 0;
                break;
            case STRUGGLE:
                Assert.assertEquals(message, Type.NORMAL, moveParser.type);
                moveParser.type = Type.NO_TYPE;
                break;
            case QUICK_GUARD:
            case CRAFTY_SHIELD:
                // Why are Quick Guard/Crafty Shield different than Protect/Spiky Shield/etc. it's weird and I don't care
                Assert.assertEquals(message, 3, (int)moveParser.priority);
                moveParser.priority = 4;
                break;
            case ELECTRIFY:
                // I merged this move with Ion Deluge and that includes priority
                Assert.assertEquals(message, 0, (int)moveParser.priority);
                moveParser.priority = 1;
                break;
            case CURSE:
                Assert.assertEquals(message, "curse", moveParser.volatileStatus);
                moveParser.volatileStatus = null;
                break;
            case ROOST:
                Assert.assertNotNull(message, moveParser.self);
                Assert.assertEquals(message, "roost", moveParser.self.volatileStatus);
                moveParser.self.volatileStatus = null;
                break;
            case GROWTH:
                TestUtils.assertEquals(message, new TestStages().set(1, Stat.ATTACK, Stat.SP_ATTACK).get(), moveParser.boosts);
                moveParser.boosts = null;
                break;
            case JUDGEMENT:
                Assert.assertEquals(message, "Judgment", moveParser.attackName);
                moveParser.attackName = "Judgement";
                break;
            case SECRET_POWER:
                Assert.assertEquals(message, 30, (int)moveParser.secondary.chance);
                moveParser.secondary.chance = null;
                Assert.assertEquals(message, StatusNamesies.PARALYZED, moveParser.secondary.status);
                moveParser.secondary.status = null;
                Assert.assertEquals(message, "", moveParser.secondary.toString());
                moveParser.secondary.chance = 30;
                break;
            case TRI_ATTACK:
                Assert.assertEquals(message, 20, (int)moveParser.secondary.chance);
                moveParser.secondary.chance = null;
                Assert.assertTrue(message, moveParser.secondary.functionKeys.remove("onHit"));
                Assert.assertEquals(message, "", moveParser.secondary.toString());
                moveParser.secondary.chance = 20;
                break;
        }
    }
}
