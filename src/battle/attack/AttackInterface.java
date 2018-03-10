package battle.attack;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.EffectInterfaces.ApplyDamageEffect;
import battle.effect.EffectInterfaces.OpponentApplyDamageEffect;
import battle.effect.EffectInterfaces.OpponentTakeDamageEffect;
import battle.effect.EffectInterfaces.TakeDamageEffect;
import main.Game;
import message.Messages;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import type.TypeAdvantage;

public interface AttackInterface {
    AttackNamesies namesies();
    MoveCategory getCategory();
    boolean isSelfTarget();

    default String getName() {
        return this.namesies().getName();
    }

    default boolean isStatusMove() {
        return this.getCategory() == MoveCategory.STATUS;
    }

    default boolean isSelfTargetStatusMove() {
        return this.isSelfTarget() && this.isStatusMove();
    }

    default void beginAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {}
    default void endAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {}
    default void totalAndCompleteFailure(Battle b, ActivePokemon attacking, ActivePokemon defending) {}

    default boolean shouldApplyDamage(Battle b, ActivePokemon user) {
        // Status moves default to no damage
        return !this.isStatusMove();
    }

    default boolean shouldApplyEffects(Battle b, ActivePokemon user) {
        return true;
    }

    // Physical and Special moves -- do dat damage!
    default void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {

        // Deal damage
        int damage = b.calculateDamage(me, o);
        boolean critYoPants = b.criticalHit(me, o);
        if (critYoPants) {
            damage *= me.hasAbility(AbilityNamesies.SNIPER) ? 3 : 2;
        }

        damage = o.reduceHealth(b, damage);
        if (critYoPants) {
            Messages.add("It's a critical hit!!");
            if (o.hasAbility(AbilityNamesies.ANGER_POINT)) {
                Messages.add(o.getName() + "'s " + AbilityNamesies.ANGER_POINT.getName() + " raised its attack to the max!");
                o.getStages().setStage(Stat.ATTACK, Stat.MAX_STAT_CHANGES);
            }
        }

        // Print Advantage
        double advantage = TypeAdvantage.getAdvantage(me, o, b);
        if (TypeAdvantage.isNotVeryEffective(advantage)) {
            Messages.add(TypeAdvantage.getNotVeryEffectiveMessage());
        } else if (TypeAdvantage.isSuperEffective(advantage)) {
            Messages.add(TypeAdvantage.getSuperEffectiveMessage());
        }

        if (me.isPlayer() && !b.isSimulating()) {
            Game.getPlayer().getMedalCase().checkAdvantage(advantage);
        }

        // Deadsies check
        o.isFainted(b);
        me.isFainted(b);

        // Apply a damage effect
        ApplyDamageEffect.invokeApplyDamageEffect(b, me, o, damage);
        OpponentApplyDamageEffect.invokeOpponentApplyDamageEffect(b, me, o, damage);

        // Effects that apply to the opponent when they take damage
        TakeDamageEffect.invokeTakeDamageEffect(b, me, o);
        OpponentTakeDamageEffect.invokeOpponentTakeDamageEffect(b, me, o);
    }
}
