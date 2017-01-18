package generator;

import main.Global;
import util.FileIO;
import util.FileName;
import util.PokeString;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

class InputFormatter {
    private static InputFormatter instance = new InputFormatter();
    static InputFormatter instance() {
        return instance;
    }

    String replaceBody(String body, String fieldValue, String className, String superClass) {
        body = body.replace("@ClassName", className);
        body = body.replace("@SuperClass", superClass.toUpperCase());

        body = body.replace("{0}", fieldValue);
        body = body.replace("{00}", fieldValue.toUpperCase());

        String[] mcSplit = fieldValue.split(" ");
        for (int i = 0; i < mcSplit.length; i++) {
            body = body.replace(String.format("{%d}", i + 1), mcSplit[i]);
            body = body.replace(String.format("{%d%d}", i + 1, i + 1), mcSplit[i].toUpperCase());
            body = body.replace(String.format("{%d_}", i + 1), mcSplit[i].replaceAll("_", " "));

            String pattern = String.format("{%d-}", i + 1);
            if (body.contains(pattern)) {
                if (i + 1 == 1) {
                    Global.error("Don't use {1-}, instead use {0} (ClassName = " + className + ")");
                }

                String text = mcSplit[i];
                for (int j = i + 1; j < mcSplit.length; j++) {
                    if (body.contains("{" + (j + 1))) {
                        Global.error(j + " Cannot have any more parameters once you split through. (ClassName = " + className + ")");
                    }

                    text += " " + mcSplit[j];
                }

                body = body.replace(pattern, text);
            }
        }

        return body;
    }

    private List<Entry<String, MethodInfo>> overrideMethods;
    private Map<String, List<Entry<String, MethodInfo>>> interfaceMethods;

    List<Entry<String, MethodInfo>> getOverrideMethods() {
        if (this.overrideMethods == null) {
            this.readFormat();
        }

        return overrideMethods;
    }

    List<Entry<String, MethodInfo>> getInterfaceMethods(String interfaceName) {
        if (this.interfaceMethods == null) {
            this.readFormat();
        }

        return this.interfaceMethods.get(interfaceName);
    }

    private void readFormat() {
        Scanner in = FileIO.openFile(FileName.OVERRIDE);

        overrideMethods = new ArrayList<>();
        interfaceMethods = new HashMap<>();

        Set<String> fieldNames = new HashSet<>();

        while (in.hasNext()) {
            String line = in.nextLine().trim();

            // Ignore comments and white space at beginning of file
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String interfaceName = line.replace(":", "");
            List<Entry<String, MethodInfo>> list = new ArrayList<>();

            boolean isInterfaceMethod = !interfaceName.equals("Override");

            while (in.hasNextLine()) {
                line = in.nextLine().trim();
                if (line.equals("***")) {
                    break;
                }

                String fieldName = line.replace(":", "");
                list.add(new SimpleEntry<>(fieldName, new MethodInfo(in, isInterfaceMethod)));

                if (fieldNames.contains(fieldName)) {
                    Global.error("Duplicate field name " + fieldName + " in override.txt");
                }

                fieldNames.add(fieldName);
            }

            if (isInterfaceMethod) {
                interfaceMethods.put(interfaceName, list);
            }
            else {
                if (list.size() != 1) {
                    Global.error("Only interfaces can include multiple methods");
                }

                overrideMethods.add(list.get(0));
            }
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
                    value = PokeString.getNamesiesString(fieldValue);
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
                value = "";
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

    String getConstructorValue(Entry<String, String> pair, Map<String, String> fields) {
        int index = 0;
        String[] split = pair.getValue().split(" ");
        String type = split[index++];

        String fieldValue = null;
        String className = fields.get("ClassName");

        if (type.equals("DefaultMap")) {
            String mapKey = split[index++];
            fieldValue = fields.get(mapKey);

            if (fieldValue == null) {
                Global.error("Invalid map key " + mapKey + " for " + className);
            }

            type = split[index++];
        }
        else if (type.equals("Default")) {
            fieldValue = split[index++];
            type = split[index++];
        }

        String key = pair.getKey();

        if (fields.containsKey(key)) {
            fieldValue = fields.get(key);
            fields.remove(key);
        }
        else if (fieldValue == null) {
            Global.error("Missing required constructor field " + key + " for " + className);
        }

        return getValue(split, fieldValue, index).getValue();
    }
}
