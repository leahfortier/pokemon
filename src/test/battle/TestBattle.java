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
import pokemon.active.PartyPokemon;
import pokemon.species.PokemonNamesies;
import test.general.TestCharacter;
import test.pokemon.TestPokemon;
import trainer.EnemyTrainer;
import trainer.Opponent;
import trainer.Team;
import trainer.Trainer;
import trainer.TrainerAction;
import trainer.WildPokemon;
import type.Type;

import java.util.List;
import java.util.Set;

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

    public TestPokemon getAttacking() {
        return ((TestPokemon)getPlayer().front());
    }

    public TestPokemon getDefending() {
        return ((TestPokemon)getOpponent().front());
    }

    // Used when there are exactly two player Pokemon -- will return the one that is not out front
    public TestPokemon getOtherAttacking() {
        return this.getOtherTrainerPokemon(true);
    }

    // Used when there are exactly two trainer Pokemon -- will return the one that is not out front
    public TestPokemon getOtherDefending() {
        return this.getOtherTrainerPokemon(false);
    }

    private TestPokemon getOtherTrainerPokemon(boolean isPlayer) {
        // Must be a trainer battle
        Team trainer = this.getTrainer(isPlayer);
        Assert.assertTrue(trainer instanceof Trainer);

        // Should be exactly two Pokemon for this method
        List<PartyPokemon> team = trainer.getTeam();
        Assert.assertEquals(2, team.size());

        // Team cannot be copies of the same Pokemon
        PartyPokemon first = team.get(0);
        PartyPokemon second = team.get(1);
        Assert.assertNotEquals(first, second);

        // Front Pokemon should be one of the team members
        PartyPokemon front = trainer.front();
        Assert.assertTrue(front == first || front == second);

        // Return the Pokemon that is not the front Pokemon
        return (TestPokemon)(front == first ? second : first);
    }

    // Adds an additional Pokemon to the player team and returns the created Pokemon
    public TestPokemon addAttacking(PokemonNamesies pokes) {
        TestPokemon attacking = TestPokemon.newPlayerPokemon(pokes);
        this.getPlayer().addPokemon(attacking);
        return attacking;
    }

    // Adds an additional Pokemon to the defending team (trainer battles only) and returns the created Pokemon
    public TestPokemon addDefending(PokemonNamesies pokes) {
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
    public void clearAllEffects() {
        this.getAttacking().resetAttributes();
        this.getDefending().resetAttributes();

        this.getPlayer().getEffects().reset();
        this.getOpponent().getEffects().reset();

        this.getEffects().reset();
        this.addEffect(WeatherNamesies.CLEAR_SKIES.getEffect());
    }

    public void splashFight() {
        this.fight(AttackNamesies.SPLASH, AttackNamesies.SPLASH);
    }

    public void emptyHeal() {
        getAttacking().fullyHeal();
        getDefending().fullyHeal();

        this.splashFight();
    }

    // Sets up the turn for the trainer to use the item on the Pokemon
    // This does not use the action, still need to manually call fight()
    // Note: If trainerPokemon is not the player's, will crash if not a trainer battle
    public void setItemAction(TestPokemon trainerPokemon, ItemNamesies item) {
        Trainer trainer = (Trainer)this.getTrainer(trainerPokemon);
        trainer.setAction(TrainerAction.ITEM);

        Bag bag = trainer.getBag();
        bag.addItem(item);
        bag.setSelectedBattleItem(item, trainerPokemon);
    }

    public void fight(AttackNamesies attackingMove, AttackNamesies defendingMove) {
        getPlayer().setAction(TrainerAction.FIGHT);

        TestPokemon attacking = this.getAttacking();
        TestPokemon defending = this.getDefending();

        attacking.setMove(new Move(attackingMove));
        defending.setMove(new Move(defendingMove));

        // Player always goes first in tests
        super.fight();
    }

    public void attackingFight(AttackNamesies attackNamesies) {
        fight(attackNamesies, AttackNamesies.SPLASH);
    }

    public void defendingFight(AttackNamesies attackNamesies) {
        fight(AttackNamesies.SPLASH, attackNamesies);
    }

    // Continuously uses False Swipe until the defending Pokemon has 1 HP
    public void falseSwipePalooza(boolean playerAttacking) {
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
        Boolean bypass = super.bypassAccuracy(me, o);
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
        } else if (Set.of(AttackNamesies.BIDE, AttackNamesies.FUTURE_SIGHT, AttackNamesies.DOOM_DESIRE).contains(attack.namesies())) {
            // TODO: Difficult to set this for both moves in a single turn right now and they might be different
            expectedBypass = bypass;
        }

        Assert.assertEquals(attack.getName(), expectedBypass, bypass);
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

    public void assertNoTerrain() {
        Assert.assertFalse(this.getEffects().hasTerrain());
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
        Assert.assertEquals(this.getEffects().getActualWeather().namesies(), weatherNamesies);
    }

    public void assertFront(TestPokemon front) {
        this.assertFront(true, front);
    }

    public void assertNotFront(TestPokemon front) {
        this.assertFront(false, front);
    }

    public void assertFront(boolean isFront, TestPokemon front) {
        Assert.assertEquals(isFront, this.isFront(front));
    }

    public static TestBattle createTrainerBattle(PokemonNamesies attacking, PokemonNamesies defending) {
        TestPokemon mahBoiiiiiii = TestPokemon.newPlayerPokemon(attacking);
        TestPokemon nahMahBoi = TestPokemon.newTrainerPokemon(defending);

        new TestCharacter(mahBoiiiiiii);

        EnemyTrainer enemy = new EnemyTrainer("MUTANT ENEMY", 93, Trainer.MAX_POKEMON, nahMahBoi);
        enemy.setAction(TrainerAction.FIGHT);

        return new TestBattle(enemy);
    }

    public static TestBattle create() {
        return create(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER);
    }

    public static TestBattle create(PokemonNamesies attacking, PokemonNamesies defending) {
        return create(false, attacking, defending);
    }

    public static TestBattle create(boolean isTrainerBattle, PokemonNamesies attacking, PokemonNamesies defending) {
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
