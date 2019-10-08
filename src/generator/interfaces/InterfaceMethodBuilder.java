package generator.interfaces;

import battle.Battle;
import generator.fields.ClassFields;
import main.Global;
import pattern.MatchType;
import util.string.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

// Blah blah blah I know it's not technically a builder but this method is for creating fields for an InterfaceMethod
public class InterfaceMethodBuilder {
    public final String interfaceName;

    public String returnType;
    public String methodName;
    public String parameters;
    public String typelessParameters;
    public String battleParameter;
    public String additionalInvokeParameters;
    public String invokeeDeclaration;
    public String updateField;
    public String moldBreaker;
    public boolean moldBreakerNullCheck;
    public String defaultMethod;
    public List<String> deadsies;
    public boolean isOverride;
    public String begin;
    public String comments;
    public InvokeMethod invokeMethod;

    InterfaceMethodBuilder(String interfaceName, ClassFields fields) {
        this.interfaceName = interfaceName;

        this.parameters = "";
        this.typelessParameters = "";
        this.deadsies = new ArrayList<>();

        this.readFields(fields);
    }

    private void readFields(ClassFields fields) {
        InterfaceMethodKey.HEADER.setField(this, fields);
        InterfaceMethodKey.INVOKE_PARAMETERS.setField(this, fields);
        this.setParameters(this.parameters, true);

        for (InterfaceMethodKey key : InterfaceMethodKey.values()) {
            key.setField(this, fields);
        }

        fields.confirmEmpty();

        if ((this.returnType == null || this.methodName == null) && this.invokeMethod == null) {
            Global.error("Interface method and invoke method are both missing for interface " + this.interfaceName);
        }

        if (this.isOverride && StringUtils.isNullOrEmpty(this.defaultMethod)) {
            Global.error("Can only override default methods.");
        }
    }

    public void setInvokeeDeclaration(String invokeeDeclaration) {
        if (!StringUtils.isNullOrEmpty(this.invokeeDeclaration)) {
            Global.error("Can not define multiple ways to set the effects list. Interface: " + this.interfaceName);
        }

        this.invokeeDeclaration = invokeeDeclaration;
    }

    public void setParameters(String parameters, boolean setTypeless) {
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
}
