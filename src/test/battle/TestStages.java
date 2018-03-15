package test.battle;

import battle.Stages;
import org.junit.Assert;
import pokemon.Stat;
import pokemon.species.PokemonNamesies;
import test.TestPokemon;

public class TestStages extends Stages {
    private static final long serialVersionUID = 1L;

    public TestStages() {
        super(TestPokemon.newPlayerPokemon(PokemonNamesies.BULBASAUR));
    }

    public TestStages set(Stat s, int stage) {
        super.setStage(s, stage);
        return this;
    }

    public void test(TestPokemon stagee) {
        for (Stat stat : Stat.BATTLE_STATS) {
            Assert.assertEquals(stat.getName(), this.getStage(stat), stagee.getStage(stat));
        }
    }
}
