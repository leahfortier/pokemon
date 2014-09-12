package battle.effect;

import item.Item;
import item.berry.StatusBerry;
import item.use.PokemonUseItem;

import java.io.Serializable;

import main.Global;
import main.Namesies;
import main.Type;
import pokemon.ActivePokemon;
import pokemon.Stat;
import battle.Attack.MoveType;
import battle.Battle;

public abstract class Status implements Serializable
{
	private static final long serialVersionUID = 1L;
	protected StatusCondition type;
	
	public static enum StatusCondition implements Serializable
	{
		NONE("", 1), FAINTED("FNT", 1), PARALYZED("PRZ", 1.5), POISONED("PSN", 1.5), 
		BURNED("BRN", 1.5), ASLEEP("SLP", 2.5), FROZEN("FRZ", 2.5);
		
		private String name;
		private double catchModifier;
		
		private StatusCondition(String name, double mod)
		{
			this.name = name;
			catchModifier = mod;
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
	
	protected abstract String getCastMessage(ActivePokemon p);
	public abstract String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim);
	
	public static String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim, StatusCondition status)
	{
		return getStatus(status, victim).getFailMessage(b, user, victim);
	}
	
	protected String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim)
	{
		Object[] list = b.getEffectsList(victim);
		Object statusPrevent = Global.getInvoke(user, list, StatusPreventionEffect.class, "preventStatus", b, user, victim, type);
		if (statusPrevent != null)
		{
			return ((StatusPreventionEffect)statusPrevent).preventionMessage(victim); 
		}
		
		return Effect.DEFAULT_FAIL_MESSAGE;
	}
	
	private static Status getStatus(StatusCondition s, ActivePokemon victim)
	{
		switch (s)
		{
			case NONE:
				return new None();
			case FAINTED:
				return new Fainted();
			case PARALYZED:
				return new Paralyzed();
			case POISONED:
				return new Poisoned();
			case BURNED:
				return new Burned();
			case FROZEN:
				return new Frozen();
			case ASLEEP:
				return new Asleep(victim);
			default:
				Global.error("No such Status Condition " + s);
				return null;
		}
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
		Object preventStatus = Global.checkInvoke(true, caster, list, StatusPreventionEffect.class, "preventStatus", b, caster, victim, type);
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
			b.addMessage(castMessage, status, victim.user());
			victim.setStatus(s);
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
			if (((PokemonUseItem)item).use(victim))
			{
				b.addMessage(victim.getName() + "'s " + item.getName() + " cured it of its status condition!", StatusCondition.NONE, victim.user());
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
			berryCheck(b, caster, status);
			b.addMessage(s.getAbilityCastMessage(victim, caster), status, caster.user());
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
	
	private static class None extends Status 
	{
		private static final long serialVersionUID = 1L;

		public None()
		{
			super.type = StatusCondition.NONE;
		}
		
		public String getCastMessage(ActivePokemon p)
		{
			return "";
		}

		public String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim)
		{
			return "";
		}
	}
	
	private static class Fainted extends Status 
	{
		private static final long serialVersionUID = 1L;

		public Fainted()
		{
			super.type = StatusCondition.FAINTED;
		}
		
		public String getCastMessage(ActivePokemon p)
		{
			return p.getName() + " fainted!";
		}
		
		public String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim)
		{
			return abilify.getName() + "'s " + abilify.getAbility().getName() + " caused " + victim.getName() + " to faint!";
		}
	}
	
	private static class Paralyzed extends Status implements BeforeTurnEffect, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public Paralyzed()
		{
			super.type = StatusCondition.PARALYZED;
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
	}
	
	private static class Poisoned extends Status implements EndTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public Poisoned()
		{
			super.type = StatusCondition.POISONED;
		}
		
		public void apply(ActivePokemon victim, Battle b) 
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
				b.addMessage(victim.getName() + "'s Poison Heal restored its health!", victim.getHP(), victim.user());
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
	}
	
	private static class Asleep extends Status implements BeforeTurnEffect
	{
		private static final long serialVersionUID = 1L;
		private int numTurns;
		
		public Asleep(ActivePokemon victim)
		{
			numTurns = (int)(Math.random()*3) + 1;
			super.type = StatusCondition.ASLEEP;
			
			if (victim.hasAbility(Namesies.EARLY_BIRD_ABILITY)) 
			{
				numTurns /= 2;
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
				b.addMessage(p.getName() + " woke up!", StatusCondition.NONE, p.user());
				p.removeStatus();
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
	}
	
	private static class Burned extends Status implements EndTurnEffect, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public Burned()
		{
			super.type = StatusCondition.BURNED;
		}
		
		public void apply(ActivePokemon victim, Battle b) 
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
	}
	
	// TODO: Does not handle being defrosted when hit by a fire-type move -- should be a TakeDamageEffect or something to that nature
	private static class Frozen extends Status implements BeforeTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public Frozen()
		{
			super.type = StatusCondition.FROZEN;
		}
		
		// Ice-type Pokemon cannot be frozen TODO: Cannot be frozen during intense sunlight
		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return super.applies(b, caster, victim) && !victim.isType(b, Type.ICE);
		}
		
		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) 
		{
			// 20% chance to thaw out each turn
			if (Math.random()*100 < 20 || p.getAttack().isMoveType(MoveType.DEFROST))
			{
				b.addMessage(p.getName() + " thawed out!", StatusCondition.NONE, p.user());
				p.removeStatus();
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
	}
}
