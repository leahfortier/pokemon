package pokemon;

import item.Item;
import item.berry.Berry;
import item.hold.HoldItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import main.Global;
import main.Namesies;
import main.Type;
import trainer.Trainer;
import trainer.WildPokemon;
import battle.Attack.Category;
import battle.Attack.MoveType;
import battle.Battle;
import battle.Move;
import battle.effect.ApplyDamageEffect;
import battle.effect.BeforeTurnEffect;
import battle.effect.BracingEffect;
import battle.effect.ChangeAbilityMove;
import battle.effect.ChangeTypeMove;
import battle.effect.CrashDamageMove;
import battle.effect.CritBlockerEffect;
import battle.effect.CritStageEffect;
import battle.effect.DamageBlocker;
import battle.effect.DefiniteEscape;
import battle.effect.Effect.CastSource;
import battle.effect.EffectBlockerEffect;
import battle.effect.EndBattleEffect;
import battle.effect.EndTurnEffect;
import battle.effect.EntryEffect;
import battle.effect.FaintEffect;
import battle.effect.IgnoreStageEffect;
import battle.effect.ItemCondition;
import battle.effect.ModifyStageValueEffect;
import battle.effect.OpponentPowerChangeEffect;
import battle.effect.OpponentTrappingEffect;
import battle.effect.OpposingBeforeTurnEffect;
import battle.effect.PhysicalContactEffect;
import battle.effect.PokemonEffect;
import battle.effect.PowerChangeEffect;
import battle.effect.RecoilMove;
import battle.effect.StageChangingEffect;
import battle.effect.StallingEffect;
import battle.effect.StatChangingEffect;
import battle.effect.StatProtectingEffect;
import battle.effect.Status;
import battle.effect.Status.StatusCondition;
import battle.effect.StatusPreventionEffect;
import battle.effect.SwitchOutEffect;
import battle.effect.TakeDamageEffect;
import battle.effect.Weather;
import battle.effect.WeatherBlockerEffect;

public abstract class Ability implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static HashMap<String, Ability> map; // Mappity map
	private static List<String> abilityNames;
	
	protected Namesies namesies;
	private String description;
	
	public Ability(Namesies s, String desc)
	{
		namesies = s;
		description = desc;
	}
	
	// TODO: Is this correct...?
	protected Ability activate() 
	{
		return this;
	}
	
	public Namesies namesies()
	{
		return this.namesies;
	}
	
	public String getName()
	{
		return namesies.getName();
	}
	
	public String getDescription()
	{
		return description;
	}
	
	// Abilities that block damage
	public static boolean blockDamage(Battle b, ActivePokemon user, ActivePokemon victim)
	{
		if (user.breaksTheMold())
		{
			return false;
		}
		
		Ability a = victim.getAbility();
		if (a instanceof DamageBlocker)
		{
			DamageBlocker blockityBlock = (DamageBlocker)a;
			if (blockityBlock.block(user.getAttack().getType(b, user), victim))
			{
				blockityBlock.alternateEffect(b, victim);
				return true;
			}
		}
		
		return false;
	}
	
	public static Ability assign(PokemonInfo p)
	{
		Namesies[] abilities = p.getAbilities();
		
		if (abilities[0] == Namesies.NONE_ABILITY)
		{
			Global.error("First ability should not be none (Pokemon " + p.getName() + ")");
		}
		
		// Only has one ability -- return the first one
		if (abilities[1] == Namesies.NONE_ABILITY) 
		{
			return getAbility(abilities[0]).newInstance();
		}
		
		// Has two abilties -- return a random one
		return getAbility(Math.random() < .5 ? abilities[0] : abilities[1]).newInstance();
	}
	
	public static Ability evolutionAssign(ActivePokemon p, PokemonInfo ev)
	{
		Namesies prev = p.getAbility().namesies();
		if (ev.hasAbility(prev)) 
		{
			return p.getAbility();
		}
		
		Namesies other = getOtherAbility(p.getPokemonInfo(), prev).namesies();
		if (ev.hasAbility(other)) 
		{
			return getOtherAbility(ev, other);
		}
		
		Namesies[] abilities = ev.getAbilities();
		if (abilities[1] == Namesies.NONE_ABILITY)
		{
			return getAbility(abilities[0]);
		}
		
		return getAbility(abilities[(int)(Math.random()*2)]);
	}
	
	private static Ability getOtherAbility(PokemonInfo p, Namesies ability)
	{
		if (!p.hasAbility(ability)) 
		{
			Global.error("Incorrect ability " + ability + " for " + p.getName() + ".");
		}
		
		Namesies[] abilities = p.getAbilities();
		return getAbility(abilities[0] == ability ? abilities[1] : abilities[0]); 
	}
	
	public abstract Ability newInstance();
	
	public static Ability getAbility(Namesies name)
	{	
		String m = name.getName();
		
		if (map == null)
		{
			loadAbilities();
		}
		
		if (map.containsKey(m))
		{
			return map.get(m);
		}

		Global.error("No such Ability " + m);
		return new None();
	}
	
	// Create and load the Ability map if it doesn't already exist
	public static void loadAbilities() 
	{
		if (map != null) 
			return;
		
		map = new HashMap<>();
		abilityNames = new ArrayList<>();

		// EVERYTHING BELOW IS GENERATED ###

		// List all of the classes we are loading
		map.put("None", new None());
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

		for (String s : map.keySet()) abilityNames.add(s);
	}

	/**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

	private static class None extends Ability 
	{
		private static final long serialVersionUID = 1L;

		public None()
		{
			super(Namesies.NONE_ABILITY, "None");
		}

		public None newInstance()
		{
			return (None)(new None().activate());
		}
	}

	private static class Overgrow extends Ability implements PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public Overgrow()
		{
			super(Namesies.OVERGROW_ABILITY, "Powers up Grass-type moves in a pinch.");
		}

		public Overgrow newInstance()
		{
			return (Overgrow)(new Overgrow().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getHPRatio() < 1/3.0 && user.getAttack().getType(b, user) == Type.GRASS ? 1.5 : 1;
		}
	}

	private static class Chlorophyll extends Ability implements StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public Chlorophyll()
		{
			super(Namesies.CHLOROPHYLL_ABILITY, "Boosts the Pok\u00e9mon’s Speed in sunshine.");
		}

		public Chlorophyll newInstance()
		{
			return (Chlorophyll)(new Chlorophyll().activate());
		}

		public Stat toModify()
		{
			return Stat.SPEED;
		}

		public Namesies getWeatherCondition()
		{
			return Namesies.SUNNY_EFFECT;
		}

		public boolean modifyCondition(Battle b, ActivePokemon p, ActivePokemon opp)
		{
			return b.getWeather().namesies() == getWeatherCondition();
		}

		public double modifyMultiplier()
		{
			return 2;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (s == toModify() && modifyCondition(b, p, opp))
			{
				stat *= modifyMultiplier();
			}
			
			return stat;
		}
	}

	private static class Blaze extends Ability implements PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public Blaze()
		{
			super(Namesies.BLAZE_ABILITY, "Powers up Fire-type moves in a pinch.");
		}

		public Blaze newInstance()
		{
			return (Blaze)(new Blaze().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getHPRatio() < 1/3.0 && user.getAttack().getType(b, user) == Type.FIRE ? 1.5 : 1;
		}
	}

	private static class SolarPower extends Ability implements PowerChangeEffect, EndTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public SolarPower()
		{
			super(Namesies.SOLAR_POWER_ABILITY, "Boosts Sp. Atk, but lowers HP in sunshine.");
		}

		public SolarPower newInstance()
		{
			return (SolarPower)(new SolarPower().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getAttack().getCategory() == Category.SPECIAL && b.getWeather().namesies() == Namesies.SUNNY_EFFECT ? 1.5 : 1;
		}

		public void applyEndTurn(ActivePokemon victim, Battle b)
		{
			if (b.getWeather().namesies() == Namesies.SUNNY_EFFECT)
			{
				b.addMessage(victim.getName() + " lost some of its HP due to its " + this.getName() + "!");
				victim.reduceHealthFraction(b, 1/8.0);
			}
		}
	}

	private static class Torrent extends Ability implements PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public Torrent()
		{
			super(Namesies.TORRENT_ABILITY, "Powers up Water-type moves in a pinch.");
		}

		public Torrent newInstance()
		{
			return (Torrent)(new Torrent().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getHPRatio() < 1/3.0 && user.getAttack().getType(b, user) == Type.WATER ? 1.5 : 1;
		}
	}

	private static class RainDish extends Ability implements EndTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public RainDish()
		{
			super(Namesies.RAIN_DISH_ABILITY, "The Pok\u00e9mon gradually recovers HP in rain.");
		}

		public RainDish newInstance()
		{
			return (RainDish)(new RainDish().activate());
		}

		public void applyEndTurn(ActivePokemon victim, Battle b)
		{
			if (b.getWeather().namesies() == Namesies.RAINING_EFFECT)
			{
				victim.healHealthFraction(1/16.0);
				b.addMessage(victim.getName() + "'s HP was restored due to its " + this.getName() + "!", victim.getHP(), victim.user());
			}
		}
	}

	private static class ShieldDust extends Ability implements EffectBlockerEffect
	{
		private static final long serialVersionUID = 1L;

		public ShieldDust()
		{
			super(Namesies.SHIELD_DUST_ABILITY, "Blocks the added effects of attacks taken.");
		}

		public ShieldDust newInstance()
		{
			return (ShieldDust)(new ShieldDust().activate());
		}

		public boolean validMove(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return !user.getAttack().hasSecondaryEffects();
		}
	}

	private static class ShedSkin extends Ability implements EndTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public ShedSkin()
		{
			super(Namesies.SHED_SKIN_ABILITY, "The Pok\u00e9mon may heal its own status problems.");
		}

		public ShedSkin newInstance()
		{
			return (ShedSkin)(new ShedSkin().activate());
		}

		public void applyEndTurn(ActivePokemon victim, Battle b)
		{
			if (victim.hasStatus() && (int)(Math.random()*3) == 0)
			{
				b.addMessage(victim.getName() + "'s " + this.getName() + " cured it of its status condition!", StatusCondition.NONE, victim.user());
				victim.removeStatus();
			}
		}
	}

	private static class Compoundeyes extends Ability implements StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public Compoundeyes()
		{
			super(Namesies.COMPOUNDEYES_ABILITY, "The Pok\u00e9mon’s accuracy is boosted.");
		}

		public Compoundeyes newInstance()
		{
			return (Compoundeyes)(new Compoundeyes().activate());
		}

		public Stat toModify()
		{
			return Stat.ACCURACY;
		}

		public boolean modifyCondition(Battle b, ActivePokemon p, ActivePokemon opp)
		{
			return true;
		}

		public double modifyMultiplier()
		{
			return 1.3;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (s == toModify() && modifyCondition(b, p, opp))
			{
				stat *= modifyMultiplier();
			}
			
			return stat;
		}
	}

	private static class TintedLens extends Ability implements PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public TintedLens()
		{
			super(Namesies.TINTED_LENS_ABILITY, "Powers up “not very effective” moves.");
		}

		public TintedLens newInstance()
		{
			return (TintedLens)(new TintedLens().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return Type.getAdvantage(user.getAttack().getType(b, user), victim, b) < 1 ? 2 : 1;
		}
	}

	private static class Swarm extends Ability implements PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public Swarm()
		{
			super(Namesies.SWARM_ABILITY, "Powers up Bug-type moves in a pinch.");
		}

		public Swarm newInstance()
		{
			return (Swarm)(new Swarm().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getHPRatio() < 1/3.0 && user.getAttack().getType(b, user) == Type.BUG ? 1.5 : 1;
		}
	}

	private static class Sniper extends Ability 
	{
		private static final long serialVersionUID = 1L;

		public Sniper()
		{
			super(Namesies.SNIPER_ABILITY, "Powers up moves if they become critical hits.");
		}

		public Sniper newInstance()
		{
			return (Sniper)(new Sniper().activate());
		}
	}

	private static class KeenEye extends Ability implements StatProtectingEffect
	{
		private static final long serialVersionUID = 1L;

		public KeenEye()
		{
			super(Namesies.KEEN_EYE_ABILITY, "Prevents the Pok\u00e9mon from losing accuracy.");
		}

		public KeenEye newInstance()
		{
			return (KeenEye)(new KeenEye().activate());
		}

		public boolean prevent(ActivePokemon caster, Stat stat)
		{
			// TODO: Also needs to ignore opponent's evasion boosts
			return stat == Stat.ACCURACY;
		}

		public String preventionMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + this.getName() + " prevents its accuracy from being lowered!";
		}
	}

	private static class TangledFeet extends Ability implements StageChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public TangledFeet()
		{
			super(Namesies.TANGLED_FEET_ABILITY, "Raises evasion if the Pok\u00e9mon is confused.");
		}

		public TangledFeet newInstance()
		{
			return (TangledFeet)(new TangledFeet().activate());
		}

		public int adjustStage(Integer stage, Stat s, ActivePokemon p, ActivePokemon opp, Battle b)
		{
			return s == Stat.EVASION && p.hasEffect(Namesies.CONFUSION_EFFECT) ? stage + 1 : stage;
		}
	}

	private static class Guts extends Ability implements StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public Guts()
		{
			super(Namesies.GUTS_ABILITY, "Boosts Attack if there is a status problem.");
		}

		public Guts newInstance()
		{
			return (Guts)(new Guts().activate());
		}

		public Stat toModify()
		{
			return Stat.ATTACK;
		}

		public boolean modifyCondition(Battle b, ActivePokemon p, ActivePokemon opp)
		{
			return p.hasStatus();
		}

		public double modifyMultiplier()
		{
			return 1.5;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (s == toModify() && modifyCondition(b, p, opp))
			{
				stat *= modifyMultiplier();
			}
			
			return stat;
		}
	}

	private static class Intimidate extends Ability implements EntryEffect
	{
		private static final long serialVersionUID = 1L;

		public Intimidate()
		{
			super(Namesies.INTIMIDATE_ABILITY, "Lowers the foe’s Attack stat.");
		}

		public Intimidate newInstance()
		{
			return (Intimidate)(new Intimidate().activate());
		}

		public void enter(Battle b, ActivePokemon victim)
		{
			ActivePokemon other = b.getOtherPokemon(victim.user());
			other.getAttributes().modifyStage(victim, other, -1, Stat.ATTACK, b, CastSource.ABILITY);
		}
	}

	private static class Static extends Ability implements PhysicalContactEffect
	{
		private static final long serialVersionUID = 1L;

		public Static()
		{
			super(Namesies.STATIC_ABILITY, "Contact with the Pok\u00e9mon may cause paralysis.");
		}

		public Static newInstance()
		{
			return (Static)(new Static().activate());
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (Math.random()*100 < 30)
			{
				Status.giveStatus(b, victim, user, StatusCondition.PARALYZED, true);
			}
		}
	}

	private static class Lightningrod extends Ability implements DamageBlocker
	{
		private static final long serialVersionUID = 1L;

		public Lightningrod()
		{
			super(Namesies.LIGHTNINGROD_ABILITY, "The Pok\u00e9mon draws in all Electric-type moves.");
		}

		public Lightningrod newInstance()
		{
			return (Lightningrod)(new Lightningrod().activate());
		}

		public Type getType()
		{
			return Type.ELECTRIC;
		}

		public Stat toIncrease()
		{
			return Stat.SP_ATTACK;
		}

		public boolean block(Type attacking, ActivePokemon victim)
		{
			return attacking == getType();
		}

		public void alternateEffect(Battle b, ActivePokemon victim)
		{
			b.addMessage(victim.getName() + "'s " + this.getName() + " makes it immune to " + getType().getName() + " type moves!");
			victim.getAttributes().modifyStage(victim, victim, 1, toIncrease(), b, CastSource.ABILITY);
		}
	}

	private static class SandVeil extends Ability implements StageChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public SandVeil()
		{
			super(Namesies.SAND_VEIL_ABILITY, "Raises the Pok\u00e9mon’s evasion during a sandstorm by one level.");
		}

		public SandVeil newInstance()
		{
			return (SandVeil)(new SandVeil().activate());
		}

		public int adjustStage(Integer stage, Stat s, ActivePokemon p, ActivePokemon opp, Battle b)
		{
			return s == Stat.EVASION && b.getWeather().namesies() == Namesies.SANDSTORM_EFFECT ? stage + 1 : stage;
		}
	}

	private static class SandRush extends Ability implements StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public SandRush()
		{
			super(Namesies.SAND_RUSH_ABILITY, "Speed rises in a Sandstorm.");
		}

		public SandRush newInstance()
		{
			return (SandRush)(new SandRush().activate());
		}

		public Stat toModify()
		{
			return Stat.SPEED;
		}

		public Namesies getWeatherCondition()
		{
			return Namesies.SANDSTORM_EFFECT;
		}

		public boolean modifyCondition(Battle b, ActivePokemon p, ActivePokemon opp)
		{
			return b.getWeather().namesies() == getWeatherCondition();
		}

		public double modifyMultiplier()
		{
			return 2;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (s == toModify() && modifyCondition(b, p, opp))
			{
				stat *= modifyMultiplier();
			}
			
			return stat;
		}
	}

	private static class PoisonPoint extends Ability implements PhysicalContactEffect
	{
		private static final long serialVersionUID = 1L;

		public PoisonPoint()
		{
			super(Namesies.POISON_POINT_ABILITY, "Contact with the Pok\u00e9mon may poison the foe.");
		}

		public PoisonPoint newInstance()
		{
			return (PoisonPoint)(new PoisonPoint().activate());
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (Math.random()*100 < 30)
			{
				Status.giveStatus(b, victim, user, StatusCondition.POISONED, true);
			}
		}
	}

	private static class Rivalry extends Ability implements PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public Rivalry()
		{
			super(Namesies.RIVALRY_ABILITY, "Raises Attack if the foe is of the same gender.");
		}

		public Rivalry newInstance()
		{
			return (Rivalry)(new Rivalry().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getGender() == Gender.GENDERLESS) return 1;
			if (Gender.oppositeGenders(user, victim)) return .75;
			if (user.getGender() == victim.getGender()) return 1.25;
			return 1;
		}
	}

	private static class CuteCharm extends Ability implements PhysicalContactEffect
	{
		private static final long serialVersionUID = 1L;

		public CuteCharm()
		{
			super(Namesies.CUTE_CHARM_ABILITY, "Contact with the Pok\u00e9mon may cause infatuation.");
		}

		public CuteCharm newInstance()
		{
			return (CuteCharm)(new CuteCharm().activate());
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (Math.random()*100 < 30)
			{
				PokemonEffect e = PokemonEffect.getEffect(Namesies.INFATUATED_EFFECT);
				if (e.applies(b, victim, user, CastSource.ABILITY))
				{
					user.addEffect(e.newInstance());
					b.addMessage(victim.getName() + "'s " + this.getName() + " infatuated " + user.getName() + "!");
				}
			}
		}
	}

	private static class MagicGuard extends Ability implements WeatherBlockerEffect
	{
		private static final long serialVersionUID = 1L;

		public MagicGuard()
		{
			super(Namesies.MAGIC_GUARD_ABILITY, "The Pok\u00e9mon only takes damage from attacks.");
		}

		public MagicGuard newInstance()
		{
			return (MagicGuard)(new MagicGuard().activate());
		}

		public boolean block(Namesies weather)
		{
			return true;
		}
	}

	private static class FlashFire extends Ability implements DamageBlocker, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;
		private boolean activated;

		public FlashFire()
		{
			super(Namesies.FLASH_FIRE_ABILITY, "Powers up Fire-type moves if hit by a fire move.");
		}

		public FlashFire newInstance()
		{
			FlashFire x = (FlashFire)(new FlashFire().activate());
			x.activated = false;
			return x;
		}

		public boolean block(Type attacking, ActivePokemon victim)
		{
			return attacking == Type.FIRE;
		}

		public void alternateEffect(Battle b, ActivePokemon victim)
		{
			b.addMessage(victim.getName() + "'s " + this.getName() + " makes it immune to Fire type moves!");
			activated = true;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return activated && user.getAttack().getType(b, user) == Type.FIRE ? 1.5 : 1;
		}
	}

	private static class Drought extends Ability implements EntryEffect
	{
		private static final long serialVersionUID = 1L;

		public Drought()
		{
			super(Namesies.DROUGHT_ABILITY, "The Pok\u00e9mon makes it sunny if it is in battle.");
		}

		public Drought newInstance()
		{
			return (Drought)(new Drought().activate());
		}

		public void enter(Battle b, ActivePokemon victim)
		{
			b.addEffect(Weather.getEffect(Namesies.SUNNY_EFFECT).newInstance());
			b.addMessage(victim.getName() + "'s " + this.getName() + " made the sunlight turn harsh!");
		}
	}

	private static class Frisk extends Ability implements EntryEffect
	{
		private static final long serialVersionUID = 1L;

		public Frisk()
		{
			super(Namesies.FRISK_ABILITY, "The Pok\u00e9mon can check the foe’s held item.");
		}

		public Frisk newInstance()
		{
			return (Frisk)(new Frisk().activate());
		}

		public void enter(Battle b, ActivePokemon victim)
		{
			ActivePokemon other = b.getOtherPokemon(victim.user());
			if (other.isHoldingItem(b)) b.addMessage(victim.getName() + "'s " + this.getName() + " alerted it to " + other.getName() + "'s " + other.getHeldItem(b).getName() + "!");
		}
	}

	private static class InnerFocus extends Ability 
	{
		private static final long serialVersionUID = 1L;

		public InnerFocus()
		{
			super(Namesies.INNER_FOCUS_ABILITY, "The Pok\u00e9mon is protected from flinching.");
		}

		public InnerFocus newInstance()
		{
			return (InnerFocus)(new InnerFocus().activate());
		}
	}

	private static class Infiltrator extends Ability 
	{
		private static final long serialVersionUID = 1L;

		public Infiltrator()
		{
			super(Namesies.INFILTRATOR_ABILITY, "You slip through the opponents walls and attack.");
		}

		public Infiltrator newInstance()
		{
			return (Infiltrator)(new Infiltrator().activate());
		}
	}

	private static class Stench extends Ability implements ApplyDamageEffect
	{
		private static final long serialVersionUID = 1L;

		public Stench()
		{
			super(Namesies.STENCH_ABILITY, "The stench may cause the target to flinch.");
		}

		public Stench newInstance()
		{
			return (Stench)(new Stench().activate());
		}

		public void applyEffect(Battle b, ActivePokemon user, ActivePokemon victim, Integer damage)
		{
			if (Math.random()*100 < 10)
			{
				PokemonEffect flinch = PokemonEffect.getEffect(Namesies.FLINCH_EFFECT);
				if (flinch.applies(b, user, victim, CastSource.ABILITY))
				{
					flinch.cast(b, user, victim, CastSource.ABILITY, false);
					b.addMessage(user.getName() + "'s " + this.getName() + " caused " + victim.getName() + " to flinch!");
				}
			}
		}
	}

	private static class EffectSpore extends Ability implements PhysicalContactEffect
	{
		private static final long serialVersionUID = 1L;
		private static StatusCondition[] statuses = new StatusCondition[] {StatusCondition.PARALYZED, StatusCondition.POISONED, StatusCondition.ASLEEP};

		public EffectSpore()
		{
			super(Namesies.EFFECT_SPORE_ABILITY, "Contact may paralyze, poison, or cause sleep.");
		}

		public EffectSpore newInstance()
		{
			return (EffectSpore)(new EffectSpore().activate());
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (Math.random()*100 < 30)
			{
				Status.giveStatus(b, victim, user, statuses[(int)(Math.random()*statuses.length)], true);
			}
		}
	}

	private static class DrySkin extends Ability implements DamageBlocker, EndTurnEffect, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public DrySkin()
		{
			super(Namesies.DRY_SKIN_ABILITY, "Reduces HP if it is hot. Water restores HP.");
		}

		public DrySkin newInstance()
		{
			return (DrySkin)(new DrySkin().activate());
		}

		public Type getType()
		{
			return Type.WATER;
		}

		public boolean block(Type attacking, ActivePokemon victim)
		{
			return attacking == getType() && !victim.hasEffect(Namesies.HEAL_BLOCK_EFFECT);
		}

		public void alternateEffect(Battle b, ActivePokemon victim)
		{
			// TODO: Srsly considering changing the heal block to be here instead -- like I don't think they should take damage when they have heal block it makes no sense
			victim.healHealthFraction(1/4.0);
			b.addMessage(victim.getName() + "'s HP was restored due to its " + this.getName() + "!", victim.getHP(), victim.user());
		}

		public void applyEndTurn(ActivePokemon victim, Battle b)
		{
			if (b.getWeather().namesies() == Namesies.SUNNY_EFFECT)
			{
				b.addMessage(victim.getName() + " lost some of its HP due to its " + this.getName() + "!");
				victim.reduceHealthFraction(b, 1/8.0);
			}
			else if (b.getWeather().namesies() == Namesies.RAINING_EFFECT && !victim.fullHealth())
			{
				victim.healHealthFraction(1/8.0);
				b.addMessage(victim.getName() + "'s HP was restored due to its " + this.getName() + "!", victim.getHP(), victim.user());
			}
		}

		public double getOppMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getAttack().getType(b, user) == Type.FIRE ? 1.25 : 1;
		}
	}

	private static class ArenaTrap extends Ability implements OpponentTrappingEffect
	{
		private static final long serialVersionUID = 1L;

		public ArenaTrap()
		{
			super(Namesies.ARENA_TRAP_ABILITY, "Prevents the foe from fleeing.");
		}

		public ArenaTrap newInstance()
		{
			return (ArenaTrap)(new ArenaTrap().activate());
		}

		public boolean isTrapped(Battle b, ActivePokemon p)
		{
			return !p.isLevitating(b);
		}

		public String trappingMessage(ActivePokemon escaper, ActivePokemon trapper)
		{
			return trapper.getName() + "'s " + this.getName() + " prevents " + escaper.getName() + " from escaping!";
		}
	}

	private static class Technician extends Ability implements PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public Technician()
		{
			super(Namesies.TECHNICIAN_ABILITY, "Powers up the Pok\u00e9mon’s weaker moves.");
		}

		public Technician newInstance()
		{
			return (Technician)(new Technician().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getAttack().getPower(b, user, victim) <= 60 ? 1.5 : 1;
		}
	}

	private static class Limber extends Ability implements StatusPreventionEffect
	{
		private static final long serialVersionUID = 1L;

		public Limber()
		{
			super(Namesies.LIMBER_ABILITY, "The Pok\u00e9mon is protected from paralysis.");
		}

		public Limber newInstance()
		{
			return (Limber)(new Limber().activate());
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status)
		{
			return status == StatusCondition.PARALYZED;
		}

		public String preventionMessage(ActivePokemon victim)
		{
			return victim.getName() + "'s Limber prevents paralysis!";
		}
	}

	private static class Damp extends Ability implements BeforeTurnEffect, OpposingBeforeTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public Damp()
		{
			super(Namesies.DAMP_ABILITY, "Prevents combatants from self destructing.");
		}

		public Damp newInstance()
		{
			return (Damp)(new Damp().activate());
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b)
		{
			if (p.getAttack().namesies() == Namesies.SELF_DESTRUCT_ATTACK || p.getAttack().namesies() == Namesies.EXPLOSION_ATTACK)
			{
				b.printAttacking(p);
				b.addMessage(p.getName() + "'s " + this.getName() + " prevents " + p.getAttack().getName() + " from being used!");
				return false;
			}
			return true;
		}

		public boolean opposingCanAttack(ActivePokemon p, ActivePokemon opp, Battle b)
		{
			if (p.getAttack().namesies() == Namesies.SELF_DESTRUCT_ATTACK || p.getAttack().namesies() == Namesies.EXPLOSION_ATTACK)
			{
				b.printAttacking(p);
				b.addMessage(opp.getName() + "'s " + this.getName() + " prevents " + p.getAttack().getName() + " from being used!");
				return false;
			}
			return true;
		}
	}

	private static class CloudNine extends Ability implements EntryEffect
	{
		private static final long serialVersionUID = 1L;

		public CloudNine()
		{
			super(Namesies.CLOUD_NINE_ABILITY, "Eliminates the effects of weather.");
		}

		public CloudNine newInstance()
		{
			return (CloudNine)(new CloudNine().activate());
		}

		public void enter(Battle b, ActivePokemon victim)
		{
			b.addEffect(Weather.getEffect(Namesies.CLEAR_SKIES_EFFECT));
			b.addMessage(victim.getName() + "'s " + this.getName() + " eliminated the weather!");
		}
	}

	private static class VitalSpirit extends Ability implements StatusPreventionEffect
	{
		private static final long serialVersionUID = 1L;

		public VitalSpirit()
		{
			super(Namesies.VITAL_SPIRIT_ABILITY, "Prevents the Pok\u00e9mon from falling asleep.");
		}

		public VitalSpirit newInstance()
		{
			return (VitalSpirit)(new VitalSpirit().activate());
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status)
		{
			return status == StatusCondition.ASLEEP;
		}

		public String preventionMessage(ActivePokemon victim)
		{
			return victim.getName() + "'s Vital Spirit prevents sleep!";
		}
	}

	private static class Insomnia extends Ability implements StatusPreventionEffect
	{
		private static final long serialVersionUID = 1L;

		public Insomnia()
		{
			super(Namesies.INSOMNIA_ABILITY, "Prevents the Pok\u00e9mon from falling asleep.");
		}

		public Insomnia newInstance()
		{
			return (Insomnia)(new Insomnia().activate());
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status)
		{
			return status == StatusCondition.ASLEEP;
		}

		public String preventionMessage(ActivePokemon victim)
		{
			return victim.getName() + "'s Insomnia prevents sleep!";
		}
	}

	private static class AngerPoint extends Ability 
	{
		private static final long serialVersionUID = 1L;

		public AngerPoint()
		{
			super(Namesies.ANGER_POINT_ABILITY, "Raises Attack upon taking a critical hit.");
		}

		public AngerPoint newInstance()
		{
			return (AngerPoint)(new AngerPoint().activate());
		}
	}

	private static class Synchronize extends Ability 
	{
		private static final long serialVersionUID = 1L;

		public Synchronize()
		{
			super(Namesies.SYNCHRONIZE_ABILITY, "Passes on a burn, poison, or paralysis to the foe.");
		}

		public Synchronize newInstance()
		{
			return (Synchronize)(new Synchronize().activate());
		}
	}

	private static class NoGuard extends Ability 
	{
		private static final long serialVersionUID = 1L;

		public NoGuard()
		{
			super(Namesies.NO_GUARD_ABILITY, "Ensures the Pok\u00e9mon and its foe’s attacks land.");
		}

		public NoGuard newInstance()
		{
			return (NoGuard)(new NoGuard().activate());
		}
	}

	private static class OwnTempo extends Ability 
	{
		private static final long serialVersionUID = 1L;

		public OwnTempo()
		{
			super(Namesies.OWN_TEMPO_ABILITY, "Prevents the Pok\u00e9mon from becoming confused.");
		}

		public OwnTempo newInstance()
		{
			return (OwnTempo)(new OwnTempo().activate());
		}
	}

	private static class ClearBody extends Ability implements StatProtectingEffect
	{
		private static final long serialVersionUID = 1L;

		public ClearBody()
		{
			super(Namesies.CLEAR_BODY_ABILITY, "Prevents the Pok\u00e9mon’s stats from being lowered.");
		}

		public ClearBody newInstance()
		{
			return (ClearBody)(new ClearBody().activate());
		}

		public boolean prevent(ActivePokemon caster, Stat stat)
		{
			return true;
		}

		public String preventionMessage(ActivePokemon p)
		{
			return p.getName() + "'s Clear Body prevents its stats from being lowered!";
		}
	}

	private static class LiquidOoze extends Ability 
	{
		private static final long serialVersionUID = 1L;

		public LiquidOoze()
		{
			super(Namesies.LIQUID_OOZE_ABILITY, "Inflicts damage on foes using any draining move.");
		}

		public LiquidOoze newInstance()
		{
			return (LiquidOoze)(new LiquidOoze().activate());
		}
	}

	private static class RockHead extends Ability 
	{
		private static final long serialVersionUID = 1L;

		public RockHead()
		{
			super(Namesies.ROCK_HEAD_ABILITY, "Protects the Pok\u00e9mon from recoil damage.");
		}

		public RockHead newInstance()
		{
			return (RockHead)(new RockHead().activate());
		}
	}

	private static class Sturdy extends Ability implements BracingEffect
	{
		private static final long serialVersionUID = 1L;

		public Sturdy()
		{
			super(Namesies.STURDY_ABILITY, "The Pok\u00e9mon is protected against 1-hit KO attacks.");
		}

		public Sturdy newInstance()
		{
			return (Sturdy)(new Sturdy().activate());
		}

		public boolean isBracing(Battle b, ActivePokemon bracer, Boolean fullHealth)
		{
			return fullHealth;
		}

		public String braceMessage(ActivePokemon bracer)
		{
			return bracer.getName() + "'s Sturdy endured the hit!";
		}
	}

	private static class Oblivious extends Ability 
	{
		private static final long serialVersionUID = 1L;

		public Oblivious()
		{
			super(Namesies.OBLIVIOUS_ABILITY, "Prevents the Pok\u00e9mon from becoming infatuated.");
		}

		public Oblivious newInstance()
		{
			return (Oblivious)(new Oblivious().activate());
		}
	}

	private static class MagnetPull extends Ability implements OpponentTrappingEffect
	{
		private static final long serialVersionUID = 1L;

		public MagnetPull()
		{
			super(Namesies.MAGNET_PULL_ABILITY, "Prevents Steel-type Pok\u00e9mon from escaping.");
		}

		public MagnetPull newInstance()
		{
			return (MagnetPull)(new MagnetPull().activate());
		}

		public boolean isTrapped(Battle b, ActivePokemon p)
		{
			return p.isType(b, Type.STEEL);
		}

		public String trappingMessage(ActivePokemon escaper, ActivePokemon trapper)
		{
			return trapper.getName() + "'s " + this.getName() + " prevents " + escaper.getName() + " from escaping!";
		}
	}

	private static class Unaware extends Ability implements IgnoreStageEffect
	{
		private static final long serialVersionUID = 1L;

		public Unaware()
		{
			super(Namesies.UNAWARE_ABILITY, "Ignores any change in ability by the foe.");
		}

		public Unaware newInstance()
		{
			return (Unaware)(new Unaware().activate());
		}

		public boolean ignoreStage(Stat s)
		{
			return s == Stat.ATTACK || s == Stat.SP_ATTACK || s == Stat.DEFENSE || s == Stat.SP_DEFENSE;
		}
	}

	private static class Simple extends Ability implements ModifyStageValueEffect
	{
		private static final long serialVersionUID = 1L;

		public Simple()
		{
			super(Namesies.SIMPLE_ABILITY, "The Pok\u00e9mon is prone to wild stat changes.");
		}

		public Simple newInstance()
		{
			return (Simple)(new Simple().activate());
		}

		public int modifyStageValue(int modVal)
		{
			return modVal*2;
		}
	}

	private static class EarlyBird extends Ability 
	{
		private static final long serialVersionUID = 1L;

		public EarlyBird()
		{
			super(Namesies.EARLY_BIRD_ABILITY, "The Pok\u00e9mon awakens quickly from sleep.");
		}

		public EarlyBird newInstance()
		{
			return (EarlyBird)(new EarlyBird().activate());
		}
	}

	private static class ThickFat extends Ability implements OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public ThickFat()
		{
			super(Namesies.THICK_FAT_ABILITY, "Raises resistance to Fire-and Ice-type moves.");
		}

		public ThickFat newInstance()
		{
			return (ThickFat)(new ThickFat().activate());
		}

		public double getOppMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getAttack().getType(b, user) == Type.FIRE || user.getAttack().getType(b, user) == Type.ICE ? .5 : 1;
		}
	}

	private static class Hydration extends Ability implements EndTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public Hydration()
		{
			super(Namesies.HYDRATION_ABILITY, "Heals status problems if it is raining.");
		}

		public Hydration newInstance()
		{
			return (Hydration)(new Hydration().activate());
		}

		public void applyEndTurn(ActivePokemon victim, Battle b)
		{
			if (b.getWeather().namesies() == Namesies.RAINING_EFFECT && victim.hasStatus())
			{
				victim.removeStatus();
				b.addMessage(victim.getName() + "'s " + this.getName() + " cured it of its status condition!", StatusCondition.NONE, victim.user());
			}
		}
	}

	private static class ShellArmor extends Ability implements CritBlockerEffect
	{
		private static final long serialVersionUID = 1L;

		public ShellArmor()
		{
			super(Namesies.SHELL_ARMOR_ABILITY, "The Pok\u00e9mon is protected against critical hits.");
		}

		public ShellArmor newInstance()
		{
			return (ShellArmor)(new ShellArmor().activate());
		}

		public boolean blockCrits()
		{
			return true;
		}
	}

	private static class BattleArmor extends Ability implements CritBlockerEffect
	{
		private static final long serialVersionUID = 1L;

		public BattleArmor()
		{
			super(Namesies.BATTLE_ARMOR_ABILITY, "The Pok\u00e9mon is protected against critical hits.");
		}

		public BattleArmor newInstance()
		{
			return (BattleArmor)(new BattleArmor().activate());
		}

		public boolean blockCrits()
		{
			return true;
		}
	}

	private static class SkillLink extends Ability 
	{
		private static final long serialVersionUID = 1L;

		public SkillLink()
		{
			super(Namesies.SKILL_LINK_ABILITY, "Increases the frequency of multi-strike moves.");
		}

		public SkillLink newInstance()
		{
			return (SkillLink)(new SkillLink().activate());
		}
	}

	private static class Levitate extends Ability 
	{
		private static final long serialVersionUID = 1L;

		public Levitate()
		{
			super(Namesies.LEVITATE_ABILITY, "Gives full immunity to all Ground-type moves.");
		}

		public Levitate newInstance()
		{
			return (Levitate)(new Levitate().activate());
		}
	}

	private static class Forewarn extends Ability implements EntryEffect
	{
		private static final long serialVersionUID = 1L;

		public Forewarn()
		{
			super(Namesies.FOREWARN_ABILITY, "Determines what moves the foe has.");
		}

		public Forewarn newInstance()
		{
			return (Forewarn)(new Forewarn().activate());
		}

		public void enter(Battle b, ActivePokemon victim)
		{
			ActivePokemon other = b.getOtherPokemon(victim.user());
			List<Namesies> besties = new ArrayList<>();
			
			int highestPower = -1;
			
			for (Move m : other.getMoves())
			{
				if (m.getAttack().getCategory() == Category.STATUS)
				{
					continue;
				}
				
				int power = m.getAttack().getPower(b, other, victim);
				if (power > highestPower)
				{
					highestPower = power;
					besties = new ArrayList<>();
					besties.add(m.getAttack().namesies());
				}
				else if (power == highestPower)
				{
					besties.add(m.getAttack().namesies());
				}
			}
			
			Namesies warn;
			if (highestPower == -1)
			{
				warn = other.getMoves().get((int)(Math.random()*other.getMoves().size())).getAttack().namesies();
			}
			else
			{
				warn = besties.get((int)(Math.random()*besties.size()));
			}
			
			b.addMessage(victim.getName() + "'s " + this.getName() + " alerted it to " + other.getName() + "'s " + warn.getName() + "!");
		}
	}

	private static class HyperCutter extends Ability implements StatProtectingEffect
	{
		private static final long serialVersionUID = 1L;

		public HyperCutter()
		{
			super(Namesies.HYPER_CUTTER_ABILITY, "Prevents the Attack stat from being lowered.");
		}

		public HyperCutter newInstance()
		{
			return (HyperCutter)(new HyperCutter().activate());
		}

		public boolean prevent(ActivePokemon caster, Stat stat)
		{
			return stat == Stat.ATTACK;
		}

		public String preventionMessage(ActivePokemon p)
		{
			return p.getName() + "'s Hyper Cutter prevents its attack from being lowered!";
		}
	}

	private static class Soundproof extends Ability implements OpposingBeforeTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public Soundproof()
		{
			super(Namesies.SOUNDPROOF_ABILITY, "Gives full immunity to all sound-based moves.");
		}

		public Soundproof newInstance()
		{
			return (Soundproof)(new Soundproof().activate());
		}

		public boolean opposingCanAttack(ActivePokemon p, ActivePokemon opp, Battle b)
		{
			if (p.getAttack().isMoveType(MoveType.SOUND_BASED))
			{
				b.printAttacking(p);
				b.addMessage(opp.getName() + "'s " + this.getName() + " makes it immune to sound based moves!");
				return false;
			}
			return true;
		}
	}

	private static class Reckless extends Ability implements PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public Reckless()
		{
			super(Namesies.RECKLESS_ABILITY, "Powers up moves that have recoil damage.");
		}

		public Reckless newInstance()
		{
			return (Reckless)(new Reckless().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getAttack() instanceof RecoilMove || user.getAttack() instanceof CrashDamageMove ? 1.2 : 1;
		}
	}

	private static class IronFist extends Ability implements PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public IronFist()
		{
			super(Namesies.IRON_FIST_ABILITY, "Boosts the power of punching moves.");
		}

		public IronFist newInstance()
		{
			return (IronFist)(new IronFist().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getAttack().isMoveType(MoveType.PUNCHING) ? 1.2 : 1;
		}
	}

	private static class NaturalCure extends Ability implements SwitchOutEffect
	{
		private static final long serialVersionUID = 1L;

		public NaturalCure()
		{
			super(Namesies.NATURAL_CURE_ABILITY, "All status problems are healed upon switching out.");
		}

		public NaturalCure newInstance()
		{
			return (NaturalCure)(new NaturalCure().activate());
		}

		public void switchOut(ActivePokemon switchee)
		{
			if (!switchee.hasStatus(StatusCondition.FAINTED)) switchee.removeStatus();
		}
	}

	private static class SereneGrace extends Ability 
	{
		private static final long serialVersionUID = 1L;

		public SereneGrace()
		{
			super(Namesies.SERENE_GRACE_ABILITY, "Boosts the likelihood of added effects appearing.");
		}

		public SereneGrace newInstance()
		{
			return (SereneGrace)(new SereneGrace().activate());
		}
	}

	private static class LeafGuard extends Ability implements StatusPreventionEffect
	{
		private static final long serialVersionUID = 1L;

		public LeafGuard()
		{
			super(Namesies.LEAF_GUARD_ABILITY, "Prevents status problems in sunny weather.");
		}

		public LeafGuard newInstance()
		{
			return (LeafGuard)(new LeafGuard().activate());
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status)
		{
			return b.getWeather().namesies() == Namesies.SUNNY_EFFECT;
		}

		public String preventionMessage(ActivePokemon victim)
		{
			return victim.getName() + "'s Leaf Guard prevents status conditions!";
		}
	}

	private static class Scrappy extends Ability 
	{
		private static final long serialVersionUID = 1L;

		public Scrappy()
		{
			super(Namesies.SCRAPPY_ABILITY, "Enables moves to hit Ghost-type foes.");
		}

		public Scrappy newInstance()
		{
			return (Scrappy)(new Scrappy().activate());
		}
	}

	private static class SwiftSwim extends Ability implements StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public SwiftSwim()
		{
			super(Namesies.SWIFT_SWIM_ABILITY, "Boosts the Pok\u00e9mon’s Speed in rain.");
		}

		public SwiftSwim newInstance()
		{
			return (SwiftSwim)(new SwiftSwim().activate());
		}

		public Stat toModify()
		{
			return Stat.SPEED;
		}

		public Namesies getWeatherCondition()
		{
			return Namesies.RAINING_EFFECT;
		}

		public boolean modifyCondition(Battle b, ActivePokemon p, ActivePokemon opp)
		{
			return b.getWeather().namesies() == getWeatherCondition();
		}

		public double modifyMultiplier()
		{
			return 2;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (s == toModify() && modifyCondition(b, p, opp))
			{
				stat *= modifyMultiplier();
			}
			
			return stat;
		}
	}

	private static class WaterVeil extends Ability implements StatusPreventionEffect
	{
		private static final long serialVersionUID = 1L;

		public WaterVeil()
		{
			super(Namesies.WATER_VEIL_ABILITY, "Prevents the Pok\u00e9mon from getting a burn.");
		}

		public WaterVeil newInstance()
		{
			return (WaterVeil)(new WaterVeil().activate());
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status)
		{
			return status == StatusCondition.BURNED;
		}

		public String preventionMessage(ActivePokemon victim)
		{
			return victim.getName() + "'s Water Veil prevents burns!";
		}
	}

	private static class Filter extends Ability implements OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public Filter()
		{
			super(Namesies.FILTER_ABILITY, "Powers down super-effective moves.");
		}

		public Filter newInstance()
		{
			return (Filter)(new Filter().activate());
		}

		public double getOppMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return Type.getAdvantage(user.getAttack().getType(b, user), victim, b) > 1 ? .75 : 1;
		}
	}

	private static class FlameBody extends Ability implements PhysicalContactEffect
	{
		private static final long serialVersionUID = 1L;

		public FlameBody()
		{
			super(Namesies.FLAME_BODY_ABILITY, "Contact with the Pok\u00e9mon may burn the foe.");
		}

		public FlameBody newInstance()
		{
			return (FlameBody)(new FlameBody().activate());
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (Math.random()*100 < 30)
			{
				Status.giveStatus(b, victim, user, StatusCondition.BURNED, true);
			}
		}
	}

	private static class Rattled extends Ability implements TakeDamageEffect
	{
		private static final long serialVersionUID = 1L;

		public Rattled()
		{
			super(Namesies.RATTLED_ABILITY, "Some move types scare it and boost its Speed.");
		}

		public Rattled newInstance()
		{
			return (Rattled)(new Rattled().activate());
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Type type = user.getAttack().getType(b, user);
			if (type == Type.BUG || type == Type.DARK || type == Type.GHOST)
			{
				victim.getAttributes().modifyStage(victim, victim, 1, Stat.SPEED, b, CastSource.ABILITY);
			}
		}
	}

	private static class Moxie extends Ability implements FaintEffect
	{
		private static final long serialVersionUID = 1L;

		public Moxie()
		{
			super(Namesies.MOXIE_ABILITY, "Attack rises when you knock out an opponent.");
		}

		public Moxie newInstance()
		{
			return (Moxie)(new Moxie().activate());
		}

		public void deathwish(Battle b, ActivePokemon dead, ActivePokemon murderer)
		{
			murderer.getAttributes().modifyStage(murderer, murderer, 1, Stat.ATTACK, b, CastSource.ABILITY);
		}
	}

	private static class Imposter extends Ability implements EntryEffect
	{
		private static final long serialVersionUID = 1L;

		public Imposter()
		{
			super(Namesies.IMPOSTER_ABILITY, "It transforms itself into the Pok\u00e9mon it is facing.");
		}

		public Imposter newInstance()
		{
			return (Imposter)(new Imposter().activate());
		}

		public void enter(Battle b, ActivePokemon victim)
		{
			PokemonEffect.getEffect(Namesies.TRANSFORMED_EFFECT).cast(b, victim, victim, CastSource.ABILITY, false);
		}
	}

	private static class Adaptability extends Ability 
	{
		private static final long serialVersionUID = 1L;

		public Adaptability()
		{
			super(Namesies.ADAPTABILITY_ABILITY, "Powers up moves of the same type.");
		}

		public Adaptability newInstance()
		{
			return (Adaptability)(new Adaptability().activate());
		}
	}

	private static class WaterAbsorb extends Ability implements DamageBlocker
	{
		private static final long serialVersionUID = 1L;

		public WaterAbsorb()
		{
			super(Namesies.WATER_ABSORB_ABILITY, "Restores HP if hit by a Water-type move.");
		}

		public WaterAbsorb newInstance()
		{
			return (WaterAbsorb)(new WaterAbsorb().activate());
		}

		public Type getType()
		{
			return Type.WATER;
		}

		public boolean block(Type attacking, ActivePokemon victim)
		{
			return attacking == getType() && !victim.hasEffect(Namesies.HEAL_BLOCK_EFFECT);
		}

		public void alternateEffect(Battle b, ActivePokemon victim)
		{
			// TODO: Srsly considering changing the heal block to be here instead -- like I don't think they should take damage when they have heal block it makes no sense
			victim.healHealthFraction(1/4.0);
			b.addMessage(victim.getName() + "'s HP was restored due to its " + this.getName() + "!", victim.getHP(), victim.user());
		}
	}

	private static class VoltAbsorb extends Ability implements DamageBlocker
	{
		private static final long serialVersionUID = 1L;

		public VoltAbsorb()
		{
			super(Namesies.VOLT_ABSORB_ABILITY, "Restores HP if hit by an Electric-type move.");
		}

		public VoltAbsorb newInstance()
		{
			return (VoltAbsorb)(new VoltAbsorb().activate());
		}

		public Type getType()
		{
			return Type.ELECTRIC;
		}

		public boolean block(Type attacking, ActivePokemon victim)
		{
			return attacking == getType() && !victim.hasEffect(Namesies.HEAL_BLOCK_EFFECT);
		}

		public void alternateEffect(Battle b, ActivePokemon victim)
		{
			// TODO: Srsly considering changing the heal block to be here instead -- like I don't think they should take damage when they have heal block it makes no sense
			victim.healHealthFraction(1/4.0);
			b.addMessage(victim.getName() + "'s HP was restored due to its " + this.getName() + "!", victim.getHP(), victim.user());
		}
	}

	private static class QuickFeet extends Ability implements StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public QuickFeet()
		{
			super(Namesies.QUICK_FEET_ABILITY, "Boosts Speed if there is a status problem.");
		}

		public QuickFeet newInstance()
		{
			return (QuickFeet)(new QuickFeet().activate());
		}

		public Stat toModify()
		{
			return Stat.SPEED;
		}

		public boolean modifyCondition(Battle b, ActivePokemon p, ActivePokemon opp)
		{
			return p.hasStatus();
		}

		public double modifyMultiplier()
		{
			return 1.5;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (s == toModify() && modifyCondition(b, p, opp))
			{
				stat *= modifyMultiplier();
			}
			
			return stat;
		}
	}

	private static class Trace extends Ability implements EntryEffect, ChangeAbilityMove
	{
		private static final long serialVersionUID = 1L;

		public Trace()
		{
			super(Namesies.TRACE_ABILITY, "The Pok\u00e9mon copies the foe’s ability.");
		}

		public Trace newInstance()
		{
			return (Trace)(new Trace().activate());
		}

		public void enter(Battle b, ActivePokemon victim)
		{
			ActivePokemon other = b.getOtherPokemon(victim.user());
			if (other.hasAbility(Namesies.MULTITYPE_ABILITY) || other.hasAbility(Namesies.ILLUSION_ABILITY) || other.hasAbility(this.namesies))
			{
				return;
			}
			
			PokemonEffect.getEffect(Namesies.CHANGE_ABILITY_EFFECT).cast(b, victim, victim, CastSource.ABILITY, true);
		}

		public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return b.getOtherPokemon(victim.user()).getAbility().newInstance();
		}

		public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			ActivePokemon other = b.getOtherPokemon(victim.user());
			return victim.getName() + " traced " + other.getName() + "'s " + other.getAbility().getName() + "!";
		}
	}

	private static class Download extends Ability implements EntryEffect
	{
		private static final long serialVersionUID = 1L;

		public Download()
		{
			super(Namesies.DOWNLOAD_ABILITY, "Adjusts power according to a foe's defenses.");
		}

		public Download newInstance()
		{
			return (Download)(new Download().activate());
		}

		public void enter(Battle b, ActivePokemon victim)
		{
			ActivePokemon other = b.getOtherPokemon(victim.user());
			
			// TODO: I don't think this should take the current battle effects into account -- just the straight up base stats of the opponent
			if (Stat.getStat(Stat.DEFENSE, other, victim, b) < Stat.getStat(Stat.SP_DEFENSE, other, victim, b))
			{
				victim.getAttributes().modifyStage(victim, victim, 1, Stat.ATTACK, b, CastSource.ABILITY);
			}
			else
			{
				victim.getAttributes().modifyStage(victim, victim, 1, Stat.SP_ATTACK, b, CastSource.ABILITY);
			}
		}
	}

	private static class Pressure extends Ability implements EntryEffect
	{
		private static final long serialVersionUID = 1L;

		public Pressure()
		{
			super(Namesies.PRESSURE_ABILITY, "The Pok\u00e9mon raises the foe’s PP usage.");
		}

		public Pressure newInstance()
		{
			return (Pressure)(new Pressure().activate());
		}

		public void enter(Battle b, ActivePokemon victim)
		{
			b.addMessage(victim.getName() + " is exerting pressure!");
		}
	}

	private static class Immunity extends Ability implements StatusPreventionEffect
	{
		private static final long serialVersionUID = 1L;

		public Immunity()
		{
			super(Namesies.IMMUNITY_ABILITY, "Prevents the Pok\u00e9mon from getting poisoned.");
		}

		public Immunity newInstance()
		{
			return (Immunity)(new Immunity().activate());
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status)
		{
			return status == StatusCondition.POISONED;
		}

		public String preventionMessage(ActivePokemon victim)
		{
			return victim.getName() + "'s Immunity prevents it from being poisoned!";
		}
	}

	private static class SnowCloak extends Ability implements StageChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public SnowCloak()
		{
			super(Namesies.SNOW_CLOAK_ABILITY, "Raises the Pok\u00e9mon’s evasion during a hailstorm by one level.");
		}

		public SnowCloak newInstance()
		{
			return (SnowCloak)(new SnowCloak().activate());
		}

		public int adjustStage(Integer stage, Stat s, ActivePokemon p, ActivePokemon opp, Battle b)
		{
			return s == Stat.EVASION && b.getWeather().namesies() == Namesies.HAILING_EFFECT ? stage + 1 : stage;
		}
	}

	private static class MarvelScale extends Ability implements StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public MarvelScale()
		{
			super(Namesies.MARVEL_SCALE_ABILITY, "Boosts Defense if there is a status problem.");
		}

		public MarvelScale newInstance()
		{
			return (MarvelScale)(new MarvelScale().activate());
		}

		public Stat toModify()
		{
			return Stat.DEFENSE;
		}

		public boolean modifyCondition(Battle b, ActivePokemon p, ActivePokemon opp)
		{
			return p.hasStatus();
		}

		public double modifyMultiplier()
		{
			return 1.5;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (s == toModify() && modifyCondition(b, p, opp))
			{
				stat *= modifyMultiplier();
			}
			
			return stat;
		}
	}

	private static class Multiscale extends Ability implements OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public Multiscale()
		{
			super(Namesies.MULTISCALE_ABILITY, "When this Pok\u00e9mon is at full HP, damage is lessened.");
		}

		public Multiscale newInstance()
		{
			return (Multiscale)(new Multiscale().activate());
		}

		public double getOppMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return victim.fullHealth() ? .5 : 1;
		}
	}

	private static class SheerForce extends Ability implements PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public SheerForce()
		{
			super(Namesies.SHEER_FORCE_ABILITY, "Attacks gain power, but lose their secondary effect.");
		}

		public SheerForce newInstance()
		{
			return (SheerForce)(new SheerForce().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getAttack().hasSecondaryEffects() ? 1.3 : 1;
		}
	}

	private static class Hustle extends Ability implements StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public Hustle()
		{
			super(Namesies.HUSTLE_ABILITY, "Boosts the Attack stat, but lowers accuracy.");
		}

		public Hustle newInstance()
		{
			return (Hustle)(new Hustle().activate());
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (s == Stat.ATTACK)
			{
				stat *= 1.5;
			}
			else if (s == Stat.ACCURACY)
			{
				stat *= .8;
			}
			
			return stat;
		}
	}

	private static class HugePower extends Ability implements StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public HugePower()
		{
			super(Namesies.HUGE_POWER_ABILITY, "Raises the Pok\u00e9mon’s Attack stat.");
		}

		public HugePower newInstance()
		{
			return (HugePower)(new HugePower().activate());
		}

		public Stat toModify()
		{
			return Stat.ATTACK;
		}

		public boolean modifyCondition(Battle b, ActivePokemon p, ActivePokemon opp)
		{
			return true;
		}

		public double modifyMultiplier()
		{
			return 2;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (s == toModify() && modifyCondition(b, p, opp))
			{
				stat *= modifyMultiplier();
			}
			
			return stat;
		}
	}

	private static class SpeedBoost extends Ability implements EndTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public SpeedBoost()
		{
			super(Namesies.SPEED_BOOST_ABILITY, "The Pok\u00e9mon’s Speed stat is gradually boosted.");
		}

		public SpeedBoost newInstance()
		{
			return (SpeedBoost)(new SpeedBoost().activate());
		}

		public void applyEndTurn(ActivePokemon victim, Battle b)
		{
			victim.getAttributes().modifyStage(victim, victim, 1, Stat.SPEED, b, CastSource.ABILITY);
		}
	}

	private static class MagicBounce extends Ability 
	{
		private static final long serialVersionUID = 1L;

		public MagicBounce()
		{
			super(Namesies.MAGIC_BOUNCE_ABILITY, "Reflects status-changing moves.");
		}

		public MagicBounce newInstance()
		{
			return (MagicBounce)(new MagicBounce().activate());
		}
	}

	private static class SuperLuck extends Ability implements CritStageEffect
	{
		private static final long serialVersionUID = 1L;

		public SuperLuck()
		{
			super(Namesies.SUPER_LUCK_ABILITY, "Heightens the critical-hit ratios of moves.");
		}

		public SuperLuck newInstance()
		{
			return (SuperLuck)(new SuperLuck().activate());
		}

		public int increaseCritStage(Integer stage, ActivePokemon p)
		{
			return stage + 1;
		}
	}

	private static class ShadowTag extends Ability implements OpponentTrappingEffect
	{
		private static final long serialVersionUID = 1L;

		public ShadowTag()
		{
			super(Namesies.SHADOW_TAG_ABILITY, "Prevents the foe from escaping.");
		}

		public ShadowTag newInstance()
		{
			return (ShadowTag)(new ShadowTag().activate());
		}

		public boolean isTrapped(Battle b, ActivePokemon p)
		{
			return !p.hasAbility(this.namesies);
		}

		public String trappingMessage(ActivePokemon escaper, ActivePokemon trapper)
		{
			return trapper.getName() + "'s " + this.getName() + " prevents " + escaper.getName() + " from escaping!";
		}
	}

	private static class Overcoat extends Ability implements WeatherBlockerEffect
	{
		private static final long serialVersionUID = 1L;

		public Overcoat()
		{
			super(Namesies.OVERCOAT_ABILITY, "Protects the Pok\u00e9mon from damage from weather.");
		}

		public Overcoat newInstance()
		{
			return (Overcoat)(new Overcoat().activate());
		}

		public boolean block(Namesies weather)
		{
			// TODO: Should also be immune to le powderz
			return true;
		}
	}

	private static class MagmaArmor extends Ability implements StatusPreventionEffect
	{
		private static final long serialVersionUID = 1L;

		public MagmaArmor()
		{
			super(Namesies.MAGMA_ARMOR_ABILITY, "Prevents the Pok\u00e9mon from becoming frozen.");
		}

		public MagmaArmor newInstance()
		{
			return (MagmaArmor)(new MagmaArmor().activate());
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status)
		{
			return status == StatusCondition.FROZEN;
		}

		public String preventionMessage(ActivePokemon victim)
		{
			return victim.getName() + "'s Magma Armor prevents freezing!";
		}
	}

	private static class SuctionCups extends Ability 
	{
		private static final long serialVersionUID = 1L;

		public SuctionCups()
		{
			super(Namesies.SUCTION_CUPS_ABILITY, "Negates all moves that force switching out.");
		}

		public SuctionCups newInstance()
		{
			return (SuctionCups)(new SuctionCups().activate());
		}
	}

	private static class Steadfast extends Ability 
	{
		private static final long serialVersionUID = 1L;

		public Steadfast()
		{
			super(Namesies.STEADFAST_ABILITY, "Raises Speed each time the Pok\u00e9mon flinches.");
		}

		public Steadfast newInstance()
		{
			return (Steadfast)(new Steadfast().activate());
		}
	}

	private static class SandStream extends Ability implements EntryEffect
	{
		private static final long serialVersionUID = 1L;

		public SandStream()
		{
			super(Namesies.SAND_STREAM_ABILITY, "The Pok\u00e9mon summons a sandstorm in battle.");
		}

		public SandStream newInstance()
		{
			return (SandStream)(new SandStream().activate());
		}

		public void enter(Battle b, ActivePokemon victim)
		{
			b.addEffect(Weather.getEffect(Namesies.SANDSTORM_EFFECT).newInstance());
			b.addMessage(victim.getName() + "'s " + this.getName() + " whipped up a sand storm!");
		}
	}

	private static class Regenerator extends Ability implements SwitchOutEffect
	{
		private static final long serialVersionUID = 1L;

		public Regenerator()
		{
			super(Namesies.REGENERATOR_ABILITY, "Restores a little HP when withdrawn from battle.");
		}

		public Regenerator newInstance()
		{
			return (Regenerator)(new Regenerator().activate());
		}

		public void switchOut(ActivePokemon switchee)
		{
			if (!switchee.hasStatus(StatusCondition.FAINTED)) switchee.healHealthFraction(1/3.0);
		}
	}

	private static class PoisonHeal extends Ability 
	{
		private static final long serialVersionUID = 1L;

		public PoisonHeal()
		{
			super(Namesies.POISON_HEAL_ABILITY, "Restores HP if the Pok\u00e9mon is poisoned.");
		}

		public PoisonHeal newInstance()
		{
			return (PoisonHeal)(new PoisonHeal().activate());
		}
	}

	private static class Truant extends Ability implements EndTurnEffect, BeforeTurnEffect
	{
		private static final long serialVersionUID = 1L;
		private boolean lazyface;

		public Truant()
		{
			super(Namesies.TRUANT_ABILITY, "Pok\u00e9mon can't attack on consecutive turns.");
		}

		public Truant newInstance()
		{
			Truant x = (Truant)(new Truant().activate());
			x.lazyface = false;
			return x;
		}

		public void applyEndTurn(ActivePokemon victim, Battle b)
		{
			if (victim.hasStatus(StatusCondition.ASLEEP)) lazyface = false;
			else lazyface = !lazyface;
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b)
		{
			if (lazyface)
			{
				b.addMessage(p.getName() + " is loafing around!");
				return false;
			}
			
			return true;
		}
	}

	private static class WonderGuard extends Ability implements OpposingBeforeTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public WonderGuard()
		{
			super(Namesies.WONDER_GUARD_ABILITY, "Only supereffective moves will hit.");
		}

		public WonderGuard newInstance()
		{
			return (WonderGuard)(new WonderGuard().activate());
		}

		public boolean opposingCanAttack(ActivePokemon p, ActivePokemon opp, Battle b)
		{
			if (p.getAttack().getCategory() == Category.STATUS) return true;
			if (Type.getAdvantage(p.getAttack().getType(b, p), opp, b) > 1) return true;
			if (p.getAttack().getType(b, p) == Type.NONE) return true;
			b.printAttacking(p);
			b.addMessage(opp.getName() + "'s " + this.getName() + " makes it immune to " + p.getAttack().getName() + "!");
			return false;
		}
	}

	private static class Normalize extends Ability 
	{
		private static final long serialVersionUID = 1L;

		public Normalize()
		{
			super(Namesies.NORMALIZE_ABILITY, "All the Pok\u00e9mon's moves become the Normal type.");
		}

		public Normalize newInstance()
		{
			return (Normalize)(new Normalize().activate());
		}
	}

	private static class Stall extends Ability implements StallingEffect
	{
		private static final long serialVersionUID = 1L;

		public Stall()
		{
			super(Namesies.STALL_ABILITY, "The Pok\u00e9mon moves after even slower foes.");
		}

		public Stall newInstance()
		{
			return (Stall)(new Stall().activate());
		}
	}

	private static class PurePower extends Ability implements StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public PurePower()
		{
			super(Namesies.PURE_POWER_ABILITY, "Raises the Pok\u00e9mon's Attack stat.");
		}

		public PurePower newInstance()
		{
			return (PurePower)(new PurePower().activate());
		}

		public Stat toModify()
		{
			return Stat.ATTACK;
		}

		public boolean modifyCondition(Battle b, ActivePokemon p, ActivePokemon opp)
		{
			return true;
		}

		public double modifyMultiplier()
		{
			return 2;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (s == toModify() && modifyCondition(b, p, opp))
			{
				stat *= modifyMultiplier();
			}
			
			return stat;
		}
	}

	private static class RoughSkin extends Ability implements PhysicalContactEffect
	{
		private static final long serialVersionUID = 1L;

		public RoughSkin()
		{
			super(Namesies.ROUGH_SKIN_ABILITY, "Inflicts damage to the foe on contact.");
		}

		public RoughSkin newInstance()
		{
			return (RoughSkin)(new RoughSkin().activate());
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			b.addMessage(user.getName() + " was hurt by " + victim.getName() + "'s " + this.getName() + "!");
			user.reduceHealthFraction(b, 1/8.0);
		}
	}

	private static class SolidRock extends Ability implements OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public SolidRock()
		{
			super(Namesies.SOLID_ROCK_ABILITY, "Reduces damage from supereffective attacks.");
		}

		public SolidRock newInstance()
		{
			return (SolidRock)(new SolidRock().activate());
		}

		public double getOppMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return Type.getAdvantage(user.getAttack().getType(b, user), victim, b) < 1 ? .75 : 1;
		}
	}

	private static class WhiteSmoke extends Ability implements StatProtectingEffect
	{
		private static final long serialVersionUID = 1L;

		public WhiteSmoke()
		{
			super(Namesies.WHITE_SMOKE_ABILITY, "Prevents other Pok\u00e9mon from lowering its stats.");
		}

		public WhiteSmoke newInstance()
		{
			return (WhiteSmoke)(new WhiteSmoke().activate());
		}

		public boolean prevent(ActivePokemon caster, Stat stat)
		{
			return true;
		}

		public String preventionMessage(ActivePokemon p)
		{
			return p.getName() + "'s White Smoke prevents its stats from being lowered!";
		}
	}

	private static class ToxicBoost extends Ability implements StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public ToxicBoost()
		{
			super(Namesies.TOXIC_BOOST_ABILITY, "Powers up physical attacks when poisoned.");
		}

		public ToxicBoost newInstance()
		{
			return (ToxicBoost)(new ToxicBoost().activate());
		}

		public Stat toModify()
		{
			return Stat.ATTACK;
		}

		public boolean modifyCondition(Battle b, ActivePokemon p, ActivePokemon opp)
		{
			return p.hasStatus(StatusCondition.POISONED);
		}

		public double modifyMultiplier()
		{
			return 1.5;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (s == toModify() && modifyCondition(b, p, opp))
			{
				stat *= modifyMultiplier();
			}
			
			return stat;
		}
	}

	private static class Anticipation extends Ability implements EntryEffect
	{
		private static final long serialVersionUID = 1L;

		public Anticipation()
		{
			super(Namesies.ANTICIPATION_ABILITY, "Senses a foe's dangerous moves.");
		}

		public Anticipation newInstance()
		{
			return (Anticipation)(new Anticipation().activate());
		}

		public void enter(Battle b, ActivePokemon victim)
		{
			ActivePokemon other = b.getOtherPokemon(victim.user());
			for (Move m : other.getMoves())
			{
				if (Type.getAdvantage(m.getAttack().getType(b, other), victim, b) > 1 || m.getAttack().isMoveType(MoveType.ONE_HIT_KO))
				{
					b.addMessage(victim.getName() + "'s " + this.getName() + " made it shudder!");
					break;
				}
			}
		}
	}

	private static class StormDrain extends Ability implements DamageBlocker
	{
		private static final long serialVersionUID = 1L;

		public StormDrain()
		{
			super(Namesies.STORM_DRAIN_ABILITY, "Draws in all Water-type moves to up Sp. Attack.");
		}

		public StormDrain newInstance()
		{
			return (StormDrain)(new StormDrain().activate());
		}

		public Type getType()
		{
			return Type.WATER;
		}

		public Stat toIncrease()
		{
			return Stat.SP_ATTACK;
		}

		public boolean block(Type attacking, ActivePokemon victim)
		{
			return attacking == getType();
		}

		public void alternateEffect(Battle b, ActivePokemon victim)
		{
			b.addMessage(victim.getName() + "'s " + this.getName() + " makes it immune to " + getType().getName() + " type moves!");
			victim.getAttributes().modifyStage(victim, victim, 1, toIncrease(), b, CastSource.ABILITY);
		}
	}

	private static class ColorChange extends Ability implements TakeDamageEffect, ChangeTypeMove
	{
		private static final long serialVersionUID = 1L;
		private Type type;

		public ColorChange()
		{
			super(Namesies.COLOR_CHANGE_ABILITY, "Changes the Pok\u00e9mon's type to the foe's move.");
		}

		public ColorChange newInstance()
		{
			ColorChange x = (ColorChange)(new ColorChange().activate());
			x.type = type;
			return x;
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Type t = user.getAttack().getType(b, user);
			if (!victim.isType(b, t))
			{
				type = t;
				PokemonEffect.getEffect(Namesies.CHANGE_TYPE_EFFECT).cast(b, victim, victim, CastSource.ABILITY, true);
			}
		}

		public Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return new Type[] {type, Type.NONE};
		}
	}

	private static class IceBody extends Ability implements EndTurnEffect, WeatherBlockerEffect
	{
		private static final long serialVersionUID = 1L;

		public IceBody()
		{
			super(Namesies.ICE_BODY_ABILITY, "The Pok\u00e9mon gradually regains HP in a hailstorm.");
		}

		public IceBody newInstance()
		{
			return (IceBody)(new IceBody().activate());
		}

		public void applyEndTurn(ActivePokemon victim, Battle b)
		{
			if (b.getWeather().namesies() == Namesies.HAILING_EFFECT)
			{
				victim.healHealthFraction(1/16.0);
				b.addMessage(victim.getName() + "'s HP was restored due to its " + this.getName() + "!", victim.getHP(), victim.user());
			}
		}

		public boolean block(Namesies weather)
		{
			return weather == Namesies.HAILING_EFFECT;
		}
	}

	private static class LightMetal extends Ability 
	{
		private static final long serialVersionUID = 1L;

		public LightMetal()
		{
			super(Namesies.LIGHT_METAL_ABILITY, "Halves the Pok\u00e9mon's weight.");
		}

		public LightMetal newInstance()
		{
			return (LightMetal)(new LightMetal().activate());
		}
	}

	private static class Drizzle extends Ability implements EntryEffect
	{
		private static final long serialVersionUID = 1L;

		public Drizzle()
		{
			super(Namesies.DRIZZLE_ABILITY, "The Pok\u00e9mon makes it rain if it appears in battle.");
		}

		public Drizzle newInstance()
		{
			return (Drizzle)(new Drizzle().activate());
		}

		public void enter(Battle b, ActivePokemon victim)
		{
			b.addEffect(Weather.getEffect(Namesies.RAINING_EFFECT).newInstance());
			b.addMessage(victim.getName() + "'s " + this.getName() + " started a downpour!");
		}
	}

	private static class AirLock extends Ability implements EntryEffect
	{
		private static final long serialVersionUID = 1L;

		public AirLock()
		{
			super(Namesies.AIR_LOCK_ABILITY, "Eliminates the effects of weather.");
		}

		public AirLock newInstance()
		{
			return (AirLock)(new AirLock().activate());
		}

		public void enter(Battle b, ActivePokemon victim)
		{
			b.addEffect(Weather.getEffect(Namesies.CLEAR_SKIES_EFFECT));
			b.addMessage(victim.getName() + "'s " + this.getName() + " eliminated the weather!");
		}
	}

	private static class Defiant extends Ability 
	{
		private static final long serialVersionUID = 1L;

		public Defiant()
		{
			super(Namesies.DEFIANT_ABILITY, "When its stats are lowered its Attack increases.");
		}

		public Defiant newInstance()
		{
			return (Defiant)(new Defiant().activate());
		}
	}

	private static class FlowerGift extends Ability implements StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public FlowerGift()
		{
			super(Namesies.FLOWER_GIFT_ABILITY, "Powers up party Pok\u00e9mon when it is sunny.");
		}

		public FlowerGift newInstance()
		{
			return (FlowerGift)(new FlowerGift().activate());
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			
			// TODO: Try to find something to do to make this generated
			return (int)(stat*((s == Stat.ATTACK || s == Stat.SP_DEFENSE) && b.getWeather().namesies() == Namesies.SUNNY_EFFECT ? 1.5 : 1));
		}
	}

	private static class Aftermath extends Ability implements PhysicalContactEffect
	{
		private static final long serialVersionUID = 1L;

		public Aftermath()
		{
			super(Namesies.AFTERMATH_ABILITY, "Damages the attacker landing the finishing hit.");
		}

		public Aftermath newInstance()
		{
			return (Aftermath)(new Aftermath().activate());
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (victim.isFainted(b))
			{
				b.addMessage(user.getName() + " was hurt by " + victim.getName() + "'s " + this.getName() + "!");
				user.reduceHealthFraction(b, 1/4.0);
			}
		}
	}

	private static class Heatproof extends Ability implements OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public Heatproof()
		{
			super(Namesies.HEATPROOF_ABILITY, "Weakens the power of Fire-type moves.");
		}

		public Heatproof newInstance()
		{
			return (Heatproof)(new Heatproof().activate());
		}

		public double getOppMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getAttack().getType(b, user) == Type.FIRE ? .5 : 1;
		}
	}

	private static class SandForce extends Ability implements PowerChangeEffect, WeatherBlockerEffect
	{
		private static final long serialVersionUID = 1L;

		public SandForce()
		{
			super(Namesies.SAND_FORCE_ABILITY, "Boosts certain moves' power in a sandstorm.");
		}

		public SandForce newInstance()
		{
			return (SandForce)(new SandForce().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Type type = user.getAttack().getType(b, user);
			return (type == Type.ROCK || type == Type.STEEL || type == Type.GROUND) && b.getWeather().namesies() == Namesies.SANDSTORM_EFFECT ? 1.3 : 1;
		}

		public boolean block(Namesies weather)
		{
			return weather == Namesies.SANDSTORM_EFFECT;
		}
	}

	private static class SnowWarning extends Ability implements EntryEffect
	{
		private static final long serialVersionUID = 1L;

		public SnowWarning()
		{
			super(Namesies.SNOW_WARNING_ABILITY, "The Pok\u00e9mon summons a hailstorm in battle.");
		}

		public SnowWarning newInstance()
		{
			return (SnowWarning)(new SnowWarning().activate());
		}

		public void enter(Battle b, ActivePokemon victim)
		{
			b.addEffect(Weather.getEffect(Namesies.HAILING_EFFECT).newInstance());
			b.addMessage(victim.getName() + "'s " + this.getName() + " caused it to hail!");
		}
	}

	private static class MotorDrive extends Ability implements DamageBlocker
	{
		private static final long serialVersionUID = 1L;

		public MotorDrive()
		{
			super(Namesies.MOTOR_DRIVE_ABILITY, "Raises Speed if hit by an Electric-type move.");
		}

		public MotorDrive newInstance()
		{
			return (MotorDrive)(new MotorDrive().activate());
		}

		public Type getType()
		{
			return Type.ELECTRIC;
		}

		public Stat toIncrease()
		{
			return Stat.SPEED;
		}

		public boolean block(Type attacking, ActivePokemon victim)
		{
			return attacking == getType();
		}

		public void alternateEffect(Battle b, ActivePokemon victim)
		{
			b.addMessage(victim.getName() + "'s " + this.getName() + " makes it immune to " + getType().getName() + " type moves!");
			victim.getAttributes().modifyStage(victim, victim, 1, toIncrease(), b, CastSource.ABILITY);
		}
	}

	private static class Justified extends Ability implements TakeDamageEffect
	{
		private static final long serialVersionUID = 1L;

		public Justified()
		{
			super(Namesies.JUSTIFIED_ABILITY, "Raises Attack when hit by a Dark-type move.");
		}

		public Justified newInstance()
		{
			return (Justified)(new Justified().activate());
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().getType(b, user) == Type.DARK)
			{
				victim.getAttributes().modifyStage(victim, victim, 1, Stat.ATTACK, b, CastSource.ABILITY);
			}
		}
	}

	private static class CursedBody extends Ability implements PhysicalContactEffect
	{
		private static final long serialVersionUID = 1L;

		public CursedBody()
		{
			super(Namesies.CURSED_BODY_ABILITY, "May disable a move used on the Pok\u00e9mon.");
		}

		public CursedBody newInstance()
		{
			return (CursedBody)(new CursedBody().activate());
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (Math.random()*100 < 30)
			{
				user.getAttributes().setLastMoveUsed();
				PokemonEffect disable = PokemonEffect.getEffect(Namesies.DISABLE_EFFECT);
				if (disable.applies(b, victim, user, CastSource.ABILITY))
				{
					disable.cast(b, victim, user, CastSource.ABILITY, false);
					b.addMessage(victim.getName() + "'s " + this.getName() + " disabled " + user.getName() + "'s " + user.getAttack().getName());
				}
			}
		}
	}

	private static class SlowStart extends Ability implements EndTurnEffect, EntryEffect, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;
		int count;

		public SlowStart()
		{
			super(Namesies.SLOW_START_ABILITY, "Temporarily halves Attack and Speed.");
		}

		public SlowStart newInstance()
		{
			SlowStart x = (SlowStart)(new SlowStart().activate());
			x.count = 0;
			return x;
		}

		public void applyEndTurn(ActivePokemon victim, Battle b)
		{
			count++;
		}

		public void enter(Battle b, ActivePokemon victim)
		{
			count = 0;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			
			// TODO: If it works for Flower Gift, do it here too
			return (int)(stat*(count < 5 && (s == Stat.ATTACK || s == Stat.SPEED) ? .5 : 1));
		}
	}

	private static class BadDreams extends Ability implements EndTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public BadDreams()
		{
			super(Namesies.BAD_DREAMS_ABILITY, "Reduces a sleeping foe's HP.");
		}

		public BadDreams newInstance()
		{
			return (BadDreams)(new BadDreams().activate());
		}

		public void applyEndTurn(ActivePokemon victim, Battle b)
		{
			ActivePokemon other = b.getOtherPokemon(victim.user());
			if (other.hasStatus(StatusCondition.ASLEEP))
			{
				b.addMessage(other.getName() + " was hurt by " + victim.getName() + "'s " + this.getName() + "!");
				other.reduceHealthFraction(b, 1/8.0);
			}
		}
	}

	private static class VictoryStar extends Ability implements StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public VictoryStar()
		{
			super(Namesies.VICTORY_STAR_ABILITY, "Boosts the accuracy of its allies and itself.");
		}

		public VictoryStar newInstance()
		{
			return (VictoryStar)(new VictoryStar().activate());
		}

		public Stat toModify()
		{
			return Stat.ACCURACY;
		}

		public boolean modifyCondition(Battle b, ActivePokemon p, ActivePokemon opp)
		{
			return true;
		}

		public double modifyMultiplier()
		{
			return 1.1;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (s == toModify() && modifyCondition(b, p, opp))
			{
				stat *= modifyMultiplier();
			}
			
			return stat;
		}
	}

	private static class Contrary extends Ability implements ModifyStageValueEffect
	{
		private static final long serialVersionUID = 1L;

		public Contrary()
		{
			super(Namesies.CONTRARY_ABILITY, "Makes stat changes have an opposite effect.");
		}

		public Contrary newInstance()
		{
			return (Contrary)(new Contrary().activate());
		}

		public int modifyStageValue(int modVal)
		{
			return modVal*(modVal < 0 ? -1 : 1);
		}
	}

	private static class BigPecks extends Ability implements StatProtectingEffect
	{
		private static final long serialVersionUID = 1L;

		public BigPecks()
		{
			super(Namesies.BIG_PECKS_ABILITY, "Protects the Pok\u00e9mon from Defense-lowering attacks.");
		}

		public BigPecks newInstance()
		{
			return (BigPecks)(new BigPecks().activate());
		}

		public boolean prevent(ActivePokemon caster, Stat stat)
		{
			return stat == Stat.DEFENSE;
		}

		public String preventionMessage(ActivePokemon p)
		{
			return p.getName() + "'s Big Pecks prevents its defense from being lowered!";
		}
	}

	private static class PoisonTouch extends Ability implements ApplyDamageEffect
	{
		private static final long serialVersionUID = 1L;

		public PoisonTouch()
		{
			super(Namesies.POISON_TOUCH_ABILITY, "May poison targets when a Pok\u00e9mon makes contact.");
		}

		public PoisonTouch newInstance()
		{
			return (PoisonTouch)(new PoisonTouch().activate());
		}

		public void applyEffect(Battle b, ActivePokemon user, ActivePokemon victim, Integer damage)
		{
			if (Math.random()*100 < 30)
			{
				Status.giveStatus(b, user, victim, StatusCondition.POISONED, true);
			}
		}
	}

	private static class Prankster extends Ability 
	{
		private static final long serialVersionUID = 1L;

		public Prankster()
		{
			super(Namesies.PRANKSTER_ABILITY, "Gives priority to a status move.");
		}

		public Prankster newInstance()
		{
			return (Prankster)(new Prankster().activate());
		}
	}

	private static class WonderSkin extends Ability implements StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public WonderSkin()
		{
			super(Namesies.WONDER_SKIN_ABILITY, "Makes status-changing moves more likely to miss.");
		}

		public WonderSkin newInstance()
		{
			return (WonderSkin)(new WonderSkin().activate());
		}

		public Stat toModify()
		{
			return Stat.EVASION;
		}

		public boolean modifyCondition(Battle b, ActivePokemon p, ActivePokemon opp)
		{
			return opp.getAttack().getCategory() == Category.STATUS;
		}

		public double modifyMultiplier()
		{
			return 1.5;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (s == toModify() && modifyCondition(b, p, opp))
			{
				stat *= modifyMultiplier();
			}
			
			return stat;
		}
	}

	private static class Mummy extends Ability implements PhysicalContactEffect, ChangeAbilityMove
	{
		private static final long serialVersionUID = 1L;

		public Mummy()
		{
			super(Namesies.MUMMY_ABILITY, "Contact with this Pok\u00e9mon spreads this Ability.");
		}

		public Mummy newInstance()
		{
			return (Mummy)(new Mummy().activate());
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.hasAbility(Namesies.MULTITYPE_ABILITY) || user.hasAbility(this.namesies))
			{
				return;
			}
			
			// Cast the change ability effect onto the user
			PokemonEffect.getEffect(Namesies.CHANGE_ABILITY_EFFECT).cast(b, victim, user, CastSource.ABILITY, true);
		}

		public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return (new Mummy()).newInstance();
		}

		public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return victim.getName() + "'s ability was changed to Mummy!";
		}
	}

	private static class Defeatist extends Ability implements PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public Defeatist()
		{
			super(Namesies.DEFEATIST_ABILITY, "Lowers stats when HP becomes half or less.");
		}

		public Defeatist newInstance()
		{
			return (Defeatist)(new Defeatist().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getHPRatio() < 1/2.0 ? .5 : 1;
		}
	}

	private static class WeakArmor extends Ability implements TakeDamageEffect
	{
		private static final long serialVersionUID = 1L;

		public WeakArmor()
		{
			super(Namesies.WEAK_ARMOR_ABILITY, "Physical attacks lower Defense and raise Speed.");
		}

		public WeakArmor newInstance()
		{
			return (WeakArmor)(new WeakArmor().activate());
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().getCategory() == Category.PHYSICAL)
			{
				victim.getAttributes().modifyStage(victim, victim, -1, Stat.DEFENSE, b, CastSource.ABILITY);
				victim.getAttributes().modifyStage(victim, victim, 1, Stat.SPEED, b, CastSource.ABILITY);
			}
		}
	}

	private static class Illusion extends Ability implements EntryEffect, SwitchOutEffect, TakeDamageEffect
	{
		private static final long serialVersionUID = 1L;
		private boolean activated;
		private String actualName;

		public Illusion()
		{
			super(Namesies.ILLUSION_ABILITY, "Comes out disguised as the Pok\u00e9mon in back.");
		}

		public Illusion newInstance()
		{
			Illusion x = (Illusion)(new Illusion().activate());
			x.activated = false;
			x.actualName = actualName;
			return x;
		}

		public void enter(Battle b, ActivePokemon victim)
		{
			List<ActivePokemon> team = b.getTrainer(victim.user()).getTeam();
			ActivePokemon illusion = team.get(team.size()-1);
			
			if (!illusion.canFight()) return;
			if (illusion.getPokemonInfo().getNumber() == victim.getPokemonInfo().getNumber()) return;
			
			activated = true;
			actualName = victim.getName(); // TODO: When the Pokemon is sent out, it displays the actual name, instead of the illusion name
			
			victim.setNickname(illusion.getName()); // TODO: Find a better workaround for this, the Illusion name is appearing in the Switch Pokemon menu
			b.addMessage("", illusion.getPokemonInfo(), illusion.isShiny(), false, victim.user());
			b.addMessage("", illusion.getName(), victim.user());
			b.addMessage("", illusion.getType(b), victim.user());
			b.addMessage("", illusion.getGender(), victim.user());
		}

		public void switchOut(ActivePokemon switchee)
		{
			activated = false;
			switchee.setNickname(actualName);
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (!activated) return;
			activated = false;
			victim.setNickname(actualName);
			b.addMessage(actualName + "'s Illusion was broken!");
			b.addMessage("", victim.getPokemonInfo(), victim.isShiny(), true, victim.user());
			b.addMessage("", actualName, victim.user());
			b.addMessage("", victim.getType(b), victim.user());
			b.addMessage("", victim.getGender(), victim.user());
		}
	}

	private static class Analytic extends Ability implements PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public Analytic()
		{
			super(Namesies.ANALYTIC_ABILITY, "Boosts move power when the Pok\u00e9mon moves last.");
		}

		public Analytic newInstance()
		{
			return (Analytic)(new Analytic().activate());
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return !b.isFirstAttack() ? 1.3 : 1;
		}
	}

	private static class SapSipper extends Ability implements DamageBlocker
	{
		private static final long serialVersionUID = 1L;

		public SapSipper()
		{
			super(Namesies.SAP_SIPPER_ABILITY, "Boosts Attack when hit by a Grass-type move.");
		}

		public SapSipper newInstance()
		{
			return (SapSipper)(new SapSipper().activate());
		}

		public Type getType()
		{
			return Type.GRASS;
		}

		public Stat toIncrease()
		{
			return Stat.ATTACK;
		}

		public boolean block(Type attacking, ActivePokemon victim)
		{
			return attacking == getType();
		}

		public void alternateEffect(Battle b, ActivePokemon victim)
		{
			b.addMessage(victim.getName() + "'s " + this.getName() + " makes it immune to " + getType().getName() + " type moves!");
			victim.getAttributes().modifyStage(victim, victim, 1, toIncrease(), b, CastSource.ABILITY);
		}
	}

	private static class IronBarbs extends Ability implements PhysicalContactEffect
	{
		private static final long serialVersionUID = 1L;

		public IronBarbs()
		{
			super(Namesies.IRON_BARBS_ABILITY, "Inflicts damage to the Pok\u00e9mon on contact.");
		}

		public IronBarbs newInstance()
		{
			return (IronBarbs)(new IronBarbs().activate());
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			b.addMessage(user.getName() + " was hurt by " + victim.getName() + "'s " + this.getName() + "!");
			user.reduceHealthFraction(b, 1/8.0);
		}
	}

	private static class MoldBreaker extends Ability implements EntryEffect
	{
		private static final long serialVersionUID = 1L;

		public MoldBreaker()
		{
			super(Namesies.MOLD_BREAKER_ABILITY, "Moves can be used regardless of Abilities.");
		}

		public MoldBreaker newInstance()
		{
			return (MoldBreaker)(new MoldBreaker().activate());
		}

		public void enter(Battle b, ActivePokemon victim)
		{
			b.addMessage(victim.getName() + " breaks the mold!");
		}
	}

	private static class Teravolt extends Ability implements EntryEffect
	{
		private static final long serialVersionUID = 1L;

		public Teravolt()
		{
			super(Namesies.TERAVOLT_ABILITY, "Moves can be used regardless of Abilities.");
		}

		public Teravolt newInstance()
		{
			return (Teravolt)(new Teravolt().activate());
		}

		public void enter(Battle b, ActivePokemon victim)
		{
			b.addMessage(victim.getName() + " is radiating a bursting aura!");
		}
	}

	private static class Turboblaze extends Ability implements EntryEffect
	{
		private static final long serialVersionUID = 1L;

		public Turboblaze()
		{
			super(Namesies.TURBOBLAZE_ABILITY, "Moves can be used regardless of Abilities.");
		}

		public Turboblaze newInstance()
		{
			return (Turboblaze)(new Turboblaze().activate());
		}

		public void enter(Battle b, ActivePokemon victim)
		{
			b.addMessage(victim.getName() + " is radiating a blazing aura!");
		}
	}

	private static class RunAway extends Ability implements DefiniteEscape
	{
		private static final long serialVersionUID = 1L;

		public RunAway()
		{
			super(Namesies.RUN_AWAY_ABILITY, "Enables a sure getaway from wild Pok\u00e9mon.");
		}

		public RunAway newInstance()
		{
			return (RunAway)(new RunAway().activate());
		}
	}

	private static class StickyHold extends Ability 
	{
		private static final long serialVersionUID = 1L;

		public StickyHold()
		{
			super(Namesies.STICKY_HOLD_ABILITY, "Protects the Pok\u00e9mon from item theft.");
		}

		public StickyHold newInstance()
		{
			return (StickyHold)(new StickyHold().activate());
		}
	}

	private static class Klutz extends Ability 
	{
		private static final long serialVersionUID = 1L;

		public Klutz()
		{
			super(Namesies.KLUTZ_ABILITY, "The Pok\u00e9mon can't use any held items.");
		}

		public Klutz newInstance()
		{
			return (Klutz)(new Klutz().activate());
		}
	}

	private static class Unburden extends Ability implements StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public Unburden()
		{
			super(Namesies.UNBURDEN_ABILITY, "Raises Speed if a held item is used.");
		}

		public Unburden newInstance()
		{
			return (Unburden)(new Unburden().activate());
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			// TODO: Is change-item effect the same thing as having a consumed item? Was this written before that existed? That should be what is being used instead if not Also I'm mostly not putting this in the other format because I want this todo to stay so when this gets fixed update the txt file as well so it's in the nice and pretty new format that is nice and pretty
			return stat*(s == Stat.SPEED && p.hasEffect(Namesies.CHANGE_ITEM_EFFECT) && !p.isHoldingItem(b) ? 2 : 1);
		}
	}

	private static class Pickpocket extends Ability implements PhysicalContactEffect, ItemCondition
	{
		private static final long serialVersionUID = 1L;
		private Item item;

		public Pickpocket()
		{
			super(Namesies.PICKPOCKET_ABILITY, "Steals an item when hit by another Pok\u00e9mon.");
		}

		public Pickpocket newInstance()
		{
			return (Pickpocket)(new Pickpocket().activate());
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (victim.isFainted(b) || !user.isHoldingItem(b) || victim.isHoldingItem(b) || b.getTrainer(victim.user()) instanceof WildPokemon || user.hasAbility(Namesies.STICKY_HOLD_ABILITY))
			{
				return;
			}
			
			Item stolen = user.getHeldItem(b);
			b.addMessage(victim.getName() + " stole " + user.getName() + "'s " + stolen.getName() + "!");
			
			if (b.isWildBattle())
			{
				user.removeItem();
				victim.giveItem((HoldItem)stolen);
				return;
			}
			
			item = stolen;
			PokemonEffect.getEffect(Namesies.CHANGE_ITEM_EFFECT).cast(b, victim, victim, CastSource.ABILITY, false);
			item = Item.noneItem();
			PokemonEffect.getEffect(Namesies.CHANGE_ITEM_EFFECT).cast(b, victim, user, CastSource.ABILITY, false);
		}

		public Item getItem()
		{
			return item;
		}
	}

	private static class Harvest extends Ability implements EndTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public Harvest()
		{
			super(Namesies.HARVEST_ABILITY, "May create another Berry after one is used.");
		}

		public Harvest newInstance()
		{
			return (Harvest)(new Harvest().activate());
		}

		public void applyEndTurn(ActivePokemon victim, Battle b)
		{
			PokemonEffect consumed = victim.getEffect(Namesies.CONSUMED_ITEM_EFFECT);
			if (consumed == null || victim.isHoldingItem(b)) return;
			Item restored = ((ItemCondition)consumed).getItem();
			if (restored instanceof Berry && (b.getWeather().namesies() == Namesies.SUNNY_EFFECT || Math.random() < .5))
			{
				victim.giveItem((HoldItem)restored);
				b.addMessage(victim.getName() + "'s " + this.getName() + " restored its " + restored.getName() + "!");
			}
		}
	}

	private static class Pickup extends Ability implements EndBattleEffect
	{
		private static final long serialVersionUID = 1L;

		public Pickup()
		{
			super(Namesies.PICKUP_ABILITY, "The Pok\u00e9mon may pick up items.");
		}

		public Pickup newInstance()
		{
			return (Pickup)(new Pickup().activate());
		}

		public void afterBattle(Trainer player, Battle b, ActivePokemon p)
		{
			if (!p.isHoldingItem(b) && Math.random() < .1)
			{
				// TODO: THIS SHOULDN'T JUST BE LEFTOVERS IT SHOULD BE MORE FUN STUFF
				p.giveItem((HoldItem)Item.getItem(Namesies.LEFTOVERS_ITEM));
			}
		}
	}

	private static class Unnerve extends Ability implements EntryEffect
	{
		private static final long serialVersionUID = 1L;

		public Unnerve()
		{
			super(Namesies.UNNERVE_ABILITY, "Makes the foe nervous and unable to eat Berries.");
		}

		public Unnerve newInstance()
		{
			return (Unnerve)(new Unnerve().activate());
		}

		public void enter(Battle b, ActivePokemon victim)
		{
			b.addMessage(victim.getName() + "'s " + this.getName() + " made " + b.getOtherPokemon(victim.user()).getName() + " too nervous to eat berries!");
		}
	}

	private static class HoneyGather extends Ability implements EndBattleEffect
	{
		private static final long serialVersionUID = 1L;

		public HoneyGather()
		{
			super(Namesies.HONEY_GATHER_ABILITY, "The Pok\u00e9mon may gather Honey from somewhere.");
		}

		public HoneyGather newInstance()
		{
			return (HoneyGather)(new HoneyGather().activate());
		}

		public void afterBattle(Trainer player, Battle b, ActivePokemon p)
		{
			if (!p.isHoldingItem(b) && Math.random()*100 < 5*Math.ceil(p.getLevel()/10.0))
			{
				// TODO: Should give the item Honey, but this item has no purpose in our game so we'll see what this ability should actually do also something about Syrup Gather
				p.giveItem((HoldItem)Item.getItem(Namesies.LEFTOVERS_ITEM));
			}
		}
	}

	private static class Gluttony extends Ability 
	{
		private static final long serialVersionUID = 1L;

		public Gluttony()
		{
			super(Namesies.GLUTTONY_ABILITY, "Makes the Pok\u00e9mon use a held Berry earlier than usual.");
		}

		public Gluttony newInstance()
		{
			return (Gluttony)(new Gluttony().activate());
		}
	}

	private static class Multitype extends Ability 
	{
		private static final long serialVersionUID = 1L;

		public Multitype()
		{
			super(Namesies.MULTITYPE_ABILITY, "Changes type to match the held Plate.");
		}

		public Multitype newInstance()
		{
			return (Multitype)(new Multitype().activate());
		}
	}

	private static class Forecast extends Ability 
	{
		private static final long serialVersionUID = 1L;

		public Forecast()
		{
			super(Namesies.FORECAST_ABILITY, "Changes with the weather.");
		}

		public Forecast newInstance()
		{
			return (Forecast)(new Forecast().activate());
		}
	}
}
