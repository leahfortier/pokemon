package test.battle;

import battle.attack.AttackNamesies;
import battle.effect.EffectNamesies.BattleEffectNamesies;
import battle.effect.InvokeInterfaces.OpponentStatSwitchingEffect;
import battle.effect.InvokeInterfaces.StatSwitchingEffect;
import battle.effect.battle.StandardBattleEffectNamesies;
import battle.effect.pokemon.PokemonEffectNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.Stat;
import pokemon.species.PokemonNamesies;
import test.BaseTest;
import test.TestPokemon;
import test.TestUtils;

import java.util.EnumMap;
import java.util.Map;

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
        // Fun fact: These are 2/10 Pokemon that have greater than 10 difference between their closest base stats
        TestBattle battle = TestBattle.create(PokemonNamesies.SNEASEL, PokemonNamesies.CARRACOSTA);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Confirm all stats are unique
        TestUtils.assertUnique(attacking.stats().getClonedStats());
        TestUtils.assertUnique(defending.stats().getClonedStats());

        // Psystrike uses attacker's Special Attack stat and defender's Defense stat
        attacking.setupMove(AttackNamesies.PSYSTRIKE, battle);
        defending.setupMove(AttackNamesies.PSYCHIC, battle);
        assertStats(battle, attacking, new StatChecker());
        assertStats(battle, defending, new StatChecker().set(Stat.SP_DEFENSE, Stat.DEFENSE));

        // Nothing interesting going on anymore -- all stats should be normal
        attacking.setupMove(AttackNamesies.TACKLE, battle);
        assertStats(battle, attacking, new StatChecker());
        assertStats(battle, defending, new StatChecker());

        // Real trickster (switches Attack and Defense)
        // Power Trick only affects the user
        battle.attackingFight(AttackNamesies.POWER_TRICK);
        attacking.assertHasEffect(PokemonEffectNamesies.POWER_TRICK);
        assertStats(battle, attacking, new StatChecker().set(Stat.ATTACK, Stat.DEFENSE).set(Stat.DEFENSE, Stat.ATTACK));
        assertStats(battle, defending, new StatChecker());

        // Using Power Trick again will remove the effect
        battle.attackingFight(AttackNamesies.POWER_TRICK);
        attacking.assertNoEffect(PokemonEffectNamesies.POWER_TRICK);
        assertStats(battle, attacking, new StatChecker());
        assertStats(battle, defending, new StatChecker());

        // Wonder Room switches defensive stats for both Pokemon
        battle.attackingFight(AttackNamesies.WONDER_ROOM);
        Assert.assertTrue(battle.hasEffect(StandardBattleEffectNamesies.WONDER_ROOM));
        assertStats(battle, attacking, new StatChecker().set(Stat.DEFENSE, Stat.SP_DEFENSE).set(Stat.SP_DEFENSE, Stat.DEFENSE));
        assertStats(battle, defending, new StatChecker().set(Stat.DEFENSE, Stat.SP_DEFENSE).set(Stat.SP_DEFENSE, Stat.DEFENSE));

        // Using again will remove the effect (regardless of who uses)
        battle.defendingFight(AttackNamesies.WONDER_ROOM);
        Assert.assertFalse(battle.hasEffect(StandardBattleEffectNamesies.WONDER_ROOM));
        assertStats(battle, attacking, new StatChecker());
        assertStats(battle, defending, new StatChecker());
    }

    // Holds a map from stat to what the stat should switch to
    private static class StatChecker {
        private Map<Stat, Stat> statMap;

        public StatChecker() {
            this.statMap = new EnumMap<>(Stat.class);

            // Add all basic stats (no HP) and map to itself (by default each stat returns its own value -- not switched)
            for (Stat stat : Stat.STATS) {
                if (stat == Stat.HP) {
                    continue;
                }

                this.statMap.put(stat, stat);
            }
        }

        public StatChecker set(Stat computedStat, Stat baseStat) {
            // Can only set stat once
            Assert.assertEquals(this.statMap.get(computedStat), computedStat);
            this.statMap.put(computedStat, baseStat);
            return this;
        }
    }

    private void assertStats(TestBattle battle, TestPokemon statPokemon, StatChecker statChecker) {
        for (Stat computedStat : statChecker.statMap.keySet()) {
            Stat baseStat = statChecker.statMap.get(computedStat);

            // Confirms that the base stat is the same as itself when computed
            checkStats(true, battle, statPokemon, baseStat, computedStat);

            // If not equal, confirms that the computed stat is different than itself when computed
            if (computedStat != baseStat) {
                checkStats(false, battle, statPokemon, computedStat, computedStat);
            }
        }
    }

    private void equalStats(TestBattle battle, TestPokemon statPokemon, Stat stat) {
        checkStats(true, battle, statPokemon, stat, stat);
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

    @Test
    public void statSplittingTest() {
        statSplittingTest(AttackNamesies.POWER_SPLIT, AttackNamesies.WORK_UP, StandardBattleEffectNamesies.POWER_SPLIT, Stat.ATTACK, Stat.SP_ATTACK);
        statSplittingTest(AttackNamesies.GUARD_SPLIT, AttackNamesies.COSMIC_POWER, StandardBattleEffectNamesies.GUARD_SPLIT, Stat.DEFENSE, Stat.SP_DEFENSE);
    }

    private void statSplittingTest(AttackNamesies splitter, AttackNamesies increaser, BattleEffectNamesies splitEffect, Stat... stats) {
        // The Pokemon have the largest minimum distance between each base stat (100)
        TestBattle battle = TestBattle.create(PokemonNamesies.SHUCKLE, PokemonNamesies.ARCEUS);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Stats must be different for this test to make sense
        compareStats(false, battle, stats);

        battle.attackingFight(splitter);
        Assert.assertTrue(battle.hasEffect(splitEffect));

        // Annndddd now they're the same it's magic
        compareStats(true, battle, stats);
        attacking.assertStages(new TestStages());
        defending.assertStages(new TestStages());

        // Stages should not be split
        battle.attackingFight(increaser);
        attacking.assertStages(new TestStages().set(1, stats));
        defending.assertStages(new TestStages());

        compareStats(false, battle, stats);
    }

    private void compareStats(boolean equals, TestBattle battle, Stat... stats) {
        for (Stat s : stats) {
            compareStats(equals, s, battle);
        }
    }

    private void compareStats(boolean equals, Stat s, TestBattle battle) {
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        int attackingStat = Stat.getStat(s, attacking, battle);
        int defendingStat = Stat.getStat(s, defending, battle);

        Assert.assertEquals(
                "Stat: " + s.getName() + ", Attacking: " + attackingStat + ", Defending: " + defendingStat + "\n" +
                "Attacking: " + attacking.statsString() + "\n" +
                "Defending: " + defending.statsString(),
                equals,
                attackingStat == defendingStat
        );
    }
}
