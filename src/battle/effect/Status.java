package battle.effect;

import item.Item;
import item.berry.GainableEffectBerry;
import item.berry.StatusBerry;

import java.io.Serializable;

import main.Global;
import main.Namesies;
import main.Type;
import pokemon.ActivePokemon;
import pokemon.Stat;
import battle.Attack.MoveType;
import battle.Battle;
import battle.effect.Effect.CastSource;

public abstract class Status implements Serializable
{
	private static final long serialVersionUID = 1L;
	protected final StatusCondition type;
	
	protected Status(StatusCondition type) {
		this.type = type;
	}
	
	protected abstract String getCastMessage(ActivePokemon p);
	protected abstract String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim);

	protected abstract String getRemoveMessage(ActivePokemon victim);
	protected abstract String getSourceRemoveMessage(ActivePokemon victim, String sourceName);
	
	// A method to be overidden if anything related to conflicted victim is necessary to create this status
	protected void postCreateEffect(ActivePokemon victim) {}
	
	public static enum StatusCondition implements Serializable
	{
		NONE("", 1, None.class), 
		FAINTED("FNT", 1, Fainted.class),
		PARALYZED("PRZ", 1.5, Paralyzed.class), 
		POISONED("PSN", 1.5, Poisoned.class), 
		BURNED("BRN", 1.5, Burned.class),
		ASLEEP("SLP", 2.5, Asleep.class), 
		FROZEN("FRZ", 2.5, Frozen.class);
		
		private final String name;
		private final double catchModifier;
		private final Class<? extends Status> statusClass;
		
		private StatusCondition(String name, double catchModifier, Class<? extends Status> statusClass)
		{
			this.name = name;
			this.catchModifier = catchModifier;
			this.statusClass = statusClass;
		}
		
		public String getName()
		{
			return name;
		}
		
		public double getCatchModifier()
		{
			return catchModifier;
		}
	}
	
	
	
	public static void removeStatus(Battle b, ActivePokemon victim, CastSource source)
	{
		b.addMessage(getRemoveStatus(b, victim, source), victim);
	}
	
	public static String getRemoveStatus(Battle b, ActivePokemon victim, CastSource source)
	{
		StatusCondition status = victim.getStatus().getType();
		victim.removeStatus();
		
		switch (source)
		{
			case ABILITY:
				return getStatus(status, victim).getSourceRemoveMessage(victim, victim.getAbility().getName());
			case HELD_ITEM:
				return getStatus(status, victim).getSourceRemoveMessage(victim, victim.getHeldItem(b).getName());
			default:
				return getStatus(status, victim).getRemoveMessage(victim);
		}
	}
	
	public static String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim, StatusCondition status)
	{
		return getStatus(status, victim).getFailMessage(b, user, victim);
	}
	
	protected String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim)
	{
		Object[] list = b.getEffectsList(victim);
		Object statusPrevent = Battle.checkInvoke(true, user, list, StatusPreventionEffect.class, "preventStatus", b, user, victim, type);
		if (statusPrevent != null)
		{
			return ((StatusPreventionEffect)statusPrevent).statusPreventionMessage(victim); 
		}
		
		return Effect.DEFAULT_FAIL_MESSAGE;
	}
	
	// Creates a new status like a motherfucking champ
	private static Status getStatus(StatusCondition s, ActivePokemon victim)
	{
		Status status = (Status)Global.dynamicInstantiaton(s.statusClass);
		status.postCreateEffect(victim);
		
		return status;
	}
	
	public static boolean applies(StatusCondition status, Battle b, ActivePokemon caster, ActivePokemon victim)
	{
		return getStatus(status, victim).applies(b, caster, victim);
	}
	
	protected boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim)
	{
		if (victim.hasStatus())
		{
			return false;
		}
		
		Object[] list = b.getEffectsList(victim);
		Object preventStatus = Battle.checkInvoke(true, caster, list, StatusPreventionEffect.class, "preventStatus", b, caster, victim, type);
		if (preventStatus != null)
		{
			return false;
		}
		
		return true;
	}
	
	public StatusCondition getType()
	{
		return type;
	}
	
	public void setTurns(int turns) {}
	
	// Returns true if a status was successfully given, and false if it failed for any reason
	public static boolean giveStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) 
	{
		return giveStatus(b, caster, victim, status, false);
	}
	
	public static boolean giveStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status, boolean abilityCast)
	{
		Status s = getStatus(status, victim);
		return giveStatus(b, caster, victim, status, abilityCast ? s.getAbilityCastMessage(caster, victim) : s.getCastMessage(victim));
	}
	
	public static boolean giveStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status, String castMessage)
	{
		Status s = getStatus(status, victim);
		if (s.applies(b, caster, victim))
		{
			victim.setStatus(s);
			b.addMessage(castMessage, victim);
			
			synchronizeCheck(b, caster, victim, status);
			berryCheck(b, victim, status);
			
			return true;
		}
		return false;
	}
	
	private static void berryCheck(Battle b, ActivePokemon victim, StatusCondition status)
	{
		Item item = victim.getHeldItem(b);
		if (item instanceof StatusBerry)
		{
			GainableEffectBerry berry = ((GainableEffectBerry)item);
			
			if (berry.gainBerryEffect(b, victim, CastSource.HELD_ITEM))
			{
				victim.consumeItem(b);				
			}
		}
	}
	
	private static void synchronizeCheck(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status)
	{
		Status s = getStatus(status, caster);
		if (victim.hasAbility(Namesies.SYNCHRONIZE_ABILITY) && s.applies(b, victim, caster)
				&& (status == StatusCondition.BURNED || status == StatusCondition.POISONED || status == StatusCondition.PARALYZED))
		{
			if (victim.hasEffect(Namesies.BAD_POISON_EFFECT)) 
			{
				caster.addEffect(PokemonEffect.getEffect(Namesies.BAD_POISON_EFFECT).newInstance());
			}
			
			caster.setStatus(s);
			b.addMessage(s.getAbilityCastMessage(victim, caster), caster);
			
			berryCheck(b, caster, status);
		}
	}
	
	public static void removeStatus(ActivePokemon p)
	{
		p.setStatus(new None());
	}
	
	public static void die(ActivePokemon p)
	{
		if (p.getHP() > 0) Global.error("Only dead Pokemon can die.");
		p.setStatus(new Fainted());
	}
	
	public static class None extends Status 
	{
		private static final long serialVersionUID = 1L;

		public None()
		{
			super(StatusCondition.NONE);
		}
		
		public String getCastMessage(ActivePokemon p)
		{
			return "";
		}

		public String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim)
		{
			return "";
		}

		public String getRemoveMessage(ActivePokemon victim)
		{
			return "";
		}
		
		public String getSourceRemoveMessage(ActivePokemon victim, String sourceName)
		{
			return "";
		}
	}
	
	public static class Fainted extends Status 
	{
		private static final long serialVersionUID = 1L;

		public Fainted()
		{
			super(StatusCondition.FAINTED);
		}
		
		public String getCastMessage(ActivePokemon p)
		{
			return p.getName() + " fainted!";
		}
		
		public String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim)
		{
			return abilify.getName() + "'s " + abilify.getAbility().getName() + " caused " + victim.getName() + " to faint!";
		}

		public String getRemoveMessage(ActivePokemon victim)
		{
			return "";
		}
		
		public String getSourceRemoveMessage(ActivePokemon victim, String sourceName)
		{
			return "";
		}
	}
	
	public static class Paralyzed extends Status implements BeforeTurnEffect, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public Paralyzed()
		{
			super(StatusCondition.PARALYZED);
		}
		
		// Electric-type Pokemon cannot be paralyzed
		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return super.applies(b, caster, victim) && !victim.isType(b, Type.ELECTRIC);
		}
		
		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) 
		{
			if (Math.random()*100 < 25)
			{
				b.addMessage(p.getName() + " is fully paralyzed!");
				return false;
			}
			return true;
		}
		
		public String getCastMessage(ActivePokemon p)
		{
			return p.getName() + " was paralyzed!";
		}
		
		public String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim)
		{
			return abilify.getName() + "'s " + abilify.getAbility().getName() + " paralyzed " + victim.getName() + "!";
		}
		
		public int modify(Integer stat, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			return (int)(stat*(s == Stat.SPEED && !p.hasAbility(Namesies.QUICK_FEET_ABILITY) ? .25 : 1));
		}

		public String getRemoveMessage(ActivePokemon victim)
		{
			return victim.getName() + " is no longer paralyzed!";
		}

		public String getSourceRemoveMessage(ActivePokemon victim, String sourceName)
		{
			return victim.getName() + "'s " + sourceName + " cured it of its paralysis!";
		}
	}
	
	public static class Poisoned extends Status implements EndTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public Poisoned()
		{
			super(StatusCondition.POISONED);
		}
		
		public void applyEndTurn(ActivePokemon victim, Battle b) 
		{
			if (victim.hasAbility(Namesies.MAGIC_GUARD_ABILITY)) 
			{
				return;
			}
			
			if (victim.hasAbility(Namesies.POISON_HEAL_ABILITY))
			{
				if  (victim.fullHealth() || victim.hasEffect(Namesies.HEAL_BLOCK_EFFECT)) 
				{
					return;
				}
				
				victim.healHealthFraction(1/8.0);
				b.addMessage(victim.getName() + "'s " + Namesies.POISON_HEAL_ABILITY + " restored its health!", victim);
				return;
			}
			
			PokemonEffect badPoison = victim.getEffect(Namesies.BAD_POISON_EFFECT);
			b.addMessage(victim.getName() + " was hurt by its poison!");
			victim.reduceHealthFraction(b, badPoison == null ? 1/8.0 : badPoison.getTurns()/16.0);
		}
		
		// Poison-type Pokemon cannot be poisoned
		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return super.applies(b, caster, victim) && !victim.isType(b, Type.POISON);
		}
		
		public String getCastMessage(ActivePokemon p)
		{
			return p.getName() + " was " + (p.hasEffect(Namesies.BAD_POISON_EFFECT) ? "badly " : "") + "poisoned!";
		}
		
		public String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim)
		{
			return abilify.getName() + "'s " + abilify.getAbility().getName() + (victim.hasEffect(Namesies.BAD_POISON_EFFECT) ? " badly " : " ") + "poisoned " + victim.getName() + "!";
		}

		public String getRemoveMessage(ActivePokemon victim)
		{
			return victim.getName() + " is no longer poisoned!";
		}
		
		public String getSourceRemoveMessage(ActivePokemon victim, String sourceName)
		{
			return victim.getName() + "'s " + sourceName + " cured it of its poison!";
		}
	}
	
	public static class Asleep extends Status implements BeforeTurnEffect
	{
		private static final long serialVersionUID = 1L;
		private int numTurns;
		
		public Asleep()
		{
			super(StatusCondition.ASLEEP);
			this.numTurns = (int)(Math.random()*3) + 1;
		}
		
		protected void postCreateEffect(ActivePokemon victim) 
		{
			if (victim.hasAbility(Namesies.EARLY_BIRD_ABILITY)) 
			{
				this.numTurns /= 2;
			}
		}

		// No one can be asleep while Uproar is in effect by either Pokemon
		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return super.applies(b, caster, victim) && !caster.hasEffect(Namesies.UPROAR_EFFECT) && !victim.hasEffect(Namesies.UPROAR_EFFECT);
		}
		
		public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.hasEffect(Namesies.UPROAR_EFFECT) || victim.hasEffect(Namesies.UPROAR_EFFECT))
			{
				return "The uproar prevents sleep!";
			}
			
			return super.getFailMessage(b, user, victim);
		}
		
		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) 
		{
			if (numTurns == 0)
			{
				Status.removeStatus(b, p, CastSource.EFFECT);
				
				return true;
			}
			
			numTurns--;
			b.addMessage(p.getName() + " is fast asleep...");
			return p.getAttack().isMoveType(MoveType.ASLEEP_USER);
		}
		
		public String getCastMessage(ActivePokemon p)
		{
			return p.getName() + " fell asleep!";
		}
		
		public String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim)
		{
			return abilify.getName() + "'s " + abilify.getAbility().getName() + " caused " + victim.getName() + " to fall asleep!";
		}
		
		public void setTurns(int turns)
		{
			numTurns = turns;
		}

		public String getRemoveMessage(ActivePokemon victim)
		{
			return victim.getName() + " woke up!";
		}
		
		public String getSourceRemoveMessage(ActivePokemon victim, String sourceName)
		{
			return victim.getName() + "'s " + sourceName + " caused it to wake up!";
		}
	}
	
	public static class Burned extends Status implements EndTurnEffect, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public Burned()
		{
			super(StatusCondition.BURNED);
		}
		
		public void applyEndTurn(ActivePokemon victim, Battle b) 
		{
			if (victim.hasAbility(Namesies.MAGIC_GUARD_ABILITY)) 
			{
				return;
			}
			
			b.addMessage(victim.getName() + " was hurt by its burn!");
			victim.reduceHealthFraction(b, victim.hasAbility(Namesies.HEATPROOF_ABILITY) ? 1/16.0 : 1/8.0);
		}
		
		// Fire-type Pokemon cannot be burned
		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return super.applies(b, caster, victim) && !victim.isType(b, Type.FIRE);
		}
		
		public String getCastMessage(ActivePokemon p)
		{
			return p.getName() + " was burned!";
		}
		
		public String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim)
		{
			return abilify.getName() + "'s " + abilify.getAbility().getName() + " burned " + victim.getName() + "!";
		}
		
		public int modify(Integer stat, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			return (int)(stat*(s == Stat.ATTACK && !p.hasAbility(Namesies.GUTS_ABILITY) ? .5 : 1));
		}

		public String getRemoveMessage(ActivePokemon victim)
		{
			return victim.getName() + " is no longer burned!";
		}
		
		public String getSourceRemoveMessage(ActivePokemon victim, String sourceName)
		{
			return victim.getName() + "'s " + sourceName + " cured it of its burn!";
		}
	}
	
	public static class Frozen extends Status implements BeforeTurnEffect, TakeDamageEffect
	{
		private static final long serialVersionUID = 1L;

		public Frozen()
		{
			super(StatusCondition.FROZEN);
		}
		
		// Ice-type Pokemon cannot be frozen and no one can frozen while sunny
		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return super.applies(b, caster, victim) && !victim.isType(b, Type.ICE) && b.getWeather().namesies() != Namesies.SUNNY_EFFECT;
		}
		
		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) 
		{
			// 20% chance to thaw out each turn
			if (Math.random()*100 < 20 || p.getAttack().isMoveType(MoveType.DEFROST))
			{
				Status.removeStatus(b, p, CastSource.EFFECT);
				
				return true;
			}
			
			b.addMessage(p.getName() + " is frozen solid!");
			return false;
		}
		
		public String getCastMessage(ActivePokemon p)
		{
			return p.getName() + " was frozen!";
		}
		
		public String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim)
		{
			return abilify.getName() + "'s " + abilify.getAbility().getName() + " froze " + victim.getName() + "!";
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			// Fire-type moves defrost the user
			if (user.isAttackType(Type.FIRE))
			{
				Status.removeStatus(b, victim, CastSource.EFFECT);
			}
		}

		public String getRemoveMessage(ActivePokemon victim)
		{
			return victim.getName() + " thawed out!";
		}
		
		public String getSourceRemoveMessage(ActivePokemon victim, String sourceName)
		{
			return victim.getName() + "'s " + sourceName + " thawed it out!";
		}
	}
}
