package map;

import gui.GameFrame;
import main.Game;
import map.entity.Entity;
import map.entity.EntityData;
import map.entity.ItemEntityData;
import map.entity.TriggerEntityData;
import map.entity.npc.NPCEntityData;
import map.triggers.TriggerData;
import pattern.AreaDataMatcher;
import pattern.AreaDataMatcher.ItemMatcher;
import pattern.AreaDataMatcher.MapEntranceMatcher;
import pattern.AreaDataMatcher.NPCMatcher;
import pattern.AreaDataMatcher.TriggerDataMatcher;
import pattern.AreaDataMatcher.TriggerMatcher;
import trainer.CharacterData;
import util.FileIO;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;


public class MapData {
	public final String name;

	private final int[] bgTile;
	private final int[] fgTile;
	private final int[] walkMap;
	private final int[] areaMap;
	
	private final Map<Integer, String> triggers;
	private final Map<String, Integer> mapEntrances;

	private final int width;
	private final int height;
	
	private final List<EntityData> entities;
	
	public MapData(File file) {
		name = file.getName();
		
		String beginFilePath = FileIO.makeFolderPath(file.getPath()) + name;
		
		BufferedImage bgMap = FileIO.readImage(beginFilePath + "_bg.png");
		width = bgMap.getWidth();
		height = bgMap.getHeight();
		bgTile = bgMap.getRGB(0, 0, width, height, null, 0, width);
		
		BufferedImage fgMap = FileIO.readImage(beginFilePath + "_fg.png");
		fgTile = fgMap.getRGB(0, 0, width, height, null, 0, width);
		
		BufferedImage moveMap = FileIO.readImage(beginFilePath + "_move.png");
		walkMap = moveMap.getRGB(0, 0, width, height, null, 0, width);
		
		BufferedImage areaM;
		File areaMapFile = new File(beginFilePath + "_area.png");
		if (areaMapFile.exists()) {
			areaM = FileIO.readImage(areaMapFile);			
		}
		else {
			// If map doesn't have an image for areas, create and save an empty image for areas.
			areaM = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			FileIO.writeImage(areaM, areaMapFile);
		}
		areaMap = areaM.getRGB(0, 0, width, height, null, 0, width);
		
		entities = new ArrayList<>();
		triggers = new HashMap<>();
		mapEntrances = new HashMap<>();
		
		File f = new File(beginFilePath + ".txt");
		String fileText = FileIO.readEntireFileWithReplacements(f, false);

		AreaDataMatcher areaDataMatcher = AreaDataMatcher.matchArea(beginFilePath + ".txt", fileText);
		for (NPCMatcher matcher : areaDataMatcher.NPCs) {
			entities.add(new NPCEntityData(matcher));
		}

		for (ItemMatcher matcher : areaDataMatcher.items) {
			entities.add(new ItemEntityData(matcher));
		}

		for (MapEntranceMatcher matcher : areaDataMatcher.mapEntrances) {
			mapEntrances.put(matcher.name, getMapIndex(matcher.x, matcher.y));
		}

		for (TriggerDataMatcher matcher : areaDataMatcher.triggerData) {
			TriggerData triggerData = new TriggerData(matcher);

			for (Integer loc: triggerData.getPoints(width)) {
				triggers.put(loc, matcher.name);
			}

			triggerData.addData();
		}

		for (TriggerMatcher matcher : areaDataMatcher.triggers) {
			entities.add(new TriggerEntityData(matcher));
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
	
	public static int getMapEntranceLocation(String contents, int width) {
		int x = 0;
		int y = 0;
		
		Matcher m = EntityData.variablePattern.matcher(contents);
		while (m.find()) {
			switch (m.group(1)) {
				case "x":
					x = Integer.parseInt(m.group(2));
					break;
				case "y":
					y = Integer.parseInt(m.group(2));
					break;
			}
		}
		
		return getMapIndex(x, y, width);
	}

	public int getPlayerMapIndex() {
		CharacterData player = Game.getPlayer();
		return getMapIndex(player.locationX, player.locationY);
	}

	public int getMapIndex(int x, int y) {
		return getMapIndex(x, y, width);
	}

	public static Integer getMapIndex(int x, int y, int width) {
		return x + y*width;
	}
	
	public boolean inBounds(int x, int y) {
		return x >= 0 && x < width && y >= 0 && y < height;
	}
	
	public int getBgTile(int x, int y) {
		if (!inBounds(x, y)) {
			return 0;
		}
		
		return bgTile[getMapIndex(x, y)];
	}
	
	public int getFgTile(int x, int y) {
		if (!inBounds(x, y)) {
			return 0;
		}
		
		return fgTile[getMapIndex(x, y)];
	}
	
	public WalkType getPassValue(int x, int y) {
		if (!inBounds(x, y)) {
			return WalkType.NOT_WALKABLE;
		}
		
		int val = walkMap[getMapIndex(x, y)]&((1<<24) - 1);
		for (WalkType t: WalkType.values()) {
			if (t.value == val) {
				return t;
			}
		}
		
		return WalkType.NOT_WALKABLE;
	}
	
	public int getAreaName(int x, int y) {
		if (!inBounds(x, y) || areaMap == null) {
			return 0;
		}
		
		return areaMap[getMapIndex(x, y)];
	}
	
	public String trigger() {
		int val = getPlayerMapIndex();
		if (triggers.containsKey(val)) {
			return triggers.get(val);
		}
	
		return null;
	}
	
	public boolean setCharacterToEntrance(CharacterData character, String entranceName) {
		if (mapEntrances.containsKey(entranceName)) {
			int entrance = mapEntrances.get(entranceName);
			int newY = entrance / width;
			int newX = entrance - newY * width;
			character.setLocation(newX, newY);
			
			return true;
		}
		
		return false;
	}

	public Entity[][] populateEntities() {
		Entity[][] res = new Entity[width][height];
		entities.stream()
				.filter(data -> data.isEntityPresent() || GameFrame.GENERATE_STUFF)
				.forEach(data -> {
					Entity entity = data.getEntity();
					entity.reset();
					entity.addData();
					res[entity.getX()][entity.getY()] = entity;
				});
		
		return res;
	}
}
