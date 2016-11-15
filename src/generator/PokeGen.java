package generator;

import battle.attack.Attack;
import battle.effect.generic.BattleEffect;
import battle.effect.generic.PokemonEffect;
import battle.effect.generic.TeamEffect;
import battle.effect.generic.Weather;
import item.Item;
import main.Global;
import main.Type;
import pokemon.ability.AbilityNamesies;
import battle.attack.AttackNamesies;
import battle.effect.generic.EffectNamesies;
import item.ItemNamesies;
import pokemon.ability.Ability;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

class PokeGen {
	private static final int TM_BASE_INDEX = 2000;

	private enum Generator {
		ATTACK_GEN("Moves.txt", Folder.ATTACK, Attack.class, AttackNamesies.class, false, true),
		POKEMON_EFFECT_GEN("PokemonEffects.txt", Folder.GENERIC_EFFECT, PokemonEffect.class, EffectNamesies.class, true, true),
		TEAM_EFFECT_GEN("TeamEffects.txt", Folder.GENERIC_EFFECT, TeamEffect.class, EffectNamesies.class, true, true),
		BATTLE_EFFECT_GEN("BattleEffects.txt", Folder.GENERIC_EFFECT, BattleEffect.class, EffectNamesies.class, true, true),
		WEATHER_GEN("Weather.txt", Folder.GENERIC_EFFECT, Weather.class, EffectNamesies.class, true, true),
		ABILITY_GEN("Abilities.txt", Folder.ABILITY, Ability.class, AbilityNamesies.class, true, true),
		ITEM_GEN("Items.txt", Folder.ITEMS, Item.class, ItemNamesies.class, false, true);
		
		private final String inputPath;
		private final String outputPath;
		private final String outputFolder;
		private final String superClassName;
		private final Class namesiesClass;
		private final boolean activate;
		private final boolean mappity;
		
		Generator(String inputFileName, String outputFolder, Class superClass, Class namesiesClass, boolean activate, boolean mappity) {
			this.inputPath = Folder.GENERATOR + inputFileName;
			this.outputPath = outputFolder + superClass.getSimpleName() + ".java";
			this.outputFolder = outputFolder;
			this.superClassName = superClass.getSimpleName();
			this.namesiesClass = namesiesClass;
			this.activate = activate;
			this.mappity = mappity;
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
		
		public boolean isActivate() {
			return this.activate;
		}
		
		public boolean isMappity() {
			return this.mappity;
		}
	}

	private NamesiesGen namesiesGen;
	private Generator currentGen;
	
	PokeGen() {
		readFormat();

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
	
	private StringBuilder startGen() {
		StringBuilder out = StuffGen.startGen(this.currentGen.getOutputPath());
		out.append("\n\t\t// List all of the classes we are loading\n");
		
		return out;
	}
	
	private void addClass(StringBuilder out, StringBuilder classes, String name, String className, Map<String, String> fields) {
		this.namesiesGen.createNamesies(name, className);
		
		// Mappity map
		if (this.currentGen.isMappity()) {
			out.append("\t\tmap.put(\"")
					.append(name)
					.append("\", new ")
					.append(className)
					.append("());\n");
		}

		List<String> interfaces = new ArrayList<>();
		String additionalMethods = getAdditionalMethods(fields, interfaces);
		String constructor = getConstructor(fields);

		String implementsString = getImplementsString(interfaces);
		
		String extraFields = "";
		if (fields.containsKey("Field")) {
			extraFields = fields.get("Field");
			fields.remove("Field");
		}
		
		// Write activation method if applicable
		additionalMethods = getActivationMethod(className, fields) + additionalMethods;
		
		String classString = StuffGen.createClass(null, className, this.currentGen.getSuperClassName(), implementsString, extraFields, constructor, additionalMethods, false);
		
		fields.remove("ClassName");
		fields.remove("Index");
		
		for (String s : fields.keySet()) {
			Global.error("Unused field " + s + " for class " + className);
		}
		
		classes.append(classString);
	}
	
	private void superGen() {
		StringBuilder out = startGen();

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
			
			addClass(out, classes, name, className, fields);
			
			if (this.currentGen == Generator.ITEM_GEN) {
				addImageIndex(indexOut, index, name, className.toLowerCase());
			}
			
			index++;
		}
		
		switch (this.currentGen) {
			case ATTACK_GEN:
				out.append("\n\t\tfor (String s : map.keySet()) {\n\t\t\tmoveNames.add(s);\n\t\t}\n");
				break;
			case ITEM_GEN:
				addTMs(out, classes, indexOut);
				out.append("\n\t\tprocessIncenseItems();\n");
				FileIO.writeToFile(Folder.ITEM_TILES + "index.txt", indexOut);
			default:
				break;
		}
		
		if (this.currentGen.isMappity()) {
			out.append("\t}\n\n");	
		}
		
		out.append("\t/**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/\n") // DON'T DO IT
				.append(classes)
				.append("}");

		FileIO.overwriteFile(this.currentGen.getOutputPath(), out);
	}
	
	private String getActivationMethod(String className, Map<String, String> fields) {
		if (!this.currentGen.isActivate()) {
			return StringUtils.empty();
		}

		String activation = "(" + className + ")(new " + className + "().activate())";
		String activateHeader = className + " newInstance()";
		
		MethodInfo activateInfo;
		if (fields.containsKey("Activate")) {
			String activateBegin = className + " x = " + activation + ";\n";
			String activateEnd = "return x;";
			
			activateInfo = new MethodInfo(activateHeader, activateBegin, fields.get("Activate"), activateEnd);
			
			fields.remove("Activate");
		}
		else {
			activateInfo = new MethodInfo(activateHeader, "", "return " + activation + ";", "");
		}
		
		return activateInfo.writeFunction("", className, this.currentGen.superClassName);
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
	
	private static String getConstructorValue(Entry<String, String> pair, Map<String, String> fields) {
		int index = 0;
		String[] split = pair.getValue().split(" ");
		String type = split[index++];
		
		String fieldValue = null;
		String className = fields.get("ClassName");
		
		if (type.equals("DefaultMap")) {
			String mapKey = split[index++];
			fieldValue = fields.get(mapKey);
			
			if (fieldValue == null) {
				Global.error("Invalid map key " + mapKey + " for " + className);
			}
			
			type = split[index++];
		}
		else if (type.equals("Default")) {
			fieldValue = split[index++];
			type = split[index++];
		}
		
		String key = pair.getKey();
		
		if (fields.containsKey(key)) {
			fieldValue = fields.get(key);
			fields.remove(key);
		}
		else if (fieldValue == null) {
			Global.error("Missing required constructor field " + key + " for " + className);
		}
	
		return getValue(split, fieldValue, index).getValue();
	}
	
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
			moreFields = MethodInfo.addMethodInfo(methods, overrideMethods, fields, currentInterfaces, "", this.currentGen.superClassName);

			for (String interfaceName : currentInterfaces) {
				interfaces.add(interfaceName);

				interfaceName = interfaceName.replace("Hidden-", "");

				List<Entry<String, MethodInfo>> list = interfaceMethods.get(interfaceName);
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
	
	private void addTMs(StringBuilder out, StringBuilder classes, StringBuilder indexOut) {
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

			Attack attack = Attack.getAttack(AttackNamesies.getValueOf(attackName));

			String itemName = attackName + " TM";
			className += "TM";

			Map<String, String> fields = new HashMap<>();
			fields.put("ClassName", className);
			fields.put("Namesies", attackName + "_TM");
			fields.put("Index", TM_BASE_INDEX + attack.getActualType().getIndex() + "");
			fields.put("Desc", attack.getDescription());
			fields.put("TM", attackName);

			addClass(out, classes, itemName, className, fields);
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
			String value = getConstructorValue(pair, fields);
			constructor.append(first ? "" : ", ")
					.append(value);
			
			first = false;
		}
		
		constructor.append(");\n");
		
		for (Entry<String, String> pair : fieldKeys) {
			String fieldKey = pair.getKey();
			
			if (fields.containsKey(fieldKey)) {
				String assignment = getAssignment(pair.getValue(), fields.get(fieldKey));
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

		return new MethodInfo(fields.get("ClassName") + "()", constructor.toString(), MethodInfo.AccessModifier.PACKAGE_PRIVATE).writeFunction();
	}
	
	private static String getAssignment(String assignmentInfo, String fieldValue) {
		int index = 0;
		String[] split = assignmentInfo.split(" ");
		
		String type = split[index++];
		if (type.equals("Multiple")) {
			StringBuilder assignments = new StringBuilder();
			assignmentInfo = assignmentInfo.substring("Multiple".length() + 1);
			
			boolean first = true;
			for (String value : fieldValue.split(",")) {
				assignments.append(first ? "" : "\n")
						.append(getAssignment(assignmentInfo, value.trim()));
				first = false;
			}
			
			return assignments.toString();
		}
		
		Entry<Integer, String> entry = getValue(split, fieldValue, index);
		index = entry.getKey();
		String value = entry.getValue();
		
		String fieldName = split[index++];
		String assignment = "super." + fieldName;
		
		if (split.length > index) {
			String assignmentType = split[index++];
			switch (assignmentType) {
				case "List":
					assignment += ".add(" + value + ");";
					break;
				default:
					Global.error("Invalid parameter " + assignmentType);
			}
		} else {
			assignment += " = " + value + ";";
		}
		
		return assignment;
	}
	
	private static String getImplementsString(List<String> interfaces) {
		boolean implemented = false;
		String implementsString = "";
		
		for (String interfaceName : interfaces) {
			if (interfaceName.contains("Hidden-")) {
				continue;
			}
			
			implementsString += (implemented ? ", " : "implements ") + interfaceName;
			implemented = true;
		}
		
		return implementsString;
	}
	
	private static List<Entry<String, MethodInfo>> overrideMethods;
	private static Map<String, List<Entry<String, MethodInfo>>> interfaceMethods;
	
	private static void readFormat() {
		Scanner in = FileIO.openFile(FileName.OVERRIDE);
		
		overrideMethods = new ArrayList<>();
		interfaceMethods = new HashMap<>();
		
		Set<String> fieldNames = new HashSet<>();
		
		while (in.hasNext()) {
			String line = in.nextLine().trim();
			
			// Ignore comments and white space at beginning of file
			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}
			
			String interfaceName = line.replace(":", "");
			List<Entry<String, MethodInfo>> list = new ArrayList<>();
			
			boolean isInterfaceMethod = !interfaceName.equals("Override");
			
			while (in.hasNextLine()) {
				line = in.nextLine().trim();
				if (line.equals("***")) {
					break;
				}
				
				String fieldName = line.replace(":", "");
				list.add(new SimpleEntry<>(fieldName, new MethodInfo(in, isInterfaceMethod)));
				
				if (fieldNames.contains(fieldName)) {
					Global.error("Duplicate field name " + fieldName + " in override.txt");
				}
				
				fieldNames.add(fieldName);
			}
			
			if (isInterfaceMethod) {
				interfaceMethods.put(interfaceName, list);	
			}
			else {
				if (list.size() != 1) {
					Global.error("Only interfaces can include multiple methods");
				}

				overrideMethods.add(list.get(0));
			}
		}
	}
	
	static Entry<Integer, String> getValue(String[] splitInfo, String fieldValue, int index) {
		String type = splitInfo[index - 1];
		String value;
		
		String[] mcSplit = fieldValue.split(" ");
		
		switch (type) {
			case "String":
				value = "\"" + fieldValue + "\"";
				break;
			case "Int":
				value = fieldValue;
				break;
			case "Boolean":
				value = fieldValue.toLowerCase();
				if (!value.equals("false") && !value.equals("true")) {
					Global.error("Invalid boolean type " + value);
				}
				
				break;
			case "Enum":
				String enumType = splitInfo[index++];
				
				if (enumType.endsWith("Namesies")) {
					value = PokeString.getNamesiesString(fieldValue);
				}
				else {
					value = fieldValue.toUpperCase();	
				}
				
				value = enumType + "." + value;
				
				break;
			case "Function":
				String functionName = splitInfo[index++];
				int numParameters = Integer.parseInt(splitInfo[index++]);
				
				value = functionName + "(";
				boolean first = true;
				
				for (int i = 0; i < numParameters; i++) {
					int mcSplitDex = Integer.parseInt(splitInfo[index++]);
					String parameter;
					
					Entry<Integer, String> entry = getValue(splitInfo, mcSplit[mcSplitDex], index + 1);
					index = entry.getKey();
					parameter = entry.getValue();
					
					value += (first ? "" : ", ") + parameter;
					first = false;
				}
				
				value += ")";
				break;
			default:
				Global.error("Invalid variable type " + type);
				value = "";
				break;
		}
		
		return new SimpleEntry<>(index, value);
	}
}
