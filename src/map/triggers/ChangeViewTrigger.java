package map.triggers;

import main.Game;
import gui.view.ViewMode;
import util.PokeString;

public class ChangeViewTrigger extends Trigger {
	private ViewMode view;

	ChangeViewTrigger(String viewMode, String condition) {
		super(TriggerType.CHANGE_VIEW, viewMode, condition);

		this.view = ViewMode.valueOf(PokeString.getNamesiesString(viewMode));
	}

	protected void executeTrigger() {
		Game.setViewMode(this.view);
	}
}
