package item.medicine;

import item.hold.HoldItem;
import item.use.PokemonUseItem;
import message.Messages;
import pokemon.ActivePokemon;
import pokemon.Stat;

public interface EvIncreaser extends HoldItem, PokemonUseItem {
    Stat toIncrease();
    int increaseAmount();

    default boolean use(ActivePokemon p) {
        int[] toAdd = new int[Stat.NUM_STATS];
        toAdd[this.toIncrease().index()] += this.increaseAmount();

        if (!p.addEVs(toAdd)) {
            return false;
        }

        Messages.add(p.getName() + "'s " + this.toIncrease().getName() + " was raised!");
        return true;
    }

    interface Vitamin extends EvIncreaser {
        default int increaseAmount() {
            return 10;
        }

        @Override
        default int flingDamage() {
            return 30;
        }
    }

    interface Wing extends EvIncreaser {
        default int increaseAmount() {
            return 1;
        }

        @Override
        default int flingDamage() {
            return 20;
        }
    }
}
