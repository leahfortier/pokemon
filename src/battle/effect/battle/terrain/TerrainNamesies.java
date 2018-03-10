package battle.effect.battle.terrain;

import battle.effect.EffectNamesies.BattleEffectNamesies;
import battle.effect.battle.terrain.TerrainEffect.ElectricTerrain;
import battle.effect.battle.terrain.TerrainEffect.GrassyTerrain;
import battle.effect.battle.terrain.TerrainEffect.MistyTerrain;
import battle.effect.battle.terrain.TerrainEffect.PsychicTerrain;

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

