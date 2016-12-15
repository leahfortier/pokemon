package battle.effect.generic;

import battle.Battle;
import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.attack.MoveCategory;
import battle.attack.MoveType;
import battle.effect.PassableEffect;
import battle.effect.SapHealthEffect;
import battle.effect.attack.ChangeAbilityMove;
import battle.effect.attack.ChangeTypeMove;
import battle.effect.generic.EffectInterfaces.AbsorbDamageEffect;
import battle.effect.generic.EffectInterfaces.AccuracyBypassEffect;
import battle.effect.generic.EffectInterfaces.AdvantageChanger;
import battle.effect.generic.EffectInterfaces.AlwaysCritEffect;
import battle.effect.generic.EffectInterfaces.AttackSelectionEffect;
import battle.effect.generic.EffectInterfaces.BeforeTurnEffect;
import battle.effect.generic.EffectInterfaces.BracingEffect;
import battle.effect.generic.EffectInterfaces.ChangeAttackTypeEffect;
import battle.effect.generic.EffectInterfaces.ChangeMoveListEffect;
import battle.effect.generic.EffectInterfaces.ChangeTypeEffect;
import battle.effect.generic.EffectInterfaces.CrashDamageMove;
import battle.effect.generic.EffectInterfaces.CritStageEffect;
import battle.effect.generic.EffectInterfaces.DamageTakenEffect;
import battle.effect.generic.EffectInterfaces.DefogRelease;
import battle.effect.generic.EffectInterfaces.DifferentStatEffect;
import battle.effect.generic.EffectInterfaces.EffectBlockerEffect;
import battle.effect.generic.EffectInterfaces.EndTurnEffect;
import battle.effect.generic.EffectInterfaces.FaintEffect;
import battle.effect.generic.EffectInterfaces.ForceMoveEffect;
import battle.effect.generic.EffectInterfaces.GroundedEffect;
import battle.effect.generic.EffectInterfaces.HalfWeightEffect;
import battle.effect.generic.EffectInterfaces.LevitationEffect;
import battle.effect.generic.EffectInterfaces.OpponentAccuracyBypassEffect;
import battle.effect.generic.EffectInterfaces.OpponentBeforeTurnEffect;
import battle.effect.generic.EffectInterfaces.OpponentTrappingEffect;
import battle.effect.generic.EffectInterfaces.PowerChangeEffect;
import battle.effect.generic.EffectInterfaces.RapidSpinRelease;
import battle.effect.generic.EffectInterfaces.StageChangingEffect;
import battle.effect.generic.EffectInterfaces.StatChangingEffect;
import battle.effect.generic.EffectInterfaces.StatProtectingEffect;
import battle.effect.generic.EffectInterfaces.StatSwitchingEffect;
import battle.effect.generic.EffectInterfaces.StatusPreventionEffect;
import battle.effect.generic.EffectInterfaces.TargetSwapperEffect;
import battle.effect.generic.EffectInterfaces.TrappingEffect;
import battle.effect.holder.AbilityHolder;
import battle.effect.holder.ItemHolder;
import battle.effect.status.Status;
import battle.effect.status.StatusCondition;
import item.Item;
import item.ItemNamesies;
import main.Global;
import main.Type;
import message.MessageUpdate;
import message.Messages;
import pokemon.ActivePokemon;
import pokemon.Gender;
import pokemon.Stat;
import pokemon.ability.Ability;
import pokemon.ability.AbilityNamesies;
import util.RandomUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Class to handle effects that are on a single Pokemon
public abstract class PokemonEffect extends Effect implements Serializable {
	private static final long serialVersionUID = 1L;

	public PokemonEffect(EffectNamesies name, int minTurns, int maxTurns, boolean nextTurnSubside) {
		super(name, minTurns, maxTurns, nextTurnSubside);
	}
	
	public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
		if (printCast) {
			Messages.add(new MessageUpdate(getCastMessage(b, caster, victim)));
		}

		victim.addEffect(this);
		
		Messages.add(new MessageUpdate().updatePokemon(b, caster));
		Messages.add(new MessageUpdate().updatePokemon(b, victim));
	}

	// EVERYTHING BELOW IS GENERATED ###
	/**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

	static class LeechSeed extends PokemonEffect implements EndTurnEffect, RapidSpinRelease, PassableEffect, SapHealthEffect {
		private static final long serialVersionUID = 1L;

		LeechSeed() {
			super(EffectNamesies.LEECH_SEED, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.isType(b, Type.GRASS) || victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " was seeded!";
		}

		public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (victim.isType(b, Type.GRASS)) {
				return "It doesn't affect " + victim.getName() + "!";
			}
			else if (victim.hasEffect(this.namesies)) {
				return victim.getName() + " is already seeded!";
			}
			
			return super.getFailMessage(b, user, victim);
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(new MessageUpdate(this.getSapMessage(victim)));
			this.sapHealth(b, b.getOtherPokemon(victim), victim, victim.reduceHealthFraction(b, 1/8.0), false);
		}

		public void releaseRapidSpin(Battle b, ActivePokemon releaser) {
			Messages.add(new MessageUpdate(releaser.getName() + " was released from leech seed!"));
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			releaser.getEffects().remove(this);
			b.getEffects(releaser.isPlayer()).remove(this);
		}
	}

	static class BadPoison extends PokemonEffect implements EndTurnEffect {
		private static final long serialVersionUID = 1L;
		private int turns;

		BadPoison() {
			super(EffectNamesies.BAD_POISON, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(!Status.applies(StatusCondition.POISONED, b, caster, victim));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			Status.giveStatus(b, caster, victim, StatusCondition.POISONED);
		}

		public int getTurns() {
			return turns;
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			turns++;
		}
	}

	static class Flinch extends PokemonEffect implements BeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		Flinch() {
			super(EffectNamesies.FLINCH, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !((victim.hasAbility(AbilityNamesies.INNER_FOCUS) && !caster.breaksTheMold()) || !b.isFirstAttack() || victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (victim.hasAbility(AbilityNamesies.STEADFAST)) {
				victim.getAttributes().modifyStage(victim, victim, 1, Stat.SPEED, b, CastSource.ABILITY);
			}
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " flinched!";
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			return false;
		}
	}

	static class FireSpin extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
		private static final long serialVersionUID = 1L;

		FireSpin() {
			super(EffectNamesies.FIRE_SPIN, 4, 5, true);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, ItemNamesies.GRIP_CLAW)) setTurns(5);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " was trapped in the fiery vortex!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return victim.getName() + " is no longer trapped by fire spin.";
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(new MessageUpdate(victim.getName() + " is hurt by fire spin!"));
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim.isPlayer()).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0);
		}

		public boolean isTrapped(Battle b, ActivePokemon escaper) {
			// Ghost-type Pokemon can always escape
			return !escaper.isType(b, Type.GHOST);
		}

		public String trappingMessage(ActivePokemon trapped) {
			return trapped.getName() + " cannot be recalled due to fire spin!";
		}

		public void releaseRapidSpin(Battle b, ActivePokemon releaser) {
			Messages.add(new MessageUpdate(releaser.getName() + " was released from fire spin!"));
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			releaser.getEffects().remove(this);
			b.getEffects(releaser.isPlayer()).remove(this);
		}
	}

	static class Infestation extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
		private static final long serialVersionUID = 1L;

		Infestation() {
			super(EffectNamesies.INFESTATION, 4, 5, true);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, ItemNamesies.GRIP_CLAW)) setTurns(5);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " has been afflicted with an infestation!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return victim.getName() + " is no longer trapped by infestation.";
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(new MessageUpdate(victim.getName() + " is hurt by infestation!"));
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim.isPlayer()).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0);
		}

		public boolean isTrapped(Battle b, ActivePokemon escaper) {
			// Ghost-type Pokemon can always escape
			return !escaper.isType(b, Type.GHOST);
		}

		public String trappingMessage(ActivePokemon trapped) {
			return trapped.getName() + " cannot be recalled due to infestation!";
		}

		public void releaseRapidSpin(Battle b, ActivePokemon releaser) {
			Messages.add(new MessageUpdate(releaser.getName() + " was released from infestation!"));
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			releaser.getEffects().remove(this);
			b.getEffects(releaser.isPlayer()).remove(this);
		}
	}

	static class MagmaStorm extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
		private static final long serialVersionUID = 1L;

		MagmaStorm() {
			super(EffectNamesies.MAGMA_STORM, 4, 5, true);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, ItemNamesies.GRIP_CLAW)) setTurns(5);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " was trapped by swirling magma!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return victim.getName() + " is no longer trapped by magma storm.";
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(new MessageUpdate(victim.getName() + " is hurt by magma storm!"));
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim.isPlayer()).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0);
		}

		public boolean isTrapped(Battle b, ActivePokemon escaper) {
			// Ghost-type Pokemon can always escape
			return !escaper.isType(b, Type.GHOST);
		}

		public String trappingMessage(ActivePokemon trapped) {
			return trapped.getName() + " cannot be recalled due to magma storm!";
		}

		public void releaseRapidSpin(Battle b, ActivePokemon releaser) {
			Messages.add(new MessageUpdate(releaser.getName() + " was released from magma storm!"));
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			releaser.getEffects().remove(this);
			b.getEffects(releaser.isPlayer()).remove(this);
		}
	}

	static class Clamped extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
		private static final long serialVersionUID = 1L;

		Clamped() {
			super(EffectNamesies.CLAMPED, 4, 5, true);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, ItemNamesies.GRIP_CLAW)) setTurns(5);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " clamped " + victim.getName() + "!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return victim.getName() + " is no longer trapped by clamp.";
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(new MessageUpdate(victim.getName() + " is hurt by clamp!"));
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim.isPlayer()).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0);
		}

		public boolean isTrapped(Battle b, ActivePokemon escaper) {
			// Ghost-type Pokemon can always escape
			return !escaper.isType(b, Type.GHOST);
		}

		public String trappingMessage(ActivePokemon trapped) {
			return trapped.getName() + " cannot be recalled due to clamp!";
		}

		public void releaseRapidSpin(Battle b, ActivePokemon releaser) {
			Messages.add(new MessageUpdate(releaser.getName() + " was released from clamp!"));
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			releaser.getEffects().remove(this);
			b.getEffects(releaser.isPlayer()).remove(this);
		}
	}

	static class Whirlpooled extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
		private static final long serialVersionUID = 1L;

		Whirlpooled() {
			super(EffectNamesies.WHIRLPOOLED, 4, 5, true);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, ItemNamesies.GRIP_CLAW)) setTurns(5);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " was trapped in the vortex!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return victim.getName() + " is no longer trapped by whirlpool.";
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(new MessageUpdate(victim.getName() + " is hurt by whirlpool!"));
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim.isPlayer()).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0);
		}

		public boolean isTrapped(Battle b, ActivePokemon escaper) {
			// Ghost-type Pokemon can always escape
			return !escaper.isType(b, Type.GHOST);
		}

		public String trappingMessage(ActivePokemon trapped) {
			return trapped.getName() + " cannot be recalled due to whirlpool!";
		}

		public void releaseRapidSpin(Battle b, ActivePokemon releaser) {
			Messages.add(new MessageUpdate(releaser.getName() + " was released from whirlpool!"));
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			releaser.getEffects().remove(this);
			b.getEffects(releaser.isPlayer()).remove(this);
		}
	}

	static class Wrapped extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
		private static final long serialVersionUID = 1L;

		Wrapped() {
			super(EffectNamesies.WRAPPED, 4, 5, true);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, ItemNamesies.GRIP_CLAW)) setTurns(5);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " was wrapped by " + user.getName() + "!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return victim.getName() + " is no longer trapped by wrap.";
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(new MessageUpdate(victim.getName() + " is hurt by wrap!"));
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim.isPlayer()).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0);
		}

		public boolean isTrapped(Battle b, ActivePokemon escaper) {
			// Ghost-type Pokemon can always escape
			return !escaper.isType(b, Type.GHOST);
		}

		public String trappingMessage(ActivePokemon trapped) {
			return trapped.getName() + " cannot be recalled due to wrap!";
		}

		public void releaseRapidSpin(Battle b, ActivePokemon releaser) {
			Messages.add(new MessageUpdate(releaser.getName() + " was released from wrap!"));
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			releaser.getEffects().remove(this);
			b.getEffects(releaser.isPlayer()).remove(this);
		}
	}

	static class Binded extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
		private static final long serialVersionUID = 1L;

		Binded() {
			super(EffectNamesies.BINDED, 4, 5, true);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, ItemNamesies.GRIP_CLAW)) setTurns(5);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " was binded by " + user.getName() + "!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return victim.getName() + " is no longer trapped by bind.";
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(new MessageUpdate(victim.getName() + " is hurt by bind!"));
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim.isPlayer()).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0);
		}

		public boolean isTrapped(Battle b, ActivePokemon escaper) {
			// Ghost-type Pokemon can always escape
			return !escaper.isType(b, Type.GHOST);
		}

		public String trappingMessage(ActivePokemon trapped) {
			return trapped.getName() + " cannot be recalled due to bind!";
		}

		public void releaseRapidSpin(Battle b, ActivePokemon releaser) {
			Messages.add(new MessageUpdate(releaser.getName() + " was released from bind!"));
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			releaser.getEffects().remove(this);
			b.getEffects(releaser.isPlayer()).remove(this);
		}
	}

	static class SandTomb extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
		private static final long serialVersionUID = 1L;

		SandTomb() {
			super(EffectNamesies.SAND_TOMB, 4, 5, true);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, ItemNamesies.GRIP_CLAW)) setTurns(5);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " was trapped by sand tomb!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return victim.getName() + " is no longer trapped by sand tomb.";
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(new MessageUpdate(victim.getName() + " is hurt by sand tomb!"));
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim.isPlayer()).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0);
		}

		public boolean isTrapped(Battle b, ActivePokemon escaper) {
			// Ghost-type Pokemon can always escape
			return !escaper.isType(b, Type.GHOST);
		}

		public String trappingMessage(ActivePokemon trapped) {
			return trapped.getName() + " cannot be recalled due to sand tomb!";
		}

		public void releaseRapidSpin(Battle b, ActivePokemon releaser) {
			Messages.add(new MessageUpdate(releaser.getName() + " was released from sand tomb!"));
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			releaser.getEffects().remove(this);
			b.getEffects(releaser.isPlayer()).remove(this);
		}
	}

	static class KingsShield extends PokemonEffect implements OpponentBeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		KingsShield() {
			super(EffectNamesies.KINGS_SHIELD, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public boolean protectingCondition(Battle b, ActivePokemon attacking) {
			return true;
		}

		public void protectingEffects(ActivePokemon p, ActivePokemon opp, Battle b) {
			// Pokemon that make contact with the king's shield have their attack reduced
			if (p.getAttack().isMoveType(MoveType.PHYSICAL_CONTACT)) {
				p.getAttributes().modifyStage(opp, p, -2, Stat.ATTACK, b, CastSource.EFFECT, "The King's Shield {change} " + p.getName() + "'s attack!");
			}
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!RandomUtils.chanceTest((int)(100*caster.getAttributes().getSuccessionDecayRate()))) {
				Messages.add(new MessageUpdate(this.getFailMessage(b, caster, victim)));
				return;
			}
			
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " protected itself!";
		}

		public boolean opposingCanAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			// Self-target moves, moves that penetrate Protect, and other conditions
			if (p.getAttack().isSelfTarget() || p.getAttack().isMoveType(MoveType.FIELD) || p.getAttack().isMoveType(MoveType.PROTECT_PIERCING) || !protectingCondition(b, p)) {
				return true;
			}
			
			// Protect is a success!
			b.printAttacking(p);
			Messages.add(new MessageUpdate(opp.getName() + " is protecting itself!"));
			CrashDamageMove.invokeCrashDamageMove(b, p);
			
			// Additional Effects
			protectingEffects(p, opp, b);
			
			return false;
		}
	}

	static class SpikyShield extends PokemonEffect implements OpponentBeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		SpikyShield() {
			super(EffectNamesies.SPIKY_SHIELD, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public boolean protectingCondition(Battle b, ActivePokemon attacking) {
			return true;
		}

		public void protectingEffects(ActivePokemon p, ActivePokemon opp, Battle b) {
			// Pokemon that make contact with the spiky shield have their health reduced
			if (p.getAttack().isMoveType(MoveType.PHYSICAL_CONTACT)) {
				Messages.add(new MessageUpdate(p.getName() + " was hurt by " + opp.getName() + "'s Spiky Shield!"));
				p.reduceHealthFraction(b, 1/8.0);
			}
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!RandomUtils.chanceTest((int)(100*caster.getAttributes().getSuccessionDecayRate()))) {
				Messages.add(new MessageUpdate(this.getFailMessage(b, caster, victim)));
				return;
			}
			
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " protected itself!";
		}

		public boolean opposingCanAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			// Self-target moves, moves that penetrate Protect, and other conditions
			if (p.getAttack().isSelfTarget() || p.getAttack().isMoveType(MoveType.FIELD) || p.getAttack().isMoveType(MoveType.PROTECT_PIERCING) || !protectingCondition(b, p)) {
				return true;
			}
			
			// Protect is a success!
			b.printAttacking(p);
			Messages.add(new MessageUpdate(opp.getName() + " is protecting itself!"));
			CrashDamageMove.invokeCrashDamageMove(b, p);
			
			// Additional Effects
			protectingEffects(p, opp, b);
			
			return false;
		}
	}

	static class BanefulBunker extends PokemonEffect implements OpponentBeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		BanefulBunker() {
			super(EffectNamesies.BANEFUL_BUNKER, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public boolean protectingCondition(Battle b, ActivePokemon attacking) {
			return true;
		}

		public void protectingEffects(ActivePokemon p, ActivePokemon opp, Battle b) {
			// Pokemon that make contact with the baneful bunker are become poisoned
			if (p.getAttack().isMoveType(MoveType.PHYSICAL_CONTACT) && Status.applies(StatusCondition.POISONED, b, opp, p)) {
				Status.giveStatus(b, opp, p, StatusCondition.POISONED, p.getName() + " was poisoned by " + opp.getName() + "'s Baneful Bunker!");
			}
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!RandomUtils.chanceTest((int)(100*caster.getAttributes().getSuccessionDecayRate()))) {
				Messages.add(new MessageUpdate(this.getFailMessage(b, caster, victim)));
				return;
			}
			
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " protected itself!";
		}

		public boolean opposingCanAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			// Self-target moves, moves that penetrate Protect, and other conditions
			if (p.getAttack().isSelfTarget() || p.getAttack().isMoveType(MoveType.FIELD) || p.getAttack().isMoveType(MoveType.PROTECT_PIERCING) || !protectingCondition(b, p)) {
				return true;
			}
			
			// Protect is a success!
			b.printAttacking(p);
			Messages.add(new MessageUpdate(opp.getName() + " is protecting itself!"));
			CrashDamageMove.invokeCrashDamageMove(b, p);
			
			// Additional Effects
			protectingEffects(p, opp, b);
			
			return false;
		}
	}

	static class Protecting extends PokemonEffect implements OpponentBeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		Protecting() {
			super(EffectNamesies.PROTECTING, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public boolean protectingCondition(Battle b, ActivePokemon attacking) {
			return true;
		}

		public void protectingEffects(ActivePokemon p, ActivePokemon opp, Battle b) {
			// No additional effects
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!RandomUtils.chanceTest((int)(100*caster.getAttributes().getSuccessionDecayRate()))) {
				Messages.add(new MessageUpdate(this.getFailMessage(b, caster, victim)));
				return;
			}
			
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " protected itself!";
		}

		public boolean opposingCanAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			// Self-target moves, moves that penetrate Protect, and other conditions
			if (p.getAttack().isSelfTarget() || p.getAttack().isMoveType(MoveType.FIELD) || p.getAttack().isMoveType(MoveType.PROTECT_PIERCING) || !protectingCondition(b, p)) {
				return true;
			}
			
			// Protect is a success!
			b.printAttacking(p);
			Messages.add(new MessageUpdate(opp.getName() + " is protecting itself!"));
			CrashDamageMove.invokeCrashDamageMove(b, p);
			
			// Additional Effects
			protectingEffects(p, opp, b);
			
			return false;
		}
	}

	static class QuickGuard extends PokemonEffect implements OpponentBeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		QuickGuard() {
			super(EffectNamesies.QUICK_GUARD, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public boolean protectingCondition(Battle b, ActivePokemon attacking) {
			return attacking.getAttack().getPriority(b, attacking) > 0;
		}

		public void protectingEffects(ActivePokemon p, ActivePokemon opp, Battle b) {
			// No additional effects
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!RandomUtils.chanceTest((int)(100*caster.getAttributes().getSuccessionDecayRate()))) {
				Messages.add(new MessageUpdate(this.getFailMessage(b, caster, victim)));
				return;
			}
			
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " protected itself!";
		}

		public boolean opposingCanAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			// Self-target moves, moves that penetrate Protect, and other conditions
			if (p.getAttack().isSelfTarget() || p.getAttack().isMoveType(MoveType.FIELD) || p.getAttack().isMoveType(MoveType.PROTECT_PIERCING) || !protectingCondition(b, p)) {
				return true;
			}
			
			// Protect is a success!
			b.printAttacking(p);
			Messages.add(new MessageUpdate(opp.getName() + " is protecting itself!"));
			CrashDamageMove.invokeCrashDamageMove(b, p);
			
			// Additional Effects
			protectingEffects(p, opp, b);
			
			return false;
		}
	}

	static class CraftyShield extends PokemonEffect implements OpponentBeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		CraftyShield() {
			super(EffectNamesies.CRAFTY_SHIELD, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public boolean protectingCondition(Battle b, ActivePokemon attacking) {
			return attacking.getAttack().getCategory() == MoveCategory.STATUS;
		}

		public void protectingEffects(ActivePokemon p, ActivePokemon opp, Battle b) {
			// No additional effects
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!RandomUtils.chanceTest((int)(100*caster.getAttributes().getSuccessionDecayRate()))) {
				Messages.add(new MessageUpdate(this.getFailMessage(b, caster, victim)));
				return;
			}
			
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " protected itself!";
		}

		public boolean opposingCanAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			// Self-target moves, moves that penetrate Protect, and other conditions
			if (p.getAttack().isSelfTarget() || p.getAttack().isMoveType(MoveType.FIELD) || p.getAttack().isMoveType(MoveType.PROTECT_PIERCING) || !protectingCondition(b, p)) {
				return true;
			}
			
			// Protect is a success!
			b.printAttacking(p);
			Messages.add(new MessageUpdate(opp.getName() + " is protecting itself!"));
			CrashDamageMove.invokeCrashDamageMove(b, p);
			
			// Additional Effects
			protectingEffects(p, opp, b);
			
			return false;
		}
	}

	static class MatBlock extends PokemonEffect implements OpponentBeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		MatBlock() {
			super(EffectNamesies.MAT_BLOCK, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public boolean protectingCondition(Battle b, ActivePokemon attacking) {
			return attacking.getAttack().getCategory() != MoveCategory.STATUS;
		}

		public void protectingEffects(ActivePokemon p, ActivePokemon opp, Battle b) {
			// No additional effects
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			// No successive decay for this move
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " protected itself!";
		}

		public boolean opposingCanAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			// Self-target moves, moves that penetrate Protect, and other conditions
			if (p.getAttack().isSelfTarget() || p.getAttack().isMoveType(MoveType.FIELD) || p.getAttack().isMoveType(MoveType.PROTECT_PIERCING) || !protectingCondition(b, p)) {
				return true;
			}
			
			// Protect is a success!
			b.printAttacking(p);
			Messages.add(new MessageUpdate(opp.getName() + " is protecting itself!"));
			CrashDamageMove.invokeCrashDamageMove(b, p);
			
			// Additional Effects
			protectingEffects(p, opp, b);
			
			return false;
		}
	}

	static class Bracing extends PokemonEffect implements BracingEffect {
		private static final long serialVersionUID = 1L;

		Bracing() {
			super(EffectNamesies.BRACING, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!RandomUtils.chanceTest((int)(100*caster.getAttributes().getSuccessionDecayRate()))) {
				Messages.add(new MessageUpdate(this.getFailMessage(b, caster, victim)));
				return;
			}
			
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " braced itself!";
		}

		public boolean isBracing(Battle b, ActivePokemon bracer, boolean fullHealth) {
			return true;
		}

		public String braceMessage(ActivePokemon bracer) {
			return bracer.getName() + " endured the hit!";
		}
	}

	static class Confusion extends PokemonEffect implements PassableEffect, BeforeTurnEffect {
		private static final long serialVersionUID = 1L;
		private int turns;

		Confusion() {
			super(EffectNamesies.CONFUSION, -1, -1, false);
			this.turns = RandomUtils.getRandomInt(1, 4); // Between 1 and 4 turns
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !((victim.hasAbility(AbilityNamesies.OWN_TEMPO) && !caster.breaksTheMold()) || victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (victim.isHoldingItem(b, ItemNamesies.PERSIM_BERRY)) {
				Messages.add(new MessageUpdate(victim.getName() + "'s " + ItemNamesies.PERSIM_BERRY.getName() + " snapped it out of confusion!"));
				victim.getAttributes().removeEffect(this.namesies);
				victim.consumeItem(b);
			}
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " became confused!";
		}

		public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (victim.hasEffect(this.namesies)) {
				return victim.getName() + " is already confused!";
			}
			else if (victim.hasAbility(AbilityNamesies.OWN_TEMPO)) {
				return victim.getName() + "'s " + victim.getAbility().getName() + " prevents confusion!";
			}
			
			return super.getFailMessage(b, user, victim);
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			// Snap it out!
			if (turns == 0) {
				Messages.add(new MessageUpdate(p.getName() + " snapped out of its confusion!"));
				super.active = false;
				return true;
			}
			
			turns--;
			Messages.add(new MessageUpdate(p.getName() + " is confused!"));
			
			// 50% chance to hurt yourself in confusion while confused
			if (RandomUtils.chanceTest(50)) {
				Messages.add(new MessageUpdate("It hurt itself in confusion!"));
				
				// Perform confusion damage
				Move temp = p.getMove();
				p.setMove(new Move(AttackNamesies.CONFUSION_DAMAGE.getAttack()));
				p.reduceHealth(b, b.calculateDamage(p, p));
				p.setMove(temp);
				
				return false;
			}
			
			return true;
		}
	}

	static class SelfConfusion extends PokemonEffect implements ForceMoveEffect {
		private static final long serialVersionUID = 1L;
		private Move move;

		SelfConfusion() {
			super(EffectNamesies.SELF_CONFUSION, 2, 3, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			move = caster.getMove();
			super.cast(b, caster, victim, source, printCast);
		}

		public void subside(Battle b, ActivePokemon p) {
			Confusion c = new Confusion();
			if (c.applies(b, p, p, CastSource.EFFECT)) {
				Messages.add(new MessageUpdate(p.getName() + " became confused due to fatigue!"));
				p.addEffect(c);
			}
		}

		public Move getForcedMove() {
			return move;
		}
	}

	static class Safeguard extends PokemonEffect implements DefogRelease, StatusPreventionEffect {
		private static final long serialVersionUID = 1L;

		Safeguard() {
			super(EffectNamesies.SAFEGUARD, 5, 5, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " is covered by a veil!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The effects of " + victim.getName() + "'s Safeguard faded.";
		}

		public void releaseDefog(Battle b, ActivePokemon victim) {
			Messages.add(new MessageUpdate("The effects of " + victim.getName() + "'s Safeguard faded."));
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both lists
			victim.getEffects().remove(this);
			b.getEffects(victim.isPlayer()).remove(this);
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
			return !caster.hasAbility(AbilityNamesies.INFILTRATOR);
		}

		public String statusPreventionMessage(ActivePokemon victim) {
			return "Safeguard protects " + victim.getName() + " from status conditions!";
		}
	}

	static class GuardSpecial extends PokemonEffect implements StatusPreventionEffect {
		private static final long serialVersionUID = 1L;

		GuardSpecial() {
			super(EffectNamesies.GUARD_SPECIAL, 5, 5, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " is covered by a veil!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The effects of " + victim.getName() + "'s Guard Special faded.";
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
			return !caster.hasAbility(AbilityNamesies.INFILTRATOR);
		}

		public String statusPreventionMessage(ActivePokemon victim) {
			return "Guard Special protects " + victim.getName() + " from status conditions!";
		}
	}

	static class Encore extends PokemonEffect implements AttackSelectionEffect, ForceMoveEffect, BeforeTurnEffect, EndTurnEffect {
		private static final long serialVersionUID = 1L;
		private Move move;

		Encore() {
			super(EffectNamesies.ENCORE, 3, 3, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !((victim.hasAbility(AbilityNamesies.AROMA_VEIL) && !caster.breaksTheMold()) || victim.getAttributes().getLastMoveUsed() == null || victim.getAttributes().getLastMoveUsed().getPP() == 0 || victim.getAttributes().getLastMoveUsed().getAttack().isMoveType(MoveType.ENCORELESS) || victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			move = victim.getAttributes().getLastMoveUsed();
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " got an encore!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The effects of " + victim.getName() + "'s encore faded.";
		}

		public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (victim.hasAbility(AbilityNamesies.AROMA_VEIL)) {
				return victim.getName() + "'s " + victim.getAbility().getName() + " prevents it from being encored!";
			}
			
			return super.getFailMessage(b, user, victim);
		}

		public boolean usable(ActivePokemon p, Move m) {
			return move.getAttack().namesies() == m.getAttack().namesies();
		}

		public String getUnusableMessage(ActivePokemon p) {
			return "Only " + move.getAttack().getName() + " can be used right now!";
		}

		public Move getForcedMove() {
			return move;
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			if (p.getAttack().namesies() != move.getAttack().namesies()) {
				b.printAttacking(p);
				Messages.add(new MessageUpdate(this.getFailMessage(b, p, opp)));
				return false;
			}
			return true;
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (move.getPP() == 0) active = false; // If the move runs out of PP, Encore immediately ends
		}
	}

	static class Disable extends PokemonEffect implements AttackSelectionEffect, BeforeTurnEffect {
		private static final long serialVersionUID = 1L;
		private Move disabled;
		private int turns;

		Disable() {
			super(EffectNamesies.DISABLE, -1, -1, false);
			this.turns = RandomUtils.getRandomInt(4, 7); // Between 4 and 7 turns
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !((victim.hasAbility(AbilityNamesies.AROMA_VEIL) && !caster.breaksTheMold()) || victim.getAttributes().getLastMoveUsed() == null || victim.getAttributes().getLastMoveUsed().getPP() == 0 || victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			disabled = victim.getAttributes().getLastMoveUsed();
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + "'s " + disabled.getAttack().getName() + " was disabled!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return victim.getName() + "'s " + disabled.getAttack().getName() + " is no longer disabled!";
		}

		public boolean shouldSubside(Battle b, ActivePokemon victim) {
			return turns == 0;
		}

		public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (victim.hasEffect(this.namesies)) {
				return victim.getName() + " is already disabled!";
			}
			else if (victim.hasAbility(AbilityNamesies.AROMA_VEIL)) {
				return victim.getName() + "'s " + victim.getAbility().getName() + " prevents it from being disabled!";
			}
			
			return super.getFailMessage(b, user, victim);
		}

		public boolean usable(ActivePokemon p, Move m) {
			return disabled.getAttack().namesies() != m.getAttack().namesies();
		}

		public String getUnusableMessage(ActivePokemon p) {
			return disabled.getAttack().getName() + " is disabled!";
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			turns--;
			if (p.getAttack().namesies() == disabled.getAttack().namesies()) {
				b.printAttacking(p);
				Messages.add(new MessageUpdate(p.getAttack().getName() + " is disabled!"));
				return false;
			}
			return true;
		}
	}

	static class RaiseCrits extends PokemonEffect implements CritStageEffect, PassableEffect {
		private static final long serialVersionUID = 1L;
		private boolean focusEnergy;
		private boolean direHit;
		private boolean berrylicious;

		RaiseCrits() {
			super(EffectNamesies.RAISE_CRITS, -1, -1, false);
			this.focusEnergy = false;
			this.direHit = false;
			this.berrylicious = false;
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(source == CastSource.USE_ITEM && victim.hasEffect(this.namesies) && ((RaiseCrits)victim.getEffect(this.namesies)).direHit);
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!victim.hasEffect(this.namesies)) {
				super.cast(b, caster, victim, source, printCast);
			}
			// Doesn't 'fail' if they already have the effect -- just display the message again
			else if (printCast) {
				Messages.add(new MessageUpdate(getCastMessage(b, caster, victim)));
			}
			
			RaiseCrits critsies = (RaiseCrits)victim.getEffect(this.namesies);
			switch (source) {
				case ATTACK:
					critsies.focusEnergy = true;
					break;
				case USE_ITEM:
					critsies.direHit = true;
					break;
				case HELD_ITEM:
					critsies.berrylicious = true;
					break;
				default:
					Global.error("Unknown source for RaiseCrits effect.");
				}
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " is getting pumped!";
		}

		public int increaseCritStage(int stage, ActivePokemon p) {
			int critStage = 0;
			
			// TODO: Should probably make an enum or something because this is stupid
			if (focusEnergy) {
				critStage++;
			}
			
			if (direHit) {
				critStage++;
			}
			
			if (berrylicious) {
				critStage++;
			}
			
			if (critStage == 0) {
				Global.error("RaiseCrits effect is not actually raising crits.");
			}
			
			return critStage + stage;
		}
	}

	static class ChangeItem extends PokemonEffect implements ItemHolder {
		private static final long serialVersionUID = 1L;
		private Item item;

		ChangeItem() {
			super(EffectNamesies.CHANGE_ITEM, -1, -1, false);
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			item = ((ItemHolder)source.getSource(b, caster)).getItem();
			while (victim.getAttributes().removeEffect(this.namesies));
			super.cast(b, caster, victim, source, printCast);
		}

		public Item getItem() {
			return item;
		}
	}

	static class ChangeType extends PokemonEffect implements ChangeTypeEffect {
		private static final long serialVersionUID = 1L;
		private Type[] type;
		private ChangeTypeMove typeSource;
		private CastSource castSource;
		
		private String castMessage(ActivePokemon victim) {
			String changeType = type[0].getName() + (type[1] == Type.NO_TYPE ? "" : "/" + type[1].getName());
			
			switch (castSource) {
				case ATTACK:
					return victim.getName() + " was changed to " + changeType + " type!!";
				case ABILITY:
					return victim.getName() + "'s " + ((Ability)typeSource).getName() + " changed it to the " + changeType + " type!!";
				case HELD_ITEM:
					return victim.getName() + "'s " + ((Item)typeSource).getName() + " changed it to the " + changeType + " type!!";
				
				default:
					Global.error("Invalid cast source for ChangeType " + castSource);
					return null;
			}
		}

		ChangeType() {
			super(EffectNamesies.CHANGE_TYPE, -1, -1, false);
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			castSource = source;
			typeSource = (ChangeTypeMove)source.getSource(b, caster);
			type = typeSource.getType(b, caster, victim);
			
			// Remove any other ChangeType effects that the victim may have
			while (victim.getAttributes().removeEffect(this.namesies));
			
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return castMessage(victim);
		}

		public void subside(Battle b, ActivePokemon p) {
			Messages.add(new MessageUpdate().updatePokemon(b, p));
		}

		public Type[] getType(Battle b, ActivePokemon p, boolean display) {
			return type;
		}
	}

	static class ChangeAbility extends PokemonEffect implements AbilityHolder {
		private static final long serialVersionUID = 1L;
		private Ability ability;
		private String message;

		ChangeAbility() {
			super(EffectNamesies.CHANGE_ABILITY, -1, -1, false);
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			Ability oldAbility = victim.getAbility();
			oldAbility.deactivate(b, victim);
			
			ChangeAbilityMove changey = (ChangeAbilityMove)source.getSource(b, caster);
			ability = changey.getAbility(b, caster, victim);
			message = changey.getMessage(b, caster, victim);
			
			// Remove any other ChangeAbility effects that the victim may have
			while (victim.getAttributes().removeEffect(this.namesies));
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return message;
		}

		public Ability getAbility() {
			return ability;
		}
	}

	static class Stockpile extends PokemonEffect implements StageChangingEffect {
		private static final long serialVersionUID = 1L;
		private int turns;

		Stockpile() {
			super(EffectNamesies.STOCKPILE, -1, -1, false);
			this.turns = 0;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!victim.hasEffect(this.namesies)) {
				super.cast(b, caster, victim, source, printCast);
			}
			
			Stockpile stockpile = (Stockpile)victim.getEffect(this.namesies);
			if (stockpile.turns < 3) {
				Messages.add(new MessageUpdate(victim.getName() + " Defense and Special Defense were raised!"));
				stockpile.turns++;
				return;
			}
			
			Messages.add(new MessageUpdate(this.getFailMessage(b, caster, victim)));
		}

		public void subside(Battle b, ActivePokemon p) {
			Messages.add(new MessageUpdate("The effects of " + p.getName() + "'s Stockpile ended!"));
			Messages.add(new MessageUpdate(p.getName() + "'s Defense and Special Defense decreased!"));
		}

		public int getTurns() {
			return turns;
		}

		public int adjustStage(Battle b,  ActivePokemon p, ActivePokemon opp, Stat s, int stage) {
			return s == Stat.DEFENSE || s == Stat.SP_DEFENSE ? stage + turns : stage;
		}
	}

	static class UsedDefenseCurl extends PokemonEffect {
		private static final long serialVersionUID = 1L;

		UsedDefenseCurl() {
			super(EffectNamesies.USED_DEFENSE_CURL, -1, -1, false);
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!victim.hasEffect(this.namesies)) {
				super.cast(b, caster, victim, source, printCast);
			}
			else {
				Messages.add(new MessageUpdate(getCastMessage(b, caster, victim)));
			}
		}
	}

	static class UsedMinimize extends PokemonEffect {
		private static final long serialVersionUID = 1L;

		UsedMinimize() {
			super(EffectNamesies.USED_MINIMIZE, -1, -1, false);
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!victim.hasEffect(this.namesies)) {
				super.cast(b, caster, victim, source, printCast);
			}
			else {
				Messages.add(new MessageUpdate(getCastMessage(b, caster, victim)));
			}
		}
	}

	static class Mimic extends PokemonEffect implements ChangeMoveListEffect {
		private static final long serialVersionUID = 1L;
		private Move mimicMove;

		Mimic() {
			super(EffectNamesies.MIMIC, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			ActivePokemon other = b.getOtherPokemon(victim.isPlayer());
			final Move lastMoveUsed = other.getAttributes().getLastMoveUsed();
			Attack lastAttack = lastMoveUsed == null ? null : lastMoveUsed.getAttack();
			
			if (lastAttack == null || victim.hasMove(b, lastAttack.namesies()) || lastAttack.isMoveType(MoveType.MIMICLESS)) {
				Messages.add(new MessageUpdate(this.getFailMessage(b, caster, victim)));
				return;
			}
			
			mimicMove = new Move(lastAttack);
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " learned " + mimicMove.getAttack().getName() + "!";
		}

		public List<Move> getMoveList(List<Move> actualMoves) {
			List<Move> list = new ArrayList<>();
			for (Move move : actualMoves) {
				if (move.getAttack().namesies() == AttackNamesies.MIMIC) {
					list.add(mimicMove);
				}
				else {
					list.add(move);
				}
			}
			
			return list;
		}
	}

	static class Imprison extends PokemonEffect implements AttackSelectionEffect, BeforeTurnEffect {
		private static final long serialVersionUID = 1L;
		private List<AttackNamesies> unableMoves;

		Imprison() {
			super(EffectNamesies.IMPRISON, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			unableMoves = new ArrayList<>();
			for (Move m : caster.getMoves(b)) {
				unableMoves.add(m.getAttack().namesies());
			}
			
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " sealed " + victim.getName() + "'s moves!";
		}

		public boolean usable(ActivePokemon p, Move m) {
			return !unableMoves.contains(m.getAttack().namesies());
		}

		public String getUnusableMessage(ActivePokemon p) {
			return "No!! You are imprisoned!!!";
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			if (unableMoves.contains(p.getAttack().namesies())) {
				b.printAttacking(p);
				Messages.add(new MessageUpdate(this.getFailMessage(b, p, opp)));
				return false;
			}
			
			return true;
		}
	}

	static class Trapped extends PokemonEffect implements TrappingEffect {
		private static final long serialVersionUID = 1L;

		Trapped() {
			super(EffectNamesies.TRAPPED, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " can't escape!";
		}

		public boolean isTrapped(Battle b, ActivePokemon escaper) {
			// Ghost-type Pokemon can always escape
			return !escaper.isType(b, Type.GHOST);
		}

		public String trappingMessage(ActivePokemon trapped) {
			return trapped.getName() + " cannot be recalled at this time!";
		}
	}

	static class Foresight extends PokemonEffect implements AdvantageChanger {
		private static final long serialVersionUID = 1L;

		Foresight() {
			super(EffectNamesies.FORESIGHT, -1, -1, false);
		}

		public Type[] getAdvantageChange(Type attacking, Type[] defending) {
			for (int i = 0; i < 2; i++) {
				if ((attacking == Type.NORMAL || attacking == Type.FIGHTING) && defending[i] == Type.GHOST) {
					defending[i] = Type.NO_TYPE;
				}
			}
			
			return defending;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!victim.hasEffect(this.namesies)) {
				super.cast(b, caster, victim, source, printCast);
			}
			else {
				Messages.add(new MessageUpdate(getCastMessage(b, caster, victim)));
			}
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " identified " + victim.getName() + "!";
		}
	}

	static class MiracleEye extends PokemonEffect implements AdvantageChanger {
		private static final long serialVersionUID = 1L;

		MiracleEye() {
			super(EffectNamesies.MIRACLE_EYE, -1, -1, false);
		}

		public Type[] getAdvantageChange(Type attacking, Type[] defending) {
			for (int i = 0; i < 2; i++) {
				if ((attacking == Type.PSYCHIC || attacking == Type.PSYCHIC) && defending[i] == Type.DARK) {
					defending[i] = Type.NO_TYPE;
				}
			}
			
			return defending;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!victim.hasEffect(this.namesies)) {
				super.cast(b, caster, victim, source, printCast);
			}
			else {
				Messages.add(new MessageUpdate(getCastMessage(b, caster, victim)));
			}
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " identified " + victim.getName() + "!";
		}
	}

	static class Torment extends PokemonEffect implements AttackSelectionEffect, BeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		Torment() {
			super(EffectNamesies.TORMENT, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !((victim.hasAbility(AbilityNamesies.AROMA_VEIL) && !caster.breaksTheMold()) || victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " tormented " + victim.getName() + "!";
		}

		public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (victim.hasAbility(AbilityNamesies.AROMA_VEIL)) {
				return victim.getName() + "'s " + victim.getAbility().getName() + " prevents torment!";
			}
			
			return super.getFailMessage(b, user, victim);
		}

		public boolean usable(ActivePokemon p, Move m) {
			return (p.getAttributes().getLastMoveUsed() == null || p.getAttributes().getLastMoveUsed().getAttack().namesies() != m.getAttack().namesies());
		}

		public String getUnusableMessage(ActivePokemon p) {
			return p.getName() + " cannot use the same move twice in a row!";
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			if (!usable(p, p.getMove())) {
				b.printAttacking(p);
				Messages.add(new MessageUpdate(this.getFailMessage(b, p, opp)));
				return false;
			}
			return true;
		}
	}

	static class Taunt extends PokemonEffect implements AttackSelectionEffect, BeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		Taunt() {
			super(EffectNamesies.TAUNT, 3, 3, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !((victim.hasAbility(AbilityNamesies.AROMA_VEIL) && !caster.breaksTheMold()) || (victim.hasAbility(AbilityNamesies.OBLIVIOUS) && !caster.breaksTheMold()) || victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " fell for the taunt!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The effects of the taunt wore off.";
		}

		public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (victim.hasAbility(AbilityNamesies.OBLIVIOUS) || victim.hasAbility(AbilityNamesies.AROMA_VEIL)) {
				return victim.getName() + "'s " + victim.getAbility().getName() + " prevents it from being taunted!";
			}
			
			return super.getFailMessage(b, user, victim);
		}

		public boolean usable(ActivePokemon p, Move m) {
			return m.getAttack().getCategory() != MoveCategory.STATUS;
		}

		public String getUnusableMessage(ActivePokemon p) {
			return "No!! Not while you're under the effects of taunt!!";
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			if (!usable(p, p.getMove())) {
				b.printAttacking(p);
				Messages.add(new MessageUpdate(this.getFailMessage(b, p, opp)));
				return false;
			}
			return true;
		}
	}

	static class LaserFocus extends PokemonEffect implements AlwaysCritEffect {
		private static final long serialVersionUID = 1L;

		LaserFocus() {
			super(EffectNamesies.LASER_FOCUS, 2, 2, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " began focusing!";
		}
	}

	static class LockOn extends PokemonEffect implements PassableEffect, AccuracyBypassEffect {
		private static final long serialVersionUID = 1L;

		LockOn() {
			super(EffectNamesies.LOCK_ON, 2, 2, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " took aim!";
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
			return true;
		}
	}

	static class Telekinesis extends PokemonEffect implements LevitationEffect, OpponentAccuracyBypassEffect {
		private static final long serialVersionUID = 1L;

		Telekinesis() {
			super(EffectNamesies.TELEKINESIS, 4, 4, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.isGrounded(b) || victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " was levitated due to " + user.getName() + "'s telekinesis!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return victim.getName() + " is no longer under the effects of telekinesis.";
		}

		public void fall(Battle b, ActivePokemon fallen) {
			Messages.add(new MessageUpdate("The effects of telekinesis were cancelled!"));
			
			// TODO: Fix this it's broken
			// Effect.removeEffect(fallen.getEffects(), this.namesies());
		}

		public boolean opponentBypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
			// Opponent can always strike you unless they are using a OHKO move or you are semi-invulnerable
			return !attacking.getAttack().isMoveType(MoveType.ONE_HIT_KO) && !defending.isSemiInvulnerable();
		}
	}

	static class Ingrain extends PokemonEffect implements TrappingEffect, EndTurnEffect, GroundedEffect, PassableEffect, BeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		Ingrain() {
			super(EffectNamesies.INGRAIN, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			removeLevitation(b, victim);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " planted its roots!";
		}

		public boolean isTrapped(Battle b, ActivePokemon escaper) {
			return true;
		}

		public String trappingMessage(ActivePokemon trapped) {
			return trapped.getName() + " cannot be recalled due to ingrain!";
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.fullHealth() || victim.hasEffect(EffectNamesies.HEAL_BLOCK)) {
				return;
			}
			
			int healAmount = victim.healHealthFraction(1/16.0);
			if (victim.isHoldingItem(b, ItemNamesies.BIG_ROOT)) {
				victim.heal((int)(healAmount*.3));
			}
			
			Messages.add(new MessageUpdate(victim.getName() + " restored some HP due to ingrain!").updatePokemon(b, victim));
		}

		private void removeLevitation(Battle b, ActivePokemon p) {
			if (p.isSemiInvulnerableFlying()) {
				p.getMove().switchReady(b, p);
				Messages.add(new MessageUpdate(p.getName() + " fell to the ground!"));
			}
			
			LevitationEffect.falllllllll(b, p);
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			if (p.getAttack().isMoveType(MoveType.AIRBORNE)) {
				b.printAttacking(p);
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return false;
			}
			
			return true;
		}
	}

	static class Grounded extends PokemonEffect implements GroundedEffect, BeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		Grounded() {
			super(EffectNamesies.GROUNDED, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			removeLevitation(b, victim);
		}

		private void removeLevitation(Battle b, ActivePokemon p) {
			if (p.isSemiInvulnerableFlying()) {
				p.getMove().switchReady(b, p);
				Messages.add(new MessageUpdate(p.getName() + " fell to the ground!"));
			}
			
			LevitationEffect.falllllllll(b, p);
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			if (p.getAttack().isMoveType(MoveType.AIRBORNE)) {
				b.printAttacking(p);
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return false;
			}
			
			return true;
		}
	}

	static class Curse extends PokemonEffect implements EndTurnEffect, PassableEffect {
		private static final long serialVersionUID = 1L;

		Curse() {
			super(EffectNamesies.CURSE, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			caster.reduceHealthFraction(b, 1/2.0);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " cut its own HP and put a curse on " + victim.getName() + "!";
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(new MessageUpdate(victim.getName() + " was hurt by the curse!"));
			victim.reduceHealthFraction(b, 1/4.0);
		}
	}

	static class Yawn extends PokemonEffect {
		private static final long serialVersionUID = 1L;

		Yawn() {
			super(EffectNamesies.YAWN, 2, 2, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(!Status.applies(StatusCondition.ASLEEP, b, caster, victim) || victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " grew drowsy!";
		}

		public void subside(Battle b, ActivePokemon p) {
			Status.giveStatus(b, b.getOtherPokemon(p.isPlayer()), p, StatusCondition.ASLEEP);
		}
	}

	static class MagnetRise extends PokemonEffect implements LevitationEffect, PassableEffect {
		private static final long serialVersionUID = 1L;

		MagnetRise() {
			super(EffectNamesies.MAGNET_RISE, 5, 5, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.isGrounded(b) || victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " levitated with electromagnetism!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return victim.getName() + " is no longer under the effects of magnet rise.";
		}

		public void fall(Battle b, ActivePokemon fallen) {
			Messages.add(new MessageUpdate("The effects of " + fallen.getName() + "'s magnet rise were cancelled!"));
			
			// TODO: Fix this it's broken
			// Effect.removeEffect(fallen.getEffects(), this.namesies());
		}
	}

	static class Uproar extends PokemonEffect implements ForceMoveEffect, AttackSelectionEffect, EndTurnEffect {
		private static final long serialVersionUID = 1L;
		private Move uproar;
		
		private static void wakeUp(Battle b, ActivePokemon wakey) {
			if (wakey.hasStatus(StatusCondition.ASLEEP)) {
				wakey.removeStatus();
				Messages.add(new MessageUpdate("The uproar woke up " + wakey.getName() + "!").updatePokemon(b, wakey));
			}
		}

		Uproar() {
			super(EffectNamesies.UPROAR, 3, 3, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			uproar = victim.getMove();
			super.cast(b, caster, victim, source, printCast);
			
			wakeUp(b, victim);
			wakeUp(b, b.getOtherPokemon(victim.isPlayer()));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " started an uproar!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The uproar ended.";
		}

		public Move getForcedMove() {
			return uproar;
		}

		public boolean usable(ActivePokemon p, Move m) {
			return m.getAttack().namesies() == AttackNamesies.UPROAR;
		}

		public String getUnusableMessage(ActivePokemon p) {
			return "Only Uproar can be used right now!";
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			// If uproar runs out of PP, the effect immediately ends
			if (uproar.getPP() == 0) {
				active = false;
			}
		}
	}

	static class AquaRing extends PokemonEffect implements PassableEffect, EndTurnEffect {
		private static final long serialVersionUID = 1L;

		AquaRing() {
			super(EffectNamesies.AQUA_RING, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " surrounded itself with a veil of water!";
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.fullHealth() || victim.hasEffect(EffectNamesies.HEAL_BLOCK)) return;
			int healAmount = victim.healHealthFraction(1/16.0);
			if (victim.isHoldingItem(b, ItemNamesies.BIG_ROOT)) {
				victim.heal((int)(healAmount*.3));
			}
			
			Messages.add(new MessageUpdate(victim.getName() + " restored some HP due to aqua ring!").updatePokemon(b, victim));
		}
	}

	static class Nightmare extends PokemonEffect implements EndTurnEffect {
		private static final long serialVersionUID = 1L;

		Nightmare() {
			super(EffectNamesies.NIGHTMARE, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(!victim.hasStatus(StatusCondition.ASLEEP) || victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " began having a nightmare!";
		}

		public boolean shouldSubside(Battle b, ActivePokemon victim) {
			return !victim.hasStatus(StatusCondition.ASLEEP);
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(new MessageUpdate(victim.getName() + " was hurt by its nightmare!"));
			victim.reduceHealthFraction(b, 1/4.0);
		}
	}

	static class Charge extends PokemonEffect implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		Charge() {
			super(EffectNamesies.CHARGE, 2, 2, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getAttackType() == Type.ELECTRIC ? 2 : 1;
		}
	}

	static class Focusing extends PokemonEffect implements DamageTakenEffect {
		private static final long serialVersionUID = 1L;

		Focusing() {
			super(EffectNamesies.FOCUSING, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " began tightening its focus!";
		}

		public void damageTaken(Battle b, ActivePokemon damageTaker) {
			Messages.add(new MessageUpdate(damageTaker.getName() + " lost its focus and couldn't move!"));
			damageTaker.getAttributes().removeEffect(this.namesies);
			damageTaker.addEffect((PokemonEffect)EffectNamesies.FLINCH.getEffect());
		}
	}

	static class FiddyPercentStronger extends PokemonEffect implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		FiddyPercentStronger() {
			super(EffectNamesies.FIDDY_PERCENT_STRONGER, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return 1.5;
		}
	}

	static class Transformed extends PokemonEffect implements ChangeMoveListEffect, DifferentStatEffect, ChangeTypeEffect {
		private static final long serialVersionUID = 1L;
		private Move[] moveList; // TODO: Check if I can change this to a list -- not sure about the activate method in particular
		private int[] stats;
		private Type[] type;

		Transformed() {
			super(EffectNamesies.TRANSFORMED, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(b.getOtherPokemon(victim.isPlayer()).hasEffect(this.namesies) || ((caster.hasAbility(AbilityNamesies.ILLUSION) && caster.getAbility().isActive())) || victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			// Pokemon to transform into
			ActivePokemon transformee = b.getOtherPokemon(victim.isPlayer());
			
			// Set the new stats
			stats = new int[Stat.NUM_STATS];
			for (int i = 0; i < stats.length; i++) {
				stats[i] = Stat.getStat(i, victim.getLevel(), transformee.getPokemonInfo().getStat(i), victim.getIV(i), victim.getEV(i), victim.getNature().getNatureVal(i));
			}
			stats[Stat.HP.index()] = victim.getMaxHP();
			
			// Copy the move list
			List<Move> transformeeMoves = transformee.getMoves(b);
			moveList = new Move[transformeeMoves.size()];
			for (int i = 0; i < transformeeMoves.size(); i++) {
				moveList[i] = new Move(transformeeMoves.get(i).getAttack(), 5);
			}
			
			// Copy all stages
			for (int i = 0; i < Stat.NUM_BATTLE_STATS; i++) {
				victim.getAttributes().setStage(i, transformee.getStage(i));
			}
			
			// Copy the type
			type = transformee.getPokemonInfo().getType();
			
			// Castaway
			super.cast(b, caster, victim, source, printCast);
			Messages.add(new MessageUpdate().withNewPokemon(transformee.getPokemonInfo(), transformee.isShiny(), true, victim.isPlayer()));
			Messages.add(new MessageUpdate().updatePokemon(b, victim));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " transformed into " + b.getOtherPokemon(victim.isPlayer()).getPokemonInfo().getName() + "!";
		}

		public List<Move> getMoveList(List<Move> actualMoves) {
			return Arrays.asList(moveList);
		}

		public Integer getStat(ActivePokemon user, Stat stat) {
			return stats[stat.index()];
		}

		public Type[] getType(Battle b, ActivePokemon p, boolean display) {
			return type;
		}
	}

	static class Substitute extends PokemonEffect implements AbsorbDamageEffect, PassableEffect, EffectBlockerEffect {
		private static final long serialVersionUID = 1L;
		private int hp;

		Substitute() {
			super(EffectNamesies.SUBSTITUTE, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.getHPRatio() <= .25 || victim.getMaxHP() <= 3 || victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			hp = victim.reduceHealthFraction(b, .25) + 1;
			super.cast(b, caster, victim, source, printCast);
			Messages.add(new MessageUpdate().updatePokemon(b, victim));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " put in a substitute!";
		}

		public boolean absorbDamage(ActivePokemon damageTaker, int damageAmount) {
			this.hp -= damageAmount;
			if (this.hp <= 0) {
				Messages.add(new MessageUpdate("The substitute broke!"));
				damageTaker.getAttributes().removeEffect(this.namesies());
			}
			else {
				Messages.add(new MessageUpdate("The substitute absorbed the hit!"));
			}
			
			// Substitute always blocks damage
			return true;
		}

		public boolean validMove(Battle b, ActivePokemon user, ActivePokemon victim) {
			// Self-target and field moves are always successful
			if (user.getAttack().isSelfTarget() || user.getAttack().isMoveType(MoveType.FIELD)) {
				return true;
			}
			
			// Substitute-piercing moves, Sound-based moves, and Pokemon with the Infiltrator ability bypass Substitute
			if (user.getAttack().isMoveType(MoveType.SUBSTITUTE_PIERCING) || user.getAttack().isMoveType(MoveType.SOUND_BASED) || user.hasAbility(AbilityNamesies.INFILTRATOR)) {
				return true;
			}
			
			// Print the failure for status moves
			if (user.getAttack().getCategory() == MoveCategory.STATUS) {
				Messages.add(new MessageUpdate(this.getFailMessage(b, user, victim)));
			}
			
			return false;
		}
	}

	static class Mist extends PokemonEffect implements StatProtectingEffect, DefogRelease {
		private static final long serialVersionUID = 1L;

		Mist() {
			super(EffectNamesies.MIST, 5, 5, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " shrouded itself in mist!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The mist faded.";
		}

		public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
			return !caster.hasAbility(AbilityNamesies.INFILTRATOR);
		}

		public String preventionMessage(ActivePokemon p, Stat s) {
			return "The mist prevents stat reductions!";
		}

		public void releaseDefog(Battle b, ActivePokemon victim) {
			Messages.add(new MessageUpdate("The mist faded."));
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both lists
			victim.getEffects().remove(this);
			b.getEffects(victim.isPlayer()).remove(this);
		}
	}

	static class MagicCoat extends PokemonEffect implements TargetSwapperEffect {
		private static final long serialVersionUID = 1L;

		MagicCoat() {
			super(EffectNamesies.MAGIC_COAT, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " shrouded itself with a magic coat!";
		}

		public boolean swapTarget(Battle b, ActivePokemon user, ActivePokemon opponent) {
			Attack attack = user.getAttack();
			if (!attack.isSelfTarget() && attack.getCategory() == MoveCategory.STATUS && !attack.isMoveType(MoveType.NO_MAGIC_COAT)) {
				Messages.add(new MessageUpdate(opponent.getName() + "'s " + "Magic Coat" + " reflected " + user.getName() + "'s move!"));
				return true;
			}
			
			return false;
		}
	}

	static class Bide extends PokemonEffect implements ForceMoveEffect, EndTurnEffect {
		private static final long serialVersionUID = 1L;
		private Move move;
		private int turns;
		private int damage;

		Bide() {
			super(EffectNamesies.BIDE, -1, -1, false);
			this.turns = 1;
			this.damage = 0;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			Bide bidesies = (Bide)victim.getEffect(this.namesies);
			
			// If the victim is not already under the effects of Bide, cast it upon them
			if (bidesies == null) {
				move = caster.getMove();
				super.cast(b, caster, victim, source, printCast);
				return;
			}
			
			// Already has the effect, but not ready for it to end yet -- store dat energy
			if (bidesies.turns > 0) {
				bidesies.turns--;
				Messages.add(new MessageUpdate(getCastMessage(b, caster, victim)));
				return;
			}
			
			// TIME'S UP -- RELEASE DAT STORED ENERGY
			Messages.add(new MessageUpdate(victim.getName() + " released energy!"));
			if (bidesies.damage == 0) {
				// Sucks to suck
				Messages.add(new MessageUpdate(this.getFailMessage(b, caster, victim)));
			}
			else {
				// RETALIATION STATION
				b.getOtherPokemon(victim.isPlayer()).reduceHealth(b, 2*bidesies.damage);
			}
			
			// Bye Bye Bidesies
			victim.getAttributes().removeEffect(this.namesies);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " is storing energy!";
		}

		public int getTurns() {
			return turns;
		}

		public Move getForcedMove() {
			return move;
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			damage += victim.getAttributes().getDamageTaken();
		}
	}

	static class HalfWeight extends PokemonEffect implements HalfWeightEffect {
		private static final long serialVersionUID = 1L;
		private int layers;

		HalfWeight() {
			super(EffectNamesies.HALF_WEIGHT, -1, -1, false);
			this.layers = 1;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			HalfWeight halfWeight = (HalfWeight)victim.getEffect(this.namesies);
			if (halfWeight == null) super.cast(b, caster, victim, source, printCast);
			else halfWeight.layers++;
		}

		public int getHalfAmount(int halfAmount) {
			return halfAmount + layers;
		}
	}

	static class PowerTrick extends PokemonEffect implements PassableEffect, StatSwitchingEffect {
		private static final long serialVersionUID = 1L;

		PowerTrick() {
			super(EffectNamesies.POWER_TRICK, -1, -1, false);
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			PokemonEffect thaPowah = victim.getEffect(this.namesies);
			if (thaPowah == null) {
				super.cast(b, caster, victim, source, printCast);
				return;
			}
			
			Messages.add(new MessageUpdate(getCastMessage(b, caster, victim)));
			victim.getAttributes().removeEffect(this.namesies);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + "'s attack and defense were swapped!";
		}

		public Stat switchStat(Stat s) {
			if (s == Stat.ATTACK) return Stat.DEFENSE;
			if (s == Stat.DEFENSE) return Stat.ATTACK;
			return s;
		}
	}

	static class PowerSplit extends PokemonEffect implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		PowerSplit() {
			super(EffectNamesies.POWER_SPLIT, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " split the power!";
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			
			// If the stat is a splitting stat, return the average between the user and the opponent
			if (s == Stat.ATTACK || s == Stat.SP_ATTACK) {
				return (p.getStat(b, s) + opp.getStat(b, s))/2;
			}
			
			return stat;
		}
	}

	static class GuardSplit extends PokemonEffect implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		GuardSplit() {
			super(EffectNamesies.GUARD_SPLIT, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " split the defense!";
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			
			// If the stat is a splitting stat, return the average between the user and the opponent
			if (s == Stat.DEFENSE || s == Stat.SP_DEFENSE) {
				return (p.getStat(b, s) + opp.getStat(b, s))/2;
			}
			
			return stat;
		}
	}

	static class HealBlock extends PokemonEffect implements BeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		HealBlock() {
			super(EffectNamesies.HEAL_BLOCK, 5, 5, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !((victim.hasAbility(AbilityNamesies.AROMA_VEIL) && !caster.breaksTheMold()) || victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " blocked " + victim.getName() + " from healing!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The effects of heal block wore off.";
		}

		public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (victim.hasAbility(AbilityNamesies.AROMA_VEIL)) {
				return victim.getName() + "'s " + victim.getAbility().getName() + " prevents heal block!";
			}
			
			return super.getFailMessage(b, user, victim);
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			if (p.getAttack() instanceof SapHealthEffect) {
				b.printAttacking(p);
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return false;
			}
			
			return true;
		}
	}

	static class Infatuated extends PokemonEffect implements BeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		Infatuated() {
			super(EffectNamesies.INFATUATED, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !((victim.hasAbility(AbilityNamesies.OBLIVIOUS) && !caster.breaksTheMold()) || (victim.hasAbility(AbilityNamesies.AROMA_VEIL) && !caster.breaksTheMold()) || !Gender.oppositeGenders(caster, victim) || victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (victim.isHoldingItem(b, ItemNamesies.DESTINY_KNOT) && this.applies(b, victim, caster, CastSource.HELD_ITEM)) {
				super.cast(b, victim, caster, CastSource.HELD_ITEM, false);
				Messages.add(new MessageUpdate(victim.getName() + "'s " + ItemNamesies.DESTINY_KNOT.getName() + " caused " + caster.getName() + " to fall in love!"));
			}
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " fell in love!";
		}

		public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (Gender.oppositeGenders(user, victim) && (victim.hasAbility(AbilityNamesies.OBLIVIOUS) || victim.hasAbility(AbilityNamesies.AROMA_VEIL))) {
				return victim.getName() + "'s " + victim.getAbility().getName() + " prevents infatuation!";
			}
			
			return super.getFailMessage(b, user, victim);
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			Messages.add(new MessageUpdate(p.getName() + " is in love with " + opp.getName() + "!"));
			if (RandomUtils.chanceTest(50)) {
				return true;
			}
			
			Messages.add(new MessageUpdate(p.getName() + "'s infatuation kept it from attacking!"));
			return false;
		}
	}

	static class Snatch extends PokemonEffect implements TargetSwapperEffect {
		private static final long serialVersionUID = 1L;

		Snatch() {
			super(EffectNamesies.SNATCH, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public boolean swapTarget(Battle b, ActivePokemon user, ActivePokemon opponent) {
			Attack attack = user.getAttack();
			if (attack.isSelfTarget() && attack.getCategory() == MoveCategory.STATUS && !attack.isMoveType(MoveType.NON_SNATCHABLE)) {
				Messages.add(new MessageUpdate(opponent.getName() + " snatched " + user.getName() + "'s move!"));
				return true;
			}
			
			return false;
		}
	}

	static class Grudge extends PokemonEffect implements FaintEffect {
		private static final long serialVersionUID = 1L;

		Grudge() {
			super(EffectNamesies.GRUDGE, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " wants " + b.getOtherPokemon(victim.isPlayer()).getName() + " to bear a grudge!";
		}

		public void deathWish(Battle b, ActivePokemon dead, ActivePokemon murderer) {
			if (murderer.getAttributes().isAttacking()) {
				Messages.add(new MessageUpdate(murderer.getName() + "'s " + murderer.getAttack().getName() + " lost all its PP due to its grudge!"));
				murderer.getMove().reducePP(murderer.getMove().getPP());
			}
		}
	}

	static class DestinyBond extends PokemonEffect implements FaintEffect, BeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		DestinyBond() {
			super(EffectNamesies.DESTINY_BOND, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " is trying to take " + b.getOtherPokemon(victim.isPlayer()).getName() + " down with it!";
		}

		public void deathWish(Battle b, ActivePokemon dead, ActivePokemon murderer) {
			if (murderer.getAttributes().isAttacking()) {
				Messages.add(new MessageUpdate(dead.getName() + " took " + murderer.getName() + " down with it!"));
				murderer.killKillKillMurderMurderMurder(b);
			}
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			super.active = false;
			return true;
		}
	}

	static class PerishSong extends PokemonEffect implements PassableEffect, EndTurnEffect {
		private static final long serialVersionUID = 1L;

		PerishSong() {
			super(EffectNamesies.PERISH_SONG, 3, 3, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !((victim.hasAbility(AbilityNamesies.SOUNDPROOF) && !caster.breaksTheMold()) || victim.hasEffect(this.namesies));
		}

		public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (victim.hasAbility(AbilityNamesies.SOUNDPROOF)) {
				return victim.getName() + "'s " + victim.getAbility().getName() + " makes it immune to sound based moves!";
			}
			
			return super.getFailMessage(b, user, victim);
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			Messages.add(new MessageUpdate(victim.getName() + "'s Perish Song count fell to " + (super.numTurns - 1) + "!"));
			if (super.numTurns == 1) {
				victim.killKillKillMurderMurderMurder(b);
			}
		}
	}

	static class Embargo extends PokemonEffect implements PassableEffect {
		private static final long serialVersionUID = 1L;

		Embargo() {
			super(EffectNamesies.EMBARGO, 5, 5, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " can't use items now!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return victim.getName() + " can use items again!";
		}
	}

	static class ConsumedItem extends PokemonEffect implements ItemHolder {
		private static final long serialVersionUID = 1L;
		private Item consumed;

		ConsumedItem() {
			super(EffectNamesies.CONSUMED_ITEM, -1, -1, false);
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			consumed = victim.getHeldItem(b);
			victim.removeItem();
			while (victim.getAttributes().removeEffect(this.namesies));
			super.cast(b, caster, victim, source, printCast);
		}

		public Item getItem() {
			return consumed;
		}
	}

	static class FairyLock extends PokemonEffect implements OpponentTrappingEffect {
		private static final long serialVersionUID = 1L;

		FairyLock() {
			super(EffectNamesies.FAIRY_LOCK, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public boolean trapOpponent(Battle b, ActivePokemon escaper, ActivePokemon trapper) {
			return true;
		}

		public String opponentTrappingMessage(ActivePokemon escaper, ActivePokemon trapper) {
			return trapper.getName() + "'s " + this.getName() + " prevents " + escaper.getName() + " from escaping!";
		}
	}

	static class Powder extends PokemonEffect implements BeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		Powder() {
			super(EffectNamesies.POWDER, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " sprinkled powder on " + victim.getName() + "!";
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			// Fire-type moves makes the user explode
			if (p.getAttackType() == Type.FIRE) {
				Messages.add(new MessageUpdate("The powder exploded!"));
				p.reduceHealthFraction(b, 1/4.0);
				return false;
			}
			
			return true;
		}
	}

	static class Electrified extends PokemonEffect implements ChangeAttackTypeEffect {
		private static final long serialVersionUID = 1L;

		Electrified() {
			super(EffectNamesies.ELECTRIFIED, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " electrified " + victim.getName() + "!";
		}

		public Type changeAttackType(Type original) {
			return Type.ELECTRIC;
		}
	}

	static class EatenBerry extends PokemonEffect {
		private static final long serialVersionUID = 1L;

		EatenBerry() {
			super(EffectNamesies.EATEN_BERRY, -1, -1, false);
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!victim.hasEffect(this.namesies)) {
				super.cast(b, caster, victim, source, printCast);
			}
			else {
				Messages.add(new MessageUpdate(getCastMessage(b, caster, victim)));
			}
		}
	}
}
