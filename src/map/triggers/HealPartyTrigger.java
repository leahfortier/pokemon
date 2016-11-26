package map.triggers;

import main.Game;
import main.Global;
import util.StringUtils;

class HealPartyTrigger extends Trigger {

	HealPartyTrigger(String contents, String condition)	{
		super(TriggerType.HEAL_PARTY, contents, condition);

		if (!StringUtils.isNullOrEmpty(contents)) {
			Global.error("Contents should be empty for HealPartyTrigger");
		}
	}

	@Override
	protected void executeTrigger() {
		Game.getPlayer().healAll();
	}
}
