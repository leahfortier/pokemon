package item.berry;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.source.CastSource;
import battle.stages.StageModifier;
import pokemon.stat.Stat;

public interface HealthTriggeredStageIncreaseBerry extends HealthTriggeredBerry {
    Stat getStat();

    @Override
    default double healthTriggerRatio() {
        return 1/4.0;
    }

    @Override
    default boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
        return new StageModifier(1, this.getStat()).modify(b, user, user, source);
    }

    @Override
    default int naturalGiftPower() {
        return 100;
    }

    @Override
    default int getHarvestHours() {
        return 72;
    }
}
