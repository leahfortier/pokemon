package battle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import main.Global;
import pokemon.Ability;
import pokemon.ActivePokemon;
import pokemon.Stat;
import battle.Attack.MoveType;
import battle.effect.Effect;
import battle.effect.Effect.CastSource;
import battle.effect.ModifyStageValueEffect;
import battle.effect.PokemonEffect;
import battle.effect.StatProtectingEffect;

public class BattleAttributes implements Serializable
{
	private static final long serialVersionUID = 1L;
	private int[] stages;
	private boolean used;
	private Move selected;
	private List<PokemonEffect> effects;
	private double successionDecayRate;
	private Move lastMoveUsed;
	private int counter;
	private int damageTaken;
	private boolean firstTurn;
	private boolean attacking;
	
	public BattleAttributes()
	{
		resetStages();
		used = false;
		effects = new ArrayList<>();
		successionDecayRate = 1;
		lastMoveUsed = null;
		counter = 1;
		damageTaken = 0;
		firstTurn = true;
		attacking = false;
	}
	
	public void setAttacking(boolean isAttacking)
	{
		attacking = isAttacking;
	}
	
	public boolean isAttacking()
	{
		return attacking;
	}
	
	public void setFirstTurn(boolean isFirstTurn)
	{
		firstTurn = isFirstTurn;
	}
	
	public void setUsed(boolean u)
	{
		used = u;
	}
	
	public boolean isUsed()
	{
		return used;
	}
	
	public boolean isFirstTurn()
	{
		return firstTurn;
	}
	
	public void resetStages()
	{
		stages = new int[Stat.NUM_BATTLE_STATS];
	}
	
	public void takeDamage(int damage)
	{
		damageTaken = damage;
	}
	
	public int getDamageTaken()
	{
		return damageTaken;
	}
	
	public boolean hasTakenDamage()
	{
		return damageTaken > 0;
	}
	
	public void resetDamageTaken()
	{
		damageTaken = 0;
	}
	
	public void setLastMoveUsed()
	{
		lastMoveUsed = selected;
	}
	
	public Move getLastMoveUsed()
	{
		return lastMoveUsed;
	}
	
	// Increment count if the pokemon uses the same move twice in a row
	public void count()
	{
		if (lastMoveUsed == null) 
		{
			counter = 1;
		}
		else if (selected.getAttack().getName().equals(lastMoveUsed.getAttack().getName()))
		{
			counter++;
		}
		else
		{
			counter = 1;
		}
	}
	
	public void resetCount()
	{
		counter = 1;
	}
	
	public int getCount()
	{
		return counter;
	}
	
	public List<PokemonEffect> getEffects()
	{
		return effects;
	}
	
	public double getSuccessionDecayRate()
	{
		return successionDecayRate;
	}
	
	public void decay()
	{
		if (selected.getAttack().isMoveType(MoveType.SUCCESSIVE_DECAY)) 
		{
			successionDecayRate *= .5;
		}
		else
		{
			successionDecayRate = 1;
		}
	}
	
	public Move getMove() 
	{
		return selected;
	}
	
	public void setMove(Move m)
	{
		selected = m;
	}
	
	public void addEffect(PokemonEffect e)
	{
		effects.add(e.newInstance());
	}
	
	public boolean removeEffect(String effect)
	{
		return Effect.removeEffect(effects, effect);
	}
	
	// Returns null if the Pokemon is not under the effects of the input effect, otherwise returns the Effect
	public PokemonEffect getEffect(String effect)
	{
		return (PokemonEffect)(Effect.getEffect(effects, effect));
	}
	
	public boolean hasEffect(String effect)
	{
		return Effect.hasEffect(effects, effect);
	}
	
	public int getStage(int index)
	{
		return stages[index];
	}
	
	public void setStage(int index, int val)
	{
		stages[index] = val;
	}
	
	public void resetStage(Stat stat)
	{
		stages[stat.index()] = 0;
	}
	
	// Modifies a stat for a Pokemon and prints appropriate messages and stuff
	public boolean modifyStage(ActivePokemon caster, ActivePokemon victim, int val, Stat stat, Battle b, CastSource source)
	{
		// Don't modify the stages of a dead Pokemon
		if (victim.isFainted(b)) return false;
		
		int index = stat.index();
		String statName = stat.getName();
		boolean print = source == CastSource.ATTACK && caster.getAttack().canPrintFail(); 
		
		// Apply abilities that effect the value of the modifier
		Ability ability = victim.getAbility(); 
		if (ability instanceof ModifyStageValueEffect && !caster.breaksTheMold())
		{
			val = ((ModifyStageValueEffect)ability).modifyStageValue(val);
		}
		
		// Effects that prevent stat reductions caused by the opponent
		if (val < 0 && caster != victim)
		{
			Object[] list = b.getEffectsList(victim);
			for (Object o : list)
			{
				if (Effect.isInactiveEffect(o)) 
					continue;
				
				if (o instanceof StatProtectingEffect && ((StatProtectingEffect)o).prevent(caster, stat))
				{
					if (print) b.addMessage(((StatProtectingEffect)o).preventionMessage(victim)); 
					return false;					
				}
			}
		}
		
		// Too High
		if (stages[index] == Stat.MAX_STAT_CHANGES && val > 0)
		{
			if (print) b.addMessage(victim.getName() + "'s " + statName + " cannot be raised any higher!");
			return false;
		}
		
		// HOW LOW CAN YOU GO?!
		if (stages[index] == -1*Stat.MAX_STAT_CHANGES && val < 0)
		{
			// THIS LOW
			if (print) b.addMessage(victim.getName() + "'s " + statName + " cannot be lowered any further!");
			return false;
		}
		
		
		String change = "", victimName = caster == victim ? "its" : victim.getName() + "'s";
		if (val >= 2) change =  "sharply raised";
		else if (val == 1) change = "raised";
		else if (val == -1) change = "lowered";
		else if (val <= -2) change = "sharply lowered";
		
		switch (source)
		{
			case ATTACK:
				b.addMessage(victim.getName() + "'s " + statName + " was " + change + "!");
				break;
			case ABILITY:
				b.addMessage(caster.getName() + "'s " + caster.getAbility().getName() + " " + change + " " + victimName + " " + statName + "!");
				break;
			case HELD_ITEM:
				b.addMessage(caster.getName() + "'s " + caster.getHeldItem(b).getName() + " " + change + " " + victimName + " " + statName + "!");
				break;
			case USE_ITEM:
				break; // Don't print anything for these, they will be handled manually
			default:
				Global.error("Unknown source for stage modifier.");
				break;
		}
		
		stages[index] += val;
		
		// Don't let it go out of bounds, yo!
		stages[index] = Math.min(Stat.MAX_STAT_CHANGES, stages[index]);
		stages[index] = Math.max(-1*Stat.MAX_STAT_CHANGES, stages[index]);
		
		// Defiant raises Attack stat by two when a stat is lowered by the opponent
		if (val < 0 && caster != victim && victim.hasAbility("Defiant") && modifyStage(victim, victim, 2, Stat.ATTACK, b, CastSource.ABILITY))
		{
			b.addMessage(victim.getName() + "'s Defiant sharply raised its attack!");
		}
		
		return true;
	}
	
	public int totalStatIncreases()
	{
		int sum = 0;
		for (int i = 0; i < Stat.NUM_BATTLE_STATS; i++)
		{
			if (stages[i] > 0) sum += stages[i];
		}
		return sum;
	}
}
