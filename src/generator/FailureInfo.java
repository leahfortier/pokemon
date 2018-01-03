package generator;

import util.StringUtils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

class FailureInfo {
    private String header;
    private List<Map.Entry<String, String>> failureInfo;

    FailureInfo(Scanner in) {
        failureInfo = new ArrayList<>();

        while (in.hasNext()) {
            String line = in.nextLine().trim();
            if (line.equals("*")) {
                break;
            }

            String[] split = line.split(" ", 2);

            String fieldName = split[0];
            String fieldInfo = split[1];

            if (fieldName.equals("Header")) {
                this.header = fieldInfo;
            }
            else {
                failureInfo.add(new AbstractMap.SimpleEntry<>(fieldName, fieldInfo));
            }
        }
    }

    String writeFailure(ClassFields fields, String superClass, InputFormatter inputFormatter) {
        String failure = StringUtils.empty();
        boolean first = true;

        for (Map.Entry<String, String> entry : failureInfo) {
            String fieldName = entry.getKey();
            String fieldInfo = entry.getValue();

            SplitScanner split = new SplitScanner(fieldInfo);

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

            String fieldValue = fields.get(fieldName);
            if (fieldValue == null) {
                if (!not) {
                    continue;
                }

                fieldValue = defaultValue;
            }
            else if (not) {
                fields.remove(fieldName);
                continue;
            }

            final String[] fieldValues;
            if (list) {
                fieldValues = fieldValue.split(",");
            } else {
                fieldValues = new String[] { fieldValue };
            }

            split.setTempIndex();
            for (String value : fieldValues) {
                split.restoreTempIndex();

                String pairValue = inputFormatter.getValue(split, value, fieldType);

                String body = split.getRemaining();
                body = inputFormatter.replaceBody(body, pairValue, fields.getClassName(), superClass);

                failure += (first ? "" : " || ")  + body;
                first = false;
            }

            fields.remove(fieldName);
        }

        if (failure.isEmpty()) {
            return StringUtils.empty();
        }

        failure = "return !(" + failure + ");";
        return new MethodInfo(this.header, failure).writeFunction();
    }
}
