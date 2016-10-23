package mapMaker.data;

import gui.GameData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;

import util.FileIO;
import map.triggers.Trigger;
import mapMaker.MapMaker;

class MapTriggerData {
	
	private static final String mapTriggersFilePath = FileIO.makeFolderPath(MapMaker.recFolderName, "triggers");
	private static final String mapFileNameExtension = "_Triggers";
	
	private boolean saved;
	
	private MapMaker mapMaker;
	private String currentMap;
	
	private HashMap<String, Trigger> triggers;
	private File triggerFile;
	
	
	MapTriggerData(MapMaker mapMaker, String currentMap) {
		this.mapMaker = mapMaker;
		this.currentMap = currentMap;
		
		triggers = new HashMap<>();
		
		readTriggers();
		
		saved = true;
	}
	
	private void readTriggers() {

		triggerFile = new File(mapMaker.getPathWithRoot(mapTriggersFilePath + currentMap + mapFileNameExtension));
		if (!triggerFile.exists()) {
			return;
		}
		
		String fileText = FileIO.readEntireFileWithReplacements(triggerFile, false);
		Matcher m = GameData.triggerBlockPattern.matcher(fileText);
		while (m.find()) {
			String type = m.group(1);
			String name = m.group(2);
			
			Trigger trigger = Trigger.createTrigger(type, name, m.group(3));
			triggers.put(name, trigger);
		}
	}
	
	public void save() {
		if (saved) {
			return;
		}

		saved = true;
		
		try {
			// TODO: FileIO
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
			e.printStackTrace(); // TODO
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
	
	boolean isTriggerNameTaken(String triggerName) {
		return triggers.containsKey(triggerName);
	}
	
	public String[] getTriggerNames() {
		String[] names = new String[triggers.size()];
		triggers.keySet().toArray(names);
		
		return names;
	}
}
