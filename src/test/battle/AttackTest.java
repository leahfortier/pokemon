package test.battle;

import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.attack.MoveCategory;
import battle.attack.MoveType;
import battle.effect.EffectInterfaces.SapHealthEffect;
import battle.effect.EffectNamesies;
import battle.effect.InvokeInterfaces.AlwaysCritEffect;
import battle.effect.InvokeInterfaces.ApplyDamageEffect;
import battle.effect.InvokeInterfaces.CritBlockerEffect;
import battle.effect.InvokeInterfaces.CritStageEffect;
import battle.effect.InvokeInterfaces.MurderEffect;
import battle.effect.InvokeInterfaces.PowerChangeEffect;
import battle.effect.attack.SelfHealingMove;
import battle.effect.battle.BattleEffect;
import battle.effect.battle.StandardBattleEffectNamesies;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.status.StatusNamesies;
import battle.effect.team.TeamEffectNamesies;
import battle.stages.Stages;
import item.Item;
import item.ItemNamesies;
import item.berry.Berry;
import item.berry.GainableEffectBerry;
import org.junit.Assert;
import org.junit.Test;
import pokemon.ability.AbilityNamesies;
import pokemon.active.Gender;
import pokemon.active.IndividualValues;
import pokemon.species.PokemonNamesies;
import pokemon.stat.Stat;
import test.general.BaseTest;
import test.general.TestUtils;
import test.pokemon.TestPokemon;
import type.PokeType;
import type.Type;
import util.GeneralUtils;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AttackTest extends BaseTest {
    @Test
    public void descriptionTest() {
        for (AttackNamesies attackNamesies : AttackNamesies.values()) {
            if (attackNamesies == AttackNamesies.CONFUSION_DAMAGE) {
                continue;
            }

            // Make sure all descriptions start capitalized, end with a period, and only contain valid characters
            Attack attack = attackNamesies.getNewAttack();
            String description = attack.getDescription();
            TestUtils.assertDescription(attack.getName(), description, "[A-Z][a-zA-Z0-9.,'é\\- ]+[.]");
        }
    }

    @Test
    public void moveTypeTest() {
        for (AttackNamesies attackNamesies : AttackNamesies.values()) {
            Attack attack = attackNamesies.getNewAttack();

            // Physical contact moves cannot be status moves
            Assert.assertFalse(attack.getName(), attack.isMoveType(MoveType.PHYSICAL_CONTACT) && attack.isStatusMove());

            // Field moves must be status moves and cannot be self-target
            Assert.assertFalse(attack.getName(), attack.isMoveType(MoveType.FIELD) && (!attack.isStatusMove() || attack.isSelfTarget()));

            // Snatch only affects self-target status moves
            Assert.assertFalse(attack.getName(), attack.isMoveType(MoveType.NON_SNATCHABLE) && !attack.isSelfTargetStatusMove());

            // Magic Coat only affects non-self target status moves
            Assert.assertFalse(attack.getName(), attack.isMoveType(MoveType.NO_MAGIC_COAT) && !(!attack.isSelfTarget() && attack.isStatusMove()));

            // Protect and Mirror Move do not apply to self-target status moves or field moves
            Assert.assertFalse(attack.getName(), attack.isMoveType(MoveType.PROTECT_PIERCING) && (attack.isSelfTargetStatusMove() || attack.isMoveType(MoveType.FIELD)));
            Assert.assertFalse(attack.getName(), attack.isMoveType(MoveType.MIRRORLESS) && (attack.isSelfTargetStatusMove() || attack.isMoveType(MoveType.FIELD)));

            // All SelfHealingMoves and SapHealthEffects should be Healing move type
            if (attack instanceof SelfHealingMove || attack instanceof SapHealthEffect) {
                Assert.assertTrue(attack.getName(), attack.isMoveType(MoveType.HEALING));
            }

            // DANCE DANCE REVOLUTION
            if (attack.getName().contains("Dance")) {
                Assert.assertTrue(attack.getName(), attack.isMoveType(MoveType.DANCE));
            }

            // Moves that change power, apply damage, murder, and crit cannot be status moves
            if (attack instanceof PowerChangeEffect
                    || attack instanceof ApplyDamageEffect
                    || attack instanceof MurderEffect
                    || attack instanceof AlwaysCritEffect
                    || attack instanceof CritStageEffect
                    || attack instanceof CritBlockerEffect) {
                Assert.assertFalse(attack.isStatusMove());
            }

            // Status moves that cast battle effects are field moves
            EffectNamesies effect = attack.getEffect();
            if (effect != null && effect.getEffect() instanceof BattleEffect && attack.isStatusMove()) {
                Assert.assertTrue(attack.getName(), attack.isMoveType(MoveType.NO_MAGIC_COAT));
                Assert.assertTrue(attack.getName(), attack.isMoveType(MoveType.FIELD));
            }

            // Status moves must apply their effects 100% of the time
            if (attack.isStatusMove()) {
                Assert.assertEquals(attack.getName(), 100, attack.getEffectChance());
            }

            // Moves that change category must have the category they change back to
            if (attackNamesies == AttackNamesies.PHOTON_GEYSER) {
                Assert.assertEquals(MoveCategory.SPECIAL, attack.getCategory());
            }
        }
    }

    @Test
    public void baseAccuracyTest() {
        for (AttackNamesies attackNamesies : AttackNamesies.values()) {
            Attack attack = attackNamesies.getNewAttack();

            // If the accuracy string is "--", then the move should ALWAYS hit
            if (attack.getAccuracyString().equals("--")) {
                // Super perfect always hit moves -- test with -6 accuracy and +6 evasion, move should still hit
                TestBattle battle = TestBattle.create();
                TestPokemon attacking = battle.getAttacking();
                TestPokemon defending = battle.getDefending();

                attacking.getStages().setStage(Stat.ACCURACY, -Stages.MAX_STAT_CHANGES);
                defending.getStages().setStage(Stat.EVASION, Stages.MAX_STAT_CHANGES);

                attacking.setupMove(attackNamesies, battle);

                int moveAccuracy = attacking.getAttack().getAccuracy(battle, attacking, defending);
                int accuracy = Stat.getStat(Stat.ACCURACY, attacking, defending, battle);
                int evasion = Stat.getStat(Stat.EVASION, defending, attacking, battle);

                int totalAccuracy = (int)(moveAccuracy*((double)accuracy/(double)evasion));
                Assert.assertTrue(attack.getName(), accuracy < 100);
                Assert.assertTrue(attack.getName(), evasion > 100);
                Assert.assertTrue(attack.getName(), totalAccuracy > 100);
            } else {
                // If not "--", then should be an integer and this should not throw a NumberFormatException
                Integer.parseInt(attack.getAccuracyString());

                // Self-target status moves and field moves should always have "--" accuracy string
                Assert.assertFalse(attack.getName(), attack.isSelfTargetStatusMove());
                Assert.assertFalse(attack.getName(), attack.isMoveType(MoveType.FIELD));
            }
        }
    }

    @Test
    public void recoilTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking().withAbility(AbilityNamesies.ROCK_HEAD);
        TestPokemon defending = battle.getDefending();

        battle.attackingFight(AttackNamesies.TAKE_DOWN);
        attacking.assertFullHealth();
        defending.assertNotFullHealth();

        defending.fullyHeal();
        defending.assertFullHealth();

        battle.defendingFight(AttackNamesies.WOOD_HAMMER);
        attacking.assertNotFullHealth();
        defending.assertNotFullHealth();

        int damage = attacking.getMaxHP() - attacking.getHP();
        Assert.assertEquals(defending.getMaxHP() - defending.getHP(), (int)(Math.ceil(damage/3.0)));

        attacking.fullyHeal();
        defending.fullyHeal();
        defending.withAbility(AbilityNamesies.MAGIC_GUARD);
        battle.defendingFight(AttackNamesies.WOOD_HAMMER);
        defending.assertFullHealth();

        // Struggle should still cause recoil damage even if they have Rock Head/Magic Guard
        attacking.fullyHeal();
        battle.attackingFight(AttackNamesies.STRUGGLE);
        attacking.assertAbility(AbilityNamesies.ROCK_HEAD);
        attacking.assertHealthRatio(.75);
    }

    @Test
    public void captivateTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking().withGender(Gender.MALE);
        TestPokemon defending = battle.getDefending().withGender(Gender.MALE);

        // TODO: Test genderless too
        attacking.apply(false, AttackNamesies.CAPTIVATE, battle);

        defending.withGender(Gender.FEMALE);
        attacking.apply(true, AttackNamesies.CAPTIVATE, battle);

        attacking.withAbility(AbilityNamesies.OBLIVIOUS);
        attacking.apply(true, AttackNamesies.CAPTIVATE, battle);

        attacking.withAbility(AbilityNamesies.NO_ABILITY);
        defending.withAbility(AbilityNamesies.OBLIVIOUS);
        attacking.apply(false, AttackNamesies.CAPTIVATE, battle);
    }

    @Test
    public void ohkoTest() {
        // TODO: Don't think this is testing that these fail for lower level -- specifically check that with Sheer Cold
        TestBattle battle = TestBattle.create(PokemonNamesies.MAGIKARP, PokemonNamesies.DRAGONITE);
        TestPokemon defending = battle.getDefending();

        // Ground type should not effect Flying type
        battle.attackingFight(AttackNamesies.FISSURE);
        defending.assertFullHealth();

        // OHKO,MF
        battle.attackingFight(AttackNamesies.HORN_DRILL);
        Assert.assertTrue(defending.isFainted(battle));

        // Sturdy prevents OHKO
        defending.fullyHeal();
        defending.withAbility(AbilityNamesies.STURDY);
        battle.attackingFight(AttackNamesies.SHEER_COLD);
        defending.assertFullHealth();

        defending.withAbility(AbilityNamesies.NO_ABILITY);
        battle.attackingFight(AttackNamesies.SHEER_COLD);
        Assert.assertTrue(defending.isFainted(battle));

        // Sheer Cold doesn't work against Ice types
        battle.emptyHeal();
        defending.withAbility(AbilityNamesies.PROTEAN);
        battle.defendingFight(AttackNamesies.HAZE);
        defending.assertType(battle, Type.ICE);
        battle.attackingFight(AttackNamesies.SHEER_COLD);
        defending.assertFullHealth();
    }

    @Test
    public void selfSwitchingMoves() {
        TestBattle battle = TestBattle.createTrainerBattle(PokemonNamesies.CHANSEY, PokemonNamesies.SHUCKLE);
        TestPokemon attacking1 = battle.getAttacking();
        TestPokemon attacking2 = battle.addAttacking(PokemonNamesies.HAPPINY);
        battle.assertFront(attacking1);

        // Use U-Turn -- make sure they swap
        battle.attackingFight(AttackNamesies.U_TURN);
        battle.assertFront(attacking2);

        // TODO: Baton Pass
        // TODO: No more remaining Pokemon, wild battles, wimp out, red card, eject button
    }

    @Test
    public void swapOpponentMoves() {
        TestBattle battle = TestBattle.createTrainerBattle(PokemonNamesies.STEELIX, PokemonNamesies.SHUCKLE);
        TestPokemon attacking1 = battle.getAttacking();
        TestPokemon attacking2 = battle.addAttacking(PokemonNamesies.REGIROCK);
        Assert.assertSame(battle.getAttacking(), attacking1);

        // Use Dragon Tail -- make sure they swap
        battle.fight(AttackNamesies.ENDURE, AttackNamesies.DRAGON_TAIL);
        Assert.assertSame(battle.getAttacking(), attacking2);

        // Don't swap with Suction Cups
        attacking2.withAbility(AbilityNamesies.SUCTION_CUPS);
        battle.fight(AttackNamesies.ENDURE, AttackNamesies.CIRCLE_THROW);
        Assert.assertSame(battle.getAttacking(), attacking2);

        attacking2.withAbility(AbilityNamesies.NO_ABILITY);
        battle.fight(AttackNamesies.ENDURE, AttackNamesies.ROAR);
        Assert.assertSame(battle.getAttacking(), attacking1);

        // Don't swap when ingrained
        battle.fight(AttackNamesies.INGRAIN, AttackNamesies.WHIRLWIND);
        Assert.assertSame(battle.getAttacking(), attacking1);

        // TODO: No more remaining Pokemon, wild battles, wimp out, red card, eject button
    }

    @Test
    public void curseTest() {
        // TODO: Protean
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Non-Ghost type curse -- apply stat changes
        battle.attackingFight(AttackNamesies.CURSE);
        attacking.assertNotType(battle, Type.GHOST);
        attacking.assertStages(new TestStages().set(1, Stat.ATTACK, Stat.DEFENSE)
                                               .set(-1, Stat.SPEED));
        attacking.assertNoEffect(PokemonEffectNamesies.CURSE);
        defending.assertNoEffect(PokemonEffectNamesies.CURSE);

        // Add Ghost Type
        battle.defendingFight(AttackNamesies.TRICK_OR_TREAT);
        attacking.assertType(battle, Type.GHOST);
        attacking.assertFullHealth();
        defending.assertFullHealth();

        // Make sure stat changes remain the same and target gets curse effect
        battle.attackingFight(AttackNamesies.CURSE);
        attacking.assertStages(new TestStages().set(1, Stat.ATTACK, Stat.DEFENSE)
                                               .set(-1, Stat.SPEED));
        attacking.assertNoEffect(PokemonEffectNamesies.CURSE);
        defending.assertHasEffect(PokemonEffectNamesies.CURSE);
        attacking.assertHealthRatio(.5);
        defending.assertHealthRatio(.75);
    }

    // Used for attacks that have a random element to them -- like Tri-Attack and Acupressure -- required running several times
    @Test
    public void randomEffectsTest() {
        boolean triAttackAlwaysSame = true;
        Map<StatusNamesies, Boolean> triAttackStatusMap = new EnumMap<>(StatusNamesies.class);
        triAttackStatusMap.put(StatusNamesies.NO_STATUS, false);
        triAttackStatusMap.put(StatusNamesies.PARALYZED, false);
        triAttackStatusMap.put(StatusNamesies.BURNED, false);
        triAttackStatusMap.put(StatusNamesies.FROZEN, false);

        boolean acupressureAlwaysSame = true;
        boolean[] acupressureStats = new boolean[Stat.NUM_BATTLE_STATS];

        // n = 15 for Tri-Attack because 20%
        int numTrials = GeneralUtils.numTrials(.99, 15);
        Assert.assertEquals(110, numTrials);
        for (int i = 0; i < numTrials; i++) {
            TestBattle battle = TestBattle.create(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE);
            TestPokemon attacking = battle.getAttacking();
            TestPokemon defending = battle.getDefending();

            // Tri-Attack
            battle.fight(AttackNamesies.TRI_ATTACK, AttackNamesies.TRI_ATTACK);

            StatusNamesies attackingCondition = attacking.getStatus().namesies();
            StatusNamesies defendingCondition = defending.getStatus().namesies();

            Assert.assertTrue(triAttackStatusMap.containsKey(attackingCondition));
            Assert.assertTrue(triAttackStatusMap.containsKey(defendingCondition));

            triAttackStatusMap.put(attackingCondition, true);
            triAttackStatusMap.put(defendingCondition, true);

            if (attackingCondition != defendingCondition) {
                triAttackAlwaysSame = false;
            }

            battle.emptyHeal();

            // Acupressure
            battle.fight(AttackNamesies.ACUPRESSURE, AttackNamesies.ACUPRESSURE);

            boolean foundAttacking = false;
            boolean foundDefending = false;
            for (Stat stat : Stat.BATTLE_STATS) {
                int attackingStage = attacking.getStage(stat);
                int defendingStage = defending.getStage(stat);
                if (attackingStage != defendingStage) {
                    acupressureAlwaysSame = false;
                }

                int index = stat.index();
                if (attackingStage > 0) {
                    Assert.assertFalse(foundAttacking);
                    acupressureStats[index] = true;
                    foundAttacking = true;
                }

                if (defendingStage > 0) {
                    Assert.assertFalse(foundDefending);
                    acupressureStats[index] = true;
                    foundDefending = true;
                }
            }
            Assert.assertTrue(foundAttacking);
            Assert.assertTrue(foundDefending);
        }

        Assert.assertFalse(triAttackAlwaysSame);
        Assert.assertTrue(triAttackStatusMap.get(StatusNamesies.NO_STATUS));
        Assert.assertTrue(triAttackStatusMap.get(StatusNamesies.PARALYZED));
        Assert.assertTrue(triAttackStatusMap.get(StatusNamesies.BURNED));
        Assert.assertTrue(triAttackStatusMap.get(StatusNamesies.FROZEN));

        Assert.assertFalse(acupressureAlwaysSame);
        for (int j = 0; j < acupressureStats.length; j++) {
            Assert.assertTrue(Stat.getStat(j, true).getName(), acupressureStats[j]);
        }
    }

    @Test
    public void acupressureTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Acupressure sharply raises a random stat
        battle.attackingFight(AttackNamesies.ACUPRESSURE);
        attacking.assertTotalStages(2);
        defending.assertStages(new TestStages());

        // Can be stole with Snatch
        battle.fight(AttackNamesies.ACUPRESSURE, AttackNamesies.SNATCH);
        attacking.assertTotalStages(2);
        defending.assertTotalStages(2);

        // Use Acupressure 20 more times (every stat should be exactly maxed afterwards)
        // Should be decent at making sure its never choosing a stat that is already maxed
        for (int i = 0; i < 20; i++) {
            battle.attackingFight(AttackNamesies.ACUPRESSURE);
            attacking.assertTotalStages(2*(i + 2)); // +2 since we've already gone once
            defending.assertTotalStages(2);
            Assert.assertTrue(attacking.lastMoveSucceeded());
        }

        // All stats should be maxed now
        attacking.assertStages(new TestStages().set(Stages.MAX_STAT_CHANGES, Stat.BATTLE_STATS.toArray(new Stat[0])));

        // Acupressure should fail now
        battle.attackingFight(AttackNamesies.ACUPRESSURE);
        Assert.assertFalse(attacking.lastMoveSucceeded());

        // But should still be snatchable
        battle.fight(AttackNamesies.ACUPRESSURE, AttackNamesies.SNATCH);
        attacking.assertTotalStages(Stages.MAX_STAT_CHANGES*Stat.NUM_BATTLE_STATS);
        defending.assertTotalStages(4);
    }

    @Test
    public void roostTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.DRAGONITE, PokemonNamesies.MAGIKARP);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Should fail with full hp
        attacking.assertType(battle, Type.FLYING);
        attacking.apply(false, AttackNamesies.ROOST, battle);
        attacking.assertType(battle, Type.FLYING);

        // Reduce health and apply again
        battle.attackingFight(AttackNamesies.BELLY_DRUM);
        attacking.assertNotFullHealth();
        attacking.apply(true, AttackNamesies.ROOST, battle);
        attacking.assertNotType(battle, Type.FLYING);
        attacking.assertFullHealth();

        // Should fail because attack is already maxed -- flying type should come back at the end of the turn
        battle.attackingFight(AttackNamesies.BELLY_DRUM);
        attacking.assertFullHealth();
        attacking.assertType(battle, Type.FLYING);

        // Clear stat changes and reduce again
        attacking.assertStages(new TestStages().set(Stages.MAX_STAT_CHANGES, Stat.ATTACK));
        battle.attackingFight(AttackNamesies.HAZE);
        attacking.assertStages(new TestStages());
        battle.attackingFight(AttackNamesies.BELLY_DRUM);
        attacking.assertStages(new TestStages().set(Stages.MAX_STAT_CHANGES, Stat.ATTACK));

        // Using a full turn should bring the flying type back at the end
        attacking.assertNotFullHealth();
        battle.attackingFight(AttackNamesies.ROOST);
        attacking.assertType(battle, Type.FLYING);
        attacking.assertFullHealth();

        defending.apply(false, AttackNamesies.MUD_SLAP, battle);
        defending.apply(true, AttackNamesies.TACKLE, battle);
        attacking.assertNotFullHealth();
        attacking.apply(true, AttackNamesies.ROOST, battle);
        attacking.assertFullHealth();
        defending.apply(true, AttackNamesies.MUD_SLAP, battle);
        attacking.assertNotFullHealth();
    }

    @Test
    public void lastResortTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking().withMoves(AttackNamesies.LAST_RESORT);
        battle.getDefending().withMoves(AttackNamesies.SPLASH, AttackNamesies.ENDURE);

        // Should work if it is the only move the pokemon knows
        attacking.apply(true, AttackNamesies.LAST_RESORT, battle);

        // Should fail if multiple moves and hasn't used all of them yet
        battle.emptyHeal();
        attacking.withMoves(AttackNamesies.TACKLE, AttackNamesies.LAST_RESORT);
        attacking.apply(false, AttackNamesies.LAST_RESORT, battle);

        // Use the other move and then it should work
        Move tackle = attacking.getMoves(battle).get(0);
        attacking.setMove(tackle);
        Assert.assertFalse(tackle.used());
        battle.fight();
        Assert.assertTrue(tackle.used());
        attacking.apply(true, AttackNamesies.LAST_RESORT, battle);
    }

    @Test
    public void psychoShiftTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking().withAbility(AbilityNamesies.MAGIC_GUARD);
        TestPokemon defending = battle.getDefending().withAbility(AbilityNamesies.MAGIC_GUARD);

        // So water-type Pokemon with Magic Guard should be fine for all statues and take no passive damage
        battle.fight(AttackNamesies.SOAK, AttackNamesies.SOAK);
        attacking.assertType(battle, Type.WATER);
        defending.assertType(battle, Type.WATER);

        // Should fail because no status condition
        attacking.apply(false, AttackNamesies.PSYCHO_SHIFT, battle);

        // Burn the opponent
        battle.attackingFight(AttackNamesies.WILL_O_WISP);
        attacking.assertNoStatus();
        defending.assertHasStatus(StatusNamesies.BURNED);

        // Attacking still doesn't have a status condition
        attacking.apply(false, AttackNamesies.PSYCHO_SHIFT, battle);
        attacking.assertNoStatus();
        defending.assertHasStatus(StatusNamesies.BURNED);

        // But the defending does! (now attacking is the burned)
        defending.apply(true, AttackNamesies.PSYCHO_SHIFT, battle);
        attacking.assertHasStatus(StatusNamesies.BURNED);
        defending.assertNoStatus();

        // Poison dat defender badly
        battle.attackingFight(AttackNamesies.TOXIC);
        attacking.assertHasStatus(StatusNamesies.BURNED);
        defending.assertBadPoison();

        // Psycho shift should fail for both now (since they both have statuses)
        attacking.apply(false, AttackNamesies.PSYCHO_SHIFT, battle);
        defending.apply(false, AttackNamesies.PSYCHO_SHIFT, battle);
        attacking.assertHasStatus(StatusNamesies.BURNED);
        defending.assertBadPoison();

        // HEAL THE BURN (like feeeel the burn get itttttt okay sorry)
        battle.attackingFight(AttackNamesies.REFRESH);
        attacking.assertNoStatus();
        defending.assertBadPoison();

        // Same deal as before -- shift the bad poison to the attacking
        attacking.apply(false, AttackNamesies.PSYCHO_SHIFT, battle);
        defending.apply(true, AttackNamesies.PSYCHO_SHIFT, battle);
        attacking.assertBadPoison();
        defending.assertNoStatus();

        // Switch to Poison-type (I guess you can still be poisoned afterwards)
        attacking.withAbility(AbilityNamesies.PROTEAN);
        attacking.assertType(battle, Type.WATER);
        battle.attackingFight(AttackNamesies.CLEAR_SMOG);
        attacking.assertType(battle, Type.POISON);
        attacking.assertBadPoison();

        // Shift the bad poison back to the defending
        battle.attackingFight(AttackNamesies.PSYCHO_SHIFT);
        attacking.assertNoStatus();
        defending.assertBadPoison();
        attacking.assertType(battle, Type.PSYCHIC);

        // Switch back to Poison-type -- Psycho Shift should fail now since it doesn't affect poison brothers
        battle.attackingFight(AttackNamesies.ACID_ARMOR);
        attacking.assertType(battle, Type.POISON);
        defending.apply(false, AttackNamesies.PSYCHO_SHIFT, battle);

        // Switch to Dragon-type -- should succeed now
        battle.attackingFight(AttackNamesies.DRAGON_DANCE);
        attacking.assertType(battle, Type.DRAGON);
        defending.apply(true, AttackNamesies.PSYCHO_SHIFT, battle);
        attacking.assertBadPoison();
        defending.assertNoStatus();

        // Give defending Immunity (cannot be poisoned) -- Psycho Shift should fail
        defending.withAbility(AbilityNamesies.IMMUNITY);
        defending.apply(false, AttackNamesies.PSYCHO_SHIFT, battle);
        attacking.assertBadPoison();
        defending.assertNoStatus();

        battle.emptyHeal();
        attacking.assertNoStatus();
        defending.assertNoStatus();

        // Put attacking to sleep
        battle.defendingFight(AttackNamesies.SPORE);
        attacking.assertHasStatus(StatusNamesies.ASLEEP);
        defending.assertNoStatus();

        // Set move list to only be Psycho Shift and use Sleep Talk (thus calling Psycho Shift)
        // This should succeed and the defending should now be asleep
        attacking.withMoves(AttackNamesies.PSYCHO_SHIFT);
        battle.attackingFight(AttackNamesies.SLEEP_TALK);
        attacking.assertNoStatus();
        defending.assertHasStatus(StatusNamesies.ASLEEP);

        // I guess you can just do this since the apply method doesn't confirm it can attack (since it's asleep...) whatever
        defending.apply(true, AttackNamesies.PSYCHO_SHIFT, battle);
        attacking.assertHasStatus(StatusNamesies.ASLEEP);
        defending.assertNoStatus();

        // Sleep Talk Psycho Shift will fail if sleep doesn't apply (like with Insomnia ability)
        defending.withAbility(AbilityNamesies.INSOMNIA);
        battle.attackingFight(AttackNamesies.SLEEP_TALK);
        attacking.assertHasStatus(StatusNamesies.ASLEEP);
        defending.assertNoStatus();
    }

    @Test
    public void powderMoveTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Powder moves shouldn't work on Grass-type Pokemon
        attacking.assertType(battle, Type.GRASS);
        defending.apply(false, AttackNamesies.SLEEP_POWDER, battle);
        attacking.apply(true, AttackNamesies.SLEEP_POWDER, battle);

        battle.emptyHeal();
        battle.defendingFight(AttackNamesies.SOAK);
        attacking.assertNotType(battle, Type.GRASS);
        defending.apply(true, AttackNamesies.SLEEP_POWDER, battle);

        // Or Pokemon with Overcoat
        battle.emptyHeal();
        defending.withAbility(AbilityNamesies.OVERCOAT);
        attacking.apply(false, AttackNamesies.SLEEP_POWDER, battle);
        attacking.apply(false, AttackNamesies.COTTON_SPORE, battle);
        attacking.apply(true, AttackNamesies.LEECH_SEED, battle);

        // Or Pokemon holding Safety Goggles
        battle.emptyHeal();
        defending.withAbility(AbilityNamesies.NO_ABILITY);
        defending.giveItem(ItemNamesies.SAFETY_GOGGLES);
        attacking.apply(false, AttackNamesies.SPORE, battle);
        battle.emptyHeal();
        attacking.apply(false, AttackNamesies.POISON_POWDER, battle);
        attacking.apply(true, AttackNamesies.VINE_WHIP, battle);
    }

    @Test
    public void bugBiteTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Attacking Pokemon will hold Rawst Berry
        // Burn the defending Pokemon
        attacking.giveItem(ItemNamesies.RAWST_BERRY);
        battle.attackingFight(AttackNamesies.WILL_O_WISP);
        Assert.assertTrue(attacking.isHoldingItem());
        defending.assertHasStatus(StatusNamesies.BURNED);

        // Defending Pokemon will use Bug Bite and eat the Rawst Berry, curing its burn
        // Attacking will have its item consumed, but defending is the one who ate the berry
        battle.fight(AttackNamesies.ENDURE, AttackNamesies.BUG_BITE);
        attacking.assertNotHoldingItem();
        defending.assertNotHoldingItem();
        defending.assertNoStatus();
        attacking.assertNoEffect(PokemonEffectNamesies.EATEN_BERRY);
        defending.assertHasEffect(PokemonEffectNamesies.EATEN_BERRY);
        attacking.assertHasEffect(PokemonEffectNamesies.CONSUMED_ITEM);
        defending.assertNoEffect(PokemonEffectNamesies.CONSUMED_ITEM);

        // Should fail since the defending did not use their own item
        battle.defendingFight(AttackNamesies.RECYCLE);
        Assert.assertFalse(defending.lastMoveSucceeded());
        attacking.assertNotHoldingItem();
        defending.assertNotHoldingItem();

        // Using Recycle after having having your berry eaten will bring the Rawst Berry back
        battle.attackingFight(AttackNamesies.RECYCLE);
        attacking.assertHoldingItem(ItemNamesies.RAWST_BERRY);
        defending.assertNotHoldingItem();
        attacking.assertNoStatus();
        defending.assertNoStatus();
        attacking.assertNoEffect(PokemonEffectNamesies.EATEN_BERRY);
        defending.assertHasEffect(PokemonEffectNamesies.EATEN_BERRY);
        attacking.assertHasEffect(PokemonEffectNamesies.CONSUMED_ITEM);
        defending.assertNoEffect(PokemonEffectNamesies.CONSUMED_ITEM);

        // Poison the attacker -- will not trigger Rawst Berry
        battle.defendingFight(AttackNamesies.POISON_POWDER);
        Assert.assertTrue(attacking.isHoldingItem());
        defending.assertNotHoldingItem();
        attacking.assertRegularPoison();
        defending.assertNoStatus();

        // Transfer Poison to the defending
        battle.attackingFight(AttackNamesies.PSYCHO_SHIFT);
        Assert.assertTrue(attacking.isHoldingItem());
        defending.assertNotHoldingItem();
        attacking.assertNoStatus();
        defending.assertRegularPoison();

        // Burn the attacker -- will consume the Rawst Berry
        // Note: I am writing these comments much later than the code was written, I have no idea what poison has to do with anything
        battle.defendingFight(AttackNamesies.WILL_O_WISP);
        attacking.assertNotHoldingItem();
        defending.assertNotHoldingItem();
        attacking.assertNoStatus();
        defending.assertRegularPoison();
        attacking.assertConsumedBerry();
        defending.assertHasEffect(PokemonEffectNamesies.EATEN_BERRY);
        defending.assertNoEffect(PokemonEffectNamesies.CONSUMED_ITEM);
    }

    @Test
    public void moveCountTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.HAPPINY, PokemonNamesies.SHUCKLE);

        assertModifier(1, AttackNamesies.ROLLOUT, battle);
        assertModifier(2, AttackNamesies.ROLLOUT, battle);
        assertModifier(3, AttackNamesies.ROLLOUT, battle);
        assertModifier(4, AttackNamesies.ROLLOUT, battle);
        assertModifier(5, AttackNamesies.ROLLOUT, battle);

        // Max is 5
        assertModifier(5, AttackNamesies.ROLLOUT, battle);
        assertModifier(5, AttackNamesies.ROLLOUT, battle);

        // Other count-based moves (obviously) do not stack
        assertModifier(1, AttackNamesies.FURY_CUTTER, battle);
        assertModifier(1, AttackNamesies.ICE_BALL, battle);
        assertModifier(1, AttackNamesies.ROLLOUT, battle);

        // Defense Curl increases multiplier by 2 for Rollout and Ice Ball (but not for Fury Cutter)
        assertModifier(1, AttackNamesies.DEFENSE_CURL, battle);
        assertModifier(2, AttackNamesies.ROLLOUT, battle);
        assertModifier(4, AttackNamesies.ROLLOUT, battle);
        assertModifier(6, AttackNamesies.ROLLOUT, battle);
        assertModifier(8, AttackNamesies.ROLLOUT, battle);
        assertModifier(10, AttackNamesies.ROLLOUT, battle);
        assertModifier(10, AttackNamesies.ROLLOUT, battle);
        assertModifier(1, AttackNamesies.FURY_CUTTER, battle);
        assertModifier(2, AttackNamesies.FURY_CUTTER, battle);
        assertModifier(3, AttackNamesies.FURY_CUTTER, battle);

        // Minimize does nothing (not sure why I included it)
        assertModifier(2, AttackNamesies.ROLLOUT, battle);
        assertModifier(1, AttackNamesies.MINIMIZE, battle);
        assertModifier(1, AttackNamesies.FURY_CUTTER, battle);
        assertModifier(2, AttackNamesies.ROLLOUT, battle);
        assertModifier(2, AttackNamesies.ICE_BALL, battle);
        assertModifier(4, AttackNamesies.ICE_BALL, battle);
    }

    private void assertModifier(double expectedModifier, AttackNamesies attack, TestBattle battle) {
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        defending.fullyHeal();
        attacking.setExpectedDamageModifier(expectedModifier);
        battle.attackingFight(attack);
        TestUtils.assertEquals(expectedModifier, battle.getDamageModifier(attacking, defending));
    }

    @Test
    public void changeAbilityTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking().withAbility(AbilityNamesies.OVERGROW);
        TestPokemon defending = battle.getDefending().withAbility(AbilityNamesies.BLAZE);

        battle.attackingFight(AttackNamesies.SKILL_SWAP);
        attacking.assertChangedAbility(AbilityNamesies.BLAZE);
        defending.assertChangedAbility(AbilityNamesies.OVERGROW);

        battle.attackingFight(AttackNamesies.WORRY_SEED);
        attacking.assertChangedAbility(AbilityNamesies.BLAZE);
        defending.assertChangedAbility(AbilityNamesies.INSOMNIA);

        battle.defendingFight(AttackNamesies.SKILL_SWAP);
        attacking.assertChangedAbility(AbilityNamesies.INSOMNIA);
        defending.assertChangedAbility(AbilityNamesies.BLAZE);

        battle.attackingFight(AttackNamesies.ENTRAINMENT);
        attacking.assertChangedAbility(AbilityNamesies.INSOMNIA);
        defending.assertChangedAbility(AbilityNamesies.INSOMNIA);

        battle.attackingFight(AttackNamesies.SIMPLE_BEAM);
        attacking.assertChangedAbility(AbilityNamesies.INSOMNIA);
        defending.assertChangedAbility(AbilityNamesies.SIMPLE);

        battle.attackingFight(AttackNamesies.SKILL_SWAP);
        attacking.assertChangedAbility(AbilityNamesies.SIMPLE);
        defending.assertChangedAbility(AbilityNamesies.INSOMNIA);

        battle.defendingFight(AttackNamesies.ROLE_PLAY);
        attacking.assertChangedAbility(AbilityNamesies.SIMPLE);
        defending.assertChangedAbility(AbilityNamesies.SIMPLE);

        battle.defendingFight(AttackNamesies.GASTRO_ACID);
        attacking.assertChangedAbility(AbilityNamesies.NO_ABILITY);
        defending.assertChangedAbility(AbilityNamesies.SIMPLE);

        // Skill Swap will fail when one ability is no ability
        battle.defendingFight(AttackNamesies.SKILL_SWAP);
        attacking.assertChangedAbility(AbilityNamesies.NO_ABILITY);
        defending.assertChangedAbility(AbilityNamesies.SIMPLE);
    }

    @Test
    public void rapidSpinDefogTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE);
        TestPokemon attacking = battle.getAttacking().withItem(ItemNamesies.GRIP_CLAW);
        TestPokemon defending = battle.getDefending().withAbility(AbilityNamesies.MAGIC_GUARD).withItem(ItemNamesies.LIGHT_CLAY);

        // Add effects
        battle.attackingFight(AttackNamesies.STEALTH_ROCK);
        battle.attackingFight(AttackNamesies.TOXIC_SPIKES);
        battle.attackingFight(AttackNamesies.SPIKES);
        battle.attackingFight(AttackNamesies.LEECH_SEED); // Rapid Spin only
        battle.defendingFight(AttackNamesies.LIGHT_SCREEN); // Defog only
        battle.attackingFight(AttackNamesies.WRAP); // Rapid Spin only
        battle.assertHasEffect(defending, TeamEffectNamesies.LIGHT_SCREEN);
        battle.assertHasEffect(defending, TeamEffectNamesies.STEALTH_ROCK);
        battle.assertHasEffect(defending, TeamEffectNamesies.TOXIC_SPIKES);
        battle.assertHasEffect(defending, TeamEffectNamesies.SPIKES);
        defending.assertHasEffect(PokemonEffectNamesies.LEECH_SEED);
        defending.assertHasEffect(PokemonEffectNamesies.WRAPPED);

        // Make sure effects persist
        battle.emptyHeal();
        battle.defendingFight(AttackNamesies.TACKLE);
        battle.assertHasEffect(defending, TeamEffectNamesies.LIGHT_SCREEN);
        battle.assertHasEffect(defending, TeamEffectNamesies.STEALTH_ROCK);
        battle.assertHasEffect(defending, TeamEffectNamesies.TOXIC_SPIKES);
        battle.assertHasEffect(defending, TeamEffectNamesies.SPIKES);
        defending.assertHasEffect(PokemonEffectNamesies.LEECH_SEED);
        defending.assertHasEffect(PokemonEffectNamesies.WRAPPED);

        // Use Rapid Spin -- should remove the appropriate effects
        battle.defendingFight(AttackNamesies.RAPID_SPIN);
        attacking.assertStages(new TestStages());
        defending.assertStages(new TestStages().set(1, Stat.SPEED));
        battle.assertHasEffect(defending, TeamEffectNamesies.LIGHT_SCREEN);
        battle.assertNoEffect(defending, TeamEffectNamesies.STEALTH_ROCK);
        battle.assertNoEffect(defending, TeamEffectNamesies.TOXIC_SPIKES);
        battle.assertNoEffect(defending, TeamEffectNamesies.SPIKES);
        defending.assertNoEffect(PokemonEffectNamesies.LEECH_SEED);
        defending.assertNoEffect(PokemonEffectNamesies.WRAPPED);

        // Add effects back
        battle.emptyHeal();
        battle.attackingFight(AttackNamesies.STEALTH_ROCK);
        battle.attackingFight(AttackNamesies.TOXIC_SPIKES);
        battle.attackingFight(AttackNamesies.SPIKES);
        battle.attackingFight(AttackNamesies.LEECH_SEED);
        battle.defendingFight(AttackNamesies.LIGHT_SCREEN);
        battle.attackingFight(AttackNamesies.WRAP);
        battle.assertHasEffect(defending, TeamEffectNamesies.LIGHT_SCREEN);
        battle.assertHasEffect(defending, TeamEffectNamesies.STEALTH_ROCK);
        battle.assertHasEffect(defending, TeamEffectNamesies.TOXIC_SPIKES);
        battle.assertHasEffect(defending, TeamEffectNamesies.SPIKES);
        defending.assertHasEffect(PokemonEffectNamesies.LEECH_SEED);
        defending.assertHasEffect(PokemonEffectNamesies.WRAPPED);

        // Wrong attacker -- effects shouldn't change
        battle.attackingFight(AttackNamesies.RAPID_SPIN);
        battle.defendingFight(AttackNamesies.DEFOG);
        attacking.assertStages(new TestStages().set(-1, Stat.EVASION).set(1, Stat.SPEED));
        defending.assertStages(new TestStages().set(1, Stat.SPEED));
        battle.assertHasEffect(defending, TeamEffectNamesies.LIGHT_SCREEN);
        battle.assertHasEffect(defending, TeamEffectNamesies.STEALTH_ROCK);
        battle.assertHasEffect(defending, TeamEffectNamesies.TOXIC_SPIKES);
        battle.assertHasEffect(defending, TeamEffectNamesies.SPIKES);
        defending.assertHasEffect(PokemonEffectNamesies.LEECH_SEED);
        defending.assertHasEffect(PokemonEffectNamesies.WRAPPED);

        // Correct defog attacker -- should only remove the appropriate effects
        battle.attackingFight(AttackNamesies.DEFOG);
        attacking.assertStages(new TestStages().set(-1, Stat.EVASION).set(1, Stat.SPEED));
        defending.assertStages(new TestStages().set(-1, Stat.EVASION).set(1, Stat.SPEED));
        battle.assertNoEffect(defending, TeamEffectNamesies.LIGHT_SCREEN);
        battle.assertNoEffect(defending, TeamEffectNamesies.STEALTH_ROCK);
        battle.assertNoEffect(defending, TeamEffectNamesies.TOXIC_SPIKES);
        battle.assertNoEffect(defending, TeamEffectNamesies.SPIKES);
        battle.assertNoEffect(defending, TeamEffectNamesies.SPIKES);
        defending.assertHasEffect(PokemonEffectNamesies.LEECH_SEED);
        defending.assertHasEffect(PokemonEffectNamesies.WRAPPED);
    }

    @Test
    public void courtChangeTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Add some team effects
        battle.fight(AttackNamesies.STEALTH_ROCK, AttackNamesies.SPIKES);
        battle.fight(AttackNamesies.LIGHT_SCREEN, AttackNamesies.TAILWIND);   // a: LS 4, T 3
        battle.fight(AttackNamesies.STICKY_WEB, AttackNamesies.LIGHT_SCREEN); // a: LS 3, T 2 | d: LS 4

        courtChangeTest(battle, attacking,
                        TeamEffectNamesies.LIGHT_SCREEN, // Longer light screen
                        TeamEffectNamesies.SPIKES
        );
        courtChangeTest(battle, defending,
                        TeamEffectNamesies.STEALTH_ROCK,
                        TeamEffectNamesies.LIGHT_SCREEN, // Shorter light screen
                        TeamEffectNamesies.STICKY_WEB,
                        TeamEffectNamesies.TAILWIND
        );

        // Swapperooni
        battle.attackingFight(AttackNamesies.COURT_CHANGE); // a: LS 3 | d: LS 2, T 1

        courtChangeTest(battle, attacking,
                        TeamEffectNamesies.STEALTH_ROCK,
                        TeamEffectNamesies.LIGHT_SCREEN, // Longer light screen
                        TeamEffectNamesies.STICKY_WEB,
                        TeamEffectNamesies.TAILWIND
        );
        courtChangeTest(battle, defending,
                        TeamEffectNamesies.LIGHT_SCREEN, // Shorter light screen
                        TeamEffectNamesies.SPIKES
        );

        // Do nothing to make counters decrease (make sure Tailwind finishes this turn exactly)
        battle.splashFight(); // a: LS 2 | d: LS 1, T 0

        courtChangeTest(battle, attacking, // No longer includes Tailwind
                        TeamEffectNamesies.STEALTH_ROCK,
                        TeamEffectNamesies.LIGHT_SCREEN, // Longer light screen
                        TeamEffectNamesies.STICKY_WEB
        );
        courtChangeTest(battle, defending,
                        TeamEffectNamesies.LIGHT_SCREEN, // Shorter light screen
                        TeamEffectNamesies.SPIKES
        );

        // Do nothing to make counters decrease (make sure shorter Light Screen finishes this turn exactly)
        battle.splashFight(); // a: LS 1 | d: LS 0

        courtChangeTest(battle, attacking,
                        TeamEffectNamesies.LIGHT_SCREEN, // Longer light screen
                        TeamEffectNamesies.STEALTH_ROCK,
                        TeamEffectNamesies.STICKY_WEB
        );
        courtChangeTest(battle, defending, TeamEffectNamesies.SPIKES); // No longer includes (shorter) Light Screen

        // Do nothing to make counters decrease (make sure longer Light Screen finishes this turn exactly)
        battle.splashFight(); // a: LS 0

        courtChangeTest(battle, attacking, // No longer includes (longer) Light Screen
                        TeamEffectNamesies.STEALTH_ROCK,
                        TeamEffectNamesies.STICKY_WEB
        );
        courtChangeTest(battle, defending, TeamEffectNamesies.SPIKES);

        // Swap entry hazards back blah blah blah
        battle.defendingFight(AttackNamesies.COURT_CHANGE);

        courtChangeTest(battle, attacking, TeamEffectNamesies.SPIKES);
        courtChangeTest(battle, defending, TeamEffectNamesies.STEALTH_ROCK, TeamEffectNamesies.STICKY_WEB);
    }

    private void courtChangeTest(TestBattle battle, TestPokemon teamPokemon, TeamEffectNamesies... teamEffects) {
        List<TeamEffectNamesies> courtChangeEffects = List.of(
                TeamEffectNamesies.REFLECT,
                TeamEffectNamesies.LIGHT_SCREEN,
                TeamEffectNamesies.AURORA_VEIL,
                TeamEffectNamesies.STEALTH_ROCK,
                TeamEffectNamesies.SPIKES,
                TeamEffectNamesies.STICKY_WEB,
                TeamEffectNamesies.TOXIC_SPIKES,
                TeamEffectNamesies.TAILWIND
        );

        Set<TeamEffectNamesies> hasEffects = Set.of(teamEffects);

        // For each relevant court change effect, confirm that the team either has or does not have the effect
        for (TeamEffectNamesies effect : courtChangeEffects) {
            battle.assertEffect(hasEffects.contains(effect), teamPokemon, effect);
        }
    }

    @Test
    public void restTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        battle.defendingFight(AttackNamesies.FALSE_SWIPE);
        battle.defendingFight(AttackNamesies.TOXIC);

        attacking.assertNotFullHealth();
        attacking.assertBadPoison();

        battle.attackingFight(AttackNamesies.REST);
        attacking.assertFullHealth();
        attacking.assertHasStatus(StatusNamesies.ASLEEP);

        // Resting Pokemon should be asleep for exactly two turns -- False Swipe should fail here and the next turn
        battle.attackingFight(AttackNamesies.FALSE_SWIPE);
        defending.assertFullHealth();
        attacking.assertFullHealth();
        attacking.assertHasStatus(StatusNamesies.ASLEEP);

        battle.attackingFight(AttackNamesies.FALSE_SWIPE);
        defending.assertFullHealth();
        attacking.assertFullHealth();
        attacking.assertHasStatus(StatusNamesies.ASLEEP);

        // Should wake up on this turn
        battle.attackingFight(AttackNamesies.FALSE_SWIPE);
        defending.assertNotFullHealth();
        attacking.assertFullHealth();
        attacking.assertNoStatus();

        // TODO: Test with Insomnia
        // TODO: Test with Sleep Talk
    }

    @Test
    public void sketchTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking();

        // Should originally fail since no move to copy
        attacking.apply(false, AttackNamesies.SKETCH, battle);
        attacking.withMoves(AttackNamesies.SKETCH);
        Assert.assertTrue(attacking.hasActualMove(AttackNamesies.SKETCH));
        attacking.apply(false, AttackNamesies.SKETCH, battle);

        battle.defendingFight(AttackNamesies.SPLASH);
        Assert.assertTrue(attacking.hasActualMove(AttackNamesies.SKETCH));
        attacking.apply(true, AttackNamesies.SKETCH, battle);
        Assert.assertFalse(attacking.hasActualMove(AttackNamesies.SKETCH));
        Assert.assertTrue(attacking.hasActualMove(AttackNamesies.SPLASH));

        // Should fail since the user doesn't actual know Sketch
        battle.defendingFight(AttackNamesies.SWORDS_DANCE);
        Assert.assertFalse(attacking.hasActualMove(AttackNamesies.SKETCH));
        attacking.apply(false, AttackNamesies.SKETCH, battle);
        Assert.assertFalse(attacking.hasActualMove(AttackNamesies.SKETCH));
        Assert.assertFalse(attacking.hasActualMove(AttackNamesies.SWORDS_DANCE));
        Assert.assertTrue(attacking.hasActualMove(AttackNamesies.SPLASH));

        // Should fail if the user already knows the sketchy moves
        attacking.withMoves(AttackNamesies.SKETCH, AttackNamesies.GROWL);
        battle.defendingFight(AttackNamesies.GROWL);
        attacking.apply(false, AttackNamesies.SKETCH, battle);
        Assert.assertTrue(attacking.hasActualMove(AttackNamesies.SKETCH));
        Assert.assertTrue(attacking.hasActualMove(AttackNamesies.GROWL));
    }

    @Test
    public void fellStingerTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.KARTANA, PokemonNamesies.HAPPINY);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Not Beast Boost
        attacking.assertAbility(AbilityNamesies.NO_ABILITY);

        // Stage should not change without MURDER
        battle.fight(AttackNamesies.FELL_STINGER, AttackNamesies.ENDURE);
        Assert.assertFalse(defending.isActuallyDead());
        attacking.assertNoStages();
        defending.assertNoStages();

        // Kill kill kill MURDER MURDER MURDER
        battle.attackingFight(AttackNamesies.FELL_STINGER);
        Assert.assertTrue(defending.isActuallyDead());
        attacking.assertStages(new TestStages().set(3, Stat.ATTACK));
    }

    @Test
    public void stageSwapTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        attacking.assertNoStages();
        defending.assertNoStages();

        // Screech is -2 defense to opponent, Swords Dance is +2 attack for use
        battle.fight(AttackNamesies.SCREECH, AttackNamesies.SWORDS_DANCE);
        attacking.assertNoStages();
        defending.assertStages(new TestStages().set(-2, Stat.DEFENSE)
                                               .set(2, Stat.ATTACK));

        // Swaps attacking stats
        battle.attackingFight(AttackNamesies.POWER_SWAP);
        attacking.assertStages(new TestStages().set(2, Stat.ATTACK));
        defending.assertStages(new TestStages().set(-2, Stat.DEFENSE));

        battle.attackingFight(AttackNamesies.POWER_SWAP);
        attacking.assertNoStages();
        defending.assertStages(new TestStages().set(-2, Stat.DEFENSE)
                                               .set(2, Stat.ATTACK));

        // Does the same exact thing regardless of attacker
        battle.defendingFight(AttackNamesies.POWER_SWAP);
        attacking.assertStages(new TestStages().set(2, Stat.ATTACK));
        defending.assertStages(new TestStages().set(-2, Stat.DEFENSE));

        // Swaps all stats
        battle.defendingFight(AttackNamesies.HEART_SWAP);
        attacking.assertStages(new TestStages().set(-2, Stat.DEFENSE));
        defending.assertStages(new TestStages().set(2, Stat.ATTACK));

        // Quiver Dance increases Sp. Attack, Sp. Defense, and Speed by 1 for user,
        // Sand Attack decreases opponent Accuracy by 1
        battle.fight(AttackNamesies.QUIVER_DANCE, AttackNamesies.SAND_ATTACK);
        attacking.assertStages(new TestStages().set(-2, Stat.DEFENSE)
                                               .set(1, Stat.SP_ATTACK, Stat.SP_DEFENSE, Stat.SPEED)
                                               .set(-1, Stat.ACCURACY));
        defending.assertStages(new TestStages().set(2, Stat.ATTACK));

        // Swaps defensive stats
        battle.attackingFight(AttackNamesies.GUARD_SWAP);
        attacking.assertStages(new TestStages().set(1, Stat.SP_ATTACK, Stat.SPEED)
                                               .set(-1, Stat.ACCURACY));
        defending.assertStages(new TestStages().set(2, Stat.ATTACK)
                                               .set(-2, Stat.DEFENSE)
                                               .set(1, Stat.SP_DEFENSE));

        // Calm Mind increases Sp. Attack and Sp. Defense by 1 for the user
        battle.fight(AttackNamesies.CALM_MIND, AttackNamesies.CALM_MIND);
        attacking.assertStages(new TestStages().set(2, Stat.SP_ATTACK)
                                               .set(1, Stat.SP_DEFENSE, Stat.SPEED)
                                               .set(-1, Stat.ACCURACY));
        defending.assertStages(new TestStages().set(2, Stat.ATTACK, Stat.SP_DEFENSE)
                                               .set(-2, Stat.DEFENSE)
                                               .set(1, Stat.SP_ATTACK));

        battle.attackingFight(AttackNamesies.GUARD_SWAP);
        attacking.assertStages(new TestStages().set(2, Stat.SP_ATTACK, Stat.SP_DEFENSE)
                                               .set(-2, Stat.DEFENSE)
                                               .set(1, Stat.SPEED)
                                               .set(-1, Stat.ACCURACY));
        defending.assertStages(new TestStages().set(2, Stat.ATTACK)
                                               .set(1, Stat.SP_ATTACK, Stat.SP_DEFENSE));

        // Decrease defending speed by 2, then swap speeds
        battle.fight(AttackNamesies.STRING_SHOT, AttackNamesies.SPEED_SWAP);
        attacking.assertStages(new TestStages().set(2, Stat.SP_ATTACK, Stat.SP_DEFENSE)
                                               .set(-2, Stat.DEFENSE, Stat.SPEED)
                                               .set(-1, Stat.ACCURACY));
        defending.assertStages(new TestStages().set(2, Stat.ATTACK)
                                               .set(1, Stat.SP_ATTACK, Stat.SP_DEFENSE, Stat.SPEED));

        // Just for the hell of it
        battle.defendingFight(AttackNamesies.HEART_SWAP);
        attacking.assertStages(new TestStages().set(2, Stat.ATTACK)
                                               .set(1, Stat.SP_ATTACK, Stat.SP_DEFENSE, Stat.SPEED));
        defending.assertStages(new TestStages().set(2, Stat.SP_ATTACK, Stat.SP_DEFENSE)
                                               .set(-2, Stat.DEFENSE, Stat.SPEED)
                                               .set(-1, Stat.ACCURACY));

        battle.defendingFight(AttackNamesies.POWER_SWAP);
        attacking.assertStages(new TestStages().set(2, Stat.SP_ATTACK)
                                               .set(1, Stat.SP_DEFENSE, Stat.SPEED));
        defending.assertStages(new TestStages().set(2, Stat.ATTACK, Stat.SP_DEFENSE)
                                               .set(-2, Stat.DEFENSE, Stat.SPEED)
                                               .set(1, Stat.SP_ATTACK)
                                               .set(-1, Stat.ACCURACY));
    }

    @Test
    public void spectralThiefTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.HAPPINY, PokemonNamesies.SHUCKLE);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending().withAbility(AbilityNamesies.PROTEAN);

        attacking.assertNoStages();
        defending.assertNoStages();

        battle.defendingFight(AttackNamesies.SHELL_SMASH);
        defending.assertType(battle, Type.NORMAL);
        attacking.assertNoStages();
        defending.assertStages(new TestStages().set(2, Stat.ATTACK, Stat.SP_ATTACK, Stat.SPEED)
                                               .set(-1, Stat.DEFENSE, Stat.SP_DEFENSE));

        // Should fail since target it normal-type -- make sure it didn't steal stats
        Assert.assertTrue(attacking.lastMoveSucceeded());
        battle.attackingFight(AttackNamesies.SPECTRAL_THIEF);
        Assert.assertFalse(attacking.lastMoveSucceeded());
        attacking.assertNoStages();
        defending.assertStages(new TestStages().set(2, Stat.ATTACK, Stat.SP_ATTACK, Stat.SPEED)
                                               .set(-1, Stat.DEFENSE, Stat.SP_DEFENSE));

        defending.withAbility(AbilityNamesies.STURDY);
        battle.fight(AttackNamesies.SOAK, AttackNamesies.GROWL);
        defending.assertNotType(battle, Type.NORMAL);
        attacking.assertStages(new TestStages().set(-1, Stat.ATTACK));
        defending.assertStages(new TestStages().set(2, Stat.ATTACK, Stat.SP_ATTACK, Stat.SPEED)
                                               .set(-1, Stat.DEFENSE, Stat.SP_DEFENSE));

        // Steal stat gains!
        battle.attackingFight(AttackNamesies.SPECTRAL_THIEF);
        attacking.assertStages(new TestStages().set(1, Stat.ATTACK)
                                               .set(2, Stat.SP_ATTACK, Stat.SPEED));
        defending.assertStages(new TestStages().set(-1, Stat.DEFENSE)
                                               .set(-1, Stat.SP_DEFENSE));

        battle.defendingFight(AttackNamesies.SHELL_SMASH);
        attacking.assertStages(new TestStages().set(1, Stat.ATTACK)
                                               .set(2, Stat.SP_ATTACK, Stat.SPEED));
        defending.assertStages(new TestStages().set(2, Stat.ATTACK, Stat.SP_ATTACK, Stat.SPEED)
                                               .set(-2, Stat.DEFENSE, Stat.SP_DEFENSE));

        battle.emptyHeal();

        // Contrary will give stat decreases instead of gains
        attacking.withAbility(AbilityNamesies.CONTRARY);
        battle.attackingFight(AttackNamesies.SPECTRAL_THIEF);
        attacking.assertStages(new TestStages().set(-1, Stat.ATTACK));
        defending.assertStages(new TestStages().set(-2, Stat.DEFENSE, Stat.SP_DEFENSE));

        battle.defendingFight(AttackNamesies.SHELL_SMASH);
        attacking.assertStages(new TestStages().set(-1, Stat.ATTACK));
        defending.assertStages(new TestStages().set(2, Stat.ATTACK, Stat.SP_ATTACK, Stat.SPEED)
                                               .set(-3, Stat.DEFENSE, Stat.SP_DEFENSE));

        battle.emptyHeal();

        // Simple will double the gains!
        attacking.withAbility(AbilityNamesies.SIMPLE);
        battle.attackingFight(AttackNamesies.SPECTRAL_THIEF);
        attacking.assertStages(new TestStages().set(3, Stat.ATTACK)
                                               .set(4, Stat.SP_ATTACK, Stat.SPEED));
        defending.assertStages(new TestStages().set(-3, Stat.DEFENSE, Stat.SP_DEFENSE));

        // TODO: Test Substitute
    }

    @Test
    public void storedPowerTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking();

        attacking.setExpectedDamageModifier(1.0);
        battle.attackingFight(AttackNamesies.STORED_POWER);

        battle.emptyHeal();
        battle.attackingFight(AttackNamesies.MINIMIZE);
        attacking.assertStages(new TestStages().set(2, Stat.EVASION));

        attacking.setExpectedDamageModifier(3.0);
        battle.attackingFight(AttackNamesies.STORED_POWER);

        battle.emptyHeal();
        battle.defendingFight(AttackNamesies.SCREECH);
        attacking.assertStages(new TestStages().set(2, Stat.EVASION).set(-2, Stat.DEFENSE));

        // Stored power ignores negative stat gains
        attacking.setExpectedDamageModifier(3.0);
        battle.attackingFight(AttackNamesies.STORED_POWER);
    }

    @Test
    public void naturalGiftTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.HAPPINY, PokemonNamesies.SYLVEON);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Fails if not holding anything
        attacking.apply(false, AttackNamesies.NATURAL_GIFT, battle);
        defending.assertFullHealth();

        // Non-berry items will fails
        attacking.withItem(ItemNamesies.ABSORB_BULB);
        attacking.apply(false, AttackNamesies.NATURAL_GIFT, battle);
        defending.assertFullHealth();
        attacking.assertNotConsumedItem();

        // Occa Berry (Fire-type) will succeed and be consumed (but not eaten)
        attacking.withItem(ItemNamesies.OCCA_BERRY);
        attacking.apply(true, AttackNamesies.NATURAL_GIFT, battle);
        Assert.assertTrue(attacking.isAttackType(Type.FIRE));
        attacking.assertConsumedItem();
        attacking.assertFullHealth();
        defending.assertNotFullHealth();

        battle.emptyHeal();
        battle.clearAllEffects();
        attacking.assertNoEffect(PokemonEffectNamesies.CONSUMED_ITEM);

        // Haban Berry (Dragon-type) will fail against Sylveon
        attacking.withItem(ItemNamesies.HABAN_BERRY);
        attacking.apply(false, AttackNamesies.NATURAL_GIFT, battle);
        Assert.assertTrue(attacking.isAttackType(Type.DRAGON));
        attacking.assertNotConsumedItem();

        // Oran Berry should not heal when consumed
        battle.falseSwipePalooza(false);
        attacking.assertHp(1);
        attacking.withItem(ItemNamesies.ORAN_BERRY);
        attacking.apply(true, AttackNamesies.NATURAL_GIFT, battle);
        Assert.assertTrue(attacking.isAttackType(Type.POISON));
        attacking.assertConsumedItem(); // Confirms berry not eaten, just consumed
        attacking.assertHp(1);
        defending.assertNotFullHealth();
    }

    @Test
    public void incinerateTest() {
        incinerateTest(ItemNamesies.ABSORB_BULB, false);
        incinerateTest(ItemNamesies.ORAN_BERRY, true);
        incinerateTest(ItemNamesies.FIRE_GEM, true);

        // Will be incinerated if the Fire-type incinerate is not super-effective
        incinerateTest(ItemNamesies.OCCA_BERRY, true);

        // Special case for Occa Berry which will be consumed once hit with the super-effective Fire-type incinerate
        // Should have the Eaten Berry effect in this case
        TestBattle battle = TestBattle.create(PokemonNamesies.CHARMANDER, PokemonNamesies.BULBASAUR);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        defending.withItem(ItemNamesies.OCCA_BERRY);
        attacking.setExpectedDamageModifier(.5);
        battle.fight(AttackNamesies.INCINERATE, AttackNamesies.ENDURE);
        defending.assertNotHoldingItem();
        defending.assertHasEffect(PokemonEffectNamesies.CONSUMED_ITEM);
        defending.assertHasEffect(PokemonEffectNamesies.EATEN_BERRY);
        defending.assertNotFullHealth();
        attacking.assertFullHealth();
    }

    private void incinerateTest(ItemNamesies victimItem, boolean incinerated) {
        PokemonManipulator manipulator = (battle, attacking, defending) -> {
            // Make sure victim does not consume the effects of the berry when eaten (in this example Oran Berry)
            // Use Substitute so Incinerate damage will be absorbed and Oran Berry will not be able to activate naturally before incinerate to death
            battle.defendingFight(AttackNamesies.SUBSTITUTE);
            defending.assertHealthRatio(.75);

            defending.withItem(victimItem);
            battle.attackingFight(AttackNamesies.INCINERATE);
        };

        // Incinerate does not consume when the defending has Sticky Hold
        PokemonManipulator sticksies = (battle, attacking, defending) -> {
            defending.assertAbility(AbilityNamesies.STICKY_HOLD);
            defending.assertNotConsumedItem();
            attacking.assertNoEffect(PokemonEffectNamesies.CONSUMED_ITEM);
            attacking.assertNoEffect(PokemonEffectNamesies.EATEN_BERRY);
            defending.assertHealthRatio(.75);
            attacking.assertFullHealth();
        };

        // If incinerated, the item will be considered consumed for the defending, but will not be 'eaten' for either
        PokemonManipulator nonStick = (battle, attacking, defending) -> {
            Assert.assertNotEquals(incinerated, defending.isHoldingItem());
            defending.assertEffect(incinerated, PokemonEffectNamesies.CONSUMED_ITEM);
            defending.assertNoEffect(PokemonEffectNamesies.EATEN_BERRY);
            attacking.assertNoEffect(PokemonEffectNamesies.CONSUMED_ITEM);
            attacking.assertNoEffect(PokemonEffectNamesies.EATEN_BERRY);
            defending.assertHealthRatio(.75);
            attacking.assertFullHealth();
        };

        new TestInfo(PokemonNamesies.XURKITREE, PokemonNamesies.IGGLYBUFF)
                .with(manipulator)
                .doubleTake(AbilityNamesies.STICKY_HOLD, nonStick, sticksies);

        // Mold Breaker overrides Sticky Hold for Incinerate (should still incinerate)
        PokemonManipulator moldBreaker = PokemonManipulator.giveAttackingAbility(AbilityNamesies.MOLD_BREAKER);
        new TestInfo(PokemonNamesies.XURKITREE, PokemonNamesies.IGGLYBUFF)
                .with(moldBreaker.add(manipulator))
                .doubleTakeSamesies(AbilityNamesies.STICKY_HOLD, nonStick);
    }

    @Test
    public void hiddenPowerTest() {
        hiddenPowerTest(new int[] { 30, 31, 31, 30, 31, 31 }, Type.GRASS);
        hiddenPowerTest(new int[] { 30, 30, 30, 30, 30, 30 }, Type.FIGHTING);
        hiddenPowerTest(new int[] { 31, 31, 31, 31, 31, 31 }, Type.DARK);
        hiddenPowerTest(new int[] { 31, 31, 31, 30, 31, 31 }, Type.ELECTRIC);

        checkHiddenPower(new int[] { 14, 11, 13, 12, 19, 15 }, Type.GRASS);
        checkHiddenPower(new int[] { 31, 31, 30, 30, 30, 30 }, Type.FIGHTING);

        // All even IVs is Fighting-type Hidden Power
        // All odd IVs is Dark-type Hidden Power
        int[] evenIVs = new int[Stat.NUM_STATS];
        int[] oddIVs = new int[Stat.NUM_STATS];
        Arrays.fill(oddIVs, 1);
        for (int i = 0; i < Stat.NUM_STATS; i++) {
            while (oddIVs[i] < IndividualValues.MAX_IV) {
                evenIVs[i] += 2;
                checkHiddenPower(evenIVs, Type.FIGHTING);

                oddIVs[i] += 2;
                checkHiddenPower(oddIVs, Type.DARK);
            }
        }
    }

    private void checkHiddenPower(int[] IVs, Type hiddenType) {
        TestPokemon pokemon = TestPokemon.newPlayerPokemon(PokemonNamesies.BULBASAUR).withIVs(IVs);
        Assert.assertEquals(hiddenType, pokemon.computeHiddenPowerType());
    }

    private void hiddenPowerTest(int[] IVs, Type hiddenType) {
        TestBattle battle = TestBattle.create(PokemonNamesies.BULBASAUR, PokemonNamesies.SANDSHREW);
        TestPokemon attacking = battle.getAttacking().withIVs(IVs);
        TestPokemon defending = battle.getDefending();

        battle.fight(AttackNamesies.HIDDEN_POWER, AttackNamesies.ENDURE);
        Assert.assertTrue(Arrays.toString(IVs) + " " + attacking.getAttackType(), attacking.isAttackType(hiddenType));
        Assert.assertEquals(hiddenType == Type.ELECTRIC, defending.fullHealth());

        battle.emptyHeal();

        // Electrify has priority so it will go first and will change Hidden Power's type to be electric
        // Will then fail against Sandshrew
        battle.fight(AttackNamesies.HIDDEN_POWER, AttackNamesies.ELECTRIFY);
        Assert.assertTrue(attacking.isAttackType(Type.ELECTRIC));
        defending.assertFullHealth();

        // Using again will be back to hidden type
        battle.fight(AttackNamesies.HIDDEN_POWER, AttackNamesies.ENDURE);
        Assert.assertTrue(attacking.isAttackType(hiddenType));
        Assert.assertEquals(hiddenType == Type.ELECTRIC, defending.fullHealth());

        battle.emptyHeal();

        // Normalize makes Hidden Power Normal-type
        attacking.withAbility(AbilityNamesies.NORMALIZE);
        attacking.setExpectedDamageModifier(1.2);
        battle.fight(AttackNamesies.HIDDEN_POWER, AttackNamesies.ENDURE);
        Assert.assertTrue(attacking.isAttackType(Type.NORMAL));
        defending.assertNotFullHealth();

        battle.emptyHeal();

        // Electrify overrides Normalize
        battle.fight(AttackNamesies.HIDDEN_POWER, AttackNamesies.ELECTRIFY);
        Assert.assertTrue(attacking.isAttackType(Type.ELECTRIC));
        defending.assertFullHealth();

        // Double check this override when it is actually effective to make sure Normalize isn't being activated for the 20% boost
        // Yes I realize this here doesn't have much to do with Hidden Power fucking sue me
        battle.attackingFight(AttackNamesies.SOAK);
        defending.assertType(battle, Type.WATER);
        defending.assertNotType(battle, Type.ELECTRIC);
        attacking.setExpectedDamageModifier(1.0);
        battle.fight(AttackNamesies.HIDDEN_POWER, AttackNamesies.ELECTRIFY);
        Assert.assertTrue(attacking.isAttackType(Type.ELECTRIC));
        defending.assertNotFullHealth();
    }

    @Test
    public void powderTest() {
        // Just make sure it works on a non-Grass type
        powderTest(
                AttackNamesies.POWDER, false,
                new TestInfo().attacking(PokemonNamesies.SQUIRTLE),
                (battle, attacking, defending) -> Assert.assertTrue(defending.lastMoveSucceeded())
        );

        // Powder doesn't work on Grass-type Pokemon
        powderTest(
                AttackNamesies.POWDER, false,
                new TestInfo().attacking(PokemonNamesies.BULBASAUR),
                (battle, attacking, defending) -> Assert.assertFalse(defending.lastMoveSucceeded())
        );

        // Powder doesn't work on Pokemon with Overcoat
        powderTest(
                AttackNamesies.POWDER, false,
                new TestInfo().attacking(PokemonNamesies.SQUIRTLE, AbilityNamesies.OVERCOAT),
                (battle, attacking, defending) -> Assert.assertFalse(defending.lastMoveSucceeded())
        );

        // Powder doesn't work on Pokemon with Safety Goggles
        powderTest(
                AttackNamesies.POWDER, false,
                new TestInfo().attacking(PokemonNamesies.SQUIRTLE, ItemNamesies.SAFETY_GOGGLES),
                (battle, attacking, defending) -> Assert.assertFalse(defending.lastMoveSucceeded())
        );

        // Powder only affects Fire-type moves
        powderTest(
                AttackNamesies.WATER_GUN, false, new TestInfo(),
                (battle, attacking, defending) -> attacking.assertNotFullHealth()
        );

        // Using a Fire-type move under the effects of Powder will decrease health by 25%
        powderTest(AttackNamesies.EMBER, true, new TestInfo(), PokemonManipulator.empty());

        // Even if it is a status move
        powderTest(AttackNamesies.WILL_O_WISP, true, new TestInfo(), PokemonManipulator.empty());

        // Fiery Dance will not trigger Dancer
        // Succeeding the .75 health ratio is the real test for this
        powderTest(
                AttackNamesies.FIERY_DANCE, true,
                new TestInfo().attacking(AbilityNamesies.DANCER),
                PokemonManipulator.empty()
        );

        // Natural Gift will fail if holding a Fire-type berry and the berry will not be consumed
        powderTest(
                AttackNamesies.NATURAL_GIFT, true,
                new TestInfo().defending(ItemNamesies.OCCA_BERRY),
                (battle, attacking, defending) -> defending.assertNotConsumedItem()
        );

        // Make sure health does not decrease when the user has Magic Guard, but user is still blocked from using the attack
        powderTest(
                AttackNamesies.EMBER, true,
                new TestInfo().defending(AbilityNamesies.MAGIC_GUARD),
                (battle, attacking, defending) -> {
                    // These are mostly checked already but just want to reinforce in this case
                    defending.assertFullHealth();
                    attacking.assertFullHealth();
                }
        );
    }

    private void powderTest(AttackNamesies attackNamesies, boolean explodes, TestInfo testInfo, PokemonManipulator afterCheck) {
        TestBattle battle = testInfo.createBattle();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();
        testInfo.manipulate(battle);

        battle.fight(AttackNamesies.POWDER, attackNamesies);
        Assert.assertTrue(attacking.lastMoveSucceeded());

        defending.assertHealthRatio(explodes && !defending.hasAbility(AbilityNamesies.MAGIC_GUARD) ? .75 : 1);
        if (explodes) {
            Assert.assertFalse(defending.lastMoveSucceeded());
            attacking.assertFullHealth();
            attacking.assertNoStatus();
            attacking.assertNoStages();
        }

        Assert.assertEquals(explodes, defending.isAttackType(Type.FIRE));

        // Neither will have the effect since it goes away at the end of the turn
        defending.assertNoEffect(PokemonEffectNamesies.POWDER);
        attacking.assertNoEffect(PokemonEffectNamesies.POWDER);

        afterCheck.manipulate(battle);
    }

    @Test
    public void venomDrenchTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Fails if the target isn't poisoned
        battle.attackingFight(AttackNamesies.VENOM_DRENCH);
        Assert.assertFalse(attacking.lastMoveSucceeded());
        defending.assertNoStatus();
        defending.assertNoStages();

        // Add regular poison
        battle.attackingFight(AttackNamesies.POISON_POWDER);
        Assert.assertTrue(attacking.lastMoveSucceeded());
        defending.assertRegularPoison();
        defending.assertNoStages();

        // Venon Drench is a success
        battle.attackingFight(AttackNamesies.VENOM_DRENCH);
        Assert.assertTrue(attacking.lastMoveSucceeded());
        defending.assertRegularPoison();
        defending.assertStages(new TestStages().set(-1, Stat.ATTACK).set(-1, Stat.SP_ATTACK).set(-1, Stat.SPEED));

        // Remove stat changes and status condition
        battle.fight(AttackNamesies.HAZE, AttackNamesies.REFRESH);
        Assert.assertTrue(attacking.lastMoveSucceeded());
        Assert.assertTrue(defending.lastMoveSucceeded());
        defending.assertNoStatus();
        defending.assertNoStages();

        // Add bad poison
        battle.attackingFight(AttackNamesies.TOXIC);
        Assert.assertTrue(attacking.lastMoveSucceeded());
        defending.assertBadPoison();
        defending.assertNoStages();

        // Make sure it works on bad poison as well
        battle.attackingFight(AttackNamesies.VENOM_DRENCH);
        Assert.assertTrue(attacking.lastMoveSucceeded());
        defending.assertBadPoison();
        defending.assertStages(new TestStages().set(-1, Stat.ATTACK).set(-1, Stat.SP_ATTACK).set(-1, Stat.SPEED));
    }

    @Test
    public void breakBarrierTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Give both teams Reflect and Light Screen
        // TODO: This test can be more comprehensive (non-barrier effects not removed etc)
        battle.fight(AttackNamesies.LIGHT_SCREEN, AttackNamesies.REFLECT);
        battle.fight(AttackNamesies.REFLECT, AttackNamesies.LIGHT_SCREEN);
        battle.assertHasEffect(attacking, TeamEffectNamesies.REFLECT);
        battle.assertHasEffect(attacking, TeamEffectNamesies.LIGHT_SCREEN);
        battle.assertHasEffect(defending, TeamEffectNamesies.REFLECT);
        battle.assertHasEffect(defending, TeamEffectNamesies.LIGHT_SCREEN);

        // Only break the opponent barriers
        battle.fight(AttackNamesies.BRICK_BREAK, AttackNamesies.ENDURE);
        battle.assertHasEffect(attacking, TeamEffectNamesies.REFLECT);
        battle.assertHasEffect(attacking, TeamEffectNamesies.LIGHT_SCREEN);
        battle.assertNoEffect(defending, TeamEffectNamesies.REFLECT);
        battle.assertNoEffect(defending, TeamEffectNamesies.LIGHT_SCREEN);

        // Break the players too
        battle.fight(AttackNamesies.ENDURE, AttackNamesies.PSYCHIC_FANGS);
        battle.assertNoEffect(attacking, TeamEffectNamesies.REFLECT);
        battle.assertNoEffect(attacking, TeamEffectNamesies.LIGHT_SCREEN);
        battle.assertNoEffect(defending, TeamEffectNamesies.REFLECT);
        battle.assertNoEffect(defending, TeamEffectNamesies.LIGHT_SCREEN);
    }

    @Test
    public void futureSightTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Super simple test so far that only really checks the most basic future sight mechanics
        battle.attackingFight(AttackNamesies.FUTURE_SIGHT);
        battle.assertNoEffect(attacking, TeamEffectNamesies.FUTURE_SIGHT);
        battle.assertHasEffect(defending, TeamEffectNamesies.FUTURE_SIGHT);
        attacking.assertFullHealth();
        defending.assertFullHealth();

        battle.splashFight();
        battle.assertNoEffect(attacking, TeamEffectNamesies.FUTURE_SIGHT);
        battle.assertHasEffect(defending, TeamEffectNamesies.FUTURE_SIGHT);
        attacking.assertFullHealth();
        defending.assertFullHealth();

        battle.splashFight();
        battle.assertNoEffect(attacking, TeamEffectNamesies.FUTURE_SIGHT);
        battle.assertNoEffect(defending, TeamEffectNamesies.FUTURE_SIGHT);
        attacking.assertFullHealth();
        defending.assertNotFullHealth();
    }

    @Test
    public void meFirstTest() {
        // Basic use-case (player always goes first, so Me First will activate and False Swipe will be 50% stronger)
        meFirstTest(1.5, 1.0, AttackNamesies.ME_FIRST, AttackNamesies.FALSE_SWIPE, (battle, attacking, defending) -> {
            attacking.assertNotFullHealth();
            defending.assertNotFullHealth();

            Assert.assertTrue(attacking.lastMoveSucceeded());
            Assert.assertTrue(defending.lastMoveSucceeded());
        });

        // Me First goes second (failureeee)
        meFirstTest(1.0, null, AttackNamesies.FALSE_SWIPE, AttackNamesies.ME_FIRST, (battle, attacking, defending) -> {
            // False Swipe hits defending
            defending.assertNotFullHealth();
            Assert.assertTrue(attacking.lastMoveSucceeded());

            // Me First fails and attacking is fine
            attacking.assertFullHealth();
            Assert.assertFalse(defending.lastMoveSucceeded());
        });

        // Me First goes first, but opponent is using a status move (failureeee)
        meFirstTest(null, null, AttackNamesies.ME_FIRST, AttackNamesies.GROWL, (battle, attacking, defending) -> {
            // Growl succeeds against player, but Me First does not replicate it
            attacking.assertStages(new TestStages().set(-1, Stat.ATTACK));
            defending.assertStages(new TestStages());

            attacking.assertFullHealth();
            defending.assertFullHealth();

            Assert.assertFalse(attacking.lastMoveSucceeded());
            Assert.assertTrue(defending.lastMoveSucceeded());
        });
    }

    private void meFirstTest(Double attackingModifier, Double defendingModifier, AttackNamesies attackingAttack, AttackNamesies defendingAttack, PokemonManipulator afterCheck) {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        attacking.setExpectedDamageModifier(attackingModifier);
        defending.setExpectedDamageModifier(defendingModifier);
        battle.fight(attackingAttack, defendingAttack);
        afterCheck.manipulate(battle);

        // This effect should not persist once the turn ends
        attacking.assertNoEffect(PokemonEffectNamesies.FIDDY_PERCENT_STRONGER);
        defending.assertNoEffect(PokemonEffectNamesies.FIDDY_PERCENT_STRONGER);
    }

    @Test
    public void sleepTalkTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Growl is the only move that can succeed from Sleep Talk in this list
        attacking.withMoves(AttackNamesies.SLEEP_TALK, AttackNamesies.GROWL, AttackNamesies.UPROAR, AttackNamesies.ASSIST);

        // Will fail because not asleep
        battle.attackingFight(AttackNamesies.SLEEP_TALK);
        attacking.assertNoStatus();
        attacking.assertStages(new TestStages());
        defending.assertStages(new TestStages());
        Assert.assertFalse(attacking.lastMoveSucceeded());

        // Nighty night
        battle.defendingFight(AttackNamesies.SING);
        attacking.assertHasStatus(StatusNamesies.ASLEEP);

        // Okay let's try that again
        battle.attackingFight(AttackNamesies.SLEEP_TALK);
        attacking.assertHasStatus(StatusNamesies.ASLEEP);
        attacking.assertStages(new TestStages());
        defending.assertStages(new TestStages().set(-1, Stat.ATTACK));
        Assert.assertTrue(attacking.lastMoveSucceeded());

        battle.splashFight();
        battle.splashFight();

        // Must be awake by now
        battle.attackingFight(AttackNamesies.GROWL);
        attacking.assertNoStatus();
        attacking.assertStages(new TestStages());
        defending.assertStages(new TestStages().set(-2, Stat.ATTACK));

        // Go back to sleepies
        battle.defendingFight(AttackNamesies.SING);
        attacking.assertHasStatus(StatusNamesies.ASLEEP);

        // Only know moves that fail with Sleep Talk -- will fail
        attacking.withMoves(AttackNamesies.SLEEP_TALK, AttackNamesies.UPROAR, AttackNamesies.ASSIST, AttackNamesies.SOLAR_BEAM);
        battle.attackingFight(AttackNamesies.SLEEP_TALK);
        attacking.assertHasStatus(StatusNamesies.ASLEEP);
        attacking.assertFullHealth();
        defending.assertFullHealth();
        Assert.assertFalse(attacking.lastMoveSucceeded());
    }

    @Test
    public void strengthSapTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.BLISSEY, PokemonNamesies.MAGIKARP);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Set attacking to 1 HP
        battle.falseSwipePalooza(false);
        attacking.assertHp(1);
        defending.assertFullHealth();

        int attackStat = Stat.ATTACK.getBasicStat(battle, defending);
        TestUtils.assertGreater(attacking.getMaxHP(), 2*attackStat);
        Assert.assertEquals(defending.getStat(battle, Stat.ATTACK), attackStat);

        // Steal strengthhh
        battle.attackingFight(AttackNamesies.STRENGTH_SAP);
        attacking.assertHp(1 + attackStat);
        defending.assertFullHealth();
        attacking.assertStages(new TestStages());
        defending.assertStages(new TestStages().set(-1, Stat.ATTACK));

        // Make sure stat value actually decreases
        int newAttackStat = Stat.ATTACK.getBasicStat(battle, defending);
        TestUtils.assertGreater(attackStat, newAttackStat);
        battle.attackingFight(AttackNamesies.STRENGTH_SAP);
        attacking.assertHp(1 + attackStat + newAttackStat);
        defending.assertFullHealth();
        attacking.assertStages(new TestStages());
        defending.assertStages(new TestStages().set(-2, Stat.ATTACK));

        // Heal to full HP
        battle.emptyHeal();
        attacking.assertFullHealth();
        defending.assertFullHealth();
        attacking.assertStages(new TestStages());
        defending.assertStages(new TestStages().set(-2, Stat.ATTACK));

        // Should still reduce attack even when at full health
        battle.attackingFight(AttackNamesies.STRENGTH_SAP);
        attacking.assertFullHealth();
        defending.assertFullHealth();
        attacking.assertStages(new TestStages());
        defending.assertStages(new TestStages().set(-3, Stat.ATTACK));

        // Liquid Ooze will make the attacker lose HP instead of heal -- but it will still lose strength
        attackStat = Stat.ATTACK.getBasicStat(battle, defending);
        defending.withAbility(AbilityNamesies.LIQUID_OOZE);
        battle.attackingFight(AttackNamesies.STRENGTH_SAP);
        attacking.assertMissingHp(attackStat);
        defending.assertFullHealth();
        attacking.assertStages(new TestStages());
        defending.assertStages(new TestStages().set(-4, Stat.ATTACK));

        // Lower attack to minimum
        battle.attackingFight(AttackNamesies.FEATHER_DANCE);
        defending.assertStages(new TestStages().set(-6, Stat.ATTACK));
        defending.withAbility(AbilityNamesies.NO_ABILITY);

        battle.falseSwipePalooza(false);
        attacking.assertHp(1);
        defending.assertFullHealth();

        // Strength Sap will fail if at minimum (should not heal either)
        battle.attackingFight(AttackNamesies.STRENGTH_SAP);
        attacking.assertHp(1);
        defending.assertFullHealth();

        // Contrary will cause Strength Sap to increase its attack -- should succeed at -6 stage
        defending.withAbility(AbilityNamesies.CONTRARY);
        attackStat = Stat.ATTACK.getBasicStat(battle, defending);
        battle.attackingFight(AttackNamesies.STRENGTH_SAP);
        attacking.assertHp(1 + attackStat);
        defending.assertFullHealth();
        attacking.assertStages(new TestStages());
        defending.assertStages(new TestStages().set(-5, Stat.ATTACK));

        // Should not heal if the stat can't be lowered due to ability
        defending.withAbility(AbilityNamesies.HYPER_CUTTER);
        battle.attackingFight(AttackNamesies.STRENGTH_SAP);
        attacking.assertHp(1 + attackStat);
        defending.assertFullHealth();
        attacking.assertStages(new TestStages());
        defending.assertStages(new TestStages().set(-5, Stat.ATTACK));
    }

    @Test
    public void moldBreakerMovesTest() {
        // Tackle does not break the mold -- power should be halved from Multiscale/Shadow Shield
        moldBreakerMovesTest(.5, AttackNamesies.TACKLE, AbilityNamesies.MULTISCALE);
        moldBreakerMovesTest(.5, AttackNamesies.TACKLE, AbilityNamesies.SHADOW_SHIELD);

        // Moongeist Beam always breaks mold regardless of attack order (Shadow Shield still blocks though)
        moldBreakerMovesTest(1, AttackNamesies.MOONGEIST_BEAM, AbilityNamesies.MULTISCALE);
        moldBreakerMovesTest(.5, AttackNamesies.MOONGEIST_BEAM, AbilityNamesies.SHADOW_SHIELD);

        // Photon Geyser same deal as Moongeist Beam
        moldBreakerMovesTest(1, AttackNamesies.PHOTON_GEYSER, AbilityNamesies.MULTISCALE);
        moldBreakerMovesTest(.5, AttackNamesies.PHOTON_GEYSER, AbilityNamesies.SHADOW_SHIELD);
    }

    private void moldBreakerMovesTest(double expectedModifier, AttackNamesies attackNamesies, AbilityNamesies otherAbility) {
        TestBattle battle = TestBattle.create(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        defending.withAbility(otherAbility);
        attacking.setExpectedDamageModifier(expectedModifier);

        battle.fight(attackNamesies, AttackNamesies.ENDURE);
        defending.assertNotFullHealth();
    }

    @Test
    public void photonGeyserTest() {
        // Kee Berry increases Defense when hit by a Physical move
        photonGeyserTest(
                (battle, attacking, defending) -> defending.withItem(ItemNamesies.KEE_BERRY),
                (battle, attacking, defending) -> {
                    defending.assertConsumedBerry();
                    defending.assertStages(new TestStages().set(1, Stat.DEFENSE));

                    attacking.assertFullHealth();
                    attacking.assertStages(new TestStages());
                },
                (battle, attacking, defending) -> {
                    defending.assertNotConsumedItem();
                    defending.assertStages(new TestStages());

                    attacking.assertFullHealth();
                    attacking.assertStages(new TestStages());
                }
        );

        // Maranga Berry increases Defense when hit by a Physical move
        photonGeyserTest(
                (battle, attacking, defending) -> defending.withItem(ItemNamesies.MARANGA_BERRY),
                (battle, attacking, defending) -> {
                    defending.assertNotConsumedItem();
                    defending.assertStages(new TestStages());

                    attacking.assertFullHealth();
                    attacking.assertStages(new TestStages());
                },
                (battle, attacking, defending) -> {
                    defending.assertConsumedBerry();
                    defending.assertStages(new TestStages().set(1, Stat.SP_DEFENSE));

                    attacking.assertFullHealth();
                    attacking.assertStages(new TestStages());
                }
        );

        // Counter should only succeed if using the attack stat
        photonGeyserTest(
                AttackNamesies.COUNTER,
                (battle, attacking, defending) -> {
                    // TODO: This isn't working because it sets the category back at the end of the move, before Counter is activated
//                    Assert.assertTrue(defending.lastMoveSucceeded());
//                    attacking.assertNotFullHealth();
                },
                (battle, attacking, defending) -> {
                    Assert.assertFalse(defending.lastMoveSucceeded());
                    attacking.assertFullHealth();
                }
        );

        // Mirror Coat should only succeed if using the special attack stat
        photonGeyserTest(
                AttackNamesies.MIRROR_COAT,
                (battle, attacking, defending) -> {
                    // Same deal as above
//                    Assert.assertFalse(defending.lastMoveSucceeded());
//                    attacking.assertFullHealth();
                },
                (battle, attacking, defending) -> {
                    Assert.assertTrue(defending.lastMoveSucceeded());
                    attacking.assertNotFullHealth();
                }
        );
    }

    private void photonGeyserTest(AttackNamesies otherAttack, PokemonManipulator physical, PokemonManipulator special) {
        photonGeyserTest(otherAttack, PokemonManipulator.empty(), physical, special);
    }

    private void photonGeyserTest(PokemonManipulator manipulator, PokemonManipulator physical, PokemonManipulator special) {
        photonGeyserTest(AttackNamesies.ENDURE, manipulator, physical, special);
    }

    private void photonGeyserTest(AttackNamesies otherAttack, PokemonManipulator manipulator, PokemonManipulator physical, PokemonManipulator special) {
        photonGeyserTest(true, otherAttack, manipulator, physical);
        photonGeyserTest(false, otherAttack, manipulator, special);
    }

    private void photonGeyserTest(boolean attackHigher, AttackNamesies otherAttack, PokemonManipulator manipulator, PokemonManipulator afterCheck) {
        final PokemonNamesies pokemon;
        final Stat higher, lower;
        if (attackHigher) {
            // Krabby for Attack > Sp. Attack
            pokemon = PokemonNamesies.KRABBY;
            higher = Stat.ATTACK;
            lower = Stat.SP_ATTACK;
        } else {
            // Abra for Sp. Attack > Attack
            // Note: Alakazam was occasionally too strong and with a crit could kill Shuckie
            pokemon = PokemonNamesies.ABRA;
            higher = Stat.SP_ATTACK;
            lower = Stat.ATTACK;
        }

        TestBattle battle = TestBattle.create(pokemon, PokemonNamesies.SHUCKLE);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending().withAbility(AbilityNamesies.STURDY);

        manipulator.manipulate(battle);

        // Confirm higher stat is actually higher since that's super important
        TestUtils.assertGreater(higher.getBasicStat(battle, attacking), lower.getBasicStat(battle, attacking));

        battle.fight(AttackNamesies.PHOTON_GEYSER, otherAttack);
        afterCheck.manipulate(battle);

        defending.assertNotFullHealth();

        // Photon Geyser always succeeds
        Move lastMoveUsed = attacking.getLastMoveUsed();
        Assert.assertNotNull(lastMoveUsed);
        Assert.assertTrue(attacking.lastMoveSucceeded());

        // Photon Geyser should always remain Special regardless of category used in fight
        Attack photonGeyser = lastMoveUsed.getAttack();
        Assert.assertEquals(AttackNamesies.PHOTON_GEYSER, photonGeyser.namesies());
        Assert.assertEquals(MoveCategory.SPECIAL, photonGeyser.getCategory());
    }

    @Test
    public void darkVoidTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.DARKRAI, PokemonNamesies.DITTO);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Ditto cannot use Dark Void because not a Darkrai
        battle.defendingFight(AttackNamesies.DARK_VOID);
        attacking.assertNoStatus();
        defending.assertNoStatus();
        Assert.assertFalse(defending.lastMoveSucceeded());

        // BUT DARKRAI CAN
        battle.attackingFight(AttackNamesies.DARK_VOID);
        attacking.assertNoStatus();
        defending.assertHasStatus(StatusNamesies.ASLEEP);

        // Wakkeeeee uppppppp
        battle.emptyHeal();
        attacking.assertNoStatus();
        defending.assertNoStatus();

        // Ditto now has Magic Bounce which should reflect the Dark Void back to Darkrai (even though Ditto no Darky)
        defending.withAbility(AbilityNamesies.MAGIC_BOUNCE);
        battle.attackingFight(AttackNamesies.DARK_VOID);
        attacking.assertHasStatus(StatusNamesies.ASLEEP);
        defending.assertNoStatus();

        battle.emptyHeal();
        attacking.assertNoStatus();
        defending.assertNoStatus();
        defending.assertSpecies(PokemonNamesies.DITTO);
        defending.withAbility(AbilityNamesies.NO_ABILITY);

        // Shouldn't reflect when it doesn't work
        attacking.withAbility(AbilityNamesies.MAGIC_BOUNCE);
        battle.defendingFight(AttackNamesies.DARK_VOID);
        attacking.assertNoStatus();
        defending.assertNoStatus();
        Assert.assertFalse(defending.lastMoveSucceeded());
        attacking.withAbility(AbilityNamesies.NO_ABILITY);

        // Who's not a Darkrai now?????? (Not Ditto)
        battle.defendingFight(AttackNamesies.TRANSFORM);
        defending.assertSpecies(PokemonNamesies.DARKRAI);
        attacking.assertNoStatus();
        defending.assertNoStatus();

        // (Transformed) Darkrai should work
        battle.defendingFight(AttackNamesies.DARK_VOID);
        attacking.assertHasStatus(StatusNamesies.ASLEEP);
        defending.assertNoStatus();

        battle.emptyHeal();
        attacking.assertNoStatus();
        defending.assertNoStatus();
        defending.assertSpecies(PokemonNamesies.DARKRAI);
    }

    @Test
    public void jawLockTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE);
        TestPokemon attacking1 = battle.getAttacking();
        TestPokemon attacking2 = battle.addAttacking(PokemonNamesies.SHUCKLE);

        // Make sure correct Pokemon are out front and they can escape just fine
        jawLockTest(battle, attacking1, false);

        // Lock them by the jaw! (Neither can escape)
        battle.attackingFight(AttackNamesies.JAW_LOCK);
        jawLockTest(battle, attacking1, true);

        // Self-switching should still work, but it should remove the effect
        battle.attackingFight(AttackNamesies.BATON_PASS);
        jawLockTest(battle, attacking2, false);

        // Lock 'em up again!
        battle.defendingFight(AttackNamesies.JAW_LOCK);
        jawLockTest(battle, attacking2, true);

        // Swapping with whirlwind should also be okay (but still removes the lock)
        battle.defendingFight(AttackNamesies.WHIRLWIND);
        jawLockTest(battle, attacking1, false);

        // Locky locky locked
        battle.attackingFight(AttackNamesies.JAW_LOCK);
        jawLockTest(battle, attacking1, true);

        // Kill kill kill murder murder murder (removes Jaw Lock)
        battle.defendingFight(AttackNamesies.FISSURE);
        Assert.assertTrue(attacking1.isFainted(battle));
        jawLockTest(battle, attacking1, false);
    }

    private void jawLockTest(TestBattle battle, TestPokemon attacking, boolean jawLocked) {
        TestPokemon defending = battle.getDefending();
        battle.assertEffect(jawLocked, StandardBattleEffectNamesies.JAW_LOCKED);
        battle.assertFront(attacking);
        battle.assertFront(defending);
        Assert.assertEquals(!jawLocked, attacking.canEscape(battle));
        Assert.assertEquals(!jawLocked, defending.canEscape(battle));
    }

    // Stuff Cheeks and Teatime
    // Doing these together because they have very similar test cases (can be separated if necessary though)
    @Test
    public void forceBerryTest() {
        // Holding non-berries will fail
        forceBerryTest(false, ItemNamesies.NO_ITEM, PokemonManipulator.empty(), PokemonManipulator.empty());
        forceBerryTest(false, ItemNamesies.POTION, PokemonManipulator.empty(), PokemonManipulator.empty());

        // Berries without gainable effects should still be consumed
        forceBerryTest(true, ItemNamesies.POMEG_BERRY, PokemonManipulator.empty(), PokemonManipulator.empty());
        Assert.assertFalse(ItemNamesies.POMEG_BERRY.getItem() instanceof GainableEffectBerry);

        // Oran Berry with full HP -- should still consume and raise defense even though nothing happens
        forceBerryTest(
                true, ItemNamesies.ORAN_BERRY,
                (battle, attacking, defending) -> attacking.assertFullHealth(),
                (battle, attacking, defending) -> attacking.assertFullHealth()
        );

        // Oran Berry with reduced HP -- reduce health to 11, restore by 10 to be missing 1
        forceBerryTest(
                true, ItemNamesies.ORAN_BERRY,
                (battle, attacking, defending) -> {
                    attacking.reduceHealth(battle, 11);
                    attacking.assertMissingHp(11);
                },
                (battle, attacking, defending) -> attacking.assertMissingHp(1)
        );

        // Rawst Berry -- burn, eating should cure
        forceBerryTest(
                true, ItemNamesies.RAWST_BERRY,
                (battle, attacking, defending) -> {
                    battle.defendingFight(AttackNamesies.WILL_O_WISP);
                    attacking.assertHasStatus(StatusNamesies.BURNED);
                },
                (battle, attacking, defending) -> attacking.assertNoStatus()
        );

        // Rawst Berry with maxed Defense -- should still consume and cure burn even if not increasing stats
        forceBerryTest(
                true, ItemNamesies.RAWST_BERRY,
                (battle, attacking, defending) -> {
                    for (int i = 1; i <= Stages.MAX_STAT_CHANGES; i++) {
                        battle.attackingFight(AttackNamesies.DEFENSE_CURL);
                        attacking.assertStages(new TestStages().set(i, Stat.DEFENSE));
                    }

                    battle.defendingFight(AttackNamesies.WILL_O_WISP);
                    attacking.assertHasStatus(StatusNamesies.BURNED);
                    attacking.assertStages(new TestStages().set(Stages.MAX_STAT_CHANGES, Stat.DEFENSE));
                },
                (battle, attacking, defending) -> {
                    attacking.assertStages(new TestStages().set(Stages.MAX_STAT_CHANGES, Stat.DEFENSE));
                    attacking.assertNoStatus();
                }
        );

        // Pecha Berry with Cheek Pouch -- Cure + heal
        // Reduce to 50% health, poison takes another 1/8, Cheek Pouch heals 33% = 17/24?
        // Note: Can't use paralysis because you can sometimes be fully paralyzed when using Stuff Cheeks
        forceBerryTest(
                true, ItemNamesies.PECHA_BERRY,
                (battle, attacking, defending) -> {
                    attacking.withAbility(AbilityNamesies.CHEEK_POUCH);
                    attacking.reduceHealthFraction(battle, .5, "");
                    attacking.assertHealthRatio(.5);

                    battle.defendingFight(AttackNamesies.POISON_POWDER);
                    attacking.assertRegularPoison();
                },
                (battle, attacking, defending) -> {
                    attacking.assertNoStatus();
                    attacking.assertHealthRatio(17/24.0, 1);
                }
        );

        // Rawst Berry with Snatch -- burn both, enemy should snatch the berry and cure only them
        // Should succeed even though the snatcher isn't holding anything
        // Passing false for success because succeeding for the defending, not the attacking
        // Note: Only Stuff Cheeks here since no Snatch interaction for Teatime
        stuffCheeksTest(
                false, ItemNamesies.RAWST_BERRY, AttackNamesies.SNATCH,
                (battle, attacking, defending) -> {
                    battle.fight(AttackNamesies.WILL_O_WISP, AttackNamesies.WILL_O_WISP);
                    attacking.assertHasStatus(StatusNamesies.BURNED);
                    defending.assertHasStatus(StatusNamesies.BURNED);
                },
                (battle, attacking, defending) -> {
                    // Attacking still burned, but defending was cured by snatching!
                    attacking.assertHasStatus(StatusNamesies.BURNED);
                    defending.assertNoStatus();

                    // Neither is holding an item
                    attacking.assertNotHoldingItem();
                    defending.assertNotHoldingItem();

                    // Only the defending has eaten a berry
                    attacking.assertNoEffect(PokemonEffectNamesies.EATEN_BERRY);
                    defending.assertHasEffect(PokemonEffectNamesies.EATEN_BERRY);

                    // Only the attacking has a consumed item
                    attacking.assertHasEffect(PokemonEffectNamesies.CONSUMED_ITEM);
                    defending.assertNoEffect(PokemonEffectNamesies.CONSUMED_ITEM);

                    // Only the defending has its stats increased
                    attacking.assertStages(new TestStages());
                    defending.assertStages(new TestStages().set(2, Stat.DEFENSE));
                }
        );
    }

    private void forceBerryTest(boolean success, ItemNamesies heldItem, PokemonManipulator beforeCheck, PokemonManipulator afterCheck) {
        forceBerryTest(success, heldItem, AttackNamesies.SPLASH, beforeCheck, afterCheck);
    }

    private void forceBerryTest(boolean success, ItemNamesies heldItem, AttackNamesies defendingAttack, PokemonManipulator beforeCheck, PokemonManipulator afterCheck) {
        stuffCheeksTest(success, heldItem, defendingAttack, beforeCheck, afterCheck);
        teatimeTest(heldItem, heldItem, beforeCheck, afterCheck);
        teatimeTest(heldItem, ItemNamesies.NO_ITEM, beforeCheck, afterCheck);
    }

    private void stuffCheeksTest(boolean success, ItemNamesies heldItem, AttackNamesies defendingAttack, PokemonManipulator beforeCheck, PokemonManipulator afterCheck) {
        TestBattle battle = TestBattle.create(PokemonNamesies.EEVEE, PokemonNamesies.EEVEE);
        TestPokemon attacking = battle.getAttacking();
        attacking.assertStages(new TestStages());

        // Set up this case and give them the item
        beforeCheck.manipulate(battle);
        attacking.withItem(heldItem);

        // So we know what to compare the defense increase to
        TestStages stages = attacking.testStages();
        if (success) {
            // Increase Defense by two stages (not exceeding maximum)
            stages.increment(2, Stat.DEFENSE);

            // Can only succeed if holding a berry
            Assert.assertTrue(attacking.getHeldItem() instanceof Berry);
        }

        // Okay let's actually stuff our cheeks with berries or something
        battle.fight(AttackNamesies.STUFF_CHEEKS, defendingAttack);
        Assert.assertEquals(success || defendingAttack == AttackNamesies.SNATCH, attacking.lastMoveSucceeded());
        attacking.assertStages(stages);

        // If successful, make sure berry was consumed
        if (success) {
            attacking.assertConsumedBerry();
            attacking.assertNotHoldingItem();
        }

        // Anything else that might be interesting
        afterCheck.manipulate(battle);
    }

    private void teatimeTest(ItemNamesies attackingItem, ItemNamesies defendingItem, PokemonManipulator beforeTea, PokemonManipulator afterTea) {
        TestBattle battle = TestBattle.create(PokemonNamesies.EEVEE, PokemonNamesies.EEVEE);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        beforeTea.manipulate(battle);
        attacking.withItem(attackingItem);
        defending.withItem(defendingItem);

        battle.attackingFight(AttackNamesies.TEATIME);
        checkTeatime(attacking, attackingItem);
        checkTeatime(defending, defendingItem);

        afterTea.manipulate(battle);
    }

    private void checkTeatime(TestPokemon teaDrinker, ItemNamesies original) {
        Item item = original.getItem();
        if (item instanceof Berry) {
            teaDrinker.assertConsumedBerry();
        } else {
            teaDrinker.assertHoldingItem(original);
        }
    }

    @Test
    public void tarShotTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.BLISSEY, PokemonNamesies.BLISSEY);
        tarShotTest(battle, 0);

        battle.attackingFight(AttackNamesies.TAR_SHOT);
        tarShotTest(battle, 1);

        battle.emptyHeal();

        battle.attackingFight(AttackNamesies.TAR_SHOT);
        tarShotTest(battle, 2);
    }

    private void tarShotTest(TestBattle battle, int numTar) {
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // No effects should actually be on the attacker
        attacking.assertStages(new TestStages());
        attacking.assertNoEffect(PokemonEffectNamesies.STICKY_TAR);

        // Speed decrease should stack with the tar
        defending.assertStages(new TestStages().set(-numTar, Stat.SPEED));
        defending.assertEffect(numTar > 0, PokemonEffectNamesies.STICKY_TAR);

        // Damage multiplier should only happen once though (even with multiple tar shots)
        attacking.setExpectedDamageModifier(numTar == 0 ? 1.0 : 2.0);
        battle.fight(AttackNamesies.INCINERATE, AttackNamesies.ENDURE);
    }

    @Test
    public void reflectTypeTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        reflectTypeTest(battle, attacking, Type.GRASS, Type.POISON);
        reflectTypeTest(battle, defending, Type.FIRE);

        // Turn Charmander into Bulby types
        battle.defendingFight(AttackNamesies.REFLECT_TYPE);
        reflectTypeTest(battle, attacking, Type.GRASS, Type.POISON);
        reflectTypeTest(battle, defending, Type.GRASS, Type.POISON);

        // Now Charmander is covered in Water and therefore is LIKE WATER
        battle.attackingFight(AttackNamesies.SOAK);
        reflectTypeTest(battle, attacking, Type.GRASS, Type.POISON);
        reflectTypeTest(battle, defending, Type.WATER);

        // Bulby can Reflect Type into a changed type no problem
        battle.attackingFight(AttackNamesies.REFLECT_TYPE);
        reflectTypeTest(battle, attacking, Type.WATER);
        reflectTypeTest(battle, defending, Type.WATER);

        // Jk don't want any of that anymore
        battle.clearAllEffects();
        reflectTypeTest(battle, attacking, Type.GRASS, Type.POISON);
        reflectTypeTest(battle, defending, Type.FIRE);

        // BURN UP THAT FLAME STUFF
        battle.fight(AttackNamesies.ENDURE, AttackNamesies.BURN_UP);
        reflectTypeTest(battle, attacking, Type.GRASS, Type.POISON);
        reflectTypeTest(battle, defending, Type.NO_TYPE);

        // Reflect Type should fail against non-types
        battle.attackingFight(AttackNamesies.REFLECT_TYPE);
        Assert.assertFalse(attacking.lastMoveSucceeded());
        reflectTypeTest(battle, attacking, Type.GRASS, Type.POISON);
        reflectTypeTest(battle, defending, Type.NO_TYPE);
    }

    private void reflectTypeTest(TestBattle battle, TestPokemon p, Type soloType) {
        reflectTypeTest(battle, p, soloType, Type.NO_TYPE);
    }

    private void reflectTypeTest(TestBattle battle, TestPokemon p, Type firstType, Type secondType) {
        p.assertType(battle, firstType);
        p.assertType(battle, secondType);

        PokeType type = p.getType(battle);
        Assert.assertEquals(type, new PokeType(firstType, secondType));
        Assert.assertEquals(firstType, type.getFirstType());
        Assert.assertEquals(secondType, type.getSecondType());
        Assert.assertEquals(secondType != Type.NO_TYPE, type.isDualTyped());

        // If the current type is different that the base Pokemon type then it should have this effect
        PokeType baseType = p.getPokemonInfo().getType();
        boolean sameType = type.equals(baseType);
        p.assertEffect(!sameType, PokemonEffectNamesies.CHANGE_TYPE);

        // If type has been changed, make sure they return false for old type (unless there is overlap)
        Type firstBase = baseType.getFirstType();
        Type secondBase = baseType.getSecondType();
        Assert.assertEquals(type.isType(firstBase), p.isType(battle, firstBase));
        Assert.assertEquals(type.isType(secondBase), p.isType(battle, secondBase));
    }
}
