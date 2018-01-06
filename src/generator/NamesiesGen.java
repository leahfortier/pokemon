package generator;

import main.Global;
import pokemon.PokemonInfo;
import pokemon.PokemonNamesies;
import util.StringAppender;
import util.FileIO;
import util.StringUtils;

import java.util.Scanner;

class NamesiesGen {
    
    private final String namesiesFolder;
    private final String namesiesClassName;
    private final StringAppender namesies;
    
    NamesiesGen(final String namesiesFolder, final Class namesiesClass) {
        this.namesiesFolder = namesiesFolder;
        this.namesiesClassName = namesiesClass.getSimpleName();
        
        this.namesies = new StringAppender();
        
        if (namesiesClass.equals(PokemonNamesies.class)) {
            pokemonNamesies();
            writeNamesies();
        }
    }
    
    void writeNamesies() {
        final String fileName = this.namesiesFolder + this.namesiesClassName + ".java";
        
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
                
                out.appendLine(namesies + ";\n\t");
                
                outputNamesies = true;
                canPrint = false;
            }
        }
        
        FileIO.overwriteFile(fileName, out.toString());
    }
    
    void createNamesies(String name, String className) {
        String enumName = StringUtils.getNamesiesString(name);
        namesies.appendDelimiter(",\n", String.format(
                "\t%s(\"%s\"%s)",
                enumName,
                name,
                StringUtils.isNullOrEmpty(className) ? "" : ", " + className + "::new"
        ));
    }
    
    private void pokemonNamesies() {
        // Add the Pokemon to namesies
        for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++) {
            PokemonInfo info = PokemonInfo.getPokemonInfo(i);
            createNamesies(info.getName(), null);
        }
    }
}
