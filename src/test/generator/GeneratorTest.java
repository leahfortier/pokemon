package test.generator;

import generator.StuffGen;
import org.junit.Test;

public class GeneratorTest {
    @Test
    public void testGenerator() {
        new StuffGen(new TestFormatter());
    }
}
