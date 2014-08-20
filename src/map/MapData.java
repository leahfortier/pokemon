package map;
import gui.GameData;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import main.Global;
import map.entity.Entity;
import map.entity.EntityData;
import map.entity.ItemEntityData;
import map.entity.NPCEntityData;
import map.entity.TriggerEntityData;
import map.triggers.TriggerData;
import trainer.CharacterData;


public class MapData {
	public static final Pattern blockPattern = Pattern.compile("((NPC|Item|Trigger|MapEntrance|TriggerData)\\s+)?(\\w+)\\s*\\{([^}]*)\\}");
	public static enum WalkType{
		WATER(0x0000FF), WALKABLE(0xFFFFFF), NOT_WALKABLE(0x000000), 
		HOP_DOWN(0x00FF00), HOP_UP(0xFF0000), HOP_LEFT(0xFFFF00), HOP_RIGHT(0x00FFFF);
		int value;
		WalkType(int v){
			value = v;
		}
	};
	
	public String name;
	private int[] bgTile, fgTile, walkMap, areaMap;
	private HashMap<Integer, String> triggers;
	private HashMap<String, Integer> mapEntrances;
	
	private int width, height;
	private ArrayList<EntityData> entities;
	public MapData(File file, GameData gameData){
		name = file.getName();
		BufferedImage bgMap, fgMap, moveMap, areaM = null;
		try {
			bgMap = ImageIO.read(new File(file.getPath()+Global.FILE_SLASH+name+"_bg.png"));
			fgMap = ImageIO.read(new File(file.getPath()+Global.FILE_SLASH+name+"_fg.png"));
			moveMap = ImageIO.read(new File(file.getPath()+Global.FILE_SLASH+name+"_move.png"));
			
			File f = new File(file.getPath()+Global.FILE_SLASH+name+"_area.png");
			if (f.exists())
				areaM = ImageIO.read(f);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		width = bgMap.getWidth();
		height = bgMap.getHeight();
		bgTile = bgMap.getRGB(0, 0, width, height, null, 0, width);
		fgTile = fgMap.getRGB(0, 0, width, height, null, 0, width);
		walkMap = moveMap.getRGB(0, 0, width, height, null, 0, width);
		
		//If map doesn't have an image for areas, create and save an empty image for areas.
		if(areaM == null)
		{
			areaM = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			File areaMapFile = new File(file.getPath()+Global.FILE_SLASH+name+"_area.png");
			try {
				ImageIO.write(areaM, "png", areaMapFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		areaMap = areaM.getRGB(0, 0, width, height, null, 0, width);
		
		entities = new ArrayList<>();
		triggers = new HashMap<>();
		mapEntrances = new HashMap<>();
		
		File f = new File(file.getPath()+Global.FILE_SLASH+name+".txt");
		String fileText = Global.readEntireFile(f, false);

		Matcher m = blockPattern.matcher(fileText);
		while(m.find()){
			String name = m.group(3);
			if(m.group(1) == null){ //trigger
				Scanner in = new Scanner(m.group(4));
				while(in.hasNext()){
					String[] xr = in.next().split("-");
					String[] yr = in.next().split("-");
					int x1, x2, y1, y2;
					x1 = Integer.parseInt(xr[0]);
					y1 = Integer.parseInt(yr[0]);
					x2 = xr.length == 2 ? Integer.parseInt(xr[1]) : x1;
					y2 = yr.length == 2 ? Integer.parseInt(yr[1]) : y1;
					for(int x = x1; x<=x2; x++)
						for(int y = y1; y<=y2; y++)
							triggers.put(y*width+x, name);
				}
				in.close();
			}
			else
			{
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
						for(Integer loc: triggerData.getPoints(width)) {
							triggers.put(loc, name);
						}
						triggerData.addData(gameData);
						break;
				}
			}
		}
	}
	
	public static Integer getMapEntranceLocation(String contents, int width) {
		
		int x = 0;
		int y = 0;
		
		Matcher m = EntityData.variablePattern.matcher(contents);
		while(m.find()){
			switch(m.group(1)){
			case "x":
				x = Integer.parseInt(m.group(2));
				break;
			case "y":
				y = Integer.parseInt(m.group(2));
				break;
			}
		}
		
		return y*width+x;
	}
	
	public int getBgTile(int x, int y){
		if(x<0 || x>=width || y<0 || y>=height)
			return 0;
		return bgTile[y*width + x];
	}
	
	public int getFgTile(int x, int y){
		if(x<0 || x>=width || y<0 || y>=height)
			return 0;
		return fgTile[y*width + x];
	}
	
	public WalkType getPassValue(int x, int y){
		if(x<0 || x>=width || y<0 || y>=height)
			return WalkType.NOT_WALKABLE;
		int val = walkMap[y*width+x]&((1<<24)-1);
		for(WalkType t: WalkType.values())
			if(t.value == val)
				return t;
		return WalkType.NOT_WALKABLE;
	}
	
	public int getAreaName(int x, int y) {
		if(x<0 || x>=width || y<0 || y>=height || areaMap == null)
			return 0;
		return areaMap[y*width + x];
	}
	
	public String trigger(CharacterData character) {
		int val = character.locationY*width + character.locationX;
		if(triggers.containsKey(val))
			return triggers.get(val);
		return null;
	}
	
	public boolean setCharacterToEntrance(CharacterData character, String entranceName) {
		if(mapEntrances.containsKey(entranceName)) {
			int entrance = mapEntrances.get(entranceName);
			int newY = entrance / width;
			int newX = entrance - newY * width;
			character.setLocation(newX, newY);
			return true;
		}
		
		return false;
	}
	
	public boolean inBounds(int x, int y){
		return !(x<0 || x>=width || y<0 || y>=height);
	}

	public Entity[][] populateEntities(CharacterData character, GameData gameData) {
		Entity[][] res = new Entity[width][height];
		for(EntityData data: entities)
			if(data.isEntityPresent(character)){
				Entity e = data.getEntity();
				e.addData(gameData);
				res[e.charX][e.charY] = e; 
			}
		return res;
	}
}
