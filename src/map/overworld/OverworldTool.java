package map.overworld;

public enum OverworldTool {
    TELEPORT("canTeleport"),
    SURF("canSurf"),
    FISH("canFish");

    private final String globalName;

    OverworldTool(String globalName) {
        this.globalName = globalName;
    }

    public String getGlobalName() {
        return this.globalName;
    }
}
