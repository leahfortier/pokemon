package map.triggers;

import battle.Battle;
import main.Game;
import map.entity.EntityAction.BattleAction;
import pattern.PokemonMatcher;
import pattern.action.UpdateMatcher;
import pokemon.ActivePokemon;
import trainer.EnemyTrainer;
import util.JsonUtils;

class TrainerBattleTrigger extends Trigger {
	private final EnemyTrainer trainer;
	private final UpdateMatcher npcUpdateInteraction;

	TrainerBattleTrigger(String contents, String condition) {
		super(TriggerType.TRAINER_BATTLE, contents, condition);

		BattleAction battleAction = JsonUtils.deserialize(contents, BattleAction.class);

		String trainerName = battleAction.name;
		int cash = battleAction.cashMoney;

		this.trainer = new EnemyTrainer(trainerName, cash);

		for (PokemonMatcher matcher : battleAction.pokemon) {
			trainer.addPokemon(null, ActivePokemon.createActivePokemon(matcher, false));
		}

		this.npcUpdateInteraction = new UpdateMatcher(battleAction.entityName, battleAction.updateInteraction);
	}

	protected void executeTrigger() {
		trainer.healAll();

		Battle b = new Battle(trainer, this.npcUpdateInteraction);
		Game.setBattleViews(b, true);
	}
}
