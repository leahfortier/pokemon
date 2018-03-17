package battle.effect.interfaces;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.Attack;
import battle.attack.MoveType;
import battle.effect.interfaces.InvokeInterfaces.AttackBlocker;
import battle.effect.interfaces.InvokeInterfaces.CrashDamageMove;

public interface ProtectingEffect extends AttackBlocker {
    default void protectingEffects(Battle b, ActivePokemon p, ActivePokemon opp) {}

    default boolean protectingCondition(Battle b, ActivePokemon attacking) {
        return true;
    }

    @Override
    default boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
        Attack attack = user.getAttack();
        return protectingCondition(b, user) && !attack.isSelfTargetStatusMove() && !attack.isMoveType(MoveType.FIELD) && !attack.isMoveType(MoveType.PROTECT_PIERCING);
    }

    @Override
    default void alternateEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
        CrashDamageMove.invokeCrashDamageMove(b, user);
        this.protectingEffects(b, user, victim);
    }

    @Override
    default String getBlockMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
        return victim.getName() + " is protecting itself!";
    }
}
