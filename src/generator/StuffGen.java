package generator;

import main.Global;
import pokemon.PokemonInfo;
import pokemon.PokemonNamesies;
import pokemon.evolution.EvolutionType;
import util.FileIO;
import util.FileName;
import util.Folder;
import util.StringUtils;

import java.io.PrintStream;
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

//		pokemonInfoStuff();

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
	
	static ClassFields readFields(Scanner in) {
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
	
	static Entry<String, String> getFieldPair(Scanner in, String line) {
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
	
	static String createClass(
			String classComments,
			String className,
			String superClass,
			String interfaces,
			String extraFields,
			String constructor,
			String additional,
			boolean isInterface
	) {
		StringBuilder classBuilder = new StringBuilder("\n");

		if (!StringUtils.isNullOrEmpty(classComments)) {
			StringUtils.appendLine(classBuilder, "\t" + classComments);
		}
		
		classBuilder.append("\t").
				append(defineClass(className, isInterface));
		
		if (!StringUtils.isNullOrEmpty(superClass)) {
			classBuilder.append(" extends ")
					.append(superClass);
		}
		if (!StringUtils.isNullOrEmpty(interfaces)) {
			classBuilder.append(" ")
					.append(interfaces);
		}
		
		classBuilder.append(" {\n");
		
		if (!isInterface) {
			classBuilder.append("\t\tprivate static final long serialVersionUID = 1L;\n");	
		}

		if (!StringUtils.isNullOrEmpty(extraFields)) {
			classBuilder.append(extraFields);
		}

		if (!StringUtils.isNullOrEmpty(constructor)) {
			classBuilder.append(constructor);
		}

		if (!StringUtils.isNullOrEmpty(additional)) {
			classBuilder.append(additional);
		}
		
		classBuilder.append("\t}\n");
		
		return classBuilder.toString();
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

	private static void baseEvolutionGenerator() {
		Set<PokemonNamesies> set = new HashSet<>();
		for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++) {
			set.add(PokemonInfo.getPokemonInfo(i).namesies());
		}

		set.remove(PokemonNamesies.SHEDINJA); // TODO
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

		List<PokemonInfo> baseEvolutions = set.stream()
				.map(PokemonInfo::getPokemonInfo)
				.sorted()
				.collect(Collectors.toList());

		StringBuilder out = new StringBuilder();
		for (PokemonInfo info : baseEvolutions) {
			StringUtils.appendLine(out, info.getName());
		}

		FileIO.overwriteFile(FileName.BASE_EVOLUTIONS, out);
	}

	// Used for editing pokemoninfo.txt
	private static void pokemonInfoStuff() {
		Scanner in = FileIO.openFile(FileName.POKEMON_INFO);
		PrintStream out = FileIO.openOutputFile("out.txt");

		while (in.hasNext()) {
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
			readMoves(in, out);    // Level Up Moves
			readMoves(in, out); // Learnable Moves
			out.println(in.nextLine()); // New Line
		}
	}

	private static void readMoves(Scanner in, PrintStream out) {
		int numMoves = in.nextInt();
		out.println(numMoves); // Number of Moves
		in.nextLine();

		for (int i = 0; i < numMoves; i++) {
			out.println(in.nextLine()); // Each move
		}
	}
	
	private static void readEvolution(Scanner in, PrintStream out) {
		String type = in.next();
		if (type.equals(EvolutionType.MULTI.name())) {
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

		for (int i = 0; i < num; i++) {
			out.println(in.nextLine());
		}
	}
}
