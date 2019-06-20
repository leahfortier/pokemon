package battle.effect.status;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.AttackNamesies;
import battle.attack.MoveType;
import battle.effect.ApplyResult;
import battle.effect.EffectInterfaces.SimpleStatModifyingEffect;
import battle.effect.InvokeEffect;
import battle.effect.InvokeInterfaces.BeforeTurnEffect;
import battle.effect.InvokeInterfaces.EndTurnEffect;
import battle.effect.InvokeInterfaces.OpponentStatusReceivedEffect;
import battle.effect.InvokeInterfaces.SleepyFightsterEffect;
import battle.effect.InvokeInterfaces.StatusPreventionEffect;
import battle.effect.InvokeInterfaces.StatusReceivedEffect;
import battle.effect.InvokeInterfaces.TakeDamageEffect;
import battle.effect.source.CastSource;
import message.MessageUpdate;
import message.Messages;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import type.Type;
import util.RandomUtils;

public abstract class StatusCondition implements InvokeEffect {
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

    public abstract String getSourcePreventionMessage(ActivePokemon victim, String sourceName);

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

    public ApplyResult appliesWithoutStatusCheck(Battle b, ActivePokemon caster, ActivePokemon victim) {
        ApplyResult result = StatusPreventionEffect.getPreventEffect(b, caster, victim, this.namesies);
        if (!result.isSuccess()) {
            return result;
        }

        return ApplyResult.newResult(this.statusApplies(b, caster, victim));
    }

    public ApplyResult applies(Battle b, ActivePokemon caster, ActivePokemon victim) {
        if (victim.hasStatus()) {
            return ApplyResult.failure();
        }

        return this.appliesWithoutStatusCheck(b, caster, victim);
    }

    // Returns true if a status was successfully given, and false if it failed for any reason
    public ApplyResult apply(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
        return apply(b, caster, victim, this.getCastMessage(b, caster, victim, source));
    }

    public ApplyResult apply(Battle b, ActivePokemon caster, ActivePokemon victim, String castMessage) {
        ApplyResult result = this.applies(b, caster, victim);
        if (result.isSuccess()) {
            victim.setStatus(this);
            Messages.add(new MessageUpdate(castMessage).updatePokemon(b, victim));

            StatusReceivedEffect.invokeStatusReceivedEffect(b, caster, victim, this.namesies);
            OpponentStatusReceivedEffect.invokeOpponentStatusReceivedEffect(b, victim, this.namesies);
        }

        return result;
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

        @Override
        public String getSourcePreventionMessage(ActivePokemon victim, String sourceName) {
            return "";
        }
    }

    static class Fainted extends StatusCondition {
        private static final long serialVersionUID = 1L;

        Fainted() {
            super(StatusNamesies.FAINTED, "FNT", 1.0);
        }

        @Override
        public ApplyResult applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Fainted status condition applies regardless of other status conditions
            return ApplyResult.newResult(this.statusApplies(b, user, victim));
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

        @Override
        public String getSourcePreventionMessage(ActivePokemon victim, String sourceName) {
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
        public boolean canAttack(ActivePokemon attacking, ActivePokemon defending, Battle b) {
            if (RandomUtils.chanceTest(25)) {
                Messages.add(attacking.getName() + " is fully paralyzed!");
                return false;
            }

            return true;
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
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
        }

        @Override
        public String getSourcePreventionMessage(ActivePokemon victim, String sourceName) {
            return victim.getName() + "'s " + sourceName + " prevents paralysis!";
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return !p.hasAbility(AbilityNamesies.QUICK_FEET);
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

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (victim.hasAbility(AbilityNamesies.POISON_HEAL)) {
                victim.healHealthFraction(1/8.0, b, victim.getName() + "'s " + AbilityNamesies.POISON_HEAL.getName() + " restored its health!");
            } else {
                victim.reduceHealthFraction(b, 1/8.0, victim.getName() + " was hurt by its poison!");
            }
        }

        @Override
        public String getSourcePreventionMessage(ActivePokemon victim, String sourceName) {
            return victim.getName() + "'s " + sourceName + " prevents poison!";
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

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (victim.hasAbility(AbilityNamesies.POISON_HEAL)) {
                victim.healHealthFraction(1/8.0, b, victim.getName() + "'s " + AbilityNamesies.POISON_HEAL.getName() + " restored its health!");
            } else {
                victim.reduceHealthFraction(b, this.turns++/16.0, victim.getName() + " was hurt by its poison!");
            }
        }

        @Override
        public String getSourcePreventionMessage(ActivePokemon victim, String sourceName) {
            return victim.getName() + "'s " + sourceName + " prevents poison!";
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
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            double reduceFraction = victim.hasAbility(AbilityNamesies.HEATPROOF) ? 1/16.0 : 1/8.0;
            victim.reduceHealthFraction(b, reduceFraction, victim.getName() + " was hurt by its burn!");
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
        public boolean isModifyStat(Stat s) {
            return s == Stat.ATTACK;
        }

        @Override
        public String getSourcePreventionMessage(ActivePokemon victim, String sourceName) {
            return victim.getName() + "'s " + sourceName + " prevents burns!";
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return !p.hasAbility(AbilityNamesies.GUTS) && p.getAttack().namesies() != AttackNamesies.FACADE;
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
        public boolean canAttack(ActivePokemon attacking, ActivePokemon defending, Battle b) {
            if (numTurns == 0) {
                attacking.removeStatus(b, CastSource.EFFECT);
                return true;
            }

            numTurns--;

            Messages.add(attacking.getName() + " is fast asleep...");
            return SleepyFightsterEffect.containsSleepyFightsterEffect(b, attacking);
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
        public int getTurns() {
            return this.numTurns;
        }

        @Override
        public void setTurns(int turns) {
            this.numTurns = turns;
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
        public String getSourcePreventionMessage(ActivePokemon victim, String sourceName) {
            return victim.getName() + "'s " + sourceName + " prevents sleep!";
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
        public boolean canAttack(ActivePokemon attacking, ActivePokemon defending, Battle b) {
            // 20% chance to thaw out each turn
            if (RandomUtils.chanceTest(20) || attacking.getAttack().isMoveType(MoveType.DEFROST)) {
                attacking.removeStatus(b, CastSource.EFFECT);
                return true;
            }

            Messages.add(attacking.getName() + " is frozen solid!");
            return false;
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
        public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Fire-type moves defrost the user
            if (user.isAttackType(Type.FIRE)) {
                victim.removeStatus(b, CastSource.EFFECT);
            }
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
        public String getSourcePreventionMessage(ActivePokemon victim, String sourceName) {
            return victim.getName() + "'s " + sourceName + " prevents freezing!";
        }
    }
}
