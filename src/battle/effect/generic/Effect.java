package battle.effect.generic;

import java.io.Serializable;
import java.util.List;

import main.Global;
import namesies.EffectNamesies;
import pokemon.ActivePokemon;
import battle.Battle;
import util.StringUtils;

public abstract class Effect implements Serializable
{
	public static final String DEFAULT_FAIL_MESSAGE = "...but it failed!";
	
	private static final long serialVersionUID = 1L;
	
	protected EffectNamesies namesies;
	protected boolean active;

	protected int numTurns;
	private int minTurns;
	private int maxTurns;
	private boolean nextTurnSubside;

	public enum EffectType {
		POKEMON,
		TEAM,
		BATTLE,
	}

	// TODO: Move this to its own file
	public enum CastSource {
		ATTACK,
		ABILITY,
		HELD_ITEM,
		USE_ITEM,
		EFFECT;
		
		public Object getSource(Battle b, ActivePokemon caster) {
			switch (this) {
				case ATTACK:
					return caster.getAttack();
				case ABILITY:
					return caster.getAbility();
				case HELD_ITEM:
					return caster.getHeldItem(b);
				default:
					Global.error("Cannot get source for CastSource." + this.name() + ".");
					return null;
			}
		}
	}
	
	public Effect(EffectNamesies name, int minTurns, int maxTurns, boolean nextTurnSubside) {
		// TODO: Should have a constant for -1
		if ((minTurns == -1 && maxTurns != -1) || (minTurns != -1 && maxTurns == -1)) {
			Global.error("Incorrect min/max turns for effect " + name);
		}
		
		this.namesies = name;
		this.minTurns = minTurns;
		this.maxTurns = maxTurns;
		this.nextTurnSubside = nextTurnSubside;
	}
	
	public abstract Effect newInstance();
	
	protected Effect activate() {
		// TODO: Random
		numTurns = minTurns == -1 ? -1 : (int)(Math.random()*(maxTurns-minTurns + 1)) + minTurns;
		active = true;
		return this;
	}
	
	public boolean nextTurnSubside() {
		return nextTurnSubside;
	}
	
	public static Effect getEffect(EffectNamesies effect, EffectType type) {
		switch (type) {
			case POKEMON:
				return PokemonEffect.getEffect(effect);
			case TEAM:
				return TeamEffect.getEffect(effect);
			case BATTLE:
				return BattleEffect.getEffect(effect);
		}

		// TODO: Make sure this should actually throw an error here -- I'm not sure why this wasn't already here
		Global.error("Unknown effect type " + type + ".  Could not get effect for " + effect);
		return null;
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
	
	public static boolean removeEffect(List<? extends Effect> effects, EffectNamesies effect) {
		// TODO: Change to for each
		for (int i = 0; i < effects.size(); i++) {
			if (effects.get(i).namesies() == effect) {
				effects.remove(i);
				return true;
			}
		}
		
		return false;
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
		b.addMessage(getSubsideMessage(p));
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
