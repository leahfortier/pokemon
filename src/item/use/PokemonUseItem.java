package item.use;

import pokemon.ActivePokemon;

public interface PokemonUseItem extends UseItem
{
	public boolean use(ActivePokemon p);
}
