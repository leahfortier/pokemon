package save;

import trainer.player.Player;
import util.Serializable;

import java.io.File;

public class SaveInfo {
    private final Player player;

    private final String name;
    private final long seconds;
    private final int badges;
    private final int pokemonSeen;

    private SaveInfo(File saveFile) {
        player = Serializable.fromFile(saveFile, Player.class);
        player.initialize();

        this.name = player.getName();
        this.seconds = player.getSeconds();
        this.badges = player.getNumBadges();
        this.pokemonSeen = player.getPokedex().numSeen();
    }

    public Player getPlayer() {
        return this.player;
    }

    public String getName() {
        return this.name;
    }

    public long getSeconds() {
        return this.seconds;
    }

    public int getBadges() {
        return this.badges;
    }

    public int getPokemonSeen() {
        return this.pokemonSeen;
    }

    public static SaveInfo[] updateSaveData() {
        SaveInfo[] saveInfo = new SaveInfo[Save.NUM_SAVES];
        for (int i = 0; i < Save.NUM_SAVES; i++) {
            File file = new File(Save.getSavePath(i));
            if (file.exists()) {
                saveInfo[i] = new SaveInfo(file);
            }
        }

        return saveInfo;
    }
}
