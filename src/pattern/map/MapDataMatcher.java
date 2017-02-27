package pattern.map;

import com.google.gson.JsonObject;
import main.Global;
import map.AreaData;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.generic.LocationTriggerMatcher;
import util.FileIO;
import util.SerializationUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MapDataMatcher {

    private AreaMatcher[] areas = new AreaMatcher[0];
    private MapTransitionMatcher[] mapTransitions = new MapTransitionMatcher[0];
    private NPCMatcher[] NPCs = new NPCMatcher[0];
    private ItemMatcher[] items = new ItemMatcher[0];
    private MiscEntityMatcher[] miscEntities = new MiscEntityMatcher[0];
    private EventMatcher[] events = new EventMatcher[0];
    private WildBattleMatcher[] wildBattles = new WildBattleMatcher[0];
    private FishingMatcher[] fishingSpots = new FishingMatcher[0];

    public MapDataMatcher(Set<AreaMatcher> areaData,
                          List<LocationTriggerMatcher> entities) {

        this.areas = areaData.stream()
                .sorted((first, second) -> first.getAreaData().getAreaName().compareTo(second.getAreaData().getAreaName()))
                .collect(Collectors.toList())
                .toArray(new AreaMatcher[0]);

        Map<TriggerModelType, List<LocationTriggerMatcher>> triggerMap = new EnumMap<>(TriggerModelType.class);
        for (TriggerModelType triggerModelType : TriggerModelType.values()) {
            triggerMap.put(triggerModelType, new ArrayList<>());
        }

        for (LocationTriggerMatcher entity : entities) {
            triggerMap.get(entity.getTriggerModelType()).add(entity);
        }

        this.mapTransitions = fillTriggerArray(this.mapTransitions, triggerMap.get(TriggerModelType.MAP_TRANSITION), trigger -> (MapTransitionMatcher)trigger);
        this.NPCs = fillTriggerArray(this.NPCs, triggerMap.get(TriggerModelType.NPC), trigger -> (NPCMatcher)trigger);
        this.items = fillTriggerArray(this.items, triggerMap.get(TriggerModelType.ITEM), trigger -> (ItemMatcher)trigger);
        this.miscEntities = fillTriggerArray(this.miscEntities, triggerMap.get(TriggerModelType.MISC_ENTITY), trigger -> (MiscEntityMatcher)trigger);
        this.events = fillTriggerArray(this.events, triggerMap.get(TriggerModelType.EVENT), trigger -> (EventMatcher)trigger);
        this.wildBattles = fillTriggerArray(this.wildBattles, triggerMap.get(TriggerModelType.WILD_BATTLE), trigger -> (WildBattleMatcher)trigger);
        this.fishingSpots = fillTriggerArray(this.fishingSpots, triggerMap.get(TriggerModelType.FISHING), trigger -> (FishingMatcher)trigger);
    }

    private <T extends LocationTriggerMatcher> T[] fillTriggerArray(
            T[] array,
            List<LocationTriggerMatcher> triggerList,
            Function<LocationTriggerMatcher, T> mapper) {
        return triggerList.stream()
                .map(mapper)
                .collect(Collectors.toList())
                .toArray(array);
    }

    public AreaMatcher getDefaultArea() {
        return this.areas[0];
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

    public List<FishingMatcher> getFishingSpots() {
        return Arrays.asList(this.fishingSpots);
    }

    public List<LocationTriggerMatcher> getAllEntities() {
        List<LocationTriggerMatcher> entities = new ArrayList<>();
        entities.addAll(getMapTransitions());
        entities.addAll(getNPCs());
        entities.addAll(getItems());
        entities.addAll(getMiscEntities());
        entities.addAll(getEvents());
        entities.addAll(getWildBattles());
        entities.addAll(getFishingSpots());

        return entities;
    }

    public AreaData[] getAreaData() {
        AreaData[] areaData = new AreaData[this.areas.length];
        for (int i = 0; i < this.areas.length; i++) {
            areaData[i] = this.areas[i].getAreaData();
        }

        return areaData;
    }

    public static MapDataMatcher matchArea(String areaDescriptionFileName) {
        String areaDescription = FileIO.readEntireFileWithReplacements(areaDescriptionFileName, false);

        MapDataMatcher areaData = SerializationUtils.deserializeJson(areaDescription, MapDataMatcher.class);
        JsonObject mappity = SerializationUtils.deserializeJson(areaDescription, JsonObject.class);

        String areaDataJson = SerializationUtils.getJson(areaData);
        String mapJson = SerializationUtils.getJson(mappity);

        if (!areaDataJson.equals(mapJson)) {
            Global.error("No dice");
        }

        areaDataJson = SerializationUtils.getJson(areaData);

        FileIO.overwriteFile(areaDescriptionFileName, new StringBuilder(areaDataJson));

        return areaData;
    }
}
