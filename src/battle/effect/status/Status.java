package battle.effect.status;

import battle.Battle;
import battle.effect.MessageGetter;
import battle.effect.generic.CastSource;
import battle.effect.generic.Effect;
import battle.effect.generic.EffectInterfaces.OpponentStatusReceivedEffect;
import battle.effect.generic.EffectInterfaces.StatusPreventionEffect;
import battle.effect.generic.EffectInterfaces.StatusReceivedEffect;
import battle.effect.generic.EffectNamesies;
import main.Global;
import message.MessageUpdate;
import message.Messages;
import pokemon.ActivePokemon;

import java.io.Serializable;

public abstract class Status implements Serializable {
	private static final long serialVersionUID = 1L;

	private final StatusCondition statusCondition;
	private final MessageGetter removeMessageGetter;

	protected Status(StatusCondition statusCondition) {
		this.statusCondition = statusCondition;
		this.removeMessageGetter = new MessageGetter() {
			@Override
			public String getGenericMessage(ActivePokemon p) {
				return getGenericRemoveMessage(p);
			}

			@Override
			public String getSourceMessage(ActivePokemon p, String sourceName) {
				return getSourceRemoveMessage(p, sourceName);
			}
		};
	}

	protected abstract boolean statusApplies(Battle b, ActivePokemon caster, ActivePokemon victim);

	protected abstract String getGenericRemoveMessage(ActivePokemon victim);
	protected abstract String getSourceRemoveMessage(ActivePokemon victim, String sourceName);

	protected abstract String getCastMessage(ActivePokemon p);
	protected abstract String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim);

	// A method to be overridden if anything related to conflicted victim is necessary to create this status
	protected void postCreateEffect(ActivePokemon victim) {}

	public static void removeStatus(Battle b, ActivePokemon victim, CastSource source) {
		Status status = victim.getStatus();
		victim.removeStatus();

		Messages.add(new MessageUpdate(status.removeMessageGetter.getMessage(b, victim, source)).updatePokemon(b, victim));
	}

	public static String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim, StatusCondition status) {
		return getStatus(status, victim).getFailMessage(b, user, victim);
	}

	private String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
		StatusPreventionEffect statusPrevent = StatusPreventionEffect.getPreventEffect(b, user, victim, this.statusCondition);
		if (statusPrevent != null) {
			return statusPrevent.statusPreventionMessage(victim);
		}

		return Effect.DEFAULT_FAIL_MESSAGE;
	}

	// Creates a new status like a motherfucking champ
	private static Status getStatus(StatusCondition s, ActivePokemon victim) {
		Status status = s.getStatus();
		status.postCreateEffect(victim);

		return status;
	}

	public static boolean appliesWithoutStatusCheck(StatusCondition status, Battle b, ActivePokemon caster, ActivePokemon victim) {
		return getStatus(status, victim).appliesWithoutStatusCheck(b, caster, victim);
	}

	public static boolean applies(StatusCondition status, Battle b, ActivePokemon caster, ActivePokemon victim) {
		return getStatus(status, victim).applies(b, caster, victim);
	}

	private boolean appliesWithoutStatusCheck(Battle b, ActivePokemon caster, ActivePokemon victim) {
		return this.statusApplies(b, caster, victim) &&
				StatusPreventionEffect.getPreventEffect(b, caster, victim, this.statusCondition) == null;

	}

	protected boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim) {
		return !victim.hasStatus() && this.appliesWithoutStatusCheck(b, caster, victim);
	}

	public StatusCondition getType() {
		return statusCondition;
	}

	public void setTurns(int turns) {}

	// Returns true if a status was successfully given, and false if it failed for any reason
	public static boolean giveStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
		return giveStatus(b, caster, victim, status, false);
	}

	public static boolean giveStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status, boolean abilityCast) {
		Status s = getStatus(status, victim);
		return giveStatus(b, caster, victim, status, abilityCast ? s.getAbilityCastMessage(caster, victim) : s.getCastMessage(victim));
	}

	public static boolean giveStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status, String castMessage) {
		Status s = getStatus(status, victim);
		if (s.applies(b, caster, victim)) {
			victim.setStatus(s);
			Messages.add(new MessageUpdate(castMessage).updatePokemon(b, victim));

            StatusReceivedEffect.invokeStatusReceivedEffect(b, caster, victim, status);
			OpponentStatusReceivedEffect.invokeOpponentStatusReceivedEffect(b, victim, status);
			return true;
		}

		return false;
	}

	public static void removeStatus(ActivePokemon p) {
		p.setStatus(new NoStatus());

		// TODO: There should be a way for effects to be tied to status conditions so that they don't have to be hardcoded here
		p.getAttributes().removeEffect(EffectNamesies.NIGHTMARE);
		p.getAttributes().removeEffect(EffectNamesies.BAD_POISON);
	}

	public static void die(Battle b, ActivePokemon murderer, ActivePokemon deady) {
		if (deady.getHP() > 0) {
			Global.error("Only dead Pokemon can die.");
		}

		giveStatus(b, murderer, deady, StatusCondition.FAINTED);
	}
}
