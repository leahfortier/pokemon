package item;

import item.Bag.BagCategory;
import item.Bag.BattleBagCategory;
import item.berry.Berry;
import item.berry.GainableEffectBerry;
import item.berry.HealthTriggeredBerry;
import item.berry.StatusBerry;
import item.hold.ConsumableItem;
import item.hold.DriveItem;
import item.hold.EVItem;
import item.hold.GemItem;
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
import main.Namesies;
import main.Namesies.NamesiesType;
import main.Type;
import pokemon.Ability;
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
import battle.Attack;
import battle.Attack.Category;
import battle.Attack.MoveType;
import battle.Battle;
import battle.Move;
import battle.effect.AdvantageChanger;
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
import battle.effect.PriorityChangeEffect;
import battle.effect.RepellingEffect;
import battle.effect.StallingEffect;
import battle.effect.StatChangingEffect;
import battle.effect.StatProtectingEffect;
import battle.effect.Status;
import battle.effect.Status.StatusCondition;
import battle.effect.TakeDamageEffect;
import battle.effect.TeamEffect;
import battle.effect.WeatherBlockerEffect;
import battle.effect.WeatherExtendingEffect;

public abstract class Item implements Comparable<Item>, Serializable
{
	private static final long serialVersionUID = 1L;

	private static HashMap<String, Item> map;

	protected Namesies namesies;
	protected String name, desc;
	protected BagCategory cat;
	protected List<BattleBagCategory> bcat;
	protected int price, index;

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

	public Item(Namesies name, String description, BagCategory category, int index)
	{
		this.namesies = name;
		this.name = name.getName();
		this.desc = description;
		this.cat = category;
		this.index = index;

		this.bcat = new ArrayList<>();
		this.price = -1;
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

	public boolean hasQuantity()
	{
		return cat != BagCategory.TM && cat != BagCategory.KEY_ITEM;
	}

	public Namesies namesies()
	{
		return namesies;
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
		return getItem(Namesies.NONE_ITEM);
	}

	// SRSLY DON'T CALL THIS UNLESS YOU'RE READING IT FROM A FILE OR THE DEV
	// CONSOLE OR SOMETHING LIKE THAT I SWEAR TO GOD THIS IS NOT A JOKE FUCK YOU
	public static Item getItemFromName(String m)
	{
		if (isItem(m))
		{
			return map.get(m);
		}

		Global.error("No such Item " + m);
		return null;
	}

	public static Item getItem(Namesies m)
	{
		return getItemFromName(m.getName());
	}

	public static boolean isItem(String m)
	{
		if (map == null)
		{
			loadItems();
		}

		if (map.containsKey(m))
		{
			return true;
		}

		return false;
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
		map.put("Adamant Orb", new AdamantOrb());
		map.put("Lustrous Orb", new LustrousOrb());
		map.put("Griseous Orb", new GriseousOrb());
		map.put("Iron Ball", new IronBall());
		map.put("Lagging Tail", new LaggingTail());
		map.put("Life Orb", new LifeOrb());
		map.put("Light Ball", new LightBall());
		map.put("Light Clay", new LightClay());
		map.put("Lucky Egg", new LuckyEgg());
		map.put("Lucky Punch", new LuckyPunch());
		map.put("Luminous Moss", new LuminousMoss());
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
		map.put("Pixie Plate", new PixiePlate());
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
		map.put("Deep Sea Scale", new DeepSeaScale());
		map.put("Deep Sea Tooth", new DeepSeaTooth());
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
		map.put("Sachet", new Sachet());
		map.put("Shiny Stone", new ShinyStone());
		map.put("Sun Stone", new SunStone());
		map.put("Thunder Stone", new ThunderStone());
		map.put("Up-Grade", new UpGrade());
		map.put("Water Stone", new WaterStone());
		map.put("Whipped Dream", new WhippedDream());
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
		map.put("X Special", new XSpecial());
		map.put("X Sp. Def", new XSpDef());
		map.put("X Speed", new XSpeed());
		map.put("HP Up", new HPUp());
		map.put("Protein", new Protein());
		map.put("Iron", new Iron());
		map.put("Calcium", new Calcium());
		map.put("Zinc", new Zinc());
		map.put("Carbos", new Carbos());
		map.put("Health Wing", new HealthWing());
		map.put("Muscle Wing", new MuscleWing());
		map.put("Resist Wing", new ResistWing());
		map.put("Genius Wing", new GeniusWing());
		map.put("Clever Wing", new CleverWing());
		map.put("Swift Wing", new SwiftWing());
		map.put("PP Max", new PPMax());
		map.put("PP Up", new PPUp());
		map.put("Rare Candy", new RareCandy());
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
		map.put("Roseli Berry", new RoseliBerry());
		map.put("Liechi Berry", new LiechiBerry());
		map.put("Ganlon Berry", new GanlonBerry());
		map.put("Salac Berry", new SalacBerry());
		map.put("Petaya Berry", new PetayaBerry());
		map.put("Apicot Berry", new ApicotBerry());
		map.put("Micle Berry", new MicleBerry());
		map.put("Kee Berry", new KeeBerry());
		map.put("Maranga Berry", new MarangaBerry());
		map.put("Jaboca Berry", new JabocaBerry());
		map.put("Rowap Berry", new RowapBerry());
		map.put("Custap Berry", new CustapBerry());
		map.put("Enigma Berry", new EnigmaBerry());
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
		map.put("Ability Capsule", new AbilityCapsule());
		map.put("Assault Vest", new AssaultVest());
		map.put("Hone Claws TM", new HoneClawsTM());
		map.put("Dragon Claw TM", new DragonClawTM());
		map.put("Psyshock TM", new PsyshockTM());
		map.put("Calm Mind TM", new CalmMindTM());
		map.put("Roar TM", new RoarTM());
		map.put("Toxic TM", new ToxicTM());
		map.put("Hail TM", new HailTM());
		map.put("Bulk Up TM", new BulkUpTM());
		map.put("Venoshock TM", new VenoshockTM());
		map.put("Hidden Power TM", new HiddenPowerTM());
		map.put("Sunny Day TM", new SunnyDayTM());
		map.put("Taunt TM", new TauntTM());
		map.put("Ice Beam TM", new IceBeamTM());
		map.put("Blizzard TM", new BlizzardTM());
		map.put("Hyper Beam TM", new HyperBeamTM());
		map.put("Light Screen TM", new LightScreenTM());
		map.put("Protect TM", new ProtectTM());
		map.put("Rain Dance TM", new RainDanceTM());
		map.put("Roost TM", new RoostTM());
		map.put("Safeguard TM", new SafeguardTM());
		map.put("Solar Beam TM", new SolarBeamTM());
		map.put("Smack Down TM", new SmackDownTM());
		map.put("Thunderbolt TM", new ThunderboltTM());
		map.put("Thunder TM", new ThunderTM());
		map.put("Earthquake TM", new EarthquakeTM());
		map.put("Dig TM", new DigTM());
		map.put("Psychic TM", new PsychicTM());
		map.put("Shadow Ball TM", new ShadowBallTM());
		map.put("Brick Break TM", new BrickBreakTM());
		map.put("Double Team TM", new DoubleTeamTM());
		map.put("Reflect TM", new ReflectTM());
		map.put("Sludge Wave TM", new SludgeWaveTM());
		map.put("Flamethrower TM", new FlamethrowerTM());
		map.put("Sludge Bomb TM", new SludgeBombTM());
		map.put("Sandstorm TM", new SandstormTM());
		map.put("Fire Blast TM", new FireBlastTM());
		map.put("Rock Tomb TM", new RockTombTM());
		map.put("Aerial Ace TM", new AerialAceTM());
		map.put("Torment TM", new TormentTM());
		map.put("Facade TM", new FacadeTM());
		map.put("Flame Charge TM", new FlameChargeTM());
		map.put("Rest TM", new RestTM());
		map.put("Attract TM", new AttractTM());
		map.put("Thief TM", new ThiefTM());
		map.put("Low Sweep TM", new LowSweepTM());
		map.put("Round TM", new RoundTM());
		map.put("Echoed Voice TM", new EchoedVoiceTM());
		map.put("Overheat TM", new OverheatTM());
		map.put("Steel Wing TM", new SteelWingTM());
		map.put("Focus Blast TM", new FocusBlastTM());
		map.put("Energy Ball TM", new EnergyBallTM());
		map.put("False Swipe TM", new FalseSwipeTM());
		map.put("Scald TM", new ScaldTM());
		map.put("Fling TM", new FlingTM());
		map.put("Charge Beam TM", new ChargeBeamTM());
		map.put("Sky Drop TM", new SkyDropTM());
		map.put("Incinerate TM", new IncinerateTM());
		map.put("Will-O-Wisp TM", new WillOWispTM());
		map.put("Acrobatics TM", new AcrobaticsTM());
		map.put("Embargo TM", new EmbargoTM());
		map.put("Explosion TM", new ExplosionTM());
		map.put("Shadow Claw TM", new ShadowClawTM());
		map.put("Payback TM", new PaybackTM());
		map.put("Retaliate TM", new RetaliateTM());
		map.put("Giga Impact TM", new GigaImpactTM());
		map.put("Rock Polish TM", new RockPolishTM());
		map.put("Flash TM", new FlashTM());
		map.put("Stone Edge TM", new StoneEdgeTM());
		map.put("Volt Switch TM", new VoltSwitchTM());
		map.put("Thunder Wave TM", new ThunderWaveTM());
		map.put("Gyro Ball TM", new GyroBallTM());
		map.put("Swords Dance TM", new SwordsDanceTM());
		map.put("Struggle Bug TM", new StruggleBugTM());
		map.put("Psych Up TM", new PsychUpTM());
		map.put("Bulldoze TM", new BulldozeTM());
		map.put("Frost Breath TM", new FrostBreathTM());
		map.put("Rock Slide TM", new RockSlideTM());
		map.put("X-Scissor TM", new XScissorTM());
		map.put("Dragon Tail TM", new DragonTailTM());
		map.put("Infestation TM", new InfestationTM());
		map.put("Poison Jab TM", new PoisonJabTM());
		map.put("Dream Eater TM", new DreamEaterTM());
		map.put("Grass Knot TM", new GrassKnotTM());
		map.put("Swagger TM", new SwaggerTM());
		map.put("Sleep Talk TM", new SleepTalkTM());
		map.put("U-turn TM", new UTurnTM());
		map.put("Substitute TM", new SubstituteTM());
		map.put("Flash Cannon TM", new FlashCannonTM());
		map.put("Trick Room TM", new TrickRoomTM());
		map.put("Wild Charge TM", new WildChargeTM());
		map.put("Rock Smash TM", new RockSmashTM());
		map.put("Snarl TM", new SnarlTM());
		map.put("Nature Power TM", new NaturePowerTM());
		map.put("Dark Pulse TM", new DarkPulseTM());
		map.put("Power-Up Punch TM", new PowerUpPunchTM());
		map.put("Dazzling Gleam TM", new DazzlingGleamTM());
		map.put("Confide TM", new ConfideTM());
		map.put("Cut TM", new CutTM());
		map.put("Fly TM", new FlyTM());
		map.put("Surf TM", new SurfTM());
		map.put("Strength TM", new StrengthTM());
		map.put("Waterfall TM", new WaterfallTM());
	}

	/**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

	private static class None extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public None()
		{
			super(Namesies.NONE_ITEM, "YOU SHUOLDN'T SEE THIS", BagCategory.MISC, 0);
			super.price = -1;
		}

		public int flingDamage()
		{
			return 9001;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class Syrup extends Item implements TrainerUseItem
	{
		private static final long serialVersionUID = 1L;

		public Syrup()
		{
			super(Namesies.SYRUP_ITEM, "A mysterious bottle of syrup. Maybe it will be useful some day.", BagCategory.KEY_ITEM, 1);
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
			super(Namesies.BICYCLE_ITEM, "A folding Bicycle that enables much faster movement than the Running Shoes.", BagCategory.KEY_ITEM, 2);
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
			super(Namesies.SURFBOARD_ITEM, "A fancy shmancy surfboard that lets you be RADICAL DUDE!", BagCategory.KEY_ITEM, 3);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public boolean use(Trainer t)
		{
			// TODO: DOESN'T DO SHIT
			return false;
		}
	}

	private static class FishingRod extends Item implements TrainerUseItem
	{
		private static final long serialVersionUID = 1L;

		public FishingRod()
		{
			super(Namesies.FISHING_ROD_ITEM, "A multi-purpose, do-it-all kind of fishing rod. The kind you can use wherever you want. Except on land.", BagCategory.KEY_ITEM, 4);
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

	private static class AbsorbBulb extends Item implements HoldItem, ConsumableItem, TakeDamageEffect
	{
		private static final long serialVersionUID = 1L;

		public AbsorbBulb()
		{
			super(Namesies.ABSORB_BULB_ITEM, "A consumable bulb. If the holder is hit by a Water-type move, its Sp. Atk will rise.", BagCategory.MISC, 5);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttackType() == Type.WATER && victim.getAttributes().modifyStage(victim, victim, 1, Stat.SP_ATTACK, b, CastSource.HELD_ITEM))
			{
				victim.consumeItem(b);
			}
		}
	}

	private static class AirBalloon extends Item implements HoldItem, ConsumableItem, LevitationEffect, TakeDamageEffect, EntryEffect
	{
		private static final long serialVersionUID = 1L;

		public AirBalloon()
		{
			super(Namesies.AIR_BALLOON_ITEM, "When held by a Pok\u00e9mon, the Pok\u00e9mon will float into the air. When the holder is attacked, this item will burst.", BagCategory.MISC, 6);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
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
			super(Namesies.AMULET_COIN_ITEM, "An item to be held by a Pok\u00e9mon. It doubles a battle's prize money if the holding Pok\u00e9mon joins in.", BagCategory.MISC, 7);
			super.price = 100;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public void enter(Battle b, ActivePokemon victim)
		{
			TeamEffect.getEffect(Namesies.DOUBLE_MONEY_EFFECT).cast(b, victim, victim, CastSource.HELD_ITEM, false);
		}
	}

	private static class BigRoot extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public BigRoot()
		{
			super(Namesies.BIG_ROOT_ITEM, "A Pok\u00e9mon held item that boosts the power of HP-stealing moves to let the holder recover more HP.", BagCategory.MISC, 8);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class BindingBand extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public BindingBand()
		{
			super(Namesies.BINDING_BAND_ITEM, "This item, when attached to a Pok\u00e9mon, increases damage caused by moves that constrict the opponent.", BagCategory.MISC, 9);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class BlackSludge extends Item implements HoldItem, EndTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public BlackSludge()
		{
			super(Namesies.BLACK_SLUDGE_ITEM, "A held item that gradually restores the HP of Poison-type Pok\u00e9mon. It inflicts damage on all other types.", BagCategory.MISC, 10);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public void applyEndTurn(ActivePokemon victim, Battle b)
		{
			if (victim.isType(b, Type.POISON))
			{
				// Don't heal if at full health
				if (victim.fullHealth())
				{
					return;
				}
				
				victim.healHealthFraction(1/16.0);
				b.addMessage(victim.getName() + "'s HP was restored by its " + this.name + "!", victim);
			}
			else if (!victim.hasAbility(Namesies.MAGIC_GUARD_ABILITY))
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
			super(Namesies.BRIGHT_POWDER_ITEM, "An item to be held by a Pok\u00e9mon. It casts a tricky glare that lowers the opponent's accuracy.", BagCategory.MISC, 11);
			super.price = 100;
		}

		public boolean isModifyStat(Stat s)
		{
			return s == Stat.EVASION;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (isModifyStat(s) && true)
			{
				stat *= 1.1;
			}
			
			return stat;
		}
	}

	private static class CellBattery extends Item implements HoldItem, ConsumableItem, TakeDamageEffect
	{
		private static final long serialVersionUID = 1L;

		public CellBattery()
		{
			super(Namesies.CELL_BATTERY_ITEM, "A consumable battery. If the holder is hit by an Electric-type move, its Attack will rise.", BagCategory.MISC, 12);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttackType() == Type.ELECTRIC && victim.getAttributes().modifyStage(victim, victim, 1, Stat.ATTACK, b, CastSource.HELD_ITEM))
			{
				victim.consumeItem(b);
			}
		}
	}

	private static class ChoiceBand extends Item implements AttackSelectionEffect, HoldItem, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public ChoiceBand()
		{
			super(Namesies.CHOICE_BAND_ITEM, "An item to be held by a Pok\u00e9mon. This headband ups Attack, but allows the use of only one of its moves.", BagCategory.MISC, 13);
			super.price = 100;
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean isModifyStat(Stat s)
		{
			return s == Stat.ATTACK;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (isModifyStat(s) && true)
			{
				stat *= 1.5;
			}
			
			return stat;
		}
	}

	private static class ChoiceScarf extends Item implements AttackSelectionEffect, HoldItem, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public ChoiceScarf()
		{
			super(Namesies.CHOICE_SCARF_ITEM, "An item to be held by a Pok\u00e9mon. This scarf boosts Speed, but allows the use of only one of its moves.", BagCategory.MISC, 14);
			super.price = 200;
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean isModifyStat(Stat s)
		{
			return s == Stat.SPEED;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (isModifyStat(s) && true)
			{
				stat *= 1.5;
			}
			
			return stat;
		}
	}

	private static class ChoiceSpecs extends Item implements AttackSelectionEffect, HoldItem, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public ChoiceSpecs()
		{
			super(Namesies.CHOICE_SPECS_ITEM, "An item to be held by a Pok\u00e9mon. These distinctive glasses boost Sp. Atk but allow the use of only one of its moves.", BagCategory.MISC, 15);
			super.price = 200;
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean isModifyStat(Stat s)
		{
			return s == Stat.SP_ATTACK;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (isModifyStat(s) && true)
			{
				stat *= 1.5;
			}
			
			return stat;
		}
	}

	private static class CleanseTag extends Item implements HoldItem, RepellingEffect
	{
		private static final long serialVersionUID = 1L;

		public CleanseTag()
		{
			super(Namesies.CLEANSE_TAG_ITEM, "An item to be held by a Pok\u00e9mon. It helps keep wild Pok\u00e9mon away if the holder is the first one in the party.", BagCategory.MISC, 16);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
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
			super(Namesies.DAMP_ROCK_ITEM, "A Pok\u00e9mon held item that extends the duration of the move Rain Dance used by the holder.", BagCategory.MISC, 17);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 60;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public Namesies getWeatherType()
		{
			return Namesies.RAINING_EFFECT;
		}
	}

	private static class HeatRock extends Item implements HoldItem, WeatherExtendingEffect
	{
		private static final long serialVersionUID = 1L;

		public HeatRock()
		{
			super(Namesies.HEAT_ROCK_ITEM, "A Pok\u00e9mon held item that extends the duration of the move Sunny Day used by the holder.", BagCategory.MISC, 18);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 60;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public Namesies getWeatherType()
		{
			return Namesies.SUNNY_EFFECT;
		}
	}

	private static class IcyRock extends Item implements HoldItem, WeatherExtendingEffect
	{
		private static final long serialVersionUID = 1L;

		public IcyRock()
		{
			super(Namesies.ICY_ROCK_ITEM, "A Pok\u00e9mon held item that extends the duration of the move Hail used by the holder.", BagCategory.MISC, 19);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 40;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public Namesies getWeatherType()
		{
			return Namesies.HAILING_EFFECT;
		}
	}

	private static class SmoothRock extends Item implements HoldItem, WeatherExtendingEffect
	{
		private static final long serialVersionUID = 1L;

		public SmoothRock()
		{
			super(Namesies.SMOOTH_ROCK_ITEM, "A Pok\u00e9mon held item that extends the duration of the move Sandstorm used by the holder.", BagCategory.MISC, 20);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public Namesies getWeatherType()
		{
			return Namesies.SANDSTORM_EFFECT;
		}
	}

	private static class EjectButton extends Item implements HoldItem, TakeDamageEffect
	{
		private static final long serialVersionUID = 1L;

		public EjectButton()
		{
			super(Namesies.EJECT_BUTTON_ITEM, "If the holder is hit by an attack, it will switch with another Pok\u00e9mon in your party.", BagCategory.MISC, 21);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
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
			super(Namesies.DESTINY_KNOT_ITEM, "A long, thin, bright-red string to be held by a Pok\u00e9mon. If the holder becomes infatuated, the foe does too.", BagCategory.MISC, 22);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class ExpertBelt extends Item implements HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public ExpertBelt()
		{
			super(Namesies.EXPERT_BELT_ITEM, "An item to be held by a Pok\u00e9mon. It is a well-worn belt that slightly boosts the power of supereffective moves.", BagCategory.MISC, 23);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return Type.getAdvantage(user, victim, b) > 1 ? 1.2 : 1;
		}
	}

	private static class FlameOrb extends Item implements HoldItem, EndTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public FlameOrb()
		{
			super(Namesies.FLAME_ORB_ITEM, "An item to be held by a Pok\u00e9mon. It is a bizarre orb that inflicts a burn on the holder in battle.", BagCategory.MISC, 24);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
			Status.giveStatus(b, pelted, pelted, StatusCondition.BURNED, pelted.getName() + " was burned by the " + this.name + "!");
		}

		public void applyEndTurn(ActivePokemon victim, Battle b)
		{
			Status.giveStatus(b, victim, victim, StatusCondition.BURNED, victim.getName() + " was burned by its " + this.name + "!");
		}
	}

	private static class ToxicOrb extends Item implements HoldItem, EndTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public ToxicOrb()
		{
			super(Namesies.TOXIC_ORB_ITEM, "An item to be held by a Pok\u00e9mon. It is a bizarre orb that inflicts a burn on the holder in battle.", BagCategory.MISC, 25);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
			// Badly poisons the pelted
			if (Status.applies(StatusCondition.POISONED, b, pelted, pelted))
			{
				pelted.addEffect(PokemonEffect.getEffect(Namesies.BAD_POISON_EFFECT).newInstance());
				Status.giveStatus(b, pelted, pelted, StatusCondition.POISONED, pelted.getName() + " was badly poisoned by the " + this.name + "!");
			}
		}

		public void applyEndTurn(ActivePokemon victim, Battle b)
		{
			// Badly poisons the holder at the end of the turn
			if (Status.applies(StatusCondition.POISONED, b, victim, victim))
			{
				victim.addEffect(PokemonEffect.getEffect(Namesies.BAD_POISON_EFFECT).newInstance());
				Status.giveStatus(b, victim, victim, StatusCondition.POISONED, victim.getName() + " was badly poisoned by its " + this.name + "!");
			}
		}
	}

	private static class FloatStone extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public FloatStone()
		{
			super(Namesies.FLOAT_STONE_ITEM, "This item, when attached to a Pok\u00e9mon, halves the Pok\u00e9mon's weight for use with attacks that deal with weight", BagCategory.MISC, 26);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class FocusBand extends Item implements HoldItem, BracingEffect
	{
		private static final long serialVersionUID = 1L;

		public FocusBand()
		{
			super(Namesies.FOCUS_BAND_ITEM, "An item to be held by a Pok\u00e9mon. The holder may endure a potential KO attack, leaving it with just 1 HP.", BagCategory.MISC, 27);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
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

	private static class FocusSash extends Item implements HoldItem, ConsumableItem, BracingEffect
	{
		private static final long serialVersionUID = 1L;

		public FocusSash()
		{
			super(Namesies.FOCUS_SASH_ITEM, "An item to be held by a Pok\u00e9mon. If it has full HP, the holder will endure one potential KO attack, leaving 1 HP.", BagCategory.MISC, 28);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
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
			super(Namesies.GRIP_CLAW_ITEM, "A Pok\u00e9mon held item that extends the duration of multiturn attacks like Bind and Wrap.", BagCategory.MISC, 29);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class AdamantOrb extends Item implements HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public AdamantOrb()
		{
			super(Namesies.ADAMANT_ORB_ITEM, "A brightly gleaming orb to be held by Dialga. It boosts the power of Dragon- and Steel-type moves.", BagCategory.MISC, 30);
			super.price = 10000;
		}

		public boolean canUseOrb(ActivePokemon user)
		{
			if (!user.isPokemon(Namesies.DIALGA_POKEMON))
			{
				return false;
			}
			
			return user.isAttackType(Type.DRAGON) || user.isAttackType(Type.STEEL);
		}

		public int flingDamage()
		{
			return 60;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (canUseOrb(user))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class LustrousOrb extends Item implements HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public LustrousOrb()
		{
			super(Namesies.LUSTROUS_ORB_ITEM, "A beautifully glowing orb to be held by Palkia. It boosts the power of Dragon- and Water-type moves.", BagCategory.MISC, 31);
			super.price = 10000;
		}

		public boolean canUseOrb(ActivePokemon user)
		{
			if (!user.isPokemon(Namesies.PALKIA_POKEMON))
			{
				return false;
			}
			
			return user.isAttackType(Type.DRAGON) || user.isAttackType(Type.WATER);
		}

		public int flingDamage()
		{
			return 60;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (canUseOrb(user))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class GriseousOrb extends Item implements HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public GriseousOrb()
		{
			super(Namesies.GRISEOUS_ORB_ITEM, "A glowing orb to be held by Giratina. It boosts the power of Dragon- and Ghost-type moves.", BagCategory.MISC, 32);
			super.price = 10000;
		}

		public boolean canUseOrb(ActivePokemon user)
		{
			if (!user.isPokemon(Namesies.GIRATINA_POKEMON))
			{
				return false;
			}
			
			return user.isAttackType(Type.DRAGON) || user.isAttackType(Type.GHOST);
		}

		public int flingDamage()
		{
			return 60;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (canUseOrb(user))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class IronBall extends Item implements HoldItem, GroundedEffect, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public IronBall()
		{
			super(Namesies.IRON_BALL_ITEM, "A Pok\u00e9mon held item that cuts Speed. It makes Flying-type and levitating holders susceptible to Ground moves.", BagCategory.MISC, 33);
			super.price = 200;
		}

		public boolean isModifyStat(Stat s)
		{
			return s == Stat.SPEED;
		}

		public int flingDamage()
		{
			return 130;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (isModifyStat(s) && true)
			{
				stat *= .5;
			}
			
			return stat;
		}
	}

	private static class LaggingTail extends Item implements HoldItem, StallingEffect
	{
		private static final long serialVersionUID = 1L;

		public LaggingTail()
		{
			super(Namesies.LAGGING_TAIL_ITEM, "An item to be held by a Pok\u00e9mon. It is tremendously heavy and makes the holder move slower than usual.", BagCategory.MISC, 34);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class LifeOrb extends Item implements HoldItem, PowerChangeEffect, ApplyDamageEffect
	{
		private static final long serialVersionUID = 1L;

		public LifeOrb()
		{
			super(Namesies.LIFE_ORB_ITEM, "An item to be held by a Pok\u00e9mon. It boosts the power of moves, but at the cost of some HP on each hit.", BagCategory.MISC, 35);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return 5324.0/4096.0;
		}

		public void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, Integer damage)
		{
			if (user.hasAbility(Namesies.MAGIC_GUARD_ABILITY))
			{
				return;
			}
			
			b.addMessage(user.getName() + " was hurt by its " + this.name + "!");
			user.reduceHealthFraction(b, .1);
		}
	}

	private static class LightBall extends Item implements HoldItem, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public LightBall()
		{
			super(Namesies.LIGHT_BALL_ITEM, "An item to be held by Pikachu. It is a puzzling orb that raises the Attack and Sp. Atk stat.", BagCategory.MISC, 36);
			super.price = 100;
		}

		public boolean isModifyStat(Stat s)
		{
			return s == Stat.ATTACK || s == Stat.SP_ATTACK;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
			Status.giveStatus(b, pelted, pelted, StatusCondition.PARALYZED, pelted.getName() + " was paralyzed by the " + this.name + "!");
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (isModifyStat(s) && p.isPokemon(Namesies.PIKACHU_POKEMON))
			{
				stat *= 2;
			}
			
			return stat;
		}
	}

	private static class LightClay extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public LightClay()
		{
			super(Namesies.LIGHT_CLAY_ITEM, "A Pok\u00e9mon held item that extends the duration of barrier moves like Light Screen and Reflect used by the holder.", BagCategory.MISC, 37);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class LuckyEgg extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public LuckyEgg()
		{
			super(Namesies.LUCKY_EGG_ITEM, "An item to be held by a Pok\u00e9mon. It is an egg filled with happiness that earns extra Exp. Points in battle.", BagCategory.MISC, 38);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class LuckyPunch extends Item implements HoldItem, CritStageEffect
	{
		private static final long serialVersionUID = 1L;

		public LuckyPunch()
		{
			super(Namesies.LUCKY_PUNCH_ITEM, "An item to be held by Chansey. It is a pair of gloves that boosts Chansey's critical-hit ratio.", BagCategory.MISC, 39);
			super.price = 10;
		}

		public int flingDamage()
		{
			return 40;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public int increaseCritStage(Integer stage, ActivePokemon p)
		{
			if (p.isPokemon(Namesies.CHANSEY_POKEMON))
			{
				return stage + 2;
			}
			
			return stage;
		}
	}

	private static class LuminousMoss extends Item implements HoldItem, ConsumableItem, TakeDamageEffect
	{
		private static final long serialVersionUID = 1L;

		public LuminousMoss()
		{
			super(Namesies.LUMINOUS_MOSS_ITEM, "If the holder is hit by an Water-type attack, the holder's Special Defense stat is increased one stage. The item is consumed.", BagCategory.MISC, 40);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttackType() == Type.WATER && victim.getAttributes().modifyStage(victim, victim, 1, Stat.SP_DEFENSE, b, CastSource.HELD_ITEM))
			{
				victim.consumeItem(b);
			}
		}
	}

	private static class MachoBrace extends Item implements EVItem, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public MachoBrace()
		{
			super(Namesies.MACHO_BRACE_ITEM, "An item to be held by a Pok\u00e9mon. It is a stiff and heavy brace that promotes strong growth but lowers Speed.", BagCategory.MISC, 41);
			super.price = 3000;
		}

		public boolean isModifyStat(Stat s)
		{
			return s == Stat.SPEED;
		}

		public int[] getEVs(int[] vals)
		{
			for (int i = 0; i < vals.length; i++)
			{
				vals[i] *= 2;
			}
			
			return vals;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (isModifyStat(s) && true)
			{
				stat *= .5;
			}
			
			return stat;
		}

		public int flingDamage()
		{
			return 60;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class MentalHerb extends Item implements HoldItem, EndTurnEffect
	{
		private static final long serialVersionUID = 1L;
		Namesies[] effects = {Namesies.INFATUATED_EFFECT, Namesies.DISABLE_EFFECT, Namesies.TAUNT_EFFECT, Namesies.ENCORE_EFFECT, Namesies.TORMENT_EFFECT, Namesies.CONFUSION_EFFECT, Namesies.HEAL_BLOCK_EFFECT};
		String[] messages = {"infatuated", "disabled", "under the effects of taunt", "under the effects of encore", "under the effects of torment", "confused", "under the effects of heal block"};

		public MentalHerb()
		{
			super(Namesies.MENTAL_HERB_ITEM, "An item to be held by a Pok\u00e9mon. It snaps the holder out of infatuation. It can be used only once.", BagCategory.MISC, 42);
			super.price = 100;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
			if (pelted.hasEffect(Namesies.INFATUATED_EFFECT))
			{
				pelted.getAttributes().removeEffect(Namesies.INFATUATED_EFFECT);
				b.addMessage(pelted.getName() + " is no longer infatuated to to the " + this.name + "!");
			}
		}

		public void applyEndTurn(ActivePokemon victim, Battle b)
		{
			// TODO: Has nothing to do with Mental Herb, but the item Power Herb is not implemented yet
			boolean used = false;
			for (int i = 0; i < effects.length; i++)
			{
				Namesies s = effects[i];
				if (victim.hasEffect(s))
				{
					used = true;
					victim.getAttributes().removeEffect(s);
					b.addMessage(victim.getName() + " is no longer " + messages[i] + " due to its " + this.name + "!");
				}
			}
			
			if (used)
			{
				victim.consumeItem(b);
			}
		}
	}

	private static class MetalPowder extends Item implements HoldItem, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public MetalPowder()
		{
			super(Namesies.METAL_POWDER_ITEM, "When this item is held by a Ditto, the holder's initial Defence & Special Defence stats are increased by 50%", BagCategory.MISC, 43);
			super.price = 10;
		}

		public boolean isModifyStat(Stat s)
		{
			return s == Stat.DEFENSE || s == Stat.SP_DEFENSE;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (isModifyStat(s) && p.isPokemon(Namesies.DITTO_POKEMON))
			{
				stat *= 1.5;
			}
			
			return stat;
		}
	}

	private static class Metronome extends Item implements HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public Metronome()
		{
			super(Namesies.METRONOME_ITEM, "A Pok\u00e9mon held item that boosts a move used consecutively. Its effect is reset if another move is used.", BagCategory.MISC, 44);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
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
			super(Namesies.MUSCLE_BAND_ITEM, "An item to be held by a Pok\u00e9mon. It is a headband that slightly boosts the power of physical moves.", BagCategory.MISC, 45);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getAttack().getCategory() == Category.PHYSICAL ? 1.1 : 1;
		}
	}

	private static class PowerAnklet extends Item implements PowerItem, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public PowerAnklet()
		{
			super(Namesies.POWER_ANKLET_ITEM, "A Pok\u00e9mon held item that promotes Speed gain on leveling, but reduces the Speed stat.", BagCategory.MISC, 46);
			super.price = 3000;
		}

		public Stat powerStat()
		{
			return Stat.SPEED;
		}

		public boolean isModifyStat(Stat s)
		{
			return s == Stat.SPEED;
		}

		public int[] getEVs(int[] vals)
		{
			vals[powerStat().index()] += 4;
			return vals;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (isModifyStat(s) && true)
			{
				stat *= .5;
			}
			
			return stat;
		}

		public int flingDamage()
		{
			return 70;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class PowerBand extends Item implements PowerItem, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public PowerBand()
		{
			super(Namesies.POWER_BAND_ITEM, "A Pok\u00e9mon held item that promotes Sp. Def gain on leveling, but reduces the Speed stat.", BagCategory.MISC, 47);
			super.price = 3000;
		}

		public Stat powerStat()
		{
			return Stat.SP_DEFENSE;
		}

		public boolean isModifyStat(Stat s)
		{
			return s == Stat.SPEED;
		}

		public int[] getEVs(int[] vals)
		{
			vals[powerStat().index()] += 4;
			return vals;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (isModifyStat(s) && true)
			{
				stat *= .5;
			}
			
			return stat;
		}

		public int flingDamage()
		{
			return 70;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class PowerBelt extends Item implements PowerItem, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public PowerBelt()
		{
			super(Namesies.POWER_BELT_ITEM, "A Pok\u00e9mon held item that promotes Def gain on leveling, but reduces the Speed stat.", BagCategory.MISC, 48);
			super.price = 3000;
		}

		public Stat powerStat()
		{
			return Stat.DEFENSE;
		}

		public boolean isModifyStat(Stat s)
		{
			return s == Stat.SPEED;
		}

		public int[] getEVs(int[] vals)
		{
			vals[powerStat().index()] += 4;
			return vals;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (isModifyStat(s) && true)
			{
				stat *= .5;
			}
			
			return stat;
		}

		public int flingDamage()
		{
			return 70;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class PowerBracer extends Item implements PowerItem, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public PowerBracer()
		{
			super(Namesies.POWER_BRACER_ITEM, "A Pok\u00e9mon held item that promotes Att gain on leveling, but reduces the Speed stat.", BagCategory.MISC, 49);
			super.price = 3000;
		}

		public Stat powerStat()
		{
			return Stat.ATTACK;
		}

		public boolean isModifyStat(Stat s)
		{
			return s == Stat.SPEED;
		}

		public int[] getEVs(int[] vals)
		{
			vals[powerStat().index()] += 4;
			return vals;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (isModifyStat(s) && true)
			{
				stat *= .5;
			}
			
			return stat;
		}

		public int flingDamage()
		{
			return 70;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class PowerLens extends Item implements PowerItem, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public PowerLens()
		{
			super(Namesies.POWER_LENS_ITEM, "A Pok\u00e9mon held item that promotes Sp. Att gain on leveling, but reduces the Speed stat.", BagCategory.MISC, 50);
			super.price = 3000;
		}

		public Stat powerStat()
		{
			return Stat.SP_ATTACK;
		}

		public boolean isModifyStat(Stat s)
		{
			return s == Stat.SPEED;
		}

		public int[] getEVs(int[] vals)
		{
			vals[powerStat().index()] += 4;
			return vals;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (isModifyStat(s) && true)
			{
				stat *= .5;
			}
			
			return stat;
		}

		public int flingDamage()
		{
			return 70;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class PowerWeight extends Item implements PowerItem, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public PowerWeight()
		{
			super(Namesies.POWER_WEIGHT_ITEM, "A Pok\u00e9mon held item that promotes HP gain on leveling, but reduces the Speed stat.", BagCategory.MISC, 51);
			super.price = 3000;
		}

		public Stat powerStat()
		{
			return Stat.HP;
		}

		public boolean isModifyStat(Stat s)
		{
			return s == Stat.SPEED;
		}

		public int[] getEVs(int[] vals)
		{
			vals[powerStat().index()] += 4;
			return vals;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (isModifyStat(s) && true)
			{
				stat *= .5;
			}
			
			return stat;
		}

		public int flingDamage()
		{
			return 70;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class QuickClaw extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public QuickClaw()
		{
			super(Namesies.QUICK_CLAW_ITEM, "An item to be held by a Pok\u00e9mon. A light, sharp claw that lets the bearer move first occasionally.", BagCategory.MISC, 52);
			super.price = 100;
		}

		public int flingDamage()
		{
			return 80;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class QuickPowder extends Item implements HoldItem, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public QuickPowder()
		{
			super(Namesies.QUICK_POWDER_ITEM, "An item to be held by Ditto. Extremely fine yet hard, this odd powder boosts the Speed stat.", BagCategory.MISC, 53);
			super.price = 10;
		}

		public boolean isModifyStat(Stat s)
		{
			return s == Stat.SPEED;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (isModifyStat(s) && p.isPokemon(Namesies.DITTO_POKEMON))
			{
				stat *= 1.5;
			}
			
			return stat;
		}
	}

	private static class RedCard extends Item implements HoldItem, TakeDamageEffect
	{
		private static final long serialVersionUID = 1L;

		public RedCard()
		{
			super(Namesies.RED_CARD_ITEM, "A card with a mysterious power. When the holder is struck by a foe, the attacker is removed from battle.", BagCategory.MISC, 54);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			// TODO: Generalize this code with that of moves like U-Turn
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

	private static class RingTarget extends Item implements HoldItem, AdvantageChanger
	{
		private static final long serialVersionUID = 1L;

		public RingTarget()
		{
			super(Namesies.RING_TARGET_ITEM, "Moves that would otherwise have no effect will land on the Pok\u00e9mon that holds it.", BagCategory.MISC, 55);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public Type[] getAdvantageChange(Type attacking, Type[] defending)
		{
			for (int i = 0; i < 2; i++)
			{
				if (Type.getBasicAdvantage(attacking, defending[i]) == 0)
				{
					defending[i] = Type.NONE;
				}
			}
			
			return defending;
		}
	}

	private static class RockyHelmet extends Item implements HoldItem, PhysicalContactEffect
	{
		private static final long serialVersionUID = 1L;

		public RockyHelmet()
		{
			super(Namesies.ROCKY_HELMET_ITEM, "If the holder of this item takes damage, the attacker will also be damaged upon contact.", BagCategory.MISC, 56);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 60;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			b.addMessage(user.getName() + " was hurt by " + victim.getName() + "'s " + this.name + "!");
			user.reduceHealthFraction(b, 1/8.0);
		}
	}

	private static class SafetyGoggles extends Item implements HoldItem, WeatherBlockerEffect, EffectBlockerEffect
	{
		private static final long serialVersionUID = 1L;

		public SafetyGoggles()
		{
			super(Namesies.SAFETY_GOGGLES_ITEM, "An item to be held by a Pok\u00e9mon. These goggles protect the holder from both weather-related damage and powder.", BagCategory.MISC, 57);
			super.price = 200;
		}

		public String getPreventMessage(ActivePokemon victim)
		{
			return victim.getName() + "'s " + this.getName() + " protects it from powder moves!";
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean block(Namesies weather)
		{
			return true;
		}

		public boolean validMove(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (!user.getAttack().isMoveType(MoveType.POWDER))
			{
				return true;
			}
			
			if (user.getAttack().getCategory() == Category.STATUS)
			{
				b.addMessage(getPreventMessage(victim));
			}
			
			return false;
		}
	}

	private static class ScopeLens extends Item implements HoldItem, CritStageEffect
	{
		private static final long serialVersionUID = 1L;

		public ScopeLens()
		{
			super(Namesies.SCOPE_LENS_ITEM, "An item to be held by a Pok\u00e9mon. It is a lens that boosts the holder's critical-hit ratio.", BagCategory.MISC, 58);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public int increaseCritStage(Integer stage, ActivePokemon p)
		{
			return stage + 1;
		}
	}

	private static class ShedShell extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public ShedShell()
		{
			super(Namesies.SHED_SHELL_ITEM, "A tough, discarded carapace to be held by a Pok\u00e9mon. It enables the holder to switch with a waiting Pok\u00e9mon in battle.", BagCategory.MISC, 59);
			super.price = 100;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class ShellBell extends Item implements HoldItem, ApplyDamageEffect
	{
		private static final long serialVersionUID = 1L;

		public ShellBell()
		{
			super(Namesies.SHELL_BELL_ITEM, "An item to be held by a Pok\u00e9mon. The holder's HP is restored a little every time it inflicts damage.", BagCategory.MISC, 60);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, Integer damage)
		{
			if (user.fullHealth())
			{
				return;
			}
			
			user.heal((int)Math.ceil(damage/8.0));
			// TODO: This looks really bad when paired with Explosion
			b.addMessage(user.getName() + " restored some HP due to its " + this.name + "!", user);
		}
	}

	private static class SmokeBall extends Item implements HoldItem, DefiniteEscape
	{
		private static final long serialVersionUID = 1L;

		public SmokeBall()
		{
			super(Namesies.SMOKE_BALL_ITEM, "An item to be held by a Pok\u00e9mon. It enables the holder to flee from any wild Pok\u00e9mon without fail.", BagCategory.MISC, 61);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class Snowball extends Item implements HoldItem, ConsumableItem, TakeDamageEffect
	{
		private static final long serialVersionUID = 1L;

		public Snowball()
		{
			super(Namesies.SNOWBALL_ITEM, "An item to be held by a Pok\u00e9mon. It boosts Attack if hit with an Ice-type attack. It can only be used once.", BagCategory.MISC, 62);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttackType() == Type.ICE && victim.getAttributes().modifyStage(victim, victim, 1, Stat.ATTACK, b, CastSource.HELD_ITEM))
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
			super(Namesies.SOUL_DEW_ITEM, "If the Soul Dew is attached to Latios or Latias, the holder's Special Attack and Special Defence is increased by 50%.", BagCategory.MISC, 63);
			super.price = 10;
		}

		public boolean isModifyStat(Stat s)
		{
			return s == Stat.SP_ATTACK || s == Stat.SP_DEFENSE;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (isModifyStat(s) && p.isPokemon(Namesies.LATIOS_POKEMON) || p.isPokemon(Namesies.LATIAS_POKEMON))
			{
				stat *= 1.5;
			}
			
			return stat;
		}
	}

	private static class Stick extends Item implements HoldItem, CritStageEffect
	{
		private static final long serialVersionUID = 1L;

		public Stick()
		{
			super(Namesies.STICK_ITEM, "An item to be held by Farfetch'd. It is a very long and stiff stalk of leek that boosts the critical-hit ratio.", BagCategory.MISC, 64);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 60;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public int increaseCritStage(Integer stage, ActivePokemon p)
		{
			if (p.isPokemon(Namesies.FARFETCHD_POKEMON))
			{
				return stage + 2;
			}
			
			return 0;
		}
	}

	private static class StickyBarb extends Item implements HoldItem, EndTurnEffect, PhysicalContactEffect, ItemCondition
	{
		private static final long serialVersionUID = 1L;
		private Item item;

		public StickyBarb()
		{
			super(Namesies.STICKY_BARB_ITEM, "A held item that damages the holder on every turn. It may latch on to foes and allies that touch the holder.", BagCategory.MISC, 65);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 80;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public void applyEndTurn(ActivePokemon victim, Battle b)
		{
			if (victim.hasAbility(Namesies.MAGIC_GUARD_ABILITY))
			{
				return;
			}
			
			b.addMessage(victim.getName() + " was hurt by its " + this.name + "!");
			victim.reduceHealthFraction(b, 1/8.0);
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (!user.hasAbility(Namesies.MAGIC_GUARD_ABILITY))
			{
				b.addMessage(user.getName() + " was hurt by " + victim.getName() + "'s " + this.name + "!");
				user.reduceHealthFraction(b, 1/8.0);
			}
			
			if (user.isHoldingItem(b) || user.isFainted(b))
			{
				return;
			}
			
			b.addMessage(victim.getName() + "s " + this.name + " latched onto " + user.getName() + "!");
			
			if (b.isWildBattle())
			{
				victim.removeItem();
				user.giveItem(this);
				return;
			}
			
			item = this;
			PokemonEffect.getEffect(Namesies.CHANGE_ITEM_EFFECT).cast(b, victim, user, CastSource.HELD_ITEM, false);
			
			item = Item.noneItem();
			PokemonEffect.getEffect(Namesies.CHANGE_ITEM_EFFECT).cast(b, victim, victim, CastSource.HELD_ITEM, false);
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
			super(Namesies.THICK_CLUB_ITEM, "An item to be held by Cubone or Marowak. It is a hard bone of some sort that boosts the Attack stat.", BagCategory.MISC, 66);
			super.price = 500;
		}

		public boolean isModifyStat(Stat s)
		{
			return s == Stat.ATTACK;
		}

		public int flingDamage()
		{
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (isModifyStat(s) && p.isPokemon(Namesies.CUBONE_POKEMON) || p.isPokemon(Namesies.MAROWAK_POKEMON))
			{
				stat *= 2;
			}
			
			return stat;
		}
	}

	private static class WeaknessPolicy extends Item implements HoldItem, TakeDamageEffect
	{
		private static final long serialVersionUID = 1L;

		public WeaknessPolicy()
		{
			super(Namesies.WEAKNESS_POLICY_ITEM, "An item to be held by a Pok\u00e9mon. Attack and Sp. Atk sharply increase if the holder is hit with a move it's weak to.", BagCategory.MISC, 67);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (Type.getAdvantage(user, victim, b) > 1)
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
			super(Namesies.WHITE_HERB_ITEM, "An item to be held by a Pok\u00e9mon. It restores any lowered stat in battle. It can be used only once.", BagCategory.MISC, 68);
			super.price = 100;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
			// Restores negative stat changes to the pelted
			for (int i = 0; i < Stat.NUM_BATTLE_STATS; i++)
			{
				if (pelted.getStage(i) < 0)
				{
					pelted.getAttributes().setStage(i, 0);
				}
			}
			
			b.addMessage("The " + this.name + " restored " + pelted.getName() + "'s negative stat changes!");
		}

		public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat)
		{
			// NOTE: Works like Clear Body, since ain't nobody want to keep track of stats.
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
			super(Namesies.WIDE_LENS_ITEM, "An item to be held by a Pok\u00e9mon. It is a magnifying lens that slightly boosts the accuracy of moves.", BagCategory.MISC, 69);
			super.price = 200;
		}

		public boolean isModifyStat(Stat s)
		{
			return s == Stat.ACCURACY;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (isModifyStat(s) && true)
			{
				stat *= 1.1;
			}
			
			return stat;
		}
	}

	private static class WiseGlasses extends Item implements HoldItem, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public WiseGlasses()
		{
			super(Namesies.WISE_GLASSES_ITEM, "An item to be held by a Pok\u00e9mon. It is a thick pair of glasses that slightly boosts the power of special moves.", BagCategory.MISC, 70);
			super.price = 200;
		}

		public boolean isModifyStat(Stat s)
		{
			return s == Stat.SP_ATTACK;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (isModifyStat(s) && true)
			{
				stat *= 1.1;
			}
			
			return stat;
		}
	}

	private static class ZoomLens extends Item implements HoldItem, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public ZoomLens()
		{
			super(Namesies.ZOOM_LENS_ITEM, "An item to be held by a Pok\u00e9mon. If the holder moves after its target, its accuracy will be boosted.", BagCategory.MISC, 71);
			super.price = 200;
		}

		public boolean isModifyStat(Stat s)
		{
			return s == Stat.ACCURACY;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (isModifyStat(s) && !b.isFirstAttack())
			{
				stat *= 1.2;
			}
			
			return stat;
		}
	}

	private static class FullIncense extends Item implements HoldItem, StallingEffect
	{
		private static final long serialVersionUID = 1L;

		public FullIncense()
		{
			super(Namesies.FULL_INCENSE_ITEM, "An item to be held by a Pok\u00e9mon. It is an exotic-smelling incense that makes the holder bloated and slow moving.", BagCategory.MISC, 72);
			super.price = 9600;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class LaxIncense extends Item implements HoldItem, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public LaxIncense()
		{
			super(Namesies.LAX_INCENSE_ITEM, "An item to be held by a Pok\u00e9mon. The tricky aroma of this incense may make attacks miss the holder.", BagCategory.MISC, 73);
			super.price = 9600;
		}

		public boolean isModifyStat(Stat s)
		{
			return s == Stat.EVASION;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (isModifyStat(s) && true)
			{
				stat *= 1.1;
			}
			
			return stat;
		}
	}

	private static class LuckIncense extends Item implements HoldItem, EntryEffect
	{
		private static final long serialVersionUID = 1L;

		public LuckIncense()
		{
			super(Namesies.LUCK_INCENSE_ITEM, "An item to be held by a Pok\u00e9mon. It doubles a battle's prize money if the holding Pok\u00e9mon joins in.", BagCategory.MISC, 74);
			super.price = 9600;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public void enter(Battle b, ActivePokemon victim)
		{
			TeamEffect.getEffect(Namesies.DOUBLE_MONEY_EFFECT).cast(b, victim, victim, CastSource.HELD_ITEM, false);
		}
	}

	private static class OddIncense extends Item implements HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public OddIncense()
		{
			super(Namesies.ODD_INCENSE_ITEM, "An item to be held by a Pok\u00e9mon. It is an exotic-smelling incense that boosts the power of Psychic-type moves.", BagCategory.MISC, 75);
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

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return getMultiplier();
			}
			
			return 1;
		}
	}

	private static class PureIncense extends Item implements HoldItem, RepellingEffect
	{
		private static final long serialVersionUID = 1L;

		public PureIncense()
		{
			super(Namesies.PURE_INCENSE_ITEM, "An item to be held by a Pok\u00e9mon. It helps keep wild Pok\u00e9mon away if the holder is the first one in the party.", BagCategory.MISC, 76);
			super.price = 9600;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double chance()
		{
			return .33;
		}
	}

	private static class RockIncense extends Item implements HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public RockIncense()
		{
			super(Namesies.ROCK_INCENSE_ITEM, "An item to be held by a Pok\u00e9mon. It is an exotic-smelling incense that boosts the power of Rock-type moves.", BagCategory.MISC, 77);
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

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return getMultiplier();
			}
			
			return 1;
		}
	}

	private static class RoseIncense extends Item implements HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public RoseIncense()
		{
			super(Namesies.ROSE_INCENSE_ITEM, "An item to be held by a Pok\u00e9mon. It is an exotic-smelling incense that boosts the power of Grass-type moves.", BagCategory.MISC, 78);
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

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return getMultiplier();
			}
			
			return 1;
		}
	}

	private static class SeaIncense extends Item implements HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public SeaIncense()
		{
			super(Namesies.SEA_INCENSE_ITEM, "An item to be held by a Pok\u00e9mon. It is incense with a curious aroma that boosts the power of Water-type moves.", BagCategory.MISC, 79);
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

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return getMultiplier();
			}
			
			return 1;
		}
	}

	private static class WaveIncense extends Item implements HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public WaveIncense()
		{
			super(Namesies.WAVE_INCENSE_ITEM, "An item to be held by a Pok\u00e9mon. It is incense with a curious aroma that boosts the power of Water-type moves.", BagCategory.MISC, 80);
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

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return getMultiplier();
			}
			
			return 1;
		}
	}

	private static class DracoPlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public DracoPlate()
		{
			super(Namesies.DRACO_PLATE_ITEM, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Dragon-type moves.", BagCategory.MISC, 81);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.DRAGON;
		}

		public int flingDamage()
		{
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class DreadPlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public DreadPlate()
		{
			super(Namesies.DREAD_PLATE_ITEM, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Dark-type moves.", BagCategory.MISC, 82);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.DARK;
		}

		public int flingDamage()
		{
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class EarthPlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public EarthPlate()
		{
			super(Namesies.EARTH_PLATE_ITEM, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Ground-type moves.", BagCategory.MISC, 83);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.GROUND;
		}

		public int flingDamage()
		{
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class FistPlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public FistPlate()
		{
			super(Namesies.FIST_PLATE_ITEM, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Fighting-type moves.", BagCategory.MISC, 84);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.FIGHTING;
		}

		public int flingDamage()
		{
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class FlamePlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public FlamePlate()
		{
			super(Namesies.FLAME_PLATE_ITEM, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Fire-type moves.", BagCategory.MISC, 85);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.FIRE;
		}

		public int flingDamage()
		{
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class IciclePlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public IciclePlate()
		{
			super(Namesies.ICICLE_PLATE_ITEM, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Ice-type moves.", BagCategory.MISC, 86);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.ICE;
		}

		public int flingDamage()
		{
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class InsectPlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public InsectPlate()
		{
			super(Namesies.INSECT_PLATE_ITEM, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Bug-type moves.", BagCategory.MISC, 87);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.BUG;
		}

		public int flingDamage()
		{
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class IronPlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public IronPlate()
		{
			super(Namesies.IRON_PLATE_ITEM, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Steel-type moves.", BagCategory.MISC, 88);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.STEEL;
		}

		public int flingDamage()
		{
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class MeadowPlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public MeadowPlate()
		{
			super(Namesies.MEADOW_PLATE_ITEM, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Grass-type moves.", BagCategory.MISC, 89);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.GRASS;
		}

		public int flingDamage()
		{
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class MindPlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public MindPlate()
		{
			super(Namesies.MIND_PLATE_ITEM, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Psychic-type moves.", BagCategory.MISC, 90);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.PSYCHIC;
		}

		public int flingDamage()
		{
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class PixiePlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public PixiePlate()
		{
			super(Namesies.PIXIE_PLATE_ITEM, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Fairy-type moves.", BagCategory.MISC, 91);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.FAIRY;
		}

		public int flingDamage()
		{
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class SkyPlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public SkyPlate()
		{
			super(Namesies.SKY_PLATE_ITEM, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Flying-type moves.", BagCategory.MISC, 92);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.FLYING;
		}

		public int flingDamage()
		{
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class SplashPlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public SplashPlate()
		{
			super(Namesies.SPLASH_PLATE_ITEM, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Water-type moves.", BagCategory.MISC, 93);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.WATER;
		}

		public int flingDamage()
		{
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class SpookyPlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public SpookyPlate()
		{
			super(Namesies.SPOOKY_PLATE_ITEM, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Ghost-type moves.", BagCategory.MISC, 94);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.GHOST;
		}

		public int flingDamage()
		{
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class StonePlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public StonePlate()
		{
			super(Namesies.STONE_PLATE_ITEM, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Rock-type moves.", BagCategory.MISC, 95);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.ROCK;
		}

		public int flingDamage()
		{
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class ToxicPlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public ToxicPlate()
		{
			super(Namesies.TOXIC_PLATE_ITEM, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Poison-type moves.", BagCategory.MISC, 96);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.POISON;
		}

		public int flingDamage()
		{
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class ZapPlate extends Item implements PlateItem
	{
		private static final long serialVersionUID = 1L;

		public ZapPlate()
		{
			super(Namesies.ZAP_PLATE_ITEM, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Electric-type moves.", BagCategory.MISC, 97);
			super.price = 1000;
		}

		public Type getType()
		{
			return Type.ELECTRIC;
		}

		public int flingDamage()
		{
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class BurnDrive extends Item implements DriveItem
	{
		private static final long serialVersionUID = 1L;

		public BurnDrive()
		{
			super(Namesies.BURN_DRIVE_ITEM, "A cassette to be held by Genesect. It changes Techno Blast to a Fire-type move.", BagCategory.MISC, 98);
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class ChillDrive extends Item implements DriveItem
	{
		private static final long serialVersionUID = 1L;

		public ChillDrive()
		{
			super(Namesies.CHILL_DRIVE_ITEM, "A cassette to be held by Genesect. It changes Techno Blast to an Ice-type move.", BagCategory.MISC, 99);
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class DouseDrive extends Item implements DriveItem
	{
		private static final long serialVersionUID = 1L;

		public DouseDrive()
		{
			super(Namesies.DOUSE_DRIVE_ITEM, "A cassette to be held by Genesect. It changes Techno Blast to a Water-type move.", BagCategory.MISC, 100);
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class ShockDrive extends Item implements DriveItem
	{
		private static final long serialVersionUID = 1L;

		public ShockDrive()
		{
			super(Namesies.SHOCK_DRIVE_ITEM, "A cassette to be held by Genesect. It changes Techno Blast to an Electric-type move.", BagCategory.MISC, 101);
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class FireGem extends Item implements GemItem
	{
		private static final long serialVersionUID = 1L;

		public FireGem()
		{
			super(Namesies.FIRE_GEM_ITEM, "A gem with an essence of fire. When held, it strengthens the power of a Fire-type move only once.", BagCategory.MISC, 102);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.FIRE;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class WaterGem extends Item implements GemItem
	{
		private static final long serialVersionUID = 1L;

		public WaterGem()
		{
			super(Namesies.WATER_GEM_ITEM, "A gem with an essence of water. When held, it strengthens the power of a Water-type move only once.", BagCategory.MISC, 103);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.WATER;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class ElectricGem extends Item implements GemItem
	{
		private static final long serialVersionUID = 1L;

		public ElectricGem()
		{
			super(Namesies.ELECTRIC_GEM_ITEM, "A gem with an essence of electricity. When held, it strengthens the power of an Electric-type move only once.", BagCategory.MISC, 104);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.ELECTRIC;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class GrassGem extends Item implements GemItem
	{
		private static final long serialVersionUID = 1L;

		public GrassGem()
		{
			super(Namesies.GRASS_GEM_ITEM, "A gem with an essence of nature. When held, it strengthens the power of a Grass-type move only once.", BagCategory.MISC, 105);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.GRASS;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class IceGem extends Item implements GemItem
	{
		private static final long serialVersionUID = 1L;

		public IceGem()
		{
			super(Namesies.ICE_GEM_ITEM, "A gem with an essence of ice. When held, it strengthens the power of an Ice-type move only once.", BagCategory.MISC, 106);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.ICE;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class FightingGem extends Item implements GemItem
	{
		private static final long serialVersionUID = 1L;

		public FightingGem()
		{
			super(Namesies.FIGHTING_GEM_ITEM, "A gem with an essence of combat. When held, it strengthens the power of a Fighting-type move only once.", BagCategory.MISC, 107);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.FIGHTING;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class PoisonGem extends Item implements GemItem
	{
		private static final long serialVersionUID = 1L;

		public PoisonGem()
		{
			super(Namesies.POISON_GEM_ITEM, "A gem with an essence of poison. When held, it strengthens the power of a Poison-type move only once.", BagCategory.MISC, 108);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.POISON;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class GroundGem extends Item implements GemItem
	{
		private static final long serialVersionUID = 1L;

		public GroundGem()
		{
			super(Namesies.GROUND_GEM_ITEM, "A gem with an essence of land. When held, it strengthens the power of a Ground-type move only once.", BagCategory.MISC, 109);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.GROUND;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class FlyingGem extends Item implements GemItem
	{
		private static final long serialVersionUID = 1L;

		public FlyingGem()
		{
			super(Namesies.FLYING_GEM_ITEM, "A gem with an essence of air. When held, it strengthens the power of a Flying-type move only once.", BagCategory.MISC, 110);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.FLYING;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class PsychicGem extends Item implements GemItem
	{
		private static final long serialVersionUID = 1L;

		public PsychicGem()
		{
			super(Namesies.PSYCHIC_GEM_ITEM, "A gem with an essence of the mind. When held, it strengthens the power of a Psychic-type move only once.", BagCategory.MISC, 111);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.PSYCHIC;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class BugGem extends Item implements GemItem
	{
		private static final long serialVersionUID = 1L;

		public BugGem()
		{
			super(Namesies.BUG_GEM_ITEM, "A gem with an insect-like essence. When held, it strengthens the power of a Bug-type move only once.", BagCategory.MISC, 112);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.BUG;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class RockGem extends Item implements GemItem
	{
		private static final long serialVersionUID = 1L;

		public RockGem()
		{
			super(Namesies.ROCK_GEM_ITEM, "A gem with an essence of rock. When held, it strengthens the power of a Rock-type move only once.", BagCategory.MISC, 113);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.ROCK;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class GhostGem extends Item implements GemItem
	{
		private static final long serialVersionUID = 1L;

		public GhostGem()
		{
			super(Namesies.GHOST_GEM_ITEM, "A gem with a spectral essence. When held, it strengthens the power of a Ghost-type move only once.", BagCategory.MISC, 114);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.GHOST;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class DragonGem extends Item implements GemItem
	{
		private static final long serialVersionUID = 1L;

		public DragonGem()
		{
			super(Namesies.DRAGON_GEM_ITEM, "A gem with a draconic essence. When held, it strengthens the power of a Dragon-type move only once.", BagCategory.MISC, 115);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.DRAGON;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class DarkGem extends Item implements GemItem
	{
		private static final long serialVersionUID = 1L;

		public DarkGem()
		{
			super(Namesies.DARK_GEM_ITEM, "A gem with an essence of darkness. When held, it strengthens the power of a Dark-type move only once.", BagCategory.MISC, 116);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.DARK;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class SteelGem extends Item implements GemItem
	{
		private static final long serialVersionUID = 1L;

		public SteelGem()
		{
			super(Namesies.STEEL_GEM_ITEM, "A gem with an essence of steel. When held, it strengthens the power of a Steel-type move only once.", BagCategory.MISC, 117);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.STEEL;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class NormalGem extends Item implements GemItem
	{
		private static final long serialVersionUID = 1L;

		public NormalGem()
		{
			super(Namesies.NORMAL_GEM_ITEM, "A gem with an ordinary essence. When held, it strengthens the power of a Normal-type move only once.", BagCategory.MISC, 118);
			super.price = 100;
		}

		public Type getType()
		{
			return Type.NORMAL;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class Leftovers extends Item implements HoldItem, EndTurnEffect
	{
		private static final long serialVersionUID = 1L;

		public Leftovers()
		{
			super(Namesies.LEFTOVERS_ITEM, "An item to be held by a Pok\u00e9mon. The holder's HP is gradually restored during battle.", BagCategory.MISC, 119);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public void applyEndTurn(ActivePokemon victim, Battle b)
		{
			if (victim.fullHealth() || victim.hasEffect(Namesies.HEAL_BLOCK_EFFECT))
			{
				return;
			}
			
			victim.healHealthFraction(1/16.0);
			b.addMessage(victim.getName() + "'s HP was restored by its " + this.name + "!", victim);
		}
	}

	private static class BlackBelt extends Item implements HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public BlackBelt()
		{
			super(Namesies.BLACK_BELT_ITEM, "An item to be held by a Pok\u00e9mon. It is a belt that boosts determination and Fighting-type moves.", BagCategory.MISC, 120);
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

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return getMultiplier();
			}
			
			return 1;
		}
	}

	private static class BlackGlasses extends Item implements HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public BlackGlasses()
		{
			super(Namesies.BLACK_GLASSES_ITEM, "An item to be held by a Pok\u00e9mon. It is a shady-looking pair of glasses that boosts Dark-type moves.", BagCategory.MISC, 121);
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

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return getMultiplier();
			}
			
			return 1;
		}
	}

	private static class Charcoal extends Item implements HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public Charcoal()
		{
			super(Namesies.CHARCOAL_ITEM, "An item to be held by a Pok\u00e9mon. It is a combustible fuel that boosts the power of Fire-type moves.", BagCategory.MISC, 122);
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

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return getMultiplier();
			}
			
			return 1;
		}
	}

	private static class DragonFang extends Item implements HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public DragonFang()
		{
			super(Namesies.DRAGON_FANG_ITEM, "An item to be held by a Pok\u00e9mon. It is a hard and sharp fang that ups the power of Dragon-type moves.", BagCategory.MISC, 123);
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

		public int flingDamage()
		{
			return 70;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return getMultiplier();
			}
			
			return 1;
		}
	}

	private static class HardStone extends Item implements HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public HardStone()
		{
			super(Namesies.HARD_STONE_ITEM, "An item to be held by a Pok\u00e9mon. It is an unbreakable stone that ups the power of Rock-type moves.", BagCategory.MISC, 124);
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

		public int flingDamage()
		{
			return 100;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return getMultiplier();
			}
			
			return 1;
		}
	}

	private static class Magnet extends Item implements HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public Magnet()
		{
			super(Namesies.MAGNET_ITEM, "An item to be held by a Pok\u00e9mon. It is a powerful magnet that boosts the power of Electric-type moves.", BagCategory.MISC, 125);
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

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return getMultiplier();
			}
			
			return 1;
		}
	}

	private static class MetalCoat extends Item implements HoldItem, PowerChangeEffect, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public MetalCoat()
		{
			super(Namesies.METAL_COAT_ITEM, "A mysterious substance full of a special filmy metal. It allows certain kinds of Pok\u00e9mon to evolve.", BagCategory.MISC, 126);
			super.price = 9800;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.isAttackType(Type.STEEL) ? 1.2 : 1;
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null)
			{
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class MiracleSeed extends Item implements HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public MiracleSeed()
		{
			super(Namesies.MIRACLE_SEED_ITEM, "An item to be held by a Pok\u00e9mon. It is a seed imbued with life that ups the power of Grass-type moves.", BagCategory.MISC, 127);
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

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return getMultiplier();
			}
			
			return 1;
		}
	}

	private static class MysticWater extends Item implements HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public MysticWater()
		{
			super(Namesies.MYSTIC_WATER_ITEM, "An item to be held by a Pok\u00e9mon. It is a teardrop-shaped gem that ups the power of Water-type moves.", BagCategory.MISC, 128);
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

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return getMultiplier();
			}
			
			return 1;
		}
	}

	private static class NeverMeltIce extends Item implements HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public NeverMeltIce()
		{
			super(Namesies.NEVER_MELT_ICE_ITEM, "An item to be held by a Pok\u00e9mon. It is a piece of ice that repels heat and boosts Ice-type moves.", BagCategory.MISC, 129);
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

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return getMultiplier();
			}
			
			return 1;
		}
	}

	private static class PoisonBarb extends Item implements HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public PoisonBarb()
		{
			super(Namesies.POISON_BARB_ITEM, "An item to be held by a Pok\u00e9mon. It is a small, poisonous barb that ups the power of Poison-type moves.", BagCategory.MISC, 130);
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

		public int flingDamage()
		{
			return 70;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
			// Poisons the pelted
			Status.giveStatus(b, pelted, pelted, StatusCondition.POISONED, pelted.getName() + " was poisoned by the " + this.name + "!");
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return getMultiplier();
			}
			
			return 1;
		}
	}

	private static class SharpBeak extends Item implements HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public SharpBeak()
		{
			super(Namesies.SHARP_BEAK_ITEM, "An item to be held by a Pok\u00e9mon. It is a long, sharp beak that boosts the power of Flying-type moves.", BagCategory.MISC, 131);
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

		public int flingDamage()
		{
			return 50;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return getMultiplier();
			}
			
			return 1;
		}
	}

	private static class SilkScarf extends Item implements HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public SilkScarf()
		{
			super(Namesies.SILK_SCARF_ITEM, "An item to be held by a Pok\u00e9mon. It is a sumptuous scarf that boosts the power of Normal-type moves.", BagCategory.MISC, 132);
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

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return getMultiplier();
			}
			
			return 1;
		}
	}

	private static class SilverPowder extends Item implements HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public SilverPowder()
		{
			super(Namesies.SILVER_POWDER_ITEM, "An item to be held by a Pok\u00e9mon. It is a shiny, silver powder that ups the power of Bug-type moves.", BagCategory.MISC, 133);
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

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return getMultiplier();
			}
			
			return 1;
		}
	}

	private static class SoftSand extends Item implements HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public SoftSand()
		{
			super(Namesies.SOFT_SAND_ITEM, "An item to be held by a Pok\u00e9mon. It is a loose, silky sand that boosts the power of Ground-type moves.", BagCategory.MISC, 134);
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

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return getMultiplier();
			}
			
			return 1;
		}
	}

	private static class SpellTag extends Item implements HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public SpellTag()
		{
			super(Namesies.SPELL_TAG_ITEM, "An item to be held by a Pok\u00e9mon. It is a sinister, eerie tag that boosts the power of Ghost-type moves.", BagCategory.MISC, 135);
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

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return getMultiplier();
			}
			
			return 1;
		}
	}

	private static class TwistedSpoon extends Item implements HoldItem, PowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public TwistedSpoon()
		{
			super(Namesies.TWISTED_SPOON_ITEM, "An item to be held by a Pok\u00e9mon. It is a spoon imbued with telekinetic power that boosts Psychic-type moves.", BagCategory.MISC, 136);
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

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isAttackType(getType()))
			{
				return getMultiplier();
			}
			
			return 1;
		}
	}

	private static class DawnStone extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public DawnStone()
		{
			super(Namesies.DAWN_STONE_ITEM, "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It sparkles like eyes.", BagCategory.MISC, 137);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public int flingDamage()
		{
			return 80;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null)
			{
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class DeepSeaScale extends Item implements HoldItem, StatChangingEffect, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public DeepSeaScale()
		{
			super(Namesies.DEEP_SEA_SCALE_ITEM, "An item to be held by Clamperl, Chinchou, or Lanturn. A scale that shines a faint pink, it raises the Sp. Def stat.", BagCategory.MISC, 138);
			super.price = 200;
		}

		public boolean isModifyStat(Stat s)
		{
			return s == Stat.SP_DEFENSE;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (isModifyStat(s) && p.isPokemon(Namesies.CLAMPERL_POKEMON) || p.isPokemon(Namesies.CHINCHOU_POKEMON) || p.isPokemon(Namesies.LANTURN_POKEMON))
			{
				stat *= 2;
			}
			
			return stat;
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null)
			{
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class DeepSeaTooth extends Item implements HoldItem, StatChangingEffect, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public DeepSeaTooth()
		{
			super(Namesies.DEEP_SEA_TOOTH_ITEM, "An item to be held by Clamperl. A fang that gleams a sharp silver, it raises the Sp. Atk stat.", BagCategory.MISC, 139);
			super.price = 200;
		}

		public boolean isModifyStat(Stat s)
		{
			return s == Stat.SP_ATTACK;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public int flingDamage()
		{
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (isModifyStat(s) && p.isPokemon(Namesies.CLAMPERL_POKEMON))
			{
				stat *= 2;
			}
			
			return stat;
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null)
			{
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class DragonScale extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public DragonScale()
		{
			super(Namesies.DRAGON_SCALE_ITEM, "A thick and tough scale. Dragon-type Pok\u00e9mon may be holding this item when caught.", BagCategory.MISC, 140);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null)
			{
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class DubiousDisc extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public DubiousDisc()
		{
			super(Namesies.DUBIOUS_DISC_ITEM, "A transparent device overflowing with dubious data. Its producer is unknown.", BagCategory.MISC, 141);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public int flingDamage()
		{
			return 50;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null)
			{
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class DuskStone extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public DuskStone()
		{
			super(Namesies.DUSK_STONE_ITEM, "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It is as dark as dark can be.", BagCategory.MISC, 142);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public int flingDamage()
		{
			return 80;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null)
			{
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class Electirizer extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public Electirizer()
		{
			super(Namesies.ELECTIRIZER_ITEM, "A box packed with a tremendous amount of electric energy. It is loved by a certain Pok\u00e9mon.", BagCategory.MISC, 143);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public int flingDamage()
		{
			return 80;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null)
			{
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class FireStone extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public FireStone()
		{
			super(Namesies.FIRE_STONE_ITEM, "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It is colored orange.", BagCategory.MISC, 144);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null)
			{
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class KingsRock extends Item implements PokemonUseItem, ApplyDamageEffect, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public KingsRock()
		{
			super(Namesies.KINGS_ROCK_ITEM, "An item to be held by a Pok\u00e9mon. When the holder inflicts damage, the target may flinch.", BagCategory.MISC, 145);
			super.price = 100;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null)
			{
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}

		public void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, Integer damage)
		{
			if (Math.random()*100 < 10)
			{
				PokemonEffect flinch = PokemonEffect.getEffect(Namesies.FLINCH_EFFECT);
				if (flinch.applies(b, user, victim, CastSource.HELD_ITEM))
				{
					flinch.cast(b, user, victim, CastSource.HELD_ITEM, false);
					b.addMessage(user.getName() + "'s " + this.name + " caused " + victim.getName() + " to flinch!");
				}
			}
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
			PokemonEffect flinch = PokemonEffect.getEffect(Namesies.FLINCH_EFFECT);
			if (flinch.applies(b, pelted, pelted, CastSource.USE_ITEM))
			{
				flinch.cast(b, pelted, pelted, CastSource.USE_ITEM, false);
				b.addMessage("The " + this.name + " caused " + pelted.getName() + " to flinch!");
			}
		}
	}

	private static class LeafStone extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public LeafStone()
		{
			super(Namesies.LEAF_STONE_ITEM, "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It has a leaf pattern.", BagCategory.MISC, 146);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null)
			{
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class Magmarizer extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public Magmarizer()
		{
			super(Namesies.MAGMARIZER_ITEM, "A box packed with a tremendous amount of magma energy. It is loved by a certain Pok\u00e9mon.", BagCategory.MISC, 147);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public int flingDamage()
		{
			return 80;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null)
			{
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class MoonStone extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public MoonStone()
		{
			super(Namesies.MOON_STONE_ITEM, "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It is as black as the night sky.", BagCategory.MISC, 148);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null)
			{
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class OvalStone extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public OvalStone()
		{
			super(Namesies.OVAL_STONE_ITEM, "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It is shaped like an egg.", BagCategory.MISC, 149);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public int flingDamage()
		{
			return 80;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null)
			{
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class Everstone extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public Everstone()
		{
			super(Namesies.EVERSTONE_ITEM, "An item to be held by a Pok\u00e9mon. The Pok\u00e9mon holding this peculiar stone is prevented from evolving.", BagCategory.MISC, 150);
			super.price = 200;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class PrismScale extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public PrismScale()
		{
			super(Namesies.PRISM_SCALE_ITEM, "A mysterious scale that evolves certain Pok\u00e9mon. It shines in rainbow colors.", BagCategory.MISC, 151);
			super.price = 500;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null)
			{
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class Protector extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public Protector()
		{
			super(Namesies.PROTECTOR_ITEM, "A protective item of some sort. It is extremely stiff and heavy. It is loved by a certain Pok\u00e9mon.", BagCategory.MISC, 152);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public int flingDamage()
		{
			return 80;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null)
			{
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class RazorClaw extends Item implements HoldItem, CritStageEffect, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public RazorClaw()
		{
			super(Namesies.RAZOR_CLAW_ITEM, "An item to be held by a Pok\u00e9mon. It is a sharply hooked claw that ups the holder's critical-hit ratio.", BagCategory.MISC, 153);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public int flingDamage()
		{
			return 80;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public int increaseCritStage(Integer stage, ActivePokemon p)
		{
			return stage + 1;
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null)
			{
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class RazorFang extends Item implements PokemonUseItem, ApplyDamageEffect, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public RazorFang()
		{
			super(Namesies.RAZOR_FANG_ITEM, "An item to be held by a Pok\u00e9mon. It may make foes and allies flinch when the holder inflicts damage.`", BagCategory.MISC, 154);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null)
			{
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}

		public void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, Integer damage)
		{
			if (Math.random()*100 < 10)
			{
				PokemonEffect flinch = PokemonEffect.getEffect(Namesies.FLINCH_EFFECT);
				if (flinch.applies(b, user, victim, CastSource.HELD_ITEM))
				{
					flinch.cast(b, user, victim, CastSource.HELD_ITEM, false);
					b.addMessage(user.getName() + "'s " + this.name + " caused " + victim.getName() + " to flinch!");
				}
			}
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
			PokemonEffect flinch = PokemonEffect.getEffect(Namesies.FLINCH_EFFECT);
			if (flinch.applies(b, pelted, pelted, CastSource.USE_ITEM))
			{
				flinch.cast(b, pelted, pelted, CastSource.USE_ITEM, false);
				b.addMessage("The " + this.name + " caused " + pelted.getName() + " to flinch!");
			}
		}
	}

	private static class ReaperCloth extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public ReaperCloth()
		{
			super(Namesies.REAPER_CLOTH_ITEM, "A cloth imbued with horrifyingly strong spiritual energy. It is loved by a certain Pok\u00e9mon.", BagCategory.MISC, 155);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null)
			{
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class Sachet extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public Sachet()
		{
			super(Namesies.SACHET_ITEM, "A sachet filled with fragrant perfumes that are just slightly too overwhelming. Yet it's loved by a certain Pok\u00e9mon.", BagCategory.MISC, 156);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null)
			{
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class ShinyStone extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public ShinyStone()
		{
			super(Namesies.SHINY_STONE_ITEM, "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It shines with a dazzling light.", BagCategory.MISC, 157);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public int flingDamage()
		{
			return 80;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null)
			{
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class SunStone extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public SunStone()
		{
			super(Namesies.SUN_STONE_ITEM, "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It is as red as the sun.", BagCategory.MISC, 158);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null)
			{
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class ThunderStone extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public ThunderStone()
		{
			super(Namesies.THUNDER_STONE_ITEM, "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It has a thunderbolt pattern.", BagCategory.MISC, 159);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null)
			{
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class UpGrade extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public UpGrade()
		{
			super(Namesies.UP_GRADE_ITEM, "A transparent device filled with all sorts of data. It was produced by Silph Co.", BagCategory.MISC, 160);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null)
			{
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class WaterStone extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public WaterStone()
		{
			super(Namesies.WATER_STONE_ITEM, "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It is a clear, light blue.", BagCategory.MISC, 161);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null)
			{
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class WhippedDream extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public WhippedDream()
		{
			super(Namesies.WHIPPED_DREAM_ITEM, "A soft and sweet treat made of fluffy, puffy, whipped and whirled cream. It is loved by a certain Pok\u00e9mon.", BagCategory.MISC, 162);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null)
			{
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class Antidote extends Item implements HoldItem, PokemonUseItem, BattleUseItem
	{
		private static final long serialVersionUID = 1L;

		public Antidote()
		{
			super(Namesies.ANTIDOTE_ITEM, "A spray-type medicine. It lifts the effect of poison from one Pok\u00e9mon.", BagCategory.MEDICINE, 163);
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

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
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
			return use(b.getPlayer(), p);
		}
	}

	private static class Awakening extends Item implements HoldItem, PokemonUseItem, BattleUseItem
	{
		private static final long serialVersionUID = 1L;

		public Awakening()
		{
			super(Namesies.AWAKENING_ITEM, "A spray-type medicine. It awakens a Pok\u00e9mon from the clutches of sleep.", BagCategory.MEDICINE, 164);
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

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
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
			return use(b.getPlayer(), p);
		}
	}

	private static class BurnHeal extends Item implements HoldItem, PokemonUseItem, BattleUseItem
	{
		private static final long serialVersionUID = 1L;

		public BurnHeal()
		{
			super(Namesies.BURN_HEAL_ITEM, "A spray-type medicine. It heals a single Pok\u00e9mon that is suffering from a burn.", BagCategory.MEDICINE, 165);
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

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
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
			return use(b.getPlayer(), p);
		}
	}

	private static class IceHeal extends Item implements HoldItem, PokemonUseItem, BattleUseItem
	{
		private static final long serialVersionUID = 1L;

		public IceHeal()
		{
			super(Namesies.ICE_HEAL_ITEM, "A spray-type medicine. It defrosts a Pok\u00e9mon that has been frozen solid.", BagCategory.MEDICINE, 166);
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

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
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
			return use(b.getPlayer(), p);
		}
	}

	private static class ParalyzeHeal extends Item implements HoldItem, PokemonUseItem, BattleUseItem
	{
		private static final long serialVersionUID = 1L;

		public ParalyzeHeal()
		{
			super(Namesies.PARALYZE_HEAL_ITEM, "A spray-type medicine. It eliminates paralysis from a single Pok\u00e9mon.", BagCategory.MEDICINE, 167);
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

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
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
			return use(b.getPlayer(), p);
		}
	}

	private static class FullHeal extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public FullHeal()
		{
			super(Namesies.FULL_HEAL_ITEM, "A spray-type medicine. It heals all the status problems of a single Pok\u00e9mon.", BagCategory.MEDICINE, 168);
			super.price = 250;
			super.bcat.add(BattleBagCategory.STATUS);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " was cured of its status condition!";
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			if (!p.hasStatus() || p.hasStatus(StatusCondition.FAINTED)) return false;
			
			p.removeStatus();
			return true;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(b.getPlayer(), p);
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class FullRestore extends Item implements PokemonUseItem, HoldItem, BattleUseItem
	{
		private static final long serialVersionUID = 1L;

		public FullRestore()
		{
			super(Namesies.FULL_RESTORE_ITEM, "A medicine that fully restores the HP and heals any status problems of a single Pok\u00e9mon.", BagCategory.MEDICINE, 169);
			super.price = 3000;
			super.bcat.add(BattleBagCategory.HP_PP);
			super.bcat.add(BattleBagCategory.STATUS);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " was fully healed!";
		}

		public boolean use(CharacterData player, ActivePokemon p)
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(b.getPlayer(), p);
		}
	}

	private static class Elixir extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;
		private boolean use(List<Move> moves)
		{
			boolean changed = false;
			for (Move m : moves)
			{
				changed |= m.increasePP(increaseAmount(m));
			}
			
			return changed;
		}

		public Elixir()
		{
			super(Namesies.ELIXIR_ITEM, "It restores the PP of all the moves learned by the targeted Pok\u00e9mon by 10 points each.", BagCategory.MEDICINE, 170);
			super.price = 3000;
			super.bcat.add(BattleBagCategory.HP_PP);
		}

		public int increaseAmount(Move m)
		{
			return 10;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s PP was restored!";
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			return use(p.getActualMoves());
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p.getMoves(b));
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class MaxElixir extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;
		private boolean use(List<Move> moves)
		{
			boolean changed = false;
			for (Move m : moves)
			{
				changed |= m.increasePP(increaseAmount(m));
			}
			
			return changed;
		}

		public MaxElixir()
		{
			super(Namesies.MAX_ELIXIR_ITEM, "It restores the PP of all the moves learned by the targeted Pok\u00e9mon by 10 points each.", BagCategory.MEDICINE, 171);
			super.price = 4500;
			super.bcat.add(BattleBagCategory.HP_PP);
		}

		public int increaseAmount(Move m)
		{
			return m.getMaxPP();
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s PP was restored!";
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			return use(p.getActualMoves());
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(p.getMoves(b));
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class Ether extends Item implements MoveUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;
		private String restore;

		public Ether()
		{
			super(Namesies.ETHER_ITEM, "It restores the PP of a Pok\u00e9mon's selected move by a maximum of 10 points.", BagCategory.MEDICINE, 172);
			super.price = 1200;
			super.bcat.add(BattleBagCategory.HP_PP);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s PP for " + restore + " PP was restored!";
		}

		public boolean use(ActivePokemon p, Move m)
		{
			// TODO: Need to be able to call these from the battle! (BattleMoveUse? yuck) -- Test messages once completed
			restore = m.getAttack().getName();
			return m.increasePP(10);
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class MaxEther extends Item implements MoveUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;
		private String restore;

		public MaxEther()
		{
			super(Namesies.MAX_ETHER_ITEM, "It fully restores the PP of a single selected move that has been learned by the target Pok\u00e9mon.", BagCategory.MEDICINE, 173);
			super.price = 2000;
			super.bcat.add(BattleBagCategory.HP_PP);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s PP for " + restore + " PP was restored!";
		}

		public boolean use(ActivePokemon p, Move m)
		{
			// TODO: Same as ether
			restore = m.getAttack().getName();
			return m.increasePP(m.getMaxPP());
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class BerryJuice extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public BerryJuice()
		{
			super(Namesies.BERRY_JUICE_ITEM, "A 100% pure juice made of Berries. It restores the HP of one Pok\u00e9mon by just 20 points.", BagCategory.MEDICINE, 174);
			super.price = 100;
			super.bcat.add(BattleBagCategory.HP_PP);
		}

		public int healAmount()
		{
			return 20;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s health was restored!";
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(b.getPlayer(), p);
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class SweetHeart extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public SweetHeart()
		{
			super(Namesies.SWEET_HEART_ITEM, "Very sweet chocolate. It restores the HP of one Pok\u00e9mon by only 20 points.", BagCategory.MEDICINE, 175);
			super.price = 100;
			super.bcat.add(BattleBagCategory.HP_PP);
		}

		public int healAmount()
		{
			return 20;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s health was restored!";
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(b.getPlayer(), p);
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class Potion extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public Potion()
		{
			super(Namesies.POTION_ITEM, "A spray-type medicine for wounds. It restores the HP of one Pok\u00e9mon by just 20 points.", BagCategory.MEDICINE, 176);
			super.price = 100;
			super.bcat.add(BattleBagCategory.HP_PP);
		}

		public int healAmount()
		{
			return 20;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s health was restored!";
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(b.getPlayer(), p);
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class EnergyPowder extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public EnergyPowder()
		{
			super(Namesies.ENERGY_POWDER_ITEM, "A very bitter medicine powder. It restores the HP of one Pok\u00e9mon by 50 points.", BagCategory.MEDICINE, 177);
			super.price = 500;
			super.bcat.add(BattleBagCategory.HP_PP);
		}

		public int healAmount()
		{
			return 50;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s health was restored!";
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(b.getPlayer(), p);
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class FreshWater extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public FreshWater()
		{
			super(Namesies.FRESH_WATER_ITEM, "Water with a high mineral content. It restores the HP of one Pok\u00e9mon by 50 points.", BagCategory.MEDICINE, 178);
			super.price = 200;
			super.bcat.add(BattleBagCategory.HP_PP);
		}

		public int healAmount()
		{
			return 50;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s health was restored!";
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(b.getPlayer(), p);
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class SuperPotion extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public SuperPotion()
		{
			super(Namesies.SUPER_POTION_ITEM, "A spray-type medicine for wounds. It restores the HP of one Pok\u00e9mon by 50 points.", BagCategory.MEDICINE, 179);
			super.price = 700;
			super.bcat.add(BattleBagCategory.HP_PP);
		}

		public int healAmount()
		{
			return 50;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s health was restored!";
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(b.getPlayer(), p);
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class SodaPop extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public SodaPop()
		{
			super(Namesies.SODA_POP_ITEM, "A fizzy soda drink. It restores the HP of one Pok\u00e9mon by 60 points.", BagCategory.MEDICINE, 180);
			super.price = 300;
			super.bcat.add(BattleBagCategory.HP_PP);
		}

		public int healAmount()
		{
			return 60;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s health was restored!";
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(b.getPlayer(), p);
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class Lemonade extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public Lemonade()
		{
			super(Namesies.LEMONADE_ITEM, "A very sweet drink. It restores the HP of one Pok\u00e9mon by 80 points.", BagCategory.MEDICINE, 181);
			super.price = 350;
			super.bcat.add(BattleBagCategory.HP_PP);
		}

		public int healAmount()
		{
			return 80;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s health was restored!";
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(b.getPlayer(), p);
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class MoomooMilk extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public MoomooMilk()
		{
			super(Namesies.MOOMOO_MILK_ITEM, "Milk with a very high nutrition content. It restores the HP of one Pok\u00e9mon by 100 points.", BagCategory.MEDICINE, 182);
			super.price = 500;
			super.bcat.add(BattleBagCategory.HP_PP);
		}

		public int healAmount()
		{
			return 100;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s health was restored!";
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(b.getPlayer(), p);
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class EnergyRoot extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public EnergyRoot()
		{
			super(Namesies.ENERGY_ROOT_ITEM, "A very bitter root. It restores the HP of one Pok\u00e9mon by 200 points.", BagCategory.MEDICINE, 183);
			super.price = 800;
			super.bcat.add(BattleBagCategory.HP_PP);
		}

		public int healAmount()
		{
			return 200;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s health was restored!";
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(b.getPlayer(), p);
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class HyperPotion extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public HyperPotion()
		{
			super(Namesies.HYPER_POTION_ITEM, "A spray-type medicine for wounds. It restores the HP of one Pok\u00e9mon by 200 points.", BagCategory.MEDICINE, 184);
			super.price = 1200;
			super.bcat.add(BattleBagCategory.HP_PP);
		}

		public int healAmount()
		{
			return 200;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s health was restored!";
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(b.getPlayer(), p);
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class MaxPotion extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public MaxPotion()
		{
			super(Namesies.MAX_POTION_ITEM, "A spray-type medicine for wounds. It completely restores the HP of a single Pok\u00e9mon.", BagCategory.MEDICINE, 185);
			super.price = 2500;
			super.bcat.add(BattleBagCategory.HP_PP);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s health was restored!";
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			return p.healHealthFraction(1) != 0;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(b.getPlayer(), p);
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class Revive extends Item implements PokemonUseItem, BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public Revive()
		{
			super(Namesies.REVIVE_ITEM, "A medicine that revives a fainted Pok\u00e9mon. It restores half the Pok\u00e9mon's maximum HP.", BagCategory.MEDICINE, 186);
			super.price = 1500;
			super.bcat.add(BattleBagCategory.STATUS);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " was revived!";
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			if (!p.hasStatus(StatusCondition.FAINTED)) return false;
			
			p.removeStatus();
			p.healHealthFraction(.5);
			
			return true;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(b.getPlayer(), p);
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class MaxRevive extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public MaxRevive()
		{
			super(Namesies.MAX_REVIVE_ITEM, "A medicine that revives a fainted Pok\u00e9mon. It fully restores the Pok\u00e9mon's HP.", BagCategory.MEDICINE, 187);
			super.price = 4000;
			super.bcat.add(BattleBagCategory.STATUS);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " was fully revived!";
		}

		public boolean use(CharacterData player, ActivePokemon p)
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class RevivalHerb extends Item implements PokemonUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public RevivalHerb()
		{
			super(Namesies.REVIVAL_HERB_ITEM, "A very bitter medicinal herb. It revives a fainted Pok\u00e9mon, fully restoring its HP.", BagCategory.MEDICINE, 188);
			super.price = 2800;
			super.bcat.add(BattleBagCategory.STATUS);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " was fully revived!";
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			if (!p.hasStatus(StatusCondition.FAINTED))
			{
				return false;
			}
			
			p.removeStatus();
			p.healHealthFraction(1);
			
			return true;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class SacredAsh extends Item implements TrainerUseItem, HoldItem, BattleUseItem
	{
		private static final long serialVersionUID = 1L;

		public SacredAsh()
		{
			super(Namesies.SACRED_ASH_ITEM, "It revives all fainted Pok\u00e9mon. In doing so, it also fully restores their HP.", BagCategory.MEDICINE, 189);
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
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
			super(Namesies.DIRE_HIT_ITEM, "It raises the critical-hit ratio greatly. It can be used only once and wears off if the Pok\u00e9mon is withdrawn.", BagCategory.STAT, 190);
			super.price = 650;
			super.bcat.add(BattleBagCategory.BATTLE);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " is getting pumped!";
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			PokemonEffect crits = PokemonEffect.getEffect(Namesies.RAISE_CRITS_EFFECT);
			if (!crits.applies(b, p, p, CastSource.USE_ITEM))
			{
				return false;
			}
			
			crits.cast(b, p, p, CastSource.USE_ITEM, false);
			return true;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class GuardSpec extends Item implements BattleUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;

		public GuardSpec()
		{
			super(Namesies.GUARD_SPEC_ITEM, "An item that prevents stat reduction among the Trainer's party Pok\u00e9mon for five turns after use.", BagCategory.STAT, 191);
			super.price = 700;
			super.bcat.add(BattleBagCategory.BATTLE);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " is covered by a veil!";
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			PokemonEffect gSpesh = PokemonEffect.getEffect(Namesies.GUARD_SPECIAL_EFFECT);
			if (!gSpesh.applies(b, p, p, CastSource.USE_ITEM)) return false;
			
			gSpesh.cast(b, p, p, CastSource.USE_ITEM, false);
			return true;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class XAccuracy extends Item implements HoldItem, BattleUseItem
	{
		private static final long serialVersionUID = 1L;

		public XAccuracy()
		{
			super(Namesies.XACCURACY_ITEM, "An item that raises the accuracy of a Pok\u00e9mon in battle. It wears off if the Pok\u00e9mon is withdrawn.", BagCategory.STAT, 192);
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

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return p.getAttributes().modifyStage(p, p, 1, toIncrease(), b, CastSource.USE_ITEM);
		}
	}

	private static class XAttack extends Item implements HoldItem, BattleUseItem
	{
		private static final long serialVersionUID = 1L;

		public XAttack()
		{
			super(Namesies.XATTACK_ITEM, "An item that raises the Attack stat of a Pok\u00e9mon in battle. It wears off if the Pok\u00e9mon is withdrawn.", BagCategory.STAT, 193);
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

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return p.getAttributes().modifyStage(p, p, 1, toIncrease(), b, CastSource.USE_ITEM);
		}
	}

	private static class XDefend extends Item implements HoldItem, BattleUseItem
	{
		private static final long serialVersionUID = 1L;

		public XDefend()
		{
			super(Namesies.XDEFEND_ITEM, "An item that raises the Defense stat of a Pok\u00e9mon in battle. It wears off if the Pok\u00e9mon is withdrawn.", BagCategory.STAT, 194);
			super.price = 550;
			super.bcat.add(BattleBagCategory.BATTLE);
		}

		public Stat toIncrease()
		{
			return Stat.DEFENSE;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return p.getAttributes().modifyStage(p, p, 1, toIncrease(), b, CastSource.USE_ITEM);
		}
	}

	private static class XSpecial extends Item implements HoldItem, BattleUseItem
	{
		private static final long serialVersionUID = 1L;

		public XSpecial()
		{
			super(Namesies.XSPECIAL_ITEM, "An item that raises the Sp. Atk stat of a Pok\u00e9mon in battle. It wears off if the Pok\u00e9mon is withdrawn.", BagCategory.STAT, 195);
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

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return p.getAttributes().modifyStage(p, p, 1, toIncrease(), b, CastSource.USE_ITEM);
		}
	}

	private static class XSpDef extends Item implements HoldItem, BattleUseItem
	{
		private static final long serialVersionUID = 1L;

		public XSpDef()
		{
			super(Namesies.XSP_DEF_ITEM, "An item that raises the Sp. Def stat of a Pok\u00e9mon in battle. It wears off if the Pok\u00e9mon is withdrawn.", BagCategory.STAT, 196);
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

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return p.getAttributes().modifyStage(p, p, 1, toIncrease(), b, CastSource.USE_ITEM);
		}
	}

	private static class XSpeed extends Item implements HoldItem, BattleUseItem
	{
		private static final long serialVersionUID = 1L;

		public XSpeed()
		{
			super(Namesies.XSPEED_ITEM, "An item that raises the Speed stat of a Pok\u00e9mon in battle. It wears off if the Pok\u00e9mon is withdrawn.", BagCategory.STAT, 197);
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

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return p.getAttributes().modifyStage(p, p, 1, toIncrease(), b, CastSource.USE_ITEM);
		}
	}

	private static class HPUp extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public HPUp()
		{
			super(Namesies.HPUP_ITEM, "A nutritious drink for Pok\u00e9mon. It raises the base HP of a single Pok\u00e9mon.", BagCategory.STAT, 198);
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

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}
	}

	private static class Protein extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public Protein()
		{
			super(Namesies.PROTEIN_ITEM, "A nutritious drink for Pok\u00e9mon. It raises the base Attack stat of a single Pok\u00e9mon.", BagCategory.STAT, 199);
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

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}
	}

	private static class Iron extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public Iron()
		{
			super(Namesies.IRON_ITEM, "A nutritious drink for Pok\u00e9mon. It raises the base Defense stat of a single Pok\u00e9mon.", BagCategory.STAT, 200);
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

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}
	}

	private static class Calcium extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public Calcium()
		{
			super(Namesies.CALCIUM_ITEM, "A nutritious drink for Pok\u00e9mon. It raises the base Sp. Atk (Special Attack) stat of a single Pok\u00e9mon.", BagCategory.STAT, 201);
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

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}
	}

	private static class Zinc extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public Zinc()
		{
			super(Namesies.ZINC_ITEM, "A nutritious drink for Pok\u00e9mon. It raises the base Sp. Def (Special Defense) stat of a single Pok\u00e9mon.", BagCategory.STAT, 202);
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

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}
	}

	private static class Carbos extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public Carbos()
		{
			super(Namesies.CARBOS_ITEM, "A nutritious drink for Pok\u00e9mon. It raises the base Speed stat of a single Pok\u00e9mon.", BagCategory.STAT, 203);
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

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}
	}

	private static class HealthWing extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public HealthWing()
		{
			super(Namesies.HEALTH_WING_ITEM, "An item for use on a Pok\u00e9mon. It slightly increases the base HP of a single Pok\u00e9mon.", BagCategory.STAT, 204);
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

		public int flingDamage()
		{
			return 20;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}
	}

	private static class MuscleWing extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public MuscleWing()
		{
			super(Namesies.MUSCLE_WING_ITEM, "An item for use on a Pok\u00e9mon. It slightly increases the base Attack stat of a single Pok\u00e9mon.", BagCategory.STAT, 205);
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

		public int flingDamage()
		{
			return 20;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}
	}

	private static class ResistWing extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public ResistWing()
		{
			super(Namesies.RESIST_WING_ITEM, "An item for use on a Pok\u00e9mon. It slightly increases the base Defense stat of a single Pok\u00e9mon.", BagCategory.STAT, 206);
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

		public int flingDamage()
		{
			return 20;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}
	}

	private static class GeniusWing extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public GeniusWing()
		{
			super(Namesies.GENIUS_WING_ITEM, "An item for use on a Pok\u00e9mon. It slightly increases the base Sp. Atk stat of a single Pok\u00e9mon.", BagCategory.STAT, 207);
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

		public int flingDamage()
		{
			return 20;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}
	}

	private static class CleverWing extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public CleverWing()
		{
			super(Namesies.CLEVER_WING_ITEM, "An item for use on a Pok\u00e9mon. It slightly increases the base Sp. Def stat of a single Pok\u00e9mon.", BagCategory.STAT, 208);
			super.price = 3000;
		}

		public Stat toIncrease()
		{
			return Stat.SP_DEFENSE;
		}

		public int increaseAmount()
		{
			return 1;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public int flingDamage()
		{
			return 20;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}
	}

	private static class SwiftWing extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public SwiftWing()
		{
			super(Namesies.SWIFT_WING_ITEM, "An item for use on a Pok\u00e9mon. It slightly increases the base Speed stat of a single Pok\u00e9mon.", BagCategory.STAT, 209);
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

		public int flingDamage()
		{
			return 20;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}
	}

	private static class PPMax extends Item implements MoveUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;
		private String increase;

		public PPMax()
		{
			super(Namesies.PPMAX_ITEM, "It maximally raises the top PP of a selected move that has been learned by the target Pok\u00e9mon.", BagCategory.STAT, 210);
			super.price = 9800;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + increase + "'s Max PP was increased!";
		}

		public boolean use(ActivePokemon p, Move m)
		{
			increase = m.getAttack().getName();
			return m.increaseMaxPP(3);
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class PPUp extends Item implements MoveUseItem, HoldItem
	{
		private static final long serialVersionUID = 1L;
		private String increase;

		public PPUp()
		{
			super(Namesies.PPUP_ITEM, "It slightly raises the maximum PP of a selected move that has been learned by the target Pok\u00e9mon.", BagCategory.STAT, 211);
			super.price = 9800;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + increase + "'s Max PP was increased!";
		}

		public boolean use(ActivePokemon p, Move m)
		{
			increase = m.getAttack().getName();
			return m.increaseMaxPP(1);
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class RareCandy extends Item implements HoldItem, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public RareCandy()
		{
			super(Namesies.RARE_CANDY_ITEM, "A candy that is packed with energy. It raises the level of a single Pok\u00e9mon by one.", BagCategory.STAT, 212);
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			// TODO: Need Level Up to be implemented -- also handle the message accordingly
			return false;
		}
	}

	private static class CherishBall extends Item implements BallItem
	{
		private static final long serialVersionUID = 1L;

		public CherishBall()
		{
			super(Namesies.CHERISH_BALL_ITEM, "A quite rare Pok\u00e9 Ball that has been specially crafted to commemorate an occasion of some sort.", BagCategory.BALL, 213);
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
			super(Namesies.DIVE_BALL_ITEM, "A somewhat different Pok\u00e9 Ball that works especially well on Pok\u00e9mon that live underwater.", BagCategory.BALL, 214);
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
			super(Namesies.DUSK_BALL_ITEM, "A somewhat different Pok\u00e9 Ball that makes it easier to catch wild Pok\u00e9mon at night or in dark places like caves.", BagCategory.BALL, 215);
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
			super(Namesies.FAST_BALL_ITEM, "A Pok\u00e9 Ball that makes it easier to catch Pok\u00e9mon which are quick to run away.", BagCategory.BALL, 216);
			super.price = 200;
			super.bcat.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b)
		{
			// If the opponent has a base speed of 100 or higher, multiplier is 4
			if (o.getPokemonInfo().getStat(Stat.SPEED.index()) >= 100)
			{
				return new double[] {4, 0};
			}
			
			return new double[] {1, 0};
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
			super(Namesies.GREAT_BALL_ITEM, "A good, high-performance Ball that provides a higher Pok\u00e9mon catch rate than a standard Pok\u00e9 Ball.", BagCategory.BALL, 217);
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
			super(Namesies.HEAL_BALL_ITEM, "A remedial Pok\u00e9 Ball that restores the caught Pok\u00e9mon's HP and eliminates any status problem.", BagCategory.BALL, 218);
			super.price = 300;
			super.bcat.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b)
		{
			return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p)
		{
			p.fullyHeal();
		}
	}

	private static class HeavyBall extends Item implements BallItem
	{
		private static final long serialVersionUID = 1L;

		public HeavyBall()
		{
			super(Namesies.HEAVY_BALL_ITEM, "A Pok\u00e9 Ball for catching very heavy Pok\u00e9mon.", BagCategory.BALL, 219);
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
			super(Namesies.LEVEL_BALL_ITEM, "A Pok\u00e9 Ball for catching Pok\u00e9mon that are a lower level than your own.", BagCategory.BALL, 220);
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
			super(Namesies.LOVE_BALL_ITEM, "Pok\u00e9 Ball for catching Pok\u00e9mon that are the opposite gender of your Pok\u00e9mon.", BagCategory.BALL, 221);
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
			super(Namesies.LURE_BALL_ITEM, "A Pok\u00e9 Ball for catching Pok\u00e9mon hooked by a Rod when fishing.", BagCategory.BALL, 222);
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
			super(Namesies.LUXURY_BALL_ITEM, "A comfortable Pok\u00e9 Ball that makes a caught wild Pok\u00e9mon quickly grow friendly.", BagCategory.BALL, 223);
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
			super(Namesies.MASTER_BALL_ITEM, "The best Ball with the ultimate level of performance. It will catch any wild Pok\u00e9mon without fail.", BagCategory.BALL, 224);
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
			super(Namesies.MOON_BALL_ITEM, "A Pok\u00e9 Ball for catching Pok\u00e9mon that evolve using the Moon Stone.", BagCategory.BALL, 225);
			super.price = 200;
			super.bcat.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b)
		{
			Evolution ev = o.getPokemonInfo().getEvolution();
			if (ev.getEvolution(EvolutionCheck.ITEM, o, Namesies.MOON_STONE_ITEM) != null)
			{
				return new double[] {4, 0};
			}
			
			return new double[] {1, 0};
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
			super(Namesies.NEST_BALL_ITEM, "A somewhat different Pok\u00e9 Ball that works especially well on weaker Pok\u00e9mon in the wild.", BagCategory.BALL, 226);
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
			super(Namesies.NET_BALL_ITEM, "A somewhat different Pok\u00e9 Ball that works especially well on Water- and Bug-type Pok\u00e9mon.", BagCategory.BALL, 227);
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
			super(Namesies.POKE_BALL_ITEM, "A device for catching wild Pok\u00e9mon. It is thrown like a ball at the target. It is designed as a capsule system.", BagCategory.BALL, 228);
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
			super(Namesies.PREMIER_BALL_ITEM, "A somewhat rare Pok\u00e9 Ball that has been specially made to commemorate an event of some sort.", BagCategory.BALL, 229);
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
			super(Namesies.QUICK_BALL_ITEM, "A somewhat different Pok\u00e9 Ball that provides a better catch rate if it is used at the start of a wild encounter.", BagCategory.BALL, 230);
			super.price = 1000;
			super.bcat.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (b.getTurn() == 1)
			{
				return new double[] {3, 0};
			}
			
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
			super(Namesies.REPEAT_BALL_ITEM, "A somewhat different Pok\u00e9 Ball that works especially well on Pok\u00e9mon species that were previously caught.", BagCategory.BALL, 231);
			super.price = 1000;
			super.bcat.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (b.getPlayer().getPokedex().caught(o.getPokemonInfo().namesies()))
			{
				return new double[] {3, 0};
			}
			
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
			super(Namesies.SAFARI_BALL_ITEM, "A special Pok\u00e9 Ball that is used only in the Safari Zone. It is decorated in a camouflage pattern.", BagCategory.BALL, 232);
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
			super(Namesies.TIMER_BALL_ITEM, "A somewhat different Ball that becomes progressively better the more turns there are in a battle.", BagCategory.BALL, 233);
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
			super(Namesies.ULTRA_BALL_ITEM, "An ultra-performance Ball that provides a higher Pok\u00e9mon catch rate than a Great Ball.", BagCategory.BALL, 234);
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

	private static class CheriBerry extends Item implements StatusBerry, PokemonUseItem, BattleUseItem
	{
		private static final long serialVersionUID = 1L;

		public CheriBerry()
		{
			super(Namesies.CHERI_BERRY_ITEM, "If held by a Pok\u00e9mon, it recovers from paralysis.", BagCategory.BERRY, 235);
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

		public boolean use(CharacterData player, ActivePokemon p)
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
			return use(b.getPlayer(), p);
		}

		public String getHoldSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + this.getName() + " cured it of its status condition!";
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source)
		{
			if (!use(user, b))
			{
				return false;
			}
			
			String message = "";
			switch (source)
			{
				case USE_ITEM:
				message = getSuccessMessage(user);
				break;
				case HELD_ITEM:
				message = getHoldSuccessMessage(user);
				break;
				default:
				Global.error("Use item and held item are the only valid cast sources for berries.");
			}
			
			b.addMessage(message, user);
			
			if (user.hasAbility(Namesies.CHEEK_POUCH_ABILITY) && !user.fullHealth())
			{
				b.addMessage(user.getName() + "'s " + Namesies.CHEEK_POUCH_ABILITY.getName() + " restored its health!");
				user.healHealthFraction(1/3.0);
				b.addMessage("", user);
			}
			
			// Eat dat berry!!
			PokemonEffect.getEffect(Namesies.EATEN_BERRY_EFFECT).cast(b, user, user, source, false);
			
			return true;
		}

		public int naturalGiftPower()
		{
			return 80;
		}

		public Type naturalGiftType()
		{
			return Type.FIRE;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class ChestoBerry extends Item implements StatusBerry, PokemonUseItem, BattleUseItem
	{
		private static final long serialVersionUID = 1L;

		public ChestoBerry()
		{
			super(Namesies.CHESTO_BERRY_ITEM, "If held by a Pok\u00e9mon, it recovers from sleep.", BagCategory.BERRY, 236);
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

		public boolean use(CharacterData player, ActivePokemon p)
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
			return use(b.getPlayer(), p);
		}

		public String getHoldSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + this.getName() + " cured it of its status condition!";
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source)
		{
			if (!use(user, b))
			{
				return false;
			}
			
			String message = "";
			switch (source)
			{
				case USE_ITEM:
				message = getSuccessMessage(user);
				break;
				case HELD_ITEM:
				message = getHoldSuccessMessage(user);
				break;
				default:
				Global.error("Use item and held item are the only valid cast sources for berries.");
			}
			
			b.addMessage(message, user);
			
			if (user.hasAbility(Namesies.CHEEK_POUCH_ABILITY) && !user.fullHealth())
			{
				b.addMessage(user.getName() + "'s " + Namesies.CHEEK_POUCH_ABILITY.getName() + " restored its health!");
				user.healHealthFraction(1/3.0);
				b.addMessage("", user);
			}
			
			// Eat dat berry!!
			PokemonEffect.getEffect(Namesies.EATEN_BERRY_EFFECT).cast(b, user, user, source, false);
			
			return true;
		}

		public int naturalGiftPower()
		{
			return 80;
		}

		public Type naturalGiftType()
		{
			return Type.WATER;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class PechaBerry extends Item implements StatusBerry, PokemonUseItem, BattleUseItem
	{
		private static final long serialVersionUID = 1L;

		public PechaBerry()
		{
			super(Namesies.PECHA_BERRY_ITEM, "If held by a Pok\u00e9mon, it recovers from poison.", BagCategory.BERRY, 237);
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

		public boolean use(CharacterData player, ActivePokemon p)
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
			return use(b.getPlayer(), p);
		}

		public String getHoldSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + this.getName() + " cured it of its status condition!";
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source)
		{
			if (!use(user, b))
			{
				return false;
			}
			
			String message = "";
			switch (source)
			{
				case USE_ITEM:
				message = getSuccessMessage(user);
				break;
				case HELD_ITEM:
				message = getHoldSuccessMessage(user);
				break;
				default:
				Global.error("Use item and held item are the only valid cast sources for berries.");
			}
			
			b.addMessage(message, user);
			
			if (user.hasAbility(Namesies.CHEEK_POUCH_ABILITY) && !user.fullHealth())
			{
				b.addMessage(user.getName() + "'s " + Namesies.CHEEK_POUCH_ABILITY.getName() + " restored its health!");
				user.healHealthFraction(1/3.0);
				b.addMessage("", user);
			}
			
			// Eat dat berry!!
			PokemonEffect.getEffect(Namesies.EATEN_BERRY_EFFECT).cast(b, user, user, source, false);
			
			return true;
		}

		public int naturalGiftPower()
		{
			return 80;
		}

		public Type naturalGiftType()
		{
			return Type.ELECTRIC;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class RawstBerry extends Item implements StatusBerry, PokemonUseItem, BattleUseItem
	{
		private static final long serialVersionUID = 1L;

		public RawstBerry()
		{
			super(Namesies.RAWST_BERRY_ITEM, "If held by a Pok\u00e9mon, it recovers from a burn.", BagCategory.BERRY, 238);
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

		public boolean use(CharacterData player, ActivePokemon p)
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
			return use(b.getPlayer(), p);
		}

		public String getHoldSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + this.getName() + " cured it of its status condition!";
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source)
		{
			if (!use(user, b))
			{
				return false;
			}
			
			String message = "";
			switch (source)
			{
				case USE_ITEM:
				message = getSuccessMessage(user);
				break;
				case HELD_ITEM:
				message = getHoldSuccessMessage(user);
				break;
				default:
				Global.error("Use item and held item are the only valid cast sources for berries.");
			}
			
			b.addMessage(message, user);
			
			if (user.hasAbility(Namesies.CHEEK_POUCH_ABILITY) && !user.fullHealth())
			{
				b.addMessage(user.getName() + "'s " + Namesies.CHEEK_POUCH_ABILITY.getName() + " restored its health!");
				user.healHealthFraction(1/3.0);
				b.addMessage("", user);
			}
			
			// Eat dat berry!!
			PokemonEffect.getEffect(Namesies.EATEN_BERRY_EFFECT).cast(b, user, user, source, false);
			
			return true;
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class AspearBerry extends Item implements StatusBerry, PokemonUseItem, BattleUseItem
	{
		private static final long serialVersionUID = 1L;

		public AspearBerry()
		{
			super(Namesies.ASPEAR_BERRY_ITEM, "If held by a Pok\u00e9mon, it defrosts it.", BagCategory.BERRY, 239);
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

		public boolean use(CharacterData player, ActivePokemon p)
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
			return use(b.getPlayer(), p);
		}

		public String getHoldSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + this.getName() + " cured it of its status condition!";
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source)
		{
			if (!use(user, b))
			{
				return false;
			}
			
			String message = "";
			switch (source)
			{
				case USE_ITEM:
				message = getSuccessMessage(user);
				break;
				case HELD_ITEM:
				message = getHoldSuccessMessage(user);
				break;
				default:
				Global.error("Use item and held item are the only valid cast sources for berries.");
			}
			
			b.addMessage(message, user);
			
			if (user.hasAbility(Namesies.CHEEK_POUCH_ABILITY) && !user.fullHealth())
			{
				b.addMessage(user.getName() + "'s " + Namesies.CHEEK_POUCH_ABILITY.getName() + " restored its health!");
				user.healHealthFraction(1/3.0);
				b.addMessage("", user);
			}
			
			// Eat dat berry!!
			PokemonEffect.getEffect(Namesies.EATEN_BERRY_EFFECT).cast(b, user, user, source, false);
			
			return true;
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class LeppaBerry extends Item implements EndTurnEffect, MoveUseItem, GainableEffectBerry
	{
		private static final long serialVersionUID = 1L;
		private String restore;

		public LeppaBerry()
		{
			super(Namesies.LEPPA_BERRY_ITEM, "If held by a Pok\u00e9mon, it restores a move's PP by 10.", BagCategory.BERRY, 240);
			super.price = 20;
			super.bcat.add(BattleBagCategory.HP_PP);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s PP for " + restore + " PP was restored!";
		}

		public String getHoldSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + this.getName() + " restored " + restore + "'s PP!";
		}

		public void applyEndTurn(ActivePokemon victim, Battle b)
		{
			for (Move m : victim.getMoves(b))
			{
				if (m.getPP() == 0)
				{
					use(victim, m);
					b.addMessage(getHoldSuccessMessage(victim));
					victim.consumeItem(b);
					break;
				}
			}
		}

		public boolean use(ActivePokemon p, Move m)
		{
			restore = m.getAttack().getName();
			return m.increasePP(10);
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source)
		{
			List<Move> list = new ArrayList<>();
			for (Move m : user.getMoves(b))
			{
				if (m.getPP() < m.getMaxPP())
				{
					list.add(m);
				}
			}
			
			int size = list.size();
			if (size == 0)
			{
				return false;
			}
			
			// TODO: This should probably heal the move with the smallest PP ratio, not at random
			use(user, list.get((int)(Math.random()*size)));
			
			String message = "";
			switch (source)
			{
				case USE_ITEM:
				message = getSuccessMessage(user);
				break;
				case HELD_ITEM:
				message = getHoldSuccessMessage(user);
				break;
				default:
				Global.error("Use item and held item are the only valid cast sources for berries.");
			}
			
			b.addMessage(message, user);
			
			if (user.hasAbility(Namesies.CHEEK_POUCH_ABILITY) && !user.fullHealth())
			{
				b.addMessage(user.getName() + "'s " + Namesies.CHEEK_POUCH_ABILITY.getName() + " restored its health!");
				user.healHealthFraction(1/3.0);
				b.addMessage("", user);
			}
			
			// Eat dat berry!!
			PokemonEffect.getEffect(Namesies.EATEN_BERRY_EFFECT).cast(b, user, user, source, false);
			
			return true;
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class OranBerry extends Item implements HealthTriggeredBerry, PokemonUseItem, BattleUseItem
	{
		private static final long serialVersionUID = 1L;

		public OranBerry()
		{
			super(Namesies.ORAN_BERRY_ITEM, "If held by a Pok\u00e9mon, it heals the user by just 10 HP.", BagCategory.BERRY, 241);
			super.price = 20;
			super.bcat.add(BattleBagCategory.HP_PP);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s health was restored!";
		}

		public String getHoldSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " was healed by its " + this.name + "!";
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user, CastSource source)
		{
			return use(user, b);
		}

		public double healthTriggerRatio()
		{
			return 1/3.0;
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			return p.heal(10) != 0;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(b.getPlayer(), p);
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source)
		{
			if (!useHealthTriggerBerry(b, user, source))
			{
				return false;
			}
			
			String message = "";
			switch (source)
			{
				case USE_ITEM:
				message = getSuccessMessage(user);
				break;
				case HELD_ITEM:
				message = getHoldSuccessMessage(user);
				break;
				default:
				Global.error("Use item and held item are the only valid cast sources for berries.");
			}
			
			b.addMessage(message, user);
			
			if (user.hasAbility(Namesies.CHEEK_POUCH_ABILITY) && !user.fullHealth())
			{
				b.addMessage(user.getName() + "'s " + Namesies.CHEEK_POUCH_ABILITY.getName() + " restored its health!");
				user.healHealthFraction(1/3.0);
				b.addMessage("", user);
			}
			
			// Eat dat berry!!
			PokemonEffect.getEffect(Namesies.EATEN_BERRY_EFFECT).cast(b, user, user, source, false);
			
			return true;
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class PersimBerry extends Item implements BattleUseItem, GainableEffectBerry
	{
		private static final long serialVersionUID = 1L;

		public PersimBerry()
		{
			super(Namesies.PERSIM_BERRY_ITEM, "If held by a Pok\u00e9mon, it recovers from confusion.", BagCategory.BERRY, 242);
			super.price = 20;
			super.bcat.add(BattleBagCategory.HP_PP);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " snapped out of its confusion!";
		}

		public String getHoldSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + this.getName() + " snapped it out of confusion!";
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			if (p.hasEffect(Namesies.CONFUSION_EFFECT))
			{
				p.getAttributes().removeEffect(Namesies.CONFUSION_EFFECT);
				return true;
			}
			
			return false;
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source)
		{
			if (!use(user, b))
			{
				return false;
			}
			
			String message = "";
			switch (source)
			{
				case USE_ITEM:
				message = getSuccessMessage(user);
				break;
				case HELD_ITEM:
				message = getHoldSuccessMessage(user);
				break;
				default:
				Global.error("Use item and held item are the only valid cast sources for berries.");
			}
			
			b.addMessage(message, user);
			
			if (user.hasAbility(Namesies.CHEEK_POUCH_ABILITY) && !user.fullHealth())
			{
				b.addMessage(user.getName() + "'s " + Namesies.CHEEK_POUCH_ABILITY.getName() + " restored its health!");
				user.healHealthFraction(1/3.0);
				b.addMessage("", user);
			}
			
			// Eat dat berry!!
			PokemonEffect.getEffect(Namesies.EATEN_BERRY_EFFECT).cast(b, user, user, source, false);
			
			return true;
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class LumBerry extends Item implements PokemonUseItem, BattleUseItem, StatusBerry
	{
		private static final long serialVersionUID = 1L;

		public LumBerry()
		{
			super(Namesies.LUM_BERRY_ITEM, "If held by a Pok\u00e9mon, it recovers from any status problem.", BagCategory.BERRY, 243);
			super.price = 20;
			super.bcat.add(BattleBagCategory.STATUS);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " was cured of its status condition!";
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			if (!p.hasStatus() || p.hasStatus(StatusCondition.FAINTED))
			{
				return false;
			}
			
			p.removeStatus();
			return true;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(b.getPlayer(), p);
		}

		public String getHoldSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + this.getName() + " cured it of its status condition!";
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source)
		{
			if (!use(user, b))
			{
				return false;
			}
			
			String message = "";
			switch (source)
			{
				case USE_ITEM:
				message = getSuccessMessage(user);
				break;
				case HELD_ITEM:
				message = getHoldSuccessMessage(user);
				break;
				default:
				Global.error("Use item and held item are the only valid cast sources for berries.");
			}
			
			b.addMessage(message, user);
			
			if (user.hasAbility(Namesies.CHEEK_POUCH_ABILITY) && !user.fullHealth())
			{
				b.addMessage(user.getName() + "'s " + Namesies.CHEEK_POUCH_ABILITY.getName() + " restored its health!");
				user.healHealthFraction(1/3.0);
				b.addMessage("", user);
			}
			
			// Eat dat berry!!
			PokemonEffect.getEffect(Namesies.EATEN_BERRY_EFFECT).cast(b, user, user, source, false);
			
			return true;
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class SitrusBerry extends Item implements PokemonUseItem, BattleUseItem, HealthTriggeredBerry
	{
		private static final long serialVersionUID = 1L;

		public SitrusBerry()
		{
			super(Namesies.SITRUS_BERRY_ITEM, "If held by a Pok\u00e9mon, it heals the user by a little.", BagCategory.BERRY, 244);
			super.price = 20;
			super.bcat.add(BattleBagCategory.HP_PP);
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s health was restored!";
		}

		public String getHoldSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " was healed by its " + this.name + "!";
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			return p.healHealthFraction(1/4.0) != 0;
		}

		public boolean use(ActivePokemon p, Battle b)
		{
			return use(b.getPlayer(), p);
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user, CastSource source)
		{
			return use(user, b);
		}

		public double healthTriggerRatio()
		{
			return 1/2.0;
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source)
		{
			if (!useHealthTriggerBerry(b, user, source))
			{
				return false;
			}
			
			String message = "";
			switch (source)
			{
				case USE_ITEM:
				message = getSuccessMessage(user);
				break;
				case HELD_ITEM:
				message = getHoldSuccessMessage(user);
				break;
				default:
				Global.error("Use item and held item are the only valid cast sources for berries.");
			}
			
			b.addMessage(message, user);
			
			if (user.hasAbility(Namesies.CHEEK_POUCH_ABILITY) && !user.fullHealth())
			{
				b.addMessage(user.getName() + "'s " + Namesies.CHEEK_POUCH_ABILITY.getName() + " restored its health!");
				user.healHealthFraction(1/3.0);
				b.addMessage("", user);
			}
			
			// Eat dat berry!!
			PokemonEffect.getEffect(Namesies.EATEN_BERRY_EFFECT).cast(b, user, user, source, false);
			
			return true;
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class RazzBerry extends Item implements Berry
	{
		private static final long serialVersionUID = 1L;

		public RazzBerry()
		{
			super(Namesies.RAZZ_BERRY_ITEM, "A very valuable berry. Useful for aquiring value.", BagCategory.BERRY, 245);
			super.price = 60000;
		}

		public int naturalGiftPower()
		{
			return 80;
		}

		public Type naturalGiftType()
		{
			return Type.STEEL;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class PomegBerry extends Item implements Berry, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public PomegBerry()
		{
			super(Namesies.POMEG_BERRY_ITEM, "Using it on a Pok\u00e9mon lowers its base HP.", BagCategory.BERRY, 246);
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
			return 90;
		}

		public Type naturalGiftType()
		{
			return Type.ICE;
		}

		public boolean use(CharacterData player, ActivePokemon p)
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class KelpsyBerry extends Item implements Berry, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public KelpsyBerry()
		{
			super(Namesies.KELPSY_BERRY_ITEM, "Using it on a Pok\u00e9mon lowers its base Attack stat.", BagCategory.BERRY, 247);
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
			return 90;
		}

		public Type naturalGiftType()
		{
			return Type.FIGHTING;
		}

		public boolean use(CharacterData player, ActivePokemon p)
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class QualotBerry extends Item implements Berry, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public QualotBerry()
		{
			super(Namesies.QUALOT_BERRY_ITEM, "Using it on a Pok\u00e9mon lowers its base Defense stat.", BagCategory.BERRY, 248);
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
			return 90;
		}

		public Type naturalGiftType()
		{
			return Type.POISON;
		}

		public boolean use(CharacterData player, ActivePokemon p)
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class HondewBerry extends Item implements Berry, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public HondewBerry()
		{
			super(Namesies.HONDEW_BERRY_ITEM, "Using it on a Pok\u00e9mon lowers its base Sp. Atk stat.", BagCategory.BERRY, 249);
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
			return 90;
		}

		public Type naturalGiftType()
		{
			return Type.GROUND;
		}

		public boolean use(CharacterData player, ActivePokemon p)
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class GrepaBerry extends Item implements Berry, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public GrepaBerry()
		{
			super(Namesies.GREPA_BERRY_ITEM, "Using it on a Pok\u00e9mon lowers its base Sp. Def stat.", BagCategory.BERRY, 250);
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
			return 90;
		}

		public Type naturalGiftType()
		{
			return Type.FLYING;
		}

		public boolean use(CharacterData player, ActivePokemon p)
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class TamatoBerry extends Item implements Berry, PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public TamatoBerry()
		{
			super(Namesies.TAMATO_BERRY_ITEM, "Using it on a Pok\u00e9mon lowers its base Speed.", BagCategory.BERRY, 251);
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
			return 90;
		}

		public Type naturalGiftType()
		{
			return Type.PSYCHIC;
		}

		public boolean use(CharacterData player, ActivePokemon p)
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class OccaBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public OccaBerry()
		{
			super(Namesies.OCCA_BERRY_ITEM, "Weakens a supereffective Fire-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 252);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.FIRE;
		}

		public int naturalGiftPower()
		{
			return 80;
		}

		public Type naturalGiftType()
		{
			return Type.FIRE;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttackType() == getType() && Type.getAdvantage(user, victim, b) > 1)
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class PasshoBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public PasshoBerry()
		{
			super(Namesies.PASSHO_BERRY_ITEM, "Weakens a supereffective Water-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 253);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.WATER;
		}

		public int naturalGiftPower()
		{
			return 80;
		}

		public Type naturalGiftType()
		{
			return Type.WATER;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttackType() == getType() && Type.getAdvantage(user, victim, b) > 1)
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class WacanBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public WacanBerry()
		{
			super(Namesies.WACAN_BERRY_ITEM, "Weakens a supereffective Electric-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 254);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.ELECTRIC;
		}

		public int naturalGiftPower()
		{
			return 80;
		}

		public Type naturalGiftType()
		{
			return Type.ELECTRIC;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttackType() == getType() && Type.getAdvantage(user, victim, b) > 1)
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class RindoBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public RindoBerry()
		{
			super(Namesies.RINDO_BERRY_ITEM, "Weakens a supereffective Grass-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 255);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.GRASS;
		}

		public int naturalGiftPower()
		{
			return 80;
		}

		public Type naturalGiftType()
		{
			return Type.GRASS;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttackType() == getType() && Type.getAdvantage(user, victim, b) > 1)
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class YacheBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public YacheBerry()
		{
			super(Namesies.YACHE_BERRY_ITEM, "Weakens a supereffective Ice-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 256);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.ICE;
		}

		public int naturalGiftPower()
		{
			return 80;
		}

		public Type naturalGiftType()
		{
			return Type.ICE;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttackType() == getType() && Type.getAdvantage(user, victim, b) > 1)
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class ChopleBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public ChopleBerry()
		{
			super(Namesies.CHOPLE_BERRY_ITEM, "Weakens a supereffective Fighting-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 257);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.FIGHTING;
		}

		public int naturalGiftPower()
		{
			return 80;
		}

		public Type naturalGiftType()
		{
			return Type.FIGHTING;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttackType() == getType() && Type.getAdvantage(user, victim, b) > 1)
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class KebiaBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public KebiaBerry()
		{
			super(Namesies.KEBIA_BERRY_ITEM, "Weakens a supereffective Poison-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 258);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.POISON;
		}

		public int naturalGiftPower()
		{
			return 80;
		}

		public Type naturalGiftType()
		{
			return Type.POISON;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttackType() == getType() && Type.getAdvantage(user, victim, b) > 1)
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class ShucaBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public ShucaBerry()
		{
			super(Namesies.SHUCA_BERRY_ITEM, "Weakens a supereffective Ground-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 259);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.GROUND;
		}

		public int naturalGiftPower()
		{
			return 80;
		}

		public Type naturalGiftType()
		{
			return Type.GROUND;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttackType() == getType() && Type.getAdvantage(user, victim, b) > 1)
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class CobaBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public CobaBerry()
		{
			super(Namesies.COBA_BERRY_ITEM, "Weakens a supereffective Flying-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 260);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.FLYING;
		}

		public int naturalGiftPower()
		{
			return 80;
		}

		public Type naturalGiftType()
		{
			return Type.FLYING;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttackType() == getType() && Type.getAdvantage(user, victim, b) > 1)
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class PayapaBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public PayapaBerry()
		{
			super(Namesies.PAYAPA_BERRY_ITEM, "Weakens a supereffective Psychic-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 261);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.PSYCHIC;
		}

		public int naturalGiftPower()
		{
			return 80;
		}

		public Type naturalGiftType()
		{
			return Type.PSYCHIC;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttackType() == getType() && Type.getAdvantage(user, victim, b) > 1)
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class TangaBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public TangaBerry()
		{
			super(Namesies.TANGA_BERRY_ITEM, "Weakens a supereffective Bug-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 262);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.BUG;
		}

		public int naturalGiftPower()
		{
			return 80;
		}

		public Type naturalGiftType()
		{
			return Type.BUG;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttackType() == getType() && Type.getAdvantage(user, victim, b) > 1)
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class ChartiBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public ChartiBerry()
		{
			super(Namesies.CHARTI_BERRY_ITEM, "Weakens a supereffective Rock-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 263);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.ROCK;
		}

		public int naturalGiftPower()
		{
			return 80;
		}

		public Type naturalGiftType()
		{
			return Type.ROCK;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttackType() == getType() && Type.getAdvantage(user, victim, b) > 1)
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class KasibBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public KasibBerry()
		{
			super(Namesies.KASIB_BERRY_ITEM, "Weakens a supereffective Ghost-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 264);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.GHOST;
		}

		public int naturalGiftPower()
		{
			return 80;
		}

		public Type naturalGiftType()
		{
			return Type.GHOST;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttackType() == getType() && Type.getAdvantage(user, victim, b) > 1)
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class HabanBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public HabanBerry()
		{
			super(Namesies.HABAN_BERRY_ITEM, "Weakens a supereffective Dragon-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 265);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.DRAGON;
		}

		public int naturalGiftPower()
		{
			return 80;
		}

		public Type naturalGiftType()
		{
			return Type.DRAGON;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttackType() == getType() && Type.getAdvantage(user, victim, b) > 1)
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class ColburBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public ColburBerry()
		{
			super(Namesies.COLBUR_BERRY_ITEM, "Weakens a supereffective Dark-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 266);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.DARK;
		}

		public int naturalGiftPower()
		{
			return 80;
		}

		public Type naturalGiftType()
		{
			return Type.DARK;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttackType() == getType() && Type.getAdvantage(user, victim, b) > 1)
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class BabiriBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public BabiriBerry()
		{
			super(Namesies.BABIRI_BERRY_ITEM, "Weakens a supereffective Steel-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 267);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.STEEL;
		}

		public int naturalGiftPower()
		{
			return 80;
		}

		public Type naturalGiftType()
		{
			return Type.STEEL;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttackType() == getType() && Type.getAdvantage(user, victim, b) > 1)
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class ChilanBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public ChilanBerry()
		{
			super(Namesies.CHILAN_BERRY_ITEM, "Weakens a supereffective Normal-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 268);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.NORMAL;
		}

		public int naturalGiftPower()
		{
			return 80;
		}

		public Type naturalGiftType()
		{
			return Type.NORMAL;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttackType() == getType() && Type.getAdvantage(user, victim, b) > 1)
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class RoseliBerry extends Item implements Berry, OpponentPowerChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public RoseliBerry()
		{
			super(Namesies.ROSELI_BERRY_ITEM, "Weakens a supereffective Fairy-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 269);
			super.price = 20;
		}

		public Type getType()
		{
			return Type.FAIRY;
		}

		public int naturalGiftPower()
		{
			return 80;
		}

		public Type naturalGiftType()
		{
			return Type.FAIRY;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttackType() == getType() && Type.getAdvantage(user, victim, b) > 1)
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class LiechiBerry extends Item implements HealthTriggeredBerry
	{
		private static final long serialVersionUID = 1L;

		public LiechiBerry()
		{
			super(Namesies.LIECHI_BERRY_ITEM, "If held by a Pok\u00e9mon, it raises its Attack stat in a pinch.", BagCategory.BERRY, 270);
			super.price = 20;
		}

		public Stat toRaise()
		{
			return Stat.ATTACK;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toRaise().getName() + " increased!";
		}

		public String getHoldSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user, CastSource source)
		{
			if (user.getAttributes().modifyStage(user, user, 1, toRaise(), b, source))
			{
				return true;
			}
			
			return false;
		}

		public double healthTriggerRatio()
		{
			return 1/4.0;
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source)
		{
			if (!useHealthTriggerBerry(b, user, source))
			{
				return false;
			}
			
			String message = "";
			switch (source)
			{
				case USE_ITEM:
				message = getSuccessMessage(user);
				break;
				case HELD_ITEM:
				message = getHoldSuccessMessage(user);
				break;
				default:
				Global.error("Use item and held item are the only valid cast sources for berries.");
			}
			
			b.addMessage(message, user);
			
			if (user.hasAbility(Namesies.CHEEK_POUCH_ABILITY) && !user.fullHealth())
			{
				b.addMessage(user.getName() + "'s " + Namesies.CHEEK_POUCH_ABILITY.getName() + " restored its health!");
				user.healHealthFraction(1/3.0);
				b.addMessage("", user);
			}
			
			// Eat dat berry!!
			PokemonEffect.getEffect(Namesies.EATEN_BERRY_EFFECT).cast(b, user, user, source, false);
			
			return true;
		}

		public int naturalGiftPower()
		{
			return 100;
		}

		public Type naturalGiftType()
		{
			return Type.GRASS;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class GanlonBerry extends Item implements HealthTriggeredBerry
	{
		private static final long serialVersionUID = 1L;

		public GanlonBerry()
		{
			super(Namesies.GANLON_BERRY_ITEM, "If held by a Pok\u00e9mon, it raises its Defense stat in a pinch.", BagCategory.BERRY, 271);
			super.price = 20;
		}

		public Stat toRaise()
		{
			return Stat.DEFENSE;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toRaise().getName() + " increased!";
		}

		public String getHoldSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user, CastSource source)
		{
			if (user.getAttributes().modifyStage(user, user, 1, toRaise(), b, source))
			{
				return true;
			}
			
			return false;
		}

		public double healthTriggerRatio()
		{
			return 1/4.0;
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source)
		{
			if (!useHealthTriggerBerry(b, user, source))
			{
				return false;
			}
			
			String message = "";
			switch (source)
			{
				case USE_ITEM:
				message = getSuccessMessage(user);
				break;
				case HELD_ITEM:
				message = getHoldSuccessMessage(user);
				break;
				default:
				Global.error("Use item and held item are the only valid cast sources for berries.");
			}
			
			b.addMessage(message, user);
			
			if (user.hasAbility(Namesies.CHEEK_POUCH_ABILITY) && !user.fullHealth())
			{
				b.addMessage(user.getName() + "'s " + Namesies.CHEEK_POUCH_ABILITY.getName() + " restored its health!");
				user.healHealthFraction(1/3.0);
				b.addMessage("", user);
			}
			
			// Eat dat berry!!
			PokemonEffect.getEffect(Namesies.EATEN_BERRY_EFFECT).cast(b, user, user, source, false);
			
			return true;
		}

		public int naturalGiftPower()
		{
			return 100;
		}

		public Type naturalGiftType()
		{
			return Type.ICE;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class SalacBerry extends Item implements HealthTriggeredBerry
	{
		private static final long serialVersionUID = 1L;

		public SalacBerry()
		{
			super(Namesies.SALAC_BERRY_ITEM, "If held by a Pok\u00e9mon, it raises its Speed stat in a pinch.", BagCategory.BERRY, 272);
			super.price = 20;
		}

		public Stat toRaise()
		{
			return Stat.SPEED;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toRaise().getName() + " increased!";
		}

		public String getHoldSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user, CastSource source)
		{
			if (user.getAttributes().modifyStage(user, user, 1, toRaise(), b, source))
			{
				return true;
			}
			
			return false;
		}

		public double healthTriggerRatio()
		{
			return 1/4.0;
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source)
		{
			if (!useHealthTriggerBerry(b, user, source))
			{
				return false;
			}
			
			String message = "";
			switch (source)
			{
				case USE_ITEM:
				message = getSuccessMessage(user);
				break;
				case HELD_ITEM:
				message = getHoldSuccessMessage(user);
				break;
				default:
				Global.error("Use item and held item are the only valid cast sources for berries.");
			}
			
			b.addMessage(message, user);
			
			if (user.hasAbility(Namesies.CHEEK_POUCH_ABILITY) && !user.fullHealth())
			{
				b.addMessage(user.getName() + "'s " + Namesies.CHEEK_POUCH_ABILITY.getName() + " restored its health!");
				user.healHealthFraction(1/3.0);
				b.addMessage("", user);
			}
			
			// Eat dat berry!!
			PokemonEffect.getEffect(Namesies.EATEN_BERRY_EFFECT).cast(b, user, user, source, false);
			
			return true;
		}

		public int naturalGiftPower()
		{
			return 100;
		}

		public Type naturalGiftType()
		{
			return Type.FIGHTING;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class PetayaBerry extends Item implements HealthTriggeredBerry
	{
		private static final long serialVersionUID = 1L;

		public PetayaBerry()
		{
			super(Namesies.PETAYA_BERRY_ITEM, "If held by a Pok\u00e9mon, it raises its Speed stat in a pinch.", BagCategory.BERRY, 273);
			super.price = 20;
		}

		public Stat toRaise()
		{
			return Stat.SP_ATTACK;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toRaise().getName() + " increased!";
		}

		public String getHoldSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user, CastSource source)
		{
			if (user.getAttributes().modifyStage(user, user, 1, toRaise(), b, source))
			{
				return true;
			}
			
			return false;
		}

		public double healthTriggerRatio()
		{
			return 1/4.0;
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source)
		{
			if (!useHealthTriggerBerry(b, user, source))
			{
				return false;
			}
			
			String message = "";
			switch (source)
			{
				case USE_ITEM:
				message = getSuccessMessage(user);
				break;
				case HELD_ITEM:
				message = getHoldSuccessMessage(user);
				break;
				default:
				Global.error("Use item and held item are the only valid cast sources for berries.");
			}
			
			b.addMessage(message, user);
			
			if (user.hasAbility(Namesies.CHEEK_POUCH_ABILITY) && !user.fullHealth())
			{
				b.addMessage(user.getName() + "'s " + Namesies.CHEEK_POUCH_ABILITY.getName() + " restored its health!");
				user.healHealthFraction(1/3.0);
				b.addMessage("", user);
			}
			
			// Eat dat berry!!
			PokemonEffect.getEffect(Namesies.EATEN_BERRY_EFFECT).cast(b, user, user, source, false);
			
			return true;
		}

		public int naturalGiftPower()
		{
			return 100;
		}

		public Type naturalGiftType()
		{
			return Type.POISON;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class ApicotBerry extends Item implements HealthTriggeredBerry
	{
		private static final long serialVersionUID = 1L;

		public ApicotBerry()
		{
			super(Namesies.APICOT_BERRY_ITEM, "If held by a Pok\u00e9mon, it raises its Speed stat in a pinch.", BagCategory.BERRY, 274);
			super.price = 20;
		}

		public Stat toRaise()
		{
			return Stat.SP_DEFENSE;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toRaise().getName() + " increased!";
		}

		public String getHoldSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user, CastSource source)
		{
			if (user.getAttributes().modifyStage(user, user, 1, toRaise(), b, source))
			{
				return true;
			}
			
			return false;
		}

		public double healthTriggerRatio()
		{
			return 1/4.0;
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source)
		{
			if (!useHealthTriggerBerry(b, user, source))
			{
				return false;
			}
			
			String message = "";
			switch (source)
			{
				case USE_ITEM:
				message = getSuccessMessage(user);
				break;
				case HELD_ITEM:
				message = getHoldSuccessMessage(user);
				break;
				default:
				Global.error("Use item and held item are the only valid cast sources for berries.");
			}
			
			b.addMessage(message, user);
			
			if (user.hasAbility(Namesies.CHEEK_POUCH_ABILITY) && !user.fullHealth())
			{
				b.addMessage(user.getName() + "'s " + Namesies.CHEEK_POUCH_ABILITY.getName() + " restored its health!");
				user.healHealthFraction(1/3.0);
				b.addMessage("", user);
			}
			
			// Eat dat berry!!
			PokemonEffect.getEffect(Namesies.EATEN_BERRY_EFFECT).cast(b, user, user, source, false);
			
			return true;
		}

		public int naturalGiftPower()
		{
			return 100;
		}

		public Type naturalGiftType()
		{
			return Type.GROUND;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class MicleBerry extends Item implements HealthTriggeredBerry
	{
		private static final long serialVersionUID = 1L;

		public MicleBerry()
		{
			super(Namesies.MICLE_BERRY_ITEM, "If held by a Pok\u00e9mon, it raises its Accuracy stat in a pinch.", BagCategory.BERRY, 275);
			super.price = 20;
		}

		public Stat toRaise()
		{
			return Stat.ACCURACY;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + toRaise().getName() + " increased!";
		}

		public String getHoldSuccessMessage(ActivePokemon p)
		{
			return "";
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user, CastSource source)
		{
			if (user.getAttributes().modifyStage(user, user, 1, toRaise(), b, source))
			{
				return true;
			}
			
			return false;
		}

		public double healthTriggerRatio()
		{
			return 1/4.0;
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source)
		{
			if (!useHealthTriggerBerry(b, user, source))
			{
				return false;
			}
			
			String message = "";
			switch (source)
			{
				case USE_ITEM:
				message = getSuccessMessage(user);
				break;
				case HELD_ITEM:
				message = getHoldSuccessMessage(user);
				break;
				default:
				Global.error("Use item and held item are the only valid cast sources for berries.");
			}
			
			b.addMessage(message, user);
			
			if (user.hasAbility(Namesies.CHEEK_POUCH_ABILITY) && !user.fullHealth())
			{
				b.addMessage(user.getName() + "'s " + Namesies.CHEEK_POUCH_ABILITY.getName() + " restored its health!");
				user.healHealthFraction(1/3.0);
				b.addMessage("", user);
			}
			
			// Eat dat berry!!
			PokemonEffect.getEffect(Namesies.EATEN_BERRY_EFFECT).cast(b, user, user, source, false);
			
			return true;
		}

		public int naturalGiftPower()
		{
			return 100;
		}

		public Type naturalGiftType()
		{
			return Type.ROCK;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class KeeBerry extends Item implements Berry, TakeDamageEffect
	{
		private static final long serialVersionUID = 1L;

		public KeeBerry()
		{
			super(Namesies.KEE_BERRY_ITEM, "If held by a Pok\u00e9mon, this Berry will increase the Pok\u00e9mon's Defense stat when hit by a physical attack.", BagCategory.BERRY, 276);
			super.price = 20;
		}

		public boolean checkModify(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getAttack().getCategory() == Category.PHYSICAL && victim.getAttributes().modifyStage(victim, victim, 1, Stat.DEFENSE, b, CastSource.HELD_ITEM);
		}

		public int naturalGiftPower()
		{
			return 100;
		}

		public Type naturalGiftType()
		{
			return Type.FAIRY;
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (checkModify(b, user, victim))
			{
				victim.consumeItem(b);
			}
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class MarangaBerry extends Item implements Berry, TakeDamageEffect
	{
		private static final long serialVersionUID = 1L;

		public MarangaBerry()
		{
			super(Namesies.MARANGA_BERRY_ITEM, "If held by a Pok\u00e9mon, this Berry will increase the Pok\u00e9mon's Sp. Defense stat when hit by a special attack.", BagCategory.BERRY, 277);
			super.price = 20;
		}

		public boolean checkModify(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return user.getAttack().getCategory() == Category.SPECIAL && victim.getAttributes().modifyStage(victim, victim, 1, Stat.SP_DEFENSE, b, CastSource.HELD_ITEM);
		}

		public int naturalGiftPower()
		{
			return 100;
		}

		public Type naturalGiftType()
		{
			return Type.DARK;
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (checkModify(b, user, victim))
			{
				victim.consumeItem(b);
			}
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class JabocaBerry extends Item implements Berry, TakeDamageEffect
	{
		private static final long serialVersionUID = 1L;

		public JabocaBerry()
		{
			super(Namesies.JABOCA_BERRY_ITEM, "If held by a Pok\u00e9mon and a physical attack lands, the attacker also takes damage.", BagCategory.BERRY, 278);
			super.price = 20;
		}

		public Category getCategory()
		{
			return Category.PHYSICAL;
		}

		public int naturalGiftPower()
		{
			return 100;
		}

		public Type naturalGiftType()
		{
			return Type.DRAGON;
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().getCategory() == getCategory())
			{
				b.addMessage(user.getName() + " was hurt by " + victim.getName() + "'s " + this.name + "!");
				user.reduceHealthFraction(b, 1/8.0);
				victim.consumeItem(b);
			}
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class RowapBerry extends Item implements Berry, TakeDamageEffect
	{
		private static final long serialVersionUID = 1L;

		public RowapBerry()
		{
			super(Namesies.ROWAP_BERRY_ITEM, "If held by a Pok\u00e9mon and a special attack lands, the attacker also takes damage.", BagCategory.BERRY, 279);
			super.price = 20;
		}

		public Category getCategory()
		{
			return Category.SPECIAL;
		}

		public int naturalGiftPower()
		{
			return 100;
		}

		public Type naturalGiftType()
		{
			return Type.DARK;
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getAttack().getCategory() == getCategory())
			{
				b.addMessage(user.getName() + " was hurt by " + victim.getName() + "'s " + this.name + "!");
				user.reduceHealthFraction(b, 1/8.0);
				victim.consumeItem(b);
			}
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class CustapBerry extends Item implements Berry, PriorityChangeEffect
	{
		private static final long serialVersionUID = 1L;

		public CustapBerry()
		{
			super(Namesies.CUSTAP_BERRY_ITEM, "If held by a Pok\u00e9mon, it gets to move first just once in a pinch.", BagCategory.BERRY, 280);
			super.price = 20;
		}

		public int naturalGiftPower()
		{
			return 100;
		}

		public Type naturalGiftType()
		{
			return Type.GHOST;
		}

		public int changePriority(Battle b, ActivePokemon user, Integer priority)
		{
			if (user.getHPRatio() < 1/3.0)
			{
				if (this instanceof ConsumableItem)
				{
					user.consumeItem(b);
				}
				
				priority++;
			}
			
			return priority;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class EnigmaBerry extends Item implements Berry, TakeDamageEffect
	{
		private static final long serialVersionUID = 1L;

		public EnigmaBerry()
		{
			super(Namesies.ENIGMA_BERRY_ITEM, "If held by a Pok\u00e9mon, it restores its HP if it is hit by any supereffective attack.", BagCategory.BERRY, 281);
			super.price = 20;
		}

		public int naturalGiftPower()
		{
			return 100;
		}

		public Type naturalGiftType()
		{
			return Type.BUG;
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (!victim.fullHealth() && Type.getAdvantage(user, victim, b) > 1)
			{
				b.addMessage(victim.getName() + "'s " + this.name + " restored its health!");
				victim.healHealthFraction(.25);
				b.addMessage("", victim);
				victim.consumeItem(b);
			}
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class LansatBerry extends Item implements HealthTriggeredBerry
	{
		private static final long serialVersionUID = 1L;

		public LansatBerry()
		{
			super(Namesies.LANSAT_BERRY_ITEM, "If held by a Pok\u00e9mon, it raises its critical-hit ratio in a pinch.", BagCategory.BERRY, 282);
			super.price = 20;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " is getting pumped!";
		}

		public String getHoldSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " is getting pumped due to its " + this.name + "!";
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user, CastSource source)
		{
			// TODO: Yes I realize this is Lansat Berry and not Honey, but functionality needs to be implemented for Honey
			PokemonEffect.getEffect(Namesies.RAISE_CRITS_EFFECT).cast(b, user, user, source, false);
			return true;
		}

		public double healthTriggerRatio()
		{
			return 1/4.0;
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source)
		{
			if (!useHealthTriggerBerry(b, user, source))
			{
				return false;
			}
			
			String message = "";
			switch (source)
			{
				case USE_ITEM:
				message = getSuccessMessage(user);
				break;
				case HELD_ITEM:
				message = getHoldSuccessMessage(user);
				break;
				default:
				Global.error("Use item and held item are the only valid cast sources for berries.");
			}
			
			b.addMessage(message, user);
			
			if (user.hasAbility(Namesies.CHEEK_POUCH_ABILITY) && !user.fullHealth())
			{
				b.addMessage(user.getName() + "'s " + Namesies.CHEEK_POUCH_ABILITY.getName() + " restored its health!");
				user.healHealthFraction(1/3.0);
				b.addMessage("", user);
			}
			
			// Eat dat berry!!
			PokemonEffect.getEffect(Namesies.EATEN_BERRY_EFFECT).cast(b, user, user, source, false);
			
			return true;
		}

		public int naturalGiftPower()
		{
			return 100;
		}

		public Type naturalGiftType()
		{
			return Type.FLYING;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class StarfBerry extends Item implements HealthTriggeredBerry
	{
		private static final long serialVersionUID = 1L;
		private String holdMessage;
		private String useMessage;

		public StarfBerry()
		{
			super(Namesies.STARF_BERRY_ITEM, "If held by a Pok\u00e9mon, it raises a random stat in a pinch.", BagCategory.BERRY, 283);
			super.price = 20;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return useMessage;
		}

		public String getHoldSuccessMessage(ActivePokemon p)
		{
			return holdMessage;
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user, CastSource source)
		{
			holdMessage = "";
			useMessage = "";
			
			int rand = (int)(Math.random()*(Stat.NUM_BATTLE_STATS + 1));
			
			// Raise crit
			if (rand == Stat.NUM_BATTLE_STATS)
			{
				PokemonEffect.getEffect(Namesies.RAISE_CRITS_EFFECT).cast(b, user, user, source, false);
				holdMessage = user.getName() + " is getting pumped due to its " + this.name + "!";
				useMessage = user.getName() + " is getting pumped!";
				return true;
			}
			
			// Raise random battle stat
			Stat stat = Stat.getStat(rand, true);
			if (user.getAttributes().modifyStage(user, user, 1, stat, b, source))
			{
				// TODO: Test this and such
				useMessage = user.getName() + "'s " + stat.getName() + " increased!";
				return true;
			}
			
			return false;
		}

		public double healthTriggerRatio()
		{
			return 1/4.0;
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source)
		{
			if (!useHealthTriggerBerry(b, user, source))
			{
				return false;
			}
			
			String message = "";
			switch (source)
			{
				case USE_ITEM:
				message = getSuccessMessage(user);
				break;
				case HELD_ITEM:
				message = getHoldSuccessMessage(user);
				break;
				default:
				Global.error("Use item and held item are the only valid cast sources for berries.");
			}
			
			b.addMessage(message, user);
			
			if (user.hasAbility(Namesies.CHEEK_POUCH_ABILITY) && !user.fullHealth())
			{
				b.addMessage(user.getName() + "'s " + Namesies.CHEEK_POUCH_ABILITY.getName() + " restored its health!");
				user.healHealthFraction(1/3.0);
				b.addMessage("", user);
			}
			
			// Eat dat berry!!
			PokemonEffect.getEffect(Namesies.EATEN_BERRY_EFFECT).cast(b, user, user, source, false);
			
			return true;
		}

		public int naturalGiftPower()
		{
			return 100;
		}

		public Type naturalGiftType()
		{
			return Type.PSYCHIC;
		}

		public int flingDamage()
		{
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class CometShard extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public CometShard()
		{
			super(Namesies.COMET_SHARD_ITEM, "A shard which fell to the ground when a comet approached. A maniac will buy it for a high price.", BagCategory.MISC, 284);
			super.price = 120000;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class TinyMushroom extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public TinyMushroom()
		{
			super(Namesies.TINY_MUSHROOM_ITEM, "A small and rare mushroom. It is sought after by collectors.", BagCategory.MISC, 285);
			super.price = 500;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class BigMushroom extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public BigMushroom()
		{
			super(Namesies.BIG_MUSHROOM_ITEM, "A large and rare mushroom. It is sought after by collectors.", BagCategory.MISC, 286);
			super.price = 5000;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class BalmMushroom extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public BalmMushroom()
		{
			super(Namesies.BALM_MUSHROOM_ITEM, "A rare mushroom which gives off a nice fragrance. A maniac will buy it for a high price.", BagCategory.MISC, 287);
			super.price = 50000;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class Nugget extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public Nugget()
		{
			super(Namesies.NUGGET_ITEM, "A nugget of pure gold that gives off a lustrous gleam. It can be sold at a high price to shops.", BagCategory.MISC, 288);
			super.price = 10000;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class BigNugget extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public BigNugget()
		{
			super(Namesies.BIG_NUGGET_ITEM, "A big nugget of pure gold that gives off a lustrous gleam. A maniac will buy it for a high price.", BagCategory.MISC, 289);
			super.price = 60000;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class Pearl extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public Pearl()
		{
			super(Namesies.PEARL_ITEM, "A somewhat-small pearl that sparkles in a pretty silver color. It can be sold cheaply to shops.", BagCategory.MISC, 290);
			super.price = 1400;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class BigPearl extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public BigPearl()
		{
			super(Namesies.BIG_PEARL_ITEM, "A quite-large pearl that sparkles in a pretty silver color. It can be sold at a high price to shops.", BagCategory.MISC, 291);
			super.price = 7500;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class Stardust extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public Stardust()
		{
			super(Namesies.STARDUST_ITEM, "Lovely, red-colored sand with a loose, silky feel. It can be sold at a high price to shops.", BagCategory.MISC, 292);
			super.price = 2000;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class StarPiece extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public StarPiece()
		{
			super(Namesies.STAR_PIECE_ITEM, "A shard of a pretty gem that sparkles in a red color. It can be sold at a high price to shops.", BagCategory.MISC, 293);
			super.price = 9800;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class RareBone extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public RareBone()
		{
			super(Namesies.RARE_BONE_ITEM, "A bone that is extremely valuable for Pok\u00e9mon archeology. It can be sold for a high price to shops.", BagCategory.MISC, 294);
			super.price = 10000;
		}

		public int flingDamage()
		{
			return 100;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class Honey extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public Honey()
		{
			super(Namesies.HONEY_ITEM, "A sweet honey with a lush aroma that attracts wild Pok\u00e9mon when it is used in grass, caves, or on special trees.", BagCategory.MISC, 295);
			super.price = 100;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class Eviolite extends Item implements HoldItem, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public Eviolite()
		{
			super(Namesies.EVIOLITE_ITEM, "A mysterious evolutionary lump. When held, it raises the Defense and Sp. Def of a Pok\u00e9mon that can still evolve.", BagCategory.MISC, 296);
			super.price = 200;
		}

		public boolean isModifyStat(Stat s)
		{
			return s == Stat.DEFENSE || s == Stat.SP_DEFENSE;
		}

		public int flingDamage()
		{
			return 40;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (isModifyStat(s) && p.getPokemonInfo().getEvolution().canEvolve())
			{
				stat *= 1.5;
			}
			
			return stat;
		}
	}

	private static class HeartScale extends Item implements HoldItem
	{
		private static final long serialVersionUID = 1L;

		public HeartScale()
		{
			super(Namesies.HEART_SCALE_ITEM, "A pretty, heart-shaped scale that is extremely rare. It glows faintly in the colors of the rainbow.", BagCategory.MISC, 297);
			super.price = 100;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}
	}

	private static class Repel extends Item implements HoldItem, TrainerUseItem
	{
		private static final long serialVersionUID = 1L;

		public Repel()
		{
			super(Namesies.REPEL_ITEM, "An item that prevents weak wild Pok\u00e9mon from appearing for 100 steps after its use.", BagCategory.MISC, 298);
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
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
			super(Namesies.SUPER_REPEL_ITEM, "An item that prevents weak wild Pok\u00e9mon from appearing for 200 steps after its use.", BagCategory.MISC, 299);
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
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
			super(Namesies.MAX_REPEL_ITEM, "An item that prevents weak wild Pok\u00e9mon from appearing for 250 steps after its use.", BagCategory.MISC, 300);
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

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
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

	private static class AbilityCapsule extends Item implements PokemonUseItem
	{
		private static final long serialVersionUID = 1L;

		public AbilityCapsule()
		{
			super(Namesies.ABILITY_CAPSULE_ITEM, "A capsule that allows a Pok\u00e9mon with two Abilities to switch between these Abilities when it is used.", BagCategory.MISC, 301);
			super.price = 1000;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + "'s ability was changed to " + p.getAbility().getName() + "!";
		}

		public boolean use(CharacterData player, ActivePokemon p)
		{
			Ability other = Ability.getOtherAbility(p);
			if (other.namesies() == Namesies.NONE_ABILITY)
			{
				return false;
			}
			
			p.assignAbility(other);
			return true;
		}
	}

	private static class AssaultVest extends Item implements HoldItem, AttackSelectionEffect, StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public AssaultVest()
		{
			super(Namesies.ASSAULT_VEST_ITEM, "An item to be held by a Pok\u00e9mon. This offensive vest raises Sp. Def but prevents the use of status moves.", BagCategory.MISC, 302);
			super.price = 1000;
		}

		public boolean isModifyStat(Stat s)
		{
			return s == Stat.SP_DEFENSE;
		}

		public int flingDamage()
		{
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted)
		{
		}

		public boolean usable(ActivePokemon p, Move m)
		{
			return m.getAttack().getCategory() != Attack.Category.STATUS;
		}

		public String getUnusableMessage(ActivePokemon p)
		{
			return p.getName() + "'s " + this.name + " prevents the use of status moves!";
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (isModifyStat(s) && true)
			{
				stat *= 1.5;
			}
			
			return stat;
		}
	}

	private static class HoneClawsTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public HoneClawsTM()
		{
			super(Namesies.HONE_CLAWS_TM_ITEM, "The user sharpens its claws to boost its Attack stat and accuracy.", BagCategory.TM, 2015);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Hone Claws", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class DragonClawTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public DragonClawTM()
		{
			super(Namesies.DRAGON_CLAW_TM_ITEM, "The user slashes the target with huge, sharp claws.", BagCategory.TM, 2014);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Dragon Claw", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class PsyshockTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public PsyshockTM()
		{
			super(Namesies.PSYSHOCK_TM_ITEM, "The user materializes an odd psychic wave to attack the target. This attack does physical damage.", BagCategory.TM, 2010);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Psyshock", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class CalmMindTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public CalmMindTM()
		{
			super(Namesies.CALM_MIND_TM_ITEM, "The user quietly focuses its mind and calms its spirit to raise its Sp. Atk and Sp. Def stats.", BagCategory.TM, 2010);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Calm Mind", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class RoarTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public RoarTM()
		{
			super(Namesies.ROAR_TM_ITEM, "The target is scared off and replaced by another Pokémon in its party. In the wild, the battle ends.", BagCategory.TM, 2000);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Roar", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class ToxicTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public ToxicTM()
		{
			super(Namesies.TOXIC_TM_ITEM, "A move that leaves the target badly poisoned. Its poison damage worsens every turn.", BagCategory.TM, 2007);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Toxic", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class HailTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public HailTM()
		{
			super(Namesies.HAIL_TM_ITEM, "The user summons a hailstorm lasting five turns. It damages all Pokémon except the Ice type.", BagCategory.TM, 2005);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Hail", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class BulkUpTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public BulkUpTM()
		{
			super(Namesies.BULK_UP_TM_ITEM, "The user tenses its muscles to bulk up its body, boosting both its Attack and Defense stats.", BagCategory.TM, 2006);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Bulk Up", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class VenoshockTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public VenoshockTM()
		{
			super(Namesies.VENOSHOCK_TM_ITEM, "The user drenches the target in a special poisonous liquid. Its power is doubled if the target is poisoned.", BagCategory.TM, 2007);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Venoshock", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class HiddenPowerTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public HiddenPowerTM()
		{
			super(Namesies.HIDDEN_POWER_TM_ITEM, "A unique attack that varies in type and intensity depending on the Pokémon using it.", BagCategory.TM, 2000);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Hidden Power", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class SunnyDayTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public SunnyDayTM()
		{
			super(Namesies.SUNNY_DAY_TM_ITEM, "The user intensifies the sun for five turns, powering up Fire-type moves.", BagCategory.TM, 2001);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Sunny Day", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class TauntTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public TauntTM()
		{
			super(Namesies.TAUNT_TM_ITEM, "The target is taunted into a rage that allows it to use only attack moves for three turns.", BagCategory.TM, 2015);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Taunt", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class IceBeamTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public IceBeamTM()
		{
			super(Namesies.ICE_BEAM_TM_ITEM, "The target is struck with an icy-cold beam of energy. It may also freeze the target solid.", BagCategory.TM, 2005);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Ice Beam", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class BlizzardTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public BlizzardTM()
		{
			super(Namesies.BLIZZARD_TM_ITEM, "A howling blizzard is summoned to strike the opposing team. It may also freeze them solid.", BagCategory.TM, 2005);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Blizzard", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class HyperBeamTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public HyperBeamTM()
		{
			super(Namesies.HYPER_BEAM_TM_ITEM, "The target is attacked with a powerful beam. The user must rest on the next turn to regain its energy.", BagCategory.TM, 2000);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Hyper Beam", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class LightScreenTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public LightScreenTM()
		{
			super(Namesies.LIGHT_SCREEN_TM_ITEM, "A wondrous wall of light is put up to suppress damage from special attacks for five turns.", BagCategory.TM, 2010);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Light Screen", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class ProtectTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public ProtectTM()
		{
			super(Namesies.PROTECT_TM_ITEM, "It enables the user to evade all attacks. Its chance of failing rises if it is used in succession.", BagCategory.TM, 2000);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Protect", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class RainDanceTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public RainDanceTM()
		{
			super(Namesies.RAIN_DANCE_TM_ITEM, "The user summons a heavy rain that falls for five turns, powering up Water-type moves.", BagCategory.TM, 2002);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Rain Dance", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class RoostTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public RoostTM()
		{
			super(Namesies.ROOST_TM_ITEM, "The user lands and rests its body. It restores the user's HP by up to half of its max HP.", BagCategory.TM, 2009);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Roost", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class SafeguardTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public SafeguardTM()
		{
			super(Namesies.SAFEGUARD_TM_ITEM, "The user creates a protective field that prevents status problems for five turns.", BagCategory.TM, 2000);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Safeguard", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class SolarBeamTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public SolarBeamTM()
		{
			super(Namesies.SOLAR_BEAM_TM_ITEM, "A two-turn attack. The user gathers light, then blasts a bundled beam on the second turn.", BagCategory.TM, 2004);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Solar Beam", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class SmackDownTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public SmackDownTM()
		{
			super(Namesies.SMACK_DOWN_TM_ITEM, "The user throws a stone or projectile to attack an opponent. A flying Pokémon will fall to the ground when hit.", BagCategory.TM, 2012);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Smack Down", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class ThunderboltTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public ThunderboltTM()
		{
			super(Namesies.THUNDERBOLT_TM_ITEM, "A strong electric blast is loosed at the target. It may also leave the target with paralysis.", BagCategory.TM, 2003);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Thunderbolt", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class ThunderTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public ThunderTM()
		{
			super(Namesies.THUNDER_TM_ITEM, "A wicked thunderbolt is dropped on the target to inflict damage. It may also leave the target with paralysis.", BagCategory.TM, 2003);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Thunder", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class EarthquakeTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public EarthquakeTM()
		{
			super(Namesies.EARTHQUAKE_TM_ITEM, "The user sets off an earthquake that strikes those around it.", BagCategory.TM, 2008);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Earthquake", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class DigTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public DigTM()
		{
			super(Namesies.DIG_TM_ITEM, "The user burrows, then attacks on the second turn. It can also be used to exit dungeons.", BagCategory.TM, 2008);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Dig", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class PsychicTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public PsychicTM()
		{
			super(Namesies.PSYCHIC_TM_ITEM, "The target is hit by a strong telekinetic force. It may also reduce the target's Sp. Def stat.", BagCategory.TM, 2010);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Psychic", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class ShadowBallTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public ShadowBallTM()
		{
			super(Namesies.SHADOW_BALL_TM_ITEM, "The user hurls a shadowy blob at the target. It may also lower the target's Sp. Def stat.", BagCategory.TM, 2013);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Shadow Ball", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class BrickBreakTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public BrickBreakTM()
		{
			super(Namesies.BRICK_BREAK_TM_ITEM, "The user attacks with a swift chop. It can also break any barrier such as Light Screen and Reflect.", BagCategory.TM, 2006);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Brick Break", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class DoubleTeamTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public DoubleTeamTM()
		{
			super(Namesies.DOUBLE_TEAM_TM_ITEM, "By moving rapidly, the user makes illusory copies of itself to raise its evasiveness.", BagCategory.TM, 2000);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Double Team", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class ReflectTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public ReflectTM()
		{
			super(Namesies.REFLECT_TM_ITEM, "A wondrous wall of light is put up to suppress damage from physical attacks for five turns.", BagCategory.TM, 2010);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Reflect", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class SludgeWaveTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public SludgeWaveTM()
		{
			super(Namesies.SLUDGE_WAVE_TM_ITEM, "It swamps the area around the user with a giant sludge wave. It may also poison those hit.", BagCategory.TM, 2007);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Sludge Wave", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class FlamethrowerTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public FlamethrowerTM()
		{
			super(Namesies.FLAMETHROWER_TM_ITEM, "The target is scorched with an intense blast of fire. It may also leave the target with a burn.", BagCategory.TM, 2001);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Flamethrower", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class SludgeBombTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public SludgeBombTM()
		{
			super(Namesies.SLUDGE_BOMB_TM_ITEM, "Unsanitary sludge is hurled at the target. It may also poison the target.", BagCategory.TM, 2007);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Sludge Bomb", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class SandstormTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public SandstormTM()
		{
			super(Namesies.SANDSTORM_TM_ITEM, "A five-turn sandstorm is summoned to hurt all combatants except the Rock, Ground, and Steel types.", BagCategory.TM, 2012);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Sandstorm", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class FireBlastTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public FireBlastTM()
		{
			super(Namesies.FIRE_BLAST_TM_ITEM, "The target is attacked with an intense blast of all-consuming fire. It may also leave the target with a burn.", BagCategory.TM, 2001);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Fire Blast", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class RockTombTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public RockTombTM()
		{
			super(Namesies.ROCK_TOMB_TM_ITEM, "Boulders are hurled at the target. It also lowers the target's Speed by preventing its movement.", BagCategory.TM, 2012);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Rock Tomb", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class AerialAceTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public AerialAceTM()
		{
			super(Namesies.AERIAL_ACE_TM_ITEM, "The user confounds the foe with speed, then slashes. The attack lands without fail.", BagCategory.TM, 2009);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Aerial Ace", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class TormentTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public TormentTM()
		{
			super(Namesies.TORMENT_TM_ITEM, "The user torments and enrages the target, making it incapable of using the same move twice in a row.", BagCategory.TM, 2015);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Torment", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class FacadeTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public FacadeTM()
		{
			super(Namesies.FACADE_TM_ITEM, "An attack move that doubles its power if the user is poisoned, burned, or has paralysis.", BagCategory.TM, 2000);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Facade", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class FlameChargeTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public FlameChargeTM()
		{
			super(Namesies.FLAME_CHARGE_TM_ITEM, "The user cloaks itself with flame and attacks. Building up more power, it raises the user's Speed stat.", BagCategory.TM, 2001);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Flame Charge", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class RestTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public RestTM()
		{
			super(Namesies.REST_TM_ITEM, "The user goes to sleep for two turns. It fully restores the user's HP and heals any status problem.", BagCategory.TM, 2010);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Rest", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class AttractTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public AttractTM()
		{
			super(Namesies.ATTRACT_TM_ITEM, "If it is the opposite gender of the user, the target becomes infatuated and less likely to attack.", BagCategory.TM, 2000);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Attract", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class ThiefTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public ThiefTM()
		{
			super(Namesies.THIEF_TM_ITEM, "The user attacks and steals the target's held item simultaneously. It can't steal if the user holds an item.", BagCategory.TM, 2015);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Thief", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class LowSweepTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public LowSweepTM()
		{
			super(Namesies.LOW_SWEEP_TM_ITEM, "The user attacks the target's legs swiftly, reducing the target's Speed stat.", BagCategory.TM, 2006);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Low Sweep", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class RoundTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public RoundTM()
		{
			super(Namesies.ROUND_TM_ITEM, "The user attacks the target with a song.", BagCategory.TM, 2000);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Round", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class EchoedVoiceTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public EchoedVoiceTM()
		{
			super(Namesies.ECHOED_VOICE_TM_ITEM, "The user attacks the target with an echoing voice. If this move is used every turn, it does greater damage.", BagCategory.TM, 2000);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Echoed Voice", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class OverheatTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public OverheatTM()
		{
			super(Namesies.OVERHEAT_TM_ITEM, "The user attacks the target at full power. The attack's recoil harshly reduces the user's Sp. Atk stat.", BagCategory.TM, 2001);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Overheat", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class SteelWingTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public SteelWingTM()
		{
			super(Namesies.STEEL_WING_TM_ITEM, "The target is hit with wings of steel. It may also raise the user's Defense stat.", BagCategory.TM, 2016);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Steel Wing", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class FocusBlastTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public FocusBlastTM()
		{
			super(Namesies.FOCUS_BLAST_TM_ITEM, "The user heightens its mental focus and unleashes its power. It may also lower the target's Sp. Def.", BagCategory.TM, 2006);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Focus Blast", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class EnergyBallTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public EnergyBallTM()
		{
			super(Namesies.ENERGY_BALL_TM_ITEM, "The user draws power from nature and fires it at the target. It may also lower the target's Sp. Def.", BagCategory.TM, 2004);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Energy Ball", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class FalseSwipeTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public FalseSwipeTM()
		{
			super(Namesies.FALSE_SWIPE_TM_ITEM, "A restrained attack that prevents the target from fainting. The target is left with at least 1 HP.", BagCategory.TM, 2000);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("False Swipe", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class ScaldTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public ScaldTM()
		{
			super(Namesies.SCALD_TM_ITEM, "The user shoots boiling hot water at its target. It may also leave the target with a burn.", BagCategory.TM, 2002);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Scald", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class FlingTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public FlingTM()
		{
			super(Namesies.FLING_TM_ITEM, "The user flings its held item at the target to attack. Its power and effects depend on the item.", BagCategory.TM, 2015);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Fling", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class ChargeBeamTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public ChargeBeamTM()
		{
			super(Namesies.CHARGE_BEAM_TM_ITEM, "The user attacks with an electric charge. The user may use any remaining electricity to raise its Sp. Atk stat.", BagCategory.TM, 2003);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Charge Beam", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class SkyDropTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public SkyDropTM()
		{
			super(Namesies.SKY_DROP_TM_ITEM, "The user takes the target into the sky, then slams it into the ground.", BagCategory.TM, 2009);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Sky Drop", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class IncinerateTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public IncinerateTM()
		{
			super(Namesies.INCINERATE_TM_ITEM, "The user attacks the target with fire. If the target is holding a Berry, the Berry becomes burnt up and unusable.", BagCategory.TM, 2001);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Incinerate", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class WillOWispTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public WillOWispTM()
		{
			super(Namesies.WILL_OWISP_TM_ITEM, "The user shoots a sinister, bluish-white flame at the target to inflict a burn.", BagCategory.TM, 2001);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Will-O-Wisp", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class AcrobaticsTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public AcrobaticsTM()
		{
			super(Namesies.ACROBATICS_TM_ITEM, "The user nimbly strikes the target. If the user is not holding an item, this attack inflicts massive damage.", BagCategory.TM, 2009);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Acrobatics", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class EmbargoTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public EmbargoTM()
		{
			super(Namesies.EMBARGO_TM_ITEM, "It prevents the target from using its held item. Its Trainer is also prevented from using items on it.", BagCategory.TM, 2015);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Embargo", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class ExplosionTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public ExplosionTM()
		{
			super(Namesies.EXPLOSION_TM_ITEM, "The user explodes to inflict damage on those around it. The user faints upon using this move.", BagCategory.TM, 2000);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Explosion", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class ShadowClawTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public ShadowClawTM()
		{
			super(Namesies.SHADOW_CLAW_TM_ITEM, "The user slashes with a sharp claw made from shadows. Critical hits land more easily.", BagCategory.TM, 2013);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Shadow Claw", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class PaybackTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public PaybackTM()
		{
			super(Namesies.PAYBACK_TM_ITEM, "If the user moves after the target, this attack's power will be doubled.", BagCategory.TM, 2015);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Payback", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class RetaliateTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public RetaliateTM()
		{
			super(Namesies.RETALIATE_TM_ITEM, "The user gets revenge for a fainted ally. If an ally fainted in the previous turn, this attack's damage increases.", BagCategory.TM, 2000);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Retaliate", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class GigaImpactTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public GigaImpactTM()
		{
			super(Namesies.GIGA_IMPACT_TM_ITEM, "The user charges at the target using every bit of its power. The user must rest on the next turn.", BagCategory.TM, 2000);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Giga Impact", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class RockPolishTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public RockPolishTM()
		{
			super(Namesies.ROCK_POLISH_TM_ITEM, "The user polishes its body to reduce drag. It can sharply raise the Speed stat.", BagCategory.TM, 2012);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Rock Polish", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class FlashTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public FlashTM()
		{
			super(Namesies.FLASH_TM_ITEM, "The user flashes a bright light that cuts the target's accuracy. It can also be used to illuminate caves.", BagCategory.TM, 2000);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Flash", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class StoneEdgeTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public StoneEdgeTM()
		{
			super(Namesies.STONE_EDGE_TM_ITEM, "The user stabs the foe with sharpened stones from below. It has a high critical-hit ratio.", BagCategory.TM, 2012);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Stone Edge", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class VoltSwitchTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public VoltSwitchTM()
		{
			super(Namesies.VOLT_SWITCH_TM_ITEM, "After making its attack, the user rushes back to switch places with a party Pokémon in waiting.", BagCategory.TM, 2003);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Volt Switch", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class ThunderWaveTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public ThunderWaveTM()
		{
			super(Namesies.THUNDER_WAVE_TM_ITEM, "A weak electric charge is launched at the target. It causes paralysis if it hits.", BagCategory.TM, 2003);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Thunder Wave", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class GyroBallTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public GyroBallTM()
		{
			super(Namesies.GYRO_BALL_TM_ITEM, "The user tackles the target with a high-speed spin. The slower the user, the greater the damage.", BagCategory.TM, 2016);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Gyro Ball", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class SwordsDanceTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public SwordsDanceTM()
		{
			super(Namesies.SWORDS_DANCE_TM_ITEM, "A frenetic dance to uplift the fighting spirit. It sharply raises the user's Attack stat.", BagCategory.TM, 2000);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Swords Dance", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class StruggleBugTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public StruggleBugTM()
		{
			super(Namesies.STRUGGLE_BUG_TM_ITEM, "While resisting, the user attacks the opposing Pokémon. The targets' Sp. Atk stat is reduced.", BagCategory.TM, 2011);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Struggle Bug", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class PsychUpTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public PsychUpTM()
		{
			super(Namesies.PSYCH_UP_TM_ITEM, "The user hypnotizes itself into copying any stat change made by the target.", BagCategory.TM, 2000);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Psych Up", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class BulldozeTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public BulldozeTM()
		{
			super(Namesies.BULLDOZE_TM_ITEM, "The user stomps down on the ground and attacks everything in the area. Hit Pokémon's Speed stat is reduced.", BagCategory.TM, 2008);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Bulldoze", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class FrostBreathTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public FrostBreathTM()
		{
			super(Namesies.FROST_BREATH_TM_ITEM, "The user blows a cold breath on the target. This attack always results in a critical hit.", BagCategory.TM, 2005);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Frost Breath", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class RockSlideTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public RockSlideTM()
		{
			super(Namesies.ROCK_SLIDE_TM_ITEM, "Large boulders are hurled at the opposing team to inflict damage. It may also make the targets flinch.", BagCategory.TM, 2012);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Rock Slide", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class XScissorTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public XScissorTM()
		{
			super(Namesies.XSCISSOR_TM_ITEM, "The user slashes at the target by crossing its scythes or claws as if they were a pair of scissors.", BagCategory.TM, 2011);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("X-Scissor", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class DragonTailTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public DragonTailTM()
		{
			super(Namesies.DRAGON_TAIL_TM_ITEM, "The user knocks away the target and drags out another Pokémon in its party. In the wild, the battle ends.", BagCategory.TM, 2014);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Dragon Tail", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class InfestationTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public InfestationTM()
		{
			super(Namesies.INFESTATION_TM_ITEM, "The target is infested and attacked for four to five turns. The target can't flee during this time.", BagCategory.TM, 2011);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Infestation", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class PoisonJabTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public PoisonJabTM()
		{
			super(Namesies.POISON_JAB_TM_ITEM, "The target is stabbed with a tentacle or arm steeped in poison. It may also poison the target.", BagCategory.TM, 2007);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Poison Jab", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class DreamEaterTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public DreamEaterTM()
		{
			super(Namesies.DREAM_EATER_TM_ITEM, "The user eats the dreams of a sleeping target. It absorbs half the damage caused to heal the user's HP.", BagCategory.TM, 2010);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Dream Eater", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class GrassKnotTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public GrassKnotTM()
		{
			super(Namesies.GRASS_KNOT_TM_ITEM, "The user snares the target with grass and trips it. The heavier the target, the greater the damage.", BagCategory.TM, 2004);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Grass Knot", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class SwaggerTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public SwaggerTM()
		{
			super(Namesies.SWAGGER_TM_ITEM, "The user enrages and confuses the target. However, it also sharply raises the target's Attack stat.", BagCategory.TM, 2000);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Swagger", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class SleepTalkTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public SleepTalkTM()
		{
			super(Namesies.SLEEP_TALK_TM_ITEM, "While it is asleep, the user randomly uses one of the moves it knows.", BagCategory.TM, 2000);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Sleep Talk", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class UTurnTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public UTurnTM()
		{
			super(Namesies.UTURN_TM_ITEM, "After making its attack, the user rushes back to switch places with a party Pokémon in waiting.", BagCategory.TM, 2011);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("U-turn", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class SubstituteTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public SubstituteTM()
		{
			super(Namesies.SUBSTITUTE_TM_ITEM, "The user makes a copy of itself using some of its HP. The copy serves as the user's decoy.", BagCategory.TM, 2000);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Substitute", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class FlashCannonTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public FlashCannonTM()
		{
			super(Namesies.FLASH_CANNON_TM_ITEM, "The user gathers all its light energy and releases it at once. It may also lower the target's Sp. Def stat.", BagCategory.TM, 2016);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Flash Cannon", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class TrickRoomTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public TrickRoomTM()
		{
			super(Namesies.TRICK_ROOM_TM_ITEM, "The user creates a bizarre area in which slower Pokémon get to move first for five turns.", BagCategory.TM, 2010);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Trick Room", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class WildChargeTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public WildChargeTM()
		{
			super(Namesies.WILD_CHARGE_TM_ITEM, "The user shrouds itself in electricity and smashes into its target. It also damages the user a little.", BagCategory.TM, 2003);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Wild Charge", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class RockSmashTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public RockSmashTM()
		{
			super(Namesies.ROCK_SMASH_TM_ITEM, "The user attacks with a punch that can shatter a rock. It may also lower the target's Defense stat.", BagCategory.TM, 2006);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Rock Smash", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class SnarlTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public SnarlTM()
		{
			super(Namesies.SNARL_TM_ITEM, "The user yells as if it is ranting about something, making the target's Sp. Atk stat decrease.", BagCategory.TM, 2015);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Snarl", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class NaturePowerTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public NaturePowerTM()
		{
			super(Namesies.NATURE_POWER_TM_ITEM, "An attack that makes use of nature's power. Its effects vary depending on the user's environment.", BagCategory.TM, 2000);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Nature Power", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class DarkPulseTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public DarkPulseTM()
		{
			super(Namesies.DARK_PULSE_TM_ITEM, "The user releases a horrible aura imbued with dark thoughts. It may also make the target flinch.", BagCategory.TM, 2015);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Dark Pulse", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class PowerUpPunchTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public PowerUpPunchTM()
		{
			super(Namesies.POWER_UP_PUNCH_TM_ITEM, "Striking opponents over and over makes the user's fists harder. Hitting a target raises the Attack stat.", BagCategory.TM, 2006);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Power-Up Punch", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class DazzlingGleamTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public DazzlingGleamTM()
		{
			super(Namesies.DAZZLING_GLEAM_TM_ITEM, "The user damages opposing Pokémon by emitting a powerful flash.", BagCategory.TM, 2017);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Dazzling Gleam", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class ConfideTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public ConfideTM()
		{
			super(Namesies.CONFIDE_TM_ITEM, "The user tells the target a secret, and the target loses its ability to concentrate. This lowers the target's Sp. Atk stat.", BagCategory.TM, 2000);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Confide", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class CutTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public CutTM()
		{
			super(Namesies.CUT_TM_ITEM, "The target is cut with a scythe or a claw. It can also be used to cut down thin trees.", BagCategory.TM, 2000);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Cut", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class FlyTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public FlyTM()
		{
			super(Namesies.FLY_TM_ITEM, "The user soars, then strikes its target on the second turn. It can also be used for flying to any familiar town.", BagCategory.TM, 2009);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Fly", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class SurfTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public SurfTM()
		{
			super(Namesies.SURF_TM_ITEM, "It swamps the area around the user with a giant wave. It can also be used for crossing water.", BagCategory.TM, 2002);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Surf", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class StrengthTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public StrengthTM()
		{
			super(Namesies.STRENGTH_TM_ITEM, "The target is slugged with a punch thrown at maximum power. It can also be used to move heavy boulders.", BagCategory.TM, 2000);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Strength", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class WaterfallTM extends Item implements MoveUseItem
	{
		private static final long serialVersionUID = 1L;

		public WaterfallTM()
		{
			super(Namesies.WATERFALL_TM_ITEM, "The user charges at the target and may make it flinch. It can also be used to climb a waterfall.", BagCategory.TM, 2002);
		}

		public Attack getAttack()
		{
			return Attack.getAttack(Namesies.getValueOf("Waterfall", NamesiesType.ATTACK));
		}

		public boolean use(ActivePokemon p, Move m)
		{
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies()))
			{
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().isTmMove(attack.namesies()))
			{
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES)
			{
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++)
			{
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies())
				{
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p)
		{
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}
}
