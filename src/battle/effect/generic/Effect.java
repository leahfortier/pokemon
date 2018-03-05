package battle.effect.generic;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.CastSource;
import battle.effect.InvokeEffect;
import battle.effect.generic.EffectInterfaces.EffectReceivedEffect;
import main.Global;
import message.Messages;
import util.RandomUtils;
import util.StringUtils;

import java.io.Serializable;

public abstract class Effect<NamesiesType extends EffectNamesies> implements InvokeEffect, Serializable {
    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_FAIL_MESSAGE = "...but it failed!";

    protected final NamesiesType namesies;
    private final boolean nextTurnSubside;
    private final boolean hasAlternateCast;

    protected boolean active;
    protected int numTurns;

    protected Effect(NamesiesType name, int minTurns, int maxTurns, boolean nextTurnSubside, boolean hasAlternateCast) {
        this.namesies = name;
        this.nextTurnSubside = nextTurnSubside;
        this.hasAlternateCast = hasAlternateCast;

        this.numTurns = minTurns == -1 ? -1 : RandomUtils.getRandomInt(minTurns, maxTurns);
        this.active = true;
    }

    protected abstract void addEffect(Battle b, ActivePokemon victim);
    protected abstract boolean hasEffect(Battle b, ActivePokemon victim);

    public final void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
        if (this.hasAlternateCast && this.hasEffect(b, victim)) {
            this.alternateCast(b, caster, victim, source, printCast);
        } else {
            this.beforeCast(b, caster, victim, source);
            Messages.update(b);

            this.addCastMessage(b, caster, victim, source, printCast);
            this.addEffect(b, victim);

            this.afterCast(b, caster, victim, source);
            Messages.update(b);

            EffectReceivedEffect.invokeEffectReceivedEffect(b, caster, victim, this.namesies());
            Messages.update(b);
        }
    }

    protected void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {}
    protected void afterCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {}
    protected void alternateCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {}

    public boolean nextTurnSubside() {
        return nextTurnSubside;
    }

    public boolean hasAlternateCast() {
        return this.hasAlternateCast;
    }

    public final NamesiesType namesies() {
        return this.namesies;
    }

    public void deactivate() {
        active = false;
    }

    public void decrement(Battle b, ActivePokemon victim) {
        if (numTurns == 0) {
            Global.error("Number of turns should never be zero before the decrement!! (Effect: " + this.namesies() + ")");
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

    protected void addCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source, boolean printCast) {
        if (printCast) {
            Messages.add(this.getCastMessage(b, user, victim, source));
        }
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
        return this.namesies() + " " + this.getTurns();
    }
}
