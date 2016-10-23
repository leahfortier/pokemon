package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface PriorityChangeEffect {
	int changePriority(Battle b, ActivePokemon user, Integer priority);
}
