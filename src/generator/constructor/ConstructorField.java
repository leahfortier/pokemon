package generator.constructor;

import generator.fields.ClassFields;
import generator.format.SplitScanner;
import main.Global;
import util.GeneralUtils;
import util.string.StringAppender;
import util.string.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Used for the field definitions at the top of the file
class ConstructorField {
    private final String fieldName;
    private final boolean multiple;
    private final String defaultValue;
    private final String fieldType;
    private final String enumType;
    private final String remaining;

    public ConstructorField(String line) {
        SplitScanner split = new SplitScanner(line);

        this.fieldName = split.next();
        String fieldType = split.next();

        boolean multiple = false;
        if (fieldType.equals("Multiple")) {
            multiple = true;
            fieldType = split.next();
        }

        String defaultValue = "";
        if (fieldType.equals("Default")) {
            defaultValue = split.next();
            fieldType = split.next();
        }

        String enumType = "";
        if (fieldType.equals("Enum")) {
            enumType = split.next();
        }

        this.remaining = split.getRemaining();

        this.multiple = multiple;
        this.defaultValue = defaultValue;
        this.fieldType = fieldType;
        this.enumType = enumType;
    }

    public String getFieldName() {
        return this.fieldName;
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

    private String getValue(String fieldValue) {
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
                return "";
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
