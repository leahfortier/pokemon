package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface EndTurnEffect {
	void applyEndTurn(ActivePokemon victim, Battle b);
}
