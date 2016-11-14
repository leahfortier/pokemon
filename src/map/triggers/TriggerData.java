package map.triggers;


import map.entity.EntityAction;
import pattern.EventMatcher;

import java.util.List;

public class TriggerData {
	public String name;
	public String condition;
	private List<EntityAction> actions;

	public TriggerData(EventMatcher matcher) {
		this.name = matcher.getTriggerName();
		this.condition = matcher.getCondition();
		this.actions = matcher.getActions();
	}
}