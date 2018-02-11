package battle.effect.generic;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.effect.CastSource;
import battle.effect.generic.EffectInterfaces.BarrierEffect;
import battle.effect.generic.EffectInterfaces.CritBlockerEffect;
import battle.effect.generic.EffectInterfaces.DefogRelease;
import battle.effect.generic.EffectInterfaces.EndBattleEffect;
import battle.effect.generic.EffectInterfaces.EntryEffect;
import battle.effect.generic.EffectInterfaces.RapidSpinRelease;
import battle.effect.generic.EffectInterfaces.SimpleStatModifyingEffect;
import battle.effect.status.Status;
import battle.effect.status.StatusCondition;
import item.ItemNamesies;
import message.MessageUpdate;
import message.Messages;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import trainer.Trainer;
import type.Type;

import java.io.Serializable;

// Class to handle effects that are specific to one side of the battle
public abstract class TeamEffect extends Effect implements Serializable {
    private static final long serialVersionUID = 1L;

    public TeamEffect(EffectNamesies name, int minTurns, int maxTurns, boolean nextTurnSubside) {
        super(name, minTurns, maxTurns, nextTurnSubside);
    }

    @Override
    public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
        if (printCast) {
            Messages.add(getCastMessage(b, caster, victim, source));
        }

        b.getTrainer(victim).getEffects().add(this);

        Messages.add(new MessageUpdate().updatePokemon(b, caster));
        Messages.add(new MessageUpdate().updatePokemon(b, victim));
    }

    // EVERYTHING BELOW IS GENERATED ###

    /**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

    static class Reflect extends TeamEffect implements BarrierEffect, DefogRelease, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        Reflect() {
            super(EffectNamesies.REFLECT, 5, 5, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getTrainer(victim).hasEffect(this.namesies));
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
        public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
            super.cast(b, caster, victim, source, printCast);
            if (caster.isHoldingItem(b, ItemNamesies.LIGHT_CLAY)) {
                b.getEffects(victim).get(this.namesies).setTurns(8);
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
            super(EffectNamesies.LIGHT_SCREEN, 5, 5, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getTrainer(victim).hasEffect(this.namesies));
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
        public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
            super.cast(b, caster, victim, source, printCast);
            if (caster.isHoldingItem(b, ItemNamesies.LIGHT_CLAY)) {
                b.getEffects(victim).get(this.namesies).setTurns(8);
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
            super(EffectNamesies.TAILWIND, 4, 4, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getTrainer(victim).hasEffect(this.namesies));
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
            super(EffectNamesies.AURORA_VEIL, 5, 5, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getTrainer(victim).hasEffect(this.namesies));
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
        public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
            super.cast(b, caster, victim, source, printCast);
            if (caster.isHoldingItem(b, ItemNamesies.LIGHT_CLAY)) {
                b.getEffects(victim).get(this.namesies).setTurns(8);
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
            super(EffectNamesies.STICKY_WEB, -1, -1, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getTrainer(victim).hasEffect(this.namesies));
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
            super(EffectNamesies.STEALTH_ROCK, -1, -1, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getTrainer(victim).hasEffect(this.namesies));
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
            if (enterer.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
                return;
            }

            Messages.add(enterer.getName() + " was hurt by stealth rock!");
            enterer.reduceHealthFraction(b, Type.ROCK.getAdvantage().getAdvantage(enterer, b)/8.0);
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
            super(EffectNamesies.TOXIC_SPIKES, -1, -1, false);
            this.layers = 1;
        }

        @Override
        public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
            Effect spikesies = b.getEffects(victim).get(this.namesies);
            if (spikesies == null) {
                super.cast(b, caster, victim, source, printCast);
                return;
            }

            ((ToxicSpikes)spikesies).layers++;
            Messages.add(getCastMessage(b, caster, victim, source));
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
            StatusCondition poisonCondition = layers >= 2 ? StatusCondition.BADLY_POISONED : StatusCondition.POISONED;
            Status.applyStatus(b, theOtherPokemon, enterer, poisonCondition);
        }

        @Override
        public String getDefogReleaseMessage(ActivePokemon released) {
            return "The toxic spikes dispersed!";
        }
    }

    static class Spikes extends TeamEffect implements EntryEffect, RapidSpinRelease, DefogRelease {
        private static final long serialVersionUID = 1L;

        private int layers;

        Spikes() {
            super(EffectNamesies.SPIKES, -1, -1, false);
            this.layers = 1;
        }

        @Override
        public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
            Effect spikesies = b.getEffects(victim).get(this.namesies);
            if (spikesies == null) {
                super.cast(b, caster, victim, source, printCast);
                return;
            }

            ((Spikes)spikesies).layers++;
            Messages.add(getCastMessage(b, caster, victim, source));
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
            if (enterer.isLevitating(b) || enterer.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
                return;
            }

            Messages.add(enterer.getName() + " was hurt by spikes!");
            if (layers == 1) {
                enterer.reduceHealthFraction(b, 1/8.0);
            } else if (layers == 2) {
                enterer.reduceHealthFraction(b, 1/6.0);
            } else {
                enterer.reduceHealthFraction(b, 1/4.0);
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
            super(EffectNamesies.WISH, 1, 1, true);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getTrainer(victim).hasEffect(this.namesies));
        }

        @Override
        public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
            casterName = caster.getName();
            super.cast(b, caster, victim, source, printCast);
        }

        @Override
        public void subside(Battle b, ActivePokemon p) {
            if (p.hasEffect(EffectNamesies.HEAL_BLOCK)) {
                return;
            }

            p.healHealthFraction(1/2.0);
            Messages.add(new MessageUpdate(casterName + "'s wish came true!").updatePokemon(b, p));
        }
    }

    static class LuckyChant extends TeamEffect implements CritBlockerEffect {
        private static final long serialVersionUID = 1L;

        LuckyChant() {
            super(EffectNamesies.LUCKY_CHANT, 5, 5, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getTrainer(victim).hasEffect(this.namesies));
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
            super(EffectNamesies.FUTURE_SIGHT, 2, 2, true);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getTrainer(victim).hasEffect(this.namesies));
        }

        @Override
        public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
            theSeeer = caster;
            super.cast(b, caster, victim, source, printCast);
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
            super(EffectNamesies.DOOM_DESIRE, 2, 2, true);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getTrainer(victim).hasEffect(this.namesies));
        }

        @Override
        public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
            theSeeer = caster;
            super.cast(b, caster, victim, source, printCast);
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
            super(EffectNamesies.HEAL_SWITCH, -1, -1, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getTrainer(victim).hasEffect(this.namesies));
        }

        @Override
        public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
            wish = caster.getAttack().namesies() == AttackNamesies.LUNAR_DANCE ? "lunar dance" : "healing wish";
            super.cast(b, caster, victim, source, printCast);
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            enterer.healHealthFraction(1);
            enterer.removeStatus();

            Messages.add(new MessageUpdate(enterer.getName() + " health was restored due to the " + wish + "!").updatePokemon(b, enterer));
            super.active = false;
        }
    }

    static class DeadAlly extends TeamEffect {
        private static final long serialVersionUID = 1L;

        DeadAlly() {
            super(EffectNamesies.DEAD_ALLY, 2, 2, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getTrainer(victim).hasEffect(this.namesies));
        }
    }

    static class PayDay extends TeamEffect implements EndBattleEffect {
        private static final long serialVersionUID = 1L;

        private int coins;

        PayDay() {
            super(EffectNamesies.PAY_DAY, -1, -1, false);
        }

        @Override
        public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
            PayDay payday = (PayDay)b.getEffects(true).get(this.namesies);
            Messages.add(getCastMessage(b, caster, victim, source));
            coins = 5*caster.getLevel();
            if (payday == null) {
                b.getPlayer().getEffects().add(this);
            } else {
                payday.coins += coins;
            }
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

    static class GetDatCashMoneyTwice extends TeamEffect {
        private static final long serialVersionUID = 1L;

        GetDatCashMoneyTwice() {
            super(EffectNamesies.GET_DAT_CASH_MONEY_TWICE, -1, -1, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getTrainer(victim).hasEffect(this.namesies));
        }
    }
}
