package test.maps;

import map.MapData;
import map.area.AreaData;
import pattern.map.MapDataMatcher;
import util.file.FileIO;

import java.io.File;

public class TestMap extends MapData {
    private final MapDataMatcher matcher;

    public TestMap(File mapFolder) {
        super(mapFolder);

        String beginFilePath = FileIO.makeFolderPath(mapFolder.getPath());
        this.matcher = MapDataMatcher.matchArea(beginFilePath + this.getName().getMapName() + ".txt");
    }

    public MapDataMatcher getMatcher() {
        return this.matcher;
    }

    public AreaData[] getAreas() {
        return this.matcher.getAreaData();
    }
}
