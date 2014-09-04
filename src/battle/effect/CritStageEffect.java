package battle.effect;

import pokemon.ActivePokemon;

public interface CritStageEffect
{
	public int increaseCritStage(Integer stage, ActivePokemon p);
}
