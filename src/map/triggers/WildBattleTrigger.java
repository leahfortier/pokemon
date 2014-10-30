package map.triggers;

import gui.view.BattleView;
import gui.view.MapView;
import item.Item;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.Game;
import main.Game.ViewMode;
import main.Global;
import main.Namesies;
import main.Namesies.NamesiesType;
import pokemon.ActivePokemon;
import pokemon.PokemonInfo;
import trainer.CharacterData;
import trainer.WildPokemon;
import trainer.Pokedex.PokedexStatus;
import battle.Battle;
import battle.effect.RepellingEffect;

public class WildBattleTrigger extends Trigger
{
	private static final Pattern eventTriggerPattern = Pattern.compile("(?:encounterRate:\\s*(\\w+)|pokemon:\\s*(\\w+)\\s+(\\d+)-(\\d+)\\s+(\\d+)%)");

	public WildEncounter[] wildEncounters;
	public EncounterRate encounterRate;
	
	public static enum EncounterRate
	{
		VERY_COMMON(15),
		COMMON(12.75),
		SEMI_RARE(10.125),
		RARE(4.995),
		VERY_RARE(1.875);
		
		private double rate;
		
		private EncounterRate(double rate)
		{
			this.rate = rate;
		}
		
		public static String[] ENCOUNTER_RATE_NAMES;
		static
		{
			ENCOUNTER_RATE_NAMES = new String[EncounterRate.values().length];
			for (int i = 0; i < ENCOUNTER_RATE_NAMES.length; i++)
			{
				ENCOUNTER_RATE_NAMES[i] = EncounterRate.values()[i].name();
			}
		}
	}
	
	public static class WildEncounter
	{
		public Namesies pokemon;
		
		public int minLevel;
		public int maxLevel;

		public int probability;
		
		public WildEncounter(String pokemon, String minLevel, String maxLevel, String probability)
		{
			this.pokemon = Namesies.getValueOf(pokemon, NamesiesType.POKEMON);
			
			this.minLevel = Integer.parseInt(minLevel);
			this.maxLevel = Integer.parseInt(maxLevel);
			
			this.probability = Integer.parseInt(probability);
		}
	}

	public WildBattleTrigger(String name, String function)
	{
		super(name, function);
		
		int count = 0;
		Matcher m = eventTriggerPattern.matcher(function);
		while (m.find())
		{
			if (m.group(2) != null) 
			{
				++count;
			}
		}
		
		wildEncounters = new WildEncounter[count];
		
		int totalProbability = 0;

		int index = 0;
		m = eventTriggerPattern.matcher(function);
		
		System.out.println(function);
		
		while (m.find())
		{
			if (m.group(1) != null) 
			{
				encounterRate = EncounterRate.valueOf(m.group(1));
			}
			else 
			{
				wildEncounters[index] = new WildEncounter(m.group(2), m.group(3), m.group(4), m.group(5));
				totalProbability += wildEncounters[index].probability;
				
				index++;
			}
		}
		
		if (totalProbability != 100)
		{
			Global.error(name + " wild battle trigger probabilities add up to " + totalProbability + ", not 100.");
		}
	}
	
	public WildBattleTrigger(String name, WildEncounter[] wildEncounters, EncounterRate encounterRate) 
	{
		super(name, "");
		
		this.wildEncounters = wildEncounters;
		this.encounterRate = encounterRate;
	}

	private int getRandomEncounterIndex()
	{
		int sum = 0, random = (int) (Math.random() * 100);
		for (int i = 0; i < wildEncounters.length; i++)
		{
			sum += wildEncounters[i].probability;
			
			if (random < sum) 
			{
				return i;
			}
		}
		
		Global.error("Probabilities don't add to 100 for " + this.name + " wild battle trigger.");
		return -1;
	}
	
	public void execute(Game game)
	{
		super.execute(game);
		double rand = Math.random()*187.5/encounterRate.rate;
				
		if (rand < 1) 
		{
			WildPokemon o = legendaryEncounter(game.charData);
			if (o == null)
			{
				WildEncounter encounter = wildEncounters[getRandomEncounterIndex()];
				
				int level = (int)(Math.random()*(encounter.maxLevel - encounter.minLevel + 1) + encounter.minLevel);
				
				o = new WildPokemon(new ActivePokemon(PokemonInfo.getPokemonInfo(encounter.pokemon), level, true, false));				
			}
			
			// Maybe you won't actually fight this Pokemon after all (due to repel, cleanse tag, etc.)
			if (game.charData.front().getLevel() >= o.front().getLevel())
			{
				if (game.charData.isUsingRepel()) 
				{
					return;
				}
				
				Item item = game.charData.front().getActualHeldItem();
				if (item instanceof RepellingEffect && Math.random() < ((RepellingEffect)item).chance()) 
				{
					return;
				}
			}
		
			boolean seenWildPokemon = game.charData.getPokedex().getStatus(o.front().getPokemonInfo().namesies()) == PokedexStatus.NOT_SEEN;
			
			// Let the battle begin!!
			Battle b = new Battle(game.charData, o);
			
			((BattleView)game.viewMap.get(ViewMode.BATTLE_VIEW)).setBattle(b);
			((MapView)game.viewMap.get(ViewMode.MAP_VIEW)).setBattle(b, seenWildPokemon);
		}
	}
	
	// Returns a legendary encounter if applicable and null otherwise
	private WildPokemon legendaryEncounter(CharacterData player)
	{
		if ((int)(Math.random()*1024) == 0 && !player.getPokedex().caught(Namesies.MEW_POKEMON))
		{
			return new WildPokemon(new ActivePokemon(PokemonInfo.getPokemonInfo(Namesies.MEW_POKEMON), 5, true, false));
		}
		
		return null;
	}
	
	public String triggerDataAsString() 
	{
		StringBuilder ret = new StringBuilder(super.triggerDataAsString());
		ret.append("\tencounterRate: " + encounterRate.name() + "\n");
		
		for (WildEncounter wildEncounter : wildEncounters) 
		{
			ret.append(String.format("\tpokemon: %s %d-%d %d%%n", wildEncounter.pokemon.getName(), wildEncounter.minLevel, wildEncounter.maxLevel, wildEncounter.probability));
		}
		
		return ret.toString();
	}
}
