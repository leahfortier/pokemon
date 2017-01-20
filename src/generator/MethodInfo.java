package generator;

import main.Global;
import util.StringUtils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

class MethodInfo {

    private String header;
    private AccessModifier accessModifier;

    private String begin;
    private String body;
    private String end;

    private String fullBody;

    private boolean tryParse;
    private boolean required;
    private boolean defaultBody;

    private final List<String> addInterfaces;
    private final List<Map.Entry<String, String>> addMapFields;

    private MethodInfo() {
        this.header = null;
        this.accessModifier = AccessModifier.PUBLIC;

        this.begin = "";
        this.body = "";
        this.end = "";

        this.fullBody = "";

        this.tryParse = false;
        this.required = true;
        this.defaultBody = false;

        this.addInterfaces = new ArrayList<>();
        this.addMapFields = new ArrayList<>();
    }

    MethodInfo(final String header, final String body) {
        this();

        this.header = header;
        this.body = body;
    }

    MethodInfo(final String header, final String body, final AccessModifier accessModifier) {
        this();

        this.header = header;
        this.body = body;
        this.accessModifier = accessModifier;
    }

    MethodInfo(final String header, final String begin, final String body, final String end) {
        this(header, body);

        this.begin = begin;
        this.end = end;
    }

    MethodInfo(final Scanner in, final boolean isInterfaceMethod) {
        this();

        while (in.hasNext()) {
            String line = in.nextLine().trim();
            if (line.equals("*")) {
                break;
            }

            Map.Entry<String, String> pair = StuffGen.getFieldPair(in, line);

            String key = pair.getKey();
            String value = pair.getValue();

            switch (key) {
                case "Header":
                    this.header = value;
                    break;
                case "Try":
                    tryParse = true;
                    this.body = value;
                    break;
                case "Default":
                    this.defaultBody = true;
                    this.body = value;
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
                    Map.Entry<String, String> fieldPair = StuffGen.getFieldPair(in, value);
                    addMapFields.add(new AbstractMap.SimpleEntry<>(fieldPair.getKey(), fieldPair.getValue()));
                    break;
                case "AddInterface":
                    addInterfaces.add(value);
                    break;
                case "Optional":
                    if (!value.equals("True")) {
                        Global.error("True is the only valid optional value");
                    }
                    this.required = false;
                    break;
                case "AccessModifier":
                    this.accessModifier = AccessModifier.getAccessModifier(value);
                    break;
                default:
                    Global.error("Invalid field name " + key);
            }
        }

        if (this.header == null && (this.body.length() > 0 || this.begin.length() > 0 || this.end.length() > 0)) {
            Global.error("Cannot have a body without a header.");
        }

        if (this.defaultBody && this.required) {
            Global.error("Can only have a default body if the field is optional");
        }
    }

    private String writeFunction(String fieldValue, String className, String superClass, InputFormatter inputFormatter) {
        if (this.header == null) {
            return StringUtils.empty();
        }

        if (this.body.isEmpty()) {
            this.fullBody = fieldValue;
        }
        else {
            this.fullBody = this.body;
        }

        if (this.tryParse) {
            try {
                Double.parseDouble(fieldValue);
            } catch (NumberFormatException exception) {
                this.fullBody = fieldValue;
            }
        }

        if (fieldValue.length() > 0 && this.defaultBody) {
            this.fullBody = fieldValue;
        }

        this.fullBody = this.begin + this.fullBody + this.end;
        this.fullBody = inputFormatter.replaceBody(this.fullBody, fieldValue, className, superClass);

        if (!this.required && !this.defaultBody && StringUtils.isNullOrEmpty(this.fullBody)) {
            return StringUtils.empty();
        }

        return this.writeFunction();
    }

    String writeFunction() {
        final String body = StringUtils.isNullOrEmpty(this.fullBody) ? this.body : this.fullBody;

        StringBuilder method = new StringBuilder();
        StringUtils.appendLine(
                method,
                "\n\t\t" +
                    StringUtils.addSpace(this.accessModifier.getModifierName()) +
                    this.header.trim() +
                    " {"
        );

        MethodFormatter formatter = new MethodFormatter(3);

        Scanner in = new Scanner(body);
        while (in.hasNextLine()) {
            String line = in.nextLine().trim();
            formatter.appendLine(line, method);
        }

        method.append("\t\t}\n");

        in.close();
        return method.toString();
    }

    // Interface name should be empty if it is an override
    static boolean addMethodInfo(StringBuilder methods,
                                 List<Map.Entry<String, MethodInfo>> methodList,
                                 ClassFields fields,
                                 List<String> interfaces,
                                 String interfaceName,
                                 String superClass,
                                 InputFormatter inputFormatter
    ) {
        boolean added = false;
        String className = fields.getClassName();

        for (Map.Entry<String, MethodInfo> pair : methodList) {
            String fieldName = pair.getKey();
            String fieldValue = fields.get(fieldName);

            MethodInfo methodInfo = pair.getValue();

            if (fieldValue == null) {
                // Overrides are not required to contain the field value
                if (interfaceName.isEmpty()) {
                    continue;
                }

                if (methodInfo.required) {
                    Global.error("Missing required field " + fieldName + " to implement interface " + interfaceName + " for class " + className);
                }

                fieldValue = "";
            }

            String implementation = methodInfo.writeFunction(fieldValue, className, superClass, inputFormatter);
            methods.append(implementation);

            interfaces.addAll(methodInfo.addInterfaces);

            for (Map.Entry<String, String> addField : methodInfo.addMapFields) {
                String fieldKey = addField.getKey();
                String addFieldValue = inputFormatter.replaceBody(addField.getValue(), fieldValue, className, superClass);

                String mapField = fields.get(fieldKey);
                if (mapField == null) {
                    mapField = addFieldValue;
                }
                else if (fieldKey.equals("MoveType")) {
                    mapField += ", " + addFieldValue;
                }
                else if (fieldKey.equals("Field")) {
                    mapField += addFieldValue;
                }
//				else {
//					// Leave the map field as is -- including in the original fields overrides the override file
//					System.out.println("Map Field (ClassName = " + className + "): " + mapField);
//				}

                fields.add(fieldKey, mapField);
            }

            fields.remove(fieldName);
            added = true;
        }

        return added;
    }

    static String createHeader(final String returnType, final String methodName, final String parameters) {
        return createHeader(returnType + " " + methodName, parameters);
    }

    static String createHeader(final String returnTypeAndName, final String parameters) {
        return String.format("%s(%s)", returnTypeAndName, parameters);
    }
}

