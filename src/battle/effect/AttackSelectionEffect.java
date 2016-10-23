package battle.effect;

import pokemon.ActivePokemon;
import battle.Move;

public interface AttackSelectionEffect {
	boolean usable(ActivePokemon p, Move m);
	String getUnusableMessage(ActivePokemon p);
}
