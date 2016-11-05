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

	public TriggerEntityData(TriggerMatcher matcher) {
		super(matcher.name, matcher.condition);

		x = matcher.x;
		y = matcher.y;
		actions = matcher.getActions();
	}
	
	public Entity getEntity() {
		if (entity == null) {
			entity = new TriggerEntity(name, x, y, actions);
		}

		return entity;
	}
}
