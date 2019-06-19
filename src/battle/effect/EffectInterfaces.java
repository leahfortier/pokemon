package battle.effect;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.Attack;
import battle.attack.AttackInterface;
import battle.attack.MoveType;
import battle.effect.InvokeInterfaces.AttackBlocker;
import battle.effect.InvokeInterfaces.AttackSelectionEffect;
import battle.effect.InvokeInterfaces.BasicAccuracyBypassEffect;
import battle.effect.InvokeInterfaces.CrashDamageMove;
import battle.effect.InvokeInterfaces.EffectExtendingEffect;
import battle.effect.InvokeInterfaces.EffectPreventionEffect;
import battle.effect.InvokeInterfaces.EndTurnEffect;
import battle.effect.InvokeInterfaces.EntryEffect;
import battle.effect.InvokeInterfaces.OpponentApplyDamageEffect;
import battle.effect.InvokeInterfaces.PowerChangeEffect;
import battle.effect.InvokeInterfaces.RapidSpinRelease;
import battle.effect.InvokeInterfaces.RepellingEffect;
import battle.effect.InvokeInterfaces.SelfAttackBlocker;
import battle.effect.InvokeInterfaces.StatModifyingEffect;
import battle.effect.InvokeInterfaces.StatusPreventionEffect;
import battle.effect.InvokeInterfaces.TrappingEffect;
import battle.effect.InvokeInterfaces.WildEncounterAlterer;
import battle.effect.InvokeInterfaces.WildEncounterSelector;
import battle.effect.battle.weather.WeatherNamesies;
import battle.effect.pokemon.PokemonEffect;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.source.CastSource;
import battle.effect.status.StatusNamesies;
import item.ItemNamesies;
import item.hold.HoldItem;
import main.Global;
import map.overworld.wild.WildEncounter;
import map.overworld.wild.WildEncounterInfo;
import message.MessageUpdate;
import message.MessageUpdateType;
import message.Messages;
import pokemon.Stat;
import pokemon.ability.AbilityInterface;
import pokemon.ability.AbilityNamesies;
import pokemon.species.PokemonInfo;
import trainer.Team;
import trainer.Trainer;
import trainer.WildPokemon;
import type.Type;
import util.RandomUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Holds non-generated interface methods for InvokeEffects
// These should not have invoke methods as they are created manually
public final class EffectInterfaces {

    // Class to hold interfaces -- should not be instantiated
    private EffectInterfaces() {
        Global.error(this.getClass().getSimpleName() + " class cannot be instantiated.");
    }

    public interface MessageGetter {
        String getGenericMessage(ActivePokemon p);
        String getSourceMessage(ActivePokemon p, String sourceName);

        default String getMessage(Battle b, ActivePokemon p, CastSource source) {
            if (source.hasSourceName()) {
                return this.getSourceMessage(p, source.getSourceName(b, p));
            } else {
                return this.getGenericMessage(p);
            }
        }
    }

    public interface ItemSwapperEffect {
        String getSwitchMessage(ActivePokemon user, HoldItem userItem, ActivePokemon victim, HoldItem victimItem);

        default void swapItems(Battle b, ActivePokemon user, ActivePokemon victim) {
            HoldItem userItem = user.getHeldItem(b);
            HoldItem victimItem = victim.getHeldItem(b);

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

    public interface PassableEffect {}

    public interface PhysicalContactEffect extends OpponentApplyDamageEffect {

        // b: The current battle
        // user: The user of the attack that caused the physical contact
        // victim: The Pokemon that received the physical contact attack
        void contact(Battle b, ActivePokemon user, ActivePokemon victim);

        @Override
        default void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
            // Only apply if physical contact is made
            if (user.isMakingContact()) {
                this.contact(b, user, victim);
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
            if (user.isHoldingItem(b, ItemNamesies.BIG_ROOT)) {
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
            double fraction = b.getOtherPokemon(victim).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0;
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
        default boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusNamesies status) {
            return this.getStatus().getStatus().isType(status);
        }

        @Override
        default String statusPreventionMessage(ActivePokemon victim) {
            return this.getStatus().getStatus().getSourcePreventionMessage(victim, this.getName());
        }

        @Override
        default void applyEffect(Battle b, ActivePokemon p) {
            if (p.hasStatus(this.getStatus())) {
                p.removeStatus(b, CastSource.ABILITY);
            }
        }
    }

    public interface MultipleEffectPreventionAbility extends AbilityInterface, EffectPreventionEffect, EntryEndTurnEffect {
        // Returns a map from preventable effect to relevant error message
        Map<PokemonEffectNamesies, String> getPreventableEffects();

        default String getEffectMessage(EffectNamesies effectName) {
            Map<PokemonEffectNamesies, String> preventableEffects = this.getPreventableEffects();
            if (effectName instanceof PokemonEffectNamesies && preventableEffects.containsKey(effectName)) {
                return preventableEffects.get(effectName);
            }

            Global.error("Should only be called for a valid effect name " + effectName);
            return "";
        }

        @Override
        default ApplyResult preventEffect(Battle b, ActivePokemon caster, ActivePokemon victim, EffectNamesies effectName) {
            if (effectName instanceof PokemonEffectNamesies && this.getPreventableEffects().containsKey(effectName)) {
                return ApplyResult.failure(victim.getName() + "'s " + this.getName() + " prevents " + this.getEffectMessage(effectName) + "!");
            }
            return ApplyResult.success();
        }

        @Override
        default void applyEffect(Battle b, ActivePokemon p) {
            Map<PokemonEffectNamesies, String> preventableEffects = this.getPreventableEffects();
            for (PokemonEffectNamesies effectName : preventableEffects.keySet()) {
                if (p.hasEffect(effectName)) {
                    // TODO: Not the correct message
                    PokemonEffect effect = p.getEffect(effectName);
                    Messages.add(effect.getSubsideMessage(p));
                    p.getEffects().remove(effect);
                }
            }
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
}
