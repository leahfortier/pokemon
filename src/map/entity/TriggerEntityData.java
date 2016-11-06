package map.entity;

import map.entity.npc.EntityAction;
import pattern.AreaDataMatcher.ActionMatcher;
import pattern.AreaDataMatcher.TriggerMatcher;
import util.StringUtils;

import java.util.List;
import java.util.regex.Matcher;

public class TriggerEntityData extends EntityData {
	private TriggerEntity entity;

	private List<EntityAction> actions;

	public TriggerEntityData(int x, int y, TriggerMatcher matcher) {
		super(matcher.name, matcher.condition);

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
