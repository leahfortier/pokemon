package test.battle;

import battle.Stages;
import org.junit.Assert;
import pokemon.PokemonNamesies;
import pokemon.Stat;
import test.TestPokemon;

public class TestStages extends Stages {
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