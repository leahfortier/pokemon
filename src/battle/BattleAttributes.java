package battle;

import battle.attack.Move;
import battle.attack.MoveType;
import battle.effect.ModifyStageValueEffect;
import battle.effect.generic.CastSource;
import battle.effect.generic.Effect;
import battle.effect.generic.EffectInterfaces.StatLoweredEffect;
import battle.effect.generic.EffectInterfaces.StatProtectingEffect;
import battle.effect.generic.EffectNamesies;
import battle.effect.generic.PokemonEffect;
import main.Global;
import message.MessageUpdate;
import message.Messages;
import pokemon.ActivePokemon;
import pokemon.Stat;
import pokemon.ability.Ability;
import util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BattleAttributes implements Serializable {
	private static final long serialVersionUID = 1L;

	private Move selected;
	private Move lastMoveUsed;
	private List<PokemonEffect> effects;
	private int[] stages;
	private int counter;
	private int damageTaken;
	private double successionDecayRate;
	private boolean firstTurn;
	private boolean attacking;
	private boolean used;
	private Object castSource;
	
	public BattleAttributes() {
		resetStages();
		used = false;
		effects = new ArrayList<>();
		successionDecayRate = 1;
		lastMoveUsed = null;
		counter = 1;
		damageTaken = 0;
		firstTurn = true;
		attacking = false;
		castSource = null;
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
	
	public void setFirstTurn(boolean isFirstTurn) {
		firstTurn = isFirstTurn;
	}
	
	public void setUsed(boolean u) {
		used = u;
	}
	
	public boolean isUsed() {
		return used;
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
	
	public void resetDamageTaken() {
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
	
	public int getStage(int index) {
		return stages[index];
	}
	
	public void setStage(int index, int val) {
		stages[index] = val;

		// Don't let it go out of bounds, yo!
		stages[index] = Math.min(Stat.MAX_STAT_CHANGES, stages[index]);
		stages[index] = Math.max(-1*Stat.MAX_STAT_CHANGES, stages[index]);
	}

	public void incrementStage(int index, int val) {
		setStage(index, getStage(index) + val);
	}

	public void resetStage(Stat stat) {
		resetStage(stat.index());
	}
	
	public void resetStage(int index) {
		setStage(index, 0);
	}
	
	public boolean modifyStage(ActivePokemon caster, ActivePokemon victim, int val, Stat stat, Battle b, CastSource source) {
		String message = StringUtils.empty();
		
		switch (source) {
			case ATTACK:
				message = victim.getName() + "'s {statName} was {change}!";
				break;
			case ABILITY:
				message = caster.getName() + "'s " + caster.getAbility().getName() + " {change} {victimName} {statName}!";
				break;
			case HELD_ITEM:
				message = caster.getName() + "'s " + caster.getHeldItem(b).getName() + " {change} {victimName} {statName}!";
				break;
			case USE_ITEM:
				break; // Don't print anything for these, they will be handled manually
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
		
		int index = stat.index();
		String statName = stat.getName();
		boolean print = source == CastSource.ATTACK && caster.getAttack().canPrintFail(); 

		// TODO: this shouldn't just be ability -- also should only check mold breaker when caster != victim
		// Apply abilities that effect the value of the modifier
		Ability ability = victim.getAbility(); 
		if (ability instanceof ModifyStageValueEffect && !caster.breaksTheMold()) {
			val = ((ModifyStageValueEffect)ability).modifyStageValue(val);
		}
		
		// Effects that prevent stat reductions caused by the opponent
		if (val < 0 && caster != victim) {
			StatProtectingEffect prevent = StatProtectingEffect.getPreventEffect(b, caster, victim, stat);
			if (prevent != null) {
				if (print) {
					Messages.add(new MessageUpdate(prevent.preventionMessage(victim, stat)));
				}

				return false;
			}
		}
		
		// Too High
		if (stages[index] == Stat.MAX_STAT_CHANGES && val > 0) {
			if (print) {
				Messages.add(new MessageUpdate(victim.getName() + "'s " + statName + " cannot be raised any higher!"));
			}

			return false;
		}
		
		// HOW LOW CAN YOU GO?!
		if (stages[index] == -1*Stat.MAX_STAT_CHANGES && val < 0) {
			// THIS LOW
			if (print) {
				Messages.add(new MessageUpdate(victim.getName() + "'s " + statName + " cannot be lowered any further!"));
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
		Messages.add(new MessageUpdate(message));

		this.incrementStage(index, val);
		
		// Defiant raises Attack stat by two when a stat is lowered by the opponent
		if (val < 0 && caster != victim) {
			StatLoweredEffect.invokeStatLoweredEffect(b, caster, victim);
		}
		
		return true;
	}
	
	public int totalStatIncreases() {
		int sum = 0;
		for (int i = 0; i < Stat.NUM_BATTLE_STATS; i++) {
			if (stages[i] > 0) {
				sum += stages[i];
			}
		}
		
		return sum;
	}

	public void swapStages(Stat s, ActivePokemon other) {
		int statIndex = s.index();

		int userStat = this.getStage(statIndex);
		int victimStat = other.getAttributes().getStage(statIndex);

		this.setStage(statIndex, victimStat);
		other.getAttributes().setStage(statIndex, userStat);
	}
}
