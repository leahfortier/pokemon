package main;

import gui.view.MainMenuView.Theme;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Scanner;

import trainer.CharacterData;

public class Save
{
	public static final int NUM_SAVES = 3;
	
	private static final String SAVE_FOLDER_PATH = FileIO.makePath("saves");
	private static final String SETTINGS_PATH = SAVE_FOLDER_PATH + "settings.txt";
	
	public static String formatTime(long l)
	{
		return (l/(3600) + ":" + String.format("%02d", ((l%3600)/60)));
	}
	
	private static String getSavePath(int fileNum)
	{
		return SAVE_FOLDER_PATH + "File " + (fileNum + 1) + ".ser";
	}
	
	private static String getPreviewPath(int fileNum) 
	{
		return SAVE_FOLDER_PATH + "Preview " + (fileNum + 1) + ".out";
	}
	
	public static void save(CharacterData player)
	{
		//printGlobals();
		
		try
		{
			player.updateTimePlayed();
			
			File saveDir = new File(SAVE_FOLDER_PATH);
			if (!saveDir.exists())
				saveDir.mkdirs();
			
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
		catch (IOException z)
		{
			Global.error("Oh no! That didn't save quite right!");
		}
	}
	
	public static CharacterData load(int fileNum, Game game)
	{
		CharacterData loadChar = null;
		
		//updateSerVariables();
		
		try 
		{
			FileInputStream fin = new FileInputStream(getSavePath(fileNum));
			ObjectInputStream in = new ObjectInputStream(fin);
			
			loadChar = (CharacterData) in.readObject();
			loadChar.initialize(game);
			
			in.close();
			fin.close();
		}
		catch(IOException | ClassNotFoundException y)
		{
			Global.error("Oh no! That didn't load quite right!");
		}
		catch (NullPointerException n)
		{
			Global.error("Someone's been trying to cheat and edit this save file! Commence deletion!");
		}
		
		//loadChar.updateGlobals(false);
		
		return loadChar;
	}
	
	private static void updateSerVariables(int fileNum) 
	{
		try 
		{
			//Replace bytes of renamed variable name
			FileInputStream fin = new FileInputStream(SAVE_FOLDER_PATH + "File" + (fileNum + 1) + ".ser");
			byte[] bytes = new byte[fin.available()];
			fin.read(bytes);
			fin.close();
			
			boolean edited = false;
			
			// TODO: Can this be deleted? It looks fucking crazy
			byte[][][] variablesToUpdate = new byte[][][] {
					
					//Move.move to Move.attack
					{
						//Move.move: 00 04 6D 6F 76 65
						//extra: 00 04 75 73 65 64 4C 00 04 6D 6F 76 65 74
						{0x00, 0x04, 0x75, 0x73, 0x65, 0x64, 0x4C, 0x00, 0x04, 0x6D, 0x6F, 0x76, 0x65, 0x74},
						//Move.attack: 00 06 61 74 74 61 63 6B
						//extra: 00 04 75 73 65 64 4C 00 06 61 74 74 61 63 6B 74
						{0x00, 0x04, 0x75, 0x73, 0x65, 0x64, 0x4C, 0x00, 0x06, 0x61, 0x74, 0x74, 0x61, 0x63, 0x6B, 0x74}
					}
					//next
			};
			
			for (int currVariable = 0; currVariable < variablesToUpdate.length; ++currVariable)
			{
				byte[] newBytes = updateSerVariables(bytes, variablesToUpdate[currVariable][0], variablesToUpdate[currVariable][1]);
				if (newBytes != null) 
				{
					bytes = newBytes;
					edited = true;
				}
			}

			// Replacement was found, resave the file.
			if (edited) 
			{
				FileOutputStream out = new FileOutputStream(getSavePath(fileNum));
				out.write(bytes);
				out.close();
			}
		} 
		catch (IOException ex)
		{
			Global.error("Couldn't update Move variable name.");
		}
	}
	
	private static byte[] updateSerVariables(byte[] bytes, byte[] find, byte[] replace)
	{
		boolean edited = false;
		
		StringBuilder findString = new StringBuilder();
		for(byte b: find) 
		{
			findString.append((char)b);
		}
		
		StringBuilder replaceString = new StringBuilder();
		for(byte b: replace) 
		{
			replaceString.append((char)b);
		}
		
		//Loop through the entire array of bytes.
		for (int curr = 0; curr< bytes.length; ++curr) 
		{
			//Search the bytes for the search array.
			int currLoc;
			for (currLoc = 0; currLoc < find.length && currLoc < bytes.length && bytes[curr+currLoc] == find[currLoc]; ++currLoc);
			
			//If searched the entire search array, location was found.
			if(currLoc == find.length)
			{
				System.out.println("Updating Serializable variable: " +findString.toString() +" with: "+replaceString.toString());
				edited = true;
				
				//Make a copy of the bytes with the new length.
				int dif = replace.length - find.length;
				
				//Move the end of the array over by the difference in the find and replace arrays.
				//If difference is smaller, move bytes before copying the array.
				if (dif < 0)
				{
					for(int newPos = bytes.length-1; newPos > curr; --newPos)
					{
						bytes[newPos+dif] = bytes[newPos];
					}
				}
				
				//Update the size of the bytes array.
				bytes = Arrays.copyOf(bytes, bytes.length + dif);
				
				//Move the end of the array over by the difference in the find and replace arrays.
				//If difference is larger, move bytes after copying the array. 
				if(dif > 0)
				{
					for(int newPos = bytes.length-1; newPos > curr; --newPos)
					{
						bytes[newPos] = bytes[newPos-dif];
					}
				}
				
				//Add the replace array.
				for(int newPos = 0; newPos < replace.length; ++newPos)
				{
					bytes[newPos+curr] = replace[newPos];
				}
			}
		}
		
		return edited? bytes: null;
	}

	
	public static void deleteSave(int index) 
	{
		FileIO.deleteFile(getSavePath(index));
		FileIO.deleteFile(getPreviewPath(index));
	}
	
	public static class SavePreviewInfo
	{
		private String name;
		private long time;
		private int badges;
		private int pokemonSeen;

		public SavePreviewInfo(String name, long time, int badges, int pokemonSeen)
		{
			this.name = name;
			this.time = time;
			this.badges = badges;
			this.pokemonSeen = pokemonSeen;
		}
		
		public String getName()
		{
			return this.name;
		}
		
		public long getTime()
		{
			return this.time;
		}
		
		public int getBadges()
		{
			return this.badges;
		}
		
		public int getPokemonSeen()
		{
			return this.pokemonSeen;
		}
	}
	
	public static SavePreviewInfo[] updateSaveData()
	{
		SavePreviewInfo[] saveInfo = new SavePreviewInfo[NUM_SAVES];
		for (int i = 0; i < NUM_SAVES; i++)
		{
			File file = new File(getPreviewPath(i));
			if (file.exists())
			{
				Scanner in = FileIO.openFile(file);
				saveInfo[i] = new SavePreviewInfo(in.next(), in.nextLong(), in.nextInt(), in.nextInt());
				in.close();
			}
		}
		
		return saveInfo;
	}

	public static void saveSettings(Theme theme)
	{
		try
		{
			PrintStream settingsOut = new PrintStream(SETTINGS_PATH);
			settingsOut.print(theme.ordinal() + " " + (Global.soundPlayer.isMuted() ? 1 : 0));
			settingsOut.close();
		}
		catch (IOException e)
		{
			Global.error("Could not create options file. Wut");
		}
	}
	
	public static Theme loadSettings()
	{
		Theme theme = null;
		
		File saveDir = new File(SAVE_FOLDER_PATH);
		if (!saveDir.exists()) 
			saveDir.mkdirs();
		
		File file = new File(SETTINGS_PATH);
		if (file.exists())
		{
			Scanner in = FileIO.openFile(file);
			theme = Theme.values()[in.nextInt()];
			
			// Is muted
			if (in.nextInt() == 1)
				Global.soundPlayer.toggleMusic();
		}
		else
		{
			// Set to basic if no settings are currently saved
			theme = Theme.BASIC;
			Save.saveSettings(theme);
		}
		
		return theme;
	}
}
