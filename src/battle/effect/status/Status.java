package battle.effect.status;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.CastSource;
import battle.effect.Effect;
import battle.effect.EffectInterfaces.OpponentStatusReceivedEffect;
import battle.effect.EffectInterfaces.StatusPreventionEffect;
import battle.effect.EffectInterfaces.StatusReceivedEffect;
import battle.effect.InvokeEffect;
import main.Global;
import message.MessageUpdate;
import message.Messages;
import pokemon.PartyPokemon;
import util.serialization.Serializable;

public abstract class Status implements InvokeEffect, Serializable {
    private static final long serialVersionUID = 1L;

    private final StatusNamesies statusCondition;

    protected Status(StatusNamesies statusCondition) {
        this.statusCondition = statusCondition;
    }

    protected abstract boolean statusApplies(Battle b, ActivePokemon caster, ActivePokemon victim);

    protected abstract String getGenericCastMessage(ActivePokemon p);
    protected abstract String getSourceCastMessage(ActivePokemon sourcerer, ActivePokemon victim, String sourceName);

    protected abstract String getGenericRemoveMessage(ActivePokemon victim);
    protected abstract String getSourceRemoveMessage(ActivePokemon victim, String sourceName);

    private String getCastMessage(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
        if (source.hasSourceName()) {
            return this.getSourceCastMessage(caster, victim, source.getSourceName(b, caster));
        } else {
            return this.getGenericCastMessage(victim);
        }
    }

    private String getRemoveMessage(Battle b, ActivePokemon victim, CastSource source) {
        if (source.hasSourceName()) {
            return this.getSourceRemoveMessage(victim, source.getSourceName(b, victim));
        } else {
            return this.getGenericRemoveMessage(victim);
        }
    }

    private String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
        StatusPreventionEffect statusPrevent = StatusPreventionEffect.getPreventEffect(b, user, victim, this.statusCondition);
        if (statusPrevent != null) {
            return statusPrevent.statusPreventionMessage(victim);
        }

        return Effect.DEFAULT_FAIL_MESSAGE;
    }

    private boolean appliesWithoutStatusCheck(Battle b, ActivePokemon caster, ActivePokemon victim) {
        return this.statusApplies(b, caster, victim) &&
                StatusPreventionEffect.getPreventEffect(b, caster, victim, this.statusCondition) == null;
    }

    protected boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim) {
        return !victim.hasStatus() && this.appliesWithoutStatusCheck(b, caster, victim);
    }

    public StatusNamesies getType() {
        return statusCondition;
    }

    public boolean isType(StatusNamesies statusCondition) {
        return this.getType() == statusCondition;
    }

    public int getTurns() {
        return -1;
    }

    public void setTurns(int turns) {}

    @Override
    public InvokeSource getSource() {
        return InvokeSource.EFFECT;
    }

    @Override
    public String toString() {
        return this.statusCondition + " " + this.getTurns();
    }

    public static void removeStatus(Battle b, ActivePokemon victim, CastSource source) {
        Status status = victim.getStatus();
        victim.removeStatus();

        Messages.add(new MessageUpdate(status.getRemoveMessage(b, victim, source)).updatePokemon(b, victim));
    }

    public static String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim, StatusNamesies status) {
        return status.getStatus().getFailMessage(b, user, victim);
    }

    public static boolean appliesWithoutStatusCheck(StatusNamesies status, Battle b, ActivePokemon caster, ActivePokemon victim) {
        return status.getStatus().appliesWithoutStatusCheck(b, caster, victim);
    }

    public static boolean applies(StatusNamesies status, Battle b, ActivePokemon caster, ActivePokemon victim) {
        return status.getStatus().applies(b, caster, victim);
    }

    // Returns true if a status was successfully given, and false if it failed for any reason
    public static boolean applyStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusNamesies status) {
        return applyStatus(b, caster, victim, status, CastSource.EFFECT);
    }

    public static boolean applyStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusNamesies status, CastSource source) {
        Status s = status.getStatus();
        return applyStatus(b, caster, victim, status, s.getCastMessage(b, caster, victim, source));
    }

    public static boolean applyStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusNamesies status, String castMessage) {
        Status s = status.getStatus();
        if (s.applies(b, caster, victim)) {
            victim.setStatus(s);
            Messages.add(new MessageUpdate(castMessage).updatePokemon(b, victim));

            StatusReceivedEffect.invokeStatusReceivedEffect(b, caster, victim, status);
            OpponentStatusReceivedEffect.invokeOpponentStatusReceivedEffect(b, victim, status);
            return true;
        }

        return false;
    }

    public static void removeStatus(PartyPokemon p) {
        p.setStatus(new NoStatus());
    }

    public static void die(Battle b, ActivePokemon murderer, ActivePokemon deady) {
        if (deady.getHP() > 0) {
            Global.error("Only dead Pokemon can die.");
        }

        applyStatus(b, murderer, deady, StatusNamesies.FAINTED);
    }
}
