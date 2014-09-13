package battle.effect;

import java.util.HashMap;

import main.Namesies;
import main.Type;
import pokemon.ActivePokemon;
import pokemon.Stat;
import battle.Attack.MoveType;
import battle.Battle;

public abstract class BattleEffect extends Effect 
{
	private static final long serialVersionUID = 1L;
	private static HashMap<String, BattleEffect> map;
	
	public BattleEffect(Namesies name, int minTurns, int maxTurns, boolean nextTurnSubside)
	{
		super(name, minTurns, maxTurns, nextTurnSubside);
	}
	
	public abstract BattleEffect newInstance();
	
	public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
	{
		if (printCast) b.addMessage(getCastMessage(b, caster, victim));
		b.addEffect(this.newInstance());
	}
	
	public static BattleEffect getEffect(Namesies name)
	{
		String e = name.getName();
		
		if (map == null) 
		{
			loadEffects();
		}
		
		if (map.containsKey(e))
		{
			return map.get(e);
		}
		
		// Otherwise, check if it's a weather effect which will handle the error checking and such if it isn't there either
		return Weather.getEffect(name);
	}

	// Create and load the effects map if it doesn't already exist
	public static void loadEffects()
	{
		if (map != null) return;
		map = new HashMap<>();
		
		// EVERYTHING BELOW IS GENERATED ###

		// List all of the classes we are loading
		map.put("Gravity", new Gravity());
		map.put("WaterSport", new WaterSport());
		map.put("MudSport", new MudSport());
		map.put("WonderRoom", new WonderRoom());
		map.put("TrickRoom", new TrickRoom());
		map.put("MagicRoom", new MagicRoom());
	}

	/**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

	private static class Gravity extends BattleEffect implements GroundedEffect, StageChangingEffect, BeforeTurnEffect
	{
		private static final long serialVersionUID = 1L;
		private void removeLevitation(Battle b, ActivePokemon p)
		{
			if (p.isSemiInvulnerableFlying())
			{
				p.getMove().switchReady(b);
				b.addMessage(p.getName() + " fell due to the gravity!");
			}
			
			if (p.hasEffect(Namesies.MAGNET_RISE_EFFECT))
			{
				Effect.removeEffect(p.getEffects(), Namesies.MAGNET_RISE_EFFECT);
				b.addMessage("The effects of " + p.getName() + "'s magnet rise were cancelled due to the gravity!");
			}
			
			if (p.hasEffect(Namesies.TELEKINESIS_EFFECT))
			{
				Effect.removeEffect(p.getEffects(), Namesies.TELEKINESIS_EFFECT);
				b.addMessage("The effects of telekinesis were cancelled due to the gravity!");
			}
		}

		public Gravity()
		{
			super(Namesies.GRAVITY_EFFECT, 5, 5, false);
		}

		public Gravity newInstance()
		{
			return (Gravity)(new Gravity().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(Effect.hasEffect(b.getEffects(), this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			super.cast(b, caster, victim, source, printCast);
			removeLevitation(b, caster);
			removeLevitation(b, victim);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return "Gravity intensified!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return "The gravity returned to normal.";
		}

		public int adjustStage(Integer stage, Stat s, ActivePokemon p, ActivePokemon opp, Battle b)
		{
			return s == Stat.EVASION ? stage - 2 : stage;
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b)
		{
			if (p.getAttack().isMoveType(MoveType.AIRBORNE))
			{
				b.printAttacking(p);
				b.addMessage(this.getFailMessage(b, p, opp));
				return false;
			}
			return true;
		}
	}

	private static class WaterSport extends BattleEffect implements PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public WaterSport()
		{
			super(Namesies.WATER_SPORT_EFFECT, -1, -1, false);
		}

		public WaterSport newInstance()
		{
			return (WaterSport)(new WaterSport().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(Effect.hasEffect(b.getEffects(), this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return "Fire's power was weakened!";
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getAttack().getType(b, user) == Type.FIRE ? .33 : 1;
		}
	}

	private static class MudSport extends BattleEffect implements PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public MudSport()
		{
			super(Namesies.MUD_SPORT_EFFECT, -1, -1, false);
		}

		public MudSport newInstance()
		{
			return (MudSport)(new MudSport().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(Effect.hasEffect(b.getEffects(), this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return "Electricity's power was weakened!";
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getAttack().getType(b, user) == Type.ELECTRIC ? .33 : 1;
		}
	}

	private static class WonderRoom extends BattleEffect implements StatSwitchingEffect
	{
		private static final long serialVersionUID = 1L;

		public WonderRoom()
		{
			super(Namesies.WONDER_ROOM_EFFECT, 5, 5, false);
		}

		public WonderRoom newInstance()
		{
			return (WonderRoom)(new WonderRoom().activate());
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			Effect wonder = Effect.getEffect(b.getEffects(), Namesies.WONDER_ROOM_EFFECT);
			if (wonder == null)
			{
				super.cast(b, caster, victim, source, printCast);
				return;
			}
			
			b.addMessage(wonder.getSubsideMessage(caster));
			Effect.removeEffect(b.getEffects(), Namesies.WONDER_ROOM_EFFECT);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getName() + " twisted the dimensions to switch defense and special defense!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return "The dimensions of the wonder room returned to normal.";
		}

		public Stat switchStat(Stat s)
		{
			if (s == Stat.DEFENSE) return Stat.SP_DEFENSE;
			if (s == Stat.SP_DEFENSE) return Stat.DEFENSE;
			return s;
		}
	}

	private static class TrickRoom extends BattleEffect 
	{
		private static final long serialVersionUID = 1L;

		public TrickRoom()
		{
			super(Namesies.TRICK_ROOM_EFFECT, 5, 5, false);
		}

		public TrickRoom newInstance()
		{
			return (TrickRoom)(new TrickRoom().activate());
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			Effect tricksies = Effect.getEffect(b.getEffects(), this.namesies);
			if (tricksies == null)
			{
				super.cast(b, caster, victim, source, printCast);
				return;
			}
			
			b.addMessage(tricksies.getSubsideMessage(caster));
			Effect.removeEffect(b.getEffects(), this.namesies);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getName() + " twisted the dimensions to switch speeds!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return "The dimensions of the trick room returned to normal.";
		}
	}

	private static class MagicRoom extends BattleEffect 
	{
		private static final long serialVersionUID = 1L;

		public MagicRoom()
		{
			super(Namesies.MAGIC_ROOM_EFFECT, 5, 5, false);
		}

		public MagicRoom newInstance()
		{
			return (MagicRoom)(new MagicRoom().activate());
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			Effect magics = Effect.getEffect(b.getEffects(), this.namesies);
			if (magics == null)
			{
				super.cast(b, caster, victim, source, printCast);
				return;
			}
			
			b.addMessage(magics.getSubsideMessage(caster));
			Effect.removeEffect(b.getEffects(), this.namesies);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getName() + " twisted the dimensions to prevent using items!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return "The dimensions of the magic room returned to normal.";
		}
	}
}
