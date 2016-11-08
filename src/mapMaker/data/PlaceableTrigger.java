package mapMaker.data;

import pattern.AreaDataMatcher.EntityMatcher;
import pattern.AreaDataMatcher.TriggerMatcher;

public class PlaceableTrigger {

	// TODO: CAPS
	public enum PlaceableTriggerType {
		Entity,
		OldTrigger,
		MapEntrance,
		TriggerData
	}
	
	public PlaceableTriggerType triggerType;
	
	public String name;
	public Integer location;
	
	public EntityMatcher entity;
	public TriggerMatcher triggerData;

	//OldTrigger, MapEntrance
	public PlaceableTrigger(PlaceableTriggerType triggerType, String name, Integer location){
		this(triggerType, name);
		this.location = location;
	}
	
	//OldTrigger, MapEntrance
	public PlaceableTrigger(PlaceableTriggerType triggerType, String name){
		this.triggerType = triggerType;
		this.name = name;
	}
	
	//NPCEntity, ItemEntity, TriggerEntity
	public PlaceableTrigger(EntityMatcher entity, Integer location) {
		this(entity);
		this.location = location;
	}

	//NPCEntity, ItemEntity, TriggerEntity
	public PlaceableTrigger(EntityMatcher entity) {
		this.triggerType = PlaceableTriggerType.Entity;
		this.entity = entity;
		this.name = entity.getName();
	}
	
	//MapExit, WildBattle, TriggerData
	public PlaceableTrigger(TriggerMatcher triggerData, Integer location) {
		this(triggerData);
		this.location = location;
	}
	
	//MapExit, WildBattle, TriggerData
	public PlaceableTrigger(TriggerMatcher triggerData) {
		this.triggerType = PlaceableTriggerType.TriggerData;
		this.triggerData = triggerData;
		this.name = triggerData.getName();
	}
}
