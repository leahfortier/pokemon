package battle.effect.generic;

import battle.Battle;
import main.Global;
import message.Messages;
import pokemon.ActivePokemon;
import util.StringUtils;

import java.io.Serializable;
import java.util.List;

public abstract class Effect implements Serializable {
	public static final String DEFAULT_FAIL_MESSAGE = "...but it failed!";
	
	private static final long serialVersionUID = 1L;
	
	protected EffectNamesies namesies;
	protected boolean active;

	protected int numTurns;
	private boolean nextTurnSubside;

	public Effect(EffectNamesies name, int minTurns, int maxTurns, boolean nextTurnSubside) {
		// TODO: Should have a constant for -1
        // TODO: Move to test
		if ((minTurns == -1 && maxTurns != -1) || (minTurns != -1 && maxTurns == -1)) {
			Global.error("Incorrect min/max turns for effect " + name);
		}
		
		this.namesies = name;
		this.nextTurnSubside = nextTurnSubside;

        this.numTurns = minTurns == -1 ? -1 : Global.getRandomInt(minTurns, maxTurns);
        this.active = true;
	}
	
	public boolean nextTurnSubside() {
		return nextTurnSubside;
	}

	public static Effect getEffect(EffectNamesies effect) {
		return effect.getEffect();
	}
	
	// Returns the effect if it is in the list, otherwise returns null
	public static Effect getEffect(List<? extends Effect> effects, EffectNamesies effect) {
		for (Effect e : effects) {
			if (e.namesies() == effect && e.isActive()) {
				return e;
			}
		}
			
		return null;
	}
	
	public static boolean hasEffect(List<? extends Effect> effects, EffectNamesies effect) {
		return getEffect(effects, effect) != null;
	}
	
	public static boolean removeEffect(List<? extends Effect> effects, EffectNamesies effectToRemove) {
		return effects.removeIf(effect -> effect.namesies() == effectToRemove);
	}
	
	public static boolean isInactiveEffect(Object object) {
		return object instanceof Effect && !((Effect)object).isActive();
	}
	
	public void deactivate() {
		active = false;
	}
	
	public void decrement(Battle b, ActivePokemon victim) {
		if (numTurns == 0) {
			Global.error("Number of turns should never be zero before the decrement!! (Effect: " + getName() + ")");
		}
		
		// -1 indicates a permanent effect
		if (numTurns != -1) {
			numTurns--;
		}
		
		// All done with this effect! If it's time to subside, do it
		if (shouldSubside(b, victim)) {
			active = false;
		}
	}
	
	// Should be overriden by subclasses as deemed appropriate
	public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
		return true;
	}
	
	public boolean shouldSubside(Battle b, ActivePokemon victim) {
		return numTurns == 0;
	}
	
	public void subside(Battle b, ActivePokemon p) {
		Messages.addMessage(getSubsideMessage(p));
		active = false; // Unnecessary, but just to be safe
	}

	// TODO: Move this it's in a weird place and I'm a psycho for location
	public abstract void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast);
	
	public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
		return StringUtils.empty();
	}
	
	public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
		return DEFAULT_FAIL_MESSAGE;
	}
	
	public String getSubsideMessage(ActivePokemon p) {
		return StringUtils.empty();
	}
	
	public EffectNamesies namesies() {
		return this.namesies;
	}
	
	public String getName() {
		return namesies.getName();
	}
	
	public boolean isActive() {
		return active;
	}
	
	// Returns the number of turns left that the Effect will be in play (-1 for permanent effects)
	public int getTurns() {
		return numTurns;
	}
	
	public void setTurns(int turns) {
		numTurns = turns;
	}
	
	public String toString() {
		return getName() + " " + getTurns();
	}
}
