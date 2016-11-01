package map;

import gui.GameData;
import gui.GameFrame;
import map.entity.Entity;
import map.entity.EntityData;
import map.entity.ItemEntityData;
import map.entity.NPCEntityData;
import map.entity.TriggerEntityData;
import map.triggers.TriggerData;
import pattern.AreaDataMatcher;
import pattern.AreaDataMatcher.GroupTriggerMatcher;
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
	
	public MapData(File file, GameData gameData) {
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
			mapEntrances.put(matcher.name, getMapEntranceLocation(matcher.x, matcher.y, width));
		}

		for (TriggerDataMatcher matcher : areaDataMatcher.triggerData) {
			TriggerData triggerData = new TriggerData(matcher);

			for (Integer loc: triggerData.getPoints(width)) {
				triggers.put(loc, matcher.name);
			}

			triggerData.addData(gameData);
		}

		for (TriggerMatcher matcher : areaDataMatcher.triggers) {
			entities.add(new TriggerEntityData(matcher));
		}

		for (GroupTriggerMatcher matcher : areaDataMatcher.groupTriggers) {
			for (int i = 0; i < matcher.location.length; i += 2) {
				final int x = matcher.location[i];
				final int y = matcher.location[i + 1];

				triggers.put(x + y*width, matcher.name);
			}

			// TODO: Does this range shit still exist?
//			while (in.hasNext()) {
//				String[] xr = in.next().split("-");
//				String[] yr = in.next().split("-");
//
//				int x1 = Integer.parseInt(xr[0]);
//				int y1 = Integer.parseInt(yr[0]);
//				int x2 = xr.length == 2 ? Integer.parseInt(xr[1]) : x1;
//				int y2 = yr.length == 2 ? Integer.parseInt(yr[1]) : y1;
//
//				for (int x = x1; x<=x2; x++)
//					for (int y = y1; y<=y2; y++)
//						triggers.put(y*width + x, name);
//			}
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
	
	public static Integer getMapEntranceLocation(String contents, int width) {
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
		
		return getMapEntranceLocation(x, y, width);
	}

	public static Integer getMapEntranceLocation(int x, int y, int width) {
		return x + y*width;
	}
	
	public boolean inBounds(int x, int y) {
		return x >= 0 && x < width && y >= 0 && y < height;
	}
	
	public int getBgTile(int x, int y) {
		if (!inBounds(x, y)) {
			return 0;
		}
		
		return bgTile[y*width + x];
	}
	
	public int getFgTile(int x, int y) {
		if (!inBounds(x, y)) {
			return 0;
		}
		
		return fgTile[y*width + x];
	}
	
	public WalkType getPassValue(int x, int y) {
		if (!inBounds(x, y)) {
			return WalkType.NOT_WALKABLE;
		}
		
		int val = walkMap[y*width + x]&((1<<24) - 1);
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
		
		return areaMap[y*width + x];
	}
	
	public String trigger(CharacterData character) {
		int val = character.locationY*width + character.locationX;
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

	public Entity[][] populateEntities(CharacterData character, GameData gameData) {
		Entity[][] res = new Entity[width][height];
		entities.stream()
				.filter(data -> data.isEntityPresent(character) || GameFrame.GENERATE_STUFF)
				.forEach(data -> {
					Entity e = data.getEntity();
					e.reset();
					e.addData(gameData);
					res[e.getX()][e.getY()] = e;
				});
		
		return res;
	}
}
