package mapMaker.data;

import gui.GameData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import main.Global;
import util.FileIO;
import map.triggers.GroupTrigger;
import map.triggers.MapTransitionTrigger;
import map.triggers.Trigger;
import mapMaker.MapMaker;
import util.Folder;

class PokeCenterTransitionData {
	private static final String pokeCenterTransitionTriggersFile = Folder.TRIGGERS + "PokeCenterTransitionTriggers";
	
	private GroupTrigger groupTrigger;
	private Map<String, MapTransitionTrigger> transitionTriggers;
	
	private MapMaker mapMaker;
	private File pokeCenterTransitionFile;
	
	private boolean saved;
	
	PokeCenterTransitionData(MapMaker mapMaker) {
		this.mapMaker = mapMaker;
		this.groupTrigger = null;
		this.transitionTriggers = new HashMap<>();
		
		readTransitions();
		
		saved = true;
	}
	
	private void readTransitions() {
		pokeCenterTransitionFile = new File(mapMaker.getPathWithRoot(pokeCenterTransitionTriggersFile));
		
		String fileText = FileIO.readEntireFileWithReplacements(pokeCenterTransitionFile, false);
		Matcher m = GameData.triggerBlockPattern.matcher(fileText);
		while (m.find()) {
			String type = m.group(1);
			String name = m.group(2);
			Trigger trigger = Trigger.createTrigger(type, name, m.group(3));
			
			if (type.equals("Group")) {
				groupTrigger = (GroupTrigger)trigger;
			}
			else if (type.equals("MapTransition")) {
				MapTransitionTrigger transitionTrigger = (MapTransitionTrigger)trigger;
				transitionTriggers.put(transitionTrigger.getTransitionTriggerName(), transitionTrigger);
			}
		}
		
		if (groupTrigger == null) {
			// TODO: These should likely be a constant or something
			groupTrigger = new GroupTrigger("GroupTrigger_PokeCenter_Exit","trigger: SetTeleportToLastPokeCenter");
		}
	}
	
	public void add(String mapName, String entrance) {
		saved = false;
		
		String name = "from_PokeCenter_to_" + mapName + "_at_" + entrance;
		String globalString = "MapGlobal_toPokeCenterFromEntrance_" + entrance;
		String conditionString = "MapGlobal_PreviousMap_" + mapName + "&" + globalString;
		String contents = "condition: " + conditionString + "\n"+
				"global: !" + globalString + "\n" +
				"nextMap: " + mapName + "\n"+
				"mapEntrance: " + entrance;

		// TODO: Need to have a method for creating that transition name thingy
		MapTransitionTrigger transitionTrigger = new MapTransitionTrigger(name, contents); 
		transitionTriggers.put(transitionTrigger.getTransitionTriggerName(), transitionTrigger);
		
		groupTrigger.triggers.add(groupTrigger.triggers.size() - 1, transitionTrigger.getName());
	}
	
	public void remove(String mapName, String entrance) {
		saved = false;
		
		String name = "from_PokeCenter_to_" + mapName + "_at_" + entrance;
		
		groupTrigger.triggers.remove(name);
		transitionTriggers.remove(mapName + "_" + entrance);
	}

	public void save() {
		if (saved) {
			return;
		}

		saved = true;
		
		FileWriter writer;
		try {
			writer = new FileWriter(pokeCenterTransitionFile);
			writer.write("GroupTrigger " + groupTrigger.getName() +" {\n" + groupTrigger.triggerDataAsString() + "}\n\n");
			
			for (String mapTransition: transitionTriggers.keySet()) {
				MapTransitionTrigger trigger = transitionTriggers.get(mapTransition);
				writer.write("MapTransitionTrigger " + trigger.getName() + " {\n" + trigger.triggerDataAsString() + "}\n\n");
			}
			
			writer.close();
		} catch (IOException exception) {
			Global.error("IOException caught while saving something or other with the poke center transition data or something");
		}
	}
}
