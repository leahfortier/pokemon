package battle.attack;

import battle.ActivePokemon;
import battle.Battle;
import battle.DamageCalculator.DamageCalculation;
import battle.effect.InvokeEffect;
import battle.effect.InvokeInterfaces.UserOnDamageEffect;
import battle.effect.InvokeInterfaces.VictimOnDamageEffect;
import battle.stages.Stages;
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
    boolean isMoveType(MoveType moveType);

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

    // Returns true if the accuracy check should be completely ignored to always hit the attack
    // Returning true here goes even before all other bypass accuracy checks like semi-invulnerable etc
    default boolean ignoreAccuracyCheck() {
        // Self-target moves and field moves never miss
        return this.isSelfTargetStatusMove() || this.isMoveType(MoveType.FIELD);
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

        if (me.isPlayer()) {
            Game.getPlayer().getMedalCase().checkAdvantage(advantage);
        }

        // Deadsies check
        o.isFainted(b);
        me.isFainted(b);

        // Apply a damage effect
        UserOnDamageEffect.invokeUserOnDamageEffect(b, me, o);
        VictimOnDamageEffect.invokeVictimOnDamageEffect(b, me, o);
    }

    @Override
    default InvokeSource getSource() {
        return InvokeSource.ATTACK;
    }
}
