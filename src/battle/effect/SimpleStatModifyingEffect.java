package battle.effect;

import battle.Battle;
import battle.effect.generic.EffectInterfaces.StatModifyingEffect;
import pokemon.ActivePokemon;
import pokemon.Stat;

public interface SimpleStatModifyingEffect extends StatModifyingEffect {
    boolean isModifyStat(Stat s);
    boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp);
    double getModifier();

    @Override
    default double modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s) {
        if (isModifyStat(s) && canModifyStat(b, p, opp)) {
            return getModifier();
        }

        return 1;
    }
}
