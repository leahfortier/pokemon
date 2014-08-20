package map.triggers;

import gui.view.BattleView;
import item.Item;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.Game;
import main.Game.ViewMode;
import main.Global;
import pokemon.ActivePokemon;
import pokemon.PokemonInfo;
import trainer.CharacterData;
import trainer.WildPokemon;
import battle.Battle;
import battle.effect.RepellingEffect;

public class WildBattleTrigger extends Trigger
{
	private static final Pattern eventTriggerPattern = Pattern.compile("(?:encounterRate:\\s*(\\w+)|pokemon:\\s*(\\w+)\\s+(\\d+)-(\\d+)\\s+(\\d+)%)");

	public int[] probability, lowLevel, highLevel;
	public String[] pokemon;
	public double encounterRate;
	public String encounterRateString;

	public WildBattleTrigger(String name, String function)
	{
		super(name, function);
		
		int count = 0;
		Matcher m = eventTriggerPattern.matcher(function);
		while (m.find())
		{
			if (m.group(2) != null) ++count;
		}
		
		probability = new int[count];
		lowLevel = new int[count];
		highLevel = new int[count];
		pokemon = new String[count];

		int index = 0;
		m = eventTriggerPattern.matcher(function);
		while (m.find())
		{
			if (m.group(1) != null) encounterRate = convEncounterRate(encounterRateString = m.group(1));
			else 
			{
				pokemon[index] = m.group(2);
				lowLevel[index] = Integer.parseInt(m.group(3));
				highLevel[index] = Integer.parseInt(m.group(4));
				probability[index] = Integer.parseInt(m.group(5));
				
				index++;
			}
		}		
	}
	
	public WildBattleTrigger(String name, int[] probability, int[] lowLevel, int[] highLevel, String[] pokemon, String encounterRate) {
		super(name, "");
		
		this.probability = probability;
		this.lowLevel = lowLevel;
		this.highLevel = highLevel;
		this.pokemon = pokemon;
		this.encounterRateString = encounterRate;
		this.encounterRate = convEncounterRate(encounterRate);
	}
	
	private double convEncounterRate(String s)
	{
		switch(s.toLowerCase())
		{
			case "verycommon":
//				return 10;
				return 15;
			case "common":
//				return 8.5;
				return 12.75;
			case "semi-rare":
//				return 6.75;
				return 10.125;
			case "rare":
//				return 3.33;
				return 4.995;
			case "veryrare":
//				return 1.25;
				return 1.875;
			default:
				Global.error("Invalid encounter rate: " + s);
				return 1;
		}
	}

	public void execute(Game game)
	{
		super.execute(game);
		double rand = Math.random()*187.5/encounterRate;
				
		if (rand < 1) 
		{
			WildPokemon o = legendaryEncounter(game.charData);
			if (o == null)
			{
				int index = Global.getPercentageIndex(probability);
				int level = (int)(Math.random()*(highLevel[index] - lowLevel[index] + 1) + lowLevel[index]);
				o = new WildPokemon(new ActivePokemon(PokemonInfo.getPokemonInfo(pokemon[index]), level, true, false));				
			}
			
			// Maybe you won't actually fight this Pokemon after all (due to repel, cleanse tag, etc.)
			if (game.charData.front().getLevel() >= o.front().getLevel())
			{
				if (game.charData.isUsingRepel()) return;
				
				Item item = game.charData.front().getActualHeldItem();
				if (item instanceof RepellingEffect && Math.random() < ((RepellingEffect)item).chance()) return;
			}
		
			// Let the battle begin!!
			Battle b = new Battle(game.charData, o);
			
			((BattleView)game.viewMap.get(ViewMode.BATTLE_VIEW)).setBattle(b);
			game.setViewMode(ViewMode.BATTLE_VIEW);
		}
	}
	
	// Returns a legendary encounter if applicable and null otherwise
	private WildPokemon legendaryEncounter(CharacterData player)
	{
		if ((int)(Math.random()*1024) == 0 && !player.getPokedex().caught("Mew"))
		{
			return new WildPokemon(new ActivePokemon(PokemonInfo.getPokemonInfo("Mew"), 5, true, false));
		}
		
		return null;
	}
	
	public String triggerDataAsString() {
		StringBuilder ret = new StringBuilder(super.triggerDataAsString());
		ret.append("\tencounterRate: " +encounterRateString +"\n");
		
		for(int currPokemon = 0; currPokemon < pokemon.length; ++currPokemon) {
			ret.append("\tpokemon: " +pokemon[currPokemon] +" " +lowLevel[currPokemon] +"-" +highLevel[currPokemon] +" " +probability[currPokemon] +"%\n");
		}
		
		return ret.toString();
	}
}
