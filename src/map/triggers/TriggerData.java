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
	
	public List<Point> points;
	
	public TriggerType triggerType;
	public String triggerContents;

	public TriggerData(TriggerDataMatcher matcher) {
		this.triggerType = matcher.getTriggerType();
		this.triggerContents = matcher.triggerContents;
		this.points = matcher.getLocation();
	}

	public TriggerData(String name, String contents) {
		points = new ArrayList<>();
		
		Scanner in = new Scanner(contents);
		while (in.hasNext(integerRangePattern)) {
			String[] xr = in.next().split("-");
			String[] yr = in.next().split("-");
		
			int x1 = Integer.parseInt(xr[0]);
			int y1 = Integer.parseInt(yr[0]);

			int x2 = xr.length == 2 ? Integer.parseInt(xr[1]) : x1;
			int y2 = yr.length == 2 ? Integer.parseInt(yr[1]) : y1;
			
			for (int x = x1; x<=x2; x++) {
				for (int y = y1; y<=y2; y++) {
					points.add(new Point(x, y));
				}
			}
		}
		
		triggerType = TriggerType.getTriggerType(in.next());
		
		StringBuilder rest = new StringBuilder();
		while (in.hasNextLine()) {
			StringUtils.appendLine(rest, in.nextLine());
		}
		
		triggerContents = rest.toString();
		
		in.close();
		
		//System.out.println(name +"\n"+ triggerType +"\n" +triggerContents);
	}
	
	public void addPoint(int x, int y) {
		points.add(new Point(x, y));
	}
	
	public void removePoint(int x, int y) {
		points.remove(new Point(x, y));
	}

	// TODO: Use that one method
	public int[] getPoints(int width) {
		int[] pointsArray = new int[points.size()];
		for (int currPoint = 0; currPoint < pointsArray.length; currPoint++) {
			Point curr = points.get(currPoint);
			pointsArray[currPoint] = curr.y*width + curr.x;
		}
		
		return pointsArray;
	}
	
	public void updatePoints(int dx, int dy) {

		for (Point curr : points) {
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
