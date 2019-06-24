package test;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.status.StatusNamesies;
import item.ItemNamesies;
import org.junit.Assert;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import pokemon.active.Gender;
import pokemon.active.IndividualValues;
import pokemon.active.MoveList;
import pokemon.species.PokemonNamesies;
import test.battle.TestBattle;
import test.battle.TestStages;
import util.string.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class TestPokemon extends ActivePokemon {
    private static final long serialVersionUID = 1L;

    private Double expectedDamageModifier;

    public TestPokemon(final PokemonNamesies pokemon, final boolean isWild, final boolean isPlayer) {
        this(pokemon, 100, isWild, isPlayer);
    }

    public TestPokemon(final PokemonNamesies pokemon, final int level, final boolean isWild, final boolean isPlayer) {
        super(pokemon, level, isWild, isPlayer);
    }

    public TestPokemon withIVs(int[] IVs) {
        super.setIVs(new IndividualValues(IVs));
        return this;
    }

    public TestPokemon withGender(Gender gender) {
        super.setGender(gender);
        return this;
    }

    public TestPokemon withAbility(AbilityNamesies ability) {
        super.setAbility(ability);
        return this;
    }

    public TestPokemon withItem(ItemNamesies item) {
        super.giveItem(item);
        return this;
    }

    public TestPokemon withMoves(AttackNamesies... moves) {
        Assert.assertTrue(moves.length <= MoveList.MAX_MOVES);
        this.setMoves(
                Arrays.stream(moves)
                      .map(Move::new)
                      .collect(Collectors.toList())
        );
        return this;
    }

    @Override
    public void resetAttributes() {
        super.resetAttributes();
        this.setExpectedDamageModifier(null);
    }

    public void setExpectedDamageModifier(Double damageModifier) {
        this.expectedDamageModifier = damageModifier;
    }

    public Double getExpectedDamageModifier() {
        return this.expectedDamageModifier;
    }

    public void setupMove(AttackNamesies attackNamesies, Battle battle) {
        this.setMove(new Move(attackNamesies));
        this.startAttack(battle);
        this.getAttack().beginAttack(battle, this, battle.getOtherPokemon(this));
    }

    public void apply(boolean assertion, AttackNamesies attack, TestBattle battle) {
        ActivePokemon other = battle.getOtherPokemon(this);

        this.setupMove(attack, battle);
        boolean success = this.getAttack().apply(this, other, battle);
        Assert.assertEquals(
                StringUtils.spaceSeparated("Attacking:", this.getName(), "Defending:", other.getName(), attack, assertion, success),
                assertion, success
        );
    }

    public void assertFullHealth() {
        Assert.assertTrue(this.getHpString(), this.fullHealth());
    }

    public void assertNotFullHealth() {
        Assert.assertFalse(this.getHpString(), this.fullHealth());
    }

    public void assertHealthRatio(double fraction) {
        this.assertHealthRatio(fraction, 0);
    }

    public void assertHealthRatio(double fraction, int errorHp) {
        int hpFraction = (int)(Math.ceil(fraction*this.getMaxHP()));
        Assert.assertTrue(
                StringUtils.spaceSeparated(fraction, this.getHPRatio(), hpFraction, this.getHpString(), errorHp),
                hpFraction >= this.getHP() - errorHp && hpFraction <= this.getHP() + errorHp
        );
    }

    public void assertHealthRatioDiff(int prevHp, double fractionDiff) {
        Assert.assertEquals(prevHp - (int)(fractionDiff*this.getMaxHP()), this.getHP());
    }

    public void assertMissingHp(int missingHp) {
        Assert.assertEquals(this.getHpString() + " " + missingHp, this.getMaxHP() - missingHp, this.getHP());
    }

    // Confirms the Pokemon has or does not have the specified status
    public void assertStatus(boolean shouldHave, StatusNamesies statusNamesies) {
        if (shouldHave) {
            this.assertHasStatus(statusNamesies);
        } else {
            this.assertNoStatus();
        }
    }

    // Confirms the Pokemon does not have any status condition
    public void assertNoStatus() {
        Assert.assertFalse(this.hasStatus());
    }

    // Confirms the Pokemon has the specified status condition
    public void assertHasStatus(StatusNamesies statusNamesies) {
        switch (statusNamesies) {
            case POISONED:
                assertRegularPoison();
                break;
            case BADLY_POISONED:
                assertBadPoison();
                break;
            default:
                Assert.assertTrue(this.getStatus().getShortName(), this.hasStatus(statusNamesies));
                break;
        }
    }

    // Asserts that the Poke is poisoned, but not badddlllyyy poisoned
    public void assertRegularPoison() {
        Assert.assertTrue(this.hasStatus(StatusNamesies.POISONED));
        Assert.assertFalse(this.hasStatus(StatusNamesies.BADLY_POISONED));
    }

    public void assertBadPoison() {
        Assert.assertTrue(this.hasStatus(StatusNamesies.POISONED));
        Assert.assertTrue(this.hasStatus(StatusNamesies.BADLY_POISONED));
    }

    public void assertNoStages() {
        this.assertStages(new TestStages());
    }

    public void assertStages(TestStages testStages) {
        for (Stat stat : Stat.BATTLE_STATS) {
            Assert.assertEquals(stat.getName(), testStages.get(stat), this.getStage(stat));
        }
    }

    // Confirms the Pokemon has the specified effect
    public void assertHasEffect(PokemonEffectNamesies effectNamesies) {
        assertEffect(true, effectNamesies);
    }

    // Confirms the Pokemon does not have the specified effect
    public void assertNoEffect(PokemonEffectNamesies effectNamesies) {
        assertEffect(false, effectNamesies);
    }

    // Confirms the Pokemon has or does not have the specified effect
    public void assertEffect(boolean shouldHave, PokemonEffectNamesies effectNamesies) {
        Assert.assertEquals(effectNamesies.name(), shouldHave, this.hasEffect(effectNamesies));
    }

    // Either checks consumed or not consumed
    public void assertExpectedConsumedItem(Battle battle, boolean shouldConsume) {
        if (shouldConsume) {
            this.assertConsumedItem(battle, false);
        } else {
            this.assertNotConsumedItem(battle);
        }
    }

    // Confirms that the Pokemon is not holding an item and has the consumed item effect
    // Explicitly confirms that a berry was not consumed in the process (use assertConsumedBerry in this case)
    public void assertConsumedItem(Battle battle) {
        this.assertConsumedItem(battle, false);
    }

    // Confirms that the Pokemon is not holding an item and has the consumed item and berry effects
    public void assertConsumedBerry(Battle battle) {
        this.assertConsumedItem(battle, true);
    }

    private void assertConsumedItem(Battle battle, boolean berry) {
        Assert.assertFalse(this.isHoldingItem(battle));
        this.assertHasEffect(PokemonEffectNamesies.CONSUMED_ITEM);
        this.assertEffect(berry, PokemonEffectNamesies.EATEN_BERRY);
    }

    // Confirms that the Pokemon is still holding an item and does not have the consumed item/berry effect
    public void assertNotConsumedItem(Battle battle) {
        Assert.assertTrue(this.isHoldingItem(battle));
        this.assertNoEffect(PokemonEffectNamesies.CONSUMED_ITEM);
        this.assertNoEffect(PokemonEffectNamesies.EATEN_BERRY);
    }

    public static TestPokemon newPlayerPokemon(final PokemonNamesies pokemon) {
        return new TestPokemon(pokemon, false, true);
    }

    public static TestPokemon newWildPokemon(final PokemonNamesies pokemon) {
        return new TestPokemon(pokemon, true, false);
    }

    public static TestPokemon newTrainerPokemon(final PokemonNamesies pokemon) {
        return new TestPokemon(pokemon, false, false);
    }
}
