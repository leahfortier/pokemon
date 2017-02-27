package map.triggers.battle;

import battle.Battle;
import main.Game;
import map.overworld.WildEncounter;
import map.triggers.Trigger;
import map.triggers.TriggerType;
import trainer.WildPokemon;
import util.SerializationUtils;

public class WildBattleTrigger extends Trigger {

	private final WildEncounter wildEncounter;

	public WildBattleTrigger(String matcherJson, String condition) {
		super(TriggerType.WILD_BATTLE, matcherJson, condition);

		this.wildEncounter = SerializationUtils.deserializeJson(matcherJson, WildEncounter.class);
	}

	protected void executeTrigger() {
		WildPokemon wildPokemon = this.wildEncounter.getWildPokemon();

		boolean seenWildPokemon = Game.getPlayer().getPokedex().isNotSeen(wildPokemon.front().getPokemonInfo().namesies());

		// Let the battle begin!!
		Battle battle = new Battle(wildPokemon);
		Game.instance().setBattleViews(battle, seenWildPokemon);
	}
}
