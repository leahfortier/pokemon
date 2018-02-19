package map.triggers;

import main.Game;

public class GlobalTrigger extends Trigger {
    private final String global;

    public GlobalTrigger(String global) {
        super(global);
        this.global = global;
    }

    @Override
    public void execute() {
        if (global.startsWith("!")) {
            Game.getPlayer().removeGlobal(global.substring(1));
        } else {
            Game.getPlayer().addGlobal(global);
        }
    }
}
