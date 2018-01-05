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
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class FileIO {
	public static final String FILE_SLASH = File.separator;

	public static void deleteFile(String fileName) {
		deleteFile(new File(fileName));
	}

	public static void deleteFile(File file) {
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

	public static void writeImage(BufferedImage image, String fileName) {
		if (!fileName.endsWith(".png")) {
			fileName += ".png";
		}

		writeImage(image, new File(fileName));
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
		if (!fileName.endsWith(".png")) {
			fileName += ".png";
		}

		File file = new File(fileName);
		return readImage(file);
	}

	public static BufferedImage readImage(File file) {
		if (!file.exists()) {
			Global.error("File does not exist: " + file.getPath());
		}

		BufferedImage image = null;
		try {
			image = ImageIO.read(file);
		}
		catch (IOException exception) {
			Global.error("Could not open image from following path: " + file.getAbsolutePath());
		}

		return image;
	}

	public static File getImageFile(int num, String suffix, String folderPath) {
		return new File(folderPath + String.format("%03d", num) + suffix + ".png");
	}

	public static String makeFolderPath(String... path) {
		StringAppender folderPath = new StringAppender();
		for (String folder : path) {
			folderPath.append(folder);

			if (!folderPath.toString().endsWith(FileIO.FILE_SLASH)) {
				folderPath.append(FileIO.FILE_SLASH);
			}
		}

		return folderPath.toString();
	}

	// Reads the whole file ignoring commented lines starting with # when ignoreComments is true
	public static String readEntireFileWithReplacements(String fileName, boolean ignoreComments) {
		return readEntireFileWithReplacements(new File(fileName), ignoreComments);
	}

	// Reads the whole file ignoring commented lines starting with # when ignoreComments is true
	public static String readEntireFileWithReplacements(File file, boolean ignoreComments) {
		String fileText = readEntireFileWithoutReplacements(file, ignoreComments);
		return SpecialCharacter.restoreSpecialFromUnicode(fileText);
	}

	public static String readEntireFileWithoutReplacements(final String fileName, final boolean ignoreComments) {
		return readEntireFileWithoutReplacements(new File(fileName), ignoreComments);
	}

	// TODO: I think there might be a bug in this that is eliminating white space or new lines or something -- probably because of the line.length() > 0
	public static String readEntireFileWithoutReplacements(File file, boolean ignoreComments) {
		BufferedReader in = openFileBuffered(file);
		if (in == null) {
			Global.error("Could not open file " + file.getName());
			return null;
		}

		StringAppender build = new StringAppender();
		try {
			String line;
			while ((line = in.readLine()) != null) {
				if (line.length() > 0 && (line.charAt(0) != '#' || ignoreComments)) {
					build.appendLine(line);
				}
			}
		}
		catch (IOException exception) {
			Global.error("IO EXCEPTION WHILE READING " + file.getName() + "!!!!");
		}

		return build.toString();
	}

	public static boolean fileEquals(String firstFileName, String secondFileName) {
		return readEntireFile(firstFileName).equals(readEntireFile(secondFileName));
	}

    public static String readEntireFile(String fileName) {
	    return readEntireFile(new File(fileName));
    }

	public static String readEntireFile(File file) {
		final Scanner in = openFile(file);
		final StringAppender out = new StringAppender();

		while (in.hasNextLine()) {
			out.appendLine(in.nextLine());
		}

		return out.toString();
	}

	public static BufferedReader openFileBuffered(File file) {
		try {
			return new BufferedReader(new FileReader(file));
		}
		catch (FileNotFoundException e) {
			Global.error(file.getAbsolutePath() + " not found!");
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

    public static boolean overwriteFile(final String fileName, String newFile) {
	    return overwriteFile(new File(fileName), newFile);
    }

	// Overwrites the given file name with the content of out only if there is a difference
	public static boolean overwriteFile(final File file, String newFile) {
	    // Replace tabs with 4 spaces
	    newFile = newFile.replaceAll("\t", StringUtils.repeat(" ", 4));

		final String previousFile = readEntireFile(file);
		if (StringUtils.isNullOrEmpty(previousFile) ||
				!newFile.equals(previousFile.substring(0, previousFile.length() - 1))) {
			writeToFile(file, newFile);
			System.out.println(file.getPath() + " overwritten.");
			return true;
		}

		return false;
	}

    public static void writeToFile(String fileName, String out) {
        writeToFile(new File(fileName), out);
    }

	public static void writeToFile(File file, String out) {
		try {
			PrintStream printStream = new PrintStream(file);
			printStream.println(out);
			printStream.close();
		}
		catch (FileNotFoundException ex) {
			Global.error("Cannot print to file " + file.getPath() + ".");
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

	public static Iterable<File> listSubdirectories(File parentDirectory) {
		return listDirectories(parentDirectory).stream()
				.map(FileIO::listDirectories)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	public static List<File> listDirectories(File parentDirectory) {
		try {
			return Files.walk(Paths.get(parentDirectory.getAbsolutePath()), 1)
					.filter(Files::isDirectory)
					.map(Path::toFile)
					.filter(directory -> !directory.getAbsolutePath().equals(parentDirectory.getAbsolutePath()))
					.collect(Collectors.toList());
		} catch (IOException e) {
			Global.error("IOException trying to list directories of " + parentDirectory.getAbsolutePath());
			return new ArrayList<>();
		}
	}

	public static List<File> listFiles(String folderName) {
        try {
            return Files.walk(Paths.get(folderName), Integer.MAX_VALUE)
                        .filter(path -> Files.isRegularFile(path))
                        .map(Path::toFile)
                        .collect(Collectors.toList());
        } catch (IOException e) {
            Global.error("IOException trying to list files of " + folderName);
            return new ArrayList<>();
        }
    }

	public static class NullOutputStream extends OutputStream {
		@Override
		public void write(int b) throws IOException {}
	}
}
