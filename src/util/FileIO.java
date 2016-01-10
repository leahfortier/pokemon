package util;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import javax.imageio.ImageIO;

import main.Global;

public class FileIO {
	
	private static final String FILE_SLASH = File.separator;
	
	public static void deleteFile(String fileName) {
		File file = new File(fileName);
		if (file.exists()) {
			file.delete();	
		}
	}
	
	public static void writeImage(BufferedImage image, File file) {
		try {
			ImageIO.write(image, "png", file);
		}
		catch (IOException e) {
			Global.error("Could not write image to file " + file.getName());
		}
	}
	
	public static BufferedImage readImage(File file) {	
		BufferedImage img = null;
		try {
			img = ImageIO.read(file);
		}
		catch (IOException e) {
			Global.error("Could not open image from following path: " + file.getName());
		}
		
		return img;
	}
	
	public static BufferedImage readImage(String fileName) {
		File file = new File(fileName);
		return readImage(file);
	}
	
	public static String makePath(String... path) {
		StringBuilder sb = new StringBuilder();
		
		for (String folder : path) {
			sb.append(folder + FileIO.FILE_SLASH);
		}
		
		return sb.toString();
	}

	/**
	 * Reads the whole file ignoring commented lines starting with #
	 * 
	 * @param file
	 * @param ignoreComments
	 * @return
	 */
	public static String readEntireFile(File file, boolean ignoreComments) {
		String fileText = readEntireFileWithoutReplacements(file, ignoreComments);
		String restoreSpecial = PokeString.restoreSpecialFromUnicode(fileText);
		
		return restoreSpecial;
	}
	
	public static String readEntireFileWithoutReplacements(File file, boolean ignoreComments) {
		BufferedReader in = openFileBuffered(file);
		StringBuilder build = new StringBuilder();
		String line = null;
		
		try {
			while ((line = in.readLine()) != null) {
				if (line.length() > 0 && (line.charAt(0) != '#' || ignoreComments))
				{
					build.append(line);
					build.append("\n");
				}
			}
		}
		catch (IOException e) {
			Global.error("IO EXCEPTION WHILE READING " + file.getName() + "!!!!");
		}
		
		return build.toString();
	}

	public static BufferedReader openFileBuffered(String file) {
		return openFileBuffered(new File(file));
	}

	public static BufferedReader openFileBuffered(File file) {
		try {
			return new BufferedReader(new FileReader(file));
		}
		catch (FileNotFoundException e) {
			Global.error(file.getName() + " not found!");
			return null;
		}
	}

	public static Scanner openFile(String file) {
		return openFile(new File(file));
	}

	public static Scanner openFile(File file) {
		try {
			return new Scanner(new FileReader(file));
		}
		catch (FileNotFoundException e) {
			Global.error(file.getName() + " not found.");
			return null;
		}
	}
	
	public static void writeToFile(String fileName, StringBuilder out) {
		try {
			PrintStream printStream = new PrintStream(new File(fileName));
			printStream.println(out);
			printStream.close();
		}
		catch (FileNotFoundException ex) {
			Global.error("Cannot print to file " + fileName + ".");
		}
	}
	
	public static PrintStream openOutputFile(String fileName) {
		try {
			return new PrintStream(fileName);
		} 
		catch (FileNotFoundException e) {
			Global.error("Could not open output file " + fileName + ".");
			return null;
		}
	}
}
