package battle.effect;

import pokemon.ActivePokemon;

public interface PriorityChangeEffect
{
	public int changePriority(ActivePokemon user, Integer priority);
}
