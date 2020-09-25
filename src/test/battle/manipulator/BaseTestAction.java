package test.battle.manipulator;

import battle.attack.AttackNamesies;
import battle.effect.EffectNamesies;
import item.ItemNamesies;
import pokemon.ability.AbilityNamesies;
import pokemon.species.PokemonNamesies;
import test.battle.TestBattle;
import test.battle.TestStages;
import util.string.StringUtils;

import java.util.ArrayList;
import java.util.List;

abstract class BaseTestAction<BaseType extends BaseTestAction<BaseType>> {
    protected PokemonManipulator manipulator;
    protected List<String> toString;

    protected BaseTestAction() {
        this.manipulator = PokemonManipulator.empty();
        this.toString = new ArrayList<>();
    }

    protected abstract BaseType getThis();

    private void updateManipulator(PokemonManipulator manipulator) {
        this.manipulator = this.manipulator.add(manipulator);
    }

    private void addEffectString(boolean attacking, EffectNamesies effectNamesies) {
        this.addString(attacking, StringUtils.properCase(effectNamesies.toString().toLowerCase().replaceAll("_", " ")));
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
        this.updateManipulator(manipulator);
        return this.getThis();
    }

    public BaseType falseSwipePalooza(boolean playerAttacking) {
        this.addString(playerAttacking, "False Swipe Palooza");
        return this.with((battle, attacking, defending) -> battle.falseSwipePalooza(playerAttacking));
    }

    public BaseType addAttacking(PokemonNamesies pokes) {
        this.addString(true, pokes.getName());
        this.updateManipulator((battle, attacking, defending) -> battle.addAttacking(pokes));
        return this.getThis();
    }

    public BaseType addDefending(PokemonNamesies pokes) {
        this.addString(false, pokes.getName());
        this.updateManipulator((battle, attacking, defending) -> battle.addDefending(pokes));
        return this.getThis();
    }

    public BaseType addDefending(PokemonNamesies pokes, AbilityNamesies ability) {
        this.addString(false, pokes.getName() + " (" + ability.getName() + ")");
        this.updateManipulator((battle, attacking, defending) -> battle.addDefending(pokes).withAbility(ability));
        return this.getThis();
    }

    public BaseType attacking(AbilityNamesies abilityNamesies) {
        this.addString(true, abilityNamesies.getName());
        this.updateManipulator(PokemonManipulator.giveAttackingAbility(abilityNamesies));
        return this.getThis();
    }

    public BaseType defending(AbilityNamesies abilityNamesies, EffectNamesies effectNamesies) {
        return this.defending(abilityNamesies).defending(effectNamesies);
    }

    public BaseType defending(AbilityNamesies abilityNamesies) {
        this.addString(false, abilityNamesies.getName());
        this.updateManipulator(PokemonManipulator.giveDefendingAbility(abilityNamesies));
        return this.getThis();
    }

    public BaseType attacking(ItemNamesies itemNamesies) {
        this.addString(true, itemNamesies.getName());
        this.updateManipulator(PokemonManipulator.giveAttackingItem(itemNamesies));
        return this.getThis();
    }

    public BaseType defending(ItemNamesies itemNamesies) {
        this.addString(false, itemNamesies.getName());
        this.updateManipulator(PokemonManipulator.giveDefendingItem(itemNamesies));
        return this.getThis();
    }

    public BaseType attacking(EffectNamesies effectNamesies) {
        this.addEffectString(true, effectNamesies);
        this.updateManipulator(PokemonManipulator.giveAttackingEffect(effectNamesies));
        return this.getThis();
    }

    public BaseType defending(EffectNamesies effectNamesies) {
        this.addEffectString(false, effectNamesies);
        this.updateManipulator(PokemonManipulator.giveDefendingEffect(effectNamesies));
        return this.getThis();
    }

    public BaseType attackingBypass(Boolean bypass) {
        this.addString(true, "Bypass: " + bypass);
        this.updateManipulator((battle, attacking, defending) -> attacking.setExpectedAccuracyBypass(bypass));
        return this.getThis();
    }

    public BaseType attacking(TestStages stages) {
        this.addString(true, stages.toString());
        this.updateManipulator((battle, attacking, defending) -> attacking.assertStages(stages));
        return this.getThis();
    }

    public BaseType defending(TestStages stages) {
        this.addString(false, stages.toString());
        this.updateManipulator((battle, attacking, defending) -> defending.assertStages(stages));
        return this.getThis();
    }
}
