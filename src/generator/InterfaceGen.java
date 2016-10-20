package generator;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import battle.Battle;
import battle.effect.generic.Effect;
import main.Global;
import pokemon.ActivePokemon;
import util.FileIO;

class InterfaceGen {
	
	private static final String INTERFACE_INPUT = "interfaces.txt";
	private static final String INTERFACE_PATH = FileIO.makePath("src", "battle", "effect") + "EffectInterfaces.java";
	
	InterfaceGen() {
		gen();
		System.out.println("Interfaces generated");
	}
	
	private static void gen() {
		final Scanner in = FileIO.openFile(INTERFACE_INPUT);
		final StringBuilder out = StuffGen.startGen(INTERFACE_PATH);
		
		// Go through the entire file
		while (in.hasNext()) {
			
			String line = in.nextLine().trim();
			
			// Ignore comments and white space at beginning of file
			if (line.length() == 0 || line.charAt(0) == '#') {
				continue;
			}
			
			final String interfaceName = line.replace(":", "");
			final Interface effectInterface = new Interface(in, interfaceName);
			
			out.append(effectInterface.writeInterface());
		}
		
		FileIO.writeToFile(INTERFACE_PATH, out);
	}
	
	private static class Interface {
		
		private static final String COMMENTS = "Comments";
		private static final String METHOD = "Method";
		
		private final String interfaceName;
		
		private String headerComments;
		private List<InterfaceMethod> methods;
		
		Interface(final Scanner in, final String interfaceName) {
			this.interfaceName = interfaceName;
			
			this.headerComments = "";
			this.methods = new LinkedList<>();
			
			readInterface(in);
		}
		
		private void readInterface(Scanner in) {
			while (in.hasNextLine()) {
				String line = in.nextLine().trim();
				
				if (line.equals("***")) {
					break;
				}
				
				final String[] split = line.split(":", 2);
				final String fieldKey = split[0].trim();
				
				switch (fieldKey) {
					case COMMENTS:
						// TODO: Right now class comment is restricted to a single line
						if (split.length == 1) {
							Global.error("Comments for " + this.interfaceName + " must be on a single line.");
						}
				
						final String fieldValue = split[1].trim();
						if (fieldValue.length() == 0) {
							Global.error("Comments for " + this.interfaceName + " is empty.");
						}
						
						this.headerComments = fieldValue;
						break;
					case METHOD:
						final Map<String, String> fields = StuffGen.readFields(in, this.interfaceName);
						this.methods.add(new InterfaceMethod(this.interfaceName, fields));
						break;
					default:
						Global.error("Invalid key name " + fieldKey + " for interface " + this.interfaceName);
						break;
				}
			}
		}
		
		// This is used when the user applies direct damage to an opponent, and has special effects associated
		interface ApplyDamageEffect
		{
			// b: The current battle
			// user: The user of that attack, the one who is probably implementing this effect
			// victim: The Pokemon that received the attack
			// damage: The amount of damage that was dealt to victim by the user
			void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, Integer damage);
			
			static void performApplyDamageEffect(List<Object> invokees, Battle b, ActivePokemon user, ActivePokemon victim, Integer damage) {
				
				for (Object invokee : invokees) {
					if (invokee instanceof ApplyDamageEffect) {
						if (Effect.isInactiveEffect(invokee)) {
							continue;
						}
						
						ApplyDamageEffect effect = (ApplyDamageEffect)invokee;
						effect.applyDamageEffect(b, user, victim, damage); 
					}
				}
			}
		}
		
		String writeInterface() {
			
			final String className = this.interfaceName;
			final String superClass = null;
			final String interfaces = null; // TODO: Will eventually not be null since some may extend other interfaces (unless that's superclass but whatever)
			
			final StringBuilder extraFields = new StringBuilder();
			for (InterfaceMethod method : this.methods) {
				if (method.comments.length() > 0) {
					extraFields.append(String.format("\t\t%s\n", method.comments));
				}
				
				extraFields.append(String.format("public %s(%s);\n", method.header, method.parameters));
			}
			
			final String constructor = null;
			final String additional = null; // TODO: Also temporary, need to implement the perform action shit
			
			// TODO: Add comments above
			return StuffGen.createClass(className, superClass, interfaces, extraFields.toString(), constructor, additional, true);
		}
	}
	
	private static class InterfaceMethod {
		
		private static final String COMMENTS = "Comments";
		private static final String HEADER = "Header";
		private static final String PARAMETERS = "Parameters";
		
		private final String methodName;
		
		private String header;
		private String parameters;
		private String comments;
		private List<InvokeMethod> invokeMethods;
		
		InterfaceMethod(final String methodName, Map<String, String> fields) {
			this.methodName = methodName;
			
			this.header = null;
			this.parameters = "";
			this.comments = "";
			this.invokeMethods = new LinkedList<>();
			
			this.readFields(fields);
		}
		
		private void readFields(Map<String, String> fields) {
			
			// Header is required
			if (!fields.containsKey(HEADER)) {
				Global.error("Interface method is missing a header...");
			}
			
			this.header = fields.get(HEADER);
			
			if (fields.containsKey(PARAMETERS)) {
				this.parameters = fields.get(PARAMETERS);
			}
			
			if (fields.containsKey(COMMENTS)) {
				this.comments = fields.get(COMMENTS);
			}
			
			for (InvokeMethod.InvokeType invokeType : InvokeMethod.InvokeType.values()) {
				final String key = invokeType.name;
				
				if (fields.containsKey(key)) {
					final String value = fields.get(key);
					final InvokeMethod invokeMethod = invokeType.getInvokeMethod(value);
					
					this.invokeMethods.add(invokeMethod);
				}
			}
		}
	}
	
	private static abstract class InvokeMethod {
		
		static enum InvokeType {
			VOID("Void", new GetInvokeMethod() {
				public InvokeMethod getInvokeMethod(final String value) {
					return new VoidInvoke(value);
				}});
			
			private final String name;
			private final GetInvokeMethod getInvokeMethod;
			
			InvokeType(final String name, final GetInvokeMethod getInvokeMethod) {
				this.name = name;
				this.getInvokeMethod = getInvokeMethod;
			}
			
			private interface GetInvokeMethod {
				InvokeMethod getInvokeMethod(final String value);
			}
			
			public InvokeMethod getInvokeMethod(String value) {
				return this.getInvokeMethod.getInvokeMethod(value);
			}
		}
		
		static class VoidInvoke extends InvokeMethod {
			
			private final boolean moldBreaker;
			private final boolean deadsies;
			
			VoidInvoke(final String value) {
				final Scanner in = new Scanner(value);
				
				this.moldBreaker = in.nextBoolean();
				this.deadsies = in.nextBoolean();
				
				in.close();
			}
		}
	}
}
