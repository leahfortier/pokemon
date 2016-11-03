package map.triggers;

import battle.Battle;
import main.Game;
import main.Global;
import pokemon.ActivePokemon;
import trainer.EnemyTrainer;
import trainer.Opponent;
import trainer.Trainer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
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
	
	private final Trainer trainer;
	private String winGlobal;

	public TrainerBattleTrigger(String name, String function) {
		super(name, function);

		Matcher m = trainerBattleTriggerPattern.matcher(function);
		String trainerName = "???"; // TODO: Why is this the default and why isn't it a constant?
		List<ActivePokemon> pokemon = new ArrayList<>();
		int cash = 0;
		while (m.find()) {
			if (m.group(1) != null) {
				pokemon.add(ActivePokemon.createActivePokemon(m.group(2), false));
			}
	
			if (m.group(3) != null) {
				trainerName = m.group(4);
			}
			
			if (m.group(5) != null) {
				winGlobal = m.group(6);
			}
			
			if (m.group(7) != null) {
				try {
					cash = Integer.parseInt(m.group(8));
				}
				catch (NumberFormatException ex) {
					Global.error(m.group(8) + " isn'trainer a number! Only numbers can be cash, I mean, what is " + m.group(8) + " pokedollars\" supposed to mean, anyway?");
				}
			}
		}
				
//		System.out.println(trainerName);
//		for (ActivePokemon p : pokemon)
//		{
//			System.out.println(p.getName() + " Lv. " + p.getLevel());
//			for (Move m2 : p.getMoves()) System.out.println(m2.getAttack().getName());
//			System.out.println();
//		}
//		System.out.println();
		
		trainer = new EnemyTrainer(trainerName, cash);
		for (ActivePokemon p : pokemon) {
			trainer.addPokemon(null, p);
		}
	}

	public void execute() {
		super.execute();
		trainer.healAll();
		
		Battle b = new Battle((Opponent)trainer, winGlobal);
		Game.setBattleViews(b, true);
	}
}
