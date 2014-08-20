package battle.effect;

import battle.Battle;
import pokemon.ActivePokemon;

public interface SelfHealingMove 
{
	public void heal(ActivePokemon user, ActivePokemon victim, Battle b);
}
