package mapMaker.data;

import map.entity.EntityData;
import map.triggers.TriggerData;

public class PlaceableTrigger {

	public static enum TriggerType{
		Entity, OldTrigger, MapEntrance, TriggerData
	};
	
	
	public TriggerType triggerType;
	
	public String name;
	public Integer location;
	
	public EntityData entity;
	public TriggerData triggerData;
	
	public TransitionBuildingPair transitionBuildingPair;
	
	//OldTrigger, MapEntrance
	public PlaceableTrigger(TriggerType triggerType, String name, Integer location){
		this.triggerType = triggerType;
		this.name = name;
		this.location = location;
	}
	
	//OldTrigger, MapEntrance
	public PlaceableTrigger(TriggerType triggerType, String name){
		this.triggerType = triggerType;
		this.name = name;
	}
	
	
	//NPCEntity, ItemEntity, TriggerEntity
	public PlaceableTrigger(EntityData entity, Integer location) {
		this.triggerType = TriggerType.Entity;
		this.entity = entity;
		
		name = entity.name;
		this.location = location;
	}
	//NPCEntity, ItemEntity, TriggerEntity
	public PlaceableTrigger(EntityData entity) {
		this.triggerType = TriggerType.Entity;
		this.entity = entity;
		
		name = entity.name;
	}
	
	
	
	//MapExit, WildBattle, TriggerData
	public PlaceableTrigger(TriggerData triggerData, Integer location) {
		this.triggerType = TriggerType.TriggerData;
		this.triggerData = triggerData;
		
		name = triggerData.name;
		this.location = location;
	}
	
	//MapExit, WildBattle, TriggerData
	public PlaceableTrigger(TriggerData triggerData) {
		this.triggerType = TriggerType.TriggerData;
		this.triggerData = triggerData;
		
		name = triggerData.name;
	}
}
