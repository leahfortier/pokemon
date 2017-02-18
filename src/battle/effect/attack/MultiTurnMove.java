package battle.effect.attack;

import battle.Battle;
import pokemon.ActivePokemon;

public interface MultiTurnMove {
	void charge(ActivePokemon user, Battle b);
	boolean chargesFirst();
	boolean semiInvulnerability();
}
