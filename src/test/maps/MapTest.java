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
import map.overworld.wild.WildEncounterInfo;
import map.triggers.CommonTrigger;
import map.triggers.DialogueTrigger;
import map.triggers.GroupTrigger;
import map.triggers.Trigger;
import message.MessageUpdate;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import pattern.action.ActionList;
import pattern.action.ActionMatcher;
import pattern.action.ActionMatcher.ChoiceActionMatcher;
import pattern.action.ActionMatcher.GiveItemActionMatcher;
import pattern.action.ActionMatcher.MoveNpcActionMatcher;
import pattern.action.ActionMatcher.TradePokemonActionMatcher;
import pattern.action.ChoiceMatcher;
import pattern.action.EmptyActionMatcher.DayCareActionMatcher;
import pattern.action.EntityActionMatcher;
import pattern.action.EnumActionMatcher.CommonTriggerActionMatcher;
import pattern.action.StringActionMatcher.DialogueActionMatcher;
import pattern.action.StringActionMatcher.GlobalActionMatcher;
import pattern.generic.TriggerMatcher;
import pattern.interaction.InteractionMatcher;
import pattern.interaction.NPCInteractionMatcher;
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
import test.general.BaseTest;
import util.Point;
import util.file.FileIO;
import util.file.Folder;
import util.string.StringAppender;
import util.string.StringUtils;

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

        File mapsDirectory = FileIO.newFile(Folder.MAPS);
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
    public void filesTest() {
        // Confirm that each map folder only has the correct files in it
        File mapsDirectory = FileIO.newFile(Folder.MAPS);
        for (File mapFolder : FileIO.listSubdirectories(mapsDirectory)) {
            Set<String> requiredFiles = new HashSet<>();
            requiredFiles.add(mapFolder.getName() + ".txt");
            for (MapDataType type : MapDataType.values()) {
                requiredFiles.add(type.getImageName(mapFolder.getName()));
            }

            for (File mapFile : FileIO.listFiles(mapFolder.getPath())) {
                String fileName = mapFile.getName();
                Assert.assertTrue(mapFile.getPath(), requiredFiles.contains(fileName));
                requiredFiles.remove(fileName);
            }

            Assert.assertTrue(requiredFiles.toString(), requiredFiles.isEmpty());
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
                        String message = StringUtils.spaceSeparated(map.getName(), x, y);
                        Assert.assertNotEquals(message, AreaData.VOID, map.getArea(new Point(x, y)));
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

    /*
        When you interact with an entity on the map, the entity is not reset without dialogue and
        in general it's weird when there's an action without dialogue

        If this fails, then you probably have the issue with this entity where you can no longer interact
        with it until the map is reset and it's very confusing so it's real important this passes
     */
    @Test
    public void dialogueInteractionTest() {
        for (TestMap map : maps) {
            for (NPCMatcher npc : map.getMatcher().getNPCs()) {
                String message = map.getName() + " " + npc.getTriggerName();
                for (NPCInteractionMatcher interaction : npc.getInteractionMatcherList()) {
                    assertDialogueInteraction(message, interaction.getActions());
                }
            }

            for (MiscEntityMatcher miscEntity : map.getMatcher().getMiscEntities()) {
                String message = map.getName() + " " + miscEntity.getTriggerName();
                assertDialogueInteraction(message, miscEntity.getActions());
            }
        }

        // Confirm all common triggers have dialogue
        for (CommonTrigger commonTrigger : CommonTrigger.values()) {
            String message = commonTrigger.name();

            // All common triggers are group triggers
            Trigger trigger = commonTrigger.getTrigger();
            Assert.assertTrue(message, trigger instanceof GroupTrigger);

            // Make sure at least one dialogue trigger
            GroupTrigger groupTrigger = (GroupTrigger)trigger;
            boolean hasDialogue = false;
            for (Trigger t : groupTrigger.getTriggers()) {
                if (t instanceof DialogueTrigger) {
                    hasDialogue = true;
                    break;
                }
            }
            Assert.assertTrue(message, hasDialogue);
        }
    }

    // Asserts that at least one of the actions has a matcher that includes dialogue
    private void assertDialogueInteraction(String message, ActionList actions) {
        List<ActionMatcher> actionList = actions.asList();
        if (actionList.size() == 0) {
            return;
        }

        message = new StringAppender(message + "\n")
                .appendJoin("\n", actionList, action -> action.getClass().getSimpleName() + " " + action.getJson())
                .toString();

        boolean hasDialogue = false;
        for (ActionMatcher action : actionList) {
            // The following actions always include dialogue
            if (action instanceof DialogueActionMatcher
                    || action instanceof ChoiceActionMatcher
                    || action instanceof DayCareActionMatcher
                    || action instanceof TradePokemonActionMatcher
                    || action instanceof CommonTriggerActionMatcher) {
                hasDialogue = true;
                break;
            }
        }

        Assert.assertTrue(message, hasDialogue);
    }

    @Test
    public void interactionTest() {
        for (TestMap map : maps) {
            MapDataMatcher mapData = map.getMatcher();
            String message = map.getName().getMapName();

            // All NPC interactions must have unique names
            // Okay to have zero interactions (mostly for NPCs in unreachable places like Nurse Joy)
            for (NPCMatcher matcher : mapData.getNPCs()) {
                assertUniqueInteractions(message + " " + matcher.getTriggerName(), matcher.getInteractionMatcherList());
            }

            // All NPC interactions must have unique names
            // Must have at least one interaction (otherwise why is it an entity)
            for (MiscEntityMatcher matcher : mapData.getMiscEntities()) {
                String fullMessage = message + " " + matcher.getTriggerName();
                List<InteractionMatcher> interactions = matcher.getInteractionMatcherList();
                Assert.assertNotEquals(fullMessage, 0, interactions.size());
                assertUniqueInteractions(fullMessage, interactions);
            }
        }
    }

    private void assertUniqueInteractions(String message, List<? extends InteractionMatcher> interactions) {
        Set<String> interactionNames = new HashSet<>();
        for (InteractionMatcher interaction : interactions) {
            String interactionName = interaction.getName();
            Assert.assertFalse(message + " " + interactionName, interactionNames.contains(interactionName));
            interactionNames.add(interactionName);
        }
        Assert.assertEquals(message, interactionNames.size(), interactions.size());
    }

    @Test
    public void actionTest() {
        for (TestMap map : maps) {
            for (ActionMatcher action : getAllActions(map)) {
                String message = map.getName() + " " + action.getJson();

                if (action instanceof DialogueActionMatcher) {
                    // Make sure all input dialogue triggers are non-empty, don't include the string 'Poke'
                    // instead of 'Poké', never include the awful sequence of a period on the INSIDE of quotes,
                    // and in general only contain approved characters
                    String dialogue = ((DialogueActionMatcher)action).getStringValue();
                    Assert.assertTrue(message, dialogue.trim().length() > 0);
                    Assert.assertFalse(message, dialogue.contains("Poke"));
                    Assert.assertFalse(message, dialogue.contains(".\""));
                    dialogue = dialogue.replaceAll(MessageUpdate.PLAYER_NAME.replace("{", "\\{"), "Red");
                    Assert.assertTrue(message, dialogue.matches("[a-zA-Z0-9.,'!?:é™*%\\-\\[( ]+"));
                } else if (action instanceof GiveItemActionMatcher) {
                    // Make sure all give items triggers give a positive quantity
                    int quantity = ((GiveItemActionMatcher)action).getQuantity();
                    Assert.assertTrue(message, quantity > 0);
                } else if (action instanceof MoveNpcActionMatcher) {
                    // Either an end entrance name must be specified or the end location is player (they are mutually exclusive)
                    MoveNpcActionMatcher moveNpcActionMatcher = (MoveNpcActionMatcher)action;
                    Assert.assertEquals(
                            message,
                            StringUtils.isNullOrEmpty(moveNpcActionMatcher.getEndEntranceName()),
                            moveNpcActionMatcher.endLocationIsPlayer()
                    );
                } else if (action instanceof ChoiceActionMatcher) {
                    // Choices cannot have entity actions as choices since they're not necessarily executed on an entity
                    ChoiceActionMatcher choiceActionMatcher = (ChoiceActionMatcher)action;
                    for (ChoiceMatcher choice : choiceActionMatcher.getChoices()) {
                        for (ActionMatcher choiceAction : choice.getActions()) {
                            Assert.assertFalse(message, choiceAction instanceof EntityActionMatcher);
                        }
                    }
                }

                // It is okay for action type to be null, but not from map file input (since it is used for map maker)
                Assert.assertNotNull(message, action.getActionType());
            }
        }
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

    private static List<ActionMatcher> getAllActions(TestMap map) {
        List<ActionMatcher> actionMatchers = new ArrayList<>();

        for (NPCMatcher npc : map.getMatcher().getNPCs()) {
            for (NPCInteractionMatcher interaction : npc.getInteractionMatcherList()) {
                addActions(actionMatchers, interaction.getActions());
            }
        }

        for (MiscEntityMatcher miscEntity : map.getMatcher().getMiscEntities()) {
            addActions(actionMatchers, miscEntity.getActions());
        }

        for (EventMatcher event : map.getMatcher().getEvents()) {
            addActions(actionMatchers, event.getActions());
        }

        return actionMatchers;
    }

    private static void addActions(List<ActionMatcher> fullList, ActionList toAdd) {
        for (ActionMatcher actionMatcher : toAdd) {
            fullList.add(actionMatcher);
            if (actionMatcher instanceof ChoiceActionMatcher) {
                ChoiceActionMatcher choiceActionMatcher = (ChoiceActionMatcher)actionMatcher;
                for (ChoiceMatcher choiceMatcher : choiceActionMatcher.getChoices()) {
                    addActions(fullList, choiceMatcher.getActions());
                }
            }
        }
    }
}
