package generator;

import battle.Battle;
import main.Global;
import util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

class InterfaceMethod {

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

        if ((this.returnType == null || this.methodName == null) && this.invokeMethod == null) {
            Global.error("Interface method and invoke method are both missing for interface " + this.interfaceName);
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
        if (StringUtils.isNullOrEmpty(this.returnType) || StringUtils.isNullOrEmpty(this.methodName)) {
            return StringUtils.empty();
        }

        final StringBuilder interfaceMethod = new StringBuilder();
        if (!StringUtils.isNullOrEmpty(this.comments)) {
            StringUtils.appendLine(interfaceMethod, "\n\t\t" + this.comments);
        }

        interfaceMethod.append(String.format("\t\t%s;\n", this.getHeader()));
        return interfaceMethod.toString();
    }

    private String getHeader() {
        return MethodInfo.createHeader(this.returnType, this.methodName, this.parameters);
    }

    private String getMethodCall() {
        return MethodInfo.createHeader(this.methodName, this.typelessParameters);
    }

    String writeInvokeMethod() {
        if (this.invokeMethod == null) {
            return StringUtils.empty();
        }

        return this.invokeMethod.writeInvokeMethod(this);
    }

    private enum InvokeType {
        VOID(VoidInvoke::new),
        CONTAINS(ContainsInvoke::new);

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
        private String methodName;

        protected abstract String getReturnType();
        protected abstract String getDefaultMethodName(final InterfaceMethod interfaceMethod);
        protected abstract void appendInnerLoop(StringBuilder body, InterfaceMethod interfaceMethod);
        protected abstract void appendPostLoop(StringBuilder body, InterfaceMethod interfaceMethod);

        private String getMethodName(final InterfaceMethod interfaceMethod) {
            if (StringUtils.isNullOrEmpty(this.methodName)) {
                return this.getDefaultMethodName(interfaceMethod);
            }

            return this.methodName;
        }

        String writeInvokeMethod(final InterfaceMethod interfaceMethod) {
            final String header = MethodInfo.createHeader(
                    "static " + this.getReturnType(),
                    this.getMethodName(interfaceMethod),
                    this.getInvokeParameters(interfaceMethod)
            );

            StringBuilder body = new StringBuilder();
            this.appendDeadsies(body, interfaceMethod);
            body.append(getDeclaration(interfaceMethod));
            StringUtils.appendLine(body, "\nfor (Object invokee : invokees) {");
            StringUtils.appendLine(body, "if (invokee instanceof " + interfaceMethod.interfaceName + ") {");
            StringUtils.appendLine(body, "if (Effect.isInactiveEffect(invokee)) {");
            StringUtils.appendLine(body, "continue;");
            StringUtils.appendLine(body, "}");
            this.appendMoldBreaker(body, interfaceMethod);
            StringUtils.appendLine(body, "");
            this.appendInnerLoop(body, interfaceMethod);
            this.appendDeadsies(body, interfaceMethod);
            StringUtils.appendLine(body, "}");
            StringUtils.appendLine(body, "}");
            StringUtils.appendLine(body, "");
            this.appendPostLoop(body, interfaceMethod);

            return new MethodInfo(header, body.toString().trim(), MethodInfo.AccessModifier.PACKAGE_PRIVATE).writeFunction();
        }

        private String getInvokeParameters(final InterfaceMethod interfaceMethod) {
            String invokeParameters = interfaceMethod.parameters;
            if (passInvokees(interfaceMethod)) {
                invokeParameters = "List<?> invokees" +
                        (StringUtils.isNullOrEmpty(invokeParameters) ? "" : ", ") +
                        invokeParameters;
            }

            return invokeParameters;
        }

        private String getDeclaration(final InterfaceMethod interfaceMethod) {
            if (passInvokees(interfaceMethod)) {
                return StringUtils.empty();
            }

            return "\n" + interfaceMethod.invokeeDeclaration;
        }

        private boolean passInvokees(final InterfaceMethod interfaceMethod) {
            return StringUtils.isNullOrEmpty(interfaceMethod.invokeeDeclaration);
        }

        private void appendMoldBreaker(final StringBuilder body, final InterfaceMethod interfaceMethod) {
            if (StringUtils.isNullOrEmpty(interfaceMethod.moldBreaker)) {
                return;
            }

            StringUtils.appendLine(body, "\n// If this is an ability that is being affected by mold breaker, we don't want to do anything with it");
            StringUtils.appendLine(body, "if (invokee instanceof Ability && " + interfaceMethod.moldBreaker + ".breaksTheMold()) {");
            StringUtils.appendLine(body, "continue;");
            StringUtils.appendLine(body, "}");
        }

        private void appendDeadsies(final StringBuilder body, final InterfaceMethod interfaceMethod) {
            final String battleParameter = interfaceMethod.battleParameter;
            for (final String activePokemonParameter : interfaceMethod.deadsies) {
                StringUtils.appendLine(body, "");
                StringUtils.appendLine(body, String.format("if (%s.isFainted(%s)) {", activePokemonParameter, battleParameter));
                StringUtils.appendLine(body, "return;");
                StringUtils.appendLine(body, "}");
            }
        }
    }

    private static class ContainsInvoke extends InvokeMethod {

        protected String getReturnType() {
            return "boolean";
        }

        protected String getDefaultMethodName(InterfaceMethod interfaceMethod) {
            return "contains" + interfaceMethod.interfaceName;
        }

        protected void appendInnerLoop(StringBuilder body, InterfaceMethod interfaceMethod) {
            StringUtils.appendLine(body, "return true;");
        }

        protected void appendPostLoop(StringBuilder body, InterfaceMethod interfaceMethod) {
            StringUtils.appendLine(body, "return false;");
        }
    }

    private static class VoidInvoke extends InvokeMethod {

        protected String getReturnType() {
            return "void";
        }

        protected String getDefaultMethodName(InterfaceMethod interfaceMethod) {
            return "invoke" + interfaceMethod.interfaceName;
        }

        protected void appendInnerLoop(StringBuilder body, InterfaceMethod interfaceMethod) {
            StringUtils.appendLine(body, interfaceMethod.interfaceName + " effect = (" + interfaceMethod.interfaceName + ")invokee;");
            StringUtils.appendLine(body, "effect." + interfaceMethod.getMethodCall() + ";");
        }

        protected void appendPostLoop(StringBuilder body, InterfaceMethod interfaceMethod) {}
    }
}
