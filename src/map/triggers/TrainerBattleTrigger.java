package map.triggers;

import gui.view.BattleView;
import gui.view.MapView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.Game;
import main.Game.ViewMode;
import main.Global;
import map.entity.npc.NPCAction.BattleAction;
import pattern.AreaDataMatcher;
import pattern.PokemonMatcher;
import pokemon.ActivePokemon;
import trainer.EnemyTrainer;
import trainer.Opponent;
import trainer.Trainer;
import battle.Battle;

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
	
	private final Trainer trainer;
	private String updateName;
	private String winGlobal;

	public TrainerBattleTrigger(String name, String function) {
		super(name, function);

		List<ActivePokemon> pokemon = new ArrayList<>();

		BattleAction battleAction = AreaDataMatcher.deserialize(function, BattleAction.class);
		String trainerName = battleAction.name;
		int cash = battleAction.cashMoney;
		for (String pokemonString : battleAction.pokemon) {
			pokemon.add(ActivePokemon.createActivePokemon(pokemonString, false));
		}

		this.updateName = battleAction.update;
		this.winGlobal = battleAction.winGlobal;
		
		trainer = new EnemyTrainer(trainerName, cash);
		for (ActivePokemon p : pokemon) {
			trainer.addPokemon(null, p);
		}
	}

	public void execute(Game game) {
		super.execute(game);
		trainer.healAll();
		
		Battle b = new Battle(game.characterData, (Opponent) trainer, winGlobal);
		game.setBattleViews(b, true);
	}
}
