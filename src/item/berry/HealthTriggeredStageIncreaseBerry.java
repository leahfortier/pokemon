package item.berry;

import battle.Battle;
import battle.effect.generic.CastSource;
import pokemon.ActivePokemon;
import pokemon.Stat;

public interface HealthTriggeredStageIncreaseBerry extends HealthTriggeredBerry {
    Stat getStat();

    default double healthTriggerRatio() {
        return 1/4.0;
    }

    default boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
        return user.getAttributes().modifyStage(user, user, 1, this.getStat(), b, source);
    }

    default int naturalGiftPower() {
        return 100;
    }
}
