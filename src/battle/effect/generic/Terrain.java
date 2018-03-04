package battle.effect.generic;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.CastSource;
import battle.effect.generic.EffectInterfaces.AttackBlocker;
import battle.effect.generic.EffectInterfaces.BattleEndTurnEffect;
import battle.effect.generic.EffectInterfaces.PowerChangeEffect;
import battle.effect.generic.EffectInterfaces.StatusPreventionEffect;
import battle.effect.status.StatusCondition;
import map.overworld.TerrainType;
import message.MessageUpdate;
import message.Messages;
import type.Type;

public abstract class Terrain extends BattleEffect {
    private final TerrainType terrainType;

    public Terrain(EffectNamesies name, TerrainType terrainType) {
        super(name, 5, 5, false, false);
        this.terrainType = terrainType;
    }

    public TerrainType getTerrainType() {
        return this.terrainType;
    }

    // EVERYTHING BELOW IS GENERATED ###

    /**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

    // Dragon type moves have halved power during the misty terrain
    static class MistyTerrain extends Terrain implements StatusPreventionEffect, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        MistyTerrain() {
            super(EffectNamesies.MISTY_TERRAIN, TerrainType.MISTY);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.hasEffect(this.namesies));
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return "Mist swirled around the battlefield!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The mist disappeared from the battlefield.";
        }

        @Override
        public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
            // Levitating Pokemon are immune to the mist
            return !victim.isLevitating(b);
        }

        @Override
        public String statusPreventionMessage(ActivePokemon victim) {
            return "The protective mist prevents status conditions!";
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.DRAGON) && !user.isLevitating(b) ? .5 : 1;
        }
    }

    // Grass-type moves are 50% stronger with the grassy terrain
    static class GrassyTerrain extends Terrain implements BattleEndTurnEffect, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        GrassyTerrain() {
            super(EffectNamesies.GRASSY_TERRAIN, TerrainType.GRASS);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.hasEffect(this.namesies));
        }

        @Override
        public void singleEndTurnEffect(Battle b, ActivePokemon victim) {
            if (!victim.fullHealth() && !victim.isLevitating(b)) {
                victim.healHealthFraction(1/16.0);
                Messages.add(new MessageUpdate(victim.getName() + " restored some HP due to the Grassy Terrain!").updatePokemon(b, victim));
            }
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return "Grass sprouted around the battlefield!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The grass withered and died.";
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.GRASS) && !user.isLevitating(b) ? 1.5 : 1;
        }
    }

    // Electric-type moves are 50% stronger with the electric terrain
    static class ElectricTerrain extends Terrain implements StatusPreventionEffect, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        ElectricTerrain() {
            super(EffectNamesies.ELECTRIC_TERRAIN, TerrainType.ELECTRIC);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.hasEffect(this.namesies));
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return "Electricity crackled around the battlefield!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The electricity dissipated.";
        }

        @Override
        public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
            return status == StatusCondition.ASLEEP && !victim.isLevitating(b);
        }

        @Override
        public String statusPreventionMessage(ActivePokemon victim) {
            return "The electric terrain prevents sleep!";
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.ELECTRIC) && !user.isLevitating(b) ? 1.5 : 1;
        }
    }

    // Psychic-type moves are 50% stronger with the psychic terrain
    static class PsychicTerrain extends Terrain implements AttackBlocker, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        PsychicTerrain() {
            super(EffectNamesies.PSYCHIC_TERRAIN, TerrainType.PSYCHIC);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.hasEffect(this.namesies));
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return "Psychic energy envelops the battlefield!!!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The psychic energy disappeared.";
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.PSYCHIC) && !user.isLevitating(b) ? 1.5 : 1;
        }

        @Override
        public boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Psychic terrain prevents increased priority moves from hitting
            return b.getAttackPriority(user) > 0 && !victim.isLevitating(b);
        }
    }
}
