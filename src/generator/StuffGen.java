package generator;

import main.Global;
import pokemon.PokemonInfo;
import pokemon.PokemonNamesies;
import pokemon.evolution.EvolutionType;
import util.FileIO;
import util.FileName;
import util.Folder;
import util.GeneralUtils;
import util.StringUtils;

import java.io.PrintStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
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
			boolean isInterface) {
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

	private static Set<String> getMoves(Scanner in) {
		int numMoves = in.nextInt(); in.nextLine();

		Set<String> moves = new TreeSet<>();
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
		Set<String> moves1 = getMoves(in1);
		Set<String> moves2 = getMoves(in2);

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
				}
		}

		return false;
	}

	// Compares the pokemon info to info in a new file and outputs the differences
	// Ignores evolution, wild hold items, and flavor text
	private static void newPokemonInfoCompare() {
		Scanner in1 = new Scanner(FileIO.readEntireFileWithReplacements(FileName.POKEMON_INFO, false));
		Scanner in2 = new Scanner(FileIO.readEntireFileWithReplacements("temp.txt", false));

		PrintStream out = FileIO.openOutputFile("out.txt");
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
			movesDiff(in1, in2, "Learnable", new StringBuilder()); // Ignore for now

			if (diffs.length() > 0) {
				out.printf("%03d %s:\n\t%s\n", num, name, diffs.toString().replace("\n", "\n\t"));
			}
		}
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
			readMoves(in, out);    		// Level Up Moves
			readMoves(in, out);			// Learnable Moves
			out.println(in.nextLine()); // New Line
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

	private static final char[] AL_BHED_PRIMER = {
			'Y', 'P', 'L', 'T', 'A', 'V', 'K', 'R',
			'E', 'Z', 'G', 'M', 'S', 'H', 'U', 'B',
			'X', 'N', 'C', 'D', 'I', 'J', 'F', 'Q',
			'O', 'W'
	};

	private static String translateAlBhed(String nonShubby) {
		StringBuilder shubs = new StringBuilder();
		for (char c : nonShubby.toCharArray()) {
			if (StringUtils.isLower(c)) {
				shubs.append((char)(AL_BHED_PRIMER[c - 'a'] - 'A' + 'a'));
			}
			else if (StringUtils.isUpper(c)) {
				shubs.append(AL_BHED_PRIMER[c - 'A']);
			}
			else {
				shubs.append(c);
			}
		}
		return shubs.toString();
	}
}
