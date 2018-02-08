package battle.effect.generic;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.CastSource;
import battle.effect.InvokeEffect;
import main.Global;
import message.Messages;
import util.RandomUtils;
import util.StringUtils;

import java.io.Serializable;
import java.util.List;

public abstract class Effect implements InvokeEffect, Serializable {
    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_FAIL_MESSAGE = "...but it failed!";

    protected final EffectNamesies namesies;
    private final boolean nextTurnSubside;

    protected boolean active;
    protected int numTurns;

    protected Effect(EffectNamesies name, int minTurns, int maxTurns, boolean nextTurnSubside) {
        this.namesies = name;
        this.nextTurnSubside = nextTurnSubside;

        this.numTurns = minTurns == -1 ? -1 : RandomUtils.getRandomInt(minTurns, maxTurns);
        this.active = true;
    }

    public abstract void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast);

    public boolean nextTurnSubside() {
        return nextTurnSubside;
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

    public boolean apply(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
        if (this.applies(b, caster, victim, source)) {
            this.cast(b, caster, victim, source, printCast);
            return true;
        }

        return false;
    }

    // Should be overriden by subclasses as deemed appropriate
    protected boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
        return true;
    }

    protected boolean shouldSubside(Battle b, ActivePokemon victim) {
        return numTurns == 0;
    }

    public void subside(Battle b, ActivePokemon p) {
        Messages.add(getSubsideMessage(p));
        active = false; // Unnecessary, but just to be safe
    }

    protected String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
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

    @Override
    public InvokeSource getSource() {
        return InvokeSource.EFFECT;
    }

    @Override
    public String toString() {
        return this.getName() + " " + this.getTurns();
    }

    // Returns the effect if it is in the list, otherwise returns null
    public static Effect getEffect(List<? extends Effect> effects, EffectNamesies effectNamesies) {
        for (Effect effect : effects) {
            if (effect.namesies() == effectNamesies) {
                return effect;
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
}
