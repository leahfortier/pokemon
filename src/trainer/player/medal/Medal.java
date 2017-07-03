package trainer.player.medal;

public enum Medal {
    // Steps Taken
    LIGHT_WALKER("Light Walker", "A Medal to praise light walkers who took 5,000 steps."),
    MIDDLE_WALKER("Middle Walker", "A Medal to praise middle walkers who took 10,000 steps."),
    HEAVY_WALKER("Heavy Walker", "A Medal to praise heavy walkers who took 25,000 steps."),
    HONORED_FOOTPRINTS("Honored Footprints", "A Medal to praise ultimate walkers who took 100,000 steps."),

    // Medals Collected
    ROOKIE_MEDALIST("Rookie Medalist", "A Medal commemorating the advance to the Rookie Rank as the result of constant efforts in the Medal Rally."),
    ELITE_MEDALIST("Elite Medalist", "A Medal commemorating the advance to the Elite Rank as the result of constant efforts in the Medal Rally."),
    MASTER_MEDALIST("Master Medalist", "A Medal commemorating the advance to the Master Rank as the result of constant efforts in the Medal Rally."),
    LEGEND_MEDALIST("Legend Medalist", "A Medal commemorating the advance to the Legend Rank as the result of constant efforts in the Medal Rally."),
    TOP_MEDALIST("Top Medalist", "An honorable Medal for those who collected all the Medals.");

    private final String medalName;
    private final String description;

    Medal(String medalName, String description) {
        this.medalName = medalName;
        this.description = description;
    }

    public String getMedalName() {
        return this.medalName;
    }

    public String getDescription() {
        return this.description;
    }
}
