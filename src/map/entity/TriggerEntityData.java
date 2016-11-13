package map.entity;

import pattern.AreaDataMatcher.TriggerMatcher;

import java.util.List;

public class TriggerEntityData extends EntityData {
	private TriggerEntity entity;

	private List<EntityAction> actions;

	public TriggerEntityData(int x, int y, TriggerMatcher matcher) {
		super(matcher);

		this.x = x;
		this.y = y;
		this.actions = matcher.getActions();
	}
	
	public Entity getEntity() {
		if (entity == null) {
			entity = new TriggerEntity(name, x, y, actions);
		}

		return entity;
	}
}
