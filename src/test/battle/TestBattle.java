package test.battle;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.attack.MoveType;
import battle.effect.EffectNamesies.BattleEffectNamesies;
import battle.effect.attack.MultiTurnMove;
import battle.effect.battle.weather.WeatherNamesies;
import battle.effect.team.TeamEffectNamesies;
import item.ItemNamesies;
import item.bag.Bag;
import org.junit.Assert;
import pokemon.ability.AbilityNamesies;
import pokemon.species.PokemonNamesies;
import test.general.TestCharacter;
import test.pokemon.TestPokemon;
import trainer.EnemyTrainer;
import trainer.Opponent;
import trainer.Trainer;
import trainer.TrainerAction;
import trainer.WildPokemon;
import type.Type;

public class TestBattle extends Battle {
    private static final long serialVersionUID = 1L;

    private TestDamageCalculator damageCalculator;

    private TestBattle(ActivePokemon nahMahBoi) {
        this(new WildPokemon(nahMahBoi));
    }

    private TestBattle(Opponent opponent) {
        super(opponent, new TestDamageCalculator());

        this.damageCalculator = (TestDamageCalculator)super.damageCalculator;

        this.getPlayer().setAction(TrainerAction.FIGHT);

        this.getAttacking().setupMove(AttackNamesies.SPLASH, this);
        this.getDefending().setupMove(AttackNamesies.SPLASH, this);
    }

    TestPokemon getAttacking() {
        return ((TestPokemon)getPlayer().front());
    }

    TestPokemon getDefending() {
        return ((TestPokemon)getOpponent().front());
    }

    // Adds an additional Pokemon to the player team and returns the created Pokemon
    TestPokemon addAttacking(PokemonNamesies pokes) {
        TestPokemon attacking = TestPokemon.newPlayerPokemon(pokes);
        this.getPlayer().addPokemon(attacking);
        return attacking;
    }

    // Adds an additional Pokemon to the defending team (trainer battles only) and returns the created Pokemon
    TestPokemon addDefending(PokemonNamesies pokes) {
        Opponent opponent = this.getOpponent();
        Assert.assertTrue(opponent instanceof EnemyTrainer);

        TestPokemon defending = TestPokemon.newTrainerPokemon(pokes);
        ((EnemyTrainer)opponent).addPokemon(defending);
        return defending;
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

        this.getPlayer().getEffects().reset();
        this.getOpponent().getEffects().reset();

        this.getEffects().reset();
        this.addEffect(WeatherNamesies.CLEAR_SKIES.getEffect());
    }

    void splashFight() {
        this.fight(AttackNamesies.SPLASH, AttackNamesies.SPLASH);
    }

    void emptyHeal() {
        getAttacking().fullyHeal();
        getDefending().fullyHeal();

        this.splashFight();
    }

    // Sets up the turn for the trainer to use the item on the Pokemon
    // This does not use the action, still need to manually call fight()
    // Note: If trainerPokemon is not the player's, will crash if not a trainer battle
    void setItemAction(TestPokemon trainerPokemon, ItemNamesies item) {
        Trainer trainer = (Trainer)this.getTrainer(trainerPokemon);
        trainer.setAction(TrainerAction.ITEM);

        Bag bag = trainer.getBag();
        bag.addItem(item);
        bag.setSelectedBattleItem(item, trainerPokemon);
    }

    void fight(AttackNamesies attackingMove, AttackNamesies defendingMove) {
        getPlayer().setAction(TrainerAction.FIGHT);

        TestPokemon attacking = this.getAttacking();
        TestPokemon defending = this.getDefending();

        attacking.setMove(new Move(attackingMove));
        defending.setMove(new Move(defendingMove));

        // Player always goes first in tests
        super.fight();
    }

    void attackingFight(AttackNamesies attackNamesies) {
        fight(attackNamesies, AttackNamesies.SPLASH);
    }

    void defendingFight(AttackNamesies attackNamesies) {
        fight(AttackNamesies.SPLASH, attackNamesies);
    }

    // Continuously uses False Swipe until the defending Pokemon has 1 HP
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
    protected Boolean bypassAccuracy(ActivePokemon me, ActivePokemon o) {
        Boolean expectedBypass = ((TestPokemon)me).getExpectedAccuracyBypass();

        // Self-Target, field, charging moves, and poisonous toxic can't miss
        // As a convenience not forcing these bypasses to be explicitly set -- but confirms not set to false
        Attack attack = me.getAttack();
        if (attack.isSelfTargetStatusMove()
                || attack.isMoveType(MoveType.FIELD)
                || me.hasAbility(AbilityNamesies.NO_GUARD)
                || o.hasAbility(AbilityNamesies.NO_GUARD)
                || (attack instanceof MultiTurnMove && ((MultiTurnMove)attack).isCharging())
                || (attack.namesies() == AttackNamesies.TOXIC && me.isType(this, Type.POISON))) {
            Assert.assertNotEquals(expectedBypass, false);
            expectedBypass = true;
        }

        Boolean bypass = super.bypassAccuracy(me, o);
        Assert.assertEquals(expectedBypass, bypass);

        return bypass;
    }

    @Override
    public boolean naturalAccuracyCheck(ActivePokemon me, ActivePokemon o) {
        // No missing by chance in tests unless specifically requested to
        return !((TestPokemon)me).shouldFailAccuracy();
    }

    public int getCritStage(ActivePokemon me) {
        return this.damageCalculator.getCritStage(this, me);
    }

    public double getDamageModifier(ActivePokemon me, ActivePokemon o) {
        return this.damageCalculator.getDamageModifier(this, me, o);
    }

    // Confirms the battle has or does not have the specified effect
    public void assertEffect(boolean shouldHave, BattleEffectNamesies effectNamesies) {
        Assert.assertEquals(effectNamesies.name(), shouldHave, this.hasEffect(effectNamesies));
    }

    public void assertHasEffect(BattleEffectNamesies effectNamesies) {
        assertEffect(true, effectNamesies);
    }

    public void assertNoEffect(BattleEffectNamesies effectNamesies) {
        assertEffect(false, effectNamesies);
    }

    public void assertEffect(boolean shouldHave, ActivePokemon member, TeamEffectNamesies effectNamesies) {
        Assert.assertEquals(effectNamesies.name(), shouldHave, this.getTrainer(member).hasEffect(effectNamesies));
    }

    public void assertHasEffect(ActivePokemon member, TeamEffectNamesies effectNamesies) {
        assertEffect(true, member, effectNamesies);
    }

    public void assertNoEffect(ActivePokemon member, TeamEffectNamesies effectNamesies) {
        assertEffect(false, member, effectNamesies);
    }

    public void assertWeather(WeatherNamesies weatherNamesies) {
        Assert.assertTrue(this.isWeather(weatherNamesies));
    }

    public void assertFront(TestPokemon front) {
        Assert.assertTrue(this.isFront(front));
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
        return create(false, attacking, defending);
    }

    static TestBattle create(boolean isTrainerBattle, PokemonNamesies attacking, PokemonNamesies defending) {
        if (isTrainerBattle) {
            return createTrainerBattle(attacking, defending);
        }

        return create(TestPokemon.newPlayerPokemon(attacking), TestPokemon.newWildPokemon(defending));
    }

    private static TestBattle create(TestPokemon mahBoiiiiiii, TestPokemon nahMahBoi) {
        new TestCharacter(mahBoiiiiiii);
        return new TestBattle(nahMahBoi);
    }
}
