package test;

import battle.Battle;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.effect.generic.CastSource;
import battle.effect.generic.EffectNamesies;
import item.ItemNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;
import test.TestBattle.PokemonManipulator;
import type.Type;
import type.TypeAdvantage;

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
        changeEffectivenessTest(
                PokemonNamesies.PIDGEY,
                AttackNamesies.EARTHQUAKE,
                (battle, attacking, defending) -> defending.giveItem(ItemNamesies.RING_TARGET)
        );
    }

    @Test
    public void foresightTest() {
        foresightTest(PokemonNamesies.GASTLY, AttackNamesies.TACKLE, EffectNamesies.FORESIGHT);
        foresightTest(PokemonNamesies.UMBREON, AttackNamesies.PSYCHIC, EffectNamesies.MIRACLE_EYE);
    }

    private void foresightTest(PokemonNamesies defendingPokemon, AttackNamesies attack, EffectNamesies effect) {
        changeEffectivenessTest(
                defendingPokemon,
                attack,
                (battle, attacking, defending) -> effect.getEffect().cast(battle, attacking, defending, CastSource.ATTACK, false)
        );
    }

    // TODO: Scrappy
    private void changeEffectivenessTest(PokemonNamesies defendingPokemon, AttackNamesies attack, PokemonManipulator manipulator) {
        ActivePokemon attacking = new TestPokemon(PokemonNamesies.BULBASAUR);
        ActivePokemon defending = new TestPokemon(defendingPokemon);

        Battle battle = TestBattle.create(attacking, defending);

        // Make sure attack is unsuccessful without the effect
        attacking.setMove(new Move(attack));
        Assert.assertTrue(TypeAdvantage.getAdvantage(attacking, defending, battle) == 0);

        // Cast the effect and make sure the move hits
        manipulator.manipulate(battle, attacking, defending);
        Assert.assertTrue(TypeAdvantage.getAdvantage(attacking, defending, battle) > 0);
    }
}
