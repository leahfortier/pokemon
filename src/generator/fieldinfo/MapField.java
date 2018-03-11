package generator.fieldinfo;

import generator.format.MethodFormatter;
import main.Global;
import util.string.StringAppender;
import util.string.StringUtils;

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
        String value = split[1].trim();
        if (value.isEmpty()) {
            value = readMethod(in);
        } else if (value.equals("<Empty>")) {
            value = StringUtils.empty();
        }

        this.fieldName = key;
        this.fieldValue = value;
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
