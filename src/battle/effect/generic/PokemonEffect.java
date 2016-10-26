package battle.effect.generic;

import battle.Attack;
import battle.Battle;
import battle.Move;
import battle.MoveCategory;
import battle.MoveType;
import battle.effect.AccuracyBypassEffect;
import battle.effect.AdvantageChanger;
import battle.effect.AttackSelectionEffect;
import battle.effect.BeforeTurnEffect;
import battle.effect.BracingEffect;
import battle.effect.ChangeAttackTypeEffect;
import battle.effect.CritStageEffect;
import battle.effect.DefogRelease;
import battle.effect.EffectBlockerEffect;
import battle.effect.EndTurnEffect;
import battle.effect.FaintEffect;
import battle.effect.ForceMoveEffect;
import battle.effect.GroundedEffect;
import battle.effect.HalfWeightEffect;
import battle.effect.LevitationEffect;
import battle.effect.OpponentAccuracyBypassEffect;
import battle.effect.OpponentBeforeTurnEffect;
import battle.effect.OpponentTrappingEffect;
import battle.effect.PassableEffect;
import battle.effect.PowerChangeEffect;
import battle.effect.RapidSpinRelease;
import battle.effect.StageChangingEffect;
import battle.effect.StatChangingEffect;
import battle.effect.StatProtectingEffect;
import battle.effect.StatSwitchingEffect;
import battle.effect.StatusPreventionEffect;
import battle.effect.TargetSwapperEffect;
import battle.effect.TrappingEffect;
import battle.effect.attack.ChangeAbilityMove;
import battle.effect.attack.ChangeTypeMove;
import battle.effect.attack.CrashDamageMove;
import battle.effect.holder.AbilityHolder;
import battle.effect.holder.IntegerHolder;
import battle.effect.holder.ItemHolder;
import battle.effect.holder.MoveHolder;
import battle.effect.holder.MoveListHolder;
import battle.effect.holder.StatsHolder;
import battle.effect.holder.TypeHolder;
import battle.effect.status.Status;
import battle.effect.status.StatusCondition;
import item.Item;
import main.Global;
import main.Type;
import namesies.AbilityNamesies;
import namesies.AttackNamesies;
import namesies.EffectNamesies;
import namesies.ItemNamesies;
import pokemon.Ability;
import pokemon.ActivePokemon;
import pokemon.Gender;
import pokemon.Stat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Class to handle effects that are on a single Pokemon
public abstract class PokemonEffect extends Effect implements Serializable {
	private static final long serialVersionUID = 1L;
	private static Map<String, PokemonEffect> map;
	
	public PokemonEffect(EffectNamesies name, int minTurns, int maxTurns, boolean nextTurnSubside) {
		super(name, minTurns, maxTurns, nextTurnSubside);
	}
	
	public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
		if (printCast) {
			b.addMessage(getCastMessage(b, caster, victim));
		}

		victim.addEffect(this.newInstance());
		
		b.addMessage("", caster);
		b.addMessage("", victim);
	}
	
	public abstract PokemonEffect newInstance();

	public static PokemonEffect getEffect(EffectNamesies name) {
		String effectName = name.getName();
		if (map == null) {
			loadEffects();
		}
		
		if (!map.containsKey(effectName)) {
			Global.error("No such PokemonEffect " + effectName);
		}

		return map.get(effectName);
	}

	// Create and load the effects map if it doesn't already exist
	public static void loadEffects() {
		if (map != null) {
			return;
		}

		map = new HashMap<>();

		// EVERYTHING BELOW IS GENERATED ###

		// List all of the classes we are loading
		map.put("LeechSeed", new LeechSeed());
		map.put("BadPoison", new BadPoison());
		map.put("Flinch", new Flinch());
		map.put("FireSpin", new FireSpin());
		map.put("Infestation", new Infestation());
		map.put("MagmaStorm", new MagmaStorm());
		map.put("Clamped", new Clamped());
		map.put("Whirlpooled", new Whirlpooled());
		map.put("Wrapped", new Wrapped());
		map.put("Binded", new Binded());
		map.put("SandTomb", new SandTomb());
		map.put("KingsShield", new KingsShield());
		map.put("SpikyShield", new SpikyShield());
		map.put("Protecting", new Protecting());
		map.put("QuickGuard", new QuickGuard());
		map.put("CraftyShield", new CraftyShield());
		map.put("MatBlock", new MatBlock());
		map.put("Bracing", new Bracing());
		map.put("Confusion", new Confusion());
		map.put("SelfConfusion", new SelfConfusion());
		map.put("Safeguard", new Safeguard());
		map.put("GuardSpecial", new GuardSpecial());
		map.put("Encore", new Encore());
		map.put("Disable", new Disable());
		map.put("RaiseCrits", new RaiseCrits());
		map.put("ChangeItem", new ChangeItem());
		map.put("ChangeType", new ChangeType());
		map.put("ChangeAbility", new ChangeAbility());
		map.put("Stockpile", new Stockpile());
		map.put("UsedDefenseCurl", new UsedDefenseCurl());
		map.put("UsedMinimize", new UsedMinimize());
		map.put("Mimic", new Mimic());
		map.put("Imprison", new Imprison());
		map.put("Trapped", new Trapped());
		map.put("Foresight", new Foresight());
		map.put("MiracleEye", new MiracleEye());
		map.put("Torment", new Torment());
		map.put("Taunt", new Taunt());
		map.put("LockOn", new LockOn());
		map.put("Telekinesis", new Telekinesis());
		map.put("Ingrain", new Ingrain());
		map.put("Grounded", new Grounded());
		map.put("Curse", new Curse());
		map.put("Yawn", new Yawn());
		map.put("MagnetRise", new MagnetRise());
		map.put("Uproar", new Uproar());
		map.put("AquaRing", new AquaRing());
		map.put("Nightmare", new Nightmare());
		map.put("Charge", new Charge());
		map.put("Focusing", new Focusing());
		map.put("FiddyPercentStronger", new FiddyPercentStronger());
		map.put("Transformed", new Transformed());
		map.put("Substitute", new Substitute());
		map.put("Mist", new Mist());
		map.put("MagicCoat", new MagicCoat());
		map.put("Bide", new Bide());
		map.put("HalfWeight", new HalfWeight());
		map.put("PowerTrick", new PowerTrick());
		map.put("PowerSplit", new PowerSplit());
		map.put("GuardSplit", new GuardSplit());
		map.put("HealBlock", new HealBlock());
		map.put("Infatuated", new Infatuated());
		map.put("Snatch", new Snatch());
		map.put("Grudge", new Grudge());
		map.put("DestinyBond", new DestinyBond());
		map.put("PerishSong", new PerishSong());
		map.put("Embargo", new Embargo());
		map.put("ConsumedItem", new ConsumedItem());
		map.put("FairyLock", new FairyLock());
		map.put("Powder", new Powder());
		map.put("Electrified", new Electrified());
		map.put("EatenBerry", new EatenBerry());
	}

	/**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

	private static class LeechSeed extends PokemonEffect implements EndTurnEffect, RapidSpinRelease, PassableEffect {
		private static final long serialVersionUID = 1L;

		LeechSeed() {
			super(EffectNamesies.LEECH_SEED, -1, -1, false);
		}

		public LeechSeed newInstance() {
			return (LeechSeed)(new LeechSeed().activate());
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
			
			b.addMessage(victim.getName() + "'s health was sapped!");
			b.getOtherPokemon(victim.user()).sapHealth(victim, victim.reduceHealthFraction(b, 1/8.0), b, false, false);
		}

		public void releaseRapidSpin(Battle b, ActivePokemon user) {
			b.addMessage(user.getName() + " was released from leech seed!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			user.getEffects().remove(this);
			b.getEffects(user.user()).remove(this);
		}
	}

	private static class BadPoison extends PokemonEffect implements EndTurnEffect {
		private static final long serialVersionUID = 1L;
		private int turns;

		BadPoison() {
			super(EffectNamesies.BAD_POISON, -1, -1, false);
		}

		public BadPoison newInstance() {
			return (BadPoison)(new BadPoison().activate());
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

	private static class Flinch extends PokemonEffect implements BeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		Flinch() {
			super(EffectNamesies.FLINCH, 1, 1, false);
		}

		public Flinch newInstance() {
			return (Flinch)(new Flinch().activate());
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

	private static class FireSpin extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
		private static final long serialVersionUID = 1L;

		FireSpin() {
			super(EffectNamesies.FIRE_SPIN, 4, 5, true);
		}

		public FireSpin newInstance() {
			return (FireSpin)(new FireSpin().activate());
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
			
			b.addMessage(victim.getName() + " is hurt by fire spin!");
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim.user()).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0);
		}

		public boolean isTrapped(Battle b, ActivePokemon p) {
			// Ghost-type Pokemon can always escape
			return !p.isType(b, Type.GHOST);
		}

		public String trappingMessage(ActivePokemon trapped) {
			return trapped.getName() + " cannot be recalled due to fire spin!";
		}

		public void releaseRapidSpin(Battle b, ActivePokemon user) {
			b.addMessage(user.getName() + " was released from fire spin!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			user.getEffects().remove(this);
			b.getEffects(user.user()).remove(this);
		}
	}

	private static class Infestation extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
		private static final long serialVersionUID = 1L;

		Infestation() {
			super(EffectNamesies.INFESTATION, 4, 5, true);
		}

		public Infestation newInstance() {
			return (Infestation)(new Infestation().activate());
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
			
			b.addMessage(victim.getName() + " is hurt by infestation!");
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim.user()).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0);
		}

		public boolean isTrapped(Battle b, ActivePokemon p) {
			// Ghost-type Pokemon can always escape
			return !p.isType(b, Type.GHOST);
		}

		public String trappingMessage(ActivePokemon trapped) {
			return trapped.getName() + " cannot be recalled due to infestation!";
		}

		public void releaseRapidSpin(Battle b, ActivePokemon user) {
			b.addMessage(user.getName() + " was released from infestation!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			user.getEffects().remove(this);
			b.getEffects(user.user()).remove(this);
		}
	}

	private static class MagmaStorm extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
		private static final long serialVersionUID = 1L;

		MagmaStorm() {
			super(EffectNamesies.MAGMA_STORM, 4, 5, true);
		}

		public MagmaStorm newInstance() {
			return (MagmaStorm)(new MagmaStorm().activate());
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
			
			b.addMessage(victim.getName() + " is hurt by magma storm!");
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim.user()).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0);
		}

		public boolean isTrapped(Battle b, ActivePokemon p) {
			// Ghost-type Pokemon can always escape
			return !p.isType(b, Type.GHOST);
		}

		public String trappingMessage(ActivePokemon trapped) {
			return trapped.getName() + " cannot be recalled due to magma storm!";
		}

		public void releaseRapidSpin(Battle b, ActivePokemon user) {
			b.addMessage(user.getName() + " was released from magma storm!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			user.getEffects().remove(this);
			b.getEffects(user.user()).remove(this);
		}
	}

	private static class Clamped extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
		private static final long serialVersionUID = 1L;

		Clamped() {
			super(EffectNamesies.CLAMPED, 4, 5, true);
		}

		public Clamped newInstance() {
			return (Clamped)(new Clamped().activate());
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
			
			b.addMessage(victim.getName() + " is hurt by clamp!");
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim.user()).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0);
		}

		public boolean isTrapped(Battle b, ActivePokemon p) {
			// Ghost-type Pokemon can always escape
			return !p.isType(b, Type.GHOST);
		}

		public String trappingMessage(ActivePokemon trapped) {
			return trapped.getName() + " cannot be recalled due to clamp!";
		}

		public void releaseRapidSpin(Battle b, ActivePokemon user) {
			b.addMessage(user.getName() + " was released from clamp!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			user.getEffects().remove(this);
			b.getEffects(user.user()).remove(this);
		}
	}

	private static class Whirlpooled extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
		private static final long serialVersionUID = 1L;

		Whirlpooled() {
			super(EffectNamesies.WHIRLPOOLED, 4, 5, true);
		}

		public Whirlpooled newInstance() {
			return (Whirlpooled)(new Whirlpooled().activate());
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
			
			b.addMessage(victim.getName() + " is hurt by whirlpool!");
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim.user()).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0);
		}

		public boolean isTrapped(Battle b, ActivePokemon p) {
			// Ghost-type Pokemon can always escape
			return !p.isType(b, Type.GHOST);
		}

		public String trappingMessage(ActivePokemon trapped) {
			return trapped.getName() + " cannot be recalled due to whirlpool!";
		}

		public void releaseRapidSpin(Battle b, ActivePokemon user) {
			b.addMessage(user.getName() + " was released from whirlpool!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			user.getEffects().remove(this);
			b.getEffects(user.user()).remove(this);
		}
	}

	private static class Wrapped extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
		private static final long serialVersionUID = 1L;

		Wrapped() {
			super(EffectNamesies.WRAPPED, 4, 5, true);
		}

		public Wrapped newInstance() {
			return (Wrapped)(new Wrapped().activate());
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
			
			b.addMessage(victim.getName() + " is hurt by wrap!");
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim.user()).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0);
		}

		public boolean isTrapped(Battle b, ActivePokemon p) {
			// Ghost-type Pokemon can always escape
			return !p.isType(b, Type.GHOST);
		}

		public String trappingMessage(ActivePokemon trapped) {
			return trapped.getName() + " cannot be recalled due to wrap!";
		}

		public void releaseRapidSpin(Battle b, ActivePokemon user) {
			b.addMessage(user.getName() + " was released from wrap!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			user.getEffects().remove(this);
			b.getEffects(user.user()).remove(this);
		}
	}

	private static class Binded extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
		private static final long serialVersionUID = 1L;

		Binded() {
			super(EffectNamesies.BINDED, 4, 5, true);
		}

		public Binded newInstance() {
			return (Binded)(new Binded().activate());
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
			
			b.addMessage(victim.getName() + " is hurt by bind!");
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim.user()).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0);
		}

		public boolean isTrapped(Battle b, ActivePokemon p) {
			// Ghost-type Pokemon can always escape
			return !p.isType(b, Type.GHOST);
		}

		public String trappingMessage(ActivePokemon trapped) {
			return trapped.getName() + " cannot be recalled due to bind!";
		}

		public void releaseRapidSpin(Battle b, ActivePokemon user) {
			b.addMessage(user.getName() + " was released from bind!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			user.getEffects().remove(this);
			b.getEffects(user.user()).remove(this);
		}
	}

	private static class SandTomb extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
		private static final long serialVersionUID = 1L;

		SandTomb() {
			super(EffectNamesies.SAND_TOMB, 4, 5, true);
		}

		public SandTomb newInstance() {
			return (SandTomb)(new SandTomb().activate());
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
			
			b.addMessage(victim.getName() + " is hurt by sand tomb!");
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim.user()).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0);
		}

		public boolean isTrapped(Battle b, ActivePokemon p) {
			// Ghost-type Pokemon can always escape
			return !p.isType(b, Type.GHOST);
		}

		public String trappingMessage(ActivePokemon trapped) {
			return trapped.getName() + " cannot be recalled due to sand tomb!";
		}

		public void releaseRapidSpin(Battle b, ActivePokemon user) {
			b.addMessage(user.getName() + " was released from sand tomb!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			user.getEffects().remove(this);
			b.getEffects(user.user()).remove(this);
		}
	}

	private static class KingsShield extends PokemonEffect implements OpponentBeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		KingsShield() {
			super(EffectNamesies.KINGS_SHIELD, 1, 1, false);
		}

		public KingsShield newInstance() {
			return (KingsShield)(new KingsShield().activate());
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
			if (Math.random() > caster.getAttributes().getSuccessionDecayRate()) {
				b.addMessage(this.getFailMessage(b, caster, victim));
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
			b.addMessage(opp.getName() + " is protecting itself!");
			Battle.invoke(Collections.singletonList(p.getAttack()), CrashDamageMove.class, "crash", b, p);
			
			// Additional Effects
			protectingEffects(p, opp, b);
			
			return false;
		}
	}

	private static class SpikyShield extends PokemonEffect implements OpponentBeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		SpikyShield() {
			super(EffectNamesies.SPIKY_SHIELD, 1, 1, false);
		}

		public SpikyShield newInstance() {
			return (SpikyShield)(new SpikyShield().activate());
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
				b.addMessage(p.getName() + " was hurt by " + opp.getName() + "'s Spiky Shield!");
				p.reduceHealthFraction(b, 1/8.0);
			}
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (Math.random() > caster.getAttributes().getSuccessionDecayRate()) {
				b.addMessage(this.getFailMessage(b, caster, victim));
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
			b.addMessage(opp.getName() + " is protecting itself!");
			Battle.invoke(Collections.singletonList(p.getAttack()), CrashDamageMove.class, "crash", b, p);
			
			// Additional Effects
			protectingEffects(p, opp, b);
			
			return false;
		}
	}

	private static class Protecting extends PokemonEffect implements OpponentBeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		Protecting() {
			super(EffectNamesies.PROTECTING, 1, 1, false);
		}

		public Protecting newInstance() {
			return (Protecting)(new Protecting().activate());
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
			if (Math.random() > caster.getAttributes().getSuccessionDecayRate()) {
				b.addMessage(this.getFailMessage(b, caster, victim));
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
			b.addMessage(opp.getName() + " is protecting itself!");
			Battle.invoke(Collections.singletonList(p.getAttack()), CrashDamageMove.class, "crash", b, p);
			
			// Additional Effects
			protectingEffects(p, opp, b);
			
			return false;
		}
	}

	private static class QuickGuard extends PokemonEffect implements OpponentBeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		QuickGuard() {
			super(EffectNamesies.QUICK_GUARD, 1, 1, false);
		}

		public QuickGuard newInstance() {
			return (QuickGuard)(new QuickGuard().activate());
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
			if (Math.random() > caster.getAttributes().getSuccessionDecayRate()) {
				b.addMessage(this.getFailMessage(b, caster, victim));
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
			b.addMessage(opp.getName() + " is protecting itself!");
			Battle.invoke(Collections.singletonList(p.getAttack()), CrashDamageMove.class, "crash", b, p);
			
			// Additional Effects
			protectingEffects(p, opp, b);
			
			return false;
		}
	}

	private static class CraftyShield extends PokemonEffect implements OpponentBeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		CraftyShield() {
			super(EffectNamesies.CRAFTY_SHIELD, 1, 1, false);
		}

		public CraftyShield newInstance() {
			return (CraftyShield)(new CraftyShield().activate());
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
			if (Math.random() > caster.getAttributes().getSuccessionDecayRate()) {
				b.addMessage(this.getFailMessage(b, caster, victim));
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
			b.addMessage(opp.getName() + " is protecting itself!");
			Battle.invoke(Collections.singletonList(p.getAttack()), CrashDamageMove.class, "crash", b, p);
			
			// Additional Effects
			protectingEffects(p, opp, b);
			
			return false;
		}
	}

	private static class MatBlock extends PokemonEffect implements OpponentBeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		MatBlock() {
			super(EffectNamesies.MAT_BLOCK, 1, 1, false);
		}

		public MatBlock newInstance() {
			return (MatBlock)(new MatBlock().activate());
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
			b.addMessage(opp.getName() + " is protecting itself!");
			Battle.invoke(Collections.singletonList(p.getAttack()), CrashDamageMove.class, "crash", b, p);
			
			// Additional Effects
			protectingEffects(p, opp, b);
			
			return false;
		}
	}

	private static class Bracing extends PokemonEffect implements BracingEffect {
		private static final long serialVersionUID = 1L;

		Bracing() {
			super(EffectNamesies.BRACING, 1, 1, false);
		}

		public Bracing newInstance() {
			return (Bracing)(new Bracing().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (Math.random() > caster.getAttributes().getSuccessionDecayRate()) {
				b.addMessage(this.getFailMessage(b, caster, victim));
				return;
			}
			
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " braced itself!";
		}

		public boolean isBracing(Battle b, ActivePokemon bracer, Boolean fullHealth) {
			return true;
		}

		public String braceMessage(ActivePokemon bracer) {
			return bracer.getName() + " endured the hit!";
		}
	}

	private static class Confusion extends PokemonEffect implements PassableEffect, BeforeTurnEffect {
		private static final long serialVersionUID = 1L;
		private int turns;

		Confusion() {
			super(EffectNamesies.CONFUSION, -1, -1, false);
		}

		public Confusion newInstance() {
			Confusion x = (Confusion)(new Confusion().activate());
			x.turns = (int)(Math.random()*4) + 1; // Between 1 and 4 turns
			return x;
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !((victim.hasAbility(AbilityNamesies.OWN_TEMPO) && !caster.breaksTheMold()) || victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (victim.isHoldingItem(b, ItemNamesies.PERSIM_BERRY)) {
				b.addMessage(victim.getName() + "'s " + ItemNamesies.PERSIM_BERRY.getName() + " snapped it out of confusion!");
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
				b.addMessage(p.getName() + " snapped out of its confusion!");
				super.active = false;
				return true;
			}
			
			turns--;
			b.addMessage(p.getName() + " is confused!");
			
			// 50% chance to hurt yourself in confusion while confused
			if (Math.random() < .5) {
				b.addMessage("It hurt itself in confusion!");
				
				// Perform confusion damage
				Move temp = p.getMove();
				p.setMove(new Move(Attack.getAttack(AttackNamesies.CONFUSION_DAMAGE)));
				p.reduceHealth(b, b.calculateDamage(p, p));
				p.setMove(temp);
				
				return false;
			}
			
			return true;
		}
	}

	private static class SelfConfusion extends PokemonEffect implements ForceMoveEffect {
		private static final long serialVersionUID = 1L;
		private Move move;

		SelfConfusion() {
			super(EffectNamesies.SELF_CONFUSION, 2, 3, false);
		}

		public SelfConfusion newInstance() {
			SelfConfusion x = (SelfConfusion)(new SelfConfusion().activate());
			x.move = move;
			return x;
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
				b.addMessage(p.getName() + " became confused due to fatigue!");
				p.addEffect(c);
			}
		}

		public Move getMove() {
			return move;
		}
	}

	private static class Safeguard extends PokemonEffect implements DefogRelease, StatusPreventionEffect {
		private static final long serialVersionUID = 1L;

		Safeguard() {
			super(EffectNamesies.SAFEGUARD, 5, 5, false);
		}

		public Safeguard newInstance() {
			return (Safeguard)(new Safeguard().activate());
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
			b.addMessage("The effects of " + victim.getName() + "'s Safeguard faded.");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both lists
			victim.getEffects().remove(this);
			b.getEffects(victim.user()).remove(this);
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
			return !caster.hasAbility(AbilityNamesies.INFILTRATOR);
		}

		public String statusPreventionMessage(ActivePokemon victim) {
			return "Safeguard protects " + victim.getName() + " from status conditions!";
		}
	}

	private static class GuardSpecial extends PokemonEffect implements StatusPreventionEffect {
		private static final long serialVersionUID = 1L;

		GuardSpecial() {
			super(EffectNamesies.GUARD_SPECIAL, 5, 5, false);
		}

		public GuardSpecial newInstance() {
			return (GuardSpecial)(new GuardSpecial().activate());
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

	private static class Encore extends PokemonEffect implements AttackSelectionEffect, ForceMoveEffect, BeforeTurnEffect, EndTurnEffect {
		private static final long serialVersionUID = 1L;
		private Move move;

		Encore() {
			super(EffectNamesies.ENCORE, 3, 3, false);
		}

		public Encore newInstance() {
			Encore x = (Encore)(new Encore().activate());
			x.move = move;
			return x;
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

		public Move getMove() {
			return move;
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			if (p.getAttack().namesies() != move.getAttack().namesies()) {
				b.printAttacking(p);
				b.addMessage(this.getFailMessage(b, p, opp));
				return false;
			}
			return true;
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (move.getPP() == 0) active = false; // If the move runs out of PP, Encore immediately ends
		}
	}

	private static class Disable extends PokemonEffect implements AttackSelectionEffect, MoveHolder, BeforeTurnEffect {
		private static final long serialVersionUID = 1L;
		private Move disabled;
		private int turns;

		Disable() {
			super(EffectNamesies.DISABLE, -1, -1, false);
		}

		public Disable newInstance() {
			Disable x = (Disable)(new Disable().activate());
			x.disabled = disabled;
			x.turns = (int)(Math.random()*4) + 4; // Between 4 and 7 turns
			return x;
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

		public Move getMove() {
			return disabled;
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			turns--;
			if (p.getAttack().namesies() == disabled.getAttack().namesies()) {
				b.printAttacking(p);
				b.addMessage(p.getAttack().getName() + " is disabled!");
				return false;
			}
			return true;
		}
	}

	private static class RaiseCrits extends PokemonEffect implements CritStageEffect, PassableEffect {
		private static final long serialVersionUID = 1L;
		private boolean focusEnergy;
		private boolean direHit;
		private boolean berrylicious;

		RaiseCrits() {
			super(EffectNamesies.RAISE_CRITS, -1, -1, false);
		}

		public RaiseCrits newInstance() {
			RaiseCrits x = (RaiseCrits)(new RaiseCrits().activate());
			x.focusEnergy = false;
			x.direHit = false;
			x.berrylicious = false;
			return x;
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
				b.addMessage(getCastMessage(b, caster, victim));
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

		public int increaseCritStage(Integer stage, ActivePokemon p) {
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

	private static class ChangeItem extends PokemonEffect implements ItemHolder {
		private static final long serialVersionUID = 1L;
		private Item item;

		ChangeItem() {
			super(EffectNamesies.CHANGE_ITEM, -1, -1, false);
		}

		public ChangeItem newInstance() {
			ChangeItem x = (ChangeItem)(new ChangeItem().activate());
			x.item = item;
			return x;
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

	private static class ChangeType extends PokemonEffect implements TypeHolder {
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

		public ChangeType newInstance() {
			ChangeType x = (ChangeType)(new ChangeType().activate());
			x.type = type;
			x.castSource = castSource;
			x.typeSource = typeSource;
			return x;
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
			b.addMessage("", p);
		}

		public Type[] getType(Battle b, ActivePokemon p, Boolean display) {
			return type;
		}
	}

	private static class ChangeAbility extends PokemonEffect implements AbilityHolder {
		private static final long serialVersionUID = 1L;
		private Ability ability;
		private String message;

		ChangeAbility() {
			super(EffectNamesies.CHANGE_ABILITY, -1, -1, false);
		}

		public ChangeAbility newInstance() {
			ChangeAbility x = (ChangeAbility)(new ChangeAbility().activate());
			x.ability = ability;
			x.message = message;
			return x;
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

	private static class Stockpile extends PokemonEffect implements StageChangingEffect {
		private static final long serialVersionUID = 1L;
		private int turns;

		Stockpile() {
			super(EffectNamesies.STOCKPILE, -1, -1, false);
		}

		public Stockpile newInstance() {
			Stockpile x = (Stockpile)(new Stockpile().activate());
			x.turns = 0;
			return x;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!victim.hasEffect(this.namesies)) {
				super.cast(b, caster, victim, source, printCast);
			}
			
			Stockpile stockpile = (Stockpile)victim.getEffect(this.namesies);
			if (stockpile.turns < 3) {
				b.addMessage(victim.getName() + " Defense and Special Defense were raised!");
				stockpile.turns++;
				return;
			}
			
			b.addMessage(this.getFailMessage(b, caster, victim));
		}

		public void subside(Battle b, ActivePokemon p) {
			b.addMessage("The effects of " + p.getName() + "'s Stockpile ended!");
			b.addMessage(p.getName() + "'s Defense and Special Defense decreased!");
		}

		public int getTurns() {
			return turns;
		}

		public int adjustStage(Integer stage, Stat s, ActivePokemon p, ActivePokemon opp, Battle b) {
			return s == Stat.DEFENSE || s == Stat.SP_DEFENSE ? stage + turns : stage;
		}
	}

	private static class UsedDefenseCurl extends PokemonEffect {
		private static final long serialVersionUID = 1L;

		UsedDefenseCurl() {
			super(EffectNamesies.USED_DEFENSE_CURL, -1, -1, false);
		}

		public UsedDefenseCurl newInstance() {
			return (UsedDefenseCurl)(new UsedDefenseCurl().activate());
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!victim.hasEffect(this.namesies)) {
				super.cast(b, caster, victim, source, printCast);
			}
			else {
				b.addMessage(getCastMessage(b, caster, victim));
			}
		}
	}

	private static class UsedMinimize extends PokemonEffect {
		private static final long serialVersionUID = 1L;

		UsedMinimize() {
			super(EffectNamesies.USED_MINIMIZE, -1, -1, false);
		}

		public UsedMinimize newInstance() {
			return (UsedMinimize)(new UsedMinimize().activate());
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!victim.hasEffect(this.namesies)) {
				super.cast(b, caster, victim, source, printCast);
			}
			else {
				b.addMessage(getCastMessage(b, caster, victim));
			}
		}
	}

	private static class Mimic extends PokemonEffect implements MoveListHolder {
		private static final long serialVersionUID = 1L;
		private Move mimicMove;

		Mimic() {
			super(EffectNamesies.MIMIC, -1, -1, false);
		}

		public Mimic newInstance() {
			Mimic x = (Mimic)(new Mimic().activate());
			x.mimicMove = mimicMove;
			return x;
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			ActivePokemon other = b.getOtherPokemon(victim.user());
			final Move lastMoveUsed = other.getAttributes().getLastMoveUsed();
			Attack lastAttack = lastMoveUsed == null ? null : lastMoveUsed.getAttack();
			
			if (lastAttack == null || victim.hasMove(b, lastAttack.namesies()) || lastAttack.isMoveType(MoveType.MIMICLESS)) {
				b.addMessage(this.getFailMessage(b, caster, victim));
				return;
			}
			
			mimicMove = new Move(lastAttack);
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " learned " + mimicMove.getAttack().getName() + "!";
		}

		public Move[] getMoveList(ActivePokemon p, Move[] moves) {
			Move[] list = new Move[moves.length];
			for (int i = 0; i < list.length; i++) {
				if (moves[i].getAttack().namesies() == AttackNamesies.MIMIC) {
					list[i] = mimicMove;
				}
				else {
					list[i] = moves[i];
				}
			}
			
			return list;
		}
	}

	private static class Imprison extends PokemonEffect implements AttackSelectionEffect, BeforeTurnEffect {
		private static final long serialVersionUID = 1L;
		private List<AttackNamesies> unableMoves;

		Imprison() {
			super(EffectNamesies.IMPRISON, -1, -1, false);
		}

		public Imprison newInstance() {
			Imprison x = (Imprison)(new Imprison().activate());
			x.unableMoves = unableMoves;
			return x;
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
				b.addMessage(this.getFailMessage(b, p, opp));
				return false;
			}
			
			return true;
		}
	}

	private static class Trapped extends PokemonEffect implements TrappingEffect {
		private static final long serialVersionUID = 1L;

		Trapped() {
			super(EffectNamesies.TRAPPED, -1, -1, false);
		}

		public Trapped newInstance() {
			return (Trapped)(new Trapped().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " can't escape!";
		}

		public boolean isTrapped(Battle b, ActivePokemon p) {
			// Ghost-type Pokemon can always escape
			return !p.isType(b, Type.GHOST);
		}

		public String trappingMessage(ActivePokemon trapped) {
			return trapped.getName() + " cannot be recalled at this time!";
		}
	}

	private static class Foresight extends PokemonEffect implements AdvantageChanger {
		private static final long serialVersionUID = 1L;

		Foresight() {
			super(EffectNamesies.FORESIGHT, -1, -1, false);
		}

		public Foresight newInstance() {
			return (Foresight)(new Foresight().activate());
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
				b.addMessage(getCastMessage(b, caster, victim));
			}
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " identified " + victim.getName() + "!";
		}
	}

	private static class MiracleEye extends PokemonEffect implements AdvantageChanger {
		private static final long serialVersionUID = 1L;

		MiracleEye() {
			super(EffectNamesies.MIRACLE_EYE, -1, -1, false);
		}

		public MiracleEye newInstance() {
			return (MiracleEye)(new MiracleEye().activate());
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
				b.addMessage(getCastMessage(b, caster, victim));
			}
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " identified " + victim.getName() + "!";
		}
	}

	private static class Torment extends PokemonEffect implements AttackSelectionEffect, BeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		Torment() {
			super(EffectNamesies.TORMENT, -1, -1, false);
		}

		public Torment newInstance() {
			return (Torment)(new Torment().activate());
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
				b.addMessage(this.getFailMessage(b, p, opp));
				return false;
			}
			return true;
		}
	}

	private static class Taunt extends PokemonEffect implements AttackSelectionEffect, BeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		Taunt() {
			super(EffectNamesies.TAUNT, 3, 3, false);
		}

		public Taunt newInstance() {
			return (Taunt)(new Taunt().activate());
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
				b.addMessage(this.getFailMessage(b, p, opp));
				return false;
			}
			return true;
		}
	}

	private static class LockOn extends PokemonEffect implements PassableEffect, AccuracyBypassEffect {
		private static final long serialVersionUID = 1L;

		LockOn() {
			super(EffectNamesies.LOCK_ON, 2, 2, false);
		}

		public LockOn newInstance() {
			return (LockOn)(new LockOn().activate());
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

	private static class Telekinesis extends PokemonEffect implements LevitationEffect, OpponentAccuracyBypassEffect {
		private static final long serialVersionUID = 1L;

		Telekinesis() {
			super(EffectNamesies.TELEKINESIS, 4, 4, false);
		}

		public Telekinesis newInstance() {
			return (Telekinesis)(new Telekinesis().activate());
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
			b.addMessage("The effects of telekinesis were cancelled!");
			
			// TODO: Fix this it's broken
			// Effect.removeEffect(fallen.getEffects(), this.namesies());
		}

		public boolean opponentBypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
			// Opponent can always strike you unless they are using a OHKO move or you are semi-invulnerable
			return !attacking.getAttack().isMoveType(MoveType.ONE_HIT_KO) && !defending.isSemiInvulnerable();
		}
	}

	private static class Ingrain extends PokemonEffect implements TrappingEffect, EndTurnEffect, GroundedEffect, PassableEffect, BeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		Ingrain() {
			super(EffectNamesies.INGRAIN, -1, -1, false);
		}

		public Ingrain newInstance() {
			return (Ingrain)(new Ingrain().activate());
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

		public boolean isTrapped(Battle b, ActivePokemon p) {
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
			
			b.addMessage(victim.getName() + " restored some HP due to ingrain!", victim);
		}

		private void removeLevitation(Battle b, ActivePokemon p) {
			if (p.isSemiInvulnerableFlying()) {
				p.getMove().switchReady(b, p);
				b.addMessage(p.getName() + " fell to the ground!");
			}
			
			Battle.invoke(b.getEffectsList(p), LevitationEffect.class, "fall", b, p);
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			if (p.getAttack().isMoveType(MoveType.AIRBORNE)) {
				b.printAttacking(p);
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return false;
			}
			
			return true;
		}
	}

	private static class Grounded extends PokemonEffect implements GroundedEffect, BeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		Grounded() {
			super(EffectNamesies.GROUNDED, -1, -1, false);
		}

		public Grounded newInstance() {
			return (Grounded)(new Grounded().activate());
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
				b.addMessage(p.getName() + " fell to the ground!");
			}
			
			Battle.invoke(b.getEffectsList(p), LevitationEffect.class, "fall", b, p);
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			if (p.getAttack().isMoveType(MoveType.AIRBORNE)) {
				b.printAttacking(p);
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return false;
			}
			
			return true;
		}
	}

	private static class Curse extends PokemonEffect implements EndTurnEffect, PassableEffect {
		private static final long serialVersionUID = 1L;

		Curse() {
			super(EffectNamesies.CURSE, -1, -1, false);
		}

		public Curse newInstance() {
			return (Curse)(new Curse().activate());
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
			
			b.addMessage(victim.getName() + " was hurt by the curse!");
			victim.reduceHealthFraction(b, 1/4.0);
		}
	}

	private static class Yawn extends PokemonEffect {
		private static final long serialVersionUID = 1L;

		Yawn() {
			super(EffectNamesies.YAWN, 2, 2, false);
		}

		public Yawn newInstance() {
			return (Yawn)(new Yawn().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(!Status.applies(StatusCondition.ASLEEP, b, caster, victim) || victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " grew drowsy!";
		}

		public void subside(Battle b, ActivePokemon p) {
			Status.giveStatus(b, b.getOtherPokemon(p.user()), p, StatusCondition.ASLEEP);
		}
	}

	private static class MagnetRise extends PokemonEffect implements LevitationEffect, PassableEffect {
		private static final long serialVersionUID = 1L;

		MagnetRise() {
			super(EffectNamesies.MAGNET_RISE, 5, 5, false);
		}

		public MagnetRise newInstance() {
			return (MagnetRise)(new MagnetRise().activate());
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
			b.addMessage("The effects of " + fallen.getName() + "'s magnet rise were cancelled!");
			
			// TODO: Fix this it's broken
			// Effect.removeEffect(fallen.getEffects(), this.namesies());
		}
	}

	private static class Uproar extends PokemonEffect implements ForceMoveEffect, AttackSelectionEffect, EndTurnEffect {
		private static final long serialVersionUID = 1L;
		private Move uproar;
		
		private static void wakeUp(Battle b, ActivePokemon wakey) {
			if (wakey.hasStatus(StatusCondition.ASLEEP)) {
				wakey.removeStatus();
				b.addMessage("The uproar woke up " + wakey.getName() + "!", wakey);
			}
		}

		Uproar() {
			super(EffectNamesies.UPROAR, 3, 3, false);
		}

		public Uproar newInstance() {
			Uproar x = (Uproar)(new Uproar().activate());
			x.uproar = uproar;
			return x;
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			uproar = victim.getMove();
			super.cast(b, caster, victim, source, printCast);
			
			wakeUp(b, victim);
			wakeUp(b, b.getOtherPokemon(victim.user()));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " started an uproar!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The uproar ended.";
		}

		public Move getMove() {
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

	private static class AquaRing extends PokemonEffect implements PassableEffect, EndTurnEffect {
		private static final long serialVersionUID = 1L;

		AquaRing() {
			super(EffectNamesies.AQUA_RING, -1, -1, false);
		}

		public AquaRing newInstance() {
			return (AquaRing)(new AquaRing().activate());
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
			
			b.addMessage(victim.getName() + " restored some HP due to aqua ring!", victim);
		}
	}

	private static class Nightmare extends PokemonEffect implements EndTurnEffect {
		private static final long serialVersionUID = 1L;

		Nightmare() {
			super(EffectNamesies.NIGHTMARE, -1, -1, false);
		}

		public Nightmare newInstance() {
			return (Nightmare)(new Nightmare().activate());
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
			
			b.addMessage(victim.getName() + " was hurt by its nightmare!");
			victim.reduceHealthFraction(b, 1/4.0);
		}
	}

	private static class Charge extends PokemonEffect implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		Charge() {
			super(EffectNamesies.CHARGE, 2, 2, false);
		}

		public Charge newInstance() {
			return (Charge)(new Charge().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getAttackType() == Type.ELECTRIC ? 2 : 1;
		}
	}

	private static class Focusing extends PokemonEffect {
		private static final long serialVersionUID = 1L;

		Focusing() {
			super(EffectNamesies.FOCUSING, 1, 1, false);
		}

		public Focusing newInstance() {
			return (Focusing)(new Focusing().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " began tightening its focus!";
		}
	}

	private static class FiddyPercentStronger extends PokemonEffect implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		FiddyPercentStronger() {
			super(EffectNamesies.FIDDY_PERCENT_STRONGER, 1, 1, false);
		}

		public FiddyPercentStronger newInstance() {
			return (FiddyPercentStronger)(new FiddyPercentStronger().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return 1.5;
		}
	}

	private static class Transformed extends PokemonEffect implements MoveListHolder, StatsHolder, TypeHolder {
		private static final long serialVersionUID = 1L;
		private Move[] moveList;
		private int[] stats;
		private Type[] type;

		Transformed() {
			super(EffectNamesies.TRANSFORMED, -1, -1, false);
		}

		public Transformed newInstance() {
			Transformed x = (Transformed)(new Transformed().activate());
			x.moveList = moveList;
			x.stats = stats;
			x.type = type;
			return x;
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(b.getOtherPokemon(victim.user()).hasEffect(this.namesies) || ((caster.hasAbility(AbilityNamesies.ILLUSION) && caster.getAbility().isActive())) || victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			// Pokemon to transform into
			ActivePokemon transformee = b.getOtherPokemon(victim.user());
			
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
			b.addMessage("", transformee.getPokemonInfo(), transformee.isShiny(), true, victim.user());
			b.addMessage("", victim);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " transformed into " + b.getOtherPokemon(victim.user()).getPokemonInfo().getName() + "!";
		}

		public Move[] getMoveList(ActivePokemon p, Move[] moves) {
			return moveList;
		}

		public int getStat(ActivePokemon user, Stat stat) {
			return stats[stat.index()];
		}

		public Type[] getType(Battle b, ActivePokemon p, Boolean display) {
			return type;
		}
	}

	private static class Substitute extends PokemonEffect implements IntegerHolder, PassableEffect, EffectBlockerEffect {
		private static final long serialVersionUID = 1L;
		private int hp;

		Substitute() {
			super(EffectNamesies.SUBSTITUTE, -1, -1, false);
		}

		public Substitute newInstance() {
			Substitute x = (Substitute)(new Substitute().activate());
			x.hp = hp;
			return x;
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.getHPRatio() <= .25 || victim.getMaxHP() <= 3 || victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			hp = victim.reduceHealthFraction(b, .25) + 1;
			super.cast(b, caster, victim, source, printCast);
			b.addMessage("", victim);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " put in a substitute!";
		}

		public int getAmount() {
			return hp;
		}

		public void decrease(int amount) {
			hp -= amount;
		}

		public void increase(int amount) {
			hp += amount;
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
				b.addMessage(this.getFailMessage(b, user, victim));
			}
			
			return false;
		}
	}

	private static class Mist extends PokemonEffect implements StatProtectingEffect, DefogRelease {
		private static final long serialVersionUID = 1L;

		Mist() {
			super(EffectNamesies.MIST, 5, 5, false);
		}

		public Mist newInstance() {
			return (Mist)(new Mist().activate());
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
			b.addMessage("The mist faded.");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both lists
			victim.getEffects().remove(this);
			b.getEffects(victim.user()).remove(this);
		}
	}

	private static class MagicCoat extends PokemonEffect implements TargetSwapperEffect {
		private static final long serialVersionUID = 1L;

		MagicCoat() {
			super(EffectNamesies.MAGIC_COAT, 1, 1, false);
		}

		public MagicCoat newInstance() {
			return (MagicCoat)(new MagicCoat().activate());
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
				b.addMessage(opponent.getName() + "'s " + "Magic Coat" + " reflected " + user.getName() + "'s move!");
				return true;
			}
			
			return false;
		}
	}

	private static class Bide extends PokemonEffect implements ForceMoveEffect, EndTurnEffect, IntegerHolder {
		private static final long serialVersionUID = 1L;
		private Move move;
		private int turns;
		private int damage;

		Bide() {
			super(EffectNamesies.BIDE, -1, -1, false);
		}

		public Bide newInstance() {
			Bide x = (Bide)(new Bide().activate());
			x.move = move;
			x.turns = 1;
			x.damage = 0;
			return x;
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
				b.addMessage(getCastMessage(b, caster, victim));
				return;
			}
			
			// TIME'S UP -- RELEASE DAT STORED ENERGY
			b.addMessage(victim.getName() + " released energy!");
			if (bidesies.damage == 0) {
				// Sucks to suck
				b.addMessage(this.getFailMessage(b, caster, victim));
			}
			else {
				// RETALIATION STATION
				b.getOtherPokemon(victim.user()).reduceHealth(b, 2*bidesies.damage);
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

		public Move getMove() {
			return move;
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			increase(victim.getAttributes().getDamageTaken());
		}

		public int getAmount() {
			return damage;
		}

		public void decrease(int amount) {
			damage -= amount;
		}

		public void increase(int amount) {
			damage += amount;
		}
	}

	private static class HalfWeight extends PokemonEffect implements HalfWeightEffect {
		private static final long serialVersionUID = 1L;
		private int layers;

		HalfWeight() {
			super(EffectNamesies.HALF_WEIGHT, -1, -1, false);
		}

		public HalfWeight newInstance() {
			HalfWeight x = (HalfWeight)(new HalfWeight().activate());
			x.layers = 1;
			return x;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			HalfWeight halfWeight = (HalfWeight)victim.getEffect(this.namesies);
			if (halfWeight == null) super.cast(b, caster, victim, source, printCast);
			else halfWeight.layers++;
		}

		public int getHalfAmount(Integer halfAmount) {
			return halfAmount + layers;
		}
	}

	private static class PowerTrick extends PokemonEffect implements PassableEffect, StatSwitchingEffect {
		private static final long serialVersionUID = 1L;

		PowerTrick() {
			super(EffectNamesies.POWER_TRICK, -1, -1, false);
		}

		public PowerTrick newInstance() {
			return (PowerTrick)(new PowerTrick().activate());
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			PokemonEffect thaPowah = victim.getEffect(this.namesies);
			if (thaPowah == null) {
				super.cast(b, caster, victim, source, printCast);
				return;
			}
			
			b.addMessage(getCastMessage(b, caster, victim));
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

	private static class PowerSplit extends PokemonEffect implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		PowerSplit() {
			super(EffectNamesies.POWER_SPLIT, -1, -1, false);
		}

		public PowerSplit newInstance() {
			return (PowerSplit)(new PowerSplit().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " split the power!";
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
			int stat = statValue;
			
			// If the stat is a splitting stat, return the average between the user and the opponent
			if (s == Stat.ATTACK || s == Stat.SP_ATTACK) {
				return (p.getStat(b, s) + opp.getStat(b, s))/2;
			}
			
			return stat;
		}
	}

	private static class GuardSplit extends PokemonEffect implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		GuardSplit() {
			super(EffectNamesies.GUARD_SPLIT, -1, -1, false);
		}

		public GuardSplit newInstance() {
			return (GuardSplit)(new GuardSplit().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " split the defense!";
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
			int stat = statValue;
			
			// If the stat is a splitting stat, return the average between the user and the opponent
			if (s == Stat.DEFENSE || s == Stat.SP_DEFENSE) {
				return (p.getStat(b, s) + opp.getStat(b, s))/2;
			}
			
			return stat;
		}
	}

	private static class HealBlock extends PokemonEffect implements BeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		HealBlock() {
			super(EffectNamesies.HEAL_BLOCK, 5, 5, false);
		}

		public HealBlock newInstance() {
			return (HealBlock)(new HealBlock().activate());
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
			if (p.getAttack().isMoveType(MoveType.SAP_HEALTH)) {
				b.printAttacking(p);
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return false;
			}
			
			return true;
		}
	}

	private static class Infatuated extends PokemonEffect implements BeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		Infatuated() {
			super(EffectNamesies.INFATUATED, -1, -1, false);
		}

		public Infatuated newInstance() {
			return (Infatuated)(new Infatuated().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !((victim.hasAbility(AbilityNamesies.OBLIVIOUS) && !caster.breaksTheMold()) || (victim.hasAbility(AbilityNamesies.AROMA_VEIL) && !caster.breaksTheMold()) || !Gender.oppositeGenders(caster, victim) || victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (victim.isHoldingItem(b, ItemNamesies.DESTINY_KNOT) && this.applies(b, victim, caster, CastSource.HELD_ITEM)) {
				super.cast(b, victim, caster, CastSource.HELD_ITEM, false);
				b.addMessage(victim.getName() + "'s " + ItemNamesies.DESTINY_KNOT.getName() + " caused " + caster.getName() + " to fall in love!");
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
			b.addMessage(p.getName() + " is in love with " + opp.getName() + "!");
			if (Math.random() < .5) {
				return true;
			}
			
			b.addMessage(p.getName() + "'s infatuation kept it from attacking!");
			return false;
		}
	}

	private static class Snatch extends PokemonEffect implements TargetSwapperEffect {
		private static final long serialVersionUID = 1L;

		Snatch() {
			super(EffectNamesies.SNATCH, 1, 1, false);
		}

		public Snatch newInstance() {
			return (Snatch)(new Snatch().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public boolean swapTarget(Battle b, ActivePokemon user, ActivePokemon opponent) {
			Attack attack = user.getAttack();
			if (attack.isSelfTarget() && attack.getCategory() == MoveCategory.STATUS && !attack.isMoveType(MoveType.NON_SNATCHABLE)) {
				b.addMessage(opponent.getName() + " snatched " + user.getName() + "'s move!");
				return true;
			}
			
			return false;
		}
	}

	private static class Grudge extends PokemonEffect implements FaintEffect {
		private static final long serialVersionUID = 1L;

		Grudge() {
			super(EffectNamesies.GRUDGE, -1, -1, false);
		}

		public Grudge newInstance() {
			return (Grudge)(new Grudge().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " wants " + b.getOtherPokemon(victim.user()).getName() + " to bear a grudge!";
		}

		public void deathwish(Battle b, ActivePokemon dead, ActivePokemon murderer) {
			if (murderer.getAttributes().isAttacking()) {
				b.addMessage(murderer.getName() + "'s " + murderer.getAttack().getName() + " lost all its PP due to its grudge!");
				murderer.getMove().reducePP(murderer.getMove().getPP());
			}
		}
	}

	private static class DestinyBond extends PokemonEffect implements FaintEffect, BeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		DestinyBond() {
			super(EffectNamesies.DESTINY_BOND, -1, -1, false);
		}

		public DestinyBond newInstance() {
			return (DestinyBond)(new DestinyBond().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.getName() + " is trying to take " + b.getOtherPokemon(victim.user()).getName() + " down with it!";
		}

		public void deathwish(Battle b, ActivePokemon dead, ActivePokemon murderer) {
			if (murderer.getAttributes().isAttacking()) {
				b.addMessage(dead.getName() + " took " + murderer.getName() + " down with it!");
				murderer.reduceHealthFraction(b, 1);
			}
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			super.active = false;
			return true;
		}
	}

	private static class PerishSong extends PokemonEffect implements PassableEffect, EndTurnEffect {
		private static final long serialVersionUID = 1L;

		PerishSong() {
			super(EffectNamesies.PERISH_SONG, 3, 3, false);
		}

		public PerishSong newInstance() {
			return (PerishSong)(new PerishSong().activate());
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
			b.addMessage(victim.getName() + "'s Perish Song count fell to " + (super.numTurns - 1) + "!");
			if (super.numTurns == 1) {
				victim.reduceHealthFraction(b, 1);
			}
		}
	}

	private static class Embargo extends PokemonEffect implements PassableEffect {
		private static final long serialVersionUID = 1L;

		Embargo() {
			super(EffectNamesies.EMBARGO, 5, 5, false);
		}

		public Embargo newInstance() {
			return (Embargo)(new Embargo().activate());
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

	private static class ConsumedItem extends PokemonEffect implements ItemHolder {
		private static final long serialVersionUID = 1L;
		private Item consumed;

		ConsumedItem() {
			super(EffectNamesies.CONSUMED_ITEM, -1, -1, false);
		}

		public ConsumedItem newInstance() {
			ConsumedItem x = (ConsumedItem)(new ConsumedItem().activate());
			x.consumed = consumed;
			return x;
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

	private static class FairyLock extends PokemonEffect implements OpponentTrappingEffect {
		private static final long serialVersionUID = 1L;

		FairyLock() {
			super(EffectNamesies.FAIRY_LOCK, -1, -1, false);
		}

		public FairyLock newInstance() {
			return (FairyLock)(new FairyLock().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public boolean trapOpponent(Battle b, ActivePokemon p) {
			return true;
		}

		public String opponentTrappingMessage(ActivePokemon escaper, ActivePokemon trapper) {
			return trapper.getName() + "'s " + this.getName() + " prevents " + escaper.getName() + " from escaping!";
		}
	}

	private static class Powder extends PokemonEffect implements BeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		Powder() {
			super(EffectNamesies.POWDER, 1, 1, false);
		}

		public Powder newInstance() {
			return (Powder)(new Powder().activate());
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
				b.addMessage("The powder exploded!");
				p.reduceHealthFraction(b, 1/4.0);
				return false;
			}
			
			return true;
		}
	}

	private static class Electrified extends PokemonEffect implements ChangeAttackTypeEffect {
		private static final long serialVersionUID = 1L;

		Electrified() {
			super(EffectNamesies.ELECTRIFIED, 1, 1, false);
		}

		public Electrified newInstance() {
			return (Electrified)(new Electrified().activate());
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

	private static class EatenBerry extends PokemonEffect {
		private static final long serialVersionUID = 1L;

		EatenBerry() {
			super(EffectNamesies.EATEN_BERRY, -1, -1, false);
		}

		public EatenBerry newInstance() {
			return (EatenBerry)(new EatenBerry().activate());
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!victim.hasEffect(this.namesies)) {
				super.cast(b, caster, victim, source, printCast);
			}
			else {
				b.addMessage(getCastMessage(b, caster, victim));
			}
		}
	}
}
