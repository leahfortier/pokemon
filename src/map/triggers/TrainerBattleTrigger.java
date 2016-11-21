package map.triggers;

import battle.Battle;
import main.Game;
import map.entity.EntityAction.BattleAction;
import pattern.PokemonMatcher;
import pattern.action.BattleMatcher;
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
		BattleMatcher battleMatcher = battleAction.getBattleMatcher();

		String trainerName = battleMatcher.getName();
		int cash = battleMatcher.getDatCashMoney();

		this.trainer = new EnemyTrainer(trainerName, cash);

		for (PokemonMatcher matcher : battleMatcher.getPokemon()) {
			trainer.addPokemon(null, ActivePokemon.createActivePokemon(matcher, false));
		}

		this.npcUpdateInteraction = new UpdateMatcher(battleAction.getEntityName(), battleMatcher.getUpdateInteraction());
	}

	protected void executeTrigger() {
		trainer.healAll();

		Battle b = new Battle(trainer, this.npcUpdateInteraction);
		Game.setBattleViews(b, true);
	}
}
