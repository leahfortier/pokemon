package generator.format;

import generator.AccessModifier;
import generator.fields.ClassFields;
import generator.fields.MapField;
import main.Global;
import util.string.StringAppender;
import util.string.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MethodWriter {
    private String header;
    private AccessModifier accessModifier;

    private String begin;
    private String body;
    private String end;
    private String fullBody;

    private List<String> addInterfaces;
    private List<MapField> addMapFields;

    private MethodWriter() {
        this.header = null;
        this.accessModifier = AccessModifier.PUBLIC;

        this.begin = "";
        this.body = "";
        this.end = "";

        this.fullBody = "";

        this.addInterfaces = new ArrayList<>();
        this.addMapFields = new ArrayList<>();
    }

    public MethodWriter(final String header, final String body, final AccessModifier accessModifier) {
        this();

        this.header = header;
        this.body = body;
        this.accessModifier = accessModifier;
    }

    MethodWriter(final Scanner in) {
        this();

        while (in.hasNext()) {
            String line = in.nextLine().trim();
            if (line.equals("*")) {
                break;
            }

            MapField mapField = new MapField(in, line);

            String key = mapField.fieldName;
            String value = mapField.fieldValue;

            switch (key) {
                case "Header":
                    this.header = value;
                    break;
                case "Body":
                    this.body = value;
                    break;
                case "Begin":
                    this.begin = value;
                    break;
                case "End":
                    this.end = value;
                    break;
                case "AddMapField":
                    addMapFields.add(new MapField(in, value));
                    break;
                case "AddInterface":
                    addInterfaces.add(value);
                    break;
                case "AccessModifier":
                    this.accessModifier = AccessModifier.getAccessModifier(value);
                    break;
                default:
                    Global.error("Invalid field name " + key);
            }
        }

        if (this.header == null && (!this.body.isEmpty() || !this.begin.isEmpty() || !this.end.isEmpty())) {
            Global.error("Cannot have a body without a header.");
        }
    }

    public Iterable<MapField> getMapFields() {
        return this.addMapFields;
    }

    private String writeMethod(String fieldValue, String className, String superClass, InputFormatter inputFormatter) {
        if (this.header == null) {
            return "";
        }

        if (this.body.isEmpty()) {
            this.fullBody = fieldValue;
        } else {
            this.fullBody = this.body;
        }

        this.fullBody = this.begin + this.fullBody + this.end;
        this.fullBody = inputFormatter.replaceBody(this.fullBody, fieldValue, className, superClass);

        return this.writeMethod();
    }

    public String writeMethod() {
        final String body = StringUtils.isNullOrEmpty(this.fullBody) ? this.body : this.fullBody;

        StringAppender method = new StringAppender();
        method.append("\n\t\t")
              .appendIf(this.accessModifier == AccessModifier.PUBLIC, "@Override\n\t\t")
              .appendPostDelimiter(" ", this.accessModifier.getModifierName())
              .appendLine(this.header.trim() + " {");

        MethodFormatter formatter = new MethodFormatter(3);

        Scanner in = new Scanner(body);
        while (in.hasNextLine()) {
            String line = in.nextLine().trim();
            formatter.appendLine(line, method);
        }

        method.appendLine("\t\t}");

        in.close();
        return method.toString();
    }

    public static boolean addMethodInfo(StringAppender methods,
                                        ClassFields fields,
                                        List<String> interfaces,
                                        String superClass,
                                        InputFormatter inputFormatter) {
        boolean added = false;
        String className = fields.getClassName();

        for (String fieldName : fields.getFieldNames()) {
            MethodWriter methodWriter = inputFormatter.getOverrideMethod(fieldName);
            if (methodWriter == null) {
                continue;
            }

            String fieldValue = fields.get(fieldName);
            String implementation = methodWriter.writeMethod(fieldValue, className, superClass, inputFormatter);
            methods.append(implementation);

            interfaces.addAll(methodWriter.addInterfaces);

            for (MapField addField : methodWriter.addMapFields) {
                String addFieldName = inputFormatter.replaceBody(addField.fieldName, fieldValue, className, superClass);
                String addFieldValue = inputFormatter.replaceBody(addField.fieldValue, fieldValue, className, superClass);
                fields.addNew(addFieldName, addFieldValue);
            }

            fields.remove(fieldName);
            inputFormatter.useOverride(fieldName);
            added = true;
        }

        return added;
    }

    public static String createHeader(final String returnType, final String methodName, final String parameters) {
        return createHeader(returnType + " " + methodName, parameters);
    }

    public static String createHeader(final String returnTypeAndName, final String parameters) {
        return String.format("%s(%s)", returnTypeAndName, parameters);
    }
}
