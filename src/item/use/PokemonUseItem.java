package item.use;

import pokemon.ActivePokemon;
import trainer.CharacterData;

public interface PokemonUseItem extends UseItem {
	boolean use(CharacterData player, ActivePokemon p);
}
