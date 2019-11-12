package pattern.map;

import map.area.AreaData;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.location.LocationTriggerMatcher;
import util.GeneralUtils;
import util.serialization.JsonMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;

public class MapDataMatcher implements JsonMatcher {
    private AreaMatcher[] areas;
    private MapTransitionMatcher[] mapTransitions;
    private NPCMatcher[] NPCs;
    private ItemMatcher[] items;
    private MiscEntityMatcher[] miscEntities;
    private EventMatcher[] events;
    private WildBattleAreaMatcher[] wildBattles;
    private FishingMatcher[] fishingSpots;

    public MapDataMatcher(Set<AreaMatcher> areaData, List<LocationTriggerMatcher> entities) {
        this.areas = areaData.stream()
                             .sorted(Comparator.comparing(areaMatcher -> areaMatcher.getAreaData().getAreaName()))
                             .toArray(AreaMatcher[]::new);

        Map<TriggerModelType, List<LocationTriggerMatcher>> triggerMap = new EnumMap<>(TriggerModelType.class);
        for (TriggerModelType triggerModelType : TriggerModelType.values()) {
            triggerMap.put(triggerModelType, new ArrayList<>());
        }

        for (LocationTriggerMatcher entity : entities) {
            triggerMap.get(entity.getTriggerModelType()).add(entity);
        }

        this.mapTransitions = fillTriggerArray(MapTransitionMatcher[]::new, triggerMap.get(TriggerModelType.MAP_TRANSITION), trigger -> (MapTransitionMatcher)trigger);
        this.NPCs = fillTriggerArray(NPCMatcher[]::new, triggerMap.get(TriggerModelType.NPC), trigger -> (NPCMatcher)trigger);
        this.items = fillTriggerArray(ItemMatcher[]::new, GeneralUtils.combine(triggerMap.get(TriggerModelType.ITEM), triggerMap.get(TriggerModelType.HIDDEN_ITEM)), trigger -> (ItemMatcher)trigger);
        this.miscEntities = fillTriggerArray(MiscEntityMatcher[]::new, triggerMap.get(TriggerModelType.MISC_ENTITY), trigger -> (MiscEntityMatcher)trigger);
        this.events = fillTriggerArray(EventMatcher[]::new, triggerMap.get(TriggerModelType.EVENT), trigger -> (EventMatcher)trigger);
        this.wildBattles = fillTriggerArray(WildBattleAreaMatcher[]::new, triggerMap.get(TriggerModelType.WILD_BATTLE), trigger -> (WildBattleAreaMatcher)trigger);
        this.fishingSpots = fillTriggerArray(FishingMatcher[]::new, triggerMap.get(TriggerModelType.FISHING), trigger -> (FishingMatcher)trigger);
    }

    private <T extends LocationTriggerMatcher> T[] fillTriggerArray(
            IntFunction<T[]> arrayGetter,
            List<LocationTriggerMatcher> triggerList,
            Function<LocationTriggerMatcher, T> mapper) {
        return triggerList.stream()
                          .map(mapper)
                          .toArray(arrayGetter);
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

    public List<WildBattleAreaMatcher> getWildBattles() {
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
        return JsonMatcher.fromFile(areaDescriptionFileName, MapDataMatcher.class);
    }
}
