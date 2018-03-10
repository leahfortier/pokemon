package generator.interfaces;

import generator.ClassFields;
import generator.StuffGen;
import main.Global;
import util.string.StringAppender;
import util.string.StringUtils;

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

        this.headerComments = StringUtils.empty();
        this.extendsInterfaces = StringUtils.empty();
        this.methods = new LinkedList<>();

        readInterface(in);
    }

    private void readInterface(Scanner in) {
        while (in.hasNextLine()) {
            String line = in.nextLine().trim();

            if (line.equals("***")) {
                break;
            }

            final String[] split = line.split(":", 2);
            final String fieldKey = split[0].trim();

            switch (fieldKey) {
                case COMMENTS:
                    // TODO: Right now class comment is restricted to a single line
                    this.headerComments = getSingleLineInput(COMMENTS, split);
                    break;
                case METHOD:
                    final ClassFields fields = new ClassFields(in, this.interfaceName);
                    this.methods.add(new InterfaceMethod(fields));
                    break;
                case EXTENDS:
                    this.extendsInterfaces = getSingleLineInput(EXTENDS, split);
                    break;
                default:
                    Global.error("Invalid key name " + fieldKey + " for interface " + this.interfaceName);
                    break;
            }
        }
    }

    private String getSingleLineInput(String key, String[] split) {
        if (split.length != 2) {
            Global.error(key + " for " + this.interfaceName + " must be on a single line.");
        }

        final String fieldValue = split[1].trim();
        if (fieldValue.isEmpty()) {
            Global.error(key + " for " + this.interfaceName + " is empty.");
        }

        return fieldValue;
    }

    String writeInterface() {
        final String superClass = this.extendsInterfaces;
        final String interfaces = null;
        final String extraFields = new StringAppender()
                .appendJoin(StringUtils.empty(), this.methods, InterfaceMethod::writeInterfaceMethod)
                .toString();
        final String constructor = null;
        final String additional = new StringAppender()
                .appendJoin(StringUtils.empty(), this.methods, InterfaceMethod::writeInvokeMethod)
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
}
