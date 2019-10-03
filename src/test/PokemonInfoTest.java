package test;

import battle.attack.AttackNamesies;
import item.ItemNamesies;
import item.bag.Bag;
import main.Game;
import org.junit.Assert;
import org.junit.Test;
import pokemon.ability.AbilityNamesies;
import pokemon.active.EffortValues;
import pokemon.active.Gender;
import pokemon.active.IndividualValues;
import pokemon.active.PartyPokemon;
import pokemon.breeding.EggGroup;
import pokemon.species.BaseStats;
import pokemon.species.GrowthRate;
import pokemon.species.LevelUpMove;
import pokemon.species.PokemonInfo;
import pokemon.species.PokemonNamesies;
import pokemon.stat.Stat;
import trainer.TrainerType;
import type.PokeType;
import type.Type;
import util.MultiMap;
import util.string.StringUtils;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class PokemonInfoTest extends BaseTest {
    @Test
    public void totalPokemonTest() {
        // Add one to account for the empty pokemon at the beginning
        Assert.assertEquals(PokemonNamesies.values().length, PokemonInfo.NUM_POKEMON + 1);
        Assert.assertSame(PokemonNamesies.values()[0], PokemonNamesies.NONE);
    }

    // Test to confirm each pokemon number corresponds correctly to the ordinal in PokemonNamesies enum
    @Test
    public void numberTest() {
        PokemonNamesies[] namesies = PokemonNamesies.values();

        // +1 because the first entry (zero index) is intentionally filler so that the index lines up correctly
        Assert.assertEquals(PokemonInfo.NUM_POKEMON + 1, namesies.length);

        for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++) {
            PokemonInfo pokemonInfo = PokemonInfo.getPokemonInfo(i);
            PokemonNamesies pokemonNamesies = namesies[i];

            Assert.assertEquals(pokemonInfo, pokemonNamesies.getInfo());
            Assert.assertEquals(pokemonInfo.namesies(), pokemonNamesies);
            Assert.assertEquals(pokemonInfo.getName(), pokemonNamesies.getName());
            Assert.assertEquals(pokemonInfo.getNumber(), pokemonNamesies.ordinal());
            Assert.assertEquals(pokemonInfo.getNumber(), i);
        }
    }

    @Test
    public void levelUpMovesTest() {
        // Evolution flag must be less than zero
        TestUtils.assertGreater("", 0, PokemonInfo.EVOLUTION_LEVEL_LEARNED);

        for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++) {
            PokemonInfo pokemonInfo = PokemonInfo.getPokemonInfo(i);

            List<LevelUpMove> levelUpMoves = pokemonInfo.getLevelUpMoves();
            int previousLevel = levelUpMoves.get(0).getLevel();
            boolean hasDefault = false;

            for (LevelUpMove levelUpMove : levelUpMoves) {
                int level = levelUpMove.getLevel();
                String message = StringUtils.spaceSeparated(pokemonInfo.getName(), level, levelUpMove.getMove().getName());

                // Make sure level is valid
                boolean inRange = level >= 0 && level <= PartyPokemon.MAX_LEVEL;
                Assert.assertTrue(message, inRange || level == PokemonInfo.EVOLUTION_LEVEL_LEARNED);

                // Level up moves must be in ascending order
                Assert.assertTrue(message, level >= previousLevel);
                previousLevel = level;

                if (level == 0) {
                    hasDefault = true;
                }
            }

            Assert.assertTrue(pokemonInfo.getName(), hasDefault);
        }
    }

    @Test
    public void duplicateLevelUpMovesTest() {
        List<PokemonMovePair> defaultLevelExceptions = Arrays.asList(
                new PokemonMovePair(PokemonNamesies.METAPOD, AttackNamesies.HARDEN),
                new PokemonMovePair(PokemonNamesies.KAKUNA, AttackNamesies.HARDEN),
                new PokemonMovePair(PokemonNamesies.SILCOON, AttackNamesies.HARDEN),
                new PokemonMovePair(PokemonNamesies.CASCOON, AttackNamesies.HARDEN)
        );

        List<PokemonMovePair> actualLevelExceptions = Arrays.asList(
                new PokemonMovePair(PokemonNamesies.SMEARGLE, AttackNamesies.SKETCH),
                new PokemonMovePair(PokemonNamesies.PUMPKABOO, AttackNamesies.TRICK_OR_TREAT),
                new PokemonMovePair(PokemonNamesies.GOURGEIST, AttackNamesies.TRICK_OR_TREAT)
        );

        for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++) {
            PokemonInfo pokemonInfo = PokemonInfo.getPokemonInfo(i);
            List<LevelUpMove> levelUpMoves = pokemonInfo.getLevelUpMoves();

            MultiMap<AttackNamesies, Integer> map = new MultiMap<>();
            for (LevelUpMove levelUpMove : levelUpMoves) {
                int currentLevel = levelUpMove.getLevel();
                AttackNamesies attack = levelUpMove.getMove();
                PokemonMovePair currentPair = new PokemonMovePair(pokemonInfo.namesies(), attack);

                // We've seen this attack already
                if (map.containsKey(attack)) {
                    String message = StringUtils.spaceSeparated(pokemonInfo.getName(), currentLevel, attack.getName());
                    List<Integer> levels = map.get(attack);

                    // No duplicates for the same level/move combination
                    Assert.assertFalse(message, levels.contains(currentLevel));

                    if (levelUpMove.isDefaultLevel() && !defaultLevelExceptions.contains(currentPair)) {
                        Assert.assertTrue(currentLevel == 0 || currentLevel == PokemonInfo.EVOLUTION_LEVEL_LEARNED);
                        for (Integer level : levels) {
                            Assert.assertTrue(message, level > 1);
                            Assert.assertFalse(message, LevelUpMove.isDefaultLevel(level));
                        }
                    } else if (!levelUpMove.isDefaultLevel() && !actualLevelExceptions.contains(currentPair)) {
                        Assert.assertTrue(currentLevel > 0);
                        for (Integer level : levels) {
                            Assert.assertTrue(message + " " + level, level == 0 || level == PokemonInfo.EVOLUTION_LEVEL_LEARNED);
                            Assert.assertTrue(message + " " + level, LevelUpMove.isDefaultLevel(level));
                        }
                    }
                }

                map.put(attack, currentLevel);
            }

            for (Entry<AttackNamesies, List<Integer>> entry : map.entrySet()) {
                AttackNamesies attack = entry.getKey();
                PokemonMovePair pair = new PokemonMovePair(pokemonInfo.namesies(), attack);
                if (actualLevelExceptions.contains(pair)) {
                    continue;
                }

                List<Integer> levels = entry.getValue();
                String message = StringUtils.spaceSeparated(pokemonInfo.getName(), attack.getName(), levels.size());
                Assert.assertTrue(message, levels.size() > 0 && levels.size() <= 2);
            }
        }

        // Default level tests
        Assert.assertTrue(LevelUpMove.isDefaultLevel(0));
        Assert.assertTrue(LevelUpMove.isDefaultLevel(PokemonInfo.EVOLUTION_LEVEL_LEARNED));
        for (int level = 1; level <= PartyPokemon.MAX_LEVEL; level++) {
            Assert.assertFalse(LevelUpMove.isDefaultLevel(level));
        }
    }

    @Test
    public void stockpileTest() {
        for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++) {
            PokemonInfo pokemonInfo = PokemonInfo.getPokemonInfo(i);

            Integer stockpileLevel = pokemonInfo.levelLearned(AttackNamesies.STOCKPILE);
            Integer spitUpLevel = pokemonInfo.levelLearned(AttackNamesies.SPIT_UP);
            Integer swallowLevel = pokemonInfo.levelLearned(AttackNamesies.SWALLOW);

            if (stockpileLevel != null) {
                Assert.assertFalse(
                        "Stockpile learned without/after Spit Up or Swallow for " + pokemonInfo.getName() + ". " +
                                "Levels: " + stockpileLevel + ", " + spitUpLevel + ", " + swallowLevel,
                        (spitUpLevel == null && swallowLevel == null) ||
                                (spitUpLevel != null && spitUpLevel < stockpileLevel) ||
                                (swallowLevel != null && swallowLevel < stockpileLevel)
                );
            } else {
                Assert.assertFalse(
                        "Spit Up/Swallow learned without Stockpile for " + pokemonInfo.getName() + ". " +
                                "Levels: " + spitUpLevel + ", " + swallowLevel,
                        spitUpLevel != null || swallowLevel != null
                );
            }
        }
    }

    @Test
    public void baseStatTest() {
        // Make sure Rizardon has the same base stat total as Charizard
        Assert.assertEquals(getBaseStatTotal(PokemonNamesies.CHARIZARD), getBaseStatTotal(PokemonNamesies.RIZARDON));
    }

    private int getBaseStatTotal(PokemonNamesies pokemonNamesies) {
        BaseStats baseStats = pokemonNamesies.getInfo().getStats();

        int total = 0;
        for (int i = 0; i < Stat.NUM_STATS; i++) {
            total += baseStats.get(i);
        }

        return total;
    }

    @Test
    public void genderTest() {
        List<Integer> validMaleRatios = Arrays.asList(-1, 0, 13, 25, 50, 75, 87, 100);
        for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++) {
            PokemonInfo pokemonInfo = PokemonInfo.getPokemonInfo(i);
            int maleRatio = pokemonInfo.getMaleRatio();
            Assert.assertTrue(pokemonInfo.getName() + " " + maleRatio, validMaleRatios.contains(maleRatio));
        }
    }

    @Test
    public void evTest() {
        for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++) {
            PokemonInfo pokemonInfo = PokemonInfo.getPokemonInfo(i);
            int evTotal = getEvTotal(pokemonInfo);

            // Make sure EVs are strictly increasing across evolutions
            PokemonNamesies[] evolutions = pokemonInfo.getEvolution().getEvolutions();
            for (PokemonNamesies evolution : evolutions) {
                Assert.assertTrue(evolution.getName(), evTotal < getEvTotal(evolution.getInfo()));
            }
        }
    }

    // Returns the total number of EVs given from this pokemon and confirms the correct range for each value and the total
    private int getEvTotal(PokemonInfo pokemonInfo) {
        int total = 0;
        for (int i = 0; i < Stat.NUM_STATS; i++) {
            int effortValue = pokemonInfo.getGivenEV(i);
            TestUtils.assertInclusiveRange(pokemonInfo.getName() + " " + effortValue, 0, 3, effortValue);
            total += effortValue;
        }
        TestUtils.assertInclusiveRange(pokemonInfo.getName() + " " + total, 1, 3, total);
        return total;
    }

    @Test
    public void abilityTest() {
        for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++) {
            PokemonInfo pokemonInfo = PokemonInfo.getPokemonInfo(i);
            AbilityNamesies[] abilities = pokemonInfo.getAbilities();

            // Must be size 2
            Assert.assertEquals(2, abilities.length);

            // No-Ability can only be the second ability
            Assert.assertNotEquals(AbilityNamesies.NO_ABILITY, abilities[0]);

            // Make sure no duplicate abilities
            Assert.assertNotEquals(abilities[0], abilities[1]);
            Set<AbilityNamesies> seen = EnumSet.noneOf(AbilityNamesies.class);
            for (AbilityNamesies ability : abilities) {
                Assert.assertFalse(seen.contains(ability));
                seen.add(ability);

                Assert.assertTrue(pokemonInfo.hasAbility(ability));
            }

            // Must only return true for hasAbility if in the list
            for (AbilityNamesies ability : AbilityNamesies.values()) {
                Assert.assertEquals(seen.contains(ability), pokemonInfo.hasAbility(ability));
            }

            // Flower Veil was changed and only makes sense for Grass-type Pokemon now
            // Levitate doesn't makes sense for Flying-type Pokemon
            for (AbilityNamesies ability : abilities) {
                if (ability == AbilityNamesies.FLOWER_VEIL) {
                    Assert.assertTrue(pokemonInfo.isType(Type.GRASS));
                } else if (ability == AbilityNamesies.LEVITATE) {
                    Assert.assertFalse(pokemonInfo.isType(Type.FLYING));
                }
            }

            // They need it!!! (If this fails change the Run Away case in substitution)
            if (pokemonInfo.namesies() == PokemonNamesies.PONYTA || pokemonInfo.namesies() == PokemonNamesies.RAPIDASH) {
                Assert.assertTrue(pokemonInfo.hasAbility(AbilityNamesies.FLAME_BODY));
            }

            // Don't worry about it (but really if this fails change the message in Iron Fist)
            if (pokemonInfo.namesies() == PokemonNamesies.PANGORO) {
                Assert.assertTrue(pokemonInfo.hasAbility(AbilityNamesies.MOLD_BREAKER));
                Assert.assertTrue(pokemonInfo.hasAbility(AbilityNamesies.IRON_FIST));
            }
        }
    }

    @Test
    public void typeTest() {
        for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++) {
            PokemonInfo pokemonInfo = PokemonInfo.getPokemonInfo(i);
            PokeType pokeType = pokemonInfo.getType();
            Type[] types = pokeType.getTypes();

            // No-Type can only be the second type
            Assert.assertNotEquals(Type.NO_TYPE, pokeType.getFirstType());

            // Make sure no duplicate types
            Assert.assertNotEquals(pokeType.getFirstType(), pokeType.getSecondType());

            // Dual-typed if and only if second type is not No-Type
            if (pokeType.isDualTyped()) {
                Assert.assertNotEquals(Type.NO_TYPE, pokeType.getSecondType());
                Assert.assertEquals(2, types.length);
            } else {
                Assert.assertEquals(Type.NO_TYPE, pokeType.getSecondType());
                Assert.assertEquals(1, types.length);
            }
        }
    }

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
                TestUtils.assertGreater(levelString, shedinja.getEVs().get(Stat.HP), 0);
                Assert.assertEquals(1, shedinja.getHP());
                Assert.assertEquals(1, shedinja.getMaxHP());

                bag.addItem(ItemNamesies.PROTEIN);
                Assert.assertTrue(bag.usePokemonItem(ItemNamesies.PROTEIN, shedinja));
                TestUtils.assertGreater(levelString, shedinja.getEVs().get(Stat.ATTACK), 0);
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
    public void meltanTest() {
        // These Pokemon are not in the script API and it's probably really easy to accidentally overwrite their
        // data since the script is giving them placeholder information
        PokemonInfo meltan = PokemonNamesies.MELTAN.getInfo();
        PokemonInfo melmetal = PokemonNamesies.MELMETAL.getInfo();

        Assert.assertEquals("46 65 65 55 35 34", meltan.getStats().toString());
        Assert.assertEquals("135 143 143 80 65 34", melmetal.getStats().toString());

        Assert.assertArrayEquals(new int[] { 0, 1, 0, 0, 0, 0 }, meltan.getGivenEVs());
        Assert.assertArrayEquals(new int[] { 0, 3, 0, 0, 0, 0 }, melmetal.getGivenEVs());

        Assert.assertEquals(135, meltan.getBaseEXP());
        Assert.assertEquals(270, melmetal.getBaseEXP());

        Assert.assertEquals("0'08\"", meltan.getHeightString());
        Assert.assertEquals("8'02\"", melmetal.getHeightString());

        TestUtils.assertEquals(17.6, meltan.getWeight());
        TestUtils.assertEquals(1763.7, melmetal.getWeight());

        Assert.assertEquals("Magnet Pull", meltan.getAbilitiesString());
        Assert.assertEquals("Iron Fist", melmetal.getAbilitiesString());

        meltanTest(meltan);
        meltanTest(melmetal);
    }

    // Used for info that is true for both Meltan and Melmetal
    private void meltanTest(PokemonInfo pokemonInfo) {

        Assert.assertTrue(pokemonInfo.isType(Type.STEEL));
        Assert.assertFalse(pokemonInfo.getType().isDualTyped());

        Assert.assertEquals(3, pokemonInfo.getCatchRate());
        Assert.assertEquals(30720, pokemonInfo.getEggSteps());
        Assert.assertEquals("Hex Nut", pokemonInfo.getClassification());
        Assert.assertEquals(GrowthRate.SLOW, pokemonInfo.getGrowthRate());
        Assert.assertEquals(Gender.GENDERLESS_CONSTANT, pokemonInfo.getMaleRatio());

        Assert.assertArrayEquals(new EggGroup[] { EggGroup.UNDISCOVERED, EggGroup.NONE }, pokemonInfo.getEggGroups());

        Assert.assertEquals(PokemonNamesies.MELTAN, pokemonInfo.getBaseEvolution());

        // Probably a good enough indicator that the moves weren't changed
        Assert.assertTrue(pokemonInfo.canLearnMove(AttackNamesies.THUNDER_SHOCK));
        Assert.assertTrue(pokemonInfo.canLearnMove(AttackNamesies.ACID_ARMOR));
        Assert.assertTrue(pokemonInfo.canLearnMove(AttackNamesies.FLASH_CANNON));
    }

    private static class PokemonMovePair {
        private final PokemonNamesies pokemon;
        private final AttackNamesies attack;

        PokemonMovePair(PokemonNamesies pokemon, AttackNamesies attack) {
            this.pokemon = pokemon;
            this.attack = attack;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof PokemonMovePair) {
                PokemonMovePair that = (PokemonMovePair)other;
                return this.pokemon == that.pokemon && this.attack == that.attack;
            } else {
                return false;
            }
        }
    }
}
