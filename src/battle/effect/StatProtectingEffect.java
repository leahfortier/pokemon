package battle.effect;

import pokemon.ActivePokemon;
import pokemon.Stat;
import battle.Battle;

public interface StatProtectingEffect 
{
	public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat);
	public String preventionMessage(ActivePokemon p);
}
