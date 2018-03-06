package battle.effect.generic.pokemon;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.attack.MoveType;
import battle.effect.CastSource;
import battle.effect.MessageGetter;
import battle.effect.attack.AbilityChanger;
import battle.effect.attack.ChangeAttackTypeSource;
import battle.effect.attack.ChangeTypeSource;
import battle.effect.generic.Effect;
import battle.effect.generic.EffectInterfaces.AbsorbDamageEffect;
import battle.effect.generic.EffectInterfaces.AlwaysCritEffect;
import battle.effect.generic.EffectInterfaces.AttackSelectionEffect;
import battle.effect.generic.EffectInterfaces.AttackSelectionSelfBlockerEffect;
import battle.effect.generic.EffectInterfaces.BeforeTurnEffect;
import battle.effect.generic.EffectInterfaces.BracingEffect;
import battle.effect.generic.EffectInterfaces.ChangeAttackTypeEffect;
import battle.effect.generic.EffectInterfaces.ChangeMoveListEffect;
import battle.effect.generic.EffectInterfaces.ChangeTypeEffect;
import battle.effect.generic.EffectInterfaces.CritStageEffect;
import battle.effect.generic.EffectInterfaces.DamageTakenEffect;
import battle.effect.generic.EffectInterfaces.DefendingNoAdvantageChanger;
import battle.effect.generic.EffectInterfaces.DefogRelease;
import battle.effect.generic.EffectInterfaces.DifferentStatEffect;
import battle.effect.generic.EffectInterfaces.EffectBlockerEffect;
import battle.effect.generic.EffectInterfaces.EndTurnEffect;
import battle.effect.generic.EffectInterfaces.ForceMoveEffect;
import battle.effect.generic.EffectInterfaces.GroundedEffect;
import battle.effect.generic.EffectInterfaces.HalfWeightEffect;
import battle.effect.generic.EffectInterfaces.ItemBlockerEffect;
import battle.effect.generic.EffectInterfaces.LevitationEffect;
import battle.effect.generic.EffectInterfaces.OpponentAccuracyBypassEffect;
import battle.effect.generic.EffectInterfaces.OpponentTrappingEffect;
import battle.effect.generic.EffectInterfaces.PassableEffect;
import battle.effect.generic.EffectInterfaces.PhysicalContactEffect;
import battle.effect.generic.EffectInterfaces.PowerChangeEffect;
import battle.effect.generic.EffectInterfaces.ProtectingEffect;
import battle.effect.generic.EffectInterfaces.RapidSpinRelease;
import battle.effect.generic.EffectInterfaces.SapHealthEffect;
import battle.effect.generic.EffectInterfaces.SelfAttackBlocker;
import battle.effect.generic.EffectInterfaces.SemiInvulnerableBypasser;
import battle.effect.generic.EffectInterfaces.StageChangingEffect;
import battle.effect.generic.EffectInterfaces.StatChangingEffect;
import battle.effect.generic.EffectInterfaces.StatProtectingEffect;
import battle.effect.generic.EffectInterfaces.StatSwitchingEffect;
import battle.effect.generic.EffectInterfaces.StatusPreventionEffect;
import battle.effect.generic.EffectInterfaces.StatusReceivedEffect;
import battle.effect.generic.EffectInterfaces.TakeDamageEffect;
import battle.effect.generic.EffectInterfaces.TargetSwapperEffect;
import battle.effect.generic.EffectInterfaces.TrappingEffect;
import battle.effect.generic.battle.StandardBattleEffectNamesies;
import battle.effect.holder.AbilityHolder;
import battle.effect.holder.ItemHolder;
import battle.effect.status.Status;
import battle.effect.status.StatusCondition;
import item.Item;
import item.ItemNamesies;
import main.Global;
import message.MessageUpdate;
import message.Messages;
import pokemon.Gender;
import pokemon.Stat;
import pokemon.ability.Ability;
import pokemon.ability.AbilityNamesies;
import type.PokeType;
import type.Type;
import util.RandomUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Class to handle effects that are on a single Pokemon
public abstract class PokemonEffect extends Effect<PokemonEffectNamesies> implements Serializable {
    private static final long serialVersionUID = 1L;

    public PokemonEffect(PokemonEffectNamesies name, int minTurns, int maxTurns, boolean nextTurnSubside, boolean hasAlternateCast) {
        super(name, minTurns, maxTurns, nextTurnSubside, hasAlternateCast);
    }

    @Override
    protected void addEffect(Battle b, ActivePokemon victim) {
        victim.getEffects().add(this);
    }

    @Override
    protected boolean hasEffect(Battle b, ActivePokemon victim) {
        return victim.hasEffect(this.namesies());
    }

    // EVERYTHING BELOW IS GENERATED ###

    /**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

    static class LeechSeed extends PokemonEffect implements EndTurnEffect, PassableEffect, SapHealthEffect, RapidSpinRelease {
        private static final long serialVersionUID = 1L;

        LeechSeed() {
            super(PokemonEffectNamesies.LEECH_SEED, -1, -1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.isType(b, Type.GRASS) || victim.hasEffect(this.namesies()));
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
                return;
            }

            // Message needs to be added first instead of in sapHealth so that it is before the victim's reduce health message
            Messages.add(this.getSapMessage(victim));
            this.sapHealth(b, b.getOtherPokemon(victim), victim, victim.reduceHealthFraction(b, 1/8.0), false);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " was seeded!";
        }

        @Override
        public String getRapidSpinReleaseMessage(ActivePokemon released) {
            return released.getName() + " was released from leech seed!";
        }

        @Override
        public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (victim.isType(b, Type.GRASS)) {
                return "It doesn't affect " + victim.getName() + "!";
            } else if (victim.hasEffect(this.namesies())) {
                return victim.getName() + " is already seeded!";
            }

            return super.getFailMessage(b, user, victim);
        }

        @Override
        public void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
            // Need to override this to not sap health when applying damage
        }
    }

    static class Flinch extends PokemonEffect implements BeforeTurnEffect {
        private static final long serialVersionUID = 1L;

        Flinch() {
            super(PokemonEffectNamesies.FLINCH, 1, 1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !((victim.hasAbility(AbilityNamesies.INNER_FOCUS) && !caster.breaksTheMold()) || !b.isFirstAttack() || victim.hasEffect(this.namesies()));
        }

        @Override
        public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
            return false;
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " flinched!";
        }
    }

    static class FireSpin extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
        private static final long serialVersionUID = 1L;

        FireSpin() {
            super(PokemonEffectNamesies.FIRE_SPIN, 4, 5, true, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public void afterCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            if (caster.isHoldingItem(b, ItemNamesies.GRIP_CLAW)) {
                this.setTurns(5);
            }
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " was trapped in the fiery vortex!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return victim.getName() + " is no longer trapped by fire spin.";
        }

        @Override
        public String getRapidSpinReleaseMessage(ActivePokemon released) {
            return released.getName() + " was released from fire spin!";
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
                return;
            }

            Messages.add(victim.getName() + " is hurt by fire spin!");

            // Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
            double fraction = b.getOtherPokemon(victim).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0;
            victim.reduceHealthFraction(b, fraction);
        }

        @Override
        public String trappingMessage(ActivePokemon trapped) {
            return trapped.getName() + " cannot be recalled due to fire spin!";
        }
    }

    static class Infestation extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
        private static final long serialVersionUID = 1L;

        Infestation() {
            super(PokemonEffectNamesies.INFESTATION, 4, 5, true, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public void afterCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            if (caster.isHoldingItem(b, ItemNamesies.GRIP_CLAW)) {
                this.setTurns(5);
            }
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " has been afflicted with an infestation!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return victim.getName() + " is no longer trapped by infestation.";
        }

        @Override
        public String getRapidSpinReleaseMessage(ActivePokemon released) {
            return released.getName() + " was released from infestation!";
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
                return;
            }

            Messages.add(victim.getName() + " is hurt by infestation!");

            // Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
            double fraction = b.getOtherPokemon(victim).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0;
            victim.reduceHealthFraction(b, fraction);
        }

        @Override
        public String trappingMessage(ActivePokemon trapped) {
            return trapped.getName() + " cannot be recalled due to infestation!";
        }
    }

    static class MagmaStorm extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
        private static final long serialVersionUID = 1L;

        MagmaStorm() {
            super(PokemonEffectNamesies.MAGMA_STORM, 4, 5, true, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public void afterCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            if (caster.isHoldingItem(b, ItemNamesies.GRIP_CLAW)) {
                this.setTurns(5);
            }
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " was trapped by swirling magma!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return victim.getName() + " is no longer trapped by magma storm.";
        }

        @Override
        public String getRapidSpinReleaseMessage(ActivePokemon released) {
            return released.getName() + " was released from magma storm!";
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
                return;
            }

            Messages.add(victim.getName() + " is hurt by magma storm!");

            // Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
            double fraction = b.getOtherPokemon(victim).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0;
            victim.reduceHealthFraction(b, fraction);
        }

        @Override
        public String trappingMessage(ActivePokemon trapped) {
            return trapped.getName() + " cannot be recalled due to magma storm!";
        }
    }

    static class Clamped extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
        private static final long serialVersionUID = 1L;

        Clamped() {
            super(PokemonEffectNamesies.CLAMPED, 4, 5, true, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public void afterCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            if (caster.isHoldingItem(b, ItemNamesies.GRIP_CLAW)) {
                this.setTurns(5);
            }
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " clamped " + victim.getName() + "!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return victim.getName() + " is no longer trapped by clamp.";
        }

        @Override
        public String getRapidSpinReleaseMessage(ActivePokemon released) {
            return released.getName() + " was released from clamp!";
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
                return;
            }

            Messages.add(victim.getName() + " is hurt by clamp!");

            // Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
            double fraction = b.getOtherPokemon(victim).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0;
            victim.reduceHealthFraction(b, fraction);
        }

        @Override
        public String trappingMessage(ActivePokemon trapped) {
            return trapped.getName() + " cannot be recalled due to clamp!";
        }
    }

    static class Whirlpooled extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
        private static final long serialVersionUID = 1L;

        Whirlpooled() {
            super(PokemonEffectNamesies.WHIRLPOOLED, 4, 5, true, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public void afterCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            if (caster.isHoldingItem(b, ItemNamesies.GRIP_CLAW)) {
                this.setTurns(5);
            }
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " was trapped in the vortex!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return victim.getName() + " is no longer trapped by whirlpool.";
        }

        @Override
        public String getRapidSpinReleaseMessage(ActivePokemon released) {
            return released.getName() + " was released from whirlpool!";
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
                return;
            }

            Messages.add(victim.getName() + " is hurt by whirlpool!");

            // Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
            double fraction = b.getOtherPokemon(victim).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0;
            victim.reduceHealthFraction(b, fraction);
        }

        @Override
        public String trappingMessage(ActivePokemon trapped) {
            return trapped.getName() + " cannot be recalled due to whirlpool!";
        }
    }

    static class Wrapped extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
        private static final long serialVersionUID = 1L;

        Wrapped() {
            super(PokemonEffectNamesies.WRAPPED, 4, 5, true, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public void afterCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            if (caster.isHoldingItem(b, ItemNamesies.GRIP_CLAW)) {
                this.setTurns(5);
            }
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " was wrapped by " + user.getName() + "!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return victim.getName() + " is no longer trapped by wrap.";
        }

        @Override
        public String getRapidSpinReleaseMessage(ActivePokemon released) {
            return released.getName() + " was released from wrap!";
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
                return;
            }

            Messages.add(victim.getName() + " is hurt by wrap!");

            // Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
            double fraction = b.getOtherPokemon(victim).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0;
            victim.reduceHealthFraction(b, fraction);
        }

        @Override
        public String trappingMessage(ActivePokemon trapped) {
            return trapped.getName() + " cannot be recalled due to wrap!";
        }
    }

    static class Binded extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
        private static final long serialVersionUID = 1L;

        Binded() {
            super(PokemonEffectNamesies.BINDED, 4, 5, true, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public void afterCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            if (caster.isHoldingItem(b, ItemNamesies.GRIP_CLAW)) {
                this.setTurns(5);
            }
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " was binded by " + user.getName() + "!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return victim.getName() + " is no longer trapped by bind.";
        }

        @Override
        public String getRapidSpinReleaseMessage(ActivePokemon released) {
            return released.getName() + " was released from bind!";
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
                return;
            }

            Messages.add(victim.getName() + " is hurt by bind!");

            // Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
            double fraction = b.getOtherPokemon(victim).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0;
            victim.reduceHealthFraction(b, fraction);
        }

        @Override
        public String trappingMessage(ActivePokemon trapped) {
            return trapped.getName() + " cannot be recalled due to bind!";
        }
    }

    static class SandTomb extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
        private static final long serialVersionUID = 1L;

        SandTomb() {
            super(PokemonEffectNamesies.SAND_TOMB, 4, 5, true, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public void afterCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            if (caster.isHoldingItem(b, ItemNamesies.GRIP_CLAW)) {
                this.setTurns(5);
            }
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " was trapped by sand tomb!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return victim.getName() + " is no longer trapped by sand tomb.";
        }

        @Override
        public String getRapidSpinReleaseMessage(ActivePokemon released) {
            return released.getName() + " was released from sand tomb!";
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
                return;
            }

            Messages.add(victim.getName() + " is hurt by sand tomb!");

            // Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
            double fraction = b.getOtherPokemon(victim).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0;
            victim.reduceHealthFraction(b, fraction);
        }

        @Override
        public String trappingMessage(ActivePokemon trapped) {
            return trapped.getName() + " cannot be recalled due to sand tomb!";
        }
    }

    static class KingsShield extends PokemonEffect implements ProtectingEffect {
        private static final long serialVersionUID = 1L;

        KingsShield() {
            super(PokemonEffectNamesies.KINGS_SHIELD, 1, 1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(!RandomUtils.chanceTest((int)(100*caster.getSuccessionDecayRate())) || victim.hasEffect(this.namesies()));
        }

        @Override
        public void protectingEffects(Battle b, ActivePokemon p, ActivePokemon opp) {
            // Pokemon that make contact with the king's shield have their attack reduced
            if (p.getAttack().isMoveType(MoveType.PHYSICAL_CONTACT)) {
                p.getStages().modifyStage(
                        opp, -2, Stat.ATTACK, b, CastSource.EFFECT,
                        (victimName, statName, changed) -> "The King's Shield " + changed + " " + p.getName() + "'s " + statName + "!"
                );
            }
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " protected itself!";
        }
    }

    static class SpikyShield extends PokemonEffect implements ProtectingEffect {
        private static final long serialVersionUID = 1L;

        SpikyShield() {
            super(PokemonEffectNamesies.SPIKY_SHIELD, 1, 1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(!RandomUtils.chanceTest((int)(100*caster.getSuccessionDecayRate())) || victim.hasEffect(this.namesies()));
        }

        @Override
        public void protectingEffects(Battle b, ActivePokemon p, ActivePokemon opp) {
            // Pokemon that make contact with the spiky shield have their health reduced
            if (p.getAttack().isMoveType(MoveType.PHYSICAL_CONTACT)) {
                Messages.add(p.getName() + " was hurt by " + opp.getName() + "'s Spiky Shield!");
                p.reduceHealthFraction(b, 1/8.0);
            }
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " protected itself!";
        }
    }

    static class BanefulBunker extends PokemonEffect implements ProtectingEffect {
        private static final long serialVersionUID = 1L;

        BanefulBunker() {
            super(PokemonEffectNamesies.BANEFUL_BUNKER, 1, 1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(!RandomUtils.chanceTest((int)(100*caster.getSuccessionDecayRate())) || victim.hasEffect(this.namesies()));
        }

        @Override
        public void protectingEffects(Battle b, ActivePokemon p, ActivePokemon opp) {
            // Pokemon that make contact with the baneful bunker are become poisoned
            if (p.getAttack().isMoveType(MoveType.PHYSICAL_CONTACT) && Status.applies(StatusCondition.POISONED, b, opp, p)) {
                Status.applyStatus(b, opp, p, StatusCondition.POISONED, p.getName() + " was poisoned by " + opp.getName() + "'s Baneful Bunker!");
            }
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " protected itself!";
        }
    }

    static class Protecting extends PokemonEffect implements ProtectingEffect {
        private static final long serialVersionUID = 1L;

        Protecting() {
            super(PokemonEffectNamesies.PROTECTING, 1, 1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(!RandomUtils.chanceTest((int)(100*caster.getSuccessionDecayRate())) || victim.hasEffect(this.namesies()));
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " protected itself!";
        }
    }

    static class QuickGuard extends PokemonEffect implements ProtectingEffect {
        private static final long serialVersionUID = 1L;

        QuickGuard() {
            super(PokemonEffectNamesies.QUICK_GUARD, 1, 1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(!RandomUtils.chanceTest((int)(100*caster.getSuccessionDecayRate())) || victim.hasEffect(this.namesies()));
        }

        @Override
        public boolean protectingCondition(Battle b, ActivePokemon attacking) {
            return b.getAttackPriority(attacking) > 0;
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " protected itself!";
        }
    }

    static class CraftyShield extends PokemonEffect implements ProtectingEffect {
        private static final long serialVersionUID = 1L;

        CraftyShield() {
            super(PokemonEffectNamesies.CRAFTY_SHIELD, 1, 1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(!RandomUtils.chanceTest((int)(100*caster.getSuccessionDecayRate())) || victim.hasEffect(this.namesies()));
        }

        @Override
        public boolean protectingCondition(Battle b, ActivePokemon attacking) {
            return attacking.getAttack().isStatusMove();
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " protected itself!";
        }
    }

    // No successive decay for this move
    static class MatBlock extends PokemonEffect implements ProtectingEffect {
        private static final long serialVersionUID = 1L;

        MatBlock() {
            super(PokemonEffectNamesies.MAT_BLOCK, 1, 1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public boolean protectingCondition(Battle b, ActivePokemon attacking) {
            return !attacking.getAttack().isStatusMove();
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " protected itself!";
        }
    }

    static class Bracing extends PokemonEffect implements BracingEffect {
        private static final long serialVersionUID = 1L;

        Bracing() {
            super(PokemonEffectNamesies.BRACING, 1, 1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(!RandomUtils.chanceTest((int)(100*caster.getSuccessionDecayRate())) || victim.hasEffect(this.namesies()));
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " braced itself!";
        }

        @Override
        public boolean isBracing(Battle b, ActivePokemon bracer, boolean fullHealth) {
            return true;
        }

        @Override
        public String braceMessage(ActivePokemon bracer) {
            return bracer.getName() + " endured the hit!";
        }
    }

    static class Confusion extends PokemonEffect implements PassableEffect, BeforeTurnEffect {
        private static final long serialVersionUID = 1L;

        private int turns;

        Confusion() {
            super(PokemonEffectNamesies.CONFUSION, -1, -1, false, false);
            this.turns = RandomUtils.getRandomInt(1, 4); // Between 1 and 4 turns
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !((victim.hasAbility(AbilityNamesies.OWN_TEMPO) && !caster.breaksTheMold()) || victim.hasEffect(this.namesies()));
        }

        @Override
        public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
            // Snap it out!
            if (turns == 0) {
                Messages.add(p.getName() + " snapped out of its confusion!");
                super.active = false;
                return true;
            }

            turns--;
            Messages.add(p.getName() + " is confused!");

            // 50% chance to hurt yourself in confusion while confused
            if (RandomUtils.chanceTest(50)) {

                // Perform confusion damage
                p.callTempMove(AttackNamesies.CONFUSION_DAMAGE, () -> {
                    Messages.add("It hurt itself in confusion!");
                    p.reduceHealth(b, b.calculateDamage(p, p));
                });

                return false;
            }

            return true;
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " became confused!";
        }

        @Override
        public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (victim.hasEffect(this.namesies())) {
                return victim.getName() + " is already confused!";
            } else if (victim.hasAbility(AbilityNamesies.OWN_TEMPO)) {
                return victim.getName() + "'s " + victim.getAbility().getName() + " prevents confusion!";
            }

            return super.getFailMessage(b, user, victim);
        }
    }

    static class SelfConfusion extends PokemonEffect implements ForceMoveEffect {
        private static final long serialVersionUID = 1L;

        private Move move;

        SelfConfusion() {
            super(PokemonEffectNamesies.SELF_CONFUSION, 2, 3, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            move = caster.getMove();
        }

        @Override
        public Move getForcedMove(ActivePokemon attacking) {
            return move;
        }

        @Override
        public void subside(Battle b, ActivePokemon p) {
            if (PokemonEffectNamesies.CONFUSION.getEffect().apply(b, p, p, CastSource.EFFECT, false)) {
                Messages.add(p.getName() + " became confused due to fatigue!");
            }
        }
    }

    static class Safeguard extends PokemonEffect implements StatusPreventionEffect, DefogRelease {
        private static final long serialVersionUID = 1L;

        Safeguard() {
            super(PokemonEffectNamesies.SAFEGUARD, 5, 5, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
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
        public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
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

    static class GuardSpecial extends PokemonEffect implements StatusPreventionEffect {
        private static final long serialVersionUID = 1L;

        GuardSpecial() {
            super(PokemonEffectNamesies.GUARD_SPECIAL, 5, 5, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
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
        public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
            return !caster.hasAbility(AbilityNamesies.INFILTRATOR);
        }

        @Override
        public String statusPreventionMessage(ActivePokemon victim) {
            return "Guard Special protects " + victim.getName() + " from status conditions!";
        }
    }

    static class Encore extends PokemonEffect implements ForceMoveEffect, EndTurnEffect, AttackSelectionSelfBlockerEffect {
        private static final long serialVersionUID = 1L;

        private Move move;

        Encore() {
            super(PokemonEffectNamesies.ENCORE, 3, 3, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !((victim.hasAbility(AbilityNamesies.AROMA_VEIL) && !caster.breaksTheMold()) || victim.getLastMoveUsed() == null || victim.getLastMoveUsed().getPP() == 0 || victim.getLastMoveUsed().getAttack().isMoveType(MoveType.ENCORELESS) || victim.hasEffect(this.namesies()));
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            // If the move runs out of PP, Encore immediately ends
            if (move.getPP() == 0) {
                active = false;
            }
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            move = victim.getLastMoveUsed();
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " got an encore!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The effects of " + victim.getName() + "'s encore faded.";
        }

        @Override
        public Move getForcedMove(ActivePokemon attacking) {
            return move;
        }

        @Override
        public boolean usable(Battle b, ActivePokemon p, Move m) {
            return move.getAttack().namesies() == m.getAttack().namesies();
        }

        @Override
        public String getUnusableMessage(Battle b, ActivePokemon p) {
            return "Only " + move.getAttack().getName() + " can be used right now!";
        }

        @Override
        public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (victim.hasAbility(AbilityNamesies.AROMA_VEIL)) {
                return victim.getName() + "'s " + victim.getAbility().getName() + " prevents it from being encored!";
            }

            return super.getFailMessage(b, user, victim);
        }
    }

    static class Disable extends PokemonEffect implements AttackSelectionSelfBlockerEffect {
        private static final long serialVersionUID = 1L;

        private Move disabled;

        Disable() {
            super(PokemonEffectNamesies.DISABLE, 4, 4, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !((victim.hasAbility(AbilityNamesies.AROMA_VEIL) && !caster.breaksTheMold()) || victim.getLastMoveUsed() == null || victim.getLastMoveUsed().getPP() == 0 || victim.hasEffect(this.namesies()));
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            disabled = victim.getLastMoveUsed();
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + "'s " + disabled.getAttack().getName() + " was disabled!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return victim.getName() + "'s " + disabled.getAttack().getName() + " is no longer disabled!";
        }

        @Override
        public boolean usable(Battle b, ActivePokemon p, Move m) {
            return disabled.getAttack().namesies() != m.getAttack().namesies();
        }

        @Override
        public String getUnusableMessage(Battle b, ActivePokemon p) {
            return disabled.getAttack().getName() + " is disabled!";
        }

        @Override
        public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (victim.hasEffect(this.namesies())) {
                return victim.getName() + " is already disabled!";
            } else if (victim.hasAbility(AbilityNamesies.AROMA_VEIL)) {
                return victim.getName() + "'s " + victim.getAbility().getName() + " prevents it from being disabled!";
            }

            return super.getFailMessage(b, user, victim);
        }
    }

    static class RaiseCrits extends PokemonEffect implements CritStageEffect, PassableEffect, MessageGetter {
        private static final long serialVersionUID = 1L;

        private boolean focusEnergy;
        private boolean direHit;
        private boolean berrylicious;

        RaiseCrits() {
            super(PokemonEffectNamesies.RAISE_CRITS, -1, -1, false, true);
            this.focusEnergy = false;
            this.direHit = false;
            this.berrylicious = false;
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(source == CastSource.USE_ITEM && victim.hasEffect(this.namesies()) && ((RaiseCrits)victim.getEffect(this.namesies())).direHit);
        }

        @Override
        public void alternateCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
            // Doesn't 'fail' if they already have the effect -- just display the message again
            this.addCastMessage(b, caster, victim, source, printCast);

            RaiseCrits critsies = (RaiseCrits)victim.getEffect(this.namesies());
            critsies.afterCast(b, caster, victim, source);
        }

        @Override
        public void afterCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            switch (source) {
                case ATTACK:
                    this.focusEnergy = true;
                    break;
                case USE_ITEM:
                    this.direHit = true;
                    break;
                case HELD_ITEM:
                    this.berrylicious = true;
                    break;
                default:
                    Global.error("Unknown source for RaiseCrits effect.");
                    break;
            }
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return this.getMessage(b, victim, source);
        }

        @Override
        public int increaseCritStage(int stage, ActivePokemon p) {
            int critStage = 0;

            // TODO: Should probably make an enum or something because this is stupid
            if (focusEnergy) {
                critStage++;
            }

            if (direHit) {
                critStage++;
            }

            if (berrylicious) {
                critStage++;
            }

            if (critStage == 0) {
                Global.error("RaiseCrits effect is not actually raising crits.");
            }

            return critStage + stage;
        }

        @Override
        public String getGenericMessage(ActivePokemon p) {
            return p.getName() + " is getting pumped!";
        }

        @Override
        public String getSourceMessage(ActivePokemon p, String sourceName) {
            return p.getName() + " is getting pumped due to its " + sourceName + "!";
        }
    }

    static class ChangeItem extends PokemonEffect implements ItemHolder {
        private static final long serialVersionUID = 1L;

        private Item item;

        ChangeItem() {
            super(PokemonEffectNamesies.CHANGE_ITEM, -1, -1, false, false);
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            item = ((ItemHolder)source.getSource(b, caster)).getItem();
            victim.getEffects().remove(this.namesies());
        }

        @Override
        public Item getItem() {
            return item;
        }
    }

    static class ChangeAttackType extends PokemonEffect implements ChangeAttackTypeEffect {
        private static final long serialVersionUID = 1L;

        private ChangeAttackTypeSource typeSource;

        ChangeAttackType() {
            super(PokemonEffectNamesies.CHANGE_ATTACK_TYPE, 1, 1, false, false);
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            typeSource = (ChangeAttackTypeSource)source.getSource(b, caster);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return typeSource.getMessage(b, user, victim);
        }

        @Override
        public Type changeAttackType(Attack attack, Type original) {
            return typeSource.getAttackType(original);
        }
    }

    static class ChangeType extends PokemonEffect implements ChangeTypeEffect {
        private static final long serialVersionUID = 1L;

        private PokeType type;
        private ChangeTypeSource typeSource;
        private CastSource castSource;

        private String castMessage(ActivePokemon victim) {
            // TODO: This is ugly
            switch (castSource) {
                case ATTACK:
                    return victim.getName() + " was changed to " + type + " type!!";
                case ABILITY:
                    return victim.getName() + "'s " + ((Ability)typeSource).getName() + " changed it to the " + type + " type!!";
                case HELD_ITEM:
                    return victim.getName() + "'s " + ((Item)typeSource).getName() + " changed it to the " + type + " type!!";
                default:
                    Global.error("Invalid cast source for ChangeType " + castSource);
                    return null;
            }
        }

        ChangeType() {
            super(PokemonEffectNamesies.CHANGE_TYPE, -1, -1, false, false);
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            castSource = source;
            typeSource = (ChangeTypeSource)source.getSource(b, caster);
            type = typeSource.getType(b, caster, victim);

            // Remove any other ChangeType effects that the victim may have
            victim.getEffects().remove(this.namesies());
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return castMessage(victim);
        }

        @Override
        public void subside(Battle b, ActivePokemon p) {
            Messages.add(new MessageUpdate().updatePokemon(b, p));
        }

        @Override
        public PokeType getType(Battle b, ActivePokemon p, boolean display) {
            return type;
        }
    }

    static class ChangeAbility extends PokemonEffect implements AbilityHolder {
        private static final long serialVersionUID = 1L;

        private Ability ability;
        private String message;

        ChangeAbility() {
            super(PokemonEffectNamesies.CHANGE_ABILITY, -1, -1, false, false);
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            Ability oldAbility = victim.getAbility();
            oldAbility.deactivate(b, victim);

            AbilityChanger changey = (AbilityChanger)source.getSource(b, caster);
            ability = changey.getAbility(b, caster, victim);
            message = changey.getMessage(b, caster, victim);

            // Remove any other ChangeAbility effects that the victim may have
            victim.getEffects().remove(this.namesies());
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return message;
        }

        @Override
        public Ability getAbility() {
            return ability;
        }
    }

    static class Stockpile extends PokemonEffect implements StageChangingEffect {
        private static final long serialVersionUID = 1L;

        private int turns;

        Stockpile() {
            super(PokemonEffectNamesies.STOCKPILE, -1, -1, false, true);
            this.turns = 0;
        }

        @Override
        public int adjustStage(Battle b, ActivePokemon p, ActivePokemon opp, Stat s) {
            return s == Stat.DEFENSE || s == Stat.SP_DEFENSE ? turns : 0;
        }

        @Override
        public void alternateCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
            Stockpile stockpile = (Stockpile)victim.getEffect(this.namesies());
            stockpile.afterCast(b, caster, victim, source);
        }

        @Override
        public void afterCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            if (this.turns < 3) {
                Messages.add(victim.getName() + " Defense and Special Defense were raised!");
                this.turns++;
                return;
            }

            Messages.add(this.getFailMessage(b, caster, victim));
        }

        @Override
        public void subside(Battle b, ActivePokemon p) {
            Messages.add("The effects of " + p.getName() + "'s Stockpile ended!");
            Messages.add(p.getName() + "'s Defense and Special Defense decreased!");
        }

        @Override
        public int getTurns() {
            return turns;
        }
    }

    static class UsedDefenseCurl extends PokemonEffect {
        private static final long serialVersionUID = 1L;

        UsedDefenseCurl() {
            super(PokemonEffectNamesies.USED_DEFENSE_CURL, -1, -1, false, true);
        }

        @Override
        public void alternateCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
            this.addCastMessage(b, caster, victim, source, printCast);
        }
    }

    static class UsedMinimize extends PokemonEffect {
        private static final long serialVersionUID = 1L;

        UsedMinimize() {
            super(PokemonEffectNamesies.USED_MINIMIZE, -1, -1, false, true);
        }

        @Override
        public void alternateCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
            this.addCastMessage(b, caster, victim, source, printCast);
        }
    }

    static class Mimic extends PokemonEffect implements ChangeMoveListEffect {
        private static final long serialVersionUID = 1L;

        private Move mimicMove;

        Mimic() {
            super(PokemonEffectNamesies.MIMIC, -1, -1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            ActivePokemon other = b.getOtherPokemon(victim);
            final Move lastMoveUsed = other.getLastMoveUsed();
            Attack lastAttack = lastMoveUsed == null ? null : lastMoveUsed.getAttack();

            if (lastAttack == null || victim.hasMove(b, lastAttack.namesies()) || lastAttack.isMoveType(MoveType.MIMICLESS)) {
                Messages.add(this.getFailMessage(b, caster, victim));
                return;
            }

            mimicMove = new Move(lastAttack);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " learned " + mimicMove.getAttack().getName() + "!";
        }

        @Override
        public List<Move> getMoveList(List<Move> actualMoves) {
            List<Move> list = new ArrayList<>();
            for (Move move : actualMoves) {
                if (move.getAttack().namesies() == AttackNamesies.MIMIC) {
                    list.add(mimicMove);
                } else {
                    list.add(move);
                }
            }

            return list;
        }
    }

    static class Imprison extends PokemonEffect implements AttackSelectionSelfBlockerEffect {
        private static final long serialVersionUID = 1L;

        private List<AttackNamesies> unableMoves;

        Imprison() {
            super(PokemonEffectNamesies.IMPRISON, -1, -1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            unableMoves = new ArrayList<>();
            for (Move m : caster.getMoves(b)) {
                unableMoves.add(m.getAttack().namesies());
            }
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " sealed " + victim.getName() + "'s moves!";
        }

        @Override
        public boolean usable(Battle b, ActivePokemon p, Move m) {
            return !unableMoves.contains(m.getAttack().namesies());
        }

        @Override
        public String getUnusableMessage(Battle b, ActivePokemon p) {
            return "No!! You are imprisoned!!!";
        }
    }

    static class Trapped extends PokemonEffect implements TrappingEffect {
        private static final long serialVersionUID = 1L;

        Trapped() {
            super(PokemonEffectNamesies.TRAPPED, -1, -1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public String trappingMessage(ActivePokemon trapped) {
            return trapped.getName() + " cannot be recalled at this time!";
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " can't escape!";
        }
    }

    static class Foresight extends PokemonEffect implements DefendingNoAdvantageChanger {
        private static final long serialVersionUID = 1L;

        Foresight() {
            super(PokemonEffectNamesies.FORESIGHT, -1, -1, false, true);
        }

        @Override
        public void alternateCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
            this.addCastMessage(b, caster, victim, source, printCast);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " identified " + victim.getName() + "!";
        }

        @Override
        public boolean negateNoAdvantage(Type attacking, Type defending) {
            return defending == Type.GHOST && (attacking == Type.NORMAL || attacking == Type.FIGHTING);
        }
    }

    static class MiracleEye extends PokemonEffect implements DefendingNoAdvantageChanger {
        private static final long serialVersionUID = 1L;

        MiracleEye() {
            super(PokemonEffectNamesies.MIRACLE_EYE, -1, -1, false, true);
        }

        @Override
        public void alternateCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
            this.addCastMessage(b, caster, victim, source, printCast);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " identified " + victim.getName() + "!";
        }

        @Override
        public boolean negateNoAdvantage(Type attacking, Type defending) {
            return defending == Type.DARK && (attacking == Type.PSYCHIC);
        }
    }

    static class Torment extends PokemonEffect implements AttackSelectionSelfBlockerEffect {
        private static final long serialVersionUID = 1L;

        Torment() {
            super(PokemonEffectNamesies.TORMENT, -1, -1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !((victim.hasAbility(AbilityNamesies.AROMA_VEIL) && !caster.breaksTheMold()) || victim.hasEffect(this.namesies()));
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " tormented " + victim.getName() + "!";
        }

        @Override
        public boolean usable(Battle b, ActivePokemon p, Move m) {
            Move lastMoveUsed = p.getLastMoveUsed();
            return (lastMoveUsed == null || lastMoveUsed.getAttack().namesies() != m.getAttack().namesies());
        }

        @Override
        public String getUnusableMessage(Battle b, ActivePokemon p) {
            return p.getName() + " cannot use the same move twice in a row!";
        }

        @Override
        public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (victim.hasAbility(AbilityNamesies.AROMA_VEIL)) {
                return victim.getName() + "'s " + victim.getAbility().getName() + " prevents torment!";
            }

            return super.getFailMessage(b, user, victim);
        }
    }

    static class SoundBlock extends PokemonEffect implements AttackSelectionSelfBlockerEffect {
        private static final long serialVersionUID = 1L;

        SoundBlock() {
            super(PokemonEffectNamesies.SOUND_BLOCK, 3, 3, false, false);
        }

        @Override
        public boolean usable(Battle b, ActivePokemon p, Move m) {
            return !m.getAttack().isMoveType(MoveType.SOUND_BASED);
        }

        @Override
        public String getUnusableMessage(Battle b, ActivePokemon p) {
            return p.getName() + " cannot use sound-based moves!!";
        }
    }

    static class Taunt extends PokemonEffect implements AttackSelectionSelfBlockerEffect {
        private static final long serialVersionUID = 1L;

        Taunt() {
            super(PokemonEffectNamesies.TAUNT, 3, 3, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !((victim.hasAbility(AbilityNamesies.AROMA_VEIL) && !caster.breaksTheMold()) || (victim.hasAbility(AbilityNamesies.OBLIVIOUS) && !caster.breaksTheMold()) || victim.hasEffect(this.namesies()));
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " fell for the taunt!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The effects of the taunt wore off.";
        }

        @Override
        public boolean usable(Battle b, ActivePokemon p, Move m) {
            return !m.getAttack().isStatusMove();
        }

        @Override
        public String getUnusableMessage(Battle b, ActivePokemon p) {
            return "No!! Not while you're under the effects of taunt!!";
        }

        @Override
        public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (victim.hasAbility(AbilityNamesies.OBLIVIOUS) || victim.hasAbility(AbilityNamesies.AROMA_VEIL)) {
                return victim.getName() + "'s " + victim.getAbility().getName() + " prevents it from being taunted!";
            }

            return super.getFailMessage(b, user, victim);
        }
    }

    static class LaserFocus extends PokemonEffect implements AlwaysCritEffect {
        private static final long serialVersionUID = 1L;

        LaserFocus() {
            super(PokemonEffectNamesies.LASER_FOCUS, 2, 2, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " began focusing!";
        }
    }

    static class LockOn extends PokemonEffect implements PassableEffect, SemiInvulnerableBypasser {
        private static final long serialVersionUID = 1L;

        LockOn() {
            super(PokemonEffectNamesies.LOCK_ON, 2, 2, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public boolean semiInvulnerableBypass(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // I think this technically is not supposed to hit semi-invulnerable, but I think it should if No Guard can
            return true;
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " took aim!";
        }
    }

    static class Telekinesis extends PokemonEffect implements LevitationEffect, OpponentAccuracyBypassEffect {
        private static final long serialVersionUID = 1L;

        Telekinesis() {
            super(PokemonEffectNamesies.TELEKINESIS, 4, 4, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.isGrounded(b) || victim.hasEffect(this.namesies()));
        }

        @Override
        public boolean opponentBypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // Opponent can always strike you unless they are using a OHKO move or you are semi-invulnerable
            return !attacking.getAttack().isMoveType(MoveType.ONE_HIT_KO) && !defending.isSemiInvulnerable();
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " was levitated due to " + user.getName() + "'s telekinesis!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return victim.getName() + " is no longer under the effects of telekinesis.";
        }

        @Override
        public void fall(Battle b, ActivePokemon fallen) {
            Messages.add("The effects of telekinesis were cancelled!");

            fallen.getEffects().remove(this.namesies());
        }
    }

    static class Ingrain extends PokemonEffect implements TrappingEffect, EndTurnEffect, GroundedEffect, PassableEffect {
        private static final long serialVersionUID = 1L;

        Ingrain() {
            super(PokemonEffectNamesies.INGRAIN, -1, -1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (victim.fullHealth() || victim.hasEffect(PokemonEffectNamesies.HEAL_BLOCK)) {
                return;
            }

            int healAmount = victim.healHealthFraction(1/16.0);
            if (victim.isHoldingItem(b, ItemNamesies.BIG_ROOT)) {
                victim.heal((int)(healAmount*.3));
            }

            Messages.add(new MessageUpdate(victim.getName() + " restored some HP due to ingrain!").updatePokemon(b, victim));
        }

        @Override
        public boolean trapped(Battle b, ActivePokemon escaper) {
            return true;
        }

        @Override
        public String trappingMessage(ActivePokemon trapped) {
            return trapped.getName() + " cannot be recalled due to ingrain!";
        }

        @Override
        public void afterCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            removeLevitation(b, victim);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " planted its roots!";
        }
    }

    static class Grounded extends PokemonEffect implements GroundedEffect {
        private static final long serialVersionUID = 1L;

        Grounded() {
            super(PokemonEffectNamesies.GROUNDED, -1, -1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public void afterCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            removeLevitation(b, victim);
        }
    }

    static class Curse extends PokemonEffect implements EndTurnEffect, PassableEffect {
        private static final long serialVersionUID = 1L;

        Curse() {
            super(PokemonEffectNamesies.CURSE, -1, -1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
                return;
            }

            Messages.add(victim.getName() + " was hurt by the curse!");
            victim.reduceHealthFraction(b, 1/4.0);
        }

        @Override
        public void afterCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            caster.reduceHealthFraction(b, 1/2.0);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " cut its own HP and put a curse on " + victim.getName() + "!";
        }
    }

    static class Yawn extends PokemonEffect {
        private static final long serialVersionUID = 1L;

        Yawn() {
            super(PokemonEffectNamesies.YAWN, 2, 2, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(!Status.applies(StatusCondition.ASLEEP, b, caster, victim) || victim.hasEffect(this.namesies()));
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " grew drowsy!";
        }

        @Override
        public void subside(Battle b, ActivePokemon p) {
            Status.applyStatus(b, b.getOtherPokemon(p), p, StatusCondition.ASLEEP);
        }
    }

    static class MagnetRise extends PokemonEffect implements LevitationEffect, PassableEffect {
        private static final long serialVersionUID = 1L;

        MagnetRise() {
            super(PokemonEffectNamesies.MAGNET_RISE, 5, 5, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.isGrounded(b) || victim.hasEffect(this.namesies()));
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " levitated with electromagnetism!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return victim.getName() + " is no longer under the effects of magnet rise.";
        }

        @Override
        public void fall(Battle b, ActivePokemon fallen) {
            Messages.add("The effects of " + fallen.getName() + "'s magnet rise were cancelled!");

            fallen.getEffects().remove(this.namesies());
        }
    }

    static class Uproar extends PokemonEffect implements ForceMoveEffect, AttackSelectionEffect, EndTurnEffect {
        private static final long serialVersionUID = 1L;

        private Move uproar;

        private static void wakeUp(Battle b, ActivePokemon wakey) {
            if (wakey.hasStatus(StatusCondition.ASLEEP)) {
                wakey.removeStatus();
                Messages.add(new MessageUpdate("The uproar woke up " + wakey.getName() + "!").updatePokemon(b, wakey));
            }
        }

        Uproar() {
            super(PokemonEffectNamesies.UPROAR, 3, 3, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            // If uproar runs out of PP, the effect immediately ends
            if (uproar.getPP() == 0) {
                active = false;
            }
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            uproar = victim.getMove();
        }

        @Override
        public void afterCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            b.addEffect(StandardBattleEffectNamesies.FIELD_UPROAR.getEffect());

            wakeUp(b, victim);
            wakeUp(b, b.getOtherPokemon(victim));
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " started an uproar!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return victim.getName() + "'s uproar ended.";
        }

        @Override
        public Move getForcedMove(ActivePokemon attacking) {
            return uproar;
        }

        @Override
        public boolean usable(Battle b, ActivePokemon p, Move m) {
            return m.getAttack().namesies() == AttackNamesies.UPROAR;
        }

        @Override
        public String getUnusableMessage(Battle b, ActivePokemon p) {
            return "Only Uproar can be used right now!";
        }
    }

    static class AquaRing extends PokemonEffect implements PassableEffect, EndTurnEffect {
        private static final long serialVersionUID = 1L;

        AquaRing() {
            super(PokemonEffectNamesies.AQUA_RING, -1, -1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (victim.fullHealth() || victim.hasEffect(PokemonEffectNamesies.HEAL_BLOCK)) {
                return;
            }

            int healAmount = victim.healHealthFraction(1/16.0);
            if (victim.isHoldingItem(b, ItemNamesies.BIG_ROOT)) {
                victim.heal((int)(healAmount*.3));
            }

            Messages.add(new MessageUpdate(victim.getName() + " restored some HP due to aqua ring!").updatePokemon(b, victim));
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " surrounded itself with a veil of water!";
        }
    }

    static class Nightmare extends PokemonEffect implements EndTurnEffect {
        private static final long serialVersionUID = 1L;

        Nightmare() {
            super(PokemonEffectNamesies.NIGHTMARE, -1, -1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(!victim.hasStatus(StatusCondition.ASLEEP) || victim.hasEffect(this.namesies()));
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (!victim.hasStatus(StatusCondition.ASLEEP)) {
                this.active = false;
                return;
            }

            if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
                return;
            }

            Messages.add(victim.getName() + " was hurt by its nightmare!");
            victim.reduceHealthFraction(b, 1/4.0);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " began having a nightmare!";
        }

        @Override
        public boolean shouldSubside(Battle b, ActivePokemon victim) {
            return !victim.hasStatus(StatusCondition.ASLEEP);
        }
    }

    static class Charge extends PokemonEffect implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Charge() {
            super(PokemonEffectNamesies.CHARGE, 2, 2, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.ELECTRIC) ? 2 : 1;
        }
    }

    static class Focusing extends PokemonEffect implements DamageTakenEffect {
        private static final long serialVersionUID = 1L;

        Focusing() {
            super(PokemonEffectNamesies.FOCUSING, 1, 1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " began tightening its focus!";
        }

        @Override
        public void damageTaken(Battle b, ActivePokemon damageTaker) {
            Messages.add(damageTaker.getName() + " lost its focus and couldn't move!");
            damageTaker.getEffects().remove(this.namesies());
            damageTaker.getEffects().add(PokemonEffectNamesies.FLINCH.getEffect());
        }
    }

    static class ShellTrap extends PokemonEffect implements PhysicalContactEffect {
        private static final long serialVersionUID = 1L;

        ShellTrap() {
            super(PokemonEffectNamesies.SHELL_TRAP, 1, 1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " set up a trap!";
        }

        @Override
        public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
            Messages.add(user.getName() + " set off " + victim.getName() + "'s trap!!");
            victim.getEffects().remove(this.namesies());
        }
    }

    static class BeakBlast extends PokemonEffect implements PhysicalContactEffect {
        private static final long serialVersionUID = 1L;

        BeakBlast() {
            super(PokemonEffectNamesies.BEAK_BLAST, 1, 1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " started heating up its beak!";
        }

        @Override
        public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
            Status.applyStatus(b, victim, user, StatusCondition.BURNED);
        }
    }

    static class FiddyPercentStronger extends PokemonEffect implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        FiddyPercentStronger() {
            super(PokemonEffectNamesies.FIDDY_PERCENT_STRONGER, 1, 1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return 1.5;
        }
    }

    static class Transformed extends PokemonEffect implements ChangeMoveListEffect, DifferentStatEffect, ChangeTypeEffect {
        private static final long serialVersionUID = 1L;

        private Move[] moveList; // TODO: Check if I can change this to a list -- not sure about the activate method in particular
        private int[] stats;
        private PokeType type;

        Transformed() {
            super(PokemonEffectNamesies.TRANSFORMED, -1, -1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getOtherPokemon(victim).hasEffect(this.namesies()) || ((caster.hasAbility(AbilityNamesies.ILLUSION) && caster.getAbility().isActive())) || victim.hasEffect(this.namesies()));
        }

        @Override
        public void afterCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            // Pokemon to transform into
            ActivePokemon transformee = b.getOtherPokemon(victim);

            // Set the new stats
            stats = new int[Stat.NUM_STATS];
            for (int i = 0; i < stats.length; i++) {
                stats[i] = Stat.getStat(i, victim.getLevel(), transformee.getPokemonInfo().getStat(i), victim.getIV(i), victim.getEV(i), victim.getNature().getNatureVal(i));
            }
            stats[Stat.HP.index()] = victim.getMaxHP();

            // Copy the move list
            List<Move> transformeeMoves = transformee.getMoves(b);
            moveList = new Move[transformeeMoves.size()];
            for (int i = 0; i < transformeeMoves.size(); i++) {
                moveList[i] = new Move(transformeeMoves.get(i).getAttack(), 5);
            }

            // Copy all stages
            for (Stat stat : Stat.BATTLE_STATS) {
                victim.getStages().setStage(stat, transformee.getStage(stat));
            }

            // Copy the type
            type = transformee.getPokemonInfo().getType();

            // Castaway
            Messages.add(new MessageUpdate().withNewPokemon(transformee.namesies(), transformee.isShiny(), true, victim.isPlayer()));
            Messages.add(new MessageUpdate().updatePokemon(b, victim));
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " transformed into " + b.getOtherPokemon(victim).namesies().getName() + "!";
        }

        @Override
        public PokeType getType(Battle b, ActivePokemon p, boolean display) {
            return type;
        }

        @Override
        public List<Move> getMoveList(List<Move> actualMoves) {
            return Arrays.asList(moveList);
        }

        @Override
        public Integer getStat(ActivePokemon user, Stat stat) {
            return stats[stat.index()];
        }
    }

    static class Substitute extends PokemonEffect implements AbsorbDamageEffect, PassableEffect, EffectBlockerEffect {
        private static final long serialVersionUID = 1L;

        private int hp;

        Substitute() {
            super(PokemonEffectNamesies.SUBSTITUTE, -1, -1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.getHPRatio() <= .25 || victim.getMaxHP() <= 3 || victim.hasEffect(this.namesies()));
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            hp = victim.reduceHealthFraction(b, .25) + 1;
        }

        @Override
        public void afterCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            String imageName = "substitute" + (victim.isPlayer() ? "-back" : "");
            Messages.add(new MessageUpdate().updatePokemon(b, victim).withImageName(imageName, victim.isPlayer()));
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " put in a substitute!";
        }

        @Override
        public boolean validMove(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Self-target and field moves are always successful
            if (user.getAttack().isSelfTarget() || user.getAttack().isMoveType(MoveType.FIELD)) {
                return true;
            }

            // Substitute-piercing moves, Sound-based moves, and Pokemon with the Infiltrator ability bypass Substitute
            if (user.getAttack().isMoveType(MoveType.SUBSTITUTE_PIERCING) || user.getAttack().isMoveType(MoveType.SOUND_BASED) || user.hasAbility(AbilityNamesies.INFILTRATOR)) {
                return true;
            }

            // Print the failure for status moves
            if (user.getAttack().isStatusMove()) {
                Messages.add(this.getFailMessage(b, user, victim));
            }

            return false;
        }

        @Override
        public boolean absorbDamage(Battle b, ActivePokemon damageTaker, int damageAmount) {
            this.hp -= damageAmount;
            if (this.hp <= 0) {
                Messages.add(new MessageUpdate("The substitute broke!").withNewPokemon(damageTaker.namesies(), damageTaker.isShiny(), true, damageTaker.isPlayer()));
                damageTaker.getEffects().remove(this.namesies());
            } else {
                Messages.add("The substitute absorbed the hit!");
            }

            // Substitute always blocks damage
            return true;
        }
    }

    static class Mist extends PokemonEffect implements StatProtectingEffect, DefogRelease {
        private static final long serialVersionUID = 1L;

        Mist() {
            super(PokemonEffectNamesies.MIST, 5, 5, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
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
        public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
            return !caster.hasAbility(AbilityNamesies.INFILTRATOR);
        }

        @Override
        public String preventionMessage(Battle b, ActivePokemon p, Stat s) {
            return "The mist prevents stat reductions!";
        }

        @Override
        public String getDefogReleaseMessage(ActivePokemon released) {
            return "The mist faded.";
        }
    }

    static class MagicCoat extends PokemonEffect implements TargetSwapperEffect {
        private static final long serialVersionUID = 1L;

        MagicCoat() {
            super(PokemonEffectNamesies.MAGIC_COAT, 1, 1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " shrouded itself with a magic coat!";
        }

        @Override
        public boolean swapTarget(Battle b, ActivePokemon user, ActivePokemon opponent) {
            Attack attack = user.getAttack();
            if (!attack.isSelfTarget() && attack.isStatusMove() && !attack.isMoveType(MoveType.NO_MAGIC_COAT)) {
                Messages.add(opponent.getName() + "'s " + "Magic Coat" + " reflected " + user.getName() + "'s move!");
                return true;
            }

            return false;
        }
    }

    static class Bide extends PokemonEffect implements ForceMoveEffect, EndTurnEffect {
        private static final long serialVersionUID = 1L;

        private Move move;
        private int turns;
        private int damage;

        Bide() {
            super(PokemonEffectNamesies.BIDE, -1, -1, false, true);
            this.turns = 1;
            this.damage = 0;
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            damage += victim.getDamageTaken();
        }

        @Override
        public void alternateCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
            Bide bidesies = (Bide)victim.getEffect(this.namesies());

            // Already has the effect, but not ready for it to end yet -- store dat energy
            if (bidesies.turns > 0) {
                bidesies.turns--;
                this.addCastMessage(b, caster, victim, source, printCast);
                return;
            }

            // TIME'S UP -- RELEASE DAT STORED ENERGY
            Messages.add(victim.getName() + " released energy!");
            if (bidesies.damage == 0) {
                // Sucks to suck
                Messages.add(this.getFailMessage(b, caster, victim));
            } else {
                // RETALIATION STATION
                b.getOtherPokemon(victim).reduceHealth(b, 2*bidesies.damage);
            }

            // Bye Bye Bidesies
            victim.getEffects().remove(this.namesies());
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            move = caster.getMove();
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " is storing energy!";
        }

        @Override
        public Move getForcedMove(ActivePokemon attacking) {
            return move;
        }

        @Override
        public int getTurns() {
            return turns;
        }
    }

    static class HalfWeight extends PokemonEffect implements HalfWeightEffect {
        private static final long serialVersionUID = 1L;

        private int layers;

        HalfWeight() {
            super(PokemonEffectNamesies.HALF_WEIGHT, -1, -1, false, true);
            this.layers = 1;
        }

        @Override
        public void alternateCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
            HalfWeight halfWeight = (HalfWeight)victim.getEffect(this.namesies());
            halfWeight.layers++;
        }

        @Override
        public int getHalfAmount(int halfAmount) {
            return halfAmount + layers;
        }
    }

    static class PowerTrick extends PokemonEffect implements PassableEffect, StatSwitchingEffect {
        private static final long serialVersionUID = 1L;

        PowerTrick() {
            super(PokemonEffectNamesies.POWER_TRICK, -1, -1, false, true);
        }

        @Override
        public Stat getSwitchStat(Battle b, ActivePokemon statPokemon, Stat s) {
            if (s == Stat.ATTACK) {
                return Stat.DEFENSE;
            } else if (s == Stat.DEFENSE) {
                return Stat.ATTACK;
            } else {
                return s;
            }
        }

        @Override
        public void alternateCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
            this.addCastMessage(b, caster, victim, source, printCast);
            victim.getEffects().remove(this.namesies());
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + "'s attack and defense were swapped!";
        }
    }

    static class PowerSplit extends PokemonEffect implements StatChangingEffect {
        private static final long serialVersionUID = 1L;

        PowerSplit() {
            super(PokemonEffectNamesies.POWER_SPLIT, -1, -1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " split the power!";
        }

        @Override
        public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {

            // If the stat is a splitting stat, return the average between the user and the opponent
            if (s == Stat.ATTACK || s == Stat.SP_ATTACK) {
                return (p.getStat(b, s) + opp.getStat(b, s))/2;
            }

            return stat;
        }
    }

    static class GuardSplit extends PokemonEffect implements StatChangingEffect {
        private static final long serialVersionUID = 1L;

        GuardSplit() {
            super(PokemonEffectNamesies.GUARD_SPLIT, -1, -1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " split the defense!";
        }

        @Override
        public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {

            // If the stat is a splitting stat, return the average between the user and the opponent
            if (s == Stat.DEFENSE || s == Stat.SP_DEFENSE) {
                return (p.getStat(b, s) + opp.getStat(b, s))/2;
            }

            return stat;
        }
    }

    static class HealBlock extends PokemonEffect implements SelfAttackBlocker {
        private static final long serialVersionUID = 1L;

        HealBlock() {
            super(PokemonEffectNamesies.HEAL_BLOCK, 5, 5, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !((victim.hasAbility(AbilityNamesies.AROMA_VEIL) && !caster.breaksTheMold()) || victim.hasEffect(this.namesies()));
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " blocked " + victim.getName() + " from healing!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The effects of heal block wore off.";
        }

        @Override
        public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (victim.hasAbility(AbilityNamesies.AROMA_VEIL)) {
                return victim.getName() + "'s " + victim.getAbility().getName() + " prevents heal block!";
            }

            return super.getFailMessage(b, user, victim);
        }

        @Override
        public boolean block(Battle b, ActivePokemon user) {
            // TODO: Test
            return user.getAttack().isMoveType(MoveType.HEALING);
        }
    }

    static class Infatuated extends PokemonEffect implements BeforeTurnEffect {
        private static final long serialVersionUID = 1L;

        Infatuated() {
            super(PokemonEffectNamesies.INFATUATED, -1, -1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !((victim.hasAbility(AbilityNamesies.OBLIVIOUS) && !caster.breaksTheMold()) || (victim.hasAbility(AbilityNamesies.AROMA_VEIL) && !caster.breaksTheMold()) || !Gender.oppositeGenders(caster, victim) || victim.hasEffect(this.namesies()));
        }

        @Override
        public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
            Messages.add(p.getName() + " is in love with " + opp.getName() + "!");
            if (RandomUtils.chanceTest(50)) {
                return true;
            }

            Messages.add(p.getName() + "'s infatuation kept it from attacking!");
            return false;
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " fell in love!";
        }

        @Override
        public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (Gender.oppositeGenders(user, victim) && (victim.hasAbility(AbilityNamesies.OBLIVIOUS) || victim.hasAbility(AbilityNamesies.AROMA_VEIL))) {
                return victim.getName() + "'s " + victim.getAbility().getName() + " prevents infatuation!";
            }

            return super.getFailMessage(b, user, victim);
        }
    }

    static class Snatch extends PokemonEffect implements TargetSwapperEffect {
        private static final long serialVersionUID = 1L;

        Snatch() {
            super(PokemonEffectNamesies.SNATCH, 1, 1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public boolean swapTarget(Battle b, ActivePokemon user, ActivePokemon opponent) {
            Attack attack = user.getAttack();
            if (attack.isSelfTargetStatusMove() && !attack.isMoveType(MoveType.NON_SNATCHABLE)) {
                Messages.add(opponent.getName() + " snatched " + user.getName() + "'s move!");
                return true;
            }

            return false;
        }
    }

    static class Grudge extends PokemonEffect implements StatusReceivedEffect {
        private static final long serialVersionUID = 1L;

        Grudge() {
            super(PokemonEffectNamesies.GRUDGE, -1, -1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " wants " + b.getOtherPokemon(victim).getName() + " to bear a grudge!";
        }

        private void deathWish(Battle b, ActivePokemon dead, ActivePokemon murderer) {
            Messages.add(murderer.getName() + "'s " + murderer.getAttack().getName() + " lost all its PP due to " + dead.getName() + "'s grudge!");
            murderer.getMove().reducePP(murderer.getMove().getPP());
        }

        @Override
        public void receiveStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition statusType) {
            if (statusType == StatusCondition.FAINTED) {
                ActivePokemon murderer = b.getOtherPokemon(victim);

                // Only grant death wish if murdered through direct damage
                if (murderer.isAttacking()) {
                    // DEATH WISH GRANTED
                    deathWish(b, victim, murderer);
                }
            }
        }
    }

    static class DestinyBond extends PokemonEffect implements BeforeTurnEffect, StatusReceivedEffect {
        private static final long serialVersionUID = 1L;

        DestinyBond() {
            super(PokemonEffectNamesies.DESTINY_BOND, -1, -1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(!RandomUtils.chanceTest((int)(100*caster.getSuccessionDecayRate())) || victim.hasEffect(this.namesies()));
        }

        @Override
        public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
            // TODO: What is happening
            p.getEffects().remove(this);
            return true;
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " is trying to take " + b.getOtherPokemon(victim).getName() + " down with it!";
        }

        private void deathWish(Battle b, ActivePokemon dead, ActivePokemon murderer) {
            Messages.add(dead.getName() + " took " + murderer.getName() + " down with it!");
            murderer.killKillKillMurderMurderMurder(b);
        }

        @Override
        public void receiveStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition statusType) {
            if (statusType == StatusCondition.FAINTED) {
                ActivePokemon murderer = b.getOtherPokemon(victim);

                // Only grant death wish if murdered through direct damage
                if (murderer.isAttacking()) {
                    // DEATH WISH GRANTED
                    deathWish(b, victim, murderer);
                }
            }
        }
    }

    static class PerishSong extends PokemonEffect implements PassableEffect, EndTurnEffect {
        private static final long serialVersionUID = 1L;

        PerishSong() {
            super(PokemonEffectNamesies.PERISH_SONG, 3, 3, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !((victim.hasAbility(AbilityNamesies.SOUNDPROOF) && !caster.breaksTheMold()) || victim.hasEffect(this.namesies()));
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            Messages.add(victim.getName() + "'s Perish Song count fell to " + (super.numTurns - 1) + "!");
            if (super.numTurns == 1) {
                victim.killKillKillMurderMurderMurder(b);
            }
        }

        @Override
        public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (victim.hasAbility(AbilityNamesies.SOUNDPROOF)) {
                return victim.getName() + "'s " + victim.getAbility().getName() + " makes it immune to sound based moves!";
            }

            return super.getFailMessage(b, user, victim);
        }
    }

    static class Embargo extends PokemonEffect implements PassableEffect, ItemBlockerEffect {
        private static final long serialVersionUID = 1L;

        Embargo() {
            super(PokemonEffectNamesies.EMBARGO, 5, 5, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " can't use items!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return victim.getName() + " can use items again!";
        }
    }

    static class ConsumedItem extends PokemonEffect implements ItemHolder {
        private static final long serialVersionUID = 1L;

        private Item consumed;

        ConsumedItem() {
            super(PokemonEffectNamesies.CONSUMED_ITEM, -1, -1, false, false);
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            consumed = victim.getHeldItem(b);
            victim.removeItem();
            victim.getEffects().remove(this.namesies());
        }

        @Override
        public Item getItem() {
            return consumed;
        }
    }

    static class FairyLock extends PokemonEffect implements OpponentTrappingEffect {
        private static final long serialVersionUID = 1L;

        FairyLock() {
            super(PokemonEffectNamesies.FAIRY_LOCK, -1, -1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public boolean trapOpponent(Battle b, ActivePokemon escaper, ActivePokemon trapper) {
            // TODO: This isn't right
            return true;
        }

        @Override
        public String opponentTrappingMessage(ActivePokemon escaper, ActivePokemon trapper) {
            return escaper.getName() + " is trapped by the Fairy Lock!";
        }
    }

    static class Powder extends PokemonEffect implements BeforeTurnEffect {
        private static final long serialVersionUID = 1L;

        Powder() {
            super(PokemonEffectNamesies.POWDER, 1, 1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
            // Fire-type moves makes the user explode
            if (p.isAttackType(Type.FIRE)) {
                Messages.add("The powder exploded!");
                p.reduceHealthFraction(b, 1/4.0);
                return false;
            }

            return true;
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " sprinkled powder on " + victim.getName() + "!";
        }
    }

    // TODO: Why does this need the UsedProof field?
    static class EatenBerry extends PokemonEffect {
        private static final long serialVersionUID = 1L;

        EatenBerry() {
            super(PokemonEffectNamesies.EATEN_BERRY, -1, -1, false, true);
        }

        @Override
        public void alternateCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
            this.addCastMessage(b, caster, victim, source, printCast);
        }
    }

    static class BreaksTheMold extends PokemonEffect {
        private static final long serialVersionUID = 1L;

        BreaksTheMold() {
            super(PokemonEffectNamesies.BREAKS_THE_MOLD, 1, 1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }
    }

    static class Raging extends PokemonEffect implements TakeDamageEffect {
        private static final long serialVersionUID = 1L;

        Raging() {
            super(PokemonEffectNamesies.RAGING, -1, -1, false, false);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(victim.hasEffect(this.namesies()));
        }

        @Override
        public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            Move lastMoveUsed = victim.getLastMoveUsed();
            if (lastMoveUsed == null || lastMoveUsed.getAttack().namesies() != AttackNamesies.RAGE) {
                victim.getEffects().remove(this);
                return;
            }

            // Bulbasaur's Rage increased its Attack!
            victim.getStages().modifyStage(
                    victim, 1, Stat.ATTACK, b, CastSource.EFFECT,
                    (victimName, statName, changed) -> String.format("%s's Rage %s %s %s!", victim.getName(), changed, victimName, statName)
            );
        }
    }
}
