package generator.interfaces;

import generator.AccessModifier;
import generator.fields.ClassFields;
import generator.format.MethodInfo;
import util.string.StringAppender;
import util.string.StringUtils;

import java.util.List;

class InterfaceMethod {
    private final String interfaceName;

    private final String returnType;
    private final String methodName;

    private final String parameters;
    private final String typelessParameters;
    private final String battleParameter;

    private final String additionalInvokeParameters;
    private final String invokeeDeclaration;

    private final String updateField;

    private final String moldBreaker;
    private final boolean moldBreakerNullCheck;

    private final String defaultMethod;

    private final List<String> deadsies;

    private final boolean isOverride;

    private final String begin;

    private final String comments;
    private final InvokeMethod invokeMethod;

    InterfaceMethod(ClassFields fields) {
        this.interfaceName = fields.getName();

        InterfaceMethodBuilder builder = new InterfaceMethodBuilder(this.interfaceName, fields);
        this.returnType = builder.returnType;
        this.methodName = builder.methodName;
        this.parameters = builder.parameters;
        this.typelessParameters = builder.typelessParameters;
        this.battleParameter = builder.battleParameter;
        this.additionalInvokeParameters = builder.additionalInvokeParameters;
        this.invokeeDeclaration = builder.invokeeDeclaration;
        this.updateField = builder.updateField;
        this.moldBreaker = builder.moldBreaker;
        this.moldBreakerNullCheck = builder.moldBreakerNullCheck;
        this.defaultMethod = builder.defaultMethod;
        this.deadsies = builder.deadsies;
        this.isOverride = builder.isOverride;
        this.begin = builder.begin;
        this.comments = builder.comments;
        this.invokeMethod = builder.invokeMethod;
    }

    String writeInterfaceMethod() {
        if (StringUtils.isNullOrEmpty(this.returnType) || StringUtils.isNullOrEmpty(this.methodName)) {
            return "";
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
            return "";
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

    String getUpdateField() {
        return this.updateField;
    }

    String getMoldBreaker() {
        return this.moldBreaker;
    }

    boolean isMoldBreakerNullCheck() {
        return this.moldBreakerNullCheck;
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
