package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface CrashDamageMove 
{
	public void crash(Battle b, ActivePokemon user);
}
