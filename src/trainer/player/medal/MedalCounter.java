package trainer.player.medal;

import main.Game;

public class MedalCounter {
    private long count;
    private MedalThreshold[] medals;

    MedalCounter(MedalThreshold... medals) {
        this.count = 0;
        this.medals = medals;
    }

    void increase(int amount) {
        this.count += amount;

        for (MedalThreshold threshold : medals) {
            if (this.count > threshold.getThreshold()) {
                Game.getPlayer().getMedalCase().earnMedal(threshold.getMedal());
            }
        }
    }
}
