package battle.effect;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.Attack;
import battle.attack.AttackInterface;
import battle.attack.Move;
import battle.attack.MoveType;
import battle.effect.EffectInterfaces.OnDamageEffect;
import battle.effect.EffectInterfaces.PowderMove;
import battle.effect.EffectInterfaces.SimpleStatModifyingEffect;
import battle.effect.EffectInterfaces.SwappableEffect;
import battle.effect.EffectNamesies.BattleEffectNamesies;
import battle.effect.attack.MultiTurnMove;
import battle.effect.battle.BattleEffect;
import battle.effect.battle.weather.WeatherNamesies;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.source.CastSource;
import battle.effect.status.StatusNamesies;
import item.ItemNamesies;
import main.Game;
import main.Global;
import map.overworld.TerrainType;
import map.overworld.wild.WildEncounter;
import map.overworld.wild.WildEncounterInfo;
import message.Messages;
import pokemon.ability.Ability;
import pokemon.ability.AbilityNamesies;
import pokemon.active.MoveList;
import pokemon.stat.Stat;
import trainer.Trainer;
import type.PokeType;
import type.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Holds generated interfaces with invoke methods
public final class InvokeInterfaces {

    // Class to hold interfaces -- should not be instantiated
    private InvokeInterfaces() {
        Global.error(this.getClass().getSimpleName() + " class cannot be instantiated.");
    }

    // EVERYTHING BELOW IS GENERATED ###

    // This is used when the user applies direct damage to an opponent, and has special effects associated with the user
    public interface ApplyDamageEffect extends OnDamageEffect {

        // user: The user of that attack, the one who is implementing this effect
        // victim: The Pokemon that received the attack
        void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim);

        static void invokeApplyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (user.isFainted(b)) {
                return;
            }

            List<InvokeEffect> invokees = b.getEffectsList(user, user.getAttack());
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof ApplyDamageEffect && invokee.isActiveEffect()) {
                    ApplyDamageEffect effect = (ApplyDamageEffect)invokee;
                    if (effect.shouldIgnore(victim)) {
                        continue;
                    }

                    effect.applyDamageEffect(b, user, victim);

                    if (user.isFainted(b)) {
                        return;
                    }
                }
            }
        }
    }

    // This is used when the user applies direct damage to an opponent, and has special effects associated with the user
    public interface OpponentApplyDamageEffect extends OnDamageEffect {

        // user: The user of that attack
        // victim: The Pokemon that received the attack, the one who is probably implementing this effect
        void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim);

        static void invokeOpponentApplyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (user.isFainted(b)) {
                return;
            }

            List<InvokeEffect> invokees = b.getEffectsList(victim);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof OpponentApplyDamageEffect && invokee.isActiveEffect()) {
                    OpponentApplyDamageEffect effect = (OpponentApplyDamageEffect)invokee;
                    if (effect.shouldIgnore(victim)) {
                        continue;
                    }

                    effect.applyDamageEffect(b, user, victim);

                    if (user.isFainted(b)) {
                        return;
                    }
                }
            }
        }
    }

    // This is used when the user applies direct damage to an opponent, and has special effects associated with the victim
    public interface TakeDamageEffect extends OnDamageEffect {

        // user: The user of the attack
        // victim: The one who is taking damage and is implementing this effect
        void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim);

        static void invokeTakeDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (victim.isFainted(b)) {
                return;
            }

            List<InvokeEffect> invokees = b.getEffectsList(victim);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof TakeDamageEffect && invokee.isActiveEffect()) {
                    TakeDamageEffect effect = (TakeDamageEffect)invokee;
                    if (effect.shouldIgnore(victim)) {
                        continue;
                    }

                    effect.takeDamage(b, user, victim);

                    if (victim.isFainted(b)) {
                        return;
                    }
                }
            }
        }
    }

    // This is used when the user applies direct damage to an opponent, and has special effects associated with the victim
    public interface OpponentTakeDamageEffect extends OnDamageEffect {

        // user: The user of the attack and implementer of the effect
        // victim: The Pokemon who is taking damage
        void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim);

        static void invokeOpponentTakeDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (victim.isFainted(b)) {
                return;
            }

            List<InvokeEffect> invokees = b.getEffectsList(user);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof OpponentTakeDamageEffect && invokee.isActiveEffect()) {
                    OpponentTakeDamageEffect effect = (OpponentTakeDamageEffect)invokee;
                    if (effect.shouldIgnore(victim)) {
                        continue;
                    }

                    effect.takeDamage(b, user, victim);

                    if (victim.isFainted(b)) {
                        return;
                    }
                }
            }
        }
    }

    public interface EndTurnEffect {
        void applyEndTurn(ActivePokemon victim, Battle b);

        static void invokeEndTurnEffect(ActivePokemon victim, Battle b) {
            if (victim.isFainted(b)) {
                return;
            }

            List<InvokeEffect> invokees = b.getIndividualAndTeamEffects(victim);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof EndTurnEffect && invokee.isActiveEffect()) {
                    EndTurnEffect effect = (EndTurnEffect)invokee;
                    effect.applyEndTurn(victim, b);

                    if (victim.isFainted(b)) {
                        return;
                    }
                }
            }
        }
    }

    // EndTurnEffect for BattleEffects -- those should only use this and not the standard EndTurnEffect!!!
    public interface BattleEndTurnEffect extends EffectInterface {
        default void singleEndTurnEffect(Battle b, ActivePokemon victim) {}

        default String getEndTurnMessage() {
            // Definitely not required to have a message here
            return "";
        }

        default boolean endTurnSubsider() {
            // Should override to true when this effect should be deactivated in the end turn method when out of turns
            return false;
        }

        default void applyEndTurn(Battle b) {
            if (this.endTurnSubsider() && this.getTurns() == 1) {
                Messages.add(this.getSubsideMessage(null));
                this.deactivate();
                return;
            }

            Messages.add(this.getEndTurnMessage());

            ActivePokemon playerFront = b.getPlayer().front();
            if (!playerFront.isFainted(b)) {
                this.singleEndTurnEffect(b, playerFront);
            }

            ActivePokemon oppFront = b.getOpponent().front();
            if (!oppFront.isFainted(b)) {
                this.singleEndTurnEffect(b, oppFront);
            }
        }

        static void invokeBattleEndTurnEffect(Battle b) {
            List<BattleEffect<? extends BattleEffectNamesies>> invokees = b.getEffects().asList();
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof BattleEndTurnEffect && invokee.isActiveEffect()) {
                    BattleEndTurnEffect effect = (BattleEndTurnEffect)invokee;
                    effect.applyEndTurn(b);
                }
            }
        }
    }

    public interface SuperDuperEndTurnEffect {
        boolean theVeryVeryEnd(Battle b, ActivePokemon p);

        static boolean checkSuperDuperEndTurnEffect(Battle b, ActivePokemon p) {
            List<InvokeEffect> invokees = b.getEffectsList(p);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof SuperDuperEndTurnEffect && invokee.isActiveEffect()) {
                    SuperDuperEndTurnEffect effect = (SuperDuperEndTurnEffect)invokee;
                    if (effect.theVeryVeryEnd(b, p)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public interface AttackMissedEffect {
        void afterMiss(Battle b, ActivePokemon misser);

        static void invokeAttackMissedEffect(Battle b, ActivePokemon misser) {
            List<InvokeEffect> invokees = b.getEffectsList(misser, misser.getAttack());
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof AttackMissedEffect && invokee.isActiveEffect()) {
                    AttackMissedEffect effect = (AttackMissedEffect)invokee;
                    effect.afterMiss(b, misser);
                }
            }
        }
    }

    // Needs a separate invoke method in addition to naturally being apart of the AttackMissedEffect because it can be invoked from other places as well
    public interface CrashDamageMove extends AttackInterface, AttackMissedEffect {
        int getMaxHealthPercentageDenominator();

        @Override
        default void afterMiss(Battle b, ActivePokemon misser) {
            // Crash when we miss
            this.crash(b, misser);
        }

        default void crash(Battle b, ActivePokemon user) {
            // Crash damage must be at least one and is affected by Magic Guard
            int crashDamage = (int)Math.max(Math.ceil((double)user.getMaxHP()/getMaxHealthPercentageDenominator()), 1);
            user.indirectReduceHealth(b, crashDamage, false, user.getName() + " kept going and crashed!");
        }

        static void invokeCrashDamageMove(Battle b, ActivePokemon user) {
            List<InvokeEffect> invokees = Collections.singletonList(user.getAttack());
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof CrashDamageMove && invokee.isActiveEffect()) {
                    CrashDamageMove effect = (CrashDamageMove)invokee;
                    effect.crash(b, user);
                }
            }
        }
    }

    public interface BarrierEffect extends SwappableEffect, SimpleStatModifyingEffect {
        String getBreakMessage(ActivePokemon breaker);

        default void breakBarrier(ActivePokemon breaker) {
            Messages.add(this.getBreakMessage(breaker));
            this.deactivate();
        }

        @Override
        default boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            // Critical hits and Pokemon with the Infiltrator ability bypass the barrier's boosts
            return !opp.isCriticalHit() && !opp.hasAbility(AbilityNamesies.INFILTRATOR);
        }

        @Override
        default double getModifier() {
            return 2;
        }

        static void breakBarriers(Battle b, ActivePokemon broken, ActivePokemon breaker) {
            List<InvokeEffect> invokees = b.getEffectsList(broken);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof BarrierEffect && invokee.isActiveEffect()) {
                    BarrierEffect effect = (BarrierEffect)invokee;
                    effect.breakBarrier(breaker);
                }
            }
        }
    }

    public interface DefogRelease extends EffectInterface {
        String getDefogReleaseMessage();

        default void releaseDefog() {
            Messages.add(this.getDefogReleaseMessage());
            this.deactivate();
        }

        static void release(Battle b, ActivePokemon released) {
            List<InvokeEffect> invokees = b.getEffectsList(released);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof DefogRelease && invokee.isActiveEffect()) {
                    DefogRelease effect = (DefogRelease)invokee;
                    effect.releaseDefog();
                }
            }
        }
    }

    public interface RapidSpinRelease extends EffectInterface {
        String getRapidSpinReleaseMessage(ActivePokemon released);

        default void releaseRapidSpin(ActivePokemon released) {
            Messages.add(this.getRapidSpinReleaseMessage(released));
            this.deactivate();
        }

        static void release(Battle b, ActivePokemon released) {
            List<InvokeEffect> invokees = b.getEffectsList(released);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof RapidSpinRelease && invokee.isActiveEffect()) {
                    RapidSpinRelease effect = (RapidSpinRelease)invokee;
                    effect.releaseRapidSpin(released);
                }
            }
        }
    }

    // This only operates on the ability which I'm not a super huge fan of but then again I'm not passing the battle
    // in here and also fuck illusion srsly maybe it should just be special cased since it's so fucking unique
    public interface NameChanger {
        String getNameChange();
        void setNameChange(Battle b, ActivePokemon victim);

        static String getChangedName(ActivePokemon p) {
            List<InvokeEffect> invokees = Collections.singletonList(p.getAbility());
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof NameChanger && invokee.isActiveEffect()) {
                    NameChanger effect = (NameChanger)invokee;
                    String value = effect.getNameChange();
                    if (value != null) {
                        return value;
                    }
                }
            }

            return null;
        }

        static void setNameChanges(Battle b, ActivePokemon victim) {
            List<InvokeEffect> invokees = b.getEffectsList(victim);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof NameChanger && invokee.isActiveEffect()) {
                    NameChanger effect = (NameChanger)invokee;
                    effect.setNameChange(b, victim);
                }
            }
        }
    }

    public interface EntryEffect {
        void enter(Battle b, ActivePokemon enterer);

        static void invokeEntryEffect(Battle b, ActivePokemon enterer) {
            List<InvokeEffect> invokees = b.getEffectsList(enterer);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof EntryEffect && invokee.isActiveEffect()) {
                    EntryEffect effect = (EntryEffect)invokee;
                    effect.enter(b, enterer);
                }
            }
        }
    }

    public interface StatLoweredEffect {

        // victim: The Pokemon whose stat is being lowered
        // selfCaster: true if the victim caused its own stats to be lowered
        void takeItToTheNextLevel(Battle b, ActivePokemon victim, boolean selfCaster);

        static void invokeStatLoweredEffect(Battle b, ActivePokemon victim, boolean selfCaster) {
            List<InvokeEffect> invokees = b.getEffectsList(victim);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof StatLoweredEffect && invokee.isActiveEffect()) {
                    StatLoweredEffect effect = (StatLoweredEffect)invokee;
                    effect.takeItToTheNextLevel(b, victim, selfCaster);
                }
            }
        }
    }

    public interface LevitationEffect {
        default void fall(ActivePokemon fallen) {}

        static void falllllllll(Battle b, ActivePokemon fallen) {
            List<InvokeEffect> invokees = b.getEffectsList(fallen);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof LevitationEffect && invokee.isActiveEffect()) {
                    LevitationEffect effect = (LevitationEffect)invokee;
                    effect.fall(fallen);
                }
            }
        }

        static boolean containsLevitationEffect(Battle b, ActivePokemon p, ActivePokemon moldBreaker) {
            List<InvokeEffect> invokees = b.getEffectsList(p);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof LevitationEffect && invokee.isActiveEffect()) {

                    // If this is an ability that is being affected by mold breaker, we don't want to do anything with it
                    if (invokee instanceof Ability && !((Ability)invokee).unbreakableMold() && moldBreaker != null && moldBreaker.breaksTheMold()) {
                        continue;
                    }

                    return true;
                }
            }

            return false;
        }
    }

    public interface FaintEffect {
        void deathWish(Battle b, ActivePokemon dead, ActivePokemon murderer);

        static void grantDeathWish(Battle b, ActivePokemon dead, ActivePokemon murderer) {
            List<InvokeEffect> invokees = b.getEffectsList(dead);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof FaintEffect && invokee.isActiveEffect()) {
                    FaintEffect effect = (FaintEffect)invokee;
                    effect.deathWish(b, dead, murderer);
                }
            }
        }
    }

    // KILL KILL KILL MURDER MURDER MURDER
    public interface MurderEffect {
        void killWish(Battle b, ActivePokemon dead, ActivePokemon murderer);

        static void killKillKillMurderMurderMurder(Battle b, ActivePokemon dead, ActivePokemon murderer) {
            List<InvokeEffect> invokees = b.getEffectsList(murderer, murderer.getAttack());
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof MurderEffect && invokee.isActiveEffect()) {
                    MurderEffect effect = (MurderEffect)invokee;
                    effect.killWish(b, dead, murderer);
                }
            }
        }
    }

    public interface EndBattleEffect {
        void afterBattle(Trainer player, ActivePokemon p);

        static void invokeEndBattleEffect(List<? extends InvokeEffect> invokees, Trainer player, ActivePokemon p) {
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof EndBattleEffect && invokee.isActiveEffect()) {
                    EndBattleEffect effect = (EndBattleEffect)invokee;
                    effect.afterBattle(player, p);
                }
            }
        }
    }

    public interface GroundedEffect extends SelfAttackBlocker {

        @Override
        default boolean block(Battle b, ActivePokemon user) {
            return user.getAttack().isMoveType(MoveType.AIRBORNE);
        }

        default void removeLevitation(Battle b, ActivePokemon p) {
            if (p.isSemiInvulnerableFlying()) {
                ((MultiTurnMove)p.getAttack()).resetReady();
                Effect.cast(PokemonEffectNamesies.FLINCH, b, p, p, CastSource.EFFECT, p.getName() + " fell to the ground!");
            }

            LevitationEffect.falllllllll(b, p);
        }

        static boolean containsGroundedEffect(Battle b, ActivePokemon p) {
            List<InvokeEffect> invokees = b.getEffectsList(p);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof GroundedEffect && invokee.isActiveEffect()) {
                    return true;
                }
            }

            return false;
        }
    }

    public interface SemiInvulnerableBypasser {

        // Attacker is the Pokemon whose accuracy is being evaluated and is the Pokemon on which this effect is attached to
        // This is evaluated BEFORE the semi-invulnerable checks, so if this returns true, the move will hit even if the defending is semi-invulnerable
        boolean semiInvulnerableBypass(Battle b, ActivePokemon attacking, ActivePokemon defending);

        static boolean bypassAccuracyCheck(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            List<InvokeEffect> invokees = b.getEffectsList(attacking, attacking.getAttack());
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof SemiInvulnerableBypasser && invokee.isActiveEffect()) {
                    SemiInvulnerableBypasser effect = (SemiInvulnerableBypasser)invokee;
                    if (effect.semiInvulnerableBypass(b, attacking, defending)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public interface BasicAccuracyBypassEffect {

        // Attacker is the Pokemon whose accuracy is being evaluated and is the Pokemon on which this effect is attached to
        // This is evaluated AFTER the semi-invulnerable checks
        // Should use SemiInvulnerableBypasser for moves that hit fly, dig, etc.
        boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending);

        static boolean bypassAccuracyCheck(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            List<InvokeEffect> invokees = b.getEffectsList(attacking, attacking.getAttack());
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof BasicAccuracyBypassEffect && invokee.isActiveEffect()) {
                    BasicAccuracyBypassEffect effect = (BasicAccuracyBypassEffect)invokee;
                    if (effect.bypassAccuracy(b, attacking, defending)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public interface OpponentAccuracyBypassEffect {

        // Attacker is the Pokemon whose accuracy is being evaluated, defending is the Pokemon on which this effect is attached to
        // This is evaluated BEFORE the semi-invulnerable checks, so if this returns true, the move will hit even if the defending is semi-invulnerable
        boolean opponentBypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending);

        static boolean bypassAccuracyCheck(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            List<InvokeEffect> invokees = b.getEffectsList(defending);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof OpponentAccuracyBypassEffect && invokee.isActiveEffect()) {
                    OpponentAccuracyBypassEffect effect = (OpponentAccuracyBypassEffect)invokee;
                    if (effect.opponentBypassAccuracy(b, attacking, defending)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public interface AttackSelectionEffect {
        boolean usable(Battle b, ActivePokemon p, Move m);
        String getUnusableMessage(ActivePokemon p);

        static AttackSelectionEffect getUnusableEffect(Battle b, ActivePokemon p, Move m) {
            List<InvokeEffect> invokees = b.getEffectsList(p);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof AttackSelectionEffect && invokee.isActiveEffect()) {
                    AttackSelectionEffect effect = (AttackSelectionEffect)invokee;
                    if (!effect.usable(b, p, m)) {
                        return effect;
                    }
                }
            }

            return null;
        }
    }

    public interface WeatherBlockerEffect {
        boolean block(WeatherNamesies weather);

        static boolean checkBlocked(Battle b, ActivePokemon p, WeatherNamesies weather) {
            // Non-overground Pokemon are immune to weather effects
            if (p.isSemiInvulnerableNotOverground()) {
                return true;
            }

            List<InvokeEffect> invokees = b.getEffectsList(p);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof WeatherBlockerEffect && invokee.isActiveEffect()) {
                    WeatherBlockerEffect effect = (WeatherBlockerEffect)invokee;
                    if (effect.block(weather)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    // Any effect that implements this will prevent a Pokemon with said effect from escaping battle
    public interface TrappingEffect {
        String trappingMessage(ActivePokemon trapped);

        default boolean trapped(Battle b, ActivePokemon escaper) {
            return true;
        }

        static boolean isTrapped(Battle b, ActivePokemon escaper) {
            List<InvokeEffect> invokees = b.getEffectsList(escaper);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof TrappingEffect && invokee.isActiveEffect()) {
                    TrappingEffect effect = (TrappingEffect)invokee;
                    if (effect.trapped(b, escaper)) {
                        Messages.add(effect.trappingMessage(escaper));
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public interface OpponentTrappingEffect {
        boolean trapOpponent(Battle b, ActivePokemon escaper, ActivePokemon trapper);
        String opponentTrappingMessage(ActivePokemon escaper, ActivePokemon trapper);

        static boolean isTrapped(Battle b, ActivePokemon escaper, ActivePokemon trapper) {
            List<InvokeEffect> invokees = b.getEffectsList(trapper);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof OpponentTrappingEffect && invokee.isActiveEffect()) {
                    OpponentTrappingEffect effect = (OpponentTrappingEffect)invokee;
                    if (effect.trapOpponent(b, escaper, trapper)) {
                        Messages.add(effect.opponentTrappingMessage(escaper, trapper));
                        return true;
                    }
                }
            }

            return false;
        }
    }

    // Used for effects that check if the Pokemon can attack during the time of execution
    public interface BeforeAttackPreventingEffect {
        boolean canAttack(Battle b, ActivePokemon attacking, ActivePokemon defending);

        static boolean checkCannotAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            if (attacking.isFainted(b)) {
                return false;
            }

            if (defending.isFainted(b)) {
                return false;
            }

            List<InvokeEffect> invokees = b.getEffectsList(attacking);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof BeforeAttackPreventingEffect && invokee.isActiveEffect()) {
                    BeforeAttackPreventingEffect effect = (BeforeAttackPreventingEffect)invokee;
                    if (!effect.canAttack(b, attacking, defending)) {
                        return true;
                    }

                    if (attacking.isFainted(b)) {
                        return false;
                    }

                    if (defending.isFainted(b)) {
                        return false;
                    }
                }
            }

            return false;
        }
    }

    // Used for effects that occur immediately after printing an attack is being used
    // (After success checks, but before accuracy checks)
    // Can also be used by attacks instead of overriding their beginAttack method if preferable after printing/other StartAttackEffects
    public interface StartAttackEffect {
        void beforeAttack(Battle b, ActivePokemon attacking, ActivePokemon defending);

        static void checkBeforeAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // Adds attack after so it can be last to be invoked
            List<InvokeEffect> invokees = b.getEffectsList(attacking);
            invokees.add(attacking.getAttack());
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof StartAttackEffect && invokee.isActiveEffect()) {
                    StartAttackEffect effect = (StartAttackEffect)invokee;
                    effect.beforeAttack(b, attacking, defending);
                }
            }
        }
    }

    public interface UserSwapperEffect {
        boolean swapUser(Battle b, ActivePokemon user, ActivePokemon opponent);

        static boolean checkSwapUser(Battle b, ActivePokemon user, ActivePokemon opponent) {
            List<InvokeEffect> invokees = b.getEffectsList(opponent);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof UserSwapperEffect && invokee.isActiveEffect()) {

                    // If this is an ability that is being affected by mold breaker, we don't want to do anything with it
                    if (invokee instanceof Ability && !((Ability)invokee).unbreakableMold() && user.breaksTheMold()) {
                        continue;
                    }

                    UserSwapperEffect effect = (UserSwapperEffect)invokee;
                    if (effect.swapUser(b, user, opponent)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public interface TargetSwapperEffect {
        boolean swapTarget(Battle b, ActivePokemon user, ActivePokemon opponent);

        static boolean checkSwapTarget(Battle b, ActivePokemon user, ActivePokemon opponent) {
            List<InvokeEffect> invokees = b.getEffectsList(opponent);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof TargetSwapperEffect && invokee.isActiveEffect()) {

                    // If this is an ability that is being affected by mold breaker, we don't want to do anything with it
                    if (invokee instanceof Ability && !((Ability)invokee).unbreakableMold() && user.breaksTheMold()) {
                        continue;
                    }

                    TargetSwapperEffect effect = (TargetSwapperEffect)invokee;
                    if (effect.swapTarget(b, user, opponent)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public interface CritBlockerEffect {

        static boolean checkBlocked(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            List<InvokeEffect> invokees = b.getEffectsList(defending, attacking.getAttack());
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof CritBlockerEffect && invokee.isActiveEffect()) {

                    // If this is an ability that is being affected by mold breaker, we don't want to do anything with it
                    if (invokee instanceof Ability && !((Ability)invokee).unbreakableMold() && attacking.breaksTheMold()) {
                        continue;
                    }

                    return true;
                }
            }

            return false;
        }
    }

    public interface StatProtectingEffect {
        boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat);
        String preventionMessage(ActivePokemon p, Stat s);

        static StatProtectingEffect getPreventEffect(ActivePokemon moldBreaker, Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
            List<InvokeEffect> invokees = b.getEffectsList(victim);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof StatProtectingEffect && invokee.isActiveEffect()) {

                    // If this is an ability that is being affected by mold breaker, we don't want to do anything with it
                    if (invokee instanceof Ability && !((Ability)invokee).unbreakableMold() && moldBreaker != null && moldBreaker.breaksTheMold()) {
                        continue;
                    }

                    StatProtectingEffect effect = (StatProtectingEffect)invokee;
                    if (effect.prevent(b, caster, victim, stat)) {
                        return effect;
                    }
                }
            }

            return null;
        }
    }

    public interface StatTargetSwapperEffect {
        String getSwapStatTargetMessage(ActivePokemon victim);

        default boolean shouldSwapTarget() {
            // TODO: This should probably use a ContainsGet type of thing but just doing this instead for now
            return true;
        }

        static StatTargetSwapperEffect checkTargetSwap(ActivePokemon moldBreaker, Battle b, ActivePokemon caster, ActivePokemon victim) {
            List<InvokeEffect> invokees = b.getEffectsList(victim);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof StatTargetSwapperEffect && invokee.isActiveEffect()) {

                    // If this is an ability that is being affected by mold breaker, we don't want to do anything with it
                    if (invokee instanceof Ability && !((Ability)invokee).unbreakableMold() && moldBreaker != null && moldBreaker.breaksTheMold()) {
                        continue;
                    }

                    StatTargetSwapperEffect effect = (StatTargetSwapperEffect)invokee;
                    if (effect.shouldSwapTarget()) {
                        return effect;
                    }
                }
            }

            return null;
        }
    }

    public interface StatusPreventionEffect {
        ApplyResult preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusNamesies status);

        static ApplyResult getPreventEffect(Battle b, ActivePokemon caster, ActivePokemon victim, StatusNamesies status) {
            List<InvokeEffect> invokees = b.getEffectsList(victim);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof StatusPreventionEffect && invokee.isActiveEffect()) {

                    // If this is an ability that is being affected by mold breaker, we don't want to do anything with it
                    if (invokee instanceof Ability && !((Ability)invokee).unbreakableMold() && caster.breaksTheMold()) {
                        continue;
                    }

                    StatusPreventionEffect effect = (StatusPreventionEffect)invokee;
                    ApplyResult value = effect.preventStatus(b, caster, victim, status);
                    if (value != null) {
                        return value;
                    }
                }
            }

            return ApplyResult.success();
        }
    }

    public interface EffectPreventionEffect {
        ApplyResult preventEffect(Battle b, ActivePokemon caster, ActivePokemon victim, EffectNamesies effectName, CastSource source);

        static ApplyResult getPreventEffect(Battle b, ActivePokemon caster, ActivePokemon victim, EffectNamesies effectName, CastSource source) {
            List<InvokeEffect> invokees = b.getEffectsList(victim);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof EffectPreventionEffect && invokee.isActiveEffect()) {

                    // If this is an ability that is being affected by mold breaker, we don't want to do anything with it
                    if (invokee instanceof Ability && !((Ability)invokee).unbreakableMold() && caster.breaksTheMold()) {
                        continue;
                    }

                    EffectPreventionEffect effect = (EffectPreventionEffect)invokee;
                    ApplyResult value = effect.preventEffect(b, caster, victim, effectName, source);
                    if (value != null) {
                        return value;
                    }
                }
            }

            return ApplyResult.success();
        }
    }

    public interface BracingEffect {
        boolean isBracing(Battle b, ActivePokemon bracer, boolean fullHealth);
        String braceMessage(ActivePokemon bracer);

        static BracingEffect getBracingEffect(Battle b, ActivePokemon bracer, boolean fullHealth) {
            List<InvokeEffect> invokees = b.getEffectsList(bracer);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof BracingEffect && invokee.isActiveEffect()) {

                    // If this is an ability that is being affected by mold breaker, we don't want to do anything with it
                    if (invokee instanceof Ability && !((Ability)invokee).unbreakableMold() && b.getOtherPokemon(bracer).breaksTheMold()) {
                        continue;
                    }

                    BracingEffect effect = (BracingEffect)invokee;
                    if (effect.isBracing(b, bracer, fullHealth)) {
                        return effect;
                    }
                }
            }

            return null;
        }
    }

    public interface IgnoreStageEffect {
        boolean ignoreStage(Stat s, int stage);

        static boolean checkIgnoreStage(Battle b, ActivePokemon stagePokemon, Stat s, int stage) {
            List<InvokeEffect> invokees = b.getEffectsList(stagePokemon);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof IgnoreStageEffect && invokee.isActiveEffect()) {
                    IgnoreStageEffect effect = (IgnoreStageEffect)invokee;
                    if (effect.ignoreStage(s, stage)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public interface OpponentIgnoreStageEffect {
        boolean ignoreStage(Stat s);

        static boolean checkIgnoreStage(Battle b, ActivePokemon stagePokemon, ActivePokemon other, Stat s) {
            // Only add the attack when checking a defensive stat -- this means the other pokemon is the one currently attacking
            List<InvokeEffect> invokees = b.getEffectsList(other);
            if (s.isDefending()) {
                invokees.add(other.getAttack());
            }

            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof OpponentIgnoreStageEffect && invokee.isActiveEffect()) {

                    // If this is an ability that is being affected by mold breaker, we don't want to do anything with it
                    if (invokee instanceof Ability && !((Ability)invokee).unbreakableMold() && stagePokemon.breaksTheMold()) {
                        continue;
                    }

                    OpponentIgnoreStageEffect effect = (OpponentIgnoreStageEffect)invokee;
                    if (effect.ignoreStage(s)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public interface ChangeTypeEffect {

        // Guarantee the change-type effect to be first
        PokeType getType(Battle b, ActivePokemon p, boolean display);

        static PokeType getChangedType(Battle b, ActivePokemon p, boolean display) {
            List<InvokeEffect> invokees = b.getEffectsList(p, p.getEffect(PokemonEffectNamesies.CHANGE_TYPE));
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof ChangeTypeEffect && invokee.isActiveEffect()) {
                    ChangeTypeEffect effect = (ChangeTypeEffect)invokee;
                    PokeType value = effect.getType(b, p, display);
                    if (value != null) {
                        return value;
                    }
                }
            }

            return null;
        }
    }

    public interface ForceMoveEffect {
        Move getForcedMove(ActivePokemon attacking);

        static Move getForcedMove(Battle b, ActivePokemon attacking) {
            List<InvokeEffect> invokees = b.getEffectsList(attacking, attacking.getAttack());
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof ForceMoveEffect && invokee.isActiveEffect()) {
                    ForceMoveEffect effect = (ForceMoveEffect)invokee;
                    Move value = effect.getForcedMove(attacking);
                    if (value != null) {
                        return value;
                    }
                }
            }

            return null;
        }
    }

    public interface DifferentStatEffect {
        Integer getStat(ActivePokemon user, Stat stat);

        static Integer getStat(Battle b, ActivePokemon user, Stat stat) {
            List<InvokeEffect> invokees = b.getEffectsList(user);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof DifferentStatEffect && invokee.isActiveEffect()) {
                    DifferentStatEffect effect = (DifferentStatEffect)invokee;
                    Integer value = effect.getStat(user, stat);
                    if (value != null) {
                        return value;
                    }
                }
            }

            return null;
        }
    }

    public interface CritStageEffect {

        default int increaseCritStage(ActivePokemon p) {
            return 1;
        }

        static int getModifier(Battle b, ActivePokemon p) {
            int modifier = 0;

            List<InvokeEffect> invokees = b.getEffectsList(p, p.getAttack());
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof CritStageEffect && invokee.isActiveEffect()) {
                    CritStageEffect effect = (CritStageEffect)invokee;
                    modifier += effect.increaseCritStage(p);
                }
            }

            return modifier;
        }
    }

    public interface PriorityChangeEffect {
        int changePriority(Battle b, ActivePokemon user);

        static int getModifier(Battle b, ActivePokemon user) {
            int modifier = 0;

            List<InvokeEffect> invokees = b.getEffectsList(user);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof PriorityChangeEffect && invokee.isActiveEffect()) {
                    PriorityChangeEffect effect = (PriorityChangeEffect)invokee;
                    modifier += effect.changePriority(b, user);
                }
            }

            return modifier;
        }
    }

    public interface ChangeAttackTypeEffect {

        // Guarantee the change-attack-type effect to be first
        Type changeAttackType(Attack attack, Type original);

        static Type getAttackType(Battle b, ActivePokemon attacking, Attack attack, Type original) {
            List<InvokeEffect> invokees = b.getEffectsList(attacking, attacking.getEffect(PokemonEffectNamesies.CHANGE_ATTACK_TYPE));
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof ChangeAttackTypeEffect && invokee.isActiveEffect()) {
                    ChangeAttackTypeEffect effect = (ChangeAttackTypeEffect)invokee;
                    Type value = effect.changeAttackType(attack, original);
                    if (value != null) {
                        return value;
                    }
                }
            }

            return null;
        }
    }

    public interface AttackingNoAdvantageChanger {
        boolean negateNoAdvantage(Type attackingType, Type defendingType);

        static boolean checkAttackingNoAdvantageChanger(Battle b, ActivePokemon attacking, Type attackingType, Type defendingType) {
            List<InvokeEffect> invokees = b.getEffectsList(attacking);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof AttackingNoAdvantageChanger && invokee.isActiveEffect()) {
                    AttackingNoAdvantageChanger effect = (AttackingNoAdvantageChanger)invokee;
                    if (effect.negateNoAdvantage(attackingType, defendingType)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public interface DefendingNoAdvantageChanger {
        boolean negateNoAdvantage(Type attackingType, Type defendingType);

        static boolean checkDefendingNoAdvantageChanger(Battle b, ActivePokemon defending, Type attackingType, Type defendingType) {
            List<InvokeEffect> invokees = b.getEffectsList(defending);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof DefendingNoAdvantageChanger && invokee.isActiveEffect()) {
                    DefendingNoAdvantageChanger effect = (DefendingNoAdvantageChanger)invokee;
                    if (effect.negateNoAdvantage(attackingType, defendingType)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public interface ChangeMoveListEffect {
        MoveList getMoveList(MoveList actualMoves);

        static MoveList getMoveList(Battle b, ActivePokemon p, MoveList actualMoves) {
            List<InvokeEffect> invokees = b.getEffectsList(p);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof ChangeMoveListEffect && invokee.isActiveEffect()) {
                    ChangeMoveListEffect effect = (ChangeMoveListEffect)invokee;
                    MoveList value = effect.getMoveList(actualMoves);
                    if (value != null) {
                        return value;
                    }
                }
            }

            return null;
        }
    }

    public interface StatSwitchingEffect {
        Stat getSwitchStat(Stat s);

        static Stat switchStat(Battle b, ActivePokemon statPokemon, Stat s) {
            List<InvokeEffect> invokees = b.getEffectsList(statPokemon, statPokemon.getAttack());
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof StatSwitchingEffect && invokee.isActiveEffect()) {
                    StatSwitchingEffect effect = (StatSwitchingEffect)invokee;
                    s = effect.getSwitchStat(s);
                }
            }

            return s;
        }
    }

    public interface OpponentStatSwitchingEffect {
        Stat getSwitchStat(Stat s);

        static Stat switchStat(Battle b, ActivePokemon other, Stat s) {
            // Only add the attack when checking a defensive stat -- this means the other pokemon is the one currently attacking
            List<InvokeEffect> invokees = b.getEffectsList(other);
            if (s.isDefending()) {
                invokees.add(other.getAttack());
            }

            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof OpponentStatSwitchingEffect && invokee.isActiveEffect()) {
                    OpponentStatSwitchingEffect effect = (OpponentStatSwitchingEffect)invokee;
                    s = effect.getSwitchStat(s);
                }
            }

            return s;
        }
    }

    public interface HalfWeightEffect {

        default int getHalfAmount() {
            return 1;
        }

        static int getHalfAmount(Battle b, ActivePokemon anorexic) {
            int modifier = 0;

            List<InvokeEffect> invokees = b.getEffectsList(anorexic);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof HalfWeightEffect && invokee.isActiveEffect()) {

                    // If this is an ability that is being affected by mold breaker, we don't want to do anything with it
                    if (invokee instanceof Ability && !((Ability)invokee).unbreakableMold() && b.getOtherPokemon(anorexic).breaksTheMold()) {
                        continue;
                    }

                    HalfWeightEffect effect = (HalfWeightEffect)invokee;
                    modifier += effect.getHalfAmount();
                }
            }

            return modifier;
        }
    }

    public interface DoubleWeightEffect {

        default int getDoubleAmount() {
            return 1;
        }

        static int getDoubleAmount(Battle b, ActivePokemon fatty) {
            int modifier = 0;

            List<InvokeEffect> invokees = b.getEffectsList(fatty);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof DoubleWeightEffect && invokee.isActiveEffect()) {

                    // If this is an ability that is being affected by mold breaker, we don't want to do anything with it
                    if (invokee instanceof Ability && !((Ability)invokee).unbreakableMold() && b.getOtherPokemon(fatty).breaksTheMold()) {
                        continue;
                    }

                    DoubleWeightEffect effect = (DoubleWeightEffect)invokee;
                    modifier += effect.getDoubleAmount();
                }
            }

            return modifier;
        }
    }

    public interface StageChangingEffect {
        int adjustStage(Battle b, ActivePokemon p, Stat s);

        default int fullAdjustStage(Battle b, ActivePokemon p, ActivePokemon opp, Stat s) {
            int adjustment = this.adjustStage(b, p, s);
            return getStageWithCritCheck(adjustment, p, opp, s);
        }

        static int getModifier(Battle b, ActivePokemon p, ActivePokemon opp, Stat s) {
            int modifier = 0;

            ActivePokemon moldBreaker = s.isDefending() ? opp : null;

            List<InvokeEffect> invokees = b.getEffectsList(p);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof StageChangingEffect && invokee.isActiveEffect()) {

                    // If this is an ability that is being affected by mold breaker, we don't want to do anything with it
                    if (invokee instanceof Ability && !((Ability)invokee).unbreakableMold() && moldBreaker != null && moldBreaker.breaksTheMold()) {
                        continue;
                    }

                    StageChangingEffect effect = (StageChangingEffect)invokee;
                    modifier += effect.fullAdjustStage(b, p, opp, s);
                }
            }

            return modifier;
        }

        // Critical hits ignore negative stages on the attacker and positive stages on the defending
        static int getStageWithCritCheck(int stage, ActivePokemon p, ActivePokemon opp, Stat s) {
            if (s.isAttacking() && p.isCriticalHit() && stage < 0) {
                return 0;
            } else if (s.isDefending() && opp.isCriticalHit() && stage > 0) {
                return 0;
            } else {
                return stage;
            }
        }
    }

    public interface StatModifyingEffect {
        double modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s);

        static double getModifier(Battle b, ActivePokemon p, ActivePokemon opp, Stat s) {
            double modifier = 1;

            ActivePokemon moldBreaker = s.isDefending() ? opp : null;

            List<InvokeEffect> invokees = b.getEffectsList(p);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof StatModifyingEffect && invokee.isActiveEffect()) {

                    // If this is an ability that is being affected by mold breaker, we don't want to do anything with it
                    if (invokee instanceof Ability && !((Ability)invokee).unbreakableMold() && moldBreaker != null && moldBreaker.breaksTheMold()) {
                        continue;
                    }

                    StatModifyingEffect effect = (StatModifyingEffect)invokee;
                    modifier *= effect.modify(b, p, opp, s);
                }
            }

            return modifier;
        }
    }

    public interface StatChangingEffect {

        // p: The Pokemon that the stat is being altered on
        // opp: The opposing Pokemon
        // s: The stat that is being altered
        // stat: The current value of stat s
        // Return: The modified value of stat, if stat was not altered, just return stat
        int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat);

        static int modifyStat(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
            ActivePokemon moldBreaker = s.isDefending() ? opp : null;

            List<InvokeEffect> invokees = b.getEffectsList(p);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof StatChangingEffect && invokee.isActiveEffect()) {

                    // If this is an ability that is being affected by mold breaker, we don't want to do anything with it
                    if (invokee instanceof Ability && !((Ability)invokee).unbreakableMold() && moldBreaker != null && moldBreaker.breaksTheMold()) {
                        continue;
                    }

                    StatChangingEffect effect = (StatChangingEffect)invokee;
                    stat = effect.modify(b, p, opp, s, stat);
                }
            }

            return stat;
        }
    }

    public interface EffectChanceMultiplierEffect {
        double getEffectChanceMultiplier(ActivePokemon user);

        static double getModifier(Battle b, ActivePokemon user) {
            double modifier = 1;

            List<InvokeEffect> invokees = b.getEffectsList(user);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof EffectChanceMultiplierEffect && invokee.isActiveEffect()) {
                    EffectChanceMultiplierEffect effect = (EffectChanceMultiplierEffect)invokee;
                    modifier *= effect.getEffectChanceMultiplier(user);
                }
            }

            return modifier;
        }
    }

    public interface PowerChangeEffect {
        double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim);

        static double getModifier(Battle b, ActivePokemon user, ActivePokemon victim) {
            double modifier = 1;

            List<InvokeEffect> invokees = b.getEffectsList(user, user.getAttack());
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof PowerChangeEffect && invokee.isActiveEffect()) {
                    PowerChangeEffect effect = (PowerChangeEffect)invokee;
                    modifier *= effect.getMultiplier(b, user, victim);
                }
            }

            return modifier;
        }
    }

    public interface OpponentPowerChangeEffect {
        double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim);

        static double getModifier(Battle b, ActivePokemon user, ActivePokemon victim) {
            double modifier = 1;

            List<InvokeEffect> invokees = b.getEffectsList(victim);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof OpponentPowerChangeEffect && invokee.isActiveEffect()) {

                    // If this is an ability that is being affected by mold breaker, we don't want to do anything with it
                    if (invokee instanceof Ability && !((Ability)invokee).unbreakableMold() && user.breaksTheMold()) {
                        continue;
                    }

                    OpponentPowerChangeEffect effect = (OpponentPowerChangeEffect)invokee;
                    modifier *= effect.getOpponentMultiplier(b, user, victim);
                }
            }

            return modifier;
        }
    }

    public interface AdvantageMultiplierMove extends AttackInterface {
        double multiplyAdvantage(Type attackingType, PokeType defendingTypes);

        static double getModifier(ActivePokemon attacking, Type attackingType, PokeType defendingTypes) {
            double modifier = 1;

            List<InvokeEffect> invokees = Collections.singletonList(attacking.getAttack());
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof AdvantageMultiplierMove && invokee.isActiveEffect()) {
                    AdvantageMultiplierMove effect = (AdvantageMultiplierMove)invokee;
                    modifier *= effect.multiplyAdvantage(attackingType, defendingTypes);
                }
            }

            return modifier;
        }
    }

    public interface StatusBoosterEffect {
        boolean statusBooster(Stat stat);

        static boolean isStatusBooster(Battle b, ActivePokemon user, Stat stat) {
            List<InvokeEffect> invokees = b.getEffectsList(user, user.getAttack());
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof StatusBoosterEffect && invokee.isActiveEffect()) {
                    StatusBoosterEffect effect = (StatusBoosterEffect)invokee;
                    if (effect.statusBooster(stat)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    // Effects which can absorb direct attacks
    public interface AbsorbDamageEffect {
        boolean absorbDamage(Battle b, ActivePokemon damageTaker, int damageAmount);

        static boolean checkAbsorbDamageEffect(Battle b, ActivePokemon damageTaker, int damageAmount) {
            List<InvokeEffect> invokees = b.getEffectsList(damageTaker);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof AbsorbDamageEffect && invokee.isActiveEffect()) {

                    // If this is an ability that is being affected by mold breaker, we don't want to do anything with it
                    if (invokee instanceof Ability && !((Ability)invokee).unbreakableMold() && b.getOtherPokemon(damageTaker).breaksTheMold()) {
                        continue;
                    }

                    AbsorbDamageEffect effect = (AbsorbDamageEffect)invokee;
                    if (effect.absorbDamage(b, damageTaker, damageAmount)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public interface DamageTakenEffect {
        void damageTaken(Battle b, ActivePokemon damageTaker, int damageAmount);

        static void invokeDamageTakenEffect(Battle b, ActivePokemon damageTaker, int damageAmount) {
            List<InvokeEffect> invokees = b.getEffectsList(damageTaker);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof DamageTakenEffect && invokee.isActiveEffect()) {
                    DamageTakenEffect effect = (DamageTakenEffect)invokee;
                    effect.damageTaken(b, damageTaker, damageAmount);
                }
            }
        }
    }

    public interface AlwaysCritEffect {

        default boolean shouldCrit(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            return true;
        }

        static boolean defCritsies(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            List<InvokeEffect> invokees = b.getEffectsList(attacking, attacking.getAttack());
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof AlwaysCritEffect && invokee.isActiveEffect()) {
                    AlwaysCritEffect effect = (AlwaysCritEffect)invokee;
                    if (effect.shouldCrit(b, attacking, defending)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public interface EffectReceivedEffect {
        void receiveEffect(Battle b, ActivePokemon caster, ActivePokemon victim, EffectNamesies effectType);

        private static void invokeEffectReceivedEffect(Battle b, ActivePokemon caster, ActivePokemon victim, EffectNamesies effectType) {
            List<InvokeEffect> invokees = b.getIndividualAndTeamEffects(victim);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof EffectReceivedEffect && invokee.isActiveEffect()) {
                    EffectReceivedEffect effect = (EffectReceivedEffect)invokee;
                    effect.receiveEffect(b, caster, victim, effectType);
                }
            }
        }

        // Calls the invoke method for both Pokemon if effect is a battle effect
        static void checkReceivedEffect(Battle b, ActivePokemon caster, ActivePokemon victim, EffectNamesies effectType) {
            if (effectType instanceof BattleEffectNamesies) {
                invokeEffectReceivedEffect(b, caster, b.getPlayer().front(), effectType);
                invokeEffectReceivedEffect(b, caster, b.getOpponent().front(), effectType);
            } else {
                invokeEffectReceivedEffect(b, caster, victim, effectType);
            }
        }
    }

    public interface StatusReceivedEffect {
        void receiveStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusNamesies statusType);

        static void invokeStatusReceivedEffect(Battle b, ActivePokemon caster, ActivePokemon victim, StatusNamesies statusType) {
            List<InvokeEffect> invokees = b.getEffectsList(victim);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof StatusReceivedEffect && invokee.isActiveEffect()) {
                    StatusReceivedEffect effect = (StatusReceivedEffect)invokee;
                    effect.receiveStatus(b, caster, victim, statusType);
                }
            }
        }
    }

    public interface OpponentStatusReceivedEffect {
        void receiveStatus(Battle b, ActivePokemon victim, StatusNamesies statusType);

        static void invokeOpponentStatusReceivedEffect(Battle b, ActivePokemon victim, StatusNamesies statusType) {
            List<InvokeEffect> invokees = b.getEffectsList(b.getOtherPokemon(victim));
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof OpponentStatusReceivedEffect && invokee.isActiveEffect()) {
                    OpponentStatusReceivedEffect effect = (OpponentStatusReceivedEffect)invokee;
                    effect.receiveStatus(b, victim, statusType);
                }
            }
        }
    }

    public interface SleepyFightsterEffect {

        static boolean containsSleepyFightsterEffect(Battle b, ActivePokemon p) {
            List<InvokeEffect> invokees = b.getEffectsList(p, p.getAttack());
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof SleepyFightsterEffect && invokee.isActiveEffect()) {
                    return true;
                }
            }

            return false;
        }
    }

    public interface EndAttackEffect {
        void endsies(Battle b, ActivePokemon attacking);

        static void invokeEndAttackEffect(Battle b, ActivePokemon attacking) {
            List<InvokeEffect> invokees = b.getEffectsList(attacking);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof EndAttackEffect && invokee.isActiveEffect()) {
                    EndAttackEffect effect = (EndAttackEffect)invokee;
                    effect.endsies(b, attacking);
                }
            }
        }
    }

    public interface OpponentEndAttackEffect {
        void endsies(Battle b, ActivePokemon attacking);

        static void invokeOpponentEndAttackEffect(Battle b, ActivePokemon attacking) {
            List<InvokeEffect> invokees = b.getEffectsList(b.getOtherPokemon(attacking));
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof OpponentEndAttackEffect && invokee.isActiveEffect()) {
                    OpponentEndAttackEffect effect = (OpponentEndAttackEffect)invokee;
                    effect.endsies(b, attacking);
                }
            }
        }
    }

    public interface TerrainCastEffect {
        void newTerrain(Battle b, ActivePokemon p, TerrainType newTerrain);

        static void invokeTerrainCastEffect(Battle b, ActivePokemon p, TerrainType newTerrain) {
            List<InvokeEffect> invokees = b.getIndividualAndTeamEffects(p);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof TerrainCastEffect && invokee.isActiveEffect()) {
                    TerrainCastEffect effect = (TerrainCastEffect)invokee;
                    effect.newTerrain(b, p, newTerrain);
                }
            }
        }
    }

    public interface AttackBlocker {
        boolean block(Battle b, ActivePokemon user, ActivePokemon victim);
        default void alternateEffect(Battle b, ActivePokemon user, ActivePokemon victim) {}

        default String getBlockMessage(ActivePokemon user, ActivePokemon victim) {
            return Effect.DEFAULT_FAIL_MESSAGE;
        }

        static AttackBlocker checkBlocked(Battle b, ActivePokemon user, ActivePokemon victim) {
            List<InvokeEffect> invokees = b.getEffectsList(victim);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof AttackBlocker && invokee.isActiveEffect()) {

                    // If this is an ability that is being affected by mold breaker, we don't want to do anything with it
                    if (invokee instanceof Ability && !((Ability)invokee).unbreakableMold() && user.breaksTheMold()) {
                        continue;
                    }

                    AttackBlocker effect = (AttackBlocker)invokee;
                    if (effect.block(b, user, victim)) {
                        return effect;
                    }
                }
            }

            return null;
        }
    }

    public interface SelfAttackBlocker {
        boolean block(Battle b, ActivePokemon user);
        default void alternateEffect(Battle b, ActivePokemon user) {}

        default String getBlockMessage(ActivePokemon user) {
            return Effect.DEFAULT_FAIL_MESSAGE;
        }

        static SelfAttackBlocker checkBlocked(Battle b, ActivePokemon user) {
            List<InvokeEffect> invokees = b.getEffectsList(user, user.getAttack());
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof SelfAttackBlocker && invokee.isActiveEffect()) {
                    SelfAttackBlocker effect = (SelfAttackBlocker)invokee;
                    if (effect.block(b, user)) {
                        return effect;
                    }
                }
            }

            return null;
        }
    }

    public interface WildEncounterAlterer {
        void alterWildPokemon(ActivePokemon playerFront, WildEncounterInfo encounterData, WildEncounter encounter);

        static void invokeWildEncounterAlterer(ActivePokemon playerFront, WildEncounterInfo encounterData, WildEncounter encounter) {
            List<InvokeEffect> invokees = new ArrayList<>();
            invokees.add(playerFront.getAbility());
            invokees.add(playerFront.getActualHeldItem());

            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof WildEncounterAlterer && invokee.isActiveEffect()) {
                    WildEncounterAlterer effect = (WildEncounterAlterer)invokee;
                    effect.alterWildPokemon(playerFront, encounterData, encounter);
                }
            }
        }
    }

    public interface RepellingEffect {
        boolean shouldRepel(ActivePokemon playerFront, WildEncounter wildPokemon);

        static boolean checkRepellingEffect(ActivePokemon playerFront, WildEncounter wildPokemon) {
            if (wildPokemon.getLevel() <= playerFront.getLevel() && Game.getPlayer().getRepelInfo().isUsingRepel()) {
                return true;
            }

            List<InvokeEffect> invokees = new ArrayList<>();
            invokees.add(playerFront.getAbility());
            invokees.add(playerFront.getActualHeldItem());

            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof RepellingEffect && invokee.isActiveEffect()) {
                    RepellingEffect effect = (RepellingEffect)invokee;
                    if (effect.shouldRepel(playerFront, wildPokemon)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public interface WildEncounterSelector {
        WildEncounterInfo getWildEncounter(WildEncounterInfo[] wildEncounters);

        static WildEncounterInfo getForcedWildEncounter(ActivePokemon playerFront, WildEncounterInfo[] wildEncounters) {
            List<InvokeEffect> invokees = new ArrayList<>();
            invokees.add(playerFront.getAbility());
            invokees.add(playerFront.getActualHeldItem());

            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof WildEncounterSelector && invokee.isActiveEffect()) {
                    WildEncounterSelector effect = (WildEncounterSelector)invokee;
                    WildEncounterInfo value = effect.getWildEncounter(wildEncounters);
                    if (value != null) {
                        return value;
                    }
                }
            }

            return null;
        }
    }

    public interface EncounterRateMultiplier {
        double getEncounterRateMultiplier();

        static double getModifier(ActivePokemon playerFront) {
            double modifier = 1;

            List<InvokeEffect> invokees = new ArrayList<>();
            invokees.add(playerFront.getAbility());
            invokees.add(playerFront.getActualHeldItem());

            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof EncounterRateMultiplier && invokee.isActiveEffect()) {
                    EncounterRateMultiplier effect = (EncounterRateMultiplier)invokee;
                    modifier *= effect.getEncounterRateMultiplier();
                }
            }

            return modifier;
        }
    }

    public interface ModifyStageValueEffect {
        int modifyStageValue();

        static double getModifier(Battle b, ActivePokemon moldBreaker, ActivePokemon victim) {
            double modifier = 1;

            List<InvokeEffect> invokees = b.getEffectsList(victim);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof ModifyStageValueEffect && invokee.isActiveEffect()) {

                    // If this is an ability that is being affected by mold breaker, we don't want to do anything with it
                    if (invokee instanceof Ability && !((Ability)invokee).unbreakableMold() && moldBreaker != null && moldBreaker.breaksTheMold()) {
                        continue;
                    }

                    ModifyStageValueEffect effect = (ModifyStageValueEffect)invokee;
                    modifier *= effect.modifyStageValue();
                }
            }

            return modifier;
        }
    }

    public interface StrikeFirstEffect {

        // Returns if the Pokemon should go first within its priority bracket
        boolean strikeFirst(Battle b, ActivePokemon striker);
        String getStrikeFirstMessage(ActivePokemon striker);

        static boolean checkStrikeFirst(Battle b, ActivePokemon striker) {
            List<InvokeEffect> invokees = b.getEffectsList(striker);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof StrikeFirstEffect && invokee.isActiveEffect()) {
                    StrikeFirstEffect effect = (StrikeFirstEffect)invokee;
                    if (effect.strikeFirst(b, striker)) {
                        Messages.add(effect.getStrikeFirstMessage(striker));
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public interface WeatherEliminatingEffect extends EntryEffect {
        String getEliminateMessage(ActivePokemon eliminator);

        @Override
        default void enter(Battle b, ActivePokemon enterer) {
            Messages.add(this.getEliminateMessage(enterer));
        }

        private static boolean shouldEliminateWeather(Battle b, ActivePokemon eliminator) {
            List<InvokeEffect> invokees = b.getIndividualAndTeamEffects(eliminator);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof WeatherEliminatingEffect && invokee.isActiveEffect()) {
                    return true;
                }
            }

            return false;
        }

        // Calls the invoke method for both front Pokemon
        static boolean shouldEliminateWeather(Battle b) {
            return shouldEliminateWeather(b, b.getPlayer().front()) || shouldEliminateWeather(b, b.getOpponent().front());
        }
    }

    // Effects that take place when weather is changed
    public interface WeatherChangedEffect {

        // Note: The effect holder here is not necessarily the Pokemon that changed the weather, but the Pokemon which holds the WeatherChangedEffect
        void weatherChanged(Battle b, ActivePokemon effectHolder);

        private static void checkWeatherChange(Battle b, ActivePokemon effectHolder) {
            List<InvokeEffect> invokees = b.getIndividualAndTeamEffects(effectHolder);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof WeatherChangedEffect && invokee.isActiveEffect()) {
                    WeatherChangedEffect effect = (WeatherChangedEffect)invokee;
                    effect.weatherChanged(b, effectHolder);
                }
            }
        }

        // Calls the invoke method for both front Pokemon
        static void invokeWeatherChangedEffect(Battle b) {
            // Check the effects from both Pokemon on the field
            // Note: Should not be implemented by any battle effects as they will be executed twice
            checkWeatherChange(b, b.getPlayer().front());
            checkWeatherChange(b, b.getOpponent().front());
        }
    }

    public interface SwitchOutEffect {
        void switchOut(ActivePokemon switchee);

        static void invokeSwitchOutEffect(ActivePokemon switchee) {
            List<InvokeEffect> invokees = switchee.getAllEffects();
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof SwitchOutEffect && invokee.isActiveEffect()) {
                    SwitchOutEffect effect = (SwitchOutEffect)invokee;
                    effect.switchOut(switchee);
                }
            }
        }
    }

    public interface EffectExtendingEffect {
        int getExtensionTurns(Effect receivedEffect, int numTurns);

        static int getModifier(Battle b, ActivePokemon p, Effect receivedEffect, int numTurns) {
            int modifier = 0;

            List<InvokeEffect> invokees = b.getEffectsList(p);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof EffectExtendingEffect && invokee.isActiveEffect()) {
                    EffectExtendingEffect effect = (EffectExtendingEffect)invokee;
                    modifier += effect.getExtensionTurns(receivedEffect, numTurns);
                }
            }

            return modifier;
        }
    }

    public interface ItemBlockerEffect {

        default boolean blockItem(Battle b, ActivePokemon p) {
            return true;
        }

        private static boolean checkItemBlockerEffect(Battle b, ActivePokemon p) {
            List<InvokeEffect> invokees = b.getEffectsList(p);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof ItemBlockerEffect && invokee.isActiveEffect()) {
                    ItemBlockerEffect effect = (ItemBlockerEffect)invokee;
                    if (effect.blockItem(b, p)) {
                        return true;
                    }
                }
            }

            return false;
        }

        static boolean shouldBlockItem(Battle b, ActivePokemon p) {
            if (p.isCheckingItemEffect()) {
                return false;
            }

            p.setCheckingItemEffect(true);
            boolean blockItem = checkItemBlockerEffect(b, p);
            p.setCheckingItemEffect(false);
            return blockItem;
        }
    }

    public interface OpponentItemBlockerEffect {
        boolean blockItem(ItemNamesies item);

        private static boolean checkOpponentItemBlockerEffect(Battle b, ActivePokemon opp, ItemNamesies item) {
            List<InvokeEffect> invokees = b.getEffectsList(opp);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof OpponentItemBlockerEffect && invokee.isActiveEffect()) {
                    OpponentItemBlockerEffect effect = (OpponentItemBlockerEffect)invokee;
                    if (effect.blockItem(item)) {
                        return true;
                    }
                }
            }

            return false;
        }

        static boolean shouldBlockItem(Battle b, ActivePokemon opp, ItemNamesies item) {
            if (opp.isCheckingItemEffect()) {
                return false;
            }

            opp.setCheckingItemEffect(true);
            boolean blockItem = checkOpponentItemBlockerEffect(b, opp, item);
            opp.setCheckingItemEffect(false);
            return blockItem;
        }
    }

    public interface StallingEffect {

        static boolean containsStallingEffect(Battle b, ActivePokemon p) {
            List<InvokeEffect> invokees = b.getEffectsList(p);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof StallingEffect && invokee.isActiveEffect()) {
                    return true;
                }
            }

            return false;
        }
    }

    public interface StickyHoldEffect {

        static boolean containsStickyHoldEffect(Battle b, ActivePokemon user, ActivePokemon stickyHands) {
            List<InvokeEffect> invokees = b.getEffectsList(stickyHands);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof StickyHoldEffect && invokee.isActiveEffect()) {

                    // If this is an ability that is being affected by mold breaker, we don't want to do anything with it
                    if (invokee instanceof Ability && !((Ability)invokee).unbreakableMold() && user.breaksTheMold()) {
                        continue;
                    }

                    return true;
                }
            }

            return false;
        }
    }

    public interface NoSwapEffect {

        static boolean containsNoSwapEffect(Battle b, ActivePokemon swapper, ActivePokemon swapped) {
            List<InvokeEffect> invokees = b.getEffectsList(swapped);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof NoSwapEffect && invokee.isActiveEffect()) {

                    // If this is an ability that is being affected by mold breaker, we don't want to do anything with it
                    if (invokee instanceof Ability && !((Ability)invokee).unbreakableMold() && swapper.breaksTheMold()) {
                        continue;
                    }

                    return true;
                }
            }

            return false;
        }
    }

    public interface DefiniteEscape extends InvokeEffect {

        default String getEscapeMessage(ActivePokemon sourcerer) {
            CastSource source = this.getSource().getCastSource();
            if (source.hasSourceName()) {
                return sourcerer.getName() + "'s " + source.getSourceName(sourcerer) + " allowed it to escape!";
            }

            return "Got away safely!";
        }

        default boolean canEscape() {
            return true;
        }

        static boolean canDefinitelyEscape(Battle b, ActivePokemon p) {
            List<InvokeEffect> invokees = b.getEffectsList(p);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof DefiniteEscape && invokee.isActiveEffect()) {
                    DefiniteEscape effect = (DefiniteEscape)invokee;
                    if (effect.canEscape()) {
                        Messages.add(effect.getEscapeMessage(p));
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public interface PowderBlocker extends AttackBlocker {

        @Override
        default boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getAttack() instanceof PowderMove;
        }

        static boolean containsPowderBlocker(Battle b, ActivePokemon p) {
            if (p.isType(b, Type.GRASS)) {
                return true;
            }

            List<InvokeEffect> invokees = b.getEffectsList(p);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof PowderBlocker && invokee.isActiveEffect()) {
                    return true;
                }
            }

            return false;
        }
    }
}
