package mapMaker.data;

import gui.GameData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.Global;
import map.triggers.EventTrigger;
import map.triggers.GroupTrigger;
import map.triggers.MapTransitionTrigger;
import map.triggers.Trigger;
import mapMaker.MapMaker;

public class TransitionBuildingData {

	public static final String TransitionBuildingTriggersFile = MapMaker.recFolderNme +MapMaker.FILE_SLASH +"triggers"+MapMaker.FILE_SLASH+"TransitionBuildingTriggers";
	public static final Pattern TransitionBuildingTransitionNamePattern = Pattern.compile("TransitionBuilding(H|V)_between_(\\w+)_and_(\\w+)_pair_(\\d+)_(North|South|East|West)Door");
	
	public static final String TransitionBuildingDialogFile = MapMaker.recFolderNme +MapMaker.FILE_SLASH +"dialogue"+MapMaker.FILE_SLASH+"TransitionBuildingDialogs";
	public static final Pattern TransitionBuildingInformationTriggerNamePattern = Pattern.compile("TransitionBuilding(H|V)_InformationDesk_between_map1_(\\w+)_area1_(\\w+)_and_map2_(\\w+)_area2_(\\w+)_pair_(\\d+)");
	
	public static final String[] directions = {"East", "West", "North", "South"};
	
	GroupTrigger[] groupTriggers;
	HashMap<String, MapTransitionTrigger> transitionTriggers;
	
	GroupTrigger infoTriggerGT;
	ArrayList<EventTrigger> infoTriggers;
	
	HashMap<String, HashMap<String, ArrayList<TransitionBuildingPair>>> transitionPairsMapping;
	HashSet<String> transitionPairNames;
	ArrayList<TransitionBuildingPair> allTransitionPairs; //Used for easy list access. 
	ArrayList<TransitionBuildingPair> incompletePairs;
	
	MapMaker mapMaker;
	File transitionBuildingTriggerFile;
	
	boolean saved;
	
	public TransitionBuildingData(MapMaker mapMaker) {
		this.mapMaker = mapMaker;
		groupTriggers = new GroupTrigger[4];
		transitionTriggers = new HashMap<>();
		
		transitionPairsMapping = new HashMap<>();
		transitionPairNames = new HashSet<>();
		allTransitionPairs = new ArrayList<>();
		incompletePairs = new ArrayList<>();
		
		infoTriggers = new ArrayList<>();
		
		readTransitions();
		
		saved = true;
	}
	
	private void readTransitions() {
		
		transitionBuildingTriggerFile = new File(mapMaker.root.getPath()+MapMaker.FILE_SLASH+TransitionBuildingTriggersFile);
		
		String fileText = Global.readEntireFile(transitionBuildingTriggerFile, false);
		Matcher m = GameData.triggerBlockPattern.matcher(fileText);
		while (m.find())
		{
			String type = m.group(1);
			String name = m.group(2);
			Trigger trig = GameData.createTrigger(type, name, m.group(3));
			
			if (type.equals("Group")) {
				if (name.endsWith("EastDoor")) {
					groupTriggers[0] = (GroupTrigger)trig;
				}
				else if (name.endsWith("WestDoor")) {
					groupTriggers[1] = (GroupTrigger)trig;
				}
				else if (name.endsWith("NorthDoor")) {
					groupTriggers[2] = (GroupTrigger)trig;
				}
				else if (name.endsWith("SouthDoor")) {
					groupTriggers[3] = (GroupTrigger)trig;
				}
				else if (name.equals("Info_Trigger")) {
					infoTriggerGT = (GroupTrigger)trig;
				}
			}
			else if (type.equals("Event")) {
				infoTriggers.add((EventTrigger)trig);
			}
			else if (type.equals("MapTransition") && name.matches(TransitionBuildingTransitionNamePattern.pattern())) {
				MapTransitionTrigger transitionTrig = (MapTransitionTrigger)trig;
				
				transitionTriggers.put(transitionTrig.mapName +"_"+transitionTrig.mapEntranceName, transitionTrig);
				
				Matcher nameMatcher = TransitionBuildingTransitionNamePattern.matcher(name);
				nameMatcher.find();
				
				String map1 = nameMatcher.group(2);
				String map2 = nameMatcher.group(3);
				
				TransitionBuildingPair pair = getTransitionPair(nameMatcher.group(1).equals("H"), map1, map2, Integer.parseInt(nameMatcher.group(4)));
				
				if (transitionTrig.mapName.equals(map1)) {
					pair.map1Entrance = transitionTrig.mapEntranceName;
				}
				else {
					pair.map2Entrance = transitionTrig.mapEntranceName;
				}
			}
		}
		
		//Create missing transition group triggers
		for (int currGroupTrig = 0; currGroupTrig < groupTriggers.length; ++currGroupTrig){
			if (groupTriggers[currGroupTrig] == null){
				groupTriggers[currGroupTrig] = new GroupTrigger("GroupTrigger_TransitionBuilding_"+directions[currGroupTrig]+"Door", "");
			}
		}
		
		//Create missing info group trigger
		if (infoTriggerGT == null) {
			infoTriggerGT = new GroupTrigger("GroupTrigger_TransitionBuilding_InformationDesk","");
		}
		
		//Loop through all info triggers and update areas for the maps
		for (EventTrigger infoTrigger: infoTriggers) {
			
			Matcher nameMatcher = TransitionBuildingInformationTriggerNamePattern.matcher(infoTrigger.getName());
			nameMatcher.find();
			String map1 = nameMatcher.group(2);
			String area1 = nameMatcher.group(3);
			String map2 = nameMatcher.group(4);
			String area2 = nameMatcher.group(5);
			
			TransitionBuildingPair pair = getTransitionPair(nameMatcher.group(1).equals("H"), map1, map2, Integer.parseInt(nameMatcher.group(6)));
			
			pair.area1 = (int)Long.parseLong(area1, 16);
			pair.area2 = (int)Long.parseLong(area2, 16);
		}
		
		infoTriggers.clear();
		
		//Collect all incomplete transition pairs
		for (TransitionBuildingPair pair: allTransitionPairs) {
			if (pair.map1Entrance == null || pair.map2Entrance == null) {
				incompletePairs.add(pair);
			}
		}
	}
	
	
	private TransitionBuildingPair getTransitionPair(boolean horizontal, String map1, String map2, int pairNumber) {
		
		if (!transitionPairsMapping.containsKey(map1)) {
			transitionPairsMapping.put(map1, new HashMap<String,ArrayList<TransitionBuildingPair>>());
		}
		if (!transitionPairsMapping.containsKey(map2)){
			transitionPairsMapping.put(map2, new HashMap<String,ArrayList<TransitionBuildingPair>>());
		}
		
		if (!transitionPairsMapping.get(map1).containsKey(map2)) {
			transitionPairsMapping.get(map1).put(map2, new ArrayList<TransitionBuildingPair>());
		}
		if (!transitionPairsMapping.get(map2).containsKey(map1)) {
			transitionPairsMapping.get(map2).put(map1, new ArrayList<TransitionBuildingPair>());
		}
		
		String pairName = TransitionBuildingPair.getPairName(horizontal, map1, map2, pairNumber); 
		ArrayList<TransitionBuildingPair> map1Pairs = transitionPairsMapping.get(map1).get(map2);
		for (TransitionBuildingPair pair: map1Pairs) {
			if (pair.getPairName().equals(pairName)) {
				//System.out.println("Found pair: " +pairName);
				return pair;
			}
		}
		
		TransitionBuildingPair transitionPair = new TransitionBuildingPair(horizontal, map1, map2, pairNumber);
		//System.out.println("Pair created: "+ transitionPair.getPairName());
		
		allTransitionPairs.add(transitionPair);
		
		transitionPairsMapping.get(map1).get(map2).add(transitionPair);
		transitionPairsMapping.get(map2).get(map1).add(transitionPair);
		
		transitionPairNames.add(transitionPair.getPairName());
		
		return transitionPair;
	}
	
	private void removeTransitionPair(boolean horizontal, String map1, String map2, int pairNumber) {
		
		if (!transitionPairsMapping.containsKey(map1)) {
			transitionPairsMapping.put(map1, new HashMap<String,ArrayList<TransitionBuildingPair>>());
		}
		if (!transitionPairsMapping.containsKey(map2)){
			transitionPairsMapping.put(map2, new HashMap<String,ArrayList<TransitionBuildingPair>>());
		}
		
		if (!transitionPairsMapping.get(map1).containsKey(map2)) {
			transitionPairsMapping.get(map1).put(map2, new ArrayList<TransitionBuildingPair>());
		}
		if (!transitionPairsMapping.get(map2).containsKey(map1)) {
			transitionPairsMapping.get(map2).put(map1, new ArrayList<TransitionBuildingPair>());
		}
		
		String pairName = TransitionBuildingPair.getPairName(horizontal, map1, map2, pairNumber); 
		ArrayList<TransitionBuildingPair> map1Pairs = transitionPairsMapping.get(map1).get(map2);
		for (TransitionBuildingPair pair: map1Pairs) {
			if (pair.getPairName().equals(pairName)) {
				//System.out.println("Found pair: " +pairName);
				map1Pairs.remove(pair);
				transitionPairsMapping.get(map2).get(map1).remove(pair);
				transitionPairNames.remove(pair.getPairName());
				
				allTransitionPairs.remove(pair);
				return;
			}
		}
		System.out.println("Pair not found");
	}
	
	public void save() {
		if (saved) return;
		saved = true;
		
		FileWriter writer;
		try {
			writer = new FileWriter(transitionBuildingTriggerFile);
			writer.write("#File is auto generated. Please do not edit.\n\n");
			
			//Group Trigger
			for (int currGroupTrigger = 0; currGroupTrigger < groupTriggers.length; ++currGroupTrigger) {
				writer.write("#Exiting to the "+directions[currGroupTrigger]+" of the "+(currGroupTrigger<2?"Horizontal":"Vertical")+" Transition Building\n");
				writer.write("GroupTrigger " +groupTriggers[currGroupTrigger].getName() +" {\n" + groupTriggers[currGroupTrigger].triggerDataAsString() + "}\n\n");
			}
			
			//Map Transition Triggers
			for (MapTransitionTrigger trig: transitionTriggers.values()){
				writer.write("MapTransitionTrigger " +trig.getName() +" {\n" + trig.triggerDataAsString() + "}\n\n");
			}
			
			
			//Info triggers
			GroupTrigger gt = new GroupTrigger(infoTriggerGT.getName(),"");
			
			for (TransitionBuildingPair pair: allTransitionPairs) {
				EventTrigger trig = pair.getInfoTrigger();
				gt.triggers.add(trig.getName());
				writer.write("EventTrigger " +trig.getName() +" {\n" + trig.triggerDataAsString() + "}\n\n");
			}
			
			writer.write("GroupTrigger " +gt.getName() +" {\n" + gt.triggerDataAsString() + "}\n\n");
			
			
			writer.close();
			
			
			//Write to dialog file
			File dialogFile = new File(mapMaker.root.getPath()+MapMaker.FILE_SLASH+TransitionBuildingDialogFile);
		
			//Create file if it doesn't exist
			if (!dialogFile.exists()) {
				dialogFile.getParentFile().mkdirs();
				dialogFile.createNewFile();
			}
			
			writer = new FileWriter(dialogFile);
			
			for (TransitionBuildingPair pair: allTransitionPairs) {
				writer.write(pair.getInfoDialog(mapMaker) +"\n\n");
			}
			
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public TransitionBuildingPair addIncompleteTransition(boolean horizontal, String map1, String map2, boolean isMap1Entrance, String mapEntrance) {

		saved = false;
		
		int pairNumber = 1;
		while (transitionPairNames.contains(TransitionBuildingPair.getPairName(horizontal, map1, map2, pairNumber))){
			++pairNumber;
		}
		
		TransitionBuildingPair pair = getTransitionPair(horizontal, map1, map2, pairNumber);
		String pairName = pair.getPairName();
		
		//System.out.println("Add Incomplete Pair name: " +pairName);
		
		incompletePairs.add(pair);
		
		int directionStartIndex = 0; //Horizontal
		
		if (!horizontal) {
			directionStartIndex = 2; //Vertical
		}
		
		String conditionString = "condition: " +pair.getConditionString()+"\n"
				+"global: !"+pair.getGlobalString();
		
		if (isMap1Entrance) {
			pair.map1Entrance = mapEntrance;
			
			groupTriggers[directionStartIndex].triggers.add(pairName +"_"+directions[directionStartIndex] +"Door");
			
			//Create map transition trigger
			MapTransitionTrigger map1Trigger = new MapTransitionTrigger(pairName+"_"+directions[directionStartIndex]+"Door", conditionString, map1, mapEntrance, -1);
			transitionTriggers.put(pair.map1 +"_"+pair.map1Entrance, map1Trigger);
		}
		else {
			pair.map2Entrance = mapEntrance;
			
			groupTriggers[directionStartIndex+1].triggers.add(pairName +"_"+directions[directionStartIndex+1] +"Door");
			
			//Create map transition trigger
			MapTransitionTrigger map2Trigger = new MapTransitionTrigger(pairName+"_"+directions[directionStartIndex+1]+"Door", conditionString, map2, mapEntrance, -1);
			transitionTriggers.put(pair.map2 +"_"+pair.map2Entrance, map2Trigger);
		}
		
		return pair;
	}
	
	public TransitionBuildingPair updateIncompleteTransition(TransitionBuildingPair pair, String otherMapEntrance) {
		
		saved = false;
		String pairName = pair.getPairName();
		
		//System.out.println("Update Pair name: " +pairName);
		
		incompletePairs.remove(pair);
		
		int directionStartIndex = 0; //Horizontal
		
		if (!pair.horizontal) {
			directionStartIndex = 2; //Vertical
		}
		
		String conditionString = "condition: " +pair.getConditionString()+"\n"
				+"global: !"+pair.getGlobalString();
		
		if (pair.map1Entrance == null) {
			pair.map1Entrance = otherMapEntrance;
			
			groupTriggers[directionStartIndex].triggers.add(pairName +"_"+directions[directionStartIndex] +"Door");
			
			//Create map transition trigger
			MapTransitionTrigger map1Trigger = new MapTransitionTrigger(pairName+"_"+directions[directionStartIndex]+"Door", conditionString, pair.map1, otherMapEntrance, -1);
			transitionTriggers.put(pair.map1 +"_"+pair.map1Entrance, map1Trigger);
		}
		else {
			pair.map2Entrance = otherMapEntrance;
			
			groupTriggers[directionStartIndex+1].triggers.add(pairName +"_"+directions[directionStartIndex+1] +"Door");
			
			//Create map transition trigger
			MapTransitionTrigger map2Trigger = new MapTransitionTrigger(pairName+"_"+directions[directionStartIndex+1]+"Door", conditionString, pair.map2, otherMapEntrance, -1);
			transitionTriggers.put(pair.map2 +"_"+pair.map2Entrance, map2Trigger);
		}
		
		return pair;
	}
	
	
	public TransitionBuildingPair updateIncompleteTransition(boolean horizontal, String map1, String map2, int pairNumber, String otherMapEntrance) {
		
		TransitionBuildingPair pair = getTransitionPair(horizontal, map1, map2, pairNumber);
		return updateIncompleteTransition(pair, otherMapEntrance);
	}
	
	public TransitionBuildingPair removeTransitionOnMap(boolean horizontal, String map1, String map2, int pairNumber, boolean removeMap1) {
	
		saved = false;
		
		TransitionBuildingPair pair = getTransitionPair(horizontal, map1, map2, pairNumber);
		String pairName = pair.getPairName();
		
		int directionStartIndex = 0; //Horizontal
		
		if (!pair.horizontal) {
			directionStartIndex = 2; //Vertical
		}
		
		if (removeMap1) {
			groupTriggers[directionStartIndex].triggers.remove(pairName +"_"+directions[directionStartIndex] +"Door");
			
			//Remove map transition trigger
			transitionTriggers.remove(pair.map1 +"_"+pair.map1Entrance);
			
			pair.map1Entrance = null;
			pair.area1 = 0;
		}
		else {
			groupTriggers[directionStartIndex+1].triggers.remove(pairName +"_"+directions[directionStartIndex+1] +"Door");
			
			//Remove map transition trigger
			transitionTriggers.remove(pair.map2 +"_"+pair.map2Entrance);
			
			pair.map2Entrance = null;
			pair.area2 = 0;
		}
		
		if (incompletePairs.contains(pair)){
			incompletePairs.remove(pair);
			removeTransition(pair);
		}
		else {
			incompletePairs.add(pair);
		}
		
		return pair;
	}
	
	
	public void removeTransition(TransitionBuildingPair pair) {
		
		removeTransitionPair(pair.horizontal, pair.map1, pair.map2, pair.pairNumber);
		
		String pairName = pair.getPairName();
		System.out.println("Removing "+pairName);
		
		transitionTriggers.remove(pair.map1 +"_"+pair.map1Entrance);
		transitionTriggers.remove(pair.map2 +"_"+pair.map2Entrance);
		
		if (pair.horizontal) {
			for (int currDir = 0; currDir < 2; ++currDir) {
				groupTriggers[currDir].triggers.remove(pairName +"_"+directions[currDir] +"Door");
			}
		}
		else {
			for (int currDir = 2; currDir < 4; ++currDir) {
				groupTriggers[currDir].triggers.remove(pairName +"_"+directions[currDir] +"Door");
			}
		}
	}
	
	//Full Remove
	public void removeTransition(boolean horizontal, String map1, String map2, int pairNumber) {
		
		saved = false;
		
		TransitionBuildingPair pair = getTransitionPair(horizontal, map1, map2, pairNumber);
		removeTransition(pair);
	}
	
	
	//Get a list of TransitionBuildingPairs that need to be placed on the given map.
	public TransitionBuildingPair[] getIncompleteTransitionPairsForMap(String mapName) {
		ArrayList<TransitionBuildingPair> list = new ArrayList<>();
		
		for (TransitionBuildingPair pair: incompletePairs) {
			if ((pair.map1.equals(mapName) && pair.map1Entrance == null) ||(pair.map2.equals(mapName) && pair.map2Entrance == null)) {
				list.add(pair);
			}
		}
		
		TransitionBuildingPair[] incompletePairArray = new TransitionBuildingPair[list.size()];
		return list.toArray(incompletePairArray);
	}
}
