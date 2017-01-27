package test;

import battle.Battle;
import battle.attack.AttackNamesies;
import pokemon.ActivePokemon;
import trainer.WildPokemon;

class TestBattle extends Battle {
    private TestBattle(ActivePokemon nahMahBoi) {
        super(new WildPokemon(nahMahBoi));
    }

    public double getDamageModifier(ActivePokemon attacking, ActivePokemon defending) {
        return super.getDamageModifier(attacking, defending);
    }

    public boolean ableToAttack(AttackNamesies attack, TestPokemon attacking, ActivePokemon defending) {
        attacking.setupMove(attack, this, defending);
        return super.ableToAttack(attacking, defending);
    }

    static TestBattle create(ActivePokemon mahBoiiiiiii, ActivePokemon nahMahBoi) {
        new TestCharacter(mahBoiiiiiii);
        return new TestBattle(nahMahBoi);
    }
}
