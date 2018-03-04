package test.battle;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.MoveType;
import battle.effect.attack.MultiTurnMove;
import battle.effect.generic.EffectNamesies;
import battle.effect.generic.WeatherEffect;
import org.junit.Assert;
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
    private Boolean expectedDefendingAccuracyBypass;

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

        Assert.assertTrue(attacking.getAttack().getName(), modifier > 0);
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

        this.getTrainer(true).getEffects().reset();
        this.getTrainer(false).getEffects().reset();

        this.getEffects().reset();
        this.addEffect((WeatherEffect)EffectNamesies.CLEAR_SKIES.getEffect());
    }

    void splashFight() {
        this.fight(AttackNamesies.SPLASH, AttackNamesies.SPLASH);
    }

    void emptyHeal() {
        getAttacking().fullyHeal();
        getDefending().fullyHeal();

        this.splashFight();
    }

    void fight(AttackNamesies attackingMove, AttackNamesies defendingMove) {
        getPlayer().setAction(TrainerAction.FIGHT);

        TestPokemon attacking = this.getAttacking();
        TestPokemon defending = this.getDefending();

        attacking.setupMove(attackingMove, this);
        defending.setupMove(defendingMove, this);

        // Player always goes first in tests
        super.fight();
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

    @Override
    protected int getSpeedStat(ActivePokemon statPokemon, ActivePokemon otherPokemon) {
        // Player always strikes first in its priority bracket in tests
        // Note: THESE VALUES DO NOT REFLECT STAT VALUES AT ALL I JUST WANT THE PLAYER'S TO BE HIGHER
        return statPokemon.isPlayer() ? 1 : 0;
    }

    @Override
    protected boolean accuracyCheck(ActivePokemon me, ActivePokemon o) {
        Boolean bypass = bypassAccuracy(me, o);
        if (bypass != null) {
            Attack attack = me.getAttack();

            // Self-Target, field, and charging moves can't miss
            if (attack.isSelfTargetStatusMove()
                    || attack.isMoveType(MoveType.FIELD)
                    || (attack instanceof MultiTurnMove && ((MultiTurnMove)attack).isCharging())) {
                Assert.assertTrue(bypass);
            } else if (!me.isPlayer()) {
                Assert.assertEquals(this.expectedDefendingAccuracyBypass, bypass);
            }
            return bypass;
        }

        if (!me.isPlayer()) {
            Assert.assertNull(this.expectedDefendingAccuracyBypass);
        }

        // No missing by chance in tests
        return true;
    }

    void setExpectedDefendingAccuracyBypass(Boolean accuracyBypass) {
        this.expectedDefendingAccuracyBypass = accuracyBypass;
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
