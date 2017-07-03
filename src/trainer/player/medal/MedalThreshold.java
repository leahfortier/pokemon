package trainer.player.medal;

class MedalThreshold {
    private final Medal medal;
    private final int threshold;

    MedalThreshold(Medal medal, int threshold) {
        this.medal = medal;
        this.threshold = threshold;
    }

    Medal getMedal() {
        return this.medal;
    }

    int getThreshold() {
        return this.threshold;
    }
}
