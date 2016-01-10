package generator;

import java.io.PrintStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import main.Global;
import pokemon.PokemonInfo;
import util.FileIO;

public class StuffGen 
{
	private static final String POKEMON_TILES_INDEX_PATH = FileIO.makePath("rec", "tiles", "pokemonTiles") + "index.txt";
	private static final String POKEMON_SMALL_TILES_INDEX_PATH = FileIO.makePath("rec", "tiles", "partyTiles") + "index.txt";
	
	public StuffGen()
	{
		NamesiesGen namesiesGen = new NamesiesGen();
		new PokeGen(namesiesGen);
		namesiesGen.writeNamesies();
		
//		pokemonInfoStuff();
//		compareMoves();
//		DrawMetrics.FindMetrics.writeFontMetrics();
	}
	
	private static class MethodFormatter {
		
		private int tabs;
		private boolean inSwitch;
		private boolean inCases;
		
		public MethodFormatter(int tabs) {
			this.tabs = tabs;
			
			this.inSwitch = false;
			this.inCases = false;
		}
		
		public void appendLine(String line, StringBuilder method) {
			
			if (line.startsWith("switch (")) {
				inSwitch = true;
			}
			
			if (inSwitch) {
				boolean inBefore = inCases;
				inCases = line.startsWith("case ") || line.equals("default:");
				
				if (inBefore && !inCases) {
					tabs++;
				}
			}
			
			if (line.contains("}") && !line.contains("{")) {
				tabs--;
				inSwitch = false;
			}
			
			// Add the tabs
			for (int i = 0; i < tabs; i++)
				method.append("\t");
			
			// Actually write the line
			method.append(line + "\n");
			
			if (inSwitch && (line.equals("break;") || line.startsWith("return ") || line.equals("return;"))) {
				tabs--;
			}
			
			if (line.contains("{") && !line.contains("}")) {
				tabs++;
			}
		}
	}
	
	public static class MethodInfo
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
		
		public static String writeFunction(MethodInfo method, String fieldValue, String className, String superClass)
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
			body = replaceBody(body, fieldValue, className, superClass);
			
			return MethodInfo.writeFunction(method.header, body);
		}
		
		public static String writeFunction(String header, String body)
		{
			StringBuilder method = new StringBuilder();
			method.append("\n\t\tpublic " + header.trim() + "\n\t\t{\n");
			
			MethodFormatter formatter = new MethodFormatter(3);
			
			Scanner in = new Scanner(body);
			while (in.hasNextLine())
			{
				String line = in.nextLine().trim();
				formatter.appendLine(line, method);
			}
			
			method.append("\t\t}\n");
			
			in.close();
			
			return method.toString();
		}
	}
	
	// Opens the original file and appends the beginning until the key to generate
	public static StringBuilder startGen(final String fileName)
	{
		Scanner original = FileIO.openFile(fileName);
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
		return out;
	}
	
	// Interface name should be empty if it is an override
	public static boolean addMethodInfo(StringBuilder methods, List<Entry<String, MethodInfo>> methodList, Map<String, String> fields, List<String> interfaces, String interfaceName, String superClass)
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
			
			String implementation = MethodInfo.writeFunction(methodInfo, fieldValue, className, superClass);
			methods.append(implementation);
			
			for (String addInterface : methodInfo.addInterfaces)
			{
				interfaces.add(addInterface);
			}
			
			for (Entry<String, String> addField : methodInfo.addMapFields)
			{
				String fieldKey = addField.getKey();
				String addFieldValue = replaceBody(addField.getValue(), fieldValue, className, superClass);
				
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
//							System.out.println("Map Field (ClassName = " + className + "): " + mapField);
				}
				
				fields.put(fieldKey, mapField);
			}
			
			fields.remove(fieldName);
			added = true;
		}
		
		return added;
	}
	
	public static String replaceBody(String body, String fieldValue, String className, String superClass)
	{
		body = body.replace("@ClassName", className);
		body = body.replace("@SuperClass", superClass.toUpperCase());
		
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
	
	public static HashMap<String, String> readFields(Scanner in, String className)
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
			
			if (fields.containsKey(key)) {
				Global.error("Repeated field " + key + " for " + className);
			}
			
			fields.put(key, value);
		}
		
		return fields;
	}
	
	public static Entry<String, String> getFieldPair(Scanner in, String line)
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
	
	public static String createClass(String className, String superClass, String interfaces, String extraFields, String constructor, String additional)
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
	
	public static String readFunction(Scanner in)
	{
		StringBuilder method = new StringBuilder();
		MethodFormatter formatter = new MethodFormatter(2);
		
		while (in.hasNext()) 
		{
			String line = in.nextLine().trim();
			
			if (line.equals("###"))
			{
				break;
			}
			
			formatter.appendLine(line, method);
		}
		
		return method.toString();
	}
	
	// Used for editing pokemoninfo.txt
	private static void pokemonInfoStuff()
	{
		Scanner in = FileIO.openFile("pokemoninfo.txt");
		Scanner attacks = FileIO.openFile("newMoves.txt");
		
		PrintStream out = FileIO.openOutputFile("out.txt");
//		PrintStream temp = openOutputFile("oldMoves.txt");

		while (in.hasNext())
		{
			out.println(in.nextLine()); // Num
			out.println(in.nextLine()); // Name
			out.println(in.nextLine()); // Base Stats
			out.println(in.nextLine()); // Base Exp
			out.println(in.nextLine()); // Growth Rate
			out.println(in.nextLine()); // Type1 Type2			
			readMoves(in, out); // Level Up Moves
			readMoves(in, out); // TM Moves
			readMoves(in, out); // Egg Moves
			readMoves(attacks, out); // Move Tutor Moves
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
			attacks.nextLine();
		}
	}
	
	private static void readMoves(Scanner in, PrintStream out)
	{
		int numMoves = in.nextInt();
		out.println(numMoves); // Number of Moves 
		in.nextLine();
		
		for (int i = 0; i < numMoves; i++) 
		{
			out.println(in.nextLine()); // Each move and level
		}
	}
	
	private static void readEvolution(Scanner in, PrintStream out)
	{
		String type = in.next();
		if (type.equals("Multi"))
		{
			int x = in.nextInt();
			out.println(type + " " + x);
			for (int i = 0; i < x; i++) 
			{
				readEvolution(in, out);
			}
			
			return;
		}
		
		out.println(type + in.nextLine());
	}
	
	private static void readHoldItems(Scanner in, PrintStream out)
	{
		int num = in.nextInt();
		out.println(num);
		in.nextLine();
		for (int i = 0; i < num; i++) 
			out.println(in.nextLine());
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
		
		FileIO.writeToFile(POKEMON_TILES_INDEX_PATH, out);
	}
	
	private static void generatePokemonPartyTileIndices()
	{
		StringBuilder out = new StringBuilder();
		for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++)
		{
			out.append(String.format("%03d-small.png %08x%n", i, i));
		}
		
		out.append("egg-small.png 00010000\n");
		
		FileIO.writeToFile(POKEMON_SMALL_TILES_INDEX_PATH, out);
	}
}
