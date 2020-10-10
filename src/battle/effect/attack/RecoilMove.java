package battle.effect.attack;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.AttackInterface;
import battle.effect.EffectInterfaces.ApplyDamageEffect;
import pokemon.ability.AbilityNamesies;

public interface RecoilMove extends AttackInterface, ApplyDamageEffect {
    void applyRecoil(Battle b, ActivePokemon user);

    @Override
    default boolean ignoreAbsorbedDamage() {
        // Take (1 damage of) recoil even when damage is absorbed
        return false;
    }

    @Override
    default void onDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
        this.applyRecoil(b, user);
    }

    interface RecoilPercentageMove extends RecoilMove {
        int getDamagePercentageDenominator();

        @Override
        default void applyRecoil(Battle b, ActivePokemon user) {
            if (user.hasAbility(AbilityNamesies.ROCK_HEAD)) {
                return;
            }

            int damageDealt = user.getDamageDealt();
            int denominator = this.getDamagePercentageDenominator();

            // Recoil amount must be at least one
            int recoilAmount = (int)Math.max(Math.ceil((double)damageDealt/denominator), 1);
            user.indirectReduceHealth(b, recoilAmount, false, user.getName() + " was hurt by recoil!");
        }
    }
}
