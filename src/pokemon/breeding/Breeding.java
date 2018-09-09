package pokemon.breeding;

import battle.ActivePokemon;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import item.ItemNamesies;
import item.hold.HoldItem;
import item.hold.IncenseItem;
import item.hold.PowerItem;
import pokemon.Stat;
import pokemon.active.Gender;
import pokemon.active.IndividualValues;
import pokemon.active.MoveList;
import pokemon.active.Nature;
import pokemon.species.PokemonInfo;
import pokemon.species.PokemonNamesies;
import util.RandomUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Breeding {
    private static Breeding instance;

    public static Breeding instance() {
        if (instance == null) {
            instance = new Breeding();
        }
        return instance;
    }

    private Breeding() {}

    public Eggy breed(ActivePokemon aPokes, ActivePokemon bPokes) {
        if (!canBreed(aPokes, bPokes)) {
            return null;
        }

        ActivePokemon mommy = getMommy(aPokes, bPokes);
        ActivePokemon daddy = aPokes == mommy ? bPokes : aPokes;
        Eggy baby = new Eggy(daddy, mommy, getBabyInfo(daddy, mommy).namesies());
        return baby;
    }

    private ActivePokemon getMommy(ActivePokemon aPokes, ActivePokemon bPokes) {
        if (isDitto(aPokes)) {
            return bPokes;
        } else if (isDitto(bPokes)) {
            return aPokes;
        } else {
            return aPokes.getGender() == Gender.FEMALE ? aPokes : bPokes;
        }
    }

    private PokemonInfo getBabyInfo(ActivePokemon daddy, ActivePokemon mommy) {
        PokemonInfo babyInfo = mommy.getPokemonInfo().getBaseEvolution().getInfo();
        if (babyInfo.isIncenseBaby()) {
            boolean incenseItemHeld = false;
            HoldItem[] holdItems = { daddy.getActualHeldItem(), mommy.getActualHeldItem() };
            for (HoldItem holdItem : holdItems) {
                if (holdItem instanceof IncenseItem) {
                    IncenseItem incenseItem = (IncenseItem)holdItem;
                    if (babyInfo.namesies() == incenseItem.getBaby()) {
                        incenseItemHeld = true;
                    }
                }
            }

            if (!incenseItemHeld) {
                PokemonNamesies[] evolutions = babyInfo.getEvolution().getEvolutions();
                babyInfo = RandomUtils.getRandomValue(evolutions).getInfo();
            }
        }

        return babyInfo;
    }

    public IndividualValues getBabyIVs(ActivePokemon daddy, ActivePokemon mommy) {
        List<Stat> remainingStats = new ArrayList<>(Stat.STATS);

        List<HoldItem> parentItems = Arrays.asList(
                daddy.getActualHeldItem(),
                mommy.getActualHeldItem()
        );

        // Inherit 5 stats instead of 3 when a parent holds Destiny Knot
        int remainingIVsToInherit =
                parentItems.stream().anyMatch(item -> item.namesies() == ItemNamesies.DESTINY_KNOT)
                ? 5
                : 3;

        int[] IVs = new int[Stat.NUM_STATS];
        Arrays.fill(IVs, -1);

        for (HoldItem item : parentItems) {
            if (item instanceof PowerItem) {
                Stat stat = ((PowerItem)item).powerStat();
                IVs[stat.index()] = getRandomParent(daddy, mommy).getIVs().get(stat);

                remainingStats.remove(stat);
                remainingIVsToInherit--;
            }
        }

        while (remainingIVsToInherit-- > 0) {
            Stat stat = RandomUtils.getRandomValue(remainingStats);
            remainingStats.remove(stat);

            IVs[stat.index()] = getRandomParent(daddy, mommy).getIVs().get(stat);
        }

        for (int i = 0; i < IVs.length; i++) {
            if (IVs[i] == -1) {
                IVs[i] = IndividualValues.getRandomIv();
            }
        }

        return new IndividualValues(IVs);
    }

    public boolean canBreed(ActivePokemon aPokes, ActivePokemon bPokes) {

        // If either pokemon cannot breed, then they can't breed together
        if (!aPokes.canBreed() || !bPokes.canBreed()) {
            return false;
        }

        // Ditto can breed with every breedable Pokemon except itself
        if (isDitto(aPokes) || isDitto(bPokes)) {
            return !(isDitto(aPokes) && isDitto(bPokes));
        }

        // If neither Pokemon is a ditto and the Pokemon do not have opposite genders, they can't breed together
        if (!Gender.oppositeGenders(aPokes, bPokes)) {
            return false;
        }

        EggGroup[] aPokesEggGroups = aPokes.getPokemonInfo().getEggGroups();
        EggGroup[] bPokesEggGroups = bPokes.getPokemonInfo().getEggGroups();

        for (EggGroup aPokesEggGroup : aPokesEggGroups) {
            for (EggGroup bPokesEggGroup : bPokesEggGroups) {
                if (aPokesEggGroup == bPokesEggGroup && aPokesEggGroup != EggGroup.NONE) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isDitto(ActivePokemon pokes) {
        return pokes.isPokemon(PokemonNamesies.DITTO);
    }

    private ActivePokemon getRandomParent(final ActivePokemon daddy, final ActivePokemon mommy) {
        return RandomUtils.getRandomValue(new ActivePokemon[] { daddy, mommy });
    }

    public Nature getBabyNature(ActivePokemon daddy, ActivePokemon mommy) {
        HoldItem daddysItem = daddy.getActualHeldItem();
        HoldItem mommysItem = mommy.getActualHeldItem();

        if (daddysItem.namesies() == ItemNamesies.EVERSTONE && mommysItem.namesies() == ItemNamesies.EVERSTONE) {
            return getRandomParent(daddy, mommy).getNature();
        } else if (daddysItem.namesies() == ItemNamesies.EVERSTONE) {
            return daddy.getNature();
        } else if (mommysItem.namesies() == ItemNamesies.EVERSTONE) {
            return mommy.getNature();
        } else {
            return new Nature();
        }
    }

    public List<Move> getBabyMoves(ActivePokemon daddy, ActivePokemon mommy, PokemonNamesies babyNamesies) {
        PokemonInfo babyInfo = babyNamesies.getInfo();
        List<AttackNamesies> babyMovesNamesies = new ArrayList<>();

        // Get moves that the pokemon learns at level 1
        babyMovesNamesies.addAll(babyInfo.getMoves(0));
        babyMovesNamesies.addAll(babyInfo.getMoves(1));

        List<Move> parentMoves = new ArrayList<>();
        parentMoves.addAll(daddy.getActualMoves().stream().collect(Collectors.toList()));
        parentMoves.addAll(mommy.getActualMoves().stream().collect(Collectors.toList()));

        // Egg moves
        for (final Move parentMove : parentMoves) {
            final AttackNamesies attackNamesies = parentMove.getAttack().namesies();
            if (babyInfo.canLearnByBreeding(attackNamesies) &&
                    !babyMovesNamesies.contains(attackNamesies)) {
                babyMovesNamesies.add(attackNamesies);
            }
        }

        // Add the last four moves on the list
        List<Move> babyMoves = new ArrayList<>();
        final int numMoves = Math.min(babyMovesNamesies.size(), MoveList.MAX_MOVES);
        final int startingIndex = babyMovesNamesies.size() - numMoves;

        for (int i = 0; i < numMoves; i++) {
            final AttackNamesies namesies = babyMovesNamesies.get(startingIndex + i);
            babyMoves.add(new Move(namesies));
        }

        return babyMoves;
    }
}
