package mapMaker.data;

import mapMaker.MapMaker;
import mapMaker.data.PlaceableTrigger.PlaceableTriggerType;
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
import pattern.AreaMatcher;
import pattern.EntityMatcher;
import pattern.EventMatcher;
import pattern.ItemMatcher;
import pattern.MapDataMatcher;
import pattern.MapMakerEntityMatcher;
import pattern.MapTransitionMatcher;
import pattern.MultiPointEntityMatcher;
import pattern.NPCMatcher;
import pattern.SinglePointEntityMatcher;
import pattern.WildBattleMatcher;
import util.DrawUtils;
import util.FileIO;
import util.JsonUtils;
import util.Point;
import util.PokeString;
import util.StringUtils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MapMakerTriggerData {

	// TODO: Merge trigger data and entities
	private Set<AreaMatcher> areaData;
	private Set<SinglePointEntityMatcher> entities;
	private Set<MultiPointEntityMatcher> triggerData;

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
		MapDataMatcher mapDataMatcher = MapDataMatcher.matchArea(mapTriggerFileName, fileText);
		this.areaData = new HashSet<>(mapDataMatcher.getAreas());
		this.entities = new HashSet<>(mapDataMatcher.getEntities());
		this.triggerData = new HashSet<>(mapDataMatcher.getEvents());

		triggersSaved = true;
	}

	private void initialize(MapMaker mapMaker) {
		this.mapMaker = mapMaker;

		this.areaData = new HashSet<>();
		this.entities = new HashSet<>();
		this.triggerData = new HashSet<>();
	}

	public boolean isSaved() {
		return triggersSaved;
	}

	public void saveTriggers(String mapFileName) {
		if (triggersSaved) {
			return;
		}

		triggersSaved = true;

		Set<String> entityNames = new HashSet<>();
		entities.forEach(matcher -> getUniqueEntityName(matcher, entityNames));
		triggerData.forEach(matcher -> getUniqueEntityName(matcher, entityNames));

		Set<MapMakerEntityMatcher> combinedSet = new HashSet<>();
		combinedSet.addAll(entities);
		combinedSet.addAll(triggerData);

		MapDataMatcher mapDataMatcher = new MapDataMatcher(
				areaData,
				combinedSet
		);

		FileIO.createFile(mapFileName);
		FileIO.overwriteFile(mapFileName, new StringBuilder(JsonUtils.getJson(mapDataMatcher)));
	}

	private String getUniqueEntityName(MapMakerEntityMatcher matcher, Set<String> entityNames) {
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

	public void moveTriggerData(Point delta) {
		triggersSaved = false;

		for (MapMakerEntityMatcher matcher : this.triggerData) {
			matcher.addDelta(delta);
		}

		for (MapMakerEntityMatcher matcher : this.entities) {
			matcher.addDelta(delta);
		}
	}

	public void drawTriggers(Graphics2D g2d, Point mapLocation) {
		for (MultiPointEntityMatcher data : this.triggerData) {
			for (Point point : data.getLocation()) {
				DrawUtils.outlineTileRed(g2d, point, mapLocation);

				if (data.getTriggerModelType() == TriggerModelType.WILD_BATTLE) {
					BufferedImage image = TriggerModelType.WILD_BATTLE.getImage(mapMaker);
					DrawUtils.drawTileImage(g2d, image, point, mapLocation);
				}
			}
		}

		for (SinglePointEntityMatcher entity : this.entities) {
			Point point = entity.getLocation();
			TriggerModelType triggerModelType = entity.getTriggerModelType();

			BufferedImage image = null;
			switch (triggerModelType) {
				case MAP_TRANSITION:
					Point exit = ((MapTransitionMatcher)entity).getExitLocation();
					BufferedImage exitImage = TriggerModel.getMapExitImage(mapMaker);
					DrawUtils.drawTileImage(g2d, exitImage, exit, mapLocation);
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
		}
	}

	private int getMapIndex(Point location) {
		return location.getIndex(this.mapMaker.getCurrentMapSize().width);
	}

	public void placeTrigger(Point location) {

		// TODO: Ask user if they would like to place over
		PlaceableTrigger placeableTrigger = mapMaker.getPlaceableTrigger();
		switch (placeableTrigger.triggerType) {
			case ENTITY:
				SinglePointEntityMatcher entity = placeableTrigger.entity;

				entity.setPoint(location);
				this.entities.add(entity);

				System.out.println("Entity placed at (" + location.x + ", " + location.y + ").");
				break;
			case TRIGGER_DATA:
				MultiPointEntityMatcher trigger = placeableTrigger.triggerData;
				trigger.addPoint(location);
				this.triggerData.add(trigger);
				break;
		}

		mapMaker.clearPlaceableTrigger();
		triggersSaved = false;
	}

	// Used for moving triggers
	public TriggerModelType getTriggerModelType(PlaceableTrigger trigger) {
		if (trigger.triggerType == PlaceableTriggerType.ENTITY) {
			return trigger.entity.getTriggerModelType();
		}
		else if (trigger.triggerType == PlaceableTriggerType.TRIGGER_DATA) {
			return  trigger.triggerData.getTriggerModelType();
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

	public void editTrigger(PlaceableTrigger trigger) {
		PlaceableTrigger newTrigger = null;

		if (trigger.triggerType == PlaceableTriggerType.ENTITY) {
			EntityMatcher entity = trigger.entity;
			TriggerModelType triggerModelType = trigger.entity.getTriggerModelType();

			switch (triggerModelType) {
				case ITEM:
					newTrigger = editItem((ItemMatcher)entity);
					break;
				case NPC:
					newTrigger = editNPC((NPCMatcher)entity);
					break;
				case TRIGGER_ENTITY:
					// TODO: Need a new edit and dialog
					newTrigger = editEventTrigger((EventMatcher)entity);
					break;
				case MAP_TRANSITION:
					newTrigger = editMapTransition((MapTransitionMatcher)entity);
					break;
			}

			// Update entity list
			if (newTrigger != null) {
				this.entities.remove(trigger.entity);
				this.entities.add(newTrigger.entity);

				newTrigger.entity.setPoint(trigger.entity.getLocation());

				triggersSaved = false;
			}
		}
		else if (trigger.triggerType == PlaceableTriggerType.TRIGGER_DATA) {

			switch (trigger.triggerData.getTriggerModelType()) {
				case WILD_BATTLE:
					newTrigger = editWildBattleTrigger((WildBattleMatcher)trigger.triggerData);
					break;
				case EVENT:
					newTrigger = editEventTrigger((EventMatcher)trigger.triggerData);
					break;
			}

			if (newTrigger != null) {
				this.triggerData.remove(trigger.triggerData);
				this.triggerData.add(newTrigger.triggerData);

				triggersSaved = false;
			}
		}
	}

	// TODO: Why is this just entities and not triggers?
	private List<SinglePointEntityMatcher> getEntitiesAtLocation(Point location) {
		List<SinglePointEntityMatcher> triggerList = new ArrayList<>();
		for (SinglePointEntityMatcher entity : this.entities) {
			if (entity.isAtLocation(location)) {
				triggerList.add(entity);
			}
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

	public void removeTrigger(PlaceableTrigger trigger) {
		this.entities.removeIf(matcher -> trigger.entity == matcher);
		this.triggerData.removeIf(matcher -> trigger.triggerData == matcher);

		triggersSaved = false;
	}

	public void moveTrigger(PlaceableTrigger trigger) {
		removeTrigger(trigger);
		mapMaker.setPlaceableTrigger(trigger);
		System.out.println(mapMaker.getPlaceableTrigger().name);
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

	private PlaceableTrigger editItem(ItemMatcher item) {
		ItemEntityDialog itemDialog = new ItemEntityDialog(mapMaker);
		itemDialog.loadMatcher(item);

		if (!dialogOption("Item Editor", itemDialog)) {
			return null;
		}

		ItemNamesies itemType = itemDialog.getItemName();
		if (itemType == null) {
			return null;
		}

		return new PlaceableTrigger(itemDialog.getMatcher());
	}

	private PlaceableTrigger editNPC(NPCMatcher npcData) {
		NPCEntityDialog npcDialog = new NPCEntityDialog(mapMaker);
		npcDialog.loadMatcher(npcData);

		if (!dialogOption("NPC Editor", npcDialog)) {
			return null;
		}

		NPCMatcher newEntity = npcDialog.getMatcher();
		return new PlaceableTrigger(newEntity);
	}

	private List<WildBattleMatcher> getWildBattleTriggers() {
		return this.triggerData
				.stream()
				.filter(entity -> entity instanceof WildBattleMatcher)
				.map(entity -> (WildBattleMatcher)entity)
				.collect(Collectors.toList());
	}

	private PlaceableTrigger wildBattleTriggerOptions() {
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
		return new PlaceableTrigger(matcher.get(0));
	}

	private PlaceableTrigger editWildBattleTrigger(WildBattleMatcher wildBattleTrigger) {
		WildBattleTriggerEditDialog dialog = new WildBattleTriggerEditDialog();
		dialog.loadMatcher(wildBattleTrigger);

		if (!dialogOption("Wild Battle Trigger Editor", dialog)) {
			return null;
		}

		WildBattleMatcher td = dialog.getMatcher();
		return new PlaceableTrigger(td);
	}

	private PlaceableTrigger editMapTransition(MapTransitionMatcher transitionMatcher) {
		MapTransitionDialog mapTransitionDialog = new MapTransitionDialog(mapMaker, this);
		if (transitionMatcher != null) {
			mapTransitionDialog.setMapTransition(transitionMatcher);
		}

		if (!dialogOption("Map Transition Editor", mapTransitionDialog)) {
			return null;
		}

		MapTransitionMatcher mapTransition = mapTransitionDialog.getMatcher();
		return new PlaceableTrigger(mapTransition);
	}

	private PlaceableTrigger editEventTrigger(EventMatcher eventMatcher) {
		EventTriggerDialog eventTriggerDialog = new EventTriggerDialog();
		eventTriggerDialog.loadMatcher(eventMatcher);

		if (!dialogOption("Event Trigger Editor", eventTriggerDialog)) {
			return null;
		}

		// TODO: confirm at least one action first

		EventMatcher matcher = eventTriggerDialog.getMatcher();
		return new PlaceableTrigger(matcher);
	}
}
