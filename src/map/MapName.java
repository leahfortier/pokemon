package map;

import java.io.Serializable;

public class MapName implements Serializable {
    private String region;
    private String mapName;

    public MapName(String region, String mapName) {
        this.region = region;
        this.mapName = mapName;
    }

    public String getRegionName() {
        return this.region;
    }

    public String getMapName() {
        return this.mapName;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof MapName)) {
            return false;
        }

        MapName that = (MapName)other;
        return this.mapName.equals(that.mapName) && this.region.equals(that.region);
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public String toString() {
        return region + " -- " + mapName;
    }
}
