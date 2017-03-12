package battle;

import battle.attack.Move;
import battle.attack.MoveType;
import battle.effect.generic.CastSource;
import battle.effect.generic.Effect;
import battle.effect.generic.EffectInterfaces.ModifyStageValueEffect;
import battle.effect.generic.EffectInterfaces.StatLoweredEffect;
import battle.effect.generic.EffectInterfaces.StatProtectingEffect;
import battle.effect.generic.EffectNamesies;
import battle.effect.generic.PokemonEffect;
import main.Global;
import message.MessageUpdate;
import message.Messages;
import pokemon.ActivePokemon;
import pokemon.Stat;
import util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BattleAttributes implements Serializable {
	private static final long serialVersionUID = 1L;

	private final transient ActivePokemon attributesHolder;

	private Move selected;
	private Move lastMoveUsed;
	private List<PokemonEffect> effects;
	private int[] stages;
	private int counter;
	private int damageTaken;
	private double successionDecayRate;
	private boolean firstTurn;
	private boolean attacking;
	private boolean reducePP;
	private boolean used;
	private boolean battleUsed;
	private boolean lastMoveSucceeded;
	private Object castSource;
	
	public BattleAttributes(ActivePokemon attributesHolder) {
		this.attributesHolder = attributesHolder;

		resetStages();
		used = false;
		battleUsed = false;
		effects = new ArrayList<>();
		successionDecayRate = 1;
		lastMoveUsed = null;
		counter = 1;
		damageTaken = 0;
		firstTurn = true;
		attacking = false;
		lastMoveSucceeded = true;
		castSource = null;
		reducePP = false;
	}

	public void setReducePP(boolean reduce) {
		reducePP = reduce;
	}

	public boolean shouldReducePP() {
		return reducePP;
	}

	public void setCastSource(Object castSource) {
		this.castSource = castSource;
	}

	public Object getCastSource() {
		return this.castSource;
	}

	public void setAttacking(boolean isAttacking) {
		attacking = isAttacking;
	}
	
	public boolean isAttacking() {
		return attacking;
	}

	void setLastMoveSucceeded(boolean lastMoveSucceeded) {
		this.lastMoveSucceeded = lastMoveSucceeded;
	}

	public boolean lastMoveSucceeded() {
		return this.lastMoveSucceeded;
	}
	
	void setFirstTurn(boolean isFirstTurn) {
		firstTurn = isFirstTurn;
	}
	
	public void setUsed(boolean u) {
		used = u;
		if (used) {
			battleUsed = true;
		}
	}
	
	public boolean isUsed() {
		return used;
	}

	public boolean isBattleUsed() {
		return this.battleUsed;
	}
	
	public boolean isFirstTurn() {
		return firstTurn;
	}
	
	public void resetStages() {
		stages = new int[Stat.NUM_BATTLE_STATS];
	}
	
	public void takeDamage(int damage) {
		damageTaken = damage;
	}
	
	public int getDamageTaken() {
		return damageTaken;
	}
	
	public boolean hasTakenDamage() {
		return damageTaken > 0;
	}

	public void resetTurn() {
		resetDamageTaken();
		setReducePP(false);
	}

	private void resetDamageTaken() {
		damageTaken = 0;
	}
	
	public void setLastMoveUsed() {
		lastMoveUsed = selected;
	}
	
	public Move getLastMoveUsed() {
		return lastMoveUsed;
	}
	
	// Increment count if the pokemon uses the same move twice in a row
	public void count() {
		if (lastMoveUsed == null || selected.getAttack().namesies() != lastMoveUsed.getAttack().namesies()) {
			resetCount();
		}
		else {
			counter++;
		}
	}
	
	public void resetCount() {
		counter = 1;
	}
	
	public int getCount() {
		return counter;
	}
	
	public List<PokemonEffect> getEffects() {
		return effects;
	}
	
	public double getSuccessionDecayRate() {
		return successionDecayRate;
	}
	
	public void decay() {
		if (selected.getAttack().isMoveType(MoveType.SUCCESSIVE_DECAY)) {
			successionDecayRate *= .5;
		}
		else {
			successionDecayRate = 1;
		}
	}
	
	public Move getMove() {
		return selected;
	}
	
	public void setMove(Move m) {
		selected = m;
	}
	
	public void addEffect(PokemonEffect e) {
		effects.add(e);
	}

	public boolean removeEffect(PokemonEffect effect) {
		return effects.remove(effect);
	}
	
	public boolean removeEffect(EffectNamesies effect) {
		return Effect.removeEffect(effects, effect);
	}
	
	// Returns null if the Pokemon is not under the effects of the input effect, otherwise returns the Effect
	public PokemonEffect getEffect(EffectNamesies effect) {
		return (PokemonEffect)(Effect.getEffect(effects, effect));
	}
	
	public boolean hasEffect(EffectNamesies effect) {
		return Effect.hasEffect(effects, effect);
	}

	public int getStage(Stat stat) {
		return this.stages[stat.index()];
	}
	
	public void setStage(Stat stat, int val) {
		int index = stat.index();
		stages[index] = val;

		// Don't let it go out of bounds, yo!
		stages[index] = Math.min(Stat.MAX_STAT_CHANGES, stages[index]);
		stages[index] = Math.max(-1*Stat.MAX_STAT_CHANGES, stages[index]);

		Messages.add(new MessageUpdate().withPokemon(attributesHolder));
	}

	public void incrementStage(Stat stat, int val) {
		setStage(stat, getStage(stat) + val);
	}

	public void resetStage(Stat stat) {
		setStage(stat, 0);
	}
	
	public boolean modifyStage(ActivePokemon caster, ActivePokemon victim, int val, Stat stat, Battle b, CastSource source) {
		String message = StringUtils.empty();
		
		switch (source) {
			case ATTACK:
			case USE_ITEM:
				message = victim.getName() + "'s {statName} was {change}!";
				break;
			case ABILITY:
				message = caster.getName() + "'s " + caster.getAbility().getName() + " {change} {victimName} {statName}!";
				break;
			case HELD_ITEM:
				message = caster.getName() + "'s " + caster.getHeldItem(b).getName() + " {change} {victimName} {statName}!";
				break;
			case EFFECT:
				Global.error("Effect message should be handled manually using the other modifyStage method.");
				break;
			default:
				Global.error("Unknown source for stage modifier.");
				break;
		}
		
		return modifyStage(caster, victim, val, stat, b, source, message);
	}
	
	// Modifies a stat for a Pokemon and prints appropriate messages and stuff
	public boolean modifyStage(ActivePokemon caster, ActivePokemon victim, int val, Stat stat, Battle b, CastSource source, String message) {

		// Don't modify the stages of a dead Pokemon
		if (victim.isFainted(b)) {
			return false;
		}

		String statName = stat.getName();
		boolean print = source == CastSource.ATTACK && caster.getAttack().canPrintFail(); 

		// Effects that change the value of the modifier
		val = ModifyStageValueEffect.updateModifyStageValueEffect(b, caster, victim, val);
		
		// Effects that prevent stat reductions caused by the opponent
		if (val < 0 && caster != victim) {
			StatProtectingEffect prevent = StatProtectingEffect.getPreventEffect(b, caster, victim, stat);
			if (prevent != null) {
				if (print) {
					Messages.add(prevent.preventionMessage(victim, stat));
				}

				return false;
			}
		}
		
		// Too High
		if (getStage(stat) == Stat.MAX_STAT_CHANGES && val > 0) {
			if (print) {
				Messages.add(victim.getName() + "'s " + statName + " cannot be raised any higher!");
			}

			return false;
		}
		
		// HOW LOW CAN YOU GO?!
		if (getStage(stat) == -1*Stat.MAX_STAT_CHANGES && val < 0) {
			// THIS LOW
			if (print) {
				Messages.add(victim.getName() + "'s " + statName + " cannot be lowered any further!");
			}

			return false;
		}		
		
		String change;
		String victimName = caster == victim ? "its" : victim.getName() + "'s";

		if (val >= 2) {
			change =  "sharply raised";
		}
		else if (val == 1) {
			change = "raised";
		}
		else if (val == -1) {
			change = "lowered";
		}
		else if (val <= -2) {
			change = "sharply lowered";
		} else {
			// TODO: Make sure this is an appropriate error -- not sure why it wasn't here before
			Global.error("Cannot modify a stage by zero.");
			return false;
		}

		message = message.replace("{statName}", statName)
				.replace("{change}", change)
				.replace("{victimName}", victimName);
		Messages.add(message);

		this.incrementStage(stat, val);
		
		// Defiant raises Attack stat by two when a stat is lowered by the opponent
		if (val < 0 && caster != victim) {
			StatLoweredEffect.invokeStatLoweredEffect(b, caster, victim);
		}
		
		return true;
	}
	
	public int totalStatIncreases() {
		int sum = 0;
		for (Stat stat : Stat.BATTLE_STATS) {
			int stage = getStage(stat);
			if (stage > 0) {
				sum += stage;
			}
		}
		
		return sum;
	}

	public void swapStages(Stat stat, ActivePokemon other) {
		int userStat = this.getStage(stat);
		int victimStat = other.getAttributes().getStage(stat);

		this.setStage(stat, victimStat);
		other.getAttributes().setStage(stat, userStat);
	}
}
