package battle.effect;

import pokemon.ActivePokemon;
import battle.Move;

public interface AttackSelectionEffect
{
	public boolean usable(ActivePokemon p, Move m);
	public String getUnusableMessage(ActivePokemon p);
}