package battle.effect.generic;

import java.util.HashMap;
import java.util.Map;

import battle.MoveType;
import battle.effect.TerrainEffect;
import battle.effect.generic.EffectInterfaces.BeforeTurnEffect;
import battle.effect.generic.EffectInterfaces.EndTurnEffect;
import battle.effect.generic.EffectInterfaces.GroundedEffect;
import battle.effect.generic.EffectInterfaces.LevitationEffect;
import battle.effect.generic.EffectInterfaces.PowerChangeEffect;
import battle.effect.generic.EffectInterfaces.StageChangingEffect;
import battle.effect.generic.EffectInterfaces.StatSwitchingEffect;
import battle.effect.generic.EffectInterfaces.StatusPreventionEffect;
import battle.effect.status.StatusCondition;
import main.Type;
import map.TerrainType;
import message.Messages;
import namesies.EffectNamesies;
import pokemon.ActivePokemon;
import battle.Battle;
import pokemon.Stat;

public abstract class BattleEffect extends Effect {
	private static final long serialVersionUID = 1L;
	private static Map<String, BattleEffect> map;

	public BattleEffect(EffectNamesies name, int minTurns, int maxTurns, boolean nextTurnSubside) {
		super(name, minTurns, maxTurns, nextTurnSubside);
	}

	public abstract BattleEffect newInstance();

	public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
		if (printCast) {
			Messages.addMessage(getCastMessage(b, caster, victim));
		}
		b.addEffect(this.newInstance());

		Messages.addMessage("", b, caster);
		Messages.addMessage("", b, victim);
	}

	public static BattleEffect getEffect(EffectNamesies name) {
		String effectName = name.getName();
		if (map == null) {
			loadEffects();
		}

		if (map.containsKey(effectName)) {
			return map.get(effectName);
		}

		// Otherwise, check if it's a weather effect which will handle the error checking and such if it isn't there either
		return Weather.getEffect(name);
	}

	// Create and load the effects map if it doesn't already exist
	public static void loadEffects() {
		if (map != null) {
			return;
		}

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

	static class Gravity extends BattleEffect implements GroundedEffect, StageChangingEffect, BeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		Gravity() {
			super(EffectNamesies.GRAVITY, 5, 5, false);
		}

		public Gravity newInstance() {
			return (Gravity)(new Gravity().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(), this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			removeLevitation(b, caster);
			removeLevitation(b, victim);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return "Gravity intensified!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The gravity returned to normal.";
		}

		private void removeLevitation(Battle b, ActivePokemon p) {
			if (p.isSemiInvulnerableFlying()) {
				p.getMove().switchReady(b, p);
				Messages.addMessage(p.getName() + " fell to the ground!");
			}
			
			LevitationEffect.falllllllll(b, p);
		}

		public int adjustStage(Battle b,  ActivePokemon p, ActivePokemon opp, Stat s, int stage) {
			return s == Stat.EVASION ? stage - 2 : stage;
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			if (p.getAttack().isMoveType(MoveType.AIRBORNE)) {
				b.printAttacking(p);
				Messages.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return false;
			}
			
			return true;
		}
	}

	static class WaterSport extends BattleEffect implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		WaterSport() {
			super(EffectNamesies.WATER_SPORT, 5, 5, false);
		}

		public WaterSport newInstance() {
			return (WaterSport)(new WaterSport().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(), this.namesies));
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getAttackType() == Type.FIRE ? .33 : 1;
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return "Fire's power was weakened!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The effects of Water Sport wore off.";
		}
	}

	static class MudSport extends BattleEffect implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		MudSport() {
			super(EffectNamesies.MUD_SPORT, 5, 5, false);
		}

		public MudSport newInstance() {
			return (MudSport)(new MudSport().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(), this.namesies));
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getAttackType() == Type.ELECTRIC ? .33 : 1;
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return "Electricity's power was weakened!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The effects of Mud Sport wore off.";
		}
	}

	static class WonderRoom extends BattleEffect implements StatSwitchingEffect {
		private static final long serialVersionUID = 1L;

		WonderRoom() {
			super(EffectNamesies.WONDER_ROOM, 5, 5, false);
		}

		public WonderRoom newInstance() {
			return (WonderRoom)(new WonderRoom().activate());
		}

		public Stat switchStat(Stat s) {
			// Defense and Special Defense are swapped
			if (s == Stat.DEFENSE) return Stat.SP_DEFENSE;
			if (s == Stat.SP_DEFENSE) return Stat.DEFENSE;
			return s;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			Effect roomsies = Effect.getEffect(b.getEffects(), this.namesies);
			if (roomsies == null) {
				super.cast(b, caster, victim, source, printCast);
				return;
			}
			
			// Remove the effect if it's already in play
			Messages.addMessage(roomsies.getSubsideMessage(caster));
			Effect.removeEffect(b.getEffects(), this.namesies);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " twisted the dimensions to switch defense and special defense!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The dimensions of the wonder room returned to normal.";
		}
	}

	static class TrickRoom extends BattleEffect {
		private static final long serialVersionUID = 1L;

		TrickRoom() {
			super(EffectNamesies.TRICK_ROOM, 5, 5, false);
		}

		public TrickRoom newInstance() {
			return (TrickRoom)(new TrickRoom().activate());
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			Effect roomsies = Effect.getEffect(b.getEffects(), this.namesies);
			if (roomsies == null) {
				super.cast(b, caster, victim, source, printCast);
				return;
			}
			
			// Remove the effect if it's already in play
			Messages.addMessage(roomsies.getSubsideMessage(caster));
			Effect.removeEffect(b.getEffects(), this.namesies);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " twisted the dimensions to switch speeds!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The dimensions of the trick room returned to normal.";
		}
	}

	static class MagicRoom extends BattleEffect {
		private static final long serialVersionUID = 1L;

		MagicRoom() {
			super(EffectNamesies.MAGIC_ROOM, 5, 5, false);
		}

		public MagicRoom newInstance() {
			return (MagicRoom)(new MagicRoom().activate());
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			Effect roomsies = Effect.getEffect(b.getEffects(), this.namesies);
			if (roomsies == null) {
				super.cast(b, caster, victim, source, printCast);
				return;
			}
			
			// Remove the effect if it's already in play
			Messages.addMessage(roomsies.getSubsideMessage(caster));
			Effect.removeEffect(b.getEffects(), this.namesies);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " twisted the dimensions to prevent using items!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The dimensions of the magic room returned to normal.";
		}
	}

	static class MistyTerrain extends BattleEffect implements StatusPreventionEffect, PowerChangeEffect, TerrainEffect {
		private static final long serialVersionUID = 1L;

		MistyTerrain() {
			super(EffectNamesies.MISTY_TERRAIN, 5, 5, false);
		}

		public MistyTerrain newInstance() {
			return (MistyTerrain)(new MistyTerrain().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(), this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return "Mist swirled around the battlefield!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The mist disappeared from the battlefield.";
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
			// Levitating Pokemon are immune to the mist
			return !victim.isLevitating(b);
		}

		public String statusPreventionMessage(ActivePokemon victim) {
			return "The protective mist prevents status conditions!";
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			// Dragon type moves have halved power during the misty terrain
			return user.getAttackType() == Type.DRAGON && !user.isLevitating(b) ? .5 : 1;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			// Remove all other Terrain Effects
			for (int i = 0; i < b.getEffects().size(); i++) {
				Effect effect = b.getEffects().get(i);
				if (effect instanceof TerrainEffect) {
					b.getEffects().remove(i);
					i--;
				}
			}
			
			super.cast(b, caster, victim, source, printCast);
			b.setTerrainType(TerrainType.MISTY, false); // TODO: Need to send a terrain change message
		}

		public void subside(Battle b, ActivePokemon p) {
			super.subside(b, p);
			b.resetTerrain();
		}
	}

	static class GrassyTerrain extends BattleEffect implements EndTurnEffect, PowerChangeEffect, TerrainEffect {
		private static final long serialVersionUID = 1L;

		GrassyTerrain() {
			super(EffectNamesies.GRASSY_TERRAIN, 5, 5, false);
		}

		public GrassyTerrain newInstance() {
			return (GrassyTerrain)(new GrassyTerrain().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(), this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return "Grass sprouted around the battlefield!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The grass withered and died.";
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (!victim.fullHealth() && !victim.isLevitating(b)) {
				victim.healHealthFraction(1/16.0);
				Messages.addMessage(victim.getName() + " restored some HP due to the Grassy Terrain!", b, victim);
			}
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			// Grass-type moves are 50% stronger with the grassy terrain
			return user.getAttackType() == Type.GRASS && !user.isLevitating(b) ? 1.5 : 1;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			// Remove all other Terrain Effects
			for (int i = 0; i < b.getEffects().size(); i++) {
				Effect effect = b.getEffects().get(i);
				if (effect instanceof TerrainEffect) {
					b.getEffects().remove(i);
					i--;
				}
			}
			
			super.cast(b, caster, victim, source, printCast);
			b.setTerrainType(TerrainType.GRASS, false); // TODO: Need to send a terrain change message
		}

		public void subside(Battle b, ActivePokemon p) {
			super.subside(b, p);
			b.resetTerrain();
		}
	}

	static class ElectricTerrain extends BattleEffect implements StatusPreventionEffect, PowerChangeEffect, TerrainEffect {
		private static final long serialVersionUID = 1L;

		ElectricTerrain() {
			super(EffectNamesies.ELECTRIC_TERRAIN, 5, 5, false);
		}

		public ElectricTerrain newInstance() {
			return (ElectricTerrain)(new ElectricTerrain().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(), this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return "Electricity crackled around the battlefield!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The electricity dissipated.";
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
			return status == StatusCondition.ASLEEP && !victim.isLevitating(b);
		}

		public String statusPreventionMessage(ActivePokemon victim) {
			return "The electric terrain prevents sleep!";
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			// Electric-type moves are 50% stronger with the electric terrain
			return user.getAttackType() == Type.ELECTRIC && !user.isLevitating(b) ? 1.5 : 1;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			// Remove all other Terrain Effects
			for (int i = 0; i < b.getEffects().size(); i++) {
				Effect effect = b.getEffects().get(i);
				if (effect instanceof TerrainEffect) {
					b.getEffects().remove(i);
					i--;
				}
			}
			
			super.cast(b, caster, victim, source, printCast);
			b.setTerrainType(TerrainType.ELECTRIC, false); // TODO: Need to send a terrain change message
		}

		public void subside(Battle b, ActivePokemon p) {
			super.subside(b, p);
			b.resetTerrain();
		}
	}
}
