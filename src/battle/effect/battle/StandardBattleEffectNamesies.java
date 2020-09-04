package battle.effect.battle;

import battle.effect.EffectNamesies.BattleEffectNamesies;
import battle.effect.battle.BattleEffect.CorrosiveGas;
import battle.effect.battle.BattleEffect.FieldUproar;
import battle.effect.battle.BattleEffect.Gravity;
import battle.effect.battle.BattleEffect.GuardSplit;
import battle.effect.battle.BattleEffect.JawLocked;
import battle.effect.battle.BattleEffect.MagicRoom;
import battle.effect.battle.BattleEffect.MudSport;
import battle.effect.battle.BattleEffect.PowerSplit;
import battle.effect.battle.BattleEffect.TrickRoom;
import battle.effect.battle.BattleEffect.WaterSport;
import battle.effect.battle.BattleEffect.WonderRoom;

import java.util.function.Supplier;

public enum StandardBattleEffectNamesies implements BattleEffectNamesies {
    // EVERYTHING BELOW IS GENERATED ###
    GRAVITY(Gravity::new),
    WATER_SPORT(WaterSport::new),
    MUD_SPORT(MudSport::new),
    WONDER_ROOM(WonderRoom::new),
    TRICK_ROOM(TrickRoom::new),
    MAGIC_ROOM(MagicRoom::new),
    FIELD_UPROAR(FieldUproar::new),
    POWER_SPLIT(PowerSplit::new),
    GUARD_SPLIT(GuardSplit::new),
    JAW_LOCKED(JawLocked::new),
    CORROSIVE_GAS(CorrosiveGas::new);

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
