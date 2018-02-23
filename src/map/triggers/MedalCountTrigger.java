package map.triggers;

import main.Game;
import trainer.player.medal.MedalTheme;

public class MedalCountTrigger extends Trigger {
    private final MedalTheme medalTheme;

    public MedalCountTrigger(MedalTheme medalTheme) {
        this.medalTheme = medalTheme;
    }

    @Override
    public void execute() {
        Game.getPlayer().getMedalCase().increase(medalTheme);
    }
}
