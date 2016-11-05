package map.triggers;

import battle.Battle;
import main.Game;
import map.entity.npc.EntityAction.BattleAction;
import pattern.AreaDataMatcher;
import pattern.AreaDataMatcher.UpdateMatcher;
import pattern.PokemonMatcher;
import pokemon.ActivePokemon;
import trainer.EnemyTrainer;

import java.util.regex.Pattern;

/*
 * Format: Name Level Parameters
 * Possible parameters:
 * 		Moves: Move1, Move2, Move3, Move4*
 * 		Shiny
 * 		Egg
 * 		Item: item name*
 */
public class TrainerBattleTrigger extends Trigger {
	public static final Pattern trainerBattleTriggerPattern = Pattern.compile("(pokemon:)\\s*([A-Za-z \\t0-9,:.\\-'*]*)|(name:)\\s*([A-Za-z0-9 ]+)|(winGlobal:)\\s*([A-Za-z0-9_]+)|(cash:)\\s*(\\d+)");

	private final EnemyTrainer trainer;
	private final UpdateMatcher npcUpdateInteraction;

	TrainerBattleTrigger(String contents) {
		super(TriggerType.TRAINER_BATTLE, contents);

		BattleAction battleAction = AreaDataMatcher.deserialize(contents, BattleAction.class);

		String trainerName = battleAction.name;
		int cash = battleAction.cashMoney;

		this.trainer = new EnemyTrainer(trainerName, cash);

		for (String pokemonString : battleAction.pokemon) {
			PokemonMatcher matcher = PokemonMatcher.matchPokemonDescription(pokemonString);
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
