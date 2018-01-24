package generator.fieldinfo;

import generator.format.SplitScanner;
import util.StringUtils;

public class FieldInfo {
    public final String fieldName;
    public final boolean not;
    public final boolean list;
    public final String defaultValue;
    public final String fieldType;
    public final SplitScanner split;

    public FieldInfo(SplitScanner split) {
        this.split = split;
        this.fieldName = split.next();

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
