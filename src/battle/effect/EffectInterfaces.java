package battle.effect;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.Attack;
import battle.attack.AttackInterface;
import battle.attack.MoveType;
import battle.effect.InvokeInterfaces.ApplyDamageEffect;
import battle.effect.InvokeInterfaces.AttackBlocker;
import battle.effect.InvokeInterfaces.AttackSelectionEffect;
import battle.effect.InvokeInterfaces.CrashDamageMove;
import battle.effect.InvokeInterfaces.EndTurnEffect;
import battle.effect.InvokeInterfaces.EntryEffect;
import battle.effect.InvokeInterfaces.OpponentApplyDamageEffect;
import battle.effect.InvokeInterfaces.RepellingEffect;
import battle.effect.InvokeInterfaces.SelfAttackBlocker;
import battle.effect.InvokeInterfaces.StatModifyingEffect;
import battle.effect.InvokeInterfaces.WildEncounterAlterer;
import battle.effect.InvokeInterfaces.WildEncounterSelector;
import battle.effect.pokemon.PokemonEffect;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.source.CastSource;
import battle.effect.team.TeamEffect;
import item.ItemNamesies;
import item.hold.HoldItem;
import main.Global;
import map.overworld.wild.WildEncounter;
import map.overworld.wild.WildEncounterInfo;
import message.MessageUpdate;
import message.MessageUpdateType;
import message.Messages;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import pokemon.species.PokemonInfo;
import trainer.Team;
import trainer.Trainer;
import trainer.WildPokemon;
import type.Type;
import util.RandomUtils;

import java.util.ArrayList;
import java.util.List;

// Holds non-generated interface methods for InvokeEffects
// These should not have invoke methods as they are created manually
public final class EffectInterfaces {

    // Class to hold interfaces -- should not be instantiated
    private EffectInterfaces() {
        Global.error(this.getClass().getSimpleName() + " class cannot be instantiated.");
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
                PokemonEffectNamesies.CHANGE_ITEM.getEffect().apply(b, user, user, CastSource.CAST_SOURCE, false);

                user.setCastSource(userItem);
                PokemonEffectNamesies.CHANGE_ITEM.getEffect().apply(b, user, victim, CastSource.CAST_SOURCE, false);
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
            if (user.getAttack().isMoveType(MoveType.PHYSICAL_CONTACT) && !user.hasAbility(AbilityNamesies.LONG_REACH)) {
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

    public interface SapHealthEffect extends ApplyDamageEffect {

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

        @Override
        default void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
            this.sapHealth(b, user, victim, damage, true);
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

    public interface AttackSelectionSelfBlockerEffect extends AttackSelectionEffect, SelfAttackBlocker {

        @Override
        default boolean block(Battle b, ActivePokemon user) {
            return !this.usable(b, user, user.getMove());
        }
    }
}
