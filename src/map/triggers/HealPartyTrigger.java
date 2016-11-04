package map.triggers;

import main.Game;
import main.Global;
import util.StringUtils;

public class HealPartyTrigger extends Trigger {

	HealPartyTrigger(String contents)	{
		super(TriggerType.HEAL_PARTY, contents);

		if (!StringUtils.isNullOrEmpty(contents)) {
			Global.error("Contents should be empty for HealPartyTrigger");
		}
	}

	@Override
	protected void executeTrigger() {
		Game.getPlayer().healAll();
	}
}
