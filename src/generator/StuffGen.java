package generator;

import generator.format.InputFormatter;
import generator.format.MethodFormatter;
import generator.interfaces.InterfaceGen;
import main.Global;
import pokemon.PokemonInfo;
import pokemon.PokemonNamesies;
import util.FileIO;
import util.FileName;
import util.Folder;
import util.StringAppender;
import util.StringUtils;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class StuffGen {

    public static void main(String[] args) {
        new StuffGen();

        System.out.println("GEN GEN GEN");
    }

    private StuffGen() {
        this(new InputFormatter());
    }

    public StuffGen(InputFormatter inputFormatter) {
        new PokeGen(inputFormatter);
        new NamesiesGen(Folder.POKEMON, PokemonNamesies.class);
        baseEvolutionGenerator();

        new InterfaceGen();

        FontMetricsGen.writeFontMetrics();
    }

    // Opens the original file and appends the beginning until the key to generate
    public static StringAppender startGen(final String fileName) {
        Scanner original = FileIO.openFile(fileName);
        StringAppender out = new StringAppender();

        while (original.hasNext()) {
            String line = original.nextLine();
            out.appendLine(line);

            if (line.contains("// EVERYTHING BELOW IS GENERATED ###")) {
                break;
            }
        }

        original.close();
        return out;
    }

    public static ClassFields readFields(Scanner in) {
        ClassFields fields = new ClassFields();
        while (in.hasNextLine()) {
            String line = in.nextLine().trim();
            if (line.equals("*")) {
                break;
            }

            Entry<String, String> pair = getFieldPair(in, line);

            String key = pair.getKey();
            String value = pair.getValue();
            fields.addNew(key, value);
        }

        return fields;
    }

    public static Entry<String, String> getFieldPair(Scanner in, String line) {
        String[] split = line.split(":", 2);
        if (split.length != 2) {
            Global.error("Field key and value must be separated by a colon " + line);
        }

        String key = split[0].trim();
        String value = split[1].trim();

        if (value.isEmpty()) {
            value = readMethod(in);
        }

        return new SimpleEntry<>(key, value);
    }

    public static String createClass(String classComments,
                                     String className,
                                     String superClass,
                                     String interfaces,
                                     String extraFields,
                                     String constructor,
                                     String additional,
                                     boolean isInterface) {
        return new StringAppender("\n")
                .appendLineIf(!StringUtils.isNullOrEmpty(classComments), "\t" + classComments)
                .append("\t" + defineClass(className, isInterface))
                .appendDelimiter(" extends ", superClass)
                .appendDelimiter(" ", interfaces)
                .appendLine(" {")
                .appendLineIf(!isInterface, "\t\tprivate static final long serialVersionUID = 1L;")
                .appendDelimiter(isInterface ? StringUtils.empty() : "\n", extraFields)
                .append(constructor)
                .append(additional)
                .appendLine("\t}")
                .toString();
    }

    private static String defineClass(final String className, final boolean isInterface) {
        final String accessModifier;
        final String classType;

        if (isInterface) {
            accessModifier = "public";
            classType = "interface";
        } else {
            accessModifier = "static";
            classType = "class";
        }

        return accessModifier + " " + classType + " " + className;
    }

    private static String readMethod(Scanner in) {
        StringAppender method = new StringAppender();
        MethodFormatter formatter = new MethodFormatter(2);

        while (in.hasNext()) {
            String line = in.nextLine().trim();
            if (line.equals("###")) {
                break;
            }

            formatter.appendLine(line, method);
        }

        return method.toString();
    }

    private static void baseEvolutionGenerator() {
        Set<PokemonNamesies> set = new HashSet<>();
        for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++) {
            set.add(PokemonInfo.getPokemonInfo(i).namesies());
        }

        set.remove(PokemonNamesies.SHEDINJA);
        set.remove(PokemonNamesies.MANAPHY);
        set.remove(PokemonNamesies.TYPE_NULL);
        set.remove(PokemonNamesies.COSMOG);

        for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++) {
            PokemonInfo pokemonInfo = PokemonInfo.getPokemonInfo(i);

            if (!pokemonInfo.canBreed() && !pokemonInfo.getEvolution().canEvolve()) {
                set.remove(pokemonInfo.namesies());
            }

            for (PokemonNamesies evolution : pokemonInfo.getEvolution().getEvolutions()) {
                set.remove(evolution);
            }
        }

        List<PokemonInfo> baseEvolutions = set
                .stream()
                .map(PokemonNamesies::getInfo)
                .sorted()
                .collect(Collectors.toList());

        StringAppender out = new StringAppender()
                .appendJoin("\n", baseEvolutions, PokemonInfo::getName)
                .appendLine();

        FileIO.overwriteFile(FileName.BASE_EVOLUTIONS, out.toString());
    }

    private static final char[] AL_BHED_PRIMER = {
            'Y', 'P', 'L', 'T', 'A', 'V', 'K', 'R',
            'E', 'Z', 'G', 'M', 'S', 'H', 'U', 'B',
            'X', 'N', 'C', 'D', 'I', 'J', 'F', 'Q',
            'O', 'W'
    };

    private static String translateAlBhed(String nonShubby) {
        StringAppender shubs = new StringAppender();
        for (char c : nonShubby.toCharArray()) {
            if (StringUtils.isLower(c)) {
                shubs.append((char)(AL_BHED_PRIMER[c - 'a'] - 'A' + 'a'));
            } else if (StringUtils.isUpper(c)) {
                shubs.append(AL_BHED_PRIMER[c - 'A']);
            } else {
                shubs.append(c);
            }
        }
        return shubs.toString();
    }
}
