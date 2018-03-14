package save;

import main.Game;
import main.Global;
import trainer.player.Player;
import trainer.player.medal.MedalTheme;
import util.file.FileIO;
import util.file.Folder;

public final class Save {
    public static final int NUM_SAVES = 3;

    // Utility class -- should not be instantiated
    private Save() {
        Global.error(this.getClass().getSimpleName() + " class cannot be instantiated.");
    }

    static String getSavePath(int fileNum) {
        return Folder.SAVES + "File " + (fileNum + 1) + ".ser";
    }

    public static void save() {
        Player player = Game.getPlayer();
        player.updateTimePlayed();
        player.getMedalCase().increase(MedalTheme.TIMES_SAVED);

        FileIO.createFolder(Folder.SAVES);
        player.serializeToFile(getSavePath(player.getFileNum()));
    }

    public static void deleteSave(int index) {
        FileIO.deleteFile(getSavePath(index));
    }
}
