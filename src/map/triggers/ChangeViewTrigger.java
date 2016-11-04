package map.triggers;

import main.Game;
import main.Game.ViewMode;
import util.PokeString;

public class ChangeViewTrigger extends Trigger {
	private ViewMode view;

	ChangeViewTrigger(String viewMode) {
		super(TriggerType.CHANGE_VIEW, viewMode);

		this.view = ViewMode.valueOf(PokeString.getNamesiesString(viewMode));
	}

	protected void executeTrigger() {
		Game.setViewMode(this.view);
	}
}
