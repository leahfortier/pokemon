package test.generator;

import generator.fields.ClassFields;
import generator.format.InputFormatter;
import generator.format.MethodInfo;
import generator.format.ReplaceType;
import org.junit.Assert;
import util.string.StringUtils;

import java.util.HashSet;
import java.util.Set;

class TestFormatter extends InputFormatter {
    private Set<String> unusedOverrides;

    public TestFormatter() {
        this.unusedOverrides = new HashSet<>();
    }

    @Override
    protected void addMethod(String fieldName, MethodInfo methodInfo) {
        Assert.assertFalse("Duplicate field name " + fieldName + " in override.txt", this.unusedOverrides.contains(fieldName));

        super.addMethod(fieldName, methodInfo);
        this.unusedOverrides.add(fieldName);
    }

    @Override
    public void validate(ClassFields fields) {
        String minTurnsString = fields.get("MinTurns");
        String maxTurnsString = fields.get("MaxTurns");

        boolean hasMin = !StringUtils.isNullOrEmpty(minTurnsString);
        boolean hasMax = !StringUtils.isNullOrEmpty(maxTurnsString);

        // If one is present, both must be present
        Assert.assertEquals(hasMin, hasMax);

        if (hasMin) {
            int minTurns = Integer.parseInt(minTurnsString);
            int maxTurns = Integer.parseInt(maxTurnsString);

            // Min should be less than max
            Assert.assertTrue(minTurns <= maxTurns);
        }

        // Reserved field name and should not be manually set
        Assert.assertFalse(fields.contains("Namesies"));
    }

    @Override
    public void useOverride(String overrideName) {
        this.unusedOverrides.remove(overrideName);
    }

    @Override
    protected String replaceBody(String body, String original, String remaining, int parameterIndex) {
        for (ReplaceType replaceType : ReplaceType.values()) {
            String newBody = replaceType.replaceBody(body, original, remaining, parameterIndex);
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
    public void close() {
        for (String fieldName : this.unusedOverrides) {
            Assert.fail("Unused field " + fieldName + " in override.txt");
        }
    }
}
