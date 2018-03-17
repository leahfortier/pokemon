package battle.effect.attack;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.AttackInterface;
import battle.effect.InvokeInterfaces.ApplyDamageEffect;
import message.Messages;
import pokemon.ability.AbilityNamesies;

public interface RecoilMove extends AttackInterface, ApplyDamageEffect {
    void applyRecoil(Battle b, ActivePokemon user, int damage);

    @Override
    default void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
        this.applyRecoil(b, user, damage);
    }

    interface RecoilPercentageMove extends RecoilMove {
        int getDamagePercentageDenominator();

        @Override
        default void applyRecoil(Battle b, ActivePokemon user, int damage) {
            if (user.hasAbility(AbilityNamesies.ROCK_HEAD) || user.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
                return;
            }

            Messages.add(user.getName() + " was hurt by recoil!");
            user.reduceHealth(b, (int)Math.ceil((double)damage/getDamagePercentageDenominator()), false);
        }
    }
}
