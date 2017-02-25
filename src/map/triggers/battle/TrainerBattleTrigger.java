package map.triggers.battle;

import battle.Battle;
import main.Game;
import map.entity.EntityAction.BattleAction;
import map.triggers.Trigger;
import map.triggers.TriggerType;
import pattern.PokemonMatcher;
import pattern.action.BattleMatcher;
import pattern.action.UpdateMatcher;
import pokemon.ActivePokemon;
import trainer.EnemyTrainer;
import util.JsonUtils;

public class TrainerBattleTrigger extends Trigger {
	private final EnemyTrainer trainer;
	private final UpdateMatcher npcUpdateInteraction;

	public TrainerBattleTrigger(String contents, String condition) {
		super(TriggerType.TRAINER_BATTLE, contents, condition);

		BattleAction battleAction = JsonUtils.deserialize(contents, BattleAction.class);
		BattleMatcher battleMatcher = battleAction.getBattleMatcher();

		String trainerName = battleMatcher.getName();
		int cash = battleMatcher.getDatCashMoney();

		this.trainer = new EnemyTrainer(trainerName, cash);

		for (PokemonMatcher matcher : battleMatcher.getPokemon()) {
			trainer.addPokemon(ActivePokemon.createActivePokemon(matcher, false));
		}

		this.npcUpdateInteraction = new UpdateMatcher(battleAction.getEntityName(), battleMatcher.getUpdateInteraction());
	}

	protected void executeTrigger() {
		trainer.healAll();

		Battle b = new Battle(trainer, this.npcUpdateInteraction);
		Game.instance().setBattleViews(b, true);
	}
}
