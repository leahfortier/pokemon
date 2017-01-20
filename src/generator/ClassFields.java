package generator;

import main.Global;
import util.StringUtils;

import java.util.HashMap;
import java.util.Map;

class ClassFields {
    static final String CLASS_NAME_FIELD = "ClassName";
    static final String FIELD_FIELD = "Field";
    static final String INTERFACE_FIELD = "Int";
    private final Map<String, String> fields;

    ClassFields() {
        this.fields = new HashMap<>();
    }

    void add(String fieldName, String value) {
        fields.put(fieldName, value);
    }

    String get(String fieldName) {
        if (fields.containsKey(fieldName)) {
            return fields.get(fieldName);
        }

        return null;
    }

    String remove(String fieldName) {
        return fields.remove(fieldName);
    }

    void addNew(String fieldName, String value, String className) {
        if (fields.containsKey(fieldName)) {
            Global.error("Repeated field " + fieldName + " for " + className);
        }

        this.add(fieldName, value);
    }

    String getRequired(String fieldName) {
        if (!fields.containsKey(fieldName)) {
            Global.error("Missing required field " + fieldName);
        }

        return this.get(fieldName);
    }

    String getAndRemove(String fieldName) {
        if (fields.containsKey(fieldName)) {
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
        if (fields.containsKey(fieldName)) {
            action.performAction(fields.get(fieldName));

            fields.remove(fieldName);
            return true;
        }

        return false;
    }

    void confirmEmpty(String className) {
        for (String field : fields.keySet()) {
            Global.error("Unused field " + field + " for class " + className);
        }
    }

    interface ActionPerformer {
        void performAction(String value);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (String key : fields.keySet()) {
            StringUtils.appendLine(builder, key + " -> " + fields.get(key));
        }

        return builder.toString();
    }
}
