package test;

import generator.InputFormatter;
import generator.StuffGen;
import org.junit.Assert;
import org.junit.Test;

public class GeneratorTest extends InputFormatter {
    @Test
    public void testGenerator() {
        new StuffGen(new GeneratorTest());
    }

    @Override
    protected String replaceBody(String body, String original, String remaining, int parameterIndex, int numParameters) {
        for (ReplaceType replaceType : ReplaceType.values()) {
            String newBody = replaceType.replaceBody(body, original, remaining, parameterIndex, numParameters);
            if (!body.equals(newBody)) {
                if (replaceType == ReplaceType.FINISH) {
                    Assert.assertTrue("Don't use {" + parameterIndex + "-}, use {0} instead.", parameterIndex != 0 && parameterIndex != 1);
                }

                body = newBody;
            }
        }

        return body;
    }
}
