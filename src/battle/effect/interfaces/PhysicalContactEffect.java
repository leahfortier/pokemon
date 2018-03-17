package battle.effect.interfaces;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.MoveType;
import battle.effect.interfaces.InvokeInterfaces.OpponentApplyDamageEffect;
import pokemon.ability.AbilityNamesies;

public interface PhysicalContactEffect extends OpponentApplyDamageEffect {

    // b: The current battle
    // user: The user of the attack that caused the physical contact
    // victim: The Pokemon that received the physical contact attack
    void contact(Battle b, ActivePokemon user, ActivePokemon victim);

    @Override
    default void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
        // Only apply if physical contact is made
        if (user.getAttack().isMoveType(MoveType.PHYSICAL_CONTACT) && !user.hasAbility(AbilityNamesies.LONG_REACH)) {
            this.contact(b, user, victim);
        }
    }
}
