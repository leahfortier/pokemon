package pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import main.Global;
import map.AreaData;
import map.AreaData.WeatherState;
import map.Direction;
import map.EncounterRate;
import map.TerrainType;
import map.WildEncounter;
import map.entity.EntityAction;
import map.entity.EntityAction.BattleAction;
import map.entity.EntityAction.ChoiceAction;
import map.entity.EntityAction.GlobalAction;
import map.entity.EntityAction.GroupTriggerAction;
import map.entity.EntityAction.TriggerAction;
import map.entity.EntityAction.UpdateAction;
import map.entity.npc.NPCInteraction;
import map.triggers.TriggerType;
import mapMaker.dialogs.action.ActionType;
import namesies.ItemNamesies;
import pattern.MatchConstants.MatchType;
import sound.MusicCondition;
import sound.SoundTitle;
import util.FileIO;
import util.Point;
import util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private AreaMatcher[] areas = new AreaMatcher[0];
    private NPCMatcher[] NPCs = new NPCMatcher[0];
    private ItemMatcher[] items = new ItemMatcher[0];
    private MapTransitionMatcher[] mapExits = new MapTransitionMatcher[0];
    private TriggerMatcher[] triggerData = new TriggerMatcher[0];
    private TriggerMatcher[] triggers = new TriggerMatcher[0]; // TODO: Rename

    public AreaDataMatcher(Set<AreaMatcher> areaData,
                           Set<EntityMatcher> entities,
                           Set<TriggerMatcher> triggerData) {
        List<NPCMatcher> npcs = new ArrayList<>();
        List<ItemMatcher> items = new ArrayList<>();
        List<MapTransitionMatcher> mapExits = new ArrayList<>();
        List<TriggerMatcher> misc = new ArrayList<>();
        for (EntityMatcher entity : entities) {
            if (entity instanceof NPCMatcher) {
                npcs.add((NPCMatcher)entity);
            } else if (entity instanceof ItemMatcher) {
                items.add((ItemMatcher)entity);
            } else if (entity instanceof MapTransitionMatcher) {
                mapExits.add((MapTransitionMatcher)entity);
            } else if (entity instanceof TriggerMatcher) {
                misc.add((TriggerMatcher)entity);
            } else {
                Global.error("Unknown entity class " + entity.getClass().getSimpleName());
            }
        }

        this.areas = areaData.toArray(new AreaMatcher[0]);
        this.NPCs = npcs.toArray(new NPCMatcher[0]);
        this.items = items.toArray(new ItemMatcher[0]);
        this.mapExits = mapExits.toArray(new MapTransitionMatcher[0]);
        this.triggers = misc.toArray(new TriggerMatcher[0]);
        this.triggerData = triggerData.toArray(new TriggerMatcher[0]);
    }

    public List<AreaMatcher> getAreas() {
        return Arrays.asList(this.areas);
    }

    public List<NPCMatcher> getNPCs() {
        return Arrays.asList(this.NPCs);
    }

    public List<ItemMatcher> getItems() {
        return Arrays.asList(this.items);
    }

    public List<MapTransitionMatcher> getMapExits() {
        return Arrays.asList(this.mapExits);
    }

    public List<TriggerMatcher> getMiscEntities() {
        return Arrays.asList(this.triggers);
    }

    public List<TriggerMatcher> getTriggerData() {
        return Arrays.asList(this.triggerData);
    }

    public List<EntityMatcher> getEntities() {
        List<EntityMatcher> entities = new ArrayList<>();
        entities.addAll(getNPCs());
        entities.addAll(getItems());
        entities.addAll(getMapExits());
        entities.addAll(getMiscEntities());

        return entities;
    }

    public static <T> T deserialize(String jsonString, Class<T> tClass) {
        return gson.fromJson(jsonString, tClass);
    }

    public AreaData[] getAreaData() {
        AreaData[] areaData = new AreaData[this.areas.length];
        for (int i = 0; i < this.areas.length; i++) {
            if (i > 0 && StringUtils.isNullOrEmpty(this.areas[i].color)) {
                Global.error("Color required for maps with multiple areas.");
            }

            areaData[i] = this.areas[i].getAreaData();
        }

        return areaData;
    }

    private static class MusicConditionMatcher {
        private String condition;
        private SoundTitle music;
    }

    public static class AreaMatcher {
        private String color;
        private String displayName;
        private TerrainType terrain;
        private WeatherState weather;
        private SoundTitle music;
        private MusicConditionMatcher[] musicConditions;

        private transient AreaData areaData;

        private int getColor() {
            return StringUtils.isNullOrEmpty(this.color) ? 0 : (int)Long.parseLong(this.color, 16);
        }

        private WeatherState getWeather() {
            return this.weather == null ? WeatherState.NORMAL : this.weather;
        }

        private MusicCondition[] getMusicConditions() {
            if (this.musicConditions == null) {
                return new MusicCondition[0];
            }

            MusicCondition[] musicConditions = new MusicCondition[this.musicConditions.length];
            for (int i = 0; i < this.musicConditions.length; i++) {
                musicConditions[i] = new MusicCondition(this.musicConditions[i].music, this.musicConditions[i].condition);
            }

            return musicConditions;
        }

        public AreaData getAreaData() {
            if (areaData != null) {
                return areaData;
            }

            areaData = new AreaData(this.displayName, this.getColor(), this.terrain, this.getWeather(), this.music, this.getMusicConditions());
            return areaData;
        }
    }

    private static final Pattern wildEncounterPattern = Pattern.compile(
            MatchConstants.group(MatchType.POKEMON_NAME) + " " +
            MatchConstants.group(MatchType.INTEGER) + "-" + MatchConstants.group(MatchType.INTEGER) + " " +
            MatchConstants.group(MatchType.INTEGER) + "%"
    );

    public static class WildBattleTriggerMatcher {
        private EncounterRate encounterRate;
        private String[] pokemon;

        private transient WildEncounter[] wildEncounters;

        public static TriggerMatcher createWildBattleMatcher(String name, EncounterRate encounterRate, WildEncounter[] wildEncounters) {
            WildBattleTriggerMatcher wildBattleTriggerMatcher = new WildBattleTriggerMatcher(encounterRate, wildEncounters);
            ActionMatcher action = new ActionMatcher();
            action.trigger = new TriggerActionMatcher(TriggerType.WILD_BATTLE, getJson(wildBattleTriggerMatcher));

            return new TriggerMatcher(name, null, new ActionMatcher[] { action });
        }

        private WildBattleTriggerMatcher(EncounterRate encounterRate, WildEncounter[] wildEncounters) {
            this.encounterRate = encounterRate;
            this.wildEncounters = wildEncounters;
            this.pokemon = new String[wildEncounters.length];
            for (int i = 0; i < pokemon.length; i++) {
                WildEncounter wildEncounter = wildEncounters[i];
                this.pokemon[i] = wildEncounter.getPokemonName() + " " + wildEncounter.getMinLevel() + "-" + wildEncounter.getMaxLevel() + " " + wildEncounter.getProbability() + "%";
            }
        }

        public EncounterRate getEncounterRate() {
            return this.encounterRate;
        }

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

    public static class GroupTriggerMatcher {
        public String[] triggers;

        public String suffix;
        public String condition;
        public String[] globals;

        public GroupTriggerMatcher(final String... triggers) {
            this.triggers = triggers;
        }
    }

    public static class TriggerMatcher extends EntityMatcher {
        public String name; // TODO: Make private
        private int[] location;
        public String condition;

        private ActionMatcher[] actions;

        private transient List<Point> points;
        private transient List<EntityAction> entityActions;
        private transient WildBattleTriggerMatcher wildBattleContents;

        public TriggerMatcher(String name, String condition, ActionMatcher[] actions) {
            this.name = StringUtils.nullWhiteSpace(name);
            this.condition = StringUtils.nullWhiteSpace(condition);
            this.actions = actions;
        }

        @Override
        public String getName() {
            return this.name;
        }

        public List<Point> getLocation() {
            if (points != null) {
                return points;
            }

            this.points = new ArrayList<>();
            if (this.location != null) {
                for (int i = 0; i < this.location.length; i += 2) {
                    int x = this.location[i];
                    int y = this.location[i + 1];

                    this.points.add(new Point(x, y));
                }
            }

            return this.points;
        }

        @Override
        public void addPoint(Point point) {
            AreaDataMatcher.addPoint(point, this.getLocation(), location);
        }

        public ActionMatcher[] getActionMatchers() {
            return this.actions;
        }

        public List<EntityAction> getActions() {
            if (this.entityActions != null) {
                return this.entityActions;
            }

            this.entityActions = new ArrayList<>();
            for (ActionMatcher matcher : this.actions) {
                this.entityActions.add(matcher.getAction(condition));
            }

            return this.entityActions;
        }

        public boolean isWildBattleTrigger() {
            return this.getWildBattleTriggerContents() != null;
        }

        public WildBattleTriggerMatcher getWildBattleTriggerContents() {
            if (this.wildBattleContents != null) {
                return this.wildBattleContents;
            }

            for (EntityAction action : this.getActions()) {
                if (action instanceof TriggerAction) {
                    TriggerAction triggerAction = (TriggerAction)action;
                    if (triggerAction.getTriggerType() == TriggerType.WILD_BATTLE) {
                        String contents = triggerAction.getTriggerContents(this.name);
                        this.wildBattleContents = deserialize(contents, WildBattleTriggerMatcher.class);
                        break;
                    }
                }
            }

            return this.wildBattleContents;
        }
    }

    public static class ItemMatcher extends EntityMatcher {
        private String name;
        private int x;
        private int y;
        private String item;
        private String condition;

        private transient ItemNamesies itemNamesies;

        private transient List<Point> location;

        public ItemMatcher(ItemNamesies itemName) {
            this.item = itemName.getName();
            this.itemNamesies = itemName;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public List<Point> getLocation() {
            if (this.location != null) {
                return this.location;
            }

            this.location = new ArrayList<>();
            this.location.add(new Point(x, y));
            return this.location;
        }

        @Override
        public void addPoint(Point point) {
            this.x = point.x;
            this.y = point.y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public ItemNamesies getItem() {
            if (this.itemNamesies != null) {
                return this.itemNamesies;
            }

            return this.itemNamesies = ItemNamesies.getValueOf(this.item);
        }
    }

    public static String addJsonField(final String previousJson, final String key, final String value) {
        JsonObject jsonObject = deserialize(previousJson, JsonObject.class);
        jsonObject.add(key, new JsonPrimitive(value));
        return getJson(jsonObject);
    }

    private static void addPoint(Point point, List<Point> points, int[] location) {
        points.add(Point.copy(point));

        location = Arrays.copyOf(location, location.length + 2);
        location[location.length - 2] = point.x;
        location[location.length - 1] = point.y;
    }

    public static class MapTransitionMatcher extends EntityMatcher {
        private String exitName;
        private int[] location;
        private String nextMap;
        private String nextEntrance;
        private Direction direction;
        private boolean deathPortal;

        private String previousMap;

        private transient List<Point> entrances;
        private transient List<Point> exits;

        public MapTransitionMatcher(String exitName, String nextMap, String nextEntrance, Direction direction, boolean deathPortal) {
            this.exitName = exitName;
            this.nextMap = nextMap;
            this.nextEntrance = nextEntrance;
            this.direction = direction;
            this.deathPortal = deathPortal;
        }

        public void setMapName(final String mapName) {
            this.previousMap = mapName;
        }

        @Override
        public String getName() {
            return this.getExitName();
        }

        public String getExitName() {
            return this.exitName;
        }

        public String getNextMap() {
            return this.nextMap;
        }

        public String getNextEntranceName() {
            return this.nextEntrance;
        }

        public Direction getDirection() {
            return this.direction;
        }

        public String getPreviousMap() {
           return this.previousMap;
        }

        public List<Point> getLocation() {
            if (entrances != null) {
                return entrances;
            }

            this.entrances = new ArrayList<>();
            if (this.location != null) {
                for (int i = 0; i < this.location.length; i += 2) {
                    int x = this.location[i];
                    int y = this.location[i + 1];

                    this.entrances.add(new Point(x, y));
                }
            }

            return this.entrances;
        }

        @Override
        public void addPoint(Point point) {
            AreaDataMatcher.addPoint(point, this.getEntrances(), this.location);
            this.addExitPoint(Point.copy(point));
        }

        public List<Point> getEntrances() {
            return this.getLocation();
        }

        private void addExitPoint(Point entrance) {
            if (this.direction != null) {
                // TODO: dx, dy -> delta
                int exitX = entrance.x + this.direction.dx;
                int exitY = entrance.y + this.direction.dy;

                this.exits.add(new Point(exitX, exitY));
            }
        }

        public List<Point> getExits() {
            if (this.exits != null) {
                return this.exits;
            }

            this.exits = new ArrayList<>();

            List<Point> entrances = this.getEntrances();
            for (Point entrance : entrances) {
                this.addExitPoint(entrance);
            }

            return this.exits;
        }

        public boolean isDeathPortal() {
            return deathPortal;
        }
    }

    public abstract static class EntityMatcher {
        public abstract String getName();
        public abstract List<Point> getLocation();
        public abstract void addPoint(Point point);
    }

    public static class NPCMatcher extends EntityMatcher {
        public String name;
        public String condition;
        private int startX;
        private int startY;
        private String path;
        public int spriteIndex;
        public Direction direction;
        public NPCInteractionMatcher[] interactions;

        private List<Point> location;
        private transient Map<String, NPCInteraction> interactionMap;
        private transient String startKey;

        public NPCMatcher(String name,
                          String condition,
                          String path,
                          int spriteIndex,
                          Direction direction,
                          List<NPCInteractionMatcher> interactions) {
            this.setName(name);
            this.condition = StringUtils.nullWhiteSpace(condition);
            this.path = StringUtils.nullWhiteSpace(path);
            this.spriteIndex = spriteIndex;
            this.direction = direction;
            this.interactions = interactions.toArray(new NPCInteractionMatcher[0]);
        }

        public void setName(String name) {
            this.name = StringUtils.nullWhiteSpace(name);
        }

        public Map<String, NPCInteraction> getInteractionMap() {
            if (interactionMap != null) {
                return interactionMap;
            }

            interactionMap = new HashMap<>();
            for (NPCInteractionMatcher interaction : interactions) {
                interactionMap.put(interaction.name, new NPCInteraction(interaction.walkToPlayer, interaction.getActions()));
            }

            if (interactions.length == 0) {
                startKey = "no_interactions";
                interactionMap.put(startKey, new NPCInteraction(false, new ArrayList<>()));

            } else {
                startKey = interactions[0].name;

            }
            return interactionMap;
        }

        public int getX() {
            return this.startX;
        }

        public int getY() {
            return this.startY;
        }

        public String getStartKey() {
            if (interactionMap == null) {
                getInteractionMap();
            }

            return startKey;
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

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public List<Point> getLocation() {
            if (this.location != null) {
                return this.location;
            }

            this.location = new ArrayList<>();
            this.location.add(new Point(startX, startY));
            return this.location;
        }

        @Override
        public void addPoint(Point location) {
            this.startX = location.x;
            this.startY = location.y;
        }
    }

    // TODO: Move this to some sort of util location
    public static boolean hasOnlyOneNonEmpty(Object... objects) {
        return Arrays.stream(objects)
                .filter(object -> object != null)
                .count() == 1;
    }

    public static class ChoiceActionMatcher {
        public String question;
        public ChoiceMatcher[] choices;
    }

    public static class ChoiceMatcher {
        public String text;
        private ActionMatcher[] actions;

        public List<EntityAction> getActions() {
            List<EntityAction> actions = new ArrayList<>();
            for (ActionMatcher action : this.actions) {
                actions.add(action.getAction(null));
            }

            return actions;
        }
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

        public TriggerActionMatcher(TriggerType triggerType, String triggerContents) {
            this.triggerType = triggerType.name();
            this.triggerContents = triggerContents;
        }

        public TriggerType getTriggerType() {
            return TriggerType.getTriggerType(this.triggerType);
        }
    }

    public static class ActionMatcher {
        public TriggerActionMatcher trigger;
        public BattleMatcher battle;
        public ChoiceActionMatcher choice;
        public String update;
        public String groupTrigger;
        public String global;

        private transient EntityAction entityAction;

        public ActionType getActionType() {
            if (!hasOnlyOneNonEmpty(trigger, battle, choice, update, groupTrigger, global)) {
                Global.error("Can only have one nonempty field for ActionMatcher");
            }

            if (trigger != null) {
                return ActionType.TRIGGER;
            } else if (battle != null) {
                return ActionType.BATTLE;
            } else if (update != null) {
                return ActionType.UPDATE;
            } else if (groupTrigger != null) {
                return ActionType.GROUP_TRIGGER;
            } else if (choice != null) {
                return ActionType.CHOICE;
            } else if (global != null) {
                return ActionType.GLOBAL;
            }

            Global.error("No action found.");
            return null;
        }

        public EntityAction getAction(final String condition) {
            if (entityAction != null) {
                return entityAction;
            }

            ActionType actionType = this.getActionType();
            switch (actionType) {
                case TRIGGER:
                    this.entityAction = new TriggerAction(trigger.getTriggerType(), trigger.triggerContents, condition);
                    break;
                case BATTLE:
                    this.entityAction = new BattleAction(battle);
                    break;
                case UPDATE:
                    this.entityAction = new UpdateAction(update);
                    break;
                case GROUP_TRIGGER:
                    this.entityAction = new GroupTriggerAction(groupTrigger);
                    break;
                case CHOICE:
                    this.entityAction = new ChoiceAction(choice);
                    break;
                case GLOBAL:
                    this.entityAction = new GlobalAction(global);
                    break;
                default:
                    Global.error("No action found.");
                    break;
            }

            return this.entityAction;
        }
    }

    public static class NPCInteractionMatcher {
        public String name;
        public boolean walkToPlayer;
        public ActionMatcher[] npcActions;

        public NPCInteractionMatcher(String name, boolean walkToPlayer, ActionMatcher[] npcActions) {
            this.name = StringUtils.nullWhiteSpace(name);
            this.walkToPlayer = walkToPlayer;
            this.npcActions = npcActions;
        }

        public List<EntityAction> getActions() {
            List<EntityAction> actions = new ArrayList<>();
            for (ActionMatcher action : this.npcActions) {
                actions.add(action.getAction(null));
            }

            return actions;
        }
    }

    public static class BattleMatcher {
        public String name;
        public int cashMoney;
        public String[] pokemon;
        public String update;

        public BattleMatcher(String name, int cashMoney, String[] pokemon, String update) {
            this.name = name;
            this.cashMoney = cashMoney;
            this.pokemon = pokemon;
            this.update = update;
        }
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
