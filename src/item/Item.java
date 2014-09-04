package item;

import item.Bag.BagCategory;
import item.Bag.BattleBagCategory;
import item.berry.Berry;
import item.berry.GainableEffectBerry;
import item.berry.HealthTriggeredBerry;
import item.berry.StatusBerry;
import item.hold.ConsumedItem;
import item.hold.DriveItem;
import item.hold.HoldItem;
import item.hold.PlateItem;
import item.hold.PowerItem;
import item.use.BallItem;
import item.use.BattleUseItem;
import item.use.MoveUseItem;
import item.use.PokemonUseItem;
import item.use.TrainerUseItem;
import item.use.UseItem;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import main.Global;
import main.Type;
import pokemon.ActivePokemon;
import pokemon.BaseEvolution;
import pokemon.Evolution;
import pokemon.Evolution.EvolutionCheck;
import pokemon.Stat;
import trainer.CharacterData;
import trainer.Team;
import trainer.Trainer;
import trainer.Trainer.Action;
import trainer.WildPokemon;
import battle.Attack.Category;
import battle.Battle;
import battle.Move;
import battle.effect.ApplyDamageEffect;
import battle.effect.AttackSelectionEffect;
import battle.effect.BracingEffect;
import battle.effect.CritStageEffect;
import battle.effect.DefiniteEscape;
import battle.effect.Effect.CastSource;
import battle.effect.EffectBlockerEffect;
import battle.effect.EndTurnEffect;
import battle.effect.EntryEffect;
import battle.effect.GroundedEffect;
import battle.effect.ItemCondition;
import battle.effect.LevitationEffect;
import battle.effect.OpponentPowerChangeEffect;
import battle.effect.PhysicalContactEffect;
import battle.effect.PokemonEffect;
import battle.effect.PowerChangeEffect;
import battle.effect.RepellingEffect;
import battle.effect.StallingEffect;
import battle.effect.StatChangingEffect;
import battle.effect.StatProtectingEffect;
import battle.effect.Status;
import battle.effect.Status.StatusCondition;
import battle.effect.TakeDamageEffect;
import battle.effect.TeamEffect;
import battle.effect.Weather.WeatherType;
import battle.effect.WeatherBlockerEffect;
import battle.effect.WeatherExtendingEffect;

public abstract class Item implements Comparable<Item>, Serializable
{
	private static final long serialVersionUID = 1L;

	private static HashMap<String, Item> map;

	protected String name, desc;
	protected BagCategory cat;
	protected List<BattleBagCategory> bcat;
	protected int price, index;

	public static boolean exists(String s)
	{
		if (map == null)
			loadItems();

		return map.containsKey(s);
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException
	{
		ois.defaultReadObject();
		synchronized (Item.class)
		{
			if (map == null)
				loadItems();
			
			map.put(this.getName(), this);
		}
	}
	
	public Item(String name, String description, BagCategory category, int index)
	{
		this.name = name;
		this.desc = description;
		this.cat = category;
		this.index = index;
		
		this.bcat = new ArrayList<>();
		this.price = -1;
	}

	public Item()
	{
		this.name = this.desc = "UNDEFINED";
		this.cat = BagCategory.MISC;
		bcat = new ArrayList<>();
		this.price = -1;
		this.index = 0;
	}

	public int compareTo(Item o)
	{
		return this.name.compareTo(o.name);
	}

	public boolean isUsable()
	{
		return this instanceof UseItem;
	}

	public boolean isHoldable()
	{
		return this instanceof HoldItem;
	}

	public String getName()
	{
		return this.name;
	}

	public String getDesc()
	{
		return this.desc;
	}

	public int getPrice()
	{
		return this.price;
	}

	public int getIndex()
	{
		return index;
	}

	public static Item noneItem()
	{
		return getItem("None");
	}

	public static Item getItem(String m)
	{
		if (map == null)
			loadItems();
		if (map.containsKey(m))
			return map.get(m);

		Global.error("No such Item " + m);
		return null;
	}

	public static boolean isItem(String m)
	{
		if (map == null)
			loadItems();
		if (map.containsKey(m))
			return true;
		return false;
	}

	public boolean equals(Object o)
	{
		if (!(o instanceof Item))
			return false;
		return (((Item) o).getName().equals(name));
	}

	public int hashCode()
	{
		return name.hashCode();
	}

	public static void loadItems()
	{
		if (map != null)
			return;
		map = new HashMap<>();

		// EVERYTHING BELOW IS GENERATED ###

		// List all of the classes we are loading
		map.put("None", new None());
		map.put("Syrup", new Syrup());
		map.put("Bicycle", new Bicycle());
		map.put("Surfboard", new Surfboard());
		map.put("Fishing Rod", new FishingRod());
		map.put("Absorb Bulb", new AbsorbBulb());
		map.put("Air Balloon", new AirBalloon());
		map.put("Amulet Coin", new AmuletCoin());
		map.put("Big Root", new BigRoot());
		map.put("Binding Band", new BindingBand());
		map.put("Black Sludge", new BlackSludge());
		map.put("Bright Powder", new BrightPowder());
		map.put("Cell Battery", new CellBattery());
		map.put("Choice Band", new ChoiceBand());
		map.put("Choice Scarf", new ChoiceScarf());
		map.put("Choice Specs", new ChoiceSpecs());
		map.put("Cleanse Tag", new CleanseTag());
		map.put("Damp Rock", new DampRock());
		map.put("Heat Rock", new HeatRock());
		map.put("Icy Rock", new IcyRock());
		map.put("Smooth Rock", new SmoothRock());
		map.put("Eject Button", new EjectButton());
		map.put("Destiny Knot", new DestinyKnot());
		map.put("Expert Belt", new ExpertBelt());
		map.put("Flame Orb", new FlameOrb());
		map.put("Toxic Orb", new ToxicOrb());
		map.put("Float Stone", new FloatStone());
		map.put("Focus Band", new FocusBand());
		map.put("Focus Sash", new FocusSash());
		map.put("Grip Claw", new GripClaw());
		map.put("Griseous Orb", new GriseousOrb());
		map.put("Iron Ball", new IronBall());
		map.put("Lagging Tail", new LaggingTail());
		map.put("Life Orb", new LifeOrb());
		map.put("Light Ball", new LightBall());
		map.put("Light Clay", new LightClay());
		map.put("Lucky Egg", new LuckyEgg());
		map.put("Lucky Punch", new LuckyPunch());
		map.put("Macho Brace", new MachoBrace());
		map.put("Mental Herb", new MentalHerb());
		map.put("Metal Powder", new MetalPowder());
		map.put("Metronome", new Metronome());
		map.put("Muscle Band", new MuscleBand());
		map.put("Power Anklet", new PowerAnklet());
		map.put("Power Band", new PowerBand());
		map.put("Power Belt", new PowerBelt());
		map.put("Power Bracer", new PowerBracer());
		map.put("Power Lens", new PowerLens());
		map.put("Power Weight", new PowerWeight());
		map.put("Quick Claw", new QuickClaw());
		map.put("Quick Powder", new QuickPowder());
		map.put("Red Card", new RedCard());
		map.put("Ring Target", new RingTarget());
		map.put("Rocky Helmet", new RockyHelmet());
		map.put("Safety Goggles", new SafetyGoggles());
		map.put("Scope Lens", new ScopeLens());
		map.put("Shed Shell", new ShedShell());
		map.put("Shell Bell", new ShellBell());
		map.put("Smoke Ball", new SmokeBall());
		map.put("Snowball", new Snowball());
		map.put("Soul Dew", new SoulDew());
		map.put("Stick", new Stick());
		map.put("Sticky Barb", new StickyBarb());
		map.put("Thick Club", new ThickClub());
		map.put("Weakness Policy", new WeaknessPolicy());
		map.put("White Herb", new WhiteHerb());
		map.put("Wide Lens", new WideLens());
		map.put("Wise Glasses", new WiseGlasses());
		map.put("Zoom Lens", new ZoomLens());
		map.put("Full Incense", new FullIncense());
		map.put("Lax Incense", new LaxIncense());
		map.put("Luck Incense", new LuckIncense());
		map.put("Odd Incense", new OddIncense());
		map.put("Pure Incense", new PureIncense());
		map.put("Rock Incense", new RockIncense());
		map.put("Rose Incense", new RoseIncense());
		map.put("Sea Incense", new SeaIncense());
		map.put("Wave Incense", new WaveIncense());
		map.put("Draco Plate", new DracoPlate());
		map.put("Dread Plate", new DreadPlate());
		map.put("Earth Plate", new EarthPlate());
		map.put("Fist Plate", new FistPlate());
		map.put("Flame Plate", new FlamePlate());
		map.put("Icicle Plate", new IciclePlate());
		map.put("Insect Plate", new InsectPlate());
		map.put("Iron Plate", new IronPlate());
		map.put("Meadow Plate", new MeadowPlate());
		map.put("Mind Plate", new MindPlate());
		map.put("Sky Plate", new SkyPlate());
		map.put("Splash Plate", new SplashPlate());
		map.put("Spooky Plate", new SpookyPlate());
		map.put("Stone Plate", new StonePlate());
		map.put("Toxic Plate", new ToxicPlate());
		map.put("Zap Plate", new ZapPlate());
		map.put("Burn Drive", new BurnDrive());
		map.put("Chill Drive", new ChillDrive());
		map.put("Douse Drive", new DouseDrive());
		map.put("Shock Drive", new ShockDrive());
		map.put("Fire Gem", new FireGem());
		map.put("Water Gem", new WaterGem());
		map.put("Electric Gem", new ElectricGem());
		map.put("Grass Gem", new GrassGem());
		map.put("Ice Gem", new IceGem());
		map.put("Fighting Gem", new FightingGem());
		map.put("Poison Gem", new PoisonGem());
		map.put("Ground Gem", new GroundGem());
		map.put("Flying Gem", new FlyingGem());
		map.put("Psychic Gem", new PsychicGem());
		map.put("Bug Gem", new BugGem());
		map.put("Rock Gem", new RockGem());
		map.put("Ghost Gem", new GhostGem());
		map.put("Dragon Gem", new DragonGem());
		map.put("Dark Gem", new DarkGem());
		map.put("Steel Gem", new SteelGem());
		map.put("Normal Gem", new NormalGem());
		map.put("Leftovers", new Leftovers());
		map.put("Black Belt", new BlackBelt());
		map.put("Black Glasses", new BlackGlasses());
		map.put("Charcoal", new Charcoal());
		map.put("Dragon Fang", new DragonFang());
		map.put("Hard Stone", new HardStone());
		map.put("Magnet", new Magnet());
		map.put("Metal Coat", new MetalCoat());
		map.put("Miracle Seed", new MiracleSeed());
		map.put("Mystic Water", new MysticWater());
		map.put("NeverMeltIce", new NeverMeltIce());
		map.put("Poison Barb", new PoisonBarb());
		map.put("Sharp Beak", new SharpBeak());
		map.put("Silk Scarf", new SilkScarf());
		map.put("Silver Powder", new SilverPowder());
		map.put("Soft Sand", new SoftSand());
		map.put("Spell Tag", new SpellTag());
		map.put("Twisted Spoon", new TwistedSpoon());
		map.put("Dawn Stone", new DawnStone());
		map.put("DeepSeaScale", new DeepSeaScale());
		map.put("DeepSeaTooth", new DeepSeaTooth());
		map.put("Dragon Scale", new DragonScale());
		map.put("Dubious Disc", new DubiousDisc());
		map.put("Dusk Stone", new DuskStone());
		map.put("Electirizer", new Electirizer());
		map.put("Fire Stone", new FireStone());
		map.put("King's Rock", new KingsRock());
		map.put("Leaf Stone", new LeafStone());
		map.put("Magmarizer", new Magmarizer());
		map.put("Moon Stone", new MoonStone());
		map.put("Oval Stone", new OvalStone());
		map.put("Everstone", new Everstone());
		map.put("Prism Scale", new PrismScale());
		map.put("Protector", new Protector());
		map.put("Razor Claw", new RazorClaw());
		map.put("Razor Fang", new RazorFang());
		map.put("Reaper Cloth", new ReaperCloth());
		map.put("Shiny Stone", new ShinyStone());
		map.put("Sun Stone", new SunStone());
		map.put("Thunder Stone", new ThunderStone());
		map.put("Up-Grade", new UpGrade());
		map.put("Water Stone", new WaterStone());
		map.put("Antidote", new Antidote());
		map.put("Awakening", new Awakening());
		map.put("Burn Heal", new BurnHeal());
		map.put("Ice Heal", new IceHeal());
		map.put("Paralyze Heal", new ParalyzeHeal());
		map.put("Full Heal", new FullHeal());
		map.put("Full Restore", new FullRestore());
		map.put("Elixir", new Elixir());
		map.put("Max Elixir", new MaxElixir());
		map.put("Ether", new Ether());
		map.put("Max Ether", new MaxEther());
		map.put("Berry Juice", new BerryJuice());
		map.put("Sweet Heart", new SweetHeart());
		map.put("Potion", new Potion());
		map.put("Energy Powder", new EnergyPowder());
		map.put("Fresh Water", new FreshWater());
		map.put("Super Potion", new SuperPotion());
		map.put("Soda Pop", new SodaPop());
		map.put("Lemonade", new Lemonade());
		map.put("Moomoo Milk", new MoomooMilk());
		map.put("Energy Root", new EnergyRoot());
		map.put("Hyper Potion", new HyperPotion());
		map.put("Max Potion", new MaxPotion());
		map.put("Revive", new Revive());
		map.put("Max Revive", new MaxRevive());
		map.put("Revival Herb", new RevivalHerb());
		map.put("Sacred Ash", new SacredAsh());
		map.put("Dire Hit", new DireHit());
		map.put("Guard Spec.", new GuardSpec());
		map.put("X Accuracy", new XAccuracy());
		map.put("X Attack", new XAttack());
		map.put("X Defend", new XDefend());
		map.put("X Sp. Def", new XSpDef());
		map.put("X Special", new XSpecial());
		map.put("X Speed", new XSpeed());
		map.put("Calcium", new Calcium());
		map.put("Carbos", new Carbos());
		map.put("Clever Wing", new CleverWing());
		map.put("Health Wing", new HealthWing());
		map.put("HP Up", new HPUp());
		map.put("Genius Wing", new GeniusWing());
		map.put("Iron", new Iron());
		map.put("Muscle Wing", new MuscleWing());
		map.put("PP Max", new PPMax());
		map.put("PP Up", new PPUp());
		map.put("Protein", new Protein());
		map.put("Rare Candy", new RareCandy());
		map.put("Resist Wing", new ResistWing());
		map.put("Swift Wing", new SwiftWing());
		map.put("Zinc", new Zinc());
		map.put("Cherish Ball", new CherishBall());
		map.put("Dive Ball", new DiveBall());
		map.put("Dusk Ball", new DuskBall());
		map.put("Fast Ball", new FastBall());
		map.put("Great Ball", new GreatBall());
		map.put("Heal Ball", new HealBall());
		map.put("Heavy Ball", new HeavyBall());
		map.put("Level Ball", new LevelBall());
		map.put("Love Ball", new LoveBall());
		map.put("Lure Ball", new LureBall());
		map.put("Luxury Ball", new LuxuryBall());
		map.put("Master Ball", new MasterBall());
		map.put("Moon Ball", new MoonBall());
		map.put("Nest Ball", new NestBall());
		map.put("Net Ball", new NetBall());
		map.put("Pok\u00e9 Ball", new PokeBall());
		map.put("Premier Ball", new PremierBall());
		map.put("Quick Ball", new QuickBall());
		map.put("Repeat Ball", new RepeatBall());
		map.put("Safari Ball", new SafariBall());
		map.put("Timer Ball", new TimerBall());
		map.put("Ultra Ball", new UltraBall());
		map.put("Cheri Berry", new CheriBerry());
		map.put("Chesto Berry", new ChestoBerry());
		map.put("Pecha Berry", new PechaBerry());
		map.put("Rawst Berry", new RawstBerry());
		map.put("Aspear Berry", new AspearBerry());
		map.put("Leppa Berry", new LeppaBerry());
		map.put("Oran Berry", new OranBerry());
		map.put("Persim Berry", new PersimBerry());
		map.put("Lum Berry", new LumBerry());
		map.put("Sitrus Berry", new SitrusBerry());
		map.put("Razz Berry", new RazzBerry());
		map.put("Pomeg Berry", new PomegBerry());
		map.put("Kelpsy Berry", new KelpsyBerry());
		map.put("Qualot Berry", new QualotBerry());
		map.put("Hondew Berry", new HondewBerry());
		map.put("Grepa Berry", new GrepaBerry());
		map.put("Tamato Berry", new TamatoBerry());
		map.put("Occa Berry", new OccaBerry());
		map.put("Passho Berry", new PasshoBerry());
		map.put("Wacan Berry", new WacanBerry());
		map.put("Rindo Berry", new RindoBerry());
		map.put("Yache Berry", new YacheBerry());
		map.put("Chople Berry", new ChopleBerry());
		map.put("Kebia Berry", new KebiaBerry());
		map.put("Shuca Berry", new ShucaBerry());
		map.put("Coba Berry", new CobaBerry());
		map.put("Payapa Berry", new PayapaBerry());
		map.put("Tanga Berry", new TangaBerry());
		map.put("Charti Berry", new ChartiBerry());
		map.put("Kasib Berry", new KasibBerry());
		map.put("Haban Berry", new HabanBerry());
		map.put("Colbur Berry", new ColburBerry());
		map.put("Babiri Berry", new BabiriBerry());
		map.put("Chilan Berry", new ChilanBerry());
		map.put("Liechi Berry", new LiechiBerry());
		map.put("Ganlon Berry", new GanlonBerry());
		map.put("Salac Berry", new SalacBerry());
		map.put("Petaya Berry", new PetayaBerry());
		map.put("Apicot Berry", new ApicotBerry());
		map.put("Lansat Berry", new LansatBerry());
		map.put("Starf Berry", new StarfBerry());
		map.put("Comet Shard", new CometShard());
		map.put("Tiny Mushroom", new TinyMushroom());
		map.put("Big Mushroom", new BigMushroom());
		map.put("Balm Mushroom", new BalmMushroom());
		map.put("Nugget", new Nugget());
		map.put("Big Nugget", new BigNugget());
		map.put("Pearl", new Pearl());
		map.put("Big Pearl", new BigPearl());
		map.put("Stardust", new Stardust());
		map.put("Star Piece", new StarPiece());
		map.put("Rare Bone", new RareBone());
		map.put("Honey", new Honey());
		map.put("Eviolite", new Eviolite());
		map.put("Heart Scale", new HeartScale());
		map.put("Repel", new Repel());
		map.put("Super Repel", new SuperRepel());
		map.put("Max Repel", new MaxRepel());
	}

	/**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

	private static class None extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public None()
		{
			super("None", "YOU SHUOLDN'T SEE THIS", BagCategory.MISC, 0);
			super.price = -1;
		}

		public int flingDamage()
		{
			return 9001;
		}
	}

	private static class Syrup extends Item implements TrainerUseItem
	{
		private static final long serialVersionUID = 1L;

		public Syrup()
		{
			super("Syrup", "A mysterious bottle of syrup. Maybe it will be useful some day.", BagCategory.KEY_ITEM, 1);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public boolean use(Trainer t)
		{
			return false;
		}
	}

	private static class Bicycle extends Item implements TrainerUseItem
	{
		private static final long serialVersionUID = 1L;

		public Bicycle()
		{
			super("Bicycle", "A folding Bicycle that enables much faster movement than the Running Shoes.", BagCategory.KEY_ITEM, 2);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public boolean use(Trainer t)
		{
			// TODO: if (Can ride bike) Set the bike as a 'currentlyUsing' item
			// May need to make this take in info on the route
			return false;
		}
	}

	private static class Surfboard extends Item implements TrainerUseItem
	{
		private static final long serialVersionUID = 1L;

		public Surfboard()
		{
			super("Surfboard", "A fancy shmancy surfboard that lets you be RADICAL DUDE!", BagCategory.KEY_ITEM, 3);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public boolean use(Trainer t)
		{
			return false;
		}
	}

	private static class FishingRod extends Item implements TrainerUseItem
	{
		private static final long serialVersionUID = 1L;

		public FishingRod()
		{
			super("Fishing Rod", "A multi-purpose, do-it-all kind of fishing rod. The kind you can use wherever you want. Except on land.", BagCategory.KEY_ITEM, 4);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "Oh! A bite!";
		}

		public boolean use(Trainer t)
		{
			// TODO: if (spot in front of player is a fishing spot) Set as 'currentlyUsing'
			// May need to make this take in info on the route
			return false;
		}
	}

	private static class AbsorbBulb extends Item implements HoldItem, ConsumedItem, TakeDamageEffect
	{
		private static final long serialVersionUID = 1L;

		public AbsorbBulb()
		{
			super("Absorb Bulb", "A consumable bulb. If the holder is hit by a Water-type move, its Sp. Atk will rise.", BagCategory.MISC, 5);
			super.price = 200;
		}

		public Stat toIncrease()
		{
			return Stat.SP_ATTACK;
		}

		public Type damageType()
		{
			return Type.WATER;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().getType(b, user) == damageType() && victim.getAttributes().modifyStage(victim, victim, 1, toIncrease(), b, CastSource.HELD_ITEM))
			{
				victim.consumeItem(b);
			}
		}
	}

	private static class AirBalloon extends Item implements HoldItem, ConsumedItem, LevitationEffect, TakeDamageEffect, EntryEffect
	{
		private static final long serialVersionUID = 1L;

		public AirBalloon()
		{
			super("Air Balloon", "When held by a Pok\u00e9mon, the Pok\u00e9mon will float into the air. When the holder is attacked, this item will burst.", BagCategory.MISC, 6);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			b.addMessage(victim.getName() + "'s " + this.name + " popped!");
			victim.consumeItem(b);
		}

		public void enter(Battle b, ActivePokemon victim)
		{
			b.addMessage(victim.getName() + " floats with its " + this.name + "!");
		}
	}

	private static class AmuletCoin extends Item implements HoldItem, EntryEffect
	{
		private static final long serialVersionUID = 1L;

		public AmuletCoin()
		{
			super("Amulet Coin", "An item to be held by a Pok\u00e9mon. It doubles a battle's prize money if the holding Pok\u00e9mon joins in.", BagCategory.MISC, 7);
			super.price = 100;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void enter(Battle b, ActivePokemon victim)
		{
			TeamEffect.getEffect("DoubleMoney").cast(b, victim, victim, CastSource.HELD_ITEM, false);
		}
	}

	private static class BigRoot extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public BigRoot()
		{
			super("Big Root", "A Pok\u00e9mon held item that boosts the power of HP-stealing moves to let the holder recover more HP.", BagCategory.MISC, 8);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class BindingBand extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public BindingBand()
		{
			super("Binding Band", "This item, when attached to a Pok\u00e9mon, increases damage caused by moves that constrict the opponent.", BagCategory.MISC, 9);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class BlackSludge extends Item implements HoldItem, EndTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public BlackSludge()
		{
			super("Black Sludge", "A held item that gradually restores the HP of Poison-type Pok\u00e9mon. It inflicts damage on all other types.", BagCategory.MISC, 10);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void apply(ActivePokemon victim, Battle b)
		{
			if (victim.isType(b, Type.POISON))
			{
				if (victim.fullHealth()) return;
				victim.healHealthFraction(1/16.0);
				b.addMessage(victim.getName() + "'s HP was restored by its " + this.name + "!", victim.getHP(), victim.user());
			}
			else if (!victim.hasAbility("Magic Guard"))
			{
				b.addMessage(victim.getName() + " lost some of its HP due to its " + this.name + "!");
				victim.reduceHealthFraction(b, 1/8.0);
			}
		}
	}

	private static class BrightPowder extends Item implements HoldItem, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public BrightPowder()
		{
			super("Bright Powder", "An item to be held by a Pok\u00e9mon. It casts a tricky glare that lowers the opponent's accuracy.", BagCategory.MISC, 11);
			super.price = 100;
		}

		public int flingDamage()
		{
			return 30;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (s == Stat.EVASION) stat *= 1.1;
			return stat;
		}
	}

	private static class CellBattery extends Item implements HoldItem, ConsumedItem, TakeDamageEffect
	{
		private static final long serialVersionUID = 1L;

		public CellBattery()
		{
			super("Cell Battery", "A consumable battery. If the holder is hit by an Electric-type move, its Attack will rise.", BagCategory.MISC, 12);
			super.price = 200;
		}

		public Stat toIncrease()
		{
			return Stat.ATTACK;
		}

		public Type damageType()
		{
			return Type.ELECTRIC;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().getType(b, user) == damageType() && victim.getAttributes().modifyStage(victim, victim, 1, toIncrease(), b, CastSource.HELD_ITEM))
			{
				victim.consumeItem(b);
			}
		}
	}

	private static class ChoiceBand extends Item implements StatChangingEffect, AttackSelectionEffect, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public ChoiceBand()
		{
			super("Choice Band", "An item to be held by a Pok\u00e9mon. This headband ups Attack, but allows the use of only one of its moves.", BagCategory.MISC, 13);
			super.price = 100;
		}

		public Stat toIncrease()
		{
			return Stat.ATTACK;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (s == toIncrease())
			{
				stat *= 1.5;
			}
			
			return stat;
		}

		public boolean usable(ActivePokemon p, Move m)
		{
			Move last = p.getAttributes().getLastMoveUsed();
			if (last == null || m == last)
			{
				return true;
			}
			
			return false;
		}

		public String getUnusableMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + super.name + " only allows " + p.getAttributes().getLastMoveUsed().getAttack().getName() + " to be used!";
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class ChoiceScarf extends Item implements StatChangingEffect, AttackSelectionEffect, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public ChoiceScarf()
		{
			super("Choice Scarf", "An item to be held by a Pok\u00e9mon. This scarf boosts Speed, but allows the use of only one of its moves.", BagCategory.MISC, 14);
			super.price = 200;
		}

		public Stat toIncrease()
		{
			return Stat.SPEED;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (s == toIncrease())
			{
				stat *= 1.5;
			}
			
			return stat;
		}

		public boolean usable(ActivePokemon p, Move m)
		{
			Move last = p.getAttributes().getLastMoveUsed();
			if (last == null || m == last)
			{
				return true;
			}
			
			return false;
		}

		public String getUnusableMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + super.name + " only allows " + p.getAttributes().getLastMoveUsed().getAttack().getName() + " to be used!";
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class ChoiceSpecs extends Item implements StatChangingEffect, AttackSelectionEffect, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public ChoiceSpecs()
		{
			super("Choice Specs", "An item to be held by a Pok\u00e9mon. These distinctive glasses boost Sp. Atk but allow the use of only one of its moves.", BagCategory.MISC, 15);
			super.price = 200;
		}

		public Stat toIncrease()
		{
			return Stat.SP_ATTACK;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (s == toIncrease())
			{
				stat *= 1.5;
			}
			
			return stat;
		}

		public boolean usable(ActivePokemon p, Move m)
		{
			Move last = p.getAttributes().getLastMoveUsed();
			if (last == null || m == last)
			{
				return true;
			}
			
			return false;
		}

		public String getUnusableMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + super.name + " only allows " + p.getAttributes().getLastMoveUsed().getAttack().getName() + " to be used!";
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class CleanseTag extends Item implements HoldItem, RepellingEffect
	{
		private static final long serialVersionUID = 1L;

		public CleanseTag()
		{
			super("Cleanse Tag", "An item to be held by a Pok\u00e9mon. It helps keep wild Pok\u00e9mon away if the holder is the first one in the party.", BagCategory.MISC, 16);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}

		public double chance()
		{
			return .33;
		}
	}

	private static class DampRock extends Item implements HoldItem, WeatherExtendingEffect
	{
		private static final long serialVersionUID = 1L;

		public DampRock()
		{
			super("Damp Rock", "A Pok\u00e9mon held item that extends the duration of the move Rain Dance used by the holder.", BagCategory.MISC, 17);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 60;
		}

		public WeatherType getWeatherType()
		{
			return WeatherType.RAINING;
		}
	}

	private static class HeatRock extends Item implements HoldItem, WeatherExtendingEffect
	{
		private static final long serialVersionUID = 1L;

		public HeatRock()
		{
			super("Heat Rock", "A Pok\u00e9mon held item that extends the duration of the move Sunny Day used by the holder.", BagCategory.MISC, 18);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 60;
		}

		public WeatherType getWeatherType()
		{
			return WeatherType.SUNNY;
		}
	}

	private static class IcyRock extends Item implements HoldItem, WeatherExtendingEffect
	{
		private static final long serialVersionUID = 1L;

		public IcyRock()
		{
			super("Icy Rock", "A Pok\u00e9mon held item that extends the duration of the move Hail used by the holder.", BagCategory.MISC, 19);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 40;
		}

		public WeatherType getWeatherType()
		{
			return WeatherType.HAILING;
		}
	}

	private static class SmoothRock extends Item implements HoldItem, WeatherExtendingEffect
	{
		private static final long serialVersionUID = 1L;

		public SmoothRock()
		{
			super("Smooth Rock", "A Pok\u00e9mon held item that extends the duration of the move Sandstorm used by the holder.", BagCategory.MISC, 20);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 10;
		}

		public WeatherType getWeatherType()
		{
			return WeatherType.SANDSTORM;
		}
	}

	private static class EjectButton extends Item implements HoldItem, TakeDamageEffect
	{
		private static final long serialVersionUID = 1L;

		public EjectButton()
		{
			super("Eject Button", "If the holder is hit by an attack, it will switch with another Pok\u00e9mon in your party.", BagCategory.MISC, 21);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Team t = b.getTrainer(victim.user());
			if (t instanceof WildPokemon) return;
			
			Trainer trainer = (Trainer)t;
			if (!trainer.hasRemainingPokemon()) return;
			
			b.addMessage(victim.getName() + "'s " + this.name + " sent it back to " + trainer.getName() + "!");
			victim.consumeItem(b);
			trainer.switchToRandom();
			trainer.setAction(Action.SWITCH);
			victim = trainer.front();
			b.enterBattle(victim, victim.getName() + " was sent out!");
		}
	}

	private static class DestinyKnot extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public DestinyKnot()
		{
			super("Destiny Knot", "A long, thin, bright-red string to be held by a Pok\u00e9mon. If the holder becomes infatuated, the foe does too.", BagCategory.MISC, 22);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class ExpertBelt extends Item implements HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public ExpertBelt()
		{
			super("Expert Belt", "An item to be held by a Pok\u00e9mon. It is a well-worn belt that slightly boosts the power of supereffective moves.", BagCategory.MISC, 23);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 10;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return Type.getAdvantage(user.getAttack().getType(b, user), victim, b) > 1 ? 1.2 : 1;
		}
	}

	private static class FlameOrb extends Item implements HoldItem, EndTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public FlameOrb()
		{
			super("Flame Orb", "An item to be held by a Pok\u00e9mon. It is a bizarre orb that inflicts a burn on the holder in battle.", BagCategory.MISC, 24);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void apply(ActivePokemon victim, Battle b)
		{
			Status.giveStatus(b, victim, victim, StatusCondition.BURNED, victim.getName() + " was burned by its " + this.name + "!");
		}
	}

	private static class ToxicOrb extends Item implements HoldItem, EndTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public ToxicOrb()
		{
			super("Toxic Orb", "An item to be held by a Pok\u00e9mon. It is a bizarre orb that inflicts a burn on the holder in battle.", BagCategory.MISC, 25);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void apply(ActivePokemon victim, Battle b)
		{
			if (Status.applies(StatusCondition.POISONED, b, victim, victim))
			{
				victim.addEffect(PokemonEffect.getEffect("BadPoison").newInstance());
				Status.giveStatus(b, victim, victim, StatusCondition.POISONED, victim.getName() + " was badly poisoned by its " + this.name + "!");
			}
		}
	}

	private static class FloatStone extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public FloatStone()
		{
			super("Float Stone", "This item, when attached to a Pok\u00e9mon, halves the Pok\u00e9mon's weight for use with attacks that deal with weight", BagCategory.MISC, 26);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class FocusBand extends Item implements HoldItem, BracingEffect
	{
		private static final long serialVersionUID = 1L;

		public FocusBand()
		{
			super("Focus Band", "An item to be held by a Pok\u00e9mon. The holder may endure a potential KO attack, leaving it with just 1 HP.", BagCategory.MISC, 27);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 10;
		}

		public boolean isBracing(Battle b, ActivePokemon bracer, Boolean fullHealth)
		{
			return Math.random()*100 < 10;
		}

		public String braceMessage(ActivePokemon bracer)
		{
			return bracer.getName() + " held on with its " + this.name + "!";
		}
	}

	private static class FocusSash extends Item implements HoldItem, ConsumedItem, BracingEffect
	{
		private static final long serialVersionUID = 1L;

		public FocusSash()
		{
			super("Focus Sash", "An item to be held by a Pok\u00e9mon. If it has full HP, the holder will endure one potential KO attack, leaving 1 HP.", BagCategory.MISC, 28);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 10;
		}

		public boolean isBracing(Battle b, ActivePokemon bracer, Boolean fullHealth)
		{
			if (fullHealth)
			{
				bracer.consumeItem(b);
				return true;
			}
			return false;
		}

		public String braceMessage(ActivePokemon bracer)
		{
			return bracer.getName() + " held on with its " + this.name + "!";
		}
	}

	private static class GripClaw extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public GripClaw()
		{
			super("Grip Claw", "A Pok\u00e9mon held item that extends the duration of multiturn attacks like Bind and Wrap.", BagCategory.MISC, 29);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 90;
		}
	}

	private static class GriseousOrb extends Item implements HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public GriseousOrb()
		{
			super("Griseous Orb", "A glowing orb to be held by Giratina. It boosts the power of Dragon- and Ghost-type moves.", BagCategory.MISC, 30);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 60;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isPokemon("Giratina"))
			{
				Type t = user.getAttack().getType(b, user);
				if (t == Type.DRAGON || t == Type.GHOST) return 1.2;
			}
			return 1;
		}
	}

	private static class IronBall extends Item implements HoldItem, GroundedEffect, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public IronBall()
		{
			super("Iron Ball", "A Pok\u00e9mon held item that cuts Speed. It makes Flying-type and levitating holders susceptible to Ground moves.", BagCategory.MISC, 31);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 130;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (s == Stat.SPEED) stat *= .5;
			return stat;
		}
	}

	private static class LaggingTail extends Item implements HoldItem, StallingEffect
	{
		private static final long serialVersionUID = 1L;

		public LaggingTail()
		{
			super("Lagging Tail", "An item to be held by a Pok\u00e9mon. It is tremendously heavy and makes the holder move slower than usual.", BagCategory.MISC, 32);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class LifeOrb extends Item implements HoldItem, PowerChangeEffect, ApplyDamageEffect
	{
		private static final long serialVersionUID = 1L;

		public LifeOrb()
		{
			super("Life Orb", "An item to be held by a Pok\u00e9mon. It boosts the power of moves, but at the cost of some HP on each hit.", BagCategory.MISC, 33);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return 5324.0/4096.0;
		}

		public void applyEffect(Battle b, ActivePokemon user, ActivePokemon victim, Integer damage)
		{
			if (user.hasAbility("Magic Guard")) return;
			b.addMessage(user.getName() + " was hurt by its " + this.name + "!");
			user.reduceHealthFraction(b, .1);
		}
	}

	private static class LightBall extends Item implements HoldItem, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public LightBall()
		{
			super("Light Ball", "An item to be held by Pikachu. It is a puzzling orb that raises the Attack and Sp. Atk stat.", BagCategory.MISC, 34);
			super.price = 100;
		}

		public int flingDamage()
		{
			return 30;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if ((s == Stat.ATTACK || s == Stat.SP_ATTACK) && p.isPokemon("Pikachu")) stat *= 2;
			return stat;
		}
	}

	private static class LightClay extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public LightClay()
		{
			super("Light Clay", "A Pok\u00e9mon held item that extends the duration of barrier moves like Light Screen and Reflect used by the holder.", BagCategory.MISC, 35);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class LuckyEgg extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public LuckyEgg()
		{
			super("Lucky Egg", "An item to be held by a Pok\u00e9mon. It is an egg filled with happiness that earns extra Exp. Points in battle.", BagCategory.MISC, 36);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class LuckyPunch extends Item implements HoldItem, CritStageEffect
	{
		private static final long serialVersionUID = 1L;

		public LuckyPunch()
		{
			super("Lucky Punch", "An item to be held by Chansey. It is a pair of gloves that boosts Chansey's critical-hit ratio.", BagCategory.MISC, 37);
			super.price = 10;
		}

		public int flingDamage()
		{
			return 40;
		}

		public int increaseCritStage(ActivePokemon p)
		{
			if (p.isPokemon("Chansey")) return 2;
			return 0;
		}
	}

	private static class MachoBrace extends Item implements PowerItem
	{
		private static final long serialVersionUID = 1L;

		public MachoBrace()
		{
			super("Macho Brace", "An item to be held by a Pok\u00e9mon. It is a stiff and heavy brace that promotes strong growth but lowers Speed.", BagCategory.MISC, 38);
			super.price = 3000;
		}

		public int[] getEVs(int[] vals)
		{
			for (int i = 0; i < vals.length; i++)
			{
				vals[i] *= 2;
			}
			
			return vals;
		}

		public int modify(Integer stat, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			if (s.equals(Stat.SPEED))
			{
				return stat / 2;
			}
			
			return stat;
		}

		public Stat toIncrease()
		{
			Global.error("toIncrease() method in " + super.name +" is unimplemented.");
			return null;
		}

		public int flingDamage()
		{
			return 60;
		}
	}

	private static class MentalHerb extends Item implements HoldItem, EndTurnEffect
	{
		private static final long serialVersionUID = 1L;
		String[] effects = {"Infatuated", "Disable", "Taunt", "Encore", "Torment", "Confusion"};
		String[] messages = {"infatuated", "disabled", "under the effects of taunt", "under the effects of encore", "under the effects of torment", "confused"};

		public MentalHerb()
		{
			super("Mental Herb", "An item to be held by a Pok\u00e9mon. It snaps the holder out of infatuation. It can be used only once.", BagCategory.MISC, 39);
			super.price = 100;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void apply(ActivePokemon victim, Battle b)
		{
			boolean used = false;
			for (int i = 0; i < effects.length; i++)
			{
				String s = effects[i];
				if (victim.hasEffect(s))
				{
					used = true;
					victim.getAttributes().removeEffect(s);
					b.addMessage(victim.getName() + " is no longer " + messages[i] + " due to its " + this.name + "!");
				}
			}
			if (used) victim.consumeItem(b);
		}
	}

	private static class MetalPowder extends Item implements HoldItem, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public MetalPowder()
		{
			super("Metal Powder", "When this item is held by a Ditto, the holder's initial Defence & Special Defence stats are increased by 50%", BagCategory.MISC, 40);
			super.price = 10;
		}

		public int flingDamage()
		{
			return 10;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if ((s == Stat.DEFENSE || s == Stat.SP_DEFENSE) && p.isPokemon("Ditto")) stat *= 1.5;
			return stat;
		}
	}

	private static class Metronome extends Item implements HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public Metronome()
		{
			super("Metronome", "A Pok\u00e9mon held item that boosts a move used consecutively. Its effect is reset if another move is used.", BagCategory.MISC, 41);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return Math.min(2, 1 + .2*(user.getAttributes().getCount() - 1));
		}
	}

	private static class MuscleBand extends Item implements HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public MuscleBand()
		{
			super("Muscle Band", "An item to be held by a Pok\u00e9mon. It is a headband that slightly boosts the power of physical moves.", BagCategory.MISC, 42);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 10;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getAttack().getCategory() == Category.PHYSICAL ? 1.1 : 1;
		}
	}

	private static class PowerAnklet extends Item implements PowerItem
	{
		private static final long serialVersionUID = 1L;

		public PowerAnklet()
		{
			super("Power Anklet", "A Pok\u00e9mon held item that promotes Speed gain on leveling, but reduces the Speed stat.", BagCategory.MISC, 43);
			super.price = 3000;
		}

		public int[] getEVs(int[] vals)
		{
			vals[toIncrease().index()] += 4;
			return vals;
		}

		public int modify(Integer stat, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			if (s.equals(Stat.SPEED))
			{
				return stat / 2;
			}
			
			return stat;
		}

		public Stat toIncrease()
		{
			return Stat.SPEED;
		}

		public int flingDamage()
		{
			return 70;
		}
	}

	private static class PowerBand extends Item implements PowerItem
	{
		private static final long serialVersionUID = 1L;

		public PowerBand()
		{
			super("Power Band", "A Pok\u00e9mon held item that promotes Sp. Def gain on leveling, but reduces the Speed stat.", BagCategory.MISC, 44);
			super.price = 3000;
		}

		public int[] getEVs(int[] vals)
		{
			vals[toIncrease().index()] += 4;
			return vals;
		}

		public int modify(Integer stat, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			if (s.equals(Stat.SPEED))
			{
				return stat / 2;
			}
			
			return stat;
		}

		public Stat toIncrease()
		{
			return Stat.SP_DEFENSE;
		}

		public int flingDamage()
		{
			return 70;
		}
	}

	private static class PowerBelt extends Item implements PowerItem
	{
		private static final long serialVersionUID = 1L;

		public PowerBelt()
		{
			super("Power Belt", "A Pok\u00e9mon held item that promotes Def gain on leveling, but reduces the Speed stat.", BagCategory.MISC, 45);
			super.price = 3000;
		}

		public int[] getEVs(int[] vals)
		{
			vals[toIncrease().index()] += 4;
			return vals;
		}

		public int modify(Integer stat, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			if (s.equals(Stat.SPEED))
			{
				return stat / 2;
			}
			
			return stat;
		}

		public Stat toIncrease()
		{
			return Stat.DEFENSE;
		}

		public int flingDamage()
		{
			return 70;
		}
	}

	private static class PowerBracer extends Item implements PowerItem
	{
		private static final long serialVersionUID = 1L;

		public PowerBracer()
		{
			super("Power Bracer", "A Pok\u00e9mon held item that promotes Att gain on leveling, but reduces the Speed stat.", BagCategory.MISC, 46);
			super.price = 3000;
		}

		public int[] getEVs(int[] vals)
		{
			vals[toIncrease().index()] += 4;
			return vals;
		}

		public int modify(Integer stat, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			if (s.equals(Stat.SPEED))
			{
				return stat / 2;
			}
			
			return stat;
		}

		public Stat toIncrease()
		{
			return Stat.ATTACK;
		}

		public int flingDamage()
		{
			return 70;
		}
	}

	private static class PowerLens extends Item implements PowerItem
	{
		private static final long serialVersionUID = 1L;

		public PowerLens()
		{
			super("Power Lens", "A Pok\u00e9mon held item that promotes Sp. Att gain on leveling, but reduces the Speed stat.", BagCategory.MISC, 47);
			super.price = 3000;
		}

		public int[] getEVs(int[] vals)
		{
			vals[toIncrease().index()] += 4;
			return vals;
		}

		public int modify(Integer stat, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			if (s.equals(Stat.SPEED))
			{
				return stat / 2;
			}
			
			return stat;
		}

		public Stat toIncrease()
		{
			return Stat.SP_ATTACK;
		}

		public int flingDamage()
		{
			return 70;
		}
	}

	private static class PowerWeight extends Item implements PowerItem
	{
		private static final long serialVersionUID = 1L;

		public PowerWeight()
		{
			super("Power Weight", "A Pok\u00e9mon held item that promotes HP gain on leveling, but reduces the Speed stat.", BagCategory.MISC, 48);
			super.price = 3000;
		}

		public int[] getEVs(int[] vals)
		{
			vals[toIncrease().index()] += 4;
			return vals;
		}

		public int modify(Integer stat, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			if (s.equals(Stat.SPEED))
			{
				return stat / 2;
			}
			
			return stat;
		}

		public Stat toIncrease()
		{
			return Stat.HP;
		}

		public int flingDamage()
		{
			return 70;
		}
	}

	private static class QuickClaw extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public QuickClaw()
		{
			super("Quick Claw", "An item to be held by a Pok\u00e9mon. A light, sharp claw that lets the bearer move first occasionally.", BagCategory.MISC, 49);
			super.price = 100;
		}

		public int flingDamage()
		{
			return 80;
		}
	}

	private static class QuickPowder extends Item implements HoldItem, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public QuickPowder()
		{
			super("Quick Powder", "An item to be held by Ditto. Extremely fine yet hard, this odd powder boosts the Speed stat.", BagCategory.MISC, 50);
			super.price = 10;
		}

		public int flingDamage()
		{
			return 10;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (s == Stat.SPEED && p.isPokemon("Ditto")) stat *= 1.5;
			return stat;
		}
	}

	private static class RedCard extends Item implements HoldItem, TakeDamageEffect
	{
		private static final long serialVersionUID = 1L;

		public RedCard()
		{
			super("Red Card", "A card with a mysterious power. When the holder is struck by a foe, the attacker is removed from battle.", BagCategory.MISC, 51);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Team t = b.getTrainer(user.user());
			if (t instanceof WildPokemon) return;
			
			Trainer trainer = (Trainer)t;
			if (!trainer.hasRemainingPokemon()) return;
			
			b.addMessage(victim.getName() + "'s " + this.name + " sent " + user.getName() + " back to " + trainer.getName() + "!");
			victim.consumeItem(b);
			trainer.switchToRandom();
			trainer.setAction(Action.SWITCH);
			user = trainer.front();
			b.enterBattle(user, user.getName() + " was sent out!");
		}
	}

	private static class RingTarget extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public RingTarget()
		{
			super("Ring Target", "Moves that would otherwise have no effect will land on the Pok\u00e9mon that holds it.", BagCategory.MISC, 52);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class RockyHelmet extends Item implements HoldItem, PhysicalContactEffect
	{
		private static final long serialVersionUID = 1L;

		public RockyHelmet()
		{
			super("Rocky Helmet", "If the holder of this item takes damage, the attacker will also be damaged upon contact.", BagCategory.MISC, 53);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 60;
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			b.addMessage(user.getName() + " was hurt by " + victim.getName() + "'s " + this.name + "!");
			user.reduceHealthFraction(b, 1/8.0);
		}
	}

	private static class SafetyGoggles extends Item implements HoldItem, EffectBlockerEffect, WeatherBlockerEffect
	{
		private static final long serialVersionUID = 1L;

		public SafetyGoggles()
		{
			super("Safety Goggles", "An item to be held by a Pok\u00e9mon. These goggles protect the holder from both weather-related damage and powder.", BagCategory.MISC, 54);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}

		public boolean validMove(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (!user.getAttack().isMoveType("Powder")) return true;
			if (user.getAttack().getCategory() == Category.STATUS) b.addMessage(victim.getName() + "'s " + this.name + " protects it from powder moves!");
			return false;
		}

		public boolean block(WeatherType weather)
		{
			return true;
		}
	}

	private static class ScopeLens extends Item implements HoldItem, CritStageEffect
	{
		private static final long serialVersionUID = 1L;

		public ScopeLens()
		{
			super("Scope Lens", "An item to be held by a Pok\u00e9mon. It is a lens that boosts the holder's critical-hit ratio.", BagCategory.MISC, 55);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}

		public int increaseCritStage(ActivePokemon p)
		{
			return 1;
		}
	}

	private static class ShedShell extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public ShedShell()
		{
			super("Shed Shell", "A tough, discarded carapace to be held by a Pok\u00e9mon. It enables the holder to switch with a waiting Pok\u00e9mon in battle.", BagCategory.MISC, 56);
			super.price = 100;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class ShellBell extends Item implements HoldItem, ApplyDamageEffect
	{
		private static final long serialVersionUID = 1L;

		public ShellBell()
		{
			super("Shell Bell", "An item to be held by a Pok\u00e9mon. The holder's HP is restored a little every time it inflicts damage.", BagCategory.MISC, 57);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void applyEffect(Battle b, ActivePokemon user, ActivePokemon victim, Integer damage)
		{
			if (user.fullHealth()) return;
			user.heal((int)Math.ceil(damage/8.0));
			// TODO: This looks really bad when paired with Explosion
			b.addMessage(user.getName() + " restored some HP due to its " + this.name + "!", user.getHP(), user.user());
		}
	}

	private static class SmokeBall extends Item implements HoldItem, DefiniteEscape
	{
		private static final long serialVersionUID = 1L;

		public SmokeBall()
		{
			super("Smoke Ball", "An item to be held by a Pok\u00e9mon. It enables the holder to flee from any wild Pok\u00e9mon without fail.", BagCategory.MISC, 58);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class Snowball extends Item implements HoldItem, ConsumedItem, TakeDamageEffect
	{
		private static final long serialVersionUID = 1L;

		public Snowball()
		{
			super("Snowball", "An item to be held by a Pok\u00e9mon. It boosts Attack if hit with an Ice-type attack. It can only be used once.", BagCategory.MISC, 59);
			super.price = 200;
		}

		public Stat toIncrease()
		{
			return Stat.ATTACK;
		}

		public Type damageType()
		{
			return Type.ICE;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().getType(b, user) == damageType() && victim.getAttributes().modifyStage(victim, victim, 1, toIncrease(), b, CastSource.HELD_ITEM))
			{
				victim.consumeItem(b);
			}
		}
	}

	private static class SoulDew extends Item implements HoldItem, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public SoulDew()
		{
			super("Soul Dew", "If the Soul Dew is attached to Latios or Latias, the holder's Special Attack and Special Defence is increased by 50%.", BagCategory.MISC, 60);
			super.price = 10;
		}

		public int flingDamage()
		{
			return 10;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if ((s == Stat.SP_ATTACK || s == Stat.SP_DEFENSE) && (p.isPokemon("Latios") || p.isPokemon("Latias"))) stat *= 1.5;
			return stat;
		}
	}

	private static class Stick extends Item implements HoldItem, CritStageEffect
	{
		private static final long serialVersionUID = 1L;

		public Stick()
		{
			super("Stick", "An item to be held by Farfetch'd. It is a very long and stiff stalk of leek that boosts the critical-hit ratio.", BagCategory.MISC, 61);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 60;
		}

		public int increaseCritStage(ActivePokemon p)
		{
			if (p.isPokemon("Farfetch'd")) return 2;
			return 0;
		}
	}

	private static class StickyBarb extends Item implements HoldItem, EndTurnEffect, PhysicalContactEffect, ItemCondition
	{
		private static final long serialVersionUID = 1L;
		private Item item;

		public StickyBarb()
		{
			super("Sticky Barb", "A held item that damages the holder on every turn. It may latch on to foes and allies that touch the holder.", BagCategory.MISC, 62);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 80;
		}

		public void apply(ActivePokemon victim, Battle b)
		{
			if (victim.hasAbility("Magic Guard")) return;
			b.addMessage(victim.getName() + " was hurt by its " + this.name + "!");
			victim.reduceHealthFraction(b, 1/8.0);
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (!user.hasAbility("Magic Guard"))
			{
				b.addMessage(user.getName() + " was hurt by " + victim.getName() + "'s " + this.name + "!");
				user.reduceHealthFraction(b, 1/8.0);
			}
			
			if (user.isHoldingItem(b) || user.isFainted(b)) return;
			b.addMessage(victim.getName() + "s " + this.name + " latched onto " + user.getName() + "!");
			
			if (b.isWildBattle())
			{
				victim.removeItem();
				user.giveItem(this);
				return;
			}
			
			item = this;
			PokemonEffect.getEffect("ChangeItem").cast(b, victim, user, CastSource.HELD_ITEM, false);
			item = Item.noneItem();
			PokemonEffect.getEffect("ChangeItem").cast(b, victim, victim, CastSource.HELD_ITEM, false);
		}

		public Item getItem()
		{
			return item;
		}
	}

	private static class ThickClub extends Item implements HoldItem, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public ThickClub()
		{
			super("Thick Club", "An item to be held by Cubone or Marowak. It is a hard bone of some sort that boosts the Attack stat.", BagCategory.MISC, 63);
			super.price = 500;
		}

		public int flingDamage()
		{
			return 90;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (s == Stat.ATTACK && (p.isPokemon("Cubone") || p.isPokemon("Marowak"))) stat *= 2;
			return stat;
		}
	}

	private static class WeaknessPolicy extends Item implements HoldItem, TakeDamageEffect
	{
		private static final long serialVersionUID = 1L;

		public WeaknessPolicy()
		{
			super("Weakness Policy", "An item to be held by a Pok\u00e9mon. Attack and Sp. Atk sharply increase if the holder is hit with a move it's weak to.", BagCategory.MISC, 64);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (Type.getAdvantage(user.getAttack().getType(b, user), victim, b) > 1)
			{
				victim.getAttributes().modifyStage(victim, victim, 2, Stat.ATTACK, b, CastSource.HELD_ITEM);
				victim.getAttributes().modifyStage(victim, victim, 2, Stat.SP_ATTACK, b, CastSource.HELD_ITEM);
			}
		}
	}

	private static class WhiteHerb extends Item implements HoldItem, StatProtectingEffect
	{
		private static final long serialVersionUID = 1L;

		public WhiteHerb()
		{
			super("White Herb", "An item to be held by a Pok\u00e9mon. It restores any lowered stat in battle. It can be used only once.", BagCategory.MISC, 65);
			super.price = 100;
		}

		public int flingDamage()
		{
			return 10;
		}

		public boolean prevent(ActivePokemon caster, Stat stat)
		{
			return true;
		}

		public String preventionMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + this.name + " prevented its stats from being lowered!";
		}
	}

	private static class WideLens extends Item implements HoldItem, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public WideLens()
		{
			super("Wide Lens", "An item to be held by a Pok\u00e9mon. It is a magnifying lens that slightly boosts the accuracy of moves.", BagCategory.MISC, 66);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 10;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (s == Stat.ACCURACY) stat *= 1.1;
			return stat;
		}
	}

	private static class WiseGlasses extends Item implements HoldItem, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public WiseGlasses()
		{
			super("Wise Glasses", "An item to be held by a Pok\u00e9mon. It is a thick pair of glasses that slightly boosts the power of special moves.", BagCategory.MISC, 67);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 10;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (s == Stat.SP_ATTACK) stat *= 1.1;
			return stat;
		}
	}

	private static class ZoomLens extends Item implements HoldItem, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public ZoomLens()
		{
			super("Zoom Lens", "An item to be held by a Pok\u00e9mon. If the holder moves after its target, its accuracy will be boosted.", BagCategory.MISC, 68);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 10;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (s == Stat.ACCURACY && !b.isFirstAttack()) stat *= 1.2;
			return stat;
		}
	}

	private static class FullIncense extends Item implements HoldItem, StallingEffect
	{
		private static final long serialVersionUID = 1L;

		public FullIncense()
		{
			super("Full Incense", "An item to be held by a Pok\u00e9mon. It is an exotic-smelling incense that makes the holder bloated and slow moving.", BagCategory.MISC, 69);
			super.price = 9600;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class LaxIncense extends Item implements HoldItem, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public LaxIncense()
		{
			super("Lax Incense", "An item to be held by a Pok\u00e9mon. The tricky aroma of this incense may make attacks miss the holder.", BagCategory.MISC, 70);
			super.price = 9600;
		}

		public int flingDamage()
		{
			return 10;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (s == Stat.EVASION) stat *= 1.1;
			return stat;
		}
	}

	private static class LuckIncense extends Item implements HoldItem, EntryEffect
	{
		private static final long serialVersionUID = 1L;

		public LuckIncense()
		{
			super("Luck Incense", "An item to be held by a Pok\u00e9mon. It doubles a battle's prize money if the holding Pok\u00e9mon joins in.", BagCategory.MISC, 71);
			super.price = 9600;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void enter(Battle b, ActivePokemon victim)
		{
			TeamEffect.getEffect("DoubleMoney").cast(b, victim, victim, CastSource.HELD_ITEM, false);
		}
	}

	private static class OddIncense extends Item implements PowerChangeEffect, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public OddIncense()
		{
			super("Odd Incense", "An item to be held by a Pok\u00e9mon. It is an exotic-smelling incense that boosts the power of Psychic-type moves.", BagCategory.MISC, 72);
			super.price = 9600;
		}

		public Type getType()
		{
			return Type.PSYCHIC;
		}

		public double getMultiplier()
		{
			return 1.2;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class PureIncense extends Item implements HoldItem, RepellingEffect
	{
		private static final long serialVersionUID = 1L;

		public PureIncense()
		{
			super("Pure Incense", "An item to be held by a Pok\u00e9mon. It helps keep wild Pok\u00e9mon away if the holder is the first one in the party.", BagCategory.MISC, 73);
			super.price = 9600;
		}

		public int flingDamage()
		{
			return 10;
		}

		public double chance()
		{
			return .33;
		}
	}

	private static class RockIncense extends Item implements PowerChangeEffect, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public RockIncense()
		{
			super("Rock Incense", "An item to be held by a Pok\u00e9mon. It is an exotic-smelling incense that boosts the power of Rock-type moves.", BagCategory.MISC, 74);
			super.price = 9600;
		}

		public Type getType()
		{
			return Type.ROCK;
		}

		public double getMultiplier()
		{
			return 1.2;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class RoseIncense extends Item implements PowerChangeEffect, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public RoseIncense()
		{
			super("Rose Incense", "An item to be held by a Pok\u00e9mon. It is an exotic-smelling incense that boosts the power of Grass-type moves.", BagCategory.MISC, 75);
			super.price = 9600;
		}

		public Type getType()
		{
			return Type.GRASS;
		}

		public double getMultiplier()
		{
			return 1.2;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class SeaIncense extends Item implements PowerChangeEffect, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public SeaIncense()
		{
			super("Sea Incense", "An item to be held by a Pok\u00e9mon. It is incense with a curious aroma that boosts the power of Water-type moves.", BagCategory.MISC, 76);
			super.price = 9600;
		}

		public Type getType()
		{
			return Type.WATER;
		}

		public double getMultiplier()
		{
			return 1.2;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class WaveIncense extends Item implements PowerChangeEffect, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public WaveIncense()
		{
			super("Wave Incense", "An item to be held by a Pok\u00e9mon. It is incense with a curious aroma that boosts the power of Water-type moves.", BagCategory.MISC, 77);
			super.price = 9600;
		}

		public Type getType()
		{
			return Type.WATER;
		}

		public double getMultiplier()
		{
			return 1.2;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class DracoPlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public DracoPlate()
		{
			super("Draco Plate", "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Dragon-type moves.", BagCategory.MISC, 78);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.DRAGON;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 90;
		}
	}

	private static class DreadPlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public DreadPlate()
		{
			super("Dread Plate", "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Dark-type moves.", BagCategory.MISC, 79);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.DARK;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 90;
		}
	}

	private static class EarthPlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public EarthPlate()
		{
			super("Earth Plate", "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Ground-type moves.", BagCategory.MISC, 80);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.GROUND;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 90;
		}
	}

	private static class FistPlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public FistPlate()
		{
			super("Fist Plate", "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Fighting-type moves.", BagCategory.MISC, 81);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.FIGHTING;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 90;
		}
	}

	private static class FlamePlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public FlamePlate()
		{
			super("Flame Plate", "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Fire-type moves.", BagCategory.MISC, 82);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.FIRE;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 90;
		}
	}

	private static class IciclePlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public IciclePlate()
		{
			super("Icicle Plate", "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Ice-type moves.", BagCategory.MISC, 83);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.ICE;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 90;
		}
	}

	private static class InsectPlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public InsectPlate()
		{
			super("Insect Plate", "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Bug-type moves.", BagCategory.MISC, 84);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.BUG;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 90;
		}
	}

	private static class IronPlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public IronPlate()
		{
			super("Iron Plate", "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Steel-type moves.", BagCategory.MISC, 85);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.STEEL;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 90;
		}
	}

	private static class MeadowPlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public MeadowPlate()
		{
			super("Meadow Plate", "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Grass-type moves.", BagCategory.MISC, 86);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.GRASS;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 90;
		}
	}

	private static class MindPlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public MindPlate()
		{
			super("Mind Plate", "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Psychic-type moves.", BagCategory.MISC, 87);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.PSYCHIC;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 90;
		}
	}

	private static class SkyPlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public SkyPlate()
		{
			super("Sky Plate", "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Flying-type moves.", BagCategory.MISC, 88);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.FLYING;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 90;
		}
	}

	private static class SplashPlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public SplashPlate()
		{
			super("Splash Plate", "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Water-type moves.", BagCategory.MISC, 89);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.WATER;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 90;
		}
	}

	private static class SpookyPlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public SpookyPlate()
		{
			super("Spooky Plate", "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Ghost-type moves.", BagCategory.MISC, 90);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.GHOST;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 90;
		}
	}

	private static class StonePlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public StonePlate()
		{
			super("Stone Plate", "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Rock-type moves.", BagCategory.MISC, 91);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.ROCK;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 90;
		}
	}

	private static class ToxicPlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public ToxicPlate()
		{
			super("Toxic Plate", "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Poison-type moves.", BagCategory.MISC, 92);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.POISON;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 90;
		}
	}

	private static class ZapPlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public ZapPlate()
		{
			super("Zap Plate", "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Electric-type moves.", BagCategory.MISC, 93);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.ELECTRIC;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 90;
		}
	}

	private static class BurnDrive extends Item implements DriveItem
	{
		private static final long serialVersionUID = 1L;

		public BurnDrive()
		{
			super("Burn Drive", "A cassette to be held by Genesect. It changes Techno Blast to a Fire-type move.", BagCategory.MISC, 94);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.FIRE;
		}

		public int flingDamage()
		{
			return 70;
		}
	}

	private static class ChillDrive extends Item implements DriveItem
	{
		private static final long serialVersionUID = 1L;

		public ChillDrive()
		{
			super("Chill Drive", "A cassette to be held by Genesect. It changes Techno Blast to an Ice-type move.", BagCategory.MISC, 95);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.ICE;
		}

		public int flingDamage()
		{
			return 70;
		}
	}

	private static class DouseDrive extends Item implements DriveItem
	{
		private static final long serialVersionUID = 1L;

		public DouseDrive()
		{
			super("Douse Drive", "A cassette to be held by Genesect. It changes Techno Blast to a Water-type move.", BagCategory.MISC, 96);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.WATER;
		}

		public int flingDamage()
		{
			return 70;
		}
	}

	private static class ShockDrive extends Item implements DriveItem
	{
		private static final long serialVersionUID = 1L;

		public ShockDrive()
		{
			super("Shock Drive", "A cassette to be held by Genesect. It changes Techno Blast to an Electric-type move.", BagCategory.MISC, 97);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.ELECTRIC;
		}

		public int flingDamage()
		{
			return 70;
		}
	}

	private static class FireGem extends Item implements PowerChangeEffect, HoldItem, ConsumedItem
	{
		private static final long serialVersionUID = 1L;

		public FireGem()
		{
			super("Fire Gem", "A gem with an essence of fire. When held, it strengthens the power of a Fire-type move only once.", BagCategory.MISC, 98);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.FIRE;
		}

		public double getMultiplier()
		{
			return 1.5;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class WaterGem extends Item implements PowerChangeEffect, HoldItem, ConsumedItem
	{
		private static final long serialVersionUID = 1L;

		public WaterGem()
		{
			super("Water Gem", "A gem with an essence of water. When held, it strengthens the power of a Water-type move only once.", BagCategory.MISC, 99);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.WATER;
		}

		public double getMultiplier()
		{
			return 1.5;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class ElectricGem extends Item implements PowerChangeEffect, HoldItem, ConsumedItem
	{
		private static final long serialVersionUID = 1L;

		public ElectricGem()
		{
			super("Electric Gem", "A gem with an essence of electricity. When held, it strengthens the power of an Electric-type move only once.", BagCategory.MISC, 100);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.ELECTRIC;
		}

		public double getMultiplier()
		{
			return 1.5;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class GrassGem extends Item implements PowerChangeEffect, HoldItem, ConsumedItem
	{
		private static final long serialVersionUID = 1L;

		public GrassGem()
		{
			super("Grass Gem", "A gem with an essence of nature. When held, it strengthens the power of a Grass-type move only once.", BagCategory.MISC, 101);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.GRASS;
		}

		public double getMultiplier()
		{
			return 1.5;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class IceGem extends Item implements PowerChangeEffect, HoldItem, ConsumedItem
	{
		private static final long serialVersionUID = 1L;

		public IceGem()
		{
			super("Ice Gem", "A gem with an essence of ice. When held, it strengthens the power of an Ice-type move only once.", BagCategory.MISC, 102);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.ICE;
		}

		public double getMultiplier()
		{
			return 1.5;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class FightingGem extends Item implements PowerChangeEffect, HoldItem, ConsumedItem
	{
		private static final long serialVersionUID = 1L;

		public FightingGem()
		{
			super("Fighting Gem", "A gem with an essence of combat. When held, it strengthens the power of a Fighting-type move only once.", BagCategory.MISC, 103);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.FIGHTING;
		}

		public double getMultiplier()
		{
			return 1.5;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class PoisonGem extends Item implements PowerChangeEffect, HoldItem, ConsumedItem
	{
		private static final long serialVersionUID = 1L;

		public PoisonGem()
		{
			super("Poison Gem", "A gem with an essence of poison. When held, it strengthens the power of a Poison-type move only once.", BagCategory.MISC, 104);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.POISON;
		}

		public double getMultiplier()
		{
			return 1.5;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class GroundGem extends Item implements PowerChangeEffect, HoldItem, ConsumedItem
	{
		private static final long serialVersionUID = 1L;

		public GroundGem()
		{
			super("Ground Gem", "A gem with an essence of land. When held, it strengthens the power of a Ground-type move only once.", BagCategory.MISC, 105);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.GROUND;
		}

		public double getMultiplier()
		{
			return 1.5;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class FlyingGem extends Item implements PowerChangeEffect, HoldItem, ConsumedItem
	{
		private static final long serialVersionUID = 1L;

		public FlyingGem()
		{
			super("Flying Gem", "A gem with an essence of air. When held, it strengthens the power of a Flying-type move only once.", BagCategory.MISC, 106);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.FLYING;
		}

		public double getMultiplier()
		{
			return 1.5;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class PsychicGem extends Item implements PowerChangeEffect, HoldItem, ConsumedItem
	{
		private static final long serialVersionUID = 1L;

		public PsychicGem()
		{
			super("Psychic Gem", "A gem with an essence of the mind. When held, it strengthens the power of a Psychic-type move only once.", BagCategory.MISC, 107);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.PSYCHIC;
		}

		public double getMultiplier()
		{
			return 1.5;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class BugGem extends Item implements PowerChangeEffect, HoldItem, ConsumedItem
	{
		private static final long serialVersionUID = 1L;

		public BugGem()
		{
			super("Bug Gem", "A gem with an insect-like essence. When held, it strengthens the power of a Bug-type move only once.", BagCategory.MISC, 108);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.BUG;
		}

		public double getMultiplier()
		{
			return 1.5;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class RockGem extends Item implements PowerChangeEffect, HoldItem, ConsumedItem
	{
		private static final long serialVersionUID = 1L;

		public RockGem()
		{
			super("Rock Gem", "A gem with an essence of rock. When held, it strengthens the power of a Rock-type move only once.", BagCategory.MISC, 109);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.ROCK;
		}

		public double getMultiplier()
		{
			return 1.5;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class GhostGem extends Item implements PowerChangeEffect, HoldItem, ConsumedItem
	{
		private static final long serialVersionUID = 1L;

		public GhostGem()
		{
			super("Ghost Gem", "A gem with a spectral essence. When held, it strengthens the power of a Ghost-type move only once.", BagCategory.MISC, 110);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.GHOST;
		}

		public double getMultiplier()
		{
			return 1.5;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class DragonGem extends Item implements PowerChangeEffect, HoldItem, ConsumedItem
	{
		private static final long serialVersionUID = 1L;

		public DragonGem()
		{
			super("Dragon Gem", "A gem with a draconic essence. When held, it strengthens the power of a Dragon-type move only once.", BagCategory.MISC, 111);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.DRAGON;
		}

		public double getMultiplier()
		{
			return 1.5;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class DarkGem extends Item implements PowerChangeEffect, HoldItem, ConsumedItem
	{
		private static final long serialVersionUID = 1L;

		public DarkGem()
		{
			super("Dark Gem", "A gem with an essence of darkness. When held, it strengthens the power of a Dark-type move only once.", BagCategory.MISC, 112);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.DARK;
		}

		public double getMultiplier()
		{
			return 1.5;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class SteelGem extends Item implements PowerChangeEffect, HoldItem, ConsumedItem
	{
		private static final long serialVersionUID = 1L;

		public SteelGem()
		{
			super("Steel Gem", "A gem with an essence of steel. When held, it strengthens the power of a Steel-type move only once.", BagCategory.MISC, 113);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.STEEL;
		}

		public double getMultiplier()
		{
			return 1.5;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class NormalGem extends Item implements PowerChangeEffect, HoldItem, ConsumedItem
	{
		private static final long serialVersionUID = 1L;

		public NormalGem()
		{
			super("Normal Gem", "A gem with an ordinary essence. When held, it strengthens the power of a Normal-type move only once.", BagCategory.MISC, 114);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.NORMAL;
		}

		public double getMultiplier()
		{
			return 1.5;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class Leftovers extends Item implements HoldItem, EndTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public Leftovers()
		{
			super("Leftovers", "An item to be held by a Pok\u00e9mon. The holder's HP is gradually restored during battle.", BagCategory.MISC, 115);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void apply(ActivePokemon victim, Battle b)
		{
			if (victim.fullHealth() || victim.hasEffect("Heal Block")) return;
			victim.healHealthFraction(1/16.0);
			b.addMessage(victim.getName() + "'s HP was restored by its " + this.name + "!", victim.getHP(), victim.user());
		}
	}

	private static class BlackBelt extends Item implements PowerChangeEffect, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public BlackBelt()
		{
			super("Black Belt", "An item to be held by a Pok\u00e9mon. It is a belt that boosts determination and Fighting-type moves.", BagCategory.MISC, 116);
			super.price = 9800;
		}

		public Type getType()
		{
			return Type.FIGHTING;
		}

		public double getMultiplier()
		{
			return 1.2;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class BlackGlasses extends Item implements PowerChangeEffect, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public BlackGlasses()
		{
			super("Black Glasses", "An item to be held by a Pok\u00e9mon. It is a shady-looking pair of glasses that boosts Dark-type moves.", BagCategory.MISC, 117);
			super.price = 9800;
		}

		public Type getType()
		{
			return Type.FIGHTING;
		}

		public double getMultiplier()
		{
			return 1.2;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class Charcoal extends Item implements PowerChangeEffect, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public Charcoal()
		{
			super("Charcoal", "An item to be held by a Pok\u00e9mon. It is a combustible fuel that boosts the power of Fire-type moves.", BagCategory.MISC, 118);
			super.price = 9800;
		}

		public Type getType()
		{
			return Type.FIRE;
		}

		public double getMultiplier()
		{
			return 1.2;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class DragonFang extends Item implements PowerChangeEffect, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public DragonFang()
		{
			super("Dragon Fang", "An item to be held by a Pok\u00e9mon. It is a hard and sharp fang that ups the power of Dragon-type moves.", BagCategory.MISC, 119);
			super.price = 9800;
		}

		public Type getType()
		{
			return Type.DRAGON;
		}

		public double getMultiplier()
		{
			return 1.2;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class HardStone extends Item implements PowerChangeEffect, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public HardStone()
		{
			super("Hard Stone", "An item to be held by a Pok\u00e9mon. It is an unbreakable stone that ups the power of Rock-type moves.", BagCategory.MISC, 120);
			super.price = 9800;
		}

		public Type getType()
		{
			return Type.ROCK;
		}

		public double getMultiplier()
		{
			return 1.2;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class Magnet extends Item implements PowerChangeEffect, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public Magnet()
		{
			super("Magnet", "An item to be held by a Pok\u00e9mon. It is a powerful magnet that boosts the power of Electric-type moves.", BagCategory.MISC, 121);
			super.price = 9800;
		}

		public Type getType()
		{
			return Type.ELECTRIC;
		}

		public double getMultiplier()
		{
			return 1.2;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class MetalCoat extends Item implements PokemonUseItem, HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;
		private String message;

		public MetalCoat()
		{
			super("Metal Coat", "A mysterious substance full of a special filmy metal. It allows certain kinds of Pok\u00e9mon to evolve.", BagCategory.MISC, 122);
			super.price = 9800;
		}

		public BaseEvolution getBaseEvolution(ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.getName());
			return base;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return message;
		}

		public boolean use(ActivePokemon p)
		{
			BaseEvolution base = getBaseEvolution(p);
			if (base == null)
			return false;
			
			message = p.evolve(null, base);
			return true;
		}

		public int flingDamage()
		{
			return 30;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getAttack().isType(b, user, Type.STEEL) ? 1.2 : 1;
		}
	}

	private static class MiracleSeed extends Item implements PowerChangeEffect, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public MiracleSeed()
		{
			super("Miracle Seed", "An item to be held by a Pok\u00e9mon. It is a seed imbued with life that ups the power of Grass-type moves.", BagCategory.MISC, 123);
			super.price = 9800;
		}

		public Type getType()
		{
			return Type.GRASS;
		}

		public double getMultiplier()
		{
			return 1.2;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class MysticWater extends Item implements PowerChangeEffect, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public MysticWater()
		{
			super("Mystic Water", "An item to be held by a Pok\u00e9mon. It is a teardrop-shaped gem that ups the power of Water-type moves.", BagCategory.MISC, 124);
			super.price = 9800;
		}

		public Type getType()
		{
			return Type.WATER;
		}

		public double getMultiplier()
		{
			return 1.2;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class NeverMeltIce extends Item implements PowerChangeEffect, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public NeverMeltIce()
		{
			super("NeverMeltIce", "An item to be held by a Pok\u00e9mon. It is a piece of ice that repels heat and boosts Ice-type moves.", BagCategory.MISC, 125);
			super.price = 9800;
		}

		public Type getType()
		{
			return Type.ICE;
		}

		public double getMultiplier()
		{
			return 1.2;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class PoisonBarb extends Item implements PowerChangeEffect, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public PoisonBarb()
		{
			super("Poison Barb", "An item to be held by a Pok\u00e9mon. It is a small, poisonous barb that ups the power of Poison-type moves.", BagCategory.MISC, 126);
			super.price = 9800;
		}

		public Type getType()
		{
			return Type.POISON;
		}

		public double getMultiplier()
		{
			return 1.2;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class SharpBeak extends Item implements PowerChangeEffect, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public SharpBeak()
		{
			super("Sharp Beak", "An item to be held by a Pok\u00e9mon. It is a long, sharp beak that boosts the power of Flying-type moves.", BagCategory.MISC, 127);
			super.price = 9800;
		}

		public Type getType()
		{
			return Type.FLYING;
		}

		public double getMultiplier()
		{
			return 1.2;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class SilkScarf extends Item implements PowerChangeEffect, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public SilkScarf()
		{
			super("Silk Scarf", "An item to be held by a Pok\u00e9mon. It is a sumptuous scarf that boosts the power of Normal-type moves.", BagCategory.MISC, 128);
			super.price = 9800;
		}

		public Type getType()
		{
			return Type.NORMAL;
		}

		public double getMultiplier()
		{
			return 1.2;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class SilverPowder extends Item implements PowerChangeEffect, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public SilverPowder()
		{
			super("Silver Powder", "An item to be held by a Pok\u00e9mon. It is a shiny, silver powder that ups the power of Bug-type moves.", BagCategory.MISC, 129);
			super.price = 9800;
		}

		public Type getType()
		{
			return Type.BUG;
		}

		public double getMultiplier()
		{
			return 1.2;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class SoftSand extends Item implements PowerChangeEffect, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public SoftSand()
		{
			super("Soft Sand", "An item to be held by a Pok\u00e9mon. It is a loose, silky sand that boosts the power of Ground-type moves.", BagCategory.MISC, 130);
			super.price = 9800;
		}

		public Type getType()
		{
			return Type.GROUND;
		}

		public double getMultiplier()
		{
			return 1.2;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class SpellTag extends Item implements PowerChangeEffect, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public SpellTag()
		{
			super("Spell Tag", "An item to be held by a Pok\u00e9mon. It is a sinister, eerie tag that boosts the power of Ghost-type moves.", BagCategory.MISC, 131);
			super.price = 9800;
		}

		public Type getType()
		{
			return Type.GHOST;
		}

		public double getMultiplier()
		{
			return 1.2;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class TwistedSpoon extends Item implements PowerChangeEffect, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public TwistedSpoon()
		{
			super("Twisted Spoon", "An item to be held by a Pok\u00e9mon. It is a spoon imbued with telekinetic power that boosts Psychic-type moves.", BagCategory.MISC, 132);
			super.price = 9800;
		}

		public Type getType()
		{
			return Type.PSYCHIC;
		}

		public double getMultiplier()
		{
			return 1.2;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().isType(b, user, getType()))
			{
				if (this instanceof ConsumedItem)
				{
					b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
					user.consumeItem(b);
				}
				
				return getMultiplier();
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class DawnStone extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;
		private String message;

		public DawnStone()
		{
			super("Dawn Stone", "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It sparkles like eyes.", BagCategory.MISC, 133);
			super.price = 2100;
		}

		public BaseEvolution getBaseEvolution(ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.getName());
			return base;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return message;
		}

		public boolean use(ActivePokemon p)
		{
			BaseEvolution base = getBaseEvolution(p);
			if (base == null)
			return false;
			
			message = p.evolve(null, base);
			return true;
		}

		public int flingDamage()
		{
			return 80;
		}
	}

	private static class DeepSeaScale extends Item implements PokemonUseItem, HoldItem, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;
		private String message;

		public DeepSeaScale()
		{
			super("DeepSeaScale", "An item to be held by Clamperl, Chinchou, or Lanturn. A scale that shines a faint pink, it raises the Sp. Def stat.", BagCategory.MISC, 134);
			super.price = 200;
		}

		public BaseEvolution getBaseEvolution(ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.getName());
			return base;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return message;
		}

		public boolean use(ActivePokemon p)
		{
			BaseEvolution base = getBaseEvolution(p);
			if (base == null)
			return false;
			
			message = p.evolve(null, base);
			return true;
		}

		public int flingDamage()
		{
			return 30;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (s == Stat.SP_DEFENSE && (p.isPokemon("Clamperl") || p.isPokemon("Chinchou") || p.isPokemon("Lanturn"))) stat *= 2;
			return stat;
		}
	}

	private static class DeepSeaTooth extends Item implements PokemonUseItem, HoldItem, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;
		private String message;

		public DeepSeaTooth()
		{
			super("DeepSeaTooth", "An item to be held by Clamperl. A fang that gleams a sharp silver, it raises the Sp. Atk stat.", BagCategory.MISC, 135);
			super.price = 200;
		}

		public BaseEvolution getBaseEvolution(ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.getName());
			return base;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return message;
		}

		public boolean use(ActivePokemon p)
		{
			BaseEvolution base = getBaseEvolution(p);
			if (base == null)
			return false;
			
			message = p.evolve(null, base);
			return true;
		}

		public int flingDamage()
		{
			return 90;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (s == Stat.SP_ATTACK && p.isPokemon("Clamperl")) stat *= 2;
			return stat;
		}
	}

	private static class DragonScale extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;
		private String message;

		public DragonScale()
		{
			super("Dragon Scale", "A thick and tough scale. Dragon-type Pok\u00e9mon may be holding this item when caught.", BagCategory.MISC, 136);
			super.price = 2100;
		}

		public BaseEvolution getBaseEvolution(ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.getName());
			return base;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return message;
		}

		public boolean use(ActivePokemon p)
		{
			BaseEvolution base = getBaseEvolution(p);
			if (base == null)
			return false;
			
			message = p.evolve(null, base);
			return true;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class DubiousDisc extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;
		private String message;

		public DubiousDisc()
		{
			super("Dubious Disc", "A transparent device overflowing with dubious data. Its producer is unknown.", BagCategory.MISC, 137);
			super.price = 2100;
		}

		public BaseEvolution getBaseEvolution(ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.getName());
			return base;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return message;
		}

		public boolean use(ActivePokemon p)
		{
			BaseEvolution base = getBaseEvolution(p);
			if (base == null)
			return false;
			
			message = p.evolve(null, base);
			return true;
		}

		public int flingDamage()
		{
			return 50;
		}
	}

	private static class DuskStone extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;
		private String message;

		public DuskStone()
		{
			super("Dusk Stone", "A peculiar stone that makes certain species of Pokémon evolve. It is as dark as dark can be.", BagCategory.MISC, 138);
			super.price = 2100;
		}

		public BaseEvolution getBaseEvolution(ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.getName());
			return base;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return message;
		}

		public boolean use(ActivePokemon p)
		{
			BaseEvolution base = getBaseEvolution(p);
			if (base == null)
			return false;
			
			message = p.evolve(null, base);
			return true;
		}

		public int flingDamage()
		{
			return 80;
		}
	}

	private static class Electirizer extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;
		private String message;

		public Electirizer()
		{
			super("Electirizer", "A box packed with a tremendous amount of electric energy. It is loved by a certain Pok\u00e9mon.", BagCategory.MISC, 139);
			super.price = 2100;
		}

		public BaseEvolution getBaseEvolution(ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.getName());
			return base;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return message;
		}

		public boolean use(ActivePokemon p)
		{
			BaseEvolution base = getBaseEvolution(p);
			if (base == null)
			return false;
			
			message = p.evolve(null, base);
			return true;
		}

		public int flingDamage()
		{
			return 80;
		}
	}

	private static class FireStone extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;
		private String message;

		public FireStone()
		{
			super("Fire Stone", "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It is colored orange.", BagCategory.MISC, 140);
			super.price = 2100;
		}

		public BaseEvolution getBaseEvolution(ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.getName());
			return base;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return message;
		}

		public boolean use(ActivePokemon p)
		{
			BaseEvolution base = getBaseEvolution(p);
			if (base == null)
			return false;
			
			message = p.evolve(null, base);
			return true;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class KingsRock extends Item implements PokemonUseItem, HoldItem, ApplyDamageEffect
	{
		private static final long serialVersionUID = 1L;
		private String message;

		public KingsRock()
		{
			super("King's Rock", "An item to be held by a Pok\u00e9mon. When the holder inflicts damage, the target may flinch.", BagCategory.MISC, 141);
			super.price = 100;
		}

		public BaseEvolution getBaseEvolution(ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.getName());
			return base;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return message;
		}

		public boolean use(ActivePokemon p)
		{
			BaseEvolution base = getBaseEvolution(p);
			if (base == null)
			return false;
			
			message = p.evolve(null, base);
			return true;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void applyEffect(Battle b, ActivePokemon user, ActivePokemon victim, Integer damage)
		{
			if (Math.random()*100 < 10)
			{
				PokemonEffect flinch = PokemonEffect.getEffect("Flinch");
				if (flinch.applies(b, user, victim, CastSource.HELD_ITEM))
				{
					flinch.cast(b, user, victim, CastSource.HELD_ITEM, false);
					b.addMessage(user.getName() + "'s " + this.name + " caused " + victim.getName() + " to flinch!");
				}
			}
		}
	}

	private static class LeafStone extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;
		private String message;

		public LeafStone()
		{
			super("Leaf Stone", "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It has a leaf pattern.", BagCategory.MISC, 142);
			super.price = 2100;
		}

		public BaseEvolution getBaseEvolution(ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.getName());
			return base;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return message;
		}

		public boolean use(ActivePokemon p)
		{
			BaseEvolution base = getBaseEvolution(p);
			if (base == null)
			return false;
			
			message = p.evolve(null, base);
			return true;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class Magmarizer extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;
		private String message;

		public Magmarizer()
		{
			super("Magmarizer", "A box packed with a tremendous amount of magma energy. It is loved by a certain Pok\u00e9mon.", BagCategory.MISC, 143);
			super.price = 2100;
		}

		public BaseEvolution getBaseEvolution(ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.getName());
			return base;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return message;
		}

		public boolean use(ActivePokemon p)
		{
			BaseEvolution base = getBaseEvolution(p);
			if (base == null)
			return false;
			
			message = p.evolve(null, base);
			return true;
		}

		public int flingDamage()
		{
			return 80;
		}
	}

	private static class MoonStone extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;
		private String message;

		public MoonStone()
		{
			super("Moon Stone", "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It is as black as the night sky.", BagCategory.MISC, 144);
			super.price = 2100;
		}

		public BaseEvolution getBaseEvolution(ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.getName());
			return base;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return message;
		}

		public boolean use(ActivePokemon p)
		{
			BaseEvolution base = getBaseEvolution(p);
			if (base == null)
			return false;
			
			message = p.evolve(null, base);
			return true;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class OvalStone extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;
		private String message;

		public OvalStone()
		{
			super("Oval Stone", "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It is shaped like an egg.", BagCategory.MISC, 145);
			super.price = 2100;
		}

		public BaseEvolution getBaseEvolution(ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.getName());
			return base;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return message;
		}

		public boolean use(ActivePokemon p)
		{
			BaseEvolution base = getBaseEvolution(p);
			if (base == null)
			return false;
			
			message = p.evolve(null, base);
			return true;
		}

		public int flingDamage()
		{
			return 80;
		}
	}

	private static class Everstone extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public Everstone()
		{
			super("Everstone", "An item to be held by a Pok\u00e9mon. The Pok\u00e9mon holding this peculiar stone is prevented from evolving.", BagCategory.MISC, 146);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class PrismScale extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;
		private String message;

		public PrismScale()
		{
			super("Prism Scale", "A mysterious scale that evolves certain Pok\u00e9mon. It shines in rainbow colors.", BagCategory.MISC, 147);
			super.price = 500;
		}

		public BaseEvolution getBaseEvolution(ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.getName());
			return base;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return message;
		}

		public boolean use(ActivePokemon p)
		{
			BaseEvolution base = getBaseEvolution(p);
			if (base == null)
			return false;
			
			message = p.evolve(null, base);
			return true;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class Protector extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;
		private String message;

		public Protector()
		{
			super("Protector", "A protective item of some sort. It is extremely stiff and heavy. It is loved by a certain Pok\u00e9mon.", BagCategory.MISC, 148);
			super.price = 2100;
		}

		public BaseEvolution getBaseEvolution(ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.getName());
			return base;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return message;
		}

		public boolean use(ActivePokemon p)
		{
			BaseEvolution base = getBaseEvolution(p);
			if (base == null)
			return false;
			
			message = p.evolve(null, base);
			return true;
		}

		public int flingDamage()
		{
			return 80;
		}
	}

	private static class RazorClaw extends Item implements PokemonUseItem, HoldItem, CritStageEffect
	{
		private static final long serialVersionUID = 1L;
		private String message;

		public RazorClaw()
		{
			super("Razor Claw", "An item to be held by a Pok\u00e9mon. It is a sharply hooked claw that ups the holder's critical-hit ratio.", BagCategory.MISC, 149);
			super.price = 2100;
		}

		public BaseEvolution getBaseEvolution(ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.getName());
			return base;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return message;
		}

		public boolean use(ActivePokemon p)
		{
			BaseEvolution base = getBaseEvolution(p);
			if (base == null)
			return false;
			
			message = p.evolve(null, base);
			return true;
		}

		public int flingDamage()
		{
			return 80;
		}

		public int increaseCritStage(ActivePokemon p)
		{
			return 1;
		}
	}

	private static class RazorFang extends Item implements PokemonUseItem, HoldItem, ApplyDamageEffect
	{
		private static final long serialVersionUID = 1L;
		private String message;

		public RazorFang()
		{
			super("Razor Fang", "An item to be held by a Pok\u00e9mon. It may make foes and allies flinch when the holder inflicts damage.`", BagCategory.MISC, 150);
			super.price = 2100;
		}

		public BaseEvolution getBaseEvolution(ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.getName());
			return base;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return message;
		}

		public boolean use(ActivePokemon p)
		{
			BaseEvolution base = getBaseEvolution(p);
			if (base == null)
			return false;
			
			message = p.evolve(null, base);
			return true;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void applyEffect(Battle b, ActivePokemon user, ActivePokemon victim, Integer damage)
		{
			if (Math.random()*100 < 10)
			{
				PokemonEffect flinch = PokemonEffect.getEffect("Flinch");
				if (flinch.applies(b, user, victim, CastSource.HELD_ITEM))
				{
					flinch.cast(b, user, victim, CastSource.HELD_ITEM, false);
					b.addMessage(user.getName() + "'s " + this.name + " caused " + victim.getName() + " to flinch!");
				}
			}
		}
	}

	private static class ReaperCloth extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;
		private String message;

		public ReaperCloth()
		{
			super("Reaper Cloth", "A cloth imbued with horrifyingly strong spiritual energy. It is loved by a certain Pok\u00e9mon.", BagCategory.MISC, 151);
			super.price = 2100;
		}

		public BaseEvolution getBaseEvolution(ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.getName());
			return base;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return message;
		}

		public boolean use(ActivePokemon p)
		{
			BaseEvolution base = getBaseEvolution(p);
			if (base == null)
			return false;
			
			message = p.evolve(null, base);
			return true;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class ShinyStone extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;
		private String message;

		public ShinyStone()
		{
			super("Shiny Stone", "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It shines with a dazzling light.", BagCategory.MISC, 152);
			super.price = 2100;
		}

		public BaseEvolution getBaseEvolution(ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.getName());
			return base;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return message;
		}

		public boolean use(ActivePokemon p)
		{
			BaseEvolution base = getBaseEvolution(p);
			if (base == null)
			return false;
			
			message = p.evolve(null, base);
			return true;
		}

		public int flingDamage()
		{
			return 80;
		}
	}

	private static class SunStone extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;
		private String message;

		public SunStone()
		{
			super("Sun Stone", "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It is as red as the sun.", BagCategory.MISC, 153);
			super.price = 2100;
		}

		public BaseEvolution getBaseEvolution(ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.getName());
			return base;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return message;
		}

		public boolean use(ActivePokemon p)
		{
			BaseEvolution base = getBaseEvolution(p);
			if (base == null)
			return false;
			
			message = p.evolve(null, base);
			return true;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class ThunderStone extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;
		private String message;

		public ThunderStone()
		{
			super("Thunder Stone", "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It has a thunderbolt pattern.", BagCategory.MISC, 154);
			super.price = 2100;
		}

		public BaseEvolution getBaseEvolution(ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.getName());
			return base;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return message;
		}

		public boolean use(ActivePokemon p)
		{
			BaseEvolution base = getBaseEvolution(p);
			if (base == null)
			return false;
			
			message = p.evolve(null, base);
			return true;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class UpGrade extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;
		private String message;

		public UpGrade()
		{
			super("Up-Grade", "A transparent device filled with all sorts of data. It was produced by Silph Co.", BagCategory.MISC, 155);
			super.price = 2100;
		}

		public BaseEvolution getBaseEvolution(ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.getName());
			return base;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return message;
		}

		public boolean use(ActivePokemon p)
		{
			BaseEvolution base = getBaseEvolution(p);
			if (base == null)
			return false;
			
			message = p.evolve(null, base);
			return true;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class WaterStone extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;
		private String message;

		public WaterStone()
		{
			super("Water Stone", "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It is a clear, light blue.", BagCategory.MISC, 156);
			super.price = 2100;
		}

		public BaseEvolution getBaseEvolution(ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.getName());
			return base;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return message;
		}

		public boolean use(ActivePokemon p)
		{
			BaseEvolution base = getBaseEvolution(p);
			if (base == null)
			return false;
			
			message = p.evolve(null, base);
			return true;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class Antidote extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public Antidote()
		{
			super("Antidote", "A spray-type medicine. It lifts the effect of poison from one Pok\u00e9mon.", BagCategory.MEDICINE, 157);
			super.price = 100;
			super.bcat.add(BattleBagCategory.STATUS);
		}

		public StatusCondition toRemove()
		{
			return StatusCondition.POISONED;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " was cured of its status condition!";
		}

		public boolean use(ActivePokemon p)
		{
			if (!p.hasStatus(toRemove()))
			{
				return false;
			}
			
			p.removeStatus();
			return true;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class Awakening extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public Awakening()
		{
			super("Awakening", "A spray-type medicine. It awakens a Pok\u00e9mon from the clutches of sleep.", BagCategory.MEDICINE, 158);
			super.price = 250;
			super.bcat.add(BattleBagCategory.STATUS);
		}

		public StatusCondition toRemove()
		{
			return StatusCondition.ASLEEP;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " was cured of its status condition!";
		}

		public boolean use(ActivePokemon p)
		{
			if (!p.hasStatus(toRemove()))
			{
				return false;
			}
			
			p.removeStatus();
			return true;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class BurnHeal extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public BurnHeal()
		{
			super("Burn Heal", "A spray-type medicine. It heals a single Pok\u00e9mon that is suffering from a burn.", BagCategory.MEDICINE, 159);
			super.price = 250;
			super.bcat.add(BattleBagCategory.STATUS);
		}

		public StatusCondition toRemove()
		{
			return StatusCondition.BURNED;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " was cured of its status condition!";
		}

		public boolean use(ActivePokemon p)
		{
			if (!p.hasStatus(toRemove()))
			{
				return false;
			}
			
			p.removeStatus();
			return true;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class IceHeal extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public IceHeal()
		{
			super("Ice Heal", "A spray-type medicine. It defrosts a Pok\u00e9mon that has been frozen solid.", BagCategory.MEDICINE, 160);
			super.price = 250;
			super.bcat.add(BattleBagCategory.STATUS);
		}

		public StatusCondition toRemove()
		{
			return StatusCondition.FROZEN;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " was cured of its status condition!";
		}

		public boolean use(ActivePokemon p)
		{
			if (!p.hasStatus(toRemove()))
			{
				return false;
			}
			
			p.removeStatus();
			return true;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class ParalyzeHeal extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public ParalyzeHeal()
		{
			super("Paralyze Heal", "A spray-type medicine. It eliminates paralysis from a single Pok\u00e9mon.", BagCategory.MEDICINE, 161);
			super.price = 200;
			super.bcat.add(BattleBagCategory.STATUS);
		}

		public StatusCondition toRemove()
		{
			return StatusCondition.PARALYZED;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " was cured of its status condition!";
		}

		public boolean use(ActivePokemon p)
		{
			if (!p.hasStatus(toRemove()))
			{
				return false;
			}
			
			p.removeStatus();
			return true;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class FullHeal extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public FullHeal()
		{
			super("Full Heal", "A spray-type medicine. It heals all the status problems of a single Pok\u00e9mon.", BagCategory.MEDICINE, 162);
			super.price = 250;
			super.bcat.add(BattleBagCategory.STATUS);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " was cured of its status condition!";
		}

		public boolean use(ActivePokemon p)
		{
			if (!p.hasStatus() || p.hasStatus(StatusCondition.FAINTED)) return false;
			
			p.removeStatus();
			return true;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class FullRestore extends Item implements PokemonUseItem, HoldItem, BattleUseItem
	{
		private static final long serialVersionUID = 1L;

		public FullRestore()
		{
			super("Full Restore", "A medicine that fully restores the HP and heals any status problems of a single Pok\u00e9mon.", BagCategory.MEDICINE, 163);
			super.price = 3000;
			super.bcat.add(BattleBagCategory.HPPP);
			super.bcat.add(BattleBagCategory.STATUS);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " was fully healed!";
		}

		public boolean use(ActivePokemon p)
		{
			if (p.hasStatus(StatusCondition.FAINTED)) return false;
			if (!p.hasStatus() && p.fullHealth()) return false;
			
			p.removeStatus();
			p.healHealthFraction(1);
			return true;
		}

		public int flingDamage()
		{
			return 30;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p);
		}
	}

	private static class Elixir extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public Elixir()
		{
			super("Elixir", "It restores the PP of all the moves learned by the targeted Pok\u00e9mon by 10 points each.", BagCategory.MEDICINE, 164);
			super.price = 3000;
			super.bcat.add(BattleBagCategory.HPPP);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s PP was restored!";
		}

		public boolean use(ActivePokemon p)
		{
			boolean changed = false;
			for (Move m : p.getMoves())
			{
				changed |= m.increasePP(10);
			}
			return changed;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class MaxElixir extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public MaxElixir()
		{
			super("Max Elixir", "It restores the PP of all the moves learned by the targeted Pok\u00e9mon by 10 points each.", BagCategory.MEDICINE, 165);
			super.price = 4500;
			super.bcat.add(BattleBagCategory.HPPP);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s PP was restored!";
		}

		public boolean use(ActivePokemon p)
		{
			boolean changed = false;
			for (Move m : p.getMoves())
			{
				changed |= m.increasePP(m.getMaxPP());
			}
			
			return changed;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class Ether extends Item implements MoveUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;
		private String restore;

		public Ether()
		{
			super("Ether", "It restores the PP of a Pok\u00e9mon's selected move by a maximum of 10 points.", BagCategory.MEDICINE, 166);
			super.price = 1200;
			super.bcat.add(BattleBagCategory.HPPP);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + restore + "'s PP was restored!";
		}

		public boolean use(Move m)
		{
			restore = m.getAttack().getName();
			return m.increasePP(10);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class MaxEther extends Item implements MoveUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;
		private String restore;

		public MaxEther()
		{
			super("Max Ether", "It fully restores the PP of a single selected move that has been learned by the target Pok\u00e9mon.", BagCategory.MEDICINE, 167);
			super.price = 2000;
			super.bcat.add(BattleBagCategory.HPPP);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + restore + "'s PP was restored!";
		}

		public boolean use(Move m)
		{
			restore = m.getAttack().getName();
			return m.increasePP(m.getMaxPP());
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class BerryJuice extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public BerryJuice()
		{
			super("Berry Juice", "A 100% pure juice made of Berries. It restores the HP of one Pok\u00e9mon by just 20 points.", BagCategory.MEDICINE, 168);
			super.price = 100;
			super.bcat.add(BattleBagCategory.HPPP);
		}

		public int healAmount()
		{
			return 20;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s health was restored!";
		}

		public boolean use(ActivePokemon p)
		{
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class SweetHeart extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public SweetHeart()
		{
			super("Sweet Heart", "Very sweet chocolate. It restores the HP of one Pok\u00e9mon by only 20 points.", BagCategory.MEDICINE, 169);
			super.price = 100;
			super.bcat.add(BattleBagCategory.HPPP);
		}

		public int healAmount()
		{
			return 20;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s health was restored!";
		}

		public boolean use(ActivePokemon p)
		{
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class Potion extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public Potion()
		{
			super("Potion", "A spray-type medicine for wounds. It restores the HP of one Pok\u00e9mon by just 20 points.", BagCategory.MEDICINE, 170);
			super.price = 100;
			super.bcat.add(BattleBagCategory.HPPP);
		}

		public int healAmount()
		{
			return 20;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s health was restored!";
		}

		public boolean use(ActivePokemon p)
		{
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class EnergyPowder extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public EnergyPowder()
		{
			super("Energy Powder", "A very bitter medicine powder. It restores the HP of one Pok\u00e9mon by 50 points.", BagCategory.MEDICINE, 171);
			super.price = 500;
			super.bcat.add(BattleBagCategory.HPPP);
		}

		public int healAmount()
		{
			return 50;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s health was restored!";
		}

		public boolean use(ActivePokemon p)
		{
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class FreshWater extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public FreshWater()
		{
			super("Fresh Water", "Water with a high mineral content. It restores the HP of one Pok\u00e9mon by 50 points.", BagCategory.MEDICINE, 172);
			super.price = 200;
			super.bcat.add(BattleBagCategory.HPPP);
		}

		public int healAmount()
		{
			return 50;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s health was restored!";
		}

		public boolean use(ActivePokemon p)
		{
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class SuperPotion extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public SuperPotion()
		{
			super("Super Potion", "A spray-type medicine for wounds. It restores the HP of one Pok\u00e9mon by 50 points.", BagCategory.MEDICINE, 173);
			super.price = 700;
			super.bcat.add(BattleBagCategory.HPPP);
		}

		public int healAmount()
		{
			return 50;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s health was restored!";
		}

		public boolean use(ActivePokemon p)
		{
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class SodaPop extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public SodaPop()
		{
			super("Soda Pop", "A fizzy soda drink. It restores the HP of one Pok\u00e9mon by 60 points.", BagCategory.MEDICINE, 174);
			super.price = 300;
			super.bcat.add(BattleBagCategory.HPPP);
		}

		public int healAmount()
		{
			return 60;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s health was restored!";
		}

		public boolean use(ActivePokemon p)
		{
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class Lemonade extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public Lemonade()
		{
			super("Lemonade", "A very sweet drink. It restores the HP of one Pok\u00e9mon by 80 points.", BagCategory.MEDICINE, 175);
			super.price = 350;
			super.bcat.add(BattleBagCategory.HPPP);
		}

		public int healAmount()
		{
			return 80;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s health was restored!";
		}

		public boolean use(ActivePokemon p)
		{
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class MoomooMilk extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public MoomooMilk()
		{
			super("Moomoo Milk", "Milk with a very high nutrition content. It restores the HP of one Pok\u00e9mon by 100 points.", BagCategory.MEDICINE, 176);
			super.price = 500;
			super.bcat.add(BattleBagCategory.HPPP);
		}

		public int healAmount()
		{
			return 100;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s health was restored!";
		}

		public boolean use(ActivePokemon p)
		{
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class EnergyRoot extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public EnergyRoot()
		{
			super("Energy Root", "A very bitter root. It restores the HP of one Pok\u00e9mon by 200 points.", BagCategory.MEDICINE, 177);
			super.price = 800;
			super.bcat.add(BattleBagCategory.HPPP);
		}

		public int healAmount()
		{
			return 200;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s health was restored!";
		}

		public boolean use(ActivePokemon p)
		{
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class HyperPotion extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public HyperPotion()
		{
			super("Hyper Potion", "A spray-type medicine for wounds. It restores the HP of one Pok\u00e9mon by 200 points.", BagCategory.MEDICINE, 178);
			super.price = 1200;
			super.bcat.add(BattleBagCategory.HPPP);
		}

		public int healAmount()
		{
			return 200;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s health was restored!";
		}

		public boolean use(ActivePokemon p)
		{
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class MaxPotion extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public MaxPotion()
		{
			super("Max Potion", "A spray-type medicine for wounds. It completely restores the HP of a single Pok\u00e9mon.", BagCategory.MEDICINE, 179);
			super.price = 2500;
			super.bcat.add(BattleBagCategory.HPPP);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s health was restored!";
		}

		public boolean use(ActivePokemon p)
		{
			return p.healHealthFraction(1) != 0;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class Revive extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public Revive()
		{
			super("Revive", "A medicine that revives a fainted Pok\u00e9mon. It restores half the Pok\u00e9mon's maximum HP.", BagCategory.MEDICINE, 180);
			super.price = 1500;
			super.bcat.add(BattleBagCategory.STATUS);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " was revived!";
		}

		public boolean use(ActivePokemon p)
		{
			if (!p.hasStatus(StatusCondition.FAINTED)) return false;
			
			p.removeStatus();
			p.healHealthFraction(.5);
			
			return true;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class MaxRevive extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public MaxRevive()
		{
			super("Max Revive", "A medicine that revives a fainted Pok\u00e9mon. It fully restores the Pok\u00e9mon's HP.", BagCategory.MEDICINE, 181);
			super.price = 4000;
			super.bcat.add(BattleBagCategory.STATUS);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " was fully revived!";
		}

		public boolean use(ActivePokemon p)
		{
			if (!p.hasStatus(StatusCondition.FAINTED)) return false;
			
			p.removeStatus();
			p.healHealthFraction(1);
			
			return true;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class RevivalHerb extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public RevivalHerb()
		{
			super("Revival Herb", "A very bitter medicinal herb. It revives a fainted Pok\u00e9mon, fully restoring its HP.", BagCategory.MEDICINE, 182);
			super.price = 2800;
			super.bcat.add(BattleBagCategory.STATUS);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " was fully revived!";
		}

		public boolean use(ActivePokemon p)
		{
			if (!p.hasStatus(StatusCondition.FAINTED)) return false;
			
			p.removeStatus();
			p.healHealthFraction(1);
			
			return true;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class SacredAsh extends Item implements TrainerUseItem, HoldItem, BattleUseItem
	{
		private static final long serialVersionUID = 1L;

		public SacredAsh()
		{
			super("Sacred Ash", "It revives all fainted Pok\u00e9mon. In doing so, it also fully restores their HP.", BagCategory.MEDICINE, 183);
			super.price = 4000;
			super.bcat.add(BattleBagCategory.STATUS);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "All fainted Pok\u00e9mon were fully revived!";
		}

		public boolean use(Trainer t)
		{
			boolean changed = false;
			for (ActivePokemon p : t.getTeam())
			{
				if (p.hasStatus(StatusCondition.FAINTED))
				{
					changed = true;
					p.removeStatus();
					p.healHealthFraction(1);
				}
			}
			return changed;
		}

		public int flingDamage()
		{
			return 30;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use((Trainer)b.getTrainer(p.user()));
		}
	}

	private static class DireHit extends Item implements BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public DireHit()
		{
			super("Dire Hit", "It raises the critical-hit ratio greatly. It can be used only once and wears off if the Pok\u00e9mon is withdrawn.", BagCategory.STAT, 184);
			super.price = 650;
			super.bcat.add(BattleBagCategory.BATTLE);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " is getting pumped!";
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			PokemonEffect crits = PokemonEffect.getEffect("RaiseCrits");
			if (!crits.applies(b, p, p, CastSource.USE_ITEM)) return false;
			
			crits.cast(b, p, p, CastSource.USE_ITEM, false);
			return true;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class GuardSpec extends Item implements BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public GuardSpec()
		{
			super("Guard Spec.", "An item that prevents stat reduction among the Trainer's party Pok\u00e9mon for five turns after use.", BagCategory.STAT, 185);
			super.price = 700;
			super.bcat.add(BattleBagCategory.BATTLE);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " is covered by a veil!";
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			PokemonEffect gSpesh = PokemonEffect.getEffect("GuardSpecial");
			if (!gSpesh.applies(b, p, p, CastSource.USE_ITEM)) return false;
			
			gSpesh.cast(b, p, p, CastSource.USE_ITEM, false);
			return true;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class XAccuracy extends Item implements BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public XAccuracy()
		{
			super("X Accuracy", "An item that raises the accuracy of a Pok\u00e9mon in battle. It wears off if the Pok\u00e9mon is withdrawn.", BagCategory.STAT, 186);
			super.price = 950;
			super.bcat.add(BattleBagCategory.BATTLE);
		}

		public Stat toIncrease()
		{
			return Stat.ACCURACY;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return p.getAttributes().modifyStage(p, p, 1, toIncrease(), b, CastSource.USE_ITEM);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class XAttack extends Item implements BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public XAttack()
		{
			super("X Attack", "An item that raises the Attack stat of a Pok\u00e9mon in battle. It wears off if the Pok\u00e9mon is withdrawn.", BagCategory.STAT, 187);
			super.price = 500;
			super.bcat.add(BattleBagCategory.BATTLE);
		}

		public Stat toIncrease()
		{
			return Stat.ATTACK;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return p.getAttributes().modifyStage(p, p, 1, toIncrease(), b, CastSource.USE_ITEM);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class XDefend extends Item implements BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public XDefend()
		{
			super("X Defend", "An item that raises the Defense stat of a Pok\u00e9mon in battle. It wears off if the Pok\u00e9mon is withdrawn.", BagCategory.STAT, 188);
			super.price = 550;
			super.bcat.add(BattleBagCategory.BATTLE);
		}

		public Stat toIncrease()
		{
			return Stat.SP_DEFENSE;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return p.getAttributes().modifyStage(p, p, 1, toIncrease(), b, CastSource.USE_ITEM);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class XSpDef extends Item implements BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public XSpDef()
		{
			super("X Sp. Def", "An item that raises the Sp. Def stat of a Pok\u00e9mon in battle. It wears off if the Pok\u00e9mon is withdrawn.", BagCategory.STAT, 189);
			super.price = 350;
			super.bcat.add(BattleBagCategory.BATTLE);
		}

		public Stat toIncrease()
		{
			return Stat.SP_DEFENSE;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return p.getAttributes().modifyStage(p, p, 1, toIncrease(), b, CastSource.USE_ITEM);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class XSpecial extends Item implements BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public XSpecial()
		{
			super("X Special", "An item that raises the Sp. Atk stat of a Pok\u00e9mon in battle. It wears off if the Pok\u00e9mon is withdrawn.", BagCategory.STAT, 190);
			super.price = 350;
			super.bcat.add(BattleBagCategory.BATTLE);
		}

		public Stat toIncrease()
		{
			return Stat.SP_ATTACK;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return p.getAttributes().modifyStage(p, p, 1, toIncrease(), b, CastSource.USE_ITEM);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class XSpeed extends Item implements BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public XSpeed()
		{
			super("X Speed", "An item that raises the Speed stat of a Pok\u00e9mon in battle. It wears off if the Pok\u00e9mon is withdrawn.", BagCategory.STAT, 191);
			super.price = 350;
			super.bcat.add(BattleBagCategory.BATTLE);
		}

		public Stat toIncrease()
		{
			return Stat.SPEED;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return p.getAttributes().modifyStage(p, p, 1, toIncrease(), b, CastSource.USE_ITEM);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class Calcium extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public Calcium()
		{
			super("Calcium", "A nutritious drink for Pok\u00e9mon. It raises the base Sp. Atk (Special Attack) stat of a single Pok\u00e9mon.", BagCategory.STAT, 192);
			super.price = 9800;
		}

		public Stat toIncrease()
		{
			return Stat.SP_ATTACK;
		}

		public int increaseAmount()
		{
			return 10;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public boolean use(ActivePokemon p)
		{
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class Carbos extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public Carbos()
		{
			super("Carbos", "A nutritious drink for Pok\u00e9mon. It raises the base Speed stat of a single Pok\u00e9mon.", BagCategory.STAT, 193);
			super.price = 9800;
		}

		public Stat toIncrease()
		{
			return Stat.SPEED;
		}

		public int increaseAmount()
		{
			return 10;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public boolean use(ActivePokemon p)
		{
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class CleverWing extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public CleverWing()
		{
			super("Clever Wing", "An item for use on a Pok\u00e9mon. It slightly increases the base Sp. Def stat of a single Pok\u00e9mon.", BagCategory.STAT, 194);
			super.price = 3000;
		}

		public Stat toIncrease()
		{
			return Stat.SP_ATTACK;
		}

		public int increaseAmount()
		{
			return 1;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public boolean use(ActivePokemon p)
		{
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}

		public int flingDamage()
		{
			return 20;
		}
	}

	private static class HealthWing extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public HealthWing()
		{
			super("Health Wing", "An item for use on a Pok\u00e9mon. It slightly increases the base HP of a single Pok\u00e9mon.", BagCategory.STAT, 195);
			super.price = 3000;
		}

		public Stat toIncrease()
		{
			return Stat.HP;
		}

		public int increaseAmount()
		{
			return 1;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public boolean use(ActivePokemon p)
		{
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}

		public int flingDamage()
		{
			return 20;
		}
	}

	private static class HPUp extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public HPUp()
		{
			super("HP Up", "A nutritious drink for Pok\u00e9mon. It raises the base HP of a single Pok\u00e9mon.", BagCategory.STAT, 196);
			super.price = 9800;
		}

		public Stat toIncrease()
		{
			return Stat.HP;
		}

		public int increaseAmount()
		{
			return 10;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public boolean use(ActivePokemon p)
		{
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class GeniusWing extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public GeniusWing()
		{
			super("Genius Wing", "An item for use on a Pok\u00e9mon. It slightly increases the base Sp. Atk stat of a single Pok\u00e9mon.", BagCategory.STAT, 197);
			super.price = 3000;
		}

		public Stat toIncrease()
		{
			return Stat.SP_ATTACK;
		}

		public int increaseAmount()
		{
			return 1;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public boolean use(ActivePokemon p)
		{
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}

		public int flingDamage()
		{
			return 20;
		}
	}

	private static class Iron extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public Iron()
		{
			super("Iron", "A nutritious drink for Pok\u00e9mon. It raises the base Defense stat of a single Pok\u00e9mon.", BagCategory.STAT, 198);
			super.price = 9800;
		}

		public Stat toIncrease()
		{
			return Stat.DEFENSE;
		}

		public int increaseAmount()
		{
			return 10;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public boolean use(ActivePokemon p)
		{
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class MuscleWing extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public MuscleWing()
		{
			super("Muscle Wing", "An item for use on a Pok\u00e9mon. It slightly increases the base Attack stat of a single Pok\u00e9mon.", BagCategory.STAT, 199);
			super.price = 3000;
		}

		public Stat toIncrease()
		{
			return Stat.ATTACK;
		}

		public int increaseAmount()
		{
			return 1;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public boolean use(ActivePokemon p)
		{
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}

		public int flingDamage()
		{
			return 20;
		}
	}

	private static class PPMax extends Item implements MoveUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;
private String increase;
		public PPMax()
		{
			super("PP Max", "It maximally raises the top PP of a selected move that has been learned by the target Pok\u00e9mon.", BagCategory.STAT, 200);
			super.price = 9800;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + increase + "'s Max PP was increased!";
		}

		public boolean use(Move m)
		{
			increase = m.getAttack().getName();
			return m.increaseMaxPP(3);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class PPUp extends Item implements MoveUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;
		private String increase;

		public PPUp()
		{
			super("PP Up", "It slightly raises the maximum PP of a selected move that has been learned by the target Pok\u00e9mon.", BagCategory.STAT, 201);
			super.price = 9800;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + increase + "'s Max PP was increased!";
		}

		public boolean use(Move m)
		{
			increase = m.getAttack().getName();
			return m.increaseMaxPP(1);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class Protein extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public Protein()
		{
			super("Protein", "A nutritious drink for Pok\u00e9mon. It raises the base Attack stat of a single Pok\u00e9mon.", BagCategory.STAT, 202);
			super.price = 9800;
		}

		public Stat toIncrease()
		{
			return Stat.ATTACK;
		}

		public int increaseAmount()
		{
			return 10;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public boolean use(ActivePokemon p)
		{
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class RareCandy extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public RareCandy()
		{
			super("Rare Candy", "A candy that is packed with energy. It raises the level of a single Pok\u00e9mon by one.", BagCategory.STAT, 203);
			super.price = 4800;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " gained a level!";
		}

		public int flingDamage()
		{
			return 30;
		}

		public boolean use(ActivePokemon p)
		{
			// TODO: Need Level Up to be implemented -- also handle the messafe accordingly
			return false;
		}
	}

	private static class ResistWing extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public ResistWing()
		{
			super("Resist Wing", "An item for use on a Pok\u00e9mon. It slightly increases the base Defense stat of a single Pok\u00e9mon.", BagCategory.STAT, 204);
			super.price = 3000;
		}

		public Stat toIncrease()
		{
			return Stat.DEFENSE;
		}

		public int increaseAmount()
		{
			return 1;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public boolean use(ActivePokemon p)
		{
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}

		public int flingDamage()
		{
			return 20;
		}
	}

	private static class SwiftWing extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public SwiftWing()
		{
			super("Swift Wing", "An item for use on a Pok\u00e9mon. It slightly increases the base Speed stat of a single Pok\u00e9mon.", BagCategory.STAT, 205);
			super.price = 3000;
		}

		public Stat toIncrease()
		{
			return Stat.SPEED;
		}

		public int increaseAmount()
		{
			return 1;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public boolean use(ActivePokemon p)
		{
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}

		public int flingDamage()
		{
			return 20;
		}
	}

	private static class Zinc extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public Zinc()
		{
			super("Zinc", "A nutritious drink for Pok\u00e9mon. It raises the base Sp. Def (Special Defense) stat of a single Pok\u00e9mon.", BagCategory.STAT, 206);
			super.price = 9800;
		}

		public Stat toIncrease()
		{
			return Stat.SP_DEFENSE;
		}

		public int increaseAmount()
		{
			return 10;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public boolean use(ActivePokemon p)
		{
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class CherishBall extends Item implements BallItem
	{
		private static final long serialVersionUID = 1L;

		public CherishBall()
		{
			super("Cherish Ball", "A quite rare Pok\u00e9 Ball that has been specially crafted to commemorate an occasion of some sort.", BagCategory.BALL, 207);
			super.price = 200;
			super.bcat.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b)
		{
			return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p)
		{
		}
	}

	private static class DiveBall extends Item implements BallItem
	{
		private static final long serialVersionUID = 1L;

		public DiveBall()
		{
			super("Dive Ball", "A somewhat different Pok\u00e9 Ball that works especially well on Pok\u00e9mon that live underwater.", BagCategory.BALL, 208);
			super.price = 1000;
			super.bcat.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b)
		{
			// TODO: How do I see if the user is underwater, surfing, or fishing?
			if (false) return new double[] {3.5, 0};
			else return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p)
		{
		}
	}

	private static class DuskBall extends Item implements BallItem
	{
		private static final long serialVersionUID = 1L;

		public DuskBall()
		{
			super("Dusk Ball", "A somewhat different Pok\u00e9 Ball that makes it easier to catch wild Pok\u00e9mon at night or in dark places like caves.", BagCategory.BALL, 209);
			super.price = 1000;
			super.bcat.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b)
		{
			// TODO: How do I see if the user is in a dark environment?
			if (false) return new double[] {3.5, 0};
			else return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p)
		{
		}
	}

	private static class FastBall extends Item implements BallItem
	{
		private static final long serialVersionUID = 1L;

		public FastBall()
		{
			super("Fast Ball", "A Pok\u00e9 Ball that makes it easier to catch Pok\u00e9mon which are quick to run away.", BagCategory.BALL, 210);
			super.price = 200;
			super.bcat.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (o.getStat(Stat.SPEED) >= 100) return new double[] {4, 0};
			else return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p)
		{
		}
	}

	private static class GreatBall extends Item implements BallItem
	{
		private static final long serialVersionUID = 1L;

		public GreatBall()
		{
			super("Great Ball", "A good, high-performance Ball that provides a higher Pok\u00e9mon catch rate than a standard Pok\u00e9 Ball.", BagCategory.BALL, 211);
			super.price = 600;
			super.bcat.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b)
		{
			return new double[] {1.5, 0};
		}

		public void afterCaught(ActivePokemon p)
		{
		}
	}

	private static class HealBall extends Item implements BallItem
	{
		private static final long serialVersionUID = 1L;

		public HealBall()
		{
			super("Heal Ball", "A remedial Pok\u00e9 Ball that restores the caught Pok\u00e9mon's HP and eliminates any status problem.", BagCategory.BALL, 212);
			super.price = 300;
			super.bcat.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b)
		{
			return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p)
		{
			p.healHealthFraction(1);
			for (Move m : p.getMoves()) m.increasePP(m.getAttack().getPP());
		}
	}

	private static class HeavyBall extends Item implements BallItem
	{
		private static final long serialVersionUID = 1L;

		public HeavyBall()
		{
			super("Heavy Ball", "A Pok\u00e9 Ball for catching very heavy Pok\u00e9mon.", BagCategory.BALL, 213);
			super.price = 200;
			super.bcat.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b)
		{
			double weight = o.getWeight(b);
			
			double[] res = new double[2];
			res[0] = 1;
			
			if (weight <= 451.5) res[1] = -20;
			else if (weight <= 661.5) res[1] = 20;
			else if (weight <= 903.0) res[1] = 30;
			else res[1] = 40;
			
			return res;
		}

		public void afterCaught(ActivePokemon p)
		{
		}
	}

	private static class LevelBall extends Item implements BallItem
	{
		private static final long serialVersionUID = 1L;

		public LevelBall()
		{
			super("Level Ball", "A Pok\u00e9 Ball for catching Pok\u00e9mon that are a lower level than your own.", BagCategory.BALL, 214);
			super.price = 200;
			super.bcat.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (me.getLevel()/4 > o.getLevel()) return new double[] {8, 0};
			else if (me.getLevel()/2 > o.getLevel()) return new double[] {4, 0};
			else if (me.getLevel() > o.getLevel()) return new double[] {2, 0};
			else return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p)
		{
		}
	}

	private static class LoveBall extends Item implements BallItem
	{
		private static final long serialVersionUID = 1L;

		public LoveBall()
		{
			super("Love Ball", "Pok\u00e9 Ball for catching Pok\u00e9mon that are the opposite gender of your Pok\u00e9mon.", BagCategory.BALL, 215);
			super.price = 200;
			super.bcat.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (me.getGender() == o.getGender()) return new double[] {8, 0};
			else return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p)
		{
		}
	}

	private static class LureBall extends Item implements BallItem
	{
		private static final long serialVersionUID = 1L;

		public LureBall()
		{
			super("Lure Ball", "A Pok\u00e9 Ball for catching Pok\u00e9mon hooked by a Rod when fishing.", BagCategory.BALL, 216);
			super.price = 200;
			super.bcat.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (false) return new double[] {3, 0};
			else return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p)
		{
		}
	}

	private static class LuxuryBall extends Item implements BallItem
	{
		private static final long serialVersionUID = 1L;

		public LuxuryBall()
		{
			super("Luxury Ball", "A comfortable Pok\u00e9 Ball that makes a caught wild Pok\u00e9mon quickly grow friendly.", BagCategory.BALL, 217);
			super.price = 1000;
			super.bcat.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b)
		{
			return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p)
		{
			// TODO: Make this item do something more interesting
		}
	}

	private static class MasterBall extends Item implements BallItem
	{
		private static final long serialVersionUID = 1L;

		public MasterBall()
		{
			super("Master Ball", "The best Ball with the ultimate level of performance. It will catch any wild Pok\u00e9mon without fail.", BagCategory.BALL, 218);
			super.price = 0;
			super.bcat.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b)
		{
			return new double[] {255, 0};
		}

		public void afterCaught(ActivePokemon p)
		{
		}
	}

	private static class MoonBall extends Item implements BallItem
	{
		private static final long serialVersionUID = 1L;

		public MoonBall()
		{
			super("Moon Ball", "A Pok\u00e9 Ball for catching Pok\u00e9mon that evolve using the Moon Stone.", BagCategory.BALL, 219);
			super.price = 200;
			super.bcat.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b)
		{
			Evolution ev = o.getPokemonInfo().getEvolution();
			if (ev.getEvolution(EvolutionCheck.ITEM, o, "Moon Stone") != null) return new double[] {4, 0};
			else return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p)
		{
		}
	}

	private static class NestBall extends Item implements BallItem
	{
		private static final long serialVersionUID = 1L;

		public NestBall()
		{
			super("Nest Ball", "A somewhat different Pok\u00e9 Ball that works especially well on weaker Pok\u00e9mon in the wild.", BagCategory.BALL, 220);
			super.price = 1000;
			super.bcat.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (o.getLevel() <= 19) return new double[] {3, 0};
			else if (o.getLevel() <= 29) return new double[] {2, 0};
			else return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p)
		{
		}
	}

	private static class NetBall extends Item implements BallItem
	{
		private static final long serialVersionUID = 1L;

		public NetBall()
		{
			super("Net Ball", "A somewhat different Pok\u00e9 Ball that works especially well on Water- and Bug-type Pok\u00e9mon.", BagCategory.BALL, 221);
			super.price = 1000;
			super.bcat.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (o.isType(b, Type.WATER) || o.isType(b, Type.BUG)) return new double[] {3, 0};
			else return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p)
		{
		}
	}

	private static class PokeBall extends Item implements BallItem
	{
		private static final long serialVersionUID = 1L;

		public PokeBall()
		{
			super("Pok\u00e9 Ball", "A device for catching wild Pok\u00e9mon. It is thrown like a ball at the target. It is designed as a capsule system.", BagCategory.BALL, 222);
			super.price = 200;
			super.bcat.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b)
		{
			return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p)
		{
		}
	}

	private static class PremierBall extends Item implements BallItem
	{
		private static final long serialVersionUID = 1L;

		public PremierBall()
		{
			super("Premier Ball", "A somewhat rare Pok\u00e9 Ball that has been specially made to commemorate an event of some sort.", BagCategory.BALL, 223);
			super.price = 200;
			super.bcat.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b)
		{
			return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p)
		{
		}
	}

	private static class QuickBall extends Item implements BallItem
	{
		private static final long serialVersionUID = 1L;

		public QuickBall()
		{
			super("Quick Ball", "A somewhat different Pok\u00e9 Ball that provides a better catch rate if it is used at the start of a wild encounter.", BagCategory.BALL, 224);
			super.price = 1000;
			super.bcat.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (b.getTurn() == 1) return new double[] {3, 0};
			else return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p)
		{
		}
	}

	private static class RepeatBall extends Item implements BallItem
	{
		private static final long serialVersionUID = 1L;

		public RepeatBall()
		{
			super("Repeat Ball", "A somewhat different Pok\u00e9 Ball that works especially well on Pok\u00e9mon species that were previously caught.", BagCategory.BALL, 225);
			super.price = 1000;
			super.bcat.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (b.getPlayer().getPokedex().caught(o.getName())) return new double[] {3, 0};
			return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p)
		{
		}
	}

	private static class SafariBall extends Item implements BallItem
	{
		private static final long serialVersionUID = 1L;

		public SafariBall()
		{
			super("Safari Ball", "A special Pok\u00e9 Ball that is used only in the Safari Zone. It is decorated in a camouflage pattern.", BagCategory.BALL, 226);
			super.bcat.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b)
		{
			return new double[] {1.5, 0};
		}

		public void afterCaught(ActivePokemon p)
		{
		}
	}

	private static class TimerBall extends Item implements BallItem
	{
		private static final long serialVersionUID = 1L;

		public TimerBall()
		{
			super("Timer Ball", "A somewhat different Ball that becomes progressively better the more turns there are in a battle.", BagCategory.BALL, 227);
			super.price = 1000;
			super.bcat.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (b.getTurn() <= 10) return new double[] {1, 0};
			else if (b.getTurn() <= 20) return new double[] {2, 0};
			else if (b.getTurn() <= 30) return new double[] {3, 0};
			else return new double[] {4, 0};
		}

		public void afterCaught(ActivePokemon p)
		{
		}
	}

	private static class UltraBall extends Item implements BallItem
	{
		private static final long serialVersionUID = 1L;

		public UltraBall()
		{
			super("Ultra Ball", "An ultra-performance Ball that provides a higher Pok\u00e9mon catch rate than a Great Ball.", BagCategory.BALL, 228);
			super.price = 1200;
			super.bcat.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b)
		{
			return new double[] {2, 0};
		}

		public void afterCaught(ActivePokemon p)
		{
		}
	}

	private static class CheriBerry extends Item implements PokemonUseItem, BattleUseItem, StatusBerry
	{
		private static final long serialVersionUID = 1L;

		public CheriBerry()
		{
			super("Cheri Berry", "If held by a Pok\u00e9mon, it recovers from paralysis.", BagCategory.BERRY, 229);
			super.price = 20;
			super.bcat.add(BattleBagCategory.STATUS);
		}

		public StatusCondition toRemove()
		{
			return StatusCondition.PARALYZED;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " was cured of its status condition!";
		}

		public boolean use(ActivePokemon p)
		{
			if (!p.hasStatus(toRemove()))
			{
				return false;
			}
			
			p.removeStatus();
			return true;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p);
		}

		public void gainBerryEffect(Battle b, ActivePokemon user, ActivePokemon opp)
		{
			if (use(user, b))
			{
				b.addMessage(getSuccessMessage(user), user.getStatus().getType(), user.user());
			}
		}

		public int naturalGiftPower()
		{
			return 60;
		}

		public Type naturalGiftType()
		{
			return Type.FIRE;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class ChestoBerry extends Item implements PokemonUseItem, BattleUseItem, StatusBerry
	{
		private static final long serialVersionUID = 1L;

		public ChestoBerry()
		{
			super("Chesto Berry", "If held by a Pok\u00e9mon, it recovers from sleep.", BagCategory.BERRY, 230);
			super.price = 20;
			super.bcat.add(BattleBagCategory.STATUS);
		}

		public StatusCondition toRemove()
		{
			return StatusCondition.ASLEEP;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " was cured of its status condition!";
		}

		public boolean use(ActivePokemon p)
		{
			if (!p.hasStatus(toRemove()))
			{
				return false;
			}
			
			p.removeStatus();
			return true;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p);
		}

		public void gainBerryEffect(Battle b, ActivePokemon user, ActivePokemon opp)
		{
			if (use(user, b))
			{
				b.addMessage(getSuccessMessage(user), user.getStatus().getType(), user.user());
			}
		}

		public int naturalGiftPower()
		{
			return 60;
		}

		public Type naturalGiftType()
		{
			return Type.WATER;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class PechaBerry extends Item implements PokemonUseItem, BattleUseItem, StatusBerry
	{
		private static final long serialVersionUID = 1L;

		public PechaBerry()
		{
			super("Pecha Berry", "If held by a Pok\u00e9mon, it recovers from poison.", BagCategory.BERRY, 231);
			super.price = 20;
			super.bcat.add(BattleBagCategory.STATUS);
		}

		public StatusCondition toRemove()
		{
			return StatusCondition.POISONED;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " was cured of its status condition!";
		}

		public boolean use(ActivePokemon p)
		{
			if (!p.hasStatus(toRemove()))
			{
				return false;
			}
			
			p.removeStatus();
			return true;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p);
		}

		public void gainBerryEffect(Battle b, ActivePokemon user, ActivePokemon opp)
		{
			if (use(user, b))
			{
				b.addMessage(getSuccessMessage(user), user.getStatus().getType(), user.user());
			}
		}

		public int naturalGiftPower()
		{
			return 60;
		}

		public Type naturalGiftType()
		{
			return Type.ELECTRIC;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class RawstBerry extends Item implements PokemonUseItem, BattleUseItem, StatusBerry
	{
		private static final long serialVersionUID = 1L;

		public RawstBerry()
		{
			super("Rawst Berry", "If held by a Pok\u00e9mon, it recovers from a burn.", BagCategory.BERRY, 232);
			super.price = 20;
			super.bcat.add(BattleBagCategory.STATUS);
		}

		public StatusCondition toRemove()
		{
			return StatusCondition.BURNED;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " was cured of its status condition!";
		}

		public boolean use(ActivePokemon p)
		{
			if (!p.hasStatus(toRemove()))
			{
				return false;
			}
			
			p.removeStatus();
			return true;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p);
		}

		public void gainBerryEffect(Battle b, ActivePokemon user, ActivePokemon opp)
		{
			if (use(user, b))
			{
				b.addMessage(getSuccessMessage(user), user.getStatus().getType(), user.user());
			}
		}

		public int naturalGiftPower()
		{
			return 60;
		}

		public Type naturalGiftType()
		{
			return Type.GRASS;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class AspearBerry extends Item implements PokemonUseItem, BattleUseItem, StatusBerry
	{
		private static final long serialVersionUID = 1L;

		public AspearBerry()
		{
			super("Aspear Berry", "If held by a Pok\u00e9mon, it defrosts it.", BagCategory.BERRY, 233);
			super.price = 20;
			super.bcat.add(BattleBagCategory.STATUS);
		}

		public StatusCondition toRemove()
		{
			return StatusCondition.FROZEN;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " was cured of its status condition!";
		}

		public boolean use(ActivePokemon p)
		{
			if (!p.hasStatus(toRemove()))
			{
				return false;
			}
			
			p.removeStatus();
			return true;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p);
		}

		public void gainBerryEffect(Battle b, ActivePokemon user, ActivePokemon opp)
		{
			if (use(user, b))
			{
				b.addMessage(getSuccessMessage(user), user.getStatus().getType(), user.user());
			}
		}

		public int naturalGiftPower()
		{
			return 60;
		}

		public Type naturalGiftType()
		{
			return Type.GRASS;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class LeppaBerry extends Item implements EndTurnEffect, MoveUseItem, GainableEffectBerry
	{
		private static final long serialVersionUID = 1L;
private String restore;
		public LeppaBerry()
		{
			super("Leppa Berry", "If held by a Pok\u00e9mon, it restores a move's PP by 10.", BagCategory.BERRY, 234);
			super.price = 20;
			super.bcat.add(BattleBagCategory.HPPP);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + restore + "'s PP was restored!";
		}

		public void apply(ActivePokemon victim, Battle b)
		{
			for (Move m : victim.getMoves())
			{
				if (m.getPP() == 0)
				{
					b.addMessage(victim.getName() + "'s " + this.name + " restored " + m.getAttack().getName() + "'s PP!");
					m.increasePP(10);
					victim.consumeItem(b);
					break;
				}
			}
		}

		public boolean use(Move m)
		{
			restore = m.getAttack().getName();
			return m.increasePP(10);
		}

		public void gainBerryEffect(Battle b, ActivePokemon user, ActivePokemon opp)
		{
			List<Move> list = new ArrayList<>();
			for (Move m : user.getMoves())
			{
				if (m.getPP() < m.getMaxPP())
				{
					list.add(m);
				}
			}
			
			int size = list.size();
			if (size == 0) return;
			
			Move m = list.get((int)(Math.random()*size));
			m.increasePP(10);
			b.addMessage(user.getName() + "'s " + this.name + " restored " + m.getAttack().getName() + "'s PP!");
		}

		public int naturalGiftPower()
		{
			return 60;
		}

		public Type naturalGiftType()
		{
			return Type.FIGHTING;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class OranBerry extends Item implements PokemonUseItem, BattleUseItem, HealthTriggeredBerry
	{
		private static final long serialVersionUID = 1L;

		public OranBerry()
		{
			super("Oran Berry", "If held by a Pok\u00e9mon, it heals the user by just 10 HP.", BagCategory.BERRY, 235);
			super.price = 20;
			super.bcat.add(BattleBagCategory.HPPP);
		}

		public int healAmount()
		{
			return 10;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s health was restored!";
		}

		public boolean use(ActivePokemon p)
		{
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p);
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user)
		{
			if (user.fullHealth()) return false;
			
			user.heal(10);
			b.addMessage(user.getName() + " was healed by its " + this.name + "!", user.getHP(), user.user());
			return true;
		}

		public double healthTriggerRatio()
		{
			return 1/3.0;
		}

		public void gainBerryEffect(Battle b, ActivePokemon user, ActivePokemon opp)
		{
			useHealthTriggerBerry(b, user);
		}

		public int naturalGiftPower()
		{
			return 60;
		}

		public Type naturalGiftType()
		{
			return Type.POISON;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class PersimBerry extends Item implements BattleUseItem, GainableEffectBerry
	{
		private static final long serialVersionUID = 1L;

		public PersimBerry()
		{
			super("Persim Berry", "If held by a Pok\u00e9mon, it recovers from confusion.", BagCategory.BERRY, 236);
			super.price = 20;
			super.bcat.add(BattleBagCategory.HPPP);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " snapped out of its confusion!";
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			if (p.hasEffect("Confusion"))
			{
				p.getAttributes().removeEffect("Confusion");
				return true;
			}
			
			return false;
		}

		public void gainBerryEffect(Battle b, ActivePokemon user, ActivePokemon opp)
		{
			if (use(user, b))
			{
				b.addMessage(getSuccessMessage(user));
			}
		}

		public int naturalGiftPower()
		{
			return 60;
		}

		public Type naturalGiftType()
		{
			return Type.GROUND;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class LumBerry extends Item implements PokemonUseItem, BattleUseItem, StatusBerry
	{
		private static final long serialVersionUID = 1L;

		public LumBerry()
		{
			super("Lum Berry", "If held by a Pok\u00e9mon, it recovers from any status problem.", BagCategory.BERRY, 237);
			super.price = 20;
			super.bcat.add(BattleBagCategory.STATUS);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " was cured of its status condition!";
		}

		public boolean use(ActivePokemon p)
		{
			if (!p.hasStatus() || p.hasStatus(StatusCondition.FAINTED)) return false;
			
			p.removeStatus();
			return true;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p);
		}

		public void gainBerryEffect(Battle b, ActivePokemon user, ActivePokemon opp)
		{
			if (use(user, b))
			{
				b.addMessage(getSuccessMessage(user), user.getStatus().getType(), user.user());
			}
		}

		public int naturalGiftPower()
		{
			return 60;
		}

		public Type naturalGiftType()
		{
			return Type.FLYING;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class SitrusBerry extends Item implements PokemonUseItem, BattleUseItem, HealthTriggeredBerry
	{
		private static final long serialVersionUID = 1L;

		public SitrusBerry()
		{
			super("Sitrus Berry", "If held by a Pok\u00e9mon, it heals the user by a little.", BagCategory.BERRY, 238);
			super.price = 20;
			super.bcat.add(BattleBagCategory.HPPP);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s health was restored!";
		}

		public boolean use(ActivePokemon p)
		{
			return p.healHealthFraction(1/4.0) != 0;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p);
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user)
		{
			if (user.fullHealth()) return false;
			
			user.healHealthFraction(1/4.0);
			b.addMessage(user.getName() + " was healed by its " + this.name + "!", user.getHP(), user.user());
			return true;
		}

		public double healthTriggerRatio()
		{
			return 1/2.0;
		}

		public void gainBerryEffect(Battle b, ActivePokemon user, ActivePokemon opp)
		{
			useHealthTriggerBerry(b, user);
		}

		public int naturalGiftPower()
		{
			return 60;
		}

		public Type naturalGiftType()
		{
			return Type.PSYCHIC;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class RazzBerry extends Item implements Berry
	{
		private static final long serialVersionUID = 1L;

		public RazzBerry()
		{
			super("Razz Berry", "A very valuable berry. Useful for aquiring value.", BagCategory.BERRY, 239);
			super.price = 60000;
		}

		public int naturalGiftPower()
		{
			return 60;
		}

		public Type naturalGiftType()
		{
			return Type.STEEL;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class PomegBerry extends Item implements Berry, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public PomegBerry()
		{
			super("Pomeg Berry", "Using it on a Pok\u00e9mon lowers its base HP.", BagCategory.BERRY, 240);
			super.price = 20;
		}

		public Stat toDecrease()
		{
			return Stat.HP;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toDecrease().getName() + " was lowered!";
		}

		public int naturalGiftPower()
		{
			return 70;
		}

		public Type naturalGiftType()
		{
			return Type.ICE;
		}

		public boolean use(ActivePokemon p)
		{
			int[] vals = new int[Stat.NUM_STATS];
			if (p.getEV(toDecrease().index()) > 110)
			vals[toDecrease().index()] = 100 - p.getEV(toDecrease().index());
			else
			vals[toDecrease().index()] -= 10;
			
			return p.addEVs(vals);
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class KelpsyBerry extends Item implements Berry, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public KelpsyBerry()
		{
			super("Kelpsy Berry", "Using it on a Pok\u00e9mon lowers its base Attack stat.", BagCategory.BERRY, 241);
			super.price = 20;
		}

		public Stat toDecrease()
		{
			return Stat.ATTACK;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toDecrease().getName() + " was lowered!";
		}

		public int naturalGiftPower()
		{
			return 70;
		}

		public Type naturalGiftType()
		{
			return Type.FIGHTING;
		}

		public boolean use(ActivePokemon p)
		{
			int[] vals = new int[Stat.NUM_STATS];
			if (p.getEV(toDecrease().index()) > 110)
			vals[toDecrease().index()] = 100 - p.getEV(toDecrease().index());
			else
			vals[toDecrease().index()] -= 10;
			
			return p.addEVs(vals);
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class QualotBerry extends Item implements Berry, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public QualotBerry()
		{
			super("Qualot Berry", "Using it on a Pok\u00e9mon lowers its base Defense stat.", BagCategory.BERRY, 242);
			super.price = 20;
		}

		public Stat toDecrease()
		{
			return Stat.DEFENSE;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toDecrease().getName() + " was lowered!";
		}

		public int naturalGiftPower()
		{
			return 70;
		}

		public Type naturalGiftType()
		{
			return Type.POISON;
		}

		public boolean use(ActivePokemon p)
		{
			int[] vals = new int[Stat.NUM_STATS];
			if (p.getEV(toDecrease().index()) > 110)
			vals[toDecrease().index()] = 100 - p.getEV(toDecrease().index());
			else
			vals[toDecrease().index()] -= 10;
			
			return p.addEVs(vals);
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class HondewBerry extends Item implements Berry, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public HondewBerry()
		{
			super("Hondew Berry", "Using it on a Pok\u00e9mon lowers its base Sp. Atk stat.", BagCategory.BERRY, 243);
			super.price = 20;
		}

		public Stat toDecrease()
		{
			return Stat.SP_ATTACK;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toDecrease().getName() + " was lowered!";
		}

		public int naturalGiftPower()
		{
			return 70;
		}

		public Type naturalGiftType()
		{
			return Type.GROUND;
		}

		public boolean use(ActivePokemon p)
		{
			int[] vals = new int[Stat.NUM_STATS];
			if (p.getEV(toDecrease().index()) > 110)
			vals[toDecrease().index()] = 100 - p.getEV(toDecrease().index());
			else
			vals[toDecrease().index()] -= 10;
			
			return p.addEVs(vals);
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class GrepaBerry extends Item implements Berry, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public GrepaBerry()
		{
			super("Grepa Berry", "Using it on a Pok\u00e9mon lowers its base Sp. Def stat.", BagCategory.BERRY, 244);
			super.price = 20;
		}

		public Stat toDecrease()
		{
			return Stat.SP_DEFENSE;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toDecrease().getName() + " was lowered!";
		}

		public int naturalGiftPower()
		{
			return 70;
		}

		public Type naturalGiftType()
		{
			return Type.FLYING;
		}

		public boolean use(ActivePokemon p)
		{
			int[] vals = new int[Stat.NUM_STATS];
			if (p.getEV(toDecrease().index()) > 110)
			vals[toDecrease().index()] = 100 - p.getEV(toDecrease().index());
			else
			vals[toDecrease().index()] -= 10;
			
			return p.addEVs(vals);
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class TamatoBerry extends Item implements Berry, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public TamatoBerry()
		{
			super("Tamato Berry", "Using it on a Pok\u00e9mon lowers its base Speed.", BagCategory.BERRY, 245);
			super.price = 20;
		}

		public Stat toDecrease()
		{
			return Stat.SPEED;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toDecrease().getName() + " was lowered!";
		}

		public int naturalGiftPower()
		{
			return 70;
		}

		public Type naturalGiftType()
		{
			return Type.PSYCHIC;
		}

		public boolean use(ActivePokemon p)
		{
			int[] vals = new int[Stat.NUM_STATS];
			if (p.getEV(toDecrease().index()) > 110)
			vals[toDecrease().index()] = 100 - p.getEV(toDecrease().index());
			else
			vals[toDecrease().index()] -= 10;
			
			return p.addEVs(vals);
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class OccaBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public OccaBerry()
		{
			super("Occa Berry", "Weakens a supereffective Fire-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 246);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.FIRE;
		}

		public int naturalGiftPower()
		{
			return 60;
		}

		public Type naturalGiftType()
		{
			return Type.FIRE;
		}

		public double getOppMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Type t = user.getAttack().getType(b, user);
			
			if (t == getType() && Type.getAdvantage(t, victim, b) > 1)
			{
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class PasshoBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public PasshoBerry()
		{
			super("Passho Berry", "Weakens a supereffective Water-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 247);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.WATER;
		}

		public int naturalGiftPower()
		{
			return 60;
		}

		public Type naturalGiftType()
		{
			return Type.WATER;
		}

		public double getOppMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Type t = user.getAttack().getType(b, user);
			
			if (t == getType() && Type.getAdvantage(t, victim, b) > 1)
			{
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class WacanBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public WacanBerry()
		{
			super("Wacan Berry", "Weakens a supereffective Electric-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 248);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.ELECTRIC;
		}

		public int naturalGiftPower()
		{
			return 60;
		}

		public Type naturalGiftType()
		{
			return Type.ELECTRIC;
		}

		public double getOppMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Type t = user.getAttack().getType(b, user);
			
			if (t == getType() && Type.getAdvantage(t, victim, b) > 1)
			{
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class RindoBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public RindoBerry()
		{
			super("Rindo Berry", "Weakens a supereffective Grass-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 249);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.GRASS;
		}

		public int naturalGiftPower()
		{
			return 60;
		}

		public Type naturalGiftType()
		{
			return Type.GRASS;
		}

		public double getOppMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Type t = user.getAttack().getType(b, user);
			
			if (t == getType() && Type.getAdvantage(t, victim, b) > 1)
			{
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class YacheBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public YacheBerry()
		{
			super("Yache Berry", "Weakens a supereffective Ice-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 250);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.ICE;
		}

		public int naturalGiftPower()
		{
			return 60;
		}

		public Type naturalGiftType()
		{
			return Type.ICE;
		}

		public double getOppMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Type t = user.getAttack().getType(b, user);
			
			if (t == getType() && Type.getAdvantage(t, victim, b) > 1)
			{
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class ChopleBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public ChopleBerry()
		{
			super("Chople Berry", "Weakens a supereffective Fighting-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 251);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.FIGHTING;
		}

		public int naturalGiftPower()
		{
			return 60;
		}

		public Type naturalGiftType()
		{
			return Type.FIGHTING;
		}

		public double getOppMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Type t = user.getAttack().getType(b, user);
			
			if (t == getType() && Type.getAdvantage(t, victim, b) > 1)
			{
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class KebiaBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public KebiaBerry()
		{
			super("Kebia Berry", "Weakens a supereffective Poison-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 252);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.POISON;
		}

		public int naturalGiftPower()
		{
			return 60;
		}

		public Type naturalGiftType()
		{
			return Type.POISON;
		}

		public double getOppMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Type t = user.getAttack().getType(b, user);
			
			if (t == getType() && Type.getAdvantage(t, victim, b) > 1)
			{
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class ShucaBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public ShucaBerry()
		{
			super("Shuca Berry", "Weakens a supereffective Ground-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 253);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.GROUND;
		}

		public int naturalGiftPower()
		{
			return 60;
		}

		public Type naturalGiftType()
		{
			return Type.GROUND;
		}

		public double getOppMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Type t = user.getAttack().getType(b, user);
			
			if (t == getType() && Type.getAdvantage(t, victim, b) > 1)
			{
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class CobaBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public CobaBerry()
		{
			super("Coba Berry", "Weakens a supereffective Flying-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 254);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.FLYING;
		}

		public int naturalGiftPower()
		{
			return 60;
		}

		public Type naturalGiftType()
		{
			return Type.FLYING;
		}

		public double getOppMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Type t = user.getAttack().getType(b, user);
			
			if (t == getType() && Type.getAdvantage(t, victim, b) > 1)
			{
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class PayapaBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public PayapaBerry()
		{
			super("Payapa Berry", "Weakens a supereffective Psychic-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 255);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.PSYCHIC;
		}

		public int naturalGiftPower()
		{
			return 60;
		}

		public Type naturalGiftType()
		{
			return Type.PSYCHIC;
		}

		public double getOppMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Type t = user.getAttack().getType(b, user);
			
			if (t == getType() && Type.getAdvantage(t, victim, b) > 1)
			{
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class TangaBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public TangaBerry()
		{
			super("Tanga Berry", "Weakens a supereffective Bug-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 256);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.BUG;
		}

		public int naturalGiftPower()
		{
			return 60;
		}

		public Type naturalGiftType()
		{
			return Type.BUG;
		}

		public double getOppMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Type t = user.getAttack().getType(b, user);
			
			if (t == getType() && Type.getAdvantage(t, victim, b) > 1)
			{
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class ChartiBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public ChartiBerry()
		{
			super("Charti Berry", "Weakens a supereffective Rock-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 257);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.ROCK;
		}

		public int naturalGiftPower()
		{
			return 60;
		}

		public Type naturalGiftType()
		{
			return Type.ROCK;
		}

		public double getOppMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Type t = user.getAttack().getType(b, user);
			
			if (t == getType() && Type.getAdvantage(t, victim, b) > 1)
			{
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class KasibBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public KasibBerry()
		{
			super("Kasib Berry", "Weakens a supereffective Ghost-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 258);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.GHOST;
		}

		public int naturalGiftPower()
		{
			return 60;
		}

		public Type naturalGiftType()
		{
			return Type.GHOST;
		}

		public double getOppMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Type t = user.getAttack().getType(b, user);
			
			if (t == getType() && Type.getAdvantage(t, victim, b) > 1)
			{
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class HabanBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public HabanBerry()
		{
			super("Haban Berry", "Weakens a supereffective Dragon-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 259);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.DRAGON;
		}

		public int naturalGiftPower()
		{
			return 60;
		}

		public Type naturalGiftType()
		{
			return Type.DRAGON;
		}

		public double getOppMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Type t = user.getAttack().getType(b, user);
			
			if (t == getType() && Type.getAdvantage(t, victim, b) > 1)
			{
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class ColburBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public ColburBerry()
		{
			super("Colbur Berry", "Weakens a supereffective Dark-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 260);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.DARK;
		}

		public int naturalGiftPower()
		{
			return 60;
		}

		public Type naturalGiftType()
		{
			return Type.DARK;
		}

		public double getOppMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Type t = user.getAttack().getType(b, user);
			
			if (t == getType() && Type.getAdvantage(t, victim, b) > 1)
			{
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class BabiriBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public BabiriBerry()
		{
			super("Babiri Berry", "Weakens a supereffective Steel-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 261);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.STEEL;
		}

		public int naturalGiftPower()
		{
			return 60;
		}

		public Type naturalGiftType()
		{
			return Type.STEEL;
		}

		public double getOppMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Type t = user.getAttack().getType(b, user);
			
			if (t == getType() && Type.getAdvantage(t, victim, b) > 1)
			{
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class ChilanBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public ChilanBerry()
		{
			super("Chilan Berry", "Weakens a supereffective Normal-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 262);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.NORMAL;
		}

		public int naturalGiftPower()
		{
			return 60;
		}

		public Type naturalGiftType()
		{
			return Type.NORMAL;
		}

		public double getOppMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Type t = user.getAttack().getType(b, user);
			
			if (t == getType() && Type.getAdvantage(t, victim, b) > 1)
			{
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class LiechiBerry extends Item implements HealthTriggeredBerry
	{
		private static final long serialVersionUID = 1L;

		public LiechiBerry()
		{
			super("Liechi Berry", "If held by a Pok\u00e9mon, it raises its Attack stat in a pinch.", BagCategory.BERRY, 263);
			super.price = 20;
		}

		public Stat toRaise()
		{
			return Stat.ATTACK;
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user)
		{
			if (user.getAttributes().modifyStage(user, user, 1, toRaise(), b, CastSource.HELD_ITEM))
			{
				return true;
			}
			
			return false;
		}

		public double healthTriggerRatio()
		{
			return 1/4.0;
		}

		public void gainBerryEffect(Battle b, ActivePokemon user, ActivePokemon opp)
		{
			useHealthTriggerBerry(b, user);
		}

		public int naturalGiftPower()
		{
			return 80;
		}

		public Type naturalGiftType()
		{
			return Type.GRASS;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class GanlonBerry extends Item implements HealthTriggeredBerry
	{
		private static final long serialVersionUID = 1L;

		public GanlonBerry()
		{
			super("Ganlon Berry", "If held by a Pok\u00e9mon, it raises its Defense stat in a pinch.", BagCategory.BERRY, 264);
			super.price = 20;
		}

		public Stat toRaise()
		{
			return Stat.DEFENSE;
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user)
		{
			if (user.getAttributes().modifyStage(user, user, 1, toRaise(), b, CastSource.HELD_ITEM))
			{
				return true;
			}
			
			return false;
		}

		public double healthTriggerRatio()
		{
			return 1/4.0;
		}

		public void gainBerryEffect(Battle b, ActivePokemon user, ActivePokemon opp)
		{
			useHealthTriggerBerry(b, user);
		}

		public int naturalGiftPower()
		{
			return 80;
		}

		public Type naturalGiftType()
		{
			return Type.ICE;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class SalacBerry extends Item implements HealthTriggeredBerry
	{
		private static final long serialVersionUID = 1L;

		public SalacBerry()
		{
			super("Salac Berry", "If held by a Pok\u00e9mon, it raises its Speed stat in a pinch.", BagCategory.BERRY, 265);
			super.price = 20;
		}

		public Stat toRaise()
		{
			return Stat.SPEED;
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user)
		{
			if (user.getAttributes().modifyStage(user, user, 1, toRaise(), b, CastSource.HELD_ITEM))
			{
				return true;
			}
			
			return false;
		}

		public double healthTriggerRatio()
		{
			return 1/4.0;
		}

		public void gainBerryEffect(Battle b, ActivePokemon user, ActivePokemon opp)
		{
			useHealthTriggerBerry(b, user);
		}

		public int naturalGiftPower()
		{
			return 80;
		}

		public Type naturalGiftType()
		{
			return Type.FIGHTING;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class PetayaBerry extends Item implements HealthTriggeredBerry
	{
		private static final long serialVersionUID = 1L;

		public PetayaBerry()
		{
			super("Petaya Berry", "If held by a Pok\u00e9mon, it raises its Speed stat in a pinch.", BagCategory.BERRY, 266);
			super.price = 20;
		}

		public Stat toRaise()
		{
			return Stat.SP_ATTACK;
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user)
		{
			if (user.getAttributes().modifyStage(user, user, 1, toRaise(), b, CastSource.HELD_ITEM))
			{
				return true;
			}
			
			return false;
		}

		public double healthTriggerRatio()
		{
			return 1/4.0;
		}

		public void gainBerryEffect(Battle b, ActivePokemon user, ActivePokemon opp)
		{
			useHealthTriggerBerry(b, user);
		}

		public int naturalGiftPower()
		{
			return 80;
		}

		public Type naturalGiftType()
		{
			return Type.POISON;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class ApicotBerry extends Item implements HealthTriggeredBerry
	{
		private static final long serialVersionUID = 1L;

		public ApicotBerry()
		{
			super("Apicot Berry", "If held by a Pok\u00e9mon, it raises its Speed stat in a pinch.", BagCategory.BERRY, 267);
			super.price = 20;
		}

		public Stat toRaise()
		{
			return Stat.SP_DEFENSE;
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user)
		{
			if (user.getAttributes().modifyStage(user, user, 1, toRaise(), b, CastSource.HELD_ITEM))
			{
				return true;
			}
			
			return false;
		}

		public double healthTriggerRatio()
		{
			return 1/4.0;
		}

		public void gainBerryEffect(Battle b, ActivePokemon user, ActivePokemon opp)
		{
			useHealthTriggerBerry(b, user);
		}

		public int naturalGiftPower()
		{
			return 80;
		}

		public Type naturalGiftType()
		{
			return Type.GROUND;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class LansatBerry extends Item implements HealthTriggeredBerry
	{
		private static final long serialVersionUID = 1L;

		public LansatBerry()
		{
			super("Lansat Berry", "If held by a Pok\u00e9mon, it raises its critical-hit ratio in a pinch.", BagCategory.BERRY, 268);
			super.price = 20;
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user)
		{
			PokemonEffect.getEffect("RaiseCrits").cast(b, user, user, CastSource.HELD_ITEM, false);
			b.addMessage(user.getName() + " is getting pumped due to its " + this.name + "!");
			return true;
		}

		public double healthTriggerRatio()
		{
			return 1/4.0;
		}

		public void gainBerryEffect(Battle b, ActivePokemon user, ActivePokemon opp)
		{
			useHealthTriggerBerry(b, user);
		}

		public int naturalGiftPower()
		{
			return 80;
		}

		public Type naturalGiftType()
		{
			return Type.FLYING;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class StarfBerry extends Item implements HealthTriggeredBerry
	{
		private static final long serialVersionUID = 1L;

		public StarfBerry()
		{
			super("Starf Berry", "If held by a Pok\u00e9mon, it raises a random stat in a pinch.", BagCategory.BERRY, 269);
			super.price = 20;
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user)
		{
			int rand = (int)(Math.random()*(Stat.NUM_BATTLE_STATS + 1));
			
			// Raise crit
			if (rand == Stat.NUM_BATTLE_STATS)
			{
				PokemonEffect.getEffect("RaiseCrits").cast(b, user, user, CastSource.HELD_ITEM, false);
				b.addMessage(user.getName() + " is getting pumped due to its " + this.name + "!");
				return true;
			}
			
			// Raise random battle stat
			if (user.getAttributes().modifyStage(user, user, 1, Stat.getStat(rand, true), b, CastSource.HELD_ITEM))
			{
				return true;
			}
			
			return false;
		}

		public double healthTriggerRatio()
		{
			return 1/4.0;
		}

		public void gainBerryEffect(Battle b, ActivePokemon user, ActivePokemon opp)
		{
			useHealthTriggerBerry(b, user);
		}

		public int naturalGiftPower()
		{
			return 80;
		}

		public Type naturalGiftType()
		{
			return Type.PSYCHIC;
		}

		public int flingDamage()
		{
			return 10;
		}
	}

	private static class CometShard extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public CometShard()
		{
			super("Comet Shard", "A shard which fell to the ground when a comet approached. A maniac will buy it for a high price.", BagCategory.MISC, 270);
			super.price = 120000;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class TinyMushroom extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public TinyMushroom()
		{
			super("Tiny Mushroom", "A small and rare mushroom. It is sought after by collectors.", BagCategory.MISC, 271);
			super.price = 500;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class BigMushroom extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public BigMushroom()
		{
			super("Big Mushroom", "A large and rare mushroom. It is sought after by collectors.", BagCategory.MISC, 272);
			super.price = 5000;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class BalmMushroom extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public BalmMushroom()
		{
			super("Balm Mushroom", "A rare mushroom which gives off a nice fragrance. A maniac will buy it for a high price.", BagCategory.MISC, 273);
			super.price = 50000;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class Nugget extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public Nugget()
		{
			super("Nugget", "A nugget of pure gold that gives off a lustrous gleam. It can be sold at a high price to shops.", BagCategory.MISC, 274);
			super.price = 10000;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class BigNugget extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public BigNugget()
		{
			super("Big Nugget", "A big nugget of pure gold that gives off a lustrous gleam. A maniac will buy it for a high price.", BagCategory.MISC, 275);
			super.price = 60000;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class Pearl extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public Pearl()
		{
			super("Pearl", "A somewhat-small pearl that sparkles in a pretty silver color. It can be sold cheaply to shops.", BagCategory.MISC, 276);
			super.price = 1400;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class BigPearl extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public BigPearl()
		{
			super("Big Pearl", "A quite-large pearl that sparkles in a pretty silver color. It can be sold at a high price to shops.", BagCategory.MISC, 277);
			super.price = 7500;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class Stardust extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public Stardust()
		{
			super("Stardust", "Lovely, red-colored sand with a loose, silky feel. It can be sold at a high price to shops.", BagCategory.MISC, 278);
			super.price = 2000;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class StarPiece extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public StarPiece()
		{
			super("Star Piece", "A shard of a pretty gem that sparkles in a red color. It can be sold at a high price to shops.", BagCategory.MISC, 279);
			super.price = 9800;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class RareBone extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public RareBone()
		{
			super("Rare Bone", "A bone that is extremely valuable for Pokémon archeology. It can be sold for a high price to shops.", BagCategory.MISC, 280);
			super.price = 10000;
		}

		public int flingDamage()
		{
			return 100;
		}
	}

	private static class Honey extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public Honey()
		{
			super("Honey", "A sweet honey with a lush aroma that attracts wild Pokémon when it is used in grass, caves, or on special trees.", BagCategory.MISC, 281);
			super.price = 100;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class Eviolite extends Item implements HoldItem, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public Eviolite()
		{
			super("Eviolite", "A mysterious evolutionary lump. When held, it raises the Defense and Sp. Def of a Pokémon that can still evolve.", BagCategory.MISC, 282);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 40;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if ((s == Stat.DEFENSE || s == Stat.SP_DEFENSE) && p.getPokemonInfo().getEvolution().canEvolve()) return (int)(1.5*stat);
			return stat;
		}
	}

	private static class HeartScale extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public HeartScale()
		{
			super("Heart Scale", "A pretty, heart-shaped scale that is extremely rare. It glows faintly in the colors of the rainbow.", BagCategory.MISC, 283);
			super.price = 100;
		}

		public int flingDamage()
		{
			return 30;
		}
	}

	private static class Repel extends Item implements HoldItem, TrainerUseItem
	{
		private static final long serialVersionUID = 1L;

		public Repel()
		{
			super("Repel", "An item that prevents weak wild Pok\u00e9mon from appearing for 100 steps after its use.", BagCategory.MISC, 284);
			super.price = 350;
		}

		public int repelSteps()
		{
			return 100;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "Weak wild Pok\u00e9mon will not appear for " + repelSteps() + " steps!";
		}

		public int flingDamage()
		{
			return 30;
		}

		public boolean use(Trainer t)
		{
			if (!(t instanceof CharacterData))
			{
				Global.error("Only the character should be using a Repel item");
			}
			
			CharacterData player = (CharacterData) t;
			if (player.isUsingRepel())
			{
				return false;
			}
			
			player.addRepelSteps(repelSteps());
			return true;
		}
	}

	private static class SuperRepel extends Item implements HoldItem, TrainerUseItem
	{
		private static final long serialVersionUID = 1L;

		public SuperRepel()
		{
			super("Super Repel", "An item that prevents weak wild Pokémon from appearing for 200 steps after its use.", BagCategory.MISC, 285);
			super.price = 500;
		}

		public int repelSteps()
		{
			return 200;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "Weak wild Pok\u00e9mon will not appear for " + repelSteps() + " steps!";
		}

		public int flingDamage()
		{
			return 30;
		}

		public boolean use(Trainer t)
		{
			if (!(t instanceof CharacterData))
			{
				Global.error("Only the character should be using a Repel item");
			}
			
			CharacterData player = (CharacterData) t;
			if (player.isUsingRepel())
			{
				return false;
			}
			
			player.addRepelSteps(repelSteps());
			return true;
		}
	}

	private static class MaxRepel extends Item implements HoldItem, TrainerUseItem
	{
		private static final long serialVersionUID = 1L;

		public MaxRepel()
		{
			super("Max Repel", "An item that prevents weak wild Pokémon from appearing for 250 steps after its use.", BagCategory.MISC, 286);
			super.price = 700;
		}

		public int repelSteps()
		{
			return 250;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "Weak wild Pok\u00e9mon will not appear for " + repelSteps() + " steps!";
		}

		public int flingDamage()
		{
			return 30;
		}

		public boolean use(Trainer t)
		{
			if (!(t instanceof CharacterData))
			{
				Global.error("Only the character should be using a Repel item");
			}
			
			CharacterData player = (CharacterData) t;
			if (player.isUsingRepel())
			{
				return false;
			}
			
			player.addRepelSteps(repelSteps());
			return true;
		}
	}
}
