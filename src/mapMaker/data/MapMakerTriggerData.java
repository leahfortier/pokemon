package mapMaker.data;

import item.Item;
import main.Global;
import map.triggers.DialogueTrigger;
import map.triggers.MapTransitionTrigger;
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
import mapMaker.model.TileModel.TileType;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.AreaDataMatcher;
import pattern.AreaDataMatcher.AreaMatcher;
import pattern.AreaDataMatcher.EntityMatcher;
import pattern.AreaDataMatcher.ItemMatcher;
import pattern.AreaDataMatcher.MapExitMatcher;
import pattern.AreaDataMatcher.NPCMatcher;
import pattern.AreaDataMatcher.TriggerMatcher;
import util.DrawUtils;
import util.FileIO;
import util.Point;
import util.PokeString;
import util.StringUtils;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
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

	// Have the triggers been saved or have they been edited?
	private boolean triggersSaved;

	private MapMaker mapMaker;

	public MapMakerTriggerData(MapMaker mapMaker) {
		initialize(mapMaker);

		// Force creation of mapName.txt file
		triggersSaved = false;
	}

	public MapMakerTriggerData(MapMaker mapMaker, String mapTriggerFileName) {
		initialize(mapMaker);

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

	private void initialize(MapMaker mapMaker) {
		this.mapMaker = mapMaker;

		this.areaData = new ArrayList<>();
		this.entities = new ArrayList<>();
		this.triggerData = new ArrayList<>();

		this.entityNames = new HashSet<>();
		this.mapTransitionTriggers = new HashMap<>();
		this.wildBattleTriggers = new HashMap<>();

		if (allMapEntrances == null) {
			allMapEntrances = new HashMap<>();
			allMapEntrances.put(mapMaker.getCurrentMapName(), new HashSet<>());
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

		FileIO.createFile(mapFileName);
		FileIO.overwriteFile(mapFileName, new StringBuilder(AreaDataMatcher.getJson(areaDataMatcher)));
	}

	public void moveTriggerData(Point delta, int newWidth) {
		triggersSaved = false;

		for (TriggerMatcher matcher : this.triggerData) {
			for (Point point : matcher.getLocation()) {
				point.add(delta);
			}
		}

		for (EntityMatcher matcher : this.entities) {
			for (Point point : matcher.getLocation()) {
				point.add(delta);
			}
		}
	}

	public void drawTriggers(Graphics2D g2d, Point mapLocation) {
		for (TriggerMatcher data : this.triggerData) {
			for (Point point : data.getLocation()) {
				DrawUtils.outlineTileRed(g2d, point, mapLocation);

				if (data.isWildBattleTrigger()) {
					BufferedImage image = TriggerModelType.WILD_BATTLE.getImage(mapMaker);
					DrawUtils.drawTileImage(g2d, image, point, mapLocation);
				}
			}
		}

		for (EntityMatcher entity : this.entities) {
			for (Point point : entity.getLocation()) {
				final BufferedImage image;
				if (entity instanceof MapExitMatcher) {
					image = TriggerModelType.MAP_ENTRANCE.getImage(mapMaker);

					for (Point exit : ((MapExitMatcher) entity).getExits()) {
						BufferedImage exitImage = TriggerModelType.MAP_EXIT.getImage(mapMaker);
						DrawUtils.drawTileImage(g2d, exitImage, exit, mapLocation);
					}
				} else if (entity instanceof NPCMatcher) {
					NPCMatcher npc = (NPCMatcher) entity;
					// TODO: This should be in a method
					image = mapMaker.getTileFromSet(TileType.TRAINER, 12 * npc.spriteIndex + 1 + npc.direction.ordinal());
				} else if (entity instanceof ItemMatcher) {
					image = TriggerModelType.ITEM.getImage(mapMaker);
				} else if (entity instanceof TriggerMatcher) {
					image = TriggerModelType.TRIGGER_ENTITY.getImage(mapMaker);
				} else {
					Global.error("Unknown entity matcher class " + entity.getClass().getSimpleName());
					continue;
				}

				DrawUtils.drawTileImage(g2d, image, point, mapLocation);
			}
		}
	}

	public Point getPointFromIndex(int locationIndex) {
		return Point.getPointAtIndex(locationIndex, this.mapMaker.getCurrentMapSize().width);
	}

	public int getMapIndex(int x, int y) {
		return Point.getIndex(x, y, this.mapMaker.getCurrentMapSize().width);
	}

	public int getMapIndex(Point location) {
		return getMapIndex(location.x, location.y);
	}

	// TODO: holy hell this method needs to be split
	public void placeTrigger(Point location) {

		// TODO: Ask user if they would like to place over

		// System.out.println("Place trigger " + placeableTrigger.name + " "
		// +placeableTrigger.triggerType);

		PlaceableTrigger placeableTrigger = mapMaker.getPlaceableTrigger();

		// TODO: these are all doing the same thing -- see if we can just remove the placeable trigger type altogether
		if (placeableTrigger.triggerType == PlaceableTriggerType.Entity) {
			EntityMatcher entity = placeableTrigger.entity;

			entity.addPoint(location);
			this.entities.add(entity);
			this.entityNames.add(entity.getName());

			System.out.println("Entity " + entity.getName() + " placed at (" + location.x + ", " + location.y + ").");
		}
		else if (placeableTrigger.triggerType == PlaceableTriggerType.MapEntrance) {
			// TODO: This
//			mapEntrances.put(value, placeableTrigger.name);
			addMapEntranceNameToMap(mapMaker.getCurrentMapName(), placeableTrigger.name);

		}
		else if (placeableTrigger.triggerType == PlaceableTriggerType.TriggerData) {
			placeableTrigger.triggerData.addPoint(location);
		}

		mapMaker.clearPlaceableTrigger();
		triggersSaved = false;
	}

	// Used for moving triggers
	public TriggerModelType getTriggerModelType(PlaceableTrigger trigger) {
		if (trigger.triggerType == PlaceableTriggerType.Entity) {
			if (trigger.entity instanceof ItemMatcher) {
				return TriggerModelType.ITEM;
			}

			if (trigger.entity instanceof NPCMatcher) {
				return TriggerModelType.NPC;
			}

			if (trigger.entity instanceof TriggerMatcher) {
				return TriggerModelType.TRIGGER_ENTITY;
			}
		}
		else if (trigger.triggerType == PlaceableTriggerType.TriggerData) {
			if (trigger.triggerData.isWildBattleTrigger()) {
				return TriggerModelType.WILD_BATTLE;
			}
			else {
				return TriggerModelType.GROUP;
			}
		}
		else if (trigger.triggerType == PlaceableTriggerType.MapEntrance) {
			return TriggerModelType.MAP_ENTRANCE;
		}

		return null;
	}

	public boolean createTrigger(TriggerModelType type) {
		mapMaker.clearPlaceableTrigger();

		PlaceableTrigger trigger = null;

		// TODO: Show popup asking user about specific trigger being placed.
		switch (type) {
			case ITEM:
				System.out.println("Item");
				trigger = editItem(null);
				break;
			case NPC:
				System.out.println("NPC");
				trigger = editNPC(null);
				break;
			case TRIGGER_ENTITY:
				System.out.println("Trigger Entity");
				trigger = editTriggerEntity(null);
				break;
			case WILD_BATTLE:
				System.out.println("Wild Battle");
				trigger = wildBattleTriggerOptions();
				break;
			case MAP_EXIT:
				System.out.println("Exit");
				trigger = editMapTransition(null);
				break;
			case MAP_ENTRANCE:
				System.out.println("Entrance");
				trigger = editMapEntrance(null);
				break;
			case EVENT:
				System.out.println("Dialogue");
				trigger = editDialogueTrigger(null);
				break;
		}

		mapMaker.setPlaceableTrigger(trigger);

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

	public List<EntityMatcher> getEntitiesAtLocation(Point location) {
		List<EntityMatcher> triggerList = new ArrayList<>();
		for (EntityMatcher entity : this.entities) {
			triggerList.addAll(
					entity.getLocation()
							.stream()
							.filter(point -> point.equals(location))
							.map(point -> entity)
							.collect(Collectors.toList())
			);
		}

		return triggerList;
	}

	public PlaceableTrigger[] getTrigger(Point location) {
		int value = getMapIndex(location);

		return getEntitiesAtLocation(location)
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
		mapMaker.setPlaceableTrigger(trigger);
		removeTrigger(trigger);
		System.out.println(mapMaker.getPlaceableTrigger().name);
	}


	private boolean dialogOption(String name, JComponent... inputs) {
		Object[] options = { "Done", "Cancel" };
		int results = JOptionPane.showOptionDialog(
				mapMaker,
				inputs,
				name,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null,
				options,
				options[0]
		);

		return results == JOptionPane.YES_OPTION;
	}

	private String getEntityNameFormat(String baseName) {
		if (StringUtils.isNullOrEmpty(baseName)) {
			baseName = "Nameless";
		}

		baseName = baseName.replaceAll("\\s", "");
		baseName = PokeString.removeSpecialSymbols(baseName);
		return baseName;
	}

	private String getUniqueEntityName(TriggerModelType type, String basicEntityName) {
		String typeName = getEntityNameFormat(type.getName());
		basicEntityName = getEntityNameFormat(basicEntityName);

		int number = 1;
		String uniqueEntityName;

		// Loop until valid name is created
		do {
			uniqueEntityName = String.format("%s_%s_%s_%02d",
					mapMaker.getCurrentMapName(), typeName, basicEntityName, number++);
		} while (entityNames.contains(uniqueEntityName));

		System.out.println(uniqueEntityName);

		return uniqueEntityName;
	}

	private PlaceableTrigger editItem(ItemMatcher item) {
		ItemEntityDialog itemDialog = new ItemEntityDialog(mapMaker);
		if (item != null) {
			itemDialog.setItem(item);
		}

		if (!dialogOption("Item Editor", itemDialog)) {
			return null;
		}

		String itemType = itemDialog.getItemName();
		if (!Item.isItem(itemType)) {
			return null;
		}

		String itemEntityName = getUniqueEntityName(TriggerModelType.ITEM, itemType);
		return new PlaceableTrigger(itemDialog.getItem(itemEntityName));
	}

	private PlaceableTrigger editNPC(NPCMatcher npcData) {
		NPCEntityDialog npcDialog = new NPCEntityDialog(mapMaker);
		if (npcData != null) {
			// TODO: wtf
			npcDialog.setNPCData(npcData, npcData.name.replaceAll("^" + mapMaker.getCurrentMapName() + "_NPC_|_\\d + $", ""));
		}

		if (!dialogOption("NPC Editor", npcDialog)) {
			return null;
		}

		NPCMatcher newEntity = npcDialog.getNPC();
		newEntity.name = getUniqueEntityName(TriggerModelType.NPC, newEntity.name);

		return new PlaceableTrigger(newEntity);
	}

	private PlaceableTrigger editTriggerEntity(TriggerMatcher triggerEntity) {
		TriggerEntityDialog triggerDialog = new TriggerEntityDialog();
		if (triggerEntity != null) {
			triggerDialog.setTriggerEntity(triggerEntity, triggerEntity.name.replaceAll("^" + mapMaker.getCurrentMapName() + "_TriggerEntity_|_\\d + $", ""));
		}

		if (!dialogOption("Trigger Entity Editor", triggerDialog)) {
			return null;
		}

		TriggerMatcher newEntity = triggerDialog.getTriggerEntity();
		newEntity.name = getUniqueEntityName(TriggerModelType.TRIGGER_ENTITY, newEntity.name);

		return new PlaceableTrigger(newEntity);
	}

	private PlaceableTrigger wildBattleTriggerOptions() {
		WildBattleTriggerOptionsDialog wildBattleTriggerOptions = new WildBattleTriggerOptionsDialog(wildBattleTriggers, this);
		if (!dialogOption("Wild Battle Trigger Options", wildBattleTriggerOptions)) {
			return null;
		}

		if (wildBattleTriggerOptions.comboBox.getItemCount() == 0) {
			return null;
		}

		String wildBattleTriggerName = (String) wildBattleTriggerOptions.comboBox.getSelectedItem();
		TriggerMatcher td = wildBattleTriggers.get(wildBattleTriggerName);

		return new PlaceableTrigger(td);
	}

	private PlaceableTrigger editWildBattleTrigger(WildBattleTrigger wildBattleTrigger) {
		WildBattleTriggerEditDialog dialog = new WildBattleTriggerEditDialog();
		dialog.initialize(wildBattleTrigger);

		if (!dialogOption("Wild Battle Trigger Editor", dialog)) {
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

		if (!dialogOption("Map Transition Editor", mapTransitionDialog)) {
			return null;
		}

		String mapDestination = mapTransitionDialog.getDestination();
		if (mapDestination.isEmpty() || mapTransitionDialog.getMapEntrance().isEmpty()) {
			return null;
		}

		String mapTriggerName = getUniqueEntityName(TriggerModelType.MAP_ENTRANCE, mapDestination);
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

		// TODO: this is all wrong and I don't feel like fixing it right now
		if (dialogueTrigger != null) {
			String name = dialogueTrigger.getName();
			String prefix = mapMaker.getCurrentMapName() + "_DialogueTrigger_";

			// TODO: what is happening? what is -3?
			String actualName = name.substring(prefix.length(), name.length() - 3);
			System.out.println(actualName);

			dialogueTriggerDialog.setDialogueTrigger(dialogueTrigger, actualName);
		}

		if (!dialogOption("Event Trigger Editor", dialogueTriggerDialog)) {
			return null;
		}

		String eventTriggerName = dialogueTriggerDialog.getDialogueTriggerName();
		if (eventTriggerName.isEmpty()) {
			return null;
		}


		String triggerName = getUniqueEntityName(TriggerModelType.EVENT, eventTriggerName);
		TriggerMatcher td = dialogueTriggerDialog.getTriggerData(triggerName);

		return new PlaceableTrigger(td);
	}
}
