package mapMaker.data;

import pattern.AreaDataMatcher.EntityMatcher;
import pattern.AreaDataMatcher.TriggerMatcher;

public class PlaceableTrigger {

	public enum PlaceableTriggerType {
		ENTITY,
		TRIGGER_DATA
	}
	
	public PlaceableTriggerType triggerType;
	
	public String name;
	public Integer location;
	
	public EntityMatcher entity;
	public TriggerMatcher triggerData;

	public PlaceableTrigger(EntityMatcher entity, Integer location) {
		this(entity);
		this.location = location;
	}

	public PlaceableTrigger(EntityMatcher entity) {
		this.triggerType = PlaceableTriggerType.ENTITY;
		this.entity = entity;
		this.name = entity.getName();
	}

	public PlaceableTrigger(TriggerMatcher triggerData) {
		this.triggerType = PlaceableTriggerType.TRIGGER_DATA;
		this.triggerData = triggerData;
		this.name = triggerData.getName();
	}
}
