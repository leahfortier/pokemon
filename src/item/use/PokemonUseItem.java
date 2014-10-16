package item.use;

import pokemon.ActivePokemon;
import trainer.CharacterData;

public interface PokemonUseItem extends UseItem
{
	public boolean use(CharacterData player, ActivePokemon p);
}
