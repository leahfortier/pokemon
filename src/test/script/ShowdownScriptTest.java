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
import generator.format.MethodWriter;
import generator.update.UpdateGen;
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

public class ShowdownScriptTest extends BaseTest {

    @Test
    public void showdownMoveParserTest() {
        Scanner in = FileIO.openFile(Folder.SCRIPTS_COMPARE + "ps-moves.txt");
        in.useDelimiter("[\\s:]+"); // whitespace and colon

        Assert.assertEquals("{", in.nextLine());

        Set<String> unimplementedIds = UpdateGen.unimplementedMoves
                .stream()
                .map(this::getId)
                .collect(Collectors.toSet());

        final Set<AttackNamesies> noMetronome = EnumSet.noneOf(AttackNamesies.class);
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

            checkCondition(
                    attackKey, moveParser.noMetronome, attackKey.equals("metronome"), false,
                    () -> {
                        Assert.assertTrue(attackKey, noMetronome.isEmpty());
                        for (String attackName : moveParser.noMetronome) {
                            AttackNamesies namesies = AttackNamesies.tryValueOf(attackName);
                            if (namesies != null) {
                                Assert.assertFalse(noMetronome.contains(namesies));
                                noMetronome.add(namesies);
                            }
                        }
                        Assert.assertFalse(attackKey, noMetronome.isEmpty());
                    }
            );

            boolean isZ = moveParser.isZ != null;
            boolean isMax = moveParser.is("isMax") != null;
            boolean isNonstandard = moveParser.is("isNonstandard") != null;
            if (attackNamesies == null) {
                if (attackKey.startsWith("hiddenpower")) {
                    Assert.assertNotNull(attackKey, StringUtils.enumTryValueOf(Type.class, attackKey.substring("hiddenpower".length())));
                } else if (unimplementedIds.contains(attackKey)) {
                    // Moves intentionally not implemented (like Helping Hand and other dumbass double battle only moves)
                    Assert.assertFalse(attackKey, isZ);
                    Assert.assertFalse(attackKey, isMax);
                    Assert.assertFalse(attackKey, isNonstandard);
                } else {
                    Assert.assertTrue(attackKey, isZ || isNonstandard || isMax);
                }
            } else {
                Assert.assertFalse(attackKey, unimplementedIds.contains(attackKey));
                Assert.assertFalse(attackKey, isZ);
                Assert.assertFalse(attackKey, isNonstandard);
                Assert.assertFalse(attackKey, isMax);
                moveMap.put(attackNamesies, moveParser);
            }
        }
        in.close();

        Assert.assertFalse(noMetronome.isEmpty());
        for (AttackNamesies namesies : noMetronome) {
            moveMap.get(namesies).addNoMetronome();
        }

        Set<AttackNamesies> allAttacks = EnumSet.complementOf(EnumSet.of(AttackNamesies.CONFUSION_DAMAGE, AttackNamesies.FAKE_FREEZER));
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
        nullStatChangesUpdate(moveMap, AttackNamesies.PARTING_SHOT, new TestStages().set(-1, Stat.ATTACK, Stat.SP_ATTACK));
        nullStatChangesUpdate(moveMap, AttackNamesies.SKULL_BASH, new TestStages().set(1, Stat.DEFENSE));
        nullStatChangesUpdate(moveMap, AttackNamesies.METEOR_BEAM, new TestStages().set(1, Stat.SP_ATTACK));
        nullStatChangesUpdate(moveMap, AttackNamesies.STUFF_CHEEKS, new TestStages().set(2, Stat.DEFENSE));

        // Manually changed moves in this API
        nullStatChangesUpdate(moveMap, AttackNamesies.FLOWER_SHIELD, new TestStages().set(1, Stat.DEFENSE));
        nullStatChangesUpdate(moveMap, AttackNamesies.ROTOTILLER, new TestStages().set(1, Stat.ATTACK, Stat.SP_ATTACK));
        nullStatChangesUpdate(moveMap, AttackNamesies.GEAR_UP, new TestStages().set(1, Stat.ATTACK, Stat.SP_ATTACK));
        nullStatChangesUpdate(moveMap, AttackNamesies.MAGNETIC_FLUX, new TestStages().set(1, Stat.DEFENSE, Stat.SP_DEFENSE));

        removeFlag(moveMap, "authentic", AttackNamesies.MIMIC, AttackNamesies.REFLECT_TYPE, AttackNamesies.CONVERSION_2, AttackNamesies.SNATCH, AttackNamesies.FAIRY_LOCK, AttackNamesies.AROMATIC_MIST, AttackNamesies.MAGNETIC_FLUX, AttackNamesies.GEAR_UP, AttackNamesies.POWDER, AttackNamesies.ME_FIRST, AttackNamesies.POWER_SWAP, AttackNamesies.GUARD_SWAP, AttackNamesies.SPEED_SWAP, AttackNamesies.SKETCH, AttackNamesies.DEFOG, AttackNamesies.BESTOW, AttackNamesies.TEATIME, AttackNamesies.LIFE_DEW, AttackNamesies.JUNGLE_HEALING, AttackNamesies.COACHING);
        removeFlag(moveMap, "charge", AttackNamesies.SKY_DROP);
        removeFlag(moveMap, "dance", AttackNamesies.CLANGOROUS_SOUL);
        removeFlag(moveMap, "mirror", AttackNamesies.WONDER_ROOM, AttackNamesies.TRICK_ROOM, AttackNamesies.MAGIC_ROOM, AttackNamesies.MIND_READER, AttackNamesies.LOCK_ON, AttackNamesies.COURT_CHANGE);
        removeFlag(moveMap, "protect", AttackNamesies.MIMIC, AttackNamesies.MIND_READER, AttackNamesies.LOCK_ON, AttackNamesies.REFLECT_TYPE, AttackNamesies.POWER_SPLIT, AttackNamesies.GUARD_SPLIT, AttackNamesies.FLORAL_HEALING, AttackNamesies.PURIFY);
        removeFlag(moveMap, "reflectable", AttackNamesies.FLORAL_HEALING, AttackNamesies.PURIFY);
        removeFlag(moveMap, "snatch", AttackNamesies.QUICK_GUARD, AttackNamesies.IMPRISON);
        removeFlag(moveMap, "noMetronome", AttackNamesies.CHATTER, AttackNamesies.BODY_PRESS, AttackNamesies.DECORATE, AttackNamesies.DRUM_BEATING, AttackNamesies.METEOR_ASSAULT, AttackNamesies.SNAP_TRAP, AttackNamesies.PYRO_BALL, AttackNamesies.BREAKING_SWIPE, AttackNamesies.BRANCH_POKE, AttackNamesies.OVERDRIVE, AttackNamesies.APPLE_ACID, AttackNamesies.GRAV_APPLE, AttackNamesies.SPIRIT_BREAK, AttackNamesies.STRANGE_STEAM, AttackNamesies.LIFE_DEW, AttackNamesies.FALSE_SURRENDER, AttackNamesies.STEEL_BEAM);

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
            // Note: Howl and Clangorous Soul are sound-based but also self-target so substitute is not as relevent
            checkFlag(namesies, moveParser, "authentic", attack.isMoveType(MoveType.SUBSTITUTE_PIERCING) || attack.isMoveType(MoveType.SOUND_BASED), AttackNamesies.HOWL, AttackNamesies.CLANGOROUS_SOUL);

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
            // TODO: Mirror Move works completely differently here
            checkFlag(
                    namesies, moveParser, "mirror",
                    !attack.isSelfTargetStatusMove() && !attack.isMoveType(MoveType.FIELD) && !attack.isMoveType(MoveType.MIRRORLESS),
                    AttackNamesies.IMPRISON, AttackNamesies.PSYCH_UP, AttackNamesies.HEAL_PULSE, AttackNamesies.GRASSY_GLIDE
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
            checkFlag(namesies, moveParser, "snatch", attack.isSnatchable(), AttackNamesies.ACUPRESSURE, AttackNamesies.AROMATIC_MIST, AttackNamesies.FLOWER_SHIELD, AttackNamesies.ROTOTILLER, AttackNamesies.FLORAL_HEALING, AttackNamesies.PURIFY, AttackNamesies.DECORATE, AttackNamesies.COACHING, AttackNamesies.JUNGLE_HEALING);

            // sound: Has no effect on Pokemon with the Ability Soundproof.
            checkFlag(namesies, moveParser, "sound", MoveType.SOUND_BASED);

            // noMetronome: Manually added flag from the noMetronome field in the Metronome attack.
            checkFlag(namesies, moveParser, "noMetronome", MoveType.METRONOMELESS, AttackNamesies.JUDGEMENT, AttackNamesies.MULTI_ATTACK);

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
                    attack.isStatusMove() && attack.getStatus() != StatusNamesies.NO_STATUS,
                    () -> Assert.assertEquals(message, moveParser.status, attack.getStatus()),
                    AttackNamesies.REST
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
                        StandardBattleEffectNamesies.CORROSIVE_GAS,
                        PokemonEffectNamesies.TRANSFORMED,
                        PokemonEffectNamesies.MIMIC,
                        PokemonEffectNamesies.TRAPPED,
                        PokemonEffectNamesies.LOCK_ON,
                        PokemonEffectNamesies.HALF_WEIGHT
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
                    || namesies == AttackNamesies.METEOR_BEAM
                    || namesies == AttackNamesies.MIMIC
                    || namesies == AttackNamesies.LOCK_ON
                    || namesies == AttackNamesies.MIND_READER
                    || namesies == AttackNamesies.REFLECT_TYPE
                    || namesies == AttackNamesies.TRANSFORM
                    || namesies == AttackNamesies.CONVERSION_2
                    || namesies == AttackNamesies.ROLE_PLAY
                    || namesies == AttackNamesies.FLORAL_HEALING
                    || namesies == AttackNamesies.PURIFY
                    || namesies == AttackNamesies.DECORATE
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
                    case "allies":
                        Assert.assertTrue(message, attack.isSelfTarget());
                        break;
                    case "all":
                        Assert.assertTrue(message, attack.isMoveType(MoveType.FIELD) || attack.isSelfTarget());
                        break;
                    default:
                        Assert.fail(message + " Unknown target " + moveParser.target);
                }
            }

            int[] actualBoosts = attack.getStageModifiers();
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
                    attack.hasSecondaryEffects(),
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
                    },
                    AttackNamesies.SKULL_BASH, AttackNamesies.SCALE_SHOT, AttackNamesies.METEOR_BEAM
            );

            checkSelfVolatile(namesies, moveParser, "lockedmove", attack.getEffect() == PokemonEffectNamesies.SELF_CONFUSION);
            checkSelfVolatile(namesies, moveParser, "mustrecharge", attack instanceof RechargingMove);
            checkSelfVolatile(namesies, moveParser, effectId, moveParser.self != null && moveParser.self.volatileStatus != null);

            Assert.assertTrue(message + " " + moveParser.self, moveParser.self == null || moveParser.self.toString().equals(""));

            if (attack instanceof FixedDamageMove && namesies != AttackNamesies.BIDE) {
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
            checkBoolean(namesies, moveParser.is("thawsTarget"), AttackNamesies.SCALD, AttackNamesies.STEAM_ERUPTION, AttackNamesies.SCORCHING_SANDS);

            // Snipe Shot was changed to ignore abilities in this game
            checkBoolean(namesies, moveParser.is("tracksTarget"), AttackNamesies.SNIPE_SHOT);
            checkBoolean(namesies, moveParser.is("smartTarget"), AttackNamesies.DRAGON_DARTS);

            // Note: Chatter can be sketched in this game
            checkBoolean(namesies, moveParser.is("noSketch"), AttackNamesies.STRUGGLE, AttackNamesies.CHATTER);

            checkBoolean(namesies, moveParser.is("struggleRecoil"), AttackNamesies.STRUGGLE);
            checkBoolean(namesies, moveParser.is("noFaint"), AttackNamesies.FALSE_SWIPE);
            checkBoolean(namesies, moveParser.is("sleepUsable"), AttackNamesies.SLEEP_TALK, AttackNamesies.SNORE);
            checkBoolean(namesies, moveParser.is("stealsBoosts"), AttackNamesies.SPECTRAL_THIEF);

            checkBoolean(namesies, moveParser.is("useTargetOffensive"), AttackNamesies.FOUL_PLAY);
            checkBoolean(namesies, moveParser.is("useSourceDefensiveAsOffensive"), AttackNamesies.BODY_PRESS);

            Boolean isFutureMove = moveParser.is("isFutureMove");
            checkBoolean(namesies, isFutureMove, AttackNamesies.FUTURE_SIGHT, AttackNamesies.DOOM_DESIRE);
            checkBoolean(namesies, moveParser.is("breaksProtect"), !attack.isStatusMove() && attack.isMoveType(MoveType.PROTECT_PIERCING) && isFutureMove == null);

            checkBoolean(namesies, moveParser.is("ignoreEvasion"), attack instanceof OpponentIgnoreStageEffect);
            checkBoolean(namesies, moveParser.is("ignoreDefensive"), attack instanceof OpponentIgnoreStageEffect);
            checkBoolean(namesies, moveParser.is("ohko"), attack instanceof OhkoMove);
            checkBoolean(namesies, moveParser.is("hasCrashDamage"), attack instanceof CrashDamageMove);
            checkBoolean(namesies, moveParser.is("willCrit"), attack instanceof AlwaysCritEffect);
            checkBoolean(namesies, moveParser.is("forceSwitch"), attack instanceof SwapOpponentEffect);

            checkBoolean(namesies, moveParser.is("multiaccuracy"), genFields.contains("TripleHit"));
            checkBoolean(namesies, moveParser.is("ignoreAbility"), (genFields.contains("IgnoreAbilityMove") && namesies != AttackNamesies.SNIPE_SHOT) || namesies == AttackNamesies.PHOTON_GEYSER);
            checkBoolean(namesies, moveParser.is("selfSwitch"), genFields.contains("SelfSwitching") || namesies == AttackNamesies.BATON_PASS || namesies == AttackNamesies.TELEPORT);
            checkBoolean(namesies, moveParser.is("mindBlownRecoil"), genFields.contains("SelfRecoil"));

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

    private void checkCondition(AttackNamesies attackNamesies, Object parserValue, boolean condition, Action additionalChecks, AttackNamesies... exceptions) {
        String message = attackNamesies.getName();
        boolean isException = Arrays.asList(exceptions).contains(attackNamesies);
        checkCondition(message, parserValue, condition, isException, additionalChecks);
    }

    private void checkCondition(String message, Object parserValue, boolean condition, boolean isException, Action additionalChecks) {
        Assert.assertFalse(message, !condition && isException);

        if (condition && !isException) {
            Assert.assertNotNull(message, parserValue);
            additionalChecks.performAction();
        } else {
            Assert.assertNull(message, parserValue);
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
                MethodWriter methodWriter = inputFormatter.getOverrideMethod(fieldName);
                if (methodWriter != null) {
                    methodWriter.getMapFields().forEach(fields::addNew);
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
        String message = attackNamesies.getName() + " " + flagName;
        boolean hasFlag = moveParser.flags.remove(flagName);
        boolean isException = Arrays.asList(exceptions).contains(attackNamesies);

        Assert.assertFalse(message, hasFlag && isException);
        Assert.assertEquals(message, hasFlag || isException, condition);
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
            case "tarshot":
                return "stickytar";
            case "octolock":
                return "octolocked";
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

    private void updateBasePower(String message, ShowdownMoveParser moveParser, int showdownPower, int actualPower) {
        Assert.assertEquals(message, showdownPower, (int)moveParser.basePower);
        moveParser.basePower = actualPower;
    }

    // There's a few moves on showdown that I can't figure out why the PP is different than
    // on serebii and bulbapedia but fixing that here
    private void updatePP(String message, ShowdownMoveParser moveParser, int showdownPP, int actualPP) {
        Assert.assertEquals(message, showdownPP, (int)moveParser.pp);
        moveParser.pp = actualPP;
    }

    private void nullSecondary(String message, ShowdownMoveParser moveParser, int secondaryChance, Action nullAction) {
        Assert.assertEquals(message, secondaryChance, (int)moveParser.secondary.chance);
        moveParser.secondary.chance = null;
        Assert.assertNotEquals(message, "", moveParser.secondary.toString());
        nullAction.performAction();
        Assert.assertEquals(message, "", moveParser.secondary.toString());
        moveParser.secondary.chance = secondaryChance;
    }

    private void nullOnHitSecondary(String message, ShowdownMoveParser moveParser, int secondaryChance) {
        nullSecondary(message, moveParser, secondaryChance, () -> {
            Assert.assertTrue(message, moveParser.secondary.functionKeys.remove("onHit"));
        });
    }

    private void specialCase(AttackNamesies attackNamesies, ShowdownMoveParser moveParser) {
        moveParser.volatileStatus = volatileStatusUpdate(moveParser.volatileStatus);
        if (moveParser.self != null) {
            moveParser.self.volatileStatus = volatileStatusUpdate(moveParser.self.volatileStatus);
        }

        String message = attackNamesies.getName();
        switch (attackNamesies) {
            case TRIPLE_KICK:
                updateBasePower(message, moveParser, 10, 20);
                break;
            case TRIPLE_AXEL:
                updateBasePower(message, moveParser, 20, 40);
                break;
            case FOUL_PLAY:
                updateBasePower(message, moveParser, 95, 0);
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
                nullSecondary(message, moveParser, 30, () -> {
                    Assert.assertEquals(message, StatusNamesies.PARALYZED, moveParser.secondary.status);
                    moveParser.secondary.status = null;
                });
                break;
            case TRI_ATTACK:
                nullOnHitSecondary(message, moveParser, 20);
                break;
            case BURNING_JEALOUSY:
                nullOnHitSecondary(message, moveParser, 100);
                moveParser.secondary.status = StatusNamesies.BURNED;
                break;
            case ETERNABEAM:
                updatePP(message, moveParser, 10, 5);
                break;
            case JAW_LOCK:
                updatePP(message, moveParser, 15, 10);
                break;
            case TAR_SHOT:
                updatePP(message, moveParser, 20, 15);
                break;
            case SPIRIT_BREAK:
                updatePP(message, moveParser, 10, 15);
                break;
            case LIFE_DEW:
                TestUtils.assertEquals(message, new int[] { 1, 4 }, moveParser.heal);
                moveParser.heal = new int[] { 1, 2 };
                break;
            case COURT_CHANGE:
                Assert.assertEquals(message, "100", moveParser.accuracy);
                moveParser.accuracy = "--";
                break;
        }
    }
}
