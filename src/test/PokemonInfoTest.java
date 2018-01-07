package test;

import battle.attack.AttackNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.ActivePokemon;
import pokemon.LevelUpMove;
import pokemon.PokemonInfo;
import pokemon.PokemonNamesies;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import type.Type;
import util.MultiMap;
import util.StringUtils;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class PokemonInfoTest extends BaseTest {
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
            Assert.assertTrue(pokemonInfo.getName() + " " + evTotal, evTotal >= 1 && evTotal <= 3);

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
            Type[] types = pokemonInfo.getType();

            // Must be size 2
            Assert.assertEquals(2, types.length);

            // No-Type can only be the second type
            Assert.assertNotEquals(Type.NO_TYPE, types[0]);

            // Make sure no duplicate types
            Set<Type> seen = EnumSet.noneOf(Type.class);
            for (Type type : types) {
                Assert.assertFalse(seen.contains(type));
                seen.add(type);
            }
        }
    }

    @Test
    public void ivsTest() {
        boolean[] hasIv = new boolean[Stat.MAX_IV + 1];
        boolean diffIvs = false;
        for (int i = 0; i < 1000; i++) {
            TestPokemon pokemon = TestPokemon.newWildPokemon(PokemonNamesies.BULBASAUR);

            for (int j = 0; j < Stat.NUM_STATS; j++) {
                Stat stat = Stat.getStat(j, false);
                int iv = pokemon.getIV(j);
                String message = stat.getName() + " " + iv;

                // Make sure all IVs are in range
                Assert.assertTrue(message, iv >= 0 && iv <= Stat.MAX_IV);
                hasIv[iv] = true;

                // Make sure not every IV is the same
                if (j > 0 && iv != pokemon.getIV(j - 1)) {
                    diffIvs = true;
                }
            }
        }

        Assert.assertTrue(diffIvs);
        for (int i = 0; i < hasIv.length; i++) {
            Assert.assertTrue(i + "", hasIv[i]);
        }
    }
}
