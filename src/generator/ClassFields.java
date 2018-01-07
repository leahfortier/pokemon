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

    String getClassName() {
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

    void add(String fieldName, String value) {
        fields.put(fieldName, value);
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

    String remove(String fieldName) {
        return fields.remove(fieldName);
    }

    void addNew(String fieldName, String value) {
        if (this.contains(fieldName)) {
            Global.error("Repeated field " + fieldName + " for " + this.className);
        }

        this.add(fieldName, value);
    }

    String getRequired(String fieldName) {
        if (!this.contains(fieldName)) {
            Global.error("Missing required field " + fieldName);
        }

        return this.get(fieldName);
    }

    String getAndRemove(String fieldName) {
        if (this.contains(fieldName)) {
            String value = fields.get(fieldName);
            fields.remove(fieldName);
            return value;
        } else {
            return null;
        }
    }

    String getAndRemoveTrimmed(String fieldName) {
        String value = getAndRemove(fieldName);
        if (value == null) {
            return null;
        }

        return value.trim();
    }

    boolean getPerformAndRemove(String fieldName, ActionPerformer action) {
        if (this.contains(fieldName)) {
            action.performAction(fields.get(fieldName));

            fields.remove(fieldName);
            return true;
        }

        return false;
    }

    void confirmEmpty() {
        for (String field : fields.keySet()) {
            Global.error("Unused field " + field + " for class " + this.className);
        }
    }

    @FunctionalInterface
    interface ActionPerformer {
        void performAction(String value);
    }
}
