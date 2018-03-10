package map.area;

import gui.view.ViewMode;
import main.Game;
import main.Global;
import map.Direction;
import map.MapName;
import pattern.SimpleMapTransition;
import trainer.player.Player;
import util.Serializable;

public class FlyLocation implements Serializable {
    private static final long serialVersionUID = 1L;

    private final MapName mapName;
    private final AreaData area;

    public FlyLocation(MapName mapName, String areaName) {
        this.mapName = mapName;
        this.area = Game.getData().getMap(mapName).getArea(areaName);

        if (!area.isFlyLocation()) {
            Global.error(area + " is not a valid fly location in " + mapName + ".");
        }
    }

    public String getAreaName() {
        return this.area.getAreaName();
    }

    public void fly() {
        Player player = Game.getPlayer();
        String flyEntrance = area.getFlyLocation();

        player.setMap(new SimpleMapTransition(mapName, flyEntrance));
        player.setDirection(Direction.DOWN);
        player.setMapReset(true);

        Game.instance().setViewMode(ViewMode.MAP_VIEW);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof FlyLocation)) {
            return false;
        }

        FlyLocation that = (FlyLocation)other;
        return this.mapName.equals(that.mapName) && this.area.getAreaName().equals(that.area.getAreaName());
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public String toString() {
        return mapName + " -- " + this.area.getAreaName();
    }
}
