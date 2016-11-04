package map.triggers;

import main.Game;
import main.Global;
import util.StringUtils;

public class SetLastPokeCenterTrigger extends Trigger {

	SetLastPokeCenterTrigger(String contents)	{
		super(TriggerType.SET_LAST_POKE_CENTER, contents);

		if (!StringUtils.isNullOrEmpty(contents)) {
			Global.error("Contents should be empty for SetLastPokeCenterTrigger");
		}
	}

	protected void executeTrigger() {
		Game.getPlayer().setPokeCenter();
	}
}
