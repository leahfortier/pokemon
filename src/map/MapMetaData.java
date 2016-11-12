package map;

import java.awt.image.BufferedImage;

public class MapMetaData {

    private int[] bgTile;
    private int[] fgTile;
    private int[] walkMap;
    private int[] areaMap;

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
            if (this.suffix == null) {
                return null;
            }

            return mapName + this.suffix + ".png";
        }
    }
}
