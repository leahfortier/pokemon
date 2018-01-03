package test;

import org.junit.BeforeClass;
import trainer.player.Player;
import util.RandomUtils;

public class BaseTest {
    private static boolean initialized = false;

    @BeforeClass
    public static void initTests() {
        if (initialized) {
            return;
        }

        System.out.println("Random Seed: " + RandomUtils.getSeed());
        TestGame.setNewPlayer(new Player());

        initialized = true;
    }
}
