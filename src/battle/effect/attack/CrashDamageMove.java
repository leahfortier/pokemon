package battle.effect.attack;

import pokemon.ActivePokemon;
import battle.Battle;

public interface CrashDamageMove {
	void crash(Battle b, ActivePokemon user);
}
