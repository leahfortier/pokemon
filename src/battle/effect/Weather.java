package battle.effect;

import item.Item;
import main.Global;
import main.Type;
import pokemon.ActivePokemon;
import pokemon.Stat;
import battle.Battle;

public abstract class Weather extends BattleEffect implements EndTurnEffect 
{
	private static final long serialVersionUID = 1L;
	
	public static enum WeatherType
	{
		CLEAR_SKIES(Type.NORMAL), 
		SUNNY(Type.FIRE), 
		RAINING(Type.WATER), 
		SANDSTORM(Type.ROCK), 
		HAILING(Type.ICE);
		
		private Type element;
		
		private WeatherType(Type element)
		{
			this.element = element;
		}
		
		public Type getElement()
		{
			return element;
		}
	}
	
	protected WeatherType weatherType;
	
	public Weather(WeatherType weather)
	{
		super(weather.toString(), -1, -1, true);
		weatherType = weather;
	}
	
	public WeatherType getType()
	{
		return weatherType;
	}
	
	public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
	{
		super.cast(b, caster, victim, source, printCast);
		b.getWeather().setTurns(getTurns(b, caster));
	}
	
	private int getTurns(Battle b, ActivePokemon caster)
	{
		Item i = caster.getHeldItem(b);
		if (i instanceof WeatherExtendingEffect && weatherType == ((WeatherExtendingEffect)i).getWeatherType()) 
		{
			return 8;
		}
		
		return 5;
	}
	
	protected void buffet(Battle b, ActivePokemon p, Type[] immunees, String message)
	{
		for (Type type : immunees)
			if (p.isType(b, type))
				return;
		
		Object[] list = b.getEffectsList(p);
		Object checkeroo = Global.checkInvoke(true, list, WeatherBlockerEffect.class, "block", weatherType);
		if (checkeroo != null)
		{
			return;
		}

		// Buffety buffety buffet
		b.addMessage(message);
		p.reduceHealthFraction(b, 1/16.0);
	}
	
	public abstract Weather newInstance();
	
	public static Weather getWeather(WeatherType weather)
	{
		switch (weather)
		{
			case CLEAR_SKIES:
				return new ClearSkies();
			case SUNNY:
				return new Sunny();
			case RAINING:
				return new Raining();
			case SANDSTORM:
				return new Sandstorm();
			case HAILING:
				return new Hailing();
			default:
				Global.error("No such WeatherType " + weather.toString());
				return null;
		}
	}
	
	private static class ClearSkies extends Weather 
	{
		private static final long serialVersionUID = 1L;
		
		public ClearSkies()
		{
			super(WeatherType.CLEAR_SKIES);
		}
		
		public ClearSkies newInstance()
		{
			return (ClearSkies)(new ClearSkies().activate());
		}

		public void apply(ActivePokemon victim, Battle b) {}
	}
	
	private static class Raining extends Weather implements StatChangingEffect 
	{
		private static final long serialVersionUID = 1L;
		
		public Raining()
		{
			super(WeatherType.RAINING);
		}
		
		public Raining newInstance()
		{
			return (Raining)(new Raining().activate());
		}

		public void apply(ActivePokemon victim, Battle b) 
		{	
			b.addMessage("The rain continues to pour.");
		}
		
		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return "It started to rain!";
		}
		
		public String getSubsideMessage(ActivePokemon p)
		{
			return "The rain stopped.";
		}
		
		public int modify(Integer stat, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			if (s == Stat.ATTACK || s == Stat.SP_ATTACK)
			{
				Type t = p.getAttack().getType(b, p); 
				if (t == Type.WATER) return (int)(stat*1.5);
				if (t == Type.FIRE) return (int)(stat*.5);
			}
			
			return stat;
		}
		
		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return b.getWeather().getType() != Weather.WeatherType.RAINING;
		}
	}
	
	private static class Sunny extends Weather implements StatChangingEffect
	{
		private static final long serialVersionUID = 1L;
		
		public Sunny()
		{
			super(WeatherType.SUNNY);
		}
		
		public Sunny newInstance()
		{
			return (Sunny)(new Sunny().activate());
		}

		public void apply(ActivePokemon victim, Battle b) 
		{	
			b.addMessage("The sunlight is strong.");
		}
		
		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return "The sunlight turned harsh!";
		}
		
		public String getSubsideMessage(ActivePokemon p)
		{
			return "The sunlight faded.";
		}

		public int modify(Integer stat, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			if (s == Stat.ATTACK || s == Stat.SP_ATTACK)
			{
				Type t = p.getAttack().getType(b, p);
				
				// Fire is fiddy percent stronger in tha sun
				if (t == Type.FIRE)
				{
					return (int)(stat*1.5);
				}
				
				// Water is fiddy percent weaker in tha sun
				if (t == Type.WATER) 
				{
					return (int)(stat*.5);				
				}
			}
			
			return stat;
		}
		
		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return b.getWeather().getType() != Weather.WeatherType.SUNNY;
		}
	}
	
	private static class Sandstorm extends Weather implements StatChangingEffect 
	{
		private static final long serialVersionUID = 1L;
		
		private static Type[] immunees = new Type[] {Type.ROCK, Type.GROUND, Type.STEEL};
		
		public Sandstorm()
		{
			super(WeatherType.SANDSTORM);
		}
		
		public Sandstorm newInstance()
		{
			return (Sandstorm)(new Sandstorm().activate());
		}

		public void apply(ActivePokemon victim, Battle b) 
		{	
			b.addMessage("The sandstorm rages.");
			
			ActivePokemon other = b.getOtherPokemon(victim.user());
			buffet(b, victim, immunees, getBuffetMessage(victim));
			buffet(b, other, immunees, getBuffetMessage(other));
		}
		
		private String getBuffetMessage(ActivePokemon p)
		{
			return p.getName() + " is buffeted by the sandstorm!";
		}
		
		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return "A sandstorm kicked up!";
		}
		
		public String getSubsideMessage(ActivePokemon p)
		{
			return "The sandstorm subsided.";
		}
		
		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return b.getWeather().getType() != Weather.WeatherType.SANDSTORM;
		}
		
		public int modify(Integer stat, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			return (int)(stat*(s == Stat.SP_DEFENSE && p.isType(b, Type.ROCK) ? 1.5 : 1));
		}
	}
	
	private static class Hailing extends Weather 
	{
		private static final long serialVersionUID = 1L;
		
		private static Type[] immunees = new Type[] {Type.ICE};
		
		public Hailing()
		{
			super(WeatherType.HAILING);
		}
		
		public Hailing newInstance()
		{
			return (Hailing)(new Hailing().activate());
		}

		public void apply(ActivePokemon victim, Battle b) 
		{	
			b.addMessage("The hail continues to fall.");
			
			ActivePokemon other = b.getOtherPokemon(victim.user());
			buffet(b, victim, immunees, getBuffetMessage(victim)); 
			buffet(b, other, immunees, getBuffetMessage(other));
		}
		
		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return b.getWeather().getType() != Weather.WeatherType.HAILING;
		}
		
		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return "It started to hail!";
		}
		
		public String getSubsideMessage(ActivePokemon p)
		{
			return "The hail stopped.";
		}
		
		private String getBuffetMessage(ActivePokemon p)
		{
			return p.getName() + " is buffeted by the hail!";
		}
	}
}
