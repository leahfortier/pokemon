package generator;

import generator.format.InputFormatter;
import generator.format.MethodInfo;
import generator.format.SplitScanner;
import main.Global;
import util.StringAppender;
import util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class FailureInfo {
    private final String header;
    private final List<FieldInfo> failureInfo;

    FailureInfo(Scanner in) {
        this.failureInfo = new ArrayList<>();

        String header = null;
        while (in.hasNext()) {
            String line = in.nextLine().trim();
            if (line.equals("*")) {
                break;
            }

            SplitScanner split = new SplitScanner(line);
            String fieldName = split.next();

            if (fieldName.equals("Header")) {
                if (header != null) {
                    Global.error("Multiple headers found for failure info (" + header + ", " + line + ")");
                }
                header = split.getRemaining();
            } else {
                this.failureInfo.add(new FieldInfo(split, fieldName));
            }
        }

        if (header == null) {
            Global.error("No header found for failure info.");
        }

        this.header = header;
    }

    String writeFailure(ClassFields fields, String superClass, InputFormatter inputFormatter) {
        StringAppender failure = new StringAppender();

        for (FieldInfo fieldInfo : failureInfo) {
            String fieldValue = fields.get(fieldInfo.fieldName);
            if (fieldValue == null) {
                if (!fieldInfo.not) {
                    continue;
                }

                fieldValue = fieldInfo.defaultValue;
            } else if (fieldInfo.not) {
                fields.remove(fieldInfo.fieldName);
                continue;
            }

            final String[] fieldValues;
            if (fieldInfo.list) {
                fieldValues = fieldValue.split(",");
            } else {
                fieldValues = new String[] { fieldValue };
            }

            fieldInfo.split.setTempIndex();
            for (String value : fieldValues) {
                String pairValue = inputFormatter.getValue(fieldInfo.split, value, fieldInfo.fieldType);

                String body = fieldInfo.split.getRemaining();
                body = inputFormatter.replaceBody(body, pairValue, fields.getClassName(), superClass);

                failure.appendDelimiter(" || ", body);

                fieldInfo.split.restoreTempIndex();
            }

            fields.remove(fieldInfo.fieldName);
        }

        if (failure.isEmpty()) {
            return StringUtils.empty();
        }

        failure.appendPrefix("return !(").append(");");

        return new MethodInfo(this.header, failure.toString()).writeFunction();
    }

    private static class FieldInfo {
        private final String fieldName;
        private final boolean not;
        private final boolean list;
        private final String defaultValue;
        private final String fieldType;
        private final SplitScanner split;

        public FieldInfo(SplitScanner split, String fieldName) {
            this.split = split;
            this.fieldName = fieldName;

            boolean not = false;
            boolean list = false;

            String fieldType = split.next();
            if (fieldType.equals("List")) {
                list = true;
                fieldType = split.next();
            }

            if (fieldType.equals("Not")) {
                not = true;
                fieldType = split.next();
            }

            String defaultValue = StringUtils.empty();
            if (fieldType.equals("Default")) {
                defaultValue = split.next();
                fieldType = split.next();
            }

            this.not = not;
            this.list = list;
            this.defaultValue = defaultValue;
            this.fieldType = fieldType;
        }
    }
}
