package mapMaker.data;

import item.Item;
import map.MapData;
import map.entity.EntityData;
import map.entity.ItemEntityData;
import map.entity.TriggerEntityData;
import map.entity.npc.NPCEntityData;
import map.triggers.DialogueTrigger;
import map.triggers.MapTransitionTrigger;
import map.triggers.TriggerData;
import map.triggers.TriggerType;
import map.triggers.WildBattleTrigger;
import mapMaker.MapMaker;
import mapMaker.dialogs.DialogueTriggerDialog;
import mapMaker.dialogs.ItemEntityDialog;
import mapMaker.dialogs.MapTransitionDialog;
import mapMaker.dialogs.NPCEntityDialog;
import mapMaker.dialogs.TransitionBuildingMainSelectDialog;
import mapMaker.dialogs.TransitionBuildingTransitionDialog;
import mapMaker.dialogs.TriggerEntityDialog;
import mapMaker.dialogs.WildBattleTriggerEditDialog;
import mapMaker.dialogs.WildBattleTriggerOptionsDialog;
import util.FileIO;
import util.PokeString;
import util.StringUtils;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MapMakerTriggerData {

	private static final Pattern blockPattern = Pattern.compile("((NPC|Item|Trigger|MapEntrance|TriggerData)\\s+)?(\\w+)\\s*\\{([^}]*)\\}");

	// Old trigger data
	private Map<Integer, String> triggers;

	// New trigger data
	private Map<Integer, TriggerData> triggerDataOnMap;

	// Entities
	// Holds a list of entities for every location. Some entities only appear
	// given a condition, allowing multiple entities to be at one location at a time
	private Map<Integer, ArrayList<EntityData>> entities;
	
	// Names of entities on map so no two entities have the same name
	private Set<String> entityNames;

	// Names of all the triggers placed on the map
	private Set<String> triggerNames;

	// Locations of the entrances for the map
	private Map<Integer, String> mapEntrances;

	// List of wild battle trigger data
	private Map<String, TriggerData> wildBattleTriggers;

	// List of map transition trigger data
	private Map<String, TriggerData> mapTransitionTriggers;

	// Data structure to hold all triggers not placed on the map
	private static MapTriggerData mapTriggers;

	// Data structure to hold pokemon center transitions
	private static PokeCenterTransitionData pokeCenterTransitionData;

	// Data structure to hold all the map entrance names for each map
	private static Map<String, Set<String>> allMapEntrances;

	private static TransitionBuildingData transitionBuildingData;

	// Currently selected item to be placed.
	// private EntityData placeableEntity;
	private PlaceableTrigger placeableTrigger;

	// Have the triggers been saved or have they been edited?
	private boolean triggersSaved;

	private String currentMapName;
	private Dimension currentMapSize;

	private MapMaker mapMaker;

	/**
	 * Create an empty collection of Triggers for the current map.
	 * 
	 * @param currentMapName
	 *            Name of the current map.
	 */
	public MapMakerTriggerData(String currentMapName, Dimension currentMapSize, MapMaker mapMaker) {
		initialize(currentMapName, currentMapSize, mapMaker);

		// Force creation of mapName.txt file
		triggersSaved = false;
	}

	/**
	 * Create and initialize triggers from file.
	 * 
	 * @param currentMapName
	 *            Name of the current map.
	 * @param mapTriggerFile
	 *            The file containing all the triggers for the current map.
	 * @param currentMapSize
	 *            The size of the current map.
	 */
	public MapMakerTriggerData(String currentMapName, Dimension currentMapSize, MapMaker mapMaker, File mapTriggerFile) {
		initialize(currentMapName, currentMapSize, mapMaker);

		String fileText = FileIO.readEntireFileWithoutReplacements(mapTriggerFile, false);

		Matcher m = blockPattern.matcher(fileText);
		while (m.find()) {
			String name = m.group(3);
			if (m.group(1) == null) { // trigger
				Scanner in = new Scanner(m.group(4));
				while (in.hasNext()) {
					String[] xr = in.next().split("-");
					String[] yr = in.next().split("-");
					
					int x1 = Integer.parseInt(xr[0]);
					int y1 = Integer.parseInt(yr[0]);
					int x2 = xr.length == 2 ? Integer.parseInt(xr[1]) : x1;
					int y2 = yr.length == 2 ? Integer.parseInt(yr[1]) : y1;
					
					for (int x = x1; x <= x2; x++) {
						for (int y = y1; y <= y2; y++) {
							triggers.put(convert(x, y), name);
						}
					}
				}
				
				in.close();
			}
			else {
				EntityData entity;
				switch (m.group(2)) {
					case "NPC":
						entity = new NPCEntityData(name, m.group(4));
						addEntityAtLocation(convert(entity.x, entity.y), entity);
						entityNames.add(name);
						break;
					case "Item":
						entity = new ItemEntityData(name, m.group(4));
						addEntityAtLocation(convert(entity.x, entity.y), entity);
						entityNames.add(name);
						break;
					case "Trigger":
						entity = new TriggerEntityData(name, m.group(4));
						addEntityAtLocation(convert(entity.x, entity.y), entity);
						entityNames.add(name);
						break;
					// Trigger Data and other items.
					case "MapEntrance":
						mapEntrances.put(MapData.getMapEntranceLocation(m.group(4), (int) currentMapSize.getWidth()), name);
						addMapEntranceNameToMap(currentMapName, name);
						break;
					case "TriggerData":
						triggerNames.add(name);

						TriggerData triggerData = new TriggerData(name, m.group(4));
						for (Integer loc : triggerData.getPoints((int) currentMapSize.getWidth())) {
							triggerDataOnMap.put(loc, triggerData);
						}

//						// TODO: Add each type of trigger data to specific data structure
//						if (triggerData.triggerType == TriggerType.WILD_BATTLE) {
//							wildBattleTriggers.put(triggerData.name, triggerData);
//						}
//
//						if (triggerData.triggerType == TriggerType.MAP_TRANSITION) {
//							mapTransitionTriggers.put(triggerData.name, triggerData);
//						}

						break;
				}
			}
		}

		triggersSaved = true;
	}

	private void initialize(String currentMapName, Dimension currentMapSize, MapMaker mapMaker) {
		this.currentMapName = currentMapName;
		this.currentMapSize = currentMapSize;
		this.mapMaker = mapMaker;

		triggers = new HashMap<>();
		triggerDataOnMap = new HashMap<>();

		entities = new HashMap<>();
		entityNames = new HashSet<>();

		mapEntrances = new HashMap<>();
		mapTransitionTriggers = new HashMap<>();

		wildBattleTriggers = new HashMap<>();

		triggerNames = new HashSet<>();

		placeableTrigger = null;

		mapTriggers = new MapTriggerData(mapMaker, currentMapName);

		if (pokeCenterTransitionData == null) {
			pokeCenterTransitionData = new PokeCenterTransitionData(mapMaker);
		}

		if (allMapEntrances == null) {
			allMapEntrances = new HashMap<>();
			allMapEntrances.put(currentMapName, new HashSet<>());
		}

		if (transitionBuildingData == null) {
			transitionBuildingData = new TransitionBuildingData(mapMaker);
		}
	}

	public void reload() {
		transitionBuildingData = new TransitionBuildingData(mapMaker);
	}

	private void readMapEntrancesForMap(String mapName) {
		if (!allMapEntrances.containsKey(mapName)) {

			Set<String> entranceNames = new HashSet<>();

			File mapTextFile = mapMaker.getMapTextFile(mapName);
			String fileText = FileIO.readEntireFileWithoutReplacements(mapTextFile, false);

			Matcher m = blockPattern.matcher(fileText);
			while (m.find()) {
				if (m.group(1) != null && m.group(2).equals("MapEntrance")) {
					String name = m.group(3);
					entranceNames.add(name);
				}
			}

			allMapEntrances.put(mapName, entranceNames);
		}
	}

	public String[] getMapEntrancesForMap(String mapName) {
		readMapEntrancesForMap(mapName);

		String[] names = new String[allMapEntrances.get(mapName).size()];
		return allMapEntrances.get(mapName).toArray(names);
	}

	private void addMapEntranceNameToMap(String mapName, String entranceName) {
		readMapEntrancesForMap(mapName);

		Set<String> entranceNames = allMapEntrances.get(mapName);
		entranceNames.add(entranceName);
	}

	private void removeMapEntranceNameFromMap(String mapName, String entranceName) {
		readMapEntrancesForMap(mapName);

		Set<String> entranceNames = allMapEntrances.get(mapName);
		entranceNames.remove(entranceName);
	}

	private void addEntityAtLocation(Integer location, EntityData entity) {
		if (!entities.containsKey(location)) {
			entities.put(location, new ArrayList<>());
		}
		
		entities.get(location).add(entity);
	}

	// TODO: lambda?
	private ArrayList<EntityData> getEntitiesAtLocation(Integer location) {
		if (!entities.containsKey(location)) {
			entities.put(location, new ArrayList<>());
		}
		
		return entities.get(location);
	}

	private EntityData getEntityAtLocationWithName(Integer location, String name) {
		ArrayList<EntityData> entitiesArrayList = getEntitiesAtLocation(location);

		for (EntityData entity : entitiesArrayList) {
			if (entity.name.equals(name)) {
				return entity;
			}
		}
		
		return null;
	}

	private EntityData removeEntityAtLocationWithName(Integer location, String name) {
		List<EntityData> entitiesArrayList = getEntitiesAtLocation(location);
		for (int currEntity = 0; currEntity < entitiesArrayList.size(); ++currEntity) {
			if (entitiesArrayList.get(currEntity).name.equals(name)) {
				return entitiesArrayList.remove(currEntity);
			}
		}
		
		return null;
	}

	public boolean isSaved() {
		return triggersSaved;
	}

	public void saveTriggers(File mapTriggerFile) {
		if (triggersSaved) {
			return;
		}

		triggersSaved = true;

		pokeCenterTransitionData.save();
		transitionBuildingData.save();

		mapTriggers.save();

		Map<String, List<Integer>> triggersByName = new HashMap<>();
		for (Integer location : triggers.keySet()) {
			String name = triggers.get(location);
			if (!triggersByName.containsKey(name)) {
				triggersByName.put(name, new LinkedList<>());
			}

			triggersByName.get(name).add(location);
		}

		// Put each value from triggerDataOnMap into triggerDataSet
		Set<TriggerData> triggerDataSet = new HashSet<>();
		triggerDataOnMap.values().stream()
				.filter(triggerData -> !triggerDataSet.contains(triggerData))
				.forEach(triggerDataSet::add);

		try {
			// TODO: FileIO
			// Create file if it doesn't exist
			if (!mapTriggerFile.exists()) {
				mapTriggerFile.getParentFile().mkdirs();
				mapTriggerFile.createNewFile();
			}

			FileWriter writer = new FileWriter(mapTriggerFile);

			// Save old format of location and trigger name
			for (String triggerName : triggersByName.keySet()) {
				List<Integer> list = triggersByName.get(triggerName);

				writer.write(triggerName + " {\n");

				for (Integer location : list) {
					int trigy = location / (int) currentMapSize.getWidth();
					int trigx = location - trigy * (int) currentMapSize.getWidth();

					writer.write("\t" + trigx + " " + trigy + "\n");
				}
				
				writer.write("}\n\n");
			}

			// Save all entities
			for (ArrayList<EntityData> entityList : entities.values()) {
				for (EntityData ed : entityList) {
					writer.write(ed.entityDataAsString() + "\n");
				}
			}

			// Save all map entrances
			for (Integer location : mapEntrances.keySet()) {
				String entrance = mapEntrances.get(location);
				
				int trigy = location / (int) currentMapSize.getWidth();
				int trigx = location - trigy * (int) currentMapSize.getWidth();

				writer.write("MapEntrance " + entrance + " {\n");
				writer.write("\tx: " + trigx + "\n");
				writer.write("\ty: " + trigy + "\n");
				writer.write("}\n\n");
			}

			// Save all trigger data items
			for (TriggerData td : triggerDataSet) {
//				writer.write(td.triggerDataAsString() + "\n");
			}

			writer.close();

		}
		catch (IOException ex) {
			ex.printStackTrace(); // TODO: Global.error
		}
	}

	public void moveTriggerData(int dx, int dy, Dimension newMapSize) {
		triggersSaved = false;

		Map<Integer, String> tempTriggers = new HashMap<>();

		for (Integer location : triggers.keySet()) {
			int trigy = location / (int) currentMapSize.getWidth();
			int trigx = location - trigy * (int) currentMapSize.getWidth();

			trigx += dx;
			trigy += dy;

			tempTriggers.put(trigy * (int) newMapSize.getWidth() + trigx, triggers.get(location));
		}

		triggers = tempTriggers;

		for (ArrayList<EntityData> entityList : entities.values()) {
			for (EntityData ed : entityList) {
				ed.x += dx;
				ed.y += dy;
			}
		}

		Map<Integer, String> tempMapEntrances = new HashMap<>();

		for (Integer location : mapEntrances.keySet()) {
			String entrance = mapEntrances.get(location);

			int trigy = location / (int) currentMapSize.getWidth();
			int trigx = location - trigy * (int) currentMapSize.getWidth();

			trigx += dx;
			trigy += dy;

			tempMapEntrances.put(trigy * (int) newMapSize.getWidth() + trigx, entrance);
		}

		mapEntrances = tempMapEntrances;

		Set<String> updatedTriggerData = new HashSet<>();
		Map<Integer, TriggerData> tempTriggerData = new HashMap<>();

//		triggerDataOnMap.values().stream()
//				.filter(triggerData -> !updatedTriggerData.contains(triggerData.name))
//				.forEach(triggerData -> {
//			updatedTriggerData.add(triggerData.name);
//			triggerData.updatePoints(dx, dy);

//			for (Integer loc : triggerData.getPoints((int)newMapSize.getWidth())) {
//				tempTriggerData.put(loc, triggerData);
//			}
//		});

		triggerDataOnMap = tempTriggerData;
		currentMapSize = newMapSize;
	}

	public void drawTriggers(Graphics2D g2d, int mapX, int mapY) {
		// Draw all old trigger types
		for (Integer location : triggers.keySet()) {
			int y = location / (int) currentMapSize.getWidth();
			int x = location - y * (int) currentMapSize.getWidth();

			g2d.setColor(Color.red);
			g2d.drawRect(x * MapMaker.tileSize + mapX, y * MapMaker.tileSize + mapY, MapMaker.tileSize, MapMaker.tileSize);
		}

		// Draw all map entrances
		for (Integer location : mapEntrances.keySet()) {
			int y = location / (int) currentMapSize.getWidth();
			int x = location - y * (int) currentMapSize.getWidth();

			BufferedImage img = mapMaker.getTileFromSet("MapMaker", 1);
			g2d.drawImage(img, (x * MapMaker.tileSize + mapX), (y * MapMaker.tileSize + mapY), null);
		}

		// Draw all trigger data
		for (Integer location : triggerDataOnMap.keySet()) {
			int y = location / (int) currentMapSize.getWidth();
			int x = location - y * (int) currentMapSize.getWidth();

			TriggerData triggerData = triggerDataOnMap.get(location);

			final BufferedImage image;
			switch (triggerData.triggerType) {
				case MAP_TRANSITION:
					// If pokecenter transition
					if (triggerData.triggerContents.contains("nextMap: PokeCenter")) {
						image = mapMaker.getTileFromSet("MapMaker", 5);
					} else if (triggerData.triggerContents.contains("nextMap: TransitionBuildingH")) {
						image = mapMaker.getTileFromSet("MapMaker", 6);
					} else if (triggerData.triggerContents.contains("nextMap: TransitionBuildingV")) {
						image = mapMaker.getTileFromSet("MapMaker", 7);
					} else {
						image = mapMaker.getTileFromSet("MapMaker", 2);
					}
					break;
				case WILD_BATTLE: {
					image = mapMaker.getTileFromSet("MapMaker", 3);
					break;
				}
				case DIALOGUE: {
					g2d.setColor(Color.RED);
					g2d.drawRect(x * MapMaker.tileSize + mapX, y * MapMaker.tileSize + mapY, MapMaker.tileSize, MapMaker.tileSize);

					image = mapMaker.getTileFromSet("MapMaker", 0xc);
					break;
				}
				default:
					g2d.setColor(Color.RED);
					g2d.drawRect(x * MapMaker.tileSize + mapX, y * MapMaker.tileSize + mapY, MapMaker.tileSize, MapMaker.tileSize);

					image = null;
					break;
			}

			if (image != null) {
				g2d.drawImage(image, (x * MapMaker.tileSize + mapX), (y * MapMaker.tileSize + mapY), null);
			}
		}

		// Draw all entities
		// Unnecessary to draw all entities at each location. Only first entity needed?
		for (List<EntityData> entityList : entities.values()) {
			for (EntityData ed : entityList) {
				// TODO: There should probably be an abstract method to do this in EntityData instead of having all these stupid instanceofs
				if (ed instanceof ItemEntityData) {
					ItemEntityData item = (ItemEntityData) ed;
					BufferedImage img = mapMaker.getTileFromSet("Trainer", 0);
					g2d.drawImage(img, (item.getX() * MapMaker.tileSize + mapX), (item.getY() * MapMaker.tileSize + mapY), null);
				}
				else if (ed instanceof NPCEntityData) {
					NPCEntityData npc = (NPCEntityData) ed;

					// TODO: This should be in a function
					BufferedImage img = mapMaker.getTileFromSet("Trainer", 12 * npc.spriteIndex + 1 + npc.defaultDirection.ordinal());
					g2d.drawImage(img, (npc.getX() * MapMaker.tileSize + mapX) - img.getWidth() / 2 + MapMaker.tileSize / 2, (npc.getY() * MapMaker.tileSize + mapY) - img.getHeight() + MapMaker.tileSize, null);
				}
				else if (ed instanceof TriggerEntityData) {
					TriggerEntityData trigData = (TriggerEntityData) ed;

					BufferedImage img = mapMaker.getTileFromSet("MapMaker", 4);
					g2d.drawImage(img, (trigData.getX() * MapMaker.tileSize + mapX), ((trigData.getY() + 1) * MapMaker.tileSize + mapY) - img.getHeight(), null);
				}
			}
		}

	}

	// TODO: This should really have a more specific name
	private Integer convert(int x, int y) {
		return y * (int) currentMapSize.getWidth() + x;
	}

	// TODO: holy hell this method needs to be split
	public void placeTrigger(int x, int y) {
		int value = convert(x, y);

		// TODO: Ask user if they would like to place over

		// System.out.println("Place trigger " + placeableTrigger.name + " "
		// +placeableTrigger.triggerType);

		if (placeableTrigger != null && placeableTrigger.triggerType == PlaceableTrigger.TriggerType.Entity) {
			EntityData entity = placeableTrigger.entity;

			entity.x = x;
			entity.y = y;

			addEntityAtLocation(value, entity);

			entityNames.add(entity.name);
			System.out.println("Entity " + entity.name + " placed at (" + entity.getX() + ", " + entity.getY() + ").");

			placeableTrigger = null;

			triggersSaved = false;
		}
		else if (placeableTrigger != null && placeableTrigger.triggerType == PlaceableTrigger.TriggerType.MapEntrance) {

			mapEntrances.put(value, placeableTrigger.name);
			addMapEntranceNameToMap(currentMapName, placeableTrigger.name);

			placeableTrigger = null;

			triggersSaved = false;
		}
		else if (placeableTrigger != null && placeableTrigger.triggerType == PlaceableTrigger.TriggerType.TriggerData) {

			triggersSaved = false;

			placeableTrigger.triggerData.addPoint(x, y);
			triggerDataOnMap.put(value, placeableTrigger.triggerData);

			switch (placeableTrigger.triggerData.triggerType) {
				case WILD_BATTLE:
					// TODO: What happens here?
					break;
				case MAP_TRANSITION:
					// If pokecenter transition
					if (placeableTrigger.triggerData.triggerContents.contains("nextMap: PokeCenter")) {
						String entranceName;
						Integer entranceLocation = convert(x, y + 1);

						if (mapEntrances.containsKey(entranceLocation)) {
							entranceName = mapEntrances.get(entranceLocation);
						} else {
							entranceName = "PokeCenter";
							int number = 1;
							while (mapEntrances.containsValue(String.format("%s%02d", entranceName, number))) {
								++number;
							}

							entranceName = String.format("%s%02d", entranceName, number);
							mapEntrances.put(entranceLocation, entranceName);
						}

						placeableTrigger.triggerData.triggerContents = placeableTrigger.triggerData.triggerContents.replace("@entranceName", entranceName);
						pokeCenterTransitionData.add(currentMapName, entranceName);
					}
					// If transition building transition
					else if (placeableTrigger.triggerData.triggerContents.contains("nextMap: TransitionBuilding")) {
						boolean isMap1 = true;

						if (currentMapName.equals(placeableTrigger.transitionBuildingPair.map2)
								&& placeableTrigger.transitionBuildingPair.map2Entrance == null) {
							isMap1 = false;
						}

						int number = 1;
						String mapEntranceName = "";

						int directionIndex = 0;
						if (!placeableTrigger.transitionBuildingPair.horizontal) {
							directionIndex = 2;
						}

						if (!isMap1) {
							++directionIndex;
						}

						String direction = TransitionBuildingData.directions[directionIndex];

						do {
							mapEntranceName = String.format("TransitionBuilding%s%sDoor%02d", (placeableTrigger.transitionBuildingPair.horizontal ? "H" : "V"), direction, number++);
						} while (mapEntrances.containsValue(mapEntranceName));

						// System.out.println(mapEntranceName);

						TransitionBuildingPair pair;

						if (placeableTrigger.transitionBuildingPair.map1Entrance == null
								&& placeableTrigger.transitionBuildingPair.map2Entrance == null) {

							// Create
							pair = transitionBuildingData.addIncompleteTransition(
									placeableTrigger.transitionBuildingPair.horizontal,
									placeableTrigger.transitionBuildingPair.map1,
									placeableTrigger.transitionBuildingPair.map2,
									isMap1,
									mapEntranceName);

							// Get pair number and replace in map transition trigger
							String mapTriggerName = currentMapName + "_to_" + pair.getPairName() + "_" + TransitionBuildingData.directions[directionIndex] + "Door";

//							placeableTrigger.triggerData.name = placeableTrigger.name = mapTriggerName;
							placeableTrigger.triggerData.triggerContents = placeableTrigger.triggerData.triggerContents.replace("@pairNumber", String.format("%02d", pair.pairNumber));
						} else {
							pair = transitionBuildingData.updateIncompleteTransition(placeableTrigger.transitionBuildingPair, mapEntranceName);
						}

						// Update map area
						int area = mapMaker.getTile(x, y, MapMaker.EditType.AREA_MAP);
						if (isMap1) {
							pair.area1 = area;
						} else {
							pair.area2 = area;
						}

						// TODO: This can probobbly be generalized
						if (placeableTrigger.transitionBuildingPair.horizontal) {
							placeableTrigger.triggerData.addPoint(x, y - 1);
							triggerDataOnMap.put(convert(x, y - 1), placeableTrigger.triggerData);

							Integer entranceLocation = convert(x + (isMap1 ? 1 : -1), y);

							mapEntrances.put(entranceLocation, mapEntranceName);
						} else {
							placeableTrigger.triggerData.addPoint(x - 1, y);
							triggerDataOnMap.put(convert(x - 1, y), placeableTrigger.triggerData);
							placeableTrigger.triggerData.addPoint(x + 1, y);
							triggerDataOnMap.put(convert(x + 1, y), placeableTrigger.triggerData);

							Integer entranceLocation = convert(x, y + (isMap1 ? -1 : 1));

							mapEntrances.put(entranceLocation, mapEntranceName);
						}
					}

					// Normal map transition Trigger
					// else {}

					mapTransitionTriggers.put(placeableTrigger.name, placeableTrigger.triggerData);
					break;
			}

//			triggerNames.add(placeableTrigger.triggerData.name);
		}
	}

	public void clearPlaceableTrigger() {
		placeableTrigger = null;
	}

	public boolean hasPlaceableTrigger() {
		return placeableTrigger != null;
	}

	public PlaceableTrigger getPlaceableTrigger() {
		return placeableTrigger;
	}

	// Used for moving triggers
	public int getTriggerTypeIndex(PlaceableTrigger trigger) {
		if (trigger.triggerType == PlaceableTrigger.TriggerType.Entity) {
			// TODO: What the fuck is this and why is it happening?
			if (trigger.entity instanceof ItemEntityData) {
				return 0;
			}

			if (trigger.entity instanceof NPCEntityData) {
				return 1;
			}

			if (trigger.entity instanceof TriggerEntityData) {
				return 2;
			}
		}
		else if (trigger.triggerType == PlaceableTrigger.TriggerType.TriggerData) {
			if (trigger.triggerData.triggerType == TriggerType.WILD_BATTLE) {
				return 3;
			}
			else if (trigger.triggerData.triggerType == TriggerType.MAP_TRANSITION) {
				// If pokecenter transition
				if (trigger.triggerData.triggerContents.contains("nextMap: PokeCenter")) {
					return 6;
				}
				else if (trigger.triggerData.triggerContents.contains("nextMap: TransitionBuilding")) {
					return 7;
				}
				else {
					return 4;
				}
			}
			// else if (trigger.triggerData.triggerType.equals("Group")) {
			// return 8;
			// }
			else if (trigger.triggerData.triggerType == TriggerType.DIALOGUE) {
				return 8;
			}
		}
		else if (trigger.triggerType == PlaceableTrigger.TriggerType.OldTrigger)
		{
			// TODO?
		}
		else if (trigger.triggerType == PlaceableTrigger.TriggerType.MapEntrance) {

			if (trigger.name.startsWith("TransitionBuilding")) {
				return 7;
			}
			else if (trigger.name.startsWith("PokeCenter")) {
				return 6;
			}

			// TODO: I swear to fucking god what is going on
			return 5;
		}

		return -1;
	}

	public boolean createTrigger(String type) {
		placeableTrigger = null;

		PlaceableTrigger trigger = null;

		// TODO: Show popup asking user about specific trigger being placed.
		switch (type) {
			// TODO: I don't think so get some fucking constants
			case "0":
				System.out.println("Item");
				trigger = editItem(null);
				break;
			case "1":
				System.out.println("NPC");
				trigger = editNPC(null);
				break;
			case "2":
				System.out.println("Trigger Entity");
				trigger = editTriggerEntity(null);
				break;
			case "3":
				System.out.println("Wild Battle");
				trigger = wildBattleTriggerOptions();
				break;
			case "4":
				System.out.println("Exit");
				trigger = editMapTransition(null);
				break;
			case "5":
				System.out.println("Entrance");
				trigger = editMapEntrance(null);
				break;
			case "6":
				System.out.println("PokeCenter");
				trigger = createPokeCenterTransition();
				break;
			case "7":
				System.out.println("Transition Building");
				trigger = transitionBuildingTransitionsOptions();
				break;
			case "8":
				System.out.println("Dialogue");
				trigger = editDialogueTrigger(null);
				break;
		}

		placeableTrigger = trigger;

		return trigger != null;
	}

	public void editTrigger(PlaceableTrigger trigger) {
		PlaceableTrigger newTrigger = null;

		if (trigger.triggerType == PlaceableTrigger.TriggerType.Entity) {
			entityNames.remove(trigger.entity.name);

			if (trigger.entity instanceof ItemEntityData) {
				newTrigger = editItem((ItemEntityData) trigger.entity);
			}
			else if (trigger.entity instanceof NPCEntityData) {
				newTrigger = editNPC((NPCEntityData) trigger.entity);
			}
			else if (trigger.entity instanceof TriggerEntityData) {
				newTrigger = editTriggerEntity((TriggerEntityData) trigger.entity);
			}

			// Update entity list
			if (newTrigger != null) {
				entityNames.add(newTrigger.entity.name);

				removeEntityAtLocationWithName(trigger.location, trigger.entity.name);
				addEntityAtLocation(trigger.location, newTrigger.entity);

				newTrigger.entity.x = trigger.entity.x;
				newTrigger.entity.y = trigger.entity.y;

				triggersSaved = false;
			}
			// Add entity name back to list
			else {
				entityNames.add(trigger.entity.name);
			}
		}
		else if (trigger.triggerType == PlaceableTrigger.TriggerType.TriggerData) {
//			triggerNames.remove(trigger.triggerData.name);

			if (trigger.triggerData.triggerType == TriggerType.MAP_TRANSITION) {
				// If pokecenter transition
				if (trigger.triggerData.triggerContents.contains("nextMap: PokeCenter")) {
					System.out.println("PokeCenter transition triggers cannot be edited.");
				}
				// TODO: Transition Buildings
				else if (trigger.triggerData.triggerContents.contains("nextMap: TransitionBuilding")) {
					System.out.println("Transition building transition triggers cannot be edited... yet");
				}
				else {
//					newTrigger = editMapTransition(new MapTransitionTrigger(trigger.name, trigger.triggerData.triggerContents));
				}
			}
			else if (trigger.triggerData.triggerType == TriggerType.WILD_BATTLE) {
//				newTrigger = editWildBattleTrigger(new WildBattleTrigger(trigger.name, trigger.triggerData.triggerContents));
			}
			else if (trigger.triggerData.triggerType == TriggerType.DIALOGUE) {
//				newTrigger = editDialogueTrigger(new DialogueTrigger(trigger.name, trigger.triggerData.triggerContents));
				// TODO
			}
			// else if (trigger.triggerData.triggerType.isEmpty()) { }

			if (newTrigger != null) {
//				triggerNames.add(newTrigger.triggerData.name);

				triggersSaved = false;

				// Loop through all of previous and replace with new
				renameTriggerData(trigger.triggerData, newTrigger.triggerData);
			}
			else {
//				triggerNames.add(trigger.triggerData.name);
			}
		}
		else if (trigger.triggerType == PlaceableTrigger.TriggerType.OldTrigger)
		{
			// Not planning on making editable
		}
		else if (trigger.triggerType == PlaceableTrigger.TriggerType.MapEntrance) {
			if (trigger.name.startsWith("TransitionBuilding") || trigger.name.startsWith("PokeCenter")) {
				System.out.println("This map entrance cannot be edited");
			}
			else {
				newTrigger = editMapEntrance(trigger.name);

				if (newTrigger != null)
				{
					mapEntrances.remove(trigger.name);
					mapEntrances.put(trigger.location, newTrigger.name);

					triggersSaved = false;
				}
			}
		}
	}

	public void renameTriggerData(TriggerData prev, TriggerData updated) {
		updated.points = prev.points;

		for (Integer loc : updated.getPoints((int) currentMapSize.getWidth())) {
			triggerDataOnMap.put(loc, updated);
		}

//		wildBattleTriggers.remove(prev.name);
//		wildBattleTriggers.put(updated.name, updated);
	}

	public PlaceableTrigger[] getTrigger(int x, int y) {
		int value = convert(x, y);

		List<PlaceableTrigger> triggersList = getEntitiesAtLocation(value).stream()
				.map(entityData -> new PlaceableTrigger(entityData, value))
				.collect(Collectors.toList());


		if (triggerDataOnMap.containsKey(value)) {
			triggersList.add(new PlaceableTrigger(triggerDataOnMap.get(value), value));
		}

		if (triggers.containsKey(value)) {
			triggersList.add(new PlaceableTrigger(PlaceableTrigger.TriggerType.OldTrigger, triggers.get(value), value));
		}

		if (mapEntrances.containsKey(value)) {
			triggersList.add(new PlaceableTrigger(PlaceableTrigger.TriggerType.MapEntrance, mapEntrances.get(value), value));
		}

		PlaceableTrigger[] triggersArray = new PlaceableTrigger[triggersList.size()];
		triggersList.toArray(triggersArray);
		return triggersArray;
	}

	public boolean hasTrigger(int x, int y) {
		int value = convert(x, y);
		return !entities.get(value).isEmpty()
				|| triggerDataOnMap.containsKey(value)
				|| triggers.containsKey(value)
				|| mapEntrances.containsKey(value);
	}

	public void removeTrigger(PlaceableTrigger trigger) {
		int y = trigger.location / (int) currentMapSize.getWidth();
		int x = trigger.location - y * (int) currentMapSize.getWidth();

		if (trigger.triggerType == PlaceableTrigger.TriggerType.Entity) {
			removeEntityAtLocationWithName(trigger.location, trigger.entity.name);
			entityNames.remove(trigger.entity.name);
		}
		else if (trigger.triggerType == PlaceableTrigger.TriggerType.TriggerData) {
			// TODO: Remove from specific data structures when no points left on map.
			if (trigger.triggerData.triggerType == TriggerType.MAP_TRANSITION) {

				// PokeCenter transition
				if (trigger.triggerData.triggerContents.contains("nextMap: PokeCenter")) {
					Integer trigLocation = convert(x, y + 1);
					String mapEntranceName = mapEntrances.remove(trigLocation);

					removeMapEntranceNameFromMap(currentMapName, mapEntranceName);

					pokeCenterTransitionData.remove(currentMapName, mapEntranceName);
				}

				// Transition buildings
				if (trigger.triggerData.triggerContents.contains("nextMap: TransitionBuilding")) {
					Matcher nameMatcher = TransitionBuildingData.transitionBuildingTransitionNamePattern.matcher(trigger.name);
					nameMatcher.find();

					String map1 = nameMatcher.group(2);
					String map2 = nameMatcher.group(3);

					boolean isMap1 = currentMapName.equals(map1);

					TransitionBuildingPair pair = transitionBuildingData.removeTransitionOnMap(nameMatcher.group(1).equals("H"), map1, map2, Integer.parseInt(nameMatcher.group(4)), isMap1);

					// Moving item, save to replace after removing from data
					// structure.
					if (placeableTrigger != null) {
						placeableTrigger.transitionBuildingPair = pair;
					}

					int[] locations = trigger.triggerData.getPoints(currentMapSize.width);
					trigger.triggerData.points.clear();

					Arrays.sort(locations);

					// Remove locations from map
					for (int currLocation : locations) {
						triggerDataOnMap.remove(currLocation);
					}

					int middleY = locations[1] / (int) currentMapSize.getWidth();
					int middleX = locations[1] - middleY * (int) currentMapSize.getWidth();

					// Remove map entrance
					if (pair.horizontal) {
						Integer trigLocation = convert(middleX + (isMap1 ? 1 : -1), middleY);
						removeMapEntranceNameFromMap(currentMapName, mapEntrances.remove(trigLocation));
					}
					else {
						Integer trigLocation = convert(middleX, middleY + (isMap1 ? -1 : 1));
						removeMapEntranceNameFromMap(currentMapName, mapEntrances.remove(trigLocation));
					}
				}

				trigger.triggerData.removePoint(x, y);

				if (trigger.triggerData.points.size() == 0) {
//					mapTransitionTriggers.remove(trigger.triggerData.name);
				}
			}
			else if (trigger.triggerData.triggerType == TriggerType.DIALOGUE) {
				// TODO
			}
			else if (trigger.triggerData.triggerType == TriggerType.WILD_BATTLE && trigger.triggerData.points.size() == 0) {
				// Don't remove completely. This will keep the trigger in the
				// options select menu.
				// wildBattleTriggers.remove(trigger.name);
			}

			// else if (trigger.triggerData.triggerType.isEmpty() &&
			// trigger.triggerData.points.size() == 0) {}

			triggerDataOnMap.remove(trigger.location);
			trigger.triggerData.removePoint(x, y);

			if (trigger.triggerData.points.size() == 0) {
//				triggerNames.remove(trigger.triggerData.name);
			}
		}
		else if (trigger.triggerType == PlaceableTrigger.TriggerType.OldTrigger) {
			triggers.remove(trigger.location);
		}
		else if (trigger.triggerType == PlaceableTrigger.TriggerType.MapEntrance) {
			if (trigger.name.startsWith("TransitionBuilding")) {

				// Remove all items
				// Check direction based on name (east/west...)
				for (int currDirection = 0; currDirection < TransitionBuildingData.directions.length; ++currDirection) {

					// Direction found
					if (trigger.name.contains(TransitionBuildingData.directions[currDirection])) {
						int dx = (currDirection < 2 ? currDirection * 2 - 1 : 0);
						int dy = (currDirection < 2 ? 0 : (currDirection - 2) * -2 + 1);

						System.out.println((x + dx) + " " + (y + dy));

						// Get the map transition for this transition building
						// transition group
						Integer location = convert(x + dx, y + dy);
						TriggerData td = triggerDataOnMap.get(location);

						PlaceableTrigger pt = new PlaceableTrigger(td, location);

						// If moving, save new trigger instead
						if (placeableTrigger != null) {
							placeableTrigger = pt;
						}

						// Remove transition building transition
						// Remove map entrance by removing all the transition building transitions.
						removeTrigger(pt);

						return;
					}
				}
			}
			else if (trigger.name.startsWith("PokeCenter")) {
				// If deleting an entrance used by a pokecenter transition
				Integer trigLocation = convert(x, y - 1);
				if (triggerDataOnMap.containsKey(trigLocation)
						&& triggerDataOnMap.get(trigLocation).triggerContents.contains("nextMap: PokeCenter")) {
					PlaceableTrigger pt = new PlaceableTrigger(triggerDataOnMap.get(trigLocation), trigLocation);
					if (placeableTrigger != null) {
						placeableTrigger = pt;
					}
					
					removeTrigger(pt);
				}
			}

			mapEntrances.remove(trigger.location);
			removeMapEntranceNameFromMap(currentMapName, trigger.name);
		}

		triggersSaved = false;
	}

	public void moveTrigger(PlaceableTrigger trigger) {
		placeableTrigger = trigger;
		removeTrigger(trigger);
		System.out.println(placeableTrigger.name);
	}

	private PlaceableTrigger editItem(ItemEntityData item) {
		ItemEntityDialog itemDialog = new ItemEntityDialog(mapMaker);
		if (item != null) {
			itemDialog.setItem(item);
		}

		JComponent[] inputs = new JComponent[] {itemDialog};
		Object[] options = {"Done", "Cancel"};

		int results = JOptionPane.showOptionDialog(
				mapMaker,
				inputs,
				"Item Editor",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null,
				options,
				options[0]
		);

		String itemType = itemDialog.getItemName();
		if (results == JOptionPane.CLOSED_OPTION
				|| results == 1 // TODO: What is results and why is it being compared like this? what does 1 represent?
				|| itemType.isEmpty()
				|| !Item.isItem(itemType)) {
			return null;
		}

		itemType = itemType.replace(' ', '_');

		// Loop until valid name is created.
		int number = 1;
		String itemEntityName = "";

		do {
			itemEntityName = String.format("%s_Item_%s_%02d", currentMapName, PokeString.removeSpecialSymbols(itemType), number++);
		} while (entityNames.contains(itemEntityName));

		System.out.println(itemEntityName);

		// TODO: ??
		item = new ItemEntityData(itemEntityName, "", PokeString.convertSpecialToUnicode(itemType), -1, -1);

		ItemEntityData newItem = itemDialog.getItem(itemEntityName);

		return new PlaceableTrigger(newItem);
	}

	private PlaceableTrigger editNPC(NPCEntityData npcData) {
		NPCEntityDialog npcDialog = new NPCEntityDialog(mapMaker);
		if (npcData != null) {
			npcDialog.setNPCData(npcData, npcData.name.replaceAll("^" + currentMapName + "_NPC_|_\\d + $", ""));
		}

		JComponent[] inputs = new JComponent[] { npcDialog };
		Object[] options = {"Done", "Cancel"};

		int results = JOptionPane.showOptionDialog(
				mapMaker,
				inputs,
				"NPC Editor",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null,
				options,
				options[0]);

		if (results == JOptionPane.CLOSED_OPTION || results == 1) {
			return null;
		}

		NPCEntityData newEntity = npcDialog.getNPC();

		// loop until valid name is created.
		int number = 1;
		
		newEntity.name = newEntity.name.isEmpty() ? "Nameless" : PokeString.removeSpecialCharacters(newEntity.name);

		// TODO: Should have a generic method to handle this naming criteria -- this loop is all over the fucking place
		String NPCName;
		do
		{
			NPCName = String.format("%s_NPC_%s_%02d", currentMapName, newEntity.name, number++);
		} while (entityNames.contains(NPCName));

		// System.out.println(NPCName);

		newEntity.name = NPCName;

		return new PlaceableTrigger(newEntity);
	}

	private PlaceableTrigger editTriggerEntity(TriggerEntityData triggerEntity) {
		TriggerEntityDialog triggerDialog = new TriggerEntityDialog();
		if (triggerEntity != null) {
			triggerDialog.setTriggerEntity(triggerEntity, triggerEntity.name.replaceAll("^" + currentMapName + "_TriggerEntity_|_\\d + $", ""));
		}

		JComponent[] inputs = new JComponent[] {triggerDialog};
		Object[] options = {"Done", "Cancel"};

		// TODO: Make a method for this
		int results = JOptionPane.showOptionDialog(
				mapMaker, inputs, "Trigger Entity Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

		if (results == JOptionPane.CLOSED_OPTION || results == 1) {
			return null;
		}

		TriggerEntityData newEntity = triggerDialog.getTriggerEntity();

		// loop until valid name is created.
		int number = 1;
		
		String triggerEntityName = "";
		newEntity.name = newEntity.name.isEmpty() ? "Nameless" : PokeString.removeSpecialCharacters(newEntity.name);
		
		do {
			triggerEntityName = String.format("%s_TriggerEntity_%s_%02d", currentMapName, newEntity.name, number++);
		} while (entityNames.contains(triggerEntityName));
		
		newEntity.name = triggerEntityName;

		return new PlaceableTrigger(newEntity);
	}

	private PlaceableTrigger wildBattleTriggerOptions() {
		WildBattleTriggerOptionsDialog wildBattleTriggerOptions = new WildBattleTriggerOptionsDialog(wildBattleTriggers, this);

		Object[] options = {"Place", "Cancel"};

		int results = JOptionPane.showOptionDialog(mapMaker, wildBattleTriggerOptions, "Wild Battle Trigger Options", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

		// TODO: Is this the same as that other one up there??
		if (results == JOptionPane.CLOSED_OPTION || results == 1 || (results == 0 && wildBattleTriggerOptions.comboBox.getItemCount() == 0)) {
			return null;
		}

		String wildBattleTriggerName = (String) wildBattleTriggerOptions.comboBox.getSelectedItem();
		TriggerData td = wildBattleTriggers.get(wildBattleTriggerName);

		// System.out.println(wildBattleTriggerName + " " + td.name + " " +
		// td.triggerType +" " + td.triggerContents);
		return new PlaceableTrigger(td);
	}

	private PlaceableTrigger editWildBattleTrigger(WildBattleTrigger wildBattleTrigger) {
		WildBattleTriggerEditDialog dialog = new WildBattleTriggerEditDialog();

		dialog.initialize(wildBattleTrigger);

		Object[] options = {"Done", "Cancel"};

		int results = JOptionPane.showOptionDialog(mapMaker, dialog, "Wild Battle Trigger Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

		if (results == JOptionPane.CLOSED_OPTION || results == 1) {
			return null;
		}

		TriggerData td = dialog.getTriggerData();

		// System.out.println(wildBattleTrigger.getName() +" " +td.name + " " +
		// td.triggerType +" " + td.triggerContents);

		return new PlaceableTrigger(td);
	}

	private PlaceableTrigger editMapTransition(MapTransitionTrigger mapTransitionTrigger) {
		MapTransitionDialog mapTransitionDialog = new MapTransitionDialog(mapMaker, this);

		if (mapTransitionTrigger != null) {
			mapTransitionDialog.setMapTransition(mapTransitionTrigger);
		}

		JComponent[] inputs = new JComponent[] {mapTransitionDialog};

		Object[] options = {"Done", "Cancel"};

		int results = JOptionPane.showOptionDialog(mapMaker, inputs, "Map Transition Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

		String mapDestination = mapTransitionDialog.getDestination();

		if (results == JOptionPane.CLOSED_OPTION || results == 1 || mapDestination.isEmpty() || mapTransitionDialog.getMapEntrance().isEmpty()) {
			return null;
		}

		// loop until valid name is created.
		int number = 1;
		String mapTriggerName = "";
		do {
			mapTriggerName = String.format("from_%s_to_%s_%02d", currentMapName, mapDestination, number++);
		} while (mapTransitionTriggers.containsKey(mapTriggerName));

		System.out.println(mapTriggerName);

		TriggerData td = mapTransitionDialog.getTriggerData(mapTriggerName);

		return new PlaceableTrigger(td);
	}

	private PlaceableTrigger editMapEntrance(String entrance) {
		String originalEntrance = entrance;

		entrance = JOptionPane.showInputDialog(mapMaker, "Please specify an entrance name:", entrance);
		if (entrance != null) {
			entrance = entrance.trim().replace(' ', '_');
		}

		// Do not allow entrances to begin with "TransitionBuilding" or "PokeCenter"
		while (!StringUtils.isNullOrEmpty(entrance) &&
				!entrance.equals(originalEntrance) &&
				(mapEntrances.containsValue(entrance) ||
						entrance.startsWith("PokeCenter") ||
						entrance.startsWith("TransitionBuilding")
				)) {
			if (entrance.startsWith("PokeCenter")) {
				entrance = JOptionPane.showInputDialog(mapMaker, "Map entrances cannot start with \"PokeCenter\".\nPlease specify a different entrance name:", entrance.replace('_', ' '));
			}
			else if (entrance.startsWith("TransitionBuilding")) {
				entrance = JOptionPane.showInputDialog(mapMaker, "Map entrances cannot start with \"TransitionBuilding\".\nPlease specify a different entrance name:", entrance.replace('_', ' '));
			}
			else if (mapEntrances.containsValue(entrance)) {
				entrance = JOptionPane.showInputDialog(mapMaker, "The entrance \"" + entrance + "\" already exist.\nPlease specify a different entrance name:", entrance.replace('_', ' '));
			}

			if (entrance != null) {
				entrance = entrance.trim().replace(' ', '_');
			}
		}

		if (StringUtils.isNullOrEmpty(entrance) || entrance.equals(originalEntrance)) {
			return null;
		}

		return new PlaceableTrigger(PlaceableTrigger.TriggerType.MapEntrance, entrance);
	}

	private PlaceableTrigger createPokeCenterTransition() {
		int number = 1;
		String mapTriggerName;
		do {
			mapTriggerName = String.format("from_%s_to_%s_%02d", currentMapName, "PokeCenter", number++);
		} while (mapTransitionTriggers.containsKey(mapTriggerName));

		TriggerData transition = new TriggerData(mapTriggerName, "MapTransition\n" + "\tglobal: MapGlobal_toPokeCenterFromEntrance_" + "@entranceName" + "\n" + "\tnextMap: PokeCenter\n" + "\tmapEntrance: " + "FrontDoor" + "\n");

		return new PlaceableTrigger(transition);
	}

	private PlaceableTrigger transitionBuildingTransitionsOptions() {
		TransitionBuildingPair[] pairs = transitionBuildingData.getIncompleteTransitionPairsForMap(currentMapName);

		String[] pairLabels = new String[pairs.length];

		for (int currPair = 0; currPair < pairs.length; ++currPair) {
			pairLabels[currPair] = "(" + (pairs[currPair].horizontal ? "H" : "V") + ") " + (currentMapName.equals(pairs[currPair].map2) ? pairs[currPair].map1 + (pairs[currPair].horizontal ? " to the East" : " to the North") : pairs[currPair].map2 + (pairs[currPair].horizontal ? " to the West" : " to the South"));
		}

		TransitionBuildingMainSelectDialog transitionBuildingMainSelectDialog = new TransitionBuildingMainSelectDialog(pairLabels);

		Object[] options = {"Create", "Place", "Cancel"};

		int results = JOptionPane.showOptionDialog(mapMaker, transitionBuildingMainSelectDialog, "Transition Building Options", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

		// System.out.println(results);

		if (results == JOptionPane.CLOSED_OPTION || results == 2) {
			return null;
		}

		// Place Button
		if (results == 1 && pairs.length > 0) {
			int index = transitionBuildingMainSelectDialog.getSelectedIndex();

			// System.out.println(index);

			int directionIndex = 0;
			if (!pairs[index].horizontal) {
				directionIndex = 2;
			}

			if (currentMapName.equals(pairs[index].map2)) {
				++directionIndex;
			}

			String mapTriggerName = currentMapName + "_to_" + pairs[index].getPairName() + "_" + TransitionBuildingData.directions[directionIndex] + "Door";

			String direction = TransitionBuildingData.directions[directionIndex];

			TriggerData td = new TriggerData(mapTriggerName, "MapTransition\n" + "\tglobal: MapGlobal_TransitionPair" + String.format("%02d", pairs[index].pairNumber) + "\n" + "\tnextMap: " + "TransitionBuilding" + (pairs[index].horizontal ? "H" : "V") + "\n" + "\tmapEntrance: " + direction + "Door" + "\n");

			PlaceableTrigger pt = new PlaceableTrigger(td);
			pt.transitionBuildingPair = pairs[index];

			return pt;
		}
		// Create button
		else if (results == 0) {
			return editTransitionBuildingTransition();
		}

		return null;
	}

	// TODO: Make editable for adding conditions for each transition
	private PlaceableTrigger editTransitionBuildingTransition() {
		TransitionBuildingTransitionDialog transitionBuildingTransitionDialog = new TransitionBuildingTransitionDialog(mapMaker, this, currentMapName);

		// if (mapTransitionTrigger != null)
		// {
		// mapTransitinoDialog.setMapTransition(mapTransitionTrigger);
		// }

		JComponent[] inputs = new JComponent[] {transitionBuildingTransitionDialog};

		Object[] options = {"Place", "Cancel"};

		int results = JOptionPane.showOptionDialog(mapMaker, inputs, "Transition Building Transition Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

		TransitionBuildingPair pair = transitionBuildingTransitionDialog.getTransitionPair();

		if (results == JOptionPane.CLOSED_OPTION || results == 1 || pair == null) {
			return null;
		}

		int directionIndex = 0;
		if (!pair.horizontal) {
			directionIndex = 2;
		}

		if (currentMapName.equals(pair.map2)) {
			++directionIndex;
		}

		String direction = TransitionBuildingData.directions[directionIndex];

		TriggerData td = new TriggerData(null, // mapTriggerName
				"MapTransition\n" + "\tglobal: MapGlobal_TransitionPair@pairNumber\n" + "\tnextMap: " + "TransitionBuilding" + (pair.horizontal ? "H" : "V") + "\n" + "\tmapEntrance: " + direction + "Door" + "\n");

		PlaceableTrigger pt = new PlaceableTrigger(td);
		pt.transitionBuildingPair = pair;

		return pt;
	}

	public int getPlaceableTriggerTransitionBuildingDirection() {
		boolean isMap1 = true;

		if (currentMapName.equals(placeableTrigger.transitionBuildingPair.map2)
				&& placeableTrigger.transitionBuildingPair.map2Entrance == null) {
			isMap1 = false;
		}

		if (placeableTrigger.transitionBuildingPair.horizontal) {
			return isMap1 ? 1 : 0;
		}
		else {
			return isMap1 ? 3 : 2;
		}
	}

	private PlaceableTrigger editDialogueTrigger(DialogueTrigger dialogueTrigger) {
		DialogueTriggerDialog dialogueTriggerDialog = new DialogueTriggerDialog();

		if (dialogueTrigger != null) {
//			String name = dialogueTrigger.getName();
			String prefix = currentMapName + "_DialogueTrigger_";

//			String actualName = name.substring(prefix.length(), name.length() - 3);
//			System.out.println(actualName);

//			dialogueTriggerDialog.setDialogueTrigger(dialogueTrigger, actualName);
		}

		JComponent[] inputs = new JComponent[] {dialogueTriggerDialog};

		Object[] options = {"Done", "Cancel"};

		int results = JOptionPane.showOptionDialog(mapMaker, inputs, "Dialogue Trigger Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

		String getName = dialogueTriggerDialog.getDialogueTriggerName();

		if (results == JOptionPane.CLOSED_OPTION || results == 1 || getName.isEmpty()) {
			return null;
		}

		// loop until valid name is created.
		int number = 1;
		String triggerName;
		do {
			triggerName = String.format("%s_DialogueTrigger_%s_%02d", currentMapName, getName, number++);
		} while (triggerNames.contains(triggerName) || mapTriggers.isTriggerNameTaken(triggerName));

		System.out.println(triggerName);

		TriggerData td = dialogueTriggerDialog.getTriggerData(triggerName);

		return new PlaceableTrigger(td);
	}
}
