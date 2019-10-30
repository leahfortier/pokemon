package test.general;

import org.junit.BeforeClass;
import util.RandomUtils;

public class BaseTest {
    private static boolean initialized = false;

    @BeforeClass
    public static void initTests() {
        if (initialized) {
            return;
        }

        System.out.println("Random Seed: " + RandomUtils.getSeed());
        TestGame.setNewPlayer();

        initialized = true;
    }
}
