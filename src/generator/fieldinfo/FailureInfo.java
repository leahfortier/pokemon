package generator.fieldinfo;

import generator.ClassFields;
import generator.format.InputFormatter;
import generator.format.MethodInfo;
import generator.format.SplitScanner;
import main.Global;
import util.string.StringAppender;

import java.util.Scanner;

public class FailureInfo extends InfoList {
    private String header;

    public FailureInfo(Scanner in) {
        super(in);

        if (this.header == null) {
            Global.error("No header found for failure info.");
        }
    }

    @Override
    protected boolean shouldCreateInfo(SplitScanner split) {
        String fieldName = split.next();
        String remaining = split.getRemaining();
        split.reset();

        if (fieldName.equals("Header")) {
            if (header != null) {
                Global.error("Multiple headers found for failure info (" + header + ", " + remaining + ")");
            }
            header = remaining;
            return false;
        }

        return super.shouldCreateInfo(split);
    }

    public String writeFailure(ClassFields fields, String superClass, InputFormatter inputFormatter) {
        StringAppender failure = new StringAppender();

        for (FieldInfo fieldInfo : infoList) {
            String fieldValue = fields.get(fieldInfo.fieldName);
            if (fieldValue == null) {
                if (!fieldInfo.not) {
                    continue;
                }

                fieldValue = fieldInfo.defaultValue;
            } else if (fieldInfo.not) {
                fields.remove(fieldInfo.fieldName);
                continue;
            }

            final String[] fieldValues;
            if (fieldInfo.multiple) {
                fieldValues = fieldValue.split(",");
            } else {
                fieldValues = new String[] { fieldValue };
            }

            for (String value : fieldValues) {
                String pairValue = fieldInfo.getValue(value);

                String body = fieldInfo.remaining;
                body = inputFormatter.replaceBody(body, pairValue, fields.getClassName(), superClass);

                failure.appendDelimiter(" || ", body);
            }

            fields.remove(fieldInfo.fieldName);
        }

        if (failure.isEmpty()) {
            return "";
        }

        failure.appendPrefix("return !(").append(");");

        return new MethodInfo(this.header, failure.toString()).writeFunction();
    }
}
