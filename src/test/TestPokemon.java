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

public class TestPokemon extends ActivePokemon {
    public TestPokemon(final PokemonNamesies pokemon) {
        super(pokemon, 100, false, false);
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
        // TODO: This shouldn't work I hate this it should be immutable
        this.getActualMoves().clear();

        Assert.assertTrue(moves.length <= Move.MAX_MOVES);
        for (AttackNamesies move : moves) {
            this.getActualMoves().add(new Move(move));
        }

        return this;
    }

    public void setupMove(AttackNamesies attackNamesies, Battle battle) {
        this.setMove(new Move(attackNamesies));
        this.startAttack(battle);
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
}
