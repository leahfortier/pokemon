package mapMaker.data;

import pattern.AreaDataMatcher.MapMakerEntityMatcher;
import pattern.AreaDataMatcher.TriggerMatcher;

public class PlaceableTrigger {

	public enum PlaceableTriggerType {
		ENTITY,
		TRIGGER_DATA
	}
	
	public PlaceableTriggerType triggerType;

	public String name;
	public Integer location;
	
	public MapMakerEntityMatcher entity;
	public TriggerMatcher triggerData;

	public PlaceableTrigger(MapMakerEntityMatcher entity, Integer location) {
		this(entity);
		this.location = location;
	}

	public PlaceableTrigger(MapMakerEntityMatcher entity) {
		this.triggerType = PlaceableTriggerType.ENTITY;
		this.entity = entity;
		this.name = entity.getBasicName();
	}

	public PlaceableTrigger(TriggerMatcher triggerData) {
		this.triggerType = PlaceableTriggerType.TRIGGER_DATA;
		this.triggerData = triggerData;
		this.name = triggerData.getBasicName();
	}
}
