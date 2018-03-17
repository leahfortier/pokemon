package battle.effect.attack;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.AttackInterface;
import battle.attack.Move;
import battle.effect.interfaces.InvokeInterfaces.ForceMoveEffect;
import item.ItemNamesies;
import item.hold.HoldItem;

public interface MultiTurnMove extends AttackInterface, ForceMoveEffect {
    boolean chargesFirst();
    String getChargeMessage(ActivePokemon user);

    boolean isCharging();
    void resetReady();
    void switchReady();

    default boolean semiInvulnerability() {
        return false;
    }

    default boolean requiresCharge(Battle b) {
        return true;
    }

    // The Power Herb item allows multi-turn moves that charge first to skip the charge turn -- BUT ONLY ONCE
    default boolean checkPowerHerb(Battle b, ActivePokemon user) {
        HoldItem item = user.getHeldItem(b);
        if (item.namesies() == ItemNamesies.POWER_HERB && this.chargesFirst() && !this.semiInvulnerability()) {
            item.consumeItem(b, user);
            return true;
        }

        return false;
    }

    @Override
    default Move getForcedMove(ActivePokemon attacking) {
        if (this.chargesFirst() == this.isCharging()) {
            return attacking.getMove();
        }

        return null;
    }

    @Override
    default void beginAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
        this.switchReady();
        if (this.isCharging() && (!this.requiresCharge(b) || this.checkPowerHerb(b, attacking))) {
            this.switchReady();
        }
    }

    @Override
    default void totalAndCompleteFailure(Battle b, ActivePokemon attacking, ActivePokemon defending) {
        if (this.isCharging()) {
            this.switchReady();
        }
    }

    @Override
    default boolean shouldApplyDamage(Battle b, ActivePokemon user) {
        // Multi-turn moves default to no damage on the charging turn
        return AttackInterface.super.shouldApplyDamage(b, user) && !this.isCharging();
    }

    @Override
    default boolean shouldApplyEffects(Battle b, ActivePokemon user) {
        // Multi-turn moves default to no effects on the charging turn
        return AttackInterface.super.shouldApplyEffects(b, user) && !this.isCharging();
    }

    interface ChargingMove extends MultiTurnMove {
        @Override
        default boolean chargesFirst() {
            return true;
        }
    }

    interface RechargingMove extends MultiTurnMove {
        @Override
        default boolean chargesFirst() {
            return false;
        }

        @Override
        default String getChargeMessage(ActivePokemon user) {
            return user.getName() + " must recharge!";
        }
    }
}
