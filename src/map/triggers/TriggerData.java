package map.triggers;

import gui.GameData;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

public class TriggerData {

	public static final Pattern integerRangePattern = Pattern.compile("\\d+(?:-\\d+)?");

	public String name;
	
	public ArrayList<point> points;
	
	public String triggerType;
	public String triggerContents;
	
	public TriggerData(String name, String contents) {
		this.name = name;
		
		points = new ArrayList<point>();
		
		Scanner in = new Scanner(contents);
		while (in.hasNext(integerRangePattern)){ 
			String[] xr = in.next().split("-");
			String[] yr = in.next().split("-");
			int x1, x2, y1, y2;
			x1 = Integer.parseInt(xr[0]);
			y1 = Integer.parseInt(yr[0]);
			x2 = xr.length == 2 ? Integer.parseInt(xr[1]) : x1;
			y2 = yr.length == 2 ? Integer.parseInt(yr[1]) : y1;
			for (int x = x1; x<=x2; x++)
				for (int y = y1; y<=y2; y++)
					points.add(new point(x, y));
		}
		
		triggerType = in.next();
		
		StringBuilder rest = new StringBuilder();
		while (in.hasNextLine()) {
			rest.append(in.nextLine()+"\n");
		}
		triggerContents = rest.toString();
		
		in.close();
		
		//System.out.println(name +"\n"+ triggerType +"\n" +triggerContents);
	}
	
	public void addPoint(int x, int y) {
		points.add(new point(x, y));
	}
	
	public void removePoint(int x, int y) {
		points.remove(new point(x, y));
	}
	
	public int[] getPoints(int width) {
		int[] pointsArray = new int[points.size()];
		
		for (int currPoint = 0; currPoint < pointsArray.length; ++currPoint) {
			point curr = points.get(currPoint);
			pointsArray[currPoint] = curr.y * width + curr.x;
		}
		return pointsArray;
	}
	
	public void updatePoints(int dx, int dy) {
		
		for (int currPoint = 0; currPoint < points.size(); ++currPoint) {
			point curr = points.get(currPoint);
			curr.x += dx;
			curr.y += dy;
		}
	}
	
	class point {
		public int x;
		public int y;
		
		public point (int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof point))
				return false;
			point p = (point) o;
			
			return p.x == x && p.y == y;
		}
	}

	public void addData(GameData gameData) {
		
		if (gameData.getTrigger(name) == null)
			gameData.addTrigger(triggerType, name, triggerContents);
	}
	
	public String triggerDataAsString() {
		
		StringBuilder ret = new StringBuilder();
		ret.append("TriggerData " +name +" {\n");
		
		for (point p: points) {
			ret.append("\t" +p.x +" " +p.y +"\n");
		}
		
		ret.append("\t" +triggerType +"\n");
		ret.append("\t"+ triggerContents.trim()+"\n");
		ret.append("}\n");
		
		return ret.toString();
		
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
