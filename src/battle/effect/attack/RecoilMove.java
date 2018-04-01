package battle.effect.attack;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.AttackInterface;
import battle.effect.InvokeInterfaces.ApplyDamageEffect;
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
            if (user.hasAbility(AbilityNamesies.ROCK_HEAD)) {
                return;
            }

            // Recoil amount must be at least one
            int recoilAmount = (int)Math.max(Math.ceil((double)damage/getDamagePercentageDenominator()), 1);
            user.indirectReduceHealth(b, recoilAmount, false, user.getName() + " was hurt by recoil!");
        }
    }
}
