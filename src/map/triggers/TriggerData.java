package map.triggers;

import main.Game;
import pattern.AreaDataMatcher.TriggerDataMatcher;
import util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class TriggerData {
	private static final Pattern integerRangePattern = Pattern.compile("\\d+(?:-\\d+)?");
	
	public TriggerType triggerType;
	public String triggerContents;

	public List<Point> location;

	public String condition;
	public String[] globals;

	public TriggerData(TriggerDataMatcher matcher) {
		this.triggerType = matcher.getTriggerType();
		this.triggerContents = matcher.triggerContents;
		this.location = matcher.getLocation();
	}
	
	public void addPoint(int x, int y) {
		this.location.add(new Point(x, y));
	}
	
	public void removePoint(int x, int y) {
		this.location.remove(new Point(x, y));
	}

	// TODO: Use that one method
	public int[] getPoints(int width) {
		int[] pointsArray = new int[this.location.size()];
		for (int currPoint = 0; currPoint < pointsArray.length; currPoint++) {
			Point curr = this.location.get(currPoint);
			pointsArray[currPoint] = curr.y*width + curr.x;
		}
		
		return pointsArray;
	}
	
	public void updatePoints(int dx, int dy) {
		for (Point curr : this.location) {
			curr.x += dx;
			curr.y += dy;
		}
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

	public void addData() {
		Game.getData().addTrigger(triggerType, triggerContents);
	}
}

/*


TriggerData triggerName {
	x1-x2 y1-y2
	...
	
	TriggerType
	trigger contents
	
}
*/
