package generator.fieldinfo;

import generator.format.MethodFormatter;
import main.Global;
import util.string.StringAppender;

import java.util.Scanner;

public class MapField {
    public final String fieldName;
    public final String fieldValue;

    public MapField(Scanner in, String line) {
        String[] split = line.split(":", 2);
        if (split.length != 2) {
            Global.error("Field key and value must be separated by a colon: " + line);
        }

        String key = split[0].trim();
        String value = this.readValue(in, key, split[1].trim());

        this.fieldName = key;
        this.fieldValue = value;
    }

    // value parameter is the remainder of the line with the key
    protected String readValue(Scanner in, String key, String value) {
        if (value.isEmpty()) {
            return readMethod(in);
        } else if (value.equals("<Empty>")) {
            return  "";
        } else {
            return value;
        }
    }

    private String readMethod(Scanner in) {
        StringAppender method = new StringAppender();
        MethodFormatter formatter = new MethodFormatter(2);

        while (in.hasNext()) {
            String line = in.nextLine().trim();
            if (line.equals("###")) {
                break;
            }

            formatter.appendLine(line, method);
        }

        return method.toString();
    }
}
