package main;

import sound.SoundPlayer;

import javax.swing.JOptionPane;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

// Loads and maintains game data.
public class Global {
	// Title of the window
	public static final String TITLE = "Pok\u00e9mon++";

	// Size of the game window
	public static final Dimension GAME_SIZE = new Dimension(800, 600);

	// Frame rate the game runs at
	public static final int FRAME_RATE = 30;

	// The time(ms) between each frame.
	public static long MS_BETWEEN_FRAMES = 1000 / FRAME_RATE;

	// The size of each tile in the map
	public static final int TILE_SIZE = 32;

	// The time(ms) it takes for the character to move from one tile on the map to another
	public static final int TIME_BETWEEN_TILES = 128;

	public static final String MONEY_SYMBOL = "\u00A5";

	public static final SoundPlayer soundPlayer = new SoundPlayer();

	private static final Random RANDOM = new Random();

	public static boolean chanceTest(final int numerator, final int denominator) {
		return getRandomInt(denominator) < numerator;
	}

	// Returns a random int with exclusive upper bound from range [0, upperBound)
	public static int getRandomInt(final int upperBound) {
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
		return list.get(Global.RANDOM.nextInt(list.size()));
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
		int sum = 0, random = (int) (Math.random() * 100);
		for (int i = 0; i < chances.length; i++) {
			sum += chances[i];
			if (random < sum) return i;
		}
		
		Global.error("Chances array is improperly formatted.");
		return -1;
	}
	
	private static Class<?>[] getParameterTypes(Object[] parameterValues) {
		Class<?>[] parameterTypes = new Class<?>[parameterValues.length];
		for (int i = 0; i < parameterValues.length; i++) {
			parameterTypes[i] = parameterValues[i].getClass();
		}
		
		return parameterTypes;
	}
	
	public static Object dynamicMethodInvoke(Class<?> className, String methodName, Object invokee, Object... parameterValues) {
		Class<?>[] parameterTypes = getParameterTypes(parameterValues);
		
		// YEAH TRY CATCH BLOCKS ARE THE GREATEST
		try {
			// Create and invoke the method -- THIS IS SO COOL THANK YOU MARCOD OF THE SEA
			Method method = className.getMethod(methodName, parameterTypes);
			return method.invoke(invokee, parameterValues);
		}
		// WOW SO MANY THINGS TO CATCH CATCH CATCHEROO
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			Global.error("No such method " + methodName + " in class " + className.getName() + " or could not invoke such method.");
			return null;
		}
	}
	
	// Dynamic instantiation from the class name as a string
	public static Object dynamicInstantiaton(String className, Object... parameterValues) {
		try {
			return dynamicInstantiaton(Class.forName(className), parameterValues);
		}
		catch (ClassNotFoundException e) {
			Global.error("Invalid class name " + className + ". Could not instantiate.");
			return null;
		}
	}
	
	// Returns a new object of type className where the constructor was called with parameterValues as parameters :) :) :)
	public static Object dynamicInstantiaton(Class<?> className, Object... parameterValues) {
		// Get the parameter types
		Class<?>[] parameterTypes = getParameterTypes(parameterValues);
		
		try {
			return className.getConstructor(parameterTypes).newInstance(parameterValues);
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			Global.error("Could not instantiate class " + className + ".");
			return null;
		}
	}
}
