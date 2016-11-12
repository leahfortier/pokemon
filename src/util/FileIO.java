package util;

import main.Global;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

public class FileIO {
	private static final String FILE_SLASH = File.separator;
	
	public static void deleteFile(String fileName) {
		File file = new File(fileName);
		if (!file.exists()) {
			Global.error("Could not find file " + file.getName() + " -- unable to delete");
		}

		if (!file.delete()) {
			Global.error("Could not delete file " + file.getName());
		}
	}

	public static void createFile(final String filePath) {
		final File file = new File(filePath);
		if (!file.exists()) {
			try {
				if (!file.createNewFile()) {
                    throw new IOException();
                }
			} catch (IOException e) {
				Global.error("Unable to create file with path " + filePath);
			}
		}
	}

	// Creates a folder with the specified path if it does not already exist
	public static void createFolder(final String folderPath) {
		final File folder = new File(folderPath);
		if (!folder.exists()) {
			if (!folder.mkdir()) {
				Global.error("Unable to create folder with path " + folderPath);
			}
		}
	}

	public static void writeImage(BufferedImage image, File file) {
		try {
			ImageIO.write(image, "png", file);
		}
		catch (IOException exception) {
			Global.error("Could not write image to file " + file.getName());
		}
	}

	public static BufferedImage readImage(String fileName) {
		File file = new File(fileName);
		return readImage(file);
	}
	
	public static BufferedImage readImage(File file) {
		if (!file.exists()) {
			System.out.println("File does not exist");
		}

		BufferedImage image = null;
		try {
			image = ImageIO.read(file);
		}
		catch (IOException exception) {
			Global.error("Could not open image from following path: " + file.getName());
		}
		
		return image;
	}

	public static String makeFolderPath(String... path) {
		String folderPath = StringUtils.empty();
		for (String folder : path) {
			folderPath += folder;

			if (!folderPath.endsWith(FileIO.FILE_SLASH)) {
				folderPath += FileIO.FILE_SLASH;
			}
		}
		
		return folderPath;
	}

	// Reads the whole file ignoring commented lines starting with # when ignoreComments is true
	public static String readEntireFileWithReplacements(String fileName, boolean ignoreComments) {
		return readEntireFileWithReplacements(new File(fileName), ignoreComments);
	}

	// Reads the whole file ignoring commented lines starting with # when ignoreComments is true
	public static String readEntireFileWithReplacements(File file, boolean ignoreComments) {
		String fileText = readEntireFileWithoutReplacements(file, ignoreComments);
		return PokeString.restoreSpecialFromUnicode(fileText);
	}

	public static String readEntireFileWithoutReplacements(final String fileName, final boolean ignoreComments) {
		return readEntireFileWithoutReplacements(new File(fileName), ignoreComments);
	}

	// TODO: I think there might be a bug in this that is eliminating white space or new lines or something
	public static String readEntireFileWithoutReplacements(File file, boolean ignoreComments) {
		BufferedReader in = openFileBuffered(file);
		if (in == null) {
			Global.error("Could not open file " + file.getName());
			return null;
		}

		StringBuilder build = new StringBuilder();
		String line;
		
		try {
			while ((line = in.readLine()) != null) {
				if (line.length() > 0 && (line.charAt(0) != '#' || ignoreComments)) {
					build.append(line)
							.append("\n");
				}
			}
		}
		catch (IOException exception) {
			Global.error("IO EXCEPTION WHILE READING " + file.getName() + "!!!!");
		}
		
		return build.toString();
	}

	public static String readEntireFile(String fileName) {
		final Scanner in = openFile(fileName);
		final StringBuilder out = new StringBuilder();

		while (in.hasNextLine()) {
			out.append(in.nextLine())
					.append("\n");
		}

		return out.toString();
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

	public static Scanner openFile(String fileName) {
		return openFile(new File(fileName));
	}

	public static Scanner openFile(File file) {
		try {
			return new Scanner(new FileReader(file));
		}
		catch (FileNotFoundException e) {
			Global.error(file.getName() + " not found.");
			return new Scanner(StringUtils.empty());
		}
	}

	// Overwrites the given file name with the content of out only if there is a difference
	public static boolean overwriteFile(final String fileName, final StringBuilder out) {
		final String previousFile = readEntireFile(fileName);
		final String newFile = out.toString();

		if (StringUtils.isNullOrEmpty(previousFile) ||
				!newFile.equals(previousFile.substring(0, previousFile.length() - 1))) {
			writeToFile(fileName, out);
			System.out.println(fileName + " overwritten.");
			return true;
		}

		return false;
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

	public static JFileChooser getImageFileChooser(final String folderPath) {
		final File folder = new File(folderPath);
		JFileChooser fileChooser = new JFileChooser(folder);
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setMultiSelectionEnabled(true);

		fileChooser.setFileFilter(new FileFilter() {
			public boolean accept(File file) {
				return file.getName().toLowerCase().endsWith("png");
			}

			public String getDescription() {
				return "PNG";
			}
		});

		return fileChooser;
	}

	public static JFileChooser getDirectoryChooser() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		return fileChooser;
	}
}
