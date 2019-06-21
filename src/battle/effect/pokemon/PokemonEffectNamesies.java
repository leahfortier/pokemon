package battle.effect.pokemon;

import battle.effect.EffectNamesies;
import battle.effect.pokemon.PokemonEffect.AquaRing;
import battle.effect.pokemon.PokemonEffect.BanefulBunker;
import battle.effect.pokemon.PokemonEffect.BeakBlast;
import battle.effect.pokemon.PokemonEffect.Bide;
import battle.effect.pokemon.PokemonEffect.Binded;
import battle.effect.pokemon.PokemonEffect.Bracing;
import battle.effect.pokemon.PokemonEffect.BreaksTheMold;
import battle.effect.pokemon.PokemonEffect.ChangeAbility;
import battle.effect.pokemon.PokemonEffect.ChangeAttackType;
import battle.effect.pokemon.PokemonEffect.ChangeItem;
import battle.effect.pokemon.PokemonEffect.ChangeType;
import battle.effect.pokemon.PokemonEffect.Charge;
import battle.effect.pokemon.PokemonEffect.Clamped;
import battle.effect.pokemon.PokemonEffect.Confusion;
import battle.effect.pokemon.PokemonEffect.ConsumedItem;
import battle.effect.pokemon.PokemonEffect.CraftyShield;
import battle.effect.pokemon.PokemonEffect.Curse;
import battle.effect.pokemon.PokemonEffect.DestinyBond;
import battle.effect.pokemon.PokemonEffect.Disable;
import battle.effect.pokemon.PokemonEffect.EatenBerry;
import battle.effect.pokemon.PokemonEffect.Embargo;
import battle.effect.pokemon.PokemonEffect.Encore;
import battle.effect.pokemon.PokemonEffect.FairyLock;
import battle.effect.pokemon.PokemonEffect.FiddyPercentStronger;
import battle.effect.pokemon.PokemonEffect.FireSpin;
import battle.effect.pokemon.PokemonEffect.Flinch;
import battle.effect.pokemon.PokemonEffect.Focusing;
import battle.effect.pokemon.PokemonEffect.Foresight;
import battle.effect.pokemon.PokemonEffect.Grounded;
import battle.effect.pokemon.PokemonEffect.Grudge;
import battle.effect.pokemon.PokemonEffect.GuardSplit;
import battle.effect.pokemon.PokemonEffect.HalfWeight;
import battle.effect.pokemon.PokemonEffect.HealBlock;
import battle.effect.pokemon.PokemonEffect.Imprison;
import battle.effect.pokemon.PokemonEffect.Infatuation;
import battle.effect.pokemon.PokemonEffect.Infestation;
import battle.effect.pokemon.PokemonEffect.Ingrain;
import battle.effect.pokemon.PokemonEffect.KingsShield;
import battle.effect.pokemon.PokemonEffect.LaserFocus;
import battle.effect.pokemon.PokemonEffect.LeechSeed;
import battle.effect.pokemon.PokemonEffect.LockOn;
import battle.effect.pokemon.PokemonEffect.MagicCoat;
import battle.effect.pokemon.PokemonEffect.MagmaStorm;
import battle.effect.pokemon.PokemonEffect.MagnetRise;
import battle.effect.pokemon.PokemonEffect.MatBlock;
import battle.effect.pokemon.PokemonEffect.Mimic;
import battle.effect.pokemon.PokemonEffect.MiracleEye;
import battle.effect.pokemon.PokemonEffect.Nightmare;
import battle.effect.pokemon.PokemonEffect.PerishSong;
import battle.effect.pokemon.PokemonEffect.Powder;
import battle.effect.pokemon.PokemonEffect.PowerSplit;
import battle.effect.pokemon.PokemonEffect.PowerTrick;
import battle.effect.pokemon.PokemonEffect.Protect;
import battle.effect.pokemon.PokemonEffect.QuickGuard;
import battle.effect.pokemon.PokemonEffect.Raging;
import battle.effect.pokemon.PokemonEffect.RaiseCrits;
import battle.effect.pokemon.PokemonEffect.SandTomb;
import battle.effect.pokemon.PokemonEffect.SelfConfusion;
import battle.effect.pokemon.PokemonEffect.ShellTrap;
import battle.effect.pokemon.PokemonEffect.Silence;
import battle.effect.pokemon.PokemonEffect.Snatch;
import battle.effect.pokemon.PokemonEffect.SpikyShield;
import battle.effect.pokemon.PokemonEffect.Stockpile;
import battle.effect.pokemon.PokemonEffect.Substitute;
import battle.effect.pokemon.PokemonEffect.Taunt;
import battle.effect.pokemon.PokemonEffect.Telekinesis;
import battle.effect.pokemon.PokemonEffect.Torment;
import battle.effect.pokemon.PokemonEffect.Transformed;
import battle.effect.pokemon.PokemonEffect.Trapped;
import battle.effect.pokemon.PokemonEffect.Uproar;
import battle.effect.pokemon.PokemonEffect.UsedDefenseCurl;
import battle.effect.pokemon.PokemonEffect.UsedMinimize;
import battle.effect.pokemon.PokemonEffect.Whirlpooled;
import battle.effect.pokemon.PokemonEffect.Wrapped;
import battle.effect.pokemon.PokemonEffect.Yawn;

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
    PROTECT(Protect::new),
    QUICK_GUARD(QuickGuard::new),
    CRAFTY_SHIELD(CraftyShield::new),
    MAT_BLOCK(MatBlock::new),
    BRACING(Bracing::new),
    CONFUSION(Confusion::new),
    SELF_CONFUSION(SelfConfusion::new),
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
    SILENCE(Silence::new),
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
    MAGIC_COAT(MagicCoat::new),
    BIDE(Bide::new),
    HALF_WEIGHT(HalfWeight::new),
    POWER_TRICK(PowerTrick::new),
    POWER_SPLIT(PowerSplit::new),
    GUARD_SPLIT(GuardSplit::new),
    HEAL_BLOCK(HealBlock::new),
    INFATUATION(Infatuation::new),
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
