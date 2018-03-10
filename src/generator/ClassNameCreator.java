package generator;

import util.string.StringAppender;

import java.util.Arrays;

class ClassNameCreator {
    private final Class<?> className;
    private final Class<?>[] parameters;

    public ClassNameCreator(Class<?> className, Class<?>... parameters) {
        this.className = className;
        this.parameters = parameters;
    }

    // Returns the base class without any parameters
    // Ex: Map
    public String getBaseClassName() {
        return this.className.getSimpleName();
    }

    // Return the base class with all type parameters
    // Ex: Map<String, Integer>
    public String getFullClassName() {
        String className = this.className.getSimpleName();
        if (parameters.length > 0) {
            className += new StringAppender("<")
                    .appendJoin(", ", Arrays.asList(parameters), Class::getSimpleName)
                    .append(">");
        }
        return className;
    }
}
