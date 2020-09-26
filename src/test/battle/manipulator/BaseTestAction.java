package test.battle.manipulator;

import battle.attack.AttackNamesies;
import battle.effect.EffectNamesies;
import item.ItemNamesies;
import item.bag.Bag;
import org.junit.Assert;
import pokemon.ability.AbilityNamesies;
import pokemon.species.PokemonNamesies;
import test.battle.TestBattle;
import test.battle.TestStages;
import test.battle.manipulator.PokemonAction.AttackingAction;
import test.battle.manipulator.PokemonAction.DefendingAction;
import trainer.Trainer;

import java.util.ArrayList;
import java.util.List;

abstract class BaseTestAction<BaseType extends BaseTestAction<BaseType>> implements TestTracker {
    protected PokemonManipulator manipulator;
    protected List<String> toString;

    protected BaseTestAction() {
        this.manipulator = PokemonManipulator.empty();
        this.toString = new ArrayList<>();
    }

    protected abstract BaseType getThis();

    @Override
    public List<String> getTestStrings() {
        return toString;
    }

    private void addString(boolean attacking, String name) {
        this.toString.add((attacking ? "ATTACKING" : "DEFENDING") + "[" + name + "]");
    }

    public void manipulate(TestBattle battle) {
        this.manipulator.manipulate(battle);
    }

    public BaseType fight(AttackNamesies attackingMove, AttackNamesies defendingMove) {
        this.toString.add("FIGHT[" + attackingMove.getName() + ", " + defendingMove.getName() + "]");
        return this.with((battle, attacking, defending) -> battle.fight(attackingMove, defendingMove));
    }

    public BaseType attackingFight(AttackNamesies attackName) {
        this.addString(true, attackName.getName());
        return this.with((battle, attacking, defending) -> battle.attackingFight(attackName));
    }

    public BaseType defendingFight(AttackNamesies attackName) {
        this.addString(false, attackName.getName());
        return this.with((battle, attacking, defending) -> battle.defendingFight(attackName));
    }

    public BaseType with(PokemonManipulator manipulator) {
        if (manipulator instanceof TestTracker) {
            this.toString.addAll(((TestTracker)manipulator).getTestStrings());
        }
        this.manipulator = this.manipulator.add(manipulator);
        return this.getThis();
    }

    public BaseType falseSwipePalooza(boolean playerAttacking) {
        this.addString(playerAttacking, "False Swipe Palooza");
        return this.with((battle, attacking, defending) -> battle.falseSwipePalooza(playerAttacking));
    }

    // Defaults to player using the item successfully
    public BaseType useItem(ItemNamesies itemNamesies) {
        return this.useItem(itemNamesies, true, true);
    }

    // Should only be a defending Pokemon in a trainer battle
    public BaseType useItem(ItemNamesies itemNamesies, boolean isPlayer, boolean assertion) {
        this.addString(isPlayer, itemNamesies.getName());
        return this.with((battle, attacking, defending) -> {
            Trainer trainer = (Trainer)battle.getTrainer(isPlayer);
            Bag bag = trainer.getBag();
            bag.addItem(itemNamesies);

            bag.setSelectedBattleItem(itemNamesies, attacking);
            Assert.assertEquals(assertion, bag.battleUseItem(battle, trainer));
            Assert.assertNotEquals(assertion, bag.hasItem(itemNamesies));

            if (bag.hasItem(itemNamesies)) {
                bag.removeItem(itemNamesies);
            }
        });
    }

    public BaseType addAttacking(PokemonNamesies pokes) {
        this.addString(true, pokes.getName());
        return this.with((battle, attacking, defending) -> battle.addAttacking(pokes));
    }

    public BaseType addDefending(PokemonNamesies pokes) {
        this.addString(false, pokes.getName());
        return this.with((battle, attacking, defending) -> battle.addDefending(pokes));
    }

    public BaseType addDefending(PokemonNamesies pokes, AbilityNamesies ability) {
        this.addString(false, pokes.getName() + " (" + ability.getName() + ")");
        return this.with((battle, attacking, defending) -> battle.addDefending(pokes).withAbility(ability));
    }

    public BaseType attacking(AbilityNamesies abilityNamesies) {
        return this.with(new AttackingAction().withAbility(abilityNamesies));
    }

    public BaseType defending(AbilityNamesies abilityNamesies, EffectNamesies effectNamesies) {
        return this.defending(abilityNamesies).defending(effectNamesies);
    }

    public BaseType defending(AbilityNamesies abilityNamesies) {
        return this.with(new DefendingAction().withAbility(abilityNamesies));
    }

    public BaseType attacking(ItemNamesies itemNamesies) {
        return this.with(new AttackingAction().withItem(itemNamesies));
    }

    public BaseType defending(ItemNamesies itemNamesies) {
        return this.with(new DefendingAction().withItem(itemNamesies));
    }

    public BaseType attacking(EffectNamesies effectNamesies) {
        return this.with(new AttackingAction().withEffect(effectNamesies));
    }

    public BaseType defending(EffectNamesies effectNamesies) {
        return this.with(new DefendingAction().withEffect(effectNamesies));
    }

    public BaseType attackingBypass(Boolean bypass) {
        return this.with(new AttackingAction().withAccuracyBypass(bypass));
    }

    public BaseType attacking(TestStages stages) {
        return this.with(new AttackingAction().assertStages(stages));
    }

    public BaseType defending(TestStages stages) {
        return this.with(new DefendingAction().assertStages(stages));
    }
}
