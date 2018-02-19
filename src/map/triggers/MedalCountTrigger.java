package map.triggers;

import main.Game;
import trainer.player.medal.MedalTheme;

public class MedalCountTrigger extends Trigger {
    private final MedalTheme medalTheme;

    public MedalCountTrigger(MedalTheme medalTheme) {
        super(medalTheme.name());
        this.medalTheme = medalTheme;
    }

    @Override
    protected void executeTrigger() {
        Game.getPlayer().getMedalCase().increase(medalTheme);
    }
}
