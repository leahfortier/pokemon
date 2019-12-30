package battle.effect.battle.terrain;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.ApplyResult;
import battle.effect.EffectNamesies;
import battle.effect.InvokeInterfaces.AttackBlocker;
import battle.effect.InvokeInterfaces.BattleEndTurnEffect;
import battle.effect.InvokeInterfaces.EffectPreventionEffect;
import battle.effect.InvokeInterfaces.PowerChangeEffect;
import battle.effect.InvokeInterfaces.StatusPreventionEffect;
import battle.effect.battle.BattleEffect;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.source.CastSource;
import battle.effect.status.StatusNamesies;
import map.overworld.TerrainType;
import type.Type;

public abstract class TerrainEffect extends BattleEffect<TerrainNamesies> implements BattleEndTurnEffect {
    private static final long serialVersionUID = 1L;

    private final TerrainType terrainType;

    public TerrainEffect(TerrainNamesies name, TerrainType terrainType) {
        super(name, 5, 5, false, false);
        this.terrainType = terrainType;
    }

    public TerrainType getTerrainType() {
        return this.terrainType;
    }

    @Override
    public boolean endTurnSubsider() {
        return true;
    }

    // EVERYTHING BELOW IS GENERATED ###

    /**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

    // Dragon type moves have halved power during the misty terrain
    static class MistyTerrain extends TerrainEffect implements StatusPreventionEffect, EffectPreventionEffect, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        MistyTerrain() {
            super(TerrainNamesies.MISTY_TERRAIN, TerrainType.MISTY);
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
        public ApplyResult preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusNamesies status) {
            // Levitating Pokemon can still receive status conditions
            if (victim.isOnTheGround(b)) {
                return ApplyResult.failure("The protective mist prevents status conditions!");
            }

            return ApplyResult.success();
        }

        @Override
        public ApplyResult preventEffect(Battle b, ActivePokemon caster, ActivePokemon victim, EffectNamesies effectName) {
            // No confusion for the groundies
            if (effectName == PokemonEffectNamesies.CONFUSION && victim.isOnTheGround(b)) {
                return ApplyResult.failure("The protective mist prevents confusion!");
            }

            return ApplyResult.success();
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.DRAGON) && victim.isOnTheGround(b) ? .5 : 1;
        }
    }

    // Grass-type moves are 30% stronger with the grassy terrain
    static class GrassyTerrain extends TerrainEffect implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        GrassyTerrain() {
            super(TerrainNamesies.GRASSY_TERRAIN, TerrainType.GRASS);
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
        public void singleEndTurnEffect(Battle b, ActivePokemon victim) {
            if (victim.isOnTheGround(b)) {
                victim.healHealthFraction(1/16.0, b, victim.getName() + " restored some HP due to the Grassy Terrain!");
            }
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.GRASS) && user.isOnTheGround(b) ? 1.3 : 1;
        }
    }

    // Electric-type moves are 30% stronger with the electric terrain
    static class ElectricTerrain extends TerrainEffect implements StatusPreventionEffect, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        ElectricTerrain() {
            super(TerrainNamesies.ELECTRIC_TERRAIN, TerrainType.ELECTRIC);
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
        public ApplyResult preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusNamesies status) {
            if (status == StatusNamesies.ASLEEP && victim.isOnTheGround(b)) {
                return ApplyResult.failure("The electric terrain prevents sleep!");
            }

            return ApplyResult.success();
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.ELECTRIC) && user.isOnTheGround(b) ? 1.3 : 1;
        }
    }

    // Psychic-type moves are 30% stronger with the psychic terrain
    static class PsychicTerrain extends TerrainEffect implements AttackBlocker, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        PsychicTerrain() {
            super(TerrainNamesies.PSYCHIC_TERRAIN, TerrainType.PSYCHIC);
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
        public boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Psychic terrain prevents increased priority moves from hitting
            return b.getAttackPriority(user) > 0 && victim.isOnTheGround(b);
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.PSYCHIC) && user.isOnTheGround(b) ? 1.3 : 1;
        }
    }
}
