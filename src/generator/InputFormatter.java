package generator;

import main.Global;
import util.FileIO;
import util.FileName;
import util.StringUtils;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

public class InputFormatter {

    protected void validate(ClassFields fields) {}

    protected String replaceBody(String body, String original, String remaining, int parameterIndex, int numParameters) {
        for (ReplaceType replaceType : ReplaceType.values()) {
            body = replaceType.replaceBody(body, original, remaining, parameterIndex, numParameters);
        }

        return body;
    }

    String replaceBody(String body, String fieldValue, String className, String superClass) {
        body = body.replace("@ClassName", className);
        body = body.replace("@SuperClass", superClass.toUpperCase());

        body = replaceBody(body, fieldValue, StringUtils.empty(), 0, -1);

        int index = 0;
        String[] mcSplit = fieldValue.split(" ");

        // Go through each parameter and replace if applicable
        // Increment the index to represent the space
        for (int i = 0; i < mcSplit.length; i++, index++) {
            index += mcSplit[i].length();
            String remaining = fieldValue.substring(index, fieldValue.length());

            body = replaceBody(body, mcSplit[i], remaining, i + 1, mcSplit.length);
        }

        return body;
    }

    private List<Entry<String, MethodInfo>> overrideMethods;

    List<Entry<String, MethodInfo>> getOverrideMethods() {
        if (this.overrideMethods == null) {
            this.readFormat();
        }

        return overrideMethods;
    }

    private void readFormat() {
        Scanner in = FileIO.openFile(FileName.OVERRIDE);

        overrideMethods = new ArrayList<>();

        Set<String> fieldNames = new HashSet<>();

        while (in.hasNext()) {
            String line = in.nextLine().trim();

            // Ignore comments and white space
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            if (line.equals("*")) {
                continue;
            }

            String fieldName = line.replace(":", "");
            if (fieldNames.contains(fieldName)) {
                Global.error("Duplicate field name " + fieldName + " in override.txt");
            }

            fieldNames.add(fieldName);

            overrideMethods.add(new SimpleEntry<>(fieldName, new MethodInfo(in)));
        }
    }

    String getValue(SplitScanner split, String fieldValue, String fieldType) {
        switch (fieldType) {
            case "StraightUp":
                return fieldValue;
            case "String":
                return "\"" + fieldValue + "\"";
            case "Int":
                return Integer.parseInt(fieldValue) + "";
            case "Boolean":
                String booleanValue = fieldValue.toLowerCase();
                if (!Arrays.asList("true", "false").contains(booleanValue)) {
                    Global.error("Invalid boolean type " + booleanValue);
                }
                return booleanValue;
            case "Enum":
                String enumType = split.next();
                final String enumValue;
                if (enumType.endsWith("Namesies")) {
                    enumValue = StringUtils.getNamesiesString(fieldValue);
                }
                else {
                    enumValue = fieldValue.toUpperCase();
                }
                return enumType + "." + enumValue;
            default:
                Global.error("Invalid variable type " + fieldType);
                return StringUtils.empty();
        }
    }

    String getAssignment(String assignmentInfo, String fieldValue) {
        SplitScanner split = new SplitScanner(assignmentInfo);

        String type = split.next();
        if (type.equals("Multiple")) {
            StringBuilder assignments = new StringBuilder();
            assignmentInfo = assignmentInfo.substring("Multiple".length() + 1);

            boolean first = true;
            for (String value : fieldValue.split(",")) {
                assignments.append(first ? "" : "\n")
                        .append(getAssignment(assignmentInfo, value.trim()));
                first = false;
            }

            return assignments.toString();
        }

        String value = getValue(split, fieldValue, type);

        String fieldName = split.next();
        String assignment = "super." + fieldName;

        if (split.hasNext()) {
            String assignmentType = split.next();
            switch (assignmentType) {
                case "List":
                    assignment += ".add(" + value + ");";
                    break;
                default:
                    Global.error("Invalid parameter " + assignmentType);
            }
        } else {
            assignment += " = " + value + ";";
        }

        return assignment;
    }

    String getImplementsString(List<String> interfaces) {
        if (interfaces.isEmpty()) {
            return StringUtils.empty();
        }

        return "implements " + String.join(", ", interfaces);
    }

    String getConstructorValue(Entry<String, String> pair, ClassFields fields) {
        SplitScanner split = new SplitScanner(pair.getValue());

        String type = split.next();
        String fieldValue = null;

        if (type.equals("Default")) {
            fieldValue = split.next();
            type = split.next();
        }

        String key = pair.getKey();
        String value = fields.getAndRemove(key);

        if (value != null) {
            fieldValue = value;
        }
        else if (StringUtils.isNullOrEmpty(fieldValue)) {
            Global.error("Missing required constructor field " + key + " for " + fields.getClassName());
        }

        return getValue(split, fieldValue, type);
    }
}
