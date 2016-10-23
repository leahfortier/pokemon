package map;

public enum EncounterRate {
    VERY_COMMON(15),
    COMMON(12.75),
    SEMI_RARE(10.125),
    RARE(4.995),
    VERY_RARE(1.875);

    private double rate;

    EncounterRate(double rate) {
        this.rate = rate;
    }

    // TODO: I still have no idea how lambda function shit works but maybe those can do something about silly loops like these?
    public static final String[] ENCOUNTER_RATE_NAMES = new String[EncounterRate.values().length];
    static {
        for (int i = 0; i < ENCOUNTER_RATE_NAMES.length; i++) {
            ENCOUNTER_RATE_NAMES[i] = EncounterRate.values()[i].name();
        }
    }

    public double getRate() {
        return this.rate;
    }
}
