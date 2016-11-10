package pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import main.Global;
import map.Direction;
import map.entity.npc.NPCAction;
import util.Point;
import util.FileIO;
import util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public MapEntranceMatcher[] mapEntrances = new MapEntranceMatcher[0];
    public TriggerDataMatcher[] triggerData = new TriggerDataMatcher[0];
    public TriggerMatcher[] triggers = new TriggerMatcher[0];

    public static <T> T deserialize(String jsonString, Class<T> tClass) {
        System.out.println(jsonString);
        return gson.fromJson(jsonString, tClass);
    }

    public static class MapTransitionTriggerMatcher {
        public String nextMap;
        public String mapEntrance;
        public Direction direction;
        public int newX;
        public int newY;
    }

    public static class GroupTriggerMatcher {
        public String[] triggers;
    }

    public static class TriggerMatcher {
        public String name;
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

    public static class MapEntranceMatcher {
        public String name;
        public int x;
        public int y;
    }

    public static class TriggerDataMatcher {
        public String name;
        private int[] location;
        public String triggerType;
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
    }

    public static class NPCMatcher {
        public String name;
        public String condition;
        public int startX;
        public int startY;
        public String trigger;
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

            startKey = interactions[0].name;
            return interactionMap;
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

    public static class ActionMatcher {
        private String text;
        private BattleMatcher battle;
        private String giveItem;
        private String givePokemon;
    }

    public static class InteractionMatcher {
        private String name;
        private ActionMatcher[] npcActions;
        private String update;

        List<NPCAction> getActions() {
            List<NPCAction> npcActions = new ArrayList<>();
            // TODO
            return npcActions;
        }
    }

    public static class BattleMatcher {
        public String name;
        public int cashMoney;
        public String[] pokemon;
        public String update;
    }

    public static class TrainerMatcher {
        public String name;
        public int cashMoney;
        public String[] pokemon;
        public String before;
        public String after;

        public String getTrainerName() {
            return this.name;
        }

        public int getDatCashMoney() {
            return this.cashMoney;
        }

        public String[] getPokemonDescriptionList() {
            return this.pokemon;
        }

        public String getTrainerBeforeMessage() {
            return this.before;
        }

        public String getTrainerAfterMessage() {
            return this.after;
        }
    }

    public static AreaDataMatcher matchArea(String fileName, String areaDescription) {

        System.out.println(fileName);

        AreaDataMatcher areaData = gson.fromJson(areaDescription, AreaDataMatcher.class);
        Map<Object, Object> mappity = gson.fromJson(areaDescription, Map.class);

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

    private static String getJson(final Object jsonObject) {
        return gson.toJson(jsonObject)
                .replaceAll("\\\\n", "\n")
                .replaceAll("\\\\t", "\t");
    }
}
