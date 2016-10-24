package test;

import battle.Attack;
import battle.Move;
import namesies.Namesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.ActivePokemon;
import pokemon.Breeding;
import pokemon.Gender;
import pokemon.PokemonInfo;
import pokemon.Stat;
import util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BreedingTest extends Breeding {

    @Test
    public void testCanBreed() {
        ActivePokemon maleRapidash = getParent(Namesies.RAPIDASH_POKEMON, Gender.MALE);
        ActivePokemon femaleRapidash = getParent(Namesies.RAPIDASH_POKEMON, Gender.FEMALE);
        ActivePokemon femaleNinetales = getParent(Namesies.NINETALES_POKEMON, Gender.FEMALE);
        ActivePokemon ditto = getParent(Namesies.DITTO_POKEMON, Gender.GENDERLESS);
        ActivePokemon magnemite = getParent(Namesies.MAGNEMITE_POKEMON, Gender.GENDERLESS);

        Assert.assertTrue(Breeding.canBreed(maleRapidash, femaleRapidash)); // Same species, opposite gender
        Assert.assertTrue(Breeding.canBreed(maleRapidash, femaleNinetales)); // Same egg group, opposite gender
        Assert.assertTrue(Breeding.canBreed(maleRapidash, ditto)); // Male and ditto
        Assert.assertTrue(Breeding.canBreed(femaleRapidash, ditto)); // Female and ditto
        Assert.assertTrue(Breeding.canBreed(magnemite, ditto)); // Genderless and ditto
    }

    @Test
    public void testCannotBreed() {
        ActivePokemon maleRapidash = getParent(Namesies.RAPIDASH_POKEMON, Gender.MALE);
        ActivePokemon femaleDragonair = getParent(Namesies.DRAGONAIR_POKEMON, Gender.FEMALE);
        ActivePokemon ditto = getParent(Namesies.DITTO_POKEMON, Gender.GENDERLESS);
        ActivePokemon magnemite = getParent(Namesies.MAGNEMITE_POKEMON, Gender.GENDERLESS);
        ActivePokemon mew = getParent(Namesies.MEW_POKEMON, Gender.GENDERLESS);
        ActivePokemon togepi = getParent(Namesies.TOGEPI_POKEMON, Gender.MALE);
        ActivePokemon togetic = getParent(Namesies.TOGETIC_POKEMON, Gender.FEMALE);
        ActivePokemon exeggcute = getParent(Namesies.EXEGGCUTE_POKEMON, Gender.MALE);
        ActivePokemon eggy = new ActivePokemon(PokemonInfo.getPokemonInfo(Namesies.EXEGGCUTE_POKEMON));

        Assert.assertFalse(Breeding.canBreed(maleRapidash, maleRapidash)); // Same species, same gender
        Assert.assertFalse(Breeding.canBreed(magnemite, magnemite)); // Same species, both genderless
        Assert.assertFalse(Breeding.canBreed(ditto, ditto)); // Two dittos
        Assert.assertFalse(Breeding.canBreed(mew, ditto)); // Legendary and ditto
        Assert.assertFalse(Breeding.canBreed(maleRapidash, femaleDragonair)); // Different egg group, opposite gender
        Assert.assertFalse(Breeding.canBreed(togepi, togetic)); // Same egg group and baby pokemon
        Assert.assertFalse(Breeding.canBreed(togepi, ditto)); // Baby pokemon and ditto
        Assert.assertFalse(Breeding.canBreed(ditto, eggy)); // Egg and ditto
        Assert.assertFalse(Breeding.canBreed(exeggcute, eggy)); // Same species and egg
    }

    @Test
    public void testBaseEvolution() {
        ActivePokemon mommy = getParent(Namesies.RAPIDASH_POKEMON, Gender.FEMALE);
        ActivePokemon daddy = getParent(Namesies.RAPIDASH_POKEMON, Gender.MALE);
        ActivePokemon baby = getBaby(mommy, daddy);

        // Mommy and daddy Rapidash -> baby Ponyta
        Assert.assertTrue(getFailMessage(mommy, daddy, baby),
                baby.isPokemon(Namesies.PONYTA_POKEMON));

        mommy = getParent(Namesies.NINETALES_POKEMON, Gender.FEMALE);
        baby = getBaby(mommy, daddy);

        // Mommy Ninetales and daddy Rapidash -> baby Vulpix
        Assert.assertTrue(getFailMessage(mommy, daddy, baby),
                baby.isPokemon(Namesies.VULPIX_POKEMON));

        mommy = getParent(Namesies.MANAPHY_POKEMON, Gender.GENDERLESS);
        daddy = getParent(Namesies.DITTO_POKEMON, Gender.GENDERLESS);
        baby = getBaby(mommy, daddy);

        // Manaphy + Ditto -> Phione
        Assert.assertTrue(getFailMessage(mommy, daddy, baby),
                baby.isPokemon(Namesies.PHIONE_POKEMON));
    }

    // TODO: Add more cases
    @Test
    public void testEggMoves() {
        ActivePokemon mommy = getParentWithMoves(Namesies.RAPIDASH_POKEMON, Gender.FEMALE, Namesies.MORNING_SUN_ATTACK);
        ActivePokemon daddy = getParentWithMoves(Namesies.NINETALES_POKEMON, Gender.MALE,
                Namesies.HYPNOSIS_ATTACK,
                Namesies.SOLAR_BEAM_ATTACK,
                Namesies.FLAMETHROWER_ATTACK);
        ActivePokemon baby = getBaby(mommy, daddy);

        Assert.assertTrue(getFailMessage(mommy, daddy, baby),
                baby.hasActualMove(Namesies.MORNING_SUN_ATTACK) &&
                        baby.hasActualMove(Namesies.HYPNOSIS_ATTACK) &&
                        baby.hasActualMove(Namesies.SOLAR_BEAM_ATTACK) &&
                        baby.hasActualMove(Namesies.FLAMETHROWER_ATTACK)
        );
    }

    @Test
    public void testIncense() {
        ActivePokemon mommy = getParentWithItem(Namesies.WOBBUFFET_POKEMON, Gender.FEMALE, Namesies.LAX_INCENSE_ITEM);
        ActivePokemon daddy = getParent(Namesies.WOBBUFFET_POKEMON, Gender.MALE);
        ActivePokemon baby = getBaby(mommy, daddy);

        // Wobby mom with incense + daddy wobby = wynaut baby
        Assert.assertTrue(getFailMessage(mommy, daddy, baby),
                baby.isPokemon(Namesies.WYNAUT_POKEMON));

        mommy.removeItem();
        baby = getBaby(mommy, daddy);

        // Without holding the incense should be a wobby baby
        Assert.assertTrue(getFailMessage(mommy, daddy, baby),
                baby.isPokemon(Namesies.WOBBUFFET_POKEMON));

        mommy.giveItem(Namesies.SEA_INCENSE_ITEM);
        daddy.giveItem(Namesies.ROSE_INCENSE_ITEM);
        baby = getBaby(mommy, daddy);

        // Holding incorrect incense should be a wobby baby
        Assert.assertTrue(getFailMessage(mommy, daddy, baby),
                baby.isPokemon(Namesies.WOBBUFFET_POKEMON));

        mommy = getParentWithItem(Namesies.DITTO_POKEMON, Gender.GENDERLESS, Namesies.LAX_INCENSE_ITEM);
        baby = getBaby(mommy, daddy);

        // Ditto mom with incense + daddy wobby = wynaut baby
        Assert.assertTrue(getFailMessage(mommy, daddy, baby),
                baby.isPokemon(Namesies.WYNAUT_POKEMON));
    }

    @Test
    public void testEverstone() {
        final ActivePokemon mommy = getParent(Namesies.RAPIDASH_POKEMON, Gender.FEMALE);
        final ActivePokemon daddy = getParentWithItem(Namesies.RAPIDASH_POKEMON, Gender.MALE, Namesies.EVERSTONE_ITEM);
        final ActivePokemon baby = getBaby(mommy, daddy);

        Assert.assertTrue(getFailMessage(mommy, daddy, baby),
                baby.getNature().equals(daddy.getNature()));
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
                mommy.giveItem(Namesies.DESTINY_KNOT_ITEM);
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
                mommy.giveItem(Namesies.POWER_LENS_ITEM);
                daddy.giveItem(Namesies.POWER_ANKLET_ITEM);
            }

            @Override
            public boolean assertTrueCondition(ActivePokemon mommy, ActivePokemon daddy, ActivePokemon baby) {
                // Power lens passes down the Special attack stat
                // Power anklet passes down the Speed stat
                return hasEqualIVs(mommy, daddy, baby, Stat.SP_ATTACK) && hasEqualIVs(mommy, daddy, baby, Stat.SPEED);
            }

            @Override
            public void verifyEnd(boolean[][] hasParent) {
                Assert.assertTrue(!hasParent[MatchType.NO_MATCH.ordinal()][Stat.SP_ATTACK.index()]);
                Assert.assertTrue(!hasParent[MatchType.NO_MATCH.ordinal()][Stat.SPEED.index()]);
            }
        });
    }

    private static void testIvInheritance(InheritanceRules rules) {

        final int numStatsToInherit = rules.numStatsToInherit();

        boolean hasExactInheritance = false;
        boolean[][] hasParent = new boolean[MatchType.values().length][Stat.NUM_STATS];

        for (int i = 0; i < 1000; i++) {
            ActivePokemon mommy = getParent(Namesies.RAPIDASH_POKEMON, Gender.FEMALE);
            ActivePokemon daddy = getParent(Namesies.RAPIDASH_POKEMON, Gender.MALE);
            rules.updateParents(mommy, daddy);

            ActivePokemon baby = getBaby(mommy, daddy);

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
                }
                else if (babyIVs[j] == daddyIVs[j]) {
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

            Assert.assertTrue(getFailMessage("Num stats to inherit: " + numStatsToInherit, mommy, daddy, baby),
                    numMatches >= numStatsToInherit);

            Assert.assertTrue(getFailMessage(mommy, daddy, baby),
                    rules.assertTrueCondition(mommy, daddy, baby));
        }

        Assert.assertTrue("Does not have exactly " + numStatsToInherit + " IV matches.", hasExactInheritance);
        rules.verifyEnd(hasParent);
    }

    private interface InheritanceRules {
        default void updateParents(ActivePokemon mommy, ActivePokemon daddy) {}

        default int numStatsToInherit() {
            return 3;
        }

        default boolean assertTrueCondition(ActivePokemon mommy, ActivePokemon daddy, ActivePokemon baby) {
            return true;
        }

        default void verifyEnd(boolean[][] hasParent) {
            for (int i = 0; i < hasParent.length; i++) {
                for (int j = 0; j < hasParent[i].length; j++) {
                    final MatchType matchType = MatchType.values()[i];
                    final String failMessage =
                            matchType.frequency +
                                    " inherits " + matchType.source +
                                    " for stat " + Stat.getStat(j, false);

                    Assert.assertTrue(failMessage, hasParent[i][j]);
                }
            }
        }
    }

    private static ActivePokemon getParent(final Namesies pokemon, final Gender gender) {
        ActivePokemon parent = new ActivePokemon(PokemonInfo.getPokemonInfo(pokemon), 100, false, false);
        parent.setGender(gender);

        return parent;
    }

    private static ActivePokemon getParentWithItem(final Namesies pokemon, final Gender gender, final Namesies item) {
        ActivePokemon parent = getParent(pokemon, gender);
        parent.giveItem(item);

        return parent;
    }

    private static ActivePokemon getParentWithMoves(final Namesies pokemon, final Gender gender, final Namesies... moves) {
        ActivePokemon parent = getParent(pokemon, gender);
        parent.setMoves(
                Arrays.stream(moves)
                .map(move -> new Move(Attack.getAttack(move)))
                .collect(Collectors.toList())
        );

        return parent;
    }

    private static ActivePokemon getBaby(ActivePokemon mommy, ActivePokemon daddy) {
        ActivePokemon baby = Breeding.breed(mommy, daddy);

        Assert.assertFalse(getFailMessage(mommy, daddy, baby),
                baby == null);

        Assert.assertTrue(getFailMessage(mommy, daddy, baby),
                baby.getLevel() == 1);

        for (int iv : baby.getIVs()) {
            Assert.assertTrue(getFailMessage(mommy, daddy, baby),
                    iv >= 0 && iv <= Stat.MAX_IV);
        }

        final List<Move> babyMoves = baby.getActualMoves();
        Assert.assertFalse(getFailMessage("Invalid move list size " + babyMoves.size(), mommy, daddy, baby),
                babyMoves.isEmpty() || babyMoves.size() > Move.MAX_MOVES);

        for (Move move : babyMoves) {
            final PokemonInfo babyInfo = baby.getPokemonInfo();
            final Namesies namesies = move.getAttack().namesies();

            if (!babyInfo.canLearnByBreeding(namesies)) {
                int levelLearned = babyInfo.levelLearned(namesies);
                Assert.assertTrue(getFailMessage("Baby should not be able to learn the move " + namesies.getName(), mommy, daddy, baby),
                        levelLearned != -1 && levelLearned <= baby.getLevel());
            }
        }

        return baby;
    }

    private static boolean hasEqualIVs(ActivePokemon mommy, ActivePokemon daddy, ActivePokemon baby, Stat stat) {
        return baby.getIV(stat.index()) == mommy.getIV(stat.index()) ||
                baby.getIV(stat.index()) == daddy.getIV(stat.index());
    }

    private static String getFailMessage(String message, ActivePokemon mommy, ActivePokemon daddy, ActivePokemon baby) {
        return message + "\n" + getFailMessage(mommy, daddy, baby);
    }

    private static String getFailMessage(ActivePokemon mommy, ActivePokemon daddy, ActivePokemon baby) {
        return getFailMessage(mommy, false) + "\n" +
                getFailMessage(daddy, false) + "\n" +
                getFailMessage(baby, true);
    }

    private static String getFailMessage(ActivePokemon activePokemon, boolean isBaby) {
        if (activePokemon == null) {
            return "null";
        }

        return String.format("%s %s IVs: %s Item: %s Nature: %s Moves: %s",
                isBaby ? "Baby" : StringUtils.firstCaps(activePokemon.getGender().name()),
                activePokemon.getName(),
                Arrays.toString(activePokemon.getIVs()),
                activePokemon.getActualHeldItem().getName(),
                activePokemon.getNature().getName(),
                activePokemon.getActualMoves().stream()
                        .map(move -> move.getAttack().getName())
                        .collect(Collectors.joining(", ")
                )
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
}
