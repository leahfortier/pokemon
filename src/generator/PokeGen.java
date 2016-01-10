package generator;

import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import battle.Attack;
import generator.StuffGen.MethodInfo;
import main.Global;
import main.Namesies;
import main.Namesies.NamesiesType;
import main.Type;
import util.FileIO;
import util.PokeString;

public class PokeGen {
	
	private static int TM_BASE_INDEX = 2000;
	
	private static final String EFFECTS_FOLDER = FileIO.makePath("src", "battle", "effect");
	private static final String POKEMON_EFFECT_PATH = EFFECTS_FOLDER + "PokemonEffect.java";
	private static final String TEAM_EFFECT_PATH = EFFECTS_FOLDER + "TeamEffect.java";
	private static final String BATTLE_EFFECT_PATH = EFFECTS_FOLDER + "BattleEffect.java";
	private static final String WEATHER_PATH = EFFECTS_FOLDER + "Weather.java";
	
	private static final String MOVE_PATH = FileIO.makePath("src", "battle") + "Attack.java";
	private static final String ABILITY_PATH = FileIO.makePath("src", "pokemon") + "Ability.java";
	private static final String ITEM_PATH = FileIO.makePath("src", "item") + "Item.java";
	private static final String ITEM_TILES_PATH = FileIO.makePath("rec", "tiles", "itemTiles");
	
	public enum Generator {
		ATTACK_GEN("Moves.txt", MOVE_PATH, "Attack", NamesiesType.ATTACK, false, true),
		POKEMON_EFFECT_GEN("PokemonEffects.txt", POKEMON_EFFECT_PATH, "PokemonEffect", NamesiesType.EFFECT, true, true),
		TEAM_EFFECT_GEN("TeamEffects.txt", TEAM_EFFECT_PATH, "TeamEffect", NamesiesType.EFFECT, true, true),
		BATTLE_EFFECT_GEN("BattleEffects.txt", BATTLE_EFFECT_PATH, "BattleEffect", NamesiesType.EFFECT, true, true),
		WEATHER_GEN("Weather.txt", WEATHER_PATH, "Weather", NamesiesType.EFFECT, true, true),
		ABILITY_GEN("Abilities.txt", ABILITY_PATH, "Ability", NamesiesType.ABILITY, true, true),
		ITEM_GEN("Items.txt", ITEM_PATH, "Item", NamesiesType.ITEM, false, true);
		
		private final String inputPath;
		private final String outputPath;
		private final String superClass;
		private final NamesiesType appendsies;
		private final boolean activate;
		private final boolean mappity;
		
		private Generator(String inputPath, String outputPath, String superClass, NamesiesType appendsies, boolean activate, boolean mappity)
		{
			this.inputPath = inputPath;
			this.outputPath = outputPath;
			this.superClass = superClass;
			this.appendsies = appendsies;
			this.activate = activate;
			this.mappity = mappity;
		}
		
		public String getInputPath() {
			return this.inputPath;
		}
		
		public String getOutputPath() {
			return this.outputPath;
		}
		
		public String getSuperClass() {
			return this.superClass;
		}
		
		public NamesiesType getNamesiesType() {
			return this.appendsies;
		}
		
		public boolean isActivate() {
			return this.activate;
		}
		
		public boolean isMappity() {
			return this.mappity;
		}	
	}
	
	private final NamesiesGen namesiesGen;
	private Generator currentGen;
	
	public PokeGen(final NamesiesGen namesiesGen) {

		readFormat();
		
		this.namesiesGen = namesiesGen;
		
		// Go through each PokeGen and generate
		for (Generator generator : Generator.values()) {
			this.currentGen = generator;
			superGen();
			
			System.out.println(generator.getInputPath() + " generated.");
		}	
	}
	
	private Map<String, String> readFields(Scanner in, String name, String className, int index) {
		Map<String, String> fields = StuffGen.readFields(in, className);
		
		fields.put("Namesies", name);
		fields.put("ClassName", className);
		
		fields.put("Index", index + "");
		
		// There will be problems if a Field move does not get the necessary methods
		if (fields.containsKey("MoveType") && fields.get("MoveType").contains("Field"))
		{
			Global.error("Field MoveType must be implemented as FieldMove: True instead of through the MoveType field. Move: " + className);
		}
		
		// NumTurns matches to both MinTurns and MaxTurns
		if (fields.containsKey("NumTurns"))
		{
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
	
	private void addClass(StringBuilder out, StringBuilder classes, String name, String className, Map<String, String> fields)
	{
		this.namesiesGen.createNamesies(name, this.currentGen.getNamesiesType());
		
		// Mappity map
		if (this.currentGen.isMappity())
		{
			out.append("\t\tmap.put(\"" + name + "\", new " + className + "());\n");	
		}

		List<String> interfaces = new ArrayList<>();
		String additionalMethods = getAdditionalMethods(fields, interfaces);
		String constructor = getConstructor(fields);

		String implementsString = getImplementsString(interfaces);
		
		String extraFields = "";
		if (fields.containsKey("Field"))
		{
			extraFields = fields.get("Field");
			fields.remove("Field");
		}
		
		// Write activation method if applicable
		additionalMethods = getActivationMethod(className, fields) + additionalMethods;
		
		String classString = StuffGen.createClass(className, this.currentGen.getSuperClass(), implementsString, extraFields, constructor, additionalMethods);
		
		fields.remove("ClassName");
		fields.remove("Index");
		
		for (String s : fields.keySet())
		{
			Global.error("Unused field " + s + " for class " + className);
		}
		
		classes.append(classString);
	}
	
	private void superGen()
	{
		StringBuilder out = startGen();
		
		Scanner in = FileIO.openFile(this.currentGen.getInputPath());
		readFileFormat(in);
		
		// StringBuilder for the classes (does not append to out directly because of the map)
		StringBuilder classes = new StringBuilder();
		
		// The image index file for the item generator
		StringBuilder indexOut = new StringBuilder();
		int index = 0;
		
		while (in.hasNext())
		{
			String line = in.nextLine().trim();
			
			// Ignore comments and white space at beginning of file
			if (line.length() == 0 || line.charAt(0) == '#')
			{
				continue;
			}
			
			// Get the name
			String name = line.replace(":", "");
			String className = PokeString.writeClassName(name);
			
			// Read in all of the fields
			Map<String, String> fields = readFields(in, name, className, index);
			
			addClass(out, classes, name, className, fields);
			
			if (this.currentGen == Generator.ITEM_GEN)
			{
				addImageIndex(indexOut, index, name, className.toLowerCase());
			}
			
			index++;
		}
		
		switch (this.currentGen)
		{
			case ATTACK_GEN:
				out.append("\n\t\tfor (String s : map.keySet())\n\t\t{\n\t\t\tmoveNames.add(s);\n\t\t}\n");
				break;
			case ITEM_GEN:
				addTMs(out, classes, indexOut);
				out.append("\n\t\tprocessIncenseItems();\n");
				FileIO.writeToFile(ITEM_TILES_PATH + "index.txt", indexOut);
			default:
				break;
		}
		
		if (this.currentGen.isMappity())
		{
			out.append("\t}\n\n");	
		}
		
		out.append("\t/**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/\n"); // DON'T DO IT
		out.append(classes + "}");
		
		FileIO.writeToFile(this.currentGen.getOutputPath(), out);
	}
	
	private String getActivationMethod(String className, Map<String, String> fields)
	{
		if (!this.currentGen.isActivate())
		{
			return "";
		}
		
		String activation = "(" + className + ")(new " + className + "().activate())";
		String activateHeader = className + " newInstance()";
		
		MethodInfo activateInfo;
		if (fields.containsKey("Activate"))
		{
			String activateBegin = className + " x = " + activation + ";\n";
			String activateEnd = "return x;";
			
			activateInfo = new MethodInfo(activateHeader, activateBegin, fields.get("Activate"), activateEnd);
			
			fields.remove("Activate");
		}
		else
		{
			activateInfo = new MethodInfo(activateHeader, "", "return " + activation + ";", "");
		}
		
		return MethodInfo.writeFunction(activateInfo, "", className);
	}
	
	private static void readFileFormat(Scanner in)
	{
		constructorKeys = new ArrayList<Entry<String, String>>();
		fieldKeys = new ArrayList<Entry<String, String>>();
		failureInfo = null;
		
		while (in.hasNext())
		{
			String line = in.nextLine().trim();
			
			// Ignore comments and white space at beginning of file
			if (line.length() == 0 || line.charAt(0) == '#')
			{
				continue;
			}
			
			if (line.equals("***"))
			{
				return;
			}
			
			String formatType = line.replace(":", "");
			
			if (formatType.equals("Failure"))
			{
				failureInfo = new Failure(in);
				continue;
			}
			
			while (in.hasNextLine())
			{
				line = in.nextLine().trim();
				if (line.equals("*"))
				{
					break;
				}
				
				String[] split = line.split(" ", 2);
				
				String key = split[0].trim();
				String value = split[1].trim();
				
				Entry<String, String> entry = new SimpleEntry<String, String>(key, value);
				
				switch (formatType)
				{
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
	private static Failure failureInfo;
	
	private static class Failure
	{
		private String header; 
		List<Entry<String, String>> failureInfo;
		
		public Failure(Scanner in)
		{
			failureInfo = new ArrayList<>();
			
			while (in.hasNext())
			{
				String line = in.nextLine().trim();
				
				if (line.equals("*"))
				{
					break;
				}
				
				String[] split = line.split(" ", 2);
				
				String fieldName = split[0];
				String fieldInfo = split[1];
				
				if (fieldName.equals("Header"))
				{
					this.header = fieldInfo;
				}
				else
				{
					failureInfo.add(new SimpleEntry<>(fieldName, fieldInfo));
				}
			}
		}
		
		private String writeFailure(Map<String, String> fields)
		{
			String failure = "";
			
			String className = fields.get("ClassName");
			boolean first = true;
			
			for (Entry<String, String> entry : failureInfo)
			{
				String fieldName = entry.getKey();
				String fieldInfo = entry.getValue();
				
				int index = 0;
				String[] split = fieldInfo.split(" ");
			
				boolean not = false, list = false;
				
				String fieldType = split[index++];
				if (fieldType.equals("List"))
				{
					list = true;
					fieldType = split[index++]; 
				}
				
				if (fieldType.equals("Not"))
				{
					not = true;
					fieldType = split[index++];
				}
				
				String defaultValue = "";
				if (fieldType.equals("Default"))
				{
					defaultValue = split[index++];
					fieldType = split[index++];
				}
				
				String fieldValue = fields.get(fieldName);
				if (fieldValue == null)
				{
					if (!not)
					{
						continue;
					}
					
					fieldValue = defaultValue;
				}
				else if (not)
				{
					fields.remove(fieldName);
					continue;
				}
				
				String[] fieldValues = new String[] {fieldValue};
				if (list)
				{
					fieldValues = fieldValue.split(",");
				}

				int previousIndex = index;
				
				for (String value : fieldValues)
				{
					index = previousIndex;
					
					Entry<Integer, String> pair = getValue(split, value, index);
					index = pair.getKey();
					String pairValue = pair.getValue();

					String body = "";
					boolean space = false;
					
					for (; index < split.length; index++)
					{
						body += (space ? " " : "") + split[index];
						space = true;
					}
					
					body = StuffGen.replaceBody(body, className, pairValue);
					
					failure += (first ? "" : " || ")  + body;
					first = false;	
				}
				
				fields.remove(fieldName);
			}
			
			if (failure.length() == 0)
			{
				return failure;
			}
			
			failure = "return !(" + failure + ");";
			return MethodInfo.writeFunction(this.header, failure.toString());
		}
	}
	
	private static String getConstructorValue(Entry<String, String> pair, Map<String, String> fields)
	{
		int index = 0;
		String[] split = pair.getValue().split(" ");
		String type = split[index++];
		
		String fieldValue = null;
		String className = fields.get("ClassName");
		
		if (type.equals("DefaultMap"))
		{
			String mapKey = split[index++];
			fieldValue = fields.get(mapKey);
			
			if (fieldValue == null)
			{
				Global.error("Invalid map key " + mapKey + " for " + className);
			}
			
			type = split[index++];
		}
		else if (type.equals("Default"))
		{
			fieldValue = split[index++];
			type = split[index++];
		}
		
		String key = pair.getKey();
		
		if (fields.containsKey(key))
		{
			fieldValue = fields.get(key);
			fields.remove(key);
		}
		else if (fieldValue == null)
		{
			Global.error("Missing required constructor field " + key + " for " + className);
		}
	
		String value =  getValue(split, fieldValue, index).getValue();
		return value;
	}
	
	private static String getAdditionalMethods(Map<String, String> fields, List<String> interfaces)
	{
		String className = fields.get("ClassName");
		
		StringBuilder methods = new StringBuilder();
		
		// Add all the interfaces to the interface list
		List<String> currentInterfaces = new ArrayList<>();
		if (fields.containsKey("Int"))
		{
			for (String interfaceName : fields.get("Int").split(", "))
			{
				currentInterfaces.add(interfaceName);
			}
			
			fields.remove("Int");
		}
		
		List<String> nextInterfaces = new ArrayList<>();
		
		boolean moreFields = true;
		while (moreFields)
		{
			moreFields = StuffGen.addMethodInfo(methods, overrideMethods, fields, currentInterfaces, "");
			
			for (int i = 0; i < currentInterfaces.size(); i++)
			{
				String interfaceName = currentInterfaces.get(i);
				interfaces.add(interfaceName);
				
				interfaceName = interfaceName.replace("Hidden-", "");
				
				List<Entry<String, MethodInfo>> list = interfaceMethods.get(interfaceName);
				if (list == null)
				{
					Global.error("Invalid interface name " + interfaceName + " for " + className);
				}
				
				moreFields |= StuffGen.addMethodInfo(methods, list, fields, nextInterfaces, interfaceName);
			}
			
			currentInterfaces = nextInterfaces;
			nextInterfaces = new ArrayList<>();
		}
		
		if (failureInfo != null)
		{
			methods.insert(0, failureInfo.writeFailure(fields));
		}
		
		return methods.toString();
	}
	
	private void addTMs(StringBuilder out, StringBuilder classes, StringBuilder indexOut)
	{
		if (this.currentGen != Generator.ITEM_GEN) {
			Global.error("Can only add TMs for the Item class");
		}
		
		// Add the image index for each type (except for None)
		for (Type t : Type.values())
		{
			if (t == Type.NONE) {
				continue;
			}
			
			String name = t.getName() + "TM";
			addImageIndex(indexOut, TM_BASE_INDEX + t.getIndex(), name, name.toLowerCase());
		}
		
		Scanner in = FileIO.openFile("tmList.txt");
		while (in.hasNext())
		{
			String attackName = in.nextLine().trim();
			String className = PokeString.writeClassName(attackName);
			
			Attack attack = Attack.getAttack(Namesies.getValueOf(attackName, NamesiesType.ATTACK));
			
			String itemName = attackName + " TM";
			className += "TM";
			
			HashMap<String, String> fields = new HashMap<>();
			fields.put("ClassName", className);
			fields.put("Namesies", attackName + "_TM");
			fields.put("Index", TM_BASE_INDEX + attack.getActualType().getIndex() + "");
			fields.put("Desc", attack.getDescription());
			fields.put("TM", attackName);
			
			addClass(out, classes, itemName, className, fields);
		}
	}
	
	private static void addImageIndex(StringBuilder indexOut, int index, String name, String imageName)
	{
		File imageFile = new File(ITEM_TILES_PATH + imageName + ".png");
		if (!imageFile.exists()) 
		{
			System.err.println("Image for " + name + " does not exist." + imageFile.getAbsolutePath());
		}
	
		indexOut.append(String.format("%s.png %08x%n", imageName, index));
	}
	
	private String getConstructor(Map<String, String> fields)
	{
		// TODO: More hardcoded nonsense
		String category = "";
		boolean physicalContact = false;
		
		if (this.currentGen == Generator.ATTACK_GEN)
		{
			category = fields.get("Cat");
			physicalContact = category.equals("Physical");	
		}
		
		StringBuilder constructor = new StringBuilder();
		constructor.append("super(");
		
		boolean first = true;
		for (Entry<String, String> pair : constructorKeys)
		{
			String value = getConstructorValue(pair, fields);
			constructor.append((first ? "" : ", ") + value);
			
			first = false;
		}
		
		constructor.append(");\n");
		
		for (Entry<String, String> pair : fieldKeys)
		{
			String fieldKey = pair.getKey();
			
			if (fields.containsKey(fieldKey))
			{
				String assignment = getAssignment(pair.getValue(), fields.get(fieldKey));
				constructor.append(assignment + "\n");
				fields.remove(fieldKey);
			}
		}
		
		// TODO: I don't like that this is hardcoded -- find a way to change it
		if (fields.containsKey("StatChange"))
		{
			String[] mcSplit = fields.get("StatChange").split(" ");
			
			for (int i = 0, index = 1; i < Integer.parseInt(mcSplit[0]); i++)   
			{
				constructor.append("super.statChanges[Stat." + mcSplit[index++].toUpperCase() + ".index()] = " + mcSplit[index++] + ";\n");
			}	
			
			fields.remove("StatChange");
		}
		
		if (this.currentGen == Generator.ATTACK_GEN)
		{
			if (fields.containsKey("PhysicalContact"))
			{
				String physicalContactSpecified = fields.get("PhysicalContact");
				
				if (!physicalContactSpecified.equals("True") && !physicalContactSpecified.equals("False")) 
					Global.error("True and false are the only valid fields for physical contact (Move " + fields.get("ClassName") + ")");
				
				physicalContact = Boolean.parseBoolean(physicalContactSpecified);
				
				if (category.contains("Status")) 
					Global.error("Status moves never make physical contact (Move " + fields.get("ClassName") + ")");
				
				if (physicalContact && category.contains("Physical")) 
					Global.error("Physical moves have implied physical contact (Move " + fields.get("ClassName") + ")");
				
				if (!physicalContact && category.contains("Special")) 
					Global.error("Special moves have implied no physical contact (Move " + fields.get("ClassName") + ")");
				
				fields.remove("PhysicalContact");	
			}
			
			if (physicalContact)
			{
				constructor.append("super.moveTypes.add(MoveType.PHYSICAL_CONTACT);\n");
			}
		}

		return MethodInfo.writeFunction(fields.get("ClassName") + "()", constructor.toString());
	}
	
	private static String getAssignment(String assignmentInfo, String fieldValue)
	{
		int index = 0;
		String[] split = assignmentInfo.split(" ");
		
		String type = split[index++];
		if (type.equals("Multiple"))
		{
			StringBuilder assignments = new StringBuilder();
			
			assignmentInfo = assignmentInfo.substring("Multiple".length() + 1);
			
			boolean first = true;
			
			for (String value : fieldValue.split(","))
			{
				assignments.append((first ? "" : "\n") + getAssignment(assignmentInfo, value.trim()));
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
	
	private static String getImplementsString(List<String> interfaces)
	{
		boolean implemented = false;
		String implementsString = "";
		
		for (String interfaceName : interfaces)
		{
			if (interfaceName.contains("Hidden-"))
			{
				continue;
			}
			
			implementsString += (implemented ? ", " : "implements ") + interfaceName;
			implemented = true;
		}
		
		return implementsString;
	}
	
	private static List<Entry<String, MethodInfo>> overrideMethods;
	private static HashMap<String, List<Entry<String, MethodInfo>>> interfaceMethods;
	
	private static void readFormat()
	{
		Scanner in = FileIO.openFile("override.txt");
		
		overrideMethods = new ArrayList<>();
		interfaceMethods = new HashMap<>();
		
		HashSet<String> fieldNames = new HashSet<>();
		
		while (in.hasNext())
		{
			String line = in.nextLine().trim();
			
			// Ignore comments and white space at beginning of file
			if (line.length() == 0 || line.charAt(0) == '#')
			{
				continue;
			}
			
			String interfaceName = line.replace(":", "");
			List<Entry<String, MethodInfo>> list = new ArrayList<>();
			
			boolean isInterfaceMethod = !interfaceName.equals("Override");
			
			while (in.hasNextLine())
			{
				line = in.nextLine().trim();
				
				if (line.equals("***"))
				{
					break;
				}
				
				String fieldName = line.replace(":", "");
				list.add(new SimpleEntry<>(fieldName, new MethodInfo(in, isInterfaceMethod)));
				
				if (fieldNames.contains(fieldName))
				{
					Global.error("Duplicate field name " + fieldName + " in override.txt");
				}
				
				fieldNames.add(fieldName);
			}
			
			if (isInterfaceMethod)
			{
				interfaceMethods.put(interfaceName, list);	
			}
			else
			{
				if (list.size() != 1)
				{
					Global.error("Only interfaces can include multiple methods");
				}
				
				overrideMethods.add(list.get(0));
			}
		}
	}
	
	private static Entry<Integer, String> getValue(String[] splitInfo, String fieldValue, int index)
	{
		String type = splitInfo[index - 1];
		String value;
		
		String[] mcSplit = fieldValue.split(" ");
		
		switch (type)
		{
			case "String":
				value = "\"" + fieldValue + "\"";
				break;
			case "Int":
				value = fieldValue;
				break;
			case "Boolean":
				value = fieldValue.toLowerCase();
				
				if (!value.equals("false") && !value.equals("true"))
				{
					Global.error("Invalid boolean type " + value);
				}
				
				break;
			case "Enum":
				String enumType = splitInfo[index++];
				
				if (enumType.equals("Namesies"))
				{
					String appendsies = splitInfo[index++];
					value = PokeString.getNamesiesString(fieldValue, NamesiesType.valueOf(appendsies.toUpperCase()));
				}
				else
				{
					value = fieldValue.toUpperCase();	
				}
				
				value = enumType + "." + value;
				
				break;
			case "Function":
				String functionName = splitInfo[index++];
				int numParameters = Integer.parseInt(splitInfo[index++]);
				
				value = functionName + "(";
				boolean first = true;
				
				for (int i = 0; i < numParameters; i++)
				{
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
				return null;
		}
		
		return new SimpleEntry<Integer, String>(index, value);
	}
}
