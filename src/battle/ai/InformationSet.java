package battle.ai;

import battle.attack.Move;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;

import java.util.Map;
import java.util.Set;

public class InformationSet {
    public Map<Move, ActionInfo> actionInfoMap;
    double nodeUtility;

    public Set<Move> knownMoves;
    public int playerHealthLevel;
    public int opponentHealthLevel;
    public ActivePokemon activepoke;

    public boolean equals(Object other){
        if(other instanceof  InformationSet){
            InformationSet other2 = ((InformationSet) other);
            if(other2.knownMoves.equals(knownMoves) && other2.playerHealthLevel == playerHealthLevel && other2.opponentHealthLevel == opponentHealthLevel && other2.activepoke == isPlayer){
                return true;
            }
        }
        return false;
    }

    public void updateStrategy(double probability) {
        double normalizingSum = 0;
        for (Move move : actionInfoMap.keySet()) {
            ActionInfo actionInfo = actionInfoMap.get(move);
            actionInfo.strategy = actionInfo.cumulativeRegret > 0 ? actionInfo.cumulativeRegret : 0;
            normalizingSum += actionInfo.strategy;
        }

        for (Move move : actionInfoMap.keySet()) {
            ActionInfo actionInfo = actionInfoMap.get(move);
            if (normalizingSum > 0) {
                actionInfo.strategy /= normalizingSum;
            } else {
                actionInfo.strategy = 1.0/actionInfoMap.size();
            }

            actionInfo.cumulativeStrategy += probability*actionInfo.strategy;
        }
    }

    static class ActionInfo {
        double strategy;
        double cumulativeStrategy;

        double regret;
        double cumulativeRegret;

        double utility;
    }
}
