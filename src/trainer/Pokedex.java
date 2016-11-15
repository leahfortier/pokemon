package trainer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pokemon.PokemonNamesies;
import pokemon.PokemonInfo;
import util.StringUtils;

public class Pokedex implements Serializable {
	private static final long serialVersionUID = 1L;

	// TODO: Move to separate file or actually make this private
	public enum PokedexStatus implements Serializable {
		NOT_SEEN(0),
		SEEN(1),
		CAUGHT(2);
		
		private int weight;
		
		PokedexStatus(int weight) {
			this.weight = weight;
		}
		
		private int getWeight() {
			return weight;
		}
	}
	
	private Map<PokemonNamesies, PokedexInfo> pokedex;
	private int numSeen;
	private int numCaught;

	Pokedex() {
		pokedex = new HashMap<>();
		for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++) {
			pokedex.put(PokemonInfo.getPokemonInfo(i).namesies(), new PokedexInfo());
		}
		
		numSeen = 0;
		numCaught = 0;
	}
	
	public PokedexStatus getStatus(PokemonNamesies name) {
		return pokedex.get(name).status;
	}
	
	public boolean setStatus(PokemonInfo p, PokedexStatus status) {
		return setStatus(p, status, null);
	}
	
	public boolean setStatus(PokemonInfo p, PokedexStatus status, String wildLocation) {
		PokemonNamesies pokemon = p.namesies();
		PokedexInfo info = pokedex.get(pokemon); 
		info.addLocation(wildLocation);
		
		if (info.status.getWeight() >= status.getWeight()) {
			return false;
		}
		
		if (status == PokedexStatus.SEEN) {
			numSeen++;
		}
		else { // Caught
			numCaught++;
			
			if (info.status == PokedexStatus.NOT_SEEN) {
				numSeen++;
			}
		}
		
		info.status = status;
	
		pokedex.put(pokemon, info);
		return true;
	}
	
	public boolean seen(PokemonNamesies name) {
		return pokedex.containsKey(name) && pokedex.get(name).status == PokedexStatus.SEEN;
	}
	
	public boolean caught(PokemonNamesies name) {
		return pokedex.containsKey(name) && pokedex.get(name).status == PokedexStatus.CAUGHT;
	}
	
	public int numSeen() {
		return numSeen;
	}
	
	public int numCaught() {
		return numCaught;
	}
	
	public List<String> getLocations(PokemonNamesies pokemon) {
		return pokedex.get(pokemon).getLocations();
	}
	
	private static class PokedexInfo implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private List<String> locations;
		private PokedexStatus status;
		
		PokedexInfo() {
			locations = new ArrayList<>();
			status = PokedexStatus.NOT_SEEN;
		}
		
		void addLocation(String location) {
			if (StringUtils.isNullOrEmpty(location) || locations.contains(location)) {
				return;
			}
			
			locations.add(location);
		}
		
		public List<String> getLocations() {
			if (!locations.isEmpty()) {
				return locations;
			}
			
			List<String> list = new ArrayList<>();
			list.add("Area Unknown");
			
			return list;
		}
	}
}
