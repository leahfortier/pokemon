package generator.interfaces;

import generator.fields.ClassFields;
import main.Global;
import pattern.MatchType;
import util.string.StringUtils;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// All valid keywords for defining an interface method and what to do with its value
public enum InterfaceMethodKey {
    HEADER("Header", ((builder, fields, value) -> {
        Pattern headerPattern = Pattern.compile(
                "^" + MatchType.VARIABLE_TYPE.group() + // Group 1: return type
                        " " + MatchType.WORD.group() +  // Group 2: method name
                        "\\((.*)\\)$"                   // Group 3: method parameters
        );

        Matcher matcher = headerPattern.matcher(value);
        if (!matcher.matches()) {
            Global.error("Header not properly formatted for " + builder.interfaceName + ", Header: " + value);
        }

        builder.returnType = matcher.group(1);
        builder.methodName = matcher.group(2);
        builder.parameters = matcher.group(3);
    })),
    INVOKE_PARAMETERS("InvokeParameters", ((builder, fields, value) -> {
        builder.additionalInvokeParameters = value;
        builder.setParameters(builder.additionalInvokeParameters, false);
    })),
    COMMENTS("Comments", ((builder, fields, value) -> {
        builder.comments = value;
    })),
    INVOKE("Invoke", ((builder, fields, value) -> {
        Scanner in = new Scanner(value);
        builder.invokeMethod = InvokeType.getInvokeMethod(in.next(), in);
    })),
    INVOKE_NAME("InvokeName", ((builder, fields, value) -> {
        if (builder.invokeMethod == null) {
            Global.error("Must specify type of invoke method if you want to name it.  Interface: " + builder.interfaceName);
        }

        builder.invokeMethod.setMethodName(value);
    })),
    EFFECT_PRIORITY("EffectPriority", null), // Used inside EffectList
    INVOKE_ATTACK("InvokeAttack", null), // Used inside EffectList
    EFFECT_LIST("EffectList", ((builder, fields, value) -> {
        builder.invokeeDeclaration = String.format(
                "List<InvokeEffect> invokees = %s.getEffectsList(%s",
                builder.battleParameter, value
        );

        final String effectPriority = fields.getAndRemoveTrimmed(EFFECT_PRIORITY.keyName);
        if (effectPriority != null) {
            builder.invokeeDeclaration += ", " + effectPriority;
        }

        final String invokeAttack = fields.getAndRemoveTrimmed(INVOKE_ATTACK.keyName);
        if (invokeAttack != null) {
            builder.invokeeDeclaration += ", " + invokeAttack + ".getAttack()";
        }

        builder.invokeeDeclaration += ");";
    })),
    STAT_INVOKE_ATTACK("StatInvokeAttack", ((builder, fields, value) -> {
        builder.setInvokeeDeclaration(
                "// Only add the attack when checking a defensive stat -- this means the other pokemon is the one currently attacking\n" +
                        "List<InvokeEffect> invokees = " + builder.battleParameter + ".getEffectsList(" + value + ");\n" +
                        "if (s.isDefending()) {\n" +
                        "invokees.add(" + value + ".getAttack());\n" +
                        "}\n"
        );
    })),
    NON_BATTLE_EFFECTS("NonBattleEffects", ((builder, fields, value) -> {
        builder.setInvokeeDeclaration(
                "List<InvokeEffect> invokees = new ArrayList<>();\n" +
                        "invokees.add(" + value + ".getAbility());\n" +
                        "invokees.add(" + value + ".getActualHeldItem());\n"
        );
    })),
    SET_INVOKEES("SetInvokees", ((builder, fields, value) -> {
        builder.setInvokeeDeclaration(value);
    })),
    MOVE("Move", ((builder, fields, value) -> {
        // TODO: Eventually would just like to remove the invokee loop for this case and just operate directly on the attack
        builder.setInvokeeDeclaration(String.format(
                "List<InvokeEffect> invokees = Collections.singletonList(%s.getAttack());", value
        ));
    })),
    UPDATE("Update", ((builder, fields, value) -> {
        builder.updateField = value;
    })),
    MOLD_BREAKER("MoldBreaker", ((builder, fields, value) -> {
        builder.moldBreaker = value;
    })),
    MOLD_BREAKER_NULL_CHECK("MoldBreakerNullCheck", ((builder, fields, value) -> {
        if (!StringUtils.isNullOrEmpty(builder.moldBreaker)) {
            Global.error("Cannot define a mold breaker and a mold breaker null check. Interface: " + builder.interfaceName);
        }

        builder.moldBreaker = "ActivePokemon moldBreaker = " + value + ";";
        builder.moldBreakerNullCheck = true;
    })),
    IGNORE_CONDITION("IgnoreCondition", ((builder, fields, value) -> {
        builder.ignoreCondition = value;
    })),
    DEADSIES("Deadsies", ((builder, fields, value) -> {
        Scanner in = new Scanner(value);
        while (in.hasNext()) {
            builder.deadsies.add(in.next());
        }
    })),
    DEFAULT("Default", ((builder, fields, value) -> {
        builder.defaultMethod = value;
    })),
    OVERRIDE("Override", ((builder, fields, value) -> {
        if (!value.equals("True")) {
            Global.error("Invalid value for Override: " + value);
        }
        builder.isOverride = true;
    })),
    PRIVATE("Private", ((builder, fields, value) -> {
        if (!value.equals("True")) {
            Global.error("Invalid value for Private: " + value);
        }
        builder.isPrivate = true;
    })),
    BEGIN("Begin", ((builder, fields, value) -> {
        builder.begin = value;
    }));

    private final String keyName;
    private final FieldSetter fieldSetter;

    InterfaceMethodKey(String keyName, FieldSetter fieldSetter) {
        this.keyName = keyName;
        this.fieldSetter = fieldSetter;
    }

    public void setField(InterfaceMethodBuilder builder, ClassFields fields) {
        if (this.fieldSetter == null) {
            return;
        }

        String value = fields.getAndRemoveTrimmed(this.keyName);
        if (value != null) {
            this.fieldSetter.setField(builder, fields, value);
        }
    }

    @FunctionalInterface
    private interface FieldSetter {
        void setField(InterfaceMethodBuilder builder, ClassFields fields, String value);
    }
}
