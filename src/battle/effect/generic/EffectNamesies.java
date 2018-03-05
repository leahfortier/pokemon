package battle.effect.generic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface EffectNamesies<EffectType extends Effect> {
    EffectType getEffect();

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

    interface BattleEffectNamesies<EffectType extends BattleEffect> extends EffectNamesies<EffectType> {}
}
