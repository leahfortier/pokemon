package generator;

import battle.Battle;
import main.Global;
import util.FileIO;
import util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

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
			
			final StringBuilder extraFields = new StringBuilder();
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
		private static final String INVOKE = "Invoke";
		private static final String INVOKE_NAME = "InvokeName";
		private static final String EFFECT_LIST = "EffectList";
		private static final String SET_INVOKEES = "SetInvokees";
		private static final String MOVE = "Move";
		private static final String MOLD_BREAKER = "MoldBreaker";
		private static final String DEADSIES = "Deadsies";

		private final String interfaceName;

		private String returnType;
		private String methodName;

		private String parameters;
		private String typelessParameters;
		private String battleParameter;

		private String invokeeDeclaration;

		private String moldBreaker;
		private List<String> deadsies;

		private String comments;
		private InvokeMethod invokeMethod;
		
		InterfaceMethod(final String interfaceName, Map<String, String> fields) {
			this.interfaceName = interfaceName;

			this.parameters = StringUtils.empty();
			this.typelessParameters = StringUtils.empty();
			this.deadsies = new ArrayList<>();

			this.readFields(fields);
		}
		
		private void readFields(Map<String, String> fields) {

			final String returnTypeAndName = getField(fields, HEADER);
			if (returnTypeAndName != null) {
				String[] split = returnTypeAndName.split(" ");
				if (split.length != 2) {
					Global.error("Header must have exactly two words -- return type and method name. " +
							returnTypeAndName + " is not valid.  Interface: " + this.interfaceName);
				}

				this.returnType = split[0];
				this.methodName = split[1];
			}

			final String returnType = getField(fields, RETURN_TYPE);
			if (returnType != null) {
				if (!StringUtils.isNullOrEmpty(this.returnType) || !StringUtils.isNullOrEmpty(this.methodName)) {
					Global.error("Cannot set the return type manually if it has already be set via the header field. " +
							"Header Return Type: " + this.returnType + ", Header Method Name: " + this.methodName +
							"New Return Type: " + returnType + ", Interface Name: " + this.interfaceName);
				}

				final String methodName = getField(fields, METHOD_NAME);
				if (methodName == null) {
					Global.error("Return type and method name must be specified together. " +
							"Return Type: " + returnType + ", Interface Name: " + this.interfaceName);
				}

				this.returnType = returnType;
				this.methodName = methodName;
			}

			if (this.returnType == null || this.methodName == null) {
				Global.error("Header is missing for interface " + this.interfaceName);
			}

			final String parameters = getField(fields, PARAMETERS);
			if (parameters != null) {
				this.parameters = parameters;

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

					if (parameterType.equals(Battle.class.getSimpleName())) {
						if (!StringUtils.isNullOrEmpty(this.battleParameter)) {
							Global.error("Can only have one battle parameter.  Interface: " + this.interfaceName);
						}

						this.battleParameter = parameterName;
					}
				}
			}

			final String comments = getField(fields, COMMENTS);
			if (comments != null) {
				this.comments = comments;
			}

			final String invoke = getField(fields, INVOKE);
			if (invoke != null) {
				this.invokeMethod = InvokeType.valueOf(invoke.toUpperCase()).getInvokeMethod();
			}

			final String invokeName = getField(fields, INVOKE_NAME);
			if (invokeName != null) {
				if (this.invokeMethod == null) {
					Global.error("Must specify type of invoke method if you want to name it.  Interface: " + this.interfaceName);
				}

				this.invokeMethod.methodName = invokeName;
			}

			final String effectListParameter = getField(fields, EFFECT_LIST);
			if (effectListParameter != null) {
				this.invokeeDeclaration = String.format("List<Object> invokees = %s.getEffectsList(%s);",
						this.battleParameter, effectListParameter);
			}

			final String setInvokees = getField(fields, SET_INVOKEES);
			if (setInvokees != null) {
				if (!StringUtils.isNullOrEmpty(this.invokeeDeclaration)) {
					Global.error("Can not define multiple ways to set the effects list. " +
							"Interface: " + this.interfaceName);
				}

				this.invokeeDeclaration = setInvokees + "\n";
			}

			// TODO: Eventually would just like to remove the invokee loop for this case and just operate directly on the attack
			final String moveInvoke = getField(fields, MOVE);
			if (moveInvoke != null) {
				if (!StringUtils.isNullOrEmpty(this.invokeeDeclaration)) {
					Global.error("Can not define multiple ways to set the effects list. " +
							"Interface: " + this.interfaceName);
				}

				this.invokeeDeclaration = String.format("List<Object> invokees = " +
						"Collections.singletonList(%s.getAttack());", moveInvoke);
			}

			final String moldBreaker = getField(fields, MOLD_BREAKER);
			if (moldBreaker != null) {
				this.moldBreaker = moldBreaker;
			}

			final String allDeadsies = getField(fields, DEADSIES);
			if (allDeadsies != null) {
				Scanner in = new Scanner(allDeadsies);
				while (in.hasNext()) {
					this.deadsies.add(in.next());
				}
			}

			for (final Entry<String, String> field : fields.entrySet()) {
				Global.error("Unused field " + field.getKey() + ": " + field.getValue() +
						" for interface " + this.interfaceName);
			}
		}

		private static String getField(final Map<String, String> fields, final String key) {
			if (fields.containsKey(key)) {
				final String value = fields.get(key).trim();
				fields.remove(key);
				return value;
			}

			return null;
		}

		String writeInterfaceMethod() {
			final StringBuilder interfaceMethod = new StringBuilder();
			if (!StringUtils.isNullOrEmpty(this.comments)) {
				StringUtils.appendLine(interfaceMethod, "\n\t\t" + this.comments);
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
			if (this.invokeMethod == null) {
				return StringUtils.empty();
			}

			return this.invokeMethod.writeInvokeMethod(this);
		}
	}

	private enum InvokeType {
		VOID(VoidInvoke::new);

		private final GetInvokeMethod getInvokeMethod;

		InvokeType(final GetInvokeMethod getInvokeMethod) {
			this.getInvokeMethod = getInvokeMethod;
		}

		private interface GetInvokeMethod {
			InvokeMethod getInvokeMethod();
		}

		public InvokeMethod getInvokeMethod() {
			return this.getInvokeMethod.getInvokeMethod();
		}
	}

	private static abstract class InvokeMethod {
		protected String methodName;

		public abstract String writeInvokeMethod(final InterfaceMethod interfaceMethod);
	}

	private static class VoidInvoke extends InvokeMethod {

		private String getMethodName(final InterfaceMethod interfaceMethod) {
			if (StringUtils.isNullOrEmpty(this.methodName)) {
				return "invoke" + interfaceMethod.interfaceName;
			}

			return this.methodName;
		}

		private String getInvokeParameters(final InterfaceMethod interfaceMethod) {
			if (passInvokees(interfaceMethod)) {
				return "List<?> invokees, " + interfaceMethod.parameters;
			}

			return interfaceMethod.parameters;
		}

		private static String writeDeclaration(final InterfaceMethod interfaceMethod) {
			if (passInvokees(interfaceMethod)) {
				return StringUtils.empty();
			}

			return "\n" + interfaceMethod.invokeeDeclaration;
		}

		private static boolean passInvokees(final InterfaceMethod interfaceMethod) {
			return StringUtils.isNullOrEmpty(interfaceMethod.invokeeDeclaration);
		}

		public String writeInvokeMethod(final InterfaceMethod interfaceMethod) {
			final String methodName = this.getMethodName(interfaceMethod);
			final String header = MethodInfo.createHeader("static void", methodName, this.getInvokeParameters(interfaceMethod));

			StringBuilder body = new StringBuilder();
			body.append(checkDeadsies(interfaceMethod));
			body.append(writeDeclaration(interfaceMethod));
			StringUtils.appendLine(body, "\nfor (Object invokee : invokees) {");
			StringUtils.appendLine(body, "if (invokee instanceof " + interfaceMethod.interfaceName + ") {");
			StringUtils.appendLine(body, "if (Effect.isInactiveEffect(invokee)) {");
			StringUtils.appendLine(body, "continue;");
			StringUtils.appendLine(body, "}");
			body.append(checkMoldBreaker(interfaceMethod));
			StringUtils.appendLine(body, "");
			StringUtils.appendLine(body, interfaceMethod.interfaceName + " effect = (" + interfaceMethod.interfaceName + ")invokee;");
			StringUtils.appendLine(body, "effect." + interfaceMethod.getMethodCall() + ";");
			body.append(checkDeadsies(interfaceMethod));
			StringUtils.appendLine(body, "}");
			StringUtils.appendLine(body, "}");

			return new MethodInfo(header, body.toString().trim(), MethodInfo.AccessModifier.PACKAGE_PRIVATE).writeFunction();
		}

		private static String checkMoldBreaker(final InterfaceMethod interfaceMethod) {
			if (StringUtils.isNullOrEmpty(interfaceMethod.moldBreaker)) {
				return StringUtils.empty();
			}

			final StringBuilder moldBreakerCheck = new StringBuilder("\n");
			StringUtils.appendLine(moldBreakerCheck, "// If this is an ability that is being affected by mold breaker, we don't want to do anything with it");
			StringUtils.appendLine(moldBreakerCheck, "if (invokee instanceof Ability && " + interfaceMethod.moldBreaker + ".breaksTheMold()) {");
			StringUtils.appendLine(moldBreakerCheck, "continue;");
			StringUtils.appendLine(moldBreakerCheck, "}");

			return moldBreakerCheck.toString();
		}

		private static String checkDeadsies(final InterfaceMethod interfaceMethod) {
			final String battleParameter = interfaceMethod.battleParameter;
			StringBuilder deadsiesCheck = new StringBuilder();
			for (final String activePokemonParameter : interfaceMethod.deadsies) {
				StringUtils.appendLine(deadsiesCheck, "");
				StringUtils.appendLine(deadsiesCheck, String.format("if (%s.isFainted(%s)) {", activePokemonParameter, battleParameter));
				StringUtils.appendLine(deadsiesCheck, "return;");
				StringUtils.appendLine(deadsiesCheck, "}");
			}

			return deadsiesCheck.toString();
		}
	}
}
