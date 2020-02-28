package pokemon.ability;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.attack.MoveCategory;
import battle.attack.MoveType;
import battle.effect.ApplyResult;
import battle.effect.Effect;
import battle.effect.EffectInterfaces.BooleanHolder;
import battle.effect.EffectInterfaces.ChoiceEffect;
import battle.effect.EffectInterfaces.FormAbility;
import battle.effect.EffectInterfaces.ItemHolder;
import battle.effect.EffectInterfaces.ItemListHolder;
import battle.effect.EffectInterfaces.ItemSwapperEffect;
import battle.effect.EffectInterfaces.MaxLevelWildEncounterEffect;
import battle.effect.EffectInterfaces.MoldBreakerEffect;
import battle.effect.EffectInterfaces.MultipleEffectPreventionAbility;
import battle.effect.EffectInterfaces.PhysicalContactEffect;
import battle.effect.EffectInterfaces.RepelLowLevelEncounterEffect;
import battle.effect.EffectInterfaces.SimpleStatModifyingEffect;
import battle.effect.EffectInterfaces.SingleEffectPreventionAbility;
import battle.effect.EffectInterfaces.StatStatusBoosterEffect;
import battle.effect.EffectInterfaces.StatusPreventionAbility;
import battle.effect.EffectInterfaces.SwapOpponentEffect;
import battle.effect.EffectInterfaces.TypedWildEncounterSelector;
import battle.effect.EffectNamesies;
import battle.effect.InvokeInterfaces.AbsorbDamageEffect;
import battle.effect.InvokeInterfaces.AlwaysCritEffect;
import battle.effect.InvokeInterfaces.ApplyDamageEffect;
import battle.effect.InvokeInterfaces.AttackBlocker;
import battle.effect.InvokeInterfaces.AttackingNoAdvantageChanger;
import battle.effect.InvokeInterfaces.BeforeAttackPreventingEffect;
import battle.effect.InvokeInterfaces.BracingEffect;
import battle.effect.InvokeInterfaces.ChangeAttackTypeEffect;
import battle.effect.InvokeInterfaces.ChangeTypeEffect;
import battle.effect.InvokeInterfaces.CrashDamageMove;
import battle.effect.InvokeInterfaces.CritBlockerEffect;
import battle.effect.InvokeInterfaces.CritStageEffect;
import battle.effect.InvokeInterfaces.DefiniteEscape;
import battle.effect.InvokeInterfaces.DifferentStatEffect;
import battle.effect.InvokeInterfaces.DoubleWeightEffect;
import battle.effect.InvokeInterfaces.EffectChanceMultiplierEffect;
import battle.effect.InvokeInterfaces.EffectPreventionEffect;
import battle.effect.InvokeInterfaces.EffectReceivedEffect;
import battle.effect.InvokeInterfaces.EncounterRateMultiplier;
import battle.effect.InvokeInterfaces.EndBattleEffect;
import battle.effect.InvokeInterfaces.EndTurnEffect;
import battle.effect.InvokeInterfaces.EntryEffect;
import battle.effect.InvokeInterfaces.FaintEffect;
import battle.effect.InvokeInterfaces.HalfWeightEffect;
import battle.effect.InvokeInterfaces.ItemBlockerEffect;
import battle.effect.InvokeInterfaces.LevitationEffect;
import battle.effect.InvokeInterfaces.ModifyStageValueEffect;
import battle.effect.InvokeInterfaces.MurderEffect;
import battle.effect.InvokeInterfaces.NameChanger;
import battle.effect.InvokeInterfaces.NoSwapEffect;
import battle.effect.InvokeInterfaces.OpponentAccuracyBypassEffect;
import battle.effect.InvokeInterfaces.OpponentApplyDamageEffect;
import battle.effect.InvokeInterfaces.OpponentEndAttackEffect;
import battle.effect.InvokeInterfaces.OpponentIgnoreStageEffect;
import battle.effect.InvokeInterfaces.OpponentItemBlockerEffect;
import battle.effect.InvokeInterfaces.OpponentPowerChangeEffect;
import battle.effect.InvokeInterfaces.OpponentStatusReceivedEffect;
import battle.effect.InvokeInterfaces.OpponentTakeDamageEffect;
import battle.effect.InvokeInterfaces.OpponentTrappingEffect;
import battle.effect.InvokeInterfaces.PowderBlocker;
import battle.effect.InvokeInterfaces.PowerChangeEffect;
import battle.effect.InvokeInterfaces.PriorityChangeEffect;
import battle.effect.InvokeInterfaces.SelfAttackBlocker;
import battle.effect.InvokeInterfaces.SemiInvulnerableBypasser;
import battle.effect.InvokeInterfaces.SleepyFightsterEffect;
import battle.effect.InvokeInterfaces.StageChangingEffect;
import battle.effect.InvokeInterfaces.StallingEffect;
import battle.effect.InvokeInterfaces.StartAttackEffect;
import battle.effect.InvokeInterfaces.StatLoweredEffect;
import battle.effect.InvokeInterfaces.StatModifyingEffect;
import battle.effect.InvokeInterfaces.StatProtectingEffect;
import battle.effect.InvokeInterfaces.StatTargetSwapperEffect;
import battle.effect.InvokeInterfaces.StatusPreventionEffect;
import battle.effect.InvokeInterfaces.StatusReceivedEffect;
import battle.effect.InvokeInterfaces.StickyHoldEffect;
import battle.effect.InvokeInterfaces.SuperDuperEndTurnEffect;
import battle.effect.InvokeInterfaces.SwitchOutEffect;
import battle.effect.InvokeInterfaces.TakeDamageEffect;
import battle.effect.InvokeInterfaces.TargetSwapperEffect;
import battle.effect.InvokeInterfaces.WeatherBlockerEffect;
import battle.effect.InvokeInterfaces.WeatherChangedEffect;
import battle.effect.InvokeInterfaces.WeatherEliminatingEffect;
import battle.effect.InvokeInterfaces.WildEncounterAlterer;
import battle.effect.attack.OhkoMove;
import battle.effect.attack.RecoilMove;
import battle.effect.battle.terrain.TerrainNamesies;
import battle.effect.battle.weather.WeatherNamesies;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.source.CastSource;
import battle.effect.source.ChangeAbilitySource;
import battle.effect.source.ChangeTypeSource;
import battle.effect.status.StatusCondition;
import battle.effect.status.StatusNamesies;
import battle.stages.StageModifier;
import item.ItemNamesies;
import item.berry.Berry;
import item.hold.HoldItem;
import item.hold.SpecialTypeItem.MemoryItem;
import item.hold.SpecialTypeItem.PlateItem;
import main.Game;
import main.Global;
import map.overworld.wild.WildEncounter;
import map.overworld.wild.WildEncounterInfo;
import map.weather.WeatherState;
import message.MessageUpdate;
import message.Messages;
import pokemon.active.Gender;
import pokemon.active.MoveList;
import pokemon.species.BaseStats;
import pokemon.species.PokemonNamesies;
import pokemon.stat.Stat;
import trainer.Trainer;
import type.PokeType;
import type.Type;
import type.TypeAdvantage;
import util.RandomUtils;
import util.ReverseIterable;
import util.string.PokeString;
import util.string.StringUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public abstract class Ability implements AbilityInterface {
    private static final long serialVersionUID = 1L;

    protected final AbilityNamesies namesies;
    private final String description;

    public Ability(AbilityNamesies namesies, String description) {
        this.namesies = namesies;
        this.description = description;
    }

    @Override
    public Ability getAbility() {
        return this;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    @Override
    public AbilityNamesies namesies() {
        return this.namesies;
    }

    @Override
    public String getName() {
        return this.namesies().getName();
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return true;
    }

    // False if ability is ignored when the opponent breaks the mold
    public boolean unbreakableMold() {
        return false;
    }

    // True if Pokemon with this ability can have it replaced by another
    public boolean isReplaceable() {
        return true;
    }

    // True if Pokemon can steal this ability when it is not their default
    public boolean isStealable() {
        return true;
    }

    // True if ability is ignored when the opponent has neutralizing gas
    public boolean isNeutralizable() {
        return true;
    }

    // Called when this ability is going to changed to a different ability -- can be overridden as necessary
    public void deactivate(Battle b, ActivePokemon victim) {}

    protected ActivePokemon getOtherPokemon(Battle b, ActivePokemon p) {
        ActivePokemon other = b.getOtherPokemon(p);
        if (other.getAbility() != this) {
            Global.error(this.getName() + " invokee is not the opposite Pokemon.");
        }

        return other;
    }

    // EVERYTHING BELOW IS GENERATED ###

    /**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

    static class NoAbility extends Ability {
        private static final long serialVersionUID = 1L;

        NoAbility() {
            super(AbilityNamesies.NO_ABILITY, "None");
        }

        @Override
        public boolean isStealable() {
            return false;
        }
    }

    static class Overgrow extends Ability implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Overgrow() {
            super(AbilityNamesies.OVERGROW, "Powers up Grass-type moves when the Pok\u00e9mon's HP is low.");
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getHPRatio() < 1/3.0 && user.isAttackType(Type.GRASS) ? 1.5 : 1;
        }
    }

    static class Chlorophyll extends Ability implements SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        Chlorophyll() {
            super(AbilityNamesies.CHLOROPHYLL, "Boosts the Pok\u00e9mon's Speed stat in sunshine.");
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return b.isWeather(WeatherNamesies.SUNNY);
        }

        @Override
        public double getModifier() {
            return 2;
        }
    }

    static class Blaze extends Ability implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Blaze() {
            super(AbilityNamesies.BLAZE, "Powers up Fire-type moves when the Pok\u00e9mon's HP is low.");
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getHPRatio() < 1/3.0 && user.isAttackType(Type.FIRE) ? 1.5 : 1;
        }
    }

    static class SolarPower extends Ability implements EndTurnEffect, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        SolarPower() {
            super(AbilityNamesies.SOLAR_POWER, "Boosts the Sp. Atk stat in sunny weather, but HP decreases every turn.");
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (b.isWeather(WeatherNamesies.SUNNY)) {
                victim.reduceHealthFraction(b, 1/8.0, victim.getName() + " lost some of its HP due to its " + this.getName() + "!");
            }
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getAttack().getCategory() == MoveCategory.SPECIAL && b.isWeather(WeatherNamesies.SUNNY) ? 1.5 : 1;
        }
    }

    static class Torrent extends Ability implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Torrent() {
            super(AbilityNamesies.TORRENT, "Powers up Water-type moves when the Pok\u00e9mon's HP is low.");
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getHPRatio() < 1/3.0 && user.isAttackType(Type.WATER) ? 1.5 : 1;
        }
    }

    static class RainDish extends Ability implements EndTurnEffect {
        private static final long serialVersionUID = 1L;

        RainDish() {
            super(AbilityNamesies.RAIN_DISH, "The Pok\u00e9mon gradually regains HP in rain.");
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (b.isWeather(WeatherNamesies.RAINING)) {
                victim.healHealthFraction(1/16.0, b, victim.getName() + "'s HP was restored due to its " + this.getName() + "!");
            }
        }
    }

    static class ShieldDust extends Ability {
        private static final long serialVersionUID = 1L;

        ShieldDust() {
            super(AbilityNamesies.SHIELD_DUST, "This Pok\u00e9mon's dust blocks the additional effects of attacks taken.");
        }
    }

    static class ShedSkin extends Ability implements EndTurnEffect {
        private static final long serialVersionUID = 1L;

        ShedSkin() {
            super(AbilityNamesies.SHED_SKIN, "The Pok\u00e9mon may heal its own status conditions by shedding its skin.");
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (victim.hasStatus() && RandomUtils.chanceTest(1, 3)) {
                victim.removeStatus(b, CastSource.ABILITY);
            }
        }
    }

    static class CompoundEyes extends Ability implements SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        CompoundEyes() {
            super(AbilityNamesies.COMPOUND_EYES, "The Pok\u00e9mon's compound eyes boost its accuracy.");
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.ACCURACY;
        }

        @Override
        public double getModifier() {
            return 1.3;
        }
    }

    static class Battery extends Ability implements SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        Battery() {
            super(AbilityNamesies.BATTERY, "Powers up the Pok\u00e9mon's special moves.");
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SP_ATTACK;
        }

        @Override
        public double getModifier() {
            return 1.3;
        }
    }

    static class TintedLens extends Ability implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        TintedLens() {
            super(AbilityNamesies.TINTED_LENS, "The Pok\u00e9mon can use \"not very effective\" moves to deal regular damage.");
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return TypeAdvantage.isNotVeryEffective(user, victim, b) ? 2 : 1;
        }
    }

    // Guessed on the encounter rate multiplier
    static class Swarm extends Ability implements EncounterRateMultiplier, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Swarm() {
            super(AbilityNamesies.SWARM, "Powers up Bug-type moves when the Pok\u00e9mon's HP is low.");
        }

        @Override
        public double getEncounterRateMultiplier() {
            return 1.5;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getHPRatio() < 1/3.0 && user.isAttackType(Type.BUG) ? 1.5 : 1;
        }
    }

    static class Sniper extends Ability {
        private static final long serialVersionUID = 1L;

        Sniper() {
            super(AbilityNamesies.SNIPER, "Powers up moves if they become critical hits when attacking.");
        }
    }

    static class KeenEye extends Ability implements OpponentIgnoreStageEffect, RepelLowLevelEncounterEffect, StatProtectingEffect {
        private static final long serialVersionUID = 1L;

        KeenEye() {
            super(AbilityNamesies.KEEN_EYE, "Keen eyes prevent other Pok\u00e9mon from lowering this Pok\u00e9mon's accuracy.");
        }

        @Override
        public boolean ignoreStage(Stat s) {
            // Keen Eye ignores the target's evasion stages
            return s == Stat.EVASION;
        }

        @Override
        public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
            return stat == Stat.ACCURACY;
        }

        @Override
        public String preventionMessage(ActivePokemon p, Stat s) {
            return p.getName() + "'s " + this.getName() + " prevents its " + s.getName() + " from being lowered!";
        }
    }

    static class TangledFeet extends Ability implements StageChangingEffect {
        private static final long serialVersionUID = 1L;

        TangledFeet() {
            super(AbilityNamesies.TANGLED_FEET, "Raises evasion if the Pok\u00e9mon is confused.");
        }

        @Override
        public int adjustStage(Battle b, ActivePokemon p, Stat s) {
            return s == Stat.EVASION && p.hasEffect(PokemonEffectNamesies.CONFUSION) ? 1 : 0;
        }
    }

    static class Guts extends Ability implements StatStatusBoosterEffect {
        private static final long serialVersionUID = 1L;

        Guts() {
            super(AbilityNamesies.GUTS, "It's so gutsy that having a status condition boosts the Pok\u00e9mon's Attack stat.");
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.ATTACK;
        }

        @Override
        public double getModifier() {
            return 1.5;
        }
    }

    static class Intimidate extends Ability implements EntryEffect, RepelLowLevelEncounterEffect {
        private static final long serialVersionUID = 1L;

        Intimidate() {
            super(AbilityNamesies.INTIMIDATE, "The Pok\u00e9mon intimidates opposing Pok\u00e9mon upon entering battle, lowering their Attack stat.");
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            ActivePokemon other = b.getOtherPokemon(enterer);
            new StageModifier(-1, Stat.ATTACK).modify(b, enterer, other, CastSource.ABILITY);
        }
    }

    static class Static extends Ability implements PhysicalContactEffect, TypedWildEncounterSelector {
        private static final long serialVersionUID = 1L;

        Static() {
            super(AbilityNamesies.STATIC, "The Pok\u00e9mon is charged with static electricity, so contact with it may cause paralysis.");
        }

        @Override
        public Type getEncounterType() {
            return Type.ELECTRIC;
        }

        @Override
        public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (RandomUtils.chanceTest(30)) {
                StatusNamesies.PARALYZED.getStatus().apply(b, victim, user, CastSource.ABILITY);
            }
        }
    }

    static class LightningRod extends Ability implements AttackBlocker {
        private static final long serialVersionUID = 1L;

        LightningRod() {
            super(AbilityNamesies.LIGHTNING_ROD, "The Pok\u00e9mon draws in all Electric-type moves. Instead of being hit by Electric-type moves, it boosts its Sp. Atk.");
        }

        @Override
        public void alternateEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            new StageModifier(1, Stat.SP_ATTACK).modify(b, victim, victim, CastSource.ABILITY);
        }

        @Override
        public boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.ELECTRIC);
        }

        @Override
        public String getBlockMessage(ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " makes it immune to " + Type.ELECTRIC.getName() + "-type moves!";
        }
    }

    static class SandVeil extends Ability implements StageChangingEffect, EncounterRateMultiplier {
        private static final long serialVersionUID = 1L;

        SandVeil() {
            super(AbilityNamesies.SAND_VEIL, "Boosts the Pok\u00e9mon's evasion in a sandstorm.");
        }

        @Override
        public int adjustStage(Battle b, ActivePokemon p, Stat s) {
            return s == Stat.EVASION && b.isWeather(WeatherNamesies.SANDSTORM) ? 1 : 0;
        }

        @Override
        public double getEncounterRateMultiplier() {
            return Game.getPlayer().getArea().getWeather() == WeatherState.SANDSTORM ? .5 : 1;
        }
    }

    static class SandRush extends Ability implements SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        SandRush() {
            super(AbilityNamesies.SAND_RUSH, "Boosts the Pok\u00e9mon's Speed stat in a sandstorm.");
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return b.isWeather(WeatherNamesies.SANDSTORM);
        }

        @Override
        public double getModifier() {
            return 2;
        }
    }

    static class SlushRush extends Ability implements SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        SlushRush() {
            super(AbilityNamesies.SLUSH_RUSH, "Boosts the Pok\u00e9mon's Speed stat in a hailstorm.");
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return b.isWeather(WeatherNamesies.HAILING);
        }

        @Override
        public double getModifier() {
            return 2;
        }
    }

    static class PoisonPoint extends Ability implements PhysicalContactEffect {
        private static final long serialVersionUID = 1L;

        PoisonPoint() {
            super(AbilityNamesies.POISON_POINT, "Contact with the Pok\u00e9mon may poison the attacker.");
        }

        @Override
        public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (RandomUtils.chanceTest(30)) {
                StatusNamesies.POISONED.getStatus().apply(b, victim, user, CastSource.ABILITY);
            }
        }
    }

    static class Rivalry extends Ability implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Rivalry() {
            super(AbilityNamesies.RIVALRY, "Becomes competitive and deals more damage to Pok\u00e9mon of the same gender, but deals less to Pok\u00e9mon of the opposite gender.");
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (user.getGender() == Gender.GENDERLESS) {
                return 1;
            } else if (Gender.oppositeGenders(user, victim)) {
                return .75;
            } else if (user.getGender() == victim.getGender()) {
                return 1.25;
            } else {
                return 1;
            }
        }
    }

    static class CuteCharm extends Ability implements PhysicalContactEffect, WildEncounterAlterer {
        private static final long serialVersionUID = 1L;

        CuteCharm() {
            super(AbilityNamesies.CUTE_CHARM, "Contact with the Pok\u00e9mon may cause infatuation.");
        }

        @Override
        public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (RandomUtils.chanceTest(30)) {
                String message = victim.getName() + "'s " + this.getName() + " infatuated " + user.getName() + "!";
                Effect.apply(PokemonEffectNamesies.INFATUATION, b, victim, user, CastSource.ABILITY, message);
            }
        }

        @Override
        public void alterWildPokemon(ActivePokemon playerFront, WildEncounterInfo encounterData, WildEncounter encounter) {
            if (RandomUtils.chanceTest(2, 3)) {
                Gender opposite = playerFront.getGender().getOppositeGender();
                if (opposite.genderApplies(encounter.getPokemon().getInfo())) {
                    encounter.setGender(opposite);
                }
            }
        }
    }

    static class MagicGuard extends Ability implements WeatherBlockerEffect {
        private static final long serialVersionUID = 1L;

        MagicGuard() {
            super(AbilityNamesies.MAGIC_GUARD, "The Pok\u00e9mon only takes damage from attacks.");
        }

        @Override
        public boolean block(WeatherNamesies weather) {
            return true;
        }
    }

    static class FlashFire extends Ability implements AttackBlocker, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        private boolean activated;

        FlashFire() {
            super(AbilityNamesies.FLASH_FIRE, "Powers up the Pok\u00e9mon's Fire-type moves if it's hit by one.");
            this.activated = false;
        }

        @Override
        public boolean isActive() {
            return activated;
        }

        @Override
        public void alternateEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            activated = true;
        }

        @Override
        public boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.FIRE);
        }

        @Override
        public String getBlockMessage(ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " makes it immune to " + Type.FIRE.getName() + "-type moves!";
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return activated && user.isAttackType(Type.FIRE) ? 1.5 : 1;
        }
    }

    static class Drought extends Ability implements EntryEffect {
        private static final long serialVersionUID = 1L;

        Drought() {
            super(AbilityNamesies.DROUGHT, "Turns the sunlight harsh when the Pok\u00e9mon enters a battle.");
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            Messages.add(enterer.getName() + "'s " + this.getName() + " made the sunlight turn harsh!");
            b.addEffect(WeatherNamesies.SUNNY.getEffect());
        }
    }

    static class Frisk extends Ability implements EntryEffect {
        private static final long serialVersionUID = 1L;

        Frisk() {
            super(AbilityNamesies.FRISK, "When it enters a battle, the Pok\u00e9mon can check an opposing Pok\u00e9mon's held item.");
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            ActivePokemon other = b.getOtherPokemon(enterer);
            if (other.isHoldingItem()) {
                Messages.add(enterer.getName() + "'s " + this.getName() + " alerted it to " + other.getName() + "'s " + other.getHeldItem().getName() + "!");
            }
        }
    }

    static class InnerFocus extends Ability implements SingleEffectPreventionAbility {
        private static final long serialVersionUID = 1L;

        InnerFocus() {
            super(AbilityNamesies.INNER_FOCUS, "The Pok\u00e9mon's intensely focused, and that protects the Pok\u00e9mon from flinching.");
        }

        @Override
        public PokemonEffectNamesies getPreventableEffect() {
            return PokemonEffectNamesies.FLINCH;
        }
    }

    static class Infiltrator extends Ability {
        private static final long serialVersionUID = 1L;

        Infiltrator() {
            super(AbilityNamesies.INFILTRATOR, "Passes through the opposing Pok\u00e9mon's barrier, substitute, and the like and strikes.");
        }
    }

    static class Stench extends Ability implements OpponentTakeDamageEffect, EncounterRateMultiplier {
        private static final long serialVersionUID = 1L;

        Stench() {
            super(AbilityNamesies.STENCH, "By releasing stench when attacking, this Pok\u00e9mon may cause the target to flinch.");
        }

        @Override
        public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (RandomUtils.chanceTest(10)) {
                String message = user.getName() + "'s " + this.getName() + " caused " + victim.getName() + " to flinch!";
                Effect.apply(PokemonEffectNamesies.FLINCH, b, user, victim, CastSource.ABILITY, message);
            }
        }

        @Override
        public double getEncounterRateMultiplier() {
            return .5;
        }
    }

    static class EffectSpore extends Ability implements PhysicalContactEffect {
        private static final long serialVersionUID = 1L;

        private static final StatusNamesies[] STATUSES = new StatusNamesies[] {
                StatusNamesies.PARALYZED,
                StatusNamesies.POISONED,
                StatusNamesies.ASLEEP
        };

        EffectSpore() {
            super(AbilityNamesies.EFFECT_SPORE, "Contact with the Pok\u00e9mon may inflict poison, sleep, or paralysis on its attacker.");
        }

        @Override
        public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Grass-type Pokemon, Pokemon with Overcoat, and Pokemon holding the Safety Goggles are immune to Effect Spore
            if (PowderBlocker.containsPowderBlocker(b, user)) {
                return;
            }

            // 30% chance to Paralyze, Poison, or induce Sleep
            if (RandomUtils.chanceTest(30)) {
                RandomUtils.getRandomValue(STATUSES).getStatus().apply(b, victim, user, CastSource.ABILITY);
            }
        }
    }

    static class DrySkin extends Ability implements EndTurnEffect, OpponentPowerChangeEffect, AttackBlocker {
        private static final long serialVersionUID = 1L;

        DrySkin() {
            super(AbilityNamesies.DRY_SKIN, "Restores HP in rain or when hit by Water-type moves. Reduces HP in sunshine, and increases the damage received from Fire-type moves.");
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (b.isWeather(WeatherNamesies.SUNNY)) {
                victim.reduceHealthFraction(b, 1/8.0, victim.getName() + " lost some of its HP due to its " + this.getName() + "!");
            } else if (b.isWeather(WeatherNamesies.RAINING)) {
                victim.healHealthFraction(1/8.0, b, victim.getName() + "'s HP was restored due to its " + this.getName() + "!");
            }
        }

        @Override
        public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.FIRE) ? 1.25 : 1;
        }

        @Override
        public boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.WATER);
        }

        @Override
        public String getBlockMessage(ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " makes it immune to " + Type.WATER.getName() + " moves!";
        }

        @Override
        public void alternateEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Technically, according to the description, Heal Block prevents the prevention entirely (meaning this should be in Block), but that makes no sense, they shouldn't take damage, this way makes more sense
            victim.healHealthFraction(1/4.0, b, victim.getName() + "'s HP was restored instead!");
        }
    }

    static class ArenaTrap extends Ability implements EncounterRateMultiplier, OpponentTrappingEffect {
        private static final long serialVersionUID = 1L;

        ArenaTrap() {
            super(AbilityNamesies.ARENA_TRAP, "Prevents opposing Pok\u00e9mon from fleeing.");
        }

        @Override
        public double getEncounterRateMultiplier() {
            return 2;
        }

        @Override
        public boolean trapOpponent(Battle b, ActivePokemon escaper, ActivePokemon trapper) {
            return escaper.isOnTheGround(b);
        }

        @Override
        public String opponentTrappingMessage(ActivePokemon escaper, ActivePokemon trapper) {
            return trapper.getName() + "'s " + this.getName() + " prevents " + escaper.getName() + " from escaping!";
        }
    }

    static class Technician extends Ability implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Technician() {
            super(AbilityNamesies.TECHNICIAN, "Powers up the Pok\u00e9mon's weaker moves.");
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getAttack().getPower(b, user, victim) <= 60 ? 1.5 : 1;
        }
    }

    static class Limber extends Ability implements StatusPreventionAbility {
        private static final long serialVersionUID = 1L;

        Limber() {
            super(AbilityNamesies.LIMBER, "Its limber body protects the Pok\u00e9mon from paralysis.");
        }

        @Override
        public StatusNamesies getStatus() {
            return StatusNamesies.PARALYZED;
        }
    }

    static class Damp extends Ability implements AttackBlocker, SelfAttackBlocker {
        private static final long serialVersionUID = 1L;

        private boolean checkeroo(ActivePokemon attacking) {
            return attacking.getAttack().isMoveType(MoveType.EXPLODING);
        }

        private String blockityMessage(ActivePokemon attacking, ActivePokemon abilify) {
            return abilify.getName() + "'s " + this.getName() + " prevents " + attacking.getAttack().getName() + " from being used!";
        }

        Damp() {
            super(AbilityNamesies.DAMP, "Prevents the use of explosive moves such as Self-Destruct by dampening its surroundings.");
        }

        @Override
        public boolean block(Battle b, ActivePokemon user) {
            return checkeroo(user);
        }

        @Override
        public boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            return checkeroo(user);
        }

        @Override
        public String getBlockMessage(ActivePokemon user) {
            return blockityMessage(user, user);
        }

        @Override
        public String getBlockMessage(ActivePokemon user, ActivePokemon victim) {
            return blockityMessage(user, victim);
        }
    }

    static class CloudNine extends Ability implements WeatherEliminatingEffect {
        private static final long serialVersionUID = 1L;

        CloudNine() {
            super(AbilityNamesies.CLOUD_NINE, "Eliminates the effects of weather.");
        }

        @Override
        public String getEliminateMessage(ActivePokemon eliminator) {
            return eliminator.getName() + "'s " + this.getName() + " eliminated the weather!";
        }
    }

    static class VitalSpirit extends Ability implements MaxLevelWildEncounterEffect, StatusPreventionAbility {
        private static final long serialVersionUID = 1L;

        VitalSpirit() {
            super(AbilityNamesies.VITAL_SPIRIT, "The Pok\u00e9mon is full of vitality, and that prevents it from falling asleep.");
        }

        @Override
        public StatusNamesies getStatus() {
            return StatusNamesies.ASLEEP;
        }
    }

    static class Insomnia extends Ability implements StatusPreventionAbility {
        private static final long serialVersionUID = 1L;

        Insomnia() {
            super(AbilityNamesies.INSOMNIA, "The Pok\u00e9mon is suffering from insomnia and cannot fall asleep.");
        }

        @Override
        public StatusNamesies getStatus() {
            return StatusNamesies.ASLEEP;
        }
    }

    static class AngerPoint extends Ability {
        private static final long serialVersionUID = 1L;

        AngerPoint() {
            super(AbilityNamesies.ANGER_POINT, "The Pok\u00e9mon is angered when it takes a critical hit, and that maxes its Attack stat.");
        }
    }

    static class Synchronize extends Ability implements StatusReceivedEffect, WildEncounterAlterer {
        private static final long serialVersionUID = 1L;

        private static final Set<StatusNamesies> PASSABLE_STATUSES = EnumSet.of(
                StatusNamesies.BURNED,
                StatusNamesies.PARALYZED,
                StatusNamesies.POISONED,
                StatusNamesies.BADLY_POISONED
        );

        Synchronize() {
            super(AbilityNamesies.SYNCHRONIZE, "The attacker will receive the same status condition if it inflicts a burn, poison, or paralysis to the Pok\u00e9mon.");
        }

        @Override
        public void receiveStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusNamesies statusType) {
            // Only applies when the opponent gives the victim the condition
            if (caster == victim) {
                return;
            }

            // Synchronize doesn't apply to every condition
            if (!PASSABLE_STATUSES.contains(statusType)) {
                return;
            }

            // Give status condition to the opponent
            statusType.getStatus().apply(b, victim, caster, CastSource.ABILITY);
        }

        @Override
        public void alterWildPokemon(ActivePokemon playerFront, WildEncounterInfo encounterData, WildEncounter encounter) {
            if (RandomUtils.chanceTest(50)) {
                encounter.setNature(playerFront.getNature());
            }
        }
    }

    static class NoGuard extends Ability implements SemiInvulnerableBypasser, OpponentAccuracyBypassEffect, EncounterRateMultiplier {
        private static final long serialVersionUID = 1L;

        NoGuard() {
            super(AbilityNamesies.NO_GUARD, "The Pok\u00e9mon employs no-guard tactics to ensure incoming and outgoing attacks always land.");
        }

        @Override
        public boolean semiInvulnerableBypass(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // Moves always hit
            return true;
        }

        @Override
        public boolean opponentBypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // Moves always hit
            return true;
        }

        @Override
        public double getEncounterRateMultiplier() {
            return 1.5;
        }
    }

    static class OwnTempo extends Ability implements SingleEffectPreventionAbility {
        private static final long serialVersionUID = 1L;

        OwnTempo() {
            super(AbilityNamesies.OWN_TEMPO, "This Pok\u00e9mon has its own tempo, and that prevents it from becoming confused.");
        }

        @Override
        public PokemonEffectNamesies getPreventableEffect() {
            return PokemonEffectNamesies.CONFUSION;
        }
    }

    static class ClearBody extends Ability implements StatProtectingEffect {
        private static final long serialVersionUID = 1L;

        ClearBody() {
            super(AbilityNamesies.CLEAR_BODY, "Prevents other Pok\u00e9mon's moves or Abilities from lowering the Pok\u00e9mon's stats.");
        }

        @Override
        public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
            return true;
        }

        @Override
        public String preventionMessage(ActivePokemon p, Stat s) {
            return p.getName() + "'s " + this.getName() + " prevents its stats from being lowered!";
        }
    }

    static class FullMetalBody extends Ability implements StatProtectingEffect {
        private static final long serialVersionUID = 1L;

        FullMetalBody() {
            super(AbilityNamesies.FULL_METAL_BODY, "Prevents other Pok\u00e9mon's moves or Abilities from lowering the Pok\u00e9mon's stats.");
        }

        @Override
        public boolean unbreakableMold() {
            // Ability is not ignored even when the opponent breaks the mold
            return true;
        }

        @Override
        public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
            return true;
        }

        @Override
        public String preventionMessage(ActivePokemon p, Stat s) {
            return p.getName() + "'s " + this.getName() + " prevents its stats from being lowered!";
        }
    }

    static class LiquidOoze extends Ability {
        private static final long serialVersionUID = 1L;

        LiquidOoze() {
            super(AbilityNamesies.LIQUID_OOZE, "Oozed liquid has strong stench, which damages attackers using any draining move.");
        }
    }

    static class RockHead extends Ability {
        private static final long serialVersionUID = 1L;

        RockHead() {
            super(AbilityNamesies.ROCK_HEAD, "Protects the Pok\u00e9mon from recoil damage.");
        }
    }

    static class Sturdy extends Ability implements BracingEffect, AttackBlocker {
        private static final long serialVersionUID = 1L;

        Sturdy() {
            super(AbilityNamesies.STURDY, "It cannot be knocked out with one hit. One-hit KO moves cannot knock it out, either.");
        }

        @Override
        public boolean isBracing(Battle b, ActivePokemon bracer, boolean fullHealth) {
            return fullHealth;
        }

        @Override
        public String braceMessage(ActivePokemon bracer) {
            return bracer.getName() + "'s " + this.getName() + " endured the hit!";
        }

        @Override
        public boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getAttack() instanceof OhkoMove;
        }

        @Override
        public String getBlockMessage(ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " prevents OHKO moves!";
        }
    }

    static class Oblivious extends Ability implements AttackBlocker, MultipleEffectPreventionAbility {
        private static final long serialVersionUID = 1L;

        private static final Set<PokemonEffectNamesies> PREVENTION_EFFECTS = EnumSet.of(
                PokemonEffectNamesies.INFATUATION,
                PokemonEffectNamesies.TAUNT
        );

        Oblivious() {
            super(AbilityNamesies.OBLIVIOUS, "The Pok\u00e9mon is oblivious, and that keeps it from being infatuated or falling for taunts.");
        }

        @Override
        public boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getAttack().namesies() == AttackNamesies.CAPTIVATE;
        }

        @Override
        public String getBlockMessage(ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + "'s " + victim.getAbility().getName() + " prevents it from being captivated!";
        }

        @Override
        public Set<PokemonEffectNamesies> getPreventableEffects() {
            return PREVENTION_EFFECTS;
        }
    }

    static class MagnetPull extends Ability implements TypedWildEncounterSelector, OpponentTrappingEffect {
        private static final long serialVersionUID = 1L;

        MagnetPull() {
            super(AbilityNamesies.MAGNET_PULL, "Prevents Steel-type Pok\u00e9mon from escaping using its magnetic force.");
        }

        @Override
        public Type getEncounterType() {
            return Type.STEEL;
        }

        @Override
        public boolean trapOpponent(Battle b, ActivePokemon escaper, ActivePokemon trapper) {
            return escaper.isType(b, Type.STEEL);
        }

        @Override
        public String opponentTrappingMessage(ActivePokemon escaper, ActivePokemon trapper) {
            return trapper.getName() + "'s " + this.getName() + " prevents " + escaper.getName() + " from escaping!";
        }
    }

    static class Unaware extends Ability implements OpponentIgnoreStageEffect {
        private static final long serialVersionUID = 1L;

        Unaware() {
            super(AbilityNamesies.UNAWARE, "When attacking, the Pok\u00e9mon ignores the target Pok\u00e9mon's stat changes.");
        }

        @Override
        public boolean ignoreStage(Stat s) {
            return s != Stat.SPEED;
        }
    }

    static class Simple extends Ability implements ModifyStageValueEffect {
        private static final long serialVersionUID = 1L;

        Simple() {
            super(AbilityNamesies.SIMPLE, "The stat changes the Pok\u00e9mon receives are doubled.");
        }

        @Override
        public int modifyStageValue() {
            return 2;
        }
    }

    static class EarlyBird extends Ability implements StatusReceivedEffect {
        private static final long serialVersionUID = 1L;

        EarlyBird() {
            super(AbilityNamesies.EARLY_BIRD, "The Pok\u00e9mon awakens twice as fast as other Pok\u00e9mon from sleep.");
        }

        @Override
        public void receiveStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusNamesies statusType) {
            if (statusType == StatusNamesies.ASLEEP) {
                StatusCondition sleepyTime = victim.getStatus();
                sleepyTime.setTurns(sleepyTime.getTurns()/2);
            }
        }
    }

    static class ThickFat extends Ability implements OpponentPowerChangeEffect {
        private static final long serialVersionUID = 1L;

        ThickFat() {
            super(AbilityNamesies.THICK_FAT, "The Pok\u00e9mon is protected by a layer of thick fat, which halves the damage taken from Fire- and Ice-type moves.");
        }

        @Override
        public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.FIRE, Type.ICE) ? .5 : 1;
        }
    }

    static class Hydration extends Ability implements EndTurnEffect {
        private static final long serialVersionUID = 1L;

        Hydration() {
            super(AbilityNamesies.HYDRATION, "Heals status conditions if it's raining.");
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (victim.hasStatus() && b.isWeather(WeatherNamesies.RAINING)) {
                victim.removeStatus(b, CastSource.ABILITY);
            }
        }
    }

    static class ShellArmor extends Ability implements CritBlockerEffect {
        private static final long serialVersionUID = 1L;

        ShellArmor() {
            super(AbilityNamesies.SHELL_ARMOR, "A hard shell protects the Pok\u00e9mon from critical hits.");
        }
    }

    static class BattleArmor extends Ability implements CritBlockerEffect {
        private static final long serialVersionUID = 1L;

        BattleArmor() {
            super(AbilityNamesies.BATTLE_ARMOR, "Hard armor protects the Pok\u00e9mon from critical hits.");
        }
    }

    static class SkillLink extends Ability {
        private static final long serialVersionUID = 1L;

        SkillLink() {
            super(AbilityNamesies.SKILL_LINK, "Maximizes the number of times multi-strike moves hit.");
        }
    }

    static class Levitate extends Ability implements LevitationEffect {
        private static final long serialVersionUID = 1L;

        Levitate() {
            super(AbilityNamesies.LEVITATE, "By floating in the air, the Pok\u00e9mon receives full immunity to all Ground-type moves.");
        }
    }

    static class Forewarn extends Ability implements EntryEffect {
        private static final long serialVersionUID = 1L;

        Forewarn() {
            super(AbilityNamesies.FOREWARN, "When it enters a battle, the Pok\u00e9mon can tell one of the moves an opposing Pok\u00e9mon has.");
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            ActivePokemon other = b.getOtherPokemon(enterer);
            MoveList otherMoves = other.getMoves(b);

            List<AttackNamesies> besties = new ArrayList<>();
            int highestPower = -1;

            for (Move move : otherMoves) {
                if (move.getAttack().isStatusMove()) {
                    continue;
                }

                int power = move.getAttack().getPower(b, other, enterer);
                if (power > highestPower) {
                    highestPower = power;
                    besties = new ArrayList<>();
                    besties.add(move.getAttack().namesies());
                } else if (power == highestPower) {
                    besties.add(move.getAttack().namesies());
                }
            }

            AttackNamesies warn;
            if (highestPower == -1) {
                warn = otherMoves.getRandomMove().getAttack().namesies();
            } else {
                warn = RandomUtils.getRandomValue(besties);
            }

            Messages.add(enterer.getName() + "'s " + this.getName() + " alerted it to " + other.getName() + "'s " + warn.getName() + "!");
        }
    }

    static class HyperCutter extends Ability implements StatProtectingEffect {
        private static final long serialVersionUID = 1L;

        HyperCutter() {
            super(AbilityNamesies.HYPER_CUTTER, "The Pok\u00e9mon's proud of its powerful pincers. They prevent other Pok\u00e9mon from lowering its Attack stat.");
        }

        @Override
        public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
            return stat == Stat.ATTACK;
        }

        @Override
        public String preventionMessage(ActivePokemon p, Stat s) {
            return p.getName() + "'s " + this.getName() + " prevents its " + s.getName() + " from being lowered!";
        }
    }

    static class Soundproof extends Ability implements AttackBlocker, EffectPreventionEffect {
        private static final long serialVersionUID = 1L;

        Soundproof() {
            super(AbilityNamesies.SOUNDPROOF, "Soundproofing of the Pok\u00e9mon itself gives full immunity to all sound-based moves.");
        }

        @Override
        public ApplyResult preventEffect(Battle b, ActivePokemon caster, ActivePokemon victim, EffectNamesies effectName, CastSource source) {
            // Only sound-based when from the Perish Song attack (not Perish Body)
            if (effectName == PokemonEffectNamesies.PERISH_SONG && source == CastSource.ATTACK) {
                return ApplyResult.failure(victim.getName() + "'s " + this.getName() + " makes it immune to sound based moves!");
            }

            return ApplyResult.success();
        }

        @Override
        public boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getAttack().isMoveType(MoveType.SOUND_BASED);
        }

        @Override
        public String getBlockMessage(ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " prevents sound-based moves!";
        }
    }

    static class Reckless extends Ability implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Reckless() {
            super(AbilityNamesies.RECKLESS, "Powers up moves that have recoil damage.");
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getAttack() instanceof RecoilMove || user.getAttack() instanceof CrashDamageMove ? 1.2 : 1;
        }
    }

    static class IronFist extends Ability implements EntryEffect, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        IronFist() {
            super(AbilityNamesies.IRON_FIST, "Powers up punching moves.");
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getAttack().isMoveType(MoveType.PUNCHING) ? 1.2 : 1;
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            if (enterer.namesies() == PokemonNamesies.PANGORO) {
                Messages.add(enterer.getName() + " does not break the mold!!!!!!!");
            }
        }
    }

    static class NaturalCure extends Ability implements SwitchOutEffect, EndBattleEffect {
        private static final long serialVersionUID = 1L;

        private void removeStatus(ActivePokemon p) {
            if (!p.hasStatus(StatusNamesies.FAINTED)) {
                p.removeStatus();
            }
        }

        NaturalCure() {
            super(AbilityNamesies.NATURAL_CURE, "All status conditions heal when the Pok\u00e9mon switches out.");
        }

        @Override
        public void switchOut(ActivePokemon switchee) {
            this.removeStatus(switchee);
        }

        @Override
        public void afterBattle(Trainer player, ActivePokemon p) {
            // Also removes status at the end of battle
            this.removeStatus(p);
        }
    }

    static class SereneGrace extends Ability implements EffectChanceMultiplierEffect {
        private static final long serialVersionUID = 1L;

        SereneGrace() {
            super(AbilityNamesies.SERENE_GRACE, "Boosts the likelihood of additional effects occurring when attacking.");
        }

        @Override
        public double getEffectChanceMultiplier(ActivePokemon user) {
            return 2;
        }
    }

    static class LeafGuard extends Ability implements StatusPreventionEffect {
        private static final long serialVersionUID = 1L;

        LeafGuard() {
            super(AbilityNamesies.LEAF_GUARD, "Prevents status conditions in sunny weather.");
        }

        @Override
        public ApplyResult preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusNamesies status) {
            if (b.isWeather(WeatherNamesies.SUNNY)) {
                return ApplyResult.failure(victim.getName() + "'s " + this.getName() + " prevents status conditions!");
            }

            return ApplyResult.success();
        }
    }

    static class Scrappy extends Ability implements AttackingNoAdvantageChanger, EntryEffect {
        private static final long serialVersionUID = 1L;

        Scrappy() {
            super(AbilityNamesies.SCRAPPY, "The Pok\u00e9mon can hit Ghost-type Pok\u00e9mon with Normal- and Fighting-type moves.");
        }

        @Override
        public boolean negateNoAdvantage(Type attacking, Type defending) {
            return defending == Type.GHOST && (attacking == Type.NORMAL || attacking == Type.FIGHTING);
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            if (enterer.namesies() == PokemonNamesies.PANGORO) {
                Messages.add(enterer.getName() + " does not break the mold!!!!!!!");
            }
        }
    }

    static class SwiftSwim extends Ability implements SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        SwiftSwim() {
            super(AbilityNamesies.SWIFT_SWIM, "Boosts the Pok\u00e9mon's Speed stat in rain.");
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return b.isWeather(WeatherNamesies.RAINING);
        }

        @Override
        public double getModifier() {
            return 2;
        }
    }

    static class WaterVeil extends Ability implements StatusPreventionAbility {
        private static final long serialVersionUID = 1L;

        WaterVeil() {
            super(AbilityNamesies.WATER_VEIL, "The Pok\u00e9mon is covered with a water veil, which prevents the Pok\u00e9mon from getting a burn.");
        }

        @Override
        public StatusNamesies getStatus() {
            return StatusNamesies.BURNED;
        }
    }

    static class Filter extends Ability implements OpponentPowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Filter() {
            super(AbilityNamesies.FILTER, "Reduces the power of supereffective attacks taken.");
        }

        @Override
        public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return TypeAdvantage.isSuperEffective(user, victim, b) ? .75 : 1;
        }
    }

    static class PrismArmor extends Ability implements OpponentPowerChangeEffect {
        private static final long serialVersionUID = 1L;

        PrismArmor() {
            super(AbilityNamesies.PRISM_ARMOR, "Reduces the power of supereffective attacks taken.");
        }

        @Override
        public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return TypeAdvantage.isSuperEffective(user, victim, b) ? .75 : 1;
        }

        @Override
        public boolean unbreakableMold() {
            // Ability is not ignored even when the opponent breaks the mold
            return true;
        }
    }

    static class FlameBody extends Ability implements PhysicalContactEffect {
        private static final long serialVersionUID = 1L;

        FlameBody() {
            super(AbilityNamesies.FLAME_BODY, "Contact with the Pok\u00e9mon may burn the attacker.");
        }

        @Override
        public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (RandomUtils.chanceTest(30)) {
                StatusNamesies.BURNED.getStatus().apply(b, victim, user, CastSource.ABILITY);
            }
        }
    }

    static class Rattled extends Ability implements TakeDamageEffect {
        private static final long serialVersionUID = 1L;

        Rattled() {
            super(AbilityNamesies.RATTLED, "Dark-, Ghost-, and Bug-type moves scare the Pok\u00e9mon and boost its Speed stat.");
        }

        @Override
        public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            Type type = user.getAttackType();
            if (type == Type.BUG || type == Type.DARK || type == Type.GHOST) {
                new StageModifier(1, Stat.SPEED).modify(b, victim, victim, CastSource.ABILITY);
            }
        }
    }

    static class Moxie extends Ability implements MurderEffect {
        private static final long serialVersionUID = 1L;

        Moxie() {
            super(AbilityNamesies.MOXIE, "The Pok\u00e9mon shows moxie, and that boosts the Attack stat after knocking out any Pok\u00e9mon.");
        }

        @Override
        public void killWish(Battle b, ActivePokemon dead, ActivePokemon murderer) {
            new StageModifier(1, Stat.ATTACK).modify(b, murderer, murderer, CastSource.ABILITY);
        }
    }

    static class BeastBoost extends Ability implements MurderEffect {
        private static final long serialVersionUID = 1L;

        BeastBoost() {
            super(AbilityNamesies.BEAST_BOOST, "The Pok\u00e9mon boosts its most proficient stat each time it knocks out a Pok\u00e9mon.");
        }

        @Override
        public void killWish(Battle b, ActivePokemon dead, ActivePokemon murderer) {
            // Increase highest stat when it murders
            Stat bestStat = murderer.stats().getBestBattleStat();
            new StageModifier(1, bestStat).modify(b, murderer, murderer, CastSource.ABILITY);
        }
    }

    static class SoulHeart extends Ability implements OpponentStatusReceivedEffect {
        private static final long serialVersionUID = 1L;

        SoulHeart() {
            super(AbilityNamesies.SOUL_HEART, "Boosts its Sp. Atk stat every time a Pok\u00e9mon faints.");
        }

        @Override
        public void receiveStatus(Battle b, ActivePokemon victim, StatusNamesies statusType) {
            if (statusType == StatusNamesies.FAINTED) {
                ActivePokemon abilify = this.getOtherPokemon(b, victim);
                new StageModifier(1, Stat.SP_ATTACK).modify(b, abilify, abilify, CastSource.ABILITY);
            }
        }
    }

    static class Imposter extends Ability implements EntryEffect {
        private static final long serialVersionUID = 1L;

        Imposter() {
            super(AbilityNamesies.IMPOSTER, "The Pok\u00e9mon transforms itself into the Pok\u00e9mon it's facing.");
        }

        @Override
        public boolean isStealable() {
            return false;
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            Effect.cast(PokemonEffectNamesies.TRANSFORMED, b, enterer, enterer, CastSource.ABILITY, false);
        }
    }

    static class Adaptability extends Ability {
        private static final long serialVersionUID = 1L;

        Adaptability() {
            super(AbilityNamesies.ADAPTABILITY, "Powers up moves of the same type as the Pok\u00e9mon.");
        }
    }

    static class WaterAbsorb extends Ability implements AttackBlocker {
        private static final long serialVersionUID = 1L;

        WaterAbsorb() {
            super(AbilityNamesies.WATER_ABSORB, "Restores HP if hit by a Water-type move, instead of taking damage.");
        }

        @Override
        public boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.WATER);
        }

        @Override
        public String getBlockMessage(ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " makes it immune to " + Type.WATER.getName() + " moves!";
        }

        @Override
        public void alternateEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Technically, according to the description, Heal Block prevents the prevention entirely (meaning this should be in Block), but that makes no sense, they shouldn't take damage, this way makes more sense
            victim.healHealthFraction(1/4.0, b, victim.getName() + "'s HP was restored instead!");
        }
    }

    static class VoltAbsorb extends Ability implements AttackBlocker {
        private static final long serialVersionUID = 1L;

        VoltAbsorb() {
            super(AbilityNamesies.VOLT_ABSORB, "Restores HP if hit by an Electric-type move, instead of taking damage.");
        }

        @Override
        public boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.ELECTRIC);
        }

        @Override
        public String getBlockMessage(ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " makes it immune to " + Type.ELECTRIC.getName() + " moves!";
        }

        @Override
        public void alternateEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Technically, according to the description, Heal Block prevents the prevention entirely (meaning this should be in Block), but that makes no sense, they shouldn't take damage, this way makes more sense
            victim.healHealthFraction(1/4.0, b, victim.getName() + "'s HP was restored instead!");
        }
    }

    static class QuickFeet extends Ability implements StatStatusBoosterEffect, EncounterRateMultiplier {
        private static final long serialVersionUID = 1L;

        QuickFeet() {
            super(AbilityNamesies.QUICK_FEET, "Boosts the Speed stat if the Pok\u00e9mon has a status condition.");
        }

        @Override
        public double getEncounterRateMultiplier() {
            return .5;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
        }

        @Override
        public double getModifier() {
            return 1.5;
        }
    }

    static class Trace extends Ability implements EntryEffect, ChangeAbilitySource {
        private static final long serialVersionUID = 1L;

        Trace() {
            super(AbilityNamesies.TRACE, "When it enters a battle, the Pok\u00e9mon copies an opposing Pok\u00e9mon's Ability.");
        }

        @Override
        public boolean isStealable() {
            return false;
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            ActivePokemon other = b.getOtherPokemon(enterer);
            if (!other.getAbility().isStealable() || other.hasAbility(this.namesies)) {
                return;
            }

            Effect.cast(PokemonEffectNamesies.CHANGE_ABILITY, b, enterer, enterer, CastSource.ABILITY, true);
        }

        @Override
        public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim) {
            Ability otherAbility = b.getOtherPokemon(victim).getAbility();
            return otherAbility.namesies().getNewAbility();
        }

        @Override
        public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim) {
            ActivePokemon other = b.getOtherPokemon(victim);
            return victim.getName() + " traced " + other.getName() + "'s " + other.getAbility().getName() + "!";
        }
    }

    static class Download extends Ability implements EntryEffect {
        private static final long serialVersionUID = 1L;

        Download() {
            super(AbilityNamesies.DOWNLOAD, "Compares opposing Pok\u00e9mon's Defense and Sp. Def stats before raising its own Attack or Sp. Atk stat -- whichever will be more effective.");
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            ActivePokemon other = b.getOtherPokemon(enterer);
            BaseStats otherStats = other.getPokemonInfo().getStats();

            int baseDefense = otherStats.get(Stat.DEFENSE);
            int baseSpecialDefense = otherStats.get(Stat.SP_DEFENSE);

            Stat toRaise = baseDefense < baseSpecialDefense ? Stat.ATTACK : Stat.SP_ATTACK;

            new StageModifier(1, toRaise).modify(b, enterer, enterer, CastSource.ABILITY);
        }
    }

    static class Pressure extends Ability implements EntryEffect, MaxLevelWildEncounterEffect {
        private static final long serialVersionUID = 1L;

        Pressure() {
            super(AbilityNamesies.PRESSURE, "By putting pressure on the opposing Pok\u00e9mon, it raises their PP usage.");
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            Messages.add(enterer.getName() + " is exerting pressure!");
        }
    }

    static class Immunity extends Ability implements StatusPreventionAbility {
        private static final long serialVersionUID = 1L;

        Immunity() {
            super(AbilityNamesies.IMMUNITY, "The immune system of the Pok\u00e9mon prevents it from getting poisoned.");
        }

        @Override
        public StatusNamesies getStatus() {
            return StatusNamesies.POISONED;
        }
    }

    static class PastelVeil extends Ability implements StatusPreventionAbility {
        private static final long serialVersionUID = 1L;

        PastelVeil() {
            super(AbilityNamesies.PASTEL_VEIL, "Protects the Pokémon from being poisoned.");
        }

        @Override
        public StatusNamesies getStatus() {
            return StatusNamesies.POISONED;
        }
    }

    static class SnowCloak extends Ability implements StageChangingEffect, EncounterRateMultiplier {
        private static final long serialVersionUID = 1L;

        SnowCloak() {
            super(AbilityNamesies.SNOW_CLOAK, "Boosts evasion in a hailstorm.");
        }

        @Override
        public int adjustStage(Battle b, ActivePokemon p, Stat s) {
            return s == Stat.EVASION && b.isWeather(WeatherNamesies.HAILING) ? 1 : 0;
        }

        @Override
        public double getEncounterRateMultiplier() {
            return Game.getPlayer().getArea().getWeather() == WeatherState.SNOW ? .5 : 1;
        }
    }

    static class MarvelScale extends Ability implements SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        MarvelScale() {
            super(AbilityNamesies.MARVEL_SCALE, "The Pok\u00e9mon's marvelous scales boost the Defense stat if it has a status condition.");
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.DEFENSE;
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return p.hasStatus();
        }

        @Override
        public double getModifier() {
            return 1.5;
        }
    }

    static class Multiscale extends Ability implements OpponentPowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Multiscale() {
            super(AbilityNamesies.MULTISCALE, "Reduces the amount of damage the Pok\u00e9mon takes when its HP is full.");
        }

        @Override
        public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.fullHealth() ? .5 : 1;
        }
    }

    static class ShadowShield extends Ability implements OpponentPowerChangeEffect {
        private static final long serialVersionUID = 1L;

        ShadowShield() {
            super(AbilityNamesies.SHADOW_SHIELD, "Reduces the amount of damage the Pok\u00e9mon takes while its HP is full.");
        }

        @Override
        public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.fullHealth() ? .5 : 1;
        }

        @Override
        public boolean unbreakableMold() {
            // Ability is not ignored even when the opponent breaks the mold
            return true;
        }
    }

    static class SheerForce extends Ability implements EffectChanceMultiplierEffect, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        SheerForce() {
            super(AbilityNamesies.SHEER_FORCE, "Removes additional effects to increase the power of moves when attacking.");
        }

        @Override
        public double getEffectChanceMultiplier(ActivePokemon user) {
            return user.getAttack().hasSecondaryEffects() ? 0 : 1;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getAttack().hasSecondaryEffects() ? 1.3 : 1;
        }
    }

    static class Hustle extends Ability implements StatModifyingEffect, MaxLevelWildEncounterEffect {
        private static final long serialVersionUID = 1L;

        Hustle() {
            super(AbilityNamesies.HUSTLE, "Boosts the Attack stat, but lowers accuracy.");
        }

        @Override
        public double modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s) {
            if (s == Stat.ATTACK) {
                return 1.5;
            } else if (s == Stat.ACCURACY) {
                return .8;
            } else {
                return 1;
            }
        }
    }

    static class HugePower extends Ability implements SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        HugePower() {
            super(AbilityNamesies.HUGE_POWER, "Doubles the Pok\u00e9mon's Attack stat.");
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.ATTACK;
        }

        @Override
        public double getModifier() {
            return 2;
        }
    }

    static class SpeedBoost extends Ability implements EndTurnEffect {
        private static final long serialVersionUID = 1L;

        SpeedBoost() {
            super(AbilityNamesies.SPEED_BOOST, "Its Speed stat is boosted every turn.");
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            new StageModifier(1, Stat.SPEED).modify(b, victim, victim, CastSource.ABILITY);
        }
    }

    static class MagicBounce extends Ability implements TargetSwapperEffect {
        private static final long serialVersionUID = 1L;

        MagicBounce() {
            super(AbilityNamesies.MAGIC_BOUNCE, "Reflects status moves, instead of getting hit by them.");
        }

        @Override
        public boolean swapTarget(Battle b, ActivePokemon user, ActivePokemon opponent) {
            if (user.getAttack().isMagicReflectable()) {
                Messages.add(opponent.getName() + "'s " + this.getName() + " reflected " + user.getName() + "'s move!");
                return true;
            }

            return false;
        }
    }

    static class SuperLuck extends Ability implements CritStageEffect {
        private static final long serialVersionUID = 1L;

        SuperLuck() {
            super(AbilityNamesies.SUPER_LUCK, "The Pok\u00e9mon is so lucky that the critical-hit ratios of its moves are boosted.");
        }
    }

    static class ShadowTag extends Ability implements OpponentTrappingEffect {
        private static final long serialVersionUID = 1L;

        ShadowTag() {
            super(AbilityNamesies.SHADOW_TAG, "This Pok\u00e9mon steps on the opposing Pok\u00e9mon's shadow to prevent it from escaping.");
        }

        @Override
        public boolean trapOpponent(Battle b, ActivePokemon escaper, ActivePokemon trapper) {
            return !escaper.hasAbility(this.namesies);
        }

        @Override
        public String opponentTrappingMessage(ActivePokemon escaper, ActivePokemon trapper) {
            return trapper.getName() + "'s " + this.getName() + " prevents " + escaper.getName() + " from escaping!";
        }
    }

    static class Overcoat extends Ability implements WeatherBlockerEffect, PowderBlocker {
        private static final long serialVersionUID = 1L;

        Overcoat() {
            super(AbilityNamesies.OVERCOAT, "Protects the Pok\u00e9mon from things like sand, hail, and powder.");
        }

        @Override
        public boolean block(WeatherNamesies weather) {
            return true;
        }

        @Override
        public String getBlockMessage(ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " protects it from powder moves!";
        }
    }

    static class MagmaArmor extends Ability implements StatusPreventionAbility {
        private static final long serialVersionUID = 1L;

        MagmaArmor() {
            super(AbilityNamesies.MAGMA_ARMOR, "The Pok\u00e9mon is covered with hot magma, which prevents the Pok\u00e9mon from becoming frozen.");
        }

        @Override
        public StatusNamesies getStatus() {
            return StatusNamesies.FROZEN;
        }
    }

    static class SuctionCups extends Ability implements AttackBlocker, NoSwapEffect {
        private static final long serialVersionUID = 1L;

        SuctionCups() {
            super(AbilityNamesies.SUCTION_CUPS, "This Pok\u00e9mon uses suction cups to stay in one spot to negate all moves and items that force switching out.");
        }

        @Override
        public boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            Attack attack = user.getAttack();
            return attack instanceof SwapOpponentEffect && attack.isStatusMove();
        }

        @Override
        public String getBlockMessage(ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " prevents it from switching!";
        }
    }

    static class Steadfast extends Ability implements EffectReceivedEffect {
        private static final long serialVersionUID = 1L;

        Steadfast() {
            super(AbilityNamesies.STEADFAST, "The Pok\u00e9mon's determination boosts the Speed stat each time the Pok\u00e9mon flinches.");
        }

        @Override
        public void receiveEffect(Battle b, ActivePokemon caster, ActivePokemon victim, EffectNamesies effectType) {
            if (effectType == PokemonEffectNamesies.FLINCH) {
                new StageModifier(1, Stat.SPEED).modify(b, victim, victim, CastSource.ABILITY);
            }
        }
    }

    static class SandStream extends Ability implements EntryEffect {
        private static final long serialVersionUID = 1L;

        SandStream() {
            super(AbilityNamesies.SAND_STREAM, "The Pok\u00e9mon summons a sandstorm when it enters a battle.");
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            Messages.add(enterer.getName() + "'s " + this.getName() + " whipped up a sandstorm!");
            b.addEffect(WeatherNamesies.SANDSTORM.getEffect());
        }
    }

    static class Regenerator extends Ability implements SwitchOutEffect {
        private static final long serialVersionUID = 1L;

        Regenerator() {
            super(AbilityNamesies.REGENERATOR, "Restores a little HP when withdrawn from battle.");
        }

        @Override
        public void switchOut(ActivePokemon switchee) {
            if (!switchee.hasStatus(StatusNamesies.FAINTED)) {
                switchee.healHealthFraction(1/3.0);
            }
        }
    }

    static class PoisonHeal extends Ability {
        private static final long serialVersionUID = 1L;

        PoisonHeal() {
            super(AbilityNamesies.POISON_HEAL, "Restores HP if the Pok\u00e9mon is poisoned, instead of losing HP.");
        }
    }

    static class Truant extends Ability implements EndTurnEffect, BeforeAttackPreventingEffect {
        private static final long serialVersionUID = 1L;

        private boolean lazyface;

        Truant() {
            super(AbilityNamesies.TRUANT, "The Pok\u00e9mon can't use a move the following turn if it uses one.");
            this.lazyface = false;
        }

        @Override
        public boolean isReplaceable() {
            return false;
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (victim.hasStatus(StatusNamesies.ASLEEP)) {
                lazyface = false;
            } else {
                lazyface = !lazyface;
            }
        }

        @Override
        public boolean canAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            if (lazyface) {
                Messages.add(attacking.getName() + " is loafing around!");
                return false;
            }

            return true;
        }
    }

    static class WonderGuard extends Ability implements AttackBlocker {
        private static final long serialVersionUID = 1L;

        WonderGuard() {
            super(AbilityNamesies.WONDER_GUARD, "Its mysterious power only lets supereffective moves hit the Pok\u00e9mon.");
        }

        @Override
        public boolean isStealable() {
            return false;
        }

        @Override
        public boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Status moves, super-effective moves, and None-type moves always hit
            return !user.getAttack().isStatusMove() && !TypeAdvantage.isSuperEffective(user, victim, b) && !user.isAttackType(Type.NO_TYPE);
        }

        @Override
        public String getBlockMessage(ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " makes it immune to " + user.getAttack().getName() + "!";
        }
    }

    static class Normalize extends Ability implements ChangeAttackTypeEffect, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Normalize() {
            super(AbilityNamesies.NORMALIZE, "All the Pok\u00e9mon's moves become Normal type. The power of those moves is boosted a little.");
        }

        @Override
        public Type changeAttackType(Attack attack, Type original) {
            return Type.NORMAL;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.NORMAL) ? 1.2 : 1;
        }
    }

    static class Stall extends Ability implements StallingEffect {
        private static final long serialVersionUID = 1L;

        Stall() {
            super(AbilityNamesies.STALL, "The Pok\u00e9mon moves after all other Pok\u00e9mon do.");
        }
    }

    static class PurePower extends Ability implements SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        PurePower() {
            super(AbilityNamesies.PURE_POWER, "Using its pure power, the Pok\u00e9mon doubles its Attack stat.");
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.ATTACK;
        }

        @Override
        public double getModifier() {
            return 2;
        }
    }

    static class RoughSkin extends Ability implements PhysicalContactEffect {
        private static final long serialVersionUID = 1L;

        RoughSkin() {
            super(AbilityNamesies.ROUGH_SKIN, "This Pok\u00e9mon inflicts damage with its rough skin to the attacker on contact.");
        }

        @Override
        public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
            user.reduceHealthFraction(b, 1/8.0, user.getName() + " was hurt by " + victim.getName() + "'s " + this.getName() + "!");
        }
    }

    static class SolidRock extends Ability implements OpponentPowerChangeEffect {
        private static final long serialVersionUID = 1L;

        SolidRock() {
            super(AbilityNamesies.SOLID_ROCK, "Reduces the power of supereffective attacks taken.");
        }

        @Override
        public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return TypeAdvantage.isSuperEffective(user, victim, b) ? .75 : 1;
        }
    }

    static class WhiteSmoke extends Ability implements StatProtectingEffect, EncounterRateMultiplier {
        private static final long serialVersionUID = 1L;

        WhiteSmoke() {
            super(AbilityNamesies.WHITE_SMOKE, "The Pok\u00e9mon is protected by its white smoke, which prevents other Pok\u00e9mon from lowering its stats.");
        }

        @Override
        public double getEncounterRateMultiplier() {
            return .5;
        }

        @Override
        public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
            return true;
        }

        @Override
        public String preventionMessage(ActivePokemon p, Stat s) {
            return p.getName() + "'s " + this.getName() + " prevents its stats from being lowered!";
        }
    }

    static class ToxicBoost extends Ability implements SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        ToxicBoost() {
            super(AbilityNamesies.TOXIC_BOOST, "Powers up physical attacks when the Pok\u00e9mon is poisoned.");
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.ATTACK;
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return p.hasStatus(StatusNamesies.POISONED);
        }

        @Override
        public double getModifier() {
            return 1.5;
        }
    }

    static class FlareBoost extends Ability implements SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        FlareBoost() {
            super(AbilityNamesies.FLARE_BOOST, "Powers up special attacks when the Pokémon is burned.");
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SP_ATTACK;
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return p.hasStatus(StatusNamesies.BURNED);
        }

        @Override
        public double getModifier() {
            return 1.5;
        }
    }

    static class Anticipation extends Ability implements EntryEffect {
        private static final long serialVersionUID = 1L;

        Anticipation() {
            super(AbilityNamesies.ANTICIPATION, "The Pok\u00e9mon can sense an opposing Pok\u00e9mon's dangerous moves.");
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            ActivePokemon other = b.getOtherPokemon(enterer);
            for (Move move : other.getMoves(b)) {
                Attack attack = move.getAttack();
                if (attack instanceof OhkoMove || attack.getActualType().getAdvantage().isSuperEffective(enterer, b)) {
                    Messages.add(enterer.getName() + "'s " + this.getName() + " made it shudder!");
                    break;
                }
            }
        }
    }

    static class StormDrain extends Ability implements AttackBlocker {
        private static final long serialVersionUID = 1L;

        StormDrain() {
            super(AbilityNamesies.STORM_DRAIN, "Draws in all Water-type moves. Instead of being hit by Water-type moves, it boosts its Sp. Atk.");
        }

        @Override
        public void alternateEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            new StageModifier(1, Stat.SP_ATTACK).modify(b, victim, victim, CastSource.ABILITY);
        }

        @Override
        public boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.WATER);
        }

        @Override
        public String getBlockMessage(ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " makes it immune to " + Type.WATER.getName() + "-type moves!";
        }
    }

    static class ColorChange extends Ability implements TakeDamageEffect, ChangeTypeSource {
        private static final long serialVersionUID = 1L;

        private Type type;

        ColorChange() {
            super(AbilityNamesies.COLOR_CHANGE, "The Pok\u00e9mon's type becomes the type of the move used on it.");
        }

        @Override
        public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            Type t = user.getAttackType();
            if (!victim.isType(b, t)) {
                type = t;
                Effect.cast(PokemonEffectNamesies.CHANGE_TYPE, b, victim, victim, CastSource.ABILITY, true);
            }
        }

        @Override
        public PokeType getType(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return new PokeType(type);
        }
    }

    static class IceBody extends Ability implements EndTurnEffect, WeatherBlockerEffect {
        private static final long serialVersionUID = 1L;

        IceBody() {
            super(AbilityNamesies.ICE_BODY, "The Pok\u00e9mon gradually regains HP in a hailstorm.");
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (b.isWeather(WeatherNamesies.HAILING)) {
                victim.healHealthFraction(1/16.0, b, victim.getName() + "'s HP was restored due to its " + this.getName() + "!");
            }
        }

        @Override
        public boolean block(WeatherNamesies weather) {
            return weather == WeatherNamesies.HAILING;
        }
    }

    static class LightMetal extends Ability implements HalfWeightEffect {
        private static final long serialVersionUID = 1L;

        LightMetal() {
            super(AbilityNamesies.LIGHT_METAL, "Halves the Pok\u00e9mon's weight.");
        }
    }

    static class HeavyMetal extends Ability implements DoubleWeightEffect {
        private static final long serialVersionUID = 1L;

        HeavyMetal() {
            super(AbilityNamesies.HEAVY_METAL, "Doubles the Pok\u00e9mon's weight.");
        }
    }

    static class Drizzle extends Ability implements EntryEffect {
        private static final long serialVersionUID = 1L;

        Drizzle() {
            super(AbilityNamesies.DRIZZLE, "The Pok\u00e9mon makes it rain when it enters a battle.");
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            Messages.add(enterer.getName() + "'s " + this.getName() + " started a downpour!");
            b.addEffect(WeatherNamesies.RAINING.getEffect());
        }
    }

    static class AirLock extends Ability implements WeatherEliminatingEffect {
        private static final long serialVersionUID = 1L;

        AirLock() {
            super(AbilityNamesies.AIR_LOCK, "Eliminates the effects of weather.");
        }

        @Override
        public String getEliminateMessage(ActivePokemon eliminator) {
            return eliminator.getName() + "'s " + this.getName() + " eliminated the weather!";
        }
    }

    static class Defiant extends Ability implements StatLoweredEffect {
        private static final long serialVersionUID = 1L;

        Defiant() {
            super(AbilityNamesies.DEFIANT, "Boosts the Pok\u00e9mon's Attack stat sharply when its stats are lowered.");
        }

        @Override
        public void takeItToTheNextLevel(Battle b, ActivePokemon victim, boolean selfCaster) {
            // Doesn't raise for self-inflicted lowers
            if (!selfCaster) {
                new StageModifier(2, Stat.ATTACK).modify(b, victim, victim, CastSource.ABILITY);
            }
        }
    }

    static class Competitive extends Ability implements StatLoweredEffect {
        private static final long serialVersionUID = 1L;

        Competitive() {
            super(AbilityNamesies.COMPETITIVE, "Boosts the Sp. Atk stat sharply when a stat is lowered.");
        }

        @Override
        public void takeItToTheNextLevel(Battle b, ActivePokemon victim, boolean selfCaster) {
            // Doesn't raise for self-inflicted lowers
            if (!selfCaster) {
                new StageModifier(2, Stat.SP_ATTACK).modify(b, victim, victim, CastSource.ABILITY);
            }
        }
    }

    static class FlowerGift extends Ability implements SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        FlowerGift() {
            super(AbilityNamesies.FLOWER_GIFT, "Boosts the Attack and Sp. Def stats of itself and allies when it is sunny.");
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.ATTACK || s == Stat.SP_DEFENSE;
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return b.isWeather(WeatherNamesies.SUNNY);
        }

        @Override
        public double getModifier() {
            return 1.5;
        }
    }

    static class Aftermath extends Ability implements PhysicalContactEffect {
        private static final long serialVersionUID = 1L;

        Aftermath() {
            super(AbilityNamesies.AFTERMATH, "Damages the attacker if it contacts the Pok\u00e9mon with a finishing hit.");
        }

        @Override
        public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (victim.isFainted(b) && !user.hasAbility(AbilityNamesies.DAMP)) {
                user.reduceHealthFraction(b, 1/4.0, user.getName() + " was hurt by " + victim.getName() + "'s " + this.getName() + "!");
            }
        }
    }

    static class Heatproof extends Ability implements OpponentPowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Heatproof() {
            super(AbilityNamesies.HEATPROOF, "The heatproof body of the Pok\u00e9mon halves the damage from Fire-type moves that hit it.");
        }

        @Override
        public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.FIRE) ? .5 : 1;
        }
    }

    static class SandForce extends Ability implements WeatherBlockerEffect, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        SandForce() {
            super(AbilityNamesies.SAND_FORCE, "Boosts the power of Rock-, Ground-, and Steel-type moves in a sandstorm.");
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.ROCK, Type.GROUND, Type.STEEL) && b.isWeather(WeatherNamesies.SANDSTORM) ? 1.3 : 1;
        }

        @Override
        public boolean block(WeatherNamesies weather) {
            return weather == WeatherNamesies.SANDSTORM;
        }
    }

    static class SnowWarning extends Ability implements EntryEffect {
        private static final long serialVersionUID = 1L;

        SnowWarning() {
            super(AbilityNamesies.SNOW_WARNING, "The Pok\u00e9mon summons a hailstorm when it enters a battle.");
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            Messages.add(enterer.getName() + "'s " + this.getName() + " caused it to hail!");
            b.addEffect(WeatherNamesies.HAILING.getEffect());
        }
    }

    static class MotorDrive extends Ability implements AttackBlocker {
        private static final long serialVersionUID = 1L;

        MotorDrive() {
            super(AbilityNamesies.MOTOR_DRIVE, "Boosts its Speed stat if hit by an Electric-type move, instead of taking damage.");
        }

        @Override
        public void alternateEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            new StageModifier(1, Stat.SPEED).modify(b, victim, victim, CastSource.ABILITY);
        }

        @Override
        public boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.ELECTRIC);
        }

        @Override
        public String getBlockMessage(ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " makes it immune to " + Type.ELECTRIC.getName() + "-type moves!";
        }
    }

    static class Justified extends Ability implements TakeDamageEffect {
        private static final long serialVersionUID = 1L;

        Justified() {
            super(AbilityNamesies.JUSTIFIED, "Being hit by a Dark-type move boosts the Attack stat of the Pok\u00e9mon, for justice.");
        }

        @Override
        public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (user.isAttackType(Type.DARK)) {
                new StageModifier(1, Stat.ATTACK).modify(b, victim, victim, CastSource.ABILITY);
            }
        }
    }

    static class CursedBody extends Ability implements PhysicalContactEffect {
        private static final long serialVersionUID = 1L;

        CursedBody() {
            super(AbilityNamesies.CURSED_BODY, "May disable a move used on the Pok\u00e9mon.");
        }

        @Override
        public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (RandomUtils.chanceTest(30)) {
                user.setLastMoveUsed();
                String message = victim.getName() + "'s " + this.getName() + " disabled " + user.getName() + "'s " + user.getAttack().getName();
                Effect.apply(PokemonEffectNamesies.DISABLE, b, victim, user, CastSource.ABILITY, message);
            }
        }
    }

    static class SlowStart extends Ability implements EndTurnEffect, EntryEffect, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        int count;

        SlowStart() {
            super(AbilityNamesies.SLOW_START, "For five turns, the Pok\u00e9mon's Attack and Speed stats are halved.");
            this.count = 0;
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            count++;
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            count = 0;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.ATTACK || s == Stat.SPEED;
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return count < 5;
        }

        @Override
        public double getModifier() {
            return .5;
        }
    }

    static class BadDreams extends Ability implements EndTurnEffect {
        private static final long serialVersionUID = 1L;

        BadDreams() {
            super(AbilityNamesies.BAD_DREAMS, "Reduces the HP of sleeping opposing Pok\u00e9mon.");
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            ActivePokemon other = b.getOtherPokemon(victim);
            if (other.hasStatus(StatusNamesies.ASLEEP)) {
                other.reduceHealthFraction(b, 1/8.0, other.getName() + " was hurt by " + victim.getName() + "'s " + this.getName() + "!");
            }
        }
    }

    static class VictoryStar extends Ability implements SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        VictoryStar() {
            super(AbilityNamesies.VICTORY_STAR, "Boosts the accuracy of its allies and itself.");
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.ACCURACY;
        }

        @Override
        public double getModifier() {
            return 1.1;
        }
    }

    static class Contrary extends Ability implements ModifyStageValueEffect {
        private static final long serialVersionUID = 1L;

        Contrary() {
            super(AbilityNamesies.CONTRARY, "Makes stat changes have an opposite effect.");
        }

        @Override
        public int modifyStageValue() {
            return -1;
        }
    }

    static class BigPecks extends Ability implements StatProtectingEffect {
        private static final long serialVersionUID = 1L;

        BigPecks() {
            super(AbilityNamesies.BIG_PECKS, "Protects the Pok\u00e9mon from Defense-lowering effects.");
        }

        @Override
        public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
            return stat == Stat.DEFENSE;
        }

        @Override
        public String preventionMessage(ActivePokemon p, Stat s) {
            return p.getName() + "'s " + this.getName() + " prevents its " + s.getName() + " from being lowered!";
        }
    }

    static class PoisonTouch extends Ability implements OpponentTakeDamageEffect {
        private static final long serialVersionUID = 1L;

        PoisonTouch() {
            super(AbilityNamesies.POISON_TOUCH, "May poison a target when the Pok\u00e9mon makes contact.");
        }

        @Override
        public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (RandomUtils.chanceTest(30)) {
                StatusNamesies.POISONED.getStatus().apply(b, user, victim, CastSource.ABILITY);
            }
        }
    }

    static class Prankster extends Ability implements PriorityChangeEffect {
        private static final long serialVersionUID = 1L;

        Prankster() {
            super(AbilityNamesies.PRANKSTER, "Gives priority to a status move.");
        }

        @Override
        public int changePriority(Battle b, ActivePokemon user) {
            return user.getAttack().isStatusMove() && !b.getOtherPokemon(user).isType(b, Type.DARK) ? 1 : 0;
        }
    }

    static class WonderSkin extends Ability implements SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        WonderSkin() {
            super(AbilityNamesies.WONDER_SKIN, "Makes status moves more likely to miss.");
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.EVASION;
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return opp.getAttack().isStatusMove();
        }

        @Override
        public double getModifier() {
            return 1.5;
        }
    }

    static class Mummy extends Ability implements PhysicalContactEffect, ChangeAbilitySource {
        private static final long serialVersionUID = 1L;

        Mummy() {
            super(AbilityNamesies.MUMMY, "Contact with the Pok\u00e9mon changes the attacker's Ability to Mummy.");
        }

        @Override
        public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (user.hasAbility(this.namesies) || !user.getAbility().isReplaceable()) {
                return;
            }

            // Cast the change ability effect onto the user
            Effect.cast(PokemonEffectNamesies.CHANGE_ABILITY, b, victim, user, CastSource.ABILITY, true);
        }

        @Override
        public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return AbilityNamesies.MUMMY.getNewAbility();
        }

        @Override
        public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return victim.getName() + "'s ability was changed to " + this.namesies().getName() + "!";
        }
    }

    static class Defeatist extends Ability implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Defeatist() {
            super(AbilityNamesies.DEFEATIST, "Halves the Pok\u00e9mon's Attack and Sp. Atk stats when its HP becomes half or less.");
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getHPRatio() < .5 ? .5 : 1;
        }
    }

    static class WeakArmor extends Ability implements TakeDamageEffect {
        private static final long serialVersionUID = 1L;

        WeakArmor() {
            super(AbilityNamesies.WEAK_ARMOR, "Physical attacks to the Pok\u00e9mon lower its Defense stat but sharply raise its Speed stat.");
        }

        @Override
        public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (user.getAttack().getCategory() == MoveCategory.PHYSICAL) {
                new StageModifier(-1, Stat.DEFENSE).set(2, Stat.SPEED).modify(b, victim, victim, CastSource.ABILITY);
            }
        }
    }

    static class Illusion extends Ability implements EntryEffect, SwitchOutEffect, TakeDamageEffect, ChangeTypeEffect, NameChanger {
        private static final long serialVersionUID = 1L;

        private boolean activated;
        private String illusionName;
        private PokeType illusionType;
        private PokemonNamesies illusionSpecies;
        private boolean illusionShiny;

        private void breakIllusion(Battle b, ActivePokemon victim) {
            // If the Illusion is already broken, no worries
            if (!activated) {
                return;
            }

            activated = false;
            Messages.add(victim.getName() + "'s Illusion was broken!");

            Messages.add(new MessageUpdate().withNewPokemon(victim.namesies(), victim.isShiny(), true, victim.isPlayer()));
            Messages.add(new MessageUpdate().updatePokemon(b, victim));
        }

        Illusion() {
            super(AbilityNamesies.ILLUSION, "Comes out disguised as the Pok\u00e9mon in the party's last spot.");
            this.activated = false;
        }

        @Override
        public boolean isStealable() {
            return false;
        }

        @Override
        public void deactivate(Battle b, ActivePokemon victim) {
            breakIllusion(b, victim);
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            // No Illusion today...
            if (!activated) {
                return;
            }

            // Display the Illusion changes
            Messages.add(new MessageUpdate().withNewPokemon(illusionSpecies, illusionShiny, false, enterer.isPlayer()));
            Messages.add(new MessageUpdate().updatePokemon(b, enterer));
        }

        @Override
        public String getNameChange() {
            return activated ? illusionName : null;
        }

        @Override
        public void setNameChange(Battle b, ActivePokemon victim) {
            List<ActivePokemon> team = b.getTrainer(victim).getActiveTeam();
            ActivePokemon illusion = null;

            // Starting from the back of the party, locate the first conscious Pokemon that is of a different species to be the illusion
            for (ActivePokemon temp : new ReverseIterable<>(team)) {

                // If the Pokemon in back cannot fight for any reason -- do nothing
                if (!temp.canFight()) {
                    continue;
                }

                // If the Pokemon in back is the same species at the current Pokemon -- do nothing
                if (temp.namesies() == victim.namesies()) {
                    continue;
                }

                // Otherwise, we've found our Illusion!
                illusion = temp;
                break;
            }

            // No valid Pokemon to be as an illusion -- do not activate
            if (illusion == null) {
                return;
            }

            // Otherwise, we're in the illusion
            activated = true;

            illusionName = illusion.getName();
            illusionType = illusion.getActualType();
            illusionSpecies = illusion.namesies();
            illusionShiny = illusion.isShiny();
        }

        @Override
        public void switchOut(ActivePokemon switchee) {
            activated = false;
        }

        @Override
        public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            breakIllusion(b, victim);
        }

        @Override
        public PokeType getType(Battle b, ActivePokemon p, boolean display) {
            // Type change is only for appearances
            return display && activated ? illusionType : null;
        }

        @Override
        public boolean isActive() {
            return activated;
        }
    }

    static class Analytic extends Ability implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Analytic() {
            super(AbilityNamesies.ANALYTIC, "Boosts move power when the Pok\u00e9mon moves last.");
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return !b.isFirstAttack() ? 1.3 : 1;
        }
    }

    static class SapSipper extends Ability implements AttackBlocker {
        private static final long serialVersionUID = 1L;

        SapSipper() {
            super(AbilityNamesies.SAP_SIPPER, "Boosts the Attack stat if hit by a Grass-type move, instead of taking damage.");
        }

        @Override
        public void alternateEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            new StageModifier(1, Stat.ATTACK).modify(b, victim, victim, CastSource.ABILITY);
        }

        @Override
        public boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.GRASS);
        }

        @Override
        public String getBlockMessage(ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " makes it immune to " + Type.GRASS.getName() + "-type moves!";
        }
    }

    static class IronBarbs extends Ability implements PhysicalContactEffect {
        private static final long serialVersionUID = 1L;

        IronBarbs() {
            super(AbilityNamesies.IRON_BARBS, "Inflicts damage to the attacker on contact with iron barbs.");
        }

        @Override
        public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
            user.reduceHealthFraction(b, 1/8.0, user.getName() + " was hurt by " + victim.getName() + "'s " + this.getName() + "!");
        }
    }

    static class MoldBreaker extends Ability implements MoldBreakerEffect, EntryEffect {
        private static final long serialVersionUID = 1L;

        MoldBreaker() {
            super(AbilityNamesies.MOLD_BREAKER, "Moves can be used on the target regardless of its Abilities.");
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            Messages.add(enterer.getName() + " breaks the mold!");
        }
    }

    static class Teravolt extends Ability implements MoldBreakerEffect, EntryEffect {
        private static final long serialVersionUID = 1L;

        Teravolt() {
            super(AbilityNamesies.TERAVOLT, "Moves can be used on the target regardless of its Abilities.");
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            Messages.add(enterer.getName() + " is radiating a bursting aura!");
        }
    }

    static class Turboblaze extends Ability implements MoldBreakerEffect, EntryEffect {
        private static final long serialVersionUID = 1L;

        Turboblaze() {
            super(AbilityNamesies.TURBOBLAZE, "Moves can be used on the target regardless of its Abilities.");
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            Messages.add(enterer.getName() + " is radiating a blazing aura!");
        }
    }

    static class RunAway extends Ability implements DefiniteEscape {
        private static final long serialVersionUID = 1L;

        RunAway() {
            super(AbilityNamesies.RUN_AWAY, "Enables a sure getaway from wild Pok\u00e9mon.");
        }
    }

    static class StickyHold extends Ability implements StickyHoldEffect {
        private static final long serialVersionUID = 1L;

        StickyHold() {
            super(AbilityNamesies.STICKY_HOLD, "Items held by the Pok\u00e9mon are stuck fast and cannot be removed by other Pok\u00e9mon.");
        }
    }

    static class Klutz extends Ability implements ItemBlockerEffect {
        private static final long serialVersionUID = 1L;

        Klutz() {
            super(AbilityNamesies.KLUTZ, "The Pok\u00e9mon can't use any held items.");
        }
    }

    static class Unburden extends Ability implements SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        Unburden() {
            super(AbilityNamesies.UNBURDEN, "Boosts the Speed stat if the Pok\u00e9mon's held item is used or lost.");
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return p.hasEffect(PokemonEffectNamesies.CONSUMED_ITEM);
        }

        @Override
        public double getModifier() {
            return 2;
        }
    }

    static class Pickpocket extends Ability implements PhysicalContactEffect, ItemSwapperEffect {
        private static final long serialVersionUID = 1L;

        Pickpocket() {
            super(AbilityNamesies.PICKPOCKET, "Steals an item from an attacker that made direct contact.");
        }

        @Override
        public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Steal from the Pokemon who made physical contact with you
            if (!victim.isFainted(b) && victim.canStealItem(b, user)) {
                this.swapItems(b, victim, user);
            }
        }

        @Override
        public String getSwitchMessage(ActivePokemon user, HoldItem userItem, ActivePokemon victim, HoldItem victimItem) {
            return user.getName() + " stole " + victim.getName() + "'s " + victimItem.getName() + "!";
        }
    }

    static class Harvest extends Ability implements EndTurnEffect {
        private static final long serialVersionUID = 1L;

        Harvest() {
            super(AbilityNamesies.HARVEST, "May create another Berry after one is used.");
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            // Does nothing if victim is holding something
            if (victim.isHoldingItem()) {
                return;
            }

            // Does nothing if victim never consumed an item
            ItemHolder consumed = (ItemHolder)victim.getEffect(PokemonEffectNamesies.CONSUMED_ITEM);
            if (consumed == null) {
                return;
            }

            // Restore the item if applicable
            HoldItem restored = consumed.getItem();
            if (restored instanceof Berry && (b.isWeather(WeatherNamesies.SUNNY) || RandomUtils.chanceTest(50))) {
                victim.giveItem(restored);
                Messages.add(victim.getName() + "'s " + this.getName() + " restored its " + restored.getName() + "!");
            }
        }
    }

    static class Pickup extends Ability implements EndBattleEffect, ItemListHolder {
        private static final long serialVersionUID = 1L;

        private static final List<ItemNamesies> items = new ArrayList<>();
        static {
            addItem(ItemNamesies.POTION, 30);
            addItem(ItemNamesies.ANTIDOTE, 20);
            addItem(ItemNamesies.SUPER_POTION, 20);
            addItem(ItemNamesies.GREAT_BALL, 20);
            addItem(ItemNamesies.REPEL, 20);
            addItem(ItemNamesies.FULL_HEAL, 15);
            addItem(ItemNamesies.ETHER, 15);
            addItem(ItemNamesies.ULTRA_BALL, 15);
            addItem(ItemNamesies.HYPER_POTION, 10);
            addItem(ItemNamesies.REVIVE, 10);
            addItem(ItemNamesies.RARE_CANDY, 10);
            addItem(ItemNamesies.HEART_SCALE, 10);
            addItem(ItemNamesies.ELIXIR, 10);
            addItem(ItemNamesies.SUN_STONE, 5);
            addItem(ItemNamesies.MOON_STONE, 5);
            addItem(ItemNamesies.DAWN_STONE, 5);
            addItem(ItemNamesies.DUSK_STONE, 5);
            addItem(ItemNamesies.FIRE_STONE, 5);
            addItem(ItemNamesies.WATER_STONE, 5);
            addItem(ItemNamesies.LEAF_STONE, 5);
            addItem(ItemNamesies.ICE_STONE, 5);
            addItem(ItemNamesies.THUNDER_STONE, 5);
            addItem(ItemNamesies.SHINY_STONE, 5);
            addItem(ItemNamesies.KINGS_ROCK, 5);
            addItem(ItemNamesies.MAX_REVIVE, 5);
            addItem(ItemNamesies.NUGGET, 5);
            addItem(ItemNamesies.PRISM_SCALE, 5);
            addItem(ItemNamesies.DESTINY_KNOT, 5);
            addItem(ItemNamesies.FULL_RESTORE, 5);
            addItem(ItemNamesies.PP_UP, 5);
            addItem(ItemNamesies.LEFTOVERS, 5);
            addItem(ItemNamesies.MAX_ELIXIR, 5);
            addItem(ItemNamesies.BIG_NUGGET, 1);
            addItem(ItemNamesies.BALM_MUSHROOM, 1);
            addItem(ItemNamesies.HP_UP, 1);
            addItem(ItemNamesies.PROTEIN, 1);
            addItem(ItemNamesies.IRON, 1);
            addItem(ItemNamesies.CALCIUM, 1);
            addItem(ItemNamesies.CARBOS, 1);
            addItem(ItemNamesies.ZINC, 1);
            addItem(ItemNamesies.RARE_BONE, 1);
        }

        private static void addItem(ItemNamesies item, int quantity) {
            for (int i = 0; i < quantity; i++) {
                items.add(item);
            }
        }

        Pickup() {
            super(AbilityNamesies.PICKUP, "The Pok\u00e9mon may pick up the item an opposing Pok\u00e9mon used during a battle. It may pick up items outside of battle, too.");
        }

        @Override
        public void afterBattle(Trainer player, ActivePokemon p) {
            if (!p.isHoldingItem() && RandomUtils.chanceTest(10)) {
                ItemNamesies item = RandomUtils.getRandomValue(items);
                p.giveItem(item);
                Messages.add(p.getName() + " picked up " + StringUtils.articleString(item.getName()) + "!");
            }
        }

        @Override
        public List<ItemNamesies> getItems() {
            return items;
        }
    }

    static class Unnerve extends Ability implements EntryEffect, OpponentItemBlockerEffect {
        private static final long serialVersionUID = 1L;

        Unnerve() {
            super(AbilityNamesies.UNNERVE, "Unnerves opposing Pok\u00e9mon and makes them unable to eat Berries.");
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            Messages.add(enterer.getName() + "'s " + this.getName() + " made " + b.getOtherPokemon(enterer).getName() + " too nervous to eat berries!");
        }

        @Override
        public boolean blockItem(ItemNamesies item) {
            return item.getItem() instanceof Berry;
        }
    }

    static class HoneyGather extends Ability implements EndBattleEffect {
        private static final long serialVersionUID = 1L;

        HoneyGather() {
            super(AbilityNamesies.HONEY_GATHER, "The Pok\u00e9mon may gather Honey after a battle.");
        }

        @Override
        public void afterBattle(Trainer player, ActivePokemon p) {
            if (!p.isHoldingItem() && RandomUtils.chanceTest(5*(int)Math.ceil(p.getLevel()/10.0))) {
                p.giveItem(ItemNamesies.HONEY);
            }
        }
    }

    static class Gluttony extends Ability {
        private static final long serialVersionUID = 1L;

        Gluttony() {
            super(AbilityNamesies.GLUTTONY, "Makes the Pok\u00e9mon eat a held Berry when its HP drops to half or less, which is sooner than usual.");
        }
    }

    static class Multitype extends Ability implements ChangeTypeEffect {
        private static final long serialVersionUID = 1L;

        Multitype() {
            super(AbilityNamesies.MULTITYPE, "Changes the Pok\u00e9mon's type to match the Plate it holds.");
        }

        @Override
        public PokeType getType(Battle b, ActivePokemon p, boolean display) {
            HoldItem item = p.getHeldItem();
            return item instanceof PlateItem ? new PokeType(((PlateItem)item).getType()) : null;
        }

        @Override
        public boolean isStealable() {
            return false;
        }

        @Override
        public boolean isReplaceable() {
            return false;
        }
    }

    static class RKSSystem extends Ability implements ChangeTypeEffect {
        private static final long serialVersionUID = 1L;

        RKSSystem() {
            super(AbilityNamesies.RKS_SYSTEM, "Changes the Pok\u00e9mon's type to match the memory disc it holds.");
        }

        @Override
        public PokeType getType(Battle b, ActivePokemon p, boolean display) {
            HoldItem item = p.getHeldItem();
            return item instanceof MemoryItem ? new PokeType(((MemoryItem)item).getType()) : null;
        }

        @Override
        public boolean isStealable() {
            return false;
        }

        @Override
        public boolean isReplaceable() {
            return false;
        }
    }

    static class Forecast extends Ability implements ChangeTypeEffect {
        private static final long serialVersionUID = 1L;

        Forecast() {
            super(AbilityNamesies.FORECAST, "The Pok\u00e9mon transforms with the weather to change its type to Water, Fire, or Ice.");
        }

        @Override
        public boolean isStealable() {
            return false;
        }

        @Override
        public PokeType getType(Battle b, ActivePokemon p, boolean display) {
            return new PokeType(b.getWeather().getElement());
        }
    }

    static class Bulletproof extends Ability implements AttackBlocker {
        private static final long serialVersionUID = 1L;

        Bulletproof() {
            super(AbilityNamesies.BULLETPROOF, "Protects the Pok\u00e9mon from some ball and bomb moves.");
        }

        @Override
        public boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getAttack().isMoveType(MoveType.BOMB_BALL);
        }

        @Override
        public String getBlockMessage(ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " prevents bomb/ball moves!";
        }
    }

    static class AuraBreak extends Ability {
        private static final long serialVersionUID = 1L;

        AuraBreak() {
            super(AbilityNamesies.AURA_BREAK, "The effects of \"Aura\" Abilities are reversed to lower the power of affected moves.");
        }
    }

    static class FairyAura extends Ability implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        FairyAura() {
            super(AbilityNamesies.FAIRY_AURA, "Powers up each Pok\u00e9mon's Fairy-type moves.");
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (user.isAttackType(Type.FAIRY)) {
                return 1 + .33*(victim.hasAbility(AbilityNamesies.AURA_BREAK) ? -1 : 1);
            }

            return 1;
        }
    }

    static class DarkAura extends Ability implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        DarkAura() {
            super(AbilityNamesies.DARK_AURA, "Powers up each Pok\u00e9mon's Dark-type moves.");
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (user.isAttackType(Type.DARK)) {
                return 1 + .33*(victim.hasAbility(AbilityNamesies.AURA_BREAK) ? -1 : 1);
            }

            return 1;
        }
    }

    static class Magician extends Ability implements ApplyDamageEffect, ItemSwapperEffect {
        private static final long serialVersionUID = 1L;

        Magician() {
            super(AbilityNamesies.MAGICIAN, "The Pok\u00e9mon steals the held item of a Pok\u00e9mon it hits with a move.");
        }

        @Override
        public void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Steal the victim's item when damage is dealt
            if (!user.isFainted(b) && user.canStealItem(b, victim)) {
                this.swapItems(b, user, victim);
            }
        }

        @Override
        public String getSwitchMessage(ActivePokemon user, HoldItem userItem, ActivePokemon victim, HoldItem victimItem) {
            return user.getName() + " stole " + victim.getName() + "'s " + victimItem.getName() + "!";
        }
    }

    static class CheekPouch extends Ability {
        private static final long serialVersionUID = 1L;

        CheekPouch() {
            super(AbilityNamesies.CHEEK_POUCH, "Restores HP as well when the Pok\u00e9mon eats a Berry.");
        }
    }

    static class StrongJaw extends Ability implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        StrongJaw() {
            super(AbilityNamesies.STRONG_JAW, "The Pok\u00e9mon's strong jaw boosts the power of its biting moves.");
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getAttack().isMoveType(MoveType.BITING) ? 1.5 : 1;
        }
    }

    static class MegaLauncher extends Ability implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        MegaLauncher() {
            super(AbilityNamesies.MEGA_LAUNCHER, "Powers up aura and pulse moves.");
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getAttack().isMoveType(MoveType.AURA_PULSE) ? 1.5 : 1;
        }
    }

    static class ToughClaws extends Ability implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        ToughClaws() {
            super(AbilityNamesies.TOUGH_CLAWS, "Powers up moves that make direct contact.");
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isMakingContact() ? 1.33 : 1;
        }
    }

    static class SweetVeil extends Ability implements StatusPreventionAbility {
        private static final long serialVersionUID = 1L;

        SweetVeil() {
            super(AbilityNamesies.SWEET_VEIL, "Prevents itself and ally Pok\u00e9mon from falling asleep.");
        }

        @Override
        public StatusNamesies getStatus() {
            return StatusNamesies.ASLEEP;
        }
    }

    static class AromaVeil extends Ability implements MultipleEffectPreventionAbility {
        private static final long serialVersionUID = 1L;

        private static final Set<PokemonEffectNamesies> PREVENTION_EFFECTS = EnumSet.of(
                PokemonEffectNamesies.INFATUATION,
                PokemonEffectNamesies.ENCORE,
                PokemonEffectNamesies.DISABLE,
                PokemonEffectNamesies.TORMENT,
                PokemonEffectNamesies.TAUNT,
                PokemonEffectNamesies.HEAL_BLOCK
        );

        AromaVeil() {
            super(AbilityNamesies.AROMA_VEIL, "Protects itself and its allies from attacks that limit their move choices.");
        }

        @Override
        public Set<PokemonEffectNamesies> getPreventableEffects() {
            return PREVENTION_EFFECTS;
        }
    }

    static class Healer extends Ability implements EndTurnEffect {
        private static final long serialVersionUID = 1L;

        Healer() {
            super(AbilityNamesies.HEALER, "Sometimes heals its status condition.");
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (victim.hasStatus() && RandomUtils.chanceTest(1, 3)) {
                victim.removeStatus(b, CastSource.ABILITY);
            }
        }
    }

    static class Pixilate extends Ability implements ChangeAttackTypeEffect, EndTurnEffect, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        private boolean activated;

        Pixilate() {
            super(AbilityNamesies.PIXILATE, "Normal-type moves become Fairy-type moves. The power of those moves is boosted a little.");
            this.activated = false;
        }

        @Override
        public Type changeAttackType(Attack attack, Type original) {
            if (original == Type.NORMAL) {
                this.activated = true;
                return Type.FAIRY;
            }

            return original;
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            this.activated = false;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return activated ? 1.2 : 1;
        }
    }

    static class Refrigerate extends Ability implements ChangeAttackTypeEffect, EndTurnEffect, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        private boolean activated;

        Refrigerate() {
            super(AbilityNamesies.REFRIGERATE, "Normal-type moves become Ice-type moves. The power of those moves is boosted a little.");
            this.activated = false;
        }

        @Override
        public Type changeAttackType(Attack attack, Type original) {
            if (original == Type.NORMAL) {
                this.activated = true;
                return Type.ICE;
            }

            return original;
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            this.activated = false;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return activated ? 1.2 : 1;
        }
    }

    static class Schooling extends Ability implements EndTurnEffect, EntryEffect, DifferentStatEffect, FormAbility {
        private static final long serialVersionUID = 1L;

        private static final BaseStats SOLO_STATS = new BaseStats(new int[] { 45, 20, 20, 25, 25, 40 });
        private static final BaseStats SCHOOL_STATS = new BaseStats(new int[] { 45, 140, 130, 140, 135, 30 });

        private boolean schoolForm;

        Schooling() {
            super(AbilityNamesies.SCHOOLING, "When it has a lot of HP, the Pok\u00e9mon forms a powerful school. It stops schooling when its HP is low.");
            this.schoolForm = false;
        }

        private void checkFormChange(ActivePokemon formsie) {
            if (this.schoolForm != formsie.getHPRatio() >= .25 && formsie.getLevel() >= 20) {
                changeForm(formsie);
            }
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            schoolForm = false;
            checkFormChange(enterer);
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            checkFormChange(victim);
        }

        private void changeForm(ActivePokemon formsie) {
            this.schoolForm = !schoolForm;
            String message = formsie.getName() + " changed into " + (schoolForm ? "School" : "Solo") + " Forme!";
            addFormMessage(formsie, message, schoolForm);
        }

        @Override
        public boolean isStealable() {
            return false;
        }

        @Override
        public boolean isReplaceable() {
            return false;
        }

        @Override
        public boolean isNeutralizable() {
            return false;
        }

        @Override
        public Integer getStat(ActivePokemon user, Stat stat) {
            // Need to calculate the new stat -- yes, I realize this is super inefficient and whatever whatever whatever
            BaseStats stats = schoolForm ? SCHOOL_STATS : SOLO_STATS;
            return user.stats().calculate(stat, stats);
        }
    }

    static class ShieldsDown extends Ability implements EndTurnEffect, EntryEffect, DifferentStatEffect, FormAbility {
        private static final long serialVersionUID = 1L;

        private static final BaseStats CORE_STATS = new BaseStats(new int[] { 60, 100, 60, 100, 60, 120 });
        private static final BaseStats METEOR_STATS = new BaseStats(new int[] { 60, 60, 100, 60, 100, 60 });

        private boolean meteorForm;

        ShieldsDown() {
            super(AbilityNamesies.SHIELDS_DOWN, "When its HP becomes half or less, the Pok\u00e9mon's shell breaks and it becomes aggressive.");
            this.meteorForm = false;
        }

        private void checkFormChange(ActivePokemon formsie) {
            if (this.meteorForm != formsie.getHPRatio() > .5) {
                changeForm(formsie);
            }
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            meteorForm = false;
            checkFormChange(enterer);
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            checkFormChange(victim);
        }

        private void changeForm(ActivePokemon formsie) {
            this.meteorForm = !meteorForm;
            String message = formsie.getName() + " changed into " + (meteorForm ? "Meteor" : "Core") + " Forme!";
            addFormMessage(formsie, message, meteorForm);
        }

        @Override
        public boolean isStealable() {
            return false;
        }

        @Override
        public boolean isReplaceable() {
            return false;
        }

        @Override
        public boolean isNeutralizable() {
            return false;
        }

        @Override
        public Integer getStat(ActivePokemon user, Stat stat) {
            // Need to calculate the new stat -- yes, I realize this is super inefficient and whatever whatever whatever
            BaseStats stats = meteorForm ? METEOR_STATS : CORE_STATS;
            return user.stats().calculate(stat, stats);
        }
    }

    static class StanceChange extends Ability implements StartAttackEffect, EntryEffect, DifferentStatEffect, FormAbility {
        private static final long serialVersionUID = 1L;

        private static final BaseStats SHIELD_STATS = new BaseStats(new int[] { 60, 50, 150, 50, 150, 60 });
        private static final BaseStats BLADE_STATS = new BaseStats(new int[] { 60, 150, 50, 150, 50, 60 });

        private boolean bladeForm;

        StanceChange() {
            super(AbilityNamesies.STANCE_CHANGE, "The Pok\u00e9mon changes its form to Blade Forme when it uses an attack move, and changes to Shield Forme when it uses King's Shield.");
            this.bladeForm = false;
        }

        @Override
        public void beforeAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // TODO: This should really activate right before printing the attacking move, not after
            if ((!bladeForm && !attacking.getAttack().isStatusMove()) || (bladeForm && attacking.getAttack().namesies() == AttackNamesies.KINGS_SHIELD)) {
                changeForm(attacking);
            }
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            Messages.add(enterer.getName() + " is in Shield Forme!");
            bladeForm = false;
        }

        private void changeForm(ActivePokemon formsie) {
            this.bladeForm = !bladeForm;
            String message = formsie.getName() + " changed into " + (bladeForm ? "Blade" : "Shield") + " Forme!";
            addFormMessage(formsie, message, bladeForm);
        }

        @Override
        public boolean isStealable() {
            return false;
        }

        @Override
        public boolean isReplaceable() {
            return false;
        }

        @Override
        public boolean isNeutralizable() {
            return false;
        }

        @Override
        public Integer getStat(ActivePokemon user, Stat stat) {
            // Need to calculate the new stat -- yes, I realize this is super inefficient and whatever whatever whatever
            BaseStats stats = bladeForm ? BLADE_STATS : SHIELD_STATS;
            return user.stats().calculate(stat, stats);
        }
    }

    static class FurCoat extends Ability implements SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        FurCoat() {
            super(AbilityNamesies.FUR_COAT, "Halves the damage from physical moves onto this Pok\u00e9mon.");
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.DEFENSE;
        }

        @Override
        public double getModifier() {
            return 2;
        }
    }

    static class GrassPelt extends Ability implements SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        GrassPelt() {
            super(AbilityNamesies.GRASS_PELT, "Boosts the Pok\u00e9mon's Defense stat in Grassy Terrain.");
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.DEFENSE;
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return b.hasEffect(TerrainNamesies.GRASSY_TERRAIN);
        }

        @Override
        public double getModifier() {
            return 1.5;
        }
    }

    static class SurgeSurfer extends Ability implements SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        SurgeSurfer() {
            super(AbilityNamesies.SURGE_SURFER, "Doubles the Pok\u00e9mon's Speed stat on Electric Terrain.");
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return b.hasEffect(TerrainNamesies.ELECTRIC_TERRAIN);
        }

        @Override
        public double getModifier() {
            return 2;
        }
    }

    static class FlowerVeil extends Ability implements StatusPreventionEffect, StatProtectingEffect {
        private static final long serialVersionUID = 1L;

        FlowerVeil() {
            super(AbilityNamesies.FLOWER_VEIL, "Grass-type Pok\u00e9mon are protected from status conditions and the lowering of their stats.");
        }

        @Override
        public ApplyResult preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusNamesies status) {
            if (victim.isType(b, Type.GRASS)) {
                return ApplyResult.failure(victim.getName() + "'s " + this.getName() + " prevents status conditions!");
            }

            return ApplyResult.success();
        }

        @Override
        public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
            return victim.isType(b, Type.GRASS);
        }

        @Override
        public String preventionMessage(ActivePokemon p, Stat s) {
            return p.getName() + "'s " + this.getName() + " prevents its stats from being lowered!";
        }
    }

    static class GaleWings extends Ability implements PriorityChangeEffect {
        private static final long serialVersionUID = 1L;

        GaleWings() {
            super(AbilityNamesies.GALE_WINGS, "Gives priority to Flying-type moves when the Pok\u00e9mon's HP is full.");
        }

        @Override
        public int changePriority(Battle b, ActivePokemon user) {
            return user.fullHealth() && user.isAttackType(Type.FLYING) ? 1 : 0;
        }
    }

    static class Protean extends Ability implements StartAttackEffect, ChangeTypeSource {
        private static final long serialVersionUID = 1L;

        private Type type;

        Protean() {
            super(AbilityNamesies.PROTEAN, "Changes the Pok\u00e9mon's type to the type of the move it's about to use.");
        }

        @Override
        public void beforeAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // Protean activates for all moves except for Struggle
            if (attacking.getAttack().namesies() != AttackNamesies.STRUGGLE) {
                type = attacking.getAttackType();
                Effect.cast(PokemonEffectNamesies.CHANGE_TYPE, b, attacking, attacking, CastSource.ABILITY, true);
            }
        }

        @Override
        public PokeType getType(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return new PokeType(type);
        }
    }

    static class Stamina extends Ability implements TakeDamageEffect {
        private static final long serialVersionUID = 1L;

        Stamina() {
            super(AbilityNamesies.STAMINA, "Boosts the Defense stat when hit by an attack.");
        }

        @Override
        public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            new StageModifier(1, Stat.DEFENSE).modify(b, victim, victim, CastSource.ABILITY);
        }
    }

    static class WaterCompaction extends Ability implements TakeDamageEffect {
        private static final long serialVersionUID = 1L;

        WaterCompaction() {
            super(AbilityNamesies.WATER_COMPACTION, "Boosts the Pok\u00e9mon's Defense stat sharply when hit by a Water-type move.");
        }

        @Override
        public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (user.isAttackType(Type.WATER)) {
                new StageModifier(2, Stat.DEFENSE).modify(b, victim, victim, CastSource.ABILITY);
            }
        }
    }

    static class Merciless extends Ability implements AlwaysCritEffect {
        private static final long serialVersionUID = 1L;

        Merciless() {
            super(AbilityNamesies.MERCILESS, "The Pok\u00e9mon's attacks become critical hits if the target is poisoned.");
        }

        @Override
        public boolean shouldCrit(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            return defending.hasStatus(StatusNamesies.POISONED);
        }
    }

    static class WaterBubble extends Ability implements OpponentPowerChangeEffect, StatusPreventionAbility, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        WaterBubble() {
            super(AbilityNamesies.WATER_BUBBLE, "Lowers the power of Fire-type moves done to the Pok\u00e9mon and prevents the Pok\u00e9mon from getting a burn.");
        }

        @Override
        public StatusNamesies getStatus() {
            return StatusNamesies.BURNED;
        }

        @Override
        public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.FIRE) ? .5 : 1;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.WATER) ? 2 : 1;
        }
    }

    static class Steelworker extends Ability implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Steelworker() {
            super(AbilityNamesies.STEELWORKER, "Powers up Steel-type moves.");
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.STEEL) ? 1.5 : 1;
        }
    }

    static class Berserk extends Ability implements TakeDamageEffect {
        private static final long serialVersionUID = 1L;

        Berserk() {
            super(AbilityNamesies.BERSERK, "Boosts the Pok\u00e9mon's Sp. Atk stat when it takes a hit that causes its HP to become half or less.");
        }

        @Override
        public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (victim.getHPRatio() < .5 && (victim.getHP() + victim.getDamageTaken())/(double)victim.getMaxHP() >= .5) {
                new StageModifier(1, Stat.SP_ATTACK).modify(b, victim, victim, CastSource.ABILITY);
            }
        }
    }

    static class WimpOut extends Ability implements TakeDamageEffect {
        private static final long serialVersionUID = 1L;

        WimpOut() {
            super(AbilityNamesies.WIMP_OUT, "The Pok\u00e9mon cowardly switches out when its HP becomes half or less.");
        }

        @Override
        public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (victim.getHPRatio() < .5 && (victim.getHP() + victim.getDamageTaken())/(double)victim.getMaxHP() >= .5) {
                victim.switcheroo(b, victim, CastSource.ABILITY, true);
            }
        }
    }

    static class EmergencyExit extends Ability implements TakeDamageEffect {
        private static final long serialVersionUID = 1L;

        EmergencyExit() {
            super(AbilityNamesies.EMERGENCY_EXIT, "The Pok\u00e9mon, sensing danger, switches out when its HP becomes half or less.");
        }

        @Override
        public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (victim.getHPRatio() < .5 && (victim.getHP() + victim.getDamageTaken())/(double)victim.getMaxHP() >= .5) {
                victim.switcheroo(b, victim, CastSource.ABILITY, true);
            }
        }
    }

    static class LongReach extends Ability {
        private static final long serialVersionUID = 1L;

        LongReach() {
            super(AbilityNamesies.LONG_REACH, "The Pok\u00e9mon uses its moves without making contact with the target.");
        }
    }

    static class LiquidVoice extends Ability implements ChangeAttackTypeEffect {
        private static final long serialVersionUID = 1L;

        LiquidVoice() {
            super(AbilityNamesies.LIQUID_VOICE, "All sound-based moves become Water-type moves.");
        }

        @Override
        public Type changeAttackType(Attack attack, Type original) {
            if (attack.isMoveType(MoveType.SOUND_BASED)) {
                return Type.WATER;
            }

            return original;
        }
    }

    static class Triage extends Ability implements PriorityChangeEffect {
        private static final long serialVersionUID = 1L;

        Triage() {
            super(AbilityNamesies.TRIAGE, "Gives priority to a healing move.");
        }

        @Override
        public int changePriority(Battle b, ActivePokemon user) {
            return user.getAttack().isMoveType(MoveType.HEALING) ? 3 : 0;
        }
    }

    static class Galvanize extends Ability implements ChangeAttackTypeEffect, EndTurnEffect, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        private boolean activated;

        Galvanize() {
            super(AbilityNamesies.GALVANIZE, "Normal-type moves become Electric-type moves. The power of those moves is boosted a little.");
            this.activated = false;
        }

        @Override
        public Type changeAttackType(Attack attack, Type original) {
            if (original == Type.NORMAL) {
                this.activated = true;
                return Type.ELECTRIC;
            }

            return original;
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            this.activated = false;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return activated ? 1.2 : 1;
        }
    }

    static class Disguise extends Ability implements AbsorbDamageEffect, FormAbility {
        private static final long serialVersionUID = 1L;

        private boolean activated;

        Disguise() {
            super(AbilityNamesies.DISGUISE, "Once per battle, the shroud that covers the Pok\u00e9mon can protect it from an attack.");
            this.activated = false;
        }

        @Override
        public boolean isStealable() {
            return false;
        }

        @Override
        public boolean isReplaceable() {
            return false;
        }

        @Override
        public boolean absorbDamage(Battle b, ActivePokemon damageTaker, int damageAmount) {
            // This method is only called for direct damage so do not need to check if attacking
            if (!activated) {
                activated = true;
                addFormMessage(damageTaker, damageTaker.getName() + "'s disguise was busted!!", true);
                return true;
            }
            return false;
        }
    }

    static class Stakeout extends Ability implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Stakeout() {
            super(AbilityNamesies.STAKEOUT, "Doubles the damage dealt to the target's replacement if the target switches out.");
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return !user.isFirstTurn() && victim.isFirstTurn() ? 2 : 1;
        }
    }

    static class Corrosion extends Ability {
        private static final long serialVersionUID = 1L;

        Corrosion() {
            super(AbilityNamesies.CORROSION, "The Pok\u00e9mon can poison the target even if it's a Steel or Poison type.");
        }
    }

    static class Comatose extends Ability implements EntryEffect, SleepyFightsterEffect, SuperDuperEndTurnEffect {
        private static final long serialVersionUID = 1L;

        // NOTE: This does not work exactly the same as in game
        // New mechanics -- Pokemon with Comatose will:
        // Become afflicted with a permanent Sleep condition upon entering battle
        // Still be able to attack while sleeping
        // Wake up from items, uproar, wake-up slap, etc.
        // Fall back to sleep at the end of turn if applicable -- if another status condition is acquired during this time, it will remove it
        // Other Pokemon are free to copy this ability
        private boolean nightyNight(Battle b, ActivePokemon sleepyHead) {

            // Sleepy head is already a sleepster nighty night
            if (sleepyHead.hasStatus(StatusNamesies.ASLEEP)) {
                return false;
            }

            // Deadsies can't sleep
            if (sleepyHead.isActuallyDead()) {
                return false;
            }

            if (StatusNamesies.ASLEEP.getStatus().appliesWithoutStatusCheck(b, sleepyHead, sleepyHead).isSuccess()) {
                sleepyHead.removeStatus();
                StatusNamesies.ASLEEP.getStatus().apply(b, sleepyHead, sleepyHead, CastSource.ABILITY);
                sleepyHead.getStatus().setTurns(-1);
                return true;
            }

            return false;
        }

        Comatose() {
            super(AbilityNamesies.COMATOSE, "It's always drowsing and will never wake up. It can attack without waking up.");
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            nightyNight(b, enterer);
        }

        @Override
        public boolean theVeryVeryEnd(Battle b, ActivePokemon p) {
            return nightyNight(b, p);
        }
    }

    static class Dazzling extends Ability implements AttackBlocker {
        private static final long serialVersionUID = 1L;

        Dazzling() {
            super(AbilityNamesies.DAZZLING, "Surprises the opposing Pok\u00e9mon, making it unable to attack using priority moves.");
        }

        @Override
        public boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            return b.getAttackPriority(user) > 0;
        }

        @Override
        public String getBlockMessage(ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " prevents priority moves!!";
        }
    }

    static class QueenlyMajesty extends Ability implements AttackBlocker {
        private static final long serialVersionUID = 1L;

        QueenlyMajesty() {
            super(AbilityNamesies.QUEENLY_MAJESTY, "Its majesty pressures the opposing Pok\u00e9mon, making it unable to attack using priority moves.");
        }

        @Override
        public boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            return b.getAttackPriority(user) > 0;
        }

        @Override
        public String getBlockMessage(ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " prevents priority moves!!";
        }
    }

    static class Dancer extends Ability implements OpponentEndAttackEffect {
        private static final long serialVersionUID = 1L;

        private boolean activated;

        Dancer() {
            super(AbilityNamesies.DANCER, "When another Pok\u00e9mon uses a dance move, it can use a dance move following it regardless of its Speed.");
            activated = false;
        }

        @Override
        public void endsies(Battle b, ActivePokemon attacking) {
            Attack attack = attacking.getAttack();
            if (attack.isMoveType(MoveType.DANCE) && (!attacking.hasAbility(this.namesies()) || !attacking.getAbility().isActive())) {
                activated = true;
                ActivePokemon abilify = this.getOtherPokemon(b, attacking);
                Messages.add(abilify.getName() + "'s " + this.getName() + " allowed it to join in the dance!");
                abilify.callFullNewMove(b, attacking, attack.namesies());
                activated = false;
            }
        }

        @Override
        public boolean isActive() {
            return this.activated;
        }
    }

    static class InnardsOut extends Ability implements FaintEffect {
        private static final long serialVersionUID = 1L;

        InnardsOut() {
            super(AbilityNamesies.INNARDS_OUT, "Damages the attacker landing the finishing hit by the amount equal to its last HP.");
        }

        @Override
        public void deathWish(Battle b, ActivePokemon dead, ActivePokemon murderer) {
            murderer.indirectReduceHealth(b, dead.getDamageTaken(), false, murderer.getName() + " was hurt by " + dead.getName() + "'s " + this.getName() + "!");
        }
    }

    static class Fluffy extends Ability implements OpponentPowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Fluffy() {
            super(AbilityNamesies.FLUFFY, "Halves the damage taken from moves that make direct contact, but doubles that of Fire-type moves.");
        }

        @Override
        public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            double modifier = 1;
            if (user.isAttackType(Type.FIRE)) {
                modifier *= 2;
            }
            if (user.isMakingContact()) {
                modifier /= 2;
            }
            return modifier;
        }
    }

    static class TanglingHair extends Ability implements PhysicalContactEffect {
        private static final long serialVersionUID = 1L;

        TanglingHair() {
            super(AbilityNamesies.TANGLING_HAIR, "Contact with the Pok\u00e9mon lowers the attacker's Speed stat.");
        }

        @Override
        public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
            new StageModifier(-1, Stat.SPEED).modify(b, victim, user, CastSource.ABILITY);
        }
    }

    static class Gooey extends Ability implements PhysicalContactEffect {
        private static final long serialVersionUID = 1L;

        Gooey() {
            super(AbilityNamesies.GOOEY, "Contact with the Pok\u00e9mon lowers the attacker's Speed stat.");
        }

        @Override
        public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
            new StageModifier(-1, Stat.SPEED).modify(b, victim, user, CastSource.ABILITY);
        }
    }

    static class PsychicSurge extends Ability implements EntryEffect {
        private static final long serialVersionUID = 1L;

        PsychicSurge() {
            super(AbilityNamesies.PSYCHIC_SURGE, "Turns the ground into Psychic Terrain when the Pok\u00e9mon enters a battle.");
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            String message = enterer.getName() + "'s " + this.getName() + " changed the field to Psychic Terrain!";
            Effect.cast(TerrainNamesies.PSYCHIC_TERRAIN, b, enterer, enterer, CastSource.ABILITY, message);
        }
    }

    static class ElectricSurge extends Ability implements EntryEffect {
        private static final long serialVersionUID = 1L;

        ElectricSurge() {
            super(AbilityNamesies.ELECTRIC_SURGE, "Turns the ground into Electric Terrain when the Pok\u00e9mon enters a battle.");
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            String message = enterer.getName() + "'s " + this.getName() + " changed the field to Electric Terrain!";
            Effect.cast(TerrainNamesies.ELECTRIC_TERRAIN, b, enterer, enterer, CastSource.ABILITY, message);
        }
    }

    static class MistySurge extends Ability implements EntryEffect {
        private static final long serialVersionUID = 1L;

        MistySurge() {
            super(AbilityNamesies.MISTY_SURGE, "Turns the ground into Misty Terrain when the Pok\u00e9mon enters a battle.");
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            String message = enterer.getName() + "'s " + this.getName() + " changed the field to Misty Terrain!";
            Effect.cast(TerrainNamesies.MISTY_TERRAIN, b, enterer, enterer, CastSource.ABILITY, message);
        }
    }

    static class GrassySurge extends Ability implements EntryEffect {
        private static final long serialVersionUID = 1L;

        GrassySurge() {
            super(AbilityNamesies.GRASSY_SURGE, "Turns the ground into Grassy Terrain when the Pok\u00e9mon enters a battle.");
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            String message = enterer.getName() + "'s " + this.getName() + " changed the field to Grassy Terrain!";
            Effect.cast(TerrainNamesies.GRASSY_TERRAIN, b, enterer, enterer, CastSource.ABILITY, message);
        }
    }

    static class Moody extends Ability implements EndTurnEffect {
        private static final long serialVersionUID = 1L;

        private Stat modifyStat(Battle b, ActivePokemon victim, List<Stat> potential, int delta) {
            // All stats maxed/mined -- don't alter
            if (potential.isEmpty()) {
                return null;
            }

            Stat stat = RandomUtils.getRandomValue(potential);
            boolean success = new StageModifier(delta, stat).modify(b, victim, victim, CastSource.ABILITY);

            return success ? stat : null;
        }

        Moody() {
            super(AbilityNamesies.MOODY, "Raises one stat sharply and lowers another every turn.");
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            // Sharply increase random stat
            Stat modified = modifyStat(b, victim, victim.getStages().getNonMaxStats(), 2);

            // Cannot alter the same stat
            List<Stat> nonMined = victim.getStages().getNonMinStats();
            if (modified != null) {
                nonMined.remove(modified);
            }

            // Decrease random stat
            modifyStat(b, victim, nonMined, -1);
        }
    }

    // WE ARE SYLAR WITH THE POWER OF ALCHEMY
    // More seriously though yes this ability was changed from ally to opponent
    static class PowerOfAlchemy extends Ability implements MurderEffect, ChangeAbilitySource {
        private static final long serialVersionUID = 1L;

        private AbilityNamesies stolenWithThePowerOfAlchemy;
        private String message;

        PowerOfAlchemy() {
            super(AbilityNamesies.POWER_OF_ALCHEMY, "The Pok\u00e9mon copies the Ability of a defeated enemy.");
        }

        @Override
        public boolean isStealable() {
            return false;
        }

        @Override
        public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return stolenWithThePowerOfAlchemy.getNewAbility();
        }

        @Override
        public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return message;
        }

        @Override
        public void killWish(Battle b, ActivePokemon dead, ActivePokemon murderer) {
            // Steal the dead's ability when you MURDER THEM
            Ability deadAbility = dead.getAbility();
            if (!deadAbility.isStealable()) {
                return;
            }

            this.stolenWithThePowerOfAlchemy = deadAbility.namesies();
            this.message = murderer.getName() + " stole " + dead.getName() + "'s " + deadAbility.getName() + " with the Power of Alchemy!!!";

            // Cast the change ability effect onto the murderer to give the dead's ability
            Effect.cast(PokemonEffectNamesies.CHANGE_ABILITY, b, murderer, murderer, CastSource.ABILITY, true);
        }
    }

    static class BallFetch extends Ability {
        private static final long serialVersionUID = 1L;

        BallFetch() {
            super(AbilityNamesies.BALL_FETCH, "If the Pokémon is not holding an item, it will fetch the Poké Ball from the first failed throw of the battle.");
        }
    }

    static class CottonDown extends Ability implements OpponentApplyDamageEffect {
        private static final long serialVersionUID = 1L;

        CottonDown() {
            super(AbilityNamesies.COTTON_DOWN, "When the Pokémon is hit by an attack, it scatters cotton fluff around and lowers the Speed stat of all Pokémon except itself.");
        }

        @Override
        public void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            new StageModifier(-1, Stat.SPEED).modify(b, victim, user, CastSource.ABILITY);
        }
    }

    static class DauntlessShield extends Ability implements EntryEffect {
        private static final long serialVersionUID = 1L;

        DauntlessShield() {
            super(AbilityNamesies.DAUNTLESS_SHIELD, "Boosts the Pokémon's Defense stat when the Pokémon enters a battle.");
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            new StageModifier(1, Stat.DEFENSE).modify(b, enterer, enterer, CastSource.ABILITY);
        }
    }

    static class GorillaTactics extends Ability implements ChoiceEffect {
        private static final long serialVersionUID = 1L;

        GorillaTactics() {
            super(AbilityNamesies.GORILLA_TACTICS, "Boosts the Pokémon's Attack stat but only allows the use of the first selected move.");
        }

        @Override
        public Stat getBoosted() {
            return Stat.ATTACK;
        }
    }

    static class GulpMissile extends Ability implements StartAttackEffect, EntryEffect, OpponentApplyDamageEffect, FormAbility {
        private static final long serialVersionUID = 1L;

        private GulpForm gulpForm;

        private enum GulpForm {
            NORMAL, GULPING, GORGING
        }

        GulpMissile() {
            super(AbilityNamesies.GULP_MISSILE, "When the Pokémon uses Surf or Dive, it will come back with prey. When it takes damage, it will spit out the prey to attack.");
        }

        @Override
        public boolean isReplaceable() {
            return false;
        }

        @Override
        public boolean isStealable() {
            return false;
        }

        @Override
        public void beforeAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // Already gulping/gorging -- nothing else to do right now
            if (this.gulpForm != GulpForm.NORMAL) {
                return;
            }

            // Using Surf or Dive will make Cramorant change forms
            AttackNamesies attack = attacking.getAttack().namesies();
            if (attack == AttackNamesies.SURF || attack == AttackNamesies.DIVE) {
                // Gorge on a Pika when low on health, otherwise get them fishies
                this.gulpForm = attacking.getHPRatio() < .5 ? GulpForm.GORGING : GulpForm.GULPING;

                // TODO: Once Cramorant's images are actually set up will need to adjust since this only includes one form
                // TODO: Game is currently freezing here when using Dive (semi-inv + image change)
                String gulping = this.gulpForm == GulpForm.GULPING ? "gulping" : "gorging";
                this.addFormMessage(attacking, attacking.getName() + " is " + gulping + "!!", true);
            }
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            this.gulpForm = GulpForm.NORMAL;
        }

        @Override
        public void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Empty mouth = empty effect
            if (this.gulpForm == GulpForm.NORMAL) {
                return;
            }

            boolean gulping = this.gulpForm == GulpForm.GULPING;

            // Release the prey and return to normal form
            this.gulpForm = GulpForm.NORMAL;
            addFormMessage(victim, victim.getName() + " released its prey!!!", false);

            // When attacked, releases its prey dealing 1/4 max HP damage to the attacker
            // Additionally, lowers defense when gulping or paralyzes when gorging
            // Note: Use Effect cast source here so it uses the default messaging instead of referring to Gulp Missile
            user.reduceHealthFraction(b, .25, "");
            if (gulping) {
                new StageModifier(-1, Stat.DEFENSE).modify(b, victim, user, CastSource.EFFECT);
            } else {
                StatusNamesies.PARALYZED.getStatus().apply(b, victim, user, CastSource.EFFECT);
            }
        }
    }

    static class HungerSwitch extends Ability implements EndTurnEffect, FormAbility, BooleanHolder {
        private static final long serialVersionUID = 1L;

        private boolean hangryMode;

        HungerSwitch() {
            super(AbilityNamesies.HUNGER_SWITCH, "The Pokémon changes its form, alternating between its Full Belly Mode and Hangry Mode after the end of each turn.");
            this.hangryMode = false;
        }

        @Override
        public boolean isStealable() {
            return false;
        }

        private void changeForm(ActivePokemon formsie) {
            this.hangryMode = !hangryMode;
            String message = formsie.getName() + " changed into " + (hangryMode ? "Hangry" : "Full Belly") + " Forme!";
            addFormMessage(formsie, message, hangryMode);
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            changeForm(victim);
        }

        @Override
        public boolean getBoolean() {
            return this.hangryMode;
        }
    }

    static class IceFace extends Ability implements EntryEffect, AbsorbDamageEffect, DifferentStatEffect, WeatherChangedEffect, FormAbility {
        private static final long serialVersionUID = 1L;

        private static final BaseStats ICE_STATS = new BaseStats(new int[] { 75, 80, 110, 65, 90, 50 });
        private static final BaseStats NO_ICE_STATS = new BaseStats(new int[] { 75, 80, 70, 65, 50, 130 });

        private boolean nonIcy;

        IceFace() {
            super(AbilityNamesies.ICE_FACE, "The Pokémon's ice head can take a physical attack as a substitute, but the attack also changes the Pokémon's appearance. The ice will be restored when it hails.");
            this.nonIcy = false;
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            // Note: In game form persists when recalled and if this is ever changed it should re-enter Ice Form if hailing
            Messages.add(enterer.getName() + " is in Ice Forme!");
            nonIcy = false;
        }

        @Override
        public boolean absorbDamage(Battle b, ActivePokemon damageTaker, int damageAmount) {
            // When hit by a physical move in Ice Form, the Pokemon will absorb the hit and lose its icy face
            if (!nonIcy && b.getOtherPokemon(damageTaker).getAttack().getCategory() == MoveCategory.PHYSICAL) {
                changeForm(damageTaker);
                return true;
            }

            return false;
        }

        @Override
        public void weatherChanged(WeatherNamesies weather, ActivePokemon effectHolder) {
            // When hail starts, change back to ice face
            if (nonIcy && weather == WeatherNamesies.HAILING) {
                changeForm(effectHolder);
            }
        }

        private void changeForm(ActivePokemon formsie) {
            this.nonIcy = !nonIcy;
            String message = formsie.getName() + " changed into " + (nonIcy ? "No Ice" : "Ice") + " Forme!";
            addFormMessage(formsie, message, nonIcy);
        }

        @Override
        public boolean isStealable() {
            return false;
        }

        @Override
        public boolean isReplaceable() {
            return false;
        }

        @Override
        public boolean isNeutralizable() {
            return false;
        }

        @Override
        public Integer getStat(ActivePokemon user, Stat stat) {
            // Need to calculate the new stat -- yes, I realize this is super inefficient and whatever whatever whatever
            BaseStats stats = nonIcy ? NO_ICE_STATS : ICE_STATS;
            return user.stats().calculate(stat, stats);
        }
    }

    static class IceScales extends Ability implements OpponentPowerChangeEffect {
        private static final long serialVersionUID = 1L;

        IceScales() {
            super(AbilityNamesies.ICE_SCALES, "The Pokémon is protected by ice scales, which halve the damage taken from special moves.");
        }

        @Override
        public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getAttack().getCategory() == MoveCategory.SPECIAL ? .5 : 1;
        }
    }

    static class IntrepidSword extends Ability implements EntryEffect {
        private static final long serialVersionUID = 1L;

        IntrepidSword() {
            super(AbilityNamesies.INTREPID_SWORD, "Boosts the Pokémon's Attack stat when the Pokémon enters a battle.");
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            new StageModifier(1, Stat.ATTACK).modify(b, enterer, enterer, CastSource.ABILITY);
        }
    }

    static class Libero extends Ability implements StartAttackEffect, ChangeTypeSource {
        private static final long serialVersionUID = 1L;

        private Type type;

        Libero() {
            super(AbilityNamesies.LIBERO, "Changes the Pokémon's type to the type of the move it's about to use.");
        }

        @Override
        public void beforeAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // Protean activates for all moves except for Struggle
            if (attacking.getAttack().namesies() != AttackNamesies.STRUGGLE) {
                type = attacking.getAttackType();
                Effect.cast(PokemonEffectNamesies.CHANGE_TYPE, b, attacking, attacking, CastSource.ABILITY, true);
            }
        }

        @Override
        public PokeType getType(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return new PokeType(type);
        }
    }

    static class Mimicry extends Ability implements ChangeTypeEffect {
        private static final long serialVersionUID = 1L;

        Mimicry() {
            super(AbilityNamesies.MIMICRY, "Changes the Pokémon's type depending on the terrain.");
        }

        @Override
        public PokeType getType(Battle b, ActivePokemon p, boolean display) {
            // Type is the same as the current terrain
            // Note: In the game this effect will override effects like Soak, but that isn't happening here
            return b.getEffects().hasTerrain() ? new PokeType(b.getTerrainType().getType()) : null;
        }
    }

    static class MirrorArmor extends Ability implements StatTargetSwapperEffect {
        private static final long serialVersionUID = 1L;

        MirrorArmor() {
            super(AbilityNamesies.MIRROR_ARMOR, "Bounces back only the stat-lowering effects that the Pokémon receives.");
        }

        @Override
        public String getSwapStatTargetMessage(ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " reflected the changes!";
        }
    }

    static class NeutralizingGas extends Ability implements EntryEffect {
        private static final long serialVersionUID = 1L;

        NeutralizingGas() {
            super(AbilityNamesies.NEUTRALIZING_GAS, "If the Pokémon with Neutralizing Gas is in the battle, the effects of all Pokémon's Abilities will be nullified or will not be triggered.");
        }

        @Override
        public boolean isNeutralizable() {
            return false;
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            Messages.add(enterer.getName() + "'s " + this.getName() + " filled the area!");
        }
    }

    static class PerishBody extends Ability implements PhysicalContactEffect {
        private static final long serialVersionUID = 1L;

        PerishBody() {
            super(AbilityNamesies.PERISH_BODY, "When hit by a move that makes direct contact, the Pokémon and the attacker will faint after three turns unless they switch out of battle.");
        }

        @Override
        public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
            Messages.add(victim.getName() + "'s " + this.getName() + " will cause both " + PokeString.POKEMON + " to faint in three turns!");
            Effect.apply(PokemonEffectNamesies.PERISH_SONG, b, victim, user, CastSource.ABILITY, false);
            Effect.apply(PokemonEffectNamesies.PERISH_SONG, b, victim, victim, CastSource.ABILITY, false);
        }
    }

    static class PowerSpot extends Ability implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        PowerSpot() {
            super(AbilityNamesies.POWER_SPOT, "Powers up moves.");
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Note: This was changed because is supposed to power up ally moves, so is probably too strong it's like Sheer Force with no side effects oh well whatever who cares...
            return 1.3;
        }
    }

    static class PunkRock extends Ability implements OpponentPowerChangeEffect, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        PunkRock() {
            super(AbilityNamesies.PUNK_ROCK, "Boosts the power of sound-based moves. The Pokémon also takes half the damage from these kinds of moves.");
        }

        @Override
        public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getAttack().isMoveType(MoveType.SOUND_BASED) ? .5 : 1;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getAttack().isMoveType(MoveType.SOUND_BASED) ? 1.3 : 1;
        }
    }
}
