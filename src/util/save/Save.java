package util.save;

import gui.view.mainmenu.Theme;
import main.Game;
import main.Global;
import sound.SoundPlayer;
import trainer.player.Player;
import util.FileIO;
import util.FileName;
import util.Folder;
import util.SerializationUtils;

import java.io.File;
import java.util.Scanner;

public final class Save {
	public static final int NUM_SAVES = 3;
	
	// Utility class -- should not be instantiated
	private Save() {
		Global.error("Save class cannot be instantiated.");
	}
	
	public static String formatTime(long l) {
		return String.format("%d:%02d", l/3600, (l%3600)/60);
	}
	
	private static String getSavePath(int fileNum) {
		return Folder.SAVES + "File " + (fileNum + 1) + ".ser";
	}
	
	private static String getPreviewPath(int fileNum) {
		return Folder.SAVES + "Preview " + (fileNum + 1) + ".out";
	}
	
	public static void save() {
		Player player = Game.getPlayer();
		player.updateTimePlayed();

		FileIO.createFolder(Folder.SAVES);
		SerializationUtils.serializeToFile(getSavePath(player.getFileNum()), player);

		// For the preview of the saves on the main menu -- output the player's name, time, number of badges, and number of pokemon seen
		String preview = player.getName() + " " + player.getSeconds() + " " + player.getNumBadges() + " " + player.getPokedex().numSeen();
		FileIO.writeToFile(getPreviewPath(player.getFileNum()), preview);
	}
	
	public static Player load(int fileNum) {
		Player loadedPlayer = (Player) SerializationUtils.deserializeFromFile(getSavePath(fileNum));
		loadedPlayer.initialize();
		
		return loadedPlayer;
	}

	public static void deleteSave(int index) {
		FileIO.deleteFile(getSavePath(index));
		FileIO.deleteFile(getPreviewPath(index));
	}

	public static SavePreviewInfo[] updateSaveData() {
		SavePreviewInfo[] saveInfo = new SavePreviewInfo[NUM_SAVES];
		for (int i = 0; i < NUM_SAVES; i++) {
			File file = new File(getPreviewPath(i));
			if (file.exists()) {
				Scanner in = FileIO.openFile(file);
				saveInfo[i] = new SavePreviewInfo(in);
				in.close();
			}
		}
		
		return saveInfo;
	}

	public static void saveSettings(Theme theme) {
		String settings = theme.ordinal() + " " + (SoundPlayer.soundPlayer.isMuted() ? 1 : 0);
		FileIO.writeToFile(FileName.SAVE_SETTINGS, settings);
	}
	
	public static Theme loadSettings() {
		Theme theme;

		FileIO.createFolder(Folder.SAVES);
		
		File file = new File(FileName.SAVE_SETTINGS);
		if (file.exists()) {
			Scanner in = FileIO.openFile(file);
			theme = Theme.values()[in.nextInt()];
			
			// Is muted
			if (in.nextInt() == 1) {
				SoundPlayer.soundPlayer.toggleMusic();
			}
		}
		else {
			// Set to basic if no settings are currently saved
			theme = Theme.BASIC;
			Save.saveSettings(theme);
		}
		
		return theme;
	}
}
