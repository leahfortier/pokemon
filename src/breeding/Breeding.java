package breeding;

import java.util.ArrayList;
import java.util.Arrays;

import item.Item;
import item.hold.PowerItem;
import main.Namesies;
import pokemon.ActivePokemon;
import pokemon.Gender;
import pokemon.Nature;
import pokemon.PokemonInfo;
import pokemon.Stat;

public class Breeding {
	
	public static ActivePokemon breed(ActivePokemon aPokes, ActivePokemon bPokes)
	{
		if (!canBreed(aPokes, bPokes))
			return null;
		
		ActivePokemon mommy = getMommy(aPokes, bPokes);
		ActivePokemon daddy = aPokes == mommy ? bPokes : aPokes;
		PokemonInfo babyInfo = getBabyInfo(daddy, mommy);
		
		ActivePokemon baby = new ActivePokemon(daddy, mommy, babyInfo);
		return baby;
	}
	
	private static ActivePokemon getMommy(ActivePokemon aPokes, ActivePokemon bPokes)
	{
		if (isDitto(aPokes))
			return bPokes;
		if (isDitto(bPokes))
			return aPokes;
		return aPokes.getGender().equals(Gender.FEMALE) ? aPokes : bPokes;
	}
	
	private static PokemonInfo getBabyInfo(ActivePokemon daddy, ActivePokemon mommy)
	{
		PokemonInfo babyInfo = mommy.getPokemonInfo().getBaseEvolution();
		if (babyInfo.isIncensePokemon())
		{
			Item daddysItem = daddy.getActualHeldItem();
			Item mommysItem = mommy.getActualHeldItem();
			
			
		}
	}
	
	public static int[] getBabyIVs(ActivePokemon daddy, ActivePokemon mommy)
	{
		Item daddysItem = daddy.getActualHeldItem();
		Item mommysItem = mommy.getActualHeldItem();
		
		ArrayList<PowerItem> powerItems = new ArrayList<>();
		if (daddysItem instanceof PowerItem && daddysItem.namesies() != Namesies.MACHO_BRACE_ITEM)
			powerItems.add((PowerItem)daddysItem);
		if (mommysItem instanceof PowerItem && mommysItem.namesies() != Namesies.MACHO_BRACE_ITEM)
			powerItems.add((PowerItem)mommysItem);
		
		ArrayList<Stat> remainingStats = new ArrayList<>();
		remainingStats.add(Stat.HP);
		remainingStats.add(Stat.ATTACK);
		remainingStats.add(Stat.DEFENSE);
		remainingStats.add(Stat.SP_ATTACK);
		remainingStats.add(Stat.SP_DEFENSE);
		remainingStats.add(Stat.SPEED);
		
		int remainingIVsToInherit = daddysItem.namesies() == Namesies.DESTINY_KNOT_ITEM || mommysItem.namesies() == Namesies.DESTINY_KNOT_ITEM ? 5 : 3;
		int[] IVs = new int[Stat.NUM_STATS];
		Arrays.fill(IVs, -1);
		
		if (powerItems.size() > 0)
		{
			PowerItem randomItem = powerItems.get((int)(Math.random() * powerItems.size()));
			Stat stat = randomItem.toIncrease();
			remainingStats.remove(stat);
			
			ActivePokemon parentToInheritFrom = (int)(Math.random() * 2) == 0 ? daddy : mommy;
			IVs[stat.index()] = parentToInheritFrom.getIV(stat.index());
			
			remainingIVsToInherit--;
		}
		
		while (remainingIVsToInherit --> 0)
		{
			Stat stat = remainingStats.get((int)(Math.random() * remainingStats.size()));
			remainingStats.remove(stat);
			
			ActivePokemon parentToInheritFrom = (int)(Math.random() * 2) == 0 ? daddy : mommy;
			IVs[stat.index()] = parentToInheritFrom.getIV(stat.index());
		}
		
		for (int i = 0; i < IVs.length; i++)
			if (IVs[i] == -1)
				IVs[i] = (int)(Math.random()* (ActivePokemon.MAX_IV + 1));
		
		return IVs;
	}
	
	public static boolean canBreed(ActivePokemon aPokes, ActivePokemon bPokes)
	{
		if (isDitto(aPokes) && isDitto(bPokes))
			return false;
		
		if (aPokes.canBreed() || !bPokes.canBreed())
			return false;
		
		if (!Gender.oppositeGenders(aPokes, bPokes) && !isDitto(aPokes) && !isDitto(bPokes))
			return false;
		
		String[] aPokesEggGroups = aPokes.getPokemonInfo().getEggGroups();
		String[] bPokesEggGroups = bPokes.getPokemonInfo().getEggGroups();
		
		for (int i = 0; i < aPokesEggGroups.length; i++)
			for (int j = 0; j < bPokesEggGroups.length; j++)
				if (aPokesEggGroups[i].equals(bPokesEggGroups[j]))
					return true;
		return false;
	}

	private static boolean isDitto(ActivePokemon pokes)
	{
		return pokes.isPokemon(Namesies.DITTO_POKEMON);
	}
	
	public static Nature getBabyNature(ActivePokemon daddy, ActivePokemon mommy)
	{
		Item daddysItem = daddy.getActualHeldItem();
		Item mommysItem = mommy.getActualHeldItem();
		
		if (daddysItem.namesies() == Namesies.EVERSTONE_ITEM && mommysItem.namesies() == Namesies.EVERSTONE_ITEM)
			return (int)(Math.random() * 2) == 0 ? daddy.getNature() : mommy.getNature();
		else if (daddysItem.namesies() == Namesies.EVERSTONE_ITEM)
			return daddy.getNature();
		else if (mommysItem.namesies() == Namesies.EVERSTONE_ITEM)
			return mommy.getNature();
		else
			return new Nature();
	}
}
