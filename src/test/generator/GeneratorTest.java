package test.generator;

import generator.StuffGen;
import org.junit.Test;
import test.BaseTest;

public class GeneratorTest extends BaseTest {
    @Test
    public void testGenerator() {
        new StuffGen(new TestFormatter());
    }
}
