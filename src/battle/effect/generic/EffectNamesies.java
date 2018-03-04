package battle.effect.generic;

import battle.effect.generic.BattleEffect.FieldUproar;
import battle.effect.generic.BattleEffect.Gravity;
import battle.effect.generic.BattleEffect.MagicRoom;
import battle.effect.generic.BattleEffect.MudSport;
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
import battle.effect.generic.Terrain.ElectricTerrain;
import battle.effect.generic.Terrain.GrassyTerrain;
import battle.effect.generic.Terrain.MistyTerrain;
import battle.effect.generic.Terrain.PsychicTerrain;
import battle.effect.generic.Weather.ClearSkies;
import battle.effect.generic.Weather.Hailing;
import battle.effect.generic.Weather.Raining;
import battle.effect.generic.Weather.Sandstorm;
import battle.effect.generic.Weather.Sunny;
import main.Global;
import util.StringUtils;

import java.util.function.Supplier;

public enum EffectNamesies {
    // EVERYTHING BELOW IS GENERATED ###
    LEECH_SEED(LeechSeed::new),
    FLINCH(Flinch::new),
    FIRE_SPIN(FireSpin::new),
    INFESTATION(Infestation::new),
    MAGMA_STORM(MagmaStorm::new),
    CLAMPED(Clamped::new),
    WHIRLPOOLED(Whirlpooled::new),
    WRAPPED(Wrapped::new),
    BINDED(Binded::new),
    SAND_TOMB(SandTomb::new),
    KINGS_SHIELD(KingsShield::new),
    SPIKY_SHIELD(SpikyShield::new),
    BANEFUL_BUNKER(BanefulBunker::new),
    PROTECTING(Protecting::new),
    QUICK_GUARD(QuickGuard::new),
    CRAFTY_SHIELD(CraftyShield::new),
    MAT_BLOCK(MatBlock::new),
    BRACING(Bracing::new),
    CONFUSION(Confusion::new),
    SELF_CONFUSION(SelfConfusion::new),
    SAFEGUARD(Safeguard::new),
    GUARD_SPECIAL(GuardSpecial::new),
    ENCORE(Encore::new),
    DISABLE(Disable::new),
    RAISE_CRITS(RaiseCrits::new),
    CHANGE_ITEM(ChangeItem::new),
    CHANGE_ATTACK_TYPE(ChangeAttackType::new),
    CHANGE_TYPE(ChangeType::new),
    CHANGE_ABILITY(ChangeAbility::new),
    STOCKPILE(Stockpile::new),
    USED_DEFENSE_CURL(UsedDefenseCurl::new),
    USED_MINIMIZE(UsedMinimize::new),
    MIMIC(Mimic::new),
    IMPRISON(Imprison::new),
    TRAPPED(Trapped::new),
    FORESIGHT(Foresight::new),
    MIRACLE_EYE(MiracleEye::new),
    TORMENT(Torment::new),
    SOUND_BLOCK(SoundBlock::new),
    TAUNT(Taunt::new),
    LASER_FOCUS(LaserFocus::new),
    LOCK_ON(LockOn::new),
    TELEKINESIS(Telekinesis::new),
    INGRAIN(Ingrain::new),
    GROUNDED(Grounded::new),
    CURSE(Curse::new),
    YAWN(Yawn::new),
    MAGNET_RISE(MagnetRise::new),
    UPROAR(Uproar::new),
    AQUA_RING(AquaRing::new),
    NIGHTMARE(Nightmare::new),
    CHARGE(Charge::new),
    FOCUSING(Focusing::new),
    SHELL_TRAP(ShellTrap::new),
    BEAK_BLAST(BeakBlast::new),
    FIDDY_PERCENT_STRONGER(FiddyPercentStronger::new),
    TRANSFORMED(Transformed::new),
    SUBSTITUTE(Substitute::new),
    MIST(Mist::new),
    MAGIC_COAT(MagicCoat::new),
    BIDE(Bide::new),
    HALF_WEIGHT(HalfWeight::new),
    POWER_TRICK(PowerTrick::new),
    POWER_SPLIT(PowerSplit::new),
    GUARD_SPLIT(GuardSplit::new),
    HEAL_BLOCK(HealBlock::new),
    INFATUATED(Infatuated::new),
    SNATCH(Snatch::new),
    GRUDGE(Grudge::new),
    DESTINY_BOND(DestinyBond::new),
    PERISH_SONG(PerishSong::new),
    EMBARGO(Embargo::new),
    CONSUMED_ITEM(ConsumedItem::new),
    FAIRY_LOCK(FairyLock::new),
    POWDER(Powder::new),
    EATEN_BERRY(EatenBerry::new),
    BREAKS_THE_MOLD(BreaksTheMold::new),
    RAGING(Raging::new),
    REFLECT(Reflect::new),
    LIGHT_SCREEN(LightScreen::new),
    TAILWIND(Tailwind::new),
    AURORA_VEIL(AuroraVeil::new),
    STICKY_WEB(StickyWeb::new),
    STEALTH_ROCK(StealthRock::new),
    TOXIC_SPIKES(ToxicSpikes::new),
    SPIKES(Spikes::new),
    WISH(Wish::new),
    LUCKY_CHANT(LuckyChant::new),
    FUTURE_SIGHT(FutureSight::new),
    DOOM_DESIRE(DoomDesire::new),
    HEAL_SWITCH(HealSwitch::new),
    DEAD_ALLY(DeadAlly::new),
    PAY_DAY(PayDay::new),
    GET_DAT_CASH_MONEY_TWICE(GetDatCashMoneyTwice::new),
    GRAVITY(Gravity::new),
    WATER_SPORT(WaterSport::new),
    MUD_SPORT(MudSport::new),
    WONDER_ROOM(WonderRoom::new),
    TRICK_ROOM(TrickRoom::new),
    MAGIC_ROOM(MagicRoom::new),
    FIELD_UPROAR(FieldUproar::new),
    CLEAR_SKIES(ClearSkies::new),
    RAINING(Raining::new),
    SUNNY(Sunny::new),
    SANDSTORM(Sandstorm::new),
    HAILING(Hailing::new),
    MISTY_TERRAIN(MistyTerrain::new),
    GRASSY_TERRAIN(GrassyTerrain::new),
    ELECTRIC_TERRAIN(ElectricTerrain::new),
    PSYCHIC_TERRAIN(PsychicTerrain::new);

    // EVERYTHING ABOVE IS GENERATED ###

    private final Supplier<Effect> effectCreator;

    EffectNamesies(Supplier<Effect> effectCreator) {
        this.effectCreator = effectCreator;
    }

    public Effect getEffect() {
        return this.effectCreator.get();
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

