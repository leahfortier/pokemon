package battle.effect.generic;

import battle.Battle;
import battle.attack.MoveType;
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
import message.MessageUpdate;
import message.Messages;
import pokemon.ActivePokemon;
import pokemon.Stat;

public abstract class BattleEffect extends Effect {
	private static final long serialVersionUID = 1L;
	public BattleEffect(EffectNamesies name, int minTurns, int maxTurns, boolean nextTurnSubside) {
		super(name, minTurns, maxTurns, nextTurnSubside);
	}

	public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
		if (printCast) {
			Messages.add(new MessageUpdate(getCastMessage(b, caster, victim)));
		}

		b.addEffect(this);

		Messages.add(new MessageUpdate().updatePokemon(b, caster));
		Messages.add(new MessageUpdate().updatePokemon(b, victim));
	}

	// EVERYTHING BELOW IS GENERATED ###
	/**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

	static class Gravity extends BattleEffect implements GroundedEffect, StageChangingEffect, BeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		Gravity() {
			super(EffectNamesies.GRAVITY, 5, 5, false);
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
				Messages.add(new MessageUpdate(p.getName() + " fell to the ground!"));
			}
			
			LevitationEffect.falllllllll(b, p);
		}

		public int adjustStage(Battle b,  ActivePokemon p, ActivePokemon opp, Stat s, int stage) {
			return s == Stat.EVASION ? stage - 2 : stage;
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			if (p.getAttack().isMoveType(MoveType.AIRBORNE)) {
				b.printAttacking(p);
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
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
			Messages.add(new MessageUpdate(roomsies.getSubsideMessage(caster)));
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

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			Effect roomsies = Effect.getEffect(b.getEffects(), this.namesies);
			if (roomsies == null) {
				super.cast(b, caster, victim, source, printCast);
				return;
			}
			
			// Remove the effect if it's already in play
			Messages.add(new MessageUpdate(roomsies.getSubsideMessage(caster)));
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

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			Effect roomsies = Effect.getEffect(b.getEffects(), this.namesies);
			if (roomsies == null) {
				super.cast(b, caster, victim, source, printCast);
				return;
			}
			
			// Remove the effect if it's already in play
			Messages.add(new MessageUpdate(roomsies.getSubsideMessage(caster)));
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
				Messages.add(new MessageUpdate(victim.getName() + " restored some HP due to the Grassy Terrain!").updatePokemon(b, victim));
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

	static class PsychicTerrain extends BattleEffect implements BeforeTurnEffect, PowerChangeEffect, TerrainEffect {
		private static final long serialVersionUID = 1L;

		PsychicTerrain() {
			super(EffectNamesies.PSYCHIC_TERRAIN, 5, 5, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(), this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return "Psychic energy evelops the battlefield!!!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The psychic energy disappeared.";
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			// TODO: Generalize this pattern
			if (p.getAttack().getPriority(b, p) > 0 && !opp.isLevitating(b)) {
				b.printAttacking(p);
				Messages.add(new MessageUpdate(DEFAULT_FAIL_MESSAGE));
				return false;
			}
			
			return true;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			// Psychic-type moves are 50% stronger with the psychic terrain
			return user.getAttackType() == Type.PSYCHIC && !user.isLevitating(b) ? 1.5 : 1;
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
			b.setTerrainType(TerrainType.PSYCHIC, false); // TODO: Need to send a terrain change message
		}

		public void subside(Battle b, ActivePokemon p) {
			super.subside(b, p);
			b.resetTerrain();
		}
	}
}
