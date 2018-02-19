package map.triggers;

import main.Game;
import main.Global;
import map.condition.Condition;
import util.StringUtils;

public class HealPartyTrigger extends Trigger {

    HealPartyTrigger(String contents, Condition condition) {
        this(condition);
        if (!StringUtils.isNullOrEmpty(contents)) {
            Global.error("Contents should be empty for " + this.getClass().getSimpleName());
        }
    }

    public HealPartyTrigger(Condition condition) {
        super(null, condition);
    }

    @Override
    protected void executeTrigger() {
        Game.getPlayer().healAll();
    }
}
