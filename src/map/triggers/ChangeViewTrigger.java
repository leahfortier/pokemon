package map.triggers;

import main.Game;
import main.Game.ViewMode;

import java.util.regex.Matcher;

public class ChangeViewTrigger extends Trigger {
	private String view;
	
	public ChangeViewTrigger(String name, String contents) {
		super(name, contents);
		Matcher m = variablePattern.matcher(contents);		
		if (m.find() && m.group(1).equals("view")) {
			view = m.group(2);
		}
	}

	public void execute() {
		super.execute();
		Game.setViewMode(ViewMode.valueOf(view));
	}
}
