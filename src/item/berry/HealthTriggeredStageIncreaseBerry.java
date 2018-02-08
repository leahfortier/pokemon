package item.berry;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.CastSource;
import pokemon.Stat;

public interface HealthTriggeredStageIncreaseBerry extends HealthTriggeredBerry {
    Stat getStat();

    @Override
    default double healthTriggerRatio() {
        return 1/4.0;
    }

    @Override
    default boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
        return user.getStages().modifyStage(user, 1, this.getStat(), b, source);
    }

    @Override
    default int naturalGiftPower() {
        return 100;
    }
}
