package test.battle.manipulator;

import battle.attack.AttackNamesies;
import battle.effect.Effect;
import battle.effect.EffectNamesies;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.source.CastSource;
import battle.effect.status.StatusNamesies;
import item.ItemNamesies;
import org.junit.Assert;
import pokemon.ability.AbilityNamesies;
import test.battle.TestBattle;
import test.battle.TestStages;
import test.pokemon.TestPokemon;

import java.util.ArrayList;
import java.util.List;

public abstract class PokemonAction implements PokemonManipulator, TestTracker {
    public static class AttackingAction extends PokemonAction {
        public AttackingAction() {
            super(true);
        }
    }

    public static class DefendingAction extends PokemonAction {
        public DefendingAction() {
            super(false);
        }
    }

    private final boolean isAttacking;
    private PokemonManipulator manipulator;
    private List<String> toString;

    protected PokemonAction(boolean isAttacking) {
        this.isAttacking = isAttacking;
        this.manipulator = PokemonManipulator.empty();
        this.toString = new ArrayList<>();
    }

    private void addString(String name) {
        this.toString.add((this.isAttacking ? "ATTACKING" : "DEFENDING") + "[" + name + "]");
    }

    @Override
    public List<String> getTestStrings() {
        return toString;
    }

    @Override
    public void manipulate(TestBattle battle, TestPokemon attacking, TestPokemon defending) {
        if (this.isAttacking != attacking.isPlayer()) {
            TestPokemon temp = attacking;
            attacking = defending;
            defending = temp;
        }
        Assert.assertNotEquals(attacking.isPlayer(), defending.isPlayer());
        this.manipulator.manipulate(battle, attacking, defending);
    }

    public PokemonAction with(PokemonManipulator manipulator) {
        this.manipulator = this.manipulator.add(manipulator);
        return this;
    }

    public PokemonAction withAbility(AbilityNamesies abilityNamesies) {
        this.addString(abilityNamesies.getName());
        return this.with((battle, p, opp) -> p.withAbility(abilityNamesies));
    }

    public PokemonAction withItem(ItemNamesies itemNamesies) {
        this.addString(itemNamesies.getName());
        return this.with((battle, p, opp) -> p.withItem(itemNamesies));
    }

    public PokemonAction withEffect(EffectNamesies effectNamesies) {
        this.addString(effectNamesies.name());
        return this.with((battle, p, opp) -> Effect.cast(effectNamesies, battle, p, p, CastSource.EFFECT, false));
    }

    public PokemonAction withDamageModifier(Double damageModifier) {
        this.addString("Damage Modifier: " + damageModifier);
        return this.with((battle, p, opp) -> p.setExpectedDamageModifier(damageModifier));
    }

    public PokemonAction withAccuracyBypass(Boolean bypass) {
        this.addString("Bypass: " + bypass);
        return this.with((battle, p, opp) -> p.setExpectedAccuracyBypass(bypass));
    }

    public PokemonAction useAttack(AttackNamesies attackNamesies) {
        this.addString("UseAttack: " + attackNamesies.getName());
        return this.with((battle, p, opp) -> p.callFullNewMove(battle, opp, attackNamesies));
    }

    public PokemonAction assertAbility(AbilityNamesies abilityNamesies) {
        this.addString(abilityNamesies.getName());
        return this.with((battle, p, opp) -> p.assertAbility(abilityNamesies));
    }

    public PokemonAction assertItem(ItemNamesies itemNamesies) {
        this.addString(itemNamesies.getName());
        return this.with((battle, p, opp) -> p.assertHoldingItem(itemNamesies));
    }

    public PokemonAction assertStages(TestStages stages) {
        this.addString(stages.toString());
        return this.with((battle, p, opp) -> p.assertStages(stages));
    }

    public PokemonAction assertEffect(PokemonEffectNamesies effectNamesies) {
        this.addString(effectNamesies.name());
        return this.with((battle, p, opp) -> p.assertHasEffect(effectNamesies));
    }

    public PokemonAction assertStatus(StatusNamesies statusNamesies) {
        this.addString(statusNamesies.name());
        return this.with((battle, p, opp) -> p.assertHasStatus(statusNamesies));
    }

    public PokemonAction assertNoStatus() {
        return this.assertStatus(StatusNamesies.NO_STATUS);
    }
}
