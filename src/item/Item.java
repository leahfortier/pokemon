package item;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.attack.MoveCategory;
import battle.attack.MoveType;
import battle.effect.Effect;
import battle.effect.EffectInterfaces.ApplyDamageEffect;
import battle.effect.EffectInterfaces.ChoiceEffect;
import battle.effect.EffectInterfaces.EntryEndTurnEffect;
import battle.effect.EffectInterfaces.ItemSwapperEffect;
import battle.effect.EffectInterfaces.MessageGetter;
import battle.effect.EffectInterfaces.OpponentTakeDamageEffect;
import battle.effect.EffectInterfaces.PartialTrappingEffect;
import battle.effect.EffectInterfaces.PhysicalContactEffect;
import battle.effect.EffectInterfaces.SimpleStatModifyingEffect;
import battle.effect.EffectInterfaces.TakeDamageEffect;
import battle.effect.EffectInterfaces.WeatherExtendingEffect;
import battle.effect.EffectNamesies;
import battle.effect.InvokeInterfaces.AttackMissedEffect;
import battle.effect.InvokeInterfaces.AttackSelectionEffect;
import battle.effect.InvokeInterfaces.BarrierEffect;
import battle.effect.InvokeInterfaces.BracingEffect;
import battle.effect.InvokeInterfaces.CritStageEffect;
import battle.effect.InvokeInterfaces.DefendingNoAdvantageChanger;
import battle.effect.InvokeInterfaces.DefiniteEscape;
import battle.effect.InvokeInterfaces.EffectExtendingEffect;
import battle.effect.InvokeInterfaces.EffectReceivedEffect;
import battle.effect.InvokeInterfaces.EndAttackEffect;
import battle.effect.InvokeInterfaces.EndTurnEffect;
import battle.effect.InvokeInterfaces.EntryEffect;
import battle.effect.InvokeInterfaces.GroundedEffect;
import battle.effect.InvokeInterfaces.HalfWeightEffect;
import battle.effect.InvokeInterfaces.LevitationEffect;
import battle.effect.InvokeInterfaces.PowderBlocker;
import battle.effect.InvokeInterfaces.PowerChangeEffect;
import battle.effect.InvokeInterfaces.RepellingEffect;
import battle.effect.InvokeInterfaces.StallingEffect;
import battle.effect.InvokeInterfaces.StatLoweredEffect;
import battle.effect.InvokeInterfaces.StrikeFirstEffect;
import battle.effect.InvokeInterfaces.TerrainCastEffect;
import battle.effect.InvokeInterfaces.VictimOnDamageEffect;
import battle.effect.InvokeInterfaces.WeatherBlockerEffect;
import battle.effect.battle.StandardBattleEffectNamesies;
import battle.effect.battle.weather.WeatherNamesies;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.source.CastSource;
import battle.effect.status.StatusNamesies;
import battle.effect.team.TeamEffectNamesies;
import battle.stages.StageModifier;
import gui.GameData;
import item.bag.BagCategory;
import item.bag.BattleBagCategory;
import item.berry.Berry;
import item.berry.CategoryBerry.CategoryDamageBerry;
import item.berry.CategoryBerry.CategoryIncreaseBerry;
import item.berry.EvDecreaseBerry;
import item.berry.GainableEffectBerry;
import item.berry.HealthTriggeredBerry;
import item.berry.HealthTriggeredStageIncreaseBerry;
import item.berry.StatusBerry;
import item.berry.TypedPowerReduceBerry;
import item.berry.TypedPowerReduceBerry.SuperEffectiveTypedPowerReduceBerry;
import item.hold.EVItem;
import item.hold.EffectCurerItem;
import item.hold.HoldItem;
import item.hold.IncenseItem;
import item.hold.PowerItem;
import item.hold.SpecialTypeItem.DriveItem;
import item.hold.SpecialTypeItem.GemItem;
import item.hold.SpecialTypeItem.MemoryItem;
import item.hold.SpecialTypeItem.PlateItem;
import item.medicine.AllPPHealer;
import item.medicine.EvIncreaser.Vitamin;
import item.medicine.EvIncreaser.Wing;
import item.medicine.HpHealer;
import item.medicine.PPHealer;
import item.medicine.RepelItem;
import item.medicine.StatusHealer;
import item.use.BallItem;
import item.use.BattlePokemonUseItem;
import item.use.BattleUseItem;
import item.use.EvolutionItem;
import item.use.MoveUseItem;
import item.use.NatureMint;
import item.use.PlayerUseItem;
import item.use.PokemonUseItem;
import item.use.TechnicalMachine;
import main.Game;
import map.MapData;
import map.overworld.TerrainType;
import map.overworld.wild.WildEncounter;
import map.triggers.Trigger;
import map.triggers.battle.WalkingWildBattleTrigger;
import map.triggers.battle.WildBattleTrigger;
import message.MessageUpdate;
import message.Messages;
import pokemon.ability.AbilityNamesies;
import pokemon.active.Gender;
import pokemon.active.Nature;
import pokemon.evolution.EvolutionMethod;
import pokemon.species.PokemonNamesies;
import pokemon.stat.Stat;
import trainer.Trainer;
import trainer.player.Player;
import type.Type;
import type.TypeAdvantage;
import util.RandomUtils;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public abstract class Item implements ItemInterface, Comparable<Item> {
    private static final long serialVersionUID = 1L;

    public static final Dimension MAX_IMAGE_SIZE = new Dimension(28, 28);

    private final ItemNamesies namesies;
    private final String description;
    private final BagCategory bagCategory;
    private List<BattleBagCategory> battleBagCategories;
    private int price;

    public Item(ItemNamesies name, String description, BagCategory category) {
        this.namesies = name;
        this.description = description;
        this.bagCategory = category;

        this.battleBagCategories = new ArrayList<>();
        this.price = -1;
    }

    @Override
    public int compareTo(Item o) {
        return this.getName().compareTo(o.getName());
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public boolean isHoldable() {
        return this instanceof HoldItem;
    }

    public boolean hasQuantity() {
        return bagCategory != BagCategory.TM && bagCategory != BagCategory.KEY_ITEM;
    }

    @Override
    public ItemNamesies namesies() {
        return this.namesies;
    }

    @Override
    public String getName() {
        return this.namesies().getName();
    }

    public String getDescription() {
        return this.description;
    }

    public int getPrice() {
        return this.price;
    }

    public int getSellPrice() {
        return this.price/2;
    }

    public BagCategory getBagCategory() {
        return this.bagCategory;
    }

    public Iterable<BattleBagCategory> getBattleBagCategories() {
        return this.battleBagCategories;
    }

    public boolean hasBattleBagCategories() {
        return !this.battleBagCategories.isEmpty();
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

    // EVERYTHING BELOW IS GENERATED ###

    /**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

    static class NoItem extends Item implements HoldItem {
        private static final long serialVersionUID = 1L;

        NoItem() {
            super(ItemNamesies.NO_ITEM, "YOU SHOULDN'T SEE THIS", BagCategory.MISC);
            super.price = -1;
        }

        @Override
        public int flingDamage() {
            return 9001;
        }
    }

    static class Syrup extends Item {
        private static final long serialVersionUID = 1L;

        Syrup() {
            super(ItemNamesies.SYRUP, "A mysterious bottle of syrup. Maybe it will be useful some day.", BagCategory.KEY_ITEM);
        }
    }

    static class Bicycle extends Item {
        private static final long serialVersionUID = 1L;

        Bicycle() {
            super(ItemNamesies.BICYCLE, "A folding bike that enables a rider to get around much faster than with Running Shoes.", BagCategory.KEY_ITEM);
        }
    }

    static class Surfboard extends Item {
        private static final long serialVersionUID = 1L;

        Surfboard() {
            super(ItemNamesies.SURFBOARD, "A fancy shmancy surfboard that lets you be RADICAL DUDE!", BagCategory.KEY_ITEM);
        }
    }

    static class FishingRod extends Item {
        private static final long serialVersionUID = 1L;

        FishingRod() {
            super(ItemNamesies.FISHING_ROD, "Use it by the water to fish up various kinds of Pokémon.", BagCategory.KEY_ITEM);
        }
    }

    static class AbsorbBulb extends Item implements HoldItem, TakeDamageEffect {
        private static final long serialVersionUID = 1L;

        AbsorbBulb() {
            super(ItemNamesies.ABSORB_BULB, "An item to be held by a Pokémon. It boosts Sp. Atk if the holder is hit with a Water-type attack. It can only be used once.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void onDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (user.isAttackType(Type.WATER) && new StageModifier(1, Stat.SP_ATTACK).modify(b, victim, victim, CastSource.HELD_ITEM)) {
                this.consumeItem(b, victim);
            }
        }
    }

    // Air Balloon ALWAYS pops when hit -- one of the few items to be able to be consumed even when
    // the holder is dead, and one of the few effects that is triggered even when holder is behind
    // a substitute or is diguised etc
    static class AirBalloon extends Item implements HoldItem, LevitationEffect, VictimOnDamageEffect, EntryEffect {
        private static final long serialVersionUID = 1L;

        AirBalloon() {
            super(ItemNamesies.AIR_BALLOON, "An item to be held by a Pokémon. The holder will float in the air until hit. Once the holder is hit, this item will burst.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            Messages.add(enterer.getName() + " floats with its " + this.getName() + "!");
        }

        @Override
        public void onDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            Messages.add(victim.getName() + "'s " + this.getName() + " popped!");
            this.consumeItem(b, victim);
        }

        @Override
        public boolean ignoreAbsorbedDamage() {
            // Air Balloon will pop a balloon held by a substitute or a disguise
            return false;
        }

        @Override
        public int flingDamage() {
            return 10;
        }
    }

    static class AmuletCoin extends Item implements HoldItem, EntryEndTurnEffect {
        private static final long serialVersionUID = 1L;

        AmuletCoin() {
            super(ItemNamesies.AMULET_COIN, "An item to be held by a Pok\u00e9mon. It doubles any prize money received if the holding Pok\u00e9mon joins a battle.", BagCategory.MISC);
            super.price = 10000;
        }

        private void getDatCashMoneyGetDatCashMoneyCast(Battle b, ActivePokemon gettinDatCashMoneyTwice) {
            Effect.cast(TeamEffectNamesies.GET_DAT_CASH_MONEY_TWICE, b, gettinDatCashMoneyTwice, gettinDatCashMoneyTwice, CastSource.HELD_ITEM, false);
        }

        @Override
        public void applyEffect(Battle b, ActivePokemon p) {
            // This is named too fantastically to just be applyEffect
            getDatCashMoneyGetDatCashMoneyCast(b, p);
        }
    }

    static class BigRoot extends Item implements HoldItem {
        private static final long serialVersionUID = 1L;

        BigRoot() {
            super(ItemNamesies.BIG_ROOT, "An item to be held by a Pokémon. It boosts the amount of HP the holder restores from HP-stealing moves.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public int flingDamage() {
            return 10;
        }
    }

    static class BindingBand extends Item implements HoldItem {
        private static final long serialVersionUID = 1L;

        BindingBand() {
            super(ItemNamesies.BINDING_BAND, "An item to be held by a Pokémon. It's a band that increases the power of binding moves used by the holder.", BagCategory.MISC);
            super.price = 4000;
        }
    }

    static class BlackSludge extends Item implements HoldItem, EndTurnEffect {
        private static final long serialVersionUID = 1L;

        BlackSludge() {
            super(ItemNamesies.BLACK_SLUDGE, "An item to be held by a Pok\u00e9mon. It gradually restores HP to Poison-type Pok\u00e9mon. It damages any other type.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (victim.isType(b, Type.POISON)) {
                victim.healHealthFraction(1/16.0, b, victim.getName() + "'s HP was restored by its " + this.getName() + "!");
            } else {
                victim.reduceHealthFraction(b, 1/8.0, victim.getName() + " lost some of its HP due to its " + this.getName() + "!");
            }
        }
    }

    static class BrightPowder extends Item implements HoldItem, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        BrightPowder() {
            super(ItemNamesies.BRIGHT_POWDER, "An item to be held by a Pok\u00e9mon. It casts a tricky glare that lowers the opposing Pok\u00e9mon's accuracy.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public int flingDamage() {
            return 10;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.EVASION;
        }

        @Override
        public double getModifier() {
            return 1.1;
        }
    }

    static class CellBattery extends Item implements HoldItem, TakeDamageEffect {
        private static final long serialVersionUID = 1L;

        CellBattery() {
            super(ItemNamesies.CELL_BATTERY, "An item to be held by a Pokémon. It boosts Attack if the holder is hit with an Electric-type attack. It can only be used once.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void onDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (user.isAttackType(Type.ELECTRIC) && new StageModifier(1, Stat.ATTACK).modify(b, victim, victim, CastSource.HELD_ITEM)) {
                this.consumeItem(b, victim);
            }
        }
    }

    static class ChoiceBand extends Item implements HoldItem, ChoiceEffect {
        private static final long serialVersionUID = 1L;

        ChoiceBand() {
            super(ItemNamesies.CHOICE_BAND, "An item to be held by a Pok\u00e9mon. This curious headband boosts Attack but only allows the use of one move.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public Stat getBoosted() {
            return Stat.ATTACK;
        }

        @Override
        public int flingDamage() {
            return 10;
        }
    }

    static class ChoiceScarf extends Item implements HoldItem, ChoiceEffect {
        private static final long serialVersionUID = 1L;

        ChoiceScarf() {
            super(ItemNamesies.CHOICE_SCARF, "An item to be held by a Pok\u00e9mon. This curious scarf boosts Speed but only allows the use of one move.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public Stat getBoosted() {
            return Stat.SPEED;
        }

        @Override
        public int flingDamage() {
            return 10;
        }
    }

    static class ChoiceSpecs extends Item implements HoldItem, ChoiceEffect {
        private static final long serialVersionUID = 1L;

        ChoiceSpecs() {
            super(ItemNamesies.CHOICE_SPECS, "An item to be held by a Pok\u00e9mon. These curious glasses boost Sp. Atk but only allow the use of one move.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public Stat getBoosted() {
            return Stat.SP_ATTACK;
        }

        @Override
        public int flingDamage() {
            return 10;
        }
    }

    static class CleanseTag extends Item implements HoldItem, RepellingEffect {
        private static final long serialVersionUID = 1L;

        CleanseTag() {
            super(ItemNamesies.CLEANSE_TAG, "An item to be held by a Pok\u00e9mon. It helps keep wild Pok\u00e9mon away if the holder is the head of the party.", BagCategory.MISC);
            super.price = 5000;
        }

        @Override
        public boolean shouldRepel(ActivePokemon playerFront, WildEncounter wildPokemon) {
            return RandomUtils.chanceTest(1, 3) && wildPokemon.getLevel() <= playerFront.getLevel();
        }
    }

    static class DampRock extends Item implements HoldItem, WeatherExtendingEffect {
        private static final long serialVersionUID = 1L;

        DampRock() {
            super(ItemNamesies.DAMP_ROCK, "An item to be held by a Pok\u00e9mon. It extends the duration of the move Rain Dance when used by the holder.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public WeatherNamesies getWeatherType() {
            return WeatherNamesies.RAINING;
        }

        @Override
        public int flingDamage() {
            return 60;
        }
    }

    static class HeatRock extends Item implements HoldItem, WeatherExtendingEffect {
        private static final long serialVersionUID = 1L;

        HeatRock() {
            super(ItemNamesies.HEAT_ROCK, "An item to be held by a Pok\u00e9mon. It extends the duration of the move Sunny Day when used by the holder.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public WeatherNamesies getWeatherType() {
            return WeatherNamesies.SUNNY;
        }

        @Override
        public int flingDamage() {
            return 60;
        }
    }

    static class IcyRock extends Item implements HoldItem, WeatherExtendingEffect {
        private static final long serialVersionUID = 1L;

        IcyRock() {
            super(ItemNamesies.ICY_ROCK, "An item to be held by a Pok\u00e9mon. It extends the duration of the move Hail when used by the holder.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public WeatherNamesies getWeatherType() {
            return WeatherNamesies.HAILING;
        }

        @Override
        public int flingDamage() {
            return 40;
        }
    }

    static class SmoothRock extends Item implements HoldItem, WeatherExtendingEffect {
        private static final long serialVersionUID = 1L;

        SmoothRock() {
            super(ItemNamesies.SMOOTH_ROCK, "An item to be held by a Pok\u00e9mon. It extends the duration of the move Sandstorm when used by the holder.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public WeatherNamesies getWeatherType() {
            return WeatherNamesies.SANDSTORM;
        }

        @Override
        public int flingDamage() {
            return 10;
        }
    }

    static class EjectButton extends Item implements HoldItem, TakeDamageEffect {
        private static final long serialVersionUID = 1L;

        EjectButton() {
            super(ItemNamesies.EJECT_BUTTON, "An item to be held by a Pok\u00e9mon. If the holder is hit by an attack, it will be switched out of battle.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void onDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (victim.switcheroo(b, victim, CastSource.HELD_ITEM, false)) {
                this.consumeItem(b, victim);
            }
        }
    }

    static class DestinyKnot extends Item implements HoldItem, EffectReceivedEffect {
        private static final long serialVersionUID = 1L;

        DestinyKnot() {
            super(ItemNamesies.DESTINY_KNOT, "An item to be held by a Pok\u00e9mon. If the holder becomes infatuated, the opposing Pok\u00e9mon will be, too.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void receiveEffect(Battle b, ActivePokemon caster, ActivePokemon victim, EffectNamesies effectType) {
            if (effectType == PokemonEffectNamesies.INFATUATION) {
                String message = victim.getName() + "'s " + this.getName() + " caused " + caster.getName() + " to fall in love!";
                Effect.apply(PokemonEffectNamesies.INFATUATION, b, victim, caster, CastSource.HELD_ITEM, message);
            }
        }

        @Override
        public int flingDamage() {
            return 10;
        }
    }

    static class ExpertBelt extends Item implements HoldItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        ExpertBelt() {
            super(ItemNamesies.EXPERT_BELT, "An item to be held by a Pok\u00e9mon. It's a well-worn belt that slightly boosts the power of supereffective moves.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public int flingDamage() {
            return 10;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return TypeAdvantage.isSuperEffective(user, victim, b) ? 1.2 : 1;
        }
    }

    static class FlameOrb extends Item implements HoldItem, EndTurnEffect {
        private static final long serialVersionUID = 1L;

        FlameOrb() {
            super(ItemNamesies.FLAME_ORB, "An item to be held by a Pok\u00e9mon. It's a bizarre orb that will afflict the holder with a burn during battle.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            StatusNamesies.BURNED.getStatus().apply(b, victim, victim, victim.getName() + " was burned by its " + this.getName() + "!");
        }

        @Override
        public void flingEffect(Battle b, ActivePokemon pelted) {
            StatusNamesies.BURNED.getStatus().apply(b, pelted, pelted, pelted.getName() + " was burned by the " + this.getName() + "!");
        }
    }

    static class ToxicOrb extends Item implements HoldItem, EndTurnEffect {
        private static final long serialVersionUID = 1L;

        ToxicOrb() {
            super(ItemNamesies.TOXIC_ORB, "An item to be held by a Pok\u00e9mon. It's a bizarre orb that will badly poison the holder during battle.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            StatusNamesies.BADLY_POISONED.getStatus().apply(b, victim, victim, victim.getName() + " was badly poisoned by its " + this.getName() + "!");
        }

        @Override
        public void flingEffect(Battle b, ActivePokemon pelted) {
            StatusNamesies.BADLY_POISONED.getStatus().apply(b, pelted, pelted, pelted.getName() + " was badly poisoned by the " + this.getName() + "!");
        }
    }

    static class FloatStone extends Item implements HoldItem, HalfWeightEffect {
        private static final long serialVersionUID = 1L;

        FloatStone() {
            super(ItemNamesies.FLOAT_STONE, "An item to be held by a Pok\u00e9mon. This very light stone reduces the weight of a Pok\u00e9mon when held.", BagCategory.MISC);
            super.price = 4000;
        }
    }

    static class FocusBand extends Item implements HoldItem, BracingEffect {
        private static final long serialVersionUID = 1L;

        FocusBand() {
            super(ItemNamesies.FOCUS_BAND, "An item to be held by a Pok\u00e9mon. The holder may endure a potential KO attack, leaving it with just 1 HP.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public boolean isBracing(Battle b, ActivePokemon bracer, boolean fullHealth) {
            return RandomUtils.chanceTest(10);
        }

        @Override
        public String braceMessage(ActivePokemon bracer) {
            return bracer.getName() + " held on with its " + this.getName() + "!";
        }

        @Override
        public int flingDamage() {
            return 10;
        }
    }

    static class FocusSash extends Item implements HoldItem, BracingEffect {
        private static final long serialVersionUID = 1L;

        FocusSash() {
            super(ItemNamesies.FOCUS_SASH, "An item to be held by a Pok\u00e9mon. If the holder has full HP, it will endure a potential KO attack with 1 HP. The item then disappears.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public boolean isBracing(Battle b, ActivePokemon bracer, boolean fullHealth) {
            if (fullHealth) {
                this.consumeItem(b, bracer);
                return true;
            }

            return false;
        }

        @Override
        public String braceMessage(ActivePokemon bracer) {
            return bracer.getName() + " held on with its " + this.getName() + "!";
        }

        @Override
        public int flingDamage() {
            return 10;
        }
    }

    static class GripClaw extends Item implements HoldItem, EffectExtendingEffect {
        private static final long serialVersionUID = 1L;

        GripClaw() {
            super(ItemNamesies.GRIP_CLAW, "An item to be held by a Pokémon. It extends the duration of multi-turn attacks, such as Bind and Wrap.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public int getExtensionTurns(Effect receivedEffect, int numTurns) {
            // Grip Claw always gives five turns
            return receivedEffect instanceof PartialTrappingEffect ? 5 - numTurns : 0;
        }

        @Override
        public int flingDamage() {
            return 90;
        }
    }

    static class AdamantOrb extends Item implements HoldItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        AdamantOrb() {
            super(ItemNamesies.ADAMANT_ORB, "A brightly gleaming orb to be held by Dialga. It boosts the power of Dragon- and Steel-type moves when it is held.", BagCategory.MISC);
            super.price = 10000;
        }

        @Override
        public int flingDamage() {
            return 60;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isPokemon(PokemonNamesies.DIALGA) && user.isAttackType(Type.DRAGON, Type.STEEL) ? 1.2 : 1;
        }
    }

    static class LustrousOrb extends Item implements HoldItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        LustrousOrb() {
            super(ItemNamesies.LUSTROUS_ORB, "A beautifully glowing orb to be held by Palkia. It boosts the power of Dragon- and Water-type moves when it is held.", BagCategory.MISC);
            super.price = 10000;
        }

        @Override
        public int flingDamage() {
            return 60;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isPokemon(PokemonNamesies.PALKIA) && user.isAttackType(Type.DRAGON, Type.WATER) ? 1.2 : 1;
        }
    }

    static class GriseousOrb extends Item implements HoldItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        GriseousOrb() {
            super(ItemNamesies.GRISEOUS_ORB, "A glowing orb to be held by Giratina. It boosts the power of Dragon- and Ghost-type moves when it is held.", BagCategory.MISC);
            super.price = 10000;
        }

        @Override
        public int flingDamage() {
            return 60;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isPokemon(PokemonNamesies.GIRATINA) && user.isAttackType(Type.DRAGON, Type.GHOST) ? 1.2 : 1;
        }
    }

    static class IronBall extends Item implements HoldItem, GroundedEffect, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        IronBall() {
            super(ItemNamesies.IRON_BALL, "An item to be held by a Pokémon. It lowers Speed and allows Ground-type moves to hit Flying types and holders that are levitating.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void flingEffect(Battle b, ActivePokemon pelted) {
            // Technically the Iron Ball doesn't do this as a fling effect, but it almost makes sense so I'm doing it
            removeLevitation(b, pelted);
        }

        @Override
        public int flingDamage() {
            return 130;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
        }

        @Override
        public double getModifier() {
            return .5;
        }
    }

    static class LaggingTail extends Item implements HoldItem, StallingEffect {
        private static final long serialVersionUID = 1L;

        LaggingTail() {
            super(ItemNamesies.LAGGING_TAIL, "An item to be held by a Pok\u00e9mon. It is tremendously heavy and makes the holder move slower than usual.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public int flingDamage() {
            return 10;
        }
    }

    static class LifeOrb extends Item implements HoldItem, ApplyDamageEffect, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        LifeOrb() {
            super(ItemNamesies.LIFE_ORB, "An item to be held by a Pokémon. It boosts the power of moves but at the cost of some HP on each hit.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return 5324.0/4096.0;
        }

        @Override
        public void onDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            user.reduceHealthFraction(b, .1, user.getName() + " was hurt by its " + this.getName() + "!");
        }

        @Override
        public boolean ignoreAbsorbedDamage() {
            // Life Orb should still reduce health even if damage was absorbed by substitute etc
            return false;
        }
    }

    static class LightBall extends Item implements HoldItem, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        LightBall() {
            super(ItemNamesies.LIGHT_BALL, "An item to be held by Pikachu. It's a puzzling orb that boosts its Attack and Sp. Atk stats.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.ATTACK || s == Stat.SP_ATTACK;
        }

        @Override
        public void flingEffect(Battle b, ActivePokemon pelted) {
            StatusNamesies.PARALYZED.getStatus().apply(b, pelted, pelted, pelted.getName() + " was paralyzed by the " + this.getName() + "!");
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return p.isPokemon(PokemonNamesies.PIKACHU);
        }

        @Override
        public double getModifier() {
            return 2;
        }
    }

    static class LightClay extends Item implements HoldItem, EffectExtendingEffect {
        private static final long serialVersionUID = 1L;

        LightClay() {
            super(ItemNamesies.LIGHT_CLAY, "An item to be held by a Pok\u00e9mon. Protective moves like Light Screen and Reflect will be effective longer.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public int getExtensionTurns(Effect receivedEffect, int numTurns) {
            return receivedEffect instanceof BarrierEffect ? 3 : 0;
        }
    }

    static class LuckyEgg extends Item implements HoldItem {
        private static final long serialVersionUID = 1L;

        LuckyEgg() {
            super(ItemNamesies.LUCKY_EGG, "An item to be held by a Pokémon. It's an egg filled with happiness that earns the holder extra Exp. Points in battle.", BagCategory.MISC);
            super.price = 10000;
        }
    }

    static class LuckyPunch extends Item implements HoldItem, CritStageEffect {
        private static final long serialVersionUID = 1L;

        LuckyPunch() {
            super(ItemNamesies.LUCKY_PUNCH, "An item to be held by Chansey. This pair of lucky boxing gloves will boost Chansey's critical-hit ratio.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public int increaseCritStage(ActivePokemon p) {
            return p.isPokemon(PokemonNamesies.CHANSEY) ? 2 : 0;
        }

        @Override
        public int flingDamage() {
            return 40;
        }
    }

    static class LuminousMoss extends Item implements HoldItem, TakeDamageEffect {
        private static final long serialVersionUID = 1L;

        LuminousMoss() {
            super(ItemNamesies.LUMINOUS_MOSS, "An item to be held by a Pokémon. It boosts Sp. Def if the holder is hit with a Water-type attack. It can only be used once.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void onDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (user.isAttackType(Type.WATER) && new StageModifier(1, Stat.SP_DEFENSE).modify(b, victim, victim, CastSource.HELD_ITEM)) {
                this.consumeItem(b, victim);
            }
        }
    }

    static class MachoBrace extends Item implements EVItem, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        MachoBrace() {
            super(ItemNamesies.MACHO_BRACE, "An item to be held by a Pok\u00e9mon. This stiff, heavy brace helps Pok\u00e9mon grow strong but cuts Speed in battle.", BagCategory.MISC);
            super.price = 3000;
        }

        @Override
        public int[] getEVs(int[] vals) {
            for (int i = 0; i < vals.length; i++) {
                vals[i] *= 2;
            }

            return vals;
        }

        @Override
        public int flingDamage() {
            return 60;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
        }

        @Override
        public double getModifier() {
            return .5;
        }
    }

    static class MentalHerb extends Item implements HoldItem, EffectCurerItem {
        private static final long serialVersionUID = 1L;

        private static final Set<PokemonEffectNamesies> REMOVABLE_EFFECTS = EnumSet.of(
                PokemonEffectNamesies.INFATUATION,
                PokemonEffectNamesies.DISABLE,
                PokemonEffectNamesies.TAUNT,
                PokemonEffectNamesies.ENCORE,
                PokemonEffectNamesies.TORMENT,
                PokemonEffectNamesies.CONFUSION,
                PokemonEffectNamesies.HEAL_BLOCK
        );

        MentalHerb() {
            super(ItemNamesies.MENTAL_HERB, "An item to be held by a Pok\u00e9mon. The holder shakes off move-binding effects to move freely. It can be used only once.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public Set<PokemonEffectNamesies> getCurableEffects() {
            return REMOVABLE_EFFECTS;
        }

        @Override
        public void flingEffect(Battle b, ActivePokemon pelted) {
            usesies(pelted);
        }

        @Override
        public int flingDamage() {
            return 10;
        }
    }

    static class MetalPowder extends Item implements HoldItem, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        MetalPowder() {
            super(ItemNamesies.METAL_POWDER, "An item to be held by Ditto. Extremely fine yet hard, this odd powder boosts the Defense stat.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public int flingDamage() {
            return 10;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.DEFENSE;
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return p.isPokemon(PokemonNamesies.DITTO);
        }

        @Override
        public double getModifier() {
            return 2;
        }
    }

    static class Metronome extends Item implements HoldItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Metronome() {
            super(ItemNamesies.METRONOME, "An item to be held by a Pokémon. It boosts moves used consecutively but only until a different move is used.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return Math.min(2, 1 + .2*(user.getCount() - 1));
        }
    }

    static class MuscleBand extends Item implements HoldItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        MuscleBand() {
            super(ItemNamesies.MUSCLE_BAND, "An item to be held by a Pok\u00e9mon. This headband exudes strength, slightly boosting the power of physical moves.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public int flingDamage() {
            return 10;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getAttack().getCategory() == MoveCategory.PHYSICAL ? 1.1 : 1;
        }
    }

    static class PowerAnklet extends Item implements PowerItem, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        PowerAnklet() {
            super(ItemNamesies.POWER_ANKLET, "An item to be held by a Pok\u00e9mon. It reduces Speed but allows the holder's Speed stat to grow more after battling.", BagCategory.MISC);
            super.price = 3000;
        }

        @Override
        public Stat powerStat() {
            return Stat.SPEED;
        }

        @Override
        public int[] getEVs(int[] vals) {
            vals[powerStat().index()] += 4;
            return vals;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
        }

        @Override
        public int flingDamage() {
            return 70;
        }

        @Override
        public double getModifier() {
            return .5;
        }
    }

    static class PowerBand extends Item implements PowerItem, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        PowerBand() {
            super(ItemNamesies.POWER_BAND, "An item to be held by a Pok\u00e9mon. It reduces Speed but allows the holder's Sp. Def stat to grow more after battling.", BagCategory.MISC);
            super.price = 3000;
        }

        @Override
        public Stat powerStat() {
            return Stat.SP_DEFENSE;
        }

        @Override
        public int[] getEVs(int[] vals) {
            vals[powerStat().index()] += 4;
            return vals;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
        }

        @Override
        public int flingDamage() {
            return 70;
        }

        @Override
        public double getModifier() {
            return .5;
        }
    }

    static class PowerBelt extends Item implements PowerItem, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        PowerBelt() {
            super(ItemNamesies.POWER_BELT, "An item to be held by a Pok\u00e9mon. It reduces Speed but allows the holder's Defense stat to grow more after battling.", BagCategory.MISC);
            super.price = 3000;
        }

        @Override
        public Stat powerStat() {
            return Stat.DEFENSE;
        }

        @Override
        public int[] getEVs(int[] vals) {
            vals[powerStat().index()] += 4;
            return vals;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
        }

        @Override
        public int flingDamage() {
            return 70;
        }

        @Override
        public double getModifier() {
            return .5;
        }
    }

    static class PowerBracer extends Item implements PowerItem, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        PowerBracer() {
            super(ItemNamesies.POWER_BRACER, "An item to be held by a Pok\u00e9mon. It reduces Speed but allows the holder's Attack stat to grow more after battling.", BagCategory.MISC);
            super.price = 3000;
        }

        @Override
        public Stat powerStat() {
            return Stat.ATTACK;
        }

        @Override
        public int[] getEVs(int[] vals) {
            vals[powerStat().index()] += 4;
            return vals;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
        }

        @Override
        public int flingDamage() {
            return 70;
        }

        @Override
        public double getModifier() {
            return .5;
        }
    }

    static class PowerLens extends Item implements PowerItem, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        PowerLens() {
            super(ItemNamesies.POWER_LENS, "An item to be held by a Pok\u00e9mon. It reduces Speed but allows the holder's Sp. Atk stat to grow more after battling.", BagCategory.MISC);
            super.price = 3000;
        }

        @Override
        public Stat powerStat() {
            return Stat.SP_ATTACK;
        }

        @Override
        public int[] getEVs(int[] vals) {
            vals[powerStat().index()] += 4;
            return vals;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
        }

        @Override
        public int flingDamage() {
            return 70;
        }

        @Override
        public double getModifier() {
            return .5;
        }
    }

    static class PowerWeight extends Item implements PowerItem, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        PowerWeight() {
            super(ItemNamesies.POWER_WEIGHT, "An item to be held by a Pokémon. It reduces Speed but allows the holder's max HP to grow more after battling.", BagCategory.MISC);
            super.price = 3000;
        }

        @Override
        public Stat powerStat() {
            return Stat.HP;
        }

        @Override
        public int[] getEVs(int[] vals) {
            vals[powerStat().index()] += 4;
            return vals;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
        }

        @Override
        public int flingDamage() {
            return 70;
        }

        @Override
        public double getModifier() {
            return .5;
        }
    }

    static class QuickClaw extends Item implements HoldItem, StrikeFirstEffect {
        private static final long serialVersionUID = 1L;

        QuickClaw() {
            super(ItemNamesies.QUICK_CLAW, "An item to be held by a Pok\u00e9mon. This light, sharp claw lets the bearer move first occasionally.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public boolean strikeFirst(Battle b, ActivePokemon striker) {
            // Quick Claw gives holder a 20% chance of striking first within its priority bracket
            return RandomUtils.chanceTest(20);
        }

        @Override
        public String getStrikeFirstMessage(ActivePokemon striker) {
            return striker.getName() + "'s " + this.getName() + " allowed it to strike first!";
        }

        @Override
        public int flingDamage() {
            return 80;
        }
    }

    static class QuickPowder extends Item implements HoldItem, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        QuickPowder() {
            super(ItemNamesies.QUICK_POWDER, "An item to be held by Ditto. Extremely fine yet hard, this odd powder boosts the Speed stat.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public int flingDamage() {
            return 10;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return p.isPokemon(PokemonNamesies.DITTO);
        }

        @Override
        public double getModifier() {
            return 1.5;
        }
    }

    // Note: Red Card does NOT trigger when the holder dies from an attack (it's dead and cannot consume items)
    static class RedCard extends Item implements HoldItem, TakeDamageEffect {
        private static final long serialVersionUID = 1L;

        RedCard() {
            super(ItemNamesies.RED_CARD, "An item to be held by a Pok\u00e9mon. When the holder is hit by an attack, the attacker is removed from battle.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void onDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (user.switcheroo(b, victim, CastSource.HELD_ITEM, false)) {
                this.consumeItem(b, victim);
            }
        }

        @Override
        public int flingDamage() {
            return 10;
        }
    }

    static class RingTarget extends Item implements HoldItem, DefendingNoAdvantageChanger {
        private static final long serialVersionUID = 1L;

        RingTarget() {
            super(ItemNamesies.RING_TARGET, "An item to be held by a Pok\u00e9mon. Moves that normally have no effect will land on a Pok\u00e9mon holding it.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public boolean negateNoAdvantage(Type attacking, Type defending) {
            return true;
        }

        @Override
        public int flingDamage() {
            return 10;
        }
    }

    static class RockyHelmet extends Item implements HoldItem, PhysicalContactEffect {
        private static final long serialVersionUID = 1L;

        RockyHelmet() {
            super(ItemNamesies.ROCKY_HELMET, "An item to be held by a Pok\u00e9mon. If the holder is hit, the attacker will also be damaged upon contact.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
            user.reduceHealthFraction(b, 1/8.0, user.getName() + " was hurt by " + victim.getName() + "'s " + this.getName() + "!");
        }

        @Override
        public int flingDamage() {
            return 60;
        }
    }

    static class SafetyGoggles extends Item implements HoldItem, WeatherBlockerEffect, PowderBlocker {
        private static final long serialVersionUID = 1L;

        SafetyGoggles() {
            super(ItemNamesies.SAFETY_GOGGLES, "An item to be held by a Pok\u00e9mon. These goggles protect the holder from both weather-related damage and powder.", BagCategory.MISC);
            super.price = 4000;
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

    static class ScopeLens extends Item implements HoldItem, CritStageEffect {
        private static final long serialVersionUID = 1L;

        ScopeLens() {
            super(ItemNamesies.SCOPE_LENS, "An item to be held by a Pok\u00e9mon. It's a lens for scoping out weak points. It boosts the holder's critical-hit ratio.", BagCategory.MISC);
            super.price = 4000;
        }
    }

    static class ShedShell extends Item implements HoldItem, EvolutionItem {
        private static final long serialVersionUID = 1L;

        ShedShell() {
            super(ItemNamesies.SHED_SHELL, "An item to be held by a Pok\u00e9mon. This discarded carapace enables the holder to switch out of battle without fail.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public int flingDamage() {
            return 10;
        }
    }

    static class ShellBell extends Item implements HoldItem, ApplyDamageEffect {
        private static final long serialVersionUID = 1L;

        ShellBell() {
            super(ItemNamesies.SHELL_BELL, "An item to be held by a Pokémon. The holder restores a little HP every time it inflicts damage on others.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void onDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (user.getAttack().isMoveType(MoveType.USER_FAINTS)) {
                return;
            }

            int damageDealt = user.getDamageDealt();
            int healAmount = (int)Math.ceil(damageDealt/8.0);
            user.heal(healAmount, b, user.getName() + " restored some HP due to its " + this.getName() + "!");
        }
    }

    static class SmokeBall extends Item implements HoldItem, DefiniteEscape {
        private static final long serialVersionUID = 1L;

        SmokeBall() {
            super(ItemNamesies.SMOKE_BALL, "An item to be held by a Pok\u00e9mon. It enables the holder to flee from any wild Pok\u00e9mon encounter without fail.", BagCategory.MISC);
            super.price = 4000;
        }
    }

    static class Snowball extends Item implements HoldItem, TakeDamageEffect {
        private static final long serialVersionUID = 1L;

        Snowball() {
            super(ItemNamesies.SNOWBALL, "An item to be held by a Pokémon. It boosts Attack if the holder is hit with an Ice-type attack. It can only be used once.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void onDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (user.isAttackType(Type.ICE) && new StageModifier(1, Stat.ATTACK).modify(b, victim, victim, CastSource.HELD_ITEM)) {
                this.consumeItem(b, victim);
            }
        }
    }

    // I changed the price on my own because everything else was increasing like that and this said N/A
    static class SoulDew extends Item implements HoldItem, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        SoulDew() {
            super(ItemNamesies.SOUL_DEW, "A wondrous orb to be held by either Latios or Latias. It raises the power of Psychic- and Dragon-type moves.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SP_ATTACK || s == Stat.SP_DEFENSE;
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return p.isPokemon(PokemonNamesies.LATIOS, PokemonNamesies.LATIAS) && p.isAttackType(Type.PSYCHIC, Type.DRAGON);
        }

        @Override
        public double getModifier() {
            return 1.2;
        }
    }

    static class Stick extends Item implements HoldItem, CritStageEffect, EvolutionItem {
        private static final long serialVersionUID = 1L;

        Stick() {
            super(ItemNamesies.STICK, "An item to be held by Farfetch'd. This very long and stiff stalk of leek boosts its critical-hit ratio.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public int increaseCritStage(ActivePokemon p) {
            return p.isPokemon(PokemonNamesies.FARFETCHD) ? 2 : 0;
        }

        @Override
        public int flingDamage() {
            return 60;
        }
    }

    static class StickyBarb extends Item implements HoldItem, EndTurnEffect, PhysicalContactEffect, ItemSwapperEffect {
        private static final long serialVersionUID = 1L;

        private void stickyPoke(Battle b, ActivePokemon victim, String possession) {
            victim.reduceHealthFraction(b, 1/8.0, victim.getName() + " was hurt by " + possession + " " + this.getName() + "!");
        }

        StickyBarb() {
            super(ItemNamesies.STICKY_BARB, "An item to be held by a Pok\u00e9mon. It damages the holder every turn and may latch on to Pok\u00e9mon that touch the holder.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            stickyPoke(b, victim, "its");
        }

        @Override
        public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
            stickyPoke(b, user, victim.getName() + "'s");
            if (user.isFainted(b) || !victim.canGiftItem(b, user)) {
                return;
            }

            this.swapItems(b, victim, user);
        }

        @Override
        public int flingDamage() {
            return 80;
        }

        @Override
        public String getSwitchMessage(ActivePokemon user, HoldItem userItem, ActivePokemon victim, HoldItem victimItem) {
            return victim.getName() + "s " + this.getName() + " latched onto " + user.getName() + "!";
        }
    }

    static class ThickClub extends Item implements HoldItem, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        ThickClub() {
            super(ItemNamesies.THICK_CLUB, "An item to be held by Cubone or Marowak. It's a hard bone of some sort that boosts the Attack stat.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public int flingDamage() {
            return 90;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.ATTACK;
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return p.isPokemon(PokemonNamesies.CUBONE, PokemonNamesies.MAROWAK, PokemonNamesies.GARA_GARA);
        }

        @Override
        public double getModifier() {
            return 2;
        }
    }

    static class WeaknessPolicy extends Item implements HoldItem, TakeDamageEffect {
        private static final long serialVersionUID = 1L;

        WeaknessPolicy() {
            super(ItemNamesies.WEAKNESS_POLICY, "An item to be held by a Pok\u00e9mon. Attack and Sp. Atk sharply increase if the holder is hit with a move it's weak to.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public void onDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (TypeAdvantage.isSuperEffective(user, victim, b)) {
                new StageModifier(2, Stat.ATTACK, Stat.SP_ATTACK).modify(b, victim, victim, CastSource.HELD_ITEM);
            }
        }
    }

    static class WhiteHerb extends Item implements HoldItem, EndTurnEffect, StatLoweredEffect {
        private static final long serialVersionUID = 1L;

        // Restores negative stat changes to the victim
        private boolean usesies(ActivePokemon p) {
            boolean used = false;
            for (Stat stat : Stat.BATTLE_STATS) {
                if (p.getStage(stat) < 0) {
                    p.getStages().setStage(stat, 0);
                    used = true;
                }
            }
            return used;
        }

        WhiteHerb() {
            super(ItemNamesies.WHITE_HERB, "An item to be held by a Pok\u00e9mon. It will restore any lowered stat in battle. It can be used only once.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void flingEffect(Battle b, ActivePokemon pelted) {
            if (usesies(pelted)) {
                Messages.add("The " + this.getName() + " restored " + pelted.getName() + "'s negative stat changes!");
            }
        }

        @Override
        public void takeItToTheNextLevel(Battle b, ActivePokemon victim, boolean selfCaster) {
            if (usesies(victim)) {
                Messages.add(victim.getName() + "'s " + this.getName() + " restored its negative stat changes!");
                this.consumeItem(b, victim);
            }
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            if (usesies(victim)) {
                Messages.add(victim.getName() + "'s " + this.getName() + " restored its negative stat changes!");
                this.consumeItem(b, victim);
            }
        }

        @Override
        public int flingDamage() {
            return 10;
        }
    }

    static class WideLens extends Item implements HoldItem, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        WideLens() {
            super(ItemNamesies.WIDE_LENS, "An item to be held by a Pok\u00e9mon. It's a magnifying lens that slightly boosts the accuracy of moves.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public int flingDamage() {
            return 10;
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

    static class WiseGlasses extends Item implements HoldItem, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        WiseGlasses() {
            super(ItemNamesies.WISE_GLASSES, "An item to be held by a Pok\u00e9mon. This thick pair of glasses slightly boosts the power of special moves.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public int flingDamage() {
            return 10;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SP_ATTACK;
        }

        @Override
        public double getModifier() {
            return 1.1;
        }
    }

    static class ZoomLens extends Item implements HoldItem, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        ZoomLens() {
            super(ItemNamesies.ZOOM_LENS, "An item to be held by a Pok\u00e9mon. If the holder moves after its target moves, its accuracy will be boosted.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public int flingDamage() {
            return 10;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.ACCURACY;
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return !b.isFirstAttack();
        }

        @Override
        public double getModifier() {
            return 1.2;
        }
    }

    static class FullIncense extends Item implements StallingEffect, IncenseItem {
        private static final long serialVersionUID = 1L;

        FullIncense() {
            super(ItemNamesies.FULL_INCENSE, "An item to be held by a Pok\u00e9mon. This exotic-smelling incense makes the holder bloated and slow moving.", BagCategory.MISC);
            super.price = 5000;
        }

        @Override
        public PokemonNamesies getBaby() {
            return PokemonNamesies.MUNCHLAX;
        }
    }

    static class LaxIncense extends Item implements IncenseItem, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        LaxIncense() {
            super(ItemNamesies.LAX_INCENSE, "An item to be held by a Pok\u00e9mon. The beguiling aroma of this incense may cause attacks to miss its holder.", BagCategory.MISC);
            super.price = 5000;
        }

        @Override
        public PokemonNamesies getBaby() {
            return PokemonNamesies.WYNAUT;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.EVASION;
        }

        @Override
        public double getModifier() {
            return 1.1;
        }
    }

    static class LuckIncense extends Item implements IncenseItem, EntryEndTurnEffect {
        private static final long serialVersionUID = 1L;

        LuckIncense() {
            super(ItemNamesies.LUCK_INCENSE, "An item to be held by a Pok\u00e9mon. It doubles any prize money received if the holding Pok\u00e9mon joins a battle.", BagCategory.MISC);
            super.price = 5000;
        }

        private void getDatCashMoneyGetDatCashMoneyCast(Battle b, ActivePokemon gettinDatCashMoneyTwice) {
            Effect.cast(TeamEffectNamesies.GET_DAT_CASH_MONEY_TWICE, b, gettinDatCashMoneyTwice, gettinDatCashMoneyTwice, CastSource.HELD_ITEM, false);
        }

        @Override
        public PokemonNamesies getBaby() {
            return PokemonNamesies.HAPPINY;
        }

        @Override
        public void applyEffect(Battle b, ActivePokemon p) {
            // This is named too fantastically to just be applyEffect
            getDatCashMoneyGetDatCashMoneyCast(b, p);
        }
    }

    static class OddIncense extends Item implements HoldItem, IncenseItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        OddIncense() {
            super(ItemNamesies.ODD_INCENSE, "An item to be held by a Pok\u00e9mon. This exotic-smelling incense boosts the power of Psychic-type moves.", BagCategory.MISC);
            super.price = 5000;
        }

        @Override
        public PokemonNamesies getBaby() {
            return PokemonNamesies.MIME_JR;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.PSYCHIC) ? 1.2 : 1;
        }
    }

    static class PureIncense extends Item implements RepellingEffect, IncenseItem {
        private static final long serialVersionUID = 1L;

        PureIncense() {
            super(ItemNamesies.PURE_INCENSE, "An item to be held by a Pok\u00e9mon. It helps keep wild Pok\u00e9mon away if the holder is the head of the party.", BagCategory.MISC);
            super.price = 5000;
        }

        @Override
        public boolean shouldRepel(ActivePokemon playerFront, WildEncounter wildPokemon) {
            return RandomUtils.chanceTest(1, 3) && wildPokemon.getLevel() <= playerFront.getLevel();
        }

        @Override
        public PokemonNamesies getBaby() {
            return PokemonNamesies.CHINGLING;
        }
    }

    static class RockIncense extends Item implements HoldItem, IncenseItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        RockIncense() {
            super(ItemNamesies.ROCK_INCENSE, "An item to be held by a Pok\u00e9mon. This exotic-smelling incense boosts the power of Rock-type moves.", BagCategory.MISC);
            super.price = 5000;
        }

        @Override
        public PokemonNamesies getBaby() {
            return PokemonNamesies.BONSLY;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.ROCK) ? 1.2 : 1;
        }
    }

    static class RoseIncense extends Item implements HoldItem, IncenseItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        RoseIncense() {
            super(ItemNamesies.ROSE_INCENSE, "An item to be held by a Pok\u00e9mon. This exotic-smelling incense boosts the power of Grass-type moves.", BagCategory.MISC);
            super.price = 5000;
        }

        @Override
        public PokemonNamesies getBaby() {
            return PokemonNamesies.BUDEW;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.GRASS) ? 1.2 : 1;
        }
    }

    static class SeaIncense extends Item implements HoldItem, IncenseItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        SeaIncense() {
            super(ItemNamesies.SEA_INCENSE, "An item to be held by a Pok\u00e9mon. This incense has a curious aroma that boosts the power of Water-type moves.", BagCategory.MISC);
            super.price = 5000;
        }

        @Override
        public PokemonNamesies getBaby() {
            return PokemonNamesies.AZURILL;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.WATER) ? 1.2 : 1;
        }
    }

    static class WaveIncense extends Item implements HoldItem, IncenseItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        WaveIncense() {
            super(ItemNamesies.WAVE_INCENSE, "An item to be held by a Pok\u00e9mon. This incense has a curious aroma that boosts the power of Water-type moves.", BagCategory.MISC);
            super.price = 5000;
        }

        @Override
        public PokemonNamesies getBaby() {
            return PokemonNamesies.MANTYKE;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.WATER) ? 1.2 : 1;
        }
    }

    static class DracoPlate extends Item implements PlateItem {
        private static final long serialVersionUID = 1L;

        DracoPlate() {
            super(ItemNamesies.DRACO_PLATE, "An item to be held by a Pok\u00e9mon. It's a stone tablet that boosts the power of Dragon-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.DRAGON;
        }
    }

    static class DreadPlate extends Item implements PlateItem {
        private static final long serialVersionUID = 1L;

        DreadPlate() {
            super(ItemNamesies.DREAD_PLATE, "An item to be held by a Pok\u00e9mon. It's a stone tablet that boosts the power of Dark-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.DARK;
        }
    }

    static class EarthPlate extends Item implements PlateItem {
        private static final long serialVersionUID = 1L;

        EarthPlate() {
            super(ItemNamesies.EARTH_PLATE, "An item to be held by a Pok\u00e9mon. It's a stone tablet that boosts the power of Ground-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.GROUND;
        }
    }

    static class FistPlate extends Item implements PlateItem {
        private static final long serialVersionUID = 1L;

        FistPlate() {
            super(ItemNamesies.FIST_PLATE, "An item to be held by a Pok\u00e9mon. It's a stone tablet that boosts the power of Fighting-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.FIGHTING;
        }
    }

    static class FlamePlate extends Item implements PlateItem {
        private static final long serialVersionUID = 1L;

        FlamePlate() {
            super(ItemNamesies.FLAME_PLATE, "An item to be held by a Pok\u00e9mon. It's a stone tablet that boosts the power of Fire-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.FIRE;
        }
    }

    static class IciclePlate extends Item implements PlateItem {
        private static final long serialVersionUID = 1L;

        IciclePlate() {
            super(ItemNamesies.ICICLE_PLATE, "An item to be held by a Pok\u00e9mon. It's a stone tablet that boosts the power of Ice-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.ICE;
        }
    }

    static class InsectPlate extends Item implements PlateItem {
        private static final long serialVersionUID = 1L;

        InsectPlate() {
            super(ItemNamesies.INSECT_PLATE, "An item to be held by a Pok\u00e9mon. It's a stone tablet that boosts the power of Bug-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.BUG;
        }
    }

    static class IronPlate extends Item implements PlateItem {
        private static final long serialVersionUID = 1L;

        IronPlate() {
            super(ItemNamesies.IRON_PLATE, "An item to be held by a Pok\u00e9mon. It's a stone tablet that boosts the power of Steel-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.STEEL;
        }
    }

    static class MeadowPlate extends Item implements PlateItem {
        private static final long serialVersionUID = 1L;

        MeadowPlate() {
            super(ItemNamesies.MEADOW_PLATE, "An item to be held by a Pok\u00e9mon. It's a stone tablet that boosts the power of Grass-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.GRASS;
        }
    }

    static class MindPlate extends Item implements PlateItem {
        private static final long serialVersionUID = 1L;

        MindPlate() {
            super(ItemNamesies.MIND_PLATE, "An item to be held by a Pok\u00e9mon. It's a stone tablet that boosts the power of Psychic-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.PSYCHIC;
        }
    }

    static class PixiePlate extends Item implements PlateItem {
        private static final long serialVersionUID = 1L;

        PixiePlate() {
            super(ItemNamesies.PIXIE_PLATE, "An item to be held by a Pok\u00e9mon. It's a stone tablet that boosts the power of Fairy-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.FAIRY;
        }
    }

    static class SkyPlate extends Item implements PlateItem {
        private static final long serialVersionUID = 1L;

        SkyPlate() {
            super(ItemNamesies.SKY_PLATE, "An item to be held by a Pok\u00e9mon. It's a stone tablet that boosts the power of Flying-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.FLYING;
        }
    }

    static class SplashPlate extends Item implements PlateItem {
        private static final long serialVersionUID = 1L;

        SplashPlate() {
            super(ItemNamesies.SPLASH_PLATE, "An item to be held by a Pok\u00e9mon. It's a stone tablet that boosts the power of Water-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.WATER;
        }
    }

    static class SpookyPlate extends Item implements PlateItem {
        private static final long serialVersionUID = 1L;

        SpookyPlate() {
            super(ItemNamesies.SPOOKY_PLATE, "An item to be held by a Pok\u00e9mon. It's a stone tablet that boosts the power of Ghost-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.GHOST;
        }
    }

    static class StonePlate extends Item implements PlateItem {
        private static final long serialVersionUID = 1L;

        StonePlate() {
            super(ItemNamesies.STONE_PLATE, "An item to be held by a Pok\u00e9mon. It's a stone tablet that boosts the power of Rock-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.ROCK;
        }
    }

    static class ToxicPlate extends Item implements PlateItem {
        private static final long serialVersionUID = 1L;

        ToxicPlate() {
            super(ItemNamesies.TOXIC_PLATE, "An item to be held by a Pok\u00e9mon. It's a stone tablet that boosts the power of Poison-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.POISON;
        }
    }

    static class ZapPlate extends Item implements PlateItem {
        private static final long serialVersionUID = 1L;

        ZapPlate() {
            super(ItemNamesies.ZAP_PLATE, "An item to be held by a Pok\u00e9mon. It's a stone tablet that boosts the power of Electric-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.ELECTRIC;
        }
    }

    static class BurnDrive extends Item implements DriveItem {
        private static final long serialVersionUID = 1L;

        BurnDrive() {
            super(ItemNamesies.BURN_DRIVE, "A cassette to be held by Genesect. It changes Genesect's Techno Blast move so it becomes Fire type.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.FIRE;
        }
    }

    static class ChillDrive extends Item implements DriveItem {
        private static final long serialVersionUID = 1L;

        ChillDrive() {
            super(ItemNamesies.CHILL_DRIVE, "A cassette to be held by Genesect. It changes Genesect's Techno Blast move so it becomes Ice type.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.ICE;
        }
    }

    static class DouseDrive extends Item implements DriveItem {
        private static final long serialVersionUID = 1L;

        DouseDrive() {
            super(ItemNamesies.DOUSE_DRIVE, "A cassette to be held by Genesect. It changes Genesect's Techno Blast move so it becomes Water type.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.WATER;
        }
    }

    static class ShockDrive extends Item implements DriveItem {
        private static final long serialVersionUID = 1L;

        ShockDrive() {
            super(ItemNamesies.SHOCK_DRIVE, "A cassette to be held by Genesect. It changes Genesect's Techno Blast move so it becomes Electric type.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.ELECTRIC;
        }
    }

    static class FireGem extends Item implements GemItem {
        private static final long serialVersionUID = 1L;

        FireGem() {
            super(ItemNamesies.FIRE_GEM, "A gem with an essence of fire. When held, it strengthens the power of a Fire-type move one time.", BagCategory.MISC);
            super.price = 200;
        }

        @Override
        public Type getType() {
            return Type.FIRE;
        }
    }

    static class WaterGem extends Item implements GemItem {
        private static final long serialVersionUID = 1L;

        WaterGem() {
            super(ItemNamesies.WATER_GEM, "A gem with an essence of water. When held, it strengthens the power of a Water-type move one time.", BagCategory.MISC);
            super.price = 200;
        }

        @Override
        public Type getType() {
            return Type.WATER;
        }
    }

    static class ElectricGem extends Item implements GemItem {
        private static final long serialVersionUID = 1L;

        ElectricGem() {
            super(ItemNamesies.ELECTRIC_GEM, "A gem with an essence of electricity. When held, it strengthens the power of an Electric-type move one time.", BagCategory.MISC);
            super.price = 200;
        }

        @Override
        public Type getType() {
            return Type.ELECTRIC;
        }
    }

    static class GrassGem extends Item implements GemItem {
        private static final long serialVersionUID = 1L;

        GrassGem() {
            super(ItemNamesies.GRASS_GEM, "A gem with an essence of nature. When held, it strengthens the power of a Grass-type move one time.", BagCategory.MISC);
            super.price = 200;
        }

        @Override
        public Type getType() {
            return Type.GRASS;
        }
    }

    static class IceGem extends Item implements GemItem {
        private static final long serialVersionUID = 1L;

        IceGem() {
            super(ItemNamesies.ICE_GEM, "A gem with an essence of ice. When held, it strengthens the power of an Ice-type move one time.", BagCategory.MISC);
            super.price = 200;
        }

        @Override
        public Type getType() {
            return Type.ICE;
        }
    }

    static class FightingGem extends Item implements GemItem {
        private static final long serialVersionUID = 1L;

        FightingGem() {
            super(ItemNamesies.FIGHTING_GEM, "A gem with an essence of combat. When held, it strengthens the power of a Fighting-type move one time.", BagCategory.MISC);
            super.price = 200;
        }

        @Override
        public Type getType() {
            return Type.FIGHTING;
        }
    }

    static class PoisonGem extends Item implements GemItem {
        private static final long serialVersionUID = 1L;

        PoisonGem() {
            super(ItemNamesies.POISON_GEM, "A gem with an essence of poison. When held, it strengthens the power of a Poison-type move one time.", BagCategory.MISC);
            super.price = 200;
        }

        @Override
        public Type getType() {
            return Type.POISON;
        }
    }

    static class GroundGem extends Item implements GemItem {
        private static final long serialVersionUID = 1L;

        GroundGem() {
            super(ItemNamesies.GROUND_GEM, "A gem with an essence of land. When held, it strengthens the power of a Ground-type move one time.", BagCategory.MISC);
            super.price = 200;
        }

        @Override
        public Type getType() {
            return Type.GROUND;
        }
    }

    static class FlyingGem extends Item implements GemItem {
        private static final long serialVersionUID = 1L;

        FlyingGem() {
            super(ItemNamesies.FLYING_GEM, "A gem with an essence of air. When held, it strengthens the power of a Flying-type move one time.", BagCategory.MISC);
            super.price = 200;
        }

        @Override
        public Type getType() {
            return Type.FLYING;
        }
    }

    static class PsychicGem extends Item implements GemItem {
        private static final long serialVersionUID = 1L;

        PsychicGem() {
            super(ItemNamesies.PSYCHIC_GEM, "A gem with an essence of the mind. When held, it strengthens the power of a Psychic-type move one time.", BagCategory.MISC);
            super.price = 200;
        }

        @Override
        public Type getType() {
            return Type.PSYCHIC;
        }
    }

    static class BugGem extends Item implements GemItem {
        private static final long serialVersionUID = 1L;

        BugGem() {
            super(ItemNamesies.BUG_GEM, "A gem with an insect-like essence. When held, it strengthens the power of a Bug-type move one time.", BagCategory.MISC);
            super.price = 200;
        }

        @Override
        public Type getType() {
            return Type.BUG;
        }
    }

    static class RockGem extends Item implements GemItem {
        private static final long serialVersionUID = 1L;

        RockGem() {
            super(ItemNamesies.ROCK_GEM, "A gem with an essence of rock. When held, it strengthens the power of a Rock-type move one time.", BagCategory.MISC);
            super.price = 200;
        }

        @Override
        public Type getType() {
            return Type.ROCK;
        }
    }

    static class GhostGem extends Item implements GemItem {
        private static final long serialVersionUID = 1L;

        GhostGem() {
            super(ItemNamesies.GHOST_GEM, "A gem with a spectral essence. When held, it strengthens the power of a Ghost-type move one time.", BagCategory.MISC);
            super.price = 200;
        }

        @Override
        public Type getType() {
            return Type.GHOST;
        }
    }

    static class DragonGem extends Item implements GemItem {
        private static final long serialVersionUID = 1L;

        DragonGem() {
            super(ItemNamesies.DRAGON_GEM, "A gem with a draconic essence. When held, it strengthens the power of a Dragon-type move one time.", BagCategory.MISC);
            super.price = 200;
        }

        @Override
        public Type getType() {
            return Type.DRAGON;
        }
    }

    static class DarkGem extends Item implements GemItem {
        private static final long serialVersionUID = 1L;

        DarkGem() {
            super(ItemNamesies.DARK_GEM, "A gem with an essence of darkness. When held, it strengthens the power of a Dark-type move one time.", BagCategory.MISC);
            super.price = 200;
        }

        @Override
        public Type getType() {
            return Type.DARK;
        }
    }

    static class SteelGem extends Item implements GemItem {
        private static final long serialVersionUID = 1L;

        SteelGem() {
            super(ItemNamesies.STEEL_GEM, "A gem with an essence of steel. When held, it strengthens the power of a Steel-type move one time.", BagCategory.MISC);
            super.price = 200;
        }

        @Override
        public Type getType() {
            return Type.STEEL;
        }
    }

    static class NormalGem extends Item implements GemItem {
        private static final long serialVersionUID = 1L;

        NormalGem() {
            super(ItemNamesies.NORMAL_GEM, "A gem with an ordinary essence. When held, it strengthens the power of a Normal-type move one time.", BagCategory.MISC);
            super.price = 200;
        }

        @Override
        public Type getType() {
            return Type.NORMAL;
        }
    }

    static class FairyGem extends Item implements GemItem {
        private static final long serialVersionUID = 1L;

        FairyGem() {
            super(ItemNamesies.FAIRY_GEM, "A gem with an essence of the fey. When held, it strengthens the power of a Fairy-type move one time.", BagCategory.MISC);
            super.price = 200;
        }

        @Override
        public Type getType() {
            return Type.FAIRY;
        }
    }

    static class Leftovers extends Item implements HoldItem, EndTurnEffect {
        private static final long serialVersionUID = 1L;

        Leftovers() {
            super(ItemNamesies.LEFTOVERS, "An item to be held by a Pok\u00e9mon. The holder's HP is slowly but steadily restored throughout every battle.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            victim.healHealthFraction(1/16.0, b, victim.getName() + "'s HP was restored by its " + this.getName() + "!");
        }

        @Override
        public int flingDamage() {
            return 10;
        }
    }

    static class BlackBelt extends Item implements EvolutionItem, HoldItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        BlackBelt() {
            super(ItemNamesies.BLACK_BELT, "An item to be held by a Pok\u00e9mon. This belt helps the wearer to focus and boosts the power of Fighting-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.FIGHTING) ? 1.2 : 1;
        }
    }

    static class BlackGlasses extends Item implements HoldItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        BlackGlasses() {
            super(ItemNamesies.BLACK_GLASSES, "An item to be held by a Pokémon. It's a pair of shady-looking glasses that boost the power of Dark-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.DARK) ? 1.2 : 1;
        }
    }

    static class Charcoal extends Item implements HoldItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Charcoal() {
            super(ItemNamesies.CHARCOAL, "An item to be held by a Pok\u00e9mon. It's a combustible fuel that boosts the power of Fire-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.FIRE) ? 1.2 : 1;
        }
    }

    static class DragonFang extends Item implements EvolutionItem, HoldItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        DragonFang() {
            super(ItemNamesies.DRAGON_FANG, "An item to be held by a Pok\u00e9mon. This hard and sharp fang boosts the power of Dragon-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public int flingDamage() {
            return 70;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.DRAGON) ? 1.2 : 1;
        }
    }

    static class HardStone extends Item implements EvolutionItem, HoldItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        HardStone() {
            super(ItemNamesies.HARD_STONE, "An item to be held by a Pok\u00e9mon. It's a durable stone that boosts the power of Rock-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public int flingDamage() {
            return 100;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.ROCK) ? 1.2 : 1;
        }
    }

    static class Magnet extends Item implements HoldItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Magnet() {
            super(ItemNamesies.MAGNET, "An item to be held by a Pok\u00e9mon. It's a powerful magnet that boosts the power of Electric-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.ELECTRIC) ? 1.2 : 1;
        }
    }

    static class MetalCoat extends Item implements EvolutionItem, HoldItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        MetalCoat() {
            super(ItemNamesies.METAL_COAT, "An item to be held by a Pok\u00e9mon. It's a special metallic film that can boost the power of Steel-type moves.", BagCategory.MISC);
            super.price = 2000;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.STEEL) ? 1.2 : 1;
        }
    }

    static class MiracleSeed extends Item implements HoldItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        MiracleSeed() {
            super(ItemNamesies.MIRACLE_SEED, "An item to be held by a Pokémon. It's a seed imbued with life-force that boosts the power of Grass-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.GRASS) ? 1.2 : 1;
        }
    }

    static class MysticWater extends Item implements HoldItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        MysticWater() {
            super(ItemNamesies.MYSTIC_WATER, "An item to be held by a Pok\u00e9mon. This teardrop-shaped gem boosts the power of Water-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.WATER) ? 1.2 : 1;
        }
    }

    static class NeverMeltIce extends Item implements HoldItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        NeverMeltIce() {
            super(ItemNamesies.NEVER_MELT_ICE, "An item to be held by a Pokémon. It's a heat-repelling piece of ice that boosts the power of Ice-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.ICE) ? 1.2 : 1;
        }
    }

    static class PoisonBarb extends Item implements HoldItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        PoisonBarb() {
            super(ItemNamesies.POISON_BARB, "An item to be held by a Pok\u00e9mon. This small, poisonous barb boosts the power of Poison-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public int flingDamage() {
            return 70;
        }

        @Override
        public void flingEffect(Battle b, ActivePokemon pelted) {
            StatusNamesies.POISONED.getStatus().apply(b, pelted, pelted, pelted.getName() + " was poisoned by the " + this.getName() + "!");
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.POISON) ? 1.2 : 1;
        }
    }

    static class SharpBeak extends Item implements HoldItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        SharpBeak() {
            super(ItemNamesies.SHARP_BEAK, "An item to be held by a Pok\u00e9mon. It's a long, sharp beak that boosts the power of Flying-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public int flingDamage() {
            return 50;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.FLYING) ? 1.2 : 1;
        }
    }

    static class SilkScarf extends Item implements HoldItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        SilkScarf() {
            super(ItemNamesies.SILK_SCARF, "An item to be held by a Pok\u00e9mon. It's a sumptuous scarf that boosts the power of Normal-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public int flingDamage() {
            return 10;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.NORMAL) ? 1.2 : 1;
        }
    }

    static class SilverPowder extends Item implements HoldItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        SilverPowder() {
            super(ItemNamesies.SILVER_POWDER, "An item to be held by a Pokémon. It's a shiny silver powder that will boost the power of Bug-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public int flingDamage() {
            return 10;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.BUG) ? 1.2 : 1;
        }
    }

    static class SoftSand extends Item implements HoldItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        SoftSand() {
            super(ItemNamesies.SOFT_SAND, "An item to be held by a Pok\u00e9mon. It's a loose, silky sand that boosts the power of Ground-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public int flingDamage() {
            return 10;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.GROUND) ? 1.2 : 1;
        }
    }

    static class SpellTag extends Item implements EvolutionItem, HoldItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        SpellTag() {
            super(ItemNamesies.SPELL_TAG, "An item to be held by a Pok\u00e9mon. It's a sinister, eerie tag that boosts the power of Ghost-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.GHOST) ? 1.2 : 1;
        }
    }

    static class TwistedSpoon extends Item implements EvolutionItem, HoldItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        TwistedSpoon() {
            super(ItemNamesies.TWISTED_SPOON, "An item to be held by a Pok\u00e9mon. This spoon is imbued with telekinetic power and boosts Psychic-type moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.PSYCHIC) ? 1.2 : 1;
        }
    }

    static class DawnStone extends Item implements HoldItem, EvolutionItem {
        private static final long serialVersionUID = 1L;

        DawnStone() {
            super(ItemNamesies.DAWN_STONE, "A peculiar stone that can make certain species of Pok\u00e9mon evolve. It sparkles like a glittering eye.", BagCategory.MISC);
            super.price = 3000;
        }

        @Override
        public int flingDamage() {
            return 80;
        }
    }

    static class DeepSeaScale extends Item implements HoldItem, EvolutionItem, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        DeepSeaScale() {
            super(ItemNamesies.DEEP_SEA_SCALE, "An item to be held by Clamperl, Chinchou, or Lanturn. This scale shines with a faint pink and raises the holder's Sp. Def stat.", BagCategory.MISC);
            super.price = 2000;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SP_DEFENSE;
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return p.isPokemon(PokemonNamesies.CLAMPERL, PokemonNamesies.CHINCHOU, PokemonNamesies.LANTURN);
        }

        @Override
        public double getModifier() {
            return 2;
        }
    }

    static class DeepSeaTooth extends Item implements HoldItem, EvolutionItem, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        DeepSeaTooth() {
            super(ItemNamesies.DEEP_SEA_TOOTH, "An item to be held by Clamperl. This fang gleams a sharp silver and raises the holder's Sp. Atk stat.", BagCategory.MISC);
            super.price = 2000;
        }

        @Override
        public int flingDamage() {
            return 90;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SP_ATTACK;
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return p.isPokemon(PokemonNamesies.CLAMPERL);
        }

        @Override
        public double getModifier() {
            return 2;
        }
    }

    static class DragonScale extends Item implements HoldItem, EvolutionItem {
        private static final long serialVersionUID = 1L;

        DragonScale() {
            super(ItemNamesies.DRAGON_SCALE, "A very tough and inflexible scale. Dragon-type Pok\u00e9mon may be holding this item when caught.", BagCategory.MISC);
            super.price = 2000;
        }
    }

    static class DubiousDisc extends Item implements HoldItem, EvolutionItem {
        private static final long serialVersionUID = 1L;

        DubiousDisc() {
            super(ItemNamesies.DUBIOUS_DISC, "A transparent device overflowing with dubious data. Its producer is unknown.", BagCategory.MISC);
            super.price = 2000;
        }

        @Override
        public int flingDamage() {
            return 50;
        }
    }

    static class DuskStone extends Item implements HoldItem, EvolutionItem {
        private static final long serialVersionUID = 1L;

        DuskStone() {
            super(ItemNamesies.DUSK_STONE, "A peculiar stone that can make certain species of Pok\u00e9mon evolve. It holds shadows as dark as can be.", BagCategory.MISC);
            super.price = 3000;
        }

        @Override
        public int flingDamage() {
            return 80;
        }
    }

    static class Electirizer extends Item implements HoldItem, EvolutionItem {
        private static final long serialVersionUID = 1L;

        Electirizer() {
            super(ItemNamesies.ELECTIRIZER, "A box packed with a tremendous amount of electric energy. It's loved by a certain Pok\u00e9mon.", BagCategory.MISC);
            super.price = 2000;
        }

        @Override
        public int flingDamage() {
            return 80;
        }
    }

    static class FireStone extends Item implements HoldItem, EvolutionItem {
        private static final long serialVersionUID = 1L;

        FireStone() {
            super(ItemNamesies.FIRE_STONE, "A peculiar stone that can make certain species of Pok\u00e9mon evolve. The stone has a fiery orange heart.", BagCategory.MISC);
            super.price = 3000;
        }
    }

    static class KingsRock extends Item implements EvolutionItem, OpponentTakeDamageEffect, HoldItem {
        private static final long serialVersionUID = 1L;

        KingsRock() {
            super(ItemNamesies.KINGS_ROCK, "An item to be held by a Pok\u00e9mon. When the holder successfully inflicts damage, the target may also flinch.", BagCategory.MISC);
            super.price = 5000;
        }

        @Override
        public void flingEffect(Battle b, ActivePokemon pelted) {
            String message = "The " + this.getName() + " caused " + pelted.getName() + " to flinch!";
            Effect.apply(PokemonEffectNamesies.FLINCH, b, pelted, pelted, CastSource.USE_ITEM, message);
        }

        @Override
        public void onDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (RandomUtils.chanceTest(10)) {
                String message = user.getName() + "'s " + this.getName() + " caused " + victim.getName() + " to flinch!";
                Effect.apply(PokemonEffectNamesies.FLINCH, b, user, victim, CastSource.HELD_ITEM, message);
            }
        }
    }

    static class LeafStone extends Item implements HoldItem, EvolutionItem {
        private static final long serialVersionUID = 1L;

        LeafStone() {
            super(ItemNamesies.LEAF_STONE, "A peculiar stone that can make certain species of Pok\u00e9mon evolve. It has an unmistakable leaf pattern.", BagCategory.MISC);
            super.price = 3000;
        }
    }

    static class Magmarizer extends Item implements HoldItem, EvolutionItem {
        private static final long serialVersionUID = 1L;

        Magmarizer() {
            super(ItemNamesies.MAGMARIZER, "A box packed with a tremendous amount of magma energy. It's loved by a certain Pok\u00e9mon.", BagCategory.MISC);
            super.price = 2000;
        }

        @Override
        public int flingDamage() {
            return 80;
        }
    }

    static class MoonStone extends Item implements HoldItem, EvolutionItem {
        private static final long serialVersionUID = 1L;

        MoonStone() {
            super(ItemNamesies.MOON_STONE, "A peculiar stone that can make certain species of Pok\u00e9mon evolve. It is as black as the night sky.", BagCategory.MISC);
            super.price = 3000;
        }
    }

    static class OvalStone extends Item implements HoldItem, EvolutionItem {
        private static final long serialVersionUID = 1L;

        OvalStone() {
            super(ItemNamesies.OVAL_STONE, "A peculiar stone that can make certain species of Pok\u00e9mon evolve. It's as round as a Pok\u00e9mon Egg.", BagCategory.MISC);
            super.price = 2000;
        }

        @Override
        public int flingDamage() {
            return 80;
        }
    }

    static class Everstone extends Item implements HoldItem {
        private static final long serialVersionUID = 1L;

        Everstone() {
            super(ItemNamesies.EVERSTONE, "An item to be held by a Pok\u00e9mon. A Pok\u00e9mon holding this peculiar stone is prevented from evolving.", BagCategory.MISC);
            super.price = 3000;
        }
    }

    static class PrismScale extends Item implements HoldItem, EvolutionItem {
        private static final long serialVersionUID = 1L;

        PrismScale() {
            super(ItemNamesies.PRISM_SCALE, "A mysterious scale that causes a certain Pok\u00e9mon to evolve. It shines in rainbow colors.", BagCategory.MISC);
            super.price = 2000;
        }
    }

    static class Protector extends Item implements HoldItem, EvolutionItem {
        private static final long serialVersionUID = 1L;

        Protector() {
            super(ItemNamesies.PROTECTOR, "A protective item of some sort. It is extremely stiff and heavy. It's loved by a certain Pok\u00e9mon.", BagCategory.MISC);
            super.price = 2000;
        }

        @Override
        public int flingDamage() {
            return 80;
        }
    }

    static class RazorClaw extends Item implements HoldItem, CritStageEffect, EvolutionItem {
        private static final long serialVersionUID = 1L;

        RazorClaw() {
            super(ItemNamesies.RAZOR_CLAW, "An item to be held by a Pok\u00e9mon. This sharply hooked claw increases the holder's critical-hit ratio.", BagCategory.MISC);
            super.price = 5000;
        }

        @Override
        public int flingDamage() {
            return 80;
        }
    }

    static class RazorFang extends Item implements EvolutionItem, OpponentTakeDamageEffect, HoldItem {
        private static final long serialVersionUID = 1L;

        RazorFang() {
            super(ItemNamesies.RAZOR_FANG, "An item to be held by a Pok\u00e9mon. When the holder successfully inflicts damage, the target may also flinch.", BagCategory.MISC);
            super.price = 5000;
        }

        @Override
        public void flingEffect(Battle b, ActivePokemon pelted) {
            String message = "The " + this.getName() + " caused " + pelted.getName() + " to flinch!";
            Effect.apply(PokemonEffectNamesies.FLINCH, b, pelted, pelted, CastSource.USE_ITEM, message);
        }

        @Override
        public void onDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (RandomUtils.chanceTest(10)) {
                String message = user.getName() + "'s " + this.getName() + " caused " + victim.getName() + " to flinch!";
                Effect.apply(PokemonEffectNamesies.FLINCH, b, user, victim, CastSource.HELD_ITEM, message);
            }
        }
    }

    static class ReaperCloth extends Item implements HoldItem, EvolutionItem {
        private static final long serialVersionUID = 1L;

        ReaperCloth() {
            super(ItemNamesies.REAPER_CLOTH, "A cloth imbued with horrifyingly strong spiritual energy. It's loved by a certain Pok\u00e9mon.", BagCategory.MISC);
            super.price = 2000;
        }

        @Override
        public int flingDamage() {
            return 10;
        }
    }

    static class Sachet extends Item implements HoldItem, EvolutionItem {
        private static final long serialVersionUID = 1L;

        Sachet() {
            super(ItemNamesies.SACHET, "A sachet filled with fragrant perfumes that are just slightly too overwhelming. Yet it's loved by a certain Pok\u00e9mon.", BagCategory.MISC);
            super.price = 2000;
        }
    }

    static class ShinyStone extends Item implements HoldItem, EvolutionItem {
        private static final long serialVersionUID = 1L;

        ShinyStone() {
            super(ItemNamesies.SHINY_STONE, "A peculiar stone that can make certain species of Pok\u00e9mon evolve. It shines with a dazzling light.", BagCategory.MISC);
            super.price = 3000;
        }

        @Override
        public int flingDamage() {
            return 80;
        }
    }

    static class SunStone extends Item implements HoldItem, EvolutionItem {
        private static final long serialVersionUID = 1L;

        SunStone() {
            super(ItemNamesies.SUN_STONE, "A peculiar stone that can make certain species of Pok\u00e9mon evolve. It burns as red as the evening sun.", BagCategory.MISC);
            super.price = 3000;
        }
    }

    static class ThunderStone extends Item implements HoldItem, EvolutionItem {
        private static final long serialVersionUID = 1L;

        ThunderStone() {
            super(ItemNamesies.THUNDER_STONE, "A peculiar stone that can make certain species of Pok\u00e9mon evolve. It has a distinct thunderbolt pattern.", BagCategory.MISC);
            super.price = 3000;
        }
    }

    static class IceStone extends Item implements HoldItem, EvolutionItem {
        private static final long serialVersionUID = 1L;

        IceStone() {
            super(ItemNamesies.ICE_STONE, "A peculiar stone that can make certain species of Pok\u00e9mon evolve. It has an unmistakable snowflake pattern.", BagCategory.MISC);
            super.price = 3000;
        }
    }

    static class UpGrade extends Item implements HoldItem, EvolutionItem {
        private static final long serialVersionUID = 1L;

        UpGrade() {
            super(ItemNamesies.UP_GRADE, "A transparent device somehow filled with all sorts of data. It was produced by Silph Co.", BagCategory.MISC);
            super.price = 2000;
        }
    }

    static class PrettyWing extends Item implements HoldItem, EvolutionItem {
        private static final long serialVersionUID = 1L;

        PrettyWing() {
            super(ItemNamesies.PRETTY_WING, "Though this feather is beautiful, it's just a regular feather and has no effect.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public int flingDamage() {
            return 20;
        }
    }

    static class Ruby extends Item implements HoldItem, EvolutionItem {
        private static final long serialVersionUID = 1L;

        Ruby() {
            super(ItemNamesies.RUBY, "A peculiar jewel that makes certain species of Pok\u00e9mon evolve.", BagCategory.MISC);
            super.price = 2000;
        }
    }

    static class WaterStone extends Item implements HoldItem, EvolutionItem {
        private static final long serialVersionUID = 1L;

        WaterStone() {
            super(ItemNamesies.WATER_STONE, "A peculiar stone that can make certain species of Pok\u00e9mon evolve. It is the blue of a pool of clear water.", BagCategory.MISC);
            super.price = 3000;
        }
    }

    static class WhippedDream extends Item implements HoldItem, EvolutionItem {
        private static final long serialVersionUID = 1L;

        WhippedDream() {
            super(ItemNamesies.WHIPPED_DREAM, "A soft and sweet treat made of fluffy, puffy, whipped, and whirled cream. It's loved by a certain Pok\u00e9mon.", BagCategory.MISC);
            super.price = 2000;
        }
    }

    static class Antidote extends Item implements HoldItem, StatusHealer {
        private static final long serialVersionUID = 1L;

        Antidote() {
            super(ItemNamesies.ANTIDOTE, "A spray-type medicine for treating poisoning. It can be used to lift the effects of being poisoned from a single Pokémon.", BagCategory.MEDICINE);
            super.price = 200;
            super.battleBagCategories.add(BattleBagCategory.STATUS);
        }

        @Override
        public boolean shouldHeal(ActivePokemon p) {
            return p.hasStatus(StatusNamesies.POISONED);
        }
    }

    static class Awakening extends Item implements HoldItem, StatusHealer {
        private static final long serialVersionUID = 1L;

        Awakening() {
            super(ItemNamesies.AWAKENING, "A spray-type medicine to wake the sleeping. It can be used to rouse a single Pokémon from the clutches of sleep.", BagCategory.MEDICINE);
            super.price = 100;
            super.battleBagCategories.add(BattleBagCategory.STATUS);
        }

        @Override
        public boolean shouldHeal(ActivePokemon p) {
            return p.hasStatus(StatusNamesies.ASLEEP);
        }
    }

    static class BurnHeal extends Item implements HoldItem, StatusHealer {
        private static final long serialVersionUID = 1L;

        BurnHeal() {
            super(ItemNamesies.BURN_HEAL, "A spray-type medicine for treating burns. It can be used to heal a single Pokémon suffering from a burn.", BagCategory.MEDICINE);
            super.price = 300;
            super.battleBagCategories.add(BattleBagCategory.STATUS);
        }

        @Override
        public boolean shouldHeal(ActivePokemon p) {
            return p.hasStatus(StatusNamesies.BURNED);
        }
    }

    static class IceHeal extends Item implements HoldItem, StatusHealer {
        private static final long serialVersionUID = 1L;

        IceHeal() {
            super(ItemNamesies.ICE_HEAL, "A spray-type medicine for treating freezing. It can be used to thaw out a single Pokémon that has been frozen solid.", BagCategory.MEDICINE);
            super.price = 100;
            super.battleBagCategories.add(BattleBagCategory.STATUS);
        }

        @Override
        public boolean shouldHeal(ActivePokemon p) {
            return p.hasStatus(StatusNamesies.FROZEN);
        }
    }

    static class ParalyzeHeal extends Item implements HoldItem, StatusHealer {
        private static final long serialVersionUID = 1L;

        ParalyzeHeal() {
            super(ItemNamesies.PARALYZE_HEAL, "A spray-type medicine for treating paralysis. It can be used to free a single Pokémon that has been paralyzed.", BagCategory.MEDICINE);
            super.price = 300;
            super.battleBagCategories.add(BattleBagCategory.STATUS);
        }

        @Override
        public boolean shouldHeal(ActivePokemon p) {
            return p.hasStatus(StatusNamesies.PARALYZED);
        }
    }

    static class FullHeal extends Item implements HoldItem, StatusHealer {
        private static final long serialVersionUID = 1L;

        FullHeal() {
            super(ItemNamesies.FULL_HEAL, "A spray-type medicine that is broadly effective. It can be used to heal all the status conditions of a single Pokémon.", BagCategory.MEDICINE);
            super.price = 400;
            super.battleBagCategories.add(BattleBagCategory.STATUS);
        }

        @Override
        public boolean shouldHeal(ActivePokemon p) {
            // Can't heal what you don't have
            return p.hasStatus();
        }
    }

    static class FullRestore extends Item implements BattlePokemonUseItem, HoldItem {
        private static final long serialVersionUID = 1L;

        FullRestore() {
            super(ItemNamesies.FULL_RESTORE, "A medicine that can be used to fully restore the HP of a single Pok\u00e9mon and heal any status conditions it has.", BagCategory.MEDICINE);
            super.price = 3000;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
            super.battleBagCategories.add(BattleBagCategory.STATUS);
        }

        @Override
        public boolean use(ActivePokemon p, Battle b) {
            // Essentially Full Restore is just a combined Max Potion and Full Heal
            boolean maxPotion = new MaxPotion().use(p, b);
            boolean fullHeal = new FullHeal().use(p, b);
            return maxPotion || fullHeal;
        }
    }

    static class Elixir extends Item implements AllPPHealer, HoldItem {
        private static final long serialVersionUID = 1L;

        Elixir() {
            super(ItemNamesies.ELIXIR, "This medicine can be used to restore 10 PP to each of the moves that have been learned by a Pokémon.", BagCategory.MEDICINE);
            super.price = 3000;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public int restoreAmount(Move toRestore) {
            return 10;
        }
    }

    static class MaxElixir extends Item implements AllPPHealer, HoldItem {
        private static final long serialVersionUID = 1L;

        MaxElixir() {
            super(ItemNamesies.MAX_ELIXIR, "This medicine can be used to fully restore the PP of all of the moves that have been learned by a Pokémon.", BagCategory.MEDICINE);
            super.price = 4500;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public int restoreAmount(Move toRestore) {
            return toRestore.getMaxPP();
        }
    }

    // TODO: These currently cannot be used in battle :(
    static class Ether extends Item implements HoldItem, PPHealer {
        private static final long serialVersionUID = 1L;

        Ether() {
            super(ItemNamesies.ETHER, "This medicine can be used to restore 10 PP to a single selected move that has been learned by a Pokémon.", BagCategory.MEDICINE);
            super.price = 1200;
        }

        @Override
        public int restoreAmount(ActivePokemon restorer, Move toRestore) {
            return 10;
        }
    }

    // TODO: These currently cannot be used in battle :(
    static class MaxEther extends Item implements HoldItem, PPHealer {
        private static final long serialVersionUID = 1L;

        MaxEther() {
            super(ItemNamesies.MAX_ETHER, "This medicine can be used to fully restore the PP of a single selected move that has been learned by a Pokémon.", BagCategory.MEDICINE);
            super.price = 2000;
        }

        @Override
        public int restoreAmount(ActivePokemon restorer, Move toRestore) {
            return toRestore.getMaxPP();
        }
    }

    static class BerryJuice extends Item implements HpHealer {
        private static final long serialVersionUID = 1L;

        BerryJuice() {
            super(ItemNamesies.BERRY_JUICE, "A 100 percent pure juice made of Berries. It can be used to restore 20 HP to a single Pokémon.", BagCategory.MEDICINE);
            super.price = 100;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public int getHealAmount(ActivePokemon p) {
            return 20;
        }
    }

    static class SweetHeart extends Item implements HpHealer {
        private static final long serialVersionUID = 1L;

        SweetHeart() {
            super(ItemNamesies.SWEET_HEART, "A piece of cloyingly sweet chocolate. It can be used to restore 20 HP to a single Pokémon.", BagCategory.MEDICINE);
            super.price = 3000;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public int getHealAmount(ActivePokemon p) {
            return 20;
        }
    }

    static class Potion extends Item implements HpHealer {
        private static final long serialVersionUID = 1L;

        Potion() {
            super(ItemNamesies.POTION, "A spray-type medicine for treating wounds. It can be used to restore 20 HP to a single Pokémon.", BagCategory.MEDICINE);
            super.price = 200;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public int getHealAmount(ActivePokemon p) {
            return 20;
        }
    }

    static class EnergyPowder extends Item implements HpHealer {
        private static final long serialVersionUID = 1L;

        EnergyPowder() {
            super(ItemNamesies.ENERGY_POWDER, "A very bitter medicinal powder. It can be used to restore 60 HP to a single Pokémon.", BagCategory.MEDICINE);
            super.price = 500;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public int getHealAmount(ActivePokemon p) {
            return 60;
        }
    }

    static class FreshWater extends Item implements HpHealer {
        private static final long serialVersionUID = 1L;

        FreshWater() {
            super(ItemNamesies.FRESH_WATER, "Water with high mineral content. It can be used to restore 30 HP to a single Pokémon.", BagCategory.MEDICINE);
            super.price = 200;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public int getHealAmount(ActivePokemon p) {
            return 30;
        }
    }

    static class SuperPotion extends Item implements HpHealer {
        private static final long serialVersionUID = 1L;

        SuperPotion() {
            super(ItemNamesies.SUPER_POTION, "A spray-type medicine for treating wounds. It can be used to restore 60 HP to a single Pokémon.", BagCategory.MEDICINE);
            super.price = 700;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public int getHealAmount(ActivePokemon p) {
            return 60;
        }
    }

    static class SodaPop extends Item implements HpHealer {
        private static final long serialVersionUID = 1L;

        SodaPop() {
            super(ItemNamesies.SODA_POP, "A highly carbonated soda drink. It can be used to restore 50 HP to a single Pokémon.", BagCategory.MEDICINE);
            super.price = 300;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public int getHealAmount(ActivePokemon p) {
            return 50;
        }
    }

    static class Lemonade extends Item implements HpHealer {
        private static final long serialVersionUID = 1L;

        Lemonade() {
            super(ItemNamesies.LEMONADE, "A very sweet and refreshing drink. It can be used to restore 70 HP to a single Pokémon.", BagCategory.MEDICINE);
            super.price = 350;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public int getHealAmount(ActivePokemon p) {
            return 70;
        }
    }

    static class MoomooMilk extends Item implements HpHealer {
        private static final long serialVersionUID = 1L;

        MoomooMilk() {
            super(ItemNamesies.MOOMOO_MILK, "A bottle of highly nutritious milk. It can be used to restore 100 HP to a single Pokémon.", BagCategory.MEDICINE);
            super.price = 600;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public int getHealAmount(ActivePokemon p) {
            return 100;
        }
    }

    static class EnergyRoot extends Item implements HpHealer {
        private static final long serialVersionUID = 1L;

        EnergyRoot() {
            super(ItemNamesies.ENERGY_ROOT, "An extremely bitter medicinal root. It can be used to restore 120 HP to a single Pokémon.", BagCategory.MEDICINE);
            super.price = 1200;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public int getHealAmount(ActivePokemon p) {
            return 120;
        }
    }

    static class HyperPotion extends Item implements HpHealer {
        private static final long serialVersionUID = 1L;

        HyperPotion() {
            super(ItemNamesies.HYPER_POTION, "A spray-type medicine for treating wounds. It can be used to restore 120 HP to a single Pokémon.", BagCategory.MEDICINE);
            super.price = 1500;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public int getHealAmount(ActivePokemon p) {
            return 120;
        }
    }

    static class MaxPotion extends Item implements HpHealer {
        private static final long serialVersionUID = 1L;

        MaxPotion() {
            super(ItemNamesies.MAX_POTION, "A spray-type medicine for treating wounds. It can be used to completely restore the max HP of a single Pokémon.", BagCategory.MEDICINE);
            super.price = 2500;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public int getHealAmount(ActivePokemon p) {
            return p.getMaxHP();
        }
    }

    static class Revive extends Item implements BattlePokemonUseItem, HoldItem {
        private static final long serialVersionUID = 1L;

        Revive() {
            super(ItemNamesies.REVIVE, "A medicine that can be used to revive a single Pokémon that has fainted. It also restores half of the Pokémon's max HP.", BagCategory.MEDICINE);
            super.price = 2000;
            super.battleBagCategories.add(BattleBagCategory.STATUS);
        }

        @Override
        public boolean use(ActivePokemon p, Battle b) {
            // Only applies to the dead
            if (!p.isActuallyDead()) {
                return false;
            }

            p.removeStatus();
            p.healHealthFraction(.5);

            Messages.add(p.getName() + " was partially revived!");
            return true;
        }
    }

    static class MaxRevive extends Item implements BattlePokemonUseItem, HoldItem {
        private static final long serialVersionUID = 1L;

        MaxRevive() {
            super(ItemNamesies.MAX_REVIVE, "A medicine that can be used to revive a single Pokémon that has fainted. It also fully restores the Pokémon's max HP.", BagCategory.MEDICINE);
            super.price = 4000;
            super.battleBagCategories.add(BattleBagCategory.STATUS);
        }

        @Override
        public boolean use(ActivePokemon p, Battle b) {
            // Only applies to the dead
            if (!p.isActuallyDead()) {
                return false;
            }

            p.removeStatus();
            p.healHealthFraction(1);

            Messages.add(p.getName() + " was fully revived!");
            return true;
        }
    }

    static class RevivalHerb extends Item implements BattlePokemonUseItem, HoldItem {
        private static final long serialVersionUID = 1L;

        RevivalHerb() {
            super(ItemNamesies.REVIVAL_HERB, "A terribly bitter medicinal herb. It revives a fainted Pokémon and fully restores its max HP.", BagCategory.MEDICINE);
            super.price = 2800;
            super.battleBagCategories.add(BattleBagCategory.STATUS);
        }

        @Override
        public boolean use(ActivePokemon p, Battle b) {
            // Only applies to the dead
            if (!p.isActuallyDead()) {
                return false;
            }

            p.removeStatus();
            p.healHealthFraction(1);

            Messages.add(p.getName() + " was fully revived!");
            return true;
        }
    }

    static class SacredAsh extends Item implements PlayerUseItem, BattleUseItem, HoldItem {
        private static final long serialVersionUID = 1L;

        private boolean use(Trainer t) {
            boolean healed = false;
            for (ActivePokemon p : t.getActiveTeam()) {
                if (p.isActuallyDead()) {
                    healed = true;
                    p.removeStatus();
                    p.healHealthFraction(1);
                }
            }

            if (healed) {
                Messages.add("All fainted Pok\u00e9mon were fully revived!");
            }

            return healed;
        }

        SacredAsh() {
            super(ItemNamesies.SACRED_ASH, "This rare ash can revive all fainted Pokémon in a party. In doing so, it also fully restores their max HP.", BagCategory.MEDICINE);
            super.price = 50000;
            super.battleBagCategories.add(BattleBagCategory.STATUS);
        }

        @Override
        public boolean use() {
            return this.use(Game.getPlayer());
        }

        @Override
        public boolean use(ActivePokemon p, Battle b) {
            return use((Trainer)b.getTrainer(p));
        }

        @Override
        public boolean use(Battle b, ActivePokemon p, Move m) {
            return b == null ? use() : use(p, b);
        }
    }

    static class DireHit extends Item implements BattleUseItem, HoldItem {
        private static final long serialVersionUID = 1L;

        DireHit() {
            super(ItemNamesies.DIRE_HIT, "An item that greatly raises the critical-hit ratio of a Pokémon during a battle. It can be used only once and wears off if the Pokémon is withdrawn.", BagCategory.STAT);
            super.price = 650;
            super.battleBagCategories.add(BattleBagCategory.BATTLE);
        }

        @Override
        public boolean use(ActivePokemon p, Battle b) {
            return Effect.apply(PokemonEffectNamesies.RAISE_CRITS, b, p, p, CastSource.USE_ITEM, true).isSuccess();
        }
    }

    static class GuardSpec extends Item implements BattleUseItem, HoldItem {
        private static final long serialVersionUID = 1L;

        GuardSpec() {
            super(ItemNamesies.GUARD_SPEC, "An item that prevents stat reduction among the Trainer's party Pok\u00e9mon for five turns after it is used in battle.", BagCategory.STAT);
            super.price = 700;
            super.battleBagCategories.add(BattleBagCategory.BATTLE);
        }

        @Override
        public boolean use(ActivePokemon p, Battle b) {
            return Effect.apply(TeamEffectNamesies.GUARD_SPECIAL, b, p, p, CastSource.USE_ITEM, true).isSuccess();
        }
    }

    static class XAccuracy extends Item implements HoldItem, BattleUseItem {
        private static final long serialVersionUID = 1L;

        XAccuracy() {
            super(ItemNamesies.X_ACCURACY, "An item that sharply boosts the accuracy of a Pok\u00e9mon during a battle. It wears off once the Pok\u00e9mon is withdrawn.", BagCategory.STAT);
            super.price = 950;
            super.battleBagCategories.add(BattleBagCategory.BATTLE);
        }

        @Override
        public boolean use(ActivePokemon p, Battle b) {
            return new StageModifier(2, Stat.ACCURACY).modify(b, p, p, CastSource.USE_ITEM);
        }
    }

    static class XAttack extends Item implements HoldItem, BattleUseItem {
        private static final long serialVersionUID = 1L;

        XAttack() {
            super(ItemNamesies.X_ATTACK, "An item that sharply boosts the Attack stat of a Pok\u00e9mon during a battle. It wears off once the Pok\u00e9mon is withdrawn.", BagCategory.STAT);
            super.price = 500;
            super.battleBagCategories.add(BattleBagCategory.BATTLE);
        }

        @Override
        public boolean use(ActivePokemon p, Battle b) {
            return new StageModifier(2, Stat.ATTACK).modify(b, p, p, CastSource.USE_ITEM);
        }
    }

    static class XDefense extends Item implements HoldItem, BattleUseItem {
        private static final long serialVersionUID = 1L;

        XDefense() {
            super(ItemNamesies.X_DEFENSE, "An item that sharply boosts the Defense stat of a Pokémon during a battle. It wears off once the Pokémon is withdrawn.", BagCategory.STAT);
            super.price = 550;
            super.battleBagCategories.add(BattleBagCategory.BATTLE);
        }

        @Override
        public boolean use(ActivePokemon p, Battle b) {
            return new StageModifier(2, Stat.DEFENSE).modify(b, p, p, CastSource.USE_ITEM);
        }
    }

    static class XSpAtk extends Item implements HoldItem, BattleUseItem {
        private static final long serialVersionUID = 1L;

        XSpAtk() {
            super(ItemNamesies.X_SP_ATK, "An item that sharply boosts the Sp. Atk stat of a Pokémon during a battle. It wears off once the Pokémon is withdrawn.", BagCategory.STAT);
            super.price = 350;
            super.battleBagCategories.add(BattleBagCategory.BATTLE);
        }

        @Override
        public boolean use(ActivePokemon p, Battle b) {
            return new StageModifier(2, Stat.SP_ATTACK).modify(b, p, p, CastSource.USE_ITEM);
        }
    }

    static class XSpDef extends Item implements HoldItem, BattleUseItem {
        private static final long serialVersionUID = 1L;

        XSpDef() {
            super(ItemNamesies.X_SP_DEF, "An item that sharply boosts the Sp. Def stat of a Pok\u00e9mon during a battle. It wears off once the Pok\u00e9mon is withdrawn.", BagCategory.STAT);
            super.price = 350;
            super.battleBagCategories.add(BattleBagCategory.BATTLE);
        }

        @Override
        public boolean use(ActivePokemon p, Battle b) {
            return new StageModifier(2, Stat.SP_DEFENSE).modify(b, p, p, CastSource.USE_ITEM);
        }
    }

    static class XSpeed extends Item implements HoldItem, BattleUseItem {
        private static final long serialVersionUID = 1L;

        XSpeed() {
            super(ItemNamesies.X_SPEED, "An item that sharply boosts the Speed stat of a Pok\u00e9mon during a battle. It wears off once the Pok\u00e9mon is withdrawn.", BagCategory.STAT);
            super.price = 350;
            super.battleBagCategories.add(BattleBagCategory.BATTLE);
        }

        @Override
        public boolean use(ActivePokemon p, Battle b) {
            return new StageModifier(2, Stat.SPEED).modify(b, p, p, CastSource.USE_ITEM);
        }
    }

    static class HPUp extends Item implements Vitamin {
        private static final long serialVersionUID = 1L;

        HPUp() {
            super(ItemNamesies.HP_UP, "A nutritious drink for Pokémon. When consumed, it raises the HP base points of a single Pokémon.", BagCategory.STAT);
            super.price = 10000;
        }

        @Override
        public Stat toIncrease() {
            return Stat.HP;
        }
    }

    static class Protein extends Item implements Vitamin {
        private static final long serialVersionUID = 1L;

        Protein() {
            super(ItemNamesies.PROTEIN, "A nutritious drink for Pokémon. When consumed, it raises the Attack base points of a single Pokémon.", BagCategory.STAT);
            super.price = 10000;
        }

        @Override
        public Stat toIncrease() {
            return Stat.ATTACK;
        }
    }

    static class Iron extends Item implements Vitamin {
        private static final long serialVersionUID = 1L;

        Iron() {
            super(ItemNamesies.IRON, "A nutritious drink for Pokémon. When consumed, it raises the Defense base points of a single Pokémon.", BagCategory.STAT);
            super.price = 10000;
        }

        @Override
        public Stat toIncrease() {
            return Stat.DEFENSE;
        }
    }

    static class Calcium extends Item implements Vitamin {
        private static final long serialVersionUID = 1L;

        Calcium() {
            super(ItemNamesies.CALCIUM, "A nutritious drink for Pokémon. When consumed, it raises the Sp. Atk base points of a single Pokémon.", BagCategory.STAT);
            super.price = 10000;
        }

        @Override
        public Stat toIncrease() {
            return Stat.SP_ATTACK;
        }
    }

    static class Zinc extends Item implements Vitamin {
        private static final long serialVersionUID = 1L;

        Zinc() {
            super(ItemNamesies.ZINC, "A nutritious drink for Pokémon. When consumed, it raises the Sp. Def base points of a single Pokémon.", BagCategory.STAT);
            super.price = 10000;
        }

        @Override
        public Stat toIncrease() {
            return Stat.SP_DEFENSE;
        }
    }

    static class Carbos extends Item implements Vitamin {
        private static final long serialVersionUID = 1L;

        Carbos() {
            super(ItemNamesies.CARBOS, "A nutritious drink for Pokémon. When consumed, it raises the Speed base points of a single Pokémon.", BagCategory.STAT);
            super.price = 10000;
        }

        @Override
        public Stat toIncrease() {
            return Stat.SPEED;
        }
    }

    static class HealthWing extends Item implements Wing {
        private static final long serialVersionUID = 1L;

        HealthWing() {
            super(ItemNamesies.HEALTH_WING, "An item for use on a Pok\u00e9mon. It slightly increases the base HP of a single Pok\u00e9mon.", BagCategory.STAT);
            super.price = 3000;
        }

        @Override
        public Stat toIncrease() {
            return Stat.HP;
        }
    }

    static class MuscleWing extends Item implements Wing {
        private static final long serialVersionUID = 1L;

        MuscleWing() {
            super(ItemNamesies.MUSCLE_WING, "An item for use on a Pok\u00e9mon. It slightly increases the base Attack stat of a single Pok\u00e9mon.", BagCategory.STAT);
            super.price = 3000;
        }

        @Override
        public Stat toIncrease() {
            return Stat.ATTACK;
        }
    }

    static class ResistWing extends Item implements Wing {
        private static final long serialVersionUID = 1L;

        ResistWing() {
            super(ItemNamesies.RESIST_WING, "An item for use on a Pok\u00e9mon. It slightly increases the base Defense stat of a single Pok\u00e9mon.", BagCategory.STAT);
            super.price = 3000;
        }

        @Override
        public Stat toIncrease() {
            return Stat.DEFENSE;
        }
    }

    static class GeniusWing extends Item implements Wing {
        private static final long serialVersionUID = 1L;

        GeniusWing() {
            super(ItemNamesies.GENIUS_WING, "An item for use on a Pok\u00e9mon. It slightly increases the base Sp. Atk stat of a single Pok\u00e9mon.", BagCategory.STAT);
            super.price = 3000;
        }

        @Override
        public Stat toIncrease() {
            return Stat.SP_ATTACK;
        }
    }

    static class CleverWing extends Item implements Wing {
        private static final long serialVersionUID = 1L;

        CleverWing() {
            super(ItemNamesies.CLEVER_WING, "An item for use on a Pok\u00e9mon. It slightly increases the base Sp. Def stat of a single Pok\u00e9mon.", BagCategory.STAT);
            super.price = 3000;
        }

        @Override
        public Stat toIncrease() {
            return Stat.SP_DEFENSE;
        }
    }

    static class SwiftWing extends Item implements Wing {
        private static final long serialVersionUID = 1L;

        SwiftWing() {
            super(ItemNamesies.SWIFT_WING, "An item for use on a Pok\u00e9mon. It slightly increases the base Speed stat of a single Pok\u00e9mon.", BagCategory.STAT);
            super.price = 3000;
        }

        @Override
        public Stat toIncrease() {
            return Stat.SPEED;
        }
    }

    // I manually increased the price because it makes sense to be higher than PP Up
    static class PPMax extends Item implements MoveUseItem, HoldItem {
        private static final long serialVersionUID = 1L;

        PPMax() {
            super(ItemNamesies.PP_MAX, "A medicine that optimally raises the max PP of a single selected move that has been learned by a Pokémon.", BagCategory.STAT);
            super.price = 12000;
        }

        @Override
        public boolean use(ActivePokemon p, Move m) {
            if (m.increaseMaxPP(3)) {
                Messages.add(p.getName() + "'s " + m.getAttack().getName() + "'s Max PP was increased!");
                return true;
            }

            return false;
        }
    }

    static class PPUp extends Item implements MoveUseItem, HoldItem {
        private static final long serialVersionUID = 1L;

        PPUp() {
            super(ItemNamesies.PP_UP, "A medicine that slightly raises the max PP of a single selected move that has been learned by a Pokémon.", BagCategory.STAT);
            super.price = 10000;
        }

        @Override
        public boolean use(ActivePokemon p, Move m) {
            if (m.increaseMaxPP(1)) {
                Messages.add(p.getName() + "'s " + m.getAttack().getName() + "'s Max PP was increased!");
                return true;
            }

            return false;
        }
    }

    static class RareCandy extends Item implements HoldItem, PokemonUseItem {
        private static final long serialVersionUID = 1L;

        RareCandy() {
            super(ItemNamesies.RARE_CANDY, "A candy that is packed with energy. When consumed, it will instantly raise the level of a single Pok\u00e9mon by one.", BagCategory.STAT);
            super.price = 10000;
        }

        @Override
        public boolean use(ActivePokemon p) {
            return p.levelUp(null);
        }
    }

    static class CherishBall extends Item implements BallItem {
        private static final long serialVersionUID = 1L;

        CherishBall() {
            super(ItemNamesies.CHERISH_BALL, "A quite rare Pok\u00e9 Ball that has been crafted in order to commemorate a special occasion of some sort.", BagCategory.BALL);
            super.price = 1000;
            super.battleBagCategories.add(BattleBagCategory.BALL);
        }
    }

    static class DiveBall extends Item implements BallItem {
        private static final long serialVersionUID = 1L;

        DiveBall() {
            super(ItemNamesies.DIVE_BALL, "A somewhat different Pok\u00e9 Ball that works especially well when catching Pok\u00e9mon that live underwater.", BagCategory.BALL);
            super.price = 1000;
            super.battleBagCategories.add(BattleBagCategory.BALL);
        }

        @Override
        public double getModifier(ActivePokemon me, ActivePokemon o, Battle b) {
            return b.getTerrainType() == TerrainType.WATER ? 3.5 : 1;
        }
    }

    static class DuskBall extends Item implements BallItem {
        private static final long serialVersionUID = 1L;

        DuskBall() {
            super(ItemNamesies.DUSK_BALL, "A somewhat different Poké Ball that makes it easier to catch wild Pokémon at night or in dark places such as caves.", BagCategory.BALL);
            super.price = 1000;
            super.battleBagCategories.add(BattleBagCategory.BALL);
        }

        @Override
        public double getModifier(ActivePokemon me, ActivePokemon o, Battle b) {
            return b.getTerrainType() == TerrainType.CAVE ? 3.5 : 1;
        }
    }

    // If the opponent has a base speed of 100 or higher, multiplier is 4
    static class FastBall extends Item implements BallItem {
        private static final long serialVersionUID = 1L;

        FastBall() {
            super(ItemNamesies.FAST_BALL, "A Pok\u00e9 Ball that makes it easier to catch Pok\u00e9mon that are usually very quick to run away.", BagCategory.BALL);
            super.price = 300;
            super.battleBagCategories.add(BattleBagCategory.BALL);
        }

        @Override
        public double getModifier(ActivePokemon me, ActivePokemon o, Battle b) {
            return o.getPokemonInfo().getStats().get(Stat.SPEED) >= 100 ? 4 : 1;
        }
    }

    static class GreatBall extends Item implements BallItem {
        private static final long serialVersionUID = 1L;

        GreatBall() {
            super(ItemNamesies.GREAT_BALL, "A good, high-performance Poké Ball that provides a higher success rate for catching Pokémon than a standard Poké Ball.", BagCategory.BALL);
            super.price = 600;
            super.battleBagCategories.add(BattleBagCategory.BALL);
        }

        @Override
        public double getModifier(ActivePokemon me, ActivePokemon o, Battle b) {
            return 1.5;
        }
    }

    static class HealBall extends Item implements BallItem {
        private static final long serialVersionUID = 1L;

        HealBall() {
            super(ItemNamesies.HEAL_BALL, "A remedial Pok\u00e9 Ball that restores the HP of a Pok\u00e9mon caught with it and eliminates any status conditions.", BagCategory.BALL);
            super.price = 300;
            super.battleBagCategories.add(BattleBagCategory.BALL);
        }

        @Override
        public void afterCaught(ActivePokemon p) {
            p.fullyHeal();
        }
    }

    static class HeavyBall extends Item implements BallItem {
        private static final long serialVersionUID = 1L;

        HeavyBall() {
            super(ItemNamesies.HEAVY_BALL, "A Pok\u00e9 Ball that is better than usual at catching very heavy Pok\u00e9mon.", BagCategory.BALL);
            super.price = 300;
            super.battleBagCategories.add(BattleBagCategory.BALL);
        }

        @Override
        public int getAdditive(ActivePokemon me, ActivePokemon o, Battle b) {
            double weight = o.getWeight(b);
            if (weight < 220.5) {
                return -20;
            } else if (weight < 440.9) {
                return 0;
            } else if (weight < 661.4) {
                return 20;
            } else {
                return 30;
            }
        }
    }

    static class LevelBall extends Item implements BallItem {
        private static final long serialVersionUID = 1L;

        LevelBall() {
            super(ItemNamesies.LEVEL_BALL, "A Pok\u00e9 Ball that makes it easier to catch Pok\u00e9mon that are at a lower level than your own Pok\u00e9mon.", BagCategory.BALL);
            super.price = 300;
            super.battleBagCategories.add(BattleBagCategory.BALL);
        }

        @Override
        public double getModifier(ActivePokemon me, ActivePokemon o, Battle b) {
            if (me.getLevel()/4 > o.getLevel()) {
                return 8;
            } else if (me.getLevel()/2 > o.getLevel()) {
                return 4;
            } else if (me.getLevel() > o.getLevel()) {
                return 2;
            } else {
                return 1;
            }
        }
    }

    static class LoveBall extends Item implements BallItem {
        private static final long serialVersionUID = 1L;

        LoveBall() {
            super(ItemNamesies.LOVE_BALL, "A Pok\u00e9 Ball that works best when catching a Pok\u00e9mon that is of the opposite gender of your Pok\u00e9mon.", BagCategory.BALL);
            super.price = 300;
            super.battleBagCategories.add(BattleBagCategory.BALL);
        }

        @Override
        public double getModifier(ActivePokemon me, ActivePokemon o, Battle b) {
            return Gender.oppositeGenders(me, o) ? 8 : 1;
        }
    }

    static class LureBall extends Item implements BallItem {
        private static final long serialVersionUID = 1L;

        LureBall() {
            super(ItemNamesies.LURE_BALL, "A Poké Ball that is good for catching Pokémon that you reel in with a rod while out fishing.", BagCategory.BALL);
            super.price = 300;
            super.battleBagCategories.add(BattleBagCategory.BALL);
        }

        @Override
        public double getModifier(ActivePokemon me, ActivePokemon o, Battle b) {
            return Game.getPlayer().isFishing() ? 3 : 1;
        }
    }

    // TODO: Make this item do something more interesting
    static class LuxuryBall extends Item implements BallItem {
        private static final long serialVersionUID = 1L;

        LuxuryBall() {
            super(ItemNamesies.LUXURY_BALL, "A particularly comfortable Pok\u00e9 Ball that makes a wild Pok\u00e9mon quickly grow friendlier after being caught.", BagCategory.BALL);
            super.price = 1000;
            super.battleBagCategories.add(BattleBagCategory.BALL);
        }
    }

    static class MasterBall extends Item implements BallItem {
        private static final long serialVersionUID = 1L;

        MasterBall() {
            super(ItemNamesies.MASTER_BALL, "The best Pok\u00e9 Ball with the ultimate level of performance. With it, you will catch any wild Pok\u00e9mon without fail.", BagCategory.BALL);
            super.price = 0;
            super.battleBagCategories.add(BattleBagCategory.BALL);
        }

        @Override
        public double getModifier(ActivePokemon me, ActivePokemon o, Battle b) {
            return 255;
        }
    }

    static class MoonBall extends Item implements BallItem {
        private static final long serialVersionUID = 1L;

        MoonBall() {
            super(ItemNamesies.MOON_BALL, "A Pok\u00e9 Ball that will make it easier to catch Pok\u00e9mon that can evolve using a Moon Stone.", BagCategory.BALL);
            super.price = 300;
            super.battleBagCategories.add(BattleBagCategory.BALL);
        }

        @Override
        public double getModifier(ActivePokemon me, ActivePokemon o, Battle b) {
            return o.getPokemonInfo().getEvolution().getEvolution(EvolutionMethod.ITEM, o, ItemNamesies.MOON_STONE) != null ? 4 : 1;
        }
    }

    static class NestBall extends Item implements BallItem {
        private static final long serialVersionUID = 1L;

        NestBall() {
            super(ItemNamesies.NEST_BALL, "A somewhat different Pok\u00e9 Ball that becomes more effective the lower the level of the wild Pok\u00e9mon.", BagCategory.BALL);
            super.price = 1000;
            super.battleBagCategories.add(BattleBagCategory.BALL);
        }

        @Override
        public double getModifier(ActivePokemon me, ActivePokemon o, Battle b) {
            if (o.getLevel() <= 19) {
                return 3;
            } else if (o.getLevel() <= 29) {
                return 2;
            } else {
                return 1;
            }
        }
    }

    static class NetBall extends Item implements BallItem {
        private static final long serialVersionUID = 1L;

        NetBall() {
            super(ItemNamesies.NET_BALL, "A somewhat different Pok\u00e9 Ball that is more effective when attempting to catch Water- or Bug-type Pok\u00e9mon.", BagCategory.BALL);
            super.price = 1000;
            super.battleBagCategories.add(BattleBagCategory.BALL);
        }

        @Override
        public double getModifier(ActivePokemon me, ActivePokemon o, Battle b) {
            return o.isType(b, Type.WATER, Type.BUG) ? 3 : 1;
        }
    }

    static class PokeBall extends Item implements BallItem {
        private static final long serialVersionUID = 1L;

        PokeBall() {
            super(ItemNamesies.POKE_BALL, "A device for catching wild Pok\u00e9mon. It's thrown like a ball at a Pok\u00e9mon, comfortably encapsulating its target.", BagCategory.BALL);
            super.price = 200;
            super.battleBagCategories.add(BattleBagCategory.BALL);
        }
    }

    static class PremierBall extends Item implements BallItem {
        private static final long serialVersionUID = 1L;

        PremierBall() {
            super(ItemNamesies.PREMIER_BALL, "A somewhat rare Pok\u00e9 Ball that was made as a commemorative item used to celebrate an event of some sort.", BagCategory.BALL);
            super.price = 200;
            super.battleBagCategories.add(BattleBagCategory.BALL);
        }
    }

    static class QuickBall extends Item implements BallItem {
        private static final long serialVersionUID = 1L;

        QuickBall() {
            super(ItemNamesies.QUICK_BALL, "A somewhat different Pok\u00e9 Ball that has a more successful catch rate if used at the start of a wild encounter.", BagCategory.BALL);
            super.price = 1000;
            super.battleBagCategories.add(BattleBagCategory.BALL);
        }

        @Override
        public double getModifier(ActivePokemon me, ActivePokemon o, Battle b) {
            return b.getTurn() == 1 ? 3 : 1;
        }
    }

    static class RepeatBall extends Item implements BallItem {
        private static final long serialVersionUID = 1L;

        RepeatBall() {
            super(ItemNamesies.REPEAT_BALL, "A somewhat different Pok\u00e9 Ball that works especially well on a Pok\u00e9mon species that has been caught before.", BagCategory.BALL);
            super.price = 1000;
            super.battleBagCategories.add(BattleBagCategory.BALL);
        }

        @Override
        public double getModifier(ActivePokemon me, ActivePokemon o, Battle b) {
            return Game.getPlayer().getPokedex().isCaught(o) ? 3 : 1;
        }
    }

    static class SafariBall extends Item implements BallItem {
        private static final long serialVersionUID = 1L;

        SafariBall() {
            super(ItemNamesies.SAFARI_BALL, "A special Pok\u00e9 Ball that is used only in the Safari Zone. It is recognizable by the camouflage pattern decorating it.", BagCategory.BALL);
            super.price = 0;
            super.battleBagCategories.add(BattleBagCategory.BALL);
        }

        @Override
        public double getModifier(ActivePokemon me, ActivePokemon o, Battle b) {
            return 1.5;
        }
    }

    static class TimerBall extends Item implements BallItem {
        private static final long serialVersionUID = 1L;

        TimerBall() {
            super(ItemNamesies.TIMER_BALL, "A somewhat different Poké Ball that becomes progressively more effective at catching Pokémon the more turns that are taken in battle.", BagCategory.BALL);
            super.price = 1000;
            super.battleBagCategories.add(BattleBagCategory.BALL);
        }

        @Override
        public double getModifier(ActivePokemon me, ActivePokemon o, Battle b) {
            return Math.min(b.getTurn()/10 + 1, 4);
        }
    }

    static class UltraBall extends Item implements BallItem {
        private static final long serialVersionUID = 1L;

        UltraBall() {
            super(ItemNamesies.ULTRA_BALL, "An ultra-high-performance Pok\u00e9 Ball that provides a higher success rate for catching Pok\u00e9mon than a Great Ball.", BagCategory.BALL);
            super.price = 800;
            super.battleBagCategories.add(BattleBagCategory.BALL);
        }

        @Override
        public double getModifier(ActivePokemon me, ActivePokemon o, Battle b) {
            return 2;
        }
    }

    static class CheriBerry extends Item implements StatusBerry, StatusHealer {
        private static final long serialVersionUID = 1L;

        CheriBerry() {
            super(ItemNamesies.CHERI_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a Pok\u00e9mon holds one, it can recover from paralysis on its own in battle.", BagCategory.BERRY);
            super.price = 20;
            super.battleBagCategories.add(BattleBagCategory.STATUS);
        }

        @Override
        public Type naturalGiftType() {
            return Type.FIRE;
        }

        @Override
        public boolean shouldHeal(ActivePokemon p) {
            return p.hasStatus(StatusNamesies.PARALYZED);
        }
    }

    static class ChestoBerry extends Item implements StatusBerry, StatusHealer {
        private static final long serialVersionUID = 1L;

        ChestoBerry() {
            super(ItemNamesies.CHESTO_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a Pok\u00e9mon holds one, it can recover from sleep on its own in battle.", BagCategory.BERRY);
            super.price = 20;
            super.battleBagCategories.add(BattleBagCategory.STATUS);
        }

        @Override
        public Type naturalGiftType() {
            return Type.WATER;
        }

        @Override
        public boolean shouldHeal(ActivePokemon p) {
            return p.hasStatus(StatusNamesies.ASLEEP);
        }
    }

    static class PechaBerry extends Item implements StatusBerry, StatusHealer {
        private static final long serialVersionUID = 1L;

        PechaBerry() {
            super(ItemNamesies.PECHA_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a Pok\u00e9mon holds one, it can recover from poisoning on its own in battle.", BagCategory.BERRY);
            super.price = 20;
            super.battleBagCategories.add(BattleBagCategory.STATUS);
        }

        @Override
        public Type naturalGiftType() {
            return Type.ELECTRIC;
        }

        @Override
        public boolean shouldHeal(ActivePokemon p) {
            return p.hasStatus(StatusNamesies.POISONED);
        }
    }

    static class RawstBerry extends Item implements StatusBerry, StatusHealer {
        private static final long serialVersionUID = 1L;

        RawstBerry() {
            super(ItemNamesies.RAWST_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a Pok\u00e9mon holds one, it can recover from a burn on its own in battle.", BagCategory.BERRY);
            super.price = 20;
            super.battleBagCategories.add(BattleBagCategory.STATUS);
        }

        @Override
        public Type naturalGiftType() {
            return Type.GRASS;
        }

        @Override
        public boolean shouldHeal(ActivePokemon p) {
            return p.hasStatus(StatusNamesies.BURNED);
        }
    }

    static class AspearBerry extends Item implements StatusBerry, StatusHealer {
        private static final long serialVersionUID = 1L;

        AspearBerry() {
            super(ItemNamesies.ASPEAR_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a Pok\u00e9mon holds one, it can recover from being frozen on its own in battle.", BagCategory.BERRY);
            super.price = 20;
            super.battleBagCategories.add(BattleBagCategory.STATUS);
        }

        @Override
        public Type naturalGiftType() {
            return Type.ICE;
        }

        @Override
        public boolean shouldHeal(ActivePokemon p) {
            return p.hasStatus(StatusNamesies.FROZEN);
        }
    }

    // TODO: These currently cannot be used in battle :(
    static class LeppaBerry extends Item implements EndTurnEffect, GainableEffectBerry, PPHealer {
        private static final long serialVersionUID = 1L;

        LeppaBerry() {
            super(ItemNamesies.LEPPA_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a Pok\u00e9mon holds one, it can restore 10 PP to a depleted move during battle.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type naturalGiftType() {
            return Type.FIGHTING;
        }

        @Override
        public int naturalGiftPower() {
            return 80;
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            for (Move m : victim.getMoves(b)) {
                if (m.getPP() == 0) {
                    use(victim, m, CastSource.HELD_ITEM);
                    this.consumeItem(b, victim);
                    break;
                }
            }
        }

        @Override
        public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
            Move lowestPPMove = null;
            double lowestPPRatio = 1;

            for (Move m : user.getMoves(b)) {
                double ratio = (double)m.getPP()/m.getMaxPP();
                if (ratio < lowestPPRatio) {
                    lowestPPRatio = ratio;
                    lowestPPMove = m;
                }
            }

            // All moves have full PP
            if (lowestPPMove == null) {
                return false;
            }

            use(user, lowestPPMove);
            return true;
        }

        @Override
        public int restoreAmount(ActivePokemon restorer, Move toRestore) {
            return this.ripen(restorer)*10;
        }
    }

    static class OranBerry extends Item implements HealthTriggeredBerry, HpHealer {
        private static final long serialVersionUID = 1L;

        OranBerry() {
            super(ItemNamesies.ORAN_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a Pok\u00e9mon holds one, it can restore its own HP by 10 points during battle.", BagCategory.BERRY);
            super.price = 20;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public Type naturalGiftType() {
            return Type.POISON;
        }

        @Override
        public int naturalGiftPower() {
            return 80;
        }

        @Override
        public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
            return use(b, user, source);
        }

        @Override
        public int getHealAmount(ActivePokemon p) {
            return this.ripen(p)*10;
        }

        @Override
        public double healthTriggerRatio() {
            return 1/3.0;
        }
    }

    static class PersimBerry extends Item implements BattleUseItem, MessageGetter, GainableEffectBerry, EffectCurerItem {
        private static final long serialVersionUID = 1L;

        private boolean use(ActivePokemon p, CastSource source) {
            if (p.getEffects().remove(PokemonEffectNamesies.CONFUSION)) {
                Messages.add(this.getMessage(p, source));
                return true;
            }

            return false;
        }

        PersimBerry() {
            super(ItemNamesies.PERSIM_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a Pok\u00e9mon holds one, it can recover from confusion on its own in battle.", BagCategory.BERRY);
            super.price = 20;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public Type naturalGiftType() {
            return Type.GROUND;
        }

        @Override
        public int naturalGiftPower() {
            return 80;
        }

        @Override
        public boolean use(ActivePokemon p, Battle b) {
            return use(p, CastSource.USE_ITEM);
        }

        @Override
        public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
            return use(user, source);
        }

        @Override
        public String getGenericMessage(ActivePokemon p) {
            return p.getName() + " snapped out of its confusion!";
        }

        @Override
        public String getSourceMessage(ActivePokemon p, String sourceName) {
            return p.getName() + "'s " + this.getName() + " snapped it out of confusion!";
        }

        @Override
        public Set<PokemonEffectNamesies> getCurableEffects() {
            return EnumSet.of(PokemonEffectNamesies.CONFUSION);
        }
    }

    static class LumBerry extends Item implements StatusBerry, StatusHealer {
        private static final long serialVersionUID = 1L;

        LumBerry() {
            super(ItemNamesies.LUM_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a Pok\u00e9mon holds one, it can recover from any status condition during battle.", BagCategory.BERRY);
            super.price = 20;
            super.battleBagCategories.add(BattleBagCategory.STATUS);
        }

        @Override
        public Type naturalGiftType() {
            return Type.FLYING;
        }

        @Override
        public int getHarvestHours() {
            return 48;
        }

        @Override
        public boolean shouldHeal(ActivePokemon p) {
            // Can't heal what you don't have
            return p.hasStatus();
        }
    }

    static class SitrusBerry extends Item implements HealthTriggeredBerry, HpHealer {
        private static final long serialVersionUID = 1L;

        SitrusBerry() {
            super(ItemNamesies.SITRUS_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a Pok\u00e9mon holds one, it can restore its own HP by a small amount during battle.", BagCategory.BERRY);
            super.price = 20;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public Type naturalGiftType() {
            return Type.PSYCHIC;
        }

        @Override
        public int naturalGiftPower() {
            return 80;
        }

        @Override
        public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
            return use(b, user, source);
        }

        @Override
        public int getHealAmount(ActivePokemon p) {
            return p.getHealHealthFractionAmount(ripen(p)/4.0);
        }

        @Override
        public int getHarvestHours() {
            return 48;
        }

        @Override
        public double healthTriggerRatio() {
            return 1/2.0;
        }
    }

    static class RazzBerry extends Item implements Berry {
        private static final long serialVersionUID = 1L;

        RazzBerry() {
            super(ItemNamesies.RAZZ_BERRY, "A very valuable berry. Useful for acquiring value.", BagCategory.BERRY);
            super.price = 60000;
        }

        @Override
        public Type naturalGiftType() {
            return Type.STEEL;
        }

        @Override
        public int naturalGiftPower() {
            return 80;
        }
    }

    static class PomegBerry extends Item implements EvDecreaseBerry {
        private static final long serialVersionUID = 1L;

        PomegBerry() {
            super(ItemNamesies.POMEG_BERRY, "A Berry to be consumed by Pok\u00e9mon. Using it on a Pok\u00e9mon lowers its HP base points.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type naturalGiftType() {
            return Type.ICE;
        }

        @Override
        public Stat toDecrease() {
            return Stat.HP;
        }
    }

    static class KelpsyBerry extends Item implements EvDecreaseBerry {
        private static final long serialVersionUID = 1L;

        KelpsyBerry() {
            super(ItemNamesies.KELPSY_BERRY, "A Berry to be consumed by Pok\u00e9mon. Using it on a Pok\u00e9mon lowers its Attack base points.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type naturalGiftType() {
            return Type.FIGHTING;
        }

        @Override
        public Stat toDecrease() {
            return Stat.ATTACK;
        }
    }

    static class QualotBerry extends Item implements EvDecreaseBerry {
        private static final long serialVersionUID = 1L;

        QualotBerry() {
            super(ItemNamesies.QUALOT_BERRY, "A Berry to be consumed by Pok\u00e9mon. Using it on a Pok\u00e9mon lowers its Defense base points.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type naturalGiftType() {
            return Type.POISON;
        }

        @Override
        public Stat toDecrease() {
            return Stat.DEFENSE;
        }
    }

    static class HondewBerry extends Item implements EvDecreaseBerry {
        private static final long serialVersionUID = 1L;

        HondewBerry() {
            super(ItemNamesies.HONDEW_BERRY, "A Berry to be consumed by Pok\u00e9mon. Using it on a Pok\u00e9mon lowers its Sp. Atk base points.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type naturalGiftType() {
            return Type.GROUND;
        }

        @Override
        public Stat toDecrease() {
            return Stat.SP_ATTACK;
        }
    }

    static class GrepaBerry extends Item implements EvDecreaseBerry {
        private static final long serialVersionUID = 1L;

        GrepaBerry() {
            super(ItemNamesies.GREPA_BERRY, "A Berry to be consumed by Pok\u00e9mon. Using it on a Pok\u00e9mon lowers its Sp. Def base points.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type naturalGiftType() {
            return Type.FLYING;
        }

        @Override
        public Stat toDecrease() {
            return Stat.SP_DEFENSE;
        }
    }

    static class TamatoBerry extends Item implements EvDecreaseBerry {
        private static final long serialVersionUID = 1L;

        TamatoBerry() {
            super(ItemNamesies.TAMATO_BERRY, "A Berry to be consumed by Pok\u00e9mon. Using it on a Pok\u00e9mon lowers its Speed base points.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type naturalGiftType() {
            return Type.PSYCHIC;
        }

        @Override
        public Stat toDecrease() {
            return Stat.SPEED;
        }
    }

    static class OccaBerry extends Item implements SuperEffectiveTypedPowerReduceBerry {
        private static final long serialVersionUID = 1L;

        OccaBerry() {
            super(ItemNamesies.OCCA_BERRY, "If held by a Pok\u00e9mon, this Berry will lessen the damage taken from one supereffective Fire-type attack.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type getType() {
            return Type.FIRE;
        }
    }

    static class PasshoBerry extends Item implements SuperEffectiveTypedPowerReduceBerry {
        private static final long serialVersionUID = 1L;

        PasshoBerry() {
            super(ItemNamesies.PASSHO_BERRY, "If held by a Pok\u00e9mon, this Berry will lessen the damage taken from one supereffective Water-type attack.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type getType() {
            return Type.WATER;
        }
    }

    static class WacanBerry extends Item implements SuperEffectiveTypedPowerReduceBerry {
        private static final long serialVersionUID = 1L;

        WacanBerry() {
            super(ItemNamesies.WACAN_BERRY, "If held by a Pok\u00e9mon, this Berry will lessen the damage taken from one supereffective Electric-type attack.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type getType() {
            return Type.ELECTRIC;
        }
    }

    static class RindoBerry extends Item implements SuperEffectiveTypedPowerReduceBerry {
        private static final long serialVersionUID = 1L;

        RindoBerry() {
            super(ItemNamesies.RINDO_BERRY, "If held by a Pok\u00e9mon, this Berry will lessen the damage taken from one supereffective Grass-type attack.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type getType() {
            return Type.GRASS;
        }
    }

    static class YacheBerry extends Item implements SuperEffectiveTypedPowerReduceBerry {
        private static final long serialVersionUID = 1L;

        YacheBerry() {
            super(ItemNamesies.YACHE_BERRY, "If held by a Pok\u00e9mon, this Berry will lessen the damage taken from one supereffective Ice-type attack.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type getType() {
            return Type.ICE;
        }
    }

    static class ChopleBerry extends Item implements SuperEffectiveTypedPowerReduceBerry {
        private static final long serialVersionUID = 1L;

        ChopleBerry() {
            super(ItemNamesies.CHOPLE_BERRY, "If held by a Pok\u00e9mon, this Berry will lessen the damage taken from one supereffective Fighting-type attack.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type getType() {
            return Type.FIGHTING;
        }
    }

    static class KebiaBerry extends Item implements SuperEffectiveTypedPowerReduceBerry {
        private static final long serialVersionUID = 1L;

        KebiaBerry() {
            super(ItemNamesies.KEBIA_BERRY, "If held by a Pok\u00e9mon, this Berry will lessen the damage taken from one supereffective Poison-type attack.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type getType() {
            return Type.POISON;
        }
    }

    static class ShucaBerry extends Item implements SuperEffectiveTypedPowerReduceBerry {
        private static final long serialVersionUID = 1L;

        ShucaBerry() {
            super(ItemNamesies.SHUCA_BERRY, "If held by a Pok\u00e9mon, this Berry will lessen the damage taken from one supereffective Ground-type attack.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type getType() {
            return Type.GROUND;
        }
    }

    static class CobaBerry extends Item implements SuperEffectiveTypedPowerReduceBerry {
        private static final long serialVersionUID = 1L;

        CobaBerry() {
            super(ItemNamesies.COBA_BERRY, "If held by a Pok\u00e9mon, this Berry will lessen the damage taken from one supereffective Flying-type attack.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type getType() {
            return Type.FLYING;
        }
    }

    static class PayapaBerry extends Item implements SuperEffectiveTypedPowerReduceBerry {
        private static final long serialVersionUID = 1L;

        PayapaBerry() {
            super(ItemNamesies.PAYAPA_BERRY, "If held by a Pok\u00e9mon, this Berry will lessen the damage taken from one supereffective Psychic-type attack.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type getType() {
            return Type.PSYCHIC;
        }
    }

    static class TangaBerry extends Item implements SuperEffectiveTypedPowerReduceBerry {
        private static final long serialVersionUID = 1L;

        TangaBerry() {
            super(ItemNamesies.TANGA_BERRY, "If held by a Pok\u00e9mon, this Berry will lessen the damage taken from one supereffective Bug-type attack.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type getType() {
            return Type.BUG;
        }
    }

    static class ChartiBerry extends Item implements SuperEffectiveTypedPowerReduceBerry {
        private static final long serialVersionUID = 1L;

        ChartiBerry() {
            super(ItemNamesies.CHARTI_BERRY, "If held by a Pok\u00e9mon, this Berry will lessen the damage taken from one supereffective Rock-type attack.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type getType() {
            return Type.ROCK;
        }
    }

    static class KasibBerry extends Item implements SuperEffectiveTypedPowerReduceBerry {
        private static final long serialVersionUID = 1L;

        KasibBerry() {
            super(ItemNamesies.KASIB_BERRY, "If held by a Pok\u00e9mon, this Berry will lessen the damage taken from one supereffective Ghost-type attack.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type getType() {
            return Type.GHOST;
        }
    }

    static class HabanBerry extends Item implements SuperEffectiveTypedPowerReduceBerry {
        private static final long serialVersionUID = 1L;

        HabanBerry() {
            super(ItemNamesies.HABAN_BERRY, "If held by a Pok\u00e9mon, this Berry will lessen the damage taken from one supereffective Dragon-type attack.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type getType() {
            return Type.DRAGON;
        }
    }

    static class ColburBerry extends Item implements SuperEffectiveTypedPowerReduceBerry {
        private static final long serialVersionUID = 1L;

        ColburBerry() {
            super(ItemNamesies.COLBUR_BERRY, "If held by a Pok\u00e9mon, this Berry will lessen the damage taken from one supereffective Dark-type attack.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type getType() {
            return Type.DARK;
        }
    }

    static class BabiriBerry extends Item implements SuperEffectiveTypedPowerReduceBerry {
        private static final long serialVersionUID = 1L;

        BabiriBerry() {
            super(ItemNamesies.BABIRI_BERRY, "If held by a Pok\u00e9mon, this Berry will lessen the damage taken from one supereffective Steel-type attack.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type getType() {
            return Type.STEEL;
        }
    }

    // Unlike the other typed power-reduce berries, the Chilan Berry does not require the attack to be super-effective.
    static class ChilanBerry extends Item implements TypedPowerReduceBerry {
        private static final long serialVersionUID = 1L;

        ChilanBerry() {
            super(ItemNamesies.CHILAN_BERRY, "If held by a Pok\u00e9mon, this Berry will lessen the damage taken from one Normal-type attack.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type getType() {
            return Type.NORMAL;
        }
    }

    static class RoseliBerry extends Item implements SuperEffectiveTypedPowerReduceBerry {
        private static final long serialVersionUID = 1L;

        RoseliBerry() {
            super(ItemNamesies.ROSELI_BERRY, "If held by a Pok\u00e9mon, this Berry will lessen the damage taken from one supereffective Fairy-type attack.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type getType() {
            return Type.FAIRY;
        }
    }

    static class LiechiBerry extends Item implements HealthTriggeredStageIncreaseBerry {
        private static final long serialVersionUID = 1L;

        LiechiBerry() {
            super(ItemNamesies.LIECHI_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a Pok\u00e9mon holds one, its Attack stat will increase when it's in a pinch.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type naturalGiftType() {
            return Type.GRASS;
        }

        @Override
        public Stat getStat() {
            return Stat.ATTACK;
        }
    }

    static class GanlonBerry extends Item implements HealthTriggeredStageIncreaseBerry {
        private static final long serialVersionUID = 1L;

        GanlonBerry() {
            super(ItemNamesies.GANLON_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a Pok\u00e9mon holds one, its Defense stat will increase when it's in a pinch.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type naturalGiftType() {
            return Type.ICE;
        }

        @Override
        public Stat getStat() {
            return Stat.DEFENSE;
        }
    }

    static class SalacBerry extends Item implements HealthTriggeredStageIncreaseBerry {
        private static final long serialVersionUID = 1L;

        SalacBerry() {
            super(ItemNamesies.SALAC_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a Pok\u00e9mon holds one, its Speed stat will increase when it's in a pinch.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type naturalGiftType() {
            return Type.FIGHTING;
        }

        @Override
        public Stat getStat() {
            return Stat.SPEED;
        }
    }

    static class PetayaBerry extends Item implements HealthTriggeredStageIncreaseBerry {
        private static final long serialVersionUID = 1L;

        PetayaBerry() {
            super(ItemNamesies.PETAYA_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a Pok\u00e9mon holds one, its Sp. Atk stat will increase when it's in a pinch.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type naturalGiftType() {
            return Type.POISON;
        }

        @Override
        public Stat getStat() {
            return Stat.SP_ATTACK;
        }
    }

    static class ApicotBerry extends Item implements HealthTriggeredStageIncreaseBerry {
        private static final long serialVersionUID = 1L;

        ApicotBerry() {
            super(ItemNamesies.APICOT_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a Pok\u00e9mon holds one, its Sp. Def stat will increase when it's in a pinch.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type naturalGiftType() {
            return Type.GROUND;
        }

        @Override
        public Stat getStat() {
            return Stat.SP_DEFENSE;
        }
    }

    static class MicleBerry extends Item implements HealthTriggeredStageIncreaseBerry {
        private static final long serialVersionUID = 1L;

        MicleBerry() {
            super(ItemNamesies.MICLE_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a Pok\u00e9mon holds one, its accuracy will increase just once when it's in a pinch.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type naturalGiftType() {
            return Type.ROCK;
        }

        @Override
        public Stat getStat() {
            return Stat.ACCURACY;
        }
    }

    static class KeeBerry extends Item implements CategoryIncreaseBerry {
        private static final long serialVersionUID = 1L;

        KeeBerry() {
            super(ItemNamesies.KEE_BERRY, "If held by a Pok\u00e9mon, this Berry will increase the holder's Defense if it's hit with a physical move.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type naturalGiftType() {
            return Type.FAIRY;
        }

        @Override
        public Stat getStat() {
            return Stat.DEFENSE;
        }

        @Override
        public MoveCategory getCategory() {
            return MoveCategory.PHYSICAL;
        }
    }

    static class MarangaBerry extends Item implements CategoryIncreaseBerry {
        private static final long serialVersionUID = 1L;

        MarangaBerry() {
            super(ItemNamesies.MARANGA_BERRY, "If held by a Pok\u00e9mon, this Berry will increase the holder's Sp. Def if it's hit with a special move.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type naturalGiftType() {
            return Type.DARK;
        }

        @Override
        public Stat getStat() {
            return Stat.SP_DEFENSE;
        }

        @Override
        public MoveCategory getCategory() {
            return MoveCategory.SPECIAL;
        }
    }

    static class JabocaBerry extends Item implements CategoryDamageBerry {
        private static final long serialVersionUID = 1L;

        JabocaBerry() {
            super(ItemNamesies.JABOCA_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a physical attack hits the Pok\u00e9mon holding it, the attacker will also be hurt.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type naturalGiftType() {
            return Type.DRAGON;
        }

        @Override
        public MoveCategory getCategory() {
            return MoveCategory.PHYSICAL;
        }
    }

    static class RowapBerry extends Item implements CategoryDamageBerry {
        private static final long serialVersionUID = 1L;

        RowapBerry() {
            super(ItemNamesies.ROWAP_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a special attack hits the Pok\u00e9mon holding it, the attacker will also be hurt.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type naturalGiftType() {
            return Type.DARK;
        }

        @Override
        public MoveCategory getCategory() {
            return MoveCategory.SPECIAL;
        }
    }

    static class CustapBerry extends Item implements Berry, StrikeFirstEffect {
        private static final long serialVersionUID = 1L;

        CustapBerry() {
            super(ItemNamesies.CUSTAP_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a Pok\u00e9mon holds one, it will be able to move first just once when it's in a pinch.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type naturalGiftType() {
            return Type.GHOST;
        }

        @Override
        public int naturalGiftPower() {
            return 100;
        }

        @Override
        public int getHarvestHours() {
            return 72;
        }

        @Override
        public boolean strikeFirst(Battle b, ActivePokemon striker) {
            if (striker.getHPRatio() < 1/3.0) {
                this.consumeItem(b, striker);
                return true;
            }

            return false;
        }

        @Override
        public String getStrikeFirstMessage(ActivePokemon striker) {
            return striker.getName() + "'s " + this.getName() + " allowed it to strike first!";
        }
    }

    static class EnigmaBerry extends Item implements Berry, TakeDamageEffect {
        private static final long serialVersionUID = 1L;

        EnigmaBerry() {
            super(ItemNamesies.ENIGMA_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a Pok\u00e9mon holds one, being hit by a supereffective attack will restore its HP.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type naturalGiftType() {
            return Type.BUG;
        }

        @Override
        public int naturalGiftPower() {
            return 100;
        }

        @Override
        public int getHarvestHours() {
            return 72;
        }

        @Override
        public void onDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (!TypeAdvantage.isSuperEffective(user, victim, b)) {
                return;
            }

            // Super-effective moves restore health
            String message = victim.getName() + "'s " + this.getName() + " restored its health!";
            if (victim.healHealthFraction(.25*ripen(victim), b, message) > 0) {
                this.consumeItem(b, victim);
            }
        }
    }

    static class LansatBerry extends Item implements HealthTriggeredBerry {
        private static final long serialVersionUID = 1L;

        LansatBerry() {
            super(ItemNamesies.LANSAT_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a Pok\u00e9mon holds one, its critical-hit ratio will increase when it's in a pinch.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type naturalGiftType() {
            return Type.FLYING;
        }

        @Override
        public int naturalGiftPower() {
            return 100;
        }

        @Override
        public int getHarvestHours() {
            return 72;
        }

        @Override
        public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
            return Effect.apply(PokemonEffectNamesies.RAISE_CRITS, b, user, user, source, true).isSuccess();
        }

        @Override
        public double healthTriggerRatio() {
            return 1/4.0;
        }
    }

    static class StarfBerry extends Item implements HealthTriggeredBerry {
        private static final long serialVersionUID = 1L;

        StarfBerry() {
            super(ItemNamesies.STARF_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a Pok\u00e9mon holds one, one of its stats will sharply increase when it's in a pinch.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Type naturalGiftType() {
            return Type.PSYCHIC;
        }

        @Override
        public int naturalGiftPower() {
            return 100;
        }

        @Override
        public int getHarvestHours() {
            return 72;
        }

        @Override
        public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
            // Get stats that can still increase
            List<Stat> stats = user.getStages().getNonMaxStats();

            // You probably don't need the berry at this point anyhow...
            if (stats.isEmpty()) {
                return false;
            }

            // Sharply raise random battle stat
            Stat stat = RandomUtils.getRandomValue(stats);
            return new StageModifier(2*ripen(user), stat).modify(b, user, user, source);
        }

        @Override
        public double healthTriggerRatio() {
            return 1/4.0;
        }
    }

    static class CometShard extends Item implements HoldItem {
        private static final long serialVersionUID = 1L;

        CometShard() {
            super(ItemNamesies.COMET_SHARD, "A shard that fell to the ground when a comet approached. It can be sold at a high price to shops.", BagCategory.MISC);
            super.price = 60000;
        }
    }

    static class TinyMushroom extends Item implements HoldItem {
        private static final long serialVersionUID = 1L;

        TinyMushroom() {
            super(ItemNamesies.TINY_MUSHROOM, "A very small and rare mushroom. It's popular with a certain class of collectors.", BagCategory.MISC);
            super.price = 500;
        }
    }

    static class BigMushroom extends Item implements HoldItem {
        private static final long serialVersionUID = 1L;

        BigMushroom() {
            super(ItemNamesies.BIG_MUSHROOM, "A very large and rare mushroom. It's popular with a certain class of collectors.", BagCategory.MISC);
            super.price = 5000;
        }
    }

    static class BalmMushroom extends Item implements HoldItem {
        private static final long serialVersionUID = 1L;

        BalmMushroom() {
            super(ItemNamesies.BALM_MUSHROOM, "A rare mushroom that gives off a nice fragrance. It can be sold at a high price to shops.", BagCategory.MISC);
            super.price = 15000;
        }
    }

    static class Nugget extends Item implements HoldItem {
        private static final long serialVersionUID = 1L;

        Nugget() {
            super(ItemNamesies.NUGGET, "A nugget of the purest gold that gives off a lustrous gleam in direct light. It can be sold at a high price to shops.", BagCategory.MISC);
            super.price = 10000;
        }
    }

    static class BigNugget extends Item implements HoldItem {
        private static final long serialVersionUID = 1L;

        BigNugget() {
            super(ItemNamesies.BIG_NUGGET, "A big nugget of pure gold that gives off a lustrous gleam. It can be sold at a high price to shops.", BagCategory.MISC);
            super.price = 40000;
        }
    }

    static class Pearl extends Item implements HoldItem {
        private static final long serialVersionUID = 1L;

        Pearl() {
            super(ItemNamesies.PEARL, "A rather small pearl that has a very nice silvery sheen to it. It can be sold at a low price to shops.", BagCategory.MISC);
            super.price = 2000;
        }
    }

    static class BigPearl extends Item implements HoldItem {
        private static final long serialVersionUID = 1L;

        BigPearl() {
            super(ItemNamesies.BIG_PEARL, "A rather large pearl that has a very nice silvery sheen. It can be sold at a high price to shops.", BagCategory.MISC);
            super.price = 8000;
        }
    }

    static class Stardust extends Item implements HoldItem {
        private static final long serialVersionUID = 1L;

        Stardust() {
            super(ItemNamesies.STARDUST, "Lovely red sand that flows between the fingers with a loose, silky feel. It can be sold at a low price to shops.", BagCategory.MISC);
            super.price = 3000;
        }
    }

    static class StarPiece extends Item implements HoldItem {
        private static final long serialVersionUID = 1L;

        StarPiece() {
            super(ItemNamesies.STAR_PIECE, "A small shard of a beautiful gem that gives off a distinctly red sparkle. It can be sold at a high price to shops.", BagCategory.MISC);
            super.price = 12000;
        }
    }

    static class RareBone extends Item implements HoldItem {
        private static final long serialVersionUID = 1L;

        RareBone() {
            super(ItemNamesies.RARE_BONE, "A rare bone that is extremely valuable for the study of Pok\u00e9mon archeology. It can be sold for a high price to shops.", BagCategory.MISC);
            super.price = 5000;
        }

        @Override
        public int flingDamage() {
            return 100;
        }
    }

    static class Honey extends Item implements HoldItem, PlayerUseItem {
        private static final long serialVersionUID = 1L;

        Honey() {
            super(ItemNamesies.HONEY, "A sweet honey collected by Pokémon. It has a lush aroma and can be sold at a low price to shops.", BagCategory.MISC);
            super.price = 100;
        }

        @Override
        public boolean use() {
            Player player = Game.getPlayer();
            GameData data = Game.getData();

            MapData map = data.getMap(player.getMapName());
            WalkingWildBattleTrigger walkingEncounter = map.getCurrentWildBattleTrigger();

            // No wild battle on the current tile
            if (walkingEncounter == null) {
                return false;
            }

            // Exit bag view to map before starting the battle
            Game.instance().getBagView().returnToMap();

            // Create an encounter from the walking encounter trigger
            WildEncounter wildPokemon = walkingEncounter.getWildEncounter(player.front());

            // Let the battle begin!
            Trigger wildBattle = new WildBattleTrigger(wildPokemon);
            Messages.add(new MessageUpdate().withTrigger(wildBattle));

            return true;
        }
    }

    static class Eviolite extends Item implements HoldItem, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        Eviolite() {
            super(ItemNamesies.EVIOLITE, "A mysterious evolutionary lump. When held by a Pokémon that can still evolve, it raises both Defense and Sp. Def.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public int flingDamage() {
            return 40;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.DEFENSE || s == Stat.SP_DEFENSE;
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return p.getPokemonInfo().getEvolution().canEvolve();
        }

        @Override
        public double getModifier() {
            return 1.5;
        }
    }

    static class HeartScale extends Item implements HoldItem {
        private static final long serialVersionUID = 1L;

        HeartScale() {
            super(ItemNamesies.HEART_SCALE, "A pretty, heart-shaped scale that is extremely rare. Some people are happy to receive one.", BagCategory.MISC);
            super.price = 100;
        }
    }

    static class Repel extends Item implements RepelItem {
        private static final long serialVersionUID = 1L;

        Repel() {
            super(ItemNamesies.REPEL, "An item that prevents any low-level wild Pok\u00e9mon from jumping out at you for a while.", BagCategory.MISC);
            super.price = 400;
        }

        @Override
        public int repelSteps() {
            return 100;
        }
    }

    static class SuperRepel extends Item implements RepelItem {
        private static final long serialVersionUID = 1L;

        SuperRepel() {
            super(ItemNamesies.SUPER_REPEL, "An item that prevents any low-level wild Pok\u00e9mon from jumping out at you for a while. It lasts longer than Repel.", BagCategory.MISC);
            super.price = 700;
        }

        @Override
        public int repelSteps() {
            return 200;
        }
    }

    static class MaxRepel extends Item implements RepelItem {
        private static final long serialVersionUID = 1L;

        MaxRepel() {
            super(ItemNamesies.MAX_REPEL, "An item that prevents any low-level wild Pok\u00e9mon from jumping out at you for a while. It lasts longer than Super Repel.", BagCategory.MISC);
            super.price = 900;
        }

        @Override
        public int repelSteps() {
            return 250;
        }
    }

    static class AbilityCapsule extends Item implements PokemonUseItem {
        private static final long serialVersionUID = 1L;

        AbilityCapsule() {
            super(ItemNamesies.ABILITY_CAPSULE, "A capsule that allows a Pok\u00e9mon to switch between its Abilities when it is used.", BagCategory.MISC);
            super.price = 10000;
        }

        @Override
        public boolean use(ActivePokemon p) {
            // Get a list of possible abilities this Pokemon can have and remove the current ability
            List<AbilityNamesies> abilities = new ArrayList<>(Arrays.asList(p.getPokemonInfo().getAbilities()));
            abilities.removeIf(p::hasAbility);

            // No possible abilities to change to
            if (abilities.isEmpty()) {
                return false;
            }

            // Set to a random ability
            p.setAbility(RandomUtils.getRandomValue(abilities));
            Messages.add(p.getName() + "'s ability was changed to " + p.getAbility().getName() + "!");
            return true;
        }
    }

    static class AssaultVest extends Item implements HoldItem, AttackSelectionEffect, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        AssaultVest() {
            super(ItemNamesies.ASSAULT_VEST, "An item to be held by a Pok\u00e9mon. This offensive vest raises Sp. Def but prevents the use of status moves.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public boolean usable(Battle b, ActivePokemon p, Move m) {
            return !m.getAttack().isStatusMove();
        }

        @Override
        public String getUnusableMessage(ActivePokemon p) {
            return p.getName() + "'s " + this.getName() + " prevents the use of status moves!";
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SP_DEFENSE;
        }

        @Override
        public int flingDamage() {
            return 80;
        }

        @Override
        public double getModifier() {
            return 1.5;
        }
    }

    static class PowerHerb extends Item implements HoldItem {
        private static final long serialVersionUID = 1L;

        PowerHerb() {
            super(ItemNamesies.POWER_HERB, "A single-use item to be held by a Pok\u00e9mon. It allows the holder to immediately use a move that normally requires a turn to charge.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public int flingDamage() {
            return 10;
        }
    }

    static class FireMemory extends Item implements MemoryItem {
        private static final long serialVersionUID = 1L;

        FireMemory() {
            super(ItemNamesies.FIRE_MEMORY, "A memory disc that contains Fire-type data. It changes the type of the holder if held by a certain species of Pok\u00e9mon.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.FIRE;
        }
    }

    static class WaterMemory extends Item implements MemoryItem {
        private static final long serialVersionUID = 1L;

        WaterMemory() {
            super(ItemNamesies.WATER_MEMORY, "A memory disc that contains Water-type data. It changes the type of the holder if held by a certain species of Pok\u00e9mon.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.WATER;
        }
    }

    static class ElectricMemory extends Item implements MemoryItem {
        private static final long serialVersionUID = 1L;

        ElectricMemory() {
            super(ItemNamesies.ELECTRIC_MEMORY, "A memory disc that contains Electric-type data. It changes the type of the holder if held by a certain species of Pok\u00e9mon.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.ELECTRIC;
        }
    }

    static class GrassMemory extends Item implements MemoryItem {
        private static final long serialVersionUID = 1L;

        GrassMemory() {
            super(ItemNamesies.GRASS_MEMORY, "A memory disc that contains Grass-type data. It changes the type of the holder if held by a certain species of Pok\u00e9mon.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.GRASS;
        }
    }

    static class IceMemory extends Item implements MemoryItem {
        private static final long serialVersionUID = 1L;

        IceMemory() {
            super(ItemNamesies.ICE_MEMORY, "A memory disc that contains Ice-type data. It changes the type of the holder if held by a certain species of Pok\u00e9mon.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.ICE;
        }
    }

    static class FightingMemory extends Item implements MemoryItem {
        private static final long serialVersionUID = 1L;

        FightingMemory() {
            super(ItemNamesies.FIGHTING_MEMORY, "A memory disc that contains Fighting-type data. It changes the type of the holder if held by a certain species of Pok\u00e9mon.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.FIGHTING;
        }
    }

    static class PoisonMemory extends Item implements MemoryItem {
        private static final long serialVersionUID = 1L;

        PoisonMemory() {
            super(ItemNamesies.POISON_MEMORY, "A memory disc that contains Poison-type data. It changes the type of the holder if held by a certain species of Pok\u00e9mon.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.POISON;
        }
    }

    static class GroundMemory extends Item implements MemoryItem {
        private static final long serialVersionUID = 1L;

        GroundMemory() {
            super(ItemNamesies.GROUND_MEMORY, "A memory disc that contains Ground-type data. It changes the type of the holder if held by a certain species of Pok\u00e9mon.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.GROUND;
        }
    }

    static class FlyingMemory extends Item implements MemoryItem {
        private static final long serialVersionUID = 1L;

        FlyingMemory() {
            super(ItemNamesies.FLYING_MEMORY, "A memory disc that contains Flying-type data. It changes the type of the holder if held by a certain species of Pok\u00e9mon.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.FLYING;
        }
    }

    static class PsychicMemory extends Item implements MemoryItem {
        private static final long serialVersionUID = 1L;

        PsychicMemory() {
            super(ItemNamesies.PSYCHIC_MEMORY, "A memory disc that contains Psychic-type data. It changes the type of the holder if held by a certain species of Pok\u00e9mon.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.PSYCHIC;
        }
    }

    static class BugMemory extends Item implements MemoryItem {
        private static final long serialVersionUID = 1L;

        BugMemory() {
            super(ItemNamesies.BUG_MEMORY, "A memory disc that contains Bug-type data. It changes the type of the holder if held by a certain species of Pok\u00e9mon.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.BUG;
        }
    }

    static class RockMemory extends Item implements MemoryItem {
        private static final long serialVersionUID = 1L;

        RockMemory() {
            super(ItemNamesies.ROCK_MEMORY, "A memory disc that contains Rock-type data. It changes the type of the holder if held by a certain species of Pok\u00e9mon.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.ROCK;
        }
    }

    static class GhostMemory extends Item implements MemoryItem {
        private static final long serialVersionUID = 1L;

        GhostMemory() {
            super(ItemNamesies.GHOST_MEMORY, "A memory disc that contains Ghost-type data. It changes the type of the holder if held by a certain species of Pok\u00e9mon.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.GHOST;
        }
    }

    static class DragonMemory extends Item implements MemoryItem {
        private static final long serialVersionUID = 1L;

        DragonMemory() {
            super(ItemNamesies.DRAGON_MEMORY, "A memory disc that contains Dragon-type data. It changes the type of the holder if held by a certain species of Pok\u00e9mon.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.DRAGON;
        }
    }

    static class DarkMemory extends Item implements MemoryItem {
        private static final long serialVersionUID = 1L;

        DarkMemory() {
            super(ItemNamesies.DARK_MEMORY, "A memory disc that contains Dark-type data. It changes the type of the holder if held by a certain species of Pok\u00e9mon.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.DARK;
        }
    }

    static class SteelMemory extends Item implements MemoryItem {
        private static final long serialVersionUID = 1L;

        SteelMemory() {
            super(ItemNamesies.STEEL_MEMORY, "A memory disc that contains Steel-type data. It changes the type of the holder if held by a certain species of Pok\u00e9mon.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.STEEL;
        }
    }

    static class FairyMemory extends Item implements MemoryItem {
        private static final long serialVersionUID = 1L;

        FairyMemory() {
            super(ItemNamesies.FAIRY_MEMORY, "A memory disc that contains Fairy-type data. It changes the type of the holder if held by a certain species of Pok\u00e9mon.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public Type getType() {
            return Type.FAIRY;
        }
    }

    static class ElectricSeed extends Item implements HoldItem, TerrainCastEffect {
        private static final long serialVersionUID = 1L;

        ElectricSeed() {
            super(ItemNamesies.ELECTRIC_SEED, "An item to be held by a Pok\u00e9mon. It boosts Defense on Electric Terrain. It can only be used once.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void newTerrain(Battle b, ActivePokemon p, TerrainType newTerrain) {
            if (newTerrain == TerrainType.ELECTRIC && new StageModifier(1, Stat.DEFENSE).modify(b, p, p, CastSource.HELD_ITEM)) {
                this.consumeItem(b, p);
            }
        }

        @Override
        public int flingDamage() {
            return 10;
        }
    }

    static class GrassySeed extends Item implements HoldItem, TerrainCastEffect {
        private static final long serialVersionUID = 1L;

        GrassySeed() {
            super(ItemNamesies.GRASSY_SEED, "An item to be held by a Pok\u00e9mon. It boosts Defense on Grassy Terrain. It can only be used once.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void newTerrain(Battle b, ActivePokemon p, TerrainType newTerrain) {
            if (newTerrain == TerrainType.GRASS && new StageModifier(1, Stat.DEFENSE).modify(b, p, p, CastSource.HELD_ITEM)) {
                this.consumeItem(b, p);
            }
        }

        @Override
        public int flingDamage() {
            return 10;
        }
    }

    static class MistySeed extends Item implements HoldItem, TerrainCastEffect {
        private static final long serialVersionUID = 1L;

        MistySeed() {
            super(ItemNamesies.MISTY_SEED, "An item to be held by a Pok\u00e9mon. It boosts Sp. Def on Misty Terrain. It can only be used once.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void newTerrain(Battle b, ActivePokemon p, TerrainType newTerrain) {
            if (newTerrain == TerrainType.MISTY && new StageModifier(1, Stat.SP_DEFENSE).modify(b, p, p, CastSource.HELD_ITEM)) {
                this.consumeItem(b, p);
            }
        }

        @Override
        public int flingDamage() {
            return 10;
        }
    }

    static class PsychicSeed extends Item implements HoldItem, TerrainCastEffect {
        private static final long serialVersionUID = 1L;

        PsychicSeed() {
            super(ItemNamesies.PSYCHIC_SEED, "An item to be held by a Pok\u00e9mon. It boosts Sp. Def on Psychic Terrain. It can only be used once.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void newTerrain(Battle b, ActivePokemon p, TerrainType newTerrain) {
            if (newTerrain == TerrainType.PSYCHIC && new StageModifier(1, Stat.SP_DEFENSE).modify(b, p, p, CastSource.HELD_ITEM)) {
                this.consumeItem(b, p);
            }
        }

        @Override
        public int flingDamage() {
            return 10;
        }
    }

    static class BlunderPolicy extends Item implements AttackMissedEffect, HoldItem {
        private static final long serialVersionUID = 1L;

        BlunderPolicy() {
            super(ItemNamesies.BLUNDER_POLICY, "Raises Speed sharply when a Pokémon misses with a move because of accuracy.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void afterMiss(Battle b, ActivePokemon misser) {
            if (misser.getMoveData().isNaturalMiss() && new StageModifier(2, Stat.SPEED).modify(b, misser, misser, CastSource.HELD_ITEM)) {
                this.consumeItem(b, misser);
            }
        }

        @Override
        public int flingDamage() {
            return 80;
        }
    }

    static class CatchingCharm extends Item {
        private static final long serialVersionUID = 1L;

        CatchingCharm() {
            super(ItemNamesies.CATCHING_CHARM, "Holding it is said to increase the chance of getting a critical catch. Curiously, the charm doesn't shake much.", BagCategory.KEY_ITEM);
        }
    }

    // Note: Only including Cracked Pot, and not Chipped Pot because they're basically the same thing
    static class CrackedPot extends Item implements HoldItem, EvolutionItem {
        private static final long serialVersionUID = 1L;

        CrackedPot() {
            super(ItemNamesies.CRACKED_POT, "A peculiar teapot that can make a certain species of Pokémon evolve. It may be cracked, but tea poured from it is delicious.", BagCategory.MISC);
            super.price = 1600;
        }
    }

    static class EjectPack extends Item implements HoldItem, StatLoweredEffect {
        private static final long serialVersionUID = 1L;

        EjectPack() {
            super(ItemNamesies.EJECT_PACK, "An item to be held by a Pokémon. When the holder's stats are lowered, it will be switched out of battle.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void takeItToTheNextLevel(Battle b, ActivePokemon victim, boolean selfCaster) {
            if (victim.switcheroo(b, victim, CastSource.HELD_ITEM, false)) {
                this.consumeItem(b, victim);
            }
        }
    }

    static class HeavyDutyBoots extends Item implements HoldItem {
        private static final long serialVersionUID = 1L;

        HeavyDutyBoots() {
            super(ItemNamesies.HEAVY_DUTY_BOOTS, "These boots prevent the effects of traps set on the battlefield.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public int flingDamage() {
            return 80;
        }
    }

    static class ThroatSpray extends Item implements HoldItem, EndAttackEffect {
        private static final long serialVersionUID = 1L;

        ThroatSpray() {
            super(ItemNamesies.THROAT_SPRAY, "Raises Sp. Atk when a Pokémon uses a sound-based move.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void endsies(Battle b, ActivePokemon attacking) {
            if (attacking.getAttack().isMoveType(MoveType.SOUND_BASED) && new StageModifier(1, Stat.SP_ATTACK).modify(b, attacking, attacking, CastSource.HELD_ITEM)) {
                this.consumeItem(b, attacking);
            }
        }
    }

    // Note: In game actually only protects from sunny and rainy weather, which is really not obvious based on
    // the description so changing it to make it immune to all weather effects
    // Another Note: I believe in game ignores the power changes when receiving the attack, but changed here
    // to ignore when attacking instead because I think this behavior makes more sense (could easily change weather
    // to be OpponentPowerChangeEffect if we wanted but we don't so intentionally leaving it this way)
    static class UtilityUmbrella extends Item implements HoldItem, WeatherBlockerEffect {
        private static final long serialVersionUID = 1L;

        UtilityUmbrella() {
            super(ItemNamesies.UTILITY_UMBRELLA, "An item to be held by a Pokémon. This sturdy umbrella protects the holder from the effects of weather.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public boolean block(WeatherNamesies weather) {
            return true;
        }

        @Override
        public int flingDamage() {
            return 60;
        }
    }

    static class RoomService extends Item implements HoldItem, EffectReceivedEffect {
        private static final long serialVersionUID = 1L;

        RoomService() {
            super(ItemNamesies.ROOM_SERVICE, "An item to be held by a Pokémon. Lowers Speed when Trick Room takes effect.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void receiveEffect(Battle b, ActivePokemon caster, ActivePokemon victim, EffectNamesies effectType) {
            if (effectType == StandardBattleEffectNamesies.TRICK_ROOM && new StageModifier(-1, Stat.SPEED).modify(b, victim, victim, CastSource.HELD_ITEM)) {
                this.consumeItem(b, victim);
            }
        }

        @Override
        public int flingDamage() {
            return 100;
        }
    }

    static class TartApple extends Item implements HoldItem, EvolutionItem {
        private static final long serialVersionUID = 1L;

        TartApple() {
            super(ItemNamesies.TART_APPLE, "A peculiar apple that can make a certain species of Pokémon evolve. It's exceptionally tart.", BagCategory.MISC);
            super.price = 2200;
        }
    }

    static class SweetApple extends Item implements HoldItem, EvolutionItem {
        private static final long serialVersionUID = 1L;

        SweetApple() {
            super(ItemNamesies.SWEET_APPLE, "A peculiar apple that can make a certain species of Pokémon evolve. It's exceptionally sweet.", BagCategory.MISC);
            super.price = 2200;
        }
    }

    static class WorkUpTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        WorkUpTM() {
            super(ItemNamesies.WORK_UP_TM, "The user is roused, and its Attack and Sp. Atk stats increase.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.WORK_UP;
        }
    }

    static class DragonClawTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        DragonClawTM() {
            super(ItemNamesies.DRAGON_CLAW_TM, "The user slashes the target with huge sharp claws.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.DRAGON_CLAW;
        }
    }

    static class PsyshockTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        PsyshockTM() {
            super(ItemNamesies.PSYSHOCK_TM, "The user materializes an odd psychic wave to attack the target. This attack does physical damage.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.PSYSHOCK;
        }
    }

    static class CalmMindTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        CalmMindTM() {
            super(ItemNamesies.CALM_MIND_TM, "The user quietly focuses its mind and calms its spirit to raise its Sp. Atk and Sp. Def stats.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.CALM_MIND;
        }
    }

    static class RoarTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        RoarTM() {
            super(ItemNamesies.ROAR_TM, "The target is scared off, and a different Pokémon is dragged out. In the wild, this ends a battle against a single Pokémon.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.ROAR;
        }
    }

    static class ToxicTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        ToxicTM() {
            super(ItemNamesies.TOXIC_TM, "A move that leaves the target badly poisoned. Its poison damage worsens every turn.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.TOXIC;
        }
    }

    static class HailTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        HailTM() {
            super(ItemNamesies.HAIL_TM, "The user summons a hailstorm lasting five turns. It damages all Pokémon except Ice types.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.HAIL;
        }
    }

    static class BulkUpTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        BulkUpTM() {
            super(ItemNamesies.BULK_UP_TM, "The user tenses its muscles to bulk up its body, raising both its Attack and Defense stats.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.BULK_UP;
        }
    }

    static class VenoshockTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        VenoshockTM() {
            super(ItemNamesies.VENOSHOCK_TM, "The user drenches the target in a special poisonous liquid. This move's power is doubled if the target is poisoned.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.VENOSHOCK;
        }
    }

    static class HiddenPowerTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        HiddenPowerTM() {
            super(ItemNamesies.HIDDEN_POWER_TM, "A unique attack that varies in type depending on the Pokémon using it.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.HIDDEN_POWER;
        }
    }

    static class SunnyDayTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        SunnyDayTM() {
            super(ItemNamesies.SUNNY_DAY_TM, "The user intensifies the sun for five turns, powering up Fire-type moves. It lowers the power of Water-type moves.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.SUNNY_DAY;
        }
    }

    static class TauntTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        TauntTM() {
            super(ItemNamesies.TAUNT_TM, "The target is taunted into a rage that allows it to use only attack moves for three turns.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.TAUNT;
        }
    }

    static class IceBeamTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        IceBeamTM() {
            super(ItemNamesies.ICE_BEAM_TM, "The target is struck with an icy-cold beam of energy. This may also leave the target frozen.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.ICE_BEAM;
        }
    }

    static class BlizzardTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        BlizzardTM() {
            super(ItemNamesies.BLIZZARD_TM, "A howling blizzard is summoned to strike the target. This may also leave the target frozen.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.BLIZZARD;
        }
    }

    static class HyperBeamTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        HyperBeamTM() {
            super(ItemNamesies.HYPER_BEAM_TM, "The target is attacked with a powerful beam. The user can't move on the next turn.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.HYPER_BEAM;
        }
    }

    static class LightScreenTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        LightScreenTM() {
            super(ItemNamesies.LIGHT_SCREEN_TM, "A wondrous wall of light is put up to reduce damage from special attacks for five turns.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.LIGHT_SCREEN;
        }
    }

    static class ProtectTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        ProtectTM() {
            super(ItemNamesies.PROTECT_TM, "This move enables the user to protect itself from all attacks. Its chance of failing rises if it is used in succession.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.PROTECT;
        }
    }

    static class RainDanceTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        RainDanceTM() {
            super(ItemNamesies.RAIN_DANCE_TM, "The user summons a heavy rain that falls for five turns, powering up Water-type moves. It lowers the power of Fire-type moves.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.RAIN_DANCE;
        }
    }

    static class RoostTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        RoostTM() {
            super(ItemNamesies.ROOST_TM, "The user lands and rests its body. This move restores the user's HP by up to half of its max HP.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.ROOST;
        }
    }

    static class SafeguardTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        SafeguardTM() {
            super(ItemNamesies.SAFEGUARD_TM, "The user creates a protective field that prevents status conditions for five turns.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.SAFEGUARD;
        }
    }

    static class SolarBeamTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        SolarBeamTM() {
            super(ItemNamesies.SOLAR_BEAM_TM, "In this two-turn attack, the user gathers light, then blasts a bundled beam on the next turn.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.SOLAR_BEAM;
        }
    }

    static class SmackDownTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        SmackDownTM() {
            super(ItemNamesies.SMACK_DOWN_TM, "The user throws a stone or similar projectile to attack the target. A flying Pokémon will fall to the ground when it's hit.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.SMACK_DOWN;
        }
    }

    static class ThunderboltTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        ThunderboltTM() {
            super(ItemNamesies.THUNDERBOLT_TM, "A strong electric blast crashes down on the target. This may also leave the target with paralysis.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.THUNDERBOLT;
        }
    }

    static class ThunderTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        ThunderTM() {
            super(ItemNamesies.THUNDER_TM, "A wicked thunderbolt is dropped on the target to inflict damage. This may also leave the target with paralysis.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.THUNDER;
        }
    }

    static class EarthquakeTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        EarthquakeTM() {
            super(ItemNamesies.EARTHQUAKE_TM, "The user sets off an earthquake that strikes every Pokémon around it.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.EARTHQUAKE;
        }
    }

    static class LeechLifeTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        LeechLifeTM() {
            super(ItemNamesies.LEECH_LIFE_TM, "The user drains the target's blood. The user's HP is restored by half the damage taken by the target.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.LEECH_LIFE;
        }
    }

    static class PsychicTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        PsychicTM() {
            super(ItemNamesies.PSYCHIC_TM, "The target is hit by a strong telekinetic force. This may also lower the target's Sp. Def stat.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.PSYCHIC;
        }
    }

    static class ShadowBallTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        ShadowBallTM() {
            super(ItemNamesies.SHADOW_BALL_TM, "The user hurls a shadowy blob at the target. This may also lower the target's Sp. Def stat.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.SHADOW_BALL;
        }
    }

    static class BrickBreakTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        BrickBreakTM() {
            super(ItemNamesies.BRICK_BREAK_TM, "The user attacks with a swift chop. It can also break barriers, such as Light Screen and Reflect.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.BRICK_BREAK;
        }
    }

    static class DoubleTeamTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        DoubleTeamTM() {
            super(ItemNamesies.DOUBLE_TEAM_TM, "By moving rapidly, the user makes illusory copies of itself to raise its evasiveness.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.DOUBLE_TEAM;
        }
    }

    static class ReflectTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        ReflectTM() {
            super(ItemNamesies.REFLECT_TM, "A wondrous wall of light is put up to reduce damage from physical attacks for five turns.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.REFLECT;
        }
    }

    static class SludgeWaveTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        SludgeWaveTM() {
            super(ItemNamesies.SLUDGE_WAVE_TM, "The user strikes everything around it by swamping the area with a giant sludge wave. This may also poison those hit.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.SLUDGE_WAVE;
        }
    }

    static class FlamethrowerTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        FlamethrowerTM() {
            super(ItemNamesies.FLAMETHROWER_TM, "The target is scorched with an intense blast of fire. This may also leave the target with a burn.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.FLAMETHROWER;
        }
    }

    static class SludgeBombTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        SludgeBombTM() {
            super(ItemNamesies.SLUDGE_BOMB_TM, "Unsanitary sludge is hurled at the target. This may also poison the target.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.SLUDGE_BOMB;
        }
    }

    static class SandstormTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        SandstormTM() {
            super(ItemNamesies.SANDSTORM_TM, "A five-turn sandstorm is summoned to hurt all combatants except Rock, Ground, and Steel types. It raises the Sp. Def stat of Rock types.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.SANDSTORM;
        }
    }

    static class FireBlastTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        FireBlastTM() {
            super(ItemNamesies.FIRE_BLAST_TM, "The target is attacked with an intense blast of all-consuming fire. This may also leave the target with a burn.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.FIRE_BLAST;
        }
    }

    static class RockTombTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        RockTombTM() {
            super(ItemNamesies.ROCK_TOMB_TM, "Boulders are hurled at the target. This also lowers the target's Speed stat by preventing its movement.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.ROCK_TOMB;
        }
    }

    static class AerialAceTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        AerialAceTM() {
            super(ItemNamesies.AERIAL_ACE_TM, "The user confounds the target with speed, then slashes. This attack never misses.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.AERIAL_ACE;
        }
    }

    static class TormentTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        TormentTM() {
            super(ItemNamesies.TORMENT_TM, "The user torments and enrages the target, making it incapable of using the same move twice in a row.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.TORMENT;
        }
    }

    static class FacadeTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        FacadeTM() {
            super(ItemNamesies.FACADE_TM, "This attack move doubles its power if the user is poisoned, burned, or paralyzed.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.FACADE;
        }
    }

    static class FlameChargeTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        FlameChargeTM() {
            super(ItemNamesies.FLAME_CHARGE_TM, "Cloaking itself in flame, the user attacks the target. Then, building up more power, the user raises its Speed stat.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.FLAME_CHARGE;
        }
    }

    static class RestTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        RestTM() {
            super(ItemNamesies.REST_TM, "The user goes to sleep for two turns. This fully restores the user's HP and heals any status conditions.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.REST;
        }
    }

    static class AttractTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        AttractTM() {
            super(ItemNamesies.ATTRACT_TM, "If it is the opposite gender of the user, the target becomes infatuated and less likely to attack.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.ATTRACT;
        }
    }

    static class ThiefTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        ThiefTM() {
            super(ItemNamesies.THIEF_TM, "The user attacks and steals the target's held item simultaneously. The user can't steal anything if it already holds an item.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.THIEF;
        }
    }

    static class LowSweepTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        LowSweepTM() {
            super(ItemNamesies.LOW_SWEEP_TM, "The user makes a swift attack on the target's legs, which lowers the target's Speed stat.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.LOW_SWEEP;
        }
    }

    static class RoundTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        RoundTM() {
            super(ItemNamesies.ROUND_TM, "The user attacks the target with a song. Others can join in the Round to increase the power of the attack.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.ROUND;
        }
    }

    static class EchoedVoiceTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        EchoedVoiceTM() {
            super(ItemNamesies.ECHOED_VOICE_TM, "The user attacks the target with an echoing voice. If this move is used every turn, its power is increased.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.ECHOED_VOICE;
        }
    }

    static class OverheatTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        OverheatTM() {
            super(ItemNamesies.OVERHEAT_TM, "The user attacks the target at full power. The attack's recoil harshly lowers the user's Sp. Atk stat.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.OVERHEAT;
        }
    }

    static class SteelWingTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        SteelWingTM() {
            super(ItemNamesies.STEEL_WING_TM, "The target is hit with wings of steel. This may also raise the user's Defense stat.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.STEEL_WING;
        }
    }

    static class FocusBlastTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        FocusBlastTM() {
            super(ItemNamesies.FOCUS_BLAST_TM, "The user heightens its mental focus and unleashes its power. This may also lower the target's Sp. Def stat.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.FOCUS_BLAST;
        }
    }

    static class EnergyBallTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        EnergyBallTM() {
            super(ItemNamesies.ENERGY_BALL_TM, "The user draws power from nature and fires it at the target. This may also lower the target's Sp. Def stat.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.ENERGY_BALL;
        }
    }

    static class FalseSwipeTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        FalseSwipeTM() {
            super(ItemNamesies.FALSE_SWIPE_TM, "A restrained attack that prevents the target from fainting. The target is left with at least 1 HP.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.FALSE_SWIPE;
        }
    }

    static class ScaldTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        ScaldTM() {
            super(ItemNamesies.SCALD_TM, "The user shoots boiling hot water at its target. This may also leave the target with a burn.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.SCALD;
        }
    }

    static class FlingTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        FlingTM() {
            super(ItemNamesies.FLING_TM, "The user flings its held item at the target to attack. This move's power and effects depend on the item.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.FLING;
        }
    }

    static class ChargeBeamTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        ChargeBeamTM() {
            super(ItemNamesies.CHARGE_BEAM_TM, "The user attacks the target with an electric charge. The user may use any remaining electricity to raise its Sp. Atk stat.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.CHARGE_BEAM;
        }
    }

    static class SkyDropTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        SkyDropTM() {
            super(ItemNamesies.SKY_DROP_TM, "The user takes the target into the sky, then slams it into the ground.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.SKY_DROP;
        }
    }

    static class BrutalSwingTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        BrutalSwingTM() {
            super(ItemNamesies.BRUTAL_SWING_TM, "The user swings its body around violently to inflict damage on everything in its vicinity.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.BRUTAL_SWING;
        }
    }

    static class WillOWispTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        WillOWispTM() {
            super(ItemNamesies.WILL_O_WISP_TM, "The user shoots a sinister flame at the target to inflict a burn.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.WILL_O_WISP;
        }
    }

    static class AcrobaticsTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        AcrobaticsTM() {
            super(ItemNamesies.ACROBATICS_TM, "The user nimbly strikes the target. If the user is not holding an item, this attack inflicts massive damage.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.ACROBATICS;
        }
    }

    static class EmbargoTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        EmbargoTM() {
            super(ItemNamesies.EMBARGO_TM, "This move prevents the target from using its held item for five turns. Its Trainer is also prevented from using items on it.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.EMBARGO;
        }
    }

    static class ExplosionTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        ExplosionTM() {
            super(ItemNamesies.EXPLOSION_TM, "The user attacks everything around it by causing a tremendous explosion. The user faints upon using this move.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.EXPLOSION;
        }
    }

    static class ShadowClawTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        ShadowClawTM() {
            super(ItemNamesies.SHADOW_CLAW_TM, "The user slashes with a sharp claw made from shadows. Critical hits land more easily.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.SHADOW_CLAW;
        }
    }

    static class PaybackTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        PaybackTM() {
            super(ItemNamesies.PAYBACK_TM, "The user stores power, then attacks. If the user moves after the target, this attack's power will be doubled.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.PAYBACK;
        }
    }

    static class SmartStrikeTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        SmartStrikeTM() {
            super(ItemNamesies.SMART_STRIKE_TM, "The user stabs the target with a sharp horn. This attack never misses.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.SMART_STRIKE;
        }
    }

    static class GigaImpactTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        GigaImpactTM() {
            super(ItemNamesies.GIGA_IMPACT_TM, "The user charges at the target using every bit of its power. The user can't move on the next turn.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.GIGA_IMPACT;
        }
    }

    static class RockPolishTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        RockPolishTM() {
            super(ItemNamesies.ROCK_POLISH_TM, "The user polishes its body to reduce drag. This sharply raises the Speed stat.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.ROCK_POLISH;
        }
    }

    static class AuroraVeilTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        AuroraVeilTM() {
            super(ItemNamesies.AURORA_VEIL_TM, "This move reduces damage from physical and special moves for five turns. This can be used only in a hailstorm.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.AURORA_VEIL;
        }
    }

    static class StoneEdgeTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        StoneEdgeTM() {
            super(ItemNamesies.STONE_EDGE_TM, "The user stabs the target from below with sharpened stones. Critical hits land more easily.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.STONE_EDGE;
        }
    }

    static class VoltSwitchTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        VoltSwitchTM() {
            super(ItemNamesies.VOLT_SWITCH_TM, "After making its attack, the user rushes back to switch places with a party Pokémon in waiting.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.VOLT_SWITCH;
        }
    }

    static class ThunderWaveTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        ThunderWaveTM() {
            super(ItemNamesies.THUNDER_WAVE_TM, "The user launches a weak jolt of electricity that paralyzes the target.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.THUNDER_WAVE;
        }
    }

    static class GyroBallTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        GyroBallTM() {
            super(ItemNamesies.GYRO_BALL_TM, "The user tackles the target with a high-speed spin. The slower the user compared to the target, the greater the move's power.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.GYRO_BALL;
        }
    }

    static class SwordsDanceTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        SwordsDanceTM() {
            super(ItemNamesies.SWORDS_DANCE_TM, "A frenetic dance to uplift the fighting spirit. This sharply raises the user's Attack stat.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.SWORDS_DANCE;
        }
    }

    static class FlyTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        FlyTM() {
            super(ItemNamesies.FLY_TM, "The user flies up into the sky and then strikes its target on the next turn.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.FLY;
        }
    }

    static class PsychUpTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        PsychUpTM() {
            super(ItemNamesies.PSYCH_UP_TM, "The user hypnotizes itself into copying any stat change made by the target.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.PSYCH_UP;
        }
    }

    static class BulldozeTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        BulldozeTM() {
            super(ItemNamesies.BULLDOZE_TM, "The user strikes everything around it by stomping down on the ground. This lowers the Speed stat of those hit.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.BULLDOZE;
        }
    }

    static class FrostBreathTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        FrostBreathTM() {
            super(ItemNamesies.FROST_BREATH_TM, "The user blows its cold breath on the target. This attack always results in a critical hit.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.FROST_BREATH;
        }
    }

    static class RockSlideTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        RockSlideTM() {
            super(ItemNamesies.ROCK_SLIDE_TM, "Large boulders are hurled at the target to inflict damage. This may also make the target flinch.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.ROCK_SLIDE;
        }
    }

    static class XScissorTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        XScissorTM() {
            super(ItemNamesies.X_SCISSOR_TM, "The user slashes at the target by crossing its scythes or claws as if they were a pair of scissors.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.X_SCISSOR;
        }
    }

    static class DragonTailTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        DragonTailTM() {
            super(ItemNamesies.DRAGON_TAIL_TM, "The target is knocked away, and a different Pokémon is dragged out. In the wild, this ends a battle against a single Pokémon.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.DRAGON_TAIL;
        }
    }

    static class InfestationTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        InfestationTM() {
            super(ItemNamesies.INFESTATION_TM, "The target is infested and attacked for four to five turns. The target can't flee during this time.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.INFESTATION;
        }
    }

    static class PoisonJabTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        PoisonJabTM() {
            super(ItemNamesies.POISON_JAB_TM, "The target is stabbed with a tentacle, arm, or the like steeped in poison. This may also poison the target.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.POISON_JAB;
        }
    }

    static class DreamEaterTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        DreamEaterTM() {
            super(ItemNamesies.DREAM_EATER_TM, "The user eats the dreams of a sleeping target. The user's HP is restored by half the damage taken by the target.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.DREAM_EATER;
        }
    }

    static class GrassKnotTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        GrassKnotTM() {
            super(ItemNamesies.GRASS_KNOT_TM, "The user snares the target with grass and trips it. The heavier the target, the greater the move's power.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.GRASS_KNOT;
        }
    }

    static class SwaggerTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        SwaggerTM() {
            super(ItemNamesies.SWAGGER_TM, "The user enrages and confuses the target. However, this also sharply raises the target's Attack stat.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.SWAGGER;
        }
    }

    static class SleepTalkTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        SleepTalkTM() {
            super(ItemNamesies.SLEEP_TALK_TM, "While it is asleep, the user randomly uses one of the moves it knows.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.SLEEP_TALK;
        }
    }

    static class UTurnTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        UTurnTM() {
            super(ItemNamesies.U_TURN_TM, "After making its attack, the user rushes back to switch places with a party Pokémon in waiting.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.U_TURN;
        }
    }

    static class SubstituteTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        SubstituteTM() {
            super(ItemNamesies.SUBSTITUTE_TM, "The user creates a substitute for itself using some of its HP. The substitute serves as the user's decoy.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.SUBSTITUTE;
        }
    }

    static class FlashCannonTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        FlashCannonTM() {
            super(ItemNamesies.FLASH_CANNON_TM, "The user gathers all its light energy and releases it all at once. This may also lower the target's Sp. Def stat.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.FLASH_CANNON;
        }
    }

    static class TrickRoomTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        TrickRoomTM() {
            super(ItemNamesies.TRICK_ROOM_TM, "The user creates a bizarre area in which slower Pokémon get to move first for five turns.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.TRICK_ROOM;
        }
    }

    static class WildChargeTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        WildChargeTM() {
            super(ItemNamesies.WILD_CHARGE_TM, "The user shrouds itself in electricity and smashes into its target. This also damages the user a little.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.WILD_CHARGE;
        }
    }

    static class SurfTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        SurfTM() {
            super(ItemNamesies.SURF_TM, "The user attacks everything around it by swamping its surroundings with a giant wave.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.SURF;
        }
    }

    static class SnarlTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        SnarlTM() {
            super(ItemNamesies.SNARL_TM, "The user yells as if it's ranting about something, which lowers the Sp. Atk stat of the target.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.SNARL;
        }
    }

    static class NaturePowerTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        NaturePowerTM() {
            super(ItemNamesies.NATURE_POWER_TM, "This attack makes use of nature's power. Its effects vary depending on the user's environment.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.NATURE_POWER;
        }
    }

    static class DarkPulseTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        DarkPulseTM() {
            super(ItemNamesies.DARK_PULSE_TM, "The user releases a horrible aura imbued with dark thoughts. This may also make the target flinch.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.DARK_PULSE;
        }
    }

    static class WaterfallTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        WaterfallTM() {
            super(ItemNamesies.WATERFALL_TM, "The user charges at the target and may make it flinch.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.WATERFALL;
        }
    }

    static class DazzlingGleamTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        DazzlingGleamTM() {
            super(ItemNamesies.DAZZLING_GLEAM_TM, "The user damages the target by emitting a powerful flash.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.DAZZLING_GLEAM;
        }
    }

    static class ConfideTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        ConfideTM() {
            super(ItemNamesies.CONFIDE_TM, "The user tells the target a secret, and the target loses its ability to concentrate. This lowers the target's Sp. Atk stat.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.CONFIDE;
        }
    }

    static class HardyMint extends Item implements NatureMint {
        private static final long serialVersionUID = 1L;

        HardyMint() {
            super(ItemNamesies.HARDY_MINT, "When a Pokémon smells this mint, all of its stats will grow at an equal rate.", BagCategory.MISC);
            super.price = 20;
        }

        @Override
        public Nature getNature() {
            return Nature.HARDY;
        }
    }

    static class LonelyMint extends Item implements NatureMint {
        private static final long serialVersionUID = 1L;

        LonelyMint() {
            super(ItemNamesies.LONELY_MINT, "When a Pokémon smells this mint, its Attack will grow more easily, but its Defense will grow more slowly.", BagCategory.MISC);
            super.price = 20;
        }

        @Override
        public Nature getNature() {
            return Nature.LONELY;
        }
    }

    static class AdamantMint extends Item implements NatureMint {
        private static final long serialVersionUID = 1L;

        AdamantMint() {
            super(ItemNamesies.ADAMANT_MINT, "When a Pokémon smells this mint, its Attack will grow more easily, but its Sp. Attack will grow more slowly.", BagCategory.MISC);
            super.price = 20;
        }

        @Override
        public Nature getNature() {
            return Nature.ADAMANT;
        }
    }

    static class NaughtyMint extends Item implements NatureMint {
        private static final long serialVersionUID = 1L;

        NaughtyMint() {
            super(ItemNamesies.NAUGHTY_MINT, "When a Pokémon smells this mint, its Attack will grow more easily, but its Sp. Defense will grow more slowly.", BagCategory.MISC);
            super.price = 20;
        }

        @Override
        public Nature getNature() {
            return Nature.NAUGHTY;
        }
    }

    static class BraveMint extends Item implements NatureMint {
        private static final long serialVersionUID = 1L;

        BraveMint() {
            super(ItemNamesies.BRAVE_MINT, "When a Pokémon smells this mint, its Attack will grow more easily, but its Speed will grow more slowly.", BagCategory.MISC);
            super.price = 20;
        }

        @Override
        public Nature getNature() {
            return Nature.BRAVE;
        }
    }

    static class BoldMint extends Item implements NatureMint {
        private static final long serialVersionUID = 1L;

        BoldMint() {
            super(ItemNamesies.BOLD_MINT, "When a Pokémon smells this mint, its Defense will grow more easily, but its Attack will grow more slowly.", BagCategory.MISC);
            super.price = 20;
        }

        @Override
        public Nature getNature() {
            return Nature.BOLD;
        }
    }

    static class DocileMint extends Item implements NatureMint {
        private static final long serialVersionUID = 1L;

        DocileMint() {
            super(ItemNamesies.DOCILE_MINT, "When a Pokémon smells this mint, all of its stats will grow at an equal rate.", BagCategory.MISC);
            super.price = 20;
        }

        @Override
        public Nature getNature() {
            return Nature.DOCILE;
        }
    }

    static class ImpishMint extends Item implements NatureMint {
        private static final long serialVersionUID = 1L;

        ImpishMint() {
            super(ItemNamesies.IMPISH_MINT, "When a Pokémon smells this mint, its Defense will grow more easily, but its Sp. Attack will grow more slowly.", BagCategory.MISC);
            super.price = 20;
        }

        @Override
        public Nature getNature() {
            return Nature.IMPISH;
        }
    }

    static class LaxMint extends Item implements NatureMint {
        private static final long serialVersionUID = 1L;

        LaxMint() {
            super(ItemNamesies.LAX_MINT, "When a Pokémon smells this mint, its Defense will grow more easily, but its Sp. Defense will grow more slowly.", BagCategory.MISC);
            super.price = 20;
        }

        @Override
        public Nature getNature() {
            return Nature.LAX;
        }
    }

    static class RelaxedMint extends Item implements NatureMint {
        private static final long serialVersionUID = 1L;

        RelaxedMint() {
            super(ItemNamesies.RELAXED_MINT, "When a Pokémon smells this mint, its Defense will grow more easily, but its Speed will grow more slowly.", BagCategory.MISC);
            super.price = 20;
        }

        @Override
        public Nature getNature() {
            return Nature.RELAXED;
        }
    }

    static class ModestMint extends Item implements NatureMint {
        private static final long serialVersionUID = 1L;

        ModestMint() {
            super(ItemNamesies.MODEST_MINT, "When a Pokémon smells this mint, its Sp. Attack will grow more easily, but its Attack will grow more slowly.", BagCategory.MISC);
            super.price = 20;
        }

        @Override
        public Nature getNature() {
            return Nature.MODEST;
        }
    }

    static class MildMint extends Item implements NatureMint {
        private static final long serialVersionUID = 1L;

        MildMint() {
            super(ItemNamesies.MILD_MINT, "When a Pokémon smells this mint, its Sp. Attack will grow more easily, but its Defense will grow more slowly.", BagCategory.MISC);
            super.price = 20;
        }

        @Override
        public Nature getNature() {
            return Nature.MILD;
        }
    }

    static class BashfulMint extends Item implements NatureMint {
        private static final long serialVersionUID = 1L;

        BashfulMint() {
            super(ItemNamesies.BASHFUL_MINT, "When a Pokémon smells this mint, all of its stats will grow at an equal rate.", BagCategory.MISC);
            super.price = 20;
        }

        @Override
        public Nature getNature() {
            return Nature.BASHFUL;
        }
    }

    static class RashMint extends Item implements NatureMint {
        private static final long serialVersionUID = 1L;

        RashMint() {
            super(ItemNamesies.RASH_MINT, "When a Pokémon smells this mint, its Sp. Attack will grow more easily, but its Sp. Defense will grow more slowly.", BagCategory.MISC);
            super.price = 20;
        }

        @Override
        public Nature getNature() {
            return Nature.RASH;
        }
    }

    static class QuietMint extends Item implements NatureMint {
        private static final long serialVersionUID = 1L;

        QuietMint() {
            super(ItemNamesies.QUIET_MINT, "When a Pokémon smells this mint, its Sp. Attack will grow more easily, but its Speed will grow more slowly.", BagCategory.MISC);
            super.price = 20;
        }

        @Override
        public Nature getNature() {
            return Nature.QUIET;
        }
    }

    static class CalmMint extends Item implements NatureMint {
        private static final long serialVersionUID = 1L;

        CalmMint() {
            super(ItemNamesies.CALM_MINT, "When a Pokémon smells this mint, its Sp. Defense will grow more easily, but its Attack will grow more slowly.", BagCategory.MISC);
            super.price = 20;
        }

        @Override
        public Nature getNature() {
            return Nature.CALM;
        }
    }

    static class GentleMint extends Item implements NatureMint {
        private static final long serialVersionUID = 1L;

        GentleMint() {
            super(ItemNamesies.GENTLE_MINT, "When a Pokémon smells this mint, its Sp. Defense will grow more easily, but its Defense will grow more slowly.", BagCategory.MISC);
            super.price = 20;
        }

        @Override
        public Nature getNature() {
            return Nature.GENTLE;
        }
    }

    static class CarefulMint extends Item implements NatureMint {
        private static final long serialVersionUID = 1L;

        CarefulMint() {
            super(ItemNamesies.CAREFUL_MINT, "When a Pokémon smells this mint, its Sp. Defense will grow more easily, but its Sp. Attack will grow more slowly.", BagCategory.MISC);
            super.price = 20;
        }

        @Override
        public Nature getNature() {
            return Nature.CAREFUL;
        }
    }

    static class QuirkyMint extends Item implements NatureMint {
        private static final long serialVersionUID = 1L;

        QuirkyMint() {
            super(ItemNamesies.QUIRKY_MINT, "When a Pokémon smells this mint, all of its stats will grow at an equal rate.", BagCategory.MISC);
            super.price = 20;
        }

        @Override
        public Nature getNature() {
            return Nature.QUIRKY;
        }
    }

    static class SassyMint extends Item implements NatureMint {
        private static final long serialVersionUID = 1L;

        SassyMint() {
            super(ItemNamesies.SASSY_MINT, "When a Pokémon smells this mint, its Sp. Defense will grow more easily, but its Speed will grow more slowly.", BagCategory.MISC);
            super.price = 20;
        }

        @Override
        public Nature getNature() {
            return Nature.SASSY;
        }
    }

    static class TimidMint extends Item implements NatureMint {
        private static final long serialVersionUID = 1L;

        TimidMint() {
            super(ItemNamesies.TIMID_MINT, "When a Pokémon smells this mint, its Speed will grow more easily, but its Attack will grow more slowly.", BagCategory.MISC);
            super.price = 20;
        }

        @Override
        public Nature getNature() {
            return Nature.TIMID;
        }
    }

    static class HastyMint extends Item implements NatureMint {
        private static final long serialVersionUID = 1L;

        HastyMint() {
            super(ItemNamesies.HASTY_MINT, "When a Pokémon smells this mint, its Speed will grow more easily, but its Defense will grow more slowly.", BagCategory.MISC);
            super.price = 20;
        }

        @Override
        public Nature getNature() {
            return Nature.HASTY;
        }
    }

    static class JollyMint extends Item implements NatureMint {
        private static final long serialVersionUID = 1L;

        JollyMint() {
            super(ItemNamesies.JOLLY_MINT, "When a Pokémon smells this mint, its Speed will grow more easily, but its Sp. Attack will grow more slowly.", BagCategory.MISC);
            super.price = 20;
        }

        @Override
        public Nature getNature() {
            return Nature.JOLLY;
        }
    }

    static class NaiveMint extends Item implements NatureMint {
        private static final long serialVersionUID = 1L;

        NaiveMint() {
            super(ItemNamesies.NAIVE_MINT, "When a Pokémon smells this mint, its Speed will grow more easily, but its Sp. Defense will grow more slowly.", BagCategory.MISC);
            super.price = 20;
        }

        @Override
        public Nature getNature() {
            return Nature.NAIVE;
        }
    }

    static class SeriousMint extends Item implements NatureMint {
        private static final long serialVersionUID = 1L;

        SeriousMint() {
            super(ItemNamesies.SERIOUS_MINT, "When a Pokémon smells this mint, all of its stats will grow at an equal rate.", BagCategory.MISC);
            super.price = 20;
        }

        @Override
        public Nature getNature() {
            return Nature.SERIOUS;
        }
    }
}
