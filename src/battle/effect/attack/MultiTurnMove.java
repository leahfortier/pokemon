package battle.effect.attack;

import pokemon.ActivePokemon;
import battle.Battle;

public interface MultiTurnMove {
	void charge(ActivePokemon user, Battle b);
	boolean chargesFirst();
	boolean semiInvulnerability();
}
