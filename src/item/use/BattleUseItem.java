package item.use;

import pokemon.ActivePokemon;
import battle.Battle;

public interface BattleUseItem extends UseItem
{
	public boolean use(ActivePokemon p, Battle b);
}
