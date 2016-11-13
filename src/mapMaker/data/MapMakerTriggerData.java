package mapMaker.data;

import main.Global;
import mapMaker.MapMaker;
import mapMaker.data.PlaceableTrigger.PlaceableTriggerType;
import mapMaker.dialogs.EventTriggerDialog;
import mapMaker.dialogs.ItemEntityDialog;
import mapMaker.dialogs.MapTransitionDialog;
import mapMaker.dialogs.NPCEntityDialog;
import mapMaker.dialogs.TriggerDialog;
import mapMaker.dialogs.TriggerEntityDialog;
import mapMaker.dialogs.WildBattleTriggerEditDialog;
import mapMaker.dialogs.WildBattleTriggerOptionsDialog;
import mapMaker.model.TileModel.TileType;
import mapMaker.model.TriggerModel;
import mapMaker.model.TriggerModel.TriggerModelType;
import namesies.ItemNamesies;
import pattern.AreaDataMatcher;
import pattern.AreaDataMatcher.AreaMatcher;
import pattern.AreaDataMatcher.EntityMatcher;
import pattern.AreaDataMatcher.ItemMatcher;
import pattern.AreaDataMatcher.MapTransitionMatcher;
import pattern.AreaDataMatcher.NPCMatcher;
import pattern.AreaDataMatcher.TriggerMatcher;
import util.DrawUtils;
import util.FileIO;
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

	// Entities
	// Holds a list of entities for every location. Some entities only appear
	// given a condition, allowing multiple entities to be at one location at a time
	private Set<AreaMatcher> areaData;
	private Set<EntityMatcher> entities;
	private Set<TriggerMatcher> triggerData;

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
		this.areaData = new HashSet<>(areaDataMatcher.getAreas());
		this.entities = new HashSet<>(areaDataMatcher.getEntities());
		this.triggerData = new HashSet<>(areaDataMatcher.getTriggerData());

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

		AreaDataMatcher areaDataMatcher = new AreaDataMatcher(
				areaData,
				entities,
				triggerData
		);

		FileIO.createFile(mapFileName);
		FileIO.overwriteFile(mapFileName, new StringBuilder(AreaDataMatcher.getJson(areaDataMatcher)));
	}

	public void moveTriggerData(Point delta) {
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
				if (entity instanceof MapTransitionMatcher) {
					image = TriggerModelType.MAP_TRANSITION.getImage(mapMaker);

					for (Point exit : ((MapTransitionMatcher) entity).getExits()) {
						BufferedImage exitImage = TriggerModel.getMapExitImage(mapMaker);
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

	public int getMapIndex(int x, int y) {
		return Point.getIndex(x, y, this.mapMaker.getCurrentMapSize().width);
	}

	public int getMapIndex(Point location) {
		return getMapIndex(location.x, location.y);
	}

	public void placeTrigger(Point location) {

		// TODO: Ask user if they would like to place over
		PlaceableTrigger placeableTrigger = mapMaker.getPlaceableTrigger();
		switch (placeableTrigger.triggerType) {
			case ENTITY:
				EntityMatcher entity = placeableTrigger.entity;

				entity.addPoint(location);
				this.entities.add(entity);

				System.out.println("Entity " + entity.getName() + " placed at (" + location.x + ", " + location.y + ").");
				break;
			case TRIGGER_DATA:
				placeableTrigger.triggerData.addPoint(location);
				break;
		}

		mapMaker.clearPlaceableTrigger();
		triggersSaved = false;
	}

	// Used for moving triggers
	public TriggerModelType getTriggerModelType(PlaceableTrigger trigger) {
		if (trigger.triggerType == PlaceableTriggerType.ENTITY) {
			if (trigger.entity instanceof ItemMatcher) {
				return TriggerModelType.ITEM;
			} else if (trigger.entity instanceof NPCMatcher) {
				return TriggerModelType.NPC;
			} else if (trigger.entity instanceof TriggerMatcher) {
				return TriggerModelType.TRIGGER_ENTITY;
			} else if (trigger.entity instanceof MapTransitionMatcher) {
				return TriggerModelType.MAP_TRANSITION;
			}
		}
		else if (trigger.triggerType == PlaceableTriggerType.TRIGGER_DATA) {
			if (trigger.triggerData.isWildBattleTrigger()) {
				return TriggerModelType.WILD_BATTLE;
			}
			else {
				return TriggerModelType.EVENT;
			}
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

			if (entity instanceof ItemMatcher) {
				newTrigger = editItem((ItemMatcher)entity);
			}
			else if (entity instanceof NPCMatcher) {
				newTrigger = editNPC((NPCMatcher)entity);
			}
			else if (entity instanceof TriggerMatcher) {
				newTrigger = editTriggerEntity((TriggerMatcher)entity);
			} else if (entity instanceof MapTransitionMatcher) {
				newTrigger = editMapTransition((MapTransitionMatcher)entity);
			}

			// Update entity list
			if (newTrigger != null) {
				this.entities.remove(trigger.entity);
				this.entities.add(newTrigger.entity);

				newTrigger.entity.addPoint(trigger.entity.getLocation().get(0));

				triggersSaved = false;
			}
		}
		else if (trigger.triggerType == PlaceableTriggerType.TRIGGER_DATA) {

			if (trigger.triggerData.isWildBattleTrigger()) {
				newTrigger = editWildBattleTrigger(trigger.triggerData);
			}
			else {
				newTrigger = editEventTrigger(trigger.triggerData);
			}

			if (newTrigger != null) {
				this.triggerData.remove(trigger.triggerData);
				this.triggerData.add(newTrigger.triggerData);

				triggersSaved = false;
			}
		}
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

	private boolean hasEntityName(String entityName) {
		for (EntityMatcher matcher : this.entities) {
			if (entityName.equals(matcher.getName())) {
				return true;
			}
		}

		for (TriggerMatcher matcher : this.triggerData) {
			if (entityName.equals(matcher.getName())) {
				return true;
			}
		}

		return false;
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
		} while (this.hasEntityName(uniqueEntityName));

		System.out.println(uniqueEntityName);

		return uniqueEntityName;
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

		String itemEntityName = getUniqueEntityName(TriggerModelType.ITEM, itemType.getName());
		return new PlaceableTrigger(itemDialog.getMatcher());
	}

	private PlaceableTrigger editNPC(NPCMatcher npcData) {
		NPCEntityDialog npcDialog = new NPCEntityDialog(mapMaker);
		if (npcData != null) {
			// TODO: wtf
//			npcDialog.setNPCData(npcData, npcData.name.replaceAll("^" + mapMaker.getCurrentMapName() + "_NPC_|_\\d + $", ""));
			npcDialog.loadMatcher(npcData);
		}

		if (!dialogOption("NPC Editor", npcDialog)) {
			return null;
		}

		NPCMatcher newEntity = npcDialog.getMatcher();
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

	private List<TriggerMatcher> getWildBattleTriggers() {
		return this.triggerData
				.stream()
				.filter(TriggerMatcher::isWildBattleTrigger)
				.collect(Collectors.toList());
	}

	private PlaceableTrigger wildBattleTriggerOptions() {
		WildBattleTriggerOptionsDialog wildBattleTriggerOptions = new WildBattleTriggerOptionsDialog();
		wildBattleTriggerOptions.loadMatcher(this.getWildBattleTriggers());

		if (!dialogOption("Wild Battle Trigger Options", wildBattleTriggerOptions)) {
			return null;
		}

		List<TriggerMatcher> matcher = wildBattleTriggerOptions.getMatcher();
		if (matcher == null || matcher.isEmpty()) {
			return null;
		}

		// TODO: Wild battles need to be handled differently
		return new PlaceableTrigger(matcher.get(0));
	}

	private PlaceableTrigger editWildBattleTrigger(TriggerMatcher wildBattleTrigger) {
		WildBattleTriggerEditDialog dialog = new WildBattleTriggerEditDialog();
		dialog.loadMatcher(wildBattleTrigger);

		if (!dialogOption("Wild Battle Trigger Editor", dialog)) {
			return null;
		}

		return null;
//		TriggerMatcher td = dialog.getTriggerData();
//
//		// System.out.println(wildBattleTrigger.getName() +" " +td.name + " " +
//		// td.triggerType +" " + td.triggerContents);
//
//		return new PlaceableTrigger(td);
	}

	private PlaceableTrigger editMapTransition(MapTransitionMatcher transitionMatcher) {
		MapTransitionDialog mapTransitionDialog = new MapTransitionDialog(mapMaker, this);
		if (transitionMatcher != null) {
			mapTransitionDialog.setMapTransition(transitionMatcher);
		}

		if (!dialogOption("Map Transition Editor", mapTransitionDialog)) {
			return null;
		}

		String mapDestination = mapTransitionDialog.getDestination();
		String mapTriggerName = getUniqueEntityName(TriggerModelType.MAP_TRANSITION, mapDestination);

		MapTransitionMatcher mapTransition = mapTransitionDialog.getMatcher();
		return new PlaceableTrigger(mapTransition);
	}

	private PlaceableTrigger editEventTrigger(TriggerMatcher triggerMatcher) {
		EventTriggerDialog eventTriggerDialog = new EventTriggerDialog();
		eventTriggerDialog.loadMatcher(triggerMatcher);

		if (!dialogOption("Event Trigger Editor", eventTriggerDialog)) {
			return null;
		}

		// TODO: confirm at least one action first

		String eventTriggerName = eventTriggerDialog.getEventName();
		String triggerName = getUniqueEntityName(TriggerModelType.EVENT, eventTriggerName);

		TriggerMatcher matcher = eventTriggerDialog.getMatcher();
		return new PlaceableTrigger(matcher);
	}
}
