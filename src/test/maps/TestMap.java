package test.maps;

import map.AreaData;
import map.MapData;

import java.io.File;

public class TestMap extends MapData {
    public TestMap(File file) {
        super(file);
    }

    public AreaData[] getAreas() {
        return this.areaData;
    }
}
