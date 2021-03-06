package test.pokemon;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.effect.EffectInterfaces.PokemonHolder;
import battle.effect.InvokeInterfaces.ItemBlockerEffect;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.status.StatusNamesies;
import battle.stages.Stages;
import item.ItemNamesies;
import main.Game;
import org.junit.Assert;
import pokemon.ability.AbilityNamesies;
import pokemon.active.Gender;
import pokemon.active.IndividualValues;
import pokemon.active.MoveList;
import pokemon.species.PokemonInfo;
import pokemon.species.PokemonNamesies;
import pokemon.stat.Stat;
import test.battle.TestBattle;
import test.battle.TestStages;
import test.general.TestUtils;
import trainer.TrainerType;
import type.Type;
import util.string.StringAppender;
import util.string.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class TestPokemon extends ActivePokemon {
    private static final long serialVersionUID = 1L;

    private Double expectedDamageModifier;
    private Type expectedAttackType;
    private Boolean expectedAccuracyBypass;
    private boolean failNaturalAccuracy;

    public TestPokemon(final PokemonNamesies pokemon, final TrainerType trainerType) {
        this(pokemon, 100, trainerType);
    }

    public TestPokemon(final PokemonNamesies pokemon, final int level, final TrainerType trainerType) {
        super(pokemon, level, trainerType);

        // Test Pokemon need to have their ability explicitly changed
        this.setAbility(AbilityNamesies.NO_ABILITY);
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
        this.setAbility(ability);
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
    public void setAbility(AbilityNamesies ability) {
        super.setAbility(ability);

        PokemonInfo pokemonInfo = this.getPokemonInfo();
        AbilityNamesies[] abilities = pokemonInfo.getAbilities();
        int abilityIndex = this.getAbilityIndex();
        String message = StringUtils.spaceSeparated(this.getActualName(), abilityIndex, abilities);

        // Make sure ability index is always in valid range (regardless if this Pokemon can have this ability)
        TestUtils.assertInclusiveRange(message, 0, 2, abilityIndex);

        // If this Pokemon can only have 2 possible abilities, then it can never have the third ability index
        if (abilities.length == 2) {
            Assert.assertNotEquals(message, 2, this.getAbilityIndex());
        }

        // If this pokemon can naturally receive this ability, make sure the index is correct
        if (pokemonInfo.hasAbility(ability) && abilities.length != 1) {
            Assert.assertEquals(message, ability, abilities[abilityIndex]);
        }
    }

    @Override
    public void resetAttributes() {
        super.resetAttributes();
        this.setExpectedDamageModifier(null);
    }

    public void setExpectedDamageModifier(Double damageModifier) {
        this.expectedDamageModifier = damageModifier;
    }

    public void setExpectedAttackType(Type attackType) {
        this.expectedAttackType = attackType;
    }

    public void setExpectedAccuracyBypass(Boolean bypass) {
        this.expectedAccuracyBypass = bypass;
    }

    // Will make it so the attack will miss (unless a bypass effect forces a hit)
    public void setFailAccuracy(boolean failAccuracy) {
        this.failNaturalAccuracy = failAccuracy;
    }

    public Double getExpectedDamageModifier() {
        return this.expectedDamageModifier;
    }

    public Type getExpectedAttackType() {
        return this.expectedAttackType;
    }

    public Boolean getExpectedAccuracyBypass() {
        return this.expectedAccuracyBypass;
    }

    public boolean shouldFailAccuracy() {
        return this.failNaturalAccuracy;
    }

    public String statsString() {
        return new StringAppender()
                .appendJoin(" ", Stat.STATS, stat -> String.valueOf(this.getStat(stat)))
                .toString();
    }

    public String stagesString() {
        return new StringAppender()
                .appendJoin(" ", Stat.BATTLE_STATS, stat -> String.valueOf(this.getStage(stat)))
                .toString();
    }

    // Creates a TestStages object based on the current stages this Pokemon has
    public TestStages testStages() {
        Stages stages = this.getStages();
        TestStages testStages = new TestStages();
        for (Stat stat : Stat.BATTLE_STATS) {
            testStages.set(stages.getStage(stat), stat);
        }
        return testStages;
    }

    public void setupMove(AttackNamesies attackNamesies, Battle battle) {
        this.setMove(new Move(attackNamesies));
        this.getMove().startTurn(battle, this);
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

    public void assertHasFullHealth(boolean shouldBeFull) {
        Assert.assertEquals(this.getHpString(), shouldBeFull, this.fullHealth());
    }

    public void assertFullHealth() {
        this.assertHasFullHealth(true);
    }

    public void assertNotFullHealth() {
        this.assertHasFullHealth(false);
    }

    public void assertHealthRatio(double fraction) {
        this.assertHealthRatio(fraction, 0);
    }

    public void assertHealthRatio(double fraction, int errorHp) {
        int hpFraction = (int)(Math.ceil(fraction*this.getMaxHP()));
        TestUtils.assertAlmostEquals(
                StringUtils.spaceSeparated(this, fraction, this.getHPRatio(), hpFraction, this.getHpString(), errorHp),
                hpFraction, this.getHP(), errorHp
        );
    }

    public void assertHealthRatioDiff(int prevHp, double fractionDiff) {
        this.assertHp(prevHp - (int)(fractionDiff*this.getMaxHP()));
    }

    public void assertMissingHp(int missingHp) {
        this.assertHp(this.getMaxHP() - missingHp);
    }

    public void assertHp(int hp) {
        Assert.assertEquals(this.getHpString() + " " + hp, hp, this.getHP());
    }

    public void assertStatus(boolean hasStatus, StatusNamesies statusNamesies) {
        if (hasStatus) {
            this.assertHasStatus(statusNamesies);
        } else {
            Assert.assertFalse(this.getStatus().toString(), this.hasStatus(statusNamesies));
        }
    }

    // Confirms the Pokemon does not have any status condition
    public void assertNoStatus() {
        Assert.assertFalse(this.getStatus().toString(), this.hasStatus());
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
                Assert.assertTrue(this.getStatus().toString(), this.hasStatus(statusNamesies));
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

    public void assertDead() {
        this.assertHasStatus(StatusNamesies.FAINTED);
        this.assertHp(0);
    }

    public void assertNoStages() {
        this.assertStages(new TestStages());
    }

    public void assertStages(TestStages testStages) {
        int total = 0;
        for (Stat stat : Stat.BATTLE_STATS) {
            Assert.assertEquals(
                    stat.getName() + " "  + this.stagesString(),
                    testStages.get(stat),
                    this.getStage(stat)
            );
            total += testStages.get(stat);
        }
        assertTotalStages(total);
    }

    public void assertTotalStages(int total) {
        Assert.assertEquals(this.stagesString(), total, this.getStages().totalStatChanges());
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
        Assert.assertEquals(this + " " + effectNamesies.name(), shouldHave, this.hasEffect(effectNamesies));
    }

    // Asserts that the Pokemon actual item (displayed in menu) is the input item
    // This method assumes that the battle item (the one used for calculation purposes) is the same and has never been changed
    public void assertActualHeldItem(ItemNamesies itemNamesies) {
        this.assertActualHeldItem(itemNamesies, itemNamesies);
    }

    // Actual item is the item that appears in the menu and what the Pokemon would be holding if the battle ended
    // Battle item is the item that is used for calculation purpose and may be different as a temporary effect
    public void assertActualHeldItem(ItemNamesies actualItem, ItemNamesies battleItem) {
        Assert.assertEquals(actualItem, this.getActualHeldItem().namesies());
        Assert.assertEquals(battleItem, this.getHeldItem().namesies());

        boolean changedItem = this.hasEffect(PokemonEffectNamesies.CHANGE_ITEM);
        boolean blockedItem = ItemBlockerEffect.shouldBlockItem(Game.getPlayer().getBattle(), this);
        String message = StringUtils.spaceSeparated(actualItem, battleItem, changedItem, blockedItem);

        if (blockedItem) {
            Assert.assertEquals(message, ItemNamesies.NO_ITEM, battleItem);
        }

        Assert.assertEquals(message, actualItem != battleItem, changedItem || blockedItem);
    }

    public void assertHoldingItem(ItemNamesies itemNamesies) {
        Assert.assertTrue(this.getHeldItem().getName() + " " + itemNamesies, this.isHoldingItem(itemNamesies));
    }

    public void assertNotHoldingItem() {
        Assert.assertFalse(this.getHeldItem().getName(), this.isHoldingItem());
    }

    // Either checks consumed or not consumed
    public void assertExpectedConsumedItem(boolean shouldConsume) {
        if (shouldConsume) {
            this.assertConsumedItem();
        } else {
            this.assertNotConsumedItem();
        }
    }

    // Confirms that the Pokemon is not holding an item and has the consumed item effect
    // Explicitly confirms that a berry was not consumed in the process (use assertConsumedBerry in this case)
    public void assertConsumedItem() {
        this.assertConsumedItem(false);
    }

    // Confirms that the Pokemon is not holding an item and has the consumed item and berry effects
    public void assertConsumedBerry() {
        this.assertConsumedItem(true);
    }

    private void assertConsumedItem(boolean berry) {
        this.assertNotHoldingItem();
        this.assertHasEffect(PokemonEffectNamesies.CONSUMED_ITEM);
        this.assertEffect(berry, PokemonEffectNamesies.EATEN_BERRY);
    }

    // Confirms that the Pokemon is still holding an item and does not have the consumed item/berry effect
    public void assertNotConsumedItem() {
        Assert.assertTrue(this.isHoldingItem());
        this.assertNoEffect(PokemonEffectNamesies.CONSUMED_ITEM);
        this.assertNoEffect(PokemonEffectNamesies.EATEN_BERRY);
    }

    public void assertAbility(AbilityNamesies abilityNamesies) {
        Assert.assertTrue(
                StringUtils.spaceSeparated(this.getActualName(), this.getAbility(), abilityNamesies.getName()),
                this.hasAbility(abilityNamesies)
        );
        this.assertNoEffect(PokemonEffectNamesies.CHANGE_ABILITY);
    }

    public void assertChangedAbility(AbilityNamesies abilityNamesies) {
        Assert.assertTrue(this.getAbility().getName(), this.hasAbility(abilityNamesies));
        this.assertHasEffect(PokemonEffectNamesies.CHANGE_ABILITY);
    }

    public void assertType(Battle b, Type type) {
        Assert.assertTrue(this.getType(b).toString(), this.isType(b, type));
    }

    public void assertNotType(Battle b, Type type) {
        Assert.assertFalse(this.isType(b, type));
    }

    public void assertAttackType(Type type) {
        Assert.assertEquals(type, this.getAttackType());
        Assert.assertTrue(this.isAttackType(type));
    }

    public void assertSpecies(PokemonNamesies species) {
        Assert.assertTrue(this.namesies() + " " + species, this.isPokemon(species));
        if (species == this.namesies()) {
            Assert.assertTrue(this.isActualPokemon(species));
            this.assertNoEffect(PokemonEffectNamesies.TRANSFORMED);
        } else {
            Assert.assertFalse(this.isActualPokemon(species));
            this.assertHasEffect(PokemonEffectNamesies.TRANSFORMED);
        }
    }

    public void assertLastMoveSucceeded(boolean success) {
        Assert.assertEquals(success, this.lastMoveSucceeded());
    }

    public void assertStatModifier(double modifier, Stat stat, TestBattle battle) {
        Assert.assertNotEquals(Stat.HP, stat);

        TestPokemon otherPokemon = battle.getOtherPokemon(this);

        int baseStat = this.calculateBaseStat(stat, this.getPokemonInfo());
        int currentStat = Stat.getStat(stat, this, otherPokemon, battle);

        // If the Pokemon is transformed, need to adjust stats
        // Note: Will likely need to update in future to also include stance change abilities and such
        int delta = 0;
        if (this.hasEffect(PokemonEffectNamesies.TRANSFORMED)) {
            // Calculate what the stat should be with different base stats (but no effects)
            PokemonHolder transformed = (PokemonHolder)this.getEffect(PokemonEffectNamesies.TRANSFORMED);
            int transformPokemonStat = this.calculateBaseStat(stat, transformed.getPokemon().getInfo());

            modifier *= (double)transformPokemonStat/baseStat;
            delta = 1;
        }

        TestUtils.assertAlmostEquals(
                StringUtils.spaceSeparated(this, baseStat, currentStat, modifier, (double)currentStat/baseStat),
                (int)(baseStat*modifier),
                currentStat,
                delta
        );
    }

    private int calculateBaseStat(Stat stat, PokemonInfo pokemonInfo) {
        return stat.isAccuracyStat() ? 100 : this.stats().calculate(stat, pokemonInfo.getStats());
    }

    public static TestPokemon newPlayerPokemon(final PokemonNamesies pokemon) {
        return new TestPokemon(pokemon, TrainerType.PLAYER);
    }

    public static TestPokemon newWildPokemon(final PokemonNamesies pokemon) {
        return new TestPokemon(pokemon, TrainerType.WILD);
    }

    public static TestPokemon newTrainerPokemon(final PokemonNamesies pokemon) {
        return new TestPokemon(pokemon, TrainerType.OPPONENT);
    }

    @Override
    public String toString() {
        // Can add other identifying info more relevant to what is being debugged
        return StringUtils.spaceSeparated(this.getName());
    }
}
