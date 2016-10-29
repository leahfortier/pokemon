package map;
import gui.GameData;
import gui.GameFrame;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import map.entity.Entity;
import map.entity.EntityData;
import map.entity.ItemEntityData;
import map.entity.NPCEntityData;
import map.entity.TriggerEntityData;
import map.triggers.TriggerData;
import trainer.CharacterData;
import util.FileIO;


public class MapData {
	public static final Pattern blockPattern = Pattern.compile("((NPC|Item|Trigger|MapEntrance|TriggerData)\\s+)?(\\w+)\\s*\\{([^}]*)\\}");
	
	public final String name;
	
	private final int[] bgTile;
	private final int[] fgTile;
	private final int[] walkMap;
	private final int[] areaMap;
	
	private final HashMap<Integer, String> triggers;
	private final HashMap<String, Integer> mapEntrances;
	
	private final int width;
	private final int height;
	
	private final ArrayList<EntityData> entities;
	
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

		Matcher m = blockPattern.matcher(fileText);
		while (m.find()) {
			String name = m.group(3);
			
			// Trigger
			if (m.group(1) == null) {
				Scanner in = new Scanner(m.group(4));
				while (in.hasNext()) {
					String[] xr = in.next().split("-");
					String[] yr = in.next().split("-");
				
					int x1 = Integer.parseInt(xr[0]);
					int y1 = Integer.parseInt(yr[0]);
					int x2 = xr.length == 2 ? Integer.parseInt(xr[1]) : x1;
					int y2 = yr.length == 2 ? Integer.parseInt(yr[1]) : y1;
					
					for (int x = x1; x<=x2; x++)
						for (int y = y1; y<=y2; y++)
							triggers.put(y*width + x, name);
				}
				
				in.close();
			}
			else {
				switch (m.group(2)) {
					case "NPC":
						entities.add(new NPCEntityData(name, m.group(4)));
						break;
					case "Item":
						entities.add(new ItemEntityData(name, m.group(4)));
						break;
					case "Trigger":
						entities.add(new TriggerEntityData(name, m.group(4)));
						break;
					case "MapEntrance":
						mapEntrances.put(name, getMapEntranceLocation(m.group(4), width));
						break;
					case "TriggerData":
						TriggerData triggerData = new TriggerData(name, m.group(4));
						
						for (Integer loc: triggerData.getPoints(width)) {
							triggers.put(loc, name);
						}
						
						triggerData.addData(gameData);
						break;
				}
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
		
		return y*width + x;
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
		for (EntityData data: entities) {
			if (data.isEntityPresent(character) || GameFrame.GENERATE_STUFF) {
				Entity e = data.getEntity();
				e.reset();
				e.addData(gameData);
				res[e.getX()][e.getY()] = e; 
			}
		}
		
		return res;
	}
}
