package test.pokemon;

import item.ItemNamesies;
import item.use.EvolutionItem;
import org.junit.Assert;
import org.junit.Test;
import pokemon.ability.AbilityNamesies;
import pokemon.evolution.BaseEvolution;
import pokemon.evolution.ConditionEvolution;
import pokemon.evolution.Evolution;
import pokemon.evolution.EvolutionMethod;
import pokemon.evolution.ExtraEvolution;
import pokemon.evolution.ItemEvolution;
import pokemon.evolution.MultipleEvolution;
import pokemon.evolution.NoEvolution;
import pokemon.species.PokemonInfo;
import pokemon.species.PokemonList;
import pokemon.species.PokemonNamesies;
import test.general.BaseTest;
import util.MultiMap;
import util.Triplet;
import util.string.StringUtils;

import java.util.AbstractMap.SimpleEntry;
import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class EvolutionTest extends BaseTest {
    // Tests to make sure all items that evolve pokemon are evolution items
    @Test
    public void itemTest() {
        // Keep track of all the items that evolve pokemon
        EvolutionItems evolutionItems = new EvolutionItems();

        // Verifies that items are evolution items if and only if they are used to evolve a pokemon
        for (ItemNamesies itemNamesies : ItemNamesies.values()) {
            boolean evolutionItem = itemNamesies.getItem() instanceof EvolutionItem;
            boolean usedItem = evolutionItems.containsKey(itemNamesies);
            Assert.assertEquals(itemNamesies.getName(), evolutionItem, usedItem);
        }
    }

    // Singleton map from each evolution item to each Pokemon that evolves using that item
    private static class EvolutionItems extends MultiMap<ItemNamesies, PokemonNamesies> {
        public EvolutionItems() {
            // Check evolution method of each pokemon checking for item evolution methods
            for (PokemonInfo pokemonInfo : PokemonList.instance()) {
                addItemEvolution(pokemonInfo.namesies(), pokemonInfo.getEvolution());
            }
        }

        private void addItemEvolution(PokemonNamesies pokes, Evolution evolution) {
            if (evolution instanceof ItemEvolution) {
                super.put(((ItemEvolution)evolution).getItem(), pokes);
            } else if (evolution instanceof MultipleEvolution) {
                for (Evolution evo : ((MultipleEvolution)evolution).getFullEvolutions()) {
                    addItemEvolution(pokes, evo);
                }
            } else if (evolution instanceof ConditionEvolution) {
                addItemEvolution(pokes, ((ConditionEvolution)evolution).getEvolution());
            } else if (evolution instanceof ExtraEvolution) {
                addItemEvolution(pokes, ((ExtraEvolution)evolution).getEvolution());
            } else {
                // Make sure we're always breaking evolution down to the base
                Assert.assertTrue(pokes.getName(), evolution instanceof BaseEvolution || evolution instanceof NoEvolution);
            }
        }
    }

    private void addItemEvolution(PokemonNamesies pokes, Evolution evolution, MultiMap<ItemNamesies, PokemonNamesies> evolutionItems) {
        if (evolution instanceof ItemEvolution) {
            evolutionItems.put(((ItemEvolution)evolution).getItem(), pokes);
        } else if (evolution instanceof MultipleEvolution) {
            for (Evolution evo : ((MultipleEvolution)evolution).getFullEvolutions()) {
                addItemEvolution(pokes, evo, evolutionItems);
            }
        }
    }

    @Test
    public void sameAbilityTest() {
        // Pokemon with the same ability when they evolve with one ability slot
        sameAbilityTest(
                PokemonNamesies.GASTLY, PokemonNamesies.HAUNTER,
                AbilityNamesies.LEVITATE
        );

        // Pokemon with the same ability when they evolve with two ability slots
        sameAbilityTest(
                PokemonNamesies.BULBASAUR, PokemonNamesies.IVYSAUR,
                AbilityNamesies.OVERGROW, AbilityNamesies.CHLOROPHYLL
        );

        // Pokemon with the same ability when they evolve with three ability slots
        // (This is secretly also confirming they have Flame Body because they need it)
        // (I'm serious if this fails don't just fix here THEY NEED FLAME BODY THEY NEED IT)
        sameAbilityTest(
                PokemonNamesies.PONYTA, PokemonNamesies.RAPIDASH,
                AbilityNamesies.RUN_AWAY, AbilityNamesies.FLASH_FIRE, AbilityNamesies.FLAME_BODY
        );
    }

    // Goes through each possible ability and makes sure its evolution has the same ability when evolved
    private void sameAbilityTest(PokemonNamesies baseName, PokemonNamesies evolutionName, AbilityNamesies... abilities) {
        confirmAbilities(baseName, abilities);
        confirmAbilities(evolutionName, abilities);
        for (AbilityNamesies ability : abilities) {
            abilitySameSlotsTest(new AbilityInfo(baseName, evolutionName, ability));
        }
    }

    @Test
    public void differentAbilitySameSlotsTest() {
        // Both single distinct abilities
        differentAbilitySameSlotsTest(
                PokemonNamesies.HAUNTER, List.of(AbilityNamesies.LEVITATE),
                PokemonNamesies.GENGAR, List.of(AbilityNamesies.CURSED_BODY),
                ItemNamesies.SPELL_TAG
        );

        // Both two distinct abilities
        differentAbilitySameSlotsTest(
                PokemonNamesies.MAGIKARP, List.of(AbilityNamesies.SWIFT_SWIM, AbilityNamesies.RATTLED),
                PokemonNamesies.GYARADOS, List.of(AbilityNamesies.INTIMIDATE, AbilityNamesies.MOXIE)
        );

        // Both two abilities, first same, second distinct
        differentAbilitySameSlotsTest(
                PokemonNamesies.ODDISH, List.of(AbilityNamesies.CHLOROPHYLL, AbilityNamesies.RUN_AWAY),
                PokemonNamesies.GLOOM, List.of(AbilityNamesies.CHLOROPHYLL, AbilityNamesies.STENCH)
        );

        // Both two abilities, first distinct, second same
        differentAbilitySameSlotsTest(
                PokemonNamesies.ELECTABUZZ, List.of(AbilityNamesies.STATIC, AbilityNamesies.VITAL_SPIRIT),
                PokemonNamesies.ELECTIVIRE, List.of(AbilityNamesies.MOTOR_DRIVE, AbilityNamesies.VITAL_SPIRIT),
                ItemNamesies.ELECTIRIZER
        );

        // Both three abilities, first same, second and third distinct
        differentAbilitySameSlotsTest(
                PokemonNamesies.KARRABLAST, List.of(AbilityNamesies.SWARM, AbilityNamesies.SHED_SKIN, AbilityNamesies.NO_GUARD),
                PokemonNamesies.ESCAVALIER, List.of(AbilityNamesies.SWARM, AbilityNamesies.SHELL_ARMOR, AbilityNamesies.OVERCOAT),
                ItemNamesies.METAL_COAT
        );

        // Both three abilities, second same, first and third distinct
        differentAbilitySameSlotsTest(
                PokemonNamesies.VENONAT, List.of(AbilityNamesies.COMPOUND_EYES, AbilityNamesies.TINTED_LENS, AbilityNamesies.RUN_AWAY),
                PokemonNamesies.VENOMOTH, List.of(AbilityNamesies.SHIELD_DUST, AbilityNamesies.TINTED_LENS, AbilityNamesies.WONDER_SKIN)
        );

        // Note: No Pokemon found to test with third same, first and second distinct

        // Both three abilities, first and second same, third distinct
        differentAbilitySameSlotsTest(
                PokemonNamesies.NIDORINA, List.of(AbilityNamesies.POISON_POINT, AbilityNamesies.RIVALRY, AbilityNamesies.HUSTLE),
                PokemonNamesies.NIDOQUEEN, List.of(AbilityNamesies.POISON_POINT, AbilityNamesies.RIVALRY, AbilityNamesies.SHEER_FORCE),
                ItemNamesies.MOON_STONE
        );

        // Both three abilities, second and third same, first distinct
        differentAbilitySameSlotsTest(
                PokemonNamesies.MEOWTH, List.of(AbilityNamesies.PICKUP, AbilityNamesies.TECHNICIAN, AbilityNamesies.UNNERVE),
                PokemonNamesies.PERSIAN, List.of(AbilityNamesies.LIMBER, AbilityNamesies.TECHNICIAN, AbilityNamesies.UNNERVE)
        );

        // Both three abilities, first and third same, second distinct
        differentAbilitySameSlotsTest(
                PokemonNamesies.RHYDON, List.of(AbilityNamesies.LIGHTNING_ROD, AbilityNamesies.ROCK_HEAD, AbilityNamesies.RECKLESS),
                PokemonNamesies.RHYPERIOR, List.of(AbilityNamesies.LIGHTNING_ROD, AbilityNamesies.SOLID_ROCK, AbilityNamesies.RECKLESS),
                ItemNamesies.PROTECTOR
        );

        // Both three distinct abilities
        differentAbilitySameSlotsTest(
                PokemonNamesies.FEEBAS, List.of(AbilityNamesies.SWIFT_SWIM, AbilityNamesies.OBLIVIOUS, AbilityNamesies.ADAPTABILITY),
                PokemonNamesies.MILOTIC, List.of(AbilityNamesies.MARVEL_SCALE, AbilityNamesies.COMPETITIVE, AbilityNamesies.CUTE_CHARM),
                ItemNamesies.PRISM_SCALE
        );
    }

    private void differentAbilitySameSlotsTest(PokemonNamesies basePokemon,
                                               List<AbilityNamesies> baseAbilities,
                                               PokemonNamesies evolutionPokemon,
                                               List<AbilityNamesies> evolutionAbilities) {
        differentAbilitySameSlotsTest(basePokemon, baseAbilities, evolutionPokemon, evolutionAbilities, null);
    }

    private void differentAbilitySameSlotsTest(PokemonNamesies basePokemon,
                                               List<AbilityNamesies> baseAbilities,
                                               PokemonNamesies evolutionPokemon,
                                               List<AbilityNamesies> evolutionAbilities,
                                               ItemNamesies evolutionItem) {
        confirmAbilities(basePokemon, baseAbilities);
        confirmAbilities(evolutionPokemon, evolutionAbilities);

        // This test is for checking the same number of ability slots
        Assert.assertEquals(baseAbilities.size(), evolutionAbilities.size());

        for (int i = 0; i < baseAbilities.size(); i++) {
            abilitySameSlotsTest(new AbilityInfo(
                    basePokemon, evolutionPokemon, baseAbilities.get(i), evolutionAbilities.get(i), evolutionItem
            ));
        }
    }

    // Used for testing evolution between Pokemon with the same number of ability slots
    private void abilitySameSlotsTest(AbilityInfo testInfo) {
        abilityTest(testInfo, true);
    }

    @Test
    public void abilityDifferentSlotsTest() {
        // Two abilities to one ability
        abilityDifferentSlotsTest(
                PokemonNamesies.CHARMELEON, PokemonNamesies.RIZARDON, ItemNamesies.DRAGON_FANG,
                List.of(AbilityNamesies.BLAZE, AbilityNamesies.SOLAR_POWER),
                List.of(AbilityNamesies.TOUGH_CLAWS),
                new SimpleEntry<>(AbilityNamesies.BLAZE, AbilityNamesies.TOUGH_CLAWS),
                new SimpleEntry<>(AbilityNamesies.SOLAR_POWER, AbilityNamesies.TOUGH_CLAWS)
        );

        // Three abilities to one ability
        abilityDifferentSlotsTest(
                PokemonNamesies.TRAPINCH, PokemonNamesies.VIBRAVA,
                List.of(AbilityNamesies.HYPER_CUTTER, AbilityNamesies.ARENA_TRAP, AbilityNamesies.SHEER_FORCE),
                List.of(AbilityNamesies.LEVITATE),
                new SimpleEntry<>(AbilityNamesies.HYPER_CUTTER, AbilityNamesies.LEVITATE),
                new SimpleEntry<>(AbilityNamesies.ARENA_TRAP, AbilityNamesies.LEVITATE),
                new SimpleEntry<>(AbilityNamesies.SHEER_FORCE, AbilityNamesies.LEVITATE)
        );

        // Three abilities to two abilities, second and third same
        abilityDifferentSlotsTest(
                PokemonNamesies.PATRAT, PokemonNamesies.WATCHOG,
                List.of(AbilityNamesies.RUN_AWAY, AbilityNamesies.KEEN_EYE, AbilityNamesies.ANALYTIC),
                List.of(AbilityNamesies.KEEN_EYE, AbilityNamesies.ANALYTIC),
                new SimpleEntry<>(AbilityNamesies.RUN_AWAY, AbilityNamesies.KEEN_EYE),
                new SimpleEntry<>(AbilityNamesies.KEEN_EYE, AbilityNamesies.KEEN_EYE),
                new SimpleEntry<>(AbilityNamesies.ANALYTIC, AbilityNamesies.ANALYTIC)
        );

        // Three abilities to two abilities, all different
        abilityDifferentSlotsTest(
                PokemonNamesies.EEVEE, PokemonNamesies.ESPEON, ItemNamesies.SUN_STONE,
                List.of(AbilityNamesies.RUN_AWAY, AbilityNamesies.ADAPTABILITY, AbilityNamesies.ANTICIPATION),
                List.of(AbilityNamesies.SYNCHRONIZE, AbilityNamesies.MAGIC_BOUNCE),
                new SimpleEntry<>(AbilityNamesies.RUN_AWAY, AbilityNamesies.SYNCHRONIZE),
                new SimpleEntry<>(AbilityNamesies.ADAPTABILITY, AbilityNamesies.SYNCHRONIZE),
                new SimpleEntry<>(AbilityNamesies.ANTICIPATION, AbilityNamesies.MAGIC_BOUNCE)
        );
    }

    @SafeVarargs
    private void abilityDifferentSlotsTest(PokemonNamesies basePokemon,
                                           PokemonNamesies evolutionPokemon,
                                           List<AbilityNamesies> baseAbilities,
                                           List<AbilityNamesies> evolutionAbilities,
                                           Entry<AbilityNamesies, AbilityNamesies>... abilities) {
        abilityDifferentSlotsTest(
                basePokemon, evolutionPokemon, null, baseAbilities, evolutionAbilities, abilities
        );
    }

    @SafeVarargs
    private void abilityDifferentSlotsTest(PokemonNamesies basePokemon,
                                           PokemonNamesies evolutionPokemon,
                                           ItemNamesies evolutionItem,
                                           List<AbilityNamesies> baseAbilities,
                                           List<AbilityNamesies> evolutionAbilities,
                                           Entry<AbilityNamesies, AbilityNamesies>... abilities) {
        confirmAbilities(basePokemon, baseAbilities);
        confirmAbilities(evolutionPokemon, evolutionAbilities);

        // This test is for checking the different number of ability slots
        Assert.assertNotEquals(baseAbilities.size(), evolutionAbilities.size());

        // Each entry should correspond to each base ability
        Assert.assertEquals(baseAbilities.size(), abilities.length);

        Set<AbilityNamesies> baseSeen = EnumSet.copyOf(baseAbilities);
        Set<AbilityNamesies> evolutionSeen = EnumSet.copyOf(evolutionAbilities);

        for (Entry<AbilityNamesies, AbilityNamesies> abilityPair : abilities) {
            AbilityNamesies baseAbility = abilityPair.getKey();
            AbilityNamesies evolutionAbility = abilityPair.getValue();

            abilityDifferentSlotsTest(new AbilityInfo(
                    basePokemon, evolutionPokemon, baseAbility, evolutionAbility, evolutionItem
            ));

            Assert.assertTrue(baseSeen.contains(baseAbility));
            baseSeen.remove(baseAbility);
            evolutionSeen.remove(evolutionAbility);
        }

        // Make sure every ability has been tested
        Assert.assertTrue(baseSeen.isEmpty());
        Assert.assertTrue(evolutionSeen.isEmpty());
    }

    // Used for testing evolution between Pokemon with a different number of ability slots
    private void abilityDifferentSlotsTest(AbilityInfo testInfo) {
        abilityTest(testInfo, false);
    }

    private void abilityTest(AbilityInfo testInfo, boolean sameSlots) {
        // Create the base Pokemon and give the base ability
        TestPokemon pokemon = TestPokemon.newPlayerPokemon(testInfo.basePokemon);
        pokemon.setAbility(testInfo.baseAbility);

        abilityTest(testInfo, sameSlots, pokemon);
    }

    @Test
    public void multipleAbilityTest() {
        // Two -> One -> Two (first and last same abilities)
        multipleAbilityTest(
                PokemonNamesies.SCATTERBUG, PokemonNamesies.SPEWPA, PokemonNamesies.VIVILLON,
                List.of(AbilityNamesies.SHIELD_DUST, AbilityNamesies.COMPOUND_EYES),
                List.of(AbilityNamesies.SHED_SKIN),
                List.of(AbilityNamesies.SHIELD_DUST, AbilityNamesies.COMPOUND_EYES),
                new Triplet<>(AbilityNamesies.SHIELD_DUST, AbilityNamesies.SHED_SKIN, AbilityNamesies.SHIELD_DUST),
                new Triplet<>(AbilityNamesies.COMPOUND_EYES, AbilityNamesies.SHED_SKIN, AbilityNamesies.COMPOUND_EYES)
        );

        // Two -> One -> Two (first and last different abilities)
        multipleAbilityTest(
                PokemonNamesies.CATERPIE, PokemonNamesies.METAPOD, PokemonNamesies.BUTTERFREE,
                List.of(AbilityNamesies.SHIELD_DUST, AbilityNamesies.RUN_AWAY),
                List.of(AbilityNamesies.SHED_SKIN),
                List.of(AbilityNamesies.COMPOUND_EYES, AbilityNamesies.TINTED_LENS),
                new Triplet<>(AbilityNamesies.SHIELD_DUST, AbilityNamesies.SHED_SKIN, AbilityNamesies.COMPOUND_EYES),
                new Triplet<>(AbilityNamesies.RUN_AWAY, AbilityNamesies.SHED_SKIN, AbilityNamesies.TINTED_LENS)
        );

        // Note: No Pokemon found with Three -> One -> Three
    }

    @SafeVarargs
    private void multipleAbilityTest(PokemonNamesies basePokemon,
                                     PokemonNamesies middlePokemon,
                                     PokemonNamesies evolutionPokemon,
                                     List<AbilityNamesies> baseAbilities,
                                     List<AbilityNamesies> middleAbilities,
                                     List<AbilityNamesies> evolutionAbilities,
                                     Triplet<AbilityNamesies, AbilityNamesies, AbilityNamesies>... abilities) {
        confirmAbilities(basePokemon, baseAbilities);
        confirmAbilities(middlePokemon, middleAbilities);
        confirmAbilities(evolutionPokemon, evolutionAbilities);

        // This test requires the first and last evolution to be the same size (not 1) and the middle evolution to be 1
        // (If they're the same size then should just use a simpler test without multiple pieces)
        Assert.assertEquals(baseAbilities.size(), evolutionAbilities.size());
        Assert.assertNotEquals(baseAbilities.size(), middleAbilities.size());
        Assert.assertEquals(1, middleAbilities.size());

        // Each entry should correspond to each base ability
        Assert.assertEquals(baseAbilities.size(), abilities.length);

        Set<AbilityNamesies> baseSeen = EnumSet.copyOf(baseAbilities);
        Set<AbilityNamesies> middleSeen = EnumSet.copyOf(middleAbilities);
        Set<AbilityNamesies> evolutionSeen = EnumSet.copyOf(evolutionAbilities);

        for (Triplet<AbilityNamesies, AbilityNamesies, AbilityNamesies> abilityPair : abilities) {
            AbilityNamesies baseAbility = abilityPair.getFirst();
            AbilityNamesies middleAbility = abilityPair.getSecond();
            AbilityNamesies evolutionAbility = abilityPair.getThird();

            multipleAbilityTest(
                    new AbilityInfo(basePokemon, middlePokemon, baseAbility, middleAbility, null),
                    new AbilityInfo(middlePokemon, evolutionPokemon, middleAbility, evolutionAbility, null)
            );

            Assert.assertTrue(baseSeen.contains(baseAbility));
            Assert.assertTrue(evolutionSeen.contains(evolutionAbility));
            baseSeen.remove(baseAbility);
            middleSeen.remove(middleAbility);
            evolutionSeen.remove(evolutionAbility);
        }

        // Make sure every ability has been tested
        Assert.assertTrue(baseSeen.isEmpty());
        Assert.assertTrue(middleSeen.isEmpty());
        Assert.assertTrue(evolutionSeen.isEmpty());
    }

    private void multipleAbilityTest(AbilityInfo firstEvolution, AbilityInfo secondEvolution) {
        // Testing for evolving a Pokemon twice so it needs to be the same Pokemon in the middle
        Assert.assertEquals(firstEvolution.evolutionPokemon, secondEvolution.basePokemon);

        // Create the base Pokemon and give the base ability
        TestPokemon pokemon = TestPokemon.newPlayerPokemon(firstEvolution.basePokemon);
        pokemon.setAbility(firstEvolution.baseAbility);

        abilityTest(firstEvolution, false, pokemon);
        abilityTest(secondEvolution, false, pokemon);
    }

    // Used for testing abilities on evolution
    // sameSlots refers to if the Pokemon and its evolution have the same number of potential abilities
    // TestPokemon is expected to already have the base ability set
    private void abilityTest(AbilityInfo testInfo, boolean sameSlots, TestPokemon pokemon) {
        PokemonInfo baseInfo = testInfo.baseInfo();
        PokemonInfo evolutionInfo = testInfo.evolutionInfo();

        // Test only makes sense if these Pokemon can naturally receive these abilities
        Assert.assertTrue(baseInfo.hasAbility(testInfo.baseAbility));
        Assert.assertTrue(evolutionInfo.hasAbility(testInfo.evolutionAbility));

        // This test is also only for evolutions with the same number of ability slots
        Assert.assertEquals(sameSlots, baseInfo.numAbilities() == evolutionInfo.numAbilities());

        // Confirm ability index (if not the only ability)
        int baseAbilityIndex = pokemon.getAbilityIndex();
        if (pokemon.getPokemonInfo().numAbilities() > 1) {
            Assert.assertEquals(testInfo.baseAbilityIndex, baseAbilityIndex);
        }

        // Evolve the Pokemon (by level up)
        BaseEvolution evolution = testInfo.getEvolution(pokemon);
        Assert.assertNotNull(evolution);
        Assert.assertEquals(testInfo.evolutionPokemon, evolution.getEvolution());
        pokemon.evolve(evolution);

        // Make sure its the expected Pokemon and that it has the expected ability
        pokemon.assertSpecies(testInfo.evolutionPokemon);
        pokemon.assertAbility(testInfo.evolutionAbility);

        // Confirm ability index (if not the only ability)
        int evolutionAbilityIndex = pokemon.getAbilityIndex();
        if (pokemon.getPokemonInfo().numAbilities() > 1) {
            Assert.assertEquals(testInfo.evolutionAbilityIndex, evolutionAbilityIndex);
        }

        if (sameSlots) {
            // Since these Pokemon always have the same number of slots, the ability index should never change
            Assert.assertEquals(baseAbilityIndex, evolutionAbilityIndex);
        }
    }

    private void confirmAbilities(PokemonNamesies pokemonNamesies, List<AbilityNamesies> abilities) {
        confirmAbilities(pokemonNamesies, abilities.toArray(new AbilityNamesies[0]));
    }

    private void confirmAbilities(PokemonNamesies pokemonNamesies, AbilityNamesies... abilities) {
        Assert.assertArrayEquals(pokemonNamesies.getName(), pokemonNamesies.getInfo().getAbilities(), abilities);
    }

    private int getAbilityIndex(PokemonNamesies pokemonNamesies, AbilityNamesies ability) {
        PokemonInfo pokemonInfo = pokemonNamesies.getInfo();
        AbilityNamesies[] abilities = pokemonInfo.getAbilities();
        String message = StringUtils.spaceSeparated(pokemonNamesies, ability, abilities);

        Assert.assertTrue(message, pokemonInfo.hasAbility(ability));

        for (int i = 0; i < abilities.length; i++) {
            if (abilities[i] == ability) {
                return i;
            }
        }

        Assert.fail(message);
        return -1;
    }

    private class AbilityInfo {
        private final PokemonNamesies basePokemon;
        private final PokemonNamesies evolutionPokemon;

        private final AbilityNamesies baseAbility;
        private final AbilityNamesies evolutionAbility;

        private final int baseAbilityIndex;
        private final int evolutionAbilityIndex;

        private final EvolutionMethod method;
        private final ItemNamesies evolutionItem;

        public AbilityInfo(PokemonNamesies basePokemon, PokemonNamesies evolutionPokemon,
                           AbilityNamesies baseAbility, AbilityNamesies evolutionAbility,
                           ItemNamesies evolutionItem) {
            this.basePokemon = basePokemon;
            this.evolutionPokemon = evolutionPokemon;

            this.baseAbility = baseAbility;
            this.evolutionAbility = evolutionAbility;

            this.baseAbilityIndex = getAbilityIndex(basePokemon, baseAbility);
            this.evolutionAbilityIndex = getAbilityIndex(evolutionPokemon, evolutionAbility);

            if (evolutionItem == null) {
                this.method = EvolutionMethod.LEVEL;
                this.evolutionItem = null;
            } else {
                this.method = EvolutionMethod.ITEM;
                this.evolutionItem = evolutionItem;
            }
        }

        public AbilityInfo(PokemonNamesies basePokemon, PokemonNamesies evolutionPokemon,
                           AbilityNamesies baseAbility, AbilityNamesies evolutionAbility) {
            this(basePokemon, evolutionPokemon, baseAbility, evolutionAbility, null);
        }

        public AbilityInfo(PokemonNamesies basePokemon, PokemonNamesies evolutionPokemon,
                           AbilityNamesies sameAbility) {
            this(basePokemon, evolutionPokemon, sameAbility, sameAbility);
        }

        public PokemonInfo baseInfo() {
            return this.basePokemon.getInfo();
        }

        public PokemonInfo evolutionInfo() {
            return this.evolutionPokemon.getInfo();
        }

        public BaseEvolution getEvolution(TestPokemon pokemon) {
            return pokemon.getPokemonInfo().getEvolution().getEvolution(this.method, pokemon, this.evolutionItem);
        }
    }
}
