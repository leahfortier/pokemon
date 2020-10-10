package generator.interfaces;

import generator.AccessModifier;
import generator.fields.ClassFields;
import generator.format.MethodWriter;
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

    private final String ignoreCondition;
    private final List<String> deadsies;

    private final String defaultMethod;
    private final boolean isOverride;
    private final boolean isHiddenHeader;
    private final boolean isPrivate;

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
        this.ignoreCondition = builder.ignoreCondition;
        this.deadsies = builder.deadsies;
        this.defaultMethod = builder.defaultMethod;
        this.isOverride = builder.isOverride;
        this.isHiddenHeader = builder.isHiddenHeader;
        this.isPrivate = builder.isPrivate;
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
                interfaceMethod.append(new MethodWriter(this.getHeader(), this.defaultMethod, AccessModifier.DEFAULT).writeMethod());
            }
        } else if (!this.isHiddenHeader) {
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
        return MethodWriter.createHeader(this.returnType, this.methodName, this.parameters);
    }

    String getMethodCall() {
        return MethodWriter.createHeader(this.methodName, this.typelessParameters);
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

    boolean isPrivate() {
        return this.isPrivate;
    }

    String getBattleParameter() {
        return this.battleParameter;
    }

    String getIgnoreCondition() {
        return this.ignoreCondition;
    }

    Iterable<String> getDeadsies() {
        return this.deadsies;
    }

    String getBegin() {
        return this.begin;
    }
}
