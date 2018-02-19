package map.triggers.map;

import main.Game;
import map.MapData;
import map.triggers.Trigger;
import trainer.player.Player;

public class ReloadMapTrigger extends Trigger {
    public ReloadMapTrigger() {
        super(null);
    }

    @Override
    public void execute() {
        Player player = Game.getPlayer();
        MapData currentMap = Game.getData().getMap(player.getMapName());

        // Repopulate the entities of the current map
        currentMap.populateEntities();
    }
}
