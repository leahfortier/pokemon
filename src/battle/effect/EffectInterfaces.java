package battle.effect;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.AttackInterface;
import battle.attack.Move;
import battle.effect.InvokeInterfaces.AttackBlocker;
import battle.effect.InvokeInterfaces.AttackSelectionEffect;
import battle.effect.InvokeInterfaces.BasicAccuracyBypassEffect;
import battle.effect.InvokeInterfaces.CrashDamageMove;
import battle.effect.InvokeInterfaces.DefogRelease;
import battle.effect.InvokeInterfaces.EffectExtendingEffect;
import battle.effect.InvokeInterfaces.EffectPreventionEffect;
import battle.effect.InvokeInterfaces.EndTurnEffect;
import battle.effect.InvokeInterfaces.EntryEffect;
import battle.effect.InvokeInterfaces.OpponentApplyDamageEffect;
import battle.effect.InvokeInterfaces.PowerChangeEffect;
import battle.effect.InvokeInterfaces.RapidSpinRelease;
import battle.effect.InvokeInterfaces.RepellingEffect;
import battle.effect.InvokeInterfaces.SelfAttackBlocker;
import battle.effect.InvokeInterfaces.SemiInvulnerableBypasser;
import battle.effect.InvokeInterfaces.StatModifyingEffect;
import battle.effect.InvokeInterfaces.StatusBoosterEffect;
import battle.effect.InvokeInterfaces.StatusPreventionEffect;
import battle.effect.InvokeInterfaces.TakeDamageEffect;
import battle.effect.InvokeInterfaces.TrappingEffect;
import battle.effect.InvokeInterfaces.WildEncounterAlterer;
import battle.effect.InvokeInterfaces.WildEncounterSelector;
import battle.effect.battle.terrain.TerrainNamesies;
import battle.effect.battle.weather.WeatherNamesies;
import battle.effect.pokemon.PokemonEffect;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.source.CastSource;
import battle.effect.source.ChangeAbilitySource;
import battle.effect.status.StatusInterface;
import battle.effect.status.StatusNamesies;
import item.ItemNamesies;
import item.hold.HoldItem;
import main.Global;
import map.overworld.wild.WildEncounter;
import map.overworld.wild.WildEncounterInfo;
import message.MessageUpdate;
import message.MessageUpdateType;
import message.Messages;
import pokemon.ability.Ability;
import pokemon.ability.AbilityInterface;
import pokemon.ability.AbilityNamesies;
import pokemon.species.PokemonInfo;
import pokemon.species.PokemonNamesies;
import pokemon.stat.Stat;
import trainer.Team;
import trainer.Trainer;
import trainer.WildPokemon;
import type.Type;
import util.RandomUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

// Holds non-generated interface methods for InvokeEffects
// These should not have invoke methods as they are created manually
public final class EffectInterfaces {

    // Class to hold interfaces -- should not be instantiated
    private EffectInterfaces() {
        Global.error(this.getClass().getSimpleName() + " class cannot be instantiated.");
    }

    public interface IntegerHolder {
        int getInteger();
    }

    public interface BooleanHolder {
        boolean getBoolean();
    }

    public interface AbilityHolder {
        Ability getAbility();
    }

    public interface ItemHolder {
        HoldItem getItem();
    }

    public interface PokemonHolder {
        PokemonNamesies getPokemon();
    }

    public interface ItemListHolder {
        List<ItemNamesies> getItems();
    }

    public interface MessageGetter {
        String getGenericMessage(ActivePokemon p);
        String getSourceMessage(ActivePokemon p, String sourceName);

        default String getMessage(ActivePokemon p, CastSource source) {
            if (source.hasSourceName()) {
                return this.getSourceMessage(p, source.getSourceName(p));
            } else {
                return this.getGenericMessage(p);
            }
        }
    }

    public interface ItemSwapperEffect {
        String getSwitchMessage(ActivePokemon user, HoldItem userItem, ActivePokemon victim, HoldItem victimItem);

        default void swapItems(Battle b, ActivePokemon user, ActivePokemon victim) {
            HoldItem userItem = user.getHeldItem();
            HoldItem victimItem = victim.getHeldItem();

            Messages.add(this.getSwitchMessage(user, userItem, victim, victimItem));

            // For wild battles, an actual switch occurs
            if (b.isWildBattle()) {
                user.giveItem(victimItem);
                victim.giveItem(userItem);
            } else {
                user.setCastSource(victimItem);
                Effect.apply(PokemonEffectNamesies.CHANGE_ITEM, b, user, user, CastSource.CAST_SOURCE, false);

                user.setCastSource(userItem);
                Effect.apply(PokemonEffectNamesies.CHANGE_ITEM, b, user, victim, CastSource.CAST_SOURCE, false);
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

    // Pokemon Effects that can be passed with Baton Pass
    public interface PassableEffect extends EffectInterface {}

    // Team Effects that can be swapped with Court Change
    public interface SwappableEffect extends EffectInterface {}

    // Pangoro breaks the mold!
    public interface MoldBreakerEffect {}

    public interface EntryHazard extends SwappableEffect, EntryEffect, RapidSpinRelease, DefogRelease {
        String getReleaseMessage();

        @Override
        default String getRapidSpinReleaseMessage(ActivePokemon released) {
            return this.getReleaseMessage();
        }

        @Override
        default String getDefogReleaseMessage() {
            return this.getReleaseMessage();
        }
    }

    // For effects that take place when applying or receiving damage
    public interface OnDamageEffect {
        // By default, the effect should be ignored if the damage was absorbed (Substitute, Disguise, etc)
        default boolean ignoreAbsorbedDamage() {
            return true;
        }

        default boolean shouldIgnore(ActivePokemon victim) {
            return this.ignoreAbsorbedDamage() && victim.hasAbsorbedDamage();
        }
    }

    public interface PhysicalContactEffect extends OpponentApplyDamageEffect {

        // b: The current battle
        // user: The user of the attack that caused the physical contact
        // victim: The Pokemon that received the physical contact attack
        void contact(Battle b, ActivePokemon user, ActivePokemon victim);

        @Override
        default void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Only apply if physical contact is made
            if (user.isMakingContact()) {
                this.contact(b, user, victim);
            }
        }
    }

    public interface TakenUnderHalfEffect extends TakeDamageEffect {
        void takenUnderHalf(Battle b, ActivePokemon user, ActivePokemon victim);

        @Override
        default void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (victim.getHPRatio() < .5 && (victim.getHP() + victim.getDamageTaken())/(double)victim.getMaxHP() >= .5) {
                this.takenUnderHalf(b, user, victim);
            }
        }
    }

    public interface PowderMove extends AttackInterface, SelfAttackBlocker {

        @Override
        default boolean block(Battle b, ActivePokemon user) {
            // Powder moves don't work against Grass-type Pokemon
            return b.getOtherPokemon(user).isType(b, Type.GRASS);
        }
    }

    public interface ProtectingEffect extends AttackBlocker {
        default void protectingEffects(Battle b, ActivePokemon p, ActivePokemon opp) {}

        default boolean protectingCondition(Battle b, ActivePokemon attacking) {
            return true;
        }

        @Override
        default boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            return protectingCondition(b, user) && user.getAttack().isProtectAffected();
        }

        @Override
        default void alternateEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            CrashDamageMove.invokeCrashDamageMove(b, user);
            this.protectingEffects(b, user, victim);
        }

        @Override
        default String getBlockMessage(ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + " is protecting itself!";
        }
    }

    public interface RepelLowLevelEncounterEffect extends RepellingEffect {

        @Override
        default boolean shouldRepel(ActivePokemon playerFront, WildEncounter wildPokemon) {
            return RandomUtils.chanceTest(50) && wildPokemon.getLevel() + 5 <= playerFront.getLevel();
        }
    }

    public interface SapHealthEffect {

        default double sapPercentage() {
            return .5;
        }

        default String getSapMessage(ActivePokemon victim) {
            return victim.getName() + "'s health was sapped!";
        }

        default void sapHealth(Battle b, ActivePokemon user, ActivePokemon victim, int damageAmount, boolean print) {
            int sapAmount = (int)Math.ceil(damageAmount*this.sapPercentage());
            if (sapAmount == 0) {
                return;
            }

            // Big Root heals an additional 30%
            if (user.isHoldingItem(ItemNamesies.BIG_ROOT)) {
                sapAmount *= 1.3;
            }

            // Oozy makes you woozy
            if (victim.hasAbility(AbilityNamesies.LIQUID_OOZE)) {
                user.indirectReduceHealth(
                        b, sapAmount, false,
                        victim.getName() + "'s " + AbilityNamesies.LIQUID_OOZE.getName()
                                + " caused " + user.getName() + " to lose health instead!"
                );
                return;
            }

            // Healers gon' heal
            user.heal(sapAmount, b, print ? this.getSapMessage(victim) : "");
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

    public interface TypedWildEncounterSelector extends WildEncounterSelector {
        Type getEncounterType();

        @Override
        default WildEncounterInfo getWildEncounter(WildEncounterInfo[] wildEncounters) {
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

    public interface AttackSelectionSelfBlockerEffect extends AttackSelectionEffect, SelfAttackBlocker {

        @Override
        default boolean block(Battle b, ActivePokemon user) {
            return !this.usable(b, user, user.getMove());
        }
    }

    public interface EndTurnSubsider extends EffectInterface, EndTurnEffect {
        void endTurnNoSubside(Battle b, ActivePokemon victim);

        @Override
        default void applyEndTurn(ActivePokemon victim, Battle b) {
            if (this.getTurns() == 1) {
                Messages.add(this.getSubsideMessage(victim));
                this.deactivate();
                return;
            }

            this.endTurnNoSubside(b, victim);
        }
    }

    public interface PartialTrappingEffect extends EndTurnSubsider, TrappingEffect, RapidSpinRelease {
        String getReduceMessage(ActivePokemon victim);

        @Override
        default void endTurnNoSubside(Battle b, ActivePokemon victim) {
            // Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
            double fraction = b.getOtherPokemon(victim).isHoldingItem(ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0;
            victim.reduceHealthFraction(b, fraction, this.getReduceMessage(victim));
        }
    }

    public interface WeatherExtendingEffect extends EffectExtendingEffect {
        WeatherNamesies getWeatherType();

        @Override
        default int getExtensionTurns(Effect receivedEffect, int numTurns) {
            return receivedEffect.namesies() == this.getWeatherType() ? 3 : 0;
        }
    }

    public interface StatusPreventionAbility extends AbilityInterface, StatusPreventionEffect, EntryEndTurnEffect {
        StatusNamesies getStatus();

        @Override
        default ApplyResult preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusNamesies status) {
            if (this.getStatus().getStatus().isType(status)) {
                return ApplyResult.failure(this.getStatus().getStatus().getSourcePreventionMessage(victim, this.getName()));
            }

            return ApplyResult.success();
        }

        @Override
        default void applyEffect(Battle b, ActivePokemon p) {
            if (p.hasStatus(this.getStatus())) {
                p.removeStatus(b, CastSource.ABILITY);
            }
        }
    }

    public interface EffectPreventionAbility extends AbilityInterface, EffectPreventionEffect, EntryEndTurnEffect {
        Iterable<PokemonEffectNamesies> getPreventableEffects();
        boolean isPreventableEffect(EffectNamesies effectNamesies);

        @Override
        default ApplyResult preventEffect(Battle b, ActivePokemon caster, ActivePokemon victim, EffectNamesies effectName, CastSource source) {
            if (this.isPreventableEffect(effectName)) {
                return ApplyResult.failure(effectName.getEffect().getSourcePreventMessage(victim, this.getName()));
            }

            return ApplyResult.success();
        }

        @Override
        default void applyEffect(Battle b, ActivePokemon p) {
            // If the victim was able to receive the effect (mold breaker, ability change, etc.), remove it at the end of turn
            for (PokemonEffectNamesies effectName : this.getPreventableEffects()) {
                if (p.hasEffect(effectName)) {
                    PokemonEffect effect = p.getEffect(effectName);
                    Messages.add(effect.getSourceRemoveMessage(p, this.getName()));
                    effect.deactivate();
                }
            }
        }
    }

    public interface SingleEffectPreventionAbility extends EffectPreventionAbility {
        PokemonEffectNamesies getPreventableEffect();

        @Override
        default boolean isPreventableEffect(EffectNamesies effectNamesies) {
            return effectNamesies == this.getPreventableEffect();
        }

        @Override
        default Iterable<PokemonEffectNamesies> getPreventableEffects() {
            return Collections.singletonList(this.getPreventableEffect());
        }
    }

    public interface MultipleEffectPreventionAbility extends EffectPreventionAbility {
        @Override
        Set<PokemonEffectNamesies> getPreventableEffects();

        @Override
        default boolean isPreventableEffect(EffectNamesies effectNamesies) {
            return effectNamesies instanceof PokemonEffectNamesies && this.getPreventableEffects().contains(effectNamesies);
        }
    }

    public interface DoubleMinimizerMove extends AttackInterface, BasicAccuracyBypassEffect, PowerChangeEffect {
        @Override
        default boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            return defending.hasEffect(PokemonEffectNamesies.USED_MINIMIZE);
        }

        @Override
        default double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.hasEffect(PokemonEffectNamesies.USED_MINIMIZE) ? 2 : 1;
        }
    }

    // Boosts a stat when has a status condition
    public interface StatStatusBoosterEffect extends StatusBoosterEffect, SimpleStatModifyingEffect {
        @Override
        default boolean statusBooster(Stat stat) {
            return this.isModifyStat(stat);
        }

        @Override
        default boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return p.hasStatus();
        }
    }

    // Boosts power when has a status condition
    public interface PowerStatusBoosterEffect extends StatusBoosterEffect, PowerChangeEffect {
        double getStatusBoost();

        @Override
        default boolean statusBooster(Stat stat) {
            return stat.isAttackingStat();
        }

        @Override
        default double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.hasStatus() ? this.getStatusBoost() : 1;
        }
    }

    public interface StatModifyingStatus extends StatusInterface, SimpleStatModifyingEffect {
        @Override
        default double modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s) {
            if (StatusBoosterEffect.isStatusBooster(b, p, s)) {
                return 1;
            } else {
                return SimpleStatModifyingEffect.super.modify(b, p, opp, s);
            }
        }
    }

    // Always hit when the opponent is digging
    public interface SemiInvulnerableDiggingBypasser extends SemiInvulnerableBypasser {
        @Override
        default boolean semiInvulnerableBypass(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // Always hit when the opponent is underground
            return defending.isSemiInvulnerableDigging();
        }
    }

    // Always hit and twice as strong when the opponent is digging
    // Power is halved by Grassy Terrain
    public interface DoubleDigger extends SemiInvulnerableDiggingBypasser, PowerChangeEffect {
        @Override
        default double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            double multiplier = 1;

            // Power is halved during Grassy Terrain
            if (b.hasEffect(TerrainNamesies.GRASSY_TERRAIN)) {
                multiplier *= .5;
            }

            // Power is doubled when the opponent is underground
            if (victim.isSemiInvulnerableDigging()) {
                multiplier *= 2;
            }

            return multiplier;
        }
    }

    // Always hit and twice as strong when the opponent is diving
    public interface DoubleDiver extends SemiInvulnerableBypasser, PowerChangeEffect {
        @Override
        default double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.isSemiInvulnerableDiving() ? 2 : 1;
        }

        @Override
        default boolean semiInvulnerableBypass(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            return defending.isSemiInvulnerableDiving();
        }
    }

    // Always hit when the opponent is flying
    public interface SemiInvulnerableFlyingBypasser extends SemiInvulnerableBypasser {
        @Override
        default boolean semiInvulnerableBypass(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            return defending.isSemiInvulnerableFlying();
        }
    }

    // Always hit and twice as strong when the opponent is flying
    public interface DoubleFlyer extends SemiInvulnerableFlyingBypasser, PowerChangeEffect {
        @Override
        default double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.isSemiInvulnerableFlying() ? 2 : 1;
        }
    }

    // Accuracy is only 50% when sunny and always hits in the rain
    // Always hit and twice as strong when the opponent is flying
    public interface StormyMove extends AttackInterface, DoubleFlyer, BasicAccuracyBypassEffect {
        @Override
        default int getAccuracy(Battle b, ActivePokemon me, ActivePokemon o) {
            if (b.isWeather(WeatherNamesies.SUNNY)) {
                return 50;
            }

            return this.getBaseAccuracy();
        }

        @Override
        default boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            return b.isWeather(WeatherNamesies.RAINING);
        }
    }

    // Used for trapping effects that are active so long as a specific Pokemon is still in play
    public interface LockingEffect extends EffectInterface, TrappingEffect {
        // Which Pokemon the locked effect is dependent upon
        List<ActivePokemon> getLocking();

        @Override
        default boolean trapped(Battle b, ActivePokemon escaper) {
            // Check if Pokemon are still locked before dooming them to a trapped lifestyle
            return this.checkActive(b);
        }

        // Checks if Pokemon are still locked, and deactivates and returns false if not
        default boolean checkActive(Battle b) {
            // If any Pokemon is no longer locked, then none of them are
            for (ActivePokemon p : this.getLocking()) {
                if (this.unlocked(b, p)) {
                    this.deactivate();
                    return false;
                }
            }

            // Still locked up (all Pokemon are alive and out front)
            return true;
        }

        // Returns true if the Pokemon is no longer locked
        // Confirms this by the Pokemon being dead or not the front Pokemon
        private boolean unlocked(Battle b, ActivePokemon p) {
            return p.isFainted(b) || !b.isFront(p);
        }
    }

    // Used for effects which boost a stat by 50%, but only allow the first selected move to be used
    public interface ChoiceEffect extends AttackSelectionEffect, SimpleStatModifyingEffect {
        Stat getBoosted();
        String getName();

        @Override
        default boolean isModifyStat(Stat s) {
            return s == this.getBoosted();
        }

        @Override
        default double getModifier() {
            return 1.5;
        }

        @Override
        default boolean usable(Battle b, ActivePokemon p, Move m) {
            // Note: Because this is just using the last move used and not actually storing the move
            // or anything like that it will break if it gets Struggled (from something like Torment)
            // and will be locked into Struggle for the rest of the fight
            Move last = p.getLastMoveUsed();
            return last == null || m == last;
        }

        @Override
        default String getUnusableMessage(ActivePokemon p) {
            return p.getName() + "'s " + this.getName() + " only allows " + p.getLastMoveUsed().getAttack().getName() + " to be used!";
        }
    }

    public interface FormAbility extends AbilityInterface {
        // Adds the message with the form image change
        default void addFormMessage(ActivePokemon formsie, String message, boolean form) {
            boolean isPlayer = formsie.isPlayer();
            boolean shiny = formsie.isShiny();
            boolean front = !isPlayer;
            String imageName = formsie.getPokemonInfo().getImageName(shiny, front, form);
            Messages.add(new MessageUpdate(message).withImageName(imageName, isPlayer));
        }
    }

    public interface AbilitySwapper extends ChangeAbilitySource, InvokeEffect {
        void setAbility(Ability ability);

        // Can swap if both abilities are replaceable and stealable and are not the same ability
        default boolean canSwapAbilities(ActivePokemon user, ActivePokemon victim) {
            Ability userAbility = user.getAbility();
            Ability victimAbility = victim.getAbility();
            return userAbility.namesies() != victimAbility.namesies()
                    && this.canSwapAbility(userAbility)
                    && this.canSwapAbility(victimAbility);
        }

        default boolean canSwapAbility(Ability ability) {
            return ability.isReplaceable() && ability.isStealable();
        }

        default void swapAbilities(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (!this.canSwapAbilities(user, victim)) {
                return;
            }

            Ability userAbility = user.getAbility();
            Ability victimAbility = victim.getAbility();
            CastSource source = this.getSource().getCastSource();

            this.setAbility(userAbility);
            Effect.cast(PokemonEffectNamesies.CHANGE_ABILITY, b, user, victim, source, true);

            this.setAbility(victimAbility);
            Effect.cast(PokemonEffectNamesies.CHANGE_ABILITY, b, user, user, source, true);
        }
    }
}
