package generator;

import generator.format.InputFormatter;
import generator.interfaces.InterfaceGen;
import pokemon.PokemonInfo;
import pokemon.PokemonNamesies;
import util.file.FileIO;
import util.file.FileName;
import util.string.StringAppender;
import util.string.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class StuffGen {

    public static void main(String[] args) {
        new StuffGen();

        System.out.println("GEN GEN GEN");
    }

    private StuffGen() {
        new PokeGen(new InputFormatter());
        new InterfaceGen();

        new NamesiesGen(NamesiesType.POKEMON_NAMESIES).writeNamesies();

        baseEvolutionGenerator();
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

    public static String createClass(String classComments,
                                     String className,
                                     String superClass,
                                     String interfaces,
                                     String extraFields,
                                     String constructor,
                                     String additional,
                                     boolean isInterface) {
        return new StringAppender("\n")
                .append(StringUtils.isNullOrEmpty(classComments) ? "" : "\t" + classComments.trim().replaceAll("\n\t", "\n") + "\n")
                .append("\t" + defineClass(className, isInterface))
                .appendDelimiter(" extends ", superClass)
                .appendDelimiter(" ", interfaces)
                .appendLine(" {")
                .appendLineIf(!isInterface, "\t\tprivate static final long serialVersionUID = 1L;")
                .appendDelimiter(isInterface ? "" : "\n", extraFields)
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
}
