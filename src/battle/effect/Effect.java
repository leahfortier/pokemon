package battle.effect;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.InvokeInterfaces.EffectExtendingEffect;
import battle.effect.InvokeInterfaces.EffectPreventionEffect;
import battle.effect.InvokeInterfaces.EffectReceivedEffect;
import battle.effect.source.CastSource;
import main.Global;
import message.Messages;
import util.RandomUtils;

public abstract class Effect<NamesiesType extends EffectNamesies> implements EffectInterface {
    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_FAIL_MESSAGE = "...but it failed!";

    protected final NamesiesType namesies;
    private final boolean canHave;
    private final boolean hasAlternateCast;

    private int numTurns;
    private boolean active;

    protected Effect(NamesiesType name, int minTurns, int maxTurns, boolean canHave, boolean hasAlternateCast) {
        this.namesies = name;
        this.canHave = canHave;
        this.hasAlternateCast = hasAlternateCast;

        this.numTurns = minTurns == -1 ? -1 : RandomUtils.getRandomInt(minTurns, maxTurns);
        this.active = true;
    }

    protected abstract void addEffect(Battle b, ActivePokemon victim);
    protected abstract boolean hasEffect(Battle b, ActivePokemon victim);
    protected abstract Effect<NamesiesType> getEffect(Battle b, ActivePokemon victim);

    public static Effect cast(EffectNamesies namesies, Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
        Effect effect = namesies.getEffect();
        if (effect.hasAlternateCast && effect.hasEffect(b, victim)) {
            effect = effect.getEffect(b, victim);
            effect.alternateCast(b, caster, victim, source, printCast);
        } else {
            effect.beforeCast(b, caster, victim, source);
            Messages.update(b);

            effect.addCastMessage(b, caster, victim, source, printCast);
            effect.addEffect(b, victim);

            effect.afterCast(b, caster, victim, source);
            Messages.update(b);

            if (effect.numTurns > 0) {
                effect.numTurns += EffectExtendingEffect.getModifier(b, caster, effect, effect.numTurns);
            }

            EffectReceivedEffect.invokeEffectReceivedEffect(b, caster, victim, namesies);
            Messages.update(b);
        }
        return effect;
    }

    protected void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {}
    protected void afterCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {}
    protected void alternateCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {}

    public boolean canHave() {
        return this.canHave;
    }

    public boolean hasAlternateCast() {
        return this.hasAlternateCast;
    }

    @Override
    public final NamesiesType namesies() {
        return this.namesies;
    }

    @Override
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
        if (numTurns == 0 || this.shouldSubside(b, victim)) {
            this.deactivate();
        }
    }

    public static ApplyResult apply(EffectNamesies namesies, Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
        ApplyResult result = namesies.getEffect().fullApplies(b, caster, victim, source);
        if (result.isSuccess()) {
            cast(namesies, b, caster, victim, source, printCast);
        }

        return result;
    }

    private ApplyResult fullApplies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
        // Check if the victim has an effect that prevents this effect
        ApplyResult effectResult = EffectPreventionEffect.getPreventEffect(b, caster, victim, this.namesies);
        if (effectResult.isFailure()) {
            return effectResult;
        }

        // Check effect-specific failures
        // Note: This should be before the direct hasEffect check since some effects check directly with special messages
        ApplyResult applyResult = this.applies(b, caster, victim, source);
        if (applyResult.isFailure()) {
            return applyResult;
        }

        // Fails if the victim already has this effect (and they can't have it again)
        if (!this.canHave && this.hasEffect(b, victim)) {
            return ApplyResult.failure();
        }

        return ApplyResult.success();
    }

    // Should be overridden by subclasses as deemed appropriate
    protected ApplyResult applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
        return ApplyResult.success();
    }

    protected boolean shouldSubside(Battle b, ActivePokemon victim) {
        return numTurns == 0;
    }

    // Prints the subside message and deactivates the effect
    public void subside(Battle b, ActivePokemon p) {
        Messages.add(getSubsideMessage(p));
        this.deactivate();
    }

    protected void addCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source, boolean printCast) {
        if (printCast) {
            Messages.add(this.getCastMessage(b, user, victim, source));
        }
    }

    protected String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
        return "";
    }

    @Override
    public String getSubsideMessage(ActivePokemon victim) {
        return "";
    }

    public String getSourcePreventMessage(ActivePokemon victim, String sourceName) {
        return DEFAULT_FAIL_MESSAGE;
    }

    public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
        return this.getSubsideMessage(victim);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    // Returns the number of turns left that the Effect will be in play (-1 for permanent effects)
    @Override
    public int getTurns() {
        return numTurns;
    }

    public void setTurns(int turns) {
        numTurns = turns;
    }

    @Override
    public String toString() {
        return this.namesies() + " " + this.getTurns();
    }
}
