package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface BarrierEffect extends DefogRelease {
	void breakBarrier(Battle b, ActivePokemon breaker);
}
