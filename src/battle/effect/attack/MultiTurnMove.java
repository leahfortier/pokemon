package battle.effect.attack;

import battle.ActivePokemon;
import battle.Battle;
import item.ItemNamesies;

public interface MultiTurnMove {
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
