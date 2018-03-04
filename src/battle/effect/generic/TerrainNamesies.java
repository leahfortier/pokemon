package battle.effect.generic;

import battle.effect.generic.EffectNamesies.BattleEffectNamesies;
import battle.effect.generic.TerrainEffect.ElectricTerrain;
import battle.effect.generic.TerrainEffect.GrassyTerrain;
import battle.effect.generic.TerrainEffect.MistyTerrain;
import battle.effect.generic.TerrainEffect.PsychicTerrain;

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

