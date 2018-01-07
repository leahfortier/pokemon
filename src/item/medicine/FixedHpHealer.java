package item.medicine;

import pokemon.ActivePokemon;

public interface FixedHpHealer extends HpHealer {
    int getFixedHealAmount(ActivePokemon p);

    default int getAmountHealed(ActivePokemon p) {
        return p.heal(this.getFixedHealAmount(p));
    }
}
