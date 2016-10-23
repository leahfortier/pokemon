package battle.effect.attack;

import battle.Battle;
import pokemon.ActivePokemon;

public interface RecoilMove {
	void applyRecoil(Battle b, ActivePokemon user, Integer damage);
}
