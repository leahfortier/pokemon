package battle.effect;

import pokemon.ActivePokemon;
import trainer.Trainer;
import battle.Battle;

public interface EndBattleEffect
{
	public void afterBattle(Trainer player, Battle b, ActivePokemon p);
}
