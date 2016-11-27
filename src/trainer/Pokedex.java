package trainer;

import main.Game;
import main.Global;
import pokemon.ActivePokemon;
import pokemon.PokemonInfo;
import pokemon.PokemonNamesies;
import util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Pokedex implements Serializable {
	private static final long serialVersionUID = 1L;

	private enum PokedexStatus implements Serializable {
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
	
	private final Map<PokemonNamesies, PokedexInfo> pokedex;

	Pokedex() {
		pokedex = new EnumMap<>(PokemonNamesies.class);
		for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++) {
			pokedex.put(PokemonInfo.getPokemonInfo(i).namesies(), new PokedexInfo());
		}
	}

	public boolean isNotSeen(PokemonNamesies namesies) {
		return getStatus(namesies) == PokedexStatus.NOT_SEEN;
	}

	public boolean isCaught(PokemonNamesies namesies) {
		return getStatus(namesies) == PokedexStatus.CAUGHT;
	}
	
	private PokedexStatus getStatus(PokemonNamesies name) {
		return pokedex.get(name).status;
	}

	public void setCaught(PokemonInfo pokemonInfo) {
		setStatus(pokemonInfo, PokedexStatus.CAUGHT, null);
	}

	public void setSeen(ActivePokemon pokemon, boolean isWildBattle) {
		this.setStatus(
				pokemon.getPokemonInfo(),
				PokedexStatus.SEEN,
				isWildBattle
						? Game.getPlayer().getAreaName()
						: StringUtils.empty()
		);
	}

	private void setStatus(PokemonInfo p, PokedexStatus status, String wildLocation) {
		PokemonNamesies pokemon = p.namesies();
		PokedexInfo info = pokedex.get(pokemon); 
		info.addLocation(wildLocation);
		
		if (info.status.getWeight() >= status.getWeight()) {
			Global.error("Cannnot decrease pokedex status weight." +
					" " + pokemon + " " + status.getWeight() + " " + info.status.getWeight());
		}
		
		info.status = status;
		pokedex.put(pokemon, info);
	}

	// Num seen includes both caught and seen
	public int numSeen() {
		return (int)pokedex.entrySet().stream()
				.filter(pair -> pair.getValue().status != PokedexStatus.NOT_SEEN)
				.count();
	}
	
	public int numCaught() {
		return (int)pokedex.entrySet().stream()
				.filter(pair -> pair.getValue().status == PokedexStatus.CAUGHT)
				.count();
	}
	
	public List<String> getLocations(PokemonNamesies pokemon) {
		return pokedex.get(pokemon).getLocations();
	}
	
	private static class PokedexInfo implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private final List<String> locations;
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
		
		List<String> getLocations() {
			if (!locations.isEmpty()) {
				return locations;
			}
			
			List<String> list = new ArrayList<>();
			list.add("Area Unknown");
			
			return list;
		}
	}
}
