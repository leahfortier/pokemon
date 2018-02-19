package map.triggers;

import main.Game;
import map.condition.Condition;
import trainer.player.medal.MedalTheme;

public class MedalCountTrigger extends Trigger {
    private final MedalTheme medalTheme;

    MedalCountTrigger(String contents, Condition condition) {
        this(MedalTheme.valueOf(contents), condition);
    }

    public MedalCountTrigger(MedalTheme medalTheme, Condition condition) {
        super(medalTheme.name(), condition);
        this.medalTheme = medalTheme;
    }

    @Override
    protected void executeTrigger() {
        Game.getPlayer().getMedalCase().increase(medalTheme);
    }
}
