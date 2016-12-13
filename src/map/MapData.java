package map;

import gui.TileSet;
import gui.view.map.MapView;
import main.Game;
import main.Global;
import map.entity.Entity;
import map.entity.EntityAction;
import map.triggers.Trigger;
import map.triggers.TriggerData;
import map.triggers.TriggerType;
import pattern.generic.EntityMatcher;
import pattern.map.EventMatcher;
import pattern.map.MapDataMatcher;
import pattern.map.MapTransitionMatcher;
import pattern.map.WildBattleMatcher;
import trainer.CharacterData;
import util.FileIO;
import util.JsonUtils;
import util.MultiMap;
import util.Point;

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
	private final String name;

	private final Dimension dimension;

	private final Map<MapDataType, int[]> dataMap;
	private final AreaData[] areaData;

	private final List<Entity> entities;
	private final MultiMap<Integer, String> triggers;
	private final Map<String, Integer> mapEntrances;

	public MapData(File file) {
		name = file.getName();

		String beginFilePath = FileIO.makeFolderPath(file.getPath());
		final Map<MapDataType, BufferedImage> imageMap = MapDataType.getImageMap(beginFilePath, name);

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

		MapDataMatcher mapDataMatcher = MapDataMatcher.matchArea(beginFilePath + name + ".txt");
		this.areaData = mapDataMatcher.getAreaData();

		addEntities(mapDataMatcher.getNPCs());
		addEntities(mapDataMatcher.getItems());
		addEntities(mapDataMatcher.getMiscEntities());

		for (MapTransitionMatcher matcher : mapDataMatcher.getMapTransitions()) {
            matcher.setMapName(this.name);

			Point entrance = matcher.getLocation();
			mapEntrances.put(matcher.getExitName(), getMapIndex(entrance));

            Point exit = matcher.getExitLocation();
			if (exit != null) {
				Trigger trigger = TriggerType.MAP_TRANSITION.createTrigger(JsonUtils.getJson(matcher), null);
				triggers.put(getMapIndex(exit), trigger.getName());
			}
        }

		for (EventMatcher matcher : mapDataMatcher.getEvents()) {
			TriggerData triggerData = new TriggerData(matcher);
			Trigger trigger = EntityAction.addActionGroupTrigger(
					triggerData.getName(),
					triggerData.getName(),
					triggerData.getCondition(),
					triggerData.getActions()
			);

			for (Point point : matcher.getLocation()) {
				triggers.put(getMapIndex(point), trigger.getName());
			}
		}

		for (WildBattleMatcher matcher : mapDataMatcher.getWildBattles()) {
			Trigger trigger = TriggerType.WILD_BATTLE.createTrigger(JsonUtils.getJson(matcher), null);
			for (Point point : matcher.getLocation()) {
				triggers.put(getMapIndex(point), trigger.getName());
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

	public Point getEntranceLocation(String entranceName) {
		return Point.getPointAtIndex(this.mapEntrances.get(entranceName), getDimension().width);
	}

	public Dimension getDimension() {
		return this.dimension;
	}

	private int getRGB(Point location, MapDataType dataType) {
		return getRGB(location.x, location.y, dataType);
	}

	private int getRGB(int x, int y, MapDataType dataType) {
		if (!Point.inBounds(x, y, this.dimension)) {
			return TileSet.INVALID_RGB;
		}

		return this.dataMap.get(dataType)[Point.getIndex(x, y, dimension.width)];
	}

	public int getBgTile(int x, int y) {
		return getRGB(x, y, MapDataType.BACKGROUND);
	}

	public int getFgTile(int x, int y) {
		return getRGB(x, y, MapDataType.FOREGROUND);
	}

	public WalkType getPassValue(Point location) {
		int rgb = getRGB(location, MapDataType.MOVE);
		if (rgb == TileSet.INVALID_RGB) {
			return WalkType.NOT_WALKABLE;
		}

		return WalkType.getWalkType(rgb);
	}

	public boolean isPassable(Point location, Direction direction) {
		return getPassValue(location).isPassable(direction);
	}

	public AreaData getArea(Point location) {
		if (areaData.length == 1) {
			return areaData[0];
		}

		int areaColor = getRGB(location, MapDataType.AREA);
		if (areaColor == TileSet.INVALID_RGB) {
			return AreaData.VOID;
		}

		for (AreaData data : areaData) {
			if (data.isColor(areaColor)) {
				return data;
			}
		}

		Global.error("No area found with color " + areaColor + " for map " + this.name);
		return AreaData.VOID;
	}

	public List<String> getCurrentTriggers() {
		int val = Game.getPlayer().getLocation().getIndex(dimension.width);
		if (triggers.containsKey(val)) {
			return triggers.get(val);
		}
	
		return null;
	}
	
	public boolean setCharacterToEntrance() {
		CharacterData player = Game.getPlayer();
		String entranceName = player.getMapEntranceName();
        if (mapEntrances.containsKey(entranceName)) {
			int entranceIndex = mapEntrances.get(entranceName);
			Point entranceLocation = Point.getPointAtIndex(entranceIndex, dimension.width);
			player.setLocation(entranceLocation);

			return true;
		}

		return false;
	}

	public Entity getEntity(Point location) {
		CharacterData player = Game.getPlayer();
		if (location.equals(player.getLocation())) {
			return player.getEntity();
		}

		List<Entity> presentEntities = entities.stream()
				.filter(entity -> entity.isVisible() && entity.getLocation().equals(location))
				.collect(Collectors.toList());

		return validateEntities(presentEntities);
	}

	public Entity getEntity(String entityName) {
		List<Entity> presentEntities = entities.stream()
				.filter(entity -> entity.isVisible() && entity.getEntityName().equals(entityName))
				.collect(Collectors.toList());

		return validateEntities(presentEntities);
	}

	private Entity validateEntities(List<Entity> entities) {
		if (entities.isEmpty()) {
			return null;
		}

		if (entities.size() != 1) {
			Global.error("Multiple entities present");
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
					entity.addData();
				});
	}
}
