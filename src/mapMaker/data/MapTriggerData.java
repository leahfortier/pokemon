package mapMaker.data;

import gui.GameData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;

import main.Global;
import map.triggers.Trigger;
import mapMaker.MapMaker;

public class MapTriggerData {
	
	public static final String mapTriggersFilePath = MapMaker.recFolderNme +MapMaker.FILE_SLASH +"triggers" + MapMaker.FILE_SLASH;
	public static final String mapFileNameExtension = "_Triggers";
	
	private boolean saved;
	
	MapMaker mapMaker;
	String currentMap;
	
	HashMap<String, Trigger> triggers;
	File triggerFile;
	
	
	public MapTriggerData(MapMaker mapMaker, String currentMap) {
		this.mapMaker = mapMaker;
		this.currentMap = currentMap;
		
		triggers = new HashMap<>();
		
		readTriggers();
		
		saved = true;
	}
	
	private void readTriggers() {

		triggerFile = new File(mapMaker.root.getPath() + MapMaker.FILE_SLASH + mapTriggersFilePath + currentMap + mapFileNameExtension);
		if (!triggerFile.exists())
			return;
		
		String fileText = Global.readEntireFile(triggerFile, false);
		Matcher m = GameData.triggerBlockPattern.matcher(fileText);
		while (m.find())
		{
			String type = m.group(1);
			String name = m.group(2);
			Trigger trig = GameData.createTrigger(type, name, m.group(3));
			
			System.out.println("Trigger: " +name);
			
			triggers.put(name, trig);
		}
	}
	
	public void save() {
		if (saved)
			return;
		saved = true;
		
		try {
				
			if (!triggerFile.exists()) {
				triggerFile.getParentFile().mkdirs();
				triggerFile.createNewFile();
			}
			
			FileWriter writer = new FileWriter(triggerFile);
			
			for (Trigger trigger: triggers.values()){
				writer.write(trigger.getClass().getName() +" " +trigger.getName() +"{\n"+
						trigger.triggerDataAsString() + "\n"+
						"}\n");
			}
			
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Trigger getTrigger(String name) {
		return triggers.get(name);
	}
	
	public void removeTrigger(String name) {
		saved = false;
		triggers.remove(name);
	}
	
	public void addTrigger(String name, Trigger trigger) {
		saved = false;
		triggers.put(name, trigger);
	}
	
	public boolean isTriggerNameTaken(String triggerName) {
		return triggers.containsKey(triggerName);
	}
	
	public String[] getTriggerNames() {
		
		String[] names = new String[triggers.size()];
		triggers.keySet().toArray(names);
		
		return names;
	}
}
