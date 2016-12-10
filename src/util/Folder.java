package util;

public abstract class Folder {

    // Main folder
    public static final String SRC = FileIO.makeFolderPath("src");
    public static final String REC = FileIO.makeFolderPath("rec");
    public static final String SAVES = FileIO.makeFolderPath("saves");

    // Code
    public static final String BATTLE = FileIO.makeFolderPath(SRC, "battle");
    public static final String ITEMS = FileIO.makeFolderPath(SRC, "item");
    public static final String POKEMON = FileIO.makeFolderPath(SRC, "pokemon");

    // Battle code
    public static final String ATTACK = FileIO.makeFolderPath(BATTLE, "attack");
    public static final String GENERIC_EFFECT = FileIO.makeFolderPath(BATTLE, "effect", "generic");

    // Pokemon code
    public static final String ABILITY = FileIO.makeFolderPath(POKEMON, "ability");

    // Resources
    public static final String MAPS = FileIO.makeFolderPath(REC, "maps");
    public static final String IMAGES = FileIO.makeFolderPath(REC, "images");
    public static final String GENERATOR = FileIO.makeFolderPath(REC, "generator");
    public static final String SOUND = FileIO.makeFolderPath(REC, "snd");

    // Image resources
    public static final String TILES = FileIO.makeFolderPath(IMAGES, "tiles");
    public static final String ATTACK_TILES = FileIO.makeFolderPath(IMAGES, "attackTiles");
    public static final String TYPE_TILES = FileIO.makeFolderPath(IMAGES, "typeTiles");
    public static final String BAG_TILES = FileIO.makeFolderPath(IMAGES, "bagTiles");

    // Tile set folders
    public static final String TERRAIN_TILES = FileIO.makeFolderPath(TILES, "terrainTiles");
    public static final String POKEMON_TILES = FileIO.makeFolderPath(TILES, "pokemonTiles");
    public static final String POKEDEX_TILES = FileIO.makeFolderPath(TILES, "pokedexTiles");
    public static final String PARTY_TILES = FileIO.makeFolderPath(TILES, "partyTiles");
    public static final String ITEM_TILES = FileIO.makeFolderPath(TILES, "itemTiles");
    public static final String MAP_TILES = FileIO.makeFolderPath(TILES, "mapTiles");
    public static final String TRAINER_TILES = FileIO.makeFolderPath(TILES, "trainerTiles");
    public static final String MAP_MAKER_TILES = FileIO.makeFolderPath(TILES, "mapMakerTiles");
}
