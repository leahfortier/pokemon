package battle.effect.generic;

import battle.effect.generic.battle.BattleEffect;
import battle.effect.generic.battle.StandardBattleEffectNamesies;
import battle.effect.generic.battle.terrain.TerrainNamesies;
import battle.effect.generic.battle.weather.WeatherNamesies;
import battle.effect.generic.pokemon.PokemonEffectNamesies;
import battle.effect.generic.team.TeamEffectNamesies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface EffectNamesies {
    Effect getEffect();

    // TODO: Test that this is all of them
    static List<EffectNamesies> values() {
        List<EffectNamesies> values = new ArrayList<>();
        Collections.addAll(values, PokemonEffectNamesies.values());
        Collections.addAll(values, TeamEffectNamesies.values());
        Collections.addAll(values, StandardBattleEffectNamesies.values());
        Collections.addAll(values, WeatherNamesies.values());
        Collections.addAll(values, TerrainNamesies.values());
        return values;
    }

    interface BattleEffectNamesies extends EffectNamesies {
        @Override
        BattleEffect<? extends BattleEffectNamesies> getEffect();
    }
}
