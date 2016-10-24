package generator;

import java.io.PrintStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import main.Global;
import pokemon.PokemonInfo;
import util.FileIO;
import util.StringUtils;

public class StuffGen {
	private static final String POKEMON_TILES_INDEX_PATH = FileIO.makeFolderPath("rec", "tiles", "pokemonTiles") + "index.txt";
	private static final String POKEMON_SMALL_TILES_INDEX_PATH = FileIO.makeFolderPath("rec", "tiles", "partyTiles") + "index.txt";
	
	public StuffGen() {
		new PokeGen();
		
//		new InterfaceGen();
		
//		pokemonInfoStuff();
//		compareMoves();
//		DrawMetrics.FindMetrics.writeFontMetrics();
	}

	// Opens the original file and appends the beginning until the key to generate
	static StringBuilder startGen(final String fileName) {
		Scanner original = FileIO.openFile(fileName);
		StringBuilder out = new StringBuilder();
		
		while (original.hasNext()) {
			String line = original.nextLine();
			StringUtils.appendLine(out, line);

			if (line.contains("// EVERYTHING BELOW IS GENERATED ###")) {
				break;
			}
		}
		
		original.close();
		return out;
	}
	
	static String replaceBody(String body, String fieldValue, String className, String superClass) {
		body = body.replace("@ClassName", className);
		body = body.replace("@SuperClass", superClass.toUpperCase());
		
		body = body.replace("{0}", fieldValue);
		body = body.replace("{00}", fieldValue.toUpperCase());
		
		String[] mcSplit = fieldValue.split(" ");
		for (int i = 0; i < mcSplit.length; i++) {
			body = body.replace(String.format("{%d}", i + 1), mcSplit[i]);
			body = body.replace(String.format("{%d%d}", i + 1, i + 1), mcSplit[i].toUpperCase());
			body = body.replace(String.format("{%d_}", i + 1), mcSplit[i].replaceAll("_", " "));
			
			String pattern = String.format("{%d-}", i + 1);
			if (body.contains(pattern)) {
				if (i + 1 == 1) {
					Global.error("Don't use {1-}, instead use {0} (ClassName = " + className + ")");
				}
				
				String text = mcSplit[i];
				for (int j = i + 1; j < mcSplit.length; j++) {
					if (body.contains("{" + (j + 1))) {
						System.out.println(body);
						Global.error(j + " Cannot have any more parameters once you split through. (ClassName = " + className + ")");
					}
					
					text += " " + mcSplit[j];
				}
				
				body = body.replace(pattern, text);
			}
		}
		
		return body;
	}
	
	static Map<String, String> readFields(Scanner in, String className) {
		Map<String, String> fields = new HashMap<>();
		
		while (in.hasNextLine()) {
			String line = in.nextLine().trim();
			if (line.equals("*")) {
				break;
			}
			
			Entry<String, String> pair = getFieldPair(in, line);
			
			String key = pair.getKey();
			String value = pair.getValue();
			
			if (fields.containsKey(key)) {
				Global.error("Repeated field " + key + " for " + className);
			}

			fields.put(key, value);
		}
		
		return fields;
	}
	
	static Entry<String, String> getFieldPair(Scanner in, String line) {
		String[] split = line.split(":", 2);
		if (split.length != 2) {
			Global.error("Field key and value must be separated by a colon " + line);
		}
		
		String key = split[0].trim();
		String value = split[1].trim();
		
		if (value.isEmpty()) {
			value = readFunction(in);
		}
		
		return new SimpleEntry<>(key, value);
	}
	
	static String createClass(
			String className,
			String superClass,
			String interfaces,
			String extraFields,
			String constructor,
			String additional,
			boolean isInterface
	) {
		StringBuilder classBuilder = new StringBuilder();
		
		classBuilder.append("\n\t").
				append(defineClass(className, isInterface));
		
		if (superClass != null && superClass.length() > 0) {
			classBuilder.append(" extends ")
					.append(superClass);
		}
		if (interfaces != null && interfaces.length() > 0) {
			classBuilder.append(" ")
					.append(interfaces);
		}
		
		classBuilder.append(" {\n");
		
		if (!isInterface) {
			classBuilder.append("\t\tprivate static final long serialVersionUID = 1L;\n");	
		}
		
		classBuilder.append(extraFields)
				.append(constructor)
				.append(additional)
				.append("\t}\n");
		
		return classBuilder.toString();
	}
	
	private static String defineClass(final String className, final boolean isInterface) {
		final String accessModifier;
		final String classType;
		
		if (isInterface) {
			accessModifier = "public";
			classType = "interface";
		} else {
			accessModifier = "private";
			classType = "class";
		}
		
		return accessModifier + " static " + classType + " " + className;
	}
	
	private static String readFunction(Scanner in) {
		StringBuilder method = new StringBuilder();
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
	
	// Used for editing pokemoninfo.txt
	private static void pokemonInfoStuff() {
		Scanner in = FileIO.openFile("pokemoninfo.txt");
		PrintStream out = FileIO.openOutputFile("out.txt");

		while (in.hasNext()) {
			out.println(in.nextLine()); // Num
			out.println(in.nextLine()); // Name
			out.println(in.nextLine()); // Base Stats
			out.println(in.nextLine()); // Base Exp
			out.println(in.nextLine()); // Growth Rate
			out.println(in.nextLine()); // Type1 Type2
			readMoves(in, out); // Level Up Moves
			readMoves(in, out); // TM Moves
			readMoves(in, out); // Egg Moves
			readMoves(in, out); // Move Tutor Moves
			out.println(in.nextLine()); // Catch Rate
			out.println(in.nextLine()); // EVs
			readEvolution(in, out); // Evolution
			readHoldItems(in, out); // Wild Items
			out.println(in.nextLine()); // Male Ratio
			out.println(in.nextLine()); // Ability 1
			out.println(in.nextLine()); // Ability 2
			out.println(in.nextLine()); // Classification
			out.println(in.nextLine()); // Height Weight FlavorText
			out.println(in.nextLine()); // Egg Steps
			out.println(in.nextLine()); // Egg Group 1
			out.println(in.nextLine()); // Egg Group 2
			
			out.println(in.nextLine()); // New Line
		}
	}
	
	private static void readMoves(Scanner in, PrintStream out) {
		int numMoves = in.nextInt();
		out.println(numMoves); // Number of Moves 
		in.nextLine();
		
		for (int i = 0; i < numMoves; i++) {
			out.println(in.nextLine()); // Each move and level
		}
	}
	
	private static void readEvolution(Scanner in, PrintStream out) {
		String type = in.next();
		if (type.equals("Multi")) {
			int x = in.nextInt();
			out.println(type + " " + x);
			for (int i = 0; i < x; i++) {
				readEvolution(in, out);
			}
			
			return;
		}
		
		out.println(type + in.nextLine());
	}
	
	private static void readHoldItems(Scanner in, PrintStream out) {
		int num = in.nextInt();
		out.println(num);
		in.nextLine();
		for (int i = 0; i < num; i++) 
			out.println(in.nextLine());
	}
	
	private static void generatePokemonTileIndices() {
		StringBuilder out = new StringBuilder();
		for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++) {
			out.append(String.format("%03d.png %08x%n", i, i*4));
			out.append(String.format("%03d-back.png %08x%n", i, i*4 + 1));
			
			if (i >= 650) {
				out.append(String.format("%03d.png %08x%n", i, i*4 + 2));
				out.append(String.format("%03d-back.png %08x%n", i, i*4 + 3));
			}
			else {
				out.append(String.format("%03d-shiny.png %08x%n", i, i*4 + 2));
				out.append(String.format("%03d-shiny-back.png %08x%n", i, i*4 + 3));				
			}
		}
		
		out.append("pokeball.png 00011111\n");
		out.append("egg.png 00010000\n");
		
		FileIO.writeToFile(POKEMON_TILES_INDEX_PATH, out);
	}
	
	private static void generatePokemonPartyTileIndices() {
		StringBuilder out = new StringBuilder();
		for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++) {
			out.append(String.format("%03d-small.png %08x%n", i, i));
		}
		
		out.append("egg-small.png 00010000\n");
		
		FileIO.writeToFile(POKEMON_SMALL_TILES_INDEX_PATH, out);
	}
}
