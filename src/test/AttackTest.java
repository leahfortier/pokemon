package test;

import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.effect.generic.EffectNamesies;
import battle.effect.status.StatusCondition;
import org.junit.Assert;
import org.junit.Test;
import pokemon.Gender;
import pokemon.PokemonNamesies;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import type.Type;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public class AttackTest {
    @Test
    public void recoilTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking().withAbility(AbilityNamesies.ROCK_HEAD);
        TestPokemon defending = battle.getDefending();

        battle.attackingFight(AttackNamesies.TAKE_DOWN);
        Assert.assertTrue(attacking.fullHealth());
        Assert.assertFalse(defending.fullHealth());

        defending.fullyHeal();
        Assert.assertTrue(defending.fullHealth());

        battle.defendingFight(AttackNamesies.WOOD_HAMMER);
        Assert.assertFalse(attacking.fullHealth());
        Assert.assertFalse(defending.fullHealth());

        int damage = attacking.getMaxHP() - attacking.getHP();
        Assert.assertTrue(defending.getMaxHP() - defending.getHP() == (int)(Math.ceil(damage/3.0)));

        attacking.fullyHeal();
        defending.fullyHeal();
        defending.withAbility(AbilityNamesies.MAGIC_GUARD);
        battle.defendingFight(AttackNamesies.WOOD_HAMMER);
        Assert.assertTrue(defending.fullHealth());

        // Struggle should still cause recoil damage even if they have Rock Head/Magic Guard
        attacking.fullyHeal();
        battle.attackingFight(AttackNamesies.STRUGGLE);
        Assert.assertTrue(attacking.hasAbility(AbilityNamesies.ROCK_HEAD));
        Assert.assertFalse(attacking.fullHealth());
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
        TestBattle battle = TestBattle.create(PokemonNamesies.MAGIKARP, PokemonNamesies.DRAGONITE);
        TestPokemon defending = battle.getDefending();

        // Ground type should not effect
        battle.attackingFight(AttackNamesies.FISSURE);
        Assert.assertTrue(defending.fullHealth());

        // OHKO,MF
        battle.attackingFight(AttackNamesies.HORN_DRILL);
        Assert.assertTrue(defending.isFainted(battle));

        // Sturdy prevents OHKO
        defending.fullyHeal();
        defending.withAbility(AbilityNamesies.STURDY);
        battle.attackingFight(AttackNamesies.SHEER_COLD);
        Assert.assertTrue(defending.fullHealth());

        defending.withAbility(AbilityNamesies.NO_ABILITY);
        battle.attackingFight(AttackNamesies.SHEER_COLD);
        Assert.assertTrue(defending.isFainted(battle));

        defending = new TestPokemon(PokemonNamesies.GLACEON);
        battle.attackingFight(AttackNamesies.SHEER_COLD);
        Assert.assertTrue(defending.fullHealth());
    }

    @Test
    public void selfSwitchingMoves() {
        TestPokemon attacking1 = new TestPokemon(PokemonNamesies.CHANSEY);
        TestPokemon attacking2 = new TestPokemon(PokemonNamesies.HAPPINY);
        TestPokemon defending = new TestPokemon(PokemonNamesies.SHUCKLE);

        TestBattle battle = TestBattle.createTrainerBattle(attacking1, defending);
        battle.getPlayer().addPokemon(attacking2);

        Assert.assertTrue(battle.getPlayer().front() == attacking1);

        // Use U-Turn -- make sure they swap
        battle.attackingFight(AttackNamesies.U_TURN);
        Assert.assertTrue(battle.getPlayer().front() == attacking2);

        // TODO: Baton Pass

        // TODO: No more remaining Pokemon, wild battles, wimp out, red card, eject button
    }

    @Test
    public void swapOpponentMoves() {
        TestPokemon attacking1 = new TestPokemon(PokemonNamesies.STEELIX);
        TestPokemon attacking2 = new TestPokemon(PokemonNamesies.REGIROCK);
        TestPokemon defending = new TestPokemon(PokemonNamesies.SHUCKLE);

        TestBattle battle = TestBattle.createTrainerBattle(attacking1, defending);
        battle.getPlayer().addPokemon(attacking2);

        Assert.assertTrue(battle.getAttacking() == attacking1);

        // Use Dragon Tail -- make sure they swap
        battle.fight(AttackNamesies.ENDURE, AttackNamesies.DRAGON_TAIL);
        Assert.assertTrue(battle.getAttacking() == attacking2);

        // Don't swap with Suction Cups
        attacking2.withAbility(AbilityNamesies.SUCTION_CUPS);
        battle.fight(AttackNamesies.ENDURE, AttackNamesies.CIRCLE_THROW);
        Assert.assertTrue(battle.getAttacking() == attacking2);

        attacking2.withAbility(AbilityNamesies.NO_ABILITY);
        battle.fight(AttackNamesies.ENDURE, AttackNamesies.ROAR);
        Assert.assertTrue(battle.getAttacking() == attacking1);

        // Don't swap when ingrained
        battle.fight(AttackNamesies.INGRAIN, AttackNamesies.WHIRLWIND);
        Assert.assertTrue(battle.getAttacking() == attacking1);

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
        Assert.assertTrue(attacking.getAttributes().getStage(Stat.ATTACK) == 1);
        Assert.assertTrue(attacking.getAttributes().getStage(Stat.DEFENSE) == 1);
        Assert.assertTrue(attacking.getAttributes().getStage(Stat.SPEED) == -1);
        Assert.assertFalse(attacking.hasEffect(EffectNamesies.CURSE));
        Assert.assertFalse(defending.hasEffect(EffectNamesies.CURSE));

        // Add Ghost Type
        battle.defendingFight(AttackNamesies.TRICK_OR_TREAT);
        Assert.assertTrue(attacking.isType(battle, Type.GHOST));
        Assert.assertTrue(attacking.fullHealth());
        Assert.assertTrue(defending.fullHealth());

        // Make sure stat changes remain the same and target gets curse effect
        battle.attackingFight(AttackNamesies.CURSE);
        Assert.assertTrue(attacking.getAttributes().getStage(Stat.ATTACK) == 1);
        Assert.assertTrue(attacking.getAttributes().getStage(Stat.DEFENSE) == 1);
        Assert.assertTrue(attacking.getAttributes().getStage(Stat.SPEED) == -1);
        Assert.assertFalse(attacking.hasEffect(EffectNamesies.CURSE));
        Assert.assertTrue(defending.hasEffect(EffectNamesies.CURSE));
        Assert.assertTrue(TestUtil.healthRatioMatch(attacking, .5));
        Assert.assertTrue(TestUtil.healthRatioMatch(defending, .75));
    }

    // Used for attacks that have a random element to them -- like Tri-Attack and Acupressure -- required running several times
    @Test
    public void randomEffectsTest() {
        boolean triAttackAlwaysSame = true;
        Map<StatusCondition, Boolean> triAttackStatusMap = new EnumMap<>(StatusCondition.class);
        triAttackStatusMap.put(StatusCondition.NO_STATUS, false);
        triAttackStatusMap.put(StatusCondition.PARALYZED, false);
        triAttackStatusMap.put(StatusCondition.BURNED, false);
        triAttackStatusMap.put(StatusCondition.FROZEN, false);

        boolean acupressureAlwaysSame = true;
        boolean[] acupressureStats = new boolean[Stat.NUM_BATTLE_STATS];

        for (int i = 0; i < 1000; i++) {
            TestBattle battle = TestBattle.create(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE);
            TestPokemon attacking = battle.getAttacking();
            TestPokemon defending = battle.getDefending();

            // Tri-Attack
            battle.fight(AttackNamesies.TRI_ATTACK, AttackNamesies.TRI_ATTACK);

            StatusCondition attackingCondition = attacking.getStatus().getType();
            StatusCondition defendingCondition = defending.getStatus().getType();

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
            for (int j = 0; j < acupressureStats.length; j++) {
                int attackingStage = attacking.getStage(j);
                int defendingStage = defending.getStage(j);
                if (attackingStage != defendingStage) {
                    acupressureAlwaysSame = false;
                }

                if (attackingStage > 0) {
                    Assert.assertFalse(foundAttacking);
                    acupressureStats[j] = true;
                    foundAttacking = true;
                }

                if (defendingStage > 0) {
                    Assert.assertFalse(foundDefending);
                    acupressureStats[j] = true;
                    foundDefending = true;
                }
            }
        }

        Assert.assertFalse(triAttackAlwaysSame);
        Assert.assertTrue(triAttackStatusMap.get(StatusCondition.NO_STATUS));
        Assert.assertTrue(triAttackStatusMap.get(StatusCondition.PARALYZED));
        Assert.assertTrue(triAttackStatusMap.get(StatusCondition.BURNED));
        Assert.assertTrue(triAttackStatusMap.get(StatusCondition.FROZEN));

        Assert.assertFalse(acupressureAlwaysSame);
        for (int j = 0; j < acupressureStats.length; j++) {
            Assert.assertTrue(Stat.getStat(j, true).getName(), acupressureStats[j]);
        }
    }

    @Test
    public void roostTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.DRAGONITE, PokemonNamesies.MAGIKARP);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Should fail with full hp
        Assert.assertTrue(attacking.isType(battle, Type.FLYING));
        attacking.apply(false, AttackNamesies.ROOST, battle);
        Assert.assertTrue(attacking.isType(battle, Type.FLYING));

        // Reduce health and apply again
        battle.attackingFight(AttackNamesies.BELLY_DRUM);
        Assert.assertFalse(attacking.fullHealth());
        attacking.apply(true, AttackNamesies.ROOST, battle);
        Assert.assertFalse(attacking.isType(battle, Type.FLYING));
        Assert.assertTrue(attacking.fullHealth());

        // Should fail because attack is already maxed -- flying type should come back at the end of the turn
        battle.attackingFight(AttackNamesies.BELLY_DRUM);
        Assert.assertTrue(attacking.fullHealth());
        Assert.assertTrue(attacking.isType(battle, Type.FLYING));

        // Clear stat changes and reduce again
        Assert.assertTrue(attacking.getStage(Stat.ATTACK.index()) == Stat.MAX_STAT_CHANGES);
        battle.attackingFight(AttackNamesies.HAZE);
        Assert.assertTrue(attacking.getStage(Stat.ATTACK.index()) == 0);
        battle.attackingFight(AttackNamesies.BELLY_DRUM);
        Assert.assertTrue(attacking.getStage(Stat.ATTACK.index()) == Stat.MAX_STAT_CHANGES);

        // Using a full turn should bring the flying type back at the end
        Assert.assertFalse(attacking.fullHealth());
        battle.attackingFight(AttackNamesies.ROOST);
        Assert.assertTrue(attacking.isType(battle, Type.FLYING));
        Assert.assertTrue(attacking.fullHealth());

        defending.apply(false, AttackNamesies.MUD_SLAP, battle);
        defending.apply(true, AttackNamesies.TACKLE, battle);
        Assert.assertFalse(attacking.fullHealth());
        attacking.apply(true, AttackNamesies.ROOST, battle);
        Assert.assertTrue(attacking.fullHealth());
        defending.apply(true, AttackNamesies.MUD_SLAP, battle);
        Assert.assertFalse(attacking.fullHealth());
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
        Move tackle = attacking.getMove(battle, 0);
        attacking.setMove(tackle);
        Assert.assertFalse(tackle.used());
        battle.fight();
        Assert.assertTrue(tackle.used());
        attacking.apply(true, AttackNamesies.LAST_RESORT, battle);
    }

    @Test
    public void psychoShift() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking().withAbility(AbilityNamesies.MAGIC_GUARD);
        TestPokemon defending = battle.getDefending().withAbility(AbilityNamesies.MAGIC_GUARD);

        battle.fight(AttackNamesies.SOAK, AttackNamesies.SOAK);
        Assert.assertTrue(attacking.isType(battle, Type.WATER));
        Assert.assertTrue(defending.isType(battle, Type.WATER));

        attacking.apply(false, AttackNamesies.PSYCHO_SHIFT, battle);
        battle.attackingFight(AttackNamesies.WILL_O_WISP);
        Assert.assertFalse(attacking.hasStatus());
        Assert.assertTrue(defending.hasStatus(StatusCondition.BURNED));
        attacking.apply(false, AttackNamesies.PSYCHO_SHIFT, battle);
        defending.apply(true, AttackNamesies.PSYCHO_SHIFT, battle);
        Assert.assertTrue(attacking.hasStatus(StatusCondition.BURNED));
        Assert.assertFalse(defending.hasStatus());

        battle.attackingFight(AttackNamesies.TOXIC);
        Assert.assertTrue(defending.hasStatus(StatusCondition.POISONED));
        Assert.assertTrue(defending.hasEffect(EffectNamesies.BAD_POISON));

        attacking.apply(false, AttackNamesies.PSYCHO_SHIFT, battle);
        defending.apply(false, AttackNamesies.PSYCHO_SHIFT, battle);
        Assert.assertTrue(attacking.hasStatus(StatusCondition.BURNED));
        Assert.assertTrue(defending.hasStatus(StatusCondition.POISONED));

        battle.attackingFight(AttackNamesies.REFRESH);
        Assert.assertFalse(attacking.hasStatus());
        Assert.assertTrue(defending.hasStatus(StatusCondition.POISONED));
        Assert.assertTrue(defending.hasEffect(EffectNamesies.BAD_POISON));

        // TODO: Should transfer bad poison as well but doesn't but I'm gonna rewrite how that works so do it after that
        attacking.apply(false, AttackNamesies.PSYCHO_SHIFT, battle);
        defending.apply(true, AttackNamesies.PSYCHO_SHIFT, battle);
        Assert.assertTrue(attacking.hasStatus(StatusCondition.POISONED));
//        Assert.assertTrue(attacking.hasEffect(EffectNamesies.BAD_POISON));
        Assert.assertFalse(defending.hasStatus());
        Assert.assertFalse(defending.hasEffect(EffectNamesies.BAD_POISON));

        attacking.withAbility(AbilityNamesies.PROTEAN);
        Assert.assertTrue(attacking.isType(battle, Type.WATER));
        battle.attackingFight(AttackNamesies.CLEAR_SMOG);
        Assert.assertTrue(attacking.isType(battle, Type.POISON));
        Assert.assertTrue(attacking.hasStatus(StatusCondition.POISONED));
//        Assert.assertTrue(attacking.hasEffect(EffectNamesies.BAD_POISON));
        battle.attackingFight(AttackNamesies.PSYCHO_SHIFT);
        Assert.assertFalse(attacking.hasStatus());
        Assert.assertTrue(defending.hasStatus(StatusCondition.POISONED));
//        Assert.assertTrue(defending.hasEffect(EffectNamesies.BAD_POISON));
        Assert.assertTrue(attacking.isType(battle, Type.PSYCHIC));
        battle.attackingFight(AttackNamesies.CLEAR_SMOG);
        Assert.assertTrue(attacking.isType(battle, Type.POISON));
        defending.apply(false, AttackNamesies.PSYCHO_SHIFT, battle);

        Assert.assertTrue(attacking.hasAbility(AbilityNamesies.PROTEAN));
        battle.attackingFight(AttackNamesies.DRAGON_DANCE);
        Assert.assertTrue(Arrays.toString(attacking.getType(battle)), attacking.isType(battle, Type.DRAGON));
        defending.apply(true, AttackNamesies.PSYCHO_SHIFT, battle);
        Assert.assertTrue(attacking.hasStatus(StatusCondition.POISONED));

        defending.withAbility(AbilityNamesies.IMMUNITY);
        defending.apply(false, AttackNamesies.PSYCHO_SHIFT, battle);
        Assert.assertTrue(attacking.hasStatus(StatusCondition.POISONED));
        Assert.assertFalse(defending.hasStatus());

        battle.emptyHeal();
        attacking.withMoves(AttackNamesies.PSYCHO_SHIFT);
        Assert.assertFalse(attacking.hasStatus());
        Assert.assertFalse(defending.hasStatus());
        battle.defendingFight(AttackNamesies.SPORE);
        Assert.assertTrue(attacking.hasStatus(StatusCondition.ASLEEP));
        Assert.assertFalse(defending.hasStatus());
        battle.attackingFight(AttackNamesies.SLEEP_TALK);
        Assert.assertFalse(attacking.hasStatus());
        Assert.assertTrue(defending.hasStatus(StatusCondition.ASLEEP));

        defending.apply(true, AttackNamesies.PSYCHO_SHIFT, battle);
        Assert.assertTrue(attacking.hasStatus(StatusCondition.ASLEEP));
        Assert.assertFalse(defending.hasStatus());

        defending.withAbility(AbilityNamesies.INSOMNIA);
        battle.attackingFight(AttackNamesies.SLEEP_TALK);
        Assert.assertTrue(attacking.hasStatus(StatusCondition.ASLEEP));
        Assert.assertFalse(defending.hasStatus());
    }

    @Test
    public void multiTurnMoveTest() {
        // TODO
    }
}
