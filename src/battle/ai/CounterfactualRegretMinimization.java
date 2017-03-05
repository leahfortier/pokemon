package battle.ai;

import battle.Battle;
import battle.ai.InformationSet.ActionInfo;
import battle.attack.Move;
import pokemon.ActivePokemon;
import trainer.Team;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CounterfactualRegretMinimization {

    Set<InformationSet> allInfoSets = new HashSet<InformationSet>();

    public double update(History history, boolean isPlayer, double p1, double p2) {
        if (history.isTerminal()) {
            if (isPlayer) {
                return history.playerWon() ? 1 : 0;
            } else {
                return history.opponentWon() ? 1 : 0;
            }
        }

        InformationSet informationSet = history.getInformationSet(allInfoSets);

        Battle battle = history.getBattle();
        Team current = battle.getTrainer(isPlayer);
        ActivePokemon front = current.front();
        List<Move> moves = front.getMoves(battle);

        informationSet.updateStrategy(isPlayer ? p1 : p2);

        // Update utility
        for (Move move : moves) {
            History nextHistory = new History(battle);
            if (isPlayer) {
                nextHistory.setPlayerMove(move);
            } else {
                nextHistory.setOpponentMove(history.getPlayerMove());
                nextHistory.setOpponentMove(move);

                nextHistory.execute();
            }

            ActionInfo actionInfo = informationSet.actionInfoMap.get(move);
            if (isPlayer) {
                actionInfo.utility = update(nextHistory, false, p1*actionInfo.strategy, p2);
            } else {
                actionInfo.utility = update(nextHistory, true, p1, p2*actionInfo.strategy);
            }

            informationSet.nodeUtility += actionInfo.strategy*actionInfo.utility;
        }

        // Update cumulative regret
        for (Move move : moves) {
            ActionInfo actionInfo = informationSet.actionInfoMap.get(move);
            double regret = actionInfo.utility - informationSet.nodeUtility;
            actionInfo.cumulativeRegret += (isPlayer ? p2 : p1)*regret;
        }

        return informationSet.nodeUtility;
    }
}
