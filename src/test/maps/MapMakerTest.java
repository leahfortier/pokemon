package test.maps;

import mapMaker.MapMaker;
import mapMaker.tools.Tool;
import mapMaker.tools.Tool.ToolType;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import test.general.BaseTest;
import util.file.FileIO;
import util.file.Folder;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class MapMakerTest extends BaseTest {
    private static MapMaker mapMaker;

    @BeforeClass
    public static void setup() {
        mapMaker = new MapMaker();
    }

    @Test
    public void mapsTest() {
        File mapsDirectory = FileIO.newFile(Folder.MAPS);
        for (File regionFolder : FileIO.listDirectories(mapsDirectory)) {
            String regionName = regionFolder.getName();
            for (File mapFolder : FileIO.listDirectories(regionFolder)) {
                String mapName = mapFolder.getName();
                mapMaker.loadMap(regionName, mapName);
                Assert.assertFalse(regionName + " -- " + mapName, mapMaker.hasUnsavedChanges());
            }
        }
    }

    @Test
    public void toolTest() {
        Set<Integer> keyEvents = new HashSet<>();
        for (ToolType toolType : ToolType.values()) {
            // Make sure all key events are unique
            int keyEvent = toolType.getKeyEvent();
            Assert.assertFalse(keyEvents.contains(keyEvent));
            keyEvents.add(keyEvent);

            // Make sure the tool created from the tool type has the same corresponding tool type
            Tool tool = toolType.createTool(mapMaker);
            Assert.assertEquals(toolType, tool.getToolType());
        }
    }
}
