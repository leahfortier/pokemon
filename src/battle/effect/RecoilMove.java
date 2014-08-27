package battle.effect;

import battle.Battle;
import pokemon.ActivePokemon;

public interface RecoilMove 
{
	public void applyRecoil(Battle b, ActivePokemon user, Integer damage);
}
