package mapMaker.data;

import pattern.MultiPointEntityMatcher;
import pattern.SinglePointEntityMatcher;

public class PlaceableTrigger {

	public enum PlaceableTriggerType {
		ENTITY,
		TRIGGER_DATA
	}
	
	public PlaceableTriggerType triggerType;

	public String name;
	public Integer location;
	
	public SinglePointEntityMatcher entity;
	public MultiPointEntityMatcher triggerData;

	public PlaceableTrigger(SinglePointEntityMatcher entity, Integer location) {
		this(entity);
		this.location = location;
	}

	public PlaceableTrigger(SinglePointEntityMatcher entity) {
		this.triggerType = PlaceableTriggerType.ENTITY;
		this.entity = entity;
		this.name = entity.getBasicName();
	}

	public PlaceableTrigger(MultiPointEntityMatcher triggerData) {
		this.triggerType = PlaceableTriggerType.TRIGGER_DATA;
		this.triggerData = triggerData;
		this.name = triggerData.getBasicName();
	}
}
