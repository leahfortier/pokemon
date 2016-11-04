package map.triggers;

import main.Game;
import main.Global;
import util.StringUtils;

public class LastPokeCenterTrigger extends Trigger {

	LastPokeCenterTrigger(String contents)	{
		super(TriggerType.LAST_POKE_CENTER, contents);

		if (!StringUtils.isNullOrEmpty(contents)) {
			Global.error("Contents should be empty for LastPokeCenterTrigger");
		}
	}

	protected void executeTrigger() {
		Game.getPlayer().setPokeCenter();
	}
}
