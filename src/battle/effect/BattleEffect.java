package battle.effect;

import java.util.HashMap;

import main.Namesies;
import main.Type;
import pokemon.ActivePokemon;
import pokemon.Stat;
import battle.Attack.MoveType;
import battle.Battle;
import battle.effect.Status.StatusCondition;

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
		
		b.addMessage("", caster);
		b.addMessage("", victim);
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
		map.put("MistyTerrain", new MistyTerrain());
		map.put("GrassyTerrain", new GrassyTerrain());
		map.put("ElectricTerrain", new ElectricTerrain());
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
			super(Namesies.WATER_SPORT_EFFECT, 5, 5, false);
		}

		public WaterSport newInstance()
		{
			return (WaterSport)(new WaterSport().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(Effect.hasEffect(b.getEffects(), this.namesies));
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getAttackType() == Type.FIRE ? .33 : 1;
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return "Fire's power was weakened!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return "The effects of Water Sport wore off.";
		}
	}

	private static class MudSport extends BattleEffect implements PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public MudSport()
		{
			super(Namesies.MUD_SPORT_EFFECT, 5, 5, false);
		}

		public MudSport newInstance()
		{
			return (MudSport)(new MudSport().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(Effect.hasEffect(b.getEffects(), this.namesies));
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getAttackType() == Type.ELECTRIC ? .33 : 1;
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return "Electricity's power was weakened!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return "The effects of Mud Sport wore off.";
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

		public Stat switchStat(Stat s)
		{
			if (s == Stat.DEFENSE) return Stat.SP_DEFENSE;
			if (s == Stat.SP_DEFENSE) return Stat.DEFENSE;
			return s;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
		{
			Effect roomsies = Effect.getEffect(b.getEffects(), this.namesies);
			if (roomsies == null)
			{
				super.cast(b, caster, victim, source, printCast);
				return;
			}
			
			// Remove the effect if it's already in play
			b.addMessage(roomsies.getSubsideMessage(caster));
			Effect.removeEffect(b.getEffects(), this.namesies);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getName() + " twisted the dimensions to switch defense and special defense!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return "The dimensions of the wonder room returned to normal.";
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
			Effect roomsies = Effect.getEffect(b.getEffects(), this.namesies);
			if (roomsies == null)
			{
				super.cast(b, caster, victim, source, printCast);
				return;
			}
			
			// Remove the effect if it's already in play
			b.addMessage(roomsies.getSubsideMessage(caster));
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
			Effect roomsies = Effect.getEffect(b.getEffects(), this.namesies);
			if (roomsies == null)
			{
				super.cast(b, caster, victim, source, printCast);
				return;
			}
			
			// Remove the effect if it's already in play
			b.addMessage(roomsies.getSubsideMessage(caster));
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

	private static class MistyTerrain extends BattleEffect implements StatusPreventionEffect, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public MistyTerrain()
		{
			super(Namesies.MISTY_TERRAIN_EFFECT, 5, 5, false);
		}

		public MistyTerrain newInstance()
		{
			return (MistyTerrain)(new MistyTerrain().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(Effect.hasEffect(b.getEffects(), this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return "Mist swirled around the battlefield!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return "The mist disappeared from the battlefield.";
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status)
		{
			// Levitating Pokemon are immune to the mist
			return !victim.isLevitating(b);
		}

		public String statusPreventionMessage(ActivePokemon victim)
		{
			return "The protective mist prevents status conditions!";
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			// Dragon type moves have halved power during the misty terrain
			return user.getAttackType() == Type.DRAGON && !user.isLevitating(b) ? .5 : 1;
		}
	}

	private static class GrassyTerrain extends BattleEffect implements EndTurnEffect, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public GrassyTerrain()
		{
			super(Namesies.GRASSY_TERRAIN_EFFECT, 5, 5, false);
		}

		public GrassyTerrain newInstance()
		{
			return (GrassyTerrain)(new GrassyTerrain().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(Effect.hasEffect(b.getEffects(), this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return "Grass sprouted around the battlefield!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return "The grass withered and died.";
		}

		public void applyEndTurn(ActivePokemon victim, Battle b)
		{
			if (!victim.fullHealth() && !victim.isLevitating(b))
			{
				victim.healHealthFraction(1/16.0);
				b.addMessage(victim.getName() + " restored some HP due to the Grassy Terrain!", victim);
			}
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			// Grass-type moves are 50% stronger with the grassy terrain
			return user.getAttackType() == Type.GRASS && !user.isLevitating(b) ? 1.5 : 1;
		}
	}

	private static class ElectricTerrain extends BattleEffect implements StatusPreventionEffect, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public ElectricTerrain()
		{
			super(Namesies.ELECTRIC_TERRAIN_EFFECT, 5, 5, false);
		}

		public ElectricTerrain newInstance()
		{
			return (ElectricTerrain)(new ElectricTerrain().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(Effect.hasEffect(b.getEffects(), this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return "Electricity crackled around the battlefield!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return "The electricity dissipated.";
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status)
		{
			return status == StatusCondition.ASLEEP && !victim.isLevitating(b);
		}

		public String statusPreventionMessage(ActivePokemon victim)
		{
			return "The electric terrain prevents sleep!";
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			// Electric-type moves are 50% stronger with the electric terrain
			return user.getAttackType() == Type.ELECTRIC && !user.isLevitating(b) ? 1.5 : 1;
		}
	}
}
