package item.berry;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.interfaces.InvokeInterfaces.DamageTakenEffect;
import battle.effect.source.CastSource;
import pokemon.ability.AbilityNamesies;

public interface HealthTriggeredBerry extends GainableEffectBerry, DamageTakenEffect {
    double healthTriggerRatio();

    @Override
    default void damageTaken(Battle b, ActivePokemon damageTaker) {
        double healthRatio = damageTaker.getHPRatio();
        if (healthRatio <= this.healthTriggerRatio() || (healthRatio <= .5 && damageTaker.hasAbility(AbilityNamesies.GLUTTONY))) {
            if (this.gainBerryEffect(b, damageTaker, CastSource.HELD_ITEM)) {
                this.consumeItem(b, damageTaker);
            }
        }
    }
}
