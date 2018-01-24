package generator;

import main.Global;
import util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ClassFields {
    private final Map<String, String> fields;
    private String className;

    ClassFields() {
        this.fields = new HashMap<>();
    }

    public String getClassName() {
        if (StringUtils.isNullOrEmpty(this.className)) {
            Global.error("Class name not set yet -- cannot retrieve.");
        }

        return this.className;
    }

    void setClassName(String className) {
        if (!StringUtils.isNullOrEmpty(this.className) && !this.className.equals(className)) {
            Global.error("Class name already set to " + this.className + ", New value: " + className + ")");
        }

        this.className = className;
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

    public void addNew(String fieldKey, String addFieldValue) {
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

    @FunctionalInterface
    public interface ActionPerformer {
        void performAction(String value);
    }
}
