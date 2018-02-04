package gui;

import main.Global;
import map.MapData;
import map.MapName;
import map.condition.ConditionSet;
import map.triggers.Trigger;
import pattern.map.ConditionsMatcher;
import util.FileIO;
import util.Folder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class GameData {
    private Map<MapName, MapData> maps;
    private Map<String, Trigger> triggers;
    private Map<String, ConditionSet> conditions;

    private IndexTileSet mapTiles;
    private IndexTileSet trainerTiles;
    private TileSet partyTiles;
    private TileSet pokemonTilesSmall;
    private TileSet pokemonTilesMedium;
    private TileSet pokemonTilesLarge;
    private TileSet pokedexTilesSmall;
    private TileSet pokedexTilesLarge;
    private TileSet itemTiles;
    private TileSet itemTilesLarge;
    private TileSet opponentTerrainTiles;
    private TileSet playerTerrainTiles;
    private TileSet weatherTiles;
    private TileSet medalTiles;

    public void loadData() {
        loadTiles();
        loadMaps();
    }

    private void loadTiles() {
        mapTiles = new IndexTileSet(Folder.MAP_TILES);
        trainerTiles = new IndexTileSet(Folder.TRAINER_TILES);
        partyTiles = new TileSet(Folder.PARTY_TILES);
        pokemonTilesSmall = new TileSet(Folder.POKEMON_TILES);
        pokemonTilesMedium = new TileSet(Folder.POKEMON_TILES, 2.3f);
        pokemonTilesLarge = new TileSet(Folder.POKEMON_TILES, 2.9f);
        pokedexTilesSmall = new TileSet(Folder.POKEDEX_TILES, .5f);
        pokedexTilesLarge = new TileSet(Folder.POKEDEX_TILES);
        itemTiles = new TileSet(Folder.ITEM_TILES);
        itemTilesLarge = new TileSet(Folder.ITEM_TILES, 2.9f);
        opponentTerrainTiles = new TileSet(Folder.TERRAIN_TILES, 2.4f);
        playerTerrainTiles = new TileSet(Folder.TERRAIN_TILES, 3.1f);
        weatherTiles = new TileSet(Folder.WEATHER_TILES);
        medalTiles = new TileSet(Folder.MEDAL_TILES, .65f);
    }

    private void loadMaps() {
        conditions = ConditionsMatcher.getConditions();

        triggers = new HashMap<>();
        Trigger.createCommonTriggers();

        maps = new HashMap<>();
        File mapsDirectory = new File(Folder.MAPS);
        for (File mapFolder : FileIO.listSubdirectories(mapsDirectory)) {
            MapData mapData = new MapData(mapFolder);
            maps.put(mapData.getName(), mapData);
        }
    }

    public MapData getMap(MapName name) {
        if (!maps.containsKey(name)) {
            Global.error("Cannot find map with name " + name);
        }

        return maps.get(name);
    }

    public ConditionSet getCondition(String conditionName) {
        if (!this.conditions.containsKey(conditionName)) {
            Global.error("Invalid condition name: " + conditionName);
        }

        return this.conditions.get(conditionName);
    }

    public boolean hasTrigger(String triggerName) {
        return triggers.containsKey(triggerName);
    }

    public Trigger getTrigger(String name) {
        return triggers.get(name);
    }

    public void addTrigger(Trigger trigger) {
        triggers.put(trigger.getName(), trigger);
    }

    public IndexTileSet getMapTiles() {
        return mapTiles;
    }

    public TileSet getOpponentTerrainTiles() {
        return opponentTerrainTiles;
    }

    public TileSet getPlayerTerrainTiles() {
        return playerTerrainTiles;
    }

    public TileSet getItemTiles() {
        return itemTiles;
    }

    public TileSet getItemTilesLarge() {
        return itemTilesLarge;
    }

    public TileSet getPokemonTilesLarge() {
        return pokemonTilesLarge;
    }

    public TileSet getPokemonTilesMedium() {
        return pokemonTilesMedium;
    }

    public TileSet getPokemonTilesSmall() {
        return pokemonTilesSmall;
    }

    // TODO: Eventually might want this to not be indexed and have an enum or something so it can be referenced easily
    public IndexTileSet getTrainerTiles() {
        return trainerTiles;
    }

    public TileSet getPartyTiles() {
        return partyTiles;
    }

    public TileSet getPokedexTilesSmall() {
        return pokedexTilesSmall;
    }

    public TileSet getPokedexTilesLarge() {
        return pokedexTilesLarge;
    }

    public TileSet getWeatherTiles() {
        return weatherTiles;
    }

    public TileSet getMedalTiles() {
        return medalTiles;
    }
}
