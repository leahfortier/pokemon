package test;

import battle.attack.AttackNamesies;
import battle.attack.Move;
import item.ItemNamesies;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import battle.ActivePokemon;
import pokemon.Gender;
import pokemon.PartyPokemon;
import pokemon.PokemonInfo;
import pokemon.PokemonNamesies;
import pokemon.Stat;
import pokemon.breeding.Breeding;
import pokemon.breeding.Eggy;
import util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BreedingTest extends BaseTest {
    private static Breeding breeding;

    @BeforeClass
    public static void setup() {
        breeding = Breeding.instance();
    }

    @Test
    public void testCanBreed() {
        ActivePokemon maleRapidash = getParent(PokemonNamesies.RAPIDASH, Gender.MALE);
        ActivePokemon femaleRapidash = getParent(PokemonNamesies.RAPIDASH, Gender.FEMALE);
        ActivePokemon femaleNinetales = getParent(PokemonNamesies.NINETALES, Gender.FEMALE);
        ActivePokemon ditto = getParent(PokemonNamesies.DITTO, Gender.GENDERLESS);
        ActivePokemon magnemite = getParent(PokemonNamesies.MAGNEMITE, Gender.GENDERLESS);

        Assert.assertTrue(ditto.canBreed());
        Assert.assertTrue(magnemite.canBreed());
        Assert.assertTrue(breeding.canBreed(maleRapidash, femaleRapidash)); // Same species, opposite gender
        Assert.assertTrue(breeding.canBreed(maleRapidash, femaleNinetales)); // Same egg group, opposite gender
        Assert.assertTrue(breeding.canBreed(maleRapidash, ditto)); // Male and ditto
        Assert.assertTrue(breeding.canBreed(femaleRapidash, ditto)); // Female and ditto
        Assert.assertTrue(breeding.canBreed(magnemite, ditto)); // Genderless and ditto
    }

    @Test
    public void testCannotBreed() {
        ActivePokemon maleRapidash = getParent(PokemonNamesies.RAPIDASH, Gender.MALE);
        ActivePokemon femaleDragonair = getParent(PokemonNamesies.DRAGONAIR, Gender.FEMALE);
        ActivePokemon ditto = getParent(PokemonNamesies.DITTO, Gender.GENDERLESS);
        ActivePokemon magnemite = getParent(PokemonNamesies.MAGNEMITE, Gender.GENDERLESS);
        ActivePokemon mew = getParent(PokemonNamesies.MEW, Gender.GENDERLESS);
        ActivePokemon togepi = getParent(PokemonNamesies.TOGEPI, Gender.MALE);
        ActivePokemon togetic = getParent(PokemonNamesies.TOGETIC, Gender.FEMALE);
        Eggy eggy = new Eggy(PokemonNamesies.EXEGGCUTE);

        Assert.assertFalse(togepi.canBreed());
        Assert.assertFalse(eggy.canBreed());
        Assert.assertFalse(breeding.canBreed(maleRapidash, maleRapidash)); // Same species, same gender
        Assert.assertFalse(breeding.canBreed(magnemite, magnemite)); // Same species, both genderless
        Assert.assertFalse(breeding.canBreed(ditto, ditto)); // Two dittos
        Assert.assertFalse(breeding.canBreed(mew, ditto)); // Legendary and ditto
        Assert.assertFalse(breeding.canBreed(maleRapidash, femaleDragonair)); // Different egg group, opposite gender
        Assert.assertFalse(breeding.canBreed(togepi, togetic)); // Same egg group and baby pokemon
        Assert.assertFalse(breeding.canBreed(togepi, ditto)); // Baby pokemon and ditto
    }

    @Test
    public void testBaseEvolution() {
        ActivePokemon mommy = getParent(PokemonNamesies.RAPIDASH, Gender.FEMALE);
        ActivePokemon daddy = getParent(PokemonNamesies.RAPIDASH, Gender.MALE);
        Eggy baby = getBaby(mommy, daddy);

        // Mommy and daddy Rapidash -> baby Ponyta
        Assert.assertTrue(getFailMessage(mommy, daddy, baby), baby.isPokemon(PokemonNamesies.PONYTA));

        mommy = getParent(PokemonNamesies.NINETALES, Gender.FEMALE);
        baby = getBaby(mommy, daddy);

        // Mommy Ninetales and daddy Rapidash -> baby Vulpix
        Assert.assertTrue(getFailMessage(mommy, daddy, baby), baby.isPokemon(PokemonNamesies.VULPIX));

        mommy = getParent(PokemonNamesies.MANAPHY, Gender.GENDERLESS);
        daddy = getParent(PokemonNamesies.DITTO, Gender.GENDERLESS);
        baby = getBaby(mommy, daddy);

        // Manaphy + Ditto -> Phione
        Assert.assertTrue(getFailMessage(mommy, daddy, baby), baby.isPokemon(PokemonNamesies.PHIONE));
    }

    // TODO: Add more cases
    @Test
    public void testEggMoves() {
        ActivePokemon mommy = getParentWithMoves(PokemonNamesies.RAPIDASH, Gender.FEMALE, AttackNamesies.MORNING_SUN);
        ActivePokemon daddy = getParentWithMoves(PokemonNamesies.NINETALES, Gender.MALE,
                                                 AttackNamesies.HYPNOSIS,
                                                 AttackNamesies.SOLAR_BEAM,
                                                 AttackNamesies.FLAMETHROWER
        );
        Eggy baby = getBaby(mommy, daddy);

        Assert.assertTrue(
                getFailMessage(mommy, daddy, baby),
                baby.hasActualMove(AttackNamesies.MORNING_SUN) &&
                        baby.hasActualMove(AttackNamesies.HYPNOSIS) &&
                        baby.hasActualMove(AttackNamesies.SOLAR_BEAM) &&
                        baby.hasActualMove(AttackNamesies.FLAMETHROWER)
        );
    }

    @Test
    public void testIncense() {
        ActivePokemon mommy = getParentWithItem(PokemonNamesies.WOBBUFFET, Gender.FEMALE, ItemNamesies.LAX_INCENSE);
        ActivePokemon daddy = getParent(PokemonNamesies.WOBBUFFET, Gender.MALE);
        Eggy baby = getBaby(mommy, daddy);

        // Wobby mom with incense + daddy wobby = wynaut baby
        Assert.assertTrue(getFailMessage(mommy, daddy, baby), baby.isPokemon(PokemonNamesies.WYNAUT));

        mommy.removeItem();
        baby = getBaby(mommy, daddy);

        // Without holding the incense should be a wobby baby
        Assert.assertTrue(getFailMessage(mommy, daddy, baby), baby.isPokemon(PokemonNamesies.WOBBUFFET));

        mommy.giveItem(ItemNamesies.SEA_INCENSE);
        daddy.giveItem(ItemNamesies.ROSE_INCENSE);
        baby = getBaby(mommy, daddy);

        // Holding incorrect incense should be a wobby baby
        Assert.assertTrue(getFailMessage(mommy, daddy, baby), baby.isPokemon(PokemonNamesies.WOBBUFFET));

        mommy = getParentWithItem(PokemonNamesies.DITTO, Gender.GENDERLESS, ItemNamesies.LAX_INCENSE);
        baby = getBaby(mommy, daddy);

        // Ditto mom with incense + daddy wobby = wynaut baby
        Assert.assertTrue(getFailMessage(mommy, daddy, baby), baby.isPokemon(PokemonNamesies.WYNAUT));
    }

    @Test
    public void testEverstone() {
        final ActivePokemon mommy = getParent(PokemonNamesies.RAPIDASH, Gender.FEMALE);
        final ActivePokemon daddy = getParentWithItem(PokemonNamesies.RAPIDASH, Gender.MALE, ItemNamesies.EVERSTONE);
        final Eggy baby = getBaby(mommy, daddy);

        Assert.assertTrue(getFailMessage(mommy, daddy, baby), baby.getNature().equals(daddy.getNature()));
    }

    @Test
    public void testRandom() {
        for (int i = 0; i < 100; i++) {
            testEverstone();
        }
    }

    @Test
    public void testIvInheritance() {
        // Make sure basic inheritance is working -- should pass down three IVs
        testIvInheritance(new InheritanceRules() {});

        // Destiny knot passes down five IVs
        testIvInheritance(new InheritanceRules() {
            @Override
            public void updateParents(ActivePokemon mommy, ActivePokemon daddy) {
                mommy.giveItem(ItemNamesies.DESTINY_KNOT);
            }

            @Override
            public int numStatsToInherit() {
                return 5;
            }
        });

        // Power items pass down IV for a specific stat
        testIvInheritance(new InheritanceRules() {
            @Override
            public void updateParents(ActivePokemon mommy, ActivePokemon daddy) {
                mommy.giveItem(ItemNamesies.POWER_LENS);
                daddy.giveItem(ItemNamesies.POWER_ANKLET);
            }

            @Override
            public boolean assertTrueCondition(ActivePokemon mommy, ActivePokemon daddy, Eggy baby) {
                // Power lens passes down the Special attack stat
                // Power anklet passes down the Speed stat
                return hasEqualIVs(mommy, daddy, baby, Stat.SP_ATTACK) && hasEqualIVs(mommy, daddy, baby, Stat.SPEED);
            }

            @Override
            public void verifyEnd(boolean[][] hasParent) {
                Assert.assertFalse(hasParent[MatchType.NO_MATCH.ordinal()][Stat.SP_ATTACK.index()]);
                Assert.assertFalse(hasParent[MatchType.NO_MATCH.ordinal()][Stat.SPEED.index()]);
            }
        });
    }

    private static void testIvInheritance(InheritanceRules rules) {
        final int numStatsToInherit = rules.numStatsToInherit();

        boolean hasExactInheritance = false;
        boolean[][] hasParent = new boolean[MatchType.values().length][Stat.NUM_STATS];

        for (int i = 0; i < 1000; i++) {
            ActivePokemon mommy = getParent(PokemonNamesies.RAPIDASH, Gender.FEMALE);
            ActivePokemon daddy = getParent(PokemonNamesies.RAPIDASH, Gender.MALE);
            rules.updateParents(mommy, daddy);

            Eggy baby = getBaby(mommy, daddy);

            int[] mommyIVs = mommy.getIVs();
            int[] daddyIVs = daddy.getIVs();
            int[] babyIVs = baby.getIVs();

            int numMatches = 0;
            boolean hasMommy = false;
            boolean hasDaddy = false;

            for (int j = 0; j < Stat.NUM_STATS; j++) {
                final MatchType matchType;
                if (babyIVs[j] == mommyIVs[j]) {
                    numMatches++;
                    hasMommy = true;
                    matchType = MatchType.MOMMY;
                } else if (babyIVs[j] == daddyIVs[j]) {
                    numMatches++;
                    hasDaddy = true;
                    matchType = MatchType.DADDY;
                } else {
                    matchType = MatchType.NO_MATCH;
                }

                hasParent[matchType.ordinal()][j] = true;
            }

            if (numMatches == numStatsToInherit && hasMommy && hasDaddy) {
                hasExactInheritance = true;
            }

            Assert.assertTrue(
                    getFailMessage("Num stats to inherit: " + numStatsToInherit, mommy, daddy, baby),
                    numMatches >= numStatsToInherit
            );

            Assert.assertTrue(getFailMessage(mommy, daddy, baby), rules.assertTrueCondition(mommy, daddy, baby));
        }

        Assert.assertTrue("Does not have exactly " + numStatsToInherit + " IV matches.", hasExactInheritance);
        rules.verifyEnd(hasParent);
    }

    private static TestPokemon getParent(final PokemonNamesies pokemon, final Gender gender) {
        return TestPokemon.newPlayerPokemon(pokemon).withGender(gender);
    }

    private static TestPokemon getParentWithItem(final PokemonNamesies pokemon, final Gender gender, final ItemNamesies item) {
        TestPokemon parent = getParent(pokemon, gender);
        parent.giveItem(item);

        return parent;
    }

    private static TestPokemon getParentWithMoves(final PokemonNamesies pokemon, final Gender gender, final AttackNamesies... moves) {
        return getParent(pokemon, gender).withMoves(moves);
    }

    private static Eggy getBaby(ActivePokemon mommy, ActivePokemon daddy) {
        Eggy baby = breeding.breed(mommy, daddy);

        Assert.assertNotNull(getFailMessage(mommy, daddy, baby), baby);
        Assert.assertEquals(getFailMessage(mommy, daddy, baby), 1, baby.getLevel());

        for (int iv : baby.getIVs()) {
            Assert.assertTrue(getFailMessage(mommy, daddy, baby), iv >= 0 && iv <= Stat.MAX_IV);
        }

        final List<Move> babyMoves = baby.getActualMoves();
        Assert.assertFalse(
                getFailMessage("Invalid move list size " + babyMoves.size(), mommy, daddy, baby),
                babyMoves.isEmpty() || babyMoves.size() > Move.MAX_MOVES
        );

        for (Move move : babyMoves) {
            final PokemonInfo babyInfo = baby.getPokemonInfo();
            final AttackNamesies namesies = move.getAttack().namesies();

            if (!babyInfo.canLearnByBreeding(namesies)) {
                Integer levelLearned = babyInfo.levelLearned(namesies);
                Assert.assertTrue(
                        getFailMessage("Baby should not be able to learn the move " + namesies.getName(), mommy, daddy, baby),
                        levelLearned != null && levelLearned <= baby.getLevel()
                );
            }
        }

        return baby;
    }

    private static boolean hasEqualIVs(ActivePokemon mommy, ActivePokemon daddy, Eggy baby, Stat stat) {
        int index = stat.index();
        int babyIv = baby.getIV(index);
        return babyIv == mommy.getIV(index) || babyIv == daddy.getIV(index);
    }

    private static String getFailMessage(String message, ActivePokemon mommy, ActivePokemon daddy, Eggy baby) {
        return message + "\n" + getFailMessage(mommy, daddy, baby);
    }

    private static String getFailMessage(ActivePokemon mommy, ActivePokemon daddy, Eggy baby) {
        return getFailMessage(mommy, false) + "\n" +
                getFailMessage(daddy, false) + "\n" +
                getFailMessage(baby, true);
    }

    private static String getFailMessage(PartyPokemon activePokemon, boolean isBaby) {
        if (activePokemon == null) {
            return "null";
        }

        return String.format(
                "%s %s IVs: %s Item: %s Nature: %s Moves: %s",
                isBaby ? "Baby" : StringUtils.properCase(activePokemon.getGender().name().toLowerCase()),
                activePokemon.getActualName(),
                Arrays.toString(activePokemon.getIVs()),
                activePokemon.getActualHeldItem().getName(),
                activePokemon.getNature().getName(),
                activePokemon.getActualMoves()
                             .stream()
                             .map(move -> move.getAttack().getName())
                             .collect(Collectors.joining(", "))
        );
    }

    private enum MatchType {
        MOMMY("Never", "from the mommy "),
        DADDY("Never", "from the daddy "),
        NO_MATCH("Always", "");

        private final String frequency;
        private final String source;

        MatchType(final String frequency, final String source) {
            this.frequency = frequency;
            this.source = source;
        }
    }

    private interface InheritanceRules {
        default void updateParents(ActivePokemon mommy, ActivePokemon daddy) {}

        default int numStatsToInherit() {
            return 3;
        }

        default boolean assertTrueCondition(ActivePokemon mommy, ActivePokemon daddy, Eggy baby) {
            return true;
        }

        default void verifyEnd(boolean[][] hasParent) {
            for (int i = 0; i < hasParent.length; i++) {
                for (int j = 0; j < hasParent[i].length; j++) {
                    final MatchType matchType = MatchType.values()[i];
                    final String failMessage = matchType.frequency +
                            " inherits " + matchType.source +
                            " for stat " + Stat.getStat(j, false);

                    Assert.assertTrue(failMessage, hasParent[i][j]);
                }
            }
        }
    }
}
