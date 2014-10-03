package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface OpponentBeforeTurnEffect 
{
	public boolean opposingCanAttack(ActivePokemon p, ActivePokemon opp, Battle b);
}
