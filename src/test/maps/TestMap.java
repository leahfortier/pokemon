package test.maps;

import map.MapData;
import map.area.AreaData;
import pattern.map.MapDataMatcher;
import util.FileIO;

import java.io.File;

public class TestMap extends MapData {
    private MapDataMatcher matcher;

    public TestMap(File mapFile) {
        super(mapFile);

        String beginFilePath = FileIO.makeFolderPath(mapFile.getPath());
        this.matcher = MapDataMatcher.matchArea(beginFilePath + getName().getMapName() + ".txt");
    }

    public MapDataMatcher getMatcher() {
        return this.matcher;
    }

    public AreaData[] getAreas() {
        return this.matcher.getAreaData();
    }
}
