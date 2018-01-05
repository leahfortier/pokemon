package generator;

import main.Global;

enum AccessModifier {
    PUBLIC("public"),
    PRIVATE("private"),
    PROTECTED("protected"),
    PACKAGE_PRIVATE(""),
    DEFAULT("default"); // Technically not an access modifier, but works here all the same yeah yeah yeah I suck
    
    private final String modifierName;
    
    AccessModifier(final String modifierName) {
        this.modifierName = modifierName;
    }
    
    String getModifierName() {
        return this.modifierName;
    }
    
    static AccessModifier getAccessModifier(final String modifierName) {
        switch (modifierName.toLowerCase()) {
            case "public":
                return PUBLIC;
            case "private":
                return PRIVATE;
            case "protected":
                return PROTECTED;
            case "package-private":
            case "none":
                return PACKAGE_PRIVATE;
            default:
                Global.error("Unknown access modifier name " + modifierName + ".");
                return PUBLIC;
        }
    }
}
