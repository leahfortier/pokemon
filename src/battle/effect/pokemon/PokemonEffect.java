package battle.effect.pokemon;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.attack.MoveType;
import battle.effect.ApplyResult;
import battle.effect.Effect;
import battle.effect.EffectInterfaces.AbilityHolder;
import battle.effect.EffectInterfaces.AttackHolder;
import battle.effect.EffectInterfaces.AttackSelectionSelfBlockerEffect;
import battle.effect.EffectInterfaces.IntegerHolder;
import battle.effect.EffectInterfaces.ItemHolder;
import battle.effect.EffectInterfaces.LockingEffect;
import battle.effect.EffectInterfaces.MessageGetter;
import battle.effect.EffectInterfaces.PartialTrappingEffect;
import battle.effect.EffectInterfaces.PassableEffect;
import battle.effect.EffectInterfaces.PhysicalContactEffect;
import battle.effect.EffectInterfaces.PokemonHolder;
import battle.effect.EffectInterfaces.ProtectingEffect;
import battle.effect.EffectInterfaces.SapHealthEffect;
import battle.effect.EffectInterfaces.TakeDamageEffect;
import battle.effect.EffectNamesies;
import battle.effect.InvokeInterfaces.AbsorbDamageEffect;
import battle.effect.InvokeInterfaces.AlwaysCritEffect;
import battle.effect.InvokeInterfaces.AttackSelectionEffect;
import battle.effect.InvokeInterfaces.BeforeAttackPreventingEffect;
import battle.effect.InvokeInterfaces.BracingEffect;
import battle.effect.InvokeInterfaces.ChangeAttackTypeEffect;
import battle.effect.InvokeInterfaces.ChangeMoveListEffect;
import battle.effect.InvokeInterfaces.ChangeTypeEffect;
import battle.effect.InvokeInterfaces.CritStageEffect;
import battle.effect.InvokeInterfaces.DamageTakenEffect;
import battle.effect.InvokeInterfaces.DefendingNoAdvantageChanger;
import battle.effect.InvokeInterfaces.DifferentStatEffect;
import battle.effect.InvokeInterfaces.EffectPreventionEffect;
import battle.effect.InvokeInterfaces.EndTurnEffect;
import battle.effect.InvokeInterfaces.FaintEffect;
import battle.effect.InvokeInterfaces.ForceMoveEffect;
import battle.effect.InvokeInterfaces.GroundedEffect;
import battle.effect.InvokeInterfaces.HalfWeightEffect;
import battle.effect.InvokeInterfaces.IgnoreStageEffect;
import battle.effect.InvokeInterfaces.ItemBlockerEffect;
import battle.effect.InvokeInterfaces.LevitationEffect;
import battle.effect.InvokeInterfaces.NoSwapEffect;
import battle.effect.InvokeInterfaces.OpponentAccuracyBypassEffect;
import battle.effect.InvokeInterfaces.OpponentPowerChangeEffect;
import battle.effect.InvokeInterfaces.PowderBlocker;
import battle.effect.InvokeInterfaces.PowerChangeEffect;
import battle.effect.InvokeInterfaces.RapidSpinRelease;
import battle.effect.InvokeInterfaces.SelfAttackBlocker;
import battle.effect.InvokeInterfaces.SemiInvulnerableBypasser;
import battle.effect.InvokeInterfaces.StageChangingEffect;
import battle.effect.InvokeInterfaces.StartAttackEffect;
import battle.effect.InvokeInterfaces.StatProtectingEffect;
import battle.effect.InvokeInterfaces.StatSwitchingEffect;
import battle.effect.InvokeInterfaces.StatusPreventionEffect;
import battle.effect.InvokeInterfaces.StickyHoldEffect;
import battle.effect.InvokeInterfaces.TargetSwapperEffect;
import battle.effect.InvokeInterfaces.TrappingEffect;
import battle.effect.InvokeInterfaces.UserSwapperEffect;
import battle.effect.attack.OhkoMove;
import battle.effect.battle.StandardBattleEffectNamesies;
import battle.effect.source.CastSource;
import battle.effect.source.ChangeAbilitySource;
import battle.effect.source.ChangeAttackTypeSource;
import battle.effect.source.ChangeTypeSource;
import battle.effect.status.StatusNamesies;
import battle.stages.ModifyStageMessenger;
import battle.stages.StageModifier;
import item.ItemNamesies;
import item.hold.HoldItem;
import message.MessageUpdate;
import message.Messages;
import pokemon.ability.Ability;
import pokemon.ability.AbilityNamesies;
import pokemon.active.Gender;
import pokemon.active.MoveList;
import pokemon.species.PokemonNamesies;
import pokemon.stat.Stat;
import type.PokeType;
import type.Type;
import util.RandomUtils;
import util.serialization.Serializable;

import java.util.ArrayList;
import java.util.List;

// Class to handle effects that are on a single Pokemon
public abstract class PokemonEffect extends Effect<PokemonEffectNamesies> implements Serializable {
    private static final long serialVersionUID = 1L;

    public PokemonEffect(PokemonEffectNamesies name, int minTurns, int maxTurns, boolean canHave, boolean hasAlternateCast) {
        super(name, minTurns, maxTurns, canHave, hasAlternateCast);
    }

    @Override
    protected void addEffect(Battle b, ActivePokemon victim) {
        victim.getEffects().add(this);
    }

    @Override
    protected boolean hasEffect(Battle b, ActivePokemon victim) {
        return victim.hasEffect(this.namesies());
    }

    @Override
    protected PokemonEffect getEffect(Battle b, ActivePokemon victim) {
        return victim.getEffect(this.namesies());
    }

    // EVERYTHING BELOW IS GENERATED ###

    /**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

    static class LeechSeed extends PokemonEffect implements EndTurnEffect, PassableEffect, SapHealthEffect, RapidSpinRelease {
        private static final long serialVersionUID = 1L;

        LeechSeed() {
            super(PokemonEffectNamesies.LEECH_SEED, -1, -1, false, false);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " was seeded!";
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            // Only print the sap message once
            int sappedAmount = victim.reduceHealthFraction(b, 1/8.0, this.getSapMessage(victim));
            this.sapHealth(b, b.getOtherPokemon(victim), victim, sappedAmount, false);
        }

        @Override
        public ApplyResult applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            if (victim.isType(b, Type.GRASS)) {
                return ApplyResult.failure("It doesn't affect " + victim.getName() + "!");
            } else if (victim.hasEffect(this.namesies())) {
                return ApplyResult.failure(victim.getName() + " is already seeded!");
            }

            return ApplyResult.success();
        }

        @Override
        public String getRapidSpinReleaseMessage(ActivePokemon released) {
            return released.getName() + " was released from leech seed!";
        }
    }

    static class Flinch extends PokemonEffect implements BeforeAttackPreventingEffect {
        private static final long serialVersionUID = 1L;

        Flinch() {
            super(PokemonEffectNamesies.FLINCH, 1, 1, false, false);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " flinched!";
        }

        @Override
        public boolean canAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            return false;
        }

        @Override
        public ApplyResult applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return ApplyResult.newResult(b.isFirstAttack());
        }
    }

    static class FireSpin extends PokemonEffect implements PartialTrappingEffect {
        private static final long serialVersionUID = 1L;

        FireSpin() {
            super(PokemonEffectNamesies.FIRE_SPIN, 4, 5, false, false);
        }

        @Override
        public String getReduceMessage(ActivePokemon victim) {
            return victim.getName() + " is hurt by fire spin!";
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
        public String trappingMessage(ActivePokemon trapped) {
            return trapped.getName() + " cannot be recalled due to fire spin!";
        }
    }

    static class Infestation extends PokemonEffect implements PartialTrappingEffect {
        private static final long serialVersionUID = 1L;

        Infestation() {
            super(PokemonEffectNamesies.INFESTATION, 4, 5, false, false);
        }

        @Override
        public String getReduceMessage(ActivePokemon victim) {
            return victim.getName() + " is hurt by infestation!";
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
        public String trappingMessage(ActivePokemon trapped) {
            return trapped.getName() + " cannot be recalled due to infestation!";
        }
    }

    static class MagmaStorm extends PokemonEffect implements PartialTrappingEffect {
        private static final long serialVersionUID = 1L;

        MagmaStorm() {
            super(PokemonEffectNamesies.MAGMA_STORM, 4, 5, false, false);
        }

        @Override
        public String getReduceMessage(ActivePokemon victim) {
            return victim.getName() + " is hurt by magma storm!";
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
        public String trappingMessage(ActivePokemon trapped) {
            return trapped.getName() + " cannot be recalled due to magma storm!";
        }
    }

    static class Clamped extends PokemonEffect implements PartialTrappingEffect {
        private static final long serialVersionUID = 1L;

        Clamped() {
            super(PokemonEffectNamesies.CLAMPED, 4, 5, false, false);
        }

        @Override
        public String getReduceMessage(ActivePokemon victim) {
            return victim.getName() + " is hurt by clamp!";
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
        public String trappingMessage(ActivePokemon trapped) {
            return trapped.getName() + " cannot be recalled due to clamp!";
        }
    }

    static class Whirlpooled extends PokemonEffect implements PartialTrappingEffect {
        private static final long serialVersionUID = 1L;

        Whirlpooled() {
            super(PokemonEffectNamesies.WHIRLPOOLED, 4, 5, false, false);
        }

        @Override
        public String getReduceMessage(ActivePokemon victim) {
            return victim.getName() + " is hurt by whirlpool!";
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
        public String trappingMessage(ActivePokemon trapped) {
            return trapped.getName() + " cannot be recalled due to whirlpool!";
        }
    }

    static class Wrapped extends PokemonEffect implements PartialTrappingEffect {
        private static final long serialVersionUID = 1L;

        Wrapped() {
            super(PokemonEffectNamesies.WRAPPED, 4, 5, false, false);
        }

        @Override
        public String getReduceMessage(ActivePokemon victim) {
            return victim.getName() + " is hurt by wrap!";
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
        public String trappingMessage(ActivePokemon trapped) {
            return trapped.getName() + " cannot be recalled due to wrap!";
        }
    }

    static class Binded extends PokemonEffect implements PartialTrappingEffect {
        private static final long serialVersionUID = 1L;

        Binded() {
            super(PokemonEffectNamesies.BINDED, 4, 5, false, false);
        }

        @Override
        public String getReduceMessage(ActivePokemon victim) {
            return victim.getName() + " is hurt by bind!";
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
        public String trappingMessage(ActivePokemon trapped) {
            return trapped.getName() + " cannot be recalled due to bind!";
        }
    }

    static class SandTomb extends PokemonEffect implements PartialTrappingEffect {
        private static final long serialVersionUID = 1L;

        SandTomb() {
            super(PokemonEffectNamesies.SAND_TOMB, 4, 5, false, false);
        }

        @Override
        public String getReduceMessage(ActivePokemon victim) {
            return victim.getName() + " is hurt by sand tomb!";
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
        public String trappingMessage(ActivePokemon trapped) {
            return trapped.getName() + " cannot be recalled due to sand tomb!";
        }
    }

    static class SnapTrapped extends PokemonEffect implements PartialTrappingEffect {
        private static final long serialVersionUID = 1L;

        SnapTrapped() {
            super(PokemonEffectNamesies.SNAP_TRAPPED, 4, 5, false, false);
        }

        @Override
        public String getReduceMessage(ActivePokemon victim) {
            return victim.getName() + " is hurt by snap trap!";
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " was trapped by snap trap!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return victim.getName() + " is no longer trapped by snap trap.";
        }

        @Override
        public String getRapidSpinReleaseMessage(ActivePokemon released) {
            return released.getName() + " was released from snap trap!";
        }

        @Override
        public String trappingMessage(ActivePokemon trapped) {
            return trapped.getName() + " cannot be recalled due to snap trap!";
        }
    }

    static class KingsShield extends PokemonEffect implements ModifyStageMessenger, ProtectingEffect {
        private static final long serialVersionUID = 1L;

        KingsShield() {
            super(PokemonEffectNamesies.KINGS_SHIELD, 1, 1, false, false);
        }

        @Override
        public boolean protectingCondition(Battle b, ActivePokemon attacking) {
            // Only protects against attacking moves
            return !attacking.getAttack().isStatusMove();
        }

        @Override
        public void protectingEffects(Battle b, ActivePokemon p, ActivePokemon opp) {
            // Pokemon that make contact with the King's Shield have their Attack reduced
            if (p.isMakingContact()) {
                // The King's Shield sharply lowered Charmander's Attack!
                new StageModifier(-1, Stat.ATTACK).withMessage(this).modify(b, opp, p, CastSource.EFFECT);
            }
        }

        @Override
        public String getMessage(String victimName, String possessiveVictim, String statName, String changed) {
            return "The King's Shield " + changed + " " + victimName + "'s " + statName + "!";
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " protected itself!";
        }

        @Override
        public ApplyResult applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return ApplyResult.newResult(RandomUtils.chanceTest((int)(100*caster.getSuccessionDecayRate())));
        }
    }

    static class Obstruct extends PokemonEffect implements ModifyStageMessenger, ProtectingEffect {
        private static final long serialVersionUID = 1L;

        Obstruct() {
            super(PokemonEffectNamesies.OBSTRUCT, 1, 1, false, false);
        }

        @Override
        public void protectingEffects(Battle b, ActivePokemon p, ActivePokemon opp) {
            // Pokemon that make contact with the obstruction have their Defense reduced
            if (p.isMakingContact()) {
                // The obstruction sharply lowered Charmander's Defense!
                new StageModifier(-2, Stat.DEFENSE).withMessage(this).modify(b, opp, p, CastSource.EFFECT);
            }
        }

        @Override
        public String getMessage(String victimName, String possessiveVictim, String statName, String changed) {
            return "The obstruction " + changed + " " + victimName + "'s " + statName + "!";
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " protected itself!";
        }

        @Override
        public ApplyResult applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return ApplyResult.newResult(RandomUtils.chanceTest((int)(100*caster.getSuccessionDecayRate())));
        }
    }

    static class SpikyShield extends PokemonEffect implements ProtectingEffect {
        private static final long serialVersionUID = 1L;

        SpikyShield() {
            super(PokemonEffectNamesies.SPIKY_SHIELD, 1, 1, false, false);
        }

        @Override
        public void protectingEffects(Battle b, ActivePokemon p, ActivePokemon opp) {
            // Pokemon that make contact with the spiky shield have their health reduced
            if (p.isMakingContact()) {
                p.reduceHealthFraction(b, 1/8.0, p.getName() + " was hurt by " + opp.getName() + "'s Spiky Shield!");
            }
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " protected itself!";
        }

        @Override
        public ApplyResult applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return ApplyResult.newResult(RandomUtils.chanceTest((int)(100*caster.getSuccessionDecayRate())));
        }
    }

    static class BanefulBunker extends PokemonEffect implements ProtectingEffect {
        private static final long serialVersionUID = 1L;

        BanefulBunker() {
            super(PokemonEffectNamesies.BANEFUL_BUNKER, 1, 1, false, false);
        }

        @Override
        public void protectingEffects(Battle b, ActivePokemon p, ActivePokemon opp) {
            // Pokemon that make contact with the baneful bunker become poisoned
            if (p.isMakingContact()) {
                StatusNamesies.POISONED.getStatus().apply(b, opp, p, p.getName() + " was poisoned by " + opp.getName() + "'s Baneful Bunker!");
            }
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " protected itself!";
        }

        @Override
        public ApplyResult applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return ApplyResult.newResult(RandomUtils.chanceTest((int)(100*caster.getSuccessionDecayRate())));
        }
    }

    static class Protect extends PokemonEffect implements ProtectingEffect {
        private static final long serialVersionUID = 1L;

        Protect() {
            super(PokemonEffectNamesies.PROTECT, 1, 1, false, false);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " protected itself!";
        }

        @Override
        public ApplyResult applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return ApplyResult.newResult(RandomUtils.chanceTest((int)(100*caster.getSuccessionDecayRate())));
        }
    }

    static class QuickGuard extends PokemonEffect implements ProtectingEffect {
        private static final long serialVersionUID = 1L;

        QuickGuard() {
            super(PokemonEffectNamesies.QUICK_GUARD, 1, 1, false, false);
        }

        @Override
        public boolean protectingCondition(Battle b, ActivePokemon attacking) {
            return attacking.getAttackPriority() > 0;
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " protected itself!";
        }

        @Override
        public ApplyResult applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return ApplyResult.newResult(RandomUtils.chanceTest((int)(100*caster.getSuccessionDecayRate())));
        }
    }

    static class CraftyShield extends PokemonEffect implements ProtectingEffect {
        private static final long serialVersionUID = 1L;

        CraftyShield() {
            super(PokemonEffectNamesies.CRAFTY_SHIELD, 1, 1, false, false);
        }

        @Override
        public boolean protectingCondition(Battle b, ActivePokemon attacking) {
            return attacking.getAttack().isStatusMove();
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " protected itself!";
        }

        @Override
        public ApplyResult applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return ApplyResult.newResult(RandomUtils.chanceTest((int)(100*caster.getSuccessionDecayRate())));
        }
    }

    // No successive decay for this move
    static class MatBlock extends PokemonEffect implements ProtectingEffect {
        private static final long serialVersionUID = 1L;

        MatBlock() {
            super(PokemonEffectNamesies.MAT_BLOCK, 1, 1, false, false);
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

        @Override
        public ApplyResult applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return ApplyResult.newResult(RandomUtils.chanceTest((int)(100*caster.getSuccessionDecayRate())));
        }
    }

    static class Confusion extends PokemonEffect implements PassableEffect, BeforeAttackPreventingEffect {
        private static final long serialVersionUID = 1L;

        private int turns;

        Confusion() {
            super(PokemonEffectNamesies.CONFUSION, -1, -1, false, false);
            this.turns = RandomUtils.getRandomInt(1, 4); // Between 1 and 4 turns
        }

        @Override
        public String getSourcePreventMessage(ActivePokemon victim, String sourceName) {
            return victim.getName() + "'s " + sourceName + " prevents confusion!";
        }

        @Override
        public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
            return victim.getName() + " is no longer confused due to its " + sourceName + "!";
        }

        @Override
        public boolean canAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // Snap it out!
            if (turns == 0) {
                Messages.add(attacking.getName() + " snapped out of its confusion!");
                this.deactivate();
                return true;
            }

            turns--;
            Messages.add(attacking.getName() + " is confused!");

            // 50% chance to hurt yourself in confusion while confused
            if (RandomUtils.chanceTest(50)) {

                // Perform confusion damage
                attacking.callTempMove(
                        AttackNamesies.CONFUSION_DAMAGE,
                        () -> {
                            int damage = b.calculateDamage(attacking, attacking).getCalculatedDamage();
                            attacking.indirectReduceHealth(b, damage, true, "It hurt itself in confusion!");
                        }
                );

                return false;
            }

            return true;
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " became confused!";
        }

        @Override
        public ApplyResult applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            if (victim.hasEffect(this.namesies())) {
                return ApplyResult.failure(victim.getName() + " is already confused!");
            }

            return ApplyResult.success();
        }
    }

    static class SelfConfusion extends PokemonEffect implements ForceMoveEffect {
        private static final long serialVersionUID = 1L;

        private Move move;

        SelfConfusion() {
            super(PokemonEffectNamesies.SELF_CONFUSION, 2, 3, false, false);
        }

        @Override
        public void subside(Battle b, ActivePokemon p) {
            String message = p.getName() + " became confused due to fatigue!";
            Effect.apply(PokemonEffectNamesies.CONFUSION, b, p, p, CastSource.EFFECT, message);
        }

        @Override
        public Move getForcedMove(ActivePokemon attacking) {
            return move;
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            move = caster.getMove();
        }
    }

    static class Encore extends PokemonEffect implements ForceMoveEffect, AttackSelectionSelfBlockerEffect {
        private static final long serialVersionUID = 1L;

        private Move move;

        Encore() {
            super(PokemonEffectNamesies.ENCORE, 3, 3, false, false);
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
        public String getUnusableMessage(ActivePokemon p) {
            return "Only " + move.getAttack().getName() + " can be used right now!";
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " got an encore!";
        }

        @Override
        public String getSourcePreventMessage(ActivePokemon victim, String sourceName) {
            return victim.getName() + "'s " + sourceName + " prevents it from being encored!";
        }

        @Override
        public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
            return victim.getName() + " is no longer under the effects of encore due to its " + sourceName + "!";
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            move = victim.getLastMoveUsed();
        }

        @Override
        public boolean shouldSubside(Battle b, ActivePokemon victim) {
            // If the move runs out of PP, Encore immediately ends
            return move.getPP() == 0;
        }

        @Override
        public ApplyResult applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            Move lastMove = victim.getLastMoveUsed();
            if (lastMove == null || lastMove.getPP() == 0 || lastMove.getAttack().isMoveType(MoveType.ENCORELESS)) {
                return ApplyResult.failure();
            }

            return ApplyResult.success();
        }
    }

    static class Disable extends PokemonEffect implements AttackSelectionSelfBlockerEffect {
        private static final long serialVersionUID = 1L;

        private Move disabled;

        Disable() {
            super(PokemonEffectNamesies.DISABLE, 4, 4, false, false);
        }

        @Override
        public boolean usable(Battle b, ActivePokemon p, Move m) {
            return disabled.getAttack().namesies() != m.getAttack().namesies();
        }

        @Override
        public String getUnusableMessage(ActivePokemon p) {
            return disabled.getAttack().getName() + " is disabled!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return victim.getName() + "'s " + disabled.getAttack().getName() + " is no longer disabled!";
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + "'s " + disabled.getAttack().getName() + " was disabled!";
        }

        @Override
        public String getSourcePreventMessage(ActivePokemon victim, String sourceName) {
            return victim.getName() + "'s " + sourceName + " prevents it from being disabled!";
        }

        @Override
        public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
            return victim.getName() + " is no longer disabled due to its " + sourceName + "!";
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            disabled = victim.getLastMoveUsed();
        }

        @Override
        public ApplyResult applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            Move lastMove = victim.getLastMoveUsed();
            if (lastMove == null || lastMove.getPP() == 0) {
                return ApplyResult.failure();
            } else if (victim.hasEffect(this.namesies())) {
                return ApplyResult.failure(victim.getName() + " is already disabled!");
            }

            return ApplyResult.success();
        }
    }

    // Used for Focus Energy, Dire Hit, Lansat Berry
    // These effects do not stack with each other
    // Ex: Dire Hit should fail if Focus Energy was already used etc.
    static class RaiseCrits extends PokemonEffect implements CritStageEffect, PassableEffect, MessageGetter {
        private static final long serialVersionUID = 1L;

        RaiseCrits() {
            super(PokemonEffectNamesies.RAISE_CRITS, -1, -1, false, false);
        }

        @Override
        public int increaseCritStage(ActivePokemon p) {
            return 2;
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return this.getMessage(victim, source);
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

        private HoldItem item;

        ChangeItem() {
            super(PokemonEffectNamesies.CHANGE_ITEM, -1, -1, true, false);
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            item = ((ItemHolder)source.getSource(caster)).getItem();
            victim.getEffects().remove(this.namesies());
        }

        @Override
        public HoldItem getItem() {
            return item;
        }
    }

    static class ChangeAttackType extends PokemonEffect implements ChangeAttackTypeEffect {
        private static final long serialVersionUID = 1L;

        private ChangeAttackTypeSource typeSource;

        ChangeAttackType() {
            super(PokemonEffectNamesies.CHANGE_ATTACK_TYPE, 1, 1, true, false);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return typeSource.getMessage(user, victim);
        }

        @Override
        public Type changeAttackType(Attack attack, Type original) {
            return typeSource.getAttackType(original);
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            typeSource = (ChangeAttackTypeSource)source.getSource(caster);
        }
    }

    static class ChangeType extends PokemonEffect implements ChangeTypeEffect, MessageGetter {
        private static final long serialVersionUID = 1L;

        private PokeType type;

        ChangeType() {
            super(PokemonEffectNamesies.CHANGE_TYPE, -1, -1, true, true);
        }

        @Override
        public void alternateCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, CastMessageGetter castMessage) {
            this.beforeCast(b, caster, victim, source);
            this.addCastMessage(b, caster, victim, source, castMessage);
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            ChangeTypeSource typeSource = (ChangeTypeSource)source.getSource(caster);
            type = typeSource.getType(b, caster, victim);
        }

        @Override
        public PokeType getType(Battle b, ActivePokemon p, boolean display) {
            return type;
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return this.getMessage(victim, source);
        }

        @Override
        public String getGenericMessage(ActivePokemon p) {
            return p.getName() + " was changed to " + type + " type!!";
        }

        @Override
        public String getSourceMessage(ActivePokemon p, String sourceName) {
            return p.getName() + "'s " + sourceName + " changed it to the " + type + " type!!";
        }

        @Override
        public void subside(Battle b, ActivePokemon p) {
            Messages.add(new MessageUpdate().updatePokemon(b, p));
        }
    }

    static class ChangeAbility extends PokemonEffect implements AbilityHolder {
        private static final long serialVersionUID = 1L;

        private Ability ability;
        private String message;

        ChangeAbility() {
            super(PokemonEffectNamesies.CHANGE_ABILITY, -1, -1, true, false);
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            Ability oldAbility = victim.getAbility();
            oldAbility.deactivate(b, victim);

            ChangeAbilitySource changey = (ChangeAbilitySource)source.getSource(caster);
            ability = changey.getAbility(b, caster, victim);
            message = changey.getMessage(b, caster, victim);

            // Remove any other ChangeAbility effects that the victim may have
            victim.getEffects().remove(this.namesies());
        }

        @Override
        public Ability getAbility() {
            return ability;
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return message;
        }
    }

    static class Stockpile extends PokemonEffect implements StageChangingEffect {
        private static final long serialVersionUID = 1L;

        private int turns;

        Stockpile() {
            super(PokemonEffectNamesies.STOCKPILE, -1, -1, true, true);
            this.turns = 0;
        }

        @Override
        public int getTurns() {
            return turns;
        }

        @Override
        public void alternateCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, CastMessageGetter castMessage) {
            this.afterCast(b, caster, victim, source);
        }

        @Override
        public void afterCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            if (this.turns < 3) {
                Messages.add(victim.getName() + " Defense and Special Defense were raised!");
                this.turns++;
                return;
            }

            Messages.add(Effect.DEFAULT_FAIL_MESSAGE);
        }

        @Override
        public int adjustStage(Battle b, ActivePokemon p, Stat s) {
            return s == Stat.DEFENSE || s == Stat.SP_DEFENSE ? turns : 0;
        }

        @Override
        public void subside(Battle b, ActivePokemon p) {
            Messages.add("The effects of " + p.getName() + "'s Stockpile ended!");
            Messages.add(p.getName() + "'s Defense and Special Defense decreased!");
        }
    }

    static class UsedDefenseCurl extends PokemonEffect {
        private static final long serialVersionUID = 1L;

        UsedDefenseCurl() {
            super(PokemonEffectNamesies.USED_DEFENSE_CURL, -1, -1, true, true);
        }

        @Override
        public void alternateCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, CastMessageGetter castMessage) {
            this.addCastMessage(b, caster, victim, source, castMessage);
        }
    }

    static class UsedMinimize extends PokemonEffect {
        private static final long serialVersionUID = 1L;

        UsedMinimize() {
            super(PokemonEffectNamesies.USED_MINIMIZE, -1, -1, true, true);
        }

        @Override
        public void alternateCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, CastMessageGetter castMessage) {
            this.addCastMessage(b, caster, victim, source, castMessage);
        }
    }

    static class Mimic extends PokemonEffect implements ChangeMoveListEffect {
        private static final long serialVersionUID = 1L;

        private Move mimicMove;

        Mimic() {
            super(PokemonEffectNamesies.MIMIC, -1, -1, false, false);
        }

        @Override
        public MoveList getMoveList(MoveList actualMoves) {
            List<Move> list = new ArrayList<>();
            for (Move move : actualMoves) {
                if (move.getAttack().namesies() == AttackNamesies.MIMIC) {
                    list.add(mimicMove);
                } else {
                    list.add(move);
                }
            }

            return new MoveList(list);
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            AttackHolder attackHolder = (AttackHolder)source.getSource(caster);
            mimicMove = new Move(attackHolder.getAttack());
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " learned " + mimicMove.getAttack().getName() + "!";
        }
    }

    static class Imprison extends PokemonEffect implements AttackSelectionSelfBlockerEffect {
        private static final long serialVersionUID = 1L;

        private List<AttackNamesies> unableMoves;

        Imprison() {
            super(PokemonEffectNamesies.IMPRISON, -1, -1, false, false);
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
        public String getUnusableMessage(ActivePokemon p) {
            return "No!! You are imprisoned!!!";
        }
    }

    static class Trapped extends PokemonEffect implements TrappingEffect {
        private static final long serialVersionUID = 1L;

        Trapped() {
            super(PokemonEffectNamesies.TRAPPED, -1, -1, false, false);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " can't escape!";
        }

        @Override
        public String trappingMessage(ActivePokemon trapped) {
            return trapped.getName() + " cannot be recalled at this time!";
        }
    }

    // Note: Different that Trapped effect because the attack can only be used once
    static class NoRetreat extends PokemonEffect implements TrappingEffect {
        private static final long serialVersionUID = 1L;

        NoRetreat() {
            super(PokemonEffectNamesies.NO_RETREAT, -1, -1, false, false);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " cannot retreat!";
        }

        @Override
        public String trappingMessage(ActivePokemon trapped) {
            return trapped.getName() + " cannot retreat!";
        }
    }

    static class Octolocked extends PokemonEffect implements EndTurnEffect, LockingEffect, ModifyStageMessenger {
        private static final long serialVersionUID = 1L;

        // Only locked as long as the caster is still in play
        private ActivePokemon caster;

        Octolocked() {
            super(PokemonEffectNamesies.OCTOLOCKED, -1, -1, false, false);
        }

        @Override
        public List<ActivePokemon> getLocking() {
            return List.of(caster);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " can't escape!";
        }

        @Override
        public String trappingMessage(ActivePokemon trapped) {
            return trapped.getName() + " is trapped by Octolock!";
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            // Make sure locking Pokemon is still here to be mean
            if (!checkActive(b)) {
                return;
            }

            // Charmander's Defense was lowered by Octolock!
            StageModifier stageReducer = new StageModifier(-1, Stat.DEFENSE, Stat.SP_DEFENSE).withMessage(this);
            stageReducer.modify(b, caster, victim, CastSource.EFFECT);
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            this.caster = caster;
        }

        @Override
        public String getMessage(String victimName, String possessiveVictim, String statName, String changed) {
            return victimName + "'s " + statName + " was " + changed + " by Octolock!";
        }
    }

    static class Foresight extends PokemonEffect implements DefendingNoAdvantageChanger, IgnoreStageEffect {
        private static final long serialVersionUID = 1L;

        Foresight() {
            super(PokemonEffectNamesies.FORESIGHT, -1, -1, true, true);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " identified " + victim.getName() + "!";
        }

        @Override
        public boolean ignoreStage(Stat s, int stage) {
            // Ignore this Pokemon's increased evasion
            return s == Stat.EVASION && stage > 0;
        }

        @Override
        public void alternateCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, CastMessageGetter castMessage) {
            this.addCastMessage(b, caster, victim, source, castMessage);
        }

        @Override
        public boolean negateNoAdvantage(Type attacking, Type defending) {
            return defending == Type.GHOST && (attacking == Type.NORMAL || attacking == Type.FIGHTING);
        }
    }

    static class MiracleEye extends PokemonEffect implements DefendingNoAdvantageChanger, IgnoreStageEffect {
        private static final long serialVersionUID = 1L;

        MiracleEye() {
            super(PokemonEffectNamesies.MIRACLE_EYE, -1, -1, true, true);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " identified " + victim.getName() + "!";
        }

        @Override
        public boolean ignoreStage(Stat s, int stage) {
            // Ignore this Pokemon's increased evasion
            return s == Stat.EVASION && stage > 0;
        }

        @Override
        public void alternateCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, CastMessageGetter castMessage) {
            this.addCastMessage(b, caster, victim, source, castMessage);
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
        public boolean usable(Battle b, ActivePokemon p, Move m) {
            // If you haven't used a move yet, there's nothing to contradict
            Move lastMoveUsed = p.getLastMoveUsed();
            if (lastMoveUsed == null) {
                return true;
            }

            // Struggle is not affected by torment
            AttackNamesies current = m.getAttack().namesies();
            if (current == AttackNamesies.STRUGGLE) {
                return true;
            }

            // Acceptable if trying to use a different move than last time
            return lastMoveUsed.getAttack().namesies() != current;
        }

        @Override
        public String getUnusableMessage(ActivePokemon p) {
            return p.getName() + " cannot use the same move twice in a row!";
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " tormented " + victim.getName() + "!";
        }

        @Override
        public String getSourcePreventMessage(ActivePokemon victim, String sourceName) {
            return victim.getName() + "'s " + sourceName + " prevents torment!";
        }

        @Override
        public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
            return victim.getName() + " is no longer tormented due to its " + sourceName + "!";
        }
    }

    // STFU MF
    static class Silence extends PokemonEffect implements AttackSelectionSelfBlockerEffect {
        private static final long serialVersionUID = 1L;

        Silence() {
            super(PokemonEffectNamesies.SILENCE, 3, 3, true, false);
        }

        @Override
        public boolean usable(Battle b, ActivePokemon p, Move m) {
            return !m.getAttack().isMoveType(MoveType.SOUND_BASED);
        }

        @Override
        public String getUnusableMessage(ActivePokemon p) {
            return p.getName() + " cannot use sound-based moves!!";
        }
    }

    static class Taunt extends PokemonEffect implements AttackSelectionSelfBlockerEffect {
        private static final long serialVersionUID = 1L;

        Taunt() {
            super(PokemonEffectNamesies.TAUNT, 3, 3, false, false);
        }

        @Override
        public boolean usable(Battle b, ActivePokemon p, Move m) {
            return !m.getAttack().isStatusMove();
        }

        @Override
        public String getUnusableMessage(ActivePokemon p) {
            return "No!! Not while you're under the effects of taunt!!";
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
        public String getSourcePreventMessage(ActivePokemon victim, String sourceName) {
            return victim.getName() + "'s " + sourceName + " prevents it from being taunted!";
        }

        @Override
        public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
            return victim.getName() + " is no longer taunted due to its " + sourceName + "!";
        }
    }

    static class LaserFocus extends PokemonEffect implements AlwaysCritEffect {
        private static final long serialVersionUID = 1L;

        LaserFocus() {
            super(PokemonEffectNamesies.LASER_FOCUS, 2, 2, false, false);
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
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " took aim!";
        }

        @Override
        public boolean semiInvulnerableBypass(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // I think this technically is not supposed to hit semi-invulnerable, but I think it should if No Guard can
            return true;
        }
    }

    static class Telekinesis extends PokemonEffect implements LevitationEffect, OpponentAccuracyBypassEffect {
        private static final long serialVersionUID = 1L;

        Telekinesis() {
            super(PokemonEffectNamesies.TELEKINESIS, 4, 4, false, false);
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
        public boolean opponentBypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // Opponent can always strike you unless they are using a OHKO move or you are semi-invulnerable
            return !(attacking.getAttack() instanceof OhkoMove) && !defending.isSemiInvulnerable();
        }

        @Override
        public void fall(ActivePokemon fallen) {
            Messages.add("The effects of telekinesis were cancelled!");
            this.deactivate();
        }

        @Override
        public ApplyResult applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return ApplyResult.newResult(!victim.isGrounded(b));
        }
    }

    static class Ingrain extends PokemonEffect implements TrappingEffect, GroundedEffect, PassableEffect, NoSwapEffect, EndTurnEffect {
        private static final long serialVersionUID = 1L;

        Ingrain() {
            super(PokemonEffectNamesies.INGRAIN, -1, -1, false, false);
        }

        @Override
        public String trappingMessage(ActivePokemon trapped) {
            return trapped.getName() + " cannot be recalled due to ingrain!";
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " planted its roots!";
        }

        @Override
        public void afterCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            removeLevitation(b, victim);
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            int healAmount = victim.getHealHealthFractionAmount(1/16.0);
            if (victim.isHoldingItem(ItemNamesies.BIG_ROOT)) {
                healAmount *= 1.3;
            }

            victim.heal(healAmount, b, victim.getName() + " restored some HP due to ingrain!");
        }
    }

    static class Grounded extends PokemonEffect implements GroundedEffect {
        private static final long serialVersionUID = 1L;

        Grounded() {
            super(PokemonEffectNamesies.GROUNDED, -1, -1, false, false);
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
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            victim.reduceHealthFraction(b, 1/4.0, victim.getName() + " was hurt by the curse!");
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " cut its own HP and put a curse on " + victim.getName() + "!";
        }

        @Override
        public void afterCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            caster.forceReduceHealthFraction(b, 1/2.0, "");
        }
    }

    static class Yawn extends PokemonEffect {
        private static final long serialVersionUID = 1L;

        Yawn() {
            super(PokemonEffectNamesies.YAWN, 2, 2, false, false);
        }

        @Override
        public ApplyResult applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return StatusNamesies.ASLEEP.getStatus().applies(b, caster, victim);
        }

        @Override
        public void subside(Battle b, ActivePokemon p) {
            StatusNamesies.ASLEEP.getStatus().apply(b, p, p, CastSource.EFFECT);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " grew drowsy!";
        }
    }

    static class MagnetRise extends PokemonEffect implements LevitationEffect, PassableEffect {
        private static final long serialVersionUID = 1L;

        MagnetRise() {
            super(PokemonEffectNamesies.MAGNET_RISE, 5, 5, false, false);
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
        public void fall(ActivePokemon fallen) {
            Messages.add("The effects of " + fallen.getName() + "'s magnet rise were cancelled!");
            this.deactivate();
        }

        @Override
        public ApplyResult applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return ApplyResult.newResult(!victim.isGrounded(b));
        }
    }

    static class Uproar extends PokemonEffect implements ForceMoveEffect, AttackSelectionEffect {
        private static final long serialVersionUID = 1L;

        private Move uproar;

        private static void wakeUp(Battle b, ActivePokemon wakey) {
            if (wakey.hasStatus(StatusNamesies.ASLEEP)) {
                wakey.removeStatus();
                Messages.add(new MessageUpdate("The uproar woke up " + wakey.getName() + "!").updatePokemon(b, wakey));
            }
        }

        Uproar() {
            super(PokemonEffectNamesies.UPROAR, 3, 3, false, false);
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return victim.getName() + "'s uproar ended.";
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
        public Move getForcedMove(ActivePokemon attacking) {
            return uproar;
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " started an uproar!";
        }

        @Override
        public boolean usable(Battle b, ActivePokemon p, Move m) {
            return m.getAttack().namesies() == AttackNamesies.UPROAR;
        }

        @Override
        public String getUnusableMessage(ActivePokemon p) {
            return "Only Uproar can be used right now!";
        }

        @Override
        public boolean shouldSubside(Battle b, ActivePokemon victim) {
            // If uproar runs out of PP, the effect immediately ends
            return uproar.getPP() == 0;
        }
    }

    static class AquaRing extends PokemonEffect implements PassableEffect, EndTurnEffect {
        private static final long serialVersionUID = 1L;

        AquaRing() {
            super(PokemonEffectNamesies.AQUA_RING, -1, -1, false, false);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " surrounded itself with a veil of water!";
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            int healAmount = victim.getHealHealthFractionAmount(1/16.0);
            if (victim.isHoldingItem(ItemNamesies.BIG_ROOT)) {
                healAmount *= 1.3;
            }

            victim.heal(healAmount, b, victim.getName() + " restored some HP due to aqua ring!");
        }
    }

    static class Nightmare extends PokemonEffect implements EndTurnEffect {
        private static final long serialVersionUID = 1L;

        Nightmare() {
            super(PokemonEffectNamesies.NIGHTMARE, -1, -1, false, false);
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            // Only active when asleep
            if (this.shouldSubside(b, victim)) {
                this.deactivate();
                return;
            }

            victim.reduceHealthFraction(b, 1/4.0, victim.getName() + " was hurt by its nightmare!");
        }

        @Override
        public boolean shouldSubside(Battle b, ActivePokemon victim) {
            return !victim.hasStatus(StatusNamesies.ASLEEP);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " began having a nightmare!";
        }

        @Override
        public ApplyResult applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return ApplyResult.newResult(victim.hasStatus(StatusNamesies.ASLEEP));
        }
    }

    static class Charge extends PokemonEffect implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Charge() {
            super(PokemonEffectNamesies.CHARGE, 2, 2, false, false);
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
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " began tightening its focus!";
        }

        @Override
        public void damageTaken(Battle b, ActivePokemon damageTaker, int damageAmount) {
            Messages.add(damageTaker.getName() + " lost its focus and couldn't move!");
            damageTaker.getEffects().add(PokemonEffectNamesies.FLINCH.getEffect());
            this.deactivate();
        }
    }

    static class ShellTrap extends PokemonEffect implements PhysicalContactEffect {
        private static final long serialVersionUID = 1L;

        ShellTrap() {
            super(PokemonEffectNamesies.SHELL_TRAP, 1, 1, false, false);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " set up a trap!";
        }

        @Override
        public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
            Messages.add(user.getName() + " set off " + victim.getName() + "'s trap!!");
            this.deactivate();
        }
    }

    static class BeakBlast extends PokemonEffect implements PhysicalContactEffect {
        private static final long serialVersionUID = 1L;

        BeakBlast() {
            super(PokemonEffectNamesies.BEAK_BLAST, 1, 1, false, false);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " started heating up its beak!";
        }

        @Override
        public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
            StatusNamesies.BURNED.getStatus().apply(b, victim, user, CastSource.EFFECT);
        }
    }

    static class FiddyPercentStronger extends PokemonEffect implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        FiddyPercentStronger() {
            super(PokemonEffectNamesies.FIDDY_PERCENT_STRONGER, 1, 1, false, false);
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return 1.5;
        }
    }

    static class Transformed extends PokemonEffect implements ChangeMoveListEffect, DifferentStatEffect, ChangeTypeEffect, PokemonHolder {
        private static final long serialVersionUID = 1L;

        private PokemonNamesies pokemon;
        private MoveList moveList;
        private int[] stats;
        private PokeType type;

        Transformed() {
            super(PokemonEffectNamesies.TRANSFORMED, -1, -1, false, false);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " transformed into " + b.getOtherPokemon(victim).namesies().getName() + "!";
        }

        @Override
        public void afterCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            // Pokemon to transform into
            ActivePokemon transformee = b.getOtherPokemon(victim);
            pokemon = transformee.namesies();

            // Set the new stats
            stats = new int[Stat.NUM_STATS];
            for (int i = 0; i < stats.length; i++) {
                stats[i] = victim.stats().calculate(i, transformee.getPokemonInfo().getStats());
            }
            stats[Stat.HP.index()] = victim.getMaxHP();

            // Copy the move list
            MoveList transformeeMoves = transformee.getMoves(b);
            List<Move> moves = new ArrayList<>();
            for (Move move : transformeeMoves) {
                moves.add(new Move(move.getAttack(), 5));
            }
            this.moveList = new MoveList(moves);

            // Copy all stages
            for (Stat stat : Stat.BATTLE_STATS) {
                victim.getStages().setStage(stat, transformee.getStage(stat));
            }

            // Copy the type
            type = transformee.getPokemonInfo().getType();

            // Castaway
            Messages.add(new MessageUpdate().withNewPokemon(pokemon, transformee.isShiny(), true, victim.isPlayer()));
            Messages.add(new MessageUpdate().updatePokemon(b, victim));
        }

        @Override
        public MoveList getMoveList(MoveList actualMoves) {
            return moveList;
        }

        @Override
        public Integer getStat(ActivePokemon user, Stat stat) {
            return stats[stat.index()];
        }

        @Override
        public PokeType getType(Battle b, ActivePokemon p, boolean display) {
            return type;
        }

        @Override
        public PokemonNamesies getPokemon() {
            return pokemon;
        }

        @Override
        public ApplyResult applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            if (b.getOtherPokemon(victim).hasEffect(this.namesies())) {
                // Cannot transform into transformed Pokemon
                return ApplyResult.failure();
            } else if ((caster.hasAbility(AbilityNamesies.ILLUSION) && caster.getAbility().isActive())) {
                // Also cannot transform into an Illusioned Pokemon
                return ApplyResult.failure();
            }

            return ApplyResult.success();
        }
    }

    static class Substitute extends PokemonEffect implements AbsorbDamageEffect, PassableEffect, StickyHoldEffect, StatProtectingEffect, EffectPreventionEffect, StatusPreventionEffect {
        private static final long serialVersionUID = 1L;

        private int hp;

        // Substitute-piercing moves, Sound-based moves, and Pokemon with the Infiltrator ability bypass Substitute
        private boolean infiltrated(ActivePokemon user) {
            if (user.hasAbility(AbilityNamesies.INFILTRATOR)) {
                return true;
            }

            // TODO: I don't know if this is sufficient or if we need a CastSource situation or what
            if (!user.isAttacking()) {
                return false;
            }

            Attack attack = user.getAttack();
            return attack.isMoveType(MoveType.SUBSTITUTE_PIERCING) || attack.isMoveType(MoveType.SOUND_BASED);
        }

        Substitute() {
            super(PokemonEffectNamesies.SUBSTITUTE, -1, -1, false, false);
        }

        @Override
        public void afterCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            this.hp = victim.forceReduceHealthFraction(b, .25, victim.getName() + " put in a substitute!") + 1;

            // TODO: This should definitely be in some sort of function I don't like this at all
            String imageName = "substitute" + (victim.isPlayer() ? "-back" : "");
            Messages.add(new MessageUpdate().updatePokemon(b, victim).withImageName(imageName, victim.isPlayer()));
        }

        @Override
        public ApplyResult preventEffect(Battle b, ActivePokemon caster, ActivePokemon victim, EffectNamesies effectName, CastSource source) {
            // Only block externally applied effects
            if (caster == victim) {
                return ApplyResult.success();
            }

            // Substitute only blocks Pokemon effects
            if (!(effectName instanceof PokemonEffectNamesies)) {
                return ApplyResult.success();
            }

            // Those pesky infiltrators
            if (this.infiltrated(caster)) {
                return ApplyResult.success();
            }

            // TODO: Attacks with multiple failing attacks print failure multiple times
            // Ex: Tickle prints "...but it failed!" twice
            // Swagger prints "Raised Attack!" then "...but it failed!" (referring to failed Confusion)
            return ApplyResult.failure();
        }

        @Override
        public boolean absorbDamage(Battle b, ActivePokemon damageTaker, int damageAmount) {
            this.hp -= damageAmount;
            if (this.hp <= 0) {
                Messages.add(new MessageUpdate("The substitute broke!").withNewPokemon(damageTaker.namesies(), damageTaker.isShiny(), true, damageTaker.isPlayer()));
                this.deactivate();
            } else {
                Messages.add("The substitute absorbed the hit!");
            }

            // Substitute always blocks damage
            return true;
        }

        @Override
        public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
            return !this.infiltrated(caster);
        }

        @Override
        public String preventionMessage(ActivePokemon p, Stat s) {
            return Effect.DEFAULT_FAIL_MESSAGE;
        }

        @Override
        public ApplyResult preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusNamesies status) {
            // Not totally sure if the caster/victim check if sufficient or if we also need to check
            // that the cast source is an attack or something along those lines
            if (caster != victim && !this.infiltrated(caster)) {
                return ApplyResult.failure();
            }

            return ApplyResult.success();
        }

        @Override
        public ApplyResult applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return ApplyResult.newResult(victim.getHPRatio() > .25 && victim.getMaxHP() > 3);
        }
    }

    static class MagicCoat extends PokemonEffect implements TargetSwapperEffect {
        private static final long serialVersionUID = 1L;

        MagicCoat() {
            super(PokemonEffectNamesies.MAGIC_COAT, 1, 1, false, false);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " shrouded itself with a magic coat!";
        }

        @Override
        public boolean swapTarget(Battle b, ActivePokemon user, ActivePokemon opponent) {
            if (user.getAttack().isMagicReflectable()) {
                Messages.add(opponent.getName() + "'s " + "Magic Coat" + " reflected " + user.getName() + "'s move!");
                return true;
            }

            return false;
        }
    }

    static class Bide extends PokemonEffect implements ForceMoveEffect, DamageTakenEffect, IntegerHolder {
        private static final long serialVersionUID = 1L;

        private Move move;
        private int turns;
        private int damage;

        Bide() {
            super(PokemonEffectNamesies.BIDE, -1, -1, true, true);
            this.turns = 1;
            this.damage = 0;
        }

        @Override
        public Move getForcedMove(ActivePokemon attacking) {
            return move;
        }

        @Override
        public int getTurns() {
            return turns;
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " is storing energy!";
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            move = caster.getMove();
        }

        @Override
        public void alternateCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, CastMessageGetter castMessage) {
            // Already has the effect, but not ready for it to end yet -- store dat energy
            if (this.turns > 0) {
                this.turns--;
                this.addCastMessage(b, caster, victim, source, castMessage);
                return;
            }

            // TIME'S UP -- RELEASE DAT STORED ENERGY
            Messages.add(victim.getName() + " released energy!");
            if (this.damage == 0) {
                // Sucks to suck
                Messages.add(Effect.DEFAULT_FAIL_MESSAGE);
            } else {
                // RETALIATION STATION
                victim.callDelayedMove(b, b.getOtherPokemon(victim), AttackNamesies.BIDE);
            }

            // Bye Bye Bidesies
            this.deactivate();
        }

        @Override
        public void damageTaken(Battle b, ActivePokemon damageTaker, int damageAmount) {
            this.damage += damageAmount;
        }

        @Override
        public int getInteger() {
            return 2*this.damage;
        }
    }

    static class HalfWeight extends PokemonEffect implements HalfWeightEffect {
        private static final long serialVersionUID = 1L;

        private int layers;

        HalfWeight() {
            super(PokemonEffectNamesies.HALF_WEIGHT, -1, -1, true, true);
            this.layers = 1;
        }

        @Override
        public void alternateCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, CastMessageGetter castMessage) {
            this.layers++;
        }

        @Override
        public int getHalfAmount() {
            return layers;
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " became nimble!";
        }
    }

    static class PowerTrick extends PokemonEffect implements PassableEffect, StatSwitchingEffect {
        private static final long serialVersionUID = 1L;

        PowerTrick() {
            super(PokemonEffectNamesies.POWER_TRICK, -1, -1, true, true);
        }

        @Override
        public Stat getSwitchStat(Stat s) {
            if (s == Stat.ATTACK) {
                return Stat.DEFENSE;
            } else if (s == Stat.DEFENSE) {
                return Stat.ATTACK;
            } else {
                return s;
            }
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + "'s attack and defense were swapped!";
        }

        @Override
        public void alternateCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, CastMessageGetter castMessage) {
            this.addCastMessage(b, caster, victim, source, castMessage);
            this.deactivate();
        }
    }

    static class HealBlock extends PokemonEffect implements SelfAttackBlocker {
        private static final long serialVersionUID = 1L;

        HealBlock() {
            super(PokemonEffectNamesies.HEAL_BLOCK, 5, 5, false, false);
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
        public String getSourcePreventMessage(ActivePokemon victim, String sourceName) {
            return victim.getName() + "'s " + sourceName + " prevents heal block!";
        }

        @Override
        public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
            return victim.getName() + " is no longer heal blocked due to its " + sourceName + "!";
        }

        @Override
        public boolean block(Battle b, ActivePokemon user) {
            // TODO: Test
            return user.getAttack().isMoveType(MoveType.HEALING);
        }
    }

    static class Infatuation extends PokemonEffect implements BeforeAttackPreventingEffect {
        private static final long serialVersionUID = 1L;

        Infatuation() {
            super(PokemonEffectNamesies.INFATUATION, -1, -1, false, false);
        }

        @Override
        public boolean canAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            Messages.add(attacking.getName() + " is in love with " + defending.getName() + "!");
            if (RandomUtils.chanceTest(50)) {
                return true;
            }

            Messages.add(attacking.getName() + "'s infatuation kept it from attacking!");
            return false;
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " fell in love!";
        }

        @Override
        public String getSourcePreventMessage(ActivePokemon victim, String sourceName) {
            return victim.getName() + "'s " + sourceName + " prevents infatuation!";
        }

        @Override
        public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
            return victim.getName() + " is no longer infatuated due to its " + sourceName + "!";
        }

        @Override
        public ApplyResult applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return ApplyResult.newResult(Gender.oppositeGenders(caster, victim));
        }
    }

    static class Snatch extends PokemonEffect implements UserSwapperEffect {
        private static final long serialVersionUID = 1L;

        Snatch() {
            super(PokemonEffectNamesies.SNATCH, 1, 1, false, false);
        }

        @Override
        public boolean swapUser(Battle b, ActivePokemon user, ActivePokemon opponent) {
            if (user.getAttack().isSnatchable() && !user.isUsingTempMove()) {
                Messages.add(opponent.getName() + " snatched " + user.getName() + "'s move!");
                opponent.callTempMove(user.getAttack().namesies(), () -> {
                    Attack attack = opponent.getAttack();
                    attack.beginAttack(b, opponent, user);
                    attack.apply(opponent, user, b);
                    attack.endAttack(b, opponent, user);
                });
                return true;
            }

            return false;
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " is awaiting an opportunity!";
        }
    }

    static class Grudge extends PokemonEffect implements FaintEffect {
        private static final long serialVersionUID = 1L;

        Grudge() {
            super(PokemonEffectNamesies.GRUDGE, -1, -1, false, false);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " wants " + b.getOtherPokemon(victim).getName() + " to bear a grudge!";
        }

        @Override
        public void deathWish(Battle b, ActivePokemon dead, ActivePokemon murderer) {
            Messages.add(murderer.getName() + "'s " + murderer.getAttack().getName() + " lost all its PP due to " + dead.getName() + "'s grudge!");
            murderer.getMove().reducePP(murderer.getMove().getPP());
        }
    }

    static class DestinyBond extends PokemonEffect implements StartAttackEffect, FaintEffect {
        private static final long serialVersionUID = 1L;

        DestinyBond() {
            super(PokemonEffectNamesies.DESTINY_BOND, -1, -1, false, false);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " is trying to take " + b.getOtherPokemon(victim).getName() + " down with it!";
        }

        @Override
        public void beforeAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // Destiny Bond stays active until the user moves again
            // TODO: Technically this should deactivate even if asleep, paralyzed, fully confused, etc
            this.deactivate();
        }

        @Override
        public void deathWish(Battle b, ActivePokemon dead, ActivePokemon murderer) {
            murderer.killKillKillMurderMurderMurder(b, dead.getName() + " took " + murderer.getName() + " down with it!");
        }

        @Override
        public ApplyResult applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return ApplyResult.newResult(RandomUtils.chanceTest((int)(100*caster.getSuccessionDecayRate())));
        }
    }

    static class PerishSong extends PokemonEffect implements PassableEffect, EndTurnEffect {
        private static final long serialVersionUID = 1L;

        PerishSong() {
            super(PokemonEffectNamesies.PERISH_SONG, 3, 3, false, false);
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            int turn = this.getTurns() - 1;
            Messages.add(victim.getName() + "'s Perish Song count fell to " + turn + "!");
            if (turn == 0) {
                victim.killKillKillMurderMurderMurder(b, "");
            }
        }
    }

    static class Embargo extends PokemonEffect implements PassableEffect, ItemBlockerEffect {
        private static final long serialVersionUID = 1L;

        Embargo() {
            super(PokemonEffectNamesies.EMBARGO, 5, 5, false, false);
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

        private HoldItem consumed;

        ConsumedItem() {
            super(PokemonEffectNamesies.CONSUMED_ITEM, -1, -1, true, false);
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            consumed = victim.getHeldItem();
            victim.removeItem();
            victim.getEffects().remove(this.namesies());
        }

        @Override
        public HoldItem getItem() {
            return consumed;
        }
    }

    static class Powder extends PokemonEffect implements SelfAttackBlocker {
        private static final long serialVersionUID = 1L;

        Powder() {
            super(PokemonEffectNamesies.POWDER, 1, 1, false, false);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " sprinkled powder on " + victim.getName() + "!";
        }

        @Override
        public boolean block(Battle b, ActivePokemon user) {
            // Fire-type moves makes the user explode
            return user.isAttackType(Type.FIRE);
        }

        @Override
        public void alternateEffect(Battle b, ActivePokemon user) {
            user.reduceHealthFraction(b, 1/4.0, "");
        }

        @Override
        public String getBlockMessage(ActivePokemon user) {
            return "The powder exploded!";
        }

        @Override
        public ApplyResult applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return ApplyResult.newResult(!PowderBlocker.containsPowderBlocker(b, victim));
        }
    }

    static class EatenBerry extends PokemonEffect {
        private static final long serialVersionUID = 1L;

        EatenBerry() {
            super(PokemonEffectNamesies.EATEN_BERRY, -1, -1, false, false);
        }
    }

    static class Raging extends PokemonEffect implements TakeDamageEffect, ModifyStageMessenger {
        private static final long serialVersionUID = 1L;

        Raging() {
            super(PokemonEffectNamesies.RAGING, -1, -1, false, false);
        }

        @Override
        public void onDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            Move lastMoveUsed = victim.getLastMoveUsed();
            if (lastMoveUsed == null || lastMoveUsed.getAttack().namesies() != AttackNamesies.RAGE) {
                this.deactivate();
                return;
            }

            // Bulbasaur's Rage increased its Attack!
            new StageModifier(1, Stat.ATTACK).withMessage(this).modify(b, victim, victim, CastSource.EFFECT);
        }

        @Override
        public String getMessage(String victimName, String possessiveVictim, String statName, String changed) {
            return victimName + "'s Rage " + changed + " " + possessiveVictim + " " + statName + "!";
        }
    }

    static class StickyTar extends PokemonEffect implements OpponentPowerChangeEffect {
        private static final long serialVersionUID = 1L;

        StickyTar() {
            super(PokemonEffectNamesies.STICKY_TAR, -1, -1, false, false);
        }

        @Override
        public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.FIRE) ? 2 : 1;
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return victim.getName() + " became weak to fire!";
        }
    }
}
