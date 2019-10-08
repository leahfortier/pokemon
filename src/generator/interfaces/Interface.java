package generator.interfaces;

import generator.fields.ClassFields;
import generator.StuffGen;
import generator.fields.MapField;
import main.Global;
import util.string.StringAppender;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

class Interface {
    private static final String COMMENTS = "Comments";
    private static final String METHOD = "Method";
    private static final String EXTENDS = "Extends";

    private final String interfaceName;

    private String headerComments;
    private String extendsInterfaces;
    private List<InterfaceMethod> methods;

    Interface(final Scanner in, final String interfaceName) {
        this.interfaceName = interfaceName;

        this.headerComments = "";
        this.extendsInterfaces = "";
        this.methods = new LinkedList<>();

        readInterface(in);
    }

    private void readInterface(Scanner in) {
        while (in.hasNextLine()) {
            String line = in.nextLine().trim();
            if (line.equals("***")) {
                break;
            }

            // Read in the next field and value
            InterfaceField mapField = new InterfaceField(in, line);

            String key = mapField.fieldName;
            String value = mapField.fieldValue;

            switch (key) {
                case COMMENTS:
                    this.headerComments = value;
                    break;
                case EXTENDS:
                    this.extendsInterfaces = value;
                    break;
                case METHOD:
                    this.methods.add(mapField.method);
                    break;
                default:
                    Global.error("Invalid key name " + key + " for interface " + this.interfaceName);
                    break;
            }
        }
    }

    String writeInterface() {
        final String superClass = this.extendsInterfaces;
        final String interfaces = null;
        final String extraFields = new StringAppender()
                .appendJoin("", this.methods, InterfaceMethod::writeInterfaceMethod)
                .toString();
        final String constructor = null;
        final String additional = new StringAppender()
                .appendJoin("", this.methods, InterfaceMethod::writeInvokeMethod)
                .toString();
        final boolean isInterface = true;

        return StuffGen.createClass(
                this.headerComments,
                this.interfaceName,
                superClass,
                interfaces,
                extraFields,
                constructor,
                additional,
                isInterface
        );
    }

    // A MapField object that overrides the readValue method to read an interface method for the appropriate key
    private class InterfaceField extends MapField {
        private InterfaceMethod method;

        public InterfaceField(Scanner in, String line) {
            super(in, line);
        }

        @Override
        protected String readValue(Scanner in, String key, String value) {
            if (key.equals(METHOD)) {
                ClassFields fields = new ClassFields(in, interfaceName);
                this.method = new InterfaceMethod(fields);
                return "";
            } else {
                return super.readValue(in, key, value);
            }
        }
    }
}
