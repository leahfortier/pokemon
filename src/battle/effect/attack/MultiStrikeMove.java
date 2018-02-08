package battle.effect.attack;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.AttackInterface;
import message.Messages;
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

    @Override
    default void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
        int hits = this.getNumHits(me);

        int hit = 1;
        for (; hit <= hits; hit++) {
            // Stop attacking the dead
            if (o.isFainted(b)) {
                break;
            }

            Messages.add("Hit " + hit + "!");
            AttackInterface.super.applyDamage(me, o, b);
        }

        hit--;

        // Print hits and gtfo
        Messages.add("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!");
    }
}
