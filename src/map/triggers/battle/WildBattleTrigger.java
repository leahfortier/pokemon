package map.triggers.battle;

import battle.Battle;
import main.Game;
import map.overworld.WildEncounter;
import map.triggers.Trigger;
import trainer.WildPokemon;
import trainer.player.Player;

public class WildBattleTrigger extends Trigger {
    private final WildEncounter wildEncounter;

    public WildBattleTrigger(WildEncounter wildEncounter) {
        this.wildEncounter = wildEncounter;
    }

    @Override
    public void execute() {
        WildPokemon wildPokemon = this.wildEncounter.getWildPokemon();

        Player player = Game.getPlayer();
        player.getMedalCase().encounterPokemon(wildPokemon.front());

        boolean seenWildPokemon = player.getPokedex().isNotSeen(wildPokemon.front());

        // Let the battle begin!!
        Battle battle = new Battle(wildPokemon);
        Game.instance().setBattleViews(battle, seenWildPokemon);
    }
}
