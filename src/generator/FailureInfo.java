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

    String writeFailure(Map<String, String> fields, String superClass) {
        String failure = StringUtils.empty();

        String className = fields.get("ClassName");
        boolean first = true;

        for (Map.Entry<String, String> entry : failureInfo) {
            String fieldName = entry.getKey();
            String fieldInfo = entry.getValue();

            int index = 0;
            String[] split = fieldInfo.split(" ");

            boolean not = false;
            boolean list = false;

            String fieldType = split[index++];
            if (fieldType.equals("List")) {
                list = true;
                fieldType = split[index++];
            }

            if (fieldType.equals("Not")) {
                not = true;
                fieldType = split[index++];
            }

            String defaultValue = "";
            if (fieldType.equals("Default")) {
                defaultValue = split[index++];
                fieldType = split[index++];
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

            String[] fieldValues = new String[] {fieldValue};
            if (list) {
                fieldValues = fieldValue.split(",");
            }

            int previousIndex = index;

            for (String value : fieldValues) {
                index = previousIndex;

                Map.Entry<Integer, String> pair = InputFormatter.instance().getValue(split, value, index);
                index = pair.getKey();
                String pairValue = pair.getValue();

                String body = "";
                boolean space = false;

                for (; index < split.length; index++) {
                    body += (space ? " " : "") + split[index];
                    space = true;
                }

                body = InputFormatter.instance().replaceBody(body, pairValue, className, superClass);

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
