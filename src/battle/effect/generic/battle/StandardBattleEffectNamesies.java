package battle.effect.generic.battle;

import battle.effect.generic.EffectNamesies.BattleEffectNamesies;
import battle.effect.generic.battle.BattleEffect.FieldUproar;
import battle.effect.generic.battle.BattleEffect.Gravity;
import battle.effect.generic.battle.BattleEffect.MagicRoom;
import battle.effect.generic.battle.BattleEffect.MudSport;
import battle.effect.generic.battle.BattleEffect.TrickRoom;
import battle.effect.generic.battle.BattleEffect.WaterSport;
import battle.effect.generic.battle.BattleEffect.WonderRoom;

import java.util.function.Supplier;

public enum StandardBattleEffectNamesies implements BattleEffectNamesies {
    // EVERYTHING BELOW IS GENERATED ###
    GRAVITY(Gravity::new),
    WATER_SPORT(WaterSport::new),
    MUD_SPORT(MudSport::new),
    WONDER_ROOM(WonderRoom::new),
    TRICK_ROOM(TrickRoom::new),
    MAGIC_ROOM(MagicRoom::new),
    FIELD_UPROAR(FieldUproar::new);

    // EVERYTHING ABOVE IS GENERATED ###

    private final Supplier<BattleEffect<StandardBattleEffectNamesies>> effectCreator;

    StandardBattleEffectNamesies(Supplier<BattleEffect<StandardBattleEffectNamesies>> effectCreator) {
        this.effectCreator = effectCreator;
    }

    @Override
    public BattleEffect<StandardBattleEffectNamesies> getEffect() {
        return this.effectCreator.get();
    }
}

