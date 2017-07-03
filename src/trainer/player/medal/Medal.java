package trainer.player.medal;

public enum Medal {
    // Steps Taken
    LIGHT_WALKER(5000, "Light Walker", "A Medal to praise light walkers who took 5,000 steps."),
    MIDDLE_WALKER(10000, "Middle Walker", "A Medal to praise middle walkers who took 10,000 steps."),
    HEAVY_WALKER(25000, "Heavy Walker", "A Medal to praise heavy walkers who took 25,000 steps."),
    HONORED_FOOTPRINTS(100000, "Honored Footprints", "A Medal to praise ultimate walkers who took 100,000 steps."),

    // Times Saved
    STEP_BY_STEP_SAVER(10, "Step-by-Step Saver", "A Medal to recognize a newly started journey that has been recorded 10 times."),
    BUSY_SAVER(20, "Busy Saver", "A Medal to recognize an energetic journey that has been recorded 20 times."),
    EXPERIENCED_SAVER(50, "Experienced Saver", "A Medal to recognize a smooth journey that has been recorded 50 times."),
    WONDER_WRITER(100, "Wonder Writer", "A Medal to recognize an astonishing journey that has been recorded 100 times"),

    // Medals Collected
    ROOKIE_MEDALIST(50, "Rookie Medalist", "A Medal commemorating the advance to the Rookie Rank as the result of constant efforts in the Medal Rally."),
    ELITE_MEDALIST(100, "Elite Medalist", "A Medal commemorating the advance to the Elite Rank as the result of constant efforts in the Medal Rally."),
    MASTER_MEDALIST(150, "Master Medalist", "A Medal commemorating the advance to the Master Rank as the result of constant efforts in the Medal Rally."),
    LEGEND_MEDALIST(200, "Legend Medalist", "A Medal commemorating the advance to the Legend Rank as the result of constant efforts in the Medal Rally."),
    TOP_MEDALIST(-1, "Top Medalist", "An honorable Medal for those who collected all the Medals.");

    static {
        TOP_MEDALIST.threshold = values().length - 1;
    }

    private final String medalName;
    private final String description;
    private int threshold;

    Medal(String medalName, String description) {
        this(-1, medalName, description);
    }

    Medal(int threshold, String medalName, String description) {
        this.threshold = threshold;
        this.medalName = medalName;
        this.description = description;
    }

    public String getMedalName() {
        return this.medalName;
    }

    public String getDescription() {
        return this.description;
    }

    public int getThreshold() {
        return this.threshold;
    }
}
