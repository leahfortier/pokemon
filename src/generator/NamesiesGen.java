package generator;

import java.util.Scanner;

import main.Global;
import namesies.PokemonNamesies;
import pokemon.PokemonInfo;
import util.FileIO;
import util.PokeString;
import util.StringUtils;

class NamesiesGen {
	private static final String NAMESIES_FOLDER = FileIO.makeFolderPath("src", "namesies");

	private final Class namesiesClass;
	private final StringBuilder namesies;

	private boolean firstNamesies;
	
	NamesiesGen(final Class namesiesClass) {
		this.namesiesClass = namesiesClass;
		this.namesies = new StringBuilder();
		this.firstNamesies = true;

		if (namesiesClass.equals(PokemonNamesies.class)) {
			pokemonNamesies();
			writeNamesies();
		}
	}
	
	void writeNamesies() {
		final String fileName = NAMESIES_FOLDER + namesiesClass.getSimpleName() + ".java";

		Scanner original = FileIO.openFile(fileName);
		StringBuilder out = new StringBuilder();
		
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
				StringUtils.appendLine(out, line);
			}
			
			if (line.contains("// EVERYTHING BELOW IS GENERATED ###")) {
				if (outputNamesies) {
					Global.error("Everything generated line should not be repeated.");
				}
				
				out.append(namesies);
				out.append(";\n\n");
				
				outputNamesies = true;
				canPrint = false;
			}
		}

		FileIO.overwriteFile(fileName, out);
	}
	
	void createNamesies(String name) {
		String enumName = PokeString.getNamesiesString(name);
		namesies.append(firstNamesies ? "" : ",\n")
				.append("\t")
				.append(enumName)
				.append("(\"")
				.append(name)
				.append("\")");
		firstNamesies = false;
	}

	void pokemonNamesies() {
		// Add the Pokemon to namesies
		for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++) {
			PokemonInfo info = PokemonInfo.getPokemonInfo(i);
			createNamesies(info.getName());
		}
	}
}
