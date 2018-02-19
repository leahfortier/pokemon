package map.triggers.map;

import main.Game;
import map.MapData;
import map.condition.Condition;
import map.triggers.Trigger;
import trainer.player.Player;

public class ReloadMapTrigger extends Trigger {
    public ReloadMapTrigger(Condition condition) {
        super(null, condition);
    }

    @Override
    protected void executeTrigger() {
        Player player = Game.getPlayer();
        MapData currentMap = Game.getData().getMap(player.getMapName());

        // Repopulate the entities of the current map
        currentMap.populateEntities();
    }
}
