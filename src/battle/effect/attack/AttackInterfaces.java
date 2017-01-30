package battle.effect.attack;

import battle.Battle;
import battle.effect.generic.EffectInterfaces.RecoilMove;
import main.Global;
import message.MessageUpdate;
import message.Messages;
import pokemon.ActivePokemon;
import pokemon.ability.AbilityNamesies;

public final class AttackInterfaces {
    // Class to hold interfaces -- should not be instantiated
    private AttackInterfaces() {
        Global.error(this.getClass().getSimpleName() + " class cannot be instantiated.");
    }

    public interface RecoilPercentageMove extends RecoilMove {
        int getDamagePercentageDenominator();

        @Override
        default void applyRecoil(Battle b, ActivePokemon user, int damage) {
            if (user.hasAbility(AbilityNamesies.ROCK_HEAD) || user.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
                return;
            }

            Messages.add(new MessageUpdate(user.getName() + " was hurt by recoil!"));
            user.reduceHealth(b, (int)Math.ceil((double)damage/getDamagePercentageDenominator()), false);
        }
    }
}
