package pokemon;

import battle.Attack;
import battle.Battle;
import battle.Move;
import battle.MoveCategory;
import battle.MoveType;
import battle.effect.DamageBlocker;
import battle.effect.DefiniteEscape;
import battle.effect.ModifyStageValueEffect;
import battle.effect.StallingEffect;
import battle.effect.SwitchOutEffect;
import battle.effect.attack.ChangeAbilityMove;
import battle.effect.attack.ChangeTypeMove;
import battle.effect.generic.Effect.CastSource;
import battle.effect.generic.EffectInterfaces.AccuracyBypassEffect;
import battle.effect.generic.EffectInterfaces.AdvantageChanger;
import battle.effect.generic.EffectInterfaces.ApplyDamageEffect;
import battle.effect.generic.EffectInterfaces.BeforeTurnEffect;
import battle.effect.generic.EffectInterfaces.BracingEffect;
import battle.effect.generic.EffectInterfaces.ChangeAttackTypeEffect;
import battle.effect.generic.EffectInterfaces.ChangeTypeEffect;
import battle.effect.generic.EffectInterfaces.CrashDamageMove;
import battle.effect.generic.EffectInterfaces.CritBlockerEffect;
import battle.effect.generic.EffectInterfaces.CritStageEffect;
import battle.effect.generic.EffectInterfaces.DifferentStatEffect;
import battle.effect.generic.EffectInterfaces.EffectBlockerEffect;
import battle.effect.generic.EffectInterfaces.EndBattleEffect;
import battle.effect.generic.EffectInterfaces.EndTurnEffect;
import battle.effect.generic.EffectInterfaces.EntryEffect;
import battle.effect.generic.EffectInterfaces.HalfWeightEffect;
import battle.effect.generic.EffectInterfaces.LevitationEffect;
import battle.effect.generic.EffectInterfaces.MurderEffect;
import battle.effect.generic.EffectInterfaces.NameChanger;
import battle.effect.generic.EffectInterfaces.OpponentAccuracyBypassEffect;
import battle.effect.generic.EffectInterfaces.OpponentBeforeTurnEffect;
import battle.effect.generic.EffectInterfaces.OpponentIgnoreStageEffect;
import battle.effect.generic.EffectInterfaces.OpponentPowerChangeEffect;
import battle.effect.generic.EffectInterfaces.OpponentTrappingEffect;
import battle.effect.generic.EffectInterfaces.PhysicalContactEffect;
import battle.effect.generic.EffectInterfaces.PowerChangeEffect;
import battle.effect.generic.EffectInterfaces.PriorityChangeEffect;
import battle.effect.generic.EffectInterfaces.RecoilMove;
import battle.effect.generic.EffectInterfaces.StageChangingEffect;
import battle.effect.generic.EffectInterfaces.StatChangingEffect;
import battle.effect.generic.EffectInterfaces.StatLoweredEffect;
import battle.effect.generic.EffectInterfaces.StatProtectingEffect;
import battle.effect.generic.EffectInterfaces.StatusPreventionEffect;
import battle.effect.generic.EffectInterfaces.TakeDamageEffect;
import battle.effect.generic.EffectInterfaces.TargetSwapperEffect;
import battle.effect.generic.EffectInterfaces.WeatherBlockerEffect;
import battle.effect.generic.PokemonEffect;
import battle.effect.generic.Weather;
import battle.effect.holder.ItemHolder;
import battle.effect.status.Status;
import battle.effect.status.StatusCondition;
import item.Item;
import item.berry.Berry;
import item.hold.ConsumableItem;
import item.hold.HoldItem;
import item.hold.PlateItem;
import main.Global;
import main.Type;
import namesies.AbilityNamesies;
import namesies.AttackNamesies;
import namesies.EffectNamesies;
import namesies.ItemNamesies;
import namesies.PokemonNamesies;
import trainer.Trainer;
import trainer.WildPokemon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class Ability implements Serializable {
	private static final long serialVersionUID = 1L;
	private static HashMap<String, Ability> map; // Mappity map
	
	protected AbilityNamesies namesies;
	private String description;
	
	public Ability(AbilityNamesies s, String desc) {
		namesies = s;
		description = desc;
	}
	
	protected Ability activate() {
		return this;
	}
	
	public AbilityNamesies namesies() {
		return this.namesies;
	}
	
	public String getName() {
		return namesies.getName();
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean isActive() {
		return true;
	}
	
	// Called when this ability is going to changed to a different ability -- can be overidden as necessary
	public void deactivate(Battle b, ActivePokemon victim) {}
	
	// Abilities that block damage
	public static boolean blockAttack(Battle b, ActivePokemon user, ActivePokemon victim) {
		if (user.breaksTheMold()) {
			return false;
		}
		
		Ability a = victim.getAbility();
		if (a instanceof DamageBlocker) {
			DamageBlocker blockityBlock = (DamageBlocker)a;
			if (blockityBlock.block(user.getAttackType(), victim)) {
				blockityBlock.alternateEffect(b, victim);
				return true;
			}
		}
		
		return false;
	}
	
	public static Ability assign(PokemonInfo p) {
		AbilityNamesies[] abilities = p.getAbilities();
		
		if (abilities[0] == AbilityNamesies.NO_ABILITY) {
			Global.error("First ability should not be none (Pokemon " + p.getName() + ")");
		}
		
		// Only has one ability -- return the first one
		if (abilities[1] == AbilityNamesies.NO_ABILITY) {
			return getAbility(abilities[0]).newInstance();
		}
		
		// Has two abilties -- return a random one
		return getAbility(Global.getRandomValue(abilities)).newInstance();
	}
	
	public static Ability evolutionAssign(ActivePokemon p, PokemonInfo ev) {
		AbilityNamesies prev = p.getAbility().namesies();
		if (ev.hasAbility(prev)) {
			return p.getAbility().newInstance();
		}

		AbilityNamesies other = getOtherAbility(p.getPokemonInfo(), prev).namesies();
		if (ev.hasAbility(other)) {
			return getOtherAbility(ev, other);
		}

		AbilityNamesies[] abilities = ev.getAbilities();
		if (abilities[1] == AbilityNamesies.NO_ABILITY) {
			return getAbility(abilities[0]);
		}
		
		return getAbility(Global.getRandomValue(abilities));
	}
	
	public static Ability getOtherAbility(ActivePokemon p) {
		return getOtherAbility(p.getPokemonInfo(), p.getAbility().namesies());
	}
	
	private static Ability getOtherAbility(PokemonInfo p, AbilityNamesies ability) {
		if (!p.hasAbility(ability)) {
			Global.error("Incorrect ability " + ability + " for " + p.getName() + ".");
		}

		AbilityNamesies[] abilities = p.getAbilities();
		return getAbility(abilities[0] == ability ? abilities[1] : abilities[0]); 
	}
	
	public abstract Ability newInstance();
	
	public static Ability getAbility(AbilityNamesies namesies) {
		String abilityName = namesies.getName();
		
		if (map == null) {
			loadAbilities();
		}
		
		if (map.containsKey(abilityName)) {
			return map.get(abilityName);
		}

		Global.error("No such Ability " + abilityName);
		return new NoAbility();
	}
	
	// Create and load the Ability map if it doesn't already exist
	public static void loadAbilities() {
		if (map != null) {
			return;
		}

		map = new HashMap<>();

		// EVERYTHING BELOW IS GENERATED ###

		// List all of the classes we are loading
		map.put("No Ability", new NoAbility());
		map.put("Overgrow", new Overgrow());
		map.put("Chlorophyll", new Chlorophyll());
		map.put("Blaze", new Blaze());
		map.put("Solar Power", new SolarPower());
		map.put("Torrent", new Torrent());
		map.put("Rain Dish", new RainDish());
		map.put("Shield Dust", new ShieldDust());
		map.put("Shed Skin", new ShedSkin());
		map.put("Compoundeyes", new Compoundeyes());
		map.put("Tinted Lens", new TintedLens());
		map.put("Swarm", new Swarm());
		map.put("Sniper", new Sniper());
		map.put("Keen Eye", new KeenEye());
		map.put("Tangled Feet", new TangledFeet());
		map.put("Guts", new Guts());
		map.put("Intimidate", new Intimidate());
		map.put("Static", new Static());
		map.put("Lightningrod", new Lightningrod());
		map.put("Sand Veil", new SandVeil());
		map.put("Sand Rush", new SandRush());
		map.put("Poison Point", new PoisonPoint());
		map.put("Rivalry", new Rivalry());
		map.put("Cute Charm", new CuteCharm());
		map.put("Magic Guard", new MagicGuard());
		map.put("Flash Fire", new FlashFire());
		map.put("Drought", new Drought());
		map.put("Frisk", new Frisk());
		map.put("Inner Focus", new InnerFocus());
		map.put("Infiltrator", new Infiltrator());
		map.put("Stench", new Stench());
		map.put("Effect Spore", new EffectSpore());
		map.put("Dry Skin", new DrySkin());
		map.put("Arena Trap", new ArenaTrap());
		map.put("Technician", new Technician());
		map.put("Limber", new Limber());
		map.put("Damp", new Damp());
		map.put("Cloud Nine", new CloudNine());
		map.put("Vital Spirit", new VitalSpirit());
		map.put("Insomnia", new Insomnia());
		map.put("Anger Point", new AngerPoint());
		map.put("Synchronize", new Synchronize());
		map.put("No Guard", new NoGuard());
		map.put("Own Tempo", new OwnTempo());
		map.put("Clear Body", new ClearBody());
		map.put("Liquid Ooze", new LiquidOoze());
		map.put("Rock Head", new RockHead());
		map.put("Sturdy", new Sturdy());
		map.put("Oblivious", new Oblivious());
		map.put("Magnet Pull", new MagnetPull());
		map.put("Unaware", new Unaware());
		map.put("Simple", new Simple());
		map.put("Early Bird", new EarlyBird());
		map.put("Thick Fat", new ThickFat());
		map.put("Hydration", new Hydration());
		map.put("Shell Armor", new ShellArmor());
		map.put("Battle Armor", new BattleArmor());
		map.put("Skill Link", new SkillLink());
		map.put("Levitate", new Levitate());
		map.put("Forewarn", new Forewarn());
		map.put("Hyper Cutter", new HyperCutter());
		map.put("Soundproof", new Soundproof());
		map.put("Reckless", new Reckless());
		map.put("Iron Fist", new IronFist());
		map.put("Natural Cure", new NaturalCure());
		map.put("Serene Grace", new SereneGrace());
		map.put("Leaf Guard", new LeafGuard());
		map.put("Scrappy", new Scrappy());
		map.put("Swift Swim", new SwiftSwim());
		map.put("Water Veil", new WaterVeil());
		map.put("Filter", new Filter());
		map.put("Flame Body", new FlameBody());
		map.put("Rattled", new Rattled());
		map.put("Moxie", new Moxie());
		map.put("Imposter", new Imposter());
		map.put("Adaptability", new Adaptability());
		map.put("Water Absorb", new WaterAbsorb());
		map.put("Volt Absorb", new VoltAbsorb());
		map.put("Quick Feet", new QuickFeet());
		map.put("Trace", new Trace());
		map.put("Download", new Download());
		map.put("Pressure", new Pressure());
		map.put("Immunity", new Immunity());
		map.put("Snow Cloak", new SnowCloak());
		map.put("Marvel Scale", new MarvelScale());
		map.put("Multiscale", new Multiscale());
		map.put("Sheer Force", new SheerForce());
		map.put("Hustle", new Hustle());
		map.put("Huge Power", new HugePower());
		map.put("Speed Boost", new SpeedBoost());
		map.put("Magic Bounce", new MagicBounce());
		map.put("Super Luck", new SuperLuck());
		map.put("Shadow Tag", new ShadowTag());
		map.put("Overcoat", new Overcoat());
		map.put("Magma Armor", new MagmaArmor());
		map.put("Suction Cups", new SuctionCups());
		map.put("Steadfast", new Steadfast());
		map.put("Sand Stream", new SandStream());
		map.put("Regenerator", new Regenerator());
		map.put("Poison Heal", new PoisonHeal());
		map.put("Truant", new Truant());
		map.put("Wonder Guard", new WonderGuard());
		map.put("Normalize", new Normalize());
		map.put("Stall", new Stall());
		map.put("Pure Power", new PurePower());
		map.put("Rough Skin", new RoughSkin());
		map.put("Solid Rock", new SolidRock());
		map.put("White Smoke", new WhiteSmoke());
		map.put("Toxic Boost", new ToxicBoost());
		map.put("Anticipation", new Anticipation());
		map.put("Storm Drain", new StormDrain());
		map.put("Color Change", new ColorChange());
		map.put("Ice Body", new IceBody());
		map.put("Light Metal", new LightMetal());
		map.put("Drizzle", new Drizzle());
		map.put("Air Lock", new AirLock());
		map.put("Defiant", new Defiant());
		map.put("Competitive", new Competitive());
		map.put("Flower Gift", new FlowerGift());
		map.put("Aftermath", new Aftermath());
		map.put("Heatproof", new Heatproof());
		map.put("Sand Force", new SandForce());
		map.put("Snow Warning", new SnowWarning());
		map.put("Motor Drive", new MotorDrive());
		map.put("Justified", new Justified());
		map.put("Cursed Body", new CursedBody());
		map.put("Slow Start", new SlowStart());
		map.put("Bad Dreams", new BadDreams());
		map.put("Victory Star", new VictoryStar());
		map.put("Contrary", new Contrary());
		map.put("Big Pecks", new BigPecks());
		map.put("Poison Touch", new PoisonTouch());
		map.put("Prankster", new Prankster());
		map.put("Wonder Skin", new WonderSkin());
		map.put("Mummy", new Mummy());
		map.put("Defeatist", new Defeatist());
		map.put("Weak Armor", new WeakArmor());
		map.put("Illusion", new Illusion());
		map.put("Analytic", new Analytic());
		map.put("Sap Sipper", new SapSipper());
		map.put("Iron Barbs", new IronBarbs());
		map.put("Mold Breaker", new MoldBreaker());
		map.put("Teravolt", new Teravolt());
		map.put("Turboblaze", new Turboblaze());
		map.put("Run Away", new RunAway());
		map.put("Sticky Hold", new StickyHold());
		map.put("Klutz", new Klutz());
		map.put("Unburden", new Unburden());
		map.put("Pickpocket", new Pickpocket());
		map.put("Harvest", new Harvest());
		map.put("Pickup", new Pickup());
		map.put("Unnerve", new Unnerve());
		map.put("Honey Gather", new HoneyGather());
		map.put("Gluttony", new Gluttony());
		map.put("Multitype", new Multitype());
		map.put("Forecast", new Forecast());
		map.put("Bulletproof", new Bulletproof());
		map.put("Aura Break", new AuraBreak());
		map.put("Fairy Aura", new FairyAura());
		map.put("Dark Aura", new DarkAura());
		map.put("Magician", new Magician());
		map.put("Cheek Pouch", new CheekPouch());
		map.put("Strong Jaw", new StrongJaw());
		map.put("Mega Launcher", new MegaLauncher());
		map.put("Tough Claws", new ToughClaws());
		map.put("Sweet Veil", new SweetVeil());
		map.put("Aroma Veil", new AromaVeil());
		map.put("Healer", new Healer());
		map.put("Pixilate", new Pixilate());
		map.put("Refrigerate", new Refrigerate());
		map.put("Stance Change", new StanceChange());
		map.put("Fur Coat", new FurCoat());
		map.put("Grass Pelt", new GrassPelt());
		map.put("Flower Veil", new FlowerVeil());
		map.put("Gale Wings", new GaleWings());
		map.put("Protean", new Protean());
	}

	/**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

	private static class NoAbility extends Ability {
		private static final long serialVersionUID = 1L;

		NoAbility() {
			super(AbilityNamesies.NO_ABILITY, "None");
		}

		public NoAbility newInstance() {
			return (NoAbility)(new NoAbility().activate());
		}
	}

	private static class Overgrow extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		Overgrow() {
			super(AbilityNamesies.OVERGROW, "Powers up Grass-type moves in a pinch.");
		}

		public Overgrow newInstance() {
			return (Overgrow)(new Overgrow().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getHPRatio() < 1/3.0 && user.getAttackType() == Type.GRASS ? 1.5 : 1;
		}
	}

	private static class Chlorophyll extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		Chlorophyll() {
			super(AbilityNamesies.CHLOROPHYLL, "Boosts the Pok\u00e9mon's Speed in sunshine.");
		}

		public Chlorophyll newInstance() {
			return (Chlorophyll)(new Chlorophyll().activate());
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.SPEED;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && b.getWeather().namesies() == EffectNamesies.SUNNY) {
				stat *= 2;
			}
			
			return stat;
		}
	}

	private static class Blaze extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		Blaze() {
			super(AbilityNamesies.BLAZE, "Powers up Fire-type moves in a pinch.");
		}

		public Blaze newInstance() {
			return (Blaze)(new Blaze().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getHPRatio() < 1/3.0 && user.getAttackType() == Type.FIRE ? 1.5 : 1;
		}
	}

	private static class SolarPower extends Ability implements PowerChangeEffect, EndTurnEffect {
		private static final long serialVersionUID = 1L;

		SolarPower() {
			super(AbilityNamesies.SOLAR_POWER, "Boosts Sp. Atk, but lowers HP in sunshine.");
		}

		public SolarPower newInstance() {
			return (SolarPower)(new SolarPower().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getAttack().getCategory() == MoveCategory.SPECIAL && b.getWeather().namesies() == EffectNamesies.SUNNY ? 1.5 : 1;
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (b.getWeather().namesies() == EffectNamesies.SUNNY) {
				b.addMessage(victim.getName() + " lost some of its HP due to its " + this.getName() + "!");
				victim.reduceHealthFraction(b, 1/8.0);
			}
		}
	}

	private static class Torrent extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		Torrent() {
			super(AbilityNamesies.TORRENT, "Powers up Water-type moves in a pinch.");
		}

		public Torrent newInstance() {
			return (Torrent)(new Torrent().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getHPRatio() < 1/3.0 && user.getAttackType() == Type.WATER ? 1.5 : 1;
		}
	}

	private static class RainDish extends Ability implements EndTurnEffect {
		private static final long serialVersionUID = 1L;

		RainDish() {
			super(AbilityNamesies.RAIN_DISH, "The Pok\u00e9mon gradually recovers HP in rain.");
		}

		public RainDish newInstance() {
			return (RainDish)(new RainDish().activate());
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (b.getWeather().namesies() == EffectNamesies.RAINING) {
				victim.healHealthFraction(1/16.0);
				b.addMessage(victim.getName() + "'s HP was restored due to its " + this.getName() + "!", victim);
			}
		}
	}

	private static class ShieldDust extends Ability implements EffectBlockerEffect {
		private static final long serialVersionUID = 1L;

		ShieldDust() {
			super(AbilityNamesies.SHIELD_DUST, "Blocks the added effects of attacks taken.");
		}

		public ShieldDust newInstance() {
			return (ShieldDust)(new ShieldDust().activate());
		}

		public boolean validMove(Battle b, ActivePokemon user, ActivePokemon victim) {
			return !user.getAttack().hasSecondaryEffects();
		}
	}

	private static class ShedSkin extends Ability implements EndTurnEffect {
		private static final long serialVersionUID = 1L;

		ShedSkin() {
			super(AbilityNamesies.SHED_SKIN, "The Pok\u00e9mon may heal its own status problems.");
		}

		public ShedSkin newInstance() {
			return (ShedSkin)(new ShedSkin().activate());
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.hasStatus() && Global.chanceTest(1, 3)) {
				Status.removeStatus(b, victim, CastSource.ABILITY);
			}
		}
	}

	private static class Compoundeyes extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		Compoundeyes() {
			super(AbilityNamesies.COMPOUNDEYES, "The Pok\u00e9mon's accuracy is boosted.");
		}

		public Compoundeyes newInstance() {
			return (Compoundeyes)(new Compoundeyes().activate());
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.ACCURACY;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && true) {
				stat *= 1.3;
			}
			
			return stat;
		}
	}

	private static class TintedLens extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		TintedLens() {
			super(AbilityNamesies.TINTED_LENS, "Powers up \"not very effective\" moves.");
		}

		public TintedLens newInstance() {
			return (TintedLens)(new TintedLens().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return Type.getAdvantage(user, victim, b) < 1 ? 2 : 1;
		}
	}

	private static class Swarm extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		Swarm() {
			super(AbilityNamesies.SWARM, "Powers up Bug-type moves in a pinch.");
		}

		public Swarm newInstance() {
			return (Swarm)(new Swarm().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getHPRatio() < 1/3.0 && user.getAttackType() == Type.BUG ? 1.5 : 1;
		}
	}

	private static class Sniper extends Ability {
		private static final long serialVersionUID = 1L;

		Sniper() {
			super(AbilityNamesies.SNIPER, "Powers up moves if they become critical hits.");
		}

		public Sniper newInstance() {
			return (Sniper)(new Sniper().activate());
		}
	}

	private static class KeenEye extends Ability implements StatProtectingEffect, OpponentIgnoreStageEffect {
		private static final long serialVersionUID = 1L;

		KeenEye() {
			super(AbilityNamesies.KEEN_EYE, "Prevents the Pok\u00e9mon from losing accuracy.");
		}

		public KeenEye newInstance() {
			return (KeenEye)(new KeenEye().activate());
		}

		public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
			return stat == Stat.ACCURACY;
		}

		public String preventionMessage(ActivePokemon p, Stat s) {
			return p.getName() + "'s " + this.getName() + " prevents its " + s.getName().toLowerCase() + " from being lowered!";
		}

		public boolean ignoreStage(Stat s) {
			return s == Stat.EVASION;
		}
	}

	private static class TangledFeet extends Ability implements StageChangingEffect {
		private static final long serialVersionUID = 1L;

		TangledFeet() {
			super(AbilityNamesies.TANGLED_FEET, "Raises evasion if the Pok\u00e9mon is confused.");
		}

		public TangledFeet newInstance() {
			return (TangledFeet)(new TangledFeet().activate());
		}

		public int adjustStage(Battle b,  ActivePokemon p, ActivePokemon opp, Stat s, int stage) {
			return s == Stat.EVASION && p.hasEffect(EffectNamesies.CONFUSION) ? stage + 1 : stage;
		}
	}

	private static class Guts extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		Guts() {
			super(AbilityNamesies.GUTS, "Boosts Attack if there is a status problem.");
		}

		public Guts newInstance() {
			return (Guts)(new Guts().activate());
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.ATTACK;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && p.hasStatus()) {
				stat *= 1.5;
			}
			
			return stat;
		}
	}

	private static class Intimidate extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		Intimidate() {
			super(AbilityNamesies.INTIMIDATE, "Lowers the foe's Attack stat.");
		}

		public Intimidate newInstance() {
			return (Intimidate)(new Intimidate().activate());
		}

		public void enter(Battle b, ActivePokemon enterer) {
			ActivePokemon other = b.getOtherPokemon(enterer.user());
			other.getAttributes().modifyStage(enterer, other, -1, Stat.ATTACK, b, CastSource.ABILITY);
		}
	}

	private static class Static extends Ability implements PhysicalContactEffect {
		private static final long serialVersionUID = 1L;

		Static() {
			super(AbilityNamesies.STATIC, "Contact with the Pok\u00e9mon may cause paralysis.");
		}

		public Static newInstance() {
			return (Static)(new Static().activate());
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (Global.chanceTest(30)) {
				Status.giveStatus(b, victim, user, StatusCondition.PARALYZED, true);
			}
		}
	}

	private static class Lightningrod extends Ability implements DamageBlocker {
		private static final long serialVersionUID = 1L;

		Lightningrod() {
			super(AbilityNamesies.LIGHTNINGROD, "The Pok\u00e9mon draws in all Electric-type moves.");
		}

		public Lightningrod newInstance() {
			return (Lightningrod)(new Lightningrod().activate());
		}

		public Stat toIncrease() {
			return Stat.SP_ATTACK;
		}

		public boolean block(Type attacking, ActivePokemon victim) {
			return attacking == Type.ELECTRIC;
		}

		public void alternateEffect(Battle b, ActivePokemon victim) {
			b.addMessage(victim.getName() + "'s " + this.getName() + " makes it immune to " + Type.ELECTRIC.getName() + " type moves!");
			victim.getAttributes().modifyStage(victim, victim, 1, toIncrease(), b, CastSource.ABILITY);
		}
	}

	private static class SandVeil extends Ability implements StageChangingEffect {
		private static final long serialVersionUID = 1L;

		SandVeil() {
			super(AbilityNamesies.SAND_VEIL, "Raises the Pok\u00e9mon's evasion during a sandstorm by one level.");
		}

		public SandVeil newInstance() {
			return (SandVeil)(new SandVeil().activate());
		}

		public int adjustStage(Battle b,  ActivePokemon p, ActivePokemon opp, Stat s, int stage) {
			return s == Stat.EVASION && b.getWeather().namesies() == EffectNamesies.SANDSTORM ? stage + 1 : stage;
		}
	}

	private static class SandRush extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		SandRush() {
			super(AbilityNamesies.SAND_RUSH, "Speed rises in a Sandstorm.");
		}

		public SandRush newInstance() {
			return (SandRush)(new SandRush().activate());
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.SPEED;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && b.getWeather().namesies() == EffectNamesies.SANDSTORM) {
				stat *= 2;
			}
			
			return stat;
		}
	}

	private static class PoisonPoint extends Ability implements PhysicalContactEffect {
		private static final long serialVersionUID = 1L;

		PoisonPoint() {
			super(AbilityNamesies.POISON_POINT, "Contact with the Pok\u00e9mon may poison the foe.");
		}

		public PoisonPoint newInstance() {
			return (PoisonPoint)(new PoisonPoint().activate());
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (Global.chanceTest(30)) {
				Status.giveStatus(b, victim, user, StatusCondition.POISONED, true);
			}
		}
	}

	private static class Rivalry extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		Rivalry() {
			super(AbilityNamesies.RIVALRY, "Raises Attack if the foe is of the same gender.");
		}

		public Rivalry newInstance() {
			return (Rivalry)(new Rivalry().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getGender() == Gender.GENDERLESS) return 1;
			if (Gender.oppositeGenders(user, victim)) return .75;
			if (user.getGender() == victim.getGender()) return 1.25;
			return 1;
		}
	}

	private static class CuteCharm extends Ability implements PhysicalContactEffect {
		private static final long serialVersionUID = 1L;

		CuteCharm() {
			super(AbilityNamesies.CUTE_CHARM, "Contact with the Pok\u00e9mon may cause infatuation.");
		}

		public CuteCharm newInstance() {
			return (CuteCharm)(new CuteCharm().activate());
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (Global.chanceTest(30)) {
				PokemonEffect e = PokemonEffect.getEffect(EffectNamesies.INFATUATED);
				if (e.applies(b, victim, user, CastSource.ABILITY)) {
					user.addEffect(e.newInstance());
					b.addMessage(victim.getName() + "'s " + this.getName() + " infatuated " + user.getName() + "!");
				}
			}
		}
	}

	private static class MagicGuard extends Ability implements WeatherBlockerEffect {
		private static final long serialVersionUID = 1L;

		MagicGuard() {
			super(AbilityNamesies.MAGIC_GUARD, "The Pok\u00e9mon only takes damage from attacks.");
		}

		public MagicGuard newInstance() {
			return (MagicGuard)(new MagicGuard().activate());
		}

		public boolean block(EffectNamesies weather) {
			return true;
		}
	}

	private static class FlashFire extends Ability implements DamageBlocker, PowerChangeEffect {
		private static final long serialVersionUID = 1L;
		private boolean activated;

		FlashFire() {
			super(AbilityNamesies.FLASH_FIRE, "Powers up Fire-type moves if hit by a fire move.");
		}

		public FlashFire newInstance() {
			FlashFire x = (FlashFire)(new FlashFire().activate());
			x.activated = false;
			return x;
		}

		public boolean isActive() {
			return activated;
		}

		public boolean block(Type attacking, ActivePokemon victim) {
			return attacking == Type.FIRE;
		}

		public void alternateEffect(Battle b, ActivePokemon victim) {
			b.addMessage(victim.getName() + "'s " + this.getName() + " makes it immune to Fire type moves!");
			activated = true;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return activated && user.getAttackType() == Type.FIRE ? 1.5 : 1;
		}
	}

	private static class Drought extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		Drought() {
			super(AbilityNamesies.DROUGHT, "The Pok\u00e9mon makes it sunny if it is in battle.");
		}

		public Drought newInstance() {
			return (Drought)(new Drought().activate());
		}

		public void enter(Battle b, ActivePokemon enterer) {
			b.addEffect(Weather.getEffect(EffectNamesies.SUNNY).newInstance());
			b.addMessage(enterer.getName() + "'s " + this.getName() + " made the sunlight turn harsh!");
		}
	}

	private static class Frisk extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		Frisk() {
			super(AbilityNamesies.FRISK, "The Pok\u00e9mon can check the foe's held item.");
		}

		public Frisk newInstance() {
			return (Frisk)(new Frisk().activate());
		}

		public void enter(Battle b, ActivePokemon enterer) {
			ActivePokemon other = b.getOtherPokemon(enterer.user());
			if (other.isHoldingItem(b)) b.addMessage(enterer.getName() + "'s " + this.getName() + " alerted it to " + other.getName() + "'s " + other.getHeldItem(b).getName() + "!");
		}
	}

	private static class InnerFocus extends Ability {
		private static final long serialVersionUID = 1L;

		InnerFocus() {
			super(AbilityNamesies.INNER_FOCUS, "The Pok\u00e9mon is protected from flinching.");
		}

		public InnerFocus newInstance() {
			return (InnerFocus)(new InnerFocus().activate());
		}
	}

	private static class Infiltrator extends Ability {
		private static final long serialVersionUID = 1L;

		Infiltrator() {
			super(AbilityNamesies.INFILTRATOR, "You slip through the opponents walls and attack.");
		}

		public Infiltrator newInstance() {
			return (Infiltrator)(new Infiltrator().activate());
		}
	}

	private static class Stench extends Ability implements ApplyDamageEffect {
		private static final long serialVersionUID = 1L;

		Stench() {
			super(AbilityNamesies.STENCH, "The stench may cause the target to flinch.");
		}

		public Stench newInstance() {
			return (Stench)(new Stench().activate());
		}

		public void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
			if (Global.chanceTest(10)) {
				PokemonEffect flinch = PokemonEffect.getEffect(EffectNamesies.FLINCH);
				if (flinch.applies(b, user, victim, CastSource.ABILITY)) {
					flinch.cast(b, user, victim, CastSource.ABILITY, false);
					b.addMessage(user.getName() + "'s " + this.getName() + " caused " + victim.getName() + " to flinch!");
				}
			}
		}
	}

	private static class EffectSpore extends Ability implements PhysicalContactEffect {
		private static final long serialVersionUID = 1L;
		private static StatusCondition[] statuses = new StatusCondition[] {
			StatusCondition.PARALYZED,
			StatusCondition.POISONED,
			StatusCondition.ASLEEP
		};

		EffectSpore() {
			super(AbilityNamesies.EFFECT_SPORE, "Contact may paralyze, poison, or cause sleep.");
		}

		public EffectSpore newInstance() {
			return (EffectSpore)(new EffectSpore().activate());
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
			// Grass-type Pokemon, Pokemon with Overcoat, and Pokemon holding the Safety Goggles are immune to Effect Spore
			if (user.isType(b, Type.GRASS) || user.hasAbility(AbilityNamesies.OVERCOAT) || user.isHoldingItem(b, ItemNamesies.SAFETY_GOGGLES)) {
				return;
			}
			
			// 30% chance to Paralyze, Poison, or induce Sleep
			if (Global.chanceTest(30)) {
				Status.giveStatus(b, victim, user, Global.getRandomValue(statuses), true);
			}
		}
	}

	private static class DrySkin extends Ability implements EndTurnEffect, OpponentPowerChangeEffect, DamageBlocker {
		private static final long serialVersionUID = 1L;

		DrySkin() {
			super(AbilityNamesies.DRY_SKIN, "Reduces HP if it is hot. Water restores HP.");
		}

		public DrySkin newInstance() {
			return (DrySkin)(new DrySkin().activate());
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (b.getWeather().namesies() == EffectNamesies.SUNNY) {
				b.addMessage(victim.getName() + " lost some of its HP due to its " + this.getName() + "!");
				victim.reduceHealthFraction(b, 1/8.0);
			}
			else if (b.getWeather().namesies() == EffectNamesies.RAINING && !victim.fullHealth()) {
				victim.healHealthFraction(1/8.0);
				b.addMessage(victim.getName() + "'s HP was restored due to its " + this.getName() + "!", victim);
			}
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getAttackType() == Type.FIRE ? 1.25 : 1;
		}

		public boolean block(Type attacking, ActivePokemon victim) {
			return attacking == Type.WATER;
		}

		public void alternateEffect(Battle b, ActivePokemon victim) {
			b.addMessage(victim.getName() + "'s " + this.getName() + " makes it immune to " + Type.WATER.getName() + " moves!");
			
			// Technically, according to the description, Heal Block prevents the prevention entirely (meaning this should be in Block), but that makes no sense, they shouldn't take damage, this way makes more sense
			if (victim.fullHealth() || victim.hasEffect(EffectNamesies.HEAL_BLOCK)) {
				return;
			}
			
			victim.healHealthFraction(1/4.0);
			b.addMessage(victim.getName() + "'s HP was restored instead!", victim);
		}
	}

	private static class ArenaTrap extends Ability implements OpponentTrappingEffect {
		private static final long serialVersionUID = 1L;

		ArenaTrap() {
			super(AbilityNamesies.ARENA_TRAP, "Prevents the foe from fleeing.");
		}

		public ArenaTrap newInstance() {
			return (ArenaTrap)(new ArenaTrap().activate());
		}

		public boolean trapOpponent(Battle b, ActivePokemon escaper, ActivePokemon trapper) {
			return !escaper.isLevitating(b) && !escaper.isType(b, Type.GHOST);
		}

		public String opponentTrappingMessage(ActivePokemon escaper, ActivePokemon trapper) {
			return trapper.getName() + "'s " + this.getName() + " prevents " + escaper.getName() + " from escaping!";
		}
	}

	private static class Technician extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		Technician() {
			super(AbilityNamesies.TECHNICIAN, "Powers up the Pok\u00e9mon's weaker moves.");
		}

		public Technician newInstance() {
			return (Technician)(new Technician().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getAttackPower() <= 60 ? 1.5 : 1;
		}
	}

	private static class Limber extends Ability implements StatusPreventionEffect {
		private static final long serialVersionUID = 1L;

		Limber() {
			super(AbilityNamesies.LIMBER, "The Pok\u00e9mon is protected from paralysis.");
		}

		public Limber newInstance() {
			return (Limber)(new Limber().activate());
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
			return status == StatusCondition.PARALYZED;
		}

		public String statusPreventionMessage(ActivePokemon victim) {
			return victim.getName() + "'s " + this.getName() + " prevents paralysis!";
		}
	}

	private static class Damp extends Ability implements BeforeTurnEffect, OpponentBeforeTurnEffect {
		private static final long serialVersionUID = 1L;
		private boolean checkeroo(Battle b, ActivePokemon attacking, ActivePokemon abilify) {
			if (attacking.getAttack().namesies() == AttackNamesies.SELF_DESTRUCT || attacking.getAttack().namesies() == AttackNamesies.EXPLOSION) {
				b.printAttacking(attacking);
				b.addMessage(abilify.getName() + "'s " + this.getName() + " prevents " + attacking.getAttack().getName() + " from being used!");
				return false;
			}
			
			return true;
		}

		Damp() {
			super(AbilityNamesies.DAMP, "Prevents combatants from self destructing.");
		}

		public Damp newInstance() {
			return (Damp)(new Damp().activate());
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			return checkeroo(b, p, p);
		}

		public boolean opposingCanAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			return checkeroo(b, p, opp);
		}
	}

	private static class CloudNine extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		CloudNine() {
			super(AbilityNamesies.CLOUD_NINE, "Eliminates the effects of weather.");
		}

		public CloudNine newInstance() {
			return (CloudNine)(new CloudNine().activate());
		}

		public void enter(Battle b, ActivePokemon enterer) {
			// TODO: I think this isn't the intended effect of this ability
			b.addEffect(Weather.getEffect(EffectNamesies.CLEAR_SKIES));
			b.addMessage(enterer.getName() + "'s " + this.getName() + " eliminated the weather!");
		}
	}

	private static class VitalSpirit extends Ability implements StatusPreventionEffect {
		private static final long serialVersionUID = 1L;

		VitalSpirit() {
			super(AbilityNamesies.VITAL_SPIRIT, "Prevents the Pok\u00e9mon from falling asleep.");
		}

		public VitalSpirit newInstance() {
			return (VitalSpirit)(new VitalSpirit().activate());
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
			return status == StatusCondition.ASLEEP;
		}

		public String statusPreventionMessage(ActivePokemon victim) {
			return victim.getName() + "'s " + this.getName() + " prevents sleep!";
		}
	}

	private static class Insomnia extends Ability implements StatusPreventionEffect {
		private static final long serialVersionUID = 1L;

		Insomnia() {
			super(AbilityNamesies.INSOMNIA, "Prevents the Pok\u00e9mon from falling asleep.");
		}

		public Insomnia newInstance() {
			return (Insomnia)(new Insomnia().activate());
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
			return status == StatusCondition.ASLEEP;
		}

		public String statusPreventionMessage(ActivePokemon victim) {
			return victim.getName() + "'s " + this.getName() + " prevents sleep!";
		}
	}

	private static class AngerPoint extends Ability {
		private static final long serialVersionUID = 1L;

		AngerPoint() {
			super(AbilityNamesies.ANGER_POINT, "Raises Attack upon taking a critical hit.");
		}

		public AngerPoint newInstance() {
			return (AngerPoint)(new AngerPoint().activate());
		}
	}

	private static class Synchronize extends Ability {
		private static final long serialVersionUID = 1L;

		Synchronize() {
			super(AbilityNamesies.SYNCHRONIZE, "Passes on a burn, poison, or paralysis to the foe.");
		}

		public Synchronize newInstance() {
			return (Synchronize)(new Synchronize().activate());
		}
	}

	private static class NoGuard extends Ability implements AccuracyBypassEffect, OpponentAccuracyBypassEffect {
		private static final long serialVersionUID = 1L;

		NoGuard() {
			super(AbilityNamesies.NO_GUARD, "Ensures the Pok\u00e9mon and its foe's attacks land.");
		}

		public NoGuard newInstance() {
			return (NoGuard)(new NoGuard().activate());
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
			// Moves always hit unless they are OHKO moves
			return !attacking.getAttack().isMoveType(MoveType.ONE_HIT_KO);
		}

		public boolean opponentBypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
			// Moves always hit unless they are OHKO moves
			return !attacking.getAttack().isMoveType(MoveType.ONE_HIT_KO);
		}
	}

	private static class OwnTempo extends Ability {
		private static final long serialVersionUID = 1L;

		OwnTempo() {
			super(AbilityNamesies.OWN_TEMPO, "Prevents the Pok\u00e9mon from becoming confused.");
		}

		public OwnTempo newInstance() {
			return (OwnTempo)(new OwnTempo().activate());
		}
	}

	private static class ClearBody extends Ability implements StatProtectingEffect {
		private static final long serialVersionUID = 1L;

		ClearBody() {
			super(AbilityNamesies.CLEAR_BODY, "Prevents the Pok\u00e9mon's stats from being lowered.");
		}

		public ClearBody newInstance() {
			return (ClearBody)(new ClearBody().activate());
		}

		public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
			return true;
		}

		public String preventionMessage(ActivePokemon p, Stat s) {
			return p.getName() + "'s " + this.getName() + " prevents its " + s.getName().toLowerCase() + " from being lowered!";
		}
	}

	private static class LiquidOoze extends Ability {
		private static final long serialVersionUID = 1L;

		LiquidOoze() {
			super(AbilityNamesies.LIQUID_OOZE, "Inflicts damage on foes using any draining move.");
		}

		public LiquidOoze newInstance() {
			return (LiquidOoze)(new LiquidOoze().activate());
		}
	}

	private static class RockHead extends Ability {
		private static final long serialVersionUID = 1L;

		RockHead() {
			super(AbilityNamesies.ROCK_HEAD, "Protects the Pok\u00e9mon from recoil damage.");
		}

		public RockHead newInstance() {
			return (RockHead)(new RockHead().activate());
		}
	}

	private static class Sturdy extends Ability implements BracingEffect {
		private static final long serialVersionUID = 1L;

		Sturdy() {
			super(AbilityNamesies.STURDY, "The Pok\u00e9mon is protected against 1-hit KO attacks.");
		}

		public Sturdy newInstance() {
			return (Sturdy)(new Sturdy().activate());
		}

		public boolean isBracing(Battle b, ActivePokemon bracer, boolean fullHealth) {
			return fullHealth;
		}

		public String braceMessage(ActivePokemon bracer) {
			return bracer.getName() + "'s " + this.getName() + " endured the hit!";
		}
	}

	private static class Oblivious extends Ability {
		private static final long serialVersionUID = 1L;

		Oblivious() {
			super(AbilityNamesies.OBLIVIOUS, "Prevents the Pok\u00e9mon from becoming infatuated.");
		}

		public Oblivious newInstance() {
			return (Oblivious)(new Oblivious().activate());
		}
	}

	private static class MagnetPull extends Ability implements OpponentTrappingEffect {
		private static final long serialVersionUID = 1L;

		MagnetPull() {
			super(AbilityNamesies.MAGNET_PULL, "Prevents Steel-type Pok\u00e9mon from escaping.");
		}

		public MagnetPull newInstance() {
			return (MagnetPull)(new MagnetPull().activate());
		}

		public boolean trapOpponent(Battle b, ActivePokemon escaper, ActivePokemon trapper) {
			return escaper.isType(b, Type.STEEL) && !escaper.isType(b, Type.GHOST);
		}

		public String opponentTrappingMessage(ActivePokemon escaper, ActivePokemon trapper) {
			return trapper.getName() + "'s " + this.getName() + " prevents " + escaper.getName() + " from escaping!";
		}
	}

	private static class Unaware extends Ability implements OpponentIgnoreStageEffect {
		private static final long serialVersionUID = 1L;

		Unaware() {
			super(AbilityNamesies.UNAWARE, "Ignores any change in ability by the foe.");
		}

		public Unaware newInstance() {
			return (Unaware)(new Unaware().activate());
		}

		public boolean ignoreStage(Stat s) {
			return s != Stat.SPEED;
		}
	}

	private static class Simple extends Ability implements ModifyStageValueEffect {
		private static final long serialVersionUID = 1L;

		Simple() {
			super(AbilityNamesies.SIMPLE, "The Pok\u00e9mon is prone to wild stat changes.");
		}

		public Simple newInstance() {
			return (Simple)(new Simple().activate());
		}

		public int modifyStageValue(int modVal) {
			return modVal*2;
		}
	}

	private static class EarlyBird extends Ability {
		private static final long serialVersionUID = 1L;

		EarlyBird() {
			super(AbilityNamesies.EARLY_BIRD, "The Pok\u00e9mon awakens quickly from sleep.");
		}

		public EarlyBird newInstance() {
			return (EarlyBird)(new EarlyBird().activate());
		}
	}

	private static class ThickFat extends Ability implements OpponentPowerChangeEffect {
		private static final long serialVersionUID = 1L;

		ThickFat() {
			super(AbilityNamesies.THICK_FAT, "Raises resistance to Fire-and Ice-type moves.");
		}

		public ThickFat newInstance() {
			return (ThickFat)(new ThickFat().activate());
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getAttackType() == Type.FIRE || user.getAttackType() == Type.ICE ? .5 : 1;
		}
	}

	private static class Hydration extends Ability implements EndTurnEffect {
		private static final long serialVersionUID = 1L;

		Hydration() {
			super(AbilityNamesies.HYDRATION, "Heals status problems if it is raining.");
		}

		public Hydration newInstance() {
			return (Hydration)(new Hydration().activate());
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.hasStatus() && b.getWeather().namesies() == EffectNamesies.RAINING) {
				Status.removeStatus(b, victim, CastSource.ABILITY);
			}
		}
	}

	private static class ShellArmor extends Ability implements CritBlockerEffect {
		private static final long serialVersionUID = 1L;

		ShellArmor() {
			super(AbilityNamesies.SHELL_ARMOR, "The Pok\u00e9mon is protected against critical hits.");
		}

		public ShellArmor newInstance() {
			return (ShellArmor)(new ShellArmor().activate());
		}

		public boolean blockCrits() {
			return true;
		}
	}

	private static class BattleArmor extends Ability implements CritBlockerEffect {
		private static final long serialVersionUID = 1L;

		BattleArmor() {
			super(AbilityNamesies.BATTLE_ARMOR, "The Pok\u00e9mon is protected against critical hits.");
		}

		public BattleArmor newInstance() {
			return (BattleArmor)(new BattleArmor().activate());
		}

		public boolean blockCrits() {
			return true;
		}
	}

	private static class SkillLink extends Ability {
		private static final long serialVersionUID = 1L;

		SkillLink() {
			super(AbilityNamesies.SKILL_LINK, "Increases the frequency of multi-strike moves.");
		}

		public SkillLink newInstance() {
			return (SkillLink)(new SkillLink().activate());
		}
	}

	private static class Levitate extends Ability implements LevitationEffect {
		private static final long serialVersionUID = 1L;

		Levitate() {
			super(AbilityNamesies.LEVITATE, "Gives full immunity to all Ground-type moves.");
		}

		public Levitate newInstance() {
			return (Levitate)(new Levitate().activate());
		}

		public void fall(Battle b, ActivePokemon fallen) {
			b.addMessage(fallen.getName() + " is no longer levitating!");
			
			// TODO: Fix this it's broken
			// Effect.removeEffect(fallen.getEffects(), this.namesies());
		}
	}

	private static class Forewarn extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		Forewarn() {
			super(AbilityNamesies.FOREWARN, "Determines what moves the foe has.");
		}

		public Forewarn newInstance() {
			return (Forewarn)(new Forewarn().activate());
		}

		public void enter(Battle b, ActivePokemon enterer) {
			ActivePokemon other = b.getOtherPokemon(enterer.user());
			List<Move> otherMoves = other.getMoves(b);
			
			List<AttackNamesies> besties = new ArrayList<>();
			int highestPower = -1;
			
			for (Move move : otherMoves) {
				if (move.getAttack().getCategory() == MoveCategory.STATUS) {
					continue;
				}
				
				int power = move.getAttack().getPower();
				if (power > highestPower) {
					highestPower = power;
					besties = new ArrayList<>();
					besties.add(move.getAttack().namesies());
				}
				else if (power == highestPower) {
					besties.add(move.getAttack().namesies());
				}
			}
			
			AttackNamesies warn;
			if (highestPower == -1) {
				warn = Global.getRandomValue(otherMoves).getAttack().namesies();
			}
			else {
				warn = Global.getRandomValue(besties);
			}
			
			b.addMessage(enterer.getName() + "'s " + this.getName() + " alerted it to " + other.getName() + "'s " + warn.getName() + "!");
		}
	}

	private static class HyperCutter extends Ability implements StatProtectingEffect {
		private static final long serialVersionUID = 1L;

		HyperCutter() {
			super(AbilityNamesies.HYPER_CUTTER, "Prevents the Attack stat from being lowered.");
		}

		public HyperCutter newInstance() {
			return (HyperCutter)(new HyperCutter().activate());
		}

		public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
			return stat == Stat.ATTACK;
		}

		public String preventionMessage(ActivePokemon p, Stat s) {
			return p.getName() + "'s " + this.getName() + " prevents its " + s.getName().toLowerCase() + " from being lowered!";
		}
	}

	private static class Soundproof extends Ability implements OpponentBeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		Soundproof() {
			super(AbilityNamesies.SOUNDPROOF, "Gives full immunity to all sound-based moves.");
		}

		public Soundproof newInstance() {
			return (Soundproof)(new Soundproof().activate());
		}

		public boolean opposingCanAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			if (p.getAttack().isMoveType(MoveType.SOUND_BASED)) {
				b.printAttacking(p);
				b.addMessage(opp.getName() + "'s " + this.getName() + " prevents " + p.getAttack().getName() + " from being used!");
				return false;
			}
			
			return true;
		}
	}

	private static class Reckless extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		Reckless() {
			super(AbilityNamesies.RECKLESS, "Powers up moves that have recoil damage.");
		}

		public Reckless newInstance() {
			return (Reckless)(new Reckless().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getAttack() instanceof RecoilMove || user.getAttack() instanceof CrashDamageMove ? 1.2 : 1;
		}
	}

	private static class IronFist extends Ability implements PowerChangeEffect, EntryEffect {
		private static final long serialVersionUID = 1L;

		IronFist() {
			super(AbilityNamesies.IRON_FIST, "Boosts the power of punching moves.");
		}

		public IronFist newInstance() {
			return (IronFist)(new IronFist().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getAttack().isMoveType(MoveType.PUNCHING) ? 1.2 : 1;
		}

		public void enter(Battle b, ActivePokemon enterer) {
			if (enterer.getPokemonInfo().namesies() == PokemonNamesies.PANGORO) {
				b.addMessage(enterer.getName() + " does not break the mold!!!!!!!");
			}
		}
	}

	private static class NaturalCure extends Ability implements SwitchOutEffect {
		private static final long serialVersionUID = 1L;

		NaturalCure() {
			super(AbilityNamesies.NATURAL_CURE, "All status problems are healed upon switching out.");
		}

		public NaturalCure newInstance() {
			return (NaturalCure)(new NaturalCure().activate());
		}

		public void switchOut(ActivePokemon switchee) {
			if (!switchee.hasStatus(StatusCondition.FAINTED)) {
				switchee.removeStatus();
			}
		}
	}

	private static class SereneGrace extends Ability {
		private static final long serialVersionUID = 1L;

		SereneGrace() {
			super(AbilityNamesies.SERENE_GRACE, "Boosts the likelihood of added effects appearing.");
		}

		public SereneGrace newInstance() {
			return (SereneGrace)(new SereneGrace().activate());
		}
	}

	private static class LeafGuard extends Ability implements StatusPreventionEffect {
		private static final long serialVersionUID = 1L;

		LeafGuard() {
			super(AbilityNamesies.LEAF_GUARD, "Prevents status problems in sunny weather.");
		}

		public LeafGuard newInstance() {
			return (LeafGuard)(new LeafGuard().activate());
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
			return b.getWeather().namesies() == EffectNamesies.SUNNY;
		}

		public String statusPreventionMessage(ActivePokemon victim) {
			return victim.getName() + "'s " + this.getName() + " prevents status conditions!";
		}
	}

	private static class Scrappy extends Ability implements AdvantageChanger {
		private static final long serialVersionUID = 1L;

		Scrappy() {
			super(AbilityNamesies.SCRAPPY, "Enables moves to hit Ghost-type foes.");
		}

		public Scrappy newInstance() {
			return (Scrappy)(new Scrappy().activate());
		}

		public Type[] getAdvantageChange(Type attacking, Type[] defending) {
			for (int i = 0; i < 2; i++) {
				if ((attacking == Type.NORMAL || attacking == Type.FIGHTING) && defending[i] == Type.GHOST) {
					defending[i] = Type.NO_TYPE;
				}
			}
			
			return defending;
		}
	}

	private static class SwiftSwim extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		SwiftSwim() {
			super(AbilityNamesies.SWIFT_SWIM, "Boosts the Pok\u00e9mon's Speed in rain.");
		}

		public SwiftSwim newInstance() {
			return (SwiftSwim)(new SwiftSwim().activate());
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.SPEED;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && b.getWeather().namesies() == EffectNamesies.RAINING) {
				stat *= 2;
			}
			
			return stat;
		}
	}

	private static class WaterVeil extends Ability implements StatusPreventionEffect {
		private static final long serialVersionUID = 1L;

		WaterVeil() {
			super(AbilityNamesies.WATER_VEIL, "Prevents the Pok\u00e9mon from getting a burn.");
		}

		public WaterVeil newInstance() {
			return (WaterVeil)(new WaterVeil().activate());
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
			return status == StatusCondition.BURNED;
		}

		public String statusPreventionMessage(ActivePokemon victim) {
			return victim.getName() + "'s " + this.getName() + " prevents burns!";
		}
	}

	private static class Filter extends Ability implements OpponentPowerChangeEffect {
		private static final long serialVersionUID = 1L;

		Filter() {
			super(AbilityNamesies.FILTER, "Powers down super-effective moves.");
		}

		public Filter newInstance() {
			return (Filter)(new Filter().activate());
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return Type.getAdvantage(user, victim, b) > 1 ? .75 : 1;
		}
	}

	private static class FlameBody extends Ability implements PhysicalContactEffect {
		private static final long serialVersionUID = 1L;

		FlameBody() {
			super(AbilityNamesies.FLAME_BODY, "Contact with the Pok\u00e9mon may burn the foe.");
		}

		public FlameBody newInstance() {
			return (FlameBody)(new FlameBody().activate());
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (Global.chanceTest(30)) {
				Status.giveStatus(b, victim, user, StatusCondition.BURNED, true);
			}
		}
	}

	private static class Rattled extends Ability implements TakeDamageEffect {
		private static final long serialVersionUID = 1L;

		Rattled() {
			super(AbilityNamesies.RATTLED, "Some move types scare it and boost its Speed.");
		}

		public Rattled newInstance() {
			return (Rattled)(new Rattled().activate());
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
			Type type = user.getAttackType();
			if (type == Type.BUG || type == Type.DARK || type == Type.GHOST) {
				victim.getAttributes().modifyStage(victim, victim, 1, Stat.SPEED, b, CastSource.ABILITY);
			}
		}
	}

	private static class Moxie extends Ability implements MurderEffect {
		private static final long serialVersionUID = 1L;

		Moxie() {
			super(AbilityNamesies.MOXIE, "Attack rises when you knock out an opponent.");
		}

		public Moxie newInstance() {
			return (Moxie)(new Moxie().activate());
		}

		public void killWish(Battle b, ActivePokemon dead, ActivePokemon murderer) {
			murderer.getAttributes().modifyStage(murderer, murderer, 1, Stat.ATTACK, b, CastSource.ABILITY);
		}
	}

	private static class Imposter extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		Imposter() {
			super(AbilityNamesies.IMPOSTER, "It transforms itself into the Pok\u00e9mon it is facing.");
		}

		public Imposter newInstance() {
			return (Imposter)(new Imposter().activate());
		}

		public void enter(Battle b, ActivePokemon enterer) {
			PokemonEffect.getEffect(EffectNamesies.TRANSFORMED).cast(b, enterer, enterer, CastSource.ABILITY, false);
		}
	}

	private static class Adaptability extends Ability {
		private static final long serialVersionUID = 1L;

		Adaptability() {
			super(AbilityNamesies.ADAPTABILITY, "Powers up moves of the same type.");
		}

		public Adaptability newInstance() {
			return (Adaptability)(new Adaptability().activate());
		}
	}

	private static class WaterAbsorb extends Ability implements DamageBlocker {
		private static final long serialVersionUID = 1L;

		WaterAbsorb() {
			super(AbilityNamesies.WATER_ABSORB, "Restores HP if hit by a Water-type move.");
		}

		public WaterAbsorb newInstance() {
			return (WaterAbsorb)(new WaterAbsorb().activate());
		}

		public boolean block(Type attacking, ActivePokemon victim) {
			return attacking == Type.WATER;
		}

		public void alternateEffect(Battle b, ActivePokemon victim) {
			b.addMessage(victim.getName() + "'s " + this.getName() + " makes it immune to " + Type.WATER.getName() + " moves!");
			
			// Technically, according to the description, Heal Block prevents the prevention entirely (meaning this should be in Block), but that makes no sense, they shouldn't take damage, this way makes more sense
			if (victim.fullHealth() || victim.hasEffect(EffectNamesies.HEAL_BLOCK)) {
				return;
			}
			
			victim.healHealthFraction(1/4.0);
			b.addMessage(victim.getName() + "'s HP was restored instead!", victim);
		}
	}

	private static class VoltAbsorb extends Ability implements DamageBlocker {
		private static final long serialVersionUID = 1L;

		VoltAbsorb() {
			super(AbilityNamesies.VOLT_ABSORB, "Restores HP if hit by an Electric-type move.");
		}

		public VoltAbsorb newInstance() {
			return (VoltAbsorb)(new VoltAbsorb().activate());
		}

		public boolean block(Type attacking, ActivePokemon victim) {
			return attacking == Type.ELECTRIC;
		}

		public void alternateEffect(Battle b, ActivePokemon victim) {
			b.addMessage(victim.getName() + "'s " + this.getName() + " makes it immune to " + Type.ELECTRIC.getName() + " moves!");
			
			// Technically, according to the description, Heal Block prevents the prevention entirely (meaning this should be in Block), but that makes no sense, they shouldn't take damage, this way makes more sense
			if (victim.fullHealth() || victim.hasEffect(EffectNamesies.HEAL_BLOCK)) {
				return;
			}
			
			victim.healHealthFraction(1/4.0);
			b.addMessage(victim.getName() + "'s HP was restored instead!", victim);
		}
	}

	private static class QuickFeet extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		QuickFeet() {
			super(AbilityNamesies.QUICK_FEET, "Boosts Speed if there is a status problem.");
		}

		public QuickFeet newInstance() {
			return (QuickFeet)(new QuickFeet().activate());
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.SPEED;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && p.hasStatus()) {
				stat *= 1.5;
			}
			
			return stat;
		}
	}

	private static class Trace extends Ability implements EntryEffect, ChangeAbilityMove {
		private static final long serialVersionUID = 1L;

		Trace() {
			super(AbilityNamesies.TRACE, "The Pok\u00e9mon copies the foe's ability.");
		}

		public Trace newInstance() {
			return (Trace)(new Trace().activate());
		}

		public void enter(Battle b, ActivePokemon enterer) {
			ActivePokemon other = b.getOtherPokemon(enterer.user());
			if (other.hasAbility(AbilityNamesies.MULTITYPE) || other.hasAbility(AbilityNamesies.ILLUSION) || other.hasAbility(AbilityNamesies.STANCE_CHANGE) || other.hasAbility(AbilityNamesies.IMPOSTER) || other.hasAbility(this.namesies)) {
				return;
			}
			
			PokemonEffect.getEffect(EffectNamesies.CHANGE_ABILITY).cast(b, enterer, enterer, CastSource.ABILITY, true);
		}

		public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim) {
			return b.getOtherPokemon(victim.user()).getAbility().newInstance();
		}

		public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim) {
			ActivePokemon other = b.getOtherPokemon(victim.user());
			return victim.getName() + " traced " + other.getName() + "'s " + other.getAbility().getName() + "!";
		}
	}

	private static class Download extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		Download() {
			super(AbilityNamesies.DOWNLOAD, "Adjusts power according to a foe's defenses.");
		}

		public Download newInstance() {
			return (Download)(new Download().activate());
		}

		public void enter(Battle b, ActivePokemon enterer) {
			ActivePokemon other = b.getOtherPokemon(enterer.user());
			PokemonInfo otherInfo = PokemonInfo.getPokemonInfo(other.getPokemonInfo().namesies());
			
			int baseDefense = otherInfo.getStat(Stat.DEFENSE.index());
			int baseSpecialDefense = otherInfo.getStat(Stat.SP_DEFENSE.index());
			
			Stat toRaise = baseDefense < baseSpecialDefense ? Stat.ATTACK : Stat.SP_ATTACK;
			
			enterer.getAttributes().modifyStage(enterer, enterer, 1, toRaise, b, CastSource.ABILITY);
		}
	}

	private static class Pressure extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		Pressure() {
			super(AbilityNamesies.PRESSURE, "The Pok\u00e9mon raises the foe's PP usage.");
		}

		public Pressure newInstance() {
			return (Pressure)(new Pressure().activate());
		}

		public void enter(Battle b, ActivePokemon enterer) {
			b.addMessage(enterer.getName() + " is exerting pressure!");
		}
	}

	private static class Immunity extends Ability implements StatusPreventionEffect {
		private static final long serialVersionUID = 1L;

		Immunity() {
			super(AbilityNamesies.IMMUNITY, "Prevents the Pok\u00e9mon from getting poisoned.");
		}

		public Immunity newInstance() {
			return (Immunity)(new Immunity().activate());
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
			return status == StatusCondition.POISONED;
		}

		public String statusPreventionMessage(ActivePokemon victim) {
			return victim.getName() + "'s " + this.getName() + " prevents poisoned!";
		}
	}

	private static class SnowCloak extends Ability implements StageChangingEffect {
		private static final long serialVersionUID = 1L;

		SnowCloak() {
			super(AbilityNamesies.SNOW_CLOAK, "Raises the Pok\u00e9mon's evasion during a hailstorm by one level.");
		}

		public SnowCloak newInstance() {
			return (SnowCloak)(new SnowCloak().activate());
		}

		public int adjustStage(Battle b,  ActivePokemon p, ActivePokemon opp, Stat s, int stage) {
			return s == Stat.EVASION && b.getWeather().namesies() == EffectNamesies.HAILING ? stage + 1 : stage;
		}
	}

	private static class MarvelScale extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		MarvelScale() {
			super(AbilityNamesies.MARVEL_SCALE, "Boosts Defense if there is a status problem.");
		}

		public MarvelScale newInstance() {
			return (MarvelScale)(new MarvelScale().activate());
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.DEFENSE;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && p.hasStatus()) {
				stat *= 1.5;
			}
			
			return stat;
		}
	}

	private static class Multiscale extends Ability implements OpponentPowerChangeEffect {
		private static final long serialVersionUID = 1L;

		Multiscale() {
			super(AbilityNamesies.MULTISCALE, "When this Pok\u00e9mon is at full HP, damage is lessened.");
		}

		public Multiscale newInstance() {
			return (Multiscale)(new Multiscale().activate());
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.fullHealth() ? .5 : 1;
		}
	}

	private static class SheerForce extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		SheerForce() {
			super(AbilityNamesies.SHEER_FORCE, "Attacks gain power, but lose their secondary effect.");
		}

		public SheerForce newInstance() {
			return (SheerForce)(new SheerForce().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getAttack().hasSecondaryEffects() ? 1.3 : 1;
		}
	}

	private static class Hustle extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		Hustle() {
			super(AbilityNamesies.HUSTLE, "Boosts the Attack stat, but lowers accuracy.");
		}

		public Hustle newInstance() {
			return (Hustle)(new Hustle().activate());
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (s == Stat.ATTACK) {
				stat *= 1.5;
			}
			else if (s == Stat.ACCURACY) {
				stat *= .8;
			}
			
			return stat;
		}
	}

	private static class HugePower extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		HugePower() {
			super(AbilityNamesies.HUGE_POWER, "Raises the Pok\u00e9mon's Attack stat.");
		}

		public HugePower newInstance() {
			return (HugePower)(new HugePower().activate());
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.ATTACK;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && true) {
				stat *= 2;
			}
			
			return stat;
		}
	}

	private static class SpeedBoost extends Ability implements EndTurnEffect {
		private static final long serialVersionUID = 1L;

		SpeedBoost() {
			super(AbilityNamesies.SPEED_BOOST, "The Pok\u00e9mon's Speed stat is gradually boosted.");
		}

		public SpeedBoost newInstance() {
			return (SpeedBoost)(new SpeedBoost().activate());
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			victim.getAttributes().modifyStage(victim, victim, 1, Stat.SPEED, b, CastSource.ABILITY);
		}
	}

	private static class MagicBounce extends Ability implements TargetSwapperEffect {
		private static final long serialVersionUID = 1L;

		MagicBounce() {
			super(AbilityNamesies.MAGIC_BOUNCE, "Reflects status-changing moves.");
		}

		public MagicBounce newInstance() {
			return (MagicBounce)(new MagicBounce().activate());
		}

		public boolean swapTarget(Battle b, ActivePokemon user, ActivePokemon opponent) {
			Attack attack = user.getAttack();
			if (!attack.isSelfTarget() && attack.getCategory() == MoveCategory.STATUS && !attack.isMoveType(MoveType.NO_MAGIC_COAT)) {
				b.addMessage(opponent.getName() + "'s " + this.getName() + " reflected " + user.getName() + "'s move!");
				return true;
			}
			
			return false;
		}
	}

	private static class SuperLuck extends Ability implements CritStageEffect {
		private static final long serialVersionUID = 1L;

		SuperLuck() {
			super(AbilityNamesies.SUPER_LUCK, "Heightens the critical-hit ratios of moves.");
		}

		public SuperLuck newInstance() {
			return (SuperLuck)(new SuperLuck().activate());
		}

		public int increaseCritStage(int stage, ActivePokemon p) {
			return stage + 1;
		}
	}

	private static class ShadowTag extends Ability implements OpponentTrappingEffect {
		private static final long serialVersionUID = 1L;

		ShadowTag() {
			super(AbilityNamesies.SHADOW_TAG, "Prevents the foe from escaping.");
		}

		public ShadowTag newInstance() {
			return (ShadowTag)(new ShadowTag().activate());
		}

		public boolean trapOpponent(Battle b, ActivePokemon escaper, ActivePokemon trapper) {
			return !escaper.hasAbility(this.namesies) && !escaper.isType(b, Type.GHOST);
		}

		public String opponentTrappingMessage(ActivePokemon escaper, ActivePokemon trapper) {
			return trapper.getName() + "'s " + this.getName() + " prevents " + escaper.getName() + " from escaping!";
		}
	}

	private static class Overcoat extends Ability implements WeatherBlockerEffect, EffectBlockerEffect {
		private static final long serialVersionUID = 1L;

		Overcoat() {
			super(AbilityNamesies.OVERCOAT, "Protects the Pok\u00e9mon from damage from weather.");
		}

		public Overcoat newInstance() {
			return (Overcoat)(new Overcoat().activate());
		}

		public String getPreventMessage(ActivePokemon victim) {
			return victim.getName() + "'s " + this.getName() + " protects it from powder moves!";
		}

		public boolean block(EffectNamesies weather) {
			return true;
		}

		public boolean validMove(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (!user.getAttack().isMoveType(MoveType.POWDER)) {
				return true;
			}
			
			if (user.getAttack().getCategory() == MoveCategory.STATUS) {
				b.addMessage(getPreventMessage(victim));
			}
			
			return false;
		}
	}

	private static class MagmaArmor extends Ability implements StatusPreventionEffect {
		private static final long serialVersionUID = 1L;

		MagmaArmor() {
			super(AbilityNamesies.MAGMA_ARMOR, "Prevents the Pok\u00e9mon from becoming frozen.");
		}

		public MagmaArmor newInstance() {
			return (MagmaArmor)(new MagmaArmor().activate());
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
			return status == StatusCondition.FROZEN;
		}

		public String statusPreventionMessage(ActivePokemon victim) {
			return victim.getName() + "'s " + this.getName() + " prevents freezing!";
		}
	}

	private static class SuctionCups extends Ability {
		private static final long serialVersionUID = 1L;

		SuctionCups() {
			super(AbilityNamesies.SUCTION_CUPS, "Negates all moves that force switching out.");
		}

		public SuctionCups newInstance() {
			return (SuctionCups)(new SuctionCups().activate());
		}
	}

	private static class Steadfast extends Ability {
		private static final long serialVersionUID = 1L;

		Steadfast() {
			super(AbilityNamesies.STEADFAST, "Raises Speed each time the Pok\u00e9mon flinches.");
		}

		public Steadfast newInstance() {
			return (Steadfast)(new Steadfast().activate());
		}
	}

	private static class SandStream extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		SandStream() {
			super(AbilityNamesies.SAND_STREAM, "The Pok\u00e9mon summons a sandstorm in battle.");
		}

		public SandStream newInstance() {
			return (SandStream)(new SandStream().activate());
		}

		public void enter(Battle b, ActivePokemon enterer) {
			b.addEffect(Weather.getEffect(EffectNamesies.SANDSTORM).newInstance());
			b.addMessage(enterer.getName() + "'s " + this.getName() + " whipped up a sandstorm!");
		}
	}

	private static class Regenerator extends Ability implements SwitchOutEffect {
		private static final long serialVersionUID = 1L;

		Regenerator() {
			super(AbilityNamesies.REGENERATOR, "Restores a little HP when withdrawn from battle.");
		}

		public Regenerator newInstance() {
			return (Regenerator)(new Regenerator().activate());
		}

		public void switchOut(ActivePokemon switchee) {
			if (!switchee.hasStatus(StatusCondition.FAINTED)) {
				switchee.healHealthFraction(1/3.0);
			}
		}
	}

	private static class PoisonHeal extends Ability {
		private static final long serialVersionUID = 1L;

		PoisonHeal() {
			super(AbilityNamesies.POISON_HEAL, "Restores HP if the Pok\u00e9mon is poisoned.");
		}

		public PoisonHeal newInstance() {
			return (PoisonHeal)(new PoisonHeal().activate());
		}
	}

	private static class Truant extends Ability implements EndTurnEffect, BeforeTurnEffect {
		private static final long serialVersionUID = 1L;
		private boolean lazyface;

		Truant() {
			super(AbilityNamesies.TRUANT, "Pok\u00e9mon can't attack on consecutive turns.");
		}

		public Truant newInstance() {
			Truant x = (Truant)(new Truant().activate());
			x.lazyface = false;
			return x;
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.hasStatus(StatusCondition.ASLEEP)) {
				lazyface = false;
			}
			else {
				lazyface = !lazyface;
			}
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			if (lazyface) {
				b.addMessage(p.getName() + " is loafing around!");
				return false;
			}
			
			return true;
		}
	}

	private static class WonderGuard extends Ability implements OpponentBeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		WonderGuard() {
			super(AbilityNamesies.WONDER_GUARD, "Only supereffective moves will hit.");
		}

		public WonderGuard newInstance() {
			return (WonderGuard)(new WonderGuard().activate());
		}

		public boolean opposingCanAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			// Status moves always hit
			if (p.getAttack().getCategory() == MoveCategory.STATUS) {
				return true;
			}
			
			// Super effective moves hit
			if (Type.getAdvantage(p, opp, b) > 1) {
				return true;
			}
			
			// None-type moves always hit
			if (p.isAttackType(Type.NO_TYPE)) {
				return true;
			}
			
			// Immunity Solunity
			b.printAttacking(p);
			b.addMessage(opp.getName() + "'s " + this.getName() + " makes it immune to " + p.getAttack().getName() + "!");
			return false;
		}
	}

	private static class Normalize extends Ability implements ChangeAttackTypeEffect {
		private static final long serialVersionUID = 1L;

		Normalize() {
			super(AbilityNamesies.NORMALIZE, "All the Pok\u00e9mon's moves become the Normal type.");
		}

		public Normalize newInstance() {
			return (Normalize)(new Normalize().activate());
		}

		public Type changeAttackType(Type original) {
			return Type.NORMAL;
		}
	}

	private static class Stall extends Ability implements StallingEffect {
		private static final long serialVersionUID = 1L;

		Stall() {
			super(AbilityNamesies.STALL, "The Pok\u00e9mon moves after even slower foes.");
		}

		public Stall newInstance() {
			return (Stall)(new Stall().activate());
		}
	}

	private static class PurePower extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		PurePower() {
			super(AbilityNamesies.PURE_POWER, "Raises the Pok\u00e9mon's Attack stat.");
		}

		public PurePower newInstance() {
			return (PurePower)(new PurePower().activate());
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.ATTACK;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && true) {
				stat *= 2;
			}
			
			return stat;
		}
	}

	private static class RoughSkin extends Ability implements PhysicalContactEffect {
		private static final long serialVersionUID = 1L;

		RoughSkin() {
			super(AbilityNamesies.ROUGH_SKIN, "Inflicts damage to the foe on contact.");
		}

		public RoughSkin newInstance() {
			return (RoughSkin)(new RoughSkin().activate());
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
			b.addMessage(user.getName() + " was hurt by " + victim.getName() + "'s " + this.getName() + "!");
			user.reduceHealthFraction(b, 1/8.0);
		}
	}

	private static class SolidRock extends Ability implements OpponentPowerChangeEffect {
		private static final long serialVersionUID = 1L;

		SolidRock() {
			super(AbilityNamesies.SOLID_ROCK, "Reduces damage from supereffective attacks.");
		}

		public SolidRock newInstance() {
			return (SolidRock)(new SolidRock().activate());
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return Type.getAdvantage(user, victim, b) < 1 ? .75 : 1;
		}
	}

	private static class WhiteSmoke extends Ability implements StatProtectingEffect {
		private static final long serialVersionUID = 1L;

		WhiteSmoke() {
			super(AbilityNamesies.WHITE_SMOKE, "Prevents other Pok\u00e9mon from lowering its stats.");
		}

		public WhiteSmoke newInstance() {
			return (WhiteSmoke)(new WhiteSmoke().activate());
		}

		public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
			return true;
		}

		public String preventionMessage(ActivePokemon p, Stat s) {
			return p.getName() + "'s " + this.getName() + " prevents its " + s.getName().toLowerCase() + " from being lowered!";
		}
	}

	private static class ToxicBoost extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		ToxicBoost() {
			super(AbilityNamesies.TOXIC_BOOST, "Powers up physical attacks when poisoned.");
		}

		public ToxicBoost newInstance() {
			return (ToxicBoost)(new ToxicBoost().activate());
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.ATTACK;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && p.hasStatus(StatusCondition.POISONED)) {
				stat *= 1.5;
			}
			
			return stat;
		}
	}

	private static class Anticipation extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		Anticipation() {
			super(AbilityNamesies.ANTICIPATION, "Senses a foe's dangerous moves.");
		}

		public Anticipation newInstance() {
			return (Anticipation)(new Anticipation().activate());
		}

		public void enter(Battle b, ActivePokemon enterer) {
			ActivePokemon other = b.getOtherPokemon(enterer.user());
			for (Move m : other.getMoves(b)) {
				Attack attack = m.getAttack();
				if (Type.getBasicAdvantage(attack.getActualType(), enterer, b) > 1 || attack.isMoveType(MoveType.ONE_HIT_KO)) {
					// TODO: Shouldn't this be for a random move?
					b.addMessage(enterer.getName() + "'s " + this.getName() + " made it shudder!");
					break;
				}
			}
		}
	}

	private static class StormDrain extends Ability implements DamageBlocker {
		private static final long serialVersionUID = 1L;

		StormDrain() {
			super(AbilityNamesies.STORM_DRAIN, "Draws in all Water-type moves to up Sp. Attack.");
		}

		public StormDrain newInstance() {
			return (StormDrain)(new StormDrain().activate());
		}

		public Stat toIncrease() {
			return Stat.SP_ATTACK;
		}

		public boolean block(Type attacking, ActivePokemon victim) {
			return attacking == Type.WATER;
		}

		public void alternateEffect(Battle b, ActivePokemon victim) {
			b.addMessage(victim.getName() + "'s " + this.getName() + " makes it immune to " + Type.WATER.getName() + " type moves!");
			victim.getAttributes().modifyStage(victim, victim, 1, toIncrease(), b, CastSource.ABILITY);
		}
	}

	private static class ColorChange extends Ability implements TakeDamageEffect, ChangeTypeMove {
		private static final long serialVersionUID = 1L;
		private Type type;

		ColorChange() {
			super(AbilityNamesies.COLOR_CHANGE, "Changes the Pok\u00e9mon's type to the foe's move.");
		}

		public ColorChange newInstance() {
			ColorChange x = (ColorChange)(new ColorChange().activate());
			x.type = type;
			return x;
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
			Type t = user.getAttackType();
			if (!victim.isType(b, t)) {
				type = t;
				PokemonEffect.getEffect(EffectNamesies.CHANGE_TYPE).cast(b, victim, victim, CastSource.ABILITY, true);
			}
		}

		public Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim) {
			return new Type[] { type, Type.NO_TYPE };
		}
	}

	private static class IceBody extends Ability implements EndTurnEffect, WeatherBlockerEffect {
		private static final long serialVersionUID = 1L;

		IceBody() {
			super(AbilityNamesies.ICE_BODY, "The Pok\u00e9mon gradually regains HP in a hailstorm.");
		}

		public IceBody newInstance() {
			return (IceBody)(new IceBody().activate());
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (b.getWeather().namesies() == EffectNamesies.HAILING) {
				victim.healHealthFraction(1/16.0);
				b.addMessage(victim.getName() + "'s HP was restored due to its " + this.getName() + "!", victim);
			}
		}

		public boolean block(EffectNamesies weather) {
			return weather == EffectNamesies.HAILING;
		}
	}

	private static class LightMetal extends Ability implements HalfWeightEffect {
		private static final long serialVersionUID = 1L;

		LightMetal() {
			super(AbilityNamesies.LIGHT_METAL, "Halves the Pok\u00e9mon's weight.");
		}

		public LightMetal newInstance() {
			return (LightMetal)(new LightMetal().activate());
		}

		public int getHalfAmount(int halfAmount) {
			return halfAmount + 1;
		}
	}

	private static class Drizzle extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		Drizzle() {
			super(AbilityNamesies.DRIZZLE, "The Pok\u00e9mon makes it rain if it appears in battle.");
		}

		public Drizzle newInstance() {
			return (Drizzle)(new Drizzle().activate());
		}

		public void enter(Battle b, ActivePokemon enterer) {
			b.addEffect(Weather.getEffect(EffectNamesies.RAINING).newInstance());
			b.addMessage(enterer.getName() + "'s " + this.getName() + " started a downpour!");
		}
	}

	private static class AirLock extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		AirLock() {
			super(AbilityNamesies.AIR_LOCK, "Eliminates the effects of weather.");
		}

		public AirLock newInstance() {
			return (AirLock)(new AirLock().activate());
		}

		public void enter(Battle b, ActivePokemon enterer) {
			// TODO: I think this isn't the intended effect of this ability
			b.addEffect(Weather.getEffect(EffectNamesies.CLEAR_SKIES));
			b.addMessage(enterer.getName() + "'s " + this.getName() + " eliminated the weather!");
		}
	}

	private static class Defiant extends Ability implements StatLoweredEffect {
		private static final long serialVersionUID = 1L;

		Defiant() {
			super(AbilityNamesies.DEFIANT, "Boosts the Attack stat when a stat is lowered.");
		}

		public Defiant newInstance() {
			return (Defiant)(new Defiant().activate());
		}

		public void takeItToTheNextLevel(Battle b, ActivePokemon caster, ActivePokemon victim) {
			victim.getAttributes().modifyStage(victim, victim, 2, Stat.ATTACK, b, CastSource.ABILITY);
		}
	}

	private static class Competitive extends Ability implements StatLoweredEffect {
		private static final long serialVersionUID = 1L;

		Competitive() {
			super(AbilityNamesies.COMPETITIVE, "Boosts the Sp. Atk stat when a stat is lowered.");
		}

		public Competitive newInstance() {
			return (Competitive)(new Competitive().activate());
		}

		public void takeItToTheNextLevel(Battle b, ActivePokemon caster, ActivePokemon victim) {
			victim.getAttributes().modifyStage(victim, victim, 2, Stat.SP_ATTACK, b, CastSource.ABILITY);
		}
	}

	private static class FlowerGift extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		FlowerGift() {
			super(AbilityNamesies.FLOWER_GIFT, "Powers up party Pok\u00e9mon when it is sunny.");
		}

		public FlowerGift newInstance() {
			return (FlowerGift)(new FlowerGift().activate());
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.ATTACK || s == Stat.SP_DEFENSE;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && b.getWeather().namesies() == EffectNamesies.SUNNY) {
				stat *= 1.5;
			}
			
			return stat;
		}
	}

	private static class Aftermath extends Ability implements PhysicalContactEffect {
		private static final long serialVersionUID = 1L;

		Aftermath() {
			super(AbilityNamesies.AFTERMATH, "Damages the attacker landing the finishing hit.");
		}

		public Aftermath newInstance() {
			return (Aftermath)(new Aftermath().activate());
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
			// TODO: Pretty sure this doesn't work anymore
			if (victim.isFainted(b)) {
				b.addMessage(user.getName() + " was hurt by " + victim.getName() + "'s " + this.getName() + "!");
				user.reduceHealthFraction(b, 1/4.0);
			}
		}
	}

	private static class Heatproof extends Ability implements OpponentPowerChangeEffect {
		private static final long serialVersionUID = 1L;

		Heatproof() {
			super(AbilityNamesies.HEATPROOF, "Weakens the power of Fire-type moves.");
		}

		public Heatproof newInstance() {
			return (Heatproof)(new Heatproof().activate());
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getAttackType() == Type.FIRE ? .5 : 1;
		}
	}

	private static class SandForce extends Ability implements PowerChangeEffect, WeatherBlockerEffect {
		private static final long serialVersionUID = 1L;

		SandForce() {
			super(AbilityNamesies.SAND_FORCE, "Boosts certain moves' power in a sandstorm.");
		}

		public SandForce newInstance() {
			return (SandForce)(new SandForce().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			Type type = user.getAttackType();
			return (type == Type.ROCK || type == Type.STEEL || type == Type.GROUND) && b.getWeather().namesies() == EffectNamesies.SANDSTORM ? 1.3 : 1;
		}

		public boolean block(EffectNamesies weather) {
			return weather == EffectNamesies.SANDSTORM;
		}
	}

	private static class SnowWarning extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		SnowWarning() {
			super(AbilityNamesies.SNOW_WARNING, "The Pok\u00e9mon summons a hailstorm in battle.");
		}

		public SnowWarning newInstance() {
			return (SnowWarning)(new SnowWarning().activate());
		}

		public void enter(Battle b, ActivePokemon enterer) {
			b.addEffect(Weather.getEffect(EffectNamesies.HAILING).newInstance());
			b.addMessage(enterer.getName() + "'s " + this.getName() + " caused it to hail!");
		}
	}

	private static class MotorDrive extends Ability implements DamageBlocker {
		private static final long serialVersionUID = 1L;

		MotorDrive() {
			super(AbilityNamesies.MOTOR_DRIVE, "Raises Speed if hit by an Electric-type move.");
		}

		public MotorDrive newInstance() {
			return (MotorDrive)(new MotorDrive().activate());
		}

		public Stat toIncrease() {
			return Stat.SPEED;
		}

		public boolean block(Type attacking, ActivePokemon victim) {
			return attacking == Type.ELECTRIC;
		}

		public void alternateEffect(Battle b, ActivePokemon victim) {
			b.addMessage(victim.getName() + "'s " + this.getName() + " makes it immune to " + Type.ELECTRIC.getName() + " type moves!");
			victim.getAttributes().modifyStage(victim, victim, 1, toIncrease(), b, CastSource.ABILITY);
		}
	}

	private static class Justified extends Ability implements TakeDamageEffect {
		private static final long serialVersionUID = 1L;

		Justified() {
			super(AbilityNamesies.JUSTIFIED, "Raises Attack when hit by a Dark-type move.");
		}

		public Justified newInstance() {
			return (Justified)(new Justified().activate());
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttackType() == Type.DARK) {
				victim.getAttributes().modifyStage(victim, victim, 1, Stat.ATTACK, b, CastSource.ABILITY);
			}
		}
	}

	private static class CursedBody extends Ability implements PhysicalContactEffect {
		private static final long serialVersionUID = 1L;

		CursedBody() {
			super(AbilityNamesies.CURSED_BODY, "May disable a move used on the Pok\u00e9mon.");
		}

		public CursedBody newInstance() {
			return (CursedBody)(new CursedBody().activate());
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (Global.chanceTest(30)) {
				user.getAttributes().setLastMoveUsed();
				PokemonEffect disable = PokemonEffect.getEffect(EffectNamesies.DISABLE);
				if (disable.applies(b, victim, user, CastSource.ABILITY)) {
					disable.cast(b, victim, user, CastSource.ABILITY, false);
					b.addMessage(victim.getName() + "'s " + this.getName() + " disabled " + user.getName() + "'s " + user.getAttack().getName());
				}
			}
		}
	}

	private static class SlowStart extends Ability implements EndTurnEffect, EntryEffect, StatChangingEffect {
		private static final long serialVersionUID = 1L;
		int count;

		SlowStart() {
			super(AbilityNamesies.SLOW_START, "Temporarily halves Attack and Speed.");
		}

		public SlowStart newInstance() {
			SlowStart x = (SlowStart)(new SlowStart().activate());
			x.count = 0;
			return x;
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.ATTACK || s == Stat.SPEED;
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			count++;
		}

		public void enter(Battle b, ActivePokemon enterer) {
			count = 0;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && count < 5) {
				stat *= .5;
			}
			
			return stat;
		}
	}

	private static class BadDreams extends Ability implements EndTurnEffect {
		private static final long serialVersionUID = 1L;

		BadDreams() {
			super(AbilityNamesies.BAD_DREAMS, "Reduces a sleeping foe's HP.");
		}

		public BadDreams newInstance() {
			return (BadDreams)(new BadDreams().activate());
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			ActivePokemon other = b.getOtherPokemon(victim.user());
			if (other.hasStatus(StatusCondition.ASLEEP)) {
				b.addMessage(other.getName() + " was hurt by " + victim.getName() + "'s " + this.getName() + "!");
				other.reduceHealthFraction(b, 1/8.0);
			}
		}
	}

	private static class VictoryStar extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		VictoryStar() {
			super(AbilityNamesies.VICTORY_STAR, "Boosts the accuracy of its allies and itself.");
		}

		public VictoryStar newInstance() {
			return (VictoryStar)(new VictoryStar().activate());
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.ACCURACY;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && true) {
				stat *= 1.1;
			}
			
			return stat;
		}
	}

	private static class Contrary extends Ability implements ModifyStageValueEffect {
		private static final long serialVersionUID = 1L;

		Contrary() {
			super(AbilityNamesies.CONTRARY, "Makes stat changes have an opposite effect.");
		}

		public Contrary newInstance() {
			return (Contrary)(new Contrary().activate());
		}

		public int modifyStageValue(int modVal) {
			return modVal*(modVal < 0 ? -1 : 1);
		}
	}

	private static class BigPecks extends Ability implements StatProtectingEffect {
		private static final long serialVersionUID = 1L;

		BigPecks() {
			super(AbilityNamesies.BIG_PECKS, "Protects the Pok\u00e9mon from Defense-lowering attacks.");
		}

		public BigPecks newInstance() {
			return (BigPecks)(new BigPecks().activate());
		}

		public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
			return stat == Stat.DEFENSE;
		}

		public String preventionMessage(ActivePokemon p, Stat s) {
			return p.getName() + "'s " + this.getName() + " prevents its " + s.getName().toLowerCase() + " from being lowered!";
		}
	}

	private static class PoisonTouch extends Ability implements ApplyDamageEffect {
		private static final long serialVersionUID = 1L;

		PoisonTouch() {
			super(AbilityNamesies.POISON_TOUCH, "May poison targets when a Pok\u00e9mon makes contact.");
		}

		public PoisonTouch newInstance() {
			return (PoisonTouch)(new PoisonTouch().activate());
		}

		public void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
			if (Global.chanceTest(30)) {
				Status.giveStatus(b, user, victim, StatusCondition.POISONED, true);
			}
		}
	}

	private static class Prankster extends Ability implements PriorityChangeEffect {
		private static final long serialVersionUID = 1L;

		Prankster() {
			super(AbilityNamesies.PRANKSTER, "Gives priority to a status move.");
		}

		public Prankster newInstance() {
			return (Prankster)(new Prankster().activate());
		}

		public int changePriority(Battle b, ActivePokemon user, int priority) {
			if (user.getAttack().getCategory() == MoveCategory.STATUS) {
				if (this instanceof ConsumableItem) {
					user.consumeItem(b);
				}
				
				priority++;
			}
			
			return priority;
		}
	}

	private static class WonderSkin extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		WonderSkin() {
			super(AbilityNamesies.WONDER_SKIN, "Makes status-changing moves more likely to miss.");
		}

		public WonderSkin newInstance() {
			return (WonderSkin)(new WonderSkin().activate());
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.EVASION;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && opp.getAttack().getCategory() == MoveCategory.STATUS) {
				stat *= 1.5;
			}
			
			return stat;
		}
	}

	private static class Mummy extends Ability implements PhysicalContactEffect, ChangeAbilityMove {
		private static final long serialVersionUID = 1L;

		Mummy() {
			super(AbilityNamesies.MUMMY, "Contact with this Pok\u00e9mon spreads this Ability.");
		}

		public Mummy newInstance() {
			return (Mummy)(new Mummy().activate());
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.hasAbility(this.namesies) || user.hasAbility(AbilityNamesies.MULTITYPE) || user.hasAbility(AbilityNamesies.STANCE_CHANGE)) {
				return;
			}
			
			// Cast the change ability effect onto the user
			PokemonEffect.getEffect(EffectNamesies.CHANGE_ABILITY).cast(b, victim, user, CastSource.ABILITY, true);
		}

		public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim) {
			return (new Mummy()).newInstance();
		}

		public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim) {
			return victim.getName() + "'s ability was changed to " + this.namesies().getName() + "!";
		}
	}

	private static class Defeatist extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		Defeatist() {
			super(AbilityNamesies.DEFEATIST, "Lowers stats when HP becomes half or less.");
		}

		public Defeatist newInstance() {
			return (Defeatist)(new Defeatist().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getHPRatio() < 1/2.0 ? .5 : 1;
		}
	}

	private static class WeakArmor extends Ability implements TakeDamageEffect {
		private static final long serialVersionUID = 1L;

		WeakArmor() {
			super(AbilityNamesies.WEAK_ARMOR, "Physical attacks lower Defense and raise Speed.");
		}

		public WeakArmor newInstance() {
			return (WeakArmor)(new WeakArmor().activate());
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttack().getCategory() == MoveCategory.PHYSICAL) {
				victim.getAttributes().modifyStage(victim, victim, -1, Stat.DEFENSE, b, CastSource.ABILITY);
				victim.getAttributes().modifyStage(victim, victim, 1, Stat.SPEED, b, CastSource.ABILITY);
			}
		}
	}

	private static class Illusion extends Ability implements EntryEffect, SwitchOutEffect, TakeDamageEffect, ChangeTypeEffect, NameChanger {
		private static final long serialVersionUID = 1L;
		private boolean activated;
		private String illusionName;
		private Type[] illusionType;
		private PokemonInfo illusionSpecies;
		private boolean illusionShiny;
		
		private void breakIllusion(Battle b, ActivePokemon victim) {
			// If the Illusion is already broken, no worries
			if (!activated) {
				return;
			}
			
			activated = false;
			b.addMessage(victim.getName() + "'s Illusion was broken!");
			
			b.addMessage("", victim.getPokemonInfo(), victim.isShiny(), true, victim.user());
			b.addMessage("", victim);
		}

		Illusion() {
			super(AbilityNamesies.ILLUSION, "Comes out disguised as the Pok\u00e9mon in back.");
		}

		public Illusion newInstance() {
			Illusion x = (Illusion)(new Illusion().activate());
			x.activated = false;
			return x;
		}

		public boolean isActive() {
			return activated;
		}

		public void deactivate(Battle b, ActivePokemon victim) {
			breakIllusion(b, victim);
		}

		public void enter(Battle b, ActivePokemon enterer) {
			// No Illusion today...
			if (!activated) {
				return;
			}
			
			// Display the Illusion changes
			b.addMessage("", illusionSpecies, illusionShiny, false, enterer.user());
			b.addMessage("", enterer);
		}

		public void switchOut(ActivePokemon switchee) {
			activated = false;
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
			breakIllusion(b, victim);
		}

		public Type[] getType(Battle b, ActivePokemon p, boolean display) {
			if (display && activated) {
				return illusionType;
			}
			
			return p.getActualType();
		}

		public String getNameChange() {
			return activated ? illusionName : null;
		}

		public void setNameChange(Battle b, ActivePokemon victim) {
			List<ActivePokemon> team = b.getTrainer(victim.user()).getTeam();
			ActivePokemon illusion = null;
			
			// Starting from the back of the party, locate the first conscious Pokemon that is of a different species to be the illusion
			for (int i = team.size() - 1; i > 0; i--) {
				ActivePokemon temp = team.get(i);
				
				// If the Pokemon in back cannot fight for any reason -- do nothing
				if (!temp.canFight()) {
					continue;
				}
				
				// If the Pokemon in back is the same species at the current Pokemon -- do nothing
				if (temp.getPokemonInfo().getNumber() == victim.getPokemonInfo().getNumber()) {
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
			illusionSpecies = illusion.getPokemonInfo();
			illusionShiny = illusion.isShiny();
		}
	}

	private static class Analytic extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		Analytic() {
			super(AbilityNamesies.ANALYTIC, "Boosts move power when the Pok\u00e9mon moves last.");
		}

		public Analytic newInstance() {
			return (Analytic)(new Analytic().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return !b.isFirstAttack() ? 1.3 : 1;
		}
	}

	private static class SapSipper extends Ability implements DamageBlocker {
		private static final long serialVersionUID = 1L;

		SapSipper() {
			super(AbilityNamesies.SAP_SIPPER, "Boosts Attack when hit by a Grass-type move.");
		}

		public SapSipper newInstance() {
			return (SapSipper)(new SapSipper().activate());
		}

		public Stat toIncrease() {
			return Stat.ATTACK;
		}

		public boolean block(Type attacking, ActivePokemon victim) {
			return attacking == Type.GRASS;
		}

		public void alternateEffect(Battle b, ActivePokemon victim) {
			b.addMessage(victim.getName() + "'s " + this.getName() + " makes it immune to " + Type.GRASS.getName() + " type moves!");
			victim.getAttributes().modifyStage(victim, victim, 1, toIncrease(), b, CastSource.ABILITY);
		}
	}

	private static class IronBarbs extends Ability implements PhysicalContactEffect {
		private static final long serialVersionUID = 1L;

		IronBarbs() {
			super(AbilityNamesies.IRON_BARBS, "Inflicts damage to the Pok\u00e9mon on contact.");
		}

		public IronBarbs newInstance() {
			return (IronBarbs)(new IronBarbs().activate());
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
			b.addMessage(user.getName() + " was hurt by " + victim.getName() + "'s " + this.getName() + "!");
			user.reduceHealthFraction(b, 1/8.0);
		}
	}

	private static class MoldBreaker extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		MoldBreaker() {
			super(AbilityNamesies.MOLD_BREAKER, "Moves can be used regardless of Abilities.");
		}

		public MoldBreaker newInstance() {
			return (MoldBreaker)(new MoldBreaker().activate());
		}

		public void enter(Battle b, ActivePokemon enterer) {
			b.addMessage(enterer.getName() + " breaks the mold!");
		}
	}

	private static class Teravolt extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		Teravolt() {
			super(AbilityNamesies.TERAVOLT, "Moves can be used regardless of Abilities.");
		}

		public Teravolt newInstance() {
			return (Teravolt)(new Teravolt().activate());
		}

		public void enter(Battle b, ActivePokemon enterer) {
			b.addMessage(enterer.getName() + " is radiating a bursting aura!");
		}
	}

	private static class Turboblaze extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		Turboblaze() {
			super(AbilityNamesies.TURBOBLAZE, "Moves can be used regardless of Abilities.");
		}

		public Turboblaze newInstance() {
			return (Turboblaze)(new Turboblaze().activate());
		}

		public void enter(Battle b, ActivePokemon enterer) {
			b.addMessage(enterer.getName() + " is radiating a blazing aura!");
		}
	}

	private static class RunAway extends Ability implements DefiniteEscape {
		private static final long serialVersionUID = 1L;

		RunAway() {
			super(AbilityNamesies.RUN_AWAY, "Enables a sure getaway from wild Pok\u00e9mon.");
		}

		public RunAway newInstance() {
			return (RunAway)(new RunAway().activate());
		}
	}

	private static class StickyHold extends Ability {
		private static final long serialVersionUID = 1L;

		StickyHold() {
			super(AbilityNamesies.STICKY_HOLD, "Protects the Pok\u00e9mon from item theft.");
		}

		public StickyHold newInstance() {
			return (StickyHold)(new StickyHold().activate());
		}
	}

	private static class Klutz extends Ability {
		private static final long serialVersionUID = 1L;

		Klutz() {
			super(AbilityNamesies.KLUTZ, "The Pok\u00e9mon can't use any held items.");
		}

		public Klutz newInstance() {
			return (Klutz)(new Klutz().activate());
		}
	}

	private static class Unburden extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		Unburden() {
			super(AbilityNamesies.UNBURDEN, "Raises Speed if a held item is used.");
		}

		public Unburden newInstance() {
			return (Unburden)(new Unburden().activate());
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.SPEED;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && p.hasEffect(EffectNamesies.CONSUMED_ITEM)) {
				stat *= 2;
			}
			
			return stat;
		}
	}

	private static class Pickpocket extends Ability implements PhysicalContactEffect, ItemHolder {
		private static final long serialVersionUID = 1L;
		private Item item;

		Pickpocket() {
			super(AbilityNamesies.PICKPOCKET, "Steals an item when hit by another Pok\u00e9mon.");
		}

		public Pickpocket newInstance() {
			return (Pickpocket)(new Pickpocket().activate());
		}

		public void steal(Battle b, ActivePokemon thief, ActivePokemon victim) {
			// Dead Pokemon and wild Pokemon cannot steal;
			// Cannot steal if victim is not holding an item or thief is already holding an item;
			// Cannot steal from a Pokemon with the Sticky Hold ability
			if (thief.isFainted(b) || !victim.isHoldingItem(b) || thief.isHoldingItem(b) || b.getTrainer(thief.user()) instanceof WildPokemon || victim.hasAbility(AbilityNamesies.STICKY_HOLD)) {
				return;
			}
			
			// Stealers gon' steal
			Item stolen = victim.getHeldItem(b);
			b.addMessage(thief.getName() + " stole " + victim.getName() + "'s " + stolen.getName() + "!");
			
			if (b.isWildBattle()) {
				victim.removeItem();
				thief.giveItem((HoldItem)stolen);
				return;
			}
			
			item = stolen;
			PokemonEffect.getEffect(EffectNamesies.CHANGE_ITEM).cast(b, thief, thief, CastSource.ABILITY, false);
			
			item = Item.noneItem();
			PokemonEffect.getEffect(EffectNamesies.CHANGE_ITEM).cast(b, thief, victim, CastSource.ABILITY, false);
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
			// Steal from the Pokemon who made physical contact with you
			steal(b, victim, user);
		}

		public Item getItem() {
			return item;
		}
	}

	private static class Harvest extends Ability implements EndTurnEffect {
		private static final long serialVersionUID = 1L;

		Harvest() {
			super(AbilityNamesies.HARVEST, "May create another Berry after one is used.");
		}

		public Harvest newInstance() {
			return (Harvest)(new Harvest().activate());
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			PokemonEffect consumed = victim.getEffect(EffectNamesies.CONSUMED_ITEM);
			if (consumed == null || victim.isHoldingItem(b)) {
				return;
			}
			
			Item restored = ((ItemHolder)consumed).getItem();
			if (restored instanceof Berry && (b.getWeather().namesies() == EffectNamesies.SUNNY || Global.chanceTest(50))) {
				victim.giveItem((HoldItem)restored);
				b.addMessage(victim.getName() + "'s " + this.getName() + " restored its " + restored.getName() + "!");
			}
		}
	}

	private static class Pickup extends Ability implements EndBattleEffect {
		private static final long serialVersionUID = 1L;

		Pickup() {
			super(AbilityNamesies.PICKUP, "The Pok\u00e9mon may pick up items.");
		}

		public Pickup newInstance() {
			return (Pickup)(new Pickup().activate());
		}

		public void afterBattle(Trainer player, Battle b, ActivePokemon p) {
			if (!p.isHoldingItem(b) && Global.chanceTest(10)) {
				// TODO: THIS SHOULDN'T JUST BE LEFTOVERS IT SHOULD BE MORE FUN STUFF
				p.giveItem((HoldItem)Item.getItem(ItemNamesies.LEFTOVERS));
			}
		}
	}

	private static class Unnerve extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		Unnerve() {
			super(AbilityNamesies.UNNERVE, "Makes the foe nervous and unable to eat Berries.");
		}

		public Unnerve newInstance() {
			return (Unnerve)(new Unnerve().activate());
		}

		public void enter(Battle b, ActivePokemon enterer) {
			b.addMessage(enterer.getName() + "'s " + this.getName() + " made " + b.getOtherPokemon(enterer.user()).getName() + " too nervous to eat berries!");
		}
	}

	private static class HoneyGather extends Ability implements EndBattleEffect {
		private static final long serialVersionUID = 1L;

		HoneyGather() {
			super(AbilityNamesies.HONEY_GATHER, "The Pok\u00e9mon may gather Honey from somewhere.");
		}

		public HoneyGather newInstance() {
			return (HoneyGather)(new HoneyGather().activate());
		}

		public void afterBattle(Trainer player, Battle b, ActivePokemon p) {
			if (!p.isHoldingItem(b) && Global.chanceTest(5*(int)Math.ceil(p.getLevel()/10.0))) {
				// TODO: Should give the item Honey, but this item has no purpose in our game so we'll see what this ability should actually do also something about Syrup Gather
				p.giveItem((HoldItem)Item.getItem(ItemNamesies.LEFTOVERS));
			}
		}
	}

	private static class Gluttony extends Ability {
		private static final long serialVersionUID = 1L;

		Gluttony() {
			super(AbilityNamesies.GLUTTONY, "Makes the Pok\u00e9mon use a held Berry earlier than usual.");
		}

		public Gluttony newInstance() {
			return (Gluttony)(new Gluttony().activate());
		}
	}

	private static class Multitype extends Ability implements ChangeTypeEffect {
		private static final long serialVersionUID = 1L;

		Multitype() {
			super(AbilityNamesies.MULTITYPE, "Changes type to match the held Plate.");
		}

		public Multitype newInstance() {
			return (Multitype)(new Multitype().activate());
		}

		public Type[] getType(Battle b, ActivePokemon p, boolean display) {
			Item item = p.getHeldItem(b);
			if (item instanceof PlateItem) {
				return new Type[] { ((PlateItem)item).getType(), Type.NO_TYPE };
			}
			
			return p.getActualType();
		}
	}

	private static class Forecast extends Ability implements ChangeTypeEffect {
		private static final long serialVersionUID = 1L;

		Forecast() {
			super(AbilityNamesies.FORECAST, "Changes with the weather.");
		}

		public Forecast newInstance() {
			return (Forecast)(new Forecast().activate());
		}

		public Type[] getType(Battle b, ActivePokemon p, boolean display) {
			return new Type[] { b.getWeather().getElement(), Type.NO_TYPE };
		}
	}

	private static class Bulletproof extends Ability implements OpponentBeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		Bulletproof() {
			super(AbilityNamesies.BULLETPROOF, "Protects the Pok\u00e9mon from some ball and bomb moves.");
		}

		public Bulletproof newInstance() {
			return (Bulletproof)(new Bulletproof().activate());
		}

		public boolean opposingCanAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			if (p.getAttack().isMoveType(MoveType.BOMB_BALL)) {
				b.printAttacking(p);
				b.addMessage(opp.getName() + "'s " + this.getName() + " prevents " + p.getAttack().getName() + " from being used!");
				return false;
			}
			
			return true;
		}
	}

	private static class AuraBreak extends Ability {
		private static final long serialVersionUID = 1L;

		AuraBreak() {
			super(AbilityNamesies.AURA_BREAK, "The effects of Aura Abilities are reversed.");
		}

		public AuraBreak newInstance() {
			return (AuraBreak)(new AuraBreak().activate());
		}
	}

	private static class FairyAura extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		FairyAura() {
			super(AbilityNamesies.FAIRY_AURA, "Powers up each Pok\u00e9mon's Fairy-type moves.");
		}

		public FairyAura newInstance() {
			return (FairyAura)(new FairyAura().activate());
		}

		public Type getAuraType() {
			return Type.FAIRY;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttackType() == getAuraType()) {
				return 1 + .33*(victim.hasAbility(AbilityNamesies.AURA_BREAK) ? -1 : 1);
			}
			
			return 1;
		}
	}

	private static class DarkAura extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		DarkAura() {
			super(AbilityNamesies.DARK_AURA, "Powers up each Pok\u00e9mon's Dark-type moves.");
		}

		public DarkAura newInstance() {
			return (DarkAura)(new DarkAura().activate());
		}

		public Type getAuraType() {
			return Type.DARK;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttackType() == getAuraType()) {
				return 1 + .33*(victim.hasAbility(AbilityNamesies.AURA_BREAK) ? -1 : 1);
			}
			
			return 1;
		}
	}

	private static class Magician extends Ability implements ApplyDamageEffect, ItemHolder {
		private static final long serialVersionUID = 1L;
		private Item item;

		Magician() {
			super(AbilityNamesies.MAGICIAN, "The Pok\u00e9mon steals the held item of a Pok\u00e9mon it hits with a move.");
		}

		public Magician newInstance() {
			return (Magician)(new Magician().activate());
		}

		public void steal(Battle b, ActivePokemon thief, ActivePokemon victim) {
			// Dead Pokemon and wild Pokemon cannot steal;
			// Cannot steal if victim is not holding an item or thief is already holding an item;
			// Cannot steal from a Pokemon with the Sticky Hold ability
			if (thief.isFainted(b) || !victim.isHoldingItem(b) || thief.isHoldingItem(b) || b.getTrainer(thief.user()) instanceof WildPokemon || victim.hasAbility(AbilityNamesies.STICKY_HOLD)) {
				return;
			}
			
			// Stealers gon' steal
			Item stolen = victim.getHeldItem(b);
			b.addMessage(thief.getName() + " stole " + victim.getName() + "'s " + stolen.getName() + "!");
			
			if (b.isWildBattle()) {
				victim.removeItem();
				thief.giveItem((HoldItem)stolen);
				return;
			}
			
			item = stolen;
			PokemonEffect.getEffect(EffectNamesies.CHANGE_ITEM).cast(b, thief, thief, CastSource.ABILITY, false);
			
			item = Item.noneItem();
			PokemonEffect.getEffect(EffectNamesies.CHANGE_ITEM).cast(b, thief, victim, CastSource.ABILITY, false);
		}

		public void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
			// Steal the victim's item when damage is dealt
			steal(b, user, victim);
		}

		public Item getItem() {
			return item;
		}
	}

	private static class CheekPouch extends Ability {
		private static final long serialVersionUID = 1L;

		CheekPouch() {
			super(AbilityNamesies.CHEEK_POUCH, "Restores HP as well when the Pok\u00e9mon eats a Berry.");
		}

		public CheekPouch newInstance() {
			return (CheekPouch)(new CheekPouch().activate());
		}
	}

	private static class StrongJaw extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		StrongJaw() {
			super(AbilityNamesies.STRONG_JAW, "The Pok\u00e9mon's strong jaw gives it tremendous biting power.");
		}

		public StrongJaw newInstance() {
			return (StrongJaw)(new StrongJaw().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getAttack().isMoveType(MoveType.BITING) ? 1.5 : 1;
		}
	}

	private static class MegaLauncher extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		MegaLauncher() {
			super(AbilityNamesies.MEGA_LAUNCHER, "Powers up aura and pulse moves.");
		}

		public MegaLauncher newInstance() {
			return (MegaLauncher)(new MegaLauncher().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getAttack().isMoveType(MoveType.AURA_PULSE) ? 1.5 : 1;
		}
	}

	private static class ToughClaws extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		ToughClaws() {
			super(AbilityNamesies.TOUGH_CLAWS, "Powers up moves that make direct contact.");
		}

		public ToughClaws newInstance() {
			return (ToughClaws)(new ToughClaws().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getAttack().isMoveType(MoveType.PHYSICAL_CONTACT) ? 1.33 : 1;
		}
	}

	private static class SweetVeil extends Ability implements StatusPreventionEffect {
		private static final long serialVersionUID = 1L;

		SweetVeil() {
			super(AbilityNamesies.SWEET_VEIL, "Prevents itself and ally Pok\u00e9mon from falling asleep.");
		}

		public SweetVeil newInstance() {
			return (SweetVeil)(new SweetVeil().activate());
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
			return status == StatusCondition.ASLEEP;
		}

		public String statusPreventionMessage(ActivePokemon victim) {
			return victim.getName() + "'s " + this.getName() + " prevents sleep!";
		}
	}

	private static class AromaVeil extends Ability {
		private static final long serialVersionUID = 1L;

		AromaVeil() {
			super(AbilityNamesies.AROMA_VEIL, "Protects allies from attacks that effect their mental state.");
		}

		public AromaVeil newInstance() {
			return (AromaVeil)(new AromaVeil().activate());
		}
	}

	private static class Healer extends Ability implements EndTurnEffect {
		private static final long serialVersionUID = 1L;

		Healer() {
			super(AbilityNamesies.HEALER, "The Pok\u00e9mon may heal its own status problems.");
		}

		public Healer newInstance() {
			return (Healer)(new Healer().activate());
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.hasStatus() && Global.chanceTest(1, 3)) {
				Status.removeStatus(b, victim, CastSource.ABILITY);
			}
		}
	}

	private static class Pixilate extends Ability implements ChangeAttackTypeEffect, EndTurnEffect, PowerChangeEffect {
		private static final long serialVersionUID = 1L;
		private boolean activated;

		Pixilate() {
			super(AbilityNamesies.PIXILATE, "Normal-type moves become Fairy-type moves.");
		}

		public Pixilate newInstance() {
			Pixilate x = (Pixilate)(new Pixilate().activate());
			x.activated = false;
			return x;
		}

		public Type getType() {
			return Type.FAIRY;
		}

		public Type changeAttackType(Type original) {
			if (original == Type.NORMAL) {
				this.activated = true;
				return getType();
			}
			
			return original;
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			this.activated = false;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return activated ? 1.3 : 1;
		}
	}

	private static class Refrigerate extends Ability implements ChangeAttackTypeEffect, EndTurnEffect, PowerChangeEffect {
		private static final long serialVersionUID = 1L;
		private boolean activated;

		Refrigerate() {
			super(AbilityNamesies.REFRIGERATE, "Normal-type moves become Ice-type moves.");
		}

		public Refrigerate newInstance() {
			Refrigerate x = (Refrigerate)(new Refrigerate().activate());
			x.activated = false;
			return x;
		}

		public Type getType() {
			return Type.ICE;
		}

		public Type changeAttackType(Type original) {
			if (original == Type.NORMAL) {
				this.activated = true;
				return getType();
			}
			
			return original;
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			this.activated = false;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return activated ? 1.3 : 1;
		}
	}

	private static class StanceChange extends Ability implements BeforeTurnEffect, EntryEffect, DifferentStatEffect {
		private static final long serialVersionUID = 1L;
		private static final int[] BLADE_STATS = new int[] {60, 150, 50, 150, 50, 60};
		private static final int[] SHIELD_STATS = new int[] {60, 50, 150, 50, 150, 60};
		
		private boolean shieldForm;

		StanceChange() {
			super(AbilityNamesies.STANCE_CHANGE, "The Pok\u00e9mon changes form depending on how it battles.");
		}

		public StanceChange newInstance() {
			StanceChange x = (StanceChange)(new StanceChange().activate());
			x.shieldForm = true;
			return x;
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			if (shieldForm && p.getAttack().getCategory() != MoveCategory.STATUS) {
				shieldForm = false;
				b.addMessage(p.getName() + " changed into Blade Forme!");
			}
			else if (!shieldForm && p.getAttack().namesies() == AttackNamesies.KINGS_SHIELD) {
				shieldForm = true;
				b.addMessage(p.getName() + " changed into Shield Forme!");
			}
			
			return true;
		}

		public void enter(Battle b, ActivePokemon enterer) {
			b.addMessage(enterer.getName() + " is in Shield Forme!");
			shieldForm = true;
		}

		public Integer getStat(ActivePokemon user, Stat stat) {
			// Need to calculate the new stat -- yes, I realize this is super inefficient and whatever whatever whatever
			int index = stat.index();
			return Stat.getStat(index, user.getLevel(), (shieldForm ? SHIELD_STATS : BLADE_STATS)[index], user.getIV(index), user.getEV(index), user.getNature().getNatureVal(index));
		}
	}

	private static class FurCoat extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		FurCoat() {
			super(AbilityNamesies.FUR_COAT, "Halves damage from physical moves.");
		}

		public FurCoat newInstance() {
			return (FurCoat)(new FurCoat().activate());
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.DEFENSE;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && true) {
				stat *= 2;
			}
			
			return stat;
		}
	}

	private static class GrassPelt extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		GrassPelt() {
			super(AbilityNamesies.GRASS_PELT, "Boosts the Defense stat in Grassy Terrain.");
		}

		public GrassPelt newInstance() {
			return (GrassPelt)(new GrassPelt().activate());
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.DEFENSE;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && b.hasEffect(EffectNamesies.GRASSY_TERRAIN)) {
				stat *= 1.5;
			}
			
			return stat;
		}
	}

	private static class FlowerVeil extends Ability implements StatusPreventionEffect, StatProtectingEffect {
		private static final long serialVersionUID = 1L;

		FlowerVeil() {
			super(AbilityNamesies.FLOWER_VEIL, "Prevents lowering of Grass-type Pok\u00e9mon's stats.");
		}

		public FlowerVeil newInstance() {
			return (FlowerVeil)(new FlowerVeil().activate());
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
			return victim.isType(b, Type.GRASS);
		}

		public String statusPreventionMessage(ActivePokemon victim) {
			return victim.getName() + "'s " + this.getName() + " prevents status conditions!";
		}

		public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
			return victim.isType(b, Type.GRASS);
		}

		public String preventionMessage(ActivePokemon p, Stat s) {
			return p.getName() + "'s " + this.getName() + " prevents its " + s.getName().toLowerCase() + " from being lowered!";
		}
	}

	private static class GaleWings extends Ability implements PriorityChangeEffect {
		private static final long serialVersionUID = 1L;

		GaleWings() {
			super(AbilityNamesies.GALE_WINGS, "Gives priority to Flying-type moves.");
		}

		public GaleWings newInstance() {
			return (GaleWings)(new GaleWings().activate());
		}

		public int changePriority(Battle b, ActivePokemon user, int priority) {
			if (user.getAttack().getActualType() == Type.FLYING) {
				if (this instanceof ConsumableItem) {
					user.consumeItem(b);
				}
				
				priority++;
			}
			
			return priority;
		}
	}

	private static class Protean extends Ability implements BeforeTurnEffect, ChangeTypeMove {
		private static final long serialVersionUID = 1L;
		private Type type;

		Protean() {
			super(AbilityNamesies.PROTEAN, "Changes the Pok\u00e9mon's type to the type of the move it's using.");
		}

		public Protean newInstance() {
			Protean x = (Protean)(new Protean().activate());
			x.type = type;
			return x;
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			// Protean activates for all moves except for Struggle
			if (p.getAttack().namesies() != AttackNamesies.STRUGGLE) {
				type = p.getAttackType();
				PokemonEffect.getEffect(EffectNamesies.CHANGE_TYPE).cast(b, p, p, CastSource.ABILITY, true);
			}
			
			return true;
		}

		public Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim) {
			return new Type[] { type, Type.NO_TYPE };
		}
	}
}
