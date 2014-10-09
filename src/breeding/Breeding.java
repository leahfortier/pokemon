package breeding;

import main.Namesies;
import pokemon.ActivePokemon;
import pokemon.Gender;

public class Breeding {
	
	public static ActivePokemon breed(ActivePokemon aPokes, ActivePokemon bPokes)
	{
		if (!canBreed(aPokes, bPokes))
			return null;
		
		ActivePokemon mommy = getMommy(aPokes, bPokes);
		ActivePokemon daddy = aPokes == mommy ? bPokes : aPokes;
		
		ActivePokemon baby = new ActivePokemon(daddy, mommy);
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
}
