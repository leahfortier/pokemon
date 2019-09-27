package trainer;

public enum TrainerType {
    PLAYER,
    OPPONENT,
    WILD;

    public boolean isPlayer() {
        return this == PLAYER;
    }

    public boolean isWild() {
        return this == WILD;
    }
}
