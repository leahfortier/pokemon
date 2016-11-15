package battle.effect.generic;

import main.Global;
import util.PokeString;

public enum EffectNamesies {
    // EVERYTHING BELOW IS GENERATED ###
	LEECH_SEED("LeechSeed"),
	BAD_POISON("BadPoison"),
	FLINCH("Flinch"),
	FIRE_SPIN("FireSpin"),
	INFESTATION("Infestation"),
	MAGMA_STORM("MagmaStorm"),
	CLAMPED("Clamped"),
	WHIRLPOOLED("Whirlpooled"),
	WRAPPED("Wrapped"),
	BINDED("Binded"),
	SAND_TOMB("SandTomb"),
	KINGS_SHIELD("KingsShield"),
	SPIKY_SHIELD("SpikyShield"),
	PROTECTING("Protecting"),
	QUICK_GUARD("QuickGuard"),
	CRAFTY_SHIELD("CraftyShield"),
	MAT_BLOCK("MatBlock"),
	BRACING("Bracing"),
	CONFUSION("Confusion"),
	SELF_CONFUSION("SelfConfusion"),
	SAFEGUARD("Safeguard"),
	GUARD_SPECIAL("GuardSpecial"),
	ENCORE("Encore"),
	DISABLE("Disable"),
	RAISE_CRITS("RaiseCrits"),
	CHANGE_ITEM("ChangeItem"),
	CHANGE_TYPE("ChangeType"),
	CHANGE_ABILITY("ChangeAbility"),
	STOCKPILE("Stockpile"),
	USED_DEFENSE_CURL("UsedDefenseCurl"),
	USED_MINIMIZE("UsedMinimize"),
	MIMIC("Mimic"),
	IMPRISON("Imprison"),
	TRAPPED("Trapped"),
	FORESIGHT("Foresight"),
	MIRACLE_EYE("MiracleEye"),
	TORMENT("Torment"),
	TAUNT("Taunt"),
	LOCK_ON("LockOn"),
	TELEKINESIS("Telekinesis"),
	INGRAIN("Ingrain"),
	GROUNDED("Grounded"),
	CURSE("Curse"),
	YAWN("Yawn"),
	MAGNET_RISE("MagnetRise"),
	UPROAR("Uproar"),
	AQUA_RING("AquaRing"),
	NIGHTMARE("Nightmare"),
	CHARGE("Charge"),
	FOCUSING("Focusing"),
	FIDDY_PERCENT_STRONGER("FiddyPercentStronger"),
	TRANSFORMED("Transformed"),
	SUBSTITUTE("Substitute"),
	MIST("Mist"),
	MAGIC_COAT("MagicCoat"),
	BIDE("Bide"),
	HALF_WEIGHT("HalfWeight"),
	POWER_TRICK("PowerTrick"),
	POWER_SPLIT("PowerSplit"),
	GUARD_SPLIT("GuardSplit"),
	HEAL_BLOCK("HealBlock"),
	INFATUATED("Infatuated"),
	SNATCH("Snatch"),
	GRUDGE("Grudge"),
	DESTINY_BOND("DestinyBond"),
	PERISH_SONG("PerishSong"),
	EMBARGO("Embargo"),
	CONSUMED_ITEM("ConsumedItem"),
	FAIRY_LOCK("FairyLock"),
	POWDER("Powder"),
	ELECTRIFIED("Electrified"),
	EATEN_BERRY("EatenBerry"),
	REFLECT("Reflect"),
	LIGHT_SCREEN("LightScreen"),
	TAILWIND("Tailwind"),
	STICKY_WEB("StickyWeb"),
	STEALTH_ROCK("StealthRock"),
	TOXIC_SPIKES("ToxicSpikes"),
	SPIKES("Spikes"),
	WISH("Wish"),
	LUCKY_CHANT("LuckyChant"),
	FUTURE_SIGHT("FutureSight"),
	DOOM_DESIRE("DoomDesire"),
	HEAL_SWITCH("HealSwitch"),
	DEAD_ALLY("DeadAlly"),
	PAY_DAY("PayDay"),
	GET_DAT_CASH_MONEY_TWICE("GetDatCashMoneyTwice"),
	GRAVITY("Gravity"),
	WATER_SPORT("WaterSport"),
	MUD_SPORT("MudSport"),
	WONDER_ROOM("WonderRoom"),
	TRICK_ROOM("TrickRoom"),
	MAGIC_ROOM("MagicRoom"),
	MISTY_TERRAIN("MistyTerrain"),
	GRASSY_TERRAIN("GrassyTerrain"),
	ELECTRIC_TERRAIN("ElectricTerrain"),
	CLEAR_SKIES("ClearSkies"),
	RAINING("Raining"),
	SUNNY("Sunny"),
	SANDSTORM("Sandstorm"),
	HAILING("Hailing");

    // EVERYTHING ABOVE IS GENERATED ###

    private String name;

    EffectNamesies(String name) {
        this.name = name;
    }

	public String getName() {
		return this.name;
	}

	public static EffectNamesies getValueOf(String name) {
		try {
			return EffectNamesies.valueOf(PokeString.getNamesiesString(name));
		} catch (IllegalArgumentException exception) {
			Global.error(name + " does not have a valid EffectNamesies value");
			return null;
		}
	}
}

