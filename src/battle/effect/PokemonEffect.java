package battle.effect;

import item.Item;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import main.Global;
import main.Type;
import pokemon.Ability;
import pokemon.ActivePokemon;
import pokemon.Gender;
import pokemon.Stat;
import battle.Attack;
import battle.Attack.Category;
import battle.Attack.MoveType;
import battle.Battle;
import battle.Move;
import battle.effect.Status.StatusCondition;

// Class to handle effects that are on a single Pokemon
public abstract class PokemonEffect extends Effect implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static HashMap<String, PokemonEffect> map;
	
	public PokemonEffect(String name, int min, int max, boolean sub)
	{
		super(name, min, max, sub);
	}
	
	public String getName()
	{
		return name;
	}
	
	public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
	{
		if (printCast) b.addMessage(getCastMessage(b, caster, victim));
		victim.addEffect(this.newInstance());
	}
	
	public abstract PokemonEffect newInstance();

	public static PokemonEffect getEffect(String e)
	{
		if (map == null) loadEffects();
		if (map.containsKey(e)) return map.get(e);

		Global.error("No such Effect " + e);
		return null;
	}

	// Create and load the effects map if it doesn't already exist
	public static void loadEffects()
	{
		if (map != null) return;
		map = new HashMap<>();
		
		// EVERYTHING BELOW IS GENERATED ###

		// List all of the classes we are loading
		map.put("LeechSeed", new LeechSeed());
		map.put("BadPoison", new BadPoison());
		map.put("Flinch", new Flinch());
		map.put("FireSpin", new FireSpin());
		map.put("MagmaStorm", new MagmaStorm());
		map.put("Clamped", new Clamped());
		map.put("Whirlpool", new Whirlpool());
		map.put("Wrapped", new Wrapped());
		map.put("Binded", new Binded());
		map.put("SandTomb", new SandTomb());
		map.put("Protecting", new Protecting());
		map.put("QuickGuard", new QuickGuard());
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
	}

	/**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

	private static class LeechSeed extends PokemonEffect implements EndTurnEffect, RapidSpinRelease, PassableEffect
	{
		private static final long serialVersionUID = 1L;

		public LeechSeed()
		{
			super("LeechSeed", -1, -1, false);
		}

		public LeechSeed newInstance()
		{
			return (LeechSeed)(new LeechSeed().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.isType(b, Type.GRASS) || victim.hasEffect("LeechSeed"));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return victim.getName() + " was seeded!";
		}

		public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (victim.isType(b, Type.GRASS)) return "It doesn't affect " + victim.getName() + "!";
			if (victim.hasEffect("LeechSeed")) return victim.getName() + " is already seeded!";
			return super.getFailMessage(b, user, victim);
		}

		public void apply(ActivePokemon victim, Battle b)
		{
			if (victim.hasAbility("Magic Guard")) return;
			b.addMessage(victim.getName() + "'s health was sapped!");
			b.getOtherPokemon(victim.user()).sapHealth(victim, victim.reduceHealthFraction(b, 1/8.0), b, false);
		}

		public String getReleaseMessage(ActivePokemon user)
		{
			return user.getName() + " was released from leech seed!";
		}
	}

	private static class BadPoison extends PokemonEffect implements EndTurnEffect
	{
		private static final long serialVersionUID = 1L;
		private int turns;

		public BadPoison()
		{
			super("BadPoison", -1, -1, false);
		}

		public BadPoison newInstance()
		{
			return (BadPoison)(new BadPoison().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(!Status.applies(StatusCondition.POISONED, b, caster, victim));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			super.cast(b, caster, victim, source, printCast);
			Status.giveStatus(b, caster, victim, StatusCondition.POISONED);
		}

		public int getTurns()
		{
			return turns;
		}

		public void apply(ActivePokemon victim, Battle b)
		{
			turns++;
		}
	}

	private static class Flinch extends PokemonEffect implements BeforeTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public Flinch()
		{
			super("Flinch", 1, 1, false);
		}

		public Flinch newInstance()
		{
			return (Flinch)(new Flinch().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !((victim.hasAbility("Inner Focus") && !caster.breaksTheMold()) || !b.isFirstAttack() || victim.hasEffect("Flinch"));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			super.cast(b, caster, victim, source, printCast);
			if (victim.hasAbility("Steadfast"))
			{
				victim.getAttributes().modifyStage(victim, victim, 1, Stat.SPEED, b, CastSource.ABILITY);
			}
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return victim.getName() + " flinched!";
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b)
		{
			return false;
		}
	}

	private static class FireSpin extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease
	{
		private static final long serialVersionUID = 1L;

		public FireSpin()
		{
			super("FireSpin", 4, 5, true);
		}

		public FireSpin newInstance()
		{
			return (FireSpin)(new FireSpin().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("FireSpin"));
		}

		public String getPartialTrapMessage(ActivePokemon victim)
		{
			return victim.getName() + " is hurt by fire spin!";
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, "Grip Claw")) setTurns(5);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return victim.getName() + " was trapped in the fiery vortex!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return victim.getName() + " is no longer trapped by fire spin.";
		}

		public void apply(ActivePokemon victim, Battle b)
		{
			if (victim.hasAbility("Magic Guard"))
			{
				return;
			}
			
			b.addMessage(getPartialTrapMessage(victim));
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim.user()).isHoldingItem(b, "Binding Band") ? 1/6.0 : 1/8.0);
		}

		public String getReleaseMessage(ActivePokemon user)
		{
			return user.getName() + " was released from fire spin!";
		}
	}

	private static class MagmaStorm extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease
	{
		private static final long serialVersionUID = 1L;

		public MagmaStorm()
		{
			super("MagmaStorm", 4, 5, true);
		}

		public MagmaStorm newInstance()
		{
			return (MagmaStorm)(new MagmaStorm().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("MagmaStorm"));
		}

		public String getPartialTrapMessage(ActivePokemon victim)
		{
			return victim.getName() + " is hurt by magma storm!";
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, "Grip Claw")) setTurns(5);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return victim.getName() + " was trapped by swirling magma!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return victim.getName() + " is no longer trapped by magma storm.";
		}

		public void apply(ActivePokemon victim, Battle b)
		{
			if (victim.hasAbility("Magic Guard"))
			{
				return;
			}
			
			b.addMessage(getPartialTrapMessage(victim));
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim.user()).isHoldingItem(b, "Binding Band") ? 1/6.0 : 1/8.0);
		}

		public String getReleaseMessage(ActivePokemon user)
		{
			return user.getName() + " was released from magma storm!";
		}
	}

	private static class Clamped extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease
	{
		private static final long serialVersionUID = 1L;

		public Clamped()
		{
			super("Clamped", 4, 5, true);
		}

		public Clamped newInstance()
		{
			return (Clamped)(new Clamped().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("Clamped"));
		}

		public String getPartialTrapMessage(ActivePokemon victim)
		{
			return victim.getName() + " is hurt by clamp!";
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, "Grip Claw")) setTurns(5);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getName() + " clamped " + victim.getName() + "!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return victim.getName() + " is no longer trapped by clamp.";
		}

		public void apply(ActivePokemon victim, Battle b)
		{
			if (victim.hasAbility("Magic Guard"))
			{
				return;
			}
			
			b.addMessage(getPartialTrapMessage(victim));
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim.user()).isHoldingItem(b, "Binding Band") ? 1/6.0 : 1/8.0);
		}

		public String getReleaseMessage(ActivePokemon user)
		{
			return user.getName() + " was released from clamp!";
		}
	}

	private static class Whirlpool extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease
	{
		private static final long serialVersionUID = 1L;

		public Whirlpool()
		{
			super("Whirlpool", 4, 5, true);
		}

		public Whirlpool newInstance()
		{
			return (Whirlpool)(new Whirlpool().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("Whirlpool"));
		}

		public String getPartialTrapMessage(ActivePokemon victim)
		{
			return victim.getName() + " is hurt by whirlpool!";
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, "Grip Claw")) setTurns(5);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return victim.getName() + " was trapped in the vortex!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return victim.getName() + " is no longer trapped by whirlpool.";
		}

		public void apply(ActivePokemon victim, Battle b)
		{
			if (victim.hasAbility("Magic Guard"))
			{
				return;
			}
			
			b.addMessage(getPartialTrapMessage(victim));
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim.user()).isHoldingItem(b, "Binding Band") ? 1/6.0 : 1/8.0);
		}

		public String getReleaseMessage(ActivePokemon user)
		{
			return user.getName() + " was released from whirlpool!";
		}
	}

	private static class Wrapped extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease
	{
		private static final long serialVersionUID = 1L;

		public Wrapped()
		{
			super("Wrapped", 4, 5, true);
		}

		public Wrapped newInstance()
		{
			return (Wrapped)(new Wrapped().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("Wrapped"));
		}

		public String getPartialTrapMessage(ActivePokemon victim)
		{
			return victim.getName() + " is hurt by wrap!";
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, "Grip Claw")) setTurns(5);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return victim.getName() + " was wrapped by " + user.getName() + "!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return victim.getName() + " was freed from wrap.";
		}

		public void apply(ActivePokemon victim, Battle b)
		{
			if (victim.hasAbility("Magic Guard"))
			{
				return;
			}
			
			b.addMessage(getPartialTrapMessage(victim));
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim.user()).isHoldingItem(b, "Binding Band") ? 1/6.0 : 1/8.0);
		}

		public String getReleaseMessage(ActivePokemon user)
		{
			return user.getName() + " was released from wrap!";
		}
	}

	private static class Binded extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease
	{
		private static final long serialVersionUID = 1L;

		public Binded()
		{
			super("Binded", 4, 5, true);
		}

		public Binded newInstance()
		{
			return (Binded)(new Binded().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("Binded"));
		}

		public String getPartialTrapMessage(ActivePokemon victim)
		{
			return victim.getName() + " is hurt by bind!";
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, "Grip Claw")) setTurns(5);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return victim.getName() + " was binded by " + user.getName() + "!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return victim.getName() + " was freed from bind.";
		}

		public void apply(ActivePokemon victim, Battle b)
		{
			if (victim.hasAbility("Magic Guard"))
			{
				return;
			}
			
			b.addMessage(getPartialTrapMessage(victim));
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim.user()).isHoldingItem(b, "Binding Band") ? 1/6.0 : 1/8.0);
		}

		public String getReleaseMessage(ActivePokemon user)
		{
			return user.getName() + " was released from bind!";
		}
	}

	private static class SandTomb extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease
	{
		private static final long serialVersionUID = 1L;

		public SandTomb()
		{
			super("SandTomb", 4, 5, true);
		}

		public SandTomb newInstance()
		{
			return (SandTomb)(new SandTomb().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("SandTomb"));
		}

		public String getPartialTrapMessage(ActivePokemon victim)
		{
			return victim.getName() + " is hurt by sand tomb!";
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, "Grip Claw")) setTurns(5);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return victim.getName() + " was trapped by sand tomb!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return victim.getName() + " is no longer trapped from sand tomb.";
		}

		public void apply(ActivePokemon victim, Battle b)
		{
			if (victim.hasAbility("Magic Guard"))
			{
				return;
			}
			
			b.addMessage(getPartialTrapMessage(victim));
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim.user()).isHoldingItem(b, "Binding Band") ? 1/6.0 : 1/8.0);
		}

		public String getReleaseMessage(ActivePokemon user)
		{
			return user.getName() + " was released from sand tomb!";
		}
	}

	private static class Protecting extends PokemonEffect implements OpposingBeforeTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public Protecting()
		{
			super("Protecting", 1, 1, false);
		}

		public Protecting newInstance()
		{
			return (Protecting)(new Protecting().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("Protecting"));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			if (Math.random() > caster.getAttributes().getSuccessionDecayRate())
			{
				b.addMessage(this.getFailMessage(b, caster, victim));
				return;
			}
			
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return victim.getName() + " protected itself!";
		}

		public boolean opposingCanAttack(ActivePokemon p, ActivePokemon opp, Battle b)
		{
			if (p.getAttack().isSelfTarget() || p.getAttack().isMoveType(MoveType.PROTECT_PIERCING))
			{
				return true;
			}
			
			b.printAttacking(p);
			b.addMessage(opp.getName() + " is protecting itself!");
			Global.invoke(new Object[] {p.getAttack()}, CrashDamageMove.class, "crash", b, p);
			
			return false;
		}
	}

	private static class QuickGuard extends PokemonEffect implements OpposingBeforeTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public QuickGuard()
		{
			super("QuickGuard", 1, 1, false);
		}

		public QuickGuard newInstance()
		{
			return (QuickGuard)(new QuickGuard().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("QuickGuard"));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			if (Math.random() > caster.getAttributes().getSuccessionDecayRate())
			{
				b.addMessage(this.getFailMessage(b, caster, victim));
				return;
			}
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return victim.getName() + " protected itself!";
		}

		public boolean opposingCanAttack(ActivePokemon p, ActivePokemon opp, Battle b)
		{
			if (p.getAttack().isSelfTarget() || p.getAttack().getName().equals("Feint") || p.getAttack().getPriority(b, p) <= 0) return true;
			b.printAttacking(p);
			b.addMessage(opp.getName() + " is protecting itself!");
			Global.invoke(new Object[] {p.getAttack()}, CrashDamageMove.class, "crash", b, p);
			return false;
		}
	}

	private static class Bracing extends PokemonEffect implements BracingEffect
	{
		private static final long serialVersionUID = 1L;

		public Bracing()
		{
			super("Bracing", 1, 1, false);
		}

		public Bracing newInstance()
		{
			return (Bracing)(new Bracing().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("Bracing"));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			if (Math.random() > caster.getAttributes().getSuccessionDecayRate())
			{
				b.addMessage(this.getFailMessage(b, caster, victim));
				return;
			}
			
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getName() + " braced itself!";
		}

		public boolean isBracing(Battle b, ActivePokemon bracer, Boolean fullHealth)
		{
			return true;
		}

		public String braceMessage(ActivePokemon bracer)
		{
			return bracer.getName() + " endured the hit!";
		}
	}

	private static class Confusion extends PokemonEffect implements PassableEffect, BeforeTurnEffect
	{
		private static final long serialVersionUID = 1L;
		private int turns;

		public Confusion()
		{
			super("Confusion", -1, -1, false);
		}

		public Confusion newInstance()
		{
			Confusion x = (Confusion)(new Confusion().activate());
			x.turns = (int)(Math.random()*4) + 1; // Between 1 and 4 turns
			return x;
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !((victim.hasAbility("Own Tempo") && !caster.breaksTheMold()) || victim.hasEffect("Confusion"));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			super.cast(b, caster, victim, source, printCast);
			if (victim.isHoldingItem(b, "Persim Berry"))
			{
				b.addMessage(victim.getName() + "'s Persim Berry snapped it out of confusion!");
				victim.getAttributes().removeEffect("Confusion");
				victim.consumeItem(b);
			}
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return victim.getName() + " became confused!";
		}

		public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (victim.hasEffect("Confusion")) return victim.getName() + " is already confused!";
			if (victim.hasAbility("Own Tempo")) return victim.getName() + "'s Own Tempo prevents confusion!";
			return super.getFailMessage(b, user, victim);
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b)
		{
			if (turns == 0)
			{
				b.addMessage(p.getName() + " snapped out of its confusion!");
				super.active = false;
				return true;
			}
			turns--;
			b.addMessage(p.getName() + " is confused!");
			if (Math.random()*100 < 50)
			{
				b.addMessage("It hurt itself in confusion!");
				Move temp = p.getMove();
				p.setMove(new Move(Attack.getAttack("ConfusionDamage")));
				b.applyDamage(p, b.damageCalc(p, p));
				p.setMove(temp);
				return false;
			}
			return true;
		}
	}

	private static class SelfConfusion extends PokemonEffect implements ForceMoveEffect
	{
		private static final long serialVersionUID = 1L;
		private Move move;

		public SelfConfusion()
		{
			super("SelfConfusion", 2, 3, false);
		}

		public SelfConfusion newInstance()
		{
			SelfConfusion x = (SelfConfusion)(new SelfConfusion().activate());
			x.move = move;
			return x;
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("SelfConfusion"));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			move = caster.getMove();
			super.cast(b, caster, victim, source, printCast);
		}

		public void subside(Battle b, ActivePokemon p)
		{
			Confusion c = new Confusion();
			if (c.applies(b, p, p, CastSource.EFFECT))
			{
				b.addMessage(p.getName() + " became confused due to fatigue!");
				p.addEffect(c);
			}
		}

		public Move getMove()
		{
			return move;
		}
	}

	private static class Safeguard extends PokemonEffect implements StatusPreventionEffect, DefogRelease
	{
		private static final long serialVersionUID = 1L;

		public Safeguard()
		{
			super("Safeguard", 5, 5, false);
		}

		public Safeguard newInstance()
		{
			return (Safeguard)(new Safeguard().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("Safeguard"));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getName() + " is covered by a veil!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return "The effects of " + victim.getName() + "'s Safeguard faded.";
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status)
		{
			return !caster.hasAbility("Infiltrator");
		}

		public String preventionMessage(ActivePokemon victim)
		{
			return "Safeguard protects " + victim.getName() + " from status conditions!";
		}

		public String getDefogReleaseMessage(ActivePokemon victim)
		{
			return "The effects of " + victim.getName() + "'s Safeguard faded.";
		}
	}

	private static class GuardSpecial extends PokemonEffect implements StatusPreventionEffect
	{
		private static final long serialVersionUID = 1L;

		public GuardSpecial()
		{
			super("GuardSpecial", 5, 5, false);
		}

		public GuardSpecial newInstance()
		{
			return (GuardSpecial)(new GuardSpecial().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("GuardSpecial"));
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return "The effects of " + victim.getName() + "'s Guard Special faded.";
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status)
		{
			return !caster.hasAbility("Infiltrator");
		}

		public String preventionMessage(ActivePokemon victim)
		{
			return "Guard Special protects " + victim.getName() + " from status conditions!";
		}
	}

	private static class Encore extends PokemonEffect implements AttackSelectionEffect, ForceMoveEffect, BeforeTurnEffect, EndTurnEffect
	{
		private static final long serialVersionUID = 1L;
		private Move move;

		public Encore()
		{
			super("Encore", 3, 3, false);
		}

		public Encore newInstance()
		{
			Encore x = (Encore)(new Encore().activate());
			x.move = move;
			return x;
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.getAttributes().getLastMoveUsed() == null || victim.getAttributes().getLastMoveUsed().getPP() == 0 || victim.getAttributes().getLastMoveUsed().getAttack().isMoveType(MoveType.ENCORELESS) || victim.hasEffect("Encore"));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			move = victim.getAttributes().getLastMoveUsed();
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return victim.getName() + " got an encore!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return "The effects of " + victim.getName() + "'s encore faded.";
		}

		public boolean usable(ActivePokemon p, Move m)
		{
			return move.getAttack().getName().equals(m.getAttack().getName());
		}

		public String getUnusableMessage(ActivePokemon p)
		{
			return "Only " + move.getAttack().getName() + " can be used right now!";
		}

		public Move getMove()
		{
			return move;
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b)
		{
			if (!p.getAttack().getName().equals(move.getAttack().getName()))
			{
				b.printAttacking(p);
				b.addMessage(this.getFailMessage(b, p, opp));
				return false;
			}
			return true;
		}

		public void apply(ActivePokemon victim, Battle b)
		{
			if (move.getPP() == 0) active = false; // If the move runs out of PP, Encore immediately ends
		}
	}

	private static class Disable extends PokemonEffect implements AttackSelectionEffect, MoveCondition, BeforeTurnEffect
	{
		private static final long serialVersionUID = 1L;
		private Move disabled;
		private int turns;

		public Disable()
		{
			super("Disable", -1, -1, false);
		}

		public Disable newInstance()
		{
			Disable x = (Disable)(new Disable().activate());
			x.disabled = disabled;
			x.turns = (int)(Math.random()*4) + 4; // Between 4 and 7 turns
			return x;
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.getAttributes().getLastMoveUsed() == null || victim.getAttributes().getLastMoveUsed().getPP() == 0 || victim.hasEffect("Disable"));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			disabled = victim.getAttributes().getLastMoveUsed();
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return victim.getName() + "'s " + disabled.getAttack().getName() + " was disabled!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return victim.getName() + "'s " + disabled.getAttack().getName() + " is no longer disabled!";
		}

		public boolean shouldSubside(Battle b, ActivePokemon victim)
		{
			return turns == 0;
		}

		public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (victim.hasEffect("Disable")) return victim.getName() + " is already disabled!";
			return super.getFailMessage(b, user, victim);
		}

		public boolean usable(ActivePokemon p, Move m)
		{
			return !disabled.getAttack().getName().equals(m.getAttack().getName());
		}

		public String getUnusableMessage(ActivePokemon p)
		{
			return disabled.getAttack().getName() + " is disabled!";
		}

		public Move getMove()
		{
			return disabled;
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b)
		{
			turns--;
			if (p.getAttack().getName().equals(disabled.getAttack().getName()))
			{
				b.printAttacking(p);
				b.addMessage(p.getAttack().getName() + " is disabled!");
				return false;
			}
			return true;
		}
	}

	private static class RaiseCrits extends PokemonEffect implements CritStageEffect, PassableEffect
	{
		private static final long serialVersionUID = 1L;
		private boolean focusEnergy;
		private boolean direHit;
		private boolean berrylicious;

		public RaiseCrits()
		{
			super("RaiseCrits", -1, -1, false);
		}

		public RaiseCrits newInstance()
		{
			RaiseCrits x = (RaiseCrits)(new RaiseCrits().activate());
			x.focusEnergy = false;
			x.direHit = false;
			x.berrylicious = false;
			return x;
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(source == CastSource.USE_ITEM && victim.hasEffect("RaiseCrits") && ((RaiseCrits)victim.getEffect("RaiseCrits")).direHit);
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			if (!victim.hasEffect("RaiseCrits")) super.cast(b, caster, victim, source, printCast);
			else if (printCast) b.addMessage(getCastMessage(b, caster, victim));
			
			RaiseCrits critsies = (RaiseCrits)victim.getEffect("RaiseCrits");
			switch (source)
			{
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

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return victim.getName() + " is getting pumped!";
		}

		public int increaseCritStage(Integer stage, ActivePokemon p)
		{
			int critStage = 0;
			
			if (focusEnergy)
			{
				critStage++;
			}
			
			if (direHit)
			{
				critStage++;
			}
			
			if (berrylicious)
			{
				critStage++;
			}
			
			if (critStage == 0)
			{
				Global.error("RaiseCrits effect is not actually raising crits.");
			}
			
			return critStage + stage;
		}
	}

	private static class ChangeItem extends PokemonEffect implements ItemCondition
	{
		private static final long serialVersionUID = 1L;
		private Item item;

		public ChangeItem()
		{
			super("ChangeItem", -1, -1, false);
		}

		public ChangeItem newInstance()
		{
			ChangeItem x = (ChangeItem)(new ChangeItem().activate());
			x.item = item;
			return x;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			item = ((ItemCondition)source.getSource(b, caster)).getItem();
			while (victim.getAttributes().removeEffect("ChangeItem"));
			super.cast(b, caster, victim, source, printCast);
		}

		public Item getItem()
		{
			return item;
		}
	}

	private static class ChangeType extends PokemonEffect implements TypeCondition
	{
		private static final long serialVersionUID = 1L;
		private Type[] type;

		public ChangeType()
		{
			super("ChangeType", -1, -1, false);
		}

		public ChangeType newInstance()
		{
			ChangeType x = (ChangeType)(new ChangeType().activate());
			x.type = type;
			return x;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			type = ((ChangeTypeMove)source.getSource(b, caster)).getType(b, caster, victim);
			while (victim.getAttributes().removeEffect("ChangeType"));
			super.cast(b, caster, victim, source, printCast);
			b.addMessage("", type, victim.user());
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return victim.getName() + " was changed to " + type[0].getName() + (type[1] == Type.NONE ? "" : "/" + type[1].getName()) + " type!!";
		}

		public void subside(Battle b, ActivePokemon p)
		{
			b.addMessage("", p.getType(b), p.user());
		}

		public Type[] getType()
		{
			return type;
		}
	}

	private static class ChangeAbility extends PokemonEffect implements AbilityCondition
	{
		private static final long serialVersionUID = 1L;
		private Ability ability;
		private String message;

		public ChangeAbility()
		{
			super("ChangeAbility", -1, -1, false);
		}

		public ChangeAbility newInstance()
		{
			ChangeAbility x = (ChangeAbility)(new ChangeAbility().activate());
			x.ability = ability;
			x.message = message;
			return x;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			ChangeAbilityMove changey = (ChangeAbilityMove)source.getSource(b, caster);
			ability = changey.getAbility(b, caster, victim);
			message = changey.getMessage(b, caster, victim);
			
			while (victim.getAttributes().removeEffect("ChangeAbility"));
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return message;
		}

		public Ability getAbility()
		{
			return ability;
		}
	}

	private static class Stockpile extends PokemonEffect implements EndTurnEffect, StageChangingEffect
	{
		private static final long serialVersionUID = 1L;
		private int turns;

		public Stockpile()
		{
			super("Stockpile", -1, -1, false);
		}

		public Stockpile newInstance()
		{
			Stockpile x = (Stockpile)(new Stockpile().activate());
			x.turns = 0;
			return x;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			if (!victim.hasEffect("Stockpile"))
			{
				super.cast(b, caster, victim, source, printCast);
			}
			
			Stockpile stockpile = (Stockpile)victim.getEffect("Stockpile");
			if (stockpile.turns < 3)
			{
				b.addMessage(victim.getName() + " Defense and Special Defense were raised!");
				stockpile.turns++;
				return;
			}
			
			b.addMessage(this.getFailMessage(b, caster, victim));
		}

		public void subside(Battle b, ActivePokemon p)
		{
			b.addMessage("The effects of " + p.getName() + "'s Stockpile ended!");
			b.addMessage(p.getName() + "'s Defense and Special Defense decreased!");
		}

		public int getTurns()
		{
			return turns;
		}

		public void apply(ActivePokemon victim, Battle b)
		{
			if (!victim.getAttributes().getLastMoveUsed().getAttack().getName().equals("Stockpile")) active = false;
		}

		public int adjustStage(Integer stage, Stat s, ActivePokemon p, ActivePokemon opp, Battle b)
		{
			return s == Stat.DEFENSE || s == Stat.SP_DEFENSE ? stage + turns : stage;
		}
	}

	private static class UsedDefenseCurl extends PokemonEffect 
	{
		private static final long serialVersionUID = 1L;

		public UsedDefenseCurl()
		{
			super("UsedDefenseCurl", -1, -1, false);
		}

		public UsedDefenseCurl newInstance()
		{
			return (UsedDefenseCurl)(new UsedDefenseCurl().activate());
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			if (!victim.hasEffect("UsedDefenseCurl")) super.cast(b, caster, victim, source, printCast);
		}
	}

	private static class UsedMinimize extends PokemonEffect 
	{
		private static final long serialVersionUID = 1L;

		public UsedMinimize()
		{
			super("UsedMinimize", -1, -1, false);
		}

		public UsedMinimize newInstance()
		{
			return (UsedMinimize)(new UsedMinimize().activate());
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			if (!victim.hasEffect("UsedMinimize")) super.cast(b, caster, victim, source, printCast);
		}
	}

	private static class Mimic extends PokemonEffect implements MoveListCondition
	{
		private static final long serialVersionUID = 1L;
		private Move mimicMove;

		public Mimic()
		{
			super("Mimic", -1, -1, false);
		}

		public Mimic newInstance()
		{
			Mimic x = (Mimic)(new Mimic().activate());
			x.mimicMove = mimicMove;
			return x;
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("Mimic"));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			ActivePokemon other = b.getOtherPokemon(victim.user());
			Attack m = other.getAttributes().getLastMoveUsed() == null ? null : other.getAttributes().getLastMoveUsed().getAttack();
			
			if (m == null || victim.hasMove(m.getName()) || m.isMoveType(MoveType.MIMICLESS))
			{
				b.addMessage(this.getFailMessage(b, caster, victim));
				return;
			}
			
			mimicMove = new Move(m);
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return victim.getName() + " learned " + mimicMove.getAttack().getName() + "!";
		}

		public List<Move> getMoveList(ActivePokemon p, List<Move> moves)
		{
			List<Move> list = new ArrayList<>();
			for (Move m : moves)
			{
				if (m.getAttack().getName().equals("Mimic")) list.add(mimicMove);
				else list.add(m);
			}
			return list;
		}
	}

	private static class Imprison extends PokemonEffect implements AttackSelectionEffect, BeforeTurnEffect
	{
		private static final long serialVersionUID = 1L;
		private List<String> unableMoves;

		public Imprison()
		{
			super("Imprison", -1, -1, false);
		}

		public Imprison newInstance()
		{
			Imprison x = (Imprison)(new Imprison().activate());
			x.unableMoves = unableMoves;
			return x;
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("Imprison"));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			unableMoves = new ArrayList<>();
			for (Move m : caster.getMoves()) unableMoves.add(m.getAttack().getName());
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getName() + " sealed " + victim.getName() + "'s moves!";
		}

		public boolean usable(ActivePokemon p, Move m)
		{
			return !unableMoves.contains(m.getAttack().getName());
		}

		public String getUnusableMessage(ActivePokemon p)
		{
			return "No!! You are imprisoned!!!";
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b)
		{
			if (unableMoves.contains(p.getAttack().getName()))
			{
				b.printAttacking(p);
				b.addMessage(this.getFailMessage(b, p, opp));
				return false;
			}
			return true;
		}
	}

	private static class Trapped extends PokemonEffect implements TrappingEffect
	{
		private static final long serialVersionUID = 1L;

		public Trapped()
		{
			super("Trapped", -1, -1, false);
		}

		public Trapped newInstance()
		{
			return (Trapped)(new Trapped().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("Trapped"));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getName() + " can't escape!";
		}
	}

	private static class Foresight extends PokemonEffect 
	{
		private static final long serialVersionUID = 1L;

		public Foresight()
		{
			super("Foresight", -1, -1, false);
		}

		public Foresight newInstance()
		{
			return (Foresight)(new Foresight().activate());
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			if (!victim.hasEffect("Foresight")) super.cast(b, caster, victim, source, printCast);
			else b.addMessage(getCastMessage(b, caster, victim));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getName() + " identified " + victim.getName() + "!";
		}
	}

	private static class MiracleEye extends PokemonEffect 
	{
		private static final long serialVersionUID = 1L;

		public MiracleEye()
		{
			super("MiracleEye", -1, -1, false);
		}

		public MiracleEye newInstance()
		{
			return (MiracleEye)(new MiracleEye().activate());
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			if (!victim.hasEffect("MiracleEye")) super.cast(b, caster, victim, source, printCast);
			else b.addMessage(getCastMessage(b, caster, victim));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getName() + " identified " + victim.getName() + "!";
		}
	}

	private static class Torment extends PokemonEffect implements AttackSelectionEffect, BeforeTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public Torment()
		{
			super("Torment", -1, -1, false);
		}

		public Torment newInstance()
		{
			return (Torment)(new Torment().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("Torment"));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getName() + " tormented " + victim.getName() + "!";
		}

		public boolean usable(ActivePokemon p, Move m)
		{
			return (p.getAttributes().getLastMoveUsed() == null || !p.getAttributes().getLastMoveUsed().getAttack().getName().equals(m.getAttack().getName()));
		}

		public String getUnusableMessage(ActivePokemon p)
		{
			return p.getName() + " cannot use the same move twice in a row!";
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b)
		{
			if (!usable(p, p.getMove()))
			{
				b.printAttacking(p);
				b.addMessage(this.getFailMessage(b, p, opp));
				return false;
			}
			return true;
		}
	}

	private static class Taunt extends PokemonEffect implements AttackSelectionEffect, BeforeTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public Taunt()
		{
			super("Taunt", 3, 3, false);
		}

		public Taunt newInstance()
		{
			return (Taunt)(new Taunt().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("Taunt"));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return victim.getName() + " fell for the taunt!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return "The effects of the taunt wore off.";
		}

		public boolean usable(ActivePokemon p, Move m)
		{
			return m.getAttack().getCategory() != Attack.Category.STATUS;
		}

		public String getUnusableMessage(ActivePokemon p)
		{
			return "No!! Not while you're under the effects of taunt!!";
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b)
		{
			if (!usable(p, p.getMove()))
			{
				b.printAttacking(p);
				b.addMessage(this.getFailMessage(b, p, opp));
				return false;
			}
			return true;
		}
	}

	private static class LockOn extends PokemonEffect implements PassableEffect
	{
		private static final long serialVersionUID = 1L;

		public LockOn()
		{
			super("LockOn", 2, 2, false);
		}

		public LockOn newInstance()
		{
			return (LockOn)(new LockOn().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("LockOn"));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getName() + " took aim!";
		}
	}

	private static class Telekinesis extends PokemonEffect implements LevitationEffect
	{
		private static final long serialVersionUID = 1L;

		public Telekinesis()
		{
			super("Telekinesis", 3, 3, false);
		}

		public Telekinesis newInstance()
		{
			return (Telekinesis)(new Telekinesis().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("Ingrain") || victim.hasEffect("Telekinesis"));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return victim.getName() + " was levitated due to " + user.getName() + "'s telekinesis!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return victim.getName() + " is no longer under the effects of telekinesis.";
		}
	}

	private static class Ingrain extends PokemonEffect implements TrappingEffect, EndTurnEffect, GroundedEffect, PassableEffect
	{
		private static final long serialVersionUID = 1L;

		public Ingrain()
		{
			super("Ingrain", -1, -1, false);
		}

		public Ingrain newInstance()
		{
			return (Ingrain)(new Ingrain().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("Ingrain"));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			super.cast(b, caster, victim, source, printCast);
			if (victim.hasEffect("MagnetRise"))
			{
				Effect.removeEffect(victim.getEffects(), "MagnetRise");
				b.addMessage("The effects of " + victim.getName() + "'s magnet rise were cancelled due to ingrain!");
			}
			if (victim.hasEffect("Telekinesis"))
			{
				Effect.removeEffect(victim.getEffects(), "Telekinesis");
				b.addMessage("The effects of telekinesis were cancelled due to ingrain!");
			}
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return victim.getName() + " planted its roots!";
		}

		public void apply(ActivePokemon victim, Battle b)
		{
			if (victim.fullHealth() || victim.hasEffect("HealBlock")) return;
			int healAmount = victim.healHealthFraction(1/16.0);
			if (victim.isHoldingItem(b, "Big Root")) victim.heal((int)(healAmount*.3));
			b.addMessage(victim.getName() + " restored some HP due to ingrain!", victim.getHP(), victim.user());
		}
	}

	private static class Grounded extends PokemonEffect implements GroundedEffect
	{
		private static final long serialVersionUID = 1L;

		public Grounded()
		{
			super("Grounded", -1, -1, false);
		}

		public Grounded newInstance()
		{
			return (Grounded)(new Grounded().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("Grounded"));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			super.cast(b, caster, victim, source, printCast);
			if (victim.isSemiInvulnerable() && (victim.getAttack().getName().equals("Fly") || victim.getAttack().getName().equals("Bounce")))
			{
				victim.getMove().switchReady(b);
				b.addMessage(victim.getName() + " fell to the ground!");
			}
			if (victim.hasEffect("MagnetRise"))
			{
				Effect.removeEffect(victim.getEffects(), "MagnetRise");
				b.addMessage("The effects of " + victim.getName() + "'s magnet rise was cancelled!");
			}
			if (victim.hasEffect("Telekinesis"))
			{
				Effect.removeEffect(victim.getEffects(), "Telekinesis");
				b.addMessage("The effects of telekinesis were cancelled!");
			}
		}
	}

	private static class Curse extends PokemonEffect implements EndTurnEffect, PassableEffect
	{
		private static final long serialVersionUID = 1L;

		public Curse()
		{
			super("Curse", -1, -1, false);
		}

		public Curse newInstance()
		{
			return (Curse)(new Curse().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("Curse"));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			super.cast(b, caster, victim, source, printCast);
			caster.reduceHealthFraction(b, 1/2.0);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getName() + " cut its own HP and put a curse on " + victim.getName() + "!";
		}

		public void apply(ActivePokemon victim, Battle b)
		{
			if (victim.hasAbility("Magic Guard")) return;
			b.addMessage(victim.getName() + " was hurt by the curse!");
			victim.reduceHealthFraction(b, 1/4.0);
		}
	}

	private static class Yawn extends PokemonEffect 
	{
		private static final long serialVersionUID = 1L;

		public Yawn()
		{
			super("Yawn", 2, 2, false);
		}

		public Yawn newInstance()
		{
			return (Yawn)(new Yawn().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(!Status.applies(StatusCondition.ASLEEP, b, caster, victim) || victim.hasEffect("Yawn"));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return victim.getName() + " grew drowsy!";
		}

		public void subside(Battle b, ActivePokemon p)
		{
			Status.giveStatus(b, b.getOtherPokemon(p.user()), p, StatusCondition.ASLEEP);
		}
	}

	private static class MagnetRise extends PokemonEffect implements LevitationEffect, PassableEffect
	{
		private static final long serialVersionUID = 1L;

		public MagnetRise()
		{
			super("MagnetRise", 5, 5, false);
		}

		public MagnetRise newInstance()
		{
			return (MagnetRise)(new MagnetRise().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("Ingrain") || victim.hasEffect("MagnetRise"));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getName() + " levitated with electromagnetism!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return victim.getName() + " is no longer under the effects of magnet rise.";
		}
	}

	private static class Uproar extends PokemonEffect implements ForceMoveEffect, AttackSelectionEffect, EndTurnEffect
	{
		private static final long serialVersionUID = 1L;
		private Move uproar;

		public Uproar()
		{
			super("Uproar", 3, 3, false);
		}

		public Uproar newInstance()
		{
			Uproar x = (Uproar)(new Uproar().activate());
			x.uproar = uproar;
			return x;
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("Uproar"));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			uproar = victim.getMove();
			super.cast(b, caster, victim, source, printCast);
			ActivePokemon theOtherPokemon = b.getOtherPokemon(victim.user());
			if (victim.hasStatus(StatusCondition.ASLEEP))
			{
				b.addMessage("The uproar woke up " + victim.getName() + "!", StatusCondition.NONE, victim.user());
				victim.removeStatus();
			}
			if (theOtherPokemon.hasStatus(StatusCondition.ASLEEP))
			{
				b.addMessage("The uproar woke up " + theOtherPokemon.getName() + "!", StatusCondition.NONE, theOtherPokemon.user());
				theOtherPokemon.removeStatus();
			}
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getName() + " started an uproar!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return "The uproar ended.";
		}

		public Move getMove()
		{
			return uproar;
		}

		public boolean usable(ActivePokemon p, Move m)
		{
			return m.getAttack().getName().equals("Uproar");
		}

		public String getUnusableMessage(ActivePokemon p)
		{
			return "Only Uproar can be used right now!";
		}

		public void apply(ActivePokemon victim, Battle b)
		{
			if (uproar.getPP() == 0) active = false; // If uproar runs out of PP, the effect immediately ends
		}
	}

	private static class AquaRing extends PokemonEffect implements PassableEffect, EndTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public AquaRing()
		{
			super("AquaRing", -1, -1, false);
		}

		public AquaRing newInstance()
		{
			return (AquaRing)(new AquaRing().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("AquaRing"));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getName() + " surrounded itself with a veil of water!";
		}

		public void apply(ActivePokemon victim, Battle b)
		{
			if (victim.fullHealth() || victim.hasEffect("HealBlock")) return;
			int healAmount = victim.healHealthFraction(1/16.0);
			if (victim.isHoldingItem(b, "Big Root")) victim.heal((int)(healAmount*.3));
			b.addMessage(victim.getName() + " restored some HP due to aqua ring!", victim.getHP(), victim.user());
		}
	}

	private static class Nightmare extends PokemonEffect implements EndTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public Nightmare()
		{
			super("Nightmare", -1, -1, false);
		}

		public Nightmare newInstance()
		{
			return (Nightmare)(new Nightmare().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(!victim.hasStatus(StatusCondition.ASLEEP) || victim.hasEffect("Nightmare"));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return victim.getName() + " began having a nightmare!";
		}

		public boolean shouldSubside(Battle b, ActivePokemon victim)
		{
			return !victim.hasStatus(StatusCondition.ASLEEP);
		}

		public void apply(ActivePokemon victim, Battle b)
		{
			if (victim.hasAbility("Magic Guard")) return;
			b.addMessage(victim.getName() + " was hurt by its nightmare!");
			victim.reduceHealthFraction(b, 1/4.0);
		}
	}

	private static class Charge extends PokemonEffect implements PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public Charge()
		{
			super("Charge", 2, 2, false);
		}

		public Charge newInstance()
		{
			return (Charge)(new Charge().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("Charge"));
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getAttack().getType(b, user) == Type.ELECTRIC ? 2 : 1;
		}
	}

	private static class Focusing extends PokemonEffect 
	{
		private static final long serialVersionUID = 1L;

		public Focusing()
		{
			super("Focusing", 1, 1, false);
		}

		public Focusing newInstance()
		{
			return (Focusing)(new Focusing().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("Focusing"));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getName() + " began tightening its focus!";
		}
	}

	private static class FiddyPercentStronger extends PokemonEffect implements PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public FiddyPercentStronger()
		{
			super("FiddyPercentStronger", 1, 1, false);
		}

		public FiddyPercentStronger newInstance()
		{
			return (FiddyPercentStronger)(new FiddyPercentStronger().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("FiddyPercentStronger"));
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return 1.5;
		}
	}

	private static class Transformed extends PokemonEffect implements MoveListCondition, StatsCondition, TypeCondition
	{
		private static final long serialVersionUID = 1L;
		private List<Move> moveList;
		private int[] stats;
		private Type[] type;

		public Transformed()
		{
			super("Transformed", -1, -1, false);
		}

		public Transformed newInstance()
		{
			Transformed x = (Transformed)(new Transformed().activate());
			x.moveList = moveList;
			x.stats = stats;
			x.type = type;
			return x;
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(b.getOtherPokemon(victim.user()).hasEffect("Transformed") || victim.hasEffect("Transformed"));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			ActivePokemon transformee = b.getOtherPokemon(victim.user());
			stats = new int[Stat.NUM_STATS];
			for (int i = 0; i < stats.length; i++)
			{
				stats[i] = Stat.getStat(i, victim.getLevel(), transformee.getPokemonInfo().getStat(i), victim.getIV(i), victim.getEV(i), victim.getNature().getNatureVal(i));
			}
			stats[Stat.HP.index()] = victim.getStat(Stat.HP);
			moveList = new ArrayList<>();
			for (Move m : transformee.getMoves()) moveList.add(new Move(m.getAttack(), 5));
			for (int i = 0; i < Stat.NUM_BATTLE_STATS; i++) victim.getAttributes().setStage(i, transformee.getStage(i));
			type = transformee.getPokemonInfo().getType();
			super.cast(b, caster, victim, source, printCast);
			b.addMessage("", transformee.getPokemonInfo(), transformee.isShiny(), true, victim.user());
			b.addMessage("", victim.getType(b), victim.user());
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return victim.getName() + " transformed into " + b.getOtherPokemon(victim.user()).getPokemonInfo().getName() + "!";
		}

		public List<Move> getMoveList(ActivePokemon p, List<Move> moves)
		{
			PokemonEffect mimic = p.getEffect("Mimic");
			if (mimic != null) return ((MoveListCondition)mimic).getMoveList(p, moveList);
			return moveList;
		}

		public int getStat(Stat stat)
		{
			return stats[stat.index()];
		}

		public Type[] getType()
		{
			return type;
		}
	}

	private static class Substitute extends PokemonEffect implements IntegerCondition, PassableEffect, EffectBlockerEffect
	{
		private static final long serialVersionUID = 1L;
		private int hp;

		public Substitute()
		{
			super("Substitute", -1, -1, false);
		}

		public Substitute newInstance()
		{
			Substitute x = (Substitute)(new Substitute().activate());
			x.hp = hp;
			return x;
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.getHPRatio() <= .25 || victim.getStat(Stat.HP) <= 3 || victim.hasEffect("Substitute"));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			hp = victim.reduceHealthFraction(b, .25) + 1;
			super.cast(b, caster, victim, source, printCast);
			b.addMessage("", victim.getHP(), victim.user());
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return victim.getName() + " put in a substitute!";
		}

		public int getAmount()
		{
			return hp;
		}

		public void decrease(int amount)
		{
			hp -= amount;
		}

		public void increase(int amount)
		{
			hp += amount;
		}

		public boolean validMove(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isSelfTarget() || user.getAttack().isMoveType(MoveType.FIELD) || user.getAttack().isMoveType(MoveType.SUBSTITUTE_PIERCING))
			{
				return true;
			}
			
			if (user.getAttack().getCategory() == Category.STATUS)
			{
				b.addMessage(this.getFailMessage(b, user, victim));
			}
			
			return false;
		}
	}

	private static class Mist extends PokemonEffect implements StatProtectingEffect, DefogRelease
	{
		private static final long serialVersionUID = 1L;

		public Mist()
		{
			super("Mist", 5, 5, false);
		}

		public Mist newInstance()
		{
			return (Mist)(new Mist().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("Mist"));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getName() + " shrouded itself in mist!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return "The mist faded.";
		}

		public boolean prevent(ActivePokemon caster, Stat stat)
		{
			return !caster.hasAbility("Infiltrator");
		}

		public String preventionMessage(ActivePokemon p)
		{
			return "The mist prevents stat reductions!";
		}

		public String getDefogReleaseMessage(ActivePokemon victim)
		{
			return "The mist faded.";
		}
	}

	private static class MagicCoat extends PokemonEffect 
	{
		private static final long serialVersionUID = 1L;

		public MagicCoat()
		{
			super("MagicCoat", 1, 1, false);
		}

		public MagicCoat newInstance()
		{
			return (MagicCoat)(new MagicCoat().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("MagicCoat"));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getName() + " shrouded itself with a magic coat!";
		}
	}

	private static class Bide extends PokemonEffect implements ForceMoveEffect, EndTurnEffect, IntegerCondition
	{
		private static final long serialVersionUID = 1L;
		private Move move;
		private int turns;
		private int damage;

		public Bide()
		{
			super("Bide", -1, -1, false);
		}

		public Bide newInstance()
		{
			Bide x = (Bide)(new Bide().activate());
			x.move = move;
			x.turns = 1;
			x.damage = 0;
			return x;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			Bide e = (Bide)victim.getEffect("Bide");
			if (e == null)
			{
				move = caster.getMove();
				super.cast(b, caster, victim, source, printCast);
				return;
			}
			
			if (e.turns > 0)
			{
				e.turns--;
				b.addMessage(getCastMessage(b, caster, victim));
				return;
			}
			
			b.addMessage(victim.getName() + " released energy!");
			if (e.damage == 0)
			{
				b.addMessage(this.getFailMessage(b, caster, victim));
			}
			else
			{
				b.applyDamage(b.getOtherPokemon(victim.user()), 2*e.damage);
			}
			
			victim.getAttributes().removeEffect("Bide");
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getName() + " is storing energy!";
		}

		public int getTurns()
		{
			return turns;
		}

		public Move getMove()
		{
			return move;
		}

		public void apply(ActivePokemon victim, Battle b)
		{
			increase(victim.getAttributes().getDamageTaken());
		}

		public int getAmount()
		{
			return damage;
		}

		public void decrease(int amount)
		{
			damage -= amount;
		}

		public void increase(int amount)
		{
			damage += amount;
		}
	}

	private static class HalfWeight extends PokemonEffect implements IntegerCondition
	{
		private static final long serialVersionUID = 1L;
		private int layers;

		public HalfWeight()
		{
			super("HalfWeight", -1, -1, false);
		}

		public HalfWeight newInstance()
		{
			HalfWeight x = (HalfWeight)(new HalfWeight().activate());
			x.layers = 1;
			return x;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			HalfWeight e = (HalfWeight)victim.getEffect("HalfWeight");
			if (e == null) super.cast(b, caster, victim, source, printCast);
			else e.layers++;
		}

		public int getAmount()
		{
			return layers;
		}

		public void decrease(int amount)
		{
			layers -= amount;
		}

		public void increase(int amount)
		{
			layers += amount;
		}
	}

	private static class PowerTrick extends PokemonEffect implements PassableEffect, StatSwitchingEffect
	{
		private static final long serialVersionUID = 1L;

		public PowerTrick()
		{
			super("PowerTrick", -1, -1, false);
		}

		public PowerTrick newInstance()
		{
			return (PowerTrick)(new PowerTrick().activate());
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			PokemonEffect p = victim.getEffect("PowerTrick");
			if (p == null)
			{
				super.cast(b, caster, victim, source, printCast);
				return;
			}
			b.addMessage(getCastMessage(b, caster, victim));
			victim.getAttributes().removeEffect("PowerTrick");
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getName() + "'s attack and defense were swapped!";
		}

		public Stat switchStat(Stat s)
		{
			if (s == Stat.ATTACK) return Stat.DEFENSE;
			if (s == Stat.DEFENSE) return Stat.ATTACK;
			return s;
		}
	}

	private static class PowerSplit extends PokemonEffect implements StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public PowerSplit()
		{
			super("PowerSplit", -1, -1, false);
		}

		public PowerSplit newInstance()
		{
			return (PowerSplit)(new PowerSplit().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("PowerSplit"));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getName() + " split the power!";
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (s == Stat.ATTACK) return (p.getStat(Stat.ATTACK) + opp.getStat(Stat.ATTACK))/2;
			if (s == Stat.SP_ATTACK) return (p.getStat(Stat.SP_ATTACK) + opp.getStat(Stat.SP_ATTACK))/2;
			return stat;
		}
	}

	private static class GuardSplit extends PokemonEffect implements StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public GuardSplit()
		{
			super("GuardSplit", -1, -1, false);
		}

		public GuardSplit newInstance()
		{
			return (GuardSplit)(new GuardSplit().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("GuardSplit"));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getName() + " split the defense!";
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (s == Stat.DEFENSE) return (p.getStat(Stat.DEFENSE) + opp.getStat(Stat.DEFENSE))/2;
			if (s == Stat.SP_DEFENSE) return (p.getStat(Stat.SP_DEFENSE) + opp.getStat(Stat.SP_DEFENSE))/2;
			return stat;
		}
	}

	private static class HealBlock extends PokemonEffect 
	{
		private static final long serialVersionUID = 1L;

		public HealBlock()
		{
			super("HealBlock", 5, 5, false);
		}

		public HealBlock newInstance()
		{
			return (HealBlock)(new HealBlock().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("HealBlock"));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getName() + " blocked " + victim.getName() + " from healing!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return "The effects of heal block wore off.";
		}
	}

	private static class Infatuated extends PokemonEffect implements BeforeTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public Infatuated()
		{
			super("Infatuated", -1, -1, false);
		}

		public Infatuated newInstance()
		{
			return (Infatuated)(new Infatuated().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !((victim.hasAbility("Oblivious") && !caster.breaksTheMold()) || !Gender.oppositeGenders(caster, victim) || victim.hasEffect("Infatuated"));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			super.cast(b, caster, victim, source, printCast);
			if (victim.isHoldingItem(b, "Destiny Knot") && this.applies(b, victim, caster, CastSource.HELD_ITEM))
			{
				super.cast(b, victim, caster, CastSource.HELD_ITEM, false);
				b.addMessage(victim.getName() + "'s Destiny Knot caused " + caster.getName() + " to fall in love!");
			}
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return victim.getName() + " fell in love!";
		}

		public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (Gender.oppositeGenders(user, victim) && victim.hasAbility("Oblivious")) return victim.getName() + "'s Oblivious prevents infatuation!";
			return super.getFailMessage(b, user, victim);
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b)
		{
			b.addMessage(p.getName() + " is in love with " + opp.getName() + "!");
			if (Math.random() < .5) return true;
			b.addMessage(p.getName() + "'s infatuation kept it from attacking!");
			return false;
		}
	}

	private static class Snatch extends PokemonEffect 
	{
		private static final long serialVersionUID = 1L;

		public Snatch()
		{
			super("Snatch", 1, 1, false);
		}

		public Snatch newInstance()
		{
			return (Snatch)(new Snatch().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("Snatch"));
		}
	}

	private static class Grudge extends PokemonEffect implements FaintEffect
	{
		private static final long serialVersionUID = 1L;

		public Grudge()
		{
			super("Grudge", -1, -1, false);
		}

		public Grudge newInstance()
		{
			return (Grudge)(new Grudge().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("Grudge"));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return victim.getName() + " wants " + b.getOtherPokemon(victim.user()).getName() + " to bear a grudge!";
		}

		public void deathwish(Battle b, ActivePokemon dead, ActivePokemon murderer)
		{
			if (murderer.getAttributes().isAttacking())
			{
				b.addMessage(murderer.getName() + "'s " + murderer.getAttack().getName() + " lost all its PP due to its grudge!");
				murderer.getMove().reducePP(murderer.getMove().getPP());
			}
		}
	}

	private static class DestinyBond extends PokemonEffect implements FaintEffect
	{
		private static final long serialVersionUID = 1L;

		public DestinyBond()
		{
			super("DestinyBond", -1, -1, false);
		}

		public DestinyBond newInstance()
		{
			return (DestinyBond)(new DestinyBond().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("DestinyBond"));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return victim.getName() + " is trying to take " + b.getOtherPokemon(victim.user()).getName() + " down with it!";
		}

		public void deathwish(Battle b, ActivePokemon dead, ActivePokemon murderer)
		{
			if (murderer.getAttributes().isAttacking())
			{
				b.addMessage(dead.getName() + " took " + murderer.getName() + " down with it!");
				murderer.reduceHealthFraction(b, 1);
			}
		}
	}

	private static class PerishSong extends PokemonEffect implements PassableEffect, EndTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public PerishSong()
		{
			super("PerishSong", 3, 3, false);
		}

		public PerishSong newInstance()
		{
			return (PerishSong)(new PerishSong().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !((victim.hasAbility("Soundproof") && !caster.breaksTheMold()) || victim.hasEffect("PerishSong"));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return "All Pokemon hearing this song will faint in three turns!";
		}

		public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (victim.hasAbility("Soundproof")) return victim.getName() + "'s " + victim.getAbility().getName() + " makes it immune to sound based moves!";
			return super.getFailMessage(b, user, victim);
		}

		public void apply(ActivePokemon victim, Battle b)
		{
			b.addMessage(victim.getName() + "'s Perish Song count fell to " + (super.numTurns - 1) + "!");
			if (super.numTurns == 1) victim.reduceHealthFraction(b, 1);
		}
	}

	private static class Embargo extends PokemonEffect implements PassableEffect
	{
		private static final long serialVersionUID = 1L;

		public Embargo()
		{
			super("Embargo", 5, 5, false);
		}

		public Embargo newInstance()
		{
			return (Embargo)(new Embargo().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(victim.hasEffect("Embargo"));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return victim.getName() + " can't use items now!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return victim.getName() + " can use items again!";
		}
	}

	private static class ConsumedItem extends PokemonEffect implements ItemCondition
	{
		private static final long serialVersionUID = 1L;
		private Item consumed;

		public ConsumedItem()
		{
			super("ConsumedItem", -1, -1, false);
		}

		public ConsumedItem newInstance()
		{
			ConsumedItem x = (ConsumedItem)(new ConsumedItem().activate());
			x.consumed = consumed;
			return x;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			consumed = victim.getHeldItem(b);
			victim.removeItem();
			while (victim.getAttributes().removeEffect("ConsumedItem"));
			super.cast(b, caster, victim, source, printCast);
		}

		public Item getItem()
		{
			return consumed;
		}
	}
}
