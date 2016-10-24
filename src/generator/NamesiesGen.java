package generator;

import java.util.Scanner;

import main.Global;
import namesies.Namesies.NamesiesType;
import pokemon.PokemonInfo;
import util.FileIO;
import util.PokeString;

class NamesiesGen {
	private static final String NAMESIES_FOLDER = FileIO.makeFolderPath("src", "namesies");

	private final String namesiesClassName;
	private final StringBuilder namesies;

	private boolean firstNamesies;
	
	NamesiesGen(final String namesiesClassName) {
		this.namesiesClassName = namesiesClassName;
		this.namesies = new StringBuilder();
		this.firstNamesies = true;
	}
	
	void writeNamesies() {
		final String fileName = NAMESIES_FOLDER + namesiesClassName + ".java";

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
				out.append(line).append("\n");
			}
			
			if (line.contains("// EVERYTHING BELOW IS GENERATED ###")) {
				if (outputNamesies) {
					Global.error("Everything generated line should not be repeated.");
				}
				
				// Add the Pokemon to namesies
				for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++) {
					PokemonInfo info = PokemonInfo.getPokemonInfo(i);
					createNamesies(info.getName(), NamesiesType.POKEMON);
				}
				
				out.append(namesies);
				out.append(";\n\n");
				
				outputNamesies = true;
				canPrint = false;
			}
		}

		if (FileIO.overwriteFile(fileName, out)) {
			System.out.println("Namesies generated.");
		}
	}
	
	void createNamesies(String name, NamesiesType superClass) {
		String enumName = PokeString.getNamesiesString(name, superClass);
		namesies.append(firstNamesies ? "" : ",\n")
				.append("\t")
				.append(enumName)
				.append("(\"")
				.append(name)
				.append("\")");
		firstNamesies = false;
	}
}
