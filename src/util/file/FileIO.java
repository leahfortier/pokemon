package util.file;

import draw.ImageUtils;
import main.Global;
import util.string.SpecialCharacter;
import util.string.StringAppender;
import util.string.StringUtils;

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
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public final class FileIO {
    public static String PATH = "";

    // Utility class -- should not be instantiated
    private FileIO() {
        Global.error(this.getClass().getSimpleName() + " class cannot be instantiated.");
    }

    public static File newFile(String fileName) {
        if (!fileName.startsWith("/")) {
            fileName = PATH + fileName;
        }

        return new File(fileName);
    }

    public static void deleteFile(String fileName) {
        deleteFile(FileIO.newFile(fileName));
    }

    public static void deleteFile(File file) {
        if (!file.exists()) {
            Global.error("Could not find file " + file.getName() + " -- unable to delete");
        }

        if (!file.delete()) {
            Global.error("Could not delete file " + file.getName());
        }
    }

    // Creates a new file if file does not already exist
    public static void createFile(final String filePath) {
        final File file = FileIO.newFile(filePath);
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
        final File folder = FileIO.newFile(folderPath);
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

        writeImage(image, FileIO.newFile(fileName));
    }

    public static void writeImage(BufferedImage image, File file) {
        try {
            ImageIO.write(image, "png", file);
        } catch (IOException exception) {
            Global.error("Could not write image to file " + file.getName());
        }
    }

    public static BufferedImage readImage(String fileName) {
        if (!fileName.endsWith(".png")) {
            fileName += ".png";
        }

        File file = FileIO.newFile(fileName);
        return readImage(file);
    }

    public static BufferedImage readImage(File file) {
        if (!file.exists()) {
            Global.error("File does not exist: " + file.getAbsolutePath());
        }

        BufferedImage image = null;
        try {
            image = ImageUtils.read(file, 1f);
        } catch (IOException exception) {
            Global.error("Could not open image from following path: " + file.getAbsolutePath());
        }

        return image;
    }

    public static File getImageFile(int num, String suffix, String folderPath) {
        return getImageFile(num, false, suffix, folderPath);
    }

    public static File getImageFile(int num, boolean form, String suffix, String folderPath) {
        return FileIO.newFile(folderPath + String.format("%03d", num) + (form ? "b" : "") + suffix + ".png");
    }

    public static String makeFolderPath(String... path) {
        StringAppender folderPath = new StringAppender();
        for (String folder : path) {
            folderPath.append(folder);

            if (!folderPath.toString().endsWith(File.separator)) {
                folderPath.append(File.separator);
            }
        }

        return folderPath.toString();
    }

    public static boolean fileEquals(String firstFileName, String secondFileName) {
        return readEntireFile(firstFileName).equals(readEntireFile(secondFileName));
    }

    // Reads the whole file replacing special characters
    public static String readEntireFileWithReplacements(String fileName) {
        String fileText = readEntireFile(fileName);
        return SpecialCharacter.restoreSpecialFromUnicode(fileText);
    }

    // Returns the entire file as a string
    public static String readEntireFile(String fileName) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(fileName));
            return new StringAppender().appendJoin("\n", lines).toString();
        } catch (IOException e) {
            Global.error("IOException while reading " + fileName + ": " + e.getMessage());
            return "";
        }
    }

    public static BufferedReader openFileBuffered(File file) {
        try {
            return new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            Global.error(file.getAbsolutePath() + " not found!");
            return new BufferedReader(new StringReader(""));
        }
    }

    public static Scanner openFile(String fileName) {
        return openFile(FileIO.newFile(fileName));
    }

    public static Scanner openFile(File file) {
        try {
            return new Scanner(new FileReader(file));
        } catch (FileNotFoundException e) {
            Global.error(file.getName() + " not found.");
            return new Scanner("");
        }
    }

    // Returns null if the file does not need to be overwritten and the new contents if it does
    // Replaces tabs with 4 spaces and trims for new contents
    public static String getOverwriteContents(String overwriteFileName, String newFileContents) {
        newFileContents = newFileContents.replaceAll("\t", StringUtils.repeat(" ", 4)).trim();

        final String previousFile = readEntireFile(overwriteFileName);
        if (StringUtils.isNullOrEmpty(previousFile) || !newFileContents.equals(previousFile)) {
            return newFileContents;
        }

        return null;
    }

    // Overwrites the given file name with the content of out only if there is a difference
    public static boolean overwriteFile(String fileName, String newFileContents) {
        // Replace tabs with 4 spaces and trim
        newFileContents = getOverwriteContents(fileName, newFileContents);

        if (!StringUtils.isNullOrEmpty(newFileContents)) {
            writeToFile(fileName, newFileContents);
            System.out.println(fileName + " overwritten.");
            return true;
        }

        return false;
    }

    private static void writeToFile(String fileName, String out) {
        try {
            PrintStream printStream = new PrintStream(FileIO.newFile(fileName));
            printStream.println(out);
            printStream.close();
        } catch (FileNotFoundException ex) {
            Global.error("Cannot print to file " + fileName + ".");
        }
    }

    public static PrintStream openOutputFile(String fileName) {
        try {
            return new PrintStream(fileName);
        } catch (FileNotFoundException e) {
            Global.error("Could not open output file " + fileName + ".");
            return new PrintStream(new NullOutputStream()); // Just so I don't get NPE warnings since above exits
        }
    }

    public static JFileChooser getImageFileChooser(String folderPath) {
        final File folder = FileIO.newFile(folderPath);
        JFileChooser fileChooser = new JFileChooser(folder);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setMultiSelectionEnabled(true);

        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().toLowerCase().endsWith("png");
            }

            @Override
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
        return listDirectories(parentDirectory)
                .stream()
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
        public void write(int b) {}
    }
}
