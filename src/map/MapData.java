package map;

import gui.TileSet;
import gui.view.map.MapView;
import main.Game;
import main.Global;
import map.area.AreaData;
import map.entity.Entity;
import map.entity.FishingSpotEntity;
import map.entity.ItemEntity;
import map.entity.MiscEntity;
import map.overworld.WalkType;
import map.overworld.wild.WildEncounterInfo;
import map.triggers.Trigger;
import map.triggers.battle.WalkingWildBattleTrigger;
import map.triggers.map.MapTransitionTrigger;
import pattern.SimpleMapTransition;
import pattern.generic.EntityMatcher;
import pattern.map.EventMatcher;
import pattern.map.FishingMatcher;
import pattern.map.MapDataMatcher;
import pattern.map.MapTransitionMatcher;
import pattern.map.MiscEntityMatcher;
import pattern.map.WildBattleAreaMatcher;
import pattern.map.WildBattleMatcher;
import trainer.player.Player;
import util.MultiMap;
import util.Point;
import util.file.FileIO;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MapData {
    private final MapName name;

    private final Dimension dimension;

    private final Map<MapDataType, int[]> dataMap;
    private final AreaData[] areaData;

    private final List<Entity> entities;
    private final MultiMap<Integer, Trigger> triggers;
    private final Map<String, MapTransitionMatcher> mapEntrances;

    public MapData(File mapFile) {
        name = new MapName(mapFile.getParentFile().getName(), mapFile.getName());

        String beginFilePath = FileIO.makeFolderPath(mapFile.getPath());
        final Map<MapDataType, BufferedImage> imageMap = MapDataType.getImageMap(beginFilePath, name.getMapName());

        BufferedImage backgroundMap = imageMap.get(MapDataType.BACKGROUND);
        dimension = new Dimension(backgroundMap.getWidth(), backgroundMap.getHeight());

        dataMap = new EnumMap<>(MapDataType.class);
        for (MapDataType dataType : MapDataType.values()) {
            BufferedImage image = imageMap.get(dataType);
            dataMap.put(dataType, image.getRGB(0, 0, dimension.width, dimension.height, null, 0, dimension.width));
        }

        entities = new ArrayList<>();
        triggers = new MultiMap<>();
        mapEntrances = new HashMap<>();

        MapDataMatcher mapDataMatcher = MapDataMatcher.matchArea(beginFilePath + name.getMapName() + ".txt");
        this.areaData = mapDataMatcher.getAreaData();

        addEntities(mapDataMatcher.getNPCs());
        addEntities(mapDataMatcher.getItems());

        for (MiscEntityMatcher matcher : mapDataMatcher.getMiscEntities()) {
            for (Point location : matcher.getLocation()) {
                MiscEntity miscEntity = new MiscEntity(
                        matcher.getTriggerName(),
                        location,
                        matcher.getCondition(),
                        matcher.getActions()
                );

                this.entities.add(miscEntity);
            }
        }

        for (MapTransitionMatcher matcher : mapDataMatcher.getMapTransitions()) {
            mapEntrances.put(matcher.getExitName(), matcher);

            List<Point> exits = matcher.getExitLocations();
            if (exits != null) {
                Trigger trigger = new MapTransitionTrigger(matcher);
                exits.forEach(exit -> triggers.put(getMapIndex(exit), trigger));
            }
        }

        for (EventMatcher matcher : mapDataMatcher.getEvents()) {
            Trigger trigger = matcher.getActions().getGroupTrigger(
                    matcher.getTriggerName(),
                    matcher.getCondition()
            );

            for (Point point : matcher.getLocation()) {
                triggers.put(getMapIndex(point), trigger);
            }
        }

        for (WildBattleAreaMatcher matcher : mapDataMatcher.getWildBattles()) {
            for (Point point : matcher.getLocation()) {
                for (WildBattleMatcher wildBattleMatcher : matcher.getWildBattles()) {
                    Trigger trigger = new WalkingWildBattleTrigger(wildBattleMatcher);
                    triggers.put(getMapIndex(point), trigger);
                    for (WildEncounterInfo wildEncounter : wildBattleMatcher.getWildEncounters()) {
                        this.getArea(point).addPokemon(wildEncounter.getPokemonName());
                    }
                }
            }
        }

        for (FishingMatcher matcher : mapDataMatcher.getFishingSpots()) {
            for (Point location : matcher.getLocation()) {
                FishingSpotEntity fishingSpotEntity = new FishingSpotEntity(
                        location,
                        matcher.getTriggerName(),
                        matcher.getCondition(),
                        matcher.getWildEncounters()
                );

                this.entities.add(fishingSpotEntity);
            }
        }
    }

    private void addEntities(List<? extends EntityMatcher> entityMatchers) {
        this.entities.addAll(entityMatchers.stream()
                                           .map(EntityMatcher::createEntity)
                                           .collect(Collectors.toList()));
    }

    private int getMapIndex(Point point) {
        return point.getIndex(getDimension().width);
    }

    public MapName getName() {
        return this.name;
    }

    public MapTransitionMatcher getEntrance(String entranceName) {
        return this.mapEntrances.get(entranceName);
    }

    public boolean hasEntrance(String entranceName) {
        return this.mapEntrances.containsKey(entranceName);
    }

    public PathDirection getExitDirection(String entranceName) {
        return this.mapEntrances.get(entranceName).getDirection();
    }

    public Point getEntranceLocation(SimpleMapTransition mapTransition) {
        return getEntranceLocation(mapTransition.getNextEntranceName(), mapTransition.getTransitionIndex(), mapTransition.numExits());
    }

    public Point getEntranceLocation(String entranceName, int exitIndex, int numExits) {
        MapTransitionMatcher entranceMatcher = this.mapEntrances.get(entranceName);
        List<Point> entrances = entranceMatcher.getLocation();

        int entranceIndex = (int)(((double)entrances.size()/numExits)*exitIndex);
        return entrances.get(entranceIndex);
    }

    public Dimension getDimension() {
        return this.dimension;
    }

    private int getRGB(Point location, MapDataType dataType) {
        return getRGB(location.x, location.y, dataType);
    }

    public int getRGB(int x, int y, MapDataType dataType) {
        if (!Point.inBounds(x, y, this.dimension)) {
            return TileSet.INVALID_RGB;
        }

        return this.dataMap.get(dataType)[Point.getIndex(x, y, dimension.width)];
    }

    public WalkType getPassValue(Point location) {
        int rgb = getRGB(location, MapDataType.MOVE);
        if (rgb == TileSet.INVALID_RGB) {
            return WalkType.NOT_WALKABLE;
        }

        return WalkType.getWalkType(rgb);
    }

    public boolean isPassable(Point location, Direction direction) {
        return getPassValue(location).isPassable(direction) && (!hasEntity(location) || getEntity(location).isPassable());
    }

    public AreaData getArea(String areaName) {
        for (AreaData data : areaData) {
            if (data.getAreaName().equals(areaName)) {
                return data;
            }
        }

        Global.error("No area found with area name " + areaName + " for map " + this.name);
        return AreaData.VOID;
    }

    public AreaData getArea(Point location) {
        if (areaData.length == 1) {
            return areaData[0];
        }

        int areaRgb = getRGB(location, MapDataType.AREA);
        if (areaRgb == TileSet.INVALID_RGB) {
            return AreaData.VOID;
        }

        Color areaColor = new Color(areaRgb);
        for (AreaData data : areaData) {
            if (data.isColor(areaColor)) {
                return data;
            }
        }

        Global.info("No area found with color " + areaRgb + " for map " + this.name);
        return AreaData.VOID;
    }

    public List<Trigger> getCurrentTriggers() {
        int val = Game.getPlayer().getLocation().getIndex(dimension.width);
        if (triggers.containsKey(val)) {
            return triggers.get(val);
        }

        return null;
    }

    public boolean setCharacterToEntrance() {
        Player player = Game.getPlayer();
        SimpleMapTransition mapTransition = player.getMapTransition();
        if (mapEntrances.containsKey(mapTransition.getNextEntranceName())) {
            Point entranceLocation = getEntranceLocation(mapTransition);
            player.setLocation(entranceLocation);

            return true;
        }

        return false;
    }

    public List<ItemEntity> getHiddenItems() {
        return entities.stream()
                       .filter(entity -> entity.isActive() && entity instanceof ItemEntity)
                       .map(entity -> (ItemEntity)entity)
                       .filter(ItemEntity::isHiddenItem)
                       .collect(Collectors.toList());
    }

    public Entity getEntity(Point location) {
        Player player = Game.getPlayer();
        if (location.equals(player.getLocation())) {
            return player.getEntity();
        }

        List<Entity> presentEntities = entities
                .stream()
                .filter(entity -> entity.isActive() && entity.getLocation().equals(location))
                .collect(Collectors.toList());

        return validateEntities(presentEntities);
    }

    public Entity getEntity(String entityName) {
        List<Entity> presentEntities = entities
                .stream()
                .filter(entity -> entity.isActive() && entity.getEntityName().equals(entityName))
                .collect(Collectors.toList());

        return validateEntities(presentEntities);
    }

    private Entity validateEntities(List<Entity> entities) {
        if (entities.isEmpty()) {
            return null;
        }

        if (entities.size() > 1) {
            List<Entity> highPriority = entities
                    .stream()
                    .filter(Entity::isHighPriorityEntity)
                    .collect(Collectors.toList());

            if (highPriority.size() != 1) {
                Global.error("Multiple entities present");
            }

            return highPriority.get(0);
        }

        return entities.get(0);
    }

    public boolean hasEntity(Point location) {
        return getEntity(location) != null;
    }

    public void updateEntities(int dt, MapView mapView) {
        entities.forEach(entity -> entity.update(dt, this, mapView));
    }

    public void populateEntities() {
        entities.stream()
                .filter(Entity::setVisible)
                .forEach(entity -> {
                    entity.reset();
                    entity.getTrigger();
                });
    }
}
