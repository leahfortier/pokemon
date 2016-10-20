package battle.effect.attack;

import main.Namesies;
import pokemon.ActivePokemon;

public interface MultiStrikeMove {
    int getMinHits();
    int getMaxHits();

    default int getNumHits(final ActivePokemon attacking) {
        final int minHits = getMinHits();
        final int maxHits = getMaxHits();

        if (attacking.hasAbility(Namesies.SKILL_LINK_ABILITY)) {
            return maxHits;
        }

        return (int)(Math.random()*(maxHits - minHits + 1)) + minHits;
    }
}
