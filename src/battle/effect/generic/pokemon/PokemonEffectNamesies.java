package battle.effect.generic.pokemon;

import battle.effect.generic.EffectNamesies;
import battle.effect.generic.pokemon.PokemonEffect.AquaRing;
import battle.effect.generic.pokemon.PokemonEffect.BanefulBunker;
import battle.effect.generic.pokemon.PokemonEffect.BeakBlast;
import battle.effect.generic.pokemon.PokemonEffect.Bide;
import battle.effect.generic.pokemon.PokemonEffect.Binded;
import battle.effect.generic.pokemon.PokemonEffect.Bracing;
import battle.effect.generic.pokemon.PokemonEffect.BreaksTheMold;
import battle.effect.generic.pokemon.PokemonEffect.ChangeAbility;
import battle.effect.generic.pokemon.PokemonEffect.ChangeAttackType;
import battle.effect.generic.pokemon.PokemonEffect.ChangeItem;
import battle.effect.generic.pokemon.PokemonEffect.ChangeType;
import battle.effect.generic.pokemon.PokemonEffect.Charge;
import battle.effect.generic.pokemon.PokemonEffect.Clamped;
import battle.effect.generic.pokemon.PokemonEffect.Confusion;
import battle.effect.generic.pokemon.PokemonEffect.ConsumedItem;
import battle.effect.generic.pokemon.PokemonEffect.CraftyShield;
import battle.effect.generic.pokemon.PokemonEffect.Curse;
import battle.effect.generic.pokemon.PokemonEffect.DestinyBond;
import battle.effect.generic.pokemon.PokemonEffect.Disable;
import battle.effect.generic.pokemon.PokemonEffect.EatenBerry;
import battle.effect.generic.pokemon.PokemonEffect.Embargo;
import battle.effect.generic.pokemon.PokemonEffect.Encore;
import battle.effect.generic.pokemon.PokemonEffect.FairyLock;
import battle.effect.generic.pokemon.PokemonEffect.FiddyPercentStronger;
import battle.effect.generic.pokemon.PokemonEffect.FireSpin;
import battle.effect.generic.pokemon.PokemonEffect.Flinch;
import battle.effect.generic.pokemon.PokemonEffect.Focusing;
import battle.effect.generic.pokemon.PokemonEffect.Foresight;
import battle.effect.generic.pokemon.PokemonEffect.Grounded;
import battle.effect.generic.pokemon.PokemonEffect.Grudge;
import battle.effect.generic.pokemon.PokemonEffect.GuardSpecial;
import battle.effect.generic.pokemon.PokemonEffect.GuardSplit;
import battle.effect.generic.pokemon.PokemonEffect.HalfWeight;
import battle.effect.generic.pokemon.PokemonEffect.HealBlock;
import battle.effect.generic.pokemon.PokemonEffect.Imprison;
import battle.effect.generic.pokemon.PokemonEffect.Infatuated;
import battle.effect.generic.pokemon.PokemonEffect.Infestation;
import battle.effect.generic.pokemon.PokemonEffect.Ingrain;
import battle.effect.generic.pokemon.PokemonEffect.KingsShield;
import battle.effect.generic.pokemon.PokemonEffect.LaserFocus;
import battle.effect.generic.pokemon.PokemonEffect.LeechSeed;
import battle.effect.generic.pokemon.PokemonEffect.LockOn;
import battle.effect.generic.pokemon.PokemonEffect.MagicCoat;
import battle.effect.generic.pokemon.PokemonEffect.MagmaStorm;
import battle.effect.generic.pokemon.PokemonEffect.MagnetRise;
import battle.effect.generic.pokemon.PokemonEffect.MatBlock;
import battle.effect.generic.pokemon.PokemonEffect.Mimic;
import battle.effect.generic.pokemon.PokemonEffect.MiracleEye;
import battle.effect.generic.pokemon.PokemonEffect.Mist;
import battle.effect.generic.pokemon.PokemonEffect.Nightmare;
import battle.effect.generic.pokemon.PokemonEffect.PerishSong;
import battle.effect.generic.pokemon.PokemonEffect.Powder;
import battle.effect.generic.pokemon.PokemonEffect.PowerSplit;
import battle.effect.generic.pokemon.PokemonEffect.PowerTrick;
import battle.effect.generic.pokemon.PokemonEffect.Protecting;
import battle.effect.generic.pokemon.PokemonEffect.QuickGuard;
import battle.effect.generic.pokemon.PokemonEffect.Raging;
import battle.effect.generic.pokemon.PokemonEffect.RaiseCrits;
import battle.effect.generic.pokemon.PokemonEffect.Safeguard;
import battle.effect.generic.pokemon.PokemonEffect.SandTomb;
import battle.effect.generic.pokemon.PokemonEffect.SelfConfusion;
import battle.effect.generic.pokemon.PokemonEffect.ShellTrap;
import battle.effect.generic.pokemon.PokemonEffect.Snatch;
import battle.effect.generic.pokemon.PokemonEffect.SoundBlock;
import battle.effect.generic.pokemon.PokemonEffect.SpikyShield;
import battle.effect.generic.pokemon.PokemonEffect.Stockpile;
import battle.effect.generic.pokemon.PokemonEffect.Substitute;
import battle.effect.generic.pokemon.PokemonEffect.Taunt;
import battle.effect.generic.pokemon.PokemonEffect.Telekinesis;
import battle.effect.generic.pokemon.PokemonEffect.Torment;
import battle.effect.generic.pokemon.PokemonEffect.Transformed;
import battle.effect.generic.pokemon.PokemonEffect.Trapped;
import battle.effect.generic.pokemon.PokemonEffect.Uproar;
import battle.effect.generic.pokemon.PokemonEffect.UsedDefenseCurl;
import battle.effect.generic.pokemon.PokemonEffect.UsedMinimize;
import battle.effect.generic.pokemon.PokemonEffect.Whirlpooled;
import battle.effect.generic.pokemon.PokemonEffect.Wrapped;
import battle.effect.generic.pokemon.PokemonEffect.Yawn;

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

