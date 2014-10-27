package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.Namesies.NamesiesType;
import pokemon.PokemonInfo;
import battle.Attack;

public class StuffGen 
{
	private static int TM_BASE_INDEX = 2000;
	
	private static String POKEMON_EFFECT_PATH = "src" + Global.FILE_SLASH + "battle" + Global.FILE_SLASH + "effect" + Global.FILE_SLASH + "PokemonEffect.java";
	private static String TEAM_EFFECT_PATH = "src" + Global.FILE_SLASH + "battle" + Global.FILE_SLASH + "effect" + Global.FILE_SLASH + "TeamEffect.java";
	private static String BATTLE_EFFECT_PATH = "src" + Global.FILE_SLASH + "battle" + Global.FILE_SLASH + "effect" + Global.FILE_SLASH + "BattleEffect.java";
	private static String WEATHER_PATH = "src" + Global.FILE_SLASH + "battle" + Global.FILE_SLASH + "effect" + Global.FILE_SLASH + "Weather.java";
	private static String MOVE_PATH = "src" + Global.FILE_SLASH + "battle" + Global.FILE_SLASH + "Attack.java";
	private static String ABILITY_PATH = "src" + Global.FILE_SLASH + "pokemon" + Global.FILE_SLASH + "Ability.java";
	private static String ITEM_PATH = "src" + Global.FILE_SLASH + "item" + Global.FILE_SLASH + "Item.java";
	private static String NAMESIES_PATH = "src" + Global.FILE_SLASH + "main" + Global.FILE_SLASH + "Namesies.java";
	
	private static String ITEM_TILES_PATH = "rec" + Global.FILE_SLASH + "tiles" + Global.FILE_SLASH + "itemTiles" + Global.FILE_SLASH;
	
	private static String POKEMON_TILES_INDEX_PATH = "rec" + Global.FILE_SLASH + "tiles" + Global.FILE_SLASH + "pokemonTiles" + Global.FILE_SLASH + "index.txt";
	private static String POKEMON_SMALL_TILES_INDEX_PATH = "rec" + Global.FILE_SLASH + "tiles" + Global.FILE_SLASH + "partyTiles" + Global.FILE_SLASH + "index.txt";
	
	private enum Generator
	{
		ATTACK_GEN("Moves.txt", MOVE_PATH, "Attack", NamesiesType.ATTACK, false, true),
		POKEMON_EFFECT_GEN("PokemonEffects.txt", POKEMON_EFFECT_PATH, "PokemonEffect", NamesiesType.EFFECT, true, true),
		TEAM_EFFECT_GEN("TeamEffects.txt", TEAM_EFFECT_PATH, "TeamEffect", NamesiesType.EFFECT, true, true),
		BATTLE_EFFECT_GEN("BattleEffects.txt", BATTLE_EFFECT_PATH, "BattleEffect", NamesiesType.EFFECT, true, true),
		WEATHER_GEN("Weather.txt", WEATHER_PATH, "Weather", NamesiesType.EFFECT, true, true),
		ABILITY_GEN("Abilities.txt", ABILITY_PATH, "Ability", NamesiesType.ABILITY, true, true),
		ITEM_GEN("Items.txt", ITEM_PATH, "Item", NamesiesType.ITEM, false, true);
		
		private String inputPath;
		private String outputPath;
		private String superClass;
		private NamesiesType appendsies;
		private boolean activate;
		private boolean mappity;
		
		private Generator(String inputPath, String outputPath, String superClass, NamesiesType appendsies, boolean activate, boolean mappity)
		{
			this.inputPath = inputPath;
			this.outputPath = outputPath;
			this.superClass = superClass;
			this.appendsies = appendsies;
			this.activate = activate;
			this.mappity = mappity;
		}
		
		private void generate()
		{
			superGen(this);
			System.out.println(this.inputPath + " generated.");
		}
	}
	
	private static StringBuilder namesies;
	private static boolean firstNamesies;
	
	public StuffGen()
	{
		readFormat();
		
		namesies = new StringBuilder();
		firstNamesies = true;
		
		for (Generator generator : Generator.values())
		{
			generator.generate();
		}
		
		writeNamesies();
	}
	
	private static void createNamesies(String name, String className, NamesiesType superClass)
	{
		String enumName = Namesies.getNamesies(className, superClass);
		namesies.append((firstNamesies ? "" : ",\n") + "\t" + enumName + "(\"" + name + "\")");
		firstNamesies = false;
	}
	
	private static void writeNamesies()
	{
		Scanner original = openFile(NAMESIES_PATH);
		StringBuilder out = new StringBuilder();
		
		boolean canPrint = true;
		boolean outputNamesies = false;
		
		while (original.hasNext())
		{
			String line = original.nextLine();
			
			if (line.contains("// EVERYTHING ABOVE IS GENERATED ###"))
			{
				if (!outputNamesies || canPrint)
				{
					Global.error("Should not see everything above generated line until after the namesies have been printed");
				}
				
				canPrint = true;
			}
			
			if (canPrint)
			{
				out.append(line + "\n");	
			}
			
			if (line.contains("// EVERYTHING BELOW IS GENERATED ###"))
			{
				if (outputNamesies)
				{
					Global.error("Everything generated line should not be repeated.");
				}
				
				// Add the Pokemon to namesies
				for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++)
				{
					PokemonInfo info = PokemonInfo.getPokemonInfo(i);
					createNamesies(info.getName(), info.getName(), NamesiesType.POKEMON);
				}
				
				out.append(namesies);
				out.append(";\n\n");
				
				outputNamesies = true;
				canPrint = false;
			}
		}
		
		printToFile(NAMESIES_PATH, out);
		System.out.println("Namesies generated.");
	}
	
	// Creates the className from the name and adds to the appropriate fields
	private static String writeClassName(String name)
	{
		String className = "";
		for (int i = 0; i < name.length(); i++)
		{
			if (name.charAt(i) == '-')
			{
				if (Namesies.isLower(name.charAt(i + 1)))
				{
					char c = (char)(name.charAt(i + 1) - 'a' + 'A');
					className += c;
					i++;
					continue;
				}
				
				continue;
			}
			
			if (Namesies.isSpecial(name.charAt(i)))
			{
				continue;
			}
			
			className += name.charAt(i);
		}
		
		className = className.replace("u00e9", "e");
		
		return className;
	}
	
	private static HashMap<String, String> readFields(Scanner in, Generator gen, String className, int index)
	{
		HashMap<String, String> fields = new HashMap<>();
		
		while (in.hasNextLine())
		{
			String line = in.nextLine().trim();
			if (line.equals("*"))
			{
				break;
			}
			
			Entry<String, String> pair = getFieldPair(in, line);
			
			String key = pair.getKey();
			String value = pair.getValue();
			
			if (fields.containsKey(key))
			{
				Global.error("Repeated field " + key + " for " + gen.superClass  + " " + className);
			}
			
			fields.put(key, value);
//			System.out.println(className + " " + key + " " + value);
		}
		
		fields.put("Namesies", className);
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
	
	// Opens the original file and appends the beginning until the key to generate
	private static StringBuilder startGen(Generator gen)
	{
		Scanner original = openFile(gen.outputPath);
		StringBuilder out = new StringBuilder();
		
		while (original.hasNext())
		{
			String line = original.nextLine();
			out.append(line + "\n");
			
			if (line.contains("// EVERYTHING BELOW IS GENERATED ###"))
			{
				break;
			}
		}
		
		original.close();
		
		out.append("\n\t\t// List all of the classes we are loading\n");
		
		return out;
	}
	
	private static void addClass(Generator gen, StringBuilder out, StringBuilder classes, String name, String className, HashMap<String, String> fields)
	{
		createNamesies(name, className, gen.appendsies);
		
		// Mappity map
		if (gen.mappity)
		{
			out.append("\t\tmap.put(\"" + name + "\", new " + className + "());\n");	
		}

		List<String> interfaces = new ArrayList<>();
		String additionalMethods = getAdditionalMethods(gen, fields, interfaces);
		String constructor = getConstructor(fields, gen);

		String implementsString = getImplementsString(interfaces);
		
		String extraFields = "";
		if (fields.containsKey("Field"))
		{
			extraFields = fields.get("Field");
			fields.remove("Field");
		}
		
		// Write activation method if applicable
		additionalMethods = getActivationMethod(gen, className, fields) + additionalMethods;
		
		String classString = createClass(className, gen.superClass, implementsString, extraFields, constructor, additionalMethods);
		
		fields.remove("ClassName");
		fields.remove("Index");
		
		for (String s : fields.keySet())
		{
			Global.error("Unused field " + s + " for class " + className);
		}
		
		classes.append(classString);
	}
	
	private static void superGen(Generator gen)
	{
		StringBuilder out = startGen(gen);
		
		Scanner in = openFile(gen.inputPath);
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
			String className = writeClassName(name);
			
			// Read in all of the fields
			HashMap<String, String> fields = readFields(in, gen, className, index);
			
			addClass(gen, out, classes, name, className, fields);
			
			if (gen == Generator.ITEM_GEN)
			{
				addImageIndex(indexOut, index, name, className.toLowerCase());
			}
			
			index++;
		}
		
		switch (gen)
		{
			case ATTACK_GEN:
				out.append("\n\t\tfor (String s : map.keySet())\n\t\t{\n\t\t\tmoveNames.add(s);\n\t\t}\n");
				break;
			case ITEM_GEN:
				addTMs(out, classes, indexOut);
				printToFile(ITEM_TILES_PATH + "index.txt", indexOut);
			default:
				break;
		}
		
		if (gen.mappity)
		{
			out.append("\t}\n\n");	
		}
		
		out.append("\t/**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/\n"); // DON'T DO IT
		out.append(classes + "}");
		
		printToFile(gen.outputPath, out);
	}
	
	private static String getActivationMethod(Generator gen, String className, HashMap<String, String> fields)
	{
		if (!gen.activate)
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
		
		return MethodInfo.writeFunction(gen, activateInfo, "", className);
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
	
	private static void addTMs(StringBuilder out, StringBuilder classes, StringBuilder indexOut)
	{
		// Add the image index for each type (except for None)
		for (Type t : Type.values())
		{
			if (t == Type.NONE)
			{
				continue;
			}
			
			String name = t.getName() + "TM";
			addImageIndex(indexOut, TM_BASE_INDEX + t.getIndex(), name, name.toLowerCase());
		}
		
		Scanner in = openFile("tmList.txt");
		while (in.hasNext())
		{
			String attackName = in.nextLine().trim();
			String className = writeClassName(attackName);
			
			Attack attack = Attack.getAttack(Namesies.getValueOf(className, NamesiesType.ATTACK));
			
			String itemName = attackName + " TM";
			className += "TM";
			
			HashMap<String, String> fields = new HashMap<>();
			fields.put("ClassName", className);
			fields.put("Namesies", className);
			fields.put("Index", TM_BASE_INDEX + attack.getActualType().getIndex() + "");
			fields.put("Desc", attack.getDescription());
			fields.put("TM", attackName);
			
			addClass(Generator.ITEM_GEN, out, classes, itemName, className, fields);
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
	
	private static class MethodInfo
	{
		private String header;
		
		private String begin;
		private String body;
		private String end;
		
		private boolean tryParse;
		private boolean required;
		private boolean defaultBody;
		
		private List<String> addInterfaces;
		private List<Entry<String, String>> addMapFields;
		
		public MethodInfo(String header, String begin, String body, String end)
		{
			this.header = header;
			
			this.begin = begin;
			this.body = body;
			this.end = end;
			
			this.tryParse = false;
			this.required = true;
			this.defaultBody = false;

			this.addInterfaces = new ArrayList<>();
			this.addMapFields = new ArrayList<>();
		}
		
		public MethodInfo(Scanner in, boolean isInterfaceMethod)
		{
			this.header = null;
			
			this.begin = "";
			this.body = "";
			this.end = "";
			
			this.tryParse = false;
			this.required = true;
			this.defaultBody = false;

			this.addInterfaces = new ArrayList<>();
			this.addMapFields = new ArrayList<>();
			
			while (in.hasNext())
			{
				String line = in.nextLine().trim();
				
				if (line.equals("*"))
				{
					break;
				}
				
				Entry<String, String> pair = getFieldPair(in, line);
				
				String key = pair.getKey();
				String value = pair.getValue();
				
				switch (key)
				{
					case "Header":
						this.header = value;
						break;
					case "Try":
						tryParse = true;
						this.body = value;
						break;
					case "Default":
						this.defaultBody = true;
						this.body = value;
						break;
					case "Body":
						this.body = value;
						break;
					case "Begin":
						this.begin = value;
						break;
					case "End":
						this.end = value;
						break;
					case "AddMapField":
						Entry<String, String> fieldPair = getFieldPair(in, value);
						addMapFields.add(new SimpleEntry<>(fieldPair.getKey(), fieldPair.getValue()));
						break;
					case "AddInterface":
						addInterfaces.add(value);
						break;
					case "Optional":
						if (!value.equals("True"))
						{
							Global.error("True is the only valid optional value");
						}
						this.required = false;
						break;
					default:
						Global.error("Invalid field name " + key);
				}
			}
			
			if (this.header == null && (this.body.length() > 0 || this.begin.length() > 0 || this.end.length() > 0))
			{
				Global.error("Cannot have a body without a header.");
			}
			
			if (this.defaultBody && this.required)
			{
				Global.error("Can only have a default body if the field is optional");
			}
		}
		
		private static String writeFunction(Generator gen, MethodInfo method, String fieldValue, String className)
		{
			if (method.header == null)
			{
				return "";
			}
			
			String body;
			
			if (method.body.length() == 0)
			{
				body = fieldValue;
			}
			else
			{
				body = method.body;
			}
			
			if (method.tryParse)
			{
				try
				{
					Double.parseDouble(fieldValue);
				}
				catch (NumberFormatException e)
				{
					body = fieldValue;
				}
			}
			
			if (fieldValue.length() > 0 && method.defaultBody)
			{
				body = fieldValue;
			}
			
			body = method.begin + body + method.end;
			body = replaceBody(gen, body, className, fieldValue);
			
			return MethodInfo.writeFunction(method.header, body);
		}
		
		private static String writeFunction(String header, String body)
		{
			StringBuilder function = new StringBuilder();
			function.append("\n\t\tpublic " + header.trim() + "\n\t\t{\n");
			
			int tabs = 3;
			
			Scanner in = new Scanner(body);
			while (in.hasNextLine())
			{
				String line = in.nextLine().trim();
				
				if (line.contains("}") && !line.contains("{")) 
					tabs--;
				
				function.append(addTabs(tabs) + line + "\n");
				
				if (line.contains("{") && !line.contains("}")) 
					tabs++;
			}
			
			function.append("\t\t}\n");
			
			in.close();
			
			return function.toString();
		}
	}
	
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
		
		private String writeFailure(Generator gen, HashMap<String, String> fields)
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
					
					body = replaceBody(gen, body, className, pairValue);
					
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
	
	private static String replaceBody(Generator gen, String body, String className, String fieldValue)
	{
		body = body.replace("@ClassName", className);
		body = body.replace("@SuperClass", gen.superClass.toUpperCase());
		
		body = body.replace("{0}", fieldValue);
		body = body.replace("{00}", fieldValue.toUpperCase());
		
		String[] mcSplit = fieldValue.split(" ");
		for (int i = 0; i < mcSplit.length; i++)
		{
			body = body.replace(String.format("{%d}", i + 1), mcSplit[i]);
			body = body.replace(String.format("{%d%d}", i + 1, i + 1), mcSplit[i].toUpperCase());
			body = body.replace(String.format("{%d_}", i + 1), mcSplit[i].replaceAll("_", " "));
			
			String pattern = String.format("{%d-}", i + 1);
			if (body.contains(pattern))
			{
				if (i + 1 == 1)
				{
					Global.error("Don't use {1-}, instead use {0} (ClassName = " + className + ")");
				}
				
				String text = mcSplit[i];
				for (int j = i + 1; j < mcSplit.length; j++)
				{
					if (body.contains("{" + (j + 1)))
					{
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
	
	private static List<Entry<String, MethodInfo>> overrideMethods;
	private static HashMap<String, List<Entry<String, MethodInfo>>> interfaceMethods;
	
	private static void readFormat()
	{
		Scanner in = openFile("override.txt");
		
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
	
	private static List<Entry<String, String>> constructorKeys;
	private static List<Entry<String, String>> fieldKeys;
	private static Failure failureInfo;
	
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
		
		if (split.length > index)
		{
			String assignmentType = split[index++];
			
			switch (assignmentType)
			{
				case "List":
					assignment += ".add(" + value + ");";
					break;
				default:
					Global.error("Invalid parameter " + assignmentType);
			}
		}
		else
		{
			assignment += " = " + value + ";";
		}
		
		return assignment;
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
					value = Namesies.getNamesies(fieldValue, NamesiesType.valueOf(appendsies.toUpperCase()));
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
	
	private static String getConstructorValue(Entry<String, String> pair, HashMap<String, String> fields)
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
	
	private static String getAdditionalMethods(Generator gen, HashMap<String, String> fields, List<String> interfaces)
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
			moreFields = addMethodInfo(gen, methods, overrideMethods, fields, currentInterfaces, "");
			
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
				
				moreFields |= addMethodInfo(gen, methods, list, fields, nextInterfaces, interfaceName);
			}
			
			currentInterfaces = nextInterfaces;
			nextInterfaces = new ArrayList<>();
		}
		
		if (failureInfo != null)
		{
			methods.insert(0, failureInfo.writeFailure(gen, fields));
		}
		
		return methods.toString();
	}
	
	// Interface name should be empty if it is an override
	private static boolean addMethodInfo(Generator gen, StringBuilder methods, List<Entry<String, MethodInfo>> methodList, HashMap<String, String> fields, List<String> interfaces, String interfaceName)
	{
		boolean added = false;
		
		String className = fields.get("ClassName");
		
		for (Entry<String, MethodInfo> pair : methodList)
		{
			String fieldName = pair.getKey();
			String fieldValue = fields.get(fieldName);

			MethodInfo methodInfo = pair.getValue();
			
			if (fieldValue == null)
			{
				// Overrides are not required to contain the field value
				if (interfaceName.length() == 0)
				{
					continue;
				}
				
				if (methodInfo.required)
				{
					Global.error("Missing required field " + fieldName + " to implement interface " + interfaceName + " for class " + className);						
				}
				
				fieldValue = "";
			}
			
			String implementation = MethodInfo.writeFunction(gen, methodInfo, fieldValue, className);
			methods.append(implementation);
			
			for (String addInterface : methodInfo.addInterfaces)
			{
				interfaces.add(addInterface);
			}
			
			for (Entry<String, String> addField : methodInfo.addMapFields)
			{
				String fieldKey = addField.getKey();
				String addFieldValue = replaceBody(gen, addField.getValue(), className, fieldValue);
				
				String mapField = fields.get(fieldKey);
				if (mapField == null)
				{
					mapField = addFieldValue;
				}
				else if (fieldKey.equals("MoveType"))
				{
					mapField += ", " + addFieldValue;
				}
				else if (fieldKey.equals("Field"))
				{
					mapField += addFieldValue;
				}
				else
				{
					// Leave the map field as is -- including in the original fields overrides the override file
//					System.out.println("Map Field (ClassName = " + className + "): " + mapField);
				}
				
				fields.put(fieldKey, mapField);
			}
			
			fields.remove(fieldName);
			added = true;
		}
		
		return added;
	}
	
	private static Entry<String, String> getFieldPair(Scanner in, String line)
	{
		String[] split = line.split(":", 2);
		if (split.length != 2)
		{
			Global.error("Field key and value must be separated by a colon " + line);
		}
		
		String key = split[0].trim();
		String value = split[1].trim();
		
		if (value.length() == 0)
		{
			value = readFunction(in);
		}
		
		return new SimpleEntry<>(key, value);
	}
	
	private static String createClass(String className, String superClass, String interfaces, String extraFields, String constructor, String additional)
	{
		StringBuilder classBuilder = new StringBuilder();
		
		classBuilder.append("\n\tprivate static class " + className + " extends " + superClass + " " + interfaces + "\n\t{\n");
		classBuilder.append("\t\tprivate static final long serialVersionUID = 1L;\n");
		classBuilder.append(extraFields);
		classBuilder.append(constructor);
		classBuilder.append(additional);
		classBuilder.append("\t}\n");
		
		return classBuilder.toString();
	}
	
	private static String getConstructor(HashMap<String, String> fields, Generator gen)
	{
		// TODO: More hardcoded nonsense
		String category = "";
		boolean physicalContact = false;
		
		if (gen == Generator.ATTACK_GEN)
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
		
		if (gen == Generator.ATTACK_GEN)
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
	
	private static String addTabs(int tabs)
	{
		String s = "";
		
		for (int i = 0; i < tabs; i++) 
			s += "\t";
		
		return s;
	}
	
	public static void printToFile(String fileName, StringBuilder out)
	{
		try
		{
			new PrintStream(new File(fileName)).println(out);
		}
		catch (FileNotFoundException ex)
		{
			Global.error("STUPIDNESS");
		}
	}
	
	public static Scanner openFile(String fileName) 
	{
		Scanner in = null;
		try
		{
			in = new Scanner(new File(fileName));
		}
		catch (FileNotFoundException ex)
		{
			Global.error(fileName + " not found");
		}
		
		return in;
	}
	
	private static String readFunction(Scanner in)
	{
		StringBuilder function = new StringBuilder();
		
		int tabs = 2;
		
		while (in.hasNext()) 
		{
			String line = in.nextLine().trim();
			
			if (line.equals("###"))
			{
				break;
			}
			
			if (line.contains("}") && !line.contains("{")) 
				tabs--;
			
			function.append(addTabs(tabs) + line + "\n");
			
			if (line.contains("{") && !line.contains("}")) 
				tabs++;
		}
		
		return function.toString();
	}
	
	// Stuff that shouldn't necessarily be in this file and isn't really used but has nowhere else to live
	private static ArrayList<File> files;
	
	private static void plus()
	{
		files = new ArrayList<File>();
		addFiles(new File("C:\\Users\\leahf_000\\Documents\\Pokemon++\\src"));
		
		for (File f : files)
		{
			Pattern p = Pattern.compile("[^ +\t]\\ + [^ +=]");
			StringBuilder out = new StringBuilder();
			
			Scanner in = null;
			try
			{
				in = new Scanner(f);
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			
			while (in.hasNext())
			{
				String line = in.nextLine();
				
				if (!line.contains("Pattern.compile"))
				{
					Matcher m = p.matcher(line);
					
					while (m.find())
					{
						String group = m.group();
						String replace = m.group().replace("+", " + ");
						
						line = line.replace(group, replace);
						System.out.println(line);
					}
				}
				
				out.append(line + "\n");
			}
			
			out = new StringBuilder(out.substring(0, out.length() - 1));
			
			printToFile(f.getAbsolutePath(), out);
//			System.out.println(f.getAbsolutePath() + "\n" + out);
		}
	}
	
	private static void longestString()
	{
		files = new ArrayList<File>();
		addFiles(new File("C:\\Users\\!\\Documents\\GitHub\\Pokemon\\src"));
		
		String max = "";
		for (File f : files)
		{
			Pattern p = Pattern.compile("addMessage[^,]*;");
			
			Scanner in = null;
			try
			{
				in = new Scanner(f);
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			
			while (in.hasNext())
			{
				String line = in.nextLine();
				Matcher m = p.matcher(line);
				while (m.find())
				{
					String temp = line.substring(m.start() + 11, m.end() - 2);
					if (temp.length() > max.length())
						max = temp;
				}
			}
		}
		
		System.out.println(max.length() + " " + max);
	}
	
	private static void addFiles(File f)
	{
		File[] list = f.listFiles();

		for (int i = 0; i < list.length; i++)
		{
			if (list[i].isFile())
			{
				files.add(list[i]);
			}
			else
			{
				addFiles(list[i]);
			}
		}
	}
	
	// Used for editing pokemoninfo.txt
	private static void pokemonInfoStuff()
	{
		Scanner in = openFile("pokemoninfo.txt");
		PrintStream out = null;
		try {
			out = new PrintStream("out.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		while (in.hasNext())
		{
			out.println(in.nextLine()); // Num
			out.println(in.nextLine()); // Name
			out.println(in.nextLine()); // Base Stats
			out.println(in.nextLine()); // Base Exp
			out.println(in.nextLine()); // Growth Rate
			out.println(in.nextLine()); // Type1 Type2			
			readMoves(in, out); // Level Up Moves
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
	
	private static void readMoves(Scanner in, PrintStream out)
	{
		int numMoves = in.nextInt();
		out.println(numMoves); // Number of Moves 
		in.nextLine();
		for (int i = 0; i < numMoves; i++) out.println(in.nextLine()); // Each move and level
	}
	
	private static void readEvolution(Scanner in, PrintStream out)
	{
		String type = in.next();
		if (type.equals("Multi"))
		{
			int x = in.nextInt();
			out.println(type + " " + x);
			for (int i = 0; i < x; i++) readEvolution(in, out);
			return;
		}
		out.println(type + " " + in.nextLine());
	}
	
	private static void readHoldItems(Scanner in, PrintStream out)
	{
		int num = in.nextInt();
		out.println(num);
		in.nextLine();
		for (int i = 0; i < num; i++) out.println(in.nextLine());
	}
	
	private static void generatePokemonTileIndices()
	{
		StringBuilder out = new StringBuilder();
		for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++)
		{
			out.append(String.format("%03d.png %08x%n", i, i*4));
			out.append(String.format("%03d-back.png %08x%n", i, i*4 + 1));
			
			if (i >= 650)
			{
				out.append(String.format("%03d.png %08x%n", i, i*4 + 2));
				out.append(String.format("%03d-back.png %08x%n", i, i*4 + 3));
			}
			else
			{
				out.append(String.format("%03d-shiny.png %08x%n", i, i*4 + 2));
				out.append(String.format("%03d-shiny-back.png %08x%n", i, i*4 + 3));				
			}
		}
		
		out.append("pokeball.png 00011111\n");
		out.append("egg.png 00010000\n");
		
		printToFile(POKEMON_TILES_INDEX_PATH, out);
	}
	
	private static void generatePokemonPartyTileIndices()
	{
		StringBuilder out = new StringBuilder();
		for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++)
		{
			out.append(String.format("%03d-small.png %08x%n", i, i));
		}
		
		out.append("egg-small.png 00010000\n");
		
		printToFile(POKEMON_SMALL_TILES_INDEX_PATH, out);
	}
}
