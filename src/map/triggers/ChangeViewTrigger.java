package map.triggers;

import java.util.regex.Matcher;

import main.Game;
import main.Game.ViewMode;

public class ChangeViewTrigger extends Trigger {
	private String view;
	
	public ChangeViewTrigger(String name, String contents) {
		super(name, contents);
		Matcher m = variablePattern.matcher(contents);		
		if (m.find() && m.group(1).equals("view")) {
			view = m.group(2);
		}
	}

	public void execute(Game game) {
		super.execute(game);
		game.setViewMode(ViewMode.valueOf(view));
	}
}
