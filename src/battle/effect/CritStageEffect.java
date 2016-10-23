package battle.effect;

import pokemon.ActivePokemon;

public interface CritStageEffect {
	int increaseCritStage(Integer stage, ActivePokemon p);
}
