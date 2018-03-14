package test.generator;

import generator.PokeGen;
import org.junit.Test;
import test.BaseTest;

public class GeneratorTest extends BaseTest {
    @Test
    public void pokeGenTest() {
        new PokeGen(new TestFormatter());
    }
}
