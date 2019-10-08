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

    // Optional fields that are set manually after the call to the super constructor
    // Ex: 'super.power = 40;
    //      super.accuracy = 100;'
    private String getOptionalFieldAssignments(ClassFields fields) {
        StringAppender fieldAssignments = new StringAppender();
        for (ConstructorField constructorField : fieldKeys) {
            fields.getPerformAndRemove(
                    constructorField.getFieldName(),
                    value -> fieldAssignments.appendLine(constructorField.getAssignment(value))
            );
        }

        // Stat changes too complicated to be handled by ConstructorField
        fields.getPerformAndRemove("StatChange", value -> {
            String[] mcSplit = value.split(" ");
            for (int i = 0, index = 1; i < Integer.parseInt(mcSplit[0]); i++) {
                fieldAssignments.append("super.statChanges[Stat.")
                           .append(mcSplit[index++].toUpperCase())
                           .append(".index()] = ")
                           .append(mcSplit[index++])
                           .append(";\n");
            }
        });

        return fieldAssignments.toString();
    }

    public String getConstructor(ClassFields fields) {
        StringAppender constructor = new StringAppender();

        // Call to super constructor
        constructor.appendLine("super(" + this.getInternalConstructorValues(fields) + ");");

        // Optional field assignments
        constructor.append(this.getOptionalFieldAssignments(fields));

        // Additional unique constructor specifications
        fields.getPerformAndRemove("Activate", constructor::append);

        // Put it all together!
        return new MethodInfo(
                fields.getClassName() + "()",
                constructor.toString(),
                AccessModifier.PACKAGE_PRIVATE
        ).writeFunction();
    }
}