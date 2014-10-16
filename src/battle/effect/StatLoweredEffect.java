package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface StatLoweredEffect
{
	public void takeItToTheNextLevel(Battle b, ActivePokemon caster, ActivePokemon victim);
}
