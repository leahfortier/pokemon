package map.triggers;

import main.Game;

public class HealPartyTrigger extends Trigger {
    public HealPartyTrigger() {
        super(null);
    }

    @Override
    public void execute() {
        Game.getPlayer().healAll();
    }
}
