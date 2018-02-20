package pattern.map;

import com.google.gson.JsonObject;
import main.Global;
import map.area.AreaData;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.JsonMatcher;
import pattern.action.ActionMatcher;
import pattern.action.ActionMatcher.ChoiceActionMatcher;
import pattern.action.ChoiceMatcher;
import pattern.action.NPCInteractionMatcher;
import pattern.generic.LocationTriggerMatcher;
import util.FileIO;
import util.GeneralUtils;
import util.SerializationUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MapDataMatcher implements JsonMatcher {
    private AreaMatcher[] areas = new AreaMatcher[0];
    private MapTransitionMatcher[] mapTransitions = new MapTransitionMatcher[0];
    private NPCMatcher[] NPCs = new NPCMatcher[0];
    private ItemMatcher[] items = new ItemMatcher[0];
    private MiscEntityMatcher[] miscEntities = new MiscEntityMatcher[0];
    private EventMatcher[] events = new EventMatcher[0];
    private WildBattleAreaMatcher[] wildBattles = new WildBattleAreaMatcher[0];
    private FishingMatcher[] fishingSpots = new FishingMatcher[0];

    public MapDataMatcher(Set<AreaMatcher> areaData, List<LocationTriggerMatcher> entities) {
        this.areas = areaData.stream()
                             .sorted(Comparator.comparing(areaMatcher -> areaMatcher.getAreaData().getAreaName()))
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
        this.items = fillTriggerArray(this.items, GeneralUtils.combine(triggerMap.get(TriggerModelType.ITEM), triggerMap.get(TriggerModelType.HIDDEN_ITEM)), trigger -> (ItemMatcher)trigger);
        this.miscEntities = fillTriggerArray(this.miscEntities, triggerMap.get(TriggerModelType.MISC_ENTITY), trigger -> (MiscEntityMatcher)trigger);
        this.events = fillTriggerArray(this.events, triggerMap.get(TriggerModelType.EVENT), trigger -> (EventMatcher)trigger);
        this.wildBattles = fillTriggerArray(this.wildBattles, triggerMap.get(TriggerModelType.WILD_BATTLE), trigger -> (WildBattleAreaMatcher)trigger);
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
//        return SerializationUtils.deserializeJsonFile(areaDescriptionFileName, MapDataMatcher.class);

        String jsonContents = FileIO.readEntireFileWithReplacements(areaDescriptionFileName, false);

        MapDataMatcher deserialized = SerializationUtils.deserializeJson(jsonContents, MapDataMatcher.class);
        JsonObject mappity = SerializationUtils.deserializeJson(jsonContents, JsonObject.class);

        for (NPCMatcher npc : deserialized.getNPCs()) {
            for (NPCInteractionMatcher interaction : npc.getInteractionMatcherList()) {
//                ActionMatcher[] npcActions = interaction.npcActions;
//                ActionMatcher2[] npcActions2 = new ActionMatcher2[npcActions.length];
//                for (int i = 0; i < npcActions.length; i++) {
//                    npcActions2[i] = npcActions[i].getNewMatcher();
//                }
//                interaction.npcActions2 = npcActions2;
//                interaction.npcActions = null;

//                interaction.npcActions = interaction.npcActions2;
//                interaction.npcActions2 = null;

                ActionMatcher[] npcActions = interaction.npcActions;
                updateActions(npcActions);
            }
        }

        for (EventMatcher event : deserialized.getEvents()) {
//            ActionMatcher[] actions = event.actions;
//            ActionMatcher2[] actions2 = new ActionMatcher2[actions.length];
//            for (int i = 0; i < actions.length; i++) {
//                actions2[i] = actions[i].getNewMatcher();
//            }
//            event.actions2 = actions2;
//            event.actions = null;

//            event.actions = event.actions2;
//            event.actions2 = null;
            updateActions(event.actions);
        }

        for (MiscEntityMatcher event : deserialized.getMiscEntities()) {
//            ActionMatcher[] actions = event.actions;
//            ActionMatcher2[] actions2 = new ActionMatcher2[actions.length];
//            for (int i = 0; i < actions.length; i++) {
//                actions2[i] = actions[i].getNewMatcher();
//            }
//            event.actions2 = actions2;
//            event.actions = null;

//            event.actions = event.actions2;
//            event.actions2 = null;

            updateActions(event.actions);
        }

        String formattedJson = SerializationUtils.getJson(deserialized);
        String mapJson = SerializationUtils.getJson(mappity);

//        FileIO.writeToFile("out.txt", formattedJson);
//        FileIO.writeToFile("out2.txt", mapJson);

        if (!formattedJson.equals(mapJson)) {
            Global.error("No dice: " + areaDescriptionFileName);
        }

        FileIO.overwriteFile(areaDescriptionFileName, formattedJson);

        return deserialized;
    }

    private static void updateActions(ActionMatcher[] actions) {
        for (int i = 0; i < actions.length; i++) {
//            if (actions[i] instanceof UpdateActionMatcher) {
//                UpdateActionMatcher updateActionMatcher = (UpdateActionMatcher)actions[i];
//                updateActionMatcher.interactionName = updateActionMatcher.update;
//                updateActionMatcher.update = null;
//            }

//            if (actions[i] instanceof TriggerActionMatcher) {
//                TriggerActionMatcher triggerActionMatcher = (TriggerActionMatcher)actions[i];
//                String contents = triggerActionMatcher.triggerContents;
//                switch (triggerActionMatcher.getTriggerType()) {
//                    case DIALOGUE:
//                        actions[i] = new DialogueActionMatcher(contents);
//                        break;
//                    case MOVE_PLAYER:
//                        actions[i] = new MovePlayerActionMatcher(contents);
//                        break;
//                    case HEAL_PARTY:
//                        actions[i] = new HealPartyActionMatcher();
//                        break;
//                    case CHANGE_VIEW:
//                        actions[i] = new ChangeViewActionMatcher(ViewMode.valueOf(contents));
//                        break;
//                    case GIVE_ITEM:
//                        actions[i] = new GiveItemActionMatcher(ItemNamesies.getValueOf(contents), 1);
//                        break;
//                    case RELOAD_MAP:
//                        actions[i] = new ReloadMapActionMatcher();
//                        break;
//                    case BADGE:
//                        actions[i] = new BadgeActionMatcher(Badge.valueOf(contents));
//                        break;
//                    case MOVE_NPC:
//                        MoveNPCTriggerMatcher moveNPCTriggerMatcher = SerializationUtils.deserializeJson(contents, MoveNPCTriggerMatcher.class);
//                        actions[i] = new MoveNpcActionMatcher(moveNPCTriggerMatcher.getNpcEntityName(), moveNPCTriggerMatcher.getEndEntranceName(), moveNPCTriggerMatcher.endLocationIsPlayer());
//                        break;
//                    case GIVE_POKEMON:
//                        PokemonMatcher pokemonMatcher = SerializationUtils.deserializeJson(contents, PokemonMatcher.class);
//                        actions[i] = new GivePokemonActionMatcher(pokemonMatcher);
//                        break;
//                    case TRADE_POKEMON:
//                        TradePokemonMatcher tradePokemonMatcher = SerializationUtils.deserializeJson(contents, TradePokemonMatcher.class);
//                        actions[i] = new TradePokemonActionMatcher(tradePokemonMatcher.getTradePokemon(), tradePokemonMatcher.getRequested());
//                        break;
//                    case DAY_CARE:
//                        actions[i] = new DayCareActionMatcher();
//                        break;
//                    case SOUND:
//                        actions[i] = new SoundActionMatcher(SoundTitle.valueOf(contents));
//                        break;
//                    case MEDAL_COUNT:
//                        actions[i] = new MedalCountActionMatcher(MedalTheme.valueOf(contents));
//                        break;
//                    default:
//                        System.err.println(triggerActionMatcher.getTriggerType());
//                        break;
//                }
//            }

            if (actions[i] instanceof ChoiceActionMatcher) {
                ChoiceActionMatcher c = (ChoiceActionMatcher)actions[i];
                for (ChoiceMatcher choice : c.choices) {
//                            ActionMatcher[] choiceActions = choice.actions;
//                            ActionMatcher2[] choiceActions2 = new ActionMatcher2[choiceActions.length];
//                            for (int i = 0; i < choiceActions.length; i++) {
//                                choiceActions2[i] = choiceActions[i].getNewMatcher();
//                            }
//                            choice.actions2 = choiceActions2;
//                            choice.actions = null;

//                            choice.actions = choice.actions2;
//                            choice.actions2 = null;
                    updateActions(choice.actions);
                }
            }
        }
    }
}
