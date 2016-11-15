package pattern.map;

import com.google.gson.JsonObject;
import main.Global;
import map.AreaData;
import pattern.generic.LocationTriggerMatcher;
import util.FileIO;
import util.JsonUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class MapDataMatcher {

    private AreaMatcher[] areas = new AreaMatcher[0];
    private MapTransitionMatcher[] mapTransitions = new MapTransitionMatcher[0];
    private NPCMatcher[] NPCs = new NPCMatcher[0];
    private ItemMatcher[] items = new ItemMatcher[0];
    private MiscEntityMatcher[] miscEntities = new MiscEntityMatcher[0];
    private EventMatcher[] events = new EventMatcher[0];
    private WildBattleMatcher[] wildBattles = new WildBattleMatcher[0];

    public MapDataMatcher(Set<AreaMatcher> areaData,
                          Set<LocationTriggerMatcher> entities) {
        List<MapTransitionMatcher> mapTransitions = new ArrayList<>();
        List<NPCMatcher> npcs = new ArrayList<>();
        List<ItemMatcher> items = new ArrayList<>();
        List<MiscEntityMatcher> misc = new ArrayList<>();
        List<EventMatcher> events = new ArrayList<>();
        List<WildBattleMatcher> wildBattles = new ArrayList<>();

        for (LocationTriggerMatcher entity : entities) {
            switch (entity.getTriggerModelType()) {
                case MAP_TRANSITION:
                    mapTransitions.add((MapTransitionMatcher) entity);
                    break;
                case NPC:
                    npcs.add((NPCMatcher)entity);
                    break;
                case ITEM:
                    items.add((ItemMatcher) entity);
                    break;
                case TRIGGER_ENTITY:
                    misc.add((MiscEntityMatcher)entity);
                    break;
                case EVENT:
                    events.add((EventMatcher)entity);
                    break;
                case WILD_BATTLE:
                    wildBattles.add((WildBattleMatcher)entity);
                    break;
                default:
                    Global.error("Unknown trigger model type " + entity.getTriggerModelType() + "," +
                            "entity class: " + entity.getClass().getSimpleName());
                    break;
            }
        }

        this.areas = areaData.toArray(new AreaMatcher[0]);
        this.mapTransitions = mapTransitions.toArray(new MapTransitionMatcher[0]);
        this.NPCs = npcs.toArray(new NPCMatcher[0]);
        this.items = items.toArray(new ItemMatcher[0]);
        this.miscEntities = misc.toArray(new MiscEntityMatcher[0]);
        this.events = events.toArray(new EventMatcher[0]);
        this.wildBattles = wildBattles.toArray(new WildBattleMatcher[0]);
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

    public List<MapTransitionMatcher> getMapTransitions() {
        return Arrays.asList(this.mapTransitions);
    }

    public List<MiscEntityMatcher> getMiscEntities() {
        return Arrays.asList(this.miscEntities);
    }

    public List<EventMatcher> getEvents() {
        return Arrays.asList(this.events);
    }

    public List<WildBattleMatcher> getWildBattles() {
        return Arrays.asList(this.wildBattles);
    }

    public List<LocationTriggerMatcher> getAllEntities() {
        List<LocationTriggerMatcher> entities = new ArrayList<>();
        entities.addAll(getMapTransitions());
        entities.addAll(getNPCs());
        entities.addAll(getItems());
        entities.addAll(getMiscEntities());
        entities.addAll(getEvents());
        entities.addAll(getWildBattles());

        return entities;
    }

    public AreaData[] getAreaData() {
        AreaData[] areaData = new AreaData[this.areas.length];
        for (int i = 0; i < this.areas.length; i++) {
            if (i > 0 && !this.areas[i].hasColor()) {
                Global.error("Color required for maps with multiple areas.");
            }

            areaData[i] = this.areas[i].getAreaData();
        }

        return areaData;
    }

    // TODO: Move this to some sort of util location
    public static boolean hasOnlyOneNonEmpty(Object... objects) {
        return Arrays.stream(objects)
                .filter(object -> object != null)
                .count() == 1;
    }

    public static MapDataMatcher matchArea(String fileName, String areaDescription) {

        MapDataMatcher areaData = JsonUtils.deserialize(areaDescription, MapDataMatcher.class);
        JsonObject mappity = JsonUtils.deserialize(areaDescription, JsonObject.class);

        String areaDataJson = JsonUtils.getJson(areaData);
        String mapJson = JsonUtils.getJson(mappity);

        FileIO.writeToFile("out.txt", new StringBuilder(areaDataJson));
        FileIO.writeToFile("out2.txt", new StringBuilder(mapJson));

        if (!areaDataJson.equals(mapJson)) {
            Global.error("No dice");
        }

        areaDataJson = JsonUtils.getJson(areaData);

        FileIO.overwriteFile(fileName, new StringBuilder(areaDataJson));

        return areaData;
    }
}
