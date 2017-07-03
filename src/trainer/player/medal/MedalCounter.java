package trainer.player.medal;

import main.Game;

public class MedalCounter {
    private long count;
    private Medal[] medals;

    MedalCounter(Medal... medals) {
        this.count = 0;
        this.medals = medals;
    }

    void increase(int amount) {
        this.count += amount;

        for (Medal medal : medals) {
            if (this.count > medal.getThreshold()) {
                Game.getPlayer().getMedalCase().earnMedal(medal);
            }
        }
    }
}
