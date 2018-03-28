package battle.effect.team;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.effect.Effect;
import battle.effect.InvokeInterfaces.BarrierEffect;
import battle.effect.InvokeInterfaces.CritBlockerEffect;
import battle.effect.InvokeInterfaces.DefogRelease;
import battle.effect.InvokeInterfaces.EndBattleEffect;
import battle.effect.InvokeInterfaces.EntryEffect;
import battle.effect.InvokeInterfaces.RapidSpinRelease;
import battle.effect.InvokeInterfaces.SimpleStatModifyingEffect;
import battle.effect.InvokeInterfaces.StatusPreventionEffect;
import battle.effect.source.CastSource;
import battle.effect.status.StatusNamesies;
import item.ItemNamesies;
import message.Messages;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import trainer.Trainer;
import type.Type;
import util.serialization.Serializable;

// Class to handle effects that are specific to one side of the battle
public abstract class TeamEffect extends Effect<TeamEffectNamesies> implements Serializable {
    private static final long serialVersionUID = 1L;

    public TeamEffect(TeamEffectNamesies name, int minTurns, int maxTurns, boolean nextTurnSubside, boolean hasAlternateCast) {
        super(name, minTurns, maxTurns, nextTurnSubside, hasAlternateCast);
    }

    @Override
    protected void addEffect(Battle b, ActivePokemon victim) {
        b.getTrainer(victim).getEffects().add(this);
    }

    @Override
    protected boolean hasEffect(Battle b, ActivePokemon victim) {
        return b.getTrainer(victim).hasEffect(this.namesies());
    }

    // EVERYTHING BELOW IS GENERATED ###

    /**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

    static class Reflect extends TeamEffect implements BarrierEffect, DefogRelease, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        Reflect() {
            super(TeamEffectNamesies.REFLECT, 5, 5, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getTrainer(victim).hasEffect(this.namesies()));
        }

        @Override
        public String getBreakMessage(ActivePokemon breaker) {
            return breaker.getName() + " broke the reflect barrier!";
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " raised the " + Stat.DEFENSE.getName().toLowerCase() + " of its team!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The effects of reflect faded.";
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return !opp.hasAbility(AbilityNamesies.INFILTRATOR);
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.DEFENSE;
        }

        @Override
        public String getDefogReleaseMessage(ActivePokemon released) {
            return "The effects of reflect faded.";
        }

        @Override
        public void afterCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            if (caster.isHoldingItem(b, ItemNamesies.LIGHT_CLAY)) {
                this.setTurns(8);
            }
        }

        @Override
        public double getModifier() {
            return 2;
        }
    }

    static class LightScreen extends TeamEffect implements BarrierEffect, DefogRelease, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        LightScreen() {
            super(TeamEffectNamesies.LIGHT_SCREEN, 5, 5, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getTrainer(victim).hasEffect(this.namesies()));
        }

        @Override
        public String getBreakMessage(ActivePokemon breaker) {
            return breaker.getName() + " broke the light screen barrier!";
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " raised the " + Stat.SP_DEFENSE.getName().toLowerCase() + " of its team!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The effects of light screen faded.";
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return !opp.hasAbility(AbilityNamesies.INFILTRATOR);
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SP_DEFENSE;
        }

        @Override
        public String getDefogReleaseMessage(ActivePokemon released) {
            return "The effects of light screen faded.";
        }

        @Override
        public void afterCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            if (caster.isHoldingItem(b, ItemNamesies.LIGHT_CLAY)) {
                this.setTurns(8);
            }
        }

        @Override
        public double getModifier() {
            return 2;
        }
    }

    static class Tailwind extends TeamEffect implements SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        Tailwind() {
            super(TeamEffectNamesies.TAILWIND, 4, 4, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getTrainer(victim).hasEffect(this.namesies()));
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " raised the speed of its team!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The effects of tailwind faded.";
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

    static class AuroraVeil extends TeamEffect implements BarrierEffect, DefogRelease, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        AuroraVeil() {
            super(TeamEffectNamesies.AURORA_VEIL, 5, 5, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getTrainer(victim).hasEffect(this.namesies()));
        }

        @Override
        public String getBreakMessage(ActivePokemon breaker) {
            return breaker.getName() + " broke the aurora veil barrier!";
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " is covered by an aurora veil!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The effects of aurora veil faded.";
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return !opp.hasAbility(AbilityNamesies.INFILTRATOR);
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.DEFENSE || s == Stat.SP_DEFENSE;
        }

        @Override
        public String getDefogReleaseMessage(ActivePokemon released) {
            return "The effects of aurora veil faded.";
        }

        @Override
        public void afterCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            if (caster.isHoldingItem(b, ItemNamesies.LIGHT_CLAY)) {
                this.setTurns(8);
            }
        }

        @Override
        public double getModifier() {
            return 2;
        }
    }

    static class StickyWeb extends TeamEffect implements EntryEffect, RapidSpinRelease, DefogRelease {
        private static final long serialVersionUID = 1L;

        StickyWeb() {
            super(TeamEffectNamesies.STICKY_WEB, -1, -1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getTrainer(victim).hasEffect(this.namesies()));
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return "Sticky web covers everything!";
        }

        @Override
        public String getRapidSpinReleaseMessage(ActivePokemon released) {
            return "The sticky web spun away!";
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            if (enterer.isLevitating(b)) {
                return;
            }

            // The sticky web lowered Charmander's Speed!
            enterer.getStages().modifyStage(
                    b.getOtherPokemon(enterer), -1, Stat.SPEED, b, CastSource.EFFECT,
                    (victimName, statName, changed) -> "The sticky web " + changed + " " + enterer.getName() + "'s " + statName + "!"
            );
        }

        @Override
        public String getDefogReleaseMessage(ActivePokemon released) {
            return "The sticky web dispersed!";
        }
    }

    static class StealthRock extends TeamEffect implements EntryEffect, RapidSpinRelease, DefogRelease {
        private static final long serialVersionUID = 1L;

        StealthRock() {
            super(TeamEffectNamesies.STEALTH_ROCK, -1, -1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getTrainer(victim).hasEffect(this.namesies()));
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return "Floating rocks were scattered all around!";
        }

        @Override
        public String getRapidSpinReleaseMessage(ActivePokemon released) {
            return "The floating rocks spun away!";
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            double advantage = Type.ROCK.getAdvantage().getAdvantage(enterer, b);
            enterer.reduceHealthFraction(b, advantage/8.0, enterer.getName() + " was hurt by stealth rock!");
        }

        @Override
        public String getDefogReleaseMessage(ActivePokemon released) {
            return "The floating rocks dispersed!";
        }
    }

    static class ToxicSpikes extends TeamEffect implements EntryEffect, RapidSpinRelease, DefogRelease {
        private static final long serialVersionUID = 1L;

        private int layers;

        ToxicSpikes() {
            super(TeamEffectNamesies.TOXIC_SPIKES, -1, -1, false, true);
            this.layers = 1;
        }

        @Override
        public void alternateCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
            ToxicSpikes spikesies = (ToxicSpikes)b.getEffects(victim).get(this.namesies);
            spikesies.layers++;

            this.addCastMessage(b, caster, victim, source, printCast);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return "Toxic spikes were scattered all around!";
        }

        @Override
        public String getRapidSpinReleaseMessage(ActivePokemon released) {
            return "The toxic spikes dispersed!";
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            if (enterer.isLevitating(b)) {
                return;
            }

            if (enterer.isType(b, Type.POISON)) {
                Messages.add(enterer.getName() + " absorbed the Toxic Spikes!");
                super.active = false;
                return;
            }

            ActivePokemon theOtherPokemon = b.getOtherPokemon(enterer);
            StatusNamesies poisonCondition = layers >= 2 ? StatusNamesies.BADLY_POISONED : StatusNamesies.POISONED;
            poisonCondition.getStatus().apply(b, theOtherPokemon, enterer, CastSource.EFFECT);
        }

        @Override
        public String getDefogReleaseMessage(ActivePokemon released) {
            return "The toxic spikes dispersed!";
        }
    }

    static class Spikes extends TeamEffect implements EntryEffect, RapidSpinRelease, DefogRelease {
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
            super(TeamEffectNamesies.SPIKES, -1, -1, false, true);
            this.layers = 1;
        }

        @Override
        public void alternateCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
            Spikes spikesies = (Spikes)b.getEffects(victim).get(this.namesies);
            spikesies.layers++;

            this.addCastMessage(b, caster, victim, source, printCast);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return "Spikes were scattered all around!";
        }

        @Override
        public String getRapidSpinReleaseMessage(ActivePokemon released) {
            return "The spikes dispersed!";
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            if (!enterer.isLevitating(b)) {
                enterer.reduceHealthFraction(b, this.getReduceFraction(), enterer.getName() + " was hurt by spikes!");
            }
        }

        @Override
        public String getDefogReleaseMessage(ActivePokemon released) {
            return "The spikes dispersed!";
        }
    }

    static class Wish extends TeamEffect {
        private static final long serialVersionUID = 1L;

        private String casterName;

        Wish() {
            super(TeamEffectNamesies.WISH, 1, 1, true, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getTrainer(victim).hasEffect(this.namesies()));
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            casterName = caster.getName();
        }

        @Override
        public void subside(Battle b, ActivePokemon p) {
            p.healHealthFraction(1/2.0, b, casterName + "'s wish came true!");
        }
    }

    static class LuckyChant extends TeamEffect implements CritBlockerEffect {
        private static final long serialVersionUID = 1L;

        LuckyChant() {
            super(TeamEffectNamesies.LUCKY_CHANT, 5, 5, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getTrainer(victim).hasEffect(this.namesies()));
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
            super(TeamEffectNamesies.FUTURE_SIGHT, 2, 2, true, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getTrainer(victim).hasEffect(this.namesies()));
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            theSeeer = caster;
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return theSeeer.getName() + " foresaw an attack!";
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
    }

    static class DoomDesire extends TeamEffect {
        private static final long serialVersionUID = 1L;

        private ActivePokemon theSeeer;

        DoomDesire() {
            super(TeamEffectNamesies.DOOM_DESIRE, 2, 2, true, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getTrainer(victim).hasEffect(this.namesies()));
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            theSeeer = caster;
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return theSeeer.getName() + " foresaw an attack!";
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
    }

    static class HealSwitch extends TeamEffect implements EntryEffect {
        private static final long serialVersionUID = 1L;

        private String wish;

        HealSwitch() {
            super(TeamEffectNamesies.HEAL_SWITCH, -1, -1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getTrainer(victim).hasEffect(this.namesies()));
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            wish = caster.getAttack().namesies() == AttackNamesies.LUNAR_DANCE ? "lunar dance" : "healing wish";
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            enterer.removeStatus();
            enterer.healHealthFraction(1, b, enterer.getName() + " health was restored due to the " + wish + "!");
            super.active = false;
        }
    }

    static class DeadAlly extends TeamEffect {
        private static final long serialVersionUID = 1L;

        DeadAlly() {
            super(TeamEffectNamesies.DEAD_ALLY, 2, 2, false, false);
        }
    }

    static class PayDay extends TeamEffect implements EndBattleEffect {
        private static final long serialVersionUID = 1L;

        private int coins;

        PayDay() {
            super(TeamEffectNamesies.PAY_DAY, -1, -1, false, true);
        }

        @Override
        public void alternateCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
            PayDay payday = (PayDay)b.getEffects(true).get(this.namesies);
            payday.coins += 5*caster.getLevel();

            this.addCastMessage(b, caster, victim, source, printCast);
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
        public void afterBattle(Trainer player, Battle b, ActivePokemon p) {
            Messages.add(player.getName() + " picked up " + coins + " pokedollars!");
            player.getDatCashMoney(coins);
        }
    }

    static class Safeguard extends TeamEffect implements StatusPreventionEffect, DefogRelease {
        private static final long serialVersionUID = 1L;

        Safeguard() {
            super(TeamEffectNamesies.SAFEGUARD, 5, 5, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getTrainer(victim).hasEffect(this.namesies()));
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " is covered by a veil!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The effects of " + victim.getName() + "'s Safeguard faded.";
        }

        @Override
        public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusNamesies status) {
            return !caster.hasAbility(AbilityNamesies.INFILTRATOR);
        }

        @Override
        public String statusPreventionMessage(ActivePokemon victim) {
            return "Safeguard protects " + victim.getName() + " from status conditions!";
        }

        @Override
        public String getDefogReleaseMessage(ActivePokemon released) {
            return "The effects of " + released.getName() + "'s Safeguard faded.";
        }
    }

    static class GuardSpecial extends TeamEffect implements StatusPreventionEffect {
        private static final long serialVersionUID = 1L;

        GuardSpecial() {
            super(TeamEffectNamesies.GUARD_SPECIAL, 5, 5, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getTrainer(victim).hasEffect(this.namesies()));
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " is covered by a veil!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The effects of " + victim.getName() + "'s Guard Special faded.";
        }

        @Override
        public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusNamesies status) {
            return !caster.hasAbility(AbilityNamesies.INFILTRATOR);
        }

        @Override
        public String statusPreventionMessage(ActivePokemon victim) {
            return "Guard Special protects " + victim.getName() + " from status conditions!";
        }
    }

    static class GetDatCashMoneyTwice extends TeamEffect {
        private static final long serialVersionUID = 1L;

        GetDatCashMoneyTwice() {
            super(TeamEffectNamesies.GET_DAT_CASH_MONEY_TWICE, -1, -1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getTrainer(victim).hasEffect(this.namesies()));
        }
    }
}
