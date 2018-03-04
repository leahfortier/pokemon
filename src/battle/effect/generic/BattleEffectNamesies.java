package battle.effect.generic;

import battle.effect.generic.BattleEffect.FieldUproar;
import battle.effect.generic.BattleEffect.Gravity;
import battle.effect.generic.BattleEffect.MagicRoom;
import battle.effect.generic.BattleEffect.MudSport;
import battle.effect.generic.BattleEffect.TrickRoom;
import battle.effect.generic.BattleEffect.WaterSport;
import battle.effect.generic.BattleEffect.WonderRoom;

import java.util.function.Supplier;

public enum BattleEffectNamesies implements EffectNamesies2 {
    // EVERYTHING BELOW IS GENERATED ###
    GRAVITY(Gravity::new),
    WATER_SPORT(WaterSport::new),
    MUD_SPORT(MudSport::new),
    WONDER_ROOM(WonderRoom::new),
    TRICK_ROOM(TrickRoom::new),
    MAGIC_ROOM(MagicRoom::new),
    FIELD_UPROAR(FieldUproar::new);

    // EVERYTHING ABOVE IS GENERATED ###

    private final Supplier<BattleEffect> effectCreator;

    BattleEffectNamesies(Supplier<BattleEffect> effectCreator) {
        this.effectCreator = effectCreator;
    }

    @Override
    public BattleEffect getEffect() {
        return this.effectCreator.get();
    }
}

