package battle.effect.generic;

import battle.effect.generic.BattleEffect.ElectricTerrain;
import battle.effect.generic.BattleEffect.FieldUproar;
import battle.effect.generic.BattleEffect.GrassyTerrain;
import battle.effect.generic.BattleEffect.Gravity;
import battle.effect.generic.BattleEffect.MagicRoom;
import battle.effect.generic.BattleEffect.MistyTerrain;
import battle.effect.generic.BattleEffect.MudSport;
import battle.effect.generic.BattleEffect.PsychicTerrain;
import battle.effect.generic.BattleEffect.TrickRoom;
import battle.effect.generic.BattleEffect.WaterSport;
import battle.effect.generic.BattleEffect.WonderRoom;
import battle.effect.generic.PokemonEffect.AquaRing;
import battle.effect.generic.PokemonEffect.BanefulBunker;
import battle.effect.generic.PokemonEffect.BeakBlast;
import battle.effect.generic.PokemonEffect.Bide;
import battle.effect.generic.PokemonEffect.Binded;
import battle.effect.generic.PokemonEffect.Bracing;
import battle.effect.generic.PokemonEffect.BreaksTheMold;
import battle.effect.generic.PokemonEffect.ChangeAbility;
import battle.effect.generic.PokemonEffect.ChangeAttackType;
import battle.effect.generic.PokemonEffect.ChangeItem;
import battle.effect.generic.PokemonEffect.ChangeType;
import battle.effect.generic.PokemonEffect.Charge;
import battle.effect.generic.PokemonEffect.Clamped;
import battle.effect.generic.PokemonEffect.Confusion;
import battle.effect.generic.PokemonEffect.ConsumedItem;
import battle.effect.generic.PokemonEffect.CraftyShield;
import battle.effect.generic.PokemonEffect.Curse;
import battle.effect.generic.PokemonEffect.DestinyBond;
import battle.effect.generic.PokemonEffect.Disable;
import battle.effect.generic.PokemonEffect.EatenBerry;
import battle.effect.generic.PokemonEffect.Embargo;
import battle.effect.generic.PokemonEffect.Encore;
import battle.effect.generic.PokemonEffect.FairyLock;
import battle.effect.generic.PokemonEffect.FiddyPercentStronger;
import battle.effect.generic.PokemonEffect.FireSpin;
import battle.effect.generic.PokemonEffect.Flinch;
import battle.effect.generic.PokemonEffect.Focusing;
import battle.effect.generic.PokemonEffect.Foresight;
import battle.effect.generic.PokemonEffect.Grounded;
import battle.effect.generic.PokemonEffect.Grudge;
import battle.effect.generic.PokemonEffect.GuardSpecial;
import battle.effect.generic.PokemonEffect.GuardSplit;
import battle.effect.generic.PokemonEffect.HalfWeight;
import battle.effect.generic.PokemonEffect.HealBlock;
import battle.effect.generic.PokemonEffect.Imprison;
import battle.effect.generic.PokemonEffect.Infatuated;
import battle.effect.generic.PokemonEffect.Infestation;
import battle.effect.generic.PokemonEffect.Ingrain;
import battle.effect.generic.PokemonEffect.KingsShield;
import battle.effect.generic.PokemonEffect.LaserFocus;
import battle.effect.generic.PokemonEffect.LeechSeed;
import battle.effect.generic.PokemonEffect.LockOn;
import battle.effect.generic.PokemonEffect.MagicCoat;
import battle.effect.generic.PokemonEffect.MagmaStorm;
import battle.effect.generic.PokemonEffect.MagnetRise;
import battle.effect.generic.PokemonEffect.MatBlock;
import battle.effect.generic.PokemonEffect.Mimic;
import battle.effect.generic.PokemonEffect.MiracleEye;
import battle.effect.generic.PokemonEffect.Mist;
import battle.effect.generic.PokemonEffect.Nightmare;
import battle.effect.generic.PokemonEffect.PerishSong;
import battle.effect.generic.PokemonEffect.Powder;
import battle.effect.generic.PokemonEffect.PowerSplit;
import battle.effect.generic.PokemonEffect.PowerTrick;
import battle.effect.generic.PokemonEffect.Protecting;
import battle.effect.generic.PokemonEffect.QuickGuard;
import battle.effect.generic.PokemonEffect.Raging;
import battle.effect.generic.PokemonEffect.RaiseCrits;
import battle.effect.generic.PokemonEffect.Safeguard;
import battle.effect.generic.PokemonEffect.SandTomb;
import battle.effect.generic.PokemonEffect.SelfConfusion;
import battle.effect.generic.PokemonEffect.ShellTrap;
import battle.effect.generic.PokemonEffect.Snatch;
import battle.effect.generic.PokemonEffect.SoundBlock;
import battle.effect.generic.PokemonEffect.SpikyShield;
import battle.effect.generic.PokemonEffect.Stockpile;
import battle.effect.generic.PokemonEffect.Substitute;
import battle.effect.generic.PokemonEffect.Taunt;
import battle.effect.generic.PokemonEffect.Telekinesis;
import battle.effect.generic.PokemonEffect.Torment;
import battle.effect.generic.PokemonEffect.Transformed;
import battle.effect.generic.PokemonEffect.Trapped;
import battle.effect.generic.PokemonEffect.Uproar;
import battle.effect.generic.PokemonEffect.UsedDefenseCurl;
import battle.effect.generic.PokemonEffect.UsedMinimize;
import battle.effect.generic.PokemonEffect.Whirlpooled;
import battle.effect.generic.PokemonEffect.Wrapped;
import battle.effect.generic.PokemonEffect.Yawn;
import battle.effect.generic.TeamEffect.AuroraVeil;
import battle.effect.generic.TeamEffect.DeadAlly;
import battle.effect.generic.TeamEffect.DoomDesire;
import battle.effect.generic.TeamEffect.FutureSight;
import battle.effect.generic.TeamEffect.GetDatCashMoneyTwice;
import battle.effect.generic.TeamEffect.HealSwitch;
import battle.effect.generic.TeamEffect.LightScreen;
import battle.effect.generic.TeamEffect.LuckyChant;
import battle.effect.generic.TeamEffect.PayDay;
import battle.effect.generic.TeamEffect.Reflect;
import battle.effect.generic.TeamEffect.Spikes;
import battle.effect.generic.TeamEffect.StealthRock;
import battle.effect.generic.TeamEffect.StickyWeb;
import battle.effect.generic.TeamEffect.Tailwind;
import battle.effect.generic.TeamEffect.ToxicSpikes;
import battle.effect.generic.TeamEffect.Wish;
import battle.effect.generic.Weather.ClearSkies;
import battle.effect.generic.Weather.Hailing;
import battle.effect.generic.Weather.Raining;
import battle.effect.generic.Weather.Sandstorm;
import battle.effect.generic.Weather.Sunny;
import main.Global;
import util.StringUtils;

public enum EffectNamesies {
    // EVERYTHING BELOW IS GENERATED ###
    LEECH_SEED("LeechSeed", LeechSeed::new),
    FLINCH("Flinch", Flinch::new),
    FIRE_SPIN("FireSpin", FireSpin::new),
    INFESTATION("Infestation", Infestation::new),
    MAGMA_STORM("MagmaStorm", MagmaStorm::new),
    CLAMPED("Clamped", Clamped::new),
    WHIRLPOOLED("Whirlpooled", Whirlpooled::new),
    WRAPPED("Wrapped", Wrapped::new),
    BINDED("Binded", Binded::new),
    SAND_TOMB("SandTomb", SandTomb::new),
    KINGS_SHIELD("KingsShield", KingsShield::new),
    SPIKY_SHIELD("SpikyShield", SpikyShield::new),
    BANEFUL_BUNKER("BanefulBunker", BanefulBunker::new),
    PROTECTING("Protecting", Protecting::new),
    QUICK_GUARD("QuickGuard", QuickGuard::new),
    CRAFTY_SHIELD("CraftyShield", CraftyShield::new),
    MAT_BLOCK("MatBlock", MatBlock::new),
    BRACING("Bracing", Bracing::new),
    CONFUSION("Confusion", Confusion::new),
    SELF_CONFUSION("SelfConfusion", SelfConfusion::new),
    SAFEGUARD("Safeguard", Safeguard::new),
    GUARD_SPECIAL("GuardSpecial", GuardSpecial::new),
    ENCORE("Encore", Encore::new),
    DISABLE("Disable", Disable::new),
    RAISE_CRITS("RaiseCrits", RaiseCrits::new),
    CHANGE_ITEM("ChangeItem", ChangeItem::new),
    CHANGE_ATTACK_TYPE("ChangeAttackType", ChangeAttackType::new),
    CHANGE_TYPE("ChangeType", ChangeType::new),
    CHANGE_ABILITY("ChangeAbility", ChangeAbility::new),
    STOCKPILE("Stockpile", Stockpile::new),
    USED_DEFENSE_CURL("UsedDefenseCurl", UsedDefenseCurl::new),
    USED_MINIMIZE("UsedMinimize", UsedMinimize::new),
    MIMIC("Mimic", Mimic::new),
    IMPRISON("Imprison", Imprison::new),
    TRAPPED("Trapped", Trapped::new),
    FORESIGHT("Foresight", Foresight::new),
    MIRACLE_EYE("MiracleEye", MiracleEye::new),
    TORMENT("Torment", Torment::new),
    SOUND_BLOCK("SoundBlock", SoundBlock::new),
    TAUNT("Taunt", Taunt::new),
    LASER_FOCUS("LaserFocus", LaserFocus::new),
    LOCK_ON("LockOn", LockOn::new),
    TELEKINESIS("Telekinesis", Telekinesis::new),
    INGRAIN("Ingrain", Ingrain::new),
    GROUNDED("Grounded", Grounded::new),
    CURSE("Curse", Curse::new),
    YAWN("Yawn", Yawn::new),
    MAGNET_RISE("MagnetRise", MagnetRise::new),
    UPROAR("Uproar", Uproar::new),
    AQUA_RING("AquaRing", AquaRing::new),
    NIGHTMARE("Nightmare", Nightmare::new),
    CHARGE("Charge", Charge::new),
    FOCUSING("Focusing", Focusing::new),
    SHELL_TRAP("ShellTrap", ShellTrap::new),
    BEAK_BLAST("BeakBlast", BeakBlast::new),
    FIDDY_PERCENT_STRONGER("FiddyPercentStronger", FiddyPercentStronger::new),
    TRANSFORMED("Transformed", Transformed::new),
    SUBSTITUTE("Substitute", Substitute::new),
    MIST("Mist", Mist::new),
    MAGIC_COAT("MagicCoat", MagicCoat::new),
    BIDE("Bide", Bide::new),
    HALF_WEIGHT("HalfWeight", HalfWeight::new),
    POWER_TRICK("PowerTrick", PowerTrick::new),
    POWER_SPLIT("PowerSplit", PowerSplit::new),
    GUARD_SPLIT("GuardSplit", GuardSplit::new),
    HEAL_BLOCK("HealBlock", HealBlock::new),
    INFATUATED("Infatuated", Infatuated::new),
    SNATCH("Snatch", Snatch::new),
    GRUDGE("Grudge", Grudge::new),
    DESTINY_BOND("DestinyBond", DestinyBond::new),
    PERISH_SONG("PerishSong", PerishSong::new),
    EMBARGO("Embargo", Embargo::new),
    CONSUMED_ITEM("ConsumedItem", ConsumedItem::new),
    FAIRY_LOCK("FairyLock", FairyLock::new),
    POWDER("Powder", Powder::new),
    EATEN_BERRY("EatenBerry", EatenBerry::new),
    BREAKS_THE_MOLD("BreaksTheMold", BreaksTheMold::new),
    RAGING("Raging", Raging::new),
    REFLECT("Reflect", Reflect::new),
    LIGHT_SCREEN("LightScreen", LightScreen::new),
    TAILWIND("Tailwind", Tailwind::new),
    AURORA_VEIL("AuroraVeil", AuroraVeil::new),
    STICKY_WEB("StickyWeb", StickyWeb::new),
    STEALTH_ROCK("StealthRock", StealthRock::new),
    TOXIC_SPIKES("ToxicSpikes", ToxicSpikes::new),
    SPIKES("Spikes", Spikes::new),
    WISH("Wish", Wish::new),
    LUCKY_CHANT("LuckyChant", LuckyChant::new),
    FUTURE_SIGHT("FutureSight", FutureSight::new),
    DOOM_DESIRE("DoomDesire", DoomDesire::new),
    HEAL_SWITCH("HealSwitch", HealSwitch::new),
    DEAD_ALLY("DeadAlly", DeadAlly::new),
    PAY_DAY("PayDay", PayDay::new),
    GET_DAT_CASH_MONEY_TWICE("GetDatCashMoneyTwice", GetDatCashMoneyTwice::new),
    GRAVITY("Gravity", Gravity::new),
    WATER_SPORT("WaterSport", WaterSport::new),
    MUD_SPORT("MudSport", MudSport::new),
    WONDER_ROOM("WonderRoom", WonderRoom::new),
    TRICK_ROOM("TrickRoom", TrickRoom::new),
    MAGIC_ROOM("MagicRoom", MagicRoom::new),
    MISTY_TERRAIN("MistyTerrain", MistyTerrain::new),
    GRASSY_TERRAIN("GrassyTerrain", GrassyTerrain::new),
    ELECTRIC_TERRAIN("ElectricTerrain", ElectricTerrain::new),
    PSYCHIC_TERRAIN("PsychicTerrain", PsychicTerrain::new),
    FIELD_UPROAR("FieldUproar", FieldUproar::new),
    CLEAR_SKIES("ClearSkies", ClearSkies::new),
    RAINING("Raining", Raining::new),
    SUNNY("Sunny", Sunny::new),
    SANDSTORM("Sandstorm", Sandstorm::new),
    HAILING("Hailing", Hailing::new);

    // EVERYTHING ABOVE IS GENERATED ###

    private final String name;
    private final EffectCreator effectCreator;

    EffectNamesies(String name, EffectCreator effectCreator) {
        this.name = name;
        this.effectCreator = effectCreator;
    }

    @FunctionalInterface
    private interface EffectCreator {
        Effect createEffect();
    }

    public Effect getEffect() {
        return this.effectCreator.createEffect();
    }

    public String getName() {
        return this.name;
    }

    public static EffectNamesies getValueOf(String name) {
        try {
            return EffectNamesies.valueOf(StringUtils.getNamesiesString(name));
        } catch (IllegalArgumentException exception) {
            Global.error(name + " does not have a valid EffectNamesies value");
            return null;
        }
    }
}

