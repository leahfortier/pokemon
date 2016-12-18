package generator;

import main.Global;
import util.StringUtils;

import java.util.Scanner;

abstract class InvokeMethod {

    private String methodName;

    protected abstract String getReturnType(InterfaceMethod interfaceMethod);
    protected abstract String getDefaultMethodName(final InterfaceMethod interfaceMethod);
    protected abstract void appendInnerLoop(StringBuilder body, InterfaceMethod interfaceMethod);
    protected abstract String getPostLoop(InterfaceMethod interfaceMethod);

    private InvokeMethod(final Scanner invokeInput) {
        if (invokeInput != null && invokeInput.hasNext()) {
            Global.error("Too much input for " + this.getClass().getSimpleName() + ": " + invokeInput);
        }
    }

    private String getMethodName(final InterfaceMethod interfaceMethod) {
        if (StringUtils.isNullOrEmpty(this.methodName)) {
            return this.getDefaultMethodName(interfaceMethod);
        }

        return this.methodName;
    }

    void setMethodName(final String methodName) {
        this.methodName = methodName;
    }

    String writeInvokeMethod(final InterfaceMethod interfaceMethod) {
        final String header = MethodInfo.createHeader(
                "static " + this.getReturnType(interfaceMethod),
                this.getMethodName(interfaceMethod),
                this.getInvokeParameters(interfaceMethod)
        );

        StringBuilder body = new StringBuilder();
        this.appendDeadsies(body, interfaceMethod);
        this.declareMoldBreaker(body, interfaceMethod);
        body.append(getDeclaration(interfaceMethod));
        StringUtils.appendLine(body, "\nfor (Object invokee : invokees) {");
        StringUtils.appendLine(body, "if (invokee instanceof " + interfaceMethod.getInterfaceName() +
                " && !Effect.isInactiveEffect(invokee, " + interfaceMethod.getBattleParameter() + ")) {");
        this.appendMoldBreaker(body, interfaceMethod);
        StringUtils.appendLine(body, "");
        this.appendInnerLoop(body, interfaceMethod);
        this.appendDeadsies(body, interfaceMethod);
        StringUtils.appendLine(body, "}");
        StringUtils.appendLine(body, "}");
        StringUtils.appendLine(body, "");
        StringUtils.appendLine(body, this.getPostLoop(interfaceMethod));

        return new MethodInfo(header, body.toString().trim(), AccessModifier.PACKAGE_PRIVATE).writeFunction();
    }

    protected String getAdditionalInvokeParameters() {
        return StringUtils.empty();
    }

    private String getInvokeParameters(final InterfaceMethod interfaceMethod) {
        String invokeParameters = interfaceMethod.getParameters();
        if (!StringUtils.isNullOrEmpty(interfaceMethod.getAdditionalInvokeParameters())) {
            invokeParameters = interfaceMethod.getAdditionalInvokeParameters() +
                    StringUtils.addLeadingComma(invokeParameters);
        }

        if (!StringUtils.isNullOrEmpty(this.getAdditionalInvokeParameters())) {
            invokeParameters = this.getAdditionalInvokeParameters() +
                    StringUtils.addLeadingComma(invokeParameters);
        }

        if (passInvokees(interfaceMethod)) {
            invokeParameters = "List<?> invokees" +
                    StringUtils.addLeadingComma(invokeParameters);
        }

        return invokeParameters;
    }

    private String getDeclaration(final InterfaceMethod interfaceMethod) {
        if (passInvokees(interfaceMethod)) {
            return StringUtils.empty();
        }

        return "\n" + interfaceMethod.getInvokeeDeclaration();
    }

    private boolean passInvokees(final InterfaceMethod interfaceMethod) {
        return StringUtils.isNullOrEmpty(interfaceMethod.getInvokeeDeclaration());
    }

    private void declareMoldBreaker(final StringBuilder body, final InterfaceMethod interfaceMethod) {
        if (!interfaceMethod.isMoldBreakNullCheck()) {
            return;
        }

        StringUtils.appendLine(body, interfaceMethod.getMoldBreaker());
    }

    private void appendMoldBreaker(final StringBuilder body, final InterfaceMethod interfaceMethod) {
        if (StringUtils.isNullOrEmpty(interfaceMethod.getMoldBreaker())) {
            return;
        }

        StringUtils.appendLine(body, "\n// If this is an ability that is being affected by mold breaker, we don't want to do anything with it");
        StringUtils.appendLine(body, "if (invokee instanceof Ability && " +
                        (interfaceMethod.isMoldBreakNullCheck()
                                ? "moldBreaker != null && moldBreaker"
                                : interfaceMethod.getMoldBreaker()) +
                        ".breaksTheMold()) {");
        StringUtils.appendLine(body, "continue;");
        StringUtils.appendLine(body, "}");
    }

    private void appendDeadsies(final StringBuilder body, final InterfaceMethod interfaceMethod) {
        final String postLoop = this.getPostLoop(interfaceMethod);
        final String returnStatement = StringUtils.isNullOrEmpty(postLoop) ? "return;" : postLoop;

        final String battleParameter = interfaceMethod.getBattleParameter();
        for (final String activePokemonParameter : interfaceMethod.getDeadsies()) {
            StringUtils.appendLine(body, "");
            StringUtils.appendLine(body, String.format("if (%s.isFainted(%s)) {", activePokemonParameter, battleParameter));
            StringUtils.appendLine(body, returnStatement);
            StringUtils.appendLine(body, "}");
        }
    }

    static class VoidInvoke extends InvokeMethod {

        VoidInvoke(Scanner invokeInput) {
            super(invokeInput);
        }

        protected String getReturnType(InterfaceMethod interfaceMethod) {
            return "void";
        }

        protected String getDefaultMethodName(InterfaceMethod interfaceMethod) {
            return "invoke" + interfaceMethod.getInterfaceName();
        }

        protected void appendInnerLoop(StringBuilder body, InterfaceMethod interfaceMethod) {
            StringUtils.appendLine(body, interfaceMethod.getInterfaceName() + " effect = (" + interfaceMethod.getInterfaceName() + ")invokee;");
            StringUtils.appendLine(body, "effect." + interfaceMethod.getMethodCall() + ";");
        }

        protected String getPostLoop(InterfaceMethod interfaceMethod) {
            return StringUtils.empty();
        }
    }

    static class ContainsInvoke extends InvokeMethod {

        ContainsInvoke(Scanner invokeInput) {
            super(invokeInput);
        }

        protected String getReturnType(InterfaceMethod interfaceMethod) {
            return "boolean";
        }

        protected String getDefaultMethodName(InterfaceMethod interfaceMethod) {
            return "contains" + interfaceMethod.getInterfaceName();
        }

        protected void appendInnerLoop(StringBuilder body, InterfaceMethod interfaceMethod) {
            StringUtils.appendLine(body, "return true;");
        }

        protected String getPostLoop(InterfaceMethod interfaceMethod) {
            return "return false;";
        }
    }

    static class CheckInvoke extends InvokeMethod {

        private final boolean check;

        CheckInvoke(final Scanner invokeInput) {
            super(null);

            this.check = invokeInput.nextBoolean();
            if (invokeInput.hasNext()) {
                Global.error("Too much input for " + this.getClass().getSimpleName() + ": " + invokeInput);
            }
        }

        protected String getReturnType(InterfaceMethod interfaceMethod) {
            return "boolean";
        }

        protected String getDefaultMethodName(InterfaceMethod interfaceMethod) {
            return "check" + interfaceMethod.getInterfaceName();
        }

        protected void appendInnerLoop(StringBuilder body, InterfaceMethod interfaceMethod) {
            StringUtils.appendLine(body, interfaceMethod.getInterfaceName() + " effect = (" + interfaceMethod.getInterfaceName() + ")invokee;");
            StringUtils.appendLine(body, String.format("if (%seffect.%s) {", check ? "" : "!", interfaceMethod.getMethodCall()));
            StringUtils.appendLine(body, this.successfulCheck());
            StringUtils.appendLine(body, "}");
        }

        protected String successfulCheck() {
            return "return true;";
        }

        protected String getPostLoop(InterfaceMethod interfaceMethod) {
            return "return false;";
        }
    }

    static class CheckGetInvoke extends CheckInvoke {

        CheckGetInvoke(Scanner invokeInput) {
            super(invokeInput);
        }

        protected String getReturnType(InterfaceMethod interfaceMethod) {
            return interfaceMethod.getInterfaceName();
        }

        protected String getDefaultMethodName(InterfaceMethod interfaceMethod) {
            return "checkAndGet" + interfaceMethod.getInterfaceName();
        }

        protected String successfulCheck() {
            return "return effect;";
        }

        protected String getPostLoop(InterfaceMethod interfaceMethod) {
            return "return null;";
        }
    }

    static class GetInvoke extends InvokeMethod {

        GetInvoke(Scanner invokeInput) {
            super(invokeInput);
        }

        protected String getReturnType(InterfaceMethod interfaceMethod) {
            return interfaceMethod.getReturnType();
        }

        protected String getDefaultMethodName(InterfaceMethod interfaceMethod) {
            return "get" + interfaceMethod.getInterfaceName();
        }

        protected void appendInnerLoop(StringBuilder body, InterfaceMethod interfaceMethod) {
            StringUtils.appendLine(body, interfaceMethod.getInterfaceName() + " effect = (" + interfaceMethod.getInterfaceName() + ")invokee;");
            StringUtils.appendLine(body, "return effect." + interfaceMethod.getMethodCall() + ";");
        }

        protected String getPostLoop(InterfaceMethod interfaceMethod) {
            return "return null;";
        }
    }

    static class UpdateInvoke extends InvokeMethod {

        UpdateInvoke(Scanner invokeInput) {
            super(invokeInput);
        }

        protected String getReturnType(InterfaceMethod interfaceMethod) {
            return interfaceMethod.getReturnType();
        }

        protected String getDefaultMethodName(InterfaceMethod interfaceMethod) {
            return "update" + interfaceMethod.getInterfaceName();
        }

        protected void appendInnerLoop(StringBuilder body, InterfaceMethod interfaceMethod) {
            StringUtils.appendLine(body, interfaceMethod.getInterfaceName() + " effect = (" + interfaceMethod.getInterfaceName() + ")invokee;");
            StringUtils.appendLine(body, interfaceMethod.getUpdateField() + " = effect." + interfaceMethod.getMethodCall() + ";");
        }

        protected String getPostLoop(InterfaceMethod interfaceMethod) {
            return "return " + interfaceMethod.getUpdateField() + ";";
        }
    }

    static class MultiplyInvoke extends InvokeMethod {

        MultiplyInvoke(Scanner invokeInput) {
            super(invokeInput);
        }

        protected String getReturnType(InterfaceMethod interfaceMethod) {
            return "double";
        }

        protected String getDefaultMethodName(InterfaceMethod interfaceMethod) {
            return "updateModifier";
        }

        protected void appendInnerLoop(StringBuilder body, InterfaceMethod interfaceMethod) {
            StringUtils.appendLine(body, interfaceMethod.getInterfaceName() + " effect = (" + interfaceMethod.getInterfaceName() + ")invokee;");
            StringUtils.appendLine(body, " modifier *= effect." + interfaceMethod.getMethodCall() + ";");
        }

        protected String getPostLoop(InterfaceMethod interfaceMethod) {
            return "return modifier;";
        }

        protected String getAdditionalInvokeParameters() {
            return "double modifier";
        }
    }
}