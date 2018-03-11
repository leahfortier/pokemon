package generator.fieldinfo;

import generator.ClassFields;
import generator.format.SplitScanner;
import main.Global;
import util.GeneralUtils;
import util.string.StringAppender;
import util.string.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FieldInfo {
    public final String fieldName;
    public final boolean multiple;
    public final boolean not;
    public final String defaultValue;
    public final String fieldType;
    public final String enumType;
    public final String remaining;

    public FieldInfo(SplitScanner split) {
        this.fieldName = split.next();

        boolean multiple = false;
        boolean not = false;

        String fieldType = split.next();
        if (fieldType.equals("Multiple")) {
            multiple = true;
            fieldType = split.next();
        }

        if (fieldType.equals("Not")) {
            not = true;
            fieldType = split.next();
        }

        String defaultValue = StringUtils.empty();
        if (fieldType.equals("Default")) {
            defaultValue = split.next();
            fieldType = split.next();
        }

        String enumType = StringUtils.empty();
        if (fieldType.equals("Enum")) {
            enumType = split.next();
        }

        this.remaining = split.getRemaining();

        this.multiple = multiple;
        this.not = not;
        this.defaultValue = defaultValue;
        this.fieldType = fieldType;
        this.enumType = enumType;
    }

    public String getAssignment(String inputFieldValue) {
        List<String> fieldValues = new ArrayList<>();
        if (this.multiple) {
            Collections.addAll(fieldValues, inputFieldValue.split(","));
        } else {
            fieldValues.add(inputFieldValue);
        }

        return new StringAppender()
                .appendJoin("\n", fieldValues, fieldValue -> {
                    String value = this.getValue(fieldValue);
                    String assignment = "super." + remaining;

                    if (this.multiple) {
                        assignment += ".add(" + value + ");";
                    } else {
                        assignment += " = " + value + ";";
                    }

                    return assignment;
                })
                .toString();
    }

    public String getValue(String fieldValue) {
        fieldValue = fieldValue.trim();

        switch (fieldType) {
            case "StraightUp":
                return fieldValue;
            case "String":
                return "\"" + fieldValue + "\"";
            case "Int":
                return Integer.parseInt(fieldValue) + "";
            case "Double":
                return Double.parseDouble(fieldValue) + "";
            case "Boolean":
                return GeneralUtils.parseBoolean(fieldValue) + "";
            case "Enum":
                final String enumValue;
                if (enumType.endsWith("Namesies")) {
                    enumValue = StringUtils.getNamesiesString(fieldValue);
                } else {
                    enumValue = fieldValue.toUpperCase();
                }
                return enumType + "." + enumValue;
            default:
                Global.error("Invalid variable type " + fieldType);
                return StringUtils.empty();
        }
    }

    public String getConstructorValue(ClassFields fields) {
        String fieldValue = this.defaultValue;

        String key = this.fieldName;
        String value = fields.getAndRemove(key);

        if (value != null) {
            fieldValue = value;
        } else if (key.equals("Namesies")) {
            fieldValue = fields.getName();
        } else if (StringUtils.isNullOrEmpty(fieldValue)) {
            Global.error("Missing required constructor field " + key + " for " + fields.getClassName());
        }

        return this.getValue(fieldValue);
    }
}
