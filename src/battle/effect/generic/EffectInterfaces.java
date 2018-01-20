package battle.effect.generic;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.Attack;
import battle.attack.Move;
import battle.attack.MoveType;
import battle.effect.status.StatusCondition;
import item.Item;
import item.ItemNamesies;
import main.Global;
import map.overworld.TerrainType;
import map.overworld.WildEncounter;
import map.overworld.WildEncounterInfo;
import message.MessageUpdate;
import message.Messages;
import pokemon.PokemonInfo;
import pokemon.Stat;
import pokemon.ability.Ability;
import pokemon.ability.AbilityNamesies;
import trainer.Trainer;
import type.Type;
import util.RandomUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EffectInterfaces {

    // Class to hold interfaces -- should not be instantiated
    private EffectInterfaces() {
        Global.error("EffectInterfaces class cannot be instantiated.");
    }

    // EVERYTHING BELOW IS GENERATED ###

    // This is used when the user applies direct damage to an opponent, and has special effects associated with the user
    public interface ApplyDamageEffect {

        // b: The current battle
        // user: The user of that attack, the one who is probably implementing this effect
        // victim: The Pokemon that received the attack
        // damage: The amount of damage that was dealt to victim by the user
        void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage);

        static void invokeApplyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
            if (user.isFainted(b)) {
                return;
            }

            List<Object> invokees = b.getEffectsList(user, user.getAttack());
            for (Object invokee : invokees) {
                if (invokee instanceof ApplyDamageEffect && Effect.isActiveEffect(invokee)) {

                    ApplyDamageEffect effect = (ApplyDamageEffect)invokee;
                    effect.applyDamageEffect(b, user, victim, damage);

                    if (user.isFainted(b)) {
                        return;
                    }
                }
            }
        }
    }

    // This is used when the user applies direct damage to an opponent, and has special effects associated with the user
    public interface OpponentApplyDamageEffect {

        // b: The current battle
        // user: The user of that attack
        // victim: The Pokemon that received the attack, the one who is probably implementing this effect
        // damage: The amount of damage that was dealt to victim by the user
        void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage);

        static void invokeOpponentApplyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
            if (user.isFainted(b)) {
                return;
            }

            List<Object> invokees = b.getEffectsList(victim);
            for (Object invokee : invokees) {
                if (invokee instanceof OpponentApplyDamageEffect && Effect.isActiveEffect(invokee)) {

                    OpponentApplyDamageEffect effect = (OpponentApplyDamageEffect)invokee;
                    effect.applyDamageEffect(b, user, victim, damage);

                    if (user.isFainted(b)) {
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

            // Weather is handled separately
            List<Object> invokees = b.getEffectsList(victim);
            invokees.remove(b.getWeather());

            for (Object invokee : invokees) {
                if (invokee instanceof EndTurnEffect && Effect.isActiveEffect(invokee)) {

                    EndTurnEffect effect = (EndTurnEffect)invokee;
                    effect.applyEndTurn(victim, b);

                    if (victim.isFainted(b)) {
                        return;
                    }
                }
            }
        }
    }

    public interface SuperDuperEndTurnEffect {
        boolean theVeryVeryEnd(Battle b, ActivePokemon p);

        static boolean checkSuperDuperEndTurnEffect(Battle b, ActivePokemon p) {
            List<Object> invokees = b.getEffectsList(p);
            for (Object invokee : invokees) {
                if (invokee instanceof SuperDuperEndTurnEffect && Effect.isActiveEffect(invokee)) {

                    SuperDuperEndTurnEffect effect = (SuperDuperEndTurnEffect)invokee;
                    if (effect.theVeryVeryEnd(b, p)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public interface RecoilMove extends ApplyDamageEffect {
        void applyRecoil(Battle b, ActivePokemon user, int damage);

        @Override
        default void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
            this.applyRecoil(b, user, damage);
        }
    }

    public interface RecoilPercentageMove extends RecoilMove {
        int getDamagePercentageDenominator();

        @Override
        default void applyRecoil(Battle b, ActivePokemon user, int damage) {
            if (user.hasAbility(AbilityNamesies.ROCK_HEAD) || user.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
                return;
            }

            Messages.add(user.getName() + " was hurt by recoil!");
            user.reduceHealth(b, (int)Math.ceil((double)damage/getDamagePercentageDenominator()), false);
        }
    }

    public interface SelfHealingMove {
        double getHealFraction(Battle b, ActivePokemon victim);

        default void heal(Battle b, ActivePokemon victim) {
            // Heal yourself!
            victim.healHealthFraction(this.getHealFraction(b, victim));

            // TODO: Make sure the message is set up correctly for the hp change
            Messages.add(new MessageUpdate(victim.getName() + "'s health was restored!").updatePokemon(b, victim));
        }
    }

    public interface PhysicalContactEffect extends OpponentApplyDamageEffect {

        // b: The current battle
        // user: The user of the attack that caused the physical contact
        // victim: The Pokemon that received the physical contact attack
        void contact(Battle b, ActivePokemon user, ActivePokemon victim);

        @Override
        default void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
            // Only apply if physical contact is made
            if (user.getAttack().isMoveType(MoveType.PHYSICAL_CONTACT) && !user.hasAbility(AbilityNamesies.LONG_REACH)) {
                this.contact(b, user, victim);
            }
        }
    }

    public interface TakeDamageEffect {

        // b: The current battle
        // user: The user of the attack
        // victim: The Pokemon who is taking damage, they are the one's probably implementing this
        void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim);

        static void invokeTakeDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (victim.isFainted(b)) {
                return;
            }

            List<Object> invokees = b.getEffectsList(victim);
            for (Object invokee : invokees) {
                if (invokee instanceof TakeDamageEffect && Effect.isActiveEffect(invokee)) {

                    TakeDamageEffect effect = (TakeDamageEffect)invokee;
                    effect.takeDamage(b, user, victim);

                    if (victim.isFainted(b)) {
                        return;
                    }
                }
            }
        }
    }

    public interface OpponentTakeDamageEffect {

        // b: The current battle
        // user: The user of the attack and implementer of the effect
        // victim: The Pokemon who is taking damage
        void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim);

        static void invokeOpponentTakeDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (victim.isFainted(b)) {
                return;
            }

            List<Object> invokees = b.getEffectsList(user);
            for (Object invokee : invokees) {
                if (invokee instanceof OpponentTakeDamageEffect && Effect.isActiveEffect(invokee)) {

                    OpponentTakeDamageEffect effect = (OpponentTakeDamageEffect)invokee;
                    effect.takeDamage(b, user, victim);

                    if (victim.isFainted(b)) {
                        return;
                    }
                }
            }
        }
    }

    public interface CrashDamageMove {
        void crash(Battle b, ActivePokemon user);

        static void invokeCrashDamageMove(Battle b, ActivePokemon user) {
            List<Object> invokees = Collections.singletonList(user.getAttack());
            for (Object invokee : invokees) {
                if (invokee instanceof CrashDamageMove && Effect.isActiveEffect(invokee)) {

                    CrashDamageMove effect = (CrashDamageMove)invokee;
                    effect.crash(b, user);
                }
            }
        }
    }

    public interface BarrierEffect extends DefogRelease {
        void breakBarrier(Battle b, ActivePokemon breaker);

        static void breakBarriers(Battle b, ActivePokemon breaker) {
            List<Object> invokees = b.getEffectsList(b.getOtherPokemon(breaker));
            for (Object invokee : invokees) {
                if (invokee instanceof BarrierEffect && Effect.isActiveEffect(invokee)) {

                    BarrierEffect effect = (BarrierEffect)invokee;
                    effect.breakBarrier(b, breaker);
                }
            }
        }
    }

    public interface DefogRelease {
        String getDefogReleaseMessage(ActivePokemon released);

        default void releaseDefog(Battle b, ActivePokemon released) {
            Messages.add(this.getDefogReleaseMessage(released));

            if (this instanceof PokemonEffect) {
                PokemonEffect effect = (PokemonEffect)this;
                released.removeEffect(effect);
            } else if (this instanceof TeamEffect) {
                TeamEffect effect = (TeamEffect)this;
                b.getTrainer(released).removeEffect(effect);
            } else {
                Global.error("Invalid defog release object " + this.getClass().getSimpleName());
            }
        }

        static void release(Battle b, ActivePokemon released) {
            List<Object> invokees = b.getEffectsList(released);
            for (Object invokee : invokees) {
                if (invokee instanceof DefogRelease && Effect.isActiveEffect(invokee)) {

                    DefogRelease effect = (DefogRelease)invokee;
                    effect.releaseDefog(b, released);
                }
            }
        }
    }

    public interface RapidSpinRelease {
        String getRapidSpinReleaseMessage(ActivePokemon releaser);

        default void releaseRapidSpin(Battle b, ActivePokemon releaser) {
            Messages.add(this.getRapidSpinReleaseMessage(releaser));

            if (this instanceof PokemonEffect) {
                PokemonEffect effect = (PokemonEffect)this;
                releaser.removeEffect(effect);
            } else if (this instanceof TeamEffect) {
                TeamEffect effect = (TeamEffect)this;
                b.getTrainer(releaser).removeEffect(effect);
            } else {
                Global.error("Invalid rapid spin release object " + this.getClass().getSimpleName());
            }
        }

        static void release(Battle b, ActivePokemon releaser) {
            List<Object> invokees = b.getEffectsList(releaser);
            for (Object invokee : invokees) {
                if (invokee instanceof RapidSpinRelease && Effect.isActiveEffect(invokee)) {

                    RapidSpinRelease effect = (RapidSpinRelease)invokee;
                    effect.releaseRapidSpin(b, releaser);
                }
            }
        }
    }

    public interface NameChanger {

        // TODO: Not a fan that this only operates on the ability but then again I'm not passing the battle in here and also fuck illusion srsly I might just special case it since it's so fucking unique
        String getNameChange();
        void setNameChange(Battle b, ActivePokemon victim);

        static String getChangedName(ActivePokemon p) {
            List<Object> invokees = Collections.singletonList(p.getAbility());

            for (Object invokee : invokees) {
                if (invokee instanceof NameChanger && Effect.isActiveEffect(invokee)) {

                    NameChanger effect = (NameChanger)invokee;
                    return effect.getNameChange();
                }
            }

            return null;
        }

        static void setNameChanges(Battle b, ActivePokemon victim) {
            List<Object> invokees = b.getEffectsList(victim);
            for (Object invokee : invokees) {
                if (invokee instanceof NameChanger && Effect.isActiveEffect(invokee)) {

                    NameChanger effect = (NameChanger)invokee;
                    effect.setNameChange(b, victim);
                }
            }
        }
    }

    public interface EntryEffect {
        void enter(Battle b, ActivePokemon enterer);

        static void invokeEntryEffect(Battle b, ActivePokemon enterer) {
            List<Object> invokees = b.getEffectsList(enterer);
            for (Object invokee : invokees) {
                if (invokee instanceof EntryEffect && Effect.isActiveEffect(invokee)) {

                    EntryEffect effect = (EntryEffect)invokee;
                    effect.enter(b, enterer);
                }
            }
        }
    }

    public interface StatLoweredEffect {

        // b: The current battle
        // caster: The Pokemon responsible for causing the stat to be lowered
        // victim: The Pokemon who's stat is being lowered
        void takeItToTheNextLevel(Battle b, ActivePokemon caster, ActivePokemon victim);

        static void invokeStatLoweredEffect(Battle b, ActivePokemon caster, ActivePokemon victim) {
            List<Object> invokees = b.getEffectsList(victim);
            for (Object invokee : invokees) {
                if (invokee instanceof StatLoweredEffect && Effect.isActiveEffect(invokee)) {

                    StatLoweredEffect effect = (StatLoweredEffect)invokee;
                    effect.takeItToTheNextLevel(b, caster, victim);
                }
            }
        }
    }

    public interface LevitationEffect {
        default void fall(Battle b, ActivePokemon fallen) {}

        static void falllllllll(Battle b, ActivePokemon fallen) {
            List<Object> invokees = b.getEffectsList(fallen);
            for (Object invokee : invokees) {
                if (invokee instanceof LevitationEffect && Effect.isActiveEffect(invokee)) {

                    LevitationEffect effect = (LevitationEffect)invokee;
                    effect.fall(b, fallen);
                }
            }
        }

        static boolean containsLevitationEffect(Battle b, ActivePokemon p, ActivePokemon moldBreaker) {
            List<Object> invokees = b.getEffectsList(p);
            for (Object invokee : invokees) {
                if (invokee instanceof LevitationEffect && Effect.isActiveEffect(invokee)) {

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

    // KILL KILL KILL MURDER MURDER MURDER
    public interface MurderEffect {
        void killWish(Battle b, ActivePokemon dead, ActivePokemon murderer);

        static void killKillKillMurderMurderMurder(Battle b, ActivePokemon dead, ActivePokemon murderer) {
            List<Object> invokees = b.getEffectsList(murderer, murderer.getAttack());
            for (Object invokee : invokees) {
                if (invokee instanceof MurderEffect && Effect.isActiveEffect(invokee)) {

                    MurderEffect effect = (MurderEffect)invokee;
                    effect.killWish(b, dead, murderer);
                }
            }
        }
    }

    public interface EndBattleEffect {
        void afterBattle(Trainer player, Battle b, ActivePokemon p);

        static void invokeEndBattleEffect(List<?> invokees, Trainer player, Battle b, ActivePokemon p) {
            for (Object invokee : invokees) {
                if (invokee instanceof EndBattleEffect && Effect.isActiveEffect(invokee)) {

                    EndBattleEffect effect = (EndBattleEffect)invokee;
                    effect.afterBattle(player, b, p);
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
                p.getMove().switchReady(b, p);
                Messages.add(p.getName() + " fell to the ground!");
            }

            LevitationEffect.falllllllll(b, p);
        }

        static boolean containsGroundedEffect(Battle b, ActivePokemon p) {
            List<Object> invokees = b.getEffectsList(p);
            for (Object invokee : invokees) {
                if (invokee instanceof GroundedEffect && Effect.isActiveEffect(invokee)) {

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
            List<Object> invokees = b.getEffectsList(attacking, attacking.getAttack());
            for (Object invokee : invokees) {
                if (invokee instanceof SemiInvulnerableBypasser && Effect.isActiveEffect(invokee)) {

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
            List<Object> invokees = b.getEffectsList(attacking, attacking.getAttack());
            for (Object invokee : invokees) {
                if (invokee instanceof BasicAccuracyBypassEffect && Effect.isActiveEffect(invokee)) {

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

        // Attacker is the Pokemon whose accuracy is being evaluated, defender is the Pokemon on which this effect is attached to
        // This is evaluated BEFORE the semi-invulnerable checks, so if this returns true, the move will hit even if the defending is semi-invulnerable
        boolean opponentBypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending);

        static boolean bypassAccuracyCheck(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            List<Object> invokees = b.getEffectsList(defending);
            for (Object invokee : invokees) {
                if (invokee instanceof OpponentAccuracyBypassEffect && Effect.isActiveEffect(invokee)) {

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
        String getUnusableMessage(Battle b, ActivePokemon p);

        static AttackSelectionEffect getUnusableEffect(Battle b, ActivePokemon p, Move m) {
            List<Object> invokees = b.getEffectsList(p);
            for (Object invokee : invokees) {
                if (invokee instanceof AttackSelectionEffect && Effect.isActiveEffect(invokee)) {

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
        boolean block(EffectNamesies weather);

        static boolean checkBlocked(Battle b, ActivePokemon p, EffectNamesies weather) {
            List<Object> invokees = b.getEffectsList(p);
            for (Object invokee : invokees) {
                if (invokee instanceof WeatherBlockerEffect && Effect.isActiveEffect(invokee)) {

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

        default boolean trapped(Battle b, ActivePokemon escaper) {
            // Ghost-type Pokemon can always escape
            return !escaper.isType(b, Type.GHOST);
        }
        String trappingMessage(ActivePokemon trapped);

        static boolean isTrapped(Battle b, ActivePokemon escaper) {
            List<Object> invokees = b.getEffectsList(escaper);
            for (Object invokee : invokees) {
                if (invokee instanceof TrappingEffect && Effect.isActiveEffect(invokee)) {

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
            List<Object> invokees = b.getEffectsList(trapper);
            for (Object invokee : invokees) {
                if (invokee instanceof OpponentTrappingEffect && Effect.isActiveEffect(invokee)) {

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

    public interface BeforeTurnEffect {

        // TODO: Rename these to attacking and defending
        boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b);

        static boolean checkCannotAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
            if (p.isFainted(b)) {
                return false;
            }

            if (opp.isFainted(b)) {
                return false;
            }

            List<Object> invokees = b.getEffectsList(p);
            for (Object invokee : invokees) {
                if (invokee instanceof BeforeTurnEffect && Effect.isActiveEffect(invokee)) {

                    BeforeTurnEffect effect = (BeforeTurnEffect)invokee;
                    if (!effect.canAttack(p, opp, b)) {
                        return true;
                    }

                    if (p.isFainted(b)) {
                        return false;
                    }

                    if (opp.isFainted(b)) {
                        return false;
                    }
                }
            }

            return false;
        }
    }

    public interface EffectBlockerEffect {
        boolean validMove(Battle b, ActivePokemon user, ActivePokemon victim);

        static boolean checkBlocked(Battle b, ActivePokemon user, ActivePokemon victim) {
            List<Object> invokees = b.getEffectsList(victim);
            for (Object invokee : invokees) {
                if (invokee instanceof EffectBlockerEffect && Effect.isActiveEffect(invokee)) {

                    // If this is an ability that is being affected by mold breaker, we don't want to do anything with it
                    if (invokee instanceof Ability && !((Ability)invokee).unbreakableMold() && user.breaksTheMold()) {
                        continue;
                    }

                    EffectBlockerEffect effect = (EffectBlockerEffect)invokee;
                    if (!effect.validMove(b, user, victim)) {
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
            List<Object> invokees = b.getEffectsList(opponent);
            for (Object invokee : invokees) {
                if (invokee instanceof TargetSwapperEffect && Effect.isActiveEffect(invokee)) {

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

        default boolean blockCrits() {
            return true;
        }

        static boolean checkBlocked(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            List<Object> invokees = b.getEffectsList(defending, attacking.getAttack());
            for (Object invokee : invokees) {
                if (invokee instanceof CritBlockerEffect && Effect.isActiveEffect(invokee)) {

                    // If this is an ability that is being affected by mold breaker, we don't want to do anything with it
                    if (invokee instanceof Ability && !((Ability)invokee).unbreakableMold() && attacking.breaksTheMold()) {
                        continue;
                    }

                    CritBlockerEffect effect = (CritBlockerEffect)invokee;
                    if (effect.blockCrits()) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public interface StatProtectingEffect {
        boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat);
        String getName();

        default String preventionMessage(ActivePokemon p, Stat s) {
            return p.getName() + "'s " + this.getName() + " prevents its " + s.getName().toLowerCase() + " from being lowered!";
        }

        static StatProtectingEffect getPreventEffect(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
            List<Object> invokees = b.getEffectsList(victim);
            for (Object invokee : invokees) {
                if (invokee instanceof StatProtectingEffect && Effect.isActiveEffect(invokee)) {

                    // If this is an ability that is being affected by mold breaker, we don't want to do anything with it
                    if (invokee instanceof Ability && !((Ability)invokee).unbreakableMold() && caster.breaksTheMold()) {
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

    public interface StatusPreventionEffect {

        // TODO: Would be nice in the future if I am able to implement multiple invoke methods for the same interface method since this could also use a basic check invoke as well
        boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status);
        String statusPreventionMessage(ActivePokemon victim);

        static StatusPreventionEffect getPreventEffect(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
            List<Object> invokees = b.getEffectsList(victim);
            for (Object invokee : invokees) {
                if (invokee instanceof StatusPreventionEffect && Effect.isActiveEffect(invokee)) {

                    // If this is an ability that is being affected by mold breaker, we don't want to do anything with it
                    if (invokee instanceof Ability && !((Ability)invokee).unbreakableMold() && caster.breaksTheMold()) {
                        continue;
                    }

                    StatusPreventionEffect effect = (StatusPreventionEffect)invokee;
                    if (effect.preventStatus(b, caster, victim, status)) {
                        return effect;
                    }
                }
            }

            return null;
        }
    }

    public interface BracingEffect {
        boolean isBracing(Battle b, ActivePokemon bracer, boolean fullHealth);
        String braceMessage(ActivePokemon bracer);

        static BracingEffect getBracingEffect(Battle b, ActivePokemon bracer, boolean fullHealth) {
            List<Object> invokees = b.getEffectsList(bracer);
            for (Object invokee : invokees) {
                if (invokee instanceof BracingEffect && Effect.isActiveEffect(invokee)) {

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

    public interface OpponentIgnoreStageEffect {
        boolean ignoreStage(Stat s);

        static boolean checkIgnoreStage(Battle b, ActivePokemon stagePokemon, ActivePokemon other, Stat s) {
            // Only add the attack when checking a defensive stat -- this means the other pokemon is the one currently attacking
            List<Object> invokees = b.getEffectsList(other);
            if (!s.user()) {
                invokees.add(other.getAttack());
            }

            for (Object invokee : invokees) {
                if (invokee instanceof OpponentIgnoreStageEffect && Effect.isActiveEffect(invokee)) {

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
        Type[] getType(Battle b, ActivePokemon p, boolean display);

        static Type[] getChangedType(Battle b, ActivePokemon p, boolean display) {
            List<Object> invokees = b.getEffectsList(p, p.getEffect(EffectNamesies.CHANGE_TYPE));
            for (Object invokee : invokees) {
                if (invokee instanceof ChangeTypeEffect && Effect.isActiveEffect(invokee)) {

                    ChangeTypeEffect effect = (ChangeTypeEffect)invokee;
                    return effect.getType(b, p, display);
                }
            }

            return null;
        }
    }

    public interface ForceMoveEffect {
        Move getForcedMove();

        static Move getForcedMove(Battle b, ActivePokemon attacking) {
            List<Object> invokees = b.getEffectsList(attacking);
            for (Object invokee : invokees) {
                if (invokee instanceof ForceMoveEffect && Effect.isActiveEffect(invokee)) {

                    ForceMoveEffect effect = (ForceMoveEffect)invokee;
                    return effect.getForcedMove();
                }
            }

            return null;
        }
    }

    public interface DifferentStatEffect {
        Integer getStat(ActivePokemon user, Stat stat);

        static Integer getStat(Battle b, ActivePokemon user, Stat stat) {
            List<Object> invokees = b.getEffectsList(user);
            for (Object invokee : invokees) {
                if (invokee instanceof DifferentStatEffect && Effect.isActiveEffect(invokee)) {

                    DifferentStatEffect effect = (DifferentStatEffect)invokee;
                    return effect.getStat(user, stat);
                }
            }

            return null;
        }
    }

    public interface CritStageEffect {

        default int increaseCritStage(int stage, ActivePokemon p) {
            return stage + 1;
        }

        static int updateCritStage(Battle b, int stage, ActivePokemon p) {
            List<Object> invokees = b.getEffectsList(p, p.getAttack());
            for (Object invokee : invokees) {
                if (invokee instanceof CritStageEffect && Effect.isActiveEffect(invokee)) {

                    CritStageEffect effect = (CritStageEffect)invokee;
                    stage = effect.increaseCritStage(stage, p);
                }
            }

            return stage;
        }
    }

    public interface PriorityChangeEffect {
        int changePriority(Battle b, ActivePokemon user);

        static int getModifier(Battle b, ActivePokemon user) {
            int modifier = 0;

            List<Object> invokees = b.getEffectsList(user);
            for (Object invokee : invokees) {
                if (invokee instanceof PriorityChangeEffect && Effect.isActiveEffect(invokee)) {

                    PriorityChangeEffect effect = (PriorityChangeEffect)invokee;
                    modifier += effect.changePriority(b, user);
                }
            }

            return modifier;
        }
    }

    public interface ChangeAttackTypeEffect {
        Type changeAttackType(Attack attack, Type original);

        static Type updateAttackType(Battle b, ActivePokemon attacking, Attack attack, Type original) {
            List<Object> invokees = b.getEffectsList(attacking);
            for (Object invokee : invokees) {
                if (invokee instanceof ChangeAttackTypeEffect && Effect.isActiveEffect(invokee)) {

                    ChangeAttackTypeEffect effect = (ChangeAttackTypeEffect)invokee;
                    original = effect.changeAttackType(attack, original);
                }
            }

            return original;
        }
    }

    public interface AttackingNoAdvantageChanger {
        boolean negateNoAdvantage(Type attackingType, Type defendingType);

        static boolean checkAttackingNoAdvantageChanger(Battle b, ActivePokemon attacking, Type attackingType, Type defendingType) {
            List<Object> invokees = b.getEffectsList(attacking);
            for (Object invokee : invokees) {
                if (invokee instanceof AttackingNoAdvantageChanger && Effect.isActiveEffect(invokee)) {

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
            List<Object> invokees = b.getEffectsList(defending);
            for (Object invokee : invokees) {
                if (invokee instanceof DefendingNoAdvantageChanger && Effect.isActiveEffect(invokee)) {

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
        List<Move> getMoveList(List<Move> actualMoves);

        static List<Move> getMoveList(Battle b, ActivePokemon p, List<Move> actualMoves) {
            List<Object> invokees = b.getEffectsList(p);
            for (Object invokee : invokees) {
                if (invokee instanceof ChangeMoveListEffect && Effect.isActiveEffect(invokee)) {

                    ChangeMoveListEffect effect = (ChangeMoveListEffect)invokee;
                    return effect.getMoveList(actualMoves);
                }
            }

            return null;
        }
    }

    public interface StatSwitchingEffect {
        Stat getSwitchStat(Battle b, ActivePokemon statPokemon, Stat s);

        static Stat switchStat(Battle b, ActivePokemon statPokemon, Stat s) {
            List<Object> invokees = b.getEffectsList(statPokemon, statPokemon.getAttack());
            for (Object invokee : invokees) {
                if (invokee instanceof StatSwitchingEffect && Effect.isActiveEffect(invokee)) {

                    StatSwitchingEffect effect = (StatSwitchingEffect)invokee;
                    s = effect.getSwitchStat(b, statPokemon, s);
                }
            }

            return s;
        }
    }

    public interface OpponentStatSwitchingEffect {
        Stat getSwitchStat(Stat s);

        static Stat switchStat(Battle b, ActivePokemon other, Stat s) {
            // Only add the attack when checking a defensive stat -- this means the other pokemon is the one currently attacking
            List<Object> invokees = b.getEffectsList(other);
            if (!s.user()) {
                invokees.add(other.getAttack());
            }

            for (Object invokee : invokees) {
                if (invokee instanceof OpponentStatSwitchingEffect && Effect.isActiveEffect(invokee)) {

                    OpponentStatSwitchingEffect effect = (OpponentStatSwitchingEffect)invokee;
                    s = effect.getSwitchStat(s);
                }
            }

            return s;
        }
    }

    public interface HalfWeightEffect {
        int getHalfAmount(int halfAmount);

        static int updateHalfAmount(Battle b, ActivePokemon anorexic, int halfAmount) {
            List<Object> invokees = b.getEffectsList(anorexic);
            for (Object invokee : invokees) {
                if (invokee instanceof HalfWeightEffect && Effect.isActiveEffect(invokee)) {

                    // If this is an ability that is being affected by mold breaker, we don't want to do anything with it
                    if (invokee instanceof Ability && !((Ability)invokee).unbreakableMold() && b.getOtherPokemon(anorexic).breaksTheMold()) {
                        continue;
                    }

                    HalfWeightEffect effect = (HalfWeightEffect)invokee;
                    halfAmount = effect.getHalfAmount(halfAmount);
                }
            }

            return halfAmount;
        }
    }

    public interface StageChangingEffect {
        int adjustStage(Battle b, ActivePokemon p, ActivePokemon opp, Stat s);

        static int getModifier(Battle b, ActivePokemon p, ActivePokemon opp, Stat s) {
            int modifier = 0;

            ActivePokemon moldBreaker = s.user() ? null : opp;

            List<Object> invokees = b.getEffectsList(p);
            for (Object invokee : invokees) {
                if (invokee instanceof StageChangingEffect && Effect.isActiveEffect(invokee)) {

                    // If this is an ability that is being affected by mold breaker, we don't want to do anything with it
                    if (invokee instanceof Ability && !((Ability)invokee).unbreakableMold() && moldBreaker != null && moldBreaker.breaksTheMold()) {
                        continue;
                    }

                    StageChangingEffect effect = (StageChangingEffect)invokee;
                    modifier += effect.adjustStage(b, p, opp, s);
                }
            }

            return modifier;
        }
    }

    public interface StatModifyingEffect {
        double modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s);

        static double getModifier(Battle b, ActivePokemon p, ActivePokemon opp, Stat s) {
            double modifier = 1;

            ActivePokemon moldBreaker = s.user() ? null : opp;

            List<Object> invokees = b.getEffectsList(p);
            for (Object invokee : invokees) {
                if (invokee instanceof StatModifyingEffect && Effect.isActiveEffect(invokee)) {

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

    public interface SimpleStatModifyingEffect extends StatModifyingEffect {
        boolean isModifyStat(Stat s);
        double getModifier();

        default boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return true;
        }

        @Override
        default double modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s) {
            if (isModifyStat(s) && canModifyStat(b, p, opp)) {
                return getModifier();
            }

            return 1;
        }
    }

    public interface StatChangingEffect {

        // b: The current battle
        // p: The Pokemon that the stat is being altered on
        // opp: The opposing Pokemon
        // s: The stat that is being altered
        // stat: The current value of stat s
        // Return: The modified value of stat, if stat was not altered, just return stat
        int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat);

        static int modifyStat(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
            ActivePokemon moldBreaker = s.user() ? null : opp;

            List<Object> invokees = b.getEffectsList(p);
            for (Object invokee : invokees) {
                if (invokee instanceof StatChangingEffect && Effect.isActiveEffect(invokee)) {

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

    public interface PowerChangeEffect {
        double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim);

        static double getModifier(Battle b, ActivePokemon user, ActivePokemon victim) {
            double modifier = 1;

            List<Object> invokees = b.getEffectsList(user, user.getAttack());
            for (Object invokee : invokees) {
                if (invokee instanceof PowerChangeEffect && Effect.isActiveEffect(invokee)) {

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

            List<Object> invokees = b.getEffectsList(victim);
            for (Object invokee : invokees) {
                if (invokee instanceof OpponentPowerChangeEffect && Effect.isActiveEffect(invokee)) {

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

    public interface AdvantageMultiplierMove {
        double multiplyAdvantage(Type attackingType, Type[] defendingTypes);

        static double getModifier(ActivePokemon attacking, Type attackingType, Type[] defendingTypes) {
            double modifier = 1;

            List<Object> invokees = Collections.singletonList(attacking.getAttack());
            for (Object invokee : invokees) {
                if (invokee instanceof AdvantageMultiplierMove && Effect.isActiveEffect(invokee)) {

                    AdvantageMultiplierMove effect = (AdvantageMultiplierMove)invokee;
                    modifier *= effect.multiplyAdvantage(attackingType, defendingTypes);
                }
            }

            return modifier;
        }
    }

    public interface AbsorbDamageEffect {
        boolean absorbDamage(Battle b, ActivePokemon damageTaker, int damageAmount);

        static boolean checkAbsorbDamageEffect(Battle b, ActivePokemon damageTaker, int damageAmount) {
            List<Object> invokees = b.getEffectsList(damageTaker);
            for (Object invokee : invokees) {
                if (invokee instanceof AbsorbDamageEffect && Effect.isActiveEffect(invokee)) {

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
        void damageTaken(Battle b, ActivePokemon damageTaker);

        static void invokeDamageTakenEffect(Battle b, ActivePokemon damageTaker) {
            List<Object> invokees = b.getEffectsList(damageTaker);
            for (Object invokee : invokees) {
                if (invokee instanceof DamageTakenEffect && Effect.isActiveEffect(invokee)) {

                    DamageTakenEffect effect = (DamageTakenEffect)invokee;
                    effect.damageTaken(b, damageTaker);
                }
            }
        }
    }

    public interface AlwaysCritEffect {

        default boolean shouldCrit(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            return true;
        }

        static boolean defCritsies(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            List<Object> invokees = b.getEffectsList(attacking, attacking.getAttack());
            for (Object invokee : invokees) {
                if (invokee instanceof AlwaysCritEffect && Effect.isActiveEffect(invokee)) {

                    AlwaysCritEffect effect = (AlwaysCritEffect)invokee;
                    if (effect.shouldCrit(b, attacking, defending)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public interface StatusReceivedEffect {
        void receiveStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition statusType);

        static void invokeStatusReceivedEffect(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition statusType) {
            List<Object> invokees = b.getEffectsList(victim);
            for (Object invokee : invokees) {
                if (invokee instanceof StatusReceivedEffect && Effect.isActiveEffect(invokee)) {

                    StatusReceivedEffect effect = (StatusReceivedEffect)invokee;
                    effect.receiveStatus(b, caster, victim, statusType);
                }
            }
        }
    }

    public interface OpponentStatusReceivedEffect {
        void receiveStatus(Battle b, ActivePokemon victim, StatusCondition statusType);

        static void invokeOpponentStatusReceivedEffect(Battle b, ActivePokemon victim, StatusCondition statusType) {
            List<Object> invokees = b.getEffectsList(b.getOtherPokemon(victim));
            for (Object invokee : invokees) {
                if (invokee instanceof OpponentStatusReceivedEffect && Effect.isActiveEffect(invokee)) {

                    OpponentStatusReceivedEffect effect = (OpponentStatusReceivedEffect)invokee;
                    effect.receiveStatus(b, victim, statusType);
                }
            }
        }
    }

    public interface SleepyFightsterEffect {

        static boolean containsSleepyFightsterEffect(Battle b, ActivePokemon p) {
            List<Object> invokees = b.getEffectsList(p, p.getAttack());
            for (Object invokee : invokees) {
                if (invokee instanceof SleepyFightsterEffect && Effect.isActiveEffect(invokee)) {

                    return true;
                }
            }

            return false;
        }
    }

    public interface OpponentEndAttackEffect {
        void endsies(Battle b, ActivePokemon attacking);

        static void invokeOpponentEndAttackEffect(Battle b, ActivePokemon attacking) {
            List<Object> invokees = b.getEffectsList(b.getOtherPokemon(attacking));
            for (Object invokee : invokees) {
                if (invokee instanceof OpponentEndAttackEffect && Effect.isActiveEffect(invokee)) {

                    OpponentEndAttackEffect effect = (OpponentEndAttackEffect)invokee;
                    effect.endsies(b, attacking);
                }
            }
        }
    }

    public interface TerrainCastEffect {
        void newTerrain(Battle b, ActivePokemon p, TerrainType newTerrain);

        static void invokeTerrainCastEffect(Battle b, ActivePokemon p, TerrainType newTerrain) {
            List<Object> invokees = b.getEffectsList(p);
            for (Object invokee : invokees) {
                if (invokee instanceof TerrainCastEffect && Effect.isActiveEffect(invokee)) {

                    TerrainCastEffect effect = (TerrainCastEffect)invokee;
                    effect.newTerrain(b, p, newTerrain);
                }
            }
        }
    }

    public interface AttackBlocker {
        boolean block(Battle b, ActivePokemon user, ActivePokemon victim);
        default void alternateEffect(Battle b, ActivePokemon user, ActivePokemon victim) {}

        default String getBlockMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
            return Effect.DEFAULT_FAIL_MESSAGE;
        }

        static AttackBlocker checkBlocked(Battle b, ActivePokemon user, ActivePokemon victim) {
            List<Object> invokees = b.getEffectsList(victim);
            for (Object invokee : invokees) {
                if (invokee instanceof AttackBlocker && Effect.isActiveEffect(invokee)) {

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

        default String getBlockMessage(Battle b, ActivePokemon user) {
            return Effect.DEFAULT_FAIL_MESSAGE;
        }

        static SelfAttackBlocker checkBlocked(Battle b, ActivePokemon user) {
            List<Object> invokees = b.getEffectsList(user, user.getAttack());
            for (Object invokee : invokees) {
                if (invokee instanceof SelfAttackBlocker && Effect.isActiveEffect(invokee)) {

                    SelfAttackBlocker effect = (SelfAttackBlocker)invokee;
                    if (effect.block(b, user)) {
                        return effect;
                    }
                }
            }

            return null;
        }
    }

    public interface ItemSwapperEffect {
        String getSwitchMessage(ActivePokemon user, Item userItem, ActivePokemon victim, Item victimItem);
    }

    public interface SwapOpponentEffect {
        String getSwapMessage(ActivePokemon user, ActivePokemon victim);
    }

    public interface ProtectingEffect extends AttackBlocker {
        default void protectingEffects(Battle b, ActivePokemon p, ActivePokemon opp) {}

        default boolean protectingCondition(Battle b, ActivePokemon attacking) {
            return true;
        }

        @Override
        default boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            Attack attack = user.getAttack();
            return protectingCondition(b, user) && !attack.isSelfTargetStatusMove() && !attack.isMoveType(MoveType.FIELD) && !attack.isMoveType(MoveType.PROTECT_PIERCING);
        }

        @Override
        default void alternateEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            CrashDamageMove.invokeCrashDamageMove(b, user);
            this.protectingEffects(b, user, victim);
        }

        @Override
        default String getBlockMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + " is protecting itself!";
        }
    }

    public interface AttackSelectionSelfBlockerEffect extends AttackSelectionEffect, SelfAttackBlocker {

        @Override
        default boolean block(Battle b, ActivePokemon user) {
            return !this.usable(b, user, user.getMove());
        }
    }

    public interface PowderMove extends SelfAttackBlocker {

        @Override
        default boolean block(Battle b, ActivePokemon user) {
            // Powder moves don't work against Grass-type Pokemon
            return b.getOtherPokemon(user).isType(b, Type.GRASS);
        }
    }

    public interface WildEncounterAlterer {
        void alterWildPokemon(ActivePokemon playerFront, WildEncounterInfo encounterData, WildEncounter encounter);

        static void invokeWildEncounterAlterer(ActivePokemon playerFront, WildEncounterInfo encounterData, WildEncounter encounter) {
            List<Object> invokees = new ArrayList<>();
            invokees.add(playerFront.getAbility());
            invokees.add(playerFront.getActualHeldItem());

            for (Object invokee : invokees) {
                if (invokee instanceof WildEncounterAlterer && Effect.isActiveEffect(invokee)) {

                    WildEncounterAlterer effect = (WildEncounterAlterer)invokee;
                    effect.alterWildPokemon(playerFront, encounterData, encounter);
                }
            }
        }
    }

    public interface MaxLevelWildEncounterEffect extends WildEncounterAlterer {

        @Override
        default void alterWildPokemon(ActivePokemon playerFront, WildEncounterInfo encounterData, WildEncounter encounter) {
            if (RandomUtils.chanceTest(50)) {
                encounter.setLevel(encounterData.getMaxLevel());
            }
        }
    }

    public interface RepellingEffect {
        boolean shouldRepel(ActivePokemon playerFront, WildEncounter wildPokemon);

        static boolean checkRepellingEffect(ActivePokemon playerFront, WildEncounter wildPokemon) {
            List<Object> invokees = new ArrayList<>();
            invokees.add(playerFront.getAbility());
            invokees.add(playerFront.getActualHeldItem());

            for (Object invokee : invokees) {
                if (invokee instanceof RepellingEffect && Effect.isActiveEffect(invokee)) {

                    RepellingEffect effect = (RepellingEffect)invokee;
                    if (effect.shouldRepel(playerFront, wildPokemon)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public interface RepelLowLevelEncounterEffect extends RepellingEffect {

        @Override
        default boolean shouldRepel(ActivePokemon playerFront, WildEncounter wildPokemon) {
            return RandomUtils.chanceTest(50) && wildPokemon.getLevel() + 5 <= playerFront.getLevel();
        }
    }

    public interface WildEncounterSelector {
        WildEncounterInfo getWildEncounter(ActivePokemon playerFront, WildEncounterInfo[] wildEncounters);

        static WildEncounterInfo getForcedWildEncounter(ActivePokemon playerFront, WildEncounterInfo[] wildEncounters) {
            List<Object> invokees = new ArrayList<>();
            invokees.add(playerFront.getAbility());
            invokees.add(playerFront.getActualHeldItem());

            for (Object invokee : invokees) {
                if (invokee instanceof WildEncounterSelector && Effect.isActiveEffect(invokee)) {

                    WildEncounterSelector effect = (WildEncounterSelector)invokee;
                    return effect.getWildEncounter(playerFront, wildEncounters);
                }
            }

            return null;
        }
    }

    public interface TypedWildEncounterSelector extends WildEncounterSelector {
        Type getEncounterType();

        @Override
        default WildEncounterInfo getWildEncounter(ActivePokemon playerFront, WildEncounterInfo[] wildEncounters) {
            if (RandomUtils.chanceTest(50)) {
                List<WildEncounterInfo> typedList = new ArrayList<>();
                for (WildEncounterInfo wildEncounter : wildEncounters) {
                    PokemonInfo pokemon = wildEncounter.getPokemonName().getInfo();
                    if (pokemon.isType(this.getEncounterType())) {
                        typedList.add(wildEncounter);
                    }
                }

                if (!typedList.isEmpty()) {
                    return RandomUtils.getRandomValue(typedList);
                }
            }

            return null;
        }
    }

    public interface EncounterRateMultiplier {
        double getEncounterRateMultiplier();

        static double getModifier(ActivePokemon playerFront) {
            double modifier = 1;

            List<Object> invokees = new ArrayList<>();
            invokees.add(playerFront.getAbility());
            invokees.add(playerFront.getActualHeldItem());

            for (Object invokee : invokees) {
                if (invokee instanceof EncounterRateMultiplier && Effect.isActiveEffect(invokee)) {

                    EncounterRateMultiplier effect = (EncounterRateMultiplier)invokee;
                    modifier *= effect.getEncounterRateMultiplier();
                }
            }

            return modifier;
        }
    }

    public interface ModifyStageValueEffect {
        int modifyStageValue(int modVal);

        static int updateModifyStageValueEffect(Battle b, ActivePokemon caster, ActivePokemon victim, int modVal) {
            ActivePokemon moldBreaker = caster == victim ? null : caster;

            List<Object> invokees = b.getEffectsList(victim);
            for (Object invokee : invokees) {
                if (invokee instanceof ModifyStageValueEffect && Effect.isActiveEffect(invokee)) {

                    // If this is an ability that is being affected by mold breaker, we don't want to do anything with it
                    if (invokee instanceof Ability && !((Ability)invokee).unbreakableMold() && moldBreaker != null && moldBreaker.breaksTheMold()) {
                        continue;
                    }

                    ModifyStageValueEffect effect = (ModifyStageValueEffect)invokee;
                    modVal = effect.modifyStageValue(modVal);
                }
            }

            return modVal;
        }
    }

    public interface WeatherEliminatingEffect extends EntryEndTurnEffect {
        String getEliminateMessage(ActivePokemon eliminator);

        default boolean eliminateWeather(Weather weather) {
            return weather.namesies() != EffectNamesies.CLEAR_SKIES;
        }

        @Override
        default void applyEffect(Battle b, ActivePokemon p) {
            b.addEffect(b.getWeather());
        }

        static boolean shouldEliminateWeather(Battle b, ActivePokemon eliminator, Weather weather) {
            List<Object> invokees = b.getEffectsList(eliminator);
            for (Object invokee : invokees) {
                if (invokee instanceof WeatherEliminatingEffect && Effect.isActiveEffect(invokee)) {

                    WeatherEliminatingEffect effect = (WeatherEliminatingEffect)invokee;
                    if (effect.eliminateWeather(weather)) {
                        Messages.add(effect.getEliminateMessage(eliminator));
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public interface EntryEndTurnEffect extends EntryEffect, EndTurnEffect {
        void applyEffect(Battle b, ActivePokemon p);

        @Override
        default void applyEndTurn(ActivePokemon victim, Battle b) {
            applyEffect(b, victim);
        }

        @Override
        default void enter(Battle b, ActivePokemon enterer) {
            applyEffect(b, enterer);
        }
    }

    public interface SwitchOutEffect {
        void switchOut(ActivePokemon switchee);

        static void invokeSwitchOutEffect(ActivePokemon switchee) {
            List<Object> invokees = switchee.getAllEffects(null);

            for (Object invokee : invokees) {
                if (invokee instanceof SwitchOutEffect && Effect.isActiveEffect(invokee)) {

                    SwitchOutEffect effect = (SwitchOutEffect)invokee;
                    effect.switchOut(switchee);
                }
            }
        }
    }

    public interface WeatherExtendingEffect {
        int getExtensionTurns(EffectNamesies weatherType);

        static int getModifier(Battle b, ActivePokemon p, EffectNamesies weatherType) {
            int modifier = 0;

            List<Object> invokees = b.getEffectsList(p);
            for (Object invokee : invokees) {
                if (invokee instanceof WeatherExtendingEffect && Effect.isActiveEffect(invokee)) {

                    WeatherExtendingEffect effect = (WeatherExtendingEffect)invokee;
                    modifier += effect.getExtensionTurns(weatherType);
                }
            }

            return modifier;
        }
    }

    public interface SapHealthEffect extends ApplyDamageEffect {

        default double sapPercentage() {
            return .5;
        }

        default String getSapMessage(ActivePokemon victim) {
            return victim.getName() + "'s health was sapped!";
        }

        default void sapHealth(Battle b, ActivePokemon user, ActivePokemon victim, int damageAmount, boolean print) {
            int sapAmount = (int)Math.ceil(damageAmount*this.sapPercentage());

            // Sap message
            if (print) {
                Messages.add(this.getSapMessage(victim));
            }

            // Big Root heals an additional 30%
            if (user.isHoldingItem(b, ItemNamesies.BIG_ROOT)) {
                sapAmount *= 1.3;
            }

            if (victim.hasAbility(AbilityNamesies.LIQUID_OOZE)) {
                Messages.add(victim.getName() + "'s " + AbilityNamesies.LIQUID_OOZE.getName() + " caused " + user.getName() + " to lose health instead!");
                user.reduceHealth(b, sapAmount);
                return;
            }

            // Healers gon' heal
            if (!user.hasEffect(EffectNamesies.HEAL_BLOCK)) {
                user.heal(sapAmount);
            }

            Messages.add(new MessageUpdate().updatePokemon(b, victim));
            Messages.add(new MessageUpdate().updatePokemon(b, user));
        }

        @Override
        default void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
            this.sapHealth(b, user, victim, damage, true);
        }
    }

    public interface PowerCountMove extends PowerChangeEffect {
        boolean doubleDefenseCurled();

        @Override
        default double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return Math.min(user.getCount(), 5)*(this.doubleDefenseCurled() && user.hasEffect(EffectNamesies.USED_DEFENSE_CURL) ? 2 : 1);
        }
    }

    public interface PassableEffect {
    }

    public interface DefiniteEscape {
    }

    public interface StallingEffect {
    }

    public interface TerrainEffect {
    }
}
