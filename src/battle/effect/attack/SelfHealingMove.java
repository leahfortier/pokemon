package battle.effect.attack;

import battle.Battle;
import pokemon.ActivePokemon;

public interface SelfHealingMove {
	void heal(ActivePokemon user, ActivePokemon victim, Battle b);
}
