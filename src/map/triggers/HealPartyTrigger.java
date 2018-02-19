package map.triggers;

import main.Game;

public class HealPartyTrigger extends Trigger {
    public HealPartyTrigger() {
        super(null);
    }

    @Override
    protected void executeTrigger() {
        Game.getPlayer().healAll();
    }
}
