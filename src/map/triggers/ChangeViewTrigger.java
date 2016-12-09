package map.triggers;

import main.Game;
import gui.view.ViewMode;
import util.PokeString;

class ChangeViewTrigger extends Trigger {
	private final ViewMode view;

	ChangeViewTrigger(String viewMode, String condition) {
		super(TriggerType.CHANGE_VIEW, viewMode, condition);

		this.view = ViewMode.valueOf(PokeString.getNamesiesString(viewMode));
	}

	protected void executeTrigger() {
		Game.instance().setViewMode(this.view);
	}
}
