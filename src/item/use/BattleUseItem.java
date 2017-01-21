package item.use;

import battle.Battle;
import pokemon.ActivePokemon;

public interface BattleUseItem extends UseItem {
	boolean use(ActivePokemon p, Battle b);
}
