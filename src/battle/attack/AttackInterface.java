package battle.attack;

import battle.ActivePokemon;
import battle.Battle;
import battle.DamageCalculator.DamageCalculation;
import battle.stages.Stages;
import battle.effect.InvokeEffect;
import battle.effect.InvokeInterfaces.ApplyDamageEffect;
import battle.effect.InvokeInterfaces.OpponentApplyDamageEffect;
import battle.effect.InvokeInterfaces.OpponentTakeDamageEffect;
import battle.effect.InvokeInterfaces.TakeDamageEffect;
import main.Game;
import message.Messages;
import pokemon.ability.AbilityNamesies;
import pokemon.stat.Stat;
import type.TypeAdvantage;

public interface AttackInterface extends InvokeEffect {
    AttackNamesies namesies();
    MoveCategory getCategory();
    boolean isSelfTarget();
    int getBaseAccuracy();

    default String getName() {
        return this.namesies().getName();
    }

    default boolean isStatusMove() {
        return this.getCategory() == MoveCategory.STATUS;
    }

    default boolean isSelfTargetStatusMove() {
        return this.isSelfTarget() && this.isStatusMove();
    }

    default int getAccuracy(Battle b, ActivePokemon me, ActivePokemon o) {
        return this.getBaseAccuracy();
    }

    default void beginAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {}
    default void endAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {}
    default void totalAndCompleteFailure(Battle b, ActivePokemon attacking, ActivePokemon defending) {}

    default boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
        return true;
    }

    default boolean shouldApplyDamage(Battle b, ActivePokemon user) {
        // Status moves default to no damage
        return !this.isStatusMove();
    }

    default boolean shouldApplyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
        return true;
    }

    // Physical and Special moves -- do dat damage!
    default void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
        // Deal damage
        DamageCalculation calculation = b.calculateDamage(me, o);
        int damage = o.reduceHealth(b, calculation.getCalculatedDamage());

        // Set the actual damage dealt on the move
        calculation.setDamageDealt(damage);

        // Crit yo pants
        if (calculation.isCritical()) {
            Messages.add("It's a critical hit!!");
            if (o.hasAbility(AbilityNamesies.ANGER_POINT)) {
                Messages.add(o.getName() + "'s " + AbilityNamesies.ANGER_POINT.getName() + " raised its attack to the max!");
                o.getStages().setStage(Stat.ATTACK, Stages.MAX_STAT_CHANGES);
            }
        }

        // Print Advantage
        double advantage = calculation.getAdvantage();
        TypeAdvantage.addAdvantageMessage(advantage);

        if (me.isPlayer() && !b.isSimulating()) {
            Game.getPlayer().getMedalCase().checkAdvantage(advantage);
        }

        // Deadsies check
        o.isFainted(b);
        me.isFainted(b);

        // Apply a damage effect
        ApplyDamageEffect.invokeApplyDamageEffect(b, me, o);
        OpponentApplyDamageEffect.invokeOpponentApplyDamageEffect(b, me, o);

        // Effects that apply to the opponent when they take damage
        TakeDamageEffect.invokeTakeDamageEffect(b, me, o);
        OpponentTakeDamageEffect.invokeOpponentTakeDamageEffect(b, me, o);
    }

    @Override
    default InvokeSource getSource() {
        return InvokeSource.ATTACK;
    }
}
