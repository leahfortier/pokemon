package generator.format;

import generator.ClassFields;
import generator.fieldinfo.ConstructorInfo;
import generator.fieldinfo.FailureInfo;
import generator.fieldinfo.InfoList;
import main.Global;
import util.file.FileIO;
import util.file.FileName;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class InputFormatter {
    private Map<String, MethodInfo> overrideMethods;
    private ConstructorInfo constructorInfo;
    private FailureInfo failureInfo;

    public void close() {}
    public void validate(ClassFields fields) {}
    public void useOverride(String overrideName) {}

    protected String replaceBody(String body, String original, String remaining, int parameterIndex, int numParameters) {
        for (ReplaceType replaceType : ReplaceType.values()) {
            body = replaceType.replaceBody(body, original, remaining, parameterIndex, numParameters);
        }

        return body;
    }

    public String replaceBody(String body, String fieldValue, String className, String superClass) {
        body = body.replace("@ClassName", className);
        body = body.replace("@SuperClass", superClass.toUpperCase());

        body = replaceBody(body, fieldValue, "", 0, -1);

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

    public Iterable<String> getOverrideFields() {
        if (this.overrideMethods == null) {
            this.readOverrideFormat();
        }

        return new ArrayList<>(overrideMethods.keySet());
    }

    public MethodInfo getOverrideMethod(String fieldName) {
        if (this.overrideMethods == null) {
            this.readOverrideFormat();
        }

        return this.overrideMethods.get(fieldName);
    }

    private void readOverrideFormat() {
        Scanner in = FileIO.openFile(FileName.OVERRIDE);

        // Want to preserve the input order
        overrideMethods = new LinkedHashMap<>();

        while (in.hasNext()) {
            String line = in.nextLine().trim();

            // Ignore comments and white space
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            this.addMethod(line.replace(":", ""), new MethodInfo(in));
        }
    }

    protected void addMethod(String fieldName, MethodInfo methodInfo) {
        this.overrideMethods.put(fieldName, methodInfo);
    }

    public String getImplementsString(List<String> interfaces) {
        if (interfaces.isEmpty()) {
            return "";
        }

        return "implements " + String.join(", ", interfaces);
    }

    public String getConstructor(ClassFields fields) {
        return constructorInfo.getConstructor(fields);
    }

    public String getFailure(ClassFields fields, String superClass) {
        if (failureInfo == null) {
            return "";
        }

        return failureInfo.writeFailure(fields, superClass, this);
    }

    public void readFileFormat(Scanner in) {
        failureInfo = null;

        InfoList superInfo = new InfoList(null);
        InfoList fieldKeys = new InfoList(null);
        while (in.hasNext()) {
            String line = in.nextLine().trim();

            // Ignore comments and white space
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            if (line.equals("***")) {
                break;
            }

            String formatType = line.replace(":", "");
            switch (formatType) {
                case "Constructor":
                    superInfo = new InfoList(in);
                    break;
                case "Fields":
                    fieldKeys = new InfoList(in);
                    break;
                case "Failure":
                    failureInfo = new FailureInfo(in);
                    break;
                default:
                    Global.error("Invalid format type " + formatType);
                    break;
            }
        }

        constructorInfo = new ConstructorInfo(superInfo, fieldKeys);
    }
}
