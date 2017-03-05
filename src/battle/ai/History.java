package battle.ai;

import battle.Battle;
import battle.attack.Move;
import util.SerializationUtils;

import java.util.Set;

public class History {
    private final Battle serialized;
    private Move playerPending;
    private Move opponentPending;

    public History(Battle b) {
        this.serialized = (Battle)SerializationUtils.deserialize(SerializationUtils.serialize(b));
    }

    public Move getPlayerMove() {
        return playerPending;
    }

    public void setPlayerMove(Move playerPending) {
        this.playerPending = (Move)SerializationUtils.deserialize(SerializationUtils.serialize(playerPending));
    }

    public void setOpponentMove(Move opponentPending) {
        this.opponentPending = (Move)SerializationUtils.deserialize(SerializationUtils.serialize(opponentPending));
    }

    public void execute() {
        serialized.getPlayer().front().setMove(playerPending);
        serialized.getOpponent().front().setMove(opponentPending);
        serialized.fight();

        playerPending = null;
        opponentPending = null;
    }

    public Battle getBattle() {
        return serialized;
    }

    public boolean playerWon() {
        return serialized.deadOpponent();
    }

    public boolean opponentWon() {
        return serialized.deadUser();
    }

    public boolean isTerminal() {
        return playerWon() || opponentWon();
    }

    public InformationSet makeInfoSet(){
        InformationSet newis = new InformationSet();
        newis.knownMoves = null; //TODO: get all moves used from battle


        //TODO: set health levels from battle
        return newis;
    }
    public InformationSet getInformationSet(Set<InformationSet> allInfoSets) {
        InformationSet newis = makeInfoSet();
        if(allInfoSets.contains(newis)){
            for(InformationSet is : allInfoSets){
                if(is.equals(newis)){
                    return is;
                }
            }
        }

        return newis;
    }
}
