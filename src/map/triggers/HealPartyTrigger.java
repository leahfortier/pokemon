package map.triggers;

import main.Game;
import map.condition.Condition;

public class HealPartyTrigger extends Trigger {
    public HealPartyTrigger(Condition condition) {
        super(null, condition);
    }

    @Override
    protected void executeTrigger() {
        Game.getPlayer().healAll();
    }
}
