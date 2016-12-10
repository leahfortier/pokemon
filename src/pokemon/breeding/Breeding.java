package pokemon.breeding;

import battle.attack.AttackNamesies;
import battle.attack.Move;
import item.Item;
import item.ItemNamesies;
import item.hold.IncenseItem;
import item.hold.PowerItem;
import pokemon.ActivePokemon;
import pokemon.Gender;
import pokemon.Nature;
import pokemon.PokemonInfo;
import pokemon.PokemonNamesies;
import pokemon.Stat;
import util.RandomUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Breeding {
	protected static ActivePokemon breed(ActivePokemon aPokes, ActivePokemon bPokes) {
		if (!canBreed(aPokes, bPokes)) {
			return null;
		}
		
		ActivePokemon mommy = getMommy(aPokes, bPokes);
		ActivePokemon daddy = aPokes == mommy ? bPokes : aPokes;
		ActivePokemon baby = new ActivePokemon(daddy, mommy, getBabyInfo(daddy, mommy).namesies());
		return baby;
	}
	
	private static ActivePokemon getMommy(ActivePokemon aPokes, ActivePokemon bPokes) {
		if (isDitto(aPokes)) {
			return bPokes;
		}
		else if (isDitto(bPokes)) {
			return aPokes;
		}
		else {
			return aPokes.getGender() == Gender.FEMALE ? aPokes : bPokes;
		}
	}
	
	private static PokemonInfo getBabyInfo(ActivePokemon daddy, ActivePokemon mommy) {
		PokemonInfo babyInfo = mommy.getPokemonInfo().getBaseEvolution();
		if (babyInfo.isIncenseBaby()) {
			Item daddysItem = daddy.getActualHeldItem();
			Item mommysItem = mommy.getActualHeldItem();
			
			boolean incenseItemHeld = false;
			if (daddysItem instanceof IncenseItem) {
				incenseItemHeld |= babyInfo.namesies() == ((IncenseItem)daddysItem).getBaby();
			}
			if (mommysItem instanceof IncenseItem) {
				incenseItemHeld |= babyInfo.namesies() == ((IncenseItem)mommysItem).getBaby();
			}
			
			if (!incenseItemHeld) {
				PokemonNamesies[] evolutions = babyInfo.getEvolution().getEvolutions();
				babyInfo = PokemonInfo.getPokemonInfo(RandomUtils.getRandomValue(evolutions));
			}
		}
		
		return babyInfo;
	}

	public static int[] getBabyIVs(ActivePokemon daddy, ActivePokemon mommy) {
		List<Stat> remainingStats = new ArrayList<>();
		Collections.addAll(remainingStats, Stat.STATS);

		List<Item> parentItems = new ArrayList<>();
		parentItems.add(daddy.getActualHeldItem());
		parentItems.add(mommy.getActualHeldItem());

		// Inherit 5 stats instead of 3 when a parent holds Destiny Knot
		int remainingIVsToInherit =
				parentItems.stream()
						.filter(item -> item.namesies() == ItemNamesies.DESTINY_KNOT)
						.count() > 0
						? 5
						: 3;

		int[] IVs = new int[Stat.NUM_STATS];
		Arrays.fill(IVs, -1);

		for (Item item : parentItems) {
			if (item instanceof PowerItem) {
				Stat stat = ((PowerItem)item).powerStat();
				IVs[stat.index()] = getRandomParent(daddy, mommy).getIV(stat.index());

				remainingStats.remove(stat);
				remainingIVsToInherit--;
			}
		}
		
		while (remainingIVsToInherit --> 0) {
			Stat stat = RandomUtils.getRandomValue(remainingStats);
			remainingStats.remove(stat);

			IVs[stat.index()] = getRandomParent(daddy, mommy).getIV(stat.index());
		}
		
		for (int i = 0; i < IVs.length; i++) {
			if (IVs[i] == -1) {
				IVs[i] = Stat.getRandomIv();
			}
		}
		
		return IVs;
	}

	protected static boolean canBreed(ActivePokemon aPokes, ActivePokemon bPokes) {
		if (isDitto(aPokes) && isDitto(bPokes)) {
			return false;
		}

		if (isDittoAndManaphy(aPokes, bPokes)) {
			return true;
		}

		if (!aPokes.canBreed() || !bPokes.canBreed()) {
			return false;
		}
		
		if (!Gender.oppositeGenders(aPokes, bPokes) && !isDitto(aPokes) && !isDitto(bPokes)) {
			return false;
		}
		
		String[] aPokesEggGroups = aPokes.getPokemonInfo().getEggGroups();
		String[] bPokesEggGroups = bPokes.getPokemonInfo().getEggGroups();

		for (String aPokesEggGroup : aPokesEggGroups) {
			for (String bPokesEggGroup : bPokesEggGroups) {
				if (aPokesEggGroup.equals(bPokesEggGroup)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean isDitto(ActivePokemon pokes) {
		return pokes.isPokemon(PokemonNamesies.DITTO);
	}

	private static boolean isDittoAndManaphy(ActivePokemon aPokes, ActivePokemon bPokes) {
		if (isDitto(aPokes)) {
			return bPokes.isPokemon(PokemonNamesies.MANAPHY);
		}

		if (isDitto(bPokes)) {
			return aPokes.isPokemon(PokemonNamesies.MANAPHY);
		}

		return false;
	}

	private static ActivePokemon getRandomParent(final ActivePokemon daddy, final ActivePokemon mommy) {
		return RandomUtils.getRandomValue(new ActivePokemon[] { daddy, mommy });
	}

	public static Nature getBabyNature(ActivePokemon daddy, ActivePokemon mommy) {
		Item daddysItem = daddy.getActualHeldItem();
		Item mommysItem = mommy.getActualHeldItem();
		
		if (daddysItem.namesies() == ItemNamesies.EVERSTONE && mommysItem.namesies() == ItemNamesies.EVERSTONE) {
			return getRandomParent(daddy, mommy).getNature();
		}
		else if (daddysItem.namesies() == ItemNamesies.EVERSTONE) {
			return daddy.getNature();
		}
		else if (mommysItem.namesies() == ItemNamesies.EVERSTONE) {
			return mommy.getNature();
		}
		else {
			return new Nature();
		}
	}
	
	public static List<Move> getBabyMoves(ActivePokemon daddy, ActivePokemon mommy, PokemonNamesies babyNamesies) {

		PokemonInfo babyInfo = PokemonInfo.getPokemonInfo(babyNamesies);
		List<AttackNamesies> babyMovesNamesies = new ArrayList<>();

		// Get moves that the pokemon learns at level 1
		babyMovesNamesies.addAll(babyInfo.getMoves(0));
		babyMovesNamesies.addAll(babyInfo.getMoves(1));

		List<Move> parentMoves = new ArrayList<>();
		parentMoves.addAll(daddy.getActualMoves());
		parentMoves.addAll(mommy.getActualMoves());

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
		final int numMoves = Math.min(babyMovesNamesies.size(), Move.MAX_MOVES);
		final int startingIndex = babyMovesNamesies.size() - numMoves;

		for (int i = 0; i < numMoves; i++) {
			final AttackNamesies namesies = babyMovesNamesies.get(startingIndex + i);
			babyMoves.add(new Move(namesies.getAttack()));
		}
		
		return babyMoves;
	}
}
