package battle.effect.attack;

import pokemon.ability.AbilityNamesies;
import pokemon.ActivePokemon;
import util.RandomUtils;

public interface MultiStrikeMove {
    int getMinHits();
    int getMaxHits();

    default int getNumHits(final ActivePokemon attacking) {
        final int minHits = getMinHits();
        final int maxHits = getMaxHits();

        if (attacking.hasAbility(AbilityNamesies.SKILL_LINK)) {
            return maxHits;
        }

        return RandomUtils.getRandomInt(minHits, maxHits);
    }
}
