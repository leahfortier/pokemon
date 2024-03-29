package battle.attack;

import battle.ActivePokemon;
import battle.Battle;
import battle.ai.DecisionTree;
import battle.effect.InvokeInterfaces.AttackSelectionEffect;
import battle.effect.InvokeInterfaces.ForceMoveEffect;
import message.Messages;
import util.RandomUtils;
import util.serialization.Serializable;

import java.util.List;

public class Move implements Serializable {
    private static final long serialVersionUID = 1L;

    private Attack attack;
    private int maxPP;
    private int pp;

    private boolean used;

    private MoveTurnData turnData;

    public Move(AttackNamesies attackNamesies) {
        this(attackNamesies.getNewAttack());
    }

    public Move(Attack attack) {
        this.attack = attack;

        maxPP = attack.getPP();
        pp = maxPP;

        resetAttack();
        used = false;

        turnData = new MoveTurnData(attack.namesies());
    }

    public Move(Attack attack, int startPP) {
        this(attack);
        pp = startPP;
    }

    public void resetPP() {
        pp = maxPP;
    }

    public void resetAttack() {
        this.attack = this.getAttack().namesies().getNewAttack();
    }

    // Should be called at the beginning of the full turn (not at beginning of attack)
    public void startTurn(Battle b, ActivePokemon user) {
        this.turnData().startTurn(b, user);
        this.getAttack().startTurn(b, user);
    }

    public MoveTurnData turnData() {
        return this.turnData;
    }

    public Attack getAttack() {
        return attack;
    }

    public void setUsed() {
        used = true;
    }

    public boolean used() {
        return used;
    }

    public int getPP() {
        return pp;
    }

    public int getMaxPP() {
        return maxPP;
    }

    public String getPPString() {
        return this.getPP() + "/" + this.getMaxPP();
    }

    public int reducePP(int reduce) {
        return pp - (pp = Math.max(0, pp - reduce));
    }

    public boolean increasePP(int n) {
        if (pp == maxPP) {
            return false;
        }

        pp = Math.min(maxPP, pp + n);
        return true;
    }

    public boolean increaseMaxPP(int n) {
        Attack attack = getAttack();
        int trueMax = attack.getPP() + 3*attack.getPP()/5;

        if (maxPP == trueMax) {
            return false;
        }

        maxPP += n*attack.getPP()/5;

        if (maxPP > trueMax) {
            maxPP = trueMax;
        }

        return true;
    }

    @Override
    public String toString() {
        return this.getAttack().getName() + " " + this.getPP();
    }

    public static Move selectOpponentMove(Battle b, ActivePokemon p) {
        if (forceMove(b, p)) {
            return p.getMove();
        }

        List<Move> usable = getUsableMoves(b, p);
        if (usable.size() == 0) {
            return new Move(AttackNamesies.STRUGGLE);
        }

        return chooseMove(b, usable);
    }

    // Returns true if a move should be forced (move will already be selected for the Pokemon), and false if not
    public static boolean forceMove(Battle b, ActivePokemon p) {

        // TODO: Why are most of the forced move effects also attack selection effects? if the move if being forced, then the attack selection menu should not appear -- check if this is working
        // Forced moves
        Move forcedMove = ForceMoveEffect.getForcedMove(b, p);
        if (forcedMove != null) {
            p.setMove(forcedMove);
            return true;
        }

        // TODO: Why just the player
        if (p.isPlayer() && getUsableMoves(b, p).isEmpty()) {
            p.setMove(new Move(AttackNamesies.STRUGGLE));
            return true;
        }

        return false;
    }

    // Returns a list of the moves that are valid for the pokemon to use
    private static List<Move> getUsableMoves(Battle b, ActivePokemon p) {
        return p.getMoves(b).filter(m -> validMove(b, p, m, false));
    }

    // Will return whether or not p can execute m
    // if selecting is true: if yes (to above line), it will set m to be p's move, if no, the battle should display why
    public static boolean validMove(Battle b, ActivePokemon p, Move m, boolean selecting) {
        // Invalid if PP is zero
        if (m.getPP() == 0) {
            if (selecting) {
                Messages.add(p.getName() + " is out of PP for " + m.attack.getName() + "!");
            }

            return false;
        }

        // BUT WHAT IF YOU HAVE A CONDITION THAT PREVENTS YOU FROM USING THAT MOVE?!!?! THEN WHAT?!!?!!
        AttackSelectionEffect unusable = AttackSelectionEffect.getUnusableEffect(b, p, m);
        if (unusable != null) {
            if (selecting) {
                Messages.add(unusable.getUnusableMessage(p));
            }

            // THAT'S WHAT
            return false;
        }

        // Set the move if selecting
        if (selecting) {
            p.setMove(m);
        }

        return true;
    }

    private static Move chooseMove(Battle b, List<Move> usable) {
        // Wild pokemon attack randomly
        if (b.isWildBattle()) {
            return RandomUtils.getRandomValue(usable);
        } else {
            // and so do trainers because fuck ai
            return RandomUtils.getRandomValue(usable);
//            return new DecisionTree(b, usable).next();
        }
    }
}
