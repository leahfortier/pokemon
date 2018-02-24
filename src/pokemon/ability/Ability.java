package pokemon.ability;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.attack.MoveCategory;
import battle.attack.MoveType;
import battle.effect.CastSource;
import battle.effect.InvokeEffect;
import battle.effect.attack.AbilityChanger;
import battle.effect.attack.ChangeTypeSource;
import battle.effect.generic.EffectInterfaces.AbsorbDamageEffect;
import battle.effect.generic.EffectInterfaces.AlwaysCritEffect;
import battle.effect.generic.EffectInterfaces.ApplyDamageEffect;
import battle.effect.generic.EffectInterfaces.AttackBlocker;
import battle.effect.generic.EffectInterfaces.AttackingNoAdvantageChanger;
import battle.effect.generic.EffectInterfaces.BeforeTurnEffect;
import battle.effect.generic.EffectInterfaces.BracingEffect;
import battle.effect.generic.EffectInterfaces.ChangeAttackTypeEffect;
import battle.effect.generic.EffectInterfaces.ChangeTypeEffect;
import battle.effect.generic.EffectInterfaces.CrashDamageMove;
import battle.effect.generic.EffectInterfaces.CritBlockerEffect;
import battle.effect.generic.EffectInterfaces.CritStageEffect;
import battle.effect.generic.EffectInterfaces.DefiniteEscape;
import battle.effect.generic.EffectInterfaces.DifferentStatEffect;
import battle.effect.generic.EffectInterfaces.EffectBlockerEffect;
import battle.effect.generic.EffectInterfaces.EffectChanceMultiplierEffect;
import battle.effect.generic.EffectInterfaces.EncounterRateMultiplier;
import battle.effect.generic.EffectInterfaces.EndBattleEffect;
import battle.effect.generic.EffectInterfaces.EndTurnEffect;
import battle.effect.generic.EffectInterfaces.EntryEffect;
import battle.effect.generic.EffectInterfaces.EntryEndTurnEffect;
import battle.effect.generic.EffectInterfaces.HalfWeightEffect;
import battle.effect.generic.EffectInterfaces.ItemBlockerEffect;
import battle.effect.generic.EffectInterfaces.ItemSwapperEffect;
import battle.effect.generic.EffectInterfaces.LevitationEffect;
import battle.effect.generic.EffectInterfaces.MaxLevelWildEncounterEffect;
import battle.effect.generic.EffectInterfaces.ModifyStageValueEffect;
import battle.effect.generic.EffectInterfaces.MurderEffect;
import battle.effect.generic.EffectInterfaces.NameChanger;
import battle.effect.generic.EffectInterfaces.OpponentAccuracyBypassEffect;
import battle.effect.generic.EffectInterfaces.OpponentEndAttackEffect;
import battle.effect.generic.EffectInterfaces.OpponentIgnoreStageEffect;
import battle.effect.generic.EffectInterfaces.OpponentItemBlockerEffect;
import battle.effect.generic.EffectInterfaces.OpponentPowerChangeEffect;
import battle.effect.generic.EffectInterfaces.OpponentStatusReceivedEffect;
import battle.effect.generic.EffectInterfaces.OpponentTakeDamageEffect;
import battle.effect.generic.EffectInterfaces.OpponentTrappingEffect;
import battle.effect.generic.EffectInterfaces.PhysicalContactEffect;
import battle.effect.generic.EffectInterfaces.PowderMove;
import battle.effect.generic.EffectInterfaces.PowerChangeEffect;
import battle.effect.generic.EffectInterfaces.PriorityChangeEffect;
import battle.effect.generic.EffectInterfaces.RecoilMove;
import battle.effect.generic.EffectInterfaces.RepelLowLevelEncounterEffect;
import battle.effect.generic.EffectInterfaces.SelfAttackBlocker;
import battle.effect.generic.EffectInterfaces.SemiInvulnerableBypasser;
import battle.effect.generic.EffectInterfaces.SimpleStatModifyingEffect;
import battle.effect.generic.EffectInterfaces.SleepyFightsterEffect;
import battle.effect.generic.EffectInterfaces.StageChangingEffect;
import battle.effect.generic.EffectInterfaces.StallingEffect;
import battle.effect.generic.EffectInterfaces.StatLoweredEffect;
import battle.effect.generic.EffectInterfaces.StatModifyingEffect;
import battle.effect.generic.EffectInterfaces.StatProtectingEffect;
import battle.effect.generic.EffectInterfaces.StatusPreventionEffect;
import battle.effect.generic.EffectInterfaces.StatusReceivedEffect;
import battle.effect.generic.EffectInterfaces.SuperDuperEndTurnEffect;
import battle.effect.generic.EffectInterfaces.SwapOpponentEffect;
import battle.effect.generic.EffectInterfaces.SwitchOutEffect;
import battle.effect.generic.EffectInterfaces.TakeDamageEffect;
import battle.effect.generic.EffectInterfaces.TargetSwapperEffect;
import battle.effect.generic.EffectInterfaces.TypedWildEncounterSelector;
import battle.effect.generic.EffectInterfaces.WeatherBlockerEffect;
import battle.effect.generic.EffectInterfaces.WeatherEliminatingEffect;
import battle.effect.generic.EffectInterfaces.WildEncounterAlterer;
import battle.effect.generic.EffectNamesies;
import battle.effect.generic.PokemonEffect;
import battle.effect.generic.Weather;
import battle.effect.holder.AbilityHolder;
import battle.effect.holder.ItemHolder;
import battle.effect.status.Status;
import battle.effect.status.StatusCondition;
import item.Item;
import item.ItemNamesies;
import item.berry.Berry;
import item.hold.HoldItem;
import item.hold.SpecialTypeItem.MemoryItem;
import item.hold.SpecialTypeItem.PlateItem;
import main.Game;
import main.Global;
import map.overworld.WildEncounter;
import map.overworld.WildEncounterInfo;
import map.weather.WeatherState;
import message.MessageUpdate;
import message.Messages;
import pokemon.Gender;
import pokemon.PartyPokemon;
import pokemon.PokemonInfo;
import pokemon.PokemonNamesies;
import pokemon.Stat;
import trainer.Trainer;
import type.Type;
import type.TypeAdvantage;
import util.RandomUtils;
import util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public abstract class Ability implements AbilityHolder, InvokeEffect, Serializable {
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
    public InvokeSource getSource() {
        return InvokeSource.ABILITY;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public AbilityNamesies namesies() {
        return this.namesies;
    }

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

    // Called when this ability is going to changed to a different ability -- can be overridden as necessary
    public void deactivate(Battle b, ActivePokemon victim) {}

    protected ActivePokemon getOtherPokemon(Battle b, ActivePokemon p) {
        ActivePokemon other = b.getOtherPokemon(p);
        if (other.getAbility() != this) {
            Global.error(this.getName() + " invokee is not the opposite Pokemon.");
        }

        return other;
    }

    public static AbilityNamesies assign(PokemonInfo p) {
        AbilityNamesies[] abilities = p.getAbilities();

        if (abilities[0] == AbilityNamesies.NO_ABILITY) {
            Global.error("First ability should not be none (Pokemon " + p.getName() + ")");
        }

        // Only has one ability -- return the first one
        if (abilities[1] == AbilityNamesies.NO_ABILITY) {
            return abilities[0];
        }

        // Has two abilities -- return a random one
        return RandomUtils.getRandomValue(abilities);
    }

    public static AbilityNamesies evolutionAssign(PartyPokemon p, PokemonInfo ev) {
        AbilityNamesies prev = p.getActualAbility().namesies();

        // Evolution has current ability
        if (ev.hasAbility(prev)) {
            return prev;
        }

        // Evolution only has a single ability
        AbilityNamesies[] abilities = ev.getAbilities();
        if (abilities[1] == AbilityNamesies.NO_ABILITY) {
            return abilities[0];
        }

        // Evolution has the alternative
        AbilityNamesies other = getOtherAbility(p.getPokemonInfo(), prev);
        if (ev.hasAbility(other)) {
            return getOtherAbility(ev, other);
        }

        return RandomUtils.getRandomValue(abilities);
    }

    public static AbilityNamesies getOtherAbility(ActivePokemon p) {
        return getOtherAbility(p.getPokemonInfo(), p.getAbility().namesies());
    }

    private static AbilityNamesies getOtherAbility(PokemonInfo p, AbilityNamesies ability) {
        if (!p.hasAbility(ability)) {
            Global.error("Incorrect ability " + ability + " for " + p.getName() + ".");
        }

        AbilityNamesies[] abilities = p.getAbilities();
        return abilities[0] == ability ? abilities[1] : abilities[0];
    }

    // EVERYTHING BELOW IS GENERATED ###

    /**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

    static class NoAbility extends Ability {
        private static final long serialVersionUID = 1L;

        NoAbility() {
            super(AbilityNamesies.NO_ABILITY, "None");
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
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return b.getWeather().namesies() == EffectNamesies.SUNNY;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
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
            if (b.getWeather().namesies() == EffectNamesies.SUNNY) {
                Messages.add(victim.getName() + " lost some of its HP due to its " + this.getName() + "!");
                victim.reduceHealthFraction(b, 1/8.0);
            }
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getAttack().getCategory() == MoveCategory.SPECIAL && b.getWeather().namesies() == EffectNamesies.SUNNY ? 1.5 : 1;
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
            if (b.getWeather().namesies() == EffectNamesies.RAINING) {
                victim.healHealthFraction(1/16.0);
                Messages.add(new MessageUpdate(victim.getName() + "'s HP was restored due to its " + this.getName() + "!").updatePokemon(b, victim));
            }
        }
    }

    static class ShieldDust extends Ability implements EffectBlockerEffect {
        private static final long serialVersionUID = 1L;

        ShieldDust() {
            super(AbilityNamesies.SHIELD_DUST, "This Pok\u00e9mon's dust blocks the additional effects of attacks taken.");
        }

        @Override
        public boolean validMove(Battle b, ActivePokemon user, ActivePokemon victim) {
            return !user.getAttack().hasSecondaryEffects();
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
                Status.removeStatus(b, victim, CastSource.ABILITY);
            }
        }
    }

    static class Compoundeyes extends Ability implements SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        Compoundeyes() {
            super(AbilityNamesies.COMPOUNDEYES, "The Pok\u00e9mon's compound eyes boost its accuracy.");
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

    static class TintedLens extends Ability implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        TintedLens() {
            super(AbilityNamesies.TINTED_LENS, "The Pok\u00e9mon can use \"not very effective\" moves to deal regular damage.");
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

    static class KeenEye extends Ability implements StatProtectingEffect, OpponentIgnoreStageEffect, RepelLowLevelEncounterEffect {
        private static final long serialVersionUID = 1L;

        KeenEye() {
            super(AbilityNamesies.KEEN_EYE, "Keen eyes prevent other Pok\u00e9mon from lowering this Pok\u00e9mon's accuracy.");
        }

        @Override
        public boolean ignoreStage(Stat s) {
            return s == Stat.EVASION;
        }

        @Override
        public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
            return stat == Stat.ACCURACY;
        }
    }

    static class TangledFeet extends Ability implements StageChangingEffect {
        private static final long serialVersionUID = 1L;

        TangledFeet() {
            super(AbilityNamesies.TANGLED_FEET, "Raises evasion if the Pok\u00e9mon is confused.");
        }

        @Override
        public int adjustStage(Battle b, ActivePokemon p, ActivePokemon opp, Stat s) {
            return s == Stat.EVASION && p.hasEffect(EffectNamesies.CONFUSION) ? 1 : 0;
        }
    }

    static class Guts extends Ability implements SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        Guts() {
            super(AbilityNamesies.GUTS, "It's so gutsy that having a status condition boosts the Pok\u00e9mon's Attack stat.");
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return p.hasStatus();
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
            other.getStages().modifyStage(enterer, -1, Stat.ATTACK, b, CastSource.ABILITY);
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
                Status.applyStatus(b, victim, user, StatusCondition.PARALYZED, CastSource.ABILITY);
            }
        }
    }

    static class Lightningrod extends Ability implements AttackBlocker {
        private static final long serialVersionUID = 1L;

        Lightningrod() {
            super(AbilityNamesies.LIGHTNINGROD, "The Pok\u00e9mon draws in all Electric-type moves. Instead of being hit by Electric-type moves, it boosts its Sp. Atk.");
        }

        @Override
        public void alternateEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            victim.getStages().modifyStage(victim, 1, Stat.SP_ATTACK, b, CastSource.ABILITY);
        }

        @Override
        public boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.ELECTRIC);
        }

        @Override
        public String getBlockMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " makes it immune to " + Type.ELECTRIC.getName() + "-type moves!";
        }
    }

    static class SandVeil extends Ability implements StageChangingEffect, EncounterRateMultiplier {
        private static final long serialVersionUID = 1L;

        SandVeil() {
            super(AbilityNamesies.SAND_VEIL, "Boosts the Pok\u00e9mon's evasion in a sandstorm.");
        }

        @Override
        public int adjustStage(Battle b, ActivePokemon p, ActivePokemon opp, Stat s) {
            return s == Stat.EVASION && b.getWeather().namesies() == EffectNamesies.SANDSTORM ? 1 : 0;
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
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return b.getWeather().namesies() == EffectNamesies.SANDSTORM;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
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
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return b.getWeather().namesies() == EffectNamesies.HAILING;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
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
                Status.applyStatus(b, victim, user, StatusCondition.POISONED, CastSource.ABILITY);
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
            if (RandomUtils.chanceTest(30) && EffectNamesies.INFATUATED.getEffect().apply(b, victim, user, CastSource.ABILITY, false)) {
                Messages.add(victim.getName() + "'s " + this.getName() + " infatuated " + user.getName() + "!");
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
        public boolean block(EffectNamesies weather) {
            return true;
        }
    }

    static class FlashFire extends Ability implements PowerChangeEffect, AttackBlocker {
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
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return activated && user.isAttackType(Type.FIRE) ? 1.5 : 1;
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
        public String getBlockMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " makes it immune to " + Type.FIRE.getName() + "-type moves!";
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
            b.addEffect((Weather)EffectNamesies.SUNNY.getEffect());
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
            if (other.isHoldingItem(b)) {
                Messages.add(enterer.getName() + "'s " + this.getName() + " alerted it to " + other.getName() + "'s " + other.getHeldItem(b).getName() + "!");
            }
        }
    }

    static class InnerFocus extends Ability {
        private static final long serialVersionUID = 1L;

        InnerFocus() {
            super(AbilityNamesies.INNER_FOCUS, "The Pok\u00e9mon's intensely focused, and that protects the Pok\u00e9mon from flinching.");
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
                if (EffectNamesies.FLINCH.getEffect().apply(b, user, victim, CastSource.ABILITY, false)) {
                    Messages.add(user.getName() + "'s " + this.getName() + " caused " + victim.getName() + " to flinch!");
                }
            }
        }

        @Override
        public double getEncounterRateMultiplier() {
            return .5;
        }
    }

    static class EffectSpore extends Ability implements PhysicalContactEffect {
        private static final long serialVersionUID = 1L;

        private static final StatusCondition[] STATUSES = new StatusCondition[] {
                StatusCondition.PARALYZED,
                StatusCondition.POISONED,
                StatusCondition.ASLEEP
        };

        EffectSpore() {
            super(AbilityNamesies.EFFECT_SPORE, "Contact with the Pok\u00e9mon may inflict poison, sleep, or paralysis on its attacker.");
        }

        @Override
        public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Grass-type Pokemon, Pokemon with Overcoat, and Pokemon holding the Safety Goggles are immune to Effect Spore
            if (user.isType(b, Type.GRASS) || user.hasAbility(AbilityNamesies.OVERCOAT) || user.isHoldingItem(b, ItemNamesies.SAFETY_GOGGLES)) {
                return;
            }

            // 30% chance to Paralyze, Poison, or induce Sleep
            if (RandomUtils.chanceTest(30)) {
                Status.applyStatus(b, victim, user, RandomUtils.getRandomValue(STATUSES), CastSource.ABILITY);
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
            if (b.getWeather().namesies() == EffectNamesies.SUNNY) {
                Messages.add(victim.getName() + " lost some of its HP due to its " + this.getName() + "!");
                victim.reduceHealthFraction(b, 1/8.0);
            } else if (b.getWeather().namesies() == EffectNamesies.RAINING && !victim.fullHealth()) {
                victim.healHealthFraction(1/8.0);
                Messages.add(new MessageUpdate(victim.getName() + "'s HP was restored due to its " + this.getName() + "!").updatePokemon(b, victim));
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
        public void alternateEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Technically, according to the description, Heal Block prevents the prevention entirely (meaning this should be in Block), but that makes no sense, they shouldn't take damage, this way makes more sense
            if (victim.fullHealth() || victim.hasEffect(EffectNamesies.HEAL_BLOCK)) {
                return;
            }

            victim.healHealthFraction(1/4.0);
            Messages.add(new MessageUpdate(victim.getName() + "'s HP was restored instead!").updatePokemon(b, victim));
        }

        @Override
        public String getBlockMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " makes it immune to " + Type.WATER.getName() + " moves!";
        }
    }

    static class ArenaTrap extends Ability implements OpponentTrappingEffect, EncounterRateMultiplier {
        private static final long serialVersionUID = 1L;

        ArenaTrap() {
            super(AbilityNamesies.ARENA_TRAP, "Prevents opposing Pok\u00e9mon from fleeing.");
        }

        @Override
        public boolean trapOpponent(Battle b, ActivePokemon escaper, ActivePokemon trapper) {
            return !escaper.isLevitating(b) && !escaper.isType(b, Type.GHOST);
        }

        @Override
        public String opponentTrappingMessage(ActivePokemon escaper, ActivePokemon trapper) {
            return trapper.getName() + "'s " + this.getName() + " prevents " + escaper.getName() + " from escaping!";
        }

        @Override
        public double getEncounterRateMultiplier() {
            return 2;
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

    static class Limber extends Ability implements StatusPreventionEffect, EntryEndTurnEffect {
        private static final long serialVersionUID = 1L;

        private void removeStatus(Battle b, ActivePokemon victim) {
            if (victim.hasStatus(StatusCondition.PARALYZED)) {
                Status.removeStatus(b, victim, CastSource.ABILITY);
            }
        }

        Limber() {
            super(AbilityNamesies.LIMBER, "Its limber body protects the Pok\u00e9mon from paralysis.");
        }

        @Override
        public void applyEffect(Battle b, ActivePokemon p) {
            removeStatus(b, p);
        }

        @Override
        public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
            return status == StatusCondition.PARALYZED;
        }

        @Override
        public String statusPreventionMessage(ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " prevents paralysis!";
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
        public String getBlockMessage(Battle b, ActivePokemon user) {
            return blockityMessage(user, user);
        }

        @Override
        public boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            return checkeroo(user);
        }

        @Override
        public String getBlockMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
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

    static class VitalSpirit extends Ability implements MaxLevelWildEncounterEffect, StatusPreventionEffect, EntryEndTurnEffect {
        private static final long serialVersionUID = 1L;

        private void removeStatus(Battle b, ActivePokemon victim) {
            if (victim.hasStatus(StatusCondition.ASLEEP)) {
                Status.removeStatus(b, victim, CastSource.ABILITY);
            }
        }

        VitalSpirit() {
            super(AbilityNamesies.VITAL_SPIRIT, "The Pok\u00e9mon is full of vitality, and that prevents it from falling asleep.");
        }

        @Override
        public void applyEffect(Battle b, ActivePokemon p) {
            removeStatus(b, p);
        }

        @Override
        public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
            return status == StatusCondition.ASLEEP;
        }

        @Override
        public String statusPreventionMessage(ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " prevents sleep!";
        }
    }

    static class Insomnia extends Ability implements StatusPreventionEffect, EntryEndTurnEffect {
        private static final long serialVersionUID = 1L;

        private void removeStatus(Battle b, ActivePokemon victim) {
            if (victim.hasStatus(StatusCondition.ASLEEP)) {
                Status.removeStatus(b, victim, CastSource.ABILITY);
            }
        }

        Insomnia() {
            super(AbilityNamesies.INSOMNIA, "The Pok\u00e9mon is suffering from insomnia and cannot fall asleep.");
        }

        @Override
        public void applyEffect(Battle b, ActivePokemon p) {
            removeStatus(b, p);
        }

        @Override
        public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
            return status == StatusCondition.ASLEEP;
        }

        @Override
        public String statusPreventionMessage(ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " prevents sleep!";
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

        private static final Set<StatusCondition> PASSABLE_STATUSES = EnumSet.of(
                StatusCondition.BURNED,
                StatusCondition.PARALYZED,
                StatusCondition.POISONED,
                StatusCondition.BADLY_POISONED
        );

        Synchronize() {
            super(AbilityNamesies.SYNCHRONIZE, "The attacker will receive the same status condition if it inflicts a burn, poison, or paralysis to the Pok\u00e9mon.");
        }

        @Override
        public void receiveStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition statusType) {
            // Only applies when the opponent gives the victim the condition
            if (caster == victim) {
                return;
            }

            // Synchronize doesn't apply to every condition
            if (PASSABLE_STATUSES.contains(statusType)) {
                return;
            }

            // Give status condition to the opponent
            Status.applyStatus(b, victim, caster, statusType, CastSource.ABILITY);
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

    static class OwnTempo extends Ability {
        private static final long serialVersionUID = 1L;

        OwnTempo() {
            super(AbilityNamesies.OWN_TEMPO, "This Pok\u00e9mon has its own tempo, and that prevents it from becoming confused.");
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
    }

    static class FullMetalBody extends Ability implements StatProtectingEffect {
        private static final long serialVersionUID = 1L;

        FullMetalBody() {
            super(AbilityNamesies.FULL_METAL_BODY, "Prevents other Pok\u00e9mon's moves or Abilities from lowering the Pok\u00e9mon's stats.");
        }

        @Override
        public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
            return true;
        }

        @Override
        public boolean unbreakableMold() {
            // Ability is not ignored even when the opponent breaks the mold
            return true;
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
            return user.getAttack().isMoveType(MoveType.ONE_HIT_KO);
        }

        @Override
        public String getBlockMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " prevents OHKO moves!";
        }
    }

    static class Oblivious extends Ability implements AttackBlocker {
        private static final long serialVersionUID = 1L;

        Oblivious() {
            super(AbilityNamesies.OBLIVIOUS, "The Pok\u00e9mon is oblivious, and that keeps it from being infatuated or falling for taunts.");
        }

        @Override
        public boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getAttack().namesies() == AttackNamesies.CAPTIVATE;
        }

        @Override
        public String getBlockMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + "'s " + victim.getAbility().getName() + " prevents it from being captivated!";
        }
    }

    static class MagnetPull extends Ability implements OpponentTrappingEffect, TypedWildEncounterSelector {
        private static final long serialVersionUID = 1L;

        MagnetPull() {
            super(AbilityNamesies.MAGNET_PULL, "Prevents Steel-type Pok\u00e9mon from escaping using its magnetic force.");
        }

        @Override
        public boolean trapOpponent(Battle b, ActivePokemon escaper, ActivePokemon trapper) {
            return escaper.isType(b, Type.STEEL) && !escaper.isType(b, Type.GHOST);
        }

        @Override
        public String opponentTrappingMessage(ActivePokemon escaper, ActivePokemon trapper) {
            return trapper.getName() + "'s " + this.getName() + " prevents " + escaper.getName() + " from escaping!";
        }

        @Override
        public Type getEncounterType() {
            return Type.STEEL;
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
        public int modifyStageValue(int modVal) {
            return modVal*2;
        }
    }

    static class EarlyBird extends Ability {
        private static final long serialVersionUID = 1L;

        EarlyBird() {
            super(AbilityNamesies.EARLY_BIRD, "The Pok\u00e9mon awakens twice as fast as other Pok\u00e9mon from sleep.");
        }
    }

    static class ThickFat extends Ability implements OpponentPowerChangeEffect {
        private static final long serialVersionUID = 1L;

        ThickFat() {
            super(AbilityNamesies.THICK_FAT, "The Pok\u00e9mon is protected by a layer of thick fat, which halves the damage taken from Fire- and Ice-type moves.");
        }

        @Override
        public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.FIRE) || user.isAttackType(Type.ICE) ? .5 : 1;
        }
    }

    static class Hydration extends Ability implements EndTurnEffect {
        private static final long serialVersionUID = 1L;

        Hydration() {
            super(AbilityNamesies.HYDRATION, "Heals status conditions if it's raining.");
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (victim.hasStatus() && b.getWeather().namesies() == EffectNamesies.RAINING) {
                Status.removeStatus(b, victim, CastSource.ABILITY);
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
            List<Move> otherMoves = other.getMoves(b);

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
                warn = RandomUtils.getRandomValue(otherMoves).getAttack().namesies();
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
    }

    static class Soundproof extends Ability implements AttackBlocker {
        private static final long serialVersionUID = 1L;

        Soundproof() {
            super(AbilityNamesies.SOUNDPROOF, "Soundproofing of the Pok\u00e9mon itself gives full immunity to all sound-based moves.");
        }

        @Override
        public boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getAttack().isMoveType(MoveType.SOUND_BASED);
        }

        @Override
        public String getBlockMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
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
        public void enter(Battle b, ActivePokemon enterer) {
            if (enterer.namesies() == PokemonNamesies.PANGORO) {
                Messages.add(enterer.getName() + " does not break the mold!!!!!!!");
            }
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getAttack().isMoveType(MoveType.PUNCHING) ? 1.2 : 1;
        }
    }

    static class NaturalCure extends Ability implements SwitchOutEffect {
        private static final long serialVersionUID = 1L;

        NaturalCure() {
            super(AbilityNamesies.NATURAL_CURE, "All status conditions heal when the Pok\u00e9mon switches out.");
        }

        @Override
        public void switchOut(ActivePokemon switchee) {
            if (!switchee.hasStatus(StatusCondition.FAINTED)) {
                switchee.removeStatus();
            }
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
        public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
            return b.getWeather().namesies() == EffectNamesies.SUNNY;
        }

        @Override
        public String statusPreventionMessage(ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " prevents status conditions!";
        }
    }

    static class Scrappy extends Ability implements AttackingNoAdvantageChanger {
        private static final long serialVersionUID = 1L;

        Scrappy() {
            super(AbilityNamesies.SCRAPPY, "The Pok\u00e9mon can hit Ghost-type Pok\u00e9mon with Normal- and Fighting-type moves.");
        }

        @Override
        public boolean negateNoAdvantage(Type attacking, Type defending) {
            return defending == Type.GHOST && (attacking == Type.NORMAL || attacking == Type.FIGHTING);
        }
    }

    static class SwiftSwim extends Ability implements SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        SwiftSwim() {
            super(AbilityNamesies.SWIFT_SWIM, "Boosts the Pok\u00e9mon's Speed stat in rain.");
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return b.getWeather().namesies() == EffectNamesies.RAINING;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
        }

        @Override
        public double getModifier() {
            return 2;
        }
    }

    static class WaterVeil extends Ability implements StatusPreventionEffect, EntryEndTurnEffect {
        private static final long serialVersionUID = 1L;

        private void removeStatus(Battle b, ActivePokemon victim) {
            if (victim.hasStatus(StatusCondition.BURNED)) {
                Status.removeStatus(b, victim, CastSource.ABILITY);
            }
        }

        WaterVeil() {
            super(AbilityNamesies.WATER_VEIL, "The Pok\u00e9mon is covered with a water veil, which prevents the Pok\u00e9mon from getting a burn.");
        }

        @Override
        public void applyEffect(Battle b, ActivePokemon p) {
            removeStatus(b, p);
        }

        @Override
        public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
            return status == StatusCondition.BURNED;
        }

        @Override
        public String statusPreventionMessage(ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " prevents burns!";
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
                Status.applyStatus(b, victim, user, StatusCondition.BURNED, CastSource.ABILITY);
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
                victim.getStages().modifyStage(victim, 1, Stat.SPEED, b, CastSource.ABILITY);
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
            murderer.getStages().modifyStage(murderer, 1, Stat.ATTACK, b, CastSource.ABILITY);
        }
    }

    static class BeastBoost extends Ability implements MurderEffect {
        private static final long serialVersionUID = 1L;

        BeastBoost() {
            super(AbilityNamesies.BEAST_BOOST, "The Pok\u00e9mon boosts its most proficient stat each time it knocks out a Pok\u00e9mon.");
        }

        @Override
        public void killWish(Battle b, ActivePokemon dead, ActivePokemon murderer) {
            murderer.getStages().modifyStage(murderer, 1, murderer.getBestBattleStat(), b, CastSource.ABILITY);
        }
    }

    static class SoulHeart extends Ability implements OpponentStatusReceivedEffect {
        private static final long serialVersionUID = 1L;

        SoulHeart() {
            super(AbilityNamesies.SOUL_HEART, "Boosts its Sp. Atk stat every time a Pok\u00e9mon faints.");
        }

        @Override
        public void receiveStatus(Battle b, ActivePokemon victim, StatusCondition statusType) {
            if (statusType == StatusCondition.FAINTED) {
                ActivePokemon abilify = this.getOtherPokemon(b, victim);
                abilify.getStages().modifyStage(abilify, 1, Stat.SP_ATTACK, b, CastSource.ABILITY);
            }
        }
    }

    static class Imposter extends Ability implements EntryEffect {
        private static final long serialVersionUID = 1L;

        Imposter() {
            super(AbilityNamesies.IMPOSTER, "The Pok\u00e9mon transforms itself into the Pok\u00e9mon it's facing.");
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            EffectNamesies.TRANSFORMED.getEffect().cast(b, enterer, enterer, CastSource.ABILITY, false);
        }

        @Override
        public boolean isStealable() {
            return false;
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
        public void alternateEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Technically, according to the description, Heal Block prevents the prevention entirely (meaning this should be in Block), but that makes no sense, they shouldn't take damage, this way makes more sense
            if (victim.fullHealth() || victim.hasEffect(EffectNamesies.HEAL_BLOCK)) {
                return;
            }

            victim.healHealthFraction(1/4.0);
            Messages.add(new MessageUpdate(victim.getName() + "'s HP was restored instead!").updatePokemon(b, victim));
        }

        @Override
        public String getBlockMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " makes it immune to " + Type.WATER.getName() + " moves!";
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
        public void alternateEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Technically, according to the description, Heal Block prevents the prevention entirely (meaning this should be in Block), but that makes no sense, they shouldn't take damage, this way makes more sense
            if (victim.fullHealth() || victim.hasEffect(EffectNamesies.HEAL_BLOCK)) {
                return;
            }

            victim.healHealthFraction(1/4.0);
            Messages.add(new MessageUpdate(victim.getName() + "'s HP was restored instead!").updatePokemon(b, victim));
        }

        @Override
        public String getBlockMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " makes it immune to " + Type.ELECTRIC.getName() + " moves!";
        }
    }

    static class QuickFeet extends Ability implements EncounterRateMultiplier, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        QuickFeet() {
            super(AbilityNamesies.QUICK_FEET, "Boosts the Speed stat if the Pok\u00e9mon has a status condition.");
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return p.hasStatus();
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
        }

        @Override
        public double getEncounterRateMultiplier() {
            return .5;
        }

        @Override
        public double getModifier() {
            return 1.5;
        }
    }

    static class Trace extends Ability implements EntryEffect, AbilityChanger {
        private static final long serialVersionUID = 1L;

        Trace() {
            super(AbilityNamesies.TRACE, "When it enters a battle, the Pok\u00e9mon copies an opposing Pok\u00e9mon's Ability.");
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

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            ActivePokemon other = b.getOtherPokemon(enterer);
            if (!other.getAbility().isStealable() || other.hasAbility(this.namesies)) {
                return;
            }

            EffectNamesies.CHANGE_ABILITY.getEffect().cast(b, enterer, enterer, CastSource.ABILITY, true);
        }

        @Override
        public boolean isStealable() {
            return false;
        }
    }

    static class Download extends Ability implements EntryEffect {
        private static final long serialVersionUID = 1L;

        Download() {
            super(AbilityNamesies.DOWNLOAD, "Compares an opposing Pok\u00e9mon's Defense and Sp. Def stats before raising its own Attack or Sp. Atk stat -- whichever will be more effective.");
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            ActivePokemon other = b.getOtherPokemon(enterer);
            PokemonInfo otherInfo = other.getPokemonInfo();

            int baseDefense = otherInfo.getStat(Stat.DEFENSE.index());
            int baseSpecialDefense = otherInfo.getStat(Stat.SP_DEFENSE.index());

            Stat toRaise = baseDefense < baseSpecialDefense ? Stat.ATTACK : Stat.SP_ATTACK;

            enterer.getStages().modifyStage(enterer, 1, toRaise, b, CastSource.ABILITY);
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

    static class Immunity extends Ability implements StatusPreventionEffect, EntryEndTurnEffect {
        private static final long serialVersionUID = 1L;

        private void removeStatus(Battle b, ActivePokemon victim) {
            if (victim.hasStatus(StatusCondition.POISONED)) {
                Status.removeStatus(b, victim, CastSource.ABILITY);
            }
        }

        Immunity() {
            super(AbilityNamesies.IMMUNITY, "The immune system of the Pok\u00e9mon prevents it from getting poisoned.");
        }

        @Override
        public void applyEffect(Battle b, ActivePokemon p) {
            removeStatus(b, p);
        }

        @Override
        public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
            return status == StatusCondition.POISONED;
        }

        @Override
        public String statusPreventionMessage(ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " prevents poison!";
        }
    }

    static class SnowCloak extends Ability implements StageChangingEffect, EncounterRateMultiplier {
        private static final long serialVersionUID = 1L;

        SnowCloak() {
            super(AbilityNamesies.SNOW_CLOAK, "Boosts evasion in a hailstorm.");
        }

        @Override
        public int adjustStage(Battle b, ActivePokemon p, ActivePokemon opp, Stat s) {
            return s == Stat.EVASION && b.getWeather().namesies() == EffectNamesies.HAILING ? 1 : 0;
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
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return p.hasStatus();
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.DEFENSE;
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
            victim.getStages().modifyStage(victim, 1, Stat.SPEED, b, CastSource.ABILITY);
        }
    }

    static class MagicBounce extends Ability implements TargetSwapperEffect {
        private static final long serialVersionUID = 1L;

        MagicBounce() {
            super(AbilityNamesies.MAGIC_BOUNCE, "Reflects status moves, instead of getting hit by them.");
        }

        @Override
        public boolean swapTarget(Battle b, ActivePokemon user, ActivePokemon opponent) {
            Attack attack = user.getAttack();
            if (!attack.isSelfTarget() && attack.isStatusMove() && !attack.isMoveType(MoveType.NO_MAGIC_COAT)) {
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
            return !escaper.hasAbility(this.namesies) && !escaper.isType(b, Type.GHOST);
        }

        @Override
        public String opponentTrappingMessage(ActivePokemon escaper, ActivePokemon trapper) {
            return trapper.getName() + "'s " + this.getName() + " prevents " + escaper.getName() + " from escaping!";
        }
    }

    static class Overcoat extends Ability implements WeatherBlockerEffect, AttackBlocker {
        private static final long serialVersionUID = 1L;

        Overcoat() {
            super(AbilityNamesies.OVERCOAT, "Protects the Pok\u00e9mon from things like sand, hail, and powder.");
        }

        @Override
        public boolean block(EffectNamesies weather) {
            return true;
        }

        @Override
        public boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getAttack() instanceof PowderMove;
        }

        @Override
        public String getBlockMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " protects it from powder moves!";
        }
    }

    static class MagmaArmor extends Ability implements StatusPreventionEffect, EntryEndTurnEffect {
        private static final long serialVersionUID = 1L;

        private void removeStatus(Battle b, ActivePokemon victim) {
            if (victim.hasStatus(StatusCondition.FROZEN)) {
                Status.removeStatus(b, victim, CastSource.ABILITY);
            }
        }

        MagmaArmor() {
            super(AbilityNamesies.MAGMA_ARMOR, "The Pok\u00e9mon is covered with hot magma, which prevents the Pok\u00e9mon from becoming frozen.");
        }

        @Override
        public void applyEffect(Battle b, ActivePokemon p) {
            removeStatus(b, p);
        }

        @Override
        public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
            return status == StatusCondition.FROZEN;
        }

        @Override
        public String statusPreventionMessage(ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " prevents freezing!";
        }
    }

    static class SuctionCups extends Ability implements AttackBlocker {
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
        public String getBlockMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " prevents it from switching!";
        }
    }

    static class Steadfast extends Ability {
        private static final long serialVersionUID = 1L;

        Steadfast() {
            super(AbilityNamesies.STEADFAST, "The Pok\u00e9mon's determination boosts the Speed stat each time the Pok\u00e9mon flinches.");
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
            b.addEffect((Weather)EffectNamesies.SANDSTORM.getEffect());
        }
    }

    static class Regenerator extends Ability implements SwitchOutEffect {
        private static final long serialVersionUID = 1L;

        Regenerator() {
            super(AbilityNamesies.REGENERATOR, "Restores a little HP when withdrawn from battle.");
        }

        @Override
        public void switchOut(ActivePokemon switchee) {
            if (!switchee.hasStatus(StatusCondition.FAINTED)) {
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

    static class Truant extends Ability implements EndTurnEffect, BeforeTurnEffect {
        private static final long serialVersionUID = 1L;

        private boolean lazyface;

        Truant() {
            super(AbilityNamesies.TRUANT, "The Pok\u00e9mon can't use a move the following turn if it uses one.");
            this.lazyface = false;
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (victim.hasStatus(StatusCondition.ASLEEP)) {
                lazyface = false;
            } else {
                lazyface = !lazyface;
            }
        }

        @Override
        public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
            if (lazyface) {
                Messages.add(p.getName() + " is loafing around!");
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
        public boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Status moves, super-effective moves, and None-type moves always hit
            return !user.getAttack().isStatusMove() && !TypeAdvantage.isSuperEffective(user, victim, b) && !user.isAttackType(Type.NO_TYPE);
        }

        @Override
        public String getBlockMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " makes it immune to " + user.getAttack().getName() + "!";
        }

        @Override
        public boolean isReplaceable() {
            return false;
        }

        @Override
        public boolean isStealable() {
            return false;
        }
    }

    static class Normalize extends Ability implements ChangeAttackTypeEffect, EndTurnEffect, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        private boolean activated;

        Normalize() {
            super(AbilityNamesies.NORMALIZE, "All the Pok\u00e9mon's moves become Normal type. The power of those moves is boosted a little.");
            this.activated = false;
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            this.activated = false;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return activated ? 1.2 : 1;
        }

        @Override
        public Type changeAttackType(Attack attack, Type original) {
            if (original != Type.NORMAL) {
                this.activated = true;
            }

            return Type.NORMAL;
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
            Messages.add(user.getName() + " was hurt by " + victim.getName() + "'s " + this.getName() + "!");
            user.reduceHealthFraction(b, 1/8.0);
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
        public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
            return true;
        }

        @Override
        public double getEncounterRateMultiplier() {
            return .5;
        }
    }

    static class ToxicBoost extends Ability implements SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        ToxicBoost() {
            super(AbilityNamesies.TOXIC_BOOST, "Powers up physical attacks when the Pok\u00e9mon is poisoned.");
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return p.hasStatus(StatusCondition.POISONED);
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

    static class Anticipation extends Ability implements EntryEffect {
        private static final long serialVersionUID = 1L;

        Anticipation() {
            super(AbilityNamesies.ANTICIPATION, "The Pok\u00e9mon can sense an opposing Pok\u00e9mon's dangerous moves.");
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            ActivePokemon other = b.getOtherPokemon(enterer);
            for (Move m : other.getMoves(b)) {
                Attack attack = m.getAttack();
                if (attack.getActualType().getAdvantage().isSuperEffective(enterer, b) || attack.isMoveType(MoveType.ONE_HIT_KO)) {
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
            victim.getStages().modifyStage(victim, 1, Stat.SP_ATTACK, b, CastSource.ABILITY);
        }

        @Override
        public boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.WATER);
        }

        @Override
        public String getBlockMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
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
        public Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return new Type[] { type, Type.NO_TYPE };
        }

        @Override
        public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            Type t = user.getAttackType();
            if (!victim.isType(b, t)) {
                type = t;
                EffectNamesies.CHANGE_TYPE.getEffect().cast(b, victim, victim, CastSource.ABILITY, true);
            }
        }
    }

    static class IceBody extends Ability implements EndTurnEffect, WeatherBlockerEffect {
        private static final long serialVersionUID = 1L;

        IceBody() {
            super(AbilityNamesies.ICE_BODY, "The Pok\u00e9mon gradually regains HP in a hailstorm.");
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (b.getWeather().namesies() == EffectNamesies.HAILING) {
                victim.healHealthFraction(1/16.0);
                Messages.add(new MessageUpdate(victim.getName() + "'s HP was restored due to its " + this.getName() + "!").updatePokemon(b, victim));
            }
        }

        @Override
        public boolean block(EffectNamesies weather) {
            return weather == EffectNamesies.HAILING;
        }
    }

    static class LightMetal extends Ability implements HalfWeightEffect {
        private static final long serialVersionUID = 1L;

        LightMetal() {
            super(AbilityNamesies.LIGHT_METAL, "Halves the Pok\u00e9mon's weight.");
        }

        @Override
        public int getHalfAmount(int halfAmount) {
            return halfAmount + 1;
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
            b.addEffect((Weather)EffectNamesies.RAINING.getEffect());
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
        public void takeItToTheNextLevel(Battle b, ActivePokemon caster, ActivePokemon victim) {
            victim.getStages().modifyStage(victim, 2, Stat.ATTACK, b, CastSource.ABILITY);
        }
    }

    static class Competitive extends Ability implements StatLoweredEffect {
        private static final long serialVersionUID = 1L;

        Competitive() {
            super(AbilityNamesies.COMPETITIVE, "Boosts the Sp. Atk stat sharply when a stat is lowered.");
        }

        @Override
        public void takeItToTheNextLevel(Battle b, ActivePokemon caster, ActivePokemon victim) {
            victim.getStages().modifyStage(victim, 2, Stat.SP_ATTACK, b, CastSource.ABILITY);
        }
    }

    static class FlowerGift extends Ability implements SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        FlowerGift() {
            super(AbilityNamesies.FLOWER_GIFT, "Boosts the Attack and Sp. Def stats of itself and allies when it is sunny.");
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return b.getWeather().namesies() == EffectNamesies.SUNNY;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.ATTACK || s == Stat.SP_DEFENSE;
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
                Messages.add(user.getName() + " was hurt by " + victim.getName() + "'s " + this.getName() + "!");
                user.reduceHealthFraction(b, 1/4.0);
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
            Type type = user.getAttackType();
            return (type == Type.ROCK || type == Type.STEEL || type == Type.GROUND) && b.getWeather().namesies() == EffectNamesies.SANDSTORM ? 1.3 : 1;
        }

        @Override
        public boolean block(EffectNamesies weather) {
            return weather == EffectNamesies.SANDSTORM;
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
            b.addEffect((Weather)EffectNamesies.HAILING.getEffect());
        }
    }

    static class MotorDrive extends Ability implements AttackBlocker {
        private static final long serialVersionUID = 1L;

        MotorDrive() {
            super(AbilityNamesies.MOTOR_DRIVE, "Boosts its Speed stat if hit by an Electric-type move, instead of taking damage.");
        }

        @Override
        public void alternateEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            victim.getStages().modifyStage(victim, 1, Stat.SPEED, b, CastSource.ABILITY);
        }

        @Override
        public boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.ELECTRIC);
        }

        @Override
        public String getBlockMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
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
                victim.getStages().modifyStage(victim, 1, Stat.ATTACK, b, CastSource.ABILITY);
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
                if (EffectNamesies.DISABLE.getEffect().apply(b, victim, user, CastSource.ABILITY, false)) {
                    Messages.add(victim.getName() + "'s " + this.getName() + " disabled " + user.getName() + "'s " + user.getAttack().getName());
                }
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
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return count < 5;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.ATTACK || s == Stat.SPEED;
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            count = 0;
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
            if (other.hasStatus(StatusCondition.ASLEEP)) {
                Messages.add(other.getName() + " was hurt by " + victim.getName() + "'s " + this.getName() + "!");
                other.reduceHealthFraction(b, 1/8.0);
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
        public int modifyStageValue(int modVal) {
            return -1*modVal;
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
    }

    static class PoisonTouch extends Ability implements OpponentTakeDamageEffect {
        private static final long serialVersionUID = 1L;

        PoisonTouch() {
            super(AbilityNamesies.POISON_TOUCH, "May poison a target when the Pok\u00e9mon makes contact.");
        }

        @Override
        public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (RandomUtils.chanceTest(30)) {
                Status.applyStatus(b, user, victim, StatusCondition.POISONED, CastSource.ABILITY);
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
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return opp.getAttack().isStatusMove();
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.EVASION;
        }

        @Override
        public double getModifier() {
            return 1.5;
        }
    }

    static class Mummy extends Ability implements PhysicalContactEffect, AbilityChanger {
        private static final long serialVersionUID = 1L;

        Mummy() {
            super(AbilityNamesies.MUMMY, "Contact with the Pok\u00e9mon changes the attacker's Ability to Mummy.");
        }

        @Override
        public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return AbilityNamesies.MUMMY.getNewAbility();
        }

        @Override
        public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return victim.getName() + "'s ability was changed to " + this.namesies().getName() + "!";
        }

        @Override
        public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (user.hasAbility(this.namesies) || !user.getAbility().isReplaceable()) {
                return;
            }

            // Cast the change ability effect onto the user
            EffectNamesies.CHANGE_ABILITY.getEffect().cast(b, victim, user, CastSource.ABILITY, true);
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
                victim.getStages().modifyStage(victim, -1, Stat.DEFENSE, b, CastSource.ABILITY);
                victim.getStages().modifyStage(victim, 2, Stat.SPEED, b, CastSource.ABILITY);
            }
        }
    }

    static class Illusion extends Ability implements EntryEffect, SwitchOutEffect, TakeDamageEffect, ChangeTypeEffect, NameChanger {
        private static final long serialVersionUID = 1L;

        private boolean activated;
        private String illusionName;
        private Type[] illusionType;
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
        public Type[] getType(Battle b, ActivePokemon p, boolean display) {
            if (display && activated) {
                return illusionType;
            }

            return p.getActualType();
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
            for (int i = team.size() - 1; i > 0; i--) {
                ActivePokemon temp = team.get(i);

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
        public boolean isActive() {
            return activated;
        }

        @Override
        public void deactivate(Battle b, ActivePokemon victim) {
            breakIllusion(b, victim);
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
        public boolean isStealable() {
            return false;
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
            victim.getStages().modifyStage(victim, 1, Stat.ATTACK, b, CastSource.ABILITY);
        }

        @Override
        public boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.GRASS);
        }

        @Override
        public String getBlockMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
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
            Messages.add(user.getName() + " was hurt by " + victim.getName() + "'s " + this.getName() + "!");
            user.reduceHealthFraction(b, 1/8.0);
        }
    }

    static class MoldBreaker extends Ability implements EntryEffect {
        private static final long serialVersionUID = 1L;

        MoldBreaker() {
            super(AbilityNamesies.MOLD_BREAKER, "Moves can be used on the target regardless of its Abilities.");
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            Messages.add(enterer.getName() + " breaks the mold!");
        }
    }

    static class Teravolt extends Ability implements EntryEffect {
        private static final long serialVersionUID = 1L;

        Teravolt() {
            super(AbilityNamesies.TERAVOLT, "Moves can be used on the target regardless of its Abilities.");
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            Messages.add(enterer.getName() + " is radiating a bursting aura!");
        }
    }

    static class Turboblaze extends Ability implements EntryEffect {
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

    static class StickyHold extends Ability {
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
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return p.hasEffect(EffectNamesies.CONSUMED_ITEM);
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
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
        public String getSwitchMessage(ActivePokemon user, Item userItem, ActivePokemon victim, Item victimItem) {
            return user.getName() + " stole " + victim.getName() + "'s " + victimItem.getName() + "!";
        }

        @Override
        public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Steal from the Pokemon who made physical contact with you
            if (!victim.isFainted(b) && victim.canStealItem(b, user)) {
                this.swapItems(b, victim, user);
            }
        }
    }

    static class Harvest extends Ability implements EndTurnEffect {
        private static final long serialVersionUID = 1L;

        Harvest() {
            super(AbilityNamesies.HARVEST, "May create another Berry after one is used.");
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            PokemonEffect consumed = victim.getEffect(EffectNamesies.CONSUMED_ITEM);
            if (consumed == null || victim.isHoldingItem(b)) {
                return;
            }

            Item restored = ((ItemHolder)consumed).getItem();
            if (restored instanceof Berry && (b.getWeather().namesies() == EffectNamesies.SUNNY || RandomUtils.chanceTest(50))) {
                victim.giveItem((HoldItem)restored);
                Messages.add(victim.getName() + "'s " + this.getName() + " restored its " + restored.getName() + "!");
            }
        }
    }

    static class Pickup extends Ability implements EndBattleEffect {
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
            addItem(ItemNamesies.PPUP, 5);
            addItem(ItemNamesies.LEFTOVERS, 5);
            addItem(ItemNamesies.MAX_ELIXIR, 5);
            addItem(ItemNamesies.BIG_NUGGET, 1);
            addItem(ItemNamesies.BALM_MUSHROOM, 1);
            addItem(ItemNamesies.HPUP, 1);
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
        public void afterBattle(Trainer player, Battle b, ActivePokemon p) {
            if (!p.isHoldingItem(b) && RandomUtils.chanceTest(10)) {
                ItemNamesies item = RandomUtils.getRandomValue(items);
                p.giveItem(item);
                Messages.add(p.getName() + " picked up " + StringUtils.articleString(item.getName()) + "!");
            }
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
        public boolean blockItem(Battle b, ActivePokemon p, ItemNamesies item) {
            return item.getItem() instanceof Berry;
        }
    }

    static class HoneyGather extends Ability implements EndBattleEffect {
        private static final long serialVersionUID = 1L;

        HoneyGather() {
            super(AbilityNamesies.HONEY_GATHER, "The Pok\u00e9mon may gather Honey after a battle.");
        }

        @Override
        public void afterBattle(Trainer player, Battle b, ActivePokemon p) {
            if (!p.isHoldingItem(b) && RandomUtils.chanceTest(5*(int)Math.ceil(p.getLevel()/10.0))) {
                // TODO: Should give the item Honey, but this item has no purpose in our game so we'll see what this ability should actually do also something about Syrup Gather
                p.giveItem(ItemNamesies.LEFTOVERS);
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
        public boolean isReplaceable() {
            return false;
        }

        @Override
        public boolean isStealable() {
            return false;
        }

        @Override
        public Type[] getType(Battle b, ActivePokemon p, boolean display) {
            Item item = p.getHeldItem(b);
            if (item instanceof PlateItem) {
                return new Type[] { ((PlateItem)item).getType(), Type.NO_TYPE };
            }

            return p.getActualType();
        }
    }

    static class RKSSystem extends Ability implements ChangeTypeEffect {
        private static final long serialVersionUID = 1L;

        RKSSystem() {
            super(AbilityNamesies.RKSSYSTEM, "Changes the Pok\u00e9mon's type to match the memory disc it holds.");
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
        public Type[] getType(Battle b, ActivePokemon p, boolean display) {
            Item item = p.getHeldItem(b);
            if (item instanceof MemoryItem) {
                return new Type[] { ((MemoryItem)item).getType(), Type.NO_TYPE };
            }

            return p.getActualType();
        }
    }

    static class Forecast extends Ability implements ChangeTypeEffect {
        private static final long serialVersionUID = 1L;

        Forecast() {
            super(AbilityNamesies.FORECAST, "The Pok\u00e9mon transforms with the weather to change its type to Water, Fire, or Ice.");
        }

        @Override
        public Type[] getType(Battle b, ActivePokemon p, boolean display) {
            return new Type[] { b.getWeather().getElement(), Type.NO_TYPE };
        }

        @Override
        public boolean isStealable() {
            return false;
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
        public String getBlockMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " prevents bomb/ball moves!";
        }
    }

    static class AuraBreak extends Ability {
        private static final long serialVersionUID = 1L;

        AuraBreak() {
            super(AbilityNamesies.AURA_BREAK, "The effects of \"Aura\" Abilities are reversed to lower the power of affected moves.");
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
        public String getSwitchMessage(ActivePokemon user, Item userItem, ActivePokemon victim, Item victimItem) {
            return user.getName() + " stole " + victim.getName() + "'s " + victimItem.getName() + "!";
        }

        @Override
        public void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
            // Steal the victim's item when damage is dealt
            if (!user.isFainted(b) && user.canStealItem(b, victim)) {
                this.swapItems(b, user, victim);
            }
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
            return user.getAttack().isMoveType(MoveType.PHYSICAL_CONTACT) ? 1.33 : 1;
        }
    }

    static class SweetVeil extends Ability implements StatusPreventionEffect, EntryEndTurnEffect {
        private static final long serialVersionUID = 1L;

        private void removeStatus(Battle b, ActivePokemon victim) {
            if (victim.hasStatus(StatusCondition.ASLEEP)) {
                Status.removeStatus(b, victim, CastSource.ABILITY);
            }
        }

        SweetVeil() {
            super(AbilityNamesies.SWEET_VEIL, "Prevents itself and ally Pok\u00e9mon from falling asleep.");
        }

        @Override
        public void applyEffect(Battle b, ActivePokemon p) {
            removeStatus(b, p);
        }

        @Override
        public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
            return status == StatusCondition.ASLEEP;
        }

        @Override
        public String statusPreventionMessage(ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " prevents sleep!";
        }
    }

    static class AromaVeil extends Ability {
        private static final long serialVersionUID = 1L;

        AromaVeil() {
            super(AbilityNamesies.AROMA_VEIL, "Protects itself and its allies from attacks that limit their move choices.");
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
                Status.removeStatus(b, victim, CastSource.ABILITY);
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
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            this.activated = false;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return activated ? 1.2 : 1;
        }

        @Override
        public Type changeAttackType(Attack attack, Type original) {
            if (original == Type.NORMAL) {
                this.activated = true;
                return Type.FAIRY;
            }

            return original;
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
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            this.activated = false;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return activated ? 1.2 : 1;
        }

        @Override
        public Type changeAttackType(Attack attack, Type original) {
            if (original == Type.NORMAL) {
                this.activated = true;
                return Type.ICE;
            }

            return original;
        }
    }

    static class Schooling extends Ability implements EndTurnEffect, EntryEffect, DifferentStatEffect {
        private static final long serialVersionUID = 1L;

        private static final int[] SOLO_STATS = new int[] { 45, 20, 20, 25, 25, 40 };
        private static final int[] SCHOOL_STATS = new int[] { 45, 140, 130, 140, 135, 30 };

        private boolean schoolForm;

        private int[] getStats() {
            return schoolForm ? SCHOOL_STATS : SOLO_STATS;
        }

        Schooling() {
            super(AbilityNamesies.SCHOOLING, "When it has a lot of HP, the Pok\u00e9mon forms a powerful school. It stops schooling when its HP is low.");
            this.schoolForm = false;
        }

        private void changeForm(ActivePokemon formsie) {
            if (this.schoolForm != formsie.getHPRatio() >= .25 && formsie.getLevel() >= 20) {
                this.schoolForm = !schoolForm;
                Messages.add(new MessageUpdate(formsie.getName() + " changed into " + (schoolForm ? "School" : "Solo") + " Forme!")
                            .withImageName(formsie.getPokemonInfo().getImageName(formsie.isShiny(), !formsie.isPlayer(), schoolForm), formsie.isPlayer())
                );
            }
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            changeForm(victim);
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            schoolForm = false;
            changeForm(enterer);
        }

        @Override
        public Integer getStat(ActivePokemon user, Stat stat) {
            // Need to calculate the new stat -- yes, I realize this is super inefficient and whatever whatever whatever
            int index = stat.index();
            return Stat.getStat(index, user.getLevel(), getStats()[index], user.getIV(index), user.getEV(index), user.getNature().getNatureVal(index));
        }

        @Override
        public boolean isReplaceable() {
            return false;
        }

        @Override
        public boolean isStealable() {
            return false;
        }
    }

    static class ShieldsDown extends Ability implements EndTurnEffect, EntryEffect, DifferentStatEffect {
        private static final long serialVersionUID = 1L;

        private static final int[] METEOR_STATS = new int[] { 60, 60, 100, 60, 100, 60 };
        private static final int[] CORE_STATS = new int[] { 60, 100, 60, 100, 60, 120 };

        private boolean coreForm;

        private int[] getStats() {
            return coreForm ? CORE_STATS : METEOR_STATS;
        }

        ShieldsDown() {
            super(AbilityNamesies.SHIELDS_DOWN, "When its HP becomes half or less, the Pok\u00e9mon's shell breaks and it becomes aggressive.");
            this.coreForm = false;
        }

        private void changeForm(ActivePokemon formsie) {
            if (this.coreForm != formsie.getHPRatio() < .5) {
                this.coreForm = !coreForm;
                Messages.add(new MessageUpdate(formsie.getName() + " changed into " + (coreForm ? "Core" : "Meteor") + " Forme!")
                            .withImageName(formsie.getPokemonInfo().getImageName(formsie.isShiny(), !formsie.isPlayer(), coreForm), formsie.isPlayer())
                );
            }
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            changeForm(victim);
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            coreForm = false;
            changeForm(enterer);
        }

        @Override
        public Integer getStat(ActivePokemon user, Stat stat) {
            // Need to calculate the new stat -- yes, I realize this is super inefficient and whatever whatever whatever
            int index = stat.index();
            return Stat.getStat(index, user.getLevel(), getStats()[index], user.getIV(index), user.getEV(index), user.getNature().getNatureVal(index));
        }

        @Override
        public boolean isReplaceable() {
            return false;
        }

        @Override
        public boolean isStealable() {
            return false;
        }
    }

    static class StanceChange extends Ability implements BeforeTurnEffect, EntryEffect, DifferentStatEffect {
        private static final long serialVersionUID = 1L;

        private static final int[] BLADE_STATS = new int[] { 60, 150, 50, 150, 50, 60 };
        private static final int[] SHIELD_STATS = new int[] { 60, 50, 150, 50, 150, 60 };

        private boolean shieldForm;

        private int[] getStats() {
            return shieldForm ? SHIELD_STATS : BLADE_STATS;
        }

        StanceChange() {
            super(AbilityNamesies.STANCE_CHANGE, "The Pok\u00e9mon changes its form to Blade Forme when it uses an attack move, and changes to Shield Forme when it uses King's Shield.");
            this.shieldForm = true;
        }

        @Override
        public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
            // TODO: Change image once I can find Aegislash Shield Form sprites
            if (shieldForm && !p.getAttack().isStatusMove()) {
                shieldForm = false;
                Messages.add(p.getName() + " changed into Blade Forme!");
            } else if (!shieldForm && p.getAttack().namesies() == AttackNamesies.KINGS_SHIELD) {
                shieldForm = true;
                Messages.add(p.getName() + " changed into Shield Forme!");
            }

            return true;
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            Messages.add(enterer.getName() + " is in Shield Forme!");
            shieldForm = true;
        }

        @Override
        public Integer getStat(ActivePokemon user, Stat stat) {
            // Need to calculate the new stat -- yes, I realize this is super inefficient and whatever whatever whatever
            int index = stat.index();
            return Stat.getStat(index, user.getLevel(), getStats()[index], user.getIV(index), user.getEV(index), user.getNature().getNatureVal(index));
        }

        @Override
        public boolean isReplaceable() {
            return false;
        }

        @Override
        public boolean isStealable() {
            return false;
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
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return b.hasEffect(EffectNamesies.GRASSY_TERRAIN);
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.DEFENSE;
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
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return b.hasEffect(EffectNamesies.ELECTRIC_TERRAIN);
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
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
        public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
            return victim.isType(b, Type.GRASS);
        }

        @Override
        public String statusPreventionMessage(ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " prevents status conditions!";
        }

        @Override
        public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
            return victim.isType(b, Type.GRASS);
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

    static class Protean extends Ability implements BeforeTurnEffect, ChangeTypeSource {
        private static final long serialVersionUID = 1L;

        private Type type;

        Protean() {
            super(AbilityNamesies.PROTEAN, "Changes the Pok\u00e9mon's type to the type of the move it's about to use.");
        }

        @Override
        public Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return new Type[] { type, Type.NO_TYPE };
        }

        @Override
        public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
            // Protean activates for all moves except for Struggle
            if (p.getAttack().namesies() != AttackNamesies.STRUGGLE) {
                type = p.getAttackType();
                EffectNamesies.CHANGE_TYPE.getEffect().cast(b, p, p, CastSource.ABILITY, true);
            }

            return true;
        }
    }

    static class Stamina extends Ability implements TakeDamageEffect {
        private static final long serialVersionUID = 1L;

        Stamina() {
            super(AbilityNamesies.STAMINA, "Boosts the Defense stat when hit by an attack.");
        }

        @Override
        public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            victim.getStages().modifyStage(victim, 1, Stat.DEFENSE, b, CastSource.ABILITY);
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
                victim.getStages().modifyStage(victim, 2, Stat.DEFENSE, b, CastSource.ABILITY);
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
            return defending.hasStatus(StatusCondition.POISONED);
        }
    }

    static class WaterBubble extends Ability implements OpponentPowerChangeEffect, PowerChangeEffect, StatusPreventionEffect, EntryEndTurnEffect {
        private static final long serialVersionUID = 1L;

        private void removeStatus(Battle b, ActivePokemon victim) {
            if (victim.hasStatus(StatusCondition.BURNED)) {
                Status.removeStatus(b, victim, CastSource.ABILITY);
            }
        }

        WaterBubble() {
            super(AbilityNamesies.WATER_BUBBLE, "Lowers the power of Fire-type moves done to the Pok\u00e9mon and prevents the Pok\u00e9mon from getting a burn.");
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.WATER) ? 2 : 1;
        }

        @Override
        public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.FIRE) ? .5 : 1;
        }

        @Override
        public void applyEffect(Battle b, ActivePokemon p) {
            removeStatus(b, p);
        }

        @Override
        public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
            return status == StatusCondition.BURNED;
        }

        @Override
        public String statusPreventionMessage(ActivePokemon victim) {
            return victim.getName() + "'s " + this.getName() + " prevents burns!";
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
                victim.getStages().modifyStage(victim, 1, Stat.SP_ATTACK, b, CastSource.ABILITY);
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
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            this.activated = false;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return activated ? 1.2 : 1;
        }

        @Override
        public Type changeAttackType(Attack attack, Type original) {
            if (original == Type.NORMAL) {
                this.activated = true;
                return Type.ELECTRIC;
            }

            return original;
        }
    }

    static class Disguise extends Ability implements AbsorbDamageEffect {
        private static final long serialVersionUID = 1L;

        private boolean activated;

        Disguise() {
            super(AbilityNamesies.DISGUISE, "Once per battle, the shroud that covers the Pok\u00e9mon can protect it from an attack.");
            this.activated = false;
        }

        @Override
        public boolean absorbDamage(Battle b, ActivePokemon damageTaker, int damageAmount) {
            if (!activated && b.getOtherPokemon(damageTaker).isAttacking()) {
                boolean isPlayer = damageTaker.isPlayer();
                boolean shiny = damageTaker.isShiny();
                boolean front = !isPlayer;

                Messages.add(new MessageUpdate(damageTaker.getName() + "'s disguise was busted!!")
                            .withImageName(damageTaker.getPokemonInfo().getImageName(shiny, front, true), isPlayer)
                );

                activated = true;
                return true;
            }

            return false;
        }

        @Override
        public boolean isReplaceable() {
            return false;
        }

        @Override
        public boolean isStealable() {
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
            if (sleepyHead.hasStatus(StatusCondition.ASLEEP)) {
                return false;
            }

            if (Status.appliesWithoutStatusCheck(StatusCondition.ASLEEP, b, sleepyHead, sleepyHead)) {
                sleepyHead.removeStatus();
                Status.applyStatus(b, sleepyHead, sleepyHead, StatusCondition.ASLEEP, CastSource.ABILITY);
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
        public String getBlockMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
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
        public String getBlockMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
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
        public boolean isActive() {
            return this.activated;
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
    }

    static class InnardsOut extends Ability implements StatusReceivedEffect {
        private static final long serialVersionUID = 1L;

        InnardsOut() {
            super(AbilityNamesies.INNARDS_OUT, "Damages the attacker landing the finishing hit by the amount equal to its last HP.");
        }

        private void deathWish(Battle b, ActivePokemon dead, ActivePokemon murderer) {
            Messages.add(murderer.getName() + " was hurt by " + dead.getName() + "'s " + this.getName() + "!");
            murderer.reduceHealth(b, dead.getDamageTaken());
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

    static class Fluffy extends Ability implements OpponentPowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Fluffy() {
            super(AbilityNamesies.FLUFFY, "Halves the damage taken from moves that make direct contact, but doubles that of Fire-type moves.");
        }

        @Override
        public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (user.getAttack().isMoveType(MoveType.PHYSICAL_CONTACT) && user.isAttackType(Type.FIRE)) {
                return .5;
            }

            if (user.isAttackType(Type.FIRE)) {
                return 2;
            }

            return 1;
        }
    }

    static class TanglingHair extends Ability implements PhysicalContactEffect {
        private static final long serialVersionUID = 1L;

        TanglingHair() {
            super(AbilityNamesies.TANGLING_HAIR, "Contact with the Pok\u00e9mon lowers the attacker's Speed stat.");
        }

        @Override
        public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
            user.getStages().modifyStage(victim, -1, Stat.SPEED, b, CastSource.ABILITY);
        }
    }

    static class PsychicSurge extends Ability implements EntryEffect {
        private static final long serialVersionUID = 1L;

        PsychicSurge() {
            super(AbilityNamesies.PSYCHIC_SURGE, "Turns the ground into Psychic Terrain when the Pok\u00e9mon enters a battle.");
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            Messages.add(enterer.getName() + "'s " + this.getName() + " changed the field to Psychic Terrain!");
            EffectNamesies.PSYCHIC_TERRAIN.getEffect().cast(b, enterer, enterer, CastSource.ABILITY, false);
        }
    }

    static class ElectricSurge extends Ability implements EntryEffect {
        private static final long serialVersionUID = 1L;

        ElectricSurge() {
            super(AbilityNamesies.ELECTRIC_SURGE, "Turns the ground into Electric Terrain when the Pok\u00e9mon enters a battle.");
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            Messages.add(enterer.getName() + "'s " + this.getName() + " changed the field to Electric Terrain!");
            EffectNamesies.ELECTRIC_TERRAIN.getEffect().cast(b, enterer, enterer, CastSource.ABILITY, false);
        }
    }

    static class MistySurge extends Ability implements EntryEffect {
        private static final long serialVersionUID = 1L;

        MistySurge() {
            super(AbilityNamesies.MISTY_SURGE, "Turns the ground into Misty Terrain when the Pok\u00e9mon enters a battle.");
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            Messages.add(enterer.getName() + "'s " + this.getName() + " changed the field to Misty Terrain!");
            EffectNamesies.MISTY_TERRAIN.getEffect().cast(b, enterer, enterer, CastSource.ABILITY, false);
        }
    }

    static class GrassySurge extends Ability implements EntryEffect {
        private static final long serialVersionUID = 1L;

        GrassySurge() {
            super(AbilityNamesies.GRASSY_SURGE, "Turns the ground into Grassy Terrain when the Pok\u00e9mon enters a battle.");
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            Messages.add(enterer.getName() + "'s " + this.getName() + " changed the field to Grassy Terrain!");
            EffectNamesies.GRASSY_TERRAIN.getEffect().cast(b, enterer, enterer, CastSource.ABILITY, false);
        }
    }
}
