package generator;

import generator.fieldinfo.MapField;
import main.Global;
import util.string.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ClassFields {
    private final Map<String, String> fields;

    private final String name;
    private final String className;

    ClassFields(String name) {
        this.fields = new HashMap<>();

        this.name = name;
        this.className = StringUtils.getClassName(name);
    }

    public ClassFields(Scanner in, String name) {
        this(name);
        while (in.hasNextLine()) {
            String line = in.nextLine().trim();
            if (line.equals("*")) {
                break;
            }

            this.addNew(new MapField(in, line));
        }
    }

    public String getName() {
        return this.name;
    }

    public String getClassName() {
        return this.className;
    }

    public boolean contains(String fieldName) {
        return fields.containsKey(fieldName);
    }

    public String get(String fieldName) {
        if (this.contains(fieldName)) {
            return fields.get(fieldName);
        }

        return null;
    }

    public String remove(String fieldName) {
        return fields.remove(fieldName);
    }

    public void addNew(MapField mapField) {
        this.addNew(mapField.fieldName, mapField.fieldValue);
    }

    public void addNew(String fieldKey, String addFieldValue) {
        // NumTurns matches to both MinTurns and MaxTurns
        if (fieldKey.equals("NumTurns")) {
            this.addNew("MinTurns", addFieldValue);
            this.addNew("MaxTurns", addFieldValue);
            return;
        }

        String mapField = fields.get(fieldKey);
        if (mapField == null) {
            mapField = addFieldValue;
        } else if (fieldKey.equals("MoveType")) {
            mapField += ", " + addFieldValue;
        } else if (fieldKey.equals("Applies")) {
            mapField += " && " + addFieldValue;
        } else if (fieldKey.equals("Field") || fieldKey.equals("UniqueEffects")) {
            mapField += addFieldValue;
        } else if (!fieldKey.equals("Price") && className.equals("RazzBerry")) {
            // Don't worry about the special case here ^^
            Global.error("Unauthorized duplicate field " + fieldKey + " in class " + className + ".\n" +
                                 "Prev:\n" + mapField + "\nNew:\n" + addFieldValue);
        }

        fields.put(fieldKey, mapField);
    }

    String getRequired(String fieldName) {
        if (!this.contains(fieldName)) {
            Global.error("Missing required field " + fieldName + " for class " + className);
        }

        return this.get(fieldName);
    }

    public String getAndRemove(String fieldName) {
        if (this.contains(fieldName)) {
            String value = fields.get(fieldName);
            fields.remove(fieldName);
            return value;
        } else {
            return null;
        }
    }

    public String getAndRemoveTrimmed(String fieldName) {
        String value = getAndRemove(fieldName);
        if (value == null) {
            return null;
        }

        return value.trim();
    }

    public boolean getPerformAndRemove(String fieldName, ActionPerformer action) {
        if (this.contains(fieldName)) {
            action.performAction(fields.get(fieldName));

            fields.remove(fieldName);
            return true;
        }

        return false;
    }

    public void confirmEmpty() {
        for (String field : fields.keySet()) {
            Global.error("Unused field " + field + " for class " + this.className);
        }
    }

    public Iterable<String> getFieldNames() {
        return new ArrayList<>(this.fields.keySet());
    }

    @FunctionalInterface
    public interface ActionPerformer {
        void performAction(String value);
    }
}
