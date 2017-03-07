package test;

import battle.Battle;
import battle.attack.AttackNamesies;
import org.junit.Test;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;
import trainer.EnemyTrainer;
import trainer.Opponent;
import trainer.Trainer;
import trainer.TrainerAction;
import trainer.WildPokemon;

class TestBattle extends Battle {
    private TestBattle(Opponent opponent) {
       super(opponent);
    }

    private TestBattle(ActivePokemon nahMahBoi) {
        super(new WildPokemon(nahMahBoi));
    }

    public double getDamageModifier(ActivePokemon attacking, ActivePokemon defending) {
        return super.getDamageModifier(attacking, defending);
    }

    TestPokemon getAttacking() {
        return ((TestPokemon)getPlayer().front());
    }

    TestPokemon getDefending() {
        return ((TestPokemon)getOpponent().front());
    }

    @Override
    protected void printShit() {}

    @Override
    public TestPokemon getOtherPokemon(ActivePokemon pokemon) {
        return (TestPokemon)super.getOtherPokemon(pokemon);
    }

    boolean ableToAttack(AttackNamesies attack, TestPokemon attacking, ActivePokemon defending) {
        attacking.setupMove(attack, this);
        return super.ableToAttack(attacking, defending);
    }

    void emptyHeal() {
        getAttacking().fullyHeal();
        getDefending().fullyHeal();

        this.fight(AttackNamesies.SPLASH, AttackNamesies.SPLASH);
    }

    void fight(AttackNamesies attackingMove, AttackNamesies defendingMove) {
        getPlayer().setAction(TrainerAction.FIGHT);

        getAttacking().setupMove(attackingMove, this);
        getDefending().setupMove(defendingMove, this);
        super.fight(getAttacking(), getDefending());
    }

    void attackingFight(AttackNamesies attackNamesies) {
        fight(attackNamesies, AttackNamesies.SPLASH);
    }

    void defendingFight(AttackNamesies attackNamesies) {
        fight(AttackNamesies.SPLASH, attackNamesies);
    }

    // Moves always hit in tests`
    protected boolean accuracyCheck(ActivePokemon me, ActivePokemon o) {
        return true;
    }

    static TestBattle createTrainerBattle(TestPokemon mahBoiiiiiii, TestPokemon nahMahBoi) {
        new TestCharacter(mahBoiiiiiii);

        EnemyTrainer enemy = new EnemyTrainer("MUTANT ENEMY", 93, Trainer.MAX_POKEMON, nahMahBoi);
        TestBattle testBattle = new TestBattle(enemy);

        mahBoiiiiiii.setupMove(AttackNamesies.SPLASH, testBattle);
        nahMahBoi.setupMove(AttackNamesies.SPLASH, testBattle);

        return testBattle;
    }

    static TestBattle create() {
        return create(new TestPokemon(PokemonNamesies.BULBASAUR), new TestPokemon(PokemonNamesies.CHARMANDER));
    }

    static TestBattle create(PokemonNamesies attacking, PokemonNamesies defending) {
        return create(new TestPokemon(attacking), new TestPokemon(defending));
    }

    static TestBattle create(TestPokemon mahBoiiiiiii, TestPokemon nahMahBoi) {
        new TestCharacter(mahBoiiiiiii);
        TestBattle testBattle = new TestBattle(nahMahBoi);

        mahBoiiiiiii.setupMove(AttackNamesies.SPLASH, testBattle);
        nahMahBoi.setupMove(AttackNamesies.SPLASH, testBattle);

        return testBattle;
    }

    @Test
    public void criticalHitTest() {
        // TODO
    }
}
