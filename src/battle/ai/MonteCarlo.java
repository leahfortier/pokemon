package battle.ai;

import battle.Battle;
import battle.attack.Move;

import java.util.List;

public class MonteCarlo {
    public Move next(Battle battle) {
    }

    public static class Node {
        List<Move> path;
        boolean isPlayer;
        double utility;
    }
}
