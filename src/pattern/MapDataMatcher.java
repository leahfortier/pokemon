package pattern;

import com.google.gson.JsonObject;
import main.Global;
import map.AreaData;
import util.FileIO;
import util.JsonUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class MapDataMatcher {

    private AreaMatcher[] areas = new AreaMatcher[0];
    private NPCMatcher[] NPCs = new NPCMatcher[0];
    private ItemMatcher[] items = new ItemMatcher[0];
    private MapTransitionMatcher[] mapExits = new MapTransitionMatcher[0];
    private TriggerMatcher[] triggerData = new TriggerMatcher[0];
    private TriggerMatcher[] miscEntities = new TriggerMatcher[0];

    public MapDataMatcher(Set<AreaMatcher> areaData,
                          Set<MapMakerEntityMatcher> entities,
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
        this.miscEntities = misc.toArray(new TriggerMatcher[0]);
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
        return Arrays.asList(this.miscEntities);
    }

    public List<TriggerMatcher> getTriggerData() {
        return Arrays.asList(this.triggerData);
    }

    public List<MapMakerEntityMatcher> getEntities() {
        List<MapMakerEntityMatcher> entities = new ArrayList<>();
        entities.addAll(getNPCs());
        entities.addAll(getItems());
        entities.addAll(getMapExits());
        entities.addAll(getMiscEntities());

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
