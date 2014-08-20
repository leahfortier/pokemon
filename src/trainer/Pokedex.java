package trainer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pokemon.ActivePokemon;
import pokemon.PokemonInfo;

public class Pokedex implements Serializable
{
	private static final long serialVersionUID = 1L;

	public enum PokedexStatus implements Serializable
	{
		NOT_SEEN(0), SEEN(1), CAUGHT(2);
		
		int weight;
		
		private PokedexStatus(int weight)
		{
			this.weight = weight;
		}
		
		int getWeight()
		{
			return weight;
		}
	}
	
	private HashMap<String, PokedexInfo> pokedex;
	private int numSeen;
	private int numCaught;

	public Pokedex()
	{
		pokedex = new HashMap<>();
		for (String s : PokemonInfo.getPokemonList())
		{
			pokedex.put(s, new PokedexInfo());
		}
		
		numSeen = 0;
		numCaught = 0;
	}
	
	public PokedexStatus getStatus(String s)
	{
		return pokedex.get(s).status;
	}
	
	public boolean setStatus(ActivePokemon p, PokedexStatus status)
	{
		return setStatus(p, status, null);
	}
	
	public boolean setStatus(ActivePokemon p, PokedexStatus status, String wildLocation)
	{
		String pokemon = p.getPokemonInfo().getName();
		System.out.println(pokemon);
		PokedexInfo info = pokedex.get(pokemon); 
		info.addLocation(wildLocation);
		
		if (info.status.getWeight() >= status.getWeight()) return false;
		
		if (status == PokedexStatus.SEEN)
		{
			numSeen++;
		}
		else // Caught
		{
			numCaught++;
			if (info.status == PokedexStatus.NOT_SEEN)
			{
				numSeen++;
			}
		}
		info.status = status;
	
		pokedex.put(pokemon, info);
		return true;
	}
	
	public boolean seen(String name)
	{
		return pokedex.containsKey(name) && pokedex.get(name).status == PokedexStatus.SEEN;
	}
	
	public boolean caught(String name)
	{
		return pokedex.containsKey(name) && pokedex.get(name).status == PokedexStatus.CAUGHT;
	}
	
	public int numSeen()
	{
		return numSeen;
	}
	
	public int numCaught()
	{
		return numCaught;
	}
	
	public List<String> getLocations(String pokemon)
	{
		return pokedex.get(pokemon).getLocations();
	}
	
	private static class PokedexInfo implements Serializable
	{
		private static final long serialVersionUID = 1L;
		
		private List<String> locations;
		private PokedexStatus status;
		
		public PokedexInfo()
		{
			locations = new ArrayList<>();
			status = PokedexStatus.NOT_SEEN;
		}
		
		public void addLocation(String location)
		{
			if (location == null || location.equals("") || locations.contains(location)) return;
			locations.add(location);
		}
		
		public List<String> getLocations()
		{
			if (locations.size() > 0) return locations;
			
			List<String> list = new ArrayList<>();
			list.add("Area Unknown");
			return list;
		}
	}
}
