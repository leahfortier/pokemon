package test.battle;

import battle.Battle;
import battle.attack.AttackNamesies;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;
import test.TestCharacter;
import test.TestPokemon;
import trainer.EnemyTrainer;
import trainer.Opponent;
import trainer.Trainer;
import trainer.TrainerAction;
import trainer.WildPokemon;

public class TestBattle extends Battle {
    private TestBattle(Opponent opponent) {
        super(opponent);

        this.getAttacking().setupMove(AttackNamesies.SPLASH, this);
        this.getDefending().setupMove(AttackNamesies.SPLASH, this);
    }

    private TestBattle(ActivePokemon nahMahBoi) {
        this(new WildPokemon(nahMahBoi));
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

    void falseSwipePalooza() {
        TestPokemon attacking = getAttacking();
        TestPokemon defending = getDefending();
        while (attacking.getHP() > 1) {
            defending.apply(true, AttackNamesies.FALSE_SWIPE, this);
        }
    }

    // Moves always hit in tests
    @Override
    protected boolean accuracyCheck(ActivePokemon me, ActivePokemon o) {
        return true;
    }

    static TestBattle createTrainerBattle(PokemonNamesies attacking, PokemonNamesies defending) {
        TestPokemon mahBoiiiiiii = TestPokemon.newPlayerPokemon(attacking);
        TestPokemon nahMahBoi = TestPokemon.newTrainerPokemon(defending);

        new TestCharacter(mahBoiiiiiii);

        EnemyTrainer enemy = new EnemyTrainer("MUTANT ENEMY", 93, Trainer.MAX_POKEMON, nahMahBoi);
        return new TestBattle(enemy);
    }

    static TestBattle create() {
        return create(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER);
    }

    static TestBattle create(PokemonNamesies attacking, PokemonNamesies defending) {
        return create(TestPokemon.newPlayerPokemon(attacking), TestPokemon.newWildPokemon(defending));
    }

    static TestBattle create(TestPokemon mahBoiiiiiii, TestPokemon nahMahBoi) {
        new TestCharacter(mahBoiiiiiii);
        return new TestBattle(nahMahBoi);
    }
}
