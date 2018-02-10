package generator;

import main.Global;
import pokemon.PokemonInfo;
import util.FileIO;
import util.StringAppender;
import util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class NamesiesGen {
    private final NamesiesType namesiesType;

    private final StringAppender namesies;

    NamesiesGen(final NamesiesType namesiesType) {
        this.namesiesType = namesiesType;

        this.namesies = new StringAppender();

        if (namesiesType == NamesiesType.POKEMON_NAMESIES) {
            // Create namesies for each Pokemon
            for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++) {
                PokemonInfo info = PokemonInfo.getPokemonInfo(i);
                this.createNamesies(info.getName(), null);
            }
        }
    }

    void writeNamesies() {
        final String fileName = this.namesiesType.getFileName();

        Scanner original = FileIO.openFile(fileName);
        StringAppender out = new StringAppender();

        boolean canPrint = true;
        boolean outputNamesies = false;

        while (original.hasNext()) {
            String line = original.nextLine();

            if (line.contains("// EVERYTHING ABOVE IS GENERATED ###")) {
                if (!outputNamesies || canPrint) {
                    Global.error("Should not see everything above generated line until after the namesies have been printed");
                }

                canPrint = true;
            }

            if (canPrint) {
                out.appendLine(line);
            }

            if (line.contains("// EVERYTHING BELOW IS GENERATED ###")) {
                if (outputNamesies) {
                    Global.error("Everything generated line should not be repeated.");
                }

                out.appendLine(namesies + ";\n");

                outputNamesies = true;
                canPrint = false;
            }
        }

        FileIO.overwriteFile(fileName, out.toString());
    }

    void createNamesies(String name, String className) {
        String enumName = StringUtils.getNamesiesString(name);
        String parameters = this.getParameters(name, className);

        namesies.appendDelimiter(",\n", String.format("\t%s(%s)", enumName, parameters));
    }

    // Returns the parameters inside the enum constructor: "name" and/or className::new
    private String getParameters(String name, String className) {
        List<String> parametersList = new ArrayList<>();
        if (this.namesiesType.includeName()) {
            parametersList.add("\"" + name + "\"");
        }

        if (!StringUtils.isNullOrEmpty(className)) {
            parametersList.add(className + "::new");
        }

        return String.join(", ", parametersList);
    }
}
