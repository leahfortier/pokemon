package battle.attack;

import battle.Battle;
import battle.ai.DecisionTree;
import battle.effect.attack.MultiTurnMove;
import battle.effect.generic.EffectInterfaces.AttackSelectionEffect;
import battle.effect.generic.EffectInterfaces.ForceMoveEffect;
import battle.effect.generic.EffectInterfaces.OpponentAttackSelectionEffect;
import message.Messages;
import pokemon.ActivePokemon;
import type.Type;
import util.RandomUtils;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class Move implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final int MAX_MOVES = 4;
	
	private AttackNamesies attack;
	private int maxPP;
	private int pp;
	
	private boolean ready;
	private boolean used;

	private Type type;

	public Move(AttackNamesies attackNamesies) {
		this(attackNamesies.getAttack());
	}
	
	public Move(Attack attack) {
		this.attack = attack.namesies();
		
		maxPP = attack.getPP();
		pp = maxPP;
		
		resetReady();
		used = false;

		type = attack.getActualType();
	}
	
	public Move(Attack m, int startPP) {
		this(m);
		pp = startPP;
	}
	
	public void resetPP() {
		pp = maxPP;
	}
	
	public void resetReady() {
		Attack attack = this.getAttack();
		ready = !(attack instanceof MultiTurnMove) || ((MultiTurnMove) attack).chargesFirst();
	}
	
	public boolean isReady() {
		return ready;
	}
	
	public void switchReady(Battle b, ActivePokemon user) {
		if (this.getAttack().isMultiTurn(b, user)) {
			ready = !ready;
		}
	}

	public Type getType() {
		return type;
	}
	
	public void setAttributes(Battle b, ActivePokemon user) {
		type = this.getAttack().getBattleType(b, user);
	}
	
	public Attack getAttack() {
		return attack.getAttack();
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
	
	public int reducePP(int reduce) {
		return pp - (pp = Math.max(0, pp - reduce));
	}
	
	public boolean increasePP(int n) {
		if (maxPP == pp) {
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
	
	public static Move selectOpponentMove(Battle b, ActivePokemon p) {
		if (forceMove(b, p)) {
			return p.getMove();
		}
		
		List<Move> usable = getUsableMoves(b, p);
		if (usable.size() == 0) {
			return new Move(AttackNamesies.STRUGGLE.getAttack());
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

		// Force second turn of a Multi-Turn Move
		if (p.getMove() != null && p.getAttack().isMultiTurn(b, p)) {
			MultiTurnMove multiTurnMove = (MultiTurnMove)p.getAttack();
			if (multiTurnMove.forceMove(p)) {
				return true;
			}
		}

		if (p.isPlayer() && getUsableMoves(b, p).size() == 0) {
			p.setMove(new Move(AttackNamesies.STRUGGLE.getAttack()));
			return true;
		}
		
		return false;
	}
	
	// Returns a list of the moves that are valid for the pokemon to use
	private static List<Move> getUsableMoves(Battle b, ActivePokemon p) {
		return p.getMoves(b).stream()
				.filter(m -> validMove(b, p, m, false))
				.collect(Collectors.toList());
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
		if (unusable == null) {
			unusable = OpponentAttackSelectionEffect.getUnusableEffect(b, p, m);
		}

		if (unusable != null) {
			if (selecting) {
				Messages.add(unusable.getUnusableMessage(b, p));
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
			return new DecisionTree(b, usable).next();
		}
	}
}
