package main;

import util.FileIO;
import util.Folder;

import javax.swing.JOptionPane;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

// Loads and maintains game data.
public class Global {
	// Title of the window
	public static final String TITLE = "Pok\u00e9mon++";

	// Size of the game window
	public static final Dimension GAME_SIZE = new Dimension(800, 600);

	// Cute little Bulby icon
	public static final BufferedImage FRAME_ICON = FileIO.readImage(Folder.POKEMON_TILES + "001.png");

	// Frame rate the game runs at
	public static final int FRAME_RATE = 30;

	// The time(ms) between each frame.
	public static final long MS_BETWEEN_FRAMES = 1000 / FRAME_RATE;

	// The size of each tile in the map
	public static final int TILE_SIZE = 32;

	// The time(ms) it takes for the character to move from one tile on the map to another
	public static final int TIME_BETWEEN_TILES = 128;

	public static final String MONEY_SYMBOL = "\u00A5";

	// TODO: Need to make a RandomUtils class
	private static final Random RANDOM = new Random();

	public static boolean chanceTest(final int chance) {
		return chanceTest(chance, 100);
	}

	public static boolean chanceTest(final int numerator, final int denominator) {
		return getRandomInt(denominator) < numerator;
	}

	// Returns a random int with exclusive upper bound from range [0, upperBound)
	public static int getRandomInt(final int upperBound) {
		if (upperBound == 0) {
			return 0;
		}

		return RANDOM.nextInt(upperBound);
	}

	// Returns a random int from the inclusive range [lowerBound, upperBound]
	public static int getRandomInt(final int lowerBound, final int upperBound) {
		if (upperBound < lowerBound) {
			Global.error("Upper bound should never be lower than the lower bound. " +
					"(Lower: " + lowerBound + ", Upper: " + upperBound + ")");
		}

		return getRandomInt((upperBound - lowerBound + 1)) + lowerBound;
	}

	public static <T> T getRandomValue(T[] array) {
		return getRandomValue(Arrays.asList(array));
	}

	public static <T> T getRandomValue(List<T> list) {
		return list.get(getRandomIndex(list));
	}

	public static <T> int getRandomIndex(T[] array) {
		return getRandomIndex(Arrays.asList(array));
	}

	public static <T> int getRandomIndex(List<T> list) {
		return RANDOM.nextInt(list.size());
	}

	public static <T> void swap(T[] arr) {
		T temp = arr[0];
		arr[0] = arr[1];
		arr[1] = temp;
	}

	public static void error(String errorMessage) {
		JOptionPane.showMessageDialog(null, "Eggs aren't supposed to be green.", "ERROR", JOptionPane.ERROR_MESSAGE);
		Thread.dumpStack();
		System.err.println(errorMessage);
		System.exit(1);
	}

	public static int getPercentageIndex(int[] chances) {
		int sum = 0;
		int random = getRandomInt(100);

		for (int i = 0; i < chances.length; i++) {
			sum += chances[i];
			if (random < sum) {
				return i;
			}
		}
		
		Global.error("Chances array is improperly formatted.");
		return -1;
	}
}
