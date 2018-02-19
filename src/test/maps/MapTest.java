package test.maps;

import map.MapDataType;
import map.area.AreaData;
import map.condition.Condition;
import map.condition.Condition.GlobalCondition;
import map.condition.Condition.NpcInteractionCondition;
import map.condition.Condition.TimeOfDayCondition;
import map.condition.ConditionHolder;
import map.condition.ConditionHolder.NotCondition;
import map.condition.ConditionHolder.OrCondition;
import map.daynight.DayCycle;
import map.overworld.WalkType;
import map.overworld.WildEncounterInfo;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import pattern.action.ActionMatcher;
import pattern.action.NPCInteractionMatcher;
import pattern.action.StringActionMatcher.DialogueActionMatcher;
import pattern.action.StringActionMatcher.GlobalActionMatcher;
import pattern.generic.TriggerMatcher;
import pattern.map.AreaMatcher;
import pattern.map.AreaMatcher.MusicConditionMatcher;
import pattern.map.EventMatcher;
import pattern.map.FishingMatcher;
import pattern.map.ItemMatcher;
import pattern.map.MapDataMatcher;
import pattern.map.MiscEntityMatcher;
import pattern.map.NPCMatcher;
import pattern.map.WildBattleAreaMatcher;
import pattern.map.WildBattleMatcher;
import test.BaseTest;
import util.FileIO;
import util.Folder;
import util.Point;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapTest extends BaseTest {
    private static List<TestMap> maps;
    private static Set<String> addedGlobals;
    private static Map<String, Set<String>> npcTriggerNames;

    @BeforeClass
    public static void setup() {
        maps = new ArrayList<>();
        addedGlobals = new HashSet<>();
        npcTriggerNames = new HashMap<>();

        File mapsDirectory = new File(Folder.MAPS);
        for (File mapFolder : FileIO.listSubdirectories(mapsDirectory)) {
            TestMap map = new TestMap(mapFolder);
            maps.add(map);

            for (ActionMatcher action : getAllActions(map)) {
                if (action instanceof GlobalActionMatcher) {
                    GlobalActionMatcher globalActionMatcher = (GlobalActionMatcher)action;
                    String globalName = globalActionMatcher.getStringValue();
                    Assert.assertFalse(addedGlobals.contains(globalName));
                    addedGlobals.add(globalName);
                }
            }

            for (NPCMatcher npc : map.getMatcher().getNPCs()) {
                String triggerName = npc.getTriggerName();

                Assert.assertFalse(triggerName, npcTriggerNames.containsKey(triggerName));
                npcTriggerNames.put(triggerName, npc.getInteractionMap().keySet());
            }

            for (ItemMatcher item : map.getMatcher().getItems()) {
                String triggerName = item.getTriggerName();

                Assert.assertFalse(addedGlobals.contains(triggerName));
                addedGlobals.add(triggerName);
            }
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
                                map.getArea(new Point(x, y)) == AreaData.VOID
                        );
                    }
                }
            }
        }
    }

    @Test
    public void wildBattleTest() {
        for (TestMap map : maps) {
            String mapName = map.getName().getMapName();

            Set<Point> seenLocations = new HashSet<>();
            for (WildBattleAreaMatcher areaMatcher : map.getMatcher().getWildBattles()) {
                checkLocation(mapName, seenLocations, areaMatcher.getLocation());

                for (WildBattleMatcher wildBattleMatcher : areaMatcher.getWildBattles()) {
                    checkProbability(mapName, wildBattleMatcher.getWildEncounters());
                }
            }

            seenLocations = new HashSet<>();
            for (FishingMatcher fishingMatcher : map.getMatcher().getFishingSpots()) {
                checkLocation(mapName, seenLocations, fishingMatcher.getLocation());
                checkProbability(mapName, fishingMatcher.getWildEncounters());
            }
        }
    }

    // Make sure there are not multiple areas that overlap in location
    private void checkLocation(String mapName, Set<Point> seenLocations, List<Point> wildLocations) {
        Assert.assertFalse(mapName, wildLocations.isEmpty());
        for (Point location : wildLocations) {
            Assert.assertFalse(seenLocations.contains(location));
            seenLocations.add(location);
        }
    }

    // Make sure the probabilities add up to 100
    private void checkProbability(String mapName, WildEncounterInfo[] wildEncounters) {
        int totalProbability = 0;
        for (WildEncounterInfo wildEncounter : wildEncounters) {
            totalProbability += wildEncounter.getProbability();
        }

        Assert.assertEquals(mapName, 100, totalProbability);
    }

    @Test
    public void dialogueTest() {
        // Make sure all input dialogue triggers don't include the string 'Poke' instead of 'Poké'
        for (TestMap map : maps) {
            for (ActionMatcher action : getAllActions(map)) {
                if (action instanceof DialogueActionMatcher) {
                    String dialogue = ((DialogueActionMatcher)action).getStringValue();
                    Assert.assertFalse(map.getName() + " " + dialogue, dialogue.contains("Poke"));
                }
            }
        }
    }

    private static List<ActionMatcher> getAllActions(TestMap map) {
        List<ActionMatcher> actionMatchers = new ArrayList<>();

        for (NPCMatcher npc : map.getMatcher().getNPCs()) {
            for (NPCInteractionMatcher interaction : npc.getInteractionMatcherList()) {
                actionMatchers.addAll(interaction.getActions());
            }
        }

        for (MiscEntityMatcher miscEntity : map.getMatcher().getMiscEntities()) {
            actionMatchers.addAll(miscEntity.getActions());
        }

        for (EventMatcher event : map.getMatcher().getEvents()) {
            actionMatchers.addAll(event.getActions());
        }

        return actionMatchers;
    }

    @Test
    public void conditionsTest() {
        for (TestMap map : maps) {
            MapDataMatcher mapData = map.getMatcher();
            for (AreaMatcher area : mapData.getAreas()) {
                MusicConditionMatcher[] musicConditions = area.getMusicConditionMatchers();
                if (musicConditions == null) {
                    continue;
                }

                for (MusicConditionMatcher musicCondition : musicConditions) {
                    checkCondition(musicCondition.getCondition());
                }
            }

            for (TriggerMatcher triggerMatcher : mapData.getAllEntities()) {
                checkCondition(triggerMatcher.getCondition());
            }

            // Make sure all wild battle areas cover all times of day and have no overlap
            for (WildBattleAreaMatcher wildBattleArea : mapData.getWildBattles()) {
                Set<DayCycle> timeOfDaySet = EnumSet.allOf(DayCycle.class);
                for (WildBattleMatcher wildBattle : wildBattleArea.getWildBattles()) {
                    checkWildCondition(
                            wildBattleArea.getTriggerName(),
                            timeOfDaySet,
                            wildBattle.getCondition()
                    );
                }
                Assert.assertTrue(timeOfDaySet.isEmpty());
            }
        }
    }

    private void checkWildCondition(String message, Set<DayCycle> timeOfDaySet, Condition condition) {
        if (condition == null) {
            for (DayCycle dayCycle : DayCycle.values()) {
                Assert.assertTrue(message, timeOfDaySet.contains(dayCycle));
                timeOfDaySet.remove(dayCycle);
            }
        } else if (condition instanceof NotCondition) {
            for (Condition subCondition : ((ConditionHolder)condition).getConditions()) {
                Assert.assertTrue(subCondition instanceof TimeOfDayCondition);

                DayCycle notDayCycle = ((TimeOfDayCondition)subCondition).getTimeOfDay();
                for (DayCycle dayCycle : DayCycle.values()) {
                    if (dayCycle == notDayCycle) {
                        continue;
                    }

                    Assert.assertTrue(message, timeOfDaySet.contains(dayCycle));
                    timeOfDaySet.remove(dayCycle);
                }
            }
        } else if (condition instanceof OrCondition) {
            for (Condition subCondition : ((ConditionHolder)condition).getConditions()) {
                Assert.assertTrue(subCondition instanceof TimeOfDayCondition);

                DayCycle dayCycle = ((TimeOfDayCondition)subCondition).getTimeOfDay();
                Assert.assertTrue(message, timeOfDaySet.contains(dayCycle));
                timeOfDaySet.remove(dayCycle);
            }
        } else {
            Assert.assertTrue(condition instanceof TimeOfDayCondition);

            DayCycle dayCycle = ((TimeOfDayCondition)condition).getTimeOfDay();
            Assert.assertTrue(message, timeOfDaySet.contains(dayCycle));
            timeOfDaySet.remove(dayCycle);
        }
    }

    private void checkCondition(Condition condition) {
        if (condition instanceof ConditionHolder) {
            for (Condition subCondition : ((ConditionHolder)condition).getConditions()) {
                checkCondition(subCondition);
            }
        } else if (condition instanceof NpcInteractionCondition) {
            // Make sure all NPC Conditions refer to valid NPCs and interactions
            NpcInteractionCondition npcCondition = (NpcInteractionCondition)condition;
            String entityName = npcCondition.getNpcEntityName();
            String interactionName = npcCondition.getInteractionName();

            Assert.assertTrue(entityName, npcTriggerNames.containsKey(entityName));
            if (!interactionName.isEmpty()) {
                Assert.assertTrue(entityName, npcTriggerNames.get(entityName).contains(interactionName));
            }
        } else if (condition instanceof GlobalCondition) {
            GlobalCondition globalCondition = (GlobalCondition)condition;
            String globalName = globalCondition.getGlobalName();
            Assert.assertTrue(globalName, addedGlobals.contains(globalName));
        }
    }
}
