package map.entity;

import gui.TileSet;
import gui.view.map.MapView;
import main.Game;
import map.Direction;
import map.MapData;
import map.WildEncounter;
import map.entity.EntityAction.TriggerAction;
import map.triggers.TriggerType;
import pattern.map.FishingMatcher;
import util.JsonUtils;
import util.Point;

import java.awt.image.BufferedImage;
import java.util.Collections;

public class FishingSpotEntity extends Entity {

    private final WildEncounter[] wildEncounters;
    private boolean dataCreated;

    public FishingSpotEntity(Point location, String entityName, String condition, WildEncounter[] wildEncounters) {
        super(location, entityName, condition);

        this.wildEncounters = wildEncounters;
        this.dataCreated = false;
    }

    @Override
    public boolean isHighPriorityEntity() {
        return false;
    }

    @Override
    public void update(int dt, MapData currentMap, MapView view) {}

    @Override
    protected BufferedImage getFrame() {
        return Game.getData().getTrainerTiles().getTile(TileSet.EMPTY_IMAGE);
    }

    @Override
    protected boolean isTransitioning() {
        return false;
    }

    @Override
    public void getAttention(Direction direction) {}

    @Override
    public void reset() {}

    @Override
    public void addData() {
        if (dataCreated) {
            return;
        }

        // TODO: condition -- need fishing rod
        FishingMatcher fishingMatcher = new FishingMatcher(this.getEntityName(), wildEncounters);
        EntityAction entityAction = new TriggerAction(TriggerType.FISHING, JsonUtils.getJson(fishingMatcher), null);

        EntityAction.addActionGroupTrigger(
                this.getEntityName(),
                this.getTriggerSuffix(),
                this.getConditionString(),
                Collections.singletonList(entityAction)
        );

        dataCreated = true;
    }
}
