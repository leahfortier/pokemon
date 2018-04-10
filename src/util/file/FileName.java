package util.file;

public abstract class FileName {
    public static final String POKEMON_INFO = Folder.REC + "pokemoninfo.txt";

    public static final String SAVE_SETTINGS = Folder.SAVES + "settings.ser";

    public static final String CONDITIONS = Folder.MAPS + "conditions.txt";

    public static final String BASE_EVOLUTIONS = Folder.GENERATOR + "BaseEvolutions.txt";
    public static final String FONT_METRICS = Folder.GENERATOR + "fontMetrics.txt";
    public static final String OVERRIDE = Folder.GENERATOR + "override.txt";
    public static final String TM_LIST = Folder.GENERATOR + "tmList.txt";
    public static final String INTERFACES = Folder.GENERATOR + "interfaces.txt";

    public static final String MOVE_SCRIPT_INPUT = Folder.SCRIPTS + "moves.in";
    public static final String MOVE_PARSER = Folder.SCRIPTS + "moves.out";
    public static final String MOVE_API = Folder.SCRIPTS + "api_moves.out";

    public static final String MAP_TILES_INDEX = getIndexFileName(Folder.MAP_TILES);
    public static final String TRAINER_TILES_INDEX = getIndexFileName(Folder.TRAINER_TILES);
    public static final String MAP_MAKER_TILES_INDEX = getIndexFileName(Folder.MAP_MAKER_TILES);

    public static String getIndexFileName(final String folderName) {
        return folderName + "index.txt";
    }
}
