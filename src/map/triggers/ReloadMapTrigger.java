package map.triggers;

import main.Game;
import main.Global;
import map.MapData;
import trainer.Player;
import util.StringUtils;

class ReloadMapTrigger extends Trigger {
    ReloadMapTrigger(String contents, String condition) {
        super(TriggerType.RELOAD_MAP, contents, condition);

        if (!StringUtils.isNullOrEmpty(contents)) {
            Global.error("Contents should be empty for " + this.getClass().getSimpleName());
        }
    }

    @Override
    protected void executeTrigger() {
        Player player = Game.getPlayer();
        MapData currentMap = Game.getData().getMap(player.getMapName());

        // Repopulate the entities of the current map
        currentMap.populateEntities();
    }
}
