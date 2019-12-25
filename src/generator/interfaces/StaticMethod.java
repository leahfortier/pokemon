package generator.interfaces;

import generator.AccessModifier;
import generator.fields.ClassFields;
import generator.format.MethodInfo;
import util.string.StringAppender;
import util.string.StringUtils;

public class StaticMethod {
    private final String comments;
    private final String header;
    private final String body;

    StaticMethod(ClassFields fields) {
        this.comments = fields.getAndRemoveTrimmed("Comments");
        this.header = fields.getAndRemoveTrimmed("Header");
        this.body = fields.getAndRemoveTrimmed("Body");

        fields.confirmEmpty();
    }

    String writeStaticMethod() {
        StringAppender staticMethod = new StringAppender();
        if (!StringUtils.isNullOrEmpty(this.comments)) {
            staticMethod.append("\n\t\t" + this.comments);
        }

        MethodInfo methodInfo = new MethodInfo("static " + this.header, this.body, AccessModifier.PACKAGE_PRIVATE);
        staticMethod.append(methodInfo.writeFunction());

        return staticMethod.toString();
    }
}
