package generator.interfaces;

import battle.Battle;
import generator.AccessModifier;
import generator.ClassFields;
import generator.format.MethodInfo;
import main.Global;
import pattern.MatchType;
import util.StringAppender;
import util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class InterfaceMethod {
    private static final String COMMENTS = "Comments";
    private static final String RETURN_TYPE = "ReturnType";
    private static final String METHOD_NAME = "MethodName";
    private static final String HEADER = "Header";
    private static final String PARAMETERS = "Parameters";
    private static final String INVOKE_PARAMETERS = "InvokeParameters";
    private static final String INVOKE = "Invoke";
    private static final String INVOKE_NAME = "InvokeName";
    private static final String EFFECT_LIST = "EffectList";
    private static final String NON_BATTLE_EFFECTS = "NonBattleEffects";
    private static final String EFFECT_PRIORITY = "EffectPriority";
    private static final String INVOKE_ATTACK = "InvokeAttack";
    private static final String STAT_INVOKE_ATTACK = "StatInvokeAttack";
    private static final String SET_INVOKEES = "SetInvokees";
    private static final String MOVE = "Move";
    private static final String UPDATE = "Update";
    private static final String MOLD_BREAKER = "MoldBreaker";
    private static final String MOLD_BREAKER_NULL_CHECK = "MoldBreakerNullCheck";
    private static final String DEFAULT = "Default";
    private static final String DEADSIES = "Deadsies";
    private static final String OVERRIDE = "Override";
    private static final String BEGIN = "Begin";

    private static final Pattern HEADER_PATTERN = Pattern.compile(
            "^" + MatchType.VARIABLE_TYPE.group() + // Group 1: return type
                    " " + MatchType.WORD.group() +  // Group 2: method name
                    "\\((.*)\\)$"                   // Group 3: method parameters
    );

    private final String interfaceName;

    private String returnType;
    private String methodName;

    private String parameters;
    private String typelessParameters;
    private String battleParameter;

    private String additionalInvokeParameters;
    private String invokeeDeclaration;

    private String updateField;

    private String moldBreaker;
    private boolean moldBreakNullCheck;

    private String defaultMethod;

    private List<String> deadsies;

    private boolean isOverride;

    private String begin;

    private String comments;
    private InvokeMethod invokeMethod;

    InterfaceMethod(ClassFields fields) {
        this.interfaceName = fields.getName();

        this.parameters = StringUtils.empty();
        this.typelessParameters = StringUtils.empty();
        this.deadsies = new ArrayList<>();

        this.readFields(fields);
    }

    private void readFields(ClassFields fields) {
        final String header = fields.getAndRemoveTrimmed(HEADER);
        if (header != null) {
            Matcher matcher = HEADER_PATTERN.matcher(header);
            if (!matcher.matches()) {
                Global.error("Header not properly formatted for " + this.interfaceName + ", Header: " + header);
            }

            this.returnType = matcher.group(1);
            this.methodName = matcher.group(2);
            this.parameters = matcher.group(3);
        }

        final String returnType = fields.getAndRemoveTrimmed(RETURN_TYPE);
        if (returnType != null) {
            if (!StringUtils.isNullOrEmpty(this.returnType) || !StringUtils.isNullOrEmpty(this.methodName)) {
                Global.error("Cannot set the return type manually if it has already be set via the header field. " +
                                     "Header Return Type: " + this.returnType + ", Header Method Name: " + this.methodName +
                                     "New Return Type: " + returnType + ", Interface Name: " + this.interfaceName);
            }

            final String methodName = fields.getAndRemoveTrimmed(METHOD_NAME);
            if (methodName == null) {
                Global.error("Return type and method name must be specified together. " +
                                     "Return Type: " + returnType + ", Interface Name: " + this.interfaceName);
            }

            this.returnType = returnType;
            this.methodName = methodName;
        }

        final String parameters = fields.getAndRemoveTrimmed(PARAMETERS);
        if (parameters != null) {
            this.parameters = parameters;
        }

        final String invokeParameters = fields.getAndRemoveTrimmed(INVOKE_PARAMETERS);
        if (invokeParameters != null) {
            this.additionalInvokeParameters = invokeParameters;
            this.setParameters(this.additionalInvokeParameters, false);
        }

        this.setParameters(this.parameters, true);

        final String comments = fields.getAndRemoveTrimmed(COMMENTS);
        if (comments != null) {
            this.comments = comments;
        }

        final String invoke = fields.getAndRemoveTrimmed(INVOKE);
        if (invoke != null) {
            Scanner in = new Scanner(invoke);
            this.invokeMethod = InvokeType.getInvokeMethod(in.next(), in);
        }

        final String invokeName = fields.getAndRemoveTrimmed(INVOKE_NAME);
        if (invokeName != null) {
            if (this.invokeMethod == null) {
                Global.error("Must specify type of invoke method if you want to name it.  Interface: " + this.interfaceName);
            }

            this.invokeMethod.setMethodName(invokeName);
        }

        // TODO: Clean all this shit up and put all the invoke declaration nonsense in a separate method
        final String effectListParameter = fields.getAndRemoveTrimmed(EFFECT_LIST);
        if (effectListParameter != null) {
            this.invokeeDeclaration = String.format(
                    "List<InvokeEffect> invokees = %s.getEffectsList(%s",
                    this.battleParameter, effectListParameter
            );

            final String effectPriority = fields.getAndRemoveTrimmed(EFFECT_PRIORITY);
            if (effectPriority != null) {
                this.invokeeDeclaration += ", " + effectPriority;
            }

            final String invokeAttack = fields.getAndRemoveTrimmed(INVOKE_ATTACK);
            if (invokeAttack != null) {
                this.invokeeDeclaration += ", " + invokeAttack + ".getAttack()";
            }

            this.invokeeDeclaration += ");";
        }

        final String statInvokeAttack = fields.getAndRemoveTrimmed(STAT_INVOKE_ATTACK);
        if (statInvokeAttack != null) {
            setInvokeeDeclaration(
                    "// Only add the attack when checking a defensive stat -- this means the other pokemon is the one currently attacking\n" +
                            "List<InvokeEffect> invokees = " + this.battleParameter + ".getEffectsList(" + statInvokeAttack + ");\n" +
                            "if (!s.user()) {\n" +
                            "invokees.add(" + statInvokeAttack + ".getAttack());\n" +
                            "}\n"
            );
        }

        final String nonBattleInvokees = fields.getAndRemoveTrimmed(NON_BATTLE_EFFECTS);
        if (nonBattleInvokees != null) {
            setInvokeeDeclaration(
                    "List<InvokeEffect> invokees = new ArrayList<>();\n" +
                            "invokees.add(" + nonBattleInvokees + ".getAbility());\n" +
                            "invokees.add(" + nonBattleInvokees + ".getActualHeldItem());\n"
            );
        }

        final String setInvokees = fields.getAndRemoveTrimmed(SET_INVOKEES);
        if (setInvokees != null) {
            setInvokeeDeclaration(setInvokees + "\n");
        }

        // TODO: Eventually would just like to remove the invokee loop for this case and just operate directly on the attack
        final String moveInvoke = fields.getAndRemoveTrimmed(MOVE);
        if (moveInvoke != null) {
            setInvokeeDeclaration(String.format("List<InvokeEffect> invokees = Collections.singletonList(%s.getAttack());", moveInvoke));
        }

        final String updateField = fields.getAndRemoveTrimmed(UPDATE);
        if (updateField != null) {
            this.updateField = updateField;
        }

        final String moldBreaker = fields.getAndRemoveTrimmed(MOLD_BREAKER);
        if (moldBreaker != null) {
            this.moldBreaker = moldBreaker;
        }

        final String moldBreakerNullCheck = fields.getAndRemoveTrimmed(MOLD_BREAKER_NULL_CHECK);
        if (moldBreakerNullCheck != null) {
            if (!StringUtils.isNullOrEmpty(this.moldBreaker)) {
                Global.error("Cannot define a mold breaker and a mold breaker null check. Interface: " + this.interfaceName);
            }

            this.moldBreaker = "ActivePokemon moldBreaker = " + moldBreakerNullCheck + ";";
            this.moldBreakNullCheck = true;
        }

        final String defaultMethod = fields.getAndRemoveTrimmed(DEFAULT);
        if (defaultMethod != null) {
            this.defaultMethod = defaultMethod;
        }

        final String allDeadsies = fields.getAndRemoveTrimmed(DEADSIES);
        if (allDeadsies != null) {
            Scanner in = new Scanner(allDeadsies);
            while (in.hasNext()) {
                this.deadsies.add(in.next());
            }
        }

        final String isOverride = fields.getAndRemoveTrimmed(OVERRIDE);
        if (isOverride != null) {
            if (!isOverride.equals("True")) {
                Global.error("Invalid value for " + OVERRIDE + ": " + isOverride);
            }
            this.isOverride = true;
        }

        final String begin = fields.getAndRemoveTrimmed(BEGIN);
        if (begin != null) {
            this.begin = begin;
        }

        fields.confirmEmpty();

        if ((this.returnType == null || this.methodName == null) && this.invokeMethod == null) {
            Global.error("Interface method and invoke method are both missing for interface " + this.interfaceName);
        }

        if (this.isOverride && StringUtils.isNullOrEmpty(this.defaultMethod)) {
            Global.error("Can only override default methods.");
        }
    }

    private void setParameters(String parameters, boolean setTypeless) {
        if (!StringUtils.isNullOrEmpty(parameters)) {
            final String[] split = parameters.split(",");
            for (final String typedParameter : split) {
                final Entry<String, String> parameterPair = MatchType.getVariableDeclaration(typedParameter.trim());
                final String parameterType = parameterPair.getKey();
                final String parameterName = parameterPair.getValue();

                if (setTypeless) {
                    if (!this.typelessParameters.isEmpty()) {
                        this.typelessParameters += ", ";
                    }

                    this.typelessParameters += parameterName;
                }

                if (parameterType.equals(Battle.class.getSimpleName())) {
                    if (!StringUtils.isNullOrEmpty(this.battleParameter)) {
                        Global.error("Can only have one battle parameter. Interface: " + this.interfaceName);
                    }

                    this.battleParameter = parameterName;
                }
            }
        }
    }

    String writeInterfaceMethod() {
        if (StringUtils.isNullOrEmpty(this.returnType) || StringUtils.isNullOrEmpty(this.methodName)) {
            return StringUtils.empty();
        }

        final StringAppender interfaceMethod = new StringAppender();
        if (!StringUtils.isNullOrEmpty(this.comments)) {
            interfaceMethod.appendLine("\n\t\t" + this.comments);
        }

        if (!StringUtils.isNullOrEmpty(this.defaultMethod)) {
            if (this.isOverride) {
                interfaceMethod.append("\n\t\t@Override");
            }

            if (this.defaultMethod.equals("Empty")) {
                interfaceMethod.appendFormat("\t\tdefault %s {}\n", this.getHeader());
            } else {
                interfaceMethod.append(new MethodInfo(this.getHeader(), this.defaultMethod, AccessModifier.DEFAULT).writeFunction());
            }
        } else {
            interfaceMethod.appendFormat("\t\t%s;\n", this.getHeader());
        }

        return interfaceMethod.toString();
    }

    String writeInvokeMethod() {
        if (this.invokeMethod == null) {
            return StringUtils.empty();
        }

        return this.invokeMethod.writeInvokeMethod(this);
    }

    private String getHeader() {
        return MethodInfo.createHeader(this.returnType, this.methodName, this.parameters);
    }

    String getMethodCall() {
        return MethodInfo.createHeader(this.methodName, this.typelessParameters);
    }

    String getInterfaceName() {
        return this.interfaceName;
    }

    String getReturnType() {
        return this.returnType;
    }

    String getParameters() {
        return this.parameters;
    }

    String getAdditionalInvokeParameters() {
        return this.additionalInvokeParameters;
    }

    String getInvokeeDeclaration() {
        return this.invokeeDeclaration;
    }

    private void setInvokeeDeclaration(String invokeeDeclaration) {
        if (!StringUtils.isNullOrEmpty(this.invokeeDeclaration)) {
            Global.error("Can not define multiple ways to set the effects list. Interface: " + this.interfaceName);
        }

        this.invokeeDeclaration = invokeeDeclaration;
    }

    String getUpdateField() {
        return this.updateField;
    }

    String getMoldBreaker() {
        return this.moldBreaker;
    }

    boolean isMoldBreakNullCheck() {
        return this.moldBreakNullCheck;
    }

    String getBattleParameter() {
        return this.battleParameter;
    }

    Iterable<String> getDeadsies() {
        return this.deadsies;
    }

    String getBegin() {
        return this.begin;
    }
}
