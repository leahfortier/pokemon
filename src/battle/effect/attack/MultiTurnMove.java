package battle.effect.attack;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.AttackInterface;
import item.ItemNamesies;

public interface MultiTurnMove extends AttackInterface {
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

    default void checkOverrideCharge(Battle b, ActivePokemon attacking) {
        if (this.isCharging() && (!this.requiresCharge(b) || this.checkPowerHerb(b, attacking))) {
            this.switchReady();
        }
    }

    // The Power Herb item allows multi-turn moves that charge first to skip the charge turn -- BUT ONLY ONCE
    default boolean checkPowerHerb(Battle b, ActivePokemon user) {
        if (this.chargesFirst() && !this.semiInvulnerability() && user.isHoldingItem(b, ItemNamesies.POWER_HERB)) {
            user.consumeItem(b);
            return true;
        }

        return false;
    }

    default boolean forceMove() {
        return this.chargesFirst() == this.isCharging();
    }

    @Override
    default void beginAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
        this.switchReady();
        this.checkOverrideCharge(b, attacking);
    }

    @Override
    default void endAttack(Battle b, ActivePokemon attacking, ActivePokemon defending, boolean attackHit, boolean success) {
        if (attackHit && !success) {
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
