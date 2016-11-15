package map.entity;

import pattern.map.MiscEntityMatcher;
import util.Point;

import java.util.List;

public class TriggerEntityData extends EntityData {
	private TriggerEntity entity;

	private List<EntityAction> actions;

	public TriggerEntityData(MiscEntityMatcher matcher) {
		super(matcher);

		Point location = matcher.getLocation();
		this.x = location.x;
		this.y = location.y;
		this.actions = matcher.getActions();
	}
	
	public Entity getEntity() {
		if (entity == null) {
			entity = new TriggerEntity(name, x, y, actions);
		}

		return entity;
	}
}
