package map.triggers;


import map.entity.EntityAction;
import pattern.AreaDataMatcher.TriggerMatcher;

import java.util.List;

public class TriggerData {
	public String name;
	public String condition;
	private List<EntityAction> actions;

	public TriggerData(TriggerMatcher matcher) {
		this.name = matcher.getTriggerName();
		this.condition = matcher.getCondition();
		this.actions = matcher.getActions();
	}
}