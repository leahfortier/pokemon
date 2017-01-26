package test;

import battle.Battle;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import item.ItemNamesies;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;
import type.Type;
import type.TypeAdvantage;
import org.junit.Assert;
import org.junit.Test;

public class TypeTest {
    private static final double typeAdvantage[][] = {
            {1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1, .5,  0,  1,  1, .5,  1, 1}, // Normal
            {1, .5, .5,  1,  2,  2,  1,  1,  1,  1,  1,  2, .5,  1, .5,  1,  2,  1, 1}, // Fire
            {1,  2, .5,  1, .5,  1,  1,  1,  2,  1,  1,  1,  2,  1, .5,  1,  1,  1, 1}, // Water
            {1,  1,  2, .5, .5,  1,  1,  1,  0,  2,  1,  1,  1,  1, .5,  1,  1,  1, 1}, // Electric
            {1, .5,  2,  1, .5,  1,  1, .5,  2, .5,  1, .5,  2,  1, .5,  1, .5,  1, 1}, // Grass
            {1, .5, .5,  1,  2, .5,  1,  1,  2,  2,  1,  1,  1,  1,  2,  1, .5,  1, 1}, // Ice
            {2,  1,  1,  1,  1,  2,  1, .5,  1, .5, .5, .5,  2,  0,  1,  2,  2, .5, 1}, // Fighting
            {1,  1,  1,  1,  2,  1,  1, .5, .5,  1,  1,  1, .5, .5,  1,  1,  0,  2, 1}, // Poison
            {1,  2,  1,  2, .5,  1,  1,  2,  1,  0,  1, .5,  2,  1,  1,  1,  2,  1, 1}, // Ground
            {1,  1,  1, .5,  2,  1,  2,  1,  1,  1,  1,  2, .5,  1,  1,  1, .5,  1, 1}, // Flying
            {1,  1,  1,  1,  1,  1,  2,  2,  1,  1, .5,  1,  1,  1,  1,  0, .5,  1, 1}, // Psychic
            {1, .5,  1,  1,  2,  1, .5, .5,  1, .5,  2,  1,  1, .5,  1,  2, .5, .5, 1}, // Bug
            {1,  2,  1,  1,  1,  2, .5,  1, .5,  2,  1,  2,  1,  1,  1,  1, .5,  1, 1}, // Rock
            {0,  1,  1,  1,  1,  1,  1,  1,  1,  1,  2,  1,  1,  2,  1, .5,  1,  1, 1}, // Ghost
            {1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  2,  1, .5,  0, 1}, // Dragon
            {1,  1,  1,  1,  1,  1, .5,  1,  1,  1,  2,  1,  1,  2,  1, .5,  1, .5, 1}, // Dark
            {1, .5, .5, .5,  1,  2,  1,  1,  1,  1,  1,  1,  2,  1,  1,  1, .5,  2, 1}, // Steel
            {1, .5,  1,  1,  1,  1,  2, .5,  1,  1,  1,  1,  1,  1,  2,  2, .5,  1, 1}, // Fairy
            {1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1, 1}}; // No Type

    @Test
    public void typeAdvantageTest() {
        for (Type attacking : Type.values()) {
            TypeAdvantage advantage = attacking.getAdvantage();
            for (int i = 0; i < typeAdvantage[attacking.getIndex()].length; i++) {
                Type defending = Type.values()[i];
                double chartAdv = typeAdvantage[attacking.getIndex()][i];
                double classAdv = advantage.getAdvantage(defending);

                Assert.assertTrue(String.format("%s %s %f %f", attacking, defending, chartAdv, classAdv), chartAdv  == classAdv);
            }
        }
    }

    @Test
    public void ringTargetTest() {
        ActivePokemon attacking = TestUtil.getPokemon(PokemonNamesies.SANDSHREW);
        ActivePokemon defending = TestUtil.getPokemon(PokemonNamesies.PIDGEY);

        Battle battle = TestBattle.create(attacking, defending);

        attacking.setMove(new Move(AttackNamesies.EARTHQUAKE));
        Assert.assertTrue(TypeAdvantage.getAdvantage(attacking, defending, battle) == 0);

        defending.giveItem(ItemNamesies.RING_TARGET);
        Assert.assertTrue(TypeAdvantage.getAdvantage(attacking, defending, battle) > 0);
    }
}
