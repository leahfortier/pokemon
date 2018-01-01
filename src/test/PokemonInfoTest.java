package test;

import battle.attack.AttackNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.ActivePokemon;
import pokemon.LevelUpMove;
import pokemon.PokemonInfo;
import pokemon.PokemonNamesies;
import pokemon.Stat;
import util.MultiMap;
import util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

public class PokemonInfoTest {
    @Test
    public void totalPokemonTest() {
        // Add one to account for the empty pokemon at the beginning
        Assert.assertTrue(PokemonNamesies.values().length == PokemonInfo.NUM_POKEMON + 1);
        Assert.assertTrue(PokemonNamesies.values()[0] == PokemonNamesies.NONE);
    }

    @Test
    public void numberTest() {
        for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++) {
            PokemonInfo pokemonInfo = PokemonInfo.getPokemonInfo(i);
            PokemonNamesies pokemonNamesies = PokemonNamesies.values()[i];

            Assert.assertEquals(pokemonInfo, pokemonNamesies.getInfo());
            Assert.assertEquals(pokemonInfo.namesies(), pokemonNamesies);
            Assert.assertEquals(pokemonInfo.getName(), pokemonNamesies.getName());
            Assert.assertEquals(pokemonInfo.getNumber(), pokemonNamesies.ordinal());
            Assert.assertEquals(pokemonInfo.getNumber(), i);
        }
    }

    @Test
    public void levelUpTest() {
        for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++) {
            PokemonInfo pokemonInfo = PokemonInfo.getPokemonInfo(i);
            List<LevelUpMove> levelUpMoves = pokemonInfo.getLevelUpMoves();
            int previousLevel = levelUpMoves.get(0).getLevel();
            for (LevelUpMove levelUpMove : levelUpMoves) {
                int level = levelUpMove.getLevel();
                String message = StringUtils.spaceSeparated(pokemonInfo.getName(), level, levelUpMove.getMove().getName());

                boolean inRange = level >= 0 && level <= ActivePokemon.MAX_LEVEL;
                Assert.assertTrue(message, inRange || level == PokemonInfo.EVOLUTION_LEVEL_LEARNED);

                // Make sure level up moves are in ascending order
                Assert.assertTrue(message, level >= previousLevel);
                previousLevel = level;
            }
        }
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
                PokemonMovePair otherPair = (PokemonMovePair)other;
                return this.pokemon == otherPair.pokemon && this.attack == otherPair.attack;
            } else {
                return false;
            }
        }
    }

    @Test
    public void duplicateLevelUpTest() {
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

                if (map.containsKey(attack)) {
                    String message = StringUtils.spaceSeparated(pokemonInfo.getName(), currentLevel, attack.getName());
                    List<Integer> levels = map.get(attack);

                    // No duplicates for the same level/move combination
                    Assert.assertFalse(message, levels.contains(currentLevel));

                    if (levelUpMove.isDefaultLevel() && !defaultLevelExceptions.contains(currentPair)) {
                        Assert.assertTrue(currentLevel == 0 || currentLevel == PokemonInfo.EVOLUTION_LEVEL_LEARNED);
                        for (Integer level : levels) {
                            Assert.assertTrue(message, level > 1);
                            Assert.assertTrue(message, !LevelUpMove.isDefaultLevel(level));
                        }
                    } else if (!levelUpMove.isDefaultLevel() && !actualLevelExceptions.contains(currentPair)) {
                        Assert.assertTrue(currentLevel > 0 && currentLevel != PokemonInfo.EVOLUTION_LEVEL_LEARNED);
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
        for (int level = 1; level <= ActivePokemon.MAX_LEVEL; level++) {
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
        PokemonInfo pokemonInfo = pokemonNamesies.getInfo();

        int total = 0;
        for (int i = 0; i < Stat.NUM_STATS; i++) {
            total += pokemonInfo.getStat(i);
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
            Assert.assertTrue(evTotal >= 1 && evTotal <= 3);

            // Make sure EVs are strictly increasing across evolutions
            PokemonNamesies[] evolutions = pokemonInfo.getEvolution().getEvolutions();
            for (PokemonNamesies evolution : evolutions) {
                Assert.assertTrue(evolution.getName(), evTotal < getEvTotal(evolution.getInfo()));
            }
        }
    }

    private int getEvTotal(PokemonInfo pokemonInfo) {
        int total = 0;
        for (int i = 0; i < Stat.NUM_STATS; i++) {
            total += pokemonInfo.getGivenEV(i);
        }

        return total;
    }
}
