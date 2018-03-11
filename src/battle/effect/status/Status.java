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
import main.Global;
import message.MessageUpdate;
import message.Messages;
import pokemon.PartyPokemon;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import type.Type;
import util.RandomUtils;
import util.serialization.Serializable;

public abstract class Status implements InvokeEffect, Serializable {
    private static final long serialVersionUID = 1L;

    private final StatusNamesies namesies;
    private final String shortName;
    private final double catchModifier;

    // TODO: Delete this
    protected Status(StatusNamesies statusCondition) {
        this(statusCondition, "", 1);
    }

    protected Status(StatusNamesies namesies, String shortName, double catchModifier) {
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

    private String getRemoveMessage(Battle b, ActivePokemon victim, CastSource source) {
        if (source.hasSourceName()) {
            return this.getSourceRemoveMessage(victim, source.getSourceName(b, victim));
        } else {
            return this.getGenericRemoveMessage(victim);
        }
    }

    private String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
        StatusPreventionEffect statusPrevent = StatusPreventionEffect.getPreventEffect(b, user, victim, this.namesies);
        if (statusPrevent != null) {
            return statusPrevent.statusPreventionMessage(victim);
        }

        return Effect.DEFAULT_FAIL_MESSAGE;
    }

    private boolean appliesWithoutStatusCheck(Battle b, ActivePokemon caster, ActivePokemon victim) {
        return this.statusApplies(b, caster, victim) &&
                StatusPreventionEffect.getPreventEffect(b, caster, victim, this.namesies) == null;
    }

    protected boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim) {
        return !victim.hasStatus() && this.appliesWithoutStatusCheck(b, caster, victim);
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

    public static void removeStatus(Battle b, ActivePokemon victim, CastSource source) {
        Status status = victim.getStatus();
        victim.removeStatus();

        Messages.add(new MessageUpdate(status.getRemoveMessage(b, victim, source)).updatePokemon(b, victim));
    }

    public static String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim, StatusNamesies status) {
        return status.getStatus().getFailMessage(b, user, victim);
    }

    public static boolean appliesWithoutStatusCheck(StatusNamesies status, Battle b, ActivePokemon caster, ActivePokemon victim) {
        return status.getStatus().appliesWithoutStatusCheck(b, caster, victim);
    }

    public static boolean applies(StatusNamesies status, Battle b, ActivePokemon caster, ActivePokemon victim) {
        return status.getStatus().applies(b, caster, victim);
    }

    // Returns true if a status was successfully given, and false if it failed for any reason
    public static boolean applyStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusNamesies status) {
        return applyStatus(b, caster, victim, status, CastSource.EFFECT);
    }

    public static boolean applyStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusNamesies status, CastSource source) {
        Status s = status.getStatus();
        return applyStatus(b, caster, victim, status, s.getCastMessage(b, caster, victim, source));
    }

    public static boolean applyStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusNamesies status, String castMessage) {
        Status s = status.getStatus();
        if (s.applies(b, caster, victim)) {
            victim.setStatus(s);
            Messages.add(new MessageUpdate(castMessage).updatePokemon(b, victim));

            StatusReceivedEffect.invokeStatusReceivedEffect(b, caster, victim, status);
            OpponentStatusReceivedEffect.invokeOpponentStatusReceivedEffect(b, victim, status);
            return true;
        }

        return false;
    }

    public static void removeStatus(PartyPokemon p) {
        p.setStatus(new NoStatus());
    }

    public static void die(Battle b, ActivePokemon murderer, ActivePokemon deady) {
        if (deady.getHP() > 0) {
            Global.error("Only dead Pokemon can die.");
        }

        applyStatus(b, murderer, deady, StatusNamesies.FAINTED);
    }

    // EVERYTHING BELOW IS GENERATED ###

    /**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

    static class NoStatus extends Status {
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

    static class Fainted extends Status {
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
    static class Paralyzed extends Status implements BeforeTurnEffect, SimpleStatModifyingEffect {
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
    static class Poisoned extends Status implements EndTurnEffect {
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
    static class BadlyPoisoned extends Status implements EndTurnEffect {
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
    static class Burned extends Status implements EndTurnEffect, SimpleStatModifyingEffect {
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
    static class Asleep extends Status implements BeforeTurnEffect {
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
                Status.removeStatus(b, p, CastSource.EFFECT);
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
    static class Frozen extends Status implements BeforeTurnEffect, TakeDamageEffect {
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
                Status.removeStatus(b, p, CastSource.EFFECT);

                return true;
            }

            Messages.add(p.getName() + " is frozen solid!");
            return false;
        }

        @Override
        public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Fire-type moves defrost the user
            if (user.isAttackType(Type.FIRE)) {
                Status.removeStatus(b, victim, CastSource.EFFECT);
            }
        }
    }
}
