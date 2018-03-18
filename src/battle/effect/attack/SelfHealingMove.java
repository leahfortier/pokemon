package battle.effect.attack;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.AttackInterface;

public interface SelfHealingMove extends AttackInterface {
    double getHealFraction(Battle b, ActivePokemon victim);

    // Heal yourself!
    default void heal(Battle b, ActivePokemon victim) {
        victim.healHealthFraction(this.getHealFraction(b, victim), b, victim.getName() + "'s health was restored!");
    }
}
