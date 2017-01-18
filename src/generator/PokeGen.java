package generator;

import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.effect.generic.BattleEffect;
import battle.effect.generic.EffectNamesies;
import battle.effect.generic.PokemonEffect;
import battle.effect.generic.TeamEffect;
import battle.effect.generic.Weather;
import item.Item;
import item.ItemNamesies;
import main.Global;
import main.Type;
import pokemon.ability.Ability;
import pokemon.ability.AbilityNamesies;
import util.FileIO;
import util.FileName;
import util.Folder;
import util.PokeString;
import util.StringUtils;

import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

class PokeGen {
	private static final int TM_BASE_INDEX = 2000;

	private enum Generator {
		ATTACK_GEN("Moves.txt", Folder.ATTACK, Attack.class, AttackNamesies.class),
		POKEMON_EFFECT_GEN("PokemonEffects.txt", Folder.GENERIC_EFFECT, PokemonEffect.class, EffectNamesies.class),
		TEAM_EFFECT_GEN("TeamEffects.txt", Folder.GENERIC_EFFECT, TeamEffect.class, EffectNamesies.class),
		BATTLE_EFFECT_GEN("BattleEffects.txt", Folder.GENERIC_EFFECT, BattleEffect.class, EffectNamesies.class),
		WEATHER_GEN("Weather.txt", Folder.GENERIC_EFFECT, Weather.class, EffectNamesies.class),
		ABILITY_GEN("Abilities.txt", Folder.ABILITY, Ability.class, AbilityNamesies.class),
		ITEM_GEN("Items.txt", Folder.ITEMS, Item.class, ItemNamesies.class);
		
		private final String inputPath;
		private final String outputPath;
		private final String outputFolder;
		private final String superClassName;
		private final Class namesiesClass;
		
		Generator(String inputFileName, String outputFolder, Class superClass, Class namesiesClass) {
			this.inputPath = Folder.GENERATOR + inputFileName;
			this.outputPath = outputFolder + superClass.getSimpleName() + ".java";
			this.outputFolder = outputFolder;
			this.superClassName = superClass.getSimpleName();
			this.namesiesClass = namesiesClass;
		}
		
		public String getInputPath() {
			return this.inputPath;
		}

		public String getOutputPath() {
			return this.outputPath;
		}

		public String getOutputFolder() {
			return this.outputFolder;
		}
		
		public String getSuperClassName() {
			return this.superClassName;
		}

		public Class getNamesiesClass() {
			return this.namesiesClass;
		}
	}

	private final InputFormatter inputFormatter;
	private NamesiesGen namesiesGen;
	private Generator currentGen;
	
	PokeGen() {
		this.inputFormatter = InputFormatter.instance();

		final Map<Class, NamesiesGen> namesiesMap = new HashMap<>();

		// Go through each PokeGen and generate
		for (Generator generator : Generator.values()) {
			this.currentGen = generator;

			final Class namesiesClass = generator.getNamesiesClass();
			if (!namesiesMap.containsKey(namesiesClass)) {
				namesiesMap.put(namesiesClass, new NamesiesGen(generator.getOutputFolder(), namesiesClass));
			}

			this.namesiesGen = namesiesMap.get(namesiesClass);
			this.superGen();
		}

		namesiesMap.values().forEach(NamesiesGen::writeNamesies);
	}
	
	private Map<String, String> readFields(Scanner in, String name, String className, int index) {
		Map<String, String> fields = StuffGen.readFields(in, className);
		
		fields.put("Namesies", name);
		fields.put("ClassName", className);
		
		fields.put("Index", index + "");
		
		// NumTurns matches to both MinTurns and MaxTurns
		if (fields.containsKey("NumTurns")) {
			String numTurns = fields.get("NumTurns");
			fields.put("MinTurns", numTurns);
			fields.put("MaxTurns", numTurns);
			
			fields.remove("NumTurns");
		}
		
		return fields;
	}
	
	private void addClass(StringBuilder classes, String name, String className, Map<String, String> fields) {
		this.namesiesGen.createNamesies(name, className);
		
		List<String> interfaces = new ArrayList<>();
		String additionalMethods = getAdditionalMethods(fields, interfaces);
		String constructor = getConstructor(fields);

		String implementsString = inputFormatter.getImplementsString(interfaces);

		String extraFields = "";
		if (fields.containsKey("Field")) {
			extraFields = fields.get("Field");
			fields.remove("Field");
		}
		
		String classString = StuffGen.createClass(null, className, this.currentGen.getSuperClassName(), implementsString, extraFields, constructor, additionalMethods, false);
		
		fields.remove("ClassName");
		fields.remove("Index");
		
		for (String s : fields.keySet()) {
			Global.error("Unused field " + s + " for class " + className);
		}
		
		classes.append(classString);
	}
	
	private void superGen() {
		StringBuilder out = StuffGen.startGen(this.currentGen.getOutputPath());

		Scanner in = FileIO.openFile(this.currentGen.getInputPath());
		readFileFormat(in);
		
		// StringBuilder for the classes (does not append to out directly because of the map)
		StringBuilder classes = new StringBuilder();
		
		// The image index file for the item generator
		StringBuilder indexOut = new StringBuilder();
		int index = 0;
		
		while (in.hasNext()) {
			String line = in.nextLine().trim();
			
			// Ignore comments and white space
			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}
			
			// Get the name
			String name = line.replace(":", "");
			String className = PokeString.writeClassName(name);
			
			// Read in all of the fields
			Map<String, String> fields = readFields(in, name, className, index);
			
			addClass(classes, name, className, fields);
			
			if (this.currentGen == Generator.ITEM_GEN) {
				addImageIndex(indexOut, index, name, className.toLowerCase());
			}
			
			index++;
		}

		if (this.currentGen == Generator.ITEM_GEN) {
			addTMs(classes, indexOut);
			FileIO.writeToFile(Folder.ITEM_TILES + "index.txt", indexOut);
		}
		
		out.append("\t/**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/\n") // DON'T DO IT
				.append(classes)
				.append("}");

		FileIO.overwriteFile(this.currentGen.getOutputPath(), out);
	}
	
	private static void readFileFormat(Scanner in) {
		constructorKeys = new ArrayList<>();
		fieldKeys = new ArrayList<>();
		failureInfo = null;
		
		while (in.hasNext()) {
			String line = in.nextLine().trim();
			
			// Ignore comments and white space
			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}
			
			if (line.equals("***")) {
				return;
			}
			
			String formatType = line.replace(":", "");
			if (formatType.equals("Failure")) {
				failureInfo = new FailureInfo(in);
				continue;
			}
			
			while (in.hasNextLine()) {
				line = in.nextLine().trim();
				if (line.equals("*")) {
					break;
				}
				
				String[] split = line.split(" ", 2);
				String key = split[0].trim();
				String value = split[1].trim();
				
				Entry<String, String> entry = new SimpleEntry<>(key, value);
				
				switch (formatType) {
					case "Constructor":
						constructorKeys.add(entry);
						break;
					case "Fields":
						fieldKeys.add(entry);
						break;
					default:
						Global.error("Invalid format type " + formatType);
				}
			}	
		}
	}
	
	private static List<Entry<String, String>> constructorKeys;
	private static List<Entry<String, String>> fieldKeys;
	private static FailureInfo failureInfo;
	
	private String getAdditionalMethods(Map<String, String> fields, List<String> interfaces) {
		String className = fields.get("ClassName");
		StringBuilder methods = new StringBuilder();
		
		// Add all the interfaces to the interface list
		List<String> currentInterfaces = new ArrayList<>();
		if (fields.containsKey("Int")) {
			Collections.addAll(currentInterfaces, fields.get("Int").split(", "));
			fields.remove("Int");
		}
		
		List<String> nextInterfaces = new ArrayList<>();
		
		boolean moreFields = true;
		while (moreFields) {
			moreFields = MethodInfo.addMethodInfo(methods, inputFormatter.getOverrideMethods(), fields, currentInterfaces, "", this.currentGen.superClassName);

			for (String interfaceName : currentInterfaces) {
				interfaces.add(interfaceName);

				interfaceName = interfaceName.replace("Hidden-", "");

				List<Entry<String, MethodInfo>> list = inputFormatter.getInterfaceMethods(interfaceName);
				if (list == null) {
					Global.error("Invalid interface name " + interfaceName + " for " + className);
				}

				moreFields |= MethodInfo.addMethodInfo(methods, list, fields, nextInterfaces, interfaceName, this.currentGen.superClassName);
			}
			
			currentInterfaces = nextInterfaces;
			nextInterfaces = new ArrayList<>();
		}
		
		if (failureInfo != null) {
			methods.insert(0, failureInfo.writeFailure(fields, this.currentGen.superClassName));
		}
		
		return methods.toString();
	}
	
	private void addTMs(StringBuilder classes, StringBuilder indexOut) {
		if (this.currentGen != Generator.ITEM_GEN) {
			Global.error("Can only add TMs for the Item class");
		}

		// Add the image index for each type (except for None)
		for (Type t : Type.values()) {
			if (t == Type.NO_TYPE) {
				continue;
			}

			String name = t.getName() + "TM";
			addImageIndex(indexOut, TM_BASE_INDEX + t.getIndex(), name, name.toLowerCase());
		}

		Scanner in = FileIO.openFile(FileName.TM_LIST);
		while (in.hasNext()) {
			String attackName = in.nextLine().trim();
			String className = PokeString.writeClassName(attackName);

			Attack attack = AttackNamesies.getValueOf(attackName).getAttack();

			String itemName = attackName + " TM";
			className += "TM";

			Map<String, String> fields = new HashMap<>();
			fields.put("ClassName", className);
			fields.put("Namesies", attackName + "_TM");
			fields.put("Index", TM_BASE_INDEX + attack.getActualType().getIndex() + "");
			fields.put("Desc", attack.getDescription());
			fields.put("TM", attackName);

			addClass(classes, itemName, className, fields);
		}
	}
	
	private static void addImageIndex(StringBuilder indexOut, int index, String name, String imageName) {
		File imageFile = new File(Folder.ITEM_TILES + imageName + ".png");
		if (!imageFile.exists()) {
			Global.error("Image for " + name + " does not exist." + imageFile.getAbsolutePath());
		}
	
		indexOut.append(String.format("%s.png %08x%n", imageName, index));
	}
	
	private String getConstructor(Map<String, String> fields) {
		// TODO: More hardcoded nonsense
		String category = "";
		boolean physicalContact = false;
		
		if (this.currentGen == Generator.ATTACK_GEN) {
			category = fields.get("Cat");
			physicalContact = category.equals("Physical");	
		}
		
		StringBuilder constructor = new StringBuilder();
		constructor.append("super(");
		
		boolean first = true;
		for (Entry<String, String> pair : constructorKeys) {
			String value = inputFormatter.getConstructorValue(pair, fields);
			constructor.append(first ? "" : ", ")
					.append(value);
			
			first = false;
		}
		
		constructor.append(");\n");
		
		for (Entry<String, String> pair : fieldKeys) {
			String fieldKey = pair.getKey();
			
			if (fields.containsKey(fieldKey)) {
				String assignment = inputFormatter.getAssignment(pair.getValue(), fields.get(fieldKey));
				StringUtils.appendLine(constructor, assignment);
				fields.remove(fieldKey);
			}
		}
		
		// TODO: I don't like that this is hardcoded -- find a way to change it
		if (fields.containsKey("StatChange")) {
			String[] mcSplit = fields.get("StatChange").split(" ");
			
			for (int i = 0, index = 1; i < Integer.parseInt(mcSplit[0]); i++) {
				constructor.append("super.statChanges[Stat.")
						.append(mcSplit[index++].toUpperCase())
						.append(".index()] = ")
						.append(mcSplit[index++])
						.append(";\n");
			}	
			
			fields.remove("StatChange");
		}
		
		if (this.currentGen == Generator.ATTACK_GEN) {
			if (fields.containsKey("PhysicalContact")) {
				String physicalContactSpecified = fields.get("PhysicalContact");
				
				if (!physicalContactSpecified.equals("True") && !physicalContactSpecified.equals("False")) {
					Global.error("True and false are the only valid fields for physical contact (Move " + fields.get("ClassName") + ")");
				}
				
				physicalContact = Boolean.parseBoolean(physicalContactSpecified);

				// TODO: Move to test
				if (category.contains("Status"))  {
					Global.error("Status moves never make physical contact (Move " + fields.get("ClassName") + ")");
				}
				
				if (physicalContact && category.contains("Physical")) { 
					Global.error("Physical moves have implied physical contact (Move " + fields.get("ClassName") + ")");
				}
				
				if (!physicalContact && category.contains("Special")) { 
					Global.error("Special moves have implied no physical contact (Move " + fields.get("ClassName") + ")");
				}
				
				fields.remove("PhysicalContact");	
			}
			
			if (physicalContact) {
				constructor.append("super.moveTypes.add(MoveType.PHYSICAL_CONTACT);\n");
			}
		}

		if (fields.containsKey("Activate")) {
			constructor.append(fields.get("Activate"));
			fields.remove("Activate");
		}

		return new MethodInfo(fields.get("ClassName") + "()", constructor.toString(), AccessModifier.PACKAGE_PRIVATE).writeFunction();
	}
}
