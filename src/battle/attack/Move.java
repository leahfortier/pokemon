package battle.attack;

import battle.Battle;
import battle.effect.attack.MultiTurnMove;
import battle.effect.generic.EffectInterfaces.AttackSelectionEffect;
import battle.effect.generic.EffectInterfaces.ChangeAttackTypeEffect;
import battle.effect.generic.EffectInterfaces.ForceMoveEffect;
import main.Type;
import message.MessageUpdate;
import message.Messages;
import pokemon.ActivePokemon;
import util.RandomUtils;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class Move implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final int MAX_MOVES = 4;
	
	private Attack attack;
	private int maxPP;
	private int pp;
	
	private boolean ready;
	private boolean used;
	
	private Type type;
	private int power;

	public Move(AttackNamesies attackNamesies) {
		this(attackNamesies.getAttack());
	}
	
	public Move(Attack attack) {
		this.attack = attack;
		
		maxPP = attack.getPP();
		pp = maxPP;
		
		resetReady();
		used = false;
		
		type = attack.getActualType();
		power = attack.getPower();
	}
	
	public Move(Attack m, int startPP) {
		this(m);
		pp = startPP;
	}
	
	public void resetPP() {
		pp = maxPP;
	}
	
	public void resetReady() {
		ready = !(attack instanceof MultiTurnMove) || ((MultiTurnMove) attack).chargesFirst();
	}
	
	public boolean isReady() {
		return ready;
	}
	
	public void switchReady(Battle b, ActivePokemon user) {
		if (attack.isMultiTurn(b, user)) {
			ready = !ready;
		}
	}
	
	public Type getType() {
		return type;
	}
	
	public int getPower() {
		return power;
	}
	
	public void setAttributes(Battle b, ActivePokemon user, ActivePokemon victim) {
		type = this.attack.setType(b, user);
		
		// Check if there is an effect that changes the type of the user -- if not just returns the actual type (I promise)
		type = ChangeAttackTypeEffect.updateAttackType(b, user, attack, type);
		
//		System.out.println(user.getName() + " " + attack.getName() + " Type: " + type.getName());
		
		power = this.attack.setPower(b, user, victim);
	}
	
	public Attack getAttack() {
		return attack;
	}
	
	public void use() {
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
		
		return chooseMove(usable);
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

		// TODO: These should be static inside multi turn move
		// Force second turn of a Multi-Turn Move
		if (p.getMove() != null && p.getAttack() instanceof MultiTurnMove) {
			MultiTurnMove multiTurnMove = (MultiTurnMove)p.getAttack();
			boolean chargesFirst = multiTurnMove.chargesFirst();
			boolean isReady = p.getMove().isReady();
			
			if (chargesFirst && !isReady) {
				return true;
			}
			
			if (!chargesFirst && isReady) {
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
				Messages.add(new MessageUpdate(p.getName() + " is out of PP for " + m.attack.getName() + "!"));
			}
			
			return false;
		}
		
		// BUT WHAT IF YOU HAVE A CONDITION THAT PREVENTS YOU FROM USING THAT MOVE?!!?! THEN WHAT?!!?!!
		AttackSelectionEffect unusable = AttackSelectionEffect.getUnusableEffect(b, p, m);
		if (unusable != null) {
			if (selecting) {
				Messages.add(new MessageUpdate(unusable.getUnusableMessage(p)));
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

	// TODO: AI Stuffffff
	private static Move chooseMove(List<Move> usable) {
		return RandomUtils.getRandomValue(usable);
	}
}
