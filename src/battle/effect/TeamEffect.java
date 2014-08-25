package battle.effect;

import java.io.Serializable;
import java.util.HashMap;

import main.Global;
import main.Type;
import pokemon.ActivePokemon;
import pokemon.Stat;
import trainer.Trainer;
import battle.Attack;
import battle.Battle;
import battle.Move;
import battle.effect.Status.StatusCondition;

// Class to handle effects that are specific to one side of the battle
public abstract class TeamEffect extends Effect implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static HashMap<String, TeamEffect> map;
	
	public TeamEffect(String s, int min, int max,boolean sub) 
	{
		super(s, min, max, sub);
	}

	public abstract TeamEffect newInstance();
	
	public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
	{
		if (printCast) b.addMessage(getCastMessage(b, caster, victim));
		b.getTrainer(victim.user()).addEffect(this);
	}

	public static TeamEffect getEffect(String e)
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

		// List all of the effects we are loading
		map.put("Reflect", new Reflect());
		map.put("LightScreen", new LightScreen());
		map.put("Tailwind", new Tailwind());
		map.put("StealthRock", new StealthRock());
		map.put("ToxicSpikes", new ToxicSpikes());
		map.put("Spikes", new Spikes());
		map.put("Wish", new Wish());
		map.put("LuckyChant", new LuckyChant());
		map.put("FutureSight", new FutureSight());
		map.put("DoomDesire", new DoomDesire());
		map.put("HealSwitch", new HealSwitch());
		map.put("DeadAlly", new DeadAlly());
		map.put("PayDay", new PayDay());
		map.put("DoubleMoney", new DoubleMoney());
	}

	/**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

	private static class Reflect extends TeamEffect implements StatChangingEffect, DefogRelease
	{
		private static final long serialVersionUID = 1L;
		public Reflect()
		{
			super("Reflect", 5, 5, false);
		}

		public Reflect newInstance()
		{
			return (Reflect)(new Reflect().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(Effect.hasEffect(b.getEffects(victim.user()), "Reflect"));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, "Light Clay")) Effect.getEffect(b.getEffects(victim.user()), "Reflect").setTurns(8);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getName() + " raised the defense of its team!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return "The effects of reflect faded.";
		}

		public int modify(int stat, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			return stat*(s == Stat.DEFENSE && !opp.hasAbility("Infiltrator") ? 2 : 1);
		}

		public String getDefogReleaseMessage(ActivePokemon victim)
		{
			return "The effects of reflect faded.";
		}
	}

	private static class LightScreen extends TeamEffect implements StatChangingEffect, DefogRelease
	{
		private static final long serialVersionUID = 1L;
		public LightScreen()
		{
			super("LightScreen", 5, 5, false);
		}

		public LightScreen newInstance()
		{
			return (LightScreen)(new LightScreen().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(Effect.hasEffect(b.getEffects(victim.user()), "LightScreen"));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, "Light Clay")) Effect.getEffect(b.getEffects(victim.user()), "LightScreen").setTurns(8);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getName() + " raised the special defense of its team!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return "The effects of light screen faded.";
		}

		public int modify(int stat, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			return stat*(s == Stat.SP_DEFENSE && !opp.hasAbility("Infiltrator") ? 2 : 1);
		}

		public String getDefogReleaseMessage(ActivePokemon victim)
		{
			return "The effects of light screen faded.";
		}
	}

	private static class Tailwind extends TeamEffect implements StatChangingEffect
	{
		private static final long serialVersionUID = 1L;
		public Tailwind()
		{
			super("Tailwind", 4, 4, false);
		}

		public Tailwind newInstance()
		{
			return (Tailwind)(new Tailwind().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(Effect.hasEffect(b.getEffects(victim.user()), "Tailwind"));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getName() + " raised the speed of its team!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return "The effects of tailwind faded.";
		}

		public int modify(int stat, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			return stat*(s == Stat.SPEED ? 2 : 1);
		}
	}

	private static class StealthRock extends TeamEffect implements EntryEffect, RapidSpinRelease, DefogRelease
	{
		private static final long serialVersionUID = 1L;
		public StealthRock()
		{
			super("StealthRock", -1, -1, false);
		}

		public StealthRock newInstance()
		{
			return (StealthRock)(new StealthRock().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(Effect.hasEffect(b.getEffects(victim.user()), "StealthRock"));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return "Floating rocks were scattered all around!";
		}

		public String getReleaseMessage(ActivePokemon user)
		{
			return "The floating rocks spun away!";
		}

		public void enter(Battle b, ActivePokemon victim)
		{
			if (victim.hasAbility("Magic Guard")) return;
			b.addMessage(victim.getName() + " was hurt by stealth rock!");
			victim.reduceHealthFraction(b, Type.getAdvantage(Type.ROCK, victim, b)/8.0);
		}

		public String getDefogReleaseMessage(ActivePokemon victim)
		{
			return "The floating rocks dispersed!";
		}
	}

	private static class ToxicSpikes extends TeamEffect implements EntryEffect, RapidSpinRelease, DefogRelease
	{
		private static final long serialVersionUID = 1L;
		private int layers;

		public ToxicSpikes()
		{
			super("ToxicSpikes", -1, -1, false);
		}

		public ToxicSpikes newInstance()
		{
			ToxicSpikes x = (ToxicSpikes)(new ToxicSpikes().activate());
			x.layers = 1;
			return x;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			Effect e = Effect.getEffect(b.getEffects(victim.user()), "ToxicSpikes");
			if (e == null)
			{
				super.cast(b, caster, victim, source, printCast);
				return;
			}
			((ToxicSpikes)e).layers++;
			b.addMessage(getCastMessage(b, caster, victim));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return "Toxic spikes were scattered all around!";
		}

		public String getReleaseMessage(ActivePokemon user)
		{
			return "The toxic spikes dispersed!";
		}

		public void enter(Battle b, ActivePokemon victim)
		{
			if (victim.isLevitating(b)) return;
			if (victim.isType(Type.POISON))
			{
				b.addMessage(victim.getName() + " absorbed the Toxic Spikes!");
				super.active = false;
				return;
			}
			ActivePokemon theOtherPokemon = b.getOtherPokemon(victim.user());
			if (Status.applies(StatusCondition.POISONED, b, theOtherPokemon, victim))
			{
				if (layers >= 2) PokemonEffect.getEffect("BadPoison").cast(b, theOtherPokemon, victim, CastSource.EFFECT, false);
				else Status.giveStatus(b, theOtherPokemon, victim, StatusCondition.POISONED);
			}
		}

		public String getDefogReleaseMessage(ActivePokemon victim)
		{
			return "The toxic spikes dispersed!";
		}
	}

	private static class Spikes extends TeamEffect implements EntryEffect, RapidSpinRelease, DefogRelease
	{
		private static final long serialVersionUID = 1L;
		private int layers;

		public Spikes()
		{
			super("Spikes", -1, -1, false);
		}

		public Spikes newInstance()
		{
			Spikes x = (Spikes)(new Spikes().activate());
			x.layers = 1;
			return x;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			Effect e = Effect.getEffect(b.getEffects(victim.user()), "Spikes");
			if (e == null)
			{
				super.cast(b, caster, victim, source, printCast);
				return;
			}
			((Spikes)e).layers++;
			b.addMessage(getCastMessage(b, caster, victim));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return "Spikes were scattered all around!";
		}

		public String getReleaseMessage(ActivePokemon user)
		{
			return "The spikes dispersed!";
		}

		public void enter(Battle b, ActivePokemon victim)
		{
			if (victim.isLevitating(b) || victim.hasAbility("Magic Guard")) return;
			b.addMessage(victim.getName() + " was hurt by spikes!");
			if (layers == 1) victim.reduceHealthFraction(b, 1/8.0);
			else if (layers == 2) victim.reduceHealthFraction(b, 1/6.0);
			else victim.reduceHealthFraction(b, 1/4.0);
		}

		public String getDefogReleaseMessage(ActivePokemon victim)
		{
			return "The spikes dispersed!";
		}
	}

	private static class Wish extends TeamEffect 
	{
		private static final long serialVersionUID = 1L;
		private String casterName;

		public Wish()
		{
			super("Wish", 1, 1, true);
		}

		public Wish newInstance()
		{
			Wish x = (Wish)(new Wish().activate());
			x.casterName = casterName;
			return x;
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(Effect.hasEffect(b.getEffects(victim.user()), "Wish"));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			casterName = caster.getName();
			super.cast(b, caster, victim, source, printCast);
		}

		public void subside(Battle b, ActivePokemon p)
		{
			if (p.hasEffect("HealBlock")) return;
			p.healHealthFraction(1/2.0);
			b.addMessage(casterName + "'s wish came true!", p.getHP(), p.user());
		}
	}

	private static class LuckyChant extends TeamEffect 
	{
		private static final long serialVersionUID = 1L;
		public LuckyChant()
		{
			super("LuckyChant", 5, 5, false);
		}

		public LuckyChant newInstance()
		{
			return (LuckyChant)(new LuckyChant().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(Effect.hasEffect(b.getEffects(victim.user()), "LuckyChant"));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return "The lucky chant shielded " + victim.getName() + "'s team from critical hits!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return "The effects of lucky chant wore off.";
		}
	}

	private static class FutureSight extends TeamEffect 
	{
		private static final long serialVersionUID = 1L;
		private ActivePokemon theSeeer;

		public FutureSight()
		{
			super("FutureSight", 2, 2, true);
		}

		public FutureSight newInstance()
		{
			FutureSight x = (FutureSight)(new FutureSight().activate());
			x.theSeeer = theSeeer;
			return x;
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(Effect.hasEffect(b.getEffects(victim.user()), "FutureSight"));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			theSeeer = caster;
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return theSeeer.getName() + " foresaw an attack!";
		}

		public void subside(Battle b, ActivePokemon p)
		{
			b.addMessage(p.getName() + " took " + theSeeer.getName() + "'s attack!");
			theSeeer.setMove(new Move(Attack.getAttack("Future Sight")));
			theSeeer.getAttack().applyDamage(theSeeer, p, b);
		}
	}

	private static class DoomDesire extends TeamEffect 
	{
		private static final long serialVersionUID = 1L;
		private ActivePokemon theSeeer;

		public DoomDesire()
		{
			super("DoomDesire", 2, 2, true);
		}

		public DoomDesire newInstance()
		{
			DoomDesire x = (DoomDesire)(new DoomDesire().activate());
			x.theSeeer = theSeeer;
			return x;
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(Effect.hasEffect(b.getEffects(victim.user()), "DoomDesire"));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			theSeeer = caster;
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return theSeeer.getName() + " foresaw an attack!";
		}

		public void subside(Battle b, ActivePokemon p)
		{
			b.addMessage(p.getName() + " took " + theSeeer.getName() + "'s attack!");
			theSeeer.setMove(new Move(Attack.getAttack("Doom Desire")));
			theSeeer.getAttack().applyDamage(theSeeer, p, b);
		}
	}

	private static class HealSwitch extends TeamEffect implements EntryEffect
	{
		private static final long serialVersionUID = 1L;
		private String wish;

		public HealSwitch()
		{
			super("HealSwitch", -1, -1, false);
		}

		public HealSwitch newInstance()
		{
			HealSwitch x = (HealSwitch)(new HealSwitch().activate());
			x.wish = wish;
			return x;
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(Effect.hasEffect(b.getEffects(victim.user()), "HealSwitch"));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			wish = caster.getAttack().getName().equals("Lunar Dance") ? "lunar dance" : "healing wish";
			super.cast(b, caster, victim, source, printCast);
		}

		public void enter(Battle b, ActivePokemon victim)
		{
			victim.healHealthFraction(1);
			victim.removeStatus();
			b.addMessage(victim.getName() + " health was restored due to the " + wish + "!", victim.getHP(), victim.user());
			b.addMessage("", StatusCondition.NONE, victim.user());
			super.active = false;
		}
	}

	private static class DeadAlly extends TeamEffect 
	{
		private static final long serialVersionUID = 1L;
		public DeadAlly()
		{
			super("DeadAlly", 2, 2, false);
		}

		public DeadAlly newInstance()
		{
			return (DeadAlly)(new DeadAlly().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(Effect.hasEffect(b.getEffects(victim.user()), "DeadAlly"));
		}
	}

	private static class PayDay extends TeamEffect implements EndBattleEffect
	{
		private static final long serialVersionUID = 1L;
		private int coins;

		public PayDay()
		{
			super("PayDay", -1, -1, false);
		}

		public PayDay newInstance()
		{
			PayDay x = (PayDay)(new PayDay().activate());
			x.coins = coins;
			return x;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			PayDay payday = (PayDay)Effect.getEffect(b.getEffects(true), "PayDay");
			b.addMessage(getCastMessage(b, caster, victim));
			coins = 5*caster.getLevel();
			if (payday == null) b.getPlayer().addEffect(this);
			else payday.coins += coins;
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return "Coins scattered everywhere!";
		}

		public void afterBattle(Trainer player, Battle b, ActivePokemon p)
		{
			b.addMessage(player.getName() + " picked up " + coins + " pokedollars!");
			player.getDatCashMoney(coins);
		}
	}

	private static class DoubleMoney extends TeamEffect 
	{
		private static final long serialVersionUID = 1L;
		public DoubleMoney()
		{
			super("DoubleMoney", -1, -1, false);
		}

		public DoubleMoney newInstance()
		{
			return (DoubleMoney)(new DoubleMoney().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(Effect.hasEffect(b.getEffects(victim.user()), "DoubleMoney"));
		}
	}

}
