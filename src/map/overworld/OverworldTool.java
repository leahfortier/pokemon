package map.overworld;

public enum OverworldTool {
    FLY("canFly"),
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
