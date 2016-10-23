package item.use;

import pokemon.ActivePokemon;
import battle.Battle;

public interface BattleUseItem extends UseItem {
	boolean use(ActivePokemon p, Battle b);
}
