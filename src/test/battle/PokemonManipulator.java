package test.battle;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.AttackNamesies;
import battle.effect.generic.CastSource;
import battle.effect.generic.EffectNamesies;
import item.ItemNamesies;
import item.bag.Bag;
import main.Game;
import org.junit.Assert;
import pokemon.ability.AbilityNamesies;
import test.TestPokemon;

@FunctionalInterface
interface PokemonManipulator {
    void manipulate(TestBattle battle, TestPokemon attacking, TestPokemon defending);

    static void useAttack(AttackNamesies attackNamesies, TestBattle battle, TestPokemon attacking, TestPokemon defending, boolean attackingTarget) {
        ActivePokemon caster = attackingTarget ? defending : attacking;
        ActivePokemon victim = attackingTarget ? attacking : defending;

        caster.callFullNewMove(battle, victim, attackNamesies);
    }

    static void giveEffect(EffectNamesies effectNamesies, Battle battle, ActivePokemon attacking, ActivePokemon defending, boolean attackingTarget) {
        ActivePokemon caster = attackingTarget ? defending : attacking;
        ActivePokemon victim = attackingTarget ? attacking : defending;

        effectNamesies.getEffect().cast(battle, caster, victim, CastSource.ATTACK, false);
    }

    static PokemonManipulator empty() {
        return (battle, attacking, defending) -> {};
    }

    static PokemonManipulator combine(PokemonManipulator... manipulators) {
        return (battle, attacking, defending) -> {
            for (PokemonManipulator manipulator : manipulators) {
                manipulator.manipulate(battle, attacking, defending);
            }
        };
    }

    static PokemonManipulator attackingAttack(AttackNamesies attackNamesies) {
        return (battle, attacking, defending) -> useAttack(attackNamesies, battle, attacking, defending, false);
    }

    static PokemonManipulator defendingAttack(AttackNamesies attackNamesies) {
        return (battle, attacking, defending) -> useAttack(attackNamesies, battle, attacking, defending, true);
    }

    static PokemonManipulator giveAttackingEffect(EffectNamesies effectNamesies) {
        return (battle, attacking, defending) -> giveEffect(effectNamesies, battle, attacking, defending, true);
    }

    static PokemonManipulator giveDefendingEffect(EffectNamesies effectNamesies) {
        return (battle, attacking, defending) -> giveEffect(effectNamesies, battle, attacking, defending, false);
    }

    static PokemonManipulator giveAttackingAbility(AbilityNamesies abilityNamesies) {
        return (battle, attacking, defending) -> attacking.withAbility(abilityNamesies);
    }

    static PokemonManipulator giveDefendingAbility(AbilityNamesies abilityNamesies) {
        return (battle, attacking, defending) -> defending.withAbility(abilityNamesies);
    }

    static PokemonManipulator giveAttackingItem(ItemNamesies itemNamesies) {
        return (battle, attacking, defending) -> attacking.giveItem(itemNamesies);
    }

    static PokemonManipulator giveDefendingItem(ItemNamesies itemNamesies) {
        return (battle, attacking, defending) -> defending.giveItem(itemNamesies);
    }

    static PokemonManipulator useItem(ItemNamesies itemNamesies) {
        return useItem(itemNamesies, true);
    }

    static PokemonManipulator useItem(ItemNamesies itemNamesies, boolean assertion) {
        return (battle, attacking, defending) -> {
            Bag bag = Game.getPlayer().getBag();
            bag.addItem(itemNamesies);

            Assert.assertTrue(bag.battleUseItem(itemNamesies, attacking, battle) == assertion);
        };
    }
}
