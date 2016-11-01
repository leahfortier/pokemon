package pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import map.Direction;
import map.EncounterRate;
import map.triggers.TriggerData;
import util.FileIO;
import util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AreaDataMatcher {

    public NPCMatcher[] NPCs = new NPCMatcher[0];
    public ItemMatcher[] items = new ItemMatcher[0];
    public MapEntranceMatcher[] mapEntrances = new MapEntranceMatcher[0];
    public TriggerDataMatcher[] triggerData = new TriggerDataMatcher[0];
    public TriggerMatcher[] triggers = new TriggerMatcher[0];
    public GroupTriggerMatcher[] groupTriggers = new GroupTriggerMatcher[0];

    public static class GroupTriggerMatcher {
        public String name;
        public int[] location;
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
        public int[] location;
        public String triggerType;
        public String condition;
        public String global;
        public String nextMap;
        public String mapEntrance;
        public EncounterRate encounterRate;
        public String[] pokemon;
        public String createDialogue;
        public String text;
        public String[] contents;
    }

    public static class NPCMatcher {
        public String name;
        public String condition;
        public int startX;
        public int startY;
        public String trigger;
        public String path;
        public int spriteIndex;
        public Direction direction;
        public boolean walkToPlayer;
        public String text;
        public Interaction[] interactions;
        public TrainerMatcher trainer;

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

    public static class Interaction {
        public String name;
        public String[] actions;
        public String update;
        public String text;
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
        Gson gson = new GsonBuilder()
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

        AreaDataMatcher areaData = gson.fromJson(areaDescription, AreaDataMatcher.class);
        Map<Object, Object> mappity = gson.fromJson(areaDescription, Map.class);

        for (TriggerDataMatcher matcher : areaData.triggerData) {
            if (matcher.contents != null) {
                continue;
            }
            final List<String> contents = new ArrayList<>();
            if (!StringUtils.isNullOrEmpty(matcher.nextMap)) {
                contents.add("nextMap: " + matcher.nextMap);
                matcher.nextMap = null;
            }

            if (!StringUtils.isNullOrEmpty(matcher.mapEntrance)) {
                contents.add("mapEntrance: " + matcher.mapEntrance);
                matcher.mapEntrance = null;
            }

            if (matcher.encounterRate != null) {
                contents.add("encounterRate: " + matcher.encounterRate);
                matcher.encounterRate = null;
            }

            if (matcher.pokemon != null) {
                String pokemonString = "";
                for (String pokemon : matcher.pokemon) {
                    pokemonString += (pokemonString.isEmpty() ? "" : ", ") + "\n\t\t\t\"" + pokemon + "\"";
                }
                contents.add("pokemon: {" + pokemonString + "\n}");
                matcher.pokemon = null;
            }

            if (!StringUtils.isNullOrEmpty(matcher.createDialogue)) {
                contents.add("createDialogue: " + matcher.createDialogue);
                matcher.createDialogue = null;
            }

            if (!StringUtils.isNullOrEmpty(matcher.text)) {
                contents.add("text: " + matcher.text);
                matcher.text = null;
            }

            matcher.contents = contents.toArray(new String[0]);
        }

        String areaDataJson = gson.toJson(areaData).replaceAll("\\\\n", "\n").replaceAll("\\\\t", "\t");
        String mapJson = gson.toJson(mappity).replaceAll("\\\\n", "\n").replaceAll("\\\\t", "\t");

        FileIO.writeToFile("out.txt", new StringBuilder(areaDataJson));
        FileIO.writeToFile("out2.txt", new StringBuilder(mapJson));

        FileIO.overwriteFile(fileName, new StringBuilder(areaDataJson));

        return areaData;
    }
}
