package test.pokemon;

import battle.attack.AttackNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.ability.AbilityNamesies;
import pokemon.active.Gender;
import pokemon.active.PartyPokemon;
import pokemon.breeding.EggGroup;
import pokemon.species.BaseStats;
import pokemon.species.GrowthRate;
import pokemon.species.LevelUpMove;
import pokemon.species.PokemonInfo;
import pokemon.species.PokemonList;
import pokemon.species.PokemonNamesies;
import pokemon.stat.Stat;
import test.general.BaseTest;
import test.general.TestUtils;
import type.PokeType;
import type.Type;
import util.GeneralUtils;
import util.MultiMap;
import util.string.StringUtils;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class PokemonInfoTest extends BaseTest {
    // Test to confirm each pokemon number corresponds correctly to the ordinal in PokemonNamesies enum
    @Test
    public void numberTest() {
        PokemonNamesies[] namesies = PokemonNamesies.values();
        Iterator<PokemonInfo> iterator = PokemonList.instance().iterator();

        // +1 because the first entry (zero index) is intentionally filler so that the index lines up correctly
        Assert.assertEquals(PokemonInfo.NUM_POKEMON + 1, namesies.length);
        Assert.assertEquals(PokemonNamesies.NONE, namesies[0]);

        for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++) {
            Assert.assertTrue(iterator.hasNext());

            PokemonInfo pokemonInfo = iterator.next();
            PokemonNamesies pokemonNamesies = namesies[i];

            Assert.assertSame(pokemonInfo, PokemonList.get(i));
            Assert.assertSame(pokemonInfo, pokemonNamesies.getInfo());
            Assert.assertEquals(pokemonInfo.namesies(), pokemonNamesies);
            Assert.assertEquals(pokemonInfo.getName(), pokemonNamesies.getName());
            Assert.assertEquals(pokemonInfo.getNumber(), pokemonNamesies.ordinal());
            Assert.assertEquals(pokemonInfo.getNumber(), i);
        }

        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void levelUpMovesTest() {
        for (PokemonInfo pokemonInfo : PokemonList.instance()) {
            List<LevelUpMove> levelUpMoves = pokemonInfo.getLevelUpMoves();
            int previousLevel = levelUpMoves.get(0).getLevel();
            boolean hasDefault = false;

            for (LevelUpMove levelUpMove : levelUpMoves) {
                int level = levelUpMove.getLevel();
                AttackNamesies attack = levelUpMove.getMove();
                String message = StringUtils.spaceSeparated(pokemonInfo.getName(), level, attack.getName());

                // Make sure level is valid
                TestUtils.assertInclusiveRange(message, 0, 100, level);

                // Level up moves must be in ascending order
                Assert.assertTrue(message, level >= previousLevel);
                previousLevel = level;

                if (LevelUpMove.isDefaultLevel(level)) {
                    hasDefault = true;
                }

                // If it learns a move at evolution, then it cannot be a base evolution
                if (level == PokemonInfo.EVOLUTION_LEVEL_LEARNED) {
                    Assert.assertNotEquals(message, pokemonInfo.namesies(), pokemonInfo.getBaseEvolution());
                }

                // Pokemon cannot learn these moves by level up
                Assert.assertNotEquals(message, AttackNamesies.CONFUSION_DAMAGE, attack);
                Assert.assertNotEquals(message, AttackNamesies.STRUGGLE, attack);
            }

            Assert.assertTrue(pokemonInfo.getName(), hasDefault);
        }
    }

    @Test
    public void duplicateLevelUpMovesTest() {
        // The following Pokemon can learn these moves more than once
        List<PokemonMovePair> exceptions = Arrays.asList(
                new PokemonMovePair(PokemonNamesies.SMEARGLE, AttackNamesies.SKETCH),
                new PokemonMovePair(PokemonNamesies.PUMPKABOO, AttackNamesies.TRICK_OR_TREAT),
                new PokemonMovePair(PokemonNamesies.GOURGEIST, AttackNamesies.TRICK_OR_TREAT)
        );

        for (PokemonInfo pokemonInfo : PokemonList.instance()) {
            List<LevelUpMove> levelUpMoves = pokemonInfo.getLevelUpMoves();

            MultiMap<AttackNamesies, Integer> map = new MultiMap<>();
            for (LevelUpMove levelUpMove : levelUpMoves) {
                int currentLevel = levelUpMove.getLevel();
                AttackNamesies attack = levelUpMove.getMove();
                PokemonMovePair currentPair = new PokemonMovePair(pokemonInfo.namesies(), attack);

                // We've seen this attack already
                if (map.containsKey(attack)) {
                    List<Integer> levels = map.get(attack);
                    String message = StringUtils.spaceSeparated(pokemonInfo.getName(), currentLevel, attack.getName(), levels);

                    // No duplicates for the same level/move combination
                    Assert.assertFalse(message, levels.contains(currentLevel));

                    // Default moves should be first in the order, and there can be no default level collisions
                    Assert.assertFalse(message, levelUpMove.isDefaultLevel());
                    Assert.assertTrue(message, currentLevel > 1);

                    // However, it is possible to learn a move at a regular level as well as a default level
                    if (!exceptions.contains(currentPair)) {
                        // There should be exactly one collision at this point and IT BETTER BE A DEFAULT LEVEL
                        // Also when I say default level I do not include evolution level (that should be only once)
                        Assert.assertEquals(message, 1, levels.size());
                        Assert.assertEquals(message, 1, levels.get(0).intValue());
                    }
                }

                map.put(attack, currentLevel);
            }

            for (Entry<AttackNamesies, List<Integer>> entry : map.entrySet()) {
                AttackNamesies attack = entry.getKey();
                PokemonMovePair pair = new PokemonMovePair(pokemonInfo.namesies(), attack);
                if (exceptions.contains(pair)) {
                    continue;
                }

                List<Integer> levels = entry.getValue();
                String message = StringUtils.spaceSeparated(pokemonInfo.getName(), attack.getName(), levels.size());
                TestUtils.assertInclusiveRange(message, 1, 2, levels.size());
            }
        }

        // Default level tests
        Assert.assertTrue(LevelUpMove.isDefaultLevel(PokemonInfo.EVOLUTION_LEVEL_LEARNED));
        Assert.assertTrue(LevelUpMove.isDefaultLevel(0)); // Evolution level
        Assert.assertTrue(LevelUpMove.isDefaultLevel(1)); // Default level
        for (int level = 2; level <= PartyPokemon.MAX_LEVEL; level++) {
            Assert.assertFalse(LevelUpMove.isDefaultLevel(level));
        }
    }

    @Test
    public void stockpileTest() {
        for (PokemonInfo pokemonInfo : PokemonList.instance()) {
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
    public void evTest() {
        for (PokemonInfo pokemonInfo : PokemonList.instance()) {
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
        for (PokemonInfo pokemonInfo : PokemonList.instance()) {
            AbilityNamesies[] abilities = pokemonInfo.getAbilities();
            String message = StringUtils.spaceSeparated(pokemonInfo.getName(), abilities);

            // Must be between sizes 1 and 3
            TestUtils.assertInclusiveRange(message, 1, 3, abilities.length);
            Assert.assertEquals(message, abilities.length, pokemonInfo.numAbilities());

            Set<AbilityNamesies> seen = EnumSet.noneOf(AbilityNamesies.class);
            for (AbilityNamesies ability : abilities) {
                Assert.assertTrue(message, pokemonInfo.hasAbility(ability));

                // Make sure no duplicate abilities
                Assert.assertFalse(message, seen.contains(ability));
                seen.add(ability);

                // No-Ability is not a valid ability
                Assert.assertNotEquals(message, AbilityNamesies.NO_ABILITY, ability);

                // Flower Veil was changed and only makes sense for Grass-type Pokemon now
                // Levitate doesn't makes sense for Flying-type Pokemon
                if (ability == AbilityNamesies.FLOWER_VEIL) {
                    Assert.assertTrue(message, pokemonInfo.isType(Type.GRASS));
                } else if (ability == AbilityNamesies.LEVITATE) {
                    Assert.assertFalse(message, pokemonInfo.isType(Type.FLYING));
                }
            }

            // Must only return true for hasAbility if in the list
            for (AbilityNamesies ability : AbilityNamesies.values()) {
                Assert.assertEquals(message, seen.contains(ability), pokemonInfo.hasAbility(ability));
            }

            // Cannot evolve from two to three abilities
            if (abilities.length == 2) {
                assertMaxAbilities(message, pokemonInfo, 2);
            } else if (abilities.length == 1 && pokemonInfo.getBaseEvolution() == pokemonInfo.namesies()) {
                // If base evolution is 1, then all need to be 1
                // Note: Okay for middle evolution to go from 1 to 2 or 3
                assertMaxAbilities(message, pokemonInfo, 1);
            }

            // Don't worry about it (but really if this fails change the message in Iron Fist or Scrappy)
            if (pokemonInfo.namesies() == PokemonNamesies.PANGORO) {
                Assert.assertTrue(pokemonInfo.hasAbility(AbilityNamesies.MOLD_BREAKER));
                Assert.assertTrue(pokemonInfo.hasAbility(AbilityNamesies.IRON_FIST));
                Assert.assertTrue(pokemonInfo.hasAbility(AbilityNamesies.SCRAPPY));
            }
        }
    }

    // Asserts that this Pokemon and all of its potential evolutions (across multiple levels) have a max of maxAbilities
    private void assertMaxAbilities(String message, PokemonInfo pokemonInfo, int maxAbilities) {
        for (PokemonNamesies evolution : pokemonInfo.getEvolution().getEvolutions()) {
            PokemonInfo evolutionInfo = evolution.getInfo();
            AbilityNamesies[] evolutionAbilities = evolutionInfo.getAbilities();
            String evolutionMessage = message + " -> #" + evolutionInfo.getNumber() + " " + evolution.getName() + " " + Arrays.toString(evolutionAbilities);

            Assert.assertTrue(evolutionMessage, evolutionAbilities.length <= maxAbilities);
            assertMaxAbilities(message, evolutionInfo, maxAbilities);
        }
    }

    @Test
    public void typeTest() {
        for (PokemonInfo pokemonInfo : PokemonList.instance()) {
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
    public void eggGroupTest() {
        Set<EggGroup> allGroups = EnumSet.allOf(EggGroup.class);
        EggGroup[] noEggs = new EggGroup[] { EggGroup.NO_EGGS };

        for (PokemonInfo pokemonInfo : PokemonList.instance()) {
            PokemonNamesies namesies = pokemonInfo.namesies();
            EggGroup[] eggGroups = pokemonInfo.getEggGroups();
            String name = pokemonInfo.getName();

            // Pokemon can either have one or two egg groups
            TestUtils.assertInclusiveRange(name, 1, 2, eggGroups.length);

            // If two egg groups, cannot be the same
            if (eggGroups.length == 2) {
                Assert.assertNotEquals(name, eggGroups[0], eggGroups[1]);
            }

            // Egg group is Ditto if and only if the Pokemon is Ditto
            Assert.assertEquals(name, eggGroups[0] == EggGroup.DITTO, namesies == PokemonNamesies.DITTO);

            for (EggGroup eggGroup : eggGroups) {
                // If cannot produce eggs, then NO_EGGS should be the only egg group
                // Similarily, the Ditto egg group is standalone
                if (eggGroup == EggGroup.NO_EGGS || eggGroup == EggGroup.DITTO) {
                    Assert.assertEquals(name, 1, eggGroups.length);
                }

                allGroups.remove(eggGroup);
            }

            // Make sure egg groups are consistent across evolutions (unless is a baby)
            PokemonNamesies[] evolutions = pokemonInfo.getEvolution().getEvolutions();
            for (PokemonNamesies evolution : evolutions) {
                String message = name + " " + evolution.getName();
                PokemonInfo evolutionInfo = evolution.getInfo();

                // Baby Pokemon can never breed (but can always be bred)
                if (pokemonInfo.isBabyPokemon()) {
                    Assert.assertArrayEquals(message, noEggs, eggGroups);
                    Assert.assertTrue(message, GeneralUtils.contains(EggGroup.NO_EGGS, eggGroups));
                    Assert.assertFalse(message, GeneralUtils.contains(EggGroup.NO_EGGS, evolutionInfo.getEggGroups()));
                } else {
                    assertSameEggGroups(namesies, evolution);
                }

                // Evolved Pokemon cannot be babies
                Assert.assertFalse(message, evolutionInfo.isBabyPokemon());
            }
        }

        // Make sure every egg group is seen at least once
        Assert.assertTrue(allGroups.toString(), allGroups.isEmpty());

        // Just to be sure
        assertSameEggGroups(PokemonNamesies.NIDORAN_F, PokemonNamesies.NIDORAN_M);
        assertSameEggGroups(PokemonNamesies.VOLBEAT, PokemonNamesies.ILLUMISE);
    }

    private void assertSameEggGroups(PokemonNamesies first, PokemonNamesies second) {
        Assert.assertArrayEquals(
                first.getName() + " " + second.getName(),
                first.getInfo().getEggGroups(),
                second.getInfo().getEggGroups()
        );
    }

    @Test
    public void nameLengthTest() {
        int maxLength = 0;
        for (PokemonInfo pokemonInfo : PokemonList.instance()) {
            String name = pokemonInfo.getName();
            int nameLength = name.length();

            // Make sure name length is in range and update max length
            TestUtils.assertInclusiveRange(name, 3, PartyPokemon.MAX_NAME_LENGTH, nameLength);
            maxLength = Math.max(maxLength, nameLength);
        }

        // Max length should match the longest Pokemon name
        Assert.assertEquals(PartyPokemon.MAX_NAME_LENGTH, maxLength);
    }

    @Test
    public void attributesTest() {
        // Testing for other basic attributes like height, weight, catch rate
        for (PokemonInfo pokemonInfo : PokemonList.instance()) {
            String name = pokemonInfo.getName();

            // Height and weight must be positive
            TestUtils.assertPositive(name, pokemonInfo.getHeightInches());
            TestUtils.assertPositive(name, pokemonInfo.getWeight());

            // Catch rate must be between 3 and 255
            TestUtils.assertInclusiveRange(name, 3, 255, pokemonInfo.getCatchRate());

            // Egg steps must be divisible by 256
            Assert.assertEquals(name, 0, pokemonInfo.getEggSteps()%256);

            // Capital classification
            Assert.assertTrue(name, Character.isUpperCase(pokemonInfo.getClassification().charAt(0)));

            // Flavor text should start with a capital letter and end with either period or exclamation
            // And the middle should only be valid characters
            String flavorText = pokemonInfo.getFlavorText();
            TestUtils.assertDescription(name, flavorText, "[A-Z][a-zA-Z0-9.,'/:é°\"\\- ]+[.!]");

            // Gender ratio must be between 0 and 8 if not genderless
            int femaleRatio = pokemonInfo.getFemaleRatio();
            if (femaleRatio != Gender.GENDERLESS_VALUE) {
                TestUtils.assertInclusiveRange(name, 0, 8, femaleRatio);
            }
        }

        // Genderless value cannot be in the same range as the female ratios
        TestUtils.assertOutsideRange(0, 8, Gender.GENDERLESS_VALUE);
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
        Assert.assertEquals(Gender.GENDERLESS_VALUE, pokemonInfo.getFemaleRatio());

        Assert.assertArrayEquals(new EggGroup[] { EggGroup.NO_EGGS }, pokemonInfo.getEggGroups());

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
