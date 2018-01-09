package test.generator;

import generator.ClassFields;
import generator.format.InputFormatter;
import generator.format.ReplaceType;
import org.junit.Assert;
import util.StringUtils;

class TestFormatter extends InputFormatter {
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

    @Override
    public void validate(ClassFields fields) {
        String minTurnsString = fields.get("MinTurns");
        String maxTurnsString = fields.get("MaxTurns");

        boolean minNull = StringUtils.isNullOrEmpty(minTurnsString);
        boolean maxNull = StringUtils.isNullOrEmpty(maxTurnsString);

        // If one is present, both must be present
        Assert.assertEquals(minNull, maxNull);

        if (!minNull) {
            int minTurns = Integer.parseInt(minTurnsString);
            int maxTurns = Integer.parseInt(maxTurnsString);
            Assert.assertTrue(minTurns < maxTurns);

            Assert.assertFalse(fields.contains("NumTurns"));
        }
    }
}
