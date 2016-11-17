package item.use;

import pokemon.ActivePokemon;

public interface PokemonUseItem extends UseItem {
	boolean use(ActivePokemon p);
}
