package test.battle;

import battle.attack.AttackNamesies;
import battle.effect.InvokeInterfaces.OpponentStatSwitchingEffect;
import battle.effect.InvokeInterfaces.StatSwitchingEffect;
import org.junit.Assert;
import org.junit.Test;
import pokemon.Stat;
import pokemon.species.PokemonNamesies;
import test.BaseTest;
import test.TestPokemon;

public class StatTest extends BaseTest {
    @Test
    public void equalStatsTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // These should be equal with no effects on the battle
        for (int i = 0; i < Stat.NUM_BATTLE_STATS; i++) {
            Stat stat = Stat.getStat(i, true);
            Assert.assertNotEquals(stat, Stat.HP);
            if (stat == Stat.ACCURACY || stat == Stat.EVASION) {
                Assert.assertEquals(stat.getName(), 100, Stat.getStat(stat, attacking, battle));
                Assert.assertEquals(stat.getName(), 100, Stat.getStat(stat, defending, battle));
            } else {
                equalStats(battle, attacking, stat);
                equalStats(battle, defending, stat);
            }
        }
    }

    @Test
    public void statSwitchingTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.MACHAMP, PokemonNamesies.ALAKAZAM);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // This test is pointless if any of these are the same
        Assert.assertNotEquals(attacking.getStat(battle, Stat.ATTACK), attacking.getStat(battle, Stat.SP_ATTACK));
        Assert.assertNotEquals(attacking.getStat(battle, Stat.DEFENSE), attacking.getStat(battle, Stat.SP_DEFENSE));
        Assert.assertNotEquals(defending.getStat(battle, Stat.ATTACK), defending.getStat(battle, Stat.SP_ATTACK));
        Assert.assertNotEquals(defending.getStat(battle, Stat.DEFENSE), defending.getStat(battle, Stat.SP_DEFENSE));

        // Psystrike uses attacker's Special Attack stat and defender's Defense stat
        attacking.setupMove(AttackNamesies.PSYSTRIKE, battle);
        defending.setupMove(AttackNamesies.PSYCHIC, battle);
        equalStats(battle, attacking, Stat.SP_ATTACK);
        equalStats(battle, defending, Stat.DEFENSE, Stat.SP_DEFENSE);
        confirmAttacking(battle, attacking);
        confirmDefending(battle, defending);

        // Test requires Attack being greater for attacking and Sp. Attack greater for defending
        Assert.assertTrue(attacking.getStat(battle, Stat.ATTACK) > attacking.getStat(battle, Stat.SP_ATTACK));
        Assert.assertTrue(defending.getStat(battle, Stat.SP_ATTACK) > defending.getStat(battle, Stat.ATTACK));

        // Photon Geyser uses Attack and Defense stats if it attack is higher
        attacking.setupMove(AttackNamesies.PHOTON_GEYSER, battle);
        equalStats(battle, attacking, Stat.ATTACK, Stat.SP_ATTACK);
        equalStats(battle, defending, Stat.DEFENSE, Stat.SP_DEFENSE);
        confirmAttacking(battle, attacking);
        confirmDefending(battle, defending);

        // Unchanged when defending uses since Sp. Attack is greater (because Alakazam) and the default
        // Note: Both Pokemon have Photon Geyser set up here
        defending.setupMove(AttackNamesies.PHOTON_GEYSER, battle);
        equalStats(battle, attacking, Stat.ATTACK, Stat.SP_ATTACK);
        equalStats(battle, defending, Stat.DEFENSE, Stat.SP_DEFENSE);
        equalStats(battle, defending, Stat.SP_ATTACK);
        equalStats(battle, attacking, Stat.SP_DEFENSE);

        // Only defending using Photon Geyser -- use normally (no change since Alakazam)
        attacking.setupMove(AttackNamesies.TACKLE, battle);
        equalStats(battle, defending, Stat.SP_ATTACK);
        equalStats(battle, attacking, Stat.SP_DEFENSE);
        confirmDefending(battle, attacking);
        confirmAttacking(battle, defending);
    }

    // The Pokemon that is attacking this turn should have no changes to either defense stat
    private void confirmAttacking(TestBattle battle, TestPokemon defending) {
        equalStats(battle, defending, Stat.DEFENSE);
        equalStats(battle, defending, Stat.SP_DEFENSE);
    }

    // The Pokemon that is defending this turn should have no changes to either attack stat
    private void confirmDefending(TestBattle battle, TestPokemon defending) {
        equalStats(battle, defending, Stat.ATTACK);
        equalStats(battle, defending, Stat.SP_ATTACK);
    }

    // Confirms that the base stat is the same as itself when computed
    // Confirms that the computed stat has the same value as the base stat
    // Confirms that the computed stat is different than itself when computed
    private void equalStats(TestBattle battle, TestPokemon statPokemon, Stat baseStat, Stat computedStat) {
        checkStats(true, battle, statPokemon, baseStat, computedStat);
        equalStats(battle, statPokemon, baseStat);
        notEqualStats(battle, statPokemon, computedStat);
    }

    private void equalStats(TestBattle battle, TestPokemon statPokemon, Stat stat) {
        checkStats(true, battle, statPokemon, stat, stat);
    }

    private void notEqualStats(TestBattle battle, TestPokemon statPokemon, Stat stat) {
        checkStats(false, battle, statPokemon, stat, stat);
    }

    private void checkStats(boolean equals, TestBattle battle, TestPokemon statPokemon, Stat baseStat, Stat computedStat) {
        // Stages have nothing to do with this and should all be zero
        statPokemon.assertStages(new TestStages());

        Stat switchedStat = StatSwitchingEffect.switchStat(battle, statPokemon, computedStat);
        switchedStat = OpponentStatSwitchingEffect.switchStat(battle, battle.getOtherPokemon(statPokemon), switchedStat);
        Assert.assertEquals(
                String.format("Base: %s, Switched: %s, Computed: %s", baseStat.getName(), switchedStat.getName(), computedStat.getName()),
                baseStat == switchedStat,
                equals
        );

        int base = statPokemon.getStat(battle, baseStat);
        int computed = Stat.getStat(computedStat, statPokemon, battle);
        Assert.assertEquals(
                String.format("Base: %s %d, Computed: %s %d", baseStat.getName(), base, computedStat.getName(), computed),
                base == computed,
                equals
        );
    }
}
