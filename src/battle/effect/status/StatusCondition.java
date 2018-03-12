package battle.effect.status;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.AttackNamesies;
import battle.attack.MoveType;
import battle.effect.CastSource;
import battle.effect.Effect;
import battle.effect.EffectInterfaces.BeforeTurnEffect;
import battle.effect.EffectInterfaces.EndTurnEffect;
import battle.effect.EffectInterfaces.OpponentStatusReceivedEffect;
import battle.effect.EffectInterfaces.SimpleStatModifyingEffect;
import battle.effect.EffectInterfaces.SleepyFightsterEffect;
import battle.effect.EffectInterfaces.StatusPreventionEffect;
import battle.effect.EffectInterfaces.StatusReceivedEffect;
import battle.effect.EffectInterfaces.TakeDamageEffect;
import battle.effect.InvokeEffect;
import battle.effect.pokemon.PokemonEffectNamesies;
import message.MessageUpdate;
import message.Messages;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import type.Type;
import util.RandomUtils;
import util.serialization.Serializable;

public abstract class StatusCondition implements InvokeEffect, Serializable {
    private static final long serialVersionUID = 1L;

    private final StatusNamesies namesies;
    private final String shortName;
    private final double catchModifier;

    protected StatusCondition(StatusNamesies namesies, String shortName, double catchModifier) {
        this.namesies = namesies;
        this.shortName = shortName;
        this.catchModifier = catchModifier;
    }

    public StatusNamesies namesies() {
        return this.namesies;
    }

    public String getShortName() {
        return shortName;
    }

    public double getCatchModifier() {
        return catchModifier;
    }

    protected abstract boolean statusApplies(Battle b, ActivePokemon caster, ActivePokemon victim);

    protected abstract String getGenericCastMessage(ActivePokemon p);
    protected abstract String getSourceCastMessage(ActivePokemon sourcerer, ActivePokemon victim, String sourceName);

    protected abstract String getGenericRemoveMessage(ActivePokemon victim);
    protected abstract String getSourceRemoveMessage(ActivePokemon victim, String sourceName);

    private String getCastMessage(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
        if (source.hasSourceName()) {
            return this.getSourceCastMessage(caster, victim, source.getSourceName(b, caster));
        } else {
            return this.getGenericCastMessage(victim);
        }
    }

    public String getRemoveMessage(Battle b, ActivePokemon victim, CastSource source) {
        if (source.hasSourceName()) {
            return this.getSourceRemoveMessage(victim, source.getSourceName(b, victim));
        } else {
            return this.getGenericRemoveMessage(victim);
        }
    }

    public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
        StatusPreventionEffect statusPrevent = StatusPreventionEffect.getPreventEffect(b, user, victim, this.namesies);
        if (statusPrevent != null) {
            return statusPrevent.statusPreventionMessage(victim);
        }

        return Effect.DEFAULT_FAIL_MESSAGE;
    }

    public boolean appliesWithoutStatusCheck(Battle b, ActivePokemon caster, ActivePokemon victim) {
        return this.statusApplies(b, caster, victim) &&
                StatusPreventionEffect.getPreventEffect(b, caster, victim, this.namesies) == null;
    }

    public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim) {
        return !victim.hasStatus() && this.appliesWithoutStatusCheck(b, caster, victim);
    }

    // Returns true if a status was successfully given, and false if it failed for any reason
    public boolean apply(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
        return apply(b, caster, victim, this.getCastMessage(b, caster, victim, source));
    }

    public boolean apply(Battle b, ActivePokemon caster, ActivePokemon victim, String castMessage) {
        if (this.applies(b, caster, victim)) {
            victim.setStatus(this);
            Messages.add(new MessageUpdate(castMessage).updatePokemon(b, victim));

            StatusReceivedEffect.invokeStatusReceivedEffect(b, caster, victim, this.namesies);
            OpponentStatusReceivedEffect.invokeOpponentStatusReceivedEffect(b, victim, this.namesies);
            return true;
        }

        return false;
    }

    public boolean isType(StatusNamesies statusCondition) {
        return this.namesies() == statusCondition;
    }

    public int getTurns() {
        return -1;
    }

    public void setTurns(int turns) {}

    @Override
    public InvokeSource getSource() {
        return InvokeSource.EFFECT;
    }

    @Override
    public String toString() {
        return this.namesies + " " + this.getTurns();
    }

    // EVERYTHING BELOW IS GENERATED ###

    /**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

    static class NoStatus extends StatusCondition {
        private static final long serialVersionUID = 1L;

        NoStatus() {
            super(StatusNamesies.NO_STATUS, "", 1.0);
        }

        @Override
        public boolean statusApplies(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return victim.getHP() > 0;
        }

        @Override
        public String getGenericCastMessage(ActivePokemon p) {
            return "";
        }

        @Override
        public String getSourceCastMessage(ActivePokemon sourcerer, ActivePokemon victim, String sourceName) {
            return "";
        }

        @Override
        public String getGenericRemoveMessage(ActivePokemon victim) {
            return "";
        }

        @Override
        public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
            return "";
        }
    }

    static class Fainted extends StatusCondition {
        private static final long serialVersionUID = 1L;

        Fainted() {
            super(StatusNamesies.FAINTED, "FNT", 1.0);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Fainted status condition applies regardless of other status conditions
            return this.statusApplies(b, user, victim);
        }

        @Override
        public boolean statusApplies(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return victim.getHP() == 0;
        }

        @Override
        public String getGenericCastMessage(ActivePokemon p) {
            return p.getName() + " fainted!";
        }

        @Override
        public String getSourceCastMessage(ActivePokemon sourcerer, ActivePokemon victim, String sourceName) {
            return sourcerer.getName() + "'s " + sourceName + " caused " + victim.getName() + " to faint!";
        }

        @Override
        public String getGenericRemoveMessage(ActivePokemon victim) {
            return "";
        }

        @Override
        public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
            return "";
        }
    }

    // Electric-type Pokemon cannot be paralyzed
    // Paralysis reduces speed by 75%
    static class Paralyzed extends StatusCondition implements BeforeTurnEffect, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        Paralyzed() {
            super(StatusNamesies.PARALYZED, "PRZ", 1.5);
        }

        @Override
        public boolean statusApplies(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return !victim.isType(b, Type.ELECTRIC);
        }

        @Override
        public String getGenericCastMessage(ActivePokemon p) {
            return p.getName() + " was paralyzed!";
        }

        @Override
        public String getSourceCastMessage(ActivePokemon sourcerer, ActivePokemon victim, String sourceName) {
            return sourcerer.getName() + "'s " + sourceName + " paralyzed " + victim.getName() + "!";
        }

        @Override
        public String getGenericRemoveMessage(ActivePokemon victim) {
            return victim.getName() + " is no longer paralyzed!";
        }

        @Override
        public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
            return victim.getName() + "'s " + sourceName + " cured it of its paralysis!";
        }

        @Override
        public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
            if (RandomUtils.chanceTest(25)) {
                Messages.add(p.getName() + " is fully paralyzed!");
                return false;
            }

            return true;
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return !p.hasAbility(AbilityNamesies.QUICK_FEET);
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
        }

        @Override
        public double getModifier() {
            return .25;
        }
    }

    // Poison-type and Steel-type Pokemon cannot be poisoned unless the caster has the Corrosion ability
    static class Poisoned extends StatusCondition implements EndTurnEffect {
        private static final long serialVersionUID = 1L;

        Poisoned() {
            super(StatusNamesies.POISONED, "PSN", 1.5);
        }

        @Override
        public String getGenericCastMessage(ActivePokemon p) {
            return p.getName() + " was poisoned!";
        }

        @Override
        public String getSourceCastMessage(ActivePokemon sourcerer, ActivePokemon victim, String sourceName) {
            return sourcerer.getName() + "'s " + sourceName + " poisoned " + victim.getName() + "!";
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
                return;
            }

            if (victim.hasAbility(AbilityNamesies.POISON_HEAL)) {
                if (victim.fullHealth() || victim.hasEffect(PokemonEffectNamesies.HEAL_BLOCK)) {
                    return;
                }

                victim.healHealthFraction(1/8.0);
                Messages.add(new MessageUpdate(victim.getName() + "'s " + AbilityNamesies.POISON_HEAL + " restored its health!").updatePokemon(b, victim));
                return;
            }

            Messages.add(victim.getName() + " was hurt by its poison!");
            victim.reduceHealthFraction(b, 1/8.0);
        }

        @Override
        public boolean statusApplies(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return (!victim.isType(b, Type.POISON) && !victim.isType(b, Type.STEEL) || caster.hasAbility(AbilityNamesies.CORROSION));
        }

        @Override
        public String getGenericRemoveMessage(ActivePokemon victim) {
            return victim.getName() + " is no longer poisoned!";
        }

        @Override
        public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
            return victim.getName() + "'s " + sourceName + " cured it of its poison!";
        }
    }

    // Poison-type and Steel-type Pokemon cannot be poisoned unless the caster has the Corrosion ability
    static class BadlyPoisoned extends StatusCondition implements EndTurnEffect {
        private static final long serialVersionUID = 1L;

        // TODO: Confirm that it's okay that the type is BADLY_POISONED instead of POISONED
        private int turns;

        BadlyPoisoned() {
            super(StatusNamesies.BADLY_POISONED, "PSN", 1.5);
            this.turns = 1;
        }

        @Override
        public String getGenericCastMessage(ActivePokemon p) {
            return p.getName() + " was badly poisoned!";
        }

        @Override
        public String getSourceCastMessage(ActivePokemon sourcerer, ActivePokemon victim, String sourceName) {
            return sourcerer.getName() + "'s " + sourceName + " badly poisoned " + victim.getName() + "!";
        }

        @Override
        public boolean isType(StatusNamesies statusCondition) {
            return statusCondition == StatusNamesies.POISONED || statusCondition == StatusNamesies.BADLY_POISONED;
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
                return;
            }

            if (victim.hasAbility(AbilityNamesies.POISON_HEAL)) {
                if (victim.fullHealth() || victim.hasEffect(PokemonEffectNamesies.HEAL_BLOCK)) {
                    return;
                }

                victim.healHealthFraction(1/8.0);
                Messages.add(new MessageUpdate(victim.getName() + "'s " + AbilityNamesies.POISON_HEAL + " restored its health!").updatePokemon(b, victim));
                return;
            }

            Messages.add(victim.getName() + " was hurt by its poison!");
            victim.reduceHealthFraction(b, this.turns++/16.0);
        }

        @Override
        public boolean statusApplies(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return (!victim.isType(b, Type.POISON) && !victim.isType(b, Type.STEEL) || caster.hasAbility(AbilityNamesies.CORROSION));
        }

        @Override
        public String getGenericRemoveMessage(ActivePokemon victim) {
            return victim.getName() + " is no longer poisoned!";
        }

        @Override
        public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
            return victim.getName() + "'s " + sourceName + " cured it of its poison!";
        }
    }

    // Fire-type Pokemon cannot be burned
    // Burn decreases attack by 50%
    static class Burned extends StatusCondition implements EndTurnEffect, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        Burned() {
            super(StatusNamesies.BURNED, "BRN", 1.5);
        }

        @Override
        public boolean statusApplies(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return !victim.isType(b, Type.FIRE);
        }

        @Override
        public String getGenericCastMessage(ActivePokemon p) {
            return p.getName() + " was burned!";
        }

        @Override
        public String getSourceCastMessage(ActivePokemon sourcerer, ActivePokemon victim, String sourceName) {
            return sourcerer.getName() + "'s " + sourceName + " burned " + victim.getName() + "!";
        }

        @Override
        public String getGenericRemoveMessage(ActivePokemon victim) {
            return victim.getName() + " is no longer burned!";
        }

        @Override
        public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
            return victim.getName() + "'s " + sourceName + " cured it of its burn!";
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
                return;
            }

            Messages.add(victim.getName() + " was hurt by its burn!");
            victim.reduceHealthFraction(b, victim.hasAbility(AbilityNamesies.HEATPROOF) ? 1/16.0 : 1/8.0);
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return !p.hasAbility(AbilityNamesies.GUTS) && p.getAttack().namesies() != AttackNamesies.FACADE;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.ATTACK;
        }

        @Override
        public double getModifier() {
            return .5;
        }
    }

    // All Pokemon can get sleepy
    static class Asleep extends StatusCondition implements BeforeTurnEffect {
        private static final long serialVersionUID = 1L;

        private int numTurns;

        Asleep() {
            super(StatusNamesies.ASLEEP, "SLP", 2.5);
            this.numTurns = RandomUtils.getRandomInt(1, 3);
        }

        @Override
        public boolean statusApplies(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return true;
        }

        @Override
        public String getGenericCastMessage(ActivePokemon p) {
            return p.getName() + " fell asleep!";
        }

        @Override
        public String getSourceCastMessage(ActivePokemon sourcerer, ActivePokemon victim, String sourceName) {
            return sourcerer.getName() + "'s " + sourceName + " caused " + victim.getName() + " to fall asleep!";
        }

        @Override
        public String getGenericRemoveMessage(ActivePokemon victim) {
            return victim.getName() + " woke up!";
        }

        @Override
        public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
            return victim.getName() + "'s " + sourceName + " caused it to wake up!";
        }

        @Override
        public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
            if (numTurns == 0) {
                p.removeStatus(b, CastSource.EFFECT);
                return true;
            }

            numTurns--;

            Messages.add(p.getName() + " is fast asleep...");
            return SleepyFightsterEffect.containsSleepyFightsterEffect(b, p);
        }

        @Override
        public int getTurns() {
            return this.numTurns;
        }

        @Override
        public void setTurns(int turns) {
            this.numTurns = turns;
        }
    }

    // Ice-type Pokemon cannot be frozen
    static class Frozen extends StatusCondition implements BeforeTurnEffect, TakeDamageEffect {
        private static final long serialVersionUID = 1L;

        Frozen() {
            super(StatusNamesies.FROZEN, "FRZ", 2.5);
        }

        @Override
        public boolean statusApplies(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return !victim.isType(b, Type.ICE);
        }

        @Override
        public String getGenericCastMessage(ActivePokemon p) {
            return p.getName() + " was frozen!";
        }

        @Override
        public String getSourceCastMessage(ActivePokemon sourcerer, ActivePokemon victim, String sourceName) {
            return sourcerer.getName() + "'s " + sourceName + " froze " + victim.getName() + "!";
        }

        @Override
        public String getGenericRemoveMessage(ActivePokemon victim) {
            return victim.getName() + " thawed out!";
        }

        @Override
        public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
            return victim.getName() + "'s " + sourceName + " thawed it out!";
        }

        @Override
        public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
            // 20% chance to thaw out each turn
            if (RandomUtils.chanceTest(20) || p.getAttack().isMoveType(MoveType.DEFROST)) {
                p.removeStatus(b, CastSource.EFFECT);
                return true;
            }

            Messages.add(p.getName() + " is frozen solid!");
            return false;
        }

        @Override
        public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Fire-type moves defrost the user
            if (user.isAttackType(Type.FIRE)) {
                victim.removeStatus(b, CastSource.EFFECT);
            }
        }
    }
}
