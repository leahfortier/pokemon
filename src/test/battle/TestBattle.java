package test.battle;

import battle.Battle;
import battle.attack.AttackNamesies;
import battle.effect.generic.EffectNamesies;
import battle.effect.generic.Weather;
import org.junit.Assert;
import battle.ActivePokemon;
import pokemon.PokemonNamesies;
import test.TestCharacter;
import test.TestPokemon;
import test.TestUtils;
import trainer.EnemyTrainer;
import trainer.Opponent;
import trainer.Trainer;
import trainer.TrainerAction;
import trainer.WildPokemon;
import util.StringUtils;

public class TestBattle extends Battle {
    private Double expectedDamageModifier;

    private TestBattle(Opponent opponent) {
        super(opponent);

        this.getPlayer().setAction(TrainerAction.FIGHT);

        this.getAttacking().setupMove(AttackNamesies.SPLASH, this);
        this.getDefending().setupMove(AttackNamesies.SPLASH, this);
    }

    private TestBattle(ActivePokemon nahMahBoi) {
        this(new WildPokemon(nahMahBoi));
    }

    @Override
    public double getDamageModifier(ActivePokemon attacking, ActivePokemon defending) {
        double modifier = super.getDamageModifier(attacking, defending);

        Assert.assertTrue(modifier > 0);
        if (expectedDamageModifier != null) {
            TestUtils.assertEquals(
                    StringUtils.spaceSeparated(attacking.getAttack(), attacking.getCount()),
                    expectedDamageModifier, modifier
            );
        }

        return modifier;
    }

    void setExpectedDamageModifier(Double damageModifier) {
        this.expectedDamageModifier = damageModifier;
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

    // Should be everything?
    void clearAllEffects() {
        this.getAttacking().resetAttributes();
        this.getDefending().resetAttributes();

        this.getTrainer(true).resetEffects();
        this.getTrainer(false).resetEffects();

        this.getEffects().clear();
        this.addEffect((Weather)EffectNamesies.CLEAR_SKIES.getEffect());
    }

    void emptyHeal() {
        getAttacking().fullyHeal();
        getDefending().fullyHeal();

        this.fight(AttackNamesies.SPLASH, AttackNamesies.SPLASH);
    }

    void fight(AttackNamesies attackingMove, AttackNamesies defendingMove) {
        getPlayer().setAction(TrainerAction.FIGHT);

        TestPokemon attacking = this.getAttacking();
        TestPokemon defending = this.getDefending();

        attacking.setupMove(attackingMove, this);
        defending.setupMove(defendingMove, this);

        // Player always goes first in tests
        super.fight(true);
    }

    void attackingFight(AttackNamesies attackNamesies) {
        fight(attackNamesies, AttackNamesies.SPLASH);
    }

    void defendingFight(AttackNamesies attackNamesies) {
        fight(AttackNamesies.SPLASH, attackNamesies);
    }

    void falseSwipePalooza(boolean playerAttacking) {
        TestPokemon attacking = playerAttacking ? this.getAttacking() : this.getDefending();
        TestPokemon defending = this.getOtherPokemon(attacking);
        while (defending.getHP() > 1) {
            attacking.apply(true, AttackNamesies.FALSE_SWIPE, this);
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
        enemy.setAction(TrainerAction.FIGHT);

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
