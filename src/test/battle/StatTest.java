package test.battle;

import battle.attack.AttackNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.PokemonNamesies;
import pokemon.Stat;
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
                Assert.assertEquals(stat.getName(), 100, Stat.getStat(stat, attacking, defending, battle));
                Assert.assertEquals(stat.getName(), 100, Stat.getStat(stat, defending, attacking, battle));
            } else {
                equalStats(battle, true, stat);
                equalStats(battle, false, stat);
            }
        }
    }

    @Test
    public void statSwitchingTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.GYARADOS, PokemonNamesies.ALAKAZAM);
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
        equalStats(battle, true, Stat.SP_ATTACK);
        equalStats(battle, false, Stat.SP_ATTACK);
        equalStats(battle, true, Stat.SP_DEFENSE);
        equalStats(battle, false, Stat.DEFENSE, Stat.SP_DEFENSE);
        notEqualStats(battle, false, Stat.SP_DEFENSE);

        // Test requires Attack being greater for attacking and Sp. Attack greater for defending
        Assert.assertTrue(attacking.getStat(battle, Stat.ATTACK) > attacking.getStat(battle, Stat.SP_ATTACK));
        Assert.assertTrue(defending.getStat(battle, Stat.SP_ATTACK) > defending.getStat(battle, Stat.ATTACK));

        // Photon Geyser uses Attack stat if it is higher
        attacking.setupMove(AttackNamesies.PHOTON_GEYSER, battle);
        notEqualStats(battle, true, Stat.SP_ATTACK);
        equalStats(battle, true, Stat.ATTACK, Stat.SP_ATTACK);
        equalStats(battle, false, Stat.SP_ATTACK);
        equalStats(battle, true, Stat.SP_DEFENSE);
        equalStats(battle, false, Stat.SP_DEFENSE);

        // Unchanged when defending uses since Sp. Attack is greater and the default
        defending.setupMove(AttackNamesies.PHOTON_GEYSER, battle);
        notEqualStats(battle, true, Stat.SP_ATTACK);
        equalStats(battle, true, Stat.ATTACK, Stat.SP_ATTACK);
        equalStats(battle, false, Stat.SP_ATTACK);
        equalStats(battle, true, Stat.SP_DEFENSE);
        equalStats(battle, false, Stat.SP_DEFENSE);

        attacking.setupMove(AttackNamesies.TACKLE, battle);
        equalStats(battle, true, Stat.SP_ATTACK);
        equalStats(battle, false, Stat.SP_ATTACK);
        equalStats(battle, true, Stat.SP_DEFENSE);
        equalStats(battle, false, Stat.SP_DEFENSE);
    }

    private void equalStats(TestBattle battle, boolean isPlayer, Stat stat) {
        equalStats(battle, isPlayer, stat, stat);
    }

    private void equalStats(TestBattle battle, boolean isPlayer, Stat baseStat, Stat computedStat) {
        checkStats(true, battle, isPlayer, baseStat, computedStat);
    }

    private void notEqualStats(TestBattle battle, boolean isPlayer, Stat stat) {
        checkStats(false, battle, isPlayer, stat, stat);
    }

    private void checkStats(boolean equals, TestBattle battle, boolean isPlayer, Stat baseStat, Stat computedStat) {
        TestPokemon statPokemon = (TestPokemon)battle.getTrainer(isPlayer).front();
        TestPokemon other = battle.getOtherPokemon(statPokemon);
        checkStats(equals, battle, statPokemon, other, baseStat, computedStat);
    }

    private void checkStats(boolean equals, TestBattle battle, TestPokemon statPokemon, TestPokemon other, Stat baseStat, Stat computedStat) {
        int base = statPokemon.getStat(battle, baseStat);
        int computed = Stat.getStat(computedStat, statPokemon, other, battle);
        Assert.assertEquals(
                String.format("Base: %s %d, Computed: %s %d", baseStat.getName(), base, computedStat.getName(), computed),
                base == computed,
                equals
        );
    }
}
