package test;

import battle.Battle;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import org.junit.Assert;
import pokemon.ActivePokemon;
import pokemon.Gender;
import pokemon.PokemonNamesies;
import pokemon.ability.AbilityNamesies;
import test.battle.TestBattle;
import util.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class TestPokemon extends ActivePokemon {
    public TestPokemon(final PokemonNamesies pokemon, final boolean isWild, final boolean isPlayer) {
        this(pokemon, 100, isWild, isPlayer);
    }

    public TestPokemon(final PokemonNamesies pokemon, final int level, final boolean isWild, final boolean isPlayer) {
        super(pokemon, level, isWild, isPlayer);
    }

    public TestPokemon withGender(Gender gender) {
        super.setGender(gender);
        return this;
    }

    public TestPokemon withAbility(AbilityNamesies ability) {
        super.setAbility(ability);
        return this;
    }

    public TestPokemon withMoves(AttackNamesies... moves) {
        Assert.assertTrue(moves.length <= Move.MAX_MOVES);
        this.setMoves(
                Arrays.stream(moves)
                      .map(move -> new Move(move.getAttack()))
                      .collect(Collectors.toList())
        );
        return this;
    }

    public void setupMove(AttackNamesies attackNamesies, Battle battle) {
        this.setMove(battle, new Move(attackNamesies));
        this.getAttributes().startAttack(battle);
    }

    public void apply(boolean assertion, AttackNamesies attack, TestBattle battle) {
        ActivePokemon other = battle.getOtherPokemon(this);

        this.setupMove(attack, battle);
        boolean success = this.getAttack().apply(this, other, battle);
        Assert.assertTrue(
                StringUtils.spaceSeparated("Attacking:", this.getName(), "Defending:", other.getName(), attack, assertion, success),
                success == assertion
        );
    }

    public void assertHealthRatio(double fraction) {
        Assert.assertEquals((int)(Math.ceil(fraction*this.getMaxHP())), this.getHP());
    }

    public static TestPokemon newPlayerPokemon(final PokemonNamesies pokemon) {
        return new TestPokemon(pokemon, false, true);
    }

    public static TestPokemon newWildPokemon(final PokemonNamesies pokemon) {
        return new TestPokemon(pokemon, true, false);
    }

    public static TestPokemon newTrainerPokemon(final PokemonNamesies pokemon) {
        return new TestPokemon(pokemon, false, false);
    }
}
