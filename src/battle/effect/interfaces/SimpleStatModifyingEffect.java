package battle.effect.interfaces;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.interfaces.InvokeInterfaces.StatModifyingEffect;
import pokemon.Stat;

public interface SimpleStatModifyingEffect extends StatModifyingEffect {
    boolean isModifyStat(Stat s);
    double getModifier();

    default boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
        return true;
    }

    @Override
    default double modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s) {
        if (isModifyStat(s) && canModifyStat(b, p, opp)) {
            return getModifier();
        }

        return 1;
    }
}
