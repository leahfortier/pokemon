package map.overworld;

public enum EncounterRate {
    VERY_COMMON(15),
    COMMON(12.75),
    SEMI_RARE(10.125),
    RARE(4.995),
    VERY_RARE(1.875);
    
    private final double rate;
    
    EncounterRate(double rate) {
        this.rate = rate;
    }
    
    public double getRate() {
        return this.rate;
    }
}
