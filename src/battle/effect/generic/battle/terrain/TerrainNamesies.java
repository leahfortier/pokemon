package battle.effect.generic.battle.terrain;

import battle.effect.generic.battle.terrain.TerrainEffect.ElectricTerrain;
import battle.effect.generic.battle.terrain.TerrainEffect.GrassyTerrain;
import battle.effect.generic.battle.terrain.TerrainEffect.MistyTerrain;
import battle.effect.generic.battle.terrain.TerrainEffect.PsychicTerrain;
import battle.effect.generic.EffectNamesies.BattleEffectNamesies;

import java.util.function.Supplier;

public enum TerrainNamesies implements BattleEffectNamesies {
    // EVERYTHING BELOW IS GENERATED ###
    MISTY_TERRAIN(MistyTerrain::new),
    GRASSY_TERRAIN(GrassyTerrain::new),
    ELECTRIC_TERRAIN(ElectricTerrain::new),
    PSYCHIC_TERRAIN(PsychicTerrain::new);

    // EVERYTHING ABOVE IS GENERATED ###

    private final Supplier<TerrainEffect> effectCreator;

    TerrainNamesies(Supplier<TerrainEffect> effectCreator) {
        this.effectCreator = effectCreator;
    }

    @Override
    public TerrainEffect getEffect() {
        return this.effectCreator.get();
    }
}

