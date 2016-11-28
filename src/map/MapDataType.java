package map;

public enum MapDataType {
    BACKGROUND("_bg"),
    FOREGROUND("_fg"),
    MOVE("_move"),
    AREA("_area");

    private final String suffix;

    MapDataType(String suffix) {
        this.suffix = suffix;
    }

    public String getImageName(String mapName) {
        return mapName + this.suffix + ".png";
    }
}
