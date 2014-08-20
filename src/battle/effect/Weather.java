package battle.effect;

import item.Item;
import main.Global;
import main.Type;
import pokemon.Ability;
import pokemon.ActivePokemon;
import pokemon.Stat;
import battle.Battle;

public abstract class Weather extends BattleEffect implements EndTurnEffect 
{
	private static final long serialVersionUID = 1L;
	
	public static enum WeatherType
	{
		CLEAR_SKIES, SUNNY, RAINING, SANDSTORM, HAILING;
		
		private String name;
		
		private WeatherType()
		{
			name = name().charAt(0)+name().substring(1).toLowerCase();
		}
		
		public String getName()
		{
			return name;
		}
	}
	
	protected WeatherType type;
	
	public Weather(WeatherType weather)
	{
		super(weather.getName(), -1, -1, true);
		type = weather;
	}
	
	public WeatherType getType()
	{
		return type;
	}
	
	public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
	{
		super.cast(b, caster, victim, source, printCast);
		b.getWeather().setTurns(getTurns(b, caster));
	}
	
	private int getTurns(Battle b, ActivePokemon caster)
	{
		Item i = caster.getHeldItem(b);
		if (i instanceof WeatherExtendingEffect && type == ((WeatherExtendingEffect)i).getWeatherType()) return 8;
		return 5;
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
				Global.error("No such WeatherType "+weather.getName());
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
		
		public int modify(int stat, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
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

		public int modify(int stat, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			if (s == Stat.ATTACK || s == Stat.SP_ATTACK)
			{
				Type t = p.getAttack().getType(b, p);
				if (t == Type.FIRE) return (int)(stat*1.5);
				if (t == Type.WATER) return (int)(stat*.5);				
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
			buffet(victim, b);
			buffet(b.getOtherPokemon(victim.user()), b);
		}
		
		public void buffet(ActivePokemon p, Battle b)
		{
			if (p.isType(Type.ROCK) || p.isType(Type.STEEL) || p.isType(Type.GROUND)) return;
			
			Ability ability = p.getAbility();
			Item item = p.getHeldItem(b);
			
			if (ability instanceof WeatherBlockerEffect && ((WeatherBlockerEffect)ability).block(type)) return;
			if (item instanceof WeatherBlockerEffect && ((WeatherBlockerEffect)item).block(type)) return;
			
			b.addMessage(p.getName()+" is buffeted by the sandstorm!");
			p.reduceHealthFraction(b, 1/16.0);
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
		
		public int modify(int stat, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			return (int)(stat*(s == Stat.SP_DEFENSE && p.isType(Type.ROCK) ? 1.5 : 1));
		}
	}
	
	private static class Hailing extends Weather 
	{
		private static final long serialVersionUID = 1L;
		
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
			buffet(victim, b);
			buffet(b.getOtherPokemon(victim.user()), b);
		}
		
		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return b.getWeather().getType() != Weather.WeatherType.HAILING;
		}
		
		public void buffet(ActivePokemon p, Battle b)
		{
			if (p.isType(Type.ICE)) return;
			
			Ability ability = p.getAbility();
			Item item = p.getHeldItem(b);
			
			if (ability instanceof WeatherBlockerEffect && ((WeatherBlockerEffect)ability).block(type)) return;
			if (item instanceof WeatherBlockerEffect && ((WeatherBlockerEffect)item).block(type)) return;
			
			b.addMessage(p.getName()+" is buffeted by the hail!");
			p.reduceHealthFraction(b, 1/16.0);
		}
		
		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return "It started to hail!";
		}
		
		public String getSubsideMessage(ActivePokemon p)
		{
			return "The hail stopped.";
		}
	}
}
