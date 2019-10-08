package generator.constructor;

import generator.AccessModifier;
import generator.fields.ClassFields;
import generator.format.MethodInfo;
import util.string.StringAppender;

public class ConstructorInfo {
    // Contains the information of what needs to be passed in the super constructor (required fields)
    private final ConstructorFieldList superKeys;

    // Contains the information of (optional) fields that have manual field setting after super call
    private final ConstructorFieldList fieldKeys;

    public ConstructorInfo(ConstructorFieldList superKeys, ConstructorFieldList fieldKeys) {
        this.superKeys = superKeys;
        this.fieldKeys = fieldKeys;
    }

    // For the super call inside the class constructor, returns the comma-separated field values
    // (It's what's inside the super parentheses)
    // Example: 'AttackNamesies.ATTACK_NAME, "Attack Description", 35, Type.NORMAL, MoveCategory.PHYSICAL'
    private String getInternalConstructorValues(ClassFields fields) {
        StringAppender superValues = new StringAppender();
        for (ConstructorField constructorField : superKeys) {
            String value = constructorField.getConstructorValue(fields);
            superValues.appendDelimiter(", ", value);
        }

        return superValues.toString();
    }

    public String getConstructor(ClassFields fields) {
        StringAppender constructor = new StringAppender();
        constructor.appendLine("super(" + getInternalConstructorValues(fields) + ");");

        for (ConstructorField constructorField : fieldKeys) {
            fields.getPerformAndRemove(
                    constructorField.getFieldName(),
                    value -> constructor.appendLine(constructorField.getAssignment(value))
            );
        }

        fields.getPerformAndRemove("StatChange", value -> {
            String[] mcSplit = value.split(" ");
            for (int i = 0, index = 1; i < Integer.parseInt(mcSplit[0]); i++) {
                constructor.append("super.statChanges[Stat.")
                           .append(mcSplit[index++].toUpperCase())
                           .append(".index()] = ")
                           .append(mcSplit[index++])
                           .append(";\n");
            }
        });

        fields.getPerformAndRemove("Activate", constructor::append);

        return new MethodInfo(
                fields.getClassName() + "()",
                constructor.toString(),
                AccessModifier.PACKAGE_PRIVATE
        ).writeFunction();
    }
}
