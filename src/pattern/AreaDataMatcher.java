package pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import main.Global;
import map.Direction;
import map.EncounterRate;
import map.WildEncounter;
import map.entity.npc.NPCAction;
import map.entity.npc.NPCAction.BattleAction;
import map.entity.npc.NPCAction.ChoiceAction;
import map.entity.npc.NPCAction.GroupTriggerAction;
import map.entity.npc.NPCAction.TriggerAction;
import map.entity.npc.NPCAction.UpdateAction;
import map.triggers.TriggerData.Point;
import map.triggers.TriggerType;
import pattern.MatchConstants.MatchType;
import util.FileIO;
import util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AreaDataMatcher {

    private static Gson gson = new GsonBuilder()
            .registerTypeAdapter(Double.class, (JsonSerializer<Double>) (source, sourceType, context) -> {
                if (source == source.longValue()) {
                    return new JsonPrimitive(source.longValue());
                } else {
                    return new JsonPrimitive(source);
                }
            })
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .setLenient()
            .create();

    public NPCMatcher[] NPCs = new NPCMatcher[0];
    public ItemMatcher[] items = new ItemMatcher[0];
    public MapExitMatcher[] mapExits = new MapExitMatcher[0];
    public TriggerDataMatcher[] triggerData = new TriggerDataMatcher[0];
    public TriggerMatcher[] triggers = new TriggerMatcher[0];

    public static <T> T deserialize(String jsonString, Class<T> tClass) {
        return gson.fromJson(jsonString, tClass);
    }

    private static final Pattern wildEncounterPattern = Pattern.compile(
            MatchConstants.group(MatchType.POKEMON_NAME) + " " +
            MatchConstants.group(MatchType.INTEGER) + "-" + MatchConstants.group(MatchType.INTEGER) + " " +
            MatchConstants.group(MatchType.INTEGER) + "%"
    );
    public static class WildBattleTriggerMatcher {
        public EncounterRate encounterRate;
        private String[] pokemon;

        private transient WildEncounter[] wildEncounters;

        public WildEncounter[] getWildEncounters() {
            if (this.wildEncounters != null) {
                return wildEncounters;
            }

            this.wildEncounters = new WildEncounter[pokemon.length];
            for (int i = 0; i < pokemon.length; i++) {
                Matcher matcher = wildEncounterPattern.matcher(pokemon[i]);
                if (!matcher.matches()) {
                    Global.error("Invalid wild pokemon encounter description " + pokemon[i]);
                }

                wildEncounters[i] = new WildEncounter(
                        matcher.group(1),   // Pokemon name
                        matcher.group(2),   // Min level
                        matcher.group(3),   // Max level
                        matcher.group(4)    // Percentage probability
                );
            }

            return this.wildEncounters;
        }
    }

    public static class SoundTriggerMatcher {
        public String effectName;
        public String musicName;
    }

    public static class GroupTriggerMatcher {
        public String[] triggers;

        public String suffix;
        public String condition;
        public String[] globals;

        public GroupTriggerMatcher(final String... triggers) {
            this.triggers = triggers;
        }
    }

    public static class TriggerMatcher {
        public int x;
        public int y;
        public String trigger;
    }

    public static class ItemMatcher {
        public String name;
        public int x;
        public int y;
        public String item;
    }

    public static String addJsonField(final String previousJson, final String key, final String value) {
        JsonObject jsonObject = deserialize(previousJson, JsonObject.class);
        jsonObject.add(key, new JsonPrimitive(value));
        return getJson(jsonObject);
    }

    public static class MapExitMatcher {
        public String exitName;
        private int[] location;
        public String nextMap;
        public String nextEntrance;
        public Direction direction;
        public Boolean deathPortal;

        public String previousMap;

        public void setMapName(final String mapName) {
            this.previousMap = mapName;
        }

        public List<Point> getLocation() {
            List<Point> points = new ArrayList<>();
            if (this.location != null) {
                for (int i = 0; i < this.location.length; i += 2) {
                    int x = this.location[i];
                    int y = this.location[i + 1];

                    points.add(new Point(x, y));
                }
            }

            return points;
        }
    }

    public static class TriggerDataMatcher {
        private int[] location;
        private String triggerType;
        public String condition;
        public String global;
        public String triggerContents;

        public List<Point> getLocation() {
            List<Point> points = new ArrayList<>();
            if (this.location != null) {
                for (int i = 0; i < this.location.length; i += 2) {
                    int x = this.location[i];
                    int y = this.location[i + 1];

                    points.add(new Point(x, y));
                }
            }

            return points;
        }

        public TriggerType getTriggerType() {
            return TriggerType.getTriggerType(this.triggerType);
        }
    }

    public static class NPCMatcher {
        public String name;
        public String condition;
        public int startX;
        public int startY;
        private String path;
        public int spriteIndex;
        public Direction direction;
        public boolean walkToPlayer;
        public InteractionMatcher[] interactions;

        private transient Map<String, List<NPCAction>> interactionMap;
        private transient String startKey;

        public Map<String, List<NPCAction>> getInteractionMap() {
            if (interactionMap != null) {
                return interactionMap;
            }

            interactionMap = new HashMap<>();
            for (InteractionMatcher interaction : interactions) {
                interactionMap.put(interaction.name, interaction.getActions());
            }

            if (interactions.length == 0) {
                startKey = "no_interactions";
                interactionMap.put(startKey, new ArrayList<>());

            } else {
                startKey = interactions[0].name;

            }
            return interactionMap;
        }

        public String getStartKey() {
            if (interactionMap == null) {
                getInteractionMap();
            }

            return startKey;
        }

        public String getNPCName() {
            return this.name;
        }

        public int getStartX() {
            return this.startX;
        }

        public int getStartY() {
            return this.startY;
        }

        public String getPath() {
            if (StringUtils.isNullOrEmpty(this.path)) {
                this.path = Direction.WAIT_CHARACTER + "";
            }

            return this.path;
        }

        public int getSpriteIndex() {
            return this.spriteIndex;
        }

        public Direction getDirection() {
            return this.direction;
        }

        public boolean shouldWalkToPlayer() {
            return this.walkToPlayer;
        }
    }

    // TODO: Move this to some sort of util location
    public static boolean hasOnlyOneNonEmpty(Object... objects) {
        return Arrays.stream(objects)
                .filter(object -> object != null)
                .count() == 1;
    }

    public static class ChoiceMatcher {
        public String text;
        public TriggerActionMatcher trigger;
    }

    public static class UpdateMatcher {
        public String npcEntityName;
        public String interactionName;

        public UpdateMatcher(final String npcEntityName, final String interactionName) {
            this.npcEntityName = npcEntityName;
            this.interactionName = interactionName;
        }
    }

    public static class TriggerActionMatcher {
        private String triggerType;
        public String triggerContents;

        public TriggerType getTriggerType() {
            return TriggerType.getTriggerType(this.triggerType);
        }
    }

    public static class ActionMatcher {
        private TriggerActionMatcher trigger;
        private BattleMatcher battle;
        private ChoiceMatcher[] choices;
        private String update;
        private String groupTrigger;

        public NPCAction getNPCAction() {
            if (!hasOnlyOneNonEmpty(trigger, battle, choices, update, groupTrigger)) {
                Global.error("Can only have one nonempty field for ActionMatcher");
            }

            if (trigger != null) {
                return new TriggerAction(trigger.getTriggerType(), trigger.triggerContents);
            } else if (battle != null) {
                return new BattleAction(battle);
            } else if (!StringUtils.isNullOrEmpty(update)) {
                return new UpdateAction(update);
            } else if (!StringUtils.isNullOrEmpty(groupTrigger)) {
                return new GroupTriggerAction(groupTrigger);
            } else if (choices != null) {
                return new ChoiceAction();
            }

            Global.error("No npc action found.");
            return null;
        }
    }

    public static class InteractionMatcher {
        private String name;
        private ActionMatcher[] npcActions;

        List<NPCAction> getActions() {
            List<NPCAction> npcActions = new ArrayList<>();
            for (ActionMatcher action : this.npcActions) {
                npcActions.add(action.getNPCAction());
            }

            return npcActions;
        }
    }

    public static class BattleMatcher {
        public String name;
        public int cashMoney;
        public String[] pokemon;
        public String update;
    }

    public static AreaDataMatcher matchArea(String fileName, String areaDescription) {

        AreaDataMatcher areaData = gson.fromJson(areaDescription, AreaDataMatcher.class);
        JsonObject mappity = gson.fromJson(areaDescription, JsonObject.class);

        String areaDataJson = getJson(areaData);
        String mapJson = getJson(mappity);

        FileIO.writeToFile("out.txt", new StringBuilder(areaDataJson));
        FileIO.writeToFile("out2.txt", new StringBuilder(mapJson));

        if (!areaDataJson.equals(mapJson)) {
            Global.error("No dice");
        }

        areaDataJson = getJson(areaData);

        FileIO.overwriteFile(fileName, new StringBuilder(areaDataJson));

        return areaData;
    }

    public static String getJson(final Object jsonObject) {
        return gson.toJson(jsonObject)
                .replaceAll("\\\\n", "\n")
                .replaceAll("\\\\t", "\t");
    }
}
