package generator.update;

import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.MoveType;
import draw.ImageUtils;
import map.condition.Condition;
import map.condition.ConditionHolder.AndCondition;
import map.condition.ConditionSet;
import pattern.map.ConditionMatcher;
import pokemon.evolution.EvolutionType;
import pokemon.species.PokemonInfo;
import pokemon.species.PokemonList;
import pokemon.species.PokemonNamesies;
import pokemon.stat.Stat;
import util.GeneralUtils;
import util.file.FileIO;
import util.file.FileIO.NullOutputStream;
import util.file.FileName;
import util.file.Folder;
import util.string.StringAppender;
import util.string.StringUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Mostly for when pokemoninfo.txt needs to be edited
public class UpdateGen {
    // Moves that aren't in the game for various reasons (double battle only, event only, not level up learnable, etc.)
    // This list does not include any Z-moves
    public static final Set<String> unimplementedMoves = Set.of(
            "After You",
            "Ally Switch",
            "Celebrate",
            "Follow Me",
            "Frustration",
            "Happy Hour",
            "Helping Hand",
            "Hold Back",
            "Hold Hands",
            "Ion Deluge",
            "Instruct",
            "Quash",
            "Rage Powder",
            "Return",
            "Spotlight",
            "Thousand Arrows",
            "Thousand Waves",
            "Wide Guard"
    );

    public static void main(String[] args) {
        new UpdateGen();

        System.out.println("GEN GEN GEN");
    }

    private UpdateGen() {
//        GeneratorUpdater.updateAll();
//        newPokemonInfoCompare();
//        pokemonInfoStuff();
//        pokemonInfoStuff2();
//        updateNum();
//        resizeImages();
//        trimImages();
//        translateAlBhed();
//        addCondition();
//        outputShowdownImagesFile();
//        testBulbapediaMoveTypeList();
//        printStatOrder();
//        sourceCodeRegexReplace();
    }

    // Updates all the files in the source code and generator input files by replacing the regex
    // Update the replaceLine method below to change the regex and replacement
    private static void sourceCodeRegexReplace() {
        for (File file : GeneralUtils.combine(FileIO.listFiles(Folder.GENERATOR), FileIO.listFiles(Folder.SRC))) {
            if (file.getName().contains(UpdateGen.class.getSimpleName())) {
                continue;
            }

            singleFileRegexReplace(file.getPath());
        }
    }

    private static void singleFileRegexReplace(String fileName) {
        Scanner original = FileIO.openFile(fileName);
        StringAppender out = new StringAppender();

        while (original.hasNext()) {
            String newLine = replaceLine(original.nextLine());
            out.append(newLine + (original.hasNext() ? "\n" : ""));
        }

        original.close();
        FileIO.overwriteFile(fileName, out.toString());
    }

    // Change the regex and replacement string to reflect what you want to change everywhere
    // Current example:
    //      Regex In: <NamesiesType>Namesies.<EFFECT_NAME>.getEffect().cast(<parameters>)
    //      Regex Out: Effect.cast(<NamesiesType>Namesies.<EFFECT_NAME>, <parameters>)
    //      Ex Input: PokemonEffectNamesies.DISABLE.getEffect().cast(b, victim, user, CastSource.ABILITY, false)
    //      Ex Output: Effect.cast(PokemonEffectNamesies.DISABLE, b, victim, user, CastSource.ABILITY, false)
    private static String replaceLine(String line) {
        Pattern regex = Pattern.compile(
                "([A-Za-z]+)Namesies." + // 1 (namesies type)
                        "([A-Z_]+).getEffect\\(\\)" + // 2 (namesies value)
                        ".cast\\(" +
                        "((?:b|battle), [A-Za-z]+, [A-Za-z]+, " + // start 3 (apply params)
                        "(?:source|CastSource\\.[A-Z_]+), " +
                        "(?:true|false|super\\.printCast))" + // end 3
                        "\\)"
        );

        while (true) {
            Matcher matcher = regex.matcher(line);
            if (!matcher.find()) {
                break;
            }

            String replacement = "Effect.cast(" +
                    matcher.group(1) + "Namesies." +
                    matcher.group(2) + ", " +
                    matcher.group(3) + ")";
            line = matcher.replaceFirst(replacement);
        }

        return line;
    }

    // Basically I'm sick of writing this -- just prints Pokemon ordered by base stat
    private static void printStatOrder() {
        // Feel free to change the stat or give a filter
        Stat toOrder = Stat.SP_ATTACK;
        Predicate<PokemonInfo> filter = p -> true;

        PokemonList.instance()
                   .stream()
                   .filter(filter)
                   .sorted(Comparator.comparingInt(p -> p.getStats().get(toOrder)))
                   .forEachOrdered(p -> System.out.println(p.getName() + " " + p.getStats().get(toOrder)));
    }

    private static void testBulbapediaMoveTypeList() {
        // Moves to ignore including those that are not implemented
        // Can be added to as necessary
        Set<String> exclude = new HashSet<>(unimplementedMoves);

        // This file should just contain a copy and pasted list of moves from those drop down thingies
        Scanner in = FileIO.openFile("temp.txt");

        // Those usually have extra crap in them so just change this to be the number of args to ignore
        // Note: This doesn't work if any of these args have whitespace in them (Category, Type, etc. is more what we're looking for)
        int numIgnorableArgs = 2;

        // Change this to match whatever it is you are testing
        Predicate<Attack> inList = attack -> attack.isMoveType(MoveType.METRONOMELESS);

        List<String> expected = new ArrayList<>();
        while (in.hasNext()) {
            String line = in.nextLine();
            String[] split = line.trim().split("\\s+");

            String attackName = new StringAppender()
                    .appendJoin(" ", split.length - numIgnorableArgs, i -> split[i])
                    .toString();
            if (!exclude.contains(attackName)) {
                expected.add(attackName);
            }
        }

        List<String> actual = new ArrayList<>();
        for (AttackNamesies attackNamesies : AttackNamesies.values()) {
            Attack attack = attackNamesies.getNewAttack();
            if (inList.test(attack)) {
                actual.add(attack.getName());
            }
        }

        System.out.println("Actual but not expected:");
        System.out.println(GeneralUtils.inFirstNotSecond(actual, expected));
        System.out.println("Expected but not actual:");
        System.out.println(GeneralUtils.inFirstNotSecond(expected, actual));
    }

    private static void outputShowdownImagesFile() {
        StringAppender out = new StringAppender();

        for (PokemonInfo pokemonInfo : PokemonList.instance()) {
            List<String> forms = new ArrayList<>();
            if (pokemonInfo.getNumber() < PokemonNamesies.RIZARDON.getInfo().getNumber()) {
                forms.add("");
            }

            switch (pokemonInfo.namesies()) {
                case AEGISLASH:
                    forms.add("-blade");
                    break;
                case WISHIWASHI:
                    forms.add("-school");
                    break;
                case MIMIKYU:
                    forms.add("-busted");
                    break;
                case MINIOR:
                    forms.add("-meteor");
                    break;
                case RIZARDON:
                    forms.add("charizard-megax");
                    break;
                case KUCHIITO:
                    forms.add("mawile-mega");
                    break;
                case ASBEL:
                    forms.add("absol-mega");
                    break;
                case YAMIRAMI:
                    forms.add("sableye-mega");
                    break;
                case SILPH_SURFER:
                    forms.add("raichu-alola");
                    break;
                case SNOWSHREW:
                    forms.add("sandshrew-alola");
                    break;
                case SNOWSLASH:
                    forms.add("sandslash-alola");
                    break;
                case YUKIKON:
                    forms.add("vulpix-alola");
                    break;
                case KYUKON:
                    forms.add("ninetales-alola");
                    break;
                case SLEIMA:
                    forms.add("grimer-alola");
                    break;
                case SLEIMOK:
                    forms.add("muk-alola");
                    break;
                case KOKONATSU:
                    forms.add("exeggutor-alola");
                    break;
                case GARA_GARA:
                    forms.add("marowak-alola");
                    break;
                case JUPETTA:
                    forms.add("banette-mega");
                    break;
                case LOUGAROC:
                    forms.add("lycanroc-midnight");
                    break;
                case LUGARUGAN:
                    forms.add("lycanroc-dusk");
                    break;
            }

            int num = pokemonInfo.getNumber();
            for (String form : forms) {
                appendNotExists(out, num, form, "");
                appendNotExists(out, num, form, "-back");
                appendNotExists(out, num, form, "-shiny");
                appendNotExists(out, num, form, "-shiny-back");
            }
        }

        FileIO.overwriteFile(Folder.SCRIPTS_COMPARE + "ps-images.in", out.toString());
    }

    private static void appendNotExists(StringAppender out, int num, String form, String suffix) {
        boolean isAddedPoke = num >= PokemonNamesies.RIZARDON.getInfo().getNumber();
        File imageFile = FileIO.getImageFile(num, !StringUtils.isNullOrEmpty(form) && !isAddedPoke, suffix, Folder.POKEMON_TILES);

        String sourcePath = "bw";
        if (suffix.equals("-shiny-back")) {
            sourcePath += "-back-shiny";
        } else {
            sourcePath += suffix;
        }
        sourcePath += "/";
        if (!isAddedPoke) {
            sourcePath += PokemonList.get(num).getName().toLowerCase().replaceAll("[:'.\\- ]", "");
        }
        sourcePath += form + ".png";

        out.appendLine(sourcePath + " " + imageFile.getName());
    }

    private static void trimImages() {
        String inputLocation = "../Downloads/sunmoonsprites";
        String outputLocation = Folder.POKEMON_TILES;

        for (File imageFile : FileIO.listFiles(inputLocation)) {
            if (imageFile.isDirectory() || imageFile.isHidden()) {
                continue;
            }

            BufferedImage image = FileIO.readImage(imageFile);
            BufferedImage trimmed = ImageUtils.trimImage(image);

            File file = FileIO.newFile(outputLocation + imageFile.getName());
            FileIO.writeImage(trimmed, file);

            System.out.println("Writing trimmed image to " + file.getPath());
        }
    }

    private static void resizeImages() {
        for (int num = 1; num <= PokemonInfo.NUM_POKEMON; num++) {
            resizeImage(num, "", Folder.POKEDEX_TILES, 140, 190);
            resizeImage(num, "-small", Folder.PARTY_TILES, 32, 32);
            resizeImage(num, "", Folder.POKEMON_TILES, 96, 96);
            resizeImage(num, "-back", Folder.POKEMON_TILES, 96, 96);
        }
    }

    private static void resizeImage(int num, String suffix, String folderPath, int maxWidth, int maxHeight) {
        File imageFile = FileIO.getImageFile(num, suffix, folderPath);
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

    public static String getCoordinatesString(BufferedImage image) {
        return getCoordinatesString(image.getWidth(), image.getHeight());
    }

    public static String getCoordinatesString(int width, int height) {
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
        int startNum = PokemonNamesies.RIZARDON.getInfo().getNumber();
        int newStartNum = 810;

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
            out.println(newNum);        // Num
            out.println(in.nextLine()); // Name
            out.println(in.nextLine()); // Base Stats
            out.println(in.nextLine()); // Base Exp
            out.println(in.nextLine()); // Growth Rate
            out.println(in.nextLine()); // Types
            out.println(in.nextLine()); // Catch Rate
            out.println(in.nextLine()); // EVs
            readEvolution(in, out);     // Evolution
            readHoldItems(in, out);     // Wild Items
            out.println(in.nextLine()); // Female Ratio
            out.println(in.nextLine()); // Abilities
            out.println(in.nextLine()); // Classification
            out.println(in.nextLine()); // Height Weight FlavorText
            out.println(in.nextLine()); // Egg Steps
            out.println(in.nextLine()); // Egg Groups
            readMoves(in, out);         // Level Up Moves
            readMoves(in, out);         // Learnable Moves
            out.println(in.nextLine()); // New Line
        }

        String fileName = Folder.NOTES + "addedpokes.txt";
        Scanner in2 = FileIO.openFile(fileName);
        StringAppender out2 = new StringAppender();
        int num = startNum;
        while (in2.hasNext()) {
            String line = in2.nextLine();
            if (line.startsWith(num + ".")) {
                int newNum = num + (newStartNum - startNum);
                line = line.replaceFirst(num + ".", newNum + ".");
                num++;
            }
            out2.appendLine(line);
        }
        FileIO.overwriteFile(fileName, out2.toString());
    }

    private static List<String> getMoves(Scanner in) {
        int numMoves = in.nextInt(); in.nextLine();

        List<String> moves = new ArrayList<>();
        for (int i = 0; i < numMoves; i++) {
            moves.add(in.nextLine());
        }

        return moves;
    }

    private static void movesDiff(Scanner in1, Scanner in2, String diffName, StringAppender diffs) {
        listDiff(in1, in2, diffName + " Moves", diffs, UpdateGen::getMoves);
    }

    private static void listDiff(Scanner in1, Scanner in2, String diffName, StringAppender diffs,
                                 Function<Scanner, List<String>> readValues) {
        List<String> values1 = readValues.apply(in1);
        List<String> values2 = readValues.apply(in2);

        List<String> removedValues = GeneralUtils.inFirstNotSecond(values1, values2);
        List<String> addedValues = GeneralUtils.inFirstNotSecond(values2, values1);

        addListDiff(removedValues, "Removed " + diffName, diffs);
        addListDiff(addedValues, "Added " + diffName, diffs);
    }

    private static void addListDiff(List<String> values, String diffName, StringAppender diffs) {
        if (!values.isEmpty()) {
            diffs.appendLine(diffName + ":\n\t" + String.join("\n\t", values));
        }
    }

    private static void diff(Scanner in1, Scanner in2, String diffName, StringAppender diffs) {
        diff(in1.nextLine(), in2.nextLine(), diffName, diffs);
    }

    // If the two lines are different, appends "diffName: line1 -> line2" to the diffs builder
    private static void diff(String line1, String line2, String diffName, StringAppender diffs) {
        if (!line1.equals(line2)) {
            diffs.appendLine(diffName + ":\n\t" + line1 + " -> " + line2);
        }
    }

    // Compares the pokemon info to info in a new file and outputs the differences
    // Ignores evolution, wild hold items, and flavor text
    private static void newPokemonInfoCompare() {
        Scanner in1 = FileIO.openFile(FileName.POKEMON_INFO);
        Scanner in2 = FileIO.openFile("temp.txt");

        PrintStream out = FileIO.openOutputFile("temp2.txt");

        // Change name to whichever Pokemon in2 starts from
        for (int i = 1; i < PokemonNamesies.BULBASAUR.ordinal(); i++) {
            readSinglePokemon(in1);
        }

        boolean hasDiffs = false;
        for (int i = 1; in2.hasNext(); i++) {
            // Don't print differences for Meltan/Melmetal
            PokemonNamesies pokemonNamesies = PokemonNamesies.values()[i];
            if (pokemonNamesies == PokemonNamesies.MELTAN || pokemonNamesies == PokemonNamesies.MELMETAL) {
                readSinglePokemon(in1);
                readSinglePokemon(in2);
                continue;
            }

            StringAppender diffs = new StringAppender();

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
            diff(in1, in2, "Female Ratio", diffs);
            diff(in1, in2, "Abilities", diffs);
            diff(in1, in2, "Classification", diffs);
            diff(in1.nextInt() + "", in2.nextInt() + "", "Height", diffs);
            diff(in1.nextDouble() + "", in2.nextDouble() + "", "Weight", diffs);
            in1.nextLine(); in2.nextLine(); // Flavor Text -- don't compare
            diff(in1, in2, "Egg Steps", diffs);
            diff(in1, in2, "Egg Groups", diffs);
            movesDiff(in1, in2, "Level Up", diffs);
            movesDiff(in1, in2, "Learnable", diffs);
            in1.nextLine(); in2.nextLine(); // New line

            if (!diffs.isEmpty()) {
                hasDiffs = true;
                out.printf("%03d %s:\n\t%s\n", num, name, diffs.toString().replace("\n", "\n\t"));
            }
        }

        if (!hasDiffs) {
            out.print("No relevant diffs.");
        }
    }

    private static void readSinglePokemon(Scanner in) {
        outputSinglePokemon(in, new PrintStream(new NullOutputStream()));
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
        out.println(in.nextLine()); // Female Ratio
        out.println(in.nextLine()); // Abilities
        out.println(in.nextLine()); // Classification
        out.println(in.nextLine()); // Height Weight FlavorText
        out.println(in.nextLine()); // Egg Steps
        out.println(in.nextLine()); // Egg Groups
        readMoves(in, out);         // Level Up Moves
        readMoves(in, out);         // Learnable Moves
        out.println(in.nextLine()); // New Line
    }

    // Outputs a single Pokemon, but reads from two input sources (only one is printed by default)
    private static void outputSinglePokemon(Scanner in, Scanner in2, PrintStream out) {
        out.println(in.nextLine()); in2.nextLine(); // Num
        out.println(in.nextLine()); in2.nextLine(); // Name
        out.println(in.nextLine()); in2.nextLine(); // Base Stats
        out.println(in.nextLine()); in2.nextLine(); // Base Exp
        out.println(in.nextLine()); in2.nextLine(); // Growth Rate
        out.println(in.nextLine()); in2.nextLine(); // Types
        out.println(in.nextLine()); in2.nextLine(); // Catch Rate
        out.println(in.nextLine()); in2.nextLine(); // EVs
        readEvolution(in, out); readEvolution(in2); // Evolution
        readHoldItems(in, out); readHoldItems(in2); // Wild Items
        out.println(in.nextLine()); in2.nextLine(); // Female Ratio
        out.println(in.nextLine()); in2.nextLine(); // Abilities
        out.println(in.nextLine()); in2.nextLine(); // Classification
        out.println(in.nextLine()); in2.nextLine(); // Height Weight FlavorText
        out.println(in.nextLine()); in2.nextLine(); // Egg Steps
        out.println(in.nextLine()); in2.nextLine(); // Egg Groups
        readMoves(in, out); readMoves(in2);         // Level Up Moves
        readMoves(in, out); readMoves(in2);         // Learnable Moves
        out.println(in.nextLine()); in2.nextLine(); // New Line
    }

    // Used for editing pokemoninfo.txt
    private static void pokemonInfoStuff() {
        Scanner in = FileIO.openFile(FileName.POKEMON_INFO);
        PrintStream out = FileIO.openOutputFile("out.txt");

        while (in.hasNext()) {
            outputSinglePokemon(in, out);
        }
    }

    // Used for editing pokemoninfo.txt using a second input source
    private static void pokemonInfoStuff2() {
        Scanner in = FileIO.openFile(FileName.POKEMON_INFO);
        Scanner in2 = FileIO.openFile("temp.txt");
        PrintStream out = FileIO.openOutputFile("out.txt");

        for (int i = 1; in.hasNext(); i++) {
            // Don't apply changes for Meltan/Melmetal
            PokemonNamesies pokemonNamesies = PokemonNamesies.values()[i];
            if (pokemonNamesies == PokemonNamesies.MELTAN || pokemonNamesies == PokemonNamesies.MELMETAL) {
                outputSinglePokemon(in, out);
                readSinglePokemon(in2);
                continue;
            }

            outputSinglePokemon(in, in2, out);
        }
    }

    private static void readMoves(Scanner in, PrintStream out) {
        out.print(readMoves(in));
    }

    private static String readMoves(Scanner in) {
        StringAppender moves = new StringAppender();

        int numMoves = in.nextInt(); in.nextLine();
        moves.appendLine(numMoves + "");     // Number of Moves
        for (int i = 0; i < numMoves; i++) {
            moves.appendLine(in.nextLine()); // Each move
        }

        return moves.toString();
    }

    private static void readEvolution(Scanner in, PrintStream out) {
        out.print(readEvolution(in));
    }

    private static String readEvolution(Scanner in) {
        String type = in.next();
        if (type.equals(EvolutionType.MULTI.name())) {
            int numEvolutions = in.nextInt(); in.nextLine();
            return new StringAppender(type + " " + numEvolutions)
                    .appendLine()
                    .appendJoin("", numEvolutions, index -> readEvolution(in))
                    .toString();
        } else {
            return type + in.nextLine() + "\n";
        }
    }

    private static void readHoldItems(Scanner in, PrintStream out) {
        out.print(readHoldItems(in));
    }

    private static String readHoldItems(Scanner in) {
        int num = in.nextInt(); in.nextLine();
        return new StringAppender(num + "\n")
                .appendJoin("", num, index -> in.nextLine().trim() + "\n")
                .toString();
    }

    private static final char[] AL_BHED_PRIMER = {
            'Y', 'P', 'L', 'T', 'A', 'V', 'K', 'R',
            'E', 'Z', 'G', 'M', 'S', 'H', 'U', 'B',
            'X', 'N', 'C', 'D', 'I', 'J', 'F', 'Q',
            'O', 'W'
    };

    private static void translateAlBhed() {
        // Fill this in when you want to translate
        String nonShubby = "You're a shub!";

        StringAppender shubs = new StringAppender();
        for (char c : nonShubby.toCharArray()) {
            if (Character.isLowerCase(c)) {
                shubs.append((char)(AL_BHED_PRIMER[c - 'a'] - 'A' + 'a'));
            } else if (Character.isUpperCase(c)) {
                shubs.append(AL_BHED_PRIMER[c - 'A']);
            } else {
                shubs.append(c);
            }
        }

        System.out.println(shubs);
    }

    private static void addCondition() {
        Condition condition;

        // Fill this in when you want to use this
        String name = "";
        String description = "";
        condition = new AndCondition();

        ConditionMatcher matcher = new ConditionMatcher(name, description, new ConditionSet(condition));

//        ConditionsMatcher.addCondition(matcher);
        System.out.println(matcher.getJson());
    }
}
