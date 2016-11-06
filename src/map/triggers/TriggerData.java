package map.triggers;

import map.entity.npc.EntityAction;
import pattern.AreaDataMatcher.TriggerMatcher;

import java.util.List;

public class TriggerData {
	public String name;
	public String condition;
	private List<EntityAction> actions;

	public TriggerData(TriggerMatcher matcher) {
		this.name = matcher.name;
		this.condition = matcher.condition;
		this.actions = matcher.getActions();
	}

	// TODO: This is generic enough to be separate
	public static class Point {
		public int x;
		public int y;
		
		public Point (int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		public boolean equals(Object o) {
			if (!(o instanceof Point)) {
				return false;
			}
		
			Point p = (Point) o;
			
			return p.x == x && p.y == y;
		}
	}
}