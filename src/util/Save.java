package util;

import gui.view.MainMenuView.Theme;
import main.Game;
import main.Global;
import sound.SoundPlayer;
import trainer.CharacterData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
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
		CharacterData player = Game.getPlayer();
		player.updateTimePlayed();
		
		File saveDir = new File(Folder.SAVES);
		if (!saveDir.exists()) {
			saveDir.mkdirs(); // TODO: file io
		}
		
		try {
			FileOutputStream fout = new FileOutputStream(getSavePath(player.getFileNum()));
			ObjectOutputStream out = new ObjectOutputStream(fout);
			
			out.writeObject(player);
			out.close();
			fout.close();
			
			// For the preview of the saves on the main menu -- output the player's name, time, number of badges, and number of pokemon seen
			PrintStream prevOut = new PrintStream(getPreviewPath(player.getFileNum()));
			prevOut.print(player.getName() + " " + player.getSeconds() + " " + player.getNumBadges() + " " + player.getPokedex().numSeen());
			prevOut.close();
		}
		catch (IOException z) {
			Global.error("Oh no! That didn't save quite right!");
		}
	}
	
	public static CharacterData load(int fileNum) {
		CharacterData loadChar = null;
		try {
			FileInputStream fin = new FileInputStream(getSavePath(fileNum));
			ObjectInputStream in = new ObjectInputStream(fin);
			
			loadChar = (CharacterData) in.readObject();
			loadChar.initialize();
			
			in.close();
			fin.close();
		}
		catch(IOException | ClassNotFoundException y) {
			Global.error("Oh no! That didn't load quite right!");
		}
		catch (NullPointerException n) {
			Global.error("Someone's been trying to cheat and edit this save file! Commence deletion!");
		}
		
		return loadChar;
	}

	public static void deleteSave(int index) {
		FileIO.deleteFile(getSavePath(index));
		FileIO.deleteFile(getPreviewPath(index));
	}

	// TODO: separate file
	public static class SavePreviewInfo {
		private final String name;
		private final long time;
		private final int badges;
		private final int pokemonSeen;

		SavePreviewInfo(Scanner in) {
			this.name = in.next();
			this.time = in.nextLong();
			this.badges = in.nextInt();
			this.pokemonSeen = in.nextInt();
		}
		
		public String getName() {
			return this.name;
		}
		
		public long getTime() {
			return this.time;
		}
		
		public int getBadges() {
			return this.badges;
		}
		
		public int getPokemonSeen() {
			return this.pokemonSeen;
		}
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
		try {
			PrintStream settingsOut = new PrintStream(FileName.SAVE_SETTINGS);
			settingsOut.print(theme.ordinal() + " " + (SoundPlayer.soundPlayer.isMuted() ? 1 : 0));
			settingsOut.close();
		}
		catch (IOException e) {
			Global.error("Could not create options file. Wut");
		}
	}
	
	public static Theme loadSettings() {
		Theme theme;
		
		File saveDir = new File(Folder.SAVES);
		if (!saveDir.exists()) {
			saveDir.mkdirs();
		}
		
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
