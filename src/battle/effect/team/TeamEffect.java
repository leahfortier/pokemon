package battle.effect.team;

import battle.ActivePokemon;
import battle.Battle;
import battle.stages.StageModifier;
import battle.stages.ModifyStageMessenger;
import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.effect.ApplyResult;
import battle.effect.Effect;
import battle.effect.EffectInterfaces.EntryHazard;
import battle.effect.EffectInterfaces.SimpleStatModifyingEffect;
import battle.effect.EffectInterfaces.SwappableEffect;
import battle.effect.EffectNamesies;
import battle.effect.InvokeInterfaces.BarrierEffect;
import battle.effect.InvokeInterfaces.CritBlockerEffect;
import battle.effect.InvokeInterfaces.DefogRelease;
import battle.effect.InvokeInterfaces.EffectPreventionEffect;
import battle.effect.InvokeInterfaces.EndBattleEffect;
import battle.effect.InvokeInterfaces.EntryEffect;
import battle.effect.InvokeInterfaces.StatProtectingEffect;
import battle.effect.InvokeInterfaces.StatusPreventionEffect;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.source.CastSource;
import battle.effect.status.StatusNamesies;
import message.Messages;
import pokemon.ability.AbilityNamesies;
import pokemon.stat.Stat;
import trainer.Trainer;
import type.Type;
import util.serialization.Serializable;
import util.string.PokeString;

// Class to handle effects that are specific to one side of the battle
public abstract class TeamEffect extends Effect<TeamEffectNamesies> implements Serializable {
    private static final long serialVersionUID = 1L;

    public TeamEffect(TeamEffectNamesies name, int minTurns, int maxTurns, boolean canHave, boolean hasAlternateCast) {
        super(name, minTurns, maxTurns, canHave, hasAlternateCast);
    }

    @Override
    protected void addEffect(Battle b, ActivePokemon victim) {
        b.getTrainer(victim).getEffects().add(this);
    }

    @Override
    protected boolean hasEffect(Battle b, ActivePokemon victim) {
        return b.getTrainer(victim).hasEffect(this.namesies());
    }

    @Override
    protected TeamEffect getEffect(Battle b, ActivePokemon victim) {
        return b.getTrainer(victim).getEffects().get(this.namesies());
    }

    // EVERYTHING BELOW IS GENERATED ###

    /**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

    static class Reflect extends TeamEffect implements BarrierEffect, DefogRelease {
        private static final long serialVersionUID = 1L;

        Reflect() {
            super(TeamEffectNamesies.REFLECT, 5, 5, false, false);
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.DEFENSE;
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " raised the " + Stat.DEFENSE.getName().toLowerCase() + " of its team!";
        }

        @Override
        public String getBreakMessage(ActivePokemon breaker) {
            return breaker.getName() + " broke the reflect barrier!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The effects of reflect faded.";
        }

        @Override
        public String getDefogReleaseMessage() {
            return "The effects of reflect faded.";
        }
    }

    static class LightScreen extends TeamEffect implements BarrierEffect, DefogRelease {
        private static final long serialVersionUID = 1L;

        LightScreen() {
            super(TeamEffectNamesies.LIGHT_SCREEN, 5, 5, false, false);
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SP_DEFENSE;
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " raised the " + Stat.SP_DEFENSE.getName().toLowerCase() + " of its team!";
        }

        @Override
        public String getBreakMessage(ActivePokemon breaker) {
            return breaker.getName() + " broke the light screen barrier!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The effects of light screen faded.";
        }

        @Override
        public String getDefogReleaseMessage() {
            return "The effects of light screen faded.";
        }
    }

    static class Tailwind extends TeamEffect implements SwappableEffect, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        Tailwind() {
            super(TeamEffectNamesies.TAILWIND, 4, 4, false, false);
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The effects of tailwind faded.";
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " raised the speed of its team!";
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
        }

        @Override
        public double getModifier() {
            return 2;
        }
    }

    static class AuroraVeil extends TeamEffect implements BarrierEffect, DefogRelease {
        private static final long serialVersionUID = 1L;

        AuroraVeil() {
            super(TeamEffectNamesies.AURORA_VEIL, 5, 5, false, false);
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.DEFENSE || s == Stat.SP_DEFENSE;
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " is covered by an aurora veil!";
        }

        @Override
        public String getBreakMessage(ActivePokemon breaker) {
            return breaker.getName() + " broke the aurora veil barrier!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The effects of aurora veil faded.";
        }

        @Override
        public String getDefogReleaseMessage() {
            return "The effects of aurora veil faded.";
        }
    }

    static class StickyWeb extends TeamEffect implements EntryHazard, ModifyStageMessenger {
        private static final long serialVersionUID = 1L;

        StickyWeb() {
            super(TeamEffectNamesies.STICKY_WEB, -1, -1, false, false);
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            if (!enterer.isOnTheGround(b)) {
                return;
            }

            // The sticky web lowered Charmander's Speed!
            new StageModifier(-1, Stat.SPEED).withMessage(this).modify(b, enterer, enterer, CastSource.EFFECT);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return "Sticky web covers everything!";
        }

        @Override
        public String getReleaseMessage() {
            return "The sticky web dispersed!";
        }

        @Override
        public String getMessage(String victimName, String possessiveVictim, String statName, String changed) {
            return "The sticky web " + changed + " " + victimName + "'s " + statName + "!";
        }
    }

    static class StealthRock extends TeamEffect implements EntryHazard {
        private static final long serialVersionUID = 1L;

        StealthRock() {
            super(TeamEffectNamesies.STEALTH_ROCK, -1, -1, false, false);
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            double advantage = Type.ROCK.getAdvantage().getAdvantage(enterer, b);
            enterer.reduceHealthFraction(b, advantage/8.0, enterer.getName() + " was hurt by stealth rock!");
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return "Floating rocks were scattered all around!";
        }

        @Override
        public String getReleaseMessage() {
            return "The floating rocks dispersed!";
        }
    }

    static class ToxicSpikes extends TeamEffect implements EntryHazard {
        private static final long serialVersionUID = 1L;

        private int layers;

        ToxicSpikes() {
            super(TeamEffectNamesies.TOXIC_SPIKES, -1, -1, true, true);
            this.layers = 1;
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            // Can't touch this
            if (!enterer.isOnTheGround(b)) {
                return;
            }

            // Poison-type Pokes absorb Toxic Spikes
            if (enterer.isType(b, Type.POISON)) {
                Messages.add(enterer.getName() + " absorbed the Toxic Spikes!");
                this.deactivate();
                return;
            }

            // Poison those bros
            ActivePokemon theOtherPokemon = b.getOtherPokemon(enterer);
            StatusNamesies poisonCondition = layers >= 2 ? StatusNamesies.BADLY_POISONED : StatusNamesies.POISONED;
            poisonCondition.getStatus().apply(b, theOtherPokemon, enterer, CastSource.EFFECT);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return "Toxic spikes were scattered all around!";
        }

        @Override
        public void alternateCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, CastMessageGetter castMessage) {
            this.addCastMessage(b, caster, victim, source, castMessage);
            this.layers++;
        }

        @Override
        public String getReleaseMessage() {
            return "The toxic spikes dispersed!";
        }
    }

    static class Spikes extends TeamEffect implements EntryHazard {
        private static final long serialVersionUID = 1L;

        private int layers;

        private double getReduceFraction() {
            switch (layers) {
                case 1:
                    return 1/8.0;
                case 2:
                    return 1/6.0;
                default:
                    return 1/4.0;
            }
        }

        Spikes() {
            super(TeamEffectNamesies.SPIKES, -1, -1, true, true);
            this.layers = 1;
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            if (enterer.isOnTheGround(b)) {
                enterer.reduceHealthFraction(b, this.getReduceFraction(), enterer.getName() + " was hurt by spikes!");
            }
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return "Spikes were scattered all around!";
        }

        @Override
        public void alternateCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, CastMessageGetter castMessage) {
            this.addCastMessage(b, caster, victim, source, castMessage);
            this.layers++;
        }

        @Override
        public String getReleaseMessage() {
            return "The spikes dispersed!";
        }
    }

    static class Wish extends TeamEffect {
        private static final long serialVersionUID = 1L;

        private String casterName;

        Wish() {
            super(TeamEffectNamesies.WISH, 2, 2, false, false);
        }

        @Override
        public void subside(Battle b, ActivePokemon p) {
            Messages.add(casterName + "'s wish came true!");
            p.healHealthFraction(1/2.0, b, p.getName() + "'s health was restored!");
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            casterName = caster.getName();
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return casterName + " made a wish!";
        }
    }

    static class LuckyChant extends TeamEffect implements CritBlockerEffect {
        private static final long serialVersionUID = 1L;

        LuckyChant() {
            super(TeamEffectNamesies.LUCKY_CHANT, 5, 5, false, false);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return "The lucky chant shielded " + victim.getName() + "'s team from critical hits!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The effects of lucky chant wore off.";
        }
    }

    static class FutureSight extends TeamEffect {
        private static final long serialVersionUID = 1L;

        private ActivePokemon theSeeer;

        FutureSight() {
            super(TeamEffectNamesies.FUTURE_SIGHT, 3, 3, false, false);
        }

        @Override
        public void subside(Battle b, ActivePokemon p) {
            Messages.add(p.getName() + " took " + theSeeer.getName() + "'s attack!");

            Attack attack = AttackNamesies.FUTURE_SIGHT.getNewAttack();

            // Don't do anything for moves that are uneffective
            if (!attack.effective(b, theSeeer, p)) {
                return;
            }

            theSeeer.callTempMove(attack.namesies(), () -> theSeeer.getAttack().applyDamage(theSeeer, p, b));
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            theSeeer = caster;
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return theSeeer.getName() + " foresaw an attack!";
        }
    }

    static class DoomDesire extends TeamEffect {
        private static final long serialVersionUID = 1L;

        private ActivePokemon theSeeer;

        DoomDesire() {
            super(TeamEffectNamesies.DOOM_DESIRE, 3, 3, false, false);
        }

        @Override
        public void subside(Battle b, ActivePokemon p) {
            Messages.add(p.getName() + " took " + theSeeer.getName() + "'s attack!");

            Attack attack = AttackNamesies.DOOM_DESIRE.getNewAttack();

            // Don't do anything for moves that are uneffective
            if (!attack.effective(b, theSeeer, p)) {
                return;
            }

            theSeeer.callTempMove(attack.namesies(), () -> theSeeer.getAttack().applyDamage(theSeeer, p, b));
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            theSeeer = caster;
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return theSeeer.getName() + " foresaw an attack!";
        }
    }

    static class HealSwitch extends TeamEffect implements EntryEffect {
        private static final long serialVersionUID = 1L;

        private String wish;

        HealSwitch() {
            super(TeamEffectNamesies.HEAL_SWITCH, -1, -1, false, false);
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            enterer.removeStatus();
            enterer.healHealthFraction(1, b, enterer.getName() + " health was restored due to the " + wish + "!");
            this.deactivate();
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            wish = caster.getAttack().namesies() == AttackNamesies.LUNAR_DANCE ? "lunar dance" : "healing wish";
        }
    }

    static class DeadAlly extends TeamEffect {
        private static final long serialVersionUID = 1L;

        DeadAlly() {
            super(TeamEffectNamesies.DEAD_ALLY, 2, 2, true, false);
        }
    }

    static class PayDay extends TeamEffect implements EndBattleEffect {
        private static final long serialVersionUID = 1L;

        private int coins;

        PayDay() {
            super(TeamEffectNamesies.PAY_DAY, -1, -1, true, true);
        }

        @Override
        public void alternateCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, CastMessageGetter castMessage) {
            // TODO: This should be a battle effect since it only applies to the player
            this.addCastMessage(b, caster, victim, source, castMessage);
            this.coins += 5*caster.getLevel();
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            coins = 5*caster.getLevel();
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return "Coins scattered everywhere!";
        }

        @Override
        public void afterBattle(Trainer player, ActivePokemon p) {
            Messages.add(player.getName() + " picked up " + coins + " " + PokeString.POKEDOLLARS + "!");
            player.getDatCashMoney(coins);
        }
    }

    static class Safeguard extends TeamEffect implements DefogRelease, StatusPreventionEffect, EffectPreventionEffect {
        private static final long serialVersionUID = 1L;

        Safeguard() {
            super(TeamEffectNamesies.SAFEGUARD, 5, 5, false, false);
        }

        @Override
        public String getDefogReleaseMessage() {
            return "The effects of Safeguard faded.";
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " is covered by a veil!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The effects of Safeguard faded.";
        }

        @Override
        public ApplyResult preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusNamesies status) {
            if (caster != victim && !caster.hasAbility(AbilityNamesies.INFILTRATOR)) {
                return ApplyResult.failure("Safeguard prevents status conditions!");
            }

            return ApplyResult.success();
        }

        @Override
        public ApplyResult preventEffect(Battle b, ActivePokemon caster, ActivePokemon victim, EffectNamesies effectName) {
            if (effectName == PokemonEffectNamesies.CONFUSION && caster != victim && !caster.hasAbility(AbilityNamesies.INFILTRATOR)) {
                return ApplyResult.failure("Safeguard prevents confusion!");
            }

            return ApplyResult.success();
        }
    }

    static class Mist extends TeamEffect implements StatProtectingEffect, DefogRelease {
        private static final long serialVersionUID = 1L;

        Mist() {
            super(TeamEffectNamesies.MIST, 5, 5, false, false);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " shrouded itself in mist!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The mist faded.";
        }

        @Override
        public String getDefogReleaseMessage() {
            return "The mist faded.";
        }

        @Override
        public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
            return !caster.hasAbility(AbilityNamesies.INFILTRATOR);
        }

        @Override
        public String preventionMessage(ActivePokemon p, Stat s) {
            return "The mist prevents stat reductions!";
        }
    }

    static class GuardSpecial extends TeamEffect implements StatProtectingEffect {
        private static final long serialVersionUID = 1L;

        GuardSpecial() {
            super(TeamEffectNamesies.GUARD_SPECIAL, 5, 5, false, false);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " is shrouded by a veil!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The effects of Guard Special faded.";
        }

        @Override
        public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
            return !caster.hasAbility(AbilityNamesies.INFILTRATOR);
        }

        @Override
        public String preventionMessage(ActivePokemon p, Stat s) {
            return "Guard Special prevents stat reductions!";
        }
    }

    static class GetDatCashMoneyTwice extends TeamEffect {
        private static final long serialVersionUID = 1L;

        GetDatCashMoneyTwice() {
            super(TeamEffectNamesies.GET_DAT_CASH_MONEY_TWICE, -1, -1, false, false);
        }
    }
}
