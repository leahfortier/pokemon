package battle.effect.generic;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.Attack;
import battle.attack.AttackInterface;
import battle.attack.Move;
import battle.attack.MoveType;
import battle.effect.CastSource;
import battle.effect.InvokeEffect;
import battle.effect.attack.MultiTurnMove;
import battle.effect.generic.battle.BattleEffect;
import battle.effect.generic.battle.weather.WeatherEffect;
import battle.effect.generic.battle.weather.WeatherNamesies;
import battle.effect.generic.EffectNamesies.BattleEffectNamesies;
import battle.effect.generic.pokemon.PokemonEffect;
import battle.effect.generic.pokemon.PokemonEffectNamesies;
import battle.effect.generic.team.TeamEffect;
import battle.effect.status.StatusCondition;
import item.Item;
import item.ItemNamesies;
import item.hold.HoldItem;
import main.Game;
import main.Global;
import map.overworld.TerrainType;
import map.overworld.WildEncounter;
import map.overworld.WildEncounterInfo;
import message.MessageUpdate;
import message.MessageUpdateType;
import message.Messages;
import pokemon.PokemonInfo;
import pokemon.Stat;
import pokemon.ability.Ability;
import pokemon.ability.AbilityNamesies;
import trainer.Team;
import trainer.Trainer;
import trainer.WildPokemon;
import type.PokeType;
import type.Type;
import util.RandomUtils;
import util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class EffectInterfaces {

    // Class to hold interfaces -- should not be instantiated
    private EffectInterfaces() {
        Global.error("EffectInterfaces class cannot be instantiated.");
    }

    // EVERYTHING BELOW IS GENERATED ###

    // This is used when the user applies direct damage to an opponent, and has special effects associated with the user
    public interface ApplyDamageEffect {

        // b: The current battle
        // user: The user of that attack, the one who is implementing this effect
        // victim: The Pokemon that received the attack
        // damage: The amount of damage that was dealt to victim by the user
        void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage);

        static void invokeApplyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
            if (user.isFainted(b)) {
                return;
            }

            List<InvokeEffect> invokees = b.getEffectsList(user, user.getAttack());
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof ApplyDamageEffect && InvokeEffect.isActiveEffect(invokee)) {
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

            List<InvokeEffect> invokees = b.getEffectsList(victim);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof OpponentApplyDamageEffect && InvokeEffect.isActiveEffect(invokee)) {
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

            List<InvokeEffect> invokees = b.getEffectsList(victim);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof EndTurnEffect && InvokeEffect.isActiveEffect(invokee)) {
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
    public interface BattleEndTurnEffect {
        default void singleEndTurnEffect(Battle b, ActivePokemon victim) {}

        default String getEndTurnMessage(Battle b) {
            // Definitely not required to have a message here
            return StringUtils.empty();
        }

        default void applyEndTurn(Battle b) {
            Messages.add(this.getEndTurnMessage(b));

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
                if (invokee instanceof BattleEndTurnEffect && InvokeEffect.isActiveEffect(invokee)) {
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
                if (invokee instanceof SuperDuperEndTurnEffect && InvokeEffect.isActiveEffect(invokee)) {
                    SuperDuperEndTurnEffect effect = (SuperDuperEndTurnEffect)invokee;
                    if (effect.theVeryVeryEnd(b, p)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public interface RecoilMove extends AttackInterface, ApplyDamageEffect {
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

    public interface SelfHealingMove extends AttackInterface {
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
        // victim: The one who is taking damage and is implementing this effect
        void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim);

        static void invokeTakeDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (victim.isFainted(b)) {
                return;
            }

            List<InvokeEffect> invokees = b.getEffectsList(victim);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof TakeDamageEffect && InvokeEffect.isActiveEffect(invokee)) {
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

            List<InvokeEffect> invokees = b.getEffectsList(user);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof OpponentTakeDamageEffect && InvokeEffect.isActiveEffect(invokee)) {
                    OpponentTakeDamageEffect effect = (OpponentTakeDamageEffect)invokee;
                    effect.takeDamage(b, user, victim);

                    if (victim.isFainted(b)) {
                        return;
                    }
                }
            }
        }
    }

    public interface CrashDamageMove extends AttackInterface {
        void crash(Battle b, ActivePokemon user);

        static void invokeCrashDamageMove(Battle b, ActivePokemon user) {
            List<InvokeEffect> invokees = Collections.singletonList(user.getAttack());
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof CrashDamageMove && InvokeEffect.isActiveEffect(invokee)) {
                    CrashDamageMove effect = (CrashDamageMove)invokee;
                    effect.crash(b, user);
                }
            }
        }
    }

    public interface EffectReleaser {

        default void release(Battle b, ActivePokemon released, String releaseMessage) {
            Messages.add(releaseMessage);

            if (this instanceof PokemonEffect) {
                released.getEffects().remove((PokemonEffect)this);
            } else if (this instanceof TeamEffect) {
                b.getTrainer(released).getEffects().remove((TeamEffect)this);
            } else {
                Global.error("Invalid release object " + this.getClass().getSimpleName());
            }
        }
    }

    public interface BarrierEffect extends EffectReleaser {
        String getBreakMessage(ActivePokemon breaker);

        default void breakBarrier(Battle b, ActivePokemon breaker, ActivePokemon broken) {
            this.release(b, broken, this.getBreakMessage(breaker));
        }

        static void breakBarriers(Battle b, ActivePokemon breaker, ActivePokemon broken) {
            List<InvokeEffect> invokees = b.getEffectsList(broken);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof BarrierEffect && InvokeEffect.isActiveEffect(invokee)) {
                    BarrierEffect effect = (BarrierEffect)invokee;
                    effect.breakBarrier(b, breaker, broken);
                }
            }
        }
    }

    public interface DefogRelease extends EffectReleaser {
        String getDefogReleaseMessage(ActivePokemon released);

        default void releaseDefog(Battle b, ActivePokemon released) {
            this.release(b, released, this.getDefogReleaseMessage(released));
        }

        static void release(Battle b, ActivePokemon released) {
            List<InvokeEffect> invokees = b.getEffectsList(released);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof DefogRelease && InvokeEffect.isActiveEffect(invokee)) {
                    DefogRelease effect = (DefogRelease)invokee;
                    effect.releaseDefog(b, released);
                }
            }
        }
    }

    public interface RapidSpinRelease extends EffectReleaser {
        String getRapidSpinReleaseMessage(ActivePokemon released);

        default void releaseRapidSpin(Battle b, ActivePokemon released) {
            this.release(b, released, this.getRapidSpinReleaseMessage(released));
        }

        static void release(Battle b, ActivePokemon released) {
            List<InvokeEffect> invokees = b.getEffectsList(released);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof RapidSpinRelease && InvokeEffect.isActiveEffect(invokee)) {
                    RapidSpinRelease effect = (RapidSpinRelease)invokee;
                    effect.releaseRapidSpin(b, released);
                }
            }
        }
    }

    public interface NameChanger {

        // TODO: Not a fan that this only operates on the ability but then again I'm not passing the battle in here and also fuck illusion srsly I might just special case it since it's so fucking unique
        String getNameChange();
        void setNameChange(Battle b, ActivePokemon victim);

        static String getChangedName(ActivePokemon p) {
            List<InvokeEffect> invokees = Collections.singletonList(p.getAbility());
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof NameChanger && InvokeEffect.isActiveEffect(invokee)) {
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
                if (invokee instanceof NameChanger && InvokeEffect.isActiveEffect(invokee)) {
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
                if (invokee instanceof EntryEffect && InvokeEffect.isActiveEffect(invokee)) {
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
            List<InvokeEffect> invokees = b.getEffectsList(victim);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof StatLoweredEffect && InvokeEffect.isActiveEffect(invokee)) {
                    StatLoweredEffect effect = (StatLoweredEffect)invokee;
                    effect.takeItToTheNextLevel(b, caster, victim);
                }
            }
        }
    }

    public interface LevitationEffect {
        default void fall(Battle b, ActivePokemon fallen) {}

        static void falllllllll(Battle b, ActivePokemon fallen) {
            List<InvokeEffect> invokees = b.getEffectsList(fallen);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof LevitationEffect && InvokeEffect.isActiveEffect(invokee)) {
                    LevitationEffect effect = (LevitationEffect)invokee;
                    effect.fall(b, fallen);
                }
            }
        }

        static boolean containsLevitationEffect(Battle b, ActivePokemon p, ActivePokemon moldBreaker) {
            List<InvokeEffect> invokees = b.getEffectsList(p);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof LevitationEffect && InvokeEffect.isActiveEffect(invokee)) {

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
            List<InvokeEffect> invokees = b.getEffectsList(murderer, murderer.getAttack());
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof MurderEffect && InvokeEffect.isActiveEffect(invokee)) {
                    MurderEffect effect = (MurderEffect)invokee;
                    effect.killWish(b, dead, murderer);
                }
            }
        }
    }

    public interface EndBattleEffect {
        void afterBattle(Trainer player, Battle b, ActivePokemon p);

        static void invokeEndBattleEffect(List<? extends InvokeEffect> invokees, Trainer player, Battle b, ActivePokemon p) {
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof EndBattleEffect && InvokeEffect.isActiveEffect(invokee)) {
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
                ((MultiTurnMove)p.getAttack()).resetReady();
                Messages.add(p.getName() + " fell to the ground!");
                PokemonEffectNamesies.FLINCH.getEffect().cast(b, p, p, CastSource.EFFECT, false);
            }

            LevitationEffect.falllllllll(b, p);
        }

        static boolean containsGroundedEffect(Battle b, ActivePokemon p) {
            List<InvokeEffect> invokees = b.getEffectsList(p);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof GroundedEffect && InvokeEffect.isActiveEffect(invokee)) {
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
                if (invokee instanceof SemiInvulnerableBypasser && InvokeEffect.isActiveEffect(invokee)) {
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
                if (invokee instanceof BasicAccuracyBypassEffect && InvokeEffect.isActiveEffect(invokee)) {
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
            List<InvokeEffect> invokees = b.getEffectsList(defending);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof OpponentAccuracyBypassEffect && InvokeEffect.isActiveEffect(invokee)) {
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
            List<InvokeEffect> invokees = b.getEffectsList(p);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof AttackSelectionEffect && InvokeEffect.isActiveEffect(invokee)) {
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
            List<InvokeEffect> invokees = b.getEffectsList(p);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof WeatherBlockerEffect && InvokeEffect.isActiveEffect(invokee)) {
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
            // Ghost-type Pokemon can always escape
            return !escaper.isType(b, Type.GHOST);
        }

        static boolean isTrapped(Battle b, ActivePokemon escaper) {
            List<InvokeEffect> invokees = b.getEffectsList(escaper);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof TrappingEffect && InvokeEffect.isActiveEffect(invokee)) {
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
                if (invokee instanceof OpponentTrappingEffect && InvokeEffect.isActiveEffect(invokee)) {
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

            List<InvokeEffect> invokees = b.getEffectsList(p);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof BeforeTurnEffect && InvokeEffect.isActiveEffect(invokee)) {
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
            List<InvokeEffect> invokees = b.getEffectsList(victim);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof EffectBlockerEffect && InvokeEffect.isActiveEffect(invokee)) {

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
            List<InvokeEffect> invokees = b.getEffectsList(opponent);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof TargetSwapperEffect && InvokeEffect.isActiveEffect(invokee)) {

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
            List<InvokeEffect> invokees = b.getEffectsList(defending, attacking.getAttack());
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof CritBlockerEffect && InvokeEffect.isActiveEffect(invokee)) {

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

    public interface StatProtectingEffect extends InvokeEffect {
        boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat);

        default String preventionMessage(Battle b, ActivePokemon p, Stat s) {
            String statName = s.getName().toLowerCase();

            CastSource source = this.getSource().getCastSource();
            if (source.hasSourceName()) {
                String sourceName = source.getSourceName(b, p);
                return p.getName() + "'s " + sourceName + " prevents its " + statName + " from being lowered!";
            }

            return p.getName() + "'s " + statName + " cannot be lowered!";
        }

        static StatProtectingEffect getPreventEffect(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
            List<InvokeEffect> invokees = b.getEffectsList(victim);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof StatProtectingEffect && InvokeEffect.isActiveEffect(invokee)) {

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
            List<InvokeEffect> invokees = b.getEffectsList(victim);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof StatusPreventionEffect && InvokeEffect.isActiveEffect(invokee)) {

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
            List<InvokeEffect> invokees = b.getEffectsList(bracer);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof BracingEffect && InvokeEffect.isActiveEffect(invokee)) {

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
            List<InvokeEffect> invokees = b.getEffectsList(other);
            if (!s.user()) {
                invokees.add(other.getAttack());
            }

            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof OpponentIgnoreStageEffect && InvokeEffect.isActiveEffect(invokee)) {

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
                if (invokee instanceof ChangeTypeEffect && InvokeEffect.isActiveEffect(invokee)) {
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
                if (invokee instanceof ForceMoveEffect && InvokeEffect.isActiveEffect(invokee)) {
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
                if (invokee instanceof DifferentStatEffect && InvokeEffect.isActiveEffect(invokee)) {
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

        default int increaseCritStage(int stage, ActivePokemon p) {
            return stage + 1;
        }

        static int updateCritStage(Battle b, int stage, ActivePokemon p) {
            List<InvokeEffect> invokees = b.getEffectsList(p, p.getAttack());
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof CritStageEffect && InvokeEffect.isActiveEffect(invokee)) {
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

            List<InvokeEffect> invokees = b.getEffectsList(user);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof PriorityChangeEffect && InvokeEffect.isActiveEffect(invokee)) {
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
            List<InvokeEffect> invokees = b.getEffectsList(attacking);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof ChangeAttackTypeEffect && InvokeEffect.isActiveEffect(invokee)) {
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
            List<InvokeEffect> invokees = b.getEffectsList(attacking);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof AttackingNoAdvantageChanger && InvokeEffect.isActiveEffect(invokee)) {
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
                if (invokee instanceof DefendingNoAdvantageChanger && InvokeEffect.isActiveEffect(invokee)) {
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
            List<InvokeEffect> invokees = b.getEffectsList(p);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof ChangeMoveListEffect && InvokeEffect.isActiveEffect(invokee)) {
                    ChangeMoveListEffect effect = (ChangeMoveListEffect)invokee;
                    List<Move> value = effect.getMoveList(actualMoves);
                    if (value != null) {
                        return value;
                    }
                }
            }

            return null;
        }
    }

    public interface StatSwitchingEffect {
        Stat getSwitchStat(Battle b, ActivePokemon statPokemon, Stat s);

        static Stat switchStat(Battle b, ActivePokemon statPokemon, Stat s) {
            List<InvokeEffect> invokees = b.getEffectsList(statPokemon, statPokemon.getAttack());
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof StatSwitchingEffect && InvokeEffect.isActiveEffect(invokee)) {
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
            List<InvokeEffect> invokees = b.getEffectsList(other);
            if (!s.user()) {
                invokees.add(other.getAttack());
            }

            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof OpponentStatSwitchingEffect && InvokeEffect.isActiveEffect(invokee)) {
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
            List<InvokeEffect> invokees = b.getEffectsList(anorexic);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof HalfWeightEffect && InvokeEffect.isActiveEffect(invokee)) {

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

            List<InvokeEffect> invokees = b.getEffectsList(p);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof StageChangingEffect && InvokeEffect.isActiveEffect(invokee)) {

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

            List<InvokeEffect> invokees = b.getEffectsList(p);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof StatModifyingEffect && InvokeEffect.isActiveEffect(invokee)) {

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

            List<InvokeEffect> invokees = b.getEffectsList(p);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof StatChangingEffect && InvokeEffect.isActiveEffect(invokee)) {

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
                if (invokee instanceof EffectChanceMultiplierEffect && InvokeEffect.isActiveEffect(invokee)) {
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
                if (invokee instanceof PowerChangeEffect && InvokeEffect.isActiveEffect(invokee)) {
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
                if (invokee instanceof OpponentPowerChangeEffect && InvokeEffect.isActiveEffect(invokee)) {

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
                if (invokee instanceof AdvantageMultiplierMove && InvokeEffect.isActiveEffect(invokee)) {
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
            List<InvokeEffect> invokees = b.getEffectsList(damageTaker);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof AbsorbDamageEffect && InvokeEffect.isActiveEffect(invokee)) {
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
            List<InvokeEffect> invokees = b.getEffectsList(damageTaker);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof DamageTakenEffect && InvokeEffect.isActiveEffect(invokee)) {
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
            List<InvokeEffect> invokees = b.getEffectsList(attacking, attacking.getAttack());
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof AlwaysCritEffect && InvokeEffect.isActiveEffect(invokee)) {
                    AlwaysCritEffect effect = (AlwaysCritEffect)invokee;
                    if (effect.shouldCrit(b, attacking, defending)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public interface EffectCurerItem extends HoldItem, EffectReceivedEffect, EndTurnEffect {
        Set<PokemonEffectNamesies> getCurableEffects();
        String getRemoveMessage(ActivePokemon victim, PokemonEffectNamesies effectType);

        default boolean usesies(ActivePokemon user) {
            boolean used = false;
            for (PokemonEffectNamesies removableEffect : this.getCurableEffects()) {
                if (user.getEffects().remove(removableEffect)) {
                    used = true;
                    Messages.add(this.getRemoveMessage(user, removableEffect));
                }
            }

            return used;
        }

        @Override
        default void receiveEffect(Battle b, ActivePokemon caster, ActivePokemon victim, EffectNamesies effectType) {
            if (effectType instanceof PokemonEffectNamesies && this.getCurableEffects().contains(effectType)) {
                PokemonEffectNamesies pokemonEffectType = (PokemonEffectNamesies)effectType;
                Messages.add(this.getRemoveMessage(victim, pokemonEffectType));
                victim.getEffects().remove(pokemonEffectType);
                victim.consumeItem(b);
            }
        }

        @Override
        default void applyEndTurn(ActivePokemon victim, Battle b) {
            if (usesies(victim)) {
                victim.consumeItem(b);
            }
        }
    }

    public interface EffectReceivedEffect {
        void receiveEffect(Battle b, ActivePokemon caster, ActivePokemon victim, EffectNamesies effectType);

        static void invokeEffectReceivedEffect(Battle b, ActivePokemon caster, ActivePokemon victim, EffectNamesies effectType) {
            List<InvokeEffect> invokees = b.getEffectsList(victim);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof EffectReceivedEffect && InvokeEffect.isActiveEffect(invokee)) {
                    EffectReceivedEffect effect = (EffectReceivedEffect)invokee;
                    effect.receiveEffect(b, caster, victim, effectType);
                }
            }
        }
    }

    public interface StatusReceivedEffect {
        void receiveStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition statusType);

        static void invokeStatusReceivedEffect(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition statusType) {
            List<InvokeEffect> invokees = b.getEffectsList(victim);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof StatusReceivedEffect && InvokeEffect.isActiveEffect(invokee)) {
                    StatusReceivedEffect effect = (StatusReceivedEffect)invokee;
                    effect.receiveStatus(b, caster, victim, statusType);
                }
            }
        }
    }

    public interface OpponentStatusReceivedEffect {
        void receiveStatus(Battle b, ActivePokemon victim, StatusCondition statusType);

        static void invokeOpponentStatusReceivedEffect(Battle b, ActivePokemon victim, StatusCondition statusType) {
            List<InvokeEffect> invokees = b.getEffectsList(b.getOtherPokemon(victim));
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof OpponentStatusReceivedEffect && InvokeEffect.isActiveEffect(invokee)) {
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
                if (invokee instanceof SleepyFightsterEffect && InvokeEffect.isActiveEffect(invokee)) {
                    return true;
                }
            }

            return false;
        }
    }

    public interface OpponentEndAttackEffect {
        void endsies(Battle b, ActivePokemon attacking);

        static void invokeOpponentEndAttackEffect(Battle b, ActivePokemon attacking) {
            List<InvokeEffect> invokees = b.getEffectsList(b.getOtherPokemon(attacking));
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof OpponentEndAttackEffect && InvokeEffect.isActiveEffect(invokee)) {
                    OpponentEndAttackEffect effect = (OpponentEndAttackEffect)invokee;
                    effect.endsies(b, attacking);
                }
            }
        }
    }

    public interface TerrainCastEffect {
        void newTerrain(Battle b, ActivePokemon p, TerrainType newTerrain);

        static void invokeTerrainCastEffect(Battle b, ActivePokemon p, TerrainType newTerrain) {
            List<InvokeEffect> invokees = b.getEffectsList(p);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof TerrainCastEffect && InvokeEffect.isActiveEffect(invokee)) {
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
            List<InvokeEffect> invokees = b.getEffectsList(victim);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof AttackBlocker && InvokeEffect.isActiveEffect(invokee)) {

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
            List<InvokeEffect> invokees = b.getEffectsList(user, user.getAttack());
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof SelfAttackBlocker && InvokeEffect.isActiveEffect(invokee)) {
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

        default void swapItems(Battle b, ActivePokemon user, ActivePokemon victim) {
            Item userItem = user.getHeldItem(b);
            Item victimItem = victim.getHeldItem(b);

            Messages.add(this.getSwitchMessage(user, userItem, victim, victimItem));

            // For wild battles, an actual switch occurs
            if (b.isWildBattle()) {
                user.giveItem((HoldItem)victimItem);
                victim.giveItem((HoldItem)userItem);
            } else {
                user.setCastSource(victimItem);
                PokemonEffectNamesies.CHANGE_ITEM.getEffect().apply(b, user, user, CastSource.CAST_SOURCE, false);

                user.setCastSource(userItem);
                PokemonEffectNamesies.CHANGE_ITEM.getEffect().apply(b, user, victim, CastSource.CAST_SOURCE, false);
            }
        }
    }

    public interface SwapOpponentEffect {
        String getSwapMessage(ActivePokemon user, ActivePokemon victim);

        default void swapOpponent(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (!user.canSwapOpponent(b, victim)) {
                return;
            }

            Messages.add(this.getSwapMessage(user, victim));

            Team opponent = b.getTrainer(victim);
            if (opponent instanceof WildPokemon) {
                // End the battle against a wild Pokemon
                Messages.add(new MessageUpdate().withUpdate(MessageUpdateType.EXIT_BATTLE));
            } else {
                Trainer trainer = (Trainer)opponent;

                // Swap to a random Pokemon!
                trainer.switchToRandom(b);
                b.enterBattle(trainer.front(), enterer -> "...and " + enterer.getName() + " was dragged out!");
            }
        }
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

    public interface PowderMove extends AttackInterface, SelfAttackBlocker {

        @Override
        default boolean block(Battle b, ActivePokemon user) {
            // Powder moves don't work against Grass-type Pokemon
            return b.getOtherPokemon(user).isType(b, Type.GRASS);
        }
    }

    public interface WildEncounterAlterer {
        void alterWildPokemon(ActivePokemon playerFront, WildEncounterInfo encounterData, WildEncounter encounter);

        static void invokeWildEncounterAlterer(ActivePokemon playerFront, WildEncounterInfo encounterData, WildEncounter encounter) {
            List<InvokeEffect> invokees = new ArrayList<>();
            invokees.add(playerFront.getAbility());
            invokees.add(playerFront.getActualHeldItem());

            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof WildEncounterAlterer && InvokeEffect.isActiveEffect(invokee)) {
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
            if (wildPokemon.getLevel() <= playerFront.getLevel() && Game.getPlayer().getRepelInfo().isUsingRepel()) {
                return true;
            }

            List<InvokeEffect> invokees = new ArrayList<>();
            invokees.add(playerFront.getAbility());
            invokees.add(playerFront.getActualHeldItem());

            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof RepellingEffect && InvokeEffect.isActiveEffect(invokee)) {
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
            List<InvokeEffect> invokees = new ArrayList<>();
            invokees.add(playerFront.getAbility());
            invokees.add(playerFront.getActualHeldItem());

            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof WildEncounterSelector && InvokeEffect.isActiveEffect(invokee)) {
                    WildEncounterSelector effect = (WildEncounterSelector)invokee;
                    WildEncounterInfo value = effect.getWildEncounter(playerFront, wildEncounters);
                    if (value != null) {
                        return value;
                    }
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

            List<InvokeEffect> invokees = new ArrayList<>();
            invokees.add(playerFront.getAbility());
            invokees.add(playerFront.getActualHeldItem());

            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof EncounterRateMultiplier && InvokeEffect.isActiveEffect(invokee)) {
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

            List<InvokeEffect> invokees = b.getEffectsList(victim);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof ModifyStageValueEffect && InvokeEffect.isActiveEffect(invokee)) {

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

    public interface StrikeFirstEffect {

        // Returns if the Pokemon should go first within its priority bracket
        boolean strikeFirst(Battle b, ActivePokemon striker);
        String getStrikeFirstMessage(ActivePokemon striker);

        static boolean checkStrikeFirst(Battle b, ActivePokemon striker) {
            List<InvokeEffect> invokees = b.getEffectsList(striker);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof StrikeFirstEffect && InvokeEffect.isActiveEffect(invokee)) {
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

    public interface WeatherEliminatingEffect extends EntryEndTurnEffect {
        String getEliminateMessage(ActivePokemon eliminator);

        default boolean eliminateWeather(WeatherEffect weather) {
            return weather.namesies() != WeatherNamesies.CLEAR_SKIES;
        }

        @Override
        default void applyEffect(Battle b, ActivePokemon p) {
            b.addEffect(b.getWeather());
        }

        static boolean shouldEliminateWeather(Battle b, ActivePokemon eliminator, WeatherEffect weather) {
            List<InvokeEffect> invokees = b.getEffectsList(eliminator);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof WeatherEliminatingEffect && InvokeEffect.isActiveEffect(invokee)) {
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
            List<InvokeEffect> invokees = switchee.getAllEffects(null);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof SwitchOutEffect && InvokeEffect.isActiveEffect(invokee)) {
                    SwitchOutEffect effect = (SwitchOutEffect)invokee;
                    effect.switchOut(switchee);
                }
            }
        }
    }

    public interface WeatherExtendingEffect {
        int getExtensionTurns(WeatherNamesies weatherType);

        static int getModifier(Battle b, ActivePokemon p, WeatherNamesies weatherType) {
            int modifier = 0;

            List<InvokeEffect> invokees = b.getEffectsList(p);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof WeatherExtendingEffect && InvokeEffect.isActiveEffect(invokee)) {
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
                user.reduceHealth(b, sapAmount, false);
                return;
            }

            // Healers gon' heal
            if (!user.hasEffect(PokemonEffectNamesies.HEAL_BLOCK)) {
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

    public interface PowerCountMove extends AttackInterface, PowerChangeEffect {
        boolean doubleDefenseCurled();

        @Override
        default double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return Math.min(user.getCount(), 5)*(this.doubleDefenseCurled() && user.hasEffect(PokemonEffectNamesies.USED_DEFENSE_CURL) ? 2 : 1);
        }
    }

    public interface ItemBlockerEffect {

        static boolean containsItemBlockerEffect(Battle b, ActivePokemon p) {
            // Don't include the item because then it's all like ahhhhhh
            List<InvokeEffect> invokees = b.getEffectsList(p, false);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof ItemBlockerEffect && InvokeEffect.isActiveEffect(invokee)) {
                    return true;
                }
            }

            return false;
        }
    }

    public interface OpponentItemBlockerEffect {
        boolean blockItem(Battle b, ActivePokemon opp, ItemNamesies item);

        static boolean checkOpponentItemBlockerEffect(Battle b, ActivePokemon opp, ItemNamesies item) {
            // Don't include the item because then it's all like ahhhhhh
            List<InvokeEffect> invokees = b.getEffectsList(opp, false);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof OpponentItemBlockerEffect && InvokeEffect.isActiveEffect(invokee)) {
                    OpponentItemBlockerEffect effect = (OpponentItemBlockerEffect)invokee;
                    if (effect.blockItem(b, opp, item)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public interface StallingEffect {

        static boolean containsStallingEffect(Battle b, ActivePokemon p) {
            List<InvokeEffect> invokees = b.getEffectsList(p);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof StallingEffect && InvokeEffect.isActiveEffect(invokee)) {
                    return true;
                }
            }

            return false;
        }
    }

    public interface DefiniteEscape extends InvokeEffect {

        default String getEscapeMessage(Battle b, ActivePokemon sourcerer) {
            CastSource source = this.getSource().getCastSource();
            if (source.hasSourceName()) {
                return sourcerer.getName() + "'s " + source.getSourceName(b, sourcerer) + " allowed it to escape!";
            }

            return "Got away safely!";
        }

        default boolean canEscape(Battle b, ActivePokemon p) {
            return true;
        }

        static boolean canDefinitelyEscape(Battle b, ActivePokemon p) {
            List<InvokeEffect> invokees = b.getEffectsList(p);
            for (InvokeEffect invokee : invokees) {
                if (invokee instanceof DefiniteEscape && InvokeEffect.isActiveEffect(invokee)) {
                    DefiniteEscape effect = (DefiniteEscape)invokee;
                    if (effect.canEscape(b, p)) {
                        Messages.add(effect.getEscapeMessage(b, p));
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public interface PassableEffect {
    }
}
