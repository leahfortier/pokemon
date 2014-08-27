package battle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import main.Type;
import pokemon.ActivePokemon;
import battle.effect.AttackSelectionEffect;
import battle.effect.Effect;
import battle.effect.ForceMoveEffect;
import battle.effect.MultiTurnMove;
import battle.effect.PokemonEffect;

public class Move implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final int MAX_MOVES = 4;
	
	private Attack move;
	private int maxPP;
	private int pp;
	private boolean ready;
	private boolean used;
	
	public Move(Attack m)
	{
		move = m;
		maxPP = move.getPP();
		pp = maxPP;
		ready = m instanceof MultiTurnMove ? ((MultiTurnMove)m).chargesFirst() : true;
		used = false;
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
	
	public boolean isReady()
	{
		return ready;
	}
	
	public void switchReady(Battle b)
	{
		if (move.isMultiTurn(b)) ready = !ready;
	}
	
	public Attack getAttack()
	{
		return move;
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
		int true_max = move.getPP() + 3*move.getPP()/5;
		
		if (maxPP == true_max) return false;
		
		maxPP += n*move.getPP()/5;
		
		if (maxPP > true_max) maxPP = true_max;
		return true;
	}
	
	public static Move selectMove(Battle b, ActivePokemon p) 
	{
		if (forceMove(p)) return p.getMove();
		List<Move> usable = getUsableMoves(b, p);
		if (usable.size() == 0) return new Move(Attack.getAttack("Struggle"));
		return moveAI(b, p, usable);
	}
	
	// Returns true if a move should be forced (move will already be selected for the Pokemon), and false if not 
	public static boolean forceMove(ActivePokemon p)
	{
		// Forced moves
		for (PokemonEffect e : p.getEffects())
		{
			if (e instanceof ForceMoveEffect) 
			{
				p.setMove(((ForceMoveEffect)e).getMove());
				return true;
			}
		}
		
		// Force second turn of a Multi-Turn Move
		if (p.getMove() != null && p.getAttack() instanceof MultiTurnMove)
		{
			MultiTurnMove m = (MultiTurnMove)p.getAttack();
			if (m.chargesFirst() && !p.getMove().ready) return true;  
			if (!m.chargesFirst() && p.getMove().ready) return true;
		}
		
		return false;
	}
	
	// Returns a list of the moves that are valid for the pokemon to use
	private static List<Move> getUsableMoves(Battle b, ActivePokemon p)
	{
		List<Move> usable = new ArrayList<Move>();
		for (Move m : p.getMoves()) 
		{
			if (m.getPP() > 0) usable.add(m); // Dat PP Check
		}
		
		// BUT WHAT IF YOU HAVE A CONDITION THAT PREVENTS YOU FROM USING THAT MOVE?!!?! THEN WHAT?!!?!!
		Object[] list = b.getEffectsList(p);
		for (Object o : list)
		{
			if (Effect.isInactiveEffect(o)) 
				continue;
			
			if (o instanceof AttackSelectionEffect)
			{
				AttackSelectionEffect a = (AttackSelectionEffect)o;
				for (int i = 0; i < usable.size(); i++)
				{
					if (!a.usable(p, usable.get(i)))
					{
						usable.remove(i--); // THAT'S WHAT
					}
				}
			}
		}
		
		return usable;
	}
	
	// Will return whether or not p can execute m
	// if yes, it will set m to be p's move, if no, the battle should display why
	public static boolean validMove(ActivePokemon p, Move m, Battle b)
	{
		if (m.getPP() == 0)
		{
			b.addMessage(p.getName() + " is out of PP for " + m.move.getName() + "!");
			return false;
		}
		
		Object[] list = b.getEffectsList(p);
		for (Object o : list)
		{
			if (Effect.isInactiveEffect(o)) 
				continue;
			 
			if (o instanceof AttackSelectionEffect)
			{
				AttackSelectionEffect a = (AttackSelectionEffect)o;
				if (!a.usable(p, m))
				{
					b.addMessage(a.getUnusableMessage(p));
					return false;					
				}
			}
		}
		
		p.setMove(m);
		return true;
	}
	
	
	//AI Stuffffff
	private static Move moveAI(Battle b, ActivePokemon p, List<Move> usable)
	{
		ActivePokemon opp = b.getOtherPokemon(p.user());
		
		// Initializes start values for the pokemons moveset
		double[] move = new double[usable.size()];
		Arrays.fill(move, 1);
		
		//multiplies by effectiveness
		for (int i = 0; i < usable.size(); i++)
		{
			move[i] *= Type.getAdvantage(usable.get(i).getAttack().getType(b, p), opp, b)*3;
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
				return usable.get(i);
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
