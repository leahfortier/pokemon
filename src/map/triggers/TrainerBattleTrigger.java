package map.triggers;

import gui.view.BattleView;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.Game;
import main.Global;
import main.Namesies;
import main.Game.ViewMode;
import main.Namesies.NamesiesType;
import pokemon.ActivePokemon;
import pokemon.PokemonInfo;
import trainer.EnemyTrainer;
import trainer.Opponent;
import trainer.Trainer;
import battle.Attack;
import battle.Battle;
import battle.Move;

/*
 * Format: Name Level Parameters
 * Possible parameters:
 * 		Moves: Move1, Move2, Move3, Move4
 * 		Shiny
 */
public class TrainerBattleTrigger extends Trigger
{
	public static final Pattern eventTriggerPattern = Pattern.compile("(pokemon:)\\s*(\\w+)\\s*(\\d+)([A-Za-z \\t0-9,:]*)|(name:)\\s*([A-Za-z0-9 ]+)|(winGlobal:)\\s*([A-Za-z0-9_]+)|(cash:)\\s*(\\d+)");
	public static final Pattern parameterPattern = Pattern.compile("(?:(Shiny)|(Moves:)\\s*([A-Za-z0-9 ]+),\\s*([A-Za-z0-9 ]+),\\s*([A-Za-z0-9 ]+),\\s*([A-Za-z0-9 ]+)|(Egg))");

	Trainer t;
	String winGlobal;

	public TrainerBattleTrigger(String name, String function)
	{
		super(name, function);

		Matcher m = eventTriggerPattern.matcher(function);
		String trainerName = "???";
		ArrayList<ActivePokemon> pokemon = new ArrayList<>();
		int cash = 0;
		while (m.find())
		{
			if (m.group(1) != null)
			{
				Namesies namesies = Namesies.getValueOf(m.group(2), NamesiesType.POKEMON);
				PokemonInfo pinfo = PokemonInfo.getPokemonInfo(namesies);
				
				int level = Integer.parseInt(m.group(3));
				ActivePokemon p = new ActivePokemon(pinfo, level, false, false);

				Matcher params = parameterPattern.matcher(m.group(4));

				while (params.find())
				{
					if (params.group(1) != null) 
						p.setShiny();
					
					if (params.group(2) != null)
					{
						ArrayList<Move> moves = new ArrayList<>();
						for (int i = 0; i < 4; ++i)
						{
							if (!params.group(3 + i).equals("None"))
							{
								// TODO: Ask Josh how all this trigger stuff works -- will this get called upon the game loading, or not until the trigger is triggered -- like will this throw an error immediately if there is a typo or will it not I need to know I really need to know
								moves.add(new Move(Attack.getAttackFromName(params.group(3 + i))));
							}
						}
						
						p.setMoves(moves);
					}
				}

				pokemon.add(p);
			}
	
			if (m.group(5) != null)
			{
				trainerName = m.group(6);
			}
			
			if (m.group(7) != null)
			{
				winGlobal = m.group(8);
			}
			
			if (m.group(9) != null)
			{
				try
				{
					cash = Integer.parseInt(m.group(10));
				}
				catch (NumberFormatException ex)
				{
					Global.error(m.group(10) + " isn't a number! Only numbers can be cash, I mean, what is " + m.group(10) + " pokedollars\" supposed to mean, anyway?");
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
		
		t = new EnemyTrainer(trainerName, cash);
		for (ActivePokemon p : pokemon) t.addPokemon(null, p);
	}

	public void execute(Game game)
	{
		super.execute(game);
		t.healAll();
		Battle b = new Battle(game.charData, (Opponent) t, winGlobal);

		((BattleView) game.viewMap.get(ViewMode.BATTLE_VIEW)).setBattle(b);
		game.setViewMode(ViewMode.BATTLE_VIEW);
	}
}
