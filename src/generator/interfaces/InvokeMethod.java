package generator.interfaces;

import battle.effect.ApplyResult;
import generator.AccessModifier;
import generator.format.MethodWriter;
import util.GeneralUtils;
import util.string.StringAppender;
import util.string.StringUtils;

import java.util.Scanner;

abstract class InvokeMethod {
    private String methodName;

    protected InvokeMethod() {}

    protected abstract String getReturnType(InterfaceMethod interfaceMethod);
    protected abstract String getDefaultMethodName(InterfaceMethod interfaceMethod);
    protected abstract String getInnerLoop(InterfaceMethod interfaceMethod);
    protected abstract String getPostLoop(InterfaceMethod interfaceMethod);
    protected String getPreLoop() { return ""; }

    protected boolean includeEffectDeclaration() { return true; }

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
        String headerPrefix = (interfaceMethod.isPrivate() ? "private " : "") + "static ";
        String header = MethodWriter.createHeader(
                headerPrefix + this.getReturnType(interfaceMethod),
                this.getMethodName(interfaceMethod),
                this.getInvokeParameters(interfaceMethod)
        );

        String body = new StringAppender()
                .appendPostDelimiter("\n", interfaceMethod.getBegin())
                .appendPostDelimiter("\n", getPreLoop())
                .appendPostDelimiter("\n", getDeadsies(interfaceMethod))
                .appendPostDelimiter("\n", getMoldBreakerDeclaration(interfaceMethod))
                .appendLine(getDeclaration(interfaceMethod))
                .appendLine("for (InvokeEffect invokee : invokees) {")
                .appendLine("if (invokee instanceof " + interfaceMethod.getInterfaceName() + " && invokee.isActiveEffect()) {")
                .appendPostDelimiter("\n\n", getMoldBreaker(interfaceMethod))
                .appendLine(getFullInnerLoop(interfaceMethod))
                .appendPostDelimiter("\n", getDeadsies(interfaceMethod))
                .appendLine("}")
                .appendLine("}")
                .appendDelimiter("\n", getPostLoop(interfaceMethod))
                .toString();

        return new MethodWriter(header, body.trim(), AccessModifier.PACKAGE_PRIVATE).writeMethod();
    }

    private String getAdditionalInvokeParameters() {
        return "";
    }

    private String getInvokeParameters(final InterfaceMethod interfaceMethod) {
        StringAppender invokeParameters = new StringAppender();
        if (passInvokees(interfaceMethod)) {
            invokeParameters.appendDelimiter(", ", "List<? extends InvokeEffect> invokees");
        }

        invokeParameters.appendDelimiter(", ", this.getAdditionalInvokeParameters());
        invokeParameters.appendDelimiter(", ", interfaceMethod.getAdditionalInvokeParameters());
        invokeParameters.appendDelimiter(", ", interfaceMethod.getParameters());

        return invokeParameters.toString();
    }

    private String getDeclaration(final InterfaceMethod interfaceMethod) {
        if (passInvokees(interfaceMethod)) {
            return "";
        }

        return "\n" + interfaceMethod.getInvokeeDeclaration();
    }

    private boolean passInvokees(final InterfaceMethod interfaceMethod) {
        return StringUtils.isNullOrEmpty(interfaceMethod.getInvokeeDeclaration());
    }

    private String getMoldBreakerDeclaration(final InterfaceMethod interfaceMethod) {
        if (!interfaceMethod.isMoldBreakerNullCheck()) {
            return "";
        }

        return "\n" + interfaceMethod.getMoldBreaker();
    }

    private String getMoldBreaker(final InterfaceMethod interfaceMethod) {
        String moldBreaker = interfaceMethod.getMoldBreaker();
        if (StringUtils.isNullOrEmpty(moldBreaker)) {
            return "";
        }

        return "\n// If this is an ability that is being affected by mold breaker, we don't want to do anything with it\n" +
                "if (invokee instanceof Ability && !((Ability)invokee).unbreakableMold() && " +
                (interfaceMethod.isMoldBreakerNullCheck() || moldBreaker.equals("moldBreaker")
                        ? "moldBreaker != null && moldBreaker"
                        : moldBreaker) +
                ".breaksTheMold()) {\n" +
                "continue;\n" +
                "}";
    }

    private String getFullInnerLoop(final InterfaceMethod interfaceMethod) {
        String innerLoop = "";
        if (this.includeEffectDeclaration()) {
            innerLoop += interfaceMethod.getInterfaceName() + " effect = (" + interfaceMethod.getInterfaceName() + ")invokee;\n";
        }
        innerLoop += this.getIgnoreCondition(interfaceMethod);
        return innerLoop + this.getInnerLoop(interfaceMethod);
    }

    private String getIgnoreCondition(final InterfaceMethod interfaceMethod) {
        String ignoreCondition = interfaceMethod.getIgnoreCondition();
        if (StringUtils.isNullOrEmpty(ignoreCondition)) {
            return "";
        }

        return "if (" + ignoreCondition + ") {\n" +
                "continue;\n" +
                "}\n\n";
    }

    private String getDeadsies(final InterfaceMethod interfaceMethod) {
        final String postLoop = this.getPostLoop(interfaceMethod);
        final String returnStatement = StringUtils.isNullOrEmpty(postLoop) ? "return;" : postLoop;
        final String battleParameter = interfaceMethod.getBattleParameter();

        final StringAppender deadsies = new StringAppender();
        for (final String activePokemonParameter : interfaceMethod.getDeadsies()) {
            deadsies.appendDelimiter("\n", String.format(
                    "\nif (%s.isFainted(%s)) {\n%s\n}",
                    activePokemonParameter, battleParameter, returnStatement
            ));
        }

        return deadsies.toString();
    }

    static class VoidInvoke extends InvokeMethod {

        @Override
        protected String getReturnType(InterfaceMethod interfaceMethod) {
            return "void";
        }

        @Override
        protected String getDefaultMethodName(InterfaceMethod interfaceMethod) {
            return "invoke" + interfaceMethod.getInterfaceName();
        }

        @Override
        protected String getInnerLoop(InterfaceMethod interfaceMethod) {
            return "effect." + interfaceMethod.getMethodCall() + ";";
        }

        @Override
        protected String getPostLoop(InterfaceMethod interfaceMethod) {
            return "";
        }
    }

    static class ContainsInvoke extends InvokeMethod {

        @Override
        protected String getReturnType(InterfaceMethod interfaceMethod) {
            return "boolean";
        }

        @Override
        protected String getDefaultMethodName(InterfaceMethod interfaceMethod) {
            return "contains" + interfaceMethod.getInterfaceName();
        }

        @Override
        protected boolean includeEffectDeclaration() {
            return false;
        }

        @Override
        protected String getInnerLoop(InterfaceMethod interfaceMethod) {
            return "return true;";
        }

        @Override
        protected String getPostLoop(InterfaceMethod interfaceMethod) {
            return "return false;";
        }
    }

    static class CheckInvoke extends InvokeMethod {

        private final boolean check;

        CheckInvoke(final Scanner invokeInput) {
            super();
            this.check = GeneralUtils.parseBoolean(invokeInput.next());
        }

        @Override
        protected String getReturnType(InterfaceMethod interfaceMethod) {
            return "boolean";
        }

        @Override
        protected String getDefaultMethodName(InterfaceMethod interfaceMethod) {
            return "check" + interfaceMethod.getInterfaceName();
        }

        @Override
        protected String getInnerLoop(InterfaceMethod interfaceMethod) {
            return new StringAppender()
                    .appendFormat("if (%seffect.%s) {\n", check ? "" : "!", interfaceMethod.getMethodCall())
                    .appendLine(this.successfulCheck())
                    .append("}")
                    .toString();
        }

        protected String successfulCheck() {
            return "return true;";
        }

        @Override
        protected String getPostLoop(InterfaceMethod interfaceMethod) {
            return "return false;";
        }
    }

    static class CheckGetInvoke extends CheckInvoke {

        CheckGetInvoke(Scanner invokeInput) {
            super(invokeInput);
        }

        @Override
        protected String getReturnType(InterfaceMethod interfaceMethod) {
            return interfaceMethod.getInterfaceName();
        }

        @Override
        protected String getDefaultMethodName(InterfaceMethod interfaceMethod) {
            return "checkAndGet" + interfaceMethod.getInterfaceName();
        }

        @Override
        protected String successfulCheck() {
            return "return effect;";
        }

        @Override
        protected String getPostLoop(InterfaceMethod interfaceMethod) {
            return "return null;";
        }
    }

    static class CheckMessageInvoke extends CheckInvoke {

        private final String getMessageCall;

        CheckMessageInvoke(Scanner invokeInput) {
            super(invokeInput);

            this.getMessageCall = invokeInput.nextLine().trim();
        }

        @Override
        protected String successfulCheck() {
            return "Messages.add(effect." + this.getMessageCall + ");\n"
                    + super.successfulCheck();
        }
    }

    static class GetInvoke extends InvokeMethod {

        @Override
        protected String getReturnType(InterfaceMethod interfaceMethod) {
            return interfaceMethod.getReturnType();
        }

        @Override
        protected String getDefaultMethodName(InterfaceMethod interfaceMethod) {
            return "get" + interfaceMethod.getInterfaceName();
        }

        @Override
        protected String getInnerLoop(InterfaceMethod interfaceMethod) {
            return new StringAppender()
                    .appendLine(interfaceMethod.getReturnType() + " value = effect." + interfaceMethod.getMethodCall() + ";")
                    .appendLine("if (value != null) {")
                    .appendLine("return value;")
                    .append("}")
                    .toString();
        }

        @Override
        protected String getPostLoop(InterfaceMethod interfaceMethod) {
            String returnValue = "null";

            // Default return value for ApplyResult is a success object, not a null one
            String returnType = this.getReturnType(interfaceMethod);
            if (returnType.equals(ApplyResult.class.getSimpleName())) {
                returnValue = "ApplyResult.success()";
            }

            return "return " + returnValue + ";";
        }
    }

    static class UpdateInvoke extends InvokeMethod {

        @Override
        protected String getReturnType(InterfaceMethod interfaceMethod) {
            return interfaceMethod.getReturnType();
        }

        @Override
        protected String getDefaultMethodName(InterfaceMethod interfaceMethod) {
            return "update" + interfaceMethod.getInterfaceName();
        }

        @Override
        protected String getInnerLoop(InterfaceMethod interfaceMethod) {
            return interfaceMethod.getUpdateField() + " = effect." + interfaceMethod.getMethodCall() + ";";
        }

        @Override
        protected String getPostLoop(InterfaceMethod interfaceMethod) {
            return "return " + interfaceMethod.getUpdateField() + ";";
        }
    }

    static class MultiplyInvoke extends InvokeMethod {

        @Override
        protected String getReturnType(InterfaceMethod interfaceMethod) {
            return "double";
        }

        @Override
        protected String getDefaultMethodName(InterfaceMethod interfaceMethod) {
            return "getModifier";
        }

        @Override
        protected String getInnerLoop(InterfaceMethod interfaceMethod) {
            return "modifier *= effect." + interfaceMethod.getMethodCall() + ";";
        }

        @Override
        protected String getPostLoop(InterfaceMethod interfaceMethod) {
            return "return modifier;";
        }

        @Override
        protected String getPreLoop() {
            return "double modifier = 1;";
        }
    }

    static class AddInvoke extends InvokeMethod {

        @Override
        protected String getReturnType(InterfaceMethod interfaceMethod) {
            return "int";
        }

        @Override
        protected String getDefaultMethodName(InterfaceMethod interfaceMethod) {
            return "getModifier";
        }

        @Override
        protected String getInnerLoop(InterfaceMethod interfaceMethod) {
            return "modifier += effect." + interfaceMethod.getMethodCall() + ";";
        }

        @Override
        protected String getPostLoop(InterfaceMethod interfaceMethod) {
            return "return modifier;";
        }

        @Override
        protected String getPreLoop() {
            return "int modifier = 0;";
        }
    }
}
