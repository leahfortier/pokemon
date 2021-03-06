package generator.format;

import generator.constructor.ConstructorFieldList;
import generator.constructor.ConstructorInfo;
import generator.fields.ClassFields;
import main.Global;
import util.file.FileIO;
import util.file.FileName;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class InputFormatter {
    private Map<String, MethodWriter> overrideMethods;
    private ConstructorInfo constructorInfo;

    public void close() {}
    public void validate(ClassFields fields) {}
    public void useOverride(String overrideName) {}

    protected String replaceBody(String body, String original, String remaining, int parameterIndex) {
        for (ReplaceType replaceType : ReplaceType.values()) {
            body = replaceType.replaceBody(body, original, remaining, parameterIndex);
        }

        return body;
    }

    public String replaceBody(String body, String fieldValue, String className, String superClass) {
        body = body.replace("@ClassName", className);
        body = body.replace("@SuperClass", superClass.toUpperCase());

        body = replaceBody(body, fieldValue, "", 0);

        int index = 0;
        String[] mcSplit = fieldValue.split(" ");

        // Go through each parameter and replace if applicable
        // Increment the index to represent the space
        for (int i = 0; i < mcSplit.length; i++, index++) {
            index += mcSplit[i].length();
            String remaining = fieldValue.substring(index);

            body = replaceBody(body, mcSplit[i], remaining, i + 1);
        }

        return body;
    }

    public MethodWriter getOverrideMethod(String fieldName) {
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

            this.addMethod(line.replace(":", ""), new MethodWriter(in));
        }
    }

    protected void addMethod(String fieldName, MethodWriter methodWriter) {
        this.overrideMethods.put(fieldName, methodWriter);
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

    public void readFileFormat(Scanner in) {
        ConstructorFieldList superKeys = new ConstructorFieldList(null);
        ConstructorFieldList fieldKeys = new ConstructorFieldList(null);
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
                    superKeys = new ConstructorFieldList(in);
                    break;
                case "Fields":
                    fieldKeys = new ConstructorFieldList(in);
                    break;
                default:
                    Global.error("Invalid format type " + formatType);
                    break;
            }
        }

        constructorInfo = new ConstructorInfo(superKeys, fieldKeys);
    }
}
