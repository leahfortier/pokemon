package battle.effect.generic;

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

import java.util.function.Supplier;

public enum PokemonEffectNamesies implements EffectNamesies {
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
    RAGING(Raging::new);

    // EVERYTHING ABOVE IS GENERATED ###

    private final Supplier<PokemonEffect> effectCreator;

    PokemonEffectNamesies(Supplier<PokemonEffect> effectCreator) {
        this.effectCreator = effectCreator;
    }

    @Override
    public PokemonEffect getEffect() {
        return this.effectCreator.get();
    }
}

