package test;

import battle.Battle;
import pokemon.ActivePokemon;
import trainer.WildPokemon;

class TestBattle extends Battle {
    private TestBattle(ActivePokemon nahMahBoi) {
        super(new WildPokemon(nahMahBoi));
    }

    static TestBattle create(ActivePokemon mahBoiiiiiii, ActivePokemon nahMahBoi) {
        new TestCharacter(mahBoiiiiiii);
        return new TestBattle(nahMahBoi);
    }

    interface PokemonManipulator {
        void manipulate(Battle battle, ActivePokemon attacking, ActivePokemon defending);
    }
}
