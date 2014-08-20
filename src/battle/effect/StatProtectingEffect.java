package battle.effect;

import pokemon.ActivePokemon;
import pokemon.Stat;

public interface StatProtectingEffect 
{
	public boolean prevent(ActivePokemon caster, Stat stat);
	public String preventionMessage(ActivePokemon p);
}
