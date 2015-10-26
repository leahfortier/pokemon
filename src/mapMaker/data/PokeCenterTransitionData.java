package mapMaker.data;

import gui.GameData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;

import main.FileIO;
import map.triggers.GroupTrigger;
import map.triggers.MapTransitionTrigger;
import map.triggers.Trigger;
import mapMaker.MapMaker;

public class PokeCenterTransitionData {
	
	public static final String pokeCenterTransitionTriggersFile = MapMaker.recFolderNme +MapMaker.FILE_SLASH +"triggers" + MapMaker.FILE_SLASH + "PokeCenterTransitionTriggers";
	
	private GroupTrigger groupTrigger;
	private HashMap<String, MapTransitionTrigger> transitionTriggers;
	
	private MapMaker mapMaker;
	private File pokeCenterTransitionFile;
	
	private boolean saved;
	
	public PokeCenterTransitionData(MapMaker mapMaker) {
		this.mapMaker = mapMaker;
		this.groupTrigger = null;
		this.transitionTriggers = new HashMap<>();
		
		readTransitions();
		
		saved = true;
	}
	
	private void readTransitions() {
		
		pokeCenterTransitionFile = new File(mapMaker.root.getPath() + MapMaker.FILE_SLASH + pokeCenterTransitionTriggersFile);
		
		String fileText = FileIO.readEntireFile(pokeCenterTransitionFile, false);
		Matcher m = GameData.triggerBlockPattern.matcher(fileText);
		while (m.find())
		{
			String type = m.group(1);
			String name = m.group(2);
			Trigger trigger = Trigger.createTrigger(type, name, m.group(3));
			
			if (type.equals("Group")) {
				groupTrigger = (GroupTrigger)trigger;
			}
			else if (type.equals("MapTransition")) {
				MapTransitionTrigger transitionTrigger = (MapTransitionTrigger)trigger;
				transitionTriggers.put(transitionTrigger.mapName +"_" + transitionTrigger.mapEntranceName, transitionTrigger);
			}
		}
		
		if (groupTrigger == null) {
			groupTrigger = new GroupTrigger("GroupTrigger_PokeCenter_Exit","trigger: SetTeleportToLastPokeCenter");
		}
	}
	
	public void add(String mapName, String entrance) {
		saved = false;
		
		String name = "from_PokeCenter_to_" + mapName + "_at_" + entrance;
		
		String globalString = "MapGlobal_toPokeCenterFromEntrance_" +entrance;
		
		String conditionString = "MapGlobal_PreviousMap_" + mapName +"&" + globalString;
		
		String contents = "condition: " + conditionString +"\n"+
				"global: !"+ globalString +"\n" +
				"nextMap: " +mapName +"\n"+
				"mapEntrance: " +entrance;
		
		MapTransitionTrigger transitionTrigger = new MapTransitionTrigger(name, contents); 
		transitionTriggers.put(transitionTrigger.mapName +"_" + transitionTrigger.mapEntranceName, transitionTrigger);
		
		groupTrigger.triggers.add(groupTrigger.triggers.size() - 1, transitionTrigger.getName());
	}
	
	public void remove(String mapName, String entrance) {
		saved = false;
		
		String name = "from_PokeCenter_to_" + mapName + "_at_" + entrance;
		
		groupTrigger.triggers.remove(name);
		
		transitionTriggers.remove(mapName +"_" + entrance);
	}

	public void save() {
		if (saved) return;
		saved = true;
		
		FileWriter writer;
		try {
			writer = new FileWriter(pokeCenterTransitionFile);
		
			writer.write("GroupTrigger " +groupTrigger.getName() +" {\n" + groupTrigger.triggerDataAsString() + "}\n\n");
			
			for (String mapTransition: transitionTriggers.keySet()) {
				
				MapTransitionTrigger trigger = transitionTriggers.get(mapTransition);
				writer.write("MapTransitionTrigger " +trigger.getName() +" {\n" + trigger.triggerDataAsString() + "}\n\n");
			}
			
			writer.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
