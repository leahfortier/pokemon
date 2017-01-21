package generator;

import main.Global;
import util.StringUtils;

import java.util.Scanner;

abstract class InvokeMethod {

    private String methodName;

    protected abstract String getReturnType(InterfaceMethod interfaceMethod);
    protected abstract String getDefaultMethodName(InterfaceMethod interfaceMethod);
    protected abstract String getInnerLoop(InterfaceMethod interfaceMethod);
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

        String body =
                StringUtils.addNewLine(getDeadsies(interfaceMethod)) +
                StringUtils.addNewLine(getMoldBreakerDeclaration(interfaceMethod)) +
                getDeclaration(interfaceMethod) + "\n" +
                "for (Object invokee : invokees) {\n" +
                "if (invokee instanceof " + interfaceMethod.getInterfaceName() +
                        " && !Effect.isInactiveEffect(invokee, " + interfaceMethod.getBattleParameter() + ")) {\n" +
                StringUtils.addNewLine(getMoldBreaker(interfaceMethod)) + "\n" +
                getInnerLoop(interfaceMethod) + "\n" +
                StringUtils.addNewLine(getDeadsies(interfaceMethod)) +
                "}\n" +
                "}\n" +
                StringUtils.preNewLine(getPostLoop(interfaceMethod));


        return new MethodInfo(header, body.trim(), AccessModifier.PACKAGE_PRIVATE).writeFunction();
    }

    protected String getAdditionalInvokeParameters() {
        return StringUtils.empty();
    }

    private String getInvokeParameters(final InterfaceMethod interfaceMethod) {
        StringBuilder invokeParameters = new StringBuilder();
        if (passInvokees(interfaceMethod)) {
            StringUtils.addCommaSeparatedValue(invokeParameters, "List<?> invokees");
        }

        StringUtils.addCommaSeparatedValue(invokeParameters, this.getAdditionalInvokeParameters());
        StringUtils.addCommaSeparatedValue(invokeParameters, interfaceMethod.getAdditionalInvokeParameters());
        StringUtils.addCommaSeparatedValue(invokeParameters, interfaceMethod.getParameters());

        return invokeParameters.toString();
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

    private String getMoldBreakerDeclaration(final InterfaceMethod interfaceMethod) {
        if (!interfaceMethod.isMoldBreakNullCheck()) {
            return StringUtils.empty();
        }

        return "\n" + interfaceMethod.getMoldBreaker();
    }

    private String getMoldBreaker(final InterfaceMethod interfaceMethod) {
        if (StringUtils.isNullOrEmpty(interfaceMethod.getMoldBreaker())) {
            return StringUtils.empty();
        }

        return "\n// If this is an ability that is being affected by mold breaker, we don't want to do anything with it\n" +
                "if (invokee instanceof Ability && !((Ability)invokee).unbreakableMold() && " +
                (interfaceMethod.isMoldBreakNullCheck()
                        ? "moldBreaker != null && moldBreaker"
                        : interfaceMethod.getMoldBreaker()) +
                ".breaksTheMold()) {\n" +
                "continue;\n" +
                "}";
    }

    private String getDeadsies(final InterfaceMethod interfaceMethod) {
        final String postLoop = this.getPostLoop(interfaceMethod);
        final String returnStatement = StringUtils.isNullOrEmpty(postLoop) ? "return;" : postLoop;
        final String battleParameter = interfaceMethod.getBattleParameter();

        final StringBuilder deadsies = new StringBuilder();
        for (final String activePokemonParameter : interfaceMethod.getDeadsies()) {
            if (deadsies.length() > 0) {
                deadsies.append("\n");
            }

            deadsies.append(String.format("\nif (%s.isFainted(%s)) {\n", activePokemonParameter, battleParameter))
                    .append(returnStatement)
                    .append("\n}");
        }

        return deadsies.toString();
    }

    static class VoidInvoke extends InvokeMethod {

        VoidInvoke(Scanner invokeInput) {
            super(invokeInput);
        }

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
            return interfaceMethod.getInterfaceName() + " effect = (" + interfaceMethod.getInterfaceName() + ")invokee;\n" +
                    "effect." + interfaceMethod.getMethodCall() + ";";
        }

        @Override
        protected String getPostLoop(InterfaceMethod interfaceMethod) {
            return StringUtils.empty();
        }
    }

    static class ContainsInvoke extends InvokeMethod {

        ContainsInvoke(Scanner invokeInput) {
            super(invokeInput);
        }

        @Override
        protected String getReturnType(InterfaceMethod interfaceMethod) {
            return "boolean";
        }

        @Override
        protected String getDefaultMethodName(InterfaceMethod interfaceMethod) {
            return "contains" + interfaceMethod.getInterfaceName();
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
            super(null);

            this.check = invokeInput.nextBoolean();
            if (invokeInput.hasNext()) {
                Global.error("Too much input for " + this.getClass().getSimpleName() + ": " + invokeInput);
            }
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
            return interfaceMethod.getInterfaceName() + " effect = (" + interfaceMethod.getInterfaceName() + ")invokee;\n" +
                    String.format("if (%seffect.%s) {\n", check ? "" : "!", interfaceMethod.getMethodCall()) +
                    this.successfulCheck() + "\n"
                    + "}";
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

    static class GetInvoke extends InvokeMethod {

        GetInvoke(Scanner invokeInput) {
            super(invokeInput);
        }

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
            return interfaceMethod.getInterfaceName() + " effect = (" + interfaceMethod.getInterfaceName() + ")invokee;\n" +
                    "return effect." + interfaceMethod.getMethodCall() + ";";
        }

        @Override
        protected String getPostLoop(InterfaceMethod interfaceMethod) {
            return "return null;";
        }
    }

    static class UpdateInvoke extends InvokeMethod {

        UpdateInvoke(Scanner invokeInput) {
            super(invokeInput);
        }

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
            return interfaceMethod.getInterfaceName() + " effect = (" + interfaceMethod.getInterfaceName() + ")invokee;\n" +
                    interfaceMethod.getUpdateField() + " = effect." + interfaceMethod.getMethodCall() + ";";
        }

        @Override
        protected String getPostLoop(InterfaceMethod interfaceMethod) {
            return "return " + interfaceMethod.getUpdateField() + ";";
        }
    }

    static class MultiplyInvoke extends InvokeMethod {

        MultiplyInvoke(Scanner invokeInput) {
            super(invokeInput);
        }

        @Override
        protected String getReturnType(InterfaceMethod interfaceMethod) {
            return "double";
        }

        @Override
        protected String getDefaultMethodName(InterfaceMethod interfaceMethod) {
            return "updateModifier";
        }

        @Override
        protected String getInnerLoop(InterfaceMethod interfaceMethod) {
            return interfaceMethod.getInterfaceName() + " effect = (" + interfaceMethod.getInterfaceName() + ")invokee;\n" +
                    "modifier *= effect." + interfaceMethod.getMethodCall() + ";";
        }

        @Override
        protected String getPostLoop(InterfaceMethod interfaceMethod) {
            return "return modifier;";
        }

        @Override
        protected String getAdditionalInvokeParameters() {
            return "double modifier";
        }
    }
}