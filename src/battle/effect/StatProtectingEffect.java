package battle.effect;

import pokemon.ActivePokemon;
import pokemon.Stat;
import battle.Battle;

public interface StatProtectingEffect {
	boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat);
	String preventionMessage(ActivePokemon p, Stat s);
}
