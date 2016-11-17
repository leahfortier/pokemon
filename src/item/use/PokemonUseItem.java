package item.use;

import pokemon.ActivePokemon;
import trainer.CharacterData;

public interface PokemonUseItem extends UseItem {
	// TODO: Remove character data as a parameter
	boolean use(CharacterData player, ActivePokemon p);
}
