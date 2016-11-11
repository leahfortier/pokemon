package mapMaker.data;

import map.triggers.EventTrigger;
import mapMaker.MapMaker;

public class TransitionBuildingPair implements Comparable<TransitionBuildingPair>{
	
	public boolean horizontal; //Horizontal or vertical
	
	public String map1; //North or East
	public String map1Entrance;
	public int area1;
	
	public String map2; //South or West
	public String map2Entrance;
	public int area2;
	
	public int pairNumber; //Used if more than one of these pairs exists
	
	public TransitionBuildingPair(boolean horizontal, String map1, String map2, int pairNumber) {
		this.horizontal = horizontal;
		
		this.map1 = map1;
		this.map2 = map2;
		
		this.pairNumber = pairNumber;
	}
	
	@Override
	public String toString() {
		return "TransitionBuildingPair:\n" +
				"Map1: " +map1 +" entrance: " + map1Entrance +" area: " +area1 +"\n" +
				"Map2: " +map2 +" entrance: " + map2Entrance +" area: " +area2 +"\n";
	}
	
	public String getGlobalString() {
		return "MapGlobal_TransitionPair" + String.format("%02d",pairNumber);
	}
	
	public String getConditionString() {
		
		int directionStartIndex = 0; //Horizontal
		
		if (!horizontal) {
			directionStartIndex = 2; //Vertical
		}
		
		return getGlobalString() + "&((MapGlobal_PreviousMap_" + map2 + "&MapGlobal_MapEntrance_"+ TransitionBuildingData.directions[directionStartIndex + 1] + "Door)|(MapGlobal_PreviousMap_" + map1 + "&MapGlobal_MapEntrance_"+ TransitionBuildingData.directions[directionStartIndex] + "Door))";
	}
	
	public String getPairName() {
		return "TransitionBuilding"+
				(horizontal?"H":"V")+
				"_between_"+
				map1 +
				"_and_"+
				map2 +
				"_pair_"+
				String.format("%02d", pairNumber)
				;
	}
	
	public static String getPairName(boolean horizontal, String map1, String map2, int pairNumber) {
		return "TransitionBuilding"+
				(horizontal?"H":"V")+
				"_between_"+
				map1 +
				"_and_"+
				map2 +
				"_pair_"+
				String.format("%02d", pairNumber)
				;
	}
	
	public EventTrigger getInfoTrigger() {
		return new EventTrigger(getInfoTriggerName(),
				"condition: " +getConditionString() +"\n" +
				"dialogue: " +getInfoDialogName()
				);
	}
	
	public String getInfoTriggerName() {
		return "TransitionBuilding"+
				(horizontal?"H":"V")+
				"_InformationDesk_between_map1_" +
				map1+
				"_area1_"+
				Integer.toHexString(area1)+
				"_and_map2_"+
				map2+
				"_area2_"+
				Integer.toHexString(area2)+
				"_pair_"+
				String.format("%02d", pairNumber)
				;
	}
	
	public String getInfoDialogue(MapMaker mapMaker) {
		
		return "Dialogue " + getInfoDialogName() +" {\n"+
				"\ttext: \"" +getInfoDialogString(mapMaker) +"\"\n"+
				"}";
	}
	
	public String getInfoDialogName() {
		return getInfoTriggerName() + "_Dialogue";
	}
	
	public String getInfoDialogString(MapMaker mapMaker) {

		int directionStartIndex = 0; //Horizontal
		
		if (!horizontal) {
			directionStartIndex = 2; //Vertical
		}

		// yeah removed the shit because this is almost gone sue me sue me sue me I don't give a fuck
		return "To the " +TransitionBuildingData.directions[directionStartIndex].toLowerCase() + " is and to the " + TransitionBuildingData.directions[directionStartIndex + 1].toLowerCase() + ".";
	}
	
	//Sort first by map1, then by map2, then by pair number
	@Override
	public int compareTo(TransitionBuildingPair o) {
		
		int ret = map1.compareTo(o.map1);
		
		if (ret == 0) {
			ret = map2.compareTo(o.map2);
		}
		
		if (ret == 0){
			ret = pairNumber - o.pairNumber;
		}
		
		return ret;
	}
}
