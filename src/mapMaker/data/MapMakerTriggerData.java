package mapMaker.data;

import item.Item;
import main.Global;
import map.triggers.DialogueTrigger;
import map.triggers.MapTransitionTrigger;
import map.triggers.TriggerData.Point;
import map.triggers.WildBattleTrigger;
import mapMaker.MapMaker;
import mapMaker.data.PlaceableTrigger.PlaceableTriggerType;
import mapMaker.dialogs.DialogueTriggerDialog;
import mapMaker.dialogs.ItemEntityDialog;
import mapMaker.dialogs.MapTransitionDialog;
import mapMaker.dialogs.NPCEntityDialog;
import mapMaker.dialogs.TriggerEntityDialog;
import mapMaker.dialogs.WildBattleTriggerEditDialog;
import mapMaker.dialogs.WildBattleTriggerOptionsDialog;
import pattern.AreaDataMatcher;
import pattern.AreaDataMatcher.AreaMatcher;
import pattern.AreaDataMatcher.EntityMatcher;
import pattern.AreaDataMatcher.ItemMatcher;
import pattern.AreaDataMatcher.MapExitMatcher;
import pattern.AreaDataMatcher.NPCMatcher;
import pattern.AreaDataMatcher.TriggerMatcher;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MapMakerTriggerData {

	// Entities
	// Holds a list of entities for every location. Some entities only appear
	// given a condition, allowing multiple entities to be at one location at a time
	private List<AreaMatcher> areaData;
	private List<EntityMatcher> entities;
	private List<TriggerMatcher> triggerData;

	// Names of entities on map so no two entities have the same name
	private Set<String> entityNames;

	// List of wild battle trigger data
	private Map<String, TriggerMatcher> wildBattleTriggers;

	// List of map transition trigger data
	private Map<String, TriggerMatcher> mapTransitionTriggers;

	// Data structure to hold all the map entrance names for each map
	private static Map<String, Set<String>> allMapEntrances;

	// Currently selected item to be placed
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

	public MapMakerTriggerData(String currentMapName, Dimension currentMapSize, MapMaker mapMaker, String mapTriggerFileName) {
		initialize(currentMapName, currentMapSize, mapMaker);

		String fileText = FileIO.readEntireFileWithoutReplacements(mapTriggerFileName, false);
		AreaDataMatcher areaDataMatcher = AreaDataMatcher.matchArea(mapTriggerFileName, fileText);
		this.areaData = areaDataMatcher.getAreas();
		this.entities = areaDataMatcher.getEntities();
		this.triggerData = areaDataMatcher.getTriggerData();

		this.entityNames.addAll(this.entities.stream()
				.map(EntityMatcher::getName)
				.collect(Collectors.toList()));

		triggersSaved = true;
	}

	private void initialize(String currentMapName, Dimension currentMapSize, MapMaker mapMaker) {
		this.currentMapName = currentMapName;
		this.currentMapSize = currentMapSize;
		this.mapMaker = mapMaker;

		this.areaData = new ArrayList<>();
		this.entities = new ArrayList<>();
		this.triggerData = new ArrayList<>();

		this.entityNames = new HashSet<>();
		this.mapTransitionTriggers = new HashMap<>();
		this.wildBattleTriggers = new HashMap<>();

		this.placeableTrigger = null;

		if (allMapEntrances == null) {
			allMapEntrances = new HashMap<>();
			allMapEntrances.put(currentMapName, new HashSet<>());
		}
	}

	private void readMapEntrancesForMap(String mapName) {
		if (!allMapEntrances.containsKey(mapName)) {
			Set<String> entranceNames = this.entities.stream()
					.filter(matcher -> matcher instanceof MapExitMatcher)
					.map(EntityMatcher::getName)
					.collect(Collectors.toSet());

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

	public boolean isSaved() {
		return triggersSaved;
	}

	public void saveTriggers(String mapFileName) {
		if (triggersSaved) {
			return;
		}

		triggersSaved = true;

		AreaDataMatcher areaDataMatcher = new AreaDataMatcher(
				areaData,
				entities,
				triggerData
		);

		FileIO.overwriteFile(mapFileName, new StringBuilder(AreaDataMatcher.getJson(areaDataMatcher)));
	}

	public void moveTriggerData(int dx, int dy, Dimension newMapSize) {
		triggersSaved = false;

		for (TriggerMatcher matcher : this.triggerData) {
			for (Point point : matcher.getLocation()) {
				point.x += dx; // TODO: Make method for this inside the point class
				point.y += dy;
			}
		}

		for (EntityMatcher matcher : this.entities) {
			for (Point point : matcher.getLocation()) {
				point.x += dx;
				point.y += dy;
			}
		}

		currentMapSize = newMapSize;
	}

	public void drawTriggers(Graphics2D g2d, int mapX, int mapY) {
		for (TriggerMatcher data : this.triggerData) {
			for (Point point : data.getLocation()) {
				int x = point.x;
				int y = point.y;

				g2d.setColor(Color.RED);
				g2d.drawRect(x*MapMaker.tileSize + mapX, y*MapMaker.tileSize + mapY, MapMaker.tileSize, MapMaker.tileSize);

				if (data.isWildBattleTrigger()) {
					// TODO: btw for dialogue triggers it was using image 0xc and I removed it
					BufferedImage image = mapMaker.getMapMakerTile(3);
					g2d.drawImage(image, x*MapMaker.tileSize + mapX, y*MapMaker.tileSize + mapY, null);
				}
			}
		}

		for (EntityMatcher entity : this.entities) {
			for (Point point : entity.getLocation()) {
				int x = point.x;
				int y = point.y;

				final BufferedImage image;
				if (entity instanceof MapExitMatcher) {
					// TODO: I'm assuming this is some sort of tile representing the entrance AND IT NEEDS A FUCKING CONSTANT WTF
					image = mapMaker.getMapMakerTile(1);

					for (Point exit : ((MapExitMatcher)entity).getExits()) {
						BufferedImage exitImage = mapMaker.getMapMakerTile(2);
						g2d.drawImage(exitImage, exit.x*MapMaker.tileSize + mapX, exit.y*MapMaker.tileSize + mapY, null);
					}
				} else if (entity instanceof NPCMatcher) {
					NPCMatcher npc = (NPCMatcher)entity;
					// TODO: This should be in a method
					image = mapMaker.getTrainerTile(12*npc.spriteIndex + 1 + npc.direction.ordinal());
				} else if (entity instanceof ItemMatcher) {
					image = mapMaker.getTrainerTile(0);
				} else if (entity instanceof TriggerMatcher) {
					image = mapMaker.getMapMakerTile(4);
				} else {
					Global.error("Unknown entity matcher class " + entity.getClass().getSimpleName());
					continue;
				}

				g2d.drawImage(image, x*MapMaker.tileSize + mapX, y*MapMaker.tileSize + mapY, null);
			}
		}
	}

	// TODO: Combine this with the MapData one
	private Integer getMapIndex(int x, int y) {
		return x + y*(int) currentMapSize.getWidth();
	}

	public void placeTrigger(int x, int y) {
		if (placeableTrigger == null) {
			return;
		}

		int value = getMapIndex(x, y);

		// TODO: Ask user if they would like to place over

		// System.out.println("Place trigger " + placeableTrigger.name + " "
		// +placeableTrigger.triggerType);

		// TODO: these are all doing the same thing -- see if we can just remove the placeable trigger type altogether
		if (placeableTrigger.triggerType == PlaceableTriggerType.Entity) {
			EntityMatcher entity = placeableTrigger.entity;

			entity.addPoint(x, y);
			this.entities.add(entity);
			this.entityNames.add(entity.getName());

			System.out.println("Entity " + entity.getName() + " placed at (" + x + ", " + y + ").");
		}
		else if (placeableTrigger.triggerType == PlaceableTriggerType.MapEntrance) {
			// TODO: This
//			mapEntrances.put(value, placeableTrigger.name);
			addMapEntranceNameToMap(currentMapName, placeableTrigger.name);

		}
		else if (placeableTrigger.triggerType == PlaceableTriggerType.TriggerData) {
			placeableTrigger.triggerData.addPoint(x, y);
		}

		placeableTrigger = null;
		triggersSaved = false;
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
		if (trigger.triggerType == PlaceableTriggerType.Entity) {
			// TODO: What the fuck is this and why is it happening?
			if (trigger.entity instanceof ItemMatcher) {
				return 0;
			}

			if (trigger.entity instanceof NPCMatcher) {
				return 1;
			}

			if (trigger.entity instanceof TriggerMatcher) {
				return 2;
			}
		}
		else if (trigger.triggerType == PlaceableTriggerType.TriggerData) {
			if (trigger.triggerData.isWildBattleTrigger()) {
				return 3;
			}

			// TODO; I AM SO FUCKING CONFUSED
			return 8;
		}
		else if (trigger.triggerType == PlaceableTriggerType.OldTrigger)
		{
			// TODO?
		}
		else if (trigger.triggerType == PlaceableTriggerType.MapEntrance) {

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
				break;
			case "7":
				System.out.println("Transition Building");
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

		if (trigger.triggerType == PlaceableTriggerType.Entity) {
			EntityMatcher entity = trigger.entity;

			if (entity instanceof ItemMatcher) {
				newTrigger = editItem((ItemMatcher)entity);
			}
			else if (entity instanceof NPCMatcher) {
				newTrigger = editNPC((NPCMatcher)entity);
			}
			else if (entity instanceof TriggerMatcher) {
				newTrigger = editTriggerEntity((TriggerMatcher)entity);
			}

			// Update entity list
			if (newTrigger != null) {
				// TODO
//				removeEntityAtLocationWithName(trigger.location, trigger.entity.name);
//				addEntityAtLocation(trigger.location, newTrigger.entity);
//
//				newTrigger.entity.x = trigger.entity.x;
//				newTrigger.entity.y = trigger.entity.y;

				triggersSaved = false;
			}
			// Add entity name back to list
			else {
				entityNames.add(trigger.entity.getName());
			}
		}
//		else if (trigger.triggerType == PlaceableTriggerType.TriggerData) {
//			triggerNames.remove(trigger.triggerData.name);
//
//			if (trigger.triggerData.triggerType == TriggerType.MAP_TRANSITION) {
//				newTrigger = editMapTransition(new MapTransitionTrigger(trigger.name, trigger.triggerData.triggerContents));
//			}
//			else if (trigger.triggerData.triggerType == TriggerType.WILD_BATTLE) {
////				newTrigger = editWildBattleTrigger(new WildBattleTrigger(trigger.name, trigger.triggerData.triggerContents));
//			}
//			else if (trigger.triggerData.triggerType == TriggerType.DIALOGUE) {
////				newTrigger = editDialogueTrigger(new DialogueTrigger(trigger.name, trigger.triggerData.triggerContents));
//				// TODO
//			}
//
//			if (newTrigger != null) {
//				triggerNames.add(newTrigger.triggerData.name);
//
//				triggersSaved = false;
//
//				// Loop through all of previous and replace with new
//				renameTriggerData(trigger.triggerData, newTrigger.triggerData);
//			}
//			else {
//				triggerNames.add(trigger.triggerData.name);
//			}
//		}
//		else if (trigger.triggerType == PlaceableTriggerType.OldTrigger)
//		{
//			// Not planning on making editable
//		}
//		else if (trigger.triggerType == PlaceableTriggerType.MapEntrance) {
//			if (trigger.name.startsWith("TransitionBuilding") || trigger.name.startsWith("PokeCenter")) {
//				System.out.println("This map entrance cannot be edited");
//			}
//			else {
//				newTrigger = editMapEntrance(trigger.name);
//
//				if (newTrigger != null)
//				{
//					mapEntrances.remove(trigger.name);
//					mapEntrances.put(trigger.location, newTrigger.name);
//
//					triggersSaved = false;
//				}
//			}
//		}
	}

	public void renameTriggerData(TriggerMatcher prev, TriggerMatcher updated) {
//		updated.points = prev.points;
//
//		for (Integer loc : updated.getPoints((int) currentMapSize.getWidth())) {
//			triggerDataOnMap.put(loc, updated);
//		}
//
//		wildBattleTriggers.remove(prev.name);
//		wildBattleTriggers.put(updated.name, updated);
	}

	public List<EntityMatcher> getEntitiesAtLocation(int x, int y) {
		List<EntityMatcher> triggerList = new ArrayList<>();
		for (EntityMatcher entity : this.entities) {
			triggerList.addAll(
					entity.getLocation()
							.stream()
							.filter(point -> point.isAt(x, y))
							.map(point -> entity)
							.collect(Collectors.toList())
			);
		}

		return triggerList;
	}

	public PlaceableTrigger[] getTrigger(int x, int y) {
		int value = getMapIndex(x, y);

		return getEntitiesAtLocation(x, y)
				.stream()
				.map(entityData -> new PlaceableTrigger(entityData, value))
				.collect(Collectors.toList())
				.toArray(new PlaceableTrigger[0]);
	}

	// TODO
	public void removeTrigger(PlaceableTrigger trigger) {
//		int y = trigger.location / (int) currentMapSize.getWidth();
//		int x = trigger.location - y * (int) currentMapSize.getWidth();
//
//		if (trigger.triggerType == PlaceableTriggerType.Entity) {
//			removeEntityAtLocationWithName(trigger.location, trigger.entity.name);
//			entityNames.remove(trigger.entity.name);
//		}
//		else if (trigger.triggerType == PlaceableTriggerType.TriggerData) {
//			// TODO: Remove from specific data structures when no points left on map.
//			if (trigger.triggerData.triggerType == TriggerType.MAP_TRANSITION) {
//
//				trigger.triggerData.removePoint(x, y);
//
//				if (trigger.triggerData.location.size() == 0) {
//					mapTransitionTriggers.remove(trigger.triggerData.name);
//				}
//			}
//			else if (trigger.triggerData.triggerType == TriggerType.DIALOGUE) {
//				// TODO
//			}
//			else if (trigger.triggerData.triggerType == TriggerType.WILD_BATTLE && trigger.triggerData.location.size() == 0) {
//				// Don't remove completely. This will keep the trigger in the
//				// options select menu.
//				// wildBattleTriggers.remove(trigger.name);
//			}
//
//			// else if (trigger.triggerData.triggerType.isEmpty() &&
//			// trigger.triggerData.location.size() == 0) {}
//
//			triggerDataOnMap.remove(trigger.location);
//			trigger.triggerData.removePoint(x, y);
//
//			if (trigger.triggerData.location.size() == 0) {
//				triggerNames.remove(trigger.triggerData.name);
//			}
//		}
//		else if (trigger.triggerType == PlaceableTriggerType.OldTrigger) {
//			triggers.remove(trigger.location);
//		}
//		else if (trigger.triggerType == PlaceableTriggerType.MapEntrance) {
//			mapEntrances.remove(trigger.location);
//			removeMapEntranceNameFromMap(currentMapName, trigger.name);
//		}

		triggersSaved = false;
	}

	public void moveTrigger(PlaceableTrigger trigger) {
		placeableTrigger = trigger;
		removeTrigger(trigger);
		System.out.println(placeableTrigger.name);
	}

	private PlaceableTrigger editItem(ItemMatcher item) {
		ItemEntityDialog itemDialog = new ItemEntityDialog(mapMaker);
		if (item != null) {
			itemDialog.setItem(item);
		}

		JComponent[] inputs = new JComponent[] { itemDialog };
		Object[] options = { "Done", "Cancel" }; // TODO: This should be a constant I see it everywhere

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
		if (results != JOptionPane.YES_OPTION || !Item.isItem(itemType)) {
			return null;
		}

		itemType = itemType.replace(' ', '_');

		// TODO: Make a method for name creation
		// Loop until valid name is created.
		int number = 1;
		String itemEntityName = "";
		do {
			itemEntityName = String.format("%s_Item_%s_%02d", currentMapName, PokeString.removeSpecialSymbols(itemType), number++);
		} while (entityNames.contains(itemEntityName));

		System.out.println(itemEntityName);

		return new PlaceableTrigger(itemDialog.getItem(itemEntityName));
	}

	private PlaceableTrigger editNPC(NPCMatcher npcData) {
		NPCEntityDialog npcDialog = new NPCEntityDialog(mapMaker);
		if (npcData != null) {
			npcDialog.setNPCData(npcData, npcData.name.replaceAll("^" + currentMapName + "_NPC_|_\\d + $", ""));
		}

		JComponent[] inputs = new JComponent[] { npcDialog };
		Object[] options = { "Done", "Cancel" };

		int results = JOptionPane.showOptionDialog(
				mapMaker,
				inputs,
				"NPC Editor",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null,
				options,
				options[0]);

		if (results != JOptionPane.YES_OPTION) {
			return null;
		}

		NPCMatcher newEntity = npcDialog.getNPC();

		// loop until valid name is created.
		// TODO: Should have a generic method to handle this naming criteria -- this loop is all over the fucking place
		int number = 1;
		String baseName = StringUtils.isNullOrEmpty(newEntity.getName()) ? "Nameless" : PokeString.removeSpecialCharacters(newEntity.getName());
		String NPCName;
		do
		{
			NPCName = String.format("%s_NPC_%s_%02d", currentMapName, baseName, number++);
		} while (entityNames.contains(NPCName));

		newEntity.setName(NPCName);
		System.out.println(NPCName);

		return new PlaceableTrigger(newEntity);
	}

	// TODO: Combine these three methods they're the same
	private PlaceableTrigger editTriggerEntity(TriggerMatcher triggerEntity) {
		TriggerEntityDialog triggerDialog = new TriggerEntityDialog();
		if (triggerEntity != null) {
			triggerDialog.setTriggerEntity(triggerEntity, triggerEntity.name.replaceAll("^" + currentMapName + "_TriggerEntity_|_\\d + $", ""));
		}

		JComponent[] inputs = new JComponent[] { triggerDialog };
		Object[] options = { "Done", "Cancel" };

		// TODO: Make a method for this
		int results = JOptionPane.showOptionDialog(
				mapMaker, inputs, "Trigger Entity Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

		if (results != JOptionPane.YES_OPTION) {
			return null;
		}

		TriggerMatcher newEntity = triggerDialog.getTriggerEntity();

		// loop until valid name is created.
		int number = 1;
		String triggerEntityName;
		newEntity.name = newEntity.name.isEmpty() ? "Nameless" : PokeString.removeSpecialCharacters(newEntity.name);
		do {
			triggerEntityName = String.format("%s_TriggerEntity_%s_%02d", currentMapName, newEntity.name, number++);
		} while (entityNames.contains(triggerEntityName));
		
		newEntity.name = triggerEntityName;

		return new PlaceableTrigger(newEntity);
	}

	private PlaceableTrigger wildBattleTriggerOptions() {
		WildBattleTriggerOptionsDialog wildBattleTriggerOptions = new WildBattleTriggerOptionsDialog(wildBattleTriggers, this);

		Object[] options = { "Place", "Cancel" };

		// TODO: AHHHHHHHHH SRSLY
		int results = JOptionPane.showOptionDialog(mapMaker, wildBattleTriggerOptions, "Wild Battle Trigger Options", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (results != JOptionPane.YES_OPTION || wildBattleTriggerOptions.comboBox.getItemCount() == 0) {
			return null;
		}

		String wildBattleTriggerName = (String) wildBattleTriggerOptions.comboBox.getSelectedItem();
		TriggerMatcher td = wildBattleTriggers.get(wildBattleTriggerName);

		// System.out.println(wildBattleTriggerName + " " + td.name + " " +
		// td.triggerType +" " + td.triggerContents);
		return new PlaceableTrigger(td);
	}

	private PlaceableTrigger editWildBattleTrigger(WildBattleTrigger wildBattleTrigger) {
		WildBattleTriggerEditDialog dialog = new WildBattleTriggerEditDialog();

		dialog.initialize(wildBattleTrigger);

		Object[] options = {"Done", "Cancel"};

		int results = JOptionPane.showOptionDialog(mapMaker, dialog, "Wild Battle Trigger Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (results != JOptionPane.YES_OPTION) {
			return null;
		}

		TriggerMatcher td = dialog.getTriggerData();

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

		TriggerMatcher td = mapTransitionDialog.getTriggerData(mapTriggerName);

		return new PlaceableTrigger(td);
	}

	private PlaceableTrigger editMapEntrance(String entrance) {
		String originalEntrance = entrance;

		entrance = JOptionPane.showInputDialog(mapMaker, "Please specify an entrance name:", entrance);
		if (entrance != null) {
			entrance = entrance.trim().replace(' ', '_');
		}

		while (!StringUtils.isNullOrEmpty(entrance) &&
				!entrance.equals(originalEntrance)) {
//			if (mapEntrances.containsValue(entrance)) {
//				entrance = JOptionPane.showInputDialog(mapMaker, "The entrance \"" + entrance + "\" already exist.\nPlease specify a different entrance name:", entrance.replace('_', ' '));
//			}

			if (entrance != null) {
				entrance = entrance.trim().replace(' ', '_');
			}
		}

		if (StringUtils.isNullOrEmpty(entrance) || entrance.equals(originalEntrance)) {
			return null;
		}

		return new PlaceableTrigger(PlaceableTriggerType.MapEntrance, entrance);
	}

	private PlaceableTrigger editDialogueTrigger(DialogueTrigger dialogueTrigger) {
		DialogueTriggerDialog dialogueTriggerDialog = new DialogueTriggerDialog();

		if (dialogueTrigger != null) {
			String name = dialogueTrigger.getName();
			String prefix = currentMapName + "_DialogueTrigger_";

			// TODO: what is happening? what is -3?
			String actualName = name.substring(prefix.length(), name.length() - 3);
			System.out.println(actualName);

			dialogueTriggerDialog.setDialogueTrigger(dialogueTrigger, actualName);
		}

		JComponent[] inputs = new JComponent[] { dialogueTriggerDialog };
		Object[] options = { "Done", "Cancel" };

		int results = JOptionPane.showOptionDialog(mapMaker, inputs, "Dialogue Trigger Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

		String getName = dialogueTriggerDialog.getDialogueTriggerName();

		if (results == JOptionPane.CLOSED_OPTION || results == 1 || getName.isEmpty()) {
			return null;
		}

		// TODO: No
		// loop until valid name is created.
		int number = 1;
		String triggerName = "";
//		do {
//			triggerName = String.format("%s_DialogueTrigger_%s_%02d", currentMapName, getName, number++);
//		} while (triggerNames.contains(triggerName) || mapTriggers.isTriggerNameTaken(triggerName));

		System.out.println(triggerName);

		TriggerMatcher td = dialogueTriggerDialog.getTriggerData(triggerName);

		return new PlaceableTrigger(td);
	}
}
