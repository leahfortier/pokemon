package battle.effect.attack;

import pokemon.ActivePokemon;

public interface MultiTurnMove {
    boolean chargesFirst();
    String getChargeMessage(ActivePokemon user);

    default boolean semiInvulnerability() {
        return false;
    }

    default boolean isCharging(ActivePokemon user) {
        return !user.getMove().isReady();
    }

    default boolean forceMove(ActivePokemon user) {
        boolean chargesFirst = this.chargesFirst();
        boolean isReady = user.getMove().isReady();

        return (chargesFirst && !isReady) || (!chargesFirst && isReady);
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
