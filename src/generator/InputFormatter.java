package generator;

import main.Global;
import util.FileIO;
import util.FileName;
import util.PokeString;
import util.StringUtils;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

public class InputFormatter {
    protected enum ReplaceType {
        BASIC("", (original, remaining) -> original),
        UPPER_CASE(index -> index + "", (original, remaining) -> original.toUpperCase()),
        UNDER_SPACE("_", (original, remaining) -> original.replaceAll("_", " ")),
        FINISH("-", (original, remaining) -> original + remaining);

        private final SuffixGetter suffixGetter;
        private final InputReplacer inputReplacer;

        ReplaceType(String replaceSuffix, InputReplacer inputReplacer) {
            this(index -> replaceSuffix, inputReplacer);
        }

        ReplaceType(SuffixGetter suffixGetter, InputReplacer inputReplacer) {
            this.suffixGetter = suffixGetter;
            this.inputReplacer = inputReplacer;
        }

        private interface SuffixGetter {
            String getSuffix(int index);
        }

        private interface InputReplacer {
            String replaceInput(String original, String remaining);
        }

        public String replaceBody(String body, String original, String remaining, int parameterIndex) {
            String suffix = this.suffixGetter.getSuffix(parameterIndex);
            String newValue = this.inputReplacer.replaceInput(original, remaining);

            return body.replace(String.format("{%d%s}", parameterIndex, suffix), newValue);
        }
    }

    protected String replaceBody(String body, String original, String remaining, int parameterIndex) {
        for (ReplaceType replaceType : ReplaceType.values()) {
            body = replaceType.replaceBody(body, original, remaining, parameterIndex);
        }

        return body;
    }

    String replaceBody(String body, String fieldValue, String className, String superClass) {
        body = body.replace("@ClassName", className);
        body = body.replace("@SuperClass", superClass.toUpperCase());

        body = replaceBody(body, fieldValue, StringUtils.empty(), 0);

        int index = 0;
        String[] mcSplit = fieldValue.split(" ");

        // Go through each parameter and replace if applicable
        // Increment the index to represent the space
        for (int i = 0; i < mcSplit.length; i++, index++) {
            index += mcSplit[i].length();
            String remaining = fieldValue.substring(index, fieldValue.length());

            body = replaceBody(body, mcSplit[i], remaining, i + 1);
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
