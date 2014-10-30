package battle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import main.Global;
import main.Namesies;
import main.Type;
import pokemon.ActivePokemon;
import battle.effect.AttackSelectionEffect;
import battle.effect.ChangeAttackTypeEffect;
import battle.effect.ForceMoveEffect;
import battle.effect.MultiTurnMove;

public class Move implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final int MAX_MOVES = 4;
	
	private Attack attack;
	private int maxPP;
	private int pp;
	
	private boolean ready;
	private boolean used;
	
	private Type type;
	private int power;
	
	public Move(Attack attack)
	{
		this.attack = attack;
		
		maxPP = attack.getPP();
		pp = maxPP;
		
		resetReady();
		used = false;
		
		type = attack.getActualType();
		power = attack.getPower();
	}
	
	public Move(Attack m, int startPP)
	{
		this(m);
		pp = startPP;
	}
	
	public void resetPP()
	{
		pp = maxPP;
	}
	
	public void resetReady()
	{
		ready = attack instanceof MultiTurnMove ? ((MultiTurnMove)attack).chargesFirst() : true;
	}
	
	public boolean isReady()
	{
		return ready;
	}
	
	public void switchReady(Battle b)
	{
		if (attack.isMultiTurn(b)) 
		{
			ready = !ready;
		}
	}
	
	public Type getType()
	{
		return type;
	}
	
	public int getPower()
	{
		return power;
	}
	
	public void setAttributes(Battle b, ActivePokemon user, ActivePokemon victim)
	{
		type = this.attack.setType(b, user);
		
		// Check if there is an effect that changes the type of the user -- if not just returns the actual type (I promise)
		Object[] invokees = b.getEffectsList(user);
		type =  (Type)Global.updateInvoke(0, invokees, ChangeAttackTypeEffect.class, "changeAttackType", type);
		
//		System.out.println(user.getName() + " " + attack.getName() + " Type: " + type.getName());
		
		power = this.attack.setPower(b, user, victim);
	}
	
	public Attack getAttack()
	{
		return attack;
	}
	
	public void use()
	{
		used = true;
	}
	
	public boolean used()
	{
		return used;
	}
	
	public int getPP()
	{
		return pp;
	}
	
	public int getMaxPP()
	{
		return maxPP;
	}
	
	public int reducePP(int reduce)
	{
		return pp - (pp = Math.max(0, pp - reduce));
	}
	
	public boolean increasePP(int n)
	{
		if (maxPP == pp) return false;
		
		pp = Math.min(maxPP, pp + 10);
		return true;
	}
	
	public boolean increaseMaxPP(int n)
	{
		int true_max = attack.getPP() + 3*attack.getPP()/5;
		
		if (maxPP == true_max) return false;
		
		maxPP += n*attack.getPP()/5;
		
		if (maxPP > true_max) maxPP = true_max;
		return true;
	}
	
	public static Move selectOpponentMove(Battle b, ActivePokemon p) 
	{
		if (forceMove(b, p)) 
		{
			return p.getMove();
		}
		
		List<Move> usable = getUsableMoves(b, p);
		if (usable.size() == 0)
		{
			return new Move(Attack.getAttack(Namesies.STRUGGLE_ATTACK));	
		}
		
		return moveAI(b, p, usable);
	}
	
	// Returns true if a move should be forced (move will already be selected for the Pokemon), and false if not 
	public static boolean forceMove(Battle b, ActivePokemon p)
	{
		// Forced moves
		Object forcedMove = Global.getInvoke(p.getEffects().toArray(), ForceMoveEffect.class, "getMove");
		if (forcedMove != null)
		{
			p.setMove((Move)forcedMove);
			return true;
		}
		
		// Force second turn of a Multi-Turn Move
		if (p.getMove() != null && p.getAttack() instanceof MultiTurnMove)
		{
			MultiTurnMove multiTurnMove = (MultiTurnMove)p.getAttack();
			boolean chargesFirst = multiTurnMove.chargesFirst();
			boolean isReady = p.getMove().isReady();
			
			if (chargesFirst && !isReady)
			{
				return true;
			}
			
			if (!chargesFirst && isReady)
			{
				return true;
			}
		}
		
		if (p.user() && getUsableMoves(b, p).size() == 0)
		{
			p.setMove(new Move(Attack.getAttack(Namesies.STRUGGLE_ATTACK)));
			return true;
		}
		
		return false;
	}
	
	// Returns a list of the moves that are valid for the pokemon to use
	private static List<Move> getUsableMoves(Battle b, ActivePokemon p)
	{
		List<Move> usable = new ArrayList<Move>();
		for (Move m : p.getMoves(b))
		{
			if (validMove(b, p, m, false))
			{
				usable.add(m);
			}
		}
		
		return usable;
	}
	
	// Will return whether or not p can execute m
	// if selecting is true: if yes (to above line), it will set m to be p's move, if no, the battle should display why
	public static boolean validMove(Battle b, ActivePokemon p, Move m, boolean selecting)
	{
		// Invalid if PP is zero
		if (m.getPP() == 0)
		{
			if (selecting)
				b.addMessage(p.getName() + " is out of PP for " + m.attack.getName() + "!");
			
			return false;
		}
		
		// BUT WHAT IF YOU HAVE A CONDITION THAT PREVENTS YOU FROM USING THAT MOVE?!!?! THEN WHAT?!!?!!
		Object unusable = Global.checkInvoke(false, b.getEffectsList(p), AttackSelectionEffect.class, "usable", p, m);
		if (unusable != null)
		{
			if (selecting)
			{
				b.addMessage(((AttackSelectionEffect)unusable).getUnusableMessage(p));
			}
			
			// THAT'S WHAT
			return false;
		}
		
		// Set the move if selecting
		if (selecting) 
			p.setMove(m);
		
		return true;
	}
	
	
	// AI Stuffffff
	private static Move moveAI(Battle b, ActivePokemon p, List<Move> usable)
	{
		ActivePokemon opp = b.getOtherPokemon(p.user());
		
		// Initializes start values for the pokemons moveset
		double[] move = new double[usable.size()];
		Arrays.fill(move, 1);
		
		// Multiplies by effectiveness
		for (int i = 0; i < usable.size(); i++)
		{
			// TODO: Honestly this shouldn't be using the basic advantage it should be using actual advantage
			move[i] *= Type.getBasicAdvantage(usable.get(i).getAttack().getActualType(), opp, b)*3;
		}
		
		double[] range = new double[move.length];
		range[0] = move[0];
		for (int i = 1; i < move.length; i++)
		{
			range[i] = move[i] + range[i - 1];
		}
		
		int value = (int) (Math.random()*range[range.length - 1]);
		for (int i = 0; i < range.length; i++)
		{
			if (value < range[i])
			{
				// Temporarily removing AI because it makes testing difficult when I need a random move
//				return usable.get(i);
			}
		}
		
		// Theorectically, it should never get here, but just in case choose a random move
		return chooseMove(p, usable);
	}
	
	// Selects a random move for the opponent (eventually Jessica will write some sick AI to make this cooler)
	private static Move chooseMove(ActivePokemon p, List<Move> usable)
	{
		return usable.get((int)(Math.random()*usable.size()));
	}
}
