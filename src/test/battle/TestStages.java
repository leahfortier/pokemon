package test.battle;

import battle.Stages;
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

    public int[] get() {
        int[] stages = new int[Stat.NUM_BATTLE_STATS];
        for (Stat stat : Stat.BATTLE_STATS) {
            stages[stat.index()] = this.getStage(stat);
        }
        return stages;
    }
}
