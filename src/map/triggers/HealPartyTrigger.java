package map.triggers;

import main.Game;

public class HealPartyTrigger extends Trigger {
    @Override
    public void execute() {
        Game.getPlayer().healAll();
    }
}
