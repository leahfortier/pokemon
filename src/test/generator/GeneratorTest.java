package test.generator;

import generator.PokeGen;
import org.junit.Assert;
import org.junit.Test;
import test.BaseTest;
import util.file.FileIO;
import util.string.StringUtils;

public class GeneratorTest extends BaseTest {
    @Test
    public void pokeGenTest() {
        new TestPokeGen();
    }

    public static class TestPokeGen extends PokeGen {

        public TestPokeGen() {
            super(new TestFormatter());
        }

        @Override
        protected void writeGen(String fileName, String contents) {
            // Don't actually overwrite files inside the test
            contents = FileIO.getOverwriteContents(fileName, contents);
            Assert.assertTrue(fileName, StringUtils.isNullOrEmpty(contents));
        }
    }
}
