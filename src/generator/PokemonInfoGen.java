package generator;

import draw.ImageUtils;
import pokemon.PokemonInfo;
import pokemon.PokemonNamesies;
import pokemon.evolution.EvolutionType;
import util.FileIO;
import util.FileIO.NullOutputStream;
import util.FileName;
import util.Folder;
import util.GeneralUtils;
import util.StringUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// For when pokemoninfo.txt needs to be edited
class PokemonInfoGen {
    public static void main(String[] args) {
        new PokemonInfoGen();

        System.out.println("GEN GEN GEN");
    }

    private PokemonInfoGen() {
        newPokemonInfoCompare();
//        pokemonInfoStuff();
//        updateNum();
//        resizePokedexImages();
    }

    private static void resizePokedexImages() {
        final int maxWidth = 140;
        final int maxHeight = 190;

        for (int num = 1; num <= PokemonInfo.NUM_POKEMON; num++) {
            File imageFile = FileIO.getImageFile(num, "", Folder.POKEDEX_TILES);
            BufferedImage image = FileIO.readImage(imageFile);
            int width = image.getWidth();
            int height = image.getHeight();

            boolean rescaled = false;
            if (width > maxWidth) {
                image = ImageUtils.scaleImageByWidth(image, maxWidth);
                rescaled = true;
            }
            if (height > maxHeight) {
                image = ImageUtils.scaleImageByHeight(image, maxHeight);
                rescaled = true;
            }

            if (rescaled) {
                System.out.println("Rescaling image " + imageFile.getPath() + " from " + getCoordinatesString(width, height) + " to " + getCoordinatesString(image));
                FileIO.writeImage(image, imageFile);
            }
        }
    }

    private static String getCoordinatesString(BufferedImage image) {
        return getCoordinatesString(image.getWidth(), image.getHeight());
    }

    private static String getCoordinatesString(int width, int height) {
        return "(" + width + ", " + height + ")";
    }

    private static void updateImageNum(int num, int newNum, String imageSuffix, String folderPath) {
        File imageFile = FileIO.getImageFile(num, imageSuffix, folderPath);
        if (imageFile.exists()) {
            BufferedImage image = FileIO.readImage(imageFile);
            FileIO.writeImage(image, FileIO.getImageFile(newNum, imageSuffix, folderPath));
            FileIO.deleteFile(imageFile);
        }
    }

    // Used if new Pokemon need to be added -- need to update the numbers of all of the added Pokemon
    // BE VERY CAREFUL TO ONLY RUN THIS ONCE OR IT WILL FUCK UP ALL THE IMAGES
    private static void updateNum() {
        int startNum = PokemonInfo.getPokemonInfo(PokemonNamesies.RIZARDON).getNumber();
        int newStartNum = 808;

        for (int num = PokemonInfo.NUM_POKEMON; num >= startNum; num--) {
            int newNum = num + (newStartNum - startNum);
            updateImageNum(num, newNum, "", Folder.POKEMON_TILES);
            updateImageNum(num, newNum, "-back", Folder.POKEMON_TILES);
            updateImageNum(num, newNum, "-shiny", Folder.POKEMON_TILES);
            updateImageNum(num, newNum, "-shiny-back", Folder.POKEMON_TILES);
            updateImageNum(num, newNum, "-small", Folder.PARTY_TILES);
            updateImageNum(num, newNum, "", Folder.POKEDEX_TILES);
        }

        Scanner in = FileIO.openFile(FileName.POKEMON_INFO);
        PrintStream out = FileIO.openOutputFile("out.txt");

        for (int i = 1; i < startNum; i++) {
            outputSinglePokemon(in, out);
        }

        while (in.hasNext()) {
            int num = in.nextInt(); in.nextLine();
            int newNum = num + (newStartNum - startNum);
            out.println(newNum); // Num
            out.println(in.nextLine()); // Name
            out.println(in.nextLine()); // Base Stats
            out.println(in.nextLine()); // Base Exp
            out.println(in.nextLine()); // Growth Rate
            out.println(in.nextLine()); // Types
            out.println(in.nextLine()); // Catch Rate
            out.println(in.nextLine()); // EVs
            readEvolution(in, out);     // Evolution
            readHoldItems(in, out);     // Wild Items
            out.println(in.nextLine()); // Male Ratio
            out.println(in.nextLine()); // Abilities
            out.println(in.nextLine()); // Classification
            out.println(in.nextLine()); // Height Weight FlavorText
            out.println(in.nextLine()); // Egg Steps
            out.println(in.nextLine()); // Egg Groups
            readMoves(in, out);    		// Level Up Moves
            readMoves(in, out);			// Learnable Moves
            out.println(in.nextLine()); // New Line
        }

        Scanner in2 = FileIO.openFile("addedpokes.txt");
        StringBuilder out2 = new StringBuilder();
        int num = startNum;
        while (in2.hasNext()) {
            String line = in2.nextLine();
            if (line.startsWith(num + ".")) {
                int newNum = num + (newStartNum - startNum);
                line = line.replaceFirst(num + ".", newNum + ".");
                num++;
            }
            StringUtils.appendLine(out2, line);
        }
        FileIO.overwriteFile("addedpokes.txt", out2);
    }

    private static List<String> getMoves(Scanner in) {
        int numMoves = in.nextInt(); in.nextLine();

        List<String> moves = new ArrayList<>();
        for (int i = 0; i < numMoves; i++) {
            moves.add(in.nextLine());
        }

        return moves;
    }

    private static void addMovesDiff(List<String> moves, String diffName, StringBuilder diffs) {
        if (!moves.isEmpty()) {
            diffs.append(diffName).append(":\n\t").append(String.join("\n\t", moves)).append("\n");
        }
    }

    private static void movesDiff(Scanner in1, Scanner in2, String diffName, StringBuilder diffs) {
        List<String> moves1 = getMoves(in1);
        List<String> moves2 = getMoves(in2);

        List<String> removedMoves = GeneralUtils.inFirstNotSecond(moves1, moves2);
        List<String> addedMoves = GeneralUtils.inFirstNotSecond(moves2, moves1);

        addMovesDiff(removedMoves, diffName + " Removed Moves", diffs);
        addMovesDiff(addedMoves, diffName + " Added Moves", diffs);
    }

    private static void diff(Scanner in1, Scanner in2, String diffName, StringBuilder diffs) {
        diff(in1.nextLine(), in2.nextLine(), diffName, diffs);
    }

    // If the two lines are different, appends "diffName: line1 -> line2" to the diffs builder
    private static void diff(String line1, String line2, String diffName, StringBuilder diffs) {
        if (!line1.equals(line2) && !ignoreDiff(line1, line2, diffName)) {
            diffs.append(diffName).append(":\n\t").append(line1).append(" -> ").append(line2).append("\n");
        }
    }

    private static boolean ignoreDiff(String line1, String line2, String diffName) {
        switch (diffName) {
            case "Male Ratio":
                switch (line1) {
                    case "87":
                        return line2.equals("88");
                    case "25":
                        return line2.equals("24");
                    case "75":
                        return line2.equals("76");
                    case "13":
                        return line2.equals("12");
                }
        }

        return false;
    }

    // TODO: WHY IS 'new Scanner(FileIO.readEntireFileWithReplacements("temp.txt", false));' DIFFERENT THAN 'FileIO.openFile("temp.txt");'
    // Compares the pokemon info to info in a new file and outputs the differences
    // Ignores evolution, wild hold items, and flavor text
    private static void newPokemonInfoCompare() {
        Scanner in1 = FileIO.openFile(FileName.POKEMON_INFO);
        Scanner in2 = new Scanner(FileIO.readEntireFileWithReplacements("temp.txt", false));

        PrintStream out = FileIO.openOutputFile("temp2.txt");
        PrintStream nullOut = new PrintStream(new NullOutputStream());

        for (int i = 1; i < PokemonNamesies.BULBASAUR.ordinal(); i++) {
            outputSinglePokemon(in1, nullOut);
        }

        while (in2.hasNext()) {
            StringBuilder diffs = new StringBuilder();

            int num = in1.nextInt(); in1.nextLine();
            diff(num + "", in2.nextLine(), "Num", diffs);

            String name = in1.nextLine();
            diff(name, in2.nextLine(), "Name", diffs);

            diff(in1, in2, "Stats", diffs);
            diff(in1, in2, "Base EXP", diffs);
            diff(in1, in2, "Growth Rate", diffs);
            diff(in1, in2, "Types", diffs);
            diff(in1, in2, "Catch Rate", diffs);
            diff(in1, in2, "EVs", diffs);
            readEvolution(in1); readEvolution(in2); // Don't compare these
            readHoldItems(in1); readHoldItems(in2); // Don't compare these either
            diff(in1, in2, "Male Ratio", diffs);
            diff(in1, in2, "Abilities", diffs);
            diff(in1, in2, "Classification", diffs);
            diff(in1.nextInt() + "", in2.nextInt() + "", "Height", diffs);
            diff(in1.nextDouble() + "", in2.nextDouble() + "", "Weight", diffs);
            in1.nextLine(); in2.nextLine(); // Flavor Text -- don't compare
            diff(in1, in2, "Egg Steps", diffs);
            diff(in1, in2, "Egg Groups", diffs);
            movesDiff(in1, in2, "Level Up", diffs);
            movesDiff(in1, in2, "Learnable", diffs);

            if (diffs.length() > 0) {
                out.printf("%03d %s:\n\t%s\n", num, name, diffs.toString().replace("\n", "\n\t"));
            }
        }
    }

    private static void outputSinglePokemon(Scanner in, PrintStream out) {
        out.println(in.nextLine()); // Num
        out.println(in.nextLine()); // Name
        out.println(in.nextLine()); // Base Stats
        out.println(in.nextLine()); // Base Exp
        out.println(in.nextLine()); // Growth Rate
        out.println(in.nextLine()); // Types
        out.println(in.nextLine()); // Catch Rate
        out.println(in.nextLine()); // EVs
        readEvolution(in, out);     // Evolution
        readHoldItems(in, out);     // Wild Items
        out.println(in.nextLine()); // Male Ratio
        out.println(in.nextLine()); // Abilities
        out.println(in.nextLine()); // Classification
        out.println(in.nextLine()); // Height Weight FlavorText
        out.println(in.nextLine()); // Egg Steps
        out.println(in.nextLine()); // Egg Groups
        readMoves(in, out);    		// Level Up Moves
        readMoves(in, out);			// Learnable Moves
        out.println(in.nextLine()); // New Line
    }

    // Used for editing pokemoninfo.txt
    private static void pokemonInfoStuff() {
        Scanner in = FileIO.openFile(FileName.POKEMON_INFO);
        PrintStream out = FileIO.openOutputFile("out.txt");

        while (in.hasNext()) {
            outputSinglePokemon(in, out);
        }
    }

    private static void readMoves(Scanner in, PrintStream out) {
        int numMoves = in.nextInt(); in.nextLine();
        out.println(numMoves); // Number of Moves

        for (int i = 0; i < numMoves; i++) {
            out.println(in.nextLine()); // Each move
        }
    }

    private static void readEvolution(Scanner in, PrintStream out) {
        out.print(readEvolution(in));
    }

    private static String readEvolution(Scanner in) {
        String type = in.next();
        if (type.equals(EvolutionType.MULTI.name())) {
            int x = in.nextInt(); in.nextLine();
            StringBuilder evolution = new StringBuilder(type + " " + x + "\n");
            for (int i = 0; i < x; i++) {
                evolution.append(readEvolution(in));
            }
            return evolution.toString();
        } else {
            return type + in.nextLine() + "\n";
        }
    }

    private static void readHoldItems(Scanner in, PrintStream out) {
        out.print(readHoldItems(in));
    }

    private static String readHoldItems(Scanner in) {
        int num = in.nextInt(); in.nextLine();

        StringBuilder holdItems = new StringBuilder(num + "\n");
        for (int i = 0; i < num; i++) {
            holdItems.append(in.nextLine()).append("\n");
        }

        return holdItems.toString();
    }
}
