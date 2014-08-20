package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface BeforeTurnEffect 
{
	public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b);
}
