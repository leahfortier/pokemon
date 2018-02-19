package map.triggers.map;

import main.Game;
import main.Global;
import map.MapData;
import map.condition.Condition;
import map.triggers.Trigger;
import trainer.player.Player;
import util.StringUtils;

public class ReloadMapTrigger extends Trigger {
    public ReloadMapTrigger(String contents, Condition condition) {
        this(condition);
        if (!StringUtils.isNullOrEmpty(contents)) {
            Global.error("Contents should be empty for " + this.getClass().getSimpleName());
        }
    }

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
