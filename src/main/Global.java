package main;

import java.awt.Dimension;

import javax.swing.JOptionPane;

import sound.SoundPlayer;

// Loads and maintains game data.
public class Global
{
	// Title of the window
	public static final String TITLE = "Pok\u00e9mon++";

	// Size of the game window
	public static final Dimension GAME_SIZE = new Dimension(800, 600);

	// Frame rate the game runs at
	public static final int FRAMERATE = 30;

	// The time(ms) between each frame.
	public static long MS_BETWEEN_FRAMES = 1000 / FRAMERATE;

	// The size of each tile in the map
	public static final int TILESIZE = 32;

	// The time(ms) it takes for the character to move from one tile on the map to another
	public static final int TIME_BETWEEN_TILES = 128;

	public static final String MONEY_SYMBOL = "\u00A5";
	
	public static SoundPlayer soundPlayer = new SoundPlayer();

	public static <T> void swap(T[] arr)
	{
		T temp = arr[0];
		arr[0] = arr[1];
		arr[1] = temp;
	}

	public static void error(String errorMessage)
	{
		JOptionPane.showMessageDialog(null, "Eggs aren't supposed to be green.", "ERROR", JOptionPane.ERROR_MESSAGE);
		Thread.dumpStack();
		System.err.println(errorMessage);
		System.exit(1);
	}

	public static int getPercentageIndex(int[] chances)
	{
		int sum = 0, random = (int) (Math.random() * 100);
		for (int i = 0; i < chances.length; i++)
		{
			sum += chances[i];
			if (random < sum) return i;
		}
		
		Global.error("Chances array is improperly formatted.");
		return -1;
	}
}
