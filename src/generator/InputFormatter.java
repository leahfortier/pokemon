package generator;

import main.Global;
import util.FileIO;
import util.FileName;
import util.StringUtils;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
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

    Entry<Integer, String> getValue(String[] splitInfo, String fieldValue, int index) {
        String type = splitInfo[index - 1];
        String value;

        String[] mcSplit = fieldValue.split(" ");

        switch (type) {
            case "String":
                value = "\"" + fieldValue + "\"";
                break;
            case "Int":
                value = fieldValue;
                break;
            case "Boolean":
                value = fieldValue.toLowerCase();
                if (!value.equals("false") && !value.equals("true")) {
                    Global.error("Invalid boolean type " + value);
                }

                break;
            case "Enum":
                String enumType = splitInfo[index++];

                if (enumType.endsWith("Namesies")) {
                    value = StringUtils.getNamesiesString(fieldValue);
                }
                else {
                    value = fieldValue.toUpperCase();
                }

                value = enumType + "." + value;

                break;
            case "Function":
                String functionName = splitInfo[index++];
                int numParameters = Integer.parseInt(splitInfo[index++]);

                value = functionName + "(";
                boolean first = true;

                for (int i = 0; i < numParameters; i++) {
                    int mcSplitDex = Integer.parseInt(splitInfo[index++]);
                    String parameter;

                    Entry<Integer, String> entry = getValue(splitInfo, mcSplit[mcSplitDex], index + 1);
                    index = entry.getKey();
                    parameter = entry.getValue();

                    value += (first ? "" : ", ") + parameter;
                    first = false;
                }

                value += ")";
                break;
            default:
                Global.error("Invalid variable type " + type);
                value = StringUtils.empty();
                break;
        }

        return new SimpleEntry<>(index, value);
    }

    String getAssignment(String assignmentInfo, String fieldValue) {
        int index = 0;
        String[] split = assignmentInfo.split(" ");

        String type = split[index++];
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

        Entry<Integer, String> entry = getValue(split, fieldValue, index);
        index = entry.getKey();
        String value = entry.getValue();

        String fieldName = split[index++];
        String assignment = "super." + fieldName;

        if (split.length > index) {
            String assignmentType = split[index++];
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
        boolean implemented = false;
        String implementsString = "";

        for (String interfaceName : interfaces) {
            if (interfaceName.contains("Hidden-")) {
                continue;
            }

            implementsString += (implemented ? ", " : "implements ") + interfaceName;
            implemented = true;
        }

        return implementsString;
    }

    String getConstructorValue(Entry<String, String> pair, ClassFields fields) {
        int index = 0;
        String[] split = pair.getValue().split(" ");
        String type = split[index++];

        String fieldValue = null;

        if (type.equals("Default")) {
            fieldValue = split[index++];
            type = split[index++];
        }

        String key = pair.getKey();
        String value = fields.getAndRemove(key);

        if (value != null) {
            fieldValue = value;
        }
        else if (fieldValue == null) {
            Global.error("Missing required constructor field " + key + " for " + fields.getClassName());
        }

        return getValue(split, fieldValue, index).getValue();
    }
}
