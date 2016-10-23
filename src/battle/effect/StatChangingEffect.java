package battle.effect;

import pokemon.ActivePokemon;
import pokemon.Stat;
import battle.Battle;

public interface StatChangingEffect {
	// stat: The current value of stat s
	// p: The Pokemon that the stat is being altered on
	// opp: The opposing Pokemon
	// s: The stat that is being altered
	// b: The current battle
	// Return: The modified value of stat, if stat was not altered, just return stat
	int modify(Integer stat, ActivePokemon p, ActivePokemon opp, Stat s, Battle b);
}
