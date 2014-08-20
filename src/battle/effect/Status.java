package battle.effect;

import item.Item;
import item.berry.StatusBerry;
import item.use.PokemonUseItem;

import java.io.Serializable;
import java.util.List;

import main.Global;
import main.Type;
import pokemon.ActivePokemon;
import pokemon.Stat;
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
		List<Object> list = b.getEffectsList(victim);
		for (Object o : list)
		{
			if (o instanceof Effect && !((Effect)o).isActive()) continue;
			if (o instanceof StatusPreventionEffect && ((StatusPreventionEffect) o).preventStatus(b, user, victim, type)) 
			{
				return ((StatusPreventionEffect)o).preventionMessage(victim);
			}
		}
		return "...but it failed!";
	}
	
	private static Status getStatus(StatusCondition s, ActivePokemon victim)
	{
		switch(s)
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
				Global.error("No such Status Condition "+s);
				return null;
		}
	}
	
	public static boolean applies(StatusCondition status, Battle b, ActivePokemon caster, ActivePokemon victim)
	{
		return getStatus(status, victim).applies(b, caster, victim);
	}
	
	protected boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim)
	{
		if (victim.hasStatus()) return false;
		List<Object> list = b.getEffectsList(victim);
		for (Object o : list)
		{
			if (o instanceof Effect && !((Effect)o).isActive()) continue;
			if (o instanceof StatusPreventionEffect && ((StatusPreventionEffect) o).preventStatus(b, caster, victim, type)) return false; 
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
				b.addMessage(victim.getName()+"'s "+item.getName()+" cured it of its status condition!", StatusCondition.NONE, victim.user());
				victim.consumeItem(b);				
			}
		}
	}
	
	private static void synchronizeCheck(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status)
	{
		Status s = getStatus(status, caster);
		if (victim.hasAbility("Synchronize") && s.applies(b, victim, caster)
				&& (status == StatusCondition.BURNED || status == StatusCondition.POISONED || status == StatusCondition.PARALYZED))
		{
			if (victim.hasEffect("BadPoison")) caster.addEffect(PokemonEffect.getEffect("BadPoison").newInstance());
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
			return p.getName()+" fainted!";
		}
		
		public String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim)
		{
			return abilify.getName()+"'s "+abilify.getAbility().getName()+" caused "+victim.getName()+" to faint!";
		}
	}
	
	private static class Paralyzed extends Status implements BeforeTurnEffect, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public Paralyzed()
		{
			super.type = StatusCondition.PARALYZED;
		}
		
		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) 
		{
			if (Math.random()*100 < 25)
			{
				b.addMessage(p.getName()+" is fully paralyzed!");
				return false;
			}
			return true;
		}
		
		public String getCastMessage(ActivePokemon p)
		{
			return p.getName()+" was paralyzed!";
		}
		
		public String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim)
		{
			return abilify.getName()+"'s "+abilify.getAbility().getName()+" paralyzed "+victim.getName()+"!";
		}
		
		public int modify(int stat, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			return (int)(stat*(s == Stat.SPEED && !p.hasAbility("Quick Feet") ? .25 : 1));
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
			if (victim.hasAbility("Magic Guard")) return;
			if (victim.hasAbility("Poison Heal"))
			{
				if  (victim.fullHealth() || victim.hasEffect("Heal Block")) return;
				victim.healHealthFraction(1/8.0);
				b.addMessage(victim.getName()+"'s Poison Heal restored its health!", victim.getHP(), victim.user());
				return;
			}
			
			PokemonEffect e = victim.getEffect("BadPoison");
			b.addMessage(victim.getName()+" was hurt by its poison!");
			victim.reduceHealthFraction(b, e == null ? 1/8.0 : e.getTurns()/16.0);
		}
		
		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return super.applies(b, caster, victim) && !victim.isType(Type.POISON);
		}
		
		public String getCastMessage(ActivePokemon p)
		{
			return p.getName()+" was "+(p.hasEffect("BadPoison") ? "badly " : "")+"poisoned!";
		}
		
		public String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim)
		{
			return abilify.getName()+"'s "+abilify.getAbility().getName()+(victim.hasEffect("BadPoison") ? " badly " : " ")+"poisoned "+victim.getName()+"!";
		}
	}
	
	private static class Asleep extends Status implements BeforeTurnEffect
	{
		private static final long serialVersionUID = 1L;
		private int numTurns;
		
		public Asleep(ActivePokemon victim)
		{
			numTurns = (int)(Math.random()*3)+1;
			super.type = StatusCondition.ASLEEP;
			if (victim.hasAbility("Early Bird")) numTurns /= 2;
		}
		
		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return super.applies(b, caster, victim) && !caster.hasEffect("Uproar") && !victim.hasEffect("Uproar");
		}
		
		public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.hasEffect("Uproar") || victim.hasEffect("Uproar")) return "The uproar prevents sleep!";
			return super.getFailMessage(b, user, victim);
		}
		
		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) 
		{
			if (numTurns == 0)
			{
				b.addMessage(p.getName()+" woke up!", StatusCondition.NONE, p.user());
				p.removeStatus();
				return true;
			}
			
			numTurns--;
			b.addMessage(p.getName()+" is fast asleep...");
			return p.getAttack().isMoveType("AsleepUser");
		}
		
		public String getCastMessage(ActivePokemon p)
		{
			return p.getName()+" fell asleep!";
		}
		
		public String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim)
		{
			return abilify.getName()+"'s "+abilify.getAbility().getName()+" caused "+victim.getName()+" to fall asleep!";
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
			if (victim.hasAbility("Magic Guard")) return;
			b.addMessage(victim.getName()+" was hurt by its burn!");
			victim.reduceHealthFraction(b, victim.hasAbility("Heatproof") ? 1/16.0 : 1/8.0);
		}
		
		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return super.applies(b, caster, victim) && !victim.isType(Type.FIRE);
		}
		
		public String getCastMessage(ActivePokemon p)
		{
			return p.getName()+" was burned!";
		}
		
		public String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim)
		{
			return abilify.getName()+"'s "+abilify.getAbility().getName()+" burned "+victim.getName()+"!";
		}
		
		public int modify(int stat, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			return (int)(stat*(s == Stat.ATTACK && !p.hasAbility("Guts") ? .5 : 1));
		}
	}
	
	private static class Frozen extends Status implements BeforeTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public Frozen()
		{
			super.type = StatusCondition.FROZEN;
		}
		
		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) 
		{
			// 20% chance to thaw out each turn
			if (Math.random()*100 < 20 || p.getAttack().isMoveType("Defrost"))
			{
				b.addMessage(p.getName()+" thawed out!", StatusCondition.NONE, p.user());
				p.removeStatus();
				return true;
			}
			b.addMessage(p.getName()+" is frozen solid!");
			return false;
		}
		
		public String getCastMessage(ActivePokemon p)
		{
			return p.getName()+" was frozen!";
		}
		
		public String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim)
		{
			return abilify.getName()+"'s "+abilify.getAbility().getName()+" froze "+victim.getName()+"!";
		}
	}
}