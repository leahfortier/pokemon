package battle.effect.attack;

import battle.ActivePokemon;
import battle.attack.AttackInterface;
import pokemon.ability.AbilityNamesies;
import util.RandomUtils;

public interface MultiStrikeMove extends AttackInterface {
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
