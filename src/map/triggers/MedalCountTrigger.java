package map.triggers;

import main.Game;
import trainer.player.medal.MedalTheme;

public class MedalCountTrigger extends Trigger {
    private final MedalTheme medalTheme;

    MedalCountTrigger(String contents, String condition) {
        super(TriggerType.MEDAL_COUNT, contents, condition);

        this.medalTheme = MedalTheme.valueOf(contents);
    }

    @Override
    protected void executeTrigger() {
        Game.getPlayer().getMedalCase().increase(medalTheme);
    }
}
