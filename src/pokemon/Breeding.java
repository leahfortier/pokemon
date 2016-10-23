package pokemon;

import item.Item;
import item.hold.IncenseItem;
import item.hold.PowerItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import main.Namesies;
import battle.Attack;
import battle.Move;

class Breeding {
	public static ActivePokemon breed(ActivePokemon aPokes, ActivePokemon bPokes) {
		if (!canBreed(aPokes, bPokes)) {
			return null;
		}
		
		ActivePokemon mommy = getMommy(aPokes, bPokes);
		ActivePokemon daddy = aPokes == mommy ? bPokes : aPokes;
		ActivePokemon baby = new ActivePokemon(daddy, mommy, getBabyInfo(daddy, mommy));
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
			return aPokes.getGender().equals(Gender.FEMALE) ? aPokes : bPokes;
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
				Namesies[] evolutions = babyInfo.getEvolution().getEvolutions();
				babyInfo = PokemonInfo.getPokemonInfo(evolutions[(int)(Math.random() * evolutions.length)]);
			}
		}
		
		return babyInfo;
	}
	
	public static int[] getBabyIVs(ActivePokemon daddy, ActivePokemon mommy) {
		Item daddysItem = daddy.getActualHeldItem();
		Item mommysItem = mommy.getActualHeldItem();
		
		ArrayList<PowerItem> powerItems = new ArrayList<>();
		if (daddysItem instanceof PowerItem && daddysItem.namesies() != Namesies.MACHO_BRACE_ITEM) {
			powerItems.add((PowerItem)daddysItem);
		}
		if (mommysItem instanceof PowerItem && mommysItem.namesies() != Namesies.MACHO_BRACE_ITEM) {
			powerItems.add((PowerItem)mommysItem);
		}

		// TODO: your face
		List<Stat> remainingStats = new ArrayList<>();
		remainingStats.add(Stat.HP);
		remainingStats.add(Stat.ATTACK);
		remainingStats.add(Stat.DEFENSE);
		remainingStats.add(Stat.SP_ATTACK);
		remainingStats.add(Stat.SP_DEFENSE);
		remainingStats.add(Stat.SPEED);
		
		int remainingIVsToInherit =
				daddysItem.namesies() == Namesies.DESTINY_KNOT_ITEM ||
				mommysItem.namesies() == Namesies.DESTINY_KNOT_ITEM
						? 5
						: 3;

		int[] IVs = new int[Stat.NUM_STATS];
		Arrays.fill(IVs, -1);
		
		if (powerItems.size() > 0) {
			PowerItem randomItem = powerItems.get((int)(Math.random() * powerItems.size()));
			Stat stat = randomItem.powerStat();
			remainingStats.remove(stat);
			
			ActivePokemon parentToInheritFrom = (int)(Math.random() * 2) == 0 ? daddy : mommy;
			IVs[stat.index()] = parentToInheritFrom.getIV(stat.index());
			
			remainingIVsToInherit--;
		}
		
		while (remainingIVsToInherit --> 0) {
			Stat stat = remainingStats.get((int)(Math.random() * remainingStats.size()));
			remainingStats.remove(stat);
			
			ActivePokemon parentToInheritFrom = (int)(Math.random() * 2) == 0 ? daddy : mommy;
			IVs[stat.index()] = parentToInheritFrom.getIV(stat.index());
		}
		
		for (int i = 0; i < IVs.length; i++) {
			if (IVs[i] == -1) {
				IVs[i] = (int)(Math.random()* (ActivePokemon.MAX_IV + 1));
			}
		}
		
		return IVs;
	}
	
	public static boolean canBreed(ActivePokemon aPokes, ActivePokemon bPokes) {
		if (isDitto(aPokes) && isDitto(bPokes)) {
			return false;
		}

		// TODO: remove above line, make canBreed check if the pokes is ditto also take in another pokes
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
		return pokes.isPokemon(Namesies.DITTO_POKEMON);
	}
	
	public static Nature getBabyNature(ActivePokemon daddy, ActivePokemon mommy) {
		Item daddysItem = daddy.getActualHeldItem();
		Item mommysItem = mommy.getActualHeldItem();
		
		if (daddysItem.namesies() == Namesies.EVERSTONE_ITEM && mommysItem.namesies() == Namesies.EVERSTONE_ITEM) {
			return (int)(Math.random() * 2) == 0 ? daddy.getNature() : mommy.getNature();
		}
		else if (daddysItem.namesies() == Namesies.EVERSTONE_ITEM) {
			return daddy.getNature();
		}
		else if (mommysItem.namesies() == Namesies.EVERSTONE_ITEM) {
			return mommy.getNature();
		}
		else {
			return new Nature();
		}
	}
	
	public static List<Move> getBabyMoves(ActivePokemon daddy, ActivePokemon mommy, PokemonInfo babyInfo) {
		List<Move> daddysMoves = daddy.getActualMoves();
		List<Move> mommysMoves = mommy.getActualMoves();
		
		List<Namesies> daddysMovesNamesies = new ArrayList<>();
		List<Namesies> mommysMovesNamesies = new ArrayList<>();
		
		for (Move move : daddysMoves) {
			daddysMovesNamesies.add(move.getAttack().namesies());
		}
		for (Move move : mommysMoves) {
			mommysMovesNamesies.add(move.getAttack().namesies());
		}
		
		List<Namesies> babyMovesNamesies = new ArrayList<>();
		int curMoveIndex = 0;

		// Get moves that the pokemon learns at level 1
		// If both parents know a move the baby learns at a later level, the baby inherits that move
		Map<Integer, List<Namesies>> levelUpMoves = babyInfo.getLevelUpMoves();
		Set<Integer> levels = levelUpMoves.keySet();
		
		for (int level : levels) {
			if (level < 2) {
				babyMovesNamesies.addAll(levelUpMoves.get(level));
			}
			else {
				List<Namesies> currentLevelMoves = levelUpMoves.get(level);
				for (Namesies moveNamesies : currentLevelMoves) {
					if (daddysMovesNamesies.contains(moveNamesies) && mommysMovesNamesies.contains(moveNamesies)) {
						babyMovesNamesies.add(moveNamesies);
					}
				}
			}
		}
		
		// Egg moves
		for (Namesies moveNamesies : daddysMovesNamesies) {
			if (babyInfo.hasEggMove(moveNamesies)) {
				babyMovesNamesies.add(moveNamesies);
			}
		}
		for (Namesies moveNamesies : mommysMovesNamesies) {
			if (babyInfo.hasEggMove(moveNamesies)) {
				babyMovesNamesies.add(moveNamesies);
			}
		}
		
		// Remove duplicates
		for (int i = 0; i < babyMovesNamesies.size(); i++) {
			Namesies currentMove = babyMovesNamesies.get(i);
			while (babyMovesNamesies.lastIndexOf(currentMove) != i) {
				babyMovesNamesies.remove(babyMovesNamesies.lastIndexOf(currentMove));
			}
		}
		
		// Cycle through so baby only knows MAX_MOVES amount of moves
		while (babyMovesNamesies.size() > Move.MAX_MOVES) {
			babyMovesNamesies.remove(curMoveIndex);
			babyMovesNamesies.add(curMoveIndex, babyMovesNamesies.get(Move.MAX_MOVES - 1));
			babyMovesNamesies.remove(Move.MAX_MOVES);
			curMoveIndex = (curMoveIndex + 1) % Move.MAX_MOVES;
		}
		
		List<Move> babyMoves = new ArrayList<>();
		for (Namesies namesies : babyMovesNamesies) {
			babyMoves.add(new Move(Attack.getAttack(namesies)));
		}
		
		return babyMoves;
	}
}
