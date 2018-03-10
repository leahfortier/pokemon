package save;

import gui.view.mainmenu.Theme;
import sound.SoundPlayer;
import util.FileIO;
import util.FileName;
import util.Folder;
import util.GeneralUtils;
import util.Serializable;

import java.io.File;

public class Settings implements Serializable {
    private static final long serialVersionUID = 1L;

    private Theme theme;
    private boolean isMuted;

    private Settings() {
        // Set theme to scenic by default
        this.theme = Theme.SCENIC;
        this.save();
    }

    public Theme getTheme() {
        return this.theme;
    }

    public void toggleTheme() {
        theme = GeneralUtils.wrapIncrementValue(Theme.values(), theme.ordinal(), 1);
    }

    public void save() {
        this.isMuted = SoundPlayer.instance().isMuted();
        this.serializeToFile(FileName.SAVE_SETTINGS);
    }

    public static Settings load() {
        Settings settings;

        FileIO.createFolder(Folder.SAVES);

        File file = new File(FileName.SAVE_SETTINGS);
        if (file.exists()) {
            settings = (Settings)Serializable.fromFile(file);
        } else {
            settings = new Settings();
        }

        SoundPlayer.instance().setMuted(settings.isMuted);
        return settings;
    }
}
