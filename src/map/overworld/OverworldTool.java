package map.overworld;

public enum OverworldTool {
    BIKE("hasBicycle"),
    FISH("canFish"),
    FLY("canFly"),
    SURF("canSurf"),
    ITEM_FINDER("hasItemFinder"),
    POKEFINDER("hasPokefinder");

    private final String globalName;

    OverworldTool(String globalName) {
        this.globalName = globalName;
    }

    public String getGlobalName() {
        return this.globalName;
    }
}
