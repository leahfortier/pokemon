package trainer.player.medal;

import main.Game;

import java.io.Serializable;

public class MedalCounter implements Serializable {
    private long count;
    private Medal[] medals;

    MedalCounter(Medal... medals) {
        this.count = 0;
        this.medals = medals;
    }

    private void checkThreshold() {
        for (Medal medal : medals) {
            if (this.count > medal.getThreshold()) {
                Game.getPlayer().getMedalCase().earnMedal(medal);
            }
        }
    }

    public void update(int count) {
        this.count = count;
        this.checkThreshold();
    }

    public void increase(int amount) {
        this.count += amount;
        this.checkThreshold();
    }
}
