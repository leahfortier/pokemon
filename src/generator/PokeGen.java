package generator;

import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.MoveCategory;
import battle.attack.MoveType;
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
	
	PokeGen(InputFormatter inputFormatter) {
		this.inputFormatter = inputFormatter;

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
	
	private String createClass(String name, String className, Map<String, String> fields) {
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

		fields.remove("ClassName");
		fields.remove("Index");

		for (String field : fields.keySet()) {
			Global.error("Unused field " + field + " for class " + className);
		}

		return StuffGen.createClass(
				null,
				className,
				this.currentGen.getSuperClassName(),
				implementsString,
				extraFields,
				constructor,
				additionalMethods,
				false
		);
	}
	
	private void superGen() {
		StringBuilder out = StuffGen.startGen(this.currentGen.getOutputPath());
		out.append("\t/**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/\n"); // DON'T DO IT

		Scanner in = FileIO.openFile(this.currentGen.getInputPath());
		readFileFormat(in);
		
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

			// Create class and append
			out.append(createClass(name, className, fields));
			
			if (this.currentGen == Generator.ITEM_GEN) {
				addImageIndex(indexOut, index, name, className.toLowerCase());
			}
			
			index++;
		}

		if (this.currentGen == Generator.ITEM_GEN) {
			addTMs(out, indexOut);
			FileIO.writeToFile(Folder.ITEM_TILES + "index.txt", indexOut);
		}

		out.append("}");

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
			moreFields = MethodInfo.addMethodInfo(methods, inputFormatter.getOverrideMethods(), fields, currentInterfaces, StringUtils.empty(), this.currentGen.superClassName, inputFormatter);

			for (String interfaceName : currentInterfaces) {
				interfaces.add(interfaceName);

				interfaceName = interfaceName.replace("Hidden-", "");

				List<Entry<String, MethodInfo>> list = inputFormatter.getInterfaceMethods(interfaceName);
				if (list == null) {
					Global.error("Invalid interface name " + interfaceName + " for " + className);
				}

				moreFields |= MethodInfo.addMethodInfo(methods, list, fields, nextInterfaces, interfaceName, this.currentGen.superClassName, inputFormatter);
			}
			
			currentInterfaces = nextInterfaces;
			nextInterfaces = new ArrayList<>();
		}
		
		if (failureInfo != null) {
			methods.insert(0, failureInfo.writeFailure(fields, this.currentGen.superClassName, inputFormatter));
		}
		
		return methods.toString();
	}
	
	private void addTMs(StringBuilder classes, StringBuilder indexOut) {
		if (this.currentGen != Generator.ITEM_GEN) {
			Global.error("Can only add TMs for the Item class");
		}

		// Add the image index for each type (except for None)
		for (Type type : Type.values()) {
			if (type == Type.NO_TYPE) {
				continue;
			}

			String name = type.getName() + "TM";
			addImageIndex(indexOut, TM_BASE_INDEX + type.getIndex(), name, name.toLowerCase());
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

			classes.append(createClass(itemName, className, fields));
		}
	}
	
	private static void addImageIndex(StringBuilder indexOut, int index, String name, String imageName) {
		File imageFile = new File(Folder.ITEM_TILES + imageName + ".png");
		if (!imageFile.exists()) {
			Global.error("Image for " + name + " does not exist." + imageFile.getAbsolutePath());
		}
	
		indexOut.append(String.format("%s.png %08x%n", imageName, index));
	}

	private static final String CLASS_NAME_FIELD = "ClassName";
	private static final String ACTIVATE_FIELD = "Activate";
	private static final String CATEGORY_FIELD = "Cat";
	private static final String PHYSICAL_CONTACT_FIELD = "PhysicalContact";
	private static final String STAT_CHANGE_FIELD = "StatChange";

	private boolean getPhysicalContact(Map<String, String> fields) {
		boolean physicalContact = false;
		if (this.currentGen == Generator.ATTACK_GEN) {
			String category = fields.get(CATEGORY_FIELD);
			physicalContact = category.toUpperCase().equals(MoveCategory.PHYSICAL.name());

			String physicalContactField = fields.get(PHYSICAL_CONTACT_FIELD);
			if (physicalContactField != null) {
				physicalContact = Boolean.parseBoolean(physicalContactField);
				fields.remove(PHYSICAL_CONTACT_FIELD);
			}
		}

		return physicalContact;
	}

	// For the super call inside the class constructor, returns the comma-separated field values
	// (It's what's inside the super parentheses)
	// Example: 'AttackNamesies.ATTACK_NAME, "Attack Description", 35, Type.NORMAL, MoveCategory.PHYSICAL'
	private String getInternalConstructorValues(Map<String, String> fields) {
		StringBuilder superValues = new StringBuilder();
		for (Entry<String, String> pair : constructorKeys) {
			String value = inputFormatter.getConstructorValue(pair, fields);
			StringUtils.addCommaSeparatedValue(superValues, value);
		}

		return superValues.toString();
	}

	private String getConstructor(Map<String, String> fields) {
		boolean physicalContact = getPhysicalContact(fields);
		
		StringBuilder constructor = new StringBuilder();
		constructor.append("super(")
				.append(getInternalConstructorValues(fields))
				.append(");\n");
		
		for (Entry<String, String> pair : fieldKeys) {
			String fieldKey = pair.getKey();

			String fieldValue = fields.get(fieldKey);
			if (fieldValue != null) {
				String assignment = inputFormatter.getAssignment(pair.getValue(), fieldValue);
				StringUtils.appendLine(constructor, assignment);
				fields.remove(fieldKey);
			}
		}

		if (fields.containsKey(STAT_CHANGE_FIELD)) {
			String[] mcSplit = fields.get(STAT_CHANGE_FIELD).split(" ");
			for (int i = 0, index = 1; i < Integer.parseInt(mcSplit[0]); i++) {
				constructor.append("super.statChanges[Stat.")
						.append(mcSplit[index++].toUpperCase())
						.append(".index()] = ")
						.append(mcSplit[index++])
						.append(";\n");
			}	
			
			fields.remove(STAT_CHANGE_FIELD);
		}

		if (physicalContact) {
			constructor.append("super.moveTypes.add(")
					.append(MoveType.class.getSimpleName())
					.append(".")
					.append(MoveType.PHYSICAL_CONTACT)
					.append(");\n");
		}

		if (fields.containsKey(ACTIVATE_FIELD)) {
			constructor.append(fields.get(ACTIVATE_FIELD));
			fields.remove(ACTIVATE_FIELD);
		}

		return new MethodInfo(
						fields.get(CLASS_NAME_FIELD) + "()",
						constructor.toString(),
						AccessModifier.PACKAGE_PRIVATE
				).writeFunction();
	}
}
