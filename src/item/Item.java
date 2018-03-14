package item;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.attack.MoveCategory;
import battle.attack.MoveType;
import battle.effect.CastSource;
import battle.effect.EffectInterfaces.ApplyDamageEffect;
import battle.effect.EffectInterfaces.AttackBlocker;
import battle.effect.EffectInterfaces.AttackSelectionEffect;
import battle.effect.EffectInterfaces.BracingEffect;
import battle.effect.EffectInterfaces.CritStageEffect;
import battle.effect.EffectInterfaces.DefendingNoAdvantageChanger;
import battle.effect.EffectInterfaces.DefiniteEscape;
import battle.effect.EffectInterfaces.EffectCurerItem;
import battle.effect.EffectInterfaces.EffectReceivedEffect;
import battle.effect.EffectInterfaces.EndTurnEffect;
import battle.effect.EffectInterfaces.EntryEffect;
import battle.effect.EffectInterfaces.EntryEndTurnEffect;
import battle.effect.EffectInterfaces.GroundedEffect;
import battle.effect.EffectInterfaces.HalfWeightEffect;
import battle.effect.EffectInterfaces.ItemSwapperEffect;
import battle.effect.EffectInterfaces.LevitationEffect;
import battle.effect.EffectInterfaces.OpponentApplyDamageEffect;
import battle.effect.EffectInterfaces.OpponentTakeDamageEffect;
import battle.effect.EffectInterfaces.PhysicalContactEffect;
import battle.effect.EffectInterfaces.PowderMove;
import battle.effect.EffectInterfaces.PowerChangeEffect;
import battle.effect.EffectInterfaces.RepellingEffect;
import battle.effect.EffectInterfaces.SimpleStatModifyingEffect;
import battle.effect.EffectInterfaces.StallingEffect;
import battle.effect.EffectInterfaces.StatProtectingEffect;
import battle.effect.EffectInterfaces.StrikeFirstEffect;
import battle.effect.EffectInterfaces.TakeDamageEffect;
import battle.effect.EffectInterfaces.TerrainCastEffect;
import battle.effect.EffectInterfaces.WeatherBlockerEffect;
import battle.effect.EffectInterfaces.WeatherExtendingEffect;
import battle.effect.EffectNamesies;
import battle.effect.MessageGetter;
import battle.effect.battle.weather.WeatherNamesies;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.status.StatusNamesies;
import battle.effect.team.TeamEffectNamesies;
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
import item.medicine.FixedHpHealer;
import item.medicine.HpHealer;
import item.medicine.PPHealer;
import item.medicine.RepelItem;
import item.medicine.StatusHealer;
import item.use.BallItem;
import item.use.BattlePokemonUseItem;
import item.use.BattleUseItem;
import item.use.EvolutionItem;
import item.use.MoveUseItem;
import item.use.PlayerUseItem;
import item.use.PokemonUseItem;
import item.use.TechnicalMachine;
import item.use.UseItem;
import main.Game;
import map.overworld.TerrainType;
import map.overworld.WildEncounter;
import message.MessageUpdate;
import message.Messages;
import pokemon.Gender;
import pokemon.PokemonNamesies;
import pokemon.Stat;
import pokemon.ability.Ability;
import pokemon.ability.AbilityNamesies;
import pokemon.evolution.EvolutionMethod;
import trainer.Trainer;
import type.Type;
import type.TypeAdvantage;
import util.RandomUtils;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Item implements ItemInterface, Comparable<Item> {
    private static final long serialVersionUID = 1L;

    protected final ItemNamesies namesies;
    private String description;
    private BagCategory bagCategory;
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
    public InvokeSource getSource() {
        return InvokeSource.ITEM;
    }

    @Override
    public int compareTo(Item o) {
        return this.getName().compareTo(o.getName());
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public boolean isUsable() {
        return this instanceof UseItem;
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

    public BagCategory getBagCategory() {
        return this.bagCategory;
    }

    public Iterable<BattleBagCategory> getBattleBagCategories() {
        return this.battleBagCategories;
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

    public static boolean isItem(String itemName) {
        return ItemNamesies.tryValueOf(itemName) != null;
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
            super(ItemNamesies.BICYCLE, "A folding Bike that enables a rider to get around much faster than with Running Shoes.", BagCategory.KEY_ITEM);
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
            super(ItemNamesies.FISHING_ROD, "A multi-purpose, do-it-all kind of fishing rod. The kind you can use wherever you want. Except on land.", BagCategory.KEY_ITEM);
        }
    }

    static class AbsorbBulb extends Item implements HoldItem, TakeDamageEffect {
        private static final long serialVersionUID = 1L;

        AbsorbBulb() {
            super(ItemNamesies.ABSORB_BULB, "An item to be held by a Pok\u00e9mon. It boosts Sp. Atk if hit with a Water-type attack. It can only be used once.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (user.isAttackType(Type.WATER) && victim.getStages().modifyStage(victim, 1, Stat.SP_ATTACK, b, CastSource.HELD_ITEM)) {
                this.consumeItem(b, victim);
            }
        }
    }

    static class AirBalloon extends Item implements HoldItem, LevitationEffect, TakeDamageEffect, EntryEffect {
        private static final long serialVersionUID = 1L;

        AirBalloon() {
            super(ItemNamesies.AIR_BALLOON, "An item to be held by a Pok\u00e9mon. The holder will float in the air until hit. Once hit, this item will burst.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void enter(Battle b, ActivePokemon enterer) {
            Messages.add(enterer.getName() + " floats with its " + this.getName() + "!");
        }

        @Override
        public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            Messages.add(victim.getName() + "'s " + this.getName() + " popped!");
            this.consumeItem(b, victim);
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
            TeamEffectNamesies.GET_DAT_CASH_MONEY_TWICE.getEffect().cast(b, gettinDatCashMoneyTwice, gettinDatCashMoneyTwice, CastSource.HELD_ITEM, false);
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
            super(ItemNamesies.BIG_ROOT, "An item to be held by a Pok\u00e9mon. It boosts the amount of HP the holder recovers from HP-stealing moves.", BagCategory.MISC);
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
            super(ItemNamesies.BINDING_BAND, "An item to be held by a Pok\u00e9mon. A band that increases the power of binding moves used by the holder.", BagCategory.MISC);
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
                // Don't heal if at full health
                if (victim.fullHealth()) {
                    return;
                }

                victim.healHealthFraction(1/16.0);
                Messages.add(new MessageUpdate(victim.getName() + "'s HP was restored by its " + this.getName() + "!").updatePokemon(b, victim));
            } else if (!victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
                Messages.add(victim.getName() + " lost some of its HP due to its " + this.getName() + "!");
                victim.reduceHealthFraction(b, 1/8.0);
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
        public boolean isModifyStat(Stat s) {
            return s == Stat.EVASION;
        }

        @Override
        public int flingDamage() {
            return 10;
        }

        @Override
        public double getModifier() {
            return 1.1;
        }
    }

    static class CellBattery extends Item implements HoldItem, TakeDamageEffect {
        private static final long serialVersionUID = 1L;

        CellBattery() {
            super(ItemNamesies.CELL_BATTERY, "An item to be held by a Pok\u00e9mon. It boosts Attack if hit with an Electric-type attack. It can only be used once.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (user.isAttackType(Type.ELECTRIC) && victim.getStages().modifyStage(victim, 1, Stat.ATTACK, b, CastSource.HELD_ITEM)) {
                this.consumeItem(b, victim);
            }
        }
    }

    static class ChoiceBand extends Item implements AttackSelectionEffect, HoldItem, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        ChoiceBand() {
            super(ItemNamesies.CHOICE_BAND, "An item to be held by a Pok\u00e9mon. This curious headband boosts Attack but only allows the use of one move.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.ATTACK;
        }

        @Override
        public boolean usable(Battle b, ActivePokemon p, Move m) {
            Move last = p.getLastMoveUsed();
            return last == null || m == last;
        }

        @Override
        public String getUnusableMessage(Battle b, ActivePokemon p) {
            return p.getName() + "'s " + this.getName() + " only allows " + p.getLastMoveUsed().getAttack().getName() + " to be used!";
        }

        @Override
        public int flingDamage() {
            return 10;
        }

        @Override
        public double getModifier() {
            return 1.5;
        }
    }

    static class ChoiceScarf extends Item implements AttackSelectionEffect, HoldItem, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        ChoiceScarf() {
            super(ItemNamesies.CHOICE_SCARF, "An item to be held by a Pok\u00e9mon. This curious scarf boosts Speed but only allows the use of one move.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
        }

        @Override
        public boolean usable(Battle b, ActivePokemon p, Move m) {
            Move last = p.getLastMoveUsed();
            return last == null || m == last;
        }

        @Override
        public String getUnusableMessage(Battle b, ActivePokemon p) {
            return p.getName() + "'s " + this.getName() + " only allows " + p.getLastMoveUsed().getAttack().getName() + " to be used!";
        }

        @Override
        public int flingDamage() {
            return 10;
        }

        @Override
        public double getModifier() {
            return 1.5;
        }
    }

    static class ChoiceSpecs extends Item implements AttackSelectionEffect, HoldItem, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        ChoiceSpecs() {
            super(ItemNamesies.CHOICE_SPECS, "An item to be held by a Pok\u00e9mon. These curious glasses boost Sp. Atk but only allow the use of one move.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SP_ATTACK;
        }

        @Override
        public boolean usable(Battle b, ActivePokemon p, Move m) {
            Move last = p.getLastMoveUsed();
            return last == null || m == last;
        }

        @Override
        public String getUnusableMessage(Battle b, ActivePokemon p) {
            return p.getName() + "'s " + this.getName() + " only allows " + p.getLastMoveUsed().getAttack().getName() + " to be used!";
        }

        @Override
        public int flingDamage() {
            return 10;
        }

        @Override
        public double getModifier() {
            return 1.5;
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
        public int flingDamage() {
            return 60;
        }

        @Override
        public int getExtensionTurns(WeatherNamesies weatherType) {
            return weatherType == WeatherNamesies.RAINING ? 3 : 0;
        }
    }

    static class HeatRock extends Item implements HoldItem, WeatherExtendingEffect {
        private static final long serialVersionUID = 1L;

        HeatRock() {
            super(ItemNamesies.HEAT_ROCK, "An item to be held by a Pok\u00e9mon. It extends the duration of the move Sunny Day when used by the holder.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public int flingDamage() {
            return 60;
        }

        @Override
        public int getExtensionTurns(WeatherNamesies weatherType) {
            return weatherType == WeatherNamesies.SUNNY ? 3 : 0;
        }
    }

    static class IcyRock extends Item implements HoldItem, WeatherExtendingEffect {
        private static final long serialVersionUID = 1L;

        IcyRock() {
            super(ItemNamesies.ICY_ROCK, "An item to be held by a Pok\u00e9mon. It extends the duration of the move Hail when used by the holder.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public int flingDamage() {
            return 40;
        }

        @Override
        public int getExtensionTurns(WeatherNamesies weatherType) {
            return weatherType == WeatherNamesies.HAILING ? 3 : 0;
        }
    }

    static class SmoothRock extends Item implements HoldItem, WeatherExtendingEffect {
        private static final long serialVersionUID = 1L;

        SmoothRock() {
            super(ItemNamesies.SMOOTH_ROCK, "An item to be held by a Pok\u00e9mon. It extends the duration of the move Sandstorm when used by the holder.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public int flingDamage() {
            return 10;
        }

        @Override
        public int getExtensionTurns(WeatherNamesies weatherType) {
            return weatherType == WeatherNamesies.SANDSTORM ? 3 : 0;
        }
    }

    static class EjectButton extends Item implements HoldItem, TakeDamageEffect {
        private static final long serialVersionUID = 1L;

        EjectButton() {
            super(ItemNamesies.EJECT_BUTTON, "An item to be held by a Pok\u00e9mon. If the holder is hit by an attack, it will be switched out of battle.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
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
        public int flingDamage() {
            return 10;
        }

        @Override
        public void receiveEffect(Battle b, ActivePokemon caster, ActivePokemon victim, EffectNamesies effectType) {
            if (effectType == PokemonEffectNamesies.INFATUATED && PokemonEffectNamesies.INFATUATED.getEffect().apply(b, victim, caster, CastSource.HELD_ITEM, false)) {
                Messages.add(victim.getName() + "'s " + this.getName() + " caused " + caster.getName() + " to fall in love!");
            }
        }
    }

    static class ExpertBelt extends Item implements HoldItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        ExpertBelt() {
            super(ItemNamesies.EXPERT_BELT, "An item to be held by a Pok\u00e9mon. It's a well-worn belt that slightly boosts the power of supereffective moves.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return TypeAdvantage.isSuperEffective(user, victim, b) ? 1.2 : 1;
        }

        @Override
        public int flingDamage() {
            return 10;
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

        @Override
        public int getHalfAmount(int halfAmount) {
            return halfAmount + 1;
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

    static class GripClaw extends Item implements HoldItem {
        private static final long serialVersionUID = 1L;

        GripClaw() {
            super(ItemNamesies.GRIP_CLAW, "An item to be held by a Pok\u00e9mon. It extends the duration of multi-turn attacks like Bind and Wrap.", BagCategory.MISC);
            super.price = 4000;
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
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isPokemon(PokemonNamesies.DIALGA) && (user.isAttackType(Type.DRAGON) || user.isAttackType(Type.STEEL)) ? 1.2 : 1;
        }

        @Override
        public int flingDamage() {
            return 60;
        }
    }

    static class LustrousOrb extends Item implements HoldItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        LustrousOrb() {
            super(ItemNamesies.LUSTROUS_ORB, "A beautifully glowing orb to be held by Palkia. It boosts the power of Dragon- and Water-type moves when it is held.", BagCategory.MISC);
            super.price = 10000;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isPokemon(PokemonNamesies.PALKIA) && (user.isAttackType(Type.DRAGON) || user.isAttackType(Type.WATER)) ? 1.2 : 1;
        }

        @Override
        public int flingDamage() {
            return 60;
        }
    }

    static class GriseousOrb extends Item implements HoldItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        GriseousOrb() {
            super(ItemNamesies.GRISEOUS_ORB, "A glowing orb to be held by Giratina. It boosts the power of Dragon- and Ghost-type moves when it is held.", BagCategory.MISC);
            super.price = 10000;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isPokemon(PokemonNamesies.GIRATINA) && (user.isAttackType(Type.DRAGON) || user.isAttackType(Type.GHOST)) ? 1.2 : 1;
        }

        @Override
        public int flingDamage() {
            return 60;
        }
    }

    static class IronBall extends Item implements HoldItem, GroundedEffect, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        IronBall() {
            super(ItemNamesies.IRON_BALL, "An item to be held by a Pok\u00e9mon. It lowers Speed and allows Ground-type moves to hit Flying-type and levitating holders.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
        }

        @Override
        public int flingDamage() {
            return 130;
        }

        @Override
        public void flingEffect(Battle b, ActivePokemon pelted) {
            // Technically the Iron Ball doesn't do this as a fling effect, but it almost makes sense so I'm doing it
            removeLevitation(b, pelted);
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
            super(ItemNamesies.LIFE_ORB, "An item to be held by a Pok\u00e9mon. It boosts the power of moves, but at the cost of some HP on each hit.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return 5324.0/4096.0;
        }

        @Override
        public void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
            if (user.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
                return;
            }

            Messages.add(user.getName() + " was hurt by its " + this.getName() + "!");
            user.reduceHealthFraction(b, .1);
        }
    }

    static class LightBall extends Item implements HoldItem, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        LightBall() {
            super(ItemNamesies.LIGHT_BALL, "An item to be held by Pikachu. It's a puzzling orb that boosts its Attack and Sp. Atk stats.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return p.isPokemon(PokemonNamesies.PIKACHU);
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
        public double getModifier() {
            return 2;
        }
    }

    static class LightClay extends Item implements HoldItem {
        private static final long serialVersionUID = 1L;

        LightClay() {
            super(ItemNamesies.LIGHT_CLAY, "An item to be held by a Pok\u00e9mon. Protective moves like Light Screen and Reflect will be effective longer.", BagCategory.MISC);
            super.price = 4000;
        }
    }

    static class LuckyEgg extends Item implements HoldItem {
        private static final long serialVersionUID = 1L;

        LuckyEgg() {
            super(ItemNamesies.LUCKY_EGG, "An item to be held by a Pok\u00e9mon. It's an egg filled with happiness that earns extra Exp. Points in battle.", BagCategory.MISC);
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
        public int increaseCritStage(int stage, ActivePokemon p) {
            if (p.isPokemon(PokemonNamesies.CHANSEY)) {
                return stage + 2;
            }

            return stage;
        }

        @Override
        public int flingDamage() {
            return 40;
        }
    }

    static class LuminousMoss extends Item implements HoldItem, TakeDamageEffect {
        private static final long serialVersionUID = 1L;

        LuminousMoss() {
            super(ItemNamesies.LUMINOUS_MOSS, "An item to be held by a Pok\u00e9mon. It boosts Sp. Def if hit with a Water-type attack. It can only be used once.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (user.isAttackType(Type.WATER) && victim.getStages().modifyStage(victim, 1, Stat.SP_DEFENSE, b, CastSource.HELD_ITEM)) {
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
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
        }

        @Override
        public int flingDamage() {
            return 60;
        }

        @Override
        public int[] getEVs(int[] vals) {
            for (int i = 0; i < vals.length; i++) {
                vals[i] *= 2;
            }

            return vals;
        }

        @Override
        public double getModifier() {
            return .5;
        }
    }

    static class MentalHerb extends Item implements HoldItem, EffectCurerItem {
        private static final long serialVersionUID = 1L;

        private static final Map<PokemonEffectNamesies, String> REMOVEABLE_EFFECTS = new EnumMap<>(PokemonEffectNamesies.class);
        static {
            REMOVEABLE_EFFECTS.put(PokemonEffectNamesies.INFATUATED, "infatuated");
            REMOVEABLE_EFFECTS.put(PokemonEffectNamesies.DISABLE, "disabled");
            REMOVEABLE_EFFECTS.put(PokemonEffectNamesies.TAUNT, "under the effects of taunt");
            REMOVEABLE_EFFECTS.put(PokemonEffectNamesies.ENCORE, "under the effects of encore");
            REMOVEABLE_EFFECTS.put(PokemonEffectNamesies.TORMENT, "under the effects of torment");
            REMOVEABLE_EFFECTS.put(PokemonEffectNamesies.CONFUSION, "confused");
            REMOVEABLE_EFFECTS.put(PokemonEffectNamesies.HEAL_BLOCK, "under the effects of heal block");
        }

        MentalHerb() {
            super(ItemNamesies.MENTAL_HERB, "An item to be held by a Pok\u00e9mon. The holder shakes off move-binding effects to move freely. It can be used only once.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public int flingDamage() {
            return 10;
        }

        @Override
        public void flingEffect(Battle b, ActivePokemon pelted) {
            usesies(pelted);
        }

        @Override
        public Set<PokemonEffectNamesies> getCurableEffects() {
            return REMOVEABLE_EFFECTS.keySet();
        }

        @Override
        public String getRemoveMessage(ActivePokemon victim, PokemonEffectNamesies effectType) {
            return victim.getName() + " is no longer " + REMOVEABLE_EFFECTS.get(effectType) + " due to its " + this.getName() + "!";
        }
    }

    static class MetalPowder extends Item implements HoldItem, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        MetalPowder() {
            super(ItemNamesies.METAL_POWDER, "An item to be held by Ditto. Extremely fine yet hard, this odd powder boosts the Defense stat.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return p.isPokemon(PokemonNamesies.DITTO);
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.DEFENSE || s == Stat.SP_DEFENSE;
        }

        @Override
        public int flingDamage() {
            return 10;
        }

        @Override
        public double getModifier() {
            return 1.5;
        }
    }

    static class Metronome extends Item implements HoldItem, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Metronome() {
            super(ItemNamesies.METRONOME, "An item to be held by a Pok\u00e9mon. It boosts moves used consecutively, but only until a different move is used.", BagCategory.MISC);
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
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getAttack().getCategory() == MoveCategory.PHYSICAL ? 1.1 : 1;
        }

        @Override
        public int flingDamage() {
            return 10;
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
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
        }

        @Override
        public int flingDamage() {
            return 70;
        }

        @Override
        public int[] getEVs(int[] vals) {
            vals[powerStat().index()] += 4;
            return vals;
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
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
        }

        @Override
        public int flingDamage() {
            return 70;
        }

        @Override
        public int[] getEVs(int[] vals) {
            vals[powerStat().index()] += 4;
            return vals;
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
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
        }

        @Override
        public int flingDamage() {
            return 70;
        }

        @Override
        public int[] getEVs(int[] vals) {
            vals[powerStat().index()] += 4;
            return vals;
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
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
        }

        @Override
        public int flingDamage() {
            return 70;
        }

        @Override
        public int[] getEVs(int[] vals) {
            vals[powerStat().index()] += 4;
            return vals;
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
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
        }

        @Override
        public int flingDamage() {
            return 70;
        }

        @Override
        public int[] getEVs(int[] vals) {
            vals[powerStat().index()] += 4;
            return vals;
        }

        @Override
        public double getModifier() {
            return .5;
        }
    }

    static class PowerWeight extends Item implements PowerItem, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        PowerWeight() {
            super(ItemNamesies.POWER_WEIGHT, "An item to be held by a Pok\u00e9mon. It reduces Speed but allows the holder's maximum HP to grow more after battling.", BagCategory.MISC);
            super.price = 3000;
        }

        @Override
        public Stat powerStat() {
            return Stat.HP;
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
        public int[] getEVs(int[] vals) {
            vals[powerStat().index()] += 4;
            return vals;
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
        public int flingDamage() {
            return 80;
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
    }

    static class QuickPowder extends Item implements HoldItem, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        QuickPowder() {
            super(ItemNamesies.QUICK_POWDER, "An item to be held by Ditto. Extremely fine yet hard, this odd powder boosts the Speed stat.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return p.isPokemon(PokemonNamesies.DITTO);
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SPEED;
        }

        @Override
        public int flingDamage() {
            return 10;
        }

        @Override
        public double getModifier() {
            return 1.5;
        }
    }

    static class RedCard extends Item implements HoldItem, OpponentApplyDamageEffect {
        private static final long serialVersionUID = 1L;

        RedCard() {
            super(ItemNamesies.RED_CARD, "An item to be held by a Pok\u00e9mon. When the holder is hit by an attack, the attacker is removed from battle.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
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
        public int flingDamage() {
            return 10;
        }

        @Override
        public boolean negateNoAdvantage(Type attacking, Type defending) {
            return true;
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
            Messages.add(user.getName() + " was hurt by " + victim.getName() + "'s " + this.getName() + "!");
            user.reduceHealthFraction(b, 1/8.0);
        }

        @Override
        public int flingDamage() {
            return 60;
        }
    }

    static class SafetyGoggles extends Item implements HoldItem, WeatherBlockerEffect, AttackBlocker {
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
        public boolean block(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getAttack() instanceof PowderMove;
        }

        @Override
        public String getBlockMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
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

    static class ShedShell extends Item implements HoldItem {
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
            super(ItemNamesies.SHELL_BELL, "An item to be held by a Pok\u00e9mon. The holder regains a little HP every time it inflicts damage on others.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
            if (user.fullHealth() || user.getAttack().isMoveType(MoveType.USER_FAINTS)) {
                return;
            }

            user.heal((int)Math.ceil(damage/8.0));
            Messages.add(new MessageUpdate(user.getName() + " restored some HP due to its " + this.getName() + "!").updatePokemon(b, user));
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
            super(ItemNamesies.SNOWBALL, "An item to be held by a Pok\u00e9mon. It boosts Attack if hit with an Ice-type attack. It can only be used once.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (user.isAttackType(Type.ICE) && victim.getStages().modifyStage(victim, 1, Stat.ATTACK, b, CastSource.HELD_ITEM)) {
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
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return p.isPokemon(PokemonNamesies.LATIOS) || p.isPokemon(PokemonNamesies.LATIAS);
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SP_ATTACK || s == Stat.SP_DEFENSE;
        }

        @Override
        public double getModifier() {
            return 1.5;
        }
    }

    static class Stick extends Item implements HoldItem, CritStageEffect {
        private static final long serialVersionUID = 1L;

        Stick() {
            super(ItemNamesies.STICK, "An item to be held by Farfetch'd. This very long and stiff stalk of leek boosts its critical-hit ratio.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public int increaseCritStage(int stage, ActivePokemon p) {
            if (p.isPokemon(PokemonNamesies.FARFETCHD)) {
                return stage + 2;
            }

            return stage;
        }

        @Override
        public int flingDamage() {
            return 60;
        }
    }

    static class StickyBarb extends Item implements HoldItem, EndTurnEffect, PhysicalContactEffect, ItemSwapperEffect {
        private static final long serialVersionUID = 1L;

        private void stickyPoke(Battle b, ActivePokemon victim, String possession) {
            if (!victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
                Messages.add(victim.getName() + " was hurt by " + possession + " " + this.getName() + "!");
                victim.reduceHealthFraction(b, 1/8.0);
            }
        }

        StickyBarb() {
            super(ItemNamesies.STICKY_BARB, "An item to be held by a Pok\u00e9mon. It damages the holder every turn and may latch on to Pok\u00e9mon that touch the holder.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public String getSwitchMessage(ActivePokemon user, HoldItem userItem, ActivePokemon victim, HoldItem victimItem) {
            return victim.getName() + "s " + this.getName() + " latched onto " + user.getName() + "!";
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
    }

    static class ThickClub extends Item implements HoldItem, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        ThickClub() {
            super(ItemNamesies.THICK_CLUB, "An item to be held by Cubone or Marowak. It's a hard bone of some sort that boosts the Attack stat.", BagCategory.MISC);
            super.price = 1000;
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return p.isPokemon(PokemonNamesies.CUBONE) || p.isPokemon(PokemonNamesies.MAROWAK);
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.ATTACK;
        }

        @Override
        public int flingDamage() {
            return 90;
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
        public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (TypeAdvantage.isSuperEffective(user, victim, b)) {
                victim.getStages().modifyStage(victim, 2, Stat.ATTACK, b, CastSource.HELD_ITEM);
                victim.getStages().modifyStage(victim, 2, Stat.SP_ATTACK, b, CastSource.HELD_ITEM);
            }
        }
    }

    static class WhiteHerb extends Item implements HoldItem, StatProtectingEffect {
        private static final long serialVersionUID = 1L;

        WhiteHerb() {
            super(ItemNamesies.WHITE_HERB, "An item to be held by a Pok\u00e9mon. It will restore any lowered stat in battle. It can be used only once.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
            // NOTE: Works like Clear Body, since ain't nobody want to keep track of stats.
            return true;
        }

        @Override
        public int flingDamage() {
            return 10;
        }

        @Override
        public void flingEffect(Battle b, ActivePokemon pelted) {
            // Restores negative stat changes to the pelted
            for (Stat stat : Stat.BATTLE_STATS) {
                if (pelted.getStage(stat) < 0) {
                    pelted.getStages().setStage(stat, 0);
                }
            }

            Messages.add("The " + this.getName() + " restored " + pelted.getName() + "'s negative stat changes!");
        }
    }

    static class WideLens extends Item implements HoldItem, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        WideLens() {
            super(ItemNamesies.WIDE_LENS, "An item to be held by a Pok\u00e9mon. It's a magnifying lens that slightly boosts the accuracy of moves.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.ACCURACY;
        }

        @Override
        public int flingDamage() {
            return 10;
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
        public boolean isModifyStat(Stat s) {
            return s == Stat.SP_ATTACK;
        }

        @Override
        public int flingDamage() {
            return 10;
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
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return !b.isFirstAttack();
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.ACCURACY;
        }

        @Override
        public int flingDamage() {
            return 10;
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

    static class LaxIncense extends Item implements SimpleStatModifyingEffect, IncenseItem {
        private static final long serialVersionUID = 1L;

        LaxIncense() {
            super(ItemNamesies.LAX_INCENSE, "An item to be held by a Pok\u00e9mon. The beguiling aroma of this incense may cause attacks to miss its holder.", BagCategory.MISC);
            super.price = 5000;
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.EVASION;
        }

        @Override
        public PokemonNamesies getBaby() {
            return PokemonNamesies.WYNAUT;
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

        @Override
        public PokemonNamesies getBaby() {
            return PokemonNamesies.HAPPINY;
        }

        private void getDatCashMoneyGetDatCashMoneyCast(Battle b, ActivePokemon gettinDatCashMoneyTwice) {
            TeamEffectNamesies.GET_DAT_CASH_MONEY_TWICE.getEffect().cast(b, gettinDatCashMoneyTwice, gettinDatCashMoneyTwice, CastSource.HELD_ITEM, false);
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
            if (victim.fullHealth() || victim.hasEffect(PokemonEffectNamesies.HEAL_BLOCK)) {
                return;
            }

            victim.healHealthFraction(1/16.0);
            Messages.add(new MessageUpdate(victim.getName() + "'s HP was restored by its " + this.getName() + "!").updatePokemon(b, victim));
        }

        @Override
        public int flingDamage() {
            return 10;
        }
    }

    static class BlackBelt extends Item implements HoldItem, PowerChangeEffect {
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
            super(ItemNamesies.BLACK_GLASSES, "An item to be held by a Pok\u00e9mon. A pair of shady-looking glasses that boost the power of Dark-type moves.", BagCategory.MISC);
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

    static class DragonFang extends Item implements HoldItem, PowerChangeEffect {
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

    static class HardStone extends Item implements HoldItem, PowerChangeEffect {
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
            super(ItemNamesies.MIRACLE_SEED, "An item to be held by a Pok\u00e9mon. It's a seed imbued with life force that boosts the power of Grass-type moves.", BagCategory.MISC);
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
            super(ItemNamesies.NEVER_MELT_ICE, "An item to be held by a Pok\u00e9mon. It's a piece of ice that repels heat effects and boosts Ice-type moves.", BagCategory.MISC);
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
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(Type.POISON) ? 1.2 : 1;
        }

        @Override
        public void flingEffect(Battle b, ActivePokemon pelted) {
            StatusNamesies.POISONED.getStatus().apply(b, pelted, pelted, pelted.getName() + " was poisoned by the " + this.getName() + "!");
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
            super(ItemNamesies.SILVER_POWDER, "An item to be held by a Pok\u00e9mon. It's a shiny, silver powder that will boost the power of Bug-type moves.", BagCategory.MISC);
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

    static class SpellTag extends Item implements HoldItem, PowerChangeEffect {
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

    static class TwistedSpoon extends Item implements HoldItem, PowerChangeEffect {
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
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return p.isPokemon(PokemonNamesies.CLAMPERL) || p.isPokemon(PokemonNamesies.CHINCHOU) || p.isPokemon(PokemonNamesies.LANTURN);
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SP_DEFENSE;
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
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return p.isPokemon(PokemonNamesies.CLAMPERL);
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SP_ATTACK;
        }

        @Override
        public int flingDamage() {
            return 90;
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
        public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (RandomUtils.chanceTest(10)) {
                if (PokemonEffectNamesies.FLINCH.getEffect().apply(b, user, victim, CastSource.HELD_ITEM, false)) {
                    Messages.add(user.getName() + "'s " + this.getName() + " caused " + victim.getName() + " to flinch!");
                }
            }
        }

        @Override
        public void flingEffect(Battle b, ActivePokemon pelted) {
            if (PokemonEffectNamesies.FLINCH.getEffect().apply(b, pelted, pelted, CastSource.USE_ITEM, false)) {
                Messages.add("The " + this.getName() + " caused " + pelted.getName() + " to flinch!");
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
        public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (RandomUtils.chanceTest(10)) {
                if (PokemonEffectNamesies.FLINCH.getEffect().apply(b, user, victim, CastSource.HELD_ITEM, false)) {
                    Messages.add(user.getName() + "'s " + this.getName() + " caused " + victim.getName() + " to flinch!");
                }
            }
        }

        @Override
        public void flingEffect(Battle b, ActivePokemon pelted) {
            if (PokemonEffectNamesies.FLINCH.getEffect().apply(b, pelted, pelted, CastSource.USE_ITEM, false)) {
                Messages.add("The " + this.getName() + " caused " + pelted.getName() + " to flinch!");
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
            super(ItemNamesies.PRETTY_WING, "Though this feather is beautiful, it's just a regular feather and has no effect on Pok\u00e9mon.", BagCategory.MISC);
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
            super(ItemNamesies.ANTIDOTE, "A spray-type medicine for poisoning. It can be used once to lift the effects of being poisoned from a Pok\u00e9mon.", BagCategory.MEDICINE);
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
            super(ItemNamesies.AWAKENING, "A spray-type medicine used against sleep. It can be used once to rouse a Pok\u00e9mon from the clutches of sleep.", BagCategory.MEDICINE);
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
            super(ItemNamesies.BURN_HEAL, "A spray-type medicine for treating burns. It can be used once to heal a Pok\u00e9mon suffering from a burn.", BagCategory.MEDICINE);
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
            super(ItemNamesies.ICE_HEAL, "A spray-type medicine for freezing. It can be used once to defrost a Pok\u00e9mon that has been frozen solid.", BagCategory.MEDICINE);
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
            super(ItemNamesies.PARALYZE_HEAL, "A spray-type medicine for paralysis. It can be used once to free a Pok\u00e9mon that has been paralyzed.", BagCategory.MEDICINE);
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
            super(ItemNamesies.FULL_HEAL, "A spray-type medicine that is broadly effective. It can be used once to heal all the status conditions of a Pok\u00e9mon.", BagCategory.MEDICINE);
            super.price = 400;
            super.battleBagCategories.add(BattleBagCategory.STATUS);
        }

        @Override
        public boolean shouldHeal(ActivePokemon p) {
            // Does not apply to the healthy and the dead
            return p.hasStatus() && !p.isActuallyDead();
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
            super(ItemNamesies.ELIXIR, "This medicine can restore 10 PP to each of the moves that have been learned by a Pok\u00e9mon.", BagCategory.MEDICINE);
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
            super(ItemNamesies.MAX_ELIXIR, "This medicine can fully restore the PP of all of the moves that have been learned by a Pok\u00e9mon.", BagCategory.MEDICINE);
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
            super(ItemNamesies.ETHER, "This medicine can restore 10 PP to a single selected move that has been learned by a Pok\u00e9mon.", BagCategory.MEDICINE);
            super.price = 1200;
        }

        @Override
        public int restoreAmount(Move toRestore) {
            return 10;
        }
    }

    // TODO: These currently cannot be used in battle :(
    static class MaxEther extends Item implements HoldItem, PPHealer {
        private static final long serialVersionUID = 1L;

        MaxEther() {
            super(ItemNamesies.MAX_ETHER, "This medicine can fully restore the PP of a single selected move that has been learned by a Pok\u00e9mon.", BagCategory.MEDICINE);
            super.price = 2000;
        }

        @Override
        public int restoreAmount(Move toRestore) {
            return toRestore.getMaxPP();
        }
    }

    static class BerryJuice extends Item implements FixedHpHealer {
        private static final long serialVersionUID = 1L;

        BerryJuice() {
            super(ItemNamesies.BERRY_JUICE, "A 100 percent pure juice made of Berries. When consumed, it restores 20 HP to an injured Pok\u00e9mon.", BagCategory.MEDICINE);
            super.price = 100;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public int getFixedHealAmount(ActivePokemon p) {
            return 20;
        }
    }

    static class SweetHeart extends Item implements FixedHpHealer {
        private static final long serialVersionUID = 1L;

        SweetHeart() {
            super(ItemNamesies.SWEET_HEART, "A piece of cloyingly sweet chocolate. When consumed, it restores 20 HP to an injured Pok\u00e9mon.", BagCategory.MEDICINE);
            super.price = 3000;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public int getFixedHealAmount(ActivePokemon p) {
            return 20;
        }
    }

    static class Potion extends Item implements FixedHpHealer {
        private static final long serialVersionUID = 1L;

        Potion() {
            super(ItemNamesies.POTION, "A spray-type medicine for treating wounds. It can be used to restore 20 HP to an injured Pok\u00e9mon.", BagCategory.MEDICINE);
            super.price = 200;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public int getFixedHealAmount(ActivePokemon p) {
            return 20;
        }
    }

    static class EnergyPowder extends Item implements FixedHpHealer {
        private static final long serialVersionUID = 1L;

        EnergyPowder() {
            super(ItemNamesies.ENERGY_POWDER, "A very bitter medicinal powder. When consumed, it restores up to 60 HP to an injured Pok\u00e9mon.", BagCategory.MEDICINE);
            super.price = 500;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public int getFixedHealAmount(ActivePokemon p) {
            return 60;
        }
    }

    static class FreshWater extends Item implements FixedHpHealer {
        private static final long serialVersionUID = 1L;

        FreshWater() {
            super(ItemNamesies.FRESH_WATER, "Water with a high mineral content. When consumed, it restores up to 30 HP to an injured Pok\u00e9mon.", BagCategory.MEDICINE);
            super.price = 200;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public int getFixedHealAmount(ActivePokemon p) {
            return 30;
        }
    }

    static class SuperPotion extends Item implements FixedHpHealer {
        private static final long serialVersionUID = 1L;

        SuperPotion() {
            super(ItemNamesies.SUPER_POTION, "A spray-type medicine for treating wounds. It can be used to restore 60 HP to an injured Pok\u00e9mon.", BagCategory.MEDICINE);
            super.price = 700;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public int getFixedHealAmount(ActivePokemon p) {
            return 60;
        }
    }

    static class SodaPop extends Item implements FixedHpHealer {
        private static final long serialVersionUID = 1L;

        SodaPop() {
            super(ItemNamesies.SODA_POP, "A highly carbonated soda drink. When consumed, it restores up to 50 HP to an injured Pok\u00e9mon.", BagCategory.MEDICINE);
            super.price = 300;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public int getFixedHealAmount(ActivePokemon p) {
            return 50;
        }
    }

    static class Lemonade extends Item implements FixedHpHealer {
        private static final long serialVersionUID = 1L;

        Lemonade() {
            super(ItemNamesies.LEMONADE, "A very sweet and refreshing drink. When consumed, it restores up to 70 HP to an injured Pok\u00e9mon.", BagCategory.MEDICINE);
            super.price = 350;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public int getFixedHealAmount(ActivePokemon p) {
            return 70;
        }
    }

    static class MoomooMilk extends Item implements FixedHpHealer {
        private static final long serialVersionUID = 1L;

        MoomooMilk() {
            super(ItemNamesies.MOOMOO_MILK, "A bottle of highly nutritious milk. When consumed, it restores up to 100 HP to an injured Pok\u00e9mon.", BagCategory.MEDICINE);
            super.price = 600;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public int getFixedHealAmount(ActivePokemon p) {
            return 100;
        }
    }

    static class EnergyRoot extends Item implements FixedHpHealer {
        private static final long serialVersionUID = 1L;

        EnergyRoot() {
            super(ItemNamesies.ENERGY_ROOT, "An extremely bitter medicinal root. When consumed, it restores up to 120 HP to an injured Pok\u00e9mon.", BagCategory.MEDICINE);
            super.price = 1200;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public int getFixedHealAmount(ActivePokemon p) {
            return 120;
        }
    }

    static class HyperPotion extends Item implements FixedHpHealer {
        private static final long serialVersionUID = 1L;

        HyperPotion() {
            super(ItemNamesies.HYPER_POTION, "A spray-type medicine for treating wounds. It can be used to restore 120 HP to an injured Pok\u00e9mon.", BagCategory.MEDICINE);
            super.price = 1500;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public int getFixedHealAmount(ActivePokemon p) {
            return 120;
        }
    }

    static class MaxPotion extends Item implements HpHealer {
        private static final long serialVersionUID = 1L;

        MaxPotion() {
            super(ItemNamesies.MAX_POTION, "A spray-type medicine for treating wounds. It will completely restore the max HP of a single Pok\u00e9mon.", BagCategory.MEDICINE);
            super.price = 2500;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public int getAmountHealed(ActivePokemon p) {
            return p.healHealthFraction(1);
        }
    }

    static class Revive extends Item implements BattlePokemonUseItem, HoldItem {
        private static final long serialVersionUID = 1L;

        Revive() {
            super(ItemNamesies.REVIVE, "A medicine that can revive fainted Pok\u00e9mon. It also restores half of a fainted Pok\u00e9mon's maximum HP.", BagCategory.MEDICINE);
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
            super(ItemNamesies.MAX_REVIVE, "A medicine that can revive fainted Pok\u00e9mon. It also fully restores a fainted Pok\u00e9mon's maximum HP.", BagCategory.MEDICINE);
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
            super(ItemNamesies.REVIVAL_HERB, "A terribly bitter medicinal herb. It revives a fainted Pok\u00e9mon and fully restores its maximum HP.", BagCategory.MEDICINE);
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

        public boolean use(Trainer t) {
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
            super(ItemNamesies.SACRED_ASH, "This rare ash can revive all fainted Pok\u00e9mon in a party. In doing so, it also fully restores their maximum HP.", BagCategory.MEDICINE);
            super.price = 50000;
            super.battleBagCategories.add(BattleBagCategory.STATUS);
        }

        @Override
        public boolean use(Battle b, ActivePokemon p, Move m) {
            return b == null ? use() : use(p, b);
        }

        @Override
        public boolean use() {
            return this.use(Game.getPlayer());
        }

        @Override
        public boolean use(ActivePokemon p, Battle b) {
            return use((Trainer)b.getTrainer(p));
        }
    }

    static class DireHit extends Item implements BattleUseItem, HoldItem {
        private static final long serialVersionUID = 1L;

        DireHit() {
            super(ItemNamesies.DIRE_HIT, "An item that raises the critical-hit ratio greatly. It can be used only once and wears off if the Pok\u00e9mon is withdrawn.", BagCategory.STAT);
            super.price = 650;
            super.battleBagCategories.add(BattleBagCategory.BATTLE);
        }

        @Override
        public boolean use(ActivePokemon p, Battle b) {
            return PokemonEffectNamesies.RAISE_CRITS.getEffect().apply(b, p, p, CastSource.USE_ITEM, true);
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
            return PokemonEffectNamesies.GUARD_SPECIAL.getEffect().apply(b, p, p, CastSource.USE_ITEM, true);
        }
    }

    static class XAccuracy extends Item implements HoldItem, BattleUseItem {
        private static final long serialVersionUID = 1L;

        XAccuracy() {
            super(ItemNamesies.XACCURACY, "An item that sharply boosts the accuracy of a Pok\u00e9mon during a battle. It wears off once the Pok\u00e9mon is withdrawn.", BagCategory.STAT);
            super.price = 950;
            super.battleBagCategories.add(BattleBagCategory.BATTLE);
        }

        @Override
        public boolean use(ActivePokemon p, Battle b) {
            return p.getStages().modifyStage(p, 2, Stat.ACCURACY, b, CastSource.USE_ITEM);
        }
    }

    static class XAttack extends Item implements HoldItem, BattleUseItem {
        private static final long serialVersionUID = 1L;

        XAttack() {
            super(ItemNamesies.XATTACK, "An item that sharply boosts the Attack stat of a Pok\u00e9mon during a battle. It wears off once the Pok\u00e9mon is withdrawn.", BagCategory.STAT);
            super.price = 500;
            super.battleBagCategories.add(BattleBagCategory.BATTLE);
        }

        @Override
        public boolean use(ActivePokemon p, Battle b) {
            return p.getStages().modifyStage(p, 2, Stat.ATTACK, b, CastSource.USE_ITEM);
        }
    }

    static class XDefend extends Item implements HoldItem, BattleUseItem {
        private static final long serialVersionUID = 1L;

        XDefend() {
            super(ItemNamesies.XDEFEND, "An item that boosts the Defense stat of a Pok\u00e9mon during a battle. It wears off once the Pok\u00e9mon is withdrawn.", BagCategory.STAT);
            super.price = 550;
            super.battleBagCategories.add(BattleBagCategory.BATTLE);
        }

        @Override
        public boolean use(ActivePokemon p, Battle b) {
            return p.getStages().modifyStage(p, 2, Stat.DEFENSE, b, CastSource.USE_ITEM);
        }
    }

    static class XSpecial extends Item implements HoldItem, BattleUseItem {
        private static final long serialVersionUID = 1L;

        XSpecial() {
            super(ItemNamesies.XSPECIAL, "An item that raises the Sp. Atk stat of a Pok\u00e9mon in battle. It wears off if the Pok\u00e9mon is withdrawn.", BagCategory.STAT);
            super.price = 350;
            super.battleBagCategories.add(BattleBagCategory.BATTLE);
        }

        @Override
        public boolean use(ActivePokemon p, Battle b) {
            return p.getStages().modifyStage(p, 2, Stat.SP_ATTACK, b, CastSource.USE_ITEM);
        }
    }

    static class XSpDef extends Item implements HoldItem, BattleUseItem {
        private static final long serialVersionUID = 1L;

        XSpDef() {
            super(ItemNamesies.XSP_DEF, "An item that sharply boosts the Sp. Def stat of a Pok\u00e9mon during a battle. It wears off once the Pok\u00e9mon is withdrawn.", BagCategory.STAT);
            super.price = 350;
            super.battleBagCategories.add(BattleBagCategory.BATTLE);
        }

        @Override
        public boolean use(ActivePokemon p, Battle b) {
            return p.getStages().modifyStage(p, 2, Stat.SP_DEFENSE, b, CastSource.USE_ITEM);
        }
    }

    static class XSpeed extends Item implements HoldItem, BattleUseItem {
        private static final long serialVersionUID = 1L;

        XSpeed() {
            super(ItemNamesies.XSPEED, "An item that sharply boosts the Speed stat of a Pok\u00e9mon during a battle. It wears off once the Pok\u00e9mon is withdrawn.", BagCategory.STAT);
            super.price = 350;
            super.battleBagCategories.add(BattleBagCategory.BATTLE);
        }

        @Override
        public boolean use(ActivePokemon p, Battle b) {
            return p.getStages().modifyStage(p, 2, Stat.SPEED, b, CastSource.USE_ITEM);
        }
    }

    static class HPUp extends Item implements Vitamin {
        private static final long serialVersionUID = 1L;

        HPUp() {
            super(ItemNamesies.HPUP, "A nutritious drink for Pok\u00e9mon. When consumed, it raises the base HP of a single Pok\u00e9mon.", BagCategory.STAT);
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
            super(ItemNamesies.PROTEIN, "A nutritious drink for Pok\u00e9mon. When consumed, it raises the base Attack stat of a single Pok\u00e9mon.", BagCategory.STAT);
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
            super(ItemNamesies.IRON, "A nutritious drink for Pok\u00e9mon. When consumed, it raises the base Defense stat of a single Pok\u00e9mon.", BagCategory.STAT);
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
            super(ItemNamesies.CALCIUM, "A nutritious drink for Pok\u00e9mon. When consumed, it raises the base Sp. Atk stat of a single Pok\u00e9mon.", BagCategory.STAT);
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
            super(ItemNamesies.ZINC, "A nutritious drink for Pok\u00e9mon. When consumed, it raises the base Sp. Def stat of a single Pok\u00e9mon.", BagCategory.STAT);
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
            super(ItemNamesies.CARBOS, "A nutritious drink for Pok\u00e9mon. When consumed, it raises the base Speed stat of a single Pok\u00e9mon.", BagCategory.STAT);
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
            super(ItemNamesies.PPMAX, "A medicine that can optimally raise the maximum PP of a single move that has been learned by the target Pok\u00e9mon.", BagCategory.STAT);
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
            super(ItemNamesies.PPUP, "A medicine that can slightly raise the maximum PP of a single move that has been learned by the target Pok\u00e9mon.", BagCategory.STAT);
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
            super(ItemNamesies.DUSK_BALL, "A somewhat different Pok\u00e9 Ball that makes it easier to catch wild Pok\u00e9mon at night or in dark places like caves.", BagCategory.BALL);
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
            return o.getPokemonInfo().getStat(Stat.SPEED.index()) >= 100 ? 4 : 1;
        }
    }

    static class GreatBall extends Item implements BallItem {
        private static final long serialVersionUID = 1L;

        GreatBall() {
            super(ItemNamesies.GREAT_BALL, "A good, high-performance Pok\u00e9 Ball that provides a higher Pok\u00e9mon catch rate than a standard Pok\u00e9 Ball.", BagCategory.BALL);
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
            if (weight <= 451.5) {
                return -20;
            } else if (weight <= 661.5) {
                return 20;
            } else if (weight <= 903.0) {
                return 30;
            } else {
                return 40;
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
            super(ItemNamesies.LURE_BALL, "A Pok\u00e9 Ball that is good for catching Pok\u00e9mon that you reel in with a Rod while out fishing.", BagCategory.BALL);
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
            return o.isType(b, Type.WATER) || o.isType(b, Type.BUG) ? 3 : 1;
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
            super(ItemNamesies.TIMER_BALL, "A somewhat different Pok\u00e9 Ball that becomes progressively more effective the more turns that are taken in battle.", BagCategory.BALL);
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
        public int restoreAmount(Move toRestore) {
            return 10;
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
        public int naturalGiftPower() {
            return 80;
        }

        @Override
        public Type naturalGiftType() {
            return Type.FIGHTING;
        }
    }

    static class OranBerry extends Item implements FixedHpHealer, HealthTriggeredBerry {
        private static final long serialVersionUID = 1L;

        OranBerry() {
            super(ItemNamesies.ORAN_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a Pok\u00e9mon holds one, it can restore its own HP by 10 points during battle.", BagCategory.BERRY);
            super.price = 20;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public int getFixedHealAmount(ActivePokemon p) {
            return 10;
        }

        @Override
        public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
            return use(b, user, source);
        }

        @Override
        public double healthTriggerRatio() {
            return 1/3.0;
        }

        @Override
        public int naturalGiftPower() {
            return 80;
        }

        @Override
        public Type naturalGiftType() {
            return Type.POISON;
        }
    }

    static class PersimBerry extends Item implements BattleUseItem, MessageGetter, GainableEffectBerry, EffectCurerItem {
        private static final long serialVersionUID = 1L;

        private boolean use(Battle b, ActivePokemon p, CastSource source) {
            if (p.getEffects().remove(PokemonEffectNamesies.CONFUSION)) {
                Messages.add(this.getMessage(b, p, source));
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
        public boolean use(ActivePokemon p, Battle b) {
            return use(b, p, CastSource.USE_ITEM);
        }

        @Override
        public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
            return use(b, user, source);
        }

        @Override
        public int naturalGiftPower() {
            return 80;
        }

        @Override
        public Type naturalGiftType() {
            return Type.GROUND;
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

        @Override
        public String getRemoveMessage(ActivePokemon victim, PokemonEffectNamesies effectType) {
            return this.getSourceMessage(victim, this.getName());
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
        public boolean shouldHeal(ActivePokemon p) {
            // Does not apply to the healthy and the dead
            return p.hasStatus() && !p.isActuallyDead();
        }

        @Override
        public Type naturalGiftType() {
            return Type.FLYING;
        }

        @Override
        public int getHarvestHours() {
            return 48;
        }
    }

    static class SitrusBerry extends Item implements HpHealer, HealthTriggeredBerry {
        private static final long serialVersionUID = 1L;

        SitrusBerry() {
            super(ItemNamesies.SITRUS_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a Pok\u00e9mon holds one, it can restore its own HP by a small amount during battle.", BagCategory.BERRY);
            super.price = 20;
            super.battleBagCategories.add(BattleBagCategory.HP_PP);
        }

        @Override
        public int getAmountHealed(ActivePokemon p) {
            return p.healHealthFraction(1/4.0);
        }

        @Override
        public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
            return use(b, user, source);
        }

        @Override
        public double healthTriggerRatio() {
            return 1/2.0;
        }

        @Override
        public int naturalGiftPower() {
            return 80;
        }

        @Override
        public Type naturalGiftType() {
            return Type.PSYCHIC;
        }

        @Override
        public int getHarvestHours() {
            return 48;
        }
    }

    static class RazzBerry extends Item implements Berry {
        private static final long serialVersionUID = 1L;

        RazzBerry() {
            super(ItemNamesies.RAZZ_BERRY, "A very valuable berry. Useful for acquiring value.", BagCategory.BERRY);
            super.price = 60000;
        }

        @Override
        public int naturalGiftPower() {
            return 80;
        }

        @Override
        public Type naturalGiftType() {
            return Type.STEEL;
        }
    }

    static class PomegBerry extends Item implements EvDecreaseBerry {
        private static final long serialVersionUID = 1L;

        PomegBerry() {
            super(ItemNamesies.POMEG_BERRY, "A Berry to be consumed by Pok\u00e9mon. Using it on a Pok\u00e9mon lowers its base HP.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Stat toDecrease() {
            return Stat.HP;
        }

        @Override
        public Type naturalGiftType() {
            return Type.ICE;
        }
    }

    static class KelpsyBerry extends Item implements EvDecreaseBerry {
        private static final long serialVersionUID = 1L;

        KelpsyBerry() {
            super(ItemNamesies.KELPSY_BERRY, "A Berry to be consumed by Pok\u00e9mon. Using it on a Pok\u00e9mon lowers its base Attack.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Stat toDecrease() {
            return Stat.ATTACK;
        }

        @Override
        public Type naturalGiftType() {
            return Type.FIGHTING;
        }
    }

    static class QualotBerry extends Item implements EvDecreaseBerry {
        private static final long serialVersionUID = 1L;

        QualotBerry() {
            super(ItemNamesies.QUALOT_BERRY, "A Berry to be consumed by Pok\u00e9mon. Using it on a Pok\u00e9mon lowers its base Defense.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Stat toDecrease() {
            return Stat.DEFENSE;
        }

        @Override
        public Type naturalGiftType() {
            return Type.POISON;
        }
    }

    static class HondewBerry extends Item implements EvDecreaseBerry {
        private static final long serialVersionUID = 1L;

        HondewBerry() {
            super(ItemNamesies.HONDEW_BERRY, "A Berry to be consumed by Pok\u00e9mon. Using it on a Pok\u00e9mon lowers its base Sp. Atk.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Stat toDecrease() {
            return Stat.SP_ATTACK;
        }

        @Override
        public Type naturalGiftType() {
            return Type.GROUND;
        }
    }

    static class GrepaBerry extends Item implements EvDecreaseBerry {
        private static final long serialVersionUID = 1L;

        GrepaBerry() {
            super(ItemNamesies.GREPA_BERRY, "A Berry to be consumed by Pok\u00e9mon. Using it on a Pok\u00e9mon lowers its base Sp. Def.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Stat toDecrease() {
            return Stat.SP_DEFENSE;
        }

        @Override
        public Type naturalGiftType() {
            return Type.FLYING;
        }
    }

    static class TamatoBerry extends Item implements EvDecreaseBerry {
        private static final long serialVersionUID = 1L;

        TamatoBerry() {
            super(ItemNamesies.TAMATO_BERRY, "A Berry to be consumed by Pok\u00e9mon. Using it on a Pok\u00e9mon lowers its base Speed.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Stat toDecrease() {
            return Stat.SPEED;
        }

        @Override
        public Type naturalGiftType() {
            return Type.PSYCHIC;
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
        public Stat getStat() {
            return Stat.ATTACK;
        }

        @Override
        public Type naturalGiftType() {
            return Type.GRASS;
        }
    }

    static class GanlonBerry extends Item implements HealthTriggeredStageIncreaseBerry {
        private static final long serialVersionUID = 1L;

        GanlonBerry() {
            super(ItemNamesies.GANLON_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a Pok\u00e9mon holds one, its Defense stat will increase when it's in a pinch.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Stat getStat() {
            return Stat.DEFENSE;
        }

        @Override
        public Type naturalGiftType() {
            return Type.ICE;
        }
    }

    static class SalacBerry extends Item implements HealthTriggeredStageIncreaseBerry {
        private static final long serialVersionUID = 1L;

        SalacBerry() {
            super(ItemNamesies.SALAC_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a Pok\u00e9mon holds one, its Speed stat will increase when it's in a pinch.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Stat getStat() {
            return Stat.SPEED;
        }

        @Override
        public Type naturalGiftType() {
            return Type.FIGHTING;
        }
    }

    static class PetayaBerry extends Item implements HealthTriggeredStageIncreaseBerry {
        private static final long serialVersionUID = 1L;

        PetayaBerry() {
            super(ItemNamesies.PETAYA_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a Pok\u00e9mon holds one, its Sp. Atk stat will increase when it's in a pinch.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Stat getStat() {
            return Stat.SP_ATTACK;
        }

        @Override
        public Type naturalGiftType() {
            return Type.POISON;
        }
    }

    static class ApicotBerry extends Item implements HealthTriggeredStageIncreaseBerry {
        private static final long serialVersionUID = 1L;

        ApicotBerry() {
            super(ItemNamesies.APICOT_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a Pok\u00e9mon holds one, its Sp. Def stat will increase when it's in a pinch.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Stat getStat() {
            return Stat.SP_DEFENSE;
        }

        @Override
        public Type naturalGiftType() {
            return Type.GROUND;
        }
    }

    static class MicleBerry extends Item implements HealthTriggeredStageIncreaseBerry {
        private static final long serialVersionUID = 1L;

        MicleBerry() {
            super(ItemNamesies.MICLE_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a Pok\u00e9mon holds one, its accuracy will increase just once when it's in a pinch.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Stat getStat() {
            return Stat.ACCURACY;
        }

        @Override
        public Type naturalGiftType() {
            return Type.ROCK;
        }
    }

    static class KeeBerry extends Item implements CategoryIncreaseBerry {
        private static final long serialVersionUID = 1L;

        KeeBerry() {
            super(ItemNamesies.KEE_BERRY, "If held by a Pok\u00e9mon, this Berry will increase the holder's Defense if it's hit with a physical move.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public Stat getStat() {
            return Stat.DEFENSE;
        }

        @Override
        public Type naturalGiftType() {
            return Type.FAIRY;
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
        public Stat getStat() {
            return Stat.SP_DEFENSE;
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
        public int naturalGiftPower() {
            return 100;
        }

        @Override
        public Type naturalGiftType() {
            return Type.GHOST;
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
        public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (!victim.fullHealth() && TypeAdvantage.isSuperEffective(user, victim, b)) {
                Messages.add(victim.getName() + "'s " + this.getName() + " restored its health!");
                victim.healHealthFraction(.25);
                Messages.add(new MessageUpdate().updatePokemon(b, victim));
                this.consumeItem(b, victim);
            }
        }

        @Override
        public int naturalGiftPower() {
            return 100;
        }

        @Override
        public Type naturalGiftType() {
            return Type.BUG;
        }

        @Override
        public int getHarvestHours() {
            return 72;
        }
    }

    static class LansatBerry extends Item implements HealthTriggeredBerry {
        private static final long serialVersionUID = 1L;

        LansatBerry() {
            super(ItemNamesies.LANSAT_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a Pok\u00e9mon holds one, its critical-hit ratio will increase when it's in a pinch.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
            PokemonEffectNamesies.RAISE_CRITS.getEffect().cast(b, user, user, source, true);
            return true;
        }

        @Override
        public double healthTriggerRatio() {
            return 1/4.0;
        }

        @Override
        public int naturalGiftPower() {
            return 100;
        }

        @Override
        public Type naturalGiftType() {
            return Type.FLYING;
        }

        @Override
        public int getHarvestHours() {
            return 72;
        }
    }

    static class StarfBerry extends Item implements HealthTriggeredBerry {
        private static final long serialVersionUID = 1L;

        StarfBerry() {
            super(ItemNamesies.STARF_BERRY, "A Berry to be consumed by Pok\u00e9mon. If a Pok\u00e9mon holds one, one of its stats will sharply increase when it's in a pinch.", BagCategory.BERRY);
            super.price = 20;
        }

        @Override
        public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
            int rand = RandomUtils.getRandomInt(Stat.NUM_BATTLE_STATS + 1);

            // Raise crit
            if (rand == Stat.NUM_BATTLE_STATS) {
                PokemonEffectNamesies.RAISE_CRITS.getEffect().cast(b, user, user, source, true);
                return true;
            }

            // Raise random battle stat
            Stat stat = Stat.getStat(rand, true);
            return user.getStages().modifyStage(user, 1, stat, b, source);
        }

        @Override
        public double healthTriggerRatio() {
            return 1/4.0;
        }

        @Override
        public int naturalGiftPower() {
            return 100;
        }

        @Override
        public Type naturalGiftType() {
            return Type.PSYCHIC;
        }

        @Override
        public int getHarvestHours() {
            return 72;
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
            super(ItemNamesies.TINY_MUSHROOM, "A very small and rare mushroom. It's popular with a certain class of collectors and sought out by them.", BagCategory.MISC);
            super.price = 500;
        }
    }

    static class BigMushroom extends Item implements HoldItem {
        private static final long serialVersionUID = 1L;

        BigMushroom() {
            super(ItemNamesies.BIG_MUSHROOM, "A very large and rare mushroom. It's popular with a certain class of collectors and sought out by them.", BagCategory.MISC);
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
            super(ItemNamesies.PEARL, "A rather small pearl that has a very nice silvery sheen to it. It can be sold cheaply to shops.", BagCategory.MISC);
            super.price = 2000;
        }
    }

    static class BigPearl extends Item implements HoldItem {
        private static final long serialVersionUID = 1L;

        BigPearl() {
            super(ItemNamesies.BIG_PEARL, "A rather large pearl that has a very nice silvery sheen. It can be sold to shops for a high price.", BagCategory.MISC);
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
            super(ItemNamesies.STAR_PIECE, "A small shard of a beautiful gem that demonstrates a distinctly red sparkle. It can be sold at a high price to shops.", BagCategory.MISC);
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

    // TODO: We need this item to do something
    static class Honey extends Item implements HoldItem {
        private static final long serialVersionUID = 1L;

        Honey() {
            super(ItemNamesies.HONEY, "A sweet honey with a lush aroma that attracts wild Pok\u00e9mon when it is used in tall grass, in caves, or on special trees.", BagCategory.MISC);
            super.price = 100;
        }
    }

    static class Eviolite extends Item implements HoldItem, SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        Eviolite() {
            super(ItemNamesies.EVIOLITE, "A mysterious Evolutionary lump. When held by a Pok\u00e9mon that can still evolve, it raises both Defense and Sp. Def.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return p.getPokemonInfo().getEvolution().canEvolve();
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.DEFENSE || s == Stat.SP_DEFENSE;
        }

        @Override
        public int flingDamage() {
            return 40;
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
            super(ItemNamesies.ABILITY_CAPSULE, "A capsule that allows a Pok\u00e9mon with two Abilities to switch between these Abilities when it is used.", BagCategory.MISC);
            super.price = 10000;
        }

        @Override
        public boolean use(ActivePokemon p) {
            AbilityNamesies other = Ability.getOtherAbility(p);
            if (other == AbilityNamesies.NO_ABILITY) {
                return false;
            }

            p.setAbility(other);
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
        public boolean isModifyStat(Stat s) {
            return s == Stat.SP_DEFENSE;
        }

        @Override
        public boolean usable(Battle b, ActivePokemon p, Move m) {
            return !m.getAttack().isStatusMove();
        }

        @Override
        public String getUnusableMessage(Battle b, ActivePokemon p) {
            return p.getName() + "'s " + this.getName() + " prevents the use of status moves!";
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
        public int flingDamage() {
            return 10;
        }

        @Override
        public void newTerrain(Battle b, ActivePokemon p, TerrainType newTerrain) {
            if (newTerrain == TerrainType.ELECTRIC && p.getStages().modifyStage(p, 1, Stat.DEFENSE, b, CastSource.HELD_ITEM)) {
                this.consumeItem(b, p);
            }
        }
    }

    static class GrassySeed extends Item implements HoldItem, TerrainCastEffect {
        private static final long serialVersionUID = 1L;

        GrassySeed() {
            super(ItemNamesies.GRASSY_SEED, "An item to be held by a Pok\u00e9mon. It boosts Defense on Grassy Terrain. It can only be used once.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public int flingDamage() {
            return 10;
        }

        @Override
        public void newTerrain(Battle b, ActivePokemon p, TerrainType newTerrain) {
            if (newTerrain == TerrainType.GRASS && p.getStages().modifyStage(p, 1, Stat.DEFENSE, b, CastSource.HELD_ITEM)) {
                this.consumeItem(b, p);
            }
        }
    }

    static class MistySeed extends Item implements HoldItem, TerrainCastEffect {
        private static final long serialVersionUID = 1L;

        MistySeed() {
            super(ItemNamesies.MISTY_SEED, "An item to be held by a Pok\u00e9mon. It boosts Sp. Def on Misty Terrain. It can only be used once.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public int flingDamage() {
            return 10;
        }

        @Override
        public void newTerrain(Battle b, ActivePokemon p, TerrainType newTerrain) {
            if (newTerrain == TerrainType.MISTY && p.getStages().modifyStage(p, 1, Stat.SP_DEFENSE, b, CastSource.HELD_ITEM)) {
                this.consumeItem(b, p);
            }
        }
    }

    static class PsychicSeed extends Item implements HoldItem, TerrainCastEffect {
        private static final long serialVersionUID = 1L;

        PsychicSeed() {
            super(ItemNamesies.PSYCHIC_SEED, "An item to be held by a Pok\u00e9mon. It boosts Sp. Def on Psychic Terrain. It can only be used once.", BagCategory.MISC);
            super.price = 4000;
        }

        @Override
        public int flingDamage() {
            return 10;
        }

        @Override
        public void newTerrain(Battle b, ActivePokemon p, TerrainType newTerrain) {
            if (newTerrain == TerrainType.PSYCHIC && p.getStages().modifyStage(p, 1, Stat.SP_DEFENSE, b, CastSource.HELD_ITEM)) {
                this.consumeItem(b, p);
            }
        }
    }

    static class HoneClawsTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        HoneClawsTM() {
            super(ItemNamesies.HONE_CLAWS_TM, "The user sharpens its claws to boost its Attack stat and accuracy.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.HONE_CLAWS;
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
            super(ItemNamesies.HAIL_TM, "The user summons a hailstorm lasting five turns. It damages all Pokémon except the Ice type.", BagCategory.TM);
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
            super(ItemNamesies.BLIZZARD_TM, "A howling blizzard is summoned to strike opposing Pokémon. This may also leave the opposing Pokémon frozen.", BagCategory.TM);
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
            super(ItemNamesies.PROTECT_TM, "Enables the user to evade all attacks. Its chance of failing rises if it is used in succession.", BagCategory.TM);
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
            super(ItemNamesies.ROOST_TM, "The user lands and rests its body. It restores the user's HP by up to half of its max HP.", BagCategory.TM);
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
            super(ItemNamesies.SMACK_DOWN_TM, "The user throws a stone or similar projectile to attack an opponent. A flying Pokémon will fall to the ground when it's hit.", BagCategory.TM);
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

    static class DigTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        DigTM() {
            super(ItemNamesies.DIG_TM, "The user burrows, then attacks on the next turn.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.DIG;
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
            super(ItemNamesies.SANDSTORM_TM, "A five-turn sandstorm is summoned to hurt all combatants except the Rock, Ground, and Steel types. It raises the Sp. Def stat of Rock types.", BagCategory.TM);
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
            super(ItemNamesies.FLAME_CHARGE_TM, "Cloaking itself in flame, the user attacks. Then, building up more power, the user raises its Speed stat.", BagCategory.TM);
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
            super(ItemNamesies.CHARGE_BEAM_TM, "The user attacks with an electric charge. The user may use any remaining electricity to raise its Sp. Atk stat.", BagCategory.TM);
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

    static class IncinerateTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        IncinerateTM() {
            super(ItemNamesies.INCINERATE_TM, "The user attacks opposing Pokémon with fire. If a Pokémon is holding a certain item, such as a Berry, the item becomes burned up and unusable.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.INCINERATE;
        }
    }

    static class WillOWispTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        WillOWispTM() {
            super(ItemNamesies.WILL_O_WISP_TM, "The user shoots a sinister, bluish-white flame at the target to inflict a burn.", BagCategory.TM);
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

    static class RetaliateTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        RetaliateTM() {
            super(ItemNamesies.RETALIATE_TM, "The user gets revenge for a fainted ally. If an ally fainted in the previous turn, this move's power is increased.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.RETALIATE;
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
            super(ItemNamesies.ROCK_POLISH_TM, "The user polishes its body to reduce drag. This can sharply raise the Speed stat.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.ROCK_POLISH;
        }
    }

    static class FlashTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        FlashTM() {
            super(ItemNamesies.FLASH_TM, "The user flashes a bright light that cuts the target's accuracy.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.FLASH;
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

    static class StruggleBugTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        StruggleBugTM() {
            super(ItemNamesies.STRUGGLE_BUG_TM, "While resisting, the user attacks the opposing Pokémon. This lowers the Sp. Atk stat of those hit.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.STRUGGLE_BUG;
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
            super(ItemNamesies.ROCK_SLIDE_TM, "Large boulders are hurled at the opposing Pokémon to inflict damage. This may also make the opposing Pokémon flinch.", BagCategory.TM);
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
            super(ItemNamesies.POISON_JAB_TM, "The target is stabbed with a tentacle or arm steeped in poison. This may also poison the target.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.POISON_JAB;
        }
    }

    static class DreamEaterTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        DreamEaterTM() {
            super(ItemNamesies.DREAM_EATER_TM, "The user eats the dreams of a sleeping target. It absorbs half the damage caused to heal its own HP.", BagCategory.TM);
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
            super(ItemNamesies.SUBSTITUTE_TM, "The user makes a copy of itself using some of its HP. The copy serves as the user's decoy.", BagCategory.TM);
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

    static class RockSmashTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        RockSmashTM() {
            super(ItemNamesies.ROCK_SMASH_TM, "The user attacks with a punch. This may also lower the target's Defense stat.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.ROCK_SMASH;
        }
    }

    static class SnarlTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        SnarlTM() {
            super(ItemNamesies.SNARL_TM, "The user yells as if it's ranting about something, which lowers the Sp. Atk stat of opposing Pokémon.", BagCategory.TM);
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

    static class PowerUpPunchTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        PowerUpPunchTM() {
            super(ItemNamesies.POWER_UP_PUNCH_TM, "Striking opponents over and over makes the user's fists harder. Hitting a target raises the Attack stat.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.POWER_UP_PUNCH;
        }
    }

    static class DazzlingGleamTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        DazzlingGleamTM() {
            super(ItemNamesies.DAZZLING_GLEAM_TM, "The user damages opposing Pokémon by emitting a powerful flash.", BagCategory.TM);
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

    static class CutTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        CutTM() {
            super(ItemNamesies.CUT_TM, "The target is cut with a scythe or claw.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.CUT;
        }
    }

    static class FlyTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        FlyTM() {
            super(ItemNamesies.FLY_TM, "The user soars and then strikes its target on the next turn.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.FLY;
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

    static class StrengthTM extends Item implements TechnicalMachine {
        private static final long serialVersionUID = 1L;

        StrengthTM() {
            super(ItemNamesies.STRENGTH_TM, "The target is slugged with a punch thrown at maximum power.", BagCategory.TM);
        }

        @Override
        public AttackNamesies getAttack() {
            return AttackNamesies.STRENGTH;
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
}
