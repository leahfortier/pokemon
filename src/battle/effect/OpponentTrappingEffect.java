package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface OpponentTrappingEffect {
	boolean trapOpponent(Battle b, ActivePokemon p);
	String opponentTrappingMessage(ActivePokemon escaper, ActivePokemon trapper);
}
