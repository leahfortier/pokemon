package generator.fieldinfo;

import generator.AccessModifier;
import generator.ClassFields;
import generator.format.MethodInfo;
import util.StringAppender;

public class ConstructorInfo {
    private final InfoList superInfo;
    private final InfoList fieldKeys;

    public ConstructorInfo(InfoList superInfo, InfoList fieldKeys) {
        this.superInfo = superInfo;
        this.fieldKeys = fieldKeys;
    }

    // For the super call inside the class constructor, returns the comma-separated field values
    // (It's what's inside the super parentheses)
    // Example: 'AttackNamesies.ATTACK_NAME, "Attack Description", 35, Type.NORMAL, MoveCategory.PHYSICAL'
    private String getInternalConstructorValues(ClassFields fields) {
        StringAppender superValues = new StringAppender();
        for (FieldInfo fieldInfo : superInfo.infoList) {
            String value = fieldInfo.getConstructorValue(fields);
            superValues.appendDelimiter(", ", value);
        }

        return superValues.toString();
    }

    public String getConstructor(ClassFields fields) {
        StringAppender constructor = new StringAppender();
        constructor.appendLine("super(" + getInternalConstructorValues(fields) + ");");

        for (FieldInfo fieldInfo : fieldKeys.infoList) {
            fields.getPerformAndRemove(
                    fieldInfo.fieldName,
                    value -> constructor.appendLine(fieldInfo.getAssignment(value))
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
