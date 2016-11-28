package map;

import gui.TileSet;
import main.Game;
import main.Global;
import map.entity.Entity;
import map.entity.EntityAction;
import map.triggers.Trigger;
import map.triggers.TriggerData;
import map.triggers.TriggerType;
import pattern.map.EventMatcher;
import pattern.map.ItemMatcher;
import pattern.map.MapDataMatcher;
import pattern.map.MapTransitionMatcher;
import pattern.map.MiscEntityMatcher;
import pattern.map.NPCMatcher;
import pattern.map.WildBattleMatcher;
import util.FileIO;
import util.JsonUtils;
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
	
	private final Map<Integer, String> triggers;
	private final Map<String, Integer> mapEntrances;
	
	private final List<Entity> entities;
	
	public MapData(File file) {
		name = file.getName();

		String beginFilePath = FileIO.makeFolderPath(file.getPath()) + name;
		final Map<MapDataType, BufferedImage> imageMap = new EnumMap<>(MapDataType.class);
		for (MapDataType dataType : MapDataType.values()) {
			File imageFile = new File(dataType.getImageName(beginFilePath));
			if (dataType == MapDataType.AREA && !imageFile.exists()) {
				Global.error("NO AREA DATA FOR " + beginFilePath);
			}

			imageMap.put(dataType, FileIO.readImage(imageFile));
		}

		BufferedImage backgroundMap = imageMap.get(MapDataType.BACKGROUND);
		dimension = new Dimension(backgroundMap.getWidth(), backgroundMap.getHeight());

		dataMap = new EnumMap<>(MapDataType.class);
		for (MapDataType dataType : MapDataType.values()) {
			BufferedImage image = imageMap.get(dataType);
			dataMap.put(dataType, image.getRGB(0, 0, dimension.width, dimension.height, null, 0, dimension.width));
		}

		entities = new ArrayList<>();
		triggers = new HashMap<>();
		mapEntrances = new HashMap<>();

		File f = new File(beginFilePath + ".txt");
		String fileText = FileIO.readEntireFileWithReplacements(f, false);

		MapDataMatcher mapDataMatcher = MapDataMatcher.matchArea(beginFilePath + ".txt", fileText);
		this.areaData = mapDataMatcher.getAreaData();

		entities.addAll(mapDataMatcher.getNPCs()
				.stream()
				.map(NPCMatcher::createEntity)
				.collect(Collectors.toList()));

		entities.addAll(mapDataMatcher.getItems()
				.stream()
				.map(ItemMatcher::createEntity)
				.collect(Collectors.toList()));

		entities.addAll(mapDataMatcher.getMiscEntities()
				.stream()
				.map(MiscEntityMatcher::createEntity)
				.collect(Collectors.toList()));

		for (MapTransitionMatcher matcher : mapDataMatcher.getMapTransitions()) {
            matcher.setMapName(this.name);

			Point entrance = matcher.getLocation();
			mapEntrances.put(matcher.getExitName(), getMapIndex(entrance.x, entrance.y));

            Point exit = matcher.getExitLocation();
			if (exit != null) {
				Trigger trigger = TriggerType.MAP_TRANSITION.createTrigger(JsonUtils.getJson(matcher), null);
				triggers.put(getMapIndex(exit.x, exit.y), trigger.getName());
			}
        }

		for (EventMatcher matcher : mapDataMatcher.getEvents()) {
			TriggerData triggerData = new TriggerData(matcher);
			Trigger trigger = EntityAction.addActionGroupTrigger(triggerData.name, triggerData.name, matcher.getActions());

			for (Point point : matcher.getLocation()) {
				triggers.put(getMapIndex(point.x, point.y), trigger.getName());
			}
		}

		for (WildBattleMatcher matcher : mapDataMatcher.getWildBattles()) {
			Trigger trigger = TriggerType.WILD_BATTLE.createTrigger(JsonUtils.getJson(matcher), null);
			for (Point point : matcher.getLocation()) {
				triggers.put(getMapIndex(point.x, point.y), trigger.getName());
			}
		}
	}

	// TODO: move this to its own file
	public enum WalkType {
		WATER(0x0000FF), 
		WALKABLE(0xFFFFFF), 
		NOT_WALKABLE(0x000000), 
		HOP_DOWN(0x00FF00), 
		HOP_UP(0xFF0000), 
		HOP_LEFT(0xFFFF00), 
		HOP_RIGHT(0x00FFFF),
		STAIRS_UP_RIGHT(0xFF00FF),
		STAIRS_UP_LEFT(0xFFC800);
		
		private final int value;
		
		WalkType(int v) {
			value = v;
		}
	}

	private int getMapIndex(int x, int y) {
		return Point.getIndex(x, y, getDimension().width);
	}

	public Dimension getDimension() {
		return this.dimension;
	}

	public boolean inBounds(Point location) {
		return location.inBounds(dimension);
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

	public WalkType getPassValue(int x, int y) {
		int rgb = getRGB(x, y, MapDataType.MOVE);
		if (rgb == TileSet.INVALID_RGB) {
			return WalkType.NOT_WALKABLE;
		}

		// TODO: SRSLY WHAT IS GOING ON
		int val = rgb&((1<<24) - 1);
		for (WalkType walkType: WalkType.values()) {
			if (walkType.value == val) {
				return walkType;
			}
		}

		return WalkType.NOT_WALKABLE;
	}

	public AreaData getArea(int x, int y) {
		if (areaData.length == 1) {
			return areaData[0];
		}

		int areaColor = getRGB(x, y, MapDataType.AREA);
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

	// TODO: Rename
	public String trigger() {
		int val = Game.getPlayer().getLocation().getIndex(dimension.width);
		if (triggers.containsKey(val)) {
			return triggers.get(val);
		}
	
		return null;
	}
	
	public boolean setCharacterToEntrance(String entranceName) {
        if (mapEntrances.containsKey(entranceName)) {
			int entrance = mapEntrances.get(entranceName);
			Point entranceLocation = Point.getPointAtIndex(entrance, dimension.width);
			Game.getPlayer().setLocation(entranceLocation);

			return true;
		}

		return false;
	}

	// TODO: Look at how this is being used, does a new array need to be created each time? does it need to be an array at all
	public Entity[][] populateEntities() {
		Entity[][] res = new Entity[dimension.width][dimension.height];
		entities.stream()
				.filter(Entity::isPresent)
				.forEach(entity -> {
					entity.reset();
					entity.addData();
					res[entity.getX()][entity.getY()] = entity;
				});
		
		return res;
	}
}
