package battle.effect.attack;

import main.Global;
import pokemon.ability.AbilityNamesies;
import pokemon.ActivePokemon;

public interface MultiStrikeMove {
    int getMinHits();
    int getMaxHits();

    default int getNumHits(final ActivePokemon attacking) {
        final int minHits = getMinHits();
        final int maxHits = getMaxHits();

        if (attacking.hasAbility(AbilityNamesies.SKILL_LINK)) {
            return maxHits;
        }

        return Global.getRandomInt(minHits, maxHits);
    }
}
