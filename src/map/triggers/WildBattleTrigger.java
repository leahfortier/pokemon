package map.triggers;

import battle.Battle;
import battle.effect.RepellingEffect;
import item.Item;
import main.Game;
import main.Global;
import map.EncounterRate;
import map.WildEncounter;
import namesies.PokemonNamesies;
import pattern.AreaDataMatcher;
import pattern.AreaDataMatcher.WildBattleTriggerMatcher;
import pokemon.ActivePokemon;
import pokemon.PokemonInfo;
import trainer.CharacterData;
import trainer.Pokedex.PokedexStatus;
import trainer.WildPokemon;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WildBattleTrigger extends Trigger {
	private static final Pattern eventTriggerPattern = Pattern.compile("(?:encounterRate:\\s*(\\w+)|pokemon:\\s*(\\w+)\\s+(\\d+)-(\\d+)\\s+(\\d+)%)");

	// TODO: Ideally would like to make a separate class for holding these
	private WildEncounter[] wildEncounters;
	private EncounterRate encounterRate;

	public WildBattleTrigger(String name, String function) {
		super(name, function);

		WildBattleTriggerMatcher matcher = AreaDataMatcher.deserialize(function, WildBattleTriggerMatcher.class);
		this.wildEncounters = matcher.getWildEncounters();
		this.encounterRate = matcher.encounterRate;

		int totalProbability = 0;
		for (WildEncounter wildEncounter : this.wildEncounters) {
			totalProbability = totalProbability + wildEncounter.getProbability();
		}
		
		if (totalProbability != 100) {
			Global.error(name + " wild battle trigger probabilities add up to " + totalProbability + ", not 100.");
		}
	}
	
	public WildBattleTrigger(String name, WildEncounter[] wildEncounters, EncounterRate encounterRate) {
		super(name, "");
		
		this.wildEncounters = wildEncounters;
		this.encounterRate = encounterRate;
	}
	
	public void execute(Game game) {
		super.execute(game);

		// TODO: What's going on with this random stuff also maybe this formula should be in the EncounterRate class
		double rand = Math.random()*187.5/encounterRate.getRate();
				
		if (rand < 1) {
			WildPokemon wildPokemon = getWildPokemon(game.characterData);

			// TODO: This should be in a separate method
			// Maybe you won't actually fight this Pokemon after all (due to repel, cleanse tag, etc.)
			if (game.characterData.front().getLevel() >= wildPokemon.front().getLevel()) {
				if (game.characterData.isUsingRepel()) {
					return;
				}

				// TODO: Make the chance method return an int instead of a double
				Item item = game.characterData.front().getActualHeldItem();
				if (item instanceof RepellingEffect && Global.chanceTest((int)(100*((RepellingEffect)item).chance()))) {
					return;
				}
			}

			// TODO: Should probably make a method for this
			boolean seenWildPokemon = game.characterData.getPokedex().getStatus(wildPokemon.front().getPokemonInfo().namesies()) == PokedexStatus.NOT_SEEN;
			
			// Let the battle begin!!
			Battle battle = new Battle(game.characterData, wildPokemon);
			game.setBattleViews(battle, seenWildPokemon);
		}
	}

	public WildEncounter[] getWildEncounters() {
		return this.wildEncounters;
	}

	public EncounterRate getEncounterRate() {
		return this.encounterRate;
	}

	private WildPokemon getWildPokemon(final CharacterData player) {
		final WildPokemon legendaryEncounter = this.getLegendaryEncounter(player);
		if (legendaryEncounter != null) {
			return legendaryEncounter;
		}

		return this.wildEncounters[getRandomEncounterIndex()].getWildPokemon();
	}
	
	// Returns a legendary encounter if applicable and null otherwise
	private WildPokemon getLegendaryEncounter(CharacterData player) {
		if (Global.chanceTest(1, 1024) && !player.getPokedex().caught(PokemonNamesies.MEW)) {
			return new WildPokemon(new ActivePokemon(PokemonNamesies.MEW, 5, true, false));
		}
		
		return null;
	}

	// TODO: I think there might be a method in Global that does something like this already if not try and make one and include the wild hold items also
	private int getRandomEncounterIndex() {
		int sum = 0, random = Global.getRandomInt(100);
		for (int i = 0; i < wildEncounters.length; i++) {
			sum += wildEncounters[i].getProbability();

			if (random < sum) {
				return i;
			}
		}

		Global.error("Probabilities don't add to 100 for " + this.name + " wild battle trigger.");
		return -1;
	}
	
	public String triggerDataAsString() {
		StringBuilder ret = new StringBuilder(super.triggerDataAsString());
		ret.append("\tencounterRate: ")
				.append(encounterRate.name())
				.append("\n");
		
		for (WildEncounter wildEncounter : wildEncounters) {
			ret.append(wildEncounter.toString());
		}
		
		return ret.toString();
	}
}
