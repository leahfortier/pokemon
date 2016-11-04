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
import trainer.CharacterData;
import trainer.Pokedex.PokedexStatus;
import trainer.WildPokemon;

public class WildBattleTrigger extends Trigger {
	// TODO: Ideally would like to make a separate class for holding these
	private WildEncounter[] wildEncounters;
	private EncounterRate encounterRate;

	WildBattleTrigger(String matcherJson) {
		super(TriggerType.WILD_BATTLE, matcherJson);

		WildBattleTriggerMatcher matcher = AreaDataMatcher.deserialize(matcherJson, WildBattleTriggerMatcher.class);
		this.wildEncounters = matcher.getWildEncounters();
		this.encounterRate = matcher.encounterRate;

		int totalProbability = 0;
		for (WildEncounter wildEncounter : this.wildEncounters) {
			totalProbability = totalProbability + wildEncounter.getProbability();
		}
		
		if (totalProbability != 100) {
			Global.error("Wild battle trigger probabilities add up to " + totalProbability + ", not 100.");
		}
	}
	
	protected void executeTrigger() {
		// TODO: What's going on with this random stuff also maybe this formula should be in the EncounterRate class
		double rand = Math.random()*187.5/encounterRate.getRate();
				
		if (rand < 1) {
			CharacterData player = Game.getPlayer();
			WildPokemon wildPokemon = getWildPokemon();

			// TODO: This should be in a separate method
			// Maybe you won't actually fight this Pokemon after all (due to repel, cleanse tag, etc.)
			if (player.front().getLevel() >= wildPokemon.front().getLevel()) {
				if (player.isUsingRepel()) {
					return;
				}

				// TODO: Make the chance method return an int instead of a double
				Item item = player.front().getActualHeldItem();
				if (item instanceof RepellingEffect && Global.chanceTest((int)(100*((RepellingEffect)item).chance()))) {
					return;
				}
			}

			// TODO: Should probably make a method for this
			boolean seenWildPokemon = player.getPokedex().getStatus(wildPokemon.front().getPokemonInfo().namesies()) == PokedexStatus.NOT_SEEN;
			
			// Let the battle begin!!
			Battle battle = new Battle(wildPokemon);
			Game.setBattleViews(battle, seenWildPokemon);
		}
	}

	public WildEncounter[] getWildEncounters() {
		return this.wildEncounters;
	}

	public EncounterRate getEncounterRate() {
		return this.encounterRate;
	}

	private WildPokemon getWildPokemon() {
		final WildPokemon legendaryEncounter = this.getLegendaryEncounter();
		if (legendaryEncounter != null) {
			return legendaryEncounter;
		}

		return this.wildEncounters[getRandomEncounterIndex()].getWildPokemon();
	}
	
	// Returns a legendary encounter if applicable and null otherwise
	private WildPokemon getLegendaryEncounter() {
		if (Global.chanceTest(1, 1024) && !Game.getPlayer().getPokedex().caught(PokemonNamesies.MEW)) {
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

		Global.error("Probabilities don't add to 100 for wild battle trigger.");
		return -1;
	}
}
