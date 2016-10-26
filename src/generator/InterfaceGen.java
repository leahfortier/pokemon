package generator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import battle.Battle;
import battle.effect.generic.Effect;
import battle.effect.generic.EffectInterfaces;
import main.Global;
import pokemon.ActivePokemon;
import util.FileIO;
import util.StringUtils;

class InterfaceGen {
	
	private static final String INTERFACE_INPUT = "interfaces.txt";
	private static final String INTERFACE_PATH = FileIO.makeFolderPath("src", "battle", "effect", "generic") + "EffectInterfaces.java";
	
	InterfaceGen() {
		gen();
	}
	
	private static void gen() {
		final Scanner in = FileIO.openFile(INTERFACE_INPUT);
		final StringBuilder out = StuffGen.startGen(INTERFACE_PATH);
		
		// Go through the entire file
		while (in.hasNext()) {
			
			String line = in.nextLine().trim();
			
			// Ignore comments and white space at beginning of file
			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}
			
			final String interfaceName = line.replace(":", "");
			final Interface effectInterface = new Interface(in, interfaceName);
			
			out.append(effectInterface.writeInterface());
		}

		out.append("}");
		
		FileIO.overwriteFile(INTERFACE_PATH, out);
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
						if (fieldValue.isEmpty()) {
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
		
		String writeInterface() {
			final String superClass = null;
			final String interfaces = null; // TODO: Will eventually not be null since some may extend other interfaces (unless that's superclass but whatever)
			
			final StringBuilder extraFields = new StringBuilder("\n");
			for (InterfaceMethod method : this.methods) {
				extraFields.append(method.writeInterfaceMethod());
			}
			
			final String constructor = null;
			final StringBuilder additional = new StringBuilder();
			for (InterfaceMethod method : this.methods) {
				additional.append(method.writeInvokeMethod());
			}

			return StuffGen.createClass(
					this.headerComments,
					this.interfaceName,
					superClass,
					interfaces,
					extraFields.toString(),
					constructor,
					additional.toString(),
					true);
		}
	}
	
	private static class InterfaceMethod {
		
		private static final String COMMENTS = "Comments";
		private static final String RETURN_TYPE = "ReturnType";
		private static final String METHOD_NAME = "MethodName";
		private static final String HEADER = "Header";
		private static final String PARAMETERS = "Parameters";

		private final String interfaceName;

		private String returnType;
		private String methodName;

		private String parameters;
		private String typelessParameters;
		private Map<String, List<String>> parameterMap;

		private String comments;
		private InvokeMethod invokeMethod;
		
		InterfaceMethod(final String interfaceName, Map<String, String> fields) {
			this.interfaceName = interfaceName;

			this.parameters = StringUtils.empty();
			this.typelessParameters = StringUtils.empty();
			this.parameterMap = new TreeMap<>();

			this.readFields(fields);
		}
		
		private void readFields(Map<String, String> fields) {
			
			// Header is required
			if (fields.containsKey(HEADER)) {
				final String returnTypeAndName = fields.get(HEADER);
				String[] split = returnTypeAndName.split(" ");
				if (split.length != 2) {
					Global.error("Header must have exactly two words -- return type and method name. " +
							returnTypeAndName + " is not valid.  Interface: " + this.interfaceName);
				}

				this.returnType = split[0];
				this.methodName = split[1];
			}

			if (fields.containsKey(RETURN_TYPE)) {
				final String returnType = fields.get(RETURN_TYPE);
				if (!StringUtils.isNullOrEmpty(this.returnType) || !StringUtils.isNullOrEmpty(this.methodName)) {
					Global.error("Cannot set the return type manually if it has already be set via the header field. " +
							"Header Return Type: " + this.returnType + ", Header Method Name: " + this.methodName +
							"New Return Type: " + returnType + ", Interface Name: " + this.interfaceName);
				}

				if (!fields.containsKey(METHOD_NAME)) {
					Global.error("Return type and method name must be specified together. " +
							"Return Type: " + returnType + ", Interface Name: " + this.interfaceName);
				}

				this.returnType = returnType;
				this.methodName = fields.get(METHOD_NAME);
			}
			
			if (fields.containsKey(PARAMETERS)) {
				this.parameters = fields.get(PARAMETERS);

				final String[] split = this.parameters.split(",");
				for (final String typedParameter : split) {
					final String[] typeSplit = typedParameter.trim().split(" ");
					if (typeSplit.length != 2) {
						Global.error("Should be exactly one space between split parameters. " +
								"Parameters: " + this.parameters + ", Interface Name: " + this.interfaceName);
					}

					final String parameterType = typeSplit[0];
					final String parameterName = typeSplit[1];

					if (!this.typelessParameters.isEmpty()) {
						this.typelessParameters += ", ";
					}

					this.typelessParameters += parameterName;

					if (!this.parameterMap.containsKey(parameterType)) {
						this.parameterMap.put(parameterType, new ArrayList<>());
					}

					this.parameterMap.get(parameterType).add(parameterName);
				}
			}
			
			if (fields.containsKey(COMMENTS)) {
				this.comments = fields.get(COMMENTS).trim();
			}
			
			for (InvokeType invokeType : InvokeType.values()) {
				final String key = invokeType.name;
				
				if (fields.containsKey(key)) {
					if (this.invokeMethod != null) {
						Global.error("Cannot set multiple invoke methods for a single interface method. " +
								"Interface: " + this.interfaceName + ", Method: " + this.getHeader());
					}

					final String value = fields.get(key);
					this.invokeMethod = invokeType.getInvokeMethod(value);
				}
			}
		}

		String writeInterfaceMethod() {
			final StringBuilder interfaceMethod = new StringBuilder();
			if (!StringUtils.isNullOrEmpty(this.comments)) {
				StringUtils.appendLine(interfaceMethod, "\t\t" + this.comments);
			}

			interfaceMethod.append(String.format("\t\t%s;\n", this.getHeader()));
			return interfaceMethod.toString();
		}

		String getHeader() {
			return MethodInfo.createHeader(this.returnType, this.methodName, this.parameters);
		}

		String getMethodCall() {
			return MethodInfo.createHeader(this.methodName, this.typelessParameters);
		}

		String writeInvokeMethod() {
			return this.invokeMethod.writeInvokeMethod(this);
		}
	}

	private enum InvokeType {
		VOID("Void", VoidInvoke::new);

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

	private static abstract class InvokeMethod {
		public abstract String writeInvokeMethod(final InterfaceMethod interfaceMethod);
	}

	private static class VoidInvoke extends InvokeMethod {
		private final boolean moldBreaker;
		private final boolean deadsies;

		VoidInvoke(final String value) {
			final Scanner in = new Scanner(value);

			this.moldBreaker = in.nextBoolean();
			this.deadsies = in.nextBoolean();

			in.close();
		}

		public String writeInvokeMethod(final InterfaceMethod interfaceMethod) {
			final String methodName = "invoke" + interfaceMethod.interfaceName;
			final String header = MethodInfo.createHeader("static void", methodName, "List<Object> invokees, " + interfaceMethod.parameters);

			StringBuilder body = new StringBuilder();
			body.append(this.checkDeadsies(interfaceMethod));
			StringUtils.appendLine(body, "\nfor (Object invokee : invokees) {");
			StringUtils.appendLine(body, "if (invokee instanceof " + interfaceMethod.interfaceName + ") {");
			StringUtils.appendLine(body, "if (Effect.isInactiveEffect(invokee)) {");
			StringUtils.appendLine(body, "continue;");
			StringUtils.appendLine(body, "}");
			StringUtils.appendLine(body, "");
			StringUtils.appendLine(body, interfaceMethod.interfaceName + " effect = (" + interfaceMethod.interfaceName + ")invokee;");
			StringUtils.appendLine(body, "effect." + interfaceMethod.getMethodCall() + ";");
			body.append(this.checkDeadsies(interfaceMethod));
			StringUtils.appendLine(body, "}");
			StringUtils.appendLine(body, "}");

			return new MethodInfo(header, body.toString(), MethodInfo.AccessModifier.PACKAGE_PRIVATE).writeFunction();
		}

		private String checkDeadsies(final InterfaceMethod interfaceMethod) {
			if (!this.deadsies) {
				return StringUtils.empty();
			}

			final List<String> battleParameters = interfaceMethod.parameterMap.get(Battle.class.getSimpleName());
			if (battleParameters == null || battleParameters.size() != 1) {
				Global.error("Deadsies should only be true when there is exactly one input battle to check." +
						"Interface Name: " + interfaceMethod);
				return StringUtils.empty();
			}

			final String battleParameter = battleParameters.get(0);

			final List<String> activePokemonParameters = interfaceMethod.parameterMap.get(ActivePokemon.class.getSimpleName());
			if (activePokemonParameters == null) {
				return StringUtils.empty();
			}

			StringBuilder body = new StringBuilder();
			for (final String activePokemonParameter : activePokemonParameters) {
				StringUtils.appendLine(body, "");
				StringUtils.appendLine(body, String.format("if (%s.isFainted(%s)) {", activePokemonParameter, battleParameter));
				StringUtils.appendLine(body, "return;");
				StringUtils.appendLine(body, "}");
			}

			return body.toString();
		}

		public static void performApplyDamageEffect(List<Object> invokees, Battle b, ActivePokemon user, ActivePokemon victim, Integer damage) {

			for (Object invokee : invokees) {
				if (invokee instanceof EffectInterfaces.ApplyDamageEffect) {
					if (Effect.isInactiveEffect(invokee)) {
						continue;
					}

					EffectInterfaces.ApplyDamageEffect effect = (EffectInterfaces.ApplyDamageEffect) invokee;
					effect.applyDamageEffect(b, user, victim, damage);
				}
			}
		}
	}
}
