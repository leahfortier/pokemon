package mapMaker;

import main.Global;
import mapMaker.dialogs.EventTriggerDialog;
import mapMaker.dialogs.ItemEntityDialog;
import mapMaker.dialogs.MapTransitionDialog;
import mapMaker.dialogs.NPCEntityDialog;
import mapMaker.dialogs.TriggerDialog;
import mapMaker.dialogs.WildBattleTriggerEditDialog;
import mapMaker.dialogs.WildBattleTriggerOptionsDialog;
import mapMaker.model.TileModel.TileType;
import mapMaker.model.TriggerModel;
import mapMaker.model.TriggerModel.TriggerModelType;
import namesies.ItemNamesies;
import pattern.map.AreaMatcher;
import pattern.map.EventMatcher;
import pattern.map.ItemMatcher;
import pattern.map.MapDataMatcher;
import pattern.generic.LocationTriggerMatcher;
import pattern.map.MapTransitionMatcher;
import pattern.generic.MultiPointTriggerMatcher;
import pattern.map.NPCMatcher;
import pattern.generic.SinglePointTriggerMatcher;
import pattern.map.WildBattleMatcher;
import util.DrawUtils;
import util.FileIO;
import util.JsonUtils;
import util.Point;
import util.PokeString;
import util.StringUtils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MapMakerTriggerData {

	private Set<AreaMatcher> areaData;
	private Set<LocationTriggerMatcher> entities;

	// Have the triggers been saved or have they been edited?
	private boolean triggersSaved;

	private MapMaker mapMaker;

	MapMakerTriggerData(MapMaker mapMaker) {
		initialize(mapMaker);

		// Force creation of mapName.txt file
		triggersSaved = false;
	}

	MapMakerTriggerData(MapMaker mapMaker, String mapTriggerFileName) {
		initialize(mapMaker);

		String fileText = FileIO.readEntireFileWithoutReplacements(mapTriggerFileName, false);
		MapDataMatcher mapDataMatcher = MapDataMatcher.matchArea(mapTriggerFileName, fileText);
		this.areaData = new HashSet<>(mapDataMatcher.getAreas());
		this.entities = new HashSet<>(mapDataMatcher.getAllEntities());

		triggersSaved = true;
	}

	private void initialize(MapMaker mapMaker) {
		this.mapMaker = mapMaker;

		this.areaData = new HashSet<>();
		this.entities = new HashSet<>();
	}

	boolean isSaved() {
		return triggersSaved;
	}

	void saveTriggers(String mapFileName) {
		if (triggersSaved) {
			return;
		}

		triggersSaved = true;

		Set<String> entityNames = new HashSet<>();
		entities.forEach(matcher -> getUniqueEntityName(matcher, entityNames));

		MapDataMatcher mapDataMatcher = new MapDataMatcher(
				areaData,
				entities
		);

		FileIO.createFile(mapFileName);
		FileIO.overwriteFile(mapFileName, new StringBuilder(JsonUtils.getJson(mapDataMatcher)));
	}

	private String getUniqueEntityName(LocationTriggerMatcher matcher, Set<String> entityNames) {
		TriggerModelType type = matcher.getTriggerModelType();
		String basicEntityName = matcher.getBasicName();

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
		matcher.setTriggerName(uniqueEntityName);
		entityNames.add(uniqueEntityName);

		return uniqueEntityName;
	}

	void moveTriggerData(Point delta) {
		for (LocationTriggerMatcher matcher : this.entities) {
			matcher.addDelta(delta);
		}

		triggersSaved = false;
	}

	void drawTriggers(Graphics2D g2d, Point mapLocation) {
		for (LocationTriggerMatcher entity : this.entities) {
			TriggerModelType triggerModelType = entity.getTriggerModelType();

			if (entity instanceof SinglePointTriggerMatcher) {
				Point point = ((SinglePointTriggerMatcher)entity).getLocation();

				BufferedImage image = null;
				switch (triggerModelType) {
					case MAP_TRANSITION:
						Point exit = ((MapTransitionMatcher)entity).getExitLocation();
						if (exit != null) {
							BufferedImage exitImage = TriggerModel.getMapExitImage(mapMaker);
							DrawUtils.drawTileImage(g2d, exitImage, exit, mapLocation);
						}
						break;
					case NPC:
						NPCMatcher npc = (NPCMatcher) entity;
						// TODO: This should be in a method
						image = mapMaker.getTileFromSet(TileType.TRAINER, 12 * npc.spriteIndex + 1 + npc.direction.ordinal());
						break;
				}

				if (image == null) {
					image = triggerModelType.getImage(mapMaker);
				}

				DrawUtils.drawTileImage(g2d, image, point, mapLocation);
			} else if (entity instanceof MultiPointTriggerMatcher) {
				List<Point> entityLocation = ((MultiPointTriggerMatcher) entity).getLocation();
				for (Point point : entityLocation) {
					BufferedImage image = triggerModelType.getImage(mapMaker);
					DrawUtils.drawTileImage(g2d, image, point, mapLocation);
				}
			} else {
				Global.error("Unknown entity matcher class " + entity.getClass().getSimpleName());
			}
		}
	}

	void placeTrigger(Point location) {

		// TODO: Ask user if they would like to place over
		LocationTriggerMatcher placeableTrigger = mapMaker.getPlaceableTrigger();
		placeableTrigger.addPoint(location);
		this.entities.add(placeableTrigger);
		System.out.println("Entity placed at (" + location.x + ", " + location.y + ").");

		triggersSaved = false;
	}

	boolean createTrigger(TriggerModelType type) {
		mapMaker.clearPlaceableTrigger();

		LocationTriggerMatcher trigger = null;

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
				trigger = editEventTrigger(null);
				break;
			case WILD_BATTLE:
				System.out.println("Wild Battle");
				trigger = wildBattleTriggerOptions();
				break;
			case MAP_TRANSITION:
				System.out.println("Map Transition");
				trigger = editMapTransition(null);
				break;
			case EVENT:
				System.out.println("Event");
				trigger = editEventTrigger(null);
				break;
		}

		mapMaker.setPlaceableTrigger(trigger);

		return trigger != null;
	}

	public void editTrigger(LocationTriggerMatcher trigger) {
		LocationTriggerMatcher newTrigger = null;
		TriggerModelType triggerModelType = trigger.getTriggerModelType();

		switch (triggerModelType) {
			case ITEM:
				newTrigger = editItem((ItemMatcher)trigger);
				break;
			case NPC:
				newTrigger = editNPC((NPCMatcher)trigger);
				break;
			case TRIGGER_ENTITY:
				// TODO: Need a new edit and dialog
				newTrigger = editEventTrigger((EventMatcher)trigger);
				break;
			case MAP_TRANSITION:
				newTrigger = editMapTransition((MapTransitionMatcher)trigger);
				break;
			case WILD_BATTLE:
				newTrigger = editWildBattleTrigger((WildBattleMatcher)trigger);
				break;
			case EVENT:
				newTrigger = editEventTrigger((EventMatcher)trigger);
				break;
		}

		// Update entity list
		if (newTrigger != null) {
			this.entities.remove(trigger);
			this.entities.add(newTrigger);

			newTrigger.setLocation(trigger);

			triggersSaved = false;
		}
	}

	public List<LocationTriggerMatcher> getEntitiesAtLocation(Point location) {
		return this.entities
				.stream()
				.filter(entity -> entity.isAtLocation(location))
				.collect(Collectors.toList());
	}

	public void removeTrigger(LocationTriggerMatcher trigger) {
		this.entities.removeIf(matcher -> trigger == matcher);

		triggersSaved = false;
	}

	public void moveTrigger(LocationTriggerMatcher trigger) {
		removeTrigger(trigger);
		mapMaker.setPlaceableTrigger(trigger);
	}

	private boolean dialogOption(String name, TriggerDialog dialog) {
		return dialog.giveOption(name, mapMaker);
	}

	private String getEntityNameFormat(String baseName) {
		if (StringUtils.isNullOrEmpty(baseName)) {
			baseName = "Nameless";
		}

		baseName = baseName.replaceAll("\\s", "");
		baseName = PokeString.removeSpecialSymbols(baseName);
		return baseName;
	}

	private ItemMatcher editItem(ItemMatcher item) {
		ItemEntityDialog itemDialog = new ItemEntityDialog(mapMaker);
		itemDialog.loadMatcher(item);

		if (!dialogOption("Item Editor", itemDialog)) {
			return null;
		}

		ItemNamesies itemType = itemDialog.getItemName();
		if (itemType == null) {
			return null;
		}

		return itemDialog.getMatcher();
	}

	private NPCMatcher editNPC(NPCMatcher npcData) {
		NPCEntityDialog npcDialog = new NPCEntityDialog(mapMaker);
		npcDialog.loadMatcher(npcData);

		if (!dialogOption("NPC Editor", npcDialog)) {
			return null;
		}

		return npcDialog.getMatcher();
	}

	private List<WildBattleMatcher> getWildBattleTriggers() {
		return this.entities
				.stream()
				.filter(entity -> entity instanceof WildBattleMatcher)
				.map(entity -> (WildBattleMatcher)entity)
				.collect(Collectors.toList());
	}

	private WildBattleMatcher wildBattleTriggerOptions() {
		WildBattleTriggerOptionsDialog wildBattleTriggerOptions = new WildBattleTriggerOptionsDialog();
		wildBattleTriggerOptions.loadMatcher(this.getWildBattleTriggers());

		if (!dialogOption("Wild Battle Trigger Options", wildBattleTriggerOptions)) {
			return null;
		}

		List<WildBattleMatcher> matcher = wildBattleTriggerOptions.getMatcher();
		if (matcher == null || matcher.isEmpty()) {
			return null;
		}

		// TODO: Wild battles need to be handled differently
		return matcher.get(0);
	}

	private WildBattleMatcher editWildBattleTrigger(WildBattleMatcher wildBattleTrigger) {
		WildBattleTriggerEditDialog dialog = new WildBattleTriggerEditDialog();
		dialog.loadMatcher(wildBattleTrigger);

		if (!dialogOption("Wild Battle Trigger Editor", dialog)) {
			return null;
		}

		return dialog.getMatcher();
	}

	private MapTransitionMatcher editMapTransition(MapTransitionMatcher transitionMatcher) {
		MapTransitionDialog mapTransitionDialog = new MapTransitionDialog(mapMaker, this);
		if (transitionMatcher != null) {
			mapTransitionDialog.setMapTransition(transitionMatcher);
		}

		if (!dialogOption("Map Transition Editor", mapTransitionDialog)) {
			return null;
		}

		return mapTransitionDialog.getMatcher();
	}

	private EventMatcher editEventTrigger(EventMatcher eventMatcher) {
		EventTriggerDialog eventTriggerDialog = new EventTriggerDialog();
		eventTriggerDialog.loadMatcher(eventMatcher);

		if (!dialogOption("Event Trigger Editor", eventTriggerDialog)) {
			return null;
		}

		// TODO: confirm at least one action first

		return eventTriggerDialog.getMatcher();
	}
}
