package test.pokemon;

import item.ItemNamesies;
import item.bag.Bag;
import main.Game;
import org.junit.Assert;
import org.junit.Test;
import pokemon.active.EffortValues;
import pokemon.active.IndividualValues;
import pokemon.active.Nature;
import pokemon.species.PokemonNamesies;
import pokemon.stat.Stat;
import test.general.BaseTest;
import test.general.TestUtils;
import trainer.TrainerType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StatValuesTest extends BaseTest {
    @Test
    public void ivsTest() {
        boolean[] hasIvs = new boolean[IndividualValues.MAX_IV + 1];
        boolean diffIvs = false;
        for (int i = 0; i < 1000; i++) {
            TestPokemon pokemon = TestPokemon.newWildPokemon(PokemonNamesies.BULBASAUR);

            for (int j = 0; j < Stat.NUM_STATS; j++) {
                Stat stat = Stat.getStat(j, false);
                int iv = pokemon.getIVs().get(j);
                String message = stat.getName() + " " + iv;

                // Make sure all IVs are in range
                Assert.assertTrue(message, iv >= 0 && iv <= IndividualValues.MAX_IV);
                hasIvs[iv] = true;

                // Make sure not every IV is the same
                if (j > 0 && iv != pokemon.getIVs().get(j - 1)) {
                    diffIvs = true;
                }
            }
        }

        Assert.assertTrue(diffIvs);
        for (boolean hasIv : hasIvs) {
            Assert.assertTrue(hasIv);
        }
    }

    @Test
    public void shedinjaTest() {
        // Basically making sure tht its HP is always 1
        int[] levels = new int[] { 1, 50, 100 };
        for (int level : levels) {
            TestPokemon shedinja = new TestPokemon(PokemonNamesies.SHEDINJA, level, TrainerType.PLAYER);
            Assert.assertEquals(1, shedinja.getHP());
            Assert.assertEquals(1, shedinja.getMaxHP());

            int baseAttack = shedinja.getStat(Stat.ATTACK);
            String levelString = Integer.toString(level);

            Bag bag = Game.getPlayer().getBag();
            for (int i = 0; i < 26; i++) {
                bag.addItem(ItemNamesies.HP_UP);
                Assert.assertTrue(bag.usePokemonItem(ItemNamesies.HP_UP, shedinja));
                TestUtils.assertPositive(levelString, shedinja.getEVs().get(Stat.HP));
                Assert.assertEquals(1, shedinja.getHP());
                Assert.assertEquals(1, shedinja.getMaxHP());

                bag.addItem(ItemNamesies.PROTEIN);
                Assert.assertTrue(bag.usePokemonItem(ItemNamesies.PROTEIN, shedinja));
                TestUtils.assertPositive(levelString, shedinja.getEVs().get(Stat.ATTACK));
            }

            // HP EVs are now maxed, but max HP should not increase still
            Assert.assertEquals(EffortValues.MAX_STAT_EVS, shedinja.getEVs().get(Stat.HP));
            Assert.assertFalse(bag.usePokemonItem(ItemNamesies.HP_UP, shedinja));
            Assert.assertEquals(EffortValues.MAX_STAT_EVS, shedinja.getEVs().get(Stat.HP));
            Assert.assertEquals(1, shedinja.getHP());
            Assert.assertEquals(1, shedinja.getMaxHP());

            // Make sure other EVs contribute
            Assert.assertEquals(EffortValues.MAX_STAT_EVS, shedinja.getEVs().get(Stat.ATTACK));
            if (level > 1) { // Don't check at level one since it's generally too small for change
                TestUtils.assertGreater(levelString, shedinja.getStat(Stat.ATTACK), baseAttack);
            }

            // Make sure level up doesn't increase HP either
            bag.addItem(ItemNamesies.RARE_CANDY);
            if (level == 100) {
                Assert.assertFalse(bag.usePokemonItem(ItemNamesies.RARE_CANDY, shedinja));
            } else {
                Assert.assertTrue(bag.usePokemonItem(ItemNamesies.RARE_CANDY, shedinja));
                Assert.assertEquals(level + 1, shedinja.getLevel());
            }
            Assert.assertEquals(1, shedinja.getHP());
            Assert.assertEquals(1, shedinja.getMaxHP());
        }
    }

    @Test
    public void natureTest() {
        Nature[] natures = Nature.values();
        Assert.assertEquals(25, natures.length);

        // Stats that are relevant to nature (not HP and battle-only stats)
        List<Stat> stats = new ArrayList<>(Stat.STATS);
        stats.remove(Stat.HP);
        Assert.assertEquals(5, stats.size());

        // Get all stat combinations
        Set<String> natureStats = new HashSet<>();
        for (Stat beneficial : stats) {
            for (Stat hindering : stats) {
                natureStats.add(beneficial + "/" + hindering);
            }
        }
        Assert.assertEquals(25, natureStats.size());

        for (Nature nature : natures) {
            Stat beneficial = nature.getBeneficial();
            Stat hindering = nature.getHindering();

            // Make sure stat is valid
            assertNatureStat(beneficial);
            assertNatureStat(hindering);

            // Check neutral nature
            Assert.assertEquals(nature.isNeutral(), beneficial == hindering);

            // Confirm each nature is unique
            String statString = beneficial + "/" + hindering;
            Assert.assertTrue(natureStats.contains(statString));
            natureStats.remove(statString);

            // Nature name is always a single word starting with a capital letter
            Assert.assertTrue(nature.getName().matches("[A-Z][a-z]+"));
        }

        // All natures accounted for
        Assert.assertTrue(natureStats.isEmpty());
    }

    private void assertNatureStat(Stat stat) {
        Assert.assertNotEquals(stat, Stat.HP);
        Assert.assertNotEquals(stat, Stat.ACCURACY);
        Assert.assertNotEquals(stat, Stat.EVASION);
    }
}
