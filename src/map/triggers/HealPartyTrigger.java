package map.triggers;

import main.Game;
import main.Global;
import map.condition.Condition;
import util.StringUtils;

class HealPartyTrigger extends Trigger {

    HealPartyTrigger(String contents, Condition condition) {
        super(TriggerType.HEAL_PARTY, contents, condition);

        if (!StringUtils.isNullOrEmpty(contents)) {
            Global.error("Contents should be empty for " + this.getClass().getSimpleName());
        }
    }

    @Override
    protected void executeTrigger() {
        Game.getPlayer().healAll();
    }
}
