package item.berry;

import item.use.PokemonUseItem;
import message.Messages;
import pokemon.ActivePokemon;
import pokemon.Stat;

public interface EvDecreaseBerry extends Berry, PokemonUseItem {
    Stat toDecrease();

    @Override
    default boolean use(ActivePokemon p) {
        int decreaseIndex = this.toDecrease().index();
        int[] vals = new int[Stat.NUM_STATS];

        // For EVs over 110, the berry will decrease the EV to 100
        if (p.getEV(decreaseIndex) > 110) {
            vals[decreaseIndex] = 100 - p.getEV(decreaseIndex);
        }
        // Otherwise, just decreases by 10
        else {
            vals[decreaseIndex] -= 10;
        }

        if (!p.addEVs(vals)) {
            return false;
        }

        Messages.add(p.getName() + "'s " + this.toDecrease().getName() + " was lowered!");
        return true;
    }

    @Override
    default int naturalGiftPower() {
        return 90;
    }
}
