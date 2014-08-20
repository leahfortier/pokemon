package battle.effect;

import battle.Battle;
import pokemon.ActivePokemon;
import pokemon.Stat;

public interface StageChangingEffect 
{
	public int adjustStage(int stage, Stat s, ActivePokemon p, ActivePokemon opp, Battle b, boolean user);
}
