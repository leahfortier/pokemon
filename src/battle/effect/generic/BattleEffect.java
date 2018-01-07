package battle.effect.generic;

import battle.Battle;
import battle.effect.generic.EffectInterfaces.AttackBlocker;
import battle.effect.generic.EffectInterfaces.EndTurnEffect;
import battle.effect.generic.EffectInterfaces.GroundedEffect;
import battle.effect.generic.EffectInterfaces.PowerChangeEffect;
import battle.effect.generic.EffectInterfaces.StageChangingEffect;
import battle.effect.generic.EffectInterfaces.StatSwitchingEffect;
import battle.effect.generic.EffectInterfaces.StatusPreventionEffect;
import battle.effect.generic.EffectInterfaces.SuperDuperEndTurnEffect;
import battle.effect.generic.EffectInterfaces.TerrainEffect;
import battle.effect.status.StatusCondition;
import map.overworld.TerrainType;
import message.MessageUpdate;
import message.Messages;
import pokemon.ActivePokemon;
import pokemon.Stat;
import type.Type;

public abstract class BattleEffect extends Effect {
    private static final long serialVersionUID = 1L;

    public BattleEffect(EffectNamesies name, int minTurns, int maxTurns, boolean nextTurnSubside) {
        super(name, minTurns, maxTurns, nextTurnSubside);
    }

    public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
        if (printCast) {
            Messages.add(getCastMessage(b, caster, victim, source));
        }

        b.addEffect(this);

        Messages.add(new MessageUpdate().updatePokemon(b, caster));
        Messages.add(new MessageUpdate().updatePokemon(b, victim));
    }

    // EVERYTHING BELOW IS GENERATED ###

    /**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

    static class Gravity extends BattleEffect implements GroundedEffect, StageChangingEffect {
        private static final long serialVersionUID = 1L;

        Gravity() {
            super(EffectNamesies.GRAVITY, 5, 5, false);
        }

        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(Effect.hasEffect(b.getEffects(), this.namesies));
        }

        public int adjustStage(Battle b, ActivePokemon p, ActivePokemon opp, Stat s) {
            return s == Stat.EVASION ? -2 : 0;
        }

        public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
            super.cast(b, caster, victim, source, printCast);
            removeLevitation(b, caster);
            removeLevitation(b, victim);
        }

        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return "Gravity intensified!";
        }

        public String getSubsideMessage(ActivePokemon victim) {
            return "The gravity returned to normal.";
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

        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return "Fire's power was weakened!";
        }

        public String getSubsideMessage(ActivePokemon victim) {
            return "The effects of Water Sport wore off.";
        }

        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.FIRE) ? .33 : 1;
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

        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return "Electricity's power was weakened!";
        }

        public String getSubsideMessage(ActivePokemon victim) {
            return "The effects of Mud Sport wore off.";
        }

        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.ELECTRIC) ? .33 : 1;
        }
    }

    static class WonderRoom extends BattleEffect implements StatSwitchingEffect {
        private static final long serialVersionUID = 1L;

        WonderRoom() {
            super(EffectNamesies.WONDER_ROOM, 5, 5, false);
        }

        public Stat getSwitchStat(Battle b, ActivePokemon statPokemon, Stat s) {
            // Defense and Special Defense are swapped
            if (s == Stat.DEFENSE) {
                return Stat.SP_DEFENSE;
            } else if (s == Stat.SP_DEFENSE) {
                return Stat.DEFENSE;
            } else {
                return s;
            }
        }

        public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
            Effect roomsies = Effect.getEffect(b.getEffects(), this.namesies);
            if (roomsies == null) {
                super.cast(b, caster, victim, source, printCast);
                return;
            }

            // Remove the effect if it's already in play
            Messages.add(roomsies.getSubsideMessage(caster));
            Effect.removeEffect(b.getEffects(), this.namesies);
        }

        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
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
            Messages.add(roomsies.getSubsideMessage(caster));
            Effect.removeEffect(b.getEffects(), this.namesies);
        }

        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
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
            Messages.add(roomsies.getSubsideMessage(caster));
            Effect.removeEffect(b.getEffects(), this.namesies);
        }

        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " twisted the dimensions to prevent using items!";
        }

        public String getSubsideMessage(ActivePokemon victim) {
            return "The dimensions of the magic room returned to normal.";
        }
    }

    // Dragon type moves have halved power during the misty terrain
    static class MistyTerrain extends BattleEffect implements StatusPreventionEffect, TerrainEffect, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        MistyTerrain() {
            super(EffectNamesies.MISTY_TERRAIN, 5, 5, false);
        }

        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(Effect.hasEffect(b.getEffects(), this.namesies));
        }

        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
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
            return user.isAttackType(Type.DRAGON) && !user.isLevitating(b) ? .5 : 1;
        }

        public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
            // Remove all other Terrain Effects
            b.getEffects().removeIf(effect -> effect instanceof TerrainEffect);

            super.cast(b, caster, victim, source, printCast);
            b.setTerrainType(TerrainType.MISTY, false);
        }

        public void subside(Battle b, ActivePokemon p) {
            super.subside(b, p);
            b.resetTerrain();
        }
    }

    // Grass-type moves are 50% stronger with the grassy terrain
    static class GrassyTerrain extends BattleEffect implements EndTurnEffect, TerrainEffect, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        GrassyTerrain() {
            super(EffectNamesies.GRASSY_TERRAIN, 5, 5, false);
        }

        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(Effect.hasEffect(b.getEffects(), this.namesies));
        }

        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (!victim.fullHealth() && !victim.isLevitating(b)) {
                victim.healHealthFraction(1/16.0);
                Messages.add(new MessageUpdate(victim.getName() + " restored some HP due to the Grassy Terrain!").updatePokemon(b, victim));
            }
        }

        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return "Grass sprouted around the battlefield!";
        }

        public String getSubsideMessage(ActivePokemon victim) {
            return "The grass withered and died.";
        }

        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.GRASS) && !user.isLevitating(b) ? 1.5 : 1;
        }

        public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
            // Remove all other Terrain Effects
            b.getEffects().removeIf(effect -> effect instanceof TerrainEffect);

            super.cast(b, caster, victim, source, printCast);
            b.setTerrainType(TerrainType.GRASS, false);
        }

        public void subside(Battle b, ActivePokemon p) {
            super.subside(b, p);
            b.resetTerrain();
        }
    }

    // Electric-type moves are 50% stronger with the electric terrain
    static class ElectricTerrain extends BattleEffect implements StatusPreventionEffect, TerrainEffect, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        ElectricTerrain() {
            super(EffectNamesies.ELECTRIC_TERRAIN, 5, 5, false);
        }

        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(Effect.hasEffect(b.getEffects(), this.namesies));
        }

        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
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
            return user.isAttackType(Type.ELECTRIC) && !user.isLevitating(b) ? 1.5 : 1;
        }

        public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
            // Remove all other Terrain Effects
            b.getEffects().removeIf(effect -> effect instanceof TerrainEffect);

            super.cast(b, caster, victim, source, printCast);
            b.setTerrainType(TerrainType.ELECTRIC, false);
        }

        public void subside(Battle b, ActivePokemon p) {
            super.subside(b, p);
            b.resetTerrain();
        }
    }

    // Psychic-type moves are 50% stronger with the psychic terrain
    static class PsychicTerrain extends BattleEffect implements AttackBlocker, TerrainEffect, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        PsychicTerrain() {
            super(EffectNamesies.PSYCHIC_TERRAIN, 5, 5, false);
        }

        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(Effect.hasEffect(b.getEffects(), this.namesies));
        }

        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return "Psychic energy envelops the battlefield!!!";
        }

        public String getSubsideMessage(ActivePokemon victim) {
            return "The psychic energy disappeared.";
        }

        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.PSYCHIC) && !user.isLevitating(b) ? 1.5 : 1;
        }

        public boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Psychic terrain prevents increased priority moves from hitting
            return b.getAttackPriority(user) > 0 && !victim.isLevitating(b);
        }

        public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
            // Remove all other Terrain Effects
            b.getEffects().removeIf(effect -> effect instanceof TerrainEffect);

            super.cast(b, caster, victim, source, printCast);
            b.setTerrainType(TerrainType.PSYCHIC, false);
        }

        public void subside(Battle b, ActivePokemon p) {
            super.subside(b, p);
            b.resetTerrain();
        }
    }

    static class FieldUproar extends BattleEffect implements StatusPreventionEffect, SuperDuperEndTurnEffect {
        private static final long serialVersionUID = 1L;

        FieldUproar() {
            super(EffectNamesies.FIELD_UPROAR, -1, -1, false);
        }

        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(Effect.hasEffect(b.getEffects(), this.namesies));
        }

        public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
            return status == StatusCondition.ASLEEP;
        }

        public String statusPreventionMessage(ActivePokemon victim) {
            return "The uproar prevents sleep!!";
        }

        public boolean theVeryVeryEnd(Battle b, ActivePokemon p) {
            if (b.getTrainer(true).front().hasEffect(EffectNamesies.UPROAR) || b.getTrainer(false).front().hasEffect(EffectNamesies.UPROAR)) {
                return false;
            }

            this.active = false;
            return true;
        }
    }
}
