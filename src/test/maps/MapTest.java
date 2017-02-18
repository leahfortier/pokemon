package test.maps;

import map.AreaData;
import map.MapDataType;
import map.WalkType;
import map.WildEncounter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pattern.map.FishingMatcher;
import pattern.map.WildBattleMatcher;
import util.FileIO;
import util.Folder;
import util.Point;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MapTest {
    private List<TestMap> maps;

    @Before
    public void loadMaps() {
        maps = new ArrayList<>();

        File mapsDirectory = new File(Folder.MAPS);
        for (File mapFolder : FileIO.listSubdirectories(mapsDirectory)) {
            maps.add(new TestMap(mapFolder));
        }
    }

    @Test
    public void flyLocationTest() {
        for (TestMap map : maps) {
            for (AreaData area : map.getAreas()) {
                if (area.isFlyLocation()) {
                    Assert.assertTrue(
                            "Invalid fly location " + area.getFlyLocation() + " for map " + map.getName(),
                            map.hasEntrance(area.getFlyLocation())
                    );
                }
            }
        }
    }

    @Test
    public void multipleAreaTest() {
        for (TestMap map : maps) {
            AreaData[] areas = map.getAreas();

            // Make sure each map has at least one area
            Assert.assertTrue(areas.length > 0);

            // Only testing maps with multiple areas
            if (areas.length == 1) {
                continue;
            }

            // Color is required for maps with multiple areas
            for (AreaData area : areas) {
                Assert.assertTrue(
                        "Missing color for area " + area.getAreaName() + " in map " + map.getName(),
                        area.hasColor()
                );
            }

            // Confirm each walkable tile is not in the void area
            Dimension dimension = map.getDimension();
            for (int x = 0; x < dimension.width; x++) {
                for (int y = 0; y < dimension.height; y++) {
                    int rgb = map.getRGB(x, y, MapDataType.MOVE);
                    WalkType walkType = WalkType.getWalkType(rgb);
                    if (walkType != WalkType.NOT_WALKABLE) {
                        Assert.assertFalse(
                                map.getName() + " " + x + " " + y,
                                map.getArea(new Point(x, y)) == AreaData.VOID);
                    }
                }
            }
        }
    }

    @Test
    public void wildBattleProbabilityTest() {
        for (TestMap map : this.maps) {
            for (WildBattleMatcher wildBattleMatcher : map.getMatcher().getWildBattles()) {
                WildEncounter[] wildEncounters = wildBattleMatcher.getWildEncounters();

                int totalProbability = 0;
                for (WildEncounter wildEncounter : wildEncounters) {
                    totalProbability += wildEncounter.getProbability();
                }

                Assert.assertTrue(totalProbability == 100);
            }

            for (FishingMatcher fishingMatcher : map.getMatcher().getFishingSpots()) {
                WildEncounter[] wildEncounters = fishingMatcher.getWildEncounters();

                int totalProbability = 0;
                for (WildEncounter wildEncounter : wildEncounters) {
                    totalProbability += wildEncounter.getProbability();
                }

                Assert.assertTrue(totalProbability == 100);
            }
        }
    }
}
