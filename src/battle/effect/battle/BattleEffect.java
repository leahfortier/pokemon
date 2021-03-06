package battle.effect.battle;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.ApplyResult;
import battle.effect.Effect;
import battle.effect.EffectInterfaces.LockingEffect;
import battle.effect.EffectNamesies.BattleEffectNamesies;
import battle.effect.InvokeInterfaces.BattleEndTurnEffect;
import battle.effect.InvokeInterfaces.GroundedEffect;
import battle.effect.InvokeInterfaces.ItemBlockerEffect;
import battle.effect.InvokeInterfaces.PowerChangeEffect;
import battle.effect.InvokeInterfaces.StageChangingEffect;
import battle.effect.InvokeInterfaces.StatChangingEffect;
import battle.effect.InvokeInterfaces.StatSwitchingEffect;
import battle.effect.InvokeInterfaces.StatusPreventionEffect;
import battle.effect.InvokeInterfaces.SuperDuperEndTurnEffect;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.source.CastSource;
import battle.effect.status.StatusNamesies;
import pokemon.stat.Stat;
import type.Type;

import java.util.List;

public abstract class BattleEffect<NamesiesType extends BattleEffectNamesies> extends Effect<NamesiesType> {
    private static final long serialVersionUID = 1L;

    public BattleEffect(NamesiesType name, int minTurns, int maxTurns, boolean canHave, boolean hasAlternateCast) {
        super(name, minTurns, maxTurns, canHave, hasAlternateCast);
    }

    @Override
    protected void addEffect(Battle b, ActivePokemon victim) {
        b.addEffect(this);
    }

    @Override
    protected boolean hasEffect(Battle b, ActivePokemon victim) {
        return b.hasEffect(this.namesies());
    }

    @Override
    protected BattleEffect<NamesiesType> getEffect(Battle b, ActivePokemon victim) {
        return (BattleEffect<NamesiesType>)b.getEffects().get(this.namesies());
    }

    // EVERYTHING BELOW IS GENERATED ###

    /**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

    static class Gravity extends BattleEffect<StandardBattleEffectNamesies> implements GroundedEffect, StageChangingEffect {
        private static final long serialVersionUID = 1L;

        Gravity() {
            super(StandardBattleEffectNamesies.GRAVITY, 5, 5, false, false);
        }

        @Override
        public int adjustStage(Battle b, ActivePokemon p, Stat s) {
            return s == Stat.EVASION ? -2 : 0;
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return "Gravity intensified!";
        }

        @Override
        public void afterCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            removeLevitation(b, caster);
            removeLevitation(b, victim);
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The gravity returned to normal.";
        }
    }

    static class WaterSport extends BattleEffect<StandardBattleEffectNamesies> implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        WaterSport() {
            super(StandardBattleEffectNamesies.WATER_SPORT, 5, 5, false, false);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return "Fire's power was weakened!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The effects of Water Sport wore off.";
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.FIRE) ? .33 : 1;
        }
    }

    static class MudSport extends BattleEffect<StandardBattleEffectNamesies> implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        MudSport() {
            super(StandardBattleEffectNamesies.MUD_SPORT, 5, 5, false, false);
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return "Electricity's power was weakened!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The effects of Mud Sport wore off.";
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.ELECTRIC) ? .33 : 1;
        }
    }

    static class WonderRoom extends BattleEffect<StandardBattleEffectNamesies> implements StatSwitchingEffect {
        private static final long serialVersionUID = 1L;

        WonderRoom() {
            super(StandardBattleEffectNamesies.WONDER_ROOM, 5, 5, true, true);
        }

        @Override
        public Stat getSwitchStat(Stat s) {
            // Defense and Special Defense are swapped
            if (s == Stat.DEFENSE) {
                return Stat.SP_DEFENSE;
            } else if (s == Stat.SP_DEFENSE) {
                return Stat.DEFENSE;
            } else {
                return s;
            }
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The dimensions of the wonder room returned to normal.";
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " twisted the dimensions to switch defense and special defense!";
        }

        @Override
        public void alternateCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, CastMessageGetter castMessage) {
            // Remove the effect if it's already in play
            this.subside(b, caster);
        }
    }

    static class TrickRoom extends BattleEffect<StandardBattleEffectNamesies> {
        private static final long serialVersionUID = 1L;

        TrickRoom() {
            super(StandardBattleEffectNamesies.TRICK_ROOM, 5, 5, true, true);
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The dimensions of the trick room returned to normal.";
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " twisted the dimensions to switch speeds!";
        }

        @Override
        public void alternateCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, CastMessageGetter castMessage) {
            // Remove the effect if it's already in play
            this.subside(b, caster);
        }
    }

    static class MagicRoom extends BattleEffect<StandardBattleEffectNamesies> implements ItemBlockerEffect {
        private static final long serialVersionUID = 1L;

        MagicRoom() {
            super(StandardBattleEffectNamesies.MAGIC_ROOM, 5, 5, true, true);
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The dimensions of the magic room returned to normal.";
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " twisted the dimensions to prevent using items!";
        }

        @Override
        public void alternateCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, CastMessageGetter castMessage) {
            // Remove the effect if it's already in play
            this.subside(b, caster);
        }
    }

    static class FieldUproar extends BattleEffect<StandardBattleEffectNamesies> implements StatusPreventionEffect, SuperDuperEndTurnEffect {
        private static final long serialVersionUID = 1L;

        FieldUproar() {
            super(StandardBattleEffectNamesies.FIELD_UPROAR, -1, -1, false, false);
        }

        @Override
        public ApplyResult preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusNamesies status) {
            if (status == StatusNamesies.ASLEEP) {
                return ApplyResult.failure("The uproar prevents sleep!!");
            }

            return ApplyResult.success();
        }

        @Override
        public boolean theVeryVeryEnd(Battle b, ActivePokemon p) {
            if (b.getPlayer().front().hasEffect(PokemonEffectNamesies.UPROAR) || b.getOpponent().front().hasEffect(PokemonEffectNamesies.UPROAR)) {
                return false;
            }

            this.deactivate();
            return true;
        }
    }

    static class PowerSplit extends BattleEffect<StandardBattleEffectNamesies> implements StatChangingEffect {
        private static final long serialVersionUID = 1L;

        PowerSplit() {
            super(StandardBattleEffectNamesies.POWER_SPLIT, -1, -1, false, false);
        }

        @Override
        public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
            // If the stat is a splitting stat, return the average between the user and the opponent
            if (s == Stat.ATTACK || s == Stat.SP_ATTACK) {
                return (p.getStat(b, s) + opp.getStat(b, s))/2;
            }

            return stat;
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " split the power!";
        }
    }

    static class GuardSplit extends BattleEffect<StandardBattleEffectNamesies> implements StatChangingEffect {
        private static final long serialVersionUID = 1L;

        GuardSplit() {
            super(StandardBattleEffectNamesies.GUARD_SPLIT, -1, -1, false, false);
        }

        @Override
        public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
            // If the stat is a splitting stat, return the average between the user and the opponent
            if (s == Stat.DEFENSE || s == Stat.SP_DEFENSE) {
                return (p.getStat(b, s) + opp.getStat(b, s))/2;
            }

            return stat;
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " split the defense!";
        }
    }

    static class JawLocked extends BattleEffect<StandardBattleEffectNamesies> implements BattleEndTurnEffect, LockingEffect {
        private static final long serialVersionUID = 1L;

        // The Pokemon that are locked by the jaw
        private ActivePokemon caster;
        private ActivePokemon victim;

        JawLocked() {
            super(StandardBattleEffectNamesies.JAW_LOCKED, -1, -1, false, false);
        }

        @Override
        public String trappingMessage(ActivePokemon trapped) {
            return trapped.getName() + " is trapped by Jaw Lock!";
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            this.caster = caster;
            this.victim = victim;
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return user.getName() + " locked jaws with " + victim.getName() + "!";
        }

        @Override
        public void singleEndTurnEffect(Battle b, ActivePokemon victim) {
            // Deactivate if Jaw Locked Pokemon aren't around
            this.checkActive(b);
        }

        @Override
        public List<ActivePokemon> getLocking() {
            return List.of(caster, victim);
        }
    }

    static class CorrosiveGas extends BattleEffect<StandardBattleEffectNamesies> implements ItemBlockerEffect {
        private static final long serialVersionUID = 1L;

        private ActivePokemon victim;

        CorrosiveGas() {
            super(StandardBattleEffectNamesies.CORROSIVE_GAS, -1, -1, true, false);
        }

        @Override
        public boolean blockItem(Battle b, ActivePokemon p) {
            return p == victim;
        }

        @Override
        public void beforeCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            this.victim = victim;
        }
    }
}
