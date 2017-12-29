package test.battle;

import battle.Battle;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.effect.generic.CastSource;
import battle.effect.generic.EffectNamesies;
import item.ItemNamesies;
import pokemon.ActivePokemon;
import pokemon.ability.AbilityNamesies;
import test.TestPokemon;

interface PokemonManipulator {
    void manipulate(TestBattle battle, TestPokemon attacking, TestPokemon defending);

    static void startAttack(Battle battle, ActivePokemon attacking, ActivePokemon defending) {
        attacking.startAttack(battle);
    }

    static void useAttack(AttackNamesies attackNamesies, TestBattle battle, TestPokemon attacking, TestPokemon defending) {
        attacking.callNewMove(battle, defending, new Move(attackNamesies));
    }

    static void giveEffect(EffectNamesies effectNamesies, Battle battle, ActivePokemon attacking, ActivePokemon defending, boolean attackingTarget) {
        ActivePokemon caster = attackingTarget ? defending : attacking;
        ActivePokemon victim = attackingTarget ? attacking : defending;

        effectNamesies.getEffect().cast(battle, caster, victim, CastSource.ATTACK, false);
        startAttack(battle, attacking, defending);
    }

    static void giveAbility(AbilityNamesies abilityNamesies, Battle battle, ActivePokemon attacking, ActivePokemon defending, boolean attackingTarget) {
        ActivePokemon victim = attackingTarget ? attacking : defending;

        victim.setAbility(abilityNamesies);
        startAttack(battle, attacking, defending);
    }

    static void giveItem(ItemNamesies itemNamesies, Battle battle, ActivePokemon attacking, ActivePokemon defending, boolean attackingTarget) {
        ActivePokemon victim = attackingTarget ? attacking : defending;

        victim.giveItem(itemNamesies);
        startAttack(battle, attacking, defending);
    }

    static PokemonManipulator empty() {
        return PokemonManipulator::startAttack;
    }

    static PokemonManipulator combine(PokemonManipulator... manipulators) {
        return (battle, attacking, defending) -> {
            for (PokemonManipulator manipulator : manipulators) {
                manipulator.manipulate(battle, attacking, defending);
            }
        };
    }

    static PokemonManipulator attackingAttack(AttackNamesies attackNamesies) {
        return (battle, attacking, defending) -> useAttack(attackNamesies, battle, attacking, defending);
    }

    static PokemonManipulator defendingAttack(AttackNamesies attackNamesies) {
        return (battle, attacking, defending) -> useAttack(attackNamesies, battle, defending, attacking);
    }

    static PokemonManipulator giveAttackingEffect(EffectNamesies effectNamesies) {
        return (battle, attacking, defending) -> giveEffect(effectNamesies, battle, attacking, defending, true);
    }

    static PokemonManipulator giveDefendingEffect(EffectNamesies effectNamesies) {
        return (battle, attacking, defending) -> giveEffect(effectNamesies, battle, attacking, defending, false);
    }

    static PokemonManipulator giveAttackingAbility(AbilityNamesies abilityNamesies) {
        return (battle, attacking, defending) -> giveAbility(abilityNamesies, battle, attacking, defending, true);
    }

    static PokemonManipulator giveDefendingAbility(AbilityNamesies abilityNamesies) {
        return (battle, attacking, defending) -> giveAbility(abilityNamesies, battle, attacking, defending, false);
    }

    static PokemonManipulator giveAttackingItem(ItemNamesies itemNamesies) {
        return (battle, attacking, defending) -> giveItem(itemNamesies, battle, attacking, defending, true);
    }

    static PokemonManipulator giveDefendingItem(ItemNamesies itemNamesies) {
        return (battle, attacking, defending) -> giveItem(itemNamesies, battle, attacking, defending, false);
    }
}
